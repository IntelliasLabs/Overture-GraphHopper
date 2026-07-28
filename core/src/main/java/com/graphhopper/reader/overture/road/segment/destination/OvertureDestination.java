package com.graphhopper.reader.overture.road.segment.destination;

import com.graphhopper.reader.overture.access.restriction.PropertyScopeContainer;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelHeading;
import java.util.List;
import java.util.Objects;

/**
 * Describes objects that can be reached by following a transportation segment in the
 * same way those objects are described on signposts or ground writing.
 * <p>
 * This allows navigation systems to refer to signs and observable writing that a traveler
 * actually sees in the real world.
 * </p>
 */
public class OvertureDestination {
    private final List<OvertureDestinationLabel> labels;
    private final List<OvertureDestinationSymbol> symbols;
    private final String fromConnectorId;
    private final String toSegmentId;
    private final String toConnectorId;
    private final PropertyScopeContainer when;
    private final TravelHeading finalHeading;

    public OvertureDestination(
            List<OvertureDestinationLabel> labels,
            List<OvertureDestinationSymbol> symbols,
            String fromConnectorId,
            String toSegmentId,
            String toConnectorId,
            PropertyScopeContainer when,
            TravelHeading finalHeading) {
        this.labels = labels;
        this.symbols = symbols;
        this.fromConnectorId = fromConnectorId;
        this.toSegmentId = toSegmentId;
        this.toConnectorId = toConnectorId;
        this.when = when;
        this.finalHeading = finalHeading;
    }

    /**
     * Gets the labeled destinations that can be reached by following the segment.
     *
     * @return a list of {@link OvertureDestinationLabel} objects, or null/empty.
     */
    public List<OvertureDestinationLabel> getLabels() {
        return labels;
    }

    /**
     * Gets the collection of symbols or icons present on the sign next to the current destination label.
     *
     * @return a list of {@link OvertureDestinationSymbol} objects, or null/empty.
     */
    public List<OvertureDestinationSymbol> getSymbols() {
        return symbols;
    }

    /**
     * Identifies the point of physical connection on this segment before which
     * the destination sign or marking is visible.
     *
     * @return the connector ID string.
     */
    public String getFromConnectorId() {
        return fromConnectorId;
    }

    /**
     * Identifies the segment to transition to in order to reach the destination(s) labeled
     * on the sign or marking.
     *
     * @return the segment ID string.
     */
    public String getToSegmentId() {
        return toSegmentId;
    }

    /**
     * Identifies the point of physical connection on the segment identified by
     * {@code toSegmentId} to transition to for reaching the destination(s).
     *
     * @return the connector ID string.
     */
    public String getToConnectorId() {
        return toConnectorId;
    }

    /**
     * Gets the conditions (temporal, heading, etc.) under which this destination information applies.
     *
     * @return the {@link PropertyScopeContainer}, or null if unconditional.
     */
    public PropertyScopeContainer getWhen() {
        return when;
    }

    /**
     * Gets the direction of travel on the segment identified by {@code toSegmentId} that leads
     * to the destination.
     *
     * @return the {@link TravelHeading} (e.g., FORWARD or BACKWARD).
     */
    public TravelHeading getFinalHeading() {
        return finalHeading;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OvertureDestination that)) return false;
        return Objects.equals(getLabels(), that.getLabels())
                && Objects.equals(getSymbols(), that.getSymbols())
                && Objects.equals(getFromConnectorId(), that.getFromConnectorId())
                && Objects.equals(getToSegmentId(), that.getToSegmentId())
                && Objects.equals(getToConnectorId(), that.getToConnectorId())
                && Objects.equals(getWhen(), that.getWhen())
                && getFinalHeading() == that.getFinalHeading();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getLabels(),
                getSymbols(),
                getFromConnectorId(),
                getToSegmentId(),
                getToConnectorId(),
                getWhen(),
                getFinalHeading());
    }

    @Override
    public String toString() {
        return "OvertureDestination{" + "labels="
                + labels + ", symbols="
                + symbols + ", fromConnectorId='"
                + fromConnectorId + '\'' + ", toSegmentId='"
                + toSegmentId + '\'' + ", toConnectorId='"
                + toConnectorId + '\'' + ", when="
                + when + ", finalHeading="
                + finalHeading + '}';
    }
}
