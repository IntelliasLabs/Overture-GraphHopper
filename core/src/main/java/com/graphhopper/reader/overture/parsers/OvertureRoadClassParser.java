package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.road.segment.OvertureRoadClass;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSubclass;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.ev.RoadClass;
import com.graphhopper.util.EdgeIteratorState;

/**
 * Maps Overture road categories to GraphHopper {@link RoadClass}.
 * <p>
 * Implements a two-tier mapping logic: first attempts to refine the classification
 * using {@link OvertureRoadSubclass}, then falls back to the primary road class.
 * </p>
 */
public final class OvertureRoadClassParser implements OvertureTagParser {

    private final EnumEncodedValue<RoadClass> roadClassEnc;

    /**
     * @param roadClassEnc the encoded value representing road class
     */
    public OvertureRoadClassParser(EnumEncodedValue<RoadClass> roadClassEnc) {
        this.roadClassEnc = roadClassEnc;
    }

    /**
     * Maps Overture road classification to GraphHopper {@link RoadClass}.
     * Subclass refinements take precedence over the general road class.
     * @param overtureClass the primary classification of the road segment
     * @param overtureSubclass the specific usage refinement, may be null
     * @return the mapped {@link RoadClass}, or {@link RoadClass#OTHER} if no match found
     */
    public static RoadClass parse(String overtureClass, String overtureSubclass) {
        OvertureRoadSubclass sub = OvertureRoadSubclass.fromString(overtureSubclass);

        RoadClass refinedClass = (sub == null)
                ? null
                : switch (sub) {
                    case SIDEWALK, CROSSWALK -> RoadClass.FOOTWAY;
                    case CYCLE_CROSSING -> RoadClass.CYCLEWAY;
                    case PARKING_AISLE, DRIVEWAY, ALLEY -> RoadClass.SERVICE;
                    default -> null;
                };
        return refinedClass != null ? refinedClass : RoadClass.find(overtureClass);
    }

    /**
     * Parses {@code RoadClass} from the road segment and applies it to the given graph edge.
     *
     * @param edge the graph edge to update
     * @param segment the Overture road segment
     * @param context unused; road class comes entirely from the segment
     */
    @Override
    public void handleSegment(
            EdgeIteratorState edge, OvertureRoadSegment segment, OvertureSegmentContext context) {
        OvertureRoadClass overtureRoadClass = segment.getProperties().getRoadClass();
        OvertureRoadSubclass overtureRoadSubclass = segment.getProperties().getSubclass();

        String overtureRoadClassStr = overtureRoadClass != null ? overtureRoadClass.toString() : null;
        String overtureRoadSubClassStr =
                overtureRoadSubclass != null ? overtureRoadSubclass.toString() : null;

        RoadClass roadClass = parse(overtureRoadClassStr, overtureRoadSubClassStr);
        edge.set(roadClassEnc, roadClass);
    }
}
