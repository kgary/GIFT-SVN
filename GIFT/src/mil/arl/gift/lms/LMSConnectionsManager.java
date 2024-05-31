/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.lms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import generated.course.Concepts;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.KnowledgeSessionCreated;
import mil.arl.gift.common.LMSConnectionInfo;
import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.common.LMSCourseRecords;
import mil.arl.gift.common.LMSDataRequest;
import mil.arl.gift.common.PedagogicalRequest;
import mil.arl.gift.common.PublishLessonScoreResponse;
import mil.arl.gift.common.SubmitSurveyResults;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.course.AssessmentChainOfCustody;
import mil.arl.gift.common.course.CourseConceptsUtil;
import mil.arl.gift.common.course.CourseRecordRef;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.LessonCompleted;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.common.survey.score.AbstractAnswerScore;
import mil.arl.gift.common.survey.score.AbstractScale;
import mil.arl.gift.common.survey.score.AbstractScaleScore;
import mil.arl.gift.common.survey.score.ScoreInterface;
import mil.arl.gift.common.survey.score.SurveyConceptAssessmentScore;
import mil.arl.gift.common.survey.score.SurveyConceptAssessmentScore.ConceptOverallDetails;
import mil.arl.gift.common.ta.request.EnvironmentControl;
import mil.arl.gift.common.survey.score.SurveyScorerManager;
import mil.arl.gift.lms.impl.common.AbstractLms;
import mil.arl.gift.lms.impl.common.LmsException;
import mil.arl.gift.lms.impl.common.LmsInvalidCourseRecordException;
import mil.arl.gift.lms.impl.common.LmsInvalidStudentIdException;
import mil.arl.gift.lms.impl.common.LmsIoException;
import mil.arl.gift.lms.impl.lrs.Lrs;
import mil.arl.gift.net.api.message.Message;

/**
 * This class manages the different LMS connections.  It includes methods for reading and writing
 * LMS entries.
 * 
 * @author mhoffman
 *
 */
public class LMSConnectionsManager {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(LMSConnectionsManager.class);
    
    /** parses the LMS connections configuration file and initialize LMS connection(s) */
    private LMSConnectionsFileHandler lmsConnectionsHandler;
    
    private static LMSConnectionsManager instance = null;
    
    /**
     * Return the singleton instance of this class
     * 
     * @return the singleton instance
     * @throws FileValidationException if there was a problem with the LMS connections file
     */
    public static LMSConnectionsManager getInstance() throws FileValidationException{
        
        if(instance == null){
            instance = new LMSConnectionsManager();
        }
        
        return instance;
    }

    private LMSConnectionsManager() throws FileValidationException{
        
        //initialize LMS connection(s)
        lmsConnectionsHandler = new LMSConnectionsFileHandler();
    }
    
    /**
     * Cleanup any LMS connections
     */
    public void cleanup(){

        try {
            for(AbstractLms lms : lmsConnectionsHandler.getLMSImplementations().values()){
                lms.disconnect();
            }
        } catch (LmsIoException ex) {
            logger.error("Caught LmsIoException on disconnect:" + ex);
        }

    }
    
    /**
     * Handle the publish lesson score by writing the necessary entries to the connected LMS(s).
     * 
     * @param userSession information about the user whose lesson score is being recorded
     * @param domainSessionId the unique domain session id for the course session the information is being recorded for
     * @param lmsUsername the LMS username for this user
     * @param record the publish lesson score request to handle
     * @param concepts the course's concept hierarchy. Can be null.
     * @return a mapping of each connected LMS's handle to the record that was just written in that system.
     * @throws LmsException if there was an issue creating the new course record
     * @throws LmsInvalidStudentIdException if studentId does not exist in the database
     * @throws LmsInvalidCourseRecordException  if the course record was malformed
     */
    public Map<LMSConnectionInfo, CourseRecordRef> publishLessonScore(UserSession userSession, 
            int domainSessionId, String lmsUsername, LMSCourseRecord record, Concepts.Hierarchy concepts) throws LmsInvalidStudentIdException, LmsInvalidCourseRecordException, LmsException {   
             
        if (logger.isInfoEnabled()) {
            logger.info("Received publish lesson score request for user " + userSession + " in domain session " + domainSessionId
                    + " with LMS username of " + lmsUsername + ".\n" + record);
        }
        
        //reply with keys from each type of LMS connection
        Map<LMSConnectionInfo, CourseRecordRef> publishedRecordsByLMS = new HashMap<>();
        
        for (AbstractLms lms : lmsConnectionsHandler.getLMSImplementations().values()) {

            try{
                CourseRecordRef recordRef = lms.insertCourseRecord(lmsUsername, userSession, domainSessionId, record, concepts);

                if(recordRef != null && recordRef.getRef() != null && !recordRef.getRef().isEmpty()){
                    publishedRecordsByLMS.put(lms.getConnectionInfo(), recordRef);
                    
                    if (logger.isInfoEnabled()) {
                        logger.info("Added record of " + recordRef + " to LMS of " + lms.getConnectionInfo());
                    }
                }
            }catch(Exception e){
                logger.error("Caught exception from misbehaving LMS connection ("+lms+") when publishing the lesson score:\n"+record, e);
            }
        }
        
        return publishedRecordsByLMS;
    }
    
