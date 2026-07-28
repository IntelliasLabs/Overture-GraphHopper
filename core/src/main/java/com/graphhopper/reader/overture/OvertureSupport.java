package com.graphhopper.reader.overture;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.DataReader;
import com.graphhopper.reader.DataReaderContext;
import com.graphhopper.reader.DataReaderInitializer;
import com.graphhopper.reader.overture.parsers.DefaultOvertureImportRegistry;
import com.graphhopper.reader.overture.parsers.OvertureImportRegistry;
import com.graphhopper.reader.overture.parsers.OvertureParsers;

/**
 * Entry point for importing Overture data with GraphHopper.
 *
 * <p>Everything source-specific about an Overture import lives here, so callers only have to decide
 * <em>which</em> source they have, not what that source needs. Without this, the choice leaks into
 * whoever wires up {@link GraphHopper} and reads as an unexplained special case: an import registry
 * set for one source and not the other.
 */
public final class OvertureSupport {

    /** Extensions and schemes that identify an Overture source. */
    private static final String S3_SCHEME = "s3://";

    private OvertureSupport() {}

    /**
     * @param dataReaderFile the configured {@code datareader.file}
     * @return whether this is an Overture source
     */
    public static boolean handles(String dataReaderFile) {
        if (dataReaderFile == null) return false;
        return dataReaderFile.startsWith(S3_SCHEME)
                || dataReaderFile.endsWith(".parquet")
                || dataReaderFile.endsWith(".geojson");
    }

    /**
     * Configures {@code graphHopper} for an Overture import and returns its reader initializer.
     *
     * <p>Must be called before {@link GraphHopper#init} so the import registry is in place when the
     * encoded values are built.
     *
     * <p>Two things happen here. The import registry is swapped for the Overture one, which creates
     * encoded values exactly as the OSM pipeline does but declares Overture's own dependency ordering
     * and supplies no OSM tag parsers. That is an optimisation rather than a correctness requirement —
     * the reader ignores the OSM parser list regardless, and parser ordering comes from the registry
     * handed to {@link OvertureParsers#build} — but it avoids building a parser set that would never
     * run, and keeps encoded values that only OSM can fill out of the graph.
     *
     * <p>The returned initializer then assembles the parser pipeline from the encoded values that were
     * actually created, which is also when anything Overture cannot fill gets reported.
     *
     * @param graphHopper the instance to configure
     * @return an initializer that builds an {@link OvertureReader}
     */
    public static DataReaderInitializer configure(GraphHopper graphHopper) {
        OvertureImportRegistry registry = new DefaultOvertureImportRegistry();
        graphHopper.setImportRegistry(registry);

        // The encoding manager comes from the context rather than the engine, because it does not exist
        // until prepareImport has run - which is after this method returns.
        //
        // The config goes to both halves because both act on it: the parsers for way-name settings, the
        // reader for geometry simplification and elevation handling.
        return context -> applySource(
                new OvertureReader(
                        context.getBaseGraph(),
                        OvertureParsers.build(registry, context.getEncodingManager(), context.getConfig()),
                        context.getConfig()),
                context);
    }

    /**
     * Points {@code reader} at the configured source.
     *
     * <p>Overture reads from a local file or from S3, and only the configured string says which. The
     * engine cannot make that call - it would have to turn {@code s3://bucket/key} into a {@link
     * java.io.File}, which is how a configured S3 source used to end up being opened as a local path
     * named {@code s3:/bucket/key} and failing with "Input file does not exist". S3 consequently only
     * ever worked when a caller built the reader by hand.
     *
     * <p>Only the source is resolved here. The reader creates a default client when it needs one, so a
     * caller that wants a custom endpoint - as the MinIO tests do - supplies its own initializer and
     * calls {@code setS3Client}, and nothing contacts AWS just because an initializer was built.
     *
     * @param reader the reader to configure
     * @param context supplies the configured source
     * @return {@code reader}, for chaining
     */
    private static DataReader applySource(OvertureReader reader, DataReaderContext context) {
        String source = context.getSource();
        if (source != null && source.startsWith(S3_SCHEME)) {
            reader.setS3Source(source);
        } else if (context.getSourceFile() != null) {
            reader.setFile(context.getSourceFile());
        }
        return reader;
    }
}
