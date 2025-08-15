package by.kev.cloudfilestorage.service;

import by.kev.cloudfilestorage.Util.PathUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MinioServiceFactory {

    private final FolderService folderService;
    private final FileService fileService;

    public MinioService getServiceForPath(String path) {
        return PathUtil.isFolderPath(path) ? folderService : fileService;
    }
}

