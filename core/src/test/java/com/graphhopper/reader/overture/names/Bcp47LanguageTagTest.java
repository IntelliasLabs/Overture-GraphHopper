package com.graphhopper.reader.overture.names;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Bcp47LanguageTagTest {

    @Test
    void testParseSimpleLanguage() {
        Bcp47LanguageTag tag = Bcp47LanguageTag.parse("en");
        assertNotNull(tag);
        assertEquals("en", tag.getTag());
        assertEquals("en", tag.getLanguage());
        assertNull(tag.getScript());
        assertNull(tag.getRegion());
    }

    @Test
    void testParseLanguageWithRegion() {
        Bcp47LanguageTag tag = Bcp47LanguageTag.parse("en-US");
        assertNotNull(tag);
        assertEquals("en-US", tag.getTag());
        assertEquals("en", tag.getLanguage());
        assertNull(tag.getScript());
        assertEquals("US", tag.getRegion());
    }

    @Test
    void testParseLanguageWithScript() {
        Bcp47LanguageTag tag = Bcp47LanguageTag.parse("zh-Hans");
        assertNotNull(tag);
        assertEquals("zh-Hans", tag.getTag());
        assertEquals("zh", tag.getLanguage());
        assertEquals("Hans", tag.getScript());
        assertNull(tag.getRegion());
    }

    @Test
    void testParseLanguageWithScriptAndRegion() {
        Bcp47LanguageTag tag = Bcp47LanguageTag.parse("zh-Hans-CN");
        assertNotNull(tag);
        assertEquals("zh-Hans-CN", tag.getTag());
        assertEquals("zh", tag.getLanguage());
        assertEquals("Hans", tag.getScript());
        assertEquals("CN", tag.getRegion());
    }

    @Test
    void testParseNullReturnsNull() {
        assertNull(Bcp47LanguageTag.parse(null));
    }

    @Test
    void testParseEmptyReturnsNull() {
        assertNull(Bcp47LanguageTag.parse(""));
    }

    @Test
    void testParseInvalidTagReturnsNull() {
        assertNull(Bcp47LanguageTag.parse("invalid!!tag"));
        assertNull(Bcp47LanguageTag.parse("123"));
        assertNull(Bcp47LanguageTag.parse("-en"));
    }

    @Test
    void testEqualsAndHashCode() {
        Bcp47LanguageTag tag1 = Bcp47LanguageTag.parse("en-US");
        Bcp47LanguageTag tag2 = Bcp47LanguageTag.parse("en-US");
        Bcp47LanguageTag tag3 = Bcp47LanguageTag.parse("en-GB");

        assertEquals(tag1, tag2);
        assertEquals(tag1.hashCode(), tag2.hashCode());
        assertNotEquals(tag1, tag3);
    }

    @Test
    void testEqualsWithNull() {
        Bcp47LanguageTag tag = Bcp47LanguageTag.parse("en");
        assertNotEquals(tag, null);
    }

    @Test
    void testToString() {
        Bcp47LanguageTag tag = Bcp47LanguageTag.parse("en-US");
        assertEquals("en-US", tag.toString());
    }

    @Test
    void testVariantsAndExtensions() {
        Bcp47LanguageTag tag = Bcp47LanguageTag.parse("uk");
        assertNotNull(tag);
        assertNotNull(tag.getVariants());
        assertNotNull(tag.getExtensions());
    }
}
