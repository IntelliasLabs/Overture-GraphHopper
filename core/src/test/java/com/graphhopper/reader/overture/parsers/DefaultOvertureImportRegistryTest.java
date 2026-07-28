package com.graphhopper.reader.overture.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.graphhopper.reader.DataReaderConfig;
import com.graphhopper.reader.overture.OvertureTestFixtures;
import com.graphhopper.routing.ev.BusAccess;
import com.graphhopper.routing.ev.Country;
import com.graphhopper.routing.ev.DefaultImportRegistry;
import com.graphhopper.routing.ev.ImportUnit;
import com.graphhopper.routing.ev.MaxSpeed;
import com.graphhopper.routing.ev.VehicleAccess;
import com.graphhopper.routing.ev.VehicleSpeed;
import com.graphhopper.routing.util.EncodingManager;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Covers the Overture import registry and the parser pipeline it assembles. */
class DefaultOvertureImportRegistryTest {

    private final DefaultOvertureImportRegistry registry = new DefaultOvertureImportRegistry();

    @Test
    @DisplayName("Encoded value creation is delegated, so layouts match the OSM pipeline")
    void encodedValueCreationIsDelegated() {
        ImportUnit unit = registry.createImportUnit(MaxSpeed.KEY);

        assertNotNull(unit);
        assertNotNull(
                unit.getCreateEncodedValue(),
                "the encoded value must still be created, only the OSM tag parser is dropped");
    }

    @Test
    @DisplayName("The OSM tag parser is dropped so no OSM parser can run on Overture data")
    void osmTagParsersAreSuppressed() {
        // Without this, buildOSMParsers would assemble parsers expecting OSM tags that Overture has
        // none of. Making it structural beats relying on the reader to ignore them.
        for (String name : List.of(MaxSpeed.KEY, VehicleAccess.key("car"), "road_class")) {
            assertNull(registry.createImportUnit(name).getCreateTagParser(), name);
        }
    }

    @Test
    @DisplayName("An unknown encoded value is still rejected, as upstream would")
    void unknownEncodedValueIsRejected() {
        assertNull(registry.createImportUnit("definitely_not_an_encoded_value"));
    }

    @Test
    @DisplayName("car_average_speed declares its dependency on max_speed")
    void carSpeedDependsOnMaxSpeed() {
        // The Overture car speed parser reads the posted limit back off the edge, so this ordering is
        // load-bearing rather than decorative.
        assertEquals(
                List.of(MaxSpeed.KEY),
                registry.createImportUnit(VehicleSpeed.key("car")).getRequiredImportUnits());
    }

    @Test
    @DisplayName("car_access drops the OSM roundabout dependency Overture cannot satisfy")
    void carAccessHasNoRoundaboutDependency() {
        assertEquals(
                List.of(), registry.createImportUnit(VehicleAccess.key("car")).getRequiredImportUnits());
    }

    @Test
    @DisplayName("max_speed is ordered before car_average_speed in the assembled pipeline")
    void maxSpeedParserRunsBeforeCarSpeedParser() {
        EncodingManager em = OvertureTestFixtures.minimalEncodingManager();

        List<OvertureTagParser> assembled = OvertureParsers.build(registry, em).getSegmentParsers();

        int maxSpeedAt = indexOfType(assembled, OvertureMaxSpeedParser.class);
        int carSpeedAt = indexOfType(assembled, OvertureCarAverageSpeedParser.class);
        assertTrue(maxSpeedAt >= 0, "max_speed parser missing from the pipeline");
        assertTrue(carSpeedAt >= 0, "car speed parser missing from the pipeline");
        assertTrue(
                maxSpeedAt < carSpeedAt,
                "max_speed must be written before the car speed parser reads it, but got indices "
                        + maxSpeedAt + " and " + carSpeedAt);
    }

    @Test
    @DisplayName("The street-name parser runs even though no encoded value declares it")
    void streetNameParserIsAlwaysOn() {
        List<OvertureTagParser> assembled = OvertureParsers.build(
                        registry, OvertureTestFixtures.minimalEncodingManager())
                .getSegmentParsers();

        assertTrue(
                indexOfType(assembled, OvertureNameParser.class) >= 0,
                "street names are key-values, so nothing in graph.encoded_values pulls this in");
    }

    @Test
    @DisplayName("parse_way_names=false leaves the street-name parser out of the pipeline entirely")
    void parseWayNamesDisabledDropsTheStreetNameParser() {
        List<OvertureTagParser> assembled = OvertureParsers.build(
                        registry,
                        OvertureTestFixtures.minimalEncodingManager(),
                        new DataReaderConfig().setParseWayNames(false))
                .getSegmentParsers();

        assertEquals(
                -1,
                indexOfType(assembled, OvertureNameParser.class),
                "disabling name parsing should remove the parser, not run one that writes nothing");
    }

