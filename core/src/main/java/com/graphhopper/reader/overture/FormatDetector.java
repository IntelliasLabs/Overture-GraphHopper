package com.graphhopper.reader.overture;

import java.io.File;
import java.util.Locale;

/**
 * Utility for identifying Overture Maps data formats by file extension.
 * <p>
 * This detector acts as a dispatcher to determine whether to route the file
 * to the GeoJSON parser or the GeoParquet/Avro pipeline.
 * </p>
 */
public final class FormatDetector {

    /**
     * Supported input formats for Overture data.
     */
    public enum DataFormat {
        /** Standard GeoJSON format, typically used for small extracts. */
        GEOJSON,
        /** Apache Parquet or GeoParquet format, used for large-scale datasets. */
        PARQUET,
        /** Fallback for unsupported or missing file extensions. */
        UNKNOWN
    }

    /**
     * Detects data format based on the file extension of a File object.
     *
     * @param file The file to check.
     * @return The detected DataFormat, or UNKNOWN.
     */
    public static DataFormat detectFromFile(File file) {
        if (file == null) {
            return DataFormat.UNKNOWN;
        }
        return detectFromPath(file.getName());
    }

    /**
     * Detects data format based on the file path or name string.
     * Checks for extensions: .geojson, .parquet, .geoparquet (case-insensitive).
     *
     * @param path The file path or name.
     * @return The detected DataFormat, or UNKNOWN.
     */
    public static DataFormat detectFromPath(String path) {
        if (path == null || path.isEmpty()) {
            return DataFormat.UNKNOWN;
        }

        String lowerCasePath = path.toLowerCase(Locale.ROOT);

        if (lowerCasePath.endsWith(".geojson")) {
            return DataFormat.GEOJSON;
        } else if (lowerCasePath.endsWith(".parquet") || lowerCasePath.endsWith(".geoparquet")) {
            return DataFormat.PARQUET;
        }

        return DataFormat.UNKNOWN;
    }
}
