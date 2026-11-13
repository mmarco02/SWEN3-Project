package fh.swen.paperless.mapper;

import fh.swen.paperless.domain.Tag;
import fh.swen.paperless.dto.TagDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TagMapperTest {

    private final TagMapper mapper = new TagMapper();

    @Test
    void mapToTarget_mapsAllFields() {
        LocalDateTime created = LocalDateTime.of(2026, 10, 15, 10, 0);
        Tag tag = Tag.builder().id(1L).name("Rechnung").createdAt(created).build();

        TagDTO dto = mapper.mapToTarget(tag);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Rechnung");
        assertThat(dto.getCreatedAt()).isEqualTo(created);
    }

    @Test
    void mapToSource_mapsIdAndName() {
        TagDTO dto = TagDTO.builder()
                .id(1L)
                .name("Rechnung")
                .createdAt(LocalDateTime.of(2026, 10, 15, 10, 0))
                .build();

        Tag tag = mapper.mapToSource(dto);

        assertThat(tag.getId()).isEqualTo(1L);
        assertThat(tag.getName()).isEqualTo("Rechnung");
        // createdAt wird von Hibernate gesetzt, nicht vom Client
        assertThat(tag.getCreatedAt()).isNull();
    }

    @Test
    void mapToTarget_list_mapsEveryElement() {
        Tag a = Tag.builder().id(1L).name("Rechnung").build();
        Tag b = Tag.builder().id(2L).name("Vertrag").build();

        List<TagDTO> dtos = mapper.mapToTarget(List.of(a, b));

        assertThat(dtos).extracting(TagDTO::getName).containsExactly("Rechnung", "Vertrag");
    }
}
