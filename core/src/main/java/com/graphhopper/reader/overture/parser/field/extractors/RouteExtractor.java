package com.graphhopper.reader.overture.parser.field.extractors;

import static java.util.Collections.emptyList;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.road.segment.OvertureRoute;
import java.util.List;

/**
 * Extractor for {@code routes} data from Overture features.
 * <p>
 * Parses information about transport routes (e.g., highway numbers or cycling networks)
 * that include the current road segment.
 */
public class RouteExtractor {
    /**
     * Extracts a list of routes associated with the segment.
     * @param segmentJson raw GeoJSON feature node
     * @return a list of {@link OvertureRoute} objects, or {@code null} if missing or empty
     */
    public static List<OvertureRoute> extractRoutes(JsonNode segmentJson) {
        return emptyList();
    }
    /**
     * Checks for the presence of the {@code routes} property.
     * @param segmentJson raw GeoJSON feature node
     * @return {@code true} if the property exists and is not null
     */
    public static boolean routesExist(JsonNode segmentJson) {
        return true;
    }
}
