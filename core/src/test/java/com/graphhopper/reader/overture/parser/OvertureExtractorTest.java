package com.graphhopper.reader.overture.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OvertureExtractorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void extractSegment_returnsSegment_whenAllRequiredFieldsPresent() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-1",
                  "geometry": {"type": "LineString", "coordinates": [[10.0,20.0],[30.0,40.0]]},
                  "properties": {
                    "subtype": "road",
                    "bbox": [10.0, 20.0, 30.0, 40.0],
                    "connectors": [],
                    "routes": [],
                    "class": "motorway",
                    "destinations": [],
                    "prohibited_transitions": [],
                    "road_surface": [],
                    "road_flags": [],
                    "speed_limits": [],
                    "width_rules": [],
                    "subclass": "motorway",
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

        OvertureRoadSegment segment = OvertureExtractor.extractSegment(feature);
        assertNotNull(segment);
        assertEquals("seg-1", segment.getId());
        assertNotNull(segment.getLineString());
        assertEquals(2, segment.getLineString().getNumPoints());
        assertEquals(10.0, segment.getLineString().getCoordinateN(0).x, 1e-9);
        assertEquals(20.0, segment.getLineString().getCoordinateN(0).y, 1e-9);
    }

    @Test
    void extractSegment_returnsNull_whenIdMissing() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "geometry": {"type": "LineString", "coordinates": [[10.0,20.0],[30.0,40.0]]},
                  "properties": {
                    "subtype": "road",
                    "bbox": [10.0, 20.0, 30.0, 40.0],
                    "connectors": [],
                    "routes": [],
                    "class": "motorway",
                    "destinations": [],
                    "prohibited_transitions": [],
                    "road_surface": [],
                    "road_flags": [],
                    "speed_limits": [],
                    "width_rules": [],
                    "subclass": "motorway",
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

        assertNull(OvertureExtractor.extractSegment(feature));
    }

    @Test
    void extractSegment_returnsNull_whenGeometryNotLineString() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-1",
                  "geometry": {"type": "Point", "coordinates": [10.0, 20.0]},
                  "properties": {
                    "subtype": "road",
                    "bbox": [10.0, 20.0, 30.0, 40.0],
                    "connectors": [],
                    "routes": [],
                    "class": "motorway",
                    "destinations": [],
                    "prohibited_transitions": [],
                    "road_surface": [],
                    "road_flags": [],
                    "speed_limits": [],
                    "width_rules": [],
                    "subclass": "motorway",
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

        assertNull(OvertureExtractor.extractSegment(feature));
    }

    @Test
    void extractSegment_returnsNull_whenCoordinatesNotArray() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-1",
                  "geometry": {"type": "LineString", "coordinates": "not-an-array"},
                  "properties": {
                    "subtype": "road",
                    "bbox": [10.0, 20.0, 30.0, 40.0],
                    "connectors": [],
                    "routes": [],
                    "class": "motorway",
                    "destinations": [],
                    "prohibited_transitions": [],
                    "road_surface": [],
                    "road_flags": [],
                    "speed_limits": [],
                    "width_rules": [],
                    "subclass": "motorway",
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

        // featuresPresent() will still pass because it only checks that the coordinates field exists, but
        // LineStringExtractor.extractLineString() returns null (wrong JSON type) and the feature must be skipped.
        OvertureRoadSegment segment = OvertureExtractor.extractSegment(feature);
        assertNull(segment);
    }
    
    @Test
    void extractSegment_defaultsToRoad_whenSubtypeMissing() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-2",
                  "geometry": {"type": "LineString", "coordinates": [[1.0,2.0],[3.0,4.0]]},
                  "properties": {
                    "class": "primary",
                    "names": {"primary": "NoSubtype"}
                  }
                }
                """);
        OvertureRoadSegment segment = OvertureExtractor.extractSegment(feature);
        assertNotNull(segment);
        assertEquals("seg-2", segment.getId());
        assertEquals(com.graphhopper.reader.overture.road.segment.OvertureSegmentSubtype.ROAD, segment.getProperties().getSubtype());
    }

    @Test
    void extractSegment_handlesWater_whenSubtypeWater() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-water",
                  "geometry": {"type": "LineString", "coordinates": [[5.0,6.0],[7.0,8.0]]},
                  "properties": {
                    "subtype": "water",
                    "names": {"primary": "River"}
                  }
                }
                """);
        OvertureRoadSegment segment = OvertureExtractor.extractSegment(feature);
        assertNotNull(segment);
        assertEquals(com.graphhopper.reader.overture.road.segment.OvertureSegmentSubtype.WATER, segment.getProperties().getSubtype());
        // For WATER roadClass must be null
        assertNull(segment.getProperties().getRoadClass());
    }

    @Test
    void extractSegment_handlesRail_whenSubtypeRail() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-rail",
                  "geometry": {"type": "LineString", "coordinates": [[11.0,12.0],[13.0,14.0]]},
                  "properties": {
                    "subtype": "rail",
                    "class": "motorway",
                    "names": {"primary": "Railway"}
                  }
                }
                """);
        OvertureRoadSegment segment = OvertureExtractor.extractSegment(feature);
        assertNotNull(segment);
        assertEquals(com.graphhopper.reader.overture.road.segment.OvertureSegmentSubtype.RAIL, segment.getProperties().getSubtype());
        assertEquals("motorway", String.valueOf(segment.getProperties().getRoadClass()));
    }

    @Test
    void extractSegment_handlesUnknownSubtype() throws Exception {
        JsonNode feature = MAPPER.readTree("""
                {
                  "type": "Feature",
                  "id": "seg-unknown",
                  "geometry": {"type": "LineString", "coordinates": [[21.0,22.0],[23.0,24.0]]},
                  "properties": {
                    "subtype": "unknown_type",
                    "class": "secondary",
                    "names": {"primary": "UnknownSubtype"}
                  }
                }
                """);
        OvertureRoadSegment segment = OvertureExtractor.extractSegment(feature);
        assertNotNull(segment);
        assertEquals(com.graphhopper.reader.overture.road.segment.OvertureSegmentSubtype.ROAD, segment.getProperties().getSubtype());
    }
}
