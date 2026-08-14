package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelHeading;
import com.graphhopper.reader.overture.common.speed.OvertureSpeedLimit;
import com.graphhopper.reader.overture.road.segment.OvertureRoadClass;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.reader.overture.road.surface.OvertureRoadSurface;
import com.graphhopper.reader.overture.road.surface.RoadSurfaceType;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.Smoothness;
import com.graphhopper.routing.ev.TrackType;
import com.graphhopper.util.EdgeIteratorState;
import java.util.List;
import java.util.Map;

/**
 * Parses average bicycle speed from Overture road segment data.
 *
 * <p>This parser calculates bike speed based on:
 * <ul>
 *   <li><b>Road class:</b> Base speed varies by road type (e.g., cycleway=18 km/h, path=6 km/h)</li>
 *   <li><b>Surface type:</b> Speed is capped based on surface quality</li>
 *   <li><b>Track type:</b> Speed is capped based on track grade (grade1-5)</li>
 *   <li><b>Smoothness:</b> Speed factor applied based on road smoothness</li>
 *   <li><b>Direction:</b> Supports different speeds for forward/backward directions</li>
 *   <li><b>Service roads:</b> Reduced to pushing speed unless bike access is designated/allowed</li>
 * </ul>
 *
 * <p>Speed values are aligned with BikeCommonAverageSpeedParser from the OSM parsing logic.
 *
 * @see com.graphhopper.routing.util.parsers.BikeCommonAverageSpeedParser
 */
public final class OvertureBikeAverageSpeedParser {

    /** Speed for pushing sections (dismount, bad surfaces, etc.) */
    private static final double PUSHING_SECTION_SPEED = 4.0;
    /** Minimum allowed speed */
    private static final double MIN_SPEED = 2.0;
    /** Default speed when road class is unknown */
    private static final double DEFAULT_SPEED = 12.0;
    /** Speed for ferry segments */
    private static final double FERRY_SPEED = 5.0;

    // Road class speeds (aligned with BikeCommonAverageSpeedParser highway speeds)
    private static final Map<OvertureRoadClass, Double> ROAD_CLASS_SPEEDS = Map.ofEntries(
            Map.entry(OvertureRoadClass.CYCLEWAY, 18.0),
            Map.entry(OvertureRoadClass.PATH, 6.0),
            Map.entry(OvertureRoadClass.FOOTWAY, 6.0),
            Map.entry(OvertureRoadClass.PEDESTRIAN, 6.0),
            Map.entry(OvertureRoadClass.BRIDLEWAY, 6.0),
            Map.entry(OvertureRoadClass.STEPS, MIN_SPEED),
            Map.entry(OvertureRoadClass.LIVING_STREET, 6.0),
            Map.entry(OvertureRoadClass.TRACK, 12.0),
            Map.entry(OvertureRoadClass.SERVICE, 12.0),
            Map.entry(OvertureRoadClass.RESIDENTIAL, 18.0),
            Map.entry(OvertureRoadClass.UNCLASSIFIED, 16.0),
            Map.entry(OvertureRoadClass.TERTIARY, 18.0),
            Map.entry(OvertureRoadClass.SECONDARY, 18.0),
            Map.entry(OvertureRoadClass.PRIMARY, 18.0),
            Map.entry(OvertureRoadClass.TRUNK, 18.0),
            Map.entry(OvertureRoadClass.MOTORWAY, 18.0),
            Map.entry(OvertureRoadClass.UNKNOWN, DEFAULT_SPEED));

    // Surface speeds (aligned with BikeCommonAverageSpeedParser surface speeds)
    private static final Map<RoadSurfaceType, Double> SURFACE_SPEEDS = Map.ofEntries(
            Map.entry(RoadSurfaceType.ASPHALT, 18.0),
            Map.entry(RoadSurfaceType.CONCRETE, 18.0),
            Map.entry(RoadSurfaceType.PAVED, 18.0),
            Map.entry(RoadSurfaceType.PAVING_STONES, 16.0),
            Map.entry(RoadSurfaceType.GRAVEL, 12.0),
            Map.entry(RoadSurfaceType.UNPAVED, 12.0),
            Map.entry(RoadSurfaceType.METAL, 10.0),
            Map.entry(RoadSurfaceType.DIRT, 10.0));

