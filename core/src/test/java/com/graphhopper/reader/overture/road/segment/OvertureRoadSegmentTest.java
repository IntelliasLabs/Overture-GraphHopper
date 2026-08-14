package com.graphhopper.reader.overture.road.segment;

import static org.junit.jupiter.api.Assertions.*;

import com.graphhopper.reader.overture.access.restriction.AccessType;
import com.graphhopper.reader.overture.access.restriction.OvertureAccessRestriction;
import com.graphhopper.reader.overture.access.restriction.PropertyScopeContainer;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelMode;
import com.graphhopper.reader.overture.common.speed.OvertureSpeed;
import com.graphhopper.reader.overture.common.speed.OvertureSpeedLimit;
import com.graphhopper.reader.overture.common.speed.SpeedUnit;
import com.graphhopper.reader.overture.names.OvertureNames;
import com.graphhopper.reader.overture.road.flags.OvertureRoadFlags;
import com.graphhopper.reader.overture.road.surface.OvertureRoadSurface;
import com.graphhopper.reader.overture.road.surface.RoadSurfaceType;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint3D;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

class OvertureRoadSegmentTest {

    private final GeometryFactory geometryFactory = new GeometryFactory();

    // Helper to create a simple JTS LineString
    private LineString createDummyLineString() {
        return geometryFactory.createLineString(
                new Coordinate[] {new Coordinate(0, 0), new Coordinate(1, 1)});
    }

    // Helper to create a real Properties object without mocking
    private OvertureRoadProperties createDummyProperties() {
        return new OvertureRoadProperties(
                null, // connectors
                null, // routes
                null, // roadClass
                null, // destinations
                null, // prohibitedTransitions
                null, // surfaces
                null, // flags
                null, // speedLimits
                null, // widthRules
                null, // subclass
                null, // subclassRules
                null, // accessRestrictions
                0, // level
                null, // levelRules
                null, // theme
                null, // type
                0, // version
                null, // sources
                null // names
                );
    }
    // Helper method specifically for testing Names
    private OvertureRoadProperties createPropertiesWithNames(OvertureNames names) {
        return new OvertureRoadProperties(
                null, null, null, null, null, null, null, null, null, null, null, null, 0, null, null, null,
                0, null, names);
    }

    // Helper to create Properties object with DENIED restriction for a given TravelMode
    private OvertureRoadProperties createPropertiesWithRestrictionsForTravelMode(
            TravelMode travelMode) {
        ArrayList<TravelMode> modes = new ArrayList<>();
        modes.add(travelMode);
        PropertyScopeContainer when = PropertyScopeContainer.ofMode(modes);

        List<OvertureAccessRestriction> restrictions = new ArrayList<>();
        restrictions.add(new OvertureAccessRestriction(AccessType.DENIED, when, null));

        return new OvertureRoadProperties(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                restrictions,
                0,
                null,
                null,
                null,
                0,
                null,
                null);
    }

    private OvertureRoadProperties createPropertiesWithSpeedLimit(OvertureSpeedLimit speedLimit) {
        List<OvertureSpeedLimit> speedLimits = new ArrayList<>();
        speedLimits.add(speedLimit);
        return new OvertureRoadProperties(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                speedLimits,
                null,
                null,
                null,
                null,
                0,
                null,
                null,
                null,
                0,
                null,
                null);
    }

    @Test
    void testConstructorAndGetters_ValidInput() {
        String validId = "gers:12345";
        LineString lineString = createDummyLineString();
        OvertureRoadProperties properties = createDummyProperties();

        OvertureRoadSegment segment = new OvertureRoadSegment(validId, lineString, properties);

        assertEquals(validId, segment.getId());
        assertSame(lineString, segment.getLineString());
        assertSame(properties, segment.getProperties());
    }

