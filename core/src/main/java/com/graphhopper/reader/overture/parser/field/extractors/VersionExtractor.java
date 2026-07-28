package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.parser.features.SegmentFeature;

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
        Integer version = SegmentFeature.VERSION.parseInteger(segmentJson, null);
        return version == null ? 0 : version;
    }
    /**
     * Checks for the presence of the {@code version} property.
     *
     * @param segmentJson raw GeoJSON feature node
     * @return {@code true} if the property exists and is not null
     */
    public static boolean versionExists(JsonNode segmentJson) {
        JsonNode node = SegmentFeature.VERSION.getFeature(segmentJson, null);
        return node != null && !node.isNull();
    }
}
