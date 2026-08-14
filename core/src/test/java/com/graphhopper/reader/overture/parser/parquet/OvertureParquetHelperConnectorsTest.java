package com.graphhopper.reader.overture.parser.parquet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.graphhopper.reader.overture.road.segment.OvertureConnector;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class OvertureParquetHelperConnectorsTest extends AbstractOvertureParserTest {

    @Test
    @DisplayName("parseConnectors: should parse multiple connectors")
    void parseMultiple() {
        GenericRecord wrapper1 = mock(GenericRecord.class);
        GenericRecord wrapper2 = mock(GenericRecord.class);
        GenericRecord wrapper3 = mock(GenericRecord.class);

        mockField(wrapper1, "connector_id", "c1");
        mockField(wrapper1, "at", 0.0);

        mockField(wrapper2, "connector_id", "c2");
        mockField(wrapper2, "at", 0.5);

        mockField(wrapper3, "connector_id", "c3");
        mockField(wrapper3, "at", 1.0);

        List<OvertureConnector> res = OvertureParquetHelper.parseConnectors(
                Arrays.asList(wrapper1, wrapper2, wrapper3), SEGMENT_ID);

        assertNotNull(res);
        assertEquals(3, res.size());
        assertEquals("c1", res.get(0).getConnectorId());
    }

    @Test
    @DisplayName("parseConnectors: should return empty list for empty connectors array")
    void parseEmptyArray() {
        assertNotNull(OvertureParquetHelper.parseConnectors(Collections.emptyList(), SEGMENT_ID));
    }

    @Test
    @DisplayName("parseConnectors: should return empty list when connectors missing")
    void parseMissing() {
        assertNotNull(OvertureParquetHelper.parseConnectors(null, SEGMENT_ID));
    }

    @Test
    @DisplayName("parseConnectors: should ignore invalid at entries and keep valid ones")
    void parseIgnoreInvalidAt() {
        GenericRecord ok = mock(GenericRecord.class);
        GenericRecord bad = mock(GenericRecord.class);
        GenericRecord ok2 = mock(GenericRecord.class);

        mockField(ok, "connector_id", "ok");
        mockField(ok, "at", 0.2);

        mockField(bad, "connector_id", "bad");
        mockField(bad, "at", "NaN");

        mockField(ok2, "connector_id", "also_ok");
        mockField(ok2, "at", 0.8);

        List<OvertureConnector> res =
                OvertureParquetHelper.parseConnectors(Arrays.asList(ok, bad, ok2), SEGMENT_ID);

        assertNotNull(res);
        assertEquals(2, res.size());
        assertEquals("ok", res.get(0).getConnectorId());
    }

    @Test
    @DisplayName("parseConnectors: should remove duplicate connectors with same at")
    void parseDuplicates() {
        GenericRecord a = mock(GenericRecord.class);
        GenericRecord b = mock(GenericRecord.class);
        GenericRecord c = mock(GenericRecord.class);

        mockField(a, "connector_id", "c");
        mockField(a, "at", 0.1);
        mockField(b, "connector_id", "c");
        mockField(b, "at", 0.1);
        mockField(c, "connector_id", "c");
        mockField(c, "at", 0.1);

        List<OvertureConnector> res =
                OvertureParquetHelper.parseConnectors(Arrays.asList(a, b, c), SEGMENT_ID);
        assertNotNull(res);
        assertEquals(1, res.size());
    }

    @Test
    @DisplayName("parseConnectors: should dedupe by at value not id")
    void parseDuplicatesAtValue() {
        GenericRecord a = mock(GenericRecord.class);
        GenericRecord b = mock(GenericRecord.class);
        GenericRecord c = mock(GenericRecord.class);

        mockField(a, "connector_id", "c1");
        mockField(a, "at", 0.1);
        mockField(b, "connector_id", "c2");
        mockField(b, "at", 0.1);
        mockField(c, "connector_id", "c3");
        mockField(c, "at", 0.1);

        List<OvertureConnector> res =
                OvertureParquetHelper.parseConnectors(Arrays.asList(a, b, c), SEGMENT_ID);
        assertNotNull(res);
        assertEquals(1, res.size());
    }

    @Test
    @DisplayName("parseConnectors: should allow same id when at differs")
    void parseDuplicatesIdValue() {
        GenericRecord a = mock(GenericRecord.class);
        GenericRecord b = mock(GenericRecord.class);
        GenericRecord c = mock(GenericRecord.class);

        mockField(a, "connector_id", "c");
        mockField(a, "at", 0.1);
        mockField(b, "connector_id", "c");
        mockField(b, "at", 0.2);
        mockField(c, "connector_id", "c");
        mockField(c, "at", 0.3);

        List<OvertureConnector> res =
                OvertureParquetHelper.parseConnectors(Arrays.asList(a, b, c), SEGMENT_ID);
        assertNotNull(res);
        assertEquals(3, res.size());
    }
}