    /**
     * Provide the learner state information to the connected LMSs.
     * 
     * @param userSession information about the user session the learner state is from
     * @param domainSessionId the unique domain session id for the learner state the information is being recorded for
     * @param learnerState contains the current learner state for the user in this domain session
     */
    public void publishLearnerState(UserSession userSession, 
            int domainSessionId, LearnerState learnerState){
        
        if (logger.isInfoEnabled()) {
            logger.info("Received publish learner state request for user " + userSession + " in domain session " + domainSessionId
                    + ".\n" + learnerState);
        }
        
        if(learnerState.isEmpty()){
            if (logger.isInfoEnabled()) {
                logger.info("Skipping an empty learner state for user " + userSession + " in domain session " + domainSessionId
                        + ".");
            }
            return;
        }
        
        for (AbstractLms lms : lmsConnectionsHandler.getLMSImplementations().values()) {
            
            try{
                lms.insertLearnerState(userSession.getUsername(), userSession, domainSessionId, learnerState);
            }catch(Exception e){
                logger.error("Caught exception from misbehaving LMS connection ("+lms+") when publishing the learner state:\n"+learnerState, e);
            }
        }

    }
    
    /**
     * Provide info about the initialized DomainSession to the connected LMSs.
     * 
     * @param message - InitializeDomainSessionRequest message
     */
    public void publishInitDomainSession(Message message) {
        
        if(logger.isInfoEnabled()){
            logger.info("Received publish domain sesion initialized " + message.toString() + " message");      
        }
        
        for(AbstractLms lms : lmsConnectionsHandler.getLMSImplementations().values()) {
            try {
                lms.insertDomainSessionInit(message);
            } catch(Exception e) {
                logger.error("Caught exception from misbehaving LMS connection ("+lms+") when publishing the Domain Session Initialized", e);
            }
        }
    }
    
    /**
     * Provide indication that the previously initialized DomainSession has now started
     * to the connected LMSs.
     * 
     * @param message - no payload, just a DomainSessionMessage
     */
    public void publishStartDomainSession(Message message) {
        
        if(logger.isInfoEnabled()){
            logger.info("Received publish domain sesion started " + message.toString() + " message");      
        }
        
        for(AbstractLms lms : lmsConnectionsHandler.getLMSImplementations().values()) {
            try {
                lms.insertDomainSessionStarted(message);
            } catch(Exception e) {
                logger.error("Caught exception from misbehaving LMS connection ("+lms+") when publishing the Domain Session Started", e);
            }
        }
    }
    
    /**
     * Provide indication that the started DomainSession has now closed to the connected LMSs.
     * 
     * @param message - CloseDomainSessionRequest message
     */
    public void publishCloseDomainSession(Message message) {
        
        if(logger.isInfoEnabled()){
            logger.info("Received publish domain sesion closed " + message.toString() + " message");      
        }
        
        for(AbstractLms lms : lmsConnectionsHandler.getLMSImplementations().values()) {
            try {
                lms.insertDomainSessionClose(message);
            } catch(Exception e) {
                logger.error("Caught exception from misbehaving LMS connection ("+lms+") when publishing the Domain Session Closed", e);
            }
        }
    }
    
