package com.graphhopper.reader.overture.road.segment;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Stores the source information for a specific property within the feature.
 * <p>
 * This class tracks the provenance of data, including the dataset it came from,
 * licensing information, and confidence scores (particularly for ML-derived data).
 * </p>
 */
public class OvertureSource implements HasBetweenProperty {

    /**
     * A reference to the property element within this Feature.
     * <p>
     * Referenced using JSON Pointer Notation RFC 6901 (e.g., "/properties/road_surface").
     * </p>
     */
    private final String property;

    /**
     * The name of the source dataset.
     * <p>
     * Specified in the Overture list of approved sources containing relevant metadata.
     * </p>
     */
    private final String dataset;

    /**
     * The license name or SPDX license identifier.
     * <p>
     * If null, contact the data provider for license information.
     * </p>
     */
    private final String license;

    /**
     * The specific record ID within the dataset that was used.
     */
    private final String recordId;

    /**
     * The timestamp when the feature was last updated.
     * <p>
     * Represents an ISO 8601 timestamp (e.g., "2023-10-01T12:00:00Z").
     * </p>
     */
    private final OffsetDateTime updateTime;

    /**
     * The confidence value from the source dataset.
     * <p>
     * Particularly relevant for ML-derived data. Must be between 0.0 and 1.0 (inclusive).
     * </p>
     */
    private final double confidence;

    /**
     * The linear range along the segment where this source applies.
     * <p>
     * If null, the source applies to the entire segment.
     * </p>
     */
    private final LinearlyReferencedRange between;

    public OvertureSource(
            String property,
            String dataset,
            String license,
            String recordId,
            OffsetDateTime updateTime,
            double confidence,
            LinearlyReferencedRange between) {
        this.property = property;
        this.dataset = dataset;
        this.license = license;
        this.recordId = recordId;
        this.updateTime = updateTime;
        this.confidence = confidence;
        this.between = between;
    }

    /**
     * Gets the reference to the property element within this Feature.
     * <p>
     * Referenced using JSON Pointer Notation RFC 6901.
     * </p>
     *
     * @return the property path string.
     */
    public String getProperty() {
        return property;
    }

    /**
     * Gets the name of the source dataset.
     *
     * @return the dataset name.
     */
    public String getDataset() {
        return dataset;
    }

    /**
     * Gets the license name, preferably a valid SPDX license identifier.
     *
     * @return the license string, or null if not available.
     */
    public String getLicense() {
        return license;
    }

    /**
     * Gets the specific record ID within the dataset that was used.
     *
     * @return the record ID, or null.
     */
    public String getRecordId() {
        return recordId;
    }

    /**
     * Gets the timestamp when the feature was last updated.
     *
     * @return the update time as an {@link OffsetDateTime}, or null.
     */
    public OffsetDateTime getUpdateTime() {
        return updateTime;
    }

    /**
     * Gets the confidence value from the source dataset.
     * <p>
     * Particularly relevant for ML-derived data. Ranges from 0.0 to 1.0.
     * </p>
     *
     * @return the confidence score.
     */
    public double getConfidence() {
        return confidence;
    }

    /**
     * Gets the linear range along the segment where this source applies.
     *
     * @return the {@link LinearlyReferencedRange}, or null if it applies to the whole segment.
     */
    @Override
    public LinearlyReferencedRange getBetween() {
        return between;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OvertureSource that)) return false;
        return Double.compare(getConfidence(), that.getConfidence()) == 0
                && Objects.equals(getProperty(), that.getProperty())
                && Objects.equals(getDataset(), that.getDataset())
                && Objects.equals(getLicense(), that.getLicense())
                && Objects.equals(getRecordId(), that.getRecordId())
                && Objects.equals(getUpdateTime(), that.getUpdateTime())
                && Objects.equals(getBetween(), that.getBetween());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getProperty(),
                getDataset(),
                getLicense(),
                getRecordId(),
                getUpdateTime(),
                getConfidence(),
                getBetween());
    }

    /**
     * Provides a string representation of the OvertureSource for debugging purposes.
     *
     * @return a string summarizing the OvertureSource fields.
     */
    @Override
    public String toString() {
        return "OvertureSource{" + "property='"
                + property + '\'' + ", dataset='"
                + dataset + '\'' + ", license='"
                + license + '\'' + ", recordId='"
                + recordId + '\'' + ", updateTime="
                + updateTime + ", confidence="
                + confidence + ", between="
                + between + '}';
    }
}
