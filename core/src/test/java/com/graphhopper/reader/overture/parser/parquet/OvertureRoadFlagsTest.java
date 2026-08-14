package com.graphhopper.reader.overture.parser.parquet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import com.graphhopper.reader.overture.road.flags.OvertureRoadFlags;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class OvertureRoadFlagsTest extends AbstractOvertureParserTest {
    @Test
    @DisplayName("parseRoadFlags: should map present strings to true and missing to false")
    void parseRoadFlags_mappingSuccess() {
        GenericRecord wrapper = mock(GenericRecord.class);

        List<Object> values =
                Arrays.asList(OvertureSchema.Flags.IS_BRIDGE, OvertureSchema.Flags.IS_TUNNEL);
        mockField(wrapper, OvertureSchema.Flags.VALUES, values);

        List<OvertureRoadFlags> result =
                OvertureParquetHelper.parseRoadFlags(Collections.singletonList(wrapper), SEGMENT_ID);

        assertNotNull(result);
        OvertureRoadFlags flags = result.getFirst();

        assertTrue(flags.isBridge(), "is_bridge should be true");
        assertTrue(flags.isTunnel(), "is_tunnel should be true");
        assertFalse(flags.isUnderConstruction(), "is_under_construction should be false");
        assertFalse(flags.isAbandoned(), "is_abandoned should be false");
    }

    @Test
    @DisplayName("parseRoadFlags: should return empty list if the values list is empty")
    void parseRoadFlags_emptyValuesReturnsNull() {
        GenericRecord wrapper = mock(GenericRecord.class);
        mockField(wrapper, OvertureSchema.Flags.VALUES, Collections.emptyList());

        List<OvertureRoadFlags> result =
                OvertureParquetHelper.parseRoadFlags(Collections.singletonList(wrapper), SEGMENT_ID);

        assertNotNull(result, "Should return empty list because no active flags were found");
    }

    @Test
    @DisplayName("parseRoadFlags: should correctly unwrap nested strings inside the values list")
    void parseRoadFlags_deepUnwrapValues() {
        GenericRecord wrapper = mock(GenericRecord.class);

        GenericRecord nestedElement = mock(GenericRecord.class);
        mockField(nestedElement, OvertureSchema.ELEMENT, OvertureSchema.Flags.IS_COVERED);

        List<Object> values = Collections.singletonList(nestedElement);
        mockField(wrapper, OvertureSchema.Flags.VALUES, values);

        List<OvertureRoadFlags> result =
                OvertureParquetHelper.parseRoadFlags(Collections.singletonList(wrapper), SEGMENT_ID);

        assertNotNull(result);
        assertTrue(result.getFirst().isCovered());
    }

    @Test
    @DisplayName("parseRoadFlags: should correctly attach between range if present")
    void parseRoadFlags_withBetweenRange() {
        GenericRecord wrapper = mock(GenericRecord.class);
        mockField(
                wrapper,
                OvertureSchema.Flags.VALUES,
                Collections.singletonList(OvertureSchema.Flags.IS_INDOOR));

        List<Object> range = Arrays.asList(0.3, 0.7);
        mockField(wrapper, OvertureSchema.Scope.BETWEEN, range);

        List<OvertureRoadFlags> result =
                OvertureParquetHelper.parseRoadFlags(Collections.singletonList(wrapper), SEGMENT_ID);

        assertNotNull(result);
        LinearlyReferencedRange between = result.getFirst().getBetween();
        assertNotNull(between);
        assertEquals(0.3, between.getStart());
        assertEquals(0.7, between.getEnd());
    }

    @Test
    @DisplayName("parseRoadFlags: should be resilient to null elements in the values list")
    void parseRoadFlags_handlesNullsInList() {
        GenericRecord wrapper = mock(GenericRecord.class);

        List<Object> values = Arrays.asList(OvertureSchema.Flags.IS_ABANDONED, null);
        mockField(wrapper, OvertureSchema.Flags.VALUES, values);

        List<OvertureRoadFlags> result =
                OvertureParquetHelper.parseRoadFlags(Collections.singletonList(wrapper), SEGMENT_ID);

        assertNotNull(result);
        assertTrue(result.getFirst().isAbandoned());
        assertEquals(1, result.size());
    }
}
