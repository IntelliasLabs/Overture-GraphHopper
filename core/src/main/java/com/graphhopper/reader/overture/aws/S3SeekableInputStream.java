package com.graphhopper.reader.overture.aws;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.apache.parquet.io.SeekableInputStream;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * A specialized {@link SeekableInputStream} that reads data directly from an AWS S3 object.
 * <p>
 * This class enables random access to large S3 files (like Parquet) without downloading
 * the entire file. It achieves this by translating read requests into HTTP {@code Range}
 * requests via the {@link OvertureS3Client}.
 * </p>
 * <p>
 * <strong>Performance Note:</strong> The {@link #seek(long)} operation is in-memory only.
 * However, every call to {@link #read(byte[], int, int)} initiates a new network connection to S3.
 * Therefore, it is designed for reading contiguous chunks (like Parquet Row Groups) rather than
 * frequent, tiny reads.
 * </p>
 */
public class S3SeekableInputStream extends SeekableInputStream {
    private final S3Client client;
    private final String bucket;
    private final String key;
    private final long contentLength;
    private long position = 0;

    private static final int EOF_VALUE = -1;

    /**
     * Creates a new stream for the specified S3 object.
     *
     * @param client        the authenticated S3 client.
     * @param bucket        the S3 bucket name.
     * @param key           the S3 object key.
     * @param contentLength the total size of the file (must be obtained beforehand via HEAD request).
     */
    public S3SeekableInputStream(S3Client client, String bucket, String key, long contentLength) {
        this.client = client;
        this.bucket = bucket;
        this.key = key;
        this.contentLength = contentLength;
    }

    /**
     * Returns the current position in the stream.
     *
     * @return the offset from the beginning of the file, in bytes.
     */
    @Override
    public long getPos() {
        return position;
    }

    /**
     * Moves the current position to the specified offset.
     * <p>
     * This implementation only updates the internal position counter and does not trigger any network I/O until.
     * </p>
     *
     * @param newPos the new position, in bytes.
     */
    @Override
    public void seek(long newPos) {
        this.position = newPos;
    }

    /**
     * Reads the next byte of data from the input stream.
     *
     * @return the next byte of data, or -1 if the end of the file is reached.
     * @throws IOException if a network error occurs.
     */
    @Override
    public int read() throws IOException {
        byte[] b = new byte[1];
        int read = read(b, 0, 1);
        return read == EOF_VALUE ? EOF_VALUE : (b[0] & 0xFF);
    }

    /**
     * Reads bytes into the provided array until it is full.
     *
     * @param bytes the buffer into which the data is read.
     * @throws EOFException if the end of the stream is reached before the buffer is full.
     * @throws IOException  if an I/O error occurs.
     */
    @Override
    public void readFully(byte[] bytes) throws IOException {
        readFully(bytes, 0, bytes.length);
    }

    /**
     * Reads a specific number of bytes into the array.
     *
     * @param bytes the buffer into which the data is read.
     * @param start the start offset in the array.
     * @param len   the number of bytes to read.
     * @throws EOFException if the end of the stream is reached before reading {@code len} bytes.
     * @throws IOException  if an I/O error occurs.
     */
    @Override
    public void readFully(byte[] bytes, int start, int len) throws IOException {
        int read = read(bytes, start, len);
        if (read < len) throw new EOFException("Reached end of stream in S3");
    }

    /**
     * Reads bytes into a {@link ByteBuffer}.
     * <p>
     * Optimized to use the backing array directly if available. For DirectByteBuffers,
     * a temporary intermediate buffer is used.
     * </p>
     *
     * @param byteBuffer the buffer to read data into.
     * @return the total number of bytes read, or -1 if there is no more data.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int read(ByteBuffer byteBuffer) throws IOException {
        if (byteBuffer.hasArray()) {
            int read = read(
                    byteBuffer.array(),
                    byteBuffer.arrayOffset() + byteBuffer.position(),
                    byteBuffer.remaining());
            if (read > 0) byteBuffer.position(byteBuffer.position() + read);
            return read;
        }

        byte[] temp = new byte[byteBuffer.remaining()];
        int read = read(temp, 0, temp.length);
        if (read > 0) byteBuffer.put(temp, 0, read);
        return read;
    }

    /**
     * Reads bytes fully into a {@link ByteBuffer}.
     *
     * @param byteBuffer the buffer to fill.
     * @throws EOFException if the stream ends before the buffer is full.
     * @throws IOException  if an I/O error occurs.
     */
    @Override
    public void readFully(ByteBuffer byteBuffer) throws IOException {
        if (byteBuffer.hasArray()) {
            readFully(
                    byteBuffer.array(),
                    byteBuffer.arrayOffset() + byteBuffer.position(),
                    byteBuffer.remaining());
            byteBuffer.position(byteBuffer.limit());
            return;
        }
        byte[] temp = new byte[byteBuffer.remaining()];
        readFully(temp, 0, temp.length);
        byteBuffer.put(temp);
    }

    /**
     * The core read implementation.
     * <p>
     * This method calculates the byte range based on the current {@code position} and
     * the requested {@code len}, executes an S3 GetObject request with the {@code Range} header,
     * and fills the buffer.
     * </p>
     *
     * @param b   the buffer into which the data is read.
     * @param off the start offset in array {@code b}.
     * @param len the maximum number of bytes to read.
     * @return the total number of bytes read into the buffer, or -1 if there is no more data.
     * @throws IOException if the S3 request fails.
     */
    @Override
    public int read(@NotNull byte[] b, int off, int len) throws IOException {
        if (len == 0) return 0;
        if (position >= contentLength) return EOF_VALUE;

        long endRange = Math.min(position + len - 1, contentLength - 1);

        try (InputStream stream =
                OvertureS3Client.openRangeStream(client, bucket, key, position, endRange)) {
            int totalRead = 0;
            int n;
            while (totalRead < len
                    && (n = stream.read(b, off + totalRead, len - totalRead)) != EOF_VALUE) {
                totalRead += n;
            }
            if (totalRead > 0) position += totalRead;
            return totalRead == 0 ? EOF_VALUE : totalRead;
        } catch (Exception e) {
            throw new IOException("S3 read error at pos " + position, e);
        }
    }
}
