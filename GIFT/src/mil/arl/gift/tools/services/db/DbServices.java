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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.course.Concepts;
import mil.arl.gift.common.BranchPathHistory;
import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.CourseRecord;
import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.LMSCourseRecords;
import mil.arl.gift.common.LMSDataRequest;
import mil.arl.gift.common.SurveyCheckRequest;
import mil.arl.gift.common.SurveyCheckResponse;
import mil.arl.gift.common.SurveyListCheckRequest;
import mil.arl.gift.common.DomainOption.DomainOptionRecommendation;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.common.course.AssessmentChainOfCustody;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.DomainOptionRecommendationEnum;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.io.ProhibitedUserException;
import mil.arl.gift.common.lti.TrustedLtiConsumer;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.domain.DomainCourseFileHandler;
import mil.arl.gift.domain.DomainDKFHandler;
import mil.arl.gift.domain.DomainCourseFileHandler.CourseOptionsWrapper;
import mil.arl.gift.lms.LMSConnectionsManager;
import mil.arl.gift.lms.impl.common.LmsInvalidCourseRecordException;
import mil.arl.gift.lms.impl.common.LmsInvalidStudentIdException;
import mil.arl.gift.lms.impl.common.LmsIoException;
import mil.arl.gift.tools.importer.CourseDBImporter;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.ums.db.UMSDatabaseException;
import mil.arl.gift.ums.db.UMSDatabaseManager;
import mil.arl.gift.ums.db.survey.SurveyValidationException;
import mil.arl.gift.ums.db.survey.Surveys;
import mil.arl.gift.ums.db.survey.Surveys.ExternalSurveyMapper;
import mil.arl.gift.ums.db.table.DbBranchPathHistory;
import mil.arl.gift.ums.db.table.DbLtiUserRecord;
import mil.arl.gift.ums.db.table.DbUser;
/**
 * This class contains GIFT database services to abstract the logic involved with 
 * the various deployment modes of GIFT.
 * 
 * @author mhoffman
 *
 */
public class DbServices implements DbServicesInterface {
        
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(DbServices.class);
    
    /** used to interact with the UMS database */
    protected static final UMSDatabaseManager umsDbMgr = UMSDatabaseManager.getInstance();
    
    /** used to interact with the LMS database(s)/connection(s) */
    protected static final LMSConnectionsManager lmsDbMgr = LMSConnectionsManager.getInstance();

    @Override
    public LMSCourseRecords getLMSData(int userId, LMSDataRequest dataRequest)
            throws LmsIoException, LmsInvalidStudentIdException,
            LmsInvalidCourseRecordException {
        
        if(logger.isInfoEnabled()){
            logger.info("Retrieving LMS records for GIFT user w/ id "+userId+".  Request = "+dataRequest);
        }
        
        try{
            return lmsDbMgr.getLMSData(userId, dataRequest);
        }catch(FileValidationException e){
            throw new LmsIoException("Failed to connect to the LMS connections.", e);
        }
    }
    
    @Override
    public LMSCourseRecords getLatestRootLMSDataPerDomain(int userId, String username)
            throws LmsIoException, LmsInvalidStudentIdException,
            LmsInvalidCourseRecordException {
        
        if(logger.isInfoEnabled()){
            logger.info("Retrieving latest root LMS records per domain for GIFT user w/ id "+userId+".  Request = "+username);
        }
        
        try{
            /* Fetch all of the domain session IDs that are associated with the given user ID and are NOT part of an experiment.
             * These IDs will be cross-referenced when the course records are queried so that course records for experiment
             * users are excluded.
             * 
             * This is used to address a problem documented in #4707 where course records for experiment users would appear
             * on a user's Learner Records page if the experiment user's ID happened to match the ID of the user that 
             * made the request.*/
            List<Integer> nonExperimentDomainSessionIds = UMSDatabaseManager.getInstance().getNonExperimentDomainSessionIds(userId);
            
            //Request the given user's course records while providing the domain session IDs for reference
            return lmsDbMgr.getLatestRootLMSDataPerDomain(userId, username, nonExperimentDomainSessionIds);
            
        }catch(FileValidationException e){
            throw new LmsIoException("Failed to connect to the LMS connections.", e);
        }
    }
    
