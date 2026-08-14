package com.graphhopper.reader.overture.parsers;

import static org.mockito.Mockito.*;

import com.graphhopper.reader.overture.road.segment.OvertureRoadProperties;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.reader.overture.road.surface.OvertureRoadSurface;
import com.graphhopper.reader.overture.road.surface.RoadSurfaceType;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.ev.Surface;
import com.graphhopper.util.EdgeIteratorState;
import java.util.List;
import org.junit.jupiter.api.Test;

public class OvertureRoadSurfaceParserTest {

    private OvertureRoadSegment mockSegmentWithRoadSurface(RoadSurfaceType roadSurfaceType) {
        OvertureRoadSurface roadSurface = mock(OvertureRoadSurface.class);
        when(roadSurface.getSurfaceType()).thenReturn(roadSurfaceType);

        OvertureRoadProperties properties = mock(OvertureRoadProperties.class);
        when(properties.getSurfaces()).thenReturn(List.of(roadSurface));

        OvertureRoadSegment segment = mock(OvertureRoadSegment.class);
        when(segment.getProperties()).thenReturn(properties);

        return segment;
    }

    @Test
    public void parseSurfaceTest_PavedSurface() {
        OvertureRoadSegment segment = mockSegmentWithRoadSurface(RoadSurfaceType.PAVED);
        EdgeIteratorState edge = mock(EdgeIteratorState.class);
        EnumEncodedValue<Surface> surfaceEnc = new EnumEncodedValue<>("surface", Surface.class);

        OvertureRoadSurfaceParser.parseSurface(edge, segment, surfaceEnc);

        verify(edge).set(surfaceEnc, Surface.PAVED);
    }

    @Test
    public void parseSurfaceTest_UnpavedSurface() {
        OvertureRoadSegment segment = mockSegmentWithRoadSurface(RoadSurfaceType.UNPAVED);
        EdgeIteratorState edge = mock(EdgeIteratorState.class);
        EnumEncodedValue<Surface> surfaceEnc = new EnumEncodedValue<>("surface", Surface.class);

        OvertureRoadSurfaceParser.parseSurface(edge, segment, surfaceEnc);

        verify(edge).set(surfaceEnc, Surface.UNPAVED);
    }

    @Test
    public void parseSurfaceTest_AsphaltSurface() {
        OvertureRoadSegment segment = mockSegmentWithRoadSurface(RoadSurfaceType.ASPHALT);
        EdgeIteratorState edge = mock(EdgeIteratorState.class);
        EnumEncodedValue<Surface> surfaceEnc = new EnumEncodedValue<>("surface", Surface.class);

        OvertureRoadSurfaceParser.parseSurface(edge, segment, surfaceEnc);

        verify(edge).set(surfaceEnc, Surface.ASPHALT);
    }

    @Test
    public void parseSurfaceTest_UnknownSurface() {
        OvertureRoadSegment segment = mockSegmentWithRoadSurface(RoadSurfaceType.UNKNOWN);
        EdgeIteratorState edge = mock(EdgeIteratorState.class);
        EnumEncodedValue<Surface> surfaceEnc = new EnumEncodedValue<>("surface", Surface.class);

        OvertureRoadSurfaceParser.parseSurface(edge, segment, surfaceEnc);

        verify(edge).set(surfaceEnc, Surface.PAVED);
    }

    @Test
    public void parseSurfaceTest_DefaultSurface() {
        OvertureRoadSegment segment = mockSegmentWithRoadSurface(null);
        EdgeIteratorState edge = mock(EdgeIteratorState.class);
        EnumEncodedValue<Surface> surfaceEnc = new EnumEncodedValue<>("surface", Surface.class);

        OvertureRoadSurfaceParser.parseSurface(edge, segment, surfaceEnc);

        verify(edge).set(surfaceEnc, Surface.PAVED);
    }
}
