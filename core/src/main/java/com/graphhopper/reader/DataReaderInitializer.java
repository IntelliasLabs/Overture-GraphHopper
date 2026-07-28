package com.graphhopper.reader;

/**
 * Common functional interface for initializing all data readers,
 * in class {@link com.graphhopper.GraphHopper}.
 */
@FunctionalInterface
public interface DataReaderInitializer {
    /**
     * Returns an initialized instance of {@link DataReader} for the given import.
     *
     * <p>The context carries only what every source needs. A reader requiring more narrows it to its
     * own sub-interface — see {@code OsmSupport#create} — so that no source's types appear in this
     * signature. It previously took {@code OSMParsers} and {@code OSMReaderConfig}, which readers of
     * other sources were handed and had to ignore.
     *
     * @param context the graph and encoded values for this import
     * @return initialized {@link DataReader} instance
     */
    DataReader initializeDataReader(DataReaderContext context);
}
