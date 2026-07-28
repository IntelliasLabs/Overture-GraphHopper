package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.reader.overture.road.surface.OvertureRoadSurface;
import com.graphhopper.reader.overture.road.surface.RoadSurfaceType;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.ev.Smoothness;
import com.graphhopper.util.EdgeIteratorState;

/**
 * Infers road smoothness based on the Overture road surface type.
 * This parser maps physical surface characteristics to expected driving quality.
 */
public final class OvertureSmoothnessParser implements OvertureTagParser {

    private final EnumEncodedValue<Smoothness> smoothnessEnc;

    /**
     * @param smoothnessEnc the encoded value representing smoothness
     */
    public OvertureSmoothnessParser(EnumEncodedValue<Smoothness> smoothnessEnc) {
        this.smoothnessEnc = smoothnessEnc;
    }

    /**
     * Parses smoothness from the segment data.
     * @param segment the Overture road segment
     * @return the inferred Smoothness value
     */
    public static Smoothness parse(OvertureRoadSegment segment) {
        if (segment == null) return Smoothness.MISSING;
        OvertureRoadSurface surface = segment.getRoadSurface();
        if (surface == null) return Smoothness.MISSING;

        return fromSurfaceType(surface.getSurfaceType());
    }
    /// <a href="https://wiki.openstreetmap.org/wiki/Key:smoothness">OSM Smoothness description</a>
    private static Smoothness fromSurfaceType(RoadSurfaceType surfaceType) {
        return switch (surfaceType) {
                // "As-new asphalt or concrete..."
            case ASPHALT, CONCRETE -> Smoothness.EXCELLENT;
                // "Asphalt or concrete showing signs of wear... best roads paved with bricks/sett"
            case METAL, PAVED -> Smoothness.GOOD;
                // "Paving stones with small/shallow potholes... best unpaved but compacted roads"
            case PAVING_STONES -> Smoothness.INTERMEDIATE;
                // "Good unpaved roads without risk of damage to normal passenger cars"
            case GRAVEL -> Smoothness.BAD;
                // "Unpaved roads with potholes and ruts...passable with average SUV (18cm clearance)"
            case UNPAVED -> Smoothness.VERY_BAD;
                // Unpaved tracks with ruts, rocks etc that need a ground clearance of at least 21
            case DIRT -> Smoothness.HORRIBLE;
            default -> Smoothness.MISSING;
        };
    }

    /**
     * Parses {@code Smoothness} from the road segment and applies it to the given graph edge.
     *
     * @param edge the graph edge to update
     * @param segment the Overture road segment
     * @param context unused; smoothness is inferred entirely from the segment
     */
    @Override
    public void handleSegment(
            EdgeIteratorState edge, OvertureRoadSegment segment, OvertureSegmentContext context) {
        Smoothness smoothness = parse(segment);
        edge.set(smoothnessEnc, smoothness);
    }
}
