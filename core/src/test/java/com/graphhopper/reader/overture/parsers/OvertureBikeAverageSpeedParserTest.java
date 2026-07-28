package com.graphhopper.reader.overture.parsers;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.graphhopper.reader.overture.access.restriction.AccessType;
import com.graphhopper.reader.overture.access.restriction.OvertureAccessRestriction;
import com.graphhopper.reader.overture.access.restriction.PropertyScopeContainer;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelHeading;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelMode;
import com.graphhopper.reader.overture.common.speed.OvertureSpeed;
import com.graphhopper.reader.overture.common.speed.OvertureSpeedLimit;
import com.graphhopper.reader.overture.common.speed.SpeedUnit;
import com.graphhopper.reader.overture.parsers.OvertureBikeAverageSpeedParser.BikeSpeed;
import com.graphhopper.reader.overture.road.segment.OvertureRoadClass;
import com.graphhopper.reader.overture.road.segment.OvertureRoadProperties;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.reader.overture.road.surface.OvertureRoadSurface;
import com.graphhopper.reader.overture.road.surface.RoadSurfaceType;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.DecimalEncodedValueImpl;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.util.EdgeIteratorState;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link OvertureBikeAverageSpeedParser}.
 */
public class OvertureBikeAverageSpeedParserTest {

    private DecimalEncodedValue speedEnc;
    private EdgeIteratorState edge;
    private BaseGraph graph;

    @BeforeEach
    void setup() {
        speedEnc = new DecimalEncodedValueImpl("bike_speed", 5, 5, true);
        EncodingManager em = EncodingManager.start().add(speedEnc).build();

        graph = new BaseGraph.Builder(em).create();
        edge = graph.edge(0, 1);
        edge.setDistance(100);
    }

    @AfterEach
    void tearDown() {
        if (graph != null) {
            graph.close();
        }
    }

    // Road Class Speed Tests

    @Test
    @DisplayName("Cycleway should have 18 km/h speed")
    void roadClass_cycleway() {
        OvertureRoadSegment segment = createSegment(OvertureRoadClass.CYCLEWAY, null, null, null);
        BikeSpeed speed = OvertureBikeAverageSpeedParser.calculateBikeSpeed(segment);
        assertEquals(18.0, speed.forward(), 0.1);
    }

    @Test
    @DisplayName("Path should have 6 km/h speed")
    void roadClass_path() {
        OvertureRoadSegment segment = createSegment(OvertureRoadClass.PATH, null, null, null);
        BikeSpeed speed = OvertureBikeAverageSpeedParser.calculateBikeSpeed(segment);
        assertEquals(6.0, speed.forward(), 0.1);
    }

    @Test
    @DisplayName("Steps should have minimum 2 km/h speed")
    void roadClass_steps() {
        OvertureRoadSegment segment = createSegment(OvertureRoadClass.STEPS, null, null, null);
        BikeSpeed speed = OvertureBikeAverageSpeedParser.calculateBikeSpeed(segment);
        assertEquals(2.0, speed.forward(), 0.1);
    }

    @Test
    @DisplayName("Residential should have 18 km/h speed")
    void roadClass_residential() {
        OvertureRoadSegment segment = createSegment(OvertureRoadClass.RESIDENTIAL, null, null, null);
        BikeSpeed speed = OvertureBikeAverageSpeedParser.calculateBikeSpeed(segment);
        assertEquals(18.0, speed.forward(), 0.1);
    }

    @Test
    @DisplayName("Track should have 12 km/h speed")
    void roadClass_track() {
        OvertureRoadSegment segment = createSegment(OvertureRoadClass.TRACK, null, null, null);
        BikeSpeed speed = OvertureBikeAverageSpeedParser.calculateBikeSpeed(segment);
        assertEquals(12.0, speed.forward(), 0.1);
    }

    @Test
    @DisplayName("Unknown road class should have default 12 km/h speed")
    void roadClass_unknown() {
        OvertureRoadSegment segment = createSegment(null, null, null, null);
        BikeSpeed speed = OvertureBikeAverageSpeedParser.calculateBikeSpeed(segment);
        assertEquals(12.0, speed.forward(), 0.1);
    }

    // Surface Speed Tests

    @Test
    @DisplayName("Asphalt surface should boost speed with excellent smoothness")
    void surface_asphalt() {
        OvertureRoadSurface surface = new OvertureRoadSurface(RoadSurfaceType.ASPHALT, null);
        OvertureRoadSegment segment = createSegment(OvertureRoadClass.RESIDENTIAL, surface, null, null);
        BikeSpeed speed = OvertureBikeAverageSpeedParser.calculateBikeSpeed(segment);
        // Base 18, EXCELLENT smoothness = 18 * 1.1 = 19.8
        assertEquals(19.8, speed.forward(), 0.1);
    }

