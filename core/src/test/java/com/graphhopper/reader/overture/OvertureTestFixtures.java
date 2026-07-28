package com.graphhopper.reader.overture;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.routing.ev.DecimalEncodedValueImpl;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.ev.Hazmat;
import com.graphhopper.routing.ev.MaxSpeed;
import com.graphhopper.routing.ev.RoadClass;
import com.graphhopper.routing.ev.RoadEnvironment;
import com.graphhopper.routing.ev.SimpleBooleanEncodedValue;
import com.graphhopper.routing.ev.Smoothness;
import com.graphhopper.routing.ev.Surface;
import com.graphhopper.routing.ev.TrackType;
import com.graphhopper.routing.util.EncodingManager;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Shared setup for Overture reader tests.
 *
 * <p>Exists to remove three copies of the encoded-value string, two copies of the minimal
 * {@link EncodingManager} builder, and four near-identical data-reader initializer lambdas. Prefer
 * adding to this class over copying setup into a new test.
 */
public final class OvertureTestFixtures {

    private OvertureTestFixtures() {}

    /**
     * The {@code graph.encoded_values} list from {@code config-overture-osm.yml}, verbatim.
     *
     * <p>This is the single source of truth for tests. {@code OvertureTestFixturesTest} asserts it
     * still matches the config file, so the two cannot drift apart silently.
     *
     * <p>Note that Overture does not currently fill every value declared here — see {@code
     * OvertureEncodedValueCoverageTest#KNOWN_UNFILLED} for the authoritative list of gaps.
     */
    public static final String CONFIG_ENCODED_VALUES =
            "car_access, car_average_speed|store_two_directions=true, road_access, road_class, "
                    + "road_class_link, surface, foot_access, foot_average_speed, bike_access, bike_priority, "
                    + "bike_average_speed|store_two_directions=true, bike_network, road_environment, roundabout, "
                    + "max_speed, smoothness, hazmat, track_type, hike_rating, foot_priority, country, "
                    + "foot_road_access, mtb_rating, bike_road_access, ferry_speed";

    /** Path to the config file {@link #CONFIG_ENCODED_VALUES} mirrors. Tests run with cwd {@code core/}. */
    public static final Path CONFIG_OVERTURE_OSM = Paths.get("..", "config-overture-osm.yml");

    /** Directory holding the GeoJSON fixtures. */
    private static final Path PARSER_FIXTURES =
            Paths.get("src/test/resources/com/graphhopper/reader/overture/parser");

    /** Directory holding the Parquet fixtures. */
    private static final Path PARQUET_FIXTURES =
            Paths.get("src/test/resources/com/graphhopper/reader/overture/parquet");

    /**
     * A small Kyiv extract (~136 KB) with enough variety to exercise most encoded values while
     * staying well inside surefire's {@code -Xmx190m} cap. Prefer this over the Lviv (1.9 MB) or
     * Berlin (1.6 MB) extracts for anything that builds a full graph under surefire.
     */
    public static File smallParquetExtract() {
        return PARQUET_FIXTURES.resolve("correctGeoJson_CenterOfKyiv.parquet").toFile();
    }

    /**
     * A Lviv extract (~1.9 MB, ~7600 segments) — the richest fixture available.
     *
     * <p>Needed by coverage-style tests because the smaller extracts lack whole categories of data:
     * the Kyiv extract contains no {@code link} subclass segments at all, so {@code road_class_link}
     * is legitimately false on every edge there and cannot be distinguished from never being written.
     * Lviv has 11 link segments plus {@code cycle_crossing} and {@code alley} subclasses.
     *
     * <p>Heavier than {@link #smallParquetExtract()}; verify it fits surefire's {@code -Xmx190m}
     * before using it in a new test.
     */
    public static File richParquetExtract() {
        return PARQUET_FIXTURES.resolve("correctGeoJson_CenterOfLviv.parquet").toFile();
    }

    /** A 5-edge / 6-node GeoJSON extract, the cheapest fixture that still produces a routable graph. */
    public static File tinyGeoJsonExtract() {
        return PARSER_FIXTURES.resolve("correctLittleSegment.geojson").toFile();
    }

