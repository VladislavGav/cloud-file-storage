package by.kev.cloudfilestorage.controller;

import by.kev.cloudfilestorage.dto.ErrorResponseDTO;
import by.kev.cloudfilestorage.dto.ResourceResponseDTO;
import by.kev.cloudfilestorage.security.UserDetailsImpl;
import by.kev.cloudfilestorage.service.StorageService;
import by.kev.cloudfilestorage.validation.ValidationConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/directory")
@Tag(name = "Directory Controller", description = "Directory API")
@Validated
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid path format",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "User is unauthorized",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "Unknown error",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
})
public class DirectoryController {

    private final StorageService storageService;

    @Operation(summary = "Get information about the contents of a folder")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully received directory information",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResourceResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Directory doesn't exist",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<ResourceResponseDTO>> getDirectoryContent(@Parameter(example = "docs/folder1/")
                                                                         @RequestParam(name = "path")
                                                                         @Pattern(regexp = ValidationConstants.PATH_REGEX,
                                                                                 message = ValidationConstants.PATH_INVALID)
                                                                         String path,
                                                                         @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<ResourceResponseDTO> resources = storageService.getDirectoryContent(path, userDetails.getUser().getId());

        return ResponseEntity.ok(resources);
    }

    @Operation(summary = "Create an empty folder")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Directory successfully created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResourceResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Parent directory doesn't exist",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Directory already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping
    public ResponseEntity<ResourceResponseDTO> createDirectory(@Parameter(example = "docs/folder1/")
                                                               @RequestParam(name = "path")
                                                               @NotBlank(message = ValidationConstants.PATH_NOT_EMPTY)
                                                               @Pattern(regexp = ValidationConstants.PATH_REGEX,
                                                                       message = ValidationConstants.PATH_INVALID)
                                                               String path,
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {

        ResourceResponseDTO directory = storageService.createFolder(path, userDetails.getUser().getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(directory);
    }
}
