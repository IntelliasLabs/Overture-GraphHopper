package com.graphhopper.reader.overture;

import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FormatDetectorTest {

    @Test
    void testDetectGeoJson() {
        assertEquals(FormatDetector.DataFormat.GEOJSON, FormatDetector.detectFromPath("data.geojson"));
        assertEquals(FormatDetector.DataFormat.GEOJSON, FormatDetector.detectFromFile(new File("/tmp/test_data.geojson")));
    }

    @Test
    void testDetectParquet() {
        assertEquals(FormatDetector.DataFormat.PARQUET, FormatDetector.detectFromPath("data.parquet"));
        assertEquals(FormatDetector.DataFormat.PARQUET, FormatDetector.detectFromFile(new File("archive.parquet")));
    }

    @Test
    void testDetectGeoParquet() {
        assertEquals(FormatDetector.DataFormat.PARQUET, FormatDetector.detectFromPath("buildings.geoparquet"));
    }

    @Test
    void testCaseInsensitivity() {
        assertEquals(FormatDetector.DataFormat.GEOJSON, FormatDetector.detectFromPath("DATA.GEOJSON"));
        assertEquals(FormatDetector.DataFormat.PARQUET, FormatDetector.detectFromPath("DATA.PARQUET"));
        assertEquals(FormatDetector.DataFormat.PARQUET, FormatDetector.detectFromPath("Data.GeoParquet"));
    }

    @Test
    void testUnknownFormat() {
        assertEquals(FormatDetector.DataFormat.UNKNOWN, FormatDetector.detectFromPath("image.png"));
        assertEquals(FormatDetector.DataFormat.UNKNOWN, FormatDetector.detectFromPath("readme.txt"));
        assertEquals(FormatDetector.DataFormat.UNKNOWN, FormatDetector.detectFromPath("data"));
    }

    @Test
    void testNullAndEmptyInputs() {
        assertEquals(FormatDetector.DataFormat.UNKNOWN, FormatDetector.detectFromPath(null));
        assertEquals(FormatDetector.DataFormat.UNKNOWN, FormatDetector.detectFromPath(""));
        assertEquals(FormatDetector.DataFormat.UNKNOWN, FormatDetector.detectFromFile(null));
    }
}