    /**
     * Handle notification that a lesson (DKF, real time assessment, domain knowledge session) received an
     * environment adaptation event.
     * 
     * @param userSession information about the user session the environment adaptation happened
     * @param domainSessionId the unique domain session id of where the environment adaptation happened
     * @param eControl information about the environment adaptation event
     */
    public void handleEnvironmentAdaptation(UserSession userSession, int domainSessionId, EnvironmentControl eControl){
        
        if (logger.isInfoEnabled()) {
            logger.info("Received environment adaptation for user " + userSession + " in domain session " + domainSessionId
                    + ".\n" + eControl);
        }
        
        for (AbstractLms lms : lmsConnectionsHandler.getLMSImplementations().values()) {
            
            try{
                lms.insertEnvironmentAdaptation(userSession.getUsername(), userSession, domainSessionId, eControl);
            }catch(Exception e){
                logger.error("Caught exception from misbehaving LMS connection ("+lms+") when handling the environment adaptation:\n"+eControl, e);
            }
        }
    }
    
    /**
     * Handle notification that a lesson (DKF, real time assessment, domain knowledge session) has ended.
     * @param userSession information about the user session the lesson was completed in.
     * @param domainSessionId the unique domain session id of where the lesson was completed in.
     * @param lessonCompleted information about the lesson completed event.
     */
    public void handleLessonCompleted(UserSession userSession, int domainSessionId, LessonCompleted lessonCompleted){
        
        if (logger.isInfoEnabled()) {
            logger.info("Received lesson completed for user " + userSession + " in domain session " + domainSessionId
                    + ".\n" + lessonCompleted);
        }
        
        for (AbstractLms lms : lmsConnectionsHandler.getLMSImplementations().values()) {
            
            try{
                lms.insertLessonCompleted(userSession.getUsername(), userSession, domainSessionId, lessonCompleted);
            }catch(Exception e){
                logger.error("Caught exception from misbehaving LMS connection ("+lms+") when handling the lesson completed:\n"+lessonCompleted, e);
            }
        }
    }
    
    /**
     * Provide the knowledge session created to the connected LMSs.
     * 
     * @param userSession information about the user session the knowledge session created message is from
     * @param domainSessionId the unique domain session id for the knowledge session created messages
     * @param knowledgeSessionCreated contains information about a knowledge session being created in this domain 
     * session including details about the available team organization hierarchy, mission metadata, who is hosting
     * the session and whether its a playback or not. 
     */
    public void handleKnowledgeSessionCreated(UserSession userSession, 
            int domainSessionId, KnowledgeSessionCreated knowledgeSessionCreated){
        
        if (logger.isInfoEnabled()) {
            logger.info("Received knowledge session created for user " + userSession + " in domain session " + domainSessionId
                    + ".\n" + knowledgeSessionCreated);
        }
        
        for (AbstractLms lms : lmsConnectionsHandler.getLMSImplementations().values()) {
            
            try{
                lms.insertKnowledgeSessionDetails(userSession.getUsername(), userSession, domainSessionId, knowledgeSessionCreated);
            }catch(Exception e){
                logger.error("Caught exception from misbehaving LMS connection ("+lms+") when handling the knowledge session created:\n"+knowledgeSessionCreated, e);
            }
        }
    }
    
    /**
     * Provide the pedagogical request information to the connected LMSs.
     * 
     * @param userSession information about the user session the pedagogical request is from
     * @param domainSessionId the unique domain session id for the pedagogical request the information is being recorded for
     * @param learnerState contains the current pedagogical request for the user in this domain session
     */
    public void publishPedagogicalRequest(UserSession userSession, 
            int domainSessionId, PedagogicalRequest pedRequest){
        
        if (logger.isInfoEnabled()) {
            logger.info("Received publish pedagogical request for user " + userSession + " in domain session " + domainSessionId
                    + ".\n" + pedRequest);
        }
        
        for (AbstractLms lms : lmsConnectionsHandler.getLMSImplementations().values()) {
            
            try{
                lms.insertPedagogicalRequest(userSession.getUsername(), userSession, domainSessionId, pedRequest);
            }catch(Exception e){
                logger.error("Caught exception from misbehaving LMS connection ("+lms+") when publishing the pedagogical request:\n"+pedRequest, e);
            }
        }

    }
    
