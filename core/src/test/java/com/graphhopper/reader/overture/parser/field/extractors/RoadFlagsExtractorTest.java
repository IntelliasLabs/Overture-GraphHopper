package com.graphhopper.reader.overture.parser.field.extractors;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.reader.overture.LinearlyReferencedRange;
import com.graphhopper.reader.overture.parser.OvertureParser;
import com.graphhopper.reader.overture.parser.features.SegmentFeature;
import com.graphhopper.reader.overture.road.flags.OvertureRoadFlags;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.graphhopper.reader.overture.parser.field.extractors.RoadFlagsExtractor.extractRoadFlags;
import static org.junit.jupiter.api.Assertions.*;

public class RoadFlagsExtractorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    @DisplayName("Return empty list if properties list is null")
    public void testExtractRoadFlagsNullPropertiesList() throws JsonProcessingException {
        JsonNode jsonNullPropertiesList = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": null
                        }
                """);

        assertNotNull(extractRoadFlags(jsonNullPropertiesList, "a737f685-b66f-41f1-9ef7-1909ecd15195"));
    }

    @Test
    @DisplayName("Return empty list if properties list is missed")
    public void testExtractRoadFlagsMissingPropertiesList() throws JsonProcessingException {
        JsonNode jsonMissingPropertiesList = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195"
                        }
                """);

        assertNotNull(extractRoadFlags(jsonMissingPropertiesList, "a737f685-b66f-41f1-9ef7-1909ecd15195"));
    }

    @Test
    @DisplayName("Return empty list if properties list is empty")
    public void testExtractRoadFlagsEmptyPropertiesList() throws JsonProcessingException {
        JsonNode jsonEmptyPropertiesList = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {}
                        }
                """);

        assertNotNull(extractRoadFlags(jsonEmptyPropertiesList, "a737f685-b66f-41f1-9ef7-1909ecd15195"));
    }

    @Test
    @DisplayName("Return null for 'values' if 'values' is null type")
    public void testExtractRoadFlagsItemNullValuesList() throws JsonProcessingException {
        JsonNode jsonNullValuesList = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {
                                "road_flags" : [
                                    {
                                       "values": null 
                                    }
                                ]
                            }
                        }
                """);

        OvertureRoadFlags overtureRoadFlags = extractRoadFlags(jsonNullValuesList, "a737f685-b66f-41f1-9ef7-1909ecd15195").getFirst();
        assertFalse(overtureRoadFlags.isAbandoned());
        assertFalse(overtureRoadFlags.isBridge());
        assertFalse(overtureRoadFlags.isCovered());
        assertFalse(overtureRoadFlags.isIndoor());
        assertFalse(overtureRoadFlags.isTunnel());
        assertFalse(overtureRoadFlags.isUnderConstruction());
    }

    @Test
    @DisplayName("Return correct flags value for 'values'")
    public void testExtractRoadFlagsItemCorrectValuesList() throws JsonProcessingException {
        JsonNode jsonNotArrayTypeValuesList = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {
                                "road_flags" : [
                                    {
                                       "values": ["is_indoor", "is_covered"]
                                    }
                                ]
                            }
                        }
                """);

        OvertureRoadFlags overtureRoadFlags = extractRoadFlags(jsonNotArrayTypeValuesList, "a737f685-b66f-41f1-9ef7-1909ecd15195").getFirst();
        assertFalse(overtureRoadFlags.isAbandoned());
        assertFalse(overtureRoadFlags.isBridge());
        assertTrue(overtureRoadFlags.isCovered());
        assertTrue(overtureRoadFlags.isIndoor());
        assertFalse(overtureRoadFlags.isTunnel());
        assertFalse(overtureRoadFlags.isUnderConstruction());
        assertNull(overtureRoadFlags.getBetween());
    }

    @Test
    @DisplayName("Return null for 'values' if 'values' is empty list")
    public void testExtractRoadFlagsItemIsCorrectValuesList() throws JsonProcessingException {
        JsonNode jsonNotArrayTypeValuesList = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {
                                "road_flags" : [
                                    {
                                       "values": ["is_indoor", "is_covered"]
                                    }
                                ]
                            }
                        }
                """);

        OvertureRoadFlags overtureRoadFlags = extractRoadFlags(jsonNotArrayTypeValuesList, "a737f685-b66f-41f1-9ef7-1909ecd15195").getFirst();
        assertFalse(overtureRoadFlags.isAbandoned());
        assertFalse(overtureRoadFlags.isBridge());
        assertTrue(overtureRoadFlags.isCovered());
        assertTrue(overtureRoadFlags.isIndoor());
        assertFalse(overtureRoadFlags.isTunnel());
        assertFalse(overtureRoadFlags.isUnderConstruction());
        assertNull(overtureRoadFlags.getBetween());
    }

    @Test
    @DisplayName("Return null for 'between' if 'between' is null type")
    public void testExtractRoadFlagsItemNullBetweenList() throws JsonProcessingException {
        JsonNode jsonNullValuesList = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {
                                "road_flags" : [
                                    {
                                       "between": null 
                                    }
                                ]
                            }
                        }
                """);

        assertNull(extractRoadFlags(jsonNullValuesList, "a737f685-b66f-41f1-9ef7-1909ecd15195").getFirst().getBetween());
    }

    @Nested
    class testUsingLogger {
        Logger logger = (Logger) LoggerFactory.getLogger(OvertureParser.class);
        ListAppender<ILoggingEvent> logList;

        @BeforeEach
        public void cleanList() {
            logList = new ListAppender<>();
            logList.start();
            logger.addAppender(logList);
        }

        @Test
        @DisplayName("Return empty list if road_surface list is empty and logged msg about 'values' is empty array")
        public void testExtractRoadFlagsEmptyItemsList() throws JsonProcessingException {
            JsonNode jsonEmptyItemsList = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {
                                "road_flags" : []
                            }
                        }
                """);

            assertNotNull(extractRoadFlags(jsonEmptyItemsList, "a737f685-b66f-41f1-9ef7-1909ecd15195"));
            assertTrue(logList.list
                    .stream().anyMatch(e ->
                            e.getLevel().toString().equals("WARN") &&
                                    e.getFormattedMessage().equals("'" + SegmentFeature.PROPERTIES.name() + "' 'road_flags' list is empty in feature id: 'a737f685-b66f-41f1-9ef7-1909ecd15195' .")));
        }

        @Test
        @DisplayName("Return null for 'values' if 'values' is empty type and logged msg about 'values' is empty array")
        public void testExtractRoadFlagsItemIsEmptyValuesList() throws JsonProcessingException {
            JsonNode jsonEmptyValuesList = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {
                                "road_flags" : [
                                    {
                                       "values": []
                                    }
                                ]
                            }
                        }
                """);

            OvertureRoadFlags overtureRoadFlags = extractRoadFlags(jsonEmptyValuesList, "a737f685-b66f-41f1-9ef7-1909ecd15195").getFirst();
            assertFalse(overtureRoadFlags.isAbandoned());
            assertFalse(overtureRoadFlags.isBridge());
            assertFalse(overtureRoadFlags.isCovered());
            assertFalse(overtureRoadFlags.isIndoor());
            assertFalse(overtureRoadFlags.isTunnel());
            assertFalse(overtureRoadFlags.isUnderConstruction());
            assertTrue(logList.list
                    .stream().anyMatch(e ->
                            e.getLevel().toString().equals("WARN") &&
                                    e.getFormattedMessage().equals("'FEATURE' 'values' list is empty in feature id: 'a737f685-b66f-41f1-9ef7-1909ecd15195' .")));
        }

        @Test
        @DisplayName("Return null for 'values' if 'values' isn't array type and logged msg about 'values' isn't array type")
        public void testExtractRoadFlagsItemValuesIsNotArrayType() throws JsonProcessingException {
            JsonNode jsonNotArrayTypeValuesList = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {
                                "road_flags" : [
                                    {
                                       "values": {}
                                    }
                                ]
                            }
                        }
                """);

            OvertureRoadFlags overtureRoadFlags = extractRoadFlags(jsonNotArrayTypeValuesList, "a737f685-b66f-41f1-9ef7-1909ecd15195").getFirst();
            assertFalse(overtureRoadFlags.isAbandoned());
            assertFalse(overtureRoadFlags.isBridge());
            assertFalse(overtureRoadFlags.isCovered());
            assertFalse(overtureRoadFlags.isIndoor());
            assertFalse(overtureRoadFlags.isTunnel());
            assertFalse(overtureRoadFlags.isUnderConstruction());
            assertNull(overtureRoadFlags.getBetween());
            assertTrue(logList.list
                    .stream().anyMatch(e ->
                            e.getLevel().toString().equals("WARN") &&
                                    e.getFormattedMessage().equals("'FEATURE' 'values' field isn't of array type in feature id: 'a737f685-b66f-41f1-9ef7-1909ecd15195' .")));
        }

        @Test
        @DisplayName("Return null for 'between' if 'between' is empty list")
        public void testExtractRoadFlagsItemBetweenIsEmptyList() throws JsonProcessingException {
            JsonNode jsonNotArrayTypeValuesList = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {
                                "road_flags" : [
                                    {
                                       "values": ["is_indoor", "is_covered"],
                                       "between": []
                                    }
                                ]
                            }
                        }
                """);

            OvertureRoadFlags overtureRoadFlags = extractRoadFlags(jsonNotArrayTypeValuesList, "a737f685-b66f-41f1-9ef7-1909ecd15195").getFirst();
            assertFalse(overtureRoadFlags.isAbandoned());
            assertFalse(overtureRoadFlags.isBridge());
            assertTrue(overtureRoadFlags.isCovered());
            assertTrue(overtureRoadFlags.isIndoor());
            assertFalse(overtureRoadFlags.isTunnel());
            assertFalse(overtureRoadFlags.isUnderConstruction());
            assertNull(overtureRoadFlags.getBetween());
            assertTrue(logList.list
                    .stream().anyMatch(e ->
                            e.getLevel().toString().equals("WARN") &&
                                    e.getFormattedMessage().equals("'FEATURE' 'between' list is empty in feature id: 'a737f685-b66f-41f1-9ef7-1909ecd15195' .")));
        }

        @Test
        @DisplayName("Return null for 'between' if 'between' contains non-unique values and logged msg about 'between' list elements contains non-unique values")
        public void testExtractRoadFlagsItemInvalidBetweenListNonUniqueElements() throws JsonProcessingException {
            JsonNode jsonUnvalidOvertureRoadFlagsBetweenValuesStringType = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {
                                "road_flags" : [
                                    {
                                        "values": ["is_indoor"],
                                        "between": [
                                            0.317118591,
                                            0.317118591
                                        ]
                                    }               
                                ]
                            }
                        }
                """);

            OvertureRoadFlags overtureRoadFlags = extractRoadFlags(jsonUnvalidOvertureRoadFlagsBetweenValuesStringType, "a737f685-b66f-41f1-9ef7-1909ecd15195").getFirst();
            assertFalse(overtureRoadFlags.isAbandoned());
            assertFalse(overtureRoadFlags.isBridge());
            assertFalse(overtureRoadFlags.isCovered());
            assertTrue(overtureRoadFlags.isIndoor());
            assertFalse(overtureRoadFlags.isTunnel());
            assertFalse(overtureRoadFlags.isUnderConstruction());
            assertNull(overtureRoadFlags.getBetween());
            assertTrue(logList.list
                    .stream().anyMatch(e ->
                            e.getLevel().toString().equals("WARN") &&
                                    e.getFormattedMessage().equals("'" + SegmentFeature.ROAD_FLAGS.name() + "' 'between' list contains non-unique values for feature id: 'a737f685-b66f-41f1-9ef7-1909ecd15195' .")));
        }

        @Test
        @DisplayName("Return null for 'between' if 'between' contains invalid values and logged msg about 'between' list elements contains invalid values")
        public void testExtractRoadFlagsItemInvalidBetweenList() throws JsonProcessingException {
            String templateJson = """
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {
                                "road_flags" : [
                                    {
                                        "values": ["is_indoor"],
                                        "between": [
                                            %f,
                                            %f
                                        ]
                                    }               
                                ]
                            }
                        }
                """;
            JsonNode jsonInvalidBetweenListElementsStartMoreThanEnd = MAPPER.readTree(String.format(templateJson, 0.4, 0.317118591));
            JsonNode jsonInvalidBetweenListElementsStartLessThanZero = MAPPER.readTree(String.format(templateJson, -0.3, 0.317118591));
            JsonNode jsonInvalidBetweenListElementsStartMoreThanOne = MAPPER.readTree(String.format(templateJson, 1.1, 0.317118591));
            JsonNode jsonInvalidBetweenListElementsEndLessThanZero = MAPPER.readTree(String.format(templateJson, 0.1, -0.3));
            JsonNode jsonInvalidBetweenListElementsEndMoreThanOne = MAPPER.readTree(String.format(templateJson, 0.1, 1.1));

            List<OvertureRoadFlags> overtureRoadFlagsList = List.of(
                extractRoadFlags(jsonInvalidBetweenListElementsStartMoreThanEnd, "a737f685-b66f-41f1-9ef7-1909ecd15195").getFirst(),
                extractRoadFlags(jsonInvalidBetweenListElementsStartLessThanZero, "a737f685-b66f-41f1-9ef7-1909ecd15195").getFirst(),
                extractRoadFlags(jsonInvalidBetweenListElementsStartMoreThanOne, "a737f685-b66f-41f1-9ef7-1909ecd15195").getFirst(),
                extractRoadFlags(jsonInvalidBetweenListElementsEndLessThanZero, "a737f685-b66f-41f1-9ef7-1909ecd15195").getFirst(),
                extractRoadFlags(jsonInvalidBetweenListElementsEndMoreThanOne, "a737f685-b66f-41f1-9ef7-1909ecd15195").getFirst());
            for (var overtureRoadFlag: overtureRoadFlagsList) {
                assertFalse(overtureRoadFlag.isAbandoned());
                assertFalse(overtureRoadFlag.isBridge());
                assertFalse(overtureRoadFlag.isCovered());
                assertTrue(overtureRoadFlag.isIndoor());
                assertFalse(overtureRoadFlag.isTunnel());
                assertFalse(overtureRoadFlag.isUnderConstruction());
                assertNull(overtureRoadFlag.getBetween());
            }

            assertTrue(logList.list
                    .stream().allMatch(e ->
                            e.getLevel().toString().equals("WARN") &&
                                    e.getFormattedMessage().equals("'" + SegmentFeature.ROAD_FLAGS.name() + "' 'between' list isn't has valid values for feature id: 'a737f685-b66f-41f1-9ef7-1909ecd15195' .")));
        }

    }

    @Test
    @DisplayName("Return expected values for 'road_flags' list")
    public void testExtractRoadSurfaceItemValidBetweenList() throws JsonProcessingException {
        List<OvertureRoadFlags> expectedValidOvertureRoadSurfaces =
                List.of(
                        new OvertureRoadFlags(false, false, false, false, true, false, new LinearlyReferencedRange(0.0, 0.317118591)),
                        new OvertureRoadFlags(false, false, false, false, false, false, new LinearlyReferencedRange(0.317118591, 1.0))
                );
        JsonNode jsonMissedBetweenList = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {
                                "road_flags" : [
                                    {
                                        "values": ["is_covered"],
                                        "between": [
                                            0.0,
                                            0.317118591
                                        ]
                                    },
                                    {
                                        "between": [
                                            0.317118591,
                                            1.0
                                        ]
                                    }                 
                                ]
                            }
                        }
                """);

        assertEquals(expectedValidOvertureRoadSurfaces, extractRoadFlags(jsonMissedBetweenList, "a737f685-b66f-41f1-9ef7-1909ecd15195"));

    }

}
