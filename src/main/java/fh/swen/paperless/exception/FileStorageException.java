package fh.swen.paperless.exception;

/**
 * The file storage (MinIO) could not store, load or delete a file.
 */
public class FileStorageException extends ServiceAgentException {
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
