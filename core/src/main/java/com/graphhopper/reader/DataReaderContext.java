package com.graphhopper.reader;

import com.graphhopper.routing.ev.ImportUnit;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.MaxSpeedCalculator;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.util.PMap;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Everything a data reader is given when the import pipeline builds it.
 *
 * <p>These are the source-agnostic <em>inputs</em> to an import: the graph to fill, the encoded values
 * that were created, the settings that apply to any source, and the declarations a reader needs if it
 * assembles its own parser pipeline. What is deliberately absent is any source-specific type — the
 * engine no longer builds one source's parsers on every source's behalf, so nothing here mentions OSM
 * or Overture.
 *
 * @see com.graphhopper.reader.osm.OsmParsersFactory
 */
public interface DataReaderContext {

    /** @return the graph to write the imported network into */
    BaseGraph getBaseGraph();

    /**
     * The configured {@code datareader.file}, verbatim.
     *
     * <p>Deliberately a string rather than a {@link File}: not every source is one. Overture accepts
     * {@code s3://bucket/key}, which has no meaningful {@code File} form, so a reader that supports
     * more than local files has to see what was actually configured in order to dispatch on it.
     *
     * @return the configured source, or {@code null} when the import is driven purely programmatically
     */
    String getSource();

    /**
     * The same source resolved to a local file.
     *
     * <p>Resolution goes through {@code GraphHopper#_getDataFile()}, so a subclass that overrides it -
     * to load a fixture off the classpath, say - keeps working.
     *
     * @return the source as a local file, or {@code null} when no source was configured
     */
    File getSourceFile();

    /** @return the encoded values this import created */
    EncodingManager getEncodingManager();

    /** @return import settings that apply to any source, such as geometry simplification */
    DataReaderConfig getConfig();

    /**
     * @return per-encoded-value options parsed from {@code graph.encoded_values}, keyed by encoded-value
     *     name. Needed by readers that build their own parsers, so those parsers see the same options
     *     the encoded values were created with.
     */
    Map<String, PMap> getEncodedValuesWithProps();

    /**
     * @return the transitive closure of import units for this import, which is what determines both
     *     which parsers are needed and the order they must run in
     */
    Map<String, ImportUnit> getActiveImportUnits();

    /**
     * @return turn-restriction vehicle types per profile, for sources that can express turn
     *     restrictions. Empty for profiles without turn costs.
     */
    Map<String, List<String>> getRestrictionVehicleTypesByProfile();

    /** @return the configured {@code datareader.date_range_parser_day}, for conditional access parsing */
    String getDateRangeParserString();

    /**
     * @return the legal-default-speed calculator, or {@code null} when {@code max_speed_calculator} is
     *     not enabled. A reader that fills {@code max_speed} itself can ignore it.
     */
    MaxSpeedCalculator getMaxSpeedCalculator();
}
