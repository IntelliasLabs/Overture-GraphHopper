package com.graphhopper.reader.overture.parsers;

import static org.junit.jupiter.api.Assertions.*;

import com.graphhopper.reader.overture.names.Bcp47LanguageTag;
import com.graphhopper.reader.overture.names.OvertureNameRule;
import com.graphhopper.reader.overture.names.OvertureNames;
import com.graphhopper.reader.overture.names.Variant;
import com.graphhopper.reader.overture.road.segment.OvertureRoadProperties;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.util.EdgeIteratorState;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OvertureNameParserTest {

    @Test
    void testParsePrimaryName() {
        OvertureRoadSegment segment = createSegment("Main Street", null, null);

        String result = OvertureNameParser.parsePrimaryName(segment);

        assertEquals("Main Street", result);
    }

    @Test
    void testParsePrimaryName_Empty() {
        OvertureRoadSegment segment = createSegment(null, null, null);

        String result = OvertureNameParser.parsePrimaryName(segment);

        assertEquals("", result);
    }

    @Test
    void testParseCommonNames() {
        Bcp47LanguageTag enTag = Bcp47LanguageTag.parse("en");
        Bcp47LanguageTag deTag = Bcp47LanguageTag.parse("de");
        Map<Bcp47LanguageTag, String> commonMap = Map.of(
                enTag, "Main St",
                deTag, "Hauptstraße");

        OvertureRoadSegment segment = createSegment("Main St", commonMap, null);

        Map<Bcp47LanguageTag, String> result = OvertureNameParser.parseCommonNames(segment);

        assertEquals(2, result.size());
        assertEquals("Main St", result.get(enTag));
        assertEquals("Hauptstraße", result.get(deTag));
    }

    @Test
    void testParseCommonNames_Empty() {
        OvertureRoadSegment segment = createSegment("Main St", null, null);

        Map<Bcp47LanguageTag, String> result = OvertureNameParser.parseCommonNames(segment);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseNameRules() {
        OvertureNameRule rule = new OvertureNameRule(
                Variant.ALTERNATE, Bcp47LanguageTag.parse("en"), null, "Old Road", null, null);
        OvertureRoadSegment segment = createSegment("New Road", null, List.of(rule));

        List<OvertureNameRule> result = OvertureNameParser.parseNameRules(segment);

        assertEquals(1, result.size());
        assertEquals("Old Road", result.getFirst().getValue());
        assertEquals(Variant.ALTERNATE, result.getFirst().getVariant());
    }

    @Test
    void testParseNameRules_Empty() {
        OvertureRoadSegment segment = createSegment("Main St", null, null);

        List<OvertureNameRule> result = OvertureNameParser.parseNameRules(segment);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseName_usesPrimaryNameWhenNoLanguagePreferred() {
        Map<Bcp47LanguageTag, String> common = Map.of(Bcp47LanguageTag.parse("de"), "Hauptstraße");

        assertEquals("Main Street", nameStoredFor(createSegment("Main Street", common, null), ""));
    }

    @Test
    void testParseName_prefersCommonNameInRequestedLanguage() {
        Map<Bcp47LanguageTag, String> common = Map.of(
                Bcp47LanguageTag.parse("en"), "Main Street",
                Bcp47LanguageTag.parse("de"), "Hauptstraße");

        OvertureRoadSegment segment = createSegment("Головна вулиця", common, null);

        assertEquals("Hauptstraße", nameStoredFor(segment, "de"));
        assertEquals("Main Street", nameStoredFor(segment, "en"));
    }

    @Test
    void testParseName_matchesOnLanguageSubtagOnly() {
        // en-GB is still English, exactly as OSM's name:en would be preferred for
        // preferred_language=en.
        Map<Bcp47LanguageTag, String> common = Map.of(Bcp47LanguageTag.parse("en-GB"), "High Street");

        assertEquals("High Street", nameStoredFor(createSegment("Hoofdstraat", common, null), "en"));
    }

    @Test
    void testParseName_fallsBackToPrimaryNameWhenLanguageAbsent() {
        Map<Bcp47LanguageTag, String> common = Map.of(Bcp47LanguageTag.parse("de"), "Hauptstraße");

        assertEquals("Hoofdstraat", nameStoredFor(createSegment("Hoofdstraat", common, null), "fr"));
    }

    /** Runs the parser against a real edge and returns the street name it stored. */
    private String nameStoredFor(OvertureRoadSegment segment, String preferredLanguage) {
        EncodingManager em = new EncodingManager.Builder().build();
        BaseGraph graph = new BaseGraph.Builder(em).create();
        graph.getNodeAccess().setNode(0, 50.0, 30.0);
        graph.getNodeAccess().setNode(1, 50.1, 30.1);
        EdgeIteratorState edge = graph.edge(0, 1).setDistance(100);

        new OvertureNameParser(preferredLanguage).handleSegment(edge, segment, null);

        return edge.getName();
    }

    private OvertureRoadSegment createSegment(
            String primary, Map<Bcp47LanguageTag, String> common, List<OvertureNameRule> rules) {

        OvertureNames names = new OvertureNames(primary, common, rules);

        OvertureRoadProperties properties = new OvertureRoadProperties(
                null, null, null, null, null, null, null, null, null, null, null, null, 0, null, null, null,
                0, null, names);

        return new OvertureRoadSegment(null, null, properties);
    }
}