    @Test
    @DisplayName("Gravel surface should cap and apply bad smoothness")
    void surface_gravel() {
        OvertureRoadSurface surface = new OvertureRoadSurface(RoadSurfaceType.GRAVEL, null);
        OvertureRoadSegment segment = createSegment(OvertureRoadClass.RESIDENTIAL, surface, null, null);
        BikeSpeed speed = OvertureBikeAverageSpeedParser.calculateBikeSpeed(segment);
        // Base 18, capped to 12 by surface, BAD smoothness = 12 * 0.7 = 8.4
        assertEquals(8.4, speed.forward(), 0.1);
    }

    @Test
    @DisplayName("Dirt surface should apply 0.3 smoothness factor")
    void surface_dirt() {
        OvertureRoadSurface surface = new OvertureRoadSurface(RoadSurfaceType.DIRT, null);
        OvertureRoadSegment segment = createSegment(OvertureRoadClass.RESIDENTIAL, surface, null, null);
        BikeSpeed speed = OvertureBikeAverageSpeedParser.calculateBikeSpeed(segment);
        // Base 18, capped to 10 by surface, then 0.3 factor for HORRIBLE = 3.0
        assertEquals(3.0, speed.forward(), 0.1);
    }

    @Test
    @DisplayName("Unpaved surface should apply 0.4 smoothness factor")
    void surface_unpaved() {
        OvertureRoadSurface surface = new OvertureRoadSurface(RoadSurfaceType.UNPAVED, null);
        OvertureRoadSegment segment = createSegment(OvertureRoadClass.RESIDENTIAL, surface, null, null);
        BikeSpeed speed = OvertureBikeAverageSpeedParser.calculateBikeSpeed(segment);
        // Base 18, capped to 12 by surface, then 0.4 factor for VERY_BAD = 4.8
        assertEquals(4.8, speed.forward(), 0.1);
    }

    // Smoothness Factor Tests

    @Test
    @DisplayName("Excellent smoothness should boost speed by 1.1x")
    void smoothness_excellent() {
        OvertureRoadSurface surface = new OvertureRoadSurface(RoadSurfaceType.ASPHALT, null);
        OvertureRoadSegment segment = createSegment(OvertureRoadClass.TRACK, surface, null, null);
        BikeSpeed speed = OvertureBikeAverageSpeedParser.calculateBikeSpeed(segment);
        // Base 12, EXCELLENT smoothness = 12 * 1.1 = 13.2
        assertEquals(13.2, speed.forward(), 0.1);
    }

    @Test
    @DisplayName("Intermediate smoothness should reduce speed by 0.9x")
    void smoothness_intermediate() {
        OvertureRoadSurface surface = new OvertureRoadSurface(RoadSurfaceType.PAVING_STONES, null);
        OvertureRoadSegment segment = createSegment(OvertureRoadClass.RESIDENTIAL, surface, null, null);
        BikeSpeed speed = OvertureBikeAverageSpeedParser.calculateBikeSpeed(segment);
        // Base 18, capped to 16 by paving stones, INTERMEDIATE = 16 * 0.9 = 14.4
        assertEquals(14.4, speed.forward(), 0.1);
    }

    // Max Speed Limit Tests

    @Test
    @DisplayName("Max speed should cap bike speed")
    void maxSpeed_caps() {
        List<OvertureSpeedLimit> speedLimits = List.of(
                new OvertureSpeedLimit(new OvertureSpeed(10.0, SpeedUnit.KM_H), null, null, null, null));
        OvertureRoadSegment segment =
                createSegment(OvertureRoadClass.RESIDENTIAL, null, speedLimits, null);
        BikeSpeed speed = OvertureBikeAverageSpeedParser.calculateBikeSpeed(segment);
        assertEquals(10.0, speed.forward(), 0.1);
    }

    @Test
    @DisplayName("Higher max speed should not increase bike speed")
    void maxSpeed_noIncrease() {
        List<OvertureSpeedLimit> speedLimits = List.of(
                new OvertureSpeedLimit(new OvertureSpeed(50.0, SpeedUnit.KM_H), null, null, null, null));
        OvertureRoadSegment segment =
                createSegment(OvertureRoadClass.RESIDENTIAL, null, speedLimits, null);
        BikeSpeed speed = OvertureBikeAverageSpeedParser.calculateBikeSpeed(segment);
        assertEquals(18.0, speed.forward(), 0.1);
    }

    // Bidirectional Speed Tests

    @Test
    @DisplayName("Forward speed limit should only apply to forward direction")
    void bidirectional_forwardOnly() {
        PropertyScopeContainer forwardScope = PropertyScopeContainer.ofHeading(TravelHeading.FORWARD);
        List<OvertureSpeedLimit> speedLimits = List.of(new OvertureSpeedLimit(
                new OvertureSpeed(10.0, SpeedUnit.KM_H), null, null, null, forwardScope));
        OvertureRoadSegment segment =
                createSegment(OvertureRoadClass.RESIDENTIAL, null, speedLimits, null);

        BikeSpeed speed = OvertureBikeAverageSpeedParser.calculateBikeSpeed(segment);
        assertEquals(10.0, speed.forward(), 0.1);
        assertEquals(18.0, speed.backward(), 0.1);
    }

    @Test
    @DisplayName("Backward speed limit should only apply to backward direction")
    void bidirectional_backwardOnly() {
        PropertyScopeContainer backwardScope = PropertyScopeContainer.ofHeading(TravelHeading.BACKWARD);
        List<OvertureSpeedLimit> speedLimits = List.of(new OvertureSpeedLimit(
                new OvertureSpeed(8.0, SpeedUnit.KM_H), null, null, null, backwardScope));
        OvertureRoadSegment segment =
                createSegment(OvertureRoadClass.RESIDENTIAL, null, speedLimits, null);

        BikeSpeed speed = OvertureBikeAverageSpeedParser.calculateBikeSpeed(segment);
        assertEquals(18.0, speed.forward(), 0.1);
        assertEquals(8.0, speed.backward(), 0.1);
    }

    @Test
    @DisplayName("Different speeds for forward and backward directions")
    void bidirectional_differentSpeeds() {
        PropertyScopeContainer forwardScope = PropertyScopeContainer.ofHeading(TravelHeading.FORWARD);
        PropertyScopeContainer backwardScope = PropertyScopeContainer.ofHeading(TravelHeading.BACKWARD);
        List<OvertureSpeedLimit> speedLimits = List.of(
                new OvertureSpeedLimit(
                        new OvertureSpeed(15.0, SpeedUnit.KM_H), null, null, null, forwardScope),
                new OvertureSpeedLimit(
                        new OvertureSpeed(10.0, SpeedUnit.KM_H), null, null, null, backwardScope));
        OvertureRoadSegment segment =
                createSegment(OvertureRoadClass.RESIDENTIAL, null, speedLimits, null);

        BikeSpeed speed = OvertureBikeAverageSpeedParser.calculateBikeSpeed(segment);
        assertEquals(15.0, speed.forward(), 0.1);
        assertEquals(10.0, speed.backward(), 0.1);
    }

    @Test
    @DisplayName("parseSpeed should set both directions on edge")
    void bidirectional_parseSpeed() {
        PropertyScopeContainer forwardScope = PropertyScopeContainer.ofHeading(TravelHeading.FORWARD);
        PropertyScopeContainer backwardScope = PropertyScopeContainer.ofHeading(TravelHeading.BACKWARD);
        List<OvertureSpeedLimit> speedLimits = List.of(
                new OvertureSpeedLimit(
                        new OvertureSpeed(15.0, SpeedUnit.KM_H), null, null, null, forwardScope),
                new OvertureSpeedLimit(
                        new OvertureSpeed(10.0, SpeedUnit.KM_H), null, null, null, backwardScope));
        OvertureRoadSegment segment =
                createSegment(OvertureRoadClass.RESIDENTIAL, null, speedLimits, null);

        new OvertureBikeAverageSpeedParser(speedEnc).handleSegment(edge, segment, null);

        assertEquals(15.0, edge.get(speedEnc), 0.1);
        // Previously commented out: parseSpeed computed the backward speed and then discarded it, so
        // both directions carried the forward value.
        assertEquals(10.0, edge.getReverse(speedEnc), 0.1);
    }

