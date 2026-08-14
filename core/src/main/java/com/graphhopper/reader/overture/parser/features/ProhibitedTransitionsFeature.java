package com.graphhopper.reader.overture.parser.features;

/**
 * Features describing prohibited transition rules attached to a segment.
 * <p>
 * Used for accessing fields inside items of the {@code properties.prohibited_transitions} array.
 */
public enum ProhibitedTransitionsFeature implements FeatureParser {

    /*-- ProhibitedTransitions feature properties --*/
    /*v*/ SEQUENCE(true, true),
    /*v*/ FINAL_HEADING(true),
    /*v*/ WHEN,
    /*v*/ BETWEEN(false, true);
    private final boolean isRequired;
    private final boolean isArray;

    ProhibitedTransitionsFeature(boolean isRequired, boolean isArray) {
        this.isRequired = isRequired;
        this.isArray = isArray;
    }

    ProhibitedTransitionsFeature(boolean isRequired) {

        this(isRequired, false);
    }

    ProhibitedTransitionsFeature() {
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
