package fh.swen.paperless.mapper;

import fh.swen.paperless.domain.User;
import fh.swen.paperless.dto.UserDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void mapToTarget_mapsPublicFields() {
        LocalDateTime created = LocalDateTime.of(2026, 10, 15, 10, 0);
        User user = User.builder()
                .id(1L)
                .username("mustafa")
                .passwordHash("hashed")
                .createdAt(created)
                .build();

        UserDTO dto = mapper.mapToTarget(user);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getUsername()).isEqualTo("mustafa");
        assertThat(dto.getCreatedAt()).isEqualTo(created);
        // UserDTO hat absichtlich kein passwordHash-Feld -> wird nie nach außen gegeben
    }

    @Test
    void mapToSource_mapsIdAndUsername() {
        UserDTO dto = UserDTO.builder().id(1L).username("mustafa").build();

        User user = mapper.mapToSource(dto);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("mustafa");
        assertThat(user.getPasswordHash()).isNull();
    }

    @Test
    void mapToTarget_list_mapsEveryElement() {
        User a = User.builder().id(1L).username("anna").passwordHash("x").build();
        User b = User.builder().id(2L).username("bernd").passwordHash("y").build();

        List<UserDTO> dtos = mapper.mapToTarget(List.of(a, b));

        assertThat(dtos).extracting(UserDTO::getUsername).containsExactly("anna", "bernd");
    }
}