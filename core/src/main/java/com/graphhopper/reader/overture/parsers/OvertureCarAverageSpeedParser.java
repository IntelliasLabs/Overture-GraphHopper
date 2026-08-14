package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.road.segment.OvertureRoadClass;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.reader.overture.road.surface.OvertureRoadSurface;
import com.graphhopper.reader.overture.road.surface.RoadSurfaceType;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.util.EdgeIteratorState;
import java.util.EnumSet;
import java.util.Set;

/**
 * Logic for estimating average car speeds from Overture road features.
 * <p>
 * This parser calculates the traversal speed for a road segment based on a hierarchical
 * priority system: explicit speed limits, link-specific defaults, and functional road
 * classification. It also accounts for physical constraints, such as surface quality,
 * to ensure realistic routing costs.
 * </p>
 */
public final class OvertureCarAverageSpeedParser {
    private OvertureCarAverageSpeedParser() {}

    // This value determines the maximal possible on roads with bad surfaces (30 km/h)
    private static final int BAD_SURFACE_SPEED = 30;
    private static final int DEFAULT_SPEED = 20;

    private static final Set<RoadSurfaceType> BAD_SURFACES = EnumSet.of(
            RoadSurfaceType.UNPAVED,
            RoadSurfaceType.GRAVEL,
            RoadSurfaceType.DIRT,
            RoadSurfaceType.PAVING_STONES);

    /**
     * Calculates and sets the average car speed for the edge.
     * @param edge the GraphHopper edge to update
     * @param segment the Overture road segment metadata
     * @param speedEnc the encoded value for car speed
     */
    public static void parseSpeed(
            EdgeIteratorState edge, OvertureRoadSegment segment, DecimalEncodedValue speedEnc) {
        double baseSpeed = getBaseSpeed(segment);
        baseSpeed = applySurfaceSpeed(segment, baseSpeed);
        edge.set(speedEnc, baseSpeed, baseSpeed);
    }

    /**
     * Determines base speed using the hierarchy: MaxSpeed > Link Defaults > Class Defaults.
     * @param segment the Overture road segment
     * @return the base speed limit in km/h
     */
    private static double getBaseSpeed(OvertureRoadSegment segment) {
        Double explicitMaxSpeed = segment.getMaxSpeed();
        if (explicitMaxSpeed != null) {
            return explicitMaxSpeed * 0.9;
        }

        OvertureRoadClass roadClass = segment.getProperties().getRoadClass();
        if (roadClass == null) return DEFAULT_SPEED;

        if (OvertureRoadClassLinkParser.isLink(roadClass, segment.getProperties().getSubclass())) {
            return getLinkSpeed(roadClass);
        }

        return getDefaultRoadClassMaxSpeed(roadClass);
    }

    /**
     * Caps speed at {@link #BAD_SURFACE_SPEED} if the segment has a bad surface type.
     * @param segment the Overture road segment
     * @param speed the current speed in km/h
     * @return the adjusted speed in km/h
     */
    private static double applySurfaceSpeed(OvertureRoadSegment segment, double speed) {
        OvertureRoadSurface surface = segment.getRoadSurface();

        if (surface != null && speed > BAD_SURFACE_SPEED) {
            if (BAD_SURFACES.contains(surface.getSurfaceType())) {
                return BAD_SURFACE_SPEED;
            }
        }
        return speed;
    }

    /**
     * Provides a default speed limit based on the road classification.
     * <a href="http://wiki.openstreetmap.org/wiki/OSM_tags_for_routing/Maxspeed">MaxSpeed</a>
     *
     * @param roadClass the classification of the road (e.g., motorway, trunk).
     * @return the default speed in km/h for the given class.
     */
    private static double getDefaultRoadClassMaxSpeed(OvertureRoadClass roadClass) {
        return switch (roadClass) {
                // autobahn
            case MOTORWAY -> 100;
                // bundesstraße
            case TRUNK -> 70;
                // linking bigger town
            case PRIMARY -> 65;
                // linking towns + villages
            case SECONDARY -> 60;
                // streets without middle line separation
            case TERTIARY -> 50;
            case UNCLASSIFIED, RESIDENTIAL -> 30;
            case LIVING_STREET, PEDESTRIAN -> 6;
            case SERVICE -> 20;
            case TRACK -> 15;
                // unknown road
            default -> DEFAULT_SPEED;
        };
    }

    /**
     * Provides a default speed limit based on the road classification for OvertureSubclass.LINK.
     * @param roadClass the classification of the road (e.g., motorway, trunk).
     * @return the default speed in km/h for the given class.
     */
    private static double getLinkSpeed(OvertureRoadClass roadClass) {
        return switch (roadClass) {
            case MOTORWAY -> 70;
            case TRUNK -> 65;
            case PRIMARY -> 60;
            case SECONDARY -> 50;
            case TERTIARY -> 40;
            default -> DEFAULT_SPEED;
        };
    }
}
