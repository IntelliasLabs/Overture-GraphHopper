package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.parser.features.SegmentFeature;
import com.graphhopper.reader.overture.road.segment.OvertureRoadClass;

/**
 * Extractor for {@code road_class} data from Overture features.
 * <p>
 * Maps string values from the JSON schema to {@link OvertureRoadClass} constants,
 * defining the functional importance and type of the road segment.
 */
public class RoadClassExtractor {
    /**
     * Extracts the road class from the given Overture road segment JSON.
     * <p>
     * This reads the {@code road_class} attribute and converts it into an {@link OvertureRoadClass}
     * via {@link OvertureRoadClass#fromString(String)}.
     *
     * @param featureJson JSON node representing an Overture road segment
     * @return the parsed {@link OvertureRoadClass}, or {@code null} if the value is missing/unknown
     */
    public static OvertureRoadClass extractRoadClass(JsonNode featureJson, String featureId) {

        String roadClassStr = SegmentFeature.ROAD_CLASS.parseString(featureJson, featureId);
        return OvertureRoadClass.fromString(roadClassStr);
    }

    /**
     * Checks whether the given segment JSON contains the required {@code road_class} attribute.
     *
     * @param segmentJson JSON node representing an Overture road segment
     * @return {@code true} if the required {@code road_class} attribute exists, {@code false} otherwise
     */
    public static boolean roadClassExists(JsonNode segmentJson, String featureId) {
        return SegmentFeature.ROAD_CLASS.existsIn(segmentJson, featureId);
    }
}
