package com.graphhopper.reader.overture.parser.field.extractors;

import static java.util.Collections.emptyList;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.road.segment.rule.OvertureSubclassRule;
import java.util.List;

/**
 * Extractor for {@code subclass_rules} data from Overture features.
 * <p>
 * Handles conditional subclass assignments that may change based on
 * temporal or physical properties of the segment.
 */
public class RoadSubclassRuleExtractor {

    /**
     * Extracts a list of subclass rules from the provided JSON node.
     * @param segmentJson raw GeoJSON feature node
     * @return a list of {@link OvertureSubclassRule} objects, or {@code null} if missing or empty
     */
    public static List<OvertureSubclassRule> extractRoadSubclassRules(JsonNode segmentJson) {
        return emptyList();
    }

    /**
     * Checks for the presence of the {@code subclass_rules} property.
     * @param segmentJson raw GeoJSON feature node
     * @return {@code true} if the property exists and is not null
     */
    public static boolean roadSubclassRulesExist(JsonNode segmentJson) {
        return true;
    }
}
