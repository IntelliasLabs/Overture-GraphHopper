package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.access.restriction.AccessType;
import com.graphhopper.reader.overture.access.restriction.OvertureAccessRestriction;
import com.graphhopper.reader.overture.access.restriction.PropertyScopeContainer;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelMode;
import com.graphhopper.reader.overture.road.segment.OvertureRoadProperties;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.SimpleBooleanEncodedValue;
import com.graphhopper.util.EdgeIteratorState;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class OvertureBusAccessParserTest {

    @Test
    public void parseAccessTest_BusAllowed() {
        ArrayList<TravelMode> travelModes = new ArrayList<>(List.of(TravelMode.BUS));
        List<OvertureAccessRestriction> restrictions = List.of(
                new OvertureAccessRestriction(AccessType.ALLOWED, PropertyScopeContainer.ofMode(travelModes), null)
        );

        OvertureRoadSegment segment = mockSegment(restrictions);

        EdgeIteratorState edge = mock(EdgeIteratorState.class);
        BooleanEncodedValue busEnc = new SimpleBooleanEncodedValue("bus", true);

        OvertureBusAccessParser.parseAccess(edge, segment, busEnc);

        verify(edge).set(busEnc, true);
    }

    @Test
    void parseAccessTest_BusExclusive() {
        ArrayList<TravelMode> travelModesBus = new ArrayList<>(List.of(TravelMode.BUS));
        ArrayList<TravelMode> travelModesCar = new ArrayList<>(List.of(TravelMode.MOTOR_VEHICLE));
        List<OvertureAccessRestriction> restrictions = List.of(
                new OvertureAccessRestriction(AccessType.ALLOWED, PropertyScopeContainer.ofMode(travelModesBus), null),
                new OvertureAccessRestriction(AccessType.DENIED, PropertyScopeContainer.ofMode(travelModesCar), null)
        );

        OvertureRoadSegment segment = mockSegment(restrictions);

        EdgeIteratorState edge = mock(EdgeIteratorState.class);
        BooleanEncodedValue busEnc = new SimpleBooleanEncodedValue("bus", true);

        OvertureBusAccessParser.parseAccess(edge, segment, busEnc);

        verify(edge).set(busEnc, true);
    }

    @Test
    void parseAccessTest_BusDenied() {
        ArrayList<TravelMode> travelModes = new ArrayList<>(List.of(TravelMode.BUS));
        List<OvertureAccessRestriction> restrictions = List.of(
                new OvertureAccessRestriction(AccessType.DENIED, PropertyScopeContainer.ofMode(travelModes), null)
        );

        OvertureRoadSegment segment = mockSegment(restrictions);

        EdgeIteratorState edge = mock(EdgeIteratorState.class);
        BooleanEncodedValue busEnc = new SimpleBooleanEncodedValue("bus", true);

        OvertureBusAccessParser.parseAccess(edge, segment, busEnc);

        verify(edge).set(busEnc, false);
    }

    @Test
    void parseAccessTest_MotorVehicleDenied() {
        ArrayList<TravelMode> travelModes = new ArrayList<>(List.of(TravelMode.MOTOR_VEHICLE));
        List<OvertureAccessRestriction> restrictions = List.of(
                new OvertureAccessRestriction(AccessType.DENIED, PropertyScopeContainer.ofMode(travelModes), null)
        );

        OvertureRoadSegment segment = mockSegment(restrictions);

        EdgeIteratorState edge = mock(EdgeIteratorState.class);
        BooleanEncodedValue busEnc = new SimpleBooleanEncodedValue("bus", true);

        OvertureBusAccessParser.parseAccess(edge, segment, busEnc);

        verify(edge).set(busEnc, false);
    }

    @Test
    void parseAccessTest_MotorVehicleAllowed() {
        ArrayList<TravelMode> travelModes = new ArrayList<>(List.of(TravelMode.MOTOR_VEHICLE));
        List<OvertureAccessRestriction> restrictions = List.of(
                new OvertureAccessRestriction(AccessType.ALLOWED, PropertyScopeContainer.ofMode(travelModes), null)
        );

        OvertureRoadSegment segment = mockSegment(restrictions);

        EdgeIteratorState edge = mock(EdgeIteratorState.class);
        BooleanEncodedValue busEnc = new SimpleBooleanEncodedValue("bus", true);

        OvertureBusAccessParser.parseAccess(edge, segment, busEnc);

        verify(edge).set(busEnc, true);
    }

    @Test
    void parseAccessTest_NoRestrictions() {

        OvertureRoadSegment segment = mockSegment(null);

        EdgeIteratorState edge = mock(EdgeIteratorState.class);
        BooleanEncodedValue busEnc = new SimpleBooleanEncodedValue("bus", true);

        OvertureBusAccessParser.parseAccess(edge, segment, busEnc);

        verify(edge).set(busEnc, true);
    }

    @Test
    void parseAccessTest_AllAllowed() {
        List<OvertureAccessRestriction> restrictions = List.of(
                new OvertureAccessRestriction(AccessType.ALLOWED, null, null)
        );

        OvertureRoadSegment segment = mockSegment(restrictions);

        EdgeIteratorState edge = mock(EdgeIteratorState.class);
        BooleanEncodedValue busEnc = new SimpleBooleanEncodedValue("bus", true);

        OvertureBusAccessParser.parseAccess(edge, segment, busEnc);

        verify(edge).set(busEnc, true);
    }

    @Test
    void parseAccessTest_AllDenied() {
        List<OvertureAccessRestriction> restrictions = List.of(
                new OvertureAccessRestriction(AccessType.DENIED, null, null)
        );

        OvertureRoadSegment segment = mockSegment(restrictions);

        EdgeIteratorState edge = mock(EdgeIteratorState.class);
        BooleanEncodedValue busEnc = new SimpleBooleanEncodedValue("bus", true);

        OvertureBusAccessParser.parseAccess(edge, segment, busEnc);

        verify(edge).set(busEnc, false);
    }

    private OvertureRoadSegment mockSegment(List<OvertureAccessRestriction> restrictions) {
        OvertureRoadSegment segment = mock(OvertureRoadSegment.class);
        OvertureRoadProperties props = mock(OvertureRoadProperties.class);
        when(segment.getProperties()).thenReturn(props);
        when(props.getAccessRestrictions()).thenReturn(restrictions);
        return segment;
    }
}
