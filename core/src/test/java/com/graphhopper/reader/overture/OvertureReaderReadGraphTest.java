package com.graphhopper.reader.overture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.graphhopper.reader.overture.road.flags.OvertureRoadFlags;
import com.graphhopper.reader.overture.road.segment.OvertureRoadProperties;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.reader.overture.road.segment.spliter.SegmentSplitter;
import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.PointList;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

public class OvertureReaderReadGraphTest {
    private static final double DELTA = 1e-6;
    private static final double STANDARD_SEGMENT_DISTANCE = 15_000.0;

    private EncodingManager createEncodingManager() {
        return new EncodingManager.Builder()
                .add(new SimpleBooleanEncodedValue("car_access", true))
                .add(new SimpleBooleanEncodedValue("bike_access", true))
                .add(new SimpleBooleanEncodedValue("foot_access", true))
                .add(new DecimalEncodedValueImpl("car_average_speed", 7, 2, true))
                .add(new DecimalEncodedValueImpl("bike_average_speed", 4, 2, true))
                .add(new DecimalEncodedValueImpl("foot_average_speed", 4, 1, true))
                .add(new EnumEncodedValue<>("road_class", RoadClass.class))
                .add(new SimpleBooleanEncodedValue("road_class_link", true))
                .add(new EnumEncodedValue<>("surface", Surface.class))
                .add(new EnumEncodedValue<>("smoothness", Smoothness.class))
                .add(new EnumEncodedValue<>("track_type", TrackType.class))
                .add(new EnumEncodedValue<>("road_environment", RoadEnvironment.class))
                .add(new EnumEncodedValue<>("hazmat", Hazmat.class))
                .build();
    }

    @ParameterizedTest
    @MethodSource("provideSkipFlags")
    @DisplayName("Should skip segments with shouldSkip=true flags")
    public void testReadGraph_skipsSegmentsWithSkipFlags(String flagName, OvertureRoadFlags flag)
            throws IOException {
        EncodingManager em = createEncodingManager();
        BaseGraph graph = new BaseGraph.Builder(em).create();
        OvertureReader reader = spy(new OvertureReader(graph));
        reader.setEncodedValueLookup(em);

        GeometryFactory gf = new GeometryFactory();

        OvertureRoadSegment validSegment = createSegmentWithFlags(
                gf.createLineString(
                        new Coordinate[] {new Coordinate(30.0, 50.0), new Coordinate(30.1, 50.1)}),
                Collections.emptyList());

        OvertureRoadSegment skipSegment = createSegmentWithFlags(
                gf.createLineString(
                        new Coordinate[] {new Coordinate(30.1, 50.1), new Coordinate(30.2, 50.2)}),
                List.of(flag));

        doReturn(List.of(validSegment, skipSegment)).when(reader).parseData();
        reader.readGraph();

        assertEquals(2, graph.getNodes());
        assertEquals(1, graph.getEdges());

        EdgeIteratorState edge = graph.getEdgeIteratorState(0, 1);
        assertNotNull(edge);

        NodeAccess na = graph.getNodeAccess();
        assertEquals(50.0, na.getLat(0), DELTA);
        assertEquals(30.0, na.getLon(0), DELTA);
        assertEquals(50.1, na.getLat(1), DELTA);
        assertEquals(30.1, na.getLon(1), DELTA);
    }

    private static Stream<Arguments> provideSkipFlags() {
        OvertureRoadFlags underConstruction = mock(OvertureRoadFlags.class);
        when(underConstruction.shouldSkip()).thenReturn(true);

        OvertureRoadFlags abandoned = mock(OvertureRoadFlags.class);
        when(abandoned.shouldSkip()).thenReturn(true);

        return Stream.of(
                Arguments.of("under_construction", underConstruction),
                Arguments.of("abandoned", abandoned));
    }

    @Test
    @DisplayName("Should skip segments with less than 2 points")
    public void testReadGraph_skipsSinglePointGeometry() throws IOException {
        EncodingManager em = createEncodingManager();
        BaseGraph graph = new BaseGraph.Builder(em).create();
        OvertureReader reader = spy(new OvertureReader(graph));
        reader.setEncodedValueLookup(em);

        OvertureRoadSegment segment = mock(OvertureRoadSegment.class);
        OvertureRoadProperties props = mock(OvertureRoadProperties.class);

        LineString mockLine = mock(LineString.class);
        when(mockLine.getNumPoints()).thenReturn(1);

        when(segment.getLineString()).thenReturn(mockLine);
        when(segment.getProperties()).thenReturn(props);
        when(props.getFlags()).thenReturn(Collections.emptyList());

        doReturn(Collections.singletonList(segment)).when(reader).parseData();
        reader.readGraph();

        assertEquals(0, graph.getEdges());
        verify(segment, never()).calculateDistance();
    }

