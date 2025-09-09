package by.kev.cloudfilestorage.service;

import by.kev.cloudfilestorage.config.properties.MinioProperties;
import by.kev.cloudfilestorage.exception.MinioServiceException;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class FileService extends MinioService {

    public FileService(MinioClient minioClient, MinioProperties minioProperties) {
        super(minioClient, minioProperties);
    }

    @Override
    public void move(String oldPath, String newPath) {
        try {
            copy(oldPath, newPath);
            delete(oldPath);
        } catch (Exception e) {
            throw new MinioServiceException("Failed to move or rename resource");
        }
    }

    @Override
    @SneakyThrows
    public void delete(String path) {
        minioClient.removeObject(RemoveObjectArgs
                .builder()
                .bucket(minioProperties.getBucket())
                .object(path)
                .build()
        );
    }

    @Override
    public boolean doesObjectExist(String path) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(path)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public InputStream getResourceStream(String path) {
        try {
            return getFileStream(path);
        } catch (Exception e) {
            throw new MinioServiceException("Failed to download file");
        }
    }
}
