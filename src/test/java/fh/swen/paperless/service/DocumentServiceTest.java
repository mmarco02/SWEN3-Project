package fh.swen.paperless.service;

import fh.swen.paperless.domain.Document;
import fh.swen.paperless.domain.User;
import fh.swen.paperless.exception.FileStorageException;
import fh.swen.paperless.exception.InvalidFileException;
import fh.swen.paperless.exception.NotFoundException;
import fh.swen.paperless.persistence.DocumentRepository;
import fh.swen.paperless.storage.FileStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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

    @Mock
    private FileStorage fileStorage;   // Attrappe statt MinIO

    @InjectMocks
    private DocumentService documentService;

    private User owner;
    private Document document;
    private final MockMultipartFile pdf =
            new MockMultipartFile("file", "mietvertrag.pdf", "application/pdf", "%PDF-1.4".getBytes());

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
    void create_storesFileAndSavesDocumentWithOwner() {
        when(userService.fetchById(1L)).thenReturn(owner);
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Document result = documentService.create("Mietvertrag", 1L, pdf);

        assertThat(result.getTitle()).isEqualTo("Mietvertrag");
        assertThat(result.getOwner()).isEqualTo(owner);
        assertThat(result.getFileName()).isEqualTo("mietvertrag.pdf");
        assertThat(result.getContentType()).isEqualTo("application/pdf");
        assertThat(result.getFileSize()).isEqualTo(pdf.getSize());
        assertThat(result.getStorageKey()).isNotBlank();
        verify(fileStorage).store(eq(result.getStorageKey()), any(InputStream.class), eq(pdf.getSize()), eq("application/pdf"));
    }

    @Test
    void create_throwsNotFound_whenOwnerMissing() {
        when(userService.fetchById(99L)).thenThrow(new NotFoundException("User with id 99 not found"));

        assertThatThrownBy(() -> documentService.create("Mietvertrag", 99L, pdf))
                .isInstanceOf(NotFoundException.class);

        verifyNoInteractions(fileStorage);
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    void create_throwsInvalidFile_whenFileEmpty() {
        MockMultipartFile empty = new MockMultipartFile("file", "leer.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> documentService.create("Leer", 1L, empty))
                .isInstanceOf(InvalidFileException.class)
                .hasMessage("File is empty");

        verifyNoInteractions(fileStorage, documentRepository);
    }

    @Test
    void create_throwsInvalidFile_whenTypeNotAllowed() {
        MockMultipartFile text = new MockMultipartFile("file", "notiz.txt", "text/plain", "hallo".getBytes());

        assertThatThrownBy(() -> documentService.create("Notiz", 1L, text))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("text/plain");

        verifyNoInteractions(fileStorage, documentRepository);
    }

    @Test
    void create_doesNotSaveDocument_whenStorageFails() {
        when(userService.fetchById(1L)).thenReturn(owner);
        doThrow(new FileStorageException("down", null))
                .when(fileStorage).store(anyString(), any(InputStream.class), anyLong(), anyString());

        assertThatThrownBy(() -> documentService.create("Mietvertrag", 1L, pdf))
                .isInstanceOf(FileStorageException.class);

        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    void create_deletesStoredFile_whenSavingFails() {
        when(userService.fetchById(1L)).thenReturn(owner);
        when(documentRepository.save(any(Document.class))).thenThrow(new RuntimeException("db down"));

        assertThatThrownBy(() -> documentService.create("Mietvertrag", 1L, pdf))
                .hasMessage("db down");

        // dieselbe Datei, die gespeichert wurde, wird wieder entfernt
        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        verify(fileStorage).store(key.capture(), any(InputStream.class), anyLong(), anyString());
        verify(fileStorage).delete(key.getValue());
    }

    @Test
    void openFile_returnsDocumentAndContent() {
        document.setStorageKey("key-1");
        InputStream content = new ByteArrayInputStream(new byte[]{1, 2, 3});
        when(documentRepository.findById(10L)).thenReturn(Optional.of(document));
        when(fileStorage.load("key-1")).thenReturn(content);

        DocumentFile result = documentService.openFile(10L);

        assertThat(result.document()).isEqualTo(document);
        assertThat(result.content()).isSameAs(content);
    }

    @Test
    void openFile_throwsNotFound_whenDocumentHasNoFile() {
        when(documentRepository.findById(10L)).thenReturn(Optional.of(document));

        assertThatThrownBy(() -> documentService.openFile(10L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("no file");

        verifyNoInteractions(fileStorage);
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
    void delete_removesDocumentAndFile_whenFound() {
        document.setStorageKey("key-1");
        when(documentRepository.findById(10L)).thenReturn(Optional.of(document));

        documentService.delete(10L);

        verify(documentRepository).delete(document);
        verify(fileStorage).delete("key-1");
    }

    @Test
    void delete_skipsStorage_whenDocumentHasNoFile() {
        when(documentRepository.findById(10L)).thenReturn(Optional.of(document));

        documentService.delete(10L);

        verify(documentRepository).delete(document);
        verifyNoInteractions(fileStorage);
    }

    @Test
    void delete_throwsNotFound_whenMissing() {
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.delete(99L))
                .isInstanceOf(NotFoundException.class);

        verify(documentRepository, never()).delete(any(Document.class));
    }
}