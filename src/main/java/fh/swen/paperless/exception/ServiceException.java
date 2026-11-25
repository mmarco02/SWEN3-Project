package fh.swen.paperless.exception;

/**
 * Base class for errors of the business layer (services): the request breaks a business rule
 * or refers to something that does not exist. These are the client's fault and end up as 4xx.
 */
public abstract class ServiceException extends RuntimeException {
    protected ServiceException(String message) {
        super(message);
    }
}
