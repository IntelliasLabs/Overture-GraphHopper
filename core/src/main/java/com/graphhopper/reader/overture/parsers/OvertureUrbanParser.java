package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.road.segment.OvertureRoadClass;
import java.util.Set;

/**
 * Utility class to determine if a given OvertureRoadClass is considered urban.
 */
public final class OvertureUrbanParser {
    private OvertureUrbanParser() {}

    private static final Set<OvertureRoadClass> URBAN_ROAD_CLASSES = Set.of(
            OvertureRoadClass.RESIDENTIAL, OvertureRoadClass.LIVING_STREET, OvertureRoadClass.SERVICE);

    /**
     * Checks if the provided road class is classified as urban.
     *
     * @param roadClass the OvertureRoadClass to check
     * @return true if the road class is urban, false otherwise or if input is null
     */
    public static boolean isUrban(OvertureRoadClass roadClass) {
        if (roadClass == null) {
            return false;
        }
        return URBAN_ROAD_CLASSES.contains(roadClass);
    }
}
