package com.graphhopper.reader.overture.road.segment.rule;

import static org.junit.jupiter.api.Assertions.*;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSubclass;
import org.junit.jupiter.api.Test;

class OvertureSubclassRuleTest {

    @Test
    void testConstructorAndGetters() {
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.0, 1.0);
        OvertureSubclassRule rule = new OvertureSubclassRule(OvertureRoadSubclass.LINK, range);

        assertEquals(OvertureRoadSubclass.LINK, rule.getValue());
        assertEquals(range, rule.getBetween());
    }

    @Test
    void testConstructorWithDifferentSubclasses() {
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.1, 0.9);

        OvertureSubclassRule rule1 = new OvertureSubclassRule(OvertureRoadSubclass.SIDEWALK, range);
        OvertureSubclassRule rule2 =
                new OvertureSubclassRule(OvertureRoadSubclass.PARKING_AISLE, range);

        assertEquals(OvertureRoadSubclass.SIDEWALK, rule1.getValue());
        assertEquals(OvertureRoadSubclass.PARKING_AISLE, rule2.getValue());
    }

    @Test
    void testConstructorWithNullRange() {
        OvertureSubclassRule rule = new OvertureSubclassRule(OvertureRoadSubclass.ALLEY, null);

        assertEquals(OvertureRoadSubclass.ALLEY, rule.getValue());
        assertNull(rule.getBetween());
    }

    @Test
    void testEqualsWithEqualObjects() {
        LinearlyReferencedRange range1 = new LinearlyReferencedRange(0.0, 0.5);
        LinearlyReferencedRange range2 = new LinearlyReferencedRange(0.0, 0.5);

        OvertureSubclassRule rule1 = new OvertureSubclassRule(OvertureRoadSubclass.LINK, range1);
        OvertureSubclassRule rule2 = new OvertureSubclassRule(OvertureRoadSubclass.LINK, range2);

        assertEquals(rule1, rule2);
    }

    @Test
    void testEqualsWithDifferentValue() {
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.0, 1.0);
        OvertureSubclassRule rule1 = new OvertureSubclassRule(OvertureRoadSubclass.LINK, range);
        OvertureSubclassRule rule2 = new OvertureSubclassRule(OvertureRoadSubclass.SIDEWALK, range);

        assertNotEquals(rule1, rule2);
    }

    @Test
    void testEqualsWithDifferentRange() {
        LinearlyReferencedRange range1 = new LinearlyReferencedRange(0.0, 1.0);
        LinearlyReferencedRange range2 = new LinearlyReferencedRange(0.5, 1.0);

        OvertureSubclassRule rule1 = new OvertureSubclassRule(OvertureRoadSubclass.LINK, range1);
        OvertureSubclassRule rule2 = new OvertureSubclassRule(OvertureRoadSubclass.LINK, range2);

        assertNotEquals(rule1, rule2);
    }

    @Test
    void testEqualsWithNull() {
        OvertureSubclassRule rule = new OvertureSubclassRule(OvertureRoadSubclass.LINK, null);
        assertNotEquals(null, rule);
    }

    @Test
    void testHashCodeEqualObjects() {
        OvertureSubclassRule rule1 = new OvertureSubclassRule(OvertureRoadSubclass.LINK, null);
        OvertureSubclassRule rule2 = new OvertureSubclassRule(OvertureRoadSubclass.LINK, null);

        assertEquals(rule1.hashCode(), rule2.hashCode());
    }

    @Test
    void testToString() {
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.0, 1.0);
        OvertureSubclassRule rule = new OvertureSubclassRule(OvertureRoadSubclass.CROSSWALK, range);

        String result = rule.toString();

        assertTrue(result.contains("OvertureSubclassRule"));
        assertTrue(result.contains("value=crosswalk"));
        assertTrue(result.contains("between="));
    }
}
