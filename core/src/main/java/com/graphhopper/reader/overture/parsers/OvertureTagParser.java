package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.util.EdgeIteratorState;

/**
 * Converts one aspect of an Overture road segment into encoded values on a graph edge.
 *
 * <p>The Overture counterpart to GraphHopper's {@code TagParser}. It deliberately takes an {@link
 * EdgeIteratorState} rather than OSM's {@code (int edgeId, EdgeIntAccess)} pair: Overture has no
 * artificial via-way edges and no relation flags, and the name parser writes key-values rather than
 * an encoded value. Parsers that genuinely need raw edge storage can reach it through {@link
 * OvertureSegmentContext#getEdgeIntAccess()}.
 *
 * <p>Implementations are instances holding the encoded values they write, resolved once at import
 * setup, so a missing encoded value fails while the pipeline is being assembled rather than on the
 * first edge.
 *
 * <p>Ordering matters where one parser reads back what another wrote — {@code car_average_speed}
 * reads {@code max_speed}, for example. That is expressed declaratively through {@code
 * requiredImportUnits} in {@link OvertureImportRegistry} and resolved by GraphHopper's {@code
 * ImportUnitSorter}, not by the order calls happen to appear in.
 */
public interface OvertureTagParser {

    /**
     * Writes this parser's contribution for one sub-segment.
     *
     * @param edge the graph edge to update
     * @param segment the sub-segment being imported, already split so its properties are uniform
     * @param context per-edge extras such as geometry and the custom-area index
     */
    void handleSegment(
            EdgeIteratorState edge, OvertureRoadSegment segment, OvertureSegmentContext context);
}
