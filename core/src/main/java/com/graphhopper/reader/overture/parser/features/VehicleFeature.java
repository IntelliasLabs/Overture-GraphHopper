package com.graphhopper.reader.overture.parser.features;

/**
 * Features describing a vehicle constraint object used in Overture rules.
 * <p>
 * This enum is used to access fields inside vehicle-related JSON objects (e.g. under
 * {@code when.vehicle} blocks) via the {@link FeatureFinder} contract.
 */
public enum VehicleFeature implements FeatureParser {
    /*-- Vehicle feature properties --*/
    /*v*/ DIMENSION(true),
    /*v*/ COMPARISON(true),
    /*v*/ VALUE(true),
    /*v*/ UNIT(true);

    private final boolean isRequired;

    VehicleFeature(boolean isRequired) {
        this.isRequired = isRequired;
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
        return false;
    }
}
