package fh.swen.paperless.controller;

import fh.swen.paperless.domain.Tag;
import fh.swen.paperless.dto.DocumentDTO;
import fh.swen.paperless.dto.TagDTO;
import fh.swen.paperless.dto.TagRequestDTO;
import fh.swen.paperless.mapper.DocumentMapper;
import fh.swen.paperless.mapper.TagMapper;
import fh.swen.paperless.service.DocumentService;
import fh.swen.paperless.service.TagService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tags")
@AllArgsConstructor
class TagController {

    final TagService tagService;
    final DocumentService documentService;
    final TagMapper tagMapper;
    final DocumentMapper documentMapper;

    @GetMapping
    public ResponseEntity<List<TagDTO>> getAll() {
        return ResponseEntity.ok(tagMapper.mapToTarget(tagService.fetchAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tagMapper.mapToTarget(tagService.fetchById(id)));
    }

    @GetMapping("/{id}/documents")
    public ResponseEntity<List<DocumentDTO>> getDocuments(@PathVariable Long id) {
        return ResponseEntity.ok(documentMapper.mapToTarget(documentService.fetchByTag(id)));
    }

    @PostMapping
    public ResponseEntity<TagDTO> create(@Valid @RequestBody TagRequestDTO request) {
        Tag tag = tagService.create(request.getName());
        return ResponseEntity
                .created(URI.create("/api/tags/" + tag.getId()))
                .body(tagMapper.mapToTarget(tag));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
