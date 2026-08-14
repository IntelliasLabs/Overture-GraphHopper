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
public final class OvertureNameParser {

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

    /** Parses the primary name of the given Overture road segment
     * and applies it to the edge.
     *
     * @param edge    The graph edge to update.
     * @param segment The Overture road segment to parse.
     */
    public static void parseName(EdgeIteratorState edge, OvertureRoadSegment segment) {
        String name = parsePrimaryName(segment);
        if (name != null && !name.isBlank()) {
            edge.setKeyValues(Map.of("street_name", new KVStorage.KValue(name)));
        }
    }
}