    @Test
    @DisplayName("Should merge nodes with same coordinates")
    public void testReadGraph_mergesNodesAtSameCoordinates() throws IOException {
        EncodingManager em = createEncodingManager();
        BaseGraph graph = new BaseGraph.Builder(em).create();
        OvertureReader reader = spy(new OvertureReader(graph));
        reader.setEncodedValueLookup(em);

        GeometryFactory gf = new GeometryFactory();

        OvertureRoadSegment segment1 = createSegmentWithFlags(
                gf.createLineString(
                        new Coordinate[] {new Coordinate(30.0, 50.0), new Coordinate(30.1, 50.1)}),
                Collections.emptyList());

        OvertureRoadSegment segment2 = createSegmentWithFlags(
                gf.createLineString(
                        new Coordinate[] {new Coordinate(30.1, 50.1), new Coordinate(30.2, 50.2)}),
                Collections.emptyList());

        doReturn(List.of(segment1, segment2)).when(reader).parseData();
        reader.readGraph();

        assertEquals(3, graph.getNodes());
        assertEquals(2, graph.getEdges());

        NodeAccess na = graph.getNodeAccess();
        int junctionNode = findNodeByCoordinates(na, 30.1, 50.1, graph.getNodes());
        assertNotEquals(-1, junctionNode);

        EdgeIterator edgeIter = graph.createEdgeExplorer().setBaseNode(junctionNode);
        int edgeCount = 0;
        while (edgeIter.next()) edgeCount++;
        assertEquals(2, edgeCount);
    }

    @Test
    @DisplayName("Should skip segment when start and end nodes are the same")
    public void testReadGraph_skipsSegmentWithSameStartAndEnd() throws IOException {
        EncodingManager em = createEncodingManager();
        BaseGraph graph = new BaseGraph.Builder(em).create();
        OvertureReader reader = spy(new OvertureReader(graph));
        reader.setEncodedValueLookup(em);

        GeometryFactory gf = new GeometryFactory();
        LineString line = gf.createLineString(
                new Coordinate[] {new Coordinate(30.0, 50.0), new Coordinate(30.0, 50.0)});

        OvertureRoadSegment segment = createSegmentWithFlags(line, Collections.emptyList());
        when(segment.calculateDistance()).thenReturn(0.0);

        doReturn(Collections.singletonList(segment)).when(reader).parseData();
        reader.readGraph();

        assertEquals(1, graph.getNodes());
        assertEquals(0, graph.getEdges());
    }

    @Test
    @DisplayName("Should handle segment splitting correctly")
    public void testReadGraph_segmentSplitting() throws IOException {
        EncodingManager em = createEncodingManager();
        BaseGraph graph = new BaseGraph.Builder(em).create();
        OvertureReader reader = spy(new OvertureReader(graph));
        reader.setEncodedValueLookup(em);

        GeometryFactory gf = new GeometryFactory();
        LineString originalLine = gf.createLineString(new Coordinate[] {
            new Coordinate(30.0, 50.0), new Coordinate(30.1, 50.1), new Coordinate(30.2, 50.2)
        });

        OvertureRoadSegment originalSegment = mock(OvertureRoadSegment.class);
        OvertureRoadProperties props = mock(OvertureRoadProperties.class);
        when(originalSegment.getLineString()).thenReturn(originalLine);
        when(originalSegment.getProperties()).thenReturn(props);
        when(originalSegment.calculateDistance()).thenReturn(30000.0);
        when(props.getFlags()).thenReturn(Collections.emptyList());

        OvertureRoadSegment subSegment1 = createSegmentWithFlags(
                gf.createLineString(
                        new Coordinate[] {new Coordinate(30.0, 50.0), new Coordinate(30.1, 50.1)}),
                Collections.emptyList());

        OvertureRoadSegment subSegment2 = createSegmentWithFlags(
                gf.createLineString(
                        new Coordinate[] {new Coordinate(30.1, 50.1), new Coordinate(30.2, 50.2)}),
                Collections.emptyList());

        try (var mockedSplitter = mockStatic(SegmentSplitter.class)) {
            mockedSplitter
                    .when(() -> SegmentSplitter.split(originalSegment))
                    .thenReturn(List.of(subSegment1, subSegment2));

            doReturn(Collections.singletonList(originalSegment)).when(reader).parseData();
            reader.readGraph();

            assertEquals(3, graph.getNodes());
            assertEquals(2, graph.getEdges());
        }
    }

