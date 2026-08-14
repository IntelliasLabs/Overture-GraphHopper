package com.graphhopper.reader.overture.parsers;

import static org.junit.jupiter.api.Assertions.*;

import com.graphhopper.reader.overture.road.segment.OvertureRoadProperties;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.reader.overture.road.segment.OvertureSegmentSubtype;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

class OvertureFerryParserTest {
    @Test
    void isFerryReturnsTrueForWaterSubtype() {
        GeometryFactory gf = new GeometryFactory();
        LineString line =
                gf.createLineString(new Coordinate[] {new Coordinate(0, 0), new Coordinate(1, 1)});
        OvertureRoadProperties props = new OvertureRoadProperties(
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
                null,
                0,
                null,
                null,
                null,
                0,
                null,
                null,
                OvertureSegmentSubtype.WATER);
        OvertureRoadSegment segment = new OvertureRoadSegment("id1", line, props);
        assertTrue(OvertureFerryParser.isFerry(segment));
    }

    @Test
    void isFerryReturnsFalseForRoadSubtype() {
        GeometryFactory gf = new GeometryFactory();
        LineString line =
                gf.createLineString(new Coordinate[] {new Coordinate(0, 0), new Coordinate(1, 1)});
        OvertureRoadProperties props = new OvertureRoadProperties(
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
                null,
                0,
                null,
                null,
                null,
                0,
                null,
                null,
                OvertureSegmentSubtype.ROAD);
        OvertureRoadSegment segment = new OvertureRoadSegment("id2", line, props);
        assertFalse(OvertureFerryParser.isFerry(segment));
    }

    @Test
    void isFerryReturnsFalseForRailSubtype() {
        GeometryFactory gf = new GeometryFactory();
        LineString line =
                gf.createLineString(new Coordinate[] {new Coordinate(0, 0), new Coordinate(1, 1)});
        OvertureRoadProperties props = new OvertureRoadProperties(
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
                null,
                0,
                null,
                null,
                null,
                0,
                null,
                null,
                OvertureSegmentSubtype.RAIL);
        OvertureRoadSegment segment = new OvertureRoadSegment("id3", line, props);
        assertFalse(OvertureFerryParser.isFerry(segment));
    }

    @Test
    void isFerryReturnsFalseForDefaultSubtype() {
        GeometryFactory gf = new GeometryFactory();
        LineString line =
                gf.createLineString(new Coordinate[] {new Coordinate(0, 0), new Coordinate(1, 1)});
        OvertureRoadProperties props = new OvertureRoadProperties(
                null, null, null, null, null, null, null, null, null, null, null, null, 0, null, null, null,
                0, null, null);
        OvertureRoadSegment segment = new OvertureRoadSegment("id4", line, props);
        assertFalse(OvertureFerryParser.isFerry(segment));
    }

    @Test
    void isFerryReturnsFalseForNullSegment() {
        assertFalse(OvertureFerryParser.isFerry(null));
    }

    @Test
    void isFerryReturnsFalseForNullProperties() {
        GeometryFactory gf = new GeometryFactory();
        LineString line =
                gf.createLineString(new Coordinate[] {new Coordinate(0, 0), new Coordinate(1, 1)});
        OvertureRoadSegment segment = new OvertureRoadSegment("id5", line, null);
        assertFalse(OvertureFerryParser.isFerry(segment));
    }
}
