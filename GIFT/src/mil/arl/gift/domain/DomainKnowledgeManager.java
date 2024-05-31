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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.course.TrainingApplication.Options;
import generated.dkf.Actions;
import generated.dkf.StrategyHandler;
import generated.dkf.TeamRef;
import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.DomainAssessmentContent;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.EvaluatorUpdateRequest;
import mil.arl.gift.common.PedagogicalRequest;
import mil.arl.gift.common.PerformanceAssessment;
import mil.arl.gift.common.SurveyResultListener;
import mil.arl.gift.common.aar.util.MessageManager;
import mil.arl.gift.common.course.dkf.DKFValidationException;
import mil.arl.gift.common.course.dkf.DomainActionKnowledge;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.SessionMembers;
import mil.arl.gift.common.course.dkf.session.LessonCompleted;
import mil.arl.gift.common.course.dkf.session.LessonCompleted.LessonCompletedStatusType;
import mil.arl.gift.common.course.dkf.strategy.PerformanceAssessmentStrategy;
import mil.arl.gift.common.course.dkf.team.AbstractTeamUnit;
import mil.arl.gift.common.course.dkf.team.Team;
import mil.arl.gift.common.course.strategy.AbstractStrategy;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy;
import mil.arl.gift.common.course.strategy.DoNothingStrategy;
import mil.arl.gift.common.course.strategy.InstructionalInterventionStrategy;
import mil.arl.gift.common.course.strategy.MidLessonMediaStrategy;
import mil.arl.gift.common.course.strategy.ScenarioAdaptationStrategy;
import mil.arl.gift.common.enums.MessageFeedbackDisplayModeEnum;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.survey.SurveyResponse;
import mil.arl.gift.common.ta.request.TrainingAppInfoRequest;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.domain.knowledge.DomainAssessmentKnowledge;
import mil.arl.gift.domain.knowledge.KnowledgeSessionManager;
import mil.arl.gift.domain.knowledge.Scenario;
import mil.arl.gift.domain.knowledge.common.AbstractPerformanceAssessmentNode;
import mil.arl.gift.domain.knowledge.common.AbstractSurveyLessonAssessment;
import mil.arl.gift.domain.knowledge.common.AssessmentProxy;
import mil.arl.gift.domain.knowledge.common.DomainKnowledgeInterface;
import mil.arl.gift.domain.knowledge.condition.SessionConditionsBlackboardMgr;
import mil.arl.gift.domain.knowledge.conversation.ConversationAssessmentHandlerInterface;
import mil.arl.gift.domain.knowledge.conversation.ConversationManager.ConversationAssessment;
import mil.arl.gift.domain.knowledge.strategy.StrategyAppliedEvent;
import mil.arl.gift.domain.knowledge.strategy.StrategyHandlerInterface;
import mil.arl.gift.domain.knowledge.strategy.StrategyHandlerRequestInterface;
import mil.arl.gift.net.api.message.Message;

/**
 * This class contains all the information about a domain including assessment constraints in a task/concept hierarchy, pedagogical
 * strategies and scenario resources.
 *
 * @author mhoffman
 *
 */
public class DomainKnowledgeManager implements ConversationAssessmentHandlerInterface{

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(DomainKnowledgeManager.class);

    /** DKF reference */
    private final FileProxy knowledgeFile;

    /** training application options (refer to constructor) */
    private final Options options;

    /**
     * the current domain assessment knowledge (i.e. concept hierarchy assessment rules)
     */
    private DomainAssessmentKnowledge domainAssessmentKnowledge = null;

    /**
     * the current domain action knowledge (i.e. tactics to implement based on instructional strategy requests)
     */
    private DomainActionKnowledge domainActionKnowledge = null;

    /** used to notify the domain session about domain knowledge events */
    private DomainKnowledgeActionInterface domainKnowledgeActionInterface;

    /** handler for ped requests (e.g. show feedback) */
    private DomainPedagogicalRequestHandler pedRequestHandler;

    /** handler for domain actions (e.g. scenario completed) */
    private DomainKnowledgeInterface domainKnowledgeInterface;
    
    /** the host domain session information */
    private DomainSession domainSession;
    
//    /** cache strategy until the appropriate training application state has been reached */
//    //TODO: for now only have one strategy cached until the logic for handling multiple strategies, timeouts of stale
//    //      strategies, ordering of cached strategies and sequencing strategies that may need to complete before the next one is applied
//    private CachedStrategy cachedStrategy = null;

