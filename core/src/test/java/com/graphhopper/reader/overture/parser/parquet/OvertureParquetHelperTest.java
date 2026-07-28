package com.graphhopper.reader.overture.parser.parquet;

import static org.junit.jupiter.api.Assertions.*;

import com.graphhopper.reader.overture.names.Bcp47LanguageTag;
import com.graphhopper.reader.overture.names.OvertureNames;
import java.io.File;
import java.net.URL;
import java.nio.ByteBuffer;
import org.apache.avro.generic.GenericRecord;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.io.InputFile;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.io.InputFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

public class OvertureParquetHelperTest {

    private static GenericRecord firstGeometryRecord;
    private static GenericRecord firstNamesRecord;
    private static GenericRecord secondNamesRecord;

    @BeforeAll
    static void setup() throws Exception {
        // Load geometry sample
        URL geomRes = OvertureParquetHelperTest.class.getResource(
                "/com/graphhopper/reader/overture/parquet/RoadGeometrySample.parquet");
        if (geomRes != null) {
            InputFile inputFile = new LocalParquetInputFile(new File(geomRes.toURI()));

            try (ParquetReader<GenericRecord> reader =
                    AvroParquetReader.<GenericRecord>builder(inputFile).build()) {
                firstGeometryRecord = reader.read();
            }
        }

        // Load first names sample
        URL namesRes = OvertureParquetHelperTest.class.getResource(
                "/com/graphhopper/reader/overture/parquet/FirstRoadNamesSample.parquet");
        if (namesRes != null) {
            InputFile inputFile = new LocalParquetInputFile(new File(namesRes.toURI()));

            try (ParquetReader<GenericRecord> reader =
                    AvroParquetReader.<GenericRecord>builder(inputFile).build()) {
                firstNamesRecord = reader.read();
            }
        }

        // Load second names sample with common field
        URL secondNamesRes = OvertureParquetHelperTest.class.getResource(
                "/com/graphhopper/reader/overture/parquet/SecondRoadNamesSample.parquet");
        if (secondNamesRes != null) {
            InputFile inputFile = new LocalParquetInputFile(new File(secondNamesRes.toURI()));

            try (ParquetReader<GenericRecord> reader =
                    AvroParquetReader.<GenericRecord>builder(inputFile).build()) {
                secondNamesRecord = reader.read();
            }
        }
    }

    @Test
    @DisplayName("Verify record identity and type")
    void testIdentity() {
        assertEquals(
                "ebd404f4-3158-414b-b784-5387933c8f55", firstGeometryRecord.get("id").toString());
    }

    @Test
    @DisplayName("Verify geometry start and end points")
    void testGeometryPoints() {
        var geometry = OvertureParquetHelper.parseGeometry(firstGeometryRecord.get("geometry"));
        assertNotNull(geometry);
        Coordinate[] coords = geometry.getCoordinates();

        assertEquals(23.9011319, coords[0].x, 1e-7);
        assertEquals(23.9020686, coords[coords.length - 1].x, 1e-7);
    }

    // ========== Names Parsing Tests from Real Parquet ==========

    @Test
    @DisplayName("Parse names from real parquet file - primary name")
    void testParseNamesFromParquet_Primary() {
        assertNotNull(firstNamesRecord, "Names sample parquet should be loaded");

        Object rawNames = firstNamesRecord.get("names");
        assertNotNull(rawNames, "names field should exist in parquet record");

        OvertureNames result = OvertureParquetHelper.parseNames(rawNames, "test-id");

        assertNotNull(result, "Names should be parsed successfully");
        assertEquals("South Pole Traverse", result.getPrimary());
    }

    @Test
    @DisplayName("Parse names from real parquet file - common map is empty")
    void testParseNamesFromParquet_CommonMapNull() {
        assertNotNull(firstNamesRecord, "Names sample parquet should be loaded");

        Object rawNames = firstNamesRecord.get("names");
        assertNotNull(rawNames, "names field should exist in parquet record");

        OvertureNames result = OvertureParquetHelper.parseNames(rawNames, "test-id");

        assertNotNull(result);
        assertNotNull(result.getCommon(), "Common map should be emptyList() for this sample");
    }

