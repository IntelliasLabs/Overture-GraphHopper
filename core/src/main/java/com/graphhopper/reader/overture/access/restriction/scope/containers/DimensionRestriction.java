package com.graphhopper.reader.overture.access.restriction.scope.containers;

/**
 * Identifies which vehicle dimension is constrained by a {@link VehicleAttributes} rule.
 */
public enum DimensionRestriction {
    /** Constraint applies to the number of axles. */
    AXLE_COUNT,
    /** Constraint applies to vehicle height. */
    HEIGHT,
    /** Constraint applies to vehicle length. */
    LENGTH,
    /** Constraint applies to vehicle weight. */
    WEIGHT,
    /** Constraint applies to vehicle width. */
    WIDTH;

    /**
     * Parses a {@link DimensionRestriction} from a case-insensitive string.
     *
     * @param s the string to parse, may be {@code null}
     * @return the matching {@link DimensionRestriction}, or {@code null} if the input is
     * {@code null} or does not match any constant
     */
    public static DimensionRestriction fromString(String s) {
        if (s == null) return null;
        try {
            return valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Returns the lower-case string representation of this dimension restriction.
     *
     * @return the lower-case {@link #name()} of this restriction
     */
    @Override
    public String toString() {

        return this.name().toLowerCase();
    }
}
