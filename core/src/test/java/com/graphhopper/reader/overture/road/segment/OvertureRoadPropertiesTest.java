package com.graphhopper.reader.overture.road.segment;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class OvertureRoadPropertiesTest {

    @Test
    void testConstructorAndGetters() {
        // Prepare dummy data
        List<OvertureConnector> connectors = Collections.emptyList();
        OvertureRoadClass roadClass = OvertureRoadClass.PRIMARY;
        OvertureRoadSubclass subclass = OvertureRoadSubclass.LINK;
        OvertureTheme theme = OvertureTheme.TRANSPORTATION;
        OvertureFeatureType type = OvertureFeatureType.SEGMENT;
        int level = 1;
        int version = 5;

        // Construct the object
        // Note: Passing null/empty lists for complex dependencies not under test here to keep the test
        // focused
        OvertureRoadProperties props = new OvertureRoadProperties(
                connectors,
                Collections.emptyList(), // routes
                roadClass,
                Collections.emptyList(), // destinations
                Collections.emptyList(), // prohibitedTransitions
                Collections.emptyList(), // surfaces
                Collections.emptyList(), // flags
                Collections.emptyList(), // speedLimits
                Collections.emptyList(), // widthRules
                subclass,
                Collections.emptyList(), // subclassRules
                Collections.emptyList(), // accessRestrictions
                level,
                Collections.emptyList(), // levelRules
                theme,
                type,
                version,
                Collections.emptyList(), // sources
                null // names
                );

        // Assertions
        assertSame(connectors, props.getConnectors());
        assertEquals(roadClass, props.getRoadClass());
        assertEquals(subclass, props.getSubclass());
        assertEquals(theme, props.getTheme());
        assertEquals(type, props.getType());
        assertEquals(level, props.getLevel());
        assertEquals(version, props.getVersion());

        // Check that lists are empty as initialized
        assertTrue(props.getRoutes().isEmpty());
        assertTrue(props.getDestinations().isEmpty());
        assertNull(props.getNames());
    }

    @Test
    void testEqualsAndHashCode() {
        // Create two identical objects
        OvertureRoadProperties p1 = createDummyProperties(1, OvertureRoadClass.MOTORWAY);
        OvertureRoadProperties p2 = createDummyProperties(1, OvertureRoadClass.MOTORWAY);

        assertEquals(p1, p2, "Objects with identical fields should be equal");
        assertEquals(p1.hashCode(), p2.hashCode(), "Equal objects must have the same hash code");
    }

    @Test
    void testNotEqualsDifferentLevel() {
        OvertureRoadProperties p1 = createDummyProperties(1, OvertureRoadClass.MOTORWAY);
        OvertureRoadProperties p2 = createDummyProperties(2, OvertureRoadClass.MOTORWAY); // Diff level

        assertNotEquals(p1, p2);
    }

    @Test
    void testNotEqualsDifferentRoadClass() {
        OvertureRoadProperties p1 = createDummyProperties(1, OvertureRoadClass.MOTORWAY);
        OvertureRoadProperties p2 =
                createDummyProperties(1, OvertureRoadClass.RESIDENTIAL); // Diff class

        assertNotEquals(p1, p2);
    }

    @Test
    void testNotEqualsNull() {
        OvertureRoadProperties p1 = createDummyProperties(1, OvertureRoadClass.MOTORWAY);
        assertNotEquals(null, p1);
    }

    @Test
    void testToString() {
        OvertureRoadProperties props = createDummyProperties(0, OvertureRoadClass.SECONDARY);
        String s = props.toString();

        assertTrue(s.contains("OvertureRoadProperties"));
        assertTrue(s.contains("roadClass=secondary"));
        assertTrue(s.contains("level=0"));
    }

    // Helper to create instances quickly
    private OvertureRoadProperties createDummyProperties(int level, OvertureRoadClass roadClass) {
        return new OvertureRoadProperties(
                Collections.emptyList(),
                Collections.emptyList(),
                roadClass,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                OvertureRoadSubclass.LINK,
                Collections.emptyList(),
                Collections.emptyList(),
                level,
                Collections.emptyList(),
                OvertureTheme.TRANSPORTATION,
                OvertureFeatureType.SEGMENT,
                1,
                Collections.emptyList(),
                null);
    }
}
