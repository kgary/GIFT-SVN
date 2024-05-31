/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.services.file;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.course.CourseFileAccessDetails;
import mil.arl.gift.common.course.CourseFileAccessDetails.CourseFileUserPermissionsDetails;
import mil.arl.gift.common.course.CourseFileAccessDetails.FileUserPermissionsKey;
import mil.arl.gift.common.enums.SharedCoursePermissionsEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileTreeModel;

/**
 * Use this class to manage the locking and unlocking of files. You first
 * acquire a lock by calling the lock method with True for the acquisition
 * parameter. From then on it is your responsibility to continue to renew the
 * lock via calls to the lock method with False for the acquisition. If a
 * lock hasn't been renewed in often enough then it is at risk
 * of being automatically released by subsequent interactions with the
 * lock manager. To manually release a lock just call the unlock method.
 * 
 * NOTE: Originally, I wanted the lock and release process to be a completely
 * manually process. I hate the idea of forcing the client to constantly renew
 * the lock over and over again. Unfortunately if the user closes the browser
 * GWT doesn't give us enough time for the client to tell the server to release
 * the lock. In that case the lock would remain engaged forever. So this is the
 * workaround, if you can think of something better then please do.
 * @author elafave
 *
 */
public class FileLockManager {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(FileLockManager.class);
    
    /** 
     * Amount of time to elapse before automatically removing a lock 
     */
    private static final long LOCK_TIMEOUT_MS = 90000;
    
    /**
     * amount of time to wait before checking for stale locks again
     */
    private static final long MIN_MS_BETWEEN_STALE_CHECKS = 100;
    
    /** when the last check for stale locks was performed */
    private long lastStaleCheckEpoch = System.currentTimeMillis();

    /** The singleton instance of the {@link FileLockManager} */
	private static FileLockManager instance = null;
	
	/**
	 * Maps the workspace relative path of a locked file to the information about the users 
	 * currently accessing that file.
	 * e.g. mhoffman/test/test.course.xml, Public/Hello World/hello world.course.xml
	 * 
	 * I can see client requests come in on their own threads. I don't see any
	 * code that deals with a multi-threaded code so for the sake of safety I
	 * used a ConcurrentHashMap.
	 */
	private ConcurrentHashMap<String, CourseFileAccessDetails> filePathToLockTimeMap = new ConcurrentHashMap<String, CourseFileAccessDetails>();
	
    /**
     * Private constructor used to enforce singleton behavior for the
     * {@link FileLockManager} class
     */
	private FileLockManager() {	}
	
	/**
	 * Get the instance of this singleton.
	 * @return The one and only instance of this class.
	 */
	public static FileLockManager getInstance() { 
		if(instance == null) {
			instance = new FileLockManager();
		}
		return instance;
	}
	
	/**
     * Determines if the given file is locked.
     * 
     * @param file the file whose lock status is in question. Can't be null or a
     * directory.
     * @param username the user whose access we are looking up
     * @return True if the file is locked, false otherwise.
	 * @throws IllegalArgumentException if the file is a directory, if the file is null, or if the name of the file is null
     */
    public boolean isLocked(FileTreeModel file, String username) throws IllegalArgumentException {
        releaseStaleLocks();
        
        if(file == null){
            throw new IllegalArgumentException("The file can't be null.");
        }else if(file.getRelativePathFromRoot() == null){
            throw new IllegalArgumentException("The file name can't be null.");
        }else if(file.isDirectory()){
            throw new IllegalArgumentException("The file can't be a directory.");
        }
               
        return isLocked(file.getRelativePathFromRoot(), username);
    }
    
