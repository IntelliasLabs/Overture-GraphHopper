package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.reader.overture.names.OvertureNameRule;
import com.graphhopper.reader.overture.names.Side;
import com.graphhopper.reader.overture.names.Variant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NameRuleExtractorTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Should parse valid name rule with all fields")
    void extractNameRule_Valid() throws Exception {
        JsonNode node = mapper.readTree("""
            {
                "variant": "common",
                "language": "en",
                "value": "Main Street",
                "between": [0.0, 1.0],
                "side": "left",
                "perspectives": {
                    "mode": "accepted_by",
                    "countries": ["US", "CA"]
                }
            }
            """);
        OvertureNameRule rule = NameRuleExtractor.extractNameRule(node, "UNKNOWN");

        assertNotNull(rule);
        assertEquals(Variant.COMMON, rule.getVariant());
        assertEquals("en", rule.getLanguage().toString());
        assertEquals("Main Street", rule.getValue());
        assertEquals(0.0, rule.getBetween().getStart());
        assertEquals(Side.LEFT, rule.getSide());
        assertNotNull(rule.getPerspectives());
    }

    @Test
    @DisplayName("Should parse name rule with minimal fields")
    void extractNameRule_Minimal() throws Exception {
        JsonNode node = mapper.readTree("""
            {
                "variant": "official",
                "value": "Highway 1"
            }
            """);
        OvertureNameRule rule = NameRuleExtractor.extractNameRule(node, "UNKNOWN");

        assertNotNull(rule);
        assertEquals(Variant.OFFICIAL, rule.getVariant());
        assertEquals("Highway 1", rule.getValue());
        assertNull(rule.getLanguage());
        assertNull(rule.getBetween());
        assertNull(rule.getSide());
    }

    @Test
    @DisplayName("Should return null if Variant is invalid/unknown")
    void extractNameRule_InvalidVariant() throws Exception {
        JsonNode node = mapper.readTree("""
            {
                "variant": "unknown_variant_type",
                "value": "Highway 1"
            }
            """);
        assertNull(NameRuleExtractor.extractNameRule(node, "UNKNOWN"));
    }

    @Test
    @DisplayName("Should return null if Value is missing")
    void extractNameRule_MissingValue() throws Exception {
        JsonNode node = mapper.readTree("""
            {
                "variant": "official"
            }
            """);
        assertNull(NameRuleExtractor.extractNameRule(node, "UNKNOWN"));
    }

    @Test
    @DisplayName("Should set Side to null if Side string is invalid")
    void extractNameRule_InvalidSide() throws Exception {
        JsonNode node = mapper.readTree("""
            {
                "variant": "official",
                "value": "Test",
                "side": "invalid_side"
            }
            """);
        OvertureNameRule rule = NameRuleExtractor.extractNameRule(node, "UNKNOWN");

        assertNotNull(rule);
        assertNull(rule.getSide(), "Side should be null for invalid values");
        assertEquals("Test", rule.getValue());
    }

    @Test
    @DisplayName("Should return null for null input")
    void extractNameRule_NullInput() {
        assertNull(NameRuleExtractor.extractNameRule(null, "UNKNOWN"));
    }

    @Test
    @DisplayName("Should handle null JSON node")
    void extractNameRule_NullNode() throws Exception {
        JsonNode node = mapper.readTree("null");
        assertNull(NameRuleExtractor.extractNameRule(node, "UNKNOWN"));
    }
}
