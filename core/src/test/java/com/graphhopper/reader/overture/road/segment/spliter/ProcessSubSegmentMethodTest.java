package com.graphhopper.reader.overture.road.segment.spliter;

import static com.graphhopper.reader.overture.road.segment.spliter.SegmentSplitter.processSubSegment;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import com.graphhopper.reader.overture.access.restriction.AccessType;
import com.graphhopper.reader.overture.access.restriction.OvertureAccessRestriction;
import com.graphhopper.reader.overture.access.restriction.PropertyScopeContainer;
import com.graphhopper.reader.overture.access.restriction.scope.containers.*;
import com.graphhopper.reader.overture.common.speed.OvertureSpeed;
import com.graphhopper.reader.overture.common.speed.OvertureSpeedLimit;
import com.graphhopper.reader.overture.common.speed.SpeedUnit;
import com.graphhopper.reader.overture.names.Bcp47LanguageTag;
import com.graphhopper.reader.overture.names.OvertureNameRule;
import com.graphhopper.reader.overture.names.OvertureNames;
import com.graphhopper.reader.overture.names.Variant;
import com.graphhopper.reader.overture.road.flags.OvertureRoadFlags;
import com.graphhopper.reader.overture.road.segment.*;
import com.graphhopper.reader.overture.road.segment.rule.OvertureLevelRule;
import com.graphhopper.reader.overture.road.segment.rule.OvertureProhibitedTransition;
import com.graphhopper.reader.overture.road.segment.rule.OvertureSubclassRule;
import com.graphhopper.reader.overture.road.segment.rule.OvertureWidthRule;
import com.graphhopper.reader.overture.road.surface.OvertureRoadSurface;
import com.graphhopper.reader.overture.road.surface.RoadSurfaceType;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

public class ProcessSubSegmentMethodTest {
    private static final GeometryFactory geometryFactory = new GeometryFactory();

    @Test
    @DisplayName("Processing subsegment for null segment.")
    public void testNullSegment() {
        assertNull(processSubSegment(null, 0.0, 1.0));
    }

    @Test
    @DisplayName("Processing subsegment for null main properties: id, geometry and properties.")
    public void testNullPropertiesSegment() {
        OvertureRoadSegment segment = new OvertureRoadSegment(null, null, null);

        assertEquals(segment, processSubSegment(segment, 0.0, 0.4));
    }

    @Test
    @DisplayName("Processing subsegment for null segment.")
    public void testFullNullSegment() {
        OvertureRoadSegment segment = getFullNullSegment();
        assertEquals(segment, processSubSegment(segment, 0.0, 0.7));
    }

    @Test
    @DisplayName("Processing subsegment for null segment properties.")
    public void testAllNullRangeSubSegment() {
        double startLr = 0.2;
        double endLr = 0.8;
        OvertureRoadSegment segment = getFullNullRangeSegment();
        OvertureRoadSegment subSegment = getSubSegmentFromNullRange(startLr, endLr);

        assertEquals(subSegment, processSubSegment(segment, startLr, endLr));
    }

    @Test
    @DisplayName("Processing subsegment for real segment.")
    public void testRealSegment() {
        double startLr = 0.614667246;
        double endLr = 0.9;
        OvertureRoadSegment segment = getRealSegment();
        OvertureRoadSegment subSegment = getSubSegmentFromReal(startLr, endLr);

        assertEquals(subSegment, processSubSegment(segment, startLr, endLr));
    }

    @Test
    @DisplayName("Processing subsegment for full segment.")
    public void testNormalFullSegment() {
        double startLr = 0.0;
        double endLr = 0.767664764;
        OvertureRoadSegment segment = getFullSegment();
        OvertureRoadSegment subSegment = getSubSegmentFromFull(startLr, endLr);

        assertEquals(subSegment, processSubSegment(segment, startLr, endLr));
    }

    private OvertureRoadSegment getFullNullSegment() {
        return new OvertureRoadSegment(
                "",
                geometryFactory.createLineString((Coordinate[]) null),
                new OvertureRoadProperties(
                        emptyList(),
                        emptyList(),
                        null,
                        emptyList(),
                        emptyList(),
                        emptyList(),
                        emptyList(),
                        emptyList(),
                        emptyList(),
                        null,
                        emptyList(),
                        emptyList(),
                        0,
                        emptyList(),
                        null,
                        null,
                        0,
                        emptyList(),
                        null,
                        null));
    }

