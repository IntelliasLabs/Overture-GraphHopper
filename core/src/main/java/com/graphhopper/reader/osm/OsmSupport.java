package com.graphhopper.reader.osm;

import com.graphhopper.reader.DataReader;
import com.graphhopper.reader.DataReaderContext;
import com.graphhopper.reader.DataReaderInitializer;

/**
 * Entry point for importing OSM data, the counterpart to {@code OvertureSupport}.
 *
 * <p>Exists so callers can name a source rather than assemble one: {@code OsmSupport::create} is a
 * {@link DataReaderInitializer}, which keeps it usable as a shared constant.
 */
public final class OsmSupport {

    private OsmSupport() {}

    /**
     * The extensions {@code OSMInput} accepts. It recognises gzip, PBF and zip by their magic bytes
     * and falls back to the name only for {@code .osm}, {@code .xml}, {@code .bz2} and {@code
     * .bzip2}, so an extract may legitimately be named {@code .osm.gz} or {@code .osm.zip}.
     */
    private static final String[] EXTENSIONS = {
        ".pbf", ".osm", ".xml", ".bz2", ".bzip2", ".gz", ".zip"
    };

    /**
     * @param dataReaderFile the configured {@code datareader.file}
     * @return whether this is an OSM source
     */
    public static boolean handles(String dataReaderFile) {
        if (dataReaderFile == null) return false;
        for (String extension : EXTENSIONS) {
            if (dataReaderFile.endsWith(extension)) return true;
        }
        return false;
    }

    /**
     * Builds an {@link OSMReader} from the import context, assembling the OSM tag parsers it needs.
     *
     * <p>Those parsers used to be built by the engine for every import regardless of source. Building
     * them here means they exist only when OSM data is actually being read.
     *
     * @param context the import context, which also supplies the file to read
     * @return a reader for OSM data, ready to run
     */
    public static DataReader create(DataReaderContext context) {
        OSMReader reader = new OSMReader(
                context.getBaseGraph(), OsmParsersFactory.create(context), context.getConfig());
        // OSM is always a local file, so the resolved File is all this reader needs. It stays optional
        // because an import may be driven entirely programmatically, with the file set by the caller.
        if (context.getSourceFile() != null) reader.setFile(context.getSourceFile());
        return reader;
    }
}
