package com.graphhopper.reader.overture.road.segment;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import java.util.Objects;

/**
 * Represents a route that this segment belongs to.
 * <p>
 * Routes typically represent highway systems (e.g., "I-5") or named networks.
 * A single segment can belong to multiple routes.
 * </p>
 */
public class OvertureRoute implements HasBetweenProperty {

    /**
     * Full name of the route (e.g., "Pacific Coast Highway").
     */
    private final String name;

    /**
     * Name of the highway system this route belongs to (e.g., "US:I", "US:US").
     */
    private final String network;

    /**
     * Code or number used to reference the route (e.g., "5", "66", "M1").
     */
    private final String ref;

    /**
     * URL or description of route signage.
     */
    private final String symbol;

    /**
     * Wikidata identifier for the route.
     */
    private final String wikidata;

    /**
     * The linear range along the segment where this route applies.
     * If null, the route applies to the entire segment.
     */
    private final LinearlyReferencedRange between;

    public OvertureRoute(
            String name,
            String network,
            String ref,
            String symbol,
            String wikidata,
            LinearlyReferencedRange between) {
        this.name = name;
        this.network = network;
        this.ref = ref;
        this.symbol = symbol;
        this.wikidata = wikidata;
        this.between = between;
    }

    /**
     * Gets the full name of the route.
     *
     * @return the route name, or null if not present.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the name of the highway system this route belongs to.
     *
     * @return the network name, or null if not present.
     */
    public String getNetwork() {
        return network;
    }

    /**
     * Gets the code or number used to reference the route (e.g., "66", "M1").
     *
     * @return the route reference, or null if not present.
     */
    public String getRef() {
        return ref;
    }

    /**
     * Gets the URL or description of the route signage.
     *
     * @return the symbol string, or null if not present.
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Gets the Wikidata identifier for this route.
     *
     * @return the Wikidata ID, or null if not present.
     */
    public String getWikidata() {
        return wikidata;
    }

    /**
     * Gets the linear range along the segment where this route applies.
     *
     * @return the {@link LinearlyReferencedRange}, or null if it applies to the whole segment.
     */
    @Override
    public LinearlyReferencedRange getBetween() {
        return between;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OvertureRoute that)) return false;
        return Objects.equals(name, that.name)
                && Objects.equals(network, that.network)
                && Objects.equals(ref, that.ref)
                && Objects.equals(symbol, that.symbol)
                && Objects.equals(wikidata, that.wikidata)
                && Objects.equals(between, that.between);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getName(), getNetwork(), getRef(), getSymbol(), getWikidata(), getBetween());
    }

    /**
     * Returns a string representation of the OvertureRoute.
     *
     * @return a string containing the route's properties.
     */
    @Override
    public String toString() {
        return "OvertureRoute{" + "name='"
                + name + '\'' + ", network='"
                + network + '\'' + ", ref='"
                + ref + '\'' + ", symbol='"
                + symbol + '\'' + ", wikidata='"
                + wikidata + '\'' + ", between="
                + between + '}';
    }
}
