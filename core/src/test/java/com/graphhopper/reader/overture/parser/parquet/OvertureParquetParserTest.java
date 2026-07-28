package com.graphhopper.reader.overture.parser.parquet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.graphhopper.reader.overture.road.segment.OvertureRoadClass;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSubclass;
import com.graphhopper.reader.overture.road.segment.OvertureSegmentSubtype;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.LineString;
import org.mockito.Mockito;

class OvertureParquetParserTest {

    static {
        System.setProperty("hadoop.home.dir", "/");
        System.setProperty("io.netty.tryReflectionSetAccessible", "true");
    }

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("File: Should throw IllegalArgumentException when file is null")
    void parseFile_NullInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> OvertureParquetParser.parse((File) null));
    }

    @Test
    @DisplayName("File: Should throw IOException when file does not exist")
    void parseFile_NonExistentFile_ThrowsException() {
        File notExistFile = new File(tempDir.toFile(), "notExist.parquet");

        assertThrows(FileNotFoundException.class, () -> OvertureParquetParser.parse(notExistFile));
    }

    @Test
    @DisplayName("File: Should throw IOException when content is invalid")
    void parseFile_InvalidContent_ThrowsException() throws IOException {
        Path badFile = tempDir.resolve("bad_data.parquet");
        Files.writeString(badFile, "This is text, not a parquet binary");

        Exception exception =
                assertThrows(Exception.class, () -> OvertureParquetParser.parse(badFile.toFile()));

        assertNotNull(exception);
    }

    @Test
    @DisplayName("Parse actual sample file if available")
    void parseFile_RealSample_Success() throws IOException {
        String sampleFileName = "com/graphhopper/reader/overture/parquet/RoadSample.parquet";

        var resourceUrl = getClass().getClassLoader().getResource(sampleFileName);

        File sampleFile = new File(resourceUrl.getFile());

        List<OvertureRoadSegment> result = OvertureParquetParser.parse(sampleFile);

        assertNotNull(result, "Result list should not be null");
    }

    @Test
    @DisplayName("tryMapRecord: Should return null when mandatory fields are missing")
    void tryMapRecord_MissingFields_ReturnsNull() {
        GenericRecord record = Mockito.mock(GenericRecord.class);

        when(record.get("id")).thenReturn("ebd404f4-3158-414b-b784-5387933c8f55");
        when(record.get("class")).thenReturn(null);

        assertNull(OvertureParquetParser.tryMapRecord(record));
    }

    @Test
    @DisplayName("tryMapRecord: Should filter out invalid road classes")
    void tryMapRecord_InvalidRoadClass_ReturnsNull() {
        GenericRecord record = Mockito.mock(GenericRecord.class);

        when(record.get("id")).thenReturn("ebd404f4-3158-414b-b784-5387933c8f55");
        when(record.get("class")).thenReturn("invalid_class_name");
        when(record.get("geometry")).thenReturn(ByteBuffer.allocate(10));

        assertNull(
                OvertureParquetParser.tryMapRecord(record),
                "Record with invalid road class should be filtered out (return null)");
    }

    @Test
    @DisplayName("tryMapRecord: Should correctly extract ID, Class and Subclass")
    void tryMapRecord_ValidRecord_ReturnsSegment() {
        GenericRecord record = Mockito.mock(GenericRecord.class);

        byte[] wkb = new byte[] {
            0, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 63, -16, 0, 0, 0,
            0, 0, 0, 63, -16, 0, 0, 0, 0, 0, 0
        };

        when(record.get("id")).thenReturn("ebd404f4-3158-414b-b784-5387933c8f55");
        when(record.get("class")).thenReturn("motorway");
        when(record.get("subclass")).thenReturn("link");
        when(record.get("geometry")).thenReturn(ByteBuffer.wrap(wkb));

        OvertureRoadSegment result = OvertureParquetParser.tryMapRecord(record);

        assertNotNull(result);
        assertEquals("ebd404f4-3158-414b-b784-5387933c8f55", result.getId());
        assertEquals(OvertureRoadClass.MOTORWAY, result.getProperties().getRoadClass());
        assertEquals(OvertureRoadSubclass.LINK, result.getProperties().getSubclass());
        assertInstanceOf(LineString.class, result.getLineString());
    }

    @Test
    @DisplayName("tryMapRecord: Missing optional subclass should return segment with null subclass")
    void tryMapRecord_MissingSubclass_ReturnsSegment() {
        GenericRecord record = Mockito.mock(GenericRecord.class);
        byte[] wkb = new byte[] {
            0, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 63, -16, 0, 0, 0,
            0, 0, 0, 63, -16, 0, 0, 0, 0, 0, 0
        };

        when(record.get("id")).thenReturn("ebd404f4-3158-414b-b784-5387933c8f55");
        when(record.get("class")).thenReturn("primary");
        when(record.get("subclass")).thenReturn(null);
        when(record.get("geometry")).thenReturn(ByteBuffer.wrap(wkb));

        OvertureRoadSegment result = OvertureParquetParser.tryMapRecord(record);

        assertNotNull(result);
        assertNull(result.getProperties().getSubclass());
        assertEquals(OvertureRoadClass.PRIMARY, result.getProperties().getRoadClass());
    }

    @Test
    @DisplayName("tryMapRecord: Invalid optional subclass should still return valid segment")
    void tryMapRecord_InvalidSubclass_ReturnsSegment() {
        GenericRecord record = Mockito.mock(GenericRecord.class);
        byte[] wkb = new byte[] {
            0, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 63, -16, 0, 0, 0,
            0, 0, 0, 63, -16, 0, 0, 0, 0, 0, 0
        };

        when(record.get("id")).thenReturn("ebd404f4-3158-414b-b784-5387933c8f55");
        when(record.get("class")).thenReturn("primary");
        when(record.get("subclass")).thenReturn("non_existent_subclass");
        when(record.get("geometry")).thenReturn(ByteBuffer.wrap(wkb));

        OvertureRoadSegment result = OvertureParquetParser.tryMapRecord(record);

        assertNotNull(result);
        assertNull(result.getProperties().getSubclass());
        assertEquals(OvertureRoadClass.PRIMARY, result.getProperties().getRoadClass());
    }

    @Test
    @DisplayName("tryMapRecord: Should parse names field correctly")
    void tryMapRecord_ValidNames_ReturnsSegmentWithNames() {
        // 1. Setup Basic Fields
        GenericRecord record = Mockito.mock(GenericRecord.class);
        byte[] wkb = new byte[] {
            0, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 63, -16, 0, 0, 0,
            0, 0, 0, 63, -16, 0, 0, 0, 0, 0, 0
        };

        when(record.get("id")).thenReturn("test-id");
        when(record.get("class")).thenReturn("primary");
        when(record.get("subclass")).thenReturn(null);
        when(record.get("geometry")).thenReturn(ByteBuffer.wrap(wkb));

        // 2. Setup Schema Mocking
        // We must mock the Schema so r.getSchema().getField("element") doesn't throw NPE
        Schema mockSchema = Mockito.mock(Schema.class);
        when(mockSchema.getField("element")).thenReturn(null); // Explicitly say "no element field"

        // 3. Setup Names Record
        GenericRecord namesRecord = Mockito.mock(GenericRecord.class);
        when(namesRecord.get("primary")).thenReturn("South Pole Traverse");
        when(namesRecord.get("common")).thenReturn(null);

        // 4. Setup Rules
        GenericRecord rule1 = Mockito.mock(GenericRecord.class);
        when(rule1.getSchema()).thenReturn(mockSchema); // Apply schema mock
        when(rule1.get("variant")).thenReturn("common");
        when(rule1.get("value")).thenReturn("South Pole Traverse");

        GenericRecord rule2 = Mockito.mock(GenericRecord.class);
        when(rule2.getSchema()).thenReturn(mockSchema); // Apply schema mock
        when(rule2.get("variant")).thenReturn("common");
        when(rule2.get("value")).thenReturn("McMurdo - South Pole Ice Highway");

        List<GenericRecord> rulesList = List.of(rule1, rule2);
        when(namesRecord.get("rules")).thenReturn(rulesList);
        when(record.get("names")).thenReturn(namesRecord);

        // 5. Execute
        OvertureRoadSegment result = OvertureParquetParser.tryMapRecord(record);

        // 6. Assert
        assertNotNull(result);
        assertNotNull(result.getProperties().getNames(), "Names object should not be null");
        assertEquals("South Pole Traverse", result.getProperties().getNames().getPrimary());

        assertNotNull(result.getProperties().getNames().getRules());
        assertEquals(2, result.getProperties().getNames().getRules().size());
        assertEquals(
                "South Pole Traverse",
                result.getProperties().getNames().getRules().getFirst().getValue());
    }

    /**
     * A water segment in Overture carries no {@code class} at all - the subtype is what identifies a
     * ferry. Requiring a class dropped every ferry in an extract before any parser saw it, which left
     * {@code ferry_speed} unwritten and {@code road_environment} never reporting FERRY.
     */
    @Test
    @DisplayName("tryMapRecord: A water segment is kept even though it has no class")
    void tryMapRecord_WaterWithoutClass_ReturnsFerrySegment() {
        GenericRecord record = Mockito.mock(GenericRecord.class);

        when(record.get("id")).thenReturn("ee2910e8-956a-46a3-88a6-1ff34e9d5c24");
        when(record.get("class")).thenReturn(null);
        when(record.get("subtype")).thenReturn("water");
        when(record.get("geometry")).thenReturn(ByteBuffer.wrap(LINE_WKB));

        OvertureRoadSegment result = OvertureParquetParser.tryMapRecord(record);

        assertNotNull(result, "a water segment without a class must not be dropped");
        assertEquals(OvertureSegmentSubtype.WATER, result.getProperties().getSubtype());
        assertNull(result.getProperties().getRoadClass(), "water carries no road class");
        assertInstanceOf(LineString.class, result.getLineString());
    }

    @Test
    @DisplayName("tryMapRecord: Road attributes are absent on a water segment rather than defaulted")
    void tryMapRecord_Water_HasNoRoadAttributes() {
        GenericRecord record = Mockito.mock(GenericRecord.class);

        when(record.get("id")).thenReturn("ee2910e8-956a-46a3-88a6-1ff34e9d5c24");
        when(record.get("subtype")).thenReturn("water");
        when(record.get("geometry")).thenReturn(ByteBuffer.wrap(LINE_WKB));

        var props = OvertureParquetParser.tryMapRecord(record).getProperties();

        assertNull(props.getSubclass());
        assertTrue(props.getSurfaces().isEmpty());
        assertTrue(props.getFlags().isEmpty());
        assertTrue(props.getSpeedLimits().isEmpty());
        assertTrue(props.getWidthRules().isEmpty());
        // No restrictions means the access parsers grant access, which is what makes a ferry routable.
        assertTrue(props.getAccessRestrictions().isEmpty());
    }

    @Test
    @DisplayName("tryMapRecord: A road segment still requires a class")
    void tryMapRecord_RoadWithoutClass_ReturnsNull() {
        GenericRecord record = Mockito.mock(GenericRecord.class);

        when(record.get("id")).thenReturn("ebd404f4-3158-414b-b784-5387933c8f55");
        when(record.get("class")).thenReturn(null);
        when(record.get("subtype")).thenReturn("road");
        when(record.get("geometry")).thenReturn(ByteBuffer.wrap(LINE_WKB));

        assertNull(
                OvertureParquetParser.tryMapRecord(record),
                "only water may omit the class; a road without one is still incomplete");
    }

    @Test
    @DisplayName("tryMapRecord: A segment with no subtype is treated as a road")
    void tryMapRecord_MissingSubtype_DefaultsToRoad() {
        GenericRecord record = Mockito.mock(GenericRecord.class);

        when(record.get("id")).thenReturn("ebd404f4-3158-414b-b784-5387933c8f55");
        when(record.get("class")).thenReturn("primary");
        when(record.get("subtype")).thenReturn(null);
        when(record.get("geometry")).thenReturn(ByteBuffer.wrap(LINE_WKB));

        OvertureRoadSegment result = OvertureParquetParser.tryMapRecord(record);

        assertNotNull(result);
        assertEquals(OvertureSegmentSubtype.ROAD, result.getProperties().getSubtype());
    }

    /** A two-point LineString in WKB, the same geometry the other cases here use. */
    private static final byte[] LINE_WKB = new byte[] {
        0, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 63, -16, 0, 0, 0,
        0, 0, 0, 63, -16, 0, 0, 0, 0, 0, 0
    };

    /**
     * A class the enum does not know is still counted by its raw value: the parsed class is null
     * exactly when the value was unrecognised, so grouping on that named nothing.
     */
    @Test
    @DisplayName("SkipTally: Unsupported classes are counted by their raw value")
    void skipTally_GroupsUnsupportedClassesByRawValue() {
        var tally = new OvertureParquetParser.SkipTally();

        for (String unknownClass : List.of("teleporter", "teleporter", "zipline")) {
            GenericRecord record = Mockito.mock(GenericRecord.class);
            when(record.get("id")).thenReturn("ebd404f4-3158-414b-b784-5387933c8f55");
            when(record.get("class")).thenReturn(unknownClass);
            when(record.get("subtype")).thenReturn("road");
            when(record.get("geometry")).thenReturn(ByteBuffer.wrap(LINE_WKB));

            assertNull(OvertureParquetParser.tryMapRecord(record, tally));
        }

        assertEquals(3, tally.total());
        assertEquals(
                Map.of("unsupported class 'teleporter'", 2, "unsupported class 'zipline'", 1),
                tally.byReason());
    }

    /**
     * Rail is rejected on its subtype rather than by the class filter. Most rail classes have no
     * OvertureRoadClass constant and so were dropped by accident, but {@code class=unknown} does map to
     * one - 84,439 segments of it in a Europe extract reached the graph as roads open to every mode,
     * because rail carries no access restrictions.
     */
    @Test
    @DisplayName("tryMapRecord: Rail is dropped whatever its class")
    void tryMapRecord_Rail_IsAlwaysDropped() {
        var tally = new OvertureParquetParser.SkipTally();

        for (String railClass : List.of("standard_gauge", "tram", "unknown")) {
            GenericRecord record = Mockito.mock(GenericRecord.class);
            when(record.get("id")).thenReturn("ebd404f4-3158-414b-b784-5387933c8f55");
            when(record.get("class")).thenReturn(railClass);
            when(record.get("subtype")).thenReturn("rail");
            when(record.get("geometry")).thenReturn(ByteBuffer.wrap(LINE_WKB));

            assertNull(
                    OvertureParquetParser.tryMapRecord(record, tally),
                    "rail class '" + railClass + "' must not reach the graph");
        }

        assertEquals(
                Map.of("rail 'standard_gauge'", 1, "rail 'tram'", 1, "rail 'unknown'", 1),
                tally.byReason());
    }

    @Test
    @DisplayName("tryMapRecord: A road whose class is unknown is still imported")
    void tryMapRecord_RoadWithUnknownClass_IsKept() {
        GenericRecord record = Mockito.mock(GenericRecord.class);
        when(record.get("id")).thenReturn("ebd404f4-3158-414b-b784-5387933c8f55");
        when(record.get("class")).thenReturn("unknown");
        when(record.get("subtype")).thenReturn("road");
        when(record.get("geometry")).thenReturn(ByteBuffer.wrap(LINE_WKB));

        OvertureRoadSegment result = OvertureParquetParser.tryMapRecord(record);

        assertNotNull(result, "only the rail subtype is rejected, not an unknown road class");
        assertEquals(OvertureRoadClass.UNKNOWN, result.getProperties().getRoadClass());
    }

    @Test
    @DisplayName("SkipTally: A record missing mandatory fields is counted, not logged per record")
    void skipTally_CountsMissingMandatoryFields() {
        var tally = new OvertureParquetParser.SkipTally();
        GenericRecord record = Mockito.mock(GenericRecord.class);
        when(record.get("id")).thenReturn(null);

        assertNull(OvertureParquetParser.tryMapRecord(record, tally));
        assertEquals(Map.of("missing a mandatory field", 1), tally.byReason());
    }

    @Test
    @DisplayName("SkipTally: A fully consumed extract records nothing")
    void skipTally_EmptyWhenNothingSkipped() {
        var tally = new OvertureParquetParser.SkipTally();
        GenericRecord record = Mockito.mock(GenericRecord.class);
        when(record.get("id")).thenReturn("ebd404f4-3158-414b-b784-5387933c8f55");
        when(record.get("class")).thenReturn("primary");
        when(record.get("geometry")).thenReturn(ByteBuffer.wrap(LINE_WKB));

        assertNotNull(OvertureParquetParser.tryMapRecord(record, tally));
        assertEquals(0, tally.total());
        assertTrue(tally.byReason().isEmpty());
    }
}
