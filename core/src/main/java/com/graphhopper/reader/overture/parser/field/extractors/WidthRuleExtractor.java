package com.graphhopper.reader.overture.parser.field.extractors;

import static com.graphhopper.reader.overture.parser.field.extractors.BetweenExtractor.extractBetween;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.parser.features.RuleFeature;
import com.graphhopper.reader.overture.parser.features.SegmentFeature;
import com.graphhopper.reader.overture.road.segment.rule.OvertureWidthRule;
import java.util.List;

/**
 * Extractor for {@code width_rules} data from Overture features.
 * <p>
 * Parses the physical road width, which may vary along the segment through linear referencing.
 */
public class WidthRuleExtractor {

    /**
     * Extracts a list of width rules from the provided JSON node.
     *
     * @param segmentJson raw GeoJSON feature node
     * @return the width rules, empty when the property is absent or holds nothing usable
     */
    public static List<OvertureWidthRule> extractWidthRules(JsonNode segmentJson) {
        return SegmentFeature.WIDTH_RULES.parseList(
                segmentJson, WidthRuleExtractor::parseWidthRule, null);
    }

    /**
     * Checks for the presence of the {@code width_rules} property.
     *
     * @param segmentJson raw GeoJSON feature node
     * @return {@code true} if the property exists and is not null
     */
    public static boolean widthRulesExist(JsonNode segmentJson) {
        JsonNode node = SegmentFeature.WIDTH_RULES.getFeature(segmentJson, null);
        return node != null && !node.isNull();
    }

    private static OvertureWidthRule parseWidthRule(JsonNode item, String featureId) {
        Double value = RuleFeature.VALUE.parseDouble(item, featureId);
        // A width of zero or less is a data error rather than "no width"; dropping it keeps a nonsense
        // value out of any future max_width parser.
        if (value == null || value <= 0) return null;

        return new OvertureWidthRule(
                value,
                extractBetween(
                        RuleFeature.BETWEEN.getFeature(item, featureId),
                        SegmentFeature.WIDTH_RULES,
                        featureId));
    }
}
