package com.graphhopper.reader.overture.access.restriction.scope.containers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Units} conversion helpers.
 */
class UnitsTest {

    /**
     * Verifies that {@link Units#fromString(String)} parses valid values case-insensitively.
     */
    @Test
    void fromStringParsesValidValuesCaseInsensitive() {
        assertEquals(Units.M, Units.fromString("m"));
        assertEquals(Units.KM, Units.fromString("KM"));
        assertEquals(Units.FT, Units.fromString("fT"));
    }

    /**
     * Verifies that {@link Units#fromString(String)} returns {@code null} for {@code null}
     * or unknown values.
     */
    @Test
    void fromStringReturnsNullForNullOrUnknown() {
        assertNull(Units.fromString(null));
        assertNull(Units.fromString("unknown"));
    }

    /**
     * Verifies that {@link Units#toString()} returns lower-case names for enum constants.
     */
    @Test
    void toStringProducesLowerCase() {
        assertEquals("m", Units.M.toString());
        assertEquals("km", Units.KM.toString());
        assertEquals("ft", Units.FT.toString());
    }
}