    private OvertureRoadSegment getFullNullRangeSegment() {
        return new OvertureRoadSegment(
                "0ae4edf3-6a3e-46a3-9c7c-c6ce5027b819",
                geometryFactory.createLineString(new Coordinate[] {
                    new Coordinate(30.4124142, 50.3698297),
                    new Coordinate(30.4132273, 50.3726218),
                    new Coordinate(30.4133767, 50.3729185),
                    new Coordinate(30.4135983, 50.3728915),
                    new Coordinate(30.413917, 50.3728039),
                    new Coordinate(30.4143589, 50.3726223),
                    new Coordinate(30.414631, 50.3726044),
                    new Coordinate(30.415469, 50.3724647),
                    new Coordinate(30.416090, 50.3723047),
                    new Coordinate(30.416600, 50.3722047),
                    new Coordinate(30.417230, 50.3721047),
                }),
                new OvertureRoadProperties(
                        List.of(
                                new OvertureConnector("020c04a7-536f-44bf-b591-510a390d4918", 0.0),
                                new OvertureConnector("81cf7f6f-a0b4-4f6b-a035-0fc2aa8d7c2f", 0.687772409),
                                new OvertureConnector("0431ceba-653a-4bde-9e54-fae36dccf194", 0.767664764),
                                new OvertureConnector("489523df-fbf7-4a41-acad-62ee3c5bf606", 0.879143175),
                                new OvertureConnector("8fca4cc8-f977-4b72-9d2a-dca5c279bb6f", 1.0)),
                        List.of(
                                new OvertureRoute("route-1", "network-1", "ref-1", "symbol-1", "wikidata", null)),
                        null,
                        emptyList(),
                        List.of(new OvertureProhibitedTransition(null, TravelHeading.FORWARD, null, null)),
                        List.of(new OvertureRoadSurface(RoadSurfaceType.UNPAVED, null)),
                        List.of(
                                new OvertureRoadFlags(false, false, true, false, false, false, null),
                                new OvertureRoadFlags(false, false, false, false, true, false, null)),
                        List.of(new OvertureSpeedLimit(
                                new OvertureSpeed(50.0, SpeedUnit.KM_H), null, null, null, null)),
                        List.of(new OvertureWidthRule(1.5, null)),
                        OvertureRoadSubclass.DRIVEWAY,
                        List.of(new OvertureSubclassRule(OvertureRoadSubclass.DRIVEWAY, null)),
                        List.of(
                                new OvertureAccessRestriction(
                                        AccessType.ALLOWED, new PropertyScopeContainer(null, null, null, null), null),
                                new OvertureAccessRestriction(
                                        AccessType.DENIED, new PropertyScopeContainer(null, null, null, null), null)),
                        0,
                        List.of(new OvertureLevelRule(1, null)),
                        OvertureTheme.TRANSPORTATION,
                        OvertureFeatureType.SEGMENT,
                        1,
                        List.of(
                                new OvertureSource(
                                        "",
                                        "OpenStreetMap",
                                        "ODbL-1.0",
                                        "w1162595551@3",
                                        OffsetDateTime.parse("2025-09-18T18:49:17Z"),
                                        0,
                                        null),
                                new OvertureSource(
                                        "",
                                        "OpenStreetMap",
                                        "ODbL-1.0",
                                        "w1430995640@1",
                                        OffsetDateTime.parse("2025-09-18T18:49:17Z"),
                                        0,
                                        null)),
                        new OvertureNames(
                                "Казкова вулиця",
                                null,
                                List.of(
                                        new OvertureNameRule(Variant.COMMON, null, null, "Казкова вулиця", null, null),
                                        new OvertureNameRule(
                                                Variant.COMMON,
                                                Bcp47LanguageTag.parse("uk"),
                                                null,
                                                "Казкова вулиця",
                                                null,
                                                null))),
                        OvertureSegmentSubtype.ROAD));
    }

