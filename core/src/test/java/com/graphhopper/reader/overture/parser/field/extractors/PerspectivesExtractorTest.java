package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.reader.overture.names.Mode;
import com.graphhopper.reader.overture.names.Perspectives;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PerspectivesExtractorTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Should parse valid perspectives with mode and countries")
    void extractPerspectives_Valid() throws Exception {
        JsonNode node = mapper.readTree("""
            {
                "perspectives": {
                    "mode": "accepted_by",
                    "countries": ["DE", "US"]
                }
            }
            """);

        Perspectives perspectives = PerspectivesExtractor.extractPerspectives(node, "UNKNOWN");

        assertNotNull(perspectives);
        assertEquals(Mode.ACCEPTED_BY, perspectives.getMode());
        assertEquals(Set.of("DE", "US"), perspectives.getCountries());
    }

    @Test
    @DisplayName("Should return null when perspectives node is missing entirely")
    void extractPerspectives_MissingNode() throws Exception {
        JsonNode node = mapper.readTree("""
            {
                "other_field": "value"
            }
            """);
        assertNull(PerspectivesExtractor.extractPerspectives(node, "UNKNOWN"));
    }

    @Test
    @DisplayName("Should return null when mode is missing")
    void extractPerspectives_NoMode() throws Exception {
        JsonNode node = mapper.readTree("""
            {
                "perspectives": {
                    "countries": ["DE"]
                }
            }
            """);
        assertNull(PerspectivesExtractor.extractPerspectives(node, "UNKNOWN"));
    }

    @Test
    @DisplayName("Should return null when mode is invalid")
    void extractPerspectives_InvalidMode() throws Exception {
        JsonNode node = mapper.readTree("""
            {
                "perspectives": {
                    "mode": "unknown_mode",
                    "countries": ["DE"]
                }
            }
            """);
        assertNull(PerspectivesExtractor.extractPerspectives(node, "UNKNOWN"));
    }

    @Test
    @DisplayName("Should return null when countries array is empty")
    void extractPerspectives_EmptyCountries() throws Exception {
        JsonNode node = mapper.readTree("""
            {
                "perspectives": {
                    "mode": "disputed_by",
                    "countries": []
                }
            }
            """);
        assertNull(PerspectivesExtractor.extractPerspectives(node, "UNKNOWN"));
    }
}
