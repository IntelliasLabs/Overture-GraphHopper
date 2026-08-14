package com.graphhopper.reader.overture.road.segment.rule;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OvertureTransitionSequenceItemTest {

    @Test
    void testConstructorAndGetters() {
        String expectedConnectorId = "overture:connector:123";
        String expectedSegmentId = "overture:segment:456";

        OvertureTransitionSequenceItem item = new OvertureTransitionSequenceItem(expectedConnectorId, expectedSegmentId);

        assertEquals(expectedConnectorId, item.getConnectorId());
        assertEquals(expectedSegmentId, item.getSegmentId());
    }

    @Test
    void testEqualsWithEqualObjects() {
        OvertureTransitionSequenceItem item1 = new OvertureTransitionSequenceItem("conn_1", "seg_1");
        OvertureTransitionSequenceItem item2 = new OvertureTransitionSequenceItem("conn_1", "seg_1");

        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());
    }

    @Test
    void testNotEqualsDifferentConnector() {
        OvertureTransitionSequenceItem item1 = new OvertureTransitionSequenceItem("conn_A", "seg_1");
        OvertureTransitionSequenceItem item2 = new OvertureTransitionSequenceItem("conn_B", "seg_1");

        assertNotEquals(item1, item2);
    }

    @Test
    void testNotEqualsDifferentSegment() {
        OvertureTransitionSequenceItem item1 = new OvertureTransitionSequenceItem("conn_1", "seg_A");
        OvertureTransitionSequenceItem item2 = new OvertureTransitionSequenceItem("conn_1", "seg_B");

        assertNotEquals(item1, item2);
    }

    @Test
    void testEqualsWithNullAndDifferentClass() {
        OvertureTransitionSequenceItem item = new OvertureTransitionSequenceItem("c1", "s1");

        assertNotEquals(null, item);
        assertNotEquals("string", item);
    }

    @Test
    void testToString() {
        OvertureTransitionSequenceItem item = new OvertureTransitionSequenceItem("c123", "s456");
        String result = item.toString();

        assertTrue(result.contains("OvertureTransitionSequenceItem"));
        assertTrue(result.contains("connectorId='c123'"));
        assertTrue(result.contains("segmentId='s456'"));
    }
}
