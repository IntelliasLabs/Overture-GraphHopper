package com.graphhopper.reader.overture.access.restriction.scope.containers;

/**
 * Describes the reason for traveling used to qualify an access rule.
 */
public enum TravelReason {
    /** Traveling as a customer. */
    AS_CUSTOMER,
    /** Traveling to reach a destination. */
    AT_DESTINATION,
    /** Traveling to deliver goods. */
    TO_DELIVER,
    /** Traveling to a farm. */
    TO_FARM,
    /** Traveling for forestry purposes. */
    FOR_FORESTRY;

    /**
     * Parses a {@link TravelReason} from a case-insensitive string.
     *
     * @param s the string to parse, may be {@code null}
     * @return the matching {@link TravelReason}, or {@code null} if the input is {@code null} or
     * does not match any constant
     */
    public static TravelReason fromString(String s) {
        if (s == null) return null;
        try {
            return valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Returns the lower-case string representation of this travel reason.
     *
     * @return the lower-case {@link #name()} of this reason
     */
    @Override
    public String toString() {

        return this.name().toLowerCase();
    }
}
