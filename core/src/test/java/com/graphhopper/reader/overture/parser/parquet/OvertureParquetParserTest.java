package com.graphhopper.reader.overture.parser.parquet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.graphhopper.reader.overture.road.segment.OvertureRoadClass;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSubclass;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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

        assertThrows(
                java.io.FileNotFoundException.class, () -> OvertureParquetParser.parse(notExistFile));
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
        org.apache.avro.Schema mockSchema = Mockito.mock(org.apache.avro.Schema.class);
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
}
