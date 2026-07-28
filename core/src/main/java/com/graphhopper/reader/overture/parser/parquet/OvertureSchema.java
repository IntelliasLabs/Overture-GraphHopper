package com.graphhopper.reader.overture.parser.parquet;

/**
 * Internal schema definition for Overture Maps GeoParquet files.
 * <p>
 * This class serves as a centralized registry of column and field names,
 * reflecting the Overture "Transportation" theme structure. It is organized into
 * nested classes that mirror the hierarchical Avro schema of the source data.</p>
 */
public final class OvertureSchema {
    private OvertureSchema() {}

    static final String ELEMENT = "element";

    //  --- Rooot columns ---

    static final String ID = "id";
    static final String CLASS = "class";
    static final String SUBTYPE = "subtype";
    static final String SUBCLASS = "subclass";
    static final String NAMES = "names";
    /**
     * The name of the column containing geometry data in Overture Parquet files.
     * According to Overture schema, this is typically a binary WKB field.
     */
    static final String GEOMETRY = "geometry";

    static final String SPEED_LIMITS = "speed_limits";
    static final String ACCESS_RESTRICTIONS = "access_restrictions";
    static final String ROAD_SURFACE = "road_surface";
    static final String ROAD_FLAGS = "road_flags";
    static final String CONNECTORS = "connectors";

    static final String ROUTES = "routes";
    static final String DESTINATIONS = "destinations";
    static final String PROHIBITED_TRANSITIONS = "prohibited_transitions";
    static final String WIDTH_RULES = "width_rules";
    static final String SUBCLASS_RULES = "subclass_rules";
    static final String LEVEL_RULES = "level_rules";
    static final String SOURCES = "sources";
    static final String VERSION = "version";

    /**
     * Columns the GeoJSON schema has but GeoParquet does not.
     *
     * <p>{@code theme} and {@code type} identify the Overture dataset partition rather than the record,
     * so a GeoParquet file carries them in its directory layout, not in a column. {@code level} was
     * replaced by {@link #LEVEL_RULES}. A Parquet import therefore leaves these at their defaults
     * legitimately, which is asserted by {@code OvertureCrossFormatEquivalenceTest} rather than left
     * as a suspicious-looking gap.
     */
    static final String[] ABSENT_FROM_PARQUET = {"theme", "type", "level"};

    //  --- nested structures ---

    /// Fields related to speed limits: speed_limits.element.xxx
    static final class Speed {
        static final String MAX_SPEED = "max_speed";
        static final String MIN_SPEED = "min_speed";
        static final String VALUE = "value";
        static final String UNIT = "unit";
        static final String IS_VARIABLE = "is_max_speed_variable";
    }

    /// Scope properties for conditions and temporal restrictions.
    static final class Scope {
        static final String BETWEEN = "between";
        static final String WHEN = "when";
        static final String DURING = "during";
        static final String HEADING = "heading";
        static final String USING = "using";
        static final String RECOGNIZED = "recognized";
        static final String MODE = "mode";
        static final String VEHICLE = "vehicle";
    }

    /// Physical vehicle attributes (height, weight, etc.) used in restrictions.
    static final class Vehicle {
        static final String DIMENSION = "dimension";
        static final String COMPARISON = "comparison";
        static final String UNIT = "unit";
        static final String VALUE = "value";
    }

    /// Properties for access and turn restrictions.
    static final class Restriction {
        static final String ACCESS_TYPE = "access_type";
    }

    /// Root properties for naming, including localized rules.
    static final class Names {
        static final String PRIMARY = "primary";
        static final String COMMON = "common";
        static final String RULES = "rules";

        /// Fields located inside each rule element: names.rules.element.xxx
        static final class Rule {
            static final String VARIANT = "variant";
            static final String LANGUAGE = "language";
            static final String VALUE = "value";
            static final String PERSPECTIVES = "perspectives";
            static final String SIDE = "side";

            /// Deeper perspective nested fields are kept with prefixes
            static final String PERSPECTIVE_MODE = "mode";
            static final String PERSPECTIVE_COUNTRIES = "countries";
        }
    }

    /// Properties for road surface
    static final class Surface {
        static final String VALUE = "value";
    }

    /// Shared field of the simple value-plus-range rule columns (width, subclass and level rules).
    static final class Rule {
        static final String VALUE = "value";
    }

    /// Fields of a route element: routes.element.xxx
    static final class Route {
        static final String NAME = "name";
        static final String NETWORK = "network";
        static final String REF = "ref";
        static final String SYMBOL = "symbol";
        static final String WIKIDATA = "wikidata";
    }

    /// Fields of a source element: sources.element.xxx
    static final class Source {
        static final String PROPERTY = "property";
        static final String DATASET = "dataset";
        static final String LICENSE = "license";
        static final String RECORD_ID = "record_id";
        static final String UPDATE_TIME = "update_time";
        static final String CONFIDENCE = "confidence";
    }

    /// Fields of a prohibited-transition element: prohibited_transitions.element.xxx
    static final class ProhibitedTransition {
        static final String SEQUENCE = "sequence";
        static final String FINAL_HEADING = "final_heading";

        /// Fields inside each sequence entry: ...sequence.element.xxx
        static final String CONNECTOR_ID = "connector_id";
        static final String SEGMENT_ID = "segment_id";
    }

    /// Fields of a destination element: destinations.element.xxx
    static final class Destination {
        static final String LABELS = "labels";
        static final String SYMBOLS = "symbols";
        static final String FROM_CONNECTOR_ID = "from_connector_id";
        static final String TO_SEGMENT_ID = "to_segment_id";
        static final String TO_CONNECTOR_ID = "to_connector_id";
        static final String FINAL_HEADING = "final_heading";

        /// Fields inside each label entry: ...labels.element.xxx
        static final String LABEL_VALUE = "value";
        static final String LABEL_TYPE = "type";
    }

    /// Properties for road flags
    static final class Flags {
        static final String VALUES = "values";

        // inner fields for road flag values
        static final String IS_BRIDGE = "is_bridge";
        static final String IS_TUNNEL = "is_tunnel";
        static final String IS_UNDER_CONSTRUCTION = "is_under_construction";
        static final String IS_INDOOR = "is_indoor";
        static final String IS_ABANDONED = "is_abandoned";
        static final String IS_COVERED = "is_covered";
    }
}
