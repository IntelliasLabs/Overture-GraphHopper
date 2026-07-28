package com.graphhopper.reader.overture.parsers;

import static org.mockito.Mockito.*;

import com.graphhopper.reader.overture.road.segment.OvertureRoadClass;
import com.graphhopper.reader.overture.road.segment.OvertureRoadProperties;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.util.EdgeIteratorState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class OvertureFootAverageSpeedParserTest {
    private EdgeIteratorState edge;
    private OvertureRoadSegment segment;
    private OvertureRoadProperties properties;
    private DecimalEncodedValue speedEnc;

    @BeforeEach
    void setup() {
        edge = mock(EdgeIteratorState.class);
        segment = mock(OvertureRoadSegment.class);
        properties = mock(OvertureRoadProperties.class);
        speedEnc = mock(DecimalEncodedValue.class);

        when(segment.getProperties()).thenReturn(properties);
    }

    @Test
    @DisplayName("Steps should result in 3.0 km/h")
    void testSteps_ReducedSpeed() {
        when(properties.getRoadClass()).thenReturn(OvertureRoadClass.STEPS);

        new OvertureFootAverageSpeedParser(speedEnc).handleSegment(edge, segment, null);

        verify(edge).set(speedEnc, 3.0);
    }

    @ParameterizedTest
    @EnumSource(
            value = OvertureRoadClass.class,
            names = {"RESIDENTIAL", "FOOTWAY", "PEDESTRIAN", "LIVING_STREET"})
    @DisplayName("Standard road classes should result in 5.0 km/h")
    void testStandardRoads_DefaultSpeed(OvertureRoadClass roadClass) {
        when(properties.getRoadClass()).thenReturn(roadClass);

        new OvertureFootAverageSpeedParser(speedEnc).handleSegment(edge, segment, null);

        verify(edge).set(speedEnc, 5.0);
    }

    @Test
    @DisplayName("Unknown or null road class should fallback to 5.0 km/h")
    void testUnknownRoadClass_DefaultSpeed() {
        when(properties.getRoadClass()).thenReturn(OvertureRoadClass.UNKNOWN);

        new OvertureFootAverageSpeedParser(speedEnc).handleSegment(edge, segment, null);

        verify(edge).set(speedEnc, 5.0);
    }
}
