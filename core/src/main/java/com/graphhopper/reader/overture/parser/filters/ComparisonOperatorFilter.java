package com.graphhopper.reader.overture.parser.filters;

import com.graphhopper.reader.overture.access.restriction.scope.containers.ComparisonOperator;

/**
 * Filter for {@link ComparisonOperator} values used in Overture data constraints.
 * <p>
 * Controls which operators (e.g., EQUALS, GREATER_THAN) are recognized when
 * evaluating vehicle dimension or speed restrictions.
 */
public class ComparisonOperatorFilter extends OvertureFilter<ComparisonOperator> {
    @Override
    public void initializeAllowedValues() {
        // TODO: Maybe we want to initialize with specific values instead of all?
        for (var val : ComparisonOperator.values()) {
            putAllowed(val);
        }
    }
}
