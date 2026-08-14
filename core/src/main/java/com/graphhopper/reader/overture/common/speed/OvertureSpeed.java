package com.graphhopper.reader.overture.common.speed;

import java.util.Objects;
import org.jetbrains.annotations.Nullable;

/**
 * Class representing the {value, unit} structure from the Overture schema.
 * See propertyDefinitions/speed in the YAML schema.
 */
public class OvertureSpeed {

    /**
     * The minimum valid speed value permitted by the Overture Maps schema (inclusive).
     * According to the specification, the speed value must be {@code >= 1}.
     */
    private static final double MIN_VALID_VALUE = 1.0;

    /**
     * The maximum valid speed value permitted by the Overture Maps schema (inclusive).
     * According to the specification, the speed value must be {@code <= 350}.
     */
    private static final double MAX_VALID_VALUE = 350.0;

    /** The numeric value of the speed limit. */
    @Nullable private final Double value;
    /** The unit of measurement for the speed value. */
    @Nullable private final SpeedUnit unit;

    /**
     * Constructs an {@link OvertureSpeed} instance with the specified value and unit.
     *
     * @param value the numeric value of the speed limit, may be {@code null}
     * @param unit the unit of measurement, may be {@code null}
     */
    public OvertureSpeed(@Nullable Double value, @Nullable SpeedUnit unit) {
        this.value = value;
        this.unit = unit;
    }

    /**
     * Checks if the speed value conforms to the Overture schema constraints.
     * Schema requires integer values between 1 and 350 (inclusive).
     *
     * @return true if value is present and within [1, 350], false otherwise.
     */
    public boolean isValid() {
        if (value == null) return false;

        return value >= MIN_VALID_VALUE && value <= MAX_VALID_VALUE;
    }

    /**
     * Gets the numeric value of the speed limit.
     * @return raw speed value. Warning: check {@link #getUnit()} before use.
     */
    @Nullable public Double getValue() {
        return value;
    }

    /**
     * Gets the unit of measurement for the speed value.
     * @return the unit of measurement.
     */
    @Nullable public SpeedUnit getUnit() {
        return unit;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OvertureSpeed speed1 = (OvertureSpeed) o;
        return Objects.equals(value, speed1.value) && Objects.equals(unit, speed1.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, unit);
    }

    /**
     * Returns a string representation of this speed containing the value and unit.
     * @return a string formatted as Speed{speed=value, unit='unit'}
     */
    @Override
    public String toString() {
        return "Speed{" + "speed=" + value + ", unit='" + unit + '\'' + '}';
    }
}
