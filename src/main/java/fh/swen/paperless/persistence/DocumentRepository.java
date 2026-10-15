package fh.swen.paperless.persistence;

import fh.swen.paperless.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findAllByOwnerId(Long ownerId);

    List<Document> findAllByTitleContainingIgnoreCase(String title);

    List<Document> findAllByOwnerIdAndTitleContainingIgnoreCase(Long ownerId, String title);

    List<Document> findAllByTagsId(Long tagId);
}
