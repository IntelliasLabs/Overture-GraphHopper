package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.access.restriction.OvertureAccessRestriction;
import com.graphhopper.reader.overture.access.restriction.PropertyScopeContainer;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import java.util.List;

/**
 * Helper parser that detects whether an {@link OvertureRoadSegment} carries any
 * temporal access restriction expressions (the {@code during} field).
 *
 * <p>This class detects the presence of a temporal expression
 * and does not attempt to parse or evaluate the {@code during} string.</p>
 */
public final class OvertureTemporalAccessParser {

    private OvertureTemporalAccessParser() {}

    /**
     * Returns {@code true} if the provided segment contains at least one
     * access restriction with a non-empty {@code during} (temporal) clause.
     *
     * @param segment the road segment to inspect, may be {@code null}
     * @return {@code true} if a temporal restriction is present
     */
    public static boolean hasTemporalRestriction(OvertureRoadSegment segment) {
        if (segment == null) {
            return false;
        }
        var props = segment.getProperties();
        if (props == null) {
            return false;
        }

        List<OvertureAccessRestriction> restrictions = props.getAccessRestrictions();
        if (restrictions == null || restrictions.isEmpty()) {
            return false;
        }

        for (OvertureAccessRestriction r : restrictions) {
            if (r == null || !r.hasWhen()) continue;

            PropertyScopeContainer when = r.getWhen();
            if (when == null) continue;

            if (when.hasDuring()) {
                String during = when.getDuring();
                if (during != null && !during.isBlank()) {
                    return true;
                }
            }
        }

        return false;
    }
}
