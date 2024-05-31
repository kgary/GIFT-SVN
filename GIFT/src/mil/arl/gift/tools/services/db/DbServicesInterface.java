/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.services.db;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import generated.course.Concepts;
import mil.arl.gift.common.BranchPathHistory;
import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.CourseRecord;
import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.common.LMSCourseRecords;
import mil.arl.gift.common.LMSDataRequest;
import mil.arl.gift.common.SurveyCheckRequest;
import mil.arl.gift.common.course.AssessmentChainOfCustody;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.io.ProhibitedUserException;
import mil.arl.gift.common.lti.TrustedLtiConsumer;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.domain.DomainCourseFileHandler.CourseOptionsWrapper;
import mil.arl.gift.lms.impl.common.LmsInvalidCourseRecordException;
import mil.arl.gift.lms.impl.common.LmsInvalidStudentIdException;
import mil.arl.gift.lms.impl.common.LmsIoException;
import mil.arl.gift.ums.db.UMSDatabaseException;
import mil.arl.gift.ums.db.survey.Surveys.ExternalSurveyMapper;

/**
 * This class contains GIFT database (UMS, LMS) services to abstract the logic involved with 
 * the various deployment modes of GIFT.
 * 
 * @author mhoffman
 *
 */
public interface DbServicesInterface {
    
    /** used for progress dialog description when applying LMS records to courses */
    static final String LMS_RECORDS_PROGRESS_DESC = "Applying your learner records to these courses";
    
    /** used for progress dialog percentage when applying LMS records to courses */
    static final int LMS_RECORDS_PERCENT_COMPLETE = 100;

    /**
     * Retrieve LMS data for the request.  The request indicates a specific user to look up.
     * 
     * @param userId information about the user retrieving records for
     * @param dataRequest information used to query the LMS.  Can't be null.
     * @return the records found.  Will not be null.
     * @throws LmsIoException if data cannot be read for any reason other than invalid studentId
     * @throws LmsInvalidStudentIdException if studentId does not exist in the database
     * @throws LmsInvalidCourseRecordException if any of the records were not found
     */
    LMSCourseRecords getLMSData(int userId, LMSDataRequest dataRequest) throws LmsIoException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException;

    /**
     * Retrieve the branch path history information from the database.  
     * Note: if there is no database entry one will be created.
     * 
     * @param branchPathHistory contains the ids used to retrieve the database entry.  This will be updated
     * with information retrieved from the database.  Can't be null.
     */
    void getBranchPathHistory(BranchPathHistory branchPathHistory);
    
    /**
     * Update the branch path history information in the database.
     * Note: if there is no database entry one will be created.
     * 
     * @param branchPathHistory contains the information to place in the database.  Can't be null.
     * @throws DetailedException if there was a problem updating (or creating) the database entry
     */
    void updateBranchPathHistory(BranchPathHistory branchPathHistory) throws DetailedException;
    
    
    /**
     * Updates or creates an Lti User Record in the LtiUserRecord table based on the consumer key 
     * and consumer id field from an lti launch request.  If there is no existing entry for the lti
     * user, a new entry is created.  If an existing entry is found in the table, then the timestamp
     * value is updated to signal that an lti launch request is being processed for the user.
     * 
     * @param consumer The trusted lti consumer object containing the consumer key and unique name of the 
     * tool consumer.
     * @param consumerId The consumer id that identifies the lti user within the Tool Consumer.
     * @param timestamp The date/timestamp that the Tool Consumer launched the request.  
     * @return True if the record was successfully created or updated.  False if there was an error.
     */
    boolean updateOrCreateLtiUserRecord(TrustedLtiConsumer consumer, String consumerId, Date timestamp);
    
    /**
     * Gets the timestamp value for an existing lti user record.  The timestamp contains the data
     * of the most recent lti launch request.
     * 
     * @param consumerKey The consumer key of the tool consumer.
     * @param consumerId The consumer id that identifies the lit user within the Tool Consumer.
     * @return the timestamp of the last lti launch request for the lti user if found.  Null is returned if an error or if the user
     * cannot be found.
     */
    Date getTimestampForLtiUserRecord(String consumerKey, String consumerId);
    
    /**
     * Gets the course record data based on the source path of the course. 
     * 
     * @param coursePath The source path of the course.  e.g. Public/Hemorrhage Control Lesson/HemorrhageControl.course.xml
     * @return The course record object (if found).  Null is returned if the course is not found or if there is an error.
     */
    CourseRecord getCourseByPath(String coursePath);
    
    /**
     * Creates a course record in the database.  This inserts a new record in the 'course' database table.
     * 
     * @param courseRecord The course record object data to insert into the database.
     * @return courseRecord The course record object that was inserted into the database (if successful), null otherwise.
     */
    CourseRecord createCourseRecord(CourseRecord courseRecord);
    
    /**
     * Return the list of domain session log files names for all the domain sessions for the given course.
     * 
     * @param courseRecord contains information about a course to retrieve domain sessions for in the database
     * @return zero or more domain session log files names (e.g. domainSession329_uId1_2020-09-21_16-28-53.log).
     */
    List<String> getCourseDomainSessionLogFileNames(CourseRecord courseRecord);

