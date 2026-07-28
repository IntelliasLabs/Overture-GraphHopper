package com.graphhopper.reader.overture;

import com.carrotsearch.hppc.LongIntScatterMap;
import com.graphhopper.reader.DataReader;
import com.graphhopper.reader.DataReaderConfig;
import com.graphhopper.reader.dem.EdgeElevationSmoothingMovingAverage;
import com.graphhopper.reader.dem.EdgeElevationSmoothingRamer;
import com.graphhopper.reader.dem.EdgeSampling;
import com.graphhopper.reader.dem.ElevationProvider;
import com.graphhopper.reader.overture.aws.S3ParquetInputFile;
import com.graphhopper.reader.overture.parser.OvertureParser;
import com.graphhopper.reader.overture.parser.parquet.OvertureParquetParser;
import com.graphhopper.reader.overture.parsers.*;
import com.graphhopper.reader.overture.road.flags.OvertureRoadFlags;
import com.graphhopper.reader.overture.road.segment.OvertureConnector;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.reader.overture.road.segment.spliter.SegmentSplitter;
import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.AreaIndex;
import com.graphhopper.routing.util.CustomArea;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import org.jetbrains.annotations.Nullable;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

/**
 * Reads Overture Maps data and imports it into a GraphHopper {@link BaseGraph}.
 * <p>
 * This class implements the {@link DataReader} interface, allowing it to be used
 * within the standard GraphHopper import pipeline. It supports configuring the source
 * data either via a local file or an S3 bucket location.
 * </p>
 * <p>
 * The reader maintains a mapping of Overture entity IDs to internal GraphHopper node IDs
 * to ensure topology is correctly reconstructed during the read process.
 * </p>
 */
public class OvertureReader implements DataReader {

    private static final Logger logger = LoggerFactory.getLogger(OvertureReader.class);
    /**
     * The file size threshold (in bytes) used to determine the S3 data access strategy.
     */
    private static final long STREAMING_THRESHOLD_BYTES = 20 * 1024 * 1024;

    /**
     * The underlying graph storage where the network will be written.
     */
    private final BaseGraph graph;

    /**
     * The local file path to the Overture data (if reading from a file).
     */
    private File overtureFile;

    /**
     * The URL endpoint for the S3 compatible storage (if reading from S3).
     */
    private String s3Url;

    /**
     * The S3 bucket name containing the Overture data.
     */
    private String s3Bucket;

    /**
     * The specific key (path) within the S3 bucket to the Overture data.
     */
    private String s3Key;

    /**
     * Client required for S3 streaming operations. Needs to be injected manually via setS3Client().
     */
    private S3Client s3Client;

    /**
     * Maps rounded coordinates to GraphHopper's internal integer node IDs.
     *
     * <p>The fallback identity, used for sub-segment boundaries created by a property range rather than
     * a connector. Both boundaries of such a split are computed from one geometry, so they agree
     * exactly and coordinates are a sound key there.
     */
    private final LongIntScatterMap nodeMap;

    /**
     * Maps Overture connector ids to GraphHopper's internal integer node IDs.
     *
     * <p>The primary identity. See {@link #getOrCreateNode} for why coordinates alone cannot express
     * Overture's topology.
     */
    private final ConnectorNodeMap connectorNodeMap = new ConnectorNodeMap();

    /**
     * Provider for elevation data (DEM) to support 3D routing.
     */
    private ElevationProvider elevationProvider;

    /**
     * Spatial index for looking up custom areas during import.
     */
    private AreaIndex<CustomArea> areaIndex;


    /**
     * The lookup registry used to retrieve specific {@link EncodedValue} instances
     * required for populating edge attributes during the import process.
     */
    private EncodedValueLookup encodedValueLookup;

    /**
     * The parsers applied to every sub-segment, assembled from the import registry.
     *
     * <p>When {@code null} the reader builds a default set from the encoded values present in the
     * lookup, which is what the deprecated single-argument constructor relies on.
     */
    private OvertureParsers parsers;

