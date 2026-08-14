package com.graphhopper.reader.overture.road.segment.destination;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OvertureDestinationLabelTypeTest {

    @Test
    void testFromStringValidLowerCase() {
        assertEquals(OvertureDestinationLabelType.STREET, OvertureDestinationLabelType.fromString("street"));
        assertEquals(OvertureDestinationLabelType.COUNTRY, OvertureDestinationLabelType.fromString("country"));
        assertEquals(OvertureDestinationLabelType.ROUTE_REF, OvertureDestinationLabelType.fromString("route_ref"));
        assertEquals(OvertureDestinationLabelType.TOWARD_ROUTE_REF, OvertureDestinationLabelType.fromString("toward_route_ref"));
        assertEquals(OvertureDestinationLabelType.UNKNOWN, OvertureDestinationLabelType.fromString("unknown"));
    }

    @Test
    void testFromStringValidUpperCase() {
        assertEquals(OvertureDestinationLabelType.STREET, OvertureDestinationLabelType.fromString("STREET"));
        assertEquals(OvertureDestinationLabelType.COUNTRY, OvertureDestinationLabelType.fromString("COUNTRY"));
        assertEquals(OvertureDestinationLabelType.ROUTE_REF, OvertureDestinationLabelType.fromString("ROUTE_REF"));
    }

    @Test
    void testFromStringValidMixedCase() {
        assertEquals(OvertureDestinationLabelType.STREET, OvertureDestinationLabelType.fromString("Street"));
        assertEquals(OvertureDestinationLabelType.ROUTE_REF, OvertureDestinationLabelType.fromString("Route_Ref"));
        assertEquals(OvertureDestinationLabelType.TOWARD_ROUTE_REF, OvertureDestinationLabelType.fromString("Toward_Route_Ref"));
    }

    @Test
    void testFromStringThrowsOnNull() {
        assertNull(OvertureDestinationLabelType.fromString(null));
    }

    @Test
    void testFromStringThrowsOnUnknownValue() {
        assertNull(OvertureDestinationLabelType.fromString("invalid_type"));
    }

    @Test
    void testToStringReturnsLowerCase() {
        assertEquals("street", OvertureDestinationLabelType.STREET.toString());
        assertEquals("country", OvertureDestinationLabelType.COUNTRY.toString());
        assertEquals("route_ref", OvertureDestinationLabelType.ROUTE_REF.toString());
        assertEquals("toward_route_ref", OvertureDestinationLabelType.TOWARD_ROUTE_REF.toString());
        assertEquals("unknown", OvertureDestinationLabelType.UNKNOWN.toString());
    }
}
