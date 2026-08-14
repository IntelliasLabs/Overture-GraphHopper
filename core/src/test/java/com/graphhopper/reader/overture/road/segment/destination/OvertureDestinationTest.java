package com.graphhopper.reader.overture.road.segment.destination;

import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelHeading;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OvertureDestinationTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange
        OvertureDestinationLabel label = new OvertureDestinationLabel("San Jose", OvertureDestinationLabelType.STREET);
        List<OvertureDestinationLabel> labels = Collections.singletonList(label);

        List<OvertureDestinationSymbol> symbols = Collections.singletonList(OvertureDestinationSymbol.AIRPORT);

        String fromConnector = "conn_start";
        String toSegment = "seg_target";
        String toConnector = "conn_end";
        TravelHeading heading = TravelHeading.FORWARD;

        // Act
        OvertureDestination destination = new OvertureDestination(
                labels,
                symbols,
                fromConnector,
                toSegment,
                toConnector,
                null,
                heading
        );

        // Assert
        assertEquals(labels, destination.getLabels());
        assertEquals(symbols, destination.getSymbols());
        assertEquals(fromConnector, destination.getFromConnectorId());
        assertEquals(toSegment, destination.getToSegmentId());
        assertEquals(toConnector, destination.getToConnectorId());
        assertEquals(heading, destination.getFinalHeading());
        assertNull(destination.getWhen());
    }

    @Test
    void testEqualsAndHashCode() {
        OvertureDestination d1 = createTestDestination("Downtown", "c1");
        OvertureDestination d2 = createTestDestination("Downtown", "c1");
        OvertureDestination d3 = createTestDestination("Uptown", "c1");

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
        assertNotEquals(d1, d3);
        assertNotEquals(null, d1);
    }

    @Test
    void testToString() {
        OvertureDestination destination = createTestDestination("Airport", "c_exit");
        String result = destination.toString();

        assertTrue(result.contains("Airport"));
        assertTrue(result.contains("fromConnectorId='c_exit'"));
        assertTrue(result.contains("finalHeading=forward"));
    }

    /**
     * Helper to reduce boilerplate in tests.
     */
    private OvertureDestination createTestDestination(String val, String fromConn) {
        return new OvertureDestination(
                Collections.singletonList(new OvertureDestinationLabel(val, OvertureDestinationLabelType.STREET)),
                Collections.singletonList(OvertureDestinationSymbol.INFO),
                fromConn,
                "target_seg",
                "target_conn",
                null,
                TravelHeading.FORWARD
        );
    }
}
