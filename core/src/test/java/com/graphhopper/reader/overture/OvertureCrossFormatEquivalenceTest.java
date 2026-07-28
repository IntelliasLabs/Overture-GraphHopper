package com.graphhopper.reader.overture;

import static org.junit.jupiter.api.Assertions.*;

import com.graphhopper.reader.overture.parser.OvertureParser;
import com.graphhopper.reader.overture.parser.parquet.OvertureParquetParser;
import com.graphhopper.reader.overture.road.segment.OvertureRoadProperties;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Holds the GeoJSON and GeoParquet mappings to the same contract.
 *
 * <p>These are two independent ~600-line mappings onto one model, and they used to disagree: the
 * Parquet path passed {@code emptyList()} for routes, destinations, prohibited transitions, width
 * rules, subclass rules, level rules and sources, and {@code 0} for version. Nothing failed, because no
 * test compared the two. On the Lviv extract that silently discarded subclass rules on 1530 of 7601
 * segments, prohibited transitions on 105, routes on 110, level rules on 92, width rules on 45 and
 * destinations on 19.
 *
 * <p>That mattered beyond missing metadata. Width, subclass and level rules are linearly referenced, and
 * {@code SplitPointCollector} splits a segment wherever a property's range starts or ends - so the same
 * area imported from Parquet produced different sub-segments than from GeoJSON.
 *
 * <p>The comparison is per-property population counts over the whole extract rather than a
 * segment-by-segment diff, because the two fixtures are independent exports and need not contain
 * identical feature sets. Counts are the strongest claim that holds regardless.
 */
class OvertureCrossFormatEquivalenceTest {

    private static final File GEOJSON = fixture("parser", "correctGeoJson_CenterOfLviv.geojson");
    private static final File PARQUET = fixture("parquet", "correctGeoJson_CenterOfLviv.parquet");

    private static File fixture(String dir, String name) {
        return Paths.get("src/test/resources/com/graphhopper/reader/overture", dir, name)
                .toFile();
    }

    /**
     * Every property both formats can carry, with how to count a populated one.
     *
     * <p>Deliberately excludes {@code theme}, {@code type} and {@code level}: GeoParquet has no such
     * columns, which {@link #parquetLacksColumnsGeoJsonHas} states as the explicit exception rather than
     * leaving it looking like an oversight.
     */
    private static final Map<String, Function<OvertureRoadProperties, Integer>> COMPARED =
            new TreeMap<>(Map.ofEntries(
                    Map.entry("connectors", p -> size(p.getConnectors())),
                    Map.entry("routes", p -> size(p.getRoutes())),
                    Map.entry("destinations", p -> size(p.getDestinations())),
                    Map.entry("prohibitedTransitions", p -> size(p.getProhibitedTransitions())),
                    Map.entry("surfaces", p -> size(p.getSurfaces())),
                    Map.entry("flags", p -> size(p.getFlags())),
                    Map.entry("speedLimits", p -> size(p.getSpeedLimits())),
                    Map.entry("widthRules", p -> size(p.getWidthRules())),
                    Map.entry("subclassRules", p -> size(p.getSubclassRules())),
                    Map.entry("accessRestrictions", p -> size(p.getAccessRestrictions())),
                    Map.entry("levelRules", p -> size(p.getLevelRules())),
                    Map.entry("sources", p -> size(p.getSources())),
                    Map.entry("version", p -> p.getVersion() > 0 ? 1 : 0)));

    private static int size(List<?> list) {
        return list == null ? 0 : list.size();
    }

    private static Map<String, Integer> populationCounts(List<OvertureRoadSegment> segments) {
        Map<String, Integer> counts = new TreeMap<>();
        COMPARED.keySet().forEach(k -> counts.put(k, 0));
        for (OvertureRoadSegment segment : segments) {
            OvertureRoadProperties props = segment.getProperties();
            if (props == null) continue;
            COMPARED.forEach((name, count) -> {
                if (count.apply(props) > 0) counts.merge(name, 1, Integer::sum);
            });
        }
        return counts;
    }

    @Test
    @DisplayName("Neither format drops a property the other extracts")
    void bothFormatsPopulateTheSameProperties() throws Exception {
        Map<String, Integer> geoJson = populationCounts(OvertureParser.parse(GEOJSON));
        Map<String, Integer> parquet = populationCounts(OvertureParquetParser.parse(PARQUET));

        assertFalse(geoJson.isEmpty(), "GeoJSON fixture produced no segments");
        assertFalse(parquet.isEmpty(), "Parquet fixture produced no segments");

        // The assertion that catches a whole mapping being forgotten: a property populated by one
        // format
        // and never by the other. Absolute counts may differ - these are independent exports - but
        // "thousands here, zero there" is always a mapping bug.
        COMPARED.keySet().forEach(name -> {
            int a = geoJson.get(name);
            int b = parquet.get(name);
            if (a > 0 && b == 0) {
                fail("GeoJSON populates " + name + " on " + a + " segments, Parquet on none."
                        + " The Parquet mapping is dropping this column.");
            }
            if (b > 0 && a == 0) {
                fail("Parquet populates " + name + " on " + b + " segments, GeoJSON on none."
                        + " The GeoJSON mapping is dropping this property.");
            }
        });
    }

    @Test
    @DisplayName("The linearly referenced rules are extracted, because they change segment splitting")
    void rangeScopedRulesAreExtractedFromBothFormats() throws Exception {
        // Singled out from the test above because these three are the ones with a routing consequence:
        // SplitPointCollector splits on their ranges, so dropping them changes the graph, not just the
        // metadata. Asserted on both formats so neither can regress.
        for (Map.Entry<String, List<OvertureRoadSegment>> entry : Map.of(
                        "GeoJSON", OvertureParser.parse(GEOJSON),
                        "Parquet", OvertureParquetParser.parse(PARQUET))
                .entrySet()) {
            Map<String, Integer> counts = populationCounts(entry.getValue());
            for (String rangeScoped : List.of("widthRules", "subclassRules", "levelRules")) {
                assertTrue(
                        counts.get(rangeScoped) > 0,
                        entry.getKey() + " extracts no " + rangeScoped
                                + ", but the Lviv extract contains them; segment splitting will differ");
            }
        }
    }

    @Test
    @DisplayName("prohibited_transitions is extracted, so turn restrictions have data to consume")
    void prohibitedTransitionsAreExtractedFromBothFormats() throws Exception {
        assertTrue(
                populationCounts(OvertureParser.parse(GEOJSON)).get("prohibitedTransitions") > 0,
                "GeoJSON extracts no prohibited transitions");
        assertTrue(
                populationCounts(OvertureParquetParser.parse(PARQUET)).get("prohibitedTransitions") > 0,
                "Parquet extracts no prohibited transitions");
    }

    @Test
    @DisplayName("theme, type and level are absent from GeoParquet, not dropped by the mapping")
    void parquetLacksColumnsGeoJsonHas() throws Exception {
        // The remaining divergence, stated as an intentional exception. If Overture adds these columns
        // this test fails and the mapping should start reading them.
        List<OvertureRoadSegment> parquet = OvertureParquetParser.parse(PARQUET);
        assertFalse(parquet.isEmpty());

        for (OvertureRoadSegment segment : parquet) {
            OvertureRoadProperties props = segment.getProperties();
            assertNull(props.getTheme(), "theme is a partition, not a column - see OvertureSchema");
            assertNull(props.getType(), "type is a partition, not a column - see OvertureSchema");
            assertEquals(0, props.getLevel(), "GeoParquet has level_rules instead of level");
        }
    }
}
