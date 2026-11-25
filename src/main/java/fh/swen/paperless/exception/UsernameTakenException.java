package fh.swen.paperless.exception;

public class UsernameTakenException extends ServiceException {
    public UsernameTakenException(String username) {
        super("Username '" + username + "' is already taken");
    }
}
