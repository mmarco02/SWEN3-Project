package fh.swen.paperless.messaging;

import fh.swen.paperless.domain.Document;

import java.time.LocalDateTime;

/**
 * JSON message sent when a document was uploaded. Contains everything a worker needs
 * to fetch the file from MinIO, without having to call back to the REST server.
 */
public record DocumentUploadedEvent(Long documentId,
                                    Long ownerId,
                                    String title,
                                    String fileName,
                                    String contentType,
                                    Long fileSize,
                                    String storageKey,
                                    LocalDateTime uploadedAt) {

    public static DocumentUploadedEvent from(Document document) {
        return new DocumentUploadedEvent(
                document.getId(),
                document.getOwner().getId(),
                document.getTitle(),
                document.getFileName(),
                document.getContentType(),
                document.getFileSize(),
                document.getStorageKey(),
                document.getCreatedAt());
    }
}
