/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import generated.course.AuthoredBranch;
import generated.course.AuthoredBranch.Paths.Path;
import generated.course.BooleanEnum;
import generated.course.DISInteropInputs;
import generated.course.HAVENInteropInputs;
import generated.course.LogFile.DomainSessionLog;
import generated.course.LtiProvider;
import generated.course.Media;
import generated.course.MobileApp;
import generated.course.Nvpair;
import generated.course.RIDEInteropInputs;
import generated.course.ShowAvatarInitially;
import generated.course.TrainingApplication;
import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.DomainOption.DomainOptionPermissions;
import mil.arl.gift.common.DomainOption.DomainOptionRecommendation;
import mil.arl.gift.common.GetKnowledgeAssessmentSurveyRequest;
import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.common.LMSCourseRecords;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.SurveyCheckRequest;
import mil.arl.gift.common.SurveyCheckRequest.Question;
import mil.arl.gift.common.SurveyCheckRequest.Reply;
import mil.arl.gift.common.SurveyCheckResponse;
import mil.arl.gift.common.SurveyCheckResponse.FailureResponse;
import mil.arl.gift.common.SurveyCheckResponse.ResponseInterface;
import mil.arl.gift.common.SurveyListCheckRequest;
import mil.arl.gift.common.course.AbstractCourseHandler;
import mil.arl.gift.common.course.CourseConceptsUtil;
import mil.arl.gift.common.course.CourseFileValidationException;
import mil.arl.gift.common.course.CourseValidationResults;
import mil.arl.gift.common.course.CourseValidationResults.CourseObjectValidationResults;
import mil.arl.gift.common.course.GIFTValidationResults;
import mil.arl.gift.common.course.GIFTValidationResults.DetailedExceptionSerializedWrapper;
import mil.arl.gift.common.course.LearnerFileValidationException;
import mil.arl.gift.common.course.PedagogyFileValidationException;
import mil.arl.gift.common.course.SensorFileValidationException;
import mil.arl.gift.common.course.TrainingAppUtil;
import mil.arl.gift.common.course.dkf.DKFValidationException;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.DomainOptionRecommendationEnum;
import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.enums.SharedCoursePermissionsEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.io.MemoryFileServletRequest;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.common.io.TimeUtil;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.io.UriUtil;
import mil.arl.gift.common.io.UriUtil.InternetConnectionStatusEnum;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.domain.knowledge.Concept;
import mil.arl.gift.domain.knowledge.CourseConceptSearchFilter;
import mil.arl.gift.domain.knowledge.IntermediateConcept;
import mil.arl.gift.domain.knowledge.Scenario;
import mil.arl.gift.domain.knowledge.Task;
import mil.arl.gift.domain.knowledge.common.AbstractLessonAssessment;
import mil.arl.gift.domain.knowledge.common.AbstractPerformanceAssessmentNode;
import mil.arl.gift.domain.knowledge.common.GIFTSurveyLessonAssessment;
import mil.arl.gift.domain.knowledge.common.QuestionAssessment;
import mil.arl.gift.domain.knowledge.common.SurveyReplyAssessment;
import mil.arl.gift.domain.knowledge.condition.AbstractCondition;
import mil.arl.gift.domain.knowledge.condition.autotutor.AutoTutorWebServiceInterface;
import mil.arl.gift.domain.knowledge.condition.autotutor.AutoTutorWebServiceInterfaceCondition;
import mil.arl.gift.domain.knowledge.conversation.AutoTutorModel;
import mil.arl.gift.domain.knowledge.conversation.ConversationTreeFileHandler;
import mil.arl.gift.domain.knowledge.metadata.MetadataFileFinder;
import mil.arl.gift.domain.knowledge.metadata.MetadataFileSearchResult;
import mil.arl.gift.domain.knowledge.metadata.MetadataSchemaHandler;
import mil.arl.gift.domain.knowledge.metadata.MetadataSearchCriteria;
import mil.arl.gift.domain.lessonmaterial.LessonMaterialFileHandler;
import mil.arl.gift.tools.authoring.common.conversion.AbstractConversionWizardUtil;

/**
 * This class is responsible for parsing and validating a domain course file.
 *
 * @author mhoffman
 *
 */
public class DomainCourseFileHandler extends AbstractCourseHandler {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(DomainCourseFileHandler.class);

    /**
     * format of the last successful course validation time stamp saved in an element in the course.xml
     * Note: using 'Z' and not 'z' to show "-0500" instead of "EST" since the resulting string is used on
     * the GAT GWT client which doesn't currently support parsing time-zones but can support time offset values.
     */
    public static final SimpleDateFormat ValidationTimeFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss:SSS Z");    
    
    /** the gateway module PPT interface information needed when created a PPT training application transition */
    public static final String GW_PPT_IMPL = "gateway.interop.ppt.PPTInterface";

    /** the course file to parse */
    protected FileProxy file;

    /** the course folder that contains all course relevant files */
    protected AbstractFolderProxy courseFolder;

    /** the Course object created by parsing the course file */
    private generated.course.Course course;
    
    /** contains information about the course.xml file contents including whether the
     *  xml was up-converted by GIFT, if the up-conversion had issues and if the xml structure is schema valid */
    private UnmarshalledFile unmarshalledFile; 

    /** used to indicate whether the Domain module has an Internet connection, useful during validation logic */
    private InternetConnectionStatusEnum currentInternetConnectionStatus = InternetConnectionStatusEnum.UNKNOWN;

    /** The message for LTI provider data that were removed during export. */
    public static final String LTI_EXPORT_PROVIDER_MESSAGE = "Please enter a new value. It was removed during export.";
    
    /**
     * the name of the LMS course record given for each record recorded when a course object is completed.
     * For now, this is placed here instead of LMSProgressReporter due to not wanting to bring in additional
     * classes to the domain services jar (see build.xml).
     */
    public static final String COMPLETE_NODE_NAME = "Course Completed Percentage"; 

    /** used to cache the various CDT quadrant tagged metadata to only search for it once during course validation */
    private MetadataFileCache metadataFileCache = new MetadataFileCache();
    
    /** used to check for duplicate authored branch ids which is not allowed */
    private Set<Integer> authoredBranchIds = new HashSet<>();

    /**
     * Class constructor - parse and validate course file
     *
     * @param courseFile - the course file to parse. This MUST be updated to the latest version of the schema by this point.
     * @param courseFolder the course folder that contains all course relevant files
     * @param exitOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @throws CourseFileValidationException if there was a problem parsing and configuring the course
     */
    public DomainCourseFileHandler(FileProxy courseFile, AbstractFolderProxy courseFolder, boolean exitOnFirstSchemaError) throws CourseFileValidationException{
        super(COURSE_SCHEMA_FILE);

        setCourseFile(courseFile);
        setCourseFolder(courseFolder);

        try{
            unmarshalledFile = super.parseAndValidate(generated.course.Course.class, file.getInputStream(), exitOnFirstSchemaError);

            course = (generated.course.Course)unmarshalledFile.getUnmarshalled();

            // remove duplicate course concepts to prevent issues down the line.  Course concepts are case insensitive.
            // - for concept lists this will remove the concept
            // - for concept hierarchy this will add a suffix to the concept to make it unique
            CourseConceptsUtil.cleanCourseConcepts(course);

        } catch (DKFValidationException e) {
            throw new CourseFileValidationException( "Failed to validate the course because a problem occurred with a real time assessment (DKF):\n" + e.getReason(),
                    "An exception was thrown when handling the DKF named '"+e.getFileName()+"': " + e.getDetails(),
                    courseFile.getFileId(),
                    e);
        }  catch (LearnerFileValidationException e) {
            throw new CourseFileValidationException(
                    "A problem occurred with the learner configuration file: " + e.getReason(),
                    "An exception was thrown when handling the learner configuration file named '"+e.getFileName()+"': " + e.getDetails(),
                    courseFile.getFileId(),
                    e);
        } catch (PedagogyFileValidationException e) {
            throw new CourseFileValidationException(
                    "A problem occurred with the pedagogy configuration file: " + e.getReason(),
                    "An exception was thrown when handling the pedagogy configuration file named '"+e.getFileName()+"': " + e.getDetails(),
                    courseFile.getFileId(),
                    e);
        } catch (SensorFileValidationException e) {
            throw new CourseFileValidationException(
                    "A problem occurred with the sensor configuration file: " + e.getReason(),
                    "An exception was thrown when handling the sensor configuration file named '"+e.getFileName()+"': " + e.getDetails(),
                    courseFile.getFileId(),
                    e);
        } catch (CourseFileValidationException e){
            throw e;
        } catch (DetailedException e){
            throw new CourseFileValidationException(
                    e.getReason(),
                    e.getDetails(),
                    courseFile.getFileId(),
                    e);
        }catch(Throwable e){

            StringBuffer details =
                    new StringBuffer("A critical validation issue was found.  If you need additional help, please create a new thread in the forums on gifttutoring.org.\n\n<b>Validation Issue:</b>\n<ol>");

            if(e.getMessage() != null){
                details.append(e.getMessage());
            }else if(e.getCause() != null && e.getCause().getMessage() != null){
                details.append(e.getCause().getMessage());
            }

            details.append("\n\n<b>Help:</b> The most likely cause of this error is because the XML file content is not correctly formatted.  This could be anything from a missing XML tag")
                .append(" needed to ensure the general XML structure was followed (i.e. all start and end tags are found), to a missing required field (e.g. course name is")
                    .append(" required) or the value for a field doesn't satisfy the schema requirements (i.e. the course name must be at least 1 character).\n\n")
                            .append("Please take a look at the first part of stacktrace for a hint at the problem or ask for help on the GIFT <a href=\"https://gifttutoring.org/projects/gift/boards\" target=\"_blank\">forums</a>.\n\n")
                                    .append("<b>For Example: </b><div style=\"padding: 20px; border: 1px solid gray; background-color: #DDDDDD\">This example stacktrace snippet that indicates the course name ('#AnonType_nameCourse') value doesn't satisfy the minimum length requirement of 1 character:\n\n")
                                            .append("<i>javax.xml.bind.UnmarshalException - with linked exception: [org.xml.sax.SAXParseException; lineNumber: 2; columnNumber: 69; cvc-minLength-valid: Value '' with length = '0' is not facet-valid with respect to minLength '1' for type '#AnonType_nameCourse'</i></div>");

            throw new CourseFileValidationException(
                    "A problem occurred when parsing and validating the course file '"+courseFile.getName()+"'.",
                    details.toString(),
                    courseFile.getFileId(),
                    e);
        }

    }

    /**
     * Return the course instance.
     *
     * @return Course will not be null.
     */
    public generated.course.Course getCourse(){
        return course;
    }

    /**
     * Return the survey context associated with this course.
     *
     * @return the unique id of a survey context, if set, null if not set.  The survey context value is optional.
     */
    public Integer getSurveyContextId(){

        if(course.getSurveyContext() != null){
            return course.getSurveyContext().intValue();
        }else{
            return null;
        }
    }

    /**
     * Return the course's custom configuration information.
     *
     * @return can be null if no custom configurations were authored
     */
    public generated.course.Course.Configurations getCustomConfigurations(){
        return course.getConfigurations();
    }

    /**
     * Return the last successful validation value found in the provided course object.
     *
     * @param course contains the last successful validation timestamp to return.
     * @return the converted Date object from the validation timestamp value.  Will be null if the optional value was
     * not found or the course provided is null.
     */
    public static Date getLastSuccessfulValidation(generated.course.Course course){

        if(course == null){
            return null;
        }

        Date lastSuccessful = null;
        try{
            String lastSuccessfulValidation = course.getLastSuccessfulValidation();
            if(lastSuccessfulValidation != null){
                lastSuccessful = DomainCourseFileHandler.ValidationTimeFormat.parse(lastSuccessfulValidation);
            }
        }catch(@SuppressWarnings("unused") Exception e){}

        return lastSuccessful;
    }

    /**
     * Retrieves the list of Gateway interop implementation class names (e.g. gateway.interop.VBSPluginInterface)
     * from the course training application course elements.  If the course contains a Adaptive courseflow
     * course element, then the entire directory where the course is located is searched for trainingApp.xml files.
     * These files contain interop implementation class references.
     *
     * @return information about the interop implementations needed in this course.
     * @throws IOException if there was a problem retrieving or parsing interop related course files
     */
    public InteropImplementationsNeeded getInteropImplementations() throws IOException{

        InteropImplementationsNeeded interopImplementationsNeeded = new InteropImplementationsNeeded();
        int size;
        
        // the collection to return
        Set<String> interops = new HashSet<>();

        List<Serializable> transitions = course.getTransitions().getTransitionType();

        List<generated.course.MerrillsBranchPoint> adaptiveCourseflows = new ArrayList<>();
        for(Serializable transitionObj : transitions){

            if (isTransitionDisabled(transitionObj)) {
                // the course object will be skipped so don't analyze it
                if (logger.isInfoEnabled()) {
                    logger.info("skipping getInteropImplementations for disabled transition: " + transitionObj);
                }
            } else if(transitionObj instanceof generated.course.TrainingApplication){
                //get gateway interops directly referenced by this course object

                generated.course.TrainingApplication ta = (generated.course.TrainingApplication)transitionObj;
                if(ta.getInterops() != null) {
                    
                    //
                    // Handle Training Applications that act as gateways for team knowledge sessions differently
                    // If the dkf is a team assessment (more than 1 playable team member), than don't force joiners
                    // to need a gateway module
                    TrainingApplicationEnum taEnum = TrainingAppUtil.getTrainingAppType(ta);
                    if(taEnum == TrainingApplicationEnum.VBS || taEnum == TrainingApplicationEnum.HAVEN || taEnum == TrainingApplicationEnum.RIDE){
                        
                        generated.course.DkfRef dkfRef = ta.getDkfRef();
                        FileProxy dkfFile;
                        try{
                            dkfFile = courseFolder.getRelativeFile(dkfRef.getFile());  
                            DomainDKFHandler dkfHandler = new DomainDKFHandler(dkfFile, courseFolder, null, true);
                            size = interops.size();
                            if(dkfHandler.getDomainAssessmentKnowledge().getScenario().supportsMultipleLearners()){
                                //Gateway should be required for a team knowledge host but not joiners
                                interopImplementationsNeeded.hostGWAllowed = true;
                            }else{
                                interopImplementationsNeeded.hostGWAllowed = false;
                            }
                        }catch(IOException e){
                            logger.warn("Unable to determine if a "+taEnum.getDisplayName()+" DKF in the course is a team knowledge session because there was a problem while parsing the "+
                                    AbstractSchemaHandler.DKF_FILE_EXTENSION+" file of "+dkfRef.getFile(), e);
                        } 
                    }else{
                        interopImplementationsNeeded.hostGWAllowed = false;
                    }
                    
                    generated.course.Interops appInterops = ta.getInterops();
                    interopImplementationsNeeded.needsLogPlaybackTopic |= hasInteropNeedingPlayback(appInterops);
                    for(generated.course.Interop interop : appInterops.getInterop()){
                        
                        if(interop.getInteropInputs() != null 
                                && interop.getInteropInputs().getInteropInput() instanceof DISInteropInputs
                                && ((DISInteropInputs) interop.getInteropInputs().getInteropInput()).getLogFile() != null) {
                            // don't collect playback interops
                            continue;
                        } else if (interop.getInteropInputs() != null
                                && interop.getInteropInputs().getInteropInput() instanceof HAVENInteropInputs
                                && ((HAVENInteropInputs) interop.getInteropInputs().getInteropInput()).getLogFile() != null) {
                            // don't collect playback interops
                            continue;
                        } else if (interop.getInteropInputs() != null
                                && interop.getInteropInputs().getInteropInput() instanceof RIDEInteropInputs
                                && ((RIDEInteropInputs) interop.getInteropInputs().getInteropInput()).getLogFile() != null) {
                            // don't collect playback interops
                            continue;
                        } else {
                            interops.add(interop.getInteropImpl());
                        }
                    }

                }

            }else if(transitionObj instanceof generated.course.MerrillsBranchPoint){

                //collect for analysis against metadata files later
                adaptiveCourseflows.add((generated.course.MerrillsBranchPoint)transitionObj);
            }
        }//end for loop

        //
        // check the adaptive courseflow course objects against metadata files in the course folder
        //
        if(!adaptiveCourseflows.isEmpty()){

            List<FileProxy> metadataFiles = new ArrayList<>();
            FileFinderUtil.getFilesByExtension(courseFolder, metadataFiles, AbstractSchemaHandler.METADATA_FILE_EXTENSION);

            if(!metadataFiles.isEmpty()){

                // collect information about file parsing errors
                StringBuffer badFileErrors = new StringBuffer();

                Iterator<FileProxy> metadataFilesItr = metadataFiles.iterator();
                while(metadataFilesItr.hasNext()){

                    FileProxy metadataFile = metadataFilesItr.next();
                    try{
                        UnmarshalledFile unmarshalledMetadataFile = MetadataSchemaHandler.getUnmarshalledFile(metadataFile, FileType.METADATA);
                        generated.metadata.Metadata metadata = (generated.metadata.Metadata)unmarshalledMetadataFile.getUnmarshalled();

                        // whether the metadata file contains practice content
                        boolean isPracticeMetadata = metadata.getPresentAt() != null && MerrillQuadrantEnum.PRACTICE.getName().equalsIgnoreCase(metadata.getPresentAt().getMerrillQuadrant());

                        //
                        // Is this metadata used by an adaptive courseflow?
                        // i.e. all the metadata's concepts must be covered by at least one adaptive courseflow's concept list
                        //
                        boolean metadataConceptsTaught = true, adaptiveCourseflowContainsConcept;
                        int currentAdaptiveCourseflowIndex = 0;
                        for(int conceptIndex = 0; conceptIndex < metadata.getConcepts().getConcept().size(); conceptIndex++){

                            generated.metadata.Concept concept = metadata.getConcepts().getConcept().get(conceptIndex);

                            if(currentAdaptiveCourseflowIndex >= adaptiveCourseflows.size()){
                                // didn't find an adaptive courseflow that taught all the metadata concepts
                                metadataConceptsTaught = false;
                                break;
                            }

                            generated.course.MerrillsBranchPoint adaptiveCourseflow = adaptiveCourseflows.get(currentAdaptiveCourseflowIndex);

                            List<String> adaptiveCourseflowConcepts = null;
                            if(isPracticeMetadata){
                                //need to get the practice phase concepts (if authored) to check against the practice tagged metadata concepts

                                for(Serializable adaptiveCourseflowContent : adaptiveCourseflow.getQuadrants().getContent()){

                                    if(adaptiveCourseflowContent instanceof generated.course.Practice){
                                        generated.course.Practice practice = (generated.course.Practice)adaptiveCourseflowContent;
                                        adaptiveCourseflowConcepts = practice.getPracticeConcepts().getCourseConcept();
                                        break;
                                    }
                                }
                            }else{
                                adaptiveCourseflowConcepts = adaptiveCourseflow.getConcepts().getConcept();
                            }

                            // does the current adaptive courseflow contain the concept
                            adaptiveCourseflowContainsConcept = false;
                            if(adaptiveCourseflowConcepts != null){
                                for(String adaptiveCourseflowConcept : adaptiveCourseflowConcepts){

                                    if(adaptiveCourseflowConcept.equalsIgnoreCase(concept.getName())){
                                        adaptiveCourseflowContainsConcept = true;
                                        break;
                                    }
                                }
                            }

                            if(adaptiveCourseflowContainsConcept){
                                //it does, check the next metadata concept against this adaptive courseflow
                                continue;
                            }else{
                                //it does NOT, restart metadata concept check on the next adaptive courseflow
                                currentAdaptiveCourseflowIndex++; //grab the next adaptive courseflow
                                conceptIndex = -1; //start searching the metadata concepts again
                            }
                        }

                        if(!metadataConceptsTaught){
                            //this metadata has a concept that is not being taught by this course,
                            //therefore the metadata can be ignored for this method
                            continue;
                        }

                        //
                        // At this point the metadata file could be used by one or more adaptive courseflows,
                        // so we need to extract the interop implementation
                        //

                        if(metadata.getContent() instanceof generated.metadata.Metadata.Simple){
                            String simpleFileRef = ((generated.metadata.Metadata.Simple)metadata.getContent()).getValue();

                            String interopToAdd = null;

                            if (DomainCourseFileHandler.isSupportedPowerPointShow(simpleFileRef)) {
                                try {

                                    if(logger.isDebugEnabled()){
                                        logger.debug("Checking metadata for file: " + file.getName());
                                    }

                                    GIFTValidationResults validationResults = MetadataSchemaHandler.checkMetadata(file.getFileId(), metadata, courseFolder, null, null, UriUtil.getInternetStatus(), false);

                                    //if there are issues with the metadata file then it won't be used during course execution, therefore don't retrieve its interop
                                    if(!(validationResults.hasCriticalIssue() || validationResults.hasImportantIssues())){
                                        interopToAdd = GW_PPT_IMPL;

                                        if(logger.isDebugEnabled()){
                                            logger.debug("getInteropImplementations() found interop:  " + interopToAdd);
                                        }
                                    }
                                }catch(@SuppressWarnings("unused") Exception e){
                                    // Ignore any exceptions here (they will be logged with validation that occurs earlier.
                                    // If there is a validation error here, then it means that the interop should not be added to
                                    // the list.
                                }
                            }

                            if(interopToAdd != null){

                                if(logger.isDebugEnabled()){
                                    logger.debug("getInteropImplementations() adding interop:  " + interopToAdd);
                                }
                                interops.add(interopToAdd);
                                interopImplementationsNeeded.hostGWAllowed = false;
                            }

                        }else if(metadata.getContent() instanceof generated.metadata.Metadata.TrainingApp){

                            generated.metadata.Metadata.TrainingApp trainingApp = (generated.metadata.Metadata.TrainingApp)metadata.getContent();
                            String referenceFileName = trainingApp.getValue();
                            FileProxy trainingAppFile = courseFolder.getRelativeFile(referenceFileName);

                            try{

                                if(logger.isDebugEnabled()){
                                    logger.debug("Checking training app reference file: " + file.getName());
                                }
                                UnmarshalledFile unmarshalledTrainingAppFile = AbstractSchemaHandler.parseAndValidate(generated.course.TrainingApplicationWrapper.class,
                                        trainingAppFile.getInputStream(),
                                        AbstractSchemaHandler.COURSE_SCHEMA_FILE,
                                        true);
                                generated.course.TrainingApplicationWrapper taWrapper = (generated.course.TrainingApplicationWrapper)unmarshalledTrainingAppFile.getUnmarshalled();
                                generated.course.TrainingApplication ta = taWrapper.getTrainingApplication();
                                generated.course.Interops courseInterops = ta.getInterops();

                                if(courseInterops == null && ta.getEmbeddedApps() != null) {

                                    //if embedded applications are defined instead of interops, we don't need to check for interop impls
                                    continue;
                                }

                                //If course interops is null, the below for loop
                                //cannot be executed. Therefore we should skip to the
                                //next iteration. This also means that there are neither
                                //interops or embedded apps, which is a validation erros
                                if(courseInterops == null) {
                                    badFileErrors.append(
                                            "\nThere was a problem while parsing the file ")
                                            .append(file).append(": There are no embedded apps or interops in the training application. ")
                                            .append("If the application is an embedded application, specify a URL or HTML file using the authoring tools. ")
                                            .append("If the application is an interop application, specify what type of application it is using the authoring tools (e.g. PowerPoint)");
                                    continue;
                                }

                                //
                                // Handle Training Applications that act as gateways for team knowledge sessions differently
                                // If the dkf is a team assessment (more than 1 playable team member), than don't force joiners
                                // to need a gateway module
                                TrainingApplicationEnum taEnum = TrainingAppUtil.getTrainingAppType(ta);
                                size = interops.size();
                                if(size == 0){
                                    if(taEnum == TrainingApplicationEnum.VBS){
                                        
                                        generated.course.DkfRef dkfRef = ta.getDkfRef();
                                        FileProxy dkfFile;
                                        try{
                                            dkfFile = courseFolder.getRelativeFile(dkfRef.getFile());  
                                            DomainDKFHandler dkfHandler = new DomainDKFHandler(dkfFile, courseFolder, null, true);
                                            size = interops.size();
                                            if(dkfHandler.getDomainAssessmentKnowledge().getScenario().supportsMultipleLearners() && size == 0){
                                                //Gateway should be required for a team knowledge host but not joiners
                                                interopImplementationsNeeded.hostGWAllowed = true;
                                            }else{
                                                interopImplementationsNeeded.hostGWAllowed = false;
                                            }
                                        }catch(IOException e){
                                            logger.warn("Unable to determine if a DKF in the course is a team knowledge session because there was a problem while parsing the "+
                                                    AbstractSchemaHandler.DKF_FILE_EXTENSION+" file of "+dkfRef.getFile(), e);
                                        }                        
                                    }else{
                                        // found a non-gateway application with interops, therefore joiners will need to have their own gateway module.
                                        interopImplementationsNeeded.hostGWAllowed = false;
                                    }
                                }
                                
                                for(generated.course.Interop interop : courseInterops.getInterop()){

                                    if(logger.isDebugEnabled()){
                                        logger.debug("getInteropImplementations() adding training app file extension interop: " + interop.getInteropImpl());
                                    }
                                    interops.add(interop.getInteropImpl());
                                }

                            }catch(Exception e){
                                //ERROR
                                badFileErrors.append("\nThere was a problem while parsing the ").append(AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION).append(" file of ").append(file).append(": ").append(e.toString());
                            }
                        }

                    }catch(Exception e){
                        //ERROR - the metadata file isn't use-able so it won't be accessed during course execution,therefore
                        //        log it and move on.
                        badFileErrors.append("\nThere was a problem while parsing the ").append(AbstractSchemaHandler.METADATA_FILE_EXTENSION).append(" file of ").append(file).append(": ").append(e.toString());
                    }
                }

                if(badFileErrors.length() > 0){
                    logger.warn("The following files could not be analyzed for the Gateway interop plugin connections needed:" + badFileErrors.toString());
                }
            }
        } //end if

        interopImplementationsNeeded.interopImpls.addAll(interops);
        return interopImplementationsNeeded;
    }
    
