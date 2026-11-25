package fh.swen.paperless.ocr.service;

import fh.swen.paperless.ocr.domain.OcrJob;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class OcrServiceTest {

    private final OcrService ocrService = new OcrService();

    @Test
    void process_logsJob(CapturedOutput output) {
        ocrService.process(new OcrJob(10L, "key-1", "mietvertrag.pdf", "application/pdf"));

        // Abnahme Sprint 3: Upload führt zu einem Log-Eintrag im Worker
        assertThat(output).contains("OCR job for document 10 (mietvertrag.pdf) accepted");
    }
}
