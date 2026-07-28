package com.graphhopper.reader.overture.access.restriction;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;

import com.graphhopper.reader.overture.access.restriction.scope.containers.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PropertyScopeContainer} covering constructor behavior, field accessors,
 * has-methods, equals/hashCode and toString for the list-based API.
 */
class PropertyScopeContainerTest {

    @Test
    void constructorStoresGivenProperties() {
        ArrayList<TravelReason> reasons = new ArrayList<>();
        reasons.add(TravelReason.AS_CUSTOMER);
        ArrayList<RecognizedStatus> recognized = new ArrayList<>();
        recognized.add(RecognizedStatus.AS_PERMITTED);
        ArrayList<TravelMode> modes = new ArrayList<>();
        modes.add(TravelMode.CAR);
        ArrayList<VehicleAttributes> vehicles = new ArrayList<>();
        vehicles.add(new VehicleAttributes(
                DimensionRestriction.HEIGHT, ComparisonOperator.LESS_THAN, 3.5, Units.M));

        PropertyScopeContainer rules = new PropertyScopeContainer(
                "10:00-18:00", TravelHeading.FORWARD, reasons, recognized, modes, vehicles);

        assertEquals("10:00-18:00", rules.getDuring());
        assertEquals(TravelHeading.FORWARD, rules.getHeading());
        assertEquals(reasons, rules.getUsing());
        assertEquals(recognized, rules.getRecognized());
        assertEquals(modes, rules.getMode());
        assertEquals(vehicles, rules.getVehicle());

        assertTrue(rules.hasDuring());
        assertTrue(rules.hasHeading());
        assertTrue(rules.hasUsing());
        assertTrue(rules.hasRecognized());
        assertTrue(rules.hasMode());
        assertTrue(rules.hasVehicle());
    }

    @Test
    void hasMethodsReflectNullAndEmptyCollections() {
        List<TravelReason> reasons = emptyList();
        List<RecognizedStatus> recognized = emptyList();
        List<TravelMode> modes = new ArrayList<>();
        modes.add(TravelMode.BUS);
        List<VehicleAttributes> vehicles = emptyList();

        PropertyScopeContainer rules =
                new PropertyScopeContainer(null, null, reasons, recognized, modes, vehicles);

        assertFalse(rules.hasDuring());
        assertFalse(rules.hasHeading());

        // empty list -> hasUsing() is false
        assertEquals(emptyList(), rules.getUsing());
        assertFalse(rules.hasUsing());

        // empty list -> hasRecognized() is false
        assertNotNull(rules.getRecognized());
        assertFalse(rules.hasRecognized());

        // non-empty list -> hasMode() is true
        assertEquals(Collections.singletonList(TravelMode.BUS), rules.getMode());
        assertTrue(rules.hasMode());

        // empty list -> hasVehicle() is false
        assertNotNull(rules.getVehicle());
        assertFalse(rules.hasVehicle());
    }

    @Test
    void overloadedConstructorsPopulateExpectedFields() {
        ArrayList<TravelReason> reasons = new ArrayList<>();
        reasons.add(TravelReason.AT_DESTINATION);
        ArrayList<RecognizedStatus> recognized = new ArrayList<>();
        recognized.add(RecognizedStatus.AS_EMPLOYEE);
        ArrayList<TravelMode> modes = new ArrayList<>();
        modes.add(TravelMode.CAR);

        PropertyScopeContainer withHeadingOnly =
                new PropertyScopeContainer("10:00-18:00", TravelHeading.FORWARD);
        assertEquals("10:00-18:00", withHeadingOnly.getDuring());
        assertEquals(TravelHeading.FORWARD, withHeadingOnly.getHeading());
        assertFalse(withHeadingOnly.hasUsing());
        assertFalse(withHeadingOnly.hasRecognized());
        assertFalse(withHeadingOnly.hasMode());
        assertFalse(withHeadingOnly.hasVehicle());

        PropertyScopeContainer withHeadingAndReasons =
                new PropertyScopeContainer("10:00-18:00", TravelHeading.BACKWARD, reasons);
        assertEquals(
                Collections.singletonList(TravelReason.AT_DESTINATION), withHeadingAndReasons.getUsing());
        assertTrue(withHeadingAndReasons.hasUsing());

        PropertyScopeContainer withRecognized =
                new PropertyScopeContainer("10:00-18:00", TravelHeading.FORWARD, reasons, recognized);
        assertEquals(
                Collections.singletonList(RecognizedStatus.AS_EMPLOYEE), withRecognized.getRecognized());
        assertTrue(withRecognized.hasRecognized());

        PropertyScopeContainer withMode = new PropertyScopeContainer(
                "10:00-18:00", TravelHeading.FORWARD, reasons, recognized, modes);
        assertEquals(Collections.singletonList(TravelMode.CAR), withMode.getMode());
        assertTrue(withMode.hasMode());
    }

    @Test
    void equalsAndHashCodeUseAllFields() {
        ArrayList<TravelReason> reasons = new ArrayList<>();
        reasons.add(TravelReason.AS_CUSTOMER);
        ArrayList<RecognizedStatus> recognized = new ArrayList<>();
        recognized.add(RecognizedStatus.AS_PERMITTED);
        ArrayList<TravelMode> modes = new ArrayList<>();
        modes.add(TravelMode.CAR);
        ArrayList<VehicleAttributes> vehicles = new ArrayList<>();
        vehicles.add(new VehicleAttributes(
                DimensionRestriction.WEIGHT, ComparisonOperator.LESS_THAN_EQUAL, 7_500.0, null));

        PropertyScopeContainer r1 = new PropertyScopeContainer(
                "10:00-18:00", TravelHeading.FORWARD, reasons, recognized, modes, vehicles);
        PropertyScopeContainer r2 = new PropertyScopeContainer(
                "10:00-18:00", TravelHeading.FORWARD, reasons, recognized, modes, vehicles);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());

