package fh.swen.paperless.controller;

import fh.swen.paperless.domain.Document;
import fh.swen.paperless.dto.DocumentDTO;
import fh.swen.paperless.dto.DocumentRequestDTO;
import fh.swen.paperless.mapper.DocumentMapper;
import fh.swen.paperless.service.DocumentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@AllArgsConstructor
class DocumentController {

    final DocumentService documentService;
    final DocumentMapper documentMapper;

    @GetMapping
    public ResponseEntity<List<DocumentDTO>> getAll() {
        return ResponseEntity.ok(documentMapper.mapToTarget(documentService.fetchAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(documentMapper.mapToTarget(documentService.fetchById(id)));
    }

    @GetMapping("/search")
    public ResponseEntity<List<DocumentDTO>> searchByTitle(@RequestParam String title) {
        return ResponseEntity.ok(documentMapper.mapToTarget(documentService.searchByTitle(title)));
    }

    @PostMapping
    public ResponseEntity<DocumentDTO> create(@Valid @RequestBody DocumentRequestDTO request) {
        Document document = documentService.create(request.getTitle(), request.getOwnerId());
        return ResponseEntity
                .created(URI.create("/api/documents/" + document.getId()))
                .body(documentMapper.mapToTarget(document));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentDTO> update(@PathVariable Long id, @Valid @RequestBody DocumentRequestDTO request) {
        Document document = documentService.update(id, request.getTitle(), request.getOwnerId());
        return ResponseEntity.ok(documentMapper.mapToTarget(document));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
