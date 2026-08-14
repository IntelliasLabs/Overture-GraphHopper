package com.graphhopper.reader.overture.parser.parquet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import java.util.Arrays;
import java.util.List;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class OvertureParquetRangeTest extends AbstractOvertureParserTest {

    @Test
    @DisplayName("parseRange: should return valid range from simple Double list")
    void parseRange_successWithDoubles() {
        List<Object> raw = Arrays.asList(0.0, 1.0);

        LinearlyReferencedRange result = OvertureParquetHelper.parseRange(raw);

        assertNotNull(result);
        assertEquals(0.0, result.getStart());
        assertEquals(1.0, result.getEnd());
    }

    @Test
    @DisplayName("parseRange: should return null if list size is not 2")
    void parseRange_invalidSize() {
        assertNull(OvertureParquetHelper.parseRange(List.of(0.5)));
        assertNull(OvertureParquetHelper.parseRange(Arrays.asList(0.5, 1.0, 1.5)));
    }

    @Test
    @DisplayName("parseRange: should handle Integer values using Number.doubleValue()")
    void parseRange_handlesIntegers() {
        List<Object> raw = Arrays.asList(0, 100);

        LinearlyReferencedRange result = OvertureParquetHelper.parseRange(raw);

        assertNotNull(result);
        assertEquals(0.0, result.getStart());
        assertEquals(100.0, result.getEnd());
    }

    @Test
    @DisplayName("parseRange: should correctly unwrap nested 'element' records")
    void parseRange_unwrapsNestedElements() {
        GenericRecord startMock = mock(GenericRecord.class);
        mockField(startMock, OvertureSchema.ELEMENT, 0.1);

        GenericRecord endMock = mock(GenericRecord.class);
        mockField(endMock, OvertureSchema.ELEMENT, 0.9);

        List<Object> raw = Arrays.asList(startMock, endMock);

        LinearlyReferencedRange result = OvertureParquetHelper.parseRange(raw);

        assertNotNull(result);
        assertEquals(0.1, result.getStart());
        assertEquals(0.9, result.getEnd());
    }

    @Test
    @DisplayName(
            "parseRange: should return null if one of the elements is not a number or valid record")
    void parseRange_corruptElements() {
        List<Object> raw = Arrays.asList(0.5, "invalid");

        assertNull(OvertureParquetHelper.parseRange(raw));
    }

    @Test
    @DisplayName("parseRange: should return null if input is not a List")
    void parseRange_invalidInputType() {
        assertNull(OvertureParquetHelper.parseRange("just-a-string"));
        assertNull(OvertureParquetHelper.parseRange(null));
    }
}
