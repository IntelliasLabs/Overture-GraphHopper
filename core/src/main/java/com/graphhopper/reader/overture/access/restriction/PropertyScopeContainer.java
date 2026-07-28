package com.graphhopper.reader.overture.access.restriction;

import static java.util.Collections.emptyList;

import com.graphhopper.reader.overture.access.restriction.scope.containers.*;
import java.util.List;
import java.util.Objects;

/**
 * Container for optional properties that further qualify an {@link OvertureAccessRestriction}.
 * <p>
 * Each property represents an additional condition under which an access restriction applies,
 * such as a temporal constraint ({@code during}), a heading constraint ({@code heading}) or
 * filters based on travel reason, recognition status, travel mode or vehicle attributes.
 * <p>
 * All fields are optional and may be {@code null} or empty. Callers can use the corresponding
 * {@code has*} methods to check whether a given dimension is present and should be considered
 * when evaluating the restriction.
 * <p>
 * Instances of this class are intended to be treated as effectively immutable. While the fields
 * themselves are {@code final}, the underlying collection instances are not defensively copied
 * and should not be modified after the container has been created.
 */
public class PropertyScopeContainer {

    // --------------------------------------------------------------------------
    // Fields
    // --------------------------------------------------------------------------

    /**
     * Temporal condition expression that limits when the associated access restriction applies,
     * or {@code null} if there is no temporal constraint.
     */
    private final String during;

    /**
     * Heading constraint that must be satisfied for the access restriction to apply,
     * or {@code null} if there is no heading constraint.
     */
    private final TravelHeading heading;

    /**
     * Travel reasons for which the access restriction is intended to apply.
     * May be empty if the restriction is not filtered by travel reason.
     */
    private final List<TravelReason> using;

    /**
     * Recognition statuses for which the access restriction is intended to apply.
     * May be empty if the restriction is not filtered by recognition status.
     */
    private final List<RecognizedStatus> recognized;

    /**
     * Travel modes for which the access restriction is intended to apply.
     * May be empty if the restriction is not filtered by travel mode.
     */
    private final List<TravelMode> mode;

    /**
     * Vehicle attributes that must match for the access restriction to apply.
     * May be empty if the restriction is not filtered by vehicle attributes.
     */
    private final List<VehicleAttributes> vehicle;

    // --------------------------------------------------------------------------
    // Get-methods
    // --------------------------------------------------------------------------

    /**
     * Returns the temporal condition under which the associated access restriction applies.
     * <p>
     * The exact format and interpretation of this string is defined by the Overture data and
     * by the components consuming it. A {@code null} value indicates that the restriction is
     * not limited by a specific temporal expression.
     *
     * @return the temporal condition string, or {@code null} if there is no temporal constraint
     */
    public String getDuring() {
        return during;
    }

    /**
     * Returns the travel heading constraint for this container.
     * <p>
     * A non-{@code null} value indicates that the access restriction only applies when traveling
     * with a certain heading (for example, in a certain direction of travel).
     *
     * @return the travel heading, or {@code null} if there is no heading constraint
     */
    public TravelHeading getHeading() {
        return heading;
    }

    /**
     * Returns the list of travel reasons that qualify this access restriction.
     * <p>
     * When this list is non-empty, the restriction is intended to apply
     * only for the given reasons (for example, delivery, residential access, emergency, etc.).
     * The returned list is the internal list used by this container and should not be modified
     * by callers.
     *
     * @return the list of travel reasons, or empty if there is no such filter
     */
    public List<TravelReason> getUsing() {
        return using;
    }

    /**
     * Returns the list of recognition statuses that qualify this access restriction.
     * <p>
     * When this list is non-empty, the restriction is intended to apply
     * only for entities with one of the given recognition statuses (for example, residents,
     * permit holders, etc.). The returned list is the internal list used by this container and
     * should not be modified by callers.
     *
     * @return the list of recognition statuses, or empty if there is no such filter
     */
    public List<RecognizedStatus> getRecognized() {
        return recognized;
    }

    /**
     * Returns the list of travel modes that qualify this access restriction.
     * <p>
     * When this list is non-empty, the restriction is intended to apply
     * only for the given modes of travel (for example, car, bicycle, foot, etc.). The returned
     * list is the internal list used by this container and should not be modified by callers.
     *
     * @return the list of travel modes, or empty if there is no such filter
     */
    public List<TravelMode> getMode() {
        return mode;
    }

