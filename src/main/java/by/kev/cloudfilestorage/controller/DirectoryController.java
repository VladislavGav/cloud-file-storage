package by.kev.cloudfilestorage.controller;

import by.kev.cloudfilestorage.dto.ResourceResponseDTO;
import by.kev.cloudfilestorage.security.UserDetailsImpl;
import by.kev.cloudfilestorage.service.StorageService;
import by.kev.cloudfilestorage.validation.ValidationConstants;
import io.swagger.v3.oas.annotations.Operation;
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
public class DirectoryController {

    private final StorageService storageService;

    @Operation(summary = "Get information about the contents of a folder")
    @GetMapping
    public ResponseEntity<List<ResourceResponseDTO>> getDirectoryContent(@RequestParam(name = "path")
                                                                         @NotBlank(message = ValidationConstants.PATH_NOT_EMPTY)
                                                                         @Pattern(regexp = ValidationConstants.PATH_REGEX,
                                                                                 message = ValidationConstants.PATH_INVALID)
                                                                         String path,
                                                                         @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<ResourceResponseDTO> resources = storageService.getDirectoryContent(path, userDetails.getUser().getId());

        return ResponseEntity.ok(resources);
    }

    @Operation(summary = "Create an empty folder")
    @PostMapping
    public ResponseEntity<ResourceResponseDTO> createDirectory(@RequestParam(name = "path")
                                                               @NotBlank(message = ValidationConstants.PATH_NOT_EMPTY)
                                                               @Pattern(regexp = ValidationConstants.PATH_REGEX,
                                                                       message = ValidationConstants.PATH_INVALID)
                                                               String path,
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {

        ResourceResponseDTO directory = storageService.createFolder(path, userDetails.getUser().getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(directory);
    }
}
