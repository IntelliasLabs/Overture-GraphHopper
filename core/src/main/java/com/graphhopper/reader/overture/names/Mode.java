package com.graphhopper.reader.overture.names;

/**
 * Whether the perspective holder accepts or disputes this name.
 */
public enum Mode {
    /** The name is officially accepted by the perspective holder. */
    ACCEPTED_BY,
    /** The name is disputed or not recognized by the perspective holder. */
    DISPUTED_BY;

    /**
     * Parses a {@link Mode} from a case-insensitive string.
     *
     * @param s the string to parse, may be {@code null}
     * @return the matching {@link Mode}, or {@code null} if the input is {@code null} or
     * does not match any constant
     */
    public static Mode fromString(String s) {
        if (s == null) return null;
        try {
            return valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Returns the lower-case string representation of this mode.
     *
     * @return the lower-case {@link #name()} of this mode
     */
    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
