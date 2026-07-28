package com.graphhopper.reader.overture.road.surface;

import static org.junit.jupiter.api.Assertions.*;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link OvertureRoadSurface} covering constructor behavior, accessors,
 * surface classification helpers and equality/toString.
 */
class OvertureRoadSurfaceTest {

    /**
     * Verifies that the constructor stores the given fields and that the getters return them.
     */
    @Test
    void constructorStoresFieldsAndGettersReturnThem() {
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.1, 0.9);
        OvertureRoadSurface surface = new OvertureRoadSurface(RoadSurfaceType.ASPHALT, range);

        assertEquals(RoadSurfaceType.ASPHALT, surface.getSurfaceType());
        assertSame(range, surface.getBetween());
    }

    /**
     * Verifies that the {@code has*} helper methods reflect presence or absence of
     * surface type and linear range.
     */
    @Test
    void hasSurfaceTypeAndHasBetweenReflectPresenceOfValues() {
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.2, 0.8);
        OvertureRoadSurface withAll = new OvertureRoadSurface(RoadSurfaceType.CONCRETE, range);

        assertTrue(withAll.hasSurfaceType());
        assertTrue(withAll.hasBetween());

        OvertureRoadSurface withNone = new OvertureRoadSurface(null, null);
        assertFalse(withNone.hasSurfaceType());
        assertFalse(withNone.hasBetween());
    }

    /**
     * Verifies that {@link OvertureRoadSurface#isPaved()} returns {@code true} for paved
     * surface types and {@code false} otherwise.
     */
    @Test
    void isPavedReflectsPavedSurfaceTypes() {
        assertTrue(new OvertureRoadSurface(RoadSurfaceType.PAVED, null).isPaved());
        assertTrue(new OvertureRoadSurface(RoadSurfaceType.ASPHALT, null).isPaved());
        assertTrue(new OvertureRoadSurface(RoadSurfaceType.CONCRETE, null).isPaved());
        assertTrue(new OvertureRoadSurface(RoadSurfaceType.PAVING_STONES, null).isPaved());

        assertFalse(new OvertureRoadSurface(RoadSurfaceType.GRAVEL, null).isPaved());
        assertFalse(new OvertureRoadSurface(RoadSurfaceType.DIRT, null).isPaved());
        assertFalse(new OvertureRoadSurface(RoadSurfaceType.UNPAVED, null).isPaved());
        assertFalse(new OvertureRoadSurface(RoadSurfaceType.UNKNOWN, null).isPaved());
    }

    /**
     * Verifies that {@link OvertureRoadSurface#isUnpaved()} returns {@code true} for unpaved
     * surface types and {@code false} otherwise.
     */
    @Test
    void isUnpavedReflectsUnpavedSurfaceTypes() {
        assertTrue(new OvertureRoadSurface(RoadSurfaceType.UNPAVED, null).isUnpaved());
        assertTrue(new OvertureRoadSurface(RoadSurfaceType.GRAVEL, null).isUnpaved());
        assertTrue(new OvertureRoadSurface(RoadSurfaceType.DIRT, null).isUnpaved());

        assertFalse(new OvertureRoadSurface(RoadSurfaceType.PAVED, null).isUnpaved());
        assertFalse(new OvertureRoadSurface(RoadSurfaceType.ASPHALT, null).isUnpaved());
        assertFalse(new OvertureRoadSurface(RoadSurfaceType.CONCRETE, null).isUnpaved());
        assertFalse(new OvertureRoadSurface(RoadSurfaceType.PAVING_STONES, null).isUnpaved());
        assertFalse(new OvertureRoadSurface(RoadSurfaceType.UNKNOWN, null).isUnpaved());
    }

    /**
     * Verifies that {@link OvertureRoadSurface#equals(Object)} and {@link OvertureRoadSurface#hashCode()}
     * use both the surface type and the linear range.
     */
    @Test
    void equalsAndHashCodeUseSurfaceTypeAndRange() {
        LinearlyReferencedRange range1 = new LinearlyReferencedRange(0.0, 0.5);
        LinearlyReferencedRange range2 = new LinearlyReferencedRange(0.5, 1.0);

        OvertureRoadSurface s1 = new OvertureRoadSurface(RoadSurfaceType.GRAVEL, range1);
        OvertureRoadSurface s2 = new OvertureRoadSurface(RoadSurfaceType.GRAVEL, range1);
        OvertureRoadSurface s3 = new OvertureRoadSurface(RoadSurfaceType.GRAVEL, range2);
        OvertureRoadSurface s4 = new OvertureRoadSurface(RoadSurfaceType.DIRT, range1);

        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
        assertNotEquals(s1, s3);
        assertNotEquals(s1, s4);
    }

    /**
     * Checks that the {@code toString()} representation contains the surface type and the
     * optional range.
     */
    @Test
    void toStringContainsSurfaceTypeAndRange() {
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.0, 1.0);
        OvertureRoadSurface surface = new OvertureRoadSurface(RoadSurfaceType.METAL, range);

        String s = surface.toString();
        assertTrue(s.contains("surfaceType=metal"));
        assertTrue(s.contains("between="));
    }
}
