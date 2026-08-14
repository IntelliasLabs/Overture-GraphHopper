package com.graphhopper.reader.overture.parser.filters;

import com.graphhopper.reader.overture.road.segment.OvertureTheme;

/**
 * Filter for {@link OvertureTheme} values within the Overture Maps dataset.
 * <p>
 * Determines which high-level data themes (e.g., transportation, buildings, places)
 * are permitted for processing, serving as the primary entry point for data selection.
 */
public class ThemeFilter extends OvertureFilter<OvertureTheme> {
    @Override
    public void initializeAllowedValues() {
        // TODO: Maybe we want to initialize with specific values instead of all?
        for (var val : OvertureTheme.values()) {
            putAllowed(val);
        }
    }
}
