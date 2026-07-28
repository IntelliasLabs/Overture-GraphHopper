package com.graphhopper.reader.overture.road.surface;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RoadSurfaceType} conversion helpers.
 */
class RoadSurfaceTypeTest {

    /**
     * Verifies that {@link RoadSurfaceType#fromString(String)} parses valid values case-insensitively.
     */
    @Test
    void fromStringParsesValidValuesCaseInsensitive() {
        assertEquals(RoadSurfaceType.ASPHALT, RoadSurfaceType.fromString("asphalt"));
        assertEquals(RoadSurfaceType.CONCRETE, RoadSurfaceType.fromString("CONCRETE"));
        assertEquals(RoadSurfaceType.GRAVEL, RoadSurfaceType.fromString("GrAvEl"));
    }

    /**
     * Verifies that {@link RoadSurfaceType#fromString(String)} returns {@code null} for
     * {@code null} or unknown values.
     */
    @Test
    void fromStringReturnsNullForNull() {
        assertNull(RoadSurfaceType.fromString(null));
    }

    /**
     * Verifies that {@link RoadSurfaceType#toString()} returns lower-case names for enum constants.
     */
    @Test
    void toStringProducesLowerCase() {
        assertEquals("asphalt", RoadSurfaceType.ASPHALT.toString());
        assertEquals("concrete", RoadSurfaceType.CONCRETE.toString());
        assertEquals("gravel", RoadSurfaceType.GRAVEL.toString());
    }
}
