/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.lms.impl.common;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import generated.course.Concepts;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.KnowledgeSessionCreated;
import mil.arl.gift.common.LMSConnectionInfo;
import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.common.PedagogicalRequest;
import mil.arl.gift.common.SubmitSurveyResults;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.course.AssessmentChainOfCustody;
import mil.arl.gift.common.course.CourseRecordRef;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.LessonCompleted;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.common.survey.score.AbstractScale;
import mil.arl.gift.common.ta.request.EnvironmentControl;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;

/**
 * A guess as to the abstract interface of a minimal LMS
 * 
 * @author cragusa
 *
 */
public abstract class AbstractLms {
    
    /** a display name for this LMS */
    private String name;
    
    /** information about the LMS connection */
    protected LMSConnectionInfo connectionInfo = null;
    
    /** (optional) parameters from the LMS connections file */
    protected generated.lms.Parameters parameters = null;
    
    /**
     * Set a display name for this LMS.
     * 
     * @param name the name to use for this LMS
     */
    public void setName(String name){
        this.name = name;
    }
    
    /**
     * Return the display name for this LMS.
     * 
     * @return String the name for this LMS
     */
    public String getName(){
        return name;
    }
	
    /**
     * Used to establish a connection to the LMS.
     * 
     * @param parameters used to configure the LMS connection.  Can be null. 
     * @throws LmsIoException if there was a problem connecting
     */
	abstract public void connect(generated.lms.Parameters parameters) throws LmsIoException;
	
	/**
	 * Used to disconnect from the LMS.
	 * 
	 * @throws LmsIoException if there was a problem disconnecting
	 */
	abstract public void disconnect() throws LmsIoException;

	/**
	 * Create a new student entry
	 * 
	 * @param userId - unique id of a LMS user
	 * @throws LmsInvalidStudentIdException if the student id is invalid
	 * @throws LmsIoException if there was a problem creating the student
	 */
	abstract public void createUser(String userId) 
		throws LmsInvalidStudentIdException, LmsIoException;
	
	/**
	 * Return the course records for the student with the given id
	 * 
	 * @param studentId unique LMS user name
	 * @param userId unique GIFT user id
	 * @param pageStart the index of the first record returned in this request.  For example if the request
     * should return the 5th and onward records, the value should be 4 (zero based index). Must be non-negative.  
     * Zero indicates to start with the first record that satisfies the request requirements.
	 * @param pageSize how many records to return, must be non-negative.  Zero indicates to return all records that satisfy the request requirements.
	 * @param sortDescending whether to sort the records by date with the latest records first
	 * @param domainIds used to filter for specific LMS records on domains/courses, can be null or empty to indicate that the field is not used.
	 * @param domainSessionIds used to filter for specific LMS records with the given domain session ids.  Can be null or empty to indicate that 
	 * this field is not used.
	 * @return the course records found.  Can be empty but not null.
	 * @throws LmsIoException if data cannot be read for any reason other than invalid studentId
	 * @throws LmsInvalidStudentIdException if studentId does not exist in the database
	 */
	abstract public List<LMSCourseRecord> getCourseRecords(String studentId, int userId, int pageStart, int pageSize, boolean sortDescending, 
	        Set<String> domainIds, Set<Integer> domainSessionIds)
		throws LmsIoException, LmsInvalidStudentIdException;
	
	/**
     * Return the course record for the record with the given id
     * 
     * @param studentId unique LMS user name
     * @param userId unique GIFT user id
     * @param recordRef reference to a course record to retrieve LMS data on.  Can't be null.
     * @return LMSCourseRecord the course record found. Can't be null.
     * @throws LmsIoException if data cannot be read for any reason other than invalid studentId
     * @throws LmsInvalidStudentIdException if studentId does not exist in the database
	 * @throws LmsInvalidCourseRecordException if the record was not found
     */
    abstract public LMSCourseRecord getCourseRecord(String studentId, int userId, CourseRecordRef recordRef)
        throws LmsIoException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException;
    
