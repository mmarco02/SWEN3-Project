package fh.swen.paperless.mapper;

import fh.swen.paperless.domain.Tag;
import fh.swen.paperless.dto.TagDTO;
import org.springframework.stereotype.Component;

@Component
public class TagMapper implements Mapper<Tag, TagDTO> {
    @Override
    public TagDTO mapToTarget(Tag source) {
        return TagDTO.builder()
                .id(source.getId())
                .name(source.getName())
                .createdAt(source.getCreatedAt())
                .build();
    }

    @Override
    public Tag mapToSource(TagDTO target) {
        return Tag.builder()
                .id(target.getId())
                .name(target.getName())
                .build();
    }
}
