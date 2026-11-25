package fh.swen.paperless.exception;

public class InvalidCredentialsException extends ServiceException {
    public InvalidCredentialsException() {
        super("Invalid username or password");
    }
}
