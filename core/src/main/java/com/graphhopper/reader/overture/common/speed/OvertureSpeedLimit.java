package com.graphhopper.reader.overture.common.speed;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import com.graphhopper.reader.overture.access.restriction.PropertyScopeContainer;
import com.graphhopper.reader.overture.road.segment.HasBetweenProperty;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

/**
 * Represents speed limit data extracted from the Overture Maps schema.
 * <p>
 * This class encapsulates maximum and minimum speed limits along with their unit of measurement
 * and variability status.
 * </p>
 *
 * <p>Data handling details:</p>
 * <ul>
 * <li><b>Units:</b> Handles "km/h" and "mph" (case-insensitive, trims whitespace).</li>
 * <li><b>Missing Data:</b> Fields are {@link Nullable} to reflect the potential absence of data in Overture.</li>
 * <li><b>Invalid Data:</b> Raw values (e.g. negative speeds) are passed as-is to preserve data integrity for downstream processing.</li>
 * </ul>
 */
public class OvertureSpeedLimit implements HasBetweenProperty {

    private static final double MPH_TO_KMH_FACTOR = 1.60934;

    /** The maximum speed limit value and unit. */
    @Nullable private final OvertureSpeed maxSpeed;
    /** The minimum speed limit value and unit. */
    @Nullable private final OvertureSpeed minSpeed;
    /**
     * Indicates whether the maximum speed limit is variable.
     * <p> Default value in schema is {@code false}. </p>
     */
    private final boolean isMaxSpeedVariable;
    /**
     * Defines the linear range along the segment where this speed limit applies.
     * Mapped from the Overture 'between' property (array of 2 numbers).
     */
    @Nullable private final LinearlyReferencedRange between;

    /**
     * Defines the conditions under which this speed limit is active (time, vehicle, etc.).
     * Mapped from the Overture 'when' property.
     */
    @Nullable private final PropertyScopeContainer when;

    /**
     * Constructs an {@link OvertureSpeedLimit} instance with speed values, variability status, and applicability scopes.
     *
     * @param maxSpeed           the maximum speed limit, may be {@code null}
     * @param minSpeed           the minimum speed limit, may be {@code null}
     * @param isMaxSpeedVariable {@code true} if the max speed limit can vary (e.g., electronic signs),
     * defaults to {@code false} if {@code null}
     * @param between            the linear range along the segment where this limit applies,
     * may be {@code null}
     * @param when               the conditions (vehicle type, weather, time) under which
     * this limit applies, may be {@code null}
     */
    public OvertureSpeedLimit(
            @Nullable OvertureSpeed maxSpeed,
            @Nullable OvertureSpeed minSpeed,
            Boolean isMaxSpeedVariable,
            @Nullable LinearlyReferencedRange between,
            @Nullable PropertyScopeContainer when) {
        this.maxSpeed = maxSpeed;
        this.minSpeed = minSpeed;
        this.isMaxSpeedVariable = (isMaxSpeedVariable != null) ? isMaxSpeedVariable : false;
        this.between = between;
        this.when = when;
    }

    /**
     * Converts the maximum speed to kilometers per hour (km/h).
     *
     * @return speed in km/h, or {@code null} if minSpeed is missing.
     */
    public Double getMaxSpeedKmh() {
        if (maxSpeed == null || maxSpeed.getValue() == null) {
            return null;
        }
        return toKmh(maxSpeed.getValue(), maxSpeed.getUnit());
    }

    /**
     * Converts the minimum speed to kilometers per hour (km/h).
     *
     * @return speed in km/h, or {@code null} if minSpeed is missing.
     */
    @Nullable public Double getMinSpeedKmh() {
        if (minSpeed == null || minSpeed.getValue() == null) {
            return null;
        }
        return toKmh(minSpeed.getValue(), minSpeed.getUnit());
    }

    private double toKmh(Double value, @Nullable SpeedUnit unit) {
        if (unit == SpeedUnit.MPH) {
            return value * MPH_TO_KMH_FACTOR;
        }
        return value;
    }

    /**
     * Gets the maximum speed limit.
     * @return the maximum speed limit, or {@code null} if not specified
     */
    @Nullable public OvertureSpeed getMaxSpeed() {
        return maxSpeed;
    }

    /**
     * Indicates whether the maximum speed limit is variable.
     * @return {@code true} if the maximum speed limit is variable
     */
    public boolean isMaxSpeedVariable() {
        return isMaxSpeedVariable;
    }

    /**
     * Gets the minimum speed limit.
     * @return the minimum speed limit, or {@code null} if not specified
     */
    @Nullable public OvertureSpeed getMinSpeed() {
        return minSpeed;
    }

    @Override
    public String toString() {
        return "OvertureSpeedLimit{" + "maxSpeed="
                + maxSpeed + ", minSpeed="
                + minSpeed + ", isMaxSpeedVariable="
                + isMaxSpeedVariable + ", between="
                + between + ", when="
                + when + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OvertureSpeedLimit limit = (OvertureSpeedLimit) o;
        return isMaxSpeedVariable == limit.isMaxSpeedVariable
                && Objects.equals(maxSpeed, limit.maxSpeed)
                && Objects.equals(minSpeed, limit.minSpeed)
                && Objects.equals(between, limit.between)
                && Objects.equals(when, limit.when);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxSpeed, minSpeed, isMaxSpeedVariable, between, when);
    }

    /**
     * Gets the linear range along the segment where this speed limit applies.
     * @return the linear range where this limit applies, or {@code null}
     */
    @Nullable public LinearlyReferencedRange getBetween() {
        return between;
    }

    /**
     * Gets the conditions under which this speed limit is active (time, vehicle, etc.).
     * @return the conditions under which this limit is active, or {@code null}
     */
    @Nullable public PropertyScopeContainer getWhen() {
        return when;
    }
}
