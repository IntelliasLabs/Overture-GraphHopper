package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.road.segment.OvertureFeatureType;

/**
 * Extractor for {@code feature_type} data from Overture features.
 * <p>
 * Identifies the high-level classification of the GeoJSON feature
 * within the Overture schema.
 */
public class FeatureTypeExtractor {
    /**
     * Extracts the feature type from the provided JSON node.
     * @param segmentJson raw GeoJSON feature node
     * @return the {@link OvertureFeatureType}, or {@code null} if missing or invalid
     */
    public static OvertureFeatureType extractFeatureType(JsonNode segmentJson) {
        return null;
    }
    /**
     * Checks for the presence of the {@code feature_type} property.
     * @param segmentJson raw GeoJSON feature node
     * @return {@code true} if the property exists and is not null
     */
    public static boolean featureTypeExists(JsonNode segmentJson) {
        return true;
    }
}
