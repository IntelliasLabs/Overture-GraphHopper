package com.graphhopper.reader.overture.parser.filters;

import com.graphhopper.reader.overture.names.Variant;

/**
 * Filter for {@link Variant} values in Overture naming data.
 * <p>
 * Controls which name variations (e.g., transliteration, short name,
 * historic) are permitted for extraction and storage during the parsing process.
 */
public class VariantFilter extends OvertureFilter<Variant> {
    @Override
    public void initializeAllowedValues() {
        // TODO: Maybe we want to initialize with specific values instead of all?
        for (var val : Variant.values()) {
            putAllowed(val);
        }
    }
}