    @Test
    void testEqualsAndHashCode() {
        LineString lineString = createDummyLineString();
        OvertureRoadProperties props = createDummyProperties();

        // Create two separate instances with the exact same references
        OvertureRoadSegment s1 = new OvertureRoadSegment("gers:1", lineString, props);
        OvertureRoadSegment s2 = new OvertureRoadSegment("gers:1", lineString, props);

        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    void testNotEquals_DifferentId() {
        LineString lineString = createDummyLineString();
        OvertureRoadProperties props = createDummyProperties();

        OvertureRoadSegment s1 = new OvertureRoadSegment("gers:1", lineString, props);
        OvertureRoadSegment s2 = new OvertureRoadSegment("gers:2", lineString, props);

        assertNotEquals(s1, s2);
    }

    @Test
    void testNotEquals_DifferentLineString() {
        OvertureRoadProperties props = createDummyProperties();
        LineString l1 = createDummyLineString();

        // Create a different geometry instance with different coordinates
        LineString l2 = geometryFactory.createLineString(
                new Coordinate[] {new Coordinate(10, 10), new Coordinate(20, 20)});

        OvertureRoadSegment s1 = new OvertureRoadSegment("gers:1", l1, props);
        OvertureRoadSegment s2 = new OvertureRoadSegment("gers:1", l2, props);

        assertNotEquals(s1, s2);
    }

    @Test
    void testNotEquals_DifferentProperties() {
        LineString lineString = createDummyLineString();

        OvertureRoadProperties p1 = createDummyProperties();

        // Create a second property object that is different (e.g. different level)
        OvertureRoadProperties p2 = new OvertureRoadProperties(
                null, null, null, null, null, null, null, null, null, null, null, null,
                99, // Different level makes this object different
                null, null, null, 0, null, null);

        OvertureRoadSegment s1 = new OvertureRoadSegment("gers:1", lineString, p1);
        OvertureRoadSegment s2 = new OvertureRoadSegment("gers:1", lineString, p2);

        assertNotEquals(s1, s2);
    }

    @Test
    void testToString() {
        OvertureRoadSegment segment =
                new OvertureRoadSegment("gers:toString", createDummyLineString(), createDummyProperties());
        String s = segment.toString();

        assertTrue(s.contains("OvertureRoadSegment"));
        assertTrue(s.contains("id='gers:toString'"));
        assertTrue(s.contains("lineString="));
        assertTrue(s.contains("properties="));
    }

    @Test
    void testIsAccessible_NoRestrictions() {
        LineString lineString = createDummyLineString();
        OvertureRoadProperties properties = createDummyProperties();

        OvertureRoadSegment segment = new OvertureRoadSegment("gers:12345", lineString, properties);

        assertTrue(segment.isAccessible());
    }

    @Test
    void testIsAccessible_CarAccessDenied() {
        LineString lineString = createDummyLineString();
        OvertureRoadProperties properties =
                createPropertiesWithRestrictionsForTravelMode(TravelMode.CAR);

        OvertureRoadSegment segment = new OvertureRoadSegment("gers:12345", lineString, properties);

        assertFalse(segment.isAccessible());
    }

    @Test
    void testIsAccessible_MotorVehicleAccessDenied() {
        LineString lineString = createDummyLineString();
        OvertureRoadProperties properties =
                createPropertiesWithRestrictionsForTravelMode(TravelMode.MOTOR_VEHICLE);

        OvertureRoadSegment segment = new OvertureRoadSegment("gers:12345", lineString, properties);

        assertFalse(segment.isAccessible());
    }

    @Test
    void testIsAccessible_BicycleAccessPermitted() {
        LineString lineString = createDummyLineString();
        OvertureRoadProperties properties =
                createPropertiesWithRestrictionsForTravelMode(TravelMode.BICYCLE);

        OvertureRoadSegment segment = new OvertureRoadSegment("gers:12345", lineString, properties);

        assertTrue(segment.isAccessible());
    }

    @Test
    void testIsAccessible_AbandonedRoadAccessDenied() {
        LineString lineString = createDummyLineString();
        List<OvertureRoadFlags> flags = new ArrayList<>();
        flags.add(new OvertureRoadFlags(false, false, false, true, false, false, null));
        OvertureRoadProperties properties = new OvertureRoadProperties(
                null, null, null, null, null, null, flags, null, null, null, null, null, 0, null, null,
                null, 0, null, null);

        OvertureRoadSegment segment = new OvertureRoadSegment("gers:12345", lineString, properties);

        assertFalse(segment.isAccessible());
    }

    @Test
    void testIsAccessible_CarAccessDeniedInSecondRestriction() {
        LineString lineString = createDummyLineString();

        ArrayList<TravelMode> modesBus = new ArrayList<>();
        modesBus.add(TravelMode.BUS);

        ArrayList<TravelMode> modesCar = new ArrayList<>();
        modesCar.add(TravelMode.CAR);

        PropertyScopeContainer whenBus = PropertyScopeContainer.ofMode(modesBus);
        PropertyScopeContainer whenCar = PropertyScopeContainer.ofMode(modesCar);

        List<OvertureAccessRestriction> restrictions = new ArrayList<>();
        restrictions.add(new OvertureAccessRestriction(AccessType.DENIED, whenBus, null));
        restrictions.add(new OvertureAccessRestriction(AccessType.DENIED, whenCar, null));

        OvertureRoadProperties properties = new OvertureRoadProperties(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                restrictions,
                0,
                null,
                null,
                null,
                0,
                null,
                null);

        OvertureRoadSegment segment = new OvertureRoadSegment("gers:12345", lineString, properties);

        assertFalse(segment.isAccessible());
    }

    // Tests for calculateDistance()
    @Test
    void calculateDistance_nullLineString_returnsZero() {
        OvertureRoadSegment seg = new OvertureRoadSegment("id", null, null);
        assertEquals(0.0, seg.calculateDistance(), 1e-9);
    }

    @Test
    void calculateDistance_emptyLineString_returnsZero() {
        LineString ls = geometryFactory.createLineString(new Coordinate[0]);
        OvertureRoadSegment seg = new OvertureRoadSegment("id", ls, null);
        assertEquals(0.0, seg.calculateDistance(), 1e-9);
    }

    @Test
    void calculateDistance_twoPoints_computesSegmentDistance() {
        Coordinate a = new Coordinate(0.0, 0.0); // lon, lat
        Coordinate b = new Coordinate(0.01, 0.0);
        LineString ls = geometryFactory.createLineString(new Coordinate[] {a, b});
        OvertureRoadSegment seg = new OvertureRoadSegment("id", ls, null);

        double expected = DistanceCalcEarth.DIST_EARTH.calcDist(a.getY(), a.getX(), b.getY(), b.getX());
        assertEquals(expected, seg.calculateDistance(), 1e-9);
    }

    @Test
    void calculateDistance_multiplePoints_sumsSegmentDistances() {
        Coordinate a = new Coordinate(0.0, 0.0);
        Coordinate b = new Coordinate(0.01, 0.0);
        Coordinate c = new Coordinate(0.02, 0.0);
        // create a LineString with multiple points to sum segment distances
        LineString ls = geometryFactory.createLineString(new Coordinate[] {a, b, c});
        OvertureRoadSegment seg = new OvertureRoadSegment("id", ls, null);

        double d1 = DistanceCalcEarth.DIST_EARTH.calcDist(a.getY(), a.getX(), b.getY(), b.getX());
        double d2 = DistanceCalcEarth.DIST_EARTH.calcDist(b.getY(), b.getX(), c.getY(), c.getX());
        double expected = d1 + d2;
        assertEquals(expected, seg.calculateDistance(), 1e-9);
    }

    // Test for single-point: checks for the case of a single position
    @Test
    void calculateDistance_identicalPoints_returnsZero() {
        Coordinate a = new Coordinate(1.0, 1.0);
        // create a LineString with two identical coordinates
        LineString ls = geometryFactory.createLineString(new Coordinate[] {a, a});
        OvertureRoadSegment seg = new OvertureRoadSegment("id", ls, null);

        assertEquals(0.0, seg.calculateDistance(), 1e-9);
    }

    @Test
    void getPrimaryName_WithValidNames_ReturnsPrimaryName() {
        String expectedName = "Main Street";
        // Create OvertureNames. Using constructor: primary, common map (null), rules (null)
        OvertureNames names = new OvertureNames(expectedName, null, null);
        OvertureRoadProperties properties = createPropertiesWithNames(names);
        OvertureRoadSegment segment =
                new OvertureRoadSegment("id", createDummyLineString(), properties);

        String result = segment.getPrimaryName();
        assertEquals(expectedName, result);
    }

    @Test
    void getPrimaryName_WithNullNamesObject_ReturnsEmptyString() {
        // Properties object creates with null names by default in this helper
        OvertureRoadProperties properties = createPropertiesWithNames(null);
        OvertureRoadSegment segment =
                new OvertureRoadSegment("id", createDummyLineString(), properties);
        String result = segment.getPrimaryName();

        assertEquals("", result);
    }

    @Test
    void getPrimaryName_WithNullProperties_ReturnsEmptyString() {
        // Segment with null properties completely
        OvertureRoadSegment segment = new OvertureRoadSegment("id", createDummyLineString(), null);

        String result = segment.getPrimaryName();
        assertEquals("", result);
    }

    @Test
    void getPrimaryName_WithNullPrimaryInNames_ReturnsEmptyString() {
        // Create OvertureNames where 'primary' string is explicitly null
        OvertureNames names = new OvertureNames(null, null, null);
        OvertureRoadProperties properties = createPropertiesWithNames(names);
        OvertureRoadSegment segment =
                new OvertureRoadSegment("id", createDummyLineString(), properties);
        String result = segment.getPrimaryName();
        assertEquals("", result);
    }

    // Test for a single point: factory method doesn't allow creating lineString of the length of 1
    // substitute with several identical points
    @Test
    void getPoinList_single_point() {
        double lon = 1.0;
        double lat = 12.0;
        Coordinate[] coordArray = new Coordinate[] {new Coordinate(lon, lat), new Coordinate(lon, lat)};
        LineString ls = geometryFactory.createLineString(coordArray);
        OvertureRoadSegment seg = new OvertureRoadSegment("id", ls, null);

        PointList expected = new PointList();
        expected.add(lat, lon);
        expected.add(lat, lon);

        assertEquals(expected, seg.getPointList());
    }

    // Test for multiple points: regular case
    @Test
    void getPointList_multiple_points() {
        LineString ls = createDummyLineString();
        OvertureRoadSegment seg = new OvertureRoadSegment("id", ls, null);

        PointList expected = new PointList();
        expected.add(0, 0);
        expected.add(1, 1);

        assertEquals(expected, seg.getPointList());
    }

    // Test to verify order: does it really swap the order of lon and lat parameters
    @Test
    void getPointList_verify_order() {
        double lon = 1.0;
        double lat = 12.0;
        Coordinate[] coordArray = new Coordinate[] {new Coordinate(lon, lat), new Coordinate(lon, lat)};
        LineString ls = geometryFactory.createLineString(coordArray);
        OvertureRoadSegment seg = new OvertureRoadSegment("id", ls, null);

        GHPoint3D actual = seg.getPointList().get(0);

        assertEquals(lat, actual.lat);
        assertEquals(lon, actual.lon);
    }

    // Test null geometry: I guess we test if lineString is null? But also added so if lineString
    // is empty it still returns an empty PointList
    @Test
    void getPointList_null_geometry() {
        LineString ls = geometryFactory.createLineString(new Coordinate[0]);
        OvertureRoadSegment seg1 = new OvertureRoadSegment("id1", ls, null);
        OvertureRoadSegment seg2 = new OvertureRoadSegment("id2", null, null);

        PointList expected = new PointList();

        assertEquals(expected, seg1.getPointList());
        assertEquals(expected, seg2.getPointList());
    }

    @Test
    void testGetMaxSpeed_KmH() {
        final double maxSpeedKmH = 120.;
        OvertureSpeed speed = new OvertureSpeed(maxSpeedKmH, SpeedUnit.KM_H);
        OvertureSpeedLimit speedLimit = new OvertureSpeedLimit(speed, null, null, null, null);

        OvertureRoadProperties properties = createPropertiesWithSpeedLimit(speedLimit);

        OvertureRoadSegment segment =
                new OvertureRoadSegment("gers:12345", createDummyLineString(), properties);

        assertEquals(maxSpeedKmH, segment.getMaxSpeed());
    }

    @Test
    void testGetMaxSpeed_MpH() {
        final double maxSpeedMpH = 120.;
        OvertureSpeed speed = new OvertureSpeed(maxSpeedMpH, SpeedUnit.MPH);
        OvertureSpeedLimit speedLimit = new OvertureSpeedLimit(speed, null, null, null, null);

        OvertureRoadProperties properties = createPropertiesWithSpeedLimit(speedLimit);

        OvertureRoadSegment segment =
                new OvertureRoadSegment("gers:12345", createDummyLineString(), properties);

        assertEquals(maxSpeedMpH * 1.60934, segment.getMaxSpeed());
    }

    @Test
    void testGetMaxSpeed_ReturnsNullWhenNoMetadata() {
        OvertureRoadSegment segmentNoProps =
                new OvertureRoadSegment("id", createDummyLineString(), null);
        assertNull(segmentNoProps.getMaxSpeed());

        OvertureRoadProperties emptyProps = createPropertiesWithSpeedLimit(null);
        OvertureRoadSegment segmentEmpty =
                new OvertureRoadSegment("id", createDummyLineString(), emptyProps);
        assertNull(segmentEmpty.getMaxSpeed());
    }

    @Test
    void testGetRoadSurface_ReturnsMetadata() {
        OvertureRoadSurface expectedSurface = new OvertureRoadSurface(RoadSurfaceType.ASPHALT, null);
        List<OvertureRoadSurface> surfaces = Collections.singletonList(expectedSurface);

        OvertureRoadProperties properties = new OvertureRoadProperties(
                null, null, null, null, null, surfaces, null, null, null, null, null, null, 0, null, null,
                null, 0, null, null);

        OvertureRoadSegment segment =
                new OvertureRoadSegment("id", createDummyLineString(), properties);

        assertEquals(expectedSurface, segment.getRoadSurface());
    }
}
