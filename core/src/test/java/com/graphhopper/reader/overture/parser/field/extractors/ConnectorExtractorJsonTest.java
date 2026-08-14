package com.graphhopper.reader.overture.parser.field.extractors;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.reader.overture.road.segment.OvertureConnector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ConnectorExtractorJsonTest {

        private final ObjectMapper mapper = new ObjectMapper();

        @Test
        @DisplayName("Should parse multiple connectors")
        void extractConnectors_Multiple() throws Exception {
                JsonNode node = mapper.readTree("""
                        {
                            "properties": {
                                "connectors": [
                                    { "connector_id": "c1", "at": 0.0 },
                                    { "connector_id": "c2", "at": 0.5 },
                                    { "connector_id": "c3", "at": 1.0 }
                                ]
                            }
                        }
                        """);

                List<OvertureConnector> connectors = ConnectorExtractor.extractConnectors(node);
                assertNotNull(connectors);
                assertEquals(3, connectors.size());
                assertEquals("c1", connectors.get(0).getConnectorId());
                assertEquals(0.0, connectors.get(0).getAt(), 0.00001);
        }

        @Test
        @DisplayName("Should return null for empty connectors array")
        void extractConnectors_EmptyArray() throws Exception {
                JsonNode node = mapper.readTree("""
                        { "properties": { "connectors": [] } }
                        """);

                assertEquals(emptyList(), ConnectorExtractor.extractConnectors(node));
        }

        @Test
        @DisplayName("Should return null when connectors missing")
        void extractConnectors_Missing() throws Exception {
                JsonNode node = mapper.readTree("""
                        { "properties": {} }
                        """);

                assertEquals(emptyList(), ConnectorExtractor.extractConnectors(node));
        }

        @Test
        @DisplayName("Should ignore entries with invalid 'at' and keep valid ones")
        void extractConnectors_IgnoreInvalidAt() throws Exception {
                JsonNode node = mapper.readTree("""
                        {
                            "properties": {
                                "connectors": [
                                    { "connector_id": "ok", "at": 0.2 },
                                    { "connector_id": "bad", "at": "NaN" },
                                    { "connector_id": "also_ok", "at": 0.8 }
                                ]
                            }
                        }
                        """);

                List<OvertureConnector> connectors = ConnectorExtractor.extractConnectors(node);
                assertNotNull(connectors);
                assertEquals(2, connectors.size());
                assertEquals("ok", connectors.get(0).getConnectorId());
                assertEquals(0.2, connectors.get(0).getAt(), 0.00001);
                assertEquals("also_ok", connectors.get(1).getConnectorId());
        }

        @Test
        @DisplayName("Shouldn`t include duplicate connector")
        void extractConnectors_Duplicates() throws Exception {
                JsonNode node = mapper.readTree("""
                        {
                            "properties": {
                                "connectors": [
                                    { "connector_id": "c", "at": 0.1 },
                                    { "connector_id": "c", "at": 0.1 },
                                    { "connector_id": "c", "at": 0.1 }
                                ]
                            }
                        }
                        """);

                List<OvertureConnector> connectors = ConnectorExtractor.extractConnectors(node);
                assertNotNull(connectors);
                // duplicates should be removed, only one unique connector remains
                assertEquals(1, connectors.size());
        }
        
        @Test
        @DisplayName("Shouldn`t include duplicate at value in connector")
        void extractConnectors_DuplicatesAtValue() throws Exception {
                JsonNode node = mapper.readTree("""
                        {
                            "properties": {
                                "connectors": [
                                    { "connector_id": "c1", "at": 0.1 },
                                    { "connector_id": "c2", "at": 0.1 },
                                    { "connector_id": "c3", "at": 0.1 }
                                ]
                            }
                        }
                        """);

                List<OvertureConnector> connectors = ConnectorExtractor.extractConnectors(node);
                assertNotNull(connectors);
                assertEquals(1, connectors.size());
        }
        
        @Test
        @DisplayName("Should include duplicate Id value in connector as At values differ")
        void extractConnectors_DuplicatesIdValue() throws Exception {
                JsonNode node = mapper.readTree("""
                        {
                            "properties": {
                                "connectors": [
                                    { "connector_id": "c", "at": 0.1 },
                                    { "connector_id": "c", "at": 0.2 },
                                    { "connector_id": "c", "at": 0.3 }
                                ]
                            }
                        }
                        """);

                List<OvertureConnector> connectors = ConnectorExtractor.extractConnectors(node);
                assertNotNull(connectors);
                assertEquals(3, connectors.size());
        }
}
