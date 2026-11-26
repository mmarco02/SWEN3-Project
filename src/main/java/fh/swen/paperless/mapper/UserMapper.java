package fh.swen.paperless.mapper;

import fh.swen.paperless.domain.User;
import fh.swen.paperless.dto.UserDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface UserMapper extends BaseMapper<User, UserDTO> {

    // id, username and createdAt have the same names on both sides; the password hash is not in the DTO
    @Override
    UserDTO mapToTarget(User source);

    // only take what a client may send, the password hash and documents are never set from a DTO
    @Override
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id")
    @Mapping(target = "username")
    User mapToSource(UserDTO target);
}
