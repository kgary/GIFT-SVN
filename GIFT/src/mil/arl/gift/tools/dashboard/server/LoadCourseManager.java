/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server;


import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.experiment.DataCollectionItem;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.domain.DomainCourseFileHandler.CourseOptionsWrapper;
import mil.arl.gift.tools.services.ServicesManager;

/**
 * The load course manager is responsible for managing the progress of each user's load course progress.
 * It contains a map of all users along with for each user, a map of each course and the progress of that course being loaded.
 * Users can have multiple load course operations going on at a single time.
 * 
 * @author nblomberg
 *
 */
public class LoadCourseManager {
	
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(LoadCourseManager.class);

	/** A single user can have multiple courses being loaded at once.  This is a mapping of the username to a secondary map of each course (and the load progress for each course). */
	private ConcurrentHashMap<String, ConcurrentHashMap<String, ProgressIndicator>> userToCourses = new ConcurrentHashMap<String, ConcurrentHashMap<String, ProgressIndicator>>();
	
	/** Singleton instance of this class*/
	private static LoadCourseManager singleton = null;
	
	
	/** Default constructor */
	private LoadCourseManager(){
		
	}

	/**
	 * Start loading the course for a specified user.  Loading involves moving the course data into the server side
	 * cache (Domain/runtime folder). 
	 * 
	 * @param username - name of the user that is loading the course (cannot be null).
	 * @param courseId - The domain id (course id) of the course to be loaded (cannot be null).
	 * @return String - The runtime course id that will be used to start the course.
	 * 
	 * @throws IllegalArgumentException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public String startCourseLoad(String username, String courseId) throws IllegalArgumentException, IOException, URISyntaxException {
	    
	    
	    if(logger.isTraceEnabled()){
	        logger.trace("Start Course Load for user: " + username + ", course id: " + courseId);
	    }
	    if(username == null){
            throw new IllegalArgumentException("Username cannot be null.");
        }
        
        if (courseId == null) {
            throw new IllegalArgumentException("CourseId cannot be null.");
        }
        
	    String runtimeCourseId = "";
	    CourseOptionsWrapper courseWrapper = new CourseOptionsWrapper();
        
        ProgressIndicator progress = new ProgressIndicator();
        progress.setTaskDescription("Loading"); //if the task description isn't set here 'null' is sometimes shown in the UI
        
        ConcurrentHashMap<String, ProgressIndicator> courseIdToProgress = userToCourses.get(username);
        
        // This can be null if the user hasn't started any course loads.
        if (courseIdToProgress == null) {
            
            if(logger.isTraceEnabled()){
                logger.trace("Creating new hash map for user: " + username);
            }
            courseIdToProgress = new ConcurrentHashMap<String, ProgressIndicator>();
            userToCourses.put(username,  courseIdToProgress);
            
        } 
       
        if(logger.isTraceEnabled()){
            logger.trace("Adding course id to hashmap: " + courseId + ", for user: " + username);
        }
        courseIdToProgress.put(courseId, progress);
        
        // The load course method is synchronous and will not return until the course is loaded (or an exception is thrown).
        // The destination folder will be placed at the root runtime folder level with the username in the root folder location.
        runtimeCourseId = ServicesManager.getInstance().getFileServices().loadCourse(username, courseId, courseWrapper, progress, username);
        
        return runtimeCourseId;
       
        
	}
	
    /**
     * Start loading the experiment. Loading involves moving the experiment
     * source course folder into the server side cache (Domain/runtime folder).
     * 
     * @param experiment the experiment to load. Can't be null.
     * @param sourceCourseXmlPath the relative path (with respect to the
     *        workspace folder) of the course xml that the experiment was
     *        published from. Can't be blank.
     * @param progressId the unique id that will be used to lookup the loading
     *        progress.
     * @return The runtime course path that will be used to start the course.
     * 
     * @throws IllegalArgumentException if a method parameter is invalid
     * @throws URISyntaxException if there was a problem build a URI to
     *         access the files
     */
    public String startExperimentLoad(DataCollectionItem experiment, String sourceCourseXmlPath, String progressId)
            throws IllegalArgumentException, URISyntaxException {
        if (logger.isTraceEnabled()) {
            StringBuilder sb = new StringBuilder("startExperimentLoad(");
            List<Object> params = Arrays.<Object>asList(experiment, sourceCourseXmlPath, progressId);
            StringUtils.join(", ", params, sb);
            logger.trace(sb.append(")").toString());
        }

        if (experiment == null) {
            throw new IllegalArgumentException("The parameter 'experiment' cannot be null.");
        } else if (StringUtils.isBlank(sourceCourseXmlPath)) {
            throw new IllegalArgumentException("The parameter 'sourceCourseXmlPath' cannot be blank.");
        }

        final String experimentUser = DashboardProperties.getInstance().getReadOnlyUser();

        CourseOptionsWrapper courseOptionsWrapper = new CourseOptionsWrapper();

        ProgressIndicator progress = new ProgressIndicator();
        progress.setTaskDescription("Loading");

        ConcurrentHashMap<String, ProgressIndicator> courseIdToProgress = userToCourses.get(experimentUser);

        // This can be null if the user hasn't started any course loads.
        if (courseIdToProgress == null) {
            if(logger.isDebugEnabled()){
                logger.debug("Creating new hash map for experiments.");
            }
            courseIdToProgress = new ConcurrentHashMap<String, ProgressIndicator>();
            userToCourses.put(experimentUser, courseIdToProgress);
        }

        courseIdToProgress.put(progressId, progress);

        if (experiment.isLegacyExperiment()) {
            /* Don't need to load since the experiment is in the legacy
             * experiment folder */
            progress.setPercentComplete(100);

            if (logger.isInfoEnabled()) {
                logger.info("Returning legacy experiment course folder: '" + experiment.getCourseFolder() + "'.");
            }

            return experiment.getCourseFolder();
        }

        /* The load experiment method is synchronous and will not return until
         * the experiment is loaded (or an exception is thrown). The destination
         * folder will be placed in the the root runtime experiments level with
         * the timestamped experiment id. */
        final String runtimeFolder = ServicesManager.getInstance().getFileServices().loadExperiment(experimentUser,
                experiment.getId(), sourceCourseXmlPath, courseOptionsWrapper, progress);

        if (logger.isInfoEnabled()) {
            logger.info("Returning experiment runtime folder: '" + runtimeFolder + "'.");
        }
        return runtimeFolder;
    }

