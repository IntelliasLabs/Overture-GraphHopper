package com.graphhopper.reader;

import com.graphhopper.reader.dem.ElevationProvider;
import com.graphhopper.routing.util.AreaIndex;
import com.graphhopper.routing.util.CustomArea;

import java.io.IOException;
import java.util.Date;

/**
 * Common interface for all GraphHopper data readers. Implementations convert a specific map data
 * source into GraphHopper's graph storage by configuring optional components and executing the {@link
 * #readGraph()} method.
 *
 * <p>What is deliberately not here is how a reader is pointed at its data. That is settled when the
 * reader is built, by the {@link DataReaderInitializer} that knows which source it is dealing with.
 */
public interface DataReader {

    /**
     * Parses the configured source and populates the graph storage.
     *
     * <p>How the source is configured is each reader's own business, and deliberately not part of this
     * interface: a reader is handed the configured {@code datareader.file} through {@link
     * DataReaderContext} when it is built, and decides what that string means. Requiring a {@code
     * setFile(File)} here would assert that every source is a local file, which is not true - Overture
     * also reads {@code s3://bucket/key}, which has no {@link java.io.File} form.
     *
     * @throws IOException if the source cannot be read or parsed
     */
    void readGraph() throws IOException;

    /**
     * Assigns the elevation provider used during import.
     *
     * @param provider the elevation provider
     * @return this reader for chaining
     */
    DataReader setElevationProvider(ElevationProvider provider);

    /**
     * Sets the optional area index which can be used for area-based features.
     *
     * @param areaIndex the area index
     * @return this reader for chaining
     */
    DataReader setAreaIndex(AreaIndex<CustomArea> areaIndex);


    /**
     * Returns the data date embedded in the source if available.
     *
     * @return the data date or {@code null} if unknown
     */
    Date getDataDate();
}
