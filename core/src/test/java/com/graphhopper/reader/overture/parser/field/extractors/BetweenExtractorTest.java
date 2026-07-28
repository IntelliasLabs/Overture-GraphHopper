package com.graphhopper.reader.overture.parser.field.extractors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.reader.overture.LinearlyReferencedRange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BetweenExtractorTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Should parse valid between range")
    void parseBetween_Valid() throws Exception {
        JsonNode node = mapper.readTree("[0.1, 0.8]");
        LinearlyReferencedRange range =
                BetweenExtractor.extractBetween(node, null, "a737f685-b66f-41f1-9ef7-1909ecd15195");

        assertNotNull(range);
        assertEquals(0.1, range.getStart());
        assertEquals(0.8, range.getEnd());
    }

    @Test
    @DisplayName("Should reject invalid array sizes for between")
    void parseBetween_InvalidSize() throws Exception {
        assertNull(BetweenExtractor.extractBetween(
                mapper.readTree("[0.5]"), null, "a737f685-b66f-41f1-9ef7-1909ecd15195"));
        assertNull(BetweenExtractor.extractBetween(
                mapper.readTree("[]"), null, "a737f685-b66f-41f1-9ef7-1909ecd15195"));
        assertNull(BetweenExtractor.extractBetween(
                mapper.readTree("[0.5, null]"), null, "a737f685-b66f-41f1-9ef7-1909ecd15195"));
        assertNull(BetweenExtractor.extractBetween(null, null, "a737f685-b66f-41f1-9ef7-1909ecd15195"));
    }

    @Test
    @DisplayName("Should validate according to Overture schema: range [0, 1]")
    void parseBetween_SchemaValidation() throws Exception {
        assertNull(BetweenExtractor.extractBetween(
                mapper.readTree("[-0.01, 0.5]"), null, "a737f685-b66f-41f1-9ef7-1909ecd15195"));
        assertNull(BetweenExtractor.extractBetween(
                mapper.readTree("[0.5, 1.0001]"), null, "a737f685-b66f-41f1-9ef7-1909ecd15195"));

        assertNull(BetweenExtractor.extractBetween(
                mapper.readTree("[0.4, 0.4]"), null, "a737f685-b66f-41f1-9ef7-1909ecd15195"));
    }

    @Test
    @DisplayName("Should reject non-numeric values")
    void parseBetween_NonNumeric() throws Exception {
        assertNull(BetweenExtractor.extractBetween(
                mapper.readTree("[\"0.1\", \"0.8\"]"), null, "a737f685-b66f-41f1-9ef7-1909ecd15195"));
        assertNull(BetweenExtractor.extractBetween(
                mapper.readTree("[0.1, \"text\"]"), null, "a737f685-b66f-41f1-9ef7-1909ecd15195"));
    }

    @Test
    @DisplayName("Should preserve precision for long decimals")
    void parseBetween_Precision() throws Exception {
        JsonNode node = mapper.readTree("[0.123456789, 0.987654321]");
        LinearlyReferencedRange range =
                BetweenExtractor.extractBetween(node, null, "a737f685-b66f-41f1-9ef7-1909ecd15195");

        assertEquals(0.123456789, range.getStart(), 1e-9);
        assertEquals(0.987654321, range.getEnd(), 1e-9);
    }
}
