/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.apache.commons.io.IOUtils;
import org.imsglobal.lti.BasicLTIConstants;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.course.AuthoredBranch;
import generated.course.AuthoredBranch.Paths.Path;
import generated.course.BooleanEnum;
import generated.course.CustomParameters;
import generated.course.DISInteropInputs;
import generated.course.DkfRef;
import generated.course.FixedDecayMandatoryBehavior;
import generated.course.Guidance;
import generated.course.Interop;
import generated.course.InteropInputs;
import generated.course.LessonMaterialList;
import generated.course.LtiProperties;
import generated.course.LtiProvider;
import generated.course.Media;
import generated.course.Nvpair;
import generated.course.PresentSurvey.ConceptSurvey;
import generated.course.SimpleMandatoryBehavior;
import generated.course.TrainingApplication;
import generated.dkf.ScenarioControls;
import generated.dkf.TutorMeParams;
import mil.arl.gift.common.AbstractAfterActionReviewEvent;
import mil.arl.gift.common.AbstractDisplayContentTutorRequest;
import mil.arl.gift.common.AbstractPedagogicalRequest;
import mil.arl.gift.common.AbstractRuntimeParameters;
import mil.arl.gift.common.AfterActionReviewCourseEvent;
import mil.arl.gift.common.AfterActionReviewRemediationEvent;
import mil.arl.gift.common.AfterActionReviewSurveyEvent;
import mil.arl.gift.common.ApplyStrategyLearnerAction;
import mil.arl.gift.common.AvatarData;
import mil.arl.gift.common.BranchPathHistory;
import mil.arl.gift.common.ChatLog;
import mil.arl.gift.common.ClearTextAction;
import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.CourseState;
import mil.arl.gift.common.CourseState.ExpandableCourseObjectStateEnum;
import mil.arl.gift.common.CourseState.RequiredLearnerStateAttributes;
import mil.arl.gift.common.CourseState.RequiredLearnerStateAttributes.AttributeValues;
import mil.arl.gift.common.CourseState.RequiredLearnerStateAttributes.ConceptAttributeValues;
import mil.arl.gift.common.DisplayAvatarAction;
import mil.arl.gift.common.DisplayChatWindowRequest;
import mil.arl.gift.common.DisplayChatWindowUpdateRequest;
import mil.arl.gift.common.DisplayCourseInitInstructionsRequest;
import mil.arl.gift.common.DisplayCourseInitInstructionsRequest.GatewayStateEnum;
import mil.arl.gift.common.DisplayMessageTutorRequest;
import mil.arl.gift.common.DisplayTextToSpeechAvatarAction;
import mil.arl.gift.common.DomainAssessmentContent;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.EvaluatorUpdateRequest;
import mil.arl.gift.common.FinishScenario;
import mil.arl.gift.common.GetExperimentRequest;
import mil.arl.gift.common.GetKnowledgeAssessmentSurveyRequest;
import mil.arl.gift.common.GetSurveyRequest;
import mil.arl.gift.common.InitializePedagogicalModelRequest;
import mil.arl.gift.common.InstantiateLearnerRequest;
import mil.arl.gift.common.InteropConnectionsInfo;
import mil.arl.gift.common.LMSConnectionInfo;
import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.common.LMSData;
import mil.arl.gift.common.LearnerTutorAction;
import mil.arl.gift.common.LoSQuery;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.PedagogicalRequest;
import mil.arl.gift.common.PerformanceAssessment;
import mil.arl.gift.common.PlayAudioAction;
import mil.arl.gift.common.PublishLessonScoreResponse;
import mil.arl.gift.common.RequestBranchAdaptation;
import mil.arl.gift.common.Siman;
import mil.arl.gift.common.SurveyResultListener;
import mil.arl.gift.common.TutorMeLearnerTutorAction;
import mil.arl.gift.common.TutorUserInterfaceFeedback;
import mil.arl.gift.common.WebClientInformation;
import mil.arl.gift.common.aar.LogFilePlaybackService;
import mil.arl.gift.common.aar.util.MessageManager;
import mil.arl.gift.common.course.AbstractCourseHandler;
import mil.arl.gift.common.course.CourseConceptsUtil;
import mil.arl.gift.common.course.CourseFileValidationException;
import mil.arl.gift.common.course.CourseRecordRef;
import mil.arl.gift.common.course.GIFTValidationResults;
import mil.arl.gift.common.course.TrainingAppUtil;
import mil.arl.gift.common.course.dkf.AbstractDKFHandler;
import mil.arl.gift.common.course.dkf.DKFValidationException;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.ObserverControls;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.SessionType;
import mil.arl.gift.common.course.dkf.session.KnowledgeAssessmentDetails;
import mil.arl.gift.common.course.dkf.session.LessonCompleted;
import mil.arl.gift.common.course.dkf.session.LessonCompleted.LessonCompletedStatusType;
import mil.arl.gift.common.course.dkf.session.ManageTeamMembershipException;
import mil.arl.gift.common.course.dkf.session.ManageTeamMembershipRequest;
import mil.arl.gift.common.course.dkf.session.Mission;
import mil.arl.gift.common.course.dkf.session.SessionMember;
import mil.arl.gift.common.course.dkf.session.TeamKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.SessionMember.GroupMembership;
import mil.arl.gift.common.course.dkf.session.SessionMember.IndividualMembership;
import mil.arl.gift.common.course.dkf.team.AbstractTeamUnit;
import mil.arl.gift.common.course.dkf.team.Team;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.course.strategy.AbstractStrategy;
import mil.arl.gift.common.course.strategy.ApplyStrategies;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.AbstractRemediationConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.RemediationInfo;
import mil.arl.gift.common.course.strategy.StrategySet;
import mil.arl.gift.common.course.strategy.StrategyToApply;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.ExpertiseLevelEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.LessonLevelEnum;
import mil.arl.gift.common.enums.LessonStateEnum;
import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.enums.TrainingApplicationStateEnum;
import mil.arl.gift.common.experiment.DataCollectionItem;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.CharacterServiceUtil;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.io.UriUtil;
import mil.arl.gift.common.io.UriUtil.InternetConnectionStatusEnum;
import mil.arl.gift.common.module.GatewayModuleStatus;
import mil.arl.gift.common.module.ModuleStatus;
import mil.arl.gift.common.module.ModuleStatusListener;
import mil.arl.gift.common.module.ModuleStatusMonitor;
import mil.arl.gift.common.module.ModuleStatusMonitor.StatusReceivedInfo;
import mil.arl.gift.common.score.DefaultRawScore;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.score.RawScore;
import mil.arl.gift.common.score.RawScoreNode;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractQuestionResponse;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.ListOption;
import mil.arl.gift.common.survey.MultipleChoiceQuestion;
import mil.arl.gift.common.survey.MultipleChoiceSurveyQuestion;
import mil.arl.gift.common.survey.OptionList;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.Survey.SurveyTypeEnum;
import mil.arl.gift.common.survey.SurveyGiftData;
import mil.arl.gift.common.survey.SurveyItemProperties;
import mil.arl.gift.common.survey.SurveyPage;
import mil.arl.gift.common.survey.SurveyProperties;
import mil.arl.gift.common.survey.SurveyResponse;
import mil.arl.gift.common.survey.SurveyResponseMetadata;
import mil.arl.gift.common.survey.TextSurveyElement;
import mil.arl.gift.common.survey.score.AttributeScorerProperties;
import mil.arl.gift.common.survey.score.ScoreInterface;
import mil.arl.gift.common.survey.score.SurveyFeedbackScorer;
import mil.arl.gift.common.survey.score.SurveyScorer;
import mil.arl.gift.common.survey.score.SurveyScorerManager;
import mil.arl.gift.common.survey.score.TotalScorer;
import mil.arl.gift.common.ta.request.ApplicationMessage;
import mil.arl.gift.common.ta.request.TrainingAppInfoRequest;
import mil.arl.gift.common.ta.request.VariablesStateRequest;
import mil.arl.gift.common.ta.state.GenericJSONState;
import mil.arl.gift.common.usersession.LtiUserSessionDetails;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.domain.DomainCourseFileHandler.InteropImplementationsNeeded;
import mil.arl.gift.domain.course.CourseObjectWrapper;
import mil.arl.gift.domain.course.DynamicContentHandler;
import mil.arl.gift.domain.course.DynamicContentHandler.CourseComprehensionException;
import mil.arl.gift.domain.course.LMSProgressReporter;
import mil.arl.gift.domain.course.LessonMaterialHandler;
import mil.arl.gift.domain.course.MerrillsBranchPointHandler;
import mil.arl.gift.domain.course.MerrillsBranchPointHandler.ExampleCourseObject;
import mil.arl.gift.domain.course.MerrillsBranchPointHandler.PracticeCourseObject;
import mil.arl.gift.domain.course.MerrillsBranchPointHandler.RecallCourseObject;
import mil.arl.gift.domain.course.MerrillsBranchPointHandler.RemediationCourseObject;
import mil.arl.gift.domain.course.MerrillsBranchPointHandler.RuleCourseObject;
import mil.arl.gift.domain.course.PathConditionEvaluator;
import mil.arl.gift.domain.course.RemoteZipGenerator;
import mil.arl.gift.domain.course.TrainingApplicationHandler;
import mil.arl.gift.domain.knowledge.Concept;
import mil.arl.gift.domain.knowledge.CourseConceptSearchFilter;
import mil.arl.gift.domain.knowledge.IntermediateConcept;
import mil.arl.gift.domain.knowledge.KnowledgeSessionManager;
import mil.arl.gift.domain.knowledge.Scenario;
import mil.arl.gift.domain.knowledge.Task;
import mil.arl.gift.domain.knowledge.common.AbstractPerformanceAssessmentNode;
import mil.arl.gift.domain.knowledge.common.AbstractSurveyLessonAssessment;
import mil.arl.gift.domain.knowledge.common.AssessmentProxy;
import mil.arl.gift.domain.knowledge.common.AssessmentStrategy;
import mil.arl.gift.domain.knowledge.common.AutoTutorSurveyLessonAssessment;
import mil.arl.gift.domain.knowledge.common.GIFTSurveyLessonAssessment;
import mil.arl.gift.domain.knowledge.condition.autotutor.AutoTutorWebServiceInterface.ActionsResponse;
import mil.arl.gift.domain.knowledge.conversation.ConversationAssessmentHandlerInterface;
import mil.arl.gift.domain.knowledge.conversation.ConversationManager.ConversationAssessment;
import mil.arl.gift.domain.knowledge.conversation.ConversationTreeFileHandler;
import mil.arl.gift.domain.knowledge.conversation.ConversationTreeManager.ConversationTreeAction;
import mil.arl.gift.domain.knowledge.conversation.ConversationTreeManager.ConversationTreeActions;
import mil.arl.gift.domain.knowledge.strategy.StrategyAppliedEvent;
import mil.arl.gift.domain.knowledge.strategy.StrategyHandlerRequestInterface;
import mil.arl.gift.domain.learneraction.LearnerActionsManager;
import mil.arl.gift.domain.lessonmaterial.LessonMaterialFileHandler;
import mil.arl.gift.domain.lessonmaterial.MidLessonMediaManager;
import mil.arl.gift.net.api.MessageCollectionCallback;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.NACK;
import mil.arl.gift.net.api.message.codec.json.survey.QuestionJSON;
import mil.arl.gift.net.api.message.codec.json.survey.SurveyJSON;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.signature.OAuthSignatureMethod;

/**
 * Encapsulates the concept of a domain session (i.e. course) for a single user.  This class orchestrates
 * the various course transitions as well as the interaction between the domain module
 * and other modules.  A new instance of this class will be created for each user for each course execution.
 *
 * @author jleonard
 */
public class BaseDomainSession implements DomainKnowledgeActionInterface, StrategyHandlerRequestInterface, DeadReckonedEntityMessageHandler {

    /** instance of the logger */
    private static final Logger logger = LoggerFactory.getLogger(BaseDomainSession.class);

    private static final String SAVING_SURVEY_RESULTS_MSG = "Saving survey results, please wait.";

    private static final String MAX_ATTEMPTS_MSG = "<p style=\"text-align: center; \"><span style=\"font-size: 36px;\">Maximum number of attempts reached.</span><br></p><p style=\"text-align: center; \">"
            + "<img style=\"width: 25%;\" src=\"images/gift_open.png\"></p><p style=\"text-align: center; \">" + org.apache.commons.lang3.StringUtils.replace(MerrillsBranchPointHandler.BAILOUT_DETAILS_MSG,  Constants.NEWLINE,  Constants.HTML_NEWLINE) + "</p>"
            + "<p><br></p>";

    // OAuth Message constants
    private static final String POST = "POST";
    private static final String LEARNER = "Learner";
    private static final String OAUTH_CALLBACK_VALUE = "about:blank";


    /** label for the Yes choice on the survey created to skip a course object */
    static final String DONT_SKIP_CHOICE = "Take it anyway";
    
    /** label for the No choice on the survey created to skip a course object */
    static final String SKIP_CHOICE = "Skip";

    /** The delimiter used to separate the components in the LTI source id */
    private static final String LTI_SOURCE_DELIM = ":";

    /**
     * used to track the various states of the domain session as managed by this class.  This is needed
     * to help when cleaning up the domain session due to various issues when there are multiple threads
     * interacting with this class.
     *
     * @author mhoffman
     *
     */
    private static enum LOCAL_SESSION_STATE{
        RUNNING,
        CLOSING,
        CLOSED
    }

    /** the state of the domain session as represented in this class */
    private LOCAL_SESSION_STATE localSessionState = LOCAL_SESSION_STATE.RUNNING;

    /**
     * flag used to indicate whether one or more Gateway module interops have been initialized
     * and are therefore enabled in the Gateway module.  Knowing this is useful for knowing when to
     * send SIMAN messages to the Gateway module or not.
     */
    private boolean interopsEnabled = false;

    /**
     * flag used to indicate whether the current training app transition
     * being used is embedded or is a training app running through the
     * gateway module. Used to appropriately route the message to
     * the training application
     */
    private boolean isCurrentAppEmbedded = false;

    /**
     * Unique id used to identify this domain session's topics for receiving training
     * application messages
     */
    private String trainingApplicationTopicId = "";

    /**
     * The name of the topic that this domain session will use to send simulation messages to
     * itself while playing back a log file, emulating the behavior of the gateway topics
     * normally used for training applications
     */
    private String logPlaybackServiceTopic = null;

    /**
     * Contains the domain knowledge for the current lesson in a domain session
     * Can be null if not currently in a lesson with an associated dkf
     */
    private DomainKnowledgeManager domainKnowledgeManager;

    /**
     * Caches the training app handler.  This is used for team sessions where
     * the training application is not loaded until the host starts the session.
     */
    private TrainingApplicationHandler cachedTrainingAppHandler;

    /**
     * The future which yields the service that is used to play a training
     * application log back (if necessary).<br/>
     * It maybe necessary to check if the 'future' is done before using it as the use (e.g. thenAccept)
     * may hang indefinitely.
     */
    private CompletableFuture<LogFilePlaybackService> playbackServiceFuture = CompletableFuture.completedFuture(null);

    /**
     * Caches the training application callback.  This is cached for team sessions
     * where the training application is not loaded until the host starts the session.
     */
    private AsyncActionCallback cachedTaFinishedCallback;

    /**
     * Caches the training application transition object.  This is cached for team sessions
     * where the training application is not loaded until the host starts the session.
     */
    private CourseObjectWrapper cachedTrainingAppTransition;

    /** Handles the collection of transitions */
    private CourseManager courseManager;

    /** the relative path of the runtime course folder from the domain folder, set during initialization of this class */
    private String runtimeCourseFolderRelativePath;

    /**
     * used to keep track of LMS records written since the last AAR course transition<br/>
     *
     * key: publish score response from the LMS<br/>
     * value: date timestamp of when the score was captured, i.e. when the event happened.  Can be used as key in
     * {@link #pendingReviewCourseEvents}.
     */
    private final Map<PublishLessonScoreResponse, Date> publishedScoreToEventIdMap = new HashMap<>();

    /**
     * used to keep track of course events that should be displayed in AAR and have not yet been presented
     *
     * key: date timestamp of when the event happened
     * value: list of AAR elements to present for the event captured at this time
     */
    private final Map<Date, List<AbstractAfterActionReviewEvent>> pendingReviewCourseEvents = new HashMap<>();

    /** the course file to use to give the lesson by moving through its content */
    final private FileProxy courseFile;

    /** the directory of the authored course file */
    final private AbstractFolderProxy courseAuthoredDirectory;

    /** The information about this domain session */
    final private DomainSession domainSessionInfo;

    /** Contains unique information about the client being used by the learner
     *            to take this course.  Can be null, but not ideal. */
    private WebClientInformation webClientInfo;

    /** The LMS username of the learner of the domain session */
    final private String lmsUsername;

    private EntityTable entityTable;

    /** the last training app state that was sent to listeners */
    private TrainingApplicationStateEnum currentTrainingAppState = null;

    /** the last lesson state that was sent to listeners
     * Note: a "lesson" refers to a training application course element or Autotutor survey course element
     */
    private LessonStateEnum currentLessonState = null;

    /** The list of listeners interested in changes in the training application state */
    private final Set<TrainingApplicationStateListener> trainingAppStateListeners = new HashSet<>();

    /** The list of listeners interested in changes in the lesson state */
    private final Set<LessonStateListener> lessonStateListeners = new HashSet<>();

    /**
     * a central location to maintain performance node assessments until they are needed
     * for retrieval usually during communication outside of this module.
     */
    private AssessmentProxy assessmentProxy = new AssessmentProxy();

    /**
     * used to initialize a Gateway module when in server mode.  Therefore this can be null.
     */
    private InitializeRemoteGateway initGW;

    /**
     * the current course transition
     */
    private CourseObjectWrapper currentCourseObject = null;

    /**
     * the current lesson material handler. This gets set when a lesson material is being displayed
     * and gets set back to null when it is moving to the next course object.
     */
    private LessonMaterialHandler currentLessonMaterialHandler = null;

    /**
     * used to help manage remediation information for Recall and Practice phases of an adaptive courseflow course
     * object currently being handled in the course.
     */

    /** whether there is a pending adaptive courseflow AAR still to show the learner */
    private boolean pendingAdaptiveCourseflowAAR = false;

    /** contains information collected for an upcoming adaptive courseflow AAR */
    private AbstractAfterActionReviewEvent adaptiveCourseflowPendingAARInfo = null;

    /** the DKF scoring results for a practice phase of adaptive courseflow */
    private PublishLessonScoreResponse adaptiveCourseflowPracticeScore = null;

    /**
     * flag used to indicate whether the current Autotutor session being run is using the legacy logic
     * that doesn't use the new conversation manager class to handle the GIFT state
     */
    private boolean usingLegacyAutoTutorHandler = false;

    /**
     * the current gateway interops connection info
     * This is useful for determining if the gateway module is currently provided services that might be needed
     * by the lesson in a particular part of the course.  E.g. display feedback in training app, allow survey question
     * to be answered in training app.
     * Will be null if not currently in a training app course object part of the course.
     */
    private InteropConnectionsInfo currentInteropConnectionsInfo = null;

    /**
     * flag used to indicate whether content loading progress updates from the gateway should be forwarded
     * to the tutor to display to the learner.
     * The progress should only be displayed if the guidance page shown while training app content is loading
     * can handle showing the progress to the learner, i.e. don't place the progress bar over content the course author wants to show.
     */
    private boolean forwardContentLoadingUpdates = false;

    /**
     * Optional, can be null.  The runtime parameters are used to pass in any runtime data that may needed by the base domain
     * session, especially depending on how it may be launched.  The initial use of this is to allow lti tool consumers to start
     * a course, and pass in lti runtime parameters that are used to report progress back to the tool consumer.  However the system
     * is abstract so that any future system could leverage passing in of runtime data to the base domain session.
     */
    private AbstractRuntimeParameters runtimeParams = null;

    /**
     * indicates whether the current location in the authored course is a course object that can 
     * be expanded to multiple course objects (e.g. adaptive course flow, training app w/ remediation)
     * This is sent in the course state messages so other modules (e.g. pedagogical) can detect entering
     * and existing the authored expandable course object vs. experiences within the same course object.
     * E.g. Training App w/ remediation course expand to PowerPoint A->Structured Review->PowerPoint B (remediation)->PowerPoint A (again).
     */
    private ExpandableCourseObjectStateEnum expandableCourseObjectState = ExpandableCourseObjectStateEnum.NONE;

    /**
     * Generates an exception for when a NACK is received
     *
     * @param actionPerformed The action being performed when the NACK was
     * received
     * @param errorMessage The NACK message received
     * @return Exception The exception generated
     */
    public static Exception generateException(String actionPerformed, Message errorMessage) {
        return new Exception("The "+errorMessage.getSenderModuleType().getDisplayName()+" had a problem while performing the action of '" + actionPerformed + "' because '"
                + ((NACK) errorMessage.getPayload()).getErrorMessage() + "'.");
    }

    /**
     * Generates an exception for when a failure happens
     *
     * @param actionPerformed The action being performed when the failure
     * happened
     * @param why Why the action failed
     * @return Exception The exception generated
     */
    public static Exception generateException(String actionPerformed, String why) {
        return new Exception("Error while '" + actionPerformed + "'. "
                + why);
    }

    /** the callback used when a course object is finished and the next course object should be loaded */
    private AsyncActionCallback transitionCallback = new TransitionCallback();

    /** The callback for when a transition completes or encounters an error */
    private class TransitionCallback implements AsyncActionCallback {

        @Override
        public void onFailure(Exception e) {
            onSessionError(e);
        }

        @Override
        public void onSuccess() {
            displayNextTransition();
        }
    }

    /** The serializable class indicating a path of an authored branch has ended */
    private class PathEnd implements Serializable {

        /** Serializable Version UID */
        private static final long serialVersionUID = 6420211730183302508L;

        /** The Authored Branch this path ending belongs to */
        AuthoredBranch authoredBranch;

        /** The path that is ending */
        Path selectedPath;

        /**
         * Constructor
         *
         * @param authoredBranch the branch that the path that is ending belongs to
         * @param selectedPath the path that is ending
         */
        public PathEnd(AuthoredBranch authoredBranch, Path selectedPath) {
            this.authoredBranch = authoredBranch;
            this.selectedPath = selectedPath;
        }

        /**
         * Gets the authored branch for the path that is ending
         *
         * @return the authored branch
         */
        public AuthoredBranch getAuthoredBranch() {
            return authoredBranch;
        }

        /**
         * Gets the path that is ending
         *
         * @return the selected path
         */
        public Path getSelectedPath() {
            return selectedPath;
        }
    }

    /**
     * Used to send the course progress to the LMS
     */
    private AbstractProgressReporter lmsProgressReporter;

    /** 
     * The most recently completed knowledge session for this domain session. Used to skip
     * reassigning team members if later knowledge sessions in the same domain session have
     * the same team roles.
     */
	private AbstractKnowledgeSession previousKnowledgeSession;

    /**
     * Class constructor - set attributes
     *
     * @param courseFile - the course file. Can't be null.
     * @param courseAuthoredDirectory - the directory of the <b>authored</b>
     *        course file (not to be confused with the runtime course file).
     *        Can't be null.
     * @param domainSessionInfo - info about the domain session. Can't be null.
     * @param lmsUsername - the users LMS credentials
     * @param webClientInfo Contains unique information about the client being
     *        used by the learner to take this course. Can be null, but not
     *        ideal.
     * @param runtimeParams - (Optional) Additional parameters that are used to
     *        configure the domain session at runtime.
     */
    public BaseDomainSession(FileProxy courseFile, AbstractFolderProxy courseAuthoredDirectory, DomainSession domainSessionInfo, String lmsUsername,
            WebClientInformation webClientInfo, AbstractRuntimeParameters runtimeParams) {

        if (courseFile == null) {
            throw new IllegalArgumentException("The course file path can't be null.");
        } else if (courseAuthoredDirectory == null) {
            throw new IllegalArgumentException("The parameter 'courseAuthoredDirectory' cannot be null.");
        } else if (domainSessionInfo == null) {
            throw new IllegalArgumentException("The domain session info can't be null.");
        }

        this.courseFile = courseFile;
        this.courseAuthoredDirectory = courseAuthoredDirectory;
        this.domainSessionInfo = domainSessionInfo;
        this.lmsUsername = lmsUsername;
        this.webClientInfo = webClientInfo;

        this.runtimeParams = runtimeParams;
        
        // Note: must be initialized after the this.domainSessionInfo is set for this class
        lmsProgressReporter = new LMSProgressReporter(this);
    }

    /**
     * Return the information about this domain session
     *
     * @return DomainSession The information about this domain session
     */
    public DomainSession getDomainSessionInfo() {
        return domainSessionInfo;
    }

    /**
     * Return the domain course file name being used for this domain session
     *
     * @return String The file name for the course of this domain session
     */
    public String getCourseFileName() {
        return courseFile.getFileId();
    }

    /**
     * Return the course manager for the current domain session
     *
     * @return CourseManager
     */
    public CourseManager getCourseManager() {
        return courseManager;
    }

    /**
     * Initialize this domain session instance
     * @throws IOException if there was a problem parsing the course or any related course files
     * @throws URISyntaxException if there was a problem constructing URIs to do the retrieval
     * @throws CourseFileValidationException if there was a problem parsing the course
     * @throws ConfigurationException if there was a problem with the initialization sequence of events. This exception should indicate the end of the course.
     */
    public void initialize() throws IOException, URISyntaxException, CourseFileValidationException, ConfigurationException {

        if(logger.isInfoEnabled()){
            logger.info("Initializing domain session for "+domainSessionInfo+" to run course referenced by "+courseFile+".");
        }

        DesktopFolderProxy domainFolder = new DesktopFolderProxy(new File(DomainModuleProperties.getInstance().getDomainDirectory()));
        DesktopFolderProxy courseFolder = (DesktopFolderProxy) domainFolder.getParentFolder(courseFile);
        runtimeCourseFolderRelativePath = FileFinderUtil.getRelativePath(domainFolder.getFolder(), courseFolder.getFolder());

        //no longer validating the course here as part of #3283 - essentially the course should be valid before starting the course
        //Dashboard started course - had to be validated to start the course
        //Experiments - not validating every time a user starts the course.  It was valid when it was created so high likelihood it is still valid
        //LTI - not validating every time a user starts the course.
        //Simple Login - had to be validated to start the course
        DomainCourseFileHandler domainCourseFileHandler = new DomainCourseFileHandler(courseFile, courseFolder, true);

        courseManager = new CourseManager(courseFile, courseAuthoredDirectory, assessmentProxy, this,
                domainCourseFileHandler, runtimeParams, domainSessionInfo.getUsername(), domainSessionInfo.buildLogFileName());

        //so course progress will be reported to LMS
        courseManager.addProgressReporter(lmsProgressReporter);

        trainingApplicationTopicId = UUID.randomUUID().toString();

        InteropImplementationsNeeded interopImplementationsNeeded = domainCourseFileHandler.getInteropImplementations();

        if(interopImplementationsNeeded.needsLogPlaybackTopic) {

            //create a topic to send simulation messages back to the domain module to simulate
            //gateway traffic while playing back a session log file
            logPlaybackServiceTopic = DomainModule.getInstance().createLogPlaybackTopic(UUID.randomUUID().toString());
        }

        getDomainSessionInfo().setHostGatewayAllowed(interopImplementationsNeeded.hostGWAllowed);

            if(DomainModuleProperties.getInstance().getDeploymentMode() == DeploymentModeEnum.SERVER){

            if(interopImplementationsNeeded.interopImpls.isEmpty()){
                //running in server mode and there are no gateway interops used in this course, therefore
                //the gateway is not needed.  This prevents a close domain session message being sent
                //to a non-existent, non allocated gateway module for this user's execution of a domain session
                getDomainSessionInfo().setRequiresGateway(false);
                getDomainSessionInfo().setGatewayConnected(false);
            }else{
                //show the course initialize instructions and download java web start if in server mode
                //in other modes the Gateway module will be started before the course starts

                //Initialize the Java Web Start (JWS) Gateway module instance in order to communicate
                //with any possible desktop application used in this course execution
                initGW = new InitializeRemoteGateway();
                initGW.start(interopImplementationsNeeded.interopImpls);

                getDomainSessionInfo().setGatewayConnected(true);
            }
        }else{

            if(interopImplementationsNeeded.interopImpls.isEmpty()){
                // A gateway module is not needed for this course, don't attempt to send message to it because
                // the gateway module will not be allocated to this user
            getDomainSessionInfo().setRequiresGateway(false);
                getDomainSessionInfo().setGatewayConnected(false);
            }else{
                // Determine if a gateway can be allocated to this domain session
                InitializeGateway bestEffortGateway = new InitializeGateway();
                bestEffortGateway.start();

                // when a gateway module was not allocated to this domain session then don't set that the gateway module is
                // connected because it wont be.  This relies on another domain session hosting the team knowledge session
                // and this domain session will be a joiner, not a hoster
                getDomainSessionInfo().setGatewayConnected(bestEffortGateway.getAllocatedGateway() != null);

                if(getDomainSessionInfo().doesRequireGateway() && !getDomainSessionInfo().isGatewayConnected() && !getDomainSessionInfo().isHostGatewayAllowed()){
                    //failed to allocate gateway module when it is needed
                    throw new ConfigurationException("Unable to find an available Gateway module",
                            "This course requires a Gateway module to interact with and possibly control an external application but an available gateway module was not found connected to this GIFT instance.", null);
                }
            }

        }

        if(domainSessionInfo.getExperimentId() == null) {
            // because the time it takes to show the first course object in the TUI can be 10+ seconds
            // due to things like retrieving learner history from the LRS, show the GIFT fun facts page
            // NOTE: don't show this for GIFT experiments because the researcher may not want to show
            //       GIFT facts for their study
            sendDisplayDefaultLoadingMessageRequest(true);
        }

        //Checks to see if there are any embedded applications
        List<String> embeddedAppUrls = domainCourseFileHandler.getEmbeddedUrls();
        getDomainSessionInfo().setRequiresTutorTopic(!embeddedAppUrls.isEmpty());

        // need to allocate a learner module after the gateway module has been allocated (or not allocated)
        // It will request that the learner module be allocated - this is important because the learner module
        // needs to know about the gateway module address being used by the domain module
        InitializeLearner initLearner = new InitializeLearner();
        initLearner.start();

        // need to allocate a sensor module after the learner module has been allocated
        // It will request that the sensor module be allocated - this is important because the sensor module
        // needs to know about the learner module address being used by the domain module
        InitializeSensor initSensor = new InitializeSensor();
        initSensor.start();

    }
    
    /**
     * Return whether this domain session is currently in playback mode for a course object (e.g. VBS log file playback).
     * 
     * @return true if the playback service is active.
     */
    public boolean isInPlaybackMode(){
        try {
            return playbackServiceFuture != null && playbackServiceFuture.get() != null && !playbackServiceFuture.get().hasTerminated();
        } catch (@SuppressWarnings("unused") InterruptedException | ExecutionException e) {
            return false;
        }
    }

    /**
     * Initialize the GIFT domain session
     * @throws Exception if there was a problem starting the domain session
     */
    public void start() throws Exception {

        //Creates a topic for the embedded training application if its necessary
        if(getDomainSessionInfo().doesRequiresTutorTopic()) {
            DomainModule.getInstance().createEmbeddedTrainingAppTopic(trainingApplicationTopicId);
        }

        initializeGiftDomainSession(new AsyncActionCallback() {

            @Override
            public void onFailure(Exception e) {

                logger.error("The domain session failed to start.", e);

                onSessionError(e);
            }

            @Override
            public void onSuccess() {
                displayNextTransition();
            }
        });
    }

    /**
     * Shutdown the domain session before it is finished. Displays the message on the tutor during a termination shutdown.
     *
     * @param error If the reason is because of an error
     * @param reason The reason why the termination is happening. This is used for logging purposes, not shown to the learner. Can't be null.
     * @param details The details of the reason. Used as additional details for logging purposes, not shown to the learner. Can be null.
     * @param message The HTML formatted message to display as Guidance to the user in the tutor before the session terminates. Can't be null.
     * @param status information about why the session is ending.
     * @throws IllegalArgumentException if the reason or message is null or empty.
     */
    public void terminateSession(boolean error, String reason, String details, String message, LessonCompletedStatusType status) {
        terminateSession(error, reason, details, status, buildModulesClosedDomainSessionCallback(message));
    }

    /**
     * Shutdown the domain session before it is finished. Displays the termination reason on the tutor during a termination shutdown.
     *
     * @param error If the reason is because of an error
     * @param reason The reason why the termination is happening. This is shown to the learner in the TUI as well
     * as used for logging purposes. Can't be null.
     * @param details The details of the reason. Used as additional details shown to the learner in the TUI as well
     * as for logging purposes. Can be null.
     * @throws IllegalArgumentException if the reason is null or empty.
     */
    public void terminateSession(boolean error, String reason, String details) {
        terminateSession(error, reason, details, LessonCompletedStatusType.ERROR, buildModulesClosedDomainSessionCallback(reason, details));
    }

    /**
     * Shutdown the domain session before it is finished.</br>
     * Note: if the current local session state is closed this method will do nothing.
     *
     * @param error If the reason is because of an error
     * @param reason The reason why the termination is happening. This is used for logging purposes. Can't be null or empty.
     * @param details The details of the reason. Used as additional details for logging purposes. Can be null.
     * @param status information about why the session is ending.
     * @param modulesClosedDomainSessionCallback The callback to execute when all modules have closed the domain session. Can't be null.
     * @throws IllegalArgumentException if the reason is null or empty or if modulesClosedDomainSessionCallback is null.
     */
    public void terminateSession(boolean error, String reason, String details, LessonCompletedStatusType status, MessageCollectionCallback modulesClosedDomainSessionCallback) {

        if(modulesClosedDomainSessionCallback == null){
            throw new IllegalArgumentException("The modulesClosedDomainSessionCallback can't be null.");
        }

        // #5044 - if the 'future' was never created successfully and was completed
        // the following thenAccept call will never be started.  This doesn't prevent this thread from moving on.
        if(!playbackServiceFuture.isDone()){
            playbackServiceFuture.thenAccept(playbackService -> {
                if (playbackService != null) {
                    if(logger.isInfoEnabled()){
                        String sessionInfo = "";
                        if(getDomainSessionInfo().getUsername() != null){
                            sessionInfo += "for user "+getDomainSessionInfo().getUsername()+" ";
                        }
                        sessionInfo += "in session "+getDomainSessionInfo().getDomainSessionId();
                        logger.info("Pausing playback "+sessionInfo);
                    }
                    playbackService.pausePlayback();
                }
            });
        }

        if(localSessionState == LOCAL_SESSION_STATE.CLOSING || localSessionState == LOCAL_SESSION_STATE.CLOSED){
            /* already closing or closed, shouldn't be displaying something to the learner
             * in the TUI because the tutor might no longer be connected to this
             * domain session (e.g. gift is shutting down) */
            // NOTE: added closing to handle a case where BaseDomainSession has ordered modules to cleanup the session
            //       but session messages are still being sent around (e.g. submit survey results) which will cause modules
            //       to send error messages because they don't know about the session anymore.
            modulesClosedDomainSessionCallback.success();

            return;
        }

        if(reason == null || reason.isEmpty()){
            throw new IllegalArgumentException("The reason can't be null or empty.");
        }

        
        /*
         * If the domain session is running a training application course object (true if it is
         * managing a knowledge session and has at least one Gateway module interop enabled),
         * then stop the training app scenario before continuing.
         * 
         * This sends a LessonCompleted message, which is used in the process of properly closing 
         * training application course objects, so it should be sent before the session finishes shutting down.
         * 
         * The domain session continues to shut down after either success or failure of the message.
         */
        if (domainKnowledgeManager != null && interopsEnabled) {
            lessonCompleted(new LessonCompleted(status), new AsyncActionCallback() {
                
                @Override
                public void onSuccess() {
                    finishTerminateSession(error, reason, details, status, modulesClosedDomainSessionCallback);
                }
                
                @Override
                public void onFailure(Exception e) {
                    logger.error("While shutting down a domain session, a LessonCompleted message failed to send. GIFT will continue attempting to shut down the domain session.", e);
                    finishTerminateSession(error, reason, details, status, modulesClosedDomainSessionCallback);
                }
            });
        } else {
            /*
             * If not closing a training application course object, finishTerminateSession should still be
             * called. In that case, it does not need to wait for a LessonCompleted message first.
             */
            finishTerminateSession(error, reason, details, status, modulesClosedDomainSessionCallback);
        }
        
        
    }
    
    /**
     * Finish the process of shutting down the domain session which was started in terminateSession.
     *
     * @param error If the reason is because of an error
     * @param reason The reason why the termination is happening. This is used for logging purposes. Can't be null or empty.
     * @param details The details of the reason. Used as additional details for logging purposes. Can be null.
     * @param status information about why the session is ending.
     * @param modulesClosedDomainSessionCallback The callback to execute when all modules have closed the domain session. Can't be null.
     * @throws IllegalArgumentException if the reason is null or empty or if modulesClosedDomainSessionCallback is null.
     */
    public void finishTerminateSession(boolean error, String reason, String details, LessonCompletedStatusType status, MessageCollectionCallback modulesClosedDomainSessionCallback) {
        String logMessage = "Terminating the domain session: " + reason;

        if (details != null) {
            logMessage = logMessage + ": " + details;
        }

        if (error) {
            logger.error(logMessage);

        } else {
            if(logger.isInfoEnabled()) {
                logger.info(logMessage);
            }
        }        
        
        // Note: #5097 - this call needs to happen before localSessionState is changed to CLOSING 
        terminationShutdownStopScenario(reason, status, modulesClosedDomainSessionCallback);

        localSessionState = LOCAL_SESSION_STATE.CLOSING;

        if (entityTable != null) {

            entityTable.stop();
            entityTable = null;

        }

        // stop any running playback - 
        // #5044 - if the 'future' was never created successfully and was completed
        // the following thenAccept call will never be started.  This doesn't prevent this thread from moving on. 
        if(!playbackServiceFuture.isDone()){
            playbackServiceFuture.thenAccept(playbackService -> {
                if (playbackService != null) {
                    playbackService.terminatePlayback();
                    playbackServiceFuture = CompletableFuture.completedFuture(null);
                }
            });
        }     
    }

    @Override
    public void scenarioCompleted(final LessonCompleted lessonCompleted) {

        if(logger.isInfoEnabled()){
            logger.info("The domain assessment for the current Training App course transition has ended ("+this.domainSessionInfo+").");
        }
        
        stopTrainingAppScenario(lessonCompleted, new AsyncActionCallback() {

                @Override
                public void onFailure(Exception e) {
                    onSessionError(e);
                    
                    // - not sure if this is better called before or after session error.
                    finishKnowledgeSessionCleanup(LessonCompletedStatusType.ERROR);  
                }

                @Override
                public void onSuccess() {
                    finishKnowledgeSessionCleanup(lessonCompleted.getStatusType());
                    changeTrainingAppState(TrainingApplicationStateEnum.STOPPED);
                }                
                
                /**
                 * Now that the knowledge session is
                 * 1. terminated for the host of the knowledge session
                 * 2. the GW OR Tutor module notified, cleaned up and any controlled training application scenario exited for the host of the knowledge session
                 * 3. the lesson score published for the host of the knowledge session
                 * 4. the published lesson score given to any joiner sessions
                 * 
                 * It is time to notify any joiner sessions to also cleanup their knowledge session and for both host and joiner to move onto the next course object.
                 */
                private void finishKnowledgeSessionCleanup(LessonCompletedStatusType status){
                    
                    final boolean isTeamSessionHost = KnowledgeSessionManager.getInstance().isHostOfTeamKnowledgeSession(getDomainSessionInfo().getDomainSessionId());
                    if(isTeamSessionHost){
                        // Notify other domains that have joined this team knowledge session to end their knowledge session instances

                        DomainModule.getInstance().notifyMembersOfKnowledgeSessionEnding(getDomainSessionInfo().getDomainSessionId(),
                                "The learner hosting the team knowledge session has manually finished the simulation.", status);
                    }
                    
                    cleanUpRealTimeAssessmentSession("The real-time knowledge assessment session has ended.", lessonCompleted.getStatusType());
                }
            });
    }
    
    /**
     * Apply a request to change team knowledge session membership.  This includes creating or destroying a team knowledge session and
     * joining or leaving a team knowledge session.
     *
     * @param membershipRequest contains the information for the request. Can't be null.
     * @param groupMembership whether the team knowledge session will be using group membership or individual membership type.
     * @param callback Callback for when the action is done or fails. Can't be null.
     * @throws ManageTeamMembershipException if there was a problem fulfilling the request
     * @throws IllegalArgumentException if the team membership request action type is unknown
     */
    public void handleTeamKnowledgeSessionMembershipRequest(ManageTeamMembershipRequest membershipRequest, boolean groupMembership, final AsyncActionCallback callback) throws ManageTeamMembershipException, IllegalArgumentException  {
        // get the info for this session, which is used to manipulate the host session's membership information 
        // (which could be this session or another session)
        // e.g. host ds id = 300, requesting ds id = 303; 303 would like to join the session hosted by 300.
        int requestingdsId = domainSessionInfo.getDomainSessionId();
        String username = domainSessionInfo.getUsername();
        handleTeamKnowledgeSessionMembershipRequest(membershipRequest, requestingdsId, username, groupMembership, callback);
    }

    /**
     * Retrieve the task course node ids (UUIDs) for any task that is either directly referenced
     * in the provided set of DKF performance node ids (integers) or contains a descendant concept 
     * with the DKF performance node id.
     * @param taskConceptDomainKnowledgeIds Retrieve the task course node ids (UUIDs) for any task that is either directly referenced
     * in the provided set of DKF performance node ids (integers) or contains a descendant concept
     * with the DKF performance node id.
     * @return a unique set of task course node ids. Can be smaller size than the provided set if a task was either
     * not found or more than one entry in the provided set was found under the same task. Will return an empty set
     * if the provided set is null or empty.  Will return null if there isn't an active domain knowledge assessment
     * ongoing (dkf session).
     */
    public Set<String> getCourseTaskIds(Set<Integer> taskConceptDomainKnowledgeIds){
        
        if(domainKnowledgeManager != null) {
            Scenario scenario = domainKnowledgeManager.getAssessmentKnowledge().getScenario();
            if(scenario != null) {
                return scenario.getCourseTaskIds(taskConceptDomainKnowledgeIds);
            }
        }
        
        return null;
    }

    /**
     * Apply a request to change team knowledge session membership.  This includes creating or destroying a team knowledge session and
     * joining or leaving a team knowledge session.
     *
     * @param membershipRequest contains the information for the request. Can't be null.
     * @param requestingdsId the domain session id for the membership request.  Can be the host or a joiner.
     * @param groupMembership whether the team knowledge session will be using group membership or individual membership type.
     * @param callback Callback for when the action is done or fails. Can't be null.
     * @throws ManageTeamMembershipException if there was a problem fulfilling the request
     * @throws IllegalArgumentException if the team membership request action type is unknown
     */
    public void handleTeamKnowledgeSessionMembershipRequest(ManageTeamMembershipRequest membershipRequest, int requestingdsId, String username,
            boolean groupMembership, final AsyncActionCallback callback) throws ManageTeamMembershipException, IllegalArgumentException  {
        if (membershipRequest == null) {
            throw new IllegalArgumentException("The parameter 'membershipRequest' cannot be null.");
        } else if (callback == null) {
            throw new IllegalArgumentException("The parameter 'callback' cannot be null.");
        }

        boolean delayCallback = false;

        if(domainKnowledgeManager != null){
            switch(membershipRequest.getActionType()){
            case CREATE_TEAM_SESSION:
                final String nameOfSessionToCreate = membershipRequest.getNameOfSessionToCreateOrDestroy();
                // Note: only Active session types will have their team sessions created this way as it involves
                //       a series of interactions with the TUI (e.g. GIFT team session lobby).
                createTeamSession(nameOfSessionToCreate, groupMembership, SessionType.ACTIVE, callback);
                delayCallback = true;
                break;
            case DESTROY_TEAM_SESSION:
                KnowledgeSessionManager.getInstance().removeKnowledgeSession(requestingdsId);
                break;
            case JOIN_TEAM_SESSION:
                IndividualMembership individualMembershipJoiner = new IndividualMembership(username);
                SessionMember joiningSessionMember = new SessionMember(individualMembershipJoiner, domainSessionInfo, requestingdsId);
                final String domainSourceId = domainSessionInfo.getDomainSourceId();
                KnowledgeSessionManager.getInstance().joinTeamSession(membershipRequest.getDomainSessionIdOfHost(),
                        joiningSessionMember, domainSourceId);
                break;
            case LEAVE_TEAM_SESSION:
                KnowledgeSessionManager.getInstance().leaveTeamSession(membershipRequest.getDomainSessionIdOfHost(),
                        requestingdsId);
                break;
            case ASSIGN_TEAM_MEMBER:
                AbstractTeamUnit aTeamUnit = domainKnowledgeManager.getAssessmentKnowledge().getScenario().getRootTeam().getTeamElement(membershipRequest.getTeamMemberName());
                if(aTeamUnit != null && aTeamUnit instanceof TeamMember<?>){
                    TeamMember<?> aTeamMember = (TeamMember<?>) aTeamUnit;
                    domainKnowledgeManager.getAssessmentKnowledge().getScenario().setLearnerTeamMember(aTeamMember);
                    KnowledgeSessionManager.getInstance().assignTeamMember(membershipRequest.getDomainSessionIdOfHost(),
                            requestingdsId, aTeamMember);
                }else{
                    callback.onFailure(new Exception("Unable to find a team member position named '"
                            + membershipRequest.getTeamMemberName() + "' in the team organization."));
                    delayCallback = true;
                }
                break;
            case UNASSIGN_TEAM_MEMBER:
                KnowledgeSessionManager.getInstance().unassignTeamMember(membershipRequest.getDomainSessionIdOfHost(),
                        requestingdsId);
                break;

            case CHANGE_TEAM_SESSION_NAME:
                KnowledgeSessionManager.getInstance().changeTeamSessionName(membershipRequest.getDomainSessionIdOfHost(),
                        membershipRequest.getNameOfSessionToCreateOrDestroy());
                break;
            case SET_TEAM_MEMBERS_GROUP_SESSION:
                // assign any team member assignments provided to the team knowledge session that should already be created
                        
                // apply the team member assignments to the new team knowledge session
                GroupMembership groupMembershipRequest = membershipRequest.getGroupSessionMembership();
                AbstractKnowledgeSession abstractKnowledgeSession = KnowledgeSessionManager.getInstance().getKnowledgeSessions().get(domainSessionInfo.getDomainSessionId());
                GroupMembership groupMembershipSession = (GroupMembership) abstractKnowledgeSession.getHostSessionMember().getSessionMembership();
                for(IndividualMembership individualMembership : groupMembershipRequest.getMembers()){
                    
                    // add the requested individual to the created session's group membership
                    groupMembershipSession.getMembers().add(individualMembership);
                    
                    try{
                        // for each session member, assign team member   
                        // get the team unit from the knowledge session
                        AbstractTeamUnit individualTeamUnit = domainKnowledgeManager.getAssessmentKnowledge().getScenario()
                                .getRootTeam().getTeamElement(individualMembership.getTeamMember().getName());
                        if(individualTeamUnit != null && individualTeamUnit instanceof TeamMember<?>){
                            TeamMember<?> aTeamMember = (TeamMember<?>) individualTeamUnit;
                            domainKnowledgeManager.getAssessmentKnowledge().getScenario().setLearnerTeamMember(aTeamMember);

                        }else{
                            callback.onFailure(new Exception("Unable to find a team member position named '"
                                    + membershipRequest.getTeamMemberName() + "' in the team organization for '"+individualMembership.getUsername()+"'."));
                        }
                    }catch(Exception e){
                        callback.onFailure(new Exception("Failed to join and assign team member role for '"+individualMembership.getUsername()+"'", e));
                    }
                }// end for
                
                callback.onSuccess();
                delayCallback = true;

                break;
            default:
                throw new IllegalArgumentException("Found unhandled team membership request action type of  "+membershipRequest.getActionType());
            }
        }else{
            callback.onFailure(new Exception(
                    "Unable to manage team knowledge session membership at this time because the domain session "
                            + requestingdsId
                            + " is not in a course object that uses real time assessment."));
            delayCallback = true;
        }

        if(!delayCallback){
            
            // release the calling thread
            new Thread(new Runnable() {
                
                @Override
                public void run() {    
                    callback.onSuccess();
                }
            }, "Team Knowledge Session Membership Request callback"+requestingdsId).start();
        }

    }
    
    /**
     * Create a new team knowledge session hosted by this domain session.
     * 
     * @param nameOfSession the name of the knowledge session, useful information to display to joiners and hosters to 
     * identify this session from other sessions in this GIFT instance.
     * @param groupMembership whether the team knowledge session will be using group membership or individual membership type.
     * Group membership type {@link GroupMembership} is currently used by RTA mode to assign all learners to team members at once.
     * @param sessionType the type of knowledge session (e.g. active, playback, past) this team session will be created within. 
     * @param callback used to notify the caller if the team session was created successfully or not.
     */
    private void createTeamSession(String nameOfSession, boolean groupMembership, final SessionType sessionType, final AsyncActionCallback callback){
        
        final String experimentId = domainSessionInfo.getExperimentId();
        
        final int numberOfPossibleLearners = domainKnowledgeManager.getNumberOfPossibleLearners();
        final Team rootTeam = domainKnowledgeManager.getAssessmentKnowledge().getScenario().getRootTeam();
        final String scenarioDescription = domainKnowledgeManager.getAssessmentKnowledge().getScenario().getDescription();
        final Map<Integer, AbstractPerformanceAssessmentNode> performanceNodes = domainKnowledgeManager
                .getAssessmentKnowledge().getScenario().getPerformanceNodes();
        final TrainingApplicationEnum trainingAppType = TrainingAppUtil
                .getTrainingAppType((generated.course.TrainingApplication)cachedTrainingAppTransition.getCourseObject());
        final String courseName = courseManager.getCourseName();
        final Mission mission = domainKnowledgeManager.getAssessmentKnowledge().getScenario().getMission();

        if (StringUtils.isNotBlank(experimentId)) {
            DomainModule.getInstance().sendGetExperimentRequest(new GetExperimentRequest(experimentId),
                    new MessageCollectionCallback() {
                        private DataCollectionItem experiment;

                        @Override
                        public void success() {
                            if (experiment != null) {
                                KnowledgeSessionManager.getInstance().hostTeamKnowledgeSession(domainSessionInfo, nameOfSession,
                                        courseName, experiment.getName(), scenarioDescription, numberOfPossibleLearners, rootTeam,
                                        performanceNodes, trainingAppType,
                                        mission, getObserverControls(), groupMembership, sessionType);
                            } else {
                                callback.onFailure(generateException(
                                        "Retrieving experiment with id " + experimentId,
                                        "Reached successful state, but never received the experiment with id '"
                                                + experimentId + "'."));
                            }
                        }

                        @Override
                        public void received(Message msg) {
                            experiment = (DataCollectionItem) msg.getPayload();
                        }

                        @Override
                        public void failure(String why) {
                            callback.onFailure(
                                    generateException("Retrieving experiment with id " + experimentId, why));
                        }

                        @Override
                        public void failure(Message msg) {
                            callback.onFailure(
                                    generateException("Retrieving experiment with id " + experimentId, msg));
                        }
                    });
        } else {
            KnowledgeSessionManager.getInstance().hostTeamKnowledgeSession(domainSessionInfo, nameOfSession,
                    courseName, null, scenarioDescription, numberOfPossibleLearners, rootTeam, performanceNodes,
                    trainingAppType, mission, getObserverControls(), groupMembership, sessionType);
            
            // release the calling thread
            new Thread(new Runnable() {
                
                @Override
                public void run() {    
                    callback.onSuccess();
                }
            }, "Create Team Knowledge Session Request callback"+domainSessionInfo.getDomainSessionId()).start();
        }
    }

    /**
     * Handles the start team knowledge session request which normally comes from the tutor when the
     * host of a team knowledge session clicks the 'start session' button on the lobby page.
     *
     * @throws Exception if the knowledge manager for this domain session has not been initialized before the start session
     * is requested.
     */
    public void startTeamKnowledgeSession() throws Exception {
        if(domainKnowledgeManager != null){
            
            if(isInPlaybackMode()){
                // pause the playback that has got the logic to this point, i.e. sent messages necessary to
                // assign learners in the log to team member positions
                playbackServiceFuture.thenAccept(playbackService -> {
                    if (playbackService != null) {
                        playbackService.pausePlayback();
                    }
                });
            }

            displayTrainingApplicationTransition(cachedTrainingAppTransition, cachedTrainingAppHandler, cachedTaFinishedCallback);
        } else {
            throw new Exception("Unable to start the team knowledge session at this time because the domain session "+getDomainSessionInfo().getDomainSessionId()+" is not in a course object that uses real time assessment.");
        }
    }

    /**
     * Call to clean up the domain session when it has completely ended.
     * Note: once this method is completed no more messages should be sent from this module for this session.
     */
    private synchronized void cleanUp() {

        //don't cleanup if already cleaned up
        if(localSessionState == LOCAL_SESSION_STATE.CLOSED){
            return;
        }

        if(logger.isInfoEnabled()){
            logger.info("Cleaning up a finished domain session ("+this.domainSessionInfo+")");
        }

        localSessionState = LOCAL_SESSION_STATE.CLOSED;

        playbackServiceFuture.thenAccept(playbackService -> {
            if (playbackService != null) {
                playbackService.terminatePlayback();
                playbackServiceFuture = CompletableFuture.completedFuture(null);
            }
        });

        //delete any created remote zip file for this domain session
        if(initGW != null){
            initGW.cleanup();
        }

        //If a topic was necessary for this domain session,
        //then it needs to be removed when the domain session
        //is closed.
        if(getDomainSessionInfo().doesRequiresTutorTopic()) {
            DomainModule.getInstance().removeTutorTopicDestinations(trainingApplicationTopicId);
        }

        //If a topic was needed to send simulation messages from
        //a log playback service associated with this session, then
        //it needs to be removed when the domain session is closed
        if(logPlaybackServiceTopic != null) {
            DomainModule.getInstance().removeDomainTopicDestinations(logPlaybackServiceTopic);
        }

        // release references/memory if not already done so when the training app course object ended
        if(domainKnowledgeManager != null){
            domainKnowledgeManager.cleanup();
            domainKnowledgeManager = null;

            KnowledgeSessionManager.getInstance().cleanupDomainSession(domainSessionInfo.getDomainSessionId());
        }

        // Cleanup the cached training app callback, which was cached for team sessions.
        if (cachedTaFinishedCallback != null) {
            cachedTaFinishedCallback = null;
        }

        // Cleanup the cached training app handler, which was cached for team sessions.
        if (cachedTrainingAppHandler != null) {
            cachedTrainingAppHandler = null;
        }

        // Cleanup the cached training app transition, which was cached for team sessions.
        if (cachedTrainingAppTransition != null) {
            cachedTrainingAppTransition = null;
        }

        // release references/memory created for this course execution
        courseManager.close();
        courseManager = null;

        transitionCallback = null;
        lmsProgressReporter = null;

        DomainModule.getInstance().closeDomainSession(getDomainSessionInfo());
    }

    /**
     * Called when an error occurs in the session that cannot be mitigated,
     * shuts down the session
     *
     * @param e The exception of that error that occurred.
     */
    public void onSessionError(Exception e) {
        terminateSession(true, "There has been a system error", e.getMessage());
    }

    /**
     * Called when an error occures in the session that cannot be mitigated,
     * shuts down the session.
     *
     * @param reason a user friendly message giving a reason to why the session is ending
     * @param details a more developer friendly message giving a reason to why the session is ending
     */
    public void onSessionError(String reason, String details) {
        terminateSession(true, reason, details);
    }

    /**
     * Returns if the domain session is active.
     *
     * @return If the domain session is active.
     */
    public boolean isActive() {
        return localSessionState == LOCAL_SESSION_STATE.RUNNING;
    }

    @Override
    public void handleFeedbackUsingTrainingApp(generated.dkf.Message argument){

        if (isActive()) {

            MessageCollectionCallback showFeedbackHandler =
                    new MessageCollectionCallback() {

                        @Override
                        public void success() {
                            // Do nothing
                        }

                        @Override
                        public void received(Message msg) {
                            // Do nothing
                        }

                        @Override
                        public void failure(Message msg) {

                            //TODO: this is serious and should be handled.  Should the course end with an error?
                            logger.warn("Training App administered feedback message failed to be responded to: "
                                    + "NACK Received: " + msg);
                        }

                        @Override
                        public void failure(String why) {

                            //TODO: this is serious and should be handled.  Should the course end with an error?
                            logger.warn("Training App administered feedback message failed to be responded to: "
                                    + why);
                        }
                    };

            sendTrainingAppFeedbackRequest(argument, showFeedbackHandler);
        }
    }

    /**
     * Return whether the act of removing the specified module (via the module status information) affects this
     * session because this session needs that module.  This check is needed because a previously closed JWS GW module could timeout
     * while another JWS GW module is coming online cause the session to terminate below.
     *
     * @param statusReceivedInfo contains the module status information for a module being removed
     * @return true iff this session should terminate because the specified module is important to this session
     */
    public boolean checkSessionAllocatedModuleRemoved(StatusReceivedInfo statusReceivedInfo){

        if(DomainModuleProperties.getInstance().getDeploymentMode() == DeploymentModeEnum.SERVER){
            //check if this is the gw module this domain session needs

            ModuleStatus mStatus = statusReceivedInfo.getModuleStatus();
            if(mStatus.getModuleType() == ModuleTypeEnum.GATEWAY_MODULE){

                if(initGW != null){

                    ModuleStatus allocatedGW = initGW.getAllocatedGateway();
                    if(allocatedGW != null){
                        //compare the queue name since the GIFT JWS logic generated a UUID in the queue name
                        if(mStatus.getQueueName().equals(allocatedGW.getQueueName())){
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void handleConversationRequest(String conversationName, generated.dkf.Conversation conversation){
        startConversation(conversation.getType(), conversationName, false, null, domainKnowledgeManager, true, null);
    }

    @Override
    public void handleFeedbackUsingTUI(TutorUserInterfaceFeedback feedback, MessageCollectionCallback showFeedbackHandler) {

        if (isActive()) {

            String networkURL;

            try {

                networkURL = DomainModuleProperties.getInstance().getDomainContentServerAddress() + Constants.FORWARD_SLASH;

            } catch (Exception ex) {

                logger.error("Could not get the host IP address, defaulting to 'localhost'", ex);
                networkURL = DomainModuleProperties.getInstance().getTransferProtocol() + "localhost:" + DomainModuleProperties.getInstance().getDomainContentServerPort() + "/";
            }

            // Assumes that the avatar data of the avatar action has a URL that is a path hosted by the domain module
            // Tacks on the domain module's IP URL to make it accessible beyond this host
            if (feedback.getDisplayAvatarAction() != null && feedback.getDisplayAvatarAction().getAvatar() != null) {

                AvatarData avatarData = feedback.getDisplayAvatarAction().getAvatar();

                AvatarData domainHostedAvatarData;
                if(avatarData.getURL().startsWith(networkURL)){
                    domainHostedAvatarData = new AvatarData(avatarData.getURL(), avatarData.getHeight(), avatarData.getWidth());
                }else{
                    domainHostedAvatarData = new AvatarData(networkURL + avatarData.getURL(), avatarData.getHeight(), avatarData.getWidth());
                }

                feedback.getDisplayAvatarAction().setAvatar(domainHostedAvatarData);
            }

            // Assumes that the URL of the audio files are on a path hosted by the domain module
            // Tacks on the domain module's IP URL to make it accessible beyond this host
            if (feedback.getPlayAudioAction() != null) {

                PlayAudioAction audioAction = feedback.getPlayAudioAction();

                String mp3File = audioAction.getMp3AudioFile();

                mp3File = prependDomainContentServerAddressToFile(mp3File);

                String oggFile = audioAction.getOggAudioFile();

                oggFile = prependDomainContentServerAddressToFile(oggFile);

                PlayAudioAction domainAudioAction = new PlayAudioAction(mp3File, oggFile);

                feedback.setPlayAudioAction(domainAudioAction);
            }

            if(showFeedbackHandler == null){
                // create a callback so the feedback message will be processed ACK'ed
                // which is useful for ERT analysis to see that it was delivered.

                showFeedbackHandler =
                    new MessageCollectionCallback() {

                        @Override
                        public void success() {
                            // Do nothing
                        }

                        @Override
                        public void received(Message msg) {
                            // Do nothing
                        }

                        @Override
                        public void failure(Message msg) {

                            //TODO: this is serious and should be handled.  Should the course end with an error?
                            logger.warn("TUI administered feedback message failed to be responded to: "
                                    + "NACK Received: " + msg);
                        }

                        @Override
                        public void failure(String why) {

                            //TODO: this is serious and should be handled.  Should the course end with an error?
                            logger.warn("TUI administered feedback message failed to be responded to: "
                                    + why);
                        }
                    };
            }

            sendTUIFeedbackRequest(feedback, showFeedbackHandler);
        }
    }

    /**
     * Prepends the given file with the domain content server address. If the file already starts with the
     * network Url, it will assume the file has already been prepended and will do nothing.
     *
     * @param file the relative path to the file to access
     * @return the url to the file on the domain content server
     */
    public String prependDomainContentServerAddressToFile(String file) {

        if (StringUtils.isBlank(file)) {
            return file;
        }

        String networkURL;

        try {

            networkURL = DomainModuleProperties.getInstance().getDomainContentServerAddress() + Constants.FORWARD_SLASH;

        } catch (Exception ex) {

            logger.error("Could not get the host IP address, defaulting to 'localhost'", ex);
            networkURL = DomainModuleProperties.getInstance().getTransferProtocol() + "localhost:" + DomainModuleProperties.getInstance().getDomainContentServerPort() + "/";
        }

        if(!file.startsWith(networkURL)) {

            // need to add course folder path from Domain folder to get domain hosted path to HTML file
            file = runtimeCourseFolderRelativePath + Constants.FORWARD_SLASH + file;

            // firefox browsers do not automatically replace backslashes, resulting in invalid urls
            file = UriUtil.makeURICompliant(file);

            file = networkURL + file;
        }
        return file;
    }
    
    /**
     * Return the enumerated lesson state this domain session is in.  See {@link #currentLessonState} for more information.
     * @return can be null if not currently in a training application course object.
     */
    public LessonStateEnum getLessonState(){
        return currentLessonState;
    }

    @Override
    public void displayDuringLessonSurvey(final AbstractSurveyLessonAssessment surveyAssessment, final SurveyResultListener surveyResultListener) {

        if (isActive()) {

            // Notify the user of the gift mobile app that a survey is waiting for them, this is needed
            // in case the mobile device is not in their hand or they are not looking at it.
            if(webClientInfo != null &&
                    webClientInfo.isMobile()){

                generated.dkf.Message notifyAppUser = new generated.dkf.Message();
                notifyAppUser.setContent("GIFT is requesting your input.");
                generated.dkf.Message.Delivery delivery = new generated.dkf.Message.Delivery();
                generated.dkf.Message.Delivery.InTrainingApplication inTrainingApp = new generated.dkf.Message.Delivery.InTrainingApplication();
                inTrainingApp.setEnabled(generated.dkf.BooleanEnum.TRUE);
                generated.dkf.Message.Delivery.InTrainingApplication.MobileOption mobileOpt = new generated.dkf.Message.Delivery.InTrainingApplication.MobileOption();
                mobileOpt.setVibrate(true);
                inTrainingApp.setMobileOption(mobileOpt);
                delivery.setInTrainingApplication(inTrainingApp);
                notifyAppUser.setDelivery(delivery);
                handleFeedbackUsingTrainingApp(notifyAppUser);
            }

            if (entityTable != null) {
                entityTable.setPaused(true);
            }

            if(logger.isInfoEnabled()){
                logger.info("Pausing scenario to display during lesson survey.");
            }

            final Object lock = new Object();

            /*
             * Spawn a new thread that will request the TUI to present the given mid-lesson media and notify this
             * thread when the learner finishes viewing all of the media.
             */
            Thread surveyThread = new Thread(new Runnable() {

                @Override
                public void run() {

                    MessageCollectionCallback pauseCallback = new MessageCollectionCallback() {

                        @Override
                        public void success() {

                            changeTrainingAppState(TrainingApplicationStateEnum.PAUSED);

                            if(surveyAssessment instanceof GIFTSurveyLessonAssessment){

                                //get survey context
                                int surveyContextId = domainKnowledgeManager.getAssessmentKnowledge().getScenario().getResources().getSurveyContextId();
                                GetSurveyRequest request = new GetSurveyRequest(surveyContextId, ((GIFTSurveyLessonAssessment)surveyAssessment).getGiftKey());

                                DisplaySurveyParamWrapper displaySurveyParamWrapper = new DisplaySurveyParamWrapper();
                                displaySurveyParamWrapper.fullScreen = false;
                                displaySurveyParamWrapper.showInAAR = false;
                                displaySurveyParamWrapper.allowGatewaySurveyResponse = true;
                                displaySurveyParamWrapper.isMidLessonSurvey = true;

                                SimpleMandatoryBehavior alwaysDoSurvey = new SimpleMandatoryBehavior();
                                alwaysDoSurvey.setUseExistingLearnerStateIfAvailable(false);
                                displaySurveyParamWrapper.mandatoryBehavior = alwaysDoSurvey;

                                presentSurvey(DomainCourseFileHandler.getCourseObjectName(currentCourseObject.getCourseObject()), request, displaySurveyParamWrapper, surveyResultListener, new AsyncActionCallback() {

                                    @Override
                                    public void onFailure(Exception e) {

                                        synchronized(lock) {

                                            //the operation resulted in failure, so we need to resume the strategy handler
                                            lock.notifyAll();
                                        }

                                        onSessionError(e);
                                    }

                                    @Override
                                    public void onSuccess() {


                                        if(TrainingApplicationStateEnum.isValidTransition(currentTrainingAppState, TrainingApplicationStateEnum.RUNNING) &&
                                                currentLessonState != LessonStateEnum.STOPPED){
                                            //MH 09.12.14 - if the midlesson survey causes the lesson to be completed (stopped), the resume shouldn't happen
                                            //              For some reason the TA hasn't replied to the stop yet.

                                            MessageCollectionCallback mCollection = new MessageCollectionCallback() {

                                                @Override
                                                public void success() {

                                                    synchronized(lock) {

                                                        //the operation resulted in failure, so we need to resume the strategy handler
                                                        lock.notifyAll();
                                                    }

                                                    changeTrainingAppState(TrainingApplicationStateEnum.RUNNING);

                                                    if(entityTable != null) {

                                                        entityTable.setPaused(false);

                                                    }
                                                }

                                                @Override
                                                public void received(Message msg) {
                                                    //Do nothing
                                                }

                                                @Override
                                                public void failure(Message msg) {

                                                    synchronized(lock) {

                                                        //the operation resulted in failure, so we need to resume the strategy handler
                                                        lock.notifyAll();
                                                    }

                                                    onSessionError(generateException("Resuming the scenario after presenting a survey", msg));
                                                }

                                                @Override
                                                public void failure(String why) {

                                                    synchronized(lock) {

                                                        //the operation resulted in failure, so we need to resume the strategy handler
                                                        lock.notifyAll();
                                                    }

                                                    onSessionError(generateException("Resuming the scenario after presenting a survey", why));
                                                }
                                            };

                                            if(currentTrainingAppState != TrainingApplicationStateEnum.RUNNING){

                                                if(!resumeScenario(mCollection)){
                                                    //failed to resume the scenario, just log the event for now
                                                    if(logger.isInfoEnabled()){
                                                        logger.info("Some event occurred to change the current training app state to "+currentTrainingAppState+", therefore the request to resume scenario after the AutoTutor mid-lesson survey will not be applied.");
                                                    }
                                                }

                                            } else {

                                                //the scenario is already running, so there's no need to resume it
                                                mCollection.success();
                                            }

                                        }else{
                                            if(logger.isInfoEnabled()){
                                                logger.info("Ignoring request to resume scenario while waiting for a mid-lesson survey because the current training app state is "+currentTrainingAppState+" which requires no change based on this request.");
                                            }
                                        }
                                    }
                                });

                            }else if(surveyAssessment instanceof AutoTutorSurveyLessonAssessment){

                                presentSurvey((AutoTutorSurveyLessonAssessment)surveyAssessment, false, new AsyncActionCallback() {

                                    @Override
                                    public void onFailure(Exception e) {

                                        synchronized(lock) {

                                            //the operation resulted in failure, so we need to resume the strategy handler
                                            lock.notifyAll();
                                        }

                                        onSessionError(e);
                                    }

                                    @Override
                                    public void onSuccess() {

                                        if(currentTrainingAppState != null){
                                            //if in a training application state, then the app needs to be unpaused.

                                            if(TrainingApplicationStateEnum.isValidTransition(currentTrainingAppState, TrainingApplicationStateEnum.RUNNING)){

                                                MessageCollectionCallback mCollection = new MessageCollectionCallback() {

                                                    @Override
                                                    public void success() {

                                                        synchronized(lock) {

                                                            //the operation resulted in failure, so we need to resume the strategy handler
                                                            lock.notifyAll();
                                                        }

                                                        changeTrainingAppState(TrainingApplicationStateEnum.RUNNING);

                                                        if(entityTable != null) {

                                                            entityTable.setPaused(false);

                                                        }
                                                    }

                                                    @Override
                                                    public void received(Message msg) {
                                                        //Do nothing
                                                    }

                                                    @Override
                                                    public void failure(Message msg) {

                                                        synchronized(lock) {

                                                            //the operation resulted in failure, so we need to resume the strategy handler
                                                            lock.notifyAll();
                                                        }

                                                        onSessionError(generateException("Resuming the scenario after presenting an Auto Tutor survey", msg));
                                                    }

                                                    @Override
                                                    public void failure(String why) {

                                                        synchronized(lock) {

                                                            //the operation resulted in failure, so we need to resume the strategy handler
                                                            lock.notifyAll();
                                                        }

                                                        onSessionError(generateException("Resuming the scenario after presenting an Auto Tutor survey", why));
                                                    }
                                                };

                                                if(currentTrainingAppState != TrainingApplicationStateEnum.RUNNING){

                                                    if(!resumeScenario(mCollection)){
                                                        //failed to resume the scenario, just log an error
                                                        logger.info("Some event occurred to change the current training app state to "+currentTrainingAppState+", therefore the request to resume scenario after the AutoTutor mid-lesson survey will not be applied.");
                                                    }

                                                } else {

                                                    //the scenario is already running, so there's no need to resume it
                                                    mCollection.success();
                                                }

                                            }else{
                                                if(logger.isInfoEnabled()) {
                                                logger.info("Ignoring request to resume scenario while waiting for the AutoTutor mid-lesson survey because the current training app state is "+currentTrainingAppState+" which requires no change based on this request.");
                                            }
                                            }

                                        }else{
                                            //not in a training app and the course AutoTutor survey is complete, therefore
                                            //the lesson is done
                                            if(logger.isInfoEnabled()) {
                                                logger.info("The course level AutoTutor survey is complete, therefore that lesson has finished.");
                                            }
                                            changeLessonState(LessonStateEnum.STOPPED);

                                            synchronized(lock) {

                                                //the operation resulted in failure, so we need to resume the strategy handler
                                                lock.notifyAll();
                                            }
                                        }

                                    }
                                });
                            }else{
                                synchronized(lock) {

                                    //the operation resulted in failure, so we need to resume the strategy handler
                                    lock.notifyAll();
                                }

                                onSessionError(generateException("Unable to present mid lesson survey", "Found unhandled mid lesson survey type of "+surveyAssessment+"."));
                            }
                        }

                        @Override
                        public void received(Message msg) {
                            //Do nothing
                        }

                        @Override
                        public void failure(Message msg) {

                            synchronized(lock) {

                                //the learner has finished digesting the mid-lesson media content, so resume the strategy handler
                                lock.notifyAll();
                            }

                            onSessionError(generateException("Pausing the scenario to present a survey", msg));
                        }

                        @Override
                        public void failure(String why) {

                            synchronized(lock) {

                                //the learner has finished digesting the mid-lesson media content, so resume the strategy handler
                                lock.notifyAll();
                            }

                            onSessionError(generateException("Pausing the scenario to present a survey", why));
                        }
                    };

                    if (currentTrainingAppState != TrainingApplicationStateEnum.PAUSED) {

                        try {

                            // the current training application is not paused, so we need to pause it for the
                            // mid-lesson survey
                            pauseScenario(pauseCallback);

                        }catch (Exception e) {

                            synchronized(lock) {

                                //the learner has finished digesting the mid-lesson media content, so resume the strategy handler
                                lock.notifyAll();
                            }

                            onSessionError(generateException(
                                    "Requesting to pausing the scenario to present survey",
                                    "Failed to initiate scenario pause due to an error: " + e));
                        }

                    } else {

                        // the current training application is already paused, so go ahead and show the
                        // mid-lesson survey
                        pauseCallback.success();
                    }
                }
            }, "Mid-lesson survey strategy handler -"+(this.domainSessionInfo.getUsername() != null ? this.domainSessionInfo.getUsername() : this.domainSessionInfo.getDomainSessionId()));

            surveyThread.start();

            synchronized(lock) {

                try {

                    /*
                     * Pause the strategy handler while the learner fills out the mid-lesson survey. If we don't pause
                     * the strategy handler, then other strategies in the same pedagogical request can interrupt the mid-lesson
                     * survey and cause the learner to navigate away from it.
                     */
                    lock.wait();

                } catch (InterruptedException e) {
                    logger.error("Interupted a paused strategy handler that was waiting for the learner ("+getDomainSessionInfo()+") to finish mid-lesson survey.", e);
                }
            }



        }
    }

    @Override
    public void performanceAssessmentCreated(PerformanceAssessment performanceAssessment) {

        if (isActive()) {

            buildAndSendPerformanceAssessment(performanceAssessment);
        }
    }

    @Override
    public void handleScenarioAdaptation(generated.dkf.EnvironmentAdaptation value, Double strategyStress) {

        /* domain session is still active AND not in playback mode for the
         * current course object Playback mode usually means there is no
         * training application where learners are actively involved, creating
         * game state, and where a scenario adaptation would be applied to
         * affect that environment and possible changes in future game state
         * messages because its a log file playback. */
        playbackServiceFuture.thenAccept(playbackService -> {
            if (isActive() && playbackService == null) {

                //TODO: Eventually the domain knowledge might care about whether
                //      the scenario adaptation was actually applied, then use
                //      callback
                if (value != null) {

                    sendSetEnvironment(value, strategyStress, new MessageCollectionCallback() {

                        @Override
                        public void success() {
                            // Do nothing
                        }

                        @Override
                        public void received(Message msg) {
                            // Do nothing
                        }

                        @Override
                        public void failure(Message msg) {

                            logger.warn("Could not set environment for scenario adaptation.",
                                    generateException("Setting Environment", msg));
                        }

                        @Override
                        public void failure(String why) {

                            logger.warn("Could not set environment for scenario adaptation.",
                                    generateException("Setting Environment", why));
                        }
                    });

                } else {

                    logger.error("Recevied unhandled scenario adaptation value of " + value);
                }
            }
        });
    }

    @Override
    public void handleTutorChatUpdate(final DisplayChatWindowUpdateRequest request){

        if (isActive()) {

            sendDisplayChatWindowUpdateRequest(request, new MessageCollectionCallback() {

                @Override
                public void success() {
                    // Do nothing
                }

                @Override
                public void received(Message msg) {
                    // Do nothing
                }

                @Override
                public void failure(Message msg) {

                    logger.warn("Could not update the chat window with request of "+request+".",
                            generateException("Updating Chat Window", msg));
                }

                @Override
                public void failure(String why) {

                    logger.warn("Could not update the chat window with request of "+request+".",
                            generateException("Updating Chat Window", why));
                }
            });
        }
    }

    /**
     * Displays the next transition defined by the domain knowledge, shutting
     * down the domain session if there are no more transitions to display
     */
    private void displayNextTransition() {

        if(courseManager == null){

            if(localSessionState == LOCAL_SESSION_STATE.CLOSED){
                // some callback logic must have called this method but the session is or has been cleaned up
                return;
            }else{
                fatalError("Unable to display the next course object", "The course manager is null for some reason.  This should only happen when the course is ending.");
                return;
            }
        }

        if (!courseManager.hasMoreTransitions()) {

            if(logger.isInfoEnabled()){
                logger.info("There are no more transitions, therefore ordering the graceful closing of the domain session ("+this.domainSessionInfo+").");
            }

            // Indicate that the progress is completed.
            courseManager.setProgressToEnd();

            // Report the final progress.
            courseManager.reportProgress(true);

            // No more transitions, close the domain session and clean up
            closeDomainSession(new AsyncActionCallback() {
                @Override
                public void onFailure(Exception e) {

                    logger.error("Caught an exception while ending the domain session.", e);

                    // An error here shouldn't stop the shutdown of a domain session
                    onSuccess();
                }

                @Override
                public void onSuccess() {

                    cleanUp();
                }
            });

        } else {

            if(logger.isInfoEnabled()){
                logger.info("Retrieving next transition to handle... ("+this.domainSessionInfo+")");
            }

            try{
                CourseObjectWrapper nextCourseObject = courseManager.getNextTransition();
                if (DomainCourseFileHandler.isTransitionDisabled(nextCourseObject.getCourseObject())) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Skipping disabled transition: " + DomainCourseFileHandler.getTransitionName(nextCourseObject.getCourseObject()));
                    }
                    displayNextTransition();
                } else {
                    // Report the progress before moving onto the next transition.
                    // This must be done before handleTransition() is called or there can be threading issues when the transitions are quickly completed.
                    courseManager.reportProgress(false);

                    // Move onto the next transition.
                    handleTransition(nextCourseObject);
                }
            }catch(DKFValidationException dkfException){
                logger.error("Caught DKF exception while trying to handle the next course element to present.", dkfException);
                fatalError(dkfException.getReason(), dkfException.getDetails());
            }catch(Throwable t){
                
                String errorDetails = t.getMessage();
                if(errorDetails == null && t.getStackTrace().length > 0){
                    errorDetails = t.getStackTrace()[0].toString();
                }
                logger.error("Caught exception while trying to handle the next course element to present for "+getDomainSessionInfo()+".", t);
                fatalError("There was a severe problem while trying to handle the next course element to present.", errorDetails);
            }

        }
    }

    /**
     * Handle the training application course transition by dealing with it's authored content.  Ultimately
     * this is the start of the logic that will configure GIFT to communicate with and assess training application
     * content.
     *
     * @param trainingAppCourseObjectWrapper the course transition information to use to configure GIFT for assessing
     * training app content.
     * @param courseState a course state to send that mentions the next course activity is a training application.
     * Can't be null.
     */
    private void handleTrainingAppCourseTransition(CourseObjectWrapper trainingAppCourseObjectWrapper, CourseState courseState){

        try {

            generated.course.TrainingApplication trainingAppCourseObject = (generated.course.TrainingApplication)trainingAppCourseObjectWrapper.getCourseObject();

            //this callback is used to clear the feedback history in the TUI once the Training App course element is over
            cachedTaFinishedCallback = new AsyncActionCallback() {

                @Override
                public void onSuccess() {

                    // The training app has stopped.  Clear all feedback messages from the TUI.
                    TutorUserInterfaceFeedback clearTextMsg = new TutorUserInterfaceFeedback(
                            null, null, null, new ClearTextAction(), null);

                    MessageCollectionCallback showFeedbackHandler =
                            new MessageCollectionCallback() {
                                @Override
                                public void success() {
                                    transitionCallback.onSuccess();
                                }

                                @Override
                                public void received(Message msg) {
                                    // Do nothing
                                }

                                @Override
                                public void failure(Message msg) {
                                    logger.warn("TUI administered clear feedback message failed to be responded to: "
                                            + "NACK Received: " + msg);
                                    transitionCallback.onFailure(new Exception("The tutor was unable to clear the feedback history."));
                                }

                                @Override
                                public void failure(String why) {
                                    logger.warn("TUI administered clear feedback message failed to be responded to: "
                                            + why);
                                    transitionCallback.onFailure(new Exception("The tutor was unable to clear the feedback history because '"+why+"'."));
                                }
                            };

                    // Send the message to clear the feedback messages from the TUI.
                    sendTUIFeedbackRequest(clearTextMsg, showFeedbackHandler);

                }

                @Override
                public void onFailure(Exception e) {
                    transitionCallback.onFailure(e);
                }
            };

            // Initialize the domain knowledge manager which is needed to get the scenario data prior
            // to determining if we need to transition into a team session or just proceed into loading the training application.
            this.cachedTrainingAppTransition = trainingAppCourseObjectWrapper;
            cachedTrainingAppHandler = new TrainingApplicationHandler(trainingAppCourseObject);
            initializeDomainKnowledgeManager(trainingAppCourseObject, cachedTrainingAppHandler);

            //
            // update the course state (and notify the ped module)
            //
            if(domainKnowledgeManager.shouldAllowRemediation()){
                // mark the next quadrant as practice so that the ped module can reply with remediation
                // requests after the training app course object's scenario is finished being assessed
                courseState.setNextQuadrant(MerrillQuadrantEnum.PRACTICE);
                expandableCourseObjectState = ExpandableCourseObjectStateEnum.TRAINING_APPLICATION;

                // FIRST see if this is a repeat of the training application course object from remediation looping option
                DynamicContentHandler handler = courseManager.getTrainingAppRemediationHandler();
                if(handler == null){
                    // SECOND this is the first instance, need to create a handler to manage remediation
                    handler = new DynamicContentHandler(trainingAppCourseObject.getTransitionName(),
                            courseManager.getCourseRuntimeDirectory(), courseManager.getCourseAuthoredDirectory(), MerrillQuadrantEnum.PRACTICE);

                    generated.course.TrainingApplication.Options.Remediation remediationOptions = domainKnowledgeManager.getRemediationOptions();
                    if(remediationOptions != null){
                        // set any options that were authored

                        if(remediationOptions.getAllowedAttempts() != null){
                            handler.setAllowedPracticeAttempts(remediationOptions.getAllowedAttempts().intValue());
                        }

                        if(remediationOptions.getExcludeRuleExampleContent() != null){
                            handler.setExcludeRuleExampleContent(remediationOptions.getExcludeRuleExampleContent() == BooleanEnum.TRUE);
                        }
                    }
                    courseManager.setTrainingAppRemediationHandler(handler);
                }
            }
            
            // make sure the course state being sent notifies other modules whether this training app course object has remediation potential
            courseState.setExpandableCourseObjectState(expandableCourseObjectState);
            sendCourseState(courseState, null);
            
            boolean creatingPlayback = false;
            final generated.course.TrainingApplication trainingAppTransition = (generated.course.TrainingApplication)trainingAppCourseObjectWrapper.getCourseObject();
            if (trainingAppTransition.getInterops() != null) {
                generated.course.Interops interops = trainingAppTransition.getInterops();

                if(DomainCourseFileHandler.hasInteropNeedingPlayback(interops)) {
                    /* Create a playback service to generate GIFT messages from a
                     * specified log if requested. */
                    creatingPlayback = true;
                    playbackServiceFuture = cachedTrainingAppHandler.createPlaybackServiceIfRequested(courseManager.getCourseRuntimeDirectory().getFolder(), interops,
                            domainSessionInfo, logPlaybackServiceTopic);                    
                    
                    try{
                        // wait until the playback service has been created in order to determine if there 
                        // was an error creating it
                        playbackServiceFuture.join();
                    }catch (Throwable t){
                        // There was an error, close out the CompletableFuture so it isn't used again
                        playbackServiceFuture.completeExceptionally(t);
                        
                        throw t;
                    }
                }
            }

            Scenario scenario = domainKnowledgeManager.getAssessmentKnowledge().getScenario();
            if(scenario.supportsMultipleLearners() || 
                    LessonLevelEnum.RTA.equals(DomainModuleProperties.getInstance().getLessonLevel())){
                // the team knowledge session needs to be built with user ids and role assignments based
                // on information outside of the domain module (e.g. TUI Team Session lobby, ManageTeamMembershipRequest message from GW via RTA Test harness)
                // The DKF being used in the training app course object should have multiple playable team member positions
                // if using the TUI but in RTA mode, GIFT will need all user ids even if a single playable position exists.
                
                if(creatingPlayback){
                    // the training app course object is going to be using knowledge session playback
                    
                       playbackServiceFuture.thenAccept(playbackService -> {
                            if(playbackService != null){
                                
                                cachedTrainingAppHandler.setTeamSessionPlayback(playbackService.isTeamSession());

                                if(playbackService.isTeamSession()){                                    
                                    // the playback should start sending the necessary messages to create team session, and assign
                                    // learners (from the log) to member positions in DKF
                                    
                                    if(logger.isDebugEnabled()){
                                        logger.debug("Found team session playback and multiple learner DKF.  Creating team session...");
                                    }
                                    
                                    createTeamSession(trainingAppCourseObject.getTransitionName(), false, SessionType.ACTIVE_PLAYBACK, new AsyncActionCallback() {
                                        
                                        @Override
                                        public void onSuccess() {
                                            
                                            if(logger.isDebugEnabled()){
                                                logger.debug("Team session created.  Starting playback...");
                                            }
                                            
                                            // start playback to send the messages to create the team session
                                            playbackServiceFuture.thenAccept(playbackService -> {
                                                if (playbackService != null) {
                                                    /* Start playback; ignore any previous message delay from
                                                     * pausing since this is the 'beginning' of the playback lesson */
                                                    playbackService.startPlayback(true);
                                                }
                                            });                            
                                        }
                                        
                                        @Override
                                        public void onFailure(Exception e) {
                                            logger.error("Failed to create the team knowledge session for playback",e);
                                            onSessionError("Unable to proceed with starting the playback of a team knowledge session because the team knowledge session could not be created due to an issue.", e.getMessage());
                                        }
                                    });
                                }else{
                                    // the playback won't have message to start a team knowledge session, continue loading the training app course object
                                    // which will manually create an individual knowledge session
                                    
                                    if(logger.isDebugEnabled()){
                                        logger.debug("Found individual session playback.  Continuing to enter training app course object normally.");
                                    }
                                    try{
                                        displayTrainingApplicationTransition(trainingAppCourseObjectWrapper, cachedTrainingAppHandler, cachedTaFinishedCallback);
                                    } catch (Throwable e) {

                                        logger.error("Caught exception while trying to handle training application transition", e);

                                        onSessionError("Unable to proceed with displaying a Training Application course element due to an issue.", e.getMessage());
                                    }
                                }
                            }
                        });

                    

	            }else{
    	            // go down the path of showing the team session lobby in the TUI
    	            if(logger.isDebugEnabled()){
    	                logger.debug("Found playable team session (not a log playback).  Sending display team session request.");
    	            }
    	            
    	            boolean inheritedPreviousMembers = false;
    	            
    	            if(previousKnowledgeSession != null && previousKnowledgeSession instanceof TeamKnowledgeSession) {
    	            	
    	            	Map<String, SessionMember> roleNameToPreviousMember = new HashMap<>();
    	            	
    	            	/* if a session was just completed, check if all of the role assignments from the previous 
    	            	 * session can be reused */
    	            	boolean canInheritMembers = true;
    	            	for(SessionMember previousMember : previousKnowledgeSession.getSessionMembers().getSessionMemberDSIdMap().values()) {
    	            		
    	            		String roleName = previousMember.getSessionMembership().getTeamMember().getName();
    	            		if(!scenario.getRootTeam().getPlayableTeamMemberNames().contains(roleName)) {
    	            			canInheritMembers = false;
    	            			break;
    	            		}
    	            		roleNameToPreviousMember.put(roleName, previousMember);
    	            	}
    	            	
    	            	if(canInheritMembers) {
    	            	    
    	            	    if(previousKnowledgeSession.getHostSessionMember().getDomainSessionId() == domainSessionInfo.getDomainSessionId()) {
                            
                                /* This domain session is the host, so make a new knowledge session with the same role assignments */
    	                        
                                /* NOTE: The naming code below is based off of 
        	            		 * TutorInterInterfaceServiceImpl.hostTeamSessionRequest and should ideally be
        	            		 * kept consistent with it */
        	            		final String SESSION_NAME_SUFFIX;
        	            		String domainSourceId = domainSessionInfo.getDomainSourceId();
        	                    if(domainSourceId.contains(Constants.FORWARD_SLASH)){
        	                        int courseFolderEnd = domainSourceId.lastIndexOf(Constants.FORWARD_SLASH);
        	                        String noCourseXML = domainSourceId.substring(0, courseFolderEnd);
        	                        int courseFolderStart = noCourseXML.lastIndexOf(Constants.FORWARD_SLASH);
        	                        if(courseFolderStart != -1){
        	                            SESSION_NAME_SUFFIX = noCourseXML.substring(courseFolderStart+1);
        	                        }else{
        	                            SESSION_NAME_SUFFIX = noCourseXML;
        	                        }
        	                    }else{
        	                        SESSION_NAME_SUFFIX = "Hosted Session";
        	                    }
        	                    
        	                    String sessionName = SESSION_NAME_SUFFIX;
        	            		
        	                    /* Create a new team session so that the role assignments can be inherited */
            	            	createTeamSession(sessionName, false, SessionType.ACTIVE, new AsyncActionCallback() {
        							
        							@Override
        							public void onSuccess() {
        								
        								TeamKnowledgeSession tKnowledgeSession = 
        										(TeamKnowledgeSession) KnowledgeSessionManager.getInstance().getKnowledgeSessions().get(domainSessionInfo.getDomainSessionId());    								
        								
        								/* Iterate through each role name in the new session and assign a team member to it using
        								 * the role assignments from the previous session. */
        								for(String roleName : tKnowledgeSession.getTeamStructure().getPlayableTeamMemberNames()) {
        	    	            			
        									SessionMember prevMember = roleNameToPreviousMember.get(roleName);
        									if(prevMember == null) {
        										
        										/* No learner assigned to this role, so skip it */
        										continue;
        									}
        									
        									TeamMember<?> member = (TeamMember<?>) tKnowledgeSession.getTeamStructure().getTeamElement(roleName);
        	    	            			
        									if(prevMember.getDomainSessionId() != domainSessionInfo.getDomainSessionId()) {
                                                
                                                IndividualMembership individualMembershipJoiner = new IndividualMembership(prevMember.getUserSession().getUsername());
                                                SessionMember joiningSessionMember = new SessionMember(individualMembershipJoiner, prevMember.getUserSession(), prevMember.getDomainSessionId());
                                                KnowledgeSessionManager.getInstance().joinTeamSession(
                                                        domainSessionInfo.getDomainSessionId(), 
                                                        joiningSessionMember, 
                                                        domainSessionInfo.getDomainSourceId());
                                            }
        									
        	    	            			try {
        	    	            				/* Assign this role based on the learner's role in the previous knowledge session*/
        	    	            				KnowledgeSessionManager.getInstance().assignTeamMember(
        	    	    								previousKnowledgeSession.getHostSessionMember().getDomainSessionId(), 
        	    	    								prevMember.getDomainSessionId(), 
        	    	    								member
        	    	    						);
        	    	    						
        	    	    					} catch (ManageTeamMembershipException e) {
        	    	    						logger.error("Unable to inherit team membership for role '" 
        	    	    								+ roleName +  "' and domain session '" + prevMember.getDomainSessionId() + "'.", e);
        	    	    					}
        	    	            		}
        								
        								try {
        									/* Start the knowledge session*/
    										DomainModule.getInstance().startTeamKnowledgeSessionForAllMembers(BaseDomainSession.this);
    									
        								} catch (Exception e) {
        									logger.error("Unable to start inherrited team session for domain session " + domainSessionInfo.getDomainSessionId(), e);
    									}
        							}
        							
        							@Override
        							public void onFailure(Exception e) {
        								logger.error("Unable to create inheritted team session for domain session " + domainSessionInfo.getDomainSessionId(), e);
        							}
        						});
            	            	
            	            	inheritedPreviousMembers = true;
    	            	    
    	            	    }
    	            	}
    	            }
    	            
    	            if(!inheritedPreviousMembers) {
    	            	
    	            	/* If we're not re-using an existing set of role assignments from a previous
    	            	 * session, then need to prompt the learners to assign their roles*/
    	            	sendDisplayTeamSession();
    	            }
	            }
	        } else {
	            if(logger.isDebugEnabled()){
                    logger.debug("The knowledge session is single player.  Continuing to enter training app course object normally.");
                }
                displayTrainingApplicationTransition(trainingAppCourseObjectWrapper, cachedTrainingAppHandler, cachedTaFinishedCallback);
            }

        } catch (Throwable e) {

            logger.error("Caught exception while trying to handle training application transition", e);

            onSessionError("Unable to proceed with displaying a Training Application course element due to an issue.", e.getMessage());
        }
    }

    /**
     * Handle the provided transition and determine what actions need to take place among the modules
     * to execute on the transition.  This may involve displaying something new to the learner through
     * the tutor or configuring a training application to run a scenario/file.
     *
     * @param courseObjectWrapper the next course object to execute
     * @throws IOException if there was a problem parsing the course or any related course files
     * @throws DKFValidationException if there was a problem parsing a DKF associated with a course transition
     * @throws FileValidationException if there was a problem validating (e.g. against the schema) a file associated with a course transition
     */
    private void handleTransition(CourseObjectWrapper courseObjectWrapper) throws IOException, FileValidationException, DKFValidationException{

        //Resets the flag for whether the current
        //course object is an embedded training
        //application
        isCurrentAppEmbedded = false;

        if (courseObjectWrapper != null) {

            if (logger.isInfoEnabled()) {
                logger.info("Displaying course object: " + DomainCourseFileHandler.getTransitionName(courseObjectWrapper.getCourseObject()) + " ("+this.domainSessionInfo+")");
            }

            // let dynamic content handler know about course content being changed in order to track
            // presentation duration of content
            courseManager.getMerrillsBranchPointManager().notifyNextCourseObject(currentCourseObject, courseObjectWrapper);

            if (courseObjectWrapper.getCourseObject() instanceof generated.course.LessonMaterial) {

                //update current object to authored object being handled
                currentCourseObject = courseObjectWrapper;

                //update the course state and send when appropriate depending on the transition type
                CourseState state = new CourseState(generated.course.LessonMaterial.class.getCanonicalName());
                state.setExpandableCourseObjectState(expandableCourseObjectState);
                sendCourseState(state, null);

                displayLessonMaterialTransition(courseObjectWrapper, transitionCallback);

            } else if (courseObjectWrapper.getCourseObject() instanceof generated.course.TrainingApplication) {

                //update current object to authored object being handled
                currentCourseObject = courseObjectWrapper;

                CourseState state = new CourseState(generated.course.TrainingApplication.class.getCanonicalName());

                // Simply proceed to handle the training application course transition normally.
                handleTrainingAppCourseTransition(courseObjectWrapper, state);

            } else if (courseObjectWrapper.getCourseObject() instanceof generated.course.AAR) {

                //update current object to authored object being handled
                currentCourseObject = courseObjectWrapper;

                //update the course state and send when appropriate depending on the transition type
                CourseState state = new CourseState(generated.course.AAR.class.getCanonicalName());
                state.setExpandableCourseObjectState(expandableCourseObjectState);   // in case this AAR is part of an expandable course object
                sendCourseState(state, null);

                displayAfterActionReviewTransition((generated.course.AAR) courseObjectWrapper.getCourseObject(), transitionCallback);

            } else if (courseObjectWrapper.getCourseObject() instanceof generated.course.Guidance) {

                //update current object to authored object being handled
                currentCourseObject = courseObjectWrapper;

                //update the course state and send when appropriate depending on the transition type
                CourseState state = new CourseState(generated.course.Guidance.class.getCanonicalName());
                state.setExpandableCourseObjectState(expandableCourseObjectState);  // in case this guidance is part of an expandable course object
                sendCourseState(state, null);

                displayGuidanceTransition((generated.course.Guidance) courseObjectWrapper.getCourseObject(), false, transitionCallback);

            } else if (courseObjectWrapper.getCourseObject() instanceof generated.course.PresentSurvey) {

                //update current object to authored object being handled
                currentCourseObject = courseObjectWrapper;

                //update the course state and send when appropriate depending on the transition type
                CourseState state = new CourseState(generated.course.PresentSurvey.class.getCanonicalName());
                state.setExpandableCourseObjectState(expandableCourseObjectState);  // in case this survey is part of an expandable course object
                sendCourseState(state, null);

                displaySurveyTransition((generated.course.PresentSurvey) courseObjectWrapper.getCourseObject(), transitionCallback);

            } else if (courseObjectWrapper.getCourseObject() instanceof generated.course.MerrillsBranchPoint){

                //update current object to authored object being handled
                currentCourseObject = courseObjectWrapper;

                //A Merrill's Branch point (MBP) transition object is used as a way to retrieve the next
                //quadrant's transition during runtime.  The way it works is that each time a MPB is reached
                //the eMAP Pedagogy is allowed to inject what the next quadrant will be in the course flow.
                //After that injection is performed, a copy of the MBP is inserted after that new course transition.
                //This process repeats until the Pedagogy indicates the MBP should be exited because the learner
                //has comprehended the concepts taught in the MBP.

                try{

                    handleMerrilsBranchPointTransition(courseObjectWrapper, transitionCallback);

                }catch(Exception e){

                    logger.error("Caught exception while trying to handle Merrills Branch Point transition", e);
                    onSessionError("Unable to proceed with Merrills Branch Point due to an issue.", e.getMessage());
                }

            } else if (courseObjectWrapper.getCourseObject() instanceof MerrillsBranchPointHandler.AbstractExpandedCourseObject){

                // This course object type is created by the logic behind the adaptive courseflow (more specifically
                // the Adaptive Courseflow handler class.  It contains the course object information to satisfy the next pahse to execute
                // in the adaptive courseflow course object.

                Serializable courseObjectImpl =
                        ((MerrillsBranchPointHandler.AbstractExpandedCourseObject) courseObjectWrapper.getCourseObject()).getCourseObject();

                currentCourseObject = courseObjectWrapper;

                //update the course state and send (to the Pedagogy)
                CourseState state = new CourseState(courseObjectImpl.getClass().getCanonicalName());
                MerrillQuadrantEnum nextQuadrant = null;
                if(courseObjectWrapper.getCourseObject() instanceof RuleCourseObject){
                    nextQuadrant = MerrillQuadrantEnum.RULE;
                }else if(courseObjectWrapper.getCourseObject() instanceof ExampleCourseObject){
                    nextQuadrant = MerrillQuadrantEnum.EXAMPLE;
                }else if(courseObjectWrapper.getCourseObject() instanceof RecallCourseObject){
                    nextQuadrant = MerrillQuadrantEnum.RECALL;
                }else if(courseObjectWrapper.getCourseObject() instanceof PracticeCourseObject){
                    nextQuadrant = MerrillQuadrantEnum.PRACTICE;
                }else if(courseObjectWrapper.getCourseObject() instanceof RemediationCourseObject){
                    nextQuadrant = MerrillQuadrantEnum.REMEDIATION_AFTER_RECALL;
                }
                state.setNextQuadrant(nextQuadrant);

                //handle the transition type -> Training App (usually for Rule, Example, Practice) and Present Survey (usually for Recall)
                if(courseObjectImpl instanceof generated.course.Recall.PresentSurvey){
                    displayRecallSurvey((RecallCourseObject) courseObjectWrapper.getCourseObject(), transitionCallback);
                }else if(courseObjectImpl instanceof generated.course.TrainingApplication){

                    // remove the adaptive courseflow expanded course object container
                    CourseObjectWrapper newTrainingAppWrapper = new CourseObjectWrapper(courseObjectImpl, courseObjectWrapper.getCourseObjectReference());
                    handleTrainingAppCourseTransition(newTrainingAppWrapper, state);

                }else if(courseObjectImpl instanceof generated.course.Guidance){
                    displayGuidanceTransition((generated.course.Guidance) courseObjectImpl, false, transitionCallback);
                }else if(courseObjectImpl instanceof generated.course.LessonMaterial){

                    // remove the adaptive courseflow expanded course object container
                    CourseObjectWrapper newLessonMaterialWrapper = new CourseObjectWrapper(courseObjectImpl, courseObjectWrapper.getCourseObjectReference());
                    displayLessonMaterialTransition(newLessonMaterialWrapper, transitionCallback);

                }else if(courseObjectImpl instanceof generated.course.PresentSurvey){
                    displaySurveyTransition((generated.course.PresentSurvey)courseObjectImpl, transitionCallback);
                } else {
                    // Getting here is pretty serious
                    onSessionError("Unable to proceed in this  an Adaptive courseflow course objectt.", "Unknown/Unhandled Adaptive courseflow content course object type: " + courseObjectImpl.getClass().getCanonicalName());
                }

            } else if (courseObjectWrapper.getCourseObject() instanceof AuthoredBranch){

                //update current object to authored object being handled
                currentCourseObject = courseObjectWrapper;

                try{

                    handleAuthoredBranchTransition((generated.course.AuthoredBranch) courseObjectWrapper.getCourseObject(), transitionCallback);

                }catch(Exception e){

                    logger.error("Caught exception while trying to handle an Authord Branch course object", e);
                    onSessionError("Unable to proceed with Authored Branch course object due to an issue.", e.getMessage());
                }

            } else if (courseObjectWrapper.getCourseObject() instanceof PathEnd){
                // reached the end of a path, send a BranchPathHistoryUpdate message for the ERT

                handlePathEnd((PathEnd)courseObjectWrapper.getCourseObject());

            } else if (courseObjectWrapper.getCourseObject() instanceof generated.course.AuthoredBranch.Paths.Path.Courseobjects.End){
                //reached a course end course object in an authored branch path, therefore end the course

                if(logger.isInfoEnabled()){
                    logger.info("Ending the course because a authored branch path end course object was reached.");
                }

                //update current object to authored object being handled
                currentCourseObject = courseObjectWrapper;

                // all path's are ending now
                while (courseManager.hasMoreTransitions()) {
                    CourseObjectWrapper nextTransition = courseManager.getNextTransition();
                    if (nextTransition.getCourseObject() instanceof PathEnd) {
                        handlePathEnd((PathEnd)nextTransition.getCourseObject());
                    }
                }

                courseManager.setNextTransitionToEnd();
                transitionCallback.onSuccess();

            } else {

                // Getting here is pretty serious
                onSessionError("Unable to proceed in the course.", "Unknown transition type: " + courseObjectWrapper.getCourseObject().getClass().getCanonicalName());
            }
        } else {

            onSessionError("Unable to proceed in the course.", "Got a null transition");
        }
    }

    /**
     * Handles a PathEnd transition by sending a BranchPathHistoryUpdate message
     *
     * @param pathEnd indicates the end of an authored branch path.
     */
    private void handlePathEnd(PathEnd pathEnd) {
        BranchPathHistory branchPathHistoryUpdate = new BranchPathHistory(domainSessionInfo.getDomainSourceId(), domainSessionInfo.getExperimentId(),
                pathEnd.getAuthoredBranch().getBranchId().intValue(),
                pathEnd.getSelectedPath().getPathId().intValue(),
                pathEnd.getSelectedPath().getName(), true);
        DomainModule.getInstance().sendBranchPathHistoryUpdate(domainSessionInfo.getDomainSessionId(), branchPathHistoryUpdate, new MessageCollectionCallback() {

            @Override
            public void success() {
                displayNextTransition();
            }

            @Override
            public void received(Message msg) {
                // nothing to do

            }

            @Override
            public void failure(String why) {
                onSessionError("There has been a system error", why);
            }

            @Override
            public void failure(Message msg) {
                onSessionError("There has been a system error", msg.getPayload().toString());
            }
        });
    }

    /**
     * Sends the display team session request to the Tutor client.
     */
    private void sendDisplayTeamSession() {
        DomainModule.getInstance().sendDisplayTeamSession(getDomainSessionInfo().getDomainSessionId(), new MessageCollectionCallback() {

            @Override
            public void success() {
             // Nothing to do here

            }

            @Override
            public void received(Message msg) {
                // Nothing to do here
            }

            @Override
            public void failure(Message msg) {


                BaseDomainSession.generateException("Closing domain session due to display team session failure: msg=", msg);

            }

            @Override
            public void failure(String why) {
                BaseDomainSession.generateException("Closing domain session due to display team session failure: why=", why);

            }

        });

    }

    /**
     * Handle an authored branch course object by first determining the path the learner will take and then
     * adding that path's course objects to the next course objects to execute.
     *
     * @param authoredBranch the course object to select the path of course objects to execute next
     * @param callback used to indicate that the next course object should be started
     * @throws IOException if there was a problem retrieving a file reference using the authored course folder
     */
    private void handleAuthoredBranchTransition(final generated.course.AuthoredBranch authoredBranch, final AsyncActionCallback callback) throws IOException{

        if(logger.isInfoEnabled()){
            logger.info("Processing authored branch course object named '"+authoredBranch.getTransitionName()+"' to determine the appropriate path for the learner.");
        }

        //get condition
        //  - Simple Branch Dist.
        //  --random   : continue 'randomly'
        //  --balanced : get all paths db data
        //  - custom % : get all paths db data
        //  - Learner centric : TBD

        //the path to execute based on a condition
        /* CHANGE NOTE:
         * Explicitly pass an empty list of survey responses for now.
         * This was done in response to ticket #3325 which required the
         * elimination of the collection storing the surveyResponses in
         * order to reduce the memory consumption of the DomainModule. */
        generated.course.AuthoredBranch.Paths.Path selectedPath = PathConditionEvaluator.getNextPath(authoredBranch, domainSessionInfo, lmsUsername, new ArrayList<SurveyResponse>(), publishedScoreToEventIdMap, callback);

        List<generated.course.AuthoredBranch.Paths.Path> paths = authoredBranch.getPaths().getPath();

        //build map of paths
        Map<Integer, generated.course.AuthoredBranch.Paths.Path> pathMapById = new HashMap<>();
        for(generated.course.AuthoredBranch.Paths.Path path : paths){
            pathMapById.put(path.getPathId().intValue(), path);
        }

        final generated.course.AuthoredBranch.SimpleDistribution simpleDistr = authoredBranch.getSimpleDistribution();
        if(simpleDistr != null){
            //random or balanced distribution

            Serializable choice = simpleDistr.getRandomOrBalancedOrCustom();
            if(choice instanceof generated.course.AuthoredBranch.SimpleDistribution.Random){
                //randomly choose a path, random number doesn't need to depend on previous random numbers

                int pathIndex = new Random().nextInt(paths.size());

                selectedPath = paths.get(pathIndex);

                if(logger.isInfoEnabled()){
                    logger.info("Selected the path named '"+selectedPath.getName()+" (id "+selectedPath.getPathId()+") for authored branch named '"+authoredBranch.getTransitionName()+
                        " (id "+authoredBranch.getBranchId()+") based on a random distribution.");
                }
                pathSelectedEvent(selectedPath, authoredBranch, callback);

            }else{
                //determine which path should be chosen based on balanced OR custom distribution

                //get all path db data to make decision
                List<BranchPathHistory> branchHistoryPaths = new ArrayList<>(paths.size());
                for(generated.course.AuthoredBranch.Paths.Path path : paths){

                    BranchPathHistory branchHistoryPath = new BranchPathHistory(domainSessionInfo.getDomainSourceId(), domainSessionInfo.getExperimentId(),
                            authoredBranch.getBranchId().intValue(), path.getPathId().intValue());

                    branchHistoryPaths.add(branchHistoryPath);
                }

                DomainModule.getInstance().sendBranchPathHistoryInfoRequest(domainSessionInfo.getDomainSessionId(),
                        branchHistoryPaths, new MessageCollectionCallback() {

                            //branch history for this course from the UMS db
                            List<BranchPathHistory> dbBranchPathsHistory;

                            @Override
                            public void success() {

                                if(choice instanceof generated.course.AuthoredBranch.SimpleDistribution.Balanced){
                                    //balanced - choose path with the lowest count, otherwise choose first

                                    BranchPathHistory lowestBranchPathHistory = null;
                                    for(BranchPathHistory branchPathHistory : dbBranchPathsHistory){

                                        if(lowestBranchPathHistory == null || branchPathHistory.getCnt() < lowestBranchPathHistory.getCnt()){
                                            lowestBranchPathHistory = branchPathHistory;
                                        }
                                    }

                                    if(lowestBranchPathHistory == null){
                                        //ERROR

                                        callback.onFailure(
                                                new DetailedException("Unable to select the appropriate path for the learner to take based on balanced distribution.",
                                                        "After searching "+dbBranchPathsHistory.size()+" paths of the current authored branch course object, no path was choosen.", null));
                                        return;
                                    }

                                    //find the path by id
                                    generated.course.AuthoredBranch.Paths.Path selectedPath = pathMapById.get(lowestBranchPathHistory.getPathId());
                                    if(logger.isInfoEnabled()){
                                        logger.info("Selected the path named '"+selectedPath.getName()+" (id "+selectedPath.getPathId()+") for authored branch named '"+authoredBranch.getTransitionName()+
                                            " (id "+authoredBranch.getBranchId()+") based on a balanced distribution.  That branch has had "+lowestBranchPathHistory.getCnt()+" learners.");
                                    }

                                    try{
                                    pathSelectedEvent(selectedPath, authoredBranch, callback);
                                    }catch(IOException e){
                                        callback.onFailure(
                                                new DetailedException("Unable to select the appropriate path for the learner to take based on balanced distribution.",
                                                        "There was a problem selected the appropriate path", e));
                                        return;
                                    }

                                }else{
                                    //custom - choose path based on % for each path

                                    generated.course.AuthoredBranch.Paths.Path selectedPath = deteremineCustomPercentPath(dbBranchPathsHistory, pathMapById);
                                    if(logger.isInfoEnabled()){
                                        logger.info("Selected the path named '"+selectedPath.getName()+" (id "+selectedPath.getPathId()+") for authored branch named '"+authoredBranch.getTransitionName()+
                                            " (id "+authoredBranch.getBranchId()+") based on a custom distribution.");
                                    }

                                    try{
                                    pathSelectedEvent(selectedPath, authoredBranch, callback);
                                    }catch(IOException e){
                                        callback.onFailure(
                                                new DetailedException("Unable to select the appropriate path for the learner to take based on custom path percent.",
                                                        "There was a problem selected the appropriate path", e));
                                        return;
                                    }

                                }//end custom

                            }

                            @SuppressWarnings("unchecked")
                            @Override
                            public void received(Message msg) {
                                dbBranchPathsHistory = (List<BranchPathHistory>) msg.getPayload();
                            }

                            @Override
                            public void failure(String why) {

                                callback.onFailure(new DetailedException("Unable to select the appropriate path for the learner to take based on balanced distribution.",
                                        "There was a problem requesting the lasted branch history information from the UMS database.  The reason reads:\n"+why, null));
                            }

                            @Override
                            public void failure(Message msg) {

                                callback.onFailure(new DetailedException("Unable to select the appropriate path for the learner to take based on balanced distribution.",
                                        "There was a problem requesting the lasted branch history information from the UMS database.  The response message was:\n"+msg, null));
                            }
                        });

            }
        }else{

            //learner centric
            // - learner state -> goto ped? (TBD)
            // - knowledge assessment -> goto ped? (TBD)
            // - survey response -> look at courseEvents (DONE)
            // - dkf scoring -> look at publishedScoreToEventIdMap
            //
            // Note: if multiple path conditions are satisfied, choose the one with more conditions first than if still a tie choose default

            int defaultPathId = authoredBranch.getDefaultPathId().intValue();
            generated.course.AuthoredBranch.Paths.Path defaultPath = pathMapById.get(defaultPathId);

            // contains paths which have the most condition(s) satisfied.  This collection will contain
            // more than one entry when more than one path is satisfied and the number of conditions satisfied
            // is equal to the other conditions satisfied for the path(s) already in the collection.
            List<generated.course.AuthoredBranch.Paths.Path> equallySatisfiedPaths = new ArrayList<>();
            int highestNumOfCond = 0;

            for(generated.course.AuthoredBranch.Paths.Path path : paths){

                Serializable condition = path.getCondition().getCustomPercentOrLearnerCentric();
                if(condition instanceof generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric){

                    generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric learnerCondition = (generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric) condition;

                    boolean satisfiedAll = true;
                    List<Serializable> condTypes = learnerCondition.getLearnerCondTypes();
                    for(Serializable condType : condTypes){

                        if(condType instanceof generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric.SurveyResponse){
                            //check responses to a survey

//                            generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric.SurveyResponse surveyResponseCond =
//                                    (generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric.SurveyResponse)condType;

                            // TODO: fix the implementation of this method
//                            if(!checkSurveyResponsePathCondition(surveyResponseCond)){
//                                satisfiedAll = false;
//                                break;
//                            }

                        }else if(condType instanceof generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric.DkfScoring){
                            //check DKF scoring results

                            generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric.DkfScoring dkfScoringCond =
                                    (generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric.DkfScoring)condType;

                            if(!checkDkfScoringPathCondition(dkfScoringCond)){
                                satisfiedAll = false;
                                break;
                            }

                        }else{

                            logger.error("Found unhandled path condition type of '"+condition+"' in authored branch course object named '"+authoredBranch.getTransitionName()+"'.");

                            satisfiedAll = false;
                            break;
                        }

                    }//end for on conditions for this path

                    if(satisfiedAll){
                        //this path's conditions where satisfied

                        if(condTypes.size() > highestNumOfCond){
                            //new highest

                            highestNumOfCond = condTypes.size();
                            equallySatisfiedPaths.clear();
                            equallySatisfiedPaths.add(path);

                        }else if(condTypes.size() == highestNumOfCond){
                            //another path with the same number of conditions satisfied
                            equallySatisfiedPaths.add(path);
                        }else{
                            //although satisfied, the number of conditions is less than the current
                            //highest number of conditions satisfied
                            if(logger.isInfoEnabled()){
                                logger.info("Not considering the path named '"+path.getName()+"' because it has "+condTypes.size()+" conditions satisfied which is less than the current best of "+highestNumOfCond+" conditions satisfied.");
                            }
                        }
                    }

                }else{
                    //ERROR - only other element at this level is custom percent and
                    //        that should be handled way above in this method
                    callback.onFailure(
                            new DetailedException("Unable to select the appropriate path for the learner to take in the Authored branch named '"+authoredBranch.getTransitionName()+"'.",
                                    "Found an unexpected path condition type of '"+condition+"'.", null));
                }
            }//end for on paths

            if(equallySatisfiedPaths.isEmpty()){
                //select default path
                selectedPath = defaultPath;

            }else if(equallySatisfiedPaths.size() == 1){
                selectedPath = equallySatisfiedPaths.get(0);

            }else{
                //there is more than 1 satisfied path, all with the same number of satisfied conditions:
                // - choose default path
                selectedPath = defaultPath;
            }

            if(selectedPath != null){
                if(logger.isInfoEnabled()){
                    logger.info("Selected the path named '"+selectedPath.getName()+" (id "+selectedPath.getPathId()+") for authored branch named '"+authoredBranch.getTransitionName()+
                        " (id "+authoredBranch.getBranchId()+") based on other (null) distribution method.");
                }
                pathSelectedEvent(selectedPath, authoredBranch, callback);
            }else{
                callback.onFailure(new DetailedException("Failed to select the appropriate branch path to take.",
                        "No path was selected in the authored branch named '"+authoredBranch.getTransitionName()+"'.", null));
            }

        }

    }

    private boolean checkDkfScoringPathCondition(generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric.DkfScoring dkfScoringCond){

        for(PublishLessonScoreResponse publishedScore : publishedScoreToEventIdMap.keySet()){

            Map<LMSConnectionInfo, CourseRecordRef> publishedRecordsByLMS = publishedScore.getPublishedRecordsByLMS();
            for(LMSConnectionInfo lmsInfo : publishedRecordsByLMS.keySet()){

                publishedScore.getPublishedRecordsByLMS().get(lmsInfo);
                if(publishedScore.getPublishedRecordsByLMS().containsKey(lmsInfo)){

                }
            }
        }

        return false;

    }

    /**
     * Select the appropriate path based on custom percentage values defined for the current authored branch
     * course object.
     *
     * @param dbBranchPathsHistory information about the paths learners have taken in the past.  This is needed
     * to determine with path this learner should take.
     * @param pathMapById paths defined in the current authored branch course object
     * @return the path the learner should take next.  Will be null if there was a problem.
     */
    private generated.course.AuthoredBranch.Paths.Path deteremineCustomPercentPath(List<BranchPathHistory> dbBranchPathsHistory,
            Map<Integer, generated.course.AuthoredBranch.Paths.Path> pathMapById){

        //get total participants from all paths of this branch defined in the course
        int totalParticipants = 0;
        int[] cnts = new int[dbBranchPathsHistory.size()];
        double[] distributions = new double[dbBranchPathsHistory.size()];
        for(int index = 0; index < dbBranchPathsHistory.size(); index++){

            BranchPathHistory branchPathHistory = dbBranchPathsHistory.get(index);
            totalParticipants += branchPathHistory.getCnt();

            cnts[index] = branchPathHistory.getCnt();

            //find the path distribution
            generated.course.AuthoredBranch.Paths.Path path = pathMapById.get(branchPathHistory.getPathId());
            distributions[index] = ((BigDecimal)path.getCondition().getCustomPercentOrLearnerCentric()).doubleValue() / 100.0;
        }

        //determine which path should be chosen based on...
        // brute force (for now), i.e. trying to add one to each path count to find
        // the closest outcome to the desired percentages

        int bestIndex = 0;
        double leastTotalOff = Double.MAX_VALUE, totalOff;
        for(int i = 0; i < cnts.length; i++){

            totalOff = 0;

            for(int j = 0; j < cnts.length; j++){

                if(i == j){
                    //adding to this cnt

                    totalOff += Math.abs(distributions[j] - (cnts[j] + 1.0) / (totalParticipants + 1.0) );

                }else{

                    totalOff += Math.abs(distributions[j] - cnts[j] / (totalParticipants + 1.0) );
                }
            }

            if(totalOff < leastTotalOff){
                // better selection found

                bestIndex = i;
                leastTotalOff = totalOff;
            }
        }

        return pathMapById.get(dbBranchPathsHistory.get(bestIndex).getPathId());
    }

    /**
     * Check whether the path's condition provided from the current authored branch course object is satisfied based on the
     * current history of survey responses made by this learner in this course execution.
     *
     * @param surveyResponseCond the authored survey response condition to check for an authored branch course object's path.  Essentially
     * does the learner's answer to the survey question match the survey response the author is looking for in this path's condition.
     * @return true iff the condition is satisfied, meaning this path should be considered as the next path for the learner in the course.
     */
//    private boolean checkSurveyResponsePathCondition(generated.course.AuthoredBranch.Paths.Path.Condition.LearnerCentric.SurveyResponse surveyResponseCond){
//
//        int surveyId = surveyResponseCond.getSurvey().intValue();
//        boolean foundSurvey = false;
//
//        //start from the latest survey response as it is possible the same survey could be
//        //given multiple times in a course and the latest response should be applied to the path condition
//        ListIterator<SurveyResponse> itr = surveyResponses.listIterator(surveyResponses.size());
//        while(itr.hasPrevious()){
//
//            SurveyResponse surveyResponse = itr.previous();
//
//            if(surveyId == surveyResponse.getSurvey().getId()){
//                //found the survey, now find the question
//
//                foundSurvey = true;
//                int questionId = surveyResponseCond.getSurveyQuestion().intValue();
//
//                boolean foundQuestion = false;
//                for(SurveyPageResponse surveyPageResponse : surveyResponse.getSurveyPageResponses()){
//
//                    for(AbstractQuestionResponse questionResponse : surveyPageResponse.getQuestionResponses()){
//
//                        if(questionId == questionResponse.getSurveyQuestion().getId()){
//                            //found the question, now check the response
//
//                            foundQuestion = true;
//
//                            Serializable answerType = surveyResponseCond.getAnswer().getAnswerType();
//
//                            if(answerType instanceof Selection){
//                                //check all the path condition's answers against the learner's answers
//
//                                Selection selection = (Selection)answerType;
//
//                                //the path's condition number of answers must match the learner's number of answers
//                                if(selection.getChoice().size() != questionResponse.getResponses().size()){
//                                    break;
//                                }
//
//                                boolean match = false;
//                                for(BigInteger choiceId : selection.getChoice()){
//
//                                    match = false;
//
//                                    for(QuestionResponseElement qResponse :  questionResponse.getResponses()){
//
//                                        for(ListOption listOption : qResponse.getTextOptionList().getListOptions()){
//
//                                            if(qResponse.getText().equals(listOption.getText())){
//                                                //found matching choice text
//
//                                                if(listOption.getId() == choiceId.intValue()){
//                                                    //match
//                                                    match = true;
//                                                    break;
//                                                }
//                                            }
//                                        }
//
//                                        if(match){
//                                            break;
//                                        }
//                                    }
//
//                                    if(!match){
//                                        //learner didn't select this choice as an answer, therefore condition failed
//                                        break;
//                                    }
//                                }
//
//                                //if loop ended with match = true than all choices were matched
//                                if(match){
//                                    return true;
//                                }
//
//                            }else{
//                                //a simple value
//
//                                //only allowed 1 simple string value to compare in this condition so
//                                //the question answer can only have 1 value as well
//                                if(questionResponse.getResponses().size() != 1){
//                                    break;
//                                }
//
//                                for(QuestionResponseElement qResponse : questionResponse.getResponses()){
//
//                                    if(answerType.equals(qResponse.getText())){
//                                        //satisfied condition
//                                        return true;
//                                    }
//                                }
//
//                            }
//
//                            break;
//                        }
//                    }
//
//                    if(foundQuestion){
//                        break;
//                    }
//                }
//
//                break;
//            }
//
//            if(foundSurvey){
//                break;
//            }
//        }//end while
//
//        return false;
//    }

    /**
     * Insert the authored branch's path course objects as the next course objects for the learner
     * to execute.
     *
     * @param selectedPath the path of a authored branch course object to extract course objects from
     * @param authoredBranch the authored branch course object the path is apart of
     * @param callback used to start the next course object, the first course object in the provided path.
     * @throws IOException if there was a problem retrieving a file reference using the authored course folder
     */
    private void pathSelectedEvent(generated.course.AuthoredBranch.Paths.Path selectedPath,
            final generated.course.AuthoredBranch authoredBranch, final AsyncActionCallback callback) throws IOException{

        // insert course objects at current location in collection
        List<Serializable> transitions = selectedPath.getCourseobjects().getAAROrAuthoredBranchOrEnd();
        // Add a path end object to indicate when the path has finished
        transitions.add(new PathEnd(authoredBranch, selectedPath));
        courseManager.insertTransitions(CourseObjectWrapper.generateCourseObjectWrappers(transitions, courseManager.getCourseAuthoredDirectory()));

        // send path selection info to UMS
        BranchPathHistory newEntry = new BranchPathHistory(domainSessionInfo.getDomainSourceId(), domainSessionInfo.getExperimentId(),
                authoredBranch.getBranchId().intValue(), selectedPath.getPathId().intValue(), selectedPath.getName(), false);
        newEntry.setIncrement(true);

        DomainModule.getInstance().sendBranchPathHistoryUpdate(domainSessionInfo.getDomainSessionId(), newEntry, new MessageCollectionCallback() {

            @Override
            public void success() {

                logger.info("Successfully updated the branch path history in the database for authored branch named '"+authoredBranch.getTransitionName()+
                        "' (id "+authoredBranch.getBranchId()+") path named '"+selectedPath.getName()+"' (id "+selectedPath.getPathId()+").");
                callback.onSuccess();
            }

            @Override
            public void received(Message msg) {
                // nothing to do
            }

            @Override
            public void failure(String why) {

                callback.onFailure(new DetailedException("The appropriate branch path was selected however GIFT was unable to record that decision in the database.",
                        "There was a problem when trying to update the UMS database of the path selection.  The error reason reads:\n"+why, null));
            }

            @Override
            public void failure(Message msg) {

                callback.onFailure(new DetailedException("The appropriate branch path was selected however GIFT was unable to record that decision in the database.",
                        "There was a problem when trying to update the UMS database of the path selection.  The response message reads:\n"+msg, null));            }
        });
    }

    /**
     * Build a new {@link RequiredLearnerStateAttributes} object by extracting the course concepts covered in the
     * adaptive courseflow provided.
     * @param adaptiveCourseFlowCourseObject the adaptive courseflow course object to extract course concepts from
     * @return a new {@link RequiredLearnerStateAttributes} that contains LearnerStateAttributeNameEnum.KNOWLEDGE attribute
     * value of Expert for each course concepts in the adaptive courseflow provided and  LearnerStateAttributeNameEnum.SKILL attribute
     * value of Expert for each course concept that is practiced, if any, in the adaptive courseflow provided.
     */
    private RequiredLearnerStateAttributes getRequiredLearnerStates(generated.course.MerrillsBranchPoint adaptiveCourseFlowCourseObject){
        
        Map<LearnerStateAttributeNameEnum, AttributeValues> learnerStateAttributesMap = new HashMap<>();
        if(adaptiveCourseFlowCourseObject != null){
            
            boolean hasPractice = false;
            for(Serializable element : adaptiveCourseFlowCourseObject.getQuadrants().getContent()){
               
                if(element instanceof generated.course.Practice){
                    hasPractice = true;
                    break;
                }                
            }
        
            Map<String, ExpertiseLevelEnum> knowledgeConceptExpertiseLevel = new HashMap<>();
            Map<String, ExpertiseLevelEnum> skillConceptExpertiseLevel = new HashMap<>();
            for(String concept : adaptiveCourseFlowCourseObject.getConcepts().getConcept()){
                
                knowledgeConceptExpertiseLevel.put(concept, ExpertiseLevelEnum.EXPERT);
                skillConceptExpertiseLevel.put(concept, ExpertiseLevelEnum.EXPERT);
            }
            
            AttributeValues knowledgeAttr = new ConceptAttributeValues(knowledgeConceptExpertiseLevel);
            learnerStateAttributesMap.put(LearnerStateAttributeNameEnum.KNOWLEDGE, knowledgeAttr);
            if(hasPractice){
                AttributeValues skillAttr = new ConceptAttributeValues(skillConceptExpertiseLevel);
                learnerStateAttributesMap.put(LearnerStateAttributeNameEnum.SKILL, skillAttr);
            }
                
        }
        
        return new RequiredLearnerStateAttributes(learnerStateAttributesMap);
    }

    /**
     * Handle the branch point transition by using its information to expand on the current
     * list of course transitions.
     *
     * @param adaptiveCourseFlowWrapper - the branch point transition information
     * @param callback - callback for when the transition has been handled
     * @throws IOException if there was a problem retrieving a file reference using the authored course folder
     */
    private void handleMerrilsBranchPointTransition(final CourseObjectWrapper adaptiveCourseFlowWrapper, final AsyncActionCallback callback) throws IOException {

        final generated.course.MerrillsBranchPoint adaptiveCourseFlowCourseObject = (generated.course.MerrillsBranchPoint)adaptiveCourseFlowWrapper.getCourseObject();
        //send next quadrant to Ped module and wait for branch adaptation request message response
        final MerrillsBranchPointHandler handler = courseManager.getMerrillsBranchPointManager().getHandler(adaptiveCourseFlowCourseObject);
        courseManager.getMerrillsBranchPointManager().setCurrentHandler(handler);
        
        if(handler.willKnowledgeBeSkipped() && !handler.isInitialized()){
            // the adaptive courseflow can be skipped because the learner has the appropriate knowledge AND
            // the adaptive courseflow handler has not been initialized yet, THEREFORE
            // ask the learner what they want to do, take it for remediation or skip it.
            
            // display survey to learner
            StringBuilder sb = new StringBuilder("learn about <ul>");
            for(String concept : adaptiveCourseFlowCourseObject.getConcepts().getConcept()){
                sb.append("<li>").append(concept).append("</li>");
            }
            sb.append("</ul>");
            final String customSkipChoice = "Skip and move on in the course";
            Survey userPrompt = createSurveyToSkipCourseObject(sb.toString(), null, "What would you like to do?", "Continue for remediation", customSkipChoice);
            
            //The callback to be used after the user specified whether they want to take the adaptive courseflow or not
            final MessageCollectionCallback handleUserPromptResponse = new MessageCollectionCallback() {

                SurveyResponse response = null;

                @Override
                public void success() {
                    if(response == null) {
                        callback.onFailure(new DetailedException(
                                "An unexpected response type was received",
                                "A response type of 'SurveyResponse' was expected but was never received",
                                null));
                    }

                    // Outcomes:
                    // 1. User indicates that they wanted to skip the adaptive courseflow, expand all phases of the adaptive courseflow
                    // but allow the knowledge phases to be ignored.
                    // skip the appropriate parts of the adaptive courseflow.
                    // 2. User indicates that they wanted to retake the adaptive courseflow, expand all phases of the adaptive courseflow 
                    // to add new course objects.
                    try{
                        if(didUserSelectSkipOption(response, customSkipChoice)) {
                            
                            // has knowledge and wants to skip knowledge phases
                            handler.initialize(true);
                            handleAdaptiveCourseflowSkipResult(adaptiveCourseFlowWrapper, handler, false, callback);
                        }else{                            
                            
                            // has knowledge and wants to take knowledge phases
                            handler.initialize(false);
                            handleAdaptiveCourseflowSkipResult(adaptiveCourseFlowWrapper, handler, true, callback);
                        }

                    }catch(Exception e){
                        logger.error("Caught exception while trying to handle the next part of the Adaptive Courseflow course object named '"+adaptiveCourseFlowCourseObject.getTransitionName()+"' for "+domainSessionInfo.getUsername()+".", e);
                        callback.onFailure(generateException("Handling an Adaptive Courseflow course object", "The Domain module failed to handle the next part of the Adaptive Courseflow course object named '"+adaptiveCourseFlowCourseObject.getTransitionName()+"'."));
                    }                        

                }

                @Override
                public void received(Message msg) {
                    if(msg.getMessageType() == MessageTypeEnum.DISPLAY_SURVEY_TUTOR_REPLY) {
                        response = (SurveyResponse) msg.getPayload();
                    }
                }

                @Override
                public void failure(Message msg) {
                    callback.onFailure(new DetailedException(
                            "There was a failure while asking the user if they wished to skip an adaptive courseflow",
                            "The following failure message was reported while asking the user if they wanted to skip an adaptive courseflow: " + msg,
                            null));
                }

                @Override
                public void failure(String why) {
                    callback.onFailure(new DetailedException(
                            "There was a failure while asking the user if they wished to skip an adaptive courseflow",
                            "The following failure reason was reported while asking the user if they wanted to skip an adaptive courseflow: " + why,
                            null));
                }
            };
            sendDisplaySurveyRequest(userPrompt, true, handleUserPromptResponse, false);
           
        }else{
            
            if(!handler.isInitialized()){
                // learner lacks knowledge and can't skip knowledge phases
                handler.initialize(false);
            }
            handleAdaptiveCourseflowSkipResult(adaptiveCourseFlowWrapper, handler, handler.knowledgeAdvancementIgnored(), callback);
        }

    }
    
    /**
     * Called after the adaptive courseflow handler is initialized and the appropriate course objects added based on prior learner
     * state information on knowledge and skill over the course concepts being covered in this adaptive courseflow.  Used to notify
     * the other modules of upcoming course state (e.g. Rule phase), then handles any pedagogical response for adaptation.
     * @param adaptiveCourseFlowWrapper - the branch point transition information
     * @param handler - the adaptive courseflow handler instance responsible for tracking progress in that course object.
     * @param ignoreAdvancement - whether to not allow skipping any part of the adaptive courseflow that could normally be skipped when
     * the learner has the expert knowledge on the course concepts.  True is usually an indication that the learner has selected to
     * take the adaptive courseflow for remediation because they are allowed to skip it based on their current known learner state.
     * @param callback - callback for when the transition has been handled
     */
    private void handleAdaptiveCourseflowSkipResult(final CourseObjectWrapper adaptiveCourseFlowWrapper,
            final MerrillsBranchPointHandler handler, boolean ignoreAdvancement, final AsyncActionCallback callback){
        
        final generated.course.MerrillsBranchPoint adaptiveCourseFlowCourseObject = (generated.course.MerrillsBranchPoint)adaptiveCourseFlowWrapper.getCourseObject();
        
        final MerrillQuadrantEnum quadrant;
        try{
            quadrant = handler.getNextQuadrant();
        }catch(@SuppressWarnings("unused") CourseComprehensionException comprehensionException){
            //force the course to end because the learner failed to comprehend the concepts
            //in the branch point
            terminateSession(false, MerrillsBranchPointHandler.BAILOUT_DETAILS_MSG, null, MAX_ATTEMPTS_MSG, LessonCompletedStatusType.INSTRUCTIONAL_STRATEGY_REQUEST);
            return;
        }

        final CourseState state = new CourseState(generated.course.MerrillsBranchPoint.class.getCanonicalName());
        state.setNextQuadrant(quadrant);
        expandableCourseObjectState = ExpandableCourseObjectStateEnum.ADAPTIVE_COURSEFLOW;
        state.setExpandableCourseObjectState(expandableCourseObjectState);

        if(ignoreAdvancement){
            // the learner has indicated they want to take the knowledge part of this adaptive courseflow that would have
            // been skipped based on their current learner state.  Let the pedagogical module know by sending a ZERO shelf life value.
            state.getLearnerStateShelfLife().put(LearnerStateAttributeNameEnum.KNOWLEDGE, BigInteger.ZERO.longValue());

        }else if(adaptiveCourseFlowCourseObject.getMandatoryOption() != null){
            // handle mandatory option - #4961 : currently not fully implemented

            Map<LearnerStateAttributeNameEnum, AttributeValues> learnerStateAttributesMapCandidate = new HashMap<LearnerStateAttributeNameEnum, AttributeValues>();

            /* If the author has specified that the adaptive courseflow should always be taken, show the adaptive courseflow.
             * Otherwise if a different behavior was specified or if no behavior was specified (null), check if it is possible to skip the adaptive courseflow
             */
            BigInteger shelfLifeParam = null;
            Serializable mandatoryBehavior = adaptiveCourseFlowCourseObject.getMandatoryOption().getMandatoryBehavior();
            if (mandatoryBehavior instanceof SimpleMandatoryBehavior
                    && !((SimpleMandatoryBehavior) mandatoryBehavior).isUseExistingLearnerStateIfAvailable()) {
                // show the adaptive courseflow, i.e. no skipping, no pedagogical decision needed, any pedagogical advancement request will be ignored
                shelfLifeParam = BigInteger.ZERO;
            } else if(mandatoryBehavior instanceof FixedDecayMandatoryBehavior) {
                // send the behavior properties to the pedagogical module for decisions making
                //Set the shelf life of the elements since the author indicated a shelf life
                FixedDecayMandatoryBehavior fixedDecayBehavior = (FixedDecayMandatoryBehavior) mandatoryBehavior;
                shelfLifeParam = fixedDecayBehavior.getLearnerStateShelfLife();
            }
            
            if(shelfLifeParam != null){
                //populate the learner state attributes that the adaptive courseflow covers
                final RequiredLearnerStateAttributes requiredLearnerStateAttributes = 
                         getRequiredLearnerStates(adaptiveCourseFlowCourseObject);
                
                state.setRequiredLearnerStateAttributes(requiredLearnerStateAttributes);
                learnerStateAttributesMapCandidate = 
                        requiredLearnerStateAttributes.getLearnerStateAttributesMap();
                
                for(LearnerStateAttributeNameEnum attrName : learnerStateAttributesMapCandidate.keySet()) {
                    state.getLearnerStateShelfLife().put(attrName, shelfLifeParam.longValue());
                }
            }
        }
        
        //listener for the Ped request message as a response to the course state message
        MessageCollectionCallback stateUpdateCallback =
                new MessageCollectionCallback() {

                    PedagogicalRequest request = null;

                    @Override
                    public void success() {

                        if(logger.isInfoEnabled()){
                            logger.info("Displaying the transition for the next quadrant of type "+quadrant+".");
                        }

                        try{
                        handleAdaptiveCourseflowPedRequest(request, adaptiveCourseFlowWrapper, handler, quadrant, callback);

                    }catch(@SuppressWarnings("unused") CourseComprehensionException comprehensionException){
                        //force the course to end because the learner failed to comprehend the concepts
                        //in the branch point
                        terminateSession(false, MerrillsBranchPointHandler.BAILOUT_DETAILS_MSG, null, MAX_ATTEMPTS_MSG, LessonCompletedStatusType.INSTRUCTIONAL_STRATEGY_REQUEST);
                        return;

                    }catch(Exception e){
                        logger.error("Caught exception while trying to handle the next part of the Adaptive Courseflow course object named '"+adaptiveCourseFlowCourseObject.getTransitionName()+"' for "+domainSessionInfo.getUsername()+".", e);
                        callback.onFailure(generateException("Handling an Adaptive Courseflow course object", "The Domain module failed to handle the next part of the Adaptive Courseflow course object named '"+adaptiveCourseFlowCourseObject.getTransitionName()+"'."));
                    }
                }

                @Override
                public void received(Message msg) {
                    if(msg.getPayload() instanceof PedagogicalRequest){
                        request = (PedagogicalRequest) msg.getPayload();

                        if(logger.isInfoEnabled()){
                            logger.info("Received ped request message of "+request+" for course state update of "+state);
                        }

                        handlePedagogicalRequest(request);

                    }else if(msg.getMessageType() == MessageTypeEnum.PROCESSED_ACK){
                        //nothing to do
                    }else{
                        callback.onFailure(generateException("Waiting for Branch Adaptation request", "Failed to receive a Pedagogical Request"));
                    }

                }

                @Override
                public void failure(Message msg) {

                    callback.onFailure(generateException("Waiting for Branch Adaptation request", msg));
                }

                @Override
                public void failure(String why) {

                    callback.onFailure(generateException("Waiting for Branch Adaptation request", why));
                }
            };


        sendCourseState(state, stateUpdateCallback);
    }
    
    /**
     * Used to handle the incoming pedagogical request during an adaptive courseflow.
     * @param request an optional pedagogical request that provides useful information for an adaptive courseflow handler (e.g. branch adaptations). Can be null.
     * @param adaptiveCourseFlowWrapper - the branch point transition information
     * @param handler - the adaptive courseflow handler instance responsible for tracking progress in that course object.
     * @param quadrant the new adaptive courseflow phase that will execute if no pedagogical request causes a change in behavior.
     * @param callback - callback for when the transition has been handled
     * @throws Exception if there was a problem handling the pedagogical request to build new course elements
     */
    private void handleAdaptiveCourseflowPedRequest(final PedagogicalRequest request, final CourseObjectWrapper adaptiveCourseFlowWrapper, 
            final MerrillsBranchPointHandler handler, final MerrillQuadrantEnum quadrant, 
            final AsyncActionCallback callback) throws Exception{
        
                            if(request != null) {
            // provide the pedagogical request to the adaptive courseflow handler 
            giveAdaptiveCourseHandlerPedRequest(request, adaptiveCourseFlowWrapper, handler, quadrant);

        }else{
            expandableCourseObjectState = ExpandableCourseObjectStateEnum.NONE; // adaptive course flow is exiting
        }

        //course manager has expanded the list of transitions
        callback.onSuccess();
    }
    
    /**
     * Provide the adaptive courseflow handler the pedagogical request which may result in new course objects needing to be
     * added as the next course objects in the course flow.
     * @param request pedagogical request that provides useful information for an adaptive courseflow handler (e.g. branch adaptations). Can't be null.
     * @param adaptiveCourseFlowWrapper - the branch point transition information
     * @param handler - the adaptive courseflow handler instance responsible for tracking progress in that course object.
     * @param quadrant the new adaptive courseflow phase that will execute if no pedagogical request causes a change in behavior.
     * @throws Exception if there was a problem handling the pedagogical request to build new course elements
     */
    private void giveAdaptiveCourseHandlerPedRequest(final PedagogicalRequest request, final CourseObjectWrapper adaptiveCourseFlowWrapper,
            final MerrillsBranchPointHandler handler, final MerrillQuadrantEnum quadrant) throws Exception{
        
                                //
                                // Things to consider:
                                // 1) the MBP could be completed but a request could come in with advancement.
                                //    In this case choices are:
                                //         i. can the request not be sent by the ped? (Not an option right now)
                                //        ii. the MBP signal that the request isn't applicable and to continue w/o
                                //            re-adding the MBP original transition [Return null]
                                //  2) the handler failed to apply the request when it should apply the request.
                                //    In this case an exception is currently being thrown and callback.failure is called [throw Exception]
                                //  3) the learner failed to proceed
                                //    In this case the course the terminateSession method should be called [throw CourseComprehensionException]
                                //  4) (normal) zero or more transitions are added as part of the handling of the request [Return size >= 0]

                                //using the ped request, gather the next transition(s) to execute in this course
                                List<CourseObjectWrapper> expandedTransitions = courseManager.getMerrillsBranchPointManager().buildTransitions(handler, request, pendingReviewCourseEvents);

                                if(quadrant != MerrillQuadrantEnum.REMEDIATION_AFTER_RECALL && (expandedTransitions == null || expandedTransitions.isEmpty())){
                                    // adaptive courseflow course object is over, clear any collected information that could be used
                                    // for an automatically added AAR during this adaptive courseflow so that these attributes don't influence
                                    // an upcoming adaptive courseflow in this course.
                                    // Note: when the quadrant is Remediation_After_Recall and there are no expandedTransitions, this is an indication
                                    //       that the recall has been passed.  With the new requirements from #4070 an AAR will be added when the next
                                    //       pedagogical request comes in that results in a course of action of progression in MerrillsBranchPointHandler which
                                    //       will then add an AAR course object.  Don't want the following attributes to be cleared in this case because the
                                    //       values will be needed for that AAR later.

                                    resetAdativeCourseflowAAR();
                                }

                                if(expandedTransitions != null){

                                    // when no additional course objects are inserted it means the adaptive courseflow is exiting
                                    expandableCourseObjectState = expandedTransitions.isEmpty() ? ExpandableCourseObjectStateEnum.NONE : ExpandableCourseObjectStateEnum.ADAPTIVE_COURSEFLOW;

                                    //make sure the branch point transitions are associated with the handler that created them
                                    for(CourseObjectWrapper courseObjectWrapper : expandedTransitions){

                                        if(courseObjectWrapper.getCourseObject() instanceof generated.course.AAR){
                                            pendingAdaptiveCourseflowAAR = true;
                                        }

                                        courseManager.getMerrillsBranchPointManager().registerHandler(courseObjectWrapper.getCourseObject(), handler);
                                    }

                                    //there maybe still transitions to explore (either still left to execute in the branch point or
                                    //as part of remediation for this branch point),
                                    //add the merrill quadrant course transition object so its analyzed again in the future in order
                                    //to expand/insert additional transitions as needed/requested (e.g. continue to next quadrant, remediation)
                                    expandedTransitions.add(adaptiveCourseFlowWrapper);

                                    //inject the next transitions after the current transitions in the course flow
                                    courseManager.insertTransitions(expandedTransitions);
                                }else{
                                    expandableCourseObjectState = ExpandableCourseObjectStateEnum.NONE;  // adaptive course flow is exiting
                                }
                            }

    /**
     * Displays the Lesson Material to the learner for an lesson material
     * transition.
     *
     * @param lessonMaterialCourseObjectWrapper The Lesson Material course object
     * @param callback Callback for when the action is done or fails
     */
    private void displayLessonMaterialTransition(CourseObjectWrapper lessonMaterialCourseObjectWrapper, final AsyncActionCallback callback) {

        generated.course.LessonMaterial lessonMaterialCourseObject = (generated.course.LessonMaterial) lessonMaterialCourseObjectWrapper.getCourseObject();
        currentLessonMaterialHandler = new LessonMaterialHandler(lessonMaterialCourseObject, courseManager.getCourseRuntimeDirectory(),
                runtimeCourseFolderRelativePath);

        final LessonMaterialList lessonMaterialList = currentLessonMaterialHandler.getLessonMaterial();

        MessageCollectionCallback displayLessonMaterialCollection = new MessageCollectionCallback() {

                    @Override
                    public void success() {

                        if(logger.isInfoEnabled()) {
                        logger.info("Recieved message for Display Lesson Message Request. Displaying next transition.");
                        }

                        currentLessonMaterialHandler = null;
                        callback.onSuccess();
                    }

                    @Override
                    public void received(Message msg) {

                        String feedbackMsg = null;

                        if(msg.getMessageType() == MessageTypeEnum.UNDER_DWELL_VIOLATION){
                            //the under dwell assessment was violated

                            if(lessonMaterialList.getAssessment() != null &&
                                    lessonMaterialList.getAssessment().getUnderDwell() != null &&
                                    lessonMaterialList.getAssessment().getUnderDwell().getFeedback() != null){

                                feedbackMsg = lessonMaterialList.getAssessment().getUnderDwell().getFeedback();
                    }

                        }else if(msg.getMessageType() == MessageTypeEnum.OVER_DWELL_VIOLATION){
                            //the over dwell assessment was violated

                            if(lessonMaterialList.getAssessment() != null &&
                                    lessonMaterialList.getAssessment().getOverDwell() != null &&
                                    lessonMaterialList.getAssessment().getOverDwell().getFeedback() != null){

                                feedbackMsg = lessonMaterialList.getAssessment().getOverDwell().getFeedback();
                            }
                        }

                        if(feedbackMsg != null){

                            // the feedback presented in a guidance course object
                            generated.course.Guidance guidance = new generated.course.Guidance();
                            guidance.setTransitionName("Content Feedback");
                            guidance.setFullScreen(BooleanEnum.TRUE);
                            generated.course.Guidance.Message message = new generated.course.Guidance.Message();
                            message.setContent(feedbackMsg);
                            guidance.setGuidanceChoice(message);

                            CourseObjectWrapper wrapper;
                            try {
                                wrapper = CourseObjectWrapper.generateCourseObjectWrapper(guidance, courseManager.getCourseAuthoredDirectory());
                                courseManager.insertTransition(wrapper);
                            } catch (IOException e) {
                                logger.warn("Problem generating a course object wrapper for '"+guidance.getTransitionName()+"' generated course object.", e);
                            }

                            if(logger.isInfoEnabled()){
                                logger.info("The learner violated the '"+lessonMaterialCourseObject.getTransitionName()+"' lesson material "+msg.getMessageType().getDisplayName()+" assessment rules therefore the authored feedback followed by a helpfulness survey will be presented next.");
                            }

                        }
                    }

                    @Override
                    public void failure(Message msg) {

                        if(LOCAL_SESSION_STATE.CLOSED == localSessionState){
                            //the failure is part of closing the domain session
                            return;
                        }

                        callback.onFailure(generateException("Displaying lesson material", msg));
                    }

                    @Override
                    public void failure(String why) {

                        if(LOCAL_SESSION_STATE.CLOSED == localSessionState){
                            //the failure is part of closing the domain session
                            return;
                        }

                        callback.onFailure(generateException("Displaying lesson material", why));
                    }
                };

            lessonMaterialCourseObject.setLessonMaterialList(lessonMaterialList);
            DomainModule.getInstance().sendDisplayMediaCollectionRequest(
                    getDomainSessionInfo().getDomainSessionId(),
                    lessonMaterialCourseObject,
                    lessonMaterialCourseObjectWrapper.getCourseObjectReference().getContentReference(),
                    displayLessonMaterialCollection);
    }

    /**
     * Initializes the domain knowledge manager
     *
     * @param trainingAppTransition The training application transition
     * @param trainingAppHandler The training application handler
     * @throws IOException if there was a problem parsing the course or any
     *         related course files
     * @throws DKFValidationException if there was a problem with the DKF
     *         associated with this training app transition
     * @throws FileValidationException if there was a problem validating a DKF
     *         against the schema
     * @throws ConfigurationException
     */
    private void initializeDomainKnowledgeManager(final generated.course.TrainingApplication trainingAppTransition,
            TrainingApplicationHandler trainingAppHandler) throws IOException, FileValidationException, DKFValidationException, ConfigurationException {
        //
        // initialize domain knowledge
        //
        FileProxy dkf = courseManager.getCourseRuntimeDirectory().getRelativeFile(trainingAppHandler.getDKFName());
        domainKnowledgeManager = new DomainKnowledgeManager(dkf, courseManager.getCourseRuntimeDirectory(), courseManager.getSessionOutputDirectory(),
                trainingAppTransition.getOptions(), this, this, assessmentProxy, domainSessionInfo);
    
        /* Copy the knowledge session's DKF to its log folder. 
         * This is used to expose scenario behavior to AAR tools. */
        String dsOutputFolder = PackageUtil.getDomainSessions() + File.separator + domainSessionInfo.buildLogFileName();
        Files.copy(dkf.getInputStream(), Paths.get(dsOutputFolder + File.separator + dkf.getName()), StandardCopyOption.REPLACE_EXISTING);
    }


    /**
     * Begins the scenario in the training application for a training
     * application transition.  Can be called automatically for a single player training app course object
     * or after the host of a team session starts the session (regardless if all playable members are assigned to learners)
     *
     * @param trainingAppCourseObjectWrapper The training application transition
     * @param trainingAppHandler The training application handler
     * @param callback Callback for handling async actions
     * @throws IOException if there was a problem parsing the course or any related course files
     * @throws DKFValidationException if there was a problem with the DKF associated with this training app transition
     * @throws FileValidationException if there was a problem validating a DKF against the schema
     * @throws ConfigurationException
     */
    private void displayTrainingApplicationTransition(final CourseObjectWrapper trainingAppCourseObjectWrapper,
            TrainingApplicationHandler trainingAppHandler, final AsyncActionCallback callback)
                    throws IOException, FileValidationException, DKFValidationException, ConfigurationException {

        final generated.course.TrainingApplication trainingAppTransition = (generated.course.TrainingApplication)trainingAppCourseObjectWrapper.getCourseObject();

        // Register individual knowledge sessions since there will be no host/join page shown to the learner
        Scenario scenario = domainKnowledgeManager.getAssessmentKnowledge().getScenario();

        // Auto create (i.e. don't show the GIFT lobby) an Individual Session for any of the following cases:
        // 1. DKF being used only supports 1 playable team member and not in RTA lesson level - in RTA lesson level
        //    the external controller of GIFT (e.g. RTA Test Harness) needs to send the team member assignment for single or multi-player DKFs.
        // 2. running a playback (not an active training application) and the playback log contains an individual session
        boolean singlePlayerDKF = !scenario.supportsMultipleLearners();
        boolean singlePlayerDKFInCourseMode = singlePlayerDKF && !DomainModuleProperties.getInstance().isRTALessonLevel();
        boolean playback = isInPlaybackMode();
        boolean singlePlayerPlayback = playback && !trainingAppHandler.isTeamSessionPlayback();
        if (singlePlayerDKFInCourseMode
                || singlePlayerPlayback) {
            
            final String transitionName = trainingAppTransition.getTransitionName();
            final TrainingApplicationEnum trainingAppType = TrainingAppUtil.getTrainingAppType(trainingAppTransition);
            final String courseName = courseManager.getCourseName();
            final String experimentId = domainSessionInfo.getExperimentId();

            if (StringUtils.isNotBlank(experimentId)) {
                DomainModule.getInstance().sendGetExperimentRequest(new GetExperimentRequest(experimentId),
                        new MessageCollectionCallback() {
                            private DataCollectionItem experiment;

                            @Override
                            public void success() {
                                if (experiment != null) {
                                    KnowledgeSessionManager.getInstance().addIndividualKnowledgeSession(
                                            domainSessionInfo, courseName, transitionName, experiment.getName(),
                                            scenario.getDescription(), scenario.getRootTeam(), scenario.getPerformanceNodes(), trainingAppType,
                                            scenario.getMission(), getObserverControls());
                                } else {
                                    callback.onFailure(generateException(
                                            "Retrieving experiment with id " + experimentId,
                                            "Reached successful state, but never received the experiment with id '"
                                                    + experimentId + "'."));
                                }
                            }

                            @Override
                            public void received(Message msg) {
                                experiment = (DataCollectionItem) msg.getPayload();
                            }

                            @Override
                            public void failure(String why) {
                                callback.onFailure(
                                        generateException("Retrieving experiment with id " + experimentId, why));
                            }

                            @Override
                            public void failure(Message msg) {
                                callback.onFailure(
                                        generateException("Retrieving experiment with id " + experimentId, msg));
                            }
                        });
            } else {
                KnowledgeSessionManager.getInstance().addIndividualKnowledgeSession(domainSessionInfo, courseName,
                        transitionName, null, scenario.getDescription(), scenario.getRootTeam(), scenario.getPerformanceNodes(), trainingAppType,
                        scenario.getMission(), getObserverControls());
            }
        }

        //provide the conversation variable handler to the domain hierarchy
        //- at some point this handler could be placed in a common domain data model class that can be accessed by the domain hierarchy
        domainKnowledgeManager.getAssessmentKnowledge().getScenario().setConversationVarsHandler(courseManager.getConversationManager().getConversationVarHandler());


        final String dkfCustomDefinedCharacter = domainKnowledgeManager.getCustomDefinedCharacter();
        final String courseObjectCustomDefinedCharacter = DomainCourseFileHandler.getCustomCharacterPath(trainingAppTransition);

        // need to process the paths just in case one has \ and the other has /
        if(dkfCustomDefinedCharacter != null && courseObjectCustomDefinedCharacter != null && !UriUtil.makeURICompliant(courseObjectCustomDefinedCharacter).equals(UriUtil.makeURICompliant(dkfCustomDefinedCharacter))){
            //error - GIFT doesn't support more than 1 character embedded in the tui during a training application (see the explanation
            //        on the alwayShowCharacterInitiallyCallback callback below
            onSessionError("Unable to start the course object "+trainingAppTransition.getTransitionName()+" because more than one character was found.",
                    "GIFT currently doesn't support more than one character per real-time assessment.  A course object character was defined as '"+courseObjectCustomDefinedCharacter+
                    "' and real-time assessment (DKF) character as '"+dkfCustomDefinedCharacter+"'.");
        }

        /* Determines whether the training application course object that is
         * being processed right now is an embedded training application or if
         * it is running through the gateway module by examining what types of
         * elements are present in the course.xml file */
        Map<String, Serializable> inputs = null;
        if (trainingAppTransition.getInterops() != null) {
            generated.course.Interops interops = trainingAppTransition.getInterops();
            isCurrentAppEmbedded = false;
            inputs = trainingAppHandler.processInterops(interops);

        } else if (trainingAppTransition.getEmbeddedApps() != null) {
            generated.course.EmbeddedApps embeddedApps = trainingAppTransition.getEmbeddedApps();
            isCurrentAppEmbedded = true;
            inputs = trainingAppHandler.processEmbeddedApps(embeddedApps, runtimeCourseFolderRelativePath);
        }

        //reset (either from null or Stopped)
        changeTrainingAppState(null);

        // create course state to send
        final CourseState taEndCourseState = new CourseState(generated.course.TrainingApplication.class.getCanonicalName());

        // store the course concepts referenced in the assessed scenario (dkf), before the domain knowledge manager is nulled at the end of the scenario.
        final Set<String> practiceCourseConcepts = new HashSet<>();
        CourseConceptSearchFilter searchFilter = new CourseConceptSearchFilter(courseManager.getCourseConceptsFlatList());
        searchFilter.setIsConceptAssessed(Boolean.TRUE);
        domainKnowledgeManager.getAssessmentKnowledge().getScenario().getCourseConcepts(searchFilter, practiceCourseConcepts);

        // extract the remediation options, if any, before the domain knowledge manager is nulled at the end of the scenario.
        final generated.course.TrainingApplication.Options.Remediation remediationOptions = domainKnowledgeManager.getRemediationOptions();

        //listener for the Ped request message as a response to the course state message
        MessageCollectionCallback stateUpdateCallback =
                new MessageCollectionCallback() {

                    PedagogicalRequest request = null;

                    @Override
                    public void success() {

                        if(logger.isInfoEnabled()){
                            logger.info("Displaying the remediation course object(s) after "+trainingAppTransition.getTransitionName()+".");
                        }

                        try{

                            if(request != null) {
                                //a request from the ped needs to be handled, collect the course concepts that need remediation

                                RemediationInfo remediation = null;
                                AfterActionReviewRemediationEvent aarEvent = new AfterActionReviewRemediationEvent(trainingAppTransition.getTransitionName());
                                for(List<AbstractPedagogicalRequest> requestList : request.getRequests().values()){
                                    for(AbstractPedagogicalRequest aRequest : requestList){

                                        if(aRequest instanceof RequestBranchAdaptation){

                                            BranchAdaptationStrategy strategy = ((RequestBranchAdaptation)aRequest).getStrategy();
                                            if(strategy.getStrategyType() instanceof RemediationInfo){

                                                RemediationInfo remediationCandidate = (RemediationInfo)strategy.getStrategyType();
                                                if(remediationCandidate.isAfterPractice()){

                                                    remediation = remediationCandidate;

                                                    Map<String, List<AbstractRemediationConcept>> remediationMap = remediation.getRemediationMap();
                                                    for(String concept : remediationMap.keySet()){

                                                        // check if concept is in this dkf
                                                        if(practiceCourseConcepts.contains(concept)){
                                                            aarEvent.addRemediationInfo("'"+concept+"'");
                                                        }
                                                    }

                                                    break; // there should only be 1 after practice remediation request
                                                }

                                            }

                                        }

                                    } // end for

                                    // found an after practice remediation to work with
                                    if(remediation != null){
                                        break;
                                    }

                                } // end for

                                if(!aarEvent.getRemediationInfo().isEmpty()){

                                    // populate AAR event with the course concepts needing remediation so they show up in
                                    // the structured review course object
                                    Date eventTime = new Date();
                                    List<AbstractAfterActionReviewEvent> eventList = new ArrayList<>();
                                    eventList.add(aarEvent);
                                    pendingReviewCourseEvents.put(eventTime, eventList);

                                    //
                                    // use handler for finding appropriate remediation metadata, then build content course objects
                                    //
                                    DynamicContentHandler handler = courseManager.getTrainingAppRemediationHandler();
                                    generated.course.Practice practice = new generated.course.Practice();
                                    generated.course.Practice.PracticeConcepts practiceConcepts = new generated.course.Practice.PracticeConcepts();

                                    practiceConcepts.getCourseConcept().addAll(practiceCourseConcepts);
                                    practice.setPracticeConcepts(practiceConcepts);
                                    handler.setPracticeQuadrant(practice);

                                    // will contain all the course objects that will be inserted after this training app
                                    List<CourseObjectWrapper> courseObjects = new ArrayList<>();

                                    // add a structured review course object after this training app course object so that no
                                    // matter pass or fail of the DKF it will show the results.  This is similar to what GIFT
                                    // does after practice in adaptive courseflow course objects.
                                    generated.course.AAR autoAAR = new generated.course.AAR();
                                    autoAAR.setTransitionName(DynamicContentHandler.AFTER_PRACTICE_REMEDIATION_HEADING);
                                    generated.course.AAR.CourseObjectsToReview courseObjectsToReview = new generated.course.AAR.CourseObjectsToReview();
                                    courseObjectsToReview.getTransitionName().add(trainingAppTransition.getTransitionName());
                                    autoAAR.setCourseObjectsToReview(courseObjectsToReview);

                                    CourseObjectWrapper autoAARWrapper = CourseObjectWrapper.generateCourseObjectWrapper(autoAAR, courseManager.getCourseAuthoredDirectory());
                                    courseObjects.add(autoAARWrapper);

                                    RemediationCourseObject remediationCourseObject =
                                            new RemediationCourseObject(trainingAppTransition.getTransitionName());
                                    remediationCourseObject.setRemediationInfo(remediation);
                                    handler.buildContentDeliveryPhase(remediationCourseObject, courseObjects, true);

                                    if(remediationOptions != null){

                                        if(BooleanEnum.TRUE.equals(remediationOptions.getLoopUntilPassed())){
                                            // add practice again because the remediation option for this training application
                                            // has indicated the training application should happen until all course concepts
                                            // have been passed

                                            try{
                                                handler.incrementAndCheckPracticeAttempt();
                                            }catch(@SuppressWarnings("unused") CourseComprehensionException attemptsException){
                                                //force the course to end because the learner failed to comprehend the course concepts
                                                //in the training application dkf
                                                terminateSession(false, DynamicContentHandler.BAILOUT_DETAILS_MSG, null, MAX_ATTEMPTS_MSG, LessonCompletedStatusType.INSTRUCTIONAL_STRATEGY_REQUEST);
                                                return;
                                            }

                                            courseObjects.add(trainingAppCourseObjectWrapper);
                                        }
                                    }

                                    courseManager.insertTransitions(courseObjects);
                                }else{
                                    // the training application course object is over and won't be repeated
                                    expandableCourseObjectState = ExpandableCourseObjectStateEnum.NONE;
                                    courseManager.setTrainingAppRemediationHandler(null);
                                }


                            }else{
                                // the training application course object is over and won't be repeated
                                expandableCourseObjectState = ExpandableCourseObjectStateEnum.NONE;
                                courseManager.setTrainingAppRemediationHandler(null);
                            }

                            //course manager has expanded the list of transitions
                            callback.onSuccess();

                        }catch(@SuppressWarnings("unused") CourseComprehensionException comprehensionException){
                            //force the course to end because the learner failed to comprehend the concepts
                            //in the branch point
                            terminateSession(false, MerrillsBranchPointHandler.BAILOUT_DETAILS_MSG, null, MAX_ATTEMPTS_MSG, LessonCompletedStatusType.INSTRUCTIONAL_STRATEGY_REQUEST);
                            return;

                        }catch(Exception e){
                            logger.error("Caught exception while trying to handle the After Training App course object remediation for course object named '"+trainingAppTransition.getTransitionName()+"' for "+domainSessionInfo.getUsername()+".", e);
                            callback.onFailure(generateException("Handling remediation after training application course object", "The Domain module failed to handle the remediation after training app course object named '"+trainingAppTransition.getTransitionName()+"'."));
                        }
                    }

                    @Override
                    public void received(Message msg) {
                        if(msg.getPayload() instanceof PedagogicalRequest){
                            request = (PedagogicalRequest) msg.getPayload();

                            if(logger.isInfoEnabled()){
                                logger.info("Received ped request message of "+request+" for course state update of "+taEndCourseState);
                            }

                            handlePedagogicalRequest(request);

                        }else if(msg.getMessageType() == MessageTypeEnum.PROCESSED_ACK){
                            //nothing to do
                        }else{
                            callback.onFailure(generateException("Waiting for After Training App course object remediation request", "Failed to receive a Pedagogical Request"));
                        }

                    }

                    @Override
                    public void failure(Message msg) {

                        callback.onFailure(generateException("Waiting for After Training App course object remediation request", msg));
                    }

                    @Override
                    public void failure(String why) {

                        callback.onFailure(generateException("Waiting for After Training App course object remediation request", why));
                    }
                };

        //listen and wait for the training app 'finished when' state authored in the course transition
        final TrainingApplicationStateListener trainingAppStateListener = new TrainingApplicationStateListener() {

            final TrainingApplicationStateEnum finishState = TrainingApplicationStateEnum.valueOf(trainingAppTransition.getFinishedWhen());

            @Override
            public void onTrainingAppStateChanged(TrainingApplicationStateEnum state) {

                if (state == finishState) {

                    if(logger.isInfoEnabled()){
                        logger.info("Training Application transition is complete because training application state is now "+state);
                    }

                    unregister();

                    if(remediationOptions != null){
                        //update the course state and notify the pedagogical module which should send a ped request
                        //as a reply which could then have the possibility of creating structured review and remediation
                        //activities
                        taEndCourseState.setExpandableCourseObjectState(expandableCourseObjectState);
                        sendCourseState(taEndCourseState, stateUpdateCallback);
                    }else{
                        //the training application course object is not configured with remediation option,
                        //go on to the next course object
                        callback.onSuccess();
                    }

                    /* If a playback service was created, stop playback. */
                    playbackServiceFuture.thenAccept(playbackService -> {
                        if (playbackService != null) {
                            playbackService.terminatePlayback();
                            playbackServiceFuture = CompletableFuture.completedFuture(null);
                        }
                    });
                }
            }

            /**
             * No longer want to receive training application states.
             * Note: this should be called on a different thread than the one calling 'onTrainingAppStateChanged' to prevent
             * deadlocks and/or concurrent modification exception.
             */
            private void unregister(){
                removeTrainingAppStateListener(this);
            }
        };

        addTrainingAppStateListener(trainingAppStateListener);

        final generated.course.Guidance trainingAppGuidance = trainingAppTransition.getGuidance();

        final AsyncActionCallback startFinishedCallback = new AsyncActionCallback() {

            @Override
            public void onFailure(Exception e) {
                onSessionError(e);
            }

            @Override
            public void onSuccess() {

            }
        };

        //send the SIMAN.start message to the appropriate modules
        final AsyncActionCallback simanStartNextCallback = new AsyncActionCallback() {

            @Override
            public void onFailure(Exception e) {
                onSessionError(e);
            }

            @Override
            public void onSuccess() {

                // the domain knowledge manager will be null if the training app course object was ended during
                // this initialization sequence (e.g. the 'I'm finished here' button on the feedback widget ribbon)
                // No need to keep the callback sequence going because the next course object should be presented next
                if(domainKnowledgeManager == null){
                    return;
                }

                startScenario(startFinishedCallback);
            }
        };

        //send the lesson started message to the appropriate modules
        final AsyncActionCallback lessonStartedNextCallback = new AsyncActionCallback() {

            @Override
            public void onFailure(Exception e) {
                onSessionError(e);
            }

            @Override
            public void onSuccess() {

                // the tutor should no longer receive loading updates since it will hide the loading progress
                // once the lesson is started
                forwardContentLoadingUpdates = false;

                // the domain knowledge manager will be null if the training app course object was ended during
                // this initialization sequence (e.g. the 'I'm finished here' button on the feedback widget ribbon)
                // No need to keep the callback sequence going because the next course object should be presented next
                if(domainKnowledgeManager == null){
                    return;
                }

                // send the initial performance assessment to the learner module so that the learner module
                // stores the course concepts then creates a learner state message for the ped module so it
                // has the course concepts as well.
                try {
                    PerformanceAssessment performanceAssessment =
                            courseManager.getCurrentPerformanceAssessment();
                    performanceAssessmentCreated(performanceAssessment);

                } catch (Exception e) {
                    logger.error("Caught exception while trying to assess performance from a survey.", e);
                }

                lessonStarted(simanStartNextCallback);
            }
        };

        //send the init lesson message to the appropriate modules
        final AsyncActionCallback initLessonNextCallback = new AsyncActionCallback() {

            @Override
            public void onFailure(Exception e) {
                onSessionError(e);
            }

            @Override
            public void onSuccess() {

                // the domain knowledge manager will be null if the training app course object was ended during
                // this initialization sequence (e.g. the 'I'm finished here' button on the feedback widget ribbon)
                // No need to keep the callback sequence going because the next course object should be presented next
                if(domainKnowledgeManager == null){
                    return;
                }                
                    
                playbackServiceFuture.thenAccept(playbackService -> {
                    if (playbackService != null && logPlaybackServiceTopic != null) {
                        // Training app course object is playback -> provide the domain knowledge instance all the playback messages before starting the knowledge session
                        List<MessageManager> playbackMessages = playbackService.getMessages();
                        loadDomainKnowledge(playbackMessages, lessonStartedNextCallback, trainingAppCourseObjectWrapper);
                    }else{
                        // Training app course object is NOT playback -> no playback message collection to provide the domain knowledge instance
                        loadDomainKnowledge(null, lessonStartedNextCallback, trainingAppCourseObjectWrapper);
                    }
                });

            }

        };

        //send the init pedagogy to the pedagogical module
        AsyncActionCallback initPedagogyNextCallback = new AsyncActionCallback() {

            @Override
            public void onFailure(Exception e) {
                onSessionError(e);
            }

            @Override
            public void onSuccess() {

                // the domain knowledge manager will be null if the training app course object was ended during
                // this initialization sequence (e.g. the 'I'm finished here' button on the feedback widget ribbon)
                // No need to keep the callback sequence going because the next course object should be presented next
                if(domainKnowledgeManager == null){
                    return;
                }

                try{
                    String actionsStr = AbstractDKFHandler.getRawDKFActionKnowledge(domainKnowledgeManager.getDomainActionKnowledge().getActions());
                    InitializePedagogicalModelRequest pedRequest = new InitializePedagogicalModelRequest(actionsStr, false);
                    pedRequest.setPedModelConfig(courseManager.getCustomPedConfiguration());
                    sendInitializePedagogicalModel(pedRequest, initLessonNextCallback);
                }catch(Exception e){
                    logger.error("Caught exception while trying to send initialize pedagogical model request.",e );
                    onSessionError(generateException("Instantiating Pedagogical model during Initializing GIFT Domain Session", e.getMessage()));
                }
            }
        };

        //
        // start the initialization sequence - this will use the callbacks instantiated above
        // Note: the sequence is described in the ICD of the GIFT docs folder under "Message Sequences" subsection
        //
        final Map<String, Serializable> finalInputs = inputs;
        final AsyncActionCallback initAppConnectionsNextCallback = new AsyncActionCallback() {

            @Override
            public void onFailure(Exception e) {
                onSessionError(e);
            }

            @Override
            public void onSuccess() {

                // the domain knowledge manager will be null if the training app course object was ended during
                // this initialization sequence (e.g. the 'I'm finished here' button on the feedback widget ribbon)
                // No need to keep the callback sequence going because the next course object should be presented next
                if(domainKnowledgeManager == null){
                    return;
                }

                sendInitializeAppConnections(finalInputs, initPedagogyNextCallback);
            }
        };

        //determine what avatar data should eventually be shown
        String characterToUse = null;
        if(courseObjectCustomDefinedCharacter != null){
            //use course define character
            characterToUse = courseObjectCustomDefinedCharacter;
        }else if(dkfCustomDefinedCharacter != null) {
            // use DKF defined character
            characterToUse = dkfCustomDefinedCharacter;
        }

        TutorUserInterfaceFeedback tuiFeedback = null;
        if(characterToUse != null){
            if(characterToUse.isEmpty()){
                //use the default avatar configured for this GIFT instance

                tuiFeedback = new TutorUserInterfaceFeedback(
                        null, null, new DisplayAvatarAction(), null, null);
            }else{
                //use a specific avatar for this course transition

                AvatarData domainAvatarData = CharacterServiceUtil.generateCharacter(runtimeCourseFolderRelativePath, characterToUse);

                tuiFeedback = new TutorUserInterfaceFeedback(
                        null, null, new DisplayAvatarAction(domainAvatarData), null, null);
            }
        }

        final TutorUserInterfaceFeedback finalTuiFeedback = tuiFeedback;

        //
        // Currently GIFT requires that only a single character be used in a real-time assessment and
        // that the character be loaded before any training application scenario is loaded.  The main
        // reason for this is to allow enough time for characters like Virtual Human to load before
        // GIFT calls upon that character to perform some action.
        //
        final AsyncActionCallback alwayShowCharacterInitiallyCallback = new AsyncActionCallback() {

            @Override
            public void onFailure(Exception e) {
                onSessionError(e);
            }

            @Override
            public void onSuccess() {

                /* the domain knowledge manager will be null if the training app
                 * course object was ended during this initialization sequence
                 * (e.g. the 'I'm finished here' button on the feedback widget
                 * ribbon) No need to keep the callback sequence going because
                 * the next course object should be presented next */
                if(domainKnowledgeManager == null){
                    return;
                }

                if(finalTuiFeedback == null) {
                    //no character
                    initAppConnectionsNextCallback.onSuccess();

                } else {

                    if(finalTuiFeedback.getDisplayAvatarAction() != null && finalTuiFeedback.getDisplayAvatarAction().isPreloadOnly()) {
                        finalTuiFeedback.getDisplayAvatarAction().setPreloadOnly(false);
                    }

                    MessageCollectionCallback showFeedbackHandler =
                            new MessageCollectionCallback() {
                                @Override
                                public void success() {
                                    initAppConnectionsNextCallback.onSuccess();
                                }

                                @Override
                                public void received(Message msg) {
                                    // Do nothing
                                }

                                @Override
                                public void failure(Message msg) {
                                    onSessionError(new Exception("TUI administered feedback message failed to be responded to: "
                                            + "NACK Received: " + msg));
                                }

                                @Override
                                public void failure(String why) {
                                    onSessionError(new Exception("TUI administered feedback message failed to be responded to: "
                                            + why));
                                }
                            };

                    //use avatar
                    handleFeedbackUsingTUI(finalTuiFeedback, showFeedbackHandler);
                }
            }
        };

        /* the domain knowledge manager will be null if the training app course
         * object was ended during this initialization sequence (e.g. the 'I'm
         * finished here' button on the feedback widget ribbon) No need to keep
         * the callback sequence going because the next course object should be
         * presented next */
        if(domainKnowledgeManager == null){
            return;
        }

        // display the appropriate (if any) learner actions on the tutor
        sendLearnerActions().exceptionally(t -> {
            if (t instanceof Exception) {
                alwayShowCharacterInitiallyCallback.onFailure((Exception) t);
            } else {
                alwayShowCharacterInitiallyCallback.onFailure(new Exception(t));
            }

            return null;
        }).thenAccept(result -> alwayShowCharacterInitiallyCallback.onSuccess());

        //display any training app guidance message authored in the course transition
        if (trainingAppGuidance != null) {

            displayGuidanceTransition(trainingAppGuidance, true, new AsyncActionCallback() {

                @Override
                public void onSuccess() {
                    // nothing more to do
                }

                @Override
                public void onFailure(Exception e) {
                    onSessionError(e);
                }
            });

        } else if(!TrainingApplicationEnum.MOBILE_DEVICE_EVENTS.equals(TrainingAppUtil.getTrainingAppType(trainingAppTransition))){

            // If there isn't training app guidance and the current training app isn't the mobile app,
            // display the default loading guidance. This helps remove the course init instruction page
            // if that was the first page presented before this point in the course.

            //display the loading message afterward to ensure it is displayed inside the avatar panel
            sendDisplayDefaultLoadingMessageRequest(true);

            forwardContentLoadingUpdates = true;
        }

        if (finalTuiFeedback != null && finalTuiFeedback.getDisplayAvatarAction() != null) {

            // preload the avatar while loading messages are being shown
            finalTuiFeedback.getDisplayAvatarAction().setPreloadOnly(true);
            handleFeedbackUsingTUI(finalTuiFeedback, null);
        }

    }

    /**
     * Load the domain knowledge instance.  This allows the Scenario with its Task hierarchy
     * to get prepared by loading any necessary information ahead of the start methods being called.
     * 
     * @param playbackMessages optional list of playback messages that if populated contain all of the messages
     * that are about to be played back in actual recorded time sequence as part of this domain knowledge part of the course.  Can be null or empty.</br>
     * In the future this should be a stream that is not directly associated with
     * the collection of messages being played back.  Until then, callers should
     * NOT manipulate this collection.
     * @param lessonStartedNextCallback the callback used when the domain knowledge has some asynchronous event or request
     * to make.
     * @param trainingAppCourseObjectWrapper contains information about the training application course object that
     * started this domain knowledge part of the course.  
     */
    private void loadDomainKnowledge(final List<MessageManager> playbackMessages, 
            final AsyncActionCallback lessonStartedNextCallback, final CourseObjectWrapper trainingAppCourseObjectWrapper){
        
        // the host should load the DKF to assess, not a joiner, this way only one
        // Scenario class is actively assessing for all of those in a team session (or individual session)
        if(KnowledgeSessionManager.getInstance().isMemberOfTeamKnowledgeSession(getDomainSessionInfo().getDomainSessionId()) == null){
            domainKnowledgeManager.loadScenario(playbackMessages);
        }

        // If the training application is an embedded app the
        // displayLearnerActionsNextCallback is skipped since
        // it's actions were already called previously
        sendInitializeLesson(trainingAppCourseObjectWrapper.getCourseObjectReference().getContentReference(), lessonStartedNextCallback);
    }

    /**
     * Gets the set of properties surrounding how the current domain knowledge session should be presented
     * to observer controllers in Game Master.
     *
     * @return the observer controls. Can be null and will always be null when there is no active knowledge session.
     */
    private ObserverControls getObserverControls() {

        ObserverControls observerControls = new ObserverControls();

        if(currentCourseObject.getCourseObject() instanceof TrainingApplication) {

            String audioFilePath = null;

            TrainingApplication app = (TrainingApplication) currentCourseObject.getCourseObject();
            if(app.getInterops() != null) {
                for(Interop interop : app.getInterops().getInterop()) {
                    if(interop.getInteropInputs() != null
                            && interop.getInteropInputs().getInteropInput() instanceof DISInteropInputs) {

                        DISInteropInputs disInputs = (DISInteropInputs) interop.getInteropInputs().getInteropInput();
                        if(disInputs.getLogFile() != null) {
                            audioFilePath = disInputs.getLogFile().getCapturedAudioFile();
                        }
                    }
                }
            }

            if(audioFilePath != null) {
                // copy the audio file to the session output folder so that it can be used when playing
                // back this session log in UIs like the Game Master

                String courseFolderPath = domainSessionInfo.getDomainSourceId().substring(0,
                        domainSessionInfo.getDomainSourceId().lastIndexOf(Constants.FORWARD_SLASH));
                String sessionOutputFolderName =  getDomainSessionInfo().buildLogFileName();
                String workspaceAudioPath = null;
                try {
                    FileProxy capturedAudioProxy = courseManager.getCourseRuntimeDirectory().getRelativeFile(audioFilePath);
                    workspaceAudioPath = LogFilePlaybackService.copyAudioToSessionOutput(audioFilePath, courseFolderPath, capturedAudioProxy, sessionOutputFolderName);
                    observerControls.setCapturedAudioPath(workspaceAudioPath);
                }catch(Exception e) {
                    logger.error("Error sending mp3 file to GIFT/output/domainSessions/"+sessionOutputFolderName, e);
                }

            }
        }

        if(domainKnowledgeManager == null
                || domainKnowledgeManager.getAssessmentKnowledge() == null) {
            return observerControls;
        }

        Scenario scenario = domainKnowledgeManager.getAssessmentKnowledge().getScenario();
        if(scenario == null) {
            return observerControls;
        }

        if (scenario.getResources() != null && scenario.getResources().getObserverControls() != null) {

            generated.dkf.ObserverControls.Audio audio = scenario.getResources().getObserverControls().getAudio();
            if(audio != null) {

                String sessionOutputFolderName =  getDomainSessionInfo().buildLogFileName();

                if(audio.getGoodPerformance() != null) {
                    String goodPerfFile = audio.getGoodPerformance();
                    
                    // copy the audio file to the session output folder so that it can be used when playing
                    // back this session log in UIs like the Game Master

                    String courseFolderPath = domainSessionInfo.getDomainSourceId().substring(0,
                            domainSessionInfo.getDomainSourceId().lastIndexOf(Constants.FORWARD_SLASH));
                    String sessionOutputAudioPath = null;
                    try {
                        FileProxy capturedAudioProxy = courseManager.getCourseRuntimeDirectory().getRelativeFile(goodPerfFile);
                        LogFilePlaybackService.copyAudioToSessionOutput(goodPerfFile, courseFolderPath, capturedAudioProxy, getDomainSessionInfo().buildLogFileName());
                        
                        // use the session output folder to host this audio file for UIs like the Game Master
                        observerControls.setGoodPerformanceAudioUrl(UriUtil.makeURICompliant(sessionOutputFolderName + "/" + goodPerfFile));
                    }catch(Exception e) {
                        logger.error("Error sending good performance mp3 file to output/domainSessions - "+sessionOutputAudioPath, e);
                    }
                }

                if(audio.getPoorPerformance() != null) {                    
                    String poorPerfFile = audio.getPoorPerformance();
                    
                    // copy the audio file to the session output folder so that it can be used when playing
                    // back this session log in UIs like the Game Master

                    String courseFolderPath = domainSessionInfo.getDomainSourceId().substring(0,
                            domainSessionInfo.getDomainSourceId().lastIndexOf(Constants.FORWARD_SLASH));
                    String sessionOutputAudioPath = null;
                    try {
                        FileProxy capturedAudioProxy = courseManager.getCourseRuntimeDirectory().getRelativeFile(poorPerfFile);
                        LogFilePlaybackService.copyAudioToSessionOutput(poorPerfFile, courseFolderPath, capturedAudioProxy, getDomainSessionInfo().buildLogFileName());
                        
                        // use the session output folder to host this audio file for UIs like the Game Master
                        observerControls.setPoorPerformanceAudioUrl(UriUtil.makeURICompliant(sessionOutputFolderName + "/" + poorPerfFile));
                    }catch(Exception e) {
                        logger.error("Error sending poor performance mp3 file to output/domainSessions - "+sessionOutputAudioPath, e);
                    }
                }
            }
        }
        return observerControls;
    }

    /**
     * Used to clear out any events collected during an adaptive courseflow course object that are shown
     * in an after recall (pass or fail) or after practice structured review.  Clearing the variables makes
     * it so these events don't influence an upcoming adaptive courseflow in this course.
     */
    private void resetAdativeCourseflowAAR(){
        adaptiveCourseflowPendingAARInfo = null;
        adaptiveCourseflowPracticeScore = null;
        pendingAdaptiveCourseflowAAR = false;
        
        /** if cleanup was called while waiting for a message response, the course manager could be null */
        if(courseManager != null){
            courseManager.getMerrillsBranchPointManager().setCurrentHandler(null);
        }
    }

    /**
     * Handles an After Action Review course object by gathering all prior events and
     * then requesting that the AAR panel be displayed to the learner.
     *
     * @param aarTransition The authored AAR course object with configuration parameters.
     * @param callback Callback for when the action is done or fails
     */
    private void displayAfterActionReviewTransition(generated.course.AAR aarTransition, final AsyncActionCallback callback) {

        if (isActive()) {

            final AsyncActionCallback aarTransitionCallback = new AsyncActionCallback() {
                @Override
                public void onFailure(Exception e) {

                    callback.onFailure(e);
                }

                @Override
                public void onSuccess() {

                    // Now that the scores have been displayed, don't display them again
                    // The reason we don't clear when showing an adaptive courseflow structured review
                    // is that the logic to gather the review events for adaptive courseflow also
                    // removes objects added to these collections.  The remaining items in the collections
                    // still need to be there for the next structured review that isn't in an adaptive
                    // courseflow.
                    if(!pendingAdaptiveCourseflowAAR){

                        publishedScoreToEventIdMap.clear();

                        pendingReviewCourseEvents.clear();
                    }

                    resetAdativeCourseflowAAR();


                    if(logger.isInfoEnabled()){
                        logger.info("Clearing the collection of previous course events because AAR was just completed.");
                    }

                    callback.onSuccess();
                }
            };

            if (publishedScoreToEventIdMap.isEmpty()) {
                //there are no DKF scoring results to retrieve and display in the AAR, therefore just display the AAR with
                //any relevant information already known to this class (e.g. survey results)

                displayAfterActionReviewPanel(aarTransition, aarTransitionCallback);

            } else {
                //retrieve the DKF scoring results (that have not been provided in an AAR yet) to display in the AAR

                final MessageCollectionCallback lmsDataRequestCallback = new MessageCollectionCallback() {

                    LMSData records = null;

                    @Override
                    public void success() {

                        if(records != null) {

                            try{

                                if (records.getCourseRecords() != null) {
                                    //
                                    // Find the publish record event for each record, then build a new AAR course event object for that record
                                    //
                                    for (LMSCourseRecord record : records.getCourseRecords().getRecords()) {

                                        //make sure each record is handled, i.e. an AAR event element is created for it
                                        boolean handled = false;

                                        CourseRecordRef recordRef = record.getCourseRecordRef();
                                        LMSConnectionInfo lmsInfo = record.getLMSConnectionInfo();

                                        Iterator<PublishLessonScoreResponse> itr = publishedScoreToEventIdMap.keySet()
                                                .iterator();
                                        while (itr.hasNext()) {
                                            PublishLessonScoreResponse publishedScore = itr.next();

                                            CourseRecordRef ref = publishedScore.getPublishedRecordsByLMS().get(lmsInfo);
                                            if (recordRef.equals(ref)) {
                                                //found matching LMS and the matching record Id in this publish score instance

                                                // remove the adaptive courseflow review event so it doesn't get shown in any structured review
                                                // course object after this adaptive courseflow course object.
                                                if (pendingAdaptiveCourseflowAAR) {
                                                    //create AAR course event element
                                                    adaptiveCourseflowPendingAARInfo = new AfterActionReviewCourseEvent(
                                                            record.getRoot().getName(), record.getRoot());

                                                    if (logger.isDebugEnabled()) {
                                                        logger.debug("Set the AAR course event element of "
                                                                + adaptiveCourseflowPendingAARInfo + " as the current Adaptive courseflow pending AAR information");
                                                    }

                                                    itr.remove();

                                                } else {

                                                    //add event element to appropriate timestamped event
                                                    List<AbstractAfterActionReviewEvent> eventElements = pendingReviewCourseEvents
                                                            .get(publishedScoreToEventIdMap.get(publishedScore));

                                                    // search to see if the graded score node is already in the list of events
                                                    boolean found = false;
                                                    for(AbstractAfterActionReviewEvent aarEvent : eventElements){

                                                        if(aarEvent instanceof AfterActionReviewCourseEvent){
                                                            AfterActionReviewCourseEvent aarCourseEventCandidate = (AfterActionReviewCourseEvent)aarEvent;
                                                            if(aarCourseEventCandidate.getScore().getName().equals(record.getRoot().getName())){
                                                                found = true;
                                                            }
                                                        }
                                                    }

                                                    if(!found){
                                                        // the published record from the LMS is not in the list of events being collected by this class,
                                                        // need to create a new course event and add it to the list to show in the structured review
                                                        AfterActionReviewCourseEvent aarCourseEvent = new AfterActionReviewCourseEvent(
                                                                record.getRoot().getName(), record.getRoot());
                                                        eventElements.add(aarCourseEvent);

                                                        if (logger.isDebugEnabled()) {
                                                            logger.debug("Added the AAR course event element of "
                                                                    + aarCourseEvent + " to event at "
                                                                    + publishedScoreToEventIdMap.get(publishedScore));
                                                        }
                                                    }


                                                }

                                                handled = true;
                                            }
                                        }

                                        if (!handled) {
                                            logger.error("Unable to determine which course event the LMS record of "
                                                    + record
                                                    + " applies too.  Therefore that record will be skipped from AAR.");
                                        }
                                    }
                                }
                                displayAfterActionReviewPanel(aarTransition, aarTransitionCallback);

                            }catch(Exception e){
                                logger.error("Caught exception while trying to handle LMS course records received for AAR.", e);
                                aarTransitionCallback.onFailure(generateException("Handling LMS data", "Exception thrown while handling LMS data for AAR."));
                            }

                        } else {

                            aarTransitionCallback.onFailure(generateException("Retrieving LMS data", "Did not get any LMS data"));
                        }
                    }

                    @Override
                    public void received(Message msg) {
                        records = (LMSData) msg.getPayload();
                    }

                    @Override
                    public void failure(Message msg) {

                        aarTransitionCallback.onFailure(generateException("Retrieving LMS data", msg));
                    }

                    @Override
                    public void failure(String why) {

                        aarTransitionCallback.onFailure(generateException("Retrieving LMS data", why));
                    }
                };

                if(adaptiveCourseflowPracticeScore != null){
                    //request only the adaptive courseflow practice dkf scoring information, not all of the learner's records
                    //collected for other prior course objects
                    retrieveLessonScores(lmsDataRequestCallback, Arrays.asList(adaptiveCourseflowPracticeScore));
                }else{
                    retrieveLessonScores(lmsDataRequestCallback, publishedScoreToEventIdMap.keySet());
                }
            }
        }
    }

    /**
     * Query the LMS module for learner records using the provided collection to filter for specific record information.
     *
     * @param lmsDataRequestCallback used to notify when the LMS records request returns with information
     * @param publishLessonScoreResponses contains records to filter on when retrieving from the LMS
     */
    private void retrieveLessonScores(final MessageCollectionCallback lmsDataRequestCallback, Collection<PublishLessonScoreResponse> publishLessonScoreResponses) {

        if (isActive()) {
            sendLMSDataRequest(publishLessonScoreResponses, lmsDataRequestCallback);
        }

    }

    /**
     * Requests that the tutor display the AAR structured review panel.
     *
     * @param aarTransition contains configuration parameters for the authored AAR course object
     * @param callback used when the AAR course object is finished and the next course object in the course flow should be handled
     */
    private void displayAfterActionReviewPanel(generated.course.AAR aarTransition, final AsyncActionCallback callback) {

        if (isActive()) {

            final MessageCollectionCallback aarResponseHandler =
                    new MessageCollectionCallback() {
                        @Override
                        public void success() {

                            if(logger.isInfoEnabled()) {
                            logger.info("Received message for Display After Action Review Request. Displaying next transition.");
                            }

                            callback.onSuccess();
                        }

                        @Override
                        public void received(Message msg) {
                            // Do nothing
                        }

                        @Override
                        public void failure(Message msg) {
                            callback.onFailure(generateException("Displaying after action review", msg));
                        }

                        @Override
                        public void failure(String why) {
                            callback.onFailure(generateException("Displaying after action review", why));
                        }
                    };

            generated.course.AAR.CourseObjectsToReview courseObjectsToReview = aarTransition.getCourseObjectsToReview();

            //Sort events by event timestamp
            List<AbstractAfterActionReviewEvent> orderedEvents = new ArrayList<>();
            SortedSet<Date> keys = new TreeSet<>(pendingReviewCourseEvents.keySet());

            // Add the adaptive courseflow pending AAR event information (if provided) which will contain any scoring
            // information for Recall or Practice in an adaptive courseflow.  This info hasn't been presented yet and
            // should only be presented during the adaptive courseflow course object generated structured review.
            if(adaptiveCourseflowPendingAARInfo != null){
                orderedEvents.add(adaptiveCourseflowPendingAARInfo);
            }

            Iterator<Date> itr = keys.iterator();
            while(itr.hasNext()) {
                Date date = itr.next();
                List<AbstractAfterActionReviewEvent> events = pendingReviewCourseEvents.get(date);

                for (AbstractAfterActionReviewEvent e : events) {
                    /* For each event, check if it should be shown in the AAR
                     * before adding it to the list. */

                    //
                    if(e.getShowInAAR() && (adaptiveCourseflowPendingAARInfo == null || e.isAdaptiveCourseflowEvent())) {

                        if(courseObjectsToReview == null || courseObjectsToReview.getTransitionName().contains(e.getCourseObjectName())){

                            // Remove any adaptive courseflow event so it won't appear in subsequent structured review course objects that are generated
                            // or authored in the course flow.
                            if(adaptiveCourseflowPendingAARInfo != null && e.isAdaptiveCourseflowEvent()){
                                pendingReviewCourseEvents.remove(date);
                            }

                            orderedEvents.add(e);
                        }
                    }
                }
            }


            if(orderedEvents.isEmpty()){
                //there are no AAR events to show, display a static guidance message instead
                logger.info("Unable to provide the course authored AAR element because there are no course events to present.  " +
                        "Currently course events are either surveys with scoring information and/or training application instances that are assessed with DKFs which contain at least one scoring rule." +
                        "If you feel this is an error, please review this log file for errors as well as the course elements in this course ( name = "+
                        courseManager.getCourseName()+", file = "+ courseFile.getFileId()+").");


                sendDisplayGuidanceTextRequest(aarTransition.getTransitionName(), CourseManager.NO_AAR_MESSAGE, true, 0, false, aarResponseHandler);

            }else{


                boolean fullscreen = aarTransition.getFullScreen() != null ? Boolean.valueOf(aarTransition.getFullScreen().value()) : true;
                sendDisplayAARRequest(aarTransition.getTransitionName(), fullscreen, orderedEvents, aarResponseHandler);
            }
        }
    }

    /**
     * Displays some guidance to the learner for a guidance transition
     *
     * @param guidanceTransition The guidance transition
     * @param whileTrainingAppLoads whether the guidance should only be displayed for as long as it takes the training app to load
     * @param callback Callback for when the action is done or fails
     */
    private void displayGuidanceTransition(generated.course.Guidance guidanceTransition, boolean whileTrainingAppLoads, final AsyncActionCallback callback) {

        if (isActive()) {

            final Object guidanceChoice = guidanceTransition.getGuidanceChoice();

            MessageCollectionCallback guidanceTransitionCallback =
                    new MessageCollectionCallback() {
                        @Override
                        public void success() {

                            if(logger.isInfoEnabled()) {
                            logger.info("Guidance transition: '" + guidanceChoice + "' successfully handled. Displaying next transition.");
                            }

                            callback.onSuccess();
                        }

                        @Override
                        public void received(Message msg) {
                            // Do nothing
                        }

                        @Override
                        public void failure(Message msg) {

                            callback.onFailure(generateException("Displaying guidance transition: " + guidanceChoice, msg));
                        }

                        @Override
                        public void failure(String why) {

                            callback.onFailure(generateException("Displaying guidance transition: " + guidanceChoice, why));
                        }
                    };

            AbstractDisplayContentTutorRequest displayContentTutorRequest = AbstractDisplayContentTutorRequest.getRequest(guidanceTransition);
            if (guidanceTransition.getDisplayTime() != null) {
                //set duration in milliseconds
                displayContentTutorRequest.setDisplayDuration((int)(guidanceTransition.getDisplayTime().doubleValue() * 1000));
            }

            //if no duration is specified and whileTrainingAppLoads is true, this will cause the guidance
            //to display until the SIMAN.start is replied to from the Gateway module
            displayContentTutorRequest.setWhileTrainingAppLoads(whileTrainingAppLoads);

            if (guidanceChoice instanceof generated.course.Guidance.Message) {

                generated.course.Guidance.Message guidanaceMessage = (generated.course.Guidance.Message) guidanceChoice;

                if(logger.isDebugEnabled()) {
                    logger.debug("Sending display guidance request for guidance message transition = " + guidanaceMessage.getContent());
                }

                //no guidance message can accurately predict where the progress bar and message text will be located
                forwardContentLoadingUpdates = false;

            } else if (guidanceChoice instanceof generated.course.Guidance.File) {

                generated.course.Guidance.File guidanceFile = (generated.course.Guidance.File) guidanceChoice;

                String fileUrl = guidanceFile.getHTML();
                String networkURL = DomainModuleProperties.getInstance().getDomainContentServerAddress() + Constants.FORWARD_SLASH;

                // need to add course folder path from Domain folder to get domain hosted path to HTML file
                fileUrl = runtimeCourseFolderRelativePath + Constants.FORWARD_SLASH + fileUrl;

                // firefox browsers do not automatically replace backslashes, resulting in invalid urls
                fileUrl = UriUtil.makeURICompliant(fileUrl);

                fileUrl = networkURL + fileUrl;

                //the default loading guidance page supports the progress bar update layout
                forwardContentLoadingUpdates = fileUrl.endsWith(CourseManager.DEFAULT_LOADING_MESSAGE_FILE);

                guidanceFile.setHTML(fileUrl);

                if(logger.isDebugEnabled()) {
                    logger.debug("Sending display guidance request for guidance file transition = " + fileUrl);
                }

            } else if (guidanceChoice instanceof generated.course.Guidance.URL) {

                generated.course.Guidance.URL guidanceUrl = (generated.course.Guidance.URL) guidanceChoice;

                String fileUrl = guidanceUrl.getAddress();

                try{
                    String youtubeUrl = null;
                    youtubeUrl = LessonMaterialFileHandler.createEmbeddedYouTubeUrl(fileUrl, null);
                    if(youtubeUrl != null) {
                        guidanceUrl.setAddress(youtubeUrl);
                    }
                } catch (@SuppressWarnings("unused") Exception e){
                    // skip YouTube URL conversion if the URI does not have a URL protocol or the URL does not match known YouTube URL conventions
                }

                try{
                    UriUtil.validateUri(guidanceUrl.getAddress(), courseManager.getCourseRuntimeDirectory(), InternetConnectionStatusEnum.UNKNOWN);
                }catch(ConnectException e){
                    guidanceUrl.setMessage("There was a problem connecting to this content.  This is most likely due to not having an Internet connection at the moment.\n\nError:\n"+e.getLocalizedMessage());
                }catch(Exception e){
                    guidanceUrl.setMessage("There was a problem with this content.\n\nError:\n"+e.getLocalizedMessage());
                }

                //the default loading guidance page supports the progress bar update layout
                forwardContentLoadingUpdates = fileUrl.endsWith(CourseManager.DEFAULT_LOADING_MESSAGE_FILE);

                guidanceUrl.setAddress(fileUrl);

                if(logger.isDebugEnabled()) {
                logger.debug("Sending display guidance request for guidance url transition = " + fileUrl);
                }

            } else {

                if(logger.isDebugEnabled()) {
                    logger.debug("Sending display guidance request for guidance " + guidanceChoice);
                }
            }

            sendDisplayContentRequest(displayContentTutorRequest, guidanceTransitionCallback);

        }//end if on is Active
    }

    /**
     * Displays an Adaptive courseflow course object Recall phase survey to the learner for a survey transition
     *
     * @param recallTransition The recall transition that contains the recall survey transition information
     * @param callback Callback for when the action is done or fails
     */
    private void displayRecallSurvey(final RecallCourseObject recallTransition, final AsyncActionCallback callback){

        if (isActive()) {

            // It can be assumed for now that a survey course level transition only exists
            // when not in a lesson (i.e. training app course transition), therefore the scenario doesn't need to
            // be paused


            generated.course.Recall.PresentSurvey presentSurvey = (generated.course.Recall.PresentSurvey) recallTransition.getCourseObject();
            boolean fullScreen = presentSurvey.getFullScreen() != null ? Boolean.valueOf(presentSurvey.getFullScreen().value()): true;
            boolean showInAAR = presentSurvey.getShowInAAR() != null ? Boolean.valueOf(presentSurvey.getShowInAAR().value()): true;

            Serializable surveyChoice = presentSurvey.getSurveyChoice();
            String giftKey = null;
            GetSurveyRequest request = null;
            SurveyResultListener surveyResultsListener = null;

            if(surveyChoice instanceof generated.course.Recall.PresentSurvey.ConceptSurvey){
                //the survey is a concept knowledge based survey, need to build the survey from the question pool

                final List<generated.course.ConceptQuestions> recallConcepts = ((generated.course.Recall.PresentSurvey.ConceptSurvey)surveyChoice).getConceptQuestions();
                giftKey = ((generated.course.Recall.PresentSurvey.ConceptSurvey)surveyChoice).getGIFTSurveyKey();

                request = GetKnowledgeAssessmentSurveyRequest.createRequestForConcepts(courseManager.getSurveyContextId(), recallConcepts);

                //update request with question priorities
                courseManager.getMerrillsBranchPointManager().prioritizeRecallSurveyRequest(recallTransition, (GetKnowledgeAssessmentSurveyRequest) request);

                //listener for the survey responses in order to assess those responses and create a performance assessment
                surveyResultsListener = new SurveyResultListener() {

                    @Override
                    public void surveyCompleted(SurveyResponse surveyResponse) {

                        try {
                            PerformanceAssessment performanceAssessment =
                                    courseManager.assessPerformanceFromSurvey(courseManager.getMerrillsBranchPointManager().getHandler(recallTransition),  recallConcepts, surveyResponse);
                            performanceAssessmentCreated(performanceAssessment);

                        } catch (Exception e) {
                            logger.error("Caught exception while trying to assess performance from a recall survey.", e);
                            callback.onFailure(e);
                        }
                    }
                };

            }else if(surveyChoice instanceof String){
                //just a gift survey key

                giftKey = (String) surveyChoice;

                request = new GetSurveyRequest(courseManager.getSurveyContextId(), giftKey);

                throw new IllegalArgumentException("Using a GIFT Key for a Recall survey is not currently supported do to not being able to author assessment rules for that type of survey.");

                //listener for the survey responses in order to assess those responses and create a performance assessment
//                surveyResultsListener = new SurveyResultListener() {
//
//                    @Override
//                    public void surveyCompleted(SurveyResponse surveyResponse) {
//
//                        try {
//                            PerformanceAssessment performanceAssessment =
//                                    courseManager.assessPerformanceFromSurvey(courseManager.getMerrillsBranchPointManager().getHandler(recallTransition),  recallConcepts, surveyResponse);
//                            performanceAssessmentCreated(performanceAssessment);
//
//                        } catch (Exception e) {
//                            logger.error("Caught exception while trying to assess performance from a recall survey.", e);
//                            callback.onFailure(e);
//                        }
//                    }
//                };

            }else{
                //ERROR
                throw new IllegalArgumentException("Found unhandled Recall survey type of "+surveyChoice+" in a Merrills branch point course element.");
            }

            // Check to see if survey questions should be bypassed
            if (!DomainModuleProperties.getInstance().shouldBypassSurveyQuestions()) {

                //show a message to the user while the concept survey is created
                sendDisplayGuidanceTextRequest("Please Wait...", CourseManager.DEFAULT_CONCEPT_SURVEY_LOADING_MESSAGE, fullScreen, 0, true, null);

                DisplaySurveyParamWrapper displaySurveyParamWrapper = new DisplaySurveyParamWrapper();
                displaySurveyParamWrapper.fullScreen = fullScreen;
                displaySurveyParamWrapper.showInAAR = showInAAR;
                displaySurveyParamWrapper.isRecallSurvey = true;

                // recall surveys should always be displayed, pass an always behavior in
                SimpleMandatoryBehavior alwaysShowSurvey = new SimpleMandatoryBehavior();
                alwaysShowSurvey.setUseExistingLearnerStateIfAvailable(false);
                displaySurveyParamWrapper.mandatoryBehavior = alwaysShowSurvey;

                presentSurvey(courseManager.getMerrillsBranchPointManager().getCurrentHandler().getCourseObjectName(), request, displaySurveyParamWrapper, surveyResultsListener, callback);

            } else {

                logger.info("Bypassing survey with key '"
                        + giftKey + "' in domain session with ID "
                        + getDomainSessionInfo().getDomainSessionId()
                        + ". Displaying next transition.");

                callback.onSuccess();
            }
        }

    }

    /**
     * Displays a survey to the learner for a survey transition
     *
     * @param surveyTransition The survey transition
     * @param callback Callback for when the action is done or fails
     * @throws IOException if there was a problem parsing the course or any
     *         related course files
     * @throws DKFValidationException if there was a problem with the DKF
     *         associated with this training app transition
     * @throws FileValidationException if there was a problem validating a DKF
     *         against the schema
     */
    private void displaySurveyTransition(final generated.course.PresentSurvey surveyTransition, final AsyncActionCallback callback) throws IOException, FileValidationException, DKFValidationException {

        if (isActive()) {

            // It can be assumed for now that a survey course level transition only exists
            // when not in a lesson (i.e. training app course transition), therefore the scenario doesn't need to
            // be paused

            Object surveyChoice = surveyTransition.getSurveyChoice();
            boolean showInAAR = surveyTransition.getShowInAAR() != null ? Boolean.valueOf(surveyTransition.getShowInAAR().value()): true;
            boolean fullScreen = surveyTransition.getFullScreen() != null ? Boolean.valueOf(surveyTransition.getFullScreen().value()): true;
            Serializable behavior = surveyTransition.getMandatoryOption() == null ? null
                    : surveyTransition.getMandatoryOption().getMandatoryBehavior();

            if(surveyChoice instanceof String){
                //reference to a GIFT Survey key in the course survey context

                String surveyKey = (String)surveyChoice;

                // Check to see if survey questions should be bypassed
                if (!DomainModuleProperties.getInstance().shouldBypassSurveyQuestions()) {

                    GetSurveyRequest request = new GetSurveyRequest(courseManager.getSurveyContextId(), surveyKey);

                    DisplaySurveyParamWrapper displaySurveyParamWrapper = new DisplaySurveyParamWrapper();
                    displaySurveyParamWrapper.fullScreen = fullScreen;
                    displaySurveyParamWrapper.showInAAR = showInAAR;
                    displaySurveyParamWrapper.mandatoryBehavior = behavior;

                    presentSurvey(surveyTransition.getTransitionName(), request, displaySurveyParamWrapper, null, callback);

                } else {

                    logger.info("Bypassing survey with key '"
                            + surveyKey + "' in domain session with ID "
                            + getDomainSessionInfo().getDomainSessionId()
                            + ". Displaying next transition.");

                    callback.onSuccess();
                }



            }else if(surveyChoice instanceof ConceptSurvey){
                //a knowledge assessment survey from the course survey context question bank
                ConceptSurvey conceptSurvey = (ConceptSurvey)surveyChoice;
                conceptSurvey.setFullScreen(fullScreen ? BooleanEnum.TRUE : BooleanEnum.FALSE);
                presentSurveyTransition(surveyTransition.getTransitionName(), conceptSurvey, callback, showInAAR, behavior);

            }else if(surveyChoice instanceof generated.course.Conversation){
                //a conversation survey

                generated.course.Conversation conversation = (generated.course.Conversation)surveyChoice;

                if(conversation.getType() instanceof generated.course.ConversationTreeFile){

                    generated.course.ConversationTreeFile conversationTreeFile = (generated.course.ConversationTreeFile)conversation.getType();

                    //callback handler to use the conversation assessment to update performance assessment
                    ConversationAssessmentHandlerInterface conversationAssessmentHandler = new ConversationAssessmentHandlerInterface() {

                        @Override
                        public void assessPerformanceFromConversation(List<ConversationAssessment> assessments) {

                            try {
                                PerformanceAssessment performanceAssessment =
                                        courseManager.assessPerformanceFromConversation(assessments);
                                performanceAssessmentCreated(performanceAssessment);

                        } catch (Exception e) {
                            logger.error("Caught exception while trying to assess performance from a conversation survey course element.", e);
                        }

                        }
                    };

                    boolean fullscreen = surveyTransition.getFullScreen() == BooleanEnum.TRUE;
                    startConversation(conversationTreeFile, surveyTransition.getTransitionName(), fullscreen, null, conversationAssessmentHandler, true, callback);

                }else if(conversation.getType() instanceof generated.course.AutoTutorSession){
                    //an AutoTutor survey element


                    //this callback is used to clear the feedback history in the TUI once the AutoTutor course element is over
                    AsyncActionCallback autoTutorFinishedCallback = new AsyncActionCallback() {

                        @Override
                        public void onSuccess() {

                            // The training app has stopped. Clear all feedback messages from the TUI.
                            TutorUserInterfaceFeedback clearTextMsg = new TutorUserInterfaceFeedback(null, null, null, new ClearTextAction(), null);

                            MessageCollectionCallback showFeedbackHandler = new MessageCollectionCallback() {
                                @Override
                                public void success() {
                                    callback.onSuccess();
                                }

                                @Override
                                public void received(Message msg) {
                                    // Do nothing
                                }

                                @Override
                                public void failure(Message msg) {
                                    logger.warn("TUI administered clear feedback message failed to be responded to: "
                                            + "NACK Received: " + msg);
                                    callback.onFailure(new Exception("The tutor was unable to clear the feedback history."));
                                }

                                @Override
                                public void failure(String why) {
                                    logger.warn("TUI administered clear feedback message failed to be responded to: "
                                            + why);
                                    callback.onFailure(new Exception("The tutor was unable to clear the feedback history because '"+why+"'."));
                                }
                            };

                            // Send the message to clear the feedback messages from the TUI.
                            sendTUIFeedbackRequest(clearTextMsg, showFeedbackHandler);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            callback.onFailure(e);
                        }
                    };

                    boolean fullscreen = surveyTransition.getFullScreen() == BooleanEnum.TRUE;
                    presentSurveyTransition(surveyTransition.getTransitionName(), (generated.course.AutoTutorSession)conversation.getType(), fullscreen, autoTutorFinishedCallback);
                }else{
                    logger.error("Unhandled conversation survey type of "+conversation.getType()+" in present survey course object named '"+surveyTransition.getTransitionName()+"'.");
                    callback.onFailure(new Exception("Unhandled conversation survey type of "+conversation.getType()+" in present survey course object named '"+surveyTransition.getTransitionName()+"'."));
                }

            }else if(surveyChoice instanceof generated.course.SurveyExport){
                //a survey export course element, needs a present survey course object to be created for it

                generated.course.SurveyExport surveyExport = (generated.course.SurveyExport)surveyChoice;
                if(surveyExport.getType() instanceof generated.course.SurveyExport.Question){

                    String fileName = ((generated.course.SurveyExport.Question)surveyExport.getType()).getFile();
                    File file = new File(DomainModuleProperties.getInstance().getDomainDirectory() + File.separator + fileName);
                    presentSurveyFromQuestionExport(surveyTransition.getTransitionName(), file, callback);

                }else if(surveyExport.getType() instanceof generated.course.SurveyExport.Survey){

                    String fileName = ((generated.course.SurveyExport.Survey)surveyExport.getType()).getFile();
                    File file = new File(DomainModuleProperties.getInstance().getDomainDirectory() + File.separator + fileName);
                    presentSurveyFromSurveyExport(surveyTransition.getTransitionName(), file, callback);

            }else{
                    logger.error("Unhandled survey export type of "+surveyExport+" in a present survey course object named '"+surveyTransition.getTransitionName()+"'.");
                    callback.onFailure(new Exception("Unhandled surveyexport  type of "+surveyExport+" in present survey course object named '"+surveyTransition.getTransitionName()+"'."));
            }
            }else{
                logger.error("Unhandled survey type of "+surveyChoice+" in present survey course object named '"+surveyTransition.getTransitionName()+"'.");
                callback.onFailure(new Exception("Unhandled survey type of "+surveyChoice+" in present survey course object named '"+surveyTransition.getTransitionName()+"'."));
            }

        }
    }

    /**
     * Creates a present survey course object with the survey found in the survey export file provided.  Then
     * sends that to the tutor to be presented.
     *
     * @param currentCourseObjectName the name of the present survey course object to create.  This is useful for error messages
     * as well as showing the name of the current course object to the learner in the TUI.
     * @param surveyExportFile contains the survey to present
     * @param callback used to notify when the present survey course object is completed by the learner, or of an error
     */
    private void presentSurveyFromSurveyExport(final String currentCourseObjectName, final File surveyExportFile, AsyncActionCallback callback){

        if(!surveyExportFile.exists()){

            callback.onFailure(new DetailedException("Unable to present a survey from a survey export file for course object named '"+currentCourseObjectName+"'.",
                    "The survey export file '"+surveyExportFile+"' doesn't exist.", new FileNotFoundException()));
        }

        //
        // get question from file
        //

        String fileContent = null;
        try(FileInputStream fis = new FileInputStream(surveyExportFile)){
            fileContent = IOUtils.toString(fis);
        } catch (IOException e) {
            callback.onFailure(new DetailedException("Unable to present a survey from a survey export file for course object named '"+currentCourseObjectName+"'.",
                    "The survey export file '"+surveyExportFile+"' could not be read due to an exception that reads:\n"+e.getMessage(), e));
            return;
        }

        Survey survey = null;
        try{
            JSONObject obj = (JSONObject) JSONValue.parse(fileContent);
            SurveyJSON json = new SurveyJSON();
            survey = (Survey) json.decode(obj);

            //make sure the survey type is set (older survey's don't have this)
            SurveyTypeEnum surveyType = determineSurveyTypeEnum(survey);
            if(surveyType != null){
                survey.getProperties().setSurveyType(surveyType);
            }

        }catch(Exception e){
            callback.onFailure(new DetailedException("Unable to present a survey from a survey export file for course object named '"+currentCourseObjectName+"'.",
                    "The survey export file '"+surveyExportFile+"' contents did not successfully create a survey that could be presented to the learner because of an exception that reads:\n"+e.getMessage(), e));
            return;
        }

        DisplaySurveyParamWrapper displaySurveyParamWrapper = new DisplaySurveyParamWrapper();
        displaySurveyParamWrapper.fullScreen = true;
        displaySurveyParamWrapper.showInAAR = false;
        displaySurveyParamWrapper.saveResponseToDb = false;

        displaySurvey(currentCourseObjectName, 0, null, survey, displaySurveyParamWrapper, null, callback);
    }

    /**
     * Return the survey type for the survey given.
     * This logic was derived from {@link mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.SurveyEditorPanel#determineSurveyType(SurveyProperties)}.
     *
     * @param survey the survey to get the survey type from
     * @return the following:
     * 1. if the survey or the survey properties is null then null
     * 2. if the survey type is already set than that type
     * 3. if the survey name is {@value mil.arl.gift.common.survey.Constants.KNOWLEDGE_ASSESSMENT_GENERATED_SURVEY_NAME}, than {@value SurveyTypeEnum.ASSESSLEARNER_QUESTIONBANK}
     * 4. if the survey scorer has only Knowledge scoring than {@value SurveyTypeEnum.ASSESSLEARNER_STATIC}
     * 5. if the survey scorer has multiple scorers than {@value SurveyTypeEnum.COLLECTINFO_SCORED}
     * 6. otherwise {@value SurveyTypeEnum.COLLECTINFO_NOTSCORED}
     */
    private SurveyTypeEnum determineSurveyTypeEnum(Survey survey){

        if(survey == null || survey.getProperties() == null){
            return null;
        }

        return survey.getProperties().getSurveyTypePropertyOrSetIt(survey.getName());
        }

    /**
     * Creates a present survey course object with the question found in the question export file provided.  Then
     * sends that to the tutor to be presented.
     *
     * @param currentCourseObjectName the name of the present survey course object to create.  This is useful for error messages
     * as well as showing the name of the current course object to the learner in the TUI.
     * @param questionExportFile contains the question to present in a survey
     * @param callback used to notify when the present survey course object is completed by the learner, or of an error
     */
    private void presentSurveyFromQuestionExport(final String currentCourseObjectName, final File questionExportFile, AsyncActionCallback callback){

        if(!questionExportFile.exists()){

            callback.onFailure(new DetailedException("Unable to present a survey from a question export file for course object named '"+currentCourseObjectName+"'.",
                    "The question export file '"+questionExportFile+"' doesn't exist.", new FileNotFoundException()));
        }

        //
        // get question from file
        //

        String fileContent = null;
        try(FileInputStream fis = new FileInputStream(questionExportFile)){
            fileContent = IOUtils.toString(fis);
        } catch (IOException e) {
            callback.onFailure(new DetailedException("Unable to present a survey from a question export file for course object named '"+currentCourseObjectName+"'.",
                    "The question export file '"+questionExportFile+"' could not be read due to an exception that reads:\n"+e.getMessage(), e));
            return;
        }

        Survey survey = null;
        try{
            JSONObject obj = (JSONObject) JSONValue.parse(fileContent);
            QuestionJSON json = new QuestionJSON();
            AbstractQuestion question = (AbstractQuestion) json.decode(obj);

            //
            // build survey from question
            //
            survey = new Survey(0, currentCourseObjectName, new ArrayList<SurveyPage>(0), null, new SurveyProperties(), null, null);
            final SurveyProperties surveyProperties = survey.getProperties();
            surveyProperties.setBooleanPropertyValue(SurveyPropertyKeyEnum.HIDE_SURVEY_QUESTION_NUMBERS, true);
            surveyProperties.setBooleanPropertyValue(SurveyPropertyKeyEnum.HIDE_SURVEY_NAME, true);
            surveyProperties.setSurveyType(SurveyTypeEnum.COLLECTINFO_NOTSCORED);

            String pageTitle = "Page 1";

            if(question.getProperties() != null
                    && question.getProperties().hasProperty(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY)){

                //if the question has a title assigned to it, use that title as the name for its survey page
                pageTitle = (String) question.getProperties().getPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY);

            } else {

                //otherwise, hide the name of this question's survey page
                surveyProperties.setBooleanPropertyValue(SurveyPropertyKeyEnum.HIDE_SURVEY_PAGE_NUMBERS, true);
            }

            AbstractSurveyQuestion<?> surveyQuestion = AbstractSurveyQuestion.createSurveyQuestion(0, 0, question, new SurveyItemProperties());
            SurveyPage surveyPage = new SurveyPage(0, pageTitle, 0);

            surveyPage.getElements().add(surveyQuestion);
            survey.getPages().add(surveyPage);

        }catch(Exception e){
            callback.onFailure(new DetailedException("Unable to present a survey from a question export file for course object named '"+currentCourseObjectName+"'.",
                    "The question export file '"+questionExportFile+"' contents did not successfully create a survey that could be presented to the learner because of an exception that reads:\n"+e.getMessage(), e));
            return;
        }

        DisplaySurveyParamWrapper displaySurveyParamWrapper = new DisplaySurveyParamWrapper();
        displaySurveyParamWrapper.fullScreen = true;
        displaySurveyParamWrapper.showInAAR = false;
        displaySurveyParamWrapper.saveResponseToDb = false;

        displaySurvey(currentCourseObjectName, 0, null, survey, displaySurveyParamWrapper, null, callback);
    }

    /**
     * Presents a survey to the learner to be completed.  The survey will be dynamically created with questions
     * associated with specific concept(s) specified in the concept survey object.
     *
     * @param currentCourseObjectName the name of the authored course object that is causing a survey to be presented
     * @param surveyTransition the concept survey course transition information used to request a survey
     * be created with concept specific questions.
     * @param callback Callback for when presenting the survey is complete or a failure has occurred
     * @param showInAAR whether or not to display the survey results in any proceeding After Action Review
     * @param mandatory whether or not the transition survey is mandatory
     */
    private void presentSurveyTransition(final String currentCourseObjectName, final ConceptSurvey surveyTransition, AsyncActionCallback callback, boolean showInAAR, Serializable mandatory){

        if (isActive()) {

            // It can be assumed for now that a survey course level transition only exists
            // when not in a lesson (i.e. training app course transition), therefore the scenario doesn't need to
            // be paused

            List<generated.course.ConceptQuestions> recallConcepts = surveyTransition.getConceptQuestions();

            // Check to see if survey questions should be bypassed
            if (!DomainModuleProperties.getInstance().shouldBypassSurveyQuestions()) {

                boolean fullScreen = surveyTransition.getFullScreen() != null ? Boolean.valueOf(surveyTransition.getFullScreen().value()): true;

                //show a message to the user while the concept survey is created
                sendDisplayGuidanceTextRequest(null, CourseManager.DEFAULT_CONCEPT_SURVEY_LOADING_MESSAGE, fullScreen, 0, true, null);

                GetKnowledgeAssessmentSurveyRequest request = GetKnowledgeAssessmentSurveyRequest.createRequestFromConceptSurvey(courseManager.getSurveyContextId(), recallConcepts);

                //listener for the survey responses in order to assess those responses and create a performance assessment
                SurveyResultListener surveyResultsListener = new SurveyResultListener() {

                    @Override
                    public void surveyCompleted(SurveyResponse surveyResponse) {

                        if(surveyTransition.getSkipConceptsByExamination() == BooleanEnum.FALSE){
                            logger.info("The survey response will not be used to calculate a performance assessment, in addition it will not be used to skip upcoming lessons"+
                                " because the 'skip concepts by examination' flag is set to 'false'.");
                        }else{
                            try {
                                    PerformanceAssessment performanceAssessment =
                                            courseManager.assessPerformanceFromSurvey(surveyTransition, surveyResponse);
                                    performanceAssessmentCreated(performanceAssessment);

                            } catch (Exception e) {
                                logger.error("Caught exception while trying to assess performance from a survey.", e);
                            }
                        }
                    }
                };

                DisplaySurveyParamWrapper displaySurveyParamWrapper = new DisplaySurveyParamWrapper();
                displaySurveyParamWrapper.fullScreen = fullScreen;
                displaySurveyParamWrapper.showInAAR = showInAAR;
                displaySurveyParamWrapper.mandatoryBehavior = mandatory;

                presentSurvey(currentCourseObjectName, request, displaySurveyParamWrapper, surveyResultsListener, callback);

            } else {

                logger.info("Bypassing survey with key '"
                        + surveyTransition.getGIFTSurveyKey() + "' in domain session with ID "
                        + getDomainSessionInfo().getDomainSessionId()
                        + ". Displaying next transition.");

                callback.onSuccess();
            }
        }
    }

    /**
     * Performs the necessary steps to present a survey to the learner. The following steps
     * occur
     * <ol>
     *      <li>Fetch the survey</li>
     *      <li>Using the mandatory behavior ask the Pedagogical module if the survey is mandatory.
     *          <ol>
     *              <li>If the survey is mandatory present the survey to the learner</li>
     *              <li>If the survey isn't mandatory ask the learner if they'd like to retake the survey
     *                  <ol>
     *                      <li>If the learner says they'd like to retake the survey, present the survey</li>
     *                      <li>If the learner says they'd like to skip the survey, do not present the survey</li>
     *                  </ol>
     *              </li>
     *          </ol>
     *      </li>
     * </ol>
     *
     * @param currentCourseObjectName the name of the authored course object that is causing a
     *            survey to be presented
     * @param request - contains the survey information
     * @param displaySurveyParamWrapper contains various information needed to display the survey and deal with the survey response
     * @param surveyResultListener Callback for when the results of the survey are available
     * @param callback Callback for when presenting the survey is complete or a failure has occurred
     */
    private void presentSurvey(final String currentCourseObjectName, final GetSurveyRequest request, DisplaySurveyParamWrapper displaySurveyParamWrapper, final SurveyResultListener surveyResultListener, final AsyncActionCallback callback) {

        MessageCollectionCallback getSurveyCollection =
                new MessageCollectionCallback() {

                    private SurveyGiftData surveyGiftData;

                    /**
                     * Display a prompt to the learner asking if they want to skip the survey.
                     * 
                     * @param surveyName used for display purposes.  Shouldn't be null or empty. 
                     * @param learnerStateAttributesMap the learner state attributes that are required for the next
                     * course object.  The values are optional and can contain additional, more granular information such 
                     * as a course concept must have 'Journeyman' Expertise level enum for 'Knowledge' learner state attribute.  Can't be null.
                     * @param callback used to notify the caller of the user's response to the prompt
                     */
                    private void promptUserToSkipSurvey(String surveyName, Map<LearnerStateAttributeNameEnum, AttributeValues> learnerStateAttributesMap, MessageCollectionCallback callback) {
                        Survey userPrompt = createSurveyToSkipCourseObject("answer the '"+surveyName+"' survey", learnerStateAttributesMap, "Would you like to answer the '"+surveyName+"' survey anyway?", DONT_SKIP_CHOICE, SKIP_CHOICE);
                        sendDisplaySurveyRequest(userPrompt, true, callback, false);
                    }

                    @Override
                    public void success() {

                        if (surveyGiftData != null) {


                            /* If the author has specified that the survey should always be shown, show the survey.
                             * Otherwise if a different behavior was specified or if no behavior was specified (null), check if it is possible to skip the survey
                             */
                            Serializable mandatoryBehavior = displaySurveyParamWrapper.mandatoryBehavior;
                            if (mandatoryBehavior instanceof SimpleMandatoryBehavior
                                    && !((SimpleMandatoryBehavior) mandatoryBehavior).isUseExistingLearnerStateIfAvailable()) {
                                /* The surveyGiftData field is used here because in instances where the GetSurveyRequest
                                 * causes the UMS module to generate a new survey, the key for the generated survey is returned.
                                 * By passing the generated key instead we are ensuring that any future references to this survey
                                 * make use of the generated version. */
                                displaySurvey(currentCourseObjectName, request.getSurveyContextId(), surveyGiftData.getGiftKey(), surveyGiftData.getSurvey(),
                                        displaySurveyParamWrapper, surveyResultListener, callback);
                            } else {

                                //Determine the learner state attributes that the survey covers
                                final RequiredLearnerStateAttributes requiredLearnerStateAttributes = 
                                         getRequiredLearnerStatesFromSurvey(surveyGiftData, request);
                                if(requiredLearnerStateAttributes == null){
                                    // the author wishes to use the latest learner state to influence whether this survey is taken or not but
                                    // this survey doesn't contain any learner state attributes, therefore treat like mandatory
                                    displaySurvey(currentCourseObjectName, request.getSurveyContextId(), surveyGiftData.getGiftKey(), surveyGiftData.getSurvey(),
                                            displaySurveyParamWrapper, surveyResultListener, callback);
                                    return;
                                }
                                
                                CourseState state = new CourseState(generated.course.PresentSurvey.class.getCanonicalName());
                                state.setExpandableCourseObjectState(expandableCourseObjectState);
                                state.setRequiredLearnerStateAttributes(requiredLearnerStateAttributes);
                                Map<LearnerStateAttributeNameEnum, AttributeValues> learnerStateAttributesMap = 
                                        requiredLearnerStateAttributes.getLearnerStateAttributesMap();

                                //The callback to be used after the user specified whether they want to retake a survey or not
                                final MessageCollectionCallback handleUserPromptResponse = new MessageCollectionCallback() {

                                    SurveyResponse response = null;

                                    @Override
                                    public void success() {
                                        if(response == null) {
                                            callback.onFailure(new DetailedException(
                                                    "An unexpected response type was received",
                                                    "A response type of 'SurveyResponse' was expected but was never received",
                                                    null));
                                        }

                                        if(didUserSelectSkipOption(response, SKIP_CHOICE)) {
                                            //User indicated that they wanted to skip the survey, go straight to the supplied callback
                                            callback.onSuccess();
                                        } else {
                                            //User indicated that they wanted to retake the survey, handle the showing the survey then go to the supplied callback

                                            /* The surveyGiftData field is used here because in instances where the GetSurveyRequest
                                             * causes the UMS module to generate a new survey, the key for the generated survey is returned.
                                             * By passing the generated key instead we are ensuring that any future references to this survey
                                             * make use of the generated version. */
                                            displaySurvey(currentCourseObjectName, request.getSurveyContextId(), surveyGiftData.getGiftKey(),
                                                    surveyGiftData.getSurvey(), displaySurveyParamWrapper, surveyResultListener, callback);
                                        }
                                    }

                                    @Override
                                    public void received(Message msg) {
                                        if(msg.getMessageType() == MessageTypeEnum.DISPLAY_SURVEY_TUTOR_REPLY) {
                                            response = (SurveyResponse) msg.getPayload();
                                        }
                                    }

                                    @Override
                                    public void failure(Message msg) {
                                        callback.onFailure(new DetailedException(
                                                "There was a failure while asking the user if they wished to skip a survey",
                                                "The following failure message was reported while asking the user if they wanted to skip a survey: " + msg,
                                                null));
                                    }

                                    @Override
                                    public void failure(String why) {
                                        callback.onFailure(new DetailedException(
                                                "There was a failure while asking the user if they wished to skip a survey",
                                                "The following failure reason was reported while asking the user if they wanted to skip a survey: " + why,
                                                null));
                                    }
                                };

                                MessageCollectionCallback courseStateCallback = new MessageCollectionCallback() {

                                    // default value is to show the survey
                                    boolean displaySurvey = true;

                                    @Override
                                    public void success() {
                                        if (displaySurvey) {
                                            // display the survey if the pedagogical module didn't indicate that an adaptation was possible

                                            /* The surveyGiftData field is used here because in instances where the GetSurveyRequest
                                             * causes the UMS module to generate a new survey, the key for the generated survey is returned.
                                             * By passing the generated key instead we are ensuring that any future references to this survey
                                             * make use of the generated version. */
                                            displaySurvey(currentCourseObjectName, request.getSurveyContextId(), surveyGiftData.getGiftKey(), surveyGiftData.getSurvey(), displaySurveyParamWrapper, surveyResultListener, callback);
                                        } else {
                                            // if the the pedagogical module indicated that skipping the survey was possible, ask the user if they'd like to skip
                                            if(logger.isInfoEnabled()) {
                                                logger.info("Survey to be presented may be skipped due to the user already having the learner state data.");
                                            }
                                            promptUserToSkipSurvey(surveyGiftData.getSurvey().getName(), learnerStateAttributesMap, handleUserPromptResponse);
                                        }
                                    }

                                    @Override
                                    public void received(Message msg) {
                                        // if the course state update returns a pedagogical request,
                                        // then we assume that means we want to skip the survey.
                                        displaySurvey = msg.getMessageType() != MessageTypeEnum.PEDAGOGICAL_REQUEST;
                                    }

                                    @Override
                                    public void failure(Message msg) {
                                        if(logger.isInfoEnabled()) {
                                            logger.info("Failure trying to determine if we should bypass the survey. Defaulting to displaying the survey.");
                                        }

                                        /* The surveyGiftData field is used here because in instances where the GetSurveyRequest
                                         * causes the UMS module to generate a new survey, the key for the generated survey is returned.
                                         * By passing the generated key instead we are ensuring that any future references to this survey
                                         * make use of the generated version. */
                                        displaySurvey(currentCourseObjectName, request.getSurveyContextId(), surveyGiftData.getGiftKey(), surveyGiftData.getSurvey(), displaySurveyParamWrapper, surveyResultListener, callback);
                                    }

                                    @Override
                                    public void failure(String why) {
                                        if(logger.isInfoEnabled()) {
                                            logger.info("Failure trying to determine if we should bypass the survey. Defaulting to displaying the survey.");
                                        }

                                        /* The surveyGiftData field is used here because in instances where the GetSurveyRequest
                                         * causes the UMS module to generate a new survey, the key for the generated survey is returned.
                                         * By passing the generated key instead we are ensuring that any future references to this survey
                                         * make use of the generated version. */
                                        displaySurvey(currentCourseObjectName, request.getSurveyContextId(), surveyGiftData.getGiftKey(), surveyGiftData.getSurvey(), displaySurveyParamWrapper, surveyResultListener, callback);
                                    }
                                };

                                if (mandatoryBehavior instanceof FixedDecayMandatoryBehavior) {
                                    //Set the shelf life of the elements since the author indicated a shelf life
                                    FixedDecayMandatoryBehavior fixedDecayBehavior = (FixedDecayMandatoryBehavior) mandatoryBehavior;
                                    long shelfLife = fixedDecayBehavior.getLearnerStateShelfLife().longValue();
                                    for(LearnerStateAttributeNameEnum attrName : learnerStateAttributesMap.keySet()) {
                                        state.getLearnerStateShelfLife().put(attrName, shelfLife);
                                    }
                                }

                                sendCourseState(state, courseStateCallback);
                            }
                        } else {

                            if(logger.isInfoEnabled()) {
                                logger.info("Survey to be presented was null. Displaying next transition.");
                            }

                            callback.onSuccess();
                        }
                    }

                    @Override
                    public void received(Message msg) {
                        if (msg.getMessageType() == MessageTypeEnum.GET_SURVEY_REPLY) {

                            surveyGiftData = (SurveyGiftData) msg.getPayload();
                        }
                    }

                    @Override
                    public void failure(Message msg) {

                        callback.onFailure(generateException("Getting survey to be presented with survey key '"
                                + request.getGiftKey() + "' for survey context with ID '"
                                + request.getSurveyContextId() + "'",
                                msg));
                    }

                    @Override
                    public void failure(String why) {

                        callback.onFailure(generateException("Getting survey to be presented with survey key '"
                                + request.getGiftKey() + "' for survey context with ID '"
                                + request.getSurveyContextId() + "'",
                                why));
                    }
                };

        sendGetSurveyRequest(request, getSurveyCollection);
    }

    /**
     * Determines whether a user selected {@value #NO_CHOICE} in the generated survey for taking the course object.
     * @param response the response to the survey asking whether the user would like
     * to skip the next course object (see {@link #createSurveyToSkipCourseObject(String, Map)})
     * @param skipChoice the text of the survey question choice for skipping.  
     * @return true is the user decided to skip the survey, false otherwise
     */
    private boolean didUserSelectSkipOption(SurveyResponse response, String skipChoice) {
        String responseText = response.getSurveyPageResponses()
                .get(0).getQuestionResponses()
                .get(0).getResponses()
                .get(0).getText();

        return StringUtils.equals(skipChoice, responseText);
    }
    
    /**
     * Generates a survey that asks the user if they'd like to retake a course object
     * that isn't mandatory or if they'd like to skip the course object
     * @param activityToSkipDesc contains information about the activity that could be skipped.  This is shown to the user
     * in the details of why the survey is being presented.  E.g. "answer the 'Grit' survey", "learn about 'Concept 1'".  Shouldn't be null or empty. 
     * @param learnerStateAttributesMap the learner state attributes that are required for the next
     * course object.  The values are optional and can contain additional, more granular information such 
     * as a course concept must have 'Journeyman' Expertise level enum for 'Knowledge' learner state attribute. Can be null or empty.
     * @param questionText the text for the question.  can't be null or empty.
     * @param dontSkipChoice the text to show for the survey question choice that corresponds to NOT skipping the course object. Can't be null or empty.
     * @param skipChoice the text to show for the survey question choice that corresponds to skipping the course object. Can't be null or empty.
     * @return the generated survey to present to the learner
     */
    private static Survey createSurveyToSkipCourseObject(String activityToSkipDesc, Map<LearnerStateAttributeNameEnum, AttributeValues> learnerStateAttributesMap, String questionText, String dontSkipChoice, String skipChoice) {

        if(StringUtils.isBlank(skipChoice)){
            throw new IllegalArgumentException("The skip choice can't be null or empty");
        }else if((StringUtils.isBlank(dontSkipChoice))){
            throw new IllegalArgumentException("The don't skip choice can't be null or empty");
        }else if(StringUtils.isBlank(questionText)){
            throw new IllegalArgumentException("The question text can't be null or empty");
        }
        
        StringBuilder sb = new StringBuilder("The next activity for you was going to have you ")
                .append(activityToSkipDesc)
                .append(" but the tutor was able to find your existing records");
        if(CollectionUtils.isNotEmpty(learnerStateAttributesMap)) {
            sb.append(" on the following: <ul>");
            for(LearnerStateAttributeNameEnum attr : learnerStateAttributesMap.keySet()) {
                sb.append("<li>");
                sb.append(attr.getDisplayName());
                
                AttributeValues attributeValues = learnerStateAttributesMap.get(attr);
                if(attributeValues instanceof ConceptAttributeValues){
                    ConceptAttributeValues conceptAttributeValues = (ConceptAttributeValues)attributeValues;
                    
                    sb.append(" on ");
                    Iterator<String> conceptNamesItr = conceptAttributeValues.getConceptExpertiseLevel().keySet().iterator();
                    while(conceptNamesItr.hasNext()){
                        
                        if(!conceptNamesItr.hasNext()){
                            // this is the last concept, the end of the string is currently ", "
                            sb.append("and ");
                        }
                        
                        sb.append("'").append(conceptNamesItr.next()).append("'");
                        
                        if(conceptNamesItr.hasNext()){
                            sb.append(", ");
                        }
                    }
                }                                
                
                sb.append("</li>");
            }
            sb.append("</ul>");
        }

        String promptText = sb.toString();

        MultipleChoiceQuestion multipleChoiceQuestion = new MultipleChoiceQuestion(0, questionText, new SurveyItemProperties(), null, null, null);
        OptionList replyOptionSet = new OptionList();
        replyOptionSet.getListOptions().add(new ListOption(0, dontSkipChoice, 0));
        replyOptionSet.getListOptions().add(new ListOption(0, skipChoice, 0));

        multipleChoiceQuestion.setReplyOptionSet(replyOptionSet);

        MultipleChoiceSurveyQuestion promptQuestion = new MultipleChoiceSurveyQuestion(0, 0, multipleChoiceQuestion, new SurveyItemProperties());
        promptQuestion.setIsRequired(true);

        TextSurveyElement textElement = new TextSurveyElement(0, 0, new SurveyItemProperties());
        textElement.setText(promptText);

        SurveyPage promptPage = new SurveyPage();
        promptPage.setName("");
        promptPage.getElements().add(textElement);
        promptPage.getElements().add(promptQuestion);

        SurveyProperties surveyProps = new SurveyProperties();
        surveyProps.setHideSurveyName(true);
        surveyProps.setHideSurveyQuestionNumbers(true);
        surveyProps.setHideSurveyPageNumbers(true);

        Survey userPrompt = new Survey();
        userPrompt.setName("");
        userPrompt.setProperties(surveyProps);
        userPrompt.getPages().add(promptPage);

        return userPrompt;
    }

    /**
     * Adds information about the learner state attributes needed to determine whether this upcoming
     * survey can be skipped by the learner.
     *
     * @param surveyGiftData contains attributes for the survey to use to determine the learner state attributes the survey
     * might determine once the questions are answered
     * @param request contains attributes that were used to retrieve the survey, e.g. course concepts
     * @return information about learner state attributes the survey would elicit if given.  Can be null.
     */
    private RequiredLearnerStateAttributes getRequiredLearnerStatesFromSurvey(SurveyGiftData surveyGiftData, GetSurveyRequest request) {
        
        Map<LearnerStateAttributeNameEnum, AttributeValues> learnerStateAttributesMap = null;
        
        SurveyScorer surveyScorer = surveyGiftData.getSurvey().getScorerModel();
        
        // load attribute scorers
        if (surveyScorer != null && surveyScorer.getAttributeScorers() != null && !surveyScorer.getAttributeScorers().isEmpty()) {
            
            if(learnerStateAttributesMap == null){
                learnerStateAttributesMap = new HashMap<>();
            }
            
            for (AttributeScorerProperties attribute : surveyScorer.getAttributeScorers()) {
                if (!learnerStateAttributesMap.containsKey(attribute.getAttributeType())) {
                    if(logger.isInfoEnabled()){
                        logger.info("Found Learner State Enum: " + attribute.getAttributeType().getDisplayName());
                    }
                    learnerStateAttributesMap.put(attribute.getAttributeType(), null);
                }
            }
        }

        // load total scorers
        if (surveyScorer != null && surveyScorer.getTotalScorer() != null) {
            TotalScorer totalScorer = surveyScorer.getTotalScorer();
            if (totalScorer.getAttributeScorers() != null && !totalScorer.getAttributeScorers().isEmpty()) {
                
                if(learnerStateAttributesMap == null){
                    learnerStateAttributesMap = new HashMap<>();
                }
                
                for (AttributeScorerProperties attribute : totalScorer.getAttributeScorers()) {
                    if (!learnerStateAttributesMap.containsKey(attribute.getAttributeType())) {
                        if(logger.isInfoEnabled()){
                            logger.info("Found Learner State Enum: " + attribute.getAttributeType().getDisplayName());
                        }
                        learnerStateAttributesMap.put(attribute.getAttributeType(), null);
                    }
                }
            }

        }
        
        // load Question bank learner state
        SurveyTypeEnum surveyType = surveyGiftData.getSurvey().getProperties().getSurveyType();
        if(surveyType == SurveyTypeEnum.ASSESSLEARNER_QUESTIONBANK){
            
            if(learnerStateAttributesMap == null){
                learnerStateAttributesMap = new HashMap<>();
            }
            
            if(request instanceof GetKnowledgeAssessmentSurveyRequest){
                GetKnowledgeAssessmentSurveyRequest knowledgeAssessmentSurveyRequest = (GetKnowledgeAssessmentSurveyRequest)request;
                
                Map<String, ExpertiseLevelEnum> conceptExpertiseLevel = new HashMap<>();
                for(String conceptName : knowledgeAssessmentSurveyRequest.getConcepts().keySet()){
                    conceptExpertiseLevel.put(conceptName, null);
                }
                
                learnerStateAttributesMap.put(LearnerStateAttributeNameEnum.KNOWLEDGE, new ConceptAttributeValues(conceptExpertiseLevel));
            }          
        }
        
        if(learnerStateAttributesMap != null){
            return new RequiredLearnerStateAttributes(learnerStateAttributesMap);
        }
        
        return null;
    }


    /**
     * Present an AutoTutor session to the user.  The callback will be used when the chat is over,
     * either through a success or failure condition.
     *
     * Note: this is the legacy logic to managing an AutoTutor conversation.  The new logic (including conversation trees) is
     * using the ConversationManager class.
     *
     * @param surveyAssessment Information about the AutoTutor session to present
     * @param isFullscreen whether the survey should be presented in fullscreen mode
     * @param callback Used to notify the caller when the survey session is finished.
     */
    private void presentSurvey(final AutoTutorSurveyLessonAssessment surveyAssessment, boolean isFullscreen, final AsyncActionCallback callback){

        if(logger.isInfoEnabled()) {
        logger.info("Presenting AutoTutor survey assessment for "+domainSessionInfo+" (chat id = "+surveyAssessment.getChatId()+")...");
        }

        usingLegacyAutoTutorHandler = true;

        final MessageCollectionCallback displayChatWindowCallback =
                new MessageCollectionCallback() {

                    @Override
                    public void success() {
                        if(logger.isInfoEnabled()) {
                        logger.info("The chat for chat id "+surveyAssessment.getChatId()+" is over because the tutor module has replied to the original display chat window request");
                        }

                        //signal that the chat is over
                        callback.onSuccess();
                    }

                    @Override
                    public void received(Message msg) {
                        //nothing to do
                    }

                    @Override
                    public void failure(Message msg) {

                        callback.onFailure(generateException("Failed to show chat window for chat id "+surveyAssessment.getChatId()+"",
                                msg));
                    }

                    @Override
                    public void failure(String why) {

                        callback.onFailure(generateException("Failed to show chat window for chat id "+surveyAssessment.getChatId()+"",
                                why));
                    }
                };

        DisplayChatWindowRequest request = new DisplayChatWindowRequest(surveyAssessment.getChatId(), new DisplayAvatarAction());
        request.setProvideBypass(DomainModuleProperties.getInstance().shouldBypassChatWindows());
        request.setChatName("AutoTutor"); //give it a default name so unknown isn't displayed
        request.setFullscreen(isFullscreen);
        sendDisplayChatWindowRequest(request, displayChatWindowCallback);
    }

    /**
     * Presents an AutoTutor session to the learner to be completed as a course transition.
     *
     * @param transitionName the name of the present survey transition.  Can be null if it wasn't authored.
     * @param autoTutorSession - session configuration
     * @param isFullscreen whether the survey should be presented in fullscreen mode
     * @param callback Callback for when presenting the session is complete or a failure has occurred
     * @throws IOException if there was a problem parsing the course or any related course files
     * @throws DKFValidationException if there was a problem with the DKF associated with this training app transition
     * @throws FileValidationException if there was a problem validating a DKF against the schema
     */
    private void presentSurveyTransition(final String transitionName, final generated.course.AutoTutorSession autoTutorSession,
            final boolean isFullscreen, final AsyncActionCallback callback) throws IOException, FileValidationException, DKFValidationException {

        if(logger.isInfoEnabled()) {
            logger.info("Presenting AutoTutor survey course transition...");
        }

        Serializable configuration = autoTutorSession.getAutoTutorConfiguration();
        if(configuration instanceof DkfRef){
            //the autotutor script is referenced in a dkf

            //
            // initialize domain knowledge
            //
            String dkfName = ((DkfRef)configuration).getFile();
            FileProxy dkf = courseManager.getCourseRuntimeDirectory().getRelativeFile(dkfName);
            domainKnowledgeManager = new DomainKnowledgeManager(dkf, courseManager.getCourseRuntimeDirectory(), courseManager.getSessionOutputDirectory(),
                    null, this, this, assessmentProxy, domainSessionInfo);

            /**
             * This lesson state listener is responsible for starting the chat session, allowing the user time to review
             * the chat history at the end of the chat and finally cleaning up and notify the caller of this method to move
             * onto the next course transition.
             *
             * The sequence of events for an AutoTutor course transition are:
             * 1) generic AutoTutor DKF with specific AutoTutor script reference is loaded and lesson state transitions (manually, i.e. in this method) to Running
             * 2) the lessonStateListener (below) is notified of the Running state and notifies the tutor module to display a chat window
             * 3) the tutor module displays the chat window and replies with an empty chat window reply message
             * 4) the empty message is received by the AutoTutor condition which retrieves the first AutoTutor chat entry (usually ends with a question).
             * 5) the first AutoTutor chat entry is sent to the Tutor module for display purposes.
             * 6) Loop:  user types response, response goes through tutor loop, AutoTutor condition receives Pedagogical request for more assessment, next AutoTutor entry is sent to tutor module.
             * 7) ... end of chat is reached.  lesson state is transitions to Stopped.
             * 8) the lessonStateListener is notified of the Stopped state and changes the state to Paused to prevent this transition from ending thereby allowing the user to review the chat history.
             * 9) ... user selects to close the chat session.  Tutor module replies to original display chat window request made in lessonStateListener, which ends this transition.
             */
            final LessonStateListener lessonStateListener = new LessonStateListener() {

                AutoTutorSurveyLessonAssessment surveyAssessment;

                /**
                 * No longer want to receive lesson states.
                 * Note: this should be called on a different thread than the one calling 'onLessonStateChanged' to prevent
                 * deadlocks and/or concurrent modification exception.
                 */
                private void unregister(){
                    removeLessonStateListener(this);
                }

                @Override
                public void onLessonStateChanged(final LessonStateEnum state) {

                    if (state == LessonStateEnum.RUNNING) {

                        if(logger.isInfoEnabled()) {
                        logger.info("Lesson state is now "+state+", sending display chat window request. ");
                        }

                        new Thread("Assess Scenario Thread"){

                            @Override
                            public void run() {

                                //generate a new chat id since this logic is about to start a new chat session
                                int nextChatId =  DisplayChatWindowRequest.getNextChatId();
                                surveyAssessment = new AutoTutorSurveyLessonAssessment(nextChatId);

                                //when the chat window is closed (after the user has time to review the chat history), callback to our handler
                                presentSurvey(surveyAssessment, isFullscreen, chatClosedCallback);

                            }

                        }.start();

                    }else if(state == LessonStateEnum.STOPPED){

                        new Thread("Chat Session Completed"){

                            @Override
                            public void run() {

                                if(logger.isInfoEnabled()) {
                                logger.info("Changing the lesson state to "+LessonStateEnum.PAUSED+" to allow the user to review the chat log.");
                                }

                                // Change the lesson state to PAUSED to signal that the lesson is in limbo because the chat session is
                                // over, however the user now has the opportunity to review the chat session before selecting to continue.
                                // Note: not doing this will cause the chat window to automatically close w/o letting the user review the chat history.
                                changeLessonState(LessonStateEnum.PAUSED);

                            }

                        }.start();
                    }
                }

                /**
                 * This callback is used when the user finished reviewing the chat history at the end of the chat session.  It
                 * will do any cleanup for the logic that started the chat session and then kick-off the logic that will continue
                 * onto the next course transition.
                 */
                private AsyncActionCallback chatClosedCallback = new AsyncActionCallback() {

                    @Override
                    public void onSuccess() {

                        //no longer need our class to receive lesson state transition information
                        unregister();

                        if(logger.isInfoEnabled()) {
                        logger.info("The chat is over for chat id "+surveyAssessment.getChatId()+" because the user has choosen to close the chat window.");
                        }

                        //signal that the chat window is over
                        callback.onSuccess();

                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                };
            };

            addLessonStateListener(lessonStateListener);

            //Sixth: (after display learner actions) the domain knowledge has been started already, just check for failure here
            final AsyncActionCallback startDomainKnowledgeNextCallback = new AsyncActionCallback() {

                @Override
                public void onFailure(Exception e) {
                    onSessionError(e);
                }

                @Override
                public void onSuccess() {
                    //nothing to do
                }
            };

            //Fifth: (after lesson started) start the domain knowledge
            final AsyncActionCallback startLessonNextCallback = new AsyncActionCallback() {

                @Override
                public void onFailure(Exception e) {
                    onSessionError(e);
                }

                @Override
                public void onSuccess() {

                    //a formality in order to mimic a training application course element since this uses domain knowledge classes
                    changeTrainingAppState(TrainingApplicationStateEnum.RUNNING);

                    lessonStarted(startDomainKnowledgeNextCallback);
                }
            };

            //Fourth: (after clear learner actions) start the lesson
            final MessageCollectionCallback learnerActionsClearedCallback =  new MessageCollectionCallback() {

                @Override
                public void success() {
                     startLessonNextCallback.onSuccess();
                }

                @Override
                public void received(Message msg) {
                    // Nothing to do
                }

                @Override
                public void failure(Message msg) {
                    callback.onFailure(generateException("Displaying empty text guidance", msg));
                }

                @Override
                public void failure(String why) {
                    callback.onFailure(generateException("Displaying empty text guidance", why));
                }

            };


            //Third: (after init the lesson below) clear the learner actions on the tutor
            final AsyncActionCallback clearLearnerActionsCallback = new AsyncActionCallback() {

                @Override
                public void onFailure(Exception e) {
                    onSessionError(e);
                }

                @Override
                public void onSuccess() {
                    learnerActionsClearedCallback.success();
                }
            };

            //Second: (after init the pedagogy below) initialize the lesson
            final AsyncActionCallback initLessonNextCallback = new AsyncActionCallback() {

                @Override
                public void onFailure(Exception e) {
                    onSessionError(e);
                }

                @Override
                public void onSuccess() {
                    sendInitializeLesson(transitionName, clearLearnerActionsCallback);
                }

            };

            //a formality in order to mimic a training application course element since this uses domain knowledge classes
            changeTrainingAppState(TrainingApplicationStateEnum.LOADED);

            //First: start the sequence of events, starting by Initialize the pedagogy
            try{
                String actionsStr = AbstractDKFHandler.getRawDKFActionKnowledge(domainKnowledgeManager.getDomainActionKnowledge().getActions());
                InitializePedagogicalModelRequest pedRequest = new InitializePedagogicalModelRequest(actionsStr, false);
                sendInitializePedagogicalModel(pedRequest, initLessonNextCallback);
            }catch(Exception e){
                logger.error("Caught exception while trying to send initialize pedagogical model request.",e );
                onSessionError("Failed to instantiate the Pedagogical model during initialization of an AutoTutor survey course element", e.getMessage());
            }

        }else if(configuration instanceof generated.course.AutoTutorSKO){

            //callback handler to use the conversation assessment to update performance assessment
            ConversationAssessmentHandlerInterface conversationAssessmentHandler = new ConversationAssessmentHandlerInterface() {

                @Override
                public void assessPerformanceFromConversation(
                        List<ConversationAssessment> assessments) {


                    try {
                        PerformanceAssessment performanceAssessment =
                                courseManager.assessPerformanceFromConversation(assessments);
                        performanceAssessmentCreated(performanceAssessment);

                    } catch (Exception e) {
                        logger.error("Caught exception while trying to assess performance from a conversation survey course element.", e);
                    }

                }
            };

            startConversation(configuration, transitionName, isFullscreen, null, conversationAssessmentHandler, true, callback);
        }else{
            throw new DetailedException("Unable to start an AutoTutor instance for a 'Present Survey' course element (named '"+transitionName+"')" ,
                    "Found an unhandled AutoTutor script reference of "+configuration+".", null);
        }

    }

    /**
     * Handle the notification from the gateway that the learner answered a survey question through
     * the training application.
     *
     * @param surveyResponse contains the survey response
     */
    private void handleTrainingApplicationSurveyResponse(SurveyResponse surveyResponse){

        //TODO: only allow during training app course transition (lesson state is !stopped)
        DomainModule.getInstance().sendTrainingApplicationSurveyResponse(getDomainSessionInfo().getDomainSessionId(), surveyResponse);

    }

    /**
     * Handle the notification from the gateway that the learner submitted a survey question through
     * the training application.
     */
    private void handleTrainingApplicationSurveySubmit(){

        //TODO: only allow during training app course transition (lesson state is !stopped)
        DomainModule.getInstance().sendTrainingApplicationSurveySubmit(getDomainSessionInfo().getDomainSessionId());

    }

    /**
     * Displays the survey to be presented on the tutor
     *
     * @param currentCourseObjectName the name of the authored course object that is causing a survey to be presented
     * @param surveyContextId - the id of the survey context the survey is in
     * @param giftSurveyKey The key of the survey presented in the current survey context
     * @param survey The survey to present
     * @param displaySurveyParamWrapper contains various information needed to display the survey and deal with the survey response
     * @param surveyResultListener Callback for when the results of the survey are available
     * @param callback Callback for when presenting the survey is complete or a failure has occurred
     */
    private void displaySurvey(String currentCourseObjectName, int surveyContextId, String giftSurveyKey, Survey survey,
            DisplaySurveyParamWrapper displaySurveyParamWrapper,
            SurveyResultListener surveyResultListener, AsyncActionCallback callback) {

        MessageCollectionCallback surveyFinishedTutorCallback =
                new SurveyFinishedCallback(currentCourseObjectName, surveyContextId, giftSurveyKey, displaySurveyParamWrapper, surveyResultListener, callback);

        boolean surveyAnsweredGatewayHandling = false;
        if(displaySurveyParamWrapper.allowGatewaySurveyResponse){
            //need to send the survey being presented to the gateway as well to allow the learner
            //to answer through the training application

            //make sure the survey contains at least 1 survey question with external training application object ids
            //so that the learner can select objects in the application as answers to the question
            if(hasTrainingAppObjectRefs(survey)){

                surveyAnsweredGatewayHandling = true;
            }
        }

        sendDisplaySurveyRequest(survey, displaySurveyParamWrapper.fullScreen, surveyFinishedTutorCallback, surveyAnsweredGatewayHandling);
    }

    /**
     * Checks whether the survey has at least one survey question with external training application
     * object ids. Those ids should allow the learner to use the native training application to
     * answer the survey question.
     *
     * @param survey the survey to check its question
     * @return true if at least one survey question was found to have external training application
     *         object id property set.
     */
    private boolean hasTrainingAppObjectRefs(Survey survey) {

        List<SurveyPage> surveyPages = survey.getPages();
        for (SurveyPage surveyPage : surveyPages) {
            for (AbstractSurveyElement element : surveyPage.getElements()) {
                if (element instanceof AbstractSurveyQuestion) {
                    if (hasTrainingAppObjectRefs((AbstractSurveyQuestion<?>) element)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Return whether the survey question has external training application object ids. Those ids
     * should allow the learner to use the native training application to answer the survey
     * question.
     *
     * @param surveyQuestion the survey question to check
     * @return true if the survey question was found to have external training application object id
     *         property set.
     */
    private boolean hasTrainingAppObjectRefs(AbstractSurveyQuestion<?> surveyQuestion) {

        SurveyItemProperties properties;
        if (surveyQuestion.getProperties().hasProperty(SurveyPropertyKeyEnum.REPLY_EXTERNAL_TA_OBJ_ID)) {
            properties = surveyQuestion.getProperties();
        } else {
            properties = ((AbstractSurveyQuestion<?>) surveyQuestion).getQuestion().getProperties();
        }

        List<String> objectIds = properties.getStringListPropertyValue(SurveyPropertyKeyEnum.REPLY_EXTERNAL_TA_OBJ_ID);
        if (objectIds != null && !objectIds.isEmpty()) {
            return true;
        }

        return false;
    }

    /**
     * Sends the results of a presented survey to the be saved
     *
     * @param surveyContextId - the id of the survey context the survey is in
     * @param giftSurveyKey The key of the survey presented in the current survey context
     * @param surveyResponse The response to the presented survey
     * @param callback Callback for when presenting the survey is complete or a
     * failure has occurred
     * @param fullScreen - whether or not to display the results in fullscreen mode
     * @param isMidLessonSurvey whether this survey request comes in the middle of an external application course object.  This will
     * dictate whether a guidance message or feedback is presented to let the user know that the surey results are being saved
     */
    private void sendSurveyResults(final int surveyContextId, final String giftSurveyKey,
            SurveyResponse surveyResponse, final AsyncActionCallback callback, final boolean fullScreen, final boolean isMidLessonSurvey) {

        if(logger.isInfoEnabled()) {
        logger.info("Presented survey and now attempting to submit survey results");
        }

        //TODO: Should probably be more concerned if the action fails, maybe
        //      not pause the domain session until a response is received but
        //      at least terminate the session if a negative acknowledgment is
        //      received

        //callback for after the domain session has been initialized
        final MessageCollectionCallback surveyResultsHandler =
                new MessageCollectionCallback() {

                    @Override
                    public void success() {

                        if(logger.isInfoEnabled()){
                            logger.info("Submitted survey responses for user '"+domainSessionInfo.getUsername()+"' for survey with key of '"+giftSurveyKey+"' in survey context of '"+surveyContextId+"'.");
                        }
                        callback.onSuccess();
                    }

                    @Override
                    public void received(Message msg) {
                        // Do nothing
                    }

                    @Override
                    public void failure(Message msg) {

                        callback.onFailure(generateException("Forwarding Survey Response to UMS", msg));
                    }

                    @Override
                    public void failure(String why) {

                        callback.onFailure(generateException("Forwarding Survey Response to UMS", why));
                    }
                };

        if(!isMidLessonSurvey){
            // TODO: Either this should be displayed indefinitely until the next
            // transition is displayed or a callback is added to generate a failure
            // when the processed ACK is received because the survey results
            // should have been saved now
            sendDisplayGuidanceTextRequest("Please Wait...", SAVING_SURVEY_RESULTS_MSG, fullScreen, 30000, false, null);
        }

        sendSurveyResults(surveyResponse, surveyContextId, giftSurveyKey, surveyResultsHandler);

    }

    /**
     * Notifies the other modules to initialize a domain session.
     * Note: the message sequence is Init Learner, Init Domain Session, Init Ped Model, Start Domain Session, load
     * @throws Exception if there was a problem initializing the domain session
     */
    private void initializeGiftDomainSession(final AsyncActionCallback callback) throws Exception {

        //callback for after the lesson has been instantiated
        final AsyncActionCallback startDomainSessionNextCallback = new AsyncActionCallback() {

            @Override
            public void onFailure(Exception e) {
                onSessionError(e);
            }

            @Override
            public void onSuccess() {
                startGiftDomainSession(callback);
            }
        };

        //callback for after the ped model has been instantiated
        final AsyncActionCallback initLessonNextCallback = new AsyncActionCallback() {

            @Override
            public void onFailure(Exception e) {
                onSessionError(e);
            }

            @Override
            public void onSuccess() {
                sendInitializeLesson(getCourseManager().getCourseName(), startDomainSessionNextCallback);
            }
        };

        //callback for after the domain session has been initialized
        final MessageCollectionCallback domainInitializationHandler =
                new MessageCollectionCallback() {

                    @Override
                    public void success() {

                        try{
                            String actionsStr = null;
                            String configStr = null;

                            if(courseManager.getActions() != null){
                                actionsStr = AbstractCourseHandler.getRawCourseActionKnowledge(courseManager.getActions());
                            }

                            configStr = courseManager.getCustomPedConfiguration();

                            InitializePedagogicalModelRequest pedRequest = new InitializePedagogicalModelRequest(actionsStr, true);
                            pedRequest.setPedModelConfig(configStr);

                            sendInitializePedagogicalModel(pedRequest, initLessonNextCallback);

                        }catch(Exception e){
                            logger.error("Caught exception while trying to send initialize pedagogical model request.",e );
                            callback.onFailure(generateException("Instantiating Pedagogical model during Initializing GIFT Domain Session", e.getMessage()));
                        }
                    }

                    @Override
                    public void received(Message msg) {
                        // Do nothing
                    }

                    @Override
                    public void failure(Message msg) {

                        callback.onFailure(generateException("Initializing GIFT Domain Session", msg));
                    }

                    @Override
                    public void failure(String why) {

                        callback.onFailure(generateException("Initializing GIFT Domain Session", why));
                    }
                };

        //callback for after the learner has been instantiated
        final MessageCollectionCallback learnerInstantiateHandler = new MessageCollectionCallback() {

                @Override
                public void success() {

                    sendInitializeDomainSession(domainInitializationHandler);
                }

                @Override
                public void received(Message msg) {
                    // Do nothing
                }

                @Override
                public void failure(Message msg) {

                    callback.onFailure(generateException("Instantiating Learner during Initializing GIFT Domain Session", msg));
                }

                @Override
                public void failure(String why) {

                    callback.onFailure(generateException("Instantiating Learner during Initializing GIFT Domain Session", why));
                }
        };

        sendInitializeLearner(learnerInstantiateHandler);
    }

    /**
     * Notifies other modules that the Domain Session is starting.
     */
    private void startGiftDomainSession(final AsyncActionCallback callback) {

        if (isActive()) {

            sendStartDomainSession(new MessageCollectionCallback() {

                @Override
                public void success() {
                    callback.onSuccess();
                }

                @Override
                public void received(Message msg) {
                    // Do nothing
                }

                @Override
                public void failure(Message msg) {
                    callback.onFailure(generateException("Starting domain session", msg));
                }

                @Override
                public void failure(String why) {
                    callback.onFailure(generateException("Starting domain session", why));
                }
            });
        }
    }

    /**
     * Notifies the Gateway module to initialize interop connections for the
     * training application.  Then it sends a load scenario request to the gateway module.
     *
     * @param loadArgs collection of arguments needed for loading the appropriate content by the interop plugins or
     * the embedded application in the tutor module. Can't be null.  Currently can't be empty.
     * Key: In the case of an interop application, the name of a gateway interop plugin implementation class as defined in the course.xml (e.g. gateway.interop.ppt.PPTInterface).
     *      In the case of an embedded application, the url to navigate the iframe
     * Value: inputs for either the interop plugin implementation class running on the gateway
     * or the embedded application running on the tutor module
     * @param callback Callback for if the action is successful or not
     */
    private void sendInitializeAppConnections(final Map<String, Serializable> loadArgs, final AsyncActionCallback callback) {

        if (isActive()) {

            String domainContentServerAddr = DomainModuleProperties.getInstance().getDomainContentServerAddress();

            final Siman simanLoad = Siman.CreateLoad(loadArgs);

            playbackServiceFuture.thenAccept(playbackService -> {
                if (playbackService != null && logPlaybackServiceTopic != null) {

                    // There's no need to initialize interops when playing back
                    // simulation messages from a log since
                    // the Gateway module is not involved, so continue as if
                    // said interops were initialized
                    interopsEnabled = true;
                    loadScenario(simanLoad, callback);

                } else if (!isCurrentAppEmbedded) {

                    // When using gateway training applications (e.g. VBS), only
                    // the host needs the gateway module to be allocated
                    // Therefore other learners who are joiners can by-pass
                    // trying to communicate with the gateway module.
                    if (getDomainSessionInfo().isHostGatewayAllowed() && !getDomainSessionInfo().isGatewayConnected()
                            && !KnowledgeSessionManager.getInstance()
                                    .isHostOfTeamKnowledgeSession(getDomainSessionInfo().getDomainSessionId())) {
                        callback.onSuccess();
                        return;
                    }

                    DomainModule.getInstance().sendInitializeInteropConnection(domainSessionInfo,
                            domainContentServerAddr, loadArgs.keySet(), simanLoad.isPlaybackLoadArgs(), new MessageCollectionCallback() {

                                @Override
                                public void success() {
                                    interopsEnabled = true;
                                    loadScenario(simanLoad, callback);
                                }

                                @Override
                                public void received(Message msg) {
                                    currentInteropConnectionsInfo = (InteropConnectionsInfo) msg.getPayload();
                                }

                                @Override
                                public void failure(Message msg) {
                                    callback.onFailure(generateException("Initializing Interop Connections", msg));
                                }

                                @Override
                                public void failure(String why) {
                                    callback.onFailure(generateException("Initializing Interop Connections", why));
                                }
                            });
                } else {

                    // If it isn't an embedded app the interop connections have
                    // to be initialized
                    DomainModule.getInstance().sendInitializeEmbeddedAppConnection(domainSessionInfo, loadArgs.keySet(),
                            new MessageCollectionCallback() {

                                @Override
                                public void success() {
                                    interopsEnabled = true;
                                    loadScenario(simanLoad, callback);
                                }

                                @Override
                                public void received(Message msg) {
                                    currentInteropConnectionsInfo = (InteropConnectionsInfo) msg.getPayload();
                                }

                                @Override
                                public void failure(Message msg) {
                                    callback.onFailure(generateException("Initializing Embedded Connections", msg));
                                }

                                @Override
                                public void failure(String why) {
                                    callback.onFailure(generateException("Initializing Embedded Connections", why));
                                }

                            });
                }
            });
        }else{
            logger.warn("Unable to send the initialize interop connection message because the domain session is not active");
        }
    }

    /**
     * Communicate with the Gateway or Tutor Module that a scenario needs to be loaded.
     * Upon successful loading of the scenario/file by the tutor or gateway module this method will
     * change the training app state to Loaded.
     *
     * @param siman contains a collection of arguments needed for loading the appropriate content by the interop plugins
     * or the embedded applications iframe
     * @param callback Callback for if the action is successful or not
     */
    private void loadScenario(Siman siman, final AsyncActionCallback callback) {

        if (isActive()) {

            entityTable = new EntityTable(this);

            sendLoadScenario(siman, new MessageCollectionCallback() {

                @Override
                public void success() {

                    changeTrainingAppState(TrainingApplicationStateEnum.LOADED);

                    callback.onSuccess();
                }

                @Override
                public void received(Message msg) {
                    // Do nothing
                }

                @Override
                public void failure(Message msg) {
                    callback.onFailure(generateException("Loading scenario", msg));
                }

                @Override
                public void failure(String why) {
                    callback.onFailure(generateException("Loading scenario", why));
                }
            });
        }
    }

    /**
     * Notifies the gateway module to starts the scenario in the training application.
     * Upon success, this method will change the training app state to Running.
     *
     * @param callback used to notify the caller of the success or failure of sending the start scenario message(s).
     */
    private void startScenario(final AsyncActionCallback callback) {

        if (isActive()) {

            sendStartScenario(new MessageCollectionCallback() {

                @Override
                public void success() {

                    changeTrainingAppState(TrainingApplicationStateEnum.RUNNING);

                    callback.onSuccess();
                }

                @Override
                public void received(Message msg) {
                    // Do nothing
                }

                @Override
                public void failure(Message msg) {
                    callback.onFailure(generateException("Starting scenario", msg));
                }

                @Override
                public void failure(String why) {
                    callback.onFailure(generateException("Starting scenario", why));
                }
            });
        }
    }

    /**
     * Send a request to the tutor to display any learner actions available for the current training
     * application course transition.  Learner actions are interactive components such as buttons that
     * should be visible on the tutor user interface during the lesson/scenario.
     *
     * @param callback used for message callback functionality
     */
    private void sendLearnerActions(final AsyncActionCallback callback) {

        if (isActive()) {

            sendDisplayLearnerActionsRequest(
                    domainKnowledgeManager.getAssessmentKnowledge().getScenario().getResources().getLearnerActions().getLearnerAction(),
                    domainKnowledgeManager.getAssessmentKnowledge().getScenario().getResources().getScenarioControls(),
                    new MessageCollectionCallback(){

                @Override
                public void success() {
                    callback.onSuccess();
                }

                @Override
                public void received(Message msg) {
                }

                @Override
                public void failure(Message msg) {
                    callback.onFailure(generateException("Sending Learner Actions", msg));
                }

                @Override
                public void failure(String why) {
                    callback.onFailure(generateException("Sending Learner Actions", why));
                }
            });
        }
    }

    /**
     * Send a request to the tutor to display any learner actions available for
     * the current training application course transition. Learner actions are
     * interactive components such as buttons that should be visible on the
     * tutor user interface during the lesson/scenario.
     *
     * @return A {@link CompletableFuture} representing the asynchronous action.
     */
    private CompletableFuture<Void> sendLearnerActions() {
        final CompletableFuture<Void> toRet = new CompletableFuture<>();
        sendLearnerActions(new AsyncActionCallback() {

            @Override
            public void onSuccess() {
                toRet.complete(null);
            }

            @Override
            public void onFailure(Exception e) {
                toRet.completeExceptionally(e);
            }
        });

        return toRet;
    }

    @Override
    public void scenarioStarted() {

        if (isActive()) {
            //nothing to do...
            //Note: don't send a SIMAN message to the Gateway module because in instances where the training
            //      app is AutoTutor, there will be no gateway module interop for it.
        }
    }

    /**
     * Send the necessary information to initialize a pedagogical module in the Pedagogical module.
     *
     * @param pedRequest - the InitializatePedagogicalModelRequest to send
     * @param callback - used to notify the caller of whether the init ped model succeeded or failed
     */
    public void sendInitializePedagogicalModel(InitializePedagogicalModelRequest pedRequest, final AsyncActionCallback callback){

        if (isActive()) {

            sendInitializePedagogicalModel(pedRequest,

                    new MessageCollectionCallback() {
                        @Override
                        public void success() {
                            callback.onSuccess();
                        }

                        @Override
                        public void received(Message msg) {
                            // Do nothing
                        }

                        @Override
                        public void failure(Message msg) {
                            onSessionError(generateException("Sending the Lesson Started message because the scenario started", msg));
                        }

                        @Override
                        public void failure(String why) {
                            onSessionError(generateException("Sending the Lesson Started message because the scenario started", why));
                        }
                    });
        }
    }

    /**
     * Send the initialize lesson.
     *
     * @param contentReference a reference to the content to be displayed that is unique to this course.  Can't be null or empty.
     * @param callback used to notify the caller if the init lesson succeeded or failed
     */
    public void sendInitializeLesson(String contentReference, final AsyncActionCallback callback){

        if (isActive()) {

            sendInitializeLesson(contentReference,

                    new MessageCollectionCallback() {
                        @Override
                        public void success() {
                            callback.onSuccess();
                        }

                        @Override
                        public void received(Message msg) {
                            // Do nothing
                        }

                        @Override
                        public void failure(Message msg) {
                            onSessionError(generateException("Sending the Lesson Started message because the scenario started", msg));
                        }

                        @Override
                        public void failure(String why) {
                            onSessionError(generateException("Sending the Lesson Started message because the scenario started", why));
                        }
                    });
        }
    }

    /**
     * Send the lesson started to the appropriate modules.  Upon all recipients successfully handling
     * the lesson started message, this method will:
     * 1) change the lesson state to Running
     * 2) manually start the domain knowledge manager for the current lesson
     *
     * @param callback used to notify the caller of lesson start success or failure
     */
    private void lessonStarted(final AsyncActionCallback callback) {

        if (isActive()) {

            sendLessonStarted(new MessageCollectionCallback() {

                @Override
                public void success() {
                    
                    try{
                        changeLessonState(LessonStateEnum.RUNNING);
    
                        //now that the scenario has started, kick-off the assessment logic
                        // Note - only the host of a team/individual session should start Scenario class to activate
                        // it for assessment.
                        if(KnowledgeSessionManager.getInstance().isMemberOfTeamKnowledgeSession(getDomainSessionInfo().getDomainSessionId()) == null){
                            domainKnowledgeManager.start();
                        }
    
                        playbackServiceFuture.thenAccept(playbackService -> {
                        	if (playbackService != null) {
                                /* Start playback; ignore any previous message delay from
                                 * pausing since this is the 'beginning' of the playback
                                 * lesson */
                                playbackService.startPlayback(true);
                            }
                        	
                            if (playbackService != null && playbackService.getCapturedAudio() != null) {
    
                                /* if the current playback has an associated file
                                 * containing its captured audio, begin playing that
                                 * audio in the Tutor */
                                String audioUrl = UriUtil.makeURICompliant(
                                        DomainModuleProperties.getInstance().getDomainContentServerAddress()
                                                + Constants.FORWARD_SLASH + runtimeCourseFolderRelativePath
                                                + Constants.FORWARD_SLASH + playbackService.getCapturedAudio());
    
                                PlayAudioAction audioAction = new PlayAudioAction(audioUrl, null);
                                TutorUserInterfaceFeedback feedback = new TutorUserInterfaceFeedback(null, audioAction,
                                        null, null, null);
                                sendTUIFeedbackRequest(feedback, null);
                            }
    
                            callback.onSuccess();
                        });
                    }catch(Exception e){
                        throw new RuntimeException("Caught exception while trying to start the knowledge session for "+getDomainSessionInfo(), e);
                    }
                }

                @Override
                public void received(Message msg) {
                    // Do nothing
                }

                @Override
                public void failure(Message msg) {

                    onSessionError(generateException("Sending the Lesson Started message because the scenario started", msg));
                }

                @Override
                public void failure(String why) {

                    onSessionError(generateException("Sending the Lesson Started message because the scenario started", why));
                }
            });
                }
        }

    /**
     * Stops the scenario running in the training application
     * @param lessonCompleted contains information about why the lesson is ending.
     * @param callback used to notify the caller of when the training application scenario is stopped.  Can't be null.
     */
    private void stopTrainingAppScenario(final LessonCompleted lessonCompleted, final AsyncActionCallback callback) {

        if (isActive()) {

            if(logger.isInfoEnabled()) {
                logger.info("Attempting to stop the training app.");
            }

            MessageCollectionCallback stopScenarioHandler = new MessageCollectionCallback() {

                @Override
                public void success() {
                    if(logger.isInfoEnabled()) {
                        logger.info("The training app is stopped.");
                    }
                    interopsEnabled = false;
                    lessonCompleted(lessonCompleted, callback);
                        }

                @Override
                public void received(Message msg) {
                    // Do nothing
                }

                @Override
                public void failure(Message msg) {
                    callback.onFailure(generateException("Stopping scenario", msg));
                }

                @Override
                public void failure(String why) {
                    callback.onFailure(generateException("Stopping scenario", why));
                }
            };

            sendStopScenario(stopScenarioHandler);

            if (entityTable != null) {

                entityTable.stop();
                entityTable = null;

            }
        }
    }

    /**
     * Notify the other modules that the lesson is over
     * @param lessonCompleted information about why the lesson is ending.
     * @param callback used to notify the caller when the lesson completed has been handled by the other modules.
     */
    private void lessonCompleted(final LessonCompleted lessonCompleted, final AsyncActionCallback callback) {

        if (isActive()) {

            if(logger.isInfoEnabled()) {
                logger.info("Attempting to notify other modules that the lesson is completed.");
            }

            sendLessonCompleted(lessonCompleted, new MessageCollectionCallback() {

                @Override
                public void success() {
                    if(logger.isInfoEnabled()) {
                        logger.info("Successfully notified other modules, therefore changing lesson state to "+LessonStateEnum.STOPPED+".");
                    }
                    currentInteropConnectionsInfo = null;
                    changeLessonState(LessonStateEnum.STOPPED);

                    // don't publish real time assessment overall assessment scores when in remediation phase of adaptive
                    // courseflow course object.  This is in support of interactive remediation (part of ICAP framework)
                    // which uses a DKF for its features like feedback but we don't want to provide a structured review
                    // or remediation on the remediation activity itself.
                    if(currentCourseObject.isRemediationObject()){

                        if(logger.isInfoEnabled()){
                            logger.info("After practice remediation has finished for\n"+getDomainSessionInfo());
                        }
                        callback.onSuccess();

                    }else{
                        try{
                            publishLessonScore(callback);
                        }catch(Exception e){
                            logger.error("Caught exception while trying to publish the lesson's score for "+getDomainSessionInfo(), e);
                            callback.onFailure(e);
                        }
                    }
                }

                @Override
                public void received(Message msg) {
                    //Do nothing
                }

                @Override
                public void failure(Message msg) {
                    
                    try{
                        // still try to publish the score since the lesson is over, the error is most likely some
                        // other module can't end the lesson spaces on their side
                        publishLessonScoreOnError();
                    }finally{
                        callback.onFailure(generateException("Lesson completed", msg));
                    }
                }

                @Override
                public void failure(String why) {
                    try{
                        // still try to publish the score since the lesson is over, the error is most likely some
                        // other module can't end the lesson spaces on their side
                        publishLessonScoreOnError();
                    }finally{
                        callback.onFailure(generateException("Lesson completed", why));
                    }
                }
                
                /**
                 * The course is most likely ending because of an error while notifying the other modules
                 * that the lesson is over.  Attempt to publish the lesson scores anyway so we don't loose that information.
                 */
                private void publishLessonScoreOnError(){
                    
                    try{
                        publishLessonScore(new AsyncActionCallback() {
                            
                            @Override
                            public void onSuccess() {
                                // do nothing
                            }
                            
                            @Override
                            public void onFailure(Exception e) {
                                // do nothing                                
                            }
                        });
                    }catch(@SuppressWarnings("unused") Exception e){
                        // ignore, already in a mode to try and publish after a previous error on lesson completed
                    }
                }
            });
        }
    }
    
    /**
     * Add a published score event to this session that comes from a team knowledge session where
     * this session was a joiner, not a host.  This is necessary so this session can show the scoring
     * results in any future structured review.
     * 
     * @param publishedLessonScoreResponse the response by the LMS to the published lesson score message.  Contains
     * information about the records written to the LMS for future reference. Can't be null.
     * @param rootNode the root node of the score for the team knowledge session.  Can't be null.
     */
    public void addKnowledgeSessionJoinerPublishedScore(final PublishLessonScoreResponse publishedLessonScoreResponse, 
            final GradedScoreNode rootNode){   
        
        if(publishedLessonScoreResponse == null){
            throw new IllegalArgumentException("The published lesson score response is null.");
        }else if(rootNode == null){
            throw new IllegalArgumentException("The root node is null");
        }

        if(logger.isInfoEnabled()){
            logger.info("Adding a knowledge session score event to the collection of course events for a joiner of a team session-\n"+getDomainSessionInfo());
        }
        
        //get the event timestamp key
        Date eventTime = new Date();

        //add the high-level scoring information for later retrieval and use when AAR is presented
        publishedScoreToEventIdMap.put(publishedLessonScoreResponse, eventTime);

        //create an entry of event elements for this event
        List<AbstractAfterActionReviewEvent> eventList = new ArrayList<>();
        AfterActionReviewCourseEvent aarCourseEvent = new AfterActionReviewCourseEvent(DomainCourseFileHandler.getCourseObjectName(currentCourseObject.getCourseObject()), rootNode);
        eventList.add(aarCourseEvent);
        pendingReviewCourseEvents.put(eventTime, eventList);

    }

    /**
     * Gather the lesson score (for a dkf session), save it for possible display to learner in the TUI, and
     * send it to the LMS module.  This should be done at the end of the lesson (dkf).
     * @param callback used to notify that the LMS module received the lesson score and provided a response that
     * was saved.  Can't be null.
     */
    private void publishLessonScore(final AsyncActionCallback callback) {

        //gather the score 
        // Note: will be null for joiners of a team knowledge session since none of the tasks ever
        //       become active.
        final GradedScoreNode rootNode = domainKnowledgeManager.getScore();

        if (isActive()) {

            if (rootNode != null) {

                // notify the current active handler, if set, of the post practice assessment event
                courseManager.getMerrillsBranchPointManager().notifyAssessmentEvent(rootNode.getAssessment().hasReachedStandards());

                MessageCollectionCallback publishScoreCollection =
                        new MessageCollectionCallback() {

                            PublishLessonScoreResponse response = null;

                            @Override
                            public void success() {

                                if (response != null) {

                                    if(courseManager.getMerrillsBranchPointManager().getCurrentHandler() != null) {
                                        adaptiveCourseflowPracticeScore = response;
                                        adaptiveCourseflowPendingAARInfo = new AfterActionReviewCourseEvent(courseManager.getMerrillsBranchPointManager().getCurrentHandler().getCourseObjectName(), rootNode);
                                    } else {

                                        if(logger.isInfoEnabled()){
                                            logger.info("Adding a domain score event to collection of course events.");
                                        }
                                        
                                        //get the event timestamp key
                                        Date eventTime = new Date();

                                        //add the high-level scoring information for later retrieval and use when AAR is presented
                                        publishedScoreToEventIdMap.put(response, eventTime);

                                        if(!pendingAdaptiveCourseflowAAR){
                                            // - this is not a published score from an adaptive courseflow course object
                                            //create an entry of event elements for this event
                                            List<AbstractAfterActionReviewEvent> eventList = new ArrayList<>();
                                            AfterActionReviewCourseEvent aarCourseEvent = new AfterActionReviewCourseEvent(DomainCourseFileHandler.getCourseObjectName(currentCourseObject.getCourseObject()), rootNode);
                                            eventList.add(aarCourseEvent);
                                            pendingReviewCourseEvents.put(eventTime, eventList);
                                        }
                                        
                                        try{
                                            // notify any joiner sessions of the published score event
                                            // Will do nothing if this is an individual knowledge session.
                                            DomainModule.getInstance().notifyKnowledgeSessionJoinerPublishedScore(
                                                    getDomainSessionInfo().getDomainSessionId(), response, rootNode);
                                        }catch(Exception e){
                                            logger.error("There was a problem notifying the joiner knowledge session(s) about a publish score event.\nHost - "+getDomainSessionInfo(), e);
                                            callback.onFailure(e);
                                            return;
                                        }

                                    }
                                }

                                callback.onSuccess();
                            }

                            @Override
                            public void received(Message msg) {
                                //As of 7/17/14 the score record is being sent to the learner module which replies with a processed ack
                                if(msg.getPayload() instanceof PublishLessonScoreResponse){
                                    response = (PublishLessonScoreResponse) msg.getPayload();
                                }
                            }

                            @Override
                            public void failure(Message msg) {

                                callback.onFailure(generateException("Publishing score", msg));
                            }

                            @Override
                            public void failure(String why) {

                                callback.onFailure(generateException("Publishing score", why));
                            }

                        };

                    try{
                        sendPublishScore(new LMSCourseRecord(domainSessionInfo.getDomainSourceId(), rootNode, new Date()),
                                publishScoreCollection);
                        if(logger.isDebugEnabled()){
                            logger.debug("Finished sending publish score message, waiting for response.");
                        }
                    }catch(Exception e){
                        logger.error("There was a problem sending the publish score message for "+getDomainSessionInfo(), e);
                        // make sure to end the callback that was created which will trigger a cascade callback
                        publishScoreCollection.failure("An error was thrown when trying to send the publish score message that reads - "+e.getMessage());
                    }

            } else {

                if(logger.isInfoEnabled()) {
                    logger.info("No lesson score to publish, skipping sending lesson score to LMS.\n"+getDomainSessionInfo());
                }

                callback.onSuccess();
            }
        }
    }

    /**
     * Notification that the strategy with the name was just applied.  Can be used to notify
     * domain knowledge task triggers.
     * 
     * @param event contains information about the strategy that was applied.  Can't be null.
     */
    public void appliedStrategyNotification(StrategyAppliedEvent event){
        
        if(domainKnowledgeManager != null){
            
            if(StringUtils.isBlank(event.getStrategyName())){
                return;
            }
            domainKnowledgeManager.appliedStrategyNotification(event);
        }
    }

    /**
     * Clean up and release any resources that were being used to provide real time assessments.
     * This is called after a training application course object.
     *
     * @param reasonMessage a message to display to any learners still in the scenario.  Can't be null or empty.
     * @param status information about why the session is ending.
     */
    private void cleanUpRealTimeAssessmentSession(String reasonMessage, LessonCompletedStatusType status){

        domainKnowledgeManager.cleanup();
        domainKnowledgeManager = null;

        //Notify any team members that the session is over
        terminateDomainKnowledgeSession(reasonMessage, status);

        previousKnowledgeSession = KnowledgeSessionManager.getInstance().removeKnowledgeSession(domainSessionInfo.getDomainSessionId());
    }

    /**
     * Close the GIFT domain session by communicating to the various modules
     */
    private void closeDomainSession(final AsyncActionCallback callback) {

        final MessageCollectionCallback closeDomainSessionCallback =
                new MessageCollectionCallback() {

                    @Override
                    public void success() {

                        callback.onSuccess();
                    }

                    @Override
                    public void received(Message msg) {
                        // Do nothing
                    }

                    @Override
                    public void failure(Message msg) {

                        logger.error("Caught an exception while closing the domain session",
                                generateException("Restarting scenario", msg));

                        //try to push forward and get the user back to a semi-ready state - one or more modules
                        //may not be able to function w/o being restarted
                        callback.onSuccess();
                    }

                    @Override
                    public void failure(String why) {

                        logger.error("Caught an exception while closing the domain session",
                                generateException("Restarting scenario", why));

                        //try to push forward and get the user back to a semi-ready state - one or more modules
                        //may not be able to function w/o being restarted
                        callback.onSuccess();
                    }
                };

        sendCloseDomainSession(closeDomainSessionCallback);
    }

    /**
     * Stop the scenario (i.e. DKF) during a termination shutdown
     *
     * @param reason user friendly message describing why the session is ending. Used for logging purposes. Can't be null or empty.
     * @param status information about why the session is ending.
     * @param modulesClosedDomainSessionCallback The callback to execute when all modules have closed the domain session. Can't be null.
     * @throws IllegalArgumentException if the reason is null or empty or if modulesClosedDomainSessionCallback is null.
     */
    private void terminationShutdownStopScenario(final String reason, LessonCompletedStatusType status, final MessageCollectionCallback modulesClosedDomainSessionCallback) {
        if (logger.isTraceEnabled()) {
            logger.trace("terminationShutdownStopScenario(" + reason + ", " + modulesClosedDomainSessionCallback + ")");
        }

        if (domainKnowledgeManager != null) {

            //TODO: replace if condition with use of newly created 'interopsEnabled'
            CourseObjectWrapper coWrapper = courseManager.getCurrentTransition();
            if(coWrapper.getCourseObject() instanceof generated.course.PresentSurvey){
                if(logger.isInfoEnabled()) {
                    logger.info("Continuing to terminate domain session, currently in a 'Present Survey' course transition.");
                }
                terminationShutdownTerminateDomainKnowledgeManager(reason, status, modulesClosedDomainSessionCallback);
            }else{

                if(logger.isInfoEnabled()) {
                logger.info("Continuing to terminate domain session by sending stop scenario request");
                }

                MessageCollectionCallback stopScenarioHandler = new MessageCollectionCallback() {
                    @Override
                    public void success() {

                        interopsEnabled = false;
                        terminationShutdownTerminateDomainKnowledgeManager(reason, status, modulesClosedDomainSessionCallback);
                    }

                    @Override
                    public void received(Message msg) {
                        // Do nothing
                    }

                    @Override
                    public void failure(Message msg) {

                        logger.error("Error stopping scenario during termination shutdown.",
                                generateException("Stopping scenario", msg));

                        terminationShutdownTerminateDomainKnowledgeManager(reason, LessonCompletedStatusType.ERROR, modulesClosedDomainSessionCallback);
                    }

                    @Override
                    public void failure(String why) {

                        logger.error("Error stopping scenario during termination shutdown.",
                                generateException("Stopping scenario", why));

                        terminationShutdownTerminateDomainKnowledgeManager(reason, LessonCompletedStatusType.ERROR, modulesClosedDomainSessionCallback);
                    }
                };

                sendStopScenario(stopScenarioHandler);
            }

        } else {

            terminationShutdownTerminateDomainKnowledgeManager(reason, status, modulesClosedDomainSessionCallback);
        }
    }

    /**
     * Terminate the Domain Knowledge manager during a termination shutdown
     *
     * @param reason user friendly message describing why the session is ending. Used for logging purposes. Can't be null or empty.
     * @param status information about why the session is ending.
     * @param modulesClosedDomainSessionCallback The callback to execute when all modules have closed the domain session. Can't be null.
     * @throws IllegalArgumentException if the reason is null or empty or if modulesClosedDomainSessionCallback is null.
     */
    private void terminationShutdownTerminateDomainKnowledgeManager(final String reason, LessonCompletedStatusType status, final MessageCollectionCallback modulesClosedDomainSessionCallback) {

        if(modulesClosedDomainSessionCallback == null){
            throw new IllegalArgumentException("The modulesClosedDomainSessionCallback can't be null.");
        }

        //don't attempt to shutdown if already closed
        if(LOCAL_SESSION_STATE.CLOSED == localSessionState){
            modulesClosedDomainSessionCallback.success();
            return;
        }

        terminateDomainKnowledgeSession(reason, status);

        if(getDomainSessionInfo().isLearnerConnected()){
            courseManager.reportProgress(true);
        }

        terminationShutdownCloseSessionCoreModules(reason, modulesClosedDomainSessionCallback);
    }

    /**
     * Close the domain session on all modules except the Tutor Module so the
     * termination reason can be displayed during a termination shutdown
     *
     * @param reason optional user friendly message describing why the session is ending.
     * @param modulesClosedDomainSessionCallback The callback to execute when all modules have closed the domain session. Can't be null.
     * @throws IllegalArgumentException if the modulesClosedDomainSessionCallback is null.
     */
    private void terminationShutdownCloseSessionCoreModules(final String reason, final MessageCollectionCallback modulesClosedDomainSessionCallback) {

        if(modulesClosedDomainSessionCallback == null){
            throw new IllegalArgumentException("The modulesClosedDomainSessionCallback can't be null.");
        }

        //don't attempt to shutdown if already closed
        if(LOCAL_SESSION_STATE.CLOSED == localSessionState){
            modulesClosedDomainSessionCallback.success();
            return;
        }

        if(logger.isInfoEnabled()) {
            logger.info("Proceeding with termination shutdown, sending close domain session messages");
        }

        // We are spawning a thread here to prevent deadlocks when shutting down the domain session.
        Thread terminateThread = new Thread("Close Domain Session No Tutor"){

            @Override
            public void run(){
                sendCloseDomainSessionNotToTutor(reason, new MessageCollectionCallback() {

                    @Override
                    public void success() {
                        /* Informs the caller that the modules successfully
                         * closed the specified domain session. The shutdown
                         * process can now continue to the remaining shutdown
                         * procedures such as displaying information to the course
                         * taker about why the shutdown occurred. */
                        modulesClosedDomainSessionCallback.success();
                    }

                    @Override
                    public void received(Message msg) {
                        // Do nothing
                    }

                    @Override
                    public void failure(Message msg) {
                        /* Informs the caller that the modules failed to
                         * close the specified domain session. The error can
                         * be logged and any remaining actions that need to occur
                         * within the shutdown process, such as informing the course taker
                         * why the shutdown took place, can occur. */
                        modulesClosedDomainSessionCallback.failure(msg);

                        logger.error("Error closing domain session except the tutor during termination shutdown.",
                                generateException("Closing domain session except for the tutor module", msg));

                    }

                    @Override
                    public void failure(String why) {
                        /* Informs the caller that the modules failed to
                         * close the specified domain session. The error can
                         * be logged and any remaining actions that need to occur
                         * within the shutdown process, such as informing the course taker
                         * why the shutdown took place, can occur. */
                        modulesClosedDomainSessionCallback.failure(why);

                        logger.error("Error closing domain session except the tutor during termination shutdown.",
                                generateException("Closing domain session except for the tutor module", why));

                    }
                });
            }
        };

        terminateThread.start();
    }

    /**
     * Wrapper method that calls the shutdown method in it's own thread
     */
    public void terminationShutdown() {

        // We are spawning a thread here to prevent deadlocks when terminating the domain session.
        Thread terminateThread = new Thread("terminationShutdown"){

            @Override
            public void run(){
                terminationShutdownCloseSessionTutor();
            }
        };

        terminateThread.start();
    }

    /**
     * Builds the default error message in HTML format to display to the user with the provided reason and details
     *
     * @param reason the reason for the error
     * @param details details related to the error
     * @return An HTML formated string to display to the user
     * @throws IllegalArgumentException if the reason is null or empty.
     */
    public String buildDefaultTerminationShutdownDisplayErrorMessage(String reason, String details) {

        if(reason == null || reason.isEmpty()){
            throw new IllegalArgumentException("The reason can't be null or empty.");
        }

        String message = "<p><font size='7'>The course is ending prematurely.</font></p><br><br>"+
                "<table style='width: 100%'><tr><font size='4'><td style='width: 8%:' align='left'><b>Reason:</b></td><td align='left'>" + org.apache.commons.lang3.StringUtils.replace(reason, Constants.NEWLINE, Constants.HTML_NEWLINE) + "</td></font></tr>"+
                "<tr><font size='3'><td style='width: 8%;' align='left'><i>Details:</i></td><td align='left'>" + org.apache.commons.lang3.StringUtils.replace(details, Constants.NEWLINE, Constants.HTML_NEWLINE) + "</td></font></tr></table><br><br>"+
                "If there was no other indication of an error, please try the course again.<br><br>If you have access to the GIFT runtime then refer to the Domain Module log in GIFT/output/logger/module for additional information.";
        return message;
    }

    /**
     * Builds the default callback used when the domain session shuts down. Builds the default error message to display to the user using the provided reason and details.
     * The callback does the following in order:
     * 1. Displays the error message to the user
     * 2. Sends the Close Domain Session request to the Tutor
     * 3. Cleans up the domain session
     *
     * @param reason the reason for the error. Can't be null or empty.
     * @param details details related to the error. Can be null.
     * @return A call back which will display a message to the user before shutting down
     * @throws IllegalArgumentException if the reason is null or empty.
     */
    public MessageCollectionCallback buildModulesClosedDomainSessionCallback(String reason, String details) {
        final String message = buildDefaultTerminationShutdownDisplayErrorMessage(reason, details);
        return buildModulesClosedDomainSessionCallback(message);
    }

    /**
     * Builds the default callback used when the domain session shuts down. Does the following in order:
     * 1. Creates the callback which displays the message to the user
     * 2. Sends the Close Domain Session request to the Tutor
     * 3. Cleans up the domain session
     *
     * @param message The HTML formatted message to display to the user. Can't be null or empty.
     * @return A call back which will display a message to the user before shutting down
     * @throws IllegalArgumentException if the message is null or empty.
     */
    public MessageCollectionCallback buildModulesClosedDomainSessionCallback(String message) {

        if(message == null || message.isEmpty()){
            throw new IllegalArgumentException("The message can't be null or empty.");
        }

        MessageCollectionCallback callback = new MessageCollectionCallback() {

            @Override
            public void success() {
                terminationShutdownDisplayError(message);
            }

            @Override
            public void received(Message msg) {
                // Do nothing
            }

            @Override
            public void failure(Message msg) {
                terminationShutdownDisplayError(message);
            }

            @Override
            public void failure(String why) {
                terminationShutdownDisplayError(message);
            }

        };

        return callback;
    }

    /**
     * Display the termination reason on the tutor during a termination shutdown
     *
     * @param reason user friendly message describing why the session is ending. Can't be null or empty.
     * @param details a more developer friendly message describing why the session is ending.  Can be null.
     * @param displayMessageToUser The HTML formatted message to display to the user in the TUI. Can't be null or empty.
     * @throws IllegalArgumentException if displayMessageToUser is null or empty.
     */
    public void terminationShutdownDisplayError(String displayMessageToUser) {

        if (displayMessageToUser == null || displayMessageToUser.isEmpty()) {
            throw new IllegalArgumentException("The display message can't be null or empty.");
        }

        // We are spawning a thread here to prevent deadlocks when terminating the domain session.
        Thread terminateThread = new Thread("terminationShutdownDisplayError"){

            @Override
            public void run(){

                if(logger.isDebugEnabled()) {
                    logger.debug("Sending display text request for termination shutdown, text = " + displayMessageToUser);
                }
                
                // when this session is a team knowledge session host, the joiner sessions also need to be closed prematurely
                DomainModule.getInstance().notifyMembersOfDomainSessionTermination(getDomainSessionInfo().getDomainSessionId(), displayMessageToUser);

                sendDisplayGuidanceTextRequest("Course is ending", displayMessageToUser, true, 0, false,
                        new MessageCollectionCallback() {

                            @Override
                            public void success() {

                                terminationShutdownCloseSessionTutor();
                            }

                            @Override
                            public void received(Message msg) {
                                // Do nothing
                            }

                            @Override
                            public void failure(Message msg) {

                                logger.error("Error displaying the termination reason in the tutor during termination shutdown.",
                                        generateException("Displaying termination reason", msg));

                                terminationShutdownCloseSessionTutor();
                            }

                            @Override
                            public void failure(String why) {

                                logger.error("Error displaying the termination reason in the tutor during termination shutdown.",
                                        generateException("Displaying termination reason", why));

                                terminationShutdownCloseSessionTutor();
                            }
                        });
            }
        };

        terminateThread.start();
    }

    /**
     * Close the domain session on the Tutor Module after the termination reason
     * has been displayed during a termination shutdown
     */
    private void terminationShutdownCloseSessionTutor() {

        //don't attempt to shutdown if already closed
        if(LOCAL_SESSION_STATE.CLOSED == localSessionState){

            if(logger.isDebugEnabled()){
                logger.debug("Not sending close domain session to tutor because the local session state is "+localSessionState);
            }

            return;
        }

        sendCloseDomainSessionToTutor(new MessageCollectionCallback() {

            @Override
            public void success() {

                cleanUp();
            }

            @Override
            public void received(Message msg) {
                // Do nothing
            }

            @Override
            public void failure(Message msg) {

                logger.error("Error closing domain session for the tutor during termination shutdown.",
                        generateException("Closing domain session for the tutor module", msg));

                cleanUp();
            }

            @Override
            public void failure(String why) {

                logger.error("Error closing domain session for the tutor during termination shutdown.",
                        generateException("Closing domain session for the tutor module", why));

                cleanUp();
            }
        });
    }

    public void handleTutorSurveyQuestionResponse(AbstractQuestionResponse questionResponse){

        //only send when in a mid lesson survey because that is the only time when an external application
        //is connected to the gateway module
        if(hasSupportingInterop(MessageTypeEnum.TUTOR_SURVEY_QUESTION_RESPONSE)){
            DomainModule.getInstance().sendTutorSurveyQuestionResponse(questionResponse, domainSessionInfo);
        }
    }

    /**
     * Builds the encrypted URL to be used by the LTI consumer to request the course information
     * from the LTI provider. It is encrypted using OAuth and the client secret key so that we can
     * provide secure requests to and from the LTI consumer and provider. The URL optional
     * parameters will come from the given custom parameters which contains a list of key value
     * pairs which will be converted into the format "custom_[key]=[value]".
     *
     * @param ltiId the LTI provider id.
     * @param customParameters the custom parameters.
     * @param rawUrl the raw media url.
     * @return the protected LTI provider url. Will return null if the given ltiId or rawUrl are
     *         null.
     */
    public String handleGetLtiProviderUrl(String ltiId, CustomParameters customParameters, String rawUrl) {

        String protectedUrl = null;

        // need to get the correct LTI provider so we can retrieve the client key and secret.
        LtiProvider provider = findCourseLtiProvider(ltiId);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Found an LTI provider using LTI id [" + ltiId + "]: " + Boolean.valueOf(provider != null).toString() + " and rawUrl is: " + rawUrl);
        }

        // required values, if null then return null
        if (provider != null && rawUrl != null) {


            // determine if the user opted to allow the provider to return a score or not; defaults to returning a score.
            boolean returnScore = true;
            if (DomainCourseFileHandler.isTransitionLessonMaterial(currentCourseObject.getCourseObject())) {
                generated.course.LessonMaterial lm = (generated.course.LessonMaterial) currentCourseObject.getCourseObject();
                if (lm.getLessonMaterialList() != null && lm.getLessonMaterialList().getMedia() != null) {
                    for (Media media : lm.getLessonMaterialList().getMedia()) {
                        if (media.getMediaTypeProperties() != null && media.getMediaTypeProperties() instanceof LtiProperties) {
                            BooleanEnum allowScore = ((LtiProperties) media.getMediaTypeProperties()).getAllowScore();
                            returnScore = BooleanEnum.TRUE.equals(allowScore);
                            break;
                        }
                    }
                }
            }

            String contextId = courseManager.getCourseName(); // TODO course id would be better
            String resourceLinkId = Integer.toString(getDomainSessionInfo().getDomainSessionId());
            String userId = getUserId();

            // build the OAuth message using the standard parameters used by LTI providers.
            OAuthMessage om = new OAuthMessage(POST, rawUrl, null);
            om.addParameter(OAuth.OAUTH_CONSUMER_KEY, provider.getKey());
            om.addParameter(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.HMAC_SHA1);
            om.addParameter(OAuth.OAUTH_VERSION, OAuth.VERSION_1_0);
            om.addParameter(OAuth.OAUTH_TIMESTAMP, Long.valueOf(new Date().getTime() / 1000).toString());
            om.addParameter(OAuth.OAUTH_NONCE, UUID.randomUUID().toString());
            om.addParameter(OAuth.OAUTH_CALLBACK, OAUTH_CALLBACK_VALUE);
            om.addParameter(BasicLTIConstants.LTI_MESSAGE_TYPE, BasicLTIConstants.LTI_MESSAGE_TYPE_BASICLTILAUNCHREQUEST);
            om.addParameter(BasicLTIConstants.LTI_VERSION, BasicLTIConstants.LTI_VERSION_1);
            om.addParameter(BasicLTIConstants.USER_ID, userId);
            // TODO: We will have more Role options when we implement Roles in GIFT.
            // ex:[Learner, Instructor, Administrator, TeachingAssistant, ContentDeveloper, and Mentor]
            om.addParameter(BasicLTIConstants.ROLES, LEARNER);
            om.addParameter(BasicLTIConstants.RESOURCE_LINK_ID, resourceLinkId);
            om.addParameter(BasicLTIConstants.CONTEXT_ID, contextId);
            if (returnScore) {
                // if we are not returning a score, this parameter will be null and the provider
                // cannot send any results back.
                if (logger.isDebugEnabled()) {
                    logger.debug("Sending the LTI provider the response URL as: " + DomainModuleProperties.getInstance().getLtiConsumerServletUrl());
                }
                om.addParameter(BasicLTIConstants.LIS_OUTCOME_SERVICE_URL, DomainModuleProperties.getInstance().getLtiConsumerServletUrl());
            }

            /* Escape all components to the sourcedid */
            contextId = escapeLtiSourceComponent(contextId, LTI_SOURCE_DELIM);
            resourceLinkId = escapeLtiSourceComponent(resourceLinkId, LTI_SOURCE_DELIM);
            userId = escapeLtiSourceComponent(userId, LTI_SOURCE_DELIM);

            om.addParameter(BasicLTIConstants.LIS_RESULT_SOURCEDID,
                    contextId + LTI_SOURCE_DELIM + resourceLinkId + LTI_SOURCE_DELIM + userId);

            // add the optional custom parameters to the OAuth message
            if (customParameters != null && !customParameters.getNvpair().isEmpty()) {
                standardizeCustomParameters(customParameters);
                for (Nvpair keyValuePair : customParameters.getNvpair()) {
                    om.addParameter(keyValuePair.getName(), keyValuePair.getValue());
                }
            }

            // create the OAuth Consumer
            OAuthConsumer oc = new OAuthConsumer(null, provider.getKey(), provider.getSharedSecret(), null);
            try {
                // sign the OAuth message
                OAuthSignatureMethod osm = OAuthSignatureMethod.newMethod(OAuth.HMAC_SHA1, new OAuthAccessor(oc));
                osm.sign(om);

                // get the encrypted URL to pass back
                protectedUrl = OAuth.addParameters(om.URL, om.getParameters());
            } catch (Exception e) {
                throw new DetailedException("LTI failed to create a signature.",
                        "An exception occurred when OAuth attempted to create a secure signature.", e);
            }
        }

        return protectedUrl;
    }

    /**
     * Ensures that the delimiter is escaped if present. To further ensure
     * string safety, all backslashes are escaped as well.
     *
     * @param value the string to escape.
     * @param delimiter the delimiter that needs to be escaped if it exists
     *        withing the value string.
     * @return the escaped string. This will return the same string if it
     *         contained no backslashes or the delimiter.
     */
    private String escapeLtiSourceComponent(String value, String delimiter) {
        // regex is used which is why there are so many backslashes
        return value.replaceAll("\\\\", "\\\\\\\\").replaceAll(delimiter, "\\\\" + delimiter);
    }

    /**
     * Searches the known sources for the user id.
     *
     * @return the user id.
     */
    private String getUserId() {
        String userId = null;

        // case 1: GIFT course run by a GIFT user
        if (domainSessionInfo.getUsername() != null) {
            userId = domainSessionInfo.getUsername();
        }
        // case 2: GIFT course run by an anonymous experiment user
        else if (domainSessionInfo.getExperimentId() != null) {
            userId = domainSessionInfo.getExperimentId() + ":" + domainSessionInfo.getUserId();
        }
        // case 3: GIFT course run as an LTI provider
        else if (domainSessionInfo.getSessionDetails() != null && domainSessionInfo.getSessionDetails() instanceof LtiUserSessionDetails) {
            LtiUserSessionDetails ltiSessionDetails = (LtiUserSessionDetails) domainSessionInfo.getSessionDetails();
            if (ltiSessionDetails.getLtiUserId() != null && ltiSessionDetails.getLtiUserId().getConsumerKey() != null
                    && ltiSessionDetails.getLtiUserId().getConsumerId() != null) {
                userId = ltiSessionDetails.getLtiUserId().getConsumerKey() + ":" + ltiSessionDetails.getLtiUserId().getConsumerId();
            }
        }

        if (userId == null) {
            throw new DetailedException("Could not find user id",
                    "Could not find a user id for this session while trying to build the OAuth LTI URL.", new NullPointerException());
        }

        return userId;
    }

    /**
     * Retrieves the course LTI provider using the specified provider id from the list of LTI
     * providers in the course properties.
     *
     * @param providerId the provider identifier.
     * @return the course LTI provider. Can return null if the providerId is null or cannot be found
     *         in the course properties.
     */
    private LtiProvider findCourseLtiProvider(String providerId) {
        LtiProvider provider = null;
        if (providerId != null) {
            for (LtiProvider courseProvider : courseManager.getCourseLtiProviders()) {
                if (providerId.equalsIgnoreCase(courseProvider.getIdentifier())) {
                    provider = courseProvider;
                    break;
                }
            }
        }

        return provider;
    }

    /**
     * Retrieves the current course object's LTI provider using the LTI provider id specified by the
     * author. The LTI provider is retrieved from the list of LTI providers in the course
     * properties.
     *
     * @return the course LTI provider. Can return null if the current course object is no longer an
     *         LTI course object, if the LtiProperties cannot be found within the current course
     *         object, or if the LTI provider identifier doesn't match the list of LTI providers in
     *         the course properties.
     */
    public LtiProvider findCurrentCourseObjectLtiProvider() {
        LtiProvider provider = null;

        if (currentLessonMaterialHandler != null) {
            LessonMaterialList lmList = currentLessonMaterialHandler.getLessonMaterial();
            if (lmList != null && lmList.getMedia() != null) {
                for (Media media : lmList.getMedia()) {
                    if (media.getMediaTypeProperties() != null && media.getMediaTypeProperties() instanceof LtiProperties) {
                        provider = findCourseLtiProvider(((LtiProperties) media.getMediaTypeProperties()).getLtiIdentifier());
                        break;
                    }
                }
            }
        }

        return provider;
    }

    /**
     * Process the LTI consumer's score from the LTI provider response. Checks the course object's
     * slider bar values to determine if the user is Novice, Journeyman, or Expert.
     *
     * @param score the score to process.
     */
    public void processLtiConsumerScoreResponse(double score) {

        // Make sure current course object is still the LTI lesson material.
        if (currentLessonMaterialHandler != null) {
            LessonMaterialList lmList = currentLessonMaterialHandler.getLessonMaterial();
            if (lmList != null && lmList.getMedia() != null) {
                for (Media media : lmList.getMedia()) {
                    if (media.getMediaTypeProperties() != null && media.getMediaTypeProperties() instanceof LtiProperties) {
                        LtiProperties ltiProperties = (LtiProperties) media.getMediaTypeProperties();

                        buildAndSendPerformanceAssessment(ltiProperties, score);

                        // there will only be 1 LTI media, so exit method after we send the
                        // performance assessment.
                        return;
                    }
                }
            }
        }
    }

    /**
     * Standardizes the custom parameter names to follow LTI standards.
     *
     * @param customParameters the custom parameter string
     */
    private void standardizeCustomParameters(CustomParameters customParameters) {

        if (customParameters != null && !customParameters.getNvpair().isEmpty()) {

            for (Nvpair keyValuePair : customParameters.getNvpair()) {
                // convert keyname to LTI standard
                // 1. prepend keyname with "custom_"
                // 2. trim off excess whitespace (e.g. if they did key = value instead of key=value)
                // 3. replace all non-alphanumeric values with an underscore
                // 4. make lower case
                StringBuilder standardizedKeyname = new StringBuilder("custom_")
                        .append(keyValuePair.getName().trim().replaceAll("[^A-Za-z0-9]", "_").toLowerCase());
                keyValuePair.setName(standardizedKeyname.toString());
            }
        }
    }

    /**
     * Handle a pedagogical request for this domain.
     *
     * @param request - the ped request message. Can't be null.
     */
    public void handlePedagogicalRequest(PedagogicalRequest request) {

        //TODO: make sure both marco and micro handlers don't implement the same strategy during this method call

        if (domainKnowledgeManager != null) {
            //handle micro adaptation first

            domainKnowledgeManager.handlePedagogicalRequest(request);
        }

        //handle macro adaptation second
        courseManager.handlePedagogicalRequest(request);
    }

    /**
     * Executes a single strategy activity.
     *
     * @param activity The activity to execute. Can't be null.
     * @throws Exception If there was a problem while executing the activity.
     */
    public void executeActivity(AbstractStrategy activity) throws Exception {
        if (activity == null) {
            throw new IllegalArgumentException("The parameter 'activity' cannot be null.");
        }

        if(domainKnowledgeManager != null) {
            domainKnowledgeManager.executeActivity(activity);
        }
    }

    /**
     * Resets the knowledge session that is currently being run by this domain session, if any.
     */
	public void resetCurrentKnowledgeSession() {
		
		if(this.cachedTrainingAppTransition == null) {
			return;
		}
		
		LessonCompletedStatusType type = LessonCompletedStatusType.INSTRUCTIONAL_STRATEGY_REQUEST;

		//the learner has manually stopped the scenario from the Tutor, so we need to terminate it in their domain session
		terminateDomainKnowledgeSession("The author has triggered a reset of the real-time assessment part of this course.", type);

		if(getDomainSessionInfo().isLearnerConnected()){
		    courseManager.reportProgress(true);
		}
		
		List<CourseObjectWrapper> courseObjects = new ArrayList<>();
		courseObjects.add(this.cachedTrainingAppTransition);
		courseManager.insertTransitions(courseObjects);
	}

    /**
     * Handle the training app game state message
     *
     * @param message The training app game state message received
     */
    public void handleTrainingAppGameStateMessage(Message message) {

        boolean isValidLessonState = currentLessonState == LessonStateEnum.RUNNING ||
                currentLessonState == LessonStateEnum.PAUSED;

        //below here is the legacy implementation of this method
        if (domainKnowledgeManager != null && isValidLessonState) {

            PerformanceAssessment perfAss = domainKnowledgeManager.handleTrainingAppGameStateMessage(message);

            if (perfAss != null && logger.isDebugEnabled()) {
                logger.debug("Creating performance assessment because the game state message caused a change: " + message);
            }

            buildAndSendPerformanceAssessment(perfAss);
        }

        if (message.getMessageType() == MessageTypeEnum.ENTITY_STATE) {

            if(entityTable != null) {

                entityTable.update(message);
            }
        }

        if(message.getMessageType() == MessageTypeEnum.LEARNER_TUTOR_ACTION){
            // handle special learner tutor actions
            LearnerTutorAction action = (LearnerTutorAction)message.getPayload();

            if(action.getAction() instanceof TutorMeLearnerTutorAction){
                //initiate conversation
                startLearnerActionConversation((TutorMeLearnerTutorAction)action.getAction());

            } else if(action.getAction() instanceof FinishScenario) {
                
                LessonCompletedStatusType type;
                if(message.getSenderModuleType().equals(ModuleTypeEnum.TUTOR_MODULE)){
                    // learner is using the TUI feedback widget to go to the next course object
                    type = LessonCompletedStatusType.LEARNER_ENDED_OBJECT;
                }else{
                    // Game Master or RTA controller is issuing to end the lesson
                    type = LessonCompletedStatusType.CONTROLLER_ENDED_LESSON;
                }

                //the learner has manually stopped the scenario from the Tutor, so we need to terminate it in their domain session
                terminateDomainKnowledgeSession("The learner has manually finished the real-time assessment part of this course.", type);

                if(getDomainSessionInfo().isLearnerConnected()){
                    courseManager.reportProgress(true);
                }
            }else if(action.getAction() instanceof ApplyStrategyLearnerAction){
                // apply the referenced instructional strategy
                ApplyStrategyLearnerAction applyStrategyLearnerAction = (ApplyStrategyLearnerAction)action.getAction();
                generated.dkf.LearnerAction learnerAction = applyStrategyLearnerAction.getLearnerAction();
                if(learnerAction != null && learnerAction.getLearnerActionParams() != null){
                    Serializable actionParams = learnerAction.getLearnerActionParams();
                    if(actionParams instanceof generated.dkf.LearnerAction.StrategyReference){
                        generated.dkf.LearnerAction.StrategyReference strategyRef = (generated.dkf.LearnerAction.StrategyReference)actionParams;
                        handleApplyStrategy(strategyRef.getName());
            }
                }

            }

        }else if(message.getMessageType() == MessageTypeEnum.CHAT_LOG){
            //update chat log with possible conversation input provided by learner
            updateConversation((ChatLog) message.getPayload());

        }else if(message.getMessageType() == MessageTypeEnum.TRAINING_APP_SURVEY_RESPONSE){

            handleTrainingApplicationSurveyResponse((SurveyResponse) message.getPayload());

        }else if(message.getMessageType() == MessageTypeEnum.TRAINING_APP_SURVEY_SUBMIT){

            handleTrainingApplicationSurveySubmit();
        }else if(message.getMessageType() == MessageTypeEnum.LOAD_PROGRESS){

            handleLoadProgress((GenericJSONState)message.getPayload());
        }

    }

    /**
     * Terminate the domain knowledge manager (if not null).  If this domain session is the host
     * of a team knowledge session than all joiners will be notified as well.
     *
     * @param reason The reason for terminating the domain knowledge. Used for logging purposes. Can't be null or empty.
     * @param status information about why the session is ending.
     */
    public void terminateDomainKnowledgeSession(String reason, LessonCompletedStatusType status){

        if(domainKnowledgeManager != null){
            domainKnowledgeManager.terminate(reason, status);
            
            // for joiners the Scenario instance will never be initialized, therefore the scenarioCompleted method would not be called
            // and the next course object for the joiner would never be shown.  Therefore need to manually call the method here.
            if(KnowledgeSessionManager.getInstance().isMemberOfTeamKnowledgeSession(getDomainSessionInfo().getDomainSessionId()) != null){
                scenarioCompleted(new LessonCompleted(status));
            }
        }

    }

    /**
     * Handle the request containing the metrics data to update the task or concept with.
     *
     * @param request the request containing the updated metrics for a specific task or concept.
     */
    public void handleEvaluatorUpdateRequest(EvaluatorUpdateRequest request) {
        if (domainKnowledgeManager == null) {
            if (logger.isInfoEnabled()) {
                logger.info("domainKnowledgeManager is null; not proccessing the EvaluatorUpdateRequest");
            }
            return;
        }

        domainKnowledgeManager.handleEvaluatorUpdateRequest(request);
    }

    /**
     * Forward the load progress information to the tutor to display to the learner.
     *
     * @param loadProgress contains progress information about loading content into a training application
     */
    private void handleLoadProgress(GenericJSONState loadProgress){

        if(loadProgress != null && forwardContentLoadingUpdates){

            //forward to tutor
            DomainModule.getInstance().sendLoadProgressToTutor(getDomainSessionInfo(), getDomainSessionInfo().getDomainSessionId(), loadProgress);
        }
    }

    /**
     * Use the chat log entries to update the associated conversation if needed.
     *
     * @param chatLog contains the last users entry in a chat
     */
    private void updateConversation(ChatLog chatLog){

        String learnerText = chatLog.getLastUserEntry();
        if(learnerText != null && !learnerText.isEmpty()) {

            if(!usingLegacyAutoTutorHandler){
                //the legacy autotutor conversation handling logic doesn't use the conversation manager yet.
                courseManager.getConversationManager().addUserResponse(chatLog);
            }
        }
    }

    /**
     * Starts the learner action drive conversation by displaying the conversation window and then the first
     * part of the conversation.
     *
     * @param tutorMeLearnerAction the learner action associated with a 'tutor me' conversation which the learner wishes to start.
     * @throws DetailedException if there is a problem parsing the conversation file associated with the learner action
     */
    private void startLearnerActionConversation(TutorMeLearnerTutorAction tutorMeLearnerAction) throws DetailedException{

        LearnerActionsManager actionsMgr = domainKnowledgeManager.getAssessmentKnowledge().getScenario().getResources().getLearnerActionsManager();
        generated.dkf.LearnerAction learnerAction = tutorMeLearnerAction.getLearnerAction();
        String tutorMeActionName = learnerAction != null ? learnerAction.getDisplayName() : null;
        generated.dkf.LearnerAction tutorMeAction = actionsMgr.getTutorMeActionByName(tutorMeActionName);
        Serializable actionParams = tutorMeAction.getLearnerActionParams();
        if(actionParams instanceof TutorMeParams){
            startConversation(((TutorMeParams)actionParams).getConfiguration(), tutorMeAction.getDisplayName(), false,
                    tutorMeAction.getDescription(), domainKnowledgeManager, false, null);
        }
    }

    /**
     * Starts the conversation by displaying the conversation window and then the first part of the conversation.
     *
     * @param conversationConfig contains a reference to the conversation to start
     * {@link generated.dkf.AutoTutorSKO}, {@link generated.course.AutoTutorSKO}, {@link generated.course.ConversationTreeFile},
     * {@link generated.dkf.ConversationTreeFile}
     * @param conversationName the name of the conversation which can be shown to the learner.  If null, another value will be used
     * @param isFullscreen whether the conversation should be presented in fullscreen mode
     * depending on the type of conversation configuration:
     * - Conversation File = the name field in the file
     * - AutoTutor script = empty string
     * @param conversationDescription an optional description to show to the learner in the TUI.  If not null, this will override the
     * description in the conversation file as the description shown to the learner.
     * @param terminateSessionOnFailure whether to terminate the domain session if the conversation UI failed to be displayed.  If false an
     * attempt will be made to log the error.
     * @throws DetailedException if there is a problem parsing the conversation file associated with the learner action
     */
    private void startConversation(Serializable conversationConfig, String conversationName, boolean isFullscreen, String conversationDescription,
            ConversationAssessmentHandlerInterface conversationAssessmentHandler, boolean terminateSessionOnFailure, AsyncActionCallback callback)
            throws DetailedException{

        usingLegacyAutoTutorHandler = false;

        if(conversationConfig instanceof generated.dkf.AutoTutorSKO ||
                conversationConfig instanceof generated.course.AutoTutorSKO){

            //
            // initiate conversation UI
            //

            conversationName = conversationName != null ? conversationName : Constants.EMPTY;

            DisplayChatWindowRequest initRequest = new DisplayChatWindowRequest(new DisplayAvatarAction());
            initRequest.setProvideBypass(DomainModuleProperties.getInstance().shouldBypassChatWindows());
            initRequest.setChatName(conversationName);
            initRequest.setFullscreen(isFullscreen);

            if(conversationDescription != null){
                initRequest.setDescription(conversationDescription);
            }

            MessageCollectionCallback displayChatWindowCallback = new ConversationEndedCallback(initRequest, terminateSessionOnFailure, callback);

            sendDisplayChatWindowRequest(initRequest, displayChatWindowCallback);

            if(conversationConfig instanceof generated.dkf.AutoTutorSKO){

                generated.dkf.AutoTutorSKO skoRef = (generated.dkf.AutoTutorSKO)conversationConfig;
                if(logger.isInfoEnabled()) {
                    logger.info("Starting conversation named '"+conversationName+"' w/ unique chat id of "+initRequest.getChatId()+" based on DKF reference to AutoTutor SKO");
                }

                courseManager.getConversationManager().startAutoTutor(initRequest.getChatId(), skoRef, courseManager.getCourseRuntimeDirectory(), conversationAssessmentHandler, this);

            }else if(conversationConfig instanceof generated.course.AutoTutorSKO){

                generated.course.AutoTutorSKO skoRef = (generated.course.AutoTutorSKO)conversationConfig;
                if(logger.isInfoEnabled()) {
                    logger.info("Starting conversation named '"+conversationName+"' w/ unique chat id of "+initRequest.getChatId()+" based on course reference to AutoTutor SKO");
                }

                courseManager.getConversationManager().startAutoTutor(initRequest.getChatId(), skoRef, courseManager.getCourseRuntimeDirectory(), conversationAssessmentHandler, this);
            }


        }else if(conversationConfig instanceof generated.dkf.ConversationTreeFile ||
                conversationConfig instanceof generated.course.ConversationTreeFile){
            //a conversation instance

            String conversationFileName;
            if(conversationConfig instanceof generated.dkf.ConversationTreeFile){

                generated.dkf.ConversationTreeFile conversationTreeFile = (generated.dkf.ConversationTreeFile)conversationConfig;
                conversationFileName = conversationTreeFile.getName();

            }else if(conversationConfig instanceof generated.course.ConversationTreeFile){

                generated.course.ConversationTreeFile conversationTreeFile = (generated.course.ConversationTreeFile)conversationConfig;
                conversationFileName = conversationTreeFile.getName();

            }else{
                //ERROR
                throw new DetailedException("Unable to start conversation named "+conversationName+" because the conversation tree type is not handled.",
                        "The conversation tree configuration of "+conversationConfig+" referenced in a course (or a DKF referenced by the course) is not handled.",
                        null);
            }

            //retrieve conversation file
            generated.conversation.Conversation conversation;
            try{
                FileProxy conversationFile = courseManager.getCourseRuntimeDirectory().getRelativeFile(conversationFileName);
                ConversationTreeFileHandler handler = new ConversationTreeFileHandler(conversationFile, true);
                GIFTValidationResults validationResults = handler.checkConversation();
                if(validationResults.hasCriticalIssue() || validationResults.hasImportantIssues()){
                    throw validationResults.getFirstError();
                }
                conversation = handler.getConversation();
                conversationDescription = conversationDescription == null ?
                        conversation.getLearnersDescription() : conversationDescription;
                conversationName = conversationName == null ?
                        conversation.getName() : conversationName;
            }catch(IOException e){
                throw new DetailedException("Failed to parse the conversation file.", "There was a problem with the conversation file '"+conversationFileName+"'.  The message reads:\n"+e.getMessage(), e);
            }catch(FileValidationException e){
                throw new DetailedException("Failed to parse and validate the conversation file.", "There was a problem when parsing and validating the conversation file '"+conversationFileName+"'.  The message reads:\n"+e.getMessage(), e);
            }catch(DetailedException e){
                throw e;
            }catch(Throwable t){
                throw new DetailedException("Failed to parse and validate the conversation file.", "There was a problem when parsing and validating the conversation file '"+conversationFileName+"'.  The message reads:\n"+t.getMessage(), t);
            }

            //
            // initiate conversation UI
            //

            DisplayChatWindowRequest initRequest = new DisplayChatWindowRequest(new DisplayAvatarAction());
            initRequest.setProvideBypass(DomainModuleProperties.getInstance().shouldBypassChatWindows());
                initRequest.setChatName(conversationName);
                initRequest.setDescription(conversationDescription);
                initRequest.setFullscreen(isFullscreen);

            MessageCollectionCallback displayChatWindowCallback = new ConversationEndedCallback(initRequest, terminateSessionOnFailure, callback);

            sendDisplayChatWindowRequest(initRequest, displayChatWindowCallback);

            //
            // Display initial conversation node(s)
            //

            if(logger.isInfoEnabled()) {
            logger.info("Starting conversation named '"+conversationName+"' w/ unique chat id of "+initRequest.getChatId()+" based on conversation tree from file '"+conversationFileName+"'");
            }

            courseManager.getConversationManager().startConversationTree(initRequest.getChatId(), conversation, conversationAssessmentHandler, this);

        }else{
            throw new DetailedException("Failed to start conversation.", "Found unhandled conversation type of "+conversationConfig+" for conversation named '"+conversationName+"'.", null);
        }
    }

    @Override
    public void handleDeadReckonedEntityMessage(Message message) {

        boolean isValidLessonState = currentLessonState == LessonStateEnum.RUNNING ||
                currentLessonState == LessonStateEnum.PAUSED;

        if (domainKnowledgeManager != null && isValidLessonState) {

            PerformanceAssessment perfAss = domainKnowledgeManager.handleTrainingAppGameStateMessage(message);
            buildAndSendPerformanceAssessment(perfAss);
        }

    }

    private class ConversationEndedCallback implements MessageCollectionCallback{

        boolean terminateSessionOnFailure;
        DisplayChatWindowRequest initRequest;
        AsyncActionCallback callback;

        public ConversationEndedCallback(DisplayChatWindowRequest initRequest, boolean terminateSessionOnFailure, AsyncActionCallback callback){

            this.initRequest = initRequest;
            this.terminateSessionOnFailure = terminateSessionOnFailure;
            this.callback = callback;
        }

        @Override
        public void success() {
            if(logger.isInfoEnabled()) {
                logger.info("The chat is over for chat id "+initRequest.getChatId()+" because the tutor module has replied to the original display chat window request");
            }

            if(callback != null){
                callback.onSuccess();
            }
        }

        @Override
        public void received(Message msg) {
            //nothing to do
        }

        @Override
        public void failure(Message msg) {

            if(LOCAL_SESSION_STATE.CLOSED == localSessionState){
                //the failure is part of closing the domain session
                return;
            }

            logger.error("Failed to display chat window for chat "+initRequest.getChatId()+": " + msg);
            if(terminateSessionOnFailure){
                terminateSession(true, "Failed to start a conversation tree" ,
                        "The Tutor failed to present the conversation tree to the learner because '"+msg+"'.");
            }
        }

        @Override
        public void failure(String why) {

            if(LOCAL_SESSION_STATE.CLOSED == localSessionState){
                //the failure is part of closing the domain session
                return;
            }

            logger.error("Failed to display chat window for chat "+initRequest.getChatId()+": " + why);

            if(terminateSessionOnFailure){
                terminateSession(true, "Failed to start a conversation tree" ,
                        "The Tutor failed to present the conversation tree to the learner because '"+why+"'.");
            }
        }
    }

    /**
     * Build a performance assessment message from the LTI properties and send it over the network.
     *
     * @param properties the LTI properties
     * @param score the score used to update the concept(s)
     */
    private void buildAndSendPerformanceAssessment(LtiProperties properties, double score) {
        // cannot have null properties
        if (properties == null) {
            return;
        }

        BigInteger minimum = properties.getSliderMinValue();
        BigInteger maximum = properties.getSliderMaxValue();

        AssessmentLevelEnum assessmentLevel;
        int percent = (int) Math.floor(score * 100);
        if (percent >= maximum.intValue()) {
            assessmentLevel = AssessmentLevelEnum.ABOVE_EXPECTATION;
        } else if (percent >= minimum.intValue()) {
            assessmentLevel = AssessmentLevelEnum.AT_EXPECTATION;
        } else {
            assessmentLevel = AssessmentLevelEnum.BELOW_EXPECTATION;
        }

        // SKILL
        if (BooleanEnum.FALSE.equals(properties.getIsKnowledge())) {
            // set this specific name to indicate that we always want to update the graded score
            // node and it's children because the course concepts have not been set for Skill.
            GradedScoreNode scoreNode = new GradedScoreNode(CourseConceptsUtil.COURSE_CONCEPTS_CONCEPT_NAME_SKILL);

            // build the graded score node for each concept named in the concept list of the LTI
            // course object.
            if (properties.getLtiConcepts() != null) {
                for (String ltiConcept : properties.getLtiConcepts().getConcepts()) {
                    GradedScoreNode conceptNode = new GradedScoreNode(ltiConcept, assessmentLevel);
                    scoreNode.addChild(conceptNode);

                    /* Graded scores require a raw score child leaf-node or
                     * it'll be marked as unrelated and will be ignored. */
                    RawScore rawScore = new DefaultRawScore(Double.toString(score), "LTI score");
                    RawScoreNode rawScoreNode = new RawScoreNode(ltiConcept, rawScore, assessmentLevel);
                    conceptNode.addChild(rawScoreNode);
                }
            }

            buildAndSendLessonGradedScoreRequest(scoreNode);
        }
        // KNOWLEDGE
        else {
            Set<String> courseConceptList = new HashSet<>();
            if (properties.getLtiConcepts() != null) {
                /* add all LTI concept assessments */
                courseConceptList.addAll(properties.getLtiConcepts().getConcepts());
            }

            PerformanceAssessment performanceAssessment = courseManager.assessPerformanceFromLTI(courseConceptList,
                    assessmentLevel);
            if (performanceAssessment != null) {
                buildAndSendPerformanceAssessment(performanceAssessment);
            }
        }
    }

    /**
     * Build a lesson graded score request message from the graded score node and send it over the
     * network.
     *
     * @param gradedScoreNode the graded score node data
     */
    private void buildAndSendLessonGradedScoreRequest(GradedScoreNode gradedScoreNode) {

        if (gradedScoreNode != null) {

            if (logger.isDebugEnabled()) {

                logger.debug("Domain Knowledge manager created a graded score node to send out: " + gradedScoreNode);
            }

            DomainModule.getInstance().sendGradedScoreNode(gradedScoreNode, courseManager.getCourseConceptsFlatList(), getDomainSessionInfo());
        }
    }

    /**
     * Build a performance assessment message from the performance assessment and send it over the
     * network.
     *
     * @param performanceAssessment - a performance assessment containing assessment data to send
     *            over the network.
     */
    private void buildAndSendPerformanceAssessment(PerformanceAssessment performanceAssessment) {

        if (performanceAssessment != null) {

            if (logger.isDebugEnabled()) {

                logger.debug("Domain Knowledge manager created a performance assessment to send out: " + performanceAssessment);
            }

            DomainModule.getInstance().sendPerformanceAssessment(
                    performanceAssessment,
                    getDomainSessionInfo());
        }

    }

    /**
     * Notify the listeners that the training application state has changed
     *
     * @param state The new training application state
     */
    private void changeTrainingAppState(TrainingApplicationStateEnum state) {

        if(!TrainingApplicationStateEnum.isValidTransition(currentTrainingAppState, state)){
            if(logger.isInfoEnabled()) {
                logger.info("Ignoring training app transition from "+currentTrainingAppState+" to "+state+" because it is not a valid transition.");
            }
            return;
        }

        synchronized (trainingAppStateListeners) {

            if (state != null) {

                for (TrainingApplicationStateListener listener : trainingAppStateListeners) {

                    listener.onTrainingAppStateChanged(state);
                }

            } else {

                // The training application has closed
                // Remove the state listeners since they are no longer relevant
                trainingAppStateListeners.clear();
            }

            if(logger.isInfoEnabled()) {
            logger.info("Setting current training application state to "+state+".");
            }

            currentTrainingAppState = state;
        }
    }

    /**
     * Registers a listener to be notified of training application state changes
     *
     * @param listener The listener
     */
    public void addTrainingAppStateListener(TrainingApplicationStateListener listener) {

        synchronized (trainingAppStateListeners) {

            trainingAppStateListeners.add(listener);
        }
    }

    /**
     * Removes a listener from being notified of training application state
     * changes
     *
     * @param listener The listener
     */
    public void removeTrainingAppStateListener(TrainingApplicationStateListener listener) {

        synchronized (trainingAppStateListeners) {

            trainingAppStateListeners.remove(listener);
        }
    }

    /**
     * Notify the listeners that the lesson state has changed
     *
     * @param state The new lesson state
     */
    private void changeLessonState(LessonStateEnum state) {

        synchronized (lessonStateListeners) {

            if (state != null) {

                if(logger.isInfoEnabled()) {
                    logger.info("Notifying listeners that lesson state has changed from "+currentLessonState+" to "+state+".");
                }

                for (LessonStateListener listener : lessonStateListeners) {

                    listener.onLessonStateChanged(state);
                }

            } else {

                // The lesson has closed
                // Remove the state listeners since they are no longer relevant
                lessonStateListeners.clear();
            }

            if(logger.isInfoEnabled()) {
                logger.info("Setting current lesson state to "+state+".");
            }
            currentLessonState = state;
        }
    }

    /**
     * Registers a listener to be notified of lesson state changes
     *
     * @param listener The listener
     */
    public void addLessonStateListener(LessonStateListener listener) {

        synchronized (lessonStateListeners) {

            lessonStateListeners.add(listener);
        }
    }

    /**
     * Removes a listener from being notified of lesson state
     * changes
     *
     * @param listener The listener
     */
    public void removeLessonStateListener(LessonStateListener listener) {

        synchronized (lessonStateListeners) {

            lessonStateListeners.remove(listener);
        }
    }

    /**
     * Initializes the domain session by initializing all needed modules.
     *
     * @param callback The callback for the message's response.
     */
    public void sendInitializeDomainSession(MessageCollectionCallback callback) {

        DomainModule.getInstance().sendInitializeDomainSession(
                getDomainSessionInfo(),
                trainingApplicationTopicId,
                webClientInfo,
                callback);
    }

    /**
     * Notify the Learner module that the Learner model needs to be loaded.
     *
     * @param callback The callback for the message's response.
     * @throws Exception if there was a problem retrieving the learner configuration
     */
    public void sendInitializeLearner(MessageCollectionCallback callback) throws Exception {

        InstantiateLearnerRequest learnerRequest = new InstantiateLearnerRequest(lmsUsername);
        learnerRequest.setLearnerConfig(courseManager.getCustomLearnerConfiguration());

        // create Concepts object since it is a @XmlRootElement which is needed to marshal/unmarshal as string
        generated.course.Concepts conceptsElement = new generated.course.Concepts();
        conceptsElement.setListOrHierarchy(courseManager.getConcepts());
        learnerRequest.setCourseConcepts(conceptsElement);

        DomainModule.getInstance().sendInstantiateLearnerRequest(getDomainSessionInfo(), learnerRequest, callback);
    }

    /**
     * Notifies other modules that the Domain Session is starting.
     *
     * @param callback The callback for the message's response.
     */
    public void sendStartDomainSession(MessageCollectionCallback callback) {

        DomainModule.getInstance().sendStartDomainSession(
                getDomainSessionInfo(),
                callback);
    }

    @Override
    public void sendAuthorizeStrategiesRequest(Map<String, List<StrategyToApply>> strategies, DomainSession domainSession) {
        DomainModule.getInstance().sendAuthorizeStrategiesRequest(strategies, domainSession);
    }

    /**
     * Apply the instructional strategy with the given name.  This is a request to apply
     * this strategy without a pedagogical request  (e.g. learner action strategy).
     * @param strategyName an instructional strategy to apply.  If null, empty, not the name of an 
     * existing instructional strategy or the current state of the session is not in a real time assessment (DKF)
     * this method will do nothing.
     */
    public void handleApplyStrategy(String strategyName){
        
        if(domainKnowledgeManager != null){
            StrategySet strategySet = domainKnowledgeManager.getDomainActionKnowledge().getStrategyActivities(strategyName);
            DomainModule.getInstance().handleApplyStrategy(strategyName, strategySet, this, strategySet.getStress());
        }
    }

    /**
     * Notifies all connected modules to stop the Domain Session.
     *
     * @param callback The callback for the message's response.
     */
    public void sendCloseDomainSession(MessageCollectionCallback callback) {

        DomainModule.getInstance().sendCloseDomainSession(
                getDomainSessionInfo(),
                callback);
    }

    /**
     * Notifies all connected modules, and the tutor module, to stop the Domain
     * Session.
     *
     * @param reason optional user friendly message describing why the session
     *        is ending.
     * @param callback The callback for the message's response.
     */
    public void sendCloseDomainSessionNotToTutor(String reason, MessageCollectionCallback callback) {
        DomainModule.getInstance().sendCloseDomainSessionNotToTutor(reason, getDomainSessionInfo(), callback);
    }

    /**
     * Notifies the tutor module to stop the Domain Session.
     *
     * @param callback The callback for the message's response.
     */
    public void sendCloseDomainSessionToTutor(MessageCollectionCallback callback) {

        DomainModule.getInstance().sendCloseDomainSessionToTutor(
                getDomainSessionInfo().getDomainSessionId(),
                callback);
    }

    /**
     * Displays some feedback in the tutor
     *
     * @param text The text to display.
     * @param callback The callback for the message's response. Can be null.
     * When null the recipient won't need to send a response that the message was processed because
     * the message header will show "NeedsACK":false.
     */
    public void sendTUIFeedbackRequest(TutorUserInterfaceFeedback text, MessageCollectionCallback callback) {

        DomainModule.getInstance().sendTUIFeedbackRequest(
                getDomainSessionInfo().getDomainSessionId(),
                text,
                callback);
    }

    /**
     * Present some feedback in the training application via the Gateway module
     *
     * @param argument The authored message information including content and delivery settings.
     * @param callback The callback for the message's response.
     */
    public void sendTrainingAppFeedbackRequest(generated.dkf.Message argument, MessageCollectionCallback callback) {

        if(hasSupportingInterop(MessageTypeEnum.DISPLAY_FEEDBACK_GATEWAY_REQUEST)){

            DomainModule.getInstance().sendTrainingAppFeedbackRequest(
                    getDomainSessionInfo().getDomainSessionId(),
                    argument,
                    callback,
                    isCurrentAppEmbedded);

        }else{
            callback.failure("There are currently no enabled Gateway module interop plugins that can handle the message type "+MessageTypeEnum.DISPLAY_FEEDBACK_GATEWAY_REQUEST+" therefore the feedback '"+argument+"' will not be handled.");
        }
    }

    /**
     * Displays a message in the tutor
     *
     * @param title A title for this guidance course object. If null, it will be defaulted to "Information"
     * @param text The text for the guidance message
     * @param fullscreen True to display this guidance in fullscreen, false otherwise
     * @param displayDuration The duration to display the guidance request.
     * @param whileTrainingAppLoads True if this guidance is being presented while a training application loads, false otherwise
     * @param callback The callback for the request's response
     */
    public void sendDisplayGuidanceTextRequest(String title, String text, boolean fullscreen, int displayDuration, boolean whileTrainingAppLoads, MessageCollectionCallback callback) {

        DisplayMessageTutorRequest displayGuidanceTutorRequest = DisplayMessageTutorRequest.createTextRequest(title, text);
        displayGuidanceTutorRequest.getGuidance().setFullScreen(fullscreen ? BooleanEnum.TRUE : BooleanEnum.FALSE);
        displayGuidanceTutorRequest.setWhileTrainingAppLoads(whileTrainingAppLoads);
        displayGuidanceTutorRequest.setDisplayDuration(displayDuration);
        sendDisplayContentRequest(displayGuidanceTutorRequest, callback);
    }

    /**
     * Presents a loading content page in the tutor
     *
     * @param fullscreen True if this message should be presented in fullscreen mode
     */
    public void sendDisplayDefaultLoadingMessageRequest(boolean fullscreen) {

        String fileURL = UriUtil.makeURICompliant(CourseManager.DEFAULT_LOADING_MESSAGE_FILE);
        generated.course.Guidance guidance = new generated.course.Guidance();
        generated.course.Guidance.URL url = new generated.course.Guidance.URL();
        url.setAddress(fileURL);
        guidance.setGuidanceChoice(url);
        guidance.setTransitionName("Please Wait...");
        guidance.setFullScreen(fullscreen ? BooleanEnum.TRUE : BooleanEnum.FALSE);

        String networkURL = DomainModuleProperties.getInstance().getDomainContentServerAddress()  + Constants.FORWARD_SLASH;
        Guidance.URL urlChoice = (Guidance.URL) guidance.getGuidanceChoice();
        urlChoice.setAddress(networkURL + urlChoice.getAddress());

        AbstractDisplayContentTutorRequest request = AbstractDisplayContentTutorRequest.getRequest(guidance);
        request.setWhileTrainingAppLoads(true);
        sendDisplayContentRequest(request, null);
    }

    /**
     * Displays media content in the tutor
     *
     * @param displayContentRequest The request to send
     * @param callback The callback for the message's response
     */
    public void sendDisplayContentRequest(AbstractDisplayContentTutorRequest displayContentRequest, MessageCollectionCallback callback) {

        //don't attempt to display content in the TUI if already closed
        if(LOCAL_SESSION_STATE.CLOSED == localSessionState){

            if(logger.isDebugEnabled()){
                logger.debug("Not displaying content because the local session state is "+localSessionState);
            }

            if(callback != null){
                callback.success();
            }

            return;
        }

        if(displayContentRequest.getDisplayDuration() == 0 && displayContentRequest.isWhileTrainingAppLoads() && callback != null) {
            // The tutor will not be replying to this message
            DomainModule.getInstance().sendDisplayContentRequest(
                    getDomainSessionInfo().getDomainSessionId(),
                    displayContentRequest,
                    null);

            callback.success();

        } else {

            DomainModule.getInstance().sendDisplayContentRequest(
                    getDomainSessionInfo().getDomainSessionId(),
                    displayContentRequest,
                    callback);
        }
    }

    /**
     * Send a course state message.
     *
     * @param state - the current course state
     * @param callback The callback for the message's response.
     */
    public void sendCourseState(CourseState state, MessageCollectionCallback callback){

        DomainModule.getInstance().sendCourseState(getDomainSessionInfo().getDomainSessionId(), state, callback);
    }

    /**
     * Displays a set of survey questions in the Tutor User Interface.
     *
     * @param survey The survey with the questions to ask.
     * @param fullScreen whether to display the survey in full screen mode
     * @param surveyFinishedTutorCallback The message callback handler for the tutor when the survey has been finished.  Can't be null.
     * @param surveyAnsweredGatewayHandling whether the gateway should be notified to allow the learner to answer the question
     * natively in a training application that is currently running as part of the ongoing course.  If null the gateway will not
     * receive the survey notification and the learner can't answer the survey through the gateway.
     */
    public void sendDisplaySurveyRequest(final Survey survey, boolean fullScreen,
            MessageCollectionCallback surveyFinishedTutorCallback, boolean surveyAnsweredGatewayHandling) {

        if(!surveyAnsweredGatewayHandling || hasSupportingInterop(MessageTypeEnum.SURVEY_PRESENTED_NOTIFICATION)){

            //make sure the survey type is set (older survey's don't have this)
            SurveyTypeEnum surveyType = determineSurveyTypeEnum(survey);
            if(surveyType != null){
                survey.getProperties().setSurveyType(surveyType);
            }
            
            /* Construct a URL where files inside the course folder can be reached by survey media elements*/
            String networkURL;
            try {
                networkURL = DomainModuleProperties.getInstance().getDomainContentServerAddress() + "/";
            } catch (Exception ex) {
                logger.error("Could not get the host IP address, defaulting to 'localhost'", ex);
                networkURL =  DomainModuleProperties.getInstance().getTransferProtocol() + "localhost:" + DomainModuleProperties.getInstance().getDomainContentServerPort() + "/";
            }
            
            String domainRelativeCourseDirectory = UriUtil.makeURICompliant(runtimeCourseFolderRelativePath);
            String courseFolderUri = networkURL + domainRelativeCourseDirectory + Constants.FORWARD_SLASH;
            
            survey.applySurveyMediaHost(courseFolderUri);

            DomainModule.getInstance().sendDisplaySurveyRequest(
                    getDomainSessionInfo().getDomainSessionId(),
                    survey,
                    fullScreen,
                    surveyFinishedTutorCallback,
                    surveyAnsweredGatewayHandling);
        }
    }

    /**
     * Displays the AAR scores in the Tutor User Interface.
     *
     * @param title the authorable title of this structured review.  Can't be null or empty.
     * @param fullscreen whether or not to display the AAR in full screen mode on the tutor
     * @param events The events to display in the AAR
     * @param callback - The callback for the message's response.
     */
    public void sendDisplayAARRequest(String title, boolean fullscreen, final List<AbstractAfterActionReviewEvent> events, MessageCollectionCallback callback) {

        DomainModule.getInstance().sendDisplayAARRequest(
                getDomainSessionInfo().getDomainSessionId(),
                title,
                fullscreen,
                events,
                callback);
    }

    /**
     * Requests the LMS for course records of completed lessons
     *
     * @param publishedScores collection of identifiable information for published score located in the LMS
     * @param callback The callback for the message's response.
     */
    public void sendLMSDataRequest(final Collection<PublishLessonScoreResponse> publishedScores, MessageCollectionCallback callback) {

        DomainModule.getInstance().sendLmsDataRequest(
                getDomainSessionInfo().getDomainSessionId(),
                lmsUsername,
                publishedScores,
                callback);
    }

    /**
     * Return whether the current state of the course has gateway interop connections that can handle
     * the message type specified.
     *
     * @param messageType the message type to check for support in the current gateway instantiation
     * @return true if the gateway module has at least one interop connection that can handle that message type
     */
    private boolean hasSupportingInterop(MessageTypeEnum messageType){
        return currentInteropConnectionsInfo != null && currentInteropConnectionsInfo.getSupportedMessages().contains(messageType);
    }

    /**
     * Loads a scenario.
     *
     * @param simanLoad - arguments for loading a scenario (e.g. scenario name)
     * @param callback The callback for the message's response.
     */
    public void sendLoadScenario(Siman simanLoad, MessageCollectionCallback callback) {
        playbackServiceFuture.thenAccept(playbackService -> {
            if(isCurrentAppEmbedded || hasSupportingInterop(MessageTypeEnum.SIMAN)){

                logger.info("Sending load scenario with current training app state value of "+currentTrainingAppState);

                simanLoad.setRuntimeCourseFolderPath(runtimeCourseFolderRelativePath);

                //preprocessLoadArgs currently only applies to PPT load args
                //so it can be skipped when the training app is an embedded
                //application
                if(!isCurrentAppEmbedded) {
                    preprocessLoadArgs(simanLoad);
                }

                DomainModule.getInstance().sendLoadScenario(
                        getDomainSessionInfo().getDomainSessionId(),
                        simanLoad,
                        callback,
                        isCurrentAppEmbedded);

            } else if(playbackService != null && logPlaybackServiceTopic != null) {

                //if a knowledge session is being played back from a log, assume all Gateway interactions are handled appropriately
                callback.success();

            }else{
                callback.failure("There are currently no enabled Gateway module interop plugins that can handle the message type "+MessageTypeEnum.SIMAN);
            }
        });
    }

    /**
     * Adds additional attributes to the load SIMAN request that is going to the gateway module.  Any added attributes
     * are information that is not authorable directly but can be useful for displaying to the user while loading occurs.
     *
     * @param simanLoad the SIMAN load request
     */
    private void preprocessLoadArgs(Siman simanLoad){

        //currently just looking for PowerPoint file to set file size arg
        for(Serializable input : simanLoad.getLoadArgs().values()){

            if(input instanceof generated.course.InteropInputs
                    && ((InteropInputs)input).getInteropInput() instanceof generated.course.PowerPointInteropInputs){

                generated.course.PowerPointInteropInputs ppInput =
                        (generated.course.PowerPointInteropInputs) ((generated.course.InteropInputs)input).getInteropInput();
                String filename = ppInput.getLoadArgs().getShowFile();
                File file = new File(DomainModuleProperties.getInstance().getDomainDirectory() + File.separator + simanLoad.getRuntimeCourseFolderPath() + File.separator + filename);
                simanLoad.setFileSize(file.length());
            }
        }
    }

    /**
     * Starts a scenario.
     *
     * @param callback The callback for the message's response.
     */
    public void sendStartScenario(MessageCollectionCallback callback) {
        playbackServiceFuture.thenAccept(playbackService -> {
            if(isCurrentAppEmbedded || hasSupportingInterop(MessageTypeEnum.SIMAN)){

                logger.info("Sending start scenario with current training app state value of "+currentTrainingAppState);

                DomainModule.getInstance().sendStartScenario(
                        getDomainSessionInfo().getDomainSessionId(),
                        callback,
                        isCurrentAppEmbedded);

            }else if(getDomainSessionInfo().isHostGatewayAllowed() && !getDomainSessionInfo().isGatewayConnected() &&
                    !KnowledgeSessionManager.getInstance().isHostOfTeamKnowledgeSession(getDomainSessionInfo().getDomainSessionId())){
                // When using gateway training applications (e.g. VBS), only the host needs the gateway module to be allocated
                // Therefore other learners who are joiners can by-pass trying to communicate with the gateway module.
                callback.success();

            } else if(playbackService != null && logPlaybackServiceTopic != null) {

                //if a knowledge session is being played back from a log, assume all Gateway interactions are handled appropriately
                callback.success();

            }else{
                callback.failure("Unable to start the external training application because there are currently no enabled Gateway module interop plugins that can handle the message type "+MessageTypeEnum.SIMAN);
            }
        });
    }

    /**
     * Pauses the active scenario.
     *
     * @param callback The callback for the message's response.
     */
    public void pauseScenario(MessageCollectionCallback callback) {

        playbackServiceFuture.thenAccept(playbackService -> {
    
        if(isCurrentAppEmbedded || hasSupportingInterop(MessageTypeEnum.SIMAN)){

            if(TrainingApplicationStateEnum.isValidTransition(currentTrainingAppState, TrainingApplicationStateEnum.PAUSED)){

                logger.info("Sending pause scenario with current training app state value of "+currentTrainingAppState);

                DomainModule.getInstance().sendPauseScenario(
                        getDomainSessionInfo().getDomainSessionId(),
                        callback,
                        isCurrentAppEmbedded);

            }else{
                callback.failure("Unable to pause scenario because the current training app state is "+currentTrainingAppState+".");
            }

        }else if(getDomainSessionInfo().isHostGatewayAllowed() && !getDomainSessionInfo().isGatewayConnected() &&
                !KnowledgeSessionManager.getInstance().isHostOfTeamKnowledgeSession(getDomainSessionInfo().getDomainSessionId())){
            // When using gateway training applications (e.g. VBS), only the host needs the gateway module to be allocated
            // Therefore other learners who are joiners can by-pass trying to communicate with the gateway module.
            callback.success();

            } else if(playbackService != null && logPlaybackServiceTopic != null) {
                
                playbackService.pausePlayback();
                
                //if a knowledge session is being played back from a log, assume all Gateway interactions are handled appropriately
                callback.success();
    
        }else{
            callback.failure("Unable to pause the external training application because there are currently no enabled Gateway module interop plugins that can handle the message type "+MessageTypeEnum.SIMAN);
        }
        });
    }

    /**
     * Resumes the active scenario.
     *
     * @param callback The callback for the message's response.  This method will call the failure method of the callback
     * if changing the training application state to running is not a valid transition (and the current transition is
     * NOT stopped).
     * @return true iff the callback will be used to indicate a resume scenario message was sent OR that the failure method
     * was used on the callback.  False is used to indicate that the request to resume will not happen because the current
     * training app state is now stopped.
     */
    public boolean resumeScenario(MessageCollectionCallback callback) {

        if(isCurrentAppEmbedded || hasSupportingInterop(MessageTypeEnum.SIMAN)){

            if(TrainingApplicationStateEnum.isValidTransition(currentTrainingAppState, TrainingApplicationStateEnum.RUNNING)){

                logger.info("Sending resume scenario with current training app state value of "+currentTrainingAppState);

                DomainModule.getInstance().sendResumeScenario(
                        getDomainSessionInfo().getDomainSessionId(),
                        callback,
                        isCurrentAppEmbedded);

                return true;

            }else if(currentTrainingAppState == TrainingApplicationStateEnum.STOPPED){
                return false;
            }else{
                callback.failure("Unable to resume scenario because the current training app state is "+currentTrainingAppState+".");
                return true;
            }

        }else if(getDomainSessionInfo().isHostGatewayAllowed() && !getDomainSessionInfo().isGatewayConnected() &&
                !KnowledgeSessionManager.getInstance().isHostOfTeamKnowledgeSession(getDomainSessionInfo().getDomainSessionId())){
            // When using gateway training applications (e.g. VBS), only the host needs the gateway module to be allocated
            // Therefore other learners who are joiners can by-pass trying to communicate with the gateway module.
            callback.success();
            return true;

        } else if(logPlaybackServiceTopic != null) {
            
            playbackServiceFuture.thenAccept(playbackService -> {
                
                if(playbackService != null) {
                    playbackService.startPlayback(false);
                }
            });

            //if a knowledge session is being played back from a log, assume all Gateway interactions are handled appropriately
            callback.success();
            return true;

        }else{
            callback.failure("Unable to resume the external training application because there are currently no enabled Gateway module interop plugins that can handle the message type "+MessageTypeEnum.SIMAN);
            return true;
        }

    }

    /**
     * Stops the active scenario.
     *
     * @param callback The callback for the message's response.
     */
    public void sendStopScenario(MessageCollectionCallback callback) {
        
        // #5044 - if the 'future' was never created successfully and was completed
        // the following thenAccept call will never be started.  This doesn't prevent this thread from moving on.
        // In this case it would prevent the callback from being used.
        if(playbackServiceFuture.isDone()){
            
            if (isCurrentAppEmbedded || hasSupportingInterop(MessageTypeEnum.SIMAN)) {
                DomainModule.getInstance().sendStopScenario(getDomainSessionInfo().getDomainSessionId(),
                        callback, isCurrentAppEmbedded);
            } else {
                callback.success();
            }
            
            return;
        }
        
        playbackServiceFuture.thenAccept(playbackService -> {
            if (isCurrentAppEmbedded || hasSupportingInterop(MessageTypeEnum.SIMAN)) {

                if (TrainingApplicationStateEnum.isValidTransition(currentTrainingAppState,
                        TrainingApplicationStateEnum.STOPPED)) {

                    if (interopsEnabled) {
                        logger.info("Sending stop scenario with current training app state value of "
                                + currentTrainingAppState);

                        DomainModule.getInstance().sendStopScenario(getDomainSessionInfo().getDomainSessionId(),
                                callback, isCurrentAppEmbedded);
                    } else {
                        callback.success();
                    }
                }else{
                    callback.failure("Unable to stop scenario because the current training app state is "
                            + currentTrainingAppState + ".");
                }

            } else if (currentTrainingAppState == TrainingApplicationStateEnum.STOPPED) {
                // already in that state
                callback.success();

            } else if (!(currentCourseObject.getCourseObject() instanceof generated.course.TrainingApplication)) {
                // handles the case of a Survey course object that uses a DKF
                // which references an AutoTutor SKO to present a conversation
                // in this case there are no Gateway interop plugins being used
                callback.success();

            } else if (getDomainSessionInfo().isHostGatewayAllowed() && !getDomainSessionInfo().isGatewayConnected()
                    && !KnowledgeSessionManager.getInstance()
                            .isHostOfTeamKnowledgeSession(getDomainSessionInfo().getDomainSessionId())) {
                // When using gateway training applications (e.g. VBS), only the
                // host needs the gateway module to be allocated
                // Therefore other learners who are joiners can by-pass trying
                // to communicate with the gateway module.
                callback.success();

            } else if (playbackService != null && logPlaybackServiceTopic != null) {

                // if a knowledge session is being played back from a log,
                // assume all Gateway interactions are handled appropriately
                callback.success();

            }else{
                callback.failure(
                        "Unable to stop the external training application because there are currently no enabled Gateway module interop plugins that can handle the message type "
                                + MessageTypeEnum.SIMAN);
            }
        });
    }

    /**
     * Resets the system for another scenario.
     *
     * @param callback The callback for the message's response.
     */
    public void sendResetScenario(MessageCollectionCallback callback) {

        if(isCurrentAppEmbedded || hasSupportingInterop(MessageTypeEnum.SIMAN)){

            logger.info("Sending reset scenario with current training app state value of "+currentTrainingAppState);

            DomainModule.getInstance().sendResetScenario(
                    getDomainSessionInfo().getDomainSessionId(),
                    callback,
                    isCurrentAppEmbedded);

        }else if(getDomainSessionInfo().isHostGatewayAllowed() && !getDomainSessionInfo().isGatewayConnected() &&
                !KnowledgeSessionManager.getInstance().isHostOfTeamKnowledgeSession(getDomainSessionInfo().getDomainSessionId())){
            // When using gateway training applications (e.g. VBS), only the host needs the gateway module to be allocated
            // Therefore other learners who are joiners can by-pass trying to communicate with the gateway module.
            callback.success();

        }else{
            callback.failure("Unable to reset the external training application because there are currently no enabled Gateway module interop plugins that can handle the message type "+MessageTypeEnum.SIMAN);
        }
    }

    /**
     * Sends a message to initialize a lesson
     *
     * @param contentReference a reference to the content to be displayed that is unique to this course.  Can't be null or empty.
     * @param callback The callback for the message's response.
     */
    public void sendInitializeLesson(String contentReference, MessageCollectionCallback callback) {

        if(logger.isInfoEnabled()){
            logger.info("Sending initialize lesson with current training app state value of "+currentTrainingAppState);
        }

        DomainModule.getInstance().sendInitializeLesson(
                getDomainSessionInfo().getDomainSessionId(),
                contentReference,
                callback);
    }

    /**
     * Sends a message to initialize a Pedagogical model
     *
     * @param pedRequest - the InitializatePedagogicalModelRequest to send.
     * @param callback The callback for the message's response.
     */
    public void sendInitializePedagogicalModel(InitializePedagogicalModelRequest pedRequest, MessageCollectionCallback callback) {

        DomainModule.getInstance().sendInitializePedagogicalModel(
                getDomainSessionInfo().getDomainSessionId(),
                pedRequest,
                callback);
    }

    /**
     * Notifies that the lesson has started
     *
     * @param callback The callback for the message's response.
     */
    public void sendLessonStarted(MessageCollectionCallback callback) {

        DomainModule.getInstance().sendLessonStarted(
                getDomainSessionInfo().getDomainSessionId(),
                callback);
    }

    /**
     * Notifies that the lesson has completed
     *
     * @param status information about why the session is ending.
     * @param callback The callback for the message's response.
     */
    public void sendLessonCompleted(LessonCompleted lessonCompleted, MessageCollectionCallback callback) {

        //If the current training application is an embedded application
        //or autotutor conversation, the lesson completed message should
        //not be sent to the Gateway since it is not being used during
        //the execution
        if(getDomainSessionInfo().isGatewayConnected()) {
            DomainModule.getInstance().sendLessonCompleted(
                    getDomainSessionInfo().getDomainSessionId(),
                    lessonCompleted,
                    callback);
        } else {
            DomainModule.getInstance().sendLessonCompletedNotToGateway(
                    getDomainSessionInfo().getDomainSessionId(),
                    lessonCompleted,
                    callback);
        }
    }

    /**
     * Displays available learner actions in the Tutor User Interface.
     *
     * @param learnerActionsList The list of learner actions to display.
     * @param controls The scenario controls to make available to the tutor. Can be null.
     * @param callback The message callback handler.
     */
    public void sendDisplayLearnerActionsRequest(final List<generated.dkf.LearnerAction> learnerActionsList,
            ScenarioControls controls, MessageCollectionCallback callback) {

        DomainModule.getInstance().sendDisplayLearnerActionsRequest(getDomainSessionInfo().getDomainSessionId(),
                learnerActionsList,
                controls,
                callback);
    }

    /**
     * Displays a chat window in the tutor user interface.
     *
     * @param request The chat window information.
     * @param callback The message callback handler.
     */
    public void sendDisplayChatWindowRequest(final DisplayChatWindowRequest request, MessageCollectionCallback callback){

        DomainModule.getInstance().sendDisplayChatWindowRequest(getDomainSessionInfo().getDomainSessionId(),
                request,
                callback);
    }

    /**
     * Displays a chat window update in the tutor user interface.
     *
     * @param request  - the chat window update information
     * @param callback The message callback handler.
     */
    public void sendDisplayChatWindowUpdateRequest(final DisplayChatWindowUpdateRequest request, MessageCollectionCallback callback){

        DomainModule.getInstance().sendDisplayChatWindowUpdateRequest(getDomainSessionInfo().getDomainSessionId(),
                request,
                callback);
    }

    /**
     * Sets the environment condition of the active scenario.
     *
     * @param value The environment condition.
     * @param strategyStress an optional value of stress associated with this strategy. Can be null.
     * See ProxyTaskAssessment for ranges on stress value.
     * @param callback The callback for the message's response.
     */
    public void sendSetEnvironment(generated.dkf.EnvironmentAdaptation value, Double strategyStress, MessageCollectionCallback callback) {

        if(hasSupportingInterop(MessageTypeEnum.ENVIRONMENT_CONTROL)){
            DomainModule.getInstance().sendSetEnvironment(
                    getDomainSessionInfo().getDomainSessionId(),
                    value,
                    strategyStress,
                    callback);

        }else{
            
            if(getDomainSessionInfo().isGatewayConnected()) {
                
                /* Only show this error if this domain session is connected to the Gateway, otherwise
                 * we get false negative errors from joiners attempting to handle environment controls */
            callback.failure("There are currently no enabled Gateway module interop plugins that can handle the message type "+MessageTypeEnum.ENVIRONMENT_CONTROL);
        }
    }
    }

    /**
     * Publishes a lesson's score.
     *
     * @param courseRecord The lesson's score.
     * @param callback The callback for the message's response.
     */
    public void sendPublishScore(LMSCourseRecord courseRecord, MessageCollectionCallback callback) {

        DomainModule.getInstance().sendPublishScore(
                getDomainSessionInfo().getDomainSessionId(),
                lmsUsername,
                courseRecord,
                callback);
    }

    /**
     * Call upon the domain module to create and send the request survey message
     * to the appropriate module(s).
     *
     * @param getSurveyRequest the survey request information (i.e. survey context, gift key, ...)
     * @param callback used to handle receiving the requested survey (if successful)
     */
    private void sendGetSurveyRequest(GetSurveyRequest getSurveyRequest, MessageCollectionCallback callback) {

        DomainModule.getInstance().sendGetSurveyRequest(
                getDomainSessionInfo().getDomainSessionId(),
                getSurveyRequest,
                callback);
    }

    /**
     * Call upon the domain module to create and send the survey results message
     * to the appropriate module(s).
     *
     * @param surveyResponse - contains the responses of the survey
     * @param surveyContextId - the id of the survey context the survey is in
     * @param key - GIFT key that identifies a survey in a survey context
     * @param callback Callback for when presenting the survey is complete or a
     * failure has occurred
     */
    private void sendSurveyResults(SurveyResponse surveyResponse, int surveyContextId, String key, MessageCollectionCallback callback) {

        //Create a shallow copy of the survey response and modify the ids with the supplied values
        SurveyResponse newSurveyResponse = SurveyResponse.createShallowCopy(surveyResponse);
        newSurveyResponse.setSurveyContextId(surveyContextId);

        DomainModule.getInstance().sendSurveyResults(
                newSurveyResponse,
                key,
                getDomainSessionInfo(),
                callback);
    }

    @Override
    public void trainingApplicationRequest(TrainingAppInfoRequest infoRequest) {

        if(infoRequest instanceof LoSQuery){
            if(hasSupportingInterop(MessageTypeEnum.LOS_QUERY)){
                DomainModule.getInstance().sendLoSQuery(
                        getDomainSessionInfo(), (LoSQuery)infoRequest);
            }
        }else if(infoRequest instanceof VariablesStateRequest){
            if(hasSupportingInterop(MessageTypeEnum.VARIABLE_STATE_REQUEST)){
                DomainModule.getInstance().sendVariablesStateRequest(
                        getDomainSessionInfo(), (VariablesStateRequest)infoRequest);
            }
        }else{
            logger.warn("Received unhandled training application request from the lesson assessment of "+infoRequest);
        }
    }


    @Override
    public void handleDomainActionWithLearner(DomainAssessmentContent action) {

        if(action instanceof ActionsResponse){

            ActionsResponse response = (ActionsResponse)action;
            if(logger.isInfoEnabled()){
                logger.info("Received a request to display information to the user in the form of a "+response);
            }

            DisplayChatWindowUpdateRequest request = new DisplayChatWindowUpdateRequest(response.hasEnded(), response.getChatId());
            request.setText(response.getDisplayTextAsString());
            DisplayTextToSpeechAvatarAction avatarAction = new DisplayTextToSpeechAvatarAction(response.getSpeakAsString());
            request.setAvatarAction(avatarAction);
            request.setAllowFreeResponse(true);
            handleTutorChatUpdate(request);

        }else if(action instanceof ConversationTreeActions){

            ConversationTreeActions actions = (ConversationTreeActions)action;

            List<DisplayChatWindowUpdateRequest> requests = new ArrayList<>();

            for(ConversationTreeAction conversationAction : actions.getActions()){

                if(conversationAction.isConversationEnd()){

                    DisplayChatWindowUpdateRequest updateRequest = new DisplayChatWindowUpdateRequest(true, actions.getChatId());
                    updateRequest.setAvatarAction(new DisplayAvatarAction());
                    updateRequest.setText(conversationAction.getText());
                    requests.add(updateRequest);

                }else{

                    DisplayChatWindowUpdateRequest updateRequest = new DisplayChatWindowUpdateRequest(false, actions.getChatId());
                    DisplayTextToSpeechAvatarAction avatarAction = new DisplayTextToSpeechAvatarAction(conversationAction.getText());
                    updateRequest.setAvatarAction(avatarAction);
                    updateRequest.setText(conversationAction.getText());
                    updateRequest.setChoices(conversationAction.getChoices());

                    requests.add(updateRequest);
                }

            }//end for

            for(DisplayChatWindowUpdateRequest request : requests){
                sendDisplayChatWindowUpdateRequest(request, null);
            }

        }else if(action instanceof AssessmentStrategy){

            AssessmentStrategy assessmentStrategy = (AssessmentStrategy)action;

            if(logger.isInfoEnabled()){
                logger.info("Received a request from the domain knowledge manager to apply a strategy on the user.\n"+assessmentStrategy);
            }

            ApplyStrategies applyStrategies = new ApplyStrategies(
                    Arrays.asList(new StrategyToApply(assessmentStrategy.getStrategy(), assessmentStrategy.getStrategy().getName(), null)), 
                            assessmentStrategy.getStrategy().getName(), null);
            applyStrategies.setScenarioSupport(true);  // because this is from the scenario/task/dkf model it is a scenario support strategy
            DomainModule.getInstance().handleApplyStrategies(applyStrategies, this);

        }else if(action instanceof ApplicationMessage){

            if(logger.isInfoEnabled()){
                logger.info("Received a request from the domain knowledge manager to display information to the user in the training application in the form of a "+action);
            }
            generated.dkf.Message message = new generated.dkf.Message();
            message.setContent(((ApplicationMessage)action).getMessage());
            handleFeedbackUsingTrainingApp(message);
        }else{
            throw new IllegalArgumentException("Received unhandled information to display of "+action);
        }

    }

    @Override
    public void fatalError(String reason, String details) {

        logger.error("The domain assessment knowledge is reporting a fatal error, therefore notifying the user.\n"+
                "reason = "+reason+"\n"+
                "details = "+details);

        onSessionError(reason, details);
    }


    /**
     * This inner class handles the initialize learner module sequence for the domain module.
     * The sequence is simple in that it just tries to allocate a learner module for this domain module but
     * it is very important because the domain module will provide the allocated gateway module for the
     * learner module to use.
     *
     * @author mhoffman
     *
     */
    private class InitializeLearner{

        /** contains a error message.  Will be null if there is no error to report */
        private String errorMsg = null;

        /** used to hold onto the calling thread until the sequence ends successfully or fails at any point */
        private Object blockingObject = new Object();

        private void setErrorMessage(String errorMsg){
            this.errorMsg = errorMsg;
        }

        private void releaseHold(){

            synchronized (blockingObject) {
                blockingObject.notifyAll();
            }
        }

        /**
         * Start the learner module initialization sequence.  This method will hold onto the
         * calling thread until the sequence ends successfully or fails at any point.
         *
         * @throws ConfigurationException if there was a problem with the initialization sequence of events.
         * This exception should indicate the end of the course.
         */
        public void start() throws ConfigurationException{

            MessageCollectionCallback learnerAllocatedCallback = new MessageCollectionCallback() {

                @Override
                public void success() {

                    getDomainSessionInfo().setLearnerConnected(true);

                    releaseHold();
                }

                @Override
                public void received(Message msg) {
                  //don't care
                }

                @Override
                public void failure(String why) {
                    logger.error("There was a failure while waiting for the Domain to connect to the Gateway.  Reason: '"+why+"'.");
                    setErrorMessage("Ending the course because a problem occurred while waiting for the Domain to connect to the Gateway.");
                    releaseHold();
                }

                @Override
                public void failure(Message msg) {
                    logger.error("There was a failure while waiting for the Domain to connect to the Gateway.  Reason Message: '"+msg+"'.");
                    setErrorMessage("Ending the course because a problem occurred while waiting for the Domain to connect to the Gateway.");
                    releaseHold();
                }
            };

            DomainModule.getInstance().selectLearnerModule(getDomainSessionInfo(), learnerAllocatedCallback);

            //hold onto calling thread until the sequence ends successfully or ends in failure
            synchronized (blockingObject) {
                try {
                    blockingObject.wait();
                } catch (InterruptedException e) {
                    logger.error("Caught exception while waiting for initialize gateway sequence to complete.", e);
                    setErrorMessage("Ending the course because there was an issue while waiting for the initialize gateway sequence to complete.");
                }
            }

            if(this.errorMsg != null){
                throw new RuntimeException(errorMsg);
            }
        }
    }

    /**
     * This inner class handles the initialize sensor module sequence for the domain module.
     * The sequence is simple in that it just tries to allocate a sensor module for this domain module but
     * it is very important because the domain module will provide the allocated gateway module for the
     * sensor module to use.
     *
     * @author mhoffman
     *
     */
    private class InitializeSensor{

        /** contains a error message.  Will be null if there is no error to report */
        private String errorMsg = null;

        /** used to hold onto the calling thread until the sequence ends successfully or fails at any point */
        private Object blockingObject = new Object();

        private void setErrorMessage(String errorMsg){
            this.errorMsg = errorMsg;
        }

        private void releaseHold(){

            synchronized (blockingObject) {
                blockingObject.notifyAll();
            }
        }

        /**
         * Start the sensor module initialization sequence.  This method will hold onto the
         * calling thread until the sequence ends successfully or fails at any point.
         *
         * @throws ConfigurationException if there was a problem with the initialization sequence of events.
         * This exception should indicate the end of the course.
         */
        public void start() throws ConfigurationException{

            MessageCollectionCallback sensorAllocatedCallback = new MessageCollectionCallback() {

              private static final String HELP = "Please make sure you are using the GIFT provided URL for the tutor in your web-browser.  If your network configuration changed"
              + " and you are using a different URL such as one that you previously bookmarked, the Sensor module will not be allocated for use with your Domain module.  Please refer to the GIFT Forums and Troubleshooting documentation for additional help.";

                @Override
                public void success() {

                    if(logger.isInfoEnabled()){
                        logger.info("Sensor module is now allocated to " + getDomainSessionInfo() + ".");
                    }

                    releaseHold();
                }

                @Override
                public void received(Message msg) {
                  //don't care
                }

                @Override
                public void failure(String why) {
                    logger.error("There was a failure while waiting for the Domain to connect to the Sensor module.  Reason: '"+why+"'.");
                    setErrorMessage("Ending the course because a problem occurred while waiting for the Domain to connect to the Sensor module.\n"+HELP);
                    releaseHold();
                }

                @Override
                public void failure(Message msg) {
                    logger.error("There was a failure while waiting for the Domain to connect to the Sensor module.  Reason Message: '"+msg+"'.");
                    setErrorMessage("Ending the course because a problem occurred while waiting for the Domain to connect to the Sensor module.\n"+HELP);
                    releaseHold();
                }
            };

            DomainModule.getInstance().selectSensorModule(getDomainSessionInfo(), sensorAllocatedCallback);

            //hold onto calling thread until the sequence ends successfully or ends in failure
            synchronized (blockingObject) {
                try {
                    blockingObject.wait();
                } catch (InterruptedException e) {
                    logger.error("Caught exception while waiting for initialize sensor sequence to complete.", e);
                    setErrorMessage("Ending the course because there was an issue while waiting for the initialize sensor sequence to complete.");
                }
            }

            if(this.errorMsg != null){
                throw new RuntimeException(errorMsg);
            }
        }
    }

    /**
     * This inner class is used to help determine if this domain session can be allocated a gateway module.
     * If a gateway module can't be allocated AND this domain session requires a gateway module of its own
     * the session will terminate.  A domain session with training applications doesn't need a gateway module
     * when the training applications are considered gateway applications which means they only need one
     * domain session with a gateway module because the application (e.g. VBS) will send the world state of
     * that application to that single gateway module.  It is the job of that domain session with the gateway
     * module to be the host of the team knowledge session in order to distribute information to other sessions.
     *
     * @author mhoffman
     *
     */
    private class InitializeGateway{

        /** contains a error message.  Will be null if there is no error to report */
        private String errorMsg = null;

        /** used to hold onto the calling thread until the sequence ends successfully or fails at any point */
        private Object blockingObject = new Object();

        /** the initial Gateway module status, used during cleanup */
        private GatewayModuleStatus gwModuleStatus;

        /** the listener used to capture gateway module status information */
        private ModuleStatusListener moduleStatusListener = null;

        /**
         * Release the block on the mutex object.
         */
        private void releaseHold(){

            synchronized (blockingObject) {
                blockingObject.notifyAll();
            }
        }

        /**
         * Return the initial module status of the gateway module this domain session is communicating with.
         * The module status object contains the address of the gateway module on the message bus.
         *
         * @return can be null if the domain session hasn't been associated with a gateway module yet
         */
        public ModuleStatus getAllocatedGateway(){
            return gwModuleStatus;
        }

        public void start(){

            // When running GIFT in server mode the Gateway module should be
            // started by a Java Web Start
            // application. The domain module will allocate the Gateway
            // module in BaseDomainSession.initialize
            // method.
            // Furthermore the learner module needs to be allocated after
            // the domain and gateway modules link up
            // because the learner needs the gateway module address as well.
            if (DomainModuleProperties.getInstance().getDeploymentMode() != DeploymentModeEnum.SERVER) {

                //
                // get a gateway module client connection
                // Note: select Gateway before Learner in order for Learner
                // models to use the same Gateway topic
                // as this Domain module.
                //
                MessageCollectionCallback gatewayCallback = new MessageCollectionCallback() {

                    @Override
                    public void success() {

                        if(logger.isInfoEnabled()){
                            logger.info("Gateway module is now allocated to " + getDomainSessionInfo() + ".");
                        }

                        releaseHold();
                    }

                    @Override
                    public void received(final Message aMsg) {

                    }

                    @Override
                    public void failure(Message nackMsg) {
                        handleFailure(((NACK) nackMsg.getPayload()).getErrorMessage());
                    }

                    @Override
                    public void failure(String why) {
                        handleFailure(why);
                    }

                    /**
                     * Handle when the gateway module allocated fails
                     *
                     * @param errorMsg a reason for the failure
                     */
                    private void handleFailure(String errorMsg){

                        if(!getDomainSessionInfo().isHostGatewayAllowed()){
                            // failed to receive a positive response from the gateway module
                            errorMsg = "Failed to allocate a connection to the Gateway module.\nReason: " + errorMsg;
                        }

                        //don't need this sequence to listen for the gateway module anymore
                        ModuleStatusMonitor.getInstance().removeListener(moduleStatusListener);

                        gwModuleStatus = null;
                        releaseHold();
                    }
                };

                // create timeout logic just in case a gateway module is not found
                Timer timeoutTimer = new Timer(false);

                //used to search for the user's gateway module instance
                moduleStatusListener = new ModuleStatusListener() {

                    @Override
                    public void moduleStatusRemoved(StatusReceivedInfo status) {

                    }

                    @Override
                    public void moduleStatusChanged(long sentTime, ModuleStatus status) {

                        //check if GW module
                        if(gwModuleStatus == null && status.getModuleType() == ModuleTypeEnum.GATEWAY_MODULE){

                            if(logger.isInfoEnabled()){
                                logger.info("Gateway Module Status Added: " + status + ".  User "+domainSessionInfo.getUserId() + " is looking for user token of = " + trainingApplicationTopicId);
                            }

                            GatewayModuleStatus candidateGWModuleStatus = (GatewayModuleStatus)status;

                            // the gateway module should be on the same machine as the tutor client because it usually controls
                            // the training application using things like auto-hot key.
                            if(webClientInfo == null || webClientInfo.getClientAddress() == null ||
                                    candidateGWModuleStatus.getQueueName().contains(webClientInfo.getClientAddress()) ||
                                    candidateGWModuleStatus.getIPAddresses().contains(webClientInfo.getClientAddress())){
                                //the GW module is online!

                                if(logger.isInfoEnabled()) {
                                    logger.info("Matching user to gateway module instance for domain session " + getDomainSessionInfo() + ".");
                                }

                                DomainModule.getInstance().selectGatewayModule(getDomainSessionInfo(), candidateGWModuleStatus, gatewayCallback, true);

                                gwModuleStatus = candidateGWModuleStatus;
                                timeoutTimer.cancel();
                            }
                        }
                    }

                    @Override
                    public void moduleStatusAdded(long sentTime, final ModuleStatus status) {

                    }
                };

                //listen for module status changes, specifically looking for the user's gateway module
                ModuleStatusMonitor.getInstance().addListener(moduleStatusListener);

                timeoutTimer.schedule(new TimerTask() {

                    @Override
                    public void run() {

                        if(gwModuleStatus == null){
                            gatewayCallback.failure("Failed to find the Gateway module running on the learner's computer");
                        }
                    }
                }, 5000);

                try {
                    synchronized (blockingObject) {
                        blockingObject.wait();
                    }
                } catch (InterruptedException e) {
                    logger.error("Caught exception while waiting for Gateway module allocation response", e);
                }

                //don't need this sequence to listen for the gateway module anymore
                ModuleStatusMonitor.getInstance().removeListener(moduleStatusListener);

                if(this.errorMsg != null){
                    throw new RuntimeException(errorMsg);
                }

            }
        }
    }

    /**
     * This inner class handles the initialize gateway module sequence for the domain module.
     * The sequence involves sending status update messages to the tutor module and initialize/configure
     * messages to the gateway module.
     *
     * @author mhoffman
     *
     */
    private class InitializeRemoteGateway{

        /** contains a error message.  Will be null if there is no error to report */
        private String errorMsg = null;

        /** used to hold onto the calling thread until the sequence ends successfully or fails at any point */
        private Object blockingObject = new Object();

        /** used to generate the zip file dynamically and create a unique file for this particular domain session */
        private RemoteZipGenerator fileGenerator;

        /** the initial Gateway module status, used during cleanup */
        private GatewayModuleStatus gwModuleStatus;

        /** whether the domain session is closing and the gateway initialization is still running */
        private boolean dsClosed = false;

        private void setErrorMessage(String errorMsg){
            this.errorMsg = errorMsg;
        }

        private void releaseHold(){

            synchronized (blockingObject) {
                blockingObject.notifyAll();
            }
        }

        /**
         * Cleanup the initialize gateway logic.  This will delete the generated zip file.
         */
        public void cleanup(){

            dsClosed = true;

            if(fileGenerator != null){
                fileGenerator.cleanup();
            }

            //in case the start() is waiting for the user to finish the GW module install
            releaseHold();

            if(gwModuleStatus != null){

                //this prevents a module timeout error on this GW module queue if the user starts another session quickly
                //after terminating this one (#2061)
                ModuleStatusMonitor.getInstance().cancelModuleListening(gwModuleStatus);

                // cleanup the gateway modules message bus destinations since the possibly remote JWS gateway
                // module might not have access to the message bus JMX
                DomainModule.getInstance().removeRemoteGatewayDestinations(gwModuleStatus);
            }
        }

        /**
         * Return the initial module status of the gateway module this domain session is communicating with.
         * The module status object contains the address of the gateway module on the message bus.
         *
         * @return can be null if the domain session hasn't been associated with a gateway module yet
         */
        public ModuleStatus getAllocatedGateway(){
            return gwModuleStatus;
        }

        /**
         * Start the gateway module initialization sequence.  This method will hold onto the
         * calling thread until the sequence ends successfully or fails at any point.
         *
         * @param interops - the collection of gateway interop plugins that are needed for this course.
         * @throws ConfigurationException if there was a problem with the initialization sequence of events.
         * This exception should indicate the end of the course.
         */
        public void start(final List<String> interops) throws ConfigurationException{

            //display page with instructions for setting up JWS GW application
            String url;
            try {
                url = DomainModuleProperties.getInstance().getDomainContentServerAddress() + "/";
            } catch (Exception ex) {
                logger.error("Could not get the host IP address, defaulting to 'localhost'", ex);
                url = DomainModuleProperties.getInstance().getTransferProtocol() + "localhost:" + DomainModuleProperties.getInstance().getDomainContentServerPort() + "/";
            }

            // Generate the zip file dynamically and create a unique file for this particular domain session.
            fileGenerator = new RemoteZipGenerator(trainingApplicationTopicId);
            boolean success = fileGenerator.generateZip();
            // Generate a new zip file for this particular domain session.
            url += fileGenerator.getGeneratedFileUrl();

            if (!success) {
                // Throw a configuration exception so that we don't proceed further.
                throw new RuntimeException("Error occurred while generating the zip file.");
            }

            DisplayCourseInitInstructionsRequest initialRequest = new DisplayCourseInitInstructionsRequest();
            initialRequest.addAssetURL(url);

            //callback used to handle the user continuing the course (when the user is allowed too do so)
            final MessageCollectionCallback userReadyCallback = new MessageCollectionCallback() {

                @Override
                public void success() {
                    releaseHold();
                }

                @Override
                public void received(Message msg) {
                    //don't care
                }

                @Override
                public void failure(String why) {
                    logger.error("There was a failure while waiting for a response to the update sent to the Tutor indicating the Gateway module has been configured.  Reason: '"+why+"'.");
                    setErrorMessage("Ending the course because a problem occurred while waiting for the user to continue the course.");
                    releaseHold();
                }

                @Override
                public void failure(Message msg) {
                    logger.error("There was a failure while waiting for a response to the update sent to the Tutor indicating the Gateway module has been configured.  Failure Messagae: '"+msg+"'.");
                    setErrorMessage("Ending the course because a problem occurred while waiting for the user to continue the course.");
                    releaseHold();
                }
            };

            //callback used to handle when the gateway module has been configured for this course
            //It will notify the Tutor that the Gateway is ready.
            final MessageCollectionCallback gatewayConfiguredCallback = new MessageCollectionCallback() {

                @Override
                public void success() {

                    if(dsClosed){
                       return;
                    }

                    //notify tutor that gateway is configured
                    DisplayCourseInitInstructionsRequest gwOnlineRequest = new DisplayCourseInitInstructionsRequest();
                    gwOnlineRequest.setGatewayState(GatewayStateEnum.READY);

                    //update instructions display to user with latest status
                    DomainModule.getInstance().sendDisplayCourseInitInstructionsRequest(getDomainSessionInfo(), gwOnlineRequest, userReadyCallback);

                }

                @Override
                public void received(Message msg) {
                    //don't care
                }

                @Override
                public void failure(String why) {
                    logger.error("There was a failure while waiting for the Gateway to configure the interop connections needed for this course.  Reason: '"+why+"'.");
                    setErrorMessage("Ending the course because a problem occurred while waiting for the Gateway to configure the interop connections needed for this course.");
                    // releaseHold in this callback prevents the gateway module from closing
                    terminateSession(true, "The GIFT Gateway module was not properly configured",
                            "This normally happens when you cancel the installation of the GIFT Gateway module (provided from remote zip) or there is a configuration issue (e.g. running Mac OS on a Windows only course).\n"
                            + "In this case the Gateway module attempting to be started on your computer reported the following failure:\n"+why+".");
                }

                @Override
                public void failure(Message msg) {
                    logger.error("There was a failure while waiting for the Gateway to configure the interop connections needed for this course.  Reason Message: '"+msg+"'.");
                    setErrorMessage("Ending the course because a problem occurred while waiting for the Gateway to configure the interop connections needed for this course.");
                    // releaseHold in this callback prevents the gateway module from closing

                    if(msg.getPayload() instanceof NACK){
                        NACK nack = (NACK)msg.getPayload();
                        terminateSession(true, "The GIFT Gateway module was not properly configured", "This normally happens when you cancel the installation of the GIFT Communication Application (provided from remote zip) or there is a configuration issue.\n"
                                + "In this case the Gateway module attempting to be started on your computer reported the following issue:\n\n"+nack.getErrorMessage()+"\nCause: "+nack.getErrorHelp()+".");
                    }else{
                        terminateSession(true, "The GIFT Gateway module was not properly configured",
                                "This normally happens when you cancel the installation of the GIFT Gateway module (provided from remote zip) or there is a configuration issue (e.g. running Mac OS on a Windows only course).\n"
                            + "In this case the Gateway module attempting to be started on your computer reported the following failure:\n"+msg+".");
                    }
                }
            };

            //callback used to handle when the Tutor has displayed that the Gateway is connected
            //It will notify the Gateway of the interops to configure
            final MessageCollectionCallback gatewayConnectedCallback = new MessageCollectionCallback() {

                @Override
                public void success() {

                    if(dsClosed){
                        return;
                     }

                    //send possible interops to GW
                    DomainModule.getInstance().sendConfigureInteropConnection(getDomainSessionInfo(),
                            DomainModuleProperties.getInstance().getDomainContentServerAddress(), interops, gatewayConfiguredCallback);
                }

                @Override
                public void received(Message msg) {
                    //don't care
                }

                @Override
                public void failure(String why) {
                    logger.error("There was a failure while waiting for the Tutor to display that the Gateway is connected.  Reason: '"+why+"'.");
                    setErrorMessage("Ending the course because a problem occurred while waiting for the Tutor to display that the Gateway is connected.");
                    releaseHold();
                }

                @Override
                public void failure(Message msg) {
                    logger.error("There was a failure while waiting for the Tutor to display that the Gateway is connected.  Reason Message: '"+msg+"'.");
                    setErrorMessage("Ending the course because a problem occurred while waiting for the Tutor to display that the Gateway is connected.");
                    releaseHold();
                }
            };

            //callback used to handle when the gateway is connected to the domain module
            //It will notify the Tutor that the Gateway was connected
            final MessageCollectionCallback gatewayAllocatedCallback = new MessageCollectionCallback() {

                @Override
                public void success() {

                    if(dsClosed){
                        return;
                     }

                    if(logger.isInfoEnabled()) {
                        logger.info("Successfully allocated gateway module for "+getDomainSessionInfo()+".");
                    }

                    getDomainSessionInfo().setGatewayConnected(true);

                    DisplayCourseInitInstructionsRequest gwOnlineRequest = new DisplayCourseInitInstructionsRequest();
                    gwOnlineRequest.setGatewayState(GatewayStateEnum.CONNECTED);

                    //update instructions display to user with latest status
                    DomainModule.getInstance().sendDisplayCourseInitInstructionsRequest(getDomainSessionInfo(), gwOnlineRequest, gatewayConnectedCallback);
                }

                @Override
                public void received(Message msg) {
                  //don't care
                }

                @Override
                public void failure(String why) {
                    logger.error("There was a failure while waiting for the Domain to connect to the Gateway.  Reason: '"+why+"'.");
                    setErrorMessage("Ending the course because a problem occurred while waiting for the Domain to connect to the Gateway.");
                    releaseHold();
                }

                @Override
                public void failure(Message msg) {
                    logger.error("There was a failure while waiting for the Domain to connect to the Gateway.  Reason Message: '"+msg+"'.");
                    setErrorMessage("Ending the course because a problem occurred while waiting for the Domain to connect to the Gateway.");
                    releaseHold();
                }
            };

            //used to search for the user's gateway module instance
            final ModuleStatusListener moduleStatusListener = new ModuleStatusListener() {

                @Override
                public void moduleStatusRemoved(StatusReceivedInfo status) {

                }

                @Override
                public void moduleStatusChanged(long sentTime, ModuleStatus status) {

                }

                @Override
                public void moduleStatusAdded(long sentTime, final ModuleStatus status) {

                    if(dsClosed){
                        return;
                     }

                    //check if GW module
                    if(status.getModuleType() == ModuleTypeEnum.GATEWAY_MODULE){

                        if(logger.isInfoEnabled()){
                            logger.info("Gateway Module Status Added: " + status + ".  User "+domainSessionInfo.getUserId() + " is looking for user token of = " + trainingApplicationTopicId);
                        }

                        //check if it is for this domain session's user
                        if(status.getQueueName().contains(trainingApplicationTopicId)){
                            //the GW module is online!

                            if(logger.isInfoEnabled()) {
                                logger.info("Matching user to gateway module instance for domain session " + getDomainSessionInfo() + ".");
                            }

                            DomainModule.getInstance().selectGatewayModule(getDomainSessionInfo(), status, gatewayAllocatedCallback, true);

                            gwModuleStatus = (GatewayModuleStatus) status;
                        }
                    }
                }
            };

            MessageCollectionCallback displayCourseInitInstructions = new MessageCollectionCallback() {

                @Override
                public void success() {

                }

                @Override
                public void received(Message msg) {
                    //don't care
                }

                @Override
                public void failure(String why) {
                    logger.error("There was a failure while waiting for the Tutor to display the initialize instructions for this course.  Reason: '"+why+"'.");
                    setErrorMessage("Ending the course because a problem occurred while waiting for the Tutor to display the initialize instructions for this course.");
                    releaseHold();
                }

                @Override
                public void failure(Message msg) {
                    logger.error("There was a failure while waiting for the Tutor to display the initialize instructions for this course.  Reason Message: '"+msg+"'.");
                    setErrorMessage("Ending the course because a problem occurred while waiting for the Tutor to display the initialize instructions for this course.");
                    releaseHold();
                }
            };

            if(dsClosed){
                // in case the course was closed while going through this remote GW module
                // zip initialization sequence
                return;
            }

            //listen for module status changes, specifically looking for the user's gateway module
            ModuleStatusMonitor.getInstance().addListener(moduleStatusListener);

            DomainModule.getInstance().sendDisplayCourseInitInstructionsRequest(getDomainSessionInfo(), initialRequest, displayCourseInitInstructions);

            if(dsClosed){
                // in case the course was closed while going through this remote GW module
                // zip initialization sequence
                return;
            }

            //hold onto calling thread until the sequence ends successfully or ends in failure
            synchronized (blockingObject) {
                try {
                    blockingObject.wait();
                } catch (InterruptedException e) {
                    logger.error("Caught exception while waiting for initialize gateway sequence to complete.", e);
                    setErrorMessage("Ending the course because there was an issue while waiting for the initialize gateway sequence to complete.");
                }
            }

            //don't need this sequence to listen for the gateway module anymore
            ModuleStatusMonitor.getInstance().removeListener(moduleStatusListener);

            if(this.errorMsg != null){
                throw new RuntimeException(errorMsg);
            }
        }
    }

    /**
     * Used to wrap several parameters needed to customize the presentation and handling of
     * surveys and survey responses.
     *
     * @author mhoffman
     *
     */
    private class DisplaySurveyParamWrapper{

        /**
         * Default constructor
         */
        public DisplaySurveyParamWrapper() {
            //Supplies the default behavior in the event that the course author didn't specify one
            SimpleMandatoryBehavior alwaysTakeSurvey = new SimpleMandatoryBehavior();
            alwaysTakeSurvey.setUseExistingLearnerStateIfAvailable(false);
            mandatoryBehavior = alwaysTakeSurvey;
        }

        // whether to display the survey in full screen mode or not
        boolean fullScreen = true;

        // whether or not to display the survey results in any proceeding After Action Review
        boolean showInAAR = true;

        //whether or not to send the survey request to the gateway module as well
        // to allow any training applications a chance for the learner to answer the survey natively in that training application.
        boolean allowGatewaySurveyResponse = false;

        // whether this survey request comes in the middle of an external application course object
        boolean isMidLessonSurvey = false;

        boolean isRecallSurvey = false;

        // whether responses to the survey should be sent to the GIFT db for long term storage.
        boolean saveResponseToDb = true;

        // whether learners will be allowed to skip the survey
        Serializable mandatoryBehavior = null;
    }

    @Override
    public void handleMediaUsingTUI(generated.dkf.LessonMaterialList media) {

        if (isActive()) {

            MidLessonMediaManager mediaManager = new MidLessonMediaManager(courseManager.getCourseRuntimeDirectory(), runtimeCourseFolderRelativePath);

            mediaManager.clear();
            mediaManager.addLessonMaterialList(media);

            final generated.dkf.LessonMaterialList lessonMaterial = new  generated.dkf.LessonMaterialList();
            lessonMaterial.setIsCollection(mediaManager.getLessonMaterial().getIsCollection());
            lessonMaterial.getMedia().addAll(mediaManager.getLessonMaterial().getMedia());

            if (entityTable != null) {
                entityTable.setPaused(true);
            }

            if(logger.isInfoEnabled()){
                logger.info("Pausing scenario to display during lesson media.");
            }

            final Object lock = new Object();

            /*
             * Spawn a new thread that will request the TUI to present the given mid-lesson media and notify this
             * thread when the learner finishes viewing all of the media.
             */
            Thread mediaThread = new Thread(new Runnable() {

                @Override
                public void run() {

                    MessageCollectionCallback pauseCallback = new MessageCollectionCallback() {

                        @Override
                        public void success() {

                            try {

                                changeTrainingAppState(TrainingApplicationStateEnum.PAUSED);

                                MessageCollectionCallback callback = new MessageCollectionCallback() {

                                    @Override
                                    public void success() {

                                        try {

                                            if(lessonMaterial.getMedia().isEmpty()) {

                                                /*
                                                 * There are no more media items that need to be presented, so we need to resume the scenario and
                                                 * continue invoking the remaining strategies in the pedagogical request that requested
                                                 * the mid-lesson media, if any remain
                                                 */

                                                if(TrainingApplicationStateEnum.isValidTransition(currentTrainingAppState, TrainingApplicationStateEnum.RUNNING) &&
                                                        currentLessonState != LessonStateEnum.STOPPED){
                                                    //MH 09.12.14 - if the midlesson media causes the lesson to be completed (stopped), the resume shouldn't happen
                                                    //              For some reason the TA hasn't replied to the stop yet.

                                                    MessageCollectionCallback mCollection = new MessageCollectionCallback() {

                                                        @Override
                                                        public void success() {

                                                            synchronized(lock) {
                                                                lock.notifyAll();
                                                            }

                                                            changeTrainingAppState(TrainingApplicationStateEnum.RUNNING);

                                                            if(entityTable != null) {
                                                                entityTable.setPaused(false);
                                                            }
                                                        }

                                                        @Override
                                                        public void received(Message msg) {
                                                            //Do nothing
                                                        }

                                                        @Override
                                                        public void failure(Message msg) {

                                                            synchronized(lock) {

                                                                //the operation resulted in failure, so we need to resume the strategy handler
                                                                lock.notifyAll();
                                                            }

                                                            onSessionError(generateException("Resuming the scenario after presenting media", msg));
                                                        }

                                                        @Override
                                                        public void failure(String why) {

                                                            synchronized(lock) {

                                                                //the operation resulted in failure, so we need to resume the strategy handler
                                                                lock.notifyAll();
                                                            }

                                                            onSessionError(generateException("Resuming the scenario after presenting media", why));
                                                        }
                                                    };

                                                        if(currentTrainingAppState != TrainingApplicationStateEnum.RUNNING){

                                                            if(!resumeScenario(mCollection)){
                                                                //failed to resume the scenario, just log the event for now
                                                                if(logger.isInfoEnabled()){
                                                                    logger.info("Some event occurred to change the current training app state to "+currentTrainingAppState+", therefore the request to resume scenario after the mid-lesson media will not be applied.");
                                                                }
                                                            }

                                                        } else {

                                                            //the scenario is already running, so there's no need to resume it
                                                            mCollection.success();
                                                        }

                                                    } else {

                                                        if(logger.isInfoEnabled()) {
                                                        logger.info("Ignoring request to resume scenario while waiting for a mid-lesson survey because the current training app state is "+currentTrainingAppState+" which requires no change based on this request.");
                                                        }

                                                        synchronized(lock) {

                                                            //the learner has finished digesting the mid-lesson media content, so resume the strategy handler
                                                            lock.notifyAll();
                                                        }
                                                    }

                                                } else {

                                                    /*
                                                     * There are still more media items in the list that need to be presented, so send a request
                                                     * to display the next one in the list.
                                                     */
                                                    generated.dkf.LessonMaterialList mediaToPresent = new generated.dkf.LessonMaterialList();
                                                    mediaToPresent.setIsCollection(lessonMaterial.getIsCollection());

                                                    mediaToPresent.getMedia().add(lessonMaterial.getMedia().remove(0));

                                                    DomainModule.getInstance().sendDisplayMidLessonMediaRequest(
                                                            getDomainSessionInfo().getDomainSessionId(),
                                                            mediaToPresent,
                                                            this);
                                                }

                                            } catch(Exception e) {

                                                synchronized(lock) {

                                                    //the operation resulted in failure, so we need to resume the strategy handler
                                                    lock.notifyAll();
                                                }

                                                onSessionError(generateException(
                                                        "Iterating through mid-lesson media for the learner",
                                                        "Failed to move on after mid-lesson media content due to an error: " + e));
                                            }
                                        }

                                        @Override
                                        public void received(Message msg) {
                                            //Nothing to do
                                        }

                                        @Override
                                        public void failure(String why) {

                                            synchronized(lock) {

                                                //the operation resulted in failure, so we need to resume the strategy handler
                                                lock.notifyAll();
                                            }

                                            onSessionError(generateException("Displaying mid-lesson media to the learner", why));
                                        }

                                        @Override
                                        public void failure(Message msg) {

                                            synchronized(lock) {

                                                //the operation resulted in failure, so we need to resume the strategy handler
                                                lock.notifyAll();
                                            }

                                            onSessionError(generateException("Displaying mid-lesson media to the learner", msg));
                                        }
                                    };

                                    generated.dkf.LessonMaterialList mediaToPresent = new generated.dkf.LessonMaterialList();
                                    mediaToPresent.setIsCollection(lessonMaterial.getIsCollection());

                                    mediaToPresent.getMedia().add(lessonMaterial.getMedia().remove(0));

                                    DomainModule.getInstance().sendDisplayMidLessonMediaRequest(
                                            getDomainSessionInfo().getDomainSessionId(),
                                            mediaToPresent,
                                            callback);

                            } catch(Exception e) {

                                synchronized(lock) {

                                    //the operation resulted in failure, so we need to resume the strategy handler
                                    lock.notifyAll();
                                }

                                onSessionError(generateException(
                                        "Requesting to display mid-lesson media to the learner",
                                        "Failed to initiate displaying mid-lesson media due to an error: " + e));
                            }
                        }

                        @Override
                        public void received(Message msg) {
                            //Do nothing
                        }

                        @Override
                        public void failure(Message msg) {

                            synchronized(lock) {

                                //the operation resulted in failure, so we need to resume the strategy handler
                                lock.notifyAll();
                            }

                            onSessionError(generateException("Pausing the scenario to present media", msg));
                        }

                        @Override
                        public void failure(String why) {

                            synchronized(lock) {

                                //the operation resulted in failure, so we need to resume the strategy handler
                                lock.notifyAll();
                            }

                            onSessionError(generateException("Pausing the scenario to present media", why));
                        }
                    };

                    if (currentTrainingAppState != TrainingApplicationStateEnum.PAUSED) {

                        try {

                            // the current training application is not paused, so we need to pause it for the
                            // mid-lesson survey
                            pauseScenario(pauseCallback);

                        }catch (Exception e) {

                            synchronized(lock) {

                                //the learner has finished digesting the mid-lesson media content, so resume the strategy handler
                                lock.notifyAll();
                            }

                            onSessionError(generateException(
                                    "Requesting to pausing the scenario to present media",
                                    "Failed to initiate scenario pause due to an error: " + e));
                        }

                    } else {

                        // the current training application is already paused, so go ahead and show the
                        // mid-lesson survey
                        pauseCallback.success();
                    }
                }

            }, "Mid-lesson media strategy handler -"+(this.domainSessionInfo.getUsername() != null ? this.domainSessionInfo.getUsername() : this.domainSessionInfo.getDomainSessionId()));

            mediaThread.start();

            synchronized(lock) {

                try {

                    /*
                     * Pause the strategy handler while the learner digests the presented mid-lesson media. If we don't pause
                     * the strategy handler, then other strategies in the same pedagogical request can interrupt the mid-lesson
                     * media and cause the learner to navigate away from it.
                     */
                    lock.wait();

                } catch (InterruptedException e) {
                    logger.error("Interupted a paused strategy handler that was waiting for the learner ("+getDomainSessionInfo()+") to finish mid-lesson media.", e);
                }
            }
        }
    }
    
    
    /**
     * Collects a JSON summary of the current knowledge session that this domain session is participating in under
     * the provided host domain session, with the intent to share this information with a strategy provider outside
     * of GIFT. This summary will include session metadata, team membership, the current performance state, and 
     * details about certain condition assessments.
     * 
     * @param hostSession the host of the domain session 
     * @return a JSON object summarizing the details of the current knowledge session session, if any. If
     */
    @SuppressWarnings("unchecked")
    public JSONObject getDataForExternalStrategyProvider(BaseDomainSession hostSession) {
        
        DomainKnowledgeManager hostManager = hostSession.domainKnowledgeManager;
        
        Integer hostDsId = hostSession.getDomainSessionInfo().getDomainSessionId();
            
            AbstractKnowledgeSession knowledgeSession = KnowledgeSessionManager.getInstance().getKnowledgeSessions().get(hostDsId);
        
        String roleName = null;
        
        if(logPlaybackServiceTopic != null) {
            
            /* For playback courses, we need to find the user role differently than usual, since the
             * user playing back the course might not have been playing as a unit */
            for(SessionMember member : knowledgeSession.getSessionMembers().getSessionMemberDSIdMap().values()) {
                if(member.getUserSession() != null 
                        && member.getUserSession() instanceof DomainSession) {
                    
                    DomainSession memberSession = ((DomainSession) member.getUserSession());
                    if(memberSession.getDomainSessionId() == domainSessionInfo.getDomainSessionId()
                            && member.getSessionMembership() != null
                            && member.getSessionMembership().getTeamMember() != null) {
                        
                        /* This is a team member from the original session that has an associated team member
                         * that's being "played" by this playback session, so use the role of that team
                         * member as the role for the playback session host */
                        roleName = member.getSessionMembership().getTeamMember().getName();
                        
                        break;
                    }
                }
            }
            
        } else {
            
            /* For regular courses, use look at the session membership to determine the role */
            SessionMember thisMember = knowledgeSession.getSessionMembers().getSessionMemberDSIdMap().get(domainSessionInfo.getDomainSessionId());
            roleName = thisMember.getSessionMembership().getTeamMember().getName();
        }
            
            /* 
         * Convert the following information to JSON to be shared with the external strategy provider:
         * 
             * Domain Session Information
             * Performance Assessments
         * Session membership
         * Condition assessment details
             */
            org.json.simple.JSONObject perfStateObj = new org.json.simple.JSONObject();
            perfStateObj.put("type", "PERFORMANCE_STATE");
            
            /* Add domain session information */
            perfStateObj.put("domainSessionId", domainSessionInfo.getDomainSessionId());
            perfStateObj.put("userId", domainSessionInfo.getUserId());
            perfStateObj.put("username", domainSessionInfo.getUsername());
            perfStateObj.put("role", roleName);
            perfStateObj.put("course", domainSessionInfo.getDomainSourceId());
            
            /* Add knowledge session information */         
            org.json.simple.JSONObject knowledgeSessionObj = new org.json.simple.JSONObject();
            knowledgeSessionObj.put("name", knowledgeSession.getNameOfSession());
            knowledgeSessionObj.put("startTime", knowledgeSession.getSessionStartTime());
            knowledgeSessionObj.put("teamStructure", getTeamJSON(knowledgeSession.getTeamStructure()));
            knowledgeSessionObj.put("hostSessionMember", getSessionMemberJSON(knowledgeSession.getHostSessionMember()));
            
            org.json.simple.JSONArray sessionMembersObj = new org.json.simple.JSONArray();
            for(SessionMember member : knowledgeSession.getSessionMembers().getSessionMemberDSIdMap().values()) {
                sessionMembersObj.add(getSessionMemberJSON(member));
            }
            knowledgeSessionObj.put("sessionMembers", sessionMembersObj);
            
            perfStateObj.put("knowledgeSession", knowledgeSessionObj);
            
            /* Add performance state information*/
            org.json.simple.JSONArray tasksObj = new org.json.simple.JSONArray();
            for(Task taskAssessment: hostManager.getAssessmentKnowledge().getScenario().getTasks()) {
                org.json.simple.JSONObject taskObj = new org.json.simple.JSONObject();
                taskObj.put("name", taskAssessment.getName());
                taskObj.put("id", taskAssessment.getNodeId());
                taskObj.put("assessment", taskAssessment.getAssessment().getAssessmentLevel() != null
                        ? taskAssessment.getAssessment().getAssessmentLevel().getName()
                        : AssessmentLevelEnum.UNKNOWN.getName());
                taskObj.put("active", taskAssessment.isActive());
                taskObj.put("completed", taskAssessment.isCompleted());
                
                org.json.simple.JSONArray conceptsObj = new org.json.simple.JSONArray();
                for(Concept conceptAssessment : taskAssessment.getConcepts()) {
                    org.json.simple.JSONObject conceptObj = getConceptAssessmentJSON(conceptAssessment);
                    conceptsObj.add(conceptObj);
                }
                
                taskObj.put("concepts", conceptsObj);
                
                tasksObj.add(taskObj);
            }
            perfStateObj.put("tasks",tasksObj);
            
            KnowledgeAssessmentDetails details = new KnowledgeAssessmentDetails();
            
            perfStateObj.put("assessmentVariables", 
                    hostManager.getAssessmentKnowledge().getScenario().getVarsHandler().getAssessmentVariablesModel(details));
            
        if(this.equals(hostSession) || hostSession == null) {
            
            /* Record the domain assessment details that are sent to the server */
            DomainModule.getInstance().sendKnowledgeAssessmentDetails(this, details);
        }
            
        return perfStateObj;  
                }

    @SuppressWarnings("unchecked")
    private JSONObject getTeamJSON(AbstractTeamUnit unit) {
        if(unit == null) {
            return null;
        }
        
        org.json.simple.JSONObject teamObj = new org.json.simple.JSONObject();
        teamObj.put("type", unit.getClass().getName());
        teamObj.put("name", unit.getName());
        teamObj.put("entityId", unit.getEntityIdentifier() != null ? unit.getEntityIdentifier().getEntityID() : null);
        
        if(unit instanceof Team) {
            
            Team team = (Team) unit;
            teamObj.put("echelon", team.getEchelon() != null ? team.getEchelon().getName(): null);
            
            org.json.simple.JSONArray unitsArray = new org.json.simple.JSONArray();
            for(AbstractTeamUnit subUnit : team.getUnits()) {
                unitsArray.add(getTeamJSON(subUnit));
            }
            
            teamObj.put("units", unitsArray);
            
        } else if(unit instanceof TeamMember<?>){
            
            TeamMember<?> member = (TeamMember<?>) unit;
            teamObj.put("playable", member.isPlayable());
        }
        
        return teamObj;
    }

    @SuppressWarnings("unchecked")
    private JSONObject getConceptAssessmentJSON(Concept conceptAssessment) {
        
        org.json.simple.JSONObject conceptObj = new org.json.simple.JSONObject();
        conceptObj.put("name", conceptAssessment.getName());
        conceptObj.put("id", conceptAssessment.getNodeId());
        conceptObj.put("assessment", conceptAssessment.getAssessment().getAssessmentLevel() != null
                ? conceptAssessment.getAssessment().getAssessmentLevel().getName()
                : AssessmentLevelEnum.UNKNOWN.getName());
       
        
        
        if(conceptAssessment instanceof IntermediateConcept) {
            org.json.simple.JSONArray conceptsObj = new org.json.simple.JSONArray();
            for(Concept childConceptAssessment : ((IntermediateConcept) conceptAssessment).getConcepts()) {
                org.json.simple.JSONObject childConceptObj= getConceptAssessmentJSON(childConceptAssessment);
                conceptsObj.add(childConceptObj);
            }
            conceptObj.put("concepts", conceptsObj);
        }
        
        return conceptObj;
    }
    
    @SuppressWarnings("unchecked")
    private JSONObject getSessionMemberJSON(SessionMember member) {
        
        org.json.simple.JSONObject memberObj = new org.json.simple.JSONObject();
        memberObj.put("userId", member.getUserSession().getUserId());
        memberObj.put("domainSessionId", member.getDomainSessionId());
        memberObj.put("username", member.getUserSession().getUsername());
        memberObj.put("unit", member.getSessionMembership().getTeamMember() != null ? member.getSessionMembership().getTeamMember().getName() : null);
        
        return memberObj;
    }
    
    /**
     * Clears any observer-specific metadata from this domain session's current scenario (if any),
     * namely any comments that were added by an observer.
     */
    public void clearObserverMetadata() {
        
        if(domainKnowledgeManager != null && domainKnowledgeManager.getAssessmentKnowledge().getScenario() != null) {
            domainKnowledgeManager.getAssessmentKnowledge().getScenario().clearObserverMetadata();
        }
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[BaseDomainSession:\n");
        sb.append(getDomainSessionInfo()).append("\n");
        sb.append(", lessonState = ").append(currentLessonState);
        sb.append(", trainingAppState = ").append(currentTrainingAppState);
        sb.append(", interopsEnabled = ").append(interopsEnabled);
        sb.append(", isCurrentAppEmbedded = ").append(isCurrentAppEmbedded);
        sb.append(", numEventsForAAR = ").append(pendingReviewCourseEvents.size());
        sb.append(", numPublishedRecordsForAAR = ").append(publishedScoreToEventIdMap.size());
        sb.append("]");
        return sb.toString();
    }

    /**
     * Inner class used for handling the actions to take when a survey is completed.
     *
     * @author mhoffman
     *
     */
    private class SurveyFinishedCallback implements MessageCollectionCallback{

        /** the response(s) to the survey */
        private SurveyResponse surveyResponse = null;

        /** extracted metadata of the survey reponse */
        private SurveyResponseMetadata responseMetadata = null;

        /** the name of the authored course object that is causing a survey to be presented  */
        private String currentCourseObjectName;

        /** the id of the survey context the survey is in */
        private int surveyContextId;

        /** The key of the survey presented in the current survey context */
        private String giftSurveyKey;

        /** contains various information needed to display the survey and deal with the survey response */
        private DisplaySurveyParamWrapper displaySurveyParamWrapper;

        /** Callback for when the results of the survey are available */
        private SurveyResultListener surveyResultListener;

        /** Callback for when presenting the survey is complete or a failure has occurred */
        private AsyncActionCallback callback;

        /**
         * Set attributes.
         *
         * @param currentCourseObjectName the name of the authored course object that is causing a survey to be presented.  Cant be null or empty.
         * @param surveyContextId - the id of the survey context the survey is in
         * @param giftSurveyKey The key of the survey presented in the current survey context
         * @param displaySurveyParamWrapper contains various information needed to display the survey and deal with the survey response
         * @param surveyResultListener Callback for when the results of the survey are available
         * @param callback Callback for when presenting the survey is complete or a failure has occurred
         */
        public SurveyFinishedCallback(String currentCourseObjectName, int surveyContextId, String giftSurveyKey,
                DisplaySurveyParamWrapper displaySurveyParamWrapper,
                SurveyResultListener surveyResultListener, AsyncActionCallback callback){

            this.currentCourseObjectName = currentCourseObjectName;
            this.surveyContextId = surveyContextId;
            this.giftSurveyKey = giftSurveyKey;
            this.displaySurveyParamWrapper = displaySurveyParamWrapper;
            this.surveyResultListener = surveyResultListener;
            this.callback = callback;
        }

        @Override
        public void success() {

            if (surveyResponse != null) {

                try{
                    List<ScoreInterface> scores = SurveyScorerManager.getScores(surveyResponse);

                    if(!scores.isEmpty() || surveyResponse.getHasFillInTheBlankQuestionWithIdealAnswer()) {

                        Date eventKey = new Date();
                        if(displaySurveyParamWrapper.showInAAR){

                            AfterActionReviewSurveyEvent aarSurveyEvent = new AfterActionReviewSurveyEvent(currentCourseObjectName, responseMetadata, scores, displaySurveyParamWrapper.showInAAR);
                            if(displaySurveyParamWrapper.isRecallSurvey){
                                //store this differently so that only this survey results are shown in the adaptive courseflow structured review
                                adaptiveCourseflowPendingAARInfo = aarSurveyEvent;
                            }else{
                                pendingReviewCourseEvents.put(eventKey, Arrays.asList(new AbstractAfterActionReviewEvent[]{aarSurveyEvent}));
                            }

                            if(logger.isInfoEnabled()){
                                logger.info("Added survey responses and all assessments/feedback to the collection of course events for any upcoming Structure Review (AAR) course object.");
                            }
                        }else{
                            //separate feedback scorer from other types in order to show feedback

                            List<ScoreInterface> shownScores = new ArrayList<>();
                            Iterator<ScoreInterface> scoresItr = scores.iterator();
                            while(scoresItr.hasNext()){

                                ScoreInterface scorer = scoresItr.next();
                                if(scorer instanceof SurveyFeedbackScorer){
                                    shownScores.add(scorer);
                                    scoresItr.remove();
                                }
                            }

                            List<AbstractAfterActionReviewEvent> events = new ArrayList<>();
                            if(!scores.isEmpty()){
                                AfterActionReviewSurveyEvent hiddenEvent = new AfterActionReviewSurveyEvent(currentCourseObjectName, responseMetadata, scores, displaySurveyParamWrapper.showInAAR);
                                events.add(hiddenEvent);
                            }

                            if(!shownScores.isEmpty()){
                                AfterActionReviewSurveyEvent feedbackEvent = new AfterActionReviewSurveyEvent(currentCourseObjectName, responseMetadata, shownScores, true);
                                events.add(feedbackEvent);
                            }

                            pendingReviewCourseEvents.put(eventKey, events);

                            if(logger.isInfoEnabled()){
                                logger.info("Added survey responses and assessments as one event and survey response and feedback as another event to the collection of course events for any upcoming Structure Review (AAR) course object.");
                            }
                        }


                    }

                }catch(Exception e){
                    logger.error("Caught exception while scoring survey response of "+surveyResponse, e);
                    callback.onFailure(e);
                }

                if (surveyResultListener != null) {

                    surveyResultListener.surveyCompleted(surveyResponse);
                }

                if(displaySurveyParamWrapper.saveResponseToDb){
                    sendSurveyResults(surveyContextId, giftSurveyKey, surveyResponse, callback, displaySurveyParamWrapper.fullScreen, displaySurveyParamWrapper.isMidLessonSurvey);
                } else {
                    callback.onSuccess();
                }
            } else {

                if(!displaySurveyParamWrapper.isMidLessonSurvey){

                    if(localSessionState == LOCAL_SESSION_STATE.RUNNING){
                        
                        if(LessonLevelEnum.RTA.equals(DomainModuleProperties.getInstance().getLessonLevel())){
                            // the survey course object was skipped because GIFT is in RTA mode which doesn't support
                            // survey course object presentation.  Therefore there is no survey response.  Continue to next course object.
                            callback.onSuccess();
                        }else{
                            callback.onFailure(new NullPointerException("Did not get a survey response to send."));
                        }
                    }else{
                        //the local session state is on its way to ending, therefore we don't care that a survey response was not
                        //received.  This mostly happens when the domain session ends after the request to display the survey has happened and
                        //before the survey response was received.
                        callback.onSuccess();
                    }

                } else {

                    //mid-lesson surveys might not receive a survey response if a pedagogical action causes the survey to be replaced
                    callback.onSuccess();
                }
            }
        }

        @Override
        public void received(Message msg) {
            if (msg.getMessageType() == MessageTypeEnum.DISPLAY_SURVEY_TUTOR_REPLY) {
                surveyResponse = (SurveyResponse) msg.getPayload();
                surveyResponse.setSurveyContextId(surveyContextId);
                surveyResponse.setGiftKey(giftSurveyKey);
                responseMetadata = new SurveyResponseMetadata(surveyResponse);
            }
        }

        @Override
        public void failure(Message msg) {

            callback.onFailure(generateException("Displaying survey to be presented with survey key '"
                    + giftSurveyKey + "' for survey context with ID '"
                    + surveyContextId + "'",
                    msg));
        }

        @Override
        public void failure(String why) {

            callback.onFailure(generateException("Displaying survey to be presented with survey key '"
                    + giftSurveyKey + "' for survey context with ID '"
                    + surveyContextId + "'",
                    why));
        }
    }
}
