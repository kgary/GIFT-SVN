/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.course.TrainingApplicationWrapper;
import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.SurveyCheckRequest;
import mil.arl.gift.common.SurveyCheckResponse;
import mil.arl.gift.common.SurveyCheckResponse.FailureResponse;
import mil.arl.gift.common.SurveyCheckResponse.ResponseInterface;
import mil.arl.gift.common.SurveyListCheckRequest;
import mil.arl.gift.common.course.CourseFileValidationException;
import mil.arl.gift.common.course.CourseValidationResults;
import mil.arl.gift.common.course.GIFTValidationResults;
import mil.arl.gift.common.course.LearnerFileValidationException;
import mil.arl.gift.common.course.MetadataFileValidationException;
import mil.arl.gift.common.course.PedagogyFileValidationException;
import mil.arl.gift.common.course.SensorFileValidationException;
import mil.arl.gift.common.course.dkf.DKFValidationException;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.AbstractSchemaHandler.FileType;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.io.UriUtil;
import mil.arl.gift.common.io.UriUtil.InternetConnectionStatusEnum;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.ListOption;
import mil.arl.gift.common.survey.OptionList;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.domain.DomainCourseFileHandler;
import mil.arl.gift.domain.DomainDKFHandler;
import mil.arl.gift.domain.knowledge.common.AbstractLessonAssessment;
import mil.arl.gift.domain.knowledge.common.AbstractPerformanceAssessmentNode;
import mil.arl.gift.domain.knowledge.common.GIFTSurveyLessonAssessment;
import mil.arl.gift.domain.knowledge.common.QuestionAssessment;
import mil.arl.gift.domain.knowledge.common.SurveyReplyAssessment;
import mil.arl.gift.domain.knowledge.conversation.ConversationTreeFileHandler;
import mil.arl.gift.domain.knowledge.metadata.MetadataSchemaHandler;
import mil.arl.gift.domain.lessonmaterial.LessonMaterialFileHandler;
import mil.arl.gift.learner.LearnerConfigFileHandler;
import mil.arl.gift.ped.engine.EMAPConfigFileHandler;
import mil.arl.gift.sensor.SensorsConfigFileHandler;
import mil.arl.gift.ums.db.UMSDatabaseManager;
import mil.arl.gift.ums.db.survey.Surveys;
import mil.arl.gift.ums.db.table.DbSurveyContext;

/**
 * This class contains logic to allow authoring tools to validate content being authored in more ways
 * than just schema validation.
 * 
 * @author mhoffman
 *
 */
