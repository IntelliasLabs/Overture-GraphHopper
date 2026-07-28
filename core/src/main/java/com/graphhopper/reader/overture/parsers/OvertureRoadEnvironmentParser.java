package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.ev.RoadEnvironment;
import com.graphhopper.util.EdgeIteratorState;

/**
 * Maps Overture segment properties to GraphHopper {@link RoadEnvironment}.
 * <p>
 * Identifies physical road contexts such as bridges, tunnels, and ferries
 * by evaluating segment flags and functional subtypes in order of precedence.
 * </p>
 */
public final class OvertureRoadEnvironmentParser implements OvertureTagParser {

    private final EnumEncodedValue<RoadEnvironment> roadEnvironmentEnc;

    /**
     * @param roadEnvironmentEnc the encoded value representing road environment
     */
    public OvertureRoadEnvironmentParser(EnumEncodedValue<RoadEnvironment> roadEnvironmentEnc) {
        this.roadEnvironmentEnc = roadEnvironmentEnc;
    }

    /**
     * Parses road environment from the segment properties.
     * @param segment the Overture road segment metaData
     * @return the mapped {@link RoadEnvironment}, or {@link RoadEnvironment#OTHER} if no mapping is found
     */
    public static RoadEnvironment parse(OvertureRoadSegment segment) {
        var prop = segment.getProperties();

        var flags = prop.getFlags();
        if (flags != null && !flags.isEmpty()) {
            var flag = flags.getFirst();
            if (flag.isBridge()) return RoadEnvironment.BRIDGE;
            if (flag.isTunnel()) return RoadEnvironment.TUNNEL;
        }

        var subtype = prop.getSubtype();
        if (subtype == null) return RoadEnvironment.OTHER;

        return switch (subtype) {
            case ROAD -> RoadEnvironment.ROAD;
            case WATER -> RoadEnvironment.FERRY;
            default -> RoadEnvironment.OTHER;
        };
    }

    /**
     * Parses {@code RoadEnvironment} from the road segment and applies it to the given graph edge.
     *
     * @param edge the graph edge to update
     * @param segment the Overture road segment
     * @param context unused; road environment comes entirely from the segment
     */
    @Override
    public void handleSegment(
            EdgeIteratorState edge, OvertureRoadSegment segment, OvertureSegmentContext context) {
        RoadEnvironment roadEnvironment = parse(segment);
        edge.set(roadEnvironmentEnc, roadEnvironment);
    }
}