	/**
	 * Return the course records for the given course record ids
	 * 
	 * @param studentId unique LMS user name
	 * @param userId unique GIFT user id
	 * @param recordRefs references to one or more course records to retrieve LMS data on.
	 * @return List<LMSCourseRecord> the course records found. Can be empty but not null.
	 * @throws LmsIoException if data cannot be read for any reason other than invalid studentId
     * @throws LmsInvalidStudentIdException if studentId does not exist in the database
	 * @throws LmsInvalidCourseRecordException if any of the records were not found
	 */
    abstract public List<LMSCourseRecord> getCourseRecords(String studentId, int userId, List<CourseRecordRef> recordRefs)
		throws LmsIoException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException;
    
    /**
     * Return the concept assessments for a user from the LRS
     * 
     * @param username unique username in the LMS
     * @return List<Assessment> the concept assessment records found.  Can be empty but not null.
     * @throws LmsIoException if data cannot be read for any reason other than invalid username
     * @throws LmsInvalidStudentIdException if username does not exist in the database
     * @throws LmsInvalidCourseRecordException if any of the records were not found
     */
    abstract public List<Assessment> getAssessments(String username) 
        throws LmsIoException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException;
    
    /**
     * Return the learner states for a user from the LRS
     * 
     * @param username unique username in the LMS
     * @param courseConcepts optional course concepts to use as an additional query parameter to find knowledge and skill
     * related learner state information on for this learner.  Can be null or empty.
     * @param sinceWhen optional time to use when only the state information since that point in time are needed.  This
     * is useful when the previous state information is saved and only state updates in the external system since the last 
     * request was made are needed.  Can be null when not used.
     * @return the learner state attributes records found.  Can be empty but not null.
     * @throws LmsIoException if data cannot be read for any reason other than invalid username
     * @throws LmsInvalidStudentIdException if username does not exist in the database
     * @throws LmsInvalidCourseRecordException if any of the records were not found
     */
    abstract public List<AbstractScale> getLearnerStateAttributes(String username, Set<String> courseConcepts, Date sinceWhen)
            throws LmsIoException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException;
	
    /**
     * Insert a new course record for the student with the specified id
     * 
     * @param studentId - a unique student id to add an entry for the appropriate student
     * @param userSession the user session
     * @param domainSessionId unique domain session id
     * @param record - the new record
     * @param concepts - the course's concept hierarchy. Can be null.
     * @return CourseRecordRef - reference to the new course record.  Null if the record was not inserted, not because of an error
     * but because the LMS instance doesn't support saving this information.
     * @throws LmsException if there was an issue creating the new course record
     * @throws LmsIoException if data cannot be read for any reason other than invalid studentId
     * @throws LmsInvalidStudentIdException if studentId does not exist in the database
     * @throws LmsInvalidCourseRecordException  if the course record was malformed
     */
    public CourseRecordRef insertCourseRecord(String studentId, UserSession userSession, int domainSessionId, LMSCourseRecord record, Concepts.Hierarchy concepts)
            throws LmsException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException {
        if (!isUserSessionSupported(userSession)) {
            return null;
        }

        return insertCourseRecord(studentId, userSession.getUserId(), domainSessionId, record, concepts);
    }

	/**
	 * Insert a new course record for the student with the specified id
	 * 
	 * @param studentId - a unique student id to add an entry for the appropriate student
	 * @param userId unique GIFT user id
	 * @param domainSessionId unique domain session id
	 * @param record - the new record
	 * @param concepts - the course's concept hierarchy. Can be null.
     * @return CourseRecordRef - reference to the new course record.  Null if the record was not inserted, not because of an error
     * but because the LMS instance doesn't support saving this information.
	 * @throws LmsException if there was an issue creating the new course record
	 * @throws LmsIoException if data cannot be read for any reason other than invalid studentId
     * @throws LmsInvalidStudentIdException if studentId does not exist in the database
	 * @throws LmsInvalidCourseRecordException  if the course record was malformed
	 */
	protected abstract CourseRecordRef insertCourseRecord(String studentId, int userId, int domainSessionId, LMSCourseRecord record, Concepts.Hierarchy concepts)
		throws LmsException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException;
	
    /**
     * Provide the learner state information to the connected LMSs.
     * 
     * @param studentId a unique student id to add an entry for the appropriate student
     * @param userSession information about the user session the learner state is from
     * @param domainSessionId the unique domain session id for the learner state the information is being recorded for
     * @param learnerState contains the current learner state for the user in this domain session
     */
	public void insertLearnerState(String studentId, UserSession userSession, int domainSessionId, LearnerState learnerState){
	    
        if (!isUserSessionSupported(userSession)) {
            return;
        }
        
        insertLearnerState(studentId, userSession.getUserId(), domainSessionId, learnerState);
	}
	
