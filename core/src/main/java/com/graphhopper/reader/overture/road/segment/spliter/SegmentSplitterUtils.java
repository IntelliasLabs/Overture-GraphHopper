package com.graphhopper.reader.overture.road.segment.spliter;

import static com.graphhopper.util.DistanceCalcEarth.R;
import static java.lang.Math.atan2;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

import java.util.Optional;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

public class SegmentSplitterUtils {

    private SegmentSplitterUtils() {}

    /**
     * Calculate length for passed lineString parameter using haversine formula
     *
     * @return calculated total length for passed lineString parameter, for duplicate coordinates in the sequence, return the length between them as zero.
     */
    public static double calculateLength(LineString lineString) {
        return lineString == null ? 0.0 : getTotalLength(lineString.getCoordinates());
    }

    /**
     * Finds a Coordinate on a LineString at a specified distance from
     * its start point. This will be used to determine the precise
     * geographic coordinates of the split points.
     *
     * @param lineString LineString whose coordinates define the segment
     * @param distance distance (in meters) from the first Coordinate to the target one
     */
    public static Optional<Coordinate> findPointAtDistance(LineString lineString, double distance) {
        Coordinate[] points = lineString.getCoordinates();

        if (points.length < 2) return Optional.empty();
        if (distance <= 0.0) return Optional.of(points[0]);

        double totalLength = getTotalLength(points);
        if (distance >= totalLength) return Optional.of(points[points.length - 1]);

        return getTargetCoordinate(points, distance);
    }

    protected static double getTotalLength(Coordinate[] points) {
        double totalLength = 0.0;
        for (int i = 0; i < points.length - 1; i++)
            totalLength += haversineDistance(points[i], points[i + 1]);

        return totalLength;
    }

    private static Optional<Coordinate> getTargetCoordinate(
            Coordinate[] points, double targetLength) {
        double currentLength = 0.0;

        for (int i = 0; i < points.length - 1; i++) {
            Coordinate p1 = points[i];
            Coordinate p2 = points[i + 1];
            double subsegmentLength = haversineDistance(points[i], points[i + 1]);

            // if there are duplicates
            if (subsegmentLength == 0.0) {
                continue;
            }

            // target point is on this sub-segment
            if (currentLength + subsegmentLength >= targetLength) {
                double remaining = targetLength - currentLength;
                double ratio = remaining / subsegmentLength;

                return Optional.of(interpolateGeodesic(p1, p2, ratio));
            }
            currentLength += subsegmentLength;
        }
        return Optional.empty();
    }

    static double haversineDistance(Coordinate c1, Coordinate c2) {
        if (c1 == null || c2 == null) return 0.0;

        double lon1 = toRadians(c1.x);
        double lon2 = toRadians(c2.x);
        double lat1 = toRadians(c1.y);
        double lat2 = toRadians(c2.y);

        double latDelta = lat2 - lat1;
        double lonDelta = lon2 - lon1;

        double a = Math.sin(latDelta / 2) * Math.sin(latDelta / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(lonDelta / 2) * Math.sin(lonDelta / 2);
        double c = 2 * atan2(sqrt(a), sqrt(1 - a));

        return R * c;
    }

    // Just linear interpolation won't work with coordinates on sphere, need to use spherical one
    private static Coordinate interpolateGeodesic(Coordinate c1, Coordinate c2, double fraction) {
        double lat1 = Math.toRadians(c1.y);
        double lon1 = Math.toRadians(c1.x);
        double lat2 = Math.toRadians(c2.y);
        double lon2 = Math.toRadians(c2.x);

        double d = haversineDistance(c1, c2) / R; // angular distance in radians
        if (d == 0) return new Coordinate(c1);

        // needed some additional variables or else it looks too convoluted
        double a = Math.sin((1 - fraction) * d) / Math.sin(d);
        double b = Math.sin(fraction * d) / Math.sin(d);

        double x = a * Math.cos(lat1) * Math.cos(lon1) + b * Math.cos(lat2) * Math.cos(lon2);
        double y = a * Math.cos(lat1) * Math.sin(lon1) + b * Math.cos(lat2) * Math.sin(lon2);
        double z = a * Math.sin(lat1) + b * Math.sin(lat2);

        double lat = Math.atan2(z, Math.sqrt(x * x + y * y));
        double lon = Math.atan2(y, x);

        return new Coordinate(Math.toDegrees(lon), Math.toDegrees(lat));
    }
}
