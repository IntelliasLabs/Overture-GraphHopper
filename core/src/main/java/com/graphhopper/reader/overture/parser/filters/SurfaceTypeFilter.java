package com.graphhopper.reader.overture.parser.filters;

import com.graphhopper.reader.overture.road.surface.RoadSurfaceType;

/**
 * Filter for {@link RoadSurfaceType} values within Overture road features.
 * <p>
 * Controls the acceptance of various pavement and surface materials (e.g., asphalt,
 * gravel, dirt), which directly impacts vehicle speed factors and routing preferences.
 */
public class SurfaceTypeFilter extends OvertureFilter<RoadSurfaceType> {
    @Override
    public void initializeAllowedValues() {
        // TODO: Maybe we want to initialize with specific values instead of all?
        for (var val : RoadSurfaceType.values()) {
            putAllowed(val);
        }
    }
}
