package com.graphhopper.reader.overture.parser.features;

/**
 * Features describing the name rule objects attached to a name.
 * <p>
 * Used for accessing fields inside items of the {@code properties.names.rules} array.
 */
public enum NameRuleFeature implements FeatureParser {

    /*-- NameRule feature properties --*/

    /*v*/ VARIANT(true),
    /*v*/ LANGUAGE,
    /*v*/ VALUE(true),
    /*v*/ BETWEEN(false, true),
    /*v*/ SIDE,
    /*v*/ PERSPECTIVES;

    private final boolean isRequired;
    private final boolean isArray;

    NameRuleFeature(boolean isRequired, boolean isArray) {
        this.isRequired = isRequired;
        this.isArray = isArray;
    }

    NameRuleFeature(boolean isRequired) {
        this(isRequired, false);
    }

    NameRuleFeature() {
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
