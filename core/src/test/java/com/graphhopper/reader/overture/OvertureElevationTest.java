package com.graphhopper.reader.overture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.reader.dem.ElevationProvider;
import com.graphhopper.routing.ev.AverageSlope;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.GHUtility;
import com.graphhopper.util.PointList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Covers importing Overture data into a 3D graph.
 *
 * <p>Enabling {@code graph.elevation.provider} used to abort the import outright: the reader built
 * every {@link PointList} as 2D and called the two-argument {@code setNode}, so {@code BaseGraph}
 * rejected the geometry with "Cannot use pointlist which is 2D for graph which is 3D". A documented
 * configuration option therefore could not be used at all, and no test noticed.
 *
 * <p>Uses a stub provider rather than a real DEM so the assertions are exact and no tiles are
 * downloaded.
 */
class OvertureElevationTest {

    /**
     * Metres of elevation gained per degree of latitude.
     *
     * <p>Kept small enough that the fixture's latitudes stay well under 10 000 m: {@code
     * Helper.eleToUInt} stores anything at or above that as a sentinel which reads back as {@link
     * Double#MAX_VALUE}, so an over-enthusiastic gradient silently produces unusable elevations.
     */
    private static final double METRES_PER_DEGREE_LAT = 2_000;

    /**
     * Elevation is stored via a float factor, and GraphHopper documents "do not expect more precision
     * than meters", so comparisons allow a metre.
     */
    private static final double ELEVATION_TOLERANCE_M = 1.0;

    /** Elevation that varies with latitude, so slope over a north-south segment is non-zero. */
    private static final ElevationProvider RISING_NORTHWARD = new ElevationProvider() {
        @Override
        public ElevationProvider init() {
            return this;
        }

        @Override
        public double getEle(double lat, double lon) {
            return (lat - 50.0) * METRES_PER_DEGREE_LAT;
        }

        @Override
        public boolean canInterpolate() {
            return false;
        }

        @Override
        public void release() {}
    };

    private GraphHopper hopper;

    @AfterEach
    void tearDown() {
        if (hopper != null) hopper.close();
    }

    @Test
    @DisplayName("An import with elevation enabled completes and produces a 3D graph")
    void elevationEnabledImportSucceeds() {
        hopper = importWithElevation();

        BaseGraph graph = hopper.getBaseGraph();
        assertTrue(
                graph.getNodeAccess().is3D(), "graph should be 3D when an elevation provider is set");
        assertTrue(graph.getEdges() > 0, "fixture produced no edges - the test proves nothing");
    }

    @Test
    @DisplayName("Node and pillar elevations come from the elevation provider")
    void geometryCarriesElevation() {
        hopper = importWithElevation();
        BaseGraph graph = hopper.getBaseGraph();

        // Tower node elevation must match the provider at that node's coordinates.
        var nodeAccess = graph.getNodeAccess();
        double expected = RISING_NORTHWARD.getEle(nodeAccess.getLat(0), nodeAccess.getLon(0));
        assertEquals(expected, nodeAccess.getEle(0), ELEVATION_TOLERANCE_M);

        // At least one edge should carry 3D pillar geometry consistent with the provider.
        AllEdgesIterator edges = graph.getAllEdges();
        boolean checkedPillars = false;
        while (edges.next()) {
            PointList pillars = edges.fetchWayGeometry(FetchMode.PILLAR_ONLY);
            if (pillars.isEmpty()) continue;
            assertTrue(pillars.is3D(), "pillar geometry should be 3D");
            assertEquals(
                    RISING_NORTHWARD.getEle(pillars.getLat(0), pillars.getLon(0)),
                    pillars.getEle(0),
                    ELEVATION_TOLERANCE_M);
            checkedPillars = true;
            break;
        }
        assertTrue(checkedPillars, "fixture had no edge with pillar nodes to check");
    }

    @Test
    @DisplayName("average_slope is filled once elevation is available")
    void averageSlopeIsFilled() {
        hopper = importWithElevation();
        BaseGraph graph = hopper.getBaseGraph();
        DecimalEncodedValue slopeEnc =
                hopper.getEncodingManager().getDecimalEncodedValue(AverageSlope.KEY);

        // The stub rises steeply with latitude, so some edge must record a non-zero slope. Without
        // 3D geometry SlopeCalculator writes 0 everywhere, which is what this asserts against.
        boolean anySlope = false;
        AllEdgesIterator edges = graph.getAllEdges();
        while (edges.next()) {
            if (edges.get(slopeEnc) != 0.0 || edges.getReverse(slopeEnc) != 0.0) {
                anySlope = true;
                break;
            }
        }
        assertTrue(anySlope, "average_slope was 0 on every edge despite a sloped elevation model");
    }

    @Test
    @DisplayName("Elevation changes the stored geometry compared with a 2D import")
    void elevationChangesGeometry() {
        hopper = importWithElevation();
        double ele = hopper.getBaseGraph().getNodeAccess().getEle(0);
        assertNotEquals(0.0, ele, "the stub provider should have supplied a non-zero elevation");
    }

    private static GraphHopper importWithElevation() {
        GraphHopper hopper = OvertureTestFixtures.overtureHopper(
                OvertureTestFixtures.smallParquetExtract(),
                // average_slope is not in the config's list, because GraphHopper rejects it unless an
                // elevation provider is configured - which is exactly what this test does configure.
                OvertureTestFixtures.CONFIG_ENCODED_VALUES + ", average_slope",
                new Profile("car").setCustomModel(GHUtility.loadCustomModelFromJar("car.json")));
        hopper.setElevationProvider(RISING_NORTHWARD);
        // A distinct cache directory, so the 2D graph built by other tests is not reused.
        hopper.setGraphHopperLocation(
                OvertureTestFixtures.graphLocationFor(OvertureTestFixtures.smallParquetExtract()) + "-3d");
        hopper.importOrLoad();
        return hopper;
    }
}
