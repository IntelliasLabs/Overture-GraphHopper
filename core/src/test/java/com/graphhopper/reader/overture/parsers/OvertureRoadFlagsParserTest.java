package com.graphhopper.reader.overture.parsers;

import static org.mockito.Mockito.*;

import com.graphhopper.reader.overture.road.flags.OvertureRoadFlags;
import com.graphhopper.reader.overture.road.segment.OvertureRoadProperties;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.util.EdgeIteratorState;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OvertureRoadFlagsParserTest {

    private EdgeIteratorState edge;
    private BooleanEncodedValue bridgeEnc;
    private BooleanEncodedValue tunnelEnc;

    @BeforeEach
    public void setUp() {
        edge = mock(EdgeIteratorState.class);
        bridgeEnc = mock(BooleanEncodedValue.class);
        tunnelEnc = mock(BooleanEncodedValue.class);
    }

    @Test
    public void testApplyFlags_Bridge() {
        OvertureRoadFlags bridgeFlag =
                new OvertureRoadFlags(true, false, false, false, false, false, null);
        OvertureRoadSegment segment = createSegment(List.of(bridgeFlag));

        OvertureRoadFlagsParser.applyFlags(edge, segment, bridgeEnc, tunnelEnc);

        verify(edge).set(bridgeEnc, true);
        verify(edge).set(tunnelEnc, false);
    }

    @Test
    public void testApplyFlags_Tunnel() {
        OvertureRoadFlags tunnelFlag =
                new OvertureRoadFlags(false, true, false, false, false, false, null);
        OvertureRoadSegment segment = createSegment(List.of(tunnelFlag));

        OvertureRoadFlagsParser.applyFlags(edge, segment, bridgeEnc, tunnelEnc);

        verify(edge).set(bridgeEnc, false);
        verify(edge).set(tunnelEnc, true);
    }

    @Test
    public void testApplyFlags_NoFlags() {
        OvertureRoadSegment segment = createSegment(List.of());

        OvertureRoadFlagsParser.applyFlags(edge, segment, bridgeEnc, tunnelEnc);

        verify(edge).set(bridgeEnc, false);
        verify(edge).set(tunnelEnc, false);
    }

    @Test
    public void testApplyFlags_NullProperties() {
        OvertureRoadSegment segment = new OvertureRoadSegment("id", null, null);
        OvertureRoadFlagsParser.applyFlags(edge, segment, bridgeEnc, tunnelEnc);
        verifyNoInteractions(edge);
    }

    private OvertureRoadSegment createSegment(List<OvertureRoadFlags> flags) {
        OvertureRoadProperties props = new OvertureRoadProperties(
                null, null, null, null, null, null, flags, null, null, null, null, null, 0, null, null,
                null, 0, null, null);
        return new OvertureRoadSegment("id", null, props);
    }
}
