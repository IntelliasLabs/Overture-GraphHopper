package com.graphhopper.reader.overture.parser.filters;

import com.graphhopper.reader.overture.road.segment.destination.OvertureDestinationSymbol;

/**
 * Filter for {@link OvertureDestinationSymbol} values from Overture features.
 * <p>
 * Determines which signpost symbols (e.g., shields, pictograms) are allowed
 * to be processed and included in the destination information.
 */
public class DestinationSymbolFilter extends OvertureFilter<OvertureDestinationSymbol> {
    @Override
    public void initializeAllowedValues() {
        // TODO: Maybe we want to initialize with specific values instead of all?
        for (var val : OvertureDestinationSymbol.values()) {
            putAllowed(val);
        }
    }
}
