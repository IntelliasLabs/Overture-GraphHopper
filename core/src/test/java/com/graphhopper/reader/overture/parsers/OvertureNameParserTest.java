package com.graphhopper.reader.overture.parsers;

import static org.junit.jupiter.api.Assertions.*;

import com.graphhopper.reader.overture.names.Bcp47LanguageTag;
import com.graphhopper.reader.overture.names.OvertureNameRule;
import com.graphhopper.reader.overture.names.OvertureNames;
import com.graphhopper.reader.overture.names.Variant;
import com.graphhopper.reader.overture.road.segment.OvertureRoadProperties;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
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

    private OvertureRoadSegment createSegment(
            String primary, Map<Bcp47LanguageTag, String> common, List<OvertureNameRule> rules) {

        OvertureNames names = new OvertureNames(primary, common, rules);

        OvertureRoadProperties properties = new OvertureRoadProperties(
                null, null, null, null, null, null, null, null, null, null, null, null, 0, null, null, null,
                0, null, names);

        return new OvertureRoadSegment(null, null, properties);
    }
}
