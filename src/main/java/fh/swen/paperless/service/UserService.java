package fh.swen.paperless.service;

import fh.swen.paperless.domain.Document;
import fh.swen.paperless.domain.User;
import fh.swen.paperless.exception.InvalidCredentialsException;
import fh.swen.paperless.exception.NotFoundException;
import fh.swen.paperless.exception.UsernameTakenException;
import fh.swen.paperless.persistence.UserRepository;
import fh.swen.paperless.storage.FileStorage;
import fh.swen.paperless.util.PasswordHasher;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final FileStorage fileStorage;

    public List<User> fetchAll() {
        return userRepository.findAll();
    }

    public User fetchById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
    }

    public User fetchByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User '" + username + "' not found"));
    }

    @Transactional
    public User register(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new UsernameTakenException(username);
        }
        User user = User.builder()
                .username(username)
                .passwordHash(passwordHasher.hash(password))
                .build();
        return userRepository.save(user);
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordHasher.verify(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return user;
    }

    @Transactional
    public void delete(Long id) {
        User user = fetchById(id);
        // documents are removed by cascade, their files have to be removed from the storage
        List<String> storageKeys = user.getDocuments().stream()
                .map(Document::getStorageKey)
                .filter(Objects::nonNull)
                .toList();
        userRepository.delete(user);
        storageKeys.forEach(fileStorage::delete);
    }
}
