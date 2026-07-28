package com.graphhopper.reader.overture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import com.graphhopper.reader.overture.road.segment.OvertureRoadProperties;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.routing.ev.DecimalEncodedValueImpl;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.ev.Hazmat;
import com.graphhopper.routing.ev.RoadClass;
import com.graphhopper.routing.ev.RoadEnvironment;
import com.graphhopper.routing.ev.SimpleBooleanEncodedValue;
import com.graphhopper.routing.ev.Smoothness;
import com.graphhopper.routing.ev.TrackType;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.BaseGraph;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

/**
 * Covers how {@link OvertureReader} reacts to an encoding manager that lacks encoded values the
 * import needs.
 *
 * <p>The reader used to log a warning per missing key, keep a {@code null} encoded value, and then
 * fail with a {@link NullPointerException} on the first edge — long after the real cause. These
 * tests pin the replacement behaviour: report every missing key at once, before touching the graph.
 */
class OvertureReaderEncodedValueResolutionTest {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    @Test
    @DisplayName("A single missing encoded value fails before any edge is created, and names the key")
    void missingEncodedValueFailsFastAndNamesIt() throws IOException {
        // Everything the reader needs except "surface".
        EncodingManager em = new EncodingManager.Builder()
                .add(new SimpleBooleanEncodedValue("car_access", true))
                .add(new SimpleBooleanEncodedValue("bike_access", true))
                .add(new SimpleBooleanEncodedValue("foot_access", true))
                .add(new DecimalEncodedValueImpl("car_average_speed", 7, 2, true))
                .add(new DecimalEncodedValueImpl("bike_average_speed", 4, 2, true))
                .add(new DecimalEncodedValueImpl("foot_average_speed", 4, 1, true))
                .add(new EnumEncodedValue<>("road_class", RoadClass.class))
                .add(new SimpleBooleanEncodedValue("road_class_link", true))
                .add(new EnumEncodedValue<>("smoothness", Smoothness.class))
                .add(new EnumEncodedValue<>("track_type", TrackType.class))
                .add(new EnumEncodedValue<>("road_environment", RoadEnvironment.class))
                .add(new EnumEncodedValue<>("hazmat", Hazmat.class))
                .build();
        BaseGraph graph = new BaseGraph.Builder(em).create();

        IllegalStateException thrown =
                assertThrows(IllegalStateException.class, readerFor(graph, em)::readGraph);

        assertTrue(
                thrown.getMessage().contains("surface"),
                "should name the missing key: " + thrown.getMessage());
        assertTrue(
                thrown.getMessage().contains("graph.encoded_values"),
                "should point at the config key to fix: " + thrown.getMessage());
        assertEquals(0, graph.getEdges(), "must fail before writing any edge");
    }

    @Test
    @DisplayName("Several missing encoded values are all reported in one failure")
    void allMissingEncodedValuesAreReportedTogether() throws IOException {
        // Missing surface, smoothness and track_type at once.
        EncodingManager em = new EncodingManager.Builder()
                .add(new SimpleBooleanEncodedValue("car_access", true))
                .add(new SimpleBooleanEncodedValue("bike_access", true))
                .add(new SimpleBooleanEncodedValue("foot_access", true))
                .add(new DecimalEncodedValueImpl("car_average_speed", 7, 2, true))
                .add(new DecimalEncodedValueImpl("bike_average_speed", 4, 2, true))
                .add(new DecimalEncodedValueImpl("foot_average_speed", 4, 1, true))
                .add(new EnumEncodedValue<>("road_class", RoadClass.class))
                .add(new SimpleBooleanEncodedValue("road_class_link", true))
                .add(new EnumEncodedValue<>("road_environment", RoadEnvironment.class))
                .add(new EnumEncodedValue<>("hazmat", Hazmat.class))
                .build();
        BaseGraph graph = new BaseGraph.Builder(em).create();

        IllegalStateException thrown =
                assertThrows(IllegalStateException.class, readerFor(graph, em)::readGraph);

        String message = thrown.getMessage();
        // One run must surface every problem, not just the first, so a fix needs one iteration.
        assertTrue(message.contains("surface"), message);
        assertTrue(message.contains("smoothness"), message);
        assertTrue(message.contains("track_type"), message);
    }

    @Test
    @DisplayName("A fully configured encoding manager imports without complaint")
    void completeEncodingManagerSucceeds() throws IOException {
        EncodingManager em = OvertureTestFixtures.minimalEncodingManager();
        BaseGraph graph = new BaseGraph.Builder(em).create();

        readerFor(graph, em).readGraph();

        assertEquals(1, graph.getEdges(), "the single fixture segment should produce one edge");
    }

    /** A reader whose parse step is stubbed to yield one straight, unremarkable segment. */
    private static OvertureReader readerFor(BaseGraph graph, EncodingManager em) throws IOException {
        OvertureReader reader = spy(new OvertureReader(graph));
        reader.setEncodedValueLookup(em);
        doReturn(List.of(plainSegment())).when(reader).parseData();
        return reader;
    }

    /**
     * A minimal two-point segment with empty property lists — enough to reach the parser calls
     * without depending on any particular attribute being present.
     */
    private static OvertureRoadSegment plainSegment() {
        LineString line = GEOMETRY_FACTORY.createLineString(new Coordinate[] {
            new Coordinate(30.5234, 50.4501), new Coordinate(30.5250, 50.4510),
        });
        OvertureRoadProperties properties = new OvertureRoadProperties(
                List.of(), // connectors
                List.of(), // routes
                null, // roadClass
                List.of(), // destinations
                List.of(), // prohibitedTransitions
                List.of(), // surfaces
                List.of(), // flags
                List.of(), // speedLimits
                List.of(), // widthRules
                null, // subclass
                List.of(), // subclassRules
                List.of(), // accessRestrictions
                0, // level
                List.of(), // levelRules
                null, // theme
                null, // type
                0, // version
                List.of(), // sources
                null // names
                );
        return new OvertureRoadSegment("test-segment", line, properties);
    }
}
