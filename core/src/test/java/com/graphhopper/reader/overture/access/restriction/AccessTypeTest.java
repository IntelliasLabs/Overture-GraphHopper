package com.graphhopper.reader.overture.access.restriction;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AccessType} conversion helpers.
 */
class AccessTypeTest {

    /**
     * Verifies that {@link AccessType#fromString(String)} parses valid values case-insensitively.
     */
    @Test
    void fromStringParsesValidValuesCaseInsensitive() {
        assertEquals(AccessType.ALLOWED, AccessType.fromString("allowed"));
        assertEquals(AccessType.DENIED, AccessType.fromString("DENIED"));
        assertEquals(AccessType.DESIGNATED, AccessType.fromString("DeSiGnAtEd"));
    }

    /**
     * Verifies that {@link AccessType#fromString(String)} returns {@code null} for {@code null}
     * or unknown values.
     */
    @Test
    void fromStringReturnsNullForNullOrUnknown() {
        assertNull(AccessType.fromString(null));
        assertNull(AccessType.fromString("unknown"));
    }

    /**
     * Verifies that {@link AccessType#toString()} returns lower-case names for enum constants.
     */
    @Test
    void toStringProducesLowerCase() {
        assertEquals("allowed", AccessType.ALLOWED.toString());
        assertEquals("denied", AccessType.DENIED.toString());
        assertEquals("designated", AccessType.DESIGNATED.toString());
    }
}
