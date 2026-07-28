package com.graphhopper.reader.overture.road.segment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OvertureRoadClassTest {

    @Test
    void testFromStringValidLowerCase() {
        assertEquals(OvertureRoadClass.MOTORWAY, OvertureRoadClass.fromString("motorway"));
        assertEquals(OvertureRoadClass.PRIMARY, OvertureRoadClass.fromString("primary"));
        assertEquals(OvertureRoadClass.LIVING_STREET, OvertureRoadClass.fromString("living_street"));
    }

    @Test
    void testFromStringValidUpperCase() {
        assertEquals(OvertureRoadClass.SECONDARY, OvertureRoadClass.fromString("SECONDARY"));
        assertEquals(OvertureRoadClass.TRUNK, OvertureRoadClass.fromString("TRUNK"));
    }

    @Test
    void testFromStringValidMixedCase() {
        assertEquals(OvertureRoadClass.TERTIARY, OvertureRoadClass.fromString("Tertiary"));
        assertEquals(OvertureRoadClass.SERVICE, OvertureRoadClass.fromString("Service"));
    }

    @Test
    void testFromStringThrowsOnNull() {
        assertNull(OvertureRoadClass.fromString(null));
    }

    @Test
    void testFromStringThrowsOnUnknownValue() {
        String invalidValue = "spaceship_way";
        assertNull(OvertureRoadClass.fromString(invalidValue));
    }

    @Test
    void testToStringReturnsLowerCase() {
        assertEquals("motorway", OvertureRoadClass.MOTORWAY.toString());
        assertEquals("living_street", OvertureRoadClass.LIVING_STREET.toString());
        assertEquals("unclassified", OvertureRoadClass.UNCLASSIFIED.toString());
    }
}
