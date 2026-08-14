package com.graphhopper.reader.overture.parser.features;

/**
 * Features describing route metadata attached to a road segment.
 * <p>
 * Used for accessing fields inside items of the {@code properties.routes} array.
 */
public enum RouteFeature implements FeatureParser {

    /*-- Route feature properties --*/
    /*v*/ NAME,
    /*v*/ NETWORK,
    /*v*/ REF,
    /*v*/ SYMBOL,
    /*v*/ WIKIDATA,
    /*v*/ BETWEEN(true);
    private final boolean isArray;

    RouteFeature() {
        this(false);
    }

    RouteFeature(boolean isArray) {
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
