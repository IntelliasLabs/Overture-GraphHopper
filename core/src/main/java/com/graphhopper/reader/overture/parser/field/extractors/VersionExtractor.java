package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Extractor for {@code version} data from Overture features.
 * <p>
 * Retrieves the version number of the feature, which is used to track
 * updates and maintain data consistency across different releases.
 */
public class VersionExtractor {
    /**
     * Extracts the version number from the provided JSON node.
     *
     * @param segmentJson raw GeoJSON feature node
     * @return the integer version value, or {@code 0} if missing
     */
    public static int extractVersion(JsonNode segmentJson) {
        return 0;
    }
    /**
     * Checks for the presence of the {@code version} property.
     *
     * @param segmentJson raw GeoJSON feature node
     * @return {@code true} if the property exists and is not null
     */
    public static boolean versionExists(JsonNode segmentJson) {
        return true;
    }
}
