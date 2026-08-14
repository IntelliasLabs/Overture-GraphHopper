package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.access.restriction.OvertureAccessRestriction;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelHeading;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.util.EdgeIteratorState;
import java.util.ArrayList;
import java.util.List;

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
public final class OvertureCarAccessParser {
    private OvertureCarAccessParser() {}
    /**
     * Determines car access for the given road segment and applies it to the edge.
     *
     * @param edge      the graph edge to update
     * @param segment   the Overture road segment
     * @param accessEnc the encoded value representing car access
     */
    public static void parseAccess(
            EdgeIteratorState edge, OvertureRoadSegment segment, BooleanEncodedValue accessEnc) {
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

        List<OvertureAccessRestriction> fwdRules = new ArrayList<>(3);
        List<OvertureAccessRestriction> bwdRules = new ArrayList<>(3);

        for (OvertureAccessRestriction r : restrictions) {
            if (!r.hasWhen() || r.getWhen().getHeading() == null) {
                fwdRules.add(r);
                bwdRules.add(r);
            } else if (r.getWhen().getHeading() == TravelHeading.FORWARD) {
                fwdRules.add(r);
            } else if (r.getWhen().getHeading() == TravelHeading.BACKWARD) {
                bwdRules.add(r);
            }
        }

        boolean canFwd = OvertureAccessParser.isAccessAllowed(fwdRules, "car");
        boolean canBwd = OvertureAccessParser.isAccessAllowed(bwdRules, "car");

        edge.set(accessEnc, canFwd, canBwd);
    }
}
