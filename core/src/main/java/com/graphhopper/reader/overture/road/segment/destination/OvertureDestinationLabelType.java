package com.graphhopper.reader.overture.road.segment.destination;

/**
 * The type of object of the destination label.
 */
public enum OvertureDestinationLabelType {
    /** The label represents a specific street or road name. */
    STREET,
    /** The label refers to a country or national territory. */
    COUNTRY,
    /** A reference number for a route (e.g., highway number). */
    ROUTE_REF,
    /** Indicates a destination that is reached via a specific route reference. */
    TOWARD_ROUTE_REF,
    /** Fallback type when the label category cannot be identified. */
    UNKNOWN;

    /**
     * Case-insensitive mapping from string to Enum.
     *
     * @param value the string value from the Overture data.
     * @return the corresponding Enum constant.
     * @throws IllegalArgumentException if the value is null or unknown.
     */
    public static OvertureDestinationLabelType fromString(String value) {
        if (value == null) {
            return null;
        }
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
