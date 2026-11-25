package fh.swen.paperless.ocr.messaging;

import fh.swen.paperless.ocr.domain.OcrJob;
import fh.swen.paperless.ocr.service.OcrService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class DocumentUploadedListenerTest {

    @Mock
    private OcrService ocrService;

    @InjectMocks
    private DocumentUploadedListener listener;

    private DocumentUploadedEvent event(Long documentId, String storageKey) {
        return new DocumentUploadedEvent(documentId, 1L, "Mietvertrag", "mietvertrag.pdf", "application/pdf",
                1234L, storageKey, LocalDateTime.of(2026, 11, 25, 10, 30));
    }

    @Test
    void onDocumentUploaded_passesJobToOcrService() {
        listener.onDocumentUploaded(event(10L, "key-1"));

        verify(ocrService).process(new OcrJob(10L, "key-1", "mietvertrag.pdf", "application/pdf"));
    }

    @Test
    void onDocumentUploaded_rejectsMessage_whenStorageKeyMissing() {
        // ohne storageKey kann die Datei nie geladen werden -> nicht erneut zustellen, ab in die Dead Letter Queue
        assertThatThrownBy(() -> listener.onDocumentUploaded(event(10L, " ")))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class)
                .hasMessageContaining("storageKey");

        verifyNoInteractions(ocrService);
    }

    @Test
    void onDocumentUploaded_rejectsMessage_whenDocumentIdMissing() {
        assertThatThrownBy(() -> listener.onDocumentUploaded(event(null, "key-1")))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);

        verifyNoInteractions(ocrService);
    }
}