    /**
     * Handle the publish survey results by writing the necessary entries to the connected LMS(s).
     * 
     * @param userSession information about the user whose lesson score is being recorded
     * @param domainSessionID the unique domain session id for the course session the information is being recorded for
     * @param lmsUsername the LMS username for this user
     * @param surveyResults the results of the survey to record
     */
    public void publishSurveyResults(UserSession userSession, int domainSessionID, String lmsUsername,
            SubmitSurveyResults surveyResults) {
        
        if (logger.isInfoEnabled()) {
            logger.info("Received publish survey results request for user session " + userSession + " in domain session " + domainSessionID
                    + " with LMS username of " + lmsUsername + ".\n" + surveyResults);
        }
        
        List<ScoreInterface> scores = SurveyScorerManager.getScores(surveyResults.getSurveyResponse());

        List<AbstractAnswerScore> answerScores = new ArrayList<AbstractAnswerScore>();
        List<AbstractScaleScore> scaleScores = new ArrayList<AbstractScaleScore>();
        List<SurveyConceptAssessmentScore> conceptAssessmentScores = new ArrayList<SurveyConceptAssessmentScore>();

        // organize scores
        for (ScoreInterface score : scores) {
            if (score instanceof AbstractAnswerScore) {
                answerScores.add((AbstractAnswerScore) score);
            } else if (score instanceof AbstractScaleScore) {
                scaleScores.add((AbstractScaleScore) score);
            } else if (score instanceof SurveyConceptAssessmentScore) {
                conceptAssessmentScores.add((SurveyConceptAssessmentScore) score);
            }
        }
        
        Map<LearnerStateAttributeNameEnum, String> learnerStates = new HashMap<LearnerStateAttributeNameEnum, String>();
        for (AbstractScaleScore scaleScore : scaleScores) {
            for (AbstractScale aScale : scaleScore.getScales()) {
                learnerStates.put(aScale.getAttribute(), aScale.getValue().getName());
            }
        }

        for (AbstractLms lms : lmsConnectionsHandler.getLMSImplementations().values()) {
            // insert abstract answer scores; scale scores get inserted as extensions
            for (AbstractAnswerScore answerScore : answerScores) {

                Double totalPointsEarned = answerScore.getTotalEarnedPoints();
                Double highestPossiblePoints = answerScore.getHighestPossiblePoints();

                try{
                    lms.insertSurveyResult(lmsUsername, userSession, domainSessionID, totalPointsEarned,
                            highestPossiblePoints, surveyResults.getSurveyResponse().getSurveyEndTime(),
                            surveyResults.getSurveyResponse().getSurveyName(), surveyResults.getCourseName(),
                            learnerStates, surveyResults);
                }catch(Exception e){
                        logger.error("Caught exception from misbehaving LMS connection ("+lms+") when publishing the survey results answer scores:\n"+surveyResults, e);
                }
            }

            // insert survey concept assessment scores
            for (SurveyConceptAssessmentScore conceptAssessmentScore : conceptAssessmentScores) {
                Map<String, ConceptOverallDetails> conceptToDetailsMap = conceptAssessmentScore.getConceptDetails();
                for (String concept : conceptToDetailsMap.keySet()) {
                    double totalPointsEarned = conceptToDetailsMap.get(concept).getCorrectQuestions().size();
                    double highestPossiblePoints = totalPointsEarned + conceptToDetailsMap.get(concept).getIncorrectQuestions().size();

                    try{
                        lms.insertSurveyResult(lmsUsername, userSession, domainSessionID, totalPointsEarned,
                                highestPossiblePoints, surveyResults.getSurveyResponse().getSurveyEndTime(),
                                surveyResults.getSurveyResponse().getSurveyName() + " concept: " + concept,
                                surveyResults.getCourseName(), null, surveyResults);
                    }catch(Exception e){
                            logger.error("Caught exception from misbehaving LMS connection ("+lms+") when publishing the survey results concept assessment scores:\n"+surveyResults, e);
                    }
                }
            }
        }
    }
    
