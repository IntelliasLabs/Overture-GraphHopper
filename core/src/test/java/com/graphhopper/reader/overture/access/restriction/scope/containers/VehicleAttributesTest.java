package com.graphhopper.reader.overture.access.restriction.scope.containers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link VehicleAttributes} verifying constructor behavior, helper methods and
 * equality.
 */
class VehicleAttributesTest {

    /**
     * Verifies that the constructor stores all provided fields and the getters return them.
     */
    @Test
    void constructorStoresFieldsAndGettersReturnThem() {
        VehicleAttributes attrs = new VehicleAttributes(
                DimensionRestriction.HEIGHT, ComparisonOperator.LESS_THAN, 3.5, Units.M);

        assertEquals(DimensionRestriction.HEIGHT, attrs.getDimension());
        assertEquals(ComparisonOperator.LESS_THAN, attrs.getComparison());
        assertEquals(3.5, attrs.getNumericQuantity());
        assertEquals(Units.M, attrs.getLengthUnits());
    }

    /**
     * Verifies that {@code has*} helper methods reflect which values are present.
     */
    @Test
    void hasMethodsReflectPresenceOfValues() {
        VehicleAttributes attrs = new VehicleAttributes(DimensionRestriction.HEIGHT, null, null, null);

        assertTrue(attrs.hasDimensionRestriction());
        assertFalse(attrs.hasComparisonOperator());
        assertFalse(attrs.hasNumericQuantity());
        assertFalse(attrs.hasLengthUnits());
    }

    /**
     * Verifies that {@link VehicleAttributes#equals(Object)} and {@link VehicleAttributes#hashCode()}
     * use all fields.
     */
    @Test
    void equalsAndHashCodeUseAllFields() {
        VehicleAttributes a1 = new VehicleAttributes(
                DimensionRestriction.WEIGHT, ComparisonOperator.GREATER_THAN, 12_000.0, null);
        VehicleAttributes a2 = new VehicleAttributes(
                DimensionRestriction.WEIGHT, ComparisonOperator.GREATER_THAN, 12_000.0, null);
        VehicleAttributes a3 = new VehicleAttributes(
                DimensionRestriction.WEIGHT, ComparisonOperator.GREATER_THAN, 13_000.0, null);

        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
        assertNotEquals(a1, a3);
    }

    /**
     * Checks that {@code toString()} contains key fields useful for debugging.
     */
    @Test
    void toStringContainsKeyFields() {
        VehicleAttributes attrs = new VehicleAttributes(
                DimensionRestriction.LENGTH, ComparisonOperator.LESS_THAN_EQUAL, 10.0, Units.M);
        String s = attrs.toString();
        assertTrue(s.contains("dimension="));
        assertTrue(s.contains("comparison=less_than_equal"));
    }

    /**
     * Verifies that {@link VehicleAttributes#ofDimensionRestriction(DimensionRestriction)} creates
     * attributes with only the dimension restriction set.
     */
    @Test
    void ofDimensionRestrictionCreatesAttributesWithOnlyDimensionSet() {
        VehicleAttributes attrs = VehicleAttributes.ofDimensionRestriction(DimensionRestriction.HEIGHT);

        assertEquals(DimensionRestriction.HEIGHT, attrs.getDimension());
        assertNull(attrs.getComparison());
        assertNull(attrs.getNumericQuantity());
        assertNull(attrs.getLengthUnits());

        assertTrue(attrs.hasDimensionRestriction());
        assertFalse(attrs.hasComparisonOperator());
        assertFalse(attrs.hasNumericQuantity());
        assertFalse(attrs.hasLengthUnits());
    }

    /**
     * Verifies that {@link VehicleAttributes#ofComparisonOperator(ComparisonOperator)} creates
     * attributes with only the comparison operator set.
     */
    @Test
    void ofComparisonOperatorCreatesAttributesWithOnlyComparisonSet() {
        VehicleAttributes attrs =
                VehicleAttributes.ofComparisonOperator(ComparisonOperator.GREATER_THAN);

        assertNull(attrs.getDimension());
        assertEquals(ComparisonOperator.GREATER_THAN, attrs.getComparison());
        assertNull(attrs.getNumericQuantity());
        assertNull(attrs.getLengthUnits());

        assertFalse(attrs.hasDimensionRestriction());
        assertTrue(attrs.hasComparisonOperator());
        assertFalse(attrs.hasNumericQuantity());
        assertFalse(attrs.hasLengthUnits());
    }

    /**
     * Verifies that {@link VehicleAttributes#ofNumericQuantity(Double)} creates attributes with
     * only the numeric quantity set.
     */
    @Test
    void ofNumericQuantityCreatesAttributesWithOnlyQuantitySet() {
        VehicleAttributes attrs = VehicleAttributes.ofNumericQuantity(3.5);

        assertNull(attrs.getDimension());
        assertNull(attrs.getComparison());
        assertEquals(3.5, attrs.getNumericQuantity());
        assertNull(attrs.getLengthUnits());

        assertFalse(attrs.hasDimensionRestriction());
        assertFalse(attrs.hasComparisonOperator());
        assertTrue(attrs.hasNumericQuantity());
        assertFalse(attrs.hasLengthUnits());
    }

    /**
     * Verifies that {@link VehicleAttributes#ofUnits(Units)} creates attributes with only the
     * units set.
     */
    @Test
    void ofUnitsCreatesAttributesWithOnlyUnitsSet() {
        VehicleAttributes attrs = VehicleAttributes.ofUnits(Units.M);

        assertNull(attrs.getDimension());
        assertNull(attrs.getComparison());
        assertNull(attrs.getNumericQuantity());
        assertEquals(Units.M, attrs.getLengthUnits());

        assertFalse(attrs.hasDimensionRestriction());
        assertFalse(attrs.hasComparisonOperator());
        assertFalse(attrs.hasNumericQuantity());
        assertTrue(attrs.hasLengthUnits());
    }
}
