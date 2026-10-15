package fh.swen.paperless.mapper;

import fh.swen.paperless.domain.User;
import fh.swen.paperless.dto.UserDTO;
import org.springframework.stereotype.Component;

@Component
public class UserMapper implements Mapper<User, UserDTO> {
    @Override
    public UserDTO mapToTarget(User source) {
        return UserDTO.builder()
                .id(source.getId())
                .username(source.getUsername())
                .createdAt(source.getCreatedAt())
                .build();
    }

    @Override
    public User mapToSource(UserDTO target) {
        return User.builder()
                .id(target.getId())
                .username(target.getUsername())
                .build();
    }
}
