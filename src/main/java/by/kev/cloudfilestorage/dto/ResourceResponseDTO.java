package by.kev.cloudfilestorage.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResourceResponseDTO(String path, String name, Long size, ResourceType type) {
}
