package com.graphhopper.reader.overture.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.graphhopper.reader.overture.road.segment.OvertureRoadClass;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSubclass;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class OvertureRoadClassLinkParserTest {
    @ParameterizedTest(name = "Link detection: class={0}, subclass={1} -> expected={2}")
    @CsvSource(
            value = {
                "MOTORWAY,    LINK, true",
                "TRUNK,       LINK, true",
                "PRIMARY,     LINK, true",
                "SECONDARY,   LINK, true",
                "TERTIARY,    LINK, true",
                "MOTORWAY,    null, false",
                "PRIMARY,     null, false",
                "RESIDENTIAL, null, false"
            },
            nullValues = "null")
    @DisplayName("Should correctly identify links for major road classes")
    void testIsLinkMapping(
            OvertureRoadClass oClass, OvertureRoadSubclass oSubclass, boolean expected) {
        assertEquals(expected, OvertureRoadClassLinkParser.isLink(oClass, oSubclass));
    }

    @Test
    @DisplayName("Non-link road: Should return false even if subclass is LINK for minor roads")
    void testNonLinkRoadTypes() {
        assertFalse(
                OvertureRoadClassLinkParser.isLink(
                        OvertureRoadClass.LIVING_STREET, OvertureRoadSubclass.LINK),
                "Living street should not be considered a link even with LINK subclass");
    }

    @Test
    @DisplayName("Edge Cases: Should handle non-link subclasses")
    void testEdgeCases() {
        assertFalse(OvertureRoadClassLinkParser.isLink(
                OvertureRoadClass.MOTORWAY, OvertureRoadSubclass.SIDEWALK));
    }
}
