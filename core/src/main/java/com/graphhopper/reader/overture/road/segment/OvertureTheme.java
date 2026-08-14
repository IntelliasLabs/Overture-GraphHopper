package com.graphhopper.reader.overture.road.segment;

/**
 * Top-level Overture theme this feature belongs to.
 * <p>
 * Overture Maps data is organized into themes, which act as the highest level of categorization.
 * </p>
 */
public enum OvertureTheme {
    /**
     * Data related to street addresses and postal information.
     */
    ADDRESSES,

    /**
     * Foundational map layers, including land, water, and land cover/use.
     */
    BASE,

    /**
     * Building footprints and related 3D attributes.
     */
    BUILDINGS,

    /**
     * Administrative boundaries and regions (countries, states, cities).
     */
    DIVISIONS,

    /**
     * Points of interest (POIs) such as businesses, landmarks, and public services.
     */
    PLACES,

    /**
     * Transportation network data, including road, rail, and water segments.
     */
    TRANSPORTATION;

    /**
     * Case-insensitive mapping from string to Enum.
     *
     * @param value the string value from the Overture data.
     * @return the corresponding Enum constant.
     * @throws IllegalArgumentException if the value is null or unknown.
     */
    public static OvertureTheme fromString(String value) {
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
     * @return lowercase name of the enum constant.
     */
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
