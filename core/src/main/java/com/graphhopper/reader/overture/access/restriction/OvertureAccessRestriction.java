package com.graphhopper.reader.overture.access.restriction;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelHeading;
import com.graphhopper.reader.overture.road.segment.HasBetweenProperty;
import java.util.Objects;

/**
 * Represents a single access restriction for a linearly referenced segment returned by the
 * Overture access layer.
 * <p>
 * An instance combines a high-level {@link AccessType} decision with optional
 * {@link PropertyScopeContainer} that further qualify when the restriction applies and a
 * {@link LinearlyReferencedRange} indicating the position along the segment.
 */
public class OvertureAccessRestriction implements HasBetweenProperty {

    // --------------------------------------------------------------
    // Fields
    // --------------------------------------------------------------

    /**
     * The high-level access decision for this restriction as returned by Overture.
     * May be {@code null} if no decision was provided.
     */
    private final AccessType accessType;
    /**
     * Optional additional vehicle-related rules that further qualify when this
     * restriction applies. May be {@code null} if there are no additional rules.
     */
    private final PropertyScopeContainer when;
    /**
     * Optional linearly referenced range along the segment this restriction
     * applies to, or {@code null} if the restriction is not linearly referenced.
     */
    private final LinearlyReferencedRange between;

    // --------------------------------------------------------------
    // Getters
    // --------------------------------------------------------------

    /**
     * Returns the high-level access decision for this restriction.
     *
     * @return the {@link AccessType}, or {@code null} if not specified
     */
    public AccessType getAccessType() {
        return accessType;
    }

    /**
     * Returns additional vehicle attribute rules that qualify this restriction.
     *
     * @return the {@link PropertyScopeContainer}, or {@code null} if there are
     * no additional rules
     */
    public PropertyScopeContainer getWhen() {
        return when;
    }

    /**
     * Returns the linearly referenced range this restriction applies to.
     *
     * @return the {@link LinearlyReferencedRange}, or {@code null} if the
     * restriction is not linearly referenced
     */
    @Override
    public LinearlyReferencedRange getBetween() {
        return between;
    }

    // --------------------------------------------------------------
    // Helper methods
    // --------------------------------------------------------------

    /**
     * Returns whether an access type has been specified.
     *
     * @return {@code true} if {@link #getAccessType()} is non-{@code null},
     * {@code false} otherwise
     */
    public boolean hasAccessType() {
        return accessType != null;
    }

    /**
     * Returns whether additional vehicle attribute rules have been specified.
     *
     * @return {@code true} if {@link #getWhen()} is non-{@code null},
     * {@code false} otherwise
     */
    public boolean hasWhen() {
        return when != null;
    }

    /**
     * Returns whether a linearly referenced range has been specified.
     *
     * @return {@code true} if {@link #getBetween()} is non-{@code null},
     * {@code false} otherwise
     */
    public boolean hasBetween() {
        return between != null;
    }

    /**
     * Returns whether access is allowed in the forward direction according to
     * this restriction.
     * <p>
     * Forward access is considered allowed when the {@link AccessType} is
     * {@link AccessType#ALLOWED} and either no travel heading is specified or
     * the associated {@link TravelHeading} is {@link TravelHeading#FORWARD}.
     * For any other access type (including {@code null}) this method returns
     * {@code false}.
     *
     * @return {@code true} if forward access is allowed, {@code false}
     * otherwise
     */
    public boolean isAllowedForward() {

        boolean haveTravelHeading = hasWhen() && when.hasHeading();
        boolean isForwardHeading = haveTravelHeading && (when.getHeading() == TravelHeading.FORWARD);

        return (!haveTravelHeading && accessType == AccessType.ALLOWED)
                || (isForwardHeading && accessType == AccessType.ALLOWED);
    }

    /**
     * Returns whether access is allowed in the backward direction according to
     * this restriction.
     * <p>
     * Backward access is considered allowed when the {@link AccessType} is
     * {@link AccessType#ALLOWED} and either no travel heading is specified or
     * the associated {@link TravelHeading} is {@link TravelHeading#BACKWARD}.
     * For any other access type (including {@code null}) this method returns
     * {@code false}.
     *
     * @return {@code true} if backward access is allowed, {@code false}
     * otherwise
     */
    public boolean isAllowedBackward() {

        boolean haveTravelHeading = hasWhen() && when.hasHeading();
        boolean isBackwardHeading = haveTravelHeading && (when.getHeading() == TravelHeading.BACKWARD);
        return (!haveTravelHeading && accessType == AccessType.ALLOWED)
                || (isBackwardHeading && accessType == AccessType.ALLOWED);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OvertureAccessRestriction that = (OvertureAccessRestriction) o;
        return accessType == that.accessType
                && Objects.equals(when, that.when)
                && Objects.equals(between, that.between);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessType, when, between);
    }

    /**
     * Returns a string representation containing the access type, optional
     * access condition and linear range used mainly for debugging and tests.
     */
    @Override
    public String toString() {
        return "OvertureAccessRestriction{" + "accessType="
                + accessType + ", accessCondition="
                + when + ", between="
                + between + '}';
    }
    // --------------------------------------------------------------
    // Constructors
    // --------------------------------------------------------------

    /**
     * Creates a new access restriction instance.
     * <p>
     * All parameters are optional and may be {@code null}. Callers can use the
     * corresponding {@code has*} helper methods to check which values are
     * present.
     *
     * @param accessType the high-level {@link AccessType} decision for this
     *                   restriction, or {@code null} if the decision is not
     *                   available
     * @param when       optional {@link PropertyScopeContainer} that further
     *                   qualifies when this restriction applies, or {@code null}
     *                   if there are no additional rules
     * @param between    optional {@link LinearlyReferencedRange} this
     *                   restriction applies to, or {@code null} if the
     *                   restriction is not linearly referenced
     */
    public OvertureAccessRestriction(
            AccessType accessType, PropertyScopeContainer when, LinearlyReferencedRange between) {
        this.accessType = accessType;
        this.when = when;
        this.between = between;
    }

    /**
     * Creates a new restriction that only specifies an {@link AccessType} decision.
     * <p>
     * The {@code when} and {@code between} fields will be {@code null}. Callers can use
     * {@link #hasWhen()} and {@link #hasBetween()} to detect this.
     *
     * @param accessType the high-level access decision for this restriction, may be {@code null}
     * @return a new {@link OvertureAccessRestriction} with only {@code accessType} set
     */
    public static OvertureAccessRestriction ofAccessType(AccessType accessType) {
        return new OvertureAccessRestriction(accessType, null, null);
    }

    /**
     * Creates a new restriction that only specifies additional conditions via
     * {@link PropertyScopeContainer}.
     * <p>
     * The {@code accessType} and {@code between} fields will be {@code null}. Callers can use
     * {@link #hasAccessType()} and {@link #hasBetween()} to detect this.
     *
     * @param when the additional conditions for this restriction, may be {@code null}
     * @return a new {@link OvertureAccessRestriction} with only {@code when} set
     */
    public static OvertureAccessRestriction ofWhen(PropertyScopeContainer when) {
        return new OvertureAccessRestriction(null, when, null);
    }

    /**
     * Creates a new restriction that only specifies a linearly referenced range.
     * <p>
     * The {@code accessType} and {@code when} fields will be {@code null}. Callers can use
     * {@link #hasAccessType()} and {@link #hasWhen()} to detect this.
     *
     * @param between the linearly referenced range this restriction applies to, may be {@code null}
     * @return a new {@link OvertureAccessRestriction} with only {@code between} set
     */
    public static OvertureAccessRestriction ofBetween(LinearlyReferencedRange between) {
        return new OvertureAccessRestriction(null, null, between);
    }
}