    /**
     * Handle the LMS data request by retrieving the LMS data for the specified learner
     * user.
     * 
     * @param userId information about the user retrieving records for
     * @param dataRequest information used to query the LMS
     * @return the records found.  Will not be null.
     * @throws LmsInvalidCourseRecordException if any of the records were not found
     * @throws LmsIoException if data cannot be read for any reason other than invalid studentId
     * @throws LmsInvalidStudentIdException if studentId does not exist in the database
     * @throws DetailedException a better explanation that can be used to communicate to other modules (or the user)
     */
    public LMSCourseRecords getLMSData(int userId, LMSDataRequest dataRequest) 
            throws LmsIoException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException, DetailedException {

        if(dataRequest == null){
            throw new IllegalArgumentException("The data request can't be null.");
        }
        
        if(logger.isInfoEnabled()){
            logger.info("Received LMS data request for user "+userId+".\n"+dataRequest);
        }

        LMSCourseRecords lmsCourseRecords = new LMSCourseRecords();
        
        if (dataRequest.getPublishedScores().isEmpty()) {
            //retrieve all records for the user            
            
            for(AbstractLms lms : lmsConnectionsHandler.getLMSImplementations().values()){                    
                
                try{
                    //First the course records
                    List<LMSCourseRecord> records = lms.getCourseRecords(
                            dataRequest.getUserName(),
                            userId,
                            dataRequest.getPageStart(),
                            dataRequest.getPageSize(),
                            dataRequest.getSortDescending(),
                            dataRequest.getDomainIds(),
                            dataRequest.getDomainSessionIds());
                    
                    lmsCourseRecords.addRecords(records);

                }catch(Throwable t){
                    //keep attempting to retrieve records
                    logger.error("Caught exception from misbehaving LMS implementation of "+lms+" when retrieving the LMS data for user "+userId, t);
                }
            }
            
        } else {
            //retrieve specific records for the user
            
            Map<String, AbstractLms> lmsImplementations = lmsConnectionsHandler.getLMSImplementations();
            
            for(PublishLessonScoreResponse publishResponse : dataRequest.getPublishedScores()){
                
                Map<LMSConnectionInfo, CourseRecordRef> publishedRecordsByLMS = publishResponse.getPublishedRecordsByLMS();
                for(LMSConnectionInfo connectionInfo : publishedRecordsByLMS.keySet()){
                    
                    try{
                    
                        //get the LMS connection by unique name
                        AbstractLms lms = lmsImplementations.get(connectionInfo.getName());
                        if(lms == null){
                            //ERROR - unable to find specified LMS by name
                            throw new LmsIoException("Unable to find LMS connection named "+connectionInfo.getName()+".", null);
                        }else{
                            LMSCourseRecord record = lms.getCourseRecord(
                                    dataRequest.getUserName(),
                                    userId,
                                    publishedRecordsByLMS.get(connectionInfo));
                            
                            lmsCourseRecords.addRecord(record);

                        }
                    }catch(Throwable t){
                        //bail out because the data requestor is referencing a published record that should be retrievable but
                        //something bad happened and the data can't be retrieved
                        throw new DetailedException(
                                "The LMS module had a problem while retrieving learner records from the LMS connection named '"+connectionInfo.getName()+"'.",
                                "The '"+connectionInfo.getName()+"' caused an exception while handling the publish response of:\n"+publishResponse, t);
                    }
                }
            }

        }
        
        return lmsCourseRecords;
    }
    
    /**
     * Handle the LMS data request by retrieving the LMS data for the specified learner
     * user.
     * 
     * @param userId information about the user retrieving records for
     * @param lmsDataRequest information used to query the LMS
     * @return the learner state records found.  Will not be null.
     * @throws LmsInvalidCourseRecordException if any of the records were not found
     * @throws LmsIoException if data cannot be read for any reason other than invalid studentId
     * @throws LmsInvalidStudentIdException if studentId does not exist in the database
     */
    public List<AbstractScale> getLMSLearnerStateAttributeData(int userId, LMSDataRequest lmsDataRequest) throws LmsIoException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException {

        if (lmsDataRequest == null) {
            throw new IllegalArgumentException("The data request can't be null.");
        }

        logger.info("Received LMS learner state data request for user " + userId + ".\n" + lmsDataRequest);

        List<AbstractScale> learnerStateAttributes = new ArrayList<AbstractScale>();
        
        Set<String> courseConcepts = null;
        if(lmsDataRequest.getCourseConcepts() != null){
            // simplify data structure for lms connection
            courseConcepts = new HashSet<>();
            generated.course.Concepts conceptsElement = lmsDataRequest.getCourseConcepts();
            courseConcepts.addAll(CourseConceptsUtil.getConceptNameList(conceptsElement));
        }

        // retrieve all learner state records for the user
        for (AbstractLms lms : lmsConnectionsHandler.getLMSImplementations().values()) {
            
            try{
                List<AbstractScale> lsAttributes = lms.getLearnerStateAttributes(lmsDataRequest.getUserName(), courseConcepts, lmsDataRequest.getSinceWhen());
                if (lsAttributes != null) {
                    learnerStateAttributes.addAll(lsAttributes);
                }
            }catch(Throwable t){
                logger.error("Misbehaving LMS connection of "+lms+" when retrieving learner state attributes for "+lmsDataRequest.getUserName(), t);
            }
        }

        return learnerStateAttributes;
    }
    
