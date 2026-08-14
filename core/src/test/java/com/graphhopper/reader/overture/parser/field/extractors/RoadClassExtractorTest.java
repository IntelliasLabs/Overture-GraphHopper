package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.reader.overture.road.segment.OvertureRoadClass;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoadClassExtractorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void roadClassExists_falseWhenMissing() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-1",
                  "geometry": {"type": "LineString", "coordinates": [[10.0,20.0],[30.0,40.0]]},
                  "properties": {}
                }
                """);

        assertFalse(RoadClassExtractor.roadClassExists(feature, "seg-1"));
    }

    @Test
    void extractRoadClass_mapsStringToEnum() throws Exception {
        // Use the first enum value to avoid coupling the test to a specific set of road classes.
        OvertureRoadClass any = OvertureRoadClass.values()[0];
        String value = any.toString();

        String json = String.format("""
                {
                  "type": "Feature",
                  "id": "seg-1",
                  "geometry": {"type": "LineString", "coordinates": [[10.0,20.0],[30.0,40.0]]},
                  "properties": {"class": "%s"}
                }
                """, value);

        JsonNode feature = MAPPER.readTree(json);
        assertEquals(any, RoadClassExtractor.extractRoadClass(feature, "seg-1"));
    }
}
