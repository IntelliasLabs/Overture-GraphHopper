package com.graphhopper.reader.overture.parser.parquet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.apache.avro.generic.GenericRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internal utility class for low-level interactions with Overture Avro data structures.
 * <p>
 * This class provides technical helpers for safe field extraction, recursive unwrapping
 * of nested 'element' structures, and type conversions to prevent RuntimeExceptions
 * during the parsing process.
 * </p>
 */
public class AvroInternalUtils {

    private static final Logger logger = LoggerFactory.getLogger(AvroInternalUtils.class);
    /**
     * Set to track missing fields in the Parquet schema.
     * Ensures that a warning for a specific missing field is logged only once per session.
     */
    private static final Set<String> MISSING_FIELDS_LOGGED = ConcurrentHashMap.newKeySet();

    /**
     * Safely parses a raw value into an Enum.
     *
     * @param <T>       The Enum type.
     * @param rawValue  The raw object (typically String or Utf8 from Avro).
     * @param parser    The mapping function (e.g., {@code Mode::fromString}).
     * @param id        Segment identifier for error context.
     * @param fieldName Field name for error context.
     * @return The Enum constant, or {@code null} if the value is null or unknown.
     */
    public static <T extends Enum<T>> T safeParseOptionalEnum(
            Object rawValue, Function<String, T> parser, String id, String fieldName) {
        if (rawValue == null) return null;
        try {
            return parser.apply(rawValue.toString());
        } catch (IllegalArgumentException e) {
            logger.debug("Unknown {} '{}' for record {}, ignoring field", fieldName, rawValue, id);
            return null;
        }
    }
    /**
     * Parses a list of Avro objects, automatically unwrapping each item.
     * <p>
     * Null items and records that fail mapping are ignored. Returns an empty list
     * if the resulting list is empty after filtering.
     * </p>
     *
     * @param <T>       The resulting type of the list elements.
     * @param rawList   The raw list object from Avro.
     * @param typeName  Name of the data type (for logging purposes).
     * @param segmentId Segment identifier.
     * @param mapper    Function to transform the unwrapped object into a domain model.
     * @return An {@link List} of processed items, or an empty list.
     */
    public static <T> List<T> parseList(
            Object rawList, String typeName, String segmentId, Function<Object, T> mapper) {
        if (!(rawList instanceof List<?> list) || list.isEmpty()) return Collections.emptyList();

        List<T> result = new ArrayList<>();
        for (Object item : list) {
            if (item == null) continue;
            try {
                T value = mapper.apply(unwrap(item));
                if (value != null) {
                    result.add(value);
                }
            } catch (Exception e) {
                logger.trace("Failed to map list item: {}", item, e);
            }
        }
        if (result.isEmpty()) {
            logger.debug(
                    "Skipping {} list for segment {}: empty list after parsing", typeName, segmentId);
            return Collections.emptyList();
        }
        return result;
    }
    /**
     * Recursively unwraps the simple values or lists that are wrapped in objects with a single "element" field.
     *
     * @param item The object to unwrap.
     * @return The unwrapped object, or the original object if no "element" field exists.
     */
    public static Object unwrap(Object item) {
        if (item instanceof GenericRecord rec
                && rec.getSchema().getField(OvertureSchema.ELEMENT) != null) {
            return unwrap(rec.get(OvertureSchema.ELEMENT));
        }
        return item;
    }

    /**
     * Extracts a numeric value as {@link Double}, handling potential nesting.
     *
     * @param item The object containing a number.
     * @return The Double value, or {@code null} if the object is not a number.
     */
    public static Double extractDouble(Object item) {
        Object unwrapped = unwrap(item);
        return (unwrapped instanceof Number n) ? n.doubleValue() : null;
    }

    /**
     * Unwraps and extracts a string value from a specific field in a record.
     *
     * @param record The Avro record.
     * @param key    The field name.
     * @return The string value, or {@code null}.
     */
    public static String extractString(GenericRecord record, String key) {
        return extractString(getValOrNull(record, key));
    }

    /**
     * Converts an object to a string after unwrapping it.
     *
     * @param val The object to process.
     * @return String representation of the unwrapped object, or {@code null}.
     */
    public static String extractString(Object val) {
        Object unwrapped = unwrap(val);
        return (unwrapped != null) ? unwrapped.toString() : null;
    }

    /**
     * Safely extracts an object from a record by key, and prevents {@code AvroRuntimeException}
     * if the Parquet file has an outdated or unexpected schema.
     * </p>
     *
     * @param record The Avro record.
     * @param key    The field key.
     * @return The value object, or {@code null} if the field is missing from schema or record.
     */
    public static Object getValOrNull(GenericRecord record, String key) {
        if (record == null) return null;
        if (record.getSchema().getField(key) != null) {
            return record.get(key);
        }
        if (MISSING_FIELDS_LOGGED.add(key)) logger.warn("Field {} is missed in Parquet File", key);
        return null;
    }
}
