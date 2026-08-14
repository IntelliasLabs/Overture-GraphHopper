package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.access.restriction.PropertyScopeContainer;
import com.graphhopper.reader.overture.access.restriction.scope.containers.*;
import com.graphhopper.reader.overture.parser.OvertureParserFilter;
import com.graphhopper.reader.overture.parser.features.FeatureParser;
import com.graphhopper.reader.overture.parser.features.WhenFeature;
import java.util.List;

/**
 * Extractor for the {@code when} property, defining conditional application scopes.
 * <p>
 * Parses temporal (during), directional (heading), and modal (travel mode, vehicle)
 * constraints that determine when a specific property or restriction is active.
 */
public class WhenExtractor {
    /**
     * Parses the conditional application scope from the "when" property.
     * <p>
     * This method extracts various constraints that define when a property is active.
     * It handles temporal constraints (during), directional constraints (heading),
     * and collections of travel reasons, recognized statuses, travel modes, and specific
     * vehicle attributes.
     * <p>
     * Collections are parsed using the universal {@link FeatureParser#parseList} helper. If the
     * "when" node is missing or null, the entire scope is considered empty.
     *
     * @param whenNode the JSON node containing the "when" key
     * @return a {@link PropertyScopeContainer} encapsulating all parsed constraints,
     * or {@code null} if no scope is defined
     */
    public static PropertyScopeContainer extractWhen(JsonNode whenNode, String featureId) {
        if (whenNode == null || whenNode.isNull()) return null;

        String during = WhenFeature.DURING.parseString(whenNode, featureId);

        String headingStr = WhenFeature.HEADING.parseString(whenNode, featureId);
        TravelHeading heading = TravelHeading.fromString(headingStr);
        if (!OvertureParserFilter.INSTANCE.getTravelHeadingFilter().isAllowed(heading)) heading = null;

        List<TravelReason> using = WhenFeature.USING.parseList(
                whenNode,
                (node, fId) -> {
                    TravelReason reason = TravelReason.fromString(node.asText());
                    return OvertureParserFilter.INSTANCE.getTravelReasonFilter().isAllowed(reason)
                            ? reason
                            : null;
                },
                featureId);

        List<RecognizedStatus> recognized = WhenFeature.RECOGNIZED.parseList(
                whenNode,
                (node, fId) -> {
                    RecognizedStatus status = RecognizedStatus.fromString(node.asText());
                    return OvertureParserFilter.INSTANCE.getRecognizedStatusFilter().isAllowed(status)
                            ? status
                            : null;
                },
                featureId);

        List<TravelMode> mode = WhenFeature.MODE.parseList(
                whenNode,
                (node, fId) -> {
                    TravelMode m = TravelMode.fromString(node.asText());
                    return OvertureParserFilter.INSTANCE.getTravelModeFilter().isAllowed(m) ? m : null;
                },
                featureId);

        List<VehicleAttributes> vehicle = WhenFeature.VEHICLE.parseList(
                whenNode, VehicleExtractor::extractVehicleAttribute, featureId);

        return new PropertyScopeContainer(during, heading, using, recognized, mode, vehicle);
    }
}