        PropertyScopeContainer r3 = new PropertyScopeContainer(
                "11:00-19:00", TravelHeading.FORWARD, reasons, recognized, modes, vehicles);
        assertNotEquals(r1, r3);
    }

    @Test
    void toStringContainsKeyFields() {
        PropertyScopeContainer rules = new PropertyScopeContainer("10:00-18:00", TravelHeading.FORWARD);
        String s = rules.toString();
        assertTrue(s.contains("during='10:00-18:00'"));
        assertTrue(s.contains("heading=FORWARD") || s.contains("heading=forward"));
    }

    @Test
    void ofDuringCreatesContainerWithOnlyDuringSet() {
        PropertyScopeContainer rules = PropertyScopeContainer.ofDuring("10:00-18:00");

        assertEquals("10:00-18:00", rules.getDuring());
        assertTrue(rules.hasDuring());
        assertFalse(rules.hasHeading());
        assertFalse(rules.hasUsing());
        assertFalse(rules.hasRecognized());
        assertFalse(rules.hasMode());
        assertFalse(rules.hasVehicle());
    }

    @Test
    void ofHeadingCreatesContainerWithOnlyHeadingSet() {
        PropertyScopeContainer rules = PropertyScopeContainer.ofHeading(TravelHeading.FORWARD);

        assertNull(rules.getDuring());
        assertEquals(TravelHeading.FORWARD, rules.getHeading());
        assertFalse(rules.hasDuring());
        assertTrue(rules.hasHeading());
        assertFalse(rules.hasUsing());
        assertFalse(rules.hasRecognized());
        assertFalse(rules.hasMode());
        assertFalse(rules.hasVehicle());
    }

    @Test
    void ofUsingCreatesContainerWithOnlyUsingSet() {
        ArrayList<TravelReason> reasons = new ArrayList<>();
        reasons.add(TravelReason.AS_CUSTOMER);

        PropertyScopeContainer rules = PropertyScopeContainer.ofUsing(reasons);

        assertNull(rules.getDuring());
        assertNull(rules.getHeading());
        assertEquals(Collections.singletonList(TravelReason.AS_CUSTOMER), rules.getUsing());
        assertFalse(rules.hasDuring());
        assertFalse(rules.hasHeading());
        assertTrue(rules.hasUsing());
        assertFalse(rules.hasRecognized());
        assertFalse(rules.hasMode());
        assertFalse(rules.hasVehicle());
    }

    @Test
    void ofRecognizedCreatesContainerWithOnlyRecognizedSet() {
        ArrayList<RecognizedStatus> recognized = new ArrayList<>();
        recognized.add(RecognizedStatus.AS_EMPLOYEE);

        PropertyScopeContainer rules = PropertyScopeContainer.ofRecognized(recognized);

        assertNull(rules.getDuring());
        assertNull(rules.getHeading());
        assertNotNull(rules.getUsing());
        assertEquals(Collections.singletonList(RecognizedStatus.AS_EMPLOYEE), rules.getRecognized());
        assertFalse(rules.hasDuring());
        assertFalse(rules.hasHeading());
        assertFalse(rules.hasUsing());
        assertTrue(rules.hasRecognized());
        assertFalse(rules.hasMode());
        assertFalse(rules.hasVehicle());
    }

    @Test
    void ofModeCreatesContainerWithOnlyModeSet() {
        ArrayList<TravelMode> modes = new ArrayList<>();
        modes.add(TravelMode.BUS);

        PropertyScopeContainer rules = PropertyScopeContainer.ofMode(modes);

        assertNull(rules.getDuring());
        assertNull(rules.getHeading());
        assertNotNull(rules.getUsing());
        assertNotNull(rules.getRecognized());
        assertEquals(Collections.singletonList(TravelMode.BUS), rules.getMode());
        assertFalse(rules.hasDuring());
        assertFalse(rules.hasHeading());
        assertFalse(rules.hasUsing());
        assertFalse(rules.hasRecognized());
        assertTrue(rules.hasMode());
        assertFalse(rules.hasVehicle());
    }

    @Test
    void ofVehicleCreatesContainerWithOnlyVehicleSet() {
        VehicleAttributes vehicleAttributes = new VehicleAttributes(
                DimensionRestriction.HEIGHT, ComparisonOperator.LESS_THAN, 3.5, Units.M);
        ArrayList<VehicleAttributes> vehicles = new ArrayList<>();
        vehicles.add(vehicleAttributes);

        PropertyScopeContainer rules = PropertyScopeContainer.ofVehicle(vehicles);

        assertNull(rules.getDuring());
        assertNull(rules.getHeading());
        assertNotNull(rules.getUsing());
        assertNotNull(rules.getRecognized());
        assertNotNull(rules.getMode());
        assertEquals(Collections.singletonList(vehicleAttributes), rules.getVehicle());
        assertFalse(rules.hasDuring());
        assertFalse(rules.hasHeading());
        assertFalse(rules.hasUsing());
        assertFalse(rules.hasRecognized());
        assertFalse(rules.hasMode());
        assertTrue(rules.hasVehicle());
    }
}