    /**
     * Determines if the given file is locked.
     * 
     * @param workspaceRelativePath the file whose lock status is in question.
     * Can't be null or empty.
     * @param username the user whose access we are looking up
     * @return True if the file is locked, false otherwise.
     * @throws IllegalArgumentException if the workspaceRelativePath is null or empty
     */
    public boolean isLocked(String workspaceRelativePath, String username) throws IllegalArgumentException {
        releaseStaleLocks();
        
        if(workspaceRelativePath == null || workspaceRelativePath.isEmpty()){
            throw new IllegalArgumentException("The file can't be null or empty.");
        }
               
        CourseFileAccessDetails courseAccessDetails = filePathToLockTimeMap.get(workspaceRelativePath); 
        return courseAccessDetails != null && courseAccessDetails.isLocked();
    }

//    /**
//     * Determines if the given directory is locked.  A directory is locked if all descendant files
//     * are locked.
//     * 
//     * @param directory the directory model to check all descendant files for locks.  This must contain a reference
//     * to descendant files because a search of the file system for files will not be conducted.
//     * @return true if all descendant files are locked, false otherwise.
//     * @throws IllegalArgumentException if there was a problem with the method arguments or a file in the tree
//     */
//    public boolean isLockedAll(FileTreeModel directory) throws IllegalArgumentException {
//        
//        if(directory == null){
//            throw new IllegalArgumentException("The directory can't be null.");
//        }else if(!directory.isDirectory()){
//            throw new IllegalArgumentException("The directory can't be a file.");
//        } 
//        
//        List<FileTreeModel> descendants = new ArrayList<>();
//        
//        directory.getAllFileTreeModels(descendants);
//        
//        boolean allLocked = true;
//        for(FileTreeModel file : descendants){
//            
//            if(!file.isDirectory()){
//                
//                if(!isLocked(file)){
//                    allLocked = false;
//                    break;
//                }
//            }
//        }
//        
//        return allLocked;
//    }
    
//    /**
//     * Attempts to lock all descendant files under the directory.  If any file can't be locked all
//     * files locked in this method are unlocked.  Descendant directories will not be locked.
//     * 
//     * @param directory the directory model to lock all descendant files.  This must contain a reference
//     * to descendant files because a search of the file system for files will not be conducted.
//     * @param acquisition True if we're trying to acquire the lock for the
//     * first time, false if we're simply renewing the lock.
//     * @return False if all the files couldn't be locked (i.e. we were in
//     * acquisition mode and a file was already locked), true otherwise.
//     * @throws IllegalArgumentException if there was a problem with the method arguments or a file in the tree
//     */
//    public boolean lockAll(FileTreeModel directory, boolean acquisition) throws IllegalArgumentException{
//        
//        if(directory == null){
//            throw new IllegalArgumentException("The directory can't be null.");
//        }else if(!directory.isDirectory()){
//            throw new IllegalArgumentException("The directory can't be a file.");
//        }        
//        
//        if(logger.isInfoEnabled()){
//            logger.info("Locking all files under directory '"+directory.getRelativePathFromRoot()+'.');
//        }
//        
//        List<FileTreeModel> descendants = new ArrayList<>();
//        List<FileTreeModel> filesLocked = new ArrayList<>();
//        
//        directory.getAllFileTreeModels(descendants);
//        
//        boolean unlock = false;
//        for(FileTreeModel file : descendants){
//            
//            if(!file.isDirectory()){
//                //can't lock directories
//                
//                if(lock(file, acquisition)){
//                    filesLocked.add(file);
//                }else{
//                    //lock failed
//                    logger.info("Failed to acquire lock on file '"+file.getFileOrDirectoryName()+"' when trying to lock all files under directory '"+directory.getRelativePathFromRoot()+"'.");
//                    unlock = true;
//                    break;
//                }
//            }
//        }
//        
//        if(unlock){            
//            //unlock any files that were locked in this call
//
//            for(FileTreeModel lockedFile : filesLocked){
//                unlock(lockedFile);
//            }
//            
//            return false;
//        }
//        
//        return true;
//    }
    
    /**
     * Attempts to lock the given file.
     * 
     * @param file File that needs to be locked. Can't be null or a directory.
     * @param username the user wanting to access the file
     * @param browserSessionKey the identifier of the browser the request is
     * being made from
     * @param writeAccess whether or not write access is being requested for the
     * course file
     * @param initialAcquisition specifies whether this is an initial request to
     * acquire the lock or if it is a renewal
     * @return information about the users accessing the specified file
     * including the user provided. Will not be null.
     * @throws IllegalArgumentException if file is null, if file's relative path
     * from root is null, or if file is a directory
     * @throws IllegalArgumentException if the file is null, the name of the file is null, or the file is a directory
     */
    public CourseFileAccessDetails lock(FileTreeModel file, String username, String browserSessionKey, boolean writeAccess, boolean initialAcquisition) throws IllegalArgumentException {
        releaseStaleLocks();

        if (file == null) {
            throw new IllegalArgumentException("The file can't be null.");
        } else if (file.getRelativePathFromRoot() == null) {
            throw new IllegalArgumentException("The file name can't be null.");
        } else if (file.isDirectory()) {
            throw new IllegalArgumentException("The file can't be a directory.");
        }

        return lock(file.getRelativePathFromRoot(), username, browserSessionKey, writeAccess, initialAcquisition);
    }
    
