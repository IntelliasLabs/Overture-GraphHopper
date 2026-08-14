package com.graphhopper.reader.overture.parser.filters;

import com.graphhopper.reader.overture.common.speed.SpeedUnit;

/**
 * Filter for {@link SpeedUnit} values in Overture speed data.
 * <p>
 * Ensures that speed limits and constraints are parsed using recognized
 * units of measurement (e.g., KMH, MPH), preventing conversion errors during routing.
 */
public class SpeedUnitFilter extends OvertureFilter<SpeedUnit> {
    @Override
    public void initializeAllowedValues() {
        // TODO: Maybe we want to initialize with specific values instead of all?
        for (var val : SpeedUnit.values()) {
            putAllowed(val);
        }
    }
}
