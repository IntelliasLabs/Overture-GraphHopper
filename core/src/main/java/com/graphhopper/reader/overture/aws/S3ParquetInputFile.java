package com.graphhopper.reader.overture.aws;

import org.apache.parquet.io.InputFile;
import org.apache.parquet.io.SeekableInputStream;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;

/**
 * An implementation of the Parquet {@link InputFile} interface that reads directly from AWS S3.
 * <p>
 * This class acts as an adapter, allowing standard Parquet readers (like {@code AvroParquetReader})
 * to access S3 objects without downloading the entire file locally. It works in tandem with
 * {@link S3SeekableInputStream} to fetch only specific byte ranges required by the Parquet format
 * (e.g., footers, row groups).
 * </p>
 */
public class S3ParquetInputFile implements InputFile {
    private final S3Client s3Client;
    private final String bucket;
    private final String key;
    private final long length;

    /**
     * Constructs a new S3 input file adapter.
     * <p>
     * During initialization, this constructor performs a {@code HEAD} request to S3 via {@link OvertureS3Client#getObjectSize}.
     * This is required because the Parquet reader needs to know the file size upfront to locate the footer.
     * </p>
     *
     * @param s3Client the authenticated AWS S3 client.
     * @param bucket   the name of the S3 bucket.
     * @param key      the object key (path) within the bucket.
     * @param length   the size of File.
     * @throws IOException if the file size cannot be retrieved (e.g., network error, file not found).
     */
    public S3ParquetInputFile(S3Client s3Client, String bucket, String key, long length){
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.key = key;
        this.length = length;
    }

    /**
     * Used in cases where the file size is not known in advance
     */
    public S3ParquetInputFile(S3Client s3Client, String bucket, String key) throws IOException {
        this(s3Client, bucket, key, OvertureS3Client.getObjectSize(s3Client, bucket, key));
    }

    /**
     * Returns the total length of the file in bytes.
     */
    @Override
    public long getLength() {
        return length;
    }

    /**
     * Opens a new {@link SeekableInputStream} for reading the file.
     * <p>
     * The returned stream supports random access via S3 Range requests.
     * </p>
     *
     * @return a new {@link S3SeekableInputStream} configured for this S3 object.
     */
    @Override
    public SeekableInputStream newStream() {
        return new S3SeekableInputStream(s3Client, bucket, key, length);
    }
}
