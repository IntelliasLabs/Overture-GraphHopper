package com.graphhopper.reader.overture.parser.features;

/**
 * Features describing the perspectives objects attached to a name rule.
 * <p>
 * Used for accessing fields inside items of the {@code properties.names.rules.perspectives} array.
 */
public enum PerspectivesFeature implements FeatureParser {

    /*-- Perspectives feature properties --*/

    /*v*/ MODE(true),
    /*v*/ COUNTRIES(true, true);

    private final boolean isRequired;
    private final boolean isArray;

    PerspectivesFeature(boolean isRequired, boolean isArray) {
        this.isRequired = isRequired;
        this.isArray = isArray;
    }

    PerspectivesFeature(boolean isRequired) {
        this(isRequired, false);
    }

    PerspectivesFeature() {
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
