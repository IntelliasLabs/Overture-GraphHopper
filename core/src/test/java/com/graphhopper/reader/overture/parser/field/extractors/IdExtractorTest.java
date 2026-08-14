package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdExtractorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void idExists_trueWhenPresent() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-1",
                  "geometry": {"type": "LineString", "coordinates": [[10.0,20.0],[30.0,40.0]]},
                  "properties": {}
                }
                """);

        assertTrue(IdExtractor.idExists(feature));
    }

    @Test
    void idExists_falseWhenMissing() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "geometry": {"type": "LineString", "coordinates": [[10.0,20.0],[30.0,40.0]]},
                  "properties": {}
                }
                """);

        assertFalse(IdExtractor.idExists(feature));
    }

    @Test
    void extractId_returnsIdText() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-123",
                  "geometry": {"type": "LineString", "coordinates": [[10.0,20.0],[30.0,40.0]]},
                  "properties": {}
                }
                """);

        assertEquals("seg-123", IdExtractor.extractId(feature));
    }

    @Test
    void extractId_nullWhenIdMissing() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "geometry": {"type": "LineString", "coordinates": [[10.0,20.0],[30.0,40.0]]},
                  "properties": {}
                }
                """);

        assertNull(IdExtractor.extractId(feature));
    }
}

