package com.graphhopper.reader.overture.parser.filters;

import com.graphhopper.reader.overture.road.segment.OvertureRoadClass;

/**
 * Filter for {@link OvertureRoadClass} values during data ingestion.
 * <p>
 * Manages the classification of road segments (e.g., motorway, primary, residential),
 * allowing the parser to include or exclude specific road types from the routing graph.
 */
public class RoadClassFilter extends OvertureFilter<OvertureRoadClass> {

    @Override
    public void initializeAllowedValues() {
        // TODO: Maybe we want to initialize with specific values instead of all?
        for (var val : OvertureRoadClass.values()) {
            putAllowed(val);
        }
    }
}
