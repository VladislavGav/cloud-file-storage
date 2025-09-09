package by.kev.cloudfilestorage.controller;

import by.kev.cloudfilestorage.Util.PathUtil;
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
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid path format",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "User is unauthorized",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "Unknown error",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
})
public class ResourceController {

    private final StorageService storageService;

    @Operation(summary = "Get information about a resource")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully received resource information",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResourceResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Resource not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping
    public ResponseEntity<ResourceResponseDTO> getResourceInfo(@Parameter(example = "docs/text.txt")
                                                               @RequestParam(name = "path")
                                                               @NotBlank(message = ValidationConstants.PATH_NOT_EMPTY)
                                                               @Pattern(regexp = ValidationConstants.PATH_REGEX,
                                                                       message = ValidationConstants.PATH_INVALID)
                                                               String path,
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {

        ResourceResponseDTO resource = storageService.getResourceMetadata(path, userDetails.getUser().getId());

        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Download a resource")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "file successfully downloaded",
                    content = @Content(mediaType = "application/octet-stream")),
            @ApiResponse(responseCode = "404", description = "Resource not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadResource(@Parameter(example = "docs/text.txt")
                                                                @RequestParam(name = "path")
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Resource successfully uploaded",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResourceResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Resource already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ResourceResponseDTO>> uploadResource(@RequestPart(name = "object")
                                                                    @NotEmpty(message = ValidationConstants.FILES_NOT_EMPTY)
                                                                    MultipartFile[] files,
                                                                    @Parameter(example = "folder/")
                                                                    @RequestParam(name = "path")
                                                                    @Pattern(regexp = ValidationConstants.PATH_REGEX,
                                                                            message = ValidationConstants.PATH_INVALID)
                                                                    String path,
                                                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<ResourceResponseDTO> resources = storageService.uploadFiles(files, path, userDetails.getUser().getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(resources);
    }

    @Operation(summary = "Delete a resource")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Resource successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Resource not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @DeleteMapping
    public ResponseEntity<Void> deleteResource(@Parameter(example = "docs/text.txt")
                                               @RequestParam(name = "path")
                                               @NotBlank(message = ValidationConstants.PATH_NOT_EMPTY)
                                               @Pattern(regexp = ValidationConstants.PATH_REGEX,
                                                       message = ValidationConstants.PATH_INVALID)
                                               String path,
                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {

        storageService.delete(path, userDetails.getUser().getId());

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Rename/move a resource")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resource successfully moved",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResourceResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Resource not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Resource already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/move")
    public ResponseEntity<ResourceResponseDTO> moveResource(@RequestParam(name = "from")
                                                            @Parameter(example = "docs/folder1/text.txt")
                                                            @NotBlank(message = ValidationConstants.FROM_PATH_NOT_EMPTY)
                                                            @Pattern(regexp = ValidationConstants.PATH_REGEX,
                                                                    message = ValidationConstants.PATH_INVALID)
                                                            String pathFrom,
                                                            @RequestParam(name = "to")
                                                            @Parameter(example = "docs/folder2/text.txt")
                                                            @NotBlank(message = ValidationConstants.TO_PATH_NOT_EMPTY)
                                                            @Pattern(regexp = ValidationConstants.PATH_REGEX,
                                                                    message = ValidationConstants.PATH_INVALID)
                                                            String pathTo,
                                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        ResourceResponseDTO resultResource = storageService.moveResource(pathFrom, pathTo, userDetails.getUser().getId());

        return ResponseEntity.ok(resultResource);
    }

    @Operation(summary = "Search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResourceResponseDTO.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<List<ResourceResponseDTO>> searchResource(@Parameter(example = "text")
                                                                    @RequestParam(name = "query")
                                                                    @NotBlank(message = ValidationConstants.QUERY_NOT_EMPTY)
                                                                    @Pattern(regexp = ValidationConstants.PATH_REGEX,
                                                                            message = ValidationConstants.PATH_INVALID)
                                                                    String query,
                                                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<ResourceResponseDTO> resources = storageService.searchResources(query, userDetails.getUser().getId());

        return ResponseEntity.ok(resources);
    }
}
