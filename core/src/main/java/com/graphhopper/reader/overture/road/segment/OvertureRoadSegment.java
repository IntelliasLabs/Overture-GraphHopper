package com.graphhopper.reader.overture.road.segment;

import com.graphhopper.reader.overture.access.restriction.AccessType;
import com.graphhopper.reader.overture.access.restriction.OvertureAccessRestriction;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelMode;
import com.graphhopper.reader.overture.common.speed.OvertureSpeedLimit;
import com.graphhopper.reader.overture.names.Bcp47LanguageTag;
import com.graphhopper.reader.overture.names.OvertureNameRule;
import com.graphhopper.reader.overture.road.flags.OvertureRoadFlags;
import com.graphhopper.reader.overture.road.surface.OvertureRoadSurface;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.PointList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

/**
 * Represents a segment of the transportation network.
 * <p>
 * Segments are paths which can be traveled by people or things.
 * They are compatible with GeoJSON LineString features.
 * </p>
 */
public class OvertureRoadSegment {
    private final String id;
    private final LineString lineString;
    private final OvertureRoadProperties properties;

    /**
     * Constructs a new OvertureRoadSegment.
     *
     * @param id         The unique GERS ID of the segment.
     * @param lineString LineString.
     * @param properties The road-specific attributes and rules.
     */
    public OvertureRoadSegment(String id, LineString lineString, OvertureRoadProperties properties) {
        this.id = id;
        this.lineString = lineString;
        this.properties = properties;
    }

    /**
     * Gets the unique Global Entity Reference System (GERS) ID.
     *
     * @return the segment ID string.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the segment's geometry as a LineString.
     * <p>
     * This corresponds to a GeoJSON LineString.
     * </p>
     *
     * @return the {@link LineString} object.
     */
    public LineString getLineString() {
        return lineString;
    }

    /**
     * Gets the road segment subtype, indicating the type of transportation feature.
     *
     * @return the {@link OvertureSegmentSubtype} enum value.
     */
    public OvertureSegmentSubtype getSubtype() {
        if (properties == null || properties.getSubtype() == null) return OvertureSegmentSubtype.ROAD;
        return properties.getSubtype();
    }

