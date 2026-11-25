package fh.swen.paperless.ocr.messaging;

import java.time.LocalDateTime;

/**
 * JSON message the REST server sends after an upload (same fields as its DocumentUploadedEvent).
 */
public record DocumentUploadedEvent(Long documentId,
                                    Long ownerId,
                                    String title,
                                    String fileName,
                                    String contentType,
                                    Long fileSize,
                                    String storageKey,
                                    LocalDateTime uploadedAt) {
}