    @Test
    @DisplayName("A one-direction encoding stores the lower of the two speeds")
    void bidirectional_singleDirectionEncoding() {
        DecimalEncodedValue oneWayEnc = new DecimalEncodedValueImpl("bike_speed_1dir", 5, 5, false);
        EncodingManager em = EncodingManager.start().add(oneWayEnc).build();
        try (BaseGraph oneDirGraph = new BaseGraph.Builder(em).create()) {
            EdgeIteratorState oneDirEdge = oneDirGraph.edge(0, 1);
            oneDirEdge.setDistance(100);

            PropertyScopeContainer forwardScope = PropertyScopeContainer.ofHeading(TravelHeading.FORWARD);
            PropertyScopeContainer backwardScope =
                    PropertyScopeContainer.ofHeading(TravelHeading.BACKWARD);
            List<OvertureSpeedLimit> speedLimits = List.of(
                    new OvertureSpeedLimit(
                            new OvertureSpeed(15.0, SpeedUnit.KM_H), null, null, null, forwardScope),
                    new OvertureSpeedLimit(
                            new OvertureSpeed(10.0, SpeedUnit.KM_H), null, null, null, backwardScope));
            OvertureRoadSegment segment =
                    createSegment(OvertureRoadClass.RESIDENTIAL, null, speedLimits, null);

            new OvertureBikeAverageSpeedParser(oneWayEnc).handleSegment(oneDirEdge, segment, null);

            // The lower value, so the stored speed never overstates the slower direction.
            assertEquals(10.0, oneDirEdge.get(oneWayEnc), 0.1);
        }
    }

    // Service Road Tests

    @Test
    @DisplayName("Service road without bike access should have pushing speed")
    void serviceRoad_noBikeAccess() {
        OvertureRoadSegment segment =
                createSegment(OvertureRoadClass.SERVICE, null, emptyList(), emptyList());
        BikeSpeed speed = OvertureBikeAverageSpeedParser.calculateBikeSpeed(segment);
        assertEquals(4.0, speed.forward(), 0.1);
    }

    @Test
    @DisplayName("Service road with bike allowed should have normal speed")
    void serviceRoad_bikeAllowed() {
        ArrayList<TravelMode> modes = new ArrayList<>();
        modes.add(TravelMode.BICYCLE);
        PropertyScopeContainer scope = new PropertyScopeContainer(null, null, null, null, modes, null);
        OvertureAccessRestriction bikeAllowed =
                new OvertureAccessRestriction(AccessType.ALLOWED, scope, null);

        OvertureRoadSegment segment =
                createSegment(OvertureRoadClass.SERVICE, null, null, List.of(bikeAllowed));
        BikeSpeed speed = OvertureBikeAverageSpeedParser.calculateBikeSpeed(segment);
        assertEquals(12.0, speed.forward(), 0.1);
    }

    @Test
    @DisplayName("Service road with bike designated should have normal speed")
    void serviceRoad_bikeDesignated() {
        ArrayList<TravelMode> modes = new ArrayList<>();
        modes.add(TravelMode.BICYCLE);
        PropertyScopeContainer scope = new PropertyScopeContainer(null, null, null, null, modes, null);
        OvertureAccessRestriction bikeDesignated =
                new OvertureAccessRestriction(AccessType.DESIGNATED, scope, null);

        OvertureRoadSegment segment =
                createSegment(OvertureRoadClass.SERVICE, null, null, List.of(bikeDesignated));
        BikeSpeed speed = OvertureBikeAverageSpeedParser.calculateBikeSpeed(segment);
        assertEquals(12.0, speed.forward(), 0.1);
    }

    // Minimum Speed Tests

    @Test
    @DisplayName("Speed should never go below 2 km/h")
    void minimumSpeed_enforced() {
        OvertureRoadSegment segment = createSegment(OvertureRoadClass.STEPS, null, null, null);
        BikeSpeed speed = OvertureBikeAverageSpeedParser.calculateBikeSpeed(segment);
        assertEquals(2.0, speed.forward(), 0.1);
    }

    // Null Safety Tests

    @Test
    @DisplayName("Null segment should return default speed")
    void nullSafety_nullSegment() {
        BikeSpeed speed = OvertureBikeAverageSpeedParser.calculateBikeSpeed(null);
        assertEquals(12.0, speed.forward(), 0.1);
    }

    @Test
    @DisplayName("Segment with null properties should return default speed")
    void nullSafety_nullProperties() {
        OvertureRoadSegment segment = new OvertureRoadSegment("test", null, null);
        BikeSpeed speed = OvertureBikeAverageSpeedParser.calculateBikeSpeed(segment);
        assertEquals(12.0, speed.forward(), 0.1);
    }

    // Helper method
    private OvertureRoadSegment createSegment(
            OvertureRoadClass roadClass,
            OvertureRoadSurface surface,
            List<OvertureSpeedLimit> speedLimits,
            List<OvertureAccessRestriction> accessRestrictions) {

        List<OvertureRoadSurface> surfaces = surface == null ? null : List.of(surface);

        OvertureRoadProperties props = new OvertureRoadProperties(
                null,
                null,
                roadClass,
                null,
                null,
                surfaces,
                null,
                speedLimits,
                null,
                null,
                null,
                accessRestrictions,
                0,
                null,
                null,
                null,
                0,
                null,
                null);

        return new OvertureRoadSegment("test_id", null, props);
    }
}
