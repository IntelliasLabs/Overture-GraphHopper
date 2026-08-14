package com.graphhopper.reader.overture.road.segment.rule;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OvertureLevelRuleTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange: Level 1 (overpass) applied to the second half of the segment
        int expectedLevel = 1;
        LinearlyReferencedRange expectedRange = new LinearlyReferencedRange(0.5, 1.0);

        // Act
        OvertureLevelRule rule = new OvertureLevelRule(expectedLevel, expectedRange);

        // Assert
        assertEquals(expectedLevel, rule.getValue());
        assertEquals(expectedRange, rule.getBetween());
    }

    @Test
    void testConstructorWithNegativeLevel() {
        // Levels can be negative (e.g., -1 for a tunnel)
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.0, 0.5);
        OvertureLevelRule rule = new OvertureLevelRule(-1, range);

        assertEquals(-1, rule.getValue());
    }

    @Test
    void testEqualsAndHashCode() {
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.1, 0.2);

        OvertureLevelRule rule1 = new OvertureLevelRule(2, range);
        OvertureLevelRule rule2 = new OvertureLevelRule(2, range);
        OvertureLevelRule rule3 = new OvertureLevelRule(3, range); // Different level
        OvertureLevelRule rule4 = new OvertureLevelRule(2, new LinearlyReferencedRange(0.1, 0.3)); // Different range

        // Equality checks
        assertEquals(rule1, rule2);
        assertEquals(rule1.hashCode(), rule2.hashCode());

        assertNotEquals(rule1, rule3);
        assertNotEquals(rule1, rule4);
        assertNotEquals(null, rule1);
    }

    @Test
    void testToString() {
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.0, 1.0);
        OvertureLevelRule rule = new OvertureLevelRule(0, range);
        String result = rule.toString();

        assertTrue(result.contains("value=0"));
        assertTrue(result.contains("between="));
        assertTrue(result.startsWith("OvertureLevelRule"));
    }
}
