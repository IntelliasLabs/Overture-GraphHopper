package com.graphhopper.reader.overture.parser.field.extractors;

import static com.graphhopper.reader.overture.parser.field.extractors.BetweenExtractor.extractBetween;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.parser.features.SegmentFeature;
import com.graphhopper.reader.overture.parser.features.SourceFeature;
import com.graphhopper.reader.overture.road.segment.OvertureSource;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extractor for {@code sources} data from Overture features.
 * <p>
 * Retrieves attribution information, identifying the datasets and providers
 * that contributed to the segment's data, optionally scoped to part of the segment.
 */
public class SourceExtractor {

    private static final Logger logger = LoggerFactory.getLogger(SourceExtractor.class);

    /**
     * Extracts a list of data sources from the feature JSON.
     * @param segmentJson raw GeoJSON feature node
     * @return the sources, empty when the property is absent or holds nothing usable
     */
    public static List<OvertureSource> extractSources(JsonNode segmentJson) {
        return SegmentFeature.SOURCES.parseList(segmentJson, SourceExtractor::parseSource, null);
    }

    /**
     * Checks for the presence of the {@code sources} property.
     * @param segmentJson raw GeoJSON feature node
     * @return {@code true} if the property exists and is not null
     */
    public static boolean sourcesExist(JsonNode segmentJson) {
        JsonNode node = SegmentFeature.SOURCES.getFeature(segmentJson, null);
        return node != null && !node.isNull();
    }

    private static OvertureSource parseSource(JsonNode item, String featureId) {
        Double confidence = SourceFeature.CONFIDENCE.parseDouble(item, featureId);

        return new OvertureSource(
                SourceFeature.PROPERTY.parseString(item, featureId),
                SourceFeature.DATASET.parseString(item, featureId),
                SourceFeature.LICENSE.parseString(item, featureId),
                SourceFeature.RECORD_ID.parseString(item, featureId),
                parseUpdateTime(SourceFeature.UPDATE_TIME.parseString(item, featureId)),
                confidence == null ? 0 : confidence,
                extractBetween(
                        SourceFeature.BETWEEN.getFeature(item, featureId), SegmentFeature.SOURCES, featureId));
    }

    /**
     * @return the parsed timestamp, or {@code null} when absent or unparseable. A bad timestamp must not
     *     lose the rest of the source record, which is why this does not throw.
     */
    private static OffsetDateTime parseUpdateTime(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return OffsetDateTime.parse(raw);
        } catch (DateTimeParseException e) {
            logger.debug("Unparseable source update_time '{}'", raw);
            return null;
        }
    }
}
