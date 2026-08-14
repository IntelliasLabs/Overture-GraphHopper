package com.graphhopper.reader.overture.parser.field.extractors;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.reader.overture.parser.OvertureParser;
import com.graphhopper.reader.overture.LinearlyReferencedRange;
import com.graphhopper.reader.overture.parser.features.SegmentFeature;
import com.graphhopper.reader.overture.road.surface.OvertureRoadSurface;
import com.graphhopper.reader.overture.road.surface.RoadSurfaceType;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.graphhopper.reader.overture.parser.field.extractors.RoadSurfaceExtractor.extractRoadSurfaces;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RoadSurfaceExtractorTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    @DisplayName("Return empty list if 'properties' list is missed")
    public void testExtractRoadSurfaceMissingPropertiesList() throws JsonProcessingException {
        JsonNode jsonMissingPropertiesList = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195"
                        }
                """);

        assertNotNull(extractRoadSurfaces(jsonMissingPropertiesList, "a737f685-b66f-41f1-9ef7-1909ecd15195"));
    }

    @Test
    @DisplayName("Return empty list if 'properties' list is null")
    public void testExtractRoadSurfaceNullPropertiesList() throws JsonProcessingException {
        JsonNode jsonNullPropertiesList = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": null
                        }
                """);

        assertNotNull(extractRoadSurfaces(jsonNullPropertiesList, "a737f685-b66f-41f1-9ef7-1909ecd15195"));
    }

    @Test
    @DisplayName("Return empty list if 'properties' list is empty")
    public void testExtractRoadSurfaceEmptyPropertiesList() throws JsonProcessingException {
        JsonNode jsonEmptyPropertiesList = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {}
                        }
                """);

        assertNotNull(extractRoadSurfaces(jsonEmptyPropertiesList, "a737f685-b66f-41f1-9ef7-1909ecd15195"));
    }

    @Test
    @DisplayName("Return empty list if 'road_surface list is empty")
    public void testExtractRoadSurfaceEmptyItemsList() throws JsonProcessingException {
        JsonNode jsonEmptyItemsList = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {
                                "road_surface" : []
                            }
                        }
                """);

        assertNotNull(extractRoadSurfaces(jsonEmptyItemsList, "a737f685-b66f-41f1-9ef7-1909ecd15195"));
    }

    @Test
    @DisplayName("Return null for 'between' if 'between' is null type")
    public void testExtractRoadSurfaceItemNullBetweenList() throws JsonProcessingException {
        OvertureRoadSurface nullBetweenListRoadSurface = new OvertureRoadSurface(RoadSurfaceType.PAVED, null);

        JsonNode jsonMissedBetweenList = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {
                                "road_surface" : [
                                    {
                                        "value": "paved",
                                        "between": null
                                    }
                                ]
                            }
                        }
                """);

        assertEquals(nullBetweenListRoadSurface, extractRoadSurfaces(jsonMissedBetweenList, "a737f685-b66f-41f1-9ef7-1909ecd15195").get(0));
    }

    @Test
    @DisplayName("Return expected values for 'between' with null 'value'")
    public void testExtractRoadSurfaceItemValidBetweenListNullValue() throws JsonProcessingException {
        OvertureRoadSurface expectedValidOvertureRoadSurface = new OvertureRoadSurface(null, new LinearlyReferencedRange(0.0, 0.317118591));
        JsonNode jsonMissedBetweenList = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {
                                "road_surface" : [
                                    {
                                        "value": null,
                                        "between": [
                                            0.0,
                                            0.317118591
                                        ]
                                    }               
                                ]
                            }
                        }
                """);

        assertEquals(expectedValidOvertureRoadSurface, extractRoadSurfaces(jsonMissedBetweenList, "a737f685-b66f-41f1-9ef7-1909ecd15195").get(0));
    }

    @Test
    @DisplayName("Return expected values for 'between' with empty 'value'")
    public void testExtractRoadSurfaceItemValidBetweenListEmptyValue() throws JsonProcessingException {
        OvertureRoadSurface expectedValidOvertureRoadSurface = new OvertureRoadSurface(null, new LinearlyReferencedRange(0.0, 0.317118591));
        JsonNode jsonMissedBetweenList = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {
                                "road_surface" : [
                                    {
                                        "value": "",
                                        "between": [
                                            0.0,
                                            0.317118591
                                        ]
                                    }               
                                ]
                            }
                        }
                """);

        assertEquals(expectedValidOvertureRoadSurface, extractRoadSurfaces(jsonMissedBetweenList, "a737f685-b66f-41f1-9ef7-1909ecd15195").get(0));
    }

    @Test
    @DisplayName("Return expected values for 'between' with invalid 'value', for value return null")
    public void testExtractRoadSurfaceItemValidBetweenListInvalidValue() throws JsonProcessingException {
        OvertureRoadSurface expectedValidOvertureRoadSurface = new OvertureRoadSurface(null, new LinearlyReferencedRange(0.0, 0.317118591));
        JsonNode jsonMissedBetweenList = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {
                                "road_surface" : [
                                    {
                                        "value": "regolith",
                                        "between": [
                                            0.0,
                                            0.317118591
                                        ]
                                    }               
                                ]
                            }
                        }
                """);

        assertEquals(expectedValidOvertureRoadSurface, extractRoadSurfaces(jsonMissedBetweenList, "a737f685-b66f-41f1-9ef7-1909ecd15195").get(0));
    }

    @Test
    @DisplayName("Return expected values for 'between' without 'value'")
    public void testExtractRoadSurfaceItemValidBetweenListMissedValue() throws JsonProcessingException {
        OvertureRoadSurface expectedValidOvertureRoadSurface = new OvertureRoadSurface(null, new LinearlyReferencedRange(0.0, 0.317118591));
        JsonNode jsonMissedBetweenList = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {
                                "road_surface" : [
                                    {
                                        "between": [
                                            0.0,
                                            0.317118591
                                        ]
                                    }               
                                ]
                            }
                        }
                """);

        assertEquals(expectedValidOvertureRoadSurface, extractRoadSurfaces(jsonMissedBetweenList, "a737f685-b66f-41f1-9ef7-1909ecd15195").get(0));
    }

    @Test
    @DisplayName("Return null for 'value' if 'value' is empty and logged msg about 'between' is missed value")
    public void testExtractRoadSurfaceItemEmptyValue() throws JsonProcessingException {
        OvertureRoadSurface expectedRoadSurface = new OvertureRoadSurface(null, null);

        JsonNode jsonInvalidValue = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {
                                "road_surface" : [
                                    {
                                        "value": ""
                                    }
                                ]
                            }
                        }
                """);

        assertEquals(expectedRoadSurface, extractRoadSurfaces(jsonInvalidValue, "a737f685-b66f-41f1-9ef7-1909ecd15195").getFirst());

    }

    @Test
    @DisplayName("Checking is correct parsing 'value' for 'road_surface' list item and logged msg about 'between' is missed value")
    public void testExtractRoadSurfaceItemCorrectValue() throws JsonProcessingException {
        List<OvertureRoadSurface> expectedOvertureRoadSurfaces_1 =
                List.of(
                        new OvertureRoadSurface(RoadSurfaceType.UNPAVED, null)
                );
        List<OvertureRoadSurface> expectedOvertureRoadSurfaces_2 =
                List.of(
                        new OvertureRoadSurface(RoadSurfaceType.UNPAVED, new LinearlyReferencedRange(0.0, 0.317118591)),
                        new OvertureRoadSurface(RoadSurfaceType.PAVED, new LinearlyReferencedRange(0.317118591, 1.0))
                );

        JsonNode validValueInvalidBetweenJson = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {
                                "road_surface" : [
                                    {
                                        "value": "unpaved"
                                    }
                                ]
                            }
                        }
                """);

        JsonNode correctRoadSurfaceJson = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "4d488b39-b3d2-493a-ab1a-5fa301e678ab",
                            "properties": {
                                "road_surface" : [
                                    {
                                        "value": "unpaved",
                                        "between": [
                                            0.0,
                                            0.317118591
                                        ]
                                    },
                                    {
                                        "value": "paved",
                                        "between": [
                                            0.317118591,
                                            1.0
                                        ]
                                    }                 
                                ]
                            }
                        }
                """);

        assertEquals(expectedOvertureRoadSurfaces_1, extractRoadSurfaces(validValueInvalidBetweenJson, "a737f685-b66f-41f1-9ef7-1909ecd15195"));
        assertEquals(expectedOvertureRoadSurfaces_2, extractRoadSurfaces(correctRoadSurfaceJson, "4d488b39-b3d2-493a-ab1a-5fa301e678ab"));
    }

    @Test
    @DisplayName("Return null for 'value' if 'value' is null and logged msg about 'between' is missed value")
    public void testExtractRoadSurfaceItemNullValue() throws JsonProcessingException {
        OvertureRoadSurface expectedRoadSurface = new OvertureRoadSurface(null, null);

        JsonNode jsonInvalidValue = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {
                                "road_surface": [
                                    {
                                        "value": null
                                    }
                                ]
                            }
                        }
                """);

        assertEquals(expectedRoadSurface, extractRoadSurfaces(jsonInvalidValue, "a737f685-b66f-41f1-9ef7-1909ecd15195").getFirst());

    }

    @Test
    @DisplayName("Return null for 'value' if 'value' is invalid type and logged msg about 'between' is missed value")
    public void testExtractRoadSurfaceItemInvalidValue() throws JsonProcessingException {
        OvertureRoadSurface expectedRoadSurface = new OvertureRoadSurface(null, null);

        JsonNode jsonInvalidValue = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {
                                "road_surface" : [
                                    {
                                        "value": "regolith"
                                    }
                                ]
                            }
                        }
                """);

        assertEquals(expectedRoadSurface, extractRoadSurfaces(jsonInvalidValue, "a737f685-b66f-41f1-9ef7-1909ecd15195").getFirst());
    }

    @Nested
    public class TestingUsingLogger {
        Logger logger = (Logger) LoggerFactory.getLogger(OvertureParser.class);
        ListAppender<ILoggingEvent> logList;

        @BeforeEach
        public void cleanList() {
            logList = new ListAppender<>();
            logList.start();
            logger.addAppender(logList);
        }

        @Test
        @DisplayName("Return null for 'between' if 'between' isn't of array type and logged msg about 'between' isn't of array type")
        public void testExtractRoadSurfaceItemNotArrayTypeBetweenList() throws JsonProcessingException {
            OvertureRoadSurface nullBetweenListRoadSurface = new OvertureRoadSurface(RoadSurfaceType.PAVED, null);

            JsonNode jsonMissedBetweenList = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {
                                "road_surface" : [
                                    {
                                        "value": "paved",
                                        "between": 0.3
                                    }
                                ]
                            }
                        }
                """);

            assertEquals(nullBetweenListRoadSurface, extractRoadSurfaces(jsonMissedBetweenList, "a737f685-b66f-41f1-9ef7-1909ecd15195").get(0));
            assertTrue(logList.list
                    .stream().anyMatch(e ->
                            e.getLevel().toString().equals("WARN") &&
                                    e.getFormattedMessage().equals("'FEATURE' 'between' field isn't of array type in feature id: 'a737f685-b66f-41f1-9ef7-1909ecd15195' .")));
        }

        @Test
        @DisplayName("Return null for 'between' if 'between' is empty and logged msg about 'between' is empty")
        public void testExtractRoadSurfaceItemEmptyBetweenList() throws JsonProcessingException {
            OvertureRoadSurface emptyBetweenListRoadSurface = new OvertureRoadSurface(RoadSurfaceType.PAVED, null);

            JsonNode jsonMissedBetweenList = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {
                                "road_surface" : [
                                    {
                                        "value": "paved",
                                        "between": []
                                    }
                                ]
                            }
                        }
                """);

            assertEquals(emptyBetweenListRoadSurface, extractRoadSurfaces(jsonMissedBetweenList, "a737f685-b66f-41f1-9ef7-1909ecd15195").get(0));

            assertTrue(logList.list
                    .stream().anyMatch(e ->
                            e.getLevel().toString().equals("WARN") &&
                                    e.getFormattedMessage().equals("'FEATURE' 'between' list is empty in feature id: 'a737f685-b66f-41f1-9ef7-1909ecd15195' .")));
        }

        @Test
        @DisplayName("Return null for 'between' if 'between' elements isn't correct numeric type and logged msg about 'between' list isn't of correct array numeric type")
        public void testExtractRoadSurfaceItemInvalidBetweenListType() throws JsonProcessingException {
            OvertureRoadSurface expectedUnvalidOvertureRoadSurfaceBetweenValuesStringType = new OvertureRoadSurface(RoadSurfaceType.UNPAVED, null);
            JsonNode jsonUnvalidOvertureRoadSurfaceBetweenValuesStringType = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {
                                "road_surface" : [
                                    {
                                        "value": "unpaved",
                                        "between": [
                                            "0.0",
                                            "0.317118591"
                                        ]
                                    }               
                                ]
                            }
                        }
                """);

            assertEquals(expectedUnvalidOvertureRoadSurfaceBetweenValuesStringType, extractRoadSurfaces(jsonUnvalidOvertureRoadSurfaceBetweenValuesStringType, "a737f685-b66f-41f1-9ef7-1909ecd15195").get(0));

            assertTrue(logList.list
                    .stream().anyMatch(e ->
                            e.getLevel().toString().equals("WARN") &&
                                    e.getFormattedMessage().equals("'" + SegmentFeature.ROAD_SURFACE.name() + "' 'between' list isn't of number type for feature id: 'a737f685-b66f-41f1-9ef7-1909ecd15195' .")));
        }

        @Test
        @DisplayName("Return null for 'between' if 'between' contains non-unique values and logged msg about 'between' list elements contains non-unique values")
        public void testExtractRoadSurfaceItemInvalidBetweenListNonUniqueElements() throws JsonProcessingException {
            OvertureRoadSurface expectedUnvalidBetweenListNonUniqueElements = new OvertureRoadSurface(RoadSurfaceType.UNPAVED, null);
            JsonNode jsonUnvalidOvertureRoadSurfaceBetweenValuesStringType = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {
                                "road_surface" : [
                                    {
                                        "value": "unpaved",
                                        "between": [
                                            0.317118591,
                                            0.317118591
                                        ]
                                    }               
                                ]
                            }
                        }
                """);

            assertEquals(expectedUnvalidBetweenListNonUniqueElements, extractRoadSurfaces(jsonUnvalidOvertureRoadSurfaceBetweenValuesStringType, "a737f685-b66f-41f1-9ef7-1909ecd15195").get(0));

            assertTrue(logList.list
                    .stream().anyMatch(e ->
                            e.getLevel().toString().equals("WARN") &&
                                    e.getFormattedMessage().equals("'" + SegmentFeature.ROAD_SURFACE.name() + "' 'between' list contains non-unique values for feature id: 'a737f685-b66f-41f1-9ef7-1909ecd15195' .")));
        }

        @Test
        @DisplayName("Return null for 'between' if 'between' contains invalid values and logged msg about 'between' list elements contains invalid values")
        public void testExtractRoadSurfaceItemInvalidBetweenList() throws JsonProcessingException {
            OvertureRoadSurface expectedInvalidBetweenListElements = new OvertureRoadSurface(RoadSurfaceType.UNPAVED, null);
            String templateJson = """
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {
                                "road_surface" : [
                                    {
                                        "value": "unpaved",
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

            assertEquals(expectedInvalidBetweenListElements, extractRoadSurfaces(jsonInvalidBetweenListElementsStartMoreThanEnd, "a737f685-b66f-41f1-9ef7-1909ecd15195").get(0));
            assertEquals(expectedInvalidBetweenListElements, extractRoadSurfaces(jsonInvalidBetweenListElementsStartLessThanZero, "a737f685-b66f-41f1-9ef7-1909ecd15195").get(0));
            assertEquals(expectedInvalidBetweenListElements, extractRoadSurfaces(jsonInvalidBetweenListElementsStartMoreThanOne, "a737f685-b66f-41f1-9ef7-1909ecd15195").get(0));
            assertEquals(expectedInvalidBetweenListElements, extractRoadSurfaces(jsonInvalidBetweenListElementsEndLessThanZero, "a737f685-b66f-41f1-9ef7-1909ecd15195").get(0));
            assertEquals(expectedInvalidBetweenListElements, extractRoadSurfaces(jsonInvalidBetweenListElementsEndMoreThanOne, "a737f685-b66f-41f1-9ef7-1909ecd15195").get(0));

            assertTrue(logList.list
                    .stream().allMatch(e ->
                            e.getLevel().toString().equals("WARN") &&
                                    e.getFormattedMessage().equals("'" + SegmentFeature.ROAD_SURFACE.name() + "' 'between' list isn't has valid values for feature id: 'a737f685-b66f-41f1-9ef7-1909ecd15195' .")));
        }

    }

    @Test
    @DisplayName("Return expected values for 'road_surface' list")
    public void testExtractRoadSurfaceItemValidBetweenList() throws JsonProcessingException {
        List<OvertureRoadSurface> expectedValidOvertureRoadSurfaces =
                List.of(
                        new OvertureRoadSurface(RoadSurfaceType.UNPAVED, new LinearlyReferencedRange(0.0, 0.317118591)),
                        new OvertureRoadSurface(null, new LinearlyReferencedRange(0.317118591, 1.0))
                );
        JsonNode jsonMissedBetweenList = MAPPER.readTree("""
                        {
                            "type": "Feature",
                            "id": "a737f685-b66f-41f1-9ef7-1909ecd15195",
                            "properties": {
                                "road_surface" : [
                                    {
                                        "value": "unpaved",
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

        assertEquals(expectedValidOvertureRoadSurfaces, extractRoadSurfaces(jsonMissedBetweenList, "a737f685-b66f-41f1-9ef7-1909ecd15195"));
    }

}