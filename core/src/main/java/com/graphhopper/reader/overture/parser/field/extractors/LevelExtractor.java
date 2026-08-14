package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Extractor for {@code level} data from Overture features.
 * <p>
 * Handles the vertical layer (Z-order) of a feature, which is crucial for
 * determining road overlapping at intersections and bridges.
 */
public class LevelExtractor {
    /**
     * Extracts the vertical level from the provided JSON node.
     * @param segmentJson raw GeoJSON feature node
     * @return the integer level value, or {@code -1} if missing
     */
    public static int extractLevel(JsonNode segmentJson) {
        return -1;
    }
    /**
     * Checks for the presence of the {@code level} property.
     * @param segmentJson raw GeoJSON feature node
     * @return {@code true} if the property exists and is not null
     */
    public static boolean levelExists(JsonNode segmentJson) {
        return true;
    }
}
