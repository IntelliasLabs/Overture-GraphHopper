package com.graphhopper.reader.overture.parser.features;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.parser.OvertureParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common contract for accessing named properties of a GeoJSON feature used by the Overture parser.
 * <p>
 * Implementations typically represent a single field (or nested field) within a feature and know
 * how to locate their value inside the JSON tree.
 */
public interface FeatureFinder {
    Logger logger = LoggerFactory.getLogger(OvertureParser.class);

    /**
     * Returns the name of the feature constantly.
     * @return the logical name of the feature property (usually the enum constant name).
     */
    String getName();

    /**
     * Returns the parent feature context for this feature.
     * @return the parent feature extractor when this feature is nested under another property,
     * or {@code null} if it is a top-level property
     */
    FeatureFinder getParentFeature();

    /**
     * Returns an alternative name for the feature if applicable.
     * @return an alternative JSON field name if it differs from {@link #getName()}, or
     * {@code null} to derive the field name from {@link #getName()}
     */
    String getOtherName();

    /**
     * Indicates whether this feature is mandatory in the Overture schema.
     * @return {@code true} if this feature is required to be present in the JSON, {@code false}
     * otherwise
     */
    boolean isRequired();

    /**
     * Checks if this feature is represented as an array in the source data.
     * @return {@code true} if the underlying JSON value is expected to be an array.
     */
    boolean isArray();

    /**
     * Resolves and returns the JSON node for this feature from the given feature JSON.
     * <p>
     * The resolution takes into account a potential {@linkplain #getParentFeature() parent}
     * and an alternative field name ({@link #getOtherName()}). If the feature or its parent
     * cannot be found, {@code null} is returned.
     *
     * @param featureJson the full JSON representation of the feature
     * @return the JSON node corresponding to this feature, or {@code null} if it does not exist
     */
    default JsonNode getFeature(JsonNode featureJson, String featureId) {
        if (featureJson == null) {
            return null;
        }

        String featureName =
                getOtherName() != null ? getOtherName() : this.getName().toLowerCase();

        FeatureFinder parentFeature = getParentFeature();
        JsonNode currentNode;
        if (parentFeature != null) {
            currentNode = parentFeature.getFeature(featureJson, featureId);

            if (currentNode == null) {
                return null;
            }
            if (currentNode.isNull()) {
                if (isRequired())
                    logger.warn(
                            "Parent node '{}' for '{}' field is null in feature id: '{}' .",
                            featureName,
                            parentFeature.getName(),
                            featureId);

                return null;
            }
        } else {
            currentNode = featureJson;
        }

        currentNode = currentNode.path(featureName);
        if (currentNode.isNull() || currentNode.isMissingNode()) {
            return null;
        }
        if (isArray()) {
            if (!currentNode.isArray()) {
                logger.warn(
                        "'{}' '{}' field isn't of array type in feature id: '{}' .",
                        parentFeature != null ? parentFeature.getName() : "FEATURE",
                        featureName,
                        featureId);
                return null;
            }
            if (currentNode.isEmpty()) {
                logger.warn(
                        "'{}' '{}' list is empty in feature id: '{}' .",
                        parentFeature != null ? parentFeature.getName() : "FEATURE",
                        featureName,
                        featureId);
                return null;
            }
        }

        return currentNode;
    }

    /**
     * Checks if this feature is considered to exist in the given JSON feature.
     * <p>
     * For non-required features this always returns {@code true}. For required features this
     * delegates to {@link #getFeature(JsonNode, String)} and checks that it is non-{@code null}.
     *
     * @param featureJson the full JSON representation of the feature
     * @return {@code true} if the feature is present or not required, {@code false} otherwise
     */
    default boolean existsIn(JsonNode featureJson, String featureId) {
        if (getFeature(featureJson, featureId) != null) {
            return true;
        } else {
            if (isRequired()) {
                logger.warn("Required feature '{}' is missing.", getName());
            }
            return !isRequired();
        }
    }
}