    // Track type speeds (aligned with BikeCommonAverageSpeedParser trackTypeSpeeds)
    private static final Map<TrackType, Double> TRACK_TYPE_SPEEDS = Map.ofEntries(
            Map.entry(TrackType.GRADE1, 18.0), // paved
            Map.entry(TrackType.GRADE2, 12.0), // unpaved but compacted
            Map.entry(TrackType.GRADE3, 8.0), // unpaved, rough
            Map.entry(TrackType.GRADE4, 6.0), // very rough
            Map.entry(TrackType.GRADE5, PUSHING_SECTION_SPEED) // like sand
            );

    // Smoothness speed factors (only values returned by OvertureSmoothnessParser)
    private static final Map<Smoothness, Double> SMOOTHNESS_FACTORS = Map.ofEntries(
            Map.entry(Smoothness.MISSING, 1.0),
            Map.entry(Smoothness.EXCELLENT, 1.1), // ASPHALT, CONCRETE
            Map.entry(Smoothness.GOOD, 1.0), // METAL, PAVED
            Map.entry(Smoothness.INTERMEDIATE, 0.9), // PAVING_STONES
            Map.entry(Smoothness.BAD, 0.7), // GRAVEL
            Map.entry(Smoothness.VERY_BAD, 0.4), // UNPAVED
            Map.entry(Smoothness.HORRIBLE, 0.3) // DIRT
            );

    private OvertureBikeAverageSpeedParser() {}

    /**
     * Holds bidirectional bike speed values.
     *
     * @param forward  speed in the forward direction (km/h)
     * @param backward speed in the backward direction (km/h)
     */
    public record BikeSpeed(double forward, double backward) {}

    /**
     * Calculates and sets the average bicycle speed for the edge.
     * Handles bidirectional speeds.
     *
     * @param edge     the GraphHopper edge to update
     * @param segment  the Overture road segment metadata
     * @param speedEnc the encoded value for bike speed
     */
    public static void parseSpeed(
            EdgeIteratorState edge, OvertureRoadSegment segment, DecimalEncodedValue speedEnc) {
        BikeSpeed speed = calculateBikeSpeed(segment);

        // EncodedValue bike_average_speed supports only one direction
        edge.set(speedEnc, speed.forward());
    }

    /**
     * Calculates the average bicycle speed for both directions.
     * This method calculates the base speed once and applies direction-specific limits.
     *
     * @param segment the Overture road segment
     * @return BikeSpeed containing forward and backward speeds in km/h
     */
    public static BikeSpeed calculateBikeSpeed(OvertureRoadSegment segment) {
        if (segment == null || segment.getProperties() == null) {
            return new BikeSpeed(DEFAULT_SPEED, DEFAULT_SPEED);
        }

        double baseSpeed = calculateBaseSpeed(segment);

        double fwd = Math.max(MIN_SPEED, applyMaxSpeed(segment, baseSpeed, TravelHeading.FORWARD));
        double bwd = Math.max(MIN_SPEED, applyMaxSpeed(segment, baseSpeed, TravelHeading.BACKWARD));

        return new BikeSpeed(fwd, bwd);
    }

    /**
     * Calculates the base bicycle speed for the segment (before direction-specific limits).
     * This includes road class, service road handling, surface, track type, and smoothness.
     *
     * @param segment the Overture road segment
     * @return the base speed in km/h (before max speed limit)
     */
    private static double calculateBaseSpeed(OvertureRoadSegment segment) {
        // Ferry segments use fixed ferry speed
        if (OvertureFerryParser.isFerry(segment)) {
            return FERRY_SPEED;
        }

        var properties = segment.getProperties();

        // Get base speed from road class
        OvertureRoadClass roadClass = properties.getRoadClass();
        double speed = getBaseSpeed(roadClass);

        // Service roads get pushing speed unless bike access is designated/allowed
        if (roadClass == OvertureRoadClass.SERVICE && !isBikeDesignatedOrAllowed(segment)) {
            speed = PUSHING_SECTION_SPEED;
        }

        // Apply surface speed limit
        speed = applySurfaceSpeed(segment, speed);

        // Apply track type speed limit
        speed = applyTrackTypeSpeed(segment, speed);

        // Apply smoothness factor
        Smoothness smoothness = OvertureSmoothnessParser.parse(segment);
        speed = applySmoothnessSpeed(speed, smoothness);

        return speed;
    }

    /**
     * Gets the base speed for the given road class.
     *
     * @param roadClass the Overture road class
     * @return the base speed in km/h
     */
    private static double getBaseSpeed(OvertureRoadClass roadClass) {
        if (roadClass == null) {
            return DEFAULT_SPEED;
        }
        return ROAD_CLASS_SPEEDS.getOrDefault(roadClass, DEFAULT_SPEED);
    }

