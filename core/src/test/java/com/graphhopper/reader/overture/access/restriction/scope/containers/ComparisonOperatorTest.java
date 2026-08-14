package com.graphhopper.reader.overture.access.restriction.scope.containers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ComparisonOperator} conversion helpers.
 */
class ComparisonOperatorTest {

    /**
     * Verifies that {@link ComparisonOperator#fromString(String)} parses valid values
     * case-insensitively.
     */
    @Test
    void fromStringParsesValidValuesCaseInsensitive() {
        assertEquals(ComparisonOperator.GREATER_THAN, ComparisonOperator.fromString("greater_than"));
        assertEquals(ComparisonOperator.LESS_THAN_EQUAL, ComparisonOperator.fromString("LESS_THAN_EQUAL"));
    }

    /**
     * Verifies that {@link ComparisonOperator#fromString(String)} returns {@code null} for
     * {@code null} or unknown values.
     */
    @Test
    void fromStringReturnsNullForNullOrUnknown() {
        assertNull(ComparisonOperator.fromString(null));
        assertNull(ComparisonOperator.fromString("unknown"));
    }

    /**
     * Verifies that {@link ComparisonOperator#toString()} returns lower-case names for enum constants.
     */
    @Test
    void toStringProducesLowerCase() {
        assertEquals("greater_than", ComparisonOperator.GREATER_THAN.toString());
        assertEquals("less_than_equal", ComparisonOperator.LESS_THAN_EQUAL.toString());
    }
}