    /**
     * The source-agnostic import settings.
     *
     * <p>Applied here: {@code maxWayPointDistance} and {@code elevationMaxWayPointDistance} (geometry
     * simplification), {@code longEdgeSamplingDistance}, {@code elevationSmoothing} with its two
     * tuning values, {@code defaultElevation}, and — through the parser props — {@code parseWayNames}
     * and {@code preferredLanguage}.
     *
     * <p>Two settings genuinely do not apply. {@code workerThreads} parallelises OSM's multi-pass node
     * scan; Overture arrives as self-contained segments read in a single pass, so there is no pass to
     * parallelise. {@code ignoredHighways} filters OSM {@code highway} tag values, which have no
     * Overture equivalent — the closest analogue would be filtering on subclass, and inventing that
     * mapping silently under an OSM-named setting would be worse than not honouring it.
     */
    private DataReaderConfig config = new DataReaderConfig();

    /**
     * Geometry simplification, configured from {@link #config}.
     *
     * <p>Overture geometry is far denser than the routing graph needs. Not simplifying it was costing
     * storage on every edge and was the reason {@code maxWayPointDistance} appeared to do nothing on an
     * Overture import.
     */
    private final RamerDouglasPeucker simplifyAlgo = new RamerDouglasPeucker();

    /**
     * Constructs a reader with an explicitly assembled parser pipeline and import settings.
     *
     * <p>Preferred over the other constructors: the parsers are resolved and ordered from {@code
     * graph.encoded_values} through the import registry, so a declared value that Overture cannot fill
     * is reported at startup rather than discovered later.
     *
     * @param graph the {@link BaseGraph} to store the imported data
     * @param parsers the parsers to apply, in execution order
     * @param config the import settings; see {@link #config} for which ones apply here
     */
    public OvertureReader(BaseGraph graph, OvertureParsers parsers, DataReaderConfig config) {
        this.graph = graph;
        this.nodeMap = new LongIntScatterMap();
        this.parsers = parsers;
        this.config = config;
        this.simplifyAlgo.setMaxDistance(config.getMaxWayPointDistance());
        this.simplifyAlgo.setElevationMaxDistance(config.getElevationMaxWayPointDistance());
    }

    /**
     * Constructs a reader with an explicitly assembled parser pipeline and default import settings.
     *
     * @param graph the {@link BaseGraph} to store the imported data
     * @param parsers the parsers to apply, in execution order
     */
    public OvertureReader(BaseGraph graph, OvertureParsers parsers) {
        this(graph, parsers, new DataReaderConfig());
    }

    /**
     * Constructs a new OvertureReader.
     *
     * @param graph the {@link BaseGraph} to store the imported data.
     * @deprecated use {@link #OvertureReader(BaseGraph, OvertureParsers, DataReaderConfig)}. This
     *     constructor defers parser assembly until {@link #readGraph()}, building a default set from
     *     whichever encoded values {@link #setEncodedValueLookup} supplies, which cannot report
     *     declared-but-unfillable values because it never sees the declarations.
     */
    @Deprecated
    public OvertureReader(BaseGraph graph) {
        this.graph = graph;
        this.nodeMap = new LongIntScatterMap();
    }

    /**
     * Gets the graph storage instance.
     *
     * @return the {@link BaseGraph} instance.
     */
    public BaseGraph getGraph() {
        return graph;
    }

    /**
     * Gets the currently configured local Overture file.
     *
     * @return the file object, or null if not set.
     */
    public File getOvertureFile() {
        return overtureFile;
    }

    /**
     * Gets the configured S3 endpoint URL.
     *
     * @return the S3 URL string.
     */
    public String getS3Url() {
        return s3Url;
    }

    /**
     * Gets the configured S3 bucket name.
     *
     * @return the bucket name.
     */
    public String getS3Bucket() {
        return s3Bucket;
    }

    /**
     * Gets the configured S3 key (path).
     *
     * @return the S3 key string.
     */
    public String getS3Key() {
        return s3Key;
    }

    /**
     * Gets the internal mapping of Overture IDs to graph node IDs.
     *
     * @return the map of ID strings to node integers.
     */
    public LongIntScatterMap getNodeMap() {
        return nodeMap;
    }

