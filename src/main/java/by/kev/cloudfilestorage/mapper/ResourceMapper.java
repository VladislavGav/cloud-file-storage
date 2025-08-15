package by.kev.cloudfilestorage.mapper;

import by.kev.cloudfilestorage.Util.PathUtil;
import by.kev.cloudfilestorage.dto.ResourceResponseDTO;
import by.kev.cloudfilestorage.dto.ResourceType;
import io.minio.messages.Item;
import org.springframework.stereotype.Component;

@Component
public class ResourceMapper {

    public ResourceResponseDTO toResourceResponseDTO(String path, Long fileSize) {
        String pathWithoutName = PathUtil.getPathWithoutRoot(PathUtil.getResourcePathWithoutName(path));
        String name = PathUtil.getResourceName(path);
        Long size = PathUtil.isFolderPath(path) ? null : fileSize;
        ResourceType type = PathUtil.getResourceType(path);

        return new ResourceResponseDTO(pathWithoutName, name, size, type);
    }

    public ResourceResponseDTO toResourceResponseDTO(Item object) {
        String objectName = object.objectName();

        String path = PathUtil.getPathWithoutRoot(PathUtil.getResourcePathWithoutName(objectName));
        String name = PathUtil.getResourceName(objectName);
        Long size = PathUtil.isFolderPath(objectName) ? null : object.size();
        ResourceType type = PathUtil.getResourceType(objectName);

        return new ResourceResponseDTO(path, name, size, type);
    }
}
