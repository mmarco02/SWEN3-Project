package fh.swen.paperless.mapper;

import fh.swen.paperless.domain.Document;
import fh.swen.paperless.domain.Tag;
import fh.swen.paperless.dto.DocumentDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper(config = MapStructConfig.class)
public interface DocumentMapper extends BaseMapper<Document, DocumentDTO> {

    // the DTO flattens the owner into id + username; tags are converted by mapTagNames below
    @Override
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerUsername", source = "owner.username")
    DocumentDTO mapToTarget(Document source);

    // owner is resolved by the service, since the DTO only carries its id;
    // file fields are set on upload only, never from a DTO
    @Override
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id")
    @Mapping(target = "title")
    Document mapToSource(DocumentDTO target);

    // used by MapStruct for Set<Tag> -> List<String>: tag names, sorted so the order is stable
    default List<String> mapTagNames(Set<Tag> tags) {
        return tags.stream().map(Tag::getName).sorted().toList();
    }
}
