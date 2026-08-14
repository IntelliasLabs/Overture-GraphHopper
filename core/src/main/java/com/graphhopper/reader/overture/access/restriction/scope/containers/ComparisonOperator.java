package com.graphhopper.reader.overture.access.restriction.scope.containers;

/**
 * Comparison operators used when evaluating a numeric vehicle attribute against a threshold.
 */
public enum ComparisonOperator {
    /**
     * Strictly greater than the threshold.
     */
    GREATER_THAN,
    /**
     * Greater than or equal to the threshold.
     */
    GREATER_THAN_EQUAL,
    /**
     * Equal to the threshold.
     */
    EQUAL,
    /**
     * Strictly less than the threshold.
     */
    LESS_THAN,
    /**
     * Less than or equal to the threshold.
     */
    LESS_THAN_EQUAL;

    /**
     * Parses a {@link ComparisonOperator} from a case-insensitive string.
     *
     * @param s the string to parse, may be {@code null}
     * @return the matching {@link ComparisonOperator}, or {@code null} if the input is
     * {@code null} or does not match any constant
     */
    public static ComparisonOperator fromString(String s) {
        if (s == null) return null;
        try {
            return valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Returns the lower-case string representation of this comparison operator.
     *
     * @return the lower-case {@link #name()} of this operator
     */
    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
