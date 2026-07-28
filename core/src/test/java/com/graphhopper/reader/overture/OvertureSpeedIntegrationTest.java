package com.graphhopper.reader.overture;

import static org.junit.jupiter.api.Assertions.*;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.json.Statement;
import com.graphhopper.routing.ev.BikeNetwork;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.FerrySpeed;
import com.graphhopper.routing.ev.Hazmat;
import com.graphhopper.routing.ev.MaxSpeed;
import com.graphhopper.routing.ev.RoadClass;
import com.graphhopper.routing.ev.RoadClassLink;
import com.graphhopper.routing.ev.RoadEnvironment;
import com.graphhopper.routing.ev.Roundabout;
import com.graphhopper.routing.ev.Smoothness;
import com.graphhopper.routing.ev.Surface;
import com.graphhopper.routing.ev.TrackType;
import com.graphhopper.routing.ev.VehicleAccess;
import com.graphhopper.routing.ev.VehicleSpeed;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.CustomModel;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Helper;
import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class OvertureSpeedIntegrationTest {
    private static final String GH_LOCATION = "target/overture-speed-test-gh";
    private static final String TEST_DATA =
            "src/test/resources/com/graphhopper/reader/overture/parser/speed_limits_test.geojson";
    private static final double SPEED_TOLERANCE = 1.0;

    private GraphHopper hopper;
    private DecimalEncodedValue carSpeedEnc;
    private DecimalEncodedValue bikeSpeedEnc;
    private DecimalEncodedValue footSpeedEnc;

    @BeforeEach
    public void setup() {
        Helper.removeDir(new File(GH_LOCATION));
        hopper = new GraphHopper();
        configureHopper(hopper);
        hopper.importOrLoad();
        carSpeedEnc = hopper.getEncodingManager().getDecimalEncodedValue("car_average_speed");
        bikeSpeedEnc = hopper.getEncodingManager().getDecimalEncodedValue("bike_average_speed");
        footSpeedEnc = hopper.getEncodingManager().getDecimalEncodedValue("foot_average_speed");
    }

    @AfterEach
    public void tearDown() {
        if (hopper != null) hopper.close();
        Helper.removeDir(new File(GH_LOCATION));
    }

    private void configureHopper(GraphHopper gh) {
        gh.setFileBacked(false)
                .setDataFile(TEST_DATA)
                .setGraphHopperLocation(GH_LOCATION)
                .setMinNetworkSize(0);

        String encodedValues = Stream.of(
                        VehicleAccess.key("car"),
                        VehicleSpeed.key("car"),
                        VehicleAccess.key("bike"),
                        VehicleSpeed.key("bike"),
                        VehicleAccess.key("foot"),
                        VehicleSpeed.key("foot"),
                        RoadClass.KEY,
                        RoadEnvironment.KEY,
                        MaxSpeed.KEY,
                        Surface.KEY,
                        Smoothness.KEY,
                        TrackType.KEY,
                        Roundabout.KEY,
                        RoadClassLink.KEY,
                        FerrySpeed.KEY,
                        Hazmat.KEY,
                        BikeNetwork.KEY)
                .collect(Collectors.joining(","));
        gh.setEncodedValuesString(encodedValues);

        CustomModel customModel = new CustomModel();
        customModel.addToSpeed(Statement.If("true", Statement.Op.LIMIT, "car_average_speed"));
        customModel.setDistanceInfluence(0.0);
        gh.setProfiles(new Profile("car").setCustomModel(customModel));

        gh.setDataReaderInitializer(context -> new OvertureReader(context.getBaseGraph())
                .setEncodedValueLookup(context.getEncodingManager())
                .setFile(context.getSourceFile()));
    }

    @Test
    @DisplayName("Integration: speed limits and access")
    public void integrationSpeedLimitsAndAccess() {
        // Explicit speed limit
        double speed1 = getCarSpeedAt(50.450, 30.525);
        assertSpeedIsValid(speed1, 60.0, 54.0);

        // MPH to KMH conversion
        double speed2 = getCarSpeedAt(50.460, 30.535);
        assertSpeedIsValid(speed2, 48.28, 43.45);

        // Conditional speed priority
        double speed3 = getCarSpeedAt(50.470, 30.555);
        assertTrue(
                Math.abs(speed3 - 50.0) < SPEED_TOLERANCE || Math.abs(speed3 - 46.0) < SPEED_TOLERANCE,
                "Speed " + speed3 + " was not near 50 or 46");

        // Fallback for invalid/garbage data
        double speed4 = getCarSpeedAt(50.480, 30.575);
        assertTrue(speed4 > 0 && speed4 < 150, "Speed fallback logic failed: " + speed4);

        // Bidirectional speed limits
        EdgeIteratorState edge1 = getEdgeByCoords(50.450, 30.525);
        double fwdSpeed = edge1.get(carSpeedEnc);
        double bwdSpeed = edge1.getReverse(carSpeedEnc);
        assertEquals(
                fwdSpeed,
                bwdSpeed,
                SPEED_TOLERANCE,
                "Speed should be equal in both directions for this segment");

        // Bike speed limit
        double bikeSpeed = edge1.get(bikeSpeedEnc);
        assertEquals(
                18.0, bikeSpeed, 0.5, "Bike speed for primary road should be 18 km/h, got: " + bikeSpeed);

        // Foot speed limit
        EdgeIteratorState edge2 = getEdgeByCoords(50.460, 30.535);
        double footSpeed = edge2.get(footSpeedEnc);
        assertTrue(
                footSpeed > 4 && footSpeed < 7, "Foot speed should be around 5 km/h, got: " + footSpeed);

        // Bike fallback speed for garbage data
        EdgeIteratorState edge3 = getEdgeByCoords(50.480, 30.575);
        double bikeFallbackSpeed = edge3.get(bikeSpeedEnc);
        assertTrue(
                bikeFallbackSpeed > 0 && bikeFallbackSpeed < 50,
                "Bike fallback speed should be reasonable, got: " + bikeFallbackSpeed);

        // Fallback to road class default when speed limit missing
        double roadClassDefaultSpeed = edge3.get(carSpeedEnc);
        assertTrue(
                roadClassDefaultSpeed > 0 && roadClassDefaultSpeed < 150,
                "Fallback speed should be reasonable, got: " + roadClassDefaultSpeed);
    }

    @Test
    @DisplayName("Integration: routing and travel time")
    public void integrationRoutingAndTravelTime() {
        LocationIndex index = hopper.getLocationIndex();
        Snap from = index.findClosest(50.450, 30.521, EdgeFilter.ALL_EDGES);
        Snap to = index.findClosest(50.450, 30.529, EdgeFilter.ALL_EDGES);

        assertTrue(from.isValid() && to.isValid(), "Route points not found in index");

        GHResponse res = hopper.route(new GHRequest()
                .addPoint(from.getSnappedPoint())
                .addPoint(to.getSnappedPoint())
                .setProfile("car"));

        assertFalse(res.hasErrors(), "Routing errors: " + res.getErrors());
        assertTrue(res.getBest().getTime() > 0, "Route time should be positive");
    }

    @Test
    @DisplayName("Integration: bike and foot speeds")
    public void integrationBikeAndFootSpeeds() {
        EdgeIteratorState edgeBike = getEdgeByCoords(50.450, 30.525);
        double bikeSpeed = edgeBike.get(bikeSpeedEnc);
        assertEquals(
                18.0, bikeSpeed, 0.5, "Bike speed for primary road should be 18 km/h, got: " + bikeSpeed);

        EdgeIteratorState edgeFoot = getEdgeByCoords(50.460, 30.535);
        double footSpeed = edgeFoot.get(footSpeedEnc);
        assertTrue(
                footSpeed > 4 && footSpeed < 7, "Foot speed should be around 5 km/h, got: " + footSpeed);

        EdgeIteratorState edgeBikeFallback = getEdgeByCoords(50.480, 30.575);
        double bikeFallbackSpeed = edgeBikeFallback.get(bikeSpeedEnc);
        assertTrue(
                bikeFallbackSpeed > 0 && bikeFallbackSpeed < 50,
                "Bike fallback speed should be reasonable, got: " + bikeFallbackSpeed);
    }

    private double getCarSpeedAt(double lat, double lon) {
        return getEdgeByCoords(lat, lon).get(carSpeedEnc);
    }

    private void assertSpeedIsValid(double actual, double expected, double penaltyExpected) {
        assertTrue(
                Math.abs(actual - expected) < SPEED_TOLERANCE
                        || Math.abs(actual - penaltyExpected) < SPEED_TOLERANCE,
                "Expected speed " + expected + " or " + penaltyExpected + ", but got " + actual);
    }

    private EdgeIteratorState getEdgeByCoords(double lat, double lon) {
        Snap snap = hopper.getLocationIndex().findClosest(lat, lon, EdgeFilter.ALL_EDGES);
        if (!snap.isValid()) {
            throw new IllegalStateException("Coordinate " + lat + "," + lon + " not found.");
        }
        return snap.getClosestEdge();
    }
}