    @Override
    public void getBranchPathHistory(BranchPathHistory branchPathHistory) {

        DbBranchPathHistory dbBranchPathHistory = umsDbMgr.getBranchPathHistory(branchPathHistory);
        
        branchPathHistory.setActualCnt(dbBranchPathHistory.getActualCnt());
        branchPathHistory.setCnt(dbBranchPathHistory.getCnt());
    }

    @Override
    public void updateBranchPathHistory(BranchPathHistory branchPathHistory) {

        umsDbMgr.updateBranchPathHistoryProperties(branchPathHistory);
    }
    
    @Override
    public boolean updateOrCreateLtiUserRecord(TrustedLtiConsumer consumer, String consumerId, Date timestamp) {
        
        boolean success = false;
        
        try {
            // See if the lti user already exists in the database.
            DbLtiUserRecord ltiUser = umsDbMgr.getLtiUserRecord(consumer.getConsumerKey(),  consumerId, false);
            if (ltiUser != null) {
                umsDbMgr.updateLtiUserRecord(consumer.getConsumerKey(),  consumerId, timestamp);
            } else {
                umsDbMgr.createLtiUserRecord(consumer,  consumerId, timestamp);
            }
            
            success = true;
        } catch (DetailedException e) {          
            logger.error("DetailedException caught while updating or creating the lti user: ", e);
        } catch (UMSDatabaseException e) {
            logger.error("UMSDatabaseException caught while updating or creating the lti user: ", e);
        }
 
        return success;
    }
    
    @Override
    public Date getTimestampForLtiUserRecord(String consumerKey, String consumerId) {
        Date timestamp = null;
        // We set 'true' here so that the result is read directly from the database rather
        // than possibly pulling from the hibernate cache.  A stale value here cannot happen
        // since the timestamp is sensitive due to the login happening within milliseconds.
        DbLtiUserRecord ltiUser = umsDbMgr.getLtiUserRecord(consumerKey,  consumerId, true);
        
        if (ltiUser != null) {
            timestamp = ltiUser.getLaunchRequestTimestamp();
        }
        
        return timestamp;
    }
    
    @Override
    public CourseRecord getCourseByPath(String coursePath) {
        
        CourseRecord courseRecord = null;
        try {
            
            // There are differences in the desktop/server mode where the path must be converted to not have a 'leading slash' character.
            String convertedPath = CourseRecord.convertRuntimeCoursePath(coursePath);
            courseRecord = umsDbMgr.getCourseByPath(convertedPath);
        } catch (DetailedException e) {
            logger.error("Exception caught while retrieving the course data with path '" + coursePath
                    + "' from the database: ", e);
        }
        return courseRecord;
    }
    
    @Override
    public CourseRecord createCourseRecord(CourseRecord courseRecord) {
        CourseRecord course = null;
        try {
            course = umsDbMgr.createCourseRecord(courseRecord);
        } catch (UMSDatabaseException e) {
            logger.error("Exception caught while trying to create a new course record object '" + courseRecord
                    + "' in the database: ", e);
        }
        
        return course;
    }

    @Override
    public boolean updateCourseRecord(String username, CourseRecord courseRecord) {
        try {
            return umsDbMgr.updateCourseRecord(username, courseRecord, null);
        } catch (UMSDatabaseException e) {
            logger.error(
                    "Exception caught while trying to create a new course record object '" + courseRecord
                            + "' for user '" + username + "' in the database: ",
                    e);
            return false;
        }
    }

