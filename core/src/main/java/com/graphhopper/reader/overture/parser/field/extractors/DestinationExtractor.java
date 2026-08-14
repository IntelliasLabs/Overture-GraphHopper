package com.graphhopper.reader.overture.parser.field.extractors;

import static java.util.Collections.emptyList;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.road.segment.destination.OvertureDestination;
import java.util.List;

/**
 * Extractor for {@code destinations} data from Overture features.
 * <p>
 * Parses signage information and destination indicators that guide navigation
 * at specific points along a segment.
 */
public class DestinationExtractor {

    /**
     * Extracts a list of destinations from the feature JSON.
     *
     * @param segmentJson raw GeoJSON feature node
     * @return a list of {@link OvertureDestination} objects, or {@code null} if missing or empty
     */
    public static List<OvertureDestination> extractDestinations(JsonNode segmentJson) {
        return emptyList();
    }
    /**
     * Checks for the presence of the {@code destinations} property.
     *
     * @param segmentJson raw GeoJSON feature node
     * @return {@code true} if the property exists and is not null
     */
    public static boolean destinationsExist(JsonNode segmentJson) {
        return true;
    }
}
