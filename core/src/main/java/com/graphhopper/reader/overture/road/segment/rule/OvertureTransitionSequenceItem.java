package com.graphhopper.reader.overture.road.segment.rule;

import java.util.Objects;

/**
 * Represents a single step in a transition sequence.
 * <p>
 * This is used within prohibited transitions to define the path leading up to a restriction.
 * It identifies a specific segment and the connector used to reach or leave it.
 * </p>
 */
public class OvertureTransitionSequenceItem {

    /**
     * The ID of the connector involved in this step of the transition.
     */
    private final String connectorId;

    /**
     * The ID of the segment involved in this step of the transition.
     */
    private final String segmentId;

    /**
     * Constructs a new sequence item for a transition rule.
     * @param connectorId the unique identifier of the connector at the junction.
     * @param segmentId the unique identifier of the segment being traversed.
     */
    public OvertureTransitionSequenceItem(String connectorId, String segmentId) {
        this.connectorId = connectorId;
        this.segmentId = segmentId;
    }

    /**
     * Gets the ID of the connector.
     *
     * @return the connector ID string.
     */
    public String getConnectorId() {
        return connectorId;
    }

    /**
     * Gets the ID of the segment.
     *
     * @return the segment ID string.
     */
    public String getSegmentId() {
        return segmentId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OvertureTransitionSequenceItem that)) return false;
        return Objects.equals(getConnectorId(), that.getConnectorId())
                && Objects.equals(getSegmentId(), that.getSegmentId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getConnectorId(), getSegmentId());
    }

    @Override
    public String toString() {
        return "OvertureTransitionSequenceItem{" + "connectorId='"
                + connectorId + '\'' + ", segmentId='"
                + segmentId + '\'' + '}';
    }
}
