package com.graphhopper.reader.overture.access.restriction.scope.containers;

/**
 * Enumerates Overture travel modes used when evaluating access restrictions.
 */
public enum TravelMode {
    /** Any vehicle. */
    VEHICLE,
    /** Any motor vehicle. */
    MOTOR_VEHICLE,
    /** Passenger car. */
    CAR,
    /** Truck or lorry. */
    TRUCK,
    /** Motorcycle. */
    MOTORCYCLE,
    /** Pedestrian. */
    FOOT,
    /** Bicycle. */
    BICYCLE,
    /** Bus. */
    BUS,
    /** Heavy goods vehicle. */
    HGV,
    /** High-occupancy vehicle. */
    HOV,
    /** Emergency vehicle. */
    EMERGENCY;

    /**
     * Parses a {@link TravelMode} from a case-insensitive string.
     *
     * @param s the string to parse, may be {@code null}
     * @return the matching {@link TravelMode}, or {@code null} if the input is {@code null} or
     * does not match any constant
     */
    public static TravelMode fromString(String s) {
        if (s == null) return null;
        try {
            return valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Returns the lower-case string representation of this travel mode.
     *
     * @return the lower-case {@link #name()} of this mode
     */
    @Override
    public String toString() {

        return this.name().toLowerCase();
    }
}
