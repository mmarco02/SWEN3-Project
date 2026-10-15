package fh.swen.paperless.service;

import fh.swen.paperless.domain.Document;
import fh.swen.paperless.domain.User;
import fh.swen.paperless.exception.NotFoundException;
import fh.swen.paperless.persistence.DocumentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserService userService;

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
    public Document create(String title, Long ownerId) {
        User owner = userService.fetchById(ownerId);

        Document document = Document.builder()
                .title(title)
                .owner(owner)
                .build();
        return documentRepository.save(document);
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
        documentRepository.delete(fetchById(id));
    }
}
