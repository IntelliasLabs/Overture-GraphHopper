package com.graphhopper.reader.overture.aws;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

/**
 * Utility class to create S3 clients for accessing public Overture Maps data.
 * Includes robust retry logic with exponential backoff and random jitter.
 */
public final class OvertureS3Client {

    private static final Logger LOGGER = LoggerFactory.getLogger(OvertureS3Client.class);

    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF_MS = 100;
    private static final double BACKOFF_MULTIPLIER = 2.0;

    private OvertureS3Client() {}

    /**
     * Creates an S3 client configured for anonymous access in the specified region.
     *
     * @param region The AWS region (e.g., "us-west-2")
     * @return A configured S3Client instance
     */
    public static S3Client createAnonymousClient(String region) {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(AnonymousCredentialsProvider.create())
                .build();
    }

    /**
     * Downloads a file from an S3 bucket to a local destination with retry logic.
     *
     * @param client      The configured S3Client instance.
     * @param bucket      The name of the S3 bucket.
     * @param key         The key (path) of the file in the S3 bucket.
     * @param destination The local file where the content will be saved.
     * @return The destination file after successful download.
     * @throws IOException If an I/O error occurs or the S3 download fails after all retries.
     */
    public static File downloadFile(S3Client client, String bucket, String key, File destination)
            throws IOException {
        return executeWithRetry(
                () -> {
                    GetObjectRequest getObjectRequest =
                            GetObjectRequest.builder().bucket(bucket).key(key).build();

                    client.getObject(getObjectRequest, ResponseTransformer.toFile(destination.toPath()));
                    return destination;
                },
                "downloadFile",
                bucket,
                key);
    }

    /**
     * Streams a file directly from S3 without buffering the entire content in memory.
     * Includes retry logic for transient failures.
     *
     * @param client The S3 client to use
     * @param bucket The S3 bucket name
     * @param key    The S3 object key (path)
     * @return An InputStream reading directly from the S3 object
     * @throws IOException If the stream cannot be initiated after all retries
     */
    public static InputStream streamFile(S3Client client, String bucket, String key)
            throws IOException {
        return executeWithRetry(
                () -> {
                    GetObjectRequest request =
                            GetObjectRequest.builder().bucket(bucket).key(key).build();
                    return client.getObject(request);
                },
                "streamFile",
                bucket,
                key);
    }

    /**
     * Lists object keys in an S3 bucket with a specific prefix.
     * Includes retry logic for transient failures.
     *
     * @param client The configured S3Client instance.
     * @param bucket The name of the S3 bucket.
     * @param prefix The prefix (folder path) to filter by.
     * @return A list of object keys found.
     * @throws IOException If an S3 service error occurs after all retries.
     */
    public static List<String> listFiles(S3Client client, String bucket, String prefix)
            throws IOException {
        return executeWithRetry(
                () -> {
                    ListObjectsV2Request listRequest =
                            ListObjectsV2Request.builder().bucket(bucket).prefix(prefix).build();

                    ListObjectsV2Response listResponse = client.listObjectsV2(listRequest);

                    return listResponse.contents().stream().map(S3Object::key).collect(Collectors.toList());
                },
                "listFiles",
                bucket,
                prefix);
    }

    /**
     * Retrieves the total size (content length) of a specific object in S3.
     * Includes retry logic for transient failures.
     *
     * @param client the authenticated {@link S3Client} to use.
     * @param bucket the name of the S3 bucket.
     * @param key    the key (path) of the object within the bucket.
     * @return the size of the object in bytes.
     * @throws IOException if the S3 request fails after all retries.
     */
    public static long getObjectSize(S3Client client, String bucket, String key) throws IOException {
        return executeWithRetry(
                () -> {
                    HeadObjectRequest request =
                            HeadObjectRequest.builder().bucket(bucket).key(key).build();
                    return client.headObject(request).contentLength();
                },
                "getObjectSize",
                bucket,
                key);
    }

    /**
     * Opens an input stream for a specific byte range of an S3 object.
     * Includes retry logic for transient failures.
     *
     * @param client The S3 client to use
     * @param bucket The S3 bucket name
     * @param key    The S3 object key
     * @param start  the inclusive start byte position.
     * @param end    the inclusive end byte position.
     * @return an {@link InputStream} containing the bytes from {@code start} to {@code end}.
     * @throws IOException if the range request fails after all retries.
     */
    public static InputStream openRangeStream(
            S3Client client, String bucket, String key, long start, long end) throws IOException {
        String rangeHeader = "bytes=" + start + "-" + end;
        return executeWithRetry(
                () -> {
                    GetObjectRequest request = GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .range(rangeHeader)
                            .build();
                    return client.getObject(request);
                },
                "openRangeStream",
                bucket,
                key);
    }

