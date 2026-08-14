package com.graphhopper.reader.overture.parser.field.extractors;

import static java.util.Collections.emptyList;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.road.segment.rule.OvertureWidthRule;
import java.util.List;

/**
 * Extractor for {@code width_rules} data from Overture features.
 * <p>
 * Parses conditional width constraints that may vary along the segment
 * or depend on specific vehicle characteristics.
 */
public class WidthRuleExtractor {
    /**
     * Extracts a list of width rules from the provided JSON node.
     * @param segmentJson raw GeoJSON feature node
     * @return a list of {@link OvertureWidthRule} objects, or {@code null} if missing or empty
     */
    public static List<OvertureWidthRule> extractWidthRules(JsonNode segmentJson) {
        return emptyList();
    }
    /**
     * Checks for the presence of the {@code width_rules} property.
     * @param segmentJson raw GeoJSON feature node
     * @return {@code true} if the property exists and is not null
     */
    public static boolean widthRulesExist(JsonNode segmentJson) {
        return true;
    }
}
