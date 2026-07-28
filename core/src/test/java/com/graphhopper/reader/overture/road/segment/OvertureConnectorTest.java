package com.graphhopper.reader.overture.road.segment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OvertureConnectorTest {

    @Test
    void testConstructorAndGetters() {
        String expectedId = "gers:12345";
        double expectedAt = 0.5;

        OvertureConnector connector = new OvertureConnector(expectedId, expectedAt);

        assertEquals(
                expectedId, connector.getConnectorId(), "Connector ID should match constructor input");
        assertEquals(
                expectedAt, connector.getAt(), 0.00001, "At position should match constructor input");
    }

    @Test
    void testEqualsReflexive() {
        OvertureConnector connector = new OvertureConnector("gers:1", 0.1);
        assertEquals(connector, connector, "An object should be equal to itself");
    }

    @Test
    void testEqualsSymmetric() {
        OvertureConnector c1 = new OvertureConnector("gers:1", 0.2);
        OvertureConnector c2 = new OvertureConnector("gers:1", 0.2);

        assertEquals(c1, c2, "Objects with same content should be equal");
        assertEquals(c2, c1, "Equality should be symmetric");
    }

    @Test
    void testNotEqualsDifferentId() {
        OvertureConnector c1 = new OvertureConnector("gers:A", 0.5);
        OvertureConnector c2 = new OvertureConnector("gers:B", 0.5);

        assertNotEquals(c1, c2, "Objects with different IDs should not be equal");
    }

    @Test
    void testNotEqualsDifferentAt() {
        OvertureConnector c1 = new OvertureConnector("gers:A", 0.1);
        OvertureConnector c2 = new OvertureConnector("gers:A", 0.9);

        assertNotEquals(c1, c2, "Objects with different 'at' positions should not be equal");
    }

    @Test
    void testNotEqualsNull() {
        OvertureConnector c1 = new OvertureConnector("gers:1", 0.5);
        assertNotEquals(null, c1, "Object should not be equal to null");
    }

    @Test
    void testNotEqualsDifferentClass() {
        OvertureConnector c1 = new OvertureConnector("gers:1", 0.5);
        assertNotEquals(
                "Some String", c1, "Object should not be equal to an instance of a different class");
    }

    @Test
    void testHashCode() {
        OvertureConnector c1 = new OvertureConnector("gers:X", 0.33);
        OvertureConnector c2 = new OvertureConnector("gers:X", 0.33);

        assertEquals(c1.hashCode(), c2.hashCode(), "Equal objects must have the same hash code");

        // Sanity check: verify hash codes differ for different objects (not strictly required by
        // contract but good for distribution)
        OvertureConnector c3 = new OvertureConnector("gers:Y", 0.33);
        assertNotEquals(c1.hashCode(), c3.hashCode());
    }

    @Test
    void testToString() {
        OvertureConnector connector = new OvertureConnector("gers:test", 0.75);
        String stringRep = connector.toString();

        assertTrue(stringRep.contains("gers:test"));
        assertTrue(stringRep.contains("0.75"));
        assertTrue(stringRep.startsWith("OvertureConnector{"));
    }
}