    /**
     * Provide the learner state information to the connected LMSs.
     * 
     * @param studentId a unique student id to add an entry for the appropriate student
     * @param userId the unique id of the user that the learner state is for
     * @param domainSessionId the unique domain session id for the learner state the information is being recorded for
     * @param learnerState contains the current learner state for the user in this domain session
     */
	protected abstract void insertLearnerState(String studentId, int userId, int domainSessionId, LearnerState learnerState);
	
    /**
     * Provide the pedagogical request information to the connected LMSs.
     * 
     * @param studentId a unique student id to add an entry for the appropriate student
     * @param userSession information about the user session the pedagogical request is from
     * @param domainSessionId the unique domain session id for the pedagogical request the information is being recorded for
     * @param pedagogicalRequest contains the current pedagogical request for the user in this domain session
     */
    public void insertPedagogicalRequest(String studentId, UserSession userSession, int domainSessionId, PedagogicalRequest pedagogicalRequest){
        
        if (!isUserSessionSupported(userSession)) {
            return;
        }
        
        insertPedagogicalRequest(studentId, userSession.getUserId(), domainSessionId, pedagogicalRequest);
    }
    
    /**
     * Provide the pedagogical request information to the connected LMSs.
     * 
     * @param studentId a unique student id to add an entry for the appropriate student
     * @param userId the unique id of the user that the pedagogical request is for
     * @param domainSessionId the unique domain session id for the pedagogical request the information is being recorded for
     * @param pedagogicalRequest contains the current pedagogical request for the user in this domain session
     */
    protected abstract void insertPedagogicalRequest(String studentId, int userId, int domainSessionId, PedagogicalRequest pedagogicalRequest);    
    
    /**
     * Provide the knowledge session details to the connected LMSs.
     * 
     * @param studentId a unique student id to add an entry for the appropriate student
     * @param userId the unique id of the user that the knowledge session is for
     * @param domainSessionId the unique domain session id for the knowledge session is being recorded for
     * @param knowledgeSessionCreated contains information about a knowledge session being created in this domain session including details
     * about the available team organization hierarchy, mission metadata, who is hosting the session and whether its a playback or not. 
     */
    protected abstract void insertKnowledgeSessionDetails(String studentId, int userId, int domainSessionId, KnowledgeSessionCreated knowledgeSessionCreated);
    
    /**
     * Provide the knowledge session details to the connected LMSs.
     * 
     * @param studentId a unique student id to add an entry for the appropriate student
     * @param userSession information about the user session the knowledge session is from
     * @param domainSessionId the unique domain session id for the knowledge session being recorded for
     * @param knowledgeSessionCreated contains information about a knowledge session being created in this domain session including details
     * about the available team organization hierarchy, mission metadata, who is hosting the session and whether its a playback or not. 
     */
    public void insertKnowledgeSessionDetails(String studentId, UserSession userSession, int domainSessionId, KnowledgeSessionCreated knowledgeSessionCreated){
        
        if (!isUserSessionSupported(userSession)) {
            return;
        }
        
        insertKnowledgeSessionDetails(studentId, userSession.getUserId(), domainSessionId, knowledgeSessionCreated);
    }
	
	/**
     * Insert the survey results for the student with the specified id.
     * 
     * @param studentId a unique student id to add an entry for the appropriate student
     * @param userSession the user session
     * @param domainSessionID unique domain session id
     * @param score the survey result score
     * @param maxScore the maximum survey score
     * @param endTime the time the survey was completed
     * @param surveyName the name of the survey
     * @param courseName the name of the course that contains the survey
     * @param learnerStates the map of attributes to their values. Can be null or empty.
     * @param surveyResults the raw survey results. This can optionally be used to collect more-specific data from the 
     * survey response. Can be null.
	 * @throws LmsIoException if there was a problem connecting too the LMS instance or inserting the survey results
     */
    public void insertSurveyResult(String studentId, UserSession userSession, int domainSessionID, double score,
            double maxScore, Date endTime, String surveyName, String courseName, Map<LearnerStateAttributeNameEnum, String> learnerStates, SubmitSurveyResults surveyResults) throws LmsIoException {
        if (!isUserSessionSupported(userSession)) {
            return;
        }

        insertSurveyResult(studentId, userSession.getUserId(), domainSessionID, score, maxScore, endTime, surveyName, courseName, learnerStates, surveyResults);
    }

