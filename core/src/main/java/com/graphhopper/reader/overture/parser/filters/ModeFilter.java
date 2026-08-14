package com.graphhopper.reader.overture.parser.filters;

import com.graphhopper.reader.overture.names.Mode;

/**
 * Filter for {@link Mode} values within the Overture names schema.
 * <p>
 * Controls which name modes (e.g., official, alternative, mnemonic)
 * are permitted to be extracted and stored during the parsing process.
 */
public class ModeFilter extends OvertureFilter<Mode> {
    @Override
    public void initializeAllowedValues() {
        // TODO: Maybe we want to initialize with specific values instead of all?
        for (var val : Mode.values()) {
            putAllowed(val);
        }
    }
}
