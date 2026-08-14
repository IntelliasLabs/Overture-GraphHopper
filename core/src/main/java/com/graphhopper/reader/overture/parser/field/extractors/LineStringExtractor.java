package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.parser.features.SegmentFeature;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

/**
 * Extractor for {@code LineString} geometry from Overture features.
 * <p>
 * Converts GeoJSON-compliant coordinate arrays into JTS {@link LineString} objects
 * used for spatial representation in GraphHopper.
 */
public class LineStringExtractor {
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    /**
     * Extracts a JTS {@link LineString} from an Overture road segment JSON.
     * <p>
     * The line geometry is built from the {@code coordinates} array. Each coordinate is expected to be a
     * pair {@code [lon, lat]} and will be converted to a JTS {@link Coordinate} (x=lon, y=lat).
     *
     * @param featureJson JSON node representing an Overture road segment
     * @return a {@link LineString} created from the segment coordinates, or {@code null} if coordinates are missing
     */
    public static LineString extractLineString(JsonNode featureJson, String featureId) {
        var geometryCoordinates = SegmentFeature.COORDINATES.getFeature(featureJson, featureId);
        if (geometryCoordinates == null) return null;

        Coordinate[] jtsCoordinates = new Coordinate[geometryCoordinates.size()];

        for (int i = 0; i < jtsCoordinates.length; i++) {
            var coordinate = geometryCoordinates.get(i);
            jtsCoordinates[i] =
                    new Coordinate(coordinate.get(0).asDouble(), coordinate.get(1).asDouble());
        }
        return GEOMETRY_FACTORY.createLineString(jtsCoordinates);
    }

    /**
     * Checks whether the given JSON node contains a valid LineString geometry definition.
     * <p>
     * This confirms that:
     * <ul>
     *   <li>{@code geometry.type} exists and equals {@code "LineString"}</li>
     *   <li>{@code geometry.coordinates} exists</li>
     * </ul>
     *
     * @param featureJson JSON node representing an Overture road segment
     * @return {@code true} if the node contains a LineString geometry, {@code false} otherwise
     */
    public static boolean lineStringExists(JsonNode featureJson, String featureId) {
        return SegmentFeature.GEOMETRY_TYPE.existsIn(featureJson, featureId)
                && SegmentFeature.COORDINATES.existsIn(featureJson, featureId)
                && SegmentFeature.GEOMETRY_TYPE
                        .getFeature(featureJson, featureId)
                        .asText()
                        .equals("LineString");
    }
}
