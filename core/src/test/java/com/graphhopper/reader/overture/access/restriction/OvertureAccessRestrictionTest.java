package com.graphhopper.reader.overture.access.restriction;

import static org.junit.jupiter.api.Assertions.*;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelHeading;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link OvertureAccessRestriction} focusing on constructor behavior, accessors,
 * {@code has*} helpers, equality and direction checks.
 */
class OvertureAccessRestrictionTest {

    /**
     * Verifies that the constructor stores the given fields and that the getters return them.
     */
    @Test
    void constructorStoresFieldsAndGettersReturnThem() {
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.1, 0.9);
        PropertyScopeContainer rules = new PropertyScopeContainer("10:00-18:00", TravelHeading.FORWARD);

        OvertureAccessRestriction restriction =
                new OvertureAccessRestriction(AccessType.ALLOWED, rules, range);

        assertEquals(AccessType.ALLOWED, restriction.getAccessType());
        assertSame(rules, restriction.getWhen());
        assertSame(range, restriction.getBetween());
    }

    /**
     * Verifies that {@code has*} helper methods reflect presence or absence of values.
     */
    @Test
    void hasMethodsReflectPresenceOfValues() {
        OvertureAccessRestriction withAll = new OvertureAccessRestriction(
                AccessType.ALLOWED,
                new PropertyScopeContainer("10:00-18:00", TravelHeading.FORWARD),
                new LinearlyReferencedRange(0.0, 1.0));
        assertTrue(withAll.hasAccessType());
        assertTrue(withAll.hasWhen());
        assertTrue(withAll.hasBetween());

        OvertureAccessRestriction withNone = new OvertureAccessRestriction(null, null, null);
        assertFalse(withNone.hasAccessType());
        assertFalse(withNone.hasWhen());
        assertFalse(withNone.hasBetween());
    }

    /**
     * Verifies that {@link OvertureAccessRestriction#equals(Object)} and
     * {@link OvertureAccessRestriction#hashCode()} use all fields.
     */
    @Test
    void equalsAndHashCodeUseAllFields() {
        LinearlyReferencedRange range1 = new LinearlyReferencedRange(0.0, 0.5);
        PropertyScopeContainer rules1 =
                new PropertyScopeContainer("10:00-18:00", TravelHeading.FORWARD);

        OvertureAccessRestriction r1 =
                new OvertureAccessRestriction(AccessType.ALLOWED, rules1, range1);
        OvertureAccessRestriction r2 =
                new OvertureAccessRestriction(AccessType.ALLOWED, rules1, range1);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());

        OvertureAccessRestriction r3 = new OvertureAccessRestriction(AccessType.DENIED, rules1, range1);
        assertNotEquals(r1, r3);
    }

    /**
     * Checks that the {@code toString()} representation contains the key fields.
     */
    @Test
    void toStringContainsKeyFields() {
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.0, 1.0);
        PropertyScopeContainer rules = new PropertyScopeContainer("10:00-18:00", TravelHeading.FORWARD);

        OvertureAccessRestriction restriction =
                new OvertureAccessRestriction(AccessType.ALLOWED, rules, range);
        String s = restriction.toString();
        assertTrue(s.contains("accessType=allowed"));
        assertTrue(s.contains("accessCondition="));
        assertTrue(s.contains("between="));
    }

    /**
     * Verifies that when access is allowed and the heading is {@link TravelHeading#FORWARD},
     * only forward travel is allowed.
     */
    @Test
    void isAllowedForwardWhenHeadingIsForward() {
        PropertyScopeContainer forwardRules =
                new PropertyScopeContainer("10:00-18:00", TravelHeading.FORWARD);
        OvertureAccessRestriction allowedForward =
                new OvertureAccessRestriction(AccessType.ALLOWED, forwardRules, null);

        assertTrue(allowedForward.isAllowedForward());
        assertFalse(allowedForward.isAllowedBackward());
    }

    /**
     * Verifies that when access is allowed and the heading is {@link TravelHeading#BACKWARD},
     * only backward travel is allowed.
     */
    @Test
    void isAllowedBackwardWhenHeadingIsBackward() {
        PropertyScopeContainer backwardRules =
                new PropertyScopeContainer("10:00-18:00", TravelHeading.BACKWARD);
        OvertureAccessRestriction allowedBackward =
                new OvertureAccessRestriction(AccessType.ALLOWED, backwardRules, null);

        assertFalse(allowedBackward.isAllowedForward());
        assertTrue(allowedBackward.isAllowedBackward());
    }

    /**
     * Verifies that when access is allowed and no travel heading is specified, access is allowed
     * in both directions.
     */
    @Test
    void isAllowedInBothDirectionsWhenHeadingIsNull() {
        PropertyScopeContainer noHeadingRules = new PropertyScopeContainer("10:00-18:00", null);
        OvertureAccessRestriction allowedNoHeading =
                new OvertureAccessRestriction(AccessType.ALLOWED, noHeadingRules, null);

        assertTrue(allowedNoHeading.isAllowedForward());
        assertTrue(allowedNoHeading.isAllowedBackward());
    }

    /**
     * Verifies that {@link OvertureAccessRestriction#ofAccessType(AccessType)} creates a
     * restriction with only the access type set.
     */
    @Test
    void ofAccessTypeCreatesRestrictionWithOnlyAccessTypeSet() {
        OvertureAccessRestriction restriction =
                OvertureAccessRestriction.ofAccessType(AccessType.ALLOWED);

        assertEquals(AccessType.ALLOWED, restriction.getAccessType());
        assertNull(restriction.getWhen());
        assertNull(restriction.getBetween());

        assertTrue(restriction.hasAccessType());
        assertFalse(restriction.hasWhen());
        assertFalse(restriction.hasBetween());
    }

    /**
     * Verifies that {@link OvertureAccessRestriction#ofWhen(PropertyScopeContainer)} creates a
     * restriction with only the {@code when} conditions set.
     */
    @Test
    void ofWhenCreatesRestrictionWithOnlyWhenSet() {
        PropertyScopeContainer when = new PropertyScopeContainer("10:00-18:00", TravelHeading.FORWARD);

        OvertureAccessRestriction restriction = OvertureAccessRestriction.ofWhen(when);

        assertNull(restriction.getAccessType());
        assertSame(when, restriction.getWhen());
        assertNull(restriction.getBetween());

        assertFalse(restriction.hasAccessType());
        assertTrue(restriction.hasWhen());
        assertFalse(restriction.hasBetween());
    }

    /**
     * Verifies that {@link OvertureAccessRestriction#ofBetween(LinearlyReferencedRange)} creates a
     * restriction with only the linear range set.
     */
    @Test
    void ofBetweenCreatesRestrictionWithOnlyBetweenSet() {
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.2, 0.8);

        OvertureAccessRestriction restriction = OvertureAccessRestriction.ofBetween(range);

        assertNull(restriction.getAccessType());
        assertNull(restriction.getWhen());
        assertSame(range, restriction.getBetween());

        assertFalse(restriction.hasAccessType());
        assertFalse(restriction.hasWhen());
        assertTrue(restriction.hasBetween());
    }
}
