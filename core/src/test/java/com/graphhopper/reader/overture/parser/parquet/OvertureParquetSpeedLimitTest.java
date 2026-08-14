package com.graphhopper.reader.overture.parser.parquet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.graphhopper.reader.overture.common.speed.OvertureSpeedLimit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class OvertureParquetSpeedLimitTest extends AbstractOvertureParserTest {

    @Test
    @DisplayName("parseSpeedLimits: should return valid list with max speed and scope")
    void parseSpeedLimits_success() {
        GenericRecord maxSpeedRec = mock(GenericRecord.class);
        mockField(maxSpeedRec, OvertureSchema.Speed.VALUE, 100.0);
        mockField(maxSpeedRec, OvertureSchema.Speed.UNIT, "km/h");

        GenericRecord speedLimitRec = mock(GenericRecord.class);
        mockField(speedLimitRec, OvertureSchema.Speed.MAX_SPEED, maxSpeedRec);
        mockField(speedLimitRec, OvertureSchema.Speed.IS_VARIABLE, true);

        mockField(speedLimitRec, OvertureSchema.Scope.BETWEEN, Arrays.asList(0.1, 0.9));

        List<OvertureSpeedLimit> result = OvertureParquetHelper.parseSpeedLimits(
                Collections.singletonList(speedLimitRec), SEGMENT_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        OvertureSpeedLimit limit = result.getFirst();

        assertEquals(100.0, limit.getMaxSpeed().getValue());
        assertTrue(limit.isMaxSpeedVariable());
        assertNotNull(limit.getBetween());
        assertEquals(0.1, limit.getBetween().getStart());
    }

    @Test
    @DisplayName(
            "parseSpeedLimits: should return empty list if both max and min speeds are invalid or missing")
    void parseSpeedLimits_filtersOutEmptySpeeds() {
        GenericRecord speedLimitRec = mock(GenericRecord.class);
        mockField(speedLimitRec, OvertureSchema.Speed.IS_VARIABLE, true);

        List<OvertureSpeedLimit> result = OvertureParquetHelper.parseSpeedLimits(
                Collections.singletonList(speedLimitRec), SEGMENT_ID);

        assertNotNull(result, "Should return empty list because no valid speeds were found");
    }

    @Test
    @DisplayName("parseSpeedLimits: should handle multiple records and skip corrupt ones")
    void parseSpeedLimits_resilientToMixedInput() {
        GenericRecord validRec = mock(GenericRecord.class);
        GenericRecord maxSpeed = mock(GenericRecord.class);
        mockField(maxSpeed, OvertureSchema.Speed.VALUE, 50.0);
        mockField(maxSpeed, OvertureSchema.Speed.UNIT, "km/h");
        mockField(validRec, OvertureSchema.Speed.MAX_SPEED, maxSpeed);

        Object corruptRec = "this is not a record";

        List<Object> input = Arrays.asList(validRec, corruptRec);

        List<OvertureSpeedLimit> result = OvertureParquetHelper.parseSpeedLimits(input, SEGMENT_ID);

        assertNotNull(result);
        assertEquals(1, result.size(), "Should skip corrupt String record and keep valid one");
    }

    @Test
    @DisplayName("parseSpeedLimits: should correctly pass when scope to parseScope")
    void parseSpeedLimits_withComplexScope() {
        GenericRecord speedLimitRec = mock(GenericRecord.class);
        GenericRecord maxSpeed = mock(GenericRecord.class);
        mockField(maxSpeed, OvertureSchema.Speed.VALUE, 30.0);
        mockField(maxSpeed, OvertureSchema.Speed.UNIT, "km/h");
        mockField(speedLimitRec, OvertureSchema.Speed.MAX_SPEED, maxSpeed);

        GenericRecord whenRec = mock(GenericRecord.class);
        mockField(whenRec, OvertureSchema.Scope.DURING, "Mo-Fr");
        mockField(speedLimitRec, OvertureSchema.Scope.WHEN, whenRec);

        List<OvertureSpeedLimit> result = OvertureParquetHelper.parseSpeedLimits(
                Collections.singletonList(speedLimitRec), SEGMENT_ID);

        assertNotNull(result);
        assertNotNull(result.getFirst().getWhen());
        assertEquals("Mo-Fr", result.getFirst().getWhen().getDuring());
    }

    @Test
    @DisplayName("parseSpeedLimits: should return empty list for null input or empty list")
    void parseSpeedLimits_nullInput() {
        assertNotNull(OvertureParquetHelper.parseSpeedLimits(null, SEGMENT_ID));
        assertNotNull(OvertureParquetHelper.parseSpeedLimits(null, SEGMENT_ID));
    }
}
