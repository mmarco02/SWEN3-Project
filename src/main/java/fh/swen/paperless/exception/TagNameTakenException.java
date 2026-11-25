package fh.swen.paperless.exception;

public class TagNameTakenException extends ServiceException {
    public TagNameTakenException(String name) {
        super("Tag '" + name + "' already exists");
    }
}
