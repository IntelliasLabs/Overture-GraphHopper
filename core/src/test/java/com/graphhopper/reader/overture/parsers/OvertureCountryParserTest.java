package com.graphhopper.reader.overture.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.graphhopper.routing.ev.Country;
import com.graphhopper.routing.ev.State;
import com.graphhopper.routing.util.AreaIndex;
import com.graphhopper.routing.util.CustomArea;
import com.graphhopper.util.PointList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

/**
 * Covers resolving the {@code country} encoded value from the custom-area index.
 *
 * <p>The index is supplied by the import pipeline and was previously stored and ignored, so the
 * encoded value stayed {@link Country#MISSING} and any custom model keyed on country did nothing.
 */
class OvertureCountryParserTest {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    @Test
    @DisplayName("The country containing the geometry midpoint is resolved")
    void resolvesContainingCountry() {
        AreaIndex<CustomArea> index = indexOf(area("DE", box(0, 0, 10, 10)));

        assertEquals(Country.DEU, OvertureCountryParser.resolveCountry(lineFrom(1, 1, 3, 3), index));
    }

    @Test
    @DisplayName("A geometry outside every area resolves to MISSING")
    void outsideEveryAreaIsMissing() {
        AreaIndex<CustomArea> index = indexOf(area("DE", box(0, 0, 10, 10)));

        assertEquals(
                Country.MISSING, OvertureCountryParser.resolveCountry(lineFrom(50, 50, 51, 51), index));
    }

    @Test
    @DisplayName("An area naming a subdivision outranks the plain country area containing it")
    void subdivisionBeatsPlainCountry() {
        // A US-CA polygon nested inside a larger US polygon: the more specific one must win, which is
        // what makes state-level country rules work.
        AreaIndex<CustomArea> index =
                indexOf(area("US", box(0, 0, 20, 20)), area("US-CA", box(0, 0, 5, 5)));

        assertEquals(Country.USA, OvertureCountryParser.resolveCountry(lineFrom(1, 1, 2, 2), index));
    }

    @Test
    @DisplayName("Among plain country areas the smaller polygon wins")
    void smallerAreaWins() {
        AreaIndex<CustomArea> index =
                indexOf(area("DE", box(0, 0, 20, 20)), area("AT", box(0, 0, 4, 4)));

        assertEquals(Country.AUT, OvertureCountryParser.resolveCountry(lineFrom(1, 1, 2, 2), index));
    }

    @Test
    @DisplayName("Areas without an ISO code are ignored rather than failing")
    void areasWithoutIsoCodeAreIgnored() {
        Map<String, Object> noIso = new HashMap<>();
        noIso.put("id", "some-custom-zone");
        CustomArea zone = new CustomArea(noIso, List.of(box(0, 0, 10, 10)));

        assertEquals(
                Country.MISSING, OvertureCountryParser.resolveCountry(lineFrom(1, 1, 2, 2), indexOf(zone)));
    }

    @Test
    @DisplayName("A null index or empty geometry resolves to MISSING without throwing")
    void degenerateInputsAreSafe() {
        assertEquals(Country.MISSING, OvertureCountryParser.resolveCountry(lineFrom(1, 1, 2, 2), null));
        assertEquals(
                Country.MISSING,
                OvertureCountryParser.resolveCountry(
                        new PointList(), indexOf(area("DE", box(0, 0, 10, 10)))));
    }

    private static AreaIndex<CustomArea> indexOf(CustomArea... areas) {
        return new AreaIndex<>(List.of(areas));
    }

    private static CustomArea area(String isoCode, Polygon border) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(State.ISO_3166_2, isoCode);
        return new CustomArea(properties, List.of(border));
    }

    /** An axis-aligned box in (lon, lat) order, matching JTS convention. */
    private static Polygon box(double minLat, double minLon, double maxLat, double maxLon) {
        return GEOMETRY_FACTORY.createPolygon(new Coordinate[] {
            new Coordinate(minLon, minLat),
            new Coordinate(maxLon, minLat),
            new Coordinate(maxLon, maxLat),
            new Coordinate(minLon, maxLat),
            new Coordinate(minLon, minLat),
        });
    }

    private static PointList lineFrom(double fromLat, double fromLon, double toLat, double toLon) {
        PointList points = new PointList(2, false);
        points.add(fromLat, fromLon);
        points.add(toLat, toLon);
        return points;
    }
}
