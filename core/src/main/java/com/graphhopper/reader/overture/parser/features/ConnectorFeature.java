package com.graphhopper.reader.overture.parser.features;

/**
 * Features describing connector objects that link road segments to other entities
 * (for example junctions or intersections) in the Overture data model.
 */
public enum ConnectorFeature implements FeatureParser {
    /*v*/ CONNECTOR_ID,
    /*v*/ AT;

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
