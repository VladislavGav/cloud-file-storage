package by.kev.cloudfilestorage.mapper;

import by.kev.cloudfilestorage.dto.UserRequestDTO;
import by.kev.cloudfilestorage.dto.UserResponseDTO;
import by.kev.cloudfilestorage.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserResponseDTO toUserResponseDTO(User user);
    UserResponseDTO toUserResponseDTO(UserRequestDTO userRequestDTO);

    User toUser(UserRequestDTO userRequestDTO);

}
