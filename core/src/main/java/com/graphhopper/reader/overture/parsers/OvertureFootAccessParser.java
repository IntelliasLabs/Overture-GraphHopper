package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.access.restriction.scope.OvertureScopes;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelHeading;
import com.graphhopper.reader.overture.road.segment.OvertureRoadClass;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.util.EdgeIteratorState;

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
public final class OvertureFootAccessParser implements OvertureTagParser {

    private final BooleanEncodedValue accessEnc;

    /**
     * @param accessEnc the encoded value representing foot access
     */
    public OvertureFootAccessParser(BooleanEncodedValue accessEnc) {
        this.accessEnc = accessEnc;
    }

    /**
     * Determines foot (pedestrian) access for the given road segment and applies it to the edge.
     *
     * <p>This method only looks at Overture access restrictions (DENIED rules) and their optional
     * {@link TravelHeading}. For the actual mode check it uses {@code "foot"}.
     *
     * @param edge the graph edge to update
     * @param segment the Overture road segment
     * @param context unused; access comes entirely from the segment's restrictions
     */
    @Override
    public void handleSegment(
            EdgeIteratorState edge, OvertureRoadSegment segment, OvertureSegmentContext context) {
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

        var byHeading = OvertureScopes.byHeading(accessRestrictions, OvertureScopes::headingOf);

        boolean canFwd = OvertureAccessParser.isAccessAllowed(byHeading.forward(), "foot");
        boolean canBwd = OvertureAccessParser.isAccessAllowed(byHeading.backward(), "foot");

        edge.set(accessEnc, canFwd, canBwd);
    }
}