    /**
     * Attempts to lock the given file.
     * 
     * @param workspaceRelativePath File that needs to be locked. Can't be null
     * or empty.  This can basically be any file path but the callers should be consistent in the format and path 
     * root folder location as a basic string compare will be used 
     * (e.g. a/b/c.txt does not equal b/c.txt even know it is the same file).
     * @param username the user wanting to access the file
     * @param browserSessionKey the unique identifier for the browser the user
     * is making the request from. Can't be null or empty.
     * @param writeAccess whether the user wanting access has permission to
     * write to the file. This doesn't guarantee that write access will be
     * guaranteed because another user with write permission could already be
     * accessing the file with write access.
     * @param initialAcquisition specifies whether this is an initial request to
     * acquire the lock or if it is a renewal
     * @return information about the users accessing the specified file
     * including the user provided. Will not be null.
     * @throws IllegalArgumentException if the workspaceRelativePath is null or empty
     * @throws DetailedException when initialAcquisition is true and the username user already has this course opened
     * in the same browser type (i.e. the same username, browser session id pair is already mapped to that file path) 
     */
    public CourseFileAccessDetails lock(String workspaceRelativePath, String username, String browserSessionKey, 
            boolean writeAccess, boolean initialAcquisition) throws IllegalArgumentException, DetailedException {
        releaseStaleLocks();
        
        if(logger.isDebugEnabled()){
            logger.debug("received request to lock file\n" + workspaceRelativePath + "\nusername = "+username+" : browser session key = "+browserSessionKey+" : hasWritePermissions = " + writeAccess);
        }
        
        //Validates the path to lock
        if(workspaceRelativePath == null || workspaceRelativePath.isEmpty()){
            throw new IllegalArgumentException("The file can't be null or empty.");
        }

        //Retrieves the access details for the specified file
        CourseFileAccessDetails courseAccessDetails;
        synchronized (filePathToLockTimeMap) {
            courseAccessDetails = filePathToLockTimeMap.get(workspaceRelativePath);
            if(courseAccessDetails == null){
                //create new object to maintain information about access to this file
                courseAccessDetails = new CourseFileAccessDetails(workspaceRelativePath);
                filePathToLockTimeMap.put(workspaceRelativePath, courseAccessDetails);
            }
        }

        // Retrieves the permissions, if any, that the user already has
        FileUserPermissionsKey key = new FileUserPermissionsKey(username, browserSessionKey);
        CourseFileUserPermissionsDetails courseUserPermissionsDetails = courseAccessDetails.getUsersPermissions().get(key);
        if(courseUserPermissionsDetails == null){
            
            SharedCoursePermissionsEnum permissions = writeAccess ?
                    SharedCoursePermissionsEnum.EDIT_COURSE :
                    SharedCoursePermissionsEnum.VIEW_COURSE;
            
            courseAccessDetails.addUserPermissions(username, browserSessionKey, permissions);
        } else if (!initialAcquisition) {
            //renewing lock so need to update timestamp
            courseUserPermissionsDetails.updateLastLockTime();
        } else {
            String reason = String.format("The user '%s' has already opened this course in the current browser",
                    username);
            String details = String.format(
                    "The user '%s' has previously logged in with browser '%s' already. This is likely due to the fact that the user has already opened this course for editing in another tab of the same browser",
                    username, browserSessionKey);
            throw new DetailedException(reason, details, null);
        }
        
        if(logger.isDebugEnabled()){
            logger.debug("Returning the following access details for "+username+"'s request to lock:\n"+courseAccessDetails);
        }

        return courseAccessDetails;
    }
	
    /**
     * Unlocks the given file if it is currently locked.
     * 
     * @param username the name of the user that is requesting the unlock, can't be null or empty
     * @param browserSessionKey the unique id of the browser that is requesting the unlock, can't be null or empty
     * @param file File that needs to be unlocked. Can't be null.
     * @throws IllegalArgumentException if there was a problem with the file
     */
    public void unlock(String username, String browserSessionKey, FileTreeModel file) throws IllegalArgumentException {
        releaseStaleLocks();
        
        if(file == null){
            throw new IllegalArgumentException("The file can't be null.");
        }else if(file.getRelativePathFromRoot() == null){
            throw new IllegalArgumentException("The file name can't be null.");
        }else if(file.isDirectory()){
            throw new IllegalArgumentException("The file can't be a directory.");
        }
        
        unlock(username, browserSessionKey, file.getRelativePathFromRoot());
    }
    