    /**
     * Gets the configured elevation provider.
     *
     * @return the {@link ElevationProvider} instance.
     */
    public ElevationProvider getElevationProvider() {
        return elevationProvider;
    }

    /**
     * Gets the configured area index.
     *
     * @return the {@link AreaIndex} instance.
     */
    public AreaIndex<CustomArea> getAreaIndex() {
        return areaIndex;
    }


    /**
     * Sets the encoded value lookup for populating edge attributes.
     * This should be called after construction when using the DataReaderInitializer pattern.
     *
     * @param encodedValueLookup the lookup for encoded values
     * @return this {@link OvertureReader} for method chaining
     */
    public OvertureReader setEncodedValueLookup(EncodedValueLookup encodedValueLookup) {
        this.encodedValueLookup = encodedValueLookup;
        return this;
    }

    /**
     * Configures the S3 source by parsing a standard S3 URL.
     * <p>
     * Expected format: {@code s3://bucket-name/path/to/key}
     * </p>
     *
     * @param s3Url the full S3 URL string.
     * @return this {@link OvertureReader} for method chaining.
     * @throws IllegalArgumentException if the URL does not start with "s3://" or is missing the bucket/key.
     */
    public OvertureReader setS3Source(String s3Url) {
        if (s3Url == null || !s3Url.startsWith("s3://")) {
            throw new IllegalArgumentException("S3 URL must start with 's3://'");
        }

        String remaining = s3Url.substring("s3://".length());
        int firstSlashIndex = remaining.indexOf('/');

        if (firstSlashIndex == -1 || firstSlashIndex == remaining.length() - 1) {
            throw new IllegalArgumentException(
                    "S3 URL must contain a bucket and a key (format: s3://bucket/key)");
        }

        this.s3Bucket = remaining.substring(0, firstSlashIndex);
        this.s3Key = remaining.substring(firstSlashIndex + 1);
        return this;
    }

    /**
     * Configures the S3 source by directly setting the bucket and key.
     *
     * @param bucket the S3 bucket name.
     * @param key    the S3 object key.
     * @return this {@link OvertureReader} for method chaining.
     */
    public OvertureReader setS3Source(String bucket, String key) {
        this.s3Bucket = bucket;
        this.s3Key = key;
        return this;
    }

    /**
     * Sets the {@link S3Client} to be used for cloud-based data access.
     *
     * @param s3Client the authenticated S3 client instance
     * @return this {@link OvertureReader} for method chaining
     */
    public OvertureReader setS3Client(S3Client s3Client) {
        this.s3Client = s3Client;
        return this;
    }

    /**
     * Sets the local file to read from, the alternative to {@link #setS3Source(String)}.
     *
     * @param file the input file.
     * @return this {@link OvertureReader} for method chaining.
     */
    public OvertureReader setFile(File file) {
        this.overtureFile = file;
        return this;
    }