    @Override
    public CourseRecord getCourseById(String courseId, Boolean byPassDeletedStatus) {
        CourseRecord courseRecord = null;
        try {
            courseRecord = umsDbMgr.getCourseById(courseId, null);
            
            // If the record is has the deleted flag set, then return a null record.
            if (courseRecord == null || (courseRecord.isDeleted() && !byPassDeletedStatus)) {
               return null;
            }
             
        } catch (DetailedException e) {
            logger.error("Exception caught while retrieving the course by id '" + courseId
                    + "' with byPassDeletedStatus='" + byPassDeletedStatus + "' from the database: ", e);
        }
        return courseRecord;
    }
    
    
    @Override
    public CourseRecord createCourseRecordIfNeeded(String userName, String relativeFilePath, boolean isModifiable) {
        
        if (relativeFilePath == null || relativeFilePath.isEmpty()) {
            throw new IllegalArgumentException("The 'relativeFilePath' parameter cannot be null or empty.");
        }
        
        if (relativeFilePath.startsWith(Constants.FORWARD_SLASH) ||
                relativeFilePath.startsWith(Constants.BACKWARD_SLASH)) {
            // The course runtime for some reason does not use a forward or backward slash, and fails, so when the file is inserted
            // into the database, make sure the path is in a format that is expected by the runtime.
            throw new IllegalArgumentException ("The 'relativeFilePath' parameter cannot begin with a forward or backward slash.");
        }
        CourseRecord record = getCourseByPath(relativeFilePath);
        if (record == null) {
            
            String owner = userName;
            
            // Only in desktop mode does the owner get set to 'all'.
            if (CommonProperties.getInstance().getDeploymentMode().equals(DeploymentModeEnum.DESKTOP)) {
                owner = CourseRecord.PUBLIC_OWNER;
            } else if (!isModifiable) {
                // If the course file is not modifiable, then it is considered public.  In the future once
                // permissions are added more properly to GIFT, this logic may need to change.
                owner = CourseRecord.PUBLIC_OWNER;
            }

            // Add a new entry for to the course table.
            CourseRecord insertedRecord = new CourseRecord(relativeFilePath, owner);
            record = createCourseRecord(insertedRecord);
        }
        
        return record;
    }

    @Override
    public boolean deleteCourseRecord(String coursePath) {
        
        boolean success = false;
              
        CourseRecord courseRecord = getCourseByPath(coursePath);
        umsDbMgr.deleteCourseRecord(courseRecord);

        return success;
    }

    @Override
    public boolean hasSurveyContextChangeSinceLastValidation(Date courseFolderLastValidation, Integer surveyContextId, String username) {

        if(courseFolderLastValidation != null){
            
            Date surveyContextLastModification = getSurveyContextLastModifiedDate(surveyContextId, username);
            if(surveyContextLastModification != null){
                
                return surveyContextLastModification.after(courseFolderLastValidation);
            }
        }
        
        return true;
    }
    
    @Override
    public Date getSurveyContextLastModifiedDate(Integer surveyContextId, String username){
        
        if(surveyContextId == null){
            return null;
        }
        return Surveys.getSurveyContextLastModification(surveyContextId, username);        
    }
    
    @Override
    public void applyLMSRecommendations(String username, DomainOption domainOption)
            throws LmsIoException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException, UMSDatabaseException,
            FileValidationException, ConfigurationException, ProhibitedUserException{

        //get GIFT user id and LMS user name by provided user name using UserServices (UMS db)
        DbUser dbUser = UMSDatabaseManager.getInstance().getUserByUsername(username, true);
        LMSDataRequest dataRequest = new LMSDataRequest(dbUser.getLMSUserName());
        dataRequest.setPageSize(1); //only want the latest for this domain
        dataRequest.setShouldSortDescending(true);
        dataRequest.addDomainId(domainOption.getDomainId());

        //retrieve user's LMS course records
        LMSCourseRecords courseRecords = ServicesManager.getInstance().getDbServices().getLMSData(dbUser.getUserId(), dataRequest);

        DomainCourseFileHandler.updateCourseOptionByLMSRecords(domainOption, courseRecords);
    }
    