    private OvertureRoadSegment getSubSegmentFromNullRange(double startLr, double endLr) {
        double recalMult = calculateMultiplier(startLr, endLr);
        return new OvertureRoadSegment(
                "0ae4edf3-6a3e-46a3-9c7c-c6ce5027b819",
                geometryFactory.createLineString(new Coordinate[] {
                    new Coordinate(30.4127440, 50.3709624),
                    new Coordinate(30.4132273, 50.3726218),
                    new Coordinate(30.4133767, 50.3729185),
                    new Coordinate(30.4135983, 50.3728915),
                    new Coordinate(30.413917, 50.3728039),
                    new Coordinate(30.4143589, 50.3726223),
                    new Coordinate(30.414631, 50.3726044),
                    new Coordinate(30.415469, 50.3724647),
                    new Coordinate(30.4155118, 50.3724537)
                }),
                new OvertureRoadProperties(
                        List.of(
                                new OvertureConnector(
                                        "81cf7f6f-a0b4-4f6b-a035-0fc2aa8d7c2f", (0.687772409 - startLr) * recalMult),
                                new OvertureConnector(
                                        "0431ceba-653a-4bde-9e54-fae36dccf194", (0.767664764 - startLr) * recalMult)),
                        List.of(
                                new OvertureRoute("route-1", "network-1", "ref-1", "symbol-1", "wikidata", null)),
                        null,
                        emptyList(),
                        List.of(new OvertureProhibitedTransition(null, TravelHeading.FORWARD, null, null)),
                        List.of(new OvertureRoadSurface(RoadSurfaceType.UNPAVED, null)),
                        List.of(
                                new OvertureRoadFlags(false, false, true, false, false, false, null),
                                new OvertureRoadFlags(false, false, false, false, true, false, null)),
                        List.of(new OvertureSpeedLimit(
                                new OvertureSpeed(50.0, SpeedUnit.KM_H), null, null, null, null)),
                        List.of(new OvertureWidthRule(1.5, null)),
                        OvertureRoadSubclass.DRIVEWAY,
                        List.of(new OvertureSubclassRule(OvertureRoadSubclass.DRIVEWAY, null)),
                        List.of(
                                new OvertureAccessRestriction(
                                        AccessType.ALLOWED, new PropertyScopeContainer(null, null, null, null), null),
                                new OvertureAccessRestriction(
                                        AccessType.DENIED, new PropertyScopeContainer(null, null, null, null), null)),
                        0,
                        List.of(new OvertureLevelRule(1, null)),
                        OvertureTheme.TRANSPORTATION,
                        OvertureFeatureType.SEGMENT,
                        1,
                        List.of(
                                new OvertureSource(
                                        "",
                                        "OpenStreetMap",
                                        "ODbL-1.0",
                                        "w1162595551@3",
                                        OffsetDateTime.parse("2025-09-18T18:49:17Z"),
                                        0,
                                        null),
                                new OvertureSource(
                                        "",
                                        "OpenStreetMap",
                                        "ODbL-1.0",
                                        "w1430995640@1",
                                        OffsetDateTime.parse("2025-09-18T18:49:17Z"),
                                        0,
                                        null)),
                        new OvertureNames(
                                "Казкова вулиця",
                                null,
                                List.of(
                                        new OvertureNameRule(Variant.COMMON, null, null, "Казкова вулиця", null, null),
                                        new OvertureNameRule(
                                                Variant.COMMON,
                                                Bcp47LanguageTag.parse("uk"),
                                                null,
                                                "Казкова вулиця",
                                                null,
                                                null))),
                        OvertureSegmentSubtype.ROAD));
    }

