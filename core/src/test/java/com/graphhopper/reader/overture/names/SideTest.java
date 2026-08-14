package com.graphhopper.reader.overture.names;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SideTest {

    @Test
    void testFromString() {
        assertEquals(Side.LEFT, Side.fromString("left"));
        assertEquals(Side.RIGHT, Side.fromString("right"));
    }

    @Test
    void testFromStringCaseInsensitive() {
        assertEquals(Side.LEFT, Side.fromString("LEFT"));
        assertEquals(Side.LEFT, Side.fromString("Left"));
        assertEquals(Side.RIGHT, Side.fromString("RIGHT"));
        assertEquals(Side.RIGHT, Side.fromString("Right"));
    }

    @Test
    void testFromStringNull() {
        assertNull(Side.fromString(null));
    }

    @Test
    void testFromStringInvalid() {
        assertNull(Side.fromString("center"));
        assertNull(Side.fromString(""));
        assertNull(Side.fromString("both"));
    }

    @Test
    void testEnumValues() {
        assertEquals(2, Side.values().length);
        assertNotNull(Side.valueOf("LEFT"));
        assertNotNull(Side.valueOf("RIGHT"));
    }
}
