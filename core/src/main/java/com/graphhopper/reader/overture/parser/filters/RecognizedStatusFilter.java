package com.graphhopper.reader.overture.parser.filters;

import com.graphhopper.reader.overture.access.restriction.scope.containers.RecognizedStatus;

/**
 * Filter for {@link RecognizedStatus} values in Overture features.
 * <p>
 * Controls which legal or social recognition statuses (e.g., official,
 * private, informal) are accepted during the data import process.
 */
public class RecognizedStatusFilter extends OvertureFilter<RecognizedStatus> {
    @Override
    public void initializeAllowedValues() {
        // TODO: Maybe we want to initialize with specific values instead of all?
        for (var val : RecognizedStatus.values()) {
            putAllowed(val);
        }
    }
}
