/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.util.HashMap;
import java.util.Map;

import mil.arl.gift.common.DomainOption.DomainOptionPermissions;
import mil.arl.gift.common.enums.SharedCoursePermissionsEnum;

/**
 * Contains the permissions for all the users for this file.
 * 
 * @author sharrison
 */
public class FileProxyPermissions {
    
    /** Map containing the permission for each user that has access to the file */
    private Map<String, SharedCoursePermissionsEnum> userToPermission;

    /**
     * Constructor.
     * 
     * @param userToPermission the map of permission types for each user.  Can be null.
     */
    public FileProxyPermissions(Map<String, SharedCoursePermissionsEnum> userToPermission) {
        this.userToPermission = userToPermission;
    }

    /**
     * Retrieves the map that contains the permission for each user.
     * 
     * @return the permission for each user.  Will not be null but can be empty.
     */
    public Map<String, SharedCoursePermissionsEnum> getUserToPermission() {
        if (userToPermission == null) {
            userToPermission = new HashMap<>();
        }

        return userToPermission;
    }

    /**
     * Sets the map of users to permission types.
     * 
     * @param userToPermission the mapping of users to permissions. Can be null.
     */
    public void setUserToPermission(Map<String, SharedCoursePermissionsEnum> userToPermission) {
        this.userToPermission = userToPermission;
    }

    /**
     * Checks if the given user has read permissions.
     * 
     * @param user the user to check.
     * @return true if the user has read permissions; false otherwise.
     */
    public boolean hasReadPermissions(String user) {
        return SharedCoursePermissionsEnum.VIEW_COURSE.equals(getUserToPermission().get(user))
                || hasWritePermissions(user);
    }

    /**
     * Checks if the given user has write permissions.
     * 
     * @param user the user to check.
     * @return true if the user has write permissions; false otherwise.
     */
    public boolean hasWritePermissions(String user) {
        return SharedCoursePermissionsEnum.EDIT_COURSE.equals(getUserToPermission().get(user))
                || SharedCoursePermissionsEnum.EDIT_COURSE.equals(getUserToPermission().get(DomainOptionPermissions.ALL_USERS));
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[FileProxyPermissions: ");
        sb.append("userToPermissions = ").append(getUserToPermission());
        sb.append("]");
        return sb.toString();
    }
}
