package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.reader.overture.road.segment.OvertureSegmentSubtype;

/**
 * Utility parser for identifying ferry and water-based transit segments.
 * <p>
 * In the Overture Maps schema, ferry routes are distinguished by their functional subtype.
 * This class provides logic to isolate these segments, allowing the routing engine
 * to apply specific costs or access rules for maritime transport.
 * </p>
 */
public final class OvertureFerryParser {
    private OvertureFerryParser() {}
    /**
     * Detects if the given segment is a ferry route based on its subtype.
     * @param segment the OvertureRoadSegment to check
     * @return true if the segment is a ferry route, false otherwise
     */
    public static boolean isFerry(OvertureRoadSegment segment) {
        if (segment == null || segment.getProperties() == null) {
            return false;
        }
        return OvertureSegmentSubtype.WATER.equals(segment.getProperties().getSubtype());
    }
}