    @Override
    public void applyLMSRecommendations(String username, CourseOptionsWrapper courseOptionsWrapper)
            throws LmsIoException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException, UMSDatabaseException,
            FileValidationException, ConfigurationException, ProhibitedUserException{

        //get GIFT user id and LMS user name by provided user name using UserServices (UMS db)
        DbUser dbUser = UMSDatabaseManager.getInstance().getUserByUsername(username, true);
        LMSDataRequest dataRequest = new LMSDataRequest(dbUser.getLMSUserName());
        dataRequest.setPageSize(1); //only want the latest for this domain
        dataRequest.setShouldSortDescending(true);

        for(DomainOption option : courseOptionsWrapper.domainOptions.values()){
            dataRequest.addDomainId(option.getDomainId());
        }

        //retrieve user's LMS course records
        LMSCourseRecords courseRecords = getLMSData(dbUser.getUserId(), dataRequest);

        DomainCourseFileHandler.updateCourseOptionsByLMSRecords(courseOptionsWrapper, courseRecords);
    }
    

    @Override
    public void checkCourseSurveyReferences(CourseOptionsWrapper courseOptionsWrapper){

        //where to put the various survey validation requests for each course, mapped by course name
        Map<String, List<SurveyCheckRequest>> surveyValidationRequests = new HashMap<>();

        for(DomainOption course : courseOptionsWrapper.domainOptions.values()){

            gatherCourseSurveyReferences(course, courseOptionsWrapper, surveyValidationRequests);
        }

        if(!surveyValidationRequests.isEmpty()){

            try {
                SurveyListCheckRequest surveyCheckRequest = new SurveyListCheckRequest(surveyValidationRequests);
                SurveyCheckResponse surveyCheckResponse = Surveys.checkSurveyReferences(surveyCheckRequest);
                DomainCourseFileHandler.updateCourseOptionsBySurveyCheck(courseOptionsWrapper, surveyCheckResponse, surveyCheckRequest);
            } catch (Exception e) {
                throw new SurveyValidationException("Caught exception while validating survey references.", e);
            }
        }
    }

    @Override
    public void checkCourseSurveyReferences(DomainOption domainOption, CourseOptionsWrapper courseOptionsWrapper){

        //where to put the various survey validation requests for each course, mapped by course name
        Map<String, List<SurveyCheckRequest>> surveyValidationRequests = new HashMap<>();

        gatherCourseSurveyReferences(domainOption, courseOptionsWrapper, surveyValidationRequests);

        if(!surveyValidationRequests.isEmpty()){

            try {
                SurveyListCheckRequest surveyCheckRequest = new SurveyListCheckRequest(surveyValidationRequests);
                SurveyCheckResponse surveyCheckResponse = Surveys.checkSurveyReferences(surveyCheckRequest);
                DomainCourseFileHandler.updateCourseOptionsBySurveyCheck(domainOption, surveyCheckResponse, surveyCheckRequest);
            } catch (Exception e) {
                throw new SurveyValidationException("Caught exception while validating survey references.", e);
            }
        }
    }

    @Override
    public void gatherCourseSurveyReferences(DomainOption domainOption, CourseOptionsWrapper courseOptionsWrapper, 
            Map<String, List<SurveyCheckRequest>> surveyValidationRequests){

        //only gather survey references for a course that is valid to this point
        if(domainOption.getDomainOptionRecommendation() == null || !domainOption.getDomainOptionRecommendation().getDomainOptionRecommendationEnum().isUnavailableType()){

            DomainCourseFileHandler handler = courseOptionsWrapper.courseFileNameToHandler.get(domainOption.getDomainId());
            try{
                List<SurveyCheckRequest> handlerRequests = handler.buildSurveyValidationRequest();

                if(!handlerRequests.isEmpty()){
                    //are there any survey references to check in this course?

                    surveyValidationRequests.put(domainOption.getDomainId(), handlerRequests);
                }
            }catch(Throwable e){
                logger.error("Caught exception while building survey validation request for "+domainOption, e);
                DomainOptionRecommendation recommendation = new DomainOptionRecommendation(DomainOptionRecommendationEnum.UNAVAILABLE_SURVEY_VALIDATION);
                recommendation.setReason("There was a problem checking the survey references in this course.");
                recommendation.setDetails(e.getMessage());
                domainOption.setDomainOptionRecommendation(recommendation);
            }
        }
    }
    

