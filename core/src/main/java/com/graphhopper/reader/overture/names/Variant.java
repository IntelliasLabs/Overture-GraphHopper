package com.graphhopper.reader.overture.names;

/**
 * The type of name variant.
 * <p>
 * This classification allows distinguishing between the standard usage name, official government names,
 * alternate spellings or synonyms, and abbreviations.
 */
public enum Variant {
    /** The most common or default name for the feature. */
    COMMON,
    /** The official name as recognized by a government or authority. */
    OFFICIAL,
    /** An alternative name, such as a synonym or local alias. */
    ALTERNATE,
    /** A shortened version or abbreviation of the name. */
    SHORT;

    /**
     * Parses a string into a Variant enum.
     *
     * @param s the string to parse (case-insensitive)
     * @return the matching Variant, or null if input is null or invalid
     */
    public static Variant fromString(String s) {
        if (s == null) return null;
        try {
            return valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Returns the lowercase string representation of the Variant.
     *
     * @return the lowercase name of the variant
     */
    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
