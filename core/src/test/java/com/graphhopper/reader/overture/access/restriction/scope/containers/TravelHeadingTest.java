package com.graphhopper.reader.overture.access.restriction.scope.containers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TravelHeading} conversion helpers.
 */
class TravelHeadingTest {

    /**
     * Verifies that {@link TravelHeading#fromString(String)} parses valid values case-insensitively.
     */
    @Test
    void fromStringParsesValidValuesCaseInsensitive() {
        assertEquals(TravelHeading.FORWARD, TravelHeading.fromString("forward"));
        assertEquals(TravelHeading.BACKWARD, TravelHeading.fromString("BACKWARD"));
    }

    /**
     * Verifies that {@link TravelHeading#fromString(String)} returns {@code null} for {@code null}
     * or unknown values.
     */
    @Test
    void fromStringReturnsNullForNullOrUnknown() {
        assertNull(TravelHeading.fromString(null));
        assertNull(TravelHeading.fromString("unknown"));
    }

    /**
     * Verifies that {@link TravelHeading#toString()} returns lower-case names for enum constants.
     */
    @Test
    void toStringProducesLowerCase() {
        assertEquals("forward", TravelHeading.FORWARD.toString());
        assertEquals("backward", TravelHeading.BACKWARD.toString());
    }
}
