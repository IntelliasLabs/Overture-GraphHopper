package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.road.segment.OvertureRoadClass;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.reader.overture.road.surface.OvertureRoadSurface;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.ev.TrackType;
import com.graphhopper.util.EdgeIteratorState;
import java.util.List;

/**
 * Maps Overture road surface data to GraphHopper {@link TrackType} (grades).
 * <p>
 * This parser evaluates the physical quality of tracks and paths. It limits its
 * scope to {@code TRACK} and {@code PATH} road classes, using the primary
 * surface material to determine the firmness and accessibility grade.
 * </p>
 */
public final class OvertureTrackTypeParser {
    private OvertureTrackTypeParser() {}

    /**
     * Determines the {@link TrackType} grade based on the segment's surface.
     * <p>
     * Logic flow:
     * <ol>
     * <li>Filters for {@link OvertureRoadClass#TRACK} or {@link OvertureRoadClass#PATH}.</li>
     * <li>Iterates through available {@link OvertureRoadSurface} entries.</li>
     * <li>Maps the first recognized surface type to a grade (e.g., ASPHALT → GRADE1, DIRT → GRADE5).</li>
     * </ol>
     * </p>
     *
     * @param segment the Overture road segment metadata.
     * @return the determined {@link TrackType}, or {@link TrackType#MISSING} if ineligible or unknown.
     */
    public static TrackType parse(OvertureRoadSegment segment) {
        if (segment == null) {
            return TrackType.MISSING;
        }

        var props = segment.getProperties();
        if (props == null) {
            return TrackType.MISSING;
        }

        // Consider only TRACK or PATH road classes
        OvertureRoadClass roadClass = props.getRoadClass();
        if (roadClass != OvertureRoadClass.TRACK && roadClass != OvertureRoadClass.PATH) {
            return TrackType.MISSING;
        }

        List<OvertureRoadSurface> surfaces = props.getSurfaces();
        if (surfaces == null || surfaces.isEmpty()) {
            return TrackType.MISSING;
        }

        // Use first recognized surface; segments may contain multiple entries
        for (OvertureRoadSurface surface : surfaces) {
            if (surface == null || surface.getSurfaceType() == null) continue;

            TrackType type =
                    switch (surface.getSurfaceType()) {
                        case PAVED, PAVING_STONES, ASPHALT, CONCRETE -> TrackType.GRADE1;
                        case GRAVEL, METAL -> TrackType.GRADE2;
                        case UNPAVED -> TrackType.GRADE3;
                        case DIRT -> TrackType.GRADE5;
                        default -> null;
                    };
            if (type != null) return type;
        }

        return TrackType.MISSING;
    }

    /**
     * Parses {@code TrackType} from the road segment and
     * applies it to the given graph edge
     *
     * @param edge      the graph edge to update
     * @param segment   the Overture road segment
     * @param trackTypeEnc the encoded value representing track type
     */
    public static void parseTrackType(
            EdgeIteratorState edge,
            OvertureRoadSegment segment,
            EnumEncodedValue<TrackType> trackTypeEnc) {
        TrackType trackType = parse(segment);
        edge.set(trackTypeEnc, trackType);
    }
}
