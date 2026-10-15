package fh.swen.paperless.controller;

import fh.swen.paperless.domain.User;
import fh.swen.paperless.dto.CredentialsDTO;
import fh.swen.paperless.dto.DocumentDTO;
import fh.swen.paperless.dto.UserDTO;
import fh.swen.paperless.mapper.DocumentMapper;
import fh.swen.paperless.mapper.UserMapper;
import fh.swen.paperless.service.DocumentService;
import fh.swen.paperless.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
class UserController {

    final UserService userService;
    final DocumentService documentService;
    final UserMapper userMapper;
    final DocumentMapper documentMapper;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAll() {
        return ResponseEntity.ok(userMapper.mapToTarget(userService.fetchAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userMapper.mapToTarget(userService.fetchById(id)));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userMapper.mapToTarget(userService.fetchByUsername(username)));
    }

    @GetMapping("/{id}/documents")
    public ResponseEntity<List<DocumentDTO>> getDocuments(@PathVariable Long id,
                                                         @RequestParam(required = false) String title) {
        var documents = title == null
                ? documentService.fetchByOwner(id)
                : documentService.searchByOwnerAndTitle(id, title);
        return ResponseEntity.ok(documentMapper.mapToTarget(documents));
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody CredentialsDTO credentials) {
        User user = userService.register(credentials.getUsername(), credentials.getPassword());
        return ResponseEntity
                .created(URI.create("/api/users/" + user.getId()))
                .body(userMapper.mapToTarget(user));
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@Valid @RequestBody CredentialsDTO credentials) {
        User user = userService.login(credentials.getUsername(), credentials.getPassword());
        return ResponseEntity.ok(userMapper.mapToTarget(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
