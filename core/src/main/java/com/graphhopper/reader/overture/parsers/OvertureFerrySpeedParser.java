package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.util.EdgeIteratorState;

/**
 * Writes {@code ferry_speed} on water segments.
 *
 * <p>The Overture counterpart to {@code FerrySpeedCalculator}, which cannot be reused directly: it is
 * a {@code TagParser} that reads OSM's {@code route=ferry} plus the artificial {@code
 * duration_in_seconds} / {@code way_distance_2d} tags that {@code OSMReader} synthesises. Overture
 * has neither - a ferry is a segment of subtype {@code water}, and the schema carries no crossing
 * duration at all - so only the distance-based fallback applies.
 *
 * <p>The fallback thresholds are copied from {@code FerrySpeedCalculator#getSpeed} rather than
 * invented, so a ferry gets the same speed whichever reader imported it. The shuttle-train doubling
 * is left out: it keys off {@code route=shuttle_train}, and Overture has no equivalent subtype.
 *
 * <p>This parser exists because every in-built speed custom model now opens with {@code { "if":
 * "road_environment == FERRY", "limit_to": "ferry_speed" }}. Leaving the encoded value unwritten
 * would store 0 on ferry edges - which reads as a real limit of 0 km/h, not as "unknown" - and make
 * every ferry unroutable rather than slow.
 */
public final class OvertureFerrySpeedParser implements OvertureTagParser {

    /** Below this length, a segment is treated as the stub of a longer crossing. */
    private static final double SHORT_FERRY_M = 1_000;

    /** Above this length, the crossing is long enough to assume an open-water speed. */
    private static final double LONG_FERRY_M = 30_000;

    private final DecimalEncodedValue ferrySpeedEnc;

    /**
     * @param ferrySpeedEnc the encoded value for the ferry speed
     */
    public OvertureFerrySpeedParser(DecimalEncodedValue ferrySpeedEnc) {
        this.ferrySpeedEnc = ferrySpeedEnc;
    }

    /**
     * Writes the ferry speed for one edge, leaving non-ferry edges untouched.
     *
     * @param edge the graph edge to update
     * @param segment the sub-segment being imported
     * @param context unused; the speed follows from the edge's own length
     */
    @Override
    public void handleSegment(
            EdgeIteratorState edge, OvertureRoadSegment segment, OvertureSegmentContext context) {
        if (!OvertureFerryParser.isFerry(segment)) return;
        edge.set(ferrySpeedEnc, clamp(speedFor(edge.getDistance()), ferrySpeedEnc));
    }

    /**
     * Guesses a crossing speed from the edge length, the only signal Overture offers.
     *
     * @param distanceMeters the edge length in metres
     * @return the speed in km/h
     */
    static double speedFor(double distanceMeters) {
        if (distanceMeters < SHORT_FERRY_M) return 5;
        return distanceMeters < LONG_FERRY_M ? 15 : 30;
    }

    /**
     * Keeps the value inside what the encoded value can store. The lower bound is the smallest
     * non-zero value rather than zero, because zero would mean "no speed" and block the edge.
     *
     * @param speed the speed in km/h
     * @param enc the encoded value it will be stored in
     * @return the storable speed
     */
    private static double clamp(double speed, DecimalEncodedValue enc) {
        return Math.max(enc.getSmallestNonZeroValue(), Math.min(speed, enc.getMaxStorableDecimal()));
    }
}