    /**
     * Executes an S3 operation with retry logic, exponential backoff, and random jitter.
     *
     * @param operation     The S3 operation to execute
     * @param operationName The name of the operation for logging
     * @param bucket        The bucket name for logging
     * @param key           The key/prefix for logging
     * @param <T>           The return type of the operation
     * @return The result of the operation
     * @throws IOException If the operation fails after all retries or encounters a non-retryable error
     */
    private static <T> T executeWithRetry(
            S3Operation<T> operation, String operationName, String bucket, String key)
            throws IOException {
        int attempt = 0;
        long backoffMs = INITIAL_BACKOFF_MS;
        Exception lastException = null;

        while (attempt < DEFAULT_MAX_RETRIES) {
            attempt++;
            try {
                return operation.execute();
            } catch (NoSuchKeyException e) {
                LOGGER.error("File not found: bucket={}, key={}", bucket, key);
                throw new IOException("File not found: " + key + " in bucket: " + bucket, e);
            } catch (NoSuchBucketException e) {
                LOGGER.error("Bucket not found: {}", bucket);
                throw new IOException("Bucket not found: " + bucket, e);
            } catch (SdkException e) {
                // Unified handling for S3Exception (service) and SdkClientException (network)
                lastException = e;

                boolean shouldRetry = true;
                String errorType = "SDK error";

                if (e instanceof S3Exception) {
                    // Only retry specific 5xx errors or throttling signals
                    shouldRetry = isRetryable((S3Exception) e);
                    errorType = "S3 service error";
                } else if (e instanceof SdkClientException) {
                    // Always retry network/connectivity issues
                    errorType = "Network/Client error";
                }

                if (shouldRetry) {
                    LOGGER.warn(
                            "{} on {} (attempt {}/{}): bucket={}, key={}, error={}",
                            errorType,
                            operationName,
                            attempt,
                            DEFAULT_MAX_RETRIES,
                            bucket,
                            key,
                            e.getMessage());

                    if (attempt < DEFAULT_MAX_RETRIES) {
                        performWait(backoffMs);
                        backoffMs = (long) (backoffMs * BACKOFF_MULTIPLIER);
                    }
                } else {
                    // Fatal S3 error (e.g., AccessDenied 403)
                    String message = (e instanceof S3Exception && ((S3Exception) e).awsErrorDetails() != null)
                            ? ((S3Exception) e).awsErrorDetails().errorMessage()
                            : e.getMessage();
                    LOGGER.error(
                            "Non-retryable {} on {}: bucket={}, key={}, error={}",
                            errorType,
                            operationName,
                            bucket,
                            key,
                            message);
                    throw new IOException("S3 service error: " + message, e);
                }
            }
        }

        LOGGER.error(
                "All {} retries exhausted for {}: bucket={}, key={}",
                DEFAULT_MAX_RETRIES,
                operationName,
                bucket,
                key);
        throw new IOException(
                "Failed to " + operationName + " after " + DEFAULT_MAX_RETRIES + " attempts for key: " + key
                        + " in bucket: " + bucket,
                lastException);
    }

    /**
     * Determines if an S3Exception is retryable based on status codes and error codes.
     */
    private static boolean isRetryable(S3Exception e) {
        int status = e.statusCode();

        // Retry on Server Errors (5xx) or Throttling (429)
        if (status >= 500 || status == 429) {
            return true;
        }

        // Check specific string codes for legacy throttling errors
        if (e.awsErrorDetails() != null) {
            String code = e.awsErrorDetails().errorCode();
            return "Throttling".equals(code)
                    || "ThrottlingException".equals(code)
                    || "ProvisionedThroughputExceededException".equals(code)
                    || "RequestLimitExceeded".equals(code);
        }

        return false;
    }

    /**
     * Pauses execution with exponential backoff and random jitter.
     * Prevents "Thundering Herd" scenarios.
     */
    private static void performWait(long backoffMs) throws IOException {
        // Jitter: Add a random delay between 0 and 50% of the current backoff
        long jitter = ThreadLocalRandom.current().nextLong(0, (backoffMs / 2) + 1);
        long sleepTime = backoffMs + jitter;

        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupt flag
            throw new IOException("Operation interrupted during backoff", e);
        }
    }

    /**
     * Functional interface for S3 operations that can throw exceptions.
     *
     * @param <T> The return type of the operation
     */
    @FunctionalInterface
    private interface S3Operation<T> {
        T execute() throws SdkException;
    }
}