    /**
     * Updates a course record in the database. This updates an existing record
     * in the 'course' database table.
     * 
     * @param username the user requesting the update. Used to check for
     *        permission to modify the course. Can't be null or empty.
     * @param courseRecord The course record object data to update in the
     *        database. Can't be null.
     * @return true if successful; false if there was an error or not
     *         successful.
     */
    boolean updateCourseRecord(String username, CourseRecord courseRecord);
    
    /**
     * Provides a new learner state that replaces an old learner state which was created during the execution
     * of a live session.  The new learner state could have been created because a concept's assessment level
     * has changed after the session was over, possibly due to an override by an observer/instructor.
     *  
     * @param knowledgeSession the past knowledge session details which include the host username and the assignment of users
     * to team members.  Can't be null or empty.
     * @param domainSession the domain session of the hosted live session where the learner state originated.
     * @param newLearnerState the new learner state that updates the old learner state.  A single attribute for a single
     * concept might be the only change that was made.  Can't be null.
     * @param oldLearnerState the original learner state that is being updated and came from the live session's historical record.
     * Can't be null.
     * @throws LmsIoException if there was a problem connecting too the LMS instance, invalidating records or inserting new records.
     */
    void pastSessionLearnerStateUpdated(AbstractKnowledgeSession knowledgeSession, DomainSession domainSession, 
            LearnerState newLearnerState, LearnerState oldLearnerState) throws LmsIoException;

    /**
     * Provides a new LMS Course Record (graded score node) that replaces an older LMS Course Record which was created during
     * the execution of a live session.  The new course record could have been created as the result of using the Game 
     * Master Past Session tool after the session was over.
     * @param knowledgeSession the past knowledge session details which include the host username and the assignment of users
     * to team members.  Can't be null or empty.
     * @param chainOfCustody contains information about the session and the GIFT instance that produced the assessment. Can't be null.
     * @param newCourseRecord the new course record that updates the old course record.  A single attribute for a single
     * score node might be the only change that was made.  Can't be null.
     * @param oldCourseRecord the original learner state that is being updated and came from the live session's historical record.
     * Can't be null.
     * @param concepts the course concepts as a hierarchy. Can be null.
     * @throws LmsIoException if there was a problem connecting too the LMS instance, invalidating records or inserting new records.
     */
    public abstract void pastSessionCourseRecordUpdated(AbstractKnowledgeSession knowledgeSession, AssessmentChainOfCustody chainOfCustody,
            LMSCourseRecord newCourseRecord, LMSCourseRecord oldCourseRecord, Concepts.Hierarchy concepts) throws LmsIoException;

    /**
     * Gets the course record data based on the course id (uuid).
     * 
     * @param courseId The UUID of the course.
     * @param byPassDeletedStatus Set to false (most normal case) to only fetch the record if it is not deleted. Set to true to fetch the course even if record has
     * a deleted flag set.  
     * @return The course record object (if found).  Null is returned if the course is not found or if there is an error.
     */
    CourseRecord getCourseById(String courseId, Boolean byPassDeletedStatus);
    
    /**
     * Creates the course record if needed for specific user based on the course source path.
     * If the record exists, or is created, true is returned.  If there is an error, false is returned.
     * 
     * @param userName The username that is the owner of the course.  '*' is used to indicate that it is accessible by 'all' users
     * which is primarily used in desktop mode or for public courses.
     * @param relativeFilePath The source course path (relative path) to the course.  e.g. mhoffman/4879 test new1 - Copy/4879 test new1 - Copy.course.xml<br/>
     * Note: should only contain forward slash folder separators to be consistant across desktop and server mode and what is in the DomainSession table db rows.
     * @param isModifiable True if the user has permissions to modify the file, false otherwise.
     * @return The course record object for the course, null if there was an error.
     */
    CourseRecord createCourseRecordIfNeeded(String userName, String relativeFilePath, boolean isModifiable);

    /**
     * Deletes the course record based on the relative file path of the course.
     * This typically should be called when a course is being deleted.
     * 
     * @param coursePath The relative path of the course to be deleted.
     * 
     * @return True if the course record was deleted, false otherwise.
     */
    boolean deleteCourseRecord(String coursePath);

    /**
     * Retrieve the latest root LMS data for all of the courses taken by a specific user.
     * 
     * @param userId information about the user retrieving records for
     * @param username the username of the user retrieving records for.  Can't be null.
     * @return the records found.  Will not be null.
     * @throws LmsIoException if data cannot be read for any reason other than invalid studentId
     * @throws LmsInvalidStudentIdException if studentId does not exist in the database
     * @throws LmsInvalidCourseRecordException if any of the records were not found
     */
	LMSCourseRecords getLatestRootLMSDataPerDomain(int userId, String username)
			throws LmsIoException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException;
	
	/**
	 * Return whether the survey context specified has changed since the last successful course validation
	 * for the course referenced.
	 * 
	 * @param lastSuccessfulValidation the date of the last successful course validation to use to compare to the
	 * survey context last modification date.
	 * @param surveyContextId the unique id of the course's survey context
	 * @param username used for authentication both on retrieving the course and survey context information
	 * @return true if the survey context last modification date is after the last successful course validation date.  If 
	 * either date is not set, true will be returned.
	 */
	boolean hasSurveyContextChangeSinceLastValidation(Date lastSuccessfulValidation, Integer surveyContextId, String username);
	
