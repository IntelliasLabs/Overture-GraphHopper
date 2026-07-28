package com.graphhopper.reader.osm;

import com.graphhopper.reader.DataReaderContext;
import com.graphhopper.routing.ev.BikeNetwork;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.FootNetwork;
import com.graphhopper.routing.ev.ImportUnit;
import com.graphhopper.routing.ev.ImportUnitSorter;
import com.graphhopper.routing.ev.MtbNetwork;
import com.graphhopper.routing.ev.RouteNetwork;
import com.graphhopper.routing.ev.TurnRestriction;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.OSMParsers;
import com.graphhopper.routing.util.parsers.OSMBikeNetworkTagParser;
import com.graphhopper.routing.util.parsers.OSMFootNetworkTagParser;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.util.PMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Assembles the OSM tag-parser pipeline.
 *
 * <p>This used to be {@code GraphHopper.buildOSMParsers}, which meant the engine built parsers for one
 * particular source whether or not that source was being imported. It lives here now, next to the
 * reader that consumes it, and takes its inputs from the source-agnostic {@link DataReaderContext}
 * that every reader receives.
 */
public final class OsmParsersFactory {

    private OsmParsersFactory() {}

    /**
     * Builds the OSM parsers for one import.
     *
     * <p>Way-tag parsers run in {@link ImportUnitSorter} order, so a parser reading an encoded value
     * written by another runs after it. Relation and restriction parsers are added separately, because
     * they are driven by profiles and encoded-value presence rather than by import units.
     *
     * @param context the import context supplied by the engine
     * @return the assembled parsers
     */
    public static OSMParsers create(DataReaderContext context) {
        EncodingManager encodingManager = context.getEncodingManager();
        Map<String, ImportUnit> activeImportUnits = context.getActiveImportUnits();

        Map<String, ImportUnit> sortedImportUnits = new LinkedHashMap<>();
        new ImportUnitSorter(activeImportUnits)
                .sort()
                .forEach(name -> sortedImportUnits.put(name, activeImportUnits.get(name)));

        List<TagParser> sortedParsers = new ArrayList<>();
        sortedImportUnits.forEach((name, importUnit) -> {
            BiFunction<EncodedValueLookup, PMap, TagParser> createTagParser =
                    importUnit.getCreateTagParser();
            if (createTagParser != null) {
                PMap pmap = context.getEncodedValuesWithProps().getOrDefault(name, new PMap());
                if (!pmap.has("date_range_parser_day"))
                    pmap.putObject("date_range_parser_day", context.getDateRangeParserString());
                sortedParsers.add(createTagParser.apply(encodingManager, pmap));
            }
        });

        OSMParsers osmParsers = new OSMParsers();
        context.getConfig().getIgnoredHighways().forEach(osmParsers::addIgnoredHighway);
        sortedParsers.forEach(osmParsers::addWayTagParser);

        if (context.getMaxSpeedCalculator() != null) {
            context.getMaxSpeedCalculator().checkEncodedValues(encodingManager);
            osmParsers.addWayTagParser(context.getMaxSpeedCalculator().getParser());
        }

        if (encodingManager.hasEncodedValue(BikeNetwork.KEY))
            osmParsers.addRelationTagParser(relConfig -> new OSMBikeNetworkTagParser(
                    encodingManager.getEnumEncodedValue(BikeNetwork.KEY, RouteNetwork.class),
                    relConfig,
                    "bicycle"));
        if (encodingManager.hasEncodedValue(MtbNetwork.KEY))
            osmParsers.addRelationTagParser(relConfig -> new OSMBikeNetworkTagParser(
                    encodingManager.getEnumEncodedValue(MtbNetwork.KEY, RouteNetwork.class),
                    relConfig,
                    "mtb"));
        if (encodingManager.hasEncodedValue(FootNetwork.KEY))
            osmParsers.addRelationTagParser(relConfig -> new OSMFootNetworkTagParser(
                    encodingManager.getEnumEncodedValue(FootNetwork.KEY, RouteNetwork.class), relConfig));

        context.getRestrictionVehicleTypesByProfile().forEach((profile, restrictionVehicleTypes) -> {
            osmParsers.addRestrictionTagParser(new RestrictionTagParser(
                    restrictionVehicleTypes,
                    encodingManager.getTurnBooleanEncodedValue(TurnRestriction.key(profile))));
        });
        return osmParsers;
    }
}