    private OvertureRoadSegment getFullSegment() {
        return new OvertureRoadSegment(
                "0ae4edf3-6a3e-46a3-9c7c-c6ce5027b819",
                geometryFactory.createLineString(new Coordinate[] {
                    new Coordinate(30.4124142, 50.3698297),
                    new Coordinate(30.4132273, 50.3726218),
                    new Coordinate(30.4133767, 50.3729185),
                    new Coordinate(30.4135983, 50.3728915),
                    new Coordinate(30.413917, 50.3728039),
                    new Coordinate(30.4143589, 50.3726223),
                    new Coordinate(30.414631, 50.3726044),
                    new Coordinate(30.415469, 50.3724647),
                    new Coordinate(30.416090, 50.3723047),
                    new Coordinate(30.416600, 50.3722047),
                    new Coordinate(30.417230, 50.3721047),
                }),
                new OvertureRoadProperties(
                        List.of(
                                new OvertureConnector("020c04a7-536f-44bf-b591-510a390d4918", 0.0),
                                new OvertureConnector("81cf7f6f-a0b4-4f6b-a035-0fc2aa8d7c2f", 0.687772409),
                                new OvertureConnector("0431ceba-653a-4bde-9e54-fae36dccf194", 0.767664764),
                                new OvertureConnector("489523df-fbf7-4a41-acad-62ee3c5bf606", 0.879143175),
                                new OvertureConnector("8fca4cc8-f977-4b72-9d2a-dca5c279bb6f", 1.0)),
                        List.of(
                                new OvertureRoute(
                                        "Київ – Одеса",
                                        "ua:international",
                                        "М-05",
                                        null,
                                        "Q1051588",
                                        new LinearlyReferencedRange(0.3, 0.6)),
                                new OvertureRoute("European route E95", "e-road", "E 95", null, "Q1165682", null)),
                        OvertureRoadClass.TRUNK,
                        emptyList(),
                        List.of(new OvertureProhibitedTransition(
                                null, TravelHeading.FORWARD, null, new LinearlyReferencedRange(0.8, 1.0))),
                        List.of(new OvertureRoadSurface(
                                RoadSurfaceType.UNPAVED, new LinearlyReferencedRange(0.687779337, 1.0))),
                        List.of(
                                new OvertureRoadFlags(
                                        false,
                                        false,
                                        true,
                                        false,
                                        false,
                                        false,
                                        new LinearlyReferencedRange(0.038124959, 0.076249917)),
                                new OvertureRoadFlags(
                                        false,
                                        false,
                                        false,
                                        false,
                                        true,
                                        false,
                                        new LinearlyReferencedRange(0.77, 1.0))),
                        List.of(new OvertureSpeedLimit(
                                new OvertureSpeed(50.0, SpeedUnit.KM_H), null, null, null, null)),
                        List.of(new OvertureWidthRule(1.5, null)),
                        OvertureRoadSubclass.DRIVEWAY,
                        List.of(new OvertureSubclassRule(
                                OvertureRoadSubclass.DRIVEWAY, new LinearlyReferencedRange(0.0, 0.756090663))),
                        List.of(
                                new OvertureAccessRestriction(
                                        AccessType.ALLOWED,
                                        new PropertyScopeContainer(null, null, null, null),
                                        new LinearlyReferencedRange(0.614667246, 1.0)),
                                new OvertureAccessRestriction(
                                        AccessType.DENIED,
                                        new PropertyScopeContainer(null, null, null, null),
                                        new LinearlyReferencedRange(0.614667246, 1.0))),
                        0,
                        List.of(new OvertureLevelRule(1, new LinearlyReferencedRange(0.0, 0.70))),
                        OvertureTheme.TRANSPORTATION,
                        OvertureFeatureType.SEGMENT,
                        1,
                        List.of(
                                new OvertureSource(
                                        "",
                                        "OpenStreetMap",
                                        "ODbL-1.0",
                                        "w1162595551@3",
                                        OffsetDateTime.parse("2025-09-18T18:49:17Z"),
                                        0,
                                        new LinearlyReferencedRange(0.0, 0.687779337)),
                                new OvertureSource(
                                        "",
                                        "OpenStreetMap",
                                        "ODbL-1.0",
                                        "w1430995640@1",
                                        OffsetDateTime.parse("2025-09-18T18:49:17Z"),
                                        0,
                                        null)),
                        new OvertureNames(
                                "Казкова вулиця",
                                null,
                                List.of(
                                        new OvertureNameRule(
                                                Variant.COMMON,
                                                null,
                                                null,
                                                "Казкова вулиця",
                                                new LinearlyReferencedRange(0.0, 0.687779337),
                                                null),
                                        new OvertureNameRule(
                                                Variant.COMMON,
                                                Bcp47LanguageTag.parse("uk"),
                                                null,
                                                "Казкова вулиця",
                                                new LinearlyReferencedRange(0.0, 0.687779337),
                                                null))),
                        OvertureSegmentSubtype.ROAD));
    }

