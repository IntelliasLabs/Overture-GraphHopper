package com.graphhopper.reader.overture.parser.filters;

import com.graphhopper.reader.overture.road.segment.destination.OvertureDestinationLabelType;

/**
 * Filter for {@link OvertureDestinationLabelType} values in Overture features.
 * <p>
 * Manages the types of destination labels (e.g., exit numbers, signpost names)
 * that are permitted for extraction and display during guidance.
 */
public class DestinationLabelTypeFilter extends OvertureFilter<OvertureDestinationLabelType> {

    @Override
    public void initializeAllowedValues() {
        // TODO: Maybe we want to initialize with specific values instead of all?
        for (var val : OvertureDestinationLabelType.values()) {
            putAllowed(val);
        }
    }
}
