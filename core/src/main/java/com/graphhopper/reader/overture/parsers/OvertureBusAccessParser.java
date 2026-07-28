package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.access.restriction.OvertureAccessRestriction;
import com.graphhopper.reader.overture.access.restriction.scope.OvertureScopes;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelMode;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.util.EdgeIteratorState;

/**
 * Parses bus access restrictions from Overture road segment data and sets directional access flags on
 * a graph edge.
 *
 * <p>Interprets the {@link OvertureAccessRestriction} theme from Overture into GraphHopper's {@link
 * BooleanEncodedValue} format. Deliberately identical in shape to {@link OvertureCarAccessParser},
 * delegating to {@link OvertureAccessParser#isAccessAllowed} rather than matching modes itself. This
 * parser previously did its own mode matching and wrote a single non-directional value, which made it
 * disagree with the car, bike and foot parsers on two counts: it ignored {@code when.heading}, so a
 * oneway denial closed the road in both directions, and it re-derived the {@link TravelMode} hierarchy
 * instead of reusing the one place that knows it.
 *
 * <p>The hierarchy needs nothing bus-specific: {@code getModesWithParents("bus")} already yields
 * {@code bus}, {@code motor_vehicle} and {@code vehicle}, so a general motor-vehicle denial closes the
 * road to buses while a {@code {mode: bus, access_type: allowed}} rule lifts it again.
 */
public final class OvertureBusAccessParser implements OvertureTagParser {

    private final BooleanEncodedValue accessEnc;

    /**
     * @param accessEnc the encoded value representing bus access
     */
    public OvertureBusAccessParser(BooleanEncodedValue accessEnc) {
        this.accessEnc = accessEnc;
    }

    /**
     * Determines bus access for the given road segment and applies it to the edge.
     *
     * @param edge the graph edge to update
     * @param segment the Overture road segment
     * @param context unused; access comes entirely from the segment's restrictions
     */
    @Override
    public void handleSegment(
            EdgeIteratorState edge, OvertureRoadSegment segment, OvertureSegmentContext context) {
        var properties = segment.getProperties();

        // A segment closed to everything is closed to buses, whatever any per-mode rule says.
        if (!segment.isAccessible()) {
            edge.set(accessEnc, false, false);
            return;
        }

        var restrictions = properties == null ? null : properties.getAccessRestrictions();

        if (restrictions == null || restrictions.isEmpty()) {
            edge.set(accessEnc, true, true);
            return;
        }

        var byHeading = OvertureScopes.byHeading(restrictions, OvertureScopes::headingOf);

        boolean canFwd = OvertureAccessParser.isAccessAllowed(byHeading.forward(), "bus");
        boolean canBwd = OvertureAccessParser.isAccessAllowed(byHeading.backward(), "bus");

        edge.set(accessEnc, canFwd, canBwd);
    }
}
