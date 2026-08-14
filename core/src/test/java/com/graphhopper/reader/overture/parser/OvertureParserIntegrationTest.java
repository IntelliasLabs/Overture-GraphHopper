package com.graphhopper.reader.overture.parser;

import static org.junit.jupiter.api.Assertions.*;

import com.graphhopper.reader.overture.common.speed.SpeedUnit;
import com.graphhopper.reader.overture.road.segment.OvertureRoadClass;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.reader.overture.road.surface.RoadSurfaceType;
import java.io.File;
import java.net.URL;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class OvertureParserIntegrationTest {
    @Test
    @DisplayName("Should parse all features from the real world sample file with no exceptions")
    void testTotalFeaturesCount() throws Exception {
        URL resource = getClass()
                .getResource("/com/graphhopper/reader/overture/parser/correctGeoJson_CenterOfLviv.geojson");
        assertNotNull(resource, "File not found in classpath!");

        File file = new File(resource.toURI());

        List<OvertureRoadSegment> segmentsFromRealData = OvertureParser.parse(file);

        assertNotNull(segmentsFromRealData);
        assertFalse(segmentsFromRealData.isEmpty(), "Segments list should not be empty");
        System.out.println("Parsed " + segmentsFromRealData.size() + " segments");
    }

    @Test
    @DisplayName("Final list of corrupted segments should be empty")
    void testFullCorruptedDataParsing() throws Exception {
        URL resource = getClass()
                .getResource(
                        "/com/graphhopper/reader/overture/parser/overture_missing_required_fields.json");
        assertNotNull(resource, "File not found in classpath!");

        File file = new File(resource.toURI());

        List<OvertureRoadSegment> segmentsFromCorruptedData = OvertureParser.parse(file);

        assertTrue(segmentsFromCorruptedData.isEmpty(), "Segments list should be empty");
    }

    @Test
    @DisplayName("Should correctly extract all road properties")
    void testPropertiesExtraction() throws Exception {
        URL resource = getClass()
                .getResource(
                        "/com/graphhopper/reader/overture/parser/overture_single_full_fields_feature.json");
        assertNotNull(resource, "File not found in classpath!");

        File file = new File(resource.toURI());

        List<OvertureRoadSegment> segmentsFromReferenceData = OvertureParser.parse(file);
        OvertureRoadSegment segment = segmentsFromReferenceData.getFirst();

        var props = segment.getProperties();

        assertNotNull(segment.getId(), "Segment ID should be extracted");
        assertEquals("4d4f7594-d82a-40d6-969b-9221c55584ee", segment.getId());

        assertNotNull(segment.getLineString(), "Geometry should be extracted");
        assertNotNull(segment.getClass(), "Class should be extracted");
        assertEquals(OvertureRoadClass.RESIDENTIAL, props.getRoadClass());

        assertNotNull(props.getSpeedLimits(), "Speed limits mapping failed");
        assertEquals(1, props.getSpeedLimits().size());
        var speed = props.getSpeedLimits().getFirst();
        assertEquals(100, speed.getMaxSpeed().getValue());
        assertEquals(SpeedUnit.KM_H, speed.getMaxSpeed().getUnit());

        assertNotNull(props.getSurfaces(), "Surface mapping failed");
        assertEquals(RoadSurfaceType.PAVED, props.getSurfaces().getFirst().getSurfaceType());

        assertNotNull(props.getFlags(), "Road flags mapping failed");
        assertTrue(props.getFlags().getFirst().isBridge());

        assertNotNull(props.getAccessRestrictions(), "Access restrictions mapping failed");
    }
}
