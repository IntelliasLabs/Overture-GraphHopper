package com.graphhopper.reader.overture.names;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OvertureNamesHelperTest {

    @Test
    void testGetNameForLanguageExactMatch() {
        Map<Bcp47LanguageTag, String> common = new HashMap<>();
        common.put(Bcp47LanguageTag.parse("uk"), "Головна вулиця");
        common.put(Bcp47LanguageTag.parse("en"), "Main Street");

        OvertureNames names = new OvertureNames("Main St", common, null);

        assertEquals("Головна вулиця", OvertureNamesHelper.getNameForLanguage(names, "uk"));
        assertEquals("Main Street", OvertureNamesHelper.getNameForLanguage(names, "en"));
    }

    @Test
    void testGetNameForLanguageFallbackToPrimary() {
        Map<Bcp47LanguageTag, String> common = new HashMap<>();
        common.put(Bcp47LanguageTag.parse("uk"), "Головна вулиця");

        OvertureNames names = new OvertureNames("Main St", common, null);

        assertEquals("Main St", OvertureNamesHelper.getNameForLanguage(names, "fr"));
        assertEquals("Main St", OvertureNamesHelper.getNameForLanguage(names, "de"));
    }

    @Test
    void testGetNameForLanguageNullCommon() {
        OvertureNames names = new OvertureNames("Main St", null, null);

        assertEquals("Main St", OvertureNamesHelper.getNameForLanguage(names, "uk"));
    }

    @Test
    void testGetNameForLanguageNullLanguageCode() {
        Map<Bcp47LanguageTag, String> common = new HashMap<>();
        common.put(Bcp47LanguageTag.parse("uk"), "Головна вулиця");

        OvertureNames names = new OvertureNames("Main St", common, null);

        assertEquals("Main St", OvertureNamesHelper.getNameForLanguage(names, null));
    }

    @Test
    void testGetNameForLanguageEmptyCommon() {
        OvertureNames names = new OvertureNames("Main St", new HashMap<>(), null);

        assertEquals("Main St", OvertureNamesHelper.getNameForLanguage(names, "uk"));
    }

    @Test
    void testGetNameForLanguageNullPrimary() {
        Map<Bcp47LanguageTag, String> common = new HashMap<>();
        common.put(Bcp47LanguageTag.parse("uk"), "Головна вулиця");

        OvertureNames names = new OvertureNames(null, common, null);

        assertEquals("Головна вулиця", OvertureNamesHelper.getNameForLanguage(names, "uk"));
        assertNull(OvertureNamesHelper.getNameForLanguage(names, "fr"));
    }

    @Test
    void testGetNameForLanguageNullNames() {
        assertNull(OvertureNamesHelper.getNameForLanguage(null, "uk"));
    }

    @Test
    void testGetNameForLanguageEmptyLanguageCode() {
        Map<Bcp47LanguageTag, String> common = new HashMap<>();
        common.put(Bcp47LanguageTag.parse("uk"), "Головна вулиця");

        OvertureNames names = new OvertureNames("Main St", common, null);

        assertEquals("Main St", OvertureNamesHelper.getNameForLanguage(names, ""));
    }
}