    /**
     * Gets the road properties, including classification, access rules, and physical attributes.
     *
     * @return the {@link OvertureRoadProperties} object.
     */
    public OvertureRoadProperties getProperties() {
        return properties;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OvertureRoadSegment that)) return false;
        return Objects.equals(getId(), that.getId())
                && Objects.equals(getLineString(), that.getLineString())
                && Objects.equals(getProperties(), that.getProperties());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getLineString(), getProperties());
    }

    /**
     * Provides a string representation of the OvertureRoadSegment.
     *
     * @return a string detailing the segment's ID, geometry, and properties.
     */
    @Override
    public String toString() {
        return "OvertureRoadSegment{" + "id='"
                + id + '\'' + ", lineString="
                + lineString + ", properties="
                + properties + '}';
    }

    /**
     * Checks whether this road segment is accessible for cars.
     * <p>
     * The segment is considered accessible if it is not abandoned or under construction
     * and there is no access restriction that denies {@link TravelMode#CAR},
     * {@link TravelMode#MOTOR_VEHICLE}, or {@link TravelMode#VEHICLE}.
     * A denial without a {@code when} clause is treated as a global denial.
     * If no restrictions are present, the road is accessible by default.
     * </p>
     *
     * @return {@code true} if the road is accessible for cars, {@code false} otherwise
     */
    public boolean isAccessible() {
        if (properties == null) return true;
        if (properties.getFlags() != null
                && properties.getFlags().stream().anyMatch(OvertureRoadFlags::shouldSkip)
                || properties.getRoadClass() != null && !properties.getRoadClass().isCarAccessible()) {
            return false;
        }

        var restrictions = properties.getAccessRestrictions();
        if (restrictions != null) {
            for (OvertureAccessRestriction restriction : restrictions) {
                if (restriction.getAccessType() == AccessType.DENIED) {
                    if (!restriction.hasWhen()) return false;
                    var restrictedModes = restriction.getWhen().getMode();
                    if (containsCarRelatedMode(restrictedModes)) return false;
                }
            }
        }
        return true;
    }

    /**
     * Evaluates whether the provided list of travel modes contains any modes
     * that would restrict a typical passenger car.
     * @param restrictedModes the list of travel modes to check; may be {@code null}.
     * @return {@code true} if the list contains {@code VEHICLE}, {@code MOTOR_VEHICLE},
     * or {@code CAR}, {@code false} otherwise.
     */
    private boolean containsCarRelatedMode(List<TravelMode> restrictedModes) {
        return restrictedModes != null
                && restrictedModes.stream()
                        .anyMatch(mode -> mode == TravelMode.VEHICLE
                                || mode == TravelMode.MOTOR_VEHICLE
                                || mode == TravelMode.CAR);
    }

    /**
     * Calculates the total 2D geodesic distance of this segment in meters.
     * Uses {@link DistanceCalcEarth#DIST_EARTH} to compute distances between consecutive coordinates.
     *
     * @return total distance in meters, or 0.0 if there are fewer than 2 points
     */
    public double calculateDistance() {
        if (lineString == null) return 0.0;

        double totalDistance = 0.0;
        for (int i = 0; i < lineString.getNumPoints() - 1; i++) {
            Coordinate segmentStart = lineString.getCoordinateN(i);
            Coordinate segmentEnd = lineString.getCoordinateN(i + 1);
            totalDistance += DistanceCalcEarth.DIST_EARTH.calcDist(
                    segmentStart.getY(), segmentStart.getX(),
                    segmentEnd.getY(), segmentEnd.getX());
        }
        return totalDistance;
    }

    /**
     * Extracts the primary name of the road segment.
     * Safely navigates the properties structure.
     *
     * @return The primary name if available, otherwise an empty string.
     */
    public String getPrimaryName() {
        if (properties == null || properties.getNames() == null) {
            return "";
        }
        String primary = properties.getNames().getPrimary();
        return primary != null ? primary : "";
    }

    /**
     * Extracts the map of common names for the road segment.
     *
     * @return a map of Bcp47LanguageTag to name strings, or an empty map if none exist.
     */
    public Map<Bcp47LanguageTag, String> getCommonNames() {
        if (properties == null
                || properties.getNames() == null
                || properties.getNames().getCommon() == null) {
            return Map.of();
        }
        return properties.getNames().getCommon();
    }
    /**
     * Extracts the list of name rules for the road segment.
     *
     * @return a list of OvertureNameRule objects, or an empty list if none exist.
     */
    public List<OvertureNameRule> getNameRules() {
        if (properties == null
                || properties.getNames() == null
                || properties.getNames().getRules() == null) {
            return List.of();
        }
        return properties.getNames().getRules();
    }
    /**
     * Converts geometry array of (lon, lat) pairs
     * to PointList of (lat, lon) pairs
     *
     * @return PointList with (lat, lon) pairs
     */
    public PointList getPointList() {
        if (lineString == null || lineString.getNumPoints() == 0) return new PointList();

        int size = lineString.getNumPoints();
        PointList convertedList = new PointList(size, false);
        Coordinate currentPoint;
        for (int i = 0; i < size; i++) {
            currentPoint = lineString.getCoordinateN(i);
            convertedList.add(currentPoint.y, currentPoint.x);
        }
        return convertedList;
    }

    /**
     * Determines the maximum speed limit for this segment based on Overture properties.
     * @return the maximum speed limit in km/h , or {@code null} if no explicit
     * speed limit is defined in the metadata.
     */
    public Double getMaxSpeed() {
        if (properties == null) return null;
        var speedLimitList = properties.getSpeedLimits();
        if (speedLimitList == null || speedLimitList.isEmpty()) return null;
        OvertureSpeedLimit speedLimit = speedLimitList.getFirst();
        return (speedLimit == null) ? null : speedLimit.getMaxSpeedKmh();
    }

    /**
     * Returns the primary road surface description for this segment from Overture metadata.
     * @return the {@link OvertureRoadSurface} object, or {@code null} if no surface
     * metadata is available.
     */
    public OvertureRoadSurface getRoadSurface() {
        if (properties == null) return null;
        var surfaceList = properties.getSurfaces();
        if (surfaceList != null && !surfaceList.isEmpty()) {
            return surfaceList.getFirst();
        }
        return null;
    }
}
