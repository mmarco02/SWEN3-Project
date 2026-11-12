package fh.swen.paperless.controller;

import fh.swen.paperless.domain.Document;
import fh.swen.paperless.dto.DocumentDTO;
import fh.swen.paperless.dto.DocumentRequestDTO;
import fh.swen.paperless.dto.DocumentUploadDTO;
import fh.swen.paperless.mapper.DocumentMapper;
import fh.swen.paperless.service.DocumentFile;
import fh.swen.paperless.service.DocumentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;
import java.nio.charset.StandardCharsets;
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentDTO> create(@Valid @ModelAttribute DocumentUploadDTO upload) {
        Document document = documentService.create(upload.getTitle(), upload.getOwnerId(), upload.getFile());
        return ResponseEntity
                .created(URI.create("/api/documents/" + document.getId()))
                .body(documentMapper.mapToTarget(document));
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<InputStreamResource> getFile(@PathVariable Long id) {
        DocumentFile file = documentService.openFile(id);
        Document document = file.document();
        ContentDisposition disposition = ContentDisposition.inline()
                .filename(document.getFileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getContentType()))
                .contentLength(document.getFileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(new InputStreamResource(file.content()));
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

    @PostMapping("/{id}/tags/{tagId}")
    public ResponseEntity<DocumentDTO> addTag(@PathVariable Long id, @PathVariable Long tagId) {
        return ResponseEntity.ok(documentMapper.mapToTarget(documentService.addTag(id, tagId)));
    }

    @DeleteMapping("/{id}/tags/{tagId}")
    public ResponseEntity<DocumentDTO> removeTag(@PathVariable Long id, @PathVariable Long tagId) {
        return ResponseEntity.ok(documentMapper.mapToTarget(documentService.removeTag(id, tagId)));
    }
}
