package com.graphhopper.reader.overture.aws;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

public class OvertureS3ClientTest {

    @Test
    public void testCreateClientUsWest2() {
        S3Client client = OvertureS3Client.createAnonymousClient("us-west-2");
        assertNotNull(client);
        assertDoesNotThrow(client::close);
    }

    @Test
    public void testClassStructure() throws Exception {
        assertTrue(Modifier.isFinal(OvertureS3Client.class.getModifiers()));
        Constructor<OvertureS3Client> constructor = OvertureS3Client.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    }

    @Test
    public void testStreamFileReturnsStreamWithoutBuffering() throws Exception {
        S3Client mockClient = Mockito.mock(S3Client.class);
        @SuppressWarnings("unchecked")
        ResponseInputStream<GetObjectResponse> mockStream = Mockito.mock(ResponseInputStream.class);

        when(mockClient.getObject(any(GetObjectRequest.class))).thenReturn(mockStream);

        InputStream result = OvertureS3Client.streamFile(mockClient, "test-bucket", "test-key");

        assertNotNull(result);
        assertEquals(mockStream, result);
    }

    @Test
    void testDownloadFile_Successful() throws IOException {
        S3Client mockClient = mock(S3Client.class);
        File tempFile = Files.createTempFile("s3-test", ".tmp").toFile();
        tempFile.deleteOnExit();

        when(mockClient.getObject(any(GetObjectRequest.class), any(ResponseTransformer.class)))
                .thenReturn(null);

        File result = OvertureS3Client.downloadFile(mockClient, "test-bucket", "test-key", tempFile);
        assertNotNull(result);
        verify(mockClient, times(1))
                .getObject(any(GetObjectRequest.class), any(ResponseTransformer.class));
    }

    @Test
    void testDownloadFile_NoSuchKeyException() throws IOException {
        S3Client mockClient = mock(S3Client.class);
        File tempFile = Files.createTempFile("error-test", ".tmp").toFile();
        tempFile.deleteOnExit();

        NoSuchKeyException noSuchKeyException = (NoSuchKeyException)
                NoSuchKeyException.builder().message("Key not found").build();

        when(mockClient.getObject(any(GetObjectRequest.class), any(ResponseTransformer.class)))
                .thenThrow(noSuchKeyException);

        IOException exception = assertThrows(
                IOException.class,
                () -> OvertureS3Client.downloadFile(mockClient, "bucket", "nonexistent-key", tempFile));

        assertTrue(exception.getMessage().contains("File not found"));
        // Should not retry for NoSuchKeyException
        verify(mockClient, times(1))
                .getObject(any(GetObjectRequest.class), any(ResponseTransformer.class));
    }

    @Test
    void testDownloadFile_NoSuchBucketException() throws IOException {
        S3Client mockClient = mock(S3Client.class);
        File tempFile = Files.createTempFile("error-test", ".tmp").toFile();
        tempFile.deleteOnExit();

        NoSuchBucketException noSuchBucketException = (NoSuchBucketException)
                NoSuchBucketException.builder().message("Bucket not found").build();

        when(mockClient.getObject(any(GetObjectRequest.class), any(ResponseTransformer.class)))
                .thenThrow(noSuchBucketException);

        IOException exception = assertThrows(
                IOException.class,
                () -> OvertureS3Client.downloadFile(mockClient, "nonexistent-bucket", "key", tempFile));

        assertTrue(exception.getMessage().contains("Bucket not found"));
        // Should not retry for NoSuchBucketException
        verify(mockClient, times(1))
                .getObject(any(GetObjectRequest.class), any(ResponseTransformer.class));
    }

    @Test
    void testDownloadFile_S3Exception_NonRetryable() throws IOException {
        S3Client mockClient = mock(S3Client.class);
        File tempFile = Files.createTempFile("error-test", ".tmp").toFile();
        tempFile.deleteOnExit();

        S3Exception s3Exception = (S3Exception) S3Exception.builder()
                .message("Access Denied")
                .statusCode(403)
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorMessage("Access Denied")
                        .errorCode("403")
                        .build())
                .build();

        when(mockClient.getObject(any(GetObjectRequest.class), any(ResponseTransformer.class)))
                .thenThrow(s3Exception);

        IOException exception = assertThrows(
                IOException.class,
                () -> OvertureS3Client.downloadFile(mockClient, "bucket", "key", tempFile));

