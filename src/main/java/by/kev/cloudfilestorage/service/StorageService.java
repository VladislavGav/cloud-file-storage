package by.kev.cloudfilestorage.service;

import by.kev.cloudfilestorage.Util.PathUtil;
import by.kev.cloudfilestorage.dto.ResourceResponseDTO;
import by.kev.cloudfilestorage.exception.ResourceAlreadyExistException;
import by.kev.cloudfilestorage.exception.ResourceNotFoundException;
import by.kev.cloudfilestorage.mapper.ResourceMapper;
import io.minio.ObjectWriteResponse;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final MinioServiceFactory minioServiceFactory;
    private final FolderService folderService;
    private final ResourceMapper mapper;

    public void delete(String path, Long userId) {
        MinioService service = minioServiceFactory.getServiceForPath(path);
        String fullPath = PathUtil.getPathWithRoot(path, userId);

        if (!service.doesObjectExist(fullPath)) {
            log.warn("User [{}] tried to delete non-existing resource [{}]", userId, path);
            throw new ResourceNotFoundException("Resource not found: " + path);
        }

        service.delete(fullPath);
        log.info("Resource [{}] deleted by user [{}]", path, userId);
    }

    public ResourceResponseDTO createFolder(String path, Long userId) {
        String fullPath = PathUtil.getPathWithRoot(path, userId);
        ObjectWriteResponse response = folderService.createEmptyDirectory(fullPath);

        log.info("Folder [{}] created by user [{}]", path, userId);
        return mapper.toResourceResponseDTO(response.object(), null);
    }

    public List<ResourceResponseDTO> uploadFiles(MultipartFile[] files, String path, Long userId) {
        String parentPath = PathUtil.getPathWithRoot(path, userId);
        List<ResourceResponseDTO> uploaded = new ArrayList<>();

        for (MultipartFile file : files) {
            String fullPath = parentPath + file.getOriginalFilename();
            MinioService service = minioServiceFactory.getServiceForPath(fullPath);

            if (service.doesObjectExist(fullPath)) {
                log.warn("User [{}] tried to upload an existing file [{}]", userId, file.getOriginalFilename());
                throw new ResourceAlreadyExistException("Resource already exists");
            }
            ObjectWriteResponse response = service.uploadFile(fullPath, file);

            uploaded.add(mapper.toResourceResponseDTO(response.object(), file.getSize()));
            log.info("File [{}] ({} bytes) uploaded by user [{}]", file.getOriginalFilename(), file.getSize(), userId);
        }

        return uploaded;
    }

    public ResourceResponseDTO moveResource(String oldPath, String newPath, Long userId) {
        MinioService service = minioServiceFactory.getServiceForPath(oldPath);
        String fullOldPath = PathUtil.getPathWithRoot(oldPath, userId);
        String fullNewPath = PathUtil.getPathWithRoot(newPath, userId);

        if (!service.doesObjectExist(fullOldPath)) {
            log.warn("User [{}] tried to move non-existing resource [{}]", userId, oldPath);
            throw new ResourceNotFoundException("Resource not found: " + oldPath);
        }

        if (service.doesObjectExist(fullNewPath)) {
            log.warn("User [{}] tried to overwrite existing resource [{}]", userId, newPath);
            throw new ResourceAlreadyExistException("Resource already exists: " + newPath);
        }

        StatObjectResponse resourceMetadata = service.getResourceMetadata(fullOldPath);
        service.move(fullOldPath, fullNewPath);

        log.info("Resource [{}] moved to [{}] by user [{}]", oldPath, newPath, userId);
        return mapper.toResourceResponseDTO(fullNewPath, resourceMetadata.size());
    }

    public InputStream download(String path, Long userId) {
        MinioService service = minioServiceFactory.getServiceForPath(path);
        String fullPath = PathUtil.getPathWithRoot(path, userId);

        if (!service.doesObjectExist(fullPath)) {
            log.warn("User [{}] tried to download non-existing resource [{}]", userId, path);
            throw new ResourceNotFoundException("Resource not found: " + path);
        }

        return service.getResourceStream(fullPath);
    }

    public ResourceResponseDTO getResourceMetadata(String path, Long userId) {
        MinioService service = minioServiceFactory.getServiceForPath(path);
        String fullPath = PathUtil.getPathWithRoot(path, userId);

        if (!service.doesObjectExist(fullPath)) {
            log.warn("User [{}] requested metadata for non-existing resource [{}]", userId, path);
            throw new ResourceNotFoundException("Resource not found: " + path);
        }
        StatObjectResponse resourceMetadata = service.getResourceMetadata(fullPath);
        log.info("Metadata retrieved for resource [{}] by user [{}]", path, userId);
        return mapper.toResourceResponseDTO(path, resourceMetadata.size());
    }

    public List<ResourceResponseDTO> getDirectoryContent(String path, Long userId) {
        String fullPath = PathUtil.getPathWithRoot(path, userId);

        if (!folderService.doesObjectExist(fullPath)) {
            log.warn("User [{}] requested content of non-existing folder [{}]", userId, path);
            throw new ResourceNotFoundException("Folder doesn't exist");
        }

        List<Item> items = folderService.getDirectoryObjects(fullPath, false);

        return items.stream()
                .filter(item -> !item.objectName().equals(fullPath))
                .map(mapper::toResourceResponseDTO)
                .toList();
    }

    public List<ResourceResponseDTO> searchResources(String query, Long userId) {
        String rootPath = PathUtil.getPathWithRoot("", userId);
        List<Item> items = folderService.getDirectoryObjects(rootPath, true);
        List<ResourceResponseDTO> result = new ArrayList<>();

        for (Item item : items) {
            String itemName = PathUtil.getResourceName(item.objectName());

            if (itemName.contains(query) && !item.objectName().equals(rootPath))
                result.add(mapper.toResourceResponseDTO(item));
        }

        if (result.isEmpty()) {
            log.warn("User [{}] searched for [{}], no results found", userId, query);
            throw new ResourceNotFoundException("Resource not found");
        }

        log.info("User [{}] searched for [{}], {} result(s) found", userId, query, result.size());
        return result;
    }
}
