package by.kev.cloudfilestorage.service;

import by.kev.cloudfilestorage.config.properties.MinioProperties;
import by.kev.cloudfilestorage.exception.MinioServiceException;
import io.minio.*;
import io.minio.errors.*;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RequiredArgsConstructor
public abstract class MinioService {

    protected final MinioClient minioClient;
    protected final MinioProperties minioProperties;

    @SneakyThrows
    public ObjectWriteResponse uploadFile(String path, MultipartFile file) {
        @Cleanup InputStream inputStream = file.getInputStream();
        return minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .object(path)
                        .stream(inputStream, file.getSize(), -1)
                        .build()
        );
    }

    @SneakyThrows
    protected InputStream getFileStream(String path) {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .object(path)
                        .bucket(minioProperties.getBucket())
                        .build()
        );
    }

    @SneakyThrows
    public void copy(String oldPath, String newPath) {
        minioClient.copyObject(CopyObjectArgs.builder()
                .bucket(minioProperties.getBucket())
                .object(newPath)
                .source(CopySource.builder()
                        .bucket(minioProperties.getBucket())
                        .object(oldPath)
                        .build())
                .build()
        );
    }

    @SneakyThrows
    public void delete(String path) {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(minioProperties.getBucket())
                .object(path)
                .build()
        );
    }

    public StatObjectResponse getResourceMetadata(String path) {
        try {
            return minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(path)
                    .build());
        } catch (Exception e) {
            throw new MinioServiceException("Failed to get metadata of resource");
        }
    }

    public abstract boolean doesObjectExist(String path);

    public abstract void move(String oldPath, String newPath);

    public abstract InputStream getResourceStream(String path);
}
