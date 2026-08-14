package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSubclass;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoadSubclassExtractorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void roadSubclassExists_trueWhenPresent() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-1",
                  "geometry": {"type": "LineString", "coordinates": [[10.0,20.0],[30.0,40.0]]},
                  "properties": {"subclass": "motorway"}
                }
                """);

        assertTrue(RoadSubclassExtractor.roadSubclassExists(feature, "seg-1"));
    }

    @Test
    void roadSubclassExists_trueWhenMissing() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-1",
                  "geometry": {"type": "LineString", "coordinates": [[10.0,20.0],[30.0,40.0]]},
                  "properties": {}
                }
                """);

        assertTrue(RoadSubclassExtractor.roadSubclassExists(feature, "seg-1"));
    }

    @Test
    void extractRoadSubclass_returnsEnumForValidString() throws Exception {
        // Use the first enum value to avoid coupling the test to exact string literals.
        OvertureRoadSubclass any = OvertureRoadSubclass.values()[0];
        String value = any.toString();

        String json = String.format("""
                {
                  "type": "Feature",
                  "id": "seg-1",
                  "geometry": {"type": "LineString", "coordinates": [[10.0,20.0],[30.0,40.0]]},
                  "properties": {"subclass": "%s"}
                }
                """, value);

        JsonNode feature = MAPPER.readTree(json);
        assertEquals(any, RoadSubclassExtractor.extractRoadSubclass(feature, "seg-1"));
    }

    @Test
    void extractRoadSubclass_returnsNullWhenSubclassMissing() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-1",
                  "geometry": {"type": "LineString", "coordinates": [[10.0,20.0],[30.0,40.0]]},
                  "properties": {}
                }
                """);

        // The current implementation calls .getFeature(...).asText() without a null-check, so a NPE is expected.
        assertNull(RoadSubclassExtractor.extractRoadSubclass(feature, "seg-1"));
    }
}
