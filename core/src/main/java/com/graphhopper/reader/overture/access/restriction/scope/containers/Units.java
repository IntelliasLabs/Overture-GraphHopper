package com.graphhopper.reader.overture.access.restriction.scope.containers;

/**
 * Enumerates supported length and weight units for vehicle dimension constraints.
 */
public enum Units {
    /** Inches. */
    IN,
    /** Feet. */
    FT,
    /** Yards. */
    YD,
    /** Miles. */
    MI,
    /** Centimetres. */
    CM,
    /** Metres. */
    M,
    /** Kilometres. */
    KM,
    /** Ounces. */
    OZ,
    /** Pounds. */
    LB,
    /** Stones. */
    ST,
    /** Long tons. */
    LT,
    /** Grams. */
    G,
    /** Kilograms. */
    KG,
    /** Metric tons. */
    T;

    /**
     * Parses {@link Units} from a case-insensitive string.
     *
     * @param s the string to parse, may be {@code null}
     * @return the matching {@link Units}, or {@code null} if the input is {@code null} or
     * does not match any constant
     */
    public static Units fromString(String s) {
        if (s == null) return null;
        try {
            return valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Returns the lower-case string representation of this unit.
     *
     * @return the lower-case {@link #name()} of this unit
     */
    @Override
    public String toString() {

        return this.name().toLowerCase();
    }
}
