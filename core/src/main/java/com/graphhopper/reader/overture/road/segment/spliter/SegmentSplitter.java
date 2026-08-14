package com.graphhopper.reader.overture.road.segment.spliter;

import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;

import java.util.List;
import java.util.TreeSet;
import java.util.ArrayList;

import static com.graphhopper.reader.overture.road.segment.spliter.SubSegmentProcessor.getSubLineString;
import static com.graphhopper.reader.overture.road.segment.spliter.SubSegmentProcessor.getPropertiesBetween;

/**
 * Provides functionality to divide an {@link OvertureRoadSegment} into multiple subsegments
 * based on linear referencing of properties or physical connectors.
 */
public final class SegmentSplitter {

    private SegmentSplitter() {}

    /**
     * Splits a given road segment into a list of sub-segments based on linear referencing (LR)
     * values and connector points.
     *
     * @param segment the original road segment to be evaluated for splitting
     * @return a {@link List} of {@link OvertureRoadSegment} objects. Returns an immutable
     * single-element list if no splits are required.
     */
    public static List<OvertureRoadSegment> split(OvertureRoadSegment segment) {
        TreeSet<Double> splitPoints = SplitPointCollector.collectSplitPoints(segment.getProperties());
        if (splitPoints.size() == 2) return List.of(segment);

        List<OvertureRoadSegment> splitSegments = new ArrayList<>(splitPoints.size() - 1);
        for (Double splitPoint : splitPoints) {
            Double endLr = splitPoints.higher(splitPoint);
            if (endLr != null) {
                splitSegments.add(processSubSegment(segment, splitPoint, endLr));
            }
        }
        return splitSegments;
    }

    /**
     * Processing the extraction of a subSegment from the passed segment between the start and end linearly-references.
     * @param segment the main segment from which the subSegment will be extracted.
     * @param startLr start linearly-referenced position
     * @param endLr end linearly-referenced position
     * @return subSegment in corresponding range
     */
    public static OvertureRoadSegment processSubSegment(OvertureRoadSegment segment,
                                                        double startLr, double endLr) {
        if (segment == null)
            return null;

        return new OvertureRoadSegment(
                segment.getId(),
                getSubLineString(segment.getLineString(), startLr, endLr),
                getPropertiesBetween(segment.getProperties(), startLr, endLr)
        );
    }

}
