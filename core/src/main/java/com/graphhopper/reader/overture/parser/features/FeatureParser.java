package com.graphhopper.reader.overture.parser.features;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

/**
 * Interface for parsing Overture Map features from JSON nodes.
 * <p>
 * Provides default methods for safe navigation and extraction of data types
 * from {@link JsonNode} structures.
 */
public interface FeatureParser extends FeatureFinder {
    /**
     * Safely navigates to the feature and extracts its value as a {@link Double}.
     *
     * @param featureJson the JSON node to start navigation from (typically the feature root)
     * @return the double value, or {@code null} if the feature is missing, null,
     * or not a numeric type
     */
    default Double parseDouble(JsonNode featureJson, String featureId) {
        JsonNode node = getFeature(featureJson, featureId);
        return (node != null && node.isNumber()) ? node.asDouble() : null;
    }

    /**
     * Safely navigates to the feature and extracts its value as a {@link String}.
     *
     * @param featureJson the JSON node to start navigation from (typically the feature root)
     * @return the string value, or {@code null} if the feature is missing, null,
     * or not a textual type
     */
    default String parseString(JsonNode featureJson, String featureId) {
        JsonNode node = getFeature(featureJson, featureId);
        return (node != null && node.isTextual()) ? node.asText().trim() : null;
    }

    /**
     * Safely navigates to the feature and extracts its value as a {@link Integer}.
     *
     * @param featureJson the JSON node to start navigation from (typically the feature root)
     * @return the integer value, or {@code null} if the feature is missing, null,
     * or not a numeric type
     */
    default Integer parseInteger(JsonNode featureJson, String featureId) {
        JsonNode node = getFeature(featureJson, featureId);
        return (node != null && node.isIntegralNumber() || node.canConvertToInt())
                ? node.asInt()
                : null;
    }

    /**
     * Safely extracts a boolean value from a JsonNode.
     *
     * @param featureJson         the JSON node to extract from
     * @return the boolean value from the node, or {@code defaultValue} if the node
     * is null, missing, or not a boolean
     */
    default Boolean parseBoolean(JsonNode featureJson, String featureId) {
        JsonNode node = getFeature(featureJson, featureId);
        return (node != null && !node.isMissingNode() && node.isBoolean()) ? node.asBoolean() : null;
    }

    /**
     * Safely extracts a boolean value from a JsonNode, with a fallback default.
     *
     * @param featureJson         the JSON node to extract from
     * @param defaultValue the value to return if extraction fails
     * @return the boolean value from the node, or {@code defaultValue} if the node
     * is null, missing, or not a boolean
     */
    default Boolean parseBoolean(JsonNode featureJson, Boolean defaultValue, String featureId) {
        JsonNode node = getFeature(featureJson, featureId);
        return (node != null && !node.isMissingNode() && node.isBoolean())
                ? node.asBoolean()
                : defaultValue;
    }

    /**
     * A universal helper method for parsing a JSON array into a list of typed objects.
     * <p>
     * This method handles common boilerplate for array parsing: null-checks, array validation,
     * and iteration. It applies the provided {@code mapper} function to each element of the array.
     * Elements that result in a {@code null} value after mapping are automatically excluded
     * from the final list.
     *
     * @param <T>       the type of objects in the resulting list
     * @param featureNode the JSON node expected to be an array
     * @param mapper    a function that defines how to convert a single {@link JsonNode} into an object of type T
     * @return an {@link ArrayList} of parsed objects, or an empty list if the input node
     * is not a valid array or the resulting list is empty
     */
    default <T> List<T> parseList(
            JsonNode featureNode, BiFunction<JsonNode, String, T> mapper, String featureId) {

        JsonNode arrayNode = getFeature(featureNode, featureId);
        if (arrayNode == null || !arrayNode.isArray()) return emptyList();

        ArrayList<T> list = new ArrayList<>(arrayNode.size());
        for (JsonNode node : arrayNode) {
            if (node.isNull()) continue;

            T value = mapper.apply(node, featureId);
            if (value != null) list.add(value);
        }

        return list.isEmpty() ? emptyList() : list;
    }

    /**
     * A universal helper method for parsing a JSON object into a map with custom typed keys and values.
     * <p>
     * This is useful when the JSON keys represent specific objects (e.g., Bcp47LanguageTag) rather than just Strings.
     *
     * @param <K>         the type of keys in the resulting map
     * @param <V>         the type of values in the resulting map
     * @param featureNode the JSON node expected to be an object
     * @param keyMapper   a function that converts the JSON field name (String) into a key of type K
     * @param valueMapper a function that converts the JSON node value into a value of type V
     * @return a {@link Map} of parsed key-value pairs, or an empty map if empty or invalid
     */
    default <K, V> Map<K, V> parseMap(
            JsonNode featureNode,
            Function<String, K> keyMapper,
            Function<JsonNode, V> valueMapper,
            String featureId) {
        JsonNode objectNode = getFeature(featureNode, featureId);

        if (objectNode != null && objectNode.isObject()) {
            Map<K, V> map = new HashMap<>();
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (field.getValue().isNull()) continue;

                K key = keyMapper.apply(field.getKey());
                V value = valueMapper.apply(field.getValue());

                if (key != null && value != null) {
                    map.put(key, value);
                }
            }
            return map;
        }
        return emptyMap();
    }
}
