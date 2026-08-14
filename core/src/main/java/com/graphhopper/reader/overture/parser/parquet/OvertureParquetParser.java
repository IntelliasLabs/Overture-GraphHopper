package com.graphhopper.reader.overture.parser.parquet;

import static java.util.Collections.emptyList;

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
import com.graphhopper.reader.overture.road.surface.OvertureRoadSurface;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
 * It is designed for high-throughput processing of large datasets by streaming records,
 * maintaining a stable memory footprint. It handles WKB decoding, field validation,
 * and applies lifecycle filtering via {@link OvertureParserFilter}.
 * </p>
 * <h4>Usage Example:</h4>
 * <pre>{@code
 * List<OvertureRoadSegment> segments = OvertureParquetParser.parse(new File("overture_data.parquet"));
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

        try (ParquetReader<GenericRecord> reader = AvroParquetReader.genericRecordReader(inputFile)) {

            GenericRecord record;
            while ((record = reader.read()) != null) {
                OvertureRoadSegment segment = tryMapRecord(record);
                if (segment != null) {
                    result.add(segment);
                }
            }
        } catch (Exception e) {
            throw new IOException("Failed to parse Parquet from InputFile", e);
        }
        return result;
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
        if (file == null) {
            throw new IllegalArgumentException("Parquet file cannot be null");
        }
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
        }
        return parse(new LocalParquetInputFile(file));
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
        Object rawId = record.get(OvertureSchema.ID);
        Object rawClass = record.get(OvertureSchema.CLASS);
        Object rawGeom = record.get(OvertureSchema.GEOMETRY);

        if (rawId == null || rawClass == null || rawGeom == null) {
            logger.warn("Skipping segment with missing mandatory fields: {}", record);
            return null;
        }
        String idStr = rawId.toString();
        if (idStr.isEmpty()) {
            logger.warn("Skipping segment with empty ID: {}", record);
            return null;
        }

        try {
            Geometry geometry = OvertureParquetHelper.parseGeometry(rawGeom);
            if (!(geometry instanceof LineString lineString)) return null;

            OvertureRoadClass roadClass = OvertureRoadClass.fromString(rawClass.toString());
            if (!OvertureParserFilter.INSTANCE.getRoadClassFilter().isAllowed(roadClass)) {
                logger.warn(
                        "Skipping segment with id: {} due to unsupported road class: {}", idStr, roadClass);
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

            return new OvertureRoadSegment(
                    idStr,
                    lineString,
                    new OvertureRoadProperties(
                            connectors,
                            emptyList(),
                            roadClass,
                            emptyList(),
                            emptyList(),
                            surfaces,
                            flags,
                            overtureSpeedLimit,
                            emptyList(),
                            roadSubclass,
                            emptyList(),
                            restrictions,
                            0,
                            emptyList(),
                            null,
                            null,
                            0,
                            emptyList(),
                            names));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
