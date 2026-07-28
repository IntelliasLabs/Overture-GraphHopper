package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.reader.overture.road.surface.RoadSurfaceType;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.ev.Surface;
import com.graphhopper.util.EdgeIteratorState;

/**
 * Parser for mapping Overture road surface types to GraphHopper's {@link Surface} enum.
 */
public final class OvertureRoadSurfaceParser implements OvertureTagParser {

    private final EnumEncodedValue<Surface> surfaceEnc;

    /**
     * @param surfaceEnc target {@link Surface} encoded value
     */
    public OvertureRoadSurfaceParser(EnumEncodedValue<Surface> surfaceEnc) {
        this.surfaceEnc = surfaceEnc;
    }

    /**
     * Maps the primary surface from an {@link OvertureRoadSegment} to the configured {@link
     * EnumEncodedValue}.
     *
     * <p>Writes {@link Surface#MISSING} when the data says nothing about the surface. It previously
     * asserted {@link Surface#PAVED} instead, which is a confident wrong answer: custom models
     * penalise unpaved surfaces and the car parser caps speed on bad ones, so claiming "paved" makes
     * unsurveyed roads look better than they may be. {@code MISSING} lets consumers apply their own
     * default rather than inheriting a guess.
     */
    @Override
    public void handleSegment(
            EdgeIteratorState edge, OvertureRoadSegment segment, OvertureSegmentContext context) {

        var properties = segment.getProperties();
        if (properties == null
                || properties.getSurfaces() == null
                || properties.getSurfaces().isEmpty()) {
            edge.set(surfaceEnc, Surface.MISSING);
            return;
        }

        var overtureSurface = properties.getSurfaces().getFirst().getSurfaceType();
        if (overtureSurface == null) {
            edge.set(surfaceEnc, Surface.MISSING);
            return;
        }

        Surface GHSurface = mapSurface(overtureSurface);
        edge.set(surfaceEnc, GHSurface);
    }

    /**
     * Direct mapping between Overture {@link RoadSurfaceType} and GraphHopper {@link Surface}.
     */
    private static Surface mapSurface(RoadSurfaceType roadSurfaceType) {
        return switch (roadSurfaceType) {
            case RoadSurfaceType.PAVED -> Surface.PAVED;
                // An explicit "unknown" carries no more information than an absent surface.
            case RoadSurfaceType.UNKNOWN -> Surface.MISSING;
            case RoadSurfaceType.UNPAVED -> Surface.UNPAVED;
            case RoadSurfaceType.GRAVEL -> Surface.GRAVEL;
            case RoadSurfaceType.DIRT -> Surface.DIRT;
            case RoadSurfaceType.PAVING_STONES -> Surface.PAVING_STONES;
            case RoadSurfaceType.ASPHALT -> Surface.ASPHALT;
            case RoadSurfaceType.CONCRETE -> Surface.CONCRETE;
            case RoadSurfaceType.METAL -> Surface.OTHER;
        };
    }
}