        assertTrue(exception.getMessage().contains("Access Denied"));
        // Should not retry for 403 (non-retryable)
        verify(mockClient, times(1))
                .getObject(any(GetObjectRequest.class), any(ResponseTransformer.class));
    }

    @Test
    void testDownloadFile_RetryOnServerError() throws IOException {
        S3Client mockClient = mock(S3Client.class);
        File tempFile = Files.createTempFile("retry-test", ".tmp").toFile();
        tempFile.deleteOnExit();

        S3Exception serverError = (S3Exception)
                S3Exception.builder().message("Internal Server Error").statusCode(500).build();

        // Fail twice, succeed on third attempt
        when(mockClient.getObject(any(GetObjectRequest.class), any(ResponseTransformer.class)))
                .thenThrow(serverError)
                .thenThrow(serverError)
                .thenReturn(null);

        File result = OvertureS3Client.downloadFile(mockClient, "bucket", "key", tempFile);

        assertNotNull(result);
        // Should have retried 3 times total
        verify(mockClient, times(3))
                .getObject(any(GetObjectRequest.class), any(ResponseTransformer.class));
    }

    @Test
    void testDownloadFile_AllRetriesExhausted() throws IOException {
        S3Client mockClient = mock(S3Client.class);
        File tempFile = Files.createTempFile("retry-exhausted", ".tmp").toFile();
        tempFile.deleteOnExit();

        S3Exception serverError = (S3Exception)
                S3Exception.builder().message("Service Unavailable").statusCode(503).build();

        // Always fail
        when(mockClient.getObject(any(GetObjectRequest.class), any(ResponseTransformer.class)))
                .thenThrow(serverError);

        IOException exception = assertThrows(
                IOException.class,
                () -> OvertureS3Client.downloadFile(mockClient, "bucket", "key", tempFile));

        assertTrue(exception.getMessage().contains("after 3 attempts"));
        // Should have tried 3 times
        verify(mockClient, times(3))
                .getObject(any(GetObjectRequest.class), any(ResponseTransformer.class));
    }

    @Test
    void testStreamFile_RetryOnNetworkError() throws IOException {
        S3Client mockClient = mock(S3Client.class);
        @SuppressWarnings("unchecked")
        ResponseInputStream<GetObjectResponse> mockStream = mock(ResponseInputStream.class);

        SdkClientException networkError =
                SdkClientException.builder().message("Connection reset").build();

        // Fail once, succeed on second attempt
        when(mockClient.getObject(any(GetObjectRequest.class)))
                .thenThrow(networkError)
                .thenReturn(mockStream);

        InputStream result = OvertureS3Client.streamFile(mockClient, "bucket", "key");

        assertNotNull(result);
        assertEquals(mockStream, result);
        verify(mockClient, times(2)).getObject(any(GetObjectRequest.class));
    }

    @Test
    void testStreamFile_AllRetriesExhausted_NetworkError() {
        S3Client mockClient = mock(S3Client.class);

        SdkClientException networkError =
                SdkClientException.builder().message("Connection timeout").build();

        when(mockClient.getObject(any(GetObjectRequest.class))).thenThrow(networkError);

        IOException exception = assertThrows(
                IOException.class, () -> OvertureS3Client.streamFile(mockClient, "bucket", "key"));

        assertTrue(exception.getMessage().contains("after 3 attempts"));
        verify(mockClient, times(3)).getObject(any(GetObjectRequest.class));
    }

    @Test
    void testListFiles_Successful() throws IOException {
        S3Client mockClient = mock(S3Client.class);

        S3Object obj1 = S3Object.builder().key("dir/file1.parquet").build();
        S3Object obj2 = S3Object.builder().key("dir/file2.parquet").build();

        ListObjectsV2Response mockResponse =
                ListObjectsV2Response.builder().contents(Arrays.asList(obj1, obj2)).build();

        when(mockClient.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(mockResponse);

        List<String> keys = OvertureS3Client.listFiles(mockClient, "test-bucket", "dir/");

        assertEquals(2, keys.size());
        assertTrue(keys.contains("dir/file1.parquet"));
        assertTrue(keys.contains("dir/file2.parquet"));
        verify(mockClient).listObjectsV2(any(ListObjectsV2Request.class));
    }

    @Test
    void testListFiles_RetryOnThrottling() throws IOException {
        S3Client mockClient = mock(S3Client.class);

        S3Exception throttlingError = (S3Exception)
                S3Exception.builder().message("Rate exceeded").statusCode(429).build();

        ListObjectsV2Response mockResponse = ListObjectsV2Response.builder()
                .contents(Arrays.asList(S3Object.builder().key("file.txt").build()))
                .build();

        when(mockClient.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenThrow(throttlingError)
                .thenReturn(mockResponse);

        List<String> keys = OvertureS3Client.listFiles(mockClient, "bucket", "prefix/");

        assertEquals(1, keys.size());
        verify(mockClient, times(2)).listObjectsV2(any(ListObjectsV2Request.class));
    }

    @Test
    void testGetObjectSize_Successful() throws IOException {
        S3Client mockClient = mock(S3Client.class);
        long expectedSize = 987654321L;

        HeadObjectResponse mockResponse =
                HeadObjectResponse.builder().contentLength(expectedSize).build();

        when(mockClient.headObject(any(HeadObjectRequest.class))).thenReturn(mockResponse);

        long actualSize = OvertureS3Client.getObjectSize(mockClient, "bucket", "key");

        assertEquals(expectedSize, actualSize);
        verify(mockClient).headObject(any(HeadObjectRequest.class));
    }

    @Test
    void testGetObjectSize_RetryOnServerError() throws IOException {
        S3Client mockClient = mock(S3Client.class);

        S3Exception serverError = (S3Exception)
                S3Exception.builder().message("Internal Error").statusCode(500).build();

        HeadObjectResponse mockResponse =
                HeadObjectResponse.builder().contentLength(100L).build();

        when(mockClient.headObject(any(HeadObjectRequest.class)))
                .thenThrow(serverError)
                .thenReturn(mockResponse);

        long size = OvertureS3Client.getObjectSize(mockClient, "bucket", "key");

        assertEquals(100L, size);
        verify(mockClient, times(2)).headObject(any(HeadObjectRequest.class));
    }

    @Test
    void testOpenRangeStream_Successful() throws IOException {
        S3Client mockClient = mock(S3Client.class);
        @SuppressWarnings("unchecked")
        ResponseInputStream<GetObjectResponse> mockStream = mock(ResponseInputStream.class);

        when(mockClient.getObject(any(GetObjectRequest.class))).thenReturn(mockStream);

        InputStream result = OvertureS3Client.openRangeStream(mockClient, "bucket", "key", 100, 200);

        assertNotNull(result);
        assertEquals(mockStream, result);
        verify(mockClient).getObject(any(GetObjectRequest.class));
    }

    @Test
    void testOpenRangeStream_NoSuchKey() {
        S3Client mockClient = mock(S3Client.class);

        NoSuchKeyException noSuchKey = (NoSuchKeyException)
                NoSuchKeyException.builder().message("Key not found").build();

        when(mockClient.getObject(any(GetObjectRequest.class))).thenThrow(noSuchKey);

        IOException exception = assertThrows(
                IOException.class,
                () -> OvertureS3Client.openRangeStream(mockClient, "bucket", "missing-key", 0, 100));

        assertTrue(exception.getMessage().contains("File not found"));
        verify(mockClient, times(1)).getObject(any(GetObjectRequest.class));
    }

    @Test
    void testDownloadFile_FunctionalVerification() throws IOException {
        S3Client mockClient = mock(S3Client.class);

        File tempFile = Files.createTempFile("overture-functional-test", ".parquet").toFile();
        tempFile.deleteOnExit();

        when(mockClient.getObject(any(GetObjectRequest.class), any(ResponseTransformer.class)))
                .thenAnswer(invocation -> {
                    Files.write(tempFile.toPath(), "mocked s3 data content".getBytes());
                    return null;
                });

        File result = OvertureS3Client.downloadFile(mockClient, "fake-bucket", "fake-key", tempFile);

        assertNotNull(result, "Resulting file should not be null");
        assertTrue(result.exists(), "File should exist on disk");
        assertEquals(tempFile.getAbsolutePath(), result.getAbsolutePath(), "Paths should match");

        String content = Files.readString(result.toPath());
        assertEquals("mocked s3 data content", content);
    }
}
