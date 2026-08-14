package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.access.restriction.AccessType;
import com.graphhopper.reader.overture.access.restriction.OvertureAccessRestriction;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelMode;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.util.EdgeIteratorState;

/**
 * Parser for mapping Overture Maps road properties to bus access encoded values.
 * <p>
 * This parser interprets the {@link OvertureAccessRestriction} theme from Overture
 * and converts it into GraphHopper's internal {@link BooleanEncodedValue} format.
 * </p>
 */
public class OvertureBusAccessParser {
    private OvertureBusAccessParser() {}

    /**
     * Parses road access permissions for buses based on Overture road segment access restrictions.
     * <p>
     * The method evaluates hierarchical access rules:
     * 1. Specific bus restrictions ({@link TravelMode#BUS}).
     * 2. Motor vehicle restrictions ({@link TravelMode#MOTOR_VEHICLE}).
     * 3. General restrictions (where no mode is specified).
     * </p>
     *
     * @param edge      the GraphHopper edge where the access permission will be stored
     * @param segment   the Overture road segment containing the access properties
     * @param accessEnc the boolean encoded value used to store bus accessibility
     */
    public static void parseAccess(
            EdgeIteratorState edge, OvertureRoadSegment segment, BooleanEncodedValue accessEnc) {
        var props = segment.getProperties();
        if (props == null || props.getAccessRestrictions() == null) {
            edge.set(accessEnc, true);
            return;
        }

        var restrictions = props.getAccessRestrictions();
        boolean access = true;
        for (OvertureAccessRestriction restriction : restrictions) {
            if (!restriction.hasAccessType()) {
                continue;
            }

            var modes = restriction.hasWhen() ? restriction.getWhen().getMode() : null;
            if (modes == null || modes.isEmpty()) {
                access = restriction.getAccessType() != AccessType.DENIED;
            } else if (modes.contains(TravelMode.BUS)) {
                access = restriction.getAccessType() != AccessType.DENIED;
                break;
            } else if (modes.contains(TravelMode.MOTOR_VEHICLE)) {
                access = restriction.getAccessType() != AccessType.DENIED;
            }
        }

        edge.set(accessEnc, access);
    }
}
