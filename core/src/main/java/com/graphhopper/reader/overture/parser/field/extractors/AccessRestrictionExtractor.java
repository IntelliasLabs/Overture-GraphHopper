package com.graphhopper.reader.overture.parser.field.extractors;

import static java.util.Collections.emptyList;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.LinearlyReferencedRange;
import com.graphhopper.reader.overture.access.restriction.AccessType;
import com.graphhopper.reader.overture.access.restriction.OvertureAccessRestriction;
import com.graphhopper.reader.overture.access.restriction.PropertyScopeContainer;
import com.graphhopper.reader.overture.parser.features.AccessRestrictionsFeature;
import com.graphhopper.reader.overture.parser.features.SegmentFeature;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extractor for {@code access_restrictions} data from Overture features.
 * <p>
 * Parses an array of restrictions, handling access types, temporal scopes (when),
 * and linear ranges (between).
 */
public class AccessRestrictionExtractor {

    private static final Logger logger = LoggerFactory.getLogger(AccessRestrictionExtractor.class);

    /**
     * Extracts and validates the access_restrictions array from the feature properties.
     * <p>
     * Each entry may contain an {@code access_type}, an optional {@code when} scope and an
     * optional {@code between} linear range. Entries that contain no usable data are skipped.
     *
     * @param segmentJson the JSON node of a GeoJSON feature
     * @return a list of valid {@link OvertureAccessRestriction}s, or {@code null} if empty or missing
     */
    public static List<OvertureAccessRestriction> extractAccessRestrictions(
            JsonNode segmentJson, String featureId) {
        JsonNode restrictionsNode =
                SegmentFeature.ACCESS_RESTRICTIONS.getFeature(segmentJson, featureId);

        if (restrictionsNode == null) {
            logger.debug("No access_restrictions feature present for segment");
            return emptyList();
        }

        List<OvertureAccessRestriction> accessRestrictions = new ArrayList<>();

        for (JsonNode restrictionNode : restrictionsNode) {
            if (restrictionNode == null || restrictionNode.isNull() || !restrictionNode.isObject()) {
                continue;
            }

            String accessTypeStr =
                    AccessRestrictionsFeature.ACCESS_TYPE.parseString(restrictionNode, featureId);
            AccessType accessType = AccessType.fromString(accessTypeStr);
            if (accessType == null) {
                logger.warn("Unknown access_type '{}' in segment; skipping access_type", accessTypeStr);
            }

            PropertyScopeContainer when = WhenExtractor.extractWhen(
                    AccessRestrictionsFeature.WHEN.getFeature(restrictionNode, featureId), featureId);

            LinearlyReferencedRange between = BetweenExtractor.extractBetween(
                    AccessRestrictionsFeature.BETWEEN.getFeature(restrictionNode, featureId),
                    AccessRestrictionsFeature.BETWEEN,
                    featureId);

            if (accessType == null && when == null && between == null) {
                continue;
            }
            accessRestrictions.add(new OvertureAccessRestriction(accessType, when, between));
        }

        return accessRestrictions.isEmpty() ? emptyList() : accessRestrictions;
    }

    /**
     * Checks for the presence of the {@code access_restrictions} property.
     * @param segmentJson raw GeoJSON feature node
     * @param featureId   identifier of the feature
     * @return {@code true} if the property exists and is not null
     */
    public static boolean accessRestrictionsExist(JsonNode segmentJson, String featureId) {
        return SegmentFeature.ACCESS_RESTRICTIONS.existsIn(segmentJson, featureId);
    }
}
