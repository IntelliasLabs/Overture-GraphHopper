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
import java.util.TreeSet;
import java.util.function.Function;

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
        return splitPoints;
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
