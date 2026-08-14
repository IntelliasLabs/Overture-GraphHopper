package com.graphhopper.reader.overture.parser.field.extractors;

import static java.util.Collections.emptyList;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.road.segment.OvertureConnector;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Extractor for {@code connectors} data from Overture features.
 * <p>
 * Handles parsing of topological connection points located at specific
 * positions along a segment.
 */
public class ConnectorExtractor {

    /**
     * Extracts connectors from `properties.connectors`.
     * <p>Parses each connector with a numeric `at` into an {@link OvertureConnector}.
     * Returns {@code null} when no connectors are present.</p>
     *
     * @param segmentJson GeoJSON feature node
     * @return list of connectors or {@code null}
     */
    public static List<OvertureConnector> extractConnectors(JsonNode segmentJson) {
        if (segmentJson == null) {
            return emptyList();
        }

        JsonNode properties = segmentJson.get("properties");
        if (properties == null) {
            return emptyList();
        }

        JsonNode connectors = properties.get("connectors");
        if (connectors == null || !connectors.isArray() || connectors.isEmpty()) {
            return emptyList();
        }

        LinkedHashMap<String, OvertureConnector> uniqueByAt = new LinkedHashMap<>();
        for (JsonNode node : connectors) {
            if (node == null || node.isNull()) continue;

            JsonNode atNode = node.get("at");
            if (atNode == null || !atNode.isNumber()) continue;

            JsonNode connIdNode = node.get("connector_id");
            if (connIdNode == null || connIdNode.isNull()) continue;

            String connectorId = connIdNode.asText(null);
            if (connectorId == null) continue;

            String atKey = atNode.asText();
            uniqueByAt.putIfAbsent(atKey, new OvertureConnector(connectorId, atNode.asDouble()));
        }

        return uniqueByAt.isEmpty() ? emptyList() : new ArrayList<>(uniqueByAt.values());
    }
}
