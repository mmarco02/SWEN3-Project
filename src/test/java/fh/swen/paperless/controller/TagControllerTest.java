package fh.swen.paperless.controller;

import fh.swen.paperless.domain.Document;
import fh.swen.paperless.domain.Tag;
import fh.swen.paperless.domain.User;
import fh.swen.paperless.exception.NotFoundException;
import fh.swen.paperless.exception.TagNameTakenException;
import fh.swen.paperless.mapper.DocumentMapper;
import fh.swen.paperless.mapper.TagMapper;
import fh.swen.paperless.service.DocumentService;
import fh.swen.paperless.service.TagService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TagController.class)
@Import({TagMapper.class, DocumentMapper.class})
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TagService tagService;

    @MockitoBean
    private DocumentService documentService;

    private final Tag tag = Tag.builder().id(1L).name("Rechnung").build();

    @Test
    void getAll_returns200WithTags() throws Exception {
        when(tagService.fetchAll()).thenReturn(List.of(tag));

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Rechnung"));
    }

    @Test
    void getById_returns200() throws Exception {
        when(tagService.fetchById(1L)).thenReturn(tag);

        mockMvc.perform(get("/api/tags/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Rechnung"));
    }

    @Test
    void getById_returns404_whenMissing() throws Exception {
        when(tagService.fetchById(99L)).thenThrow(new NotFoundException("Tag with id 99 not found"));

        mockMvc.perform(get("/api/tags/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Tag with id 99 not found"));
    }

    @Test
    void getDocuments_returnsDocumentsWithTag() throws Exception {
        User owner = User.builder().id(1L).username("mustafa").passwordHash("hashed").build();
        Document document = Document.builder()
                .id(10L)
                .title("Stromrechnung")
                .owner(owner)
                .tags(new HashSet<>(Set.of(tag)))
                .build();
        when(documentService.fetchByTag(1L)).thenReturn(List.of(document));

        mockMvc.perform(get("/api/tags/1/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Stromrechnung"))
                .andExpect(jsonPath("$[0].tags[0]").value("Rechnung"));
    }

    @Test
    void create_returns201WithLocation() throws Exception {
        when(tagService.create("Rechnung")).thenReturn(tag);

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "Rechnung" }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/tags/1"))
                .andExpect(jsonPath("$.name").value("Rechnung"));
    }

    @Test
    void create_returns409_whenNameTaken() throws Exception {
        when(tagService.create("Rechnung")).thenThrow(new TagNameTakenException("Rechnung"));

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "Rechnung" }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Tag 'Rechnung' already exists"));
    }

    @Test
    void create_returns400_whenNameBlank() throws Exception {
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "  " }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(tagService);
    }

    @Test
    void create_returns400_whenNameTooLong() throws Exception {
        String name = "x".repeat(51);

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"" + name + "\" }"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(tagService);
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/tags/1"))
                .andExpect(status().isNoContent());

        verify(tagService).delete(1L);
    }

    @Test
    void delete_returns404_whenMissing() throws Exception {
        doThrow(new NotFoundException("Tag with id 99 not found")).when(tagService).delete(99L);

        mockMvc.perform(delete("/api/tags/99"))
                .andExpect(status().isNotFound());
    }
}
