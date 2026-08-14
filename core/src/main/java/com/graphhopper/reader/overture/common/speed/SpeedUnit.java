package com.graphhopper.reader.overture.common.speed;

import org.jetbrains.annotations.Nullable;

/**
 * Enumerates the speed units used in Overture Maps.
 */
public enum SpeedUnit {
    /**Kilometers per hour. */
    KM_H("km/h"),
    /** Miles per hour.*/
    MPH("mph");

    private final String value;

    /**
     * Internal constructor for speed unit constants.
     *
     * @param value the raw string value representing the unit
     */
    SpeedUnit(String value) {
        this.value = value;
    }

    /**
     * Parses a string representation of the unit (e.g. "km/h", "mph").
     * strict string matching.
     *
     * @param text the raw string from JSON
     * @return the corresponding Unit, or null if text is null/unknown.
     */
    @Nullable public static SpeedUnit fromString(@Nullable String text) {
        if (text == null) return null;
        for (SpeedUnit unit : SpeedUnit.values()) {
            if (unit.value.equals(text)) {
                return unit;
            }
        }
        return null;
    }

    /**
     * Returns string representation of this unit.
     *
     * @return "km_h" or "mph"
     */
    @Override
    public String toString() {
        return value;
    }

    /**
     * The numeric value of the speed limit.
     *
     * @return the raw string value of this speed unit
     */
    public String getValue() {
        return value;
    }
}
