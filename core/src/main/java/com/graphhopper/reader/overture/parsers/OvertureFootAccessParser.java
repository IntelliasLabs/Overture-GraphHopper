package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.access.restriction.OvertureAccessRestriction;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelHeading;
import com.graphhopper.reader.overture.road.segment.OvertureRoadClass;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.util.EdgeIteratorState;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses pedestrian/foot access restrictions from Overture road segment data and
 * sets directional access flags on a graph edge.
 *
 * <p>Logic:
 * <ul>
 *   <li>{@link OvertureRoadClass#MOTORWAY} is treated as not walkable and is always closed.</li>
 *   <li>If no access restrictions are present, foot access is allowed in both directions.</li>
 *   <li>If restrictions are present, they are evaluated separately for forward/backward travel
 *       depending on {@link TravelHeading}. Restrictions without a heading apply to both directions.</li>
 * </ul>
 */
public final class OvertureFootAccessParser {

    /**
     * Determines foot (pedestrian) access for the given road segment and applies it to the edge.
     *
     * <p>This method only looks at Overture access restrictions (DENIED rules) and their optional
     * {@link TravelHeading}. For the actual mode check it uses {@code "foot"}.
     *
     * @param edge      the graph edge to update
     * @param segment   the Overture road segment
     * @param accessEnc the encoded value representing foot access
     */
    public static void parseAccess(
            EdgeIteratorState edge, OvertureRoadSegment segment, BooleanEncodedValue accessEnc) {
        var properties = segment.getProperties();

        // Motorways are not walkable by default.
        if (properties.getRoadClass() == OvertureRoadClass.MOTORWAY) {
            edge.set(accessEnc, false, false);
            return;
        }

        var accessRestrictions = properties.getAccessRestrictions();

        // Default: walkable in both directions unless explicitly denied.
        if (accessRestrictions == null || accessRestrictions.isEmpty()) {
            edge.set(accessEnc, true, true);
            return;
        }

        List<OvertureAccessRestriction> fwdRules = new ArrayList<>();
        List<OvertureAccessRestriction> bwdRules = new ArrayList<>();

        for (OvertureAccessRestriction r : accessRestrictions) {
            if (!r.hasWhen() || r.getWhen().getHeading() == null) {
                fwdRules.add(r);
                bwdRules.add(r);
            } else if (r.getWhen().getHeading() == TravelHeading.FORWARD) {
                fwdRules.add(r);
            } else if (r.getWhen().getHeading() == TravelHeading.BACKWARD) {
                bwdRules.add(r);
            }
        }

        boolean canFwd = OvertureAccessParser.isAccessAllowed(fwdRules, "foot");
        boolean canBwd = OvertureAccessParser.isAccessAllowed(bwdRules, "foot");

        edge.set(accessEnc, canFwd, canBwd);
    }
}