	/**
     * Start loading the lti course for a specified user.  Loading involves moving the course data into the server side
     * cache (Domain/runtime folder). This is similar to the normal course load function but needs to use the internal
     * lti user for accessing the course information in server mode.  Also since there is no logged in user like normal,
     * the folder name that is placed in the root runtime level is based on unique identifier (within a limited character range 
     * to avoid exceeding the path limit) that is given to the lti user.
     * 
     * @param username - name of the user that is loading the course (cannot be null).
     * @param courseSourcePath - The domain id (source path) of the course to be loaded (cannot be null).
     * @param runtimeRootFolderName  The folder name that is placed at the root runtime folder location.  This must be unique and not collide with normal (non-lti) users.
     * @return String - The runtime course id that will be used to start the course.
     * 
     * @throws IllegalArgumentException
     * @throws IOException
     * @throws URISyntaxException
     */
    public String startLtiCourseLoad(String uniqueUserName, String courseSourcePath, String runtimeRootFolderName) throws IllegalArgumentException, IOException, URISyntaxException {
        
        
        if(logger.isDebugEnabled()){
            logger.debug("Start Course Load for user: " + uniqueUserName + ", course path: " + courseSourcePath);
        }
        if(uniqueUserName == null){
            throw new IllegalArgumentException("Username cannot be null.");
        }
        
        if (courseSourcePath == null) {
            throw new IllegalArgumentException("CourseId cannot be null.");
        }
        
        CourseOptionsWrapper courseWrapper = new CourseOptionsWrapper();
        
        ProgressIndicator progress = new ProgressIndicator();
        progress.setTaskDescription("Loading"); //if the task description isn't set here 'null' is sometimes shown in the UI
        
        ConcurrentHashMap<String, ProgressIndicator> courseIdToProgress = userToCourses.get(uniqueUserName);
        
        // This can be null if the user hasn't started any course loads.
        if (courseIdToProgress == null) {
            
            if(logger.isDebugEnabled()){
                logger.debug("Creating new hash map for user: " + uniqueUserName);
            }
            courseIdToProgress = new ConcurrentHashMap<String, ProgressIndicator>();
            userToCourses.put(uniqueUserName,  courseIdToProgress);
            
        } 
       
        if(logger.isDebugEnabled()){
            logger.debug("Adding course path to hashmap: " + courseSourcePath + ", for user: " + uniqueUserName);
        }
        courseIdToProgress.put(courseSourcePath, progress);

        final String readOnlyUser = DashboardProperties.getInstance().getReadOnlyUser();

        // The load course method is synchronous and will not return until the course is loaded (or an exception is thrown).
        // The course will be loaded (in server mode) using the internal lti user for course access.
        return ServicesManager.getInstance().getFileServices().loadLTICourse(readOnlyUser, courseSourcePath, courseWrapper, progress, runtimeRootFolderName);
    }
	
