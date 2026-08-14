package com.graphhopper.reader;

import com.graphhopper.routing.OSMReaderConfig;
import com.graphhopper.routing.util.OSMParsers;
import com.graphhopper.storage.BaseGraph;

/**
 * Common functional interface for initializing all data readers,
 * in class {@link com.graphhopper.GraphHopper}.
 */
@FunctionalInterface
public interface DataReaderInitializer {
    /**
     * Returns an initialized instance of {@link DataReader} using
     * the provided values.
     *
     * @param baseGraph graph
     * @param osmParsers parsers
     * @param config config
     * @return initialized {@link DataReader} instance
     */
    DataReader initializeDataReader(BaseGraph baseGraph, OSMParsers osmParsers, OSMReaderConfig config);
}
