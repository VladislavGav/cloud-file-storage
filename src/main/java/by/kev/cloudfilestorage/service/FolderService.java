package by.kev.cloudfilestorage.service;

import by.kev.cloudfilestorage.Util.PathUtil;
import by.kev.cloudfilestorage.config.properties.MinioProperties;
import by.kev.cloudfilestorage.exception.MinioServiceException;
import by.kev.cloudfilestorage.exception.ResourceAlreadyExistException;
import by.kev.cloudfilestorage.exception.ResourceNotFoundException;
import io.minio.*;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FolderService extends MinioService {

    public FolderService(MinioClient minioClient, MinioProperties minioProperties) {
        super(minioClient, minioProperties);
    }

    @Override
    public boolean doesObjectExist(String path) {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .prefix(path)
                    .maxKeys(1)
                    .build());

            return results.iterator().hasNext();
        } catch (Exception e) {
            throw new MinioServiceException("Failed checking object existence");
        }
    }

    @Override
    public void move(String oldPath, String newPath) {
        try {
            Iterable<Result<Item>> results = getListObjects(oldPath, true);

            for (Result<Item> result : results) {
                String oldObjectName = result.get().objectName();
                String newObjectName = newPath + oldObjectName.substring(oldPath.length());

                copy(oldObjectName, newObjectName);
                delete(oldObjectName);
            }
        } catch (Exception e) {
            throw new MinioServiceException("Failed to move or rename folder");
        }
    }

    @Override
    public InputStream getResourceStream(String path) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            Iterable<Result<Item>> results = getListObjects(path, true);
            for (Result<Item> result : results) {
                String objectName = result.get().objectName();

                try (InputStream inputStream = getFileStream(objectName)) {
                    zipOutputStream.putNextEntry(new ZipEntry(PathUtil.getPathWithoutRoot(objectName)));
                    inputStream.transferTo(zipOutputStream);
                    zipOutputStream.closeEntry();
                }
            }
        } catch (Exception e) {
            throw new MinioServiceException("Failed to download folder");
        }

        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    public void createRootDirectory(Long userId) {
        String rootPath = PathUtil.getPathWithRoot("", userId);

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(rootPath)
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .build());
        } catch (Exception e) {
            throw new MinioServiceException("Failed to create root folder");
        }
    }

    public ObjectWriteResponse createEmptyDirectory(String path) {
        String parentPath = PathUtil.getResourcePathWithoutName(path);

        if (!doesObjectExist(parentPath))
            throw new ResourceNotFoundException("Parent folder doesn't exist");

        if (doesObjectExist(path))
            throw new ResourceAlreadyExistException("Folder already exists");

        try {
            return minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(path)
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .build());
        } catch (Exception e) {
            throw new MinioServiceException("Failed to create empty folder");
        }
    }

    public List<Item> getDirectoryObjects(String path, boolean recursive) {
        try {
            List<Item> items = new ArrayList<>();
            Iterable<Result<Item>> results = getListObjects(path, recursive);

            for (Result<Item> result : results) {
                items.add(result.get());
            }

            return items;
        } catch (Exception e) {
            throw new MinioServiceException("Failed to receive folder contents");
        }

    }

    private Iterable<Result<Item>> getListObjects(String path, boolean recursive) {
        return minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(minioProperties.getBucket())
                .prefix(path)
                .recursive(recursive)
                .build());
    }

    @Override
    @SneakyThrows
    public void delete(String path) {
        List<Item> items = getDirectoryObjects(path, true);
        List<DeleteObject> deleteObjectList = items.stream()
                .map(item -> new DeleteObject(item.objectName()))
                .toList();

        minioClient.removeObjects(RemoveObjectsArgs
                        .builder()
                        .bucket(minioProperties.getBucket())
                        .objects(deleteObjectList)
                        .build())
                .forEach(del -> {
                });
    }
}
