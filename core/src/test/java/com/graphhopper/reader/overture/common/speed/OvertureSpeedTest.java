package com.graphhopper.reader.overture.common.speed;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for {@link OvertureSpeed}.
 */
class OvertureSpeedTest {
    @Test
    void testConstructorAndGetters() {
        OvertureSpeed speed = new OvertureSpeed(50.0, SpeedUnit.KM_H);

        assertEquals(50.0, speed.getValue());
        assertEquals(SpeedUnit.KM_H, speed.getUnit());
    }

    @Test
    void testConstructorWithNulls() {
        OvertureSpeed speed = new OvertureSpeed(null, null);

        assertNull(speed.getValue());
        assertNull(speed.getUnit());
    }

    @Test
    void testIsValidWithStandardValues() {
        assertTrue(new OvertureSpeed(50.0, SpeedUnit.KM_H).isValid(), "50.0 should be valid");
        assertTrue(new OvertureSpeed(100.0, SpeedUnit.MPH).isValid(), "100.0 should be valid");
    }

    @Test
    void testIsValidBoundaries() {
        assertTrue(
                new OvertureSpeed(1.0, SpeedUnit.KM_H).isValid(), "1.0 should be valid (min boundary)");

        assertTrue(
                new OvertureSpeed(350.0, SpeedUnit.KM_H).isValid(), "350.0 should be valid (max boundary)");
    }

    @ParameterizedTest(name = "Should be invalid: {0}")
    @ValueSource(doubles = {0.0, 0.99, -10.0, 350.1, 500.0})
    void testIsInvalidOutsideBoundaries(double invalidValue) {
        OvertureSpeed speed = new OvertureSpeed(invalidValue, SpeedUnit.KM_H);
        assertFalse(speed.isValid(), "Value " + invalidValue + " should be considered invalid");
    }

    @Test
    void testIsValidReturnsFalseForNull() {
        OvertureSpeed speed = new OvertureSpeed(null, SpeedUnit.KM_H);
        assertFalse(speed.isValid(), "Null value should be invalid");
    }

    @Test
    void testEqualsAndHashCode() {
        OvertureSpeed s1 = new OvertureSpeed(60.0, SpeedUnit.KM_H);
        OvertureSpeed s2 = new OvertureSpeed(60.0, SpeedUnit.KM_H); // / Same as s1
        OvertureSpeed s3 = new OvertureSpeed(60.0, SpeedUnit.MPH); // / Diff unit
        OvertureSpeed s4 = new OvertureSpeed(61.0, SpeedUnit.KM_H); // / Diff value

        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());

        assertNotEquals(s1, s3);
        assertNotEquals(s1, s4);
        assertNotNull(s1);
        assertNotEquals(new Object(), s1);
    }

    @Test
    void testToString() {
        OvertureSpeed speed = new OvertureSpeed(120.0, SpeedUnit.KM_H);
        String str = speed.toString();

        assertTrue(str.contains("120.0"));
        assertTrue(str.contains("km/h"));
    }
}
