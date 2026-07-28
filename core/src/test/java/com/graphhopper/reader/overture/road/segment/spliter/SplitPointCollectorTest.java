package com.graphhopper.reader.overture.road.segment.spliter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import com.graphhopper.reader.overture.common.speed.OvertureSpeedLimit;
import com.graphhopper.reader.overture.names.OvertureNameRule;
import com.graphhopper.reader.overture.names.OvertureNames;
import com.graphhopper.reader.overture.road.flags.OvertureRoadFlags;
import com.graphhopper.reader.overture.road.segment.OvertureConnector;
import com.graphhopper.reader.overture.road.segment.OvertureRoadProperties;
import com.graphhopper.reader.overture.road.surface.OvertureRoadSurface;
import com.graphhopper.reader.overture.road.surface.RoadSurfaceType;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Covers which positions along a segment become sub-segment boundaries.
 *
 * <p>Deliberately built from real value objects rather than Mockito mocks. These are small immutable
 * carriers, so mocking them bought nothing, and the previous mock-based version was intermittently
 * flaky in the full suite: mocks that were reachable only through another mock's stubbed return value
 * would occasionally answer with defaults instead of their stubbed values, silently dropping split
 * points. Constructing the values directly removes that failure mode and is clearer besides.
 */
class SplitPointCollectorTest {

    @Test
    @DisplayName("Should always include 0.0 and 1.0 even with empty properties")
    void testDefaultPoints() {
        TreeSet<Double> result = SplitPointCollector.collectSplitPoints(properties().build());

        assertEquals(2, result.size());
        assertTrue(result.contains(0.0));
        assertTrue(result.contains(1.0));
    }

    @Test
    @DisplayName("Should collect points from road connectors")
    void testConnectorPoints() {
        OvertureRoadProperties prop =
                properties().connectors(connector("c1", 0.5), connector("c2", 0.7)).build();

        TreeSet<Double> result = SplitPointCollector.collectSplitPoints(prop);

        assertEquals(4, result.size());
        assertArrayEquals(new Double[] {0.0, 0.5, 0.7, 1.0}, result.toArray());
    }

    @Test
    @DisplayName("Should collect and deduplicate range points from multiple property sources")
    void testRangePointsFromMultipleSources() {
        OvertureRoadProperties prop = properties()
                .speedLimits(speedLimit(0.1, 0.4))
                .surfaces(surface(0.4, 0.6))
                .build();

        TreeSet<Double> result = SplitPointCollector.collectSplitPoints(prop);

        // 0.4 is shared by the two ranges and must appear once.
        assertArrayEquals(new Double[] {0.0, 0.1, 0.4, 0.6, 1.0}, result.toArray());
    }

    @Test
    @DisplayName("Should handle null names property without throwing exception")
    void testNameRulesWithNullNames() {
        OvertureRoadProperties prop = properties().names(null).build();

        assertDoesNotThrow(() -> SplitPointCollector.collectSplitPoints(prop));
    }

    @Test
    @DisplayName("Should extract split points from road name rules")
    void testNameRulesExtraction() {
        OvertureRoadProperties prop = properties().names(namesWithRule(0.3, 0.8)).build();

        TreeSet<Double> result = SplitPointCollector.collectSplitPoints(prop);

        assertTrue(result.contains(0.3));
        assertTrue(result.contains(0.8));
    }

    @Test
    @DisplayName("Should correctly combine and sort points from connectors and all ranges")
    void testAllPropertiesCombined() {
        OvertureRoadProperties prop = properties()
                .speedLimits(speedLimit(0.2, 0.3))
                .flags(flags(0.5, 0.55))
                .connectors(connector("c", 0.9))
                .build();

        TreeSet<Double> result = SplitPointCollector.collectSplitPoints(prop);

        assertArrayEquals(new Double[] {0.0, 0.2, 0.3, 0.5, 0.55, 0.9, 1.0}, result.toArray());
    }

    // ------------------------------------------------------------------
    // Value construction
    // ------------------------------------------------------------------

    private static OvertureConnector connector(String id, double at) {
        return new OvertureConnector(id, at);
    }

    private static OvertureSpeedLimit speedLimit(double start, double end) {
        return new OvertureSpeedLimit(null, null, null, new LinearlyReferencedRange(start, end), null);
    }

    private static OvertureRoadSurface surface(double start, double end) {
        return new OvertureRoadSurface(RoadSurfaceType.PAVED, new LinearlyReferencedRange(start, end));
    }

    private static OvertureRoadFlags flags(double start, double end) {
        return new OvertureRoadFlags(
                false, false, false, false, false, false, new LinearlyReferencedRange(start, end));
    }

    private static OvertureNames namesWithRule(double start, double end) {
        OvertureNameRule rule = new OvertureNameRule(
                null, null, null, "Test Street", new LinearlyReferencedRange(start, end), null);
        return new OvertureNames("Test Street", null, List.of(rule));
    }

    private static Builder properties() {
        return new Builder();
    }

    /**
     * Assembles an {@link OvertureRoadProperties} for the few fields these tests care about.
     *
     * <p>Exists because the production type takes nineteen positional arguments, which would bury the
     * one or two values that matter to each test.
     */
    private static final class Builder {
        private List<OvertureConnector> connectors;
        private List<OvertureSpeedLimit> speedLimits;
        private List<OvertureRoadSurface> surfaces;
        private List<OvertureRoadFlags> flags;
        private OvertureNames names;

        Builder connectors(OvertureConnector... values) {
            this.connectors = Arrays.asList(values);
            return this;
        }

        Builder speedLimits(OvertureSpeedLimit... values) {
            this.speedLimits = Arrays.asList(values);
            return this;
        }

        Builder surfaces(OvertureRoadSurface... values) {
            this.surfaces = Arrays.asList(values);
            return this;
        }

        Builder flags(OvertureRoadFlags... values) {
            this.flags = Arrays.asList(values);
            return this;
        }

        Builder names(OvertureNames value) {
            this.names = value;
            return this;
        }

        OvertureRoadProperties build() {
            return new OvertureRoadProperties(
                    connectors,
                    null, // routes
                    null, // roadClass
                    null, // destinations
                    null, // prohibitedTransitions
                    surfaces,
                    flags,
                    speedLimits,
                    null, // widthRules
                    null, // subclass
                    null, // subclassRules
                    null, // accessRestrictions
                    0, // level
                    null, // levelRules
                    null, // theme
                    null, // type
                    0, // version
                    null, // sources
                    names);
        }
    }
}
