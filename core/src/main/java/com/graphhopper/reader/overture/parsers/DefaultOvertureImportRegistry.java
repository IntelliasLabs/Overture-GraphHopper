package com.graphhopper.reader.overture.parsers;

import static java.util.Map.entry;

import com.graphhopper.routing.ev.BusAccess;
import com.graphhopper.routing.ev.Country;
import com.graphhopper.routing.ev.DefaultImportRegistry;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.FerrySpeed;
import com.graphhopper.routing.ev.Hazmat;
import com.graphhopper.routing.ev.ImportRegistry;
import com.graphhopper.routing.ev.ImportUnit;
import com.graphhopper.routing.ev.MaxSpeed;
import com.graphhopper.routing.ev.RoadClass;
import com.graphhopper.routing.ev.RoadEnvironment;
import com.graphhopper.routing.ev.Smoothness;
import com.graphhopper.routing.ev.Surface;
import com.graphhopper.routing.ev.TrackType;
import com.graphhopper.routing.ev.VehicleAccess;
import com.graphhopper.routing.ev.VehicleSpeed;
import com.graphhopper.util.PMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import org.jetbrains.annotations.Nullable;

/**
 * The Overture import registry: which encoded values Overture can fill, and with what.
 *
 * <p>This is the file to read to understand Overture's coverage. Anything absent from {@link
 * #OVERTURE_PARSERS} is reported at startup as keeping its default value.
 *
 * <p>Encoded-value <em>creation</em> is delegated to {@link DefaultImportRegistry} rather than
 * re-declared here. That keeps bit layouts identical to an OSM graph built from the same {@code
 * graph.encoded_values}, means new upstream encoded values are understood without touching this
 * class, and removes a second copy of the truth that could drift.
 *
 * <p>What is <em>not</em> delegated is the dependency table: Overture's parsers read different things
 * than OSM's, so {@code requiredImportUnits} is overridden where the two genuinely differ.
 */
public class DefaultOvertureImportRegistry implements OvertureImportRegistry {

    /** Supplies encoded-value definitions, so they stay identical to the OSM pipeline's. */
    private final ImportRegistry encodedValueLayout;

    public DefaultOvertureImportRegistry() {
        this(new DefaultImportRegistry());
    }

    DefaultOvertureImportRegistry(ImportRegistry encodedValueLayout) {
        this.encodedValueLayout = encodedValueLayout;
    }

    /** Synthetic name for the street-name parser, which writes key-values rather than encoded values. */
    static final String STREET_NAME = "overture_street_name";

    /**
     * Props key carrying {@code datareader.preferred_language} to the street-name parser.
     *
     * <p>Declared above {@link #OVERTURE_PARSERS} because a static field referenced by simple name from
     * that initializer must be declared first, even inside a lambda body.
     */
    public static final String PREFERRED_LANGUAGE = "preferred_language";

    /** Props key carrying whether way names should be parsed at all. */
    public static final String PARSE_WAY_NAMES = "parse_way_names";

    /**
     * Bus access.
     *
     * <p>Upstream owns this value through {@link BusAccess} rather than the {@code VehicleAccess}
     * family that car, bike and foot use, so it is delegated like everything else - only the parser and
     * the dependency edge are ours. Aliased here so {@link #OVERTURE_PARSERS} can key on it, which
     * requires it to precede that initializer.
     */
    static final String BUS_ACCESS = BusAccess.KEY;

