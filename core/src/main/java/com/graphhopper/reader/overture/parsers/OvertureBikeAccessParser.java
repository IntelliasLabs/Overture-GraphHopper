package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.access.restriction.scope.OvertureScopes;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelHeading;
import com.graphhopper.reader.overture.road.segment.OvertureRoadClass;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.util.EdgeIteratorState;

/**
 * Parses bicycle access restrictions from Overture road segment data and
 * sets directional access flags on a graph edge.
 *
 * <p>Logic:
 * <ul>
 *   <li>Motorways are always closed to bicycles.</li>
 *   <li>If no access restrictions are present, bicycles are allowed in both directions.</li>
 *   <li>Otherwise, access restrictions are evaluated separately for forward and backward
 *       directions using {@link TravelHeading} and {@link OvertureAccessParser#isAccessAllowed}.</li>
 * </ul>
 */
public final class OvertureBikeAccessParser implements OvertureTagParser {

    private final BooleanEncodedValue accessEnc;

    /**
     * @param accessEnc the encoded value representing bicycle access
     */
    public OvertureBikeAccessParser(BooleanEncodedValue accessEnc) {
        this.accessEnc = accessEnc;
    }

    /**
     * Determines bicycle access for the given road segment and applies it to the edge.
     *
     * @param edge the graph edge to update
     * @param segment the Overture road segment
     * @param context unused; access comes entirely from the segment's restrictions
     */
    @Override
    public void handleSegment(
            EdgeIteratorState edge, OvertureRoadSegment segment, OvertureSegmentContext context) {
        var properties = segment.getProperties();

        if (properties.getRoadClass() == OvertureRoadClass.MOTORWAY) {
            edge.set(accessEnc, false, false);
            return;
        }

        var accessRestrictions = properties.getAccessRestrictions();

        if (accessRestrictions == null || accessRestrictions.isEmpty()) {
            edge.set(accessEnc, true, true);
            return;
        }

        var byHeading = OvertureScopes.byHeading(accessRestrictions, OvertureScopes::headingOf);

        boolean canFwd = OvertureAccessParser.isAccessAllowed(byHeading.forward(), "bicycle");
        boolean canBwd = OvertureAccessParser.isAccessAllowed(byHeading.backward(), "bicycle");

        edge.set(accessEnc, canFwd, canBwd);
    }
}
