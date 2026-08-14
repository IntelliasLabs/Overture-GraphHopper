package com.graphhopper.reader.overture.road.segment.spliter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import com.graphhopper.reader.overture.common.speed.OvertureSpeedLimit;
import com.graphhopper.reader.overture.names.OvertureNameRule;
import com.graphhopper.reader.overture.names.OvertureNames;
import com.graphhopper.reader.overture.road.flags.OvertureRoadFlags;
import com.graphhopper.reader.overture.road.segment.OvertureConnector;
import com.graphhopper.reader.overture.road.segment.OvertureRoadProperties;
import com.graphhopper.reader.overture.road.surface.OvertureRoadSurface;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SplitPointCollectorTest {

    @Test
    @DisplayName("Should always include 0.0 and 1.0 even with empty properties")
    void testDefaultPoints() {
        OvertureRoadProperties prop = mock(OvertureRoadProperties.class);

        TreeSet<Double> result = SplitPointCollector.collectSplitPoints(prop);

        assertEquals(2, result.size());
        assertTrue(result.contains(0.0));
        assertTrue(result.contains(1.0));
    }

    @Test
    @DisplayName("Should collect points from road connectors")
    void testConnectorPoints() {
        OvertureRoadProperties prop = mock(OvertureRoadProperties.class);
        OvertureConnector c1 = mock(OvertureConnector.class);
        OvertureConnector c2 = mock(OvertureConnector.class);

        when(c1.getAt()).thenReturn(0.5);
        when(c2.getAt()).thenReturn(0.7);
        when(prop.getConnectors()).thenReturn(Arrays.asList(c1, c2));

        TreeSet<Double> result = SplitPointCollector.collectSplitPoints(prop);

        assertEquals(4, result.size());
        assertArrayEquals(new Double[] {0.0, 0.5, 0.7, 1.0}, result.toArray());
    }

    @Test
    @DisplayName("Should collect and deduplicate range points from multiple property sources")
    void testRangePointsFromMultipleSources() {
        OvertureRoadProperties prop = mock(OvertureRoadProperties.class);

        OvertureSpeedLimit speed = mock(OvertureSpeedLimit.class);
        when(speed.getBetween()).thenReturn(new LinearlyReferencedRange(0.1, 0.4));
        when(prop.getSpeedLimits()).thenReturn(Collections.singletonList(speed));

        OvertureRoadSurface surface = mock(OvertureRoadSurface.class);
        when(surface.getBetween()).thenReturn(new LinearlyReferencedRange(0.4, 0.6));
        when(prop.getSurfaces()).thenReturn(Collections.singletonList(surface));

        TreeSet<Double> result = SplitPointCollector.collectSplitPoints(prop);

        assertArrayEquals(new Double[] {0.0, 0.1, 0.4, 0.6, 1.0}, result.toArray());
    }

    @Test
    @DisplayName("Should handle null names property without throwing exception")
    void testNameRulesWithNullNames() {
        OvertureRoadProperties prop = mock(OvertureRoadProperties.class);

        when(prop.getNames()).thenReturn(null);

        assertDoesNotThrow(() -> SplitPointCollector.collectSplitPoints(prop));
    }

    @Test
    @DisplayName("Should extract split points from road name rules")
    void testNameRulesExtraction() {
        OvertureRoadProperties prop = mock(OvertureRoadProperties.class);
        OvertureNames names = mock(OvertureNames.class);
        OvertureNameRule rule = mock(OvertureNameRule.class);

        when(prop.getNames()).thenReturn(names);
        when(names.getRules()).thenReturn(Collections.singletonList(rule));
        when(rule.getBetween()).thenReturn(new LinearlyReferencedRange(0.3, 0.8));

        TreeSet<Double> result = SplitPointCollector.collectSplitPoints(prop);

        assertTrue(result.contains(0.3));
        assertTrue(result.contains(0.8));
    }

    @Test
    @DisplayName("Should correctly combine and sort points from connectors and all ranges")
    void testAllPropertiesCombined() {
        OvertureRoadProperties prop = mock(OvertureRoadProperties.class);

        OvertureSpeedLimit s = mock(OvertureSpeedLimit.class);
        when(s.getBetween()).thenReturn(new LinearlyReferencedRange(0.2, 0.3));
        when(prop.getSpeedLimits()).thenReturn(List.of(s));

        OvertureRoadFlags f = mock(OvertureRoadFlags.class);
        when(f.getBetween()).thenReturn(new LinearlyReferencedRange(0.5, 0.55));
        when(prop.getFlags()).thenReturn(List.of(f));

        OvertureConnector c = mock(OvertureConnector.class);
        when(c.getAt()).thenReturn(0.9);
        when(prop.getConnectors()).thenReturn(List.of(c));

        TreeSet<Double> result = SplitPointCollector.collectSplitPoints(prop);

        assertArrayEquals(new Double[] {0.0, 0.2, 0.3, 0.5, 0.55, 0.9, 1.0}, result.toArray());
    }
}
