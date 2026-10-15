package fh.swen.paperless.mapper;

import fh.swen.paperless.domain.Document;
import fh.swen.paperless.domain.Tag;
import fh.swen.paperless.dto.DocumentDTO;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper implements Mapper<Document, DocumentDTO> {
    @Override
    public DocumentDTO mapToTarget(Document source) {
        return DocumentDTO.builder()
                .id(source.getId())
                .title(source.getTitle())
                .ownerId(source.getOwner().getId())
                .ownerUsername(source.getOwner().getUsername())
                .tags(source.getTags().stream().map(Tag::getName).sorted().toList())
                .createdAt(source.getCreatedAt())
                .updatedAt(source.getUpdatedAt())
                .build();
    }

    // owner is resolved by the service, since the DTO only carries its id
    @Override
    public Document mapToSource(DocumentDTO target) {
        return Document.builder()
                .id(target.getId())
                .title(target.getTitle())
                .build();
    }
}
