package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.LinearlyReferencedRange;
import com.graphhopper.reader.overture.names.*;
import com.graphhopper.reader.overture.parser.OvertureParserFilter;
import com.graphhopper.reader.overture.parser.features.NameRuleFeature;
import com.graphhopper.reader.overture.parser.features.SegmentFeature;

/**
 * Extracts name rule information from Overture Maps segment JSON data.
 * Handles parsing of individual naming rules including variant, language,
 * perspectives, value, between range, and side.
 */
public class NameRuleExtractor {

    /**
     * Extracts a single name rule from an Overture Name Rule JSON node.
     *
     * @param ruleJson the JSON node representing a single name rule
     * @return an {@link OvertureNameRule} object containing parsed name rule data,
     * or {@code null} if the name rule data is missing or invalid
     */
    public static OvertureNameRule extractNameRule(JsonNode ruleJson, String featureId) {
        if (ruleJson == null) return null;

        String variantStr = NameRuleFeature.VARIANT.parseString(ruleJson, featureId);
        Variant variant = Variant.fromString(variantStr);
        if (!OvertureParserFilter.INSTANCE.getVariantFilter().isAllowed(variant)) {
            return null;
        }

        String value = NameRuleFeature.VALUE.parseString(ruleJson, featureId);
        if (value == null || value.isEmpty()) return null;

        String languageStr = NameRuleFeature.LANGUAGE.parseString(ruleJson, featureId);
        Bcp47LanguageTag language = Bcp47LanguageTag.parse(languageStr);

        Perspectives perspectives = PerspectivesExtractor.extractPerspectives(ruleJson, featureId);

        LinearlyReferencedRange between = BetweenExtractor.extractBetween(
                NameRuleFeature.BETWEEN.getFeature(ruleJson, featureId), SegmentFeature.RULES, featureId);

        String sideStr = NameRuleFeature.SIDE.parseString(ruleJson, featureId);
        Side side = Side.fromString(sideStr);
        if (!OvertureParserFilter.INSTANCE.getSideFilter().isAllowed(side)) {
            side = null;
        }

        return new OvertureNameRule(variant, language, perspectives, value, between, side);
    }

    /**
     * Checks whether name rule data exists in the given rule JSON.
     *
     * @param ruleJson the JSON node of a name rule
     * @return {@code true} if the rule contains the required variant and value fields
     */
    public static boolean nameRuleExist(JsonNode ruleJson, String featureId) {
        return NameRuleFeature.VARIANT.existsIn(ruleJson, featureId)
                && NameRuleFeature.VALUE.existsIn(ruleJson, featureId);
    }
}
