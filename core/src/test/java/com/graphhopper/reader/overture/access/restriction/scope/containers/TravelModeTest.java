package com.graphhopper.reader.overture.access.restriction.scope.containers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TravelMode} conversion helpers.
 */
class TravelModeTest {

    /**
     * Verifies that {@link TravelMode#fromString(String)} parses valid values case-insensitively.
     */
    @Test
    void fromStringParsesValidValuesCaseInsensitive() {
        assertEquals(TravelMode.CAR, TravelMode.fromString("car"));
        assertEquals(TravelMode.TRUCK, TravelMode.fromString("TRUCK"));
        assertEquals(TravelMode.HGV, TravelMode.fromString("HgV"));
    }

    /**
     * Verifies that {@link TravelMode#fromString(String)} returns {@code null} for {@code null}
     * or unknown values.
     */
    @Test
    void fromStringReturnsNullForNullOrUnknown() {
        assertNull(TravelMode.fromString(null));
        assertNull(TravelMode.fromString("unknown"));
    }

    /**
     * Verifies that {@link TravelMode#toString()} returns lower-case names for enum constants.
     */
    @Test
    void toStringProducesLowerCase() {
        assertEquals("car", TravelMode.CAR.toString());
        assertEquals("truck", TravelMode.TRUCK.toString());
        assertEquals("hgv", TravelMode.HGV.toString());
    }
}
