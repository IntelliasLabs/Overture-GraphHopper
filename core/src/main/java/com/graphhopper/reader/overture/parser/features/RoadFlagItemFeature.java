package com.graphhopper.reader.overture.parser.features;

/**
 * Features describing road flags.
 * <p>
 * Used for extracting fields inside item of the {@code road_flags.items} array.
 */
public enum RoadFlagItemFeature implements FeatureParser {

    VALUES(false, true),
    BETWEEN(true, true);

    private final boolean isRequired;
    private final boolean isArray;

    RoadFlagItemFeature(boolean isRequired, boolean isArray) {
        this.isRequired = isRequired;
        this.isArray = isArray;
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
