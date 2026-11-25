package fh.swen.paperless.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Names of the RabbitMQ exchange, queues and routing keys, bound from the "paperless.messaging.*" properties.
 * The OCR worker declares the same names, so both sides agree on the topology.
 */
@ConfigurationProperties(prefix = "paperless.messaging")
public record MessagingProperties(String exchange,
                                  String documentUploadedRoutingKey,
                                  String ocrQueue,
                                  String ocrDeadLetterQueue) {
}
