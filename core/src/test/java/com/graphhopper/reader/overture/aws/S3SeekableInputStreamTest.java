package com.graphhopper.reader.overture.aws;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@DisplayName("S3SeekableInputStream Unit Tests")
class S3SeekableInputStreamTest {

    private final String BUCKET = "test-bucket";
    private final String KEY = "test.parquet";
    private final long CONTENT_LENGTH = 100L;

    @Test
    @DisplayName("Seek() updates internal position correctly")
    void testSeekAndGetPos() {
        S3Client mockClient = mock(S3Client.class);
        S3SeekableInputStream stream =
                new S3SeekableInputStream(mockClient, BUCKET, KEY, CONTENT_LENGTH);

        assertEquals(0, stream.getPos());

        stream.seek(50);
        assertEquals(50, stream.getPos());

        stream.seek(0);
        assertEquals(0, stream.getPos());
    }

    @Test
    @DisplayName("Read() single byte returns correct value and advances position")
    void testRead_SingleByte() throws IOException {
        S3Client mockClient = mock(S3Client.class);

        @SuppressWarnings("unchecked")
        ResponseInputStream<GetObjectResponse> mockS3Stream = mock(ResponseInputStream.class);

        when(mockS3Stream.read(any(byte[].class), anyInt(), anyInt())).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            buffer[0] = 65; // / 'A'
            return 1; /// 1 byte read
        });

        when(mockClient.getObject(any(GetObjectRequest.class))).thenReturn(mockS3Stream);

        S3SeekableInputStream stream =
                new S3SeekableInputStream(mockClient, BUCKET, KEY, CONTENT_LENGTH);

        int byteRead = stream.read();

        assertEquals(65, byteRead);
        assertEquals(1, stream.getPos(), "Position should advance by 1");
    }

    @Test
    @DisplayName("Read(byte[]) requests correct Range from S3 (bytes=10-19)")
    void testRead_Array_VerifyRangeHeader() throws IOException {
        S3Client mockClient = mock(S3Client.class);

        @SuppressWarnings("unchecked")
        ResponseInputStream<GetObjectResponse> mockS3Stream = mock(ResponseInputStream.class);

        when(mockS3Stream.read(any(byte[].class), anyInt(), anyInt()))
                .thenReturn(10)
                .thenReturn(-1);
        when(mockClient.getObject(any(GetObjectRequest.class))).thenReturn(mockS3Stream);

        S3SeekableInputStream stream =
                new S3SeekableInputStream(mockClient, BUCKET, KEY, CONTENT_LENGTH);

        stream.seek(10);
        byte[] buffer = new byte[10];
        int readCount = stream.read(buffer, 0, 10);

        assertEquals(10, readCount);
        assertEquals(20, stream.getPos()); // / 10 (start) + 10 (read)

        ArgumentCaptor<GetObjectRequest> captor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(mockClient).getObject(captor.capture());

        GetObjectRequest request = captor.getValue();
        assertEquals("bytes=10-19", request.range());
    }

    @Test
    @DisplayName("ReadFully() successfully fills the buffer")
    void testReadFully_Success() throws IOException {
        S3Client mockClient = mock(S3Client.class);

        @SuppressWarnings("unchecked")
        ResponseInputStream<GetObjectResponse> mockS3Stream = mock(ResponseInputStream.class);

        when(mockS3Stream.read(any(byte[].class), anyInt(), anyInt())).thenAnswer(inv -> {
            byte[] b = inv.getArgument(0);
            Arrays.fill(b, (byte) 5);
            return 5;
        });
        when(mockClient.getObject(any(GetObjectRequest.class))).thenReturn(mockS3Stream);

        S3SeekableInputStream stream =
                new S3SeekableInputStream(mockClient, BUCKET, KEY, CONTENT_LENGTH);

        byte[] buffer = new byte[5];
        stream.readFully(buffer);

        assertEquals(5, buffer[0]);
        assertEquals(5, stream.getPos());
    }

    @Test
    @DisplayName("ReadFully() throws EOFException if S3 returns fewer bytes than requested")
    void testReadFully_ThrowsEOF_IfShortRead() throws IOException {
        S3Client mockClient = mock(S3Client.class);

        @SuppressWarnings("unchecked")
        ResponseInputStream<GetObjectResponse> mockS3Stream = mock(ResponseInputStream.class);

        when(mockS3Stream.read(any(byte[].class), anyInt(), anyInt())).thenReturn(5).thenReturn(-1);

        when(mockClient.getObject(any(GetObjectRequest.class))).thenReturn(mockS3Stream);

        S3SeekableInputStream stream =
                new S3SeekableInputStream(mockClient, BUCKET, KEY, CONTENT_LENGTH);
        byte[] buffer = new byte[10];

        assertThrows(EOFException.class, () -> stream.readFully(buffer));
    }

    @Test
    @DisplayName("Read(ByteBuffer) works with DirectByteBuffer")
    void testRead_ByteBuffer_Direct() throws IOException {
        S3Client mockClient = mock(S3Client.class);

        @SuppressWarnings("unchecked")
        ResponseInputStream<GetObjectResponse> mockS3Stream = mock(ResponseInputStream.class);

        when(mockS3Stream.read(any(byte[].class), anyInt(), anyInt())).thenReturn(5).thenReturn(-1);
        when(mockClient.getObject(any(GetObjectRequest.class))).thenReturn(mockS3Stream);

        S3SeekableInputStream stream =
                new S3SeekableInputStream(mockClient, BUCKET, KEY, CONTENT_LENGTH);

        ByteBuffer buffer = ByteBuffer.allocateDirect(10);
        int read = stream.read(buffer);

        assertEquals(5, read);
        assertEquals(5, stream.getPos());
        assertEquals(5, buffer.position());
    }

    @Test
    @DisplayName("Read() returns -1 (EOF) when reading past ContentLength")
    void testRead_EndOfFile() throws IOException {
        S3Client mockClient = mock(S3Client.class);
        S3SeekableInputStream stream = new S3SeekableInputStream(mockClient, BUCKET, KEY, 100);

        stream.seek(100);

        byte[] b = new byte[10];
        int result = stream.read(b, 0, 10);

        assertEquals(-1, result, "Should return -1 (EOF) when reading past length");
        verifyNoInteractions(mockClient);
    }
}
