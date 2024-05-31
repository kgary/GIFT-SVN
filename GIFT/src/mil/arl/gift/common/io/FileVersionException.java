/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import mil.arl.gift.common.util.StringUtils;

/**
 * This exception is used to provide feedback on why a file is not a correct version (for example,
 * it may not be the latest version or it may not be an expected version).
 * 
 * @author sharrison
 *
 */
public class FileVersionException extends RuntimeException {
    /** serial version */
    private static final long serialVersionUID = 1L;

    /**
     * The different types of causes for throwing a file version exception.
     * 
     * @author sharrison
     */
    public static enum VersionExceptionType {
        /** When two files are of different versions */
        DIFFERENT_FILE_VERSIONS,
        /** When a file is not matching an expected version */
        NOT_EXPECTED_VERSION;
    }

    /** the error message that is built using the provided constructor arugments */
    private String message;

    /** The type describing the cause of the file version exception */
    private VersionExceptionType versionExceptionType;

    /**
     * No-arg constructor needed by GWT RPC. This constructor does not create a valid instance of
     * this class and should not be used under most circumstances
     */
    protected FileVersionException() {
    }

    /**
     * Create an exception for when a specific file does not match an expected version.
     * 
     * @param sourceFilePath the path of the file that failed the version check. Can be absolute or
     *        relative. Can't be blank.
     * @param sourceVersion the version of the file that failed the version check. Can't be blank.
     * @param expectedVersion the expected version. Can't be blank.
     * @param cause the exception that caused this exception to be created. Can be null.
     */
    public FileVersionException(String sourceFilePath, String sourceVersion, String expectedVersion, Throwable cause) {
        super(cause);

        if (StringUtils.isBlank(sourceFilePath)) {
            throw new IllegalArgumentException("The filePath can't be null or empty.");
        } else if (StringUtils.isBlank(sourceVersion)) {
            throw new IllegalArgumentException("The sourceVersion can't be null or empty.");
        } else if (StringUtils.isBlank(expectedVersion)) {
            throw new IllegalArgumentException("The expectedVersion can't be null or empty.");
        }

        this.versionExceptionType = VersionExceptionType.NOT_EXPECTED_VERSION;
        this.message = "The file '" + sourceFilePath + "' is not the expected version. The file is version '"
                + sourceVersion + "' but the expected version is '" + expectedVersion + "'.";
    }

    /**
     * Create an exception for when two files do not have matching versions.
     * 
     * @param sourceFilePath the path of the first file that failed the version check. Can be
     *        absolute or relative. Can't be blank.
     * @param sourceVersion the version of the first file that failed the version check. Can't be
     *        blank.
     * @param targetFilePath the path of the second file that failed the version check. Can be
     *        absolute or relative. Can't be blank.
     * @param targetVersion the version of the second file that failed the version check. Can't be
     *        blank.
     * @param cause the exception that caused this exception to be created. Can be null.
     */
    public FileVersionException(String sourceFilePath, String sourceVersion, String targetFilePath,
            String targetVersion, Throwable cause) {
        super(cause);

        if (StringUtils.isBlank(sourceFilePath)) {
            throw new IllegalArgumentException("The sourceFilePath can't be null or empty.");
        } else if (StringUtils.isBlank(sourceVersion)) {
            throw new IllegalArgumentException("The sourceVersion can't be null or empty.");
        } else if (StringUtils.isBlank(targetFilePath)) {
            throw new IllegalArgumentException("The targetFilePath can't be null or empty.");
        } else if (StringUtils.isBlank(targetVersion)) {
            throw new IllegalArgumentException("The targetVersion can't be null or empty.");
        }

        this.versionExceptionType = VersionExceptionType.DIFFERENT_FILE_VERSIONS;
        this.message = "The files are different versions. The first file is version '" + sourceVersion
                + "' but the second file is version '" + targetVersion + "'. File 1: '" + sourceFilePath
                + "'; File 2: '" + targetFilePath + "'.";
    }

    @Override
    public String getMessage() {
        return message;
    }

    /**
     * Gets the type describing the cause of the file version exception.
     * 
     * @return the versionExceptionType the {@link VersionExceptionType}.
     */
    public VersionExceptionType getVersionExceptionType() {
        return versionExceptionType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[FileVersionException: ");
        sb.append("message = ").append(getMessage());
        sb.append(", versionExceptionType = ").append(getVersionExceptionType());
        sb.append("]");
        return sb.toString();
    }
}