/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import java.util.HashMap;
import java.util.Map;

import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * The result of the CourseFilesExist action
 */
public class CourseFilesExistResult extends GatServiceResult {

    /** The map containing the checked files and if they exist within the course folder. */
    private Map<String, Boolean> filesExistMap = new HashMap<>();

    /**
     * Constructor
     * 
     */
    public CourseFilesExistResult() {
        super();
    }

    /**
     * Add a result of the file check
     * 
     * @param file the file that was checked
     * @param existsInCourseFolder true if the file exists within the course folder; false
     *        otherwise.
     */
    public void addFileResult(String file, boolean existsInCourseFolder) {
        if (StringUtils.isBlank(file)) {
            throw new IllegalArgumentException("The parameter 'file' cannot be blank.");
        }

        filesExistMap.put(file, existsInCourseFolder);
    }

    /**
     * Gets the map containing the checked files and if they exist within the course folder
     *
     * @return the map containing the checked files and if they exist within the course folder
     */
    public Map<String, Boolean> getFilesExistMap() {
        return filesExistMap;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[CourseFilesExistResult: ");
        sb.append(super.toString());
        sb.append(", filesExistMap = ").append(getFilesExistMap());
        sb.append("]");

        return sb.toString();
    }
}
