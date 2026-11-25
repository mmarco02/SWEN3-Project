package fh.swen.paperless.ocr.domain;

/**
 * A document that has to be run through OCR. Independent of the message format,
 * the listener translates the incoming message into this.
 */
public record OcrJob(Long documentId, String storageKey, String fileName, String contentType) {
}
