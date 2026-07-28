package com.graphhopper.reader.overture.parser;

import static com.graphhopper.reader.overture.parser.OvertureParser.parse;
import static org.junit.jupiter.api.Assertions.*;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.JsonParseException;
import java.io.File;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

public class OvertureParserTest {

    @Test
    public void testPassInvalidGeoJsonFile() {
        Exception jsonParseException = assertThrows(IllegalArgumentException.class, () -> parse(null));
        assertEquals("File must be not null or exist.", jsonParseException.getMessage());

        jsonParseException =
                assertThrows(IllegalArgumentException.class, () -> parse(new File("notExistedJson.json")));
        assertEquals("File must be not null or exist.", jsonParseException.getMessage());
    }

    @Test
    public void testOtherFileFormatThanGeoJson() {
        File jsonShortFile = Paths.get(
                        "src/test/resources/", "com/graphhopper/reader/overture/parser/otherFileFormat.txt")
                .toFile();
        assertDoesNotThrow(() -> parse(jsonShortFile));
    }

    @Test
    public void testMissedTypeGeoJsonFile() {
        File missedTypeFile = Paths.get(
                        "src/test/resources/",
                        "com/graphhopper/reader/overture/parser/missedTypeGeoJson.geojson")
                .toFile();
        Exception jsonParseException =
                assertThrows(IllegalArgumentException.class, () -> parse(missedTypeFile));
        assertEquals(
                "Field 'type' with value 'FeatureCollection' isn't presented in: '"
                        + missedTypeFile.getName() + "' file.",
                jsonParseException.getMessage());
    }

    @Test
    public void testNullTypeValueGeoJsonFile() {
        File nullTypeValueFile = Paths.get(
                        "src/test/resources/",
                        "com/graphhopper/reader/overture/parser/nullTypeValueGeoJson.geojson")
                .toFile();
        Exception jsonParseException =
                assertThrows(IllegalArgumentException.class, () -> parse(nullTypeValueFile));
        assertEquals(
                "Field 'type' with null value is presented in: '" + nullTypeValueFile.getName() + "' file.",
                jsonParseException.getMessage());
    }

    @Test
    public void testOtherTypeGeoJsonFile() {
        File otherTypeValueFile = Paths.get(
                        "src/test/resources/",
                        "com/graphhopper/reader/overture/parser/otherTypeValueGeoJson.geojson")
                .toFile();
        Exception jsonParseException =
                assertThrows(IllegalArgumentException.class, () -> parse(otherTypeValueFile));
        assertEquals(
                "Field 'type' with value different from 'FeatureCollection' is presented in: '"
                        + otherTypeValueFile.getName() + "' file.",
                jsonParseException.getMessage());
    }

    @Test
    public void testPassMissingFeaturesGeoJsonFile() {
        File missingFeaturesFile = Paths.get(
                        "src/test/resources/",
                        "com/graphhopper/reader/overture/parser/missedFeaturesGeoJson.geojson")
                .toFile();
        Exception jsonParseException =
                assertThrows(IllegalArgumentException.class, () -> parse(missingFeaturesFile));
        assertEquals(
                "Field 'features' isn't presented in: '" + missingFeaturesFile.getPath() + "' file.",
                jsonParseException.getMessage());
    }

    @Test
    public void testPassNullFeaturesGeoJsonFile() {
        File nullFeaturesFile = Paths.get(
                        "src/test/resources/",
                        "com/graphhopper/reader/overture/parser/nullFeaturesGeoJson.geojson")
                .toFile();
        Exception jsonParseException =
                assertThrows(IllegalArgumentException.class, () -> parse(nullFeaturesFile));
        assertEquals(
                "Field 'features' is null in: '" + nullFeaturesFile.getPath() + "' file.",
                jsonParseException.getMessage());
    }

    @Test
    public void testPassNotArrayTypeFeaturesGeoJsonFile() {
        File notArrayTypeFeaturesFile = Paths.get(
                        "src/test/resources/",
                        "com/graphhopper/reader/overture/parser/notArrayTypeFeaturesGeoJson.geojson")
                .toFile();
        Exception jsonParseException =
                assertThrows(IllegalArgumentException.class, () -> parse(notArrayTypeFeaturesFile));
        assertEquals(
                "Field 'features' isn't of array type in: '" + notArrayTypeFeaturesFile + "' file.",
                jsonParseException.getMessage());
    }

    @Test
    public void testPassEmptyFeaturesGeoJsonFile() {
        Logger logger = (Logger) LoggerFactory.getLogger(OvertureParser.class);
        ListAppender<ILoggingEvent> logList = new ListAppender<>();
        logList.start();
        logger.addAppender(logList);

        File emptyFeaturesFile = Paths.get(
                        "src/test/resources/",
                        "com/graphhopper/reader/overture/parser/emptyFeaturesGeoJson.geojson")
                .toFile();
        assertDoesNotThrow(() -> parse(emptyFeaturesFile));

        assertTrue(logList.list.stream()
                .anyMatch(e -> e.getLevel().toString().equals("WARN")
                        && e.getFormattedMessage()
                                .equals("Field 'features' in '" + emptyFeaturesFile.getName() + "' is empty.")));
    }

    @Test
    public void testHandleMalformedJson() {
        String exceptionMsgPattern = "Malformed geoJson file: '%s' detected.";
        File malformedJsonFile1 = Paths.get(
                        "src/test/resources/",
                        "com/graphhopper/reader/overture/parser/malformedGeoJsonTestCase1.geojson")
                .toFile();
        File malformedJsonFile2 = Paths.get(
                        "src/test/resources/",
                        "com/graphhopper/reader/overture/parser/malformedGeoJsonTestCase2.geojson")
                .toFile();
        File malformedJsonFile3 = Paths.get(
                        "src/test/resources/",
                        "com/graphhopper/reader/overture/parser/malformedGeoJsonTestCase3.geojson")
                .toFile();

        Exception jsonParseException =
                assertThrows(JsonParseException.class, () -> parse(malformedJsonFile1));
        assertEquals(
                String.format(exceptionMsgPattern, malformedJsonFile1.getName()),
                jsonParseException.getMessage());

        jsonParseException = assertThrows(
                JsonParseException.class, () -> parse(malformedJsonFile2), "Feature list can't be parsed.");
        assertEquals(
                String.format(exceptionMsgPattern, malformedJsonFile2.getName()),
                jsonParseException.getMessage());

        jsonParseException = assertThrows(JsonParseException.class, () -> parse(malformedJsonFile3));
        assertEquals(
                String.format(exceptionMsgPattern, malformedJsonFile3.getName()),
                jsonParseException.getMessage());
    }

    @Test
    public void testPassValidGeoJsonFiles() {
        File correctCenterOfLvivFile = Paths.get(
                        "src/test/resources/",
                        "com/graphhopper/reader/overture/parser/correctGeoJson_CenterOfLviv.geojson")
                .toFile();
        File correctCenterOfKyivFile = Paths.get(
                        "src/test/resources/",
                        "com/graphhopper/reader/overture/parser/correctGeoJson_CenterOfKyiv.geojson")
                .toFile();

        assertDoesNotThrow(() -> parse(correctCenterOfLvivFile));
        assertDoesNotThrow(() -> parse(correctCenterOfKyivFile));
    }
}
