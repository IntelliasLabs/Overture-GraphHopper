package com.graphhopper.reader.overture.parser;

import static java.util.Collections.emptyList;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.parser.features.SegmentFeature;
import com.graphhopper.reader.overture.parser.field.extractors.*;
import com.graphhopper.reader.overture.parser.field.extractors.SubtypeExtractor;
import com.graphhopper.reader.overture.road.segment.OvertureRoadClass;
import com.graphhopper.reader.overture.road.segment.OvertureRoadProperties;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.reader.overture.road.segment.OvertureSegmentSubtype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Orchestrates the conversion of Overture GeoJSON features into {@link OvertureRoadSegment} objects.
 * <p>
 * This extractor coordinates specialized field extractors to handle validation, road class filtering,
 * and subtype-specific mapping (ROAD, RAIL, WATER).
 * <p>
 * <b>Example:</b>
 * <pre>{@code
 * JsonNode featureJson = ...;
 * OvertureRoadSegment segment = OvertureExtractor.extractSegment(featureJson);
 * }</pre>
 *
 * @see com.graphhopper.reader.overture.parser.field.extractors
 */
public class OvertureExtractor {

    private static final OvertureParserFilter parserFilter = new OvertureParserFilter();
    private static final Logger logger = LoggerFactory.getLogger(OvertureExtractor.class);

    /**
     * Extracts an {@link OvertureRoadSegment} from the given GeoJSON feature represented by the
     * provided {@link JsonNode}.
     * <p>
     * This method performs the following steps:
     * <ul>
     *   <li>Validates that the feature contains the minimal required fields via
     *   {@link #featuresPresent(JsonNode)}. If validation fails, a warning is logged (including the
     *   feature id when available) and {@code null} is returned.</li>
     *   <li>Determines the {@link OvertureRoadClass} using
     *   {@link RoadClassExtractor#extractRoadClass(JsonNode, String)} and checks it against the
     *   {@link com.graphhopper.reader.overture.parser.filters.RoadClassFilter} configured in
     *   {@link OvertureParserFilter}. If the road class is not allowed the feature is skipped and
     *   {@code null} is returned.</li>
     *   <li>Builds a new {@link OvertureRoadSegment} from the feature's id, geometry and road
     *   properties. The geometry is expected to be a GeoJSON {@code LineString} and is extracted via
     *   {@link LineStringExtractor#extractLineString(JsonNode, String)}. All other road-related attributes
     *   are collected into an {@link OvertureRoadProperties} instance using the various field
     *   extractors in {@code com.graphhopper.reader.overture.parser.field.extractors}.</li>
     * </ul>
     * The method does not throw checked exceptions. Parsing and mapping errors from the underlying
     * extractor utilities may result in runtime exceptions, which are propagated to the caller.
     *
     * @param featureJson the JSON node representing a single GeoJSON feature of the overture
     *                    road network; must not be {@code null}
     * @return a fully constructed {@link OvertureRoadSegment} if the feature passes basic
     * validation and road class filtering; {@code null} if the feature is invalid or
     * filtered out
     */
    public static OvertureRoadSegment extractSegment(JsonNode featureJson) {

        // TODO: All null-checks should be here:

        JsonNode featureIdNode = SegmentFeature.ID.getFeature(featureJson, "UNKNOWN");
        String featureId;
        if (featureIdNode == null) {
            logger.warn("Skipping feature cause id value is invalid or missed.");
            return null;
        }
        featureId = featureIdNode.asText();

        if (!featuresPresent(featureJson)) {
            logger.warn(
                    "Skipping feature with id: {} due to invalid or missing required properties.", featureId);
            return null;
        }
        // Read subtype first and adapt validation/parsing accordingly. Default to ROAD when missing.
        OvertureSegmentSubtype subtype = SubtypeExtractor.extractSubtype(featureJson);
        if (subtype == null) subtype = OvertureSegmentSubtype.ROAD;

        // Handle subtype-specific behavior
        switch (subtype) {
            case WATER:
                // For water features: certain road-specific fields must be null or emptyList().
                // Other metadata (connectors, routes, accessRestrictions, names, etc.) may still be
                // present.
                return new OvertureRoadSegment(
                        IdExtractor.extractId(featureJson),
                        LineStringExtractor.extractLineString(featureJson, featureId),
                        new OvertureRoadProperties(
                                ConnectorExtractor.extractConnectors(featureJson), // connectors allowed
                                RouteExtractor.extractRoutes(featureJson), // routes allowed
                                null, // roadClass must be null for water
                                emptyList(), // destinations must be emptyList()
                                emptyList(), // prohibitedTransitions must be emptyList()
                                emptyList(), // surfaces must be emptyList()
                                emptyList(), // flags must be emptyList()
                                emptyList(), // speedLimits must be emptyList()
                                emptyList(), // widthRules must be emptyList()
                                null, // subclass must be null
                                emptyList(), // subclassRules must be emptyList()
                                AccessRestrictionExtractor.extractAccessRestrictions(
                                        featureJson, featureId), // accessRestrictions allowed
                                LevelExtractor.extractLevel(featureJson),
                                LevelRulesExtractor.extractLevelRules(featureJson),
                                ThemeExtractor.extractTheme(featureJson),
                                FeatureTypeExtractor.extractFeatureType(featureJson),
                                VersionExtractor.extractVersion(featureJson),
                                SourceExtractor.extractSources(featureJson),
                                NamesExtractor.extractNames(featureJson, featureId),
                                OvertureSegmentSubtype.WATER));

            case RAIL:
                // Not routable by any profile this reader supports, and rail carries no access
                // restrictions, so importing one yields a road open to every mode. This used to build a
                // segment whenever the class survived the filter, which let class=unknown rail through.
                // See the matching check in OvertureParquetParser.tryMapRecord.
                logger.debug("Skipping rail segment with id: {}", featureId);
                return null;

            case ROAD:
            default: {
                // Default/road behaviour: full parsing as before
                var roadClass = RoadClassExtractor.extractRoadClass(featureJson, featureId);
                if (!OvertureParserFilter.INSTANCE.getRoadClassFilter().isAllowed(roadClass)) {
                    logger.info(
                            "Skipping segment with id: {} due to filtering of road class: {}.",
                            SegmentFeature.ID.getFeature(featureJson, "ID") != null
                                    ? SegmentFeature.ID.getFeature(featureJson, "ID").asText()
                                    : "unknown",
                            roadClass);

                    return null;
                }

                return new OvertureRoadSegment(
                        IdExtractor.extractId(featureJson),
                        LineStringExtractor.extractLineString(featureJson, featureId),
                        new OvertureRoadProperties(
                                ConnectorExtractor.extractConnectors(featureJson),
                                RouteExtractor.extractRoutes(featureJson),
                                roadClass,
                                DestinationExtractor.extractDestinations(featureJson),
                                ProhibitedDestinationExtractor.extractProhibitedDestinations(featureJson),
                                RoadSurfaceExtractor.extractRoadSurfaces(featureJson, featureId),
                                RoadFlagsExtractor.extractRoadFlags(featureJson, featureId),
                                SpeedLimitExtractor.extractSpeedLimits(featureJson, featureId),
                                WidthRuleExtractor.extractWidthRules(featureJson),
                                RoadSubclassExtractor.extractRoadSubclass(featureJson, featureId),
                                RoadSubclassRuleExtractor.extractRoadSubclassRules(featureJson),
                                AccessRestrictionExtractor.extractAccessRestrictions(featureJson, featureId),
                                LevelExtractor.extractLevel(featureJson),
                                LevelRulesExtractor.extractLevelRules(featureJson),
                                ThemeExtractor.extractTheme(featureJson),
                                FeatureTypeExtractor.extractFeatureType(featureJson),
                                VersionExtractor.extractVersion(featureJson),
                                SourceExtractor.extractSources(featureJson),
                                NamesExtractor.extractNames(featureJson, featureId),
                                OvertureSegmentSubtype.ROAD));
            }
        }
    }

