/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course;

import java.io.Serializable;
import java.util.HashMap;

import mil.arl.gift.common.enums.SharedCoursePermissionsEnum;
import mil.arl.gift.common.util.StringUtils;

/**
 * Information about the user(s) accessing a specific course file (e.g. course.xml, dkf.xml...)
 * in a course folder.
 * 
 * @author mhoffman
 *
 */
public class CourseFileAccessDetails implements Serializable {

    /**
     * Acts as a key that uniquely identifies a specific user using
     * a specific browser
     * @author tflowers
     *
     */
    public static class FileUserPermissionsKey implements Serializable {

        private static final long serialVersionUID = 1L;
        
        /** The name of the user that is being identified */
        private String username = null;
        
        /** The unique identifier of the browser that the user is using */
        private String browserSessionKey = null;
        
        /**
         * No arg constructor for GWT serialization
         */
        private FileUserPermissionsKey() {
            
        }
        
        /** 
         * Constructor that takes values for the username and the browser identifier
         * @param username the name of the user that is being identified by the key
         * @param browserSessionKey the identifier of the browser that is being by the key, can't be null or empty
         */
        public FileUserPermissionsKey(String username, String browserSessionKey) {
            this();
            setUsername(username);
            setBrowserSessionKey(browserSessionKey);
        }
        
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof FileUserPermissionsKey) {
                FileUserPermissionsKey other = (FileUserPermissionsKey) obj;
                return other.getUsername().equals(username) &&
                        other.getBrowserSessionKey().equals(browserSessionKey);
            } else {
                return super.equals(obj);                
            }
        }
        
        @Override
        public int hashCode() {
            int userHash = username != null ? username.hashCode() : 0;
            int bskHash = browserSessionKey != null ? browserSessionKey.hashCode() : 0;
            
            return (userHash << 1) + bskHash;
        }
        
        /**
         * Gets the username of the user being identified by this key
         * @return the value of the user's name, never null
         */
        public String getUsername() {
            return username;
        }
        
        /**
         * Gets the username of the user being identified by this key
         * @param username the new value of the user's name, can't be null or empty
         */
        private void setUsername(String username) {
            if(username == null || username.isEmpty()) {
                throw new IllegalArgumentException("The value of 'username' can't be null or empty");
            }
            
            this.username = username;
        }
        
        /**
         * Gets the unique identifier of the browser that the user is using
         * @return the value of the browser identifier, never null
         */
        public String getBrowserSessionKey() {
            return browserSessionKey;
        }
        
        /**
         * Sets the unique identifier of the browser that the user is using
         * @param browserSessionKey the new value of the browser identifier, can't be null or empty
         */
        private void setBrowserSessionKey(String browserSessionKey) {
            if(browserSessionKey == null || browserSessionKey.isEmpty()) {
                throw new IllegalArgumentException("The value of 'browserSessionKey' can't be null or empty");
            }
            
            this.browserSessionKey = browserSessionKey;
        }
        
        @Override
        public String toString() {
            return new StringBuilder("[FileUserPermissionsKey: ")
                    .append("username = ").append(getUsername())
                    .append(", browserSessionKey = ").append(getBrowserSessionKey())
                    .append("]").toString();
        }
    }
    
    private static final long serialVersionUID = 1L;

    /** unique identifier of a file in a course folder (i.e. includes workspace path and file name) */
    private String courseFileId;
    
    /** 
     * Mapping of gift usernames to the information about the access that user currently has on the file this 
     * class is maintaining access details on.
     */
    private HashMap<FileUserPermissionsKey, CourseFileUserPermissionsDetails> userToReadPermissions = new HashMap<>();
    
    /**
     * Required for GWT serialization.  Don't use.
     */
    @SuppressWarnings("unused")
    private CourseFileAccessDetails(){}
    
    /**
     * Set attribute(s).
     * 
     * @param courseFileId unique identifier of a file in a course folder (i.e. includes workspace path and file name).
     * Can't be null or empty.
     */
    public CourseFileAccessDetails(String courseFileId){
        setCourseFileId(courseFileId);
    }
    
    /**
     * Set the unique id of the file access being maintained in this class.
     * 
     * @param courseFileId unique identifier of a file in a course folder (i.e. includes workspace path and file name).
     * Can't be null or empty.
     */
    private void setCourseFileId(String courseFileId){
        
        if(courseFileId == null || courseFileId.isEmpty()){
            throw new IllegalArgumentException("The course file id can't be null or empty.");
        }
        
        this.courseFileId = courseFileId;
    }
    
    /**
     * The id of the course file that this class contains access information for.
     * 
     * @return won't be null or empty.
     */
    public String getCourseFileId(){
        return courseFileId;
    }
    
    /**
     * The mapping of gift user to the current access to this course.
     * 
     * @return can be empty but won't be null
     */
    public HashMap<FileUserPermissionsKey, CourseFileUserPermissionsDetails> getUsersPermissions(){
        return userToReadPermissions;
    }
    
    /**
     * Return whether any users are currently accessing the course file with write permissions.
     * 
     * @return true if any user is accessing the course file with write permissions.
     */
    public boolean isLocked(){
        
        boolean locked = false;
        synchronized (userToReadPermissions) {
            for(FileUserPermissionsKey key : userToReadPermissions.keySet()){
                
                CourseFileUserPermissionsDetails details = userToReadPermissions.get(key);
                if(details.hasWritePermissions()){
                    locked = true;
                    break;                         
                }
            }
        }
        return locked;
    }
    
    /**
     * Determines whether or not the specified user using the specified browser has 
     * acquired write permissions for the course
     * @param username the name of the user to test writer permissions for
     * @param browserSessionKey the identifier of the browser to test permissions for
     * @return true if the user using that browser has acquired write permission for the course, false otherwise
     */
    public boolean userHasWritePermission(String username, String browserSessionKey) {
        for(CourseFileUserPermissionsDetails details : userToReadPermissions.values()) {
            boolean usernameMatches = details.getUsername().equals(username);
            boolean bskMatches = details.getBrowserSessionKey().equals(browserSessionKey);
            
            if(usernameMatches && bskMatches && details.hasWritePermissions()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Add information about a user accessing the course file being tracked by
     * this class instance.
     * 
     * @param username the name of the user that the permission is being added
     *            for
     * @param browserSessionKey the unique identifier of the browser that the
     *            permission is being requested for, can't be null or empty
     * @param requestedPermission the permission that is being requested for the
     *            user and browser
     * 
     * @param courseUserPermissionsDetails details about access for a user on a
     *            particular course file. If null this method does nothing. This
     *            will over-ride any pre-existing access details for the user
     *            defined in the provided object.
     */
    public void addUserPermissions(String username, String browserSessionKey, SharedCoursePermissionsEnum requestedPermission){
        
        FileUserPermissionsKey key = new FileUserPermissionsKey(username, browserSessionKey);
        CourseFileUserPermissionsDetails desiredPermissions = new CourseFileUserPermissionsDetails(username, browserSessionKey, requestedPermission);
        CourseFileUserPermissionsDetails readOnlyPermissions = new CourseFileUserPermissionsDetails(username, browserSessionKey, SharedCoursePermissionsEnum.VIEW_COURSE);
        
        synchronized (userToReadPermissions) {
            
            boolean userHasLock = false;
            
            //check if the file is currently locked by this user
            CourseFileUserPermissionsDetails currentPermissions = userToReadPermissions.get(key);
            if(currentPermissions != null) {
                userHasLock = currentPermissions.hasWritePermissions();
            }
            
            if(desiredPermissions.hasWritePermissions() && !userHasLock && isLocked()) {
                
                //if this user has requested write permissions but the file is locked by another user,
                //then only give them read-only permissions
                userToReadPermissions.put(key, readOnlyPermissions);
                
            } else {
                userToReadPermissions.put(key, desiredPermissions);
            }
        }
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[CourseFileAccessDetails: ");
        sb.append("fileId = ").append(getCourseFileId());
        sb.append(", users = {\n");
        sb.append(StringUtils.join(",\n", userToReadPermissions.entrySet()));
        sb.append("}");
        
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Contains a single users access information including lock timestamps.
     * 
     * @author mhoffman
     *
     */
    public static class CourseFileUserPermissionsDetails implements Serializable{
        
        private static final long serialVersionUID = 1L;
        
        /** the username of the user whose file access is being tracked in this object. */
        private String username;
        
        /** the identifier for the browser which the user is accessing the course from*/
        private String browserSessionKey;
        
        /** The epoch time at which the lock was granted */
        private long lockGrantedTime;
        
        /** the epoch time for the last time the lock was renewed for this user for the file in the course folder */
        private long lastLockTime;
        
        private SharedCoursePermissionsEnum permissions;
        
        /**
         * Required for GWT serialization.  Don't use.
         */
        @SuppressWarnings("unused")
        private CourseFileUserPermissionsDetails() {}

        /**
         * Set attribute(s).
         * 
         * @param username the username of the user whose file access is being tracked in this object. Can't be null or empty.
         * @param browserSessionKey the unique identifier of the browser whose file access is being tracked in this object. Can't be null or empty
         * @param permissions the permissions that have been granted to the specified user within the specified browser
         */
        public CourseFileUserPermissionsDetails(String username, String browserSessionKey, SharedCoursePermissionsEnum permissions){
            if(permissions == null) {
                throw new IllegalArgumentException("The 'permissions' parameter can't be null");
            }
            
            setUsername(username);
            setBrowserSessionKey(browserSessionKey);
            
            
            this.permissions = permissions;
            
            this.lockGrantedTime = System.currentTimeMillis();
            this.lastLockTime = lockGrantedTime;
        }
        
        /**
         * Set the username of the user whose file access is being tracked in this object.
         * 
         * @param username can't be null or empty.
         */
        private void setUsername(String username){
            
            if(username == null || username.isEmpty()){
                throw new IllegalArgumentException("The username id can't be null or empty.");
            }
            
            this.username = username;
        }
        
        /**
         * The gift user for which this course permission is for.
         * 
         * @return won't be null or empty.
         */
        public String getUsername(){
            return username;
        }
        
        /**
         * The time at which the lock was granted.
         * 
         * @return the epoch time when this object was created.  See last lock time for the time when
         * the last lock renewal happened.
         */
        public long getLockGrantedTime(){
            return lockGrantedTime;
        }
        
        /**
         * Gets the identifier for the browser that the user is accessing the course from
         * @return the value of the browser session key, never null
         */
        public String getBrowserSessionKey() {
            return browserSessionKey;
        }
        
        /**
         * Sets the identifier for the browser that the user is accessing the course from
         * @param browserSessionKey the value of the browser session key, can't be null or empty
         */
        private void setBrowserSessionKey(String browserSessionKey) {
            if(browserSessionKey == null || browserSessionKey.isEmpty()) {
                throw new IllegalArgumentException("The value of 'browserSessionKey' can't be null or empty");
            }
            
            this.browserSessionKey = browserSessionKey;
        }
        
        /**
         * Return the epoch time for the last time the lock was renewed for this user for the file in the course folder.
         * 
         * @return epoch time of the last lock update.  By default this will equal the lock granted time.
         */
        public long getLastLockTime(){
            return lastLockTime;
        }
        
        /**
         * Set the last lock time to the current epoch time.
         */
        public void updateLastLockTime(){
            lastLockTime = System.currentTimeMillis();
        }
        
        /**
         * Return whether this user has write access to the file in the course folder.
         * 
         * @return true if the user has write access.
         */
        public boolean hasWritePermissions(){
            return SharedCoursePermissionsEnum.EDIT_COURSE.equals(permissions);
        }
        
        /**
         * Returns the permissions that the user has for the given course file
         * @return an enumeration for the permissions the user has over this course file
         */
        public SharedCoursePermissionsEnum getPermissions() {
            return permissions;
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[CourseFileUserPermissionsDetails: ");
            sb.append("user = ").append(getUsername());
            sb.append(", browserSessionKey = ").append(getBrowserSessionKey());
            sb.append(", write = ").append(hasWritePermissions());
            sb.append(", granted = ").append(getLockGrantedTime());
            sb.append(", lastLockTime = ").append(getLastLockTime());
            sb.append("]");
            return sb.toString();
        }
    }
}
