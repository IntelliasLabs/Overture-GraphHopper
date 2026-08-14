package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.parser.features.SegmentFeature;
import com.graphhopper.reader.overture.road.segment.OvertureSegmentSubtype;

/**
 * Extractor for the {@code subtype} property of Overture segments.
 * <p>
 * This property determines the high-level category of the segment (e.g., ROAD, RAIL, WATER),
 * which dictates subsequent parsing logic and field validation.
 */
public class SubtypeExtractor {
    /**
     * Extracts and maps the segment subtype from the provided JSON node.
     * @param segmentJson raw GeoJSON feature node
     * @return the {@link OvertureSegmentSubtype}, or {@code null} if missing or unrecognized
     */
    public static OvertureSegmentSubtype extractSubtype(JsonNode segmentJson) {
        String val = SegmentFeature.SUBTYPE.parseString(segmentJson, "SUBTYPE");
        return OvertureSegmentSubtype.fromString(val);
    }
    /**
     * Checks for the presence of the {@code subtype} property.
     * @param segmentJson raw GeoJSON feature node
     * @return {@code true} if the property exists and is not null
     */
    public static boolean subtypeExists(JsonNode segmentJson) {
        return SegmentFeature.SUBTYPE.getFeature(segmentJson, "SUBTYPE") != null;
    }
}
