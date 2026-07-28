package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.road.segment.OvertureRoadClass;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.util.EdgeIteratorState;

/**
 * Logic for estimating average walking speeds from Overture road features.
 * <p>
 * This parser assigns pedestrian speeds based on the road classification.
 * While car speeds depend on surface and hierarchy, foot travel is primarily
 * influenced by terrain features like steps.
 * </p>
 */
public final class OvertureFootAverageSpeedParser implements OvertureTagParser {

    /** The standard average walking speed on most road types (km/h). */
    private static final double MEAN_SPEED = 5.0;
    /** Reduced speed for segments identified as steps or stairs (km/h). */
    private static final double STEPS_SPEED = 3.0;

    private final DecimalEncodedValue speedEnc;

    /**
     * @param speedEnc the encoded value for foot speed
     */
    public OvertureFootAverageSpeedParser(DecimalEncodedValue speedEnc) {
        this.speedEnc = speedEnc;
    }

    /**
     * Sets the average walking speed for pedestrians.
     *
     * @param edge the GraphHopper edge to update
     * @param segment the Overture road segment metadata
     * @param context unused; walking speed is derived entirely from the road class
     */
    @Override
    public void handleSegment(
            EdgeIteratorState edge, OvertureRoadSegment segment, OvertureSegmentContext context) {
        var properties = segment.getProperties();
        double speed =
                (properties.getRoadClass() == OvertureRoadClass.STEPS) ? STEPS_SPEED : MEAN_SPEED;

        // According to Overture Schema, sac_scale (representing hiking difficulty) is missing.
        // Once it added, speed should be decreased to SLOW_SPEED = 2km/h for non-hiking path.
        // to align with OSM FootAverageParser logic

        edge.set(speedEnc, speed);
    }
}
