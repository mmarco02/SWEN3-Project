package fh.swen.paperless.ocr;

import fh.swen.paperless.ocr.messaging.DocumentUploadedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OcrWorkerApplicationTests {

    @Autowired
    private MessageConverter messageConverter;

    @Test
    void contextLoads() {
    }

    @Test
    void messageConverter_readsJsonFromRestServer() {
        // so kommt die Message vom REST-Server an: JSON + Typ-Header mit der Klasse aus dem REST-Projekt
        String json = """
                {"documentId":10,"ownerId":1,"title":"Mietvertrag","fileName":"mietvertrag.pdf",
                 "contentType":"application/pdf","fileSize":1234,"storageKey":"key-1",
                 "uploadedAt":"2026-11-25T10:30:00"}
                """;
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setHeader("__TypeId__", "fh.swen.paperless.messaging.DocumentUploadedEvent");
        // setzt Spring beim @RabbitListener aus dem Parametertyp der Listener-Methode
        properties.setInferredArgumentType(DocumentUploadedEvent.class);
        Message message = new Message(json.getBytes(StandardCharsets.UTF_8), properties);

        Object converted = messageConverter.fromMessage(message);

        assertThat(converted).isEqualTo(new DocumentUploadedEvent(10L, 1L, "Mietvertrag", "mietvertrag.pdf",
                "application/pdf", 1234L, "key-1", LocalDateTime.of(2026, 11, 25, 10, 30)));
    }
}
