package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LineStringExtractorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void lineStringExists_trueForLineStringWithCoords() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-1",
                  "geometry": {"type": "LineString", "coordinates": [[10.0,20.0],[30.0,40.0]]},
                  "properties": {"class": "motorway"}
                }
                """);

        assertTrue(LineStringExtractor.lineStringExists(feature, "seg-1"));
    }

    @Test
    void lineStringExists_falseForWrongGeometryType() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-1",
                  "geometry": {"type": "Point", "coordinates": [10.0,20.0]},
                  "properties": {"class": "motorway"}
                }
                """);

        assertFalse(LineStringExtractor.lineStringExists(feature, "seg-1"));
    }

    @Test
    void extractLineString_buildsJtsLineStringWithSameOrder() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-1",
                  "geometry": {"type": "LineString", "coordinates": [[10.0,20.0],[30.0,40.0]]},
                  "properties": {"class": "motorway"}
                }
                """);

        var line = LineStringExtractor.extractLineString(feature, "seg-1");
        assertNotNull(line);
        assertEquals(2, line.getNumPoints());
        assertEquals(10.0, line.getCoordinateN(0).x, 1e-9);
        assertEquals(20.0, line.getCoordinateN(0).y, 1e-9);
        assertEquals(30.0, line.getCoordinateN(1).x, 1e-9);
        assertEquals(40.0, line.getCoordinateN(1).y, 1e-9);
    }

    @Test
    void extractLineString_returnsNullWhenCoordinatesNotArray() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-1",
                  "geometry": {"type": "LineString", "coordinates": "not-an-array"},
                  "properties": {"class": "motorway"}
                }
                """);

        assertNull(LineStringExtractor.extractLineString(feature, "seg-1"));
    }
}

