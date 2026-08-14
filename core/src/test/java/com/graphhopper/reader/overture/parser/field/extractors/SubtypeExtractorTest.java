package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.reader.overture.road.segment.OvertureSegmentSubtype;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtypeExtractorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void subtypeExists_trueWhenPresent() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-1",
                  "geometry": {"type": "LineString", "coordinates": [[10.0,20.0],[30.0,40.0]]},
                  "properties": {"subtype": "road"}
                }
                """);

        assertTrue(SubtypeExtractor.subtypeExists(feature));
    }

    @Test
    void subtypeExists_falseWhenMissing() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-1",
                  "geometry": {"type": "LineString", "coordinates": [[10.0,20.0],[30.0,40.0]]},
                  "properties": {}
                }
                """);

        assertFalse(SubtypeExtractor.subtypeExists(feature));
    }

    @Test
    void extractSubtype_returnsRoadForValidString() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-1",
                  "geometry": {"type": "LineString", "coordinates": [[10.0,20.0],[30.0,40.0]]},
                  "properties": {"subtype": "road"}
                }
                """);

        assertEquals(OvertureSegmentSubtype.ROAD, SubtypeExtractor.extractSubtype(feature));
    }

    @Test
    void extractSubtype_returnsRailForValidString() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-1",
                  "geometry": {"type": "LineString", "coordinates": [[10.0,20.0],[30.0,40.0]]},
                  "properties": {"subtype": "rail"}
                }
                """);

        assertEquals(OvertureSegmentSubtype.RAIL, SubtypeExtractor.extractSubtype(feature));
    }

    @Test
    void extractSubtype_returnsWaterForValidString() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-1",
                  "geometry": {"type": "LineString", "coordinates": [[10.0,20.0],[30.0,40.0]]},
                  "properties": {"subtype": "water"}
                }
                """);

        assertEquals(OvertureSegmentSubtype.WATER, SubtypeExtractor.extractSubtype(feature));
    }

    @Test
    void extractSubtype_handlesUpperCaseInput() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-1",
                  "geometry": {"type": "LineString", "coordinates": [[10.0,20.0],[30.0,40.0]]},
                  "properties": {"subtype": "ROAD"}
                }
                """);

        assertEquals(OvertureSegmentSubtype.ROAD, SubtypeExtractor.extractSubtype(feature));
    }

    @Test
    void extractSubtype_handlesMixedCaseInput() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-1",
                  "geometry": {"type": "LineString", "coordinates": [[10.0,20.0],[30.0,40.0]]},
                  "properties": {"subtype": "RaiL"}
                }
                """);

        assertEquals(OvertureSegmentSubtype.RAIL, SubtypeExtractor.extractSubtype(feature));
    }

    @Test
    void extractSubtype_returnsNullWhenSubtypeMissing() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-1",
                  "geometry": {"type": "LineString", "coordinates": [[10.0,20.0],[30.0,40.0]]},
                  "properties": {}
                }
                """);

        assertNull(SubtypeExtractor.extractSubtype(feature));
    }

    @Test
    void extractSubtype_returnsNullForInvalidString() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-1",
                  "geometry": {"type": "LineString", "coordinates": [[10.0,20.0],[30.0,40.0]]},
                  "properties": {"subtype": "invalid_subtype"}
                }
                """);

        assertNull(SubtypeExtractor.extractSubtype(feature));
    }

    @Test
    void extractSubtype_returnsNullForEmptyString() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-1",
                  "geometry": {"type": "LineString", "coordinates": [[10.0,20.0],[30.0,40.0]]},
                  "properties": {"subtype": ""}
                }
                """);

        assertNull(SubtypeExtractor.extractSubtype(feature));
    }

    @Test
    void extractSubtype_returnsNullForNullValue() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-1",
                  "geometry": {"type": "LineString", "coordinates": [[10.0,20.0],[30.0,40.0]]},
                  "properties": {"subtype": null}
                }
                """);

        assertNull(SubtypeExtractor.extractSubtype(feature));
    }
}