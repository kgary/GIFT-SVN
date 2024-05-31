/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.Assessments;
import generated.dkf.BooleanEnum;
import generated.dkf.ChildConceptEnded;
import generated.dkf.CompetenceMetric;
import generated.dkf.ConceptAssessment;
import generated.dkf.ConceptEnded;
import generated.dkf.Conditions;
import generated.dkf.ConfidenceMetric;
import generated.dkf.Coordinate;
import generated.dkf.EndTriggers;
import generated.dkf.EntityLocation;
import generated.dkf.EnvironmentAdaptation;
import generated.dkf.Feedback;
import generated.dkf.LearnerAction;
import generated.dkf.LearnerActionReference;
import generated.dkf.Media;
import generated.dkf.PerformanceMetric;
import generated.dkf.PlacesOfInterest;
import generated.dkf.PointRef;
import generated.dkf.PriorityMetric;
import generated.dkf.ScenarioStarted;
import generated.dkf.Segment;
import generated.dkf.StartLocation;
import generated.dkf.StartTriggers;
import generated.dkf.StrategyApplied;
import generated.dkf.TaskEnded;
import generated.dkf.TrendMetric;
import generated.dkf.TriggerLocation;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.coordinate.AGL;
import mil.arl.gift.common.coordinate.AbstractCoordinate;
import mil.arl.gift.common.coordinate.GCC;
import mil.arl.gift.common.coordinate.GDC;
import mil.arl.gift.common.course.AbstractActionKnowledge;
import mil.arl.gift.common.course.CourseConceptsUtil;
import mil.arl.gift.common.course.GIFTValidationResults;
import mil.arl.gift.common.course.dkf.AbstractDKFHandler;
import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.DKFValidationException;
import mil.arl.gift.common.course.dkf.DomainActionKnowledge;
import mil.arl.gift.common.course.dkf.session.Mission;
import mil.arl.gift.common.course.dkf.strategy.AbstractDKFStrategy;
import mil.arl.gift.common.course.dkf.strategy.PerformanceAssessmentStrategy;
import mil.arl.gift.common.course.dkf.team.AbstractTeamUnit;
import mil.arl.gift.common.course.dkf.team.LocatedTeamMember;
import mil.arl.gift.common.course.dkf.team.MarkedTeamMember;
import mil.arl.gift.common.course.dkf.team.Team;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.course.dkf.team.TeamOrganization;
import mil.arl.gift.common.course.strategy.AbstractStrategy;
import mil.arl.gift.common.course.strategy.InstructionalInterventionStrategy;
import mil.arl.gift.common.course.strategy.MidLessonMediaStrategy;
import mil.arl.gift.common.course.strategy.ScenarioAdaptationStrategy;
import mil.arl.gift.common.course.strategy.StrategySet;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.EchelonEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.io.TimeUtil;
import mil.arl.gift.common.io.UriUtil;
import mil.arl.gift.common.io.UriUtil.InternetConnectionStatusEnum;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.domain.knowledge.Concept;
import mil.arl.gift.domain.knowledge.DomainAssessmentKnowledge;
import mil.arl.gift.domain.knowledge.IntermediateConcept;
import mil.arl.gift.domain.knowledge.Resources;
import mil.arl.gift.domain.knowledge.Scenario;
import mil.arl.gift.domain.knowledge.Task;
import mil.arl.gift.domain.knowledge.common.AbstractLessonAssessment;
import mil.arl.gift.domain.knowledge.common.AbstractPerformanceAssessmentNode;
import mil.arl.gift.domain.knowledge.common.AbstractTrigger;
import mil.arl.gift.domain.knowledge.common.Area;
import mil.arl.gift.domain.knowledge.common.ConceptAssessmentTrigger;
import mil.arl.gift.domain.knowledge.common.ConceptEndedTrigger;
import mil.arl.gift.domain.knowledge.common.ConditionLessonAssessment;
import mil.arl.gift.domain.knowledge.common.EntityLocationTrigger;
import mil.arl.gift.domain.knowledge.common.GIFTSurveyLessonAssessment;
import mil.arl.gift.domain.knowledge.common.LearnerActionTrigger;
import mil.arl.gift.domain.knowledge.common.Path;
import mil.arl.gift.domain.knowledge.common.PlaceOfInterestInterface;
import mil.arl.gift.domain.knowledge.common.PlacesOfInterestManager;
import mil.arl.gift.domain.knowledge.common.Point;
import mil.arl.gift.domain.knowledge.common.ScenarioStartTrigger;
import mil.arl.gift.domain.knowledge.common.StrategyAppliedTrigger;
import mil.arl.gift.domain.knowledge.common.TaskEndedTrigger;
import mil.arl.gift.domain.knowledge.common.metric.assessment.PerformanceMetricInterface;
import mil.arl.gift.domain.knowledge.common.metric.competence.CompetenceMetricInterface;
import mil.arl.gift.domain.knowledge.common.metric.confidence.ConfidenceMetricInterface;
import mil.arl.gift.domain.knowledge.common.metric.grade.DefaultGradeMetric;
import mil.arl.gift.domain.knowledge.common.metric.priority.PriorityMetricInterface;
import mil.arl.gift.domain.knowledge.common.metric.trend.TrendMetricInterface;
import mil.arl.gift.domain.knowledge.condition.AbstractCondition;
import mil.arl.gift.domain.knowledge.condition.simile.SIMILEInterfaceCondition;
import mil.arl.gift.domain.knowledge.conversation.AutoTutorModel;
import mil.arl.gift.domain.knowledge.conversation.ConversationTreeFileHandler;
import mil.arl.gift.domain.knowledge.strategy.StrategyHandlerInterface;
import mil.arl.gift.domain.lessonmaterial.LessonMaterialFileHandler;

/**
 * This dkf handler is used by the domain module to take dkf content and
 * populate domain classes with the knowledge needed to executed on a domain.
 *
 * @author mhoffman
 */
public class DomainDKFHandler extends AbstractDKFHandler {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(DomainDKFHandler.class);

    /**
     * the set of condition classes that require a running training application to complete their assessment
     */
    private static final Set<Class<?>> conditionsClassesRequiringTrainingApp;
    static{
        conditionsClassesRequiringTrainingApp = new HashSet<>();        
        conditionsClassesRequiringTrainingApp.add(mil.arl.gift.domain.knowledge.condition.IdentifyPOIsCondition.class);
    }
    
    /** 
     * contains the dkf scenario object as domain package classes used to track 
     * assessments across tasks and concepts 
     */
    private DomainAssessmentKnowledge domainAssessmentKnowledge;

    /** map of used node ids to the task/concept generated object  */
    private Map<Integer, Serializable> nodeIds = new HashMap<Integer, Serializable>();

    /** collection of performance assessment node names used */
    private List<String> nodeNames = new ArrayList<>();
    
    /** collection of team unit names used */
    private List<String> teamNames = new ArrayList<>();

    /** used to organize places of interest which can be referenced by name in various parts of the DKF */
    private PlacesOfInterestManager placesOfInterestMgr = null;

    /** the course folder contains all the assets for a course include files used by this DKF.
     * This can be a desktop folder or server folder (i.e. in a content management system like Nuxeo) depending
     * on the logic that is using this class (e.g. pre domain session validation = server folder, domain session = desktop folder) */
    private AbstractFolderProxy courseFolder;
    
    /**
     * the folder where the output for a domain session instance is being written too. This could
     * include domain session log, video files, sensor data.
     * Will be null if not in a domain session, i.e. validating a DKF could be done outside of a domain session.
     */
    private DesktopFolderProxy outputFolder;

    /** the course folder relative file name of the DKF */
    private String dkfilename;

    /** Whether or not the domainAssessmentKnowledge has been built */
    private boolean builtDomainAssessmentKnowledge = false;
    
    /** whether to skip loading/checking files that this DKF references (e.g. learner actions xml, conversation tree xml) 
     *  This is useful when the DKF is used for Game Master playback and not during course execution.
     */
    private boolean skipExternalFileLoading = false;

    /** date formatter for time stamps that don't care about the date */
    public static TimeUtil.ConcurrentDateFormatAccess atTime_df;

    static {
        //NOTE: set the time zone to GMT since getTime uses that time zone
        atTime_df = new TimeUtil.ConcurrentDateFormatAccess("HH:mm:ss", TimeZone.getTimeZone("GMT"));
    }

    /**
     * Class constructor - handle the dkf by processing the generated class's
     * objects into domain specific implementation.<br/>
     * NOTE: the DKF file must be of the same version as this GIFT instance schema version.  If not an exception will be thrown. 
     * Use AbstractLegacySchemaHandler.getUnmarshalledFile for unknown or older schema version DKF file support, possibly saving
     * the original file as a .bak, saving the upconverted jaxb object to the original file and using the original file here.
     *
     * @param dkfFile - the dkf to parse.
     * @param courseFolder - the course folder of the course using this DKF.  Can't be null.
     * @param outputFolder the folder where the output for a domain session instance is being written too. This could
     * include domain session log, video files, sensor data.
     * Will be null if not in a domain session, i.e. validating a DKF could be done outside of a domain session.
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @throws FileValidationException Thrown when there is a problem validating the DKF against the schema
     */
    public DomainDKFHandler(FileProxy dkfFile, AbstractFolderProxy courseFolder,
            DesktopFolderProxy outputFolder,
            boolean failOnFirstSchemaError)
            throws FileValidationException {
        super(dkfFile, DKF_SCHEMA_FILE, failOnFirstSchemaError);

        if(courseFolder == null){
            throw new IllegalArgumentException("The course folder can't be null.");
        }

        this.courseFolder = courseFolder;
        
        this.outputFolder = outputFolder;

        try {
            this.dkfilename = courseFolder.getRelativeFileName(dkfFile);

            placesOfInterestMgr = gatherPlacesOfInterest();

        } catch (Exception e) {
            throw new DKFValidationException("There was a problem while trying to parse the dkf '"+dkfFile.getName()+"'.", "The error reads : "+e.getMessage(), dkfFile.getFileId(), e);
        }
    }
    
    /**
     * Set whether to skip loading/checking files that this DKF references (e.g. learner actions xml, conversation tree xml) 
     *  This is useful when the DKF is used for Game Master playback and not during course execution.
     * @param shouldSkip the value to use
     */
    public void shouldSkipExternalFileLoading(boolean shouldSkip){
        this.skipExternalFileLoading = shouldSkip;
    }

    /**
     * Check the DKF against gift validation logic.
     *
     * @param additionalValidation contains additional validation checks that need to be performed.  Can be null
     * @param taType the enumerated type of training application this DKF is for.  Can be null if the type is not known, the DKF
     * is for a conversation,  the DKF is being validated by itself in no given context.
     * @return validation results
     */
    public GIFTValidationResults checkDKF(AbstractAdditionalValidationSettings additionalValidation, TrainingApplicationEnum taType){

        GIFTValidationResults validationResults = new GIFTValidationResults();

        try{
            //validate assessment knowledge
            buildDomainAssessmentKnowledge(courseFolder);

            //
            //validate action knowledge
            //
            AbstractActionKnowledge dak = getDomainActionKnowledge();

            InternetConnectionStatusEnum connectionStatus = UriUtil.getInternetStatus();

            // GIFT only supports 1 character per real-time assessment,
            // use this to track the characters found (if any)
            String foundCharacter = null;

            //check each strategy
            Set<String> highlightNames = new HashSet<>();
            Map<String, List<String>> removeHighlightNameToStrategyNames = new HashMap<>();
            for(String strategyName : dak.getStrageyNames()){

                StrategySet strategySet = dak.getStrategyActivities(strategyName);
                if(strategySet == null) {
                    continue;
                }
                for (AbstractStrategy strategy : strategySet.getStrategies()) {
                    if(strategy != null){

                        // has a valid handler
                        StrategyHandlerInterface handlerInterface = AbstractPedagogicalRequestHandler.getHandler(((AbstractDKFStrategy)strategy).getHandlerInfo().getImpl());
                        if(handlerInterface == null){
                            validationResults.setCriticalIssue(new DKFValidationException("There was a problem retrieving the strategy handler interface.",
                                    "Is the interface implementation class of "+((AbstractDKFStrategy)strategy).getHandlerInfo().getImpl()+" missing the instance class attribute?",
                                    dkfilename,
                                    null));
                            return validationResults;
                        }

                        // has valid parameters
                        if (strategy instanceof InstructionalInterventionStrategy) {

                            InstructionalInterventionStrategy iiStrategy = (InstructionalInterventionStrategy) strategy;

                            Feedback feedbackTactic = iiStrategy.getFeedback();
                            Serializable feedbackPresentation = feedbackTactic.getFeedbackPresentation();
                            if (feedbackPresentation instanceof generated.dkf.MediaSemantics) {
                                // check the avatar information

                                generated.dkf.MediaSemantics mediaSemantics = (generated.dkf.MediaSemantics) feedbackPresentation;
                                String filename = mediaSemantics.getAvatar();

                                if (!courseFolder.fileExists(filename)) {
                                    validationResults.setCriticalIssue(new DetailedException(
                                            "Found an incomplete instructional strategy.",
                                            "The Media Semantics character file of '" + filename
                                                    + "' doesn't exist but is referenced in a instructional intervention strategy called '"+iiStrategy.getName()+"' in the DKF '"
                                                    + file.getName() + "'.",
                                            null));
                                    return validationResults;
                                }

                                if (filename != null && !filename.isEmpty()) {

                                    if (foundCharacter != null && !foundCharacter.equalsIgnoreCase(filename)) {
                                        String errorMsg = "GIFT only supports a single character per real time assessment but more than one character was found in '"
                                                + dkfilename + "'.\n";
                                        if (foundCharacter.isEmpty()) {
                                            errorMsg += "1. the GIFT default character.";
                                        } else {
                                            errorMsg += "1. '" + foundCharacter + "'.";
                                        }

                                        errorMsg += "\n2. '" + filename + "'.";

                                        validationResults.setCriticalIssue(new DKFValidationException(
                                                "Found more than one character defined in the Real-time assessment file.",
                                                errorMsg, dkfilename, null));
                                        return validationResults;
                                    }

                                    foundCharacter = filename;
                                }
                            } else if (feedbackPresentation instanceof generated.dkf.Message) {
                                // check if the feedback is to be presented in the
                                // TUI (not the training app)

                                generated.dkf.Message message = (generated.dkf.Message) feedbackPresentation;
                                generated.dkf.Message.Delivery delivery = message.getDelivery();
                                if (delivery != null) {

                                    if (delivery.getInTutor() != null) {

                                        if (foundCharacter != null && !foundCharacter.equals(Constants.EMPTY)) {
                                            // the feedback is to be presented in
                                            // the TUI and a custom character was
                                            // defined elsewhere in the DKF
                                            // but this feedback would be using the
                                            // default GIFT character not the custom
                                            // character

                                            validationResults.setCriticalIssue(new DKFValidationException(
                                                    "Found that more than one character could be used in the Real-time assessment.",
                                                    "GIFT only supports a single character per real time assessment but more than one character could be used when analyzing '"
                                                            + dkfilename
                                                            + "'.\n1. the GIFT default character\n2. Custom Defined Character: "
                                                            + foundCharacter,
                                                    dkfilename, null));
                                            return validationResults;
                                        } else {
                                            // use empty string as an indicator of
                                            // the gift default character
                                            foundCharacter = Constants.EMPTY;
                                        }
                                    }
                                }
                                
                                if(message.getDisplaySessionProperties() != null 
                                        && BooleanEnum.TRUE.equals(message.getDisplaySessionProperties().getRequestUsingSessionState())
                                        && StringUtils.isBlank(DomainModuleProperties.getInstance().getExternalStrategyProviderUrl())) {
                                    
                                    validationResults.setCriticalIssue(new DKFValidationException(
                                            "The feedback strategy '"+iiStrategy.getName()+"' is attempting to request feedback from an external strategy provider but GIFT "
                                                    + "currently has this feature disabled, as is the default.",
                                            "To enable this feature, you can edit the GIFT/config/common.properties file to remove the # before the ExternalStrategyProviderUrl property "
                                            + "and then restart GIFT. "
                                            + "Note that any courses uing this URL will expect it to be reachable when they display the feedback strategy. If you do not "
                                            + "wish to use an external strategy provider, you can alternatively modify the strategy to no longer make this request.",
                                            dkfilename, null));
                                }
                            }
                            
                            //check any team organization references
                            if(!feedbackTactic.getTeamRef().isEmpty()){
                                
                                Team rootTeam = getDomainAssessmentKnowledge().getScenario().getRootTeam();
                                if(rootTeam == null || rootTeam.getNumberOfTeamMembers() == 0){
                                    validationResults.setCriticalIssue(new DKFValidationException(
                                            "The feedback strategy '"+iiStrategy.getName()+"' references one or more team organization members that have not been defined.",
                                            "You must author a team organization in the real time assessment before attempting to assign feedback to a particular individual or group of individuals.",
                                            dkfilename, null));
                                }else{
                                
                                    for(generated.dkf.TeamRef teamRef : feedbackTactic.getTeamRef()){
                                        
                                        String teamElementName = teamRef.getValue();
                                        AbstractTeamUnit foundTeamUnit = rootTeam.getTeamElement(teamElementName);
                                        if(foundTeamUnit == null){
                                            validationResults.setCriticalIssue(new DKFValidationException(
                                                    "The feedback strategy '"+iiStrategy.getName()+"' references a team organization member of "+teamElementName+" that doesn't exist.",
                                                    "The team organization element named '"+teamElementName+"' can't be found in the authored team organization in the real time assessment.",
                                                    dkfilename, null));
                                            break;
                                        }else if(foundTeamUnit instanceof Team && ((Team)foundTeamUnit).getNumberOfTeamMembers() == 0){
                                            validationResults.setCriticalIssue(new DKFValidationException(
                                                    "The feedback strategy '"+iiStrategy.getName()+"' references a team that contains no team members.",
                                                    "The team named '"+teamElementName+"' was found but contains no team members.  This means the strategy will not be applied to any learners.  Please add team members under this team.",
                                                    dkfilename, null));
                                            break;
                                        }
                                    }
                                }
                            }
                        }else if(strategy instanceof MidLessonMediaStrategy){

                            MidLessonMediaStrategy iiStrategy = (MidLessonMediaStrategy)strategy;

                            for(Media m : iiStrategy.getMediaList().getMedia()){

                                try{
                                    LessonMaterialFileHandler.validateMedia(m, courseFolder, connectionStatus);
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
                                            "The lesson material in the strategy named '"+strategyName+"' failed media validation because '"+m.getName()+"' threw an error: " +exception.getMessage(), exception));
                                    }
                                }
                                
                                if(m.getDisplaySessionProperties() != null 
                                        && BooleanEnum.TRUE.equals(m.getDisplaySessionProperties().getRequestUsingSessionState())
                                        && StringUtils.isBlank(DomainModuleProperties.getInstance().getExternalStrategyProviderUrl())) {
                                    
                                    validationResults.setCriticalIssue(new DKFValidationException(
                                            "The media strategy '"+iiStrategy.getName()+"' is attempting to request media from an external strategy provider but GIFT "
                                                    + "currently has this feature disabled, as is the default.",
                                            "To enable this feature, you can edit the GIFT/config/common.properties file to remove the # before the ExternalStrategyProviderUrl property "
                                            + "and then restart GIFT. "
                                            + "Note that any courses uing this URL will expect it to be reachable when they display the feedback strategy. If you do not "
                                            + "wish to use an external strategy provider, you can alternatively modify the strategy to no longer make this request.",
                                            dkfilename, null));
                                }
                            }

                        }else if(strategy instanceof PerformanceAssessmentStrategy){

                            PerformanceAssessmentStrategy paStrategy = (PerformanceAssessmentStrategy)strategy;

                            Serializable assessmentType = paStrategy.getAssessmentType();
                            if(assessmentType instanceof generated.dkf.Conversation){
                                //check conversation choice

                                generated.dkf.Conversation conversation = (generated.dkf.Conversation)assessmentType;

                                if(conversation.getType() instanceof generated.dkf.AutoTutorSKO){
                                    generated.dkf.AutoTutorSKO atSKO = (generated.dkf.AutoTutorSKO)conversation.getType();
                                    DomainDKFHandler.checkAutoTutorReference(atSKO, courseFolder);

                                }else if(conversation.getType() instanceof String){
                                    //conversation tree conversation type

                                    String conversationFileName = (String) conversation.getType();

                                    try{
                                        FileProxy conversationFile = courseFolder.getRelativeFile(conversationFileName);
                                        ConversationTreeFileHandler tree = new ConversationTreeFileHandler(conversationFile, true);
                                        GIFTValidationResults treeValidationResults = tree.checkConversation();
                                        if(treeValidationResults.hasCriticalIssue() || treeValidationResults.hasImportantIssues()){
                                            throw treeValidationResults.getFirstError();
                                        }
                                    }catch(IOException e){
                                        throw new DetailedException("Failed to parse the conversation file.", "There was a problem with the conversation file '"+conversationFileName+"'.  The message reads:\n"+e.getMessage(), e);
                                    }catch(FileValidationException e){
                                        throw new DetailedException("Failed to parse and validate the conversation file.", "There was a problem when parsing and validating the conversation file '"+conversationFileName+"'.  The message reads:\n"+e.getMessage(), e);
                                    }catch(DetailedException e){
                                        throw e;
                                    }catch(Throwable e){
                                        throw new DetailedException("Failed to parse and validate the conversation file.", "There was a problem when parsing and validating the conversation file '"+conversationFileName+"'.  The message reads:\n"+e.getMessage(), e);
                                    }
                                }
                            }
                        } else if (strategy instanceof ScenarioAdaptationStrategy) {
                            ScenarioAdaptationStrategy scenarioAdaptationStrategy = (ScenarioAdaptationStrategy) strategy;
                            EnvironmentAdaptation adaptation = scenarioAdaptationStrategy.getType();

                            if(adaptation.getType() == null){
                                throw new DetailedException("There is no environment adaptation defined for the scenario adaptation "+scenarioAdaptationStrategy.getName(), "A scenario adaptation strategy can't be empty.", null);
                            }
                            
                            if(adaptation.getType() instanceof generated.dkf.EnvironmentAdaptation.Teleport){
                                
                                generated.dkf.EnvironmentAdaptation.Teleport teleport = (generated.dkf.EnvironmentAdaptation.Teleport)adaptation.getType();
                           
                                //check any team organization references
                                if(teleport.getTeamMemberRef() != null){
                                    
                                    Team rootTeam = getDomainAssessmentKnowledge().getScenario().getRootTeam();
                                    if(rootTeam == null || rootTeam.getNumberOfTeamMembers() == 0){
                                        validationResults.setCriticalIssue(new DKFValidationException(
                                                "The scenario adaptation strategy '"+scenarioAdaptationStrategy.getName()+"' references a team organization member of "+teleport.getTeamMemberRef().getValue()+" that has not been defined.",
                                                "You must author a team organization in the real time assessment before attempting to assign feedback to a particular individual or group of individuals.",
                                                dkfilename, null));
                                    }else{                                    
                                            
                                        String teamElementName = teleport.getTeamMemberRef().getValue();
                                        AbstractTeamUnit foundTeamUnit = rootTeam.getTeamElement(teamElementName);
                                        if(foundTeamUnit == null){
                                            validationResults.setCriticalIssue(new DKFValidationException(
                                                    "The scenario adaptation strategy '"+scenarioAdaptationStrategy.getName()+"' references a team organization member of "+teamElementName+" that doesn't exist.",
                                                    "The team organization element named '"+teamElementName+"' can't be found in the authored team organization in the real time assessment.",
                                                    dkfilename, null));
                                        }else if(foundTeamUnit instanceof Team && ((Team)foundTeamUnit).getNumberOfTeamMembers() == 0){
                                            validationResults.setCriticalIssue(new DKFValidationException(
                                                    "The scenario adaptation strategy '"+scenarioAdaptationStrategy.getName()+"' references a team that contains no team members.",
                                                    "The team named '"+teamElementName+"' was found but contains no team members.  This means the strategy will not be applied to any learners.  Please add team members under this team.",
                                                    dkfilename, null));
                                        }

                                    }
                                }
                            }else if(adaptation.getType() instanceof generated.dkf.EnvironmentAdaptation.FatigueRecovery){
                                
                                generated.dkf.EnvironmentAdaptation.FatigueRecovery fatigue = (generated.dkf.EnvironmentAdaptation.FatigueRecovery)adaptation.getType();
                                
                                //check any team organization references
                                if(fatigue.getTeamMemberRef() != null){
                                    
                                    Team rootTeam = getDomainAssessmentKnowledge().getScenario().getRootTeam();
                                    if(rootTeam == null || rootTeam.getNumberOfTeamMembers() == 0){
                                        validationResults.setCriticalIssue(new DKFValidationException(
                                                "The scenario adaptation strategy '"+scenarioAdaptationStrategy.getName()+"' references a team organization member of "+fatigue.getTeamMemberRef().getValue()+" that has not been defined.",
                                                "You must author a team organization in the real time assessment before attempting to assign feedback to a particular individual or group of individuals.",
                                                dkfilename, null));
                                    }else{                                    
                                            
                                        String teamElementName = fatigue.getTeamMemberRef().getValue();
                                        AbstractTeamUnit foundTeamUnit = rootTeam.getTeamElement(teamElementName);
                                        if(foundTeamUnit == null){
                                            validationResults.setCriticalIssue(new DKFValidationException(
                                                    "The scenario adaptation strategy '"+scenarioAdaptationStrategy.getName()+"' references a team organization member of "+teamElementName+" that doesn't exist.",
                                                    "The team organization element named '"+teamElementName+"' can't be found in the authored team organization in the real time assessment.",
                                                    dkfilename, null));
                                        }else if(foundTeamUnit instanceof Team && ((Team)foundTeamUnit).getNumberOfTeamMembers() == 0){
                                            validationResults.setCriticalIssue(new DKFValidationException(
                                                    "The scenario adaptation strategy '"+scenarioAdaptationStrategy.getName()+"' references a team that contains no team members.",
                                                    "The team named '"+teamElementName+"' was found but contains no team members.  This means the strategy will not be applied to any learners.  Please add team members under this team.",
                                                    dkfilename, null));
                                        }

                                    }
                                }
                            }else if(adaptation.getType() instanceof generated.dkf.EnvironmentAdaptation.Endurance){
                                
                                generated.dkf.EnvironmentAdaptation.Endurance endurance = (generated.dkf.EnvironmentAdaptation.Endurance)adaptation.getType();
                                
                                //check any team organization references
                                if(endurance.getTeamMemberRef() != null){
                                    
                                    Team rootTeam = getDomainAssessmentKnowledge().getScenario().getRootTeam();
                                    if(rootTeam == null || rootTeam.getNumberOfTeamMembers() == 0){
                                        validationResults.setCriticalIssue(new DKFValidationException(
                                                "The scenario adaptation strategy '"+scenarioAdaptationStrategy.getName()+"' references a team organization member of "+endurance.getTeamMemberRef()+" that has not been defined.",
                                                "You must author a team organization in the real time assessment before attempting to assign feedback to a particular individual or group of individuals.",
                                                dkfilename, null));
                                    }else{                                    
                                            
                                        String teamElementName = endurance.getTeamMemberRef().getValue();
                                        AbstractTeamUnit foundTeamUnit = rootTeam.getTeamElement(teamElementName);
                                        if(foundTeamUnit == null){
                                            validationResults.setCriticalIssue(new DKFValidationException(
                                                    "The scenario adaptation strategy '"+scenarioAdaptationStrategy.getName()+"' references a team organization member of "+endurance.getTeamMemberRef()+" that doesn't exist.",
                                                    "The team organization element named '"+teamElementName+"' can't be found in the authored team organization in the real time assessment.",
                                                    dkfilename, null));
                                        }else if(foundTeamUnit instanceof Team && ((Team)foundTeamUnit).getNumberOfTeamMembers() == 0){
                                            validationResults.setCriticalIssue(new DKFValidationException(
                                                    "The scenario adaptation strategy '"+scenarioAdaptationStrategy.getName()+"' references a team that contains no team members.",
                                                    "The team named '"+teamElementName+"' was found but contains no team members.  This means the strategy will not be applied to any learners.  Please add team members under this team.",
                                                    dkfilename, null));
                                        }

                                    }
                                }
                            }else if(adaptation.getType() instanceof generated.dkf.EnvironmentAdaptation.HighlightObjects){
                                // collect the highlight names in case there is a remove highlight action
                                
                                generated.dkf.EnvironmentAdaptation.HighlightObjects highlight = (generated.dkf.EnvironmentAdaptation.HighlightObjects)adaptation.getType();
                                
                                if(!StringUtils.isAlphaNumeric(highlight.getName())){
                                    validationResults.setCriticalIssue(new DKFValidationException(
                                            "The scenario adaptation strategy '"+scenarioAdaptationStrategy.getName()+"' has an invalid highlight object name.",
                                            "The highlight named '"+highlight.getName()+"' can only contain letters or numbers.",
                                            dkfilename, null));
                                }
                                highlightNames.add(highlight.getName());
                                
                            }else if(adaptation.getType() instanceof generated.dkf.EnvironmentAdaptation.RemoveHighlightOnObjects){
                                // collect the remove highlight names to compare after collecting all highlight names
                                
                                generated.dkf.EnvironmentAdaptation.RemoveHighlightOnObjects remove = (generated.dkf.EnvironmentAdaptation.RemoveHighlightOnObjects)adaptation.getType();
                                
                                if(!StringUtils.isAlphaNumeric(remove.getHighlightName())){
                                    validationResults.setCriticalIssue(new DKFValidationException(
                                            "The scenario adaptation strategy '"+scenarioAdaptationStrategy.getName()+"' has an invalid highlight object name.",
                                            "The highlight named '"+remove.getHighlightName()+"' can only contain letters or numbers.",
                                            dkfilename, null));
                                }
                                
                                List<String> strategyNames = removeHighlightNameToStrategyNames.get(remove.getHighlightName());
                                if(strategyNames == null){
                                    strategyNames = new ArrayList<>();
                                    removeHighlightNameToStrategyNames.put(remove.getHighlightName(), strategyNames);
                                }
                                
                                strategyNames.add(scenarioAdaptationStrategy.getName());
                            }
                        }
                    }
                }
            }//end for on strategies
            
            // Check remove highlight name references
            for(String highlightNameToRemove : removeHighlightNameToStrategyNames.keySet()){
                
                if(!highlightNames.contains(highlightNameToRemove)){
                    validationResults.setCriticalIssue(new DKFValidationException(
                            "The scenario adaptation strategies {"+StringUtils.join(", ", removeHighlightNameToStrategyNames.get(highlightNameToRemove))+
                                "} references a highlight name that doesn't exist.",
                            "The highlight named '"+highlightNameToRemove+"' was not found as a highlight that can be created.",
                            dkfilename, null));
                }
            }

        } catch (DKFValidationException dkfException){
            throw dkfException;
        } catch (DetailedException e){
            throw new DKFValidationException("Failed to parse and validate the dkf '"+dkfilename+"' because "+e.getReason(), e.getDetails(), dkfilename, e);
        } catch (Exception e) {
            throw new DKFValidationException("There was a problem while trying to parse the dkf '"+dkfilename+"'.", "The following error happened while validating the DKF\n"+e.toString(), dkfilename, e);
        }

        /* Validate on possible excluded conditions. Throws
         * DKFValidationException if an excluded condition is found. */
        for (Task task : getDomainAssessmentKnowledge().getScenario().getTasks()) {
            for (Concept concept : task.getConcepts()) {
                checkExcludedConceptConditions(concept);
            }
        }
        
        // Check that the total performance metrics args optionally defined at any level don't exceed 1.0
        for (Task task : getDomainAssessmentKnowledge().getScenario().getTasks()) {            
            checkConceptAssessmentRollUpArgs(task.getName(), task.getConcepts());
        }

        //
        // Apply additional validation
        //
        if(additionalValidation != null && additionalValidation instanceof AdditionalDKFValidationSettings){

            AdditionalDKFValidationSettings validationSettings = (AdditionalDKFValidationSettings)additionalValidation;

            if(validationSettings.getNodeNamesToMatch() != null && !validationSettings.getNodeNamesToMatch().isEmpty()){

                AbstractPerformanceAssessmentNode matchedNode;

                for(String nodeToMatch : validationSettings.getNodeNamesToMatch()){

                    //reset
                    matchedNode = null;

                    for(AbstractPerformanceAssessmentNode node : getDomainAssessmentKnowledge().getScenario().getPerformanceNodes().values()){

                        if(nodeToMatch.equalsIgnoreCase(node.getName())){
                            matchedNode = node;
                            break;
                        }

                    }

                    if(matchedNode == null){
                        //ERROR - unable to find the node of the specified name
                        throw new DKFValidationException("The DKF failed additional validation checks.", "The DKF performance assessment node name '"+nodeToMatch+"' is not in the DKF "+dkfilename+
                                ".  This is required as a parameter for additional validation because '"+validationSettings.getReason()+"'.", dkfilename, null);
                    }

                    if(validationSettings.shouldMatchedNamesNeedScoring() && !matchedNode.hasScoringRules()){
                        throw new DKFValidationException("The DKF failed additional validation checks.", "The DKF performance assessment node name '"+nodeToMatch+"' doesn't have any scoring rules associated with it's descendant(s).  "+
                                "This is required as a parameter for additional validation because '"+validationSettings.getReason()+"'.", dkfilename, null);
                    }
                }
            }
        }

        return validationResults;
    }
    
    /**
     * Check whether the concepts have appropriate assessment roll up rules defined.  Specifically, if
     * this optional parameter is specified that it is specified for all concepts and the total weights
     * must be equal to zero (within a small threshold due to double precision).
     * @param parentName the name of the parent to these concepts.  Used in exception message.
     * @param concepts the concepts to check the assessment roll up rules for.
     * @throws DKFValidationException if the assessment roll up rules are not valid.
     */
    private void checkConceptAssessmentRollUpArgs(String parentName, List<Concept> concepts) throws DKFValidationException{
        
        double childrenTotal = 0.0;
        boolean childrenHasArgs = false, isFirstChild = true; 
        for (Concept concept : concepts) {
            
            generated.dkf.PerformanceMetricArguments args = concept.getPerformanceArguments();
            if(args != null){
                // this child concept has an arg defined
                
                if(!isFirstChild && !childrenHasArgs){
                    // this is not the first child, and previous children had no args
                    // This is not supported, either all children have args or none do
                    throw new DKFValidationException(
                            "Found an incomplete set of roll up assessment weights defined for the child concepts of the parent '"+parentName+"'.",
                            "The concept '"+concept.getName()+"' has a roll up assessment weight but one or more other sibling concepts have no weight defined.",
                            dkfilename,
                            null);
                }
                childrenHasArgs = true;
                childrenTotal += args.getWeight();
            }else if(childrenHasArgs){
                // a previous child had args but this child doesn't
                // This is not supported, either all children have args or none do
                throw new DKFValidationException(
                        "Found an incomplete set of roll up assessment weights defined for the child concepts of the parent '"+parentName+"'.",
                        "The concept '"+concept.getName()+"' does NOT have a roll up assessment weight but the other sibling concepts have weights defined.",
                        dkfilename,
                        null);
            }else{
                // this child concept has NO arg defined AND no previous child had the arg defined
                // Nothing to do.
            }
            
            isFirstChild = false;
            
            if(concept instanceof IntermediateConcept){
                // check nested concepts
                checkConceptAssessmentRollUpArgs(concept.getName(), ((IntermediateConcept)concept).getConcepts());
            }else{
                // check condition under the concept
                checkConditionAssessmentRollUpArgs(concept.getName(), concept.getConditions());
            }
        }
        
        // check within threshold window - only 2 decimal precision is authorable
        if(childrenHasArgs && (childrenTotal < 0.999 || childrenTotal > 1.001)){
            throw new DKFValidationException(
                    "Found a total roll up assessment weight of "+childrenTotal+" defined for the child concepts of the parent '"+parentName+"'.",
                    "The total weights must be equal to 1.0.",
                    dkfilename,
                    null);
        }
    }
    
    /**
     * Check whether the condition have appropriate assessment roll up rules defined.  Specifically, if
     * this optional parameter is specified that it is specified for all condition and the total weights
     * must be equal to zero (within a small threshold due to double precision).
     * @param parentName the name of the parent to these concepts.  Used in exception message.
     * @param conditions the conditions to check the assessment roll up rules for.
     * @throws DKFValidationException if the assessment roll up rules are not valid.
     */
    private void checkConditionAssessmentRollUpArgs(String parentName, Set<AbstractCondition> conditions) throws DKFValidationException{
        
        double childrenTotal = 0.0;
        boolean childrenHasArgs = false, isFirstChild = true; 
        for (AbstractCondition condition : conditions) {
            
            generated.dkf.PerformanceMetricArguments args = condition.getPerformanceArguments();
            if(args != null){
                // this child concept has an arg defined
                
                if(!isFirstChild && !childrenHasArgs){
                    // this is not the first child, and previous children had no args
                    // This is not supported, either all children have args or none do
                    throw new DKFValidationException(
                            "Found an incomplete set of roll up assessment weights defined for the child conditions of the parent concept '"+parentName+"'.",
                            "The condition '"+condition.getClass()+"' has a roll up assessment weight but one or more other sibling conditions have no weight defined.",
                            dkfilename,
                            null);
                }
                childrenHasArgs = true;
                childrenTotal += args.getWeight();
            }else if(childrenHasArgs){
                // a previous child had args but this child doesn't
                // This is not supported, either all children have args or none do
                throw new DKFValidationException(
                        "Found an incomplete set of roll up assessment weights defined for the child conditions of the parent concept '"+parentName+"'.",
                        "The condition '"+condition.getClass()+"' does NOT have a roll up assessment weight but the other sibling conditions have weights defined.",
                        dkfilename,
                        null);
            }else{
                // this child concept has NO arg defined AND no previous child had the arg defined
                // Nothing to do.
            }
            
            isFirstChild = false;

        }
        
        // check within threshold window - only 2 decimal precision is authorable
        if(childrenHasArgs && (childrenTotal < 0.999 || childrenTotal > 1.001)){
            throw new DKFValidationException(
                    "Found a total roll up assessment weight of "+childrenTotal+" defined for the child conditions of the parent concept '"+parentName+"'.",
                    "The total weights must be equal to 1.0.",
                    dkfilename,
                    null);
        }
    }

    /**
     * Checks if the concept contains any excluded conditions.
     * 
     * @param concept the concept to check.
     * @throws DKFValidationException thrown if any of the contained conditions
     *         are excluded for any reason (e.g. not supported on a 64-bit JRE).
     */
    private void checkExcludedConceptConditions(Concept concept) throws DKFValidationException {
        if (concept instanceof IntermediateConcept) {
            for (Concept child : ((IntermediateConcept) concept).getConcepts()) {
                checkExcludedConceptConditions(child);
            }
        } else {
            boolean is64bit = CommonProperties.getInstance().isJRE64Bit();
            for (AbstractCondition condition : concept.getConditions()) {
                if (is64bit && condition instanceof SIMILEInterfaceCondition) {
                    throw new DKFValidationException(
                            "The condition '" + condition.getClass() + "' does not support a 64-bit JRE.",
                            "The condition '" + condition.getClass()
                                    + "' does not support a 64-bit JRE. Try running GIFT in 32-bit.",
                            dkfilename, null);
                }
            }
        }
    }

    /**
     * Return the domain assessment knowledge for this dkf
     *
     * @return DomainKnowledge the domain assessment knowledge
     * @throws DetailedException if there was an error parsing the learner actions or DKF
     */
    public DomainAssessmentKnowledge getDomainAssessmentKnowledge() throws DetailedException {
    	if(!builtDomainAssessmentKnowledge) {
    		try {
				buildDomainAssessmentKnowledge(courseFolder);
			} catch (DetailedException e) {
				throw e;
			} catch (Exception e) {
				throw new DetailedException("There was a problem while trying to parse the dkf '"+dkfilename+"'.", "The error reads : "+e.getMessage(), e);
			}
    	}

        return domainAssessmentKnowledge;
    }
    
    /**
     * Return a collection of information describing the conditions that need a training
     * app connection in order to properly assess (e.g. Identify POIs condition requires VBS).
     * This method looks at the entire scenario object in the dkf (i.e. all tasks, subconcepts, concepts)
     * 
     * @return zero or more sentences describing the conditions in this dkf that need a training
     * application. Example sentence: "'Assess perimeter' is using the Identify POIs condition type".
     * Will not return null.
     */
    public List<String> getInfoOnConditionsNeedingTrainingApp(){
        
        List<String> info = new ArrayList<>();
        
        for(Task task : domainAssessmentKnowledge.getScenario().getTasks()){
            getInfoOnConditionsNeedingTrainingApp(task.getConcepts(), info);
        }
        
        return info;
    }
    
    /**
     * Recursively populate the info collection provided with information describing the conditions 
     * that need a training app connection in order to properly assess (e.g. Identify POIs condition
     * requires VBS).
     * 
     * @param concepts the collection of concepts from a task or subconcept that will be looked
     * at in this method for conditions.
     * @param info zero or more sentences describing the conditions in this dkf that need a training
     * application. Example sentence: "'Assess perimeter' is using the Identify POIs condition type".
     * Will not return null.
     */
    private void getInfoOnConditionsNeedingTrainingApp(List<Concept> concepts, List<String> info){
        
        for(Concept concept : concepts){
            
            if(concept instanceof IntermediateConcept){
                IntermediateConcept iConcept = (IntermediateConcept)concept;
                getInfoOnConditionsNeedingTrainingApp(iConcept.getConcepts(), info);
                continue;
            }
            
            for(AbstractCondition condition : concept.getConditions()){
                
                if(conditionsClassesRequiringTrainingApp.contains(condition.getClass())){
                    
                    StringBuilder conditionInfo = new StringBuilder();
                    String condDisplayName;
                    ConditionDescription condDesc = condition.getDescription();
                    if(condDesc != null){
                        condDisplayName = condDesc.getDisplayName();
                    }else{
                        condDisplayName = condition.getClass().getName();
                    }
                    conditionInfo.append("'").append(concept.getName()).append("' is using the ").append(condDisplayName).append(" condition type.");
                    info.add(conditionInfo.toString());
                }
            }

        }
    }

    /**
     * Validate the AutoTutor SKO dkf reference.  This will not parse the SKO but instead make sure
     * it is reachable by GIFT.
     *
     * @param skoRef contains the reference to the SKO to check
     * @param courseFolder used if the SKO being referenced is a file in the course folder
     * @throws DetailedException if there was a validation issue with the reference
     */
    public static void checkAutoTutorReference(generated.dkf.AutoTutorSKO skoRef, AbstractFolderProxy courseFolder) throws DetailedException{

        Serializable script = skoRef.getScript();
        if(script instanceof generated.dkf.LocalSKO){

            String filename = ((generated.dkf.LocalSKO)script).getFile();
            AutoTutorModel.checkAutoTutorReference(filename, courseFolder);

        }else if(script instanceof generated.dkf.ATRemoteSKO){

            String scriptNameOrURL = ((generated.dkf.ATRemoteSKO)script).getURL().getAddress();

            String scriptURL = scriptNameOrURL;
            AutoTutorModel.checkAutoTutorReference(scriptURL, courseFolder);
        }
    }

    /**
     * Build the waypoints map for this domain
     */
    private PlacesOfInterestManager gatherPlacesOfInterest() {

        PlacesOfInterestManager placesOfInterestMgr = new PlacesOfInterestManager();

        PlacesOfInterest placesofInterest = scenario.getAssessment().getObjects().getPlacesOfInterest();
        if(placesofInterest != null){
            for (Serializable placeOfInterest : placesofInterest.getPointOrPathOrArea()) {

                if(placeOfInterest instanceof generated.dkf.Point){
                    placesOfInterestMgr.addPlaceOfInterest(new Point((generated.dkf.Point)placeOfInterest));
                }else if(placeOfInterest instanceof generated.dkf.Path){
                    placesOfInterestMgr.addPlaceOfInterest(new Path((generated.dkf.Path)placeOfInterest));
                }else if(placeOfInterest instanceof generated.dkf.Area){
                    placesOfInterestMgr.addPlaceOfInterest(new Area((generated.dkf.Area)placeOfInterest));
            }
        }
        }

        return placesOfInterestMgr;
    }

    /**
     * Build the domain assessment knowledge for this handler.
     *
     * @param courseFolder - the course folder of the course using this DKF.  Can't be null.
     * @throws IOException if there is a problem with resources, i.e. learner action file can't be found or accessed
     * @throws FileValidationException  if there was a problem parsing the learner actions or DKF file
     * @throws DetailedException if there was a gift validation issue or problem retrieving a file referenced by a learner action (e.g. conversation file)
     * @throws Exception if a learner action is missing required parameters
     */
    private void buildDomainAssessmentKnowledge(AbstractFolderProxy courseFolder) throws IOException, FileValidationException, DetailedException, Exception {

        Resources resources = buildResources(courseFolder, skipExternalFileLoading);
        resources.getLearnerActionsManager().checkLearnerActions(getDomainActionKnowledge().getStrageyNames());
        generated.dkf.LearnerActionsList learnerActionsList = resources.getLearnerActionsManager().getLearnerActions();
        Map<String, LearnerAction> learnerActionsMap = new HashMap<>();
        for(LearnerAction learnerAction : learnerActionsList.getLearnerAction()){

            if(learnerActionsMap.containsKey(learnerAction.getDisplayName())){
                //found duplicate, not allowed
                throw new DKFValidationException("Found a duplicate named learner action",
                        "The learner action name '"+learnerAction.getDisplayName()+"' is already being used.  Learner actions must have unique names.",
                        dkfilename, null);
            }

            learnerActionsMap.put(learnerAction.getDisplayName(), learnerAction);
        }

        Map<Integer, Concept> globalConceptIdToConcept = new HashMap<>();
        Map<generated.dkf.Task, List<Concept>> taskToConcepts = new HashMap<>();
        List<Task> tasks = buildTasks(globalConceptIdToConcept, taskToConcepts, learnerActionsMap);

        if (tasks == null || tasks.isEmpty()) {
            logger.error("unable to build tasks list in scenario named " + this.scenario.getName());
            throw new IllegalArgumentException("There are no tasks in scenario named " + this.scenario.getName());
        } else {

            List<AbstractTrigger> endTriggers = null;
            if(this.scenario.getEndTriggers() != null){
                try{
                    endTriggers = buildScenarioEndTriggers(this.scenario.getEndTriggers().getTrigger(),
                            globalConceptIdToConcept, taskToConcepts.keySet(), learnerActionsMap);
                }catch(Exception e){
                    throw new RuntimeException("Failed to build the scenario level end triggers.", e);
                }
            }

            /* Build mission */
            Mission mission = buildMission(this.scenario.getMission());

            // Build team organization
            TeamOrganization teamOrg = null;
            if (this.scenario.getTeamOrganization() != null){
                teamOrg = buildTeamOrganization(this.scenario.getTeamOrganization().getTeam());
            }            
            
            Scenario scenario = new Scenario(this.scenario.getName(), this.scenario.getDescription(), tasks, mission, resources, endTriggers, teamOrg);

            //Make sure performance nodes with the same parent don't have the same names since current scoring logic can't support that (starting with GradedScoreNode.java children map)
            if(logger.isInfoEnabled()){
                logger.info("Determining if DKF scenario object scoring rules are legitimate in order to validate the DKF of "+file+".");
            }
            scenario.getScores();

            domainAssessmentKnowledge =
                    new DomainAssessmentKnowledge(scenario);

        }

        builtDomainAssessmentKnowledge = true;
    }

    /**
     * Build the resources for this domain from the generated class's object
     * content for resources
     *
     * @param courseFolder - the course folder of the course using this DKF.  Can't be null.
     * @param skipExternalFileLoading - whether to skip loading/checking files that this DKF references (e.g. learner actions xml, conversation tree xml) 
     *  This is useful when the DKF is used for Game Master playback and not during course execution.
     * @return new resources instance
     * @throws IOException if there is a problem with resources, i.e. learner action file can't be found or accessed
     * @throws FileValidationException  if there was a problem parsing the learner actions file
     */
    private Resources buildResources(AbstractFolderProxy courseFolder, boolean skipExternalFileLoading) throws IOException, FileValidationException {
        return new Resources(this.scenario.getResources(), courseFolder, skipExternalFileLoading);
    }

    /**
     * Build the container of tasks for this domain
     *
     * @param globalConceptIdToConcept map of unique DKF concept node ids to the concept object created for it
     * @param taskToConcepts map of the DKF task element represented by a jaxb generated class object to the map of
     * concepts under that task.
     * @param learnerActionsMap map of unique learner action name to the learner action information.  Won't be null but can be empty.
     * @return List<Task> the created tasks based on the tasks authored in the DKF
     * @throws ConditionException if there was a problem building a condition class
     * @throws ConceptException  if there was a problem building a concept class
     */
    private List<Task> buildTasks(Map<Integer, Concept> globalConceptIdToConcept,
            Map<generated.dkf.Task, List<Concept>> taskToConcepts, Map<String, LearnerAction> learnerActionsMap)
                    throws ConceptException, ConditionException, DKFValidationException {

        List<Task> giftTasks = new ArrayList<Task>();

        buildConceptMap(this.scenario.getAssessment().getTasks(), globalConceptIdToConcept, taskToConcepts, learnerActionsMap);

        for (generated.dkf.Task task : this.scenario.getAssessment().getTasks().getTask()) {

            Task giftTask = buildTask(task, globalConceptIdToConcept, taskToConcepts, learnerActionsMap);

            //check that this task name is not a duplicate, because it doesn't make sense
            //to have 2 or more tasks with the same name.  It will make AAR review, LMS history and debugging
            //confusing because it will be difficult to determine one task from the other.
            for (Task aTask : giftTasks) {

                if (aTask.getName().equals(giftTask.getName())) {
                    logger.error("found duplicate task name of " + giftTask.getName() + ".  All task names must be unique.");
                    throw new DKFValidationException("Found duplicate task name of " + giftTask.getName(),
                            "The set containing task and concept names must be unique within a DKF",
                            file.getFileId(),
                            null);
                }
            }

            giftTasks.add(giftTask);
        }



        return giftTasks;
    }

    /**
     * Build a task from the generated class's task content
     *
     * @param task - dkf content for a task
     * @param globalConceptIdToConcept - map of all concepts in the dkf
     * @param taskToConcepts - map of task to its concepts
     * @param learnerActionsMap map of unique learner action name to the learner action information.  Won't be null but can be empty.
     * @return Task - new task
     * @throws DKFValidationException if there was a problem building the task
     */
    private Task buildTask(generated.dkf.Task task, Map<Integer, Concept> globalConceptIdToConcept,
            Map<generated.dkf.Task, List<Concept>> taskToConcepts, Map<String, LearnerAction> learnerActionsMap) throws DKFValidationException {

        String taskName = task.getName();
        validateNodeName(taskName);

        int nodeId = task.getNodeId().intValue();
        validateNodeId(nodeId, task);

        List<AbstractTrigger> startTriggers;
        StartTriggers sTriggers = task.getStartTriggers();
        if (sTriggers != null) {
            startTriggers = buildStartTriggers(task.getName(), task.getStartTriggers().getTrigger(), globalConceptIdToConcept, taskToConcepts.keySet(), learnerActionsMap);
        } else {
            startTriggers = new ArrayList<AbstractTrigger>(0);
        }

        List<AbstractTrigger> endTriggers = buildEndTriggers(task.getName(), task.getEndTriggers().getTrigger(), globalConceptIdToConcept,
                taskToConcepts.keySet(), learnerActionsMap);

        List<AbstractLessonAssessment> assessments = buildAssessments(task.getAssessments());
        
        // extract the optional initial difficulty value of the task
        Double initDifficultyValue = null;
        if(task.getDifficultyMetric() != null) {
            initDifficultyValue = task.getDifficultyMetric().getValue().doubleValue();
        }
        
        // extract the optional initial stress value of the task
        Double initStressValue = null;
        if(task.getStressMetric() != null) {
            initStressValue = task.getStressMetric().getValue().doubleValue();
        }

        Task newTask = new Task(nodeId, taskName, startTriggers, endTriggers, 
                taskToConcepts.get(task), assessments, task.isScenarioSupport(), initDifficultyValue, initStressValue);
        
        //
        // Metric algorithms (optional)
        //
        
        buildPerformanceMetric(task.getPerformanceMetric(), newTask);
        buildConfidenceMetric(task.getConfidenceMetric(), newTask);
        buildCompetenceMetric(task.getCompetenceMetric(), newTask);
        buildTrendMetric(task.getTrendMetric(), newTask);
        buildPriorityMetric(task.getPriorityMetric(), newTask);
        buildGradeMetric(newTask);
        
        return newTask;
    }
    
    /**
     * Instantiate the grade metric implementation class for this node used to calculate
     * the overall assessment grade.
     * 
     * @param node the node to set the grade metric implementation for. Can't be null.
     */
    private void buildGradeMetric(AbstractPerformanceAssessmentNode node){
        
        // for now use the default
        node.setGradeMetric(new DefaultGradeMetric());
    }

    /**
     * Instantiate the priority metric implementation class if provided.
     * 
     * @param trendMetric a performance node's priority metric parameter.  Can be null if not set.
     * @param node the performance node to set the priority metric algorithm on, if provided.
     */
    private void buildPriorityMetric(PriorityMetric priorityMetric, AbstractPerformanceAssessmentNode node){
        
        if(priorityMetric != null){
            
            String implClassName = priorityMetric.getPriorityMetricImpl();
            PriorityMetricInterface priorityMetricImpl;
            
            try {
                Constructor<?> constructor = Class.forName(PackageUtil.getRoot() + "." + implClassName).getDeclaredConstructor();
                constructor.setAccessible(true);
                priorityMetricImpl = (PriorityMetricInterface) constructor.newInstance();
                node.setPriorityMetric(priorityMetricImpl);

            } catch (NoSuchMethodException noSuchMethodException){
                //used to provide specific error message about an incorrect java class that was developed or incorrect input
                //type that was selected but could not be provided to the metric implementation class
                DKFValidationException exception = new DKFValidationException(
                        "Unable to instantiate the priority metric class for the task named '"+node.getName()+"'",
                        "There was a problem while trying to instantiate the class '" + implClassName + "', therefore this task will not be available. " +
                                "Please correct the error before trying to use that class again.",
                        dkfilename,
                        noSuchMethodException);

                throw exception;            
                
            } catch (Throwable e) {
                DKFValidationException exception = new DKFValidationException(
                        "Unable to instantiate the priority metric class for the task named '"+node.getName()+"'",
                        "There was a problem while trying to instantiate the class '" + implClassName + "', therefore this task will not be available.\n\nThe error message reads : "+e.getMessage(), 
                        dkfilename,
                        e);
                throw exception;
            }
        }
    }
    
    /**
     * Instantiate the assessment trend metric implementation class if provided.
     * 
     * @param trendMetric a performance node's trend metric parameter.  Can be null if not set.
     * @param node the performance node to set the trend metric algorithm on, if provided.
     */
    private void buildTrendMetric(TrendMetric trendMetric, AbstractPerformanceAssessmentNode node){
        
        if(trendMetric != null){
            
            String implClassName = trendMetric.getTrendMetricImpl();
            TrendMetricInterface trendMetricImpl;
            
            try {
                Constructor<?> constructor = Class.forName(PackageUtil.getRoot() + "." + implClassName).getDeclaredConstructor();
                constructor.setAccessible(true);
                trendMetricImpl = (TrendMetricInterface) constructor.newInstance();
                node.setTrendMetric(trendMetricImpl);

            } catch (NoSuchMethodException noSuchMethodException){
                //used to provide specific error message about an incorrect java class that was developed or incorrect input
                //type that was selected but could not be provided to the metric implementation class
                DKFValidationException exception = new DKFValidationException(
                        "Unable to instantiate the trend metric class for the task named '"+node.getName()+"'",
                        "There was a problem while trying to instantiate the class '" + implClassName + "', therefore this task will not be available. " +
                                "Please correct the error before trying to use that class again.",
                        dkfilename,
                        noSuchMethodException);

                throw exception;            
                
            } catch (Throwable e) {
                DKFValidationException exception = new DKFValidationException(
                        "Unable to instantiate the trend metric class for the task named '"+node.getName()+"'",
                        "There was a problem while trying to instantiate the class '" + implClassName + "', therefore this task will not be available.\n\nThe error message reads : "+e.getMessage(), 
                        dkfilename,
                        e);
                throw exception;
            }
        }
    }
    
    /**
     * Instantiate the competence metric implementation class if provided.
     * 
     * @param competenceMetric a performance node's competence metric parameter.  Can be null if not set.
     * @param node the performance node to set the competence metric algorithm on, if provided.
     */
    private void buildCompetenceMetric(CompetenceMetric competenceMetric, AbstractPerformanceAssessmentNode node){
        
        if(competenceMetric != null){
            
            String implClassName = competenceMetric.getCompetenceMetricImpl();
            CompetenceMetricInterface competenceMetricImpl;
            
            try {
                Constructor<?> constructor = Class.forName(PackageUtil.getRoot() + "." + implClassName).getDeclaredConstructor();
                constructor.setAccessible(true);
                competenceMetricImpl = (CompetenceMetricInterface) constructor.newInstance();
                node.setCompetenceMetric(competenceMetricImpl);

            } catch (NoSuchMethodException noSuchMethodException){
                //used to provide specific error message about an incorrect java class that was developed or incorrect input
                //type that was selected but could not be provided to the metric implementation class
                DKFValidationException exception = new DKFValidationException(
                        "Unable to instantiate the competence metric class for the task named '"+node.getName()+"'",
                        "There was a problem while trying to instantiate the class '" + implClassName + "', therefore this task will not be available. " +
                                "Please correct the error before trying to use that class again.",
                        dkfilename,
                        noSuchMethodException);

                throw exception;            
                
            } catch (Throwable e) {
                DKFValidationException exception = new DKFValidationException(
                        "Unable to instantiate the competence metric class for the task named '"+node.getName()+"'",
                        "There was a problem while trying to instantiate the class '" + implClassName + "', therefore this task will not be available.\n\nThe error message reads : "+e.getMessage(), 
                        dkfilename,
                        e);
                throw exception;
            }
        }
    }
    
    /**
     * Instantiate the confidence metric implementation class if provided.
     * 
     * @param confidenceMetric a performance node's confidence metric parameter.  Can be null if not set.
     * @param node the performance node to set the confidence metric algorithm on, if provided.
     */
    private void buildConfidenceMetric(ConfidenceMetric confidenceMetric, AbstractPerformanceAssessmentNode node){
        
        if(confidenceMetric != null){
            
            String implClassName = confidenceMetric.getConfidenceMetricImpl();
            ConfidenceMetricInterface confidenceMetricImpl;
            
            try {
                Constructor<?> constructor = Class.forName(PackageUtil.getRoot() + "." + implClassName).getDeclaredConstructor();
                constructor.setAccessible(true);
                confidenceMetricImpl = (ConfidenceMetricInterface) constructor.newInstance();
                node.setConfidenceMetric(confidenceMetricImpl);

            } catch (NoSuchMethodException noSuchMethodException){
                //used to provide specific error message about an incorrect java class that was developed or incorrect input
                //type that was selected but could not be provided to the metric implementation class
                DKFValidationException exception = new DKFValidationException(
                        "Unable to instantiate the confidence metric class for the task named '"+node.getName()+"'",
                        "There was a problem while trying to instantiate the class '" + implClassName + "', therefore this task will not be available. " +
                                "Please correct the error before trying to use that class again.",
                        dkfilename,
                        noSuchMethodException);

                throw exception;            
                
            } catch (Throwable e) {
                DKFValidationException exception = new DKFValidationException(
                        "Unable to instantiate the confidence metric class for the task named '"+node.getName()+"'",
                        "There was a problem while trying to instantiate the class '" + implClassName + "', therefore this task will not be available.\n\nThe error message reads : "+e.getMessage(), 
                        dkfilename,
                        e);
                throw exception;
            }
        }
    }
    
    /**
     * Instantiate the performance metric implementation class if provided.
     * 
     * @param performanceMetric a performance node's performance metric parameter.  Can be null if not set.
     * @param node the performance node to set the performance metric algorithm on, if provided.
     */
    private void buildPerformanceMetric(PerformanceMetric performanceMetric, AbstractPerformanceAssessmentNode node){
        
        if(performanceMetric != null){
            
            String implClassName = performanceMetric.getPerformanceMetricImpl();
            PerformanceMetricInterface performanceMetricImpl;
            
            try {
                Constructor<?> constructor = Class.forName(PackageUtil.getRoot() + "." + implClassName).getDeclaredConstructor();
                constructor.setAccessible(true);
                performanceMetricImpl = (PerformanceMetricInterface) constructor.newInstance();
                node.setPerformanceMetric(performanceMetricImpl);

            } catch (NoSuchMethodException noSuchMethodException){
                //used to provide specific error message about an incorrect java class that was developed or incorrect input
                //type that was selected but could not be provided to the metric implementation class
                DKFValidationException exception = new DKFValidationException(
                        "Unable to instantiate the performance metric class for the task named '"+node.getName()+"'",
                        "There was a problem while trying to instantiate the class '" + implClassName + "', therefore this task will not be available. " +
                                "Please correct the error before trying to use that class again.",
                        dkfilename,
                        noSuchMethodException);

                throw exception;            
                
            } catch (Throwable e) {
                DKFValidationException exception = new DKFValidationException(
                        "Unable to instantiate the performance metric class for the task named '"+node.getName()+"'",
                        "There was a problem while trying to instantiate the class '" + implClassName + "', therefore this task will not be available.\n\nThe error message reads : "+e.getMessage(), 
                        dkfilename,
                        e);
                throw exception;
            }
        }
    }

    /**
     * Build the collection of assessments for the generated class's collection
     * of assessments.
     *
     * @param assessments - dkf content for a collection of assessments
     * @return List<AbstractLessonAssessment> - new collection of assessments
     */
    private List<AbstractLessonAssessment> buildAssessments(Assessments assessments) {

        if(assessments == null){
            return null;
        }

        AbstractLessonAssessment giftAssessment = null;
        List<AbstractLessonAssessment> giftAssessments = new ArrayList<AbstractLessonAssessment>(assessments.getAssessmentTypes().size());
        for (Object assessmentType : assessments.getAssessmentTypes()) {

            if(assessmentType instanceof generated.dkf.Assessments.Survey){

                giftAssessment = new GIFTSurveyLessonAssessment((generated.dkf.Assessments.Survey)assessmentType);
                giftAssessments.add(giftAssessment);

            }else if(assessmentType instanceof generated.dkf.Assessments.ConditionAssessment){
                giftAssessments.add(new ConditionLessonAssessment());
            }
        }

        return giftAssessments;
    }

    /**
     * Handle building the scenario level end triggers which are used to end a scenario.
     *
     * @param endTriggers scenario end triggers authored in the DKF.  Can be null or empty.
     * @param conceptIdToConcept map of unique DKF concept node ids to the concept object created for it
     * @param tasks collection of DKF tasks authored for this scenario
     * @param learnerActionsMap map of unique learner action name to the learner action information.  Won't be null but can be empty.
     * @return List<AbstractTrigger> the collection of GIFT triggers based on the authored scenario end triggers.
     * List can be empty but not null.
     * @throws DKFValidationException if there was a problem building a trigger
     */
    private List<AbstractTrigger> buildScenarioEndTriggers(List<generated.dkf.Scenario.EndTriggers.Trigger> endTriggers,
            Map<Integer, Concept> conceptIdToConcept, Set<generated.dkf.Task> tasks, Map<String, LearnerAction> learnerActionsMap) throws DKFValidationException{

        List<AbstractTrigger> giftTriggers = new ArrayList<AbstractTrigger>(endTriggers.size());
        for(generated.dkf.Scenario.EndTriggers.Trigger trigger : endTriggers){

            AbstractTrigger giftTrigger = buildTrigger("Real-time assessment", trigger.getTriggerType(), conceptIdToConcept, tasks, learnerActionsMap);

            if(trigger.getTriggerDelay() != null){
                giftTrigger.setTriggerDelay(trigger.getTriggerDelay().floatValue());
            }

            if(trigger.getMessage() != null){                
                addDomainActionsForTrigger(giftTrigger, trigger.getMessage().getStrategy());
            }

            giftTriggers.add(giftTrigger);
        }

        return giftTriggers;
    }

    /**
     * Handle building the task level end triggers which are used to end a running task.
     * 
     * @param taskName the name of the task that needs end triggers built
     * @param endTriggers task end triggers authored in the DKF.  Can be null or empty.
     * @param conceptIdToConcept map of unique DKF concept node ids to the concept object created for it
     * @param tasks collection of DKF tasks authored for this scenario
     * @param learnerActionsMap map of unique learner action name to the learner action information.  Won't be null but can be empty.
     * @return List<AbstractTrigger> the collection of GIFT triggers based on the authored task end triggers.
     * List can be empty but not null.
     * @throws DKFValidationException  if there was a problem building a trigger
     */
    private List<AbstractTrigger> buildEndTriggers(String taskName, List<EndTriggers.Trigger> endTriggers,
            Map<Integer, Concept> conceptIdToConcept, Set<generated.dkf.Task> tasks,
            Map<String, LearnerAction> learnerActionsMap) throws DKFValidationException{

        List<AbstractTrigger> giftTriggers = new ArrayList<AbstractTrigger>(endTriggers.size());
        for(EndTriggers.Trigger trigger : endTriggers){

            AbstractTrigger giftTrigger = buildTrigger(taskName, trigger.getTriggerType(), conceptIdToConcept, tasks, learnerActionsMap);

            if(trigger.getTriggerDelay() != null){
                giftTrigger.setTriggerDelay(trigger.getTriggerDelay().floatValue());
            }

            if(trigger.getMessage() != null){
                addDomainActionsForTrigger(giftTrigger, trigger.getMessage().getStrategy());
            }

            giftTriggers.add(giftTrigger);
        }

        return giftTriggers;
    }

    /**
     * Handle building the task level start triggers which are used to start a task that isn't already running.
     *
     * @param taskName - the name of the task who needs start triggers built
     * @param startTriggers task start triggers authored in the DKF.  Can be null or empty.
     * @param conceptIdToConcept map of unique DKF concept node ids to the concept object created for it
     * @param tasks collection of DKF tasks authored for this scenario
     * @param learnerActionsMap map of unique learner action name to the learner action information.  Won't be null but can be empty.
     * @return List<AbstractTrigger> the collection of GIFT triggers based on the authored task end triggers.
     * List can be empty but not null.
     * @throws DKFValidationException  if there was a problem building a trigger
     */
    private List<AbstractTrigger> buildStartTriggers(String taskName, List<StartTriggers.Trigger> startTriggers,
            Map<Integer, Concept> conceptIdToConcept, Set<generated.dkf.Task> tasks, Map<String, LearnerAction> learnerActionsMap) throws DKFValidationException{

        List<AbstractTrigger> giftTriggers = new ArrayList<AbstractTrigger>(startTriggers.size());
        for(StartTriggers.Trigger trigger : startTriggers){

            AbstractTrigger giftTrigger = buildTrigger(taskName, trigger.getTriggerType(), conceptIdToConcept, tasks, learnerActionsMap);

            if(trigger.getTriggerDelay() != null){
                giftTrigger.setTriggerDelay(trigger.getTriggerDelay().floatValue());
            }

            if(trigger.getTriggerMessage() != null){
                addDomainActionsForTrigger(giftTrigger, trigger.getTriggerMessage().getStrategy());
            }

            giftTriggers.add(giftTrigger);
        }

        return giftTriggers;
    }

    /**
     * Builds the appropriate feedback presentation object for the trigger message authored.  Then adds
     * that object to the common domain representation for that trigger type.
     *
     * @param giftTrigger the common domain representation for the trigger being built
     * @param strategy the authored strategy for the trigger to handle when the trigger is activated
     */
    private void addDomainActionsForTrigger(AbstractTrigger giftTrigger, generated.dkf.Strategy strategy){

        if(strategy != null && !strategy.getStrategyActivities().isEmpty()){

            for(Serializable action : strategy.getStrategyActivities()){
                giftTrigger.addDomainAction(action);
            }
        }
    }

    /**
     * Build the collection of triggers for the generated class's collection of
     * triggers
     *
     * @param triggerNamePrefix - a prefix to append to the beginning of the trigger name used for display purposes.  Can be empty
     * but not recommended.  Can't be null. 
     * @param trigger - dkf content for a collection of triggers
     * @param conceptIdToConcept - mapping of concepts used to find a concept if
     *        certain types of triggers are used
     * @param tasks - the tasks that exist in this scenario
     * @param learnerActionMap map of unique learner action name to the learner
     *        action information. Won't be null but can be empty.
     * @return List<AbstractTrigger> - new collection of triggers
     * @throws DKFValidationException if there was a problem building a trigger
     */
    private AbstractTrigger buildTrigger(String triggerNamePrefix, Serializable trigger, Map<Integer, Concept> conceptIdToConcept,
            Set<generated.dkf.Task> tasks,
            Map<String, LearnerAction> learnerActionMap) throws DKFValidationException {

        AbstractTrigger giftTrigger = null;

        if (trigger instanceof EntityLocation) {
            EntityLocation entityLocation = (EntityLocation) trigger;
            TriggerLocation triggerLocation = entityLocation.getTriggerLocation();
            AbstractCoordinate location;
            Double radius = null;
            if(triggerLocation.getCoordinate() != null) {
                location = DomainDKFHandler.buildCoordinate(triggerLocation.getCoordinate());
            }else if(triggerLocation.getPointRef() != null) {
                PointRef ptRef = triggerLocation.getPointRef();
                PlaceOfInterestInterface poiInterface = placesOfInterestMgr.getPlacesOfInterest(ptRef.getValue());
                if(!(poiInterface instanceof Point)) {
                    // error
                    throw new DKFValidationException("Unable to build an entity location trigger because the place of interest is not a point.",
                            "The entity location type is not a point but a "+poiInterface,
                            dkfilename, null);
                }
                Point pt = (Point)poiInterface;
                location = pt.toGCC();
                
                BigDecimal distanceBD = ptRef.getDistance();
                if(distanceBD != null) {
                    radius = distanceBD.doubleValue();
                }
            }else {
                // error
                throw new DKFValidationException("Unable to build an entity location trigger because the entity location type is not handled.",
                        "The entity location type is not supported or not specified",
                        dkfilename, null);
            }
            giftTrigger = new EntityLocationTrigger(triggerNamePrefix + " entity location trigger", (EntityLocation) trigger, location, radius);

        } else if (trigger instanceof ConceptEnded) {
            Concept concept = conceptIdToConcept.get(((ConceptEnded) trigger).getNodeId().intValue());
            giftTrigger = new ConceptEndedTrigger(triggerNamePrefix + " concept '"+concept.getName()+"' ended trigger", concept);

        } else if (trigger instanceof ChildConceptEnded) {
            Concept concept = conceptIdToConcept.get(((ChildConceptEnded) trigger).getNodeId().intValue());
            giftTrigger = new ConceptEndedTrigger(triggerNamePrefix + " concept '"+concept.getName()+"' ended trigger", concept);

        } else if (trigger instanceof LearnerActionReference){
            LearnerActionReference learnerActionReference = (LearnerActionReference)trigger;
            LearnerAction learnerAction = learnerActionMap.get(learnerActionReference.getName());
            if(learnerAction == null){
                throw new DKFValidationException("Unable to build a trigger because the learner action could not be found.",
                        "A learner action based trigger was authored that referenced a learner action named '"+learnerActionReference.getName()+"' but no learner action with that name was authored",
                        dkfilename, null);
            }
            giftTrigger = new LearnerActionTrigger(triggerNamePrefix + " learner action "+learnerAction.getDisplayName()+" trigger", learnerAction);

        } else if (trigger instanceof TaskEnded) {
            // Find the generated task that is represented by the TaskEnded trigger
            for (generated.dkf.Task task : tasks) {

                if (task.getNodeId().compareTo(((TaskEnded) trigger).getNodeId()) == 0) {
                    giftTrigger = new TaskEndedTrigger(triggerNamePrefix + " "+task.getName()+" task ended trigger", task);
                }
            }
        } else if (trigger instanceof ConceptAssessment) {
            Concept concept = conceptIdToConcept.get(((ConceptAssessment) trigger).getConcept().intValue());
            AssessmentLevelEnum goalAssessment = AssessmentLevelEnum.valueOf(((ConceptAssessment)trigger).getResult());
            giftTrigger = new ConceptAssessmentTrigger(triggerNamePrefix + " concept "+concept.getName()+" "+goalAssessment.getDisplayName()+" trigger", concept, goalAssessment);
            
        } else if(trigger instanceof ScenarioStarted){
            giftTrigger = new ScenarioStartTrigger("Scenario Started Trigger");
            
        } else if(trigger instanceof StrategyApplied){
            
            StrategyApplied strategyApplied = (StrategyApplied)trigger;
            DomainActionKnowledge dak = getDomainActionKnowledge();
            boolean found = false;
            for(String strategyName : dak.getStrageyNames()){
                if(strategyName.equalsIgnoreCase(strategyApplied.getStrategyName())){
                    found = true;
                }
            }
            
            if(!found){
                throw new DKFValidationException("Unable to build a trigger because the strategy referenced could not be found.",
                        "A strategy applied based trigger was authored that referenced a strategy named '"+strategyApplied.getStrategyName()+"' but no strategy with that name was authored",
                        dkfilename, null);
            }
            giftTrigger = new StrategyAppliedTrigger(triggerNamePrefix + " Applied Strategy '" + strategyApplied.getStrategyName()+"'  trigger", strategyApplied);
            
        } else {
            logger.error("Found unhandled trigger type of " + trigger);
            throw new DKFValidationException("Found unhandled trigger type of " + trigger,
                    "Maybe you are using a new trigger type in the DKF schema that needs to be handled when parsing the DKF.",
                    file.getFileId(),
                    null);
        }

        if (giftTrigger == null) {
            logger.error("While building trigger " + trigger + ", null was returned so the trigger will not be implemented.");
            throw new DKFValidationException("Failed to build the trigger "+trigger+".",
                    "While building trigger " + trigger + ", null was returned so the trigger will not be implemented.",
                    file.getFileId(),
                    null);
        }


        return giftTrigger;
    }

    /**
     * Build the collection of concepts based on the generated class's concepts
     * content from a dkf
     *
     * @param tasks - dkf content for a collection of tasks which have concepts
     * @param globalConceptIdToConcept - map of all concept unique node ids associated with all tasks
     * @param taskToConcepts - map of task to its concepts
     * @param learnerActionsMap map of unique learner action name to the learner action information.  Won't be null but can be empty.
     * @throws ConceptException if there was a problem building the concept (besides a problem with a concept's condition)
     * @throws ConditionException if there was a problem building a concept's condition
     */
    private void buildConceptMap(generated.dkf.Tasks tasks,  Map<Integer, Concept> globalConceptIdToConcept,
            Map<generated.dkf.Task, List<Concept>> taskToConcepts,
            Map<String, LearnerAction> learnerActionsMap) throws ConceptException, ConditionException {

        for(generated.dkf.Task task : tasks.getTask()){
            List<Concept> conceptList = buildConcepts(task.getConcepts().getConcept(), globalConceptIdToConcept, learnerActionsMap);
            taskToConcepts.put(task, conceptList);
        }

    }

    /**
     * Build the collection of concepts based on the generated class's concepts
     * content from a dkf
     *
     * @param concepts - list of dkf concepts to create Concept class instances for that will be used during course execution.  Can't be null.
     * @param globalConceptIdToConcept - mapping of unique concept (node) id in the DKF to the Concept class instance used during course execution for that dkf concept.  Can't be null.
     * @param learnerActionsMap map of unique learner action name to the learner action information.  Won't be null but can be empty.
     * @return collection of created concepts (excluding descendant concepts beyond the first level of descendants if intermediate/sub-concepts are being
     * used).  Can be empty but not null.
     * @throws ConceptException if there was a problem building the concept (besides a problem with a concept's condition)
     * @throws ConditionException if there was a problem building a concept's condition
     */
    private List<Concept> buildConcepts(List<generated.dkf.Concept> concepts,
            Map<Integer, Concept> globalConceptIdToConcept,
            Map<String, LearnerAction> learnerActionsMap) throws ConceptException, ConditionException {

        List<Concept> conceptList = new ArrayList<Concept>(concepts.size());

        for (generated.dkf.Concept concept : concepts) {
            conceptList.add(buildConcept(concept, globalConceptIdToConcept, learnerActionsMap));
        }

        return conceptList;
    }

    /**
     * Build a concept based on the generated class's concept content from a dkf
     *
     * @param concept - a dkf concept to create Concept class instance for to use during course execution.  Can't be null.
     * @param globalConceptIdToConcept - mapping of unique concept (node) id in the DKF to the Concept class instance used during course execution for that dkf concept.  Can't be null.
     * This method will add entries to this map when a concept or subconcept is created.
     * @param learnerActionsMap map of unique learner action name to the learner action information.  Won't be null but can be empty.
     * @return the runtime Concept instance created from the dkf concept parameters
     * @throws ConceptException if there was a problem building the concept (besides a problem with a concept's condition)
     * @throws ConditionException if there was a problem building a concept's condition
     */
    private Concept buildConcept(generated.dkf.Concept concept,
            Map<Integer, Concept> globalConceptIdToConcept,
            Map<String, LearnerAction> learnerActionsMap) throws ConceptException, ConditionException {

        try{
            String conceptName = concept.getName();
            validateNodeName(conceptName);

            int nodeId = concept.getNodeId().intValue();
            validateNodeId(nodeId, concept);

            List<AbstractLessonAssessment> assessments = buildAssessments(concept.getAssessments());

            Object conditionsOrConcepts = concept.getConditionsOrConcepts();

            //the concept to build
            Concept giftConcept;

            if (conditionsOrConcepts instanceof Conditions) {
                //the concept has conditions node as a child, therefore it doesn't have a nested concept as a child

                List<AbstractCondition> conditions = buildConditions(concept.getName(), (Conditions) conditionsOrConcepts, learnerActionsMap);
                giftConcept = new Concept(nodeId, conceptName, conditions, assessments, concept.getPriority(), concept.isScenarioSupport());

            } else {
                //the concept has sub-concepts as a child, need to keep searching for conditions...

                List<Concept> concepts = buildConcepts(((generated.dkf.Concepts) conditionsOrConcepts).getConcept(), globalConceptIdToConcept, learnerActionsMap);
                giftConcept = new IntermediateConcept(nodeId, conceptName, concepts, assessments, concept.getPriority(), concept.isScenarioSupport());
            }
            
            giftConcept.setPerformanceArguments(concept.getPerformanceMetricArguments());

            globalConceptIdToConcept.put(nodeId, giftConcept);
            return giftConcept;

        } catch (ConditionException conditionException){
            conditionException.setParentConcept(concept);
            throw conditionException;
        }catch(Throwable t){
            throw new ConceptException(concept,
                    "Unable to configure concept "+concept.getName()+" in DKF '"+dkfilename+"'.",
                    "Caught exception while trying configure concept.  The error messages reads:\n"+t.getMessage(),
                    dkfilename,
                    t);
        }
    }

    /**
     * Build the container of conditions based on the generated class's
     * conditions content from a dkf
     *
     * @param conceptName the name of the concept that contains these conditions
     * @param conditions - dkf content for a collection of conditions
     * @param learnerActionsMap map of unique learner action name to the learner action information.  Won't be null but can be empty.
     * @return List<AbstractCondition> - new collection of gift conditions
     * @throws ConditionException if there was a problem building a condition
     */
    private List<AbstractCondition> buildConditions(String conceptName, generated.dkf.Conditions conditions,
            Map<String, LearnerAction> learnerActionsMap) throws ConditionException {

        List<AbstractCondition> giftConditions = new ArrayList<AbstractCondition>(conditions.getCondition().size());
        for (generated.dkf.Condition condition : conditions.getCondition()) {

            AbstractCondition giftCondition = buildCondition(conceptName, condition, learnerActionsMap);
            giftConditions.add(giftCondition);
        }

        return giftConditions;
    }

    /**
     * Build the condition based on the generated class's condition content from
     * a dkf
     *
     * @param conceptName the name of the concept that contains these conditions
     * @param condition - dkf content for a condition
     * @param learnerActionsMap map of unique learner action name to the learner action information.  Won't be null but can be empty.
     * @return AbstractCondition - new condition
     * @throws ConditionException - if there was a problem building the condition class.
     */
    private AbstractCondition buildCondition(String conceptName, generated.dkf.Condition condition,
            Map<String, LearnerAction> learnerActionsMap) throws ConditionException {

        AbstractCondition giftCondition = null;

        String implClassName = condition.getConditionImpl();

        try {
            Constructor<?> constructor = Class.forName(PackageUtil.getRoot() + "." + implClassName).getDeclaredConstructor(condition.getInput().getType().getClass());
            constructor.setAccessible(true);
            giftCondition = (AbstractCondition) constructor.newInstance(condition.getInput().getType());

            //set course folder which is needed when conditions need to perform I/O file operations
            giftCondition.setCourseFolder(courseFolder);
            
            if(outputFolder != null) {
                giftCondition.setOutputFolder(outputFolder);
            }

            //set places of interest manager which is needed when a condition references a place of interest authored in the dkf
            giftCondition.setPlacesOfInterestManager(placesOfInterestMgr);
            
            giftCondition.setPerformanceArguments(condition.getPerformanceMetricArguments());

            //check for overall scoring (i.e. AAR) rules
            if (condition.getScoring() != null) {
                // setting overall scoring rules for all team org members that might be assessed with this condition
                // the condition is responsible for decomposing its inputs to determine how these scorers might be decomposed/copied
                // for each sub team member being assessed.
                giftCondition.addScorers(condition.getScoring(), null);
            }

            //check for default assessment level
            if (condition.getDefault() != null) {
                giftCondition.updateAssessment(AssessmentLevelEnum.valueOf(condition.getDefault().getAssessment()));
            }
            
            //check that the condition has all the necessary learner actions available
            Set<generated.dkf.LearnerActionEnumType> learnerActionTypes = giftCondition.getLearnerActionsNeeded();
            if(learnerActionTypes != null){

                Iterator<generated.dkf.LearnerActionEnumType> itr = learnerActionTypes.iterator();
                boolean found;
                while(itr.hasNext()){
                    generated.dkf.LearnerActionEnumType learnerActionTypeNeeded = itr.next();

                    found = false;
                    for(LearnerAction learnerAction : learnerActionsMap.values()){

                        if(learnerAction.getType().equals(learnerActionTypeNeeded)){
                            found = true;
                            break;
                        }
                    }

                    if(!found){
                        throw new Exception("Unable to find the learner action type "+learnerActionTypeNeeded+" which is needed by the "+
                                giftCondition.getDescription().getDisplayName()+" condition to assess the '"+conceptName+"' concept.  Please "+
                                "author a "+learnerActionTypeNeeded+" learner action.");
                    }
                }
            }

        } catch (NoSuchMethodException noSuchMethodException){
            ConditionException exception = new ConditionException(condition,
                    "Unable to instantiate the condition class for the concept named '"+conceptName+"' given the condition input type authored.",
                    "There was a problem while trying to instantiate the condition class '" + implClassName + "', therefore this condition will not be available. " +
                            "The most common cause is that the incorrect input type was choosen for the condition implementation entry.  Look for similarly named implementation "+
                            "and input type values (e.g. 'domain.knowledge.condition.AvoidLocationCondition' and 'AvoidLocationCondition')\n\n"+
                            "Developer Note:\nIf you want to use the selected input type with the class, make sure that '"+implClassName+"' has a constructor with a single parameter of type "+condition.getInput().getType().getClass()+".",
                    dkfilename,
                    noSuchMethodException);

            throw exception;

        } catch (Throwable e) {
            // this error is useful because the stack might be stripped from display on the gift dashboard by the time it makes it there
            logger.error("There was an issue while trying to instantiate the condition class '"+implClassName+"'.", e);
            
            ConditionException exception = new ConditionException(condition,
                    "Unable to instantiate the condition class for the concept named '"+conceptName+"'.",
                    "There was a problem while trying to instantiate the condition class '" + implClassName + "', therefore this condition will not be available.\n\nThe error message reads : "+e.getMessage(),
                    dkfilename,
                    e);
            throw exception;
        }

        return giftCondition;
    }

    /**
     * Check the node id against all other currently known node ids for
     * duplicates.
     *
     * @param nodeId - the latest node id to check
     * @param node - the node associated with this node id
     * @throws DKFValidationException if there is a duplicate node id in the DKF
     */
    private void validateNodeId(int nodeId, Serializable node) throws DKFValidationException {

        if(nodeIds.containsKey(nodeId)){
            //there is a node with that id already, is it the provided node?

            if(nodeIds.get(nodeId) != node){
                throw new DKFValidationException("There are two (or more) performance assessment node (task/concept) ids of " + nodeId,
                        "The set of ids for all tasks and concepts must be unique within a DKF.",
                        file.getFileId(),
                        null);
            }

        }else{
            nodeIds.put(nodeId, node);
        }

    }

    /**
     * Check the performance assessment node name against all other currently known node names for duplicates.
     * The node names need to be unique in order to facilitate an easier mapping to course level concepts.
     *
     * @param nodeName the performance assessment node name to check
     * @throws DKFValidationException if there is a duplicate node name in the DKF
     */
    private void validateNodeName(String nodeName) throws DKFValidationException{

        if(nodeNames.contains(nodeName)){
            throw new DKFValidationException("There are two (or more) performance assessment node names of '" + nodeName + "'.",
                    "The set of names for all tasks and concepts must be unique within a DKF",
                    file.getFileId(),
                    null);

        }else if(nodeName.equalsIgnoreCase(CourseConceptsUtil.COURSE_CONCEPTS_CONCEPT_NAME)){
            throw new DKFValidationException("The performance assessment node name of '" + nodeName + "' is not allowed.",
                    "This is a restriced node name used internally by GIFT.",
                    file.getFileId(),
                    null);
        }else{
            nodeNames.add(nodeName);
        }
    }

    /**
     * Parse a location string containing x,y,z coordinates
     *
     * @param coordinate The coordinate element to build the coordinate from
     * @return Point3d - the location coordinate in 3d space
     */
    public static AbstractCoordinate buildCoordinate(Coordinate coordinate) {

        if (coordinate == null) {
            logger.error("Unable to build coordinate from null");
            return null;
        }

        Object coordType = coordinate.getType();
        if (coordType instanceof generated.dkf.GCC) {
            generated.dkf.GCC gcc = (generated.dkf.GCC) coordType;
            return new GCC(gcc.getX().doubleValue(), gcc.getY().doubleValue(), gcc.getZ().doubleValue());

        } else if (coordType instanceof generated.dkf.GDC) {
            generated.dkf.GDC gdc = (generated.dkf.GDC) coordType;
            return new GDC(gdc.getLatitude().doubleValue(), gdc.getLongitude().doubleValue(), gdc.getElevation().doubleValue());

        } else if (coordType instanceof generated.dkf.AGL) {
            generated.dkf.AGL agl = (generated.dkf.AGL) coordType;
            return new AGL(agl.getX().doubleValue(), agl.getY().doubleValue(), agl.getElevation().doubleValue());

        } else {
            logger.error("Found unhandled DKF coordinate type in " + coordinate);
        }

        return null;

    }

    /**
     * Convert the dkf coordinates provided into the common coordinate objects.
     * 
     * @param path a collection of dkf coordinate objects
     * @return the common coordinate objects.
     */
    public static List<AbstractCoordinate> buildCoordinatesFromCoordinates(List<Coordinate> path){
        
        List<AbstractCoordinate> points = new ArrayList<>();
        for(Coordinate coordinate : path){
            
            AbstractCoordinate coord = buildCoordinate(coordinate);
            if(coord != null){
                points.add(coord);
            }
        }
        return points;
    }
    
    /**
     * Convert the dkf segments provided into a collection of common coordinate objects.
     * 
     * @param segments a collection of dkf segments objects
     * @return the common coordinate objects for the segments.  This will maintain the order
     * of the segments and the segments start/end points.  If the Nth segment end point is the 
     * same as the N+1 segment start point than that point will only appear once in the returned
     * list.  But if the segments are disjointed, both the end and start points in this example,
     * will be added to the returned list.
     */
    public static List<AbstractCoordinate> buildCoordinatesFromSegments(List<Segment> segments){
        
        List<AbstractCoordinate> points = new ArrayList<>();
        
        AbstractCoordinate prevEndCoord = null;
        for(Segment segment : segments){
            
            if(segment.getStart() != null){
                AbstractCoordinate startCoord = buildCoordinate(segment.getStart().getCoordinate());
                
                if(startCoord != null){
                
                    if(prevEndCoord != null && !startCoord.equals(prevEndCoord)){
                        //disjointed segment, i.e. the start of this segment is not the same as the end of the previous segment
                        points.add(startCoord);
                    }else if(prevEndCoord == null){
                        //this is the first segment
                        points.add(startCoord);
                    }
                }
            }
            
            if(segment.getEnd() != null){
                AbstractCoordinate endCoord = buildCoordinate(segment.getEnd().getCoordinate());
                
                if(endCoord != null){
                    points.add(endCoord);
                    prevEndCoord = endCoord;
                }
            }
        }
        
        return points;
    }

    /**
     * Build the mission data from the authored mission data.
     * 
     * @param mission the scenario mission data.
     * @return new mission data. Can be null if the scenario mission data was
     *         null.
     */
    public Mission buildMission(generated.dkf.Scenario.Mission mission) {
        if (mission == null) {
            return null;
        }

        return new Mission(mission.getSource(), mission.getMET(), mission.getTask(), mission.getSituation(),
                mission.getGoals(), mission.getCondition(), mission.getROE(), mission.getThreatWarning(),
                mission.getWeaponStatus(), mission.getWeaponPosture());
    }

    /**
     * Build the team organization from the authored team organization
     * 
     * @param team the root team in the authored team organization
     * @return new team organization.  Can be null if the root team was not provided.
     */
    public TeamOrganization buildTeamOrganization(generated.dkf.Team team){
        
        Team rootTeam = buildTeam(team);
        if(rootTeam == null){
            return null;
        }
        
        return new TeamOrganization(rootTeam);
    }
    /**
     * Build a team from the generated class's team content
     * 
     * @param team - dkf content for a team. Can be null.
     * @return  new team. Can be null if the root team was not provided.
     */
    private Team buildTeam(generated.dkf.Team team) {
        
        if(team == null) {
            return null;
        }
        
        String name = team.getName();
        validateTeamUnitName(name);
        
        List<AbstractTeamUnit> units = new ArrayList<>();
        
        for(Serializable unit : team.getTeamOrTeamMember()) {
            
            if(unit instanceof generated.dkf.TeamMember) {
                units.add(buildTeamMember((generated.dkf.TeamMember) unit));
                
            } else if(unit instanceof generated.dkf.Team) {
                units.add(buildTeam((generated.dkf.Team) unit));
            }
        }
        
        EchelonEnum echelon = null;
        if (team.getEchelon() != null) {
            echelon = EchelonEnum.valueOf(team.getEchelon(), EchelonEnum.VALUES());
        }
        
        return new Team(name, echelon, units);
    }
    
    /**
     * Build a team member from the generated class's team member content
     * 
     * @param member - dkf content for a team member. Can be null.
     * @return TeamMember - new team member. Can be null.
     */
    public TeamMember<?> buildTeamMember(generated.dkf.TeamMember member) {
        
        if(member == null) {
            return null;
        }
        
        if(member.getLearnerId() == null) {
            throw new DKFValidationException("A team member is missing a learner ID used to uniquely identify it.",
                    "All team members must provide unique learner IDs within a DKF",
                    file.getFileId(),
                    null);
        }
        
        String name = member.getName();
        validateTeamUnitName(name);
        TeamMember<?> teamMember;
        
        if(member.getLearnerId().getType() instanceof StartLocation) {
            teamMember = new LocatedTeamMember(name, buildCoordinate(((StartLocation) member.getLearnerId().getType()).getCoordinate()));
            
        } else if(member.getLearnerId().getType() instanceof String) {
            teamMember = new MarkedTeamMember(name, (String) member.getLearnerId().getType());
            
        } else {
            throw new DKFValidationException("A team member's learner ID is not of a known type.",
                    "A team member's learner IDs within a DKF must specify either the member's start location or "
                    + "a training-application-specific marker.",
                    file.getFileId(),
                    null);
        }
        
        if(!member.isPlayable()){
            teamMember.setPlayable(false);
        }
        
        return teamMember;
    }
    
    /**
     * Check the team unit name against all other currently known team unit names for duplicates.
     * The team unit names need to be unique in order to facilitate an easier mapping to team-based strategies.
     *
     * @param teamName the team unit name to check
     * @throws DKFValidationException if there is a duplicate node name in the DKF
     */
    private void validateTeamUnitName(String teamName) throws DKFValidationException{

        if(teamNames.contains(teamName)){
            throw new DKFValidationException("There are two (or more) team unit names of '" + teamName + "'.",
                    "The set of names for all teams and team members must be unique within a DKF",
                    file.getFileId(),
                    null);

        }else{
            teamNames.add(teamName);
        }
    }

    /**
     * Used to contain information about an exception related to configuring a concept
     * for a DKF.
     *
     * @author mhoffman
     *
     */
    private static class ConceptException extends DKFValidationException{

        private static final long serialVersionUID = 1L;

        private generated.dkf.Concept concept;

        /**
         * Set attributes
         *
         * @param concept the authored concept in the DKF that caused an exception
         * @param reason user friendly message describing why the session is ending
         * @param details a more developer friendly message describing why the session is ending
         * @param dkfilename
         * @param cause
         */
        public ConceptException(generated.dkf.Concept concept, String reason, String details, String dkfilename, Throwable cause){
            super(reason, details, dkfilename, cause);

            if(concept == null){
                throw new IllegalArgumentException("The DKF concept can't be null.");
            }

            this.concept = concept;
        }

        public generated.dkf.Concept getConcept(){
            return concept;
        }

        @Override
        public String toString(){

            StringBuffer sb = new StringBuffer();
            sb.append("[ConditionException: ");
            sb.append(super.toString());
            sb.append(", concept = ").append(getConcept());
            sb.append("]");
            return sb.toString();
        }
    }

    /**
     * Used to contain information about an exception related to configuring a
     * condition for a concept for a DKF.
     *
     * @author mhoffman
     *
     */
    private static class ConditionException extends DKFValidationException{

        private static final long serialVersionUID = 1L;

        private generated.dkf.Condition condition;

        private generated.dkf.Concept parentConcept;

        /**
         * Set attributes.
         *
         * @param condition the authored condition in the DKF that caused an exception
         * @param reason user friendly message describing why the session is ending
         * @param details a more developer friendly message describing why the session is ending
         * @param dkfilename the filename of the DKF that contains the condition
         * @param cause an exception that was caught and which caused this exception.  Can be null.
         */
        public ConditionException(generated.dkf.Condition condition, String reason, String details, String dkfilename, Throwable cause){
            super(reason, details, dkfilename, cause);

            if(condition == null){
                throw new IllegalArgumentException("The DKF condition can't be null.");
            }

            this.condition = condition;

        }

        /**
         * Return the condition authored in the DKF that is causing this exception.
         *
         * @return
         */
        public generated.dkf.Condition getCondition(){
            return condition;
        }

        /**
         * Set the parent concept to the condition that was authored in the DKF.
         *
         * @param parentConcept can't be null.
         */
        public void setParentConcept(generated.dkf.Concept parentConcept){

            if(parentConcept == null){
                throw new IllegalArgumentException("The parent concept can't be null.");
            }

            this.parentConcept = parentConcept;
        }

        /**
         * Return the parent concept to the condition that was authored in the DKF.
         *
         * @return can be null if it wasn't set.
         */
        public generated.dkf.Concept getParentConcept(){
            return parentConcept;
        }

        @Override
        public String toString(){

            StringBuffer sb = new StringBuffer();
            sb.append("[ConditionException: ");
            sb.append(super.toString());
            sb.append(", condition = ").append(getCondition());
            sb.append(", parentConcept = ").append(getParentConcept());
            sb.append("]");
            return sb.toString();
        }
    }
}
