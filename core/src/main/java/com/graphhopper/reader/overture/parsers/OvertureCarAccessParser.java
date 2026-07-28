package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.access.restriction.scope.OvertureScopes;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelHeading;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.util.EdgeIteratorState;

/**
 * Parses car access restrictions from Overture road segment data and
 * sets directional access flags on a graph edge.
 *
 * <p>Logic:
 * <ul>
 *   <li>If no access restrictions are present, cars are allowed in both directions.</li>
 *   <li>Otherwise, access restrictions are evaluated separately for forward and backward
 *       directions using {@link TravelHeading} and {@link OvertureAccessParser#isAccessAllowed}.</li>
 * </ul>
 */
public final class OvertureCarAccessParser implements OvertureTagParser {

    private final BooleanEncodedValue accessEnc;

    /**
     * @param accessEnc the encoded value representing car access
     */
    public OvertureCarAccessParser(BooleanEncodedValue accessEnc) {
        this.accessEnc = accessEnc;
    }

    /**
     * Determines car access for the given road segment and applies it to the edge.
     *
     * @param edge the graph edge to update
     * @param segment the Overture road segment
     * @param context unused; access comes entirely from the segment's restrictions
     */
    @Override
    public void handleSegment(
            EdgeIteratorState edge, OvertureRoadSegment segment, OvertureSegmentContext context) {
        var properties = segment.getProperties();

        if (!segment.isAccessible()) {
            edge.set(accessEnc, false, false);
            return;
        }

        var restrictions = properties.getAccessRestrictions();

        if (restrictions.isEmpty()) {
            edge.set(accessEnc, true, true);
            return;
        }

        var byHeading = OvertureScopes.byHeading(restrictions, OvertureScopes::headingOf);

        boolean canFwd = OvertureAccessParser.isAccessAllowed(byHeading.forward(), "car");
        boolean canBwd = OvertureAccessParser.isAccessAllowed(byHeading.backward(), "car");

        edge.set(accessEnc, canFwd, canBwd);
    }
}
