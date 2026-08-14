package com.graphhopper.reader.overture.parser.field.extractors;

import static java.util.Collections.emptyList;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.LinearlyReferencedRange;
import com.graphhopper.reader.overture.access.restriction.PropertyScopeContainer;
import com.graphhopper.reader.overture.common.speed.OvertureSpeed;
import com.graphhopper.reader.overture.common.speed.OvertureSpeedLimit;
import com.graphhopper.reader.overture.parser.features.SegmentFeature;
import com.graphhopper.reader.overture.parser.features.SpeedLimitsFeature;
import java.util.ArrayList;
import java.util.List;

/**
 * Extractor for {@code speed_limits} array data from Overture features.
 * <p>
 * Aggregates complex speed limit definitions, including maximum and minimum speeds,
 * variable speed indicators, temporal conditions (when), and linear ranges (between).
 */
public class SpeedLimitExtractor {

    /**
     * Extracts and validates the speed limits array from the feature properties.
     * <p>
     * Parses {@code max_speed} and {@code min_speed} for each entry, skipping those
     * that don't contain at least one valid speed value (1-350 km/h).
     * Includes optional 'between' ranges and 'when' application scopes.
     *
     * @param segmentJson the JSON node of a GeoJSON feature
     * @return a list of valid {@link OvertureSpeedLimit}s, or {@code null} if empty or missing
     */
    public static List<OvertureSpeedLimit> extractSpeedLimits(
            JsonNode segmentJson, String featureId) {
        JsonNode limitsNode = SegmentFeature.SPEED_LIMITS.getFeature(segmentJson, featureId);

        if (limitsNode == null) {
            return emptyList();
        }

        List<OvertureSpeedLimit> speedLimits = new ArrayList<>();
        for (JsonNode limitNode : limitsNode) {
            JsonNode maxSpeedNode = SpeedLimitsFeature.MAX_SPEED.getFeature(limitNode, featureId);
            JsonNode minSpeedNode = SpeedLimitsFeature.MIN_SPEED.getFeature(limitNode, featureId);

            boolean hasMax = maxSpeedNode != null && SpeedExtractor.speedExists(maxSpeedNode, featureId);
            boolean hasMin = minSpeedNode != null && SpeedExtractor.speedExists(minSpeedNode, featureId);

            if (!hasMax && !hasMin) {
                continue;
            }

            OvertureSpeed maxSpeed = SpeedExtractor.extractSpeed(
                    SpeedLimitsFeature.MAX_SPEED.getFeature(limitNode, featureId), featureId);
            OvertureSpeed minSpeed = SpeedExtractor.extractSpeed(
                    SpeedLimitsFeature.MIN_SPEED.getFeature(limitNode, featureId), featureId);

            if (maxSpeed == null && minSpeed == null) continue;

            boolean isMaxSpeedVariable =
                    SpeedLimitsFeature.IS_MAX_SPEED_VARIABLE.parseBoolean(limitNode, false, featureId);

            LinearlyReferencedRange between = BetweenExtractor.extractBetween(
                    SpeedLimitsFeature.BETWEEN.getFeature(limitNode, featureId),
                    SpeedLimitsFeature.MAX_SPEED,
                    featureId);
            PropertyScopeContainer when = WhenExtractor.extractWhen(
                    SpeedLimitsFeature.WHEN.getFeature(limitNode, featureId), featureId);

            speedLimits.add(
                    new OvertureSpeedLimit(maxSpeed, minSpeed, isMaxSpeedVariable, between, when));
        }
        return speedLimits.isEmpty() ? emptyList() : speedLimits;
    }

    /**
     * Checks for the presence of the {@code speed_limits} property.
     * @param segmentJson raw GeoJSON feature node
     * @param featureId   identifier of the feature
     * @return {@code true} if the property exists and is not null
     */
    public static boolean speedLimitsExist(JsonNode segmentJson, String featureId) {
        return SegmentFeature.SPEED_LIMITS.existsIn(segmentJson, featureId);
    }
}
