package by.kev.cloudfilestorage.controller;

import by.kev.cloudfilestorage.Util.PathUtil;
import by.kev.cloudfilestorage.dto.ResourceResponseDTO;
import by.kev.cloudfilestorage.security.UserDetailsImpl;
import by.kev.cloudfilestorage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/resource")
public class ResourceController {

    private final StorageService storageService;

    @GetMapping
    public ResponseEntity<ResourceResponseDTO> getResourceInfo(@RequestParam(name = "path") String path,
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {

        ResourceResponseDTO resource = storageService.getResourceMetadata(path, userDetails.getUser().getId());

        return ResponseEntity.ok(resource);
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadResource(@RequestParam(name = "path") String path,
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

    @PostMapping
    public ResponseEntity<List<ResourceResponseDTO>> uploadResource(@RequestParam(name = "object") MultipartFile[] files,
                                                                    @RequestParam(name = "path") String path,
                                                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<ResourceResponseDTO> resources = storageService.uploadFiles(files, path, userDetails.getUser().getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(resources);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteResource(@RequestParam(name = "path") String path,
                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {

        storageService.delete(path, userDetails.getUser().getId());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/move")
    public ResponseEntity<ResourceResponseDTO> moveResource(@RequestParam(name = "from") String pathFrom,
                                                            @RequestParam(name = "to") String pathTo,
                                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        ResourceResponseDTO resultResource = storageService.moveResource(pathFrom, pathTo, userDetails.getUser().getId());

        return ResponseEntity.ok(resultResource);
    }
}
