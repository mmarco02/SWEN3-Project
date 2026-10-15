package fh.swen.paperless.controller;

import fh.swen.paperless.domain.Document;
import fh.swen.paperless.domain.User;
import fh.swen.paperless.exception.InvalidCredentialsException;
import fh.swen.paperless.exception.NotFoundException;
import fh.swen.paperless.exception.UsernameTakenException;
import fh.swen.paperless.mapper.DocumentMapper;
import fh.swen.paperless.mapper.UserMapper;
import fh.swen.paperless.service.DocumentService;
import fh.swen.paperless.service.UserService;
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

@WebMvcTest(UserController.class)
@Import({UserMapper.class, DocumentMapper.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private DocumentService documentService;

    private final User user = User.builder().id(1L).username("mustafa").passwordHash("hashed").build();

    @Test
    void getAll_returns200WithUsers() throws Exception {
        when(userService.fetchAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("mustafa"));
    }

    @Test
    void getById_returns200_andNeverExposesPasswordHash() throws Exception {
        when(userService.fetchById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("mustafa"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void getById_returns404_whenMissing() throws Exception {
        when(userService.fetchById(99L)).thenThrow(new NotFoundException("User with id 99 not found"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByUsername_returns200() throws Exception {
        when(userService.fetchByUsername("mustafa")).thenReturn(user);

        mockMvc.perform(get("/api/users/username/mustafa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getDocuments_withoutTitle_returnsAllDocumentsOfUser() throws Exception {
        Document document = Document.builder().id(10L).title("Mietvertrag").owner(user).build();
        when(documentService.fetchByOwner(1L)).thenReturn(List.of(document));

        mockMvc.perform(get("/api/users/1/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Mietvertrag"));

        verify(documentService, never()).searchByOwnerAndTitle(anyLong(), anyString());
    }

    @Test
    void getDocuments_withTitle_searchesDocumentsOfUser() throws Exception {
        Document document = Document.builder().id(10L).title("Mietvertrag").owner(user).build();
        when(documentService.searchByOwnerAndTitle(1L, "miet")).thenReturn(List.of(document));

        mockMvc.perform(get("/api/users/1/documents").param("title", "miet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Mietvertrag"));

        verify(documentService, never()).fetchByOwner(anyLong());
    }

    @Test
    void register_returns201WithLocation() throws Exception {
        when(userService.register("mustafa", "geheim1234")).thenReturn(user);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "username": "mustafa", "password": "geheim1234" }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/users/1"))
                .andExpect(jsonPath("$.username").value("mustafa"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void register_returns409_whenUsernameTaken() throws Exception {
        when(userService.register("mustafa", "geheim1234")).thenThrow(new UsernameTakenException("mustafa"));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "username": "mustafa", "password": "geheim1234" }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void register_returns400_whenPasswordTooShort() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "username": "mustafa", "password": "kurz" }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void login_returns200_whenCredentialsValid() throws Exception {
        when(userService.login("mustafa", "geheim1234")).thenReturn(user);

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "username": "mustafa", "password": "geheim1234" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("mustafa"));
    }

    @Test
    void login_returns401_whenCredentialsInvalid() throws Exception {
        when(userService.login("mustafa", "falsch9999")).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "username": "mustafa", "password": "falsch9999" }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).delete(1L);
    }
}