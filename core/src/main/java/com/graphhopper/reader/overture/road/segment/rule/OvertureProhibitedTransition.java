package com.graphhopper.reader.overture.road.segment.rule;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import com.graphhopper.reader.overture.access.restriction.PropertyScopeContainer;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelHeading;
import com.graphhopper.reader.overture.road.segment.HasBetweenProperty;
import java.util.List;
import java.util.Objects;

/**
 * Rules preventing transitions from this segment to another segment.
 * <p>
 * This is typically used to model turn restrictions (e.g., "No Left Turn").
 * </p>
 */
public class OvertureProhibitedTransition implements HasBetweenProperty {
    private final List<OvertureTransitionSequenceItem> sequence;
    private final TravelHeading finalHeading;
    private final PropertyScopeContainer when;
    private final LinearlyReferencedRange between;

    /**
     * Constructs a new prohibited transition rule.
     * @param sequence the ordered list of segments/connectors forming the restricted path.
     * @param finalHeading the direction prohibited on the last segment of the sequence.
     * @param when conditional scopes (vehicle types, time, etc.) for the restriction.
     * @param between the specific sub-range of the segment where the restriction starts.
     */
    public OvertureProhibitedTransition(
            List<OvertureTransitionSequenceItem> sequence,
            TravelHeading finalHeading,
            PropertyScopeContainer when,
            LinearlyReferencedRange between) {
        this.sequence = sequence;
        this.finalHeading = finalHeading;
        this.when = when;
        this.between = between;
    }

    /**
     * Gets the ordered sequence of connector/segment pairs that it is prohibited to follow from this segment.
     * <p>
     * This defines the path leading up to the restricted turn.
     * </p>
     *
     * @return a list of {@link OvertureTransitionSequenceItem}.
     */
    public List<OvertureTransitionSequenceItem> getSequence() {
        return sequence;
    }

    /**
     * Gets the direction of travel that is prohibited on the destination segment of the sequence.
     *
     * @return the {@link TravelHeading} (e.g., FORWARD or BACKWARD).
     */
    public TravelHeading getFinalHeading() {
        return finalHeading;
    }

    /**
     * Gets the conditions (temporal, vehicle type, etc.) under which this transition is prohibited.
     *
     * @return the {@link PropertyScopeContainer}, or null if unconditional.
     */
    public PropertyScopeContainer getWhen() {
        return when;
    }

    /**
     * Gets the geometric range along the segment where this rule applies.
     *
     * @return the {@link LinearlyReferencedRange}, or null if it applies to the whole segment.
     */
    @Override
    public LinearlyReferencedRange getBetween() {
        return between;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OvertureProhibitedTransition that)) return false;
        return Objects.equals(getSequence(), that.getSequence())
                && getFinalHeading() == that.getFinalHeading()
                && Objects.equals(getWhen(), that.getWhen())
                && Objects.equals(getBetween(), that.getBetween());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSequence(), getFinalHeading(), getWhen(), getBetween());
    }

    @Override
    public String toString() {
        return "OvertureProhibitedTransition{" + "sequence="
                + sequence + ", finalHeading="
                + finalHeading + ", when="
                + when + ", between="
                + between + '}';
    }
}
