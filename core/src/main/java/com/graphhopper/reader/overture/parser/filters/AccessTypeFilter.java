package com.graphhopper.reader.overture.parser.filters;

import com.graphhopper.reader.overture.access.restriction.AccessType;

/**
 * Filter for {@link AccessType} values during Overture data parsing.
 * <p>
 * Determines which access categories (e.g., private, public, restricted)
 * are permitted to be processed and stored in the routing graph.
 */
public class AccessTypeFilter extends OvertureFilter<AccessType> {
    @Override
    public void initializeAllowedValues() {
        // TODO: Maybe we want to initialize with specific values instead of all?
        for (var val : AccessType.values()) {
            putAllowed(val);
        }
    }
}
