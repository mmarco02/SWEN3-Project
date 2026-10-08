package fh.swen.paperless.mapper;

import fh.swen.paperless.domain.Document;
import fh.swen.paperless.dto.DocumentDTO;

public class DocumentMapper implements Mapper<Document, DocumentDTO> {
    @Override
    public DocumentDTO mapToTarget(Document source) {
        return null;
    }

    @Override
    public Document mapToSource(DocumentDTO target) {
        return null;
    }
}
