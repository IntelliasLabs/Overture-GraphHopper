package com.graphhopper.reader.overture.names;

/**
 * Represents the side on which something appears relative to a facing or heading direction.
 * <p>
 * This is a geometric scoping property defining the side of a road modeled when moving
 * along the line from beginning to end (the feature's digitized direction).
 * <p>
 * Common contexts include:
 * <ul>
 * <li>The side of a road relative to the road orientation.</li>
 * <li>Relative to the direction of travel of a person or vehicle.</li>
 * </ul>
 */
public enum Side {
    /** The left side relative to the direction of travel. */
    LEFT,
    /** The right side relative to the direction of travel. */
    RIGHT;

    /**
     * Parses a string into a Side enum.
     *
     * @param s the string to parse (case-insensitive)
     * @return the matching Side, or null if input is null or invalid
     */
    public static Side fromString(String s) {
        if (s == null) return null;
        try {
            return valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Returns the lower-case string representation of this side.
     *
     * @return the lower-case name of this side
     */
    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