    /**
     * Checks whether the given GeoJSON feature contains the minimal set of fields required to
     * construct an {@link OvertureRoadSegment}.
     * <p>
     * Currently the following conditions must hold:
     * <ul>
     *   <li>The feature id exists and is considered valid according to
     *   {@link IdExtractor#idExists(JsonNode)}.</li>
     *   <li>A road class property exists, see {@link SegmentFeature#ROAD_CLASS}.</li>
     *   <li>A geometry type exists, see {@link SegmentFeature#GEOMETRY_TYPE}, and its value is
     *   exactly {@code "LineString"}.</li>
     *   <li>Geometry coordinates are present and stored as an array, see
     *   {@link SegmentFeature#COORDINATES}.</li>
     * </ul>
     * No other properties (e.g. subclass or additional road attributes) are validated here. This
     * method is intentionally minimal and may be extended in the future as needed.
     *
     * @param featureJson the JSON node representing a single GeoJSON feature; must not be
     *                    {@code null}
     * @return {@code true} if all required fields exist and the geometry type is a
     * {@code LineString}; {@code false} otherwise
     */
    private static boolean featuresPresent(JsonNode featureJson) {
        // Require id and geometry always
        if (!IdExtractor.idExists(featureJson)) return false;

        // Get featureId for further checks
        String featureId = null;
        JsonNode featureIdNode = SegmentFeature.ID.getFeature(featureJson, "UNKNOWN");
        if (featureIdNode != null) {
            featureId = featureIdNode.asText();
        } else {
            featureId = "unknown";
        }

        if (!LineStringExtractor.lineStringExists(featureJson, featureId)) return false;

        // Determine subtype; default to ROAD when missing for backward compatibility
        OvertureSegmentSubtype subtype = SubtypeExtractor.extractSubtype(featureJson);
        if (subtype == null) {
            subtype = OvertureSegmentSubtype.ROAD;
        }

        switch (subtype) {
            case WATER:
                // For water features we only need id and geometry (subtype handled above)
                return true;
            case RAIL:
                // For rail only class is required (other road-specific fields may be absent)
                return RoadClassExtractor.roadClassExists(featureJson, featureId);
            case ROAD:
            default:
                // For road only 'class' is required; other attributes are optional
                return RoadClassExtractor.roadClassExists(featureJson, featureId);
        }
    }
}
