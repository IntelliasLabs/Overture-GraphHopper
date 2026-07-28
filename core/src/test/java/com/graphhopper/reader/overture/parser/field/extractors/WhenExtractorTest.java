package com.graphhopper.reader.overture.parser.field.extractors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.reader.overture.access.restriction.PropertyScopeContainer;
import com.graphhopper.reader.overture.access.restriction.scope.containers.VehicleAttributes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WhenExtractorTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Should parse complex WHEN object with lists")
    void parseWhen_Complex() throws Exception {
        String json = "{" + "\"during\": \"Mo-Fr 08:00-10:00\","
                + "\"heading\": \"forward\","
                + "\"mode\": [\"bus\", \"truck\"],"
                + "\"vehicle\": [{"
                + "  \"dimension\": \"weight\","
                + "  \"comparison\": \"greater_than\","
                + "  \"value\": 5.5,"
                + "  \"unit\": \"tonne\""
                + "}]"
                + "}";
        JsonNode node = mapper.readTree(json);

        PropertyScopeContainer when = WhenExtractor.extractWhen(node, "UNKNOWN");

        assertNotNull(when);
        assertEquals("Mo-Fr 08:00-10:00", when.getDuring());
        assertEquals("forward", when.getHeading().toString());

        assertEquals(2, when.getMode().size());
        assertEquals(1, when.getVehicle().size());

        VehicleAttributes v = when.getVehicle().getFirst();
        assertEquals(5.5, v.getNumericQuantity());
    }

    @Test
    @DisplayName("Should handle partial WHEN object")
    void parseWhen_Partial() throws Exception {
        String json = "{\"heading\": \"backward\"}";
        PropertyScopeContainer when = WhenExtractor.extractWhen(mapper.readTree(json), "UNKNOWN");

        assertNotNull(when);
        assertEquals("backward", when.getHeading().toString());
        assertNull(when.getDuring());
        assertNotNull(when.getMode());
    }

    @Test
    @DisplayName("Should handle empty object")
    void parseWhen_Empty() throws Exception {
        PropertyScopeContainer when = WhenExtractor.extractWhen(mapper.readTree("{}"), "UNKNOWN");
        assertNotNull(when);
        assertNull(when.getDuring());
        assertNull(when.getHeading());
        assertNotNull(when.getMode());
    }

    @Test
    @DisplayName("Should return null for null input")
    void parseWhen_Null() {
        assertNull(WhenExtractor.extractWhen(null, "UNKNOWN"));
    }

    @Test
    @DisplayName("Should handle empty or invalid mode list")
    void parseWhen_InvalidMode() throws Exception {
        String json = "{\"mode\": []}";
        PropertyScopeContainer when = WhenExtractor.extractWhen(mapper.readTree(json), "UNKNOWN");
        assertNotNull(when.getMode());

        String jsonWithNull = "{\"mode\": [\"bus\", null]}";
        PropertyScopeContainer whenNull =
                WhenExtractor.extractWhen(mapper.readTree(jsonWithNull), "UNKNOWN");
        assertEquals(1, whenNull.getMode().size());
    }

    @Test
    @DisplayName("Should skip invalid vehicle attributes")
    void parseWhen_InvalidVehicle() throws Exception {
        String json = "{\"vehicle\": [{\"value\": 5.5}]}";
        PropertyScopeContainer when = WhenExtractor.extractWhen(mapper.readTree(json), "UNKNOWN");

        assertNotNull(when.getVehicle());
    }
}
