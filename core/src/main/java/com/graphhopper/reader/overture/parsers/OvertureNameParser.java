package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.names.Bcp47LanguageTag;
import com.graphhopper.reader.overture.names.OvertureNameRule;
import com.graphhopper.reader.overture.road.segment.OvertureRoadSegment;
import com.graphhopper.search.KVStorage;
import com.graphhopper.util.EdgeIteratorState;
import java.util.List;
import java.util.Map;

/**
 * Parser for extracting names from Overture road segments.
 */
public final class OvertureNameParser implements OvertureTagParser {

    private final String preferredLanguage;

    /**
     * @param preferredLanguage the configured {@code datareader.preferred_language}, or empty for none
     */
    public OvertureNameParser(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    /** @return the language this parser prefers, for tests and diagnostics */
    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    /** Parses the primary name of the given Overture road segment.
     *
     * @param segment The Overture road segment to parse.
     * @return The primary name of the road segment.
     */
    public static String parsePrimaryName(OvertureRoadSegment segment) {
        return segment.getPrimaryName();
    }

    /** Parses the common names of the given Overture road segment.
     *
     * @param segment The Overture road segment to parse.
     * @return A map of language tags to common names for the road segment.
     */
    public static Map<Bcp47LanguageTag, String> parseCommonNames(OvertureRoadSegment segment) {
        return segment.getCommonNames();
    }

    /** Parses the name rules of the given Overture road segment.
     *
     * @param segment The Overture road segment to parse.
     * @return A list of name rules for the road segment.
     */
    public static List<OvertureNameRule> parseNameRules(OvertureRoadSegment segment) {
        return segment.getNameRules();
    }

    /**
     * Parses the name of the given Overture road segment and applies it to the edge.
     *
     * <p>Overture carries translations in {@code names.common}, keyed by BCP-47 tag. When a preferred
     * language is configured this is the direct analogue of the OSM reader preferring the {@code
     * name:<lang>} tag over {@code name}: a common name in that language wins over the primary name,
     * and otherwise the primary name is used unchanged. Only the language subtag is compared, so
     * {@code en} matches {@code en-GB}.
     *
     * @param edge the graph edge to update
     * @param segment the Overture road segment to parse
     * @param context unused; names come entirely from the segment
     */
    @Override
    public void handleSegment(
            EdgeIteratorState edge, OvertureRoadSegment segment, OvertureSegmentContext context) {
        String name = nameIn(segment, preferredLanguage);
        int maxLength = 100;
        if (name != null && !name.isBlank()) {
            String resultName;
            if (name.length() > maxLength) {
                int index = name.indexOf(":");
                if (index > 0) {
                    resultName = name.substring(0, index);
                } else {
                    resultName = name.substring(0, maxLength);
                }
            } else {
                resultName = name;
            }
            edge.setKeyValues(Map.of("street_name", new KVStorage.KValue(resultName)));
        }
    }

    /**
     * Picks the name to store, preferring a common name in the requested language.
     *
     * @return the chosen name, possibly empty but never null
     */
    private static String nameIn(OvertureRoadSegment segment, String preferredLanguage) {
        if (preferredLanguage == null || preferredLanguage.isEmpty()) return parsePrimaryName(segment);

        for (Map.Entry<Bcp47LanguageTag, String> entry : parseCommonNames(segment).entrySet()) {
            Bcp47LanguageTag tag = entry.getKey();
            if (tag != null
                    && tag.getLanguage() != null
                    && tag.getLanguage().equalsIgnoreCase(preferredLanguage)
                    && entry.getValue() != null
                    && !entry.getValue().isBlank()) {
                return entry.getValue();
            }
        }
        return parsePrimaryName(segment);
    }
}
