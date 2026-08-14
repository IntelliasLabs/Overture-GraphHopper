package com.graphhopper.reader.overture.road.segment;

import java.util.Objects;

/**
 * Represents a physical connection point on a road segment.
 * <p>
 * Each connector is a possible routing decision point, defining a place along the segment
 * where it is possible to transition to other segments sharing the same connector.
 * </p>
 */
public class OvertureConnector {
    /**
     * The GERS ID of the connector feature.
     */
    private final String connectorId;

    /**
     * The relative position of the connector along the segment geometry, between 0 and 1.
     */
    private final double at;

    /**
     * Constructs a new connector point for a road segment.
     * @param connectorId the unique GERS identifier for the connector.
     * @param at the relative position (0.0 to 1.0) along the parent segment's geometry.
     */
    public OvertureConnector(String connectorId, double at) {
        this.connectorId = connectorId;
        this.at = at;
    }

    /**
     * Gets the GERS ID of the connector feature.
     *
     * @return the unique connector identifier string.
     */
    public String getConnectorId() {
        return connectorId;
    }

    /**
     * Gets the relative position of the connector along the segment geometry.
     *
     * @return a value between 0.0 (start) and 1.0 (end).
     */
    public double getAt() {
        return at;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OvertureConnector that = (OvertureConnector) o;
        return Double.compare(that.at, at) == 0 && Objects.equals(connectorId, that.connectorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectorId, at);
    }

    /**
     * Generates a string representation of the connector.
     *
     * @return a string in the format "OvertureConnector{id='connectorId', at=at}"
     */
    @Override
    public String toString() {
        return "OvertureConnector{id='" + connectorId + "', at=" + at + "}";
    }
}
