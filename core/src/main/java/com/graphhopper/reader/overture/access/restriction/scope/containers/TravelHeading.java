package com.graphhopper.reader.overture.access.restriction.scope.containers;

/**
 * Describes the travel heading for which an access rule applies.
 */
public enum TravelHeading {
    /** Access applies in the forward direction only. */
    FORWARD,
    /** Access applies in the backward direction only. */
    BACKWARD;

    /**
     * Parses a {@link TravelHeading} from a case-insensitive string.
     *
     * @param s the string to parse, may be {@code null}
     * @return the matching {@link TravelHeading}, or {@code null} if the input is {@code null} or
     * does not match any constant
     */
    public static TravelHeading fromString(String s) {
        if (s == null) return null;
        try {
            return valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Returns the lower-case string representation of this travel heading.
     *
     * @return the lower-case {@link #name()} of this heading
     */
    @Override
    public String toString() {

        return this.name().toLowerCase();
    }
}
