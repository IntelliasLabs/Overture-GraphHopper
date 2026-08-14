package com.graphhopper.reader.overture.parser.filters;

import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelReason;

/**
 * Filter for {@link TravelReason} values within Overture access constraints.
 * <p>
 * Manages specific purposes of travel (e.g., delivery, transit, emergency)
 * that trigger or waive certain road restrictions.
 */
public class TravelReasonFilter extends OvertureFilter<TravelReason> {
    @Override
    public void initializeAllowedValues() {
        // TODO: Maybe we want to initialize with specific values instead of all?
        for (var val : TravelReason.values()) {
            putAllowed(val);
        }
    }
}
