package com.graphhopper.reader.overture.parser.parquet;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

public abstract class AbstractOvertureParserTest {

    protected static final String SEGMENT_ID = "way-123";

    /**
     * Helper to mock a field in a GenericRecord.
     * Fixes the NPE by providing a basic schema for the Field object.
     */
    protected void mockField(GenericRecord record, String fieldName, Object value) {
        Schema recordSchema = record.getSchema();
        if (recordSchema == null) {
            recordSchema = mock(Schema.class);
            when(record.getSchema()).thenReturn(recordSchema);
        }

        Schema fieldTypeSchema;
        if (value instanceof Double) {
            fieldTypeSchema = Schema.create(Schema.Type.DOUBLE);
        } else if (value instanceof Integer) {
            fieldTypeSchema = Schema.create(Schema.Type.INT);
        } else if (value instanceof Boolean) {
            fieldTypeSchema = Schema.create(Schema.Type.BOOLEAN);
        } else {
            fieldTypeSchema = Schema.create(Schema.Type.STRING);
        }

        Schema.Field field = new Schema.Field(fieldName, fieldTypeSchema, null, null);
        when(recordSchema.getField(fieldName)).thenReturn(field);

        when(record.get(fieldName)).thenReturn(value);
    }
}
