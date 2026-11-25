package fh.swen.paperless.exception;

/**
 * Base class for errors of the service agents, i.e. the adapters to external systems
 * (MinIO file storage, RabbitMQ). The external system is down or failed, so these end up as 503.
 * The original exception is kept as cause and logged by the agent, the client gets a generic message.
 */
public abstract class ServiceAgentException extends RuntimeException {
    protected ServiceAgentException(String message, Throwable cause) {
        super(message, cause);
    }
}
