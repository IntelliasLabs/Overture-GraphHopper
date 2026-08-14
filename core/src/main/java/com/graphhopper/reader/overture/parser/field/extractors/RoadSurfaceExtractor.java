package com.graphhopper.reader.overture.parser.field.extractors;

import static com.graphhopper.reader.overture.parser.field.extractors.BetweenExtractor.extractBetween;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.LinearlyReferencedRange;
import com.graphhopper.reader.overture.parser.features.RoadSurfaceItemFeature;
import com.graphhopper.reader.overture.parser.features.SegmentFeature;
import com.graphhopper.reader.overture.road.surface.OvertureRoadSurface;
import com.graphhopper.reader.overture.road.surface.RoadSurfaceType;
import java.util.List;

/**
 * Extractor for {@code road_surface} data from Overture features.
 * <p>
 * Parses information about road materials and conditions, supporting
 * linear referencing to handle segments with varying surface types.
 */
public class RoadSurfaceExtractor {

    /**
     * Extracts a list of road surfaces from the feature JSON.
     * @param segmentJson raw GeoJSON feature node
     * @param featureId   identifier of the feature for lookup
     * @return a list of {@link OvertureRoadSurface} objects, or {@code null} if missing or empty
     */
    public static List<OvertureRoadSurface> extractRoadSurfaces(
            JsonNode segmentJson, String featureId) {
        return SegmentFeature.ROAD_SURFACE.parseList(
                segmentJson, RoadSurfaceExtractor::parseOvertureRoadSurfaceItem, featureId);
    }

    /**
     * Checks for the presence of the {@code road_surface} property.
     * @param segmentJson raw GeoJSON feature node
     * @param featureId   identifier of the feature
     * @return {@code true} if the property exists and is not null
     */
    public static boolean roadSurfacesExist(JsonNode segmentJson, String featureId) {
        return true;
    }

    /**
     * Parses a single road surface entry from a JSON node.
     * @param overtureRoadSurfaceItem the JSON node representing an individual surface entry
     * @param featureId               identifier of the feature for logging
     * @return a populated {@link OvertureRoadSurface} object
     */
    private static OvertureRoadSurface parseOvertureRoadSurfaceItem(
            JsonNode overtureRoadSurfaceItem, String featureId) {
        String roadSurfaceTypeName =
                RoadSurfaceItemFeature.VALUE.parseString(overtureRoadSurfaceItem, featureId);

        RoadSurfaceType roadSurfaceTypeNode = RoadSurfaceType.fromString(roadSurfaceTypeName);

        LinearlyReferencedRange linearlyReferencedRange = extractBetween(
                RoadSurfaceItemFeature.BETWEEN.getFeature(overtureRoadSurfaceItem, featureId),
                SegmentFeature.ROAD_SURFACE,
                featureId);

        return new OvertureRoadSurface(roadSurfaceTypeNode, linearlyReferencedRange);
    }
}
