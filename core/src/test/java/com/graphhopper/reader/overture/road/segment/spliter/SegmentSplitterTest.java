package com.graphhopper.reader.overture.road.segment.spliter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.graphhopper.reader.overture.road.segment.OvertureRoadProperties;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import java.util.List;
import java.util.TreeSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class SegmentSplitterTest {
    @Test
    @DisplayName(
            "Should return original segment in immutable list when only two boundary points exist")
    void splitWithMinimalPoints() {
        OvertureRoadSegment segment = mock(OvertureRoadSegment.class);
        OvertureRoadProperties props = mock(OvertureRoadProperties.class);
        when(segment.getProperties()).thenReturn(props);

        TreeSet<Double> points = new TreeSet<>(List.of(0.0, 1.0));

        try (MockedStatic<SplitPointCollector> collectorMock = mockStatic(SplitPointCollector.class)) {
            collectorMock.when(() -> SplitPointCollector.collectSplitPoints(props)).thenReturn(points);

            List<OvertureRoadSegment> result = SegmentSplitter.split(segment);

            assertEquals(1, result.size());
            assertSame(segment, result.getFirst());
            assertThrows(
                    UnsupportedOperationException.class, () -> result.add(mock(OvertureRoadSegment.class)));
        }
    }

    @Test
    @DisplayName("Should create three subsegments when four unique split points are provided")
    void splitWithMultiplePoints() {
        OvertureRoadSegment segment = mock(OvertureRoadSegment.class);
        OvertureRoadProperties props = mock(OvertureRoadProperties.class);
        when(segment.getProperties()).thenReturn(props);

        TreeSet<Double> points = new TreeSet<>(List.of(0.0, 0.3, 0.6, 1.0));

        try (MockedStatic<SplitPointCollector> collectorMock = mockStatic(SplitPointCollector.class)) {
            collectorMock.when(() -> SplitPointCollector.collectSplitPoints(props)).thenReturn(points);

            List<OvertureRoadSegment> result = SegmentSplitter.split(segment);

            assertEquals(3, result.size());
            assertNotNull(result.get(0));
            assertNotNull(result.get(1));
            assertNotNull(result.get(2));
        }
    }

    @Test
    @DisplayName("Should return empty list when collector returns fewer than two points")
    void splitWithInsufficientPoints() {
        OvertureRoadSegment segment = mock(OvertureRoadSegment.class);
        OvertureRoadProperties props = mock(OvertureRoadProperties.class);
        when(segment.getProperties()).thenReturn(props);

        TreeSet<Double> points = new TreeSet<>(List.of(0.0));

        try (MockedStatic<SplitPointCollector> collectorMock = mockStatic(SplitPointCollector.class)) {
            collectorMock.when(() -> SplitPointCollector.collectSplitPoints(props)).thenReturn(points);

            List<OvertureRoadSegment> result = SegmentSplitter.split(segment);

            assertTrue(result.isEmpty());
        }
    }
}
