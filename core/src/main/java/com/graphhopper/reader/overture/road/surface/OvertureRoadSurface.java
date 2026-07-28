package com.graphhopper.reader.overture.road.surface;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import com.graphhopper.reader.overture.road.segment.HasBetweenProperty;
import java.util.Objects;

/**
 * Represents the road surface for a linearly referenced segment as returned by the Overture
 * road surface layer.
 * <p>
 * An instance combines a {@link RoadSurfaceType} classification with an optional
 * {@link LinearlyReferencedRange} that locates the surface section along a geometry.
 */
public class OvertureRoadSurface implements HasBetweenProperty {
    /** The surface classification for this segment, never {@code null}. */
    private final RoadSurfaceType surfaceType;
    /**
     * Optional linearly referenced range along the segment this surface applies to, or
     * {@code null} if the surface is not associated with a specific sub-range.
     */
    private final LinearlyReferencedRange between;

    /**
     * Creates a new road surface description.
     *
     * @param surfaceType the {@link RoadSurfaceType} for this segment, must not be {@code null}
     * @param between     optional {@link LinearlyReferencedRange} this surface applies to, or
     *                    {@code null} if not linearly referenced
     */
    public OvertureRoadSurface(RoadSurfaceType surfaceType, LinearlyReferencedRange between) {
        this.surfaceType = surfaceType;
        this.between = between;
    }

    /**
     * Returns the surface classification for this segment.
     *
     * @return the {@link RoadSurfaceType}, possibly {@code null} if created that way
     */
    public RoadSurfaceType getSurfaceType() {
        return surfaceType;
    }

    /**
     * Returns whether a surface type has been specified for this segment.
     *
     * @return {@code true} if {@link #getSurfaceType()} is non-{@code null}, {@code false} otherwise
     */
    public boolean hasSurfaceType() {
        return surfaceType != null;
    }

    /**
     * Returns whether a linearly referenced range is associated with this surface.
     *
     * @return {@code true} if {@link #getBetween()} is non-{@code null}, {@code false} otherwise
     */
    public boolean hasBetween() {
        return between != null;
    }

    /**
     * Returns the linearly referenced range this surface applies to.
     *
     * @return the {@link LinearlyReferencedRange}, or {@code null} if not specified
     */
    @Override
    public LinearlyReferencedRange getBetween() {
        return between;
    }

    /**
     * Returns whether this surface is considered paved.
     * <p>
     * This is true for {@link RoadSurfaceType#PAVED}, {@link RoadSurfaceType#ASPHALT},
     * {@link RoadSurfaceType#CONCRETE} and {@link RoadSurfaceType#PAVING_STONES}.
     *
     * @return {@code true} if the surface is paved, {@code false} otherwise
     */
    public boolean isPaved() {
        return surfaceType == RoadSurfaceType.PAVED
                || surfaceType == RoadSurfaceType.ASPHALT
                || surfaceType == RoadSurfaceType.CONCRETE
                || surfaceType == RoadSurfaceType.PAVING_STONES
                || surfaceType == RoadSurfaceType.METAL;
    }

    /**
     * Returns whether this surface is considered unpaved.
     * <p>
     * This is true for {@link RoadSurfaceType#UNPAVED}, {@link RoadSurfaceType#GRAVEL} and
     * {@link RoadSurfaceType#DIRT}.
     *
     * @return {@code true} if the surface is unpaved, {@code false} otherwise
     */
    public boolean isUnpaved() {
        return surfaceType == RoadSurfaceType.UNPAVED
                || surfaceType == RoadSurfaceType.GRAVEL
                || surfaceType == RoadSurfaceType.DIRT;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OvertureRoadSurface that = (OvertureRoadSurface) o;
        return surfaceType == that.surfaceType && Objects.equals(between, that.between);
    }

    @Override
    public int hashCode() {
        return Objects.hash(surfaceType, between);
    }

    /**
     * Returns a string representation containing the surface type and optional linear range,
     * intended mainly for debugging and tests.
     */
    @Override
    public String toString() {
        return "OvertureRoadSurface{" + "surfaceType=" + surfaceType + ", between=" + between + '}';
    }
}
