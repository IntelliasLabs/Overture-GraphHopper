package com.graphhopper.reader.overture.parser.features;

/**
 * Features describing per-property provenance attached to a road segment.
 * <p>
 * Used for accessing fields inside items of the {@code properties.sources} array.
 */
public enum SourceFeature implements FeatureParser {

    /*-- Source feature properties --*/
    /*v*/ PROPERTY,
    /*v*/ DATASET,
    /*v*/ LICENSE,
    /*v*/ RECORD_ID,
    /*v*/ UPDATE_TIME,
    /*v*/ CONFIDENCE,
    /*v*/ BETWEEN(true);

    private final boolean isArray;

    SourceFeature() {
        this(false);
    }

    SourceFeature(boolean isArray) {
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