	/**
	 * Insert the survey results for the student with the specified id.
	 * 
	 * @param studentId a unique student id to add an entry for the appropriate student
	 * @param userId unique GIFT user id
	 * @param domainSessionID unique domain session id
	 * @param score the survey result score
	 * @param maxScore the maximum survey score
	 * @param endTime the time the survey was completed
	 * @param surveyName the name of the survey
	 * @param courseName the name of the course that contains the survey
	 * @param learnerStates the map of attributes to their values.  Can be null or empty.
	 * @param surveyResults the raw survey results. This can optionally be used to collect more-specific data from the 
     * survey response. Can be null.
	 * @throws LmsIoException if there was a problem connecting too the LMS instance or inserting the survey results
	 */
    protected abstract void insertSurveyResult(String studentId, int userId, int domainSessionID, double score,
            double maxScore, Date endTime, String surveyName, String courseName, Map<LearnerStateAttributeNameEnum, String> learnerStates, SubmitSurveyResults surveyResults)  throws LmsIoException;
    
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
    public abstract void pastSessionLearnerStateUpdated(AbstractKnowledgeSession knowledgeSession, DomainSession domainSession, 
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
     * Provides the lesson completed event to the LMS instance for handling.  A lesson completed happens at the end of
     * a real time assessment (aka DKF, knowledge session) part of a GIFT course.
     * @param studentId a unique student id to add an entry for the appropriate student
     * @param userId unique GIFT user id
     * @param domainSessionId the unique domain session id of where the lesson was completed in.
     * @param lessonCompleted information about the lesson completed event.
     */
    protected abstract void insertLessonCompleted(String studentId, int userId, int domainSessionId, LessonCompleted lessonCompleted);    

    /**
     * Provides the lesson completed event to the LMS instance for handling.  A lesson completed happens at the end of
     * a real time assessment (aka DKF, knowledge session) part of a GIFT course.
     * @param studentId a unique student id to add an entry for the appropriate student
     * @param userSession information about the user session the lesson was completed in.
     * @param domainSessionId the unique domain session id of where the lesson was completed in.
     * @param lessonCompleted information about the lesson completed event.
     */
    public void insertLessonCompleted(String studentId, UserSession userSession, int domainSessionId, LessonCompleted lessonCompleted){
        
        if (!isUserSessionSupported(userSession)) {
            return;
        }
        
        insertLessonCompleted(studentId, userSession.getUserId(), domainSessionId, lessonCompleted);        
    }
    
    /**
     * Provides the environment adaptation to the LMS instance for handling.  An environment adaptation happens during a 
     * real time assessment (aka DKF, knowledge session) part of a GIFT course.
     * 
     * @param studentId a unique student id to add an entry for the appropriate student
     * @param userId unique GIFT user id
     * @param domainSessionId the unique domain session id of where the environment adaptation happened
     * @param eControl information about the adaptation event
     */
    protected abstract void insertEnvironmentAdaptation(String studentId, int userId, int domainSessionId, EnvironmentControl eControl);
    
    /**
     * Provides the environment adaptation to the LMS instance for handling.  An environment adaptation happens during a 
     * real time assessment (aka DKF, knowledge session) part of a GIFT course.
     * 
     * @param studentId a unique student id to add an entry for the appropriate student
     * @param userSession information about the user session the environment adaptation happened
     * @param domainSessionId the unique domain session id of where the environment adaptation happened
     * @param eControl information about the adaptation event
     */
    public void insertEnvironmentAdaptation(String studentId, UserSession userSession, int domainSessionId, EnvironmentControl eControl){
        
        if (!isUserSessionSupported(userSession)) {
            return;
        }
        
        insertEnvironmentAdaptation(studentId, userSession.getUserId(), domainSessionId, eControl);   
    }
    
    /**
     * Passes the InitializeDomainSessionRequest to abstract method
     * 
     * @param message - InitializeDomainSessionRequest message
     */
    public void insertDomainSessionInit(Message message) throws LmsDomainSessionException {
        // Cast now that message is being used
        DomainSessionMessage msg = (DomainSessionMessage) message;
        if (!isUserSessionSupported(msg.getUserSession())) {
            return;
        }
        insertDomainSessionInit(msg);
    }
    
    /**
     * LMS specific implementation of handling the InitializeDomainSessionRequest DomainSessionMessage
     * 
     * @param message - DomainSessionMessage w/ payload InitializeDomainSessionRequest
     */
    protected abstract void insertDomainSessionInit(DomainSessionMessage message) throws LmsDomainSessionException;
    
    /**
     * Passes the DomainSessionMessage to abstract method
     * 
     * @param message - no payload, just a DomainSessionMessage
     */
    public void insertDomainSessionStarted(Message message) 
            throws LmsDomainSessionException, LmsIoException, LmsStatementIdException, LmsXapiAgentException, LmsXapiActivityException {
        // Cast now that message is being used
        DomainSessionMessage msg = (DomainSessionMessage) message;
        if (!isUserSessionSupported(msg.getUserSession())) {
            return;
        }
        insertDomainSessionStarted(msg);
    }
    
    /**
     * LMS specific implementation of handling the DomainSessionStarted DomainSessionMessage
     * 
     * @param message - DomainSessionMessage without payload
     */
    protected abstract void insertDomainSessionStarted(DomainSessionMessage message) 
            throws LmsDomainSessionException, LmsIoException, LmsStatementIdException, LmsXapiAgentException, LmsXapiActivityException;
    
    /**
     * Passes the CloseDomainSessionRequest to abstract method
     * 
     * @param message - CloseDomainSessionRequest message
     */
    public void insertDomainSessionClose(Message message) 
            throws LmsDomainSessionException, LmsIoException, LmsStatementIdException, LmsXapiAgentException, LmsXapiActivityException {
        // Cast now that message is being used
        DomainSessionMessage msg = (DomainSessionMessage) message;
        if (!isUserSessionSupported(msg.getUserSession())) {
            return;
        }
        insertDomainSessionClose(msg);
    }
    
    /**
     * LMS specific implementation of handling the CloseDomainSessionRequest DomainSessionMessage
     * 
     * @param message - DomainSessionMessage w/ payload CloseDomainSessionRequest
     */
    protected abstract void insertDomainSessionClose(DomainSessionMessage message) 
            throws LmsDomainSessionException, LmsIoException, LmsStatementIdException, LmsXapiAgentException, LmsXapiActivityException;
	 
	/**
	 * Return information about the LMS connection that can be paired with data retrieved
	 * from the LMS and sent to other modules for purposes such as adjusting the learner model
	 * and displaying that information to the user.
	 * 
	 * @return LMSConnectionInfo
	 */
	public abstract LMSConnectionInfo getConnectionInfo();
	

    /**
     * Return the latest root course records for all of the courses taken by the student with the given id
     * 
     * @param studentId unique LMS user name
     * @param userId unique GIFT user id
     * @param domainSessionIds (Optional) The domain session IDs that the records should be filtered by. If
     * a record is associated with a domain session ID that is not in this list, it will be excluded from the 
     * result. If null, all records matching the given user ID and username will be included in the result.
     * @return List<LMSCourseRecord> the course records found.  Can be empty but not null.
     * @throws LmsIoException if data cannot be read for any reason other than invalid studentId
     * @throws LmsInvalidStudentIdException if studentId does not exist in the database
     */
    abstract public List<LMSCourseRecord> getLatestRootCourseRecordsPerDomain(String studentId, int userId, List<Integer> domainSessionIds)
            throws LmsIoException, LmsInvalidStudentIdException;

    /**
     * Checks if the provided {@link UserSession user session} is supported by
     * the LMS.
     * 
     * @param userSession the user session to check.
     * @return true if the user session is supported by the LMS; false
     *         otherwise.
     */
    public boolean isUserSessionSupported(UserSession userSession) {
        /* Default is to support all user sessions */
        return true;
    }

	@Override
    public String toString(){
	    
	    StringBuffer sb = new StringBuffer();
	    sb.append("name = ").append(getName());
	    sb.append(", connectionInfo = ").append(getConnectionInfo());
	    return sb.toString();
	}
	
}
