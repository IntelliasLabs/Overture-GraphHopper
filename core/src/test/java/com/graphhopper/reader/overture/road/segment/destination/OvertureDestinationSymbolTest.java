package com.graphhopper.reader.overture.road.segment.destination;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OvertureDestinationSymbolTest {

    @Test
    void testFromStringValidLowerCase() {
        assertEquals(
                OvertureDestinationSymbol.MOTORWAY, OvertureDestinationSymbol.fromString("motorway"));
        assertEquals(
                OvertureDestinationSymbol.AIRPORT, OvertureDestinationSymbol.fromString("airport"));
        assertEquals(
                OvertureDestinationSymbol.HOSPITAL, OvertureDestinationSymbol.fromString("hospital"));
        assertEquals(OvertureDestinationSymbol.CENTER, OvertureDestinationSymbol.fromString("center"));
        assertEquals(
                OvertureDestinationSymbol.TRAIN_STATION,
                OvertureDestinationSymbol.fromString("train_station"));
        assertEquals(
                OvertureDestinationSymbol.RESTROOMS, OvertureDestinationSymbol.fromString("restrooms"));
    }

    @Test
    void testFromStringValidUpperCase() {
        assertEquals(
                OvertureDestinationSymbol.INDUSTRIAL, OvertureDestinationSymbol.fromString("INDUSTRIAL"));
        assertEquals(
                OvertureDestinationSymbol.PARKING, OvertureDestinationSymbol.fromString("PARKING"));
        assertEquals(OvertureDestinationSymbol.BUS, OvertureDestinationSymbol.fromString("BUS"));
        assertEquals(OvertureDestinationSymbol.FERRY, OvertureDestinationSymbol.fromString("FERRY"));
    }

    @Test
    void testFromStringValidMixedCase() {
        assertEquals(
                OvertureDestinationSymbol.REST_AREA, OvertureDestinationSymbol.fromString("Rest_Area"));
        assertEquals(
                OvertureDestinationSymbol.MOTORROAD, OvertureDestinationSymbol.fromString("MotorRoad"));
        assertEquals(
                OvertureDestinationSymbol.FUEL_DIESEL, OvertureDestinationSymbol.fromString("Fuel_Diesel"));
        assertEquals(
                OvertureDestinationSymbol.CAMP_SITE, OvertureDestinationSymbol.fromString("Camp_Site"));
    }

    @Test
    void testFromStringAllValues() {
        assertEquals(OvertureDestinationSymbol.FUEL, OvertureDestinationSymbol.fromString("fuel"));
        assertEquals(
                OvertureDestinationSymbol.VIEWPOINT, OvertureDestinationSymbol.fromString("viewpoint"));
        assertEquals(OvertureDestinationSymbol.FOOD, OvertureDestinationSymbol.fromString("food"));
        assertEquals(
                OvertureDestinationSymbol.LODGING, OvertureDestinationSymbol.fromString("lodging"));
        assertEquals(OvertureDestinationSymbol.INFO, OvertureDestinationSymbol.fromString("info"));
        assertEquals(
                OvertureDestinationSymbol.INTERCHANGE, OvertureDestinationSymbol.fromString("interchange"));
    }

    @Test
    void testFromStringThrowsOnNull() {
        assertNull(OvertureDestinationSymbol.fromString(null));
    }

    @Test
    void testFromStringThrowsOnUnknownValue() {
        assertNull(OvertureDestinationSymbol.fromString("unknown_symbol"));
    }

    @Test
    void testToStringReturnsLowerCase() {
        assertEquals("motorway", OvertureDestinationSymbol.MOTORWAY.toString());
        assertEquals("airport", OvertureDestinationSymbol.AIRPORT.toString());
        assertEquals("hospital", OvertureDestinationSymbol.HOSPITAL.toString());
        assertEquals("center", OvertureDestinationSymbol.CENTER.toString());
        assertEquals("industrial", OvertureDestinationSymbol.INDUSTRIAL.toString());
        assertEquals("parking", OvertureDestinationSymbol.PARKING.toString());
        assertEquals("bus", OvertureDestinationSymbol.BUS.toString());
        assertEquals("train_station", OvertureDestinationSymbol.TRAIN_STATION.toString());
        assertEquals("rest_area", OvertureDestinationSymbol.REST_AREA.toString());
        assertEquals("ferry", OvertureDestinationSymbol.FERRY.toString());
        assertEquals("motorroad", OvertureDestinationSymbol.MOTORROAD.toString());
        assertEquals("fuel", OvertureDestinationSymbol.FUEL.toString());
        assertEquals("viewpoint", OvertureDestinationSymbol.VIEWPOINT.toString());
        assertEquals("fuel_diesel", OvertureDestinationSymbol.FUEL_DIESEL.toString());
        assertEquals("food", OvertureDestinationSymbol.FOOD.toString());
        assertEquals("lodging", OvertureDestinationSymbol.LODGING.toString());
        assertEquals("info", OvertureDestinationSymbol.INFO.toString());
        assertEquals("camp_site", OvertureDestinationSymbol.CAMP_SITE.toString());
        assertEquals("interchange", OvertureDestinationSymbol.INTERCHANGE.toString());
        assertEquals("restrooms", OvertureDestinationSymbol.RESTROOMS.toString());
    }
}
