package fh.swen.paperless.storage;

import fh.swen.paperless.config.MinioProperties;
import fh.swen.paperless.exception.FileStorageException;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Slf4j
@Component
public class MinioFileStorage implements FileStorage {

    private final MinioClient minioClient;
    private final String bucket;
    private volatile boolean bucketReady = false;

    public MinioFileStorage(MinioClient minioClient, MinioProperties properties) {
        this.minioClient = minioClient;
        this.bucket = properties.bucket();
    }

    @Override
    public void store(String key, InputStream content, long size, String contentType) {
        try {
            ensureBucket();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .stream(content, size, null) // null part size: let the client choose
                    .contentType(contentType)
                    .build());
            log.info("Stored file {} ({} bytes) in bucket {}", key, size, bucket);
        } catch (Exception e) {
            log.error("Could not store file {} in bucket {}", key, bucket, e);
            throw new FileStorageException("Could not store file", e);
        }
    }

    @Override
    public InputStream load(String key) {
        try {
            return minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(key).build());
        } catch (Exception e) {
            log.error("Could not load file {} from bucket {}", key, bucket, e);
            throw new FileStorageException("Could not load file", e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(key).build());
            log.info("Deleted file {} from bucket {}", key, bucket);
        } catch (Exception e) {
            log.error("Could not delete file {} from bucket {}", key, bucket, e);
            throw new FileStorageException("Could not delete file", e);
        }
    }

    // created on first upload instead of at startup, so the app does not depend on MinIO being up
    private void ensureBucket() throws Exception {
        if (bucketReady) {
            return;
        }
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            log.info("Created bucket {}", bucket);
        }
        bucketReady = true;
    }
}
