package com.graphhopper.reader.overture.road.segment;

/**
 * Refines the expected usage of the segment.
 * <p>
 * Subclasses provide more specific categorization than the broad {@link OvertureRoadClass}
 * and must not overlap.
 * </p>
 */
public enum OvertureRoadSubclass {
    /**
     * Connecting stretch (sliproad or ramp) between two road types.
     */
    LINK,

    /**
     * Footway that lies along a road.
     */
    SIDEWALK,

    /**
     * Footway that intersects other roads.
     */
    CROSSWALK,

    /**
     * Service road intended for parking.
     */
    PARKING_AISLE,

    /**
     * Service road intended for deliveries.
     */
    DRIVEWAY,

    /**
     * Service road intended for rear entrances, fire exits.
     */
    ALLEY,

    /**
     * Cycleway that intersects with other roads.
     */
    CYCLE_CROSSING;

    /**
     * Case-insensitive mapping from string to Enum.
     *
     * @param value the string value from the Overture data.
     * @return the corresponding Enum constant, or {@code null} if the input is null (as subclass is optional).
     */
    public static OvertureRoadSubclass fromString(String value) {
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
