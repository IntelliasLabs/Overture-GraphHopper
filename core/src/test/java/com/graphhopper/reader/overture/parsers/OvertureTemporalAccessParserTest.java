package com.graphhopper.reader.overture.parsers;

import static org.junit.jupiter.api.Assertions.*;

import com.graphhopper.reader.overture.access.restriction.OvertureAccessRestriction;
import com.graphhopper.reader.overture.access.restriction.PropertyScopeContainer;
import com.graphhopper.reader.overture.road.segment.OvertureRoadProperties;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import java.util.List;
import org.junit.jupiter.api.Test;

class OvertureTemporalAccessParserTest {

    private OvertureRoadSegment createSegmentWithRestrictions(
            List<OvertureAccessRestriction> restrictions) {
        OvertureRoadProperties props = new OvertureRoadProperties(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                restrictions,
                0,
                null,
                null,
                null,
                0,
                null,
                null);
        return new OvertureRoadSegment("id", null, props);
    }

    @Test
    void hasTemporalRestriction_present() {
        PropertyScopeContainer scope = PropertyScopeContainer.ofDuring("Mo-Fr 08:00-18:00");
        OvertureAccessRestriction restriction = OvertureAccessRestriction.ofWhen(scope);

        OvertureRoadSegment segment = createSegmentWithRestrictions(List.of(restriction));

        assertTrue(OvertureTemporalAccessParser.hasTemporalRestriction(segment));
    }

    @Test
    void hasTemporalRestriction_absent_nullList() {
        OvertureRoadSegment segment = createSegmentWithRestrictions(null);

        assertFalse(OvertureTemporalAccessParser.hasTemporalRestriction(segment));
    }

    @Test
    void hasTemporalRestriction_absent_emptyList() {
        OvertureRoadSegment segment = createSegmentWithRestrictions(List.of());

        assertFalse(OvertureTemporalAccessParser.hasTemporalRestriction(segment));
    }

    @Test
    void hasTemporalRestriction_absent_noDuring() {
        PropertyScopeContainer scope = PropertyScopeContainer.ofHeading(null);
        OvertureAccessRestriction restriction = OvertureAccessRestriction.ofWhen(scope);

        OvertureRoadSegment segment = createSegmentWithRestrictions(List.of(restriction));

        assertFalse(OvertureTemporalAccessParser.hasTemporalRestriction(segment));
    }

    @Test
    void hasTemporalRestriction_absent_blankDuring() {
        PropertyScopeContainer scope = PropertyScopeContainer.ofDuring("   ");
        OvertureAccessRestriction restriction = OvertureAccessRestriction.ofWhen(scope);

        OvertureRoadSegment segment = createSegmentWithRestrictions(List.of(restriction));

        assertFalse(OvertureTemporalAccessParser.hasTemporalRestriction(segment));
    }

    @Test
    void hasTemporalRestriction_multipleRestrictions_oneTemporal() {
        OvertureAccessRestriction r1 =
                OvertureAccessRestriction.ofWhen(PropertyScopeContainer.ofHeading(null));
        OvertureAccessRestriction r2 =
                OvertureAccessRestriction.ofWhen(PropertyScopeContainer.ofDuring("Sat 10:00-12:00"));

        OvertureRoadSegment segment = createSegmentWithRestrictions(List.of(r1, r2));

        assertTrue(OvertureTemporalAccessParser.hasTemporalRestriction(segment));
    }
}