    /**
     * Returns the list of vehicle attributes that qualify this access restriction.
     * <p>
     * When this list is non-empty, the restriction is intended to apply
     * only for vehicles matching the specified attributes (for example, weight, height,
     * presence of trailer, etc.). The returned list is the internal list used by this container
     * and should not be modified by callers.
     *
     * @return the list of vehicle attributes, or empty if there is no such filter
     */
    public List<VehicleAttributes> getVehicle() {
        return vehicle;
    }

    // --------------------------------------------------------------------------
    // Has-methods
    // --------------------------------------------------------------------------

    /**
     * Returns {@code true} if a temporal condition is present.
     * <p>
     * This method only checks whether {@link #getDuring()} is non-{@code null} and does not
     * validate the format or semantics of the underlying string.
     *
     * @return {@code true} if {@code during} is non-{@code null}
     */
    public boolean hasDuring() {
        return during != null;
    }

    /**
     * Returns {@code true} if a heading constraint is present.
     *
     * @return {@code true} if {@code heading} is non-{@code null}
     */
    public boolean hasHeading() {
        return heading != null;
    }

    /**
     * Returns {@code true} if there is at least one travel reason specified.
     *
     * @return {@code true} if {@code using} is not empty
     */
    public boolean hasUsing() {
        // the list fields are nullable: the primary constructor stores what it is given while the
        // shorter overloads substitute emptyList(), so null and empty both mean "no constraint"
        return using != null && !using.isEmpty();
    }

    /**
     * Returns {@code true} if there is at least one recognition status specified.
     *
     * @return {@code true} if {@code recognized} is not empty
     */
    public boolean hasRecognized() {
        return recognized != null && !recognized.isEmpty();
    }

    /**
     * Returns {@code true} if there is at least one travel mode specified.
     *
     * @return {@code true} if {@code mode} is not empty
     */
    public boolean hasMode() {
        return mode != null && !mode.isEmpty();
    }

    /**
     * Returns {@code true} if there is at least one vehicle attribute specified.
     *
     * @return {@code true} if {@code vehicle} is not empty
     */
    public boolean hasVehicle() {
        return vehicle != null && !vehicle.isEmpty();
    }

    // --------------------------------------------------------------------------
    // Constructors and factory methods
    // --------------------------------------------------------------------------

    /**
     * Creates a new container with the given combination of scope properties.
     * <p>
     * All arguments are optional and may be {@code null} (or, for collections, empty). Callers
     * are responsible for ensuring that the chosen combination makes sense for their use case
     * and typically should avoid creating containers without any properties at all.
     *
     * @param during     the temporal condition, or {@code null} if not limited by time
     * @param heading    the heading constraint, or {@code null} if there is none
     * @param using      the list of travel reasons, or empty if unrestricted
     * @param recognized the list of recognition statuses, or empty if unrestricted
     * @param mode       the list of travel modes, or empty if unrestricted
     * @param vehicle    the list of vehicle attributes, or empty if unrestricted
     */
    public PropertyScopeContainer(
            String during,
            TravelHeading heading,
            List<TravelReason> using,
            List<RecognizedStatus> recognized,
            List<TravelMode> mode,
            List<VehicleAttributes> vehicle) {
        this.during = during;
        this.heading = heading;
        this.using = using;
        this.recognized = recognized;
        this.mode = mode;
        this.vehicle = vehicle;
    }

    /**
     * Creates a new container with the given temporal condition, heading, travel reasons,
     * recognition statuses and travel modes. Vehicle attributes remain unset.
     *
     * @param during     the temporal condition, or {@code null}
     * @param heading    the heading constraint, or {@code null}
     * @param using      the list of travel reasons, or empty
     * @param recognized the list of recognition statuses, or empty
     * @param mode       the list of travel modes, or empty
     */
    public PropertyScopeContainer(
            String during,
            TravelHeading heading,
            List<TravelReason> using,
            List<RecognizedStatus> recognized,
            List<TravelMode> mode) {
        this(during, heading, using, recognized, mode, emptyList());
    }

