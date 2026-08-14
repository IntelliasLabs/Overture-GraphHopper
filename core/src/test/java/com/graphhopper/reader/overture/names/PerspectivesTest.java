package com.graphhopper.reader.overture.names;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import org.junit.jupiter.api.Test;

class PerspectivesTest {

    @Test
    void testConstructorAndGetters() {
        Set<String> countries = Set.of("US", "CA");
        Perspectives p = new Perspectives(Mode.ACCEPTED_BY, countries);

        assertEquals(Mode.ACCEPTED_BY, p.getMode());
        assertEquals(countries, p.getCountries());
    }

    @Test
    void testEquals_Reflexive() {
        Perspectives p = new Perspectives(Mode.ACCEPTED_BY, Set.of("DE"));
        assertEquals(p, p, "Object should be equal to itself");
    }

    @Test
    void testEquals_Symmetric() {
        Perspectives p1 = new Perspectives(Mode.DISPUTED_BY, Set.of("UA"));
        Perspectives p2 = new Perspectives(Mode.DISPUTED_BY, Set.of("UA"));

        assertEquals(p1, p2);
        assertEquals(p2, p1);
        assertEquals(p1.hashCode(), p2.hashCode(), "Equal objects must have the same hashCode");
    }

    @Test
    void testNotEquals_DifferentMode() {
        Perspectives p1 = new Perspectives(Mode.ACCEPTED_BY, Set.of("US"));
        Perspectives p2 = new Perspectives(Mode.DISPUTED_BY, Set.of("US"));

        assertNotEquals(p1, p2);
    }

    @Test
    void testNotEquals_DifferentCountries() {
        Perspectives p1 = new Perspectives(Mode.ACCEPTED_BY, Set.of("US"));
        Perspectives p2 = new Perspectives(Mode.ACCEPTED_BY, Set.of("FR"));

        assertNotEquals(p1, p2);
    }

    @Test
    void testNotEquals_NullAndOtherTypes() {
        Perspectives p = new Perspectives(Mode.ACCEPTED_BY, Set.of("US"));

        assertNotEquals(p, null);
        assertNotEquals(p, "Not a Perspectives object");
    }

    @Test
    void testNullFields_Equality() {
        Perspectives p1 = new Perspectives(null, null);
        Perspectives p2 = new Perspectives(null, null);
        Perspectives p3 = new Perspectives(Mode.ACCEPTED_BY, null);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
    }

    @Test
    void testToString() {
        Perspectives p = new Perspectives(Mode.ACCEPTED_BY, Set.of("US"));
        String result = p.toString();

        assertTrue(result.contains("mode=accepted_by"));
        assertTrue(result.contains("countries=[US]"));
    }
}
