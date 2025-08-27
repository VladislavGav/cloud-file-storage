package by.kev.cloudfilestorage.controller;

import by.kev.cloudfilestorage.Util.PathUtil;
import by.kev.cloudfilestorage.dto.ResourceResponseDTO;
import by.kev.cloudfilestorage.security.UserDetailsImpl;
import by.kev.cloudfilestorage.service.StorageService;
import by.kev.cloudfilestorage.validation.ValidationConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/resource")
@Tag(name = "Resource Controller", description = "Resource API")
@Validated
public class ResourceController {

    private final StorageService storageService;

    @Operation(summary = "Get information about a resource")
    @GetMapping
    public ResponseEntity<ResourceResponseDTO> getResourceInfo(@RequestParam(name = "path")
                                                               @NotBlank(message = ValidationConstants.PATH_NOT_EMPTY)
                                                               @Pattern(regexp = ValidationConstants.PATH_REGEX,
                                                                       message = ValidationConstants.PATH_INVALID)
                                                               String path,
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {

        ResourceResponseDTO resource = storageService.getResourceMetadata(path, userDetails.getUser().getId());

        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Download a resource")
    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadResource(@RequestParam(name = "path")
                                                                @NotBlank(message = ValidationConstants.PATH_NOT_EMPTY)
                                                                @Pattern(regexp = ValidationConstants.PATH_REGEX,
                                                                        message = ValidationConstants.PATH_INVALID)
                                                                String path,
                                                                @AuthenticationPrincipal UserDetailsImpl userDetails) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(PathUtil.getResourceName(path))
                .build());

        InputStream resource = storageService.download(path, userDetails.getUser().getId());

        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(resource));
    }

    @Operation(summary = "Upload a resource")
    @PostMapping
    public ResponseEntity<List<ResourceResponseDTO>> uploadResource(@RequestParam(name = "object")
                                                                    @NotEmpty(message = ValidationConstants.FILES_NOT_EMPTY)
                                                                    MultipartFile[] files,
                                                                    @RequestParam(name = "path")
                                                                    @NotBlank(message = ValidationConstants.PATH_NOT_EMPTY)
                                                                    @Pattern(regexp = ValidationConstants.PATH_REGEX,
                                                                            message = ValidationConstants.PATH_INVALID)
                                                                    String path,
                                                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<ResourceResponseDTO> resources = storageService.uploadFiles(files, path, userDetails.getUser().getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(resources);
    }

    @Operation(summary = "Delete a resource")
    @DeleteMapping
    public ResponseEntity<Void> deleteResource(@RequestParam(name = "path")
                                               @NotBlank(message = ValidationConstants.PATH_NOT_EMPTY)
                                               @Pattern(regexp = ValidationConstants.PATH_REGEX,
                                                       message = ValidationConstants.PATH_INVALID)
                                               String path,
                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {

        storageService.delete(path, userDetails.getUser().getId());

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Rename/move a resource")
    @GetMapping("/move")
    public ResponseEntity<ResourceResponseDTO> moveResource(@RequestParam(name = "from")
                                                            @NotBlank(message = ValidationConstants.FROM_PATH_NOT_EMPTY)
                                                            @Pattern(regexp = ValidationConstants.PATH_REGEX,
                                                                    message = ValidationConstants.PATH_INVALID)
                                                            String pathFrom,
                                                            @RequestParam(name = "to")
                                                            @NotBlank(message = ValidationConstants.TO_PATH_NOT_EMPTY)
                                                            @Pattern(regexp = ValidationConstants.PATH_REGEX,
                                                                    message = ValidationConstants.PATH_INVALID)
                                                            String pathTo,
                                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        ResourceResponseDTO resultResource = storageService.moveResource(pathFrom, pathTo, userDetails.getUser().getId());

        return ResponseEntity.ok(resultResource);
    }

    @Operation(summary = "Search")
    @GetMapping("/search")
    public ResponseEntity<List<ResourceResponseDTO>> searchResource(@RequestParam(name = "query")
                                                                    @NotBlank(message = ValidationConstants.QUERY_NOT_EMPTY)
                                                                    @Pattern(regexp = ValidationConstants.PATH_REGEX,
                                                                            message = ValidationConstants.PATH_INVALID)
                                                                    String query,
                                                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<ResourceResponseDTO> resources = storageService.searchResources(query, userDetails.getUser().getId());

        return ResponseEntity.ok(resources);
    }
}
