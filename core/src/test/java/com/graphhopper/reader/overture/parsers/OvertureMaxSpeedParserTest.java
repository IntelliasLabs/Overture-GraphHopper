package com.graphhopper.reader.overture.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.graphhopper.reader.overture.access.restriction.PropertyScopeContainer;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelHeading;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelMode;
import com.graphhopper.reader.overture.common.speed.OvertureSpeed;
import com.graphhopper.reader.overture.common.speed.OvertureSpeedLimit;
import com.graphhopper.reader.overture.common.speed.SpeedUnit;
import com.graphhopper.reader.overture.road.segment.OvertureRoadProperties;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.MaxSpeed;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.util.EdgeIteratorState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Covers resolution of the {@code max_speed} encoded value from Overture speed limits.
 *
 * <p>The important invariant is that an absent limit is written as {@link
 * MaxSpeed#MAXSPEED_MISSING} (positive infinity) and never left at the storage default of 0. A 0
 * reads as a genuine limit of zero km/h and also stops {@code MaxSpeedCalculator} from supplying a
 * legal default, so the distinction is load-bearing rather than cosmetic.
 */
class OvertureMaxSpeedParserTest {

    private DecimalEncodedValue maxSpeedEnc;
    private EdgeIteratorState edge;
    private BaseGraph graph;

    @BeforeEach
    void setup() {
        maxSpeedEnc = MaxSpeed.create();
        EncodingManager em = EncodingManager.start().add(maxSpeedEnc).build();
        graph = new BaseGraph.Builder(em).create();
        edge = graph.edge(0, 1);
        edge.setDistance(100);
    }

    @AfterEach
    void tearDown() {
        if (graph != null) graph.close();
    }

    @Test
    @DisplayName("An unscoped limit in km/h applies to both directions")
    void unscopedLimitAppliesBothWays() {
        new OvertureMaxSpeedParser(maxSpeedEnc)
                .handleSegment(edge, segmentWith(limit(50.0, SpeedUnit.KM_H, null)), null);

        assertEquals(50.0, edge.get(maxSpeedEnc), 0.01);
        assertEquals(50.0, edge.getReverse(maxSpeedEnc), 0.01);
    }

    @Test
    @DisplayName("A limit in mph is converted to km/h")
    void mphIsConvertedToKmh() {
        new OvertureMaxSpeedParser(maxSpeedEnc)
                .handleSegment(edge, segmentWith(limit(30.0, SpeedUnit.MPH, null)), null);

        // 30 mph = 48.28 km/h; the encoding stores multiples of 2, so allow a step of tolerance.
        assertEquals(48.28, edge.get(maxSpeedEnc), 2.0);
    }

    @Test
    @DisplayName("Directional limits are applied per direction")
    void directionalLimitsArePerDirection() {
        OvertureRoadSegment segment = segmentWith(
                limit(70.0, SpeedUnit.KM_H, TravelHeading.FORWARD),
                limit(30.0, SpeedUnit.KM_H, TravelHeading.BACKWARD));

        new OvertureMaxSpeedParser(maxSpeedEnc).handleSegment(edge, segment, null);

        assertEquals(70.0, edge.get(maxSpeedEnc), 0.01);
        assertEquals(30.0, edge.getReverse(maxSpeedEnc), 0.01);
    }

    @Test
    @DisplayName("A directional limit overrides an unscoped one for that direction only")
    void directionalOverridesUnscoped() {
        OvertureRoadSegment segment = segmentWith(
                limit(80.0, SpeedUnit.KM_H, null), limit(40.0, SpeedUnit.KM_H, TravelHeading.FORWARD));

        new OvertureMaxSpeedParser(maxSpeedEnc).handleSegment(edge, segment, null);

        assertEquals(40.0, edge.get(maxSpeedEnc), 0.01, "forward takes the direction-specific sign");
        assertEquals(80.0, edge.getReverse(maxSpeedEnc), 0.01, "backward keeps the general limit");
    }

    @Test
    @DisplayName("Where several limits apply to one direction the lowest wins")
    void lowestOfOverlappingLimitsWins() {
        OvertureRoadSegment segment =
                segmentWith(limit(90.0, SpeedUnit.KM_H, null), limit(60.0, SpeedUnit.KM_H, null));

        new OvertureMaxSpeedParser(maxSpeedEnc).handleSegment(edge, segment, null);

        assertEquals(60.0, edge.get(maxSpeedEnc), 0.01);
    }

    @Test
    @DisplayName("Limits above 150 km/h are capped")
    void excessiveLimitsAreCapped() {
        new OvertureMaxSpeedParser(maxSpeedEnc)
                .handleSegment(edge, segmentWith(limit(200.0, SpeedUnit.KM_H, null)), null);

        assertEquals(MaxSpeed.MAXSPEED_150, edge.get(maxSpeedEnc), 0.01);
    }

    @Test
    @DisplayName("A variable speed limit is still recorded")
    void variableLimitIsRecorded() {
        OvertureSpeedLimit variable = new OvertureSpeedLimit(
                new OvertureSpeed(60.0, SpeedUnit.KM_H), null, Boolean.TRUE, null, null);

        new OvertureMaxSpeedParser(maxSpeedEnc).handleSegment(edge, segmentWith(variable), null);

        assertEquals(60.0, edge.get(maxSpeedEnc), 0.01);
    }

    @Test
    @DisplayName("No speed limits yields MAXSPEED_MISSING, not zero")
    void absentLimitIsMissingNotZero() {
        new OvertureMaxSpeedParser(maxSpeedEnc).handleSegment(edge, segmentWith(), null);

        assertTrue(Double.isInfinite(edge.get(maxSpeedEnc)), "expected MAXSPEED_MISSING (infinity)");
        assertTrue(Double.isInfinite(edge.getReverse(maxSpeedEnc)));
    }

    @Test
    @DisplayName("Null and non-positive limits are treated as absent rather than as a zero limit")
    void unusableLimitsAreTreatedAsAbsent() {
        OvertureSpeedLimit noValue = new OvertureSpeedLimit(null, null, null, null, null);
        OvertureSpeedLimit zero = limit(0.0, SpeedUnit.KM_H, null);
        OvertureSpeedLimit negative = limit(-20.0, SpeedUnit.KM_H, null);

        new OvertureMaxSpeedParser(maxSpeedEnc)
                .handleSegment(edge, segmentWith(noValue, zero, negative), null);

        // A zero would make the road un-routable for anything reading max_speed, so bad data must
        // fall back to "unknown" instead.
        assertTrue(Double.isInfinite(edge.get(maxSpeedEnc)), "bad data must not produce a real limit");
    }

    @Test
    @DisplayName("A time-conditional limit does not become the permanent limit")
    void timeConditionalLimitIsIgnored() {
        // Real Overture data pattern: a general 50 plus a lower rush-hour restriction. max_speed has
        // no time dimension, so the rush-hour value must not win.
        OvertureSpeedLimit always = limit(50.0, SpeedUnit.KM_H, null);
        OvertureSpeedLimit rushHour = new OvertureSpeedLimit(
                new OvertureSpeed(20.0, SpeedUnit.KM_H),
                null,
                null,
                null,
                new PropertyScopeContainer("Mo-Fr 08:00-16:00", null, null, null, null, null));

        new OvertureMaxSpeedParser(maxSpeedEnc)
                .handleSegment(edge, segmentWith(always, rushHour), null);

        assertEquals(50.0, edge.get(maxSpeedEnc), 0.01);
    }

    @Test
    @DisplayName("A mode-specific limit does not apply to all traffic")
    void modeSpecificLimitIsIgnored() {
        OvertureSpeedLimit always = limit(60.0, SpeedUnit.KM_H, null);
        OvertureSpeedLimit lorriesOnly = new OvertureSpeedLimit(
                new OvertureSpeed(30.0, SpeedUnit.KM_H),
                null,
                null,
                null,
                new PropertyScopeContainer(
                        null, null, null, null, new ArrayList<>(List.of(TravelMode.TRUCK)), null));

        new OvertureMaxSpeedParser(maxSpeedEnc)
                .handleSegment(edge, segmentWith(always, lorriesOnly), null);

        assertEquals(60.0, edge.get(maxSpeedEnc), 0.01);
    }

    @Test
    @DisplayName("A conditional limit with no unconditional counterpart leaves the value missing")
    void conditionalOnlyLeavesMissing() {
        OvertureSpeedLimit rushHourOnly = new OvertureSpeedLimit(
                new OvertureSpeed(20.0, SpeedUnit.KM_H),
                null,
                null,
                null,
                new PropertyScopeContainer("Mo-Fr 08:00-16:00", null, null, null, null, null));

        new OvertureMaxSpeedParser(maxSpeedEnc).handleSegment(edge, segmentWith(rushHourOnly), null);

        // Better to report "unknown" and let the road-class default apply than to assert a limit that
        // only holds part of the time.
        assertTrue(Double.isInfinite(edge.get(maxSpeedEnc)));
    }

    @Test
    @DisplayName("A null segment or absent properties resolves to MAXSPEED_MISSING")
    void nullInputsAreSafe() {
        assertTrue(Double.isInfinite(OvertureMaxSpeedParser.parseMaxSpeeds(null)[0]));

        OvertureRoadSegment noProperties = new OvertureRoadSegment("id", null, null);
        assertTrue(Double.isInfinite(OvertureMaxSpeedParser.parseMaxSpeeds(noProperties)[0]));
    }

    @Test
    @DisplayName("A null entry inside the speed-limit list is skipped")
    void nullEntriesAreSkipped() {
        OvertureRoadSegment segment =
                segmentWithList(Arrays.asList(null, limit(50.0, SpeedUnit.KM_H, null)));

        new OvertureMaxSpeedParser(maxSpeedEnc).handleSegment(edge, segment, null);

        assertEquals(50.0, edge.get(maxSpeedEnc), 0.01);
    }

    private static OvertureSpeedLimit limit(double value, SpeedUnit unit, TravelHeading heading) {
        PropertyScopeContainer when =
                heading == null ? null : new PropertyScopeContainer(null, heading, null, null, null, null);
        return new OvertureSpeedLimit(new OvertureSpeed(value, unit), null, null, null, when);
    }

    private static OvertureRoadSegment segmentWith(OvertureSpeedLimit... limits) {
        return segmentWithList(List.of(limits));
    }

    private static OvertureRoadSegment segmentWithList(List<OvertureSpeedLimit> limits) {
        OvertureRoadProperties props = new OvertureRoadProperties(
                null, null, null, null, null, null, null, limits, null, null, null, null, 0, null, null,
                null, 0, null, null);
        return new OvertureRoadSegment("test_id", null, props);
    }
}
