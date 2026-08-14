package com.graphhopper.reader.overture.road.segment.destination;

import java.util.Objects;

/**
 * Represents a labeled destination that can be reached by following a segment.
 * <p>
 * This corresponds to the text or names found on signposts or ground markings.
 * </p>
 */
public class OvertureDestinationLabel {

    /**
     * Names the object that is reached (e.g., "Berlin", "I-95").
     */
    public final String value;

    /**
     * The type of object of the destination label (e.g., street, country, route_ref).
     */
    public final OvertureDestinationLabelType type;

    public OvertureDestinationLabel(String value, OvertureDestinationLabelType type) {
        this.value = value;
        this.type = type;
    }

    /**
     * Gets the name of the object that is reached.
     * <p>
     * Examples: "Berlin", "I-95", "Main Street".
     * </p>
     *
     * @return the label text.
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the type of object of the destination label.
     *
     * @return the {@link OvertureDestinationLabelType} (e.g., STREET, COUNTRY, ROUTE_REF).
     */
    public OvertureDestinationLabelType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OvertureDestinationLabel that)) return false;
        return Objects.equals(value, that.value) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, type);
    }

    @Override
    public String toString() {
        return "OvertureDestinationLabel{value='" + value + "', type=" + type + "}";
    }
}