    /**
     * Reads the graph data from the configured source (File or S3) and populates the graph.
     *
     * @throws IOException if an I/O error occurs during reading.
     */
    @Override
    public void readGraph() throws IOException {
        Iterable<OvertureRoadSegment> segments = parseData();

        NodeAccess nodeAccess = graph.getNodeAccess();
        int edgeCount = 0;
        int skippedCount = 0;
        long segmentCount = 0;

        // Resolved on the first segment rather than up front. Streaming means the segment count is not
        // known until the source is drained, and a source with nothing in it must not demand a parser
        // pipeline - which is what the empty-segment early return used to guarantee.
        OvertureParsers segmentParsers = null;
        EdgeIntAccess edgeIntAccess = graph.getEdgeAccess();

        try {
            for (OvertureRoadSegment segment : segments) {
                if (segmentParsers == null) segmentParsers = resolveParsers();
                segmentCount++;
                List<OvertureRoadSegment> subsegments = SegmentSplitter.split(segment);
                for (OvertureRoadSegment subsegment : subsegments) {

                    ///  Skip if segment abandoned or under construction
                    boolean needsSkipping = subsegment.getProperties().getFlags().stream()
                            .anyMatch(OvertureRoadFlags::shouldSkip);
                    if (needsSkipping) {
                        skippedCount++;
                        continue;
                    }

                    LineString lineString = subsegment.getLineString();

                    int numPoints = (lineString != null) ? lineString.getNumPoints() : 0;
                    if (numPoints < 2) {
                        skippedCount++;
                        continue;
                    }

                    /// Using the coordinate inversion: y=Lat, x=Lon
                    Coordinate startCoord = lineString.getCoordinateN(0);
                    Coordinate endCoord = lineString.getCoordinateN(numPoints - 1);
                    int fromId = getOrCreateNode(
                            connectorIdAt(subsegment, 0.0), startCoord.y, startCoord.x, nodeAccess);
                    int toId =
                            getOrCreateNode(connectorIdAt(subsegment, 1.0), endCoord.y, endCoord.x, nodeAccess);
                    if (fromId == toId) continue;

                    PointList fullGeometry = processGeometry(convertToPointList(lineString));
                    PointList intermediatePoints = extractIntermediatePoints(fullGeometry);

                    // Deliberately measured on the source geometry rather than the processed one. The OSM
                    // reader derives distance from its simplified point list; here the segment already knows
                    // its own length, and taking it from the simplified geometry instead would shorten every
                    // edge as a side effect of a storage setting.
                    double distance = subsegment.calculateDistance();

                    EdgeIteratorState edge =
                            graph.edge(fromId, toId).setDistance(distance).setWayGeometry(intermediatePoints);

                    // Every attribute is written by a registered parser, in an order the import registry
                    // determines from declared dependencies — max_speed before car_average_speed, for
                    // instance, because the latter reads the former back off the edge.
                    segmentParsers.handleSegment(
                            edge, subsegment, new OvertureSegmentContext(fullGeometry, areaIndex, edgeIntAccess));

                    edgeCount++;
                }
                if (edgeCount % 50000 == 0) {
                    logger.info("Progress: {} edges created...", edgeCount);
                }
            }
        } finally {
            // The streaming source holds the open Parquet reader. A plain List - which is what the
            // deprecated path and the unit tests supply - is not closeable and needs nothing here.
            if (segments instanceof AutoCloseable closeable) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    logger.warn("Failed to close the segment source", e);
                }
            }
        }

        if (segmentCount == 0) {
            logger.info("No segments found to processed");
            return;
        }
        logger.info(
                "Finished readGraph. Segments: {}, Created: {}, Skipped: {}",
                segmentCount,
                edgeCount,
                skippedCount);

        if (elevationProvider != null) {
            // Mirrors the OSM reader: elevation providers hold tile caches and file handles.
            elevationProvider.release();
        }
    }

    /**
     * Returns the parser pipeline to apply, building a default one if none was supplied.
     *
     * @return the parsers, never empty in a correctly configured import
     * @throws IllegalStateException if neither parsers nor an encoded-value lookup were provided
     */
    private OvertureParsers resolveParsers() {
        if (parsers != null) return parsers;

        if (encodedValueLookup == null) {
            throw new IllegalStateException("Neither parsers nor an EncodedValueLookup were provided."
                    + " Construct OvertureReader with OvertureParsers, or call setEncodedValueLookup().");
        }
        // Deprecated path: assemble from whatever the lookup holds. Ordering still comes from the
        // registry, so this cannot disagree with the registry-driven path about what runs first.
        parsers =
                OvertureParsers.build(new DefaultOvertureImportRegistry(), encodedValueLookup, config);
        return parsers;
    }

    /**
     * Applies the configured elevation and simplification settings to one edge's geometry.
     *
     * <p>The order matches {@code OSMReader.addEdge} and is not arbitrary: sampling adds the points that
     * smoothing then operates on, and simplification runs last so it can drop the points the earlier
     * two steps decided are redundant. Elevation work is skipped entirely on a 2D graph, where there is
     * nothing to sample or smooth.
     *
     * <p>The returned list is what the parsers see, so {@code average_slope} and {@code max_slope} are
     * computed from the same geometry the graph stores — again as in the OSM reader, which sets its
     * {@code point_list} tag after simplification.
     *
     * @param pointList the geometry converted from the source, modified in place where possible
     * @return the geometry to store and to hand to the parsers
     */
    private PointList processGeometry(PointList pointList) {
        if (pointList.is3D()) {
            // Sampling interpolates points and asks the provider for their elevation, so it needs one.
            if (config.getLongEdgeSamplingDistance() < Double.MAX_VALUE && elevationProvider != null) {
                pointList = EdgeSampling.sample(
                        pointList,
                        config.getLongEdgeSamplingDistance(),
                        DistanceCalcEarth.DIST_EARTH,
                        elevationProvider);
            }

            String smoothing = config.getElevationSmoothing();
            if (smoothing.equals("ramer")) {
                EdgeElevationSmoothingRamer.smooth(pointList, config.getElevationSmoothingRamerMax());
            } else if (smoothing.equals("moving_average")) {
                EdgeElevationSmoothingMovingAverage.smooth(
                        pointList, config.getSmoothElevationAverageWindowSize());
            } else if (!smoothing.isEmpty()) {
                throw new IllegalArgumentException(
                        "Unsupported elevation smoothing algorithm: '" + smoothing + "'");
            }
        }

        if (config.getMaxWayPointDistance() > 0 && pointList.size() > 2) {
            simplifyAlgo.simplify(pointList);
        }
        return pointList;
    }

    /**
     * Extracts intermediate points from the full geometry, excluding the first and last points.
     *
     * @param fullGeometry the complete point list including start and end nodes
     * @return a new point list containing only the intermediate geometry points,
     *         or {@link PointList#EMPTY} if there are no intermediate points
     */
    private PointList extractIntermediatePoints(PointList fullGeometry) {
        if (fullGeometry.size() <= 2) {
            return PointList.EMPTY;
        }
        // Must match the source list's dimension, otherwise setWayGeometry rejects it on a 3D graph.
        PointList intermediate = new PointList(fullGeometry.size() - 2, fullGeometry.is3D());
        for (int i = 1; i < fullGeometry.size() - 1; i++) {
            if (fullGeometry.is3D()) {
                intermediate.add(fullGeometry.getLat(i), fullGeometry.getLon(i), fullGeometry.getEle(i));
            } else {
                intermediate.add(fullGeometry.getLat(i), fullGeometry.getLon(i));
            }
        }
        return intermediate;
    }

    /**
     * Sets the elevation provider for 3D coordinates.
     *
     * @param provider the {@link ElevationProvider} to use.
     * @return this {@link DataReader} instance for chaining.
     */
    @Override
    public DataReader setElevationProvider(ElevationProvider provider) {
        this.elevationProvider = provider;
        return this;
    }

    /**
     * Sets the area index for spatial lookups during import.
     *
     * @param areaIndex the {@link AreaIndex} to use.
     * @return this {@link DataReader} instance for chaining.
     */
    @Override
    public DataReader setAreaIndex(AreaIndex<CustomArea> areaIndex) {
        this.areaIndex = areaIndex;
        return this;
    }


    /**
     * Returns the modification date of the data.
     *
     * @return the {@link Date} of the data, or null if unknown.
     */
    @Override
    public Date getDataDate() {
        return null;
    }

    /**
     * Dispatches the data parsing process based on the configured source (S3 or local file).
     * <p>
     * The method follows a priority-based selection:
     * <ol>
     * <li><b>S3 Source:</b> If the bucket, key, and S3Client are all provided, it attempts
     * to read from AWS S3 using {@link #parseFromS3()}.</li>
     * <li><b>Local File:</b> If S3 is not fully configured or missing the client, it checks
     * for a local file provided via {@link #setFile(File)}.</li>
     * </ol>
     * </p>
     *
     * @return the segments to import. Parquet sources return a lazily-read, closeable stream so peak
     *     memory does not scale with the size of the extract; {@code readGraph} closes it. GeoJSON still
     *     returns a fully materialised list, because its parser builds a whole-document tree anyway.
     * @throws IOException if no valid data source is configured, the local file is missing, or an error occurs during the parsing process.
     */
    protected Iterable<OvertureRoadSegment> parseData() throws IOException {
        if (s3Bucket != null && s3Key != null) {
            if (s3Client == null) {
                // Built here rather than when the reader was configured, so that merely assembling an
                // import never contacts AWS, and a missing region surfaces against the source that
                // actually needs one. A caller wanting a custom endpoint sets the client itself.
                logger.info("No S3 client set, creating one from the default AWS configuration");
                s3Client = S3Client.create();
            }
            logger.info("Reading from S3 source: s3://{}/{}", s3Bucket, s3Key);
            return parseFromS3();
        }

        if (overtureFile != null) {
            if (!overtureFile.exists()) {
                throw new IOException("Input file does not exist: " + overtureFile.getAbsolutePath());
            }
            logger.info("Reading from local file: {}", overtureFile.getAbsolutePath());
            return parseFromLocal();
        }

        throw new IOException("No data source configured. Set File or S3 parameters.");
    }

    /**
     * Parses Overture data from S3 using a size-based strategy.
     * <p>
     * Small files (<{@value #STREAMING_THRESHOLD_BYTES} bytes) are downloaded to a temp file
     * to reduce HTTP overhead. Large files are streamed via {@link S3ParquetInputFile}.
     * GeoJSON is not supported on S3 for performance reasons.
     * </p>
     *
     * @return the road segments, streamed for the large-file path
     * @throws IOException if format is unsupported or network fails
     */
    private Iterable<OvertureRoadSegment> parseFromS3() throws IOException {
        logger.info("Resolving S3 object: s3://{}/{}", s3Bucket, s3Key);
        FormatDetector.DataFormat format = FormatDetector.detectFromPath(s3Key);

        if (format == FormatDetector.DataFormat.UNKNOWN) {
            throw new IOException("Unsupported S3 file format: " + s3Key);
        }

        if (format == FormatDetector.DataFormat.GEOJSON) {
            throw new IOException(
                    "Reading GeoJSON directly from S3 is not supported due to performance constraints. "
                            + "Overture Maps data on S3 is expected to be in Parquet format.");
        }

        HeadObjectResponse headResponse = s3Client.headObject(
                HeadObjectRequest.builder().bucket(s3Bucket).key(s3Key).build());
        long size = headResponse.contentLength();

        if (size < STREAMING_THRESHOLD_BYTES) {
            logger.info(
                    "File is small ({} bytes < 20MB). Downloading to temp file for faster access...", size);
            File tempFile = downloadToTempFile();
            try {
                return OvertureParquetParser.parse(tempFile);
            } finally {
                if (tempFile.exists()) {
                    boolean deleted = tempFile.delete();
                    if (!deleted) logger.warn("Failed to delete temp file: {}", tempFile);
                }
            }
        } else {
            logger.info("File is large ({} bytes). Using S3 Streaming adapter...", size);
            S3ParquetInputFile s3InputFile = new S3ParquetInputFile(s3Client, s3Bucket, s3Key, size);
            // Streamed rather than materialised: this is the branch taken for continent-sized extracts,
            // where accumulating every segment first cannot fit in any heap.
            return OvertureParquetParser.stream(s3InputFile);
        }
    }

    /**
     * Downloads an S3 object to a temporary local file.
     * <p>
     * Used for small Parquet files to improve processing speed. The file is
     * automatically deleted in case of a download failure.
     * </p>
     *
     * @return the temporary file containing S3 data
     * @throws IOException if the download fails or file I/O errors occur
     */
    private File downloadToTempFile() throws IOException {
        File tempFile = File.createTempFile("overture-s3-download", ".parquet");

        try (ResponseInputStream<GetObjectResponse> s3Stream =
                s3Client.getObject(b -> b.bucket(s3Bucket).key(s3Key))) {
            Files.copy(s3Stream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            if (tempFile.exists() && !tempFile.delete()) {
                logger.warn("Failed to delete incomplete download: {}", tempFile.getAbsolutePath());
            }
            throw new IOException("Failed to download file from S3", e);
        }

        return tempFile;
    }

    /**
     * Parses a local file by detecting its format from the extension.
     * <p>
     * Supports Parquet (.parquet) via {@link OvertureParquetParser}
     * and GeoJSON (.geojson) via {@link OvertureParser}.
     * </p>
     *
     * @return list of parsed road segments
     * @throws IOException if the format is unsupported or file access fails
     */
    private Iterable<OvertureRoadSegment> parseFromLocal() throws IOException {
        FormatDetector.DataFormat format = FormatDetector.detectFromFile(overtureFile);

        return switch (format) {
                // Streamed: this is the path a large local extract takes.
            case PARQUET -> OvertureParquetParser.stream(overtureFile);
                // Not streamed, and not worth pretending otherwise: OvertureParser reads the whole GeoJSON
                // document into a Jackson tree before any segment is produced, so the tree - not the
                // segment
                // list - is what bounds the file size this path can handle.
            case GEOJSON -> OvertureParser.parse(overtureFile);
            default -> throw new IOException("Unsupported local file format: " + overtureFile.getName());
        };
    }

    /**
     * Returns the connector this sub-segment starts or ends at, if Overture names one there.
     *
     * <p>{@code SubSegmentProcessor.recalculateAt} rewrites each surviving connector's position into the
     * sub-segment's own 0..1 space and assigns <em>exactly</em> {@code 0.0} or {@code 1.0} when the
     * connector coincides with a boundary, so an exact comparison is the intended test rather than a
     * tolerance. An unsplit segment keeps its original positions, where the same rule holds.
     *
     * @param subsegment the sub-segment being imported
     * @param at {@code 0.0} for its start, {@code 1.0} for its end
     * @return the connector id, or {@code null} when this boundary came from a property range rather
     *     than a connector
     */
    @Nullable private static String connectorIdAt(OvertureRoadSegment subsegment, double at) {
        if (subsegment.getProperties() == null) return null;
        List<OvertureConnector> connectors = subsegment.getProperties().getConnectors();
        if (connectors == null) return null;

        for (OvertureConnector connector : connectors) {
            if (connector != null && connector.getAt() == at && connector.getConnectorId() != null) {
                return connector.getConnectorId();
            }
        }
        return null;
    }

    /**
     * Retrieves an existing node ID or creates a new one for a sub-segment end point.
     *
     * <p>Identity comes from the Overture connector when there is one, and from the rounded coordinate
     * otherwise. That ordering is the whole point: Overture states topology through connector ids, and
     * coordinates cannot reproduce it. Measured on a 124k-segment Florence extract, 54,456 of 133,449
     * junctions - 41% - were shared by two or more segments whose end points did not round to the same
     * 1e-7 degree key, so they became separate nodes and the road network fell apart there. Only 1.2% of
     * those gaps were under a centimetre; the rest ranged from decimetres to tens of metres, so no
     * snapping tolerance could fix it without also merging genuinely distinct junctions.
     *
     * <p>The coordinate map is still consulted and still populated, so this only ever merges more than
     * before, never less: a connector seen for the first time at a coordinate that already has a node
     * adopts that node rather than adding a second one.
     *
     * @param connectorId the Overture connector at this point, or {@code null} if none
     * @param lat The latitude of the node.
     * @param lon The longitude of the node.
     * @param na  The NodeAccess object used to store coordinates in the graph.
     * @return The unique identifier of the node within the graph.
     */
    private int getOrCreateNode(@Nullable String connectorId, double lat, double lon, NodeAccess na) {
        int latFixed = (int) Math.round(lat * 1e7);
        int lonFixed = (int) Math.round(lon * 1e7);

        // key contains lat in the first 32 bits and lon in the other 32 bits
        long key = ((long) latFixed << 32) | (lonFixed & 0xFFFFFFFFL);

        if (connectorId != null) {
            int byConnector = connectorNodeMap.get(connectorId);
            if (byConnector != ConnectorNodeMap.NOT_FOUND) return byConnector;
        }
        if (nodeMap.containsKey(key)) {
            int existing = nodeMap.get(key);
            // Bind the connector to the node that is already here, so a later sub-segment arriving at
            // the same connector from different geometry still lands on it.
            if (connectorId != null) connectorNodeMap.put(connectorId, existing);
            return existing;
        }

        int newNodeId = graph.getNodes();
        if (na.is3D()) {
            na.setNode(newNodeId, lat, lon, elevationAt(lat, lon));
        } else {
            na.setNode(newNodeId, lat, lon);
        }
        nodeMap.put(key, newNodeId);
        if (connectorId != null) connectorNodeMap.put(connectorId, newNodeId);

        return newNodeId;
    }

    /**
     * Samples the configured elevation provider.
     *
     * @param lat latitude of the point
     * @param lon longitude of the point
     * @return elevation in meters, or the configured {@code defaultElevation} when no provider is
     *     configured or it has no data for the point. A provider returning {@link Double#NaN} means
     *     "unknown"; storing NaN would poison every downstream slope and 3D distance computation, and
     *     substituting the configured default is what the OSM reader does with the same value.
     */
    private double elevationAt(double lat, double lon) {
        if (elevationProvider == null) return config.getDefaultElevation();
        double ele = elevationProvider.getEle(lat, lon);
        return Double.isNaN(ele) ? config.getDefaultElevation() : ele;
    }

    /**
     * Converts a JTS LineString geometry into a GraphHopper PointList.
     * Performs coordinate inversion to match GraphHopper's internal standard:
     * JTS (x=lon, y=lat) -> GraphHopper (lat, lon).
     *
     * <p>The result matches the graph's dimension. It used to be hard-coded to 2D, which made any
     * import with {@code graph.elevation.provider} configured fail in {@code BaseGraph} with
     * "Cannot use pointlist which is 2D for graph which is 3D".
     *
     * @param linestring The segment geometry in JTS format (Longitude/Latitude).
     * @return A list of points in GraphHopper format (Latitude/Longitude), 3D when the graph is 3D.
     */
    private PointList convertToPointList(LineString linestring) {
        boolean is3D = graph.getNodeAccess().is3D();
        PointList pointList = new PointList(linestring.getNumPoints(), is3D);
        for (int i = 0; i < linestring.getNumPoints(); i++) {
            Coordinate coord = linestring.getCoordinateN(i);
            double lat = coord.getY();
            double lon = coord.getX();
            if (is3D) {
                // Overture geometry carries no elevation, so sample it like the OSM reader does.
                pointList.add(lat, lon, elevationAt(lat, lon));
            } else {
                pointList.add(lat, lon);
            }
        }
        return pointList;
    }

    /**
     * Retrieves a {@link BooleanEncodedValue} by its key, recording the key as missing rather than
     * throwing so the caller can report every missing value at once.
     *
     * @param key the unique identifier for the boolean encoded value
     * @param missing collects keys that are not configured
     * @return the encoded value, or {@code null} if it is not configured
     */
    private BooleanEncodedValue requireBoolean(String key, List<String> missing) {
        if (!encodedValueLookup.hasEncodedValue(key)) {
            missing.add(key);
            return null;
        }
        return encodedValueLookup.getBooleanEncodedValue(key);
    }

    /**
     * Retrieves a {@link DecimalEncodedValue} by its key, recording the key as missing rather than
     * throwing so the caller can report every missing value at once.
     *
     * @param key the unique identifier for the decimal encoded value
     * @param missing collects keys that are not configured
     * @return the encoded value, or {@code null} if it is not configured
     */
    private DecimalEncodedValue requireDecimal(String key, List<String> missing) {
        if (!encodedValueLookup.hasEncodedValue(key)) {
            missing.add(key);
            return null;
        }
        return encodedValueLookup.getDecimalEncodedValue(key);
    }

    /**
     * Retrieves an {@link EnumEncodedValue} by its key, recording the key as missing rather than
     * throwing so the caller can report every missing value at once.
     *
     * @param <T> the Enum type
     * @param key the unique identifier for the enum encoded value
     * @param enumClass the class of the enumeration
     * @param missing collects keys that are not configured
     * @return the encoded value, or {@code null} if it is not configured
     */
    private <T extends Enum<T>> EnumEncodedValue<T> requireEnum(
            String key, Class<T> enumClass, List<String> missing) {
        if (!encodedValueLookup.hasEncodedValue(key)) {
            missing.add(key);
            return null;
        }
        return encodedValueLookup.getEnumEncodedValue(key, enumClass);
    }
}
