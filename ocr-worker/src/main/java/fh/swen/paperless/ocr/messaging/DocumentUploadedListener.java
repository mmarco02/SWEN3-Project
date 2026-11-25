package fh.swen.paperless.ocr.messaging;

import fh.swen.paperless.ocr.domain.OcrJob;
import fh.swen.paperless.ocr.exception.InvalidMessageException;
import fh.swen.paperless.ocr.service.OcrService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumes the "document.uploaded" messages from the OCR queue and hands them to the {@link OcrService}.
 * Messages that can't be processed are rejected without requeue, RabbitMQ then moves them to the dead letter queue.
 */
@Slf4j
@Component
@AllArgsConstructor
public class DocumentUploadedListener {

    private final OcrService ocrService;

    @RabbitListener(queues = "${paperless.messaging.ocr-queue}")
    public void onDocumentUploaded(DocumentUploadedEvent event) {
        log.info("Received document.uploaded: document {} '{}' ({}, {} bytes, owner {}, storage key {})",
                event.documentId(), event.fileName(), event.contentType(), event.fileSize(),
                event.ownerId(), event.storageKey());
        try {
            ocrService.process(toJob(event));
        } catch (InvalidMessageException e) {
            log.error("Rejected message for document {}: {}", event.documentId(), e.getMessage());
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }

    private OcrJob toJob(DocumentUploadedEvent event) {
        if (event.documentId() == null || event.storageKey() == null || event.storageKey().isBlank()) {
            throw new InvalidMessageException("documentId and storageKey are required");
        }
        return new OcrJob(event.documentId(), event.storageKey(), event.fileName(), event.contentType());
    }
}
