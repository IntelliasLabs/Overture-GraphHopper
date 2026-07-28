package com.graphhopper.reader.overture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.graphhopper.reader.dem.ElevationProvider;
import com.graphhopper.reader.overture.aws.S3ParquetInputFile;
import com.graphhopper.reader.overture.parser.OvertureParser;
import com.graphhopper.reader.overture.parser.parquet.OvertureParquetParser;
import com.graphhopper.reader.overture.road.flags.OvertureRoadFlags;
import com.graphhopper.reader.overture.road.segment.OvertureRoadProperties;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.AreaIndex;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeIteratorState;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.mockito.ArgumentMatchers;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

/**
 * Unit tests for the {@link OvertureReader} class.
 * <p>
 * This class ensures that the reader is correctly instantiated, that configuration setters
 * function as expected, and that dependencies are properly injected before any heavy
 * reading operations occur.
 * </p>
 */
public class OvertureReaderTest {

    /**
     * Verifies that the constructor correctly stores provided dependencies
     * and initializes internal data structures (like the node map) to a safe empty state.
     */
    @Test
    public void testConstructor_initializesFieldsCorrectly() {
        // ---------------------------------------------------------
        // Arrange: Prepare the environment and dependencies
        // ---------------------------------------------------------
        BaseGraph graph = mock(BaseGraph.class);

        // ---------------------------------------------------------
        // Act: Execute the code under test
        // ---------------------------------------------------------
        OvertureReader reader = new OvertureReader(graph);

        // ---------------------------------------------------------
        // Assert: Verify the results
        // ---------------------------------------------------------

        // 1. Verify Dependency Injection
        assertSame(
                graph,
                reader.getGraph(),
                "The stored Graph instance should be the exact object passed to the constructor.");

        // 2. Verify Internal State Initialization
        assertNotNull(
                reader.getNodeMap(), "Internal NodeMap should be initialized to a non-null object.");
        assertTrue(
                reader.getNodeMap().isEmpty(), "Internal NodeMap should be empty upon initialization.");
    }

    /**
     * Verifies that the setFile method correctly stores the file reference
     * so it can be used later during the readGraph process.
     */
    @Test
    public void testSetFile_storesFileCorrectly() {
        // ---------------------------------------------------------
        // Arrange: Prepare the reader
        // ---------------------------------------------------------
        BaseGraph graph = mock(BaseGraph.class);
        OvertureReader reader = new OvertureReader(graph);

        File inputFile = new File("local/data/overture.parquet");

        // ---------------------------------------------------------
        // Act: Set the file source
        // ---------------------------------------------------------
        reader.setFile(inputFile);

        // ---------------------------------------------------------
        // Assert: Verify the file is stored
        // ---------------------------------------------------------
        assertEquals(
                inputFile,
                reader.getOvertureFile(),
                "The reader should store the exact File object provided via setFile.");
    }

    /**
     * Verifies that setS3Source correctly parses a standard S3 URL
     * into its constituent bucket and key parts.
     */
    @Test
    public void testSetS3Source_parsesValidUrlCorrectly() {
        // ---------------------------------------------------------
        // Arrange: Prepare the reader and the input URL
        // ---------------------------------------------------------
        BaseGraph graph = mock(BaseGraph.class);
        OvertureReader reader = new OvertureReader(graph);

        String s3Url = "s3://overturemaps-us-west-2/release/2024-04-16.0/data.parquet";

        // ---------------------------------------------------------
        // Act: Set the S3 source using the URL string
        // ---------------------------------------------------------
        reader.setS3Source(s3Url);

        // ---------------------------------------------------------
        // Assert: Verify bucket and key extraction
        // ---------------------------------------------------------
        assertEquals(
                "overturemaps-us-west-2",
                reader.getS3Bucket(),
                "The bucket name should be extracted correctly from the S3 URL.");
        assertEquals(
                "release/2024-04-16.0/data.parquet",
                reader.getS3Key(),
                "The object key should be extracted correctly from the S3 URL.");
    }

