package com.graphhopper.reader.overture.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.reader.overture.road.surface.OvertureRoadSurface;
import com.graphhopper.reader.overture.road.surface.RoadSurfaceType;
import com.graphhopper.routing.ev.Smoothness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class OvertureSmoothnessParserTest {
    @ParameterizedTest(name = "Mapping: surface={0} -> expected={1}")
    @CsvSource({
        "ASPHALT,      EXCELLENT",
        "CONCRETE,     EXCELLENT",
        "METAL,        GOOD",
        "PAVED,        GOOD",
        "PAVING_STONES, INTERMEDIATE",
        "GRAVEL,       BAD",
        "UNPAVED,      VERY_BAD",
        "DIRT,         HORRIBLE",
        "UNKNOWN,      MISSING"
    })
    @DisplayName("Should correctly infer Smoothness from RoadSurfaceType")
    void testSmoothnessInference(RoadSurfaceType type, Smoothness expected) {
        OvertureRoadSegment segment = mock(OvertureRoadSegment.class);
        OvertureRoadSurface surface = mock(OvertureRoadSurface.class);

        when(segment.getRoadSurface()).thenReturn(surface);
        when(surface.getSurfaceType()).thenReturn(type);

        assertEquals(
                expected,
                OvertureSmoothnessParser.parse(segment),
                "Inference failed for surface type: " + type);
    }

    @Test
    @DisplayName("Should return MISSING when segment is null")
    void testNullSegment() {
        assertEquals(Smoothness.MISSING, OvertureSmoothnessParser.parse(null));
    }

    @Test
    @DisplayName("Should return MISSING when road surface object is missing")
    void testNullRoadSurface() {
        OvertureRoadSegment segment = mock(OvertureRoadSegment.class);
        when(segment.getRoadSurface()).thenReturn(null);

        assertEquals(Smoothness.MISSING, OvertureSmoothnessParser.parse(segment));
    }
}