    /**
     * Class constructor used when there is a DKF with concepts to be assessed.
     *
     * @param knowledgeFile - the domain knowledge file to parse
     * @param courseFolder the course folder for the course using this DKF.  This is a desktop folder as it represents the runtime
     * course folder not the authored instance.  Can't be null.
     * @param outputFolder  the folder where the output for this session instance is being written too. This could
     * include domain session log, video files, sensor data. Can't be null.
     * @param options - training application options for the execution of this domain (i.e. lesson) [Note: can be null if there are no options]
     * @param domainKnowledgeActionInterface - handler for domain related actions such as notifying the domain session of scenario completed.
     * @param strategyHandlerRequestInterface - handler for Pedagogical strategy requests handler classes that need to request something from the domain module (e.g. send feedback to tutor)
     * @param assessmentProxy - contains references to all assessment nodes in the current course
     * @param domainSession - info about the domain session being assessed.  Can't be null or empty.
     * @throws IOException if there is a problem with resources, i.e. learner action file can't be found or accessed
     * @throws FileValidationException Thrown when there is a problem validating the DKF against the schema
     * @throws DKFValidationException if there was a problem parsing the DKF
     * @throws DetailedException if there was a problem parsing the learner actions or DKF
     */
    public DomainKnowledgeManager(FileProxy knowledgeFile, DesktopFolderProxy courseFolder, DesktopFolderProxy outputFolder, Options options,
            DomainKnowledgeActionInterface domainKnowledgeActionInterface, StrategyHandlerRequestInterface strategyHandlerRequestInterface,
            AssessmentProxy assessmentProxy, DomainSession domainSession) throws FileValidationException, IOException, DKFValidationException, DetailedException{

        if(knowledgeFile == null){
            throw new IllegalArgumentException("The knowledge file can't be null");
        }
        this.knowledgeFile = knowledgeFile;
        
        if(domainSession == null){
            throw new IllegalArgumentException("The domain session can't be null");
        }
        this.domainSession = domainSession;

        DomainDKFHandler dkfh = new DomainDKFHandler(knowledgeFile, courseFolder, outputFolder, true);

        DomainAssessmentKnowledge domainAssessmentKnowledge = dkfh.getDomainAssessmentKnowledge();
        domainActionKnowledge = dkfh.getDomainActionKnowledge();

        //
        // Register performance nodes with AssessmentProxy
        //
        try{
            domainAssessmentKnowledge.registerNodes(assessmentProxy);
        }catch(Exception e){
            throw new ConfigurationException("Unable to register the performance assessment nodes with the assessment proxy.",
                    e.getMessage(),
                    e);
        }        
        
        if(domainKnowledgeActionInterface == null){
            throw new IllegalArgumentException("The domain knowledge action interface can't be null");
        }
        this.domainKnowledgeActionInterface = domainKnowledgeActionInterface;
        
        this.domainAssessmentKnowledge = domainAssessmentKnowledge;
        //handler for domain actions
        domainKnowledgeInterface = new DomainKnowledgeImpl(this.domainKnowledgeActionInterface, this.domainSession, courseFolder, outputFolder);
                
        pedRequestHandler = new DomainPedagogicalRequestHandler(domainActionKnowledge, strategyHandlerRequestInterface, courseFolder, this.domainSession);

        //
        // Training Application Options
        //
        if(options == null){
            //if there are no options, instantiate empty class so we don't have to check for null ptrs every time we
            //want to get an attribute
            options = new Options();
        }
        this.options = options;

        //Give an FYI for ignoring instructional intervention Ped requests so hopefully avoid confusion about why tutoring is not happening during a course
        if(shouldDisableInstructionalInterventions()){
            logger.warn("The domain knowledge is being configured to ignore instructional intervention strategy implementations");
        }
    }

    /**
     * Return the total number of learners the authored scenario object can support.
     *
     * @return 1 is the default if the team organization is not defined, otherwise the team organization hierarchy
     * is analyzed for the number of team members.
     */
    public int getNumberOfPossibleLearners(){

        int possibleLearners = 1;
        Team rootTeam = domainAssessmentKnowledge.getScenario().getRootTeam();
        if(rootTeam != null){
            //want to return 1 if the root team is null to indicate a single individual can play this scenario
            possibleLearners = rootTeam.getNumberOfPlayableTeamMembers();
        }

        return possibleLearners;
    }

    /**
     * Return the DKF file name.
     *
     * @return the file identifier.local disk = absolute path of the file, 
     * "C:/work/GIFT/my.course.xml"server (Nuxeo) = workspace path of the file, "my workspace/clearBldg/my.course.xml"
     */
    public String getKnowledgeFileName() {
        return knowledgeFile.getFileId();
    }

    /**
     * Return the domain assessment knowledge managed by this class
     *
     * @return the container for the assessment logic (e.g. Scenario->Tasks->Concepts->Conditions)
     */
    public DomainAssessmentKnowledge getAssessmentKnowledge(){
        return domainAssessmentKnowledge;
    }

    /**
     * Return the domain action knowledge managed by this class
     *
     * @return the container for instructional strategies and state transitions
     */
    public DomainActionKnowledge getDomainActionKnowledge(){
        return domainActionKnowledge;
    }

    /**
     * Return the score node that contains the scoring hierarchy for task and concepts.
     *
     * @return can be null if there is no scoring information
     */
    public GradedScoreNode getScore(){
        return domainAssessmentKnowledge != null && domainAssessmentKnowledge.getScenario() != null ? 
                domainAssessmentKnowledge.getScenario().getScores() : null;
    }

    /**
     * Return the path of the character to show for feedback in the real-time assessment.
     * This is either a custom defined character, the GIFT default or null (see @return for more details).
     *
     * @param actionKnowledge the action object for a real time assessment, which may contain one or more feedback strategies to analyze.
     * @return the authored character path for feedback in the action object of a real time assessment.  Edge cases:</br>
     * 1. if the action knowledge object provided is null, null is returned</br>
     * 2. if there is feedback authored and no custom character paths defined (means use the default character),
     *    an empty string will be returned.
     * @throws DetailedException if more than one character was found for feedback.  Normally this means either 2 or more custom
     * characters or 1 custom character and the default gift character.
     */
    public static String getCustomDefinedCharacter(DomainActionKnowledge actionKnowledge) throws DetailedException{

        String foundCharacter = null;

        if (actionKnowledge.getActions() == null) {
            return foundCharacter;
        }

        Actions.InstructionalStrategies iStrategies = actionKnowledge.getActions().getInstructionalStrategies();
        if (iStrategies == null) {
            return foundCharacter;
        }

        for (final generated.dkf.Strategy strategy : iStrategies.getStrategy()) {
            for (final Serializable activity : strategy.getStrategyActivities()) {
                if (activity instanceof generated.dkf.InstructionalIntervention) {
                    /* only instructional interventions can currently have
                     * custom characters defined */

                    final generated.dkf.InstructionalIntervention iiStrategy = (generated.dkf.InstructionalIntervention) activity;

                    /* only feedback interventions types can currently have
                     * custom characters defined */

                    final generated.dkf.Feedback feedback = iiStrategy.getFeedback();
                    if (feedback.getFeedbackPresentation() != null) {

                        if (feedback.getFeedbackPresentation() instanceof generated.dkf.MediaSemantics) {

                            final generated.dkf.MediaSemantics msc = (generated.dkf.MediaSemantics) feedback
                                    .getFeedbackPresentation();
                            final String thisCharacter = msc.getAvatar();
                            if (thisCharacter != null && !thisCharacter.isEmpty()) {

                                if (foundCharacter != null && !foundCharacter.equalsIgnoreCase(thisCharacter)) {
                                    String errorMsg = "GIFT only supports a single character per real time assessment but more than one character was found.\n";
                                    if (foundCharacter.isEmpty()) {
                                        errorMsg += "1. the GIFT default character.";
                                    } else {
                                        errorMsg += "1. '" + foundCharacter + "'.";
                                    }

                                    errorMsg += "\n2. '" + thisCharacter + "'.";

                                    throw new DetailedException(
                                            "Found more than one character defined in the Real-time assessment file.",
                                            errorMsg, null);
                                }

                                foundCharacter = thisCharacter;
                            }
                        } else if (feedback.getFeedbackPresentation() instanceof generated.dkf.Message) {
                            /* check if the feedback is to be presented in the
                             * TUI (not the training app) */

                            final generated.dkf.Message message = (generated.dkf.Message) feedback
                                    .getFeedbackPresentation();
                            final generated.dkf.Message.Delivery delivery = message.getDelivery();
                            if (delivery != null) {

                                if (delivery.getInTutor() != null) {

                                    if (foundCharacter != null && !foundCharacter.equals(Constants.EMPTY)) {
                                        /* the feedback is to be presented in
                                         * the TUI and a custom character was
                                         * defined elsewhere in the DKF but this
                                         * feedback would be using the default
                                         * GIFT character not the custom
                                         * character */

                                        throw new DetailedException(
                                                "Found that more than one character could be used in the Real-time assessment.",
                                                "GIFT only supports a single character per real time assessment but more than one character could be used"
                                                        + ".\n1. the GIFT default character\n2. Custom Defined Character: "
                                                        + foundCharacter,
                                                null);
                                    } else if(delivery.getInTutor().getMessagePresentation() == null || !delivery.getInTutor().getMessagePresentation().equals(MessageFeedbackDisplayModeEnum.TEXT_ONLY.getName())) {
                                        /* the feedback either doesn't have message presentation defined and therefore 
                                         * will rely on the default presentation which is in-tutor + character speaks -OR-
                                         * a message presentation that includes the character has been specified, either way use 
                                         * empty string as an indicator of the gift default character */
                                        foundCharacter = Constants.EMPTY;
                                    }
                                }
                            }else{
                                // the default for now delivery specified is in-tutor + character speaks
                                
                                /* use empty string as an indicator of
                                 * the gift default character */
                                foundCharacter = Constants.EMPTY;
                            }
                        }
                    }
                }
            }
        }

        return foundCharacter;
    }

