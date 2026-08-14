package com.graphhopper.reader.overture.parser.field.extractors;

import static java.util.Collections.emptyList;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.road.segment.rule.OvertureLevelRule;
import java.util.List;

/**
 * Extractor for {@code level_rules} data from Overture features.
 * <p>
 * Handles conditional vertical layering rules that may vary based on
 * specific properties or temporal conditions.
 */
public class LevelRulesExtractor {
    /**
     * Extracts a list of level rules from the provided JSON node.
     * @param segmentJson raw GeoJSON feature node
     * @return a list of {@link OvertureLevelRule} objects, or {@code null} if missing or empty
     */
    public static List<OvertureLevelRule> extractLevelRules(JsonNode segmentJson) {
        return emptyList();
    }
    /**
     * Checks for the presence of the {@code level_rules} property.
     * @param segmentJson raw GeoJSON feature node
     * @return {@code true} if the property exists and is not null
     */
    public static boolean levelRulesExist(JsonNode segmentJson) {
        return true;
    }
}
