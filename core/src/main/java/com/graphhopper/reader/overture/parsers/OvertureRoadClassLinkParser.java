package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.road.segment.OvertureRoadClass;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSubclass;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.util.EdgeIteratorState;

/**
 * Identifies road connectors and ramps.
 * Validates 'LINK' subclass only for major road categories (Motorway to Tertiary)
 * to ensure consistency with highway hierarchy.
 */
public final class OvertureRoadClassLinkParser {
    private OvertureRoadClassLinkParser() {}
    /**
     * Determines if a segment is a connector (link/ramp).
     * Only road classes that belong to the main network hierarchy can be links.
     *
     * @return true if subclass is LINK and road class is a major highway type.
     */
    public static boolean isLink(
            OvertureRoadClass overtureClass, OvertureRoadSubclass overtureSubclass) {
        if (overtureSubclass != OvertureRoadSubclass.LINK) return false;

        return switch (overtureClass) {
            case MOTORWAY, TRUNK, PRIMARY, SECONDARY, TERTIARY -> true;
            default -> false;
        };
    }

    /**
     * Determines whether the given segment is a link and applies it to the edge.
     *
     * @param edge      the graph edge to update
     * @param segment   the Overture road segment
     * @param lincEnc the encoded value representing link
     */
    public static void parseLink(
            EdgeIteratorState edge, OvertureRoadSegment segment, BooleanEncodedValue lincEnc) {
        var props = segment.getProperties();
        if (props == null) return;

        OvertureRoadClass overtureClass = props.getRoadClass();
        OvertureRoadSubclass overtureSubclass = props.getSubclass();

        boolean isLink = isLink(overtureClass, overtureSubclass);
        edge.set(lincEnc, isLink);
    }
}
