package com.graphhopper.reader.overture.parsers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.graphhopper.reader.overture.road.segment.OvertureRoadClass;
import org.junit.jupiter.api.Test;

public class OvertureUrbanParserTest {

    @Test
    public void testUrbanRoadClasses() {
        assertTrue(OvertureUrbanParser.isUrban(OvertureRoadClass.RESIDENTIAL));
        assertTrue(OvertureUrbanParser.isUrban(OvertureRoadClass.LIVING_STREET));
        assertTrue(OvertureUrbanParser.isUrban(OvertureRoadClass.SERVICE));
    }

    @Test
    public void testRuralRoadClasses() {
        assertFalse(OvertureUrbanParser.isUrban(OvertureRoadClass.MOTORWAY));
        assertFalse(OvertureUrbanParser.isUrban(OvertureRoadClass.TRUNK));
        assertFalse(OvertureUrbanParser.isUrban(OvertureRoadClass.PRIMARY));
    }

    @Test
    public void testNullHandling() {
        assertFalse(OvertureUrbanParser.isUrban(null));
    }
}
