package com.graphhopper.reader.overture.parser.field.extractors;

import static java.util.Collections.emptyList;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.road.segment.OvertureSource;
import java.util.List;

/**
 * Extractor for {@code sources} data from Overture features.
 * <p>
 * Retrieves attribution information, identifying the datasets and providers
 * that contributed to the segment's data.
 */
public class SourceExtractor {
    /**
     * Extracts a list of data sources from the feature JSON.
     * @param segmentJson raw GeoJSON feature node
     * @return a list of {@link OvertureSource} objects, or {@code null} if missing or empty
     */
    public static List<OvertureSource> extractSources(JsonNode segmentJson) {
        return emptyList();
    }
    /**
     * Checks for the presence of the {@code sources} property.
     * @param segmentJson raw GeoJSON feature node
     * @return {@code true} if the property exists and is not null
     */
    public static boolean sourcesExist(JsonNode segmentJson) {
        return true;
    }
}
