package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.access.restriction.scope.OvertureScopes;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelHeading;
import com.graphhopper.reader.overture.common.speed.OvertureSpeedLimit;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.MaxSpeed;
import com.graphhopper.util.EdgeIteratorState;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * Writes the posted speed limit from Overture data into the {@code max_speed} encoded value.
 *
 * <p>Overture states speed limits explicitly, per direction, in {@code properties.speed_limits}.
 * Before this parser existed those limits were consumed only by {@link
 * OvertureCarAverageSpeedParser} to derive an average speed and then discarded, so {@code max_speed}
 * stayed at its storage default of 0 km/h on every edge. That is worse than merely unset: {@link
 * MaxSpeed#MAXSPEED_MISSING} is positive infinity, so a 0 reads as a real limit of zero, and
 * {@code MaxSpeedCalculator} skips any edge whose value is not {@code MAXSPEED_MISSING}, meaning the
 * legal-default-speeds fallback could not rescue it either.
 *
 * <p>Directional limits win over unscoped ones. Where several limits apply to the same direction the
 * lowest is used, which is the safe reading of overlapping restrictions.
 */
public final class OvertureMaxSpeedParser implements OvertureTagParser {

    private final DecimalEncodedValue maxSpeedEnc;

    /**
     * @param maxSpeedEnc the encoded value for the posted speed limit
     */
    public OvertureMaxSpeedParser(DecimalEncodedValue maxSpeedEnc) {
        this.maxSpeedEnc = maxSpeedEnc;
    }

    /**
     * Resolves the posted limits for both directions.
     *
     * @param segment the Overture road segment
     * @return {@code {forward, backward}} in km/h, each capped at {@link MaxSpeed#MAXSPEED_150}, or
     *     {@link MaxSpeed#MAXSPEED_MISSING} for a direction with no usable posted limit
     */
    public static double[] parseMaxSpeeds(OvertureRoadSegment segment) {
        if (segment == null || segment.getProperties() == null) {
            return new double[] {MaxSpeed.MAXSPEED_MISSING, MaxSpeed.MAXSPEED_MISSING};
        }
        List<OvertureSpeedLimit> speedLimits = segment.getProperties().getSpeedLimits();
        if (speedLimits == null || speedLimits.isEmpty()) {
            return new double[] {MaxSpeed.MAXSPEED_MISSING, MaxSpeed.MAXSPEED_MISSING};
        }

        OvertureScopes.Directed<OvertureSpeedLimit> byHeading =
                OvertureScopes.byHeading(speedLimits, OvertureMaxSpeedParser::headingOf);
        return new double[] {
            resolve(byHeading.forward(), TravelHeading.FORWARD),
            resolve(byHeading.backward(), TravelHeading.BACKWARD),
        };
    }

    /**
     * Resolves and writes {@code max_speed} for both directions of the edge.
     *
     * @param edge the graph edge to update
     * @param segment the Overture road segment
     * @param context unused; posted limits come entirely from the segment
     */
    @Override
    public void handleSegment(
            EdgeIteratorState edge, OvertureRoadSegment segment, OvertureSegmentContext context) {
        double[] speeds = parseMaxSpeeds(segment);
        if (maxSpeedEnc.isStoreTwoDirections()) {
            edge.set(maxSpeedEnc, speeds[0], speeds[1]);
        } else {
            // A one-direction encoding cannot express an asymmetric limit; take the lower so the
            // stored value never overstates what is legal.
            edge.set(maxSpeedEnc, Math.min(speeds[0], speeds[1]));
        }
    }

    /**
     * Picks the limit that governs one direction.
     *
     * <p>A limit scoped to {@code direction} takes precedence over an unscoped one, mirroring how a
     * direction-specific sign overrides a general one. Within each group the lowest value wins.
     *
     * @param applicable limits already filtered to those applying to {@code direction}
     * @param direction the direction being resolved
     * @return the governing limit in km/h, or {@link MaxSpeed#MAXSPEED_MISSING} if there is none
     */
    private static double resolve(List<OvertureSpeedLimit> applicable, TravelHeading direction) {
        Double directional = null;
        Double unscoped = null;
        for (OvertureSpeedLimit limit : applicable) {
            Double kmh = usableMaxSpeedKmh(limit);
            if (kmh == null) continue;

            if (headingOf(limit) == direction) {
                if (directional == null || kmh < directional) directional = kmh;
            } else if (unscoped == null || kmh < unscoped) {
                unscoped = kmh;
            }
        }
        Double governing = directional != null ? directional : unscoped;
        return governing == null
                ? MaxSpeed.MAXSPEED_MISSING
                : Math.min(governing, MaxSpeed.MAXSPEED_150);
    }

    /**
     * @return the limit in km/h, or {@code null} when it is absent, not a usable positive speed, or
     *     conditional on something {@code max_speed} cannot express
     */
    @Nullable private static Double usableMaxSpeedKmh(@Nullable OvertureSpeedLimit limit) {
        if (limit == null) return null;
        // A limit that only applies at certain times, to certain vehicles, or for certain trip
        // purposes is not the road's permanent limit. Folding one in would apply e.g. a rush-hour or
        // lorry-only restriction to every vehicle at all times.
        if (!OvertureScopes.isDirectionOnly(limit.getWhen())) return null;

        Double kmh = limit.getMaxSpeedKmh();
        // Non-positive values are data errors rather than "no limit"; treat them as absent so they
        // cannot make a road un-routable.
        return (kmh == null || kmh <= 0) ? null : kmh;
    }

    /**
     * @return the heading this limit is scoped to, or {@code null} if it applies to both directions
     */
    @Nullable private static TravelHeading headingOf(OvertureSpeedLimit limit) {
        return limit.getWhen() == null ? null : limit.getWhen().getHeading();
    }
}
