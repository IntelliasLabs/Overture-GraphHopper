package com.graphhopper.reader.overture.access.restriction.scope;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.graphhopper.reader.overture.access.restriction.AccessType;
import com.graphhopper.reader.overture.access.restriction.OvertureAccessRestriction;
import com.graphhopper.reader.overture.access.restriction.PropertyScopeContainer;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelHeading;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Covers the directional partitioning shared by the access and speed parsers. */
class OvertureScopesTest {

    @Test
    @DisplayName("A rule with no heading applies to both directions")
    void unscopedRuleAppliesBothWays() {
        OvertureAccessRestriction unscoped = restriction(null);

        var byHeading = OvertureScopes.byHeading(List.of(unscoped), OvertureScopes::headingOf);

        assertEquals(List.of(unscoped), byHeading.forward());
        assertEquals(List.of(unscoped), byHeading.backward());
    }

    @Test
    @DisplayName("A scoped rule appears only in its own direction")
    void scopedRuleAppearsOnceOnly() {
        OvertureAccessRestriction forwardOnly = restriction(TravelHeading.FORWARD);
        OvertureAccessRestriction backwardOnly = restriction(TravelHeading.BACKWARD);

        var byHeading =
                OvertureScopes.byHeading(List.of(forwardOnly, backwardOnly), OvertureScopes::headingOf);

        assertEquals(List.of(forwardOnly), byHeading.forward());
        assertEquals(List.of(backwardOnly), byHeading.backward());
    }

    @Test
    @DisplayName("Relative order within each direction is preserved")
    void orderIsPreserved() {
        OvertureAccessRestriction first = restriction(null);
        OvertureAccessRestriction second = restriction(TravelHeading.FORWARD);
        OvertureAccessRestriction third = restriction(null);

        var byHeading =
                OvertureScopes.byHeading(List.of(first, second, third), OvertureScopes::headingOf);

        // Callers resolve conflicts by scanning, so a stable order keeps the outcome predictable.
        assertEquals(List.of(first, second, third), byHeading.forward());
        assertEquals(List.of(first, third), byHeading.backward());
    }

    @Test
    @DisplayName("Null, empty and null-element inputs are handled")
    void degenerateInputsAreSafe() {
        var fromNull = OvertureScopes.byHeading(null, OvertureScopes::headingOf);
        assertTrue(fromNull.forward().isEmpty());
        assertTrue(fromNull.backward().isEmpty());

        var fromEmpty = OvertureScopes.byHeading(List.of(), OvertureScopes::headingOf);
        assertTrue(fromEmpty.forward().isEmpty());

        OvertureAccessRestriction real = restriction(null);
        var withNullElement =
                OvertureScopes.byHeading(Arrays.asList(null, real), OvertureScopes::headingOf);
        assertEquals(List.of(real), withNullElement.forward());
    }

    @Test
    @DisplayName("appliesTo treats an absent heading as unconstrained")
    void appliesToSemantics() {
        assertTrue(OvertureScopes.appliesTo(null, TravelHeading.FORWARD));
        assertTrue(OvertureScopes.appliesTo(null, TravelHeading.BACKWARD));
        assertTrue(OvertureScopes.appliesTo(TravelHeading.FORWARD, TravelHeading.FORWARD));
        assertFalse(OvertureScopes.appliesTo(TravelHeading.FORWARD, TravelHeading.BACKWARD));
    }

    @Test
    @DisplayName("headingOf reports null when the when clause is absent")
    void headingOfHandlesAbsentWhen() {
        assertEquals(null, OvertureScopes.headingOf(restriction(null)));
        assertEquals(
                TravelHeading.FORWARD, OvertureScopes.headingOf(restriction(TravelHeading.FORWARD)));
    }

    private static OvertureAccessRestriction restriction(TravelHeading heading) {
        PropertyScopeContainer when =
                heading == null ? null : new PropertyScopeContainer(null, heading, null, null, null, null);
        return new OvertureAccessRestriction(AccessType.DENIED, when, null);
    }
}
