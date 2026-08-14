package com.graphhopper.reader.overture.access.restriction.scope.containers;

import java.util.Objects;

/**
 * Encapsulates vehicle-related attributes used when evaluating an access restriction.
 * <p>
 * Instances typically describe a single numeric constraint such as height, width, length
 * or weight together with a {@link ComparisonOperator} and optional {@link Units}.
 */
public class VehicleAttributes {

    // ---------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------

    /** The vehicle dimension being constrained, or {@code null} if not specified. */
    private final DimensionRestriction dimensionRestriction;
    /** The comparison operator used to evaluate the numeric quantity, or {@code null} if not specified. */
    private final ComparisonOperator comparisonOperator;
    /** The numeric quantity used in the comparison, or {@code null} if not specified. */
    private final Double numericQuantity;
    /** Optional units associated with the numeric quantity, or {@code null} if not applicable. */
    private final Units units;

    // ---------------------------------------------------------------------
    // Getters
    // ---------------------------------------------------------------------

    /**
     * Returns the dimension being constrained.
     *
     * @return the {@link DimensionRestriction}, or {@code null} if not specified
     */
    public DimensionRestriction getDimension() {
        return dimensionRestriction;
    }

    /**
     * Returns the comparison operator used for this constraint.
     *
     * @return the {@link ComparisonOperator}, or {@code null} if not specified
     */
    public ComparisonOperator getComparison() {
        return comparisonOperator;
    }

    /**
     * Returns the numeric quantity used in the comparison.
     *
     * @return the numeric quantity, or {@code null} if not specified
     */
    public Double getNumericQuantity() {
        return numericQuantity;
    }

    /**
     * Returns the units associated with the numeric quantity, if any.
     *
     * @return the {@link Units}, or {@code null} if not applicable
     */
    public Units getLengthUnits() {
        return units;
    }

    // ---------------------------------------------------------------------
    // Presence checkers
    // ---------------------------------------------------------------------

    /**
     * Returns whether a dimension restriction is specified.
     *
     * @return {@code true} if a {@link DimensionRestriction} is present, {@code false} otherwise
     */
    public boolean hasDimensionRestriction() {
        return dimensionRestriction != null;
    }

    /**
     * Returns whether a comparison operator is specified.
     *
     * @return {@code true} if a {@link ComparisonOperator} is present, {@code false} otherwise
     */
    public boolean hasComparisonOperator() {
        return comparisonOperator != null;
    }

    /**
     * Returns whether a numeric quantity is specified.
     *
     * @return {@code true} if a numeric quantity is present, {@code false} otherwise
     */
    public boolean hasNumericQuantity() {
        return numericQuantity != null;
    }

    /**
     * Returns whether length units are specified.
     *
     * @return {@code true} if {@link Units} are present, {@code false} otherwise
     */
    public boolean hasLengthUnits() {
        return units != null;
    }

    // ---------------------------------------------------------------------
    // Object overrides
    // ---------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        VehicleAttributes vehicle = (VehicleAttributes) o;
        return dimensionRestriction == vehicle.dimensionRestriction
                && comparisonOperator == vehicle.comparisonOperator
                && Objects.equals(numericQuantity, vehicle.numericQuantity)
                && units == vehicle.units;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimensionRestriction, comparisonOperator, numericQuantity, units);
    }

    @Override
    public String toString() {
        return "Vehicle{" + "dimension="
                + dimensionRestriction + ", comparison="
                + comparisonOperator + ", valueData="
                + numericQuantity + ", lengthUnits="
                + units + '}';
    }

    // ---------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------

    /**
     * Creates a new set of vehicle attributes.
     *
     * @param dimensionRestriction the {@link DimensionRestriction} being constrained, or
     *                             {@code null} if not specified
     * @param comparisonOperator   the {@link ComparisonOperator} that relates the vehicle's
     *                             dimension to the numeric quantity, or {@code null} if not
     *                             specified
     * @param numericQuantity      the numeric quantity used in the comparison, or {@code null} if
     *                             not specified
     * @param units                optional {@link Units} associated with the numeric
     *                             quantity, or {@code null} if not applicable
     */
    public VehicleAttributes(
            DimensionRestriction dimensionRestriction,
            ComparisonOperator comparisonOperator,
            Double numericQuantity,
            Units units) {
        this.dimensionRestriction = dimensionRestriction;
        this.comparisonOperator = comparisonOperator;
        this.numericQuantity = numericQuantity;
        this.units = units;
    }

    /**
     * Creates attributes that constrain only the vehicle dimension.
     * <p>
     * All other properties (comparison operator, numeric quantity and units) will be
     * {@code null}. Callers can use the corresponding {@code has*} methods to detect this.
     *
     * @param dimensionRestriction the {@link DimensionRestriction} being constrained, or
     *                             {@code null} if not specified
     * @return a new {@link VehicleAttributes} instance with only {@code dimensionRestriction} set
     */
    public static VehicleAttributes ofDimensionRestriction(
            DimensionRestriction dimensionRestriction) {
        return new VehicleAttributes(dimensionRestriction, null, null, null);
    }

    /**
     * Creates attributes that constrain only via a comparison operator.
     * <p>
     * All other properties (dimension restriction, numeric quantity and units) will be
     * {@code null}. Callers can use the corresponding {@code has*} methods to detect this.
     *
     * @param comparisonOperator the {@link ComparisonOperator} used to evaluate the constraint,
     *                           or {@code null} if not specified
     * @return a new {@link VehicleAttributes} instance with only {@code comparisonOperator} set
     */
    public static VehicleAttributes ofComparisonOperator(ComparisonOperator comparisonOperator) {
        return new VehicleAttributes(null, comparisonOperator, null, null);
    }

    /**
     * Creates attributes that constrain only via a numeric quantity.
     * <p>
     * All other properties (dimension restriction, comparison operator and units) will be
     * {@code null}. Callers can use the corresponding {@code has*} methods to detect this.
     *
     * @param numericQuantity the numeric quantity used in the comparison, or {@code null}
     * @return a new {@link VehicleAttributes} instance with only {@code numericQuantity} set
     */
    public static VehicleAttributes ofNumericQuantity(Double numericQuantity) {
        return new VehicleAttributes(null, null, numericQuantity, null);
    }

    /**
     * Creates attributes that specify only the units of measurement.
     * <p>
     * All other properties (dimension restriction, comparison operator and numeric quantity)
     * will be {@code null}. Callers can use the corresponding {@code has*} methods to detect this.
     *
     * @param units the {@link Units} associated with the numeric quantity, or {@code null}
     *              if not applicable
     * @return a new {@link VehicleAttributes} instance with only {@code units} set
     */
    public static VehicleAttributes ofUnits(Units units) {
        return new VehicleAttributes(null, null, null, units);
    }

    /**
     * Checks whether the object meets the minimum requirements of the Overture schema.
     * According to the schema: dimension, comparison, and value are required.
     */
    public boolean isValid() {
        return dimensionRestriction != null && comparisonOperator != null && numericQuantity != null;
    }
}
