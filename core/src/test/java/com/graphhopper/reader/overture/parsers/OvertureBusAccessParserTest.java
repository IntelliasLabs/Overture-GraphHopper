package com.graphhopper.reader.overture.parsers;

import static org.mockito.Mockito.*;

import com.graphhopper.reader.overture.access.restriction.AccessType;
import com.graphhopper.reader.overture.access.restriction.OvertureAccessRestriction;
import com.graphhopper.reader.overture.access.restriction.PropertyScopeContainer;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelHeading;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelMode;
import com.graphhopper.reader.overture.road.segment.OvertureRoadProperties;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.util.EdgeIteratorState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OvertureBusAccessParserTest {

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
        when(segment.isAccessible()).thenReturn(true);
    }

    private void parse() {
        new OvertureBusAccessParser(accessEnc).handleSegment(edge, segment, null);
    }

    private static PropertyScopeContainer mode(TravelMode... modes) {
        return PropertyScopeContainer.ofMode(new ArrayList<>(List.of(modes)));
    }

    @Test
    void noRestrictionsAllowsBothDirections() {
        when(properties.getAccessRestrictions()).thenReturn(Collections.emptyList());

        parse();

        verify(edge).set(accessEnc, true, true);
    }

    @Test
    void nullRestrictionsAllowsBothDirections() {
        when(properties.getAccessRestrictions()).thenReturn(null);

        parse();

        verify(edge).set(accessEnc, true, true);
    }

    @Test
    void busDeniedClosesBothDirections() {
        when(properties.getAccessRestrictions())
                .thenReturn(
                        List.of(new OvertureAccessRestriction(AccessType.DENIED, mode(TravelMode.BUS), null)));

        parse();

        verify(edge).set(accessEnc, false, false);
    }

    @Test
    @DisplayName("A motor_vehicle denial closes the road to buses through the mode hierarchy")
    void motorVehicleDenialAppliesToBuses() {
        when(properties.getAccessRestrictions())
                .thenReturn(List.of(new OvertureAccessRestriction(
                        AccessType.DENIED, mode(TravelMode.MOTOR_VEHICLE), null)));

        parse();

        verify(edge).set(accessEnc, false, false);
    }

    @Test
    @DisplayName("An explicit bus allowance lifts a broader motor_vehicle denial")
    void busAllowanceOverridesMotorVehicleDenial() {
        when(properties.getAccessRestrictions())
                .thenReturn(List.of(
                        new OvertureAccessRestriction(AccessType.DENIED, mode(TravelMode.MOTOR_VEHICLE), null),
                        new OvertureAccessRestriction(AccessType.ALLOWED, mode(TravelMode.BUS), null)));

        parse();

        verify(edge).set(accessEnc, true, true);
    }

    @Test
    @DisplayName("A bus-only road stays open to buses whatever the restriction order")
    void busAllowanceOverridesMotorVehicleDenialWhateverTheOrder() {
        when(properties.getAccessRestrictions())
                .thenReturn(List.of(
                        new OvertureAccessRestriction(AccessType.ALLOWED, mode(TravelMode.BUS), null),
                        new OvertureAccessRestriction(
                                AccessType.DENIED, mode(TravelMode.MOTOR_VEHICLE), null)));

        parse();

        verify(edge).set(accessEnc, true, true);
    }

    @Test
    @DisplayName("An unconditional denial closes the road, as it does for car, bike and foot")
    void generalDenialClosesTheRoad() {
        when(properties.getAccessRestrictions())
                .thenReturn(List.of(new OvertureAccessRestriction(AccessType.DENIED, null, null)));

        parse();

        verify(edge).set(accessEnc, false, false);
    }

    @Test
    @DisplayName("A backward-heading denial leaves the forward direction open")
    void headingScopedDenialIsDirectional() {
        // The old implementation wrote one non-directional value, so this oneway denial closed the road
        // to buses in both directions - the same defect that was fixed for car, bike and foot.
        PropertyScopeContainer backward = mock(PropertyScopeContainer.class);
        when(backward.getHeading()).thenReturn(TravelHeading.BACKWARD);
        when(backward.hasMode()).thenReturn(true);
        when(backward.getMode()).thenReturn(new ArrayList<>(List.of(TravelMode.MOTOR_VEHICLE)));

        OvertureAccessRestriction restriction = mock(OvertureAccessRestriction.class);
        when(restriction.hasAccessType()).thenReturn(true);
        when(restriction.getAccessType()).thenReturn(AccessType.DENIED);
        when(restriction.hasWhen()).thenReturn(true);
        when(restriction.getWhen()).thenReturn(backward);

        when(properties.getAccessRestrictions()).thenReturn(List.of(restriction));

        parse();

        verify(edge).set(accessEnc, true, false);
    }

    @Test
    @DisplayName("A segment closed to everything is closed to buses")
    void inaccessibleSegmentIsClosed() {
        when(segment.isAccessible()).thenReturn(false);

        parse();

        verify(edge).set(accessEnc, false, false);
    }
}
