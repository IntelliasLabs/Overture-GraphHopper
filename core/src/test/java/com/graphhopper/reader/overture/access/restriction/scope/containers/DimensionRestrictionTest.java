package com.graphhopper.reader.overture.access.restriction.scope.containers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DimensionRestriction} conversion helpers.
 */
class DimensionRestrictionTest {

    /**
     * Verifies that {@link DimensionRestriction#fromString(String)} parses valid values
     * case-insensitively.
     */
    @Test
    void fromStringParsesValidValuesCaseInsensitive() {
        assertEquals(DimensionRestriction.HEIGHT, DimensionRestriction.fromString("height"));
        assertEquals(DimensionRestriction.LENGTH, DimensionRestriction.fromString("LENGTH"));
        assertEquals(DimensionRestriction.AXLE_COUNT, DimensionRestriction.fromString("AxLe_CoUnT"));
    }

    /**
     * Verifies that {@link DimensionRestriction#fromString(String)} returns {@code null} for
     * {@code null} or unknown values.
     */
    @Test
    void fromStringReturnsNullForNullOrUnknown() {
        assertNull(DimensionRestriction.fromString(null));
        assertNull(DimensionRestriction.fromString("unknown"));
    }

    /**
     * Verifies that {@link DimensionRestriction#toString()} returns lower-case names for enum constants.
     */
    @Test
    void toStringProducesLowerCase() {
        assertEquals("height", DimensionRestriction.HEIGHT.toString());
        assertEquals("length", DimensionRestriction.LENGTH.toString());
        assertEquals("axle_count", DimensionRestriction.AXLE_COUNT.toString());
    }
}
