package com.graphhopper.reader.overture.parser.field.extractors;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelHeading;
import com.graphhopper.reader.overture.common.speed.OvertureSpeedLimit;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SpeedLimitExtractorTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Should extract valid limits and SKIP invalid ones (Garbage Collection)")
    void parseSpeedLimits_FilterLogic() throws Exception {
        String jsonFeature = "{" + "\"id\": \"seg_1\", "
                + "\"properties\": {"
                + "  \"speed_limits\": ["
                + "    {\"max_speed\": {\"value\": 50, \"unit\": \"km/h\"}},"
                + /// OK
                "    {\"max_speed\": {\"value\": 400, \"unit\": \"km/h\"}},"
                + /// INVALID
                "    {\"min_speed\": {\"value\": 30, \"unit\": \"km/h\"}},"
                + /// OK
                "    {\"max_speed\": null, \"min_speed\": null},"
                + /// INVALID
                "    {\"max_speed\": {\"value\": 60, \"unit\": \"km/h\"}, \"between\": [0.5, 1.0]}"
                + /// OK
                "  ]"
                + "}"
                + "}";

        JsonNode root = mapper.readTree(jsonFeature);

        List<OvertureSpeedLimit> limits = SpeedLimitExtractor.extractSpeedLimits(root, "seg_1");

        assertNotNull(limits);
        assertEquals(3, limits.size(), "only 3 valid obj");

        assertEquals(50.0, limits.get(0).getMaxSpeed().getValue());
        assertNull(limits.get(1).getMaxSpeed());
        assertEquals(30.0, limits.get(1).getMinSpeed().getValue());

        assertEquals(60.0, limits.get(2).getMaxSpeed().getValue());
        assertEquals(0.5, limits.get(2).getBetween().getStart());
    }

    @Test
    @DisplayName("Should extract variable speed limit flag")
    void parseSpeedLimits_VariableFlag() throws Exception {
        String json = "{\"id\": \"seg_1\", \"properties\": {\"speed_limits\": ["
                + "{\"max_speed\": {\"value\": 120, \"unit\": \"km/h\"}, \"is_max_speed_variable\": true}"
                + "]}}";
        List<OvertureSpeedLimit> limits =
                SpeedLimitExtractor.extractSpeedLimits(mapper.readTree(json), "seg_1");
        assertNotNull(limits);
        assertTrue(limits.getFirst().isMaxSpeedVariable());
    }

    @Test
    @DisplayName("Should parse limit with only min_speed present")
    void parseSpeedLimits_OnlyMinSpeed() throws Exception {
        String json = "{\"id\": \"seg_1\", \"properties\": {\"speed_limits\": ["
                + "{\"min_speed\": {\"value\": 40, \"unit\": \"km/h\"}}"
                + "]}}";
        List<OvertureSpeedLimit> limits =
                SpeedLimitExtractor.extractSpeedLimits(mapper.readTree(json), "seg_1");
        assertNotNull(limits);
        assertNull(limits.getFirst().getMaxSpeed());
        assertEquals(40.0, limits.getFirst().getMinSpeed().getValue());
    }

    @Test
    @DisplayName("Should return null if speed_limits array is missing or empty")
    void parseSpeedLimits_Empty() throws Exception {
        assertEquals(
                emptyList(),
                SpeedLimitExtractor.extractSpeedLimits(
                        mapper.readTree("{\"id\": \"seg_1\", \"properties\": {}}"), "seg_1"));
        assertEquals(
                emptyList(),
                SpeedLimitExtractor.extractSpeedLimits(
                        mapper.readTree("{\"id\": \"seg_1\", \"properties\": {\"speed_limits\": []}}"),
                        "seg_1"));
        assertEquals(
                emptyList(),
                SpeedLimitExtractor.extractSpeedLimits(
                        mapper.readTree("{\"id\": \"seg_1\", \"properties\": {\"speed_limits\": null}}"),
                        "seg_1"));
    }

    @Test
    void parseRealSampleOfData_SpeedLimits() throws Exception {
        InputStream is = getClass()
                .getResourceAsStream("/com/graphhopper/reader/overture/parser/speedLimitTestSample.json");
        assertNotNull(is);

        JsonNode rootNode = mapper.readTree(is);

        List<OvertureSpeedLimit> limits =
                SpeedLimitExtractor.extractSpeedLimits(rootNode, "45f1d47c-e53d-4d84-b3ab-be6411240032");

        assertNotNull(limits);
        assertEquals(4, limits.size());

        OvertureSpeedLimit limit1 = limits.getFirst();
        assertEquals(100.0, limit1.getMaxSpeed().getValue());
        assertEquals(0.0, limit1.getBetween().getStart(), 0.0001);
        assertEquals(0.9311, limit1.getBetween().getEnd(), 0.0001);
        assertNull(limit1.getWhen());

        OvertureSpeedLimit limit2 = limits.get(1);
        assertEquals(50.0, limit2.getMaxSpeed().getValue());
        assertEquals(0.9655, limit2.getBetween().getStart(), 0.0001);
        assertEquals(1.0, limit2.getBetween().getEnd(), 0.0001);

        OvertureSpeedLimit limit3 = limits.get(2);
        assertEquals(100.0, limit3.getMaxSpeed().getValue());
        assertNotNull(limit3.getWhen());
        assertEquals(TravelHeading.BACKWARD, limit3.getWhen().getHeading());
        assertEquals(0.9311, limit3.getBetween().getStart(), 0.0001);

        OvertureSpeedLimit limit4 = limits.get(3);
        assertEquals(70.0, limit4.getMaxSpeed().getValue());
        assertNotNull(limit4.getWhen());
        assertEquals(TravelHeading.FORWARD, limit4.getWhen().getHeading());

        for (OvertureSpeedLimit limit : limits) {
            System.out.println(limit);
        }
    }
}
