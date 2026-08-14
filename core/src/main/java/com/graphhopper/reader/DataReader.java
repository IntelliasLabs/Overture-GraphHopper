package com.graphhopper.reader;

import com.graphhopper.reader.dem.ElevationProvider;
import com.graphhopper.routing.util.AreaIndex;
import com.graphhopper.routing.util.CustomArea;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Common interface for all GraphHopper data readers. Implementations convert a specific
 * map data source into GraphHopper's graph storage by configuring optional components and
 * executing the {@link #readGraph()} method.
 */
public interface DataReader {

    /**
     * Sets the data file that should be parsed.
     *
     * @param file the data file
     * @return this reader for chaining
     */
    DataReader setFile(File file);

    /**
     * Parses the configured data file and populates the graph storage.
     *
     * @throws IOException if the file cannot be read or parsed
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
