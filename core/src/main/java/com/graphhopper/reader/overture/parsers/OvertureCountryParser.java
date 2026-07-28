package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.routing.ev.Country;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.ev.State;
import com.graphhopper.routing.util.AreaIndex;
import com.graphhopper.routing.util.CustomArea;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves the country an edge lies in from GraphHopper's custom-area index.
 *
 * <p>Country is not an Overture attribute: it comes from the country polygons GraphHopper loads
 * itself, the same source the OSM reader uses. {@code OvertureReader} was already handed an {@link
 * AreaIndex} by the import pipeline but never read it, so the {@code country} encoded value stayed
 * {@link Country#MISSING} on every edge, and custom models keyed on country silently did nothing.
 *
 * <p>Selection mirrors {@code OSMReader}: the smallest matching area wins, and an area naming a
 * subdivision outranks one that does not, so a US state beats the country-level polygon.
 */
public final class OvertureCountryParser implements OvertureTagParser {

    private final EnumEncodedValue<Country> countryEnc;

    /**
     * @param countryEnc the encoded value for the country
     */
    public OvertureCountryParser(EnumEncodedValue<Country> countryEnc) {
        this.countryEnc = countryEnc;
    }

    /**
     * Resolves and writes the country for one edge.
     *
     * <p>Unlike most Overture parsers this one reads the {@code context}: country comes from where the
     * edge is, not from what the segment says.
     *
     * @param edge the graph edge to update
     * @param segment unused; the segment carries no country attribute
     * @param context supplies the geometry and the custom-area index
     */
    @Override
    public void handleSegment(
            EdgeIteratorState edge, OvertureRoadSegment segment, OvertureSegmentContext context) {
        Country country = resolveCountry(context.getGeometry(), context.getAreaIndex());
        if (country != Country.MISSING) edge.set(countryEnc, country);
    }

    /**
     * Resolves the country containing the middle of {@code geometry}.
     *
     * <p>The midpoint is used rather than an end node because an end node may sit exactly on a
     * border, where the lookup could land on either side.
     *
     * @param geometry the edge geometry
     * @param areaIndex the custom-area index, may be {@code null}
     * @return the country, or {@link Country#MISSING} when it cannot be determined
     */
    public static Country resolveCountry(
            PointList geometry, @Nullable AreaIndex<CustomArea> areaIndex) {
        if (areaIndex == null || geometry == null || geometry.isEmpty()) return Country.MISSING;

        int middle = geometry.size() / 2;
        double lat = geometry.getLat(middle);
        double lon = geometry.getLon(middle);

        Country country = Country.MISSING;
        State state = State.MISSING;
        double smallestArea = Double.POSITIVE_INFINITY;

        for (CustomArea customArea : areaIndex.query(lat, lon)) {
            if (customArea.getProperties() == null) continue;
            String alpha2WithSubdivision = (String) customArea.getProperties().get(State.ISO_3166_2);
            if (alpha2WithSubdivision == null) continue;

            // Either a plain country code such as "DE", or one including a subdivision such as "US-CA".
            String[] parts = alpha2WithSubdivision.split("-");
            if (parts.length == 0 || parts.length > 2) continue;
            Country candidate = Country.find(parts[0]);
            if (candidate == null) continue;

            boolean hasSubdivision = parts.length == 2;
            // A subdivision-bearing area outranks a plain country area, and among equals the smaller
            // polygon wins, so nested areas resolve to the most specific one.
            boolean better = hasSubdivision
                    ? (state == State.MISSING || customArea.getArea() < smallestArea)
                    : (state == State.MISSING && customArea.getArea() < smallestArea);
            if (better) {
                country = candidate;
                state = State.find(alpha2WithSubdivision);
                smallestArea = customArea.getArea();
            }
        }
        return country;
    }

    /**
     * @param geometry the edge geometry
     * @param areaIndex the custom-area index, may be {@code null}
     * @return every custom area covering the middle of the geometry
     */
    public static List<CustomArea> customAreasAt(
            PointList geometry, @Nullable AreaIndex<CustomArea> areaIndex) {
        if (areaIndex == null || geometry == null || geometry.isEmpty()) return List.of();
        int middle = geometry.size() / 2;
        return areaIndex.query(geometry.getLat(middle), geometry.getLon(middle));
    }
}
