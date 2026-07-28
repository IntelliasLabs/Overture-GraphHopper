package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.parser.features.SegmentFeature;

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
     * @return the integer level value, or {@code 0} if missing. Zero rather than -1 because 0 is
     *     Overture's ground level and is what a GeoParquet import yields, which has no level column at
     *     all; returning -1 here would have made the same road differ by format.
     */
    public static int extractLevel(JsonNode segmentJson) {
        Integer level = SegmentFeature.LEVEL.parseInteger(segmentJson, null);
        return level == null ? 0 : level;
    }
    /**
     * Checks for the presence of the {@code level} property.
     * @param segmentJson raw GeoJSON feature node
     * @return {@code true} if the property exists and is not null
     */
    public static boolean levelExists(JsonNode segmentJson) {
        JsonNode node = SegmentFeature.LEVEL.getFeature(segmentJson, null);
        return node != null && !node.isNull();
    }
}
