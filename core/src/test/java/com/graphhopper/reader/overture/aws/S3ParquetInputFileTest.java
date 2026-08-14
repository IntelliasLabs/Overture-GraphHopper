package com.graphhopper.reader.overture.aws;

import org.apache.parquet.io.SeekableInputStream;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class S3ParquetInputFileTest {
    @Test
    void testConstructor_Success() throws IOException {
        S3Client mockClient = mock(S3Client.class);
        String bucket = "my-bucket";
        String key = "my-key.parquet";
        long expectedSize = 500L;

        HeadObjectResponse mockResponse = HeadObjectResponse.builder()
                .contentLength(expectedSize)
                .build();

        when(mockClient.headObject(any(HeadObjectRequest.class))).thenReturn(mockResponse);

        S3ParquetInputFile inputFile = new S3ParquetInputFile(mockClient, bucket, key);

        assertEquals(expectedSize, inputFile.getLength(), "Length should match the S3 object size");
        verify(mockClient, times(1)).headObject(any(HeadObjectRequest.class));
    }

    @Test
    void testConstructor_FailsOnNetworkError() {
        try(S3Client mockClient = mock(S3Client.class)) {

            when(mockClient.headObject(any(HeadObjectRequest.class)))
                    .thenThrow(SdkException.builder().message("S3 Unavailable").build());

            assertThrows(IOException.class, () -> new S3ParquetInputFile(mockClient, "bucket", "key"));
        }
    }

    @Test
    void testNewStream_ReturnsCorrectInstance() throws IOException {
        S3Client mockClient = mock(S3Client.class);
        HeadObjectResponse mockResponse = HeadObjectResponse.builder().contentLength(100L).build();
        when(mockClient.headObject(any(HeadObjectRequest.class))).thenReturn(mockResponse);

        S3ParquetInputFile inputFile = new S3ParquetInputFile(mockClient, "bucket", "key");

        SeekableInputStream stream = inputFile.newStream();

        assertNotNull(stream);
        assertInstanceOf(S3SeekableInputStream.class, stream, "Should create our custom S3 stream");

        SeekableInputStream stream2 = inputFile.newStream();
        assertNotSame(stream, stream2, "Each call should return a new stream instance");
    }
}