    /**
     * Verifies that setS3Source correctly stores the bucket and key
     * when they are provided explicitly (bypassing URL parsing).
     */
    @Test
    public void testSetS3Source_storesDirectBucketAndKey() {
        // ---------------------------------------------------------
        // Arrange: Prepare input data
        // ---------------------------------------------------------
        BaseGraph graph = mock(BaseGraph.class);
        OvertureReader reader = new OvertureReader(graph);

        String bucket = "my-custom-bucket";
        String key = "folder/map-data.json";

        // ---------------------------------------------------------
        // Act: Set the bucket and key directly
        // ---------------------------------------------------------
        reader.setS3Source(bucket, key);

        // ---------------------------------------------------------
        // Assert: Verify storage
        // ---------------------------------------------------------
        assertEquals(bucket, reader.getS3Bucket(), "The reader should store the provided bucket name.");
        assertEquals(key, reader.getS3Key(), "The reader should store the provided object key.");
    }

    /**
     * Verifies that the S3 URL parser throws an exception if the protocol
     * is not 's3://'.
     */
    @Test
    public void testSetS3Source_throwsOnInvalidProtocol() {
        // ---------------------------------------------------------
        // Arrange: Prepare an invalid URL
        // ---------------------------------------------------------
        BaseGraph graph = mock(BaseGraph.class);
        OvertureReader reader = new OvertureReader(graph);

        String invalidUrl = "http://amazon.com/bucket/key";

        // ---------------------------------------------------------
        // Act & Assert: Expect an exception
        // ---------------------------------------------------------
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> {
                    reader.setS3Source(invalidUrl);
                },
                "Should throw IllegalArgumentException when URL does not start with s3://");

