package com.graphhopper.reader.overture;

import static org.junit.jupiter.api.Assertions.*;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.VehicleAccess;
import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.util.GHUtility;
import com.graphhopper.util.Helper;
import java.io.File;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * End-to-end proof that a bus profile works on an Overture import.
 *
 * <p>Worth an integration test rather than unit tests alone, because every part of bus support can pass
 * in isolation and still produce a graph nothing can route on:
 *
 * <ul>
 *   <li>{@code bus_overture.json} deliberately omits the dimension clause that opens the upstream
 *       {@code bus.json}. Overture has no weight or height data, so {@code max_weight} would sit at its
 *       storage default of 0, {@code max_weight < 5} would be true on every edge, and {@code
 *       multiply_by: 0} would make the whole graph unroutable while every unit test still passed. Only
 *       an actual route request catches that.
 *   <li>{@code bus_access} is filled by an Overture parser but its encoded value is created by
 *       upstream's {@link com.graphhopper.routing.ev.BusAccess}, and the two only meet when a real
 *       import runs through {@link OvertureSupport#configure}.
 * </ul>
 *
 * <p>The route is the same Lviv pair {@code GraphConstructionTest} uses for its car assertion, so a
 * failure here means something about bus specifically, not about the fixture.
 */
class OvertureBusProfileIntegrationTest {

    private static final File LVIV = Paths.get(
                    "src/test/resources",
                    "com/graphhopper/reader/overture/parser/correctGeoJson_CenterOfLviv.geojson")
            .toFile();

    private static final double FROM_LAT = 49.82915104534047;
    private static final double FROM_LON = 24.010322973268586;
    private static final double TO_LAT = 49.83976312814286;
    private static final double TO_LON = 24.047616211550206;

    private GraphHopper hopper;

    @BeforeEach
    void setup() {
        String graphLocation = Paths.get("target", "gh-overture-bus-test").toString();
        Helper.removeDir(new File(graphLocation));

        // Loads the shipped bus_overture.json off the classpath, comments and all, so this test
        // exercises the real file rather than a copy of its rules.
        Profile bus =
                new Profile("bus").setCustomModel(GHUtility.loadCustomModelFromJar("bus_overture.json"));

        hopper = new GraphHopper();
        hopper.setEncodedValuesString(OvertureTestFixtures.CONFIG_ENCODED_VALUES + ", bus_access");
        hopper.setDataFile(LVIV.getAbsolutePath());
        hopper.setGraphHopperLocation(graphLocation);
        hopper.setProfiles(bus);
        // The production wiring: this is what installs the Overture import registry, without which
        // bus_access cannot be created at all.
        hopper.setDataReaderInitializer(OvertureSupport.configure(hopper));
        hopper.importOrLoad();
    }

    @AfterEach
    void tearDown() {
        if (hopper != null) hopper.close();
    }

    @Test
    @DisplayName("bus_access is created and written on real Overture data")
    void busAccessIsFilled() {
        assertTrue(
                hopper.getEncodingManager().hasEncodedValue(VehicleAccess.key("bus")),
                "bus_access must be created when graph.encoded_values asks for it");

        BooleanEncodedValue busAccess =
                hopper.getEncodingManager().getBooleanEncodedValue(VehicleAccess.key("bus"));

        int accessible = 0;
        AllEdgesIterator edges = hopper.getBaseGraph().getAllEdges();
        while (edges.next()) {
            if (edges.get(busAccess)) accessible++;
        }

        assertTrue(
                accessible > 0,
                "no edge is open to buses, so OvertureBusAccessParser did not run or wrote only false");
    }

    @Test
    @DisplayName("A bus profile finds a route, so the custom model does not zero every edge")
    void busProfileRoutes() {
        GHResponse response =
                hopper.route(new GHRequest(FROM_LAT, FROM_LON, TO_LAT, TO_LON).setProfile("bus"));

        assertFalse(response.hasErrors(), () -> "bus routing failed: " + response.getErrors());
        assertTrue(response.getBest().getDistance() > 1000, "suspiciously short bus route");
    }
}
