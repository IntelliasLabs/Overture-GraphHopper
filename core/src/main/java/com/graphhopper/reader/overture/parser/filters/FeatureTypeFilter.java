package com.graphhopper.reader.overture.parser.filters;

import com.graphhopper.reader.overture.road.segment.OvertureFeatureType;

/**
 * Filter for {@link OvertureFeatureType} values within the Overture schema.
 * <p>
 * Controls the high-level classification of features that the parser will accept,
 * such as road segments, nodes, or other top-level Overture entities.
 */
public class FeatureTypeFilter extends OvertureFilter<OvertureFeatureType> {
    @Override
    public void initializeAllowedValues() {
        // TODO: Maybe we want to initialize with specific values instead of all?
        for (var val : OvertureFeatureType.values()) {
            putAllowed(val);
        }
    }
}
