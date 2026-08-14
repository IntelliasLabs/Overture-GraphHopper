package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.reader.overture.road.surface.RoadSurfaceType;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.ev.Surface;
import com.graphhopper.util.EdgeIteratorState;

/**
 * Parser for mapping Overture road surface types to GraphHopper's {@link Surface} enum.
 */
public final class OvertureRoadSurfaceParser {
    private OvertureRoadSurfaceParser() {}

    /**
     * Maps the primary surface from an {@link OvertureRoadSegment} to the provided {@link EnumEncodedValue}.
     * Defaults to {@link Surface#PAVED} if surface information is missing.
     *
     * @param edge       target {@link EdgeIteratorState}
     * @param segment    source {@link OvertureRoadSegment}
     * @param surfaceEnc target {@link Surface} encoded value
     */
    public static void parseSurface(
            EdgeIteratorState edge, OvertureRoadSegment segment, EnumEncodedValue<Surface> surfaceEnc) {

        var properties = segment.getProperties();
        if (properties == null
                || properties.getSurfaces() == null
                || properties.getSurfaces().isEmpty()) {
            edge.set(surfaceEnc, Surface.PAVED);
            return;
        }

        var overtureSurface = properties.getSurfaces().getFirst().getSurfaceType();
        if (overtureSurface == null) {
            edge.set(surfaceEnc, Surface.PAVED);
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
            case RoadSurfaceType.PAVED, RoadSurfaceType.UNKNOWN -> Surface.PAVED;
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
