package com.graphhopper.reader.overture.road.segment.spliter;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

public class SegmentSplitterUtilsTest {
    private final GeometryFactory gf = new GeometryFactory();

    @Test
    @DisplayName("Empty LineString should return empty Optional")
    void testEmptyLineString() {
        LineString line = gf.createLineString(new Coordinate[] {});

        Optional<Coordinate> result = SegmentSplitterUtils.findPointAtDistance(line, 1000);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Distance 0 meters should return first Coordinate in LineString")
    void testDistanceZero() {
        LineString line =
                gf.createLineString(new Coordinate[]
                        {new Coordinate(0, 0), new Coordinate(0.0001, 0)}
                );

        Optional<Coordinate> result = SegmentSplitterUtils.findPointAtDistance(line, 0.0);

        assertTrue(result.isPresent());
        assertEquals(0, result.get().x, 1e-9);
        assertEquals(0, result.get().y, 1e-9);
    }

    @Test
    @DisplayName("Distance equal to total length should return last Coordinate")
    void testDistanceTotalLength() {
        LineString line =
                gf.createLineString(new Coordinate[]
                        {new Coordinate(0, 0), new Coordinate(0.0001, 0)}
                );

        double totalLength = SegmentSplitterUtils.getTotalLength(line.getCoordinates());

        Optional<Coordinate> result = SegmentSplitterUtils.findPointAtDistance(line, totalLength);

        assertTrue(result.isPresent());
        assertEquals(0.0001, result.get().x, 1e-9);
        assertEquals(0, result.get().y, 1e-9);
    }

    @Test
    @DisplayName("Interpolate along single segment")
    void testMiddleDistance() {
        LineString line =
                gf.createLineString(new Coordinate[]
                        {new Coordinate(0, 0), new Coordinate(0.0001, 0)}
                );

        double totalLength = SegmentSplitterUtils.getTotalLength(line.getCoordinates());
        double halfDistance = totalLength / 2;

        Optional<Coordinate> result = SegmentSplitterUtils.findPointAtDistance(line, halfDistance);

        assertTrue(result.isPresent());
        assertEquals(0.00005, result.get().x, 1e-7);
        assertEquals(0, result.get().y, 1e-9);
    }

    @Test
    @DisplayName("Interpolate across multiple segments")
    void testMultipleSegments() {
        LineString line = gf.createLineString(new Coordinate[] {
                new Coordinate(0, 0),
                new Coordinate(0.0001, 0),
                new Coordinate(0.0001, 0.0001)
        });

        double targetDistance = 15;

        Optional<Coordinate> result = SegmentSplitterUtils.findPointAtDistance(line, targetDistance);
        assertTrue(result.isPresent());

        assertEquals(0.0001, result.get().x, 1e-7);
        assertTrue(result.get().y > 0.0);
    }

    @Test
    @DisplayName("Ignore zero-length segments")
    void testZeroLengthSegment() {
        LineString line = gf.createLineString(new Coordinate[] {
            new Coordinate(0, 0),
            new Coordinate(0, 0), // duplicate
            new Coordinate(0.0001, 0) // ~11 meters east
        });

        double totalLength = SegmentSplitterUtils.getTotalLength(line.getCoordinates());
        double halfDistance = totalLength / 2;

        Optional<Coordinate> result = SegmentSplitterUtils.findPointAtDistance(line, halfDistance);
        assertTrue(result.isPresent());

        assertEquals(0.00005, result.get().x, 1e-7);
        assertEquals(0, result.get().y, 1e-9);
    }
}
