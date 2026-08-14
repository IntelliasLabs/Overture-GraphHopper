package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.road.flags.OvertureRoadFlags;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.util.EdgeIteratorState;
import java.util.List;

/**
 * Parses road flags (e.g. bridge, tunnel) from Overture road segment data and
 * applies them to a graph edge.
 */
public final class OvertureRoadFlagsParser {

    private OvertureRoadFlagsParser() {}

    /**
     * Extracts flags from the segment properties and applies them to the edge
     * using the provided encoded values.
     *
     * @param edge      The graph edge to update.
     * @param segment   The Overture road segment containing properties.
     * @param bridgeEnc The encoded value for bridge.
     * @param tunnelEnc The encoded value for tunnel.
     */
    public static void applyFlags(
            EdgeIteratorState edge,
            OvertureRoadSegment segment,
            BooleanEncodedValue bridgeEnc,
            BooleanEncodedValue tunnelEnc) {

        var properties = segment.getProperties();
        if (properties == null) {
            return;
        }

        List<OvertureRoadFlags> flags = properties.getFlags();
        if (flags == null || flags.isEmpty()) {
            edge.set(bridgeEnc, false);
            edge.set(tunnelEnc, false);
            return;
        }

        OvertureRoadFlags flag = flags.getFirst();
        edge.set(bridgeEnc, flag.isBridge());
        edge.set(tunnelEnc, flag.isTunnel());
    }
}