	/**
	 * Return the date of the last modification for the survey context specified. 
	 * 
	 * @param surveyContextId the unique id of a survey context for a course
	 * @param username used for authentication to see if the user has read access to the survey context
	 * @return the date value of the last modification for the survey context.  Can be null if the value has not been
	 * saved yet.
	 */
	Date getSurveyContextLastModifiedDate(Integer surveyContextId, String username);
	
    /**
     * Apply a recommendation level to the course provided based on LMS information found for the user.
     *
     * @param username information used to authenticate the request.  In this case this information is used
     * to determine what courses the user has Read access too.  It is also used for LMS lookups.
     * @param domainOption the course to set recommendation information about. Can't be null.
     * @throws LmsInvalidCourseRecordException if any of the records were not found
     * @throws LmsInvalidStudentIdException if user does not exist in the database
     * @throws LmsIoException if data cannot be read for any reason other than invalid user name
     * @throws UMSDatabaseException there was an issue retrieving the user
     * @throws FileValidationException if there was a problem with the LMS connections file
     * @throws ConfigurationException if there was a problem connecting to the UMS to get the user information
     * @throws ProhibitedUserException if the username is on the prohibited list, meaning the username can't be used in this gift instance (e.g. Public).
     */
    void applyLMSRecommendations(String username, DomainOption domainOption)
            throws LmsIoException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException, UMSDatabaseException,
            FileValidationException, ConfigurationException, ProhibitedUserException;
    
    /**
     * Apply a recommendation level to all the courses provided based on LMS information found for the user.
     *
     * @param username information used to authenticate the request.  In this case this information is used
     * to determine what courses the user has Read access too.  It is also used for LMS lookups.
     * @param courseOptionsWrapper the object to populate with information about the course found in the domain directory. Can't be null.
     * @throws LmsInvalidCourseRecordException if any of the records were not found
     * @throws LmsInvalidStudentIdException if user does not exist in the database
     * @throws LmsIoException if data cannot be read for any reason other than invalid user name
     * @throws UMSDatabaseException there was an issue retrieving the user
     * @throws FileValidationException if there was a problem with the LMS connections file
     * @throws ConfigurationException if there was a problem connecting to the UMS to get the user information
     * @throws ProhibitedUserException if the username is on the prohibited list, meaning the username can't be used in this gift instance (e.g. Public).
     */
    public void applyLMSRecommendations(String username, CourseOptionsWrapper courseOptionsWrapper)
            throws LmsIoException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException, UMSDatabaseException,
            FileValidationException, ConfigurationException, ProhibitedUserException;
    
    /**
     * Gather references to survey elements in the course provided.
     *
     * @param domainOption the course to analyze. Can't be null.
     * @param courseOptionsWrapper contains information about all known courses.  Can't be null.
     * @param surveyValidationRequests where to place the survey check requests for survey elements found in the course specified.  Can't be null.
     */
    public void gatherCourseSurveyReferences(DomainOption domainOption, CourseOptionsWrapper courseOptionsWrapper, 
            Map<String, List<SurveyCheckRequest>> surveyValidationRequests);
    
    /**
     * Validate references to survey elements in the course provided.
     *
     * @param domainOption the course to check survey references for
     * @param courseOptionsWrapper contains information about all known courses.  Can't be null.
     */
    public void checkCourseSurveyReferences(DomainOption domainOption, CourseOptionsWrapper courseOptionsWrapper);
    
    /**
     * Validate references to survey elements in the courses provided.
     *
     * @param courseOptionsWrapper contains information about all known courses.  Can't be null.
     */
    public void checkCourseSurveyReferences(CourseOptionsWrapper courseOptionsWrapper);
    
    /**
     * To be used after a course is copied. This method copies survey references stored in the mapper to the specified course. Also updates survey references in the dkfs.
     *
     * @param username The user copying the course
     * @param course The path to the course.xml file that needs the updated references.
     * The root of the path must be a workspace sub-folder (e.g. Public, &lt;username&gt;).
     * @param courseName A new name for the course
     * @param mapper Contains the mapping of original to new survey item ids
     * @param filesToUpdate A collection of files needing to be updated, i.e. written to disk
     * @return mapping of file names (full path) to the new content that needs to be written to that file (e.g. because of a survey id reference updated)
     * @throws DetailedException If there was a problem copying the course survey references
     */
    public Map<String, String> copyCourseSurveyReferences(String username, String course, String courseName, AbstractFolderProxy courseFolder,
            ExternalSurveyMapper mapper,  HashMap<FileProxy, AbstractSchemaHandler> filesToUpdate) throws DetailedException;

    /**
     * Gets a list of the names of all connected LRS instances. This can be handy for reporting errors
     * related to LRS instances.
     * 
     * @return the list of LRS names. Will not be null, but can be empty.
     */
    public List<String> getLrsNames();
}
