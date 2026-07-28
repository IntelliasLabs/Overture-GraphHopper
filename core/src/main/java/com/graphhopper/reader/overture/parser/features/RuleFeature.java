package com.graphhopper.reader.overture.parser.features;

/**
 * Features of the simple value-plus-range rule arrays.
 * <p>
 * Shared by {@code properties.width_rules}, {@code properties.subclass_rules} and
 * {@code properties.level_rules}: all three are arrays of {@code {value, between}}, differing only in
 * the type of {@code value}.
 */
public enum RuleFeature implements FeatureParser {

    /*-- Rule feature properties --*/
    /*v*/ VALUE,
    /*v*/ BETWEEN(true);

    private final boolean isArray;

    RuleFeature() {
        this(false);
    }

    RuleFeature(boolean isArray) {
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
        return false;
    }

    @Override
    public boolean isArray() {
        return isArray;
    }
}