    /**
     * Return the path of the character to show for feedback in the real-time assessment.
     * This is either a custom defined character, the GIFT default or null (see @return for more details).
     *
     * @return the authored character path for feedback in the action object of a real time assessment.  Edge cases:</br>
     * 1. if the action knowledge object for this class is null, null is returned</br>
     * 2. if there is feedback authored and no custom character paths defined (means use the default character),
     *    an empty string will be returned.
     * @throws DetailedException if more than one character was found for feedback.  Normally this means either 2 or more custom
     * characters or 1 custom character and the default gift character.
     */
    public String getCustomDefinedCharacter() throws DetailedException{
        return DomainKnowledgeManager.getCustomDefinedCharacter(domainActionKnowledge);

    }

    /**
     * Load the scenario provided in the domain knowledge.  If this method was called
     * before on this instance an exception will be thrown.<br/>
     * This should be called before {@link #start()}.<br/>
     * Note: only the host session should call this method.  This way there is only
     * one running assessment for an individual or collective knowledge assessment session.
     * @param playbackMessages optional list of playback messages that if populated contain all of the messages
     * that are about to be played back in actual recorded time sequence as part of this domain knowledge part of the course.  Can be null or empty.</br>
     * In the future this should be a stream that is not directly associated with
     * the collection of messages being played back.  Until then, callers should
     * NOT manipulate this collection.
     */
    public void loadScenario(final List<MessageManager> playbackMessages){

        if(logger.isInfoEnabled()){
            logger.info("Loading new domain knowledge");
        }
        
        domainKnowledgeInterface.setPlaybackMessages(playbackMessages);
        
        if(domainAssessmentKnowledge.load(domainKnowledgeInterface)){
            if(logger.isInfoEnabled()){
                logger.info("Domain knowledge loaded successfully");
            }
            
            SessionConditionsBlackboardMgr.getInstance().getSessionBlackboard(domainSession.getDomainSessionId()).setTrainingAppAvailable(domainSession.isGatewayConnected());
        }else{
            //raise an error higher up because the domain session scenario is now corrupt/incomplete
            logger.error("Domain knowledge failed to load");
            domainKnowledgeInterface.fatalError("Domain knowledge failed to load", "There was a problem loading the domain knowledge.");
        }
    }

    /**
     * Stop assessing the domain
     *
     * @param reason The reason for terminating the domain knowledge. Used for logging purposes. Can't be null or empty.
     * @param status information about why the domain knowledge session is ending.
     */
    public void terminate(String reason, LessonCompletedStatusType status){

        if(reason == null || reason.isEmpty()){
            throw new IllegalArgumentException("The reason can't be null or empty.");
        }

        if(domainAssessmentKnowledge != null){
            //terminate assessing the domain

            if(logger.isInfoEnabled()){
                logger.info("Terminating previous domain knowledge because "+reason);
            }

            domainAssessmentKnowledge.terminate(status);
        } 

    }
    
    /**
     * This instance will no longer be used.  Release references to objects that were created
     * outside of this class, used by inner classes and are inner classes. 
     */
    public void cleanup(){
        domainKnowledgeInterface = null;
        domainActionKnowledge = null;
        domainAssessmentKnowledge.cleanup();
        domainAssessmentKnowledge = null;
        domainKnowledgeActionInterface = null;
        pedRequestHandler.cleanup();
        pedRequestHandler = null;
    }
    
    /**
     * Notification that the strategy with the name was just applied.  Can be used to notify
     * domain knowledge task triggers.
     * 
     * @param event contains information about the strategy that was applied.  Can't be null.
     */
    public void appliedStrategyNotification(StrategyAppliedEvent event){
        
        if(domainAssessmentKnowledge != null){
            domainAssessmentKnowledge.appliedStrategyNotification(event);
        }
    }
    
    /**
     * Start the domain assessment knowledge before receiving a single training application message.
     * This should be called after {@link #loadScenario()}.
     */
    public void start(){
        if(logger.isInfoEnabled()){
            logger.info("Starting domain knowledge manager");
        }
        domainAssessmentKnowledge.start();
    }

    /**
     * Process the training application game state message received.  The message usually comes
     * from the Tutor or the Gateway modules.
     *
     * @param message - the training application game state message to handle
     * @return PerformanceAssessment - an assessment created as a result of the game state message<br/>
     * Notes:<br/>
     *        i. Return null to indicate that the assessment value hasn't changed for the domain assessment knowledge
     *           from the last reported value.<br/>
     *       ii. Return Above/At/Below as a result of domain assessment knowledge. It is acceptable
     *           to return the same value back to back as an indication that the first value is independent
     *           of the second value.<br/>
     *           For example, every time a user presses 'button A' a Below assessment value
     *           is returned for a Condition.  Therefore the first and second time that 'button A' is pressed
     *           are different events but with the same back-to-back reported assessment value.
     */
    public PerformanceAssessment handleTrainingAppGameStateMessage(Message message){

        PerformanceAssessment performanceAssessment = null;
        if(domainAssessmentKnowledge != null){
            performanceAssessment = domainAssessmentKnowledge.trainingAppGameStateMessageReceived(message);

            if(logger.isDebugEnabled() && performanceAssessment != null){
                logger.debug("Created new "+performanceAssessment);
            }
        }

        return performanceAssessment;
    }

    /**
     * Build an updated performance assessment using the metrics provided within the request.
     *
     * @param request the request containing the updated metrics for a specific task or concept.
     */
    public void handleEvaluatorUpdateRequest(EvaluatorUpdateRequest request) {
        if (domainAssessmentKnowledge != null) {
            domainAssessmentKnowledge.evaluatorUpdateRequestReceived(request);
        }
    }

    /**
     * Return whether or not the domain knowledge was configured to ignore instructional intervention Requests (e.g. Feedback)
     * (i.e. disable strategy implementation(s))
     *
     * @return boolean
     */
    private boolean shouldDisableInstructionalInterventions(){

        if(options.getDisableInstInterImpl() != null){
            return Boolean.valueOf(options.getDisableInstInterImpl().value());
        }

        return false;
    }
    
    /**
     * Whether the course author has marked that remediation should be allowed after the training application DKF
     * is done.
     * 
     * @return true if remediation can be presented if needed after this domain knowledge / scenario is finished being assessed.
     */
    public boolean shouldAllowRemediation(){        
        return options.getRemediation() != null;
    }
    
    /**
     * Return the remediation options for this assessed scenario.  Options may include
     * whether remediation is enabled, should the scenario be started again after remediation and
     * how many times can remediation be given before ending the course prematurely.
     * 
     * @return can be null if not authored.
     */
    public generated.course.TrainingApplication.Options.Remediation getRemediationOptions(){
        return options.getRemediation();
    }

    /**
     * Return an object that contains information about an initial avatar to show when the training application
     * transition has started.
     *
     * @return Object - information about the avatar to display.  Can be null if no avatar should be displayed.
     */
    public Object getInitialAvatar(){

        if(options.getShowAvatarInitially() != null){
            return options.getShowAvatarInitially().getAvatarChoice();
        }

        return null;
    }

    /**
     * A pedagogical request was received and need to be handled by the domain knowledge
     *
     * @param request - request to handle
     */
    public void handlePedagogicalRequest(PedagogicalRequest request){
        pedRequestHandler.handlePedagogicalRequest(request, domainActionKnowledge);
    }

    /**
     * Executes an individual strategy activity.
     *
     * @param activity The activity to execute. Can't be null.
     * @throws Exception If there was a problem determining if the activity was
     *         applicable to this domain session.
     */
    public void executeActivity(AbstractStrategy activity) throws Exception {
        if (activity == null) {
            throw new IllegalArgumentException("The parameter 'activity' cannot be null.");
        }

        pedRequestHandler.executeActivity(activity, isActivityApplicable(activity));
    }

    /**
     * Determines if a strategy activity applies (should be executed) to this
     * domain session.
     *
     * @param activity The activity to test for applicability. Can't be null.
     * @return True if the activity applies to this domain session, false
     *         otherwise.
     * @throws Exception If a team member was specified for the activity but
     *         this session has no team structure defined.
     */
    private boolean isActivityApplicable(AbstractStrategy activity) throws Exception {
        if (activity == null) {
            throw new IllegalArgumentException("The parameter 'activity' cannot be null.");
        }

        Scenario scenario = domainAssessmentKnowledge.getScenario();
        Team rootTeam = scenario.getRootTeam();

        /* Fetch the list of TeamRefs for the current activity */
        List<TeamRef> teamRefs = null;
        if (activity instanceof InstructionalInterventionStrategy) {
            InstructionalInterventionStrategy instructionalIntervention = (InstructionalInterventionStrategy) activity;
            teamRefs = instructionalIntervention.getFeedback().getTeamRef();
        }

        /* If a list of TeamRefs were found, see if it contains a teamRef that
         * is applicable to this scenario. */
        if (teamRefs != null && !teamRefs.isEmpty()) {

            if (rootTeam == null) {
                throw new Exception("Unable to apply the activity '" + activity + "' because it"
                        + " references team organization elements but the team organization has not been defined in the real time assessment.");
            } else if (scenario.getLearnerTeamMember() == null) {
                throw new Exception("Unable to apply the activity '" + activity + "' because it"
                        + " references team organization elements but the learner's team member has not been set in the real time assessment logic.");
            }

            for (TeamRef teamRef : teamRefs) {
                String teamVal = teamRef.getValue();

                /* See if the strategy's list of applicable team element names
                 * includes the team member name of the current learner
                 * directly */
                if (StringUtils.equals(teamVal, scenario.getLearnerTeamMember().getName())) {
                    return true;
                }

                /* See if the strategy's list of applicable team element names
                 * includes a team of which this learner is a team member of */
                AbstractTeamUnit teamElement = rootTeam.getTeamElement(teamVal);
                if (teamElement == null) {
                    throw new Exception("Unable to apply the activity '" + activity
                            + "' because one of the activities references a team organization element named '"
                            + teamVal
                            + " which doesn't exist in the team organization defined in the real time assessment.");
                }

                if (teamElement instanceof Team
                        && ((Team) teamElement).getTeamElement(scenario.getLearnerTeamMember().getName()) != null) {
                    return true;
                }
            }
        } else {
            return true;
        }

        return false;
    }

    /**
     * Handle the survey results by assessing the answers.
     *
     * @param surveyResponse - survey results to assess
     */
    public void handleSurveyResults(SurveyResponse surveyResponse){

//        Map<Integer, AbstractPerformanceAssessmentNode> nodes = domainAssessmentKnowledge.getScenario().getPerformanceNodes();
//        for(AbstractPerformanceAssessmentNode node : nodes.values()){
//            node.handleSurveyResults(surveyResponse);
//        }
    }

    @Override
    public void assessPerformanceFromConversation(List<ConversationAssessment> assessments){

        if(domainAssessmentKnowledge != null){
            domainAssessmentKnowledge.handleConversationAssessment(assessments);
        }
    }

    /**
     * Implementation of the Domain module pedagogical request handler that provides logic for dealing
     * with performance assessment request (among other things).
     *
     * @author mhoffman
     *
     */
    private class DomainPedagogicalRequestHandler extends AbstractPedagogicalRequestHandler{

        /**
         * Class constructor - set attributes
         *
         * @param domainActionKnowledge  - the action knowledge used to handle pedagogical requests
         * @param strategyHandlerRequestInterface - the callback interface to handle implementation of pedagogical request via the domain module
         * @param courseFolder the course folder for the course using the DKF with the pedagogical tactics to apply
         * @param domainSession - info about the domain session being assessed.  Can't be null or empty.
         */
        public DomainPedagogicalRequestHandler(DomainActionKnowledge domainActionKnowledge,
                StrategyHandlerRequestInterface strategyHandlerRequestInterface, DesktopFolderProxy courseFolder, DomainSession domainSession) {
            super(domainActionKnowledge, strategyHandlerRequestInterface, courseFolder, domainSession);
        }

        @Override
        protected boolean shouldIgnoreInstructionalInterventions() {
            return shouldDisableInstructionalInterventions();
        }
        
        @Override
        protected boolean isRemediationEnabled(){
            return shouldAllowRemediation();
        }