    /**
     * Handle the LMS data request by retrieving the latest root LMS data for every course the specified learner user has taken 
     * 
     * @param userId information about the user retrieving records for
     * @param username the LMS user name of the user whose records are being retrieved
     * @param domainSessionIds (Optional) The domain session IDs that the records should be filtered by. If
     * a record is associated with a domain session ID that is not in this list, it will be excluded from the 
     * result. If null, all records matching the given user ID and username will be included in the result.
     * @return the records found.  Will not be null.
     * @throws LmsInvalidCourseRecordException if any of the records were not found
     * @throws LmsIoException if data cannot be read for any reason other than invalid studentId
     * @throws LmsInvalidStudentIdException if studentId does not exist in the database
     */
    public LMSCourseRecords getLatestRootLMSDataPerDomain(int userId, String username, List<Integer> domainSessionIds) throws LmsIoException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException {

        if(username == null){
            throw new IllegalArgumentException("The data request can't be null.");
        }
        
        if(logger.isInfoEnabled()){
            logger.info("Received latest root LMS data per domain request for user "+userId+".\n"+username);
        }

        LMSCourseRecords lmsCourseRecords = new LMSCourseRecords();
            //retrieve all records for the user            
            
        for(AbstractLms lms : lmsConnectionsHandler.getLMSImplementations().values()){
            // First the course records
            List<LMSCourseRecord> records = null;
            
            // DEBUG
            if(logger.isInfoEnabled()){
                logger.info("current LMS handling root LMS data per domain request = "+lms);
            }
            
            try {
                records = lms.getLatestRootCourseRecordsPerDomain(username, userId, domainSessionIds);
            } catch(LmsIoException e) {
                throw e;
            } catch(LmsInvalidStudentIdException e) {
                throw e;
            } catch(Throwable t){
                throw new LmsIoException("A generic error happend on the server when retrieving records from "+lms, t);
            }
            
            if(records != null) {
                lmsCourseRecords.addRecords(records);
            }

        }
        
        return lmsCourseRecords;
    }
    
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
     * @throws LmsIoException  if there was a problem connecting too the LMS instance, invalidating records or inserting new records.
     */
    public void pastSessionLearnerStateUpdated(AbstractKnowledgeSession knowledgeSession, DomainSession domainSession, 
            LearnerState newLearnerState, LearnerState oldLearnerState) throws LmsIoException{        
        
        if(logger.isDebugEnabled()){
            logger.info("Received past session learner state update for host session user "+knowledgeSession.getHostSessionMember().getUserSession().getUsername()+", domain session = "+domainSession+".\n"+newLearnerState);
        }
        
        for(AbstractLms lms : lmsConnectionsHandler.getLMSImplementations().values()){
            
            try {
                lms.pastSessionLearnerStateUpdated(knowledgeSession, domainSession, newLearnerState, oldLearnerState);
            } catch(LmsIoException e) {
                throw e;
            }
        }
    }
    
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
     * @param concepts the course concepts as a hierarchy.  Can be null.
     * @throws LmsIoException if there was a problem connecting too the LMS instance, invalidating records or inserting new records.
     */
    public void pastSessionCourseRecordUpdated(AbstractKnowledgeSession knowledgeSession, AssessmentChainOfCustody chainOfCustody,
            LMSCourseRecord newCourseRecord, LMSCourseRecord oldCourseRecord, Concepts.Hierarchy concepts) throws LmsIoException{
        
        if(logger.isDebugEnabled()){
            logger.info("Received past session LMS course record update for "+chainOfCustody+".\n"+newCourseRecord);
        }
        
        for(AbstractLms lms : lmsConnectionsHandler.getLMSImplementations().values()){
            
            try{
                lms.pastSessionCourseRecordUpdated(knowledgeSession, chainOfCustody,
                        newCourseRecord, oldCourseRecord, concepts);
            } catch(LmsIoException e) {
                throw e;
            }
        }
    }
    
    /**
     * Gets a list of the names of all connected LRS instances. This can be handy for reporting errors
     * related to LRS instances.
     * 
     * @return the list of LRS names. Will not be null, but can be empty.
     */
    public List<String> getLrsNames(){
        
        List<String> lrsNames = new ArrayList<String>();
        
        for(AbstractLms lms : lmsConnectionsHandler.getLMSImplementations().values()) {
            
            if(lms instanceof Lrs) {
                lrsNames.add(lms.getName());
            }
        }
        
        return lrsNames;
    }
}
