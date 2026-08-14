package com.graphhopper.reader.overture.parser.features;

/**
 * Features describing the optional "when" condition object used by Overture rules.
 * <p>
 * The "when" object can restrict applicability of rules (e.g. by time range, heading,
 * transport mode, or vehicle properties).
 */
public enum WhenFeature implements FeatureParser {
    /*-- When feature properties --*/
    /*v*/ DURING,
    /*v*/ HEADING,
    /*v*/ USING(true),
    /*v*/ RECOGNIZED(true),
    /*v*/ MODE(true),
    /*v*/ VEHICLE(true);
    private final boolean isArray;

    WhenFeature(boolean isArray) {
        this.isArray = isArray;
    }

    WhenFeature() {
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
        return false;
    }

    @Override
    public boolean isArray() {
        return isArray;
    }
}
