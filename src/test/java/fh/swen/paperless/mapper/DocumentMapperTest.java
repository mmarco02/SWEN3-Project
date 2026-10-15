package fh.swen.paperless.mapper;

import fh.swen.paperless.domain.Document;
import fh.swen.paperless.domain.User;
import fh.swen.paperless.dto.DocumentDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentMapperTest {

    private final DocumentMapper mapper = new DocumentMapper();

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
                .owner(owner)
                .createdAt(created)
                .updatedAt(updated)
                .build();

        DocumentDTO dto = mapper.mapToTarget(document);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getTitle()).isEqualTo("Mietvertrag");
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