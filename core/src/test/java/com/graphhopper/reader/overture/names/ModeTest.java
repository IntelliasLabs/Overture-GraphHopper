package com.graphhopper.reader.overture.names;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ModeTest {

    @Test
    void testFromString_ValidExactMatch() {
        assertEquals(Mode.ACCEPTED_BY, Mode.fromString("ACCEPTED_BY"));
        assertEquals(Mode.DISPUTED_BY, Mode.fromString("DISPUTED_BY"));
    }

    @Test
    void testFromString_ValidCaseInsensitive() {
        assertEquals(Mode.ACCEPTED_BY, Mode.fromString("accepted_by"));
        assertEquals(Mode.DISPUTED_BY, Mode.fromString("disputed_by"));
        assertEquals(Mode.ACCEPTED_BY, Mode.fromString("Accepted_By"));
    }

    @Test
    void testFromString_InvalidString() {
        assertNull(Mode.fromString("UNKNOWN_MODE"));
        assertNull(Mode.fromString("random_text"));
    }

    @Test
    void testFromString_EmptyString() {
        assertNull(Mode.fromString(""));
        assertNull(Mode.fromString("   "));
    }

    @Test
    void testFromString_Null() {
        assertNull(Mode.fromString(null));
    }
}
