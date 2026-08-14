package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.names.Mode;
import com.graphhopper.reader.overture.names.Perspectives;
import com.graphhopper.reader.overture.parser.OvertureParserFilter;
import com.graphhopper.reader.overture.parser.features.NameRuleFeature;
import com.graphhopper.reader.overture.parser.features.PerspectivesFeature;
import java.util.HashSet;
import java.util.List;

/**
 * Extracts perspective information from Overture Maps segment JSON data.
 * Handles parsing of mode and associated countries.
 */
public class PerspectivesExtractor {

    /**
     * Extracts perspective information from an Overture name rule JSON node.
     *
     * @param ruleJson the JSON node of a name rule
     * @return a {@link Perspectives} object containing parsed perspective data,
     * or {@code null} if perspectives data is missing or invalid
     */
    public static Perspectives extractPerspectives(JsonNode ruleJson, String featureId) {
        JsonNode perspectivesNode = NameRuleFeature.PERSPECTIVES.getFeature(ruleJson, featureId);
        if (perspectivesNode == null) return null;

        String modeStr = PerspectivesFeature.MODE.parseString(perspectivesNode, featureId);
        Mode mode = Mode.fromString(modeStr);

        if (!OvertureParserFilter.INSTANCE.getModeFilter().isAllowed(mode)) {
            return null;
        }

        List<String> countriesList =
                PerspectivesFeature.COUNTRIES.parseList(perspectivesNode, JsonNode::asText, featureId);

        if (countriesList.isEmpty()) return null;

        return new Perspectives(mode, new HashSet<>(countriesList));
    }

    /**
     * Checks whether perspective data exists in the given name rule JSON.
     *
     * @param ruleJson the JSON node of a name rule
     * @return {@code true} if the rule contains a perspectives object
     */
    public static boolean perspectivesExists(JsonNode ruleJson, String featureId) {
        return PerspectivesFeature.MODE.existsIn(ruleJson, featureId)
                && PerspectivesFeature.COUNTRIES.existsIn(ruleJson, featureId);
    }
}
