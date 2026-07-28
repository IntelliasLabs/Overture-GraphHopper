package com.graphhopper.reader.overture.parser.field.extractors;

import static com.graphhopper.reader.overture.parser.field.extractors.BetweenExtractor.extractBetween;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.parser.features.RouteFeature;
import com.graphhopper.reader.overture.parser.features.SegmentFeature;
import com.graphhopper.reader.overture.road.segment.OvertureRoute;
import java.util.List;

/**
 * Extractor for {@code routes} data from Overture features.
 * <p>
 * Parses the named road networks a segment belongs to, which may cover part of the segment only
 * through linear referencing.
 */
public class RouteExtractor {

    /**
     * Extracts a list of routes from the provided JSON node.
     *
     * @param segmentJson raw GeoJSON feature node
     * @return the routes, empty when the property is absent or holds nothing usable
     */
    public static List<OvertureRoute> extractRoutes(JsonNode segmentJson) {
        return SegmentFeature.ROUTES.parseList(segmentJson, RouteExtractor::parseRoute, null);
    }

    /**
     * Checks for the presence of the {@code routes} property.
     *
     * @param segmentJson raw GeoJSON feature node
     * @return {@code true} if the property exists and is not null
     */
    public static boolean routesExist(JsonNode segmentJson) {
        JsonNode node = SegmentFeature.ROUTES.getFeature(segmentJson, null);
        return node != null && !node.isNull();
    }

    private static OvertureRoute parseRoute(JsonNode item, String featureId) {
        return new OvertureRoute(
                RouteFeature.NAME.parseString(item, featureId),
                RouteFeature.NETWORK.parseString(item, featureId),
                RouteFeature.REF.parseString(item, featureId),
                RouteFeature.SYMBOL.parseString(item, featureId),
                RouteFeature.WIKIDATA.parseString(item, featureId),
                extractBetween(
                        RouteFeature.BETWEEN.getFeature(item, featureId), SegmentFeature.ROUTES, featureId));
    }
}
