package com.graphhopper.reader.overture.parsers;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;

import com.graphhopper.reader.overture.access.restriction.AccessType;
import com.graphhopper.reader.overture.access.restriction.OvertureAccessRestriction;
import com.graphhopper.reader.overture.access.restriction.PropertyScopeContainer;
import com.graphhopper.reader.overture.access.restriction.scope.OvertureScopes;
import com.graphhopper.reader.overture.access.restriction.scope.containers.ComparisonOperator;
import com.graphhopper.reader.overture.access.restriction.scope.containers.DimensionRestriction;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelHeading;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelMode;
import com.graphhopper.reader.overture.access.restriction.scope.containers.Units;
import com.graphhopper.reader.overture.access.restriction.scope.containers.VehicleAttributes;
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
     * A denial with no {@code when} clause is the plain "no access" case: a gated service road or
     * private drive. It used to be skipped outright, so bicycles and pedestrians were routed through
     * unconditionally closed roads. Cars escaped it only because the same rule was implemented a
     * second time, correctly, in {@code OvertureRoadSegment.isAccessible}.
     */
    @Test
    void unconditionalDenialClosesTheRoadForEveryMode() {
        OvertureAccessRestriction unconditional =
                new OvertureAccessRestriction(AccessType.DENIED, null, null);

        assertFalse(OvertureAccessParser.isAccessAllowed(List.of(unconditional), "car"));
        assertFalse(OvertureAccessParser.isAccessAllowed(List.of(unconditional), "bicycle"));
        assertFalse(OvertureAccessParser.isAccessAllowed(List.of(unconditional), "foot"));
    }

    /**
     * Verifies that a denial qualified only by vehicle dimensions is not a general closure: it bars
     * oversized vehicles, not all traffic.
     */
    @Test
    void dimensionalDenialIsNotAGeneralClosure() {
        ArrayList<VehicleAttributes> tooTall = new ArrayList<>();
        tooTall.add(new VehicleAttributes(
                DimensionRestriction.HEIGHT, ComparisonOperator.GREATER_THAN, 4.5, Units.M));
        PropertyScopeContainer when = new PropertyScopeContainer(null, null, null, null, null, tooTall);
        OvertureAccessRestriction restriction =
                new OvertureAccessRestriction(AccessType.DENIED, when, null);

        assertTrue(OvertureAccessParser.isAccessAllowed(List.of(restriction), "car"));
        assertTrue(OvertureAccessParser.isAccessAllowed(List.of(restriction), "bicycle"));
    }

    /**
     * Verifies that an allow naming the queried mode lifts a broader denial, so a road closed to
     * vehicles but explicitly opened to bicycles stays routable for bicycles.
     */
    @Test
    void explicitAllowOverridesBroaderDenial() {
        ArrayList<TravelMode> vehicleMode = new ArrayList<>();
        vehicleMode.add(TravelMode.VEHICLE);
        ArrayList<TravelMode> bicycleMode = new ArrayList<>();
        bicycleMode.add(TravelMode.BICYCLE);

        List<OvertureAccessRestriction> restrictions = List.of(
                new OvertureAccessRestriction(
                        AccessType.DENIED, PropertyScopeContainer.ofMode(vehicleMode), null),
                new OvertureAccessRestriction(
                        AccessType.ALLOWED, PropertyScopeContainer.ofMode(bicycleMode), null));

        assertTrue(OvertureAccessParser.isAccessAllowed(restrictions, "bicycle"));
        // Cars are not named by the allow, so the vehicle-wide denial still stands for them.
        assertFalse(OvertureAccessParser.isAccessAllowed(restrictions, "car"));
    }

    /**
     * Verifies the override does not depend on the order restrictions happen to appear in, since
     * Overture defines no ordering over them.
     */
    @Test
    void overrideIsOrderIndependent() {
        ArrayList<TravelMode> vehicleMode = new ArrayList<>();
        vehicleMode.add(TravelMode.VEHICLE);
        ArrayList<TravelMode> bicycleMode = new ArrayList<>();
        bicycleMode.add(TravelMode.BICYCLE);

        OvertureAccessRestriction deny = new OvertureAccessRestriction(
                AccessType.DENIED, PropertyScopeContainer.ofMode(vehicleMode), null);
        OvertureAccessRestriction allow = new OvertureAccessRestriction(
                AccessType.ALLOWED, PropertyScopeContainer.ofMode(bicycleMode), null);

        assertTrue(OvertureAccessParser.isAccessAllowed(List.of(deny, allow), "bicycle"));
        assertTrue(OvertureAccessParser.isAccessAllowed(List.of(allow, deny), "bicycle"));
    }

    /**
     * A oneway is encoded in Overture as a denial scoped to a single heading, and it is by far the
     * most common denial shape in real data. Evaluating the unsplit list with {@link
     * OvertureAccessParser#isAccessAllowed} treats such a denial as a closure of the whole segment,
     * which would shut every oneway in both directions; callers holding the unsplit list must use
     * {@link OvertureAccessParser#isAccessAllowedEitherDirection}.
     */
    @Test
    void onewayIsOpenInAtLeastOneDirection() {
        PropertyScopeContainer backwardOnly =
                new PropertyScopeContainer(null, TravelHeading.BACKWARD, null, null, null, null);
        List<OvertureAccessRestriction> oneway =
                List.of(new OvertureAccessRestriction(AccessType.DENIED, backwardOnly, null));

        assertTrue(OvertureAccessParser.isAccessAllowedEitherDirection(oneway, "car"));

        // ...while the per-direction view still closes the backward side.
        var byHeading = OvertureScopes.byHeading(oneway, OvertureScopes::headingOf);
        assertTrue(OvertureAccessParser.isAccessAllowed(byHeading.forward(), "car"));
        assertFalse(OvertureAccessParser.isAccessAllowed(byHeading.backward(), "car"));
    }

    /** An unconditional denial closes the segment outright, in either-direction terms too. */
    @Test
    void unconditionalDenialClosesBothDirections() {
        List<OvertureAccessRestriction> closed =
                List.of(new OvertureAccessRestriction(AccessType.DENIED, null, null));

        assertFalse(OvertureAccessParser.isAccessAllowedEitherDirection(closed, "car"));
        assertFalse(OvertureAccessParser.isAccessAllowedEitherDirection(closed, "foot"));
    }

    /** Verifies a null list and null entries are tolerated rather than throwing. */
    @Test
    void nullInputsAreTolerated() {
        assertTrue(OvertureAccessParser.isAccessAllowed(null, "car"));

        List<OvertureAccessRestriction> withNull = new ArrayList<>();
        withNull.add(null);
        withNull.add(new OvertureAccessRestriction(AccessType.DENIED, null, null));
        assertFalse(OvertureAccessParser.isAccessAllowed(withNull, "car"));
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
