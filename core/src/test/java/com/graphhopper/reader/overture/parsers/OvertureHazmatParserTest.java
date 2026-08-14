package com.graphhopper.reader.overture.parsers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.graphhopper.reader.overture.road.flags.OvertureRoadFlags;
import com.graphhopper.reader.overture.road.segment.OvertureRoadProperties;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class OvertureHazmatParserTest {

    private OvertureRoadFlags flags;
    private OvertureRoadProperties properties;
    private OvertureRoadSegment segment;

    @BeforeEach
    public void setUp() {
        flags = mock(OvertureRoadFlags.class);
        properties = mock(OvertureRoadProperties.class);
        segment = mock(OvertureRoadSegment.class);

        when(segment.getProperties()).thenReturn(properties);
        when(properties.getFlags()).thenReturn(List.of(flags));
    }

    @Test
    @DisplayName("Should return false for normal roads (no tunnel)")
    public void testNoHazmatRestriction_NormalRoad() {
        when(flags.isTunnel()).thenReturn(false);
        assertFalse(OvertureHazmatParser.hasHazmatRestriction(segment));
    }

    @Test
    @DisplayName("Should return true when the segment is a tunnel")
    public void testHazmatRestriction_TunnelSegment() {
        when(flags.isTunnel()).thenReturn(true);
        assertTrue(OvertureHazmatParser.hasHazmatRestriction(segment));
    }

    @Test
    @DisplayName("Should return false (safe) when segment is null")
    public void testNoHazmatRestriction_NullSegment() {
        assertFalse(OvertureHazmatParser.hasHazmatRestriction(null));
    }

    @Test
    @DisplayName("Should return false when properties are null")
    public void testNoHazmatRestriction_NullProperties() {
        when(segment.getProperties()).thenReturn(null);
        assertFalse(OvertureHazmatParser.hasHazmatRestriction(segment));
    }

    @Test
    @DisplayName("Should return false when flags list is null")
    public void testNoHazmatRestriction_NullFlags() {
        when(properties.getFlags()).thenReturn(null);
        assertFalse(OvertureHazmatParser.hasHazmatRestriction(segment));
    }

    @Test
    @DisplayName("Should return false when flags list is empty")
    public void testNoHazmatRestriction_EmptyFlags() {
        when(properties.getFlags()).thenReturn(List.of());
        assertFalse(OvertureHazmatParser.hasHazmatRestriction(segment));
    }
}
