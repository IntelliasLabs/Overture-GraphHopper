package com.graphhopper.reader.overture.parser.features;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Enumeration of all known GeoJSON feature properties that describe a single road segment
 * in the Overture road network.
 * <p>
 * Each enum constant represents a concrete JSON field (optionally nested under a parent
 * feature) and implements {@link FeatureFinder} so it can be used to access the
 * corresponding value from a {@link JsonNode}.
 */
public enum SegmentFeature implements FeatureParser {

    /*-- All features that can be accessed from Segment in GeoJson*/

    /*v*/ TYPE(true),
    /*v*/ ID(true),

    /*v*/ GEOMETRY(true),
    /*----*/ GEOMETRY_TYPE(GEOMETRY, "type", true),
    /*----*/ COORDINATES(GEOMETRY, true, true),

    /*v*/ PROPERTIES(true),

    /*----*/ SUBTYPE(PROPERTIES, false, true),
    /*----*/ BBOX(PROPERTIES, true, true),
    /*----*/ CONNECTORS(PROPERTIES, true),
    /*----*/ ROUTES(PROPERTIES, true),
    /*----*/ ROAD_CLASS(PROPERTIES, "class", true),
    /*----*/ DESTINATIONS(PROPERTIES, true),
    /*----*/ PROHIBITED_TRANSITIONS(PROPERTIES, true),
    /*----*/ ROAD_SURFACE(PROPERTIES, true),
    /*----*/ ROAD_FLAGS(PROPERTIES, true),
    /*----*/ SPEED_LIMITS(PROPERTIES, true),
    /*----*/ WIDTH_RULES(PROPERTIES, true),
    /*----*/ SUBCLASS(PROPERTIES),
    /*----*/ SUBCLASS_RULES(PROPERTIES, true),
    /*----*/ ACCESS_RESTRICTIONS(PROPERTIES, true),
    /*----*/ LEVEL(PROPERTIES),
    /*----*/ LEVEL_RULES(PROPERTIES, true),
    /*----*/ THEME(PROPERTIES, false, true),
    /*----*/ FEATURE_TYPE(PROPERTIES, "type", true),
    /*----*/ VERSION(PROPERTIES, false, true),
    /*----*/ SOURCES(PROPERTIES, true),
    /*---v*/ NAMES(PROPERTIES),
    /*--------*/ PRIMARY(NAMES),
    /*--------*/ COMMON(NAMES),
    /*--------*/ RULES(NAMES, true);

    private final FeatureFinder parentFeature;
    private final String otherName;
    private final boolean isRequired;
    private final boolean isArray;

    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public FeatureFinder getParentFeature() {
        return parentFeature;
    }

    @Override
    public String getOtherName() {
        return otherName;
    }

    @Override
    public boolean isRequired() {
        return isRequired;
    }

    @Override
    public boolean isArray() {
        return isArray;
    }

    SegmentFeature(
            FeatureFinder parentFeature, String otherName, boolean isRequired, boolean isArray) {
        this.parentFeature = parentFeature;
        this.otherName = otherName;
        this.isRequired = isRequired;
        this.isArray = isArray;
    }

    SegmentFeature(FeatureFinder parentFeature, String otherName, boolean isRequired) {
        this.parentFeature = parentFeature;
        this.otherName = otherName;
        this.isRequired = isRequired;
        this.isArray = false;
    }

    SegmentFeature(FeatureFinder parentFeature, boolean isArray, boolean isRequired) {
        this(parentFeature, null, isRequired, isArray);
    }

    SegmentFeature(FeatureFinder parentFeature, boolean isArray) {
        this(parentFeature, isArray, false);
    }

    SegmentFeature(boolean isRequired) {
        this(null, null, isRequired);
    }

    SegmentFeature(FeatureFinder parentFeature, String otherName) {
        this(parentFeature, otherName, false);
    }

    SegmentFeature(FeatureFinder parentFeature) {
        this(parentFeature, null);
    }
}
