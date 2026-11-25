package fh.swen.paperless.messaging;

import fh.swen.paperless.domain.Document;
import fh.swen.paperless.exception.MessagePublishException;

/**
 * Publishes events about documents to the message broker. Implemented by {@link RabbitDocumentEventPublisher},
 * the services only depend on this interface.
 */
public interface DocumentEventPublisher {

    /**
     * Announces a newly uploaded document, the OCR worker picks it up from there.
     *
     * @throws MessagePublishException if the broker is not reachable
     */
    void publishDocumentUploaded(Document document);
}