    private OvertureRoadSegment getSubSegmentFromFull(double startLr, double endLr) {
        double recalMult = calculateMultiplier(startLr, endLr);
        return new OvertureRoadSegment(
                "0ae4edf3-6a3e-46a3-9c7c-c6ce5027b819",
                geometryFactory.createLineString(new Coordinate[] {
                    new Coordinate(30.4124142, 50.3698297),
                    new Coordinate(30.4132273, 50.3726218),
                    new Coordinate(30.4133767, 50.3729185),
                    new Coordinate(30.4135983, 50.3728915),
                    new Coordinate(30.413917, 50.3728039),
                    new Coordinate(30.4143589, 50.3726223),
                    new Coordinate(30.414631, 50.3726044),
                    new Coordinate(30.4152311, 50.3725044)
                }),
                new OvertureRoadProperties(
                        List.of(
                                new OvertureConnector(
                                        "020c04a7-536f-44bf-b591-510a390d4918", (0.0 - startLr) * recalMult),
                                new OvertureConnector(
                                        "81cf7f6f-a0b4-4f6b-a035-0fc2aa8d7c2f", (0.687772409 - startLr) * recalMult),
                                new OvertureConnector(
                                        "0431ceba-653a-4bde-9e54-fae36dccf194", (0.767664764 - startLr) * recalMult)),
                        List.of(
                                new OvertureRoute(
                                        "Київ – Одеса",
                                        "ua:international",
                                        "М-05",
                                        null,
                                        "Q1051588",
                                        new LinearlyReferencedRange(
                                                (0.3 - startLr) * recalMult, (0.6 - startLr) * recalMult)),
                                new OvertureRoute("European route E95", "e-road", "E 95", null, "Q1165682", null)),
                        OvertureRoadClass.TRUNK,
                        emptyList(),
                        emptyList(),
                        List.of(new OvertureRoadSurface(
                                RoadSurfaceType.UNPAVED,
                                new LinearlyReferencedRange((0.687779337 - startLr) * recalMult, 1.0))),
                        List.of(new OvertureRoadFlags(
                                false,
                                false,
                                true,
                                false,
                                false,
                                false,
                                new LinearlyReferencedRange(
                                        (0.038124959 - startLr) * recalMult, (0.076249917 - startLr) * recalMult))),
                        List.of(new OvertureSpeedLimit(
                                new OvertureSpeed(50.0, SpeedUnit.KM_H), null, null, null, null)),
                        List.of(new OvertureWidthRule(1.5, null)),
                        OvertureRoadSubclass.DRIVEWAY,
                        List.of(new OvertureSubclassRule(
                                OvertureRoadSubclass.DRIVEWAY,
                                new LinearlyReferencedRange(0.0, (0.756090663 - startLr) * recalMult))),
                        List.of(
                                new OvertureAccessRestriction(
                                        AccessType.ALLOWED,
                                        new PropertyScopeContainer(null, null, null, null),
                                        new LinearlyReferencedRange((0.614667246 - startLr) * recalMult, 1.0)),
                                new OvertureAccessRestriction(
                                        AccessType.DENIED,
                                        new PropertyScopeContainer(null, null, null, null),
                                        new LinearlyReferencedRange((0.614667246 - startLr) * recalMult, 1.0))),
                        0,
                        List.of(new OvertureLevelRule(
                                1, new LinearlyReferencedRange(0.0, (0.70 - startLr) * recalMult))),
                        OvertureTheme.TRANSPORTATION,
                        OvertureFeatureType.SEGMENT,
                        1,
                        List.of(
                                new OvertureSource(
                                        "",
                                        "OpenStreetMap",
                                        "ODbL-1.0",
                                        "w1162595551@3",
                                        OffsetDateTime.parse("2025-09-18T18:49:17Z"),
                                        0,
                                        new LinearlyReferencedRange(0.0, (0.687779337 - startLr) * recalMult)),
                                new OvertureSource(
                                        "",
                                        "OpenStreetMap",
                                        "ODbL-1.0",
                                        "w1430995640@1",
                                        OffsetDateTime.parse("2025-09-18T18:49:17Z"),
                                        0,
                                        null)),
                        new OvertureNames(
                                "Казкова вулиця",
                                null,
                                List.of(
                                        new OvertureNameRule(
                                                Variant.COMMON,
                                                null,
                                                null,
                                                "Казкова вулиця",
                                                new LinearlyReferencedRange(0.0, (0.687779337 - startLr) * recalMult),
                                                null),
                                        new OvertureNameRule(
                                                Variant.COMMON,
                                                Bcp47LanguageTag.parse("uk"),
                                                null,
                                                "Казкова вулиця",
                                                new LinearlyReferencedRange(0.0, (0.687779337 - startLr) * recalMult),
                                                null))),
                        OvertureSegmentSubtype.ROAD));
    }

