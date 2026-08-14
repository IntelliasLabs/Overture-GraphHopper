package com.graphhopper.reader.overture.access.restriction.scope.containers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TravelReason} conversion helpers.
 */
class TravelReasonTest {

    /**
     * Verifies that {@link TravelReason#fromString(String)} parses valid values case-insensitively.
     */
    @Test
    void fromStringParsesValidValuesCaseInsensitive() {
        assertEquals(TravelReason.AS_CUSTOMER, TravelReason.fromString("as_customer"));
        assertEquals(TravelReason.TO_FARM, TravelReason.fromString("TO_FARM"));
    }

    /**
     * Verifies that {@link TravelReason#fromString(String)} returns {@code null} for {@code null}
     * or unknown values.
     */
    @Test
    void fromStringReturnsNullForNullOrUnknown() {
        assertNull(TravelReason.fromString(null));
        assertNull(TravelReason.fromString("unknown"));
    }

    /**
     * Verifies that {@link TravelReason#toString()} returns lower-case names for enum constants.
     */
    @Test
    void toStringProducesLowerCase() {
        assertEquals("as_customer", TravelReason.AS_CUSTOMER.toString());
        assertEquals("to_farm", TravelReason.TO_FARM.toString());
    }
}
