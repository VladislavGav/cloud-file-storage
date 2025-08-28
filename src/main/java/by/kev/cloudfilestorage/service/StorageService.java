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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final MinioServiceFactory minioServiceFactory;
    private final FolderService folderService;
    private final ResourceMapper mapper;

    public void delete(String path, Long userId) {
        MinioService service = minioServiceFactory.getServiceForPath(path);
        String fullPath = PathUtil.getPathWithRoot(path, userId);

        if (!service.doesObjectExist(fullPath))
            throw new ResourceNotFoundException("Resource not found in path: " + path);

        service.delete(fullPath);
    }

    public ResourceResponseDTO createFolder(String path, Long userId) {
        String fullPath = PathUtil.getPathWithRoot(path, userId);
        ObjectWriteResponse response = folderService.createEmptyDirectory(fullPath);

        return mapper.toResourceResponseDTO(response.object(), null);
    }

    public List<ResourceResponseDTO> uploadFiles(MultipartFile[] files, String path, Long userId) {
        List<ResourceResponseDTO> resourceResponseDTOList = new ArrayList<>();
        String parentPath = PathUtil.getPathWithRoot(path, userId);

        for (MultipartFile file : files) {
            String fullPath = parentPath + file.getOriginalFilename();
            MinioService service = minioServiceFactory.getServiceForPath(fullPath);

            if (service.doesObjectExist(fullPath))
                throw new ResourceAlreadyExistException("Resource already exists: " + fullPath);

            ObjectWriteResponse response = service.uploadFile(fullPath, file);

            resourceResponseDTOList.add(mapper.toResourceResponseDTO(response.object(), file.getSize()));
        }

        return resourceResponseDTOList;
    }

    public ResourceResponseDTO moveResource(String oldPath, String newPath, Long userId) {
        MinioService service = minioServiceFactory.getServiceForPath(oldPath);
        String fullOldPath = PathUtil.getPathWithRoot(oldPath, userId);
        String fullNewPath = PathUtil.getPathWithRoot(newPath, userId);

        if (!service.doesObjectExist(fullOldPath))
            throw new ResourceNotFoundException("Resource not found in path: " + oldPath);

        if (service.doesObjectExist(fullNewPath))
            throw new ResourceAlreadyExistException("Resource already exists: " + newPath);

        StatObjectResponse resourceMetadata = service.getResourceMetadata(fullOldPath);

        service.move(fullOldPath, fullNewPath);

        return mapper.toResourceResponseDTO(fullNewPath, resourceMetadata.size());
    }

    public InputStream download(String path, Long userId) {
        MinioService service = minioServiceFactory.getServiceForPath(path);
        String fullPath = PathUtil.getPathWithRoot(path, userId);

        if (!service.doesObjectExist(fullPath))
            throw new ResourceNotFoundException("Resource not found in path: " + path);

        return service.getResourceStream(fullPath);
    }

    public ResourceResponseDTO getResourceMetadata(String path, Long userId) {
        MinioService service = minioServiceFactory.getServiceForPath(path);
        String fullPath = PathUtil.getPathWithRoot(path, userId);

        if (!service.doesObjectExist(fullPath))
            throw new ResourceNotFoundException("Resource not found in path: " + path);

        StatObjectResponse resourceMetadata = service.getResourceMetadata(fullPath);

        return mapper.toResourceResponseDTO(path, resourceMetadata.size());
    }

    public List<ResourceResponseDTO> getDirectoryContent(String path, Long userId) {
        String fullPath = PathUtil.getPathWithRoot(path, userId);

        if (!folderService.doesObjectExist(fullPath))
            throw new ResourceNotFoundException("Folder doesn't exist");

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

        if (result.isEmpty())
            throw new ResourceNotFoundException("Resource not found");

        return result;
    }
}
