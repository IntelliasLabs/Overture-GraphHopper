package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.road.flags.OvertureRoadFlags;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.ev.Hazmat;
import com.graphhopper.util.EdgeIteratorState;
import java.util.List;

/**
 * Utility class to determine if a road segment has hazardous materials restrictions.
 * <p>
 * Hazmat transport is typically banned in tunnels due to ventilation concerns and
 * potential for catastrophic accidents in enclosed spaces.
 * </p>
 */
public final class OvertureHazmatParser implements OvertureTagParser {

    private final EnumEncodedValue<Hazmat> hazmatEnc;

    /**
     * @param hazmatEnc the encoded value representing hazmat restrictions
     */
    public OvertureHazmatParser(EnumEncodedValue<Hazmat> hazmatEnc) {
        this.hazmatEnc = hazmatEnc;
    }

    /**
     * Checks if the provided road segment has hazmat restrictions.
     * <p>
     * Currently detects hazmat restrictions based on tunnel status, as hazardous
     * materials transport is commonly prohibited in tunnels.
     * </p>
     *
     * @param segment the OvertureRoadSegment to check
     * @return true if the segment has hazmat restrictions, false otherwise or if input is null
     */
    public static boolean hasHazmatRestriction(OvertureRoadSegment segment) {
        if (segment == null || segment.getProperties() == null) {
            return false;
        }

        List<OvertureRoadFlags> flags = segment.getProperties().getFlags();
        if (flags == null || flags.isEmpty()) {
            return false;
        }

        OvertureRoadFlags flag = flags.getFirst();
        return flag != null && flag.isTunnel();
    }

    /**
     * Determines and applies restrictions for the transport of hazardous materials.
     *
     * @param edge the graph edge to update
     * @param segment the Overture road segment
     * @param context unused; hazmat status comes entirely from the segment
     */
    @Override
    public void handleSegment(
            EdgeIteratorState edge, OvertureRoadSegment segment, OvertureSegmentContext context) {
        Hazmat hazmat = hasHazmatRestriction(segment) ? Hazmat.YES : Hazmat.NO;
        edge.set(hazmatEnc, hazmat);
    }
}