    /**
     * Dependency overrides for the Overture pipeline. A name absent here inherits the OSM dependencies.
     *
     * <p>These exist because a parser reads an encoded value another parser wrote, so the sorter must
     * order them. Overture's set differs from OSM's in both directions.
     */
    private static final Map<String, List<String>> OVERTURE_DEPENDENCIES = Map.ofEntries(
            // The Overture car speed parser reads the posted limit back off the edge instead of
            // re-deriving it, so max_speed must be written first. This is a real ordering constraint,
            // and the only one in the Overture pipeline today.
            entry(VehicleSpeed.key("car"), List.of(MaxSpeed.KEY)),
            // OSM's CarAccessParser reads the roundabout encoded value to infer implied oneways.
            // Overture states direction explicitly via when.heading, and has no roundabout attribute
            // at all, so that dependency would order a parser that never runs.
            entry(VehicleAccess.key("car"), List.of()),
            // OSM's bike and foot speed parsers read ferry_speed. The Overture speed parsers detect
            // ferries themselves through OvertureFerryParser and never read the encoded value, so
            // ordering against OvertureFerrySpeedParser buys nothing.
            entry(VehicleSpeed.key("bike"), List.of()),
            entry(VehicleSpeed.key("foot"), List.of()),
            // Upstream's bus_access uses ModeAccessParser, which reads roundabout to infer implied
            // oneways. Dropped for the same reason as car_access: Overture states direction explicitly
            // through when.heading and has no roundabout attribute to populate.
            entry(BUS_ACCESS, List.of()));

    /**
     * Parser factories keyed by encoded-value name. Absence means "Overture has no source for this".
     *
     * <p>Bear in mind when adding an entry: the encoded values a parser writes are resolved here, at
     * assembly time, so a missing one throws while the pipeline is being built rather than on the
     * first edge.
     *
     * <p>One absence is worth explaining, because it looks like it only needs wiring up. {@code
     * car_temporal_access} and its bike and foot siblings are <em>evaluated</em> values upstream:
     * {@code OSMTemporalAccessParser} resolves a condition such as {@code no @ (Mo-Fr 07:00-09:00)}
     * against {@code datareader.date_range_parser_day} and stores YES when access is permitted on that
     * date. Overture's {@code when.during} is an unparsed interval expression, so filling these would
     * mean evaluating it against the same reference date. Reporting merely "this segment has some
     * temporal rule" as YES would mark restricted roads as open, which is worse than leaving the value
     * MISSING.
     */
    private static final Map<String, BiFunction<EncodedValueLookup, PMap, OvertureTagParser>>
            OVERTURE_PARSERS = Map.ofEntries(
                    entry(
                            VehicleAccess.key("car"),
                            (lookup, props) -> new OvertureCarAccessParser(
                                    lookup.getBooleanEncodedValue(VehicleAccess.key("car")))),
                    entry(
                            VehicleAccess.key("bike"),
                            (lookup, props) -> new OvertureBikeAccessParser(
                                    lookup.getBooleanEncodedValue(VehicleAccess.key("bike")))),
                    entry(
                            VehicleAccess.key("foot"),
                            (lookup, props) -> new OvertureFootAccessParser(
                                    lookup.getBooleanEncodedValue(VehicleAccess.key("foot")))),
                    entry(
                            BUS_ACCESS,
                            (lookup, props) ->
                                    new OvertureBusAccessParser(lookup.getBooleanEncodedValue(BUS_ACCESS))),
                    entry(
                            VehicleSpeed.key("car"),
                            (lookup, props) -> new OvertureCarAverageSpeedParser(
                                    lookup.getDecimalEncodedValue(VehicleSpeed.key("car")),
                                    lookup.getDecimalEncodedValue(MaxSpeed.KEY))),
                    entry(
                            VehicleSpeed.key("bike"),
                            (lookup, props) -> new OvertureBikeAverageSpeedParser(
                                    lookup.getDecimalEncodedValue(VehicleSpeed.key("bike")))),
                    entry(
                            VehicleSpeed.key("foot"),
                            (lookup, props) -> new OvertureFootAverageSpeedParser(
                                    lookup.getDecimalEncodedValue(VehicleSpeed.key("foot")))),
                    entry(
                            MaxSpeed.KEY,
                            (lookup, props) ->
                                    new OvertureMaxSpeedParser(lookup.getDecimalEncodedValue(MaxSpeed.KEY))),
                    entry(
                            RoadClass.KEY,
                            (lookup, props) -> new OvertureRoadClassParser(
                                    lookup.getEnumEncodedValue(RoadClass.KEY, RoadClass.class))),
                    entry(
                            "road_class_link",
                            (lookup, props) -> new OvertureRoadClassLinkParser(
                                    lookup.getBooleanEncodedValue("road_class_link"))),
                    entry(
                            RoadEnvironment.KEY,
                            (lookup, props) -> new OvertureRoadEnvironmentParser(
                                    lookup.getEnumEncodedValue(RoadEnvironment.KEY, RoadEnvironment.class))),
                    entry(
                            Surface.KEY,
                            (lookup, props) -> new OvertureRoadSurfaceParser(
                                    lookup.getEnumEncodedValue(Surface.KEY, Surface.class))),
                    entry(
                            Smoothness.KEY,
                            (lookup, props) -> new OvertureSmoothnessParser(
                                    lookup.getEnumEncodedValue(Smoothness.KEY, Smoothness.class))),
                    entry(
                            TrackType.KEY,
                            (lookup, props) -> new OvertureTrackTypeParser(
                                    lookup.getEnumEncodedValue(TrackType.KEY, TrackType.class))),
                    entry(
                            Hazmat.KEY,
                            (lookup, props) ->
                                    new OvertureHazmatParser(lookup.getEnumEncodedValue(Hazmat.KEY, Hazmat.class))),
                    // Overture marks a ferry through the segment subtype but carries no crossing
                    // duration, so the speed falls back to the edge length, as it does for OSM.
                    entry(
                            FerrySpeed.KEY,
                            (lookup, props) -> new OvertureFerrySpeedParser(
                                    lookup.getDecimalEncodedValue(FerrySpeed.KEY))),
                    // Not an Overture attribute: resolved from the custom-area index the import
                    // pipeline supplies, exactly as the OSM reader does it.
                    entry(
                            Country.KEY,
                            (lookup, props) -> new OvertureCountryParser(
                                    lookup.getEnumEncodedValue(Country.KEY, Country.class))),
                    // Street names are key-values rather than an encoded value, so there is no encoded
                    // value name to key this on. It is registered under a synthetic name and pulled in
                    // unconditionally by OvertureParsers.
                    //
                    // The two naming settings arrive through props, the same channel OSM uses for
                    // date_range_parser_day. Returning null for parse_way_names=false leaves the parser
                    // out of the pipeline entirely rather than running a parser that writes nothing.
                    entry(
                            STREET_NAME,
                            (lookup, props) -> props.getBool(PARSE_WAY_NAMES, true)
                                    ? new OvertureNameParser(props.getString(PREFERRED_LANGUAGE, ""))
                                    : null));

