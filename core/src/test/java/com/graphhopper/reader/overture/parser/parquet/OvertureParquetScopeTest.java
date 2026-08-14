package com.graphhopper.reader.overture.parser.parquet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.graphhopper.reader.overture.access.restriction.PropertyScopeContainer;
import com.graphhopper.reader.overture.access.restriction.scope.containers.DimensionRestriction;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelMode;
import com.graphhopper.reader.overture.access.restriction.scope.containers.VehicleAttributes;
import java.util.Arrays;
import java.util.List;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class OvertureParquetScopeTest extends AbstractOvertureParserTest {

    @Test
    @DisplayName("parseScope: should return null when input is not a GenericRecord")
    void parseScope_returnsNullForInvalidInput() {
        assertNull(OvertureParquetHelper.parseScope("not-a-record", SEGMENT_ID));
        assertNull(OvertureParquetHelper.parseScope(null, SEGMENT_ID));
    }

    @Test
    @DisplayName("parseScope: should successfully extract 'during' field")
    void parseScope_extractsDuringString() {
        GenericRecord record = mock(GenericRecord.class);
        mockField(record, OvertureSchema.Scope.DURING, "Mo-Fr 08:00-20:00");

        PropertyScopeContainer result = OvertureParquetHelper.parseScope(record, SEGMENT_ID);

        assertNotNull(result);
        assertEquals("Mo-Fr 08:00-20:00", result.getDuring());
    }

    @Test
    @DisplayName("parseScope: should set heading to null")
    void parseScope_filtersInvalidHeading() {
        GenericRecord record = mock(GenericRecord.class);
        mockField(record, OvertureSchema.Scope.HEADING, "u-turn");

        PropertyScopeContainer result = OvertureParquetHelper.parseScope(record, SEGMENT_ID);

        assertNull(result, "Heading should be null when filtered out");
    }

    @Test
    @DisplayName("parseScope: should return null if all fields are null or filtered out")
    void parseScope_returnsNullForEmptyData() {
        GenericRecord record = mock(GenericRecord.class);
        when(record.getSchema()).thenReturn(mock(Schema.class));

        PropertyScopeContainer result = OvertureParquetHelper.parseScope(record, SEGMENT_ID);

        assertNull(result, "Should return null if no useful data was parsed");
    }

    @Test
    @DisplayName("parseScope: should handle list with mixed valid and invalid travel modes")
    void parseScope_handlesMixedModes() {
        GenericRecord record = mock(GenericRecord.class);
        List<Object> mixedModes = Arrays.asList("car", "teleportation", null);
        mockField(record, OvertureSchema.Scope.MODE, mixedModes);

        PropertyScopeContainer result = OvertureParquetHelper.parseScope(record, SEGMENT_ID);

        assertNotNull(result);
        assertEquals(1, result.getMode().size());
        assertEquals(TravelMode.CAR, result.getMode().getFirst());
    }

    @Test
    @DisplayName("parseScope: should continue parsing if one vehicle attribute is corrupt")
    void parseScope_resilientToCorruptItems() {
        GenericRecord record = mock(GenericRecord.class);

        mockField(record, OvertureSchema.Scope.DURING, "Mo-Fr");

        GenericRecord validVehicle = mock(GenericRecord.class);
        mockField(validVehicle, OvertureSchema.Vehicle.DIMENSION, "weight");
        mockField(validVehicle, OvertureSchema.Vehicle.COMPARISON, "equal");
        mockField(validVehicle, OvertureSchema.Vehicle.VALUE, 10.5);

        List<Object> vehicles = Arrays.asList(validVehicle, true);
        mockField(record, OvertureSchema.Scope.VEHICLE, vehicles);

        PropertyScopeContainer result = OvertureParquetHelper.parseScope(record, SEGMENT_ID);

        assertNotNull(result, "Scope should be created because 'during' is set");
        assertEquals("Mo-Fr", result.getDuring());

        assertNotNull(result.getVehicle());
        assertEquals(1, result.getVehicle().size(), "Should contain only the valid vehicle attribute");
    }

    @Test
    @DisplayName("parseVehicleAttribute: should handle Integer values as Double")
    void parseVehicleAttribute_handlesIntegerAsDouble() {
        GenericRecord vRec = mock(GenericRecord.class);
        mockField(vRec, OvertureSchema.Vehicle.DIMENSION, "weight");
        mockField(vRec, OvertureSchema.Vehicle.COMPARISON, "equal");
        mockField(vRec, OvertureSchema.Vehicle.VALUE, 10);

        VehicleAttributes result = OvertureParquetHelper.parseVehicleAttribute(vRec, SEGMENT_ID);

        assertNotNull(result);
        assertEquals(10.0, result.getNumericQuantity(), 0.001);
    }

    @Test
    @DisplayName("parseVehicleAttribute: should return valid object when all fields are present")
    void parseVehicleAttribute_success() {
        GenericRecord vRec = mock(GenericRecord.class);
        mockField(vRec, OvertureSchema.Vehicle.DIMENSION, "weight");
        mockField(vRec, OvertureSchema.Vehicle.COMPARISON, "greater_than");
        mockField(vRec, OvertureSchema.Vehicle.UNIT, "ton");
        mockField(vRec, OvertureSchema.Vehicle.VALUE, 12.0);

        VehicleAttributes result = OvertureParquetHelper.parseVehicleAttribute(vRec, SEGMENT_ID);

        assertNotNull(result);
        assertEquals(DimensionRestriction.WEIGHT, result.getDimension());
        assertEquals(12.0, result.getNumericQuantity(), 0.001);
    }

    @Test
    @DisplayName("parseVehicleAttribute: should return null if 'value' field is missing or null")
    void parseVehicleAttribute_nullValueReturnsNull() {
        GenericRecord vRec = mock(GenericRecord.class);
        mockField(vRec, OvertureSchema.Vehicle.DIMENSION, "height");
        mockField(vRec, OvertureSchema.Vehicle.VALUE, null);

        VehicleAttributes result = OvertureParquetHelper.parseVehicleAttribute(vRec, SEGMENT_ID);

        assertNull(result, "Should return null if numeric value is missing");
    }
}
