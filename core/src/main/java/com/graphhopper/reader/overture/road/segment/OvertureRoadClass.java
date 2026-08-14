package com.graphhopper.reader.overture.road.segment;

/**
 * Captures the kind of road and its position in the road network hierarchy.
 * <p>
 * This classification helps determine the importance and physical characteristics of the road
 * for routing and rendering purposes.
 * </p>
 */
public enum OvertureRoadClass {
    /**
     * High capacity highways designed to safely carry high traffic volumes at high speeds.
     */
    MOTORWAY,

    /**
     * Major highways linking large towns and cities.
     */
    PRIMARY,

    /**
     * Highways linking large towns to smaller towns.
     */
    SECONDARY,

    /**
     * Roads connecting smaller settlements and local centers.
     */
    TERTIARY,

    /**
     * Roads that serve as access to housing, without the function of connecting settlements.
     */
    RESIDENTIAL,

    /**
     * Similar to residential but has implied legal restriction for motor vehicles
     * (which can vary country by country).
     */
    LIVING_STREET,

    /**
     * Important roads that aren't motorways but provide high-speed connections.
     */
    TRUNK,

    /**
     * Known roads, paved, but subordinate to all of: motorway, trunk, primary, secondary, tertiary.
     */
    UNCLASSIFIED,

    /**
     * Provides vehicle access to a feature (such as a building), typically not part of the public street network.
     */
    SERVICE,

    /**
     * Roads primarily for pedestrians, often in shopping areas or plazas.
     */
    PEDESTRIAN,

    /**
     * Minor segments mainly used by pedestrians.
     */
    FOOTWAY,

    /**
     * Flights of steps on footways.
     */
    STEPS,

    /**
     * A generic path, often unpaved, that may be used by pedestrians, cyclists, or others.
     */
    PATH,

    /**
     * Roads for agricultural or forestry use.
     */
    TRACK,

    /**
     * Paths designated for bicycles.
     */
    CYCLEWAY,

    /**
     * Similar to track but has implied access only for horses.
     */
    BRIDLEWAY,

    /**
     * The road class is unknown or not specified.
     */
    UNKNOWN;

    /**
     * Case-insensitive mapping from string to Enum.
     *
     * @param value the string value from the Overture data.
     * @return the corresponding Enum constant.
     */
    public static OvertureRoadClass fromString(String value) {
        if (value == null) {
            return null;
        }
        try {
            return OvertureRoadClass.valueOf(value.toUpperCase());
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

    /**
     * Checks if this road class is accessible for cars.
     *
     * @return {@code true} if the class is accessible for cars and {@code false} if not.
     */
    public boolean isCarAccessible() {
        return this != PEDESTRIAN
                && this != FOOTWAY
                && this != STEPS
                && this != PATH
                && this != CYCLEWAY
                && this != BRIDLEWAY
                && this != UNKNOWN;
    }
}