    /**
     * Unlocks the given file if it is currently locked.
     * 
     * @param username the name of the user who is requesting the unlock operation, can't be null or empty
     * @param browserSessionKey the unique id of the browser that is requesting the unlock operation, can't be null or empty
     * @param workspaceRelativePath File that needs to be unlocked.  Can't be null or empty.
     * @throws IllegalArgumentException if there was a problem with the file
     */
    public void unlock(String username, String browserSessionKey, String workspaceRelativePath) throws IllegalArgumentException {
        releaseStaleLocks();
        
        if(logger.isDebugEnabled()){
            logger.debug("unlock: file=" + workspaceRelativePath);
        }
        
        if(workspaceRelativePath == null){
            throw new IllegalArgumentException("The file can't be null or empty.");
        }

        FileUserPermissionsKey key = new FileUserPermissionsKey(username, browserSessionKey);
        synchronized(filePathToLockTimeMap) {
            // Get the object that tracks the locking for the specified file
            CourseFileAccessDetails courseDetails = filePathToLockTimeMap.get(workspaceRelativePath);
            
            // If there are no locks for the file, return early
            if(courseDetails == null) {
                return;
            }
            
            // Remove the permissions for the user-browser combo and check if the map is empty
            if(courseDetails.getUsersPermissions().remove(key) != null) {
                if(courseDetails.getUsersPermissions().isEmpty()) {
                    filePathToLockTimeMap.remove(workspaceRelativePath);
                }
            }
        }
    }
    
//    /**
//     * Unlocks all descendant files under the directory.
//     * 
//     * @param directory the directory model to unlock all descendant files.  This must contain a reference
//     * to descendant files because a search of the file system for files will not be conducted.
//     * @throws IllegalArgumentException if there was a problem with the method arguments or a file in the tree
//     */
//    public void unlockAll(FileTreeModel directory) throws IllegalArgumentException{
//        
//        if(directory == null){
//            throw new IllegalArgumentException("The directory can't be null.");
//        }else if(!directory.isDirectory()){
//            throw new IllegalArgumentException("The directory can't be a file.");
//        }        
//        
//        logger.info("Unlocking all files under directory '"+directory.getRelativePathFromRoot()+'.');
//        
//        List<FileTreeModel> descendants = new ArrayList<>();
//        
//        directory.getAllFileTreeModels(descendants);
//        
//        for(FileTreeModel file : descendants){
//            
//            if(!file.isDirectory()){
//                unlock(file);
//            }
//        }
//    }
	
	/**
	 * Removes any locks that haven't been locked in a while.
	 */
	private void releaseStaleLocks() {
		try {
			long currentTime = System.currentTimeMillis();
			
			if((currentTime - lastStaleCheckEpoch) < MIN_MS_BETWEEN_STALE_CHECKS){
			    //don't check for stale locks since it wasn't that long ago since the last check
			    return;
			}
			
			//update last checked time
			lastStaleCheckEpoch = currentTime;
			
			Iterator<Entry<String, CourseFileAccessDetails>> fileAccessItr = filePathToLockTimeMap.entrySet().iterator();
		    while (fileAccessItr.hasNext()) {
		        Entry<String, CourseFileAccessDetails> courseEntry = fileAccessItr.next();
		        CourseFileAccessDetails courseAccessDetails = courseEntry.getValue();
		        
		        Iterator<Entry<FileUserPermissionsKey, CourseFileUserPermissionsDetails>> userAccessItr = courseAccessDetails.getUsersPermissions().entrySet().iterator();
		        while(userAccessItr.hasNext()){
		            Entry<FileUserPermissionsKey, CourseFileUserPermissionsDetails> userEntry = userAccessItr.next();
		            CourseFileUserPermissionsDetails details = userEntry.getValue();
		            long lastLockTime = details.getLastLockTime();
		            
		            long delta = currentTime - lastLockTime;
		            
		            //If the file hasn't been locked for a while then
	                //release the lock.
	                if(delta >= LOCK_TIMEOUT_MS) {
	                    
	                    if(logger.isDebugEnabled()){
	                        logger.debug("Removing stale course access for course '"+courseAccessDetails.getCourseFileId()+"' for user " + details);
	                    }
	                    userAccessItr.remove();
	                }
		        }
		    	
		    	if(courseAccessDetails.getUsersPermissions().isEmpty()){
		    	    //remove lock details for course since no users are accessing it now
		    	    fileAccessItr.remove();
		    	}

		    }
		} catch(Exception e) {
			logger.error("Caught exception while releasing stale locks.", e);
		}
	}
	
	/**
     * Unlocks the all of the files that are currently locked to the given browser session
     * 
     * @param username the name of the user that is requesting the unlock, can't be null or empty
     * @param browserSessionKey the unique id of the browser that is requesting the unlock, can't be null or empty.
     * @throws IllegalArgumentException if there was a problem with the file
     */
	public void unlockAll(String username, String browserSessionKey) throws IllegalArgumentException {
        
        synchronized (filePathToLockTimeMap) {
            for(Entry<String, CourseFileAccessDetails> entry : filePathToLockTimeMap.entrySet()) {
                
                CourseFileAccessDetails value = entry.getValue();
                
                if(value != null &&  value.userHasWritePermission(username, browserSessionKey)) {
                    
                    //this file is currently locked for writing by this browser session, so unlock it
                    unlock(username, browserSessionKey, entry.getKey());
                }
            }
        }
    }
}