    @Test
    @DisplayName("The configured preferred language reaches the street-name parser")
    void preferredLanguageReachesTheStreetNameParser() {
        List<OvertureTagParser> assembled = OvertureParsers.build(
                        registry,
                        OvertureTestFixtures.minimalEncodingManager(),
                        new DataReaderConfig().setPreferredLanguage("uk"))
                .getSegmentParsers();

        int at = indexOfType(assembled, OvertureNameParser.class);
        assertTrue(at >= 0, "street-name parser missing from the pipeline");
        assertEquals("uk", ((OvertureNameParser) assembled.get(at)).getPreferredLanguage());
    }

    @Test
    @DisplayName("A missing required encoded value fails assembly and names every gap")
    void missingRequiredEncodedValueFailsAssembly() {
        // Only car access: everything else the registry considers required is absent.
        EncodingManager sparse =
                EncodingManager.start().add(VehicleAccess.create("car")).build();

        IllegalStateException thrown =
                assertThrows(IllegalStateException.class, () -> OvertureParsers.build(registry, sparse));

        assertTrue(thrown.getMessage().contains("surface"), thrown.getMessage());
        assertTrue(thrown.getMessage().contains(MaxSpeed.KEY), thrown.getMessage());
        assertTrue(thrown.getMessage().contains("graph.encoded_values"), thrown.getMessage());
    }

    @Test
    @DisplayName("Optional encoded values may be absent without failing")
    void optionalEncodedValuesMayBeAbsent() {
        // The fixture omits country and the slope values; assembly must still succeed, because those
        // depend on an area index and an elevation provider the reader may not have.
        assertNotNull(OvertureParsers.build(registry, OvertureTestFixtures.minimalEncodingManager()));

        assertTrue(registry.optionalParserNames().contains(Country.KEY));
    }

    @Test
    @DisplayName("Every name the registry claims to parse has a factory")
    void everyClaimedNameHasAFactory() {
        for (String name : registry.parserNames()) {
            assertNotNull(registry.createSegmentParser(name), name);
        }
    }

    @Test
    @DisplayName("bus_access is delegated like every other encoded value")
    void busAccessIsDelegated() {
        // Upstream owns bus_access through BusAccess, not the VehicleAccess family that car, bike and
        // foot use. It is still the same key, so delegation creates it and this registry must not
        // declare its own - that would substitute a local bit layout for upstream's.
        assertEquals(BusAccess.KEY, VehicleAccess.key("bus"));
        assertNotNull(new DefaultImportRegistry().createImportUnit(BusAccess.KEY));

        ImportUnit unit = registry.createImportUnit(BusAccess.KEY);
        assertNotNull(unit);
        assertNotNull(unit.getCreateEncodedValue());
        assertNull(unit.getCreateTagParser(), "no OSM parser may run on Overture data");
    }

    @Test
    @DisplayName("bus_access drops the roundabout dependency, as car_access does")
    void busAccessHasNoRoundaboutDependency() {
        // Upstream's bus_access uses ModeAccessParser, which reads roundabout to infer implied oneways.
        // Overture has no roundabout attribute, so keeping the edge would order a parser that never
        // runs.
        assertEquals(List.of(), registry.createImportUnit(BusAccess.KEY).getRequiredImportUnits());
    }

    @Test
    @DisplayName("bus_access is optional, so imports without a bus profile still assemble")
    void busAccessIsOptional() {
        // The fixture does not declare bus_access. If it were required, every existing import would
        // start failing.
        assertTrue(registry.optionalParserNames().contains(VehicleAccess.key("bus")));

        List<OvertureTagParser> assembled = OvertureParsers.build(
                        registry, OvertureTestFixtures.minimalEncodingManager())
                .getSegmentParsers();

        assertEquals(
                -1,
                indexOfType(assembled, OvertureBusAccessParser.class),
                "the bus parser must stay out of the pipeline when bus_access was not created");
    }

    @Test
    @DisplayName("The bus parser joins the pipeline once bus_access is declared")
    void busParserRunsWhenBusAccessExists() {
        // The minimal fixture plus bus_access, so only the bus parser differs from the other tests.
        EncodingManager withBus = OvertureTestFixtures.minimalEncodingManagerWith(BusAccess.create());

        List<OvertureTagParser> assembled =
                OvertureParsers.build(registry, withBus).getSegmentParsers();

        assertTrue(indexOfType(assembled, OvertureBusAccessParser.class) >= 0);
    }

    private static int indexOfType(List<OvertureTagParser> parsers, Class<?> type) {
        for (int i = 0; i < parsers.size(); i++) {
            if (type.isInstance(parsers.get(i))) return i;
        }
        return -1;
    }
}
