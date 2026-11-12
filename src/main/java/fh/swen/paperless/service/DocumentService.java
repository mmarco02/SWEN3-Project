package fh.swen.paperless.service;

import fh.swen.paperless.domain.Document;
import fh.swen.paperless.domain.User;
import fh.swen.paperless.exception.InvalidFileException;
import fh.swen.paperless.exception.NotFoundException;
import fh.swen.paperless.persistence.DocumentRepository;
import fh.swen.paperless.storage.FileStorage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class DocumentService {

    // PDFs and images, the formats the OCR worker will handle later
    public static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf", "image/png", "image/jpeg", "image/gif", "image/webp");

    private final DocumentRepository documentRepository;
    private final UserService userService;
    private final TagService tagService;
    private final FileStorage fileStorage;

    public List<Document> fetchAll() {
        return documentRepository.findAll();
    }

    public Document fetchById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document with id " + id + " not found"));
    }

    public List<Document> fetchByOwner(Long ownerId) {
        userService.fetchById(ownerId);
        return documentRepository.findAllByOwnerId(ownerId);
    }

    public List<Document> searchByTitle(String title) {
        return documentRepository.findAllByTitleContainingIgnoreCase(title);
    }

    public List<Document> searchByOwnerAndTitle(Long ownerId, String title) {
        userService.fetchById(ownerId);
        return documentRepository.findAllByOwnerIdAndTitleContainingIgnoreCase(ownerId, title);
    }

    @Transactional
    public Document create(String title, Long ownerId, MultipartFile file) {
        validateFile(file);
        User owner = userService.fetchById(ownerId);

        String storageKey = UUID.randomUUID().toString();
        try (InputStream content = file.getInputStream()) {
            fileStorage.store(storageKey, content, file.getSize(), file.getContentType());
        } catch (IOException e) {
            throw new InvalidFileException("Could not read uploaded file");
        }

        Document document = Document.builder()
                .title(title)
                .owner(owner)
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .storageKey(storageKey)
                .build();
        try {
            Document saved = documentRepository.save(document);
            log.info("Uploaded document {} ({}) for user {}", saved.getId(), saved.getFileName(), ownerId);
            return saved;
        } catch (RuntimeException e) {
            // don't leave an orphaned file in the storage
            fileStorage.delete(storageKey);
            throw e;
        }
    }

    public DocumentFile openFile(Long id) {
        Document document = fetchById(id);
        if (document.getStorageKey() == null) {
            throw new NotFoundException("Document with id " + id + " has no file");
        }
        return new DocumentFile(document, fileStorage.load(document.getStorageKey()));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is empty");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new InvalidFileException("Unsupported file type: " + file.getContentType());
        }
    }

    @Transactional
    public Document update(Long id, String title, Long ownerId) {
        Document document = fetchById(id);
        document.setTitle(title);

        if (!document.getOwner().getId().equals(ownerId)) {
            document.setOwner(userService.fetchById(ownerId));
        }
        return documentRepository.saveAndFlush(document);
    }

    @Transactional
    public void delete(Long id) {
        Document document = fetchById(id);
        documentRepository.delete(document);
        if (document.getStorageKey() != null) {
            // if this fails, the transaction rolls back and the document stays
            fileStorage.delete(document.getStorageKey());
        }
    }

    public List<Document> fetchByTag(Long tagId) {
        tagService.fetchById(tagId);
        return documentRepository.findAllByTagsId(tagId);
    }

    @Transactional
    public Document addTag(Long id, Long tagId) {
        Document document = fetchById(id);
        document.getTags().add(tagService.fetchById(tagId));
        return documentRepository.saveAndFlush(document);
    }

    @Transactional
    public Document removeTag(Long id, Long tagId) {
        Document document = fetchById(id);
        document.getTags().remove(tagService.fetchById(tagId));
        return documentRepository.saveAndFlush(document);
    }
}