    /**
     * Every encoded value {@link OvertureReader} writes today, built directly rather than through
     * {@code DefaultImportRegistry}.
     *
     * <p>Use this for unit tests that construct an {@link OvertureReader} against a bare {@code
     * BaseGraph}. Tests that need the values a real import would produce should go through {@link
     * #overtureHopper} instead, so bit layouts match production.
     *
     * <p>Keep this in step with the reader: it resolves all of these up front and fails fast if any
     * is absent, so omitting one makes every reader test fail rather than silently skip a parser.
     */
    public static EncodingManager minimalEncodingManager() {
        return minimalEncodingManagerWith();
    }

    /**
     * {@link #minimalEncodingManager()} plus {@code extra}.
     *
     * <p>For the optional encoded values a normal import leaves out - {@code bus_access}, {@code
     * country}, the slope values - so a test can add just the one it is about without restating the
     * required set.
     *
     * @param extra encoded values to add on top of the minimal set
     * @return an encoding manager holding the minimal set and {@code extra}
     */
    public static EncodingManager minimalEncodingManagerWith(EncodedValue... extra) {
        EncodingManager.Builder builder = minimalBuilder();
        for (EncodedValue encodedValue : extra) {
            builder.add(encodedValue);
        }
        return builder.build();
    }

    private static EncodingManager.Builder minimalBuilder() {
        return new EncodingManager.Builder()
                // MaxSpeed.create() rather than a hand-rolled decimal: its "missing means infinity"
                // behaviour is part of what the reader relies on.
                .add(MaxSpeed.create())
                .add(new SimpleBooleanEncodedValue("car_access", true))
                .add(new SimpleBooleanEncodedValue("bike_access", true))
                .add(new SimpleBooleanEncodedValue("foot_access", true))
                .add(new DecimalEncodedValueImpl("car_average_speed", 7, 2, true))
                .add(new DecimalEncodedValueImpl("bike_average_speed", 4, 2, true))
                .add(new DecimalEncodedValueImpl("foot_average_speed", 4, 1, true))
                .add(new EnumEncodedValue<>("road_class", RoadClass.class))
                .add(new SimpleBooleanEncodedValue("road_class_link", true))
                .add(new EnumEncodedValue<>("surface", Surface.class))
                .add(new EnumEncodedValue<>("smoothness", Smoothness.class))
                .add(new EnumEncodedValue<>("track_type", TrackType.class))
                .add(new EnumEncodedValue<>("road_environment", RoadEnvironment.class))
                .add(new EnumEncodedValue<>("hazmat", Hazmat.class));
    }

    /**
     * A {@link GraphHopper} wired to read {@code dataFile} through {@link OvertureReader}.
     *
     * <p>Returned <em>unimported</em> so callers can still add CH/LM profiles or tweak config;
     * call {@link GraphHopper#importOrLoad()} yourself. The graph location is derived from the
     * fixture name so concurrently-running tests do not share a cache directory.
     */
    public static GraphHopper overtureHopper(
            File dataFile, String encodedValues, Profile... profiles) {
        GraphHopper hopper = new GraphHopper();
        hopper.setEncodedValuesString(encodedValues);
        hopper.setDataFile(dataFile.getAbsolutePath());
        hopper.setGraphHopperLocation(graphLocationFor(dataFile));
        hopper.setProfiles(profiles);
        // The lambda resolves the encoding manager lazily, at import time, so it is safe to install
        // it before init(). setEncodedValueLookup is the side-channel every Overture caller must
        // remember; see DataReader in the refactor plan for why it should move onto the interface.
        hopper.setDataReaderInitializer(context -> new OvertureReader(context.getBaseGraph())
                .setEncodedValueLookup(context.getEncodingManager())
                .setFile(context.getSourceFile()));
        return hopper;
    }

    /** A per-fixture graph cache directory under {@code target/}, safe for parallel test runs. */
    public static String graphLocationFor(File dataFile) {
        String sanitized = dataFile.getName().replaceAll("[^A-Za-z0-9._-]", "_");
        return Paths.get("target", "gh-overture-test", sanitized).toString();
    }
}
