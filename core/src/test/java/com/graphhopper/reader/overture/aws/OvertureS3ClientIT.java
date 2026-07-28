package com.graphhopper.reader.overture.aws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Integration tests for {@link OvertureS3Client}.
 * <p>
 * This class uses Testcontainers to spin up a local MinIO instance (S3 compatible storage)
 * to verify that the client can correctly stream files from an S3 bucket.
 * <p>
 */
// disabledWithoutDocker: this class needs a MinIO container, and without it the whole build
// failed rather than skipping - it cannot pass on a machine with no Docker daemon.
@Testcontainers(disabledWithoutDocker = true)
public class OvertureS3ClientIT {

    // Define credentials as constants to ensure consistency between Server (MinIO) and Client (Java)
    private static final String ACCESS_KEY = "admin";
    private static final String SECRET_KEY = "password";

    /**
     * Starts a MinIO container via Docker to emulate S3.
     * We explicitly set the root user and password for predictable testing authentication.
     */
    @Container
    private static final MinIOContainer minio = new MinIOContainer("minio/minio")
            .withEnv("MINIO_ROOT_USER", ACCESS_KEY)
            .withEnv("MINIO_ROOT_PASSWORD", SECRET_KEY);

    /**
     * Helper method to create a configured S3Client connected to the MinIO container.
     */
    private S3Client createTestClient() {
        return S3Client.builder()
                .endpointOverride(URI.create(minio.getS3URL()))
                .region(Region.US_EAST_1) // MinIO ignores region, but SDK requires it
                .forcePathStyle(true) // REQUIRED for MinIO/Testcontainers to avoid 400 Bad Request
                .credentialsProvider(StaticCredentialsProvider.create(
                        // Use the explicit constants to avoid "Access Key Id does not exist" errors
                        AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)))
                .build();
    }

    /**
     * Verifies that {@link OvertureS3Client#streamFile} can successfully retrieve
     * an InputStream from an S3-compatible source.
     *
     * <p>This test performs the following steps:
     * <ol>
     * <li>Configures a test S3 client connected to the local MinIO container.</li>
     * <li>Creates a bucket and uploads a dummy file (mimicking a Parquet file header).</li>
     * <li>Calls the method under test to open a stream to that file.</li>
     * <li>Reads from the stream to validate the content matches what was uploaded.</li>
     * </ol>
     *
     * @throws Exception if any S3 or IO error occurs during the test.
     */
    @Test
    public void testStreamFileFromMinIO() throws Exception {
        // 1. Configure the S3 client for the local MinIO instance.
        // NOTE: We must use forcePathStyle(true) because MinIO running in Docker
        // cannot support virtual-hosted-style access (e.g., bucket.localhost) without
        // complex DNS configuration.
        S3Client testClient = createTestClient();

        String bucket = "test-bucket";
        String key = "data/test-file.parquet";

        // Simulate the Magic Bytes of a real Parquet file ("PAR1")
        byte[] fakeParquetContent = "PAR1_some_data_PAR1".getBytes(StandardCharsets.UTF_8);

        // 2. Setup: Create the bucket and upload the fake file
        testClient.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
        testClient.putObject(
                PutObjectRequest.builder().bucket(bucket).key(key).build(),
                RequestBody.fromBytes(fakeParquetContent));

        // 3. Act: Test the streamFile method
        // We pass our manually configured testClient to the method
        try (InputStream stream = OvertureS3Client.streamFile(testClient, bucket, key)) {
            assertNotNull(stream, "Stream should not be null");

            // 4. Assert: Read bytes to verify content
            byte[] buffer = new byte[4];
            int read = stream.read(buffer);

            assertEquals(4, read, "Should have read 4 bytes");
            assertEquals('P', buffer[0]);
            assertEquals('A', buffer[1]);
            assertEquals('R', buffer[2]);
            assertEquals('1', buffer[3]);
        }

        // Cleanup the test client (Container will be cleaned up automatically by @Testcontainers)
        testClient.close();
    }

    @Test
    public void testGetObjectSizeFromMinIO() throws Exception {
        try (S3Client testClient = createTestClient()) {
            String bucket = "test-size-bucket";
            String key = "bigfile.parquet";
            byte[] content = "0123456789".getBytes(StandardCharsets.UTF_8);

            testClient.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            testClient.putObject(
                    PutObjectRequest.builder().bucket(bucket).key(key).build(),
                    RequestBody.fromBytes(content));

            long size = OvertureS3Client.getObjectSize(testClient, bucket, key);
            assertEquals(10L, size, "Content length should be 10 bytes");
        }
    }

    @Test
    public void testOpenRangeStreamFromMinIO() throws Exception {
        try (S3Client testClient = createTestClient()) {
            String bucket = "test-range-bucket";
            String key = "range.txt";
            byte[] content = "0123456789".getBytes(StandardCharsets.UTF_8);

            testClient.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            testClient.putObject(
                    PutObjectRequest.builder().bucket(bucket).key(key).build(),
                    RequestBody.fromBytes(content));

            long start = 2;
            long end = 5;

            try (InputStream stream =
                    OvertureS3Client.openRangeStream(testClient, bucket, key, start, end)) {
                byte[] resultBuffer = stream.readAllBytes();
                String resultString = new String(resultBuffer, StandardCharsets.UTF_8);

                assertEquals("2345", resultString, "Should read specific range of bytes");
                assertEquals(4, resultBuffer.length, "Should read exactly 4 bytes");
            }
        }
    }
}
