package fh.swen.paperless.mapper;

import fh.swen.paperless.domain.Tag;
import fh.swen.paperless.dto.TagDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface TagMapper extends BaseMapper<Tag, TagDTO> {

    @Override
    TagDTO mapToTarget(Tag source);

    // createdAt is set by Hibernate, not by the client
    @Override
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id")
    @Mapping(target = "name")
    Tag mapToSource(TagDTO target);
}
