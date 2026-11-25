package fh.swen.paperless.messaging;

import fh.swen.paperless.config.MessagingProperties;
import fh.swen.paperless.domain.Document;
import fh.swen.paperless.exception.MessagePublishException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class RabbitDocumentEventPublisher implements DocumentEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final MessagingProperties properties;

    @Override
    public void publishDocumentUploaded(Document document) {
        DocumentUploadedEvent event = DocumentUploadedEvent.from(document);
        String routingKey = properties.documentUploadedRoutingKey();
        try {
            rabbitTemplate.convertAndSend(properties.exchange(), routingKey, event);
            log.info("Published {} for document {} to exchange {}", routingKey, event.documentId(), properties.exchange());
        } catch (AmqpException e) {
            log.error("Could not publish {} for document {}", routingKey, event.documentId(), e);
            throw new MessagePublishException("Could not publish " + routingKey + " for document " + event.documentId(), e);
        }
    }
}
