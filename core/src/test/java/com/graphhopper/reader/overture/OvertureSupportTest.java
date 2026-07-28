package com.graphhopper.reader.overture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.DataReader;
import com.graphhopper.reader.DataReaderConfig;
import com.graphhopper.reader.DataReaderContext;
import com.graphhopper.routing.ev.ImportUnit;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.MaxSpeedCalculator;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.util.PMap;
import java.io.File;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Covers how {@link OvertureSupport} resolves the configured source.
 *
 * <p>The reader supports a local file and S3, and only the configured string distinguishes them. The
 * engine used to resolve it for every reader by calling {@code new File(source)}, which turned {@code
 * s3://bucket/key} into a local path named {@code s3:/bucket/key}; the reader then found no S3
 * settings, fell through to its local-file branch and failed with "Input file does not exist". S3
 * consequently only worked when a caller built the reader by hand, so nothing that went through
 * {@code datareader.file} could use it.
 */
class OvertureSupportTest {

    private EncodingManager encodingManager;
    private BaseGraph graph;

    @BeforeEach
    void setup() {
        encodingManager = OvertureTestFixtures.minimalEncodingManager();
        graph = new BaseGraph.Builder(encodingManager).create();
    }

    @AfterEach
    void tearDown() {
        if (graph != null) graph.close();
    }

    @Test
    @DisplayName("An s3:// source is resolved to a bucket and key, not to a local file")
    void s3SourceReachesS3Settings() {
        OvertureReader reader = readerFor("s3://overture-bucket/2026-08/segments.parquet");

        assertEquals("overture-bucket", reader.getS3Bucket());
        assertEquals("2026-08/segments.parquet", reader.getS3Key());
    }

    @Test
    @DisplayName("A local source is resolved to a file, leaving the S3 settings unset")
    void localSourceReachesTheFile() {
        OvertureReader reader = readerFor("/data/overture/europe.parquet");

        assertEquals(new File("/data/overture/europe.parquet"), reader.getOvertureFile());
        assertNull(reader.getS3Bucket());
        assertNull(reader.getS3Key());
    }

    @Test
    @DisplayName("A programmatic import configures no source at all")
    void noSourceLeavesTheReaderUnconfigured() {
        OvertureReader reader = readerFor(null);

        assertNull(reader.getOvertureFile());
        assertNull(reader.getS3Bucket());
    }

    /**
     * @param source the configured {@code datareader.file}, or {@code null} for none
     * @return the reader the Overture initializer builds for that source
     */
    private OvertureReader readerFor(String source) {
        DataReader reader =
                OvertureSupport.configure(new GraphHopper()).initializeDataReader(contextFor(source));
        return (OvertureReader) reader;
    }

    /** @return a context supplying just what the Overture initializer reads */
    private DataReaderContext contextFor(String source) {
        return new DataReaderContext() {
            @Override
            public BaseGraph getBaseGraph() {
                return graph;
            }

            @Override
            public String getSource() {
                return source;
            }

            @Override
            public File getSourceFile() {
                return source == null ? null : new File(source);
            }

            @Override
            public EncodingManager getEncodingManager() {
                return encodingManager;
            }

            @Override
            public DataReaderConfig getConfig() {
                return new DataReaderConfig();
            }

            @Override
            public Map<String, PMap> getEncodedValuesWithProps() {
                return Map.of();
            }

            @Override
            public Map<String, ImportUnit> getActiveImportUnits() {
                return Map.of();
            }

            @Override
            public Map<String, List<String>> getRestrictionVehicleTypesByProfile() {
                return Map.of();
            }

            @Override
            public String getDateRangeParserString() {
                return "";
            }

            @Override
            public MaxSpeedCalculator getMaxSpeedCalculator() {
                return null;
            }
        };
    }
}
