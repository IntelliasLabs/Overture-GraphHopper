package com.graphhopper.reader.overture.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.graphhopper.reader.overture.road.segment.OvertureRoadClass;
import com.graphhopper.reader.overture.road.segment.OvertureRoadProperties;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.reader.overture.road.surface.OvertureRoadSurface;
import com.graphhopper.reader.overture.road.surface.RoadSurfaceType;
import com.graphhopper.routing.ev.TrackType;
import java.util.List;
import org.junit.jupiter.api.Test;

public class OvertureTrackTypeParserTest {

    private OvertureRoadProperties makeProps(
            OvertureRoadClass cls, List<OvertureRoadSurface> surfaces) {
        return new OvertureRoadProperties(
                null, null, cls, null, null, surfaces, null, null, null, null, null, null, 0, null, null,
                null, 0, null, null);
    }

    @Test
    public void gravelTrack() {
        OvertureRoadProperties props = makeProps(
                OvertureRoadClass.TRACK, List.of(new OvertureRoadSurface(RoadSurfaceType.GRAVEL, null)));

        OvertureRoadSegment segment = new OvertureRoadSegment("1", null, props);
        TrackType t = OvertureTrackTypeParser.parse(segment);
        assertEquals(TrackType.GRADE2, t);
    }

    @Test
    public void pavedRoadNotTrack() {
        OvertureRoadProperties props = makeProps(
                OvertureRoadClass.RESIDENTIAL,
                List.of(new OvertureRoadSurface(RoadSurfaceType.PAVED, null)));

        OvertureRoadSegment segment = new OvertureRoadSegment("2", null, props);
        TrackType t = OvertureTrackTypeParser.parse(segment);
        // parser only considers TRACK/PATH classes; residential + paved -> MISSING
        assertEquals(TrackType.MISSING, t);
    }

    @Test
    public void pavedTrackShouldBeGrade1() {
        OvertureRoadProperties props = makeProps(
                OvertureRoadClass.TRACK, List.of(new OvertureRoadSurface(RoadSurfaceType.PAVED, null)));

        OvertureRoadSegment segment = new OvertureRoadSegment("3", null, props);
        TrackType t = OvertureTrackTypeParser.parse(segment);
        assertEquals(TrackType.GRADE1, t);
    }

    @Test
    public void missingSurfaceReturnsMissing() {
        OvertureRoadProperties props = makeProps(OvertureRoadClass.TRACK, null);

        OvertureRoadSegment segment = new OvertureRoadSegment("4", null, props);
        TrackType t = OvertureTrackTypeParser.parse(segment);
        assertEquals(TrackType.MISSING, t);
    }

    @Test
    public void pathWithGravelShouldBeGrade2() {
        OvertureRoadProperties props = makeProps(
                OvertureRoadClass.PATH, List.of(new OvertureRoadSurface(RoadSurfaceType.GRAVEL, null)));

        OvertureRoadSegment segment = new OvertureRoadSegment("5", null, props);
        TrackType t = OvertureTrackTypeParser.parse(segment);
        assertEquals(TrackType.GRADE2, t);
    }

    @Test
    public void unknownSurfaceEnumReturnsMissing() {
        OvertureRoadProperties props = makeProps(
                OvertureRoadClass.TRACK, List.of(new OvertureRoadSurface(RoadSurfaceType.UNKNOWN, null)));

        OvertureRoadSegment segment = new OvertureRoadSegment("6", null, props);
        TrackType t = OvertureTrackTypeParser.parse(segment);
        assertEquals(TrackType.MISSING, t);
    }

    @Test
    public void nullSurfaceTypeReturnsMissing() {
        OvertureRoadProperties props =
                makeProps(OvertureRoadClass.TRACK, List.of(new OvertureRoadSurface(null, null)));

        OvertureRoadSegment segment = new OvertureRoadSegment("7", null, props);
        TrackType t = OvertureTrackTypeParser.parse(segment);
        assertEquals(TrackType.MISSING, t);
    }
}
