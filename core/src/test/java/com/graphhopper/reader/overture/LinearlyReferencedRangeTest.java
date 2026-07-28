package com.graphhopper.reader.overture;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LinearlyReferencedRangeTest {

    @Test
    void testConstructorAndGetters() {
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.0, 1.0);
        assertEquals(0.0, range.getStart());
        assertEquals(1.0, range.getEnd());
    }

    @Test
    void testPartialRange() {
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.25, 0.75);
        assertEquals(0.25, range.getStart());
        assertEquals(0.75, range.getEnd());
    }

    @Test
    void testEqualsAndHashCode() {
        LinearlyReferencedRange range1 = new LinearlyReferencedRange(0.0, 0.5);
        LinearlyReferencedRange range2 = new LinearlyReferencedRange(0.0, 0.5);
        LinearlyReferencedRange range3 = new LinearlyReferencedRange(0.0, 1.0);
        LinearlyReferencedRange range4 = new LinearlyReferencedRange(0.1, 0.5);

        assertEquals(range1, range2);
        assertEquals(range1.hashCode(), range2.hashCode());
        assertNotEquals(range1, range3);
        assertNotEquals(range1, range4);
    }

    @Test
    void testEqualsSameInstance() {
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.0, 1.0);
        assertEquals(range, range);
    }

    @Test
    void testEqualsNull() {
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.0, 1.0);
        assertNotEquals(null, range);
    }

    @Test
    void testEqualsDifferentType() {
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.0, 1.0);
        assertNotEquals("not a range", range);
    }

    @Test
    void testToString() {
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.25, 0.75);
        String str = range.toString();
        assertTrue(str.contains("0.25"));
        assertTrue(str.contains("0.75"));
        assertTrue(str.contains("LinearlyReferencedRange"));
    }
}
