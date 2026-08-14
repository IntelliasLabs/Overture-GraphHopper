package com.graphhopper.reader.overture.parser.parquet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.graphhopper.reader.overture.access.restriction.AccessType;
import com.graphhopper.reader.overture.access.restriction.OvertureAccessRestriction;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class OvertureParquetAccessRestrictionTest extends AbstractOvertureParserTest {

    @Test
    @DisplayName("parseRestriction: should return a list with valid restriction when data is correct")
    void parseRestriction_success() {
        GenericRecord restrictionRecord = mock(GenericRecord.class);
        mockField(restrictionRecord, OvertureSchema.Restriction.ACCESS_TYPE, "allowed");

        List<OvertureAccessRestriction> result = OvertureParquetHelper.parseRestriction(
                Collections.singletonList(restrictionRecord), SEGMENT_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(AccessType.ALLOWED, result.getFirst().getAccessType());
    }

    @Test
    @DisplayName("parseRestriction: should filter out restrictions with disallowed access types")
    void parseRestriction_filtersInvalidAccessType() {
        GenericRecord restrictionRecord = mock(GenericRecord.class);
        mockField(restrictionRecord, OvertureSchema.Restriction.ACCESS_TYPE, "unknown_type");

        List<OvertureAccessRestriction> result = OvertureParquetHelper.parseRestriction(
                Collections.singletonList(restrictionRecord), SEGMENT_ID);

        assertNotNull(result, "Should return empty list because the only item was filtered out");
    }

    @Test
    @DisplayName("parseRestriction: should correctly attach 'between' range and 'when' scope")
    void parseRestriction_withRangeAndScope() {
        GenericRecord restrictionRecord = mock(GenericRecord.class);
        mockField(restrictionRecord, OvertureSchema.Restriction.ACCESS_TYPE, "denied");

        List<Object> rangeList = Arrays.asList(0.0, 1.0);
        mockField(restrictionRecord, OvertureSchema.Scope.BETWEEN, rangeList);

        GenericRecord whenRec = mock(GenericRecord.class);
        mockField(whenRec, OvertureSchema.Scope.DURING, "24/7");
        mockField(restrictionRecord, OvertureSchema.Scope.WHEN, whenRec);

        List<OvertureAccessRestriction> result = OvertureParquetHelper.parseRestriction(
                Collections.singletonList(restrictionRecord), SEGMENT_ID);

        assertNotNull(result);
        OvertureAccessRestriction restriction = result.getFirst();

        assertNotNull(restriction.getBetween());
        assertEquals(0.0, restriction.getBetween().getStart());

        assertNotNull(restriction.getWhen());
        assertEquals("24/7", restriction.getWhen().getDuring());
    }

    @Test
    @DisplayName("parseRestriction: should handle mixed valid and invalid records in the list")
    void parseRestriction_resilientToMixedInput() {
        GenericRecord validRecord = mock(GenericRecord.class);
        mockField(validRecord, OvertureSchema.Restriction.ACCESS_TYPE, "allowed");

        List<Object> mixedInput = Arrays.asList(validRecord, "corrupt_data");

        List<OvertureAccessRestriction> result =
                OvertureParquetHelper.parseRestriction(mixedInput, SEGMENT_ID);

        assertNotNull(result);
        assertEquals(1, result.size(), "Should skip 'corrupt_data' and keep the valid record");
    }

    @Test
    @DisplayName("parseRestriction: should return empty list for null or empty input")
    void parseRestriction_handlesNullInput() {
        assertNotNull(OvertureParquetHelper.parseRestriction(null, SEGMENT_ID));
        assertNotNull(OvertureParquetHelper.parseRestriction(null, SEGMENT_ID));
    }
}
