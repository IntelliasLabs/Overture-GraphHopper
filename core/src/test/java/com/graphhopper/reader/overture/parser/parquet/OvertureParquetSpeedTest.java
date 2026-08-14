package com.graphhopper.reader.overture.parser.parquet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.graphhopper.reader.overture.common.speed.OvertureSpeed;
import com.graphhopper.reader.overture.common.speed.SpeedUnit;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class OvertureParquetSpeedTest extends AbstractOvertureParserTest {

    @Test
    @DisplayName("parseSpeed: should return valid speed when value and unit are correct")
    void parseSpeed_success() {
        GenericRecord speedRec = mock(GenericRecord.class);
        mockField(speedRec, OvertureSchema.Speed.VALUE, 50.0);
        mockField(speedRec, OvertureSchema.Speed.UNIT, "km/h");

        OvertureSpeed result = OvertureParquetHelper.parseSpeed(speedRec, SEGMENT_ID);

        assertNotNull(result);
        assertEquals(50.0, result.getValue());
        assertEquals(SpeedUnit.KM_H, result.getUnit());
    }

    @Test
    @DisplayName("parseSpeed: should handle Integer speed values correctly")
    void parseSpeed_handlesIntegerValues() {
        GenericRecord speedRec = mock(GenericRecord.class);
        mockField(speedRec, OvertureSchema.Speed.VALUE, 60);
        mockField(speedRec, OvertureSchema.Speed.UNIT, "mph");

        OvertureSpeed result = OvertureParquetHelper.parseSpeed(speedRec, SEGMENT_ID);

        assertNotNull(result);
        assertEquals(60.0, result.getValue());
        assertEquals(SpeedUnit.MPH, result.getUnit());
    }

    @Test
    @DisplayName("parseSpeed: should return null for disallowed speed units")
    void parseSpeed_filtersInvalidUnit() {
        GenericRecord speedRec = mock(GenericRecord.class);
        mockField(speedRec, OvertureSchema.Speed.VALUE, 20.0);
        mockField(speedRec, OvertureSchema.Speed.UNIT, "knots");

        OvertureSpeed result = OvertureParquetHelper.parseSpeed(speedRec, SEGMENT_ID);

        assertNull(result, "Should return null because 'knots' unit is filtered out");
    }

    @Test
    @DisplayName("parseSpeed: should return null if value is negative (invalid speed)")
    void parseSpeed_invalidNegativeValue() {
        GenericRecord speedRec = mock(GenericRecord.class);
        mockField(speedRec, OvertureSchema.Speed.VALUE, -10.0);
        mockField(speedRec, OvertureSchema.Speed.UNIT, "km/h");

        OvertureSpeed result = OvertureParquetHelper.parseSpeed(speedRec, SEGMENT_ID);

        assertNull(result, "Should return null for negative speed values");
    }

    @Test
    @DisplayName("parseSpeed: should return null if speed value is missing")
    void parseSpeed_missingValue() {
        GenericRecord speedRec = mock(GenericRecord.class);
        mockField(speedRec, OvertureSchema.Speed.VALUE, null);
        mockField(speedRec, OvertureSchema.Speed.UNIT, "km/h");

        OvertureSpeed result = OvertureParquetHelper.parseSpeed(speedRec, SEGMENT_ID);

        assertNull(result);
    }

    @Test
    @DisplayName("parseSpeed: should correctly unwrap nested 'element' for speed value")
    void parseSpeed_unwrapsNestedValue() {
        GenericRecord speedRec = mock(GenericRecord.class);
        mockField(speedRec, OvertureSchema.Speed.UNIT, "km/h");

        GenericRecord wrapper = mock(GenericRecord.class);
        mockField(wrapper, OvertureSchema.ELEMENT, 80.0);
        mockField(speedRec, OvertureSchema.Speed.VALUE, wrapper);

        OvertureSpeed result = OvertureParquetHelper.parseSpeed(speedRec, SEGMENT_ID);

        assertNotNull(result);
        assertEquals(80.0, result.getValue());
    }
}
