package com.graphhopper.reader.overture.parsers;

import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.ImportRegistry;
import com.graphhopper.util.PMap;
import java.util.Set;
import java.util.function.BiFunction;
import org.jetbrains.annotations.Nullable;

/**
 * Maps encoded-value names to the Overture parsers that fill them.
 *
 * <p>Extends GraphHopper's {@link ImportRegistry} so that a single object answers both questions the
 * import pipeline asks: how to create an encoded value (inherited, and delegated to {@code
 * DefaultImportRegistry} so layouts stay byte-compatible with an OSM graph and upstream additions
 * arrive for free) and how to fill it from Overture data.
 */
public interface OvertureImportRegistry extends ImportRegistry {

    /**
     * @param name the encoded-value name
     * @return a factory building the parser that fills {@code name}, or {@code null} when Overture has
     *     no source for it. A {@code null} here is the signal behind the startup report of encoded
     *     values that will keep their default value.
     */
    @Nullable BiFunction<EncodedValueLookup, PMap, OvertureTagParser> createSegmentParser(String name);

    /**
     * @return every name {@link #createSegmentParser} can supply a parser for. Used to assemble a
     *     parser set when the caller has no import-unit map to work from, and to check in tests that
     *     the registry and the coverage expectations agree.
     */
    Set<String> parserNames();

    /**
     * @return names whose parser runs regardless of {@code graph.encoded_values}, because it writes
     *     something other than an encoded value. The street-name parser writes key-values, so no
     *     encoded value declares it.
     */
    Set<String> alwaysOnParserNames();

    /**
     * @return names that may legitimately be absent. Everything else in {@link #parserNames()} is
     *     required, and a caller assembling parsers without it should fail rather than quietly drop the
     *     parser — silently skipping one is the failure mode that left twelve encoded values unwritten.
     *     Optional entries depend on data the reader may not have, such as an elevation provider.
     */
    Set<String> optionalParserNames();
}
