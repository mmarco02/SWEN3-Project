package fh.swen.paperless.ocr.exception;

/**
 * An incoming message is missing data the worker needs. Retrying would not help,
 * so the message goes to the dead letter queue.
 */
public class InvalidMessageException extends RuntimeException {
    public InvalidMessageException(String message) {
        super(message);
    }
}