        @Override
        protected void determinePerformanceAssessment(PerformanceAssessmentStrategy performanceAssessment){

            Serializable assessmentType = performanceAssessment.getAssessmentType();

            if(assessmentType instanceof generated.dkf.Conversation){
                generated.dkf.Conversation conversation = (generated.dkf.Conversation)assessmentType;

                StrategyHandler handler = performanceAssessment.getHandlerInfo();
                if(handler != null){
                    //found a custom handler for the assessment request, call upon it to do the assessment instead

                    StrategyHandlerInterface handlerInterface = getHandler(handler.getImpl());
                    if(handlerInterface != null){
                        handlerInterface.handleRequestForPerformanceAssessment(conversation, strategyHandlerRequestInterface);
                    }else{
                        logger.error("unable to implement on selected request for performance assessment of "+performanceAssessment);
                    }
                }

            }else if(assessmentType instanceof generated.dkf.PerformanceAssessment.PerformanceNode){
                generated.dkf.PerformanceAssessment.PerformanceNode perfNode = (generated.dkf.PerformanceAssessment.PerformanceNode)assessmentType;

                //find the performance assessment node needing further assessment
                Map<Integer, AbstractPerformanceAssessmentNode> nodes = domainAssessmentKnowledge.getScenario().getPerformanceNodes();
                AbstractPerformanceAssessmentNode node = nodes.get(perfNode.getNodeId().intValue());

                if(node == null){
                    logger.error("The performance assessment strategy of "+performanceAssessment+" will not be applied because unable to find a performance node with id = "+perfNode.getNodeId());
                }else{

                    StrategyHandler handler = performanceAssessment.getHandlerInfo();
                    if(handler == null){
                        node.handlePerformanceAssessmentRequest(strategyHandlerRequestInterface);
                    }else{
                        //found a custom handler for the assessment request, call upon it to do the assessment instead

                        StrategyHandlerInterface handlerInterface = getHandler(handler.getImpl());
                        if(handlerInterface != null){
                            handlerInterface.handleRequestForPerformanceAssessment(node, strategyHandlerRequestInterface);
                        }else{
                            logger.error("unable to implement on selected request for performance assessment of "+performanceAssessment);
                        }
                    }
                }
            }
        }
        

        /**
         * Executes a single activity within a {@link Strategy}.
         *
         * @param activity The strategy activity to execute. Can't be null.
         * @param executeForThisSession Whether or not this domain session should
         *        execute the activity.
         */
        public void executeActivity(AbstractStrategy activity, boolean executeForThisSession) {
            if (activity == null) {
                throw new IllegalArgumentException("The parameter 'activity' cannot be null.");
            }

            /* Determine if this domain session is a joiner */
            int dsId = domainSession.getDomainSessionId();
            KnowledgeSessionManager ksm = KnowledgeSessionManager.getInstance();
            boolean isJoiner = ksm.isMemberOfTeamKnowledgeSession(dsId) != null;

            if (executeForThisSession) {
                if (activity instanceof InstructionalInterventionStrategy) {

                    if (!shouldIgnoreInstructionalInterventions()) {
                        determineInstructionalIntervention((InstructionalInterventionStrategy) activity);
                    } else {
                        if (logger.isInfoEnabled()) {
                            logger.info(
                                    "The ped request was ignored because the domain knowledge was configured to ignore strategy implementations");
                        }
                    }

                } else if (activity instanceof MidLessonMediaStrategy) {
                    determineMidLessonMedia((MidLessonMediaStrategy) activity);
                } else if (activity instanceof ScenarioAdaptationStrategy) {
                    determineScenarioAdaptation((ScenarioAdaptationStrategy) activity);
                } else if (activity instanceof PerformanceAssessmentStrategy) {
                    determinePerformanceAssessment((PerformanceAssessmentStrategy) activity);
                } else if (activity instanceof BranchAdaptationStrategy) {
                    determineBranchAdaptation((BranchAdaptationStrategy) activity);
                } else if (activity instanceof DoNothingStrategy) {
                    determineDoNothingAction((DoNothingStrategy) activity);
                } else {
                    logger.error("A handler for the strategy activity " + activity + " has not been implemented.");
                }
            }

            /* Pause this tactic handling thread so other tactics in this ped
             * request are handled afterwards only if this */
            if (activity.getDelayAfterStrategy() > 0 && !isJoiner) {
                try {
                    if (logger.isInfoEnabled()) {
                        logger.info("Ped request tactic is having the handling thread sleep for "
                                + activity.getDelayAfterStrategy() + " seconds.");
                    }

                    long sleepTimeInMillis = (long) (activity.getDelayAfterStrategy() * 1000);
                    Thread.sleep(sleepTimeInMillis);
                } catch (InterruptedException e) {
                    logger.error("An exception was thrown while waiting for a delayed tactic to finish.  The tatic is "
                            + activity, e);
                }
            }
        }
    }

    /**
     * Generate a new unique id to be used for a performance assessment node.
     *
     * @return UUID the id object created
     */
    public static UUID getNewPerformanceAssessmentNodeUUID(){
        return UUID.randomUUID();
    }
    
    /**
     * Handles notifications of scenario events such as performance assessments and scenario completion.
     * 
     * @author mhoffman
     *
     */
    private static class DomainKnowledgeImpl implements DomainKnowledgeInterface{
        
        /** used to communicate events caused in the scenario to BaseDomainSession */
        private DomainKnowledgeActionInterface domainKnowledgeActionInterface;
        
        /** the host domain session information */
        private DomainSession domainSession;
        
