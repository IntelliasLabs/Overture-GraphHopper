package com.graphhopper.reader.overture.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.graphhopper.routing.ev.RoadClass;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class OvertureRoadClassParserTest {
    @ParameterizedTest
    @CsvSource({
        /// OvertureClass, OvertureSubclass, Expected RoadClass
        "path,        sidewalk,      FOOTWAY", // Subclass refinement (priority)
        "footway,     crosswalk,     FOOTWAY", // Subclass refinement
        "path,        cycle_crossing, CYCLEWAY", // Subclass refinement
        "service,     parking_aisle, SERVICE", // Subclass refinement
        "service,     driveway,      SERVICE", // Subclass refinement
        "service,     alley,         SERVICE", // Subclass refinement
        "motorway,    link,          MOTORWAY", // Subclass known but not refined
        "motorway,    null,          MOTORWAY", // Direct mapping
        "primary,     '',            PRIMARY", // Direct mapping with empty subclass
        "residential, null,          RESIDENTIAL", // Direct mapping
        "track,       null,          TRACK", // Direct mapping
        "steps,       null,          STEPS", // Direct mapping
        "cycleway,    null,          CYCLEWAY" // Direct mapping
    })
    @DisplayName("Should correctly map Overture class/subclass combinations to RoadClass")
    void testParseMapping(String oClass, String oSubclass, RoadClass expected) {
        String subclass = "null".equals(oSubclass) ? null : oSubclass;
        assertEquals(expected, OvertureRoadClassParser.parse(oClass, subclass));
    }

    @Test
    @DisplayName("Should handle unknown classes, nulls, and unrecognized strings by returning OTHER")
    void testUnknownAndEdgeCases() {
        assertEquals(
                RoadClass.OTHER,
                OvertureRoadClassParser.parse("abrakadabra_road", null),
                "Should return OTHER for completely unknown class");

        assertEquals(
                RoadClass.OTHER,
                OvertureRoadClassParser.parse(null, null),
                "Should return OTHER for null inputs");

        assertEquals(
                RoadClass.OTHER,
                OvertureRoadClassParser.parse("unknown", "unknown_sub"),
                "Should return OTHER when both class and subclass are unrecognized");
    }

    @Test
    @DisplayName("Precedence Check: Subclass must override general Class (path+sidewalk -> FOOTWAY)")
    void testSubclassPrecedenceOverClass() {
        assertEquals(
                RoadClass.FOOTWAY,
                OvertureRoadClassParser.parse("path", "sidewalk"),
                "Subclass 'sidewalk' must override class 'path' and return FOOTWAY");
    }
}
