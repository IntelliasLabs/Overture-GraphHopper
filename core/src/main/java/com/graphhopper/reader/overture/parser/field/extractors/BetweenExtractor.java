package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.LinearlyReferencedRange;
import com.graphhopper.reader.overture.parser.features.FeatureFinder;
import org.slf4j.Logger;

/**
 * Extractor for {@code between} linear referencing properties.
 * <p>
 * Validates and parses a two-element array of doubles {@code [start, end]}
 * representing a portion of a segment.
 */
public class BetweenExtractor {
    private static final Logger logger = FeatureFinder.logger;

    /**
     * Parses the linear referencing range from the "between" property.
     * <p>
     * Overture uses a two-element array of doubles [start, end] to define the portion of
     * the segment where a property applies. This method validates that the array exists
     * and has exactly two elements before creating a {@link LinearlyReferencedRange}.
     *
     * @param betweenNode the JSON node containing the "between" key
     * @return a {@link LinearlyReferencedRange} representing the start and end positions,
     * or {@code null} if the property is missing or invalid
     */
    static LinearlyReferencedRange extractBetween(
            JsonNode betweenNode, FeatureFinder parentNode, String featureId) {
        if (betweenNode == null || betweenNode.isNull()) {
            return null;
        }
        if (betweenNode.isMissingNode()) {
            logger.warn(
                    "'{}' 'between' list field isn't presented in feature id: '{}' .", parentNode, featureId);
            return null;
        }
        if (!betweenNode.isArray()) {
            logger.warn(
                    "'{}' 'between' list field isn't of array type for feature id: '{}' .",
                    parentNode,
                    featureId);
            return null;
        }
        if (betweenNode.isEmpty()) {
            logger.warn(
                    "'{}' 'between' list field is empty for feature id: '{}' , but must be >= 2 AND <= 2.",
                    parentNode,
                    featureId);
            return null;
        }
        if (betweenNode.size() != 2) {
            logger.warn(
                    "'{}' 'between' list field size is less or more than 2 elements for feature id: '{}' , but must be >= 2 AND <= 2.",
                    parentNode,
                    featureId);
            return null;
        }

        JsonNode startBetweenNode = betweenNode.path(0);
        JsonNode endBetweenNode = betweenNode.path(1);
        if (!startBetweenNode.isNumber() || !endBetweenNode.isNumber()) {
            logger.warn(
                    "'{}' 'between' list isn't of number type for feature id: '{}' .", parentNode, featureId);
            return null;
        }

        double start = startBetweenNode.asDouble();
        double end = endBetweenNode.asDouble();
        if (end - start == 0) {
            logger.warn(
                    "'{}' 'between' list contains non-unique values for feature id: '{}' .",
                    parentNode,
                    featureId);
            return null;
        }

        LinearlyReferencedRange linearlyReferencedRange = new LinearlyReferencedRange(start, end);
        if (!linearlyReferencedRange.isValid()) {
            logger.warn(
                    "'{}' 'between' list isn't has valid values for feature id: '{}' .",
                    parentNode,
                    featureId);
            return null;
        }

        return linearlyReferencedRange;
    }
}