	/**
	 * Gets the current progress of the load course operation for a specified user & course.
	 * 
	 * @param username - The name of the user to query the progress for. If null, it will be assumed to be an experiment.
	 * @param courseId - The domain id (course id) of the course to query the progress for (cannot be null).
	 * @return ProgressIndicator - The current progress data of the load course operation.
	 */
	public ProgressIndicator getCourseLoadProgress(String username, String courseId){
	    
	    ProgressIndicator progress = null;

        if (username == null) {
            /* try the experiment user */
            username = DashboardProperties.getInstance().getReadOnlyUser();
        }

	    ConcurrentHashMap<String, ProgressIndicator> courseIdToProgress = userToCourses.get(username);
	    
	    if (courseIdToProgress != null) {
	        progress = courseIdToProgress.get(courseId);
	    }
		return progress;
	}
	
	/**
	 * Cancels the load course operation for a user and course, if one exists
	 * 
	 * @param username - the username of the user for whom to cancel the load course operation.
	 * @param courseid - The domain id (course id) to cancel the load course operation.
	 */
	public void cancelLoadCourse(String username, String courseId){
		
		if(username == null){
			throw new IllegalArgumentException("Username cannot be null.");
		}
		
		if (courseId == null) {
		    throw new IllegalArgumentException("CourseId cannot be null.");
		}
		
		ConcurrentHashMap<String, ProgressIndicator> courseIdToProgress = userToCourses.get(username);
        
        if (courseIdToProgress != null) {
            ProgressIndicator progress = courseIdToProgress.get(courseId);
            progress.setShouldCancel(true);
        }
		
	}
	
	/**
     * Remove the progress indicator maintained for the loading of the course
     * specified for the user.
     * 
     * @param username the username of the user loading a course. If null, it
     *        will be assumed to be trying to clean an experiment.
     * @param courseId the id of the course being loaded.
     */
	public void cleanup(String username, String courseId){

	    if (username == null) {
            /* try the experiment user */
            username = DashboardProperties.getInstance().getReadOnlyUser();
        }

	    ConcurrentHashMap<String, ProgressIndicator> courseIdToProgress = userToCourses.get(username);
	    if(courseIdToProgress != null){
	        courseIdToProgress.remove(courseId);
	        
	        if(courseIdToProgress.isEmpty()){
	            userToCourses.remove(username);
	        }
	    }
	}	
	
	
	/**
	 * Gets this class' singleton instance
	 * 
	 * @return the singleton instance
	 */
	public static LoadCourseManager getInstance(){
		
		if(singleton == null){
			singleton = new LoadCourseManager();
		}
		
		return singleton;
	}
}
