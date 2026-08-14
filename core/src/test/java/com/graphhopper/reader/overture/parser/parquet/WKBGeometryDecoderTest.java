package com.graphhopper.reader.overture.parser.parquet;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.LineString;
import static org.junit.jupiter.api.Assertions.*;

public class WKBGeometryDecoderTest {

    // Helper to create byte arrays from hex strings
    private static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    @Test
    public void testDecodeLineString_LittleEndian() {
        // Little Endian (01), Type 2 (LineString), 2 points: (10, 20), (30, 40)
        String wkbHex = "01" + "02000000" + "02000000"
                + "0000000000002440" + "0000000000003440"
                + "0000000000003E40" + "0000000000004440";

        LineString result = WKBGeometryDecoder.decodeLineString(hexToBytes(wkbHex));

        assertNotNull(result);
        assertEquals(2, result.getNumPoints());

        // Point 1
        assertEquals(10.0, result.getCoordinateN(0).x, 0.0001);
        assertEquals(20.0, result.getCoordinateN(0).y, 0.0001);

        // Point 2
        assertEquals(30.0, result.getCoordinateN(1).x, 0.0001);
        assertEquals(40.0, result.getCoordinateN(1).y, 0.0001);
    }

    @Test
    public void testDecodeLineString_BigEndian() {
        // Big Endian (00), Type 2 (LineString), 2 points
        // Point 1: (1.0, 1.0)
        // Point 2: (2.0, 2.0)
        String wkbHex = "00" + "00000002" + "00000002"
                + "3ff0000000000000" + "3ff0000000000000"
                + "4000000000000000" + "4000000000000000";

        LineString result = WKBGeometryDecoder.decodeLineString(hexToBytes(wkbHex));

        assertNotNull(result);
        assertEquals(2, result.getNumPoints(), "Should decode 2 points");

        // Check Point 1
        assertEquals(1.0, result.getCoordinateN(0).x, 0.0001);
        assertEquals(1.0, result.getCoordinateN(0).y, 0.0001);

        // Check Point 2
        assertEquals(2.0, result.getCoordinateN(1).x, 0.0001);
        assertEquals(2.0, result.getCoordinateN(1).y, 0.0001);
    }

    @Test
    public void testInvalidGeometryType() {
        // Valid WKB, but it's a Point (Type 1), not a LineString
        String wkbHex = "01" + "01000000" + "0000000000002440" + "0000000000003440";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            WKBGeometryDecoder.decodeLineString(hexToBytes(wkbHex));
        });

        assertTrue(exception.getMessage().contains("Expected LineString"),
                "Exception message should mention expected geometry type");
    }

    @Test
    public void testCorruptWKB() {
        // Random garbage bytes that cannot form a valid WKB structure
        byte[] garbage = new byte[]{0x05, 0x05, 0x05, 0x05};

        assertThrows(IllegalArgumentException.class, () -> {
            WKBGeometryDecoder.decodeLineString(garbage);
        });
    }

    @Test
    public void testNullOrEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> WKBGeometryDecoder.decodeLineString(null));
        assertThrows(IllegalArgumentException.class, () -> WKBGeometryDecoder.decodeLineString(new byte[0]));
    }
}