    @Test
    @DisplayName("Should handle way geometry correctly for multi-point segments")
    public void testReadGraph_wayGeometry() throws IOException {
        EncodingManager em = createEncodingManager();
        BaseGraph graph = new BaseGraph.Builder(em).create();
        OvertureReader reader = spy(new OvertureReader(graph));
        reader.setEncodedValueLookup(em);

        GeometryFactory gf = new GeometryFactory();
        LineString line = gf.createLineString(new Coordinate[] {
            new Coordinate(30.0, 50.0),
            new Coordinate(30.05, 50.05),
            new Coordinate(30.1, 50.1),
            new Coordinate(30.15, 50.15)
        });

        OvertureRoadSegment segment = createSegmentWithFlags(line, Collections.emptyList());
        when(segment.calculateDistance()).thenReturn(45000.0);

        doReturn(Collections.singletonList(segment)).when(reader).parseData();
        reader.readGraph();

        assertEquals(2, graph.getNodes());
        assertEquals(1, graph.getEdges());

        EdgeIteratorState edge = graph.getEdgeIteratorState(0, 1);
        PointList wayGeometry = edge.fetchWayGeometry(FetchMode.PILLAR_ONLY);
        assertEquals(2, wayGeometry.size());

        assertEquals(50.05, wayGeometry.getLat(0), DELTA);
        assertEquals(30.05, wayGeometry.getLon(0), DELTA);
        assertEquals(50.1, wayGeometry.getLat(1), DELTA);
        assertEquals(30.1, wayGeometry.getLon(1), DELTA);
    }

    @Test
    @DisplayName("Should handle segments crossing anti-meridian")
    public void testReadGraph_negativeCoordinates() throws IOException {
        EncodingManager em = createEncodingManager();
        BaseGraph graph = new BaseGraph.Builder(em).create();
        OvertureReader reader = spy(new OvertureReader(graph));
        reader.setEncodedValueLookup(em);

        GeometryFactory gf = new GeometryFactory();

        LineString line = gf.createLineString(
                new Coordinate[] {new Coordinate(179.9, 50.0), new Coordinate(-179.9, 50.0)});

        OvertureRoadSegment segment = createSegmentWithFlags(line, Collections.emptyList());
        when(segment.calculateDistance()).thenReturn(20000.0);

        doReturn(Collections.singletonList(segment)).when(reader).parseData();
        reader.readGraph();

        assertEquals(2, graph.getNodes());
        assertEquals(1, graph.getEdges());

        NodeAccess na = graph.getNodeAccess();
        assertEquals(50.0, na.getLat(0), DELTA);
        assertEquals(179.9, na.getLon(0), DELTA);
        assertEquals(50.0, na.getLat(1), DELTA);
        assertEquals(-179.9, na.getLon(1), DELTA);
    }

    @Test
    @DisplayName("Should handle segments with invalid coordinates gracefully")
    public void testReadGraph_invalidCoordinates() throws IOException {
        EncodingManager em = createEncodingManager();
        BaseGraph graph = new BaseGraph.Builder(em).create();
        OvertureReader reader = spy(new OvertureReader(graph));
        reader.setEncodedValueLookup(em);

        LineString mockLine = mock(LineString.class);
        Coordinate invalidCoord = new Coordinate(Double.NaN, Double.POSITIVE_INFINITY);
        when(mockLine.getNumPoints()).thenReturn(2);
        when(mockLine.getCoordinateN(0)).thenReturn(invalidCoord);
        when(mockLine.getCoordinateN(1)).thenReturn(invalidCoord);

        OvertureRoadSegment segment = mock(OvertureRoadSegment.class);
        OvertureRoadProperties props = mock(OvertureRoadProperties.class);
        when(segment.getLineString()).thenReturn(mockLine);
        when(segment.getProperties()).thenReturn(props);
        when(props.getFlags()).thenReturn(Collections.emptyList());

        doReturn(Collections.singletonList(segment)).when(reader).parseData();

        assertDoesNotThrow(reader::readGraph);
        assertEquals(0, graph.getEdges());
    }

    private int findNodeByCoordinates(
            NodeAccess na, double expectedLon, double expectedLat, int totalNodes) {
        for (int i = 0; i < totalNodes; i++) {
            if (Math.abs(na.getLat(i) - expectedLat) < DELTA
                    && Math.abs(na.getLon(i) - expectedLon) < DELTA) {
                return i;
            }
        }
        return -1;
    }

    private OvertureRoadSegment createSegmentWithFlags(
            LineString line, List<OvertureRoadFlags> flags) {
        OvertureRoadSegment segment = mock(OvertureRoadSegment.class);
        OvertureRoadProperties props = mock(OvertureRoadProperties.class);
        when(segment.getLineString()).thenReturn(line);
        when(segment.getProperties()).thenReturn(props);
        when(segment.calculateDistance()).thenReturn(STANDARD_SEGMENT_DISTANCE);
        when(props.getFlags()).thenReturn(flags);
        return segment;
    }
}
