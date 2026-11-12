package fh.swen.paperless.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Connection settings for MinIO, bound from the "minio.*" properties.
 */
@ConfigurationProperties(prefix = "minio")
public record MinioProperties(String endpoint, String accessKey, String secretKey, String bucket) {
}
