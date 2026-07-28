package com.graphhopper.reader.overture.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.graphhopper.reader.overture.road.segment.OvertureRoadProperties;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.reader.overture.road.segment.OvertureSegmentSubtype;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.FerrySpeed;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.util.EdgeIteratorState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

/**
 * Covers {@code ferry_speed} on Overture water segments.
 *
 * <p>The invariant that matters is that a ferry never keeps the storage default of 0: every in-built
 * speed custom model limits ferry edges to {@code ferry_speed}, so a 0 makes the crossing
 * unroutable rather than slow.
 */
class OvertureFerrySpeedParserTest {

    /**
     * {@code ferry_speed} is stored in 5 bits with a factor of 2, so a written speed comes back
     * rounded to the nearest 2 km/h. That is upstream's encoding, shared with the OSM path.
     */
    private static final double STORAGE_GRANULARITY_KMH = 2;

    private DecimalEncodedValue ferrySpeedEnc;
    private BaseGraph graph;
    private EdgeIteratorState edge;

    @BeforeEach
    void setup() {
        ferrySpeedEnc = FerrySpeed.create();
        EncodingManager em = EncodingManager.start().add(ferrySpeedEnc).build();
        graph = new BaseGraph.Builder(em).create();
        edge = graph.edge(0, 1);
    }

    @AfterEach
    void tearDown() {
        if (graph != null) graph.close();
    }

    @Test
    @DisplayName("A short crossing gets the slowest fallback speed")
    void shortCrossing() {
        edge.setDistance(500);
        parse(ferrySegment());
        assertEquals(5, edge.get(ferrySpeedEnc), STORAGE_GRANULARITY_KMH);
    }

    @Test
    @DisplayName("A medium crossing gets the mid fallback speed")
    void mediumCrossing() {
        edge.setDistance(10_000);
        parse(ferrySegment());
        assertEquals(15, edge.get(ferrySpeedEnc), STORAGE_GRANULARITY_KMH);
    }

    @Test
    @DisplayName("A long crossing gets the open-water fallback speed")
    void longCrossing() {
        edge.setDistance(50_000);
        parse(ferrySegment());
        assertEquals(30, edge.get(ferrySpeedEnc), STORAGE_GRANULARITY_KMH);
    }

    @Test
    @DisplayName("A ferry is never left at the storage default, which would block the edge")
    void ferryIsNeverZero() {
        // One metre is far below anything the fallback table produces, so this exercises the clamp
        // to the smallest storable non-zero value rather than the thresholds.
        edge.setDistance(1);
        parse(ferrySegment());
        assertTrue(edge.get(ferrySpeedEnc) > 0, "ferry_speed was left at 0, blocking the crossing");
    }

    @Test
    @DisplayName("A road segment is left untouched")
    void roadSegmentUntouched() {
        edge.setDistance(500);
        parse(segmentWith(OvertureSegmentSubtype.ROAD));
        assertEquals(0, edge.get(ferrySpeedEnc), 0.001);
    }

    private void parse(OvertureRoadSegment segment) {
        new OvertureFerrySpeedParser(ferrySpeedEnc).handleSegment(edge, segment, null);
    }

    private static OvertureRoadSegment ferrySegment() {
        return segmentWith(OvertureSegmentSubtype.WATER);
    }

    private static OvertureRoadSegment segmentWith(OvertureSegmentSubtype subtype) {
        LineString line = new GeometryFactory()
                .createLineString(new Coordinate[] {new Coordinate(0, 0), new Coordinate(1, 1)});
        OvertureRoadProperties props = new OvertureRoadProperties(
                null, null, null, null, null, null, null, null, null, null, null, null, 0, null,
                null, null, 0, null, null, subtype);
        return new OvertureRoadSegment("ferry-1", line, props);
    }
}
