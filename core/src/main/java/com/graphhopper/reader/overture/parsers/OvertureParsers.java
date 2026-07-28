package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.DataReaderConfig;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.ImportUnit;
import com.graphhopper.routing.ev.ImportUnitSorter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ordered set of parsers applied to every imported Overture sub-segment.
 *
 * <p>The Overture counterpart to {@code OSMParsers}, minus the relation and turn-restriction lists:
 * Overture has no relations, and its turn restrictions arrive as {@code prohibited_transitions} on the
 * segment itself.
 */
public class OvertureParsers {

    private static final Logger logger = LoggerFactory.getLogger(OvertureParsers.class);

    private final List<OvertureTagParser> segmentParsers;

    public OvertureParsers() {
        this(new ArrayList<>());
    }

    public OvertureParsers(List<OvertureTagParser> segmentParsers) {
        this.segmentParsers = segmentParsers;
    }

    /**
     * @param parser the parser to append; it runs after every parser already added
     * @return this instance for chaining
     */
    public OvertureParsers addSegmentParser(OvertureTagParser parser) {
        segmentParsers.add(parser);
        return this;
    }

    /** @return the parsers in the order they will run */
    public List<OvertureTagParser> getSegmentParsers() {
        return segmentParsers;
    }

    /**
     * Applies every parser to one sub-segment, in order.
     *
     * @param edge the graph edge to update
     * @param segment the sub-segment being imported
     * @param context per-edge extras such as geometry and the custom-area index
     */
    public void handleSegment(
            EdgeIteratorState edge, OvertureRoadSegment segment, OvertureSegmentContext context) {
        for (OvertureTagParser parser : segmentParsers) {
            parser.handleSegment(edge, segment, context);
        }
    }

    /**
     * Assembles the parser pipeline from the encoded values an import actually created.
     *
     * <p>The encoding manager is the only input needed. It already reflects everything {@code
     * graph.encoded_values} asked for, plus whatever GraphHopper added itself, so there is no reason
     * for {@code GraphHopper} to hand out its internal import bookkeeping — and no reason for it to
     * know that an Overture pipeline exists.
     *
     * <p>Parsers run in {@link ImportUnitSorter} order, so one that reads back an encoded value
     * written by another runs after it. GraphHopper's own sorter is reused rather than reimplemented,
     * which also means a cyclic dependency is rejected the same way it is for OSM.
     *
     * <p>Two things are reported: a missing <em>required</em> encoded value fails immediately, and any
     * encoded value present in the graph that Overture has no parser for is logged once. The latter is
     * the only signal that such a value will silently keep its default on every edge — the condition
     * that went unnoticed for twelve of them.
     *
     * @param registry supplies the parser factory per encoded-value name
     * @param lookup the built encoding manager, handed to each parser factory
     * @return the assembled parsers, in execution order
     * @throws IllegalStateException if a required encoded value was not configured
     */
    public static OvertureParsers build(OvertureImportRegistry registry, EncodedValueLookup lookup) {
        return build(registry, lookup, new DataReaderConfig());
    }

    /**
     * Assembles the parser pipeline, letting parsers see the source-agnostic import settings.
     *
     * <p>Settings reach the parser factories through a {@link PMap}, which is the channel OSM already
     * uses to hand {@code date_range_parser_day} to its parsers. Doing the same here means a parser is
     * configured the same way in both pipelines, and {@code OvertureParsers} needs no knowledge of which
     * setting any individual parser cares about.
     *
     * <p>A factory is allowed to return {@code null} for the settings it was given — that is how {@code
     * parse_way_names: false} keeps the street-name parser out of the pipeline rather than running a
     * parser that writes nothing.
     *
     * @param registry supplies the parser factory per encoded-value name
     * @param lookup the built encoding manager, handed to each parser factory
     * @param config the import settings for this import
     * @return the assembled parsers, in execution order
     * @throws IllegalStateException if a required encoded value was not configured
     */
    public static OvertureParsers build(
            OvertureImportRegistry registry, EncodedValueLookup lookup, DataReaderConfig config) {
        requireEncodedValues(registry, lookup);

        // Dependencies are narrowed to the names actually available, otherwise the sorter would reject
        // an edge pointing at a unit that was legitimately left out.
        Set<String> available = new LinkedHashSet<>();
        for (String name : registry.parserNames()) {
            if (registry.alwaysOnParserNames().contains(name) || lookup.hasEncodedValue(name)) {
                available.add(name);
            }
        }

        Map<String, ImportUnit> units = new LinkedHashMap<>();
        for (String name : available) {
            ImportUnit unit = registry.createImportUnit(name);
            List<String> dependencies = unit == null
                    ? List.of()
                    : unit.getRequiredImportUnits().stream().filter(available::contains).toList();
            units.put(
                    name, ImportUnit.create(name, props -> null, null, dependencies.toArray(String[]::new)));
        }

        PMap props = propsFrom(config);

        OvertureParsers parsers = new OvertureParsers();
        for (String name : new ImportUnitSorter(units).sort()) {
            BiFunction<EncodedValueLookup, PMap, OvertureTagParser> factory =
                    registry.createSegmentParser(name);
            if (factory != null && !registry.alwaysOnParserNames().contains(name)) {
                addIfPresent(parsers, factory.apply(lookup, props));
            }
        }
        addAlwaysOnParsers(registry, lookup, props, parsers);

        // Only the concrete encoding manager can enumerate what it holds; EncodedValueLookup cannot.
        // The report is therefore best-effort, which is fine: it is a diagnostic, not a contract.
        if (lookup instanceof EncodingManager encodingManager)
            reportUnfillable(registry, encodingManager);
        logger.info("Overture import assembled {} segment parser(s)", parsers.segmentParsers.size());
        return parsers;
    }

