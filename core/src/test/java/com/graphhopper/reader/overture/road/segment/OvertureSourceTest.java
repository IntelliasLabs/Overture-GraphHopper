package com.graphhopper.reader.overture.road.segment;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OvertureSourceTest {

    @Test
    void testConstructorAndGetters() {
        String property = "/properties/road_surface";
        String dataset = "overture";
        String license = "CDLA-Permissive-2.0";
        String recordId = "rec_12345";
        OffsetDateTime updateTime = OffsetDateTime.parse("2023-10-01T12:00:00Z");
        double confidence = 0.95;
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.0, 1.0);

        OvertureSource source = new OvertureSource(
                property, dataset, license, recordId, updateTime, confidence, range
        );

        assertEquals(property, source.getProperty());
        assertEquals(dataset, source.getDataset());
        assertEquals(license, source.getLicense());
        assertEquals(recordId, source.getRecordId());
        assertEquals(updateTime, source.getUpdateTime());
        assertEquals(confidence, source.getConfidence(), 0.0001);
        assertEquals(range, source.getBetween());
    }

    @Test
    void testConstructorWithMinimalData() {
        // Test with null optional fields
        OvertureSource source = new OvertureSource(
                "/properties/class", "osm", null, null, null, 1.0, null
        );

        assertEquals("/properties/class", source.getProperty());
        assertNull(source.getLicense());
        assertNull(source.getUpdateTime());
        assertNull(source.getBetween());
    }

    @Test
    void testEqualsAndHashCode() {
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.2, 0.8);
        // Fix: Use a valid OffsetDateTime object instead of "t" string
        OffsetDateTime now = OffsetDateTime.now();

        OvertureSource s1 = new OvertureSource("/p", "d", "l", "r", now, 0.5, range);
        OvertureSource s2 = new OvertureSource("/p", "d", "l", "r", now, 0.5, range);
        OvertureSource s3 = new OvertureSource("/p", "d", "l", "r", now, 0.9, range); // Diff confidence

        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
        assertNotEquals(s1, s3);
        assertNotEquals(null, s1);
    }

    @Test
    void testNotEqualsDifferentRange() {
        OffsetDateTime now = OffsetDateTime.now();

        OvertureSource s1 = new OvertureSource("/p", "d", "l", "r", now, 0.5, new LinearlyReferencedRange(0.0, 0.5));
        OvertureSource s2 = new OvertureSource("/p", "d", "l", "r", now, 0.5, new LinearlyReferencedRange(0.5, 1.0));

        assertNotEquals(s1, s2);
    }

    @Test
    void testToString() {
        OvertureSource source = new OvertureSource("/p", "dataset_alpha", "MIT", null, null, 0.8, null);
        String result = source.toString();

        assertTrue(result.contains("dataset_alpha"));
        assertTrue(result.contains("property='/p'"));
        assertTrue(result.contains("confidence=0.8"));
    }
}
