package fh.swen.paperless.exception;

public class TagNameTakenException extends RuntimeException {
    public TagNameTakenException(String name) {
        super("Tag '" + name + "' already exists");
    }
}