    private OvertureRoadSegment getRealSegment() {
        return new OvertureRoadSegment(
                "148a5d18-3539-4fb4-ae35-deb298630a3e",
                geometryFactory.createLineString(new Coordinate[] {
                    new Coordinate(30.4378565, 50.3564498),
                    new Coordinate(30.4383579, 50.356785),
                    new Coordinate(30.4395787, 50.3576026),
                    new Coordinate(30.4428076, 50.3597785),
                    new Coordinate(30.4431411, 50.360004),
                    new Coordinate(30.4437997, 50.3604493),
                    new Coordinate(30.4439406, 50.3605446),
                    new Coordinate(30.4448602, 50.3611664),
                    new Coordinate(30.4451426, 50.3613574),
                    new Coordinate(30.4455037, 50.3615864),
                    new Coordinate(30.4465096, 50.3622335),
                    new Coordinate(30.4474623, 50.3628603),
                    new Coordinate(30.4484554, 50.3635137),
                    new Coordinate(30.4497642, 50.3644006)
                }),
                new OvertureRoadProperties(
                        List.of(
                                new OvertureConnector("85bb577c-c233-4a5b-82f8-c8c2a90ac09a", 0.0),
                                new OvertureConnector("8f94cbdc-0efd-48d1-93fe-f7dffbf55e7c", 0.513060188),
                                new OvertureConnector("80d60865-9a0c-4d6b-adf2-4b6b02207f6b", 0.590796018),
                                new OvertureConnector("1e3bdbd5-6d29-4064-84c5-9a012b13fc6a", 0.614671213),
                                new OvertureConnector("20678a1a-51f0-4620-9f6a-6f3852f65ffa", 0.727085704),
                                new OvertureConnector("075180f6-55c3-44f3-8783-5bceae6b4b82", 0.80647897),
                                new OvertureConnector("e4acaf8d-9758-48ef-b2c2-09b8355c9834", 0.88923974),
                                new OvertureConnector("86305427-0f8b-4384-9513-252c6635f747", 1.0)),
                        List.of(
                                new OvertureRoute(
                                        "Київ – Одеса", "ua:international", "М-05", null, "Q1051588", null),
                                new OvertureRoute("European route E95", "e-road", "E 95", null, "Q1165682", null)),
                        null,
                        emptyList(),
                        emptyList(),
                        List.of(
                                new OvertureRoadSurface(
                                        RoadSurfaceType.PAVED, new LinearlyReferencedRange(0.0, 0.445472102)),
                                new OvertureRoadSurface(
                                        RoadSurfaceType.PAVED, new LinearlyReferencedRange(0.614667246, 1.0))),
                        emptyList(),
                        List.of(
                                new OvertureSpeedLimit(
                                        new OvertureSpeed(110.0, SpeedUnit.KM_H),
                                        null,
                                        null,
                                        new LinearlyReferencedRange(0.0, 0.042135502),
                                        null),
                                new OvertureSpeedLimit(
                                        new OvertureSpeed(50.0, SpeedUnit.KM_H),
                                        null,
                                        null,
                                        new LinearlyReferencedRange(0.042135502, 1.0),
                                        null)),
                        emptyList(),
                        OvertureRoadSubclass.DRIVEWAY,
                        emptyList(),
                        List.of(
                                new OvertureAccessRestriction(
                                        AccessType.ALLOWED,
                                        new PropertyScopeContainer(
                                                null, null, null, null, new ArrayList<>(List.of(TravelMode.BICYCLE)), null),
                                        new LinearlyReferencedRange(0.614667246, 1.0)),
                                new OvertureAccessRestriction(
                                        AccessType.DENIED,
                                        new PropertyScopeContainer(
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                new ArrayList<>(List.of(new VehicleAttributes(
                                                        DimensionRestriction.HEIGHT,
                                                        ComparisonOperator.GREATER_THAN,
                                                        4.5,
                                                        Units.M)))),
                                        new LinearlyReferencedRange(0.614667246, 1.0)),
                                new OvertureAccessRestriction(
                                        AccessType.DENIED,
                                        new PropertyScopeContainer(null, null, null, null, null, null),
                                        null)),
                        0,
                        emptyList(),
                        OvertureTheme.TRANSPORTATION,
                        OvertureFeatureType.SEGMENT,
                        1,
                        List.of(
                                new OvertureSource(
                                        "",
                                        "OpenStreetMap",
                                        "ODbL-1.0",
                                        "w338761673@10",
                                        OffsetDateTime.parse("2025-06-05T13:24:35Z"),
                                        0,
                                        new LinearlyReferencedRange(0.0, 0.042135502)),
                                new OvertureSource(
                                        "",
                                        "OpenStreetMap",
                                        "ODbL-1.0",
                                        "w288980686@24",
                                        OffsetDateTime.parse("2024-11-07T06:25:48Z"),
                                        0,
                                        new LinearlyReferencedRange(0.042135502, 0.445472102)),
                                new OvertureSource(
                                        "",
                                        "OpenStreetMap",
                                        "ODbL-1.0",
                                        "w288980684@26",
                                        OffsetDateTime.parse("2024-11-07T06:25:46Z"),
                                        0,
                                        new LinearlyReferencedRange(0.445472102, 0.513063618)),
                                new OvertureSource(
                                        "",
                                        "OpenStreetMap",
                                        "ODbL-1.0",
                                        "w288980685@17",
                                        OffsetDateTime.parse("2024-11-07T06:25:47Z"),
                                        0,
                                        new LinearlyReferencedRange(0.513063618, 0.614667246)),
                                new OvertureSource(
                                        "routes", "OpenStreetMap", "ODbL-1.0", "r2314106@1098", null, 0, null),
                                new OvertureSource(
                                        "routes", "OpenStreetMap", "ODbL-1.0", "r23751@293", null, 0, null),
                                new OvertureSource(
                                        "",
                                        "OpenStreetMap",
                                        "ODbL-1.0",
                                        "w31875944@58",
                                        OffsetDateTime.parse("2024-11-06T20:36:07Z"),
                                        0,
                                        null)),
                        new OvertureNames(
                                "Казкова вулиця",
                                null,
                                List.of(
                                        new OvertureNameRule(
                                                Variant.COMMON,
                                                Bcp47LanguageTag.parse("ru"),
                                                null,
                                                "проспект Академика Глушкова",
                                                new LinearlyReferencedRange(0.042135502, 0.513063618),
                                                null),
                                        new OvertureNameRule(
                                                Variant.COMMON,
                                                Bcp47LanguageTag.parse("en"),
                                                null,
                                                "Akademika Hlushkova Avenue",
                                                new LinearlyReferencedRange(0.042135502, 1.0),
                                                null),
                                        new OvertureNameRule(
                                                Variant.COMMON,
                                                Bcp47LanguageTag.parse("pl"),
                                                null,
                                                "prospekt Akademika Głuszkowa",
                                                new LinearlyReferencedRange(0.042135502, 1.0),
                                                null),
                                        new OvertureNameRule(
                                                Variant.COMMON,
                                                null,
                                                null,
                                                "проспект Академіка Глушкова",
                                                new LinearlyReferencedRange(0.042135502, 1.0),
                                                null),
                                        new OvertureNameRule(
                                                Variant.COMMON,
                                                Bcp47LanguageTag.parse("uk-Latn"),
                                                null,
                                                "prospekt Akademika Hlushkova",
                                                new LinearlyReferencedRange(0.042135502, 1.0),
                                                null),
                                        new OvertureNameRule(
                                                Variant.COMMON,
                                                Bcp47LanguageTag.parse("uk"),
                                                null,
                                                "проспект Академіка Глушкова",
                                                new LinearlyReferencedRange(0.042135502, 1.0),
                                                null))),
                        OvertureSegmentSubtype.ROAD));
    }

