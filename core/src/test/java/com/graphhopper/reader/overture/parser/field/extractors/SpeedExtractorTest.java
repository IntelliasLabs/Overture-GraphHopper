package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.reader.overture.common.speed.OvertureSpeed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;



public class SpeedExtractorTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 351.0, -10.0, 500.0})
    @DisplayName("Should return NULL for invalid speed values (Boundary Check)")
    void parseSpeed_InvalidValues_ReturnsNull(double invalidValue) throws Exception {
        String json = String.format("{\"value\": %s}", invalidValue);
        JsonNode node = mapper.readTree(json);

        OvertureSpeed speed = SpeedExtractor.extractSpeed(node, "UNKNOWN");

        assertNull(speed, "Speed " + invalidValue + " Should return null");
    }

    @Test
    @DisplayName("Should handle missing value or null node")
    void parseSpeed_Nulls() throws Exception {
        assertNull(SpeedExtractor.extractSpeed(null, "UNKNOWN"));
        assertNull(SpeedExtractor.extractSpeed(mapper.readTree("null"), "UNKNOWN"));
        assertNull(SpeedExtractor.extractSpeed(mapper.readTree("{}"), "UNKNOWN"));
    }

    @Test
    @DisplayName("Should parse valid speed with unit")
    void parseSpeed_Valid() throws Exception {
        String json = "{\"value\": 50.0, \"unit\": \"km/h\"}";
        JsonNode node = mapper.readTree(json);

        OvertureSpeed speed = SpeedExtractor.extractSpeed(node, "UNKNOWN");

        assertNotNull(speed);
        assertEquals(50.0, speed.getValue());
        assertEquals("km/h", speed.getUnit().toString());
    }

    @Test
    @DisplayName("Should return null if unit is missing")
    void parseSpeed_MissingUnit() throws Exception {
        String json = "{\"value\": 60.0}";
        assertNull(SpeedExtractor.extractSpeed(mapper.readTree(json), "UNKNOWN"));
    }

    @Test
    @DisplayName("Should return null for UNKNOWN unit")
    void parseSpeed_UnknownUnit() throws Exception {
        String json = "{\"value\": 60.0, \"unit\": \"light_years_per_hour\"}";
        assertNull(SpeedExtractor.extractSpeed(mapper.readTree(json), "UNKNOWN"));
    }

    @Test
    @DisplayName("Should handle non-numeric speed values")
    void parseSpeed_InvalidType() throws Exception {
        assertNull(SpeedExtractor.extractSpeed(mapper.readTree("{\"value\": \"fast\", \"unit\": \"km/h\"}"), "UNKNOWN"));
        assertNull(SpeedExtractor.extractSpeed(mapper.readTree("{\"value\": [50], \"unit\": \"km/h\"}"), "UNKNOWN"));
    }
}
