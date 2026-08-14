package com.graphhopper.reader.overture.parser.parquet;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.apache.parquet.io.InputFile;
import org.apache.parquet.io.SeekableInputStream;

/**
 * Reads a local Parquet file for the Parquet reader, without going through Hadoop.
 *
 * <p>Replaces {@code HadoopInputFile.fromPath}, which cannot be used on Java 24 or later.
 * {@code HadoopInputFile.fromPath} resolves a {@code FileSystem}, which calls
 * {@code UserGroupInformation.getCurrentUser}, which calls {@code Subject.getSubject} - and JEP 486
 * permanently disabled the Security Manager in JDK 24, so that method now always throws
 * {@link UnsupportedOperationException}. Setting {@code -Djava.security.manager=allow} does not help.
 * The effect was that every local Parquet import failed outright on Java 25.
 *
 * <p>Nothing about reading a file off local disk needs Hadoop, and the Parquet reader only asks an
 * {@link InputFile} for a length and a seekable stream. The S3 path already supplied its own
 * implementation for the same reason, so this simply gives the local path the same treatment.
 */
public class LocalParquetInputFile implements InputFile {

    private final Path path;
    private final long length;

    /**
     * @param file the local Parquet file
     * @throws IOException if the file cannot be read or its size cannot be determined
     */
    public LocalParquetInputFile(File file) throws IOException {
        this(file.toPath());
    }

    /**
     * @param path the local Parquet file
     * @throws IOException if the file cannot be read or its size cannot be determined
     */
    public LocalParquetInputFile(Path path) throws IOException {
        this.path = path;
        this.length = Files.size(path);
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public SeekableInputStream newStream() throws IOException {
        return new LocalSeekableInputStream(FileChannel.open(path, StandardOpenOption.READ));
    }

    @Override
    public String toString() {
        return path.toString();
    }

    /** A {@link SeekableInputStream} over a {@link FileChannel}. */
    private static final class LocalSeekableInputStream extends SeekableInputStream {

        private final FileChannel channel;

        private LocalSeekableInputStream(FileChannel channel) {
            this.channel = channel;
        }

        @Override
        public long getPos() throws IOException {
            return channel.position();
        }

        @Override
        public void seek(long newPos) throws IOException {
            channel.position(newPos);
        }

        @Override
        public int read() throws IOException {
            ByteBuffer one = ByteBuffer.allocate(1);
            return channel.read(one) < 0 ? -1 : (one.array()[0] & 0xFF);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return channel.read(ByteBuffer.wrap(b, off, len));
        }

        @Override
        public int read(ByteBuffer buf) throws IOException {
            return channel.read(buf);
        }

        /**
         * Parquet relies on this filling the buffer completely; a short read means the file is
         * truncated, and continuing would surface as an unrelated parse error much later.
         */
        @Override
        public void readFully(byte[] bytes) throws IOException {
            readFully(bytes, 0, bytes.length);
        }

        @Override
        public void readFully(byte[] bytes, int start, int len) throws IOException {
            readFully(ByteBuffer.wrap(bytes, start, len));
        }

        @Override
        public void readFully(ByteBuffer buf) throws IOException {
            while (buf.hasRemaining()) {
                if (channel.read(buf) < 0) {
                    throw new EOFException(
                            "Reached end of file with " + buf.remaining() + " byte(s) still expected");
                }
            }
        }

        @Override
        public void close() throws IOException {
            channel.close();
        }
    }
}
