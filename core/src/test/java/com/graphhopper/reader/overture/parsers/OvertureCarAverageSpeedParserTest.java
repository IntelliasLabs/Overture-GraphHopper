package com.graphhopper.reader.overture.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.graphhopper.reader.overture.common.speed.OvertureSpeed;
import com.graphhopper.reader.overture.common.speed.OvertureSpeedLimit;
import com.graphhopper.reader.overture.common.speed.SpeedUnit;
import com.graphhopper.reader.overture.road.segment.OvertureRoadClass;
import com.graphhopper.reader.overture.road.segment.OvertureRoadProperties;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSubclass;
import com.graphhopper.reader.overture.road.surface.OvertureRoadSurface;
import com.graphhopper.reader.overture.road.surface.RoadSurfaceType;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.DecimalEncodedValueImpl;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.util.EdgeIteratorState;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class OvertureCarAverageSpeedParserTest {
    private DecimalEncodedValue speedEnc;
    private EdgeIteratorState edge;
    private BaseGraph graph;

    @BeforeEach
    void setup() {
        speedEnc = new DecimalEncodedValueImpl("car_speed", 5, 5, true);
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

    @Test
    @DisplayName("Should apply 0.9 factor to explicit max speed limits")
    void parseSpeed_ExplicitMaxSpeed() {
        OvertureRoadSegment segment = createSegment(100.0, OvertureRoadClass.MOTORWAY, null, null);
        OvertureCarAverageSpeedParser.parseSpeed(edge, segment, speedEnc);
        assertEquals(90.0, edge.get(speedEnc), 0.1);
    }

    @Test
    @DisplayName("Should use road class defaults without 0.9 factor")
    void parseSpeed_RoadClassDefault() {
        OvertureRoadSegment segment = createSegment(null, OvertureRoadClass.PRIMARY, null, null);
        OvertureCarAverageSpeedParser.parseSpeed(edge, segment, speedEnc);
        assertEquals(65.0, edge.get(speedEnc), 0.1);
    }

    @Test
    @DisplayName("Should use link specific speed for road class with link subclass")
    void parseSpeed_LinkSubclass() {
        OvertureRoadSegment segment =
                createSegment(null, OvertureRoadClass.MOTORWAY, OvertureRoadSubclass.LINK, null);
        OvertureCarAverageSpeedParser.parseSpeed(edge, segment, speedEnc);
        assertEquals(70.0, edge.get(speedEnc), 0.1);
    }

    @Test
    @DisplayName("Should cap speed to 30 km/h on bad surfaces like gravel")
    void parseSpeed_BadSurface_Gravel() {
        OvertureRoadSurface surface = new OvertureRoadSurface(RoadSurfaceType.GRAVEL, null);
        OvertureRoadSegment segment = createSegment(null, OvertureRoadClass.MOTORWAY, null, surface);
        OvertureCarAverageSpeedParser.parseSpeed(edge, segment, speedEnc);
        assertEquals(30.0, edge.get(speedEnc), 0.1);
    }

    @Test
    @DisplayName("Should cap even explicit high speed limits on dirt surfaces")
    void parseSpeed_ExplicitHighSpeedOnDirt() {
        OvertureRoadSurface surface = new OvertureRoadSurface(RoadSurfaceType.DIRT, null);
        OvertureRoadSegment segment = createSegment(120.0, OvertureRoadClass.MOTORWAY, null, surface);
        OvertureCarAverageSpeedParser.parseSpeed(edge, segment, speedEnc);
        assertEquals(30.0, edge.get(speedEnc), 0.1);
    }

    @Test
    @DisplayName("Should not cap speed on paved asphalt surface")
    void parseSpeed_PavedSurface() {
        OvertureRoadSurface surface = new OvertureRoadSurface(RoadSurfaceType.ASPHALT, null);
        OvertureRoadSegment segment = createSegment(null, OvertureRoadClass.MOTORWAY, null, surface);
        OvertureCarAverageSpeedParser.parseSpeed(edge, segment, speedEnc);
        assertEquals(100.0, edge.get(speedEnc), 0.1);
    }

    @Test
    @DisplayName("Should fallback to default speed for unknown road class")
    void parseSpeed_UnknownClass() {
        OvertureRoadSegment segment = createSegment(null, null, null, null);
        OvertureCarAverageSpeedParser.parseSpeed(edge, segment, speedEnc);
        assertEquals(20.0, edge.get(speedEnc), 0.1);
    }

    @Test
    @DisplayName("Should not increase speed if it is already below 30 on bad surface")
    void parseSpeed_AlreadyLowSpeedOnBadSurface() {
        OvertureRoadSurface surface = new OvertureRoadSurface(RoadSurfaceType.DIRT, null);
        OvertureRoadSegment segment = createSegment(null, OvertureRoadClass.TRACK, null, surface);
        OvertureCarAverageSpeedParser.parseSpeed(edge, segment, speedEnc);
        assertEquals(15.0, edge.get(speedEnc), 0.1);
    }

    @Test
    @DisplayName("Should cap speed on PAVING_STONES as it is in BAD_SURFACES set")
    void parseSpeed_PavingStones() {
        OvertureRoadSurface surface = new OvertureRoadSurface(RoadSurfaceType.PAVING_STONES, null);
        OvertureRoadSegment segment = createSegment(null, OvertureRoadClass.MOTORWAY, null, surface);
        OvertureCarAverageSpeedParser.parseSpeed(edge, segment, speedEnc);
        assertEquals(30.0, edge.get(speedEnc), 0.1);
    }

    @Test
    @DisplayName("Explicit speed limit should override link default speed")
    void parseSpeed_ExplicitOnLink() {
        OvertureRoadSegment segment =
                createSegment(50.0, OvertureRoadClass.MOTORWAY, OvertureRoadSubclass.LINK, null);
        OvertureCarAverageSpeedParser.parseSpeed(edge, segment, speedEnc);
        assertEquals(45.0, edge.get(speedEnc), 0.1);
    }

    private OvertureRoadSegment createSegment(
            Double maxSpeed,
            OvertureRoadClass roadClass,
            OvertureRoadSubclass subclass,
            OvertureRoadSurface surface) {
        List<OvertureSpeedLimit> speedLimits = maxSpeed == null
                ? null
                : List.of(new OvertureSpeedLimit(
                        new OvertureSpeed(maxSpeed, SpeedUnit.KM_H), null, null, null, null));
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
                subclass,
                null,
                null,
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
