package com.graphhopper.reader.overture.parsers;

import static org.mockito.Mockito.*;

import com.graphhopper.reader.overture.access.restriction.AccessType;
import com.graphhopper.reader.overture.access.restriction.OvertureAccessRestriction;
import com.graphhopper.reader.overture.access.restriction.PropertyScopeContainer;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelHeading;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelMode;
import com.graphhopper.reader.overture.road.segment.OvertureRoadClass;
import com.graphhopper.reader.overture.road.segment.OvertureRoadProperties;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.util.EdgeIteratorState;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OvertureFootAccessParserTest {

    private EdgeIteratorState edge;
    private OvertureRoadSegment segment;
    private OvertureRoadProperties properties;
    private BooleanEncodedValue accessEnc;

    @BeforeEach
    void setup() {
        edge = mock(EdgeIteratorState.class);
        segment = mock(OvertureRoadSegment.class);
        properties = mock(OvertureRoadProperties.class);
        accessEnc = mock(BooleanEncodedValue.class);

        when(segment.getProperties()).thenReturn(properties);
        when(properties.getRoadClass()).thenReturn(OvertureRoadClass.RESIDENTIAL);
    }

    @Test
    void accessible_NoRestrictions_AllowedBothDirections() {
        when(properties.getAccessRestrictions()).thenReturn(null);

        OvertureFootAccessParser.parseAccess(edge, segment, accessEnc);

        verify(edge).set(accessEnc, true, true);
    }

    @Test
    void pedestrianOnly_RoadClassPedestrian_AllowedBothDirections() {
        when(properties.getRoadClass()).thenReturn(OvertureRoadClass.PEDESTRIAN);
        when(properties.getAccessRestrictions()).thenReturn(null);

        OvertureFootAccessParser.parseAccess(edge, segment, accessEnc);

        verify(edge).set(accessEnc, true, true);
    }

    @Test
    void noPedestrianAccess_ExplicitFootDenied_DeniedBothDirections() {
        OvertureAccessRestriction restriction =
                createRestriction(TravelMode.FOOT, AccessType.DENIED, null);
        when(properties.getAccessRestrictions()).thenReturn(List.of(restriction));

        OvertureFootAccessParser.parseAccess(edge, segment, accessEnc);

        verify(edge).set(accessEnc, false, false);
    }

    private OvertureAccessRestriction createRestriction(
            TravelMode mode, AccessType type, TravelHeading heading) {
        ArrayList<TravelMode> modes = new ArrayList<>();
        if (mode != null) {
            modes.add(mode);
        }

        PropertyScopeContainer scope = mock(PropertyScopeContainer.class);
        when(scope.getMode()).thenReturn(modes);
        when(scope.getHeading()).thenReturn(heading);

        OvertureAccessRestriction restriction = mock(OvertureAccessRestriction.class);
        when(restriction.hasWhen()).thenReturn(true);
        when(restriction.getWhen()).thenReturn(scope);
        when(restriction.hasAccessType()).thenReturn(true);
        when(restriction.getAccessType()).thenReturn(type);

        return restriction;
    }
}
