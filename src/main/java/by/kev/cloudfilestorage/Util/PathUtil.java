package by.kev.cloudfilestorage.Util;

import by.kev.cloudfilestorage.dto.ResourceType;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class PathUtil {

    public static String getPathWithoutRoot(String path) {
        return StringUtils.substringAfter(path, "/");
    }

    public static String getPathWithRoot(String path, Long userId) {
        return String.format("user-%d-files/%s", userId, path);
    }


    public static boolean isFolderPath(String path) {
        return path.endsWith("/");
    }

    public static ResourceType getResourceType(String path) {
        return isFolderPath(path) ? ResourceType.DIRECTORY : ResourceType.FILE;
    }

    public static String getResourceName(String path) {
        return FilenameUtils.getName(StringUtils.removeEnd(path, "/"));
    }

    public static String getResourcePathWithoutName(String path) {
        return FilenameUtils.getFullPath(StringUtils.removeEnd(path, "/"));

    }
}
