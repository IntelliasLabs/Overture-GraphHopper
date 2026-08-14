package com.graphhopper.reader.overture.road.segment.rule;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import com.graphhopper.reader.overture.road.segment.HasBetweenProperty;

import java.util.Objects;

/**
 * Defines the physical width of the road surface for a specific range of the segment.
 * <p>
 * This rule allows the width of a road segment to vary along its length (e.g., narrowing
 * at a bridge or widening at an intersection approach).
 * </p>
 */
public class OvertureWidthRule implements HasBetweenProperty {

    /**
     * The width of the road surface in meters.
     */
    private final double value;

    /**
     * The linear range along the segment where this width applies.
     * <p>
     * If null, the width applies to the entire segment.
     * </p>
     */
    private final LinearlyReferencedRange between;

    /**
     * Initializes a new width rule for a defined portion of a segment.
     * @param value the physical width of the road in meters.
     * @param between the specific sub-range (0.0 to 1.0) where this width is valid,
     * or {@code null} if it applies to the entire segment.
     */
    public OvertureWidthRule(double value, LinearlyReferencedRange between) {
        this.value = value;
        this.between = between;
    }

    /**
     * Gets the width of the road surface.
     *
     * @return the width in meters.
     */
    public double getValue() {
        return value;
    }

    /**
     * Gets the geometric range where this width applies.
     *
     * @return the {@link LinearlyReferencedRange}, or null if it applies to the whole segment.
     */
    @Override
    public LinearlyReferencedRange getBetween() {
        return between;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OvertureWidthRule that)) return false;
        return Double.compare(getValue(), that.getValue()) == 0
                && Objects.equals(getBetween(), that.getBetween());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue(), getBetween());
    }

    @Override
    public String toString() {
        return "OvertureWidthRule{" + "value=" + value + ", between=" + between + '}';
    }
}
