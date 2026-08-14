package com.graphhopper.reader.overture.road.segment.rule;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import com.graphhopper.reader.overture.road.segment.HasBetweenProperty;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSubclass;
import java.util.Objects;

/**
 * Defines a road subclass assignment that applies to a specific range of the segment.
 * <p>
 * This allows a single segment to change its subclass along its length (e.g., a road that
 * starts as a generic residential road but becomes a bridge or link for a specific section).
 * </p>
 */
public class OvertureSubclassRule implements HasBetweenProperty {

    /**
     * The subclass value to apply (e.g., LINK, SIDEWALK).
     */
    private final OvertureRoadSubclass value;

    /**
     * The linear range along the segment where this subclass applies.
     * <p>
     * If null, the subclass applies to the entire segment (though typically rules are used for partial ranges).
     * </p>
     */
    private final LinearlyReferencedRange between;

    /**
     * Constructs a new subclass rule for a specific segment range.
     * @param value the {@link OvertureRoadSubclass} to be assigned.
     * @param between the linear range (0.0 to 1.0) where this subclass applies,
     * or null for the entire segment.
     */
    public OvertureSubclassRule(OvertureRoadSubclass value, LinearlyReferencedRange between) {
        this.value = value;
        this.between = between;
    }

    /**
     * Gets the subclass assigned by this rule.
     *
     * @return the {@link OvertureRoadSubclass}.
     */
    public OvertureRoadSubclass getValue() {
        return value;
    }

    /**
     * Gets the geometric range where this rule applies.
     *
     * @return the {@link LinearlyReferencedRange}.
     */
    @Override
    public LinearlyReferencedRange getBetween() {
        return between;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OvertureSubclassRule that)) return false;
        return getValue() == that.getValue() && Objects.equals(getBetween(), that.getBetween());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue(), getBetween());
    }

    @Override
    public String toString() {
        return "OvertureSubclassRule{" + "value=" + value + ", between=" + between + '}';
    }
}
