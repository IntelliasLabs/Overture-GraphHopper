package com.graphhopper.reader.overture.parser.parquet;

import com.graphhopper.reader.overture.access.restriction.OvertureAccessRestriction;
import com.graphhopper.reader.overture.common.speed.OvertureSpeedLimit;
import com.graphhopper.reader.overture.names.OvertureNames;
import com.graphhopper.reader.overture.parser.OvertureParserFilter;
import com.graphhopper.reader.overture.road.flags.OvertureRoadFlags;
import com.graphhopper.reader.overture.road.segment.OvertureConnector;
import com.graphhopper.reader.overture.road.segment.OvertureRoadClass;
import com.graphhopper.reader.overture.road.segment.OvertureRoadProperties;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSubclass;
import com.graphhopper.reader.overture.road.segment.OvertureRoute;
import com.graphhopper.reader.overture.road.segment.OvertureSegmentSubtype;
import com.graphhopper.reader.overture.road.segment.OvertureSource;
import com.graphhopper.reader.overture.road.segment.destination.OvertureDestination;
import com.graphhopper.reader.overture.road.segment.rule.OvertureLevelRule;
import com.graphhopper.reader.overture.road.segment.rule.OvertureProhibitedTransition;
import com.graphhopper.reader.overture.road.segment.rule.OvertureSubclassRule;
import com.graphhopper.reader.overture.road.segment.rule.OvertureWidthRule;
import com.graphhopper.reader.overture.road.surface.OvertureRoadSurface;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import org.apache.avro.generic.GenericRecord;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.io.InputFile;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The primary engine for parsing Overture Maps GeoParquet records into the GraphHopper model.
 * <p>
 * This class orchestrates the extraction and transformation of {@link GenericRecord} objects.
 * It handles WKB decoding, field validation, and applies lifecycle filtering via
 * {@link OvertureParserFilter}.
 * </p>
 * <p>
 * Use {@link #stream(File)} for anything but a small extract. {@link #parse(File)} collects every
 * segment into a list first, so its peak memory scales with the size of the source - this class used to
 * claim "a stable memory footprint" while doing exactly that, which is why a continent-sized extract
 * could not be imported at any heap size.
 * </p>
 * <h4>Usage Example:</h4>
 * <pre>{@code
 * try (var segments = OvertureParquetParser.stream(new File("overture_data.parquet"))) {
 *     for (OvertureRoadSegment segment : segments) { ... }
 * }
 * }</pre>
 *
 * @see OvertureParquetHelper
 * @see OvertureParserFilter
 */
public final class OvertureParquetParser {

    private static final Logger logger = LoggerFactory.getLogger(OvertureParquetParser.class);

    /**
     * Parses Overture Maps road segments from a generic Parquet input source.
     * <p>
     * This is the core parsing logic. By accepting an {@link InputFile}, this method
     * supporting both local files (via Hadoop adapters) and remote S3 streams (via custom adapters).
     * </p>
     *
     * @param inputFile the Parquet input source abstraction.
     * @return a list of parsed {@link OvertureRoadSegment} objects.
     * @throws IOException if the Parquet file cannot be read or parsed.
     */
    public static List<OvertureRoadSegment> parse(InputFile inputFile) throws IOException {
        List<OvertureRoadSegment> result = new ArrayList<>();
        try (SegmentStream stream = stream(inputFile)) {
            for (OvertureRoadSegment segment : stream) result.add(segment);
        }
        return result;
    }

    /**
     * Streams Overture road segments from a Parquet input source, one record at a time.
     *
     * <p>Prefer this over {@link #parse(InputFile)} for anything but a small extract. {@code parse}
     * accumulates every segment into a list first, so peak memory scales with the size of the source
     * rather than with the largest single segment: a 44 MB Florence extract needs roughly 900 MB, and a
     * continent-sized extract cannot be imported at any heap size. This method holds one record at a
     * time, which is what the class always claimed to do.
     *
     * <p>The returned stream owns the underlying Parquet reader and <b>must be closed</b>. Iterating it
     * more than once is not supported.
     *
     * @param inputFile the Parquet input source abstraction
     * @return a closeable, single-pass stream of segments
     * @throws IOException if the Parquet file cannot be opened
     */
    public static SegmentStream stream(InputFile inputFile) throws IOException {
        return new ParquetSegmentStream(AvroParquetReader.genericRecordReader(inputFile));
    }

    /**
     * Streams a local Parquet {@link File}. See {@link #stream(InputFile)}.
     *
     * @param file the local file on disk to parse
     * @return a closeable, single-pass stream of segments
     * @throws IOException if the file is missing or cannot be opened
     */
    public static SegmentStream stream(File file) throws IOException {
        return stream(toInputFile(file));
    }

    /**
     * A single-pass, closeable sequence of segments.
     *
     * <p>An interface rather than the concrete class so that callers - {@code OvertureReader} included -
     * depend only on "iterate once, then close", and so a test can supply a trivial implementation
     * without mocking a final type.
     */
    public interface SegmentStream extends Iterable<OvertureRoadSegment>, AutoCloseable {
        @Override
        void close() throws IOException;
    }

    /**
     * A {@link SegmentStream} over an open Parquet reader.
     *
     * <p>Records that {@link #tryMapRecord} rejects are skipped during iteration rather than surfacing
     * as nulls, so {@code hasNext} has to read ahead to know whether anything is left.
     */
    private static final class ParquetSegmentStream implements SegmentStream {

        private final ParquetReader<GenericRecord> reader;
        private final SkipTally tally = new SkipTally();
        private boolean iterated;

        private ParquetSegmentStream(ParquetReader<GenericRecord> reader) {
            this.reader = reader;
        }

        @Override
        public Iterator<OvertureRoadSegment> iterator() {
            if (iterated) {
                throw new IllegalStateException(
                        "A Parquet SegmentStream can only be iterated once; it is backed by a forward-only"
                                + " reader.");
            }
            iterated = true;

            return new Iterator<>() {
                private OvertureRoadSegment next = advance();

                @Override
                public boolean hasNext() {
                    return next != null;
                }

                @Override
                public OvertureRoadSegment next() {
                    if (next == null) throw new NoSuchElementException();
                    OvertureRoadSegment current = next;
                    next = advance();
                    return current;
                }

                /** @return the next mappable segment, or {@code null} at end of file */
                private OvertureRoadSegment advance() {
                    try {
                        GenericRecord record;
                        while ((record = reader.read()) != null) {
                            OvertureRoadSegment segment = tryMapRecord(record, tally);
                            if (segment != null) return segment;
                        }
                        return null;
                    } catch (Exception e) {
                        // Iterator cannot throw a checked exception, and a truncated import must not look
                        // like a complete one, so this has to surface as a failure rather than an end.
                        throw new UncheckedIOException(
                                new IOException("Failed to parse Parquet from InputFile", e));
                    }
                }
            };
        }

        @Override
        public void close() throws IOException {
            tally.report();
            reader.close();
        }
    }

    /**
     * Counts the records an import discards, grouped by why.
     *
     * <p>Replaces a per-record {@code WARN}. A continent-sized extract discards a few hundred thousand
     * records - rail segments, mostly - and logging each one produced hundreds of megabytes of
     * identical lines that buried everything else and, on one run, filled the disk. Worse, the
     * per-record message reported the <em>parsed</em> class, which is {@code null} precisely because
     * the value was unrecognised, so the spam did not even say which classes were dropped.
     *
     * <p>One summary at the end of the import says strictly more: how many, and of what.
     */
    static final class SkipTally {

        private final Map<String, Integer> byReason = new TreeMap<>();
        private int total;

        /**
         * @param reason why the record was discarded, as a low-cardinality label
         */
        void skipped(String reason) {
            byReason.merge(reason, 1, Integer::sum);
            total++;
        }

        /** @return how many records were discarded in total */
        int total() {
            return total;
        }

        /** @return the counts keyed by reason */
        Map<String, Integer> byReason() {
            return byReason;
        }

        /** Logs the summary, or nothing at all when the extract was fully consumed. */
        void report() {
            if (total == 0) return;
            logger.warn("Skipped {} segment(s) that cannot be routed on: {}", total, byReason);
        }
    }

    /**
     * Convenience method to parse a local Parquet {@link File}.
     * <p>
     * This wrapper validates the file existence, converts it to a Hadoop {@link InputFile},
     * and delegates to the main {@link #parse(InputFile)} method.
     * </p>
     *
     * @param file the local file on disk to parse.
     * @return a list of parsed {@link OvertureRoadSegment} objects.
     * @throws IllegalArgumentException if the provided file is {@code null}.
     * @throws FileNotFoundException    if the file does not exist.
     * @throws IOException              if an error occurs during parsing.
     */
    public static List<OvertureRoadSegment> parse(File file) throws IOException {
        return parse(toInputFile(file));
    }

    /**
     * @param file the local file on disk
     * @return a Hadoop-backed {@link InputFile} for it
     * @throws IllegalArgumentException if {@code file} is {@code null}
     * @throws FileNotFoundException if it does not exist
     */
    private static InputFile toInputFile(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("Parquet file cannot be null");
        }
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
        }
        return new LocalParquetInputFile(file);
    }

    /**
     * Maps a single {@link GenericRecord} from a Parquet file to an {@link OvertureRoadSegment}.
     * <p>
     * This method performs several critical validation and filtering steps:
     * <ul>
     * <li>Checks for the presence of mandatory fields (ID, Class, Geometry).</li>
     * <li>Decodes binary WKB geometry and ensures it is a {@link LineString}.</li>
     * <li>Filters records by {@link OvertureRoadClass}, the unknown record is skipped.</li>
     * </ul>
     * </p>
     *
     * @param record the raw Avro record containing road segment data.
     * @return a populated {@link OvertureRoadSegment} object, or {@code null} if the record
     * is invalid, missing required data, or filtered out due to an unsupported road class.
     */
    static OvertureRoadSegment tryMapRecord(GenericRecord record) {
        return tryMapRecord(record, new SkipTally());
    }

    /**
     * Maps one record, counting it in {@code tally} when it cannot be used.
     *
     * @param record the raw Avro record containing road segment data
     * @param tally collects why records were discarded, so the import logs one summary rather than one
     *     line per record
     * @return a populated {@link OvertureRoadSegment}, or {@code null} if the record is invalid,
     *     missing required data, or filtered out due to an unsupported road class
     */
    static OvertureRoadSegment tryMapRecord(GenericRecord record, SkipTally tally) {
        Object rawId = record.get(OvertureSchema.ID);
        Object rawClass = record.get(OvertureSchema.CLASS);
        Object rawGeom = record.get(OvertureSchema.GEOMETRY);
        Object rawSubtype = record.get(OvertureSchema.SUBTYPE);

        // Missing subtype means road, matching OvertureExtractor. This column was previously not read
        // at all, so every segment reached the parsers claiming to be a road - including ferries.
        OvertureSegmentSubtype subtype =
                rawSubtype == null ? null : OvertureSegmentSubtype.fromString(rawSubtype.toString());
        if (subtype == null) subtype = OvertureSegmentSubtype.ROAD;

        // A water segment carries no class - Overture describes ferries through the subtype alone - so
        // requiring one here dropped every ferry in the extract before any parser saw it.
        boolean water = subtype == OvertureSegmentSubtype.WATER;
        if (rawId == null || rawGeom == null || (rawClass == null && !water)) {
            tally.skipped("missing a mandatory field");
            logger.debug("Skipping segment with missing mandatory fields: {}", record);
            return null;
        }
        String idStr = rawId.toString();
        if (idStr.isEmpty()) {
            tally.skipped("empty id");
            logger.debug("Skipping segment with empty ID: {}", record);
            return null;
        }

        // Railways are not routable by any profile this reader supports, and they carry no access
        // restrictions, so an imported one becomes a road that is open to cars, cyclists and
        // pedestrians alike. Most were already dropped, but only by accident: OvertureRoadClass has no
        // constant for standard_gauge, tram and the rest, so they failed the class filter. Rail tagged
        // class=unknown does map to a constant, passes the filter, and was reaching the graph - 84,439
        // segments of it in a Europe extract. Rejecting on the subtype makes the intent explicit and
        // covers every rail class, and doing it here also skips decoding their geometry.
        if (subtype == OvertureSegmentSubtype.RAIL) {
            // Keyed by class so the summary still distinguishes tram from heavy rail, as it did when
            // these were dropped by the class filter.
            tally.skipped(rawClass == null ? "rail" : "rail '" + rawClass + "'");
            logger.debug("Skipping rail segment with id: {}", idStr);
            return null;
        }

        try {
            Geometry geometry = OvertureParquetHelper.parseGeometry(rawGeom);
            if (!(geometry instanceof LineString lineString)) {
                tally.skipped("geometry is not a line");
                return null;
            }

            if (water) return waterSegment(record, idStr, lineString);

            OvertureRoadClass roadClass = OvertureRoadClass.fromString(rawClass.toString());
            if (!OvertureParserFilter.INSTANCE.getRoadClassFilter().isAllowed(roadClass)) {
                // The raw value, not the parsed one: roadClass is null exactly when the value was not
                // recognised, so reporting it named nothing. Rail classes land here, as intended.
                tally.skipped("unsupported class '" + rawClass + "'");
                logger.debug("Skipping segment with id: {} due to unsupported road class", idStr);
                return null;
            }

            Object rawSubclass = record.get(OvertureSchema.SUBCLASS);
            Object rawNames = record.get(OvertureSchema.NAMES);
            Object rawSpeedLimits = record.get(OvertureSchema.SPEED_LIMITS);
            Object rawAccessRestrictions = record.get(OvertureSchema.ACCESS_RESTRICTIONS);
            Object rawOvertureRoadSurface = record.get(OvertureSchema.ROAD_SURFACE);
            Object rawOvertureRoadFlags = record.get(OvertureSchema.ROAD_FLAGS);
            Object rawConnectors = record.get(OvertureSchema.CONNECTORS);

            OvertureRoadSubclass roadSubclass = AvroInternalUtils.safeParseOptionalEnum(
                    rawSubclass, OvertureRoadSubclass::fromString, idStr, OvertureSchema.SUBCLASS);
            if (!OvertureParserFilter.INSTANCE.getRoadSubclassFilter().isAllowed(roadSubclass))
                roadSubclass = null;

            OvertureNames names = OvertureParquetHelper.parseNames(rawNames, idStr);

            List<OvertureSpeedLimit> overtureSpeedLimit =
                    OvertureParquetHelper.parseSpeedLimits(rawSpeedLimits, idStr);

            List<OvertureAccessRestriction> restrictions =
                    OvertureParquetHelper.parseRestriction(rawAccessRestrictions, idStr);

            List<OvertureRoadSurface> surfaces =
                    OvertureParquetHelper.parseRoadSurface(rawOvertureRoadSurface, idStr);

            List<OvertureRoadFlags> flags =
                    OvertureParquetHelper.parseRoadFlags(rawOvertureRoadFlags, idStr);

            List<OvertureConnector> connectors =
                    OvertureParquetHelper.parseConnectors(rawConnectors, idStr);

            // Every column below was previously dropped, passing emptyList() while the GeoJSON path
            // extracted it. Three of them are linearly referenced, so the loss also changed where
            // SplitPointCollector split a segment; see OvertureCrossFormatEquivalenceTest.
            List<OvertureRoute> routes =
                    OvertureParquetHelper.parseRoutes(record.get(OvertureSchema.ROUTES), idStr);
            List<OvertureDestination> destinations =
                    OvertureParquetHelper.parseDestinations(record.get(OvertureSchema.DESTINATIONS), idStr);
            List<OvertureProhibitedTransition> prohibitedTransitions =
                    OvertureParquetHelper.parseProhibitedTransitions(
                            record.get(OvertureSchema.PROHIBITED_TRANSITIONS), idStr);
            List<OvertureWidthRule> widthRules =
                    OvertureParquetHelper.parseWidthRules(record.get(OvertureSchema.WIDTH_RULES), idStr);
            List<OvertureSubclassRule> subclassRules = OvertureParquetHelper.parseSubclassRules(
                    record.get(OvertureSchema.SUBCLASS_RULES), idStr);
            List<OvertureLevelRule> levelRules =
                    OvertureParquetHelper.parseLevelRules(record.get(OvertureSchema.LEVEL_RULES), idStr);
            List<OvertureSource> sources =
                    OvertureParquetHelper.parseSources(record.get(OvertureSchema.SOURCES), idStr);

            Object rawVersion = record.get(OvertureSchema.VERSION);
            int version = rawVersion instanceof Number number ? number.intValue() : 0;

            return new OvertureRoadSegment(
                    idStr,
                    lineString,
                    new OvertureRoadProperties(
                            connectors,
                            routes,
                            roadClass,
                            destinations,
                            prohibitedTransitions,
                            surfaces,
                            flags,
                            overtureSpeedLimit,
                            widthRules,
                            roadSubclass,
                            subclassRules,
                            restrictions,
                            // level and theme/type stay at their defaults deliberately: GeoParquet has no
                            // such columns. theme and type identify the dataset partition rather than the
                            // record, and level was superseded by level_rules.
                            0,
                            levelRules,
                            null,
                            null,
                            version,
                            sources,
                            names,
                            subtype));
        } catch (IllegalArgumentException e) {
            tally.skipped("could not be parsed");
            logger.debug("Skipping segment with id: {}", idStr, e);
            return null;
        }
    }

    /**
     * Builds a ferry segment.
     *
     * <p>Mirrors the water branch of {@code OvertureExtractor} so the two ingest paths agree: the road
     * attributes are absent rather than defaulted, and the subtype is what marks the segment as a
     * ferry. {@code roadClass} stays {@code null} deliberately - GraphHopper has no {@code
     * RoadClass.FERRY}, and {@code OvertureRoadEnvironmentParser} derives {@link
     * com.graphhopper.routing.ev.RoadEnvironment#FERRY} from the subtype instead.
     *
     * <p>Only connectors, routes, access restrictions, level rules, sources and names can appear on a
     * water segment; surfaces, flags, speed limits and the rest are road-only columns.
     *
     * @param record the raw Avro record
     * @param idStr the segment id
     * @param lineString the decoded geometry
     * @return the ferry segment
     */
    private static OvertureRoadSegment waterSegment(
            GenericRecord record, String idStr, LineString lineString) {
        Object rawVersion = record.get(OvertureSchema.VERSION);
        return new OvertureRoadSegment(
                idStr,
                lineString,
                new OvertureRoadProperties(
                        OvertureParquetHelper.parseConnectors(record.get(OvertureSchema.CONNECTORS), idStr),
                        OvertureParquetHelper.parseRoutes(record.get(OvertureSchema.ROUTES), idStr),
                        null, // roadClass: water carries none
                        List.of(), // destinations
                        List.of(), // prohibitedTransitions
                        List.of(), // surfaces
                        List.of(), // flags
                        List.of(), // speedLimits
                        List.of(), // widthRules
                        null, // subclass
                        List.of(), // subclassRules
                        OvertureParquetHelper.parseRestriction(
                                record.get(OvertureSchema.ACCESS_RESTRICTIONS), idStr),
                        0, // level: superseded by level_rules, as on the road path
                        OvertureParquetHelper.parseLevelRules(record.get(OvertureSchema.LEVEL_RULES), idStr),
                        null, // theme: no such GeoParquet column
                        null, // type: no such GeoParquet column
                        rawVersion instanceof Number number ? number.intValue() : 0,
                        OvertureParquetHelper.parseSources(record.get(OvertureSchema.SOURCES), idStr),
                        OvertureParquetHelper.parseNames(record.get(OvertureSchema.NAMES), idStr),
                        OvertureSegmentSubtype.WATER));
    }
}
