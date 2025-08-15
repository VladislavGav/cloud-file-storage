package by.kev.cloudfilestorage.dto;

import jakarta.validation.constraints.Size;

public record UserRequestDTO(
        @Size(min = 2, max = 30, message = "Username must be between 2 and 30 characters.")
        String username,

        @Size(min = 4, max = 20, message = "Password must be between 4 and 20 characters.")
        String password) {
}
