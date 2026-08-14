package com.graphhopper.reader.overture.names;

import static org.junit.jupiter.api.Assertions.*;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import org.junit.jupiter.api.Test;

class OvertureNameRuleTest {

    @Test
    void testConstructorAndGetters() {
        OvertureNameRule rule = new OvertureNameRule(
                Variant.OFFICIAL,
                Bcp47LanguageTag.parse("en"),
                null,
                "Main St",
                new LinearlyReferencedRange(0.0, 1.0),
                Side.LEFT);

        assertEquals(Variant.OFFICIAL, rule.getVariant());
        assertEquals("en", rule.getLanguage().getTag());
        assertEquals("Main St", rule.getValue());
        assertEquals(0.0, rule.getBetween().getStart());
        assertEquals(1.0, rule.getBetween().getEnd());
        assertEquals(Side.LEFT, rule.getSide());
    }

    @Test
    void testMinimalRule() {
        OvertureNameRule rule =
                new OvertureNameRule(Variant.COMMON, null, null, "Test Road", null, null);

        assertEquals(Variant.COMMON, rule.getVariant());
        assertNull(rule.getLanguage());
        assertEquals("Test Road", rule.getValue());
        assertNull(rule.getBetween());
        assertNull(rule.getSide());
    }

    @Test
    void testAllNullFields() {
        OvertureNameRule rule = new OvertureNameRule(null, null, null, null, null, null);

        assertNull(rule.getVariant());
        assertNull(rule.getLanguage());
        assertNull(rule.getValue());
        assertNull(rule.getBetween());
        assertNull(rule.getSide());
    }

    @Test
    void testEqualsAndHashCode() {
        OvertureNameRule rule1 = new OvertureNameRule(Variant.COMMON, null, null, "Test", null, null);
        OvertureNameRule rule2 = new OvertureNameRule(Variant.COMMON, null, null, "Test", null, null);
        OvertureNameRule rule3 = new OvertureNameRule(Variant.SHORT, null, null, "Test", null, null);

        assertEquals(rule1, rule2);
        assertEquals(rule1.hashCode(), rule2.hashCode());
        assertNotEquals(rule1, rule3);
    }

    @Test
    void testEqualsWithAllFields() {
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.0, 0.5);
        Bcp47LanguageTag lang = Bcp47LanguageTag.parse("de");

        OvertureNameRule rule1 =
                new OvertureNameRule(Variant.ALTERNATE, lang, null, "Straße", range, Side.RIGHT);
        OvertureNameRule rule2 =
                new OvertureNameRule(Variant.ALTERNATE, lang, null, "Straße", range, Side.RIGHT);

        assertEquals(rule1, rule2);
        assertEquals(rule1.hashCode(), rule2.hashCode());
    }

    @Test
    void testEqualsSameInstance() {
        OvertureNameRule rule = new OvertureNameRule(Variant.OFFICIAL, null, null, "Test", null, null);
        assertEquals(rule, rule);
    }

    @Test
    void testEqualsNull() {
        OvertureNameRule rule = new OvertureNameRule(Variant.OFFICIAL, null, null, "Test", null, null);
        assertNotEquals(null, rule);
    }

    @Test
    void testEqualsDifferentType() {
        OvertureNameRule rule = new OvertureNameRule(Variant.OFFICIAL, null, null, "Test", null, null);
        assertNotEquals("not a rule", rule);
    }

    @Test
    void testToString() {
        OvertureNameRule rule = new OvertureNameRule(
                Variant.ALTERNATE,
                Bcp47LanguageTag.parse("fr"),
                null,
                "Rue Principale",
                new LinearlyReferencedRange(0.0, 1.0),
                Side.LEFT);

        String str = rule.toString();
        assertTrue(str.contains("OvertureNameRule"));
        assertTrue(str.contains("alternate"));
        assertTrue(str.contains("Rue Principale"));
    }
}