    @Override
    public ImportUnit createImportUnit(String name) {
        ImportUnit base = encodedValueLayout.createImportUnit(name);
        if (base == null) return null;

        List<String> dependencies =
                OVERTURE_DEPENDENCIES.getOrDefault(name, base.getRequiredImportUnits());
        // The OSM tag parser is dropped deliberately: an Overture import must not run OSM parsers,
        // which expect tags that do not exist here. Passing null makes buildOSMParsers produce an
        // empty parser list, so "no OSM parser touches Overture data" is structural rather than
        // accidental. The encoded value itself is still created by the delegate above.
        return ImportUnit.create(
                name, base.getCreateEncodedValue(), null, dependencies.toArray(String[]::new));
    }

    @Override
    @Nullable public BiFunction<EncodedValueLookup, PMap, OvertureTagParser> createSegmentParser(String name) {
        return OVERTURE_PARSERS.get(name);
    }

    @Override
    public Set<String> parserNames() {
        return OVERTURE_PARSERS.keySet();
    }

    @Override
    public Set<String> alwaysOnParserNames() {
        return Set.of(STREET_NAME);
    }

    @Override
    public Set<String> optionalParserNames() {
        return Set.of(
                // Needs the custom-area index, which a bare reader may not have been given.
                Country.KEY,
                // Only meaningful on an extract containing water segments. Any profile whose custom
                // model limits ferries to ferry_speed must declare it - GraphHopper rejects the
                // import otherwise - so requiring it here would only penalise imports with no
                // ferries to describe.
                FerrySpeed.KEY,
                // Only relevant to a bus profile. Required-by-default would break every import that
                // does not declare it, which is all of them unless a bus profile is configured.
                BUS_ACCESS);
    }
}
