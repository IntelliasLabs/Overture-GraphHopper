package com.graphhopper.reader.overture.parser.features;

/**
 * Features describing the speed limit rule objects attached to a segment.
 * <p>
 * Used for accessing fields inside items of the {@code properties.speed_limits} array.
 */
public enum SpeedLimitsFeature implements FeatureParser {

    /*-- SpeedLimits feature properties --*/

    /*v*/ MIN_SPEED,
    /*v*/ MAX_SPEED(true),
    /*v*/ IS_MAX_SPEED_VARIABLE,
    /*v*/ WHEN,
    /*v*/ BETWEEN(false, true);
    private final boolean isRequired;
    private final boolean isArray;

    SpeedLimitsFeature(boolean isRequired, boolean isArray) {
        this.isRequired = isRequired;
        this.isArray = isArray;
    }

    SpeedLimitsFeature(boolean isRequired) {
        this(isRequired, false);
    }

    SpeedLimitsFeature() {
        this(false);
    }

    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public FeatureFinder getParentFeature() {
        return null;
    }

    @Override
    public String getOtherName() {
        return null;
    }

    @Override
    public boolean isRequired() {
        return isRequired;
    }

    @Override
    public boolean isArray() {
        return isArray;
    }
}
