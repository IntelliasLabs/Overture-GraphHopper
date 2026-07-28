package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.names.Bcp47LanguageTag;
import com.graphhopper.reader.overture.names.OvertureNameRule;
import com.graphhopper.reader.overture.names.OvertureNames;
import com.graphhopper.reader.overture.parser.features.SegmentFeature;
import java.util.List;
import java.util.Map;

/**
 * Extracts name information from Overture Maps segment JSON data.
 * Handles parsing of primary names, common name translations, and naming rules.
 */
public class NamesExtractor {

    /**
     * Extracts complete name information from an Overture segment JSON node.
     *
     * @param segmentJson the root JSON node of an Overture segment
     * @return an {@link OvertureNames} object containing parsed name data,
     * or {@code null} if names data is missing or invalid
     */
    public static OvertureNames extractNames(JsonNode segmentJson, String featureId) {
        String primary = SegmentFeature.PRIMARY.parseString(segmentJson, featureId);

        if (primary == null || primary.isEmpty()) {
            return null;
        }

        Map<Bcp47LanguageTag, String> common = SegmentFeature.COMMON.parseMap(
                segmentJson, Bcp47LanguageTag::parse, JsonNode::asText, featureId);

        List<OvertureNameRule> rules =
                SegmentFeature.RULES.parseList(segmentJson, NameRuleExtractor::extractNameRule, featureId);

        return new OvertureNames(primary, common, rules);
    }

    /**
     * Checks whether name data exists in the given segment JSON.
     *
     * @param segmentJson the root JSON node of an Overture segment
     * @return {@code true} if the segment contains name data, {@code false} otherwise
     */
    public static boolean namesExist(JsonNode segmentJson, String featureId) {
        return SegmentFeature.NAMES.existsIn(segmentJson, featureId);
    }
}
