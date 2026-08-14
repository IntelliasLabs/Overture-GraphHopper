package com.graphhopper.reader.overture.road.segment;

/**
 * Subtype of an Overture segment feature.
 */
public enum OvertureSegmentSubtype {
    /** Segments designed for vehicular, pedestrian, or bicycle traffic. */
    ROAD,
    /** Infrastructure dedicated to train or light rail transport. */
    RAIL,
    /** Water-based routes, typically used for ferry transport segments. */
    WATER;

    /**
     * Case-insensitive mapping from string to Enum.
     *
     * @param value the string value from the Overture data.
     * @return the corresponding Enum constant, or {@code null} if the input is null (as subclass is optional).
     */
    public static OvertureSegmentSubtype fromString(String value) {
        if (value == null) {
            return null;
        }
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Returns the lowercase string representation of the enum constant.
     *
     * @return the lowercase name of the enum constant.
     */
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
