package fh.swen.paperless.storage;

import fh.swen.paperless.config.MinioProperties;
import fh.swen.paperless.exception.FileStorageException;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioFileStorageTest {

    @Mock
    private MinioClient minioClient;   // Attrappe statt echtem MinIO-Server

    private MinioFileStorage storage;

    @BeforeEach
    void setUp() {
        storage = new MinioFileStorage(minioClient, new MinioProperties("http://minio:9000", "user", "pass", "documents"));
    }

    @Test
    void store_putsObjectIntoBucket() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        storage.store("key-1", new ByteArrayInputStream(new byte[]{1, 2, 3}), 3, "application/pdf");

        ArgumentCaptor<PutObjectArgs> args = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient).putObject(args.capture());
        assertThat(args.getValue().bucket()).isEqualTo("documents");
        assertThat(args.getValue().object()).isEqualTo("key-1");
        verify(minioClient, never()).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void store_createsBucketOnce_whenMissing() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

        storage.store("key-1", new ByteArrayInputStream(new byte[]{1}), 1, "image/png");
        storage.store("key-2", new ByteArrayInputStream(new byte[]{2}), 1, "image/png");

        verify(minioClient, times(1)).makeBucket(any(MakeBucketArgs.class));
        verify(minioClient, times(2)).putObject(any(PutObjectArgs.class));
    }

    @Test
    void store_throwsFileStorageException_whenMinioFails() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenThrow(new MinioException("connection refused"));

        assertThatThrownBy(() -> storage.store("key-1", new ByteArrayInputStream(new byte[]{1}), 1, "image/png"))
                .isInstanceOf(FileStorageException.class)
                .hasCauseInstanceOf(MinioException.class);
    }

    @Test
    void load_returnsObjectStream() throws Exception {
        GetObjectResponse response = mock(GetObjectResponse.class);
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(response);

        assertThat(storage.load("key-1")).isSameAs(response);
    }

    @Test
    void load_throwsFileStorageException_whenMinioFails() throws Exception {
        when(minioClient.getObject(any(GetObjectArgs.class))).thenThrow(new MinioException("not found"));

        assertThatThrownBy(() -> storage.load("key-1")).isInstanceOf(FileStorageException.class);
    }

    @Test
    void delete_removesObject() throws Exception {
        storage.delete("key-1");

        ArgumentCaptor<RemoveObjectArgs> args = ArgumentCaptor.forClass(RemoveObjectArgs.class);
        verify(minioClient).removeObject(args.capture());
        assertThat(args.getValue().object()).isEqualTo("key-1");
    }

    @Test
    void delete_throwsFileStorageException_whenMinioFails() throws Exception {
        doThrow(new MinioException("down")).when(minioClient).removeObject(any(RemoveObjectArgs.class));

        assertThatThrownBy(() -> storage.delete("key-1")).isInstanceOf(FileStorageException.class);
    }
}
