package com.graphhopper.reader.overture.parser;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * High-level parser for Overture Maps GeoJSON files.
 * <p>
 * This class handles the initial file processing, validates the GeoJSON structure
 * (specifically {@code FeatureCollection}), and delegates individual feature parsing
 * to {@link OvertureExtractor}.
 * <p>
 * <b>Usage Example:</b>
 * <pre>{@code
 * File file = new File("overture_data.geojson");
 * List<OvertureRoadSegment> segments = OvertureParser.parse(file);
 * }</pre>
 *
 * @see OvertureExtractor
 */
public class OvertureParser {

    private static final Logger logger = LoggerFactory.getLogger(OvertureParser.class);
    private static final ObjectMapper parser = new ObjectMapper();

    /**
     * Parses an Overture road network from a local GeoJSON file.
     * <p>
     * This method performs a strict validation of the GeoJSON structure, ensuring the input
     * is a valid {@code FeatureCollection}. It handles malformed JSON by rewrapping low-level
     * {@link JsonParseException} into more descriptive errors.
     * </p>
     * @param geoJsonFile the local {@link File} containing Overture road features in GeoJSON format.
     * @return a {@link List} of successfully parsed {@link OvertureRoadSegment} objects.
     * @throws IllegalArgumentException if the file is missing, the JSON structure is invalid,
     * or the root type is not a 'FeatureCollection'.
     * @throws IOException if there is an I/O error or the file contains malformed JSON.
     */
    public static List<OvertureRoadSegment> parse(File geoJsonFile) throws IOException {
        if (geoJsonFile == null || !geoJsonFile.exists())
            throw new IllegalArgumentException("File must be not null or exist.");

        logger.info("Starting parsing geoJson file: '{}' .", geoJsonFile.getName());

        JsonNode jsonFeatureCollection;
        try {
            jsonFeatureCollection = parser.readValue(geoJsonFile, new TypeReference<>() {});
        } catch (JsonParseException e) {
            JsonParseException wrapEx = new JsonParseException(
                    "Malformed geoJson file: '" + geoJsonFile.getName() + "' detected.");
            wrapEx.setStackTrace(e.getStackTrace());
            throw wrapEx;
        }

        if (!jsonFeatureCollection.has("type"))
            throw new IllegalArgumentException(
                    "Field 'type' with value 'FeatureCollection' isn't presented in: '"
                            + geoJsonFile.getName() + "' file.");
        if (jsonFeatureCollection.get("type").isNull())
            throw new IllegalArgumentException(
                    "Field 'type' with null value is presented in: '" + geoJsonFile.getName() + "' file.");
        if (!jsonFeatureCollection.get("type").asText().equals("FeatureCollection"))
            throw new IllegalArgumentException(
                    "Field 'type' with value different from 'FeatureCollection' is presented in: '"
                            + geoJsonFile.getName() + "' file.");

        if (!jsonFeatureCollection.has("features"))
            throw new IllegalArgumentException(
                    "Field 'features' isn't presented in: '" + geoJsonFile + "' file.");

        jsonFeatureCollection = jsonFeatureCollection.get("features");

        if (jsonFeatureCollection.isNull())
            throw new IllegalArgumentException(
                    "Field 'features' is null in: '" + geoJsonFile + "' file.");
        if (jsonFeatureCollection.isEmpty())
            logger.warn("Field 'features' in '{}' is empty.", geoJsonFile.getName());
        if (!jsonFeatureCollection.isArray())
            throw new IllegalArgumentException(
                    "Field 'features' isn't of array type in: '" + geoJsonFile + "' file.");

        Iterator<JsonNode> jsonFeaturesIterator = jsonFeatureCollection.values();

        List<OvertureRoadSegment> overtureRoadSegments = new ArrayList<>();
        while (jsonFeaturesIterator.hasNext()) {
            JsonNode featureJson = jsonFeaturesIterator.next();
            OvertureRoadSegment overtureRoadSegment = OvertureExtractor.extractSegment(featureJson);
            if (overtureRoadSegment != null) overtureRoadSegments.add(overtureRoadSegment);
        }

        logger.info("Successfully parsed values from geoJson file: '{}' .", geoJsonFile.getName());

        return overtureRoadSegments;
    }
}
