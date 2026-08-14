package com.graphhopper.reader.overture.parser.filters;

import com.graphhopper.reader.overture.road.segment.OvertureRoadSubclass;

/**
 * Filter for {@link OvertureRoadSubclass} values in Overture road data.
 * <p>
 * Provides granular control over specific road categories (e.g., links, service roads, ramps),
 * allowing for fine-tuned inclusion or exclusion of segments based on their functional role.
 */
public class RoadSubclassFilter extends OvertureFilter<OvertureRoadSubclass> {
    @Override
    public void initializeAllowedValues() {
        // TODO: Maybe we want to initialize with specific values instead of all?
        for (var val : OvertureRoadSubclass.values()) {
            putAllowed(val);
        }
    }
}
