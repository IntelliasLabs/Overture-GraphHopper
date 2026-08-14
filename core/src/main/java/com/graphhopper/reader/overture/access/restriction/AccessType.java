package com.graphhopper.reader.overture.access.restriction;

/**
 * Describes the high-level access decision for a restricted segment.
 */
public enum AccessType {
    /** Access is explicitly allowed. */
    ALLOWED,
    /** Access is explicitly denied. */
    DENIED,
    /** Access is designated for this mode (e.g. preferred route). */
    DESIGNATED;

    /**
     * Parses an {@link AccessType} from a case-insensitive string.
     *
     * @param s the string to parse, may be {@code null}
     * @return the matching {@link AccessType}, or {@code null} if the input is {@code null} or
     * does not match any constant
     */
    public static AccessType fromString(String s) {
        if (s == null) return null;
        try {
            return valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Returns the lower-case string representation of this access type.
     *
     * @return the lower-case {@link #name()} of this access type
     */
    @Override
    public String toString() {

        return this.name().toLowerCase();
    }
}
