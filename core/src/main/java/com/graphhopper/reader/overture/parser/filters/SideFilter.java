package com.graphhopper.reader.overture.parser.filters;

import com.graphhopper.reader.overture.names.Side;

/**
 * Filter for {@link Side} values within the Overture schema.
 * <p>
 * Determines which spatial orientations (e.g., left, right, both) are
 * permitted for name extraction or directional restrictions.
 */
public class SideFilter extends OvertureFilter<Side> {
    @Override
    public void initializeAllowedValues() {
        // TODO: Maybe we want to initialize with specific values instead of all?
        for (var val : Side.values()) {
            putAllowed(val);
        }
    }
}
