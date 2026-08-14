package com.graphhopper.reader.overture.parser.features;

/**
 * Features describing the fields inside an item of the {@code properties.access_restrictions} array.
 */
public enum AccessRestrictionsFeature implements FeatureParser {

    /*-- Access restriction object properties --*/
    /*v*/ ACCESS_TYPE(true),
    /*v*/ WHEN(),
    /*v*/ BETWEEN(false, true);

    private final boolean isRequired;
    private final boolean isArray;

    AccessRestrictionsFeature(boolean isRequired, boolean isArray) {
        this.isRequired = isRequired;
        this.isArray = isArray;
    }

    AccessRestrictionsFeature(boolean isRequired) {
        this(isRequired, false);
    }

    AccessRestrictionsFeature() {
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
