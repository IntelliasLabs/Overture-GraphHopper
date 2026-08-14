package com.graphhopper.reader.overture.road.flags;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import com.graphhopper.reader.overture.road.segment.HasBetweenProperty;

import java.util.Objects;

/**
 * Represents the boolean flags associated with an Overture Maps road segment,
 * potentially scoped to a specific linear range.
 *
 * @see <a href="https://docs.overturemaps.org/schema/concepts/by-theme/transportation/segments/#flags">Overture Road Flags Schema</a>
 */
public class OvertureRoadFlags implements HasBetweenProperty {
    private final boolean isBridge;
    private final boolean isTunnel;
    private final boolean isUnderConstruction;
    private final boolean isAbandoned;
    private final boolean isCovered;
    private final boolean isIndoor;
    private final LinearlyReferencedRange between;

    /**
     * Constructs a new OvertureRoadFlags instance.
     *
     * @param isBridge            true if the road is a bridge
     * @param isTunnel            true if the road is a tunnel
     * @param isUnderConstruction true if the road is under construction
     * @param isAbandoned         true if the road is abandoned
     * @param isCovered           true if the road is covered
     * @param isIndoor            true if the road is indoor
     * @param between             the linear range these flags apply to (null implies the entire segment)
     */
    public OvertureRoadFlags(
            boolean isBridge,
            boolean isTunnel,
            boolean isUnderConstruction,
            boolean isAbandoned,
            boolean isCovered,
            boolean isIndoor,
            LinearlyReferencedRange between) {
        this.isBridge = isBridge;
        this.isTunnel = isTunnel;
        this.isUnderConstruction = isUnderConstruction;
        this.isAbandoned = isAbandoned;
        this.isCovered = isCovered;
        this.isIndoor = isIndoor;
        this.between = between;
    }

    /**
     * Checks if the segment is a bridge.
     * @return true if the road is elevated over an obstacle or another route.
     */
    public boolean isBridge() {
        return isBridge;
    }

    /**
     * Checks if the segment is a tunnel.
     * @return true if the segment passes underground or through a mountain.
     */
    public boolean isTunnel() {
        return isTunnel;
    }

    /**
     * Checks if the road is currently under construction.
     * @return true if the segment is not yet traversable due to ongoing work.
     */
    public boolean isUnderConstruction() {
        return isUnderConstruction;
    }

    /**
     * Checks if the road has been abandoned.
     * @return true if the route is no longer maintained or used for transit.
     */
    public boolean isAbandoned() {
        return isAbandoned;
    }

    /**
     * Checks if the road is covered by a structure.
     * @return true if a roof or structure covers the road (e.g., gallery).
     */
    public boolean isCovered() {
        return isCovered;
    }

    /**
     * Checks if the segment is part of an indoor network.
     * @return true if the segment is part of an indoor path network.
     */
    public boolean isIndoor() {
        return isIndoor;
    }

    /**
     * Gets the spatial range for these flags.
     * @return the linear range along the segment where these flags apply, or null if cover the entire segment.
     */
    @Override
    public LinearlyReferencedRange getBetween() {
        return between;
    }

    /**
     * Checks if the road should be skipped during graph processing based on the flags.
     * Note: This does not account for the 'between' range; logic handling partial skips
     * must be handled by the caller.
     *
     * @return true if the road should be ignored (abandoned or under construction)
     */
    public boolean shouldSkip() {
        return isAbandoned || isUnderConstruction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OvertureRoadFlags that = (OvertureRoadFlags) o;
        return isBridge == that.isBridge
                && isTunnel == that.isTunnel
                && isUnderConstruction == that.isUnderConstruction
                && isAbandoned == that.isAbandoned
                && isCovered == that.isCovered
                && isIndoor == that.isIndoor
                && Objects.equals(between, that.between);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                isBridge, isTunnel, isUnderConstruction, isAbandoned, isCovered, isIndoor, between);
    }

    @Override
    public String toString() {
        return "OvertureRoadFlags{" + "isBridge="
                + isBridge + ", isTunnel="
                + isTunnel + ", isUnderConstruction="
                + isUnderConstruction + ", isAbandoned="
                + isAbandoned + ", isCovered="
                + isCovered + ", isIndoor="
                + isIndoor + ", between="
                + between + '}';
    }
}
