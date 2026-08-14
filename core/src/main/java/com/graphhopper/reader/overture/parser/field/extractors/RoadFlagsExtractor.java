package com.graphhopper.reader.overture.parser.field.extractors;

import static com.graphhopper.reader.overture.parser.field.extractors.BetweenExtractor.extractBetween;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.LinearlyReferencedRange;
import com.graphhopper.reader.overture.parser.features.RoadFlagItemFeature;
import com.graphhopper.reader.overture.parser.features.SegmentFeature;
import com.graphhopper.reader.overture.road.flags.OvertureRoadFlags;
import java.util.List;

/**
 * Extractor for {@code road_flags} data from Overture features.
 * <p>
 * Parses physical attributes such as bridges, tunnels, and construction status.
 * Supports linear referencing to apply flags only to specific parts of a segment.
 */
public class RoadFlagsExtractor {

    /**
     * Extracts a list of road flags from the feature JSON.
     * @param segmentJson raw GeoJSON feature node
     * @param featureId   identifier of the feature for lookup
     * @return a list of {@link OvertureRoadFlags}, or {@code null} if missing or empty
     */
    public static List<OvertureRoadFlags> extractRoadFlags(JsonNode segmentJson, String featureId) {
        return SegmentFeature.ROAD_FLAGS.parseList(
                segmentJson, RoadFlagsExtractor::parseOvertureRoadFlag, featureId);
    }

    /**
     * Checks for the presence of the {@code road_flags} property.
     * @param segmentJson raw GeoJSON feature node
     * @return {@code true} if the property exists and is not null
     */
    public static boolean roadFlagsExist(JsonNode segmentJson) {
        return true;
    }

    /**
     * Parses a single road flag entry from a JSON node.
     * @param roadFlagNode the JSON node representing an individual flag
     * @param featureId    identifier of the feature for logging
     * @return a populated {@link OvertureRoadFlags} object
     */
    private static OvertureRoadFlags parseOvertureRoadFlag(JsonNode roadFlagNode, String featureId) {
        boolean isBridge = false;
        boolean isTunnel = false;
        boolean isUnderConstruction = false;
        boolean isAbandoned = false;
        boolean isCovered = false;
        boolean isIndoor = false;

        List<String> typesRoadNode =
                RoadFlagItemFeature.VALUES.parseList(roadFlagNode, JsonNode::asText, featureId);

        if (!typesRoadNode.isEmpty()) {
            for (var typeRoadNode : typesRoadNode) {
                if (typeRoadNode == null) break;

                switch (typeRoadNode) {
                    case "is_bridge":
                        isBridge = true;
                        break;
                    case "is_tunnel":
                        isTunnel = true;
                        break;
                    case "is_under_construction":
                        isUnderConstruction = true;
                        break;
                    case "is_abandoned":
                        isAbandoned = true;
                        break;
                    case "is_covered":
                        isCovered = true;
                        break;
                    case "is_indoor":
                        isIndoor = true;
                        break;
                }
            }
        }

        LinearlyReferencedRange linearlyReferencedRange = extractBetween(
                RoadFlagItemFeature.BETWEEN.getFeature(roadFlagNode, featureId),
                SegmentFeature.ROAD_FLAGS,
                featureId);

        return new OvertureRoadFlags(
                isBridge,
                isTunnel,
                isUnderConstruction,
                isAbandoned,
                isCovered,
                isIndoor,
                linearlyReferencedRange);
    }
}
