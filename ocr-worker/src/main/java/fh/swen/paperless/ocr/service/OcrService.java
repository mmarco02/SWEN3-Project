package fh.swen.paperless.ocr.service;

import fh.swen.paperless.ocr.domain.OcrJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Runs the OCR for a document. Sprint 3 only needs the "empty" worker: the job is logged,
 * fetching the file from MinIO and the OCR itself (Tesseract) follow in Sprint 4.
 */
@Slf4j
@Service
public class OcrService {

    public void process(OcrJob job) {
        log.info("OCR job for document {} ({}) accepted, OCR is not implemented yet", job.documentId(), job.fileName());
    }
}
