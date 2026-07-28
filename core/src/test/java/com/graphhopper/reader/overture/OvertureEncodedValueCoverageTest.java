package com.graphhopper.reader.overture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.GHUtility;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Asserts exactly which of the encoded values declared in {@code config-overture-osm.yml} the
 * Overture import actually writes.
 *
 * <p>Why this exists: {@code config-overture-osm.yml} declares 25 encoded values and {@link
 * OvertureReader} fills far fewer. The unfilled ones silently keep their default on every edge, so
 * the only symptom is subtly wrong routing. Nothing failed when that gap opened, which is why it
 * went unnoticed.
 *
 * <p>The assertion is deliberately <b>set equality</b> against {@link #KNOWN_UNFILLED}, not
 * containment. That makes the test fail in both directions: when a working encoded value regresses,
 * and when a new parser lands without updating the list. Treat any change to {@link
 * #KNOWN_UNFILLED} as requiring a comment explaining why.
 *
 * <p><b>Known limitation of the detection method.</b> "Unfilled" is inferred from values: an encoded
 * value equal to its default on every edge is assumed never to have been written. That cannot
 * distinguish "no parser exists" from "a parser ran but the fixture contains no positive cases". This
 * is why the test uses {@link OvertureTestFixtures#richParquetExtract()} rather than a smaller
 * extract — on the Kyiv extract, {@code road_class_link} was falsely reported unfilled purely
 * because that data contains no {@code link} subclass segments. If you add an entry here, first
 * confirm the fixture actually contains data that should produce a non-default value.
 */
class OvertureEncodedValueCoverageTest {

    /**
     * Encoded values declared in the config that the Overture import does not write.
     *
     * <p>Remove an entry when the corresponding parser lands. Adding an entry needs a note on why
     * Overture cannot supply the value.
     */
    private static final Set<String> KNOWN_UNFILLED = new TreeSet<>(Arrays.asList(
            // No Overture source at all: no sac_scale / mtb:scale / lcn-rcn-ncn-icn equivalent.
            "hike_rating",
            "mtb_rating",
            "bike_network",
            // Filled by OvertureFerrySpeedParser, but only on water segments, and this extract is
            // inland - a fixture property rather than a coverage gap. OvertureFerrySpeedParserTest
            // covers the parser directly.
            "ferry_speed",
            // No Overture attribute; would need geometric/topological inference over connectors.
            "roundabout",
            // Derivable from access_restrictions, unimplemented.
            "road_access",
            "foot_road_access",
            "bike_road_access",
            // No Overture source; the *_overture.json custom models neutralise these to 1.
            "bike_priority",
            "foot_priority"));

    private static GraphHopper hopper;

    @BeforeAll
    static void importGraph() {
        hopper = OvertureTestFixtures.overtureHopper(
                OvertureTestFixtures.richParquetExtract(),
                OvertureTestFixtures.CONFIG_ENCODED_VALUES,
                new Profile("car").setCustomModel(GHUtility.loadCustomModelFromJar("car.json")),
                new Profile("foot").setCustomModel(GHUtility.loadCustomModelFromJar("foot_overture.json")),
                new Profile("bike").setCustomModel(GHUtility.loadCustomModelFromJar("bike_overture.json")));
        hopper.importOrLoad();
    }

    @AfterAll
    static void closeGraph() {
        if (hopper != null) hopper.close();
    }

    @Test
    @DisplayName("Every declared encoded value is either written by the import or a known gap")
    void declaredEncodedValuesAreEitherFilledOrKnownUnfilled() {
        BaseGraph graph = hopper.getBaseGraph();
        EncodingManager em = hopper.getEncodingManager();
        assertTrue(graph.getEdges() > 0, "fixture produced no edges - the test proves nothing");

        List<String> declared = declaredEncodedValueNames();
        Map<String, EncodedValue> present = new LinkedHashMap<>();
        Set<String> unfilled = new TreeSet<>();
        for (String name : declared) {
            if (em.hasEncodedValue(name)) present.put(name, em.getEncodedValue(name, EncodedValue.class));
            // Not created at all is a different failure from "created but never written".
            else unfilled.add(name + " (ABSENT from EncodingManager)");
        }

        Map<String, String> defaults = defaultValuesOf(em, present);
        for (Map.Entry<String, EncodedValue> entry : present.entrySet()) {
            if (!isWrittenOnAnyEdge(graph, entry.getValue(), defaults.get(entry.getKey()))) {
                unfilled.add(entry.getKey());
            }
        }

        assertEquals(
                KNOWN_UNFILLED,
                unfilled,
                "Overture encoded-value coverage changed.\n"
                        + "  Newly unfilled (regression): " + difference(unfilled, KNOWN_UNFILLED) + "\n"
                        + "  Newly filled (update KNOWN_UNFILLED): " + difference(KNOWN_UNFILLED, unfilled)
                        + "\n");
    }

    @Test
    @DisplayName("Coverage expectations only name encoded values the config actually declares")
    void knownUnfilledNamesAreAllDeclared() {
        Set<String> declared = new LinkedHashSet<>(declaredEncodedValueNames());
        Set<String> stale = difference(KNOWN_UNFILLED, declared);
        assertTrue(stale.isEmpty(), "KNOWN_UNFILLED names no longer in the config: " + stale);
    }

    /**
     * Reads each encoded value's default off a single untouched edge in a throwaway graph.
     *
     * <p>Deliberately measured rather than hard-coded per type: {@code MaxSpeed} in particular does
     * not default to 0 by intent (its "missing" marker is positive infinity), so assuming zero would
     * misreport it.
     *
     * @return default rendered as {@code "forward|backward"} per encoded-value name
     */
    private static Map<String, String> defaultValuesOf(
            EncodingManager em, Map<String, EncodedValue> evs) {
        Map<String, String> defaults = new LinkedHashMap<>();
        BaseGraph empty = new BaseGraph.Builder(em).create();
        try {
            EdgeIteratorState untouched = empty.edge(0, 1);
            for (Map.Entry<String, EncodedValue> entry : evs.entrySet()) {
                EncodedValue ev = entry.getValue();
                defaults.put(entry.getKey(), read(untouched, ev, false) + "|" + read(untouched, ev, true));
            }
            return defaults;
        } finally {
            empty.close();
        }
    }

    /**
     * @return true if at least one edge holds a value differing from {@code defaultValue}, in either
     *     direction. Equal to the default on every edge of a varied extract is taken as evidence
     *     that nothing ever wrote it.
     */
    private static boolean isWrittenOnAnyEdge(BaseGraph graph, EncodedValue ev, String defaultValue) {
        AllEdgesIterator edges = graph.getAllEdges();
        while (edges.next()) {
            if (!(read(edges, ev, false) + "|" + read(edges, ev, true)).equals(defaultValue)) return true;
        }
        return false;
    }

    /**
     * Reads {@code ev} off {@code edge} as a comparable string.
     *
     * <p>Order matters: {@link EnumEncodedValue} and {@code StringEncodedValue} both extend {@code
     * IntEncodedValueImpl}, and the decimal and boolean implementations are int-backed too, so the
     * narrower types must be tested before {@link IntEncodedValue}.
     */
    private static String read(EdgeIteratorState edge, EncodedValue ev, boolean reverse) {
        if (ev instanceof BooleanEncodedValue b)
            return String.valueOf(reverse ? edge.getReverse(b) : edge.get(b));
        if (ev instanceof DecimalEncodedValue d)
            return String.valueOf(reverse ? edge.getReverse(d) : edge.get(d));
        if (ev instanceof EnumEncodedValue<?> e)
            return String.valueOf(reverse ? edge.getReverse(e) : edge.get(e));
        if (ev instanceof IntEncodedValue i)
            return String.valueOf(reverse ? edge.getReverse(i) : edge.get(i));
        throw new IllegalArgumentException("unhandled encoded value type: " + ev.getClass());
    }

    /** The encoded-value names declared in the config, in declaration order, without their options. */
    static List<String> declaredEncodedValueNames() {
        return Arrays.stream(OvertureTestFixtures.CONFIG_ENCODED_VALUES.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                // strip "|store_two_directions=true" style options
                .map(s -> s.contains("|") ? s.substring(0, s.indexOf('|')) : s)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static Set<String> difference(Set<String> from, Set<String> remove) {
        Set<String> result = new TreeSet<>(from);
        result.removeAll(remove);
        return result;
    }
}
