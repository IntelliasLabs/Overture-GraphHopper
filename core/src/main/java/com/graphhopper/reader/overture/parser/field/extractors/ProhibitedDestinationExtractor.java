package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelHeading;
import com.graphhopper.reader.overture.parser.OvertureParserFilter;
import com.graphhopper.reader.overture.parser.features.ProhibitedTransitionsFeature;
import com.graphhopper.reader.overture.parser.features.SegmentFeature;
import com.graphhopper.reader.overture.road.segment.rule.OvertureProhibitedTransition;
import com.graphhopper.reader.overture.road.segment.rule.OvertureTransitionSequenceItem;
import java.util.ArrayList;
import java.util.List;

/**
 * Extractor for {@code prohibited_transitions} data from Overture features.
 * <p>
 * Parses routing restrictions that define forbidden manoeuvres from a segment. Each entry is a
 * sequence of connector-and-segment hops leading away from this segment, so a two-entry sequence is a
 * via-way restriction.
 * <p>
 * Nothing consumes these yet - mapping them onto GraphHopper turn restrictions is separate work - but
 * they are extracted so that data exists for it, and so {@code SplitPointCollector} can see their
 * ranges.
 */
public class ProhibitedDestinationExtractor {

    /** Field name of the connector inside a sequence entry. */
    private static final String CONNECTOR_ID = "connector_id";

    /** Field name of the target segment inside a sequence entry. */
    private static final String SEGMENT_ID = "segment_id";

    /**
     * Extracts a list of prohibited transitions from the feature JSON.
     * @param segmentJson raw GeoJSON feature node
     * @return the prohibited transitions, empty when the property is absent or holds nothing usable
     */
    public static List<OvertureProhibitedTransition> extractProhibitedDestinations(
            JsonNode segmentJson) {
        return SegmentFeature.PROHIBITED_TRANSITIONS.parseList(
                segmentJson, ProhibitedDestinationExtractor::parseTransition, null);
    }

    /**
     * Checks for the presence of prohibited transition properties.
     * @param segmentJson raw GeoJSON feature node
     * @return {@code true} if the property exists and is not null
     */
    public static boolean prohibitedDestinationsExist(JsonNode segmentJson) {
        JsonNode node = SegmentFeature.PROHIBITED_TRANSITIONS.getFeature(segmentJson, null);
        return node != null && !node.isNull();
    }

    private static OvertureProhibitedTransition parseTransition(JsonNode item, String featureId) {
        List<OvertureTransitionSequenceItem> sequence =
                parseSequence(ProhibitedTransitionsFeature.SEQUENCE.getFeature(item, featureId));
        // Without a resolvable sequence the restriction cannot be applied to any edge, so keeping it
        // would be a silently inert entry rather than an incomplete one.
        if (sequence.isEmpty()) return null;

        TravelHeading finalHeading = TravelHeading.fromString(
                ProhibitedTransitionsFeature.FINAL_HEADING.parseString(item, featureId));
        if (!OvertureParserFilter.INSTANCE.getTravelHeadingFilter().isAllowed(finalHeading)) {
            finalHeading = null;
        }

        return new OvertureProhibitedTransition(
                sequence,
                finalHeading,
                WhenExtractor.extractWhen(
                        ProhibitedTransitionsFeature.WHEN.getFeature(item, featureId), featureId),
                BetweenExtractor.extractBetween(
                        ProhibitedTransitionsFeature.BETWEEN.getFeature(item, featureId),
                        SegmentFeature.PROHIBITED_TRANSITIONS,
                        featureId));
    }

    private static List<OvertureTransitionSequenceItem> parseSequence(JsonNode sequenceNode) {
        if (sequenceNode == null || !sequenceNode.isArray()) return List.of();

        List<OvertureTransitionSequenceItem> sequence = new ArrayList<>(sequenceNode.size());
        for (JsonNode entry : sequenceNode) {
            if (entry == null || entry.isNull()) continue;
            JsonNode segmentId = entry.get(SEGMENT_ID);
            if (segmentId == null || segmentId.isNull() || segmentId.asText().isBlank()) continue;

            JsonNode connectorId = entry.get(CONNECTOR_ID);
            sequence.add(new OvertureTransitionSequenceItem(
                    connectorId == null || connectorId.isNull() ? null : connectorId.asText(),
                    segmentId.asText()));
        }
        return sequence;
    }
}
