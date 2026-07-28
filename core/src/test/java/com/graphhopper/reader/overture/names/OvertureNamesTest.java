package com.graphhopper.reader.overture.names;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.*;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OvertureNamesTest {

    @Test
    void testConstructorAndGetters() {
        Map<Bcp47LanguageTag, String> common = new HashMap<>();
        common.put(Bcp47LanguageTag.parse("uk"), "Головна вулиця");
        common.put(Bcp47LanguageTag.parse("en"), "Main Street");

        OvertureNames names = new OvertureNames("Main St", common, null);

        assertEquals("Main St", names.getPrimary());
        assertNotNull(names.getCommon());
        assertEquals(2, names.getCommon().size());
        assertEquals("Головна вулиця", names.getCommon().get(Bcp47LanguageTag.parse("uk")));
        assertEquals("Main Street", names.getCommon().get(Bcp47LanguageTag.parse("en")));
    }

    @Test
    void testGetCommon() {
        Map<Bcp47LanguageTag, String> common = new HashMap<>();
        common.put(Bcp47LanguageTag.parse("uk"), "Головна вулиця");
        common.put(Bcp47LanguageTag.parse("en"), "Main Street");

        OvertureNames names = new OvertureNames("Main St", common, null);

        Map<Bcp47LanguageTag, String> retrieved = names.getCommon();
        assertNotNull(retrieved);
        assertEquals(2, retrieved.size());
    }

    @Test
    void testNullCommon() {
        OvertureNames names = new OvertureNames("Main St", emptyMap(), null);

        assertEquals("Main St", names.getPrimary());
        assertNotNull(names.getCommon());
    }

    @Test
    void testEmptyCommon() {
        OvertureNames names = new OvertureNames("Main St", new HashMap<>(), null);

        assertEquals("Main St", names.getPrimary());
        assertNotNull(names.getCommon());
        assertTrue(names.getCommon().isEmpty());
    }

    @Test
    void testNullPrimary() {
        Map<Bcp47LanguageTag, String> common = new HashMap<>();
        common.put(Bcp47LanguageTag.parse("uk"), "Головна вулиця");

        OvertureNames names = new OvertureNames(null, common, null);

        assertNull(names.getPrimary());
        assertNotNull(names.getCommon());
    }

    @Test
    void testInvalidLanguageTagFiltered() {
        Map<Bcp47LanguageTag, String> common = new HashMap<>();
        common.put(Bcp47LanguageTag.parse("uk"), "Головна вулиця");

        OvertureNames names = new OvertureNames("Main St", common, null);

        assertEquals(1, names.getCommon().size());
        assertEquals("Головна вулиця", names.getCommon().get(Bcp47LanguageTag.parse("uk")));
    }

    @Test
    void testEqualsAndHashCode() {
        Map<Bcp47LanguageTag, String> common1 = new HashMap<>();
        common1.put(Bcp47LanguageTag.parse("uk"), "Головна вулиця");

        Map<Bcp47LanguageTag, String> common2 = new HashMap<>();
        common2.put(Bcp47LanguageTag.parse("uk"), "Головна вулиця");

        OvertureNames names1 = new OvertureNames("Main St", common1, null);
        OvertureNames names2 = new OvertureNames("Main St", common2, null);
        OvertureNames names3 = new OvertureNames("Other St", common1, null);

        assertEquals(names1, names2);
        assertEquals(names1.hashCode(), names2.hashCode());
        assertNotEquals(names1, names3);
    }

    @Test
    void testEqualsNull() {
        OvertureNames names = new OvertureNames("Main St", null, null);
        assertNotEquals(null, names);
    }

    @Test
    void testEqualsDifferentType() {
        OvertureNames names = new OvertureNames("Main St", null, null);
        assertNotEquals("not a names object", names);
    }

    @Test
    void testToString() {
        OvertureNames names = new OvertureNames("Main St", null, null);

        String str = names.toString();
        assertTrue(str.contains("Main St"));
        assertTrue(str.contains("OvertureNames"));
    }

    @Test
    void testConstructorWithRules() {
        OvertureNameRule rule = new OvertureNameRule(
                Variant.ALTERNATE, Bcp47LanguageTag.parse("hr"), null, "Bila kuća", null, null);

        OvertureNames names = new OvertureNames("White House", null, List.of(rule));

        assertEquals("White House", names.getPrimary());
        assertNotNull(names.getRules());
        assertEquals(1, names.getRules().size());
        assertEquals(rule, names.getRules().getFirst());
    }

    @Test
    void testRulesWithAllFields() {
        OvertureNameRule rule = new OvertureNameRule(
                Variant.OFFICIAL,
                Bcp47LanguageTag.parse("en"),
                null,
                "Highway 101",
                new LinearlyReferencedRange(0.0, 0.5),
                Side.LEFT);

        OvertureNames names = new OvertureNames("US-101", null, List.of(rule));

        OvertureNameRule retrieved = names.getRules().getFirst();
        assertEquals(Variant.OFFICIAL, retrieved.getVariant());
        assertEquals("Highway 101", retrieved.getValue());
        assertEquals(0.0, retrieved.getBetween().getStart());
        assertEquals(0.5, retrieved.getBetween().getEnd());
        assertEquals(Side.LEFT, retrieved.getSide());
    }

    @Test
    void testNullRules() {
        OvertureNames names = new OvertureNames("Main St", emptyMap(), emptyList());
        assertNotNull(names.getRules());
    }

    @Test
    void testEmptyRules() {
        OvertureNames names = new OvertureNames("Main St", emptyMap(), emptyList());

        assertNotNull(names.getRules());
        assertTrue(names.getRules().isEmpty());
    }

    @Test
    void testEqualsWithRules() {
        OvertureNameRule rule =
                new OvertureNameRule(Variant.ALTERNATE, null, null, "Alt Name", null, null);

        OvertureNames names1 = new OvertureNames("Main St", emptyMap(), List.of(rule));
        OvertureNames names2 = new OvertureNames("Main St", emptyMap(), List.of(rule));
        OvertureNames names3 = new OvertureNames("Main St", emptyMap(), emptyList());

        assertEquals(names1, names2);
        assertEquals(names1.hashCode(), names2.hashCode());
        assertNotEquals(names1, names3);
    }

    @Test
    void testToStringWithRules() {
        OvertureNameRule rule = new OvertureNameRule(Variant.SHORT, null, null, "Main", null, null);
        OvertureNames names = new OvertureNames("Main St", null, List.of(rule));

        String str = names.toString();
        assertTrue(str.contains("rules"));
    }

    @Test
    void testFullConstructorWithAllFields() {
        Map<Bcp47LanguageTag, String> common = new HashMap<>();
        common.put(Bcp47LanguageTag.parse("en"), "Main Street");
        common.put(Bcp47LanguageTag.parse("de"), "Hauptstraße");

        OvertureNameRule rule1 = new OvertureNameRule(Variant.ALTERNATE, null, null, "Alt", null, null);
        OvertureNameRule rule2 =
                new OvertureNameRule(Variant.SHORT, null, null, "Main", null, Side.LEFT);

        OvertureNames names = new OvertureNames("Main St", common, List.of(rule1, rule2));

        assertEquals("Main St", names.getPrimary());
        assertEquals(2, names.getCommon().size());
        assertEquals(2, names.getRules().size());
    }
}
