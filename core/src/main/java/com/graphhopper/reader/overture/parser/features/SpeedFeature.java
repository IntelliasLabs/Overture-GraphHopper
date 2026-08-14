package com.graphhopper.reader.overture.parser.features;

/**
 * Features describing speed properties within the Overture Maps schema.
 * <p>
 * Used for accessing fields inside the {@code speed} object (e.g. within speed limits).
 */
public enum SpeedFeature implements FeatureParser {
    /*v*/ VALUE,
    /*v*/ UNIT;

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
        return true;
    }

    @Override
    public boolean isArray() {
        return false;
    }
}