    /**
     * Fails if an encoded value the pipeline requires was never created.
     *
     * <p>Reporting every missing name at once means a misconfigured profile takes one round trip to
     * fix, and — more importantly — a parser is never dropped silently, which is how encoded values
     * came to be unwritten in the first place.
     */
    private static void requireEncodedValues(
            OvertureImportRegistry registry, EncodedValueLookup lookup) {
        List<String> missing = new ArrayList<>();
        for (String name : registry.parserNames()) {
            boolean required = !registry.optionalParserNames().contains(name)
                    && !registry.alwaysOnParserNames().contains(name);
            if (required && !lookup.hasEncodedValue(name)) missing.add(name);
        }
        if (!missing.isEmpty()) {
            missing.sort(String::compareTo);
            throw new IllegalStateException("The Overture import requires encoded values that are not"
                    + " configured: " + missing
                    + ". Add them to graph.encoded_values (see config-overture-osm.yml).");
        }
    }

    /**
     * Logs the encoded values present in the graph that no Overture parser fills.
     *
     * <p>Derived from the encoding manager rather than the declared config string, so it reports what
     * the graph will actually contain. Subnetwork values are excluded: GraphHopper creates one per
     * profile and fills them itself during subnetwork removal.
     */
    private static void reportUnfillable(
            OvertureImportRegistry registry, EncodingManager encodingManager) {
        List<String> unfillable = new ArrayList<>();
        for (EncodedValue encodedValue : encodingManager.getEncodedValues()) {
            String name = encodedValue.getName();
            if (name.endsWith("_subnetwork")) continue;
            if (registry.createSegmentParser(name) == null) unfillable.add(name);
        }
        if (!unfillable.isEmpty()) {
            unfillable.sort(String::compareTo);
            logger.warn(
                    "Overture import cannot fill {} encoded value(s): {}. They keep their default value"
                            + " on every edge, which affects any custom model reading them.",
                    unfillable.size(),
                    unfillable);
        }
    }

    /** Appends parsers that write something other than an encoded value, so nothing declares them. */
    private static void addAlwaysOnParsers(
            OvertureImportRegistry registry,
            EncodedValueLookup lookup,
            PMap props,
            OvertureParsers parsers) {
        for (String name : registry.alwaysOnParserNames()) {
            BiFunction<EncodedValueLookup, PMap, OvertureTagParser> factory =
                    registry.createSegmentParser(name);
            if (factory != null) addIfPresent(parsers, factory.apply(lookup, props));
        }
    }

    /**
     * Translates the import settings a parser might need into the props every factory receives.
     *
     * <p>Only settings a parser can act on appear here. The ones the reader itself applies — geometry
     * simplification, elevation sampling and smoothing — are deliberately absent, because a parser
     * receives the geometry after the reader has already applied them.
     */
    private static PMap propsFrom(DataReaderConfig config) {
        return new PMap()
                .putObject(DefaultOvertureImportRegistry.PARSE_WAY_NAMES, config.isParseWayNames())
                .putObject(DefaultOvertureImportRegistry.PREFERRED_LANGUAGE, config.getPreferredLanguage());
    }

    /** Adds a parser unless its factory declined to build one for the settings it was given. */
    private static void addIfPresent(OvertureParsers parsers, OvertureTagParser parser) {
        if (parser != null) parsers.addSegmentParser(parser);
    }
}
