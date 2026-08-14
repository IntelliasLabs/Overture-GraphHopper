package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.parser.features.SegmentFeature;

/**
 * Extractor for unique identifiers (IDs) from Overture features.
 * <p>
 * Responsible for retrieving the primary key used to identify segments
 * across the Overture Maps dataset.
 */
public class IdExtractor {

    /**
     * Extracts the segment identifier from the given Overture road segment JSON.
     *
     * @param featureJson JSON node representing an Overture road segment
     * @return the segment {@code id} value, or {@code null} if missing
     */
    public static String extractId(JsonNode featureJson) {
        return SegmentFeature.ID.parseString(featureJson, "UNKNOWN");
    }

    /**
     * Checks whether the given segment JSON contains the required {@code id} attribute.
     *
     * @param featureJson JSON node representing an Overture road segment
     * @return {@code true} if the required {@code id} attribute exists, {@code false} otherwise
     */
    public static boolean idExists(JsonNode featureJson) {
        return SegmentFeature.ID.existsIn(featureJson, "UNKNOWN");
    }
}
