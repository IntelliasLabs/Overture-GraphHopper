package com.graphhopper.reader.overture.parser.field.extractors;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.reader.overture.access.restriction.AccessType;
import com.graphhopper.reader.overture.access.restriction.OvertureAccessRestriction;
import com.graphhopper.reader.overture.access.restriction.PropertyScopeContainer;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelHeading;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelMode;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AccessRestrictionExtractorTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private JsonNode readSamples() throws Exception {
        InputStream in = getClass()
                .getResourceAsStream(
                        "/com/graphhopper/reader/overture/parser/accessRestrictionsTestSamples.json");
        assertNotNull(in, "Test resource not found");
        return mapper.readTree(in);
    }

    @Test
    @DisplayName("Should parse single access restriction with mode")
    void singleRestriction() throws Exception {
        JsonNode samples = readSamples();
        JsonNode sample = samples.get("single");
        List<OvertureAccessRestriction> res =
                AccessRestrictionExtractor.extractAccessRestrictions(sample, "segment-1");
        assertNotNull(res);
        assertEquals(1, res.size());
        OvertureAccessRestriction r = res.get(0);
        assertEquals(AccessType.DENIED, r.getAccessType());
        PropertyScopeContainer when = r.getWhen();
        assertNotNull(when);
        assertTrue(when.hasMode());
        assertTrue(when.getMode().contains(TravelMode.CAR));
    }

    @Test
    @DisplayName("Should parse multiple access restrictions")
    void multipleRestrictions() throws Exception {
        JsonNode samples = readSamples();
        JsonNode sample = samples.get("multiple");
        List<OvertureAccessRestriction> res =
                AccessRestrictionExtractor.extractAccessRestrictions(sample, "segment-1");
        assertNotNull(res);
        assertEquals(2, res.size());
        assertEquals(AccessType.ALLOWED, res.get(0).getAccessType());
        assertEquals(AccessType.DENIED, res.get(1).getAccessType());
    }

    @Test
    @DisplayName("Should parse various travel modes")
    void variousModes() throws Exception {
        JsonNode samples = readSamples();
        JsonNode sample = samples.get("modes");
        List<OvertureAccessRestriction> res =
                AccessRestrictionExtractor.extractAccessRestrictions(sample, "segment-1");
        assertNotNull(res);
        assertEquals(1, res.size());
        PropertyScopeContainer when = res.get(0).getWhen();
        assertNotNull(when);
        assertTrue(when.hasMode());
        assertTrue(when.getMode().contains(TravelMode.CAR));
        assertTrue(when.getMode().contains(TravelMode.FOOT));
        assertTrue(when.getMode().contains(TravelMode.BICYCLE));
    }

    @Test
    @DisplayName("Should parse directional (heading) restriction")
    void directionalRestriction() throws Exception {
        JsonNode samples = readSamples();
        JsonNode sample = samples.get("directional");
        List<OvertureAccessRestriction> res =
                AccessRestrictionExtractor.extractAccessRestrictions(sample, "segment-1");
        assertNotNull(res);
        assertEquals(1, res.size());
        PropertyScopeContainer when = res.get(0).getWhen();
        assertNotNull(when);
        assertTrue(when.hasHeading());
        assertEquals(TravelHeading.FORWARD, when.getHeading());
    }

    @Test
    @DisplayName("Should return null when array is missing")
    void missingArrayReturnsNull() throws Exception {
        JsonNode samples = readSamples();
        JsonNode sample = samples.get("missing");
        List<OvertureAccessRestriction> res =
                AccessRestrictionExtractor.extractAccessRestrictions(sample, null);
        assertEquals(emptyList(), res);
    }

    @Test
    @DisplayName("Should return null for null input")
    void nullInputReturnsNull() throws Exception {
        List<OvertureAccessRestriction> res =
                AccessRestrictionExtractor.extractAccessRestrictions(null, null);
        assertEquals(emptyList(), res);
    }
}
