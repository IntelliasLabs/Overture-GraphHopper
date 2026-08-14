package com.graphhopper.reader.overture.road.segment;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OvertureThemeTest {

    @Test
    void testFromStringValidLowerCase() {
        assertEquals(OvertureTheme.TRANSPORTATION, OvertureTheme.fromString("transportation"));
        assertEquals(OvertureTheme.PLACES, OvertureTheme.fromString("places"));
        assertEquals(OvertureTheme.BUILDINGS, OvertureTheme.fromString("buildings"));
    }

    @Test
    void testFromStringValidUpperCase() {
        assertEquals(OvertureTheme.DIVISIONS, OvertureTheme.fromString("DIVISIONS"));
        assertEquals(OvertureTheme.BASE, OvertureTheme.fromString("BASE"));
    }

    @Test
    void testFromStringValidMixedCase() {
        assertEquals(OvertureTheme.ADDRESSES, OvertureTheme.fromString("Addresses"));
    }

    @Test
    void testFromStringThrowsOnNull() {
        assertNull(OvertureTheme.fromString(null));
    }

    @Test
    void testFromStringThrowsOnUnknownValue() {
        String invalidValue = "weather";
        assertNull(OvertureTheme.fromString(invalidValue));
    }

    @Test
    void testToStringReturnsLowerCase() {
        assertEquals("transportation", OvertureTheme.TRANSPORTATION.toString());
        assertEquals("places", OvertureTheme.PLACES.toString());
        assertEquals("divisions", OvertureTheme.DIVISIONS.toString());
    }
}
