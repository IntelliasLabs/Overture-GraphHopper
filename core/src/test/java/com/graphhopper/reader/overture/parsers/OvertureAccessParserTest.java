package com.graphhopper.reader.overture.parsers;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;

import com.graphhopper.reader.overture.access.restriction.AccessType;
import com.graphhopper.reader.overture.access.restriction.OvertureAccessRestriction;
import com.graphhopper.reader.overture.access.restriction.PropertyScopeContainer;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelMode;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link OvertureAccessParser} focusing on mode checking logic
 * and hierarchical mode restrictions.
 */
class OvertureAccessParserTest {

    /**
     * Verifies that when no restrictions are provided, access is allowed by default.
     */
    @Test
    void noRestrictionsReturnsTrue() {
        assertTrue(OvertureAccessParser.isAccessAllowed(emptyList(), "car"));
        assertTrue(OvertureAccessParser.isAccessAllowed(List.of(), "car"));
        assertTrue(OvertureAccessParser.isAccessAllowed(List.of(), "bicycle"));
        assertTrue(OvertureAccessParser.isAccessAllowed(List.of(), "foot"));
    }

    /**
     * Verifies that when a specific mode is explicitly denied, access is not allowed.
     */
    @Test
    void modeDenied() {
        ArrayList<TravelMode> carMode = new ArrayList<>();
        carMode.add(TravelMode.CAR);

        PropertyScopeContainer carDenied = PropertyScopeContainer.ofMode(carMode);
        OvertureAccessRestriction restriction =
                new OvertureAccessRestriction(AccessType.DENIED, carDenied, null);

        assertFalse(OvertureAccessParser.isAccessAllowed(List.of(restriction), "car"));
        assertTrue(OvertureAccessParser.isAccessAllowed(List.of(restriction), "bicycle"));
        assertTrue(OvertureAccessParser.isAccessAllowed(List.of(restriction), "foot"));
    }

    /**
     * Verifies that when a parent mode is denied, all child modes are also denied.
     */
    @Test
    void parentModeDenied() {
        ArrayList<TravelMode> vehicleMode = new ArrayList<>();
        vehicleMode.add(TravelMode.VEHICLE);

        PropertyScopeContainer vehicleDenied = PropertyScopeContainer.ofMode(vehicleMode);
        OvertureAccessRestriction restriction =
                new OvertureAccessRestriction(AccessType.DENIED, vehicleDenied, null);

        // All vehicle types should be denied
        assertFalse(OvertureAccessParser.isAccessAllowed(List.of(restriction), "car"));
        assertFalse(OvertureAccessParser.isAccessAllowed(List.of(restriction), "bicycle"));
        assertFalse(OvertureAccessParser.isAccessAllowed(List.of(restriction), "motorcycle"));
        assertFalse(OvertureAccessParser.isAccessAllowed(List.of(restriction), "hgv"));

        // Foot should still be allowed (not a vehicle)
        assertTrue(OvertureAccessParser.isAccessAllowed(List.of(restriction), "foot"));
    }

    /**
     * Verifies that when motor_vehicle is denied, only motor vehicles are denied,
     * not non-motorized vehicles.
     */
    @Test
    void motorVehicleDenied() {
        ArrayList<TravelMode> motorVehicleMode = new ArrayList<>();
        motorVehicleMode.add(TravelMode.MOTOR_VEHICLE);

        PropertyScopeContainer motorVehicleDenied = PropertyScopeContainer.ofMode(motorVehicleMode);
        OvertureAccessRestriction restriction =
                new OvertureAccessRestriction(AccessType.DENIED, motorVehicleDenied, null);

        // Motor vehicles should be denied
        assertFalse(OvertureAccessParser.isAccessAllowed(List.of(restriction), "car"));
        assertFalse(OvertureAccessParser.isAccessAllowed(List.of(restriction), "motorcycle"));
        assertFalse(OvertureAccessParser.isAccessAllowed(List.of(restriction), "hgv"));
        assertFalse(OvertureAccessParser.isAccessAllowed(List.of(restriction), "bus"));

        // Non-motor vehicles should be allowed
        assertTrue(OvertureAccessParser.isAccessAllowed(List.of(restriction), "bicycle"));
        assertTrue(OvertureAccessParser.isAccessAllowed(List.of(restriction), "foot"));
    }

    /**
     * Verifies that when a different mode is restricted, the queried mode remains allowed.
     */
    @Test
    void otherModeRestrictedReturnsTrue() {
        ArrayList<TravelMode> bicycleMode = new ArrayList<>();
        bicycleMode.add(TravelMode.BICYCLE);

        PropertyScopeContainer bicycleDenied = PropertyScopeContainer.ofMode(bicycleMode);
        OvertureAccessRestriction restriction =
                new OvertureAccessRestriction(AccessType.DENIED, bicycleDenied, null);

        assertFalse(OvertureAccessParser.isAccessAllowed(List.of(restriction), "bicycle"));
        assertTrue(OvertureAccessParser.isAccessAllowed(List.of(restriction), "car"));
        assertTrue(OvertureAccessParser.isAccessAllowed(List.of(restriction), "foot"));
    }
}
