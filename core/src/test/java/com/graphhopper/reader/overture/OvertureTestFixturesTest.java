package com.graphhopper.reader.overture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Guards {@link OvertureTestFixtures#CONFIG_ENCODED_VALUES} against drifting away from the config
 * file it mirrors.
 */
class OvertureTestFixturesTest {

    @Test
    @DisplayName("CONFIG_ENCODED_VALUES matches graph.encoded_values in config-overture-osm.yml")
    void configEncodedValuesMatchesTheConfigFile() throws IOException {
        Path config = OvertureTestFixtures.CONFIG_OVERTURE_OSM;
        // config-overture-osm.yml is not tracked in git, so a clean checkout legitimately lacks it.
        // Skip rather than fail: this test guards against drift, it does not require the file.
        assumeTrue(Files.exists(config), "config-overture-osm.yml not present - skipping drift check");

        String fromConfig = readEncodedValues(config)
                .orElseThrow(() -> new AssertionError("no graph.encoded_values key in " + config));

        assertEquals(
                normalize(OvertureTestFixtures.CONFIG_ENCODED_VALUES),
                normalize(fromConfig),
                "OvertureTestFixtures.CONFIG_ENCODED_VALUES has drifted from config-overture-osm.yml. "
                        + "Update the constant to match the config.");
    }

    @Test
    @DisplayName("The declared encoded-value list has no duplicates")
    void declaredEncodedValuesAreUnique() {
        List<String> names = OvertureEncodedValueCoverageTest.declaredEncodedValueNames();
        assertEquals(names.size(), names.stream().distinct().count(), "duplicate entry in " + names);
    }

    @Test
    @DisplayName("Fixture files referenced by the shared helpers exist")
    void fixturesExist() {
        assertTrue(
                OvertureTestFixtures.smallParquetExtract().isFile(),
                "missing " + OvertureTestFixtures.smallParquetExtract());
        assertTrue(
                OvertureTestFixtures.tinyGeoJsonExtract().isFile(),
                "missing " + OvertureTestFixtures.tinyGeoJsonExtract());
    }

    /**
     * Reads the {@code graph.encoded_values} value out of the YAML by hand. Deliberately avoids a
     * YAML parser: the value is a single scalar on one line and this keeps the test dependency-free.
     */
    private static Optional<String> readEncodedValues(Path config) throws IOException {
        String key = "graph.encoded_values:";
        for (String line : Files.readAllLines(config)) {
            String trimmed = line.trim();
            if (trimmed.startsWith("#") || !trimmed.startsWith(key)) continue;
            return Optional.of(trimmed.substring(key.length()));
        }
        return Optional.empty();
    }

    /** Collapses whitespace differences so indentation and line wrapping do not fail the check. */
    private static String normalize(String encodedValues) {
        return encodedValues.replaceAll("\\s+", "").trim();
    }
}
