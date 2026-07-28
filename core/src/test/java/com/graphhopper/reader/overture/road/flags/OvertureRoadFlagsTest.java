package com.graphhopper.reader.overture.road.flags;

import static org.junit.jupiter.api.Assertions.*;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import org.junit.jupiter.api.Test;

public class OvertureRoadFlagsTest {

    @Test
    public void testAllFlagsTrueWithRange() {
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.0, 0.5);
        OvertureRoadFlags flags = new OvertureRoadFlags(true, true, true, true, true, true, range);

        assertTrue(flags.isBridge());
        assertTrue(flags.isTunnel());
        assertTrue(flags.isUnderConstruction());
        assertTrue(flags.isAbandoned());
        assertTrue(flags.isCovered());
        assertTrue(flags.isIndoor());

        assertNotNull(flags.getBetween());
        assertEquals(0.0, flags.getBetween().getStart());
        assertEquals(0.5, flags.getBetween().getEnd());
    }

    @Test
    public void testAllFlagsFalse() {
        OvertureRoadFlags flags = new OvertureRoadFlags(false, false, false, false, false, false, null);

        assertFalse(flags.isBridge());
        assertFalse(flags.isTunnel());
        assertFalse(flags.isUnderConstruction());
        assertFalse(flags.isAbandoned());
        assertFalse(flags.isCovered());
        assertFalse(flags.isIndoor());
        assertFalse(flags.shouldSkip());
        assertNull(flags.getBetween());
    }

    @Test
    public void testShouldSkipForAbandoned() {
        OvertureRoadFlags abandoned =
                new OvertureRoadFlags(false, false, false, true, false, false, null);
        assertTrue(abandoned.isAbandoned());
        assertTrue(abandoned.shouldSkip());
    }

    @Test
    public void testShouldSkipForUnderConstruction() {
        OvertureRoadFlags construction =
                new OvertureRoadFlags(false, false, true, false, false, false, null);
        assertTrue(construction.isUnderConstruction());
        assertTrue(construction.shouldSkip());
    }

    @Test
    public void testEqualsAndHashCode() {
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.1, 0.2);
        OvertureRoadFlags flags1 =
                new OvertureRoadFlags(true, false, false, false, false, false, range);
        OvertureRoadFlags flags2 = new OvertureRoadFlags(
                true, false, false, false, false, false, new LinearlyReferencedRange(0.1, 0.2));
        OvertureRoadFlags flags3 =
                new OvertureRoadFlags(false, true, false, false, false, false, range);

        assertEquals(flags1, flags2);
        assertEquals(flags1.hashCode(), flags2.hashCode());
        assertNotEquals(flags1, flags3);
    }
}
