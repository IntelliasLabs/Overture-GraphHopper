package com.graphhopper.reader.overture.road.segment;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import org.junit.jupiter.api.Test;

class OvertureRoadSegmentSubtypeTest {

    @Test
    void waterSubtype_createsSegmentWithNullProperties() {
        OvertureRoadProperties props =
                withSubtype(createDummyProperties(0, null), OvertureSegmentSubtype.WATER);
        OvertureRoadSegment seg = new OvertureRoadSegment("seg-water", null, props);

        assertEquals(OvertureSegmentSubtype.WATER, seg.getSubtype());
        assertNotNull(seg.getProperties(), "Properties must be present and contain subtype for water");
        assertEquals(OvertureSegmentSubtype.WATER, seg.getProperties().getSubtype());
    }

    @Test
    void roadSubtype_keepsPropertiesAsProvided() {
        OvertureRoadProperties props = withSubtype(
                createDummyProperties(0, OvertureRoadClass.PRIMARY), OvertureSegmentSubtype.ROAD);
        OvertureRoadSegment seg = new OvertureRoadSegment("seg-road", null, props);

        assertEquals(OvertureSegmentSubtype.ROAD, seg.getSubtype());
        assertSame(props, seg.getProperties(), "Road subtype should keep provided properties");
    }

    @Test
    void railSubtype_keepsPropertiesAsProvided() {
        OvertureRoadProperties props =
                withSubtype(createDummyProperties(2, OvertureRoadClass.TRACK), OvertureSegmentSubtype.RAIL);
        OvertureRoadSegment seg = new OvertureRoadSegment("seg-rail", null, props);

        assertEquals(OvertureSegmentSubtype.RAIL, seg.getSubtype());
        assertSame(
                props,
                seg.getProperties(),
                "Rail subtype should keep provided properties (parser may null some fields later)");
    }

    // Helper to create minimal OvertureRoadProperties instances used in tests
    private OvertureRoadProperties createDummyProperties(int level, OvertureRoadClass roadClass) {
        return new OvertureRoadProperties(
                Collections.emptyList(), // connectors
                Collections.emptyList(), // routes
                roadClass,
                Collections.emptyList(), // destinations
                Collections.emptyList(), // prohibitedTransitions
                Collections.emptyList(), // surfaces
                Collections.emptyList(), // flags
                Collections.emptyList(), // speedLimits
                Collections.emptyList(), // widthRules
                OvertureRoadSubclass.LINK,
                Collections.emptyList(), // subclassRules
                Collections.emptyList(), // accessRestrictions
                level,
                Collections.emptyList(), // levelRules
                OvertureTheme.TRANSPORTATION,
                OvertureFeatureType.SEGMENT,
                1, // version
                Collections.emptyList(), // sources
                null, // names
                OvertureSegmentSubtype.ROAD);
    }

    // Convenience helper to return a modified properties instance with a different subtype.
    // Since OvertureRoadProperties is immutable, create a shallow copy using the extended
    // constructor.
    private OvertureRoadProperties withSubtype(OvertureRoadProperties p, OvertureSegmentSubtype st) {
        return new OvertureRoadProperties(
                p.getConnectors(),
                p.getRoutes(),
                p.getRoadClass(),
                p.getDestinations(),
                p.getProhibitedTransitions(),
                p.getSurfaces(),
                p.getFlags(),
                p.getSpeedLimits(),
                p.getWidthRules(),
                p.getSubclass(),
                p.getSubclassRules(),
                p.getAccessRestrictions(),
                p.getLevel(),
                p.getLevelRules(),
                p.getTheme(),
                p.getType(),
                p.getVersion(),
                p.getSources(),
                p.getNames(),
                st);
    }
}
