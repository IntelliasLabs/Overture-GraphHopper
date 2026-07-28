package com.graphhopper.reader.overture;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.graphhopper.GHRequest;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.util.GHUtility;
import com.graphhopper.util.shapes.BBox;
import java.io.File;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class GraphConstructionTest {

    private static final File correctLittleSegmentGeoJson = Paths.get(
                    "src/test/resources/",
                    "com/graphhopper/reader/overture/parser/correctLittleSegment.geojson")
            .toFile();

    private static final String GH_LOCATION = "target/graphhopper-test-gh";

    private static final double DELTA = 0.08;

    @Test
    @DisplayName("Test graph construction: correct edge number")
    public void testGraphConstructionOnOvertureDataCorrectEdgeNumber() {
        assertEquals(
                5,
                createGraphHopper(correctLittleSegmentGeoJson)
                        .getBaseGraph()
                        .getAllEdges()
                        .length());
    }

    @Test
    @DisplayName("Test graph construction: correct node number")
    public void testGraphConstructionOnOvertureDataCorrectNodeNumber() {
        assertEquals(
                6, createGraphHopper(correctLittleSegmentGeoJson).getBaseGraph().getNodes());
    }

    @Test
    @DisplayName("Test graph construction: correct graph bounds")
    public void testGraphConstructionOnOvertureDataCorrectGraphBounds() {
        BaseGraph graph = createGraphHopper(correctLittleSegmentGeoJson).getBaseGraph();

        BBox bounds = graph.getBounds();
        assertEquals(30.3623593, bounds.minLon);
        assertEquals(50.356176, bounds.minLat);
        assertEquals(30.365696, bounds.maxLon);
        assertEquals(50.3613893, bounds.maxLat);
    }

    private static Stream<Arguments> testParametrization() {
        return Stream.of(
                Arguments.of(
                        Paths.get(
                                        "src/test/resources/",
                                        "com/graphhopper/reader/overture/parser/correctGeoJson_CenterOfLviv.geojson")
                                .toFile(),
                        49.82915104534047,
                        24.010322973268586,
                        49.83976312814286,
                        24.047616211550206,
                        "car",
                        4.3),
                Arguments.of(
                        Paths.get(
                                        "src/test/resources/",
                                        "com/graphhopper/reader/overture/parser/correctGeoJson_CenterOfKyiv.geojson")
                                .toFile(),
                        50.5087781,
                        30.5898204,
                        50.5281311,
                        30.5887254,
                        "foot",
                        2.7),
                Arguments.of(
                        Paths.get(
                                        "src/test/resources/",
                                        "com/graphhopper/reader/overture/parser/centerOfBerlin.geojson")
                                .toFile(),
                        52.524063,
                        13.3906422,
                        52.5168102,
                        13.4178287,
                        "bike",
                        2.8));
    }

    @ParameterizedTest
    @MethodSource("testParametrization")
    @DisplayName("Test graph construction: graph connectivity")
    public void testGraphConstructionOnOvertureDataConnectivity(
            File file,
            double fLat,
            double fLon,
            double tLat,
            double tLon,
            String profile,
            double distExp) {
        GraphHopper gh = createGraphHopper(file);
        GHRequest hRequest = new GHRequest(fLat, fLon, tLat, tLon).setProfile(profile);

        assertEquals(distExp, gh.route(hRequest).getBest().getDistance() / 1000, DELTA);
    }

    private GraphHopper createGraphHopper(File file) {
        GraphHopper hopper = new GraphHopper();
        hopper.setFileBacked(false);
        hopper.setEncodedValuesString("car_access, car_average_speed, foot_access, foot_average_speed, "
                + "road_access, hike_rating, foot_priority, country, road_class, "
                + "foot_road_access, mtb_rating, bike_access, bike_average_speed, "
                + "bike_network, bike_priority, bike_road_access, surface, hazmat, track_type, "
                + "road_environment, ferry_speed, max_speed");

        hopper.setDataFile(file.getAbsolutePath());
        hopper.setGraphHopperLocation(GH_LOCATION);

        hopper.setDataReaderInitializer(context -> new OvertureReader(context.getBaseGraph())
                .setEncodedValueLookup(context.getEncodingManager())
                .setFile(context.getSourceFile()));

        hopper.setProfiles(
                new Profile("car").setCustomModel(GHUtility.loadCustomModelFromJar("car.json")),
                new Profile("foot").setCustomModel(GHUtility.loadCustomModelFromJar("foot_overture.json")),
                new Profile("bike").setCustomModel(GHUtility.loadCustomModelFromJar("bike_overture.json")));

        hopper
                .getCHPreparationHandler()
                .setCHProfiles(new CHProfile("car"), new CHProfile("foot"), new CHProfile("bike"));

        hopper.importOrLoad();
        return hopper;
    }
}
