package com.graphhopper.reader.overture.parser.features;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SegmentFeatureTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void getFeature_topLevelId_returnsExpectedText() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-1",
                  "geometry": {"type": "LineString", "coordinates": [[1.0,2.0],[3.0,4.0]]},
                  "properties": {
                    "subtype": "road",
                    "class": "motorway",
                    "subclass": "motorway",
                    "destinations": [],
                    "prohibited_transitions": [],
                    "road_surface": [],
                    "road_flags": [],
                    "speed_limits": [],
                    "width_rules": [],
                    "subclass_rules": [],
                    "access_restrictions": [],
                    "level": 0,
                    "level_rules": [],
                    "theme": "transportation",
                    "type": "segment",
                    "version": 1,
                    "sources": [],
                    "names": {"primary": "Main"}
                  }
                }
                """);

        assertEquals("seg-1", SegmentFeature.ID.getFeature(feature, "seg-1").asText());
    }

    @Test
    void getFeature_nestedOtherName_class_readsPropertiesClass() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-1",
                  "geometry": {"type": "LineString", "coordinates": [[1.0,2.0],[3.0,4.0]]},
                  "properties": {"class": "motorway"}
                }
                """);

        assertEquals("motorway", SegmentFeature.ROAD_CLASS.getFeature(feature, "seg-1").asText());
        assertTrue(SegmentFeature.ROAD_CLASS.existsIn(feature, "seg-1"));
    }

    @Test
    void getFeature_missingParent_returnsNull() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-1",
                  "geometry": {"type": "LineString", "coordinates": [[1.0,2.0],[3.0,4.0]]}
                }
                """);

        assertNull(SegmentFeature.ROAD_CLASS.getFeature(feature, "seg-1"));
        assertFalse(SegmentFeature.ROAD_CLASS.existsIn(feature, "seg-1"));
    }
}

