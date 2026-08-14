package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.reader.overture.names.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NamesExtractorTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Should parse valid names with all fields")
    void extractNames_Valid() throws Exception {
        JsonNode node = mapper.readTree("""
            {
                "properties": {
                    "names": {
                        "primary": "Main Street",
                        "common": {
                            "en": "Main Street",
                            "de": "Hauptstraße"
                        },
                        "rules": [
                            {
                                "variant": "common",
                                "value": "Main St"
                            }
                        ]
                    }
                }
            }
            """);
        OvertureNames names = NamesExtractor.extractNames(node, "UNKNOWN");

        assertNotNull(names);
        assertEquals("Main Street", names.getPrimary());
        assertEquals("Main Street", names.getCommon().get(Bcp47LanguageTag.parse("en")));
        assertEquals(1, names.getRules().size());
    }

    @Test
    @DisplayName("Should parse names with only primary")
    void extractNames_PrimaryOnly() throws Exception {
        JsonNode node = mapper.readTree("""
            {
                "properties": {
                    "names": {
                        "primary": "Highway 101"
                    }
                }
            }
            """);
        OvertureNames names = NamesExtractor.extractNames(node, "UNKNOWN");

        assertNotNull(names);
        assertEquals("Highway 101", names.getPrimary());
        assertNotNull(names.getCommon());
        assertNotNull(names.getRules());
    }

    @Test
    @DisplayName("Should return null when primary name is missing")
    void extractNames_MissingPrimary() throws Exception {
        JsonNode node = mapper.readTree("""
            {
                "properties": {
                    "names": {
                        "common": { "en": "Test" }
                    }
                }
            }
            """);
        assertNull(NamesExtractor.extractNames(node, "UNKNOWN"));
    }

    @Test
    @DisplayName("Should return null when primary name is empty")
    void extractNames_EmptyPrimary() throws Exception {
        JsonNode node = mapper.readTree("""
            {
                "properties": {
                    "names": {
                        "primary": ""
                    }
                }
            }
            """);
        assertNull(NamesExtractor.extractNames(node, "UNKNOWN"));
    }

    @Test
    @DisplayName("Should return null for null input")
    void extractNames_NullInput() {
        assertNull(NamesExtractor.extractNames(null, "UNKNOWN"));
    }

    @Test
    @DisplayName("Should handle empty rules array gracefully")
    void extractNames_EmptyRules() throws Exception {
        JsonNode node = mapper.readTree("""
            {
                "properties": {
                    "names": {
                        "primary": "Test Road",
                        "rules": []
                    }
                }
            }
            """);
        OvertureNames names = NamesExtractor.extractNames(node, "UNKNOWN");

        assertNotNull(names);
        assertEquals("Test Road", names.getPrimary());
        assertNotNull(names.getRules());
    }

    @Test
    @DisplayName("Should parse complex name rules (Seeburger Straße simulation)")
    void parseComplexData_Names() throws Exception {
        JsonNode rootNode = mapper.readTree("""
            {
                "properties": {
                    "names": {
                        "primary": "Seeburger Straße",
                        "rules": [
                            {
                                "variant": "common",
                                "value": "Seeburger Chaussee",
                                "between": [0.0, 0.4113]
                            },
                            {
                                "variant": "common",
                                "value": "Seeburger Straße",
                                "between": [0.4484, 1.0]
                            }
                        ]
                    }
                }
            }
            """);

        OvertureNames names = NamesExtractor.extractNames(rootNode, "UNKNOWN");

        assertNotNull(names);
        assertEquals("Seeburger Straße", names.getPrimary());

        List<OvertureNameRule> rules = names.getRules();
        assertNotNull(rules);
        assertEquals(2, rules.size());

        OvertureNameRule rule1 = rules.getFirst();
        assertEquals(Variant.COMMON, rule1.getVariant());
        assertEquals("Seeburger Chaussee", rule1.getValue());
        assertNotNull(rule1.getBetween());
        assertEquals(0.0, rule1.getBetween().getStart(), 0.0001);
        assertEquals(0.4113, rule1.getBetween().getEnd(), 0.0001);

        OvertureNameRule rule2 = rules.get(1);
        assertEquals(Variant.COMMON, rule2.getVariant());
        assertEquals("Seeburger Straße", rule2.getValue());
        assertNotNull(rule2.getBetween());
        assertEquals(0.4484, rule2.getBetween().getStart(), 0.0001);
        assertEquals(1.0, rule2.getBetween().getEnd(), 0.0001);
    }

    @Test
    @DisplayName("Should correctly parse nested perspectives inside name rules")
    void extractNames_WithPerspectivesInRules() throws Exception {
        JsonNode node = mapper.readTree("""
            {
                "properties": {
                    "names": {
                        "primary": "City",
                        "rules": [
                            {
                                "variant": "common",
                                "value": "Name Variant A",
                                "perspectives": {
                                    "mode": "accepted_by",
                                    "countries": ["US", "FR"]
                                }
                            }
                        ]
                    }
                }
            }
            """);

        OvertureNames names = NamesExtractor.extractNames(node, "UNKNOWN");

        assertNotNull(names);
        assertEquals("City", names.getPrimary());

        List<OvertureNameRule> rules = names.getRules();
        assertNotNull(rules);
        assertEquals(1, rules.size());
        OvertureNameRule rule = rules.getFirst();
        assertEquals("Name Variant A", rule.getValue());

        assertNotNull(rule.getPerspectives(), "Perspectives object should be parsed");
        assertEquals(Mode.ACCEPTED_BY, rule.getPerspectives().getMode());
        assertTrue(rule.getPerspectives().getCountries().contains("US"));
        assertTrue(rule.getPerspectives().getCountries().contains("FR"));
    }

    @Test
    @DisplayName("Should parse real sample data with complex name rules (Seeburger Straße)")
    void parseRealSampleOfData_Names() throws Exception {
        InputStream is = getClass().
                getResourceAsStream("/com/graphhopper/reader/overture/parser/speedLimitTestSample.json");
        assertNotNull(is);

        JsonNode rootNode = mapper.readTree(is);

        OvertureNames names = NamesExtractor.extractNames(rootNode, "UNKNOWN");

        assertNotNull(names);

        assertEquals("Seeburger Straße", names.getPrimary());

        List<OvertureNameRule> rules = names.getRules();
        assertNotNull(rules);
        assertEquals(2, rules.size());

        OvertureNameRule rule1 = rules.getFirst();
        assertEquals(Variant.COMMON, rule1.getVariant());
        assertEquals("Seeburger Chaussee", rule1.getValue());
        assertNotNull(rule1.getBetween());
        assertEquals(0.0, rule1.getBetween().getStart(), 0.0001);
        assertEquals(0.4113, rule1.getBetween().getEnd(), 0.0001);

        OvertureNameRule rule2 = rules.get(1);
        assertEquals(Variant.COMMON, rule2.getVariant());
        assertEquals("Seeburger Straße", rule2.getValue());
        assertNotNull(rule2.getBetween());
        assertEquals(0.4484, rule2.getBetween().getStart(), 0.0001);
        assertEquals(1.0, rule2.getBetween().getEnd(), 0.0001);

        System.out.println("Primary: " + names.getPrimary());
        for (OvertureNameRule rule : rules) {
            System.out.println(rule);
        }
    }
}
