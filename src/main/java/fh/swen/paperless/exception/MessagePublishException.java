package fh.swen.paperless.exception;

/**
 * A message could not be handed over to the message broker (RabbitMQ).
 */
public class MessagePublishException extends ServiceAgentException {
    public MessagePublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
