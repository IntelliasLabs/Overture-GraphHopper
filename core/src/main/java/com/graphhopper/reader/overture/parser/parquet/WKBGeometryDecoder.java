package com.graphhopper.reader.overture.parser.parquet;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;

/**
 * Utility for decoding Well-Known Binary (WKB) geometry data.
 * <p>
 * Specifically handles the conversion of Overture binary spatial data into JTS {@link LineString} objects.
 * This decoder is a critical component of the Parquet parsing pipeline, ensuring that
 * raw bytes from GeoParquet are correctly transformed for routing graph construction.
 * </p>
 */
public class WKBGeometryDecoder {

    /**
     * Decodes a WKB (Well-Known Binary) byte array into a JTS LineString.
     *
     * @param wkb The WKB byte array.
     * @return The JTS LineString object.
     * @throws IllegalArgumentException if the input is null/empty, parsing fails, or geometry is not a LineString.
     */
    public static LineString decodeLineString(byte[] wkb) {
        if (wkb == null || wkb.length == 0) {
            throw new IllegalArgumentException("Invalid WKB: Input is null or empty.");
        }

        try {
            WKBReader reader = new WKBReader();
            Geometry geometry = reader.read(wkb);

            // Uses pattern matching for instanceof (Java 16+)
            // If geometry is NOT a LineString, we enter the block and throw.
            // If it IS a LineString, the variable 'lineString' is bound and available after the if block.
            if (!(geometry instanceof LineString lineString)) {
                throw new IllegalArgumentException(
                        "Unsupported geometry type: " + geometry.getGeometryType() + ". Expected LineString.");
            }

            return lineString;

        } catch (ParseException e) {
            throw new IllegalArgumentException("Failed to parse WKB: " + e.getMessage(), e);
        }
    }
}
