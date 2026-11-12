package fh.swen.paperless.controller;

import fh.swen.paperless.domain.Document;
import fh.swen.paperless.domain.User;
import fh.swen.paperless.exception.FileStorageException;
import fh.swen.paperless.exception.InvalidFileException;
import fh.swen.paperless.exception.NotFoundException;
import fh.swen.paperless.mapper.DocumentMapper;
import fh.swen.paperless.service.DocumentFile;
import fh.swen.paperless.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
@Import(DocumentMapper.class)   // echter Mapper, wird damit gleich mitgetestet
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentService documentService;

    private final MockMultipartFile pdf =
            new MockMultipartFile("file", "mietvertrag.pdf", "application/pdf", "%PDF-1.4".getBytes());

    private final User owner =User.builder().id(1L).username("mustafa").passwordHash("hashed").build();

    private Document document(Long id, String title) {
        return Document.builder().id(id).title(title).owner(owner).build();
    }

    @Test
    void getAll_returns200WithDocuments() throws Exception {
        when(documentService.fetchAll()).thenReturn(List.of(document(10L, "Mietvertrag")));

        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].title").value("Mietvertrag"))
                .andExpect(jsonPath("$[0].ownerUsername").value("mustafa"));
    }

    @Test
    void getById_returns200_whenFound() throws Exception {
        when(documentService.fetchById(10L)).thenReturn(document(10L, "Mietvertrag"));

        mockMvc.perform(get("/api/documents/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Mietvertrag"));
    }

    @Test
    void getById_returns404_whenMissing() throws Exception {
        when(documentService.fetchById(99L)).thenThrow(new NotFoundException("Document with id 99 not found"));

        mockMvc.perform(get("/api/documents/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Document with id 99 not found"));
    }

    @Test
    void searchByTitle_returns200WithMatches() throws Exception {
        when(documentService.searchByTitle("miet")).thenReturn(List.of(document(10L, "Mietvertrag")));

        mockMvc.perform(get("/api/documents/search").param("title", "miet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Mietvertrag"));
    }

    @Test
    void create_returns201WithLocation() throws Exception {
        Document created = document(10L, "Mietvertrag");
        created.setFileName("mietvertrag.pdf");
        when(documentService.create(eq("Mietvertrag"), eq(1L), any(MultipartFile.class))).thenReturn(created);

        mockMvc.perform(multipart("/api/documents")
                        .file(pdf)
                        .param("title", "Mietvertrag")
                        .param("ownerId", "1"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/documents/10"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.fileName").value("mietvertrag.pdf"));
    }

    @Test
    void create_returns400_whenTitleBlank() throws Exception {
        mockMvc.perform(multipart("/api/documents")
                        .file(pdf)
                        .param("title", "")
                        .param("ownerId", "1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(documentService); // Validierung stoppt vor dem Service
    }

    @Test
    void create_returns400_whenOwnerIdMissing() throws Exception {
        mockMvc.perform(multipart("/api/documents")
                        .file(pdf)
                        .param("title", "Mietvertrag"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(documentService);
    }

    @Test
    void create_returns400_whenFileMissing() throws Exception {
        mockMvc.perform(multipart("/api/documents")
                        .param("title", "Mietvertrag")
                        .param("ownerId", "1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(documentService);
    }

    @Test
    void create_returns400_whenFileInvalid() throws Exception {
        when(documentService.create(eq("Notiz"), eq(1L), any(MultipartFile.class)))
                .thenThrow(new InvalidFileException("Unsupported file type: text/plain"));

        mockMvc.perform(multipart("/api/documents")
                        .file(new MockMultipartFile("file", "notiz.txt", "text/plain", "hallo".getBytes()))
                        .param("title", "Notiz")
                        .param("ownerId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Unsupported file type: text/plain"));
    }

    @Test
    void create_returns503_whenStorageUnavailable() throws Exception {
        when(documentService.create(eq("Mietvertrag"), eq(1L), any(MultipartFile.class)))
                .thenThrow(new FileStorageException("Could not store file", null));

        mockMvc.perform(multipart("/api/documents")
                        .file(pdf)
                        .param("title", "Mietvertrag")
                        .param("ownerId", "1"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail").value("File storage is not available"));
    }

    @Test
    void getFile_returnsContentWithHeaders() throws Exception {
        Document stored = document(10L, "Mietvertrag");
        stored.setFileName("mietvertrag.pdf");
        stored.setContentType("application/pdf");
        stored.setFileSize(3L);
        when(documentService.openFile(10L))
                .thenReturn(new DocumentFile(stored, new ByteArrayInputStream(new byte[]{1, 2, 3})));

        mockMvc.perform(get("/api/documents/10/file"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string("Content-Disposition", containsString("inline")))
                .andExpect(header().string("Content-Disposition", containsString("mietvertrag.pdf")))
                .andExpect(content().bytes(new byte[]{1, 2, 3}));
    }

    @Test
    void getFile_returns404_whenDocumentHasNoFile() throws Exception {
        when(documentService.openFile(10L)).thenThrow(new NotFoundException("Document with id 10 has no file"));

        mockMvc.perform(get("/api/documents/10/file"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_returns200() throws Exception {
        when(documentService.update(10L, "Neu", 1L)).thenReturn(document(10L, "Neu"));

        mockMvc.perform(put("/api/documents/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "title": "Neu", "ownerId": 1 }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Neu"));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/documents/10"))
                .andExpect(status().isNoContent());

        verify(documentService).delete(10L);
    }

    @Test
    void delete_returns404_whenMissing() throws Exception {
        doThrow(new NotFoundException("Document with id 99 not found"))
                .when(documentService).delete(99L);

        mockMvc.perform(delete("/api/documents/99"))
                .andExpect(status().isNotFound());
    }
}