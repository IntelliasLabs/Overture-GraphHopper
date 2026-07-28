package com.graphhopper.reader.overture.road.segment.rule;

import static org.junit.jupiter.api.Assertions.*;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import org.junit.jupiter.api.Test;

class OvertureWidthRuleTest {
    @Test
    void testConstructorAndGetters() {
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.0, 1.0);
        double expectedWidth = 7.5;

        OvertureWidthRule rule = new OvertureWidthRule(expectedWidth, range);

        assertEquals(expectedWidth, rule.getValue());
        assertEquals(range, rule.getBetween());
    }

    @Test
    void testConstructorWithNullRange() {
        OvertureWidthRule rule = new OvertureWidthRule(10.0, null);

        assertEquals(10.0, rule.getValue());
        assertNull(rule.getBetween());
    }

    @Test
    void testEqualsWithEqualObjects() {
        LinearlyReferencedRange range1 = new LinearlyReferencedRange(0.0, 0.5);
        LinearlyReferencedRange range2 = new LinearlyReferencedRange(0.0, 0.5);

        OvertureWidthRule rule1 = new OvertureWidthRule(4.0, range1);
        OvertureWidthRule rule2 = new OvertureWidthRule(4.0, range2);

        assertEquals(rule1, rule2);
    }

    @Test
    void testEqualsWithDifferentValue() {
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.0, 1.0);
        OvertureWidthRule rule1 = new OvertureWidthRule(4.0, range);
        OvertureWidthRule rule2 = new OvertureWidthRule(4.1, range);

        assertNotEquals(rule1, rule2);
    }

    @Test
    void testEqualsWithDifferentRange() {
        LinearlyReferencedRange range1 = new LinearlyReferencedRange(0.0, 1.0);
        LinearlyReferencedRange range2 = new LinearlyReferencedRange(0.0, 0.8);

        OvertureWidthRule rule1 = new OvertureWidthRule(5.0, range1);
        OvertureWidthRule rule2 = new OvertureWidthRule(5.0, range2);

        assertNotEquals(rule1, rule2);
    }

    @Test
    void testEqualsWithNull() {
        OvertureWidthRule rule = new OvertureWidthRule(6.0, null);
        assertNotEquals(null, rule);
    }

    @Test
    void testHashCodeEqualObjects() {
        OvertureWidthRule rule1 = new OvertureWidthRule(8.5, null);
        OvertureWidthRule rule2 = new OvertureWidthRule(8.5, null);

        assertEquals(rule1.hashCode(), rule2.hashCode());
    }

    @Test
    void testToString() {
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.1, 0.9);
        OvertureWidthRule rule = new OvertureWidthRule(12.25, range);

        String result = rule.toString();

        assertTrue(result.contains("OvertureWidthRule"));
        assertTrue(result.contains("value=12.25"));
        assertTrue(result.contains("between="));
    }
}
