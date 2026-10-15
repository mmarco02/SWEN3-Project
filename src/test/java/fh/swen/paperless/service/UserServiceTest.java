package fh.swen.paperless.service;

import fh.swen.paperless.domain.User;
import fh.swen.paperless.exception.InvalidCredentialsException;
import fh.swen.paperless.exception.NotFoundException;
import fh.swen.paperless.exception.UsernameTakenException;
import fh.swen.paperless.persistence.UserRepository;
import fh.swen.paperless.util.PasswordHasher;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;   // Attrappe statt echter Datenbank

    @Mock
    private PasswordHasher passwordHasher;

    @InjectMocks
    private UserService userService;         // echter Service, bekommt die Mocks injiziert

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("mustafa")
                .passwordHash("hashed")
                .build();
    }

    @Test
    void fetchAll_returnsAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        assertThat(userService.fetchAll()).containsExactly(user);
    }

    @Test
    void fetchById_returnsUser_whenFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThat(userService.fetchById(1L)).isEqualTo(user);
    }

    @Test
    void fetchById_throwsNotFound_whenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.fetchById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void fetchByUsername_returnsUser_whenFound() {
        when(userRepository.findByUsername("mustafa")).thenReturn(Optional.of(user));

        assertThat(userService.fetchByUsername("mustafa")).isEqualTo(user);
    }

    @Test
    void fetchByUsername_throwsNotFound_whenMissing() {
        when(userRepository.findByUsername("niemand")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.fetchByUsername("niemand"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("niemand");
    }

    @Test
    void register_savesUserWithHashedPassword() {
        when(userRepository.existsByUsername("mustafa")).thenReturn(false);
        when(passwordHasher.hash("geheim1234")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.register("mustafa", "geheim1234");

        assertThat(result.getUsername()).isEqualTo("mustafa");
        assertThat(result.getPasswordHash()).isEqualTo("hashed");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_throws_whenUsernameTaken() {
        when(userRepository.existsByUsername("mustafa")).thenReturn(true);

        assertThatThrownBy(() -> userService.register("mustafa", "geheim1234"))
                .isInstanceOf(UsernameTakenException.class);

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordHasher);
    }

    @Test
    void login_returnsUser_whenPasswordCorrect() {
        when(userRepository.findByUsername("mustafa")).thenReturn(Optional.of(user));
        when(passwordHasher.verify("geheim1234", "hashed")).thenReturn(true);

        assertThat(userService.login("mustafa", "geheim1234")).isEqualTo(user);
    }

    @Test
    void login_throws_whenUserUnknown() {
        when(userRepository.findByUsername("niemand")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login("niemand", "geheim1234"))
                .isInstanceOf(InvalidCredentialsException.class);

        verifyNoInteractions(passwordHasher);
    }

    @Test
    void login_throws_whenPasswordWrong() {
        when(userRepository.findByUsername("mustafa")).thenReturn(Optional.of(user));
        when(passwordHasher.verify("falsch999", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> userService.login("mustafa", "falsch999"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void delete_removesUser_whenFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.delete(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void delete_throwsNotFound_whenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(99L))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository, never()).delete(any(User.class));
    }
}