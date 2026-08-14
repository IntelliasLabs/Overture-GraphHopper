package com.graphhopper.reader.overture.parser.filters;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic filter that stores a set of allowed values of type {@code T}.
 * <p>
 * Concrete domain-specific filters (e.g. {@code RoadClassFilter}, {@code ThemeFilter})
 * extend this class and typically call {@link #putAllowed(Object)} during initialization
 * to register the values that should be accepted.
 *
 * @param <T> the type of values managed by this filter
 */
public class OvertureFilter<T> {
    /** Thread-safe storage for allowed values identified by unique keys. */
    private ConcurrentHashMap<UUID, T> allowedValues;

    public OvertureFilter() {
        allowedValues = new ConcurrentHashMap<>();
    }

    /**
     * Resets or prepares the filter's internal state.
     * To be overridden by subclasses for specific domain logic.
     */
    void initializeAllowedValues() {
        allowedValues = new ConcurrentHashMap<>();
    }

    /**
     * Adds a value to the whitelist of allowed items.
     * @param value the item to permit
     */
    public final void putAllowed(T value) {
        allowedValues.put(UUID.randomUUID(), value);
    }

    /**
     * Removes a value from the whitelist.
     * @param value the item to exclude
     */
    public final void removeAllowed(T value) {
        if (value == null) return;
        allowedValues.values().removeIf(v -> v == value);
    }

    /**
     * Checks if a specific value is permitted by the filter.
     * @param value the item to check
     * @return {@code true} if the item is in the allowed set, {@code false} otherwise
     */
    public final boolean isAllowed(T value) {
        if (value == null) return false;
        return allowedValues.containsValue(value);
    }

    /**
     * Returns the value if it is allowed, otherwise returns {@code null}.
     * @param value the item to retrieve
     * @return the permitted value or {@code null}
     */
    public T getAllowedValue(T value) {
        if (isAllowed(value)) return value;
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OvertureFilter<?> that = (OvertureFilter<?>) o;
        return Objects.equals(allowedValues, that.allowedValues);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(allowedValues);
    }

    @Override
    public String toString() {
        return "OvertureFilter{" + "allowedValues=" + allowedValues + '}';
    }
}
