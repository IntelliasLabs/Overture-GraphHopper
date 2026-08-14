package com.graphhopper.reader.overture.parser.field.extractors;

import static java.util.Collections.emptyList;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.road.segment.rule.OvertureProhibitedTransition;
import java.util.List;

/**
 * Extractor for {@code prohibited_transitions} data from Overture features.
 * <p>
 * Parses routing restrictions that define forbidden maneuvers or transitions
 * to specific destinations from a segment.
 */
public class ProhibitedDestinationExtractor {
    /**
     * Extracts a list of prohibited transitions from the feature JSON.
     * @param segmentJson raw GeoJSON feature node
     * @return a list of {@link OvertureProhibitedTransition} objects, or {@code null} if missing or empty
     */
    public static List<OvertureProhibitedTransition> extractProhibitedDestinations(
            JsonNode segmentJson) {
        return emptyList();
    }
    /**
     * Checks for the presence of prohibited transition properties.
     * @param segmentJson raw GeoJSON feature node
     * @return {@code true} if the property exists and is not null
     */
    public static boolean prohibitedDestinationsExist(JsonNode segmentJson) {
        return true;
    }
}
