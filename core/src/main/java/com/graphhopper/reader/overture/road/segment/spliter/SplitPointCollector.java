package com.graphhopper.reader.overture.road.segment.spliter;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import com.graphhopper.reader.overture.access.restriction.OvertureAccessRestriction;
import com.graphhopper.reader.overture.common.speed.OvertureSpeedLimit;
import com.graphhopper.reader.overture.names.OvertureNameRule;
import com.graphhopper.reader.overture.road.flags.OvertureRoadFlags;
import com.graphhopper.reader.overture.road.segment.OvertureConnector;
import com.graphhopper.reader.overture.road.segment.OvertureRoadProperties;
import com.graphhopper.reader.overture.road.surface.OvertureRoadSurface;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for determining the precise locations where an Overture road segment
 * must be divided into smaller sub-segments.
 * <p>
 * This collector identifies "split points" based on physical connections (connectors)
 * and attribute changes (speed limits, surface changes, etc.) to ensure that
 * each resulting edge in the routing graph has uniform properties.
 * </p>
 */
public class SplitPointCollector {

    private static final Logger logger = LoggerFactory.getLogger(SplitPointCollector.class);

    /** Warn once per property kind rather than once per segment, which would flood the import log. */
    private static final Set<String> UNCOLLECTED_WARNED = ConcurrentHashMap.newKeySet();

    private SplitPointCollector() {}
    /**
     * Collects unique split points (0.0 to 1.0) from connectors
     * and property ranges to determine where the segment should be divided.
     * @return a {@link TreeSet} of unique split points in ascending order
     */
    public static TreeSet<Double> collectSplitPoints(OvertureRoadProperties prop) {
        TreeSet<Double> splitPoints = new TreeSet<>();
        splitPoints.add(0.0);
        splitPoints.add(1.0);

        // Add points for all properties that require a segment split
        addRangePoints(prop.getSpeedLimits(), splitPoints, OvertureSpeedLimit::getBetween);
        addRangePoints(prop.getSurfaces(), splitPoints, OvertureRoadSurface::getBetween);
        addRangePoints(prop.getFlags(), splitPoints, OvertureRoadFlags::getBetween);
        addRangePoints(
                prop.getAccessRestrictions(), splitPoints, OvertureAccessRestriction::getBetween);
        if (prop.getNames() != null)
            addRangePoints(prop.getNames().getRules(), splitPoints, OvertureNameRule::getBetween);

        addConnectorPoints(prop.getConnectors(), splitPoints);
        warnAboutUncollectedRanges(prop);
        return splitPoints;
    }

    /**
     * Warns when a range-scoped property that no parser reads becomes one that some parser reads.
     *
     * <p>A split point costs an edge. Splitting on a property's range is only worth it when some parser
     * reads that property, because splitting is what makes the {@code getFirst()} calls parsers rely on
     * safe. The five collected above are exactly the ones a parser reads today - speed limits, surfaces,
     * flags, access restrictions and name rules.
     *
     * <p>The properties in {@link #NOT_READ_BY_ANY_PARSER} are populated (both ingest paths extract
     * them) but deliberately not split on, because nothing consumes them yet. Splitting on them would
     * fragment every graph to preserve variation no parser looks at.
     *
     * <p>That makes this a contract rather than a data check: <b>when you write a parser that reads one
     * of these, remove it from that set.</b> This method then warns until its range is added to {@link
     * #collectSplitPoints}, so the transition from "unread" to "read" cannot happen silently and leave
     * a parser quietly discarding variation.
     */
    private static void warnAboutUncollectedRanges(OvertureRoadProperties prop) {
        warnIfPopulatedAndRead(prop.getWidthRules(), "widthRules");
        warnIfPopulatedAndRead(prop.getSubclassRules(), "subclassRules");
        warnIfPopulatedAndRead(prop.getLevelRules(), "levelRules");
        warnIfPopulatedAndRead(prop.getProhibitedTransitions(), "prohibitedTransitions");
        warnIfPopulatedAndRead(prop.getRoutes(), "routes");
        warnIfPopulatedAndRead(prop.getDestinations(), "destinations");
        warnIfPopulatedAndRead(prop.getSources(), "sources");
    }

    /**
     * Range-scoped properties that are extracted but that no parser reads, so their ranges are
     * deliberately not split on.
     *
     * <p>Remove an entry the moment a parser starts reading it. {@code destinations} is listed for
     * completeness even though it carries no {@code between} range at all in either format.
     */
    private static final Set<String> NOT_READ_BY_ANY_PARSER = Set.of(
            "widthRules",
            "subclassRules",
            "levelRules",
            "prohibitedTransitions",
            "routes",
            "destinations",
            "sources");

    private static void warnIfPopulatedAndRead(List<?> items, String propertyName) {
        if (NOT_READ_BY_ANY_PARSER.contains(propertyName)) return;

        if (items != null && !items.isEmpty() && UNCOLLECTED_WARNED.add(propertyName)) {
            logger.warn(
                    "Overture property '{}' is populated and read by a parser but contributes no segment"
                            + " split points, so sub-segments may carry more than one value for it and the"
                            + " parser will use only the first. Add its range to"
                            + " SplitPointCollector.collectSplitPoints.",
                    propertyName);
        }
    }
    /// Extracts start and end points from a list of linearly referenced items and adds them to the
    // set.
    private static <T> void addRangePoints(
            List<T> items, TreeSet<Double> points, Function<T, LinearlyReferencedRange> rangeExtractor) {
        if (items == null || items.isEmpty()) return;
        for (T item : items) {
            var range = rangeExtractor.apply(item);
            if (range != null) {
                points.add(range.getStart());
                points.add(range.getEnd());
            }
        }
    }
    /// Adds connector_at values to the set of split points.
    private static void addConnectorPoints(
            List<OvertureConnector> connectors, TreeSet<Double> points) {
        if (connectors == null || connectors.isEmpty()) return;
        connectors.forEach(c -> points.add(c.getAt()));
    }
}
