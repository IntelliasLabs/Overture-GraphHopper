package com.graphhopper.reader.overture.parser.filters;

import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelMode;

/**
 * Filter for {@link TravelMode} values in Overture data features.
 * <p>
 * Controls the inclusion of different transportation modes (e.g., pedestrian,
 * motor_vehicle, bicycle) for mode-specific access restrictions and routing.
 */
public class TravelModeFilter extends OvertureFilter<TravelMode> {
    @Override
    public void initializeAllowedValues() {
        // TODO: Maybe we want to initialize with specific values instead of all?
        for (var val : TravelMode.values()) {
            putAllowed(val);
        }
    }
}
