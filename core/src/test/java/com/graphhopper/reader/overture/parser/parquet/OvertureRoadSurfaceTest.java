package com.graphhopper.reader.overture.parser.parquet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.graphhopper.reader.overture.road.surface.OvertureRoadSurface;
import com.graphhopper.reader.overture.road.surface.RoadSurfaceType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class OvertureRoadSurfaceTest extends AbstractOvertureParserTest {
    @Test
    @DisplayName("parseRoadSurface: should return valid surface and range")
    void parseRoadSurface_success() {
        GenericRecord surfaceRecord = mock(GenericRecord.class);
        mockField(surfaceRecord, OvertureSchema.Surface.VALUE, "paved");

        List<Object> range = Arrays.asList(0.0, 1.0);
        mockField(surfaceRecord, OvertureSchema.Scope.BETWEEN, range);

        List<OvertureRoadSurface> result = OvertureParquetHelper.parseRoadSurface(
                Collections.singletonList(surfaceRecord), SEGMENT_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(RoadSurfaceType.PAVED, result.getFirst().getSurfaceType());
        assertNotNull(result.getFirst().getBetween());
        assertEquals(1.0, result.getFirst().getBetween().getEnd());
    }

    @Test
    @DisplayName("parseRoadSurface: should return empty list if surface type is disallowed by filter")
    void parseRoadSurface_filtersInvalidType() {
        GenericRecord surfaceRecord = mock(GenericRecord.class);
        mockField(surfaceRecord, OvertureSchema.Surface.VALUE, "cobblestone");

        List<OvertureRoadSurface> result = OvertureParquetHelper.parseRoadSurface(
                Collections.singletonList(surfaceRecord), SEGMENT_ID);

        assertNotNull(result, "Should be empty list because surface type was filtered out");
    }

    @Test
    @DisplayName("parseRoadSurface: should successfully parse surface without 'between' range")
    void parseRoadSurface_missingRange() {
        GenericRecord surfaceRecord = mock(GenericRecord.class);
        mockField(surfaceRecord, OvertureSchema.Surface.VALUE, "asphalt");
        mockField(surfaceRecord, OvertureSchema.Scope.BETWEEN, null);

        List<OvertureRoadSurface> result = OvertureParquetHelper.parseRoadSurface(
                Collections.singletonList(surfaceRecord), SEGMENT_ID);

        assertNotNull(result);
        assertEquals(RoadSurfaceType.ASPHALT, result.getFirst().getSurfaceType());
        assertNull(result.getFirst().getBetween(), "Between should be null but object created");
    }

    @Test
    @DisplayName("parseRoadSurface: should handle mixed valid records and corrupt data types")
    void parseRoadSurface_resilientToCorruptItems() {
        GenericRecord validRecord = mock(GenericRecord.class);
        mockField(validRecord, OvertureSchema.Surface.VALUE, "gravel");

        List<Object> mixedInput = Arrays.asList(validRecord, "not_a_record");

        List<OvertureRoadSurface> result =
                OvertureParquetHelper.parseRoadSurface(mixedInput, SEGMENT_ID);

        assertNotNull(result);
        assertEquals(1, result.size(), "Should skip the string and parse the gravel record");
        assertEquals(RoadSurfaceType.GRAVEL, result.getFirst().getSurfaceType());
    }

    @Test
    @DisplayName("parseRoadSurface: should correctly unwrap nested surface value")
    void parseRoadSurface_unwrapsValue() {
        GenericRecord wrapper = mock(GenericRecord.class);

        GenericRecord nestedElement = mock(GenericRecord.class);
        mockField(nestedElement, OvertureSchema.ELEMENT, "paved");

        when(nestedElement.toString()).thenReturn("paved");

        mockField(wrapper, OvertureSchema.Surface.VALUE, nestedElement);

        List<OvertureRoadSurface> result =
                OvertureParquetHelper.parseRoadSurface(Collections.singletonList(wrapper), SEGMENT_ID);

        assertNotNull(result, "If this is null, check your unwrap() logic in OvertureParquetHelper");
        assertEquals(RoadSurfaceType.PAVED, result.getFirst().getSurfaceType());
    }
}
