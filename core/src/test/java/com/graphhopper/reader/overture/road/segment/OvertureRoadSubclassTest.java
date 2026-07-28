package com.graphhopper.reader.overture.road.segment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OvertureRoadSubclassTest {

    @Test
    void testFromString_ValidValues() {
        assertEquals(OvertureRoadSubclass.LINK, OvertureRoadSubclass.fromString("link"));
        assertEquals(OvertureRoadSubclass.SIDEWALK, OvertureRoadSubclass.fromString("SIDEWALK"));
        assertEquals(
                OvertureRoadSubclass.PARKING_AISLE, OvertureRoadSubclass.fromString("Parking_Aisle"));
        assertEquals(
                OvertureRoadSubclass.CYCLE_CROSSING, OvertureRoadSubclass.fromString("cycle_crossing"));
    }

    @Test
    void testFromString_ReturnsNullOnNullInput() {
        assertNull(OvertureRoadSubclass.fromString(null), "Null input should return null enum");
    }

    @Test
    void testFromString_ThrowsOnUnknownValue() {
        String invalid = "superhighway";
        assertNull(OvertureRoadSubclass.fromString(invalid), "Unknown value should return null");
    }

    @Test
    void testSpecificToStringMappings() {
        assertEquals("link", OvertureRoadSubclass.LINK.toString());
        assertEquals("parking_aisle", OvertureRoadSubclass.PARKING_AISLE.toString());
        assertEquals("cycle_crossing", OvertureRoadSubclass.CYCLE_CROSSING.toString());
    }
}
