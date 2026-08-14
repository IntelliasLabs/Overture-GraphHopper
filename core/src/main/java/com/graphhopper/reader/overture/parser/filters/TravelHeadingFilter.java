package com.graphhopper.reader.overture.parser.filters;

import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelHeading;

/**
 * Filter for {@link TravelHeading} values within Overture constraints.
 * <p>
 * Manages directional headings (e.g., clockwise, forward, north) that
 * define the orientation-specific applicability of a road property or restriction.
 */
public class TravelHeadingFilter extends OvertureFilter<TravelHeading> {
    @Override
    public void initializeAllowedValues() {
        // TODO: Maybe we want to initialize with specific values instead of all?
        for (var val : TravelHeading.values()) {
            putAllowed(val);
        }
    }
}
