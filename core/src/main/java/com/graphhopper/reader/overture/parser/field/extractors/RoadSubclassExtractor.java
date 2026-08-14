package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.parser.features.SegmentFeature;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSubclass;

/**
 * Extractor for {@code subclass} data from Overture features.
 * <p>
 * Provides additional classification for road segments, further refining
 * the category defined by the road class.
 */
public class RoadSubclassExtractor {
    /**
     * Extracts the road subclass from the given Overture road segment JSON.
     * <p>
     * This reads the {@code subclass} attribute and converts it into an {@link OvertureRoadSubclass}
     * via {@link OvertureRoadSubclass#fromString(String)}.
     *
     * @param segmentJson JSON node representing an Overture road segment
     * @return the parsed {@link OvertureRoadSubclass}, or {@code null} if the value is missing/unknown
     */
    public static OvertureRoadSubclass extractRoadSubclass(JsonNode segmentJson, String featureId) {
        return OvertureRoadSubclass.fromString(
                SegmentFeature.SUBCLASS.parseString(segmentJson, featureId));
    }

    /**
     * Checks whether the given segment JSON contains the required {@code subclass} attribute.
     *
     * @param segmentJson JSON node representing an Overture road segment
     * @return {@code true} if the required {@code subclass} attribute exists, {@code false} otherwise
     */
    public static boolean roadSubclassExists(JsonNode segmentJson, String featureId) {
        return SegmentFeature.SUBCLASS.existsIn(segmentJson, featureId);
    }
}
