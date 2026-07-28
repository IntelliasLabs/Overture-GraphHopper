package com.graphhopper.reader;

import java.util.ArrayList;
import java.util.List;

/**
 * Import settings that apply to any data source.
 *
 * <p>These were previously only reachable as {@code OSMReaderConfig}, which made them look
 * OSM-specific when almost none of them are: geometry simplification, elevation smoothing, worker
 * threads and way-name parsing are all questions any reader has to answer. Naming them after one
 * source meant the Overture reader silently honoured none of them.
 *
 * <p>Setters return {@code DataReaderConfig} for chaining. {@code OSMReaderConfig} remains as a
 * subclass so existing code and configuration continue to work unchanged.
 */
public class DataReaderConfig {

    private List<String> ignoredHighways = new ArrayList<>();
    private boolean parseWayNames = true;
    private String preferredLanguage = "";
    private double maxWayPointDistance = 0.5;
    private double elevationMaxWayPointDistance = Double.MAX_VALUE;
    private String smoothElevation = "";

    private double smoothElevationAverageWindowSize = 150.0;
    private int ramerElevationSmoothingMax = 5;
    private double longEdgeSamplingDistance = Double.MAX_VALUE;
    private int workerThreads = 2;
    private double defaultElevation = 0;

    public List<String> getIgnoredHighways() {
        return ignoredHighways;
    }

    /**
     * Sets the road classes that shall be ignored when reading the source. This can be used to speed up
     * the import and reduce the size of the resulting routing graph. For example if one is only interested in routing
     * for motorized vehicles the routing graph size can be reduced by excluding footways, cycleways, paths and/or
     * tracks. This can be quite significant depending on your area. Not only are there fewer ways to be processed, but
     * there are also fewer junctions, which means fewer nodes and edges. Another reason to exclude footways etc. for
     * motorized vehicle routing could be preventing undesired u-turns (#1858). Similarly, one could exclude motorway,
     * trunk or even primary highways for bicycle or pedestrian routing.
     */
    public DataReaderConfig setIgnoredHighways(List<String> ignoredHighways) {
        this.ignoredHighways = ignoredHighways;
        return this;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    /**
     * Sets the language used to parse way names. For example if this is set to 'en' we will use the 'name:en' tag
     * rather than the 'name' tag if it is present. The language code should be given as defined in ISO 639-1 or ISO 639-2.
     * This setting becomes irrelevant if parseWayNames is set to false.
     */
    public DataReaderConfig setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
        return this;
    }

    public boolean isParseWayNames() {
        return parseWayNames;
    }

    /**
     * Enables/disables the parsing of the name and ref tags to set the name of the graph edges
     */
    public DataReaderConfig setParseWayNames(boolean parseWayNames) {
        this.parseWayNames = parseWayNames;
        return this;
    }

    public double getMaxWayPointDistance() {
        return maxWayPointDistance;
    }

    /**
     * This parameter affects the routine used to simplify the edge geometries (Ramer-Douglas-Peucker). Higher values mean
     * more details are preserved. The default is 1 (meter). Simplification can be disabled by setting it to 0.
     */
    public DataReaderConfig setMaxWayPointDistance(double maxWayPointDistance) {
        this.maxWayPointDistance = maxWayPointDistance;
        return this;
    }

    public double getElevationMaxWayPointDistance() {
        return elevationMaxWayPointDistance;
    }

    /**
     * Sets the max elevation discrepancy between way points and the simplified polyline in meters
     */
    public DataReaderConfig setElevationMaxWayPointDistance(double elevationMaxWayPointDistance) {
        this.elevationMaxWayPointDistance = elevationMaxWayPointDistance;
        return this;
    }

    public String getElevationSmoothing() {
        return smoothElevation;
    }

    /**
     * Enables/disables elevation smoothing
     */
    public DataReaderConfig setElevationSmoothing(String smoothElevation) {
        this.smoothElevation = smoothElevation;
        return this;
    }

    public int getElevationSmoothingRamerMax() {
        return ramerElevationSmoothingMax;
    }

    public DataReaderConfig setElevationSmoothingRamerMax(int max) {
        this.ramerElevationSmoothingMax = max;
        return this;
    }

    public double getSmoothElevationAverageWindowSize() {
        return smoothElevationAverageWindowSize;
    }

    public void setSmoothElevationAverageWindowSize(double smoothElevationAverageWindowSize) {
        this.smoothElevationAverageWindowSize = smoothElevationAverageWindowSize;
    }

    public double getLongEdgeSamplingDistance() {
        return longEdgeSamplingDistance;
    }

    /**
     * Sets the distance between elevation samples on long edges
     */
    public DataReaderConfig setLongEdgeSamplingDistance(double longEdgeSamplingDistance) {
        this.longEdgeSamplingDistance = longEdgeSamplingDistance;
        return this;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    /**
     * Sets the number of threads used for the import
     */
    public DataReaderConfig setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
        return this;
    }

    public double getDefaultElevation() {
        return defaultElevation;
    }

    /**
     * Sets the elevation in meters that shall be used if the elevation data source is missing a value
     */
    public DataReaderConfig setDefaultElevation(double defaultElevation) {
        this.defaultElevation = defaultElevation;
        return this;
    }
}
