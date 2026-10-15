package fh.swen.paperless.service;

import fh.swen.paperless.domain.Tag;
import fh.swen.paperless.exception.NotFoundException;
import fh.swen.paperless.exception.TagNameTakenException;
import fh.swen.paperless.persistence.DocumentRepository;
import fh.swen.paperless.persistence.TagRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final DocumentRepository documentRepository;

    public List<Tag> fetchAll() {
        return tagRepository.findAll();
    }

    public Tag fetchById(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tag with id " + id + " not found"));
    }

    @Transactional
    public Tag create(String name) {
        if (tagRepository.existsByNameIgnoreCase(name)) {
            throw new TagNameTakenException(name);
        }
        return tagRepository.save(Tag.builder().name(name).build());
    }

    @Transactional
    public void delete(Long id) {
        Tag tag = fetchById(id);
        // remove the tag from all documents first, otherwise the join table still references it
        documentRepository.findAllByTagsId(id).forEach(document -> document.getTags().remove(tag));
        tagRepository.delete(tag);
    }
}