    @Override
    public Map<String, String> copyCourseSurveyReferences(String username, String course, String courseName, AbstractFolderProxy courseFolder,
            ExternalSurveyMapper mapper,  HashMap<FileProxy, AbstractSchemaHandler> filesToUpdate) throws DetailedException {

        Map<String, String> fileToNewContent = new HashMap<>();
        try {

            FileTreeModel courseModel = FileTreeModel.createFromRawPath(course);

            FileProxy courseFile = courseFolder.getRelativeFile(courseModel.getFileOrDirectoryName());

            CourseDBImporter.copyCourseSurveyReferences(courseFile, mapper, filesToUpdate, courseFolder);

            for(FileProxy fileToUpdate  : filesToUpdate.keySet()) {

                String content = null;
                AbstractSchemaHandler handler = filesToUpdate.get(fileToUpdate);
                if(handler instanceof DomainCourseFileHandler){
                    DomainCourseFileHandler courseHandler = (DomainCourseFileHandler) handler;
                    // Note: ignoring invalid schema document to prevent ignored course objects from causing exceptions
                    content = AbstractSchemaHandler.getAsXMLString(courseHandler.getCourse(), AbstractSchemaHandler.COURSE_ROOT, AbstractSchemaHandler.COURSE_SCHEMA_FILE, true);

                } else if (handler instanceof DomainDKFHandler) {
                    DomainDKFHandler dkfHandler = (DomainDKFHandler) handler;
                    content = AbstractSchemaHandler.getAsXMLString(dkfHandler.getScenario(), AbstractSchemaHandler.DKF_ROOT, AbstractSchemaHandler.DKF_SCHEMA_FILE);
                }

                if(content != null) {
                    String relativePath = courseFolder.getRelativeFileName(fileToUpdate);
                    FileTreeModel fileModelToUpdate = courseModel.getParentTreeModel().getModelFromRelativePath(relativePath);                    
                    fileToNewContent.put(fileModelToUpdate.getRelativePathFromRoot(), content);
                } else {
                    throw new DetailedException("There was a problem copying the course's survey resources.", "The file " + fileToUpdate.getName() + " has no content.", null);
                }
            }

        } catch(DetailedException e) {
            throw e;

        } catch (Exception e) {
            throw new DetailedException("There was a problem copying the course's survey resources.",
                    "An exception was caught: "+e.toString(),
                    e);
        }

        return fileToNewContent;
    }

    @Override
    public void pastSessionLearnerStateUpdated(AbstractKnowledgeSession knowledgeSession, DomainSession domainSession, 
            LearnerState newLearnerState, LearnerState oldLearnerState) throws LmsIoException {
        lmsDbMgr.pastSessionLearnerStateUpdated(knowledgeSession, domainSession, newLearnerState, oldLearnerState);
    }

    @Override
    public void pastSessionCourseRecordUpdated(AbstractKnowledgeSession knowledgeSession, AssessmentChainOfCustody chainOfCustody,
            LMSCourseRecord newCourseRecord, LMSCourseRecord oldCourseRecord, Concepts.Hierarchy concepts) throws LmsIoException{
        lmsDbMgr.pastSessionCourseRecordUpdated(knowledgeSession, chainOfCustody, newCourseRecord, oldCourseRecord, concepts);
    }

    @Override
    public List<String> getCourseDomainSessionLogFileNames(CourseRecord courseRecord) {
        return umsDbMgr.getCourseDomainSessionLogFileNames(courseRecord);
    }
    
    @Override
    public List<String> getLrsNames(){
        return lmsDbMgr.getLrsNames();
    }

}
