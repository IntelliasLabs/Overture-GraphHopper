package com.graphhopper.reader.overture.common.speed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class SpeedUnitTest {

    /**
     * Verifies that "km/h" and "mph" is parsed correctly.
     */
    @Test
    void parsesKmhVariations() {
        assertEquals(SpeedUnit.KM_H, SpeedUnit.fromString("km/h"));
        assertEquals(SpeedUnit.MPH, SpeedUnit.fromString("mph"));
    }

    /**
     * Verifies that invalid, unknown, or empty strings safely return null
     * instead of throwing exceptions.
     */
    @ParameterizedTest(name = "Should return null for invalid input: \"{0}\"")
    @NullAndEmptySource
    @ValueSource(
            strings = {
                "knots", /// Unknown unit
                "m/s", /// Unknown unit
                "kmh", /// Missing slash (typo)
                " " /// Whitespace only
            })
    void returnsNullForInvalidInput(String input) {
        assertNull(SpeedUnit.fromString(input));
    }

    /**
     * Verifies the toString() behavior.
     * EXPECTED: Should return the stored value ("km/h"), not the enum name ("KM_H").
     */
    @Test
    void testToString() {
        assertEquals("mph", SpeedUnit.MPH.toString());
        assertEquals("km/h", SpeedUnit.KM_H.toString());
    }
}