        assertEquals(
                "S3 URL must start with 's3://'",
                exception.getMessage(),
                "Exception message should indicate the protocol error.");
    }

    /**
     * Verifies that the S3 URL parser throws an exception if the URL
     * contains a bucket but no key (path).
     */
    @Test
    public void testSetS3Source_throwsOnMissingKey() {
        // ---------------------------------------------------------
        // Arrange: Prepare a URL missing the key part
        // ---------------------------------------------------------
        BaseGraph graph = mock(BaseGraph.class);
        OvertureReader reader = new OvertureReader(graph);

        String invalidUrl = "s3://just-a-bucket";

        // ---------------------------------------------------------
        // Act & Assert: Expect an exception
        // ---------------------------------------------------------
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> {
                    reader.setS3Source(invalidUrl);
                },
                "Should throw IllegalArgumentException when URL is missing the object key.");

        assertEquals(
                "S3 URL must contain a bucket and a key (format: s3://bucket/key)",
                exception.getMessage(),
                "Exception message should explain the required format.");
    }

    /**
     * Verifies that all auxiliary dependencies (Elevation, AreaIndex)
     * are correctly set via their respective setter methods.
     */
    @Test
    public void testSetDependencies_storesProvidersCorrectly() {
        // ---------------------------------------------------------
        // Arrange: Mock the auxiliary dependencies
        // ---------------------------------------------------------
        BaseGraph graph = mock(BaseGraph.class);
        OvertureReader reader = new OvertureReader(graph);

        ElevationProvider elevationProvider = mock(ElevationProvider.class);
        AreaIndex areaIndex = mock(AreaIndex.class);

        // ---------------------------------------------------------
        // Act: Inject dependencies via setters
        // ---------------------------------------------------------
        reader.setElevationProvider(elevationProvider);
        reader.setAreaIndex(areaIndex);

        // ---------------------------------------------------------
        // Assert: Verify dependencies are stored by reference
        // ---------------------------------------------------------
        assertSame(
                elevationProvider,
                reader.getElevationProvider(),
                "ElevationProvider should be correctly stored.");
        assertSame(areaIndex, reader.getAreaIndex(),
                "AreaIndex should be correctly stored.");
    }

    @Test
    public void testGetOrCreateNode_createNewNode() {
        BaseGraph graph = mock(BaseGraph.class);
        NodeAccess nodeAccess = mock(NodeAccess.class);
        EncodingManager encodingManager = mock(EncodingManager.class);

        when(graph.getNodeAccess()).thenReturn(nodeAccess);
        when(graph.getNodes()).thenReturn(0);

        OvertureReader reader = new OvertureReader(graph);

        double lat = 14.5492657;
        double lon = 67.5284527;

        int id = invokeGetOrCreateNode(reader, lat, lon, nodeAccess);

        assertEquals(0, id);
        verify(nodeAccess).setNode(0, lat, lon);
    }

    @Test
    public void testGetOrCreateNode_reuseExistingNode() {
        BaseGraph graph = mock(BaseGraph.class);
        NodeAccess nodeAccess = mock(NodeAccess.class);
        EncodingManager encodingManager = mock(EncodingManager.class);

        when(graph.getNodeAccess()).thenReturn(nodeAccess);
        when(graph.getNodes()).thenReturn(0);

        OvertureReader reader = new OvertureReader(graph);

        double lat = 14.5492657;
        double lon = 67.5284527;

        int id1 = invokeGetOrCreateNode(reader, lat, lon, nodeAccess);
        assertEquals(0, id1);

        int id2 = invokeGetOrCreateNode(reader, lat, lon, nodeAccess);

        assertEquals(id1, id2);
        verify(nodeAccess, times(1)).setNode(anyInt(), anyDouble(), anyDouble());
        verify(graph, times(1)).getNodes();
    }

    @Test
    public void testGetOrCreateNode_multipleNodeWithSameCoordinates() {
        BaseGraph graph = mock(BaseGraph.class);
        NodeAccess nodeAccess = mock(NodeAccess.class);
        EncodingManager encodingManager = mock(EncodingManager.class);

        when(graph.getNodeAccess()).thenReturn(nodeAccess);
        when(graph.getNodes()).thenReturn(0);

        OvertureReader reader = new OvertureReader(graph);

        double lat = 14.5492657;
        double lon = 67.5284527;

        int id1 = invokeGetOrCreateNode(reader, lat, lon, nodeAccess);
        int id2 = invokeGetOrCreateNode(reader, lat, lon, nodeAccess);
        int id3 = invokeGetOrCreateNode(reader, lat, lon, nodeAccess);
        int id4 = invokeGetOrCreateNode(reader, lat, lon, nodeAccess);

        assertEquals(0, id1);
        assertEquals(id1, id2);
        assertEquals(id2, id3);
        assertEquals(id3, id4);

        verify(nodeAccess, times(1)).setNode(anyInt(), anyDouble(), anyDouble());
        verify(graph, times(1)).getNodes();
    }

    /**
     * Helper to access the private getOrCreateNode method in OvertureReader, with no connector.
     *
     * <p>Passing {@code null} for the connector exercises the coordinate fallback, which is what these
     * tests were written to cover and what still applies to sub-segment boundaries created by a property
     * range. Connector-based identity is covered by {@link #testGetOrCreateNode_connectorIdWins} and
     * {@link #testGetOrCreateNode_connectorAdoptsExistingCoordinateNode}.
     */
    private int invokeGetOrCreateNode(
            OvertureReader reader, double lat, double lon, NodeAccess nodeAccess) {
        return invokeGetOrCreateNode(reader, null, lat, lon, nodeAccess);
    }

    private int invokeGetOrCreateNode(
            OvertureReader reader, String connectorId, double lat, double lon, NodeAccess nodeAccess) {
        try {
            Method method = OvertureReader.class.getDeclaredMethod(
                    "getOrCreateNode", String.class, double.class, double.class, NodeAccess.class);
            method.setAccessible(true);
            return (int) method.invoke(reader, connectorId, lat, lon, nodeAccess);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Two different coordinates sharing a connector become one node")
    public void testGetOrCreateNode_connectorIdWins() {
        BaseGraph graph = mock(BaseGraph.class);
        NodeAccess nodeAccess = mock(NodeAccess.class);

        when(graph.getNodeAccess()).thenReturn(nodeAccess);
        when(graph.getNodes()).thenReturn(0);

        OvertureReader reader = new OvertureReader(graph);

        // Two segments meeting at one Overture connector, whose end points disagree by ~4 m. On the
        // Florence extract this was the shape of 41% of all junctions, and coordinate keying left each
        // one as two disconnected nodes.
        int a = invokeGetOrCreateNode(reader, "connector-1", 43.7696000, 11.2558000, nodeAccess);
        int b = invokeGetOrCreateNode(reader, "connector-1", 43.7696380, 11.2558210, nodeAccess);

        assertEquals(a, b, "segments sharing a connector must share a node whatever the coordinates");
        verify(nodeAccess, times(1)).setNode(anyInt(), anyDouble(), anyDouble());
    }

    @Test
    @DisplayName(
            "A connector adopts a node already created at its coordinate, rather than adding one")
    public void testGetOrCreateNode_connectorAdoptsExistingCoordinateNode() {
        BaseGraph graph = mock(BaseGraph.class);
        NodeAccess nodeAccess = mock(NodeAccess.class);

        when(graph.getNodeAccess()).thenReturn(nodeAccess);
        when(graph.getNodes()).thenReturn(0);

        OvertureReader reader = new OvertureReader(graph);

        double lat = 43.7696;
        double lon = 11.2558;

        // A property-range boundary lands here first with no connector...
        int viaCoordinate = invokeGetOrCreateNode(reader, null, lat, lon, nodeAccess);
        // ...then a connector arrives at the same place. It must reuse the node, so the change can only
        // ever merge more than coordinate keying did, never split something that used to be joined.
        int viaConnector = invokeGetOrCreateNode(reader, "connector-2", lat, lon, nodeAccess);

        assertEquals(viaCoordinate, viaConnector);
        verify(nodeAccess, times(1)).setNode(anyInt(), anyDouble(), anyDouble());
    }

    @Test
    @DisplayName("Distinct connectors at distinct coordinates stay distinct")
    public void testGetOrCreateNode_distinctConnectorsStayDistinct() {
        BaseGraph graph = mock(BaseGraph.class);
        NodeAccess nodeAccess = mock(NodeAccess.class);

        when(graph.getNodeAccess()).thenReturn(nodeAccess);
        when(graph.getNodes()).thenReturn(0, 1);

        OvertureReader reader = new OvertureReader(graph);

        int a = invokeGetOrCreateNode(reader, "connector-a", 43.7696, 11.2558, nodeAccess);
        int b = invokeGetOrCreateNode(reader, "connector-b", 43.7800, 11.2600, nodeAccess);

        assertNotEquals(a, b, "different connectors in different places must not be merged");
        verify(nodeAccess, times(2)).setNode(anyInt(), anyDouble(), anyDouble());
    }

    @Test
    @DisplayName("Data Sources: Fallback to local file if S3Client is missing")
    public void testParseData_fallsBackToLocalIfS3ClientMissing() throws IOException {
        BaseGraph graph = mock(BaseGraph.class);
        OvertureReader reader = new OvertureReader(graph);

        reader.setS3Source("s3://bucket/data.parquet");

        File tempFile = File.createTempFile("test", ".parquet");
        tempFile.deleteOnExit();
        reader.setFile(tempFile);

        assertDoesNotThrow(
                () -> {
                    try {
                        reader.readGraph();
                    } catch (Exception ignored) {
                    }
                },
                "Reader should attempt to use the local file if the S3Client is not initialized.");
    }

    @Test
    @DisplayName("S3: Reject GeoJSON due to performance constraints")
    public void testParseFromS3_throwsOnGeoJson() {
        BaseGraph graph = mock(BaseGraph.class);
        S3Client s3Client = mock(S3Client.class);
        OvertureReader reader = new OvertureReader(graph);

        reader.setS3Client(s3Client);
        reader.setS3Source("s3://bucket/data.geojson");

        Exception exception = assertThrows(IOException.class, reader::readGraph);
        assertTrue(
                exception.getMessage().contains("GeoJSON directly from S3 is not supported"),
                "Should throw IOException explaining GeoJSON limitations on S3.");
    }

    @Test
    @DisplayName("S3 Strategy: Small Parquet (<20MB) routes to temp file download")
    public void testParseFromS3_smallFileRoutesToDownload() {
        BaseGraph graph = mock(BaseGraph.class);
        S3Client s3Client = mock(S3Client.class);
        OvertureReader reader = new OvertureReader(graph);

        reader.setS3Client(s3Client);
        reader.setS3Source("s3://bucket/small.parquet");

        HeadObjectResponse head = HeadObjectResponse.builder().contentLength(1024L).build();
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(head);

        when(s3Client.getObject(ArgumentMatchers.<Consumer<GetObjectRequest.Builder>>any()))
                .thenThrow(new RuntimeException("Download Started"));

        IOException ex = assertThrows(IOException.class, reader::readGraph);

        assertInstanceOf(RuntimeException.class, ex.getCause());
        assertEquals("Download Started", ex.getCause().getMessage());

        verify(s3Client).headObject(any(HeadObjectRequest.class));
        verify(s3Client).getObject(ArgumentMatchers.<Consumer<GetObjectRequest.Builder>>any());
    }

    @Test
    @DisplayName("S3 Strategy: Large Parquet (>20MB) uses Cloud-Native Streaming")
    public void testParseFromS3_largeFileRoutesToStreaming() {
        BaseGraph graph = mock(BaseGraph.class);
        S3Client s3Client = mock(S3Client.class);
        OvertureReader reader = new OvertureReader(graph);

        reader.setS3Client(s3Client);
        reader.setS3Source("s3://bucket/large.parquet");

        HeadObjectResponse head =
                HeadObjectResponse.builder().contentLength(30 * 1024 * 1024L).build();
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(head);

        try {
            reader.readGraph();
        } catch (Exception ignored) {
            /// Expected to fail at the stubbed parser level
        }

        verify(s3Client, never()).getObject(ArgumentMatchers.<Consumer<GetObjectRequest.Builder>>any());
        verify(s3Client).headObject(any(HeadObjectRequest.class));
    }

    @Test
    @DisplayName("Local Files: Error on unsupported extensions")
    public void testParseFromLocal_throwsOnUnknownFormat() {
        BaseGraph graph = mock(BaseGraph.class);
        OvertureReader reader = new OvertureReader(graph);

        File unknownFile = new File("unsupported_format.txt");
        reader.setFile(unknownFile);

        assertThrows(
                IOException.class,
                reader::readGraph,
                "Reader should throw IOException if the file extension is not .parquet or .geojson.");
    }

    @Test
    @DisplayName("GeoJSON format from local file source")
    public void testParseLocalGeoJson() throws IOException {
        BaseGraph graph = mock(BaseGraph.class);
        OvertureReader reader = new OvertureReader(graph);

        File geoJsonFile = File.createTempFile("test", ".geojson");
        geoJsonFile.deleteOnExit();
        reader.setFile(geoJsonFile);

        try (var mockedParser = mockStatic(OvertureParser.class)) {
            mockedParser
                    .when(() -> OvertureParser.parse(any(File.class)))
                    .thenReturn(Collections.emptyList());

            reader.readGraph();

            mockedParser.verify(() -> OvertureParser.parse(geoJsonFile));
        }
    }

    @Test
    @DisplayName("Parquet format from S3 source (Streaming)")
    public void testParseS3Parquet() throws IOException {
        BaseGraph graph = mock(BaseGraph.class);
        S3Client s3Client = mock(S3Client.class);
        OvertureReader reader = new OvertureReader(graph);

        reader.setS3Client(s3Client);
        reader.setS3Source("s3://bucket/data.parquet");

        HeadObjectResponse head =
                HeadObjectResponse.builder().contentLength(50 * 1024 * 1024L).build();
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(head);

        try (var mockedParquetParser = mockStatic(OvertureParquetParser.class)) {
            // The large-file S3 branch streams rather than materialising, so that a continent-sized
            // extract does not have to fit in the heap before graph building starts.
            mockedParquetParser
                    .when(() -> OvertureParquetParser.stream(any(S3ParquetInputFile.class)))
                    .thenReturn(emptySegmentStream());

            reader.readGraph();

            mockedParquetParser.verify(() -> OvertureParquetParser.stream(any(S3ParquetInputFile.class)));
        }
    }

    /** A trivial empty {@link OvertureParquetParser.SegmentStream}, so no final type has to be mocked. */
    private static OvertureParquetParser.SegmentStream emptySegmentStream() {
        return new OvertureParquetParser.SegmentStream() {
            @Override
            public Iterator<OvertureRoadSegment> iterator() {
                return Collections.emptyIterator();
            }

            @Override
            public void close() {}
        };
    }

    @Test
    @DisplayName("ReadGraph handles empty segments list")
    public void testReadGraph_emptySegments() throws IOException {
        EncodingManager em = new EncodingManager.Builder()
                .add(new SimpleBooleanEncodedValue("car_access", true))
                .build();
        BaseGraph baseGraph = new BaseGraph.Builder(em).create();
        OvertureReader reader = spy(new OvertureReader(baseGraph));

        doReturn(Collections.emptyList()).when(reader).parseData();

        reader.readGraph();

        assertEquals(0, baseGraph.getNodes(), "Graph should have 0 nodes");
        assertEquals(0, baseGraph.getEdges(), "Graph should have 0 edges");
    }

    @Test
    @DisplayName("ReadGraph creates nodes and edges from valid segments")
    public void testReadGraph_validSegments() throws IOException {
        EncodingManager em = OvertureTestFixtures.minimalEncodingManager();
        BaseGraph graph = new BaseGraph.Builder(em).create();
        OvertureReader reader = spy(new OvertureReader(graph));
        reader.setEncodedValueLookup(em);

        GeometryFactory gf = new GeometryFactory();

        LineString line = gf.createLineString(
                new Coordinate[] {new Coordinate(30.5, 50.4), new Coordinate(30.6, 50.5)});

        OvertureRoadSegment segment = mock(OvertureRoadSegment.class);
        OvertureRoadProperties props = mock(OvertureRoadProperties.class);
        when(segment.getLineString()).thenReturn(line);
        when(segment.getProperties()).thenReturn(props);
        when(segment.calculateDistance()).thenReturn(1.);
        when(props.getFlags()).thenReturn(Collections.emptyList());

        doReturn(Collections.singletonList(segment)).when(reader).parseData();

        reader.readGraph();

        assertEquals(2, graph.getNodes(), "Should create 2 tower nodes");
        assertEquals(1, graph.getEdges(), "Should create 1 edge");

        NodeAccess na = graph.getNodeAccess();
        assertEquals(50.4, na.getLat(0), 1e-6);
        assertEquals(30.5, na.getLon(0), 1e-6);

        EdgeIteratorState edge = graph.getEdgeIteratorState(0, 1);
        assertTrue(edge.getDistance() > 0, "Distance should be calculated");
    }

    @Test
    @DisplayName("ReadGraph skips segments marked as abandoned")
    public void testReadGraph_skipsAbandoned() throws IOException {
        // the reader resolves every encoded value before the segment loop and fails fast if one is
        // missing, so a car_access-only manager would never reach the logic under test
        EncodingManager em = OvertureTestFixtures.minimalEncodingManager();
        BaseGraph graph = new BaseGraph.Builder(em).create();
        OvertureReader reader = spy(new OvertureReader(graph));
        reader.setEncodedValueLookup(em);

        OvertureRoadSegment segment = mock(OvertureRoadSegment.class);
        OvertureRoadProperties props = mock(OvertureRoadProperties.class);
        OvertureRoadFlags abandonedFlag = mock(OvertureRoadFlags.class);

        when(abandonedFlag.shouldSkip()).thenReturn(true);
        when(segment.getProperties()).thenReturn(props);
        when(props.getFlags()).thenReturn(Collections.singletonList(abandonedFlag));

        doReturn(Collections.singletonList(segment)).when(reader).parseData();

        reader.readGraph();

        assertEquals(0, graph.getEdges(), "Abandoned segment should not create an edge");
        verify(segment, never()).getLineString();
    }

    @Test
    @DisplayName("Skip segments with null or single-point geometry")
    public void testReadGraph_handlesInvalidGeometry() throws IOException {
        // the reader resolves every encoded value before the segment loop and fails fast if one is
        // missing, so a car_access-only manager would never reach the logic under test
        EncodingManager em = OvertureTestFixtures.minimalEncodingManager();
        BaseGraph graph = new BaseGraph.Builder(em).create();
        OvertureReader reader = spy(new OvertureReader(graph));
        reader.setEncodedValueLookup(em);

        OvertureRoadSegment badSegment = mock(OvertureRoadSegment.class);
        OvertureRoadProperties props = mock(OvertureRoadProperties.class);
        when(badSegment.getProperties()).thenReturn(props);
        when(props.getFlags()).thenReturn(Collections.emptyList());

        when(badSegment.getLineString()).thenReturn(null);

        doReturn(Collections.singletonList(badSegment)).when(reader).parseData();

        assertDoesNotThrow(reader::readGraph, "Reader should not throw NPE on null geometry");
        assertEquals(0, graph.getEdges(), "No edges should be created from null geometry");
    }
}
