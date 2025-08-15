package by.kev.cloudfilestorage.controller;

import by.kev.cloudfilestorage.dto.ResourceResponseDTO;
import by.kev.cloudfilestorage.security.UserDetailsImpl;
import by.kev.cloudfilestorage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/directory")
public class DirectoryController {

    private final StorageService storageService;

    @GetMapping
    public ResponseEntity<List<ResourceResponseDTO>> getDirectoryContent(@RequestParam(name = "path") String path,
                                                                         @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<ResourceResponseDTO> resources = storageService.getDirectoryContent(path, userDetails.getUser().getId());

        return ResponseEntity.ok(resources);
    }

    @PostMapping
    public ResponseEntity<ResourceResponseDTO> createDirectory(@RequestParam(name = "path") String path,
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {

        ResourceResponseDTO directory = storageService.createFolder(path, userDetails.getUser().getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(directory);
    }
}
