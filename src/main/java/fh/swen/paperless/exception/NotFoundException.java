package fh.swen.paperless.exception;

public class NotFoundException extends ServiceException {
    public NotFoundException(String message) {
        super(message);
    }
}
