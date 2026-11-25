package fh.swen.paperless.ocr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Names of the RabbitMQ exchange, queues and routing keys, bound from the "paperless.messaging.*" properties.
 * Must match the configuration of the REST server.
 */
@ConfigurationProperties(prefix = "paperless.messaging")
public record MessagingProperties(String exchange,
                                  String documentUploadedRoutingKey,
                                  String ocrQueue,
                                  String ocrDeadLetterQueue) {
}