public class ValidationUtil {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ValidationUtil.class);
    
    /**
     * Validate the provided file against the schema and GIFT logic.
     * 
     * @param contentFile the file to validate.  Can't be null and must exist.
     * @param courseFolder the course folder the file is a descendant of.  Can't be null and must exist.
     * @param fileType the type of file being validated.  Can't be null.
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @param progressIndicator the progress indicator
     * @return validation results for the file
     * @throws FileValidationException if there was an issue validating (against the schema) the file
     * @throws DKFValidationException  if there was a problem validating a DKF (including DKFs referenced by course and training app reference files)
     * @throws CourseFileValidationException  if there was a problem validating a course file
     * @throws ConfigurationException  if there was a general problem not specific to a file
     * @throws SensorFileValidationException  if there was a problem validating a sensor configuration file
     * @throws PedagogyFileValidationException if there was a problem validating a pedagogical configuration file
     * @throws MetadataFileValidationException  if there was a problem validating a metadata file (including referenced by course files)
     */
    public static GIFTValidationResults validateFile(FileProxy contentFile, AbstractFolderProxy courseFolder, FileType fileType, 
            boolean failOnFirstSchemaError, ProgressIndicator progressIndicator) 
            throws FileValidationException, DKFValidationException, CourseFileValidationException, ConfigurationException, SensorFileValidationException, 
            PedagogyFileValidationException, MetadataFileValidationException{

        switch(fileType){
        
            case COURSE:
                return validateCourse(contentFile, courseFolder, failOnFirstSchemaError, true, progressIndicator);
            case DKF:
                return validateDKF(contentFile, courseFolder, failOnFirstSchemaError);
            case INTEROP_CONFIGURATION:
                //ignore for now, there is not authoring tool yet
                break;
            case LEARNER_CONFIGURATION:
                validateLearnerConfiguration(contentFile, failOnFirstSchemaError);
                return null;  //TODO: revisit returning something useful here
            case LESSON_MATERIAL_REF:
                return validateLessonMaterialReference(contentFile, courseFolder, failOnFirstSchemaError);
            case METADATA:
                return validateMetadata(contentFile, courseFolder, failOnFirstSchemaError);
            case EMAP_PEDAGOGICAL_CONFIGURATION:
                validatePedagogyConfiguration(contentFile, failOnFirstSchemaError);
                return null;  //TODO: revisit returning something useful here
            case SENSOR_CONFIGURATION:
                validateSensorConfiguration(contentFile, failOnFirstSchemaError);
                return null; //TODO: revisit returning something useful here
            case TRAINING_APP_REFERENCE:
                return validateTrainingAppReference(contentFile, courseFolder, failOnFirstSchemaError);
            case CONVERSATION:
                return validateConversation(contentFile, courseFolder, failOnFirstSchemaError);
            default:
                throw new IllegalArgumentException("The file type of "+fileType+" is not handled.");
        }
        
        return null;
               
    }
    
    /**
     * Validate the course contained in the course folder.
     * 
     * @param courseFolder contains the files to validate
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @param progressIndicator will be populated with any progress updates related to validating the course.  Can be null if progress updates
     * are not wanted.
     * @throws FileValidationException if there was an issue validating (against the schema) the file
     * @throws DKFValidationException  if there was a problem validating a DKF (including DKFs referenced by course and training app reference files)
     * @throws CourseFileValidationException  if there was a problem validating a course file
     * @throws ConfigurationException  if there was a general problem not specific to a file
     * @throws SensorFileValidationException  if there was a problem validating a sensor configuration file
     * @throws PedagogyFileValidationException if there was a problem validating a pedagogical configuration file
     * @throws MetadataFileValidationException  if there was a problem validating a metadata file (including referenced by course files)
     * @throws DetailedException if there was a problem related to retrieving the course.xml file from the course folder
     */
    public static void validateCourseFolder(AbstractFolderProxy courseFolder, boolean failOnFirstSchemaError, ProgressIndicator progressIndicator)
            throws FileValidationException, DKFValidationException, CourseFileValidationException, ConfigurationException, SensorFileValidationException, 
            PedagogyFileValidationException, MetadataFileValidationException, DetailedException{
        
        //find course file
        List<FileProxy> courseFiles = new ArrayList<>();
        try{
            FileFinderUtil.getFilesByExtension(courseFolder, courseFiles, AbstractSchemaHandler.COURSE_FILE_EXTENSION);
        }catch(IOException e){
            throw new DetailedException("Failed to validate course folder.", "There was a problem searching the course folder named '"+courseFolder.getName()+"' for course.xml files : "+e.getMessage()+".", e);
        }
        
        if(courseFiles.isEmpty()){
            throw new DetailedException("Failed to validate course folder", "The course folder named '"+courseFolder.getName()+"' contains no course.xml files.", null);
        }else if(courseFiles.size() > 1){
            throw new DetailedException("Failed to validate course folder", "The course folder named '"+courseFolder.getName()+"' contains more than one course.xml file, therefore it violates the course folder requirements of having only one course.", null);
        }
        
        validateCourse(courseFiles.get(0), courseFolder, failOnFirstSchemaError, true, progressIndicator);
    }
    
    
    /**
     * Check the pedagogical configuration file for issues
     * 
     * @param contentFile the pedagogical configuration file to validate
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @throws PedagogyFileValidationException if there was a problem validating the file
     */
    public static void validatePedagogyConfiguration(FileProxy contentFile, boolean failOnFirstSchemaError) throws PedagogyFileValidationException{        
        @SuppressWarnings("unused")
        EMAPConfigFileHandler handler = new EMAPConfigFileHandler(contentFile, failOnFirstSchemaError);
    }
    
    /**
     * Check the training application reference file for issues.
     * 
     * @param contentFile the training application reference file to validate
     * @param courseFolder contains course files relevant to the training application reference file (e.g. dkf)
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @return validation results for the file
     * @throws FileValidationException if there was a problem parsing and validating the training application reference file against the schema
     * @throws DKFValidationException if there was a validation problem with the DKF referenced by the training application reference file
     * @throws DetailedException if there was a problem retrieving the DKF referenced by the training application reference file
     */
    public static GIFTValidationResults validateTrainingAppReference(FileProxy contentFile, AbstractFolderProxy courseFolder, 
            boolean failOnFirstSchemaError) 
            throws FileValidationException, DKFValidationException, DetailedException{

        UnmarshalledFile uFile = DomainCourseFileHandler.getTrainingAppReference(contentFile, failOnFirstSchemaError);
        return DomainCourseFileHandler.validateTrainingAppReference((TrainingApplicationWrapper) uFile.getUnmarshalled(), courseFolder, null, InternetConnectionStatusEnum.UNKNOWN);
    }
    
    /**
     * Check the conversation file for issues.
     * 
     * @param contentFile the conversation file to validate
     * @param courseFolder contains course files relevant to the conversation file (e.g. dkf)
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @return validation results for the file
     * @throws FileValidationException if there was a problem parsing and validating the conversation file against the schema
     * @throws DKFValidationException if there was a validation problem with the DKF referenced by the conversation file
     * @throws DetailedException if there was a problem retrieving the DKF referenced by the conversation file
     */
    public static GIFTValidationResults validateConversation(FileProxy contentFile, AbstractFolderProxy courseFolder, boolean failOnFirstSchemaError) 
            throws FileValidationException, DKFValidationException, DetailedException{
        
        ConversationTreeFileHandler handler = new ConversationTreeFileHandler(contentFile, failOnFirstSchemaError);
        return handler.checkConversation();
    }
    
    /**
     * Check the metadata file for issues.
     * 
     * @param contentFile the metadata file to validate
     * @param courseFolder contains course files relevant to the metadata file
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @return validation results for the file
     * @throws MetadataFileValidationException if there was a problem validating the file
     */
    public static GIFTValidationResults validateMetadata(FileProxy contentFile, AbstractFolderProxy courseFolder, boolean failOnFirstSchemaError) throws MetadataFileValidationException{        

        try {
            generated.metadata.Metadata metadata = MetadataSchemaHandler.getMetadata(contentFile, failOnFirstSchemaError);
            GIFTValidationResults validationResults = MetadataSchemaHandler.checkMetadata(contentFile.getFileId(), metadata, courseFolder, null, null, UriUtil.getInternetStatus(), failOnFirstSchemaError);
            return validationResults;
        }catch(DKFValidationException e){
            throw new MetadataFileValidationException("There was a problem with the DKF of \n'"+e.getFileName()+"'\nreferenced by the metadata file.\n"+e.getReason(), 
                    e.getDetails(),
                    contentFile.getFileId(), 
                    e);
        }
    }
    
    /**
     * Check the sensor configuration file for issues.
     * 
     * @param contentFile the sensor configuration file to validate
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @throws SensorFileValidationException if there was a problem validating the file
     */
    public static void validateSensorConfiguration(FileProxy contentFile, boolean failOnFirstSchemaError) throws SensorFileValidationException{
        
        @SuppressWarnings("unused")
        SensorsConfigFileHandler handler = new SensorsConfigFileHandler(contentFile);
    }
    
    /**
     * Check the lesson material reference file for issues.
     * 
     * @param contentFile the lesson material reference file to validate
     * @param courseFolder contains course files relevant for the media reference in the lesson material file
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @return validation results for the file
     * @throws DetailedException if there was a problem validating the file
     */
    public static GIFTValidationResults validateLessonMaterialReference(FileProxy contentFile, AbstractFolderProxy courseFolder, boolean failOnFirstSchemaError) throws DetailedException{
        
        try{
            LessonMaterialFileHandler handler = new LessonMaterialFileHandler(contentFile, courseFolder, InternetConnectionStatusEnum.UNKNOWN);
            handler.parse(failOnFirstSchemaError);
            return handler.validateLessonMaterial(null, courseFolder);
        }catch(Exception e){
            throw new DetailedException("Failed to validate lesson material reference file.", e.getMessage(), e);
        }
    }
    
    /**
     * Check the learner configuration file for issues.
     * 
     * @param contentFile the learner configuration file to validate
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @throws LearnerFileValidationException if there was a problem validating the learner configuration file
     */
    public static void validateLearnerConfiguration(FileProxy contentFile, boolean failOnFirstSchemaError) throws LearnerFileValidationException{
        
        @SuppressWarnings("unused")
        LearnerConfigFileHandler handler = new LearnerConfigFileHandler(contentFile, failOnFirstSchemaError);
    }
    
    /**
     * Check the course content for issues.
     * 
     * Note: this logic utilizes the Domain Course Handler parse and validate logic in addition to 
     * checking the the UMS survey database references by using a direct connection to that database.
     * This direct connection is something that the Domain module doesn't have, instead the domain module
     * communicates to the UMS module during GIFT runtime via GIFT messages.
     * 
     * @param contentFile - the course content file
     * @param courseFolder - the course folder containing all course files
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @param validateSurveyReferences whether to validation the survey references in the course.  This value will be ignored if CommonProperties.getInstance().shouldUseDBConnection()
     * return false.
     * @param progressIndicator will be populated with any progress updates related to validating the course.  Can be null if progress updates
     * are not wanted.
     * @return validation results for the file
     * @throws DKFValidationException if there was a problem building the dkf objects
     * @throws FileValidationException if there was an issue validating (against the schema) the course or any related GIFT file
     * @throws CourseFileValidationException if there was a problem building the course objects
     * @throws ConfigurationException if there was a general problem
     */
    public static CourseValidationResults validateCourse(FileProxy contentFile, AbstractFolderProxy courseFolder, 
            boolean failOnFirstSchemaError, boolean validateSurveyReferences, ProgressIndicator progressIndicator) 
            throws FileValidationException, DKFValidationException, CourseFileValidationException, ConfigurationException{
        
        DomainCourseFileHandler courseHandler = new DomainCourseFileHandler(contentFile, courseFolder, failOnFirstSchemaError);
        final InternetConnectionStatusEnum connectionStatus = UriUtil.getInternetStatus();
        courseHandler.setCurrentInternetStatus(connectionStatus);
        CourseValidationResults courseValidationResults = courseHandler.checkCourse(failOnFirstSchemaError, progressIndicator);
        
        if(!courseValidationResults.hasCriticalIssue() && CommonProperties.getInstance().shouldUseDBConnection() && validateSurveyReferences){
            //don't waste time performing additional validation if there a critical validation issue was already detected
            
            //
            //validate Survey Database references
            //
            validateSurveyReferences(courseHandler, contentFile, courseFolder, courseValidationResults);
        }
        
        return courseValidationResults;
    }

    /**
     * Validate any survey references found in GIFT XML files in the course folder specified.
     * 
     * @param courseHandler used to search for survey references used by the course
     * @param courseXMLFile simply used to map survey validation requests to the course XML file 
     * @param courseFolder used to get the name of the course folder file in case of a failure in validating a survey reference
     * @param courseValidationResults contains survey reference validation issues, if any are found
     */
    public static void validateSurveyReferences(DomainCourseFileHandler courseHandler, FileProxy courseXMLFile, AbstractFolderProxy courseFolder, CourseValidationResults courseValidationResults){
        
        List<SurveyCheckRequest> surveyCheckRequests = null;
        SurveyCheckResponse surveyCheckResponse = null;
        try{
            surveyCheckRequests = courseHandler.buildSurveyValidationRequest();
            Map<String, List<SurveyCheckRequest>> surveyValidationRequests = new HashMap<>();
            surveyValidationRequests.put(courseXMLFile.getFileId(), surveyCheckRequests);
            surveyCheckResponse = Surveys.checkSurveyReferences(new SurveyListCheckRequest(surveyValidationRequests));
        }catch(DetailedException e){
            courseValidationResults.setCriticalIssue(e);
        }catch(Exception e){
            courseValidationResults.setCriticalIssue(
                    new DetailedException("There was a problem while validating the survey references in the course.",
                            "An exception was thrown with the message "+e.getMessage(), e));
        }
        
        if(surveyCheckRequests != null && surveyCheckResponse != null){
            //check response for failures
            
            for(String courseId : surveyCheckResponse.getResponses().keySet()){
                
                List<ResponseInterface> responses = surveyCheckResponse.getResponses().get(courseId);
                for(ResponseInterface response : responses){
                    
                    if(response instanceof FailureResponse){
                        
                        String fileName = surveyCheckRequests.get(responses.indexOf(response)).getSourceReference().getFileId();
                        try {
                            fileName = courseFolder.getRelativeFileName(surveyCheckRequests.get(responses.indexOf(response)).getSourceReference());
                        } catch (@SuppressWarnings("unused") Exception e) {
                            fileName = fileName.substring(fileName.indexOf(courseFolder.getName()));
                        }
                        
                        FailureResponse failureResponse = (FailureResponse)response;
                        
                        StringBuilder msg = new StringBuilder();
                        msg.append("There are invalid survey references in the course. ");
                        
                        if(fileName.endsWith(AbstractSchemaHandler.DKF_FILE_EXTENSION)) {
                            msg.append("Please check the Real-Time Assessments in any External Application or Adaptive Courseflow objects.");
                        }
                        
                        Integer courseObjectIndex = ((FailureResponse) response).getCourseObjectIndex();
                        if(courseObjectIndex != null) {
                            courseValidationResults.addImportantIssue(
                                    new FileValidationException(msg.toString(), 
                                        failureResponse.getMessage(), 
                                        fileName,
                                        null), 
                                    courseObjectIndex);
                            
                        } else {
                            courseValidationResults.addWarningIssue(
                                    new FileValidationException(msg.toString(), 
                                            failureResponse.getMessage(), 
                                            fileName,
                                            null));      
                        }
                    }
                    
                }
                
            }
        }
    }
    
    

    /**
     * Check the DKF contents for issues.
     * 
     * Note: this logic utilizes the Domain DKF Handler parse and validate logic in addition to 
     * checking the the UMS survey database references by using a direct connection to that database.
     * This direct connection is something that the Domain module doesn't have, instead the domain module
     * communicates to the UMS module during GIFT runtime via GIFT messages.
     * 
     * @param contentFile - the DKF content file
     * @param courseFolder the course folder used for relative paths in the DKF
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @return validation results for the file
     * @throws FileValidationException Thrown when there is a problem validating the DKF
     * @throws DKFValidationException if there was a problem building the DKF objects
     * @throws ConfigurationException if there was a problem connecting to the UMS database
     */
    public static GIFTValidationResults validateDKF(FileProxy contentFile, AbstractFolderProxy courseFolder,
            boolean failOnFirstSchemaError)
            throws FileValidationException, DKFValidationException, ConfigurationException {

        if(contentFile == null){
            throw new IllegalArgumentException("The DKF content file of "+contentFile+" doesn't exist.");
        }
        
        GIFTValidationResults validationResults;
        
        DomainDKFHandler dkfHandler;
        try{
            dkfHandler = new DomainDKFHandler(contentFile, courseFolder, null, failOnFirstSchemaError);
            validationResults = dkfHandler.checkDKF(null, null);
        }catch(Throwable t){
            validationResults = new GIFTValidationResults();
            validationResults.setCriticalIssue(
                new DetailedException("There was a problem while validating the real time assessment (DKF) referenced in the course.",
                    "An exception was thrown while checking the DKF file "+contentFile.getName()+" with the message "+t.getMessage(), t));
            return validationResults;
        }
        
        if(CommonProperties.getInstance().shouldUseDBConnection()){

            //
            //validate Survey Database references
            //
            
            UMSDatabaseManager dbMgr = UMSDatabaseManager.getInstance();           
            
            //survey context (optional element) 
            Integer surveyContextId = dkfHandler.getDomainAssessmentKnowledge().getScenario().getResources().getSurveyContextId();
            DbSurveyContext sContext = null;
            if(surveyContextId != null){
                sContext = dbMgr.selectRowById(surveyContextId, DbSurveyContext.class);
                if(sContext == null){
                    validationResults.setCriticalIssue( new DKFValidationException("Unable to find a survey context with id "+surveyContextId,
                            "The survey context must exist in the Survey database.  Survey Context can be authored using the Survey Authoring System.",
                            contentFile.getFileId(),
                            null));
                    return validationResults;
                }
            }
            
            //survey GIFT keys                
            for(AbstractPerformanceAssessmentNode node : dkfHandler.getDomainAssessmentKnowledge().getScenario().getPerformanceNodes().values()){
                
                for(AbstractLessonAssessment assessment : node.getAssessments()){
                    
                    if(assessment instanceof GIFTSurveyLessonAssessment){
                        
                        GIFTSurveyLessonAssessment surveyAssessment = (GIFTSurveyLessonAssessment)assessment;
                        
                        if(surveyContextId != null && sContext != null){
                            Survey survey = Surveys.getSurveyContextSurvey(surveyContextId, surveyAssessment.getGiftKey());
                            if(survey == null){
                                validationResults.setCriticalIssue(  new DKFValidationException("Unable to find the survey gift key of "+surveyAssessment.getGiftKey()+" in the survey context in survey context "+sContext.getName()+" ("+surveyContextId+")",
                                        "The survey context must contain the survey key in order to validate that survey.",
                                        contentFile.getFileId(),
                                        null));
                                return validationResults;
                            }
                        }else{
                            validationResults.setCriticalIssue(  new DKFValidationException("The survey context id was not provided therefore any GIFT surveys can not be validated.",
                                    "A survey context is used to gather surveys that you would like to present in a course.  A survey context can be authored in the Survey Authoring System.",
                                    contentFile.getFileId(),
                                    null));
                            return validationResults;
                        }
                        
                        //survey questions
                        for(QuestionAssessment qAssessment : surveyAssessment.getQuestionAssessments()){
                            

                            int surveyQuestionId = qAssessment.getQuestionId();
                            AbstractSurveyQuestion<?> sQuestion = Surveys.getSurveyQuestion(surveyQuestionId);
                            if(sQuestion == null){
                                
                                validationResults.setCriticalIssue(  new DKFValidationException("Unable to find the survey question with id "+surveyQuestionId,
                                        "Please make sure the question is part of the survey with key of '"+surveyAssessment.getGiftKey()+"' in survey context "+sContext.getName()+" ("+surveyContextId+").",
                                        contentFile.getFileId(),
                                        null));
                                return validationResults;
                            }
                            
                            OptionList replyOptions = Surveys.getOptionList(sQuestion);
                            
                            //survey question replies
                            boolean found;
                            for(SurveyReplyAssessment rAssessment : qAssessment.getReplyAssessments()){
                                
                                found = false;
                                int surveyReplyId = rAssessment.getReplyId();
                                if(replyOptions != null){
                                    for (ListOption reply : replyOptions.getListOptions()) {
    
                                        if (reply.getId() == surveyReplyId) {
                                            found = true;
                                        }
    
                                    }
                                }

                                if (!found) {
                                    validationResults.setCriticalIssue(  new DKFValidationException("The question answer w/ id " + surveyReplyId + " was not found as an option for the survey question " + surveyQuestionId,
                                            "Please check that the answer selected is an answer choice for the question "+surveyQuestionId+" in the survey with key of '"+surveyAssessment.getGiftKey()+"' in survey context "+sContext.getName()+" ("+surveyContextId+").",
                                            contentFile.getFileId(),
                                            null));
                                    return validationResults;
                                }

                            }//end for 
                        }//end for

                    }//end if
                }
            }

        }else{
            logger.warn("Not validating survey related keys because the common authoring property for connecting to the database is set to false");
        }
        
        return validationResults;
    }
}
