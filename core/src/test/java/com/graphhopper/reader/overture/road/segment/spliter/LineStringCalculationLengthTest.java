package com.graphhopper.reader.overture.road.segment.spliter;

import org.junit.jupiter.api.DisplayName;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static com.graphhopper.reader.overture.road.segment.spliter.SegmentSplitterUtils.calculateLength;

public class LineStringCalculationLengthTest {
    private static final double DELTA = 0.00001;
    final Coordinate COORDINATE_OF_LVIV = new Coordinate(13.377899998432648, 52.51629086537031);
    final Coordinate COORDINATE_OF_BERLIN = new Coordinate(24.026531540754164, 49.84355344050558);

    @Test
    @DisplayName("Return zero for identical coordinates.")
    public void identicalCoordinatesTest() throws IOException {
        LineString lineString = new LineString(new CoordinateArraySequence(
                new Coordinate[] {COORDINATE_OF_LVIV, COORDINATE_OF_LVIV}),
                new GeometryFactory());

        double length = calculateLength(lineString);
        assertEquals(0, length);
    }

    @Test
    @DisplayName("Distance between two coordinates is calculating within the margin of error.")
    public void twoRealCoordinateTest() {
        LineString lineString = new LineString(new CoordinateArraySequence(
                new Coordinate[] {COORDINATE_OF_LVIV, COORDINATE_OF_BERLIN}),
                new GeometryFactory());

        double length = calculateLength(lineString);
        assertEquals(798601.81, length, Math.max(0.01, length * DELTA));
    }

    @Test
    @DisplayName("Distance set of coordinates is calculating within the margin of error for ferry trip.")
    public void ferryRouteFromNewYorkToBrighton() throws IOException {
        LineString ferryRoute = readAndParse("com/graphhopper/reader/overture/road/segment/splitter/ferryRouteFromNewYorkToBrighton_5838km.txt");

        double length = calculateLength(ferryRoute);
        assertEquals(5837962.60, length, Math.max(0.01, length * DELTA));
    }

    @Test
    @DisplayName("Distance set of coordinates is calculating within the margin of error for airplane trip.")
    public void airplaneTripRoute() throws IOException {
        LineString airplaneTour = readAndParse("com/graphhopper/reader/overture/road/segment/splitter/airplaneTour_3000km.txt");

        double length = calculateLength(airplaneTour);
        assertEquals(3000330.14, length, Math.max(0.01, length * DELTA));
    }

    private LineString readAndParse(String filePath) throws IOException {
        File file = Paths.get("src/test/resources/",filePath).toFile();
        Scanner scanner = new Scanner(file);

        ArrayList<Coordinate> coordinates = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] parts = line.split(", ");
            double lon = Double.parseDouble(parts[0]);
            double lat = Double.parseDouble(parts[1]);
            coordinates.add(new Coordinate(lon, lat, 0));
        }
        return new LineString(new CoordinateArraySequence(
                coordinates.toArray(new Coordinate[0])),
                new GeometryFactory());
    }

}
