package com.graphhopper.reader.overture.parser.field.extractors;

import static com.graphhopper.reader.overture.parser.field.extractors.BetweenExtractor.extractBetween;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.parser.OvertureParserFilter;
import com.graphhopper.reader.overture.parser.features.RuleFeature;
import com.graphhopper.reader.overture.parser.features.SegmentFeature;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSubclass;
import com.graphhopper.reader.overture.road.segment.rule.OvertureSubclassRule;
import java.util.List;

/**
 * Extractor for {@code subclass_rules} data from Overture features.
 * <p>
 * Parses a subclass that applies to part of the segment only, through linear referencing.
 */
public class RoadSubclassRuleExtractor {

    /**
     * Extracts a list of subclass rules from the provided JSON node.
     *
     * @param segmentJson raw GeoJSON feature node
     * @return the subclass rules, empty when the property is absent or holds nothing usable
     */
    public static List<OvertureSubclassRule> extractRoadSubclassRules(JsonNode segmentJson) {
        return SegmentFeature.SUBCLASS_RULES.parseList(
                segmentJson, RoadSubclassRuleExtractor::parseSubclassRule, null);
    }

    /**
     * Checks for the presence of the {@code subclass_rules} property.
     *
     * @param segmentJson raw GeoJSON feature node
     * @return {@code true} if the property exists and is not null
     */
    public static boolean roadSubclassRulesExist(JsonNode segmentJson) {
        JsonNode node = SegmentFeature.SUBCLASS_RULES.getFeature(segmentJson, null);
        return node != null && !node.isNull();
    }

    private static OvertureSubclassRule parseSubclassRule(JsonNode item, String featureId) {
        OvertureRoadSubclass value =
                OvertureRoadSubclass.fromString(RuleFeature.VALUE.parseString(item, featureId));
        // Honour the same filter the top-level subclass goes through, otherwise a filtered subclass
        // could re-enter the model through a rule.
        if (value == null || !OvertureParserFilter.INSTANCE.getRoadSubclassFilter().isAllowed(value)) {
            return null;
        }

        return new OvertureSubclassRule(
                value,
                extractBetween(
                        RuleFeature.BETWEEN.getFeature(item, featureId),
                        SegmentFeature.SUBCLASS_RULES,
                        featureId));
    }
}