    /**
     * Return whether any of the interop configurations provided are in need of a playback service.
     * @param appInterops the interops to process for a given training application course object
     * @return true if there is a playback configuration.  
     */
    public static boolean hasInteropNeedingPlayback(generated.course.Interops appInterops){
        
        if(appInterops == null){
            return false;
        }
        
        for(generated.course.Interop interop : appInterops.getInterop()){
            
            if (interop.getInteropInputs() != null) {
                if(interop.getInteropInputs().getInteropInput() instanceof DISInteropInputs
                        && ((DISInteropInputs) interop.getInteropInputs().getInteropInput()).getLogFile() != null) {
                    return true;
                } else if (interop.getInteropInputs().getInteropInput() instanceof HAVENInteropInputs
                        && ((HAVENInteropInputs) interop.getInteropInputs().getInteropInput()).getLogFile() != null) {
                    return true;
                } else if (interop.getInteropInputs().getInteropInput() instanceof RIDEInteropInputs
                        && ((RIDEInteropInputs) interop.getInteropInputs().getInteropInput()).getLogFile() != null) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * Searches the course for any reference to an embedded application. If there is a
     * reference to such an application, the url is placed in the returned list. This
     * method is used to determine whether or not a tutor topic should be allocated for
     * a given domain session
     * @return the list of urls for all embedded training applications within the course
     * @throws IOException if there was a problem retrieving or parsing embedded app related course files
     */
    public List<String> getEmbeddedUrls() throws IOException {

        List<String> urls = new ArrayList<>();

        List<Serializable> transitions = course.getTransitions().getTransitionType();

        boolean directoryChecked = false;
        for(Serializable transitionObj : transitions){

            if (isTransitionDisabled(transitionObj)) {
                if (logger.isInfoEnabled()) {
                    logger.info("skipping getEmbeddedUrls for disabled transition: " + transitionObj);
                }
            } else if(transitionObj instanceof generated.course.TrainingApplication){
                //get embeddedApps

                generated.course.TrainingApplication ta = (generated.course.TrainingApplication)transitionObj;
                if(ta.getEmbeddedApps() != null) {
                    generated.course.EmbeddedApps embeddedApps = ta.getEmbeddedApps();
                    generated.course.EmbeddedApp embeddedApp = embeddedApps.getEmbeddedApp();

                    String url;

                    if(embeddedApp.getEmbeddedAppImpl() instanceof MobileApp) {
                        url = embeddedApp.getEmbeddedAppImpl().getClass().getName();

                    } else {
                        url = (String) embeddedApp.getEmbeddedAppImpl();
                    }

                    if(!urls.contains(url)){
                        urls.add(url);
                    }
                }

            }else if(transitionObj instanceof generated.course.MerrillsBranchPoint){
                //check entire course directory for Training App reference files, get the embeddedApps from those files

                if(directoryChecked){
                    continue;
                }

                StringBuffer badFileErrors = new StringBuffer();

                // First - training app reference files
                List<FileProxy> files = new ArrayList<>();
                FileFinderUtil.getFilesByExtension(courseFolder, files, AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION);
                if(!files.isEmpty()){

                    for(FileProxy file : files){
                        try{

                            logger.debug("Checking training app reference file: " + file.getName());
                            UnmarshalledFile unmarshalledFile = AbstractSchemaHandler.parseAndValidate(generated.course.TrainingApplicationWrapper.class,
                                    file.getInputStream(),
                                    AbstractSchemaHandler.COURSE_SCHEMA_FILE,
                                    true);
                            generated.course.TrainingApplicationWrapper taWrapper = (generated.course.TrainingApplicationWrapper)unmarshalledFile.getUnmarshalled();
                            generated.course.TrainingApplication ta = taWrapper.getTrainingApplication();
                            generated.course.EmbeddedApps courseEmbeddedApps = ta.getEmbeddedApps();

                            if(courseEmbeddedApps == null) {
                                //If there are no course embedded apps, skip this training applcation
                                continue;
                            }

                            generated.course.EmbeddedApp embeddedApp = courseEmbeddedApps.getEmbeddedApp();

                            String url;

                            if(embeddedApp.getEmbeddedAppImpl() instanceof MobileApp) {
                                url = embeddedApp.getEmbeddedAppImpl().getClass().getName();

                            } else {
                                url = (String) embeddedApp.getEmbeddedAppImpl();
                            }

                            if(!urls.contains(url)){

                                if(logger.isDebugEnabled()){
                                    logger.debug("getEmbeddedAppUrls() adding training app file extension url: " + url);
                                }
                                urls.add(url);
                            }

                        }catch(Exception e){
                            //ERROR
                            badFileErrors.append("\nThere was a problem while parsing the ").append(AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION).append(" file of ").append(file).append(": ").append(e.getMessage());

                        }
                    }
                }

                if(badFileErrors.length() > 0){
                    logger.warn("The following files could not be analyzed for the embedded training application urls needed:" + badFileErrors.toString());
                }

                directoryChecked = true;
            }
        }

        return urls;
    }

    /**
     * Set the current Internet status of the domain module. This enumerated value
     * is useful for bypassing URL validation checks when it is already known that Internet websites will
     * fail.
     *
     * @param status the enumerated status to set
     */
    public void setCurrentInternetStatus(InternetConnectionStatusEnum status){
        currentInternetConnectionStatus = status;
    }

    /**
     * Set the course file that will be parsed and validated.
     *
     * @param courseFilename can't be null
     */
    private void setCourseFile(FileProxy courseFile){

        if(courseFile == null){
            throw new NullPointerException("The course file can't be null");
        }

        this.file = courseFile;
    }

    /**
     * Set the course folder that contains all course relevant files.
     *
     * @param courseFolder can't be null
     */
    private void setCourseFolder(AbstractFolderProxy courseFolder){

        if(courseFolder == null){
            throw new NullPointerException("The course folder can't be null");
        }

        this.courseFolder = courseFolder;
    }

    /**
     * Return the course folder used in this handler to retrieve and validate references in the course.
     *
     * @return the course folder, wont be null
     */
    public AbstractFolderProxy getCourseFolder(){
        return courseFolder;
    }
    
    /**
     * Return the course.xml unmarshalled file object that contains information about the course.xml file 
     * contents including whether the xml was up-converted by GIFT, if the up-conversion had issues and 
     * if the xml structure is schema valid
     * @return the unmarshalled file instance for this course.xml, won't be null.
     */
    public UnmarshalledFile getUnmarshalledFile(){
        return unmarshalledFile;
    }

    /**
     * Return a collection of survey requests based on the course transitions.  This is useful
     * for querying the survey database to validate the course's references to survey elements.
     *
     * @return List<SurveyCheckRequest> collection of survey checks that need to be performed.  Can be empty but not null.
     * @throws DKFValidationException if there is a problem parsing the dkf
     * @throws FileValidationException if there is a problem validating the DKF against the schema
     * @throws DetailedException if there was a problem retrieving or parsing course related files that have survey references.
     */
    public List<SurveyCheckRequest> buildSurveyValidationRequest() throws FileValidationException, DKFValidationException, DetailedException{

        List<SurveyCheckRequest> requests = new ArrayList<>();
        if(course != null){

            BigInteger courseSurveyContext = course.getSurveyContext();

            if(courseSurveyContext != null){
                //in case there are no transitions that use a survey context but the course has been associated with
                //a survey context, therefore need to make sure the survey context exists.
                SurveyCheckRequest request = new SurveyCheckRequest(courseSurveyContext.intValue());
                request.setSourceReference(this.file);
                requests.add(request);
            }

            List<FileProxy> trainingAppFileCache = null;

            //
            // Check transitions
            //
            List<Serializable> transitions = course.getTransitions().getTransitionType();

            for(int index = 0; index < transitions.size(); index++){

                Serializable transitionObj = transitions.get(index);

                // skip building survey validations if the transition is disabled
                if(isTransitionDisabled(transitionObj)){
                    if (logger.isInfoEnabled()) {
                        logger.info("skipping building survey validation for disabled transitionObj: " + transitionObj);
                    }
                }else if(transitionObj instanceof generated.course.TrainingApplication){
                    //found training application transition, check the dkf for survey references

                    generated.course.TrainingApplication tApp = ((generated.course.TrainingApplication)transitionObj);
                    generated.course.DkfRef dkfRef = tApp.getDkfRef();
                    String filename = dkfRef.getFile();
                    FileProxy dkfFile;
                    try{
                        dkfFile = courseFolder.getRelativeFile(filename);
                    }catch(IOException e){
                        throw new DetailedException("Failed to build the survey validation request for course named '"+course.getName()+"'.",
                                "The training application course element named '"+tApp.getTransitionName()+"' references the DKF '"+filename+"' which could not be retrieved.  The error reads:\n"+e.getMessage(),
                                e);
                    }

                    try{
                        buildSurveyValidationRequest(requests, dkfFile, index);
                    }catch(Exception e){
                        throw new DetailedException("Failed to gather the survey references in the '"+tApp.getTransitionName()+"' external application course object.",
                                "An exception was thrown while processing the real time assessment portion of this course object.", e);
                    }

                }else if(transitionObj instanceof generated.course.PresentSurvey){
                    //found present survey transition, check for a gift survey

                    generated.course.PresentSurvey presentSurvey = (generated.course.PresentSurvey)transitionObj;

                    Object surveyChoice = presentSurvey.getSurveyChoice();
                    if(surveyChoice instanceof String){
                        //found a GIFT key

                        if(courseSurveyContext == null){
                            throw new IllegalArgumentException("Can't validate the survey for the '"+presentSurvey.getTransitionName()+"' survey course object because the course survey context is null.");
                        }

                        String giftKey = (String)surveyChoice;
                        SurveyCheckRequest request = new SurveyCheckRequest(courseSurveyContext.intValue(), index, giftKey);
                        request.setSourceReference(this.file);
                        requests.add(request);

                    }else if(surveyChoice instanceof generated.course.PresentSurvey.ConceptSurvey){
                        //found a concept survey

                        generated.course.PresentSurvey.ConceptSurvey conceptSurvey = (generated.course.PresentSurvey.ConceptSurvey)surveyChoice;

                        if(courseSurveyContext == null){
                            throw new IllegalArgumentException("Can't validate the survey for the '"+presentSurvey.getTransitionName()+"' survey course object because the course survey context is null.");
                        }

                        String giftKey = conceptSurvey.getGIFTSurveyKey();  //a fixed value

                        List<generated.course.ConceptQuestions> concepts = new ArrayList<>();
                        for(generated.course.ConceptQuestions concept :  conceptSurvey.getConceptQuestions()){
                            concepts.add(concept);
                        }

                        if(concepts.isEmpty()){
                            throw new DetailedException("Unable to check the Question Bank survey for the the '"+presentSurvey.getTransitionName()+"' Question Bank course object.",
                                    "Please make sure one or more course concepts have been selected/provided in the '"+presentSurvey.getTransitionName()+"' Question Bank course object.", null);
                        }

                        SurveyCheckRequest request = new SurveyCheckRequest(courseSurveyContext.intValue(), index, giftKey);
                        request.setSourceReference(this.file);
                        request.setGetKnowledgeAssessmentSurveyRequest(GetKnowledgeAssessmentSurveyRequest.createRequestFromConceptSurvey(courseSurveyContext.intValue(), concepts));
                        requests.add(request);
                    }else if(surveyChoice instanceof generated.course.Conversation){
                        //nothing to validate right now

                    }

                }else if(transitionObj instanceof generated.course.MerrillsBranchPoint){
                    //found branch point...

                    generated.course.MerrillsBranchPoint merrill = (generated.course.MerrillsBranchPoint)transitionObj;

                    if(courseSurveyContext == null){
                        throw new IllegalArgumentException("Can't validate the surveys for the '"+merrill.getTransitionName()+"' Adaptive courseflow course object because the course survey context is null.");
                    }

                    //
                    // check Recall survey logic
                    //

                    for(Serializable transition : merrill.getQuadrants().getContent()){

                        if(transition instanceof generated.course.Recall){
                            //the recall survey is a concept knowledge based survey created during runtime
                            generated.course.Recall recallTransition = (generated.course.Recall) transition;

                            Serializable surveyChoice = recallTransition.getPresentSurvey().getSurveyChoice();

                            String giftKey;
                            List<generated.course.ConceptQuestions> recallConcepts = new ArrayList<>();

                            if(surveyChoice instanceof generated.course.Recall.PresentSurvey.ConceptSurvey){
                                //a fixed value
                                giftKey = ((generated.course.Recall.PresentSurvey.ConceptSurvey)surveyChoice).getGIFTSurveyKey();

                                for(generated.course.ConceptQuestions concept : ((generated.course.Recall.PresentSurvey.ConceptSurvey)surveyChoice).getConceptQuestions()){
                                    recallConcepts.add(concept);
                                }

                            }else if(surveyChoice instanceof String){
                                //a static gift survey

                                giftKey = (String) surveyChoice;
                            }else{
                                throw new IllegalArgumentException("Found unhandled Recall survey type of "+surveyChoice+" in the '"+merrill.getTransitionName()+"' Adaptive courseflow course object.");
                            }

                            SurveyCheckRequest request = new SurveyCheckRequest(courseSurveyContext.intValue(), index, giftKey);
                            request.setSourceReference(this.file);

                            if(!recallConcepts.isEmpty()){
                                request.setGetKnowledgeAssessmentSurveyRequest(GetKnowledgeAssessmentSurveyRequest.createRequestForConcepts(courseSurveyContext.intValue(), recallConcepts));
                            }

                            requests.add(request);
                        }

                    }

                }
            }//end for


            //
            // Find trainingApp.xml files to get their DKFs
            // Note: the reason this is outside of the for loop and the MBP transition check is to support an
            //       incomplete course export that maybe completed by the course importer.
            //
            if(trainingAppFileCache == null){
                trainingAppFileCache = new ArrayList<>();

                //look for training apps in course folder
                try{
                    FileFinderUtil.getFilesByExtension(courseFolder, trainingAppFileCache, AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION);
                }catch(IOException e){
                    throw new DetailedException("Failed to build the survey validation request for course named '"+course.getName()+"'.",
                            "There was a problem searching for all "+AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION+" files in the course folder '"+courseFolder.getName()+"'.  The error reads:\n"+e.getMessage(),
                            e);
                }

                for(FileProxy file : trainingAppFileCache){

                    try{
                        UnmarshalledFile unmarshalledFile = AbstractSchemaHandler.parseAndValidate(generated.course.TrainingApplicationWrapper.class,
                                file.getInputStream(),
                                AbstractSchemaHandler.COURSE_SCHEMA_FILE,
                                true);
                        generated.course.TrainingApplicationWrapper taWrapper = (generated.course.TrainingApplicationWrapper)unmarshalledFile.getUnmarshalled();
                        generated.course.DkfRef dkfRef = taWrapper.getTrainingApplication().getDkfRef();
                        String filename = dkfRef.getFile();
                        FileProxy dkfFile = courseFolder.getRelativeFile(filename);
                        buildSurveyValidationRequest(requests, dkfFile, null);

                    }catch(Exception e){
                        //ERROR
                        throw new DetailedException("Failed to build the survey validation request for course named '"+course.getName()+"'.",
                                "There was a problem while retrieving the survey references from the trainingapp.xml file of "+file+".  The error reads:\n"+e.getMessage(),
                                e);
                    }
                }
            }
        }

        return requests;
    }

    /**
     * Adds more survey requests to the provided collection based on the DKF file provided.  This is useful
     * for querying the survey database to validate the DKF's references to survey elements.
     *
     * @param requests collection of requests to add too
     * @param dkfFile the DKF to analyze for survey element references
     * @param index The index of the course object that contains the dkf reference. Can be null.
     * @throws FileValidationException Thrown when there is a problem validating the DKF
     * @throws DKFValidationException if there is a problem parsing and configuring the DKF
     * @throws DetailedException if there is a problem with resources, i.e. learner action file can't be found or accessed
     */
    public void buildSurveyValidationRequest(List<SurveyCheckRequest> requests, FileProxy dkfFile, Integer index) throws FileValidationException, DKFValidationException, DetailedException{

        if(dkfFile == null){
            //ERROR
            throw new IllegalArgumentException("The dkf file of "+dkfFile+" doesn't exist.");
        }

        //get the dkf parsed and validated against schema
        DomainDKFHandler dkfh = new DomainDKFHandler(dkfFile, getCourseFolder(), null, true);
        GIFTValidationResults validationResults = dkfh.checkDKF(null, null);
        if(validationResults.hasCriticalIssue() || validationResults.hasImportantIssues()){
            throw new DKFValidationException("DKF validation failed.",
                    "There was one or more issues during validation", dkfFile.getName(), validationResults.getFirstError());
        }

        Integer domainSurveyContext = dkfh.getDomainAssessmentKnowledge().getScenario().getResources().getSurveyContextId();
        if(domainSurveyContext == null){
            //the training app has no survey context reference, therefore it shouldn't have any survey references either.
            return;
        }else{
            //in case there are no nodes that use a survey context but the course has been associated with
            //a survey context, therefore need to make sure the survey context exists.
            SurveyCheckRequest request = new SurveyCheckRequest(domainSurveyContext, index);
            request.setSourceReference(dkfFile);
            requests.add(request);
        }

        //loop over each performance node (task/concept) to check for survey references
        for(AbstractPerformanceAssessmentNode node : dkfh.getDomainAssessmentKnowledge().getScenario().getPerformanceNodes().values()){

            //survey references are stored in assessment elements
            List<AbstractLessonAssessment> assessments = node.getAssessments();
            if(assessments != null && !assessments.isEmpty()){

                for(AbstractLessonAssessment assessment : assessments){

                    if(assessment instanceof GIFTSurveyLessonAssessment){

                        //build the survey check request given the survey references for this survey assessment object
                        GIFTSurveyLessonAssessment giftSurvey = (GIFTSurveyLessonAssessment)assessment;
                        String giftKey = giftSurvey.getGiftKey();
                        SurveyCheckRequest request = new SurveyCheckRequest(domainSurveyContext, index, giftKey);
                        request.setSourceReference(dkfFile);

                        for(QuestionAssessment qAss : giftSurvey.getQuestionAssessments()){

                            int qId = qAss.getQuestionId();
                            Question question = new Question(qId);

                            for(SurveyReplyAssessment rAss : qAss.getReplyAssessments()){

                                int rId = rAss.getReplyId();
                                Reply reply = new Reply(rId);
                                question.addReply(reply);
                            }

                            request.addQuestion(question);
                        }

                        requests.add(request);
                    }
                }
            }
        }
    }

    /**
     * Check the course content for errors.
     *
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @param progressIndicator will be populated with any progress updates related to validating the course.  Can be null if progress updates
     * are not wanted.
     * @return the validation results for a course including the validation results for each course object in the course, successful or not.
     * @throws IllegalArgumentException - if there was a problem with an argument to the method
     */
    public CourseValidationResults checkCourse(boolean failOnFirstSchemaError, ProgressIndicator progressIndicator) throws IllegalArgumentException{
        return checkCourse(course, courseFolder, currentInternetConnectionStatus, failOnFirstSchemaError, progressIndicator);
    }

    /**
     * Check the course content for errors.
     * Note: this method will not attempt to validate survey database references such as GIFT keys or concept survey elements.
     *
     * @param course - the course to check for issues
     * @param courseDirectory - the directory where the course file lives
     * @param domainDirectoryPath absolute path to the domain directory to use for domain content references (e.g. DKF relative path to Domain folder) found in the course file.
     * @param connectionStatus used to indicate whether the Domain module has an Internet connection
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @param progressIndicator will be populated with any progress updates related to validating the course.  Can be null if progress updates
     * are not wanted.
     * @return the validation results for a course including the validation results for each course object in the course, successful or not.
     * @throws IllegalArgumentException - if there was a problem with an argument to the method
     */
    public CourseValidationResults checkCourse(generated.course.Course course,
            AbstractFolderProxy courseDirectory, InternetConnectionStatusEnum connectionStatus, boolean failOnFirstSchemaError, ProgressIndicator progressIndicator)
                    throws IllegalArgumentException{

        if(course == null){
            throw new IllegalArgumentException("The course can't be null.");
        }else if(courseDirectory == null){
            throw new IllegalArgumentException("The course directory can't be null.");
        }

        if(logger.isInfoEnabled()){
            logger.info("Checking course named "+course.getName()+" in course folder of '"+courseDirectory.getName()+"'.");
        }

        CourseValidationResults courseValidationResults = new CourseValidationResults(course.getName());

        //check if the course directory has more than 1 course.xml which is not allowed in GIFT
        List<FileProxy> files = new ArrayList<>();
        try {
            FileFinderUtil.getFilesByExtension(courseDirectory, files, AbstractSchemaHandler.COURSE_FILE_EXTENSION);
        } catch (IOException e) {
            courseValidationResults.setCriticalIssue(new DetailedException("Failed to validate the course named '"+course.getName()+"'.",
                    "There was a problem while checking if the course folder '"+courseDirectory.getName()+"' has more than 1 course.xml file in it.\nError message:'"+e.getMessage()+"'.",
                    e));
            return courseValidationResults;

        }

        if(files.size() > 1){
            courseValidationResults.setCriticalIssue(new DetailedException("Failed to validate the course named '"+course.getName()+"'.",
                    "The course folder '"+courseDirectory.getName()+"' has "+files.size()+" course.xml files.  Course folders can only have one course.xml file.  Please separate the courses into separate course folders.",
                    null));
            return courseValidationResults;
        }

        //optional survey context element value
        BigInteger courseSurveyContext = course.getSurveyContext();

        //get a flattened list of the course concepts
        Set<String> courseConceptNames = new HashSet<>();
        try{
            getFlattenedConcepts(courseConceptNames, course.getConcepts());
        }catch(DetailedException e){
            courseValidationResults.setCriticalIssue(e);
            return courseValidationResults;
        }

        // Maintain list of course concepts referenced by Merrill's branch points thus far
        Set<String> courseConceptsTaught = new HashSet<>(courseConceptNames.size());

        //
        // Validate configuration files
        //
        if(course.getConfigurations() != null) {
            if(course.getConfigurations().getLearner() != null) {
                // Validate learner config
                FileProxy learnerConfig = null;
                try{
                    learnerConfig = courseDirectory.getRelativeFile(course.getConfigurations().getLearner());
                    if(learnerConfig.exists()) {
                        AbstractSchemaHandler.parseAndValidate(AbstractSchemaHandler.LEARNER_ROOT,
                                learnerConfig.getInputStream(), AbstractSchemaHandler.LEARNER_SCHEMA_FILE, failOnFirstSchemaError);
                    } else {
                        throw new FileNotFoundException("The learner configuration file could not be found: " + course.getConfigurations().getLearner());
                    }
                }catch(IOException e){
                    courseValidationResults.addImportantIssue(
                            new DetailedException("Failed to retrieve the learner configuration file referenced by the course.",
                            "There was an error while trying to retrieve the learner configuration of '"+courseDirectory+Constants.FORWARD_SLASH+course.getConfigurations().getLearner()+"' : "+e.getMessage(), e));
                } catch (JAXBException | SAXException e) {
                    courseValidationResults.addImportantIssue(
                            new LearnerFileValidationException("Failed to parse and validate the learner configuration file reference by the course.",
                            "There was an error when validating the learner configuration file of '"+courseDirectory+Constants.FORWARD_SLASH+course.getConfigurations().getLearner()+"' : "+e.getMessage(), learnerConfig.getFileId(), e));
                }
            }

            if(course.getConfigurations().getPedagogy() != null) {
                // Validate pedagogy config
                FileProxy pedConfig = null;
                try{
                    pedConfig = courseDirectory.getRelativeFile(course.getConfigurations().getPedagogy());
                    if(pedConfig.exists()) {
                        AbstractSchemaHandler.parseAndValidate(generated.ped.EMAP.class, pedConfig.getInputStream(),
                                AbstractSchemaHandler.EMAP_PEDAGOGICAL_SCHEMA_FILE, failOnFirstSchemaError);
                    } else {
                        throw new FileNotFoundException("The pedagogical configuration file could not be found: " + course.getConfigurations().getPedagogy());
                    }
                }catch(IOException e){
                    courseValidationResults.addImportantIssue(
                            new DetailedException("Failed to retrieve the pedagogical configuration file referenced by the course.",
                            "There was an error while trying to retrieve the pedagogical configuration of '"+courseDirectory+Constants.FORWARD_SLASH+course.getConfigurations().getPedagogy()+"' : "+e.getMessage(), e));
                } catch (JAXBException | SAXException e) {
                    courseValidationResults.addImportantIssue(
                            new PedagogyFileValidationException("Failed to parse and validate the pedagogical configuration file reference by the course.",
                            "There was an error when validating the pedagogical configuration file of '"+pedConfig.getFileId()+"' : "+e.getMessage(), pedConfig.getFileId(), e));
                }
            }

            if(course.getConfigurations().getSensor() != null) {
                // Validate sensor config
                FileProxy sensorConfig = null;
                try{
                    sensorConfig = courseDirectory.getRelativeFile(course.getConfigurations().getSensor());
                    if(sensorConfig.exists()) {
                        AbstractSchemaHandler.parseAndValidate(generated.sensor.SensorsConfiguration.class, sensorConfig.getInputStream(),
                                AbstractSchemaHandler.SENSOR_SCHEMA_FILE, failOnFirstSchemaError);
                    } else {
                        throw new FileNotFoundException("The sensor configuration file could not be found: " + course.getConfigurations().getSensor());
                    }
                }catch(IOException e){
                    courseValidationResults.addImportantIssue(
                            new DetailedException("Failed to retrieve the sensor configuration file referenced by the course.",
                            "There was an error while trying to retrieve the sensor configuration of '"+courseDirectory+Constants.FORWARD_SLASH+course.getConfigurations().getSensor()+"' : "+e.getMessage(), e));
                } catch (JAXBException | SAXException e) {
                    courseValidationResults.addImportantIssue(
                            new SensorFileValidationException("Failed to parse and validate the sensor configuration file reference by the course.",
                            "There was an error when validating the sensor configuration file of '"+sensorConfig.getFileId()+"' : "+e.getMessage(), sensorConfig.getFileId(), e));
                }
            }
        }

        //
        // Check that ltiProviders have the appropriate fields. Only checks the course authored LTI providers.
        //
        List<LtiProvider> ltiProviders = null;
        if (course.getLtiProviders() != null) {
            ltiProviders = course.getLtiProviders().getLtiProvider();
            for (LtiProvider ltiProvider : ltiProviders) {
                if (ltiProvider.getIdentifier() == null || ltiProvider.getIdentifier().trim().isEmpty() || ltiProvider.getKey() == null
                        || ltiProvider.getIdentifier().trim().isEmpty() || ltiProvider.getSharedSecret() == null
                        || ltiProvider.getSharedSecret().trim().isEmpty()) {

                    courseValidationResults.addImportantIssue(
                            new DetailedException("Found an LTI Provider that doesn't have a valid identifier, key, or shared secret",
                                    "Please ensure that the identifier, key, and shared secret are included for all LTI Providers", null));
                } else if (LTI_EXPORT_PROVIDER_MESSAGE.equals(ltiProvider.getKey())
                        || LTI_EXPORT_PROVIDER_MESSAGE.equals(ltiProvider.getSharedSecret())) {
                    courseValidationResults.addImportantIssue(new DetailedException(
                            "Found an LTI Provider that doesn't have a valid identifier, key, or shared secret",
                            "The LTI Provider '" + ltiProvider.getIdentifier()
                                    + "' had it's values removed for security purposes when the course was exported. Please update them to the correct values.",
                            null));
                }
            }
        }

        //
        // Check transitions
        //
        List<Serializable> transitions = course.getTransitions().getTransitionType();

        if (transitions.isEmpty()) {
            courseValidationResults.setCriticalIssue(new DetailedException("Course contains no course objects.", "The course requires at least 1 course object.", null));
        } else {
            // used to check for duplicate transition names which is not allowed
            Set<String> transitionNames = new HashSet<>();

            // used to check for duplicate authored branch ids which is not allowed
            authoredBranchIds = new HashSet<>();

            metadataFileCache.setRuleMetadataFiles(null);
            metadataFileCache.setExampleMetadataFiles(null);
            metadataFileCache.setPracticeMetadataFiles(null);
            metadataFileCache.setRemediationOnlyMetadataFiles(null);

            int numDisabledTransitions = 0;
            int size = getNumOfCourseObjects(transitions);  // the number of course objects in the entire course, including authored branches
            int courseObjectsProcessed = 0; // keep track of the number of course objects processed, including authored branches
            double perObjectProgress = 100.0 / size;

            for(int index = 0; index < transitions.size(); index++){

                courseObjectsProcessed++;
                Serializable transitionObj = transitions.get(index);
                if(progressIndicator != null){
                    progressIndicator.setPercentComplete((int)(perObjectProgress * courseObjectsProcessed)); //need to calculate percent each time in order to not
                                                                                                  //loose decimal places along the way as you would with incrementing by an int
                }

                try{

                    CourseObjectValidationResults courseObjValidationResults = null;

                    // skip validation if transition is disabled
                    if (isTransitionDisabled(transitionObj)) {
                        if (logger.isInfoEnabled()) {
                            logger.info("skipping checkCourse Validation for disabled transitionObj: " + transitionObj);
                        }
                        courseObjValidationResults = new CourseObjectValidationResults("DISABLED - " + getTransitionName(transitionObj));
                        numDisabledTransitions++;
                    } else if (isTransitionTrainingApplication(transitionObj)) {
                        // found training application transition

                        generated.course.TrainingApplication trainingAppTransition = (generated.course.TrainingApplication) transitionObj;
                        String transitionName = trainingAppTransition.getTransitionName();
                        courseObjValidationResults = new CourseObjectValidationResults(transitionName);

                        GIFTValidationResults validationResults = checkTrainingAppElement(courseDirectory, trainingAppTransition, courseConceptNames, connectionStatus, null);

                        try {
                            checkTransitionName(transitionName, transitionNames);
                        } catch (DetailedException e) {
                            validationResults.addImportantIssue(e);
                        }

                        courseObjValidationResults.setValidationResults(validationResults);

                    } else if (isTransitionLessonMaterial(transitionObj)) {
                        // found lesson material transition

                        generated.course.LessonMaterial lessonMaterialTransition = (generated.course.LessonMaterial) transitionObj;
                        String transitionName = lessonMaterialTransition.getTransitionName();
                        courseObjValidationResults = new CourseObjectValidationResults(transitionName);

                        GIFTValidationResults validationResults = checkLessonMaterial(lessonMaterialTransition, course, courseDirectory, connectionStatus, failOnFirstSchemaError);

                        try {
                            checkTransitionName(transitionName, transitionNames);
                        } catch (DetailedException e) {
                            validationResults.addImportantIssue(e);
                        }

                        courseObjValidationResults.setValidationResults(validationResults);

                    } else if (isTransitionGuidance(transitionObj)) {
                        // found guidance transition

                        generated.course.Guidance guidanceTransition = (generated.course.Guidance) transitionObj;
                        String transitionName = guidanceTransition.getTransitionName();
                        courseObjValidationResults = new CourseObjectValidationResults(transitionName);

                        GIFTValidationResults validationResults = checkGuidance(guidanceTransition, courseDirectory, connectionStatus);

                        try {
                            checkTransitionName(transitionName, transitionNames);
                        } catch (DetailedException e) {
                            validationResults.addImportantIssue(e);
                        }

                        courseObjValidationResults.setValidationResults(validationResults);

                    } else if (isTransitionMerrillsBranchPoint(transitionObj)) {

                        generated.course.MerrillsBranchPoint merrill = (generated.course.MerrillsBranchPoint) transitionObj;
                        String transitionName = merrill.getTransitionName();
                        courseObjValidationResults = new CourseObjectValidationResults(transitionName);

                        GIFTValidationResults validationResults = checkMerrillsBranchPoint(courseDirectory, courseConceptNames, courseConceptsTaught, transitionNames, merrill);

                        try {
                            checkTransitionName(transitionName, transitionNames);
                        } catch (DetailedException e) {
                            validationResults.addImportantIssue(e);
                        }

                        courseObjValidationResults.setValidationResults(validationResults);

                    } else if (isTransitionPresentSurvey(transitionObj)) {
                        // found survey transition

                        generated.course.PresentSurvey surveyTransition = (generated.course.PresentSurvey) transitionObj;
                        String transitionName = surveyTransition.getTransitionName();
                        courseObjValidationResults = new CourseObjectValidationResults(transitionName);

                        GIFTValidationResults validationResults = checkPresentSurvey((generated.course.PresentSurvey) transitionObj, courseSurveyContext, courseDirectory);

                        try {
                            checkTransitionName(transitionName, transitionNames);
                        } catch (DetailedException e) {
                            validationResults.addImportantIssue(e);
                        }

                        courseObjValidationResults.setValidationResults(validationResults);

                    } else if (isTransitionAAR(transitionObj)) {
                        // found AAR transition

                        generated.course.AAR aarTransition = (generated.course.AAR) transitionObj;
                        String transitionName = aarTransition.getTransitionName();
                        courseObjValidationResults = new CourseObjectValidationResults(transitionName);

                        GIFTValidationResults validationResults = checkAAR(aarTransition, transitionNames);

                        try {
                            checkTransitionName(transitionName, transitionNames);
                        } catch (DetailedException e) {
                            validationResults.addImportantIssue(e);
                        }

                        courseObjValidationResults.setValidationResults(validationResults);

                    } else if (isTransitionAuthoredBranch(transitionObj)) {

                        generated.course.AuthoredBranch authoredBranch = (generated.course.AuthoredBranch) transitionObj;
                        String transitionName = authoredBranch.getTransitionName();
                        courseObjValidationResults = new CourseObjectValidationResults(transitionName);

                        LinkedList<String> authoredBranchAncestortNames = new LinkedList<>();
                        authoredBranchAncestortNames.add(transitionName);
                        GIFTValidationResults validationResults = checkAuthoredBranch(authoredBranch, courseValidationResults, progressIndicator, 
                                perObjectProgress, courseDirectory, connectionStatus,
                                failOnFirstSchemaError, courseConceptNames, courseConceptsTaught, courseSurveyContext, transitionNames, authoredBranchAncestortNames);
                        courseObjectsProcessed += validationResults.getNumOfCourseObjectsChecked();
                        
                        try {
                            checkTransitionName(transitionName, transitionNames);
                        } catch (DetailedException e) {
                            validationResults.addImportantIssue(e);
                        }

                        courseObjValidationResults.setValidationResults(validationResults);

                    }

                    // every course object type should be validated at some level,
                    // this is the catch all others that fell through the cracks
                    if (courseObjValidationResults == null) {
                        courseObjValidationResults = new CourseObjectValidationResults("UNHANDLED TYPE - " + transitionObj);
                    }

                    courseValidationResults.addCourseObjectResults(courseObjValidationResults);
                }catch(Throwable t){

                    String courseObjectName = getCourseObjectName(transitionObj);
                    CourseObjectValidationResults objectValidationResults = new CourseObjectValidationResults(courseObjectName);
                    GIFTValidationResults validationResults = new GIFTValidationResults();
                    validationResults.setCriticalIssue(
                        new DetailedException("The course object named '"+courseObjectName+"' failed to validate because of an error.",
                        "While validating this course object named '"+courseObjectName+"' the following error was thrown on the server: "+t.toString(), t));
                    objectValidationResults.setValidationResults(validationResults);
                    courseValidationResults.addCourseObjectResults(objectValidationResults);
                }
            } // end for

            if (transitions.size() == numDisabledTransitions) {
                courseValidationResults.setCriticalIssue(new DetailedException("Course contains no enabled course objects.", "The course requires at least 1 enabled course object.", null));
            }
        }

        //
        // Check Ped actions - future work...
        //
        // new CourseActionKnowledge(course.getActions());

        return courseValidationResults;
    }
    
    /**
     * Returns the number of course objects found in the course.  This includes finding all
     * the course objects under each authored branch course object, which can have its
     * own paths.
     * 
     * @param courseObjects the course objects to provide a count for, recursively looking
     * at authored branch course objects in this list.
     * @return the total number of course objects represented by the provided list.
     */
    private int getNumOfCourseObjects(List<Serializable> courseObjects){
        
        int cnt = 0;
        for(Serializable courseObject: courseObjects){
            
            // course objects to ignore because they don't actually have any logic
            if(courseObject instanceof generated.course.AuthoredBranch.Paths.Path.Courseobjects.End){
                continue;
            }
            
            cnt++;
            
            if(courseObject instanceof generated.course.AuthoredBranch){
                generated.course.AuthoredBranch authoredBranch = (generated.course.AuthoredBranch) courseObject;
                for(generated.course.AuthoredBranch.Paths.Path path : authoredBranch.getPaths().getPath()){
                    
                    cnt += getNumOfCourseObjects(path.getCourseobjects().getAAROrAuthoredBranchOrEnd());
                }
            }
        }
        
        return cnt;
    }

    /**
     * Checks the Merrills Branch Point course object for GIFT validation errors
     * 
     * @param courseDirectory the directory where the course file lives
     * @param conceptNames the names of the concepts taught within the course
     * @param courseConceptsTaught the list of course concepts referenced by Merrill's branch points thus far
     * @param transitionNames contains the course transition names already checked
     * @param merrill the merrills branch course object
     * @return validation results
     */
    private GIFTValidationResults checkMerrillsBranchPoint(AbstractFolderProxy courseDirectory, Set<String> conceptNames,
            Set<String> courseConceptsTaught, Set<String> transitionNames, generated.course.MerrillsBranchPoint merrill) {
        
        GIFTValidationResults validationResults = new GIFTValidationResults();
        
        String transitionName = merrill.getTransitionName();

        // are there any concepts selected?
        if (merrill.getConcepts() == null || merrill.getConcepts().getConcept().isEmpty()) {
        validationResults.addImportantIssue(
                new DetailedException("No concepts have been selected.",
                        "Please select one or more 'concepts to cover' in the Adaptive Courseflow course object '"+merrill.getTransitionName()+"'.",
                        null));
        }


        // are there duplicate concepts?
        String firstDuplicateConcept = checkDuplicateConcepts(merrill.getConcepts().getConcept());
        if (firstDuplicateConcept != null) {
        validationResults.addImportantIssue(
                new DetailedException("Found a duplicate course concept.",
                    "The concept named '" + firstDuplicateConcept + "' is in the course concepts list more than once of the Adaptive courseflow course object ('" + transitionName + "').",
                    null));
        }

        try{
            gatherMetadataFiles(courseDirectory, metadataFileCache);
        }catch(IOException e){
            validationResults.addImportantIssue(
                    new DetailedException("Failed to find the metadata files in this course folder.",
                        "An error occurred while finding metadata files for the Adaptive courseflow course object named '" + transitionName + "' : " + e.getMessage(), e));
        }

        //
        // check that the concepts for the Merrill's transitions exist in the course concepts
        //

        for (String conceptName : merrill.getConcepts().getConcept()) {

            if (!CollectionUtils.containsIgnoreCase(conceptNames, conceptName)) {
                //found a concept referenced by a Merrill quadrant that isn't in the list of course concepts
                validationResults.addImportantIssue(
                        new DetailedException("Found missing course concept.",
                        "The concept named '"+conceptName+"' referenced by an Adaptive courseflow course object ('"+transitionName+"') is not in the list of "+conceptNames.size()+" course concepts.",
                        null));
            }

            courseConceptsTaught.add(conceptName);
        }

        //
        // check the question counts for the Recall quadrant
        //
        List<DetailedException> recallValidationIssues = checkRecallQuadrant(merrill);
        validationResults.addImportantIssues(recallValidationIssues);

        //
        // Quadrant/Phase check (minus Recall which was just done above)
        // 1. Metadata file check
        // 2. any transitions/objects between main phases (e.g. guidance after example phase)
        //

        for(Serializable transition : merrill.getQuadrants().getContent()){

            if (transition instanceof generated.course.Rule) {

                // check Rule metadata files - optional as long as there is at least one example metadata
                if (metadataFileCache.getRuleMetadataFiles().isEmpty() && metadataFileCache.getExampleMetadataFiles().isEmpty()){
                        validationResults.addImportantIssue(
                            new DetailedException("Failed to find any content tagged for the 'Rule' or 'Example' phase of the Adaptive courseflow course object.",
                            "Unable to find any successfully validated metadata files under the workspace directory '"+courseDirectory.getName()+"' for the Rule or Example phase of the Adaptive courseflow course object named '"+transitionName+
                            "'.\n\nPlease add content tagged with the concept(s) for the Rule or Example phase of that course object by using the 'Add Content' button located under the Rule or Example section of the authoring page for that particular Adaptive courseflow course object.", null));

                } else if (!MetadataSchemaHandler.metadataFileConceptCheck(metadataFileCache.getRuleMetadataFiles(), merrill.getConcepts().getConcept())) {
                    //there are no metadata tagged with the concepts taught in this adaptive courseflow

                    if(metadataFileCache.getExampleMetadataFiles().isEmpty()){
                        validationResults.addImportantIssue(
                            new DetailedException("Failed to find any content tagged with the appropriate concepts for a Adaptive courseflow course object.",
                            "Unable to find a single, successfully validated metadata file under the workspace directory '"+courseDirectory.getName()+"' for the Rule phase of Adaptive courseflow course object named '"+transitionName+"' that, at minimum, references the following branch point concepts:\n"+merrill.getConcepts().getConcept()+
                            ".\n\nPlease add content tagged with the concept(s) for the Rule phase of that course object by using the 'Add Content' button located under the Rule section of the authoring page for that particular Adaptive courseflow course object."+
                            ".\n\nSearch Summary:Looked at a total of "+metadataFileCache.getRuleMetadataFiles().size()+" metadata files tagged with the 'Rule' phase.", null));
                    }
                }

            } else if (transition instanceof generated.course.Example) {

                // check Example metadata files
                if (metadataFileCache.getExampleMetadataFiles().isEmpty()) {
                    // ERROR
                    validationResults.addImportantIssue(
                            new DetailedException("Failed to find any content tagged for the 'Example' phase of the Adaptive courseflow course object.",
                            "Unable to find any successfully validated metadata files under the workspace directory '"+courseDirectory.getName()+"' for the Example phase of the Adaptive courseflow course object named '"+transitionName+
                            "'.\n\nPlease add content tagged with the concept(s) for the Example phase of that course object by using the 'Add Content' button located under the Example section of the authoring page for that particular Adaptive courseflow course object.", null));
                } else if (!MetadataSchemaHandler.metadataFileConceptCheck(metadataFileCache.getExampleMetadataFiles(), merrill.getConcepts().getConcept())) {
                    // ERROR
                    validationResults.addImportantIssue(
                            new DetailedException("Failed to find any content tagged with the appropriate concepts for a Adaptive courseflow course object.",
                            "Unable to find a single, successfully validated metadata file under the workspace directory '"+courseDirectory.getName()+"' for the Example phase of Adaptive courseflow course object named '"+transitionName+"' that, at minimum, references the following branch point concepts:\n"+merrill.getConcepts().getConcept()+
                            ".\n\nPlease add content tagged with the concept(s) for the Example phase of that course object by using the 'Add Content' button located under the Example section of the authoring page for that particular Adaptive courseflow course object."+
                            ".\n\nSearch Summary:Looked at a total of "+metadataFileCache.getExampleMetadataFiles().size()+" metadata files tagged with the 'Example' phase.", null));
                }

            }else if(transition instanceof generated.course.Remediation){

                generated.course.Remediation remediation = (generated.course.Remediation)transition;

                if(remediation.getExcludeRuleExampleContent() != null && remediation.getExcludeRuleExampleContent() == BooleanEnum.TRUE){
                    //check to make sure there is at least 1 piece of content tagged with remediation only and not rule/example phase

                    //check Remediation metadata files
                    if(metadataFileCache.getRemediationOnlyMetadataFiles().isEmpty()){
                        //ERROR
                        validationResults.addImportantIssue(
                                new DetailedException("Failed to find any content tagged for remediation only in an Adaptive courseflow course object.",
                                "Unable to find any successfully validated metadata files under the workspace directory '"+courseDirectory.getName()+"' for the Remediation phase only of the Adaptive courseflow course object named '"+transitionName+
                                "'.\n\nPlease add content tagged with the concept(s) in the Remediation phase of that course object by using the 'Add Content' button located under the Remediation section of the authoring page for that particular Adaptive courseflow course object."+
                                        " You can also uncheck the 'Exclude Rule and Example content' checkbox to use the already authored content for remediation.", null));
                    }else if(!MetadataSchemaHandler.metadataFileConceptCheck(metadataFileCache.getRemediationOnlyMetadataFiles(), merrill.getConcepts().getConcept())){
                        //ERROR
                        validationResults.addImportantIssue(
                                new DetailedException("Failed to find any content tagged with the appropriate concepts for remediation only in an Adaptive courseflow course object.",
                                "Unable to find a single, successfully validated metadata file under the workspace directory '"+courseDirectory.getName()+"' for the Remediation phase only of the Adaptive courseflow course object named '"+transitionName+
                                "' that, at minimum, references the following branch point concepts:\n"+merrill.getConcepts().getConcept()+".\n\nPlease add content tagged with the concept(s) in the Remediation phase of that course object by using the 'Add Content' button located under the Remediation section of the authoring page for that particular Adaptive courseflow course object."+
                                        " You can also uncheck the 'Exclude Rule and Example content' checkbox to use the already authored content for remediation."+
                                        "\n\nSearch Summary:Looked at a total of "+metadataFileCache.getRemediationOnlyMetadataFiles().size()+" metadata files tagged with the remediation phase only.", null));
                    }
                }

            } else if (transition instanceof generated.course.Practice) {

                // check the Practice metadata files:
                // 1) there are practice metadata files
                // 2) branch point concepts are part of this or a previous Adaptive courseflow course element
                //       - the concept needs to be taught in a MBP before being practiced
                // 3) there is at least 1 practice metadata file for the concepts in this branch point
                // 4) [Done prior when finding the metadata files in the course folder] Content reference (simpleRef or trainingApp.xml) is valid
                // 5) there is at least 1 practice metadata referenced DKF that:
                //     5.1. has all the practice course concepts as concepts being assessed
                //     5.2. those concepts have a descendant condition with at least 1 scoring rule - if the concept isn't scored, it can't be remediated

                generated.course.Practice practice = (generated.course.Practice) transition;

                // are there duplicate concepts?
                firstDuplicateConcept = checkDuplicateConcepts(practice.getPracticeConcepts().getCourseConcept());
                if (firstDuplicateConcept != null) {
                    validationResults.addImportantIssue(
                        new DetailedException("Found a duplicate course concept.",
                        "The concept named '"+firstDuplicateConcept+"' is in the course concepts list more than once of the Practice for the Adaptive courseflow course object ('"+transitionName+"').",
                                    null));
                }

                // 1) there are practice metadata files in the course folder 
                if (metadataFileCache.getPracticeMetadataFiles().isEmpty()) {
                    // ERROR
                    validationResults.addImportantIssue(
                        new DetailedException("Failed to find any content tagged for the 'Practice' phase of the Adaptive courseflow course object part of the course.",
                        "Unable to find any successfully validated metadata files under the workspace directory '"+courseDirectory.getName()+"' for the Practice phase of the Adaptive courseflow course object named '"+transitionName+
                        "'.\n\nPlease add content for the Practice phase of that course element by using the 'Add Content' button located under the Practice section of the authoring page for that particular Adaptive courseflow course element.", null));

                    // 2)  branch point concepts are part of this or a previous Adaptive courseflow course element
                } else if (!courseConceptsTaught.containsAll(practice.getPracticeConcepts().getCourseConcept())) {
                    // ERROR

                    //determine the set of concepts not covered to this point in the course
                    List<String> uncoveredConcepts = new ArrayList<>();
                    for (String practiceConcept : practice.getPracticeConcepts().getCourseConcept()) {

                        if (!courseConceptsTaught.contains(practiceConcept)) {
                            uncoveredConcepts.add(practiceConcept);
                        }
                    }

                    validationResults.addImportantIssue(
                        new DetailedException("Found at least one concept where the learner can Practice their Skill before acquiring the Knowledge on that concept.",
                                "The course flow of a Adaptive Courseflow course object first has the learner prove their knowledge on concepts before allowing the them to practice those concepts."+
                                "  This is acheived by presenting Rule and Example content followed by a knowledge assessment test on those conepts.  During validation it was discovered that the following concepts referenced by the Practice phase of the Adaptive courseflow course element named '"+merrill.getTransitionName()+
                                "'are not associated with that or any previous Adaptive courseflow course object:\n"+uncoveredConcepts+".\nThis is an indication that the concepts haven't been taught up to this point in the course.\n\n"
                                        + "Note: when using an Authored Branch course object, every path (that doesn't end the course) must teach the concepts as well to ensure the concepts are covered no matter the path(s) choosen.", null));

                    // 3) there is at least 1 practice metadata file for the concepts in this branch point practice phase 
                } else if (!MetadataSchemaHandler.metadataFileConceptCheck(metadataFileCache.getPracticeMetadataFiles(), practice.getPracticeConcepts().getCourseConcept())) {
                    // ERROR
                    validationResults.addImportantIssue(
                        new DetailedException("Failed to find any content tagged with the appropriate concepts for a Adaptive courseflow course object part of the course.",
                        "Unable to find a single, successfully validated, metadata file under the workspace directory '"+courseDirectory.getName()+"' for the Practice phase of Adaptive courseflow course object named '"+transitionName+"' that, at minimum, references the following branch point concepts:\n"+merrill.getConcepts().getConcept()+
                        ".\n\nPlease add content tagged with the concept(s) for the Practice phase of that course element by using the 'Add Content' button located under the Practice section of the authoring page for that particular Adaptive courseflow course element."+
                        ".\n\nSearch Summary:Looked at a total of "+metadataFileCache.getPracticeMetadataFiles().size()+" metadata files tagged with the 'Practice' phase.", null));
                }
                
                // 5 and 6) checking the DKFs for scored course concepts for this practice phase 
                boolean foundOne = false;
                for(generated.metadata.Metadata metadata : metadataFileCache.getPracticeMetadataFiles().values()){
                    
                    boolean shouldCheck = true;
                    if(metadata.getConcepts().getConcept().size() != practice.getPracticeConcepts().getCourseConcept().size()){
                        // currently the practice metadata must cover exactly the same concepts being assessed in the practice phase
                        continue;
                    }
                    
                    for(generated.metadata.Concept concept : metadata.getConcepts().getConcept()){
                        
                        if(!practice.getPracticeConcepts().getCourseConcept().contains(concept.getName().toLowerCase())){
                            //found extraneous concept
                            shouldCheck = false;
                            break;
                        }
                    }
                    
                    if(shouldCheck){
                        try {
                            GIFTValidationResults metadataValidationResults = MetadataSchemaHandler.checkMetadata(metadata, courseDirectory, null, practice.getPracticeConcepts().getCourseConcept(), null, true);
                            if(metadataValidationResults.hasIssues()){
                                // this metadata doesn't have the necessary scored course concepts in the DKF
                                continue;
                            }
                            
                            // found at least 1 DKF with the necessary scored course concepts
                            foundOne = true;
                            break;
                        } catch (@SuppressWarnings("unused") Exception e) {
                            // can't use this metadata
                            continue;
                        }
                    }
                }// end for
                
                if(!foundOne){
                    validationResults.addImportantIssue(
                            new DetailedException("Failed to find at least one Practice application with a real time assessment that has the necessary course concepts.",
                                "In order to properly remediate on the practice phase there must be at least one training application real time assessment with the following concepts:\n"+practice.getPracticeConcepts().getCourseConcept()+".\n"
                                        + "Please make sure these concept being assessed in the practice phase are in a real time assessment and have overall assessment rules for at least one descendant condition.", null));
                }

            }else if(transition instanceof generated.course.Transitions){
                // check any course objects that were added between certain phases of the adaptive courseflow (e.g. message after example phase, before recall phase)
                               
                generated.course.Transitions betweenPhaseObjects = (generated.course.Transitions)transition;
                for(int index = 0; index < betweenPhaseObjects.getTransitionType().size(); index++){
                    
                    Serializable transitionObj = betweenPhaseObjects.getTransitionType().get(index);
                    
                    // skip validation if transition is disabled
                    if (isTransitionDisabled(transitionObj)) {
                        if (logger.isInfoEnabled()) {
                            logger.info("skipping validation for disabled object that was found between phases in the '"+merrill.getTransitionName()+"' Adaptive courseflow course object: " + transitionObj);
                        }
                        continue;
                    } else if (isTransitionLessonMaterial(transitionObj)) {
                        // found lesson material transition

                        generated.course.LessonMaterial lessonMaterialTransition = (generated.course.LessonMaterial) transitionObj;

                        GIFTValidationResults lmValidationResults = checkLessonMaterial(lessonMaterialTransition, course, courseDirectory, InternetConnectionStatusEnum.UNKNOWN, false);
                        validationResults.addGIFTValidationResults(lmValidationResults);

                    } else if (isTransitionGuidance(transitionObj)) {
                        // found guidance transition

                        generated.course.Guidance guidanceTransition = (generated.course.Guidance) transitionObj;

                        GIFTValidationResults guidanceValidationResults = checkGuidance(guidanceTransition, courseDirectory, InternetConnectionStatusEnum.UNKNOWN);
                        Throwable firstError = guidanceValidationResults.getFirstError();
                        if(firstError != null){
                            // add additional help to the error                            
                            validationResults.addImportantIssue(new DetailedException("Found an invalid/incomplete Message object between Adaptive courseflow phases in '"+merrill.getTransitionName()+"'.", 
                                    "Please fix the Message object by looking between each phase in this adaptive courseflow course object for 'Show message on completion'.  The most common issues are selecting to present a message to the learner but not "+
                                            "specifying the message content or providing an incorrect Message value (e.g. unreachable website URL)", firstError));

                        }
                    }else{
                        // nothing else makes sense to be located here at this point in time
                        validationResults.addImportantIssue(
                                new DetailedException("Found unhandled course object between Adaptive courseflow phases in '"+merrill.getTransitionName()+"'.",
                                        "The between phase element at index "+(index+1)+" (1-based) of "+transitionObj+" is not a type that is currently validated. Perhaps logic needs to be added to handled this type now.", null));
                    }
                }//end for

            }

        }
        
        return validationResults;
    }
    
    /**
     * Populates the metadata file cache provided with the metadata files found in the course folder.
     * 
     * @param courseDirectory where to search for metadata files in.
     * @param metadataFileCache where to group the metadata files found by CDT phases (e.g. Rule, Remediation).  Can't be null.
     * @throws IOException if there was a problem searching the course folder or parsing a metadata file.
     */
    private static void gatherMetadataFiles(AbstractFolderProxy courseDirectory, MetadataFileCache metadataFileCache) throws IOException{
        
        if(metadataFileCache == null){
            throw new IllegalArgumentException("The metadata file cache can't be null.");
        }
        
        // only gather the metadata files for each quadrant 1x
        if(metadataFileCache.getRuleMetadataFiles() == null || metadataFileCache.getExampleMetadataFiles() == null || 
                metadataFileCache.getPracticeMetadataFiles() == null || metadataFileCache.getRemediationOnlyMetadataFiles() == null){

            try{
                // Build a single request for all phases to limit calls to search, parse and validate the same metadata files
                Map<MerrillQuadrantEnum, MetadataSearchCriteria> quadrantSearchCriteria = new HashMap<>();

                generated.metadata.PresentAt presentAtRule = new generated.metadata.PresentAt();
                presentAtRule.setMerrillQuadrant(MerrillQuadrantEnum.RULE.getName());
                MetadataSearchCriteria ruleCriteria = new MetadataSearchCriteria(presentAtRule);
                quadrantSearchCriteria.put(MerrillQuadrantEnum.RULE, ruleCriteria);

                generated.metadata.PresentAt presentAtExample = new generated.metadata.PresentAt();
                presentAtExample.setMerrillQuadrant(MerrillQuadrantEnum.EXAMPLE.getName());
                MetadataSearchCriteria exampleCriteria = new MetadataSearchCriteria(presentAtExample);
                quadrantSearchCriteria.put(MerrillQuadrantEnum.EXAMPLE, exampleCriteria);

                generated.metadata.PresentAt presentAtRemediation = new generated.metadata.PresentAt();
                presentAtRemediation.setRemediationOnly(generated.metadata.BooleanEnum.TRUE);
                MetadataSearchCriteria remediationCriteria = new MetadataSearchCriteria(presentAtRemediation);
                remediationCriteria.setExcludeRuleExampleContent(true);  //rule and example content have their own collections (above)
                quadrantSearchCriteria.put(MerrillQuadrantEnum.REMEDIATION_AFTER_RECALL, remediationCriteria);

                generated.metadata.PresentAt presentAtPractice = new generated.metadata.PresentAt();
                presentAtPractice.setMerrillQuadrant(MerrillQuadrantEnum.PRACTICE.getName());
                MetadataSearchCriteria practiceCriteria = new MetadataSearchCriteria(presentAtPractice);
                quadrantSearchCriteria.put(MerrillQuadrantEnum.PRACTICE, practiceCriteria);

                Map<MerrillQuadrantEnum, MetadataFileSearchResult> quadrantToQualifiedMetadatas =
                        MetadataFileFinder.findFiles(courseDirectory, quadrantSearchCriteria);

                for(MerrillQuadrantEnum quadrant : quadrantToQualifiedMetadatas.keySet()){

                    if(quadrant == MerrillQuadrantEnum.RULE){

                        metadataFileCache.setRuleMetadataFiles(quadrantToQualifiedMetadatas.get(quadrant) != null ?
                                quadrantToQualifiedMetadatas.get(quadrant).getMetadataFilesMap() : new HashMap<>());

                    }else if(quadrant == MerrillQuadrantEnum.EXAMPLE){

                        metadataFileCache.setExampleMetadataFiles(quadrantToQualifiedMetadatas.get(quadrant) != null ?
                                quadrantToQualifiedMetadatas.get(quadrant).getMetadataFilesMap() : new HashMap<>());

                    }else if(quadrant == MerrillQuadrantEnum.REMEDIATION_AFTER_RECALL){

                        metadataFileCache.setRemediationOnlyMetadataFiles(quadrantToQualifiedMetadatas.get(quadrant) != null ?
                                quadrantToQualifiedMetadatas.get(quadrant).getMetadataFilesMap() : new HashMap<>());

                    }else if(quadrant == MerrillQuadrantEnum.PRACTICE){

                        metadataFileCache.setPracticeMetadataFiles(quadrantToQualifiedMetadatas.get(quadrant) != null ?
                                quadrantToQualifiedMetadatas.get(quadrant).getMetadataFilesMap() : new HashMap<>());
                    }
                }

                if(metadataFileCache.getRuleMetadataFiles() == null){
                    metadataFileCache.setRuleMetadataFiles(new HashMap<>());
                }

                if(metadataFileCache.getExampleMetadataFiles() == null){
                    metadataFileCache.setExampleMetadataFiles(new HashMap<>());
                }

                if(metadataFileCache.getRemediationOnlyMetadataFiles() == null){
                    metadataFileCache.setRemediationOnlyMetadataFiles(new HashMap<>());
                }

                if(metadataFileCache.getPracticeMetadataFiles() == null){
                    metadataFileCache.setPracticeMetadataFiles(new HashMap<>());
                }

            } catch (IOException e) {
                //don't expect it to work the next time, so prevent this if statement from being
                //entered for following adaptive courseflow course objects in the course
                metadataFileCache.setRuleMetadataFiles(new HashMap<>());
                metadataFileCache.setExampleMetadataFiles(new HashMap<>());
                metadataFileCache.setRemediationOnlyMetadataFiles(new HashMap<>());
                metadataFileCache.setPracticeMetadataFiles(new HashMap<>());

                throw e;
            }

        }
    }

    /**
     * Check the collection for duplicate concept names.  The check ignores case.
     *
     * @param concepts collection of concepts to check for duplicates.
     * @return the first duplicate concept name found. Null if no duplicates were found.
     */
    private static String checkDuplicateConcepts(List<String> concepts){

        if(concepts == null || concepts.isEmpty()){
            return null;
        }

        Set<String> uniqueSet = new HashSet<>(concepts.size());
        for(String concept : concepts){

            String conceptLowercase = concept.toLowerCase();
            if(uniqueSet.contains(conceptLowercase)){
                return concept;
            }

            uniqueSet.add(conceptLowercase);
        }

        return null;
    }

    /**
     * Returns a unique set of Survey Context GIFT keys found in the course object contained
     * within this class.  The set will only contain GIFT keys for surveys referenced in survey
     * course objects that are marked as shared (i.e. not authored in this course but selected from
     * pre-existing surveys and the original survey is used rather than making a copy for this course).
     *
     * @return the unique set of shared survey context gift keys.  If no shared surveys are found, an
     * empty set will be returned.
     */
    public Set<String> getSharedSurveyGiftKeys(){

        Set<String> sharedSurveyGiftKeys = new HashSet<>();
        for(Serializable courseObject : course.getTransitions().getTransitionType()){

            if(courseObject instanceof generated.course.PresentSurvey){

                generated.course.PresentSurvey presentSurvey = (generated.course.PresentSurvey)courseObject;

                //must be a GIFT key survey type, not a conversation or other type
                if(presentSurvey.getSurveyChoice() instanceof String &&
                        presentSurvey.getSharedSurvey() != null && presentSurvey.getSharedSurvey() == BooleanEnum.TRUE){
                    sharedSurveyGiftKeys.add((String) presentSurvey.getSurveyChoice());
                }
            }

        }

        return sharedSurveyGiftKeys;
    }

    /**
     * Check the AAR course object for GIFT validation errors
     *
     * @param aarTransition contains the AAR course object configuration parameters.  Can't be null.
     * @param priorTransitionNames the course object names prior to this AAR course object in the course flow.  Can be empty but not null.
     * @return validation results
     */
    public static GIFTValidationResults checkAAR(generated.course.AAR aarTransition, Set<String> priorTransitionNames){

        GIFTValidationResults validationResults = new GIFTValidationResults();

        generated.course.AAR.CourseObjectsToReview courseObjectsToReview = aarTransition.getCourseObjectsToReview();
        if(courseObjectsToReview != null){

            List<String> courseObjectList = courseObjectsToReview.getTransitionName();
            for(String courseObjectName : courseObjectList){

                if(!priorTransitionNames.contains(courseObjectName)){
                    validationResults.addImportantIssue(
                            new DetailedException("The structure review course object named '"+aarTransition.getTransitionName()+"' is not valid.",
                            "The course object named '"+courseObjectName+"' to be included in this structured review course object is not a course object that happens before this part of the course.  In order for a course object to be reviewed in a structured review course object it must exist earlier in the course flow.", null));
                }
            }
        }

        return validationResults;
    }

    /**
     * Return the collection of course object names in the provided course instance.  Course object names
     * are suppose to be not null, not empty and unique within a course.
     *
     * @param course the course to get the course object names from.
     * @return the unique collection of course object names.  Null and empty names will not be included.  Can be empty but not null.
     */
    public static Set<String> getCourseObjectNames(generated.course.Course course){
        
        if(course != null){
            return getCourseObjectNames(course.getTransitions().getTransitionType());
        }else{
            return new HashSet<>();
        }
    }
    
    /**
     * Return the collection of course object names in the provided course instance.  Course object names
     * are suppose to be not null, not empty and unique within a course.
     *
     * @param transitions the course objects to get the course object names from.
     * @return the unique collection of course object names.  Null and empty names will not be included.  Can be empty but not null.
     */
    public static Set<String> getCourseObjectNames(List<Serializable> transitions){
     
        Set<String> courseObjectNames = new HashSet<>();
        if(transitions != null){

            for(Serializable courseObject : transitions){

                String candidate = getCourseObjectName(courseObject);

                if(candidate != null && !candidate.isEmpty()){
                    courseObjectNames.add(candidate);
                }
                
                // gather course object names from each path
                if(courseObject instanceof generated.course.AuthoredBranch){
                    
                    generated.course.AuthoredBranch branch = (generated.course.AuthoredBranch)courseObject;
                    if(branch.getPaths() == null){
                        continue;
                    }
                    
                    for(Path path : branch.getPaths().getPath()){
                        courseObjectNames.addAll(getCourseObjectNames(path.getCourseobjects().getAAROrAuthoredBranchOrEnd()));                        
                    }
                }
            }
        }

        return courseObjectNames;
    }

    /**
     * Return the name of the course object which is taken from the transitionName XML authored
     * element.
     *
     * @param courseObject the authored course object to get the name from
     * @return the unique name of the course object in a course instance.  Will be null only if the
     * course object provided is not recognized.
     */
    public static String getCourseObjectName(Serializable courseObject){

        String name = null;

        if(courseObject instanceof generated.course.TrainingApplication){
            name = ((generated.course.TrainingApplication)courseObject).getTransitionName();

        }else if(courseObject instanceof generated.course.LessonMaterial){
            name = ((generated.course.LessonMaterial)courseObject).getTransitionName();

        }else if(courseObject instanceof generated.course.Guidance){
            name = ((generated.course.Guidance)courseObject).getTransitionName();

        }else if(courseObject instanceof generated.course.MerrillsBranchPoint){
            name = ((generated.course.MerrillsBranchPoint)courseObject).getTransitionName();

        }else if(courseObject instanceof generated.course.PresentSurvey){
            name = ((generated.course.PresentSurvey)courseObject).getTransitionName();

        }else if(courseObject instanceof generated.course.AAR){
            name = ((generated.course.AAR)courseObject).getTransitionName();

        }else if(courseObject instanceof generated.course.AuthoredBranch){
            name = ((generated.course.AuthoredBranch)courseObject).getTransitionName();
        }

        return name;
    }

    /**
     * Checks the provided transition name against the transition names list.
     *
     * @param transitionName the name of a transition to check against course transitions already checked.  Can be null.
     * @param transitionNames contains the course transition names already checked
     * @throws DetailedException if the transition name being checked is a duplicate of another transition or
     * the transition name is empty.
     */
    private static void checkTransitionName(String transitionName, Set<String> transitionNames) throws DetailedException{

        if(transitionName == null || transitionName.isEmpty() || transitionName.trim().isEmpty()){
            throw new DetailedException("Found empty course object name.",
                    "Course objects must be given non-empty and unique name in order to identify each on in case of validation issues.  Please check your course objects for non-empty, unique names.", null);
        }else if(transitionNames.contains(transitionName.trim())){
            throw new DetailedException("Found duplicate course object name of '"+transitionName.trim()+"'.",
                    "Course object names must be unique in order to identify each one in case of validation issues.  Please check your course objects for unique names.", null);
        }

        transitionNames.add(transitionName.trim());
    }

    /**
     * Find the Recall quadrant in the branch point course element and check that every concept
     * taught in the branch point is included in the requested concept knowledge survey.
     *
     * @param merrill the branch point course element to check
     * @return collection of validation errors found.  Can be empty but not null.
     */
    private static List<DetailedException> checkRecallQuadrant(generated.course.MerrillsBranchPoint merrill){

        List<DetailedException> issuesFound = new ArrayList<>();

        int recallCnt = 0;
        for(Serializable transition : merrill.getQuadrants().getContent()){

            if(transition instanceof generated.course.Recall){

                recallCnt++;

                Serializable surveyChoice = ((generated.course.Recall)transition).getPresentSurvey().getSurveyChoice();
                if(surveyChoice instanceof generated.course.Recall.PresentSurvey.ConceptSurvey){

                    generated.course.Recall.PresentSurvey.ConceptSurvey conceptSurvey = (generated.course.Recall.PresentSurvey.ConceptSurvey)surveyChoice;

                    //check that there is at least 1 recall survey question asked for each concept
                    int qCnt = 0;
                    for(generated.course.ConceptQuestions concept : conceptSurvey.getConceptQuestions()){

                        qCnt = concept.getQuestionTypes().getEasy().intValue() + concept.getQuestionTypes().getMedium().intValue() +
                                concept.getQuestionTypes().getHard().intValue();

                        if(qCnt == 0){
                            issuesFound.add(
                                    new DetailedException("Incorrect number of Recall questions in '"+merrill.getTransitionName()+"' course transition.",
                                    "The "+concept.getName()+" concept in the Recall portion of the #"+recallCnt+
                                    "Adaptive courseflow in the course has ZERO 'question types' authored.  Please provide a value "+
                                    "of *at least 1* for *at least one* of the question types (easy/medium/hard) in order to test the learner's comprehension of this concept.",
                                    null));
                            continue;
                        }

                        //check that the assessment rules against the total number of questions being asked
                        int aboveCnt = concept.getAssessmentRules().getAboveExpectation().getNumberCorrect().intValue();
                        int atCnt = concept.getAssessmentRules().getAtExpectation().getNumberCorrect().intValue();
                        int belowCnt = concept.getAssessmentRules().getBelowExpectation().getNumberCorrect().intValue();
                        if(aboveCnt > qCnt){
                            issuesFound.add(
                                    new DetailedException("Incorrect number of Recall questions resulting in 'Above Expectation' in '"+merrill.getTransitionName()+"' course transition.",
                                    "The "+concept.getName()+" concept in the Recall portion of the #"+recallCnt+
                                    " Adaptive courseflow in the course specifies that the learner must get "+aboveCnt+" questions correct "+
                                    "in order to receive an 'Above Expectation' result and therefore advance the learner to the next course transition."+
                                    " However the course requests that only "+qCnt+" question be given for this concept.",
                                    null));
                        }else if(atCnt > qCnt){
                            issuesFound.add(
                                    new DetailedException("Incorrect number of Recall questions resulting in 'At Expectation' in '"+merrill.getTransitionName()+"' course transition.",
                                    "The "+concept.getName()+" concept in the Recall portion of the #"+recallCnt+
                                    " Adaptive courseflow in the course specifies that the learner must get "+aboveCnt+" questions correct "+
                                    "in order to receive an 'At Expectation' result and therefore remediation the learner to the Example quadrant."+
                                    " However the course requests that only "+qCnt+" question be given for this concept.",
                                    null));
                        }else if(atCnt >= aboveCnt){
                            issuesFound.add(
                                    new DetailedException("Incorrect number of Recall questions resulting in 'At Expectation' in '"+merrill.getTransitionName()+"' course transition.",
                                    "The "+concept.getName()+" concept in the Recall portion of the #"+recallCnt+
                                    " Adaptive courseflow in the course specifies that the 'At Expectation' question count is equal-to/higher-than "+
                                    "the 'Above Expectation' question count.  This doesn't make logical sense.",
                                    null));
                        }else if(belowCnt > qCnt){
                            issuesFound.add(
                                    new DetailedException("Incorrect number of Recall questions resulting in 'Below Expectation' in '"+merrill.getTransitionName()+"' course transition.",
                                    "The "+concept.getName()+" concept in the Recall portion of the #"+recallCnt+
                                    " Adaptive courseflow in the course specifies that the learner must get "+aboveCnt+" questions correct "+
                                    "in order to receive an 'Below Expectation' result and therefore remediation the learner to the Rule quadrant."+
                                    " However the course requests that only "+qCnt+" question be given for this concept.",
                                    null));
                        //MH: removing to support 0 for both Below and At in a case where only 1 question is asked, therefore
                        //    you are either below or above.
        //            }else if(belowCnt >= atCnt){
        //                throw new Exception("The "+concept.getName()+" concept in the Recall portion of the #"+recallCnt+
        //                        " Merrill's branch in the course specifies that the 'Below Expectation' question count is equal-to/higher-than "+
        //                        "the 'At Expectation' question count.  This doesn't make logical sense.");
                        }
                    }//end for

                }else if(surveyChoice instanceof String){
                    issuesFound.add(
                            new DetailedException("Found unsupported Recall survey type.",
                            "Using a GIFT Key for a Recall survey is not currently supported do to not being able to author assessment rules for that type of survey.",
                            null));
                }else{
                    issuesFound.add(
                            new DetailedException("Found unsupported Recall survey type.",
                            "Found unhandled Recall survey type of "+surveyChoice+" in a Adaptive courseflow point course element.",
                            null));
                }

            }

        }//end for

        return issuesFound;
    }

    /**
     * Check the lesson material course element for GIFT validation errors.
     *
     * @param lessonMaterial the lesson material course element to validate
     * @param course the course that contains the course properties. A null course will not validate
     *            if media properties correctly match with course properties (e.g. concepts, LTI
     *            provider identifiers, etc...)
     * @param courseDirectory the course folder to use for course content references (e.g. DKF path relative to course folder) found in the course file.
     * @param connectionStatus information about if the computer is currently connected to the Internet.  This is used when validating.
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @return validation results
     */
    private static GIFTValidationResults checkLessonMaterial(generated.course.LessonMaterial lessonMaterial, generated.course.Course course, AbstractFolderProxy courseDirectory,
            InternetConnectionStatusEnum connectionStatus, boolean failOnFirstSchemaError) {

        GIFTValidationResults validationResults = new GIFTValidationResults();
        boolean hasFiles = false;
        boolean hasList = false;

        //check/validate lesson material files
        generated.course.LessonMaterialFiles lessonMaterialFiles = lessonMaterial.getLessonMaterialFiles();
        if(lessonMaterialFiles != null){

            if(lessonMaterialFiles.getFile() != null && !lessonMaterialFiles.getFile().isEmpty()) {
                hasFiles = true;
            }

            for(String lessonMaterialFile : lessonMaterialFiles.getFile()){

                try{
                    LessonMaterialFileHandler handler = new LessonMaterialFileHandler(lessonMaterialFile, courseDirectory, connectionStatus);
                    handler.parse(failOnFirstSchemaError);
                    GIFTValidationResults fileValidationResults = handler.validateLessonMaterial(course, courseDirectory);
                    if(fileValidationResults.getImportantIssues() != null){

                        for(DetailedExceptionSerializedWrapper t : fileValidationResults.getImportantIssues()){
                            validationResults.addImportantIssue(t);
                        }
                    }

                    if(fileValidationResults.getWarningIssues() != null){

                        for(DetailedExceptionSerializedWrapper t : fileValidationResults.getWarningIssues()){
                            validationResults.addWarningIssue(t);
                        }
                    }

                }catch(Exception e){
                    validationResults.addImportantIssue(
                            new FileValidationException("Failed to retrieve lesson material file reference.",
                            "An error occurred while retrieving the lesson material file named '"+lessonMaterialFile+"' from '"+courseDirectory.getFileId()+"' : "+e.getMessage(), lessonMaterialFile, e));
                }
            }
        }

        //check/validate lesson material list
        generated.course.LessonMaterialList lessonMaterialList = lessonMaterial.getLessonMaterialList();
        if(lessonMaterialList != null){

            if(lessonMaterialList.getMedia() != null && !lessonMaterialList.getMedia().isEmpty()) {
                hasList = true;

                for(Media m : lessonMaterialList.getMedia()){
                    try{
                        LessonMaterialFileHandler.validateMedia(m, course, courseDirectory, connectionStatus);
                    }catch(ConnectException exception){

                        if(connectionStatus == InternetConnectionStatusEnum.CONNECTED){
                            validationResults.addImportantIssue(
                                    new DetailedException("Failed to validate media.",
                                    "The media named '"+m.getName()+"' failed validation checks because of not being able to connection to a network resource (e.g. website)." +
                                  "  If you are running GIFT without a network/internet connection then please provide an appropriate network connection for this course to be available.",
                                  exception));
                        }else{
                            validationResults.addWarningIssue(
                                    new DetailedException("Failed to validate media.",
                                    "The lesson material named '"+m.getName()+"' failed validation checks because of not being able to connection to a network resource (e.g. website)." +
                                  "  If you are running GIFT without a network/internet connection then please provide an appropriate network connection.",
                                  exception));
                        }
                    }catch(Exception exception){
                        String message = exception.getMessage();
                        if (message.contains("No Slides found")) {
                            validationResults.addImportantIssue(
                                    new DetailedException("Failed to validate media.",
                                    "The lesson material named '"+m.getName()+"' failed validation checks because there was no PowerPoint show selected." +
                                    "\n\nPlease add a PowerPoint show to the lesson material.", exception));
                        }else if (message.contains("no protocol:")) {
                            String[] split = message.split("no protocol: ");
                            String filePath = split[split.length - 1];
                            validationResults.addImportantIssue(
                                    new DetailedException("Failed to validate media.",
                                    "The lesson material named '"+m.getName()+"' failed validation checks because the resource \"" + filePath + "\" could not be found." +
                                     "\n\nCheck to make sure the file exists and can be accessed or replace the existing lesson material.", exception));
                        }else{
                            validationResults.addImportantIssue(
                                    new DetailedException("Failed to validate media.",
                                "The lesson material named '"+lessonMaterial.getTransitionName()+"' failed media validation because '"+m.getName()+"' threw an error: " +exception.getMessage(), exception));
                        }
                    }
                }
            }
        }

        if(!hasFiles && !hasList) {
             validationResults.addImportantIssue(
                     new DetailedException("Failed to validate media collection.",
                 "The media collection named '" + lessonMaterial.getTransitionName() + "' failed validation checks because there are no media files in this collection.\n\n"+
                 "Please add media to present in this media collection.", null));
        }

        return validationResults;
    }

    /**
     * Check the Present Survey course element for GIFT validation errors.
     * Note: this method will not attempt to validate survey database references such as GIFT keys or concept survey elements.
     *
     * @param presentSurvey the survey course element to validate
     * @param courseSurveyContext the course survey context that contains references to survey to use in this course
     * @param courseFolder the course folder to use for course content references (e.g. DKF path relative to course folder) found in the course file.
     * @return validation results
     */
    private static GIFTValidationResults checkPresentSurvey(generated.course.PresentSurvey presentSurvey,
            BigInteger courseSurveyContext, AbstractFolderProxy courseDirectory) {

        GIFTValidationResults validationResults = new GIFTValidationResults();

        Serializable sChoice = presentSurvey.getSurveyChoice();

        if(sChoice instanceof String){
            //validate GIFT survey (a direct reference to a GIFT survey key in a survey context) or
            //and AutoTutor dkf (a direct reference to a GIFT dkf, which has a reference to the autotutor sko)

            String giftKey = (String)sChoice;
            if(courseSurveyContext == null){
                validationResults.setCriticalIssue(
                        new DetailedException("Unable to validate the '"+presentSurvey.getTransitionName()+"' course object.",
                        "The course survey context has not been set, therefore the '"+presentSurvey.getTransitionName()+"' course object with the parameter '"+giftKey+"' can't be validated.", null));
            }
        }else if(sChoice instanceof generated.course.Conversation){

            generated.course.Conversation conversation = (generated.course.Conversation)sChoice;

            if(conversation.getType() instanceof generated.course.ConversationTreeFile){
                //validate conversation tree survey

                generated.course.ConversationTreeFile conversationTreeFile = (generated.course.ConversationTreeFile)conversation.getType();
                String conversationFileName = conversationTreeFile.getName();

                FileProxy conversationFile;
                try {
                    conversationFile = courseDirectory.getRelativeFile(conversationFileName);
                    ConversationTreeFileHandler tree = new ConversationTreeFileHandler(conversationFile, true);
                    validationResults = tree.checkConversation();
                } catch (Throwable e) {
                    validationResults.setCriticalIssue(
                            new DetailedException("Failed to retrieve the conversation file named '"+conversationFileName+"' referenced in the course object named '"+presentSurvey.getTransitionName()+"'.",
                            "There was a severe error when trying to retrieve the file:\n"+e.getMessage(), e));
                }

            }else if(conversation.getType() instanceof generated.course.AutoTutorSession){
                //validate autotutor survey (i.e. a direct reference to an autotutor sko)

                generated.course.AutoTutorSession atSession = (generated.course.AutoTutorSession)conversation.getType();

                Serializable atConfiguration = atSession.getAutoTutorConfiguration();
                if(atConfiguration instanceof generated.course.DkfRef){
                    String filename = ((generated.course.DkfRef)atConfiguration).getFile();

                    FileProxy dkfFile;
                    try{
                        dkfFile = courseDirectory.getRelativeFile(filename);
                    }catch(IOException e){
                        validationResults.setCriticalIssue(
                                new DetailedException("Failed to retrieve real time assessment (DKF) referenced by a present survey course object named '"+presentSurvey.getTransitionName()+"'.",
                                "An error occurred while retrieving the DKF '"+filename+"' in '"+courseDirectory.getFileId()+"' : "+e.getMessage(), e));
                        return validationResults;
                    }

                    DomainDKFHandler dkfh;
                    try{
                        dkfh = new DomainDKFHandler(dkfFile, courseDirectory, null, true);
                    }catch(FileValidationException e){
                        validationResults.setCriticalIssue(e);
                        return validationResults;
                    }

                    try {
                        //there can only be 1 AutoTutor condition in a DKF referenced by an AutoTutor survey course transition.
                        int atwsConditions = 0;
                        if(dkfh.getDomainAssessmentKnowledge() != null &&
                                dkfh.getDomainAssessmentKnowledge().getScenario() != null){

                            for(Task task : dkfh.getDomainAssessmentKnowledge().getScenario().getTasks()){

                                Set<AbstractCondition> conditions = getConditions(task.getConcepts());

                                for(AbstractCondition condition : conditions){

                                    if(condition instanceof AutoTutorWebServiceInterfaceCondition){
                                        atwsConditions++;
                                    }
                                }

                            }
                        }

                        if(atwsConditions != 1){
                            validationResults.setCriticalIssue(
                                    new DetailedException("Missing AutoTutor real time assessment (DKF) condition in the '"+presentSurvey.getTransitionName()+"' AutoTutor course object.",
                                    "Found "+atwsConditions+" AutoTutor conditions in "+dkfFile.getFileId()+" for the '"+presentSurvey.getTransitionName()+"' AutoTutor course object.  " +
                                    "Only 1 AutoTutor condition in a DKF referenced by an AutoTutor survey course transition is currently allowed.  Please add/remove conditions accordingly.", null));
                        }

                    } catch (DetailedException e) {
                         validationResults.setCriticalIssue(e);
                    }

                }else if(atConfiguration instanceof generated.course.AutoTutorSKO){

                    generated.course.AutoTutorSKO sko = (generated.course.AutoTutorSKO)atConfiguration;
                    try{
                        checkAutoTutorReference(sko, courseDirectory);
                    }catch(DetailedException e){
                        validationResults.setCriticalIssue(e);
                    }
                    
                }else if(atConfiguration == null){
                    //error
                    validationResults.setCriticalIssue(
                            new DetailedException("The '"+presentSurvey.getTransitionName()+"' AutoTutor course object needs a reference to an AutoTutor conversation.",
                            "Please provide the URL of an AutoTutor conversation.  AutoTutor conversations can be found on the AutoTutor conversation authoring tool site by clicking the add button on the AutoTutor course object editor.", null));
                }else{
                    //error
                    validationResults.setCriticalIssue(
                            new DetailedException("Failed to validate the AutoTutor conversation reference in the '"+presentSurvey.getTransitionName()+"' course object.",
                            "Found an unhandled AutoTutor reference of "+atConfiguration+" that needs validation logic to be written.", null));
                }

                //check if Autotutor webserver is alive
                try{
                    @SuppressWarnings("unused")
                    AutoTutorWebServiceInterface atws = new AutoTutorWebServiceInterface();

                }catch(Exception e){
                    validationResults.setCriticalIssue(
                            new DetailedException("Unable to connect to the AutoTutor webservice, therefore this course will not be able to run.",
                            "The AutoTutor webservice is a third party tool that GIFT utilizes.  Are you able to check that it is running?",
                            e));
                }
            }else{
                validationResults.setCriticalIssue(
                        new DetailedException("Found unhandled conversation type in course object named '"+presentSurvey.getTransitionName()+"'.",
                        "The type "+conversation.getType()+" needs logic to in order to validate the '"+presentSurvey.getTransitionName()+"' course object.",
                        null));
            }

        }else if(sChoice instanceof generated.course.PresentSurvey.ConceptSurvey){

            generated.course.PresentSurvey.ConceptSurvey conceptSurvey = (generated.course.PresentSurvey.ConceptSurvey)sChoice;

            if(courseSurveyContext == null){
                validationResults.setCriticalIssue(
                        new DetailedException("Unable to validate the '"+presentSurvey.getTransitionName()+"' Question Bank course object.",
                        "The course survey context has not been set, therefore the '"+presentSurvey.getTransitionName()+"' course object.", null));
            }

            //make sure the question bank course object has one or more concepts selected/provided
            if(conceptSurvey.getConceptQuestions() == null || conceptSurvey.getConceptQuestions().isEmpty()){
                validationResults.setCriticalIssue(
                        new DetailedException("Unable to check the Question Bank survey for the '"+presentSurvey.getTransitionName()+"' Question Bank course object.",
                        "Please make sure one or more course concepts have been selected/provided in the '"+presentSurvey.getTransitionName()+"' Question Bank course object.", null));
            }
        }else{
            validationResults.setCriticalIssue(
                    new DetailedException("Found unhandled survey choice in the course object named '"+presentSurvey.getTransitionName()+"'.",
                    "The survey type '"+sChoice+"' is not handled by the current validation logic and therefore the '"+presentSurvey.getTransitionName()+"' course object can't be validated.",
                    null));
        }

        return validationResults;
    }

    /**
     * Check the authored branch course object for GIFT validation errors.
     *
     * @param authoredBranch the course object to validate
     * @param courseValidationResults the validation results for a course including the validation results for each course object in the course, successful or not.
     * @param progressIndicator will be populated with any progress updates related to validating the course.  Can be null if progress updates
     * are not wanted.
     * @param perObjectProgress percent progress to add for each course object that is checked in the overall course.
     * @param courseDirectory the directory where the course file lives
     * @param connectionStatus used to indicate whether the Domain module has an Internet connection
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @param courseConceptNames the names of the concepts taught within the course
     * @param courseConceptsTaught the list of course concepts referenced by (i.e. teaching rule/example) Merrill's branch 
     * points (adaptive courseflow course objects) thus far.  The concepts taught to this authored branch course object.
     * @param courseSurveyContext the course survey context that contains references to survey to use in this course
     * @param transitionNames used to check for duplicate transition names which is not allowed
     * @param authoredBranchAncestortNames the collection of authored branch course object names with the highest level (i.e. top level course tree course object)
     * being in the first position of the collection.  This is useful when the validation issue represented by 
     * this class instance is nested under one or more authored branch course objects.  Should at least contain the name of the authored branch course object
     * that lead to this method being called.
     * @return validation results, won't be null.
     */
    private GIFTValidationResults checkAuthoredBranch(generated.course.AuthoredBranch authoredBranch, CourseValidationResults courseValidationResults, ProgressIndicator progressIndicator,
            double perObjectProgress, AbstractFolderProxy courseDirectory, InternetConnectionStatusEnum connectionStatus, boolean failOnFirstSchemaError, Set<String> courseConceptNames, Set<String> courseConceptsTaught,
            BigInteger courseSurveyContext, Set<String> transitionNames, LinkedList<String> authoredBranchAncestortNames){ 

        GIFTValidationResults authoredBranchValidationResults = new GIFTValidationResults();

        int authoredBranchId = authoredBranch.getBranchId().intValue();
        if (authoredBranchIds.contains(authoredBranchId)) {
            // ERROR
            authoredBranchValidationResults.setCriticalIssue(
                    new DetailedException("The authored branch course object named "+authoredBranch.getTransitionName()+" is invalid.",
                    "This branch has the same id, "+authoredBranchId+", as another authored branch course object in this course.  Duplicates are not allowed in order to uniquely identify this branch for tracking the history of path selection.", null));
        } else {

            authoredBranchIds.add(authoredBranchId);

        }

        //
        // either simple distribution (balanced, random, custom)
        //   -if custom % then values must be (0.0-100.0) and total near 100, all must be custom %
        //   -else default path id is specified
        // -or-
        // learner centric conditions are authored
        //

        generated.course.AuthoredBranch.SimpleDistribution simpleDistribution = authoredBranch.getSimpleDistribution();
        if(simpleDistribution == null || simpleDistribution.getRandomOrBalancedOrCustom() instanceof generated.course.AuthoredBranch.SimpleDistribution.Custom){

            //check path level conditions

            Map<Integer, generated.course.AuthoredBranch.Paths.Path> pathIds = new HashMap<>();
            boolean usingCustomPercent = false;
            List<Double> customPercents = new ArrayList<>(0);
            List<generated.course.AuthoredBranch.Paths.Path> paths = authoredBranch.getPaths().getPath();
            for(generated.course.AuthoredBranch.Paths.Path path : paths){

                if(pathIds.containsKey(path.getPathId().intValue())){
                    //ERROR
                    authoredBranchValidationResults.addImportantIssue(
                            new DetailedException("The authored branch course object named "+authoredBranch.getTransitionName()+" is invalid.",
                            "There is more than one path in this branch with the id of "+path.getPathId().intValue()+" named "+
                                    pathIds.get(path.getPathId().intValue()).getName()+" and "+path.getName()+".", null));
                }

                pathIds.put(path.getPathId().intValue(), path);

                if(path.getCondition() == null){
                    //ERROR

                    authoredBranchValidationResults.addImportantIssue(
                            new DetailedException("The authored branch course object named "+authoredBranch.getTransitionName()+" is invalid.",
                            "Every path must have a condition authored when using custom percent or learner centric logic to choose among paths.  "+
                                    "The path named "+path.getName()+" is missing a condition.", null));

                }else{

                    Serializable condition = path.getCondition().getCustomPercentOrLearnerCentric();
                    if(condition instanceof generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric){

                        if(usingCustomPercent){
                            //ERROR - can't mix custom percent and learner centric

                            authoredBranchValidationResults.addImportantIssue(
                                    new DetailedException("The authored branch course object named "+authoredBranch.getTransitionName()+" is invalid.",
                                    "When using custom percent in an authored branch course object, every path must use only custom percent conditions  "+
                                            "The path named "+path.getName()+" is using a learner centric condition.", null));
                        }

                    }else{
                        //custom percent

                        usingCustomPercent = true;

                        if(simpleDistribution == null){
                            authoredBranchValidationResults.addImportantIssue(
                                    new DetailedException("The authored branch course object named "+authoredBranch.getTransitionName()+" is invalid.",
                                    "When using custom percent in an authored branch course object, the Simple Distribution element must specify custom percent but currently it is null.", null));
                        }

                        double percent = ((BigDecimal)condition).doubleValue();
                        if(percent < 0.0 || percent > 100.0){
                            //ERROR
                            authoredBranchValidationResults.addImportantIssue(
                                    new DetailedException("The authored branch course object named "+authoredBranch.getTransitionName()+" is invalid.",
                                    "When using custom percent in an authored branch course object the value provided must be between 0.0 and 100.0.  "+
                                            "The path named "+path.getName()+" is using a custom percent value of "+percent+".", null));
                        }

                        customPercents.add(percent);
                    }
                }
            }//end for

            if(usingCustomPercent){

                //check total - must be within 0.02 of 100.0
                double total = 0;
                for(Double value : customPercents){
                    total += value;
                }

                if(total < 99.98 || total > 100.02){
                    //ERROR
                    authoredBranchValidationResults.addImportantIssue(
                            new DetailedException("The authored branch course object named "+authoredBranch.getTransitionName()+" is invalid.",
                            "When using custom percent in an authored branch course object the total across all paths must be 100.0 (or at least 99.98).  "+
                                    "The total for this authored branch course object is "+total+".", null));
                }
            }else{
                //is default path specified and valid

                if(authoredBranch.getDefaultPathId() == null){
                    //ERROR
                    authoredBranchValidationResults.addImportantIssue(
                            new DetailedException("The authored branch course object named "+authoredBranch.getTransitionName()+" is invalid.",
                            "When using learner centric path conditions the default path must be provided in order to account for any issues in selected a path for a learner.", null));
                }else{

                    int defaultPathId = authoredBranch.getDefaultPathId().intValue();

                    if(!pathIds.containsKey(defaultPathId)){
                        //ERROR
                        authoredBranchValidationResults.addImportantIssue(
                                new DetailedException("The authored branch course object named "+authoredBranch.getTransitionName()+" is invalid.",
                                "The default path with id of "+defaultPathId+" could not found among the  "+pathIds.size()+" paths authored in this branch.", null));
                    }
                }
            }
        }//end if simple distribution        
        
        // copy the current nesting of authored branch course objects to this level of course objects
        // to be used if there are any validation issues at this current level of the course tree
        LinkedList<String> thisLevelAuthoredBranchAncestortNames = new LinkedList<>();
        thisLevelAuthoredBranchAncestortNames.addAll(authoredBranchAncestortNames);
        
        // the course concepts taught by all paths in this authored branch course object,
        // i.e. if all paths don't teach the concept than this authored branch course object can't
        // claim to have taught the concept down stream in the course.
        Set<String> commonConceptsTaught = null;

        //
        // Check transitions
        //
        int checkedCourseObjectsCnt = 0; // number of course objects checked under this  authored branch, including nested authored branches
        for (Path path : authoredBranch.getPaths().getPath()) {
            List<Serializable> transitions = path.getCourseobjects().getAAROrAuthoredBranchOrEnd();

            // used to check for duplicate authored branch ids which is not allowed
            authoredBranchIds = new HashSet<>();

            metadataFileCache.setRuleMetadataFiles(null);
            metadataFileCache.setExampleMetadataFiles(null);
            metadataFileCache.setPracticeMetadataFiles(null);
            metadataFileCache.setRemediationOnlyMetadataFiles(null);
            
            // the concepts taught before this path starts and then adding the concepts taught in this path
            Set<String> courseConceptsThroughThisPath = new HashSet<>(courseConceptsTaught);         

            for(int index = 0; index < transitions.size(); index++){
                
                Serializable transitionObj = transitions.get(index);
                
                // course objects to ignore because they don't actually have any logic
                if(!(transitionObj instanceof AuthoredBranch.Paths.Path.Courseobjects.End)){
                    checkedCourseObjectsCnt++;
                }
                
                if(progressIndicator != null){
                    progressIndicator.increasePercentComplete((int)(perObjectProgress * (index + 1))); //need to calculate percent each time in order to not
                                                                                                  //loose decimal places along the way as you would with incrementing by an int
                }

                try{

                    CourseObjectValidationResults courseObjValidationResults = null;

                    // skip validation if transition is disabled
                    if (isTransitionDisabled(transitionObj)) {
                        if (logger.isInfoEnabled()) {
                            logger.info("skipping checkCourse Validation for disabled transitionObj: " + transitionObj);
                        }
                        courseObjValidationResults = new CourseObjectValidationResults("DISABLED - " + getTransitionName(transitionObj));
                    } else if (isTransitionTrainingApplication(transitionObj)) {
                        // found training application transition

                        generated.course.TrainingApplication trainingAppTransition = (generated.course.TrainingApplication) transitionObj;
                        String transitionName = trainingAppTransition.getTransitionName();
                        courseObjValidationResults = new CourseObjectValidationResults(transitionName);

                        GIFTValidationResults validationResults = checkTrainingAppElement(courseDirectory, trainingAppTransition, courseConceptNames, connectionStatus, null);

                        try {
                            checkTransitionName(transitionName, transitionNames);
                        } catch (DetailedException e) {
                            validationResults.addImportantIssue(e);
                        }

                        courseObjValidationResults.setValidationResults(validationResults);

                    } else if (isTransitionLessonMaterial(transitionObj)) {
                        // found lesson material transition

                        generated.course.LessonMaterial lessonMaterialTransition = (generated.course.LessonMaterial) transitionObj;
                        String transitionName = lessonMaterialTransition.getTransitionName();
                        courseObjValidationResults = new CourseObjectValidationResults(transitionName);

                        GIFTValidationResults validationResults = checkLessonMaterial(lessonMaterialTransition, course, courseDirectory, connectionStatus, failOnFirstSchemaError);

                        try {
                            checkTransitionName(transitionName, transitionNames);
                        } catch (DetailedException e) {
                            validationResults.addImportantIssue(e);
                        }

                        courseObjValidationResults.setValidationResults(validationResults);

                    } else if (isTransitionGuidance(transitionObj)) {
                        // found guidance transition

                        generated.course.Guidance guidanceTransition = (generated.course.Guidance) transitionObj;
                        String transitionName = guidanceTransition.getTransitionName();
                        courseObjValidationResults = new CourseObjectValidationResults(transitionName);

                        GIFTValidationResults validationResults = checkGuidance(guidanceTransition, courseDirectory, connectionStatus);

                        try {
                            checkTransitionName(transitionName, transitionNames);
                        } catch (DetailedException e) {
                            validationResults.addImportantIssue(e);
                        }

                        courseObjValidationResults.setValidationResults(validationResults);

                    } else if (isTransitionMerrillsBranchPoint(transitionObj)) {

                        generated.course.MerrillsBranchPoint merrill = (generated.course.MerrillsBranchPoint) transitionObj;
                        String transitionName = merrill.getTransitionName();
                        courseObjValidationResults = new CourseObjectValidationResults(transitionName);
                        
                        GIFTValidationResults validationResults = checkMerrillsBranchPoint(courseDirectory, courseConceptNames, courseConceptsThroughThisPath, transitionNames, merrill);

                        try {
                            checkTransitionName(transitionName, transitionNames);
                        } catch (DetailedException e) {
                            validationResults.addImportantIssue(e);
                        }

                        courseObjValidationResults.setValidationResults(validationResults);

                    } else if (isTransitionPresentSurvey(transitionObj)) {
                        // found survey transition

                        generated.course.PresentSurvey surveyTransition = (generated.course.PresentSurvey) transitionObj;
                        String transitionName = surveyTransition.getTransitionName();
                        courseObjValidationResults = new CourseObjectValidationResults(transitionName);

                        GIFTValidationResults validationResults = checkPresentSurvey((generated.course.PresentSurvey) transitionObj, courseSurveyContext, courseDirectory);

                        try {
                            checkTransitionName(transitionName, transitionNames);
                        } catch (DetailedException e) {
                            validationResults.addImportantIssue(e);
                        }

                        courseObjValidationResults.setValidationResults(validationResults);

                    } else if (isTransitionAAR(transitionObj)) {
                        // found AAR transition

                        generated.course.AAR aarTransition = (generated.course.AAR) transitionObj;
                        String transitionName = aarTransition.getTransitionName();
                        courseObjValidationResults = new CourseObjectValidationResults(transitionName);

                        GIFTValidationResults validationResults = checkAAR(aarTransition, transitionNames);

                        try {
                            checkTransitionName(transitionName, transitionNames);
                        } catch (DetailedException e) {
                            validationResults.addImportantIssue(e);
                        }

                        courseObjValidationResults.setValidationResults(validationResults);

                    } else if (isTransitionAuthoredBranch(transitionObj)) {

                        generated.course.AuthoredBranch subAuthoredBranch = (generated.course.AuthoredBranch) transitionObj;
                        String transitionName = subAuthoredBranch.getTransitionName();
                        courseObjValidationResults = new CourseObjectValidationResults(transitionName);
                        
                        // add to the current collection of nested ancestor authored branch course objects to use at the next level of the
                        // course tree about to be checked
                        authoredBranchAncestortNames.add(transitionName);                        

                        GIFTValidationResults validationResults = checkAuthoredBranch(subAuthoredBranch, courseValidationResults, progressIndicator, 
                                perObjectProgress, courseDirectory, connectionStatus,
                                failOnFirstSchemaError, courseConceptNames, courseConceptsThroughThisPath, courseSurveyContext, transitionNames, authoredBranchAncestortNames);
                        checkedCourseObjectsCnt += validationResults.getNumOfCourseObjectsChecked();
                        
                        try {
                            checkTransitionName(transitionName, transitionNames);
                        } catch (DetailedException e) {
                            validationResults.addImportantIssue(e);
                        }

                        courseObjValidationResults.setAuthoredBranchAncestortNames(thisLevelAuthoredBranchAncestortNames);
                        courseObjValidationResults.setValidationResults(validationResults);
                        
                    } else if(transitionObj instanceof AuthoredBranch.Paths.Path.Courseobjects.End){
                        // this path ends and doesn't rejoin with the rest of the course after the authored branch course
                        // object, i.e. the course will end when this End course object is reached.
                        
                        // null is used to tell if the path should not be considered for the common course concepts
                        // taught across all paths since the course will now end
                        courseConceptsThroughThisPath = null;
                    }

                    // every course object type should be validated at some level,
                    // this is the catch all others that fell through the cracks
                    if (courseObjValidationResults == null) {
                        courseObjValidationResults = new CourseObjectValidationResults("UNHANDLED TYPE - " + transitionObj);
                    }

                    // every course object at this level needs to report the current nesting of authored branch course object names for use in 
                    // error reporting to the user
                    courseObjValidationResults.setAuthoredBranchAncestortNames(thisLevelAuthoredBranchAncestortNames);
                    
                    courseValidationResults.addCourseObjectResults(courseObjValidationResults);
                }catch(Throwable t){

                    String courseObjectName = getCourseObjectName(transitionObj);
                    CourseObjectValidationResults objectValidationResults = new CourseObjectValidationResults(courseObjectName);
                    GIFTValidationResults validationResults = new GIFTValidationResults();
                    validationResults.setCriticalIssue(
                        new DetailedException("The course object named '"+courseObjectName+"' failed to validate because of an error.",
                        "While validating this course object named '"+courseObjectName+"' the following error was thrown on the server: "+t.toString(), t));
                    objectValidationResults.setValidationResults(validationResults);
                    courseValidationResults.addCourseObjectResults(objectValidationResults);
                }
            } // end for on this path's course objects
            
            //
            // check if the concepts taught on this path are common among all paths
            //
            if(courseConceptsThroughThisPath != null){
                if(commonConceptsTaught == null){
                    // this is the first path, use its concepts as the comparison set for other paths 
                    // in this authored branch course object
                    commonConceptsTaught = new HashSet<>();
                    commonConceptsTaught.addAll(courseConceptsThroughThisPath);
                }else{
                    // this is not the first path, compare concepts, only keeping the concepts that are in common
                    // across all paths in this authored branch course object
                    Iterator<String> commonConceptsTaughtItr = commonConceptsTaught.iterator();
                    while(commonConceptsTaughtItr.hasNext()){
                        String otherPathsConcept = commonConceptsTaughtItr.next();
                        
                        if(!courseConceptsThroughThisPath.contains(otherPathsConcept)){
                            // concept taught in prior paths is not in this path, remove it
                            commonConceptsTaughtItr.remove();
                        }
                    }
                }
            }

        } // end for
        
        authoredBranchValidationResults.setNumOfCourseObjectsChecked(checkedCourseObjectsCnt);
        
        // add any concepts that are taught (at some point) in all paths so that future
        // course objects will know that this authored branch covers some course concepts
        if(commonConceptsTaught != null){
            courseConceptsTaught.addAll(commonConceptsTaught);
        }

        return authoredBranchValidationResults;
    }

    /**
     * Check the guidance course element for GIFT validation errors.
     *
     * @param guidance the guidance course element to check for errors
     * @param courseDirectory the course directory to use for course content references (e.g. DKF path relative to course folder) found in the course file.
     * @param connectionStatus used to indicate whether the Domain module has an Internet connection
     * @return validation results
     */
    private static GIFTValidationResults checkGuidance(generated.course.Guidance guidance,
            AbstractFolderProxy courseDirectory, InternetConnectionStatusEnum connectionStatus){

        GIFTValidationResults validationResults = new GIFTValidationResults();

        //check/validate guidance URL
        Object gChoice = guidance.getGuidanceChoice();
        if(gChoice instanceof generated.course.Guidance.URL){

            generated.course.Guidance.URL gURL = ((generated.course.Guidance.URL)gChoice);

            String url = gURL.getAddress();
            try{
                String correctedUrl = UriUtil.validateUri(url, courseDirectory, connectionStatus);

                if(correctedUrl != null){
                    //update URL to corrected one
                    url = correctedUrl;
                }

                try{
                    String hostname = (new URL(url)).getHost();
                    if(hostname.contains("youtube")){

                        //if a valid URL references a YouTube video that is not embedded in an iFrame, add information to embed the video
                        correctedUrl = LessonMaterialFileHandler.createEmbeddedYouTubeUrl(url, null);
                    }
                } catch (@SuppressWarnings("unused") MalformedURLException e){
                    // skip YouTube URL conversion if the URI does not have a URL protocol or the URL does not match known YouTube URL conventions
                }

                if(correctedUrl != null){
                    gURL.setAddress(correctedUrl);
                }

            }catch(ConnectException exception){

                if(connectionStatus == InternetConnectionStatusEnum.CONNECTED){
                    validationResults.addImportantIssue(
                            new DetailedException("Failed to validate URL for course object named '"+guidance.getTransitionName()+"'.",
                            "The URL '"+url+"' failed validation checks because of not being able to connection to a network resource (e.g. website)." +
                          "  If you are running GIFT without a network/internet connection then please provide an appropriate network connection for this course to be available.",
                          exception));
                }else{

                    validationResults.addWarningIssue(
                            new DetailedException("Failed to validate URL for course object named '"+guidance.getTransitionName()+"'.",
                            "The URL '"+url+"' failed validation checks because of not being able to connection to a network resource (e.g. website)." +
                          "  If you are running GIFT without a network/internet connection then please provide an appropriate network connection.",
                          exception));
                }
            }catch(Exception e){
                validationResults.addImportantIssue(
                        new DetailedException("Failed to validate URL for course object named '"+guidance.getTransitionName()+"'.",
                        "The URL '"+url+"' failed validation checks because of an error.  Is that a valid URL?\n\nThe error message reads : "+e.getMessage(), e));
            }

        }else if(gChoice instanceof generated.course.Guidance.File){

            generated.course.Guidance.File gFile = (generated.course.Guidance.File)gChoice;

            String courseHtmlFilename = gFile.getHTML();
            try{
                if(!courseDirectory.fileExists(courseHtmlFilename)){
                    validationResults.setCriticalIssue(
                            new DetailedException("Failed to find the guidance transition html file.",
                            "Unable to find the html file of '"+courseHtmlFilename+"' referenced in the guidance transition named '"+guidance.getTransitionName()+"'.", null));
                }
            }catch(IOException e){
                validationResults.setCriticalIssue(
                        new DetailedException("Failed to retrieve a guidance transition html file.",
                        "While validating the guidance transition named '"+guidance.getTransitionName()+"' there was a problem retrieving the guidance html file of '"+courseHtmlFilename+"' in '"+courseDirectory+"' : "+e.getMessage(), e));
            }


        }else if(gChoice instanceof generated.course.Guidance.Message){
            
            generated.course.Guidance.Message message = (generated.course.Guidance.Message)gChoice;
            if(StringUtils.isBlank(message.getContent())){
                validationResults.setCriticalIssue(
                        new DetailedException("The guidance course object is missing the text to display to the learner.",
                        "While validating the guidance course object named '"+guidance.getTransitionName()+"' there was a problem found because the content is empty.  Please provide a useful message to display to the learner.", null));
            }
        }else{
            // not a checked type - might need to add a new else-if above
            // NOTE: #5026 also handling when null is set as the guidance choice.
            validationResults.setCriticalIssue(
                    new DetailedException("Found an unhandled guidance type.",
                    "While validating the guidance course object named '"+guidance.getTransitionName()+"' an unhandled type of "+gChoice+" was found.  Perhaps an engineer needs to add logic to check this type or something else went wrong when authoring.", null));
        }

        return validationResults;
    }

    /**
     * Validate the AutoTutor SKO course reference.  This will not parse the SKO but instead make sure
     * it is reachable by GIFT.
     *
     * @param skoRef contains the reference to the SKO to check
     * @param courseFolder used if the SKO being referenced is a file in the course folder
     * @throws DetailedException if there was a validation issue with the reference
     */
    public static void checkAutoTutorReference(generated.course.AutoTutorSKO skoRef, AbstractFolderProxy courseFolder) throws DetailedException{

        Serializable script = skoRef.getScript();
        if(script instanceof generated.course.LocalSKO){

            String filename = ((generated.course.LocalSKO)script).getFile();
            AutoTutorModel.checkAutoTutorReference(filename, courseFolder);

        }else if(script instanceof generated.course.ATRemoteSKO){

            String scriptNameOrURL = ((generated.course.ATRemoteSKO)script).getURL().getAddress();

            String scriptURL = scriptNameOrURL;
            AutoTutorModel.checkAutoTutorReference(scriptURL, courseFolder);
        }

    }

    /**
     * Get the training application reference object from the training app ref XML file.
     *
     * @param contentFile the training application reference XML file to parse
     * @param failOnFirstSchemaError whether to exit parsing upon the first schema related issue
     * @return the unmarshalled file information
     */
    public static UnmarshalledFile getTrainingAppReference(FileProxy contentFile, boolean failOnFirstSchemaError){

        //get generated class instance from XML file
        UnmarshalledFile unmarshalledFile;
        try {
            unmarshalledFile = AbstractSchemaHandler.parseAndValidate(generated.course.TrainingApplicationWrapper.class,
                    contentFile.getInputStream(), AbstractSchemaHandler.TRAINING_APP_ELEMENT_SCHEMA_FILE,
                    failOnFirstSchemaError);
        } catch (Exception e) {
            logger.error("Caught exception while trying to parse the training application XML file of "+contentFile+".", e);
            
            String details = e.getMessage();
            if(StringUtils.isBlank(details)){
                details = e.toString();
                if(StringUtils.isBlank(details)){
                    details = "An error was thrown that prevented the file from being read but the details could not be shown here.  See the logs for more information.";
                }
            }
            throw new FileValidationException("There was a problem while parsing the training application XML file during validation.",
                    e.getMessage(),
                    contentFile.getFileId(),
                    e);
        }

        return unmarshalledFile;
    }

    /**
     * Check the training application reference file for issues.
     *
     * @param taWrapper the training application reference to validate
     * @param courseFolder contains course files relevant to the training application reference file (e.g. dkf)
     * @param courseConceptNames collection of unique course concept names used to find which DKF concept names
     * are course concepts.<br/>
     * When the dkf has:<br/>
     * 1. remediation option defined, the course concepts in the DKF are used to check for remediation metadata tagged
     * content. <br/>
     * 2. otherwise, the course concepts are used to make sure the DKF contains all of the concepts provided.  This
     * is useful when making sure a metadata referenced DKF actually assesses the concepts it should.<br/>
     * When the course concepts list is null or empty these checks are not performed.
     * @param status used to indicate whether the Domain module has an Internet connection
     * @throws FileValidationException if there was a problem parsing and validating the training application reference file against the schema
     * @throws DKFValidationException if there was a validation problem with the DKF referenced by the training application reference file
     * @throws DetailedException if there was a problem retrieving the DKF referenced by the training application reference file
     */
    public static GIFTValidationResults validateTrainingAppReference(generated.course.TrainingApplicationWrapper taWrapper,
            AbstractFolderProxy courseFolder, Collection<String> courseConceptNames, InternetConnectionStatusEnum status){

        GIFTValidationResults validationResults;

        generated.course.TrainingApplication trainingApp = taWrapper.getTrainingApplication();

        try{
            validationResults = DomainCourseFileHandler.checkTrainingAppElement(courseFolder, trainingApp, courseConceptNames, status, null);
        }catch(DKFValidationException e){
            validationResults = new GIFTValidationResults();
            validationResults.setCriticalIssue( new DKFValidationException("Failed to validate the training application reference.",
                    e.getMessage(),
                    trainingApp.getDkfRef().getFile(),
                    e));
        }

        return validationResults;
    }

    /**
     * Return the path of the character to show when this training application object is started during a course.
     * This is the 'show character initially' value when authoring this training application object.  This will
     * not look at the real time assessment (DKF) referenced by this training application object.
     *
     * @param trainingAppObject the training application object to check for a character to show initially value.
     * @return ideally this returns the authored character path for the character to show when this training application
     * object is first started during a course but there are edge cases:</br>
     * 1. if the training application object provided is null, null is returned</br>
     * 2. if the 'show character initially' was selected but a custom character was not provided (means use the default character),
     *    an empty string will be returned.
     */
    public static String getCustomCharacterPath(final generated.course.TrainingApplication trainingAppObject){

        String customCharacter = null;
        if(trainingAppObject != null){

            TrainingApplication.Options trainingAppOptions = trainingAppObject.getOptions();

            if(trainingAppOptions != null) {
                //show the initial avatar in the tutor

                ShowAvatarInitially showAvatar = trainingAppOptions.getShowAvatarInitially();

                if (showAvatar != null) {
                    //the user wants the avatar to appear at the beginning of the lesson, before
                    //it speaks a single word.

                    ShowAvatarInitially.MediaSemantics avatar = showAvatar.getAvatarChoice();
                    if(avatar != null){
                        customCharacter = avatar.getAvatar();
                    }else{
                        customCharacter = Constants.EMPTY;
                    }
                }
            }
        }

        return customCharacter;
    }

    /**
     * Check the training application course element for GIFT validation errors.
     *
     * @param courseDirectory the course directory to use for course content references (e.g. DKF path relative to course folder) found in the course file.
     * @param tAppElement the course training application element to validate
     * @param courseConceptNames collection of unique course concept names used to find which DKF concept names
     * are course concepts.  This can be used for training application remediation metadata searches.  If null, the remediation
     * check will not be performed which is acceptable if the training app reference is from a metadata file which already
     * leverages course concept validation checks. 
     * @param connectionStatus information about if the computer is currently connected to the Internet.  This is used when validating web apps.
     * @param additionalValidation contains additional validation checks that need to be performed.  Can be null
     * @return validation results
     */
    public static GIFTValidationResults checkTrainingAppElement(AbstractFolderProxy courseDirectory,
            generated.course.TrainingApplication tAppElement, Collection<String> courseConceptNames, InternetConnectionStatusEnum connectionStatus, AbstractAdditionalValidationSettings additionalValidation) {

        GIFTValidationResults validationResults = new GIFTValidationResults();

        // is there a 'show character initially'
        String initialCharacter = getCustomCharacterPath(tAppElement);

        //check/validate dkf reference
        generated.course.DkfRef dkfRef = tAppElement.getDkfRef();
        if(dkfRef == null){
            validationResults.setCriticalIssue(new DetailedException(
                    "Failed to retrieve the DKF from a external application course object named '"
                            + tAppElement.getTransitionName() + "'.",
                    "The DKF element is null and was most likely not authored", new NullPointerException()));
            return validationResults;
        }

        String filename = dkfRef.getFile();

        if (filename == null || filename.isEmpty()) {
            validationResults.setCriticalIssue(new DetailedException(
                    "Failed to retrieve the DKF file from a external application course object named '"
                            + tAppElement.getTransitionName() + "'.",
                    "The DKF file name is null or empty", new NullPointerException()));
            return validationResults;
        }

        FileProxy dkfFile;
        try{
            dkfFile = courseDirectory.getRelativeFile(filename);
        }catch(Exception e){
            validationResults.setCriticalIssue(
                    new DKFValidationException(
                            "Failed to retrieve the DKF from a external application course object named '"+tAppElement.getTransitionName()+"'.",
                            e.getMessage(),
                            filename,
                            e));
            return validationResults;
        }

        if(logger.isInfoEnabled()){
            logger.info("Checking the training application course element DKF reference of "+dkfFile.getFileId()+".");
        }        
        
        TrainingApplicationEnum taEnum = TrainingAppUtil.getTrainingAppType(tAppElement);

        DomainDKFHandler dkfh;
        try{
            dkfh = new DomainDKFHandler(dkfFile, courseDirectory, null, true);
            validationResults = dkfh.checkDKF(additionalValidation, taEnum);
            String dkfCharacter = DomainKnowledgeManager.getCustomDefinedCharacter(dkfh.getDomainActionKnowledge());

            // need to process the paths just in case one has \ and the other has /
            if(initialCharacter != null && dkfCharacter != null && !UriUtil.makeURICompliant(initialCharacter).equals(UriUtil.makeURICompliant(dkfCharacter))){
                //found mismatch between the training application level character and the dkf defined character to show
                //(remember) gift only supports pre-loading a single character during a real-time assessment

                String errorMsg = "Found more than one character defined for a Real-time assessment experience and GIFT only "+
                        "supports a single character per real time assessment.  Make sure that the training application 'show character initially' and real time assessment instructional strategies all use the same character.\n";
                if(initialCharacter.isEmpty()){
                    errorMsg += "1. showing the GIFT default character initially";
                }else{
                    errorMsg += "1. showing this character initially: '"+initialCharacter+"'.";
                }

                if(dkfCharacter.isEmpty()){
                    errorMsg += "\n2. showing the GIFT default character for feedback in the real-time assessment (DKF)";
                }else{
                    errorMsg += "\n2. showing the this character for feedback in the real-time assessment (DKF): '"+dkfCharacter+"'.";
                }
                throw new Exception(errorMsg);
            }
            
            if(CollectionUtils.isNotEmpty(courseConceptNames)){
                // caller is interested in using course concepts collection to validate in some manner

                Scenario scenario = dkfh.getDomainAssessmentKnowledge().getScenario();

                CourseConceptSearchFilter searchFilter = new CourseConceptSearchFilter(courseConceptNames);
                /* Filter will return all course concepts */
                Set<String> dkfCourseConcepts = new HashSet<>();
                scenario.getCourseConcepts(searchFilter, dkfCourseConcepts);

                /* Filter will return all assessed course concepts */
                Set<String> assessedCourseConcepts = new HashSet<>();
                searchFilter.setIsConceptAssessed(Boolean.TRUE);
                scenario.getCourseConcepts(searchFilter, assessedCourseConcepts);

                // If remediation is enabled for this training app course object than check for appropriate metadata match
                // for remediation activities, i.e. all dkf concepts should have at least 1 metadata that covers those concepts
                // and no extraneous concepts not in the dkf.  Not necessary to have a single metadata that covers all
                // dkf course concepts as multiple remediation activities can be shown back to back.
                if(tAppElement.getOptions() != null && tAppElement.getOptions().getRemediation() != null){

                    if (dkfCourseConcepts.isEmpty()) {
                        validationResults.addImportantIssue(new DetailedException(
                                "The real time assessment is missing a course concept for the remediation content.",
                                "In order to use the remediation portion of the training application course object named '"
                                        + tAppElement.getTransitionName()
                                        + "', a course concept must exist in the associated training application.\nPlease add a course concept to the training application.",
                                null));
                    }

                    if(!assessedCourseConcepts.isEmpty()){
                        // check for the existing of at least 1 VALID metadata file that can be selected
                        // during runtime to provide remediation for this dkf if needed.
                        
                        generated.course.TrainingApplication.Options.Remediation remediation = tAppElement.getOptions().getRemediation();
                        
                        MetadataFileCache metadataFileCache = new MetadataFileCache();
                        try{
                            gatherMetadataFiles(courseDirectory, metadataFileCache);
                        }catch(IOException e){
                            validationResults.addImportantIssue(
                                    new DetailedException("Failed to find the remediation content in this course folder.",
                                        "An error occurred while finding content for the remediation portion of the training application course object named '" + tAppElement.getTransitionName() + "' : " + e.getMessage(), e));
                        }
                        
                        // only Example and remediation only content can be shown after practice according to ICAP
                        Map<FileProxy, generated.metadata.Metadata> exampleRemediations = metadataFileCache.getExampleMetadataFiles();
                        Map<FileProxy, generated.metadata.Metadata> remediationOnlyRemediations = metadataFileCache.getRemediationOnlyMetadataFiles();
                        if(exampleRemediations.isEmpty() && remediationOnlyRemediations.isEmpty()){
                            validationResults.addImportantIssue(
                                    new DetailedException("Failed to find any content that can be used for remediation in this course folder.",
                                        "There are no example or remediation-only tagged content with the concepts '"+assessedCourseConcepts+"' in the course folder."+
                                                " These are needed for the remediation portion of the training application course object named '" + tAppElement.getTransitionName() + "'.", null));
                        }else{
                            // check for content that covers the dkf course concepts
                            
                            Set<String> remainingAssessedConcepts = new HashSet<>(assessedCourseConcepts);
                            
                            if(remediation.getExcludeRuleExampleContent() == null || remediation.getExcludeRuleExampleContent() == BooleanEnum.FALSE){
                                // author choose to include rule/example tagged metadata
                                
                                for(generated.metadata.Metadata metadata : exampleRemediations.values()){
                                    
                                    boolean coversConcepts = true;
                                    Set<String> metadataConcepts = new HashSet<>();
                                    for(generated.metadata.Concept concept : metadata.getConcepts().getConcept()){
                                        
                                        if(assessedCourseConcepts.contains(concept.getName())){
                                            //this metadata contains a concept that is being assessed in this training app dkf
                                            metadataConcepts.add(concept.getName());
                                        }else{
                                            //this metadata contains a concept that is NOT being assessed, therefore can't use it for remediation
                                            coversConcepts = false;
                                            break;
                                        }
                                    }
                                    
                                    if(!coversConcepts){
                                        continue;
                                    }
                                    
                                    // keep track of the remaining assessed course concepts that need metadata for remediation
                                    remainingAssessedConcepts.removeAll(metadataConcepts);
                                    
                                    if(remainingAssessedConcepts.isEmpty()){
                                        break;
                                    }
                                } // end for on example metadata
                            }
                            
                            if(!remainingAssessedConcepts.isEmpty()){
                                //check remediation only tagged content next
                                
                                for(generated.metadata.Metadata metadata : remediationOnlyRemediations.values()){
                                    
                                    boolean coversConcepts = true;
                                    Set<String> metadataConcepts = new HashSet<>();
                                    for(generated.metadata.Concept concept : metadata.getConcepts().getConcept()){
                                        
                                        if(assessedCourseConcepts.contains(concept.getName())){
                                            //this metadata contains a concept that is being assessed in this training app dkf
                                            metadataConcepts.add(concept.getName());
                                        }else{
                                            //this metadata contains a concept that is NOT being assessed, therefore can't use it for remediation
                                            coversConcepts = false;
                                            break;
                                        }
                                    }
                                    
                                    if(!coversConcepts){
                                        continue;
                                    }
                                    
                                    // keep track of the remaining assessed course concepts that need metadata for remediation
                                    remainingAssessedConcepts.removeAll(metadataConcepts);
                                    
                                    if(remainingAssessedConcepts.isEmpty()){
                                        break;
                                    }
                                }//end for on remediation only metadata
                                
                                if(!remainingAssessedConcepts.isEmpty()){
                                    validationResults.addImportantIssue(
                                            new DetailedException("Failed to find enough content that can be used for remediation in this course folder.",
                                                "There is not enough example or remediation only content tagged with the course concepts covered in this real time assessment."+
                                                        " The following course concepts need content in the course folder:\n'"+remainingAssessedConcepts+"'.\nThese are needed for the remediation portion of the training application course object named '" + tAppElement.getTransitionName() + "'.", null));
                                }
                            }
                        }
                    }
                }

                /*- We need to check the DKF to see if its course concepts are
                 * being assessed.
                 * 1. If the DKF doesn't have any course concepts, then
                 * validation passes.
                 * 2. If the DKF has any number of course concepts, then
                 * validation will only pass if they all have a scoring rule. */

                if (dkfCourseConcepts.size() != assessedCourseConcepts.size()) {
                    // ERROR

                    // extract the missing concepts
                    Set<String> missingConcepts = new HashSet<>();
                    for (String courseConcept : dkfCourseConcepts) {

                        if (!assessedCourseConcepts.contains(courseConcept)) {
                            missingConcepts.add(courseConcept);
                        }
                    }

                    validationResults.addImportantIssue(new DetailedException(
                            "The real time assessment is missing the necessary course concepts.",
                            "In order to use the training application scenario, the real time assessment must include overall assessment rules for the following course concepts:\n"
                                    + missingConcepts + ".\n"
                                    + "Please have overall assessment rules for at least one descendant condition for each concept.",
                            null));
                }
            }
            
        } catch (Exception e) {
            String reason = "There was a problem with the real-time assessment (DKF) in the external application course object named '"
                    + tAppElement.getTransitionName() + "'.\n\nPlease open the real-time assessment editor to see the specific components that are invalid.";
            if (e instanceof DetailedException) {
                validationResults.setCriticalIssue(
                        new DKFValidationException(reason, ((DetailedException) e).getDetails(), filename, e));
                return validationResults;
            } else {
                validationResults.setCriticalIssue(new DKFValidationException(reason, e.toString(), filename, e));
                return validationResults;
            }
        }

        //
        // check interop inputs
        //

        if(tAppElement.getInterops() != null) {

            generated.course.Interops courseInterops = tAppElement.getInterops();

            if(courseInterops.getInterop() == null || courseInterops.getInterop().isEmpty()){

                validationResults.setCriticalIssue(
                        new DetailedException("Failed to retrieve the interop application arguments for a training application course element named '"+tAppElement.getTransitionName()+"'.", "The application arguments (interops element) for the training application course element is null or empty and were most likely not authored", new NullPointerException()));
            }else{

                //
                // specific checks
                //

                List<generated.course.Interop> interops = courseInterops.getInterop();

                boolean usingDISPlayback = false, hasVBSInterop = false;
                
                for(generated.course.Interop interop : interops){

                    if(interop.getInteropImpl() == null || interop.getInteropImpl().isEmpty()){
                        validationResults.setCriticalIssue(
                                new DetailedException("The Gateway interop implementation class for this external application course object named '"+tAppElement.getTransitionName()+"' can't be null or empty.",
                                        "The implementation class is used to determine which parts of the Gateway module are needed to communication with external applications during this part of the course.", null));
                        return validationResults;
                    }

                    //check the inputs to the interop class
                    if(interop.getInteropInputs() != null && interop.getInteropInputs().getInteropInput() != null){

                        Serializable input = interop.getInteropInputs().getInteropInput();

                        //Powerpoint - check for valid show file
                        if(input instanceof generated.course.PowerPointInteropInputs){

                            generated.course.PowerPointInteropInputs pptInputs = (generated.course.PowerPointInteropInputs) input;
                            generated.course.PowerPointInteropInputs.LoadArgs loadArgs = pptInputs.getLoadArgs();
                            String showFilename = loadArgs.getShowFile();

                            //exists?
                            try{
                                if(!courseDirectory.fileExists(showFilename)){
                                    throw new IOException("File does not exist.");
                                }
                            }catch(IOException e){

                                DetailedException detailedException;
                                if(StringUtils.isNotBlank(showFilename)){
                                    detailedException = new DetailedException(
                                            "Failed to find the PowerPoint show file named '"+showFilename+"' from the course object named '"+tAppElement.getTransitionName()+"'.",
                                            "Did you upload a PowerPoint show to that course object and does that file still exist in your course folder?\n\n"+e.getMessage(),
                                            e);
                                }else{
                                    detailedException = new DetailedException(
                                            "There is no PowerPoint show file associated with the course object named '"+tAppElement.getTransitionName()+"'.",
                                            "Did you upload a PowerPoint show to that course object and does that file still exist in your course folder?\n\n"+e.getMessage(),
                                            e);
                                }

                                validationResults.setCriticalIssue(detailedException);

                                return validationResults;
                            }

                        } else if(input instanceof generated.course.VBSInteropInputs){

                            generated.course.VBSInteropInputs appInputs = (generated.course.VBSInteropInputs) input;
                            generated.course.VBSInteropInputs.LoadArgs loadArgs = appInputs.getLoadArgs();
                            String scenarioName = loadArgs.getScenarioName();

                            if(scenarioName != null && scenarioName.isEmpty()){
                                // scenario name can be null to indicate that GIFT isn't managing the scenario/menu of VBS

                                validationResults.setCriticalIssue(
                                        new DetailedException(
                                                "No VBS scenario name has been defined for the external application course object named '"+tAppElement.getTransitionName()+"'.",
                                                "Did you assign a scenario name to that external application course object?",
                                                null));
                                return validationResults;
                            }
                            
                            hasVBSInterop = true;

                        } else if(input instanceof generated.course.TC3InteropInputs){

                            generated.course.TC3InteropInputs appInputs = (generated.course.TC3InteropInputs) input;
                            generated.course.TC3InteropInputs.LoadArgs loadArgs = appInputs.getLoadArgs();
                            String scenarioName = loadArgs.getScenarioName();

                            if(scenarioName == null || scenarioName.isEmpty()){

                                validationResults.setCriticalIssue(
                                        new DetailedException(
                                                "No TC3 scenario name has been defined for the external application course object named '"+tAppElement.getTransitionName()+"'.",
                                                "Did you assign a scenario name to that external application course object?",
                                                null));
                                return validationResults;
                            }

                        } else if(input instanceof generated.course.DETestbedInteropInputs){

                            generated.course.DETestbedInteropInputs appInputs = (generated.course.DETestbedInteropInputs) input;
                            generated.course.DETestbedInteropInputs.LoadArgs loadArgs = appInputs.getLoadArgs();
                            String scenarioName = loadArgs.getScenarioName();

                            if(scenarioName == null || scenarioName.isEmpty()){

                                validationResults.setCriticalIssue(
                                        new DetailedException(
                                                "No Testbed scenario name has been defined for the external application course object named '"+tAppElement.getTransitionName()+"'.",
                                                "Did you assign a scenario name to that external application course object?",
                                                null));
                                return validationResults;
                            }

                        } else if(input instanceof generated.course.SimpleExampleTAInteropInputs){

                            generated.course.SimpleExampleTAInteropInputs appInputs = (generated.course.SimpleExampleTAInteropInputs) input;
                            generated.course.SimpleExampleTAInteropInputs.LoadArgs loadArgs = appInputs.getLoadArgs();
                            String scenarioName = loadArgs.getScenarioName();

                            if(scenarioName == null || scenarioName.isEmpty()){

                                validationResults.setCriticalIssue(
                                        new DetailedException(
                                                "No scenario name has been defined for the external application course object named '"+tAppElement.getTransitionName()+"'.",
                                                "Did you assign a scenario name to that external application course object?",
                                                null));
                                return validationResults;
                            }

                        } else if(input instanceof generated.course.GenericLoadInteropInputs){

                            generated.course.GenericLoadInteropInputs appInputs = (generated.course.GenericLoadInteropInputs) input;
                            generated.course.GenericLoadInteropInputs.LoadArgs loadArgs = appInputs.getLoadArgs();
                            String contentRef = loadArgs.getContentRef();

                            if(contentRef == null || contentRef.isEmpty()){

                                validationResults.setCriticalIssue(
                                        new DetailedException(
                                                "No load arguments been defined for the external application course object named '"+tAppElement.getTransitionName()+"'.",
                                                "Did you assign load arguments to that external application course object?",
                                                null));
                                return validationResults;
                            }
                            
                        } else if(input instanceof generated.course.DISInteropInputs){
                            
                            generated.course.DISInteropInputs disInteropInputs = (generated.course.DISInteropInputs)input;
                            
                            if(disInteropInputs.getLogFile() != null){
                                // using a log file for playback
                                
                                final DomainSessionLog domainSessionLog = disInteropInputs.getLogFile()
                                        .getDomainSessionLog();
                                if (domainSessionLog != null) {
                                    usingDISPlayback = true;

                                    if (StringUtils.isBlank(domainSessionLog.getValue())) {
                                        validationResults.setCriticalIssue(new DetailedException(
                                                "No domain session log has been defined for the external application course object named '"
                                                        + tAppElement.getTransitionName() + "'.",
                                                "Did you assign a domain session log to that external application course object?",
                                                null));
                                        return validationResults;
                                    } else {
                                        /* Check if the domain session log
                                         * exists */
                                        try {
                                            if(!courseDirectory.fileExists(domainSessionLog.getValue())){
                                                throw new Exception("File not found");
                                            }
                                        } catch (Exception e) {
                                            validationResults.setCriticalIssue(new DKFValidationException(
                                                    "Failed to retrieve the domain sesion log from a external application course object named '"
                                                            + tAppElement.getTransitionName() + "'.",
                                                    e.getMessage(), filename, e));
                                            return validationResults;
                                        }
                                    }

                                    if (StringUtils.isBlank(domainSessionLog.getAssessmentName())) {
                                        validationResults.setCriticalIssue(new DetailedException(
                                                "No domain session log assessment has been defined for the external application course object named '"
                                                        + tAppElement.getTransitionName() + "'.",
                                                "Did you assign a domain session log assessment to that external application course object?",
                                                null));
                                        return validationResults;
                                    }
                                }
                            }
                            
                        } else if(input instanceof generated.course.HAVENInteropInputs) {
                                                        
                            generated.course.HAVENInteropInputs havenInteropInputs = (generated.course.HAVENInteropInputs)input;
                            
                            if(havenInteropInputs.getLogFile() != null){
                                // using a log file for playback
                                
                                final DomainSessionLog domainSessionLog = havenInteropInputs.getLogFile()
                                        .getDomainSessionLog();
                                if (domainSessionLog != null) {

                                    if (StringUtils.isBlank(domainSessionLog.getValue())) {
                                        validationResults.setCriticalIssue(new DetailedException(
                                                "No domain session log has been defined for the external application course object named '"
                                                        + tAppElement.getTransitionName() + "'.",
                                                "Did you assign a domain session log to that external application course object?",
                                                null));
                                        return validationResults;
                                    } else {
                                        /* Check if the domain session log
                                         * exists */
                                        try {
                                            if(!courseDirectory.fileExists(domainSessionLog.getValue())){
                                                throw new Exception("File not found");
                                            }
                                        } catch (Exception e) {
                                            validationResults.setCriticalIssue(new DKFValidationException(
                                                    "Failed to retrieve the domain sesion log from a external application course object named '"
                                                            + tAppElement.getTransitionName() + "'.",
                                                    e.getMessage(), filename, e));
                                            return validationResults;
                                        }
                                    }

                                    if (StringUtils.isBlank(domainSessionLog.getAssessmentName())) {
                                        validationResults.setCriticalIssue(new DetailedException(
                                                "No domain session log assessment has been defined for the external application course object named '"
                                                        + tAppElement.getTransitionName() + "'.",
                                                "Did you assign a domain session log assessment to that external application course object?",
                                                null));
                                        return validationResults;
                                    }
                                }
                            }
                        } else if(input instanceof generated.course.RIDEInteropInputs) {
                            
                            generated.course.RIDEInteropInputs rideInteropInputs = (generated.course.RIDEInteropInputs)input;
                            
                            if(rideInteropInputs.getLogFile() != null){
                                // using a log file for playback
                                
                                final DomainSessionLog domainSessionLog = rideInteropInputs.getLogFile()
                                        .getDomainSessionLog();
                                if (domainSessionLog != null) {
                            
                                    if (StringUtils.isBlank(domainSessionLog.getValue())) {
                                        validationResults.setCriticalIssue(new DetailedException(
                                                "No domain session log has been defined for the external application course object named '"
                                                        + tAppElement.getTransitionName() + "'.",
                                                "Did you assign a domain session log to that external application course object?",
                                                null));
                                        return validationResults;
                                    } else {
                                        /* Check if the domain session log
                                         * exists */
                                        try {
                                            if(!courseDirectory.fileExists(domainSessionLog.getValue())){
                                                throw new Exception("File not found");
                                            }
                                        } catch (Exception e) {
                                            validationResults.setCriticalIssue(new DKFValidationException(
                                                    "Failed to retrieve the domain sesion log from a external application course object named '"
                                                            + tAppElement.getTransitionName() + "'.",
                                                    e.getMessage(), filename, e));
                                            return validationResults;
                                        }
                                    }
                            
                                    if (StringUtils.isBlank(domainSessionLog.getAssessmentName())) {
                                        validationResults.setCriticalIssue(new DetailedException(
                                                "No domain session log assessment has been defined for the external application course object named '"
                                                        + tAppElement.getTransitionName() + "'.",
                                                "Did you assign a domain session log assessment to that external application course object?",
                                                null));
                                        return validationResults;
                                    }
                                }
                            }
                        } else if(input instanceof generated.course.UnityInteropInputs) {
                            
                            generated.course.UnityInteropInputs unityInteropInputs = (generated.course.UnityInteropInputs)input;
                            
                            //if unity inputs are defined, verify that they are valid
                            if(unityInteropInputs.getLoadArgs() == null || unityInteropInputs.getLoadArgs().getNvpair() == null){

                                validationResults.addWarningIssue(
                                        new DetailedException(
                                                "The '"+tAppElement.getTransitionName()+"' external application course object is missing a set of inputs for an Unity application.",
                                                "The Unity application inputs, when defined, are used to pass additional arguments to the Unity application and may be needed to perform additional logic when the application loads. ",
                                                null));
                            }

                            List<Nvpair> inputPairs = unityInteropInputs.getLoadArgs().getNvpair();

                            if(!inputPairs.isEmpty()){

                                //check for collisions in the unity app inputs
                                List<String> foundKeys = new ArrayList<String>();

                                for(Nvpair pair : inputPairs){

                                    if(pair.getName() == null){

                                        validationResults.addWarningIssue(
                                                new DetailedException(
                                                        "The '"+tAppElement.getTransitionName()+"' external application course object is missing a key for an Unity input.",
                                                        "Missing an input key may cause the wrong input value to be passed to the Unity application, which may cause problems loading the application.",
                                                        null));

                                    } else {

                                        if(foundKeys.contains(pair.getName())){

                                            validationResults.addWarningIssue(
                                                    new DetailedException(
                                                            "The '"+tAppElement.getTransitionName()+"' external application course object has more than one value defined for an Unity application input key named '" + pair.getName() + "'.",
                                                            "Assigning the same input key to multiple values may cause the wrong input value to be passed to the Unity application, which may cause problems loading the application.",
                                                            null));
                                        }

                                        foundKeys.add(pair.getName());
                                    }
                                }
                            }
                        }
                    }
                }  // end for on interop inputs
                
                // check to make sure that any conditions needing the training application to be running
                // will be able to access the training application when a log file is used for playback.
                // This is because playback alone shouldn't require the training application but conditions
                // like Identify POIs need to query a running training application.
                if(taEnum != null && usingDISPlayback){                    
                    
                    if(taEnum == TrainingApplicationEnum.VBS && !hasVBSInterop){
                        // this VBS training app course object has not specified a connection 
                        // to VBS, does the DKF contains conditions that needs this connection?
                        
                        List<String> conditionInfo = dkfh.getInfoOnConditionsNeedingTrainingApp();
                        if(CollectionUtils.isNotEmpty(conditionInfo)){
                            validationResults.setCriticalIssue(
                                    new DetailedException("The real time assessment for the '"+tAppElement.getTransitionName()+
                                            "' VBS course object requires a connection to VBS which has not been specified.", 
                                            "The following concepts reference conditions that require a running VBS application "+
                                            "in order to assess properly but this VBS course object is configured for log file playback only.\n"+conditionInfo, 
                                            null));
                        }
                        
                    }
                }
            }

        } else if(tAppElement.getEmbeddedApps() != null) {

            generated.course.EmbeddedApps courseEmbeddedApps = tAppElement.getEmbeddedApps();

            if(courseEmbeddedApps.getEmbeddedApp() == null){

                validationResults.setCriticalIssue(
                        new DetailedException(
                                "The '"+tAppElement.getTransitionName()+"' external application course object is missing the mandatory embedded application attributes.",
                                "The application arguments (embeddedApps element) for the training application course element is null or empty and were most likely not authored",
                                new NullPointerException()));

            } else {

                //
                // specific checks
                //

                generated.course.EmbeddedApp embeddedApp = courseEmbeddedApps.getEmbeddedApp();

                //verify that each embedded application has an assigned URL
                if(embeddedApp.getEmbeddedAppImpl() == null){

                    validationResults.setCriticalIssue(
                            new DetailedException(
                                    "The '"+tAppElement.getTransitionName()+"' external application course object is missing a implementation reference pointing to an embedded application it is supposed to use.",
                                    "The embedded application implementation is used load the application into the tutoring interface and is needed needed to allow GIFT to display and communicate with the application during this part of the course.",
                                    null));

                    return validationResults;

                } else {

                    if(embeddedApp.getEmbeddedAppImpl() instanceof String){

                        String uri = (String) embeddedApp.getEmbeddedAppImpl();

                        if(uri.isEmpty()){

                            validationResults.setCriticalIssue(
                                    new DetailedException(
                                            "The '"+tAppElement.getTransitionName()+"' external application course object is missing a URL pointing to an embedded application it is supposed to use.",
                                            "The embedded application URL is used load the application into the tutoring interface and is needed needed to allow GIFT to display and communicate with the application during this part of the course.",
                                            null));

                            return validationResults;

                        } else {
                            /* verify that the resource located by that URL is actually reachable */

                            /* first, see if it is a local path relative to the course folder */
                            boolean foundFile = false;
                            try{
                                foundFile = new File(courseDirectory.getFileId() + File.separator + uri).exists();
                            } catch (@SuppressWarnings("unused") Exception e) {
                                foundFile = false;
                            }

                            /* second, see if it is a local path relative to the Training.Apps maps
                             * directory */
                            try {
                                if (!foundFile) {
                                    File mapsFolder = new File(CommonProperties.getInstance().getTrainingAppsDirectory()
                                            + File.separator + PackageUtil.getWrapResourcesDir()
                                            + File.separator + PackageUtil.getTrainingAppsMaps());
                                    foundFile = new File(mapsFolder + File.separator + uri).exists();
                                }
                            } catch (@SuppressWarnings("unused") Exception e) {
                                foundFile = false;
                            }

                            /* third, if it wasn't a local path, check if it's a valid URL */
                            try {
                                if (!foundFile) {
                                String correctedUri = UriUtil.validateUri(uri, courseDirectory, connectionStatus);

                                if(correctedUri != null){
                                    //update URI to corrected one
                                    uri = correctedUri;
                                }

                                if(correctedUri != null){
                                    embeddedApp.setEmbeddedAppImpl(correctedUri);
                                }
                                }
                            }catch(ConnectException exception){

                                if(connectionStatus == InternetConnectionStatusEnum.CONNECTED){

                                    validationResults.setCriticalIssue(
                                            new DetailedException("Failed to validate embedded application.",
                                            "The training application named '"+tAppElement.getTransitionName()+"' failed validation checks because of not being able to connect to a network resource (e.g. website)." +
                                          "  If you are running GIFT without a network/internet connection then please provide an appropriate network connection for this course to be available.",
                                          exception));

                                    return validationResults;

                                }else{

                                    validationResults.setCriticalIssue(
                                            new DetailedException("Failed to validate embedded application.",
                                            "The training application named '"+tAppElement.getTransitionName()+"' failed validation checks because of not being able to connect to a network resource (e.g. website)." +
                                          "  If you are running GIFT without a network/internet connection then please provide an appropriate network connection.",
                                          exception));

                                    return validationResults;
                                }

                            }catch(Exception exception){

                                String message = exception.getMessage();

                                if (message.contains("no protocol:")) {

                                    String[] split = message.split("no protocol: ");
                                    String filePath = split[split.length - 1];

                                    validationResults.setCriticalIssue(
                                            new DetailedException("Failed to validate embedded application.",
                                            "The training application named '"+tAppElement.getTransitionName()+"' failed validation checks because the resource \"" + filePath + "\" could not be found." +
                                             "\n\nCheck to make sure the file exists and can be accessed or replace the existing training application.", exception));

                                    return validationResults;

                                }else{

                                    validationResults.setCriticalIssue(
                                            new DetailedException("Failed to validate embedded application.",
                                        "The training application named '"+tAppElement.getTransitionName()+"' failed validation checks because of an error : " +exception.getMessage(), exception));

                                    return validationResults;
                                }
                            }
                        }

                    } else if(embeddedApp.getEmbeddedAppImpl() instanceof MobileApp){
                        //no need to check implementation for mobile apps

                    } else {

                        validationResults.setCriticalIssue(
                                new DetailedException(
                                        "The '"+tAppElement.getTransitionName()+"' external application course object is using an implementation reference pointing to an unknown embedded application.",
                                        "A valid embedded application implementation is used load the application into the tutoring interface and is needed needed to allow GIFT to display and communicate with the application during this part of the course.",
                                        null));

                        return validationResults;
                    }

                    if(embeddedApp.getEmbeddedAppInputs() != null) {

                        //if embedded app inputs are defined, verify that they are valid
                        if(embeddedApp.getEmbeddedAppInputs().getNvpair() == null){

                            validationResults.addWarningIssue(
                                    new DetailedException(
                                            "The '"+tAppElement.getTransitionName()+"' external application course object is missing a set of inputs for an embedded application.",
                                            "The embedded application inputs, when defined, are used to pass additional arguments to the embedded application and may be needed to perform additional logic when the application loads. ",
                                            null));
                        }

                        List<Nvpair> inputPairs = embeddedApp.getEmbeddedAppInputs().getNvpair();

                        if(inputPairs.isEmpty()){

                            //check for collisions in the embedded app inputs
                            List<String> foundKeys = new ArrayList<String>();

                            for(Nvpair pair : inputPairs){

                                if(pair.getName() == null){

                                    validationResults.addWarningIssue(
                                            new DetailedException(
                                                    "The '"+tAppElement.getTransitionName()+"' external application course object is missing a key for an embedded application input.",
                                                    "Missing an input key may cause the wrong input value to be passed to the embedded application, which may cause problems loading the application.",
                                                    null));

                                } else {

                                    if(foundKeys.contains(pair.getName())){

                                        validationResults.addWarningIssue(
                                                new DetailedException(
                                                        "The '"+tAppElement.getTransitionName()+"' external application course object has more than one value defined for an embedded application input key named '" + pair.getName() + "'.",
                                                        "Assigning the same input key to multiple values may cause the wrong input value to be passed to the embedded application, which may cause problems loading the application.",
                                                        null));
                                    }

                                    foundKeys.add(pair.getName());
                                }
                            }
                        }
                    }
                }
            }
        }


        if(logger.isInfoEnabled()){
            logger.info("Successfully checked the DKF reference of "+dkfFile.getFileId()+".");
        }

        return validationResults;
    }

    /**
     * Return the custom pedagogical configuration file contents based on the file reference found in the
     * configuration object provided as a string.
     *
     * @param courseFolder the course folder that contains the course.xml file.  This is used to retrieve the pedagogy
     * configuration file by the course folder relative path.
     * @param configurations the course.xml configurations object.  Can be null.
     * @return the contents of the pedagogical configuration file referenced by the configurations object.  Will be null if either
     * the configurations object is null or the pedagogical configuration parameter is null.
     * @throws FileNotFoundException if the pedagogical configuration file referenced by course folder relative path was not found.
     * @throws JAXBException if there was a problem unmarshalling the pedagogical configuration file
     * @throws SAXException If that was a SAX error during parsing of the pedagogical configuration file
     * @throws IOException if there was a problem accessing the pedagogical configuration file
     */
    public static String getCustomPedConfiguration(AbstractFolderProxy courseFolder, generated.course.Course.Configurations configurations)
            throws FileNotFoundException, JAXBException, SAXException, IOException{

        return getConfigFileContents(courseFolder, configurations, ModuleTypeEnum.PEDAGOGICAL_MODULE);
    }

    /**
     * Return the custom learner configuration file contents based on the file reference found in the
     * configuration object provided as a string.
     *
     * @param courseFolder the course folder that contains the course.xml file.  This is used to retrieve the learner
     * configuration file by the course folder relative path.
     * @param configurations the course.xml configurations object.  Can be null.
     * @return the contents of the learner configuration file referenced by the configurations object.  Will be null if either
     * the configurations object is null or the learner configuration parameter is null.
     * @throws FileNotFoundException if the learner configuration file referenced by course folder relative path was not found.
     * @throws JAXBException if there was a problem unmarshalling the learner configuration file
     * @throws SAXException If that was a SAX error during parsing of the learner configuration file
     * @throws IOException if there was a problem accessing the learner configuration file
     */
    public static String getCustomLearnerConfiguration(AbstractFolderProxy courseFolder, generated.course.Course.Configurations configurations)
            throws FileNotFoundException, JAXBException, SAXException, IOException{

        return getConfigFileContents(courseFolder, configurations, ModuleTypeEnum.LEARNER_MODULE);
    }

    /**
     * Return the custom sensor configuration file contents based on the file reference found in the
     * configuration object provided as a string.
     *
     * @param courseFolder the course folder that contains the course.xml file.  This is used to retrieve the sensor
     * configuration file by the course folder relative path.
     * @param configurations the course.xml configurations object.  Can be null.
     * @return the contents of the sensor configuration file referenced by the configurations object.  Will be null if either
     * the configurations object is null or the sensor configuration parameter is null.
     * @throws FileNotFoundException if the sensor configuration file referenced by course folder relative path was not found.
     * @throws JAXBException if there was a problem unmarshalling the sensor configuration file
     * @throws SAXException If that was a SAX error during parsing of the sensor configuration file
     * @throws IOException if there was a problem accessing the sensor configuration file
     */
    public static String getCustomSensorConfiguration(AbstractFolderProxy courseFolder, generated.course.Course.Configurations configurations)
            throws FileNotFoundException, JAXBException, SAXException, IOException{

        return getConfigFileContents(courseFolder, configurations, ModuleTypeEnum.SENSOR_MODULE);
    }

    /**
     * Returns the custom configuration file from the configurations object as a string for the specified module type.
     *
     * @param courseFolder the course folder that contains the course.xml file.  This is used to retrieve the
     * configuration file by the course folder relative path.
     * @param configurations the course.xml configurations object.  Can be null.
     * @param module - used to determine who to parse the configuration file (i.e. module type Learner reads a learnerconfiguration.xml file)
     * @return the contents of the configuration file referenced by the course as a string.  will be null if the configurations object
     * is null or the configuration for the module type is null.
     * @throws IOException if there was a problem accessing the configuration file
     * @throws SAXException if that was a SAX error during parsing of the configuration file
     * @throws JAXBException if there was a problem unmarshalling the configuration file
     * @throws FileNotFoundException if the configuration file referenced by course folder relative path was not found.
     */
    private static String getConfigFileContents(AbstractFolderProxy courseFolder,
            generated.course.Course.Configurations configurations, ModuleTypeEnum module)
                    throws FileNotFoundException, JAXBException, SAXException, IOException {

        if(configurations == null) {
            return null;
        }else if(courseFolder == null){
            throw new IllegalArgumentException("The course folder can't be null.");
        }

        FileProxy fileProxy = null;
        File schemaFile = null;
        Class<?> generatedClass = null;

        // set parameters for the module type
        if(module == ModuleTypeEnum.LEARNER_MODULE) {

            if(configurations.getLearner() == null) {
                return null;
            }

            schemaFile = AbstractSchemaHandler.LEARNER_SCHEMA_FILE;
            generatedClass = AbstractSchemaHandler.LEARNER_ROOT;
            fileProxy = courseFolder.getRelativeFile(configurations.getLearner());

        } else if(module == ModuleTypeEnum.SENSOR_MODULE) {

            if(configurations.getSensor() == null) {
                return null;
            }

            schemaFile = AbstractSchemaHandler.SENSOR_SCHEMA_FILE;
            generatedClass = AbstractSchemaHandler.SENSOR_ROOT;
            fileProxy = courseFolder.getRelativeFile(configurations.getSensor());

        } else if(module == ModuleTypeEnum.PEDAGOGICAL_MODULE) {

            if(configurations.getPedagogy() == null) {
                return null;
            }

            schemaFile = AbstractSchemaHandler.EMAP_PEDAGOGICAL_SCHEMA_FILE;
            generatedClass = AbstractSchemaHandler.EMAP_PEDAGOGICAL_ROOT;
            fileProxy = courseFolder.getRelativeFile(configurations.getPedagogy());

        } else {
            throw new IllegalArgumentException("No configuration file found for " + module.getDisplayName());
        }

        UnmarshalledFile unmarshalledFile = AbstractSchemaHandler.parseAndValidate(generatedClass, fileProxy.getInputStream(), schemaFile, true);
        return AbstractSchemaHandler.getAsXMLString(unmarshalledFile.getUnmarshalled(), generatedClass, schemaFile);
    }

    /**
     * Populate the set with the concept names defined in the course 'metadata' (i.e. top most elements of the course).
     *
     * @param conceptNames the collection to place the concept names authored for this course.
     * @param courseConcepts the object that contains the authored course concepts in a list or hierarchical layout
     * @throws DetailedException if a duplicate concept name was found in the course concepts
     */
    private static void getFlattenedConcepts(Set<String> conceptNames, generated.course.Concepts courseConcepts) throws DetailedException{

        if(courseConcepts != null){
            //course concepts are optional, however when specified there can be no duplicate name values

            if(courseConcepts.getListOrHierarchy() instanceof generated.course.Concepts.Hierarchy){
                //gather all the concept names in the hierarchy
                getHierarchyConcepts(conceptNames, ((generated.course.Concepts.Hierarchy)courseConcepts.getListOrHierarchy()).getConceptNode());

            }else{
                //must be a list
                generated.course.Concepts.List conceptList = (generated.course.Concepts.List) courseConcepts.getListOrHierarchy();
                for(generated.course.Concepts.List.Concept concept : conceptList.getConcept()){

                    if(!conceptNames.contains(concept.getName())){
                        conceptNames.add(concept.getName().toLowerCase());
                    }else{
                        throw new DetailedException("Duplicate course concept found.",
                                "Found a duplicate course concept of "+concept.getName()+" which is not allowed.  Course concepts must be uniquely named for identification purposes.",
                                null);
                    }
                }

            }
        }

    }

    /**
     * Update the concept name list with the concept names found in the concept hierarchy provided.
     * This will use a depth first search.
     *
     * @param conceptNames the current list of concept names found and where to add additional found concept names.
     * @param conceptNode a node in the concept hierarchy to check for a concept name as well as descendant concept nodes.
     * @throws DetailedException if a duplicate concept name was found in the course concepts
     */
    private static void getHierarchyConcepts(Set<String> conceptNames, generated.course.ConceptNode conceptNode) throws DetailedException{

        if(!conceptNames.contains(conceptNode.getName())){
            conceptNames.add(conceptNode.getName());
        }else{
            throw new DetailedException("Duplicate course concept found.",
                    "Found a duplicate course concept of "+conceptNode.getName()+" which is not allowed.  Course concepts must be uniquely named for identification purposes.",
                    null);
        }

        if(conceptNode.getConceptNode() != null){

            for(generated.course.ConceptNode childConceptNode : conceptNode.getConceptNode()){
                getHierarchyConcepts(conceptNames, childConceptNode);
            }
        }
    }

    /**
     * Return a collection of the conditions for the given concepts.
     *
     * @param concepts collection of concepts that have child conditions
     * @return Set<AbstractCondition> the collection of the concept's child conditions
     */
    private static Set<AbstractCondition> getConditions(Collection<Concept> concepts){

        Set<AbstractCondition> conditions = new HashSet<>();

        for(Concept concept : concepts){

            if(concept instanceof IntermediateConcept){

                conditions.addAll(getConditions(((IntermediateConcept)concept).getConcepts()));
            }else{
                conditions.addAll(concept.getConditions());
            }
        }

        return conditions;

    }

    /**
     * Return whether the PowerPoint show file provided is supported by GIFT's training application
     * course element.
     *
     * Currently supported types are: pps, ppsx, ppsm
     *
     * @param pptShowFileName the show file to check its file extension.  If null false is returned.
     * @return boolean true iff the show file is supported by GIFT
     */
    public static boolean isSupportedPowerPointShow(String pptShowFileName){

        if(pptShowFileName != null){

            for(String extension : Constants.ppt_show_supported_types){

                if(pptShowFileName.endsWith(extension)){
                    return true;
                }
            }
        }

        return false;

    }

    /**
     * Return whether the HTML file provided is supported by GIFT's course element(s).
     *
     * Currently supported types are: htm, html
     *
     * @param htmlFileName the file to check its file extension
     * @return boolean true iff the file is supported by GIFT
     */
    public static boolean isSupportedHTML(String htmlFileName){

        if(htmlFileName != null){

            for(String extension : Constants.html_supported_types){

                if(htmlFileName.endsWith(extension)){
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Return whether the URL provided is supported by GIFT's course element(s).
     *
     * @param url the URL address to check
     * @return boolean true iff the URL is supported by GIFT
     */
    public static boolean isSupportedURL(String url){

        if(url != null){

            return UriUtil.hasValidURLScheme(url);
        }

        return false;
    }


    /**
     * Update the course option recommendation information based on the survey check response provided. Basically
     * this will update the recommendation value for any course options whose survey references failed verification
     * in the survey database.
     *
     * @param courseOptionsWrapper the object to populate with information about the courses found in the domain directory. Can't be null.
     * @param surveyCheckResponse the result of a survey reference validation check.  Can include failure information on a per
     * survey check request.  If null, this method does nothing.
     * @param surveyCheckRequest The survey check requests used to obtain the surveyCheckResponse. This is needed to report the exact files
     * containing validation errors. Can be null.
     * @throws IllegalArgumentException if a parameter specified is not valid.
     */
    public static void updateCourseOptionsBySurveyCheck(CourseOptionsWrapper courseOptionsWrapper,
            SurveyCheckResponse surveyCheckResponse, SurveyListCheckRequest surveyCheckRequest) throws IllegalArgumentException{

        if(courseOptionsWrapper == null){
            throw new IllegalArgumentException("The course options helper can't be null.");
        }

        if(surveyCheckResponse != null){
            //check response and remove any domains that have references to survey elements not found

            for(String courseId : surveyCheckResponse.getResponses().keySet()){

                //update the domain option by providing an unavailable reason
                DomainOption domainOption = courseOptionsWrapper.domainOptions.get(courseId);
                if(domainOption == null){
                    logger.warn("The UMS reported back a course id of '"+courseId+" which is not known to the domain module's list of "+courseOptionsWrapper.domainOptions.size()+" domains.");
                    continue;
                }

                updateCourseOptionsBySurveyCheck(domainOption, surveyCheckResponse, surveyCheckRequest);
            }

        }
    }


    /**
     * Update the course option recommendation information based on the survey check response provided. Basically
     * this will update the recommendation value for any course options whose survey references failed verification
     * in the survey database.
     *
     * @param courseOptionsWrapper the object to populate with information about the courses found in the domain directory. Can't be null.
     * @param surveyCheckResponse the result of a survey reference validation check.  Can include failure information on a per
     * survey check request.  If null, this method does nothing.
     * @param surveyCheckRequest The survey check requests used to obtain the surveyCheckResponse. This is needed to report the exact files
     * containing validation errors. Can be null.
     * @throws IllegalArgumentException if a parameter specified is not valid.
     */
    public static void updateCourseOptionsBySurveyCheck(DomainOption domainOption,
            SurveyCheckResponse surveyCheckResponse, SurveyListCheckRequest surveyCheckRequest) throws IllegalArgumentException{

        if(domainOption == null){
            throw new IllegalArgumentException("The domain option can't be null.");
        }

        String courseId = domainOption.getDomainId();

        List<ResponseInterface> responses = surveyCheckResponse.getResponses().get(courseId);
        if(responses == null){
            return;
        }

        for(int index = 0; index < responses.size(); index++){

            ResponseInterface response = responses.get(index);
            if(response instanceof FailureResponse){

                // Get the name of file that caused the validation error
                SurveyCheckRequest request = surveyCheckRequest.getRequests().get(courseId).get(index);

                StringBuilder reason = new StringBuilder("Failed to successfully validate the surveys in this course.");

                DomainOptionRecommendation unavailable = new DomainOptionRecommendation(DomainOptionRecommendationEnum.UNAVAILABLE_SURVEY_VALIDATION);

                try {

                    // If the file is not the course file, display it in the error message.
                    if(request.getSourceReference() != null){
                        String filePath = FileFinderUtil.getCourseFolderRelativePath(courseId, request.getSourceReference().getFileId());
                        if(filePath != null && AbstractSchemaHandler.getFileType(request.getSourceReference().getName()) != FileType.COURSE) {
                            reason.append("</br></br>There was an issue with the surveys referenced in one of the course files.<br/><b>File: ").append(filePath).append("</b>");
                        }
                    }

                    reason.append("</br></br>Please use the course creator's validation tool to help resolve this issue. ")
                            .append("If you don't have permission to edit this course than notify the course owner about the issue you are seeing.  ")
                            .append("You may also use the forums at gifttutoring.org for additional help on fixing this course.");

                } catch(Exception e) {
                    logger.error("An exception was caught while trying to retrieve the name of the file that caused a survey validation error.", e);
                }

                unavailable.setReason(reason.toString());
                unavailable.setDetails(((FailureResponse)response).getMessage());
                domainOption.setDomainOptionRecommendation(unavailable);

                break;  //no need to look at additional error messages
            }
        }//end for

    }

    /**
     * Update the course option recommendation information based on the LMS course records provided.  Basically this will
     * update the recommendation value for the course option where there is an LMS course record that shows pass or failure type scoring
     * results.
     *
     * @param course the domain option to potentially change the recommendation value for. Can't be null.
     * @param courseRecords LMS course records for a specific user.  If null this method won't do anything.
     */
    public static void updateCourseOptionByLMSRecords(DomainOption course, LMSCourseRecords courseRecords){

        if(course == null){
            throw new IllegalArgumentException("The course option can't be null.");
        }else if(courseRecords == null){
            return;
        }else if(courseRecords.getRecords().isEmpty()){
            return;  //no records, no assessments
        }

        Map<String, Set<LMSCourseRecord>> latestRecordsByDomain = courseRecords.getRecordsByDomain();

        if(course.getDomainOptionRecommendation() != null){
            //there is a recommendation already

            DomainOptionRecommendationEnum currentRecommendation = course.getDomainOptionRecommendation().getDomainOptionRecommendationEnum();
            if(currentRecommendation.isUnavailableType()){
                //the only other recommendations are of the Unavailable type, if the course is unavailable don't waste
                //time checking LMS records for it
                return;
            }
        }

        if(course.getConcepts() != null) {
            //
            // TODO: update possible options based on LMS records (specifically update the 'DomainOptionRecommendation' attribute of the DomainOption)
            //       Suggest that if a course is 'not available' that the following 'buildSurveyValidationRequest' not be called
            //       because there is no point in checking the survey references in a course that the user can't execute in the first place.
            //

            // Whether all course concepts are assessed at or above expectation
            boolean allAtOrAbove = false;
            // information on what has poor performance
            // Concepts
            StringBuilder belowConcepts = new StringBuilder();
            
            boolean completedLastAttempt = false;
            Set<LMSCourseRecord> records = latestRecordsByDomain.get(course.getDomainId());
            if(records != null){
                // there is a record for this course

                for(LMSCourseRecord record : records){                    
                    // check if this is the course completed record which is produced after each course object
                    AssessmentLevelEnum completeCourseGrade = record.getRoot().getGradeByName(COMPLETE_NODE_NAME);
                    if(completeCourseGrade.hasReachedStandards()){
                        // this course was taken to completion, don't need to check course concept grading
                        completedLastAttempt = true;
                        break;
                    }else if(completeCourseGrade.equals(AssessmentLevelEnum.UNKNOWN)){
                        // this course wasn't completed the last time, keep checking records
                        continue;
                    }

                    // look at the grades for course concepts grades, see if any are failed
                    
                    // Loop through concepts of this course
                    boolean missingConceptGrade = false, failedConcept = false;
                    for(String concept : course.getConceptsAsList()) {
                        
                        AssessmentLevelEnum conceptRecordGrade = record.getRoot().getGradeByName(concept);
                        if(conceptRecordGrade == null){
                            missingConceptGrade = true;
                            break;
                        }else if(!conceptRecordGrade.hasReachedStandards()){
                            failedConcept = true;
                            break;
                        }
                    }
                    
                    if(failedConcept){
                        // maybe another course record will have all passing concepts
                        belowConcepts.append("some of the course concepts");
                        continue;
                    }else if(missingConceptGrade){
                        // maybe another course record will have all passing concepts
                        belowConcepts.append("some of the course concepts");
                        continue;
                    }else{
                        // this record had all passing course concepts
                        allAtOrAbove = true;
                        break;
                    }
                
                } // end for on records                 

                if(completedLastAttempt){
                    // assume the course completed means all course concepts passed
                    allAtOrAbove = true;
                }
            }

            // Set recommendations
            if(allAtOrAbove) {
                // this course is a refresher course 

                // Generate the message
                StringBuilder message = new StringBuilder("You are performing at/above expectation on all course concepts");

                DomainOptionRecommendation recommendation = new DomainOptionRecommendation(DomainOptionRecommendationEnum.NOT_RECOMMENDED);
                recommendation.setReason(message.toString());
                course.setDomainOptionRecommendation(recommendation);

            }else if(belowConcepts.length() > 0) {
                // recommend this course since the course hasn't been passed
                DomainOptionRecommendation recommendation = new DomainOptionRecommendation(DomainOptionRecommendationEnum.RECOMMENDED);
                recommendation.setReason("You are performing below expectation on " + belowConcepts.toString());
                course.setDomainOptionRecommendation(recommendation);
                
            }else if(records != null && !completedLastAttempt){
                // recommend this course since the last attempt was incomplete (and we can't currently tell if the course concepts
                // were passed in a previous attempt)
                DomainOptionRecommendation recommendation = new DomainOptionRecommendation(DomainOptionRecommendationEnum.RECOMMENDED);
                recommendation.setReason("You left the lesson early on your last attempt");
                course.setDomainOptionRecommendation(recommendation);
            }

        } else {
            Set<LMSCourseRecord> records = latestRecordsByDomain.get(course.getDomainId());

            if(records != null) {
                AssessmentLevelEnum latestGrade = AssessmentLevelEnum.UNKNOWN;
                LMSCourseRecord latestRecord = null;
                for(LMSCourseRecord record : records){
                    // check if this is the course completed record which is produced after each course object
                    AssessmentLevelEnum assessment = record.getRoot().getAssessment();
                    if(assessment.hasReachedStandards()){
                        latestGrade = assessment;
                        latestRecord = record;
                        break;
                    }else{
                        // keep latest record by date
                        
                        if(latestRecord == null || latestRecord.getDate().after(record.getDate())){
                            latestRecord = record;
                            latestGrade = assessment;
                        }
                    }
                }

                if(latestRecord == null){
                    // course completion record not found, was a record of taking this course ever stored
                    course.setDomainOptionRecommendation(null);
                }else{
                    Date latestRecordDate = latestRecord.getDate();
    
                    if(latestGrade.hasReachedStandards()) {
                        DomainOptionRecommendation recommendation = new DomainOptionRecommendation(DomainOptionRecommendationEnum.NOT_RECOMMENDED);
                        recommendation.setReason("you met the standards on " + TimeUtil.LEARNER_RECORD_DATE_FORMAT.format(latestRecordDate));
                        course.setDomainOptionRecommendation(recommendation);
                    } else if(latestGrade.isPoorPerforming()) {
                        DomainOptionRecommendation recommendation = new DomainOptionRecommendation(DomainOptionRecommendationEnum.RECOMMENDED);
                        recommendation.setReason("you failed to met the standards on " + TimeUtil.LEARNER_RECORD_DATE_FORMAT.format(latestRecordDate));
                        course.setDomainOptionRecommendation(recommendation);
                    } else /* Unknown grade */ {
                        course.setDomainOptionRecommendation(null);
                    }
                }

            } else /* No course record */ {
                course.setDomainOptionRecommendation(null);
            }
        }
    }

    /**
     * Update the course option recommendation information based on the LMS course records provided.  Basically this will
     * update the recommendation value for any course options whose LMS course records show pass or failure type scoring
     * results.
     *
     * @param courseOptionsWrapper the object to populate with information about the courses found in the domain directory. Can't be null.
     * @param courseRecords LMS course records for a specific user.  If null this method won't do anything.
     */
    public static void updateCourseOptionsByLMSRecords(CourseOptionsWrapper courseOptionsWrapper, LMSCourseRecords courseRecords){

        if(courseRecords == null ||
               courseRecords.getRecords().isEmpty() ){
            return;
        }else if(courseOptionsWrapper == null){
            throw new IllegalArgumentException("The course options helper can't be null.");
        }

        if(!courseRecords.getRecords().isEmpty()) {

            for(DomainOption course : courseOptionsWrapper.domainOptions.values()){

                updateCourseOptionByLMSRecords(course, courseRecords);

            }//end for
        }
    }

    /**
     * Creates a domain option for the given course id.
     *
     * @param courseId unique id of the course. This is the workspace relative path of the course.xml file.
     * @param courseOptionsWrapper the object to populate with information about the course specified. Can't be null.
     * This will only have 1 entry.
     * @param rootFolder the folder containing all course folders. Used to check for courses in.  Can't be null and must exist.
     * @param validateLogic whether or not to validate the course.  The validation logic referred to here is additional
     * GIFT validation logic (e.g. reachable URLs, DKF rules).  Note that this validation can take more time depending on the elements within each course
     * being validated.
     * @param exitOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @param userName Name of the user requesting to build the domain option data.
     * @param progressIndicator will be populated with any progress updates related to validating the course.  Can be null if progress updates
     * are not wanted.
     * @return the created domain option.  Null if this course is marked to be excluded or there was a problem parsing the course.
     * @throws IllegalArgumentException if a parameter specified is not valid.
     * @throws FileNotFoundException if the domain directory or course file specified was not found.
     */
    public static DomainOption getCourse(String courseId, CourseOptionsWrapper courseOptionsWrapper,
            AbstractFolderProxy rootFolder, boolean validateLogic, boolean exitOnFirstSchemaError, String userName, ProgressIndicator progressIndicator) throws IllegalArgumentException, FileNotFoundException {

        if(rootFolder == null){
            throw new IllegalArgumentException("The domain directory can't be null.");
        }else if(courseOptionsWrapper == null){
            throw new IllegalArgumentException("The course options helper can't be null.");
        }else if(courseId == null || courseId.isEmpty()){
            throw new IllegalArgumentException("The course id can't be null or empty.");
        }

        try {

            FileProxy courseFile = rootFolder.getRelativeFile(courseId);
            return buildDomainOption(courseFile, rootFolder, courseOptionsWrapper, validateLogic,
                    exitOnFirstSchemaError, userName, progressIndicator);

        } catch (Throwable e) {

            logger.error("Caught exception while parsing course file named " + courseId + ", therefore not adding this course to the list of available courses for "+userName+".", e);

            courseOptionsWrapper.parseFailedFiles.add(courseId);
        }

        return null;
    }

    /**
     * Build a domain option for the given course file.  The domain option will be added to the course options
     * provided.
     *
     * @param courseFile the course file to build a domain option for. This MUST be updated to the latest version of the schema by this point.
     * @param rootDirectory the workspaces directory (usually 'workspaces') where all workspaces (e.g. Public, mhoffman) are located.  Can't be null.
     * @param courseOptionsWrapper the object to populate with information about the courses found in the domain directory. Can't be null.
     * @param validateLogic whether or not to validate the course.  The validation logic referred to here is additional
     * GIFT validation logic (e.g. reachable URLs, DKF rules).  Note that this validation can take more time depending on the elements within each course
     * being validated.
     * @param exitOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @param userName - Name of the user requesting to build the domain option data.
     * @param progressIndicator will be populated with any progress updates related to validating the course.  Can be null if progress updates
     * are not wanted.
     * @return the domain option object created.  Null if this course is flagged to be excluded.
     * @throws Exception if there was a problem parsing the course
     * @throws CourseFileValidationException if there is a problem parsing or configuring the course
     */
    public static DomainOption buildDomainOption(FileProxy courseFile, AbstractFolderProxy rootDirectory, CourseOptionsWrapper courseOptionsWrapper,
            boolean validateLogic, boolean exitOnFirstSchemaError, String userName, ProgressIndicator progressIndicator) throws Exception, CourseFileValidationException{

        AbstractFolderProxy courseDirectory = rootDirectory.getParentFolder(courseFile);

        //don't attempt to validate against GIFT logic, just parse the file first
        DomainCourseFileHandler dcfh = new DomainCourseFileHandler(courseFile, courseDirectory, exitOnFirstSchemaError);
        final InternetConnectionStatusEnum connectionStatus = UriUtil.getInternetStatus();
        dcfh.setCurrentInternetStatus(connectionStatus);

        if(logger.isInfoEnabled()){
            logger.info("Successfully parsed course file named " + courseFile.getFileId());
        }

        String relativeFileName = rootDirectory.getRelativeFileName(courseFile);


        boolean exclude = false;

        if (dcfh.getCourse().getExclude() != null) {
            exclude = Boolean.valueOf(dcfh.getCourse().getExclude().value());
        }

        if (!exclude) {

            if(logger.isInfoEnabled()){
                logger.info("Adding course mapping of '"+relativeFileName+"' to '"+courseFile.getFileId()+"'.");
            }

            courseOptionsWrapper.courseFileNameToFileMap.put(relativeFileName, courseFile);
            courseOptionsWrapper.courseFileNameToHandler.put(relativeFileName, dcfh);

            DomainOption option = new DomainOption(dcfh.getCourse().getName(), relativeFileName, dcfh.getCourse().getDescription(), userName);
            if(dcfh.getCourse().getConcepts() != null){
                option.setConcepts(dcfh.getCourse().getConcepts());
            }
            if(dcfh.getSurveyContextId() != null) {
                option.setSurveyContextId(dcfh.getSurveyContextId());
            }

            String relativeImageFileName =  Constants.EMPTY;
            String imageFileId = Constants.EMPTY;
            // Ensure we have a valid course tile image for the course.  Some courses may have null or empty course tile images.
            if (dcfh != null && dcfh.getCourse() != null && dcfh.getCourse().getImage() != null &&
                    !dcfh.getCourse().getImage().isEmpty()) {

                if(courseDirectory.fileExists(dcfh.getCourse().getImage())){

                    imageFileId = courseDirectory.getFileId() + Constants.FORWARD_SLASH + dcfh.getCourse().getImage();

                    relativeImageFileName = rootDirectory.getRelativeFileName(imageFileId);

                    if(logger.isInfoEnabled()){
                        logger.info("Course Tile Image fileId = " + imageFileId + ", relative name = " + relativeImageFileName);
                    }
                } else {
                    logger.error("Unable to find the course image '" + dcfh.getCourse().getImage()+"' in the course folder '"+courseDirectory.getFileId()+"'.");
                }
            }

            option.setImageURL(getAssociatedCourseImage(imageFileId, relativeImageFileName, userName));
            option.setDomainIdWritable(courseFile.canWrite(userName));
            option.setDomainIdReadable(courseFile.canRead(userName));

            if(validateLogic){
                try{
                    CourseValidationResults validationResults = dcfh.checkCourse(exitOnFirstSchemaError, progressIndicator);

                    if(validationResults.hasCriticalIssue() ||
                            validationResults.hasImportantIssues() ||
                            validationResults.hasWarningIssues()){

                        //
                        // First : check course level issues
                        //
                        Throwable criticalIssue = validationResults.getCriticalIssue();
                        if(criticalIssue != null){

                            String details = criticalIssue.getMessage();
                            String reason = "The course is unavailable because it failed to pass the GIFT validation checks.  Please use the course authoring tool for additional details and help to make the course valid.";
                            if(criticalIssue instanceof DetailedException){
                                DetailedException dException = (DetailedException)criticalIssue;
                                reason += "\n" + dException.getReason();
                            }

                            DomainOptionRecommendation recommendation = new DomainOptionRecommendation(DomainOptionRecommendationEnum.UNAVAILABLE_LOGIC_VALIDATION);
                            recommendation.setReason(reason);
                            recommendation.setDetails(details);
                            recommendation.setCourseValidationResulst(validationResults);
                            option.setDomainOptionRecommendation(recommendation);

                        }else if(validationResults.getImportantIssues() != null && !validationResults.getImportantIssues().isEmpty()){

                            Throwable firstIssue = validationResults.getFirstError();
                            String details = firstIssue.getMessage();
                            String reason = "The course is unavailable because it failed to pass the GIFT validation checks.  Please use the course authoring tool for additional details and help to make the course valid.";
                            if(criticalIssue instanceof DetailedException){
                                DetailedException dException = (DetailedException)criticalIssue;
                                reason += "\n" + dException.getReason();
                            }

                            DomainOptionRecommendation recommendation = new DomainOptionRecommendation(DomainOptionRecommendationEnum.UNAVAILABLE_LOGIC_VALIDATION);
                            recommendation.setReason(reason);
                            recommendation.setDetails(details);
                            recommendation.setCourseValidationResulst(validationResults);
                            option.setDomainOptionRecommendation(recommendation);

                        }else if(validationResults.getWarningIssues() != null && !validationResults.getWarningIssues().isEmpty()){

                            Throwable firstIssue = validationResults.getFirstError();
                            String reason = firstIssue.getMessage();

                            DomainOptionRecommendation recommendation = new DomainOptionRecommendation(DomainOptionRecommendationEnum.AVAILABLE_WITH_WARNING);
                            recommendation.setReason("The course is available despite being unable to access certain resources used in the course due to a lack of Internet connection at the moment.  Please consider that parts of the course might not appear as the author intended.");
                            recommendation.setDetails(reason);
                            recommendation.setCourseValidationResulst(validationResults);
                            option.setDomainOptionRecommendation(recommendation);

                        }else{
                            //Second : check course objects

                            DomainOptionRecommendation worstRecommendation = null;

                            for(CourseObjectValidationResults courseObjectValidationResult : validationResults.getCourseObjectResults()){

                                if(courseObjectValidationResult.getValidationResults() == null ||
                                        (!courseObjectValidationResult.getValidationResults().hasCriticalIssue() &&
                                        !courseObjectValidationResult.getValidationResults().hasImportantIssues() &&
                                        !courseObjectValidationResult.getValidationResults().hasWarningIssues())){
                                    //no validation issues to handle
                                    continue;
                                }

                                GIFTValidationResults giftValidationResults = courseObjectValidationResult.getValidationResults();
                                criticalIssue = giftValidationResults.getCriticalIssue();
                                if(criticalIssue != null){
                                    //the worst validation issue

                                    String details = criticalIssue.getMessage();
                                    String reason = "The course is unavailable because it failed to pass the GIFT validation checks.  Please use the course authoring tool for additional details and help to make the course valid.";
                                    if(criticalIssue instanceof DetailedException){
                                        DetailedException dException = (DetailedException)criticalIssue;
                                        reason += "\n" + dException.getReason();
                                    }

                                    worstRecommendation = new DomainOptionRecommendation(DomainOptionRecommendationEnum.UNAVAILABLE_LOGIC_VALIDATION);
                                    worstRecommendation.setReason(reason);
                                    worstRecommendation.setDetails(details);
                                    worstRecommendation.setCourseValidationResulst(validationResults);
                                    option.setDomainOptionRecommendation(worstRecommendation);

                                    break; //nothing can be worse than this

                                }else if(giftValidationResults.getImportantIssues() != null && !giftValidationResults.getImportantIssues().isEmpty()){

                                    if(worstRecommendation != null && worstRecommendation.getDomainOptionRecommendationEnum() == DomainOptionRecommendationEnum.UNAVAILABLE_LOGIC_VALIDATION){
                                        continue;  //important is not worst than critical issue
                                    }

                                    Throwable firstIssue = giftValidationResults.getFirstError();
                                    String details = firstIssue.getMessage();
                                    String reason = "The course is unavailable because it failed to pass the GIFT validation checks.  Please use the course authoring tool for additional details and help to make the course valid.";
                                    if(criticalIssue instanceof DetailedException){
                                        DetailedException dException = (DetailedException)criticalIssue;
                                        reason += "\n" + dException.getReason();
                                    }

                                    worstRecommendation = new DomainOptionRecommendation(DomainOptionRecommendationEnum.UNAVAILABLE_LOGIC_VALIDATION);
                                    worstRecommendation.setReason(reason);
                                    worstRecommendation.setDetails(details);
                                    worstRecommendation.setCourseValidationResulst(validationResults);

                                }else if(giftValidationResults.getWarningIssues() != null && !giftValidationResults.getWarningIssues().isEmpty()){

                                    if(worstRecommendation != null &&
                                            (worstRecommendation.getDomainOptionRecommendationEnum() == DomainOptionRecommendationEnum.UNAVAILABLE_LOGIC_VALIDATION)){
                                        continue;  //warning is not worst than critical or important issue
                                    }

                                    Throwable firstIssue = giftValidationResults.getFirstError();
                                    String reason = firstIssue.getMessage();

                                    worstRecommendation = new DomainOptionRecommendation(DomainOptionRecommendationEnum.AVAILABLE_WITH_WARNING);
                                    worstRecommendation.setReason("The course is available despite being unable to access certain resources used in the course due to a lack of Internet connection at the moment.  Please consider that parts of the course might not appear as the author intended.");
                                    worstRecommendation.setDetails(reason);
                                    worstRecommendation.setCourseValidationResulst(validationResults);

                                }else{
                                    throw new Exception("Ooops!  Found an unhandled validation state for this course.  This needs to be fixed.");
                                }


                            }//end for

                            if(worstRecommendation != null){
                                option.setDomainOptionRecommendation(worstRecommendation);
                            }
                        }


                    }

                }catch(Throwable e){
                    logger.error("Caught (general) exception with domain option of "+option+" while trying to gather possible domain options to check.", e);

                    DomainOptionRecommendation unavailable = new DomainOptionRecommendation(DomainOptionRecommendationEnum.UNAVAILABLE_OTHER);
                    unavailable.setReason("Failed to successfully validate the course.");
                    unavailable.setDetails(e.getMessage());
                    option.setDomainOptionRecommendation(unavailable);
                }
            }

            // determine if the user that this domain option is being built for is the owner of the course
            // represented by this domain option
            if (courseFile.getFileProxyPermissions() != null) {

                AbstractFolderProxy workspaceFolder = courseDirectory.getParentFolder(null);
                if (workspaceFolder != null) {
                    String parentUser = workspaceFolder.getName();  //e.g. Public, mhoffman
                    for (Entry<String, SharedCoursePermissionsEnum> entry : courseFile.getFileProxyPermissions().getUserToPermission().entrySet()) {
                        // The user is an owner if the permission's username specifies their
                        // username AND that matches the workspace folder name
                        // -OR-
                        // desktop mode
                        boolean isOwner = parentUser.equalsIgnoreCase(entry.getKey())
                                || !DomainModuleProperties.getInstance().isServerDeploymentMode();
                        option.addDomainOptionPermissions(
                                new DomainOptionPermissions(entry.getKey(), entry.getValue(), isOwner));
                    }
                }
            }

            courseOptionsWrapper.domainOptions.put(option.getDomainId(), option);

            return option;
        }

        return null;
    }

    /**
     * Gets the appropriate image path based on the image file id for a course tile image.
     *
     * @param imageFileId - The fileId where the image lives.  This will be different based on desktop or server mode.<br/>
     * E.g. (server mode) /default-domain/workspaces/Public/COIN AutoTutor/autotutor.png
     * @param relativeFileName - the course folder relative file name for the image file (including tile image, name, etc.).  Can be null or empty.<br/>
     * E.g. (server mode) Public/COIN AutoTutor/autotutor.png
     * @param username - used for authentication purposes when retrieving the image
     * @return String - Returns the image path that should be used for this course.
     */
    public static String getAssociatedCourseImage(String imageFileId, String relativeFileName, String userName) {

        String imagePath = "";
        if(logger.isDebugEnabled()){
            logger.debug("getAssociatedCourseImage called with imageFileId(" + imageFileId + ") + relativeFileName=" + relativeFileName +
                         ", userName = " + userName);
        }
        if(relativeFileName != null && !relativeFileName.isEmpty()){

            if (DomainModuleProperties.getInstance().getDeploymentMode().equals(DeploymentModeEnum.SERVER)){

                String imageURL = CommonProperties.getInstance().getDashboardURL();
                String dashboardPath = CommonProperties.getInstance().getDashboardPath();

                imageURL = imageURL + Constants.FORWARD_SLASH + dashboardPath + Constants.FORWARD_SLASH +
                                CommonProperties.getInstance().getDashboardMemoryFileServletSubPath() +
                                MemoryFileServletRequest.encode(new MemoryFileServletRequest(relativeFileName, userName));

                imagePath = imageURL;

            } else {

                if (!relativeFileName.isEmpty()) {

                    // The url should look something like this: http://<ip>:<port>/workspace/nblomberg/TestCourse/testimage.jpg
                    String workspaceImagePath = CommonProperties.getInstance().getWorkspaceDirectoryName() + Constants.FORWARD_SLASH + relativeFileName;

                    File imageFile = new File(imageFileId);
                    if(imageFile.exists()){

                        // This will be a full url to the image on the domain content file server.
                        imagePath = DomainModuleProperties.getInstance().getDomainContentServerAddress() + Constants.FORWARD_SLASH + workspaceImagePath;
                    }
                    else{
                        imagePath = DomainOption.FILE_NOT_FOUND_IMAGE;
                    }
                } else {
                    imagePath = DomainOption.FILE_NOT_FOUND_IMAGE;
                }
            }
        }

        if(logger.isDebugEnabled()){
            logger.debug("getAssociatedCourseImage - for image file id (" + imageFileId + ") returning imagePath of: " + imagePath);
        }
        return imagePath;
    }

    /**
     * Find all the courses in the directory provided.  This method reports parsing and validation errors in the
     * course options wrapper object.
     *
     * @param courseOptionsWrapper the object to populate with information about the courses found in the domain directory. Can't be null.
     * @param rootWorkspaceDirectory the root directory where all course folders are in.  Can't be null and must exist.
     * @param folderToSearch the folder that all the course folders should actually be fetched from
     * @param pathsToExclude the paths that should not be searched when finding the courses to return.
     * <em>Example:</em> If pathsToExclude contains 'foo/', then the files 'foo/bar.txt' and
     * 'foo/baz/file.txt' should not be included in the returned results.
     * @param validateLogic whether or not to validate the courses found.  The validation logic referred to here is additional
     * GIFT validation logic (e.g. reachable URLs, DKF rules).  Note that this validation can take more time depending on the elements within each course
     * being validated.
     * @param exitOnFirstSchemaError if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @param username used for authentication, Name of the user requesting to get the courses
     * @param progressIndicator will be populated with any progress updates related to validating the course.  Can be null if progress updates
     * are not wanted.
     * @throws IllegalArgumentException if a parameter specified is not valid.
     * @throws IOException if there was a problem retrieving courses
     */
    public static void getAllCourses(CourseOptionsWrapper courseOptionsWrapper,
            AbstractFolderProxy rootWorkspaceDirectory, AbstractFolderProxy folderToSearch, Iterable<String> pathsToExclude,
            boolean validateLogic, boolean exitOnFirstSchemaError, String username, ProgressIndicator progressIndicator) throws IllegalArgumentException, IOException {

        if(rootWorkspaceDirectory == null){
            throw new IllegalArgumentException("The root workspace directory can't be null.");
        } else if (folderToSearch == null) {
            throw new IllegalArgumentException("The directory to search can't be null.");
        } else if(courseOptionsWrapper == null){
            throw new IllegalArgumentException("The course options helper can't be null.");
        }

        if(logger.isInfoEnabled()){
            logger.info("Starting search for course files in "+folderToSearch);
        }

        List<FileProxy> courseFiles = new ArrayList<>();
        FileFinderUtil.getFilesByExtension(folderToSearch, courseFiles, pathsToExclude, AbstractSchemaHandler.COURSE_FILE_EXTENSION);

        if(logger.isInfoEnabled()){
            logger.info("Found " + courseFiles.size() + " course files to parse in "+rootWorkspaceDirectory);
        }

        if(!courseFiles.isEmpty()){

            for (int index = 0; index < courseFiles.size(); index++) {
                FileProxy courseFile = courseFiles.get(index);

                if (logger.isInfoEnabled()) {
                    logger.info("Parsing course file named " + courseFile.getFileId());
                }

                try {
                    // update course to the latest version if it needs to be
                    AbstractFolderProxy courseFolder = rootWorkspaceDirectory.getParentFolder(courseFile);

                    if(progressIndicator != null){
                        progressIndicator.setTaskDescription("Updating course(s) (if needed)");
                        progressIndicator.setPercentComplete((int)((index+1)/(double)courseFiles.size() * 100));
                    }

                    List<String> updatedFiles = AbstractConversionWizardUtil.updateCourseToLatestVersion(courseFile,
                            courseFolder, true);
                    courseOptionsWrapper.upconvertedFiles.addAll(updatedFiles);

                    if(progressIndicator != null){
                        progressIndicator.setPercentComplete(0);
                    }

                    // build domain option
                    buildDomainOption(courseFile, rootWorkspaceDirectory, courseOptionsWrapper, validateLogic,
                            exitOnFirstSchemaError, username, progressIndicator);
                } catch (Throwable e) {
                    logger.error("Caught exception while parsing course file named " + courseFile.getFileId() + ", therefore not adding this course to the list of available courses", e);

                    try {
                        courseOptionsWrapper.parseFailedFiles.add(rootWorkspaceDirectory.getRelativeFileName(courseFile));
                    } catch (Exception e1) {
                        logger.error("Caught exception while retrieving relative file path to the course " + courseFile.getFileId(), e1);
                        courseOptionsWrapper.parseFailedFiles.add(courseFile.getFileId());
                    }
                }
            }
        }
    }
    
    /**
     * This inner class is used to simply wrap a course's interop implementation information which will
     * help determine the gateway module connection requirement for the course.  With the information
     * it should help to determine the following:
     *  1. is a GW connection required to run the course because the course contains at least one non-gateway applications + list of interops
     *  2. is at least 1 GW connection required to run the course because it contains only gateway applications with team assessments + list of interops
     *  3. empty list of interops if none are found in the course
     * @author mhoffman
     *
     */
    public static class InteropImplementationsNeeded{
        
        /** whether a single gateway module connection is allowed for the course that can contain multiple team members (learners) */
        public boolean hostGWAllowed = false;
        
        /** the interop implementation classes found in the course (course.xml and course folder) */
        public List<String> interopImpls = new ArrayList<>();
        
        /** whether a topic needs to be established to handle messages from a log playback service */
        public boolean needsLogPlaybackTopic = false;
    }

    /**
     * This inner class is used to simply wrap objects used to help identify and organize course
     * options for a single course options request.
     *
     * @author mhoffman
     *
     */
    //TODO: this class could be more flushed out with help methods, private class attributes, getters/setters, toString method, etc.
    public static class CourseOptionsWrapper{

        /**
         * metadata for domain options found for this instance of the domain module
         * Key: unique domain id (the relative path to the course.xml)
         * Value: information about that domain
         */
        public Map<String, DomainOption> domainOptions = new HashMap<>();

        /**
         * mapping of unique course file name to the handler for that content.  Can be empty but not null
         * Key: unique domain id (the relative path to the course.xml)
         * Value: the handler that has parsed and possibly validated that domain
         */
        public Map<String, DomainCourseFileHandler> courseFileNameToHandler = new HashMap<>();

        /**
         * map of unique course file name (relative to domain folder) to the file name (absolute file name)
         * that contains the course description and flow
         * The keys are the unique identifiers for each course (this identifier use to be the course name but that
         * prevented courses from having the same name)
         */
        public Map<String,FileProxy> courseFileNameToFileMap = new HashMap<>();

        /**
         * contains the course files that weren't parse-able (i.e. valid XML documents)
         */
        public Set<String> parseFailedFiles = new HashSet<>();

        /**
         * contains the course files that were upconverted to the latest version
         */
        public Set<String> upconvertedFiles = new HashSet<>();
    }
    
    /**
     * Used to cache the various CDT quadrant tagged metadata to only search for it once during course validation
     * @author mhoffman
     *
     */
    private static class MetadataFileCache{
        
        /** The rule phase metadata files of a course */
        private Map<FileProxy, generated.metadata.Metadata> ruleMetadataFiles = null;
        
        /** The example phase metadata files of a course */
        private Map<FileProxy, generated.metadata.Metadata> exampleMetadataFiles = null;
                
        /** The practice phase metadata files of a course */
        private Map<FileProxy, generated.metadata.Metadata> practiceMetadataFiles = null;
        
        /** The remediation only phase metadata files of a course */
        private Map<FileProxy, generated.metadata.Metadata> remediationOnlyMetadataFiles = null;

        /**
         * Return the rule phase metadata files of a course
         * @return cant be null.
         */
        public Map<FileProxy, generated.metadata.Metadata> getRuleMetadataFiles() {
            return ruleMetadataFiles;
        }

        /**
         * Set the rule phase metadata files of a course
         * @param ruleMetadataFiles can be null
         */
        public void setRuleMetadataFiles(Map<FileProxy, generated.metadata.Metadata> ruleMetadataFiles) {
            this.ruleMetadataFiles = ruleMetadataFiles;
        }

        /**
         * Return the example phase metadata files of a course
         * @return cant be null.
         */
        public Map<FileProxy, generated.metadata.Metadata> getExampleMetadataFiles() {
            return exampleMetadataFiles;
        }

        /**
         * Set the example phase metadata files of a course
         * @param ruleMetadataFiles can be null
         */
        public void setExampleMetadataFiles(Map<FileProxy, generated.metadata.Metadata> exampleMetadataFiles) {
            this.exampleMetadataFiles = exampleMetadataFiles;
        }

        /**
         * Return the practice phase metadata files of a course
         * @return cant be null.
         */
        public Map<FileProxy, generated.metadata.Metadata> getPracticeMetadataFiles() {
            return practiceMetadataFiles;
        }

        /**
         * Set the practice phase metadata files of a course
         * @param ruleMetadataFiles can be null
         */
        public void setPracticeMetadataFiles(Map<FileProxy, generated.metadata.Metadata> practiceMetadataFiles) {
            this.practiceMetadataFiles = practiceMetadataFiles;
        }

        /**
         * Return the remediation only phase metadata files of a course
         * @return cant be null.
         */
        public Map<FileProxy, generated.metadata.Metadata> getRemediationOnlyMetadataFiles() {
            return remediationOnlyMetadataFiles;
        }

        /**
         * Set the remediation only phase metadata files of a course
         * @param ruleMetadataFiles can be null
         */
        public void setRemediationOnlyMetadataFiles(Map<FileProxy, generated.metadata.Metadata> remediationOnlyMetadataFiles) {
            this.remediationOnlyMetadataFiles = remediationOnlyMetadataFiles;
        }

    }

    /**
     * Removes the sensitive LTI provider information (key and secret) from the provider.
     */
    public void scrubLtiProviders() {
        if (course != null && course.getLtiProviders() != null) {
            for (LtiProvider provider : course.getLtiProviders().getLtiProvider()) {

                if (BooleanEnum.TRUE.equals(provider.getProtectClientData())) {

                    if (logger.isDebugEnabled()) {
                        logger.debug("scrubbed key and secret from LTI provider: " + provider.getIdentifier());
                    }
                    provider.setKey(LTI_EXPORT_PROVIDER_MESSAGE);
                    provider.setSharedSecret(LTI_EXPORT_PROVIDER_MESSAGE);
                    // removes the field from export file
                    provider.setProtectClientData(null);
                } else if (logger.isDebugEnabled()) {
                    logger.debug("Did not scrub LTI provider '" + provider.getIdentifier() + "' because the author chose to have it be unprotected.");
                }
            }
        }
    }
}