    private OvertureRoadSegment getSubSegmentFromReal(double startLr, double endLr) {
        double recalMult = calculateMultiplier(startLr, endLr);
        return new OvertureRoadSegment(
                "148a5d18-3539-4fb4-ae35-deb298630a3e",
                geometryFactory.createLineString(new Coordinate[] {
                    new Coordinate(30.4451425, 50.3613573),
                    new Coordinate(30.4451426, 50.3613574),
                    new Coordinate(30.4455037, 50.3615864),
                    new Coordinate(30.4465096, 50.3622335),
                    new Coordinate(30.4474623, 50.3628603),
                    new Coordinate(30.4484554, 50.3635137),
                    new Coordinate(30.4485826, 50.3635999)
                }),
                new OvertureRoadProperties(
                        List.of(
                                new OvertureConnector(
                                        "1e3bdbd5-6d29-4064-84c5-9a012b13fc6a", (0.614671213 - startLr) * recalMult),
                                new OvertureConnector(
                                        "20678a1a-51f0-4620-9f6a-6f3852f65ffa", (0.727085704 - startLr) * recalMult),
                                new OvertureConnector(
                                        "075180f6-55c3-44f3-8783-5bceae6b4b82", (0.80647897 - startLr) * recalMult),
                                new OvertureConnector(
                                        "e4acaf8d-9758-48ef-b2c2-09b8355c9834", (0.88923974 - startLr) * recalMult)),
                        List.of(
                                new OvertureRoute(
                                        "Київ – Одеса", "ua:international", "М-05", null, "Q1051588", null),
                                new OvertureRoute("European route E95", "e-road", "E 95", null, "Q1165682", null)),
                        null,
                        emptyList(),
                        emptyList(),
                        List.of(new OvertureRoadSurface(
                                RoadSurfaceType.PAVED,
                                new LinearlyReferencedRange((0.614667246 - startLr) * recalMult, 1.0))),
                        emptyList(),
                        List.of(new OvertureSpeedLimit(
                                new OvertureSpeed(50.0, SpeedUnit.KM_H),
                                null,
                                null,
                                new LinearlyReferencedRange(0.0, 1.0),
                                null)),
                        emptyList(),
                        OvertureRoadSubclass.DRIVEWAY,
                        emptyList(),
                        List.of(
                                new OvertureAccessRestriction(
                                        AccessType.ALLOWED,
                                        new PropertyScopeContainer(
                                                null, null, null, null, new ArrayList<>(List.of(TravelMode.BICYCLE)), null),
                                        new LinearlyReferencedRange((0.614667246 - startLr) * recalMult, 1.0)),
                                new OvertureAccessRestriction(
                                        AccessType.DENIED,
                                        new PropertyScopeContainer(
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                new ArrayList<>(List.of(new VehicleAttributes(
                                                        DimensionRestriction.HEIGHT,
                                                        ComparisonOperator.GREATER_THAN,
                                                        4.5,
                                                        Units.M)))),
                                        new LinearlyReferencedRange((0.614667246 - startLr) * recalMult, 1.0)),
                                new OvertureAccessRestriction(
                                        AccessType.DENIED,
                                        new PropertyScopeContainer(null, null, null, null, null, null),
                                        null)),
                        0,
                        emptyList(),
                        OvertureTheme.TRANSPORTATION,
                        OvertureFeatureType.SEGMENT,
                        1,
                        List.of(
                                // Source "w288980685@17" spans [0.513063618, 0.614667246] in the parent
                                // segment, ending exactly where this sub-segment starts (startLr =
                                // 0.614667246). It has zero overlap with this sub-segment and is excluded:
                                // range bounds are exclusive, so a property whose range ends at the
                                // sub-segment's start does not apply to it. It was previously retained and
                                // collapsed to a degenerate [0.0, 0.0] range.

                                new OvertureSource(
                                        "routes", "OpenStreetMap", "ODbL-1.0", "r2314106@1098", null, 0, null),
                                new OvertureSource(
                                        "routes", "OpenStreetMap", "ODbL-1.0", "r23751@293", null, 0, null),
                                new OvertureSource(
                                        "",
                                        "OpenStreetMap",
                                        "ODbL-1.0",
                                        "w31875944@58",
                                        OffsetDateTime.parse("2024-11-06T20:36:07Z"),
                                        0,
                                        null)),
                        new OvertureNames(
                                "Казкова вулиця",
                                null,
                                List.of(
                                        new OvertureNameRule(
                                                Variant.COMMON,
                                                Bcp47LanguageTag.parse("en"),
                                                null,
                                                "Akademika Hlushkova Avenue",
                                                new LinearlyReferencedRange(0.0, 1.0),
                                                null),
                                        new OvertureNameRule(
                                                Variant.COMMON,
                                                Bcp47LanguageTag.parse("pl"),
                                                null,
                                                "prospekt Akademika Głuszkowa",
                                                new LinearlyReferencedRange(0.0, 1.0),
                                                null),
                                        new OvertureNameRule(
                                                Variant.COMMON,
                                                null,
                                                null,
                                                "проспект Академіка Глушкова",
                                                new LinearlyReferencedRange(0.0, 1.0),
                                                null),
                                        new OvertureNameRule(
                                                Variant.COMMON,
                                                Bcp47LanguageTag.parse("uk-Latn"),
                                                null,
                                                "prospekt Akademika Hlushkova",
                                                new LinearlyReferencedRange(0.0, 1.0),
                                                null),
                                        new OvertureNameRule(
                                                Variant.COMMON,
                                                Bcp47LanguageTag.parse("uk"),
                                                null,
                                                "проспект Академіка Глушкова",
                                                new LinearlyReferencedRange(0.0, 1.0),
                                                null))),
                        OvertureSegmentSubtype.ROAD));
    }

    private double calculateMultiplier(double startLr, double endLr) {
        return 1.0 / (endLr - startLr);
    }
}