    /**
     * Creates a new container with the given temporal condition, heading, travel reasons
     * and recognition statuses. Travel modes and vehicle attributes remain unset.
     *
     * @param during     the temporal condition, or {@code null}
     * @param heading    the heading constraint, or {@code null}
     * @param using      the list of travel reasons, or empty
     * @param recognized the list of recognition statuses, or empty
     */
    public PropertyScopeContainer(
            String during,
            TravelHeading heading,
            List<TravelReason> using,
            List<RecognizedStatus> recognized) {
        this(during, heading, using, recognized, emptyList(), emptyList());
    }

    /**
     * Creates a new container with the given temporal condition, heading and travel reasons.
     * Recognition statuses, travel modes and vehicle attributes remain unset.
     *
     * @param during  the temporal condition, or {@code null}
     * @param heading the heading constraint, or {@code null}
     * @param using   the list of travel reasons, or empty
     */
    public PropertyScopeContainer(String during, TravelHeading heading, List<TravelReason> using) {
        this(during, heading, using, emptyList(), emptyList(), emptyList());
    }

    /**
     * Creates a new container with the given temporal condition and heading.
     * All list-based properties remain unset.
     *
     * @param during  the temporal condition, or {@code null}
     * @param heading the heading constraint, or {@code null}
     */
    public PropertyScopeContainer(String during, TravelHeading heading) {
        this(during, heading, emptyList(), emptyList(), emptyList(), emptyList());
    }

    /**
     * Creates a new container that only carries a temporal condition.
     *
     * @param during the temporal condition, may be {@code null}
     * @return a new {@link PropertyScopeContainer} with only {@code during} set
     */
    public static PropertyScopeContainer ofDuring(String during) {
        return new PropertyScopeContainer(during, null);
    }

    /**
     * Creates a new container that only carries a heading constraint.
     *
     * @param heading the heading constraint, may be {@code null}
     * @return a new {@link PropertyScopeContainer} with only {@code heading} set
     */
    public static PropertyScopeContainer ofHeading(TravelHeading heading) {
        return new PropertyScopeContainer(null, heading);
    }

    /**
     * Creates a new container that only carries travel reasons.
     *
     * @param using the list of travel reasons. If the provided list is {@code null} or empty, it is stored as {@link java.util.Collections#emptyList()}
     * @return a new {@link PropertyScopeContainer} with only {@code using} set
     */
    public static PropertyScopeContainer ofUsing(List<TravelReason> using) {
        return new PropertyScopeContainer(null, null, using);
    }

    /**
     * Creates a new container that only carries recognition statuses.
     *
     * @param recognized the list of recognized statuses. If the provided list is {@code null} or empty, it is stored as {@link java.util.Collections#emptyList()}
     * @return a new {@link PropertyScopeContainer} with only {@code recognized} set
     */
    public static PropertyScopeContainer ofRecognized(List<RecognizedStatus> recognized) {
        return new PropertyScopeContainer(null, null, emptyList(), recognized);
    }

    /**
     * Creates a new container that only carries travel modes.
     *
     * @param mode the list of travel modes. If the provided list is {@code null} or empty, it is stored as {@link java.util.Collections#emptyList()}
     * @return a new {@link PropertyScopeContainer} with only {@code mode} set
     */
    public static PropertyScopeContainer ofMode(List<TravelMode> mode) {
        return new PropertyScopeContainer(null, null, emptyList(), emptyList(), mode);
    }

    /**
     * Creates a new container that only carries vehicle attributes.
     *
     * @param vehicle the list of vehicle attributes. If the provided list is {@code null} or empty, it is stored as {@link java.util.Collections#emptyList()}
     * @return a new {@link PropertyScopeContainer} with only {@code vehicle} set
     */
    public static PropertyScopeContainer ofVehicle(List<VehicleAttributes> vehicle) {
        return new PropertyScopeContainer(null, null, emptyList(), emptyList(), emptyList(), vehicle);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PropertyScopeContainer that = (PropertyScopeContainer) o;
        return Objects.equals(during, that.during)
                && heading == that.heading
                && Objects.equals(using, that.using)
                && Objects.equals(recognized, that.recognized)
                && Objects.equals(mode, that.mode)
                && Objects.equals(vehicle, that.vehicle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(during, heading, using, recognized, mode, vehicle);
    }

    @Override
    public String toString() {
        return "PropertyScopeContainer{" + "during='"
                + during + '\'' + ", heading="
                + heading + ", using="
                + using + ", recognized="
                + recognized + ", mode="
                + mode + ", vehicle="
                + vehicle + '}';
    }
}
