package com.graphhopper.reader.overture;

import com.carrotsearch.hppc.LongIntScatterMap;
import com.graphhopper.reader.DataReader;
import com.graphhopper.reader.dem.ElevationProvider;
import com.graphhopper.reader.overture.aws.S3ParquetInputFile;
import com.graphhopper.reader.overture.parser.OvertureParser;
import com.graphhopper.reader.overture.parser.parquet.OvertureParquetParser;
import com.graphhopper.reader.overture.parsers.*;
import com.graphhopper.reader.overture.road.flags.OvertureRoadFlags;
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
     * Maps Overture string IDs (e.g., GID) to GraphHopper's internal integer node IDs.
     */
    private final LongIntScatterMap nodeMap;

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
     * Constructs a new OvertureReader.
     *
     * @param graph the {@link BaseGraph} to store the imported data.
     */
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
     * @return this {@link DataReader} instance.
     * @throws IllegalArgumentException if the URL does not start with "s3://" or is missing the bucket/key.
     */
    public DataReader setS3Source(String s3Url) {
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
     * @return this {@link DataReader} instance.
     */
    public DataReader setS3Source(String bucket, String key) {
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
     * Sets the input file for the reader.
     *
     * @param file the input file.
     * @return this {@link DataReader} instance for chaining.
     */
    @Override
    public DataReader setFile(File file) {
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
        List<OvertureRoadSegment> segments = parseData();

        if (segments.isEmpty()) {
            logger.info("No segments found to processed");
            return;
        }
        logger.info("Loaded {} segments. Starting graph creation...", segments.size());

        NodeAccess nodeAccess = graph.getNodeAccess();
        int edgeCount = 0;
        int skippedCount = 0;

        if (encodedValueLookup == null) {
            throw new IllegalStateException(
                    "EncodedValueLookup is not set. Call setEncodedValueLookup() and ensure car_access and car_average_speed are configured.");
        }

        BooleanEncodedValue bikeAccessEnc = fillBooleanEncodedValue(VehicleAccess.key("bike"));
        DecimalEncodedValue bikeSpeedEnc = fillDecimalEncodedValues(VehicleSpeed.key("bike"));
        BooleanEncodedValue carAccessEnc = fillBooleanEncodedValue(VehicleAccess.key("car"));
        DecimalEncodedValue carSpeedEnc = fillDecimalEncodedValues(VehicleSpeed.key("car"));
        BooleanEncodedValue footAccessEnc = fillBooleanEncodedValue(VehicleAccess.key("foot"));
        DecimalEncodedValue footSpeedEnc = fillDecimalEncodedValues(VehicleSpeed.key("foot"));
        EnumEncodedValue<Hazmat> hazmatEnc = fillEnumEncodedValues("hazmat", Hazmat.class);
        BooleanEncodedValue roadClassLinkEnc = fillBooleanEncodedValue("road_class_link");
        EnumEncodedValue<RoadClass> roadClassEnc = fillEnumEncodedValues("road_class", RoadClass.class);
        EnumEncodedValue<RoadEnvironment> roadEnvironmentEnc =
                fillEnumEncodedValues("road_environment", RoadEnvironment.class);
        EnumEncodedValue<Surface> surfaceEnc = fillEnumEncodedValues("surface", Surface.class);
        EnumEncodedValue<Smoothness> smoothnessEnc =
                fillEnumEncodedValues("smoothness", Smoothness.class);
        EnumEncodedValue<TrackType> trackTypeEnc = fillEnumEncodedValues("track_type", TrackType.class);

        for (OvertureRoadSegment segment : segments) {
            List<OvertureRoadSegment> subsegments = SegmentSplitter.split(segment);
            for (OvertureRoadSegment subsegment : subsegments) {

                ///  Skip if segment abandoned or under construction
                boolean needsSkipping =
                        subsegment.getProperties().getFlags().stream().anyMatch(OvertureRoadFlags::shouldSkip);
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
                int fromId = getOrCreateNode(startCoord.y, startCoord.x, nodeAccess);
                int toId = getOrCreateNode(endCoord.y, endCoord.x, nodeAccess);
                if (fromId == toId) continue;

                PointList fullGeometry = convertToPointList(lineString);
                PointList intermediatePoints = extractIntermediatePoints(fullGeometry);

                double distance = subsegment.calculateDistance();

                EdgeIteratorState edge =
                        graph.edge(fromId, toId).setDistance(distance).setWayGeometry(intermediatePoints);

                OvertureBikeAccessParser.parseAccess(edge, subsegment, bikeAccessEnc);
                OvertureBikeAverageSpeedParser.parseSpeed(edge, subsegment, bikeSpeedEnc);
                OvertureCarAccessParser.parseAccess(edge, subsegment, carAccessEnc);
                OvertureCarAverageSpeedParser.parseSpeed(edge, subsegment, carSpeedEnc);
                OvertureFootAccessParser.parseAccess(edge, subsegment, footAccessEnc);
                OvertureFootAverageSpeedParser.parseSpeed(edge, subsegment, footSpeedEnc);
                OvertureHazmatParser.parseHazmat(edge, subsegment, hazmatEnc);
                OvertureRoadClassLinkParser.parseLink(edge, subsegment, roadClassLinkEnc);
                OvertureRoadClassParser.parseRoadClass(edge, subsegment, roadClassEnc);
                OvertureRoadEnvironmentParser.parseRoadEnvironment(edge, subsegment, roadEnvironmentEnc);
                OvertureRoadSurfaceParser.parseSurface(edge, subsegment, surfaceEnc);
                OvertureSmoothnessParser.parseSmoothness(edge, subsegment, smoothnessEnc);
                OvertureTrackTypeParser.parseTrackType(edge, subsegment, trackTypeEnc);
                OvertureNameParser.parseName(edge, subsegment);

                edgeCount++;
            }
            if (edgeCount % 50000 == 0) {
                logger.info("Progress: {} edges created...", edgeCount);
            }
        }
        logger.info("Finished readGraph. Created: {}, Skipped: {}", edgeCount, skippedCount);
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
        PointList intermediate = new PointList(fullGeometry.size() - 2, false);
        for (int i = 1; i < fullGeometry.size() - 1; i++) {
            intermediate.add(fullGeometry.getLat(i), fullGeometry.getLon(i));
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
     * @return a list of parsed {@link OvertureRoadSegment} objects.
     * @throws IOException if no valid data source is configured, the local file is missing, or an error occurs during the parsing process.
     */
    protected List<OvertureRoadSegment> parseData() throws IOException {
        if (s3Bucket != null && s3Key != null && s3Client != null) {
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
     * @return list of road segments
     * @throws IOException if format is unsupported or network fails
     */
    private List<OvertureRoadSegment> parseFromS3() throws IOException {
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
            return OvertureParquetParser.parse(s3InputFile);
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
    private List<OvertureRoadSegment> parseFromLocal() throws IOException {
        FormatDetector.DataFormat format = FormatDetector.detectFromFile(overtureFile);

        return switch (format) {
            case PARQUET -> OvertureParquetParser.parse(overtureFile);
            case GEOJSON -> OvertureParser.parse(overtureFile);
            default -> throw new IOException("Unsupported local file format: " + overtureFile.getName());
        };
    }

    /**
     * Retrieves an existing node ID or creates a new one for the specified coordinates.
     * Uses a LongIntScatterMap to ensure road connectivity by deduplicating nodes at intersections.
     *
     * @param lat The latitude of the node.
     * @param lon The longitude of the node.
     * @param na  The NodeAccess object used to store coordinates in the graph.
     * @return The unique identifier of the node within the graph.
     */
    private int getOrCreateNode(double lat, double lon, NodeAccess na) {
        int latFixed = (int) Math.round(lat * 1e7);
        int lonFixed = (int) Math.round(lon * 1e7);

        // key contains lat in the first 32 bits and lon in the other 32 bits
        long key = ((long) latFixed << 32) | (lonFixed & 0xFFFFFFFFL);

        if (nodeMap.containsKey(key)) {
            return nodeMap.get(key);
        }

        int newNodeId = graph.getNodes();
        na.setNode(newNodeId, lat, lon);
        nodeMap.put(key, newNodeId);

        return newNodeId;
    }

    /**
     * Converts a JTS LineString geometry into a GraphHopper PointList.
     * Performs coordinate inversion to match GraphHopper's internal standard:
     * JTS (x=lon, y=lat) -> GraphHopper (lat, lon).
     *
     * @param linestring The segment geometry in JTS format (Longitude/Latitude).
     * @return A list of points in GraphHopper format (Latitude/Longitude).
     */
    private PointList convertToPointList(LineString linestring) {
        PointList pointList = new PointList(linestring.getNumPoints(), false);
        for (int i = 0; i < linestring.getNumPoints(); i++) {
            Coordinate coord = linestring.getCoordinateN(i);
            pointList.add(coord.getY(), coord.getX());
        }
        return pointList;
    }

    /**
     * Retrieves a {@link BooleanEncodedValue} by its key.
     * Logs a warning and returns {@code null} if the key is not found.
     *
     * @param key the unique identifier for the boolean encoded value
     * @return the retrieved {@link BooleanEncodedValue}, or {@code null} if unavailable
     */
    private BooleanEncodedValue fillBooleanEncodedValue(String key) {
        BooleanEncodedValue booleanEncodedValue = null;
        try {
            booleanEncodedValue = encodedValueLookup.getBooleanEncodedValue(key);
        } catch (Exception e) {
            logger.warn(key + " not available: " + e.getMessage());
        }
        return booleanEncodedValue;
    }

    /**
     * Retrieves a {@link DecimalEncodedValue} by its key.
     * Logs a warning and returns {@code null} if the key is not found.
     *
     * @param key the unique identifier for the boolean encoded value
     * @return the retrieved {@link DecimalEncodedValue}, or {@code null} if unavailable
     */
    private DecimalEncodedValue fillDecimalEncodedValues(String key) {
        DecimalEncodedValue decimalEncodedValue = null;
        try {
            decimalEncodedValue = encodedValueLookup.getDecimalEncodedValue(key);
        } catch (Exception e) {
            logger.warn(key + " not available: " + e.getMessage());
        }
        return decimalEncodedValue;
    }

    /**
     * Retrieves a {@link EnumEncodedValue} by its key.
     * Logs a warning and returns {@code null} if the key is not found.
     *
     * @param <T>       the Enum type
     * @param key       the unique identifier for the enum encoded value
     * @param enumClass the class of the enumeration
     * @return the retrieved {@link EnumEncodedValue}, or {@code null} if unavailable
     */
    private <T extends Enum<T>> EnumEncodedValue<T> fillEnumEncodedValues(
            String key, Class<T> enumClass) {
        EnumEncodedValue<T> enumEncodedValue = null;
        try {
            enumEncodedValue = encodedValueLookup.getEnumEncodedValue(key, enumClass);
        } catch (Exception e) {
            logger.warn(key + " not available: " + e.getMessage());
        }
        return enumEncodedValue;
    }
}
