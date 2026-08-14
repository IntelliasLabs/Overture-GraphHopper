package com.graphhopper.reader.overture.parser.parquet;

import static com.graphhopper.reader.overture.parser.parquet.AvroInternalUtils.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import com.graphhopper.reader.overture.access.restriction.AccessType;
import com.graphhopper.reader.overture.access.restriction.OvertureAccessRestriction;
import com.graphhopper.reader.overture.access.restriction.PropertyScopeContainer;
import com.graphhopper.reader.overture.access.restriction.scope.containers.*;
import com.graphhopper.reader.overture.common.speed.OvertureSpeed;
import com.graphhopper.reader.overture.common.speed.OvertureSpeedLimit;
import com.graphhopper.reader.overture.common.speed.SpeedUnit;
import com.graphhopper.reader.overture.names.*;
import com.graphhopper.reader.overture.parser.OvertureParserFilter;
import com.graphhopper.reader.overture.road.flags.OvertureRoadFlags;
import com.graphhopper.reader.overture.road.segment.OvertureConnector;
import com.graphhopper.reader.overture.road.surface.OvertureRoadSurface;
import com.graphhopper.reader.overture.road.surface.RoadSurfaceType;
import java.nio.ByteBuffer;
import java.util.*;
import org.apache.avro.generic.GenericRecord;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class providing helper methods for parsing Overture Maps data from Parquet files.
 * <p>
 * This class encapsulates technical details related to Avro-to-Java type conversions and
 * spatial data decoding specifically for the GeoParquet format used by Overture.
 * </p>
 */
public class OvertureParquetHelper {

    private static final Logger logger = LoggerFactory.getLogger(OvertureParquetHelper.class);

    // =============================================================================
    // GEOMETRY
    // =============================================================================