    /**
     * Applies surface-based speed limits.
     *
     * @param segment the Overture road segment
     * @param speed   the current speed
     * @return the adjusted speed based on surface
     */
    private static double applySurfaceSpeed(OvertureRoadSegment segment, double speed) {
        OvertureRoadSurface surface = segment.getRoadSurface();
        if (surface == null || surface.getSurfaceType() == null) {
            return speed;
        }

        RoadSurfaceType surfaceType = surface.getSurfaceType();
        Double surfaceSpeed = SURFACE_SPEEDS.get(surfaceType);

        if (surfaceSpeed != null) {
            return Math.min(speed, surfaceSpeed);
        }

        // Unknown surface type - use pushing section speed
        return Math.min(speed, PUSHING_SECTION_SPEED);
    }

    /**
     * Applies track type speed limits.
     * Uses OvertureTrackTypeParser to determine track grade.
     *
     * @param segment the Overture road segment
     * @param speed   the current speed
     * @return the adjusted speed based on track type
     */
    private static double applyTrackTypeSpeed(OvertureRoadSegment segment, double speed) {
        TrackType trackType = OvertureTrackTypeParser.parse(segment);
        if (trackType == null || trackType == TrackType.MISSING) {
            return speed;
        }

        Double trackTypeSpeed = TRACK_TYPE_SPEEDS.get(trackType);
        if (trackTypeSpeed != null) {
            return Math.min(speed, trackTypeSpeed);
        }
        return speed;
    }

    /**
     * Applies smoothness factor to the speed.
     *
     * @param speed      the current speed
     * @param smoothness the road smoothness
     * @return the adjusted speed
     */
    private static double applySmoothnessSpeed(double speed, Smoothness smoothness) {
        Double factor = SMOOTHNESS_FACTORS.get(smoothness);
        if (factor == null) {
            factor = 1.0;
        }
        return Math.max(MIN_SPEED, speed * factor);
    }

    /**
     * Applies direction-specific max speed limit from segment data.
     * Supports Overture's directional speed limits via the 'when.heading' property.
     *
     * @param segment   the Overture road segment
     * @param speed     the current speed
     * @param direction the travel direction (FORWARD or BACKWARD)
     * @return the adjusted speed respecting max speed limits
     */
    private static double applyMaxSpeed(
            OvertureRoadSegment segment, double speed, TravelHeading direction) {
        if (segment.getProperties() == null) {
            return speed;
        }

        List<OvertureSpeedLimit> speedLimits = segment.getProperties().getSpeedLimits();
        if (speedLimits == null || speedLimits.isEmpty()) {
            return speed;
        }
        Double directionalMaxSpeed = null;
        Double globalMaxSpeed = null;

        for (OvertureSpeedLimit limit : speedLimits) {
            if (limit == null) continue;

            Double maxSpeedKmh = limit.getMaxSpeedKmh();
            if (maxSpeedKmh == null || maxSpeedKmh <= 0) continue;

            var when = limit.getWhen();
            if (when == null || when.getHeading() == null) {
                // No heading constraint - applies to both directions
                if (globalMaxSpeed == null || maxSpeedKmh < globalMaxSpeed) {
                    globalMaxSpeed = maxSpeedKmh;
                }
            } else if (when.getHeading() == direction) {
                // Direction-specific speed limit
                if (directionalMaxSpeed == null || maxSpeedKmh < directionalMaxSpeed) {
                    directionalMaxSpeed = maxSpeedKmh;
                }
            }
        }

        // Direction-specific limit takes precedence over global limit
        Double effectiveMaxSpeed = directionalMaxSpeed != null ? directionalMaxSpeed : globalMaxSpeed;
        if (effectiveMaxSpeed != null) {
            return Math.min(speed, effectiveMaxSpeed);
        }

        return speed;
    }

    /**
     * Checks if bicycle access is explicitly allowed or designated for the segment.
     * This is used to determine if service roads should use normal speed instead of pushing speed.
     *
     * @param segment the Overture road segment
     * @return true if bicycle access is ALLOWED or DESIGNATED, false otherwise
     */
    private static boolean isBikeDesignatedOrAllowed(OvertureRoadSegment segment) {
        if (segment.getProperties() == null) {
            return false;
        }
        return OvertureAccessParser.isAccessDesignatedOrAllowed(
                segment.getProperties().getAccessRestrictions(), "bicycle");
    }
}
