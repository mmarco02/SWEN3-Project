package fh.swen.paperless.service;

import fh.swen.paperless.domain.User;
import fh.swen.paperless.exception.InvalidCredentialsException;
import fh.swen.paperless.exception.NotFoundException;
import fh.swen.paperless.exception.UsernameTakenException;
import fh.swen.paperless.persistence.UserRepository;
import fh.swen.paperless.util.PasswordHasher;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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
                .passwordHash(PasswordHasher.hash(password))
                .build();
        return userRepository.save(user);
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);

        if (!PasswordHasher.verify(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return user;
    }

    @Transactional
    public void delete(Long id) {
        userRepository.delete(fetchById(id));
    }
}