    /**
     * Extracts and decodes geometry from a single Parquet record.
     * <p>
     * This implementation focuses on road segments and delegates decoding to
     * {@link WKBGeometryDecoder}. If the geometry is missing, empty, or
     * of an unsupported type (e.g., Point), the method logs an error and
     * returns {@code null} to allow the import process to continue.
     * </p>
     *
     * @return a JTS {@link Geometry} (typically a {@link LineString}),
     * or {@code null} if parsing fails or data is missing.
     */
    public static Geometry parseGeometry(Object rawGeom) {
        if (rawGeom == null) return null;
        try {
            byte[] wkb = extractBytes(rawGeom);
            /// When WKBGeometryDecoder supports other types, switch to a generic decode(wkb) method here.
            Geometry geometry = WKBGeometryDecoder.decodeLineString(wkb);

            if (geometry == null || geometry.isEmpty()) {
                logger.warn("Decoded geometry is empty for record");
                return null;
            }
            return geometry;
        } catch (IllegalArgumentException e) {
            logger.error("Failed to decode WKB geometry: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Converts the raw geometry object from the Parquet record into a byte array.
     * <p>
     * Avro typically returns binary data as a {@link ByteBuffer}. This method
     * handles the buffer's position and remaining bytes correctly to ensure
     * the full WKB payload is extracted.
     * </p>
     *
     * @param rawGeometry the object extracted from the record's geometry column.
     * @return a {@code byte[]} containing the Well-Known Binary data.
     * @throws IllegalArgumentException if the input type is not {@link ByteBuffer} or {@code byte[]}.
     */
    private static byte[] extractBytes(Object rawGeometry) {
        if (rawGeometry instanceof ByteBuffer buffer) {
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            return bytes;
        } else if (rawGeometry instanceof byte[] bytes) {
            return bytes;
        }
        throw new IllegalArgumentException(
                "Unsupported binary type: " + rawGeometry.getClass().getName());
    }

    // =============================================================================
    // SPEED LIMITS
    // =============================================================================

    /**
     * Parses {@code speed_limits} rules from Parquet record properties.
     * <p>
     * Maps max/min speeds, variable flags, temporal scopes, and linear ranges.
     * Skips rules where both max and min speeds are missing or invalid.
     * </p>
     *
     * @param rawSpeedLimits Raw object from the 'speed_limits' column.
     * @param segmentId      Contextual segment ID for logging.
     * @return List of parsed limits or {@code null} if empty/invalid.
     */
    public static List<OvertureSpeedLimit> parseSpeedLimits(Object rawSpeedLimits, String segmentId) {
        return parseList(rawSpeedLimits, OvertureSchema.SPEED_LIMITS, segmentId, item -> {
            if (!(item instanceof GenericRecord realRecord)) {
                logger.debug("Skipping speed limits for segment {}: invalid record type", segmentId);
                return null;
            }

            OvertureSpeed maxSpeed = parseSpeed(
                    (GenericRecord) getValOrNull(realRecord, OvertureSchema.Speed.MAX_SPEED), segmentId);
            OvertureSpeed minSpeed = parseSpeed(
                    (GenericRecord) getValOrNull(realRecord, OvertureSchema.Speed.MIN_SPEED), segmentId);

            if (maxSpeed == null && minSpeed == null) {
                logger.debug("Skipping speed limits for segment {}: no speed limits defined", segmentId);
                return null;
            }

            boolean isMaxVar =
                    Boolean.TRUE.equals(getValOrNull(realRecord, OvertureSchema.Speed.IS_VARIABLE));
            LinearlyReferencedRange between =
                    parseRange(getValOrNull(realRecord, OvertureSchema.Scope.BETWEEN));
            PropertyScopeContainer when =
                    parseScope(getValOrNull(realRecord, OvertureSchema.Scope.WHEN), segmentId);

            return new OvertureSpeedLimit(maxSpeed, minSpeed, isMaxVar, between, when);
        });
    }

    /**
     * Helper to parse the speed struct (Value and Unit).
     */
    static OvertureSpeed parseSpeed(GenericRecord speedRecord, String segmentId) {
        if (speedRecord == null) return null;

        Double value = extractDouble(getValOrNull(speedRecord, OvertureSchema.Speed.VALUE));
        String unitStr = extractString(speedRecord, OvertureSchema.Speed.UNIT);
        SpeedUnit speedUnit = SpeedUnit.fromString(unitStr);
        if (!OvertureParserFilter.INSTANCE.getSpeedUnitFilter().isAllowed(speedUnit)) {
            logger.debug("Skipping speed for segment {}: invalid speed unit '{}'", segmentId, unitStr);
            return null;
        }

        OvertureSpeed speed = new OvertureSpeed(value, speedUnit);
        return Optional.of(speed).filter(OvertureSpeed::isValid).orElseGet(() -> {
            logger.debug("Skipping speed for segment {}: invalid speed value '{}'", segmentId, value);
            return null;
        });
    }

    // =============================================================================
    // ACCESS RESTRICTIONS
    // =============================================================================

    /**
     * Parses {@code access_restrictions} rules from Parquet record properties.
     * <p>
     * Maps access types, temporal scopes, and linear ranges.
     * Skips rules with disallowed access types or invalid record structures.
     * </p>
     *
     * @param rawAccessRestrictions Raw object from the 'access_restrictions' column.
     * @param segmentId             Contextual segment ID for logging.
     * @return List of parsed restrictions or {@code null} if empty/invalid.
     */
    public static List<OvertureAccessRestriction> parseRestriction(
            Object rawAccessRestrictions, String segmentId) {
        return parseList(rawAccessRestrictions, OvertureSchema.ACCESS_RESTRICTIONS, segmentId, item -> {
            if (!(item instanceof GenericRecord wrapper)) {
                logger.debug("Skipping access restrictions for segment {}: invalid record type", segmentId);
                return null;
            }

            AccessType accessType =
                    AccessType.fromString(extractString(wrapper, OvertureSchema.Restriction.ACCESS_TYPE));
            if (!OvertureParserFilter.INSTANCE.getAccessTypeFilter().isAllowed(accessType)) {
                logger.debug(
                        "Skipping access restrictions for segment {}: invalid access type '{}'",
                        segmentId,
                        accessType);
                return null;
            }

            LinearlyReferencedRange between =
                    parseRange(getValOrNull(wrapper, OvertureSchema.Scope.BETWEEN));
            PropertyScopeContainer when =
                    parseScope(getValOrNull(wrapper, OvertureSchema.Scope.WHEN), segmentId);

            return new OvertureAccessRestriction(accessType, when, between);
        });
    }

    // =============================================================================
    // ROAD SURFACE
    // =============================================================================

    /**
     * Parses {@code road_surface} rules from Parquet record properties.
     * <p>
     * Maps surface types and their linear ranges. Skips rules with
     * disallowed surface types or invalid record structures.
     * </p>
     *
     * @param rawRoadSurface Raw object from the 'road_surface' column.
     * @param segmentId      Contextual segment ID for logging.
     * @return List of parsed surface rules or {@code null} if empty/invalid.
     */
    public static List<OvertureRoadSurface> parseRoadSurface(
            Object rawRoadSurface, String segmentId) {
        return parseList(rawRoadSurface, OvertureSchema.ROAD_SURFACE, segmentId, item -> {
            if (!(item instanceof GenericRecord wrapper)) {
                logger.debug("Skipping road surface for segment {}: invalid record type", segmentId);
                return null;
            }
            RoadSurfaceType surfaceType =
                    RoadSurfaceType.fromString(extractString(wrapper, OvertureSchema.Surface.VALUE));
            if (!OvertureParserFilter.INSTANCE.getSurfaceTypeFilter().isAllowed(surfaceType)) {
                logger.debug(
                        "Skipping road surface for segment {}: invalid surface type '{}'",
                        segmentId,
                        surfaceType);
                return null;
            }

            LinearlyReferencedRange between =
                    parseRange(getValOrNull(wrapper, OvertureSchema.Scope.BETWEEN));

            return new OvertureRoadSurface(surfaceType, between);
        });
    }

    // =============================================================================
    // ROAD FLAGS
    // =============================================================================

    /**
     * Parses {@code road_flags} from Parquet record properties.
     * <p>
     * Maps a list of active flag strings (e.g., bridge, tunnel) to a boolean-based
     * flag container with an optional linear range. Skips if no flags are present.
     * </p>
     *
     * @param rawRoadFlags Raw object from the 'road_flags' column.
     * @param segmentId    Contextual segment ID for logging.
     * @return List of parsed road flags or {@code null} if empty/invalid.
     */
    public static List<OvertureRoadFlags> parseRoadFlags(Object rawRoadFlags, String segmentId) {
        return parseList(rawRoadFlags, OvertureSchema.ROAD_FLAGS, segmentId, item -> {
            if (!(item instanceof GenericRecord wrapper)) return null;
            Object valuesObj = getValOrNull(wrapper, OvertureSchema.Flags.VALUES);
            List<String> activeFlags = new ArrayList<>();

            if (valuesObj instanceof List<?> rawList) {
                for (Object val : rawList) {
                    activeFlags.add(extractString(val));
                }
            }

            if (activeFlags.isEmpty()) {
                logger.debug("Skipping road flags for segment {}: no flags defined", segmentId);
                return null;
            }

            boolean isBridge = activeFlags.contains(OvertureSchema.Flags.IS_BRIDGE);
            boolean isTunnel = activeFlags.contains(OvertureSchema.Flags.IS_TUNNEL);
            boolean isUnderConstruction =
                    activeFlags.contains(OvertureSchema.Flags.IS_UNDER_CONSTRUCTION);
            boolean isAbandoned = activeFlags.contains(OvertureSchema.Flags.IS_ABANDONED);
            boolean isCovered = activeFlags.contains(OvertureSchema.Flags.IS_COVERED);
            boolean isIndoor = activeFlags.contains(OvertureSchema.Flags.IS_INDOOR);

            LinearlyReferencedRange between =
                    parseRange(getValOrNull(wrapper, OvertureSchema.Scope.BETWEEN));

            return new OvertureRoadFlags(
                    isBridge, isTunnel, isUnderConstruction, isAbandoned, isCovered, isIndoor, between);
        });
    }
    // =============================================================================
    // CONNECTORS
    // =============================================================================

    /**
     * Parses the {@code connectors} array from a Parquet record into a list of {@link OvertureConnector}.
     * <p>
     * - Each item must contain a numeric {@code at} value to be considered valid.
     * - {@code connector_id} is read as a string when present.
     * - Duplicate connectors by exact {@code at} value are removed, preserving first-seen order.
     * - Returns {@code null} when the input is empty or no valid connectors remain.
     * </p>
     */
    public static List<OvertureConnector> parseConnectors(Object rawConnectors, String segmentId) {
        List<OvertureConnector> parsed =
                parseList(rawConnectors, OvertureSchema.CONNECTORS, segmentId, item -> {
                    if (!(item instanceof GenericRecord rec)) {
                        return null;
                    }
                    Object rawAt = getValOrNull(rec, "at");
                    if (rawAt == null) {
                        return null;
                    }
                    Double at = extractDouble(rawAt);
                    if (at == null) {
                        return null;
                    }
                    Object rawConnectorId = getValOrNull(rec, "connector_id");
                    if (rawConnectorId == null) {
                        return null;
                    }
                    String connectorId = extractString(rawConnectorId);
                    if (connectorId == null) {
                        return null;
                    }

                    return new OvertureConnector(connectorId, at);
                });

        if (parsed.isEmpty()) {
            return emptyList();
        }

        // Deduplicate by normalized `at` value (Double.toString), preserving first-seen order
        HashMap<String, OvertureConnector> uniqueByAt = new LinkedHashMap<>();
        for (OvertureConnector c : parsed) {
            String atKey = Double.toString(c.getAt());
            uniqueByAt.putIfAbsent(atKey, c);
        }
        return uniqueByAt.isEmpty() ? emptyList() : new ArrayList<>(uniqueByAt.values());
    }

    // =============================================================================
    // NAMES
    // =============================================================================

    /**
     * Parses the complex 'names' structure from the Avro record.
     * <p>
     * This handles the Overture names schema:
     * <ul>
     * <li>primary: The default name string.</li>
     * <li>common: A map of language tags to strings.</li>
     * <li>rules: A list of complex rules (variants, sides, ranges).</li>
     * </ul>
     * </p>
     *
     * @param rawNames  The raw object from the 'names' column.
     * @param segmentId The ID of the road segment (for debugging logs).
     * @return A populated {@link OvertureNames} object, or {@code null} if parsing fails.
     */
    public static OvertureNames parseNames(Object rawNames, String segmentId) {
        if (!(rawNames instanceof GenericRecord record)) {
            // It's valid for names to be null in some contexts, though rare for major roads.
            return null;
        }

        try {
            // 1. Primary Name
            Object rawPrimary = record.get(OvertureSchema.Names.PRIMARY);
            String primary = (rawPrimary != null) ? rawPrimary.toString() : null;

            // 2. Common Names Map
            Map<Bcp47LanguageTag, String> commonMap = null;
            Object rawCommon = record.get(OvertureSchema.Names.COMMON);
            if (rawCommon instanceof Map<?, ?> map) {
                commonMap = new HashMap<>();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        Bcp47LanguageTag tag = Bcp47LanguageTag.parse(entry.getKey().toString());
                        if (tag != null) {
                            commonMap.put(tag, entry.getValue().toString());
                        }
                    }
                }
            }

            // 3. Rules List
            List<OvertureNameRule> rulesList = null;
            Object rawRules = record.get(OvertureSchema.Names.RULES);
            if (rawRules instanceof Collection<?> collection) {
                rulesList = new ArrayList<>();
                for (Object item : collection) {
                    if (item instanceof GenericRecord ruleRecord) {
                        OvertureNameRule rule = parseNameRule(ruleRecord, segmentId);
                        if (rule != null) {
                            rulesList.add(rule);
                        }
                    }
                }
            }

            return new OvertureNames(primary,
                    commonMap != null ? commonMap : emptyMap(),
                    rulesList != null ? rulesList : emptyList());

        } catch (Exception e) {
            // Soft failure: Log debug info but don't crash the whole import
            logger.debug("Failed to parse names for segment {}: {}", segmentId, e.getMessage());
            return null;
        }
    }

    /**
     * Helper to parse a single name rule.
     */
    private static OvertureNameRule parseNameRule(GenericRecord r, String segmentId) {
        Object elementField = r.get(OvertureSchema.ELEMENT);
        if (elementField instanceof GenericRecord elementRecord) {
            r = elementRecord;
        }

        // Variant Enum
        Variant variant = safeParseOptionalEnum(
                r.get(OvertureSchema.Names.Rule.VARIANT),
                Variant::fromString,
                segmentId,
                OvertureSchema.Names.Rule.VARIANT);

        // Language Tag (String -> Bcp47LanguageTag)
        Object rawLang = r.get(OvertureSchema.Names.Rule.LANGUAGE);
        Bcp47LanguageTag langTag =
                (rawLang != null) ? Bcp47LanguageTag.parse(rawLang.toString()) : null;

        // Name Value
        Object rawValue = r.get(OvertureSchema.Names.Rule.VALUE);
        String value = (rawValue != null) ? rawValue.toString() : null;

        // Side Enum
        Side side = safeParseOptionalEnum(
                r.get(OvertureSchema.Names.Rule.SIDE),
                Side::fromString,
                segmentId,
                OvertureSchema.Names.Rule.SIDE);

        // Perspectives Struct
        Perspectives perspectives =
                parsePerspectives(r.get(OvertureSchema.Names.Rule.PERSPECTIVES), segmentId);

        // Between (Linear Reference Range)
        LinearlyReferencedRange between = parseRange(r.get(OvertureSchema.Scope.BETWEEN));

        return new OvertureNameRule(variant, langTag, perspectives, value, between, side);
    }

    /**
     * Helper to parse the perspectives struct (Mode + Countries list).
     */
    private static Perspectives parsePerspectives(Object rawPerspectives, String segmentId) {
        if (!(rawPerspectives instanceof GenericRecord r)) return null;

        Mode mode = safeParseOptionalEnum(
                r.get(OvertureSchema.Names.Rule.PERSPECTIVE_MODE),
                Mode::fromString,
                segmentId,
                OvertureSchema.Names.Rule.PERSPECTIVE_MODE);

        Set<String> countries = new HashSet<>();
        Object rawCountries = r.get(OvertureSchema.Names.Rule.PERSPECTIVE_COUNTRIES);
        if (rawCountries instanceof Collection<?> list) {
            for (Object c : list) {
                if (c != null) countries.add(c.toString());
            }
        }

        // A perspective must have a mode and at least one country to be valid
        if (mode == null || countries.isEmpty()) return null;
        return new Perspectives(mode, countries);
    }

    // =============================================================================
    // SHARED HELPERS
    // =============================================================================

    /**
     * Helper to parse a [start, end] double array into a LinearlyReferencedRange object.
     */
    static LinearlyReferencedRange parseRange(Object rawBetween) {
        // Avro typically deserializes arrays as a Collection or GenericData.Array
        if (rawBetween instanceof List<?> list && list.size() == 2) {
            Double start = extractDouble(list.get(0));
            Double end = extractDouble(list.get(1));

            if (start != null && end != null) {
                return new LinearlyReferencedRange(start, end);
            }
        }
        return null;
    }

    /**
     * Parses the temporal and conditional scopes (the {@code when} object) for a property.
     * <p>
     * Extracts time ranges, headings, travel reasons, recognized statuses, travel modes,
     * and vehicle attributes. Applies global road filters to each attribute.
     * Returns {@code null} if no valid scoping criteria are found.
     * </p>
     *
     * @param rawWhen   Raw {@link GenericRecord} representing the scope.
     * @param segmentId Contextual segment ID for logging.
     * @return A populated {@link PropertyScopeContainer} or {@code null} if empty/invalid.
     */
    static PropertyScopeContainer parseScope(Object rawWhen, String segmentId) {
        if (!(rawWhen instanceof GenericRecord whenRec)) return null;

        String during = extractString(whenRec, OvertureSchema.Scope.DURING);

        String headingStr = extractString(whenRec, OvertureSchema.Scope.HEADING);
        TravelHeading heading = TravelHeading.fromString(headingStr);
        if (!OvertureParserFilter.INSTANCE.getTravelHeadingFilter().isAllowed(heading)) heading = null;

        List<TravelReason> using = parseList(
                getValOrNull(whenRec, OvertureSchema.Scope.USING),
                OvertureSchema.Scope.USING,
                segmentId,
                item -> {
                    TravelReason reason = TravelReason.fromString(item.toString());
                    return OvertureParserFilter.INSTANCE.getTravelReasonFilter().isAllowed(reason)
                            ? reason
                            : null;
                });

        List<RecognizedStatus> recognized = parseList(
                getValOrNull(whenRec, OvertureSchema.Scope.RECOGNIZED),
                OvertureSchema.Scope.RECOGNIZED,
                segmentId,
                item -> {
                    RecognizedStatus status = RecognizedStatus.fromString(item.toString());
                    return OvertureParserFilter.INSTANCE.getRecognizedStatusFilter().isAllowed(status)
                            ? status
                            : null;
                });

        List<TravelMode> mode = parseList(
                getValOrNull(whenRec, OvertureSchema.Scope.MODE),
                OvertureSchema.Scope.MODE,
                segmentId,
                item -> {
                    TravelMode m = TravelMode.fromString(item.toString());
                    return OvertureParserFilter.INSTANCE.getTravelModeFilter().isAllowed(m) ? m : null;
                });

        List<VehicleAttributes> vehicle = parseList(
                getValOrNull(whenRec, OvertureSchema.Scope.VEHICLE),
                OvertureSchema.Scope.VEHICLE,
                segmentId,
                item -> parseVehicleAttribute(item, segmentId));

        if (during == null
                && heading == null
                && using.isEmpty()
                && recognized.isEmpty()
                && mode.isEmpty()
                && vehicle.isEmpty()) return null;
        return new PropertyScopeContainer(during, heading, using, recognized, mode, vehicle);
    }

    /**
     * Parses a vehicle attribute from a Parquet record and applies global road filters.
     * Extracts dimension, comparison operator, units, and value.
     * * @param rawItem the raw record object (GenericRecord)
     *
     * @return a valid VehicleAttributes instance, or null if filtered or invalid
     */
    static VehicleAttributes parseVehicleAttribute(Object rawItem, String segmentId) {
        if (!(rawItem instanceof GenericRecord vRec)) return null;

        DimensionRestriction dimension =
                DimensionRestriction.fromString(extractString(vRec, OvertureSchema.Vehicle.DIMENSION));
        ComparisonOperator comparison =
                ComparisonOperator.fromString(extractString(vRec, OvertureSchema.Vehicle.COMPARISON));
        Units units = Units.fromString(extractString(vRec, OvertureSchema.Vehicle.UNIT));
        Double value = extractDouble(getValOrNull(vRec, OvertureSchema.Vehicle.VALUE));

        if (!OvertureParserFilter.INSTANCE.getDimensionRestrictionFilter().isAllowed(dimension)
                || !OvertureParserFilter.INSTANCE.getComparisonOperatorFilter().isAllowed(comparison)
                || value == null) {
            logger.debug(
                    "Skipping vehicle attribute for segment {}: invalid dimension/comparison/value",
                    segmentId);
            return null;
        }

        return new VehicleAttributes(dimension, comparison, value, units);
    }
}
