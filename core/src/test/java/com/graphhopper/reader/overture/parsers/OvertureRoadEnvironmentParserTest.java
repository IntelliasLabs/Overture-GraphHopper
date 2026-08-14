package com.graphhopper.reader.overture.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.graphhopper.reader.overture.road.flags.OvertureRoadFlags;
import com.graphhopper.reader.overture.road.segment.OvertureRoadProperties;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.reader.overture.road.segment.OvertureSegmentSubtype;
import com.graphhopper.routing.ev.RoadEnvironment;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class OvertureRoadEnvironmentParserTest {

    @Test
    @DisplayName("Should detect BRIDGE when bridge flag is present")
    void TestBridge() {
        OvertureRoadFlags flag = mock(OvertureRoadFlags.class);
        when(flag.isBridge()).thenReturn(true);

        OvertureRoadSegment segment = mockSegment(List.of(flag), OvertureSegmentSubtype.ROAD);
        assertEquals(RoadEnvironment.BRIDGE, OvertureRoadEnvironmentParser.parse(segment));
    }

    @Test
    @DisplayName("Should detect TUNNEL when tunnel flag is present")
    void TestTunnel() {
        OvertureRoadFlags flag = mock(OvertureRoadFlags.class);
        when(flag.isTunnel()).thenReturn(true);

        OvertureRoadSegment segment = mockSegment(List.of(flag), OvertureSegmentSubtype.ROAD);
        assertEquals(RoadEnvironment.TUNNEL, OvertureRoadEnvironmentParser.parse(segment));
    }

    @Test
    @DisplayName("Should detect ROAD when subtype is ROAD")
    void TestRoad() {
        OvertureRoadSegment segment = mockSegment(null, OvertureSegmentSubtype.ROAD);
        assertEquals(RoadEnvironment.ROAD, OvertureRoadEnvironmentParser.parse(segment));
    }

    @Test
    @DisplayName("Should detect FERRY when subtype is WATER")
    void TestFerry() {
        OvertureRoadSegment segment = mockSegment(List.of(), OvertureSegmentSubtype.WATER);
        assertEquals(RoadEnvironment.FERRY, OvertureRoadEnvironmentParser.parse(segment));
    }

    @Test
    @DisplayName("Should detect OTHER when subtype and flags not mapped")
    void TestOther() {
        OvertureRoadSegment segment = mockSegment(null, OvertureSegmentSubtype.RAIL);
        assertEquals(RoadEnvironment.OTHER, OvertureRoadEnvironmentParser.parse(segment));
    }

    private OvertureRoadSegment mockSegment(
            List<OvertureRoadFlags> flag, OvertureSegmentSubtype overtureRoadSubtype) {
        OvertureRoadSegment segment = mock(OvertureRoadSegment.class);
        OvertureRoadProperties properties = mock(OvertureRoadProperties.class);

        when(segment.getProperties()).thenReturn(properties);
        when(properties.getFlags()).thenReturn(flag);
        when(properties.getSubtype()).thenReturn(overtureRoadSubtype);

        return segment;
    }
}
