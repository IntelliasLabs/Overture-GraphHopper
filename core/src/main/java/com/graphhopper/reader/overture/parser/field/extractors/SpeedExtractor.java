package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.common.speed.OvertureSpeed;
import com.graphhopper.reader.overture.common.speed.SpeedUnit;
import com.graphhopper.reader.overture.parser.OvertureParserFilter;
import com.graphhopper.reader.overture.parser.features.SpeedFeature;

/**
 * Extractor for speed-related data (maximum or minimum speeds) from Overture features.
 * <p>
 * Handles numeric values and their corresponding units (km/h, mph), ensuring
 * the data conforms to valid speed ranges for routing.
 */
public class SpeedExtractor {
    /**
     * Parses a single speed object (max_speed or min_speed) from the provided JSON node.
     * <p>
     * This method extracts the numeric value and the unit of measurement.
     * It ensures data quality by validating the resulting {@link OvertureSpeed}
     * instance against schema constraints via {@link OvertureSpeed#isValid()}.
     *
     * @param speedNodeJson the JSON node representing a speed object
     * @return a valid {@link OvertureSpeed} instance, or {@code null} if the node is missing,
     * null, or contains a value outside the allowed range (1-350 km/h)
     */
    public static OvertureSpeed extractSpeed(JsonNode speedNodeJson, String featureId) {
        if (speedNodeJson == null || speedNodeJson.isNull()) return null;

        Double value = SpeedFeature.VALUE.parseDouble(speedNodeJson, featureId);
        String unitStr = SpeedFeature.UNIT.parseString(speedNodeJson, featureId);

        if (value == null || unitStr == null) return null;

        SpeedUnit unit = SpeedUnit.fromString(unitStr);
        if (!OvertureParserFilter.INSTANCE.getSpeedUnitFilter().isAllowed(unit)) return null;

        OvertureSpeed speed = new OvertureSpeed(value, unit);
        return speed.isValid() ? speed : null;
    }

    /**
     * Checks whether the given JSON node contains a speed object with both required fields.
     * <p>
     * This is a lightweight presence check that does not validate the field contents. For full parsing and
     * validation use {@link #extractSpeed(JsonNode, String)}.
     *
     * @param segmentJson JSON node that is expected to contain a speed object
     * @return {@code true} if both {@code value} and {@code unit} fields are present, {@code false} otherwise
     */
    public static boolean speedExists(JsonNode segmentJson, String featureId) {
        return SpeedFeature.VALUE.existsIn(segmentJson, featureId)
                && SpeedFeature.UNIT.existsIn(segmentJson, featureId);
    }
}