        /**
         * the course folder for the course using this DKF.  This is a desktop folder as it represents the runtime
         * course folder not the authored instance.  Can't be null.
         */
        @SuppressWarnings("unused")
        private DesktopFolderProxy courseFolder;
        
        /**
         * the folder where the output for this session instance is being written too. This could
         * include domain session log, video files, sensor data.  Can't be null.
         */
        @SuppressWarnings("unused")
        private DesktopFolderProxy outputFolder;
        
        /**
         * optional list of playback messages that if populated contain all of the messages
         * that are about to be played back in actual recorded time sequence as part of this 
         * domain knowledge part of the course.  Can be null or empty.</br>
         * In the future this should be a stream that is not directly associated with
         * the collection of messages being played back.  Until then, callers should
         * NOT manipulate this collection.
         */
        private List<MessageManager> playbackMessages;
        
        /**
         * Set attributes
         * 
         * @param domainKnowledgeActionInterface used to communicate events caused in the scenario to BaseDomainSession.  Can't be null.
         * @param domainSession the host domain session information.  Can't be null.
         * @param courseFolder the course folder for the course using this DKF.  This is a desktop folder as it represents the runtime
         * course folder not the authored instance.  Can't be null.
         * @param outputFolder the folder where the output for this session instance is being written too. This could
         * include domain session log, video files, sensor data.  Can't be null.
         */
        public DomainKnowledgeImpl(DomainKnowledgeActionInterface domainKnowledgeActionInterface, 
                DomainSession domainSession, DesktopFolderProxy courseFolder, DesktopFolderProxy outputFolder){
            
            if(domainKnowledgeActionInterface == null){
                throw new IllegalArgumentException("The domain knowledge action interface can't be null");
            }
            this.domainKnowledgeActionInterface = domainKnowledgeActionInterface;
            
            if(domainSession == null){
                throw new IllegalArgumentException("The domain session can't be null");
            }
            this.domainSession = domainSession;
            
            if(courseFolder == null){
                throw new IllegalArgumentException("The course folder can't be null.");
            }

            this.courseFolder = courseFolder;
            
            if(outputFolder == null){
                throw new IllegalArgumentException("The output folder can't be null.");
            }

            this.outputFolder = outputFolder;
        }
        
        @Override
        public void domainStarted() {
            domainKnowledgeActionInterface.scenarioStarted();
        }
        
        @Override
        public void domainEnded(LessonCompletedStatusType status) {
            LessonCompleted lessonCompleted = new LessonCompleted(status);
            domainKnowledgeActionInterface.scenarioCompleted(lessonCompleted);                
        }
        
        @Override
        public void performanceAssessmentCreated(PerformanceAssessment performanceAssessment){
            
            if (logger.isDebugEnabled()) {
                logger.debug("Domain Knowledge manager's interface has created an out-of-game-state-sequence performance assessment to send out:\n"+performanceAssessment);
            }
            
            domainKnowledgeActionInterface.performanceAssessmentCreated(performanceAssessment);
        }

        @Override
        public void trainingApplicationRequest(TrainingAppInfoRequest infoRequest) {
            domainKnowledgeActionInterface.trainingApplicationRequest(infoRequest);
        }

        @Override
        public void handleDomainActionWithLearner(DomainAssessmentContent information) {
            domainKnowledgeActionInterface.handleDomainActionWithLearner(information);
        }

        @Override
        public void fatalError(String reason, String details) {
            domainKnowledgeActionInterface.fatalError(reason, details);                
        }

        @Override
        public void displayDuringLessonSurvey(
                AbstractSurveyLessonAssessment surveyAssessment,
                SurveyResultListener surveyResultListener) {
            domainKnowledgeActionInterface.displayDuringLessonSurvey(surveyAssessment, surveyResultListener);                
        }

        @Override
        public SessionMembers getSessionMembers() {
            return KnowledgeSessionManager.getInstance().getKnowledgeSessionMembers(domainSession.getDomainSessionId());
        }

        @Override
        public List<MessageManager> getPlaybackMessages() {
            return playbackMessages;
        }

        @Override
        public void setPlaybackMessages(List<MessageManager> playbackMessages) {
            this.playbackMessages = playbackMessages;
        }

    }

//    private class CachedStrategy{
//
//        private AbstractStrategy strategy;
//
//        private TrainingApplicationStateEnum desiredState;
//
//        public CachedStrategy(AbstractStrategy strategy, TrainingApplicationStateEnum desiredState){
//            this.strategy = strategy;
//            this.desiredState = desiredState;
//        }
//
//        public AbstractStrategy getStrategy(){
//            return strategy;
//        }
//
//        public TrainingApplicationStateEnum getDesiredState(){
//            return desiredState;
//        }
//    }

}
