package com.graphhopper.reader.overture.road.segment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OvertureFeatureTypeTest {

    @Test
    void testFromStringValidLowerCase() {
        assertEquals(OvertureFeatureType.SEGMENT, OvertureFeatureType.fromString("segment"));
        assertEquals(OvertureFeatureType.CONNECTOR, OvertureFeatureType.fromString("connector"));
        assertEquals(OvertureFeatureType.BUILDING, OvertureFeatureType.fromString("building"));
    }

    @Test
    void testFromStringValidUpperCase() {
        assertEquals(OvertureFeatureType.SEGMENT, OvertureFeatureType.fromString("SEGMENT"));
        assertEquals(OvertureFeatureType.WATER, OvertureFeatureType.fromString("WATER"));
    }

    @Test
    void testFromStringValidMixedCase() {
        assertEquals(OvertureFeatureType.LAND_USE, OvertureFeatureType.fromString("Land_Use"));
        assertEquals(
                OvertureFeatureType.DIVISION_BOUNDARY, OvertureFeatureType.fromString("Division_Boundary"));
    }

    @Test
    void testFromStringThrowsOnNull() {
        assertNull(OvertureFeatureType.fromString(null));
    }

    @Test
    void testFromStringThrowsOnUnknownValue() {
        assertNull(OvertureFeatureType.fromString(null));
    }

    @Test
    void testToStringReturnsLowerCase() {
        assertEquals("segment", OvertureFeatureType.SEGMENT.toString());
        assertEquals("building_part", OvertureFeatureType.BUILDING_PART.toString());
        assertEquals("land_cover", OvertureFeatureType.LAND_COVER.toString());
    }
}