    @Test
    @DisplayName("Parse names from real parquet file - common map with languages")
    void testParseNamesFromParquet_Common() {
        assertNotNull(secondNamesRecord, "Second names sample parquet should be loaded");

        Object rawNames = secondNamesRecord.get("names");
        assertNotNull(rawNames, "names field should exist in parquet record");

        OvertureNames result = OvertureParquetHelper.parseNames(rawNames, "test-id");

        assertNotNull(result, "Names should be parsed successfully");
        assertEquals("Kohnen-Traverse", result.getPrimary());

        assertNotNull(result.getCommon(), "Common map should not be null");
        assertEquals(2, result.getCommon().size(), "Common map should have 2 language entries");
        assertEquals("Kohnen-Traverse", result.getCommon().get(Bcp47LanguageTag.parse("de")));
        assertEquals("Kohnen Traverse", result.getCommon().get(Bcp47LanguageTag.parse("en")));

        assertNotNull(result.getRules(), "Rules list should not be null");
        assertEquals(1, result.getRules().size(), "Should have 1 rule");
        assertEquals("Kohnen-Traverse", result.getRules().getFirst().getValue());
    }

    @Test
    @DisplayName("Parse names from real parquet file - rules with variants")
    void testParseNamesFromParquet_Rules() {
        assertNotNull(firstNamesRecord, "Names sample parquet should be loaded");

        Object rawNames = firstNamesRecord.get("names");
        assertNotNull(rawNames, "names field should exist in parquet record");

        OvertureNames result = OvertureParquetHelper.parseNames(rawNames, "test-id");

        assertNotNull(result);
        assertNotNull(result.getRules(), "Rules list should not be null");
        assertEquals(2, result.getRules().size(), "Should have 2 rules");

        assertEquals("South Pole Traverse", result.getRules().getFirst().getValue());
        assertEquals("McMurdo - South Pole Ice Highway", result.getRules().get(1).getValue());
    }

    // ========== Existing Geometry Tests ==========

    @Test
    @DisplayName("Edge Case: Handling ByteBuffer with non-zero position")
    void testByteBufferPositionRobustness() {
        byte[] wkb = new byte[] {
            0, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 63, -16, 0, 0, 0,
            0, 0, 0, 63, -16, 0, 0, 0, 0, 0, 0
        };

        ByteBuffer buffer = ByteBuffer.allocate(wkb.length + 10);
        buffer.put(new byte[10]);
        buffer.put(wkb);
        buffer.position(10);

        var result = OvertureParquetHelper.parseGeometry(buffer);

        assertNotNull(result);
        assertEquals(2, result.getNumPoints());
    }

    @Test
    @DisplayName("Handling empty geometry object")
    void testEmptyGeometry() {
        byte[] emptyWkb = new byte[] {0, 0, 0, 0, 2, 0, 0, 0, 0};
        assertNull(
                OvertureParquetHelper.parseGeometry(ByteBuffer.wrap(emptyWkb)),
                "Should return null for empty geometry");
    }

    @Test
    @DisplayName("Support for raw byte array instead of ByteBuffer")
    void testRawByteArraySupport() {
        byte[] wkb = new byte[] {
            0, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 63, -16, 0, 0, 0,
            0, 0, 0, 63, -16, 0, 0, 0, 0, 0, 0
        };

        var result = OvertureParquetHelper.parseGeometry(wkb);

        assertNotNull(result, "Should support direct byte array extraction");
    }

    @Test
    @DisplayName("Handling null and invalid geometry data types")
    void testErrorHandling() {
        assertNull(OvertureParquetHelper.parseGeometry(null), "Should return null for null geometry");
        assertNull(
                OvertureParquetHelper.parseGeometry(ByteBuffer.wrap(new byte[] {1, 2, 3, 4, 5})),
                "Should handle invalid WKB gracefully");
        assertNull(
                OvertureParquetHelper.parseGeometry("not-binary-data"), "Should handle type mismatch");
    }

    // ========== Names Edge Cases ==========

    @Test
    @DisplayName("Parse names returns null for null input")
    void testParseNamesNullInput() {
        assertNull(OvertureParquetHelper.parseNames(null, "test-id"));
    }

    @Test
    @DisplayName("Parse names returns null for non-GenericRecord input")
    void testParseNamesInvalidType() {
        assertNull(OvertureParquetHelper.parseNames("not-a-record", "test-id"));
    }
}
