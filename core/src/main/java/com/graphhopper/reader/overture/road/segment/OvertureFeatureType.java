package com.graphhopper.reader.overture.road.segment;

/**
 * Specific feature type within the theme.
 * <p>
 * Enumerates the possible types of features found in Overture Maps data layers.
 * </p>
 */
public enum OvertureFeatureType {
    /** Postal or physical address point. */
    ADDRESS,
    /** Depth or underwater topography data. */
    BATHYMETRY,
    /** Physical building structure. */
    BUILDING,
    /** Topological connection point between segments. */
    CONNECTOR,
    /** Administrative or political division. */
    DIVISION,
    /** Polygonal area of an administrative division. */
    DIVISION_AREA,
    /** Line representing a political or administrative boundary. */
    DIVISION_BOUNDARY,
    /** Man-made physical infrastructure elements. */
    INFRASTRUCTURE,
    /** General land mass features. */
    LAND,
    /** Classification of physical material on the earth's surface. */
    LAND_COVER,
    /** Human-defined use of land (e.g., industrial, residential). */
    LAND_USE,
    /** Specific component or wing of a larger building. */
    BUILDING_PART,
    /** Named location or point of interest (POI). */
    PLACE,
    /** Linear transportation element, such as a road or track. */
    SEGMENT,
    /** Hydrological features including rivers, lakes, and oceans. */
    WATER;

    /**
     * Case-insensitive mapping from string to Enum.
     *
     * @param value the string value from the Overture data.
     * @return the corresponding Enum constant.
     *
     */
    public static OvertureFeatureType fromString(String value) {
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
     * @return the lowercase name of the feature type.
     */
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
