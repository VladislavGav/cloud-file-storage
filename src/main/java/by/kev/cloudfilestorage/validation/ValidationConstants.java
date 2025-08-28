package by.kev.cloudfilestorage.validation;

public final class ValidationConstants {

    private ValidationConstants() {
    }

    public static final String PATH_NOT_EMPTY = "Path must not be empty";
    public static final String PATH_INVALID = "Invalid path format";

    public static final String QUERY_NOT_EMPTY = "Query must not be empty";
    public static final String FILES_NOT_EMPTY = "At least one file must be provided";

    public static final String FROM_PATH_NOT_EMPTY = "Path 'from' must not be empty";
    public static final String TO_PATH_NOT_EMPTY = "Path 'to' must not be empty";

    /**
     * Symbols are prohibited: \ : * ? " < > |
     */
    public static final String PATH_REGEX = "^$|^[^\\\\:*?\"<>|]+$";

}