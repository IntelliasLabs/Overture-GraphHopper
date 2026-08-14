package com.graphhopper.reader.overture.road.segment.destination;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OvertureDestinationLabelTest {

    @Test
    void testConstructorAndGetters() {
        OvertureDestinationLabel label = new OvertureDestinationLabel("Berlin", OvertureDestinationLabelType.COUNTRY);

        assertEquals("Berlin", label.getValue());
        assertEquals(OvertureDestinationLabelType.COUNTRY, label.getType());
    }

    @Test
    void testConstructorWithRouteRef() {
        OvertureDestinationLabel label = new OvertureDestinationLabel("I-95", OvertureDestinationLabelType.ROUTE_REF);

        assertEquals("I-95", label.getValue());
        assertEquals(OvertureDestinationLabelType.ROUTE_REF, label.getType());
    }

    @Test
    void testConstructorWithNullValue() {
        OvertureDestinationLabel label = new OvertureDestinationLabel(null, OvertureDestinationLabelType.STREET);

        assertNull(label.getValue());
        assertEquals(OvertureDestinationLabelType.STREET, label.getType());
    }

    @Test
    void testPublicFields() {
        OvertureDestinationLabel label = new OvertureDestinationLabel("Main Street", OvertureDestinationLabelType.STREET);

        assertEquals("Main Street", label.value);
        assertEquals(OvertureDestinationLabelType.STREET, label.type);
    }

    @Test
    void testEqualsWithEqualObjects() {
        OvertureDestinationLabel label1 = new OvertureDestinationLabel("Berlin", OvertureDestinationLabelType.COUNTRY);
        OvertureDestinationLabel label2 = new OvertureDestinationLabel("Berlin", OvertureDestinationLabelType.COUNTRY);

        assertEquals(label1, label2);
        assertEquals(label2, label1);
    }

    @Test
    void testEqualsWithDifferentValue() {
        OvertureDestinationLabel label1 = new OvertureDestinationLabel("Berlin", OvertureDestinationLabelType.COUNTRY);
        OvertureDestinationLabel label2 = new OvertureDestinationLabel("Paris", OvertureDestinationLabelType.COUNTRY);

        assertNotEquals(label1, label2);
    }

    @Test
    void testEqualsWithDifferentType() {
        OvertureDestinationLabel label1 = new OvertureDestinationLabel("Berlin", OvertureDestinationLabelType.COUNTRY);
        OvertureDestinationLabel label2 = new OvertureDestinationLabel("Berlin", OvertureDestinationLabelType.STREET);

        assertNotEquals(label1, label2);
    }

    @Test
    void testEqualsWithNull() {
        OvertureDestinationLabel label = new OvertureDestinationLabel("Berlin", OvertureDestinationLabelType.COUNTRY);

        assertNotEquals(null, label);
    }

    @Test
    void testEqualsWithDifferentClass() {
        OvertureDestinationLabel label = new OvertureDestinationLabel("Berlin", OvertureDestinationLabelType.COUNTRY);

        assertNotEquals("Berlin", label);
    }

    @Test
    void testEqualsWithBothNullValues() {
        OvertureDestinationLabel label1 = new OvertureDestinationLabel(null, OvertureDestinationLabelType.STREET);
        OvertureDestinationLabel label2 = new OvertureDestinationLabel(null, OvertureDestinationLabelType.STREET);

        assertEquals(label1, label2);
    }

    @Test
    void testHashCodeConsistency() {
        OvertureDestinationLabel label = new OvertureDestinationLabel("Berlin", OvertureDestinationLabelType.COUNTRY);

        int hash1 = label.hashCode();
        int hash2 = label.hashCode();

        assertEquals(hash1, hash2);
    }

    @Test
    void testHashCodeEqualObjects() {
        OvertureDestinationLabel label1 = new OvertureDestinationLabel("Berlin", OvertureDestinationLabelType.COUNTRY);
        OvertureDestinationLabel label2 = new OvertureDestinationLabel("Berlin", OvertureDestinationLabelType.COUNTRY);

        assertEquals(label1.hashCode(), label2.hashCode());
    }

    @Test
    void testHashCodeDifferentObjects() {
        OvertureDestinationLabel label1 = new OvertureDestinationLabel("Berlin", OvertureDestinationLabelType.COUNTRY);
        OvertureDestinationLabel label2 = new OvertureDestinationLabel("Paris", OvertureDestinationLabelType.COUNTRY);

        assertNotEquals(label1.hashCode(), label2.hashCode());
    }

    @Test
    void testToString() {
        OvertureDestinationLabel label = new OvertureDestinationLabel("Berlin", OvertureDestinationLabelType.COUNTRY);

        String result = label.toString();

        assertTrue(result.contains("Berlin"));
        assertTrue(result.contains("country"));
        assertTrue(result.contains("OvertureDestinationLabel"));
    }

    @Test
    void testToStringWithRouteRef() {
        OvertureDestinationLabel label = new OvertureDestinationLabel("I-95", OvertureDestinationLabelType.ROUTE_REF);

        String result = label.toString();

        assertEquals("OvertureDestinationLabel{value='I-95', type=route_ref}", result);
    }

    @Test
    void testToStringWithNullValue() {
        OvertureDestinationLabel label = new OvertureDestinationLabel(null, OvertureDestinationLabelType.STREET);

        String result = label.toString();

        assertTrue(result.contains("null"));
        assertTrue(result.contains("street"));
    }
}
