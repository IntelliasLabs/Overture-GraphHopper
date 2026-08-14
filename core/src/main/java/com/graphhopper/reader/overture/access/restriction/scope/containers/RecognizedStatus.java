package com.graphhopper.reader.overture.access.restriction.scope.containers;

/**
 * Describes the recognized status of the traveler used to qualify an access rule.
 */
public enum RecognizedStatus {
    /** Access applies as for permitted users. */
    AS_PERMITTED,
    /** Access applies as for private users or owners. */
    AS_PRIVATE,
    /** Access applies as for disabled users. */
    AS_DISABLED,
    /** Access applies as for employees. */
    AS_EMPLOYEE,
    /** Access applies as for students. */
    AS_STUDENT;

    /**
     * Parses a {@link RecognizedStatus} from a case-insensitive string.
     *
     * @param s the string to parse, may be {@code null}
     * @return the matching {@link RecognizedStatus}, or {@code null} if the input is {@code null}
     * or does not match any constant
     */
    public static RecognizedStatus fromString(String s) {
        if (s == null) return null;
        try {
            return valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Returns the lower-case string representation of this recognized status.
     *
     * @return the lower-case {@link #name()} of this status
     */
    @Override
    public String toString() {

        return this.name().toLowerCase();
    }
}
