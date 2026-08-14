package com.graphhopper.reader.overture.parser.filters;

import com.graphhopper.reader.overture.access.restriction.scope.containers.Units;

/**
 * Filter for {@link Units} of measurement in Overture data.
 * <p>
 * Determines which measurement units (e.g., meters, feet, kilograms) are
 * accepted when parsing physical dimensions and weight restrictions.
 */
public class UnitsFilter extends OvertureFilter<Units> {
    @Override
    public void initializeAllowedValues() {
        // TODO: Maybe we want to initialize with specific values instead of all?
        for (var val : Units.values()) {
            putAllowed(val);
        }
    }
}
