package fh.swen.paperless.service;

import fh.swen.paperless.domain.Document;
import fh.swen.paperless.domain.User;
import fh.swen.paperless.exception.NotFoundException;
import fh.swen.paperless.persistence.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private DocumentService documentService;

    private User owner;
    private Document document;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).username("mustafa").passwordHash("hashed").build();
        document = Document.builder().id(10L).title("Mietvertrag").owner(owner).build();
    }

    @Test
    void fetchAll_returnsAllDocuments() {
        when(documentRepository.findAll()).thenReturn(List.of(document));

        assertThat(documentService.fetchAll()).containsExactly(document);
    }

    @Test
    void fetchById_returnsDocument_whenFound() {
        when(documentRepository.findById(10L)).thenReturn(Optional.of(document));

        assertThat(documentService.fetchById(10L)).isEqualTo(document);
    }

    @Test
    void fetchById_throwsNotFound_whenMissing() {
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.fetchById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void fetchByOwner_returnsDocumentsOfOwner() {
        when(userService.fetchById(1L)).thenReturn(owner);
        when(documentRepository.findAllByOwnerId(1L)).thenReturn(List.of(document));

        assertThat(documentService.fetchByOwner(1L)).containsExactly(document);
    }

    @Test
    void fetchByOwner_throwsNotFound_whenOwnerMissing() {
        when(userService.fetchById(99L)).thenThrow(new NotFoundException("User with id 99 not found"));

        assertThatThrownBy(() -> documentService.fetchByOwner(99L))
                .isInstanceOf(NotFoundException.class);

        verifyNoInteractions(documentRepository);
    }

    @Test
    void searchByTitle_delegatesToRepository() {
        when(documentRepository.findAllByTitleContainingIgnoreCase("miet")).thenReturn(List.of(document));

        assertThat(documentService.searchByTitle("miet")).containsExactly(document);
    }

    @Test
    void searchByOwnerAndTitle_returnsMatchingDocuments() {
        when(userService.fetchById(1L)).thenReturn(owner);
        when(documentRepository.findAllByOwnerIdAndTitleContainingIgnoreCase(1L, "miet"))
                .thenReturn(List.of(document));

        assertThat(documentService.searchByOwnerAndTitle(1L, "miet")).containsExactly(document);
    }

    @Test
    void create_savesDocumentWithOwner() {
        when(userService.fetchById(1L)).thenReturn(owner);
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Document result = documentService.create("Mietvertrag", 1L);

        assertThat(result.getTitle()).isEqualTo("Mietvertrag");
        assertThat(result.getOwner()).isEqualTo(owner);
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    void create_throwsNotFound_whenOwnerMissing() {
        when(userService.fetchById(99L)).thenThrow(new NotFoundException("User with id 99 not found"));

        assertThatThrownBy(() -> documentService.create("Mietvertrag", 99L))
                .isInstanceOf(NotFoundException.class);

        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    void update_changesTitle_andKeepsOwner_whenOwnerUnchanged() {
        when(documentRepository.findById(10L)).thenReturn(Optional.of(document));
        when(documentRepository.saveAndFlush(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Document result = documentService.update(10L, "Neuer Titel", 1L);

        assertThat(result.getTitle()).isEqualTo("Neuer Titel");
        assertThat(result.getOwner()).isEqualTo(owner);
        verifyNoInteractions(userService); // Owner gleich -> keine zusätzliche Abfrage
    }

    @Test
    void update_changesOwner_whenOwnerDifferent() {
        User newOwner = User.builder().id(2L).username("kollege").passwordHash("x").build();
        when(documentRepository.findById(10L)).thenReturn(Optional.of(document));
        when(userService.fetchById(2L)).thenReturn(newOwner);
        when(documentRepository.saveAndFlush(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Document result = documentService.update(10L, "Mietvertrag", 2L);

        assertThat(result.getOwner()).isEqualTo(newOwner);
    }

    @Test
    void update_throwsNotFound_whenDocumentMissing() {
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.update(99L, "Titel", 1L))
                .isInstanceOf(NotFoundException.class);

        verify(documentRepository, never()).saveAndFlush(any(Document.class));
    }

    @Test
    void delete_removesDocument_whenFound() {
        when(documentRepository.findById(10L)).thenReturn(Optional.of(document));

        documentService.delete(10L);

        verify(documentRepository).delete(document);
    }

    @Test
    void delete_throwsNotFound_whenMissing() {
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.delete(99L))
                .isInstanceOf(NotFoundException.class);

        verify(documentRepository, never()).delete(any(Document.class));
    }
}