package com.graphhopper.reader.overture.parser.field.extractors;

import static com.graphhopper.reader.overture.parser.field.extractors.BetweenExtractor.extractBetween;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.parser.features.RuleFeature;
import com.graphhopper.reader.overture.parser.features.SegmentFeature;
import com.graphhopper.reader.overture.road.segment.rule.OvertureLevelRule;
import java.util.List;

/**
 * Extractor for {@code level_rules} data from Overture features.
 * <p>
 * Parses the z-order level, which tells crossing ways apart and may vary along the segment through
 * linear referencing.
 */
public class LevelRulesExtractor {

    /**
     * Extracts a list of level rules from the provided JSON node.
     *
     * @param segmentJson raw GeoJSON feature node
     * @return the level rules, empty when the property is absent or holds nothing usable
     */
    public static List<OvertureLevelRule> extractLevelRules(JsonNode segmentJson) {
        return SegmentFeature.LEVEL_RULES.parseList(
                segmentJson, LevelRulesExtractor::parseLevelRule, null);
    }

    /**
     * Checks for the presence of the {@code level_rules} property.
     *
     * @param segmentJson raw GeoJSON feature node
     * @return {@code true} if the property exists and is not null
     */
    public static boolean levelRulesExist(JsonNode segmentJson) {
        JsonNode node = SegmentFeature.LEVEL_RULES.getFeature(segmentJson, null);
        return node != null && !node.isNull();
    }

    private static OvertureLevelRule parseLevelRule(JsonNode item, String featureId) {
        Integer value = RuleFeature.VALUE.parseInteger(item, featureId);
        // Level 0 is meaningful here - it is ground level - so unlike width only absence is rejected.
        if (value == null) return null;

        return new OvertureLevelRule(
                value,
                extractBetween(
                        RuleFeature.BETWEEN.getFeature(item, featureId),
                        SegmentFeature.LEVEL_RULES,
                        featureId));
    }
}
