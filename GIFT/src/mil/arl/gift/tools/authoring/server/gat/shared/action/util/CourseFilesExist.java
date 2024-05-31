/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import java.util.Set;

import mil.arl.gift.common.util.StringUtils;
import net.customware.gwt.dispatch.shared.Action;

/**
 * Checks if the provided course files exist within the course folder
 */
public class CourseFilesExist implements Action<CourseFilesExistResult> {

    /** The path of the course folder */
    private String courseFolderPath;

    /** The course files to search for relative to the course folder itself */
    private Set<String> courseFiles;

    /** The username */
    private String username;

    /**
     * Required for GWT serialization policy
     */
    private CourseFilesExist() {
    }

    /**
     * Constructor.
     * 
     * @param courseFolderPath the path of the course folder to search within. Can't be blank.
     * @param courseFiles the course files to search for relative to the course folder itself. Can't
     *        be null or empty.
     * @param username the user performing the operation. Can't be blank.
     */
    public CourseFilesExist(String courseFolderPath, Set<String> courseFiles, String username) {
        this();
        if (StringUtils.isBlank(courseFolderPath)) {
            throw new IllegalArgumentException("The parameter 'courseFolderPath' cannot be blank.");
        } else if (courseFiles == null) {
            throw new IllegalArgumentException("The parameter 'courseFiles' cannot be null.");
        } else if (courseFiles.isEmpty()) {
            throw new IllegalArgumentException("The parameter 'courseFiles' cannot be empty.");
        } else if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("The parameter 'username' cannot be blank.");
        }

        this.courseFiles = courseFiles;
        this.username = username;
    }

    /**
     * Gets the path of the course folder to search within
     *
     * @return the course folder path
     */
    public String getCourseFolderPath() {
        return courseFolderPath;
    }

    /**
     * Gets the course files.
     *
     * @return the course files
     */
    public Set<String> getCourseFiles() {
        return courseFiles;
    }

    /**
     * Gets the user name.
     * 
     * @return User name.
     */
    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[CourseFilesExist: ");
        sb.append(" courseFiles = [");
        StringUtils.join(", ", getCourseFiles(), sb);
        sb.append("]");
        sb.append(", userName = ").append(username);
        sb.append("]");

        return sb.toString();
    }
}
