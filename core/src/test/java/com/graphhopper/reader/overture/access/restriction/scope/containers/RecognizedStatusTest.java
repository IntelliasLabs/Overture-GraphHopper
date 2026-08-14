package com.graphhopper.reader.overture.access.restriction.scope.containers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RecognizedStatus} conversion helpers.
 */
class RecognizedStatusTest {

    /**
     * Verifies that {@link RecognizedStatus#fromString(String)} parses valid values
     * case-insensitively.
     */
    @Test
    void fromStringParsesValidValuesCaseInsensitive() {
        assertEquals(RecognizedStatus.AS_PERMITTED, RecognizedStatus.fromString("as_permitted"));
        assertEquals(RecognizedStatus.AS_EMPLOYEE, RecognizedStatus.fromString("AS_EMPLOYEE"));
    }

    /**
     * Verifies that {@link RecognizedStatus#fromString(String)} returns {@code null} for
     * {@code null} or unknown values.
     */
    @Test
    void fromStringReturnsNullForNullOrUnknown() {
        assertNull(RecognizedStatus.fromString(null));
        assertNull(RecognizedStatus.fromString("unknown"));
    }

    /**
     * Verifies that {@link RecognizedStatus#toString()} returns lower-case names for enum constants.
     */
    @Test
    void toStringProducesLowerCase() {
        assertEquals("as_permitted", RecognizedStatus.AS_PERMITTED.toString());
        assertEquals("as_employee", RecognizedStatus.AS_EMPLOYEE.toString());
    }
}
