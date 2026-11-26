package fh.swen.paperless.mapper;

import fh.swen.paperless.domain.Document;
import fh.swen.paperless.domain.Tag;
import fh.swen.paperless.domain.User;
import fh.swen.paperless.dto.DocumentDTO;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentMapperTest {

    private final DocumentMapper mapper = Mappers.getMapper(DocumentMapper.class);

    private final User owner = User.builder()
            .id(1L)
            .username("mustafa")
            .passwordHash("hashed")
            .build();

    @Test
    void mapToTarget_mapsAllFieldsIncludingOwner() {
        LocalDateTime created = LocalDateTime.of(2026, 10, 15, 10, 0);
        LocalDateTime updated = LocalDateTime.of(2026, 10, 15, 12, 0);
        Document document = Document.builder()
                .id(10L)
                .title("Mietvertrag")
                .fileName("mietvertrag.pdf")
                .contentType("application/pdf")
                .fileSize(1234L)
                .storageKey("key-1")
                .owner(owner)
                .createdAt(created)
                .updatedAt(updated)
                .build();

        DocumentDTO dto = mapper.mapToTarget(document);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getTitle()).isEqualTo("Mietvertrag");
        assertThat(dto.getFileName()).isEqualTo("mietvertrag.pdf");
        assertThat(dto.getContentType()).isEqualTo("application/pdf");
        assertThat(dto.getFileSize()).isEqualTo(1234L);
        assertThat(dto.getOwnerId()).isEqualTo(1L);
        assertThat(dto.getOwnerUsername()).isEqualTo("mustafa");
        assertThat(dto.getCreatedAt()).isEqualTo(created);
        assertThat(dto.getUpdatedAt()).isEqualTo(updated);
    }

    @Test
    void mapToSource_mapsIdAndTitle_butNotOwner() {
        DocumentDTO dto = DocumentDTO.builder()
                .id(10L)
                .title("Mietvertrag")
                .ownerId(1L)
                .build();

        Document document = mapper.mapToSource(dto);

        assertThat(document.getId()).isEqualTo(10L);
        assertThat(document.getTitle()).isEqualTo("Mietvertrag");
        assertThat(document.getOwner()).isNull(); // Owner wird vom Service gesetzt
    }

    @Test
    void mapToTarget_mapsTagsToSortedNames() {
        Document document = Document.builder()
                .id(10L)
                .title("Mietvertrag")
                .owner(owner)
                .tags(Set.of(Tag.builder().id(2L).name("Vertrag").build(),
                        Tag.builder().id(1L).name("Rechnung").build()))
                .build();

        DocumentDTO dto = mapper.mapToTarget(document);

        assertThat(dto.getTags()).containsExactly("Rechnung", "Vertrag");
    }

    @Test
    void mapToSource_ignoresFileFieldsFromClient() {
        DocumentDTO dto = DocumentDTO.builder()
                .id(10L)
                .title("Mietvertrag")
                .fileName("fremde-datei.pdf")
                .contentType("application/pdf")
                .fileSize(999L)
                .tags(List.of("Rechnung"))
                .build();

        Document document = mapper.mapToSource(dto);

        // Datei-Felder werden nur beim Upload gesetzt, nie aus einem DTO
        assertThat(document.getFileName()).isNull();
        assertThat(document.getContentType()).isNull();
        assertThat(document.getFileSize()).isNull();
        assertThat(document.getStorageKey()).isNull();
        assertThat(document.getTags()).isEmpty();
    }

    @Test
    void mapToTarget_list_mapsEveryElementInOrder() {
        Document first = Document.builder().id(1L).title("A").owner(owner).build();
        Document second = Document.builder().id(2L).title("B").owner(owner).build();

        List<DocumentDTO> dtos = mapper.mapToTarget(List.of(first, second));

        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getTitle()).isEqualTo("A");
        assertThat(dtos.get(1).getTitle()).isEqualTo("B");
    }

    @Test
    void mapToSource_list_mapsEveryElement() {
        DocumentDTO first = DocumentDTO.builder().id(1L).title("A").build();
        DocumentDTO second = DocumentDTO.builder().id(2L).title("B").build();

        List<Document> documents = mapper.mapToSource(List.of(first, second));

        assertThat(documents).extracting(Document::getTitle).containsExactly("A", "B");
    }
}