package com.graphhopper.reader.overture.names;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class VariantTest {

    @Test
    void testFromString() {
        assertEquals(Variant.COMMON, Variant.fromString("common"));
        assertEquals(Variant.OFFICIAL, Variant.fromString("official"));
        assertEquals(Variant.ALTERNATE, Variant.fromString("alternate"));
        assertEquals(Variant.SHORT, Variant.fromString("short"));
    }

    @Test
    void testFromStringCaseInsensitive() {
        assertEquals(Variant.COMMON, Variant.fromString("COMMON"));
        assertEquals(Variant.OFFICIAL, Variant.fromString("Official"));
        assertEquals(Variant.ALTERNATE, Variant.fromString("ALTERNATE"));
        assertEquals(Variant.SHORT, Variant.fromString("SHORT"));
    }

    @Test
    void testFromStringNull() {
        assertNull(Variant.fromString(null));
    }

    @Test
    void testFromStringInvalid() {
        assertNull(Variant.fromString("unknown"));
        assertNull(Variant.fromString(""));
        assertNull(Variant.fromString("invalid"));
    }

    @Test
    void testEnumValues() {
        assertEquals(4, Variant.values().length);
        assertNotNull(Variant.valueOf("COMMON"));
        assertNotNull(Variant.valueOf("OFFICIAL"));
        assertNotNull(Variant.valueOf("ALTERNATE"));
        assertNotNull(Variant.valueOf("SHORT"));
    }
}
