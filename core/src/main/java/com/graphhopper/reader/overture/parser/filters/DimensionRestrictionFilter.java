package com.graphhopper.reader.overture.parser.filters;

import com.graphhopper.reader.overture.access.restriction.scope.containers.DimensionRestriction;

/**
 * Filter for {@link DimensionRestriction} values in Overture data.
 * <p>
 * Manages physical constraints such as height, weight, width, and length,
 * ensuring only supported dimension types are used for routing logic.
 */
public class DimensionRestrictionFilter extends OvertureFilter<DimensionRestriction> {
    @Override
    public void initializeAllowedValues() {
        // TODO: Maybe we want to initialize with specific values instead of all?
        for (var val : DimensionRestriction.values()) {
            putAllowed(val);
        }
    }
}
