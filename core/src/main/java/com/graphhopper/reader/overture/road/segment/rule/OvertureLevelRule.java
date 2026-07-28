package com.graphhopper.reader.overture.road.segment.rule;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import com.graphhopper.reader.overture.road.segment.HasBetweenProperty;
import java.util.Objects;

/**
 * Defines a Z-order level that applies only to a specific range of the segment.
 * <p>
 * This rule allows a single road segment to have different vertical stacking orders
 * along its length (e.g., a road that starts at ground level and then becomes an overpass).
 * </p>
 */
public class OvertureLevelRule implements HasBetweenProperty {

    private final int value;
    private final LinearlyReferencedRange between;

    /**
     * Constructs a new level rule for a specific segment range.
     * @param value the Z-order level (0 for ground, positive for above, negative for below).
     * @param between the linear range (0.0 to 1.0) where this level applies,
     * or null for the entire segment.
     */
    public OvertureLevelRule(int value, LinearlyReferencedRange between) {
        this.value = value;
        this.between = between;
    }

    /**
     * Gets the Z-order value.
     * <p>
     * 0 represents the visual ground level. Positive values are above ground,
     * and negative values are below ground.
     * </p>
     *
     * @return the level integer.
     */
    public int getValue() {
        return value;
    }

    /**
     * Gets the linear range along the segment where this level applies.
     *
     * @return the {@link LinearlyReferencedRange} defining the start and end points (between 0.0 and 1.0).
     */
    @Override
    public LinearlyReferencedRange getBetween() {
        return between;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OvertureLevelRule that)) return false;
        return getValue() == that.getValue() && Objects.equals(getBetween(), that.getBetween());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue(), getBetween());
    }

    @Override
    public String toString() {
        return "OvertureLevelRule{" + "value=" + value + ", between=" + between + '}';
    }
}
