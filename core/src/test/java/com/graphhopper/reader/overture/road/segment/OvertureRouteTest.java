package com.graphhopper.reader.overture.road.segment;

import static org.junit.jupiter.api.Assertions.*;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import org.junit.jupiter.api.Test;

class OvertureRouteTest {

    @Test
    void testConstructorAndGetters() {
        String name = "Pacific Coast Highway";
        String network = "US:CA";
        String ref = "1";
        String symbol = "https://example.com/hwy1.png";
        String wikidata = "Q494573";
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.1, 0.9);

        OvertureRoute route = new OvertureRoute(name, network, ref, symbol, wikidata, range);

        assertEquals(name, route.getName());
        assertEquals(network, route.getNetwork());
        assertEquals(ref, route.getRef());
        assertEquals(symbol, route.getSymbol());
        assertEquals(wikidata, route.getWikidata());
        assertEquals(range, route.getBetween());
    }

    @Test
    void testConstructorWithNulls() {
        // Most fields are optional in the schema except where logic dictates otherwise
        OvertureRoute route = new OvertureRoute(null, "US:I", "5", null, null, null);

        assertNull(route.getName());
        assertEquals("US:I", route.getNetwork());
        assertEquals("5", route.getRef());
        assertNull(route.getSymbol());
        assertNull(route.getWikidata());
        assertNull(route.getBetween());
    }

    @Test
    void testEqualsAndHashCode() {
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.0, 1.0);

        OvertureRoute r1 = new OvertureRoute("Route 66", "US:US", "66", "sym", "wd", range);
        OvertureRoute r2 = new OvertureRoute("Route 66", "US:US", "66", "sym", "wd", range);
        OvertureRoute r3 = new OvertureRoute("I-5", "US:I", "5", "sym", "wd", null);

        // Reflexive
        assertEquals(r1, r1);

        // Symmetric
        assertEquals(r1, r2);
        assertEquals(r2, r1);
        assertEquals(r1.hashCode(), r2.hashCode());

        // Not equal
        assertNotEquals(r1, r3);
        assertNotEquals(null, r1);
        assertNotEquals("not a route", r1);
    }

    @Test
    void testNotEqualsDifferentRange() {
        OvertureRoute r1 =
                new OvertureRoute("Name", "Net", "Ref", null, null, new LinearlyReferencedRange(0.0, 0.5));
        OvertureRoute r2 =
                new OvertureRoute("Name", "Net", "Ref", null, null, new LinearlyReferencedRange(0.5, 1.0));

        assertNotEquals(r1, r2);
    }

    @Test
    void testNotEqualsOneNullRange() {
        OvertureRoute r1 =
                new OvertureRoute("Name", "Net", "Ref", null, null, new LinearlyReferencedRange(0.0, 1.0));
        OvertureRoute r2 = new OvertureRoute("Name", "Net", "Ref", null, null, null);

        assertNotEquals(r1, r2);
    }
}
