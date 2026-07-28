package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelHeading;
import com.graphhopper.reader.overture.parser.features.SegmentFeature;
import com.graphhopper.reader.overture.road.segment.destination.OvertureDestination;
import com.graphhopper.reader.overture.road.segment.destination.OvertureDestinationLabel;
import com.graphhopper.reader.overture.road.segment.destination.OvertureDestinationLabelType;
import com.graphhopper.reader.overture.road.segment.destination.OvertureDestinationSymbol;
import java.util.ArrayList;
import java.util.List;

/**
 * Extractor for {@code destinations} data from Overture features.
 * <p>
 * Parses signage information and destination indicators that guide navigation
 * at specific points along a segment.
 */
public class DestinationExtractor {

    private static final String LABELS = "labels";
    private static final String SYMBOLS = "symbols";
    private static final String FROM_CONNECTOR_ID = "from_connector_id";
    private static final String TO_SEGMENT_ID = "to_segment_id";
    private static final String TO_CONNECTOR_ID = "to_connector_id";
    private static final String WHEN = "when";
    private static final String FINAL_HEADING = "final_heading";
    private static final String LABEL_VALUE = "value";
    private static final String LABEL_TYPE = "type";

    /**
     * Extracts a list of destinations from the feature JSON.
     *
     * @param segmentJson raw GeoJSON feature node
     * @return the destinations, empty when the property is absent or holds nothing usable
     */
    public static List<OvertureDestination> extractDestinations(JsonNode segmentJson) {
        return SegmentFeature.DESTINATIONS.parseList(
                segmentJson, DestinationExtractor::parseDestination, null);
    }
    /**
     * Checks for the presence of the {@code destinations} property.
     *
     * @param segmentJson raw GeoJSON feature node
     * @return {@code true} if the property exists and is not null
     */
    public static boolean destinationsExist(JsonNode segmentJson) {
        JsonNode node = SegmentFeature.DESTINATIONS.getFeature(segmentJson, null);
        return node != null && !node.isNull();
    }

    private static OvertureDestination parseDestination(JsonNode item, String featureId) {
        return new OvertureDestination(
                parseLabels(item.get(LABELS)),
                parseSymbols(item.get(SYMBOLS)),
                text(item.get(FROM_CONNECTOR_ID)),
                text(item.get(TO_SEGMENT_ID)),
                text(item.get(TO_CONNECTOR_ID)),
                WhenExtractor.extractWhen(item.get(WHEN), featureId),
                TravelHeading.fromString(text(item.get(FINAL_HEADING))));
    }

    private static List<OvertureDestinationLabel> parseLabels(JsonNode labelsNode) {
        if (labelsNode == null || !labelsNode.isArray()) return List.of();

        List<OvertureDestinationLabel> labels = new ArrayList<>(labelsNode.size());
        for (JsonNode label : labelsNode) {
            if (label == null || label.isNull()) continue;
            String value = text(label.get(LABEL_VALUE));
            // A label with no text signs nothing, so it is dropped rather than stored blank.
            if (value == null || value.isBlank()) continue;
            labels.add(new OvertureDestinationLabel(
                    value, OvertureDestinationLabelType.fromString(text(label.get(LABEL_TYPE)))));
        }
        return labels;
    }

    private static List<OvertureDestinationSymbol> parseSymbols(JsonNode symbolsNode) {
        if (symbolsNode == null || !symbolsNode.isArray()) return List.of();

        List<OvertureDestinationSymbol> symbols = new ArrayList<>(symbolsNode.size());
        for (JsonNode symbol : symbolsNode) {
            OvertureDestinationSymbol parsed = OvertureDestinationSymbol.fromString(text(symbol));
            if (parsed != null) symbols.add(parsed);
        }
        return symbols;
    }

    private static String text(JsonNode node) {
        return node == null || node.isNull() ? null : node.asText();
    }
}
