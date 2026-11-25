package fh.swen.paperless.messaging;

import fh.swen.paperless.config.MessagingProperties;
import fh.swen.paperless.domain.Document;
import fh.swen.paperless.domain.User;
import fh.swen.paperless.exception.MessagePublishException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.net.ConnectException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitDocumentEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RabbitDocumentEventPublisher publisher;

    private final MessagingProperties properties = new MessagingProperties(
            "paperless.documents", "document.uploaded", "paperless.ocr", "paperless.ocr.dlq");

    private final LocalDateTime uploadedAt = LocalDateTime.of(2026, 11, 25, 10, 30);

    private Document document;

    @BeforeEach
    void setUp() {
        publisher = new RabbitDocumentEventPublisher(rabbitTemplate, properties);
        User owner = User.builder().id(1L).username("mustafa").passwordHash("hashed").build();
        document = Document.builder()
                .id(10L)
                .title("Mietvertrag")
                .owner(owner)
                .fileName("mietvertrag.pdf")
                .contentType("application/pdf")
                .fileSize(1234L)
                .storageKey("key-1")
                .createdAt(uploadedAt)
                .build();
    }

    @Test
    void publishDocumentUploaded_sendsEventToExchangeWithRoutingKey() {
        publisher.publishDocumentUploaded(document);

        ArgumentCaptor<Object> message = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(eq("paperless.documents"), eq("document.uploaded"), message.capture());
        assertThat(message.getValue()).isEqualTo(new DocumentUploadedEvent(
                10L, 1L, "Mietvertrag", "mietvertrag.pdf", "application/pdf", 1234L, "key-1", uploadedAt));
    }

    @Test
    void publishDocumentUploaded_wrapsBrokerError_inMessagePublishException() {
        AmqpConnectException brokerDown = new AmqpConnectException(new ConnectException("Connection refused"));
        doThrow(brokerDown).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        assertThatThrownBy(() -> publisher.publishDocumentUploaded(document))
                .isInstanceOf(MessagePublishException.class)
                .hasMessageContaining("document 10")
                .hasCause(brokerDown);
    }
}
