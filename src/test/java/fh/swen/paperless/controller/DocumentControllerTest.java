package fh.swen.paperless.controller;

import fh.swen.paperless.domain.Document;
import fh.swen.paperless.domain.User;
import fh.swen.paperless.exception.NotFoundException;
import fh.swen.paperless.mapper.DocumentMapper;
import fh.swen.paperless.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

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

    private final User owner = User.builder().id(1L).username("mustafa").passwordHash("hashed").build();

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
        when(documentService.create("Mietvertrag", 1L)).thenReturn(document(10L, "Mietvertrag"));

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "title": "Mietvertrag", "ownerId": 1 }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/documents/10"))
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void create_returns400_whenTitleBlank() throws Exception {
        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "title": "", "ownerId": 1 }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(documentService); // Validierung stoppt vor dem Service
    }

    @Test
    void create_returns400_whenOwnerIdMissing() throws Exception {
        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "title": "Mietvertrag" }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(documentService);
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