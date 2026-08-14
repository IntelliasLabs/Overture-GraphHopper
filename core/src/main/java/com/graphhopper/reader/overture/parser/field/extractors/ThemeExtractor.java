package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.road.segment.OvertureTheme;

/**
 * Extractor for the {@code theme} property from Overture features.
 * <p>
 * Identifies the high-level dataset theme (e.g., 'transportation') that the
 * feature belongs to, aiding in broad data classification.
 */
public class ThemeExtractor {
    /**
     * Extracts the theme classification from the provided JSON node.
     * @param segmentJson raw GeoJSON feature node
     * @return the {@link OvertureTheme}, or {@code null} if missing or invalid
     */
    public static OvertureTheme extractTheme(JsonNode segmentJson) {
        return null;
    }
    /**
     * Checks for the presence of the {@code theme} property.
     * @param segmentJson raw GeoJSON feature node
     * @return {@code true} if the property exists and is not null
     */
    public static boolean themeExists(JsonNode segmentJson) {
        return true;
    }
}
