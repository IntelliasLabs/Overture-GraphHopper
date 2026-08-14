package com.graphhopper.reader.overture.parser.field.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphhopper.reader.overture.access.restriction.scope.containers.ComparisonOperator;
import com.graphhopper.reader.overture.access.restriction.scope.containers.DimensionRestriction;
import com.graphhopper.reader.overture.access.restriction.scope.containers.Units;
import com.graphhopper.reader.overture.access.restriction.scope.containers.VehicleAttributes;
import com.graphhopper.reader.overture.parser.OvertureParserFilter;
import com.graphhopper.reader.overture.parser.features.VehicleFeature;

/**
 * Extractor for vehicle-specific attributes and constraints from Overture features.
 * <p>
 * Parses dimensions such as weight, height, and width, along with comparison operators
 * and units to define access restrictions for different vehicle types.
 */
public class VehicleExtractor {

    /**
     * Parses a single vehicle attribute object from the provided JSON node.
     * <p>
     * This method extracts specific vehicle constraints such as dimensions (weight, height, etc.),
     * comparison operators, numeric values, and units. It uses safe path navigation to handle
     * optional fields, returning {@code null} for individual fields if they are missing in the JSON.
     *
     * @param vehicleNode the JSON node representing a vehicle attribute object
     * @return a {@link VehicleAttributes} instance containing the parsed constraints
     */
    public static VehicleAttributes extractVehicleAttribute(JsonNode vehicleNode, String featureId) {
        if (vehicleNode == null || !vehicleNode.isObject()) return null;

        String dimStr = VehicleFeature.DIMENSION.parseString(vehicleNode, featureId);
        String compStr = VehicleFeature.COMPARISON.parseString(vehicleNode, featureId);
        Double val = VehicleFeature.VALUE.parseDouble(vehicleNode, featureId);
        String unitStr = VehicleFeature.UNIT.parseString(vehicleNode, featureId);

        DimensionRestriction dim = DimensionRestriction.fromString(dimStr);
        ComparisonOperator comp = ComparisonOperator.fromString(compStr);
        Units unit = Units.fromString(unitStr);

        if (!OvertureParserFilter.INSTANCE.getDimensionRestrictionFilter().isAllowed(dim)) return null;
        if (!OvertureParserFilter.INSTANCE.getComparisonOperatorFilter().isAllowed(comp)) return null;
        if (!OvertureParserFilter.INSTANCE.getUnitsFilter().isAllowed(unit)) unit = null;

        VehicleAttributes attributes = new VehicleAttributes(dim, comp, val, unit);

        return attributes.isValid() ? attributes : null;
    }
}
