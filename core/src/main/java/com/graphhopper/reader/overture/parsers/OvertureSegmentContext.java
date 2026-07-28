package com.graphhopper.reader.overture.parsers;

import com.graphhopper.routing.ev.EdgeIntAccess;
import com.graphhopper.routing.util.AreaIndex;
import com.graphhopper.routing.util.CustomArea;
import com.graphhopper.util.PointList;
import org.jetbrains.annotations.Nullable;

/**
 * Per-edge context that some parsers need beyond the segment itself.
 *
 * <p>This is the Overture counterpart to the "artificial tags" the OSM reader injects into a {@code
 * ReaderWay} — geometry, the custom-area index and so on. Keeping them in a typed carrier rather than
 * a string-keyed map means a parser that needs the area index says so in its constructor instead of
 * discovering a missing tag at runtime.
 *
 * <p>Most parsers ignore this entirely and read only the segment.
 */
public final class OvertureSegmentContext {

    private final PointList geometry;
    private final AreaIndex<CustomArea> areaIndex;
    private final EdgeIntAccess edgeIntAccess;

    /**
     * @param geometry the sub-segment geometry including both end points, 3D when the graph is
     * @param areaIndex the custom-area index, or {@code null} when none was configured
     * @param edgeIntAccess raw edge storage, needed by the upstream GraphHopper parsers that write
     *     through it rather than through an {@code EdgeIteratorState}
     */
    public OvertureSegmentContext(
            PointList geometry,
            @Nullable AreaIndex<CustomArea> areaIndex,
            @Nullable EdgeIntAccess edgeIntAccess) {
        this.geometry = geometry;
        this.areaIndex = areaIndex;
        this.edgeIntAccess = edgeIntAccess;
    }

    /** @return the sub-segment geometry, including both end points */
    public PointList getGeometry() {
        return geometry;
    }

    /** @return the custom-area index, or {@code null} when none was configured */
    @Nullable public AreaIndex<CustomArea> getAreaIndex() {
        return areaIndex;
    }

    /** @return raw edge storage, or {@code null} outside a real import */
    @Nullable public EdgeIntAccess getEdgeIntAccess() {
        return edgeIntAccess;
    }

    /**
     * A context carrying only geometry, for tests and for parsers that need nothing else.
     *
     * @param geometry the sub-segment geometry
     * @return a context with no area index and no edge storage
     */
    public static OvertureSegmentContext of(PointList geometry) {
        return new OvertureSegmentContext(geometry, null, null);
    }
}
