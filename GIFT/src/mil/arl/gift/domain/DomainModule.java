/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.server.SameFileAliasChecker;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppContext;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import generated.course.CustomParameters;
import generated.course.LessonMaterial;
import generated.course.LtiProvider;
import generated.dkf.Audio;
import generated.dkf.BooleanEnum;
import generated.dkf.DelayAfterStrategy;
import generated.dkf.Feedback;
import generated.dkf.InstructionalIntervention;
import generated.dkf.Message.Delivery;
import generated.dkf.ScenarioControls;
import generated.dkf.Strategy;
import mil.arl.gift.common.AbstractAfterActionReviewEvent;
import mil.arl.gift.common.AbstractDisplayContentTutorRequest;
import mil.arl.gift.common.BranchPathHistory;
import mil.arl.gift.common.CloseDomainSessionRequest;
import mil.arl.gift.common.CourseState;
import mil.arl.gift.common.DisplayAfterActionReviewTutorRequest;
import mil.arl.gift.common.DisplayChatWindowRequest;
import mil.arl.gift.common.DisplayChatWindowUpdateRequest;
import mil.arl.gift.common.DisplayCourseInitInstructionsRequest;
import mil.arl.gift.common.DisplayLearnerActionsTutorRequest;
import mil.arl.gift.common.DisplayMediaCollectionRequest;
import mil.arl.gift.common.DisplayMidLessonMediaRequest;
import mil.arl.gift.common.DisplaySurveyTutorRequest;
import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.DomainOption.DomainOptionRecommendation;
import mil.arl.gift.common.DomainOptionsList;
import mil.arl.gift.common.DomainOptionsRequest;
import mil.arl.gift.common.DomainSelectionRequest;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.DomainSessionList;
import mil.arl.gift.common.EvaluatorUpdateRequest;
import mil.arl.gift.common.ExperimentCourseRequest;
import mil.arl.gift.common.FinishScenario;
import mil.arl.gift.common.GetExperimentRequest;
import mil.arl.gift.common.GetSurveyRequest;
import mil.arl.gift.common.InitializeDomainSessionRequest;
import mil.arl.gift.common.InitializeEmbeddedConnections;
import mil.arl.gift.common.InitializeInteropConnections;
import mil.arl.gift.common.InitializeLessonRequest;
import mil.arl.gift.common.InitializePedagogicalModelRequest;
import mil.arl.gift.common.InstantiateLearnerRequest;
import mil.arl.gift.common.KnowledgeSessionCreated;
import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.common.LMSCourseRecords;
import mil.arl.gift.common.LMSData;
import mil.arl.gift.common.LMSDataRequest;
import mil.arl.gift.common.LearnerTutorAction;
import mil.arl.gift.common.LoSQuery;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.PedagogicalRequest;
import mil.arl.gift.common.PerformanceAssessment;
import mil.arl.gift.common.PublishLessonScore;
import mil.arl.gift.common.PublishLessonScoreResponse;
import mil.arl.gift.common.Siman;
import mil.arl.gift.common.SubmitSurveyResults;
import mil.arl.gift.common.SurveyCheckRequest;
import mil.arl.gift.common.SurveyCheckResponse;
import mil.arl.gift.common.SurveyCheckResponse.FailureResponse;
import mil.arl.gift.common.SurveyCheckResponse.ResponseInterface;
import mil.arl.gift.common.SurveyListCheckRequest;
import mil.arl.gift.common.TaskAssessment;
import mil.arl.gift.common.TutorUserInterfaceFeedback;
import mil.arl.gift.common.UserData;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.UserSessionType;
import mil.arl.gift.common.WebClientInformation;
import mil.arl.gift.common.course.CourseValidationResults;
import mil.arl.gift.common.course.GIFTValidationResults.DetailedExceptionSerializedWrapper;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.SessionMembers;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.SessionType;
import mil.arl.gift.common.course.dkf.session.KnowledgeAssessmentDetails;
import mil.arl.gift.common.course.dkf.session.KnowledgeSessionsReply;
import mil.arl.gift.common.course.dkf.session.KnowledgeSessionsRequest;
import mil.arl.gift.common.course.dkf.session.LessonCompleted;
import mil.arl.gift.common.course.dkf.session.LessonCompleted.LessonCompletedStatusType;
import mil.arl.gift.common.course.dkf.session.ManageTeamMembershipException;
import mil.arl.gift.common.course.dkf.session.ManageTeamMembershipRequest;
import mil.arl.gift.common.course.dkf.session.SessionMember;
import mil.arl.gift.common.course.dkf.session.SessionMember.GroupMembership;
import mil.arl.gift.common.course.dkf.session.SessionMember.IndividualMembership;
import mil.arl.gift.common.course.dkf.session.SessionMember.SessionMembership;
import mil.arl.gift.common.course.dkf.session.TeamKnowledgeSession;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.course.strategy.AbstractStrategy;
import mil.arl.gift.common.course.strategy.ApplyStrategies;
import mil.arl.gift.common.course.strategy.AuthorizeStrategiesRequest;
import mil.arl.gift.common.course.strategy.ExecuteOCStrategy;
import mil.arl.gift.common.course.strategy.InstructionalInterventionStrategy;
import mil.arl.gift.common.course.strategy.MidLessonMediaStrategy;
import mil.arl.gift.common.course.strategy.StrategySet;
import mil.arl.gift.common.course.strategy.StrategyToApply;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.DomainOptionRecommendationEnum;
import mil.arl.gift.common.enums.ErrorEnum;
import mil.arl.gift.common.enums.GenderEnum;
import mil.arl.gift.common.enums.LessonLevelEnum;
import mil.arl.gift.common.enums.LessonStateEnum;
import mil.arl.gift.common.enums.MessageEncodingTypeEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.ModuleModeEnum;
import mil.arl.gift.common.enums.ModuleStateEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.enums.TrainingAppRouteTypeEnum;
import mil.arl.gift.common.experiment.DataCollectionItem;
import mil.arl.gift.common.experiment.ExperimentUtil.ExperimentStatus;
import mil.arl.gift.common.experiment.SubjectCreated;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.io.UriUtil;
import mil.arl.gift.common.io.UriUtil.InternetConnectionStatusEnum;
import mil.arl.gift.common.lti.LtiGetProviderUrlRequest;
import mil.arl.gift.common.lti.LtiLessonGradedScoreRequest;
import mil.arl.gift.common.metrics.MetricsSender;
import mil.arl.gift.common.module.AbstractModule;
import mil.arl.gift.common.module.DomainModuleStatus;
import mil.arl.gift.common.module.GatewayModuleStatus;
import mil.arl.gift.common.module.ModuleStatus;
import mil.arl.gift.common.module.ModuleStatusListener;
import mil.arl.gift.common.module.ModuleStatusMonitor;
import mil.arl.gift.common.module.ModuleStatusMonitor.StatusReceivedInfo;
import mil.arl.gift.common.module.WebMonitorModuleStatus;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.survey.AbstractQuestionResponse;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.SurveyResponse;
import mil.arl.gift.common.ta.request.EnvironmentControl;
import mil.arl.gift.common.ta.request.VariablesStateRequest;
import mil.arl.gift.common.ta.state.GenericJSONState;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.domain.DomainCourseFileHandler.CourseOptionsWrapper;
import mil.arl.gift.domain.course.SurveyHTMLElementsHandler;
import mil.arl.gift.domain.knowledge.KnowledgeSessionEventListener;
import mil.arl.gift.domain.knowledge.KnowledgeSessionManager;
import mil.arl.gift.domain.knowledge.strategy.StrategyAppliedEvent;
import mil.arl.gift.net.api.AllocatedModuleListener;
import mil.arl.gift.net.api.ConnectionFilter;
import mil.arl.gift.net.api.MessageCollectionCallback;
import mil.arl.gift.net.api.NetworkSession;
import mil.arl.gift.net.api.SubjectUtil;
import mil.arl.gift.net.api.message.ACK;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.MessageUtil;
import mil.arl.gift.net.api.message.NACK;
import mil.arl.gift.net.api.message.RawMessageHandler;
import mil.arl.gift.net.api.message.UserSessionMessage;
import mil.arl.gift.tools.authoring.common.conversion.AbstractConversionWizardUtil;
import mil.arl.gift.tools.services.file.FileServices;

/**
 * The domain module contains the domain logic and is the core of the GIFT
 * system.
 *
 * @author mhoffman
 *
 */
public class DomainModule extends AbstractModule implements KnowledgeSessionEventListener {

    /** instance of the logger */
    private static final Logger logger = LoggerFactory.getLogger(DomainModule.class);

    /** the name of the module */
    private static final String DEFAULT_MODULE_NAME = "Domain_Module";

    /** singleton instance of this class */
    private static DomainModule instance = null;
    
    /** 
     * The static block configuring the domain logger must be placed above the static fields that 
     * use an instance of DomainModuleProperties to retrieve property values. If not, every time the 
     * Domain Module is initialized, log4j will display WARN statements indicating that no logger has 
     * been appended for the module.
     */
    static {
        // use domain log4j
        PropertyConfigurator.configureAndWatch(PackageUtil.getConfiguration() + "/domain/domain.log4j.properties");
    }

    /**
     * the directory where course folders are placed when a user wants to run a
     * course
     */
    private static DesktopFolderProxy runtimeDirectory = new DesktopFolderProxy(
            new File(DomainModuleProperties.getInstance().getDomainRuntimeDirectory()));

    /**
     * the legacy directory where course folders are placed when a subject is running
     * an experiment
     */
    private static DesktopFolderProxy legacyExperimentDirectory = new DesktopFolderProxy(
            new File(DomainModuleProperties.getInstance().getExperimentsDirectory()));

    /**
     * the directory where course folders are placed when a subject is running
     * an experiment
     */
    private static File experimentDirectory = new File(runtimeDirectory.getFileId() + File.separator + "experiments");

    private static File WORKSPACE_DIRECTORY = new File(DomainModuleProperties.getInstance().getWorkspaceDirectory());

    /** map of domain session id to current domain session instance */
    private final Map<Integer, BaseDomainSession> domainSessionIdToDomainSession = new HashMap<>();

    /** lookup for the web monitors that are attached to a domain session */
    private final Map<Integer, Set<String>> domainSessionIdToWebMonitorInboxes = new HashMap<>();

    /** The message handler that will handle messages coming from all training applications.
     *  This includes both interop training applications and embedded training applications. */
    private RawMessageHandler trainingAppMessageHandler = new RawMessageHandler() {

        @Override
        public boolean processMessage(String msg, MessageEncodingTypeEnum encodingType) {

            // received message in queue, decode it
            Message message = MessageUtil.getMessageFromString(msg, encodingType);

            handleTrainingAppGameStateMessage(message);

            return true;
        }
    };

    /**
     * Simple Mode only: Contains information about the last queried set of
     * known courses to this domain module instance. This is valid only in simple mode, or if
     * the user is accessing the old simple login ui when running in desktop mode.
     */
    private SimpleModeCourseOptions simpleModeCourseOptions = null;

    /** The metrics sender is responsible for sending metrics of the domain module to the metrics server */
    private MetricsSender metrics = new MetricsSender("domain");
    private static final String NUM_DOMAIN_SESSIONS_METRIC = "domainSessionsTotal";
    private static final String NUM_EXPERIMENT_DOMAIN_SESSIONS_METRIC = "domainSessionsExperiments";
    
    /** unique status object for Domain module to send messages to itself when playing back a domain session log */
    private DomainModuleStatus domainStatus;

    /**
     * Return the singleton instance of this class
     *
     * @return singleton instance which could be newly created
     */
    public static synchronized DomainModule getInstance() {

        if (instance == null) {
            instance = new DomainModule();
            instance.init();
        }

        return instance;
    }

    /**
     * Default constructor
     */
    private DomainModule() {
        super(DEFAULT_MODULE_NAME, SubjectUtil.DOMAIN_QUEUE_PREFIX + ADDRESS_TOKEN_DELIM + ipaddr,
                SubjectUtil.DOMAIN_QUEUE_PREFIX + ADDRESS_TOKEN_DELIM + ipaddr + ADDRESS_TOKEN_DELIM + SubjectUtil.INBOX_QUEUE_SUFFIX,
                DomainModuleProperties.getInstance());
    }

    @Override
    protected void init() {

        // This is only valid in Simple mode or if the user is accessing simple login pages in desktop mode.
        simpleModeCourseOptions = new SimpleModeCourseOptions();
        
        KnowledgeSessionManager.getInstance().setEventListener(this);

        // create client to send domain status too
        createSubjectTopicClient(SubjectUtil.DOMAIN_DISCOVERY_TOPIC, false);
        createSubjectTopicClient(SubjectUtil.MONITOR_TOPIC, false);
        
        domainStatus = new DomainModuleStatus(moduleStatus);

        ModuleStatusMonitor.getInstance().addListener(new ModuleStatusListener() {

            @Override
            public void moduleStatusRemoved(StatusReceivedInfo status) {
                if( status.getModuleStatus() instanceof WebMonitorModuleStatus) {
                    for (Map.Entry<Integer, Set<String>> entry : domainSessionIdToWebMonitorInboxes.entrySet()) {
                        entry.getValue().remove(status.getModuleStatus().getQueueName());
                    }
                }
            }

            @Override
            public void moduleStatusChanged(long sentTime, ModuleStatus status) {
                if (status instanceof WebMonitorModuleStatus) {
                    WebMonitorModuleStatus wmStatus = (WebMonitorModuleStatus) status;

                    /* Ensure that all keys have entries */
                    for (Integer domainId : wmStatus.getAttachedDomainSessions()) {
                        if (!domainSessionIdToWebMonitorInboxes.containsKey(domainId)) {
                            domainSessionIdToWebMonitorInboxes.put(domainId, new HashSet<>());
                        }
                    }

                    /* Remove the association of the queue from the domain
                     * session if the web monitor is no longer attached.
                     * Otherwise, ensure it is associated. */
                    Set<Integer> attachedSessions = wmStatus.getAttachedDomainSessions();
                    for (Map.Entry<Integer, Set<String>> entry : domainSessionIdToWebMonitorInboxes.entrySet()) {
                        if (attachedSessions.contains(entry.getKey())) {
                            entry.getValue().add(wmStatus.getQueueName());
                        } else {
                            entry.getValue().remove(wmStatus.getQueueName());
                        }
                    }
                }
            }

            @Override
            public void moduleStatusAdded(long sentTime, ModuleStatus status) {
                moduleStatusChanged(sentTime, status);
            }
        });

        // register a handler for notification of simulation messages
        registerTrainingAppGameStateMessageHandler(trainingAppMessageHandler);

        // register a handler for notification of allocated module events
        registerAllocatedModuleHandler(new AllocatedModuleListener() {

            @Override
            public void allocatedModuleRemoved(int userId, ModuleTypeEnum moduleType, StatusReceivedInfo lastStatus) {

                // for now just iterate over current map
                // domainSessionIdToDomainSession to find
                // the domain session with the same user id. This way don't have
                // to create new user-id to current domain session map
                // and then have to maintain that map along side
                // domainSessionIdToDomainSession
                synchronized (domainSessionIdToDomainSession) {

                    for (BaseDomainSession session : domainSessionIdToDomainSession.values()) {

                        if (session.getDomainSessionInfo().getUserId() == userId) {
                            // found session
                            if (session.isActive()) {

                                // check with the session to see if this removed
                                // module is needed
                                // this is for ticket 2061 where a previously
                                // closed JWS GW module could timeout
                                // while another JWS GW module is coming online
                                // cause the session to terminate below
                                if (session.checkSessionAllocatedModuleRemoved(lastStatus)) {

                                    if (moduleType == ModuleTypeEnum.GATEWAY_MODULE) {
                                        session.getDomainSessionInfo().setGatewayConnected(false);
                                    }

                                    session.terminateSession(true,
                                            "The " + moduleType.getDisplayName()
                                                    + " is no longer connected to the Domain Module after not hearing from it for "
                                                    + lastStatus.getTimeoutValue() / 1000.0
                                                    + " seconds, therefore forcing the domain session to close",
                                            "A module can timeout for several reasons including slow network connectivity.  In order to ensure that the appropriate tutoring actions take place when they should, the GIFT modules need to be able to communicate with each other.");
                                    break;
                                }
                            }
                        }
                    }
                }

            }
        });

        // start the module heartbeat
        // - Note: doing this before populating the course list to allow
        // applications such as SPL to continue execution.
        // The module status will indicate when the state of the domain module
        // is ready.
        initializeHeartbeat();

        int fileServerPort = DomainModuleProperties.getInstance().getDomainContentServerPort();
        startFileServer(fileServerPort);

        // Start sending metrics.
        metrics.startSending();

        // reset to zero since the module is just starting
        metrics.updateMetricCounter(NUM_DOMAIN_SESSIONS_METRIC, domainSessionIdToDomainSession.size());
        metrics.updateMetricCounter(NUM_EXPERIMENT_DOMAIN_SESSIONS_METRIC, domainSessionIdToDomainSession.size());
    }

    /**
     * Return the module type
     *
     * @return ModuleTypeEnum
     */
    @Override
    public ModuleTypeEnum getModuleType() {
        return ModuleTypeEnum.DOMAIN_MODULE;
    }

    @Override
    protected void handleMessage(Message message) {

        if (message.getMessageType() == MessageTypeEnum.KILL_MODULE) {
            handleKillModuleMessage(message);
        } else if (message.getMessageType() == MessageTypeEnum.DOMAIN_OPTIONS_REQUEST) {
            handleDomainOptionsRequest(message);
        } else if (message.getMessageType() == MessageTypeEnum.DOMAIN_SELECTION_REQUEST) {
            handleDomainSelectionRequest(message);
        } else if (message.getMessageType() == MessageTypeEnum.PEDAGOGICAL_REQUEST) {
            handlePedagogicalRequest(message);
        } else if (message.getMessageType() == MessageTypeEnum.LEARNER_TUTOR_ACTION) {
            handleLearnerTutorActionMessage(message);
        } else if (message.getMessageType() == MessageTypeEnum.CHAT_LOG) {
            handleChatLogMessage(message);
        } else if (message.getMessageType() == MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST) {
            handleCloseDomainSessionRequest(message);
        } else if (message.getMessageType() == MessageTypeEnum.ACTIVE_DOMAIN_SESSIONS_REQUEST) {
            handleActiveDomainSessionsRequest(message);
        } else if (message.getMessageType() == MessageTypeEnum.ACTIVE_KNOWLEDGE_SESSIONS_REQUEST) {
            handleActiveKnowledgeSessionsRequest(message);
        } else if (message.getMessageType() == MessageTypeEnum.MANAGE_MEMBERSHIP_TEAM_KNOWLEDGE_SESSION){
            handleManageMembershipTeamKnowledgeSessionRequest(message);
        } else if (message.getMessageType() == MessageTypeEnum.START_TEAM_KNOWLEDGE_SESSION_REQUEST){
            handleStartTeamKnowledgeSessionRequest(message);
        } else if (message.getMessageType() == MessageTypeEnum.KNOWLEDGE_SESSION_UPDATED_REQUEST){
            handleKnowledgeSessionUpdatedRequest(message);
        } else if (message.getMessageType() == MessageTypeEnum.EXPERIMENT_COURSE_REQUEST) {
            handleExperimentCourseRequest(message);
        } else if (message.getMessageType() == MessageTypeEnum.TUTOR_SURVEY_QUESTION_RESPONSE) {
            handleTutorSurveyQuestionResponse(message);
        } else if (message.getMessageType() == MessageTypeEnum.LTI_GET_PROVIDER_URL_REQUEST ) {
            handleGetLtiProviderUrl(message);
        } else if (message.getMessageType() == MessageTypeEnum.EVALUATOR_UPDATE_REQUEST) {
            handleEvaluatorUpdateRequest(message);
        } else if (message.getMessageType() == MessageTypeEnum.APPLY_STRATEGIES) {
            handleApplyStrategies(message);
        } else {

            logger.error(getModuleName() + " received unhandled message:" + message);

            if (message.needsHandlingResponse()) {
                this.sendReply(message, new NACK(ErrorEnum.UNHANDLED_MESSAGE_ERROR,
                        "The module did not know how to handle the message"), MessageTypeEnum.PROCESSED_NACK);
            }
        }
    }

    /**
     * Handle the notification that the learner answered a survey question in
     * the tutor.
     *
     * @param message
     *            contains the survey question response details
     */
    private void handleTutorSurveyQuestionResponse(Message message) {

        DomainSessionMessage domainSessionMessage = (DomainSessionMessage) message;
        BaseDomainSession ds = domainSessionIdToDomainSession.get(domainSessionMessage.getDomainSessionId());
        if (ds != null) {

            AbstractQuestionResponse questionResponse = (AbstractQuestionResponse) message.getPayload();
            try {
                ds.handleTutorSurveyQuestionResponse(questionResponse);
            } catch (Exception e) {
                logger.error("Caught exception while handling notificatoin of tutor survey question response of "
                        + questionResponse + ".", e);
                ds.terminateSession(true, "There was a problem handling a tutor survey question response notification.",
                        "An exception was thrown that reads:\n" + e.getMessage()
                                + ".\n\nFor additional debug information check the latest Domain module log file at GIFT/output/logger/module/ for more information.");
            }

        } else {
            logger.error("Received tutor survey question response message for user " + domainSessionMessage.getUserId()
                    + " in domain session " + domainSessionMessage.getDomainSessionId()
                    + ", but the Domain module doesn't have a domain session with that id.  Checked "
                    + domainSessionIdToDomainSession.size() + " known domain sessions.\n" + message);
        }
    }

    /**
     * Retrieves the current course object's LTI provider using the LTI provider id specified by the
     * author. The LTI provider is retrieved from the list of LTI providers in the course
     * properties.
     *
     * @param domainSessionId the domain session id
     * @return the course LTI provider. Can return null if the current course object is no longer an
     *         LTI course object, if the LtiProperties cannot be found within the current course
     *         object, or if the LTI provider identifier doesn't match the list of LTI providers in
     *         the course properties.
     */
    public LtiProvider lookupCurrentLtiProvider(Integer domainSessionId) {
        LtiProvider provider = null;

        BaseDomainSession ds = domainSessionIdToDomainSession.get(domainSessionId);
        if (ds != null) {
            provider = ds.findCurrentCourseObjectLtiProvider();
        }

        return provider;
    }

    /**
     * Handle the request to build a provider url.
     *
     * @param message the provider url request message.
     */
    private void handleGetLtiProviderUrl(Message message) {

        LtiGetProviderUrlRequest request = (LtiGetProviderUrlRequest) message.getPayload();

        BaseDomainSession ds = domainSessionIdToDomainSession.get(request.getDomainSessionId());
        if (logger.isDebugEnabled()) {
            logger.debug("BaseDomainSession: " + ds + " retrieved with id: " + request.getDomainSessionId());
        }
        if (ds != null) {
            String ltiId = request.getLtiId();
            CustomParameters customParameters = request.getCustomParameters();
            String rawUrl = request.getRawUrl();
            try {
                String protectedUrl = ds.handleGetLtiProviderUrl(ltiId, customParameters, rawUrl);
                sendReply(message, protectedUrl, MessageTypeEnum.LTI_GET_PROVIDER_URL_REPLY);
            } catch (Exception e) {
                logger.error("Caught exception while handling the get LTI provider URL request message.", e);
                sendReply(message, new NACK(ErrorEnum.OPERATION_FAILED, e.getMessage()), MessageTypeEnum.PROCESSED_NACK);
            }
        } else {
            logger.error("Base Domain Session was null with id: " + request.getDomainSessionId());
            sendReply(message, new NACK(ErrorEnum.OPERATION_FAILED, "Base Domain Session was null with id: " + request.getDomainSessionId()),
                    MessageTypeEnum.PROCESSED_NACK);
        }
    }

    /**
     * Process the LTI consumer's score from the LTI provider response. Checks the course object's
     * slider bar values to determine if the user is Novice, Journeyman, or Expert.
     *
     * @param score the score returned from the LTI provider.
     * @param domainSessionId the domain session id.
     */
    public void processLtiConsumerScoreResponse(Double score, Integer domainSessionId) {
        BaseDomainSession ds = domainSessionIdToDomainSession.get(domainSessionId);
        if (ds != null && score != null) {
            ds.processLtiConsumerScoreResponse(score);
        }
    }

    /**
     * Handle a pedagogical request for this domain.
     *
     * @param message
     *            - the ped request message
     */
    private void handlePedagogicalRequest(Message message) {

        if (logger.isDebugEnabled()) {
            logger.debug("Received ped request: " + message);
        }

        DomainSessionMessage domainSessionMessage = (DomainSessionMessage) message;
        BaseDomainSession ds = domainSessionIdToDomainSession.get(domainSessionMessage.getDomainSessionId());
        if (ds != null) {

            PedagogicalRequest pRequest = (PedagogicalRequest) message.getPayload();
            try {
                ds.handlePedagogicalRequest(pRequest);
            } catch (Exception e) {
                logger.error("Caught exception while handling pedagogical request of " + pRequest + ".", e);
                ds.terminateSession(true, "There was a problem handling a pedagogical request.",
                        "An exception was thrown.  Please review the latest Domain module log file at GIFT/output/logger/module/ for more information.");
            }

        } else {
            logger.error("Received pedagogical request message for user " + domainSessionMessage.getUserId()
                    + " in domain session " + domainSessionMessage.getDomainSessionId()
                    + ", but the Domain module doesn't have a domain session with that id.  Checked "
                    + domainSessionIdToDomainSession.size() + " known domain sessions.\n" + message);

        }
    }

    /**
     * Routes an incoming {@link MessageTypeEnum#APPLY_STRATEGIES} GIFT
     * {@link Message} to the appropriate {@link BaseDomainSession}.
     *
     * @param message The {@link Message} that has been received and needs to be
     *        handled. Can't be null.
     */
    private void handleApplyStrategies(Message message) {
        DomainSessionMessage domainSessionMessage = (DomainSessionMessage) message;
        int domainSessionId = domainSessionMessage.getDomainSessionId();
        BaseDomainSession ds = domainSessionIdToDomainSession.get(domainSessionId);
        if (ds != null) {
            ApplyStrategies applyStrategies = (ApplyStrategies) message.getPayload();
            handleApplyStrategies(applyStrategies, ds);
        } else {
            logger.error("Received apply strategies message for user " + domainSessionMessage.getUserId()
                    + " in domain session " + domainSessionId
                    + ", but the Domain module doesn't have a domain session with that id. Checked "
                    + domainSessionIdToDomainSession.size() + " known domain sessions.\n" + message);
        }
    }
    
    /**
     * Execute the instructional strategy activities provided for a single
     * instructional strategy with the given name. If the domain session is a
     * joiner and not a host, this method will do nothing. This will not hold onto the calling
     * thread and will instead apply strategies in a separate thread.  This method version is normally used
     * when a request to apply the strategy is without a pedagogical request/approval (e.g. learner action strategy).
     * 
     * @param strategyName the name of the instructional strategy with the
     *        activities to apply.
     * @param strategySet the instructional strategy activities to apply.
     * @param hostBaseDs the domain session requesting the strategy be applied.
     * @param strategyStress optional stress rating for this strategy being applied
     */
    public void handleApplyStrategy(final String strategyName, final StrategySet strategySet, 
            final BaseDomainSession hostBaseDs, final Double strategyStress){
     
        if (strategySet == null) {
            throw new IllegalArgumentException("The parameter 'strategies' cannot be null.");
        } else if (hostBaseDs == null) {
            throw new IllegalArgumentException("The parameter 'ds' cannot be null.");
        }

        final int domainSessionId = hostBaseDs.getDomainSessionInfo().getDomainSessionId();

        // handle strategies in a separate thread in case one strategy (such as
        // a mid-lesson survey) needs to pause execution
        Thread applyStrategyThread = new Thread("HandleApplyStrategy-" + domainSessionId) {

            @Override
            public void run() {

                try{
                    /* If the message domain session is a member of a team knowledge
                     * session, we want to drop the message on the ground. The host
                     * will redirect the message to the team members and we don't
                     * want to process duplicate messages. */
                    KnowledgeSessionManager ksm = KnowledgeSessionManager.getInstance();
                    if (ksm.isMemberOfTeamKnowledgeSession(domainSessionId) != null) {
                        return;
                    }
    
                    /* Get the list of joined BaseDomainsessions (if any sessions
                     * are joined) */
                    Map<Integer, SessionMember> joinerSessionIdToInfo = ksm
                            .getTeamKnowledgeSessionsMembersForHost(domainSessionId);
                    Collection<BaseDomainSession> joinedSessions;
                    if (joinerSessionIdToInfo != null) {
                        Collection<Integer> joinedSessionIds = joinerSessionIdToInfo.keySet();
                        joinedSessions = new ArrayList<>(joinedSessionIds.size());
                        if(!hostBaseDs.isInPlaybackMode()){
                            // there won't be any joiner base domain sessions in playback mode
                            for (Integer jId : joinedSessionIds) {
                                joinedSessions.add(domainSessionIdToDomainSession.get(jId));
                            }
                        }
                    } else {
                        joinedSessions = new ArrayList<>();
                    }
                    for (AbstractStrategy activity : strategySet.getStrategies()) {
                        try {
                            handleApplyStrategy(activity, hostBaseDs, null, "Learner Action", joinedSessions, false, strategyStress);
                        } catch (Exception e) {
                            logger.error(
                                    "Caught exception while handling apply strategy for " + hostBaseDs.getDomainSessionInfo() + ".",
                                    e);
                            hostBaseDs.terminateSession(true,
                                    "There was a problem handling a request to apply a strategy named '" + strategyName
                                            + "' with a set of " + strategySet.getStrategies().size() + " actions.",
                                    "An exception was thrown.  Please review the latest Domain module log file at GIFT/output/logger/module/ for more information.");
                        }
                    }
                    
                    StrategyAppliedEvent event = new StrategyAppliedEvent(strategyName);
                    event.setStrategyAppliedStress(strategySet.getStress());
                    event.setStrategyAppliedDifficulty(strategySet.getDifficulty());
                                        
                    // now that the strategy has been applied, notify the domain structure in case any triggers will activate
                    hostBaseDs.appliedStrategyNotification(event);
                
                } catch (Exception e) {
                    logger.error("Caught exception while handling apply strategy named '" + strategyName + "'.", e);
                    hostBaseDs.terminateSession(true, "There was a problem applying the strategy named '"+strategyName+"'.",
                            "An exception was thrown.  Please review the latest Domain module log file at GIFT/output/logger/module/ for more information.");
                }
            }
        };

        applyStrategyThread.start();
    }

    /**
     * Handles an {@link ApplyStrategies} for a given {@link BaseDomainSession}. This will not hold onto the calling
     * thread and will instead apply strategies in a separate thread.  This method version is normally used
     * when an official pedagogical request happens and/or some logic approves strategies (Game Master, Adaptive Learning Service API).
     *
     * @param applyStrategies The {@link ApplyStrategies} payload to handle.
     *        Can't be null.
     * @param hostBaseDs The {@link BaseDomainSession} to within which to handle the
     *        payload. Can't be null.
     */
    public void handleApplyStrategies(ApplyStrategies applyStrategies, final BaseDomainSession hostBaseDs) {
        if (applyStrategies == null) {
            throw new IllegalArgumentException("The parameter 'applyStrategies' cannot be null.");
        } else if (hostBaseDs == null) {
            throw new IllegalArgumentException("The parameter 'ds' cannot be null.");
        }

        final int domainSessionId = hostBaseDs.getDomainSessionInfo().getDomainSessionId();

        //handle strategies in a separate thread in case one strategy (such as a mid-lesson survey) needs to pause execution
        Thread applyStrategiesThread = new Thread("HandleApplyStrategies-" + domainSessionId) {

            @Override
            public void run() {

                try {

                    /* If the message domain session is a member of a team knowledge
                     * session, we want to drop the message on the ground. The host will
                     * redirect the message to the team members and we don't want to
                     * process duplicate messages. */
                    KnowledgeSessionManager ksm = KnowledgeSessionManager.getInstance();
                    if (ksm.isMemberOfTeamKnowledgeSession(domainSessionId) != null) {
                        return;
                    }

                    /* Get the list of joined BaseDomainsessions (if any sessions are
                     * joined) */
                    Map<Integer, SessionMember> joinerSessionIdToInfo = ksm.getTeamKnowledgeSessionsMembersForHost(domainSessionId);
                    Collection<BaseDomainSession> joinedSessions;
                    if (joinerSessionIdToInfo != null) {
                        Collection<Integer> joinedSessionIds = joinerSessionIdToInfo.keySet();
                        joinedSessions = new ArrayList<>(joinedSessionIds.size());
                        if(!hostBaseDs.isInPlaybackMode()){
                            // there won't be any joiner base domain sessions in playback mode
                            for (Integer jId : joinedSessionIds) {
                                joinedSessions.add(domainSessionIdToDomainSession.get(jId));
                            }
                        }
                    } else {
                        joinedSessions = new ArrayList<>();
                    }
                    
                    for (StrategyToApply strategyToApply : applyStrategies.getStrategies()) {
                        
                        Strategy strategy = strategyToApply.getStrategy();
                        
                        // get the optional strategy stress value
                        Double strategyStress = null;
                        if(strategy.getStress() != null) {
                            strategyStress = Double.valueOf(strategy.getStress().doubleValue());
                        }
                        
                        // get the optional strategy difficulty value
                        Double strategyDifficulty = null;
                        if(strategy.getDifficulty() != null) {
                            strategyDifficulty = Double.valueOf(strategy.getDifficulty().doubleValue());
                        }
                        
                        for (AbstractStrategy activity : AbstractStrategy.createActivitiesFrom(strategy)) {
                            String evaluator = strategyToApply != null ? strategyToApply.getEvaluator() : null;
                            String reason = strategyToApply != null ? strategyToApply.getTrigger() : null;
                            handleApplyStrategy(activity, hostBaseDs, evaluator, reason, joinedSessions, applyStrategies.isScenarioSupport(), strategyStress);                            
                        }  
                        
                        if(strategy.isShouldResetScenario() != null && strategy.isShouldResetScenario()) {
                        	
                        	/* The author has specified that this strategy should reset the knowledge session */
                        	hostBaseDs.resetCurrentKnowledgeSession();	
                        }
                        
                        StrategyAppliedEvent event = new StrategyAppliedEvent(strategy.getName());
                        event.setStrategyAppliedStress(strategyStress);
                        event.setStrategyAppliedDifficulty(strategyDifficulty);
                        
                        if(strategyToApply.getTaskConceptsAppliedToo() != null) {
                            // find tasks ids associated with the strategyToApply taskconcept ids
                            event.setTasksAppliedToo(hostBaseDs.getCourseTaskIds(strategyToApply.getTaskConceptsAppliedToo()));
                        }
                        
                        // now that the strategy has been applied, notify the domain structure in case any triggers will activate
                        hostBaseDs.appliedStrategyNotification(event);
                    }                
                    
                } catch (Exception e) {
                    logger.error("Caught exception while handling apply strategies message of " + applyStrategies + ".", e);
                    hostBaseDs.terminateSession(true, "There was a problem handling a pedagogical request.",
                            "An exception was thrown.  Please review the latest Domain module log file at GIFT/output/logger/module/ for more information.");
                }
            }

        };

        applyStrategiesThread.start();
    }
    
    /**
     * Execute the instructional strategy activity provided.
     * 
     * @param activity the instructional strategy activity to apply (e.g. feedback).  If null this method does nothing.
     * @param ds the host domain session to apply the activity too.  Can't be null.
     * @param evaluator the username of the person who initiated the strategy. Can be null if request was made automatically by GIFT.
     * @param reason the reason for the strategy being activated. Can be null.
     * @param joinedSessions the other sessions joined to this host session.  Can be empty.
     * @param isScenarioSupport whether this strategy is associated with a scenario element like a task trigger versus a 
     * strategy that is in the set of strategies for the pedagogical module to request
     * @param strategyStress an optional value of stress associated with this strategy. Can be null.
     * See ProxyTaskAssessment for ranges on stress value.
     * @throws Exception if there was a problem applying the activity.
     */
    private void handleApplyStrategy(AbstractStrategy activity, final BaseDomainSession ds, 
            String evaluator, String reason, Collection<BaseDomainSession> joinedSessions, 
            boolean isScenarioSupport, Double strategyStress) throws Exception{
        
        /* If this activity is intended for the observer controller send it back to the
         * controller */
        if (activity instanceof InstructionalInterventionStrategy) {
            InstructionalInterventionStrategy iis = (InstructionalInterventionStrategy) activity;
            Serializable presentation = iis.getFeedback().getFeedbackPresentation();
            Strategy controllerStrategy = null;
            if (presentation instanceof generated.dkf.Message) {
                generated.dkf.Message message = (generated.dkf.Message) presentation;
                if (message.getDelivery() != null && message.getDelivery().getToObserverController() != null) {
                    
                    // create a strategy with just the activity intended for the controller
                    controllerStrategy = copyStrategyActivityForObserverController(activity.getName(), iis, message);
                }
            } else if (presentation instanceof generated.dkf.Audio) {
                generated.dkf.Audio audio = (generated.dkf.Audio) presentation;
                
                if (audio.getToObserverController() != null) {
                
                    // Modify the audio path if it is going to be sent to the dashboard 
                    modifyAudioToPrependDomainContentServerAddress(audio, ds);

                    /* create a strategy with just the activity intended for the controller */
                    controllerStrategy = copyStrategyActivityForObserverController(activity.getName(), iis, audio);
                }
            }
            
            if (controllerStrategy != null) {
                // send this strategy to the observer controller
                for (BaseDomainSession joinedSession : joinedSessions) {
                    sendActivityToObserverController(controllerStrategy, joinedSession, evaluator, reason, isScenarioSupport);
                }
                sendActivityToObserverController(controllerStrategy, ds, evaluator, reason, isScenarioSupport);
            }
        }
        
        List<Thread> activityThreads = new ArrayList<>();
        final List<Exception> caughtExceptions = new ArrayList<>();
        
        /* Execute the activity for each joiner */
        for (final BaseDomainSession joinedSession : joinedSessions) {
            
            if(joinedSession == null) {
                logger.warn("Unable to apply a strategy for a missing joiner in domain session " + ds.getDomainSessionInfo().getDomainSessionId() 
                        + ". They may have been filtered out by an earlier step.");
            }
            
            Thread joinerActivityTread = new Thread("Joiner Activity Thread") {
                
                @Override
                public void run() {
                    try {
                        executeStrategyActivity(activity, joinedSession);
                        
                    } catch (Exception e) {
                        caughtExceptions.add(e);
                    }
                }
            };
            
            activityThreads.add(joinerActivityTread);
            joinerActivityTread.start();
        }

         
        if(activityThreads.isEmpty()) {
            
            /* Execute the activity for the host.*/
            executeStrategyActivity(activity, ds);
            
        } else {
            
            /* 
             * Wait for all joiners and the host to finish handling the activity.
             * This entails executing each activity in a separate thread and calling
             * join on each of them to suspend this thread until all of them 
             * have ended.
             * 
             * This is needed for 2 reasons:
             *     1) The host needs to handle any delays AFTER all joiners have executed
             *     2) Activities that pause execution, such as webpages, can last indefinitely
             *        and may block other domain sessions attempting to execute the strategy
             *        if they are executed in the same thread.
             */
            
            Thread hostActivityTread = new Thread("Host Activity Thread") {
                
                @Override
                public void run() {
                    try {
                        executeStrategyActivity(activity, ds);
                        
                    } catch (Exception e) {
                        caughtExceptions.add(e);
                    }
                }
            };
            
            /* Let the host handle the activity*/
            hostActivityTread.start();
            
            /* wait on the joiners */
            for(Thread joinerThread : activityThreads) {
                joinerThread.join();
            }
            
            /* wait on the host */
            hostActivityTread.join();
            
            if(!caughtExceptions.isEmpty()) {
                
                /* If any exceptions occurred, log them and send the first one
                 * back to this method's invoker */
                for(Exception exception : caughtExceptions) {
                    logger.error("Caught exception while executing concurrent activity", exception);
                }
                
                throw caughtExceptions.get(0);
            }
        }
    }
    
    /**
     * Creates a deep copy of the feedback presentation strategy activity to send to the controller
     * 
     * @param strategyName the name of the strategy to copy from 
     * @param iis the instructional intervention that contains the message
     * @param presentation the presentation method to copy
     * @return a new strategy with a copy of the activity
     */
    private Strategy copyStrategyActivityForObserverController(String strategyName,
            InstructionalInterventionStrategy iis, Serializable presentation) {
        Strategy controllerStrategy = new Strategy();
        controllerStrategy.setName(strategyName);
        InstructionalIntervention ii = new InstructionalIntervention();
        if (iis.getDelayAfterStrategy() > 0) {
            DelayAfterStrategy delay = new DelayAfterStrategy();
            delay.setDuration(new BigDecimal(iis.getDelayAfterStrategy()));
            ii.setDelayAfterStrategy(delay);
        }
        ii.setStrategyHandler(iis.getHandlerInfo());
        Feedback feedback = new Feedback();
        feedback.setAffectiveFeedbackType(iis.getFeedback().getAffectiveFeedbackType());
        feedback.setFeedbackDuration(iis.getFeedback().getFeedbackDuration());
        feedback.setFeedbackSpecificityType(iis.getFeedback().getFeedbackSpecificityType());
        // intentionally omitting team ref on feedback since it's just being sent to the controller
        copyPresentation(presentation, feedback);
        ii.setFeedback(feedback);
        controllerStrategy.getStrategyActivities().add(ii);
        return controllerStrategy;
    }

    /**
     * Copies the given feedback presentation to the feedback
     * 
     * @param presentation the presentation to copy
     * @param feedback the feedback to apply the copied presentation to
     */
    private void copyPresentation(Serializable presentation, Feedback feedback) {
        if (presentation instanceof generated.dkf.Message) {
            generated.dkf.Message message = (generated.dkf.Message) presentation;
            generated.dkf.Message copyMessage = new generated.dkf.Message();
            copyMessage.setContent(message.getContent());
            Delivery copyDelivery = new Delivery();
            copyDelivery.setInTrainingApplication(message.getDelivery().getInTrainingApplication());
            copyDelivery.setInTutor(message.getDelivery().getInTutor());
            copyDelivery.setToObserverController(message.getDelivery().getToObserverController());
            copyMessage.setDelivery(copyDelivery);
            feedback.setFeedbackPresentation(copyMessage);
        } else if (presentation instanceof generated.dkf.Audio) {
            Audio audio = (Audio) presentation;
            Audio copyAudio = new Audio();
            copyAudio.setMP3File(audio.getMP3File());
            copyAudio.setOGGFile(audio.getOGGFile());
            copyAudio.setToObserverController(audio.getToObserverController());
            feedback.setFeedbackPresentation(copyAudio);
        }
    }
    
    /**
     * Sends the strategy to the monitor module with the feedback content intended for the observer controller
     * 
     * @param strategy the strategy containing the feedback for the controller
     * @param bds the base domain session
     * @param evaluator the username of the person who initiated the strategy. Can be
     *        null if request was made automatically by GIFT.
     * @param reason the reason for the strategy being activated. Can be null.
     * @param isScenarioSupport whether this strategy is associated with a scenario element like a task trigger versus a 
     * strategy that is in the set of strategies for the pedagogical module to request
     */
    private void sendActivityToObserverController(Strategy strategy, BaseDomainSession bds, String evaluator, String reason, boolean isScenarioSupport) {
        Set<String> attachedWebMonitors = domainSessionIdToWebMonitorInboxes.get(bds.getDomainSessionInfo().getDomainSessionId());
        
        // there are no game master clients right now
        if(CollectionUtils.isEmpty(attachedWebMonitors)){
            return;
        }

        // Create the request
        ExecuteOCStrategy request = new ExecuteOCStrategy(strategy, evaluator, reason);
        request.setScenarioSupport(isScenarioSupport);

        // Send the request
        for(String destination : attachedWebMonitors) {
            sendDomainSessionMessage(
                    destination,
                    request,
                    bds.getDomainSessionInfo(),
                    bds.getDomainSessionInfo().getDomainSessionId(),
                    bds.getDomainSessionInfo().getExperimentId(),
                    MessageTypeEnum.EXECUTE_OC_STRATEGY,
                    null);
        }
    }

    /**
     * Handles the experiment course request by:
     *
     * 1) retrieves experiment details from UMS 2) validates experiment course
     * 3) creates experiment subject entry in UMS 4) starts the domain selection
     * request logic that ultimately starts the course
     *
     * @param experimentCourseRequestMessage
     *            contains the experiment details needed to start the subject's
     *            course experience.
     */
    private void handleExperimentCourseRequest(final Message experimentCourseRequestMessage) {

        ExperimentCourseRequest request = (ExperimentCourseRequest) experimentCourseRequestMessage.getPayload();
        if (logger.isInfoEnabled()){
            logger.info("Handling experiment course request of " + request);
        }

        if(DomainModuleProperties.getInstance().getLessonLevel() == LessonLevelEnum.RTA){
            // experiments not supported (yet)
            logger.warn("A request to start an experiment course was received while the configuration is "+LessonLevelEnum.RTA+
                    ".\n"+experimentCourseRequestMessage);
            NACK nack = new NACK(ErrorEnum.OPERATION_FAILED,
                    "Unable to start an experiment course while in the "+LessonLevelEnum.RTA+" configuration.");
            nack.setErrorHelp("Please contact the Experiment Administrator for more details.");
            sendReply(experimentCourseRequestMessage, nack, MessageTypeEnum.NACK);
        }

        ExperimentCourseRequestHandler handler = new ExperimentCourseRequestHandler(experimentCourseRequestMessage,
                request);
        handler.start(); // releases the calling thread
    }

    /**
     * Handles a request for Domain Options by sending back all the domain
     * options that this domain module has.
     *
     * Note: this will only happen in Simple mode (or in desktop mode if the simple login page is being accessed).
     *
     * @param domainOptionsMessage
     *            The domain options request
     */
    private void handleDomainOptionsRequest(final Message domainOptionsMessage) {

        // information useful when querying other modules for further info about
        // the user
        final UserSession userSession = ((UserSessionMessage) domainOptionsMessage).getUserSession();
        int userId = userSession.getUserId();
        String lmsUsername = ((DomainOptionsRequest) domainOptionsMessage.getPayload()).getLMSUserName();

        try {
            // the course handlers for each course found in the Domain folder
            long preRefreshEpoch = System.currentTimeMillis();
            refreshCourseList(userId, false);
            long postRefreshEpoch = System.currentTimeMillis();
            if (logger.isDebugEnabled()){
                logger.debug("It took " + (postRefreshEpoch - preRefreshEpoch) + " ms to refresh the course list.");
            }
        } catch (DetailedException e) {
            logger.error("Caught exception while trying to refresh the course list for a domain options request of\n"
                    + domainOptionsMessage, e);

            // still need to send a response
            NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, e.getReason());
            nack.setErrorHelp(e.getDetails());
            sendReply(domainOptionsMessage, nack, MessageTypeEnum.PROCESSED_NACK);

            return;
        }

        try {

            //
            // Check for Internet connection for course validation purposes
            //
            final InternetConnectionStatusEnum connectionStatus = UriUtil.getInternetStatus();

            final long preLMSRetrieveEpoch = System.currentTimeMillis();

            // where to put the various survey validation requests for each
            // course, mapped by course name
            final Map<String, List<SurveyCheckRequest>> surveyValidationRequests = new HashMap<>();

            // used to handle a response to the LMS records request message
            // ...in this case, the LMS records will be used to update/filter
            // the course list
            MessageCollectionCallback lmsRecordsCallback = new MessageCollectionCallback() {

                // response received for request
                LMSCourseRecords courseRecords;

                @Override
                public void success() {

                    try {

                        long postLMSRetrieveEpoch = System.currentTimeMillis();
                        if (logger.isInfoEnabled()){
                            logger.info("It took " + (postLMSRetrieveEpoch - preLMSRetrieveEpoch)
                                + " ms to retrieve LMS records for " + userSession + ".");
                        }
                        final long preValidationEpoch = System.currentTimeMillis();
                        long buildSurveyValidationTotalTime = 0;

                        boolean shouldBuildSurveyRequest;

                        CourseOptionsWrapper courseOptionsWrapper = simpleModeCourseOptions
                                .getCourseOptionsWrapper(userId);

                        DomainCourseFileHandler.updateCourseOptionsByLMSRecords(courseOptionsWrapper, courseRecords);

                        DesktopFolderProxy domainFolderProxy = new DesktopFolderProxy(
                                new File(DomainModuleProperties.getInstance().getDomainDirectory()));
                        //
                        // Check the domain options (i.e. courses) available
                        //
                        for (DomainOption option : courseOptionsWrapper.domainOptions.values()) {

                            shouldBuildSurveyRequest = true; // default to
                                                             // always check
                                                             // survey request

                            FileProxy courseFileProxy = courseOptionsWrapper.courseFileNameToFileMap
                                    .get(option.getDomainId());

                            try {
                                DomainCourseFileHandler handler = courseOptionsWrapper.courseFileNameToHandler
                                        .get(option.getDomainId());
                                if (handler == null) {
                                    handler = new DomainCourseFileHandler(courseFileProxy, domainFolderProxy, true);
                                    courseOptionsWrapper.courseFileNameToHandler.put(option.getDomainName(), handler);
                                }

                                handler.setCurrentInternetStatus(connectionStatus);

                                // validate the course against gift logic
                                if (DomainModuleProperties.getInstance()
                                        .shouldValidateCourseContentAtCourseListRequest()) {

                                    CourseValidationResults validationResults = handler.checkCourse(true, null);
                                    DetailedExceptionSerializedWrapper criticalIssue = validationResults.getCriticalIssue();
                                    if(criticalIssue != null){
                                        //there was a critical validation issue, set unavailable recommendation and continue
                                        //on with the next course in the list

                                        logger.error(
                                                "There was a validation issue with domain option of " + option
                                                        + " while trying to gather possible domain options to check.",
                                                        criticalIssue);

                                        DomainOptionRecommendation recommendation = new DomainOptionRecommendation(
                                                DomainOptionRecommendationEnum.UNAVAILABLE_OTHER);
                                        recommendation.setReason("Failed to successfully validate the course.");
                                        recommendation.setDetails(criticalIssue.getMessage());
                                        option.setDomainOptionRecommendation(recommendation);
                                        continue;
                                    }
                                }

                                if (shouldBuildSurveyRequest && DomainModuleProperties.getInstance()
                                        .shouldValidateSurveyAtCourseListRequest()) {

                                    // build survey validation requests
                                    long preTime = System.currentTimeMillis();
                                    List<SurveyCheckRequest> handlerRequests = handler.buildSurveyValidationRequest();
                                    buildSurveyValidationTotalTime += System.currentTimeMillis() - preTime;

                                    if (!handlerRequests.isEmpty()) {
                                        // if there are no survey checks to be
                                        // performed, don't bother sending an
                                        // object to be checked.

                                        surveyValidationRequests.put(option.getDomainId(), handlerRequests);
                                    }
                                }

                            } catch (FileValidationException exception) {
                                logger.error(
                                        "Caught specific exception with domain option of " + option
                                                + " while trying to gather possible domain options to check.",
                                        exception);

                                DomainOptionRecommendation recommendation = new DomainOptionRecommendation(
                                        DomainOptionRecommendationEnum.UNAVAILABLE_OTHER);

                                // add information about an Internet connection
                                // issue for this course
                                if (exception.getCause() instanceof ConnectException) {
                                    recommendation.setReason(
                                            "Failed to successfully validate the course due to not being able to connection to a network resource (e.g. website)."
                                                    + "  If you are running GIFT without a network/internet connection then please provide an appropriate network connection for this course to be available.");
                                } else {
                                    recommendation.setReason("Failed to successfully validate the course.");
                                    recommendation.setDetails(exception.getMessage());
                                }
                                option.setDomainOptionRecommendation(recommendation);

                            } catch (Exception e) {
                                logger.error("Caught (general) exception with domain option of " + option
                                        + " while trying to gather possible domain options to check.", e);

                                DomainOptionRecommendation unavailable = new DomainOptionRecommendation(
                                        DomainOptionRecommendationEnum.UNAVAILABLE_OTHER);
                                unavailable.setReason("Failed to successfully validate the course.");
                                unavailable.setDetails(e.getMessage());
                                option.setDomainOptionRecommendation(unavailable);
                            }
                        }

                        long postValidationEpoch = System.currentTimeMillis();
                        if (logger.isDebugEnabled()){
                            logger.debug("It took " + (postValidationEpoch - preValidationEpoch)
                                + " ms to validate and organize the courses for " + userSession + ".  Of that it took "
                                + buildSurveyValidationTotalTime + " ms to build the survey validation request.");
                        }

                        if (surveyValidationRequests.isEmpty()) {
                            // there are no surveys to validate, send the course
                            // options as they are now

                            List<DomainOption> options = new ArrayList<>(
                                    courseOptionsWrapper.domainOptions.values());
                            Collections.sort(options);
                            DomainOptionsList optionsList = new DomainOptionsList(options);
                            sendReply(domainOptionsMessage, optionsList, MessageTypeEnum.DOMAIN_OPTIONS_REPLY);

                        } else {
                            if (logger.isDebugEnabled()){
                                logger.debug("Sending survey request at epoch of " + System.currentTimeMillis() + ".");
                            }
                            sendUserSessionMessage(new SurveyListCheckRequest(surveyValidationRequests), userSession,
                                    MessageTypeEnum.SURVEY_CHECK_REQUEST, surveyValidationCallback);
                        }

                    } catch (Throwable t) {
                        // catch all
                        logger.error("Caught exception while trying to process domain options request.", t);
                        sendReply(domainOptionsMessage,
                                new NACK(ErrorEnum.OPERATION_FAILED,
                                        "Failed to retrieve course list because an exception was thrown."),
                                MessageTypeEnum.NACK);
                    }
                }

                @Override
                public void received(Message msg) {

                    if (msg.getPayload() instanceof LMSData) {
                        courseRecords = ((LMSData) msg.getPayload()).getCourseRecords();
                    }
                }

                @Override
                public void failure(String why) {
                    // error, send NACK

                    logger.error("There was a problem with the LMS history request for " + userSession + " because '"
                            + why + "'.");
                    sendReply(domainOptionsMessage, new NACK(ErrorEnum.OPERATION_FAILED,
                            "Failed to retrieve LMS history because '" + why + "'."), MessageTypeEnum.NACK);
                }

                @Override
                public void failure(Message msg) {
                    // error, send NACK

                    logger.error("There was a problem with the LMS history request for " + userSession
                            + " because the message " + msg + " was received.");
                    sendReply(domainOptionsMessage,
                            new NACK(ErrorEnum.OPERATION_FAILED,
                                    "Failed to retrieve LMS history because '"
                                            + ((NACK) msg.getPayload()).getErrorMessage()
                                            + "' in response to the LMS history Request."),
                            MessageTypeEnum.NACK);
                }

                // used to handle a response to the survey validation request
                // message
                // ...in this case, the results of the validation will be used
                // to update/filter the course list
                final MessageCollectionCallback surveyValidationCallback = new MessageCollectionCallback() {

                    SurveyCheckResponse surveyCheckResponse;

                    @Override
                    public void success() {

                        if (surveyCheckResponse != null) {
                            // check response and remove any domains that have
                            // references to survey elements not found

                            CourseOptionsWrapper courseOptionsWrapper = simpleModeCourseOptions
                                    .getCourseOptionsWrapper(userId);

                            if (logger.isDebugEnabled()){
                                logger.debug(
                                    "Processing survey check response at epoch of " + System.currentTimeMillis() + ".");
                            }
                            DomainCourseFileHandler.updateCourseOptionsBySurveyCheck(courseOptionsWrapper,
                                    surveyCheckResponse, new SurveyListCheckRequest(surveyValidationRequests));

                            List<DomainOption> options = new ArrayList<>(
                                    courseOptionsWrapper.domainOptions.values());
                            Collections.sort(options);
                            DomainOptionsList optionsList = new DomainOptionsList(options);
                            sendReply(domainOptionsMessage, optionsList, MessageTypeEnum.DOMAIN_OPTIONS_REPLY);

                        } else {
                            // error, send NACK
                            sendReply(domainOptionsMessage,
                                    new NACK(ErrorEnum.OPERATION_FAILED,
                                            "Failed to validate survey refrences in the possible domain options because a Survey Check Response was not received."),
                                    MessageTypeEnum.NACK);
                        }

                    }

                    @Override
                    public void received(Message msg) {

                        if (msg.getPayload() instanceof SurveyCheckResponse) {
                            surveyCheckResponse = (SurveyCheckResponse) msg.getPayload();
                        }

                    }

                    @Override
                    public void failure(String why) {
                        // error, send NACK

                        logger.error("There was a problem with the survey validation request for " + userSession
                                + " because '" + why + "'.");
                        sendReply(domainOptionsMessage,
                                new NACK(ErrorEnum.OPERATION_FAILED,
                                        "Failed to validate survey refrences in the possible domain options because '"
                                                + why + "'."),
                                MessageTypeEnum.NACK);
                    }

                    @Override
                    public void failure(Message msg) {
                        // error, send NACK

                        logger.error("There was a problem with the survey validation request for " + userSession
                                + " because the message " + msg + " was received.");
                        sendReply(domainOptionsMessage,
                                new NACK(ErrorEnum.OPERATION_FAILED,
                                        "Failed to validate survey refrences in the possible domain options because '"
                                                + ((NACK) msg.getPayload()).getErrorMessage()
                                                + "' in response to the Survey Check Request."),
                                MessageTypeEnum.NACK);
                    }
                };

            };

            if (StringUtils.isNotBlank(lmsUsername)
                    && DomainModuleProperties.getInstance().shouldApplyLMSRecordsAtCourseListRequest()) {

                //
                // get a LMS module client connection
                //
                MessageCollectionCallback lmsAllocationCallback = new MessageCollectionCallback() {

                    @Override
                    public void success() {
                        sendUserSessionMessage(new LMSDataRequest(lmsUsername), userSession,
                                MessageTypeEnum.LMS_DATA_REQUEST, lmsRecordsCallback);
                    }

                    @Override
                    public void received(final Message aMsg) {

                    }

                    @Override
                    public void failure(Message nackMsg) {
                        lmsRecordsCallback.failure(nackMsg);
                    }

                    @Override
                    public void failure(String why) {
                        lmsRecordsCallback.failure(why);
                    }
                };

                selectModule(userSession, ModuleTypeEnum.LMS_MODULE, lmsAllocationCallback);

            } else {
                lmsRecordsCallback.success();
            }

        } catch (Exception e) {
            logger.error("Caught exception while trying to build the course list.", e);

            // still need to send a response
            NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "There was a problem trying to build the course list.");
            nack.setErrorHelp("The error reads: " + e.getMessage());
            sendReply(domainOptionsMessage, nack, MessageTypeEnum.PROCESSED_NACK);
        }

    }

    /**
     * Handles a Domain Selection request from the TUI and sends that onto the
     * UMS. If that has been acknowledged then the TUI is notified and a Domain
     * Session is created.
     *
     * @param domainSelectionRequestMsg
     *            The Domain selection request message
     */
    private void handleDomainSelectionRequest(final Message domainSelectionRequestMsg) {

        /* If no domain runtime id was provided and GIFT is in RTA mode, the
         * course still needs to be loaded into the runtime folder. */
        final boolean isRTA = LessonLevelEnum.RTA.equals(DomainModuleProperties.getInstance().getLessonLevel());
        if (isRTA) {
            final DomainSelectionRequest request = (DomainSelectionRequest) domainSelectionRequestMsg.getPayload();
            String domainRuntimeId = request.getDomainRuntimeId();

            final CourseOptionsWrapper courseWrapper = new CourseOptionsWrapper();
            final ProgressIndicator progress = new ProgressIndicator();
            final String lmsUsername = request.getLmsUsername();

            try {
                domainRuntimeId = FileServices.getInstance().getFileServices().loadCourse(lmsUsername,
                        request.getDomainSourceId(), courseWrapper, progress, lmsUsername);
                request.setDomainRuntimeId(domainRuntimeId);
            } catch (Exception e) {
                logger.error("There was a problem loading the course specified in " + request, e);
                sendReply(domainSelectionRequestMsg, new NACK(ErrorEnum.OPERATION_FAILED,
                        "There was a problem loading the course: " + e.getMessage()),
                        MessageTypeEnum.NACK);
            }
        }

        // keep original callback
        MessageCollectionCallback callback = new MessageCollectionCallback() {

            UserSessionMessage domainSelectionReply = null;
            
            @Override
            public void success() {

                if(domainSelectionReply != null){
                    // need to establish connections to all the needed modules
                    // for a domain session
                    DomainSessionModuleAllocation moduleAllocationThread = new DomainSessionModuleAllocation(
                            domainSelectionReply, (UserSessionMessage) domainSelectionRequestMsg);
                    moduleAllocationThread.start();
                }else{
                    failure("The domain selection request message callback was notified of completion but never received the proper response.");
                }
            }

            @Override
            public void received(final Message aMsg) {
                if (aMsg.getMessageType() == MessageTypeEnum.DOMAIN_SELECTION_REPLY) {
                    domainSelectionReply = (UserSessionMessage) aMsg;
                }
            }

            @Override
            public void failure(Message msg) {
                
                NACK nack = null;
                if(msg.getMessageType().equals(MessageTypeEnum.PROCESSED_NACK)){
                    nack = (NACK) msg.getPayload();
                }else{
                    nack = new NACK(ErrorEnum.OPERATION_FAILED, "Received unhandled "+msg.getMessageType()+" failure type message from "+msg.getSenderModuleName()+" when processing the course start request.\n"+msg);
                }
                
                DomainModule.this.sendReply(domainSelectionRequestMsg,
                        nack,
                        MessageTypeEnum.PROCESSED_NACK);
            }

            @Override
            public void failure(String why) {
                DomainModule.this.sendReply(domainSelectionRequestMsg, new NACK(ErrorEnum.OPERATION_FAILED, why),
                        MessageTypeEnum.PROCESSED_NACK);
            }
        };

        // send the domain selection request to the UMS module to get the domain
        // session id
        sendUserSessionMessage(ModuleTypeEnum.UMS_MODULE, domainSelectionRequestMsg.getPayload(), ((UserSessionMessage) domainSelectionRequestMsg).getUserSession(),
                MessageTypeEnum.DOMAIN_SELECTION_REQUEST, callback);
    }

    /**
     * Forwards a domain selection reply message to a destination and starts a
     * new Domain Session once the reply has been acknowledged.
     *
     * @param domainSelectionReply
     *            The reply to forward
     * @param domainSelectionRequest
     *            The request that originated the message sequence
     * @param clientInfo
     *            information about the tutor client
     */
    private void forwardDomainSelectionReply(final Message domainSelectionReply, final Message domainSelectionRequest,
            final WebClientInformation clientInfo) {

        final DomainSelectionRequest request = (DomainSelectionRequest) domainSelectionRequest.getPayload();
        final DomainSession domainSession = (DomainSession) domainSelectionReply.getPayload();

        try {
            FileProxy courseFileProxy = null;
            // Order matters here since experiment mode takes priority over simpleModeCourseOptions.
            // Because of the fact that simple mode can work in desktop mode, it is important to look at examining the
            // experiment id first since that takes priority.
            boolean isLegacyExperiment = false;
            if (domainSession.getExperimentId() != null) {
                // the domain session is for an experiment
                if(logger.isInfoEnabled()){
                    logger.info("Creating domain session for subject in experiment " + domainSession.getExperimentId()
                            + " with course in Experiments directory of '" + request.getDomainRuntimeId() + "'.");
                }

                try {
                    /* Check legacy first */
                    File courseFile = new File(legacyExperimentDirectory.getFolder(), request.getDomainRuntimeId());
                    courseFileProxy = new FileProxy(courseFile);
                    isLegacyExperiment = true;
                } catch (@SuppressWarnings("unused") Exception e) {
                    /* the experiment course folder doesn't exist in the
                     * Experiments directory of the Domain folder, check in the
                     * runtime experiments folder */
                    File courseFile = new File(experimentDirectory, request.getDomainRuntimeId());
                    courseFileProxy = new FileProxy(courseFile);
                }
            } else if (simpleModeCourseOptions != null
                    && simpleModeCourseOptions.containsUser(domainSession.getUserId())
                    && !LessonLevelEnum.RTA.equals(DomainModuleProperties.getInstance().getLessonLevel())) {
                // the only way this should be in map is if this in Simple mode
                // because that mode uses the refreshCourseList method in this
                // class.
                CourseOptionsWrapper courseOptionsWrapper = simpleModeCourseOptions
                        .getCourseOptionsWrapper(domainSession.getUserId());
                courseFileProxy = courseOptionsWrapper.courseFileNameToFileMap.get(request.getDomainRuntimeId());
                if(logger.isInfoEnabled()){
                    logger.info("Found '" + courseFileProxy + "' file for domain id of '" + request.getDomainRuntimeId()
                            + "' in map of " + courseOptionsWrapper.courseFileNameToFileMap.size() + " entries.");
                }
            } else {
                if(logger.isInfoEnabled()){
                    logger.info("Creating domain session for user with course in runtime directory of '"
                            + request.getDomainRuntimeId() + "'.");
                }
                File courseFile = new File(
                        runtimeDirectory.getFileId() + File.separator + request.getDomainRuntimeId());
                courseFileProxy = new FileProxy(courseFile);
            }

            DomainSession ds = new DomainSession(domainSession.getDomainSessionId(), domainSession.getUserId(),
                    request.getDomainRuntimeId(), request.getDomainSourceId());
            ds.copyFromUserSession(domainSession);

            /* Need to check for legacy experiments because the there is no true
             * authored directory since it was copied to the Experiments folder.
             * Use the copied course in Experiments as the authored
             * directory. */
            AbstractFolderProxy courseAuthoredDirectory;
            if (isLegacyExperiment) {
                /* Create a new folder proxy, but the authored and runtime are
                 * the same for legacy experiments */
                courseAuthoredDirectory = new DesktopFolderProxy(new File(courseFileProxy.getFileId()).getParentFile());
            } else {
                /* Not a legacy experiment course, retrieve the authored course
                 * directory */
                courseAuthoredDirectory = FileServices.getInstance().getFileServices()
                        .getCourseFolder(ds.getDomainSourceId(), ds.getUsername());
            }

            final BaseDomainSession baseDomainSession = new BaseDomainSession(courseFileProxy, courseAuthoredDirectory, ds,
                    request.getLmsUsername(), request.getClientInformation(), request.getRuntimeParams());

            domainSessionIdToDomainSession.put(domainSession.getDomainSessionId(), baseDomainSession);
            //
            // update metrics - NOTE: need to do this next to the above domain session map since the metric reporting
            //                        is using the map to gather and in a case where initialize() causes a close domain session
            //                        the experiment count needs to have been incremented before decrementing for this session
            //
            metrics.updateMetricCounter(NUM_DOMAIN_SESSIONS_METRIC, domainSessionIdToDomainSession.size());
            if(domainSession.getExperimentId() != null){
                metrics.incrementMetricCounter(NUM_EXPERIMENT_DOMAIN_SESSIONS_METRIC);
            }

            baseDomainSession.initialize();

            if (baseDomainSession.isActive()) {
                // it is possible that some other process is attempting to close
                // the domain session, therefore
                // don't send an ACK here

                // keep original callback
                MessageCollectionCallback callback = new MessageCollectionCallback() {

                    @Override
                    public void success() {

                        try {
                            baseDomainSession.start();
                        } catch (Exception e) {
                            logger.error(
                                    "Caught exception while trying to initialize a new domain session for domain name of '"
                                            + request.getDomainRuntimeId() + "'.",
                                    e);

                            baseDomainSession.onSessionError(e);
                        }
                    }

                    @Override
                    public void received(Message aMsg) {
                        // Do nothing
                    }

                    @Override
                    public void failure(Message nackMsg) {

                        if(logger.isInfoEnabled()){
                            logger.info(
                                    "Release domain session modules because of the tutor module sending a failure to the domain selection reply with message of "
                                            + nackMsg);
                        }
                        releaseDomainSessionModules(domainSession);
                    }

                    @Override
                    public void failure(String why) {

                        if(logger.isInfoEnabled()){
                            logger.info(
                                    "Release domain session modules because of the tutor module sending a failure to the domain selection reply with reason of "
                                            + why);
                        }
                        releaseDomainSessionModules(domainSession);
                    }
                };

                // reply back to the Tutor module who sent the original request
                sendReply(domainSelectionRequest, domainSession, MessageTypeEnum.DOMAIN_SELECTION_REPLY, callback);
            } else {
                logger.warn(
                        "While initializing the domain session some other process de-activated the domain session.  Replying with P-NACK to original request of "
                                + domainSelectionRequest);

                // send P-NACK to tutor, reply to request
                sendReply(domainSelectionRequest,
                        new NACK(ErrorEnum.OPERATION_FAILED,
                                "Another process de-activated the domain session while trying to initialize and start the course named '"
                                        + request.getDomainRuntimeId() + "'."),
                        MessageTypeEnum.PROCESSED_NACK);

                if(logger.isInfoEnabled()){
                    logger.info("Release domain session modules because of failure to initialize the domain session.");
                }
                releaseDomainSessionModules(domainSession);

                closeDomainSession(domainSession);
            }

        } catch (Throwable e) {
            logger.error("Caught exception while trying to initialize the domain session for domain name of '"
                    + request.getDomainRuntimeId() + "'.", e);

            // send P-NACK to tutor, reply to request
            NACK nack = new NACK(ErrorEnum.OPERATION_FAILED,
                    "Failed to start the course named '" + request.getDomainRuntimeId() + "'.");
            String message = e.getMessage();
            if(StringUtils.isBlank(message)){
                message = e.getClass().getName();
            }
            
            Throwable cause = e.getCause();
            String causeStr;
            if(cause == null){
                StackTraceElement[] elements = e.getStackTrace();
                if(elements.length > 0){
                    causeStr = elements[0].getFileName() + ":" + elements[0].getLineNumber();
                }
                causeStr = "(unable to get cause)";
            }else{
                causeStr = cause.toString();
            }
            nack.setErrorHelp("An exception occurred that reads:\n" + message + "\nCause:\n" + causeStr);
            sendReply(domainSelectionRequest, nack, MessageTypeEnum.PROCESSED_NACK);

            if(logger.isInfoEnabled()){
                logger.info("Release domain session modules because of failure to initialize the domain session.");
            }
            releaseDomainSessionModules(domainSession);

            closeDomainSession(domainSession);
        }
    }

    /**
     * Display a course initialization page in the Tutor User Interface.
     *
     * @param domainSession
     *            The domain session information of the session requesting.
     * @param request
     *            The course initialization information.
     * @param callback
     *            The message callback handler.
     */
    public void sendDisplayCourseInitInstructionsRequest(DomainSession domainSession,
            DisplayCourseInitInstructionsRequest request, MessageCollectionCallback callback) {

        final BaseDomainSession bDomainSession = domainSessionIdToDomainSession.get(domainSession.getDomainSessionId());

        if (bDomainSession != null) {

            sendDomainSessionMessage(request, domainSession, domainSession.getDomainSessionId(),
                    MessageTypeEnum.DISPLAY_COURSE_INIT_INSTRUCTIONS_REQUEST, callback);
        } else {
            logger.error("Could not display course initialization instructions in the TUI: " + domainSession
                    + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Send a message to the tutor that the experiment subject has been created.
     *
     * @param subjectCreated
     *            contain useful information needed by the tutor module to help
     *            create the web session objects for a subject in an experiment
     * @param userSession
     *            contains information about the user session
     * @param callback
     *            used to notify the caller of a reply or problem to this
     *            message being sent
     */
    public void sendExperimentSubjectCreatedMessage(SubjectCreated subjectCreated, UserSession userSession,
            MessageCollectionCallback callback) {
        sendUserSessionMessage(subjectCreated, userSession, MessageTypeEnum.SUBJECT_CREATED, callback);
    }

    /**
     * Notify the gateway that the learner has selected an answer to a survey
     * question.
     *
     * @param questionResponse
     *            contains the response to a survey question
     * @param domainSession
     *            the session the response was made in
     */
    public void sendTutorSurveyQuestionResponse(AbstractQuestionResponse questionResponse,
            DomainSession domainSession) {
        sendDomainSessionMessage(ModuleTypeEnum.GATEWAY_MODULE, questionResponse, domainSession,
                domainSession.getDomainSessionId(), MessageTypeEnum.TUTOR_SURVEY_QUESTION_RESPONSE, null);
    }

    /**
     * Creates the tutor topic at which a domain session will receive messages from
     * all of its embedded training applications.
     *
     * @param topicId The unique identifier for this domain session's tutor topic
     */
    public void createEmbeddedTrainingAppTopic(String topicId) {
        String topicName = SubjectUtil.TUTOR_TOPIC_PREFIX + ADDRESS_TOKEN_DELIM + topicId;
        if(!haveSubjectClient(topicName)) {
            registerEmbeddedTrainingAppGameStateMessageHandler(topicName, trainingAppMessageHandler);
        }
    }

    /**
     * Handles the message to kill this module
     *
     * @param message
     *            - The Kill Module message
     */
    private void handleKillModuleMessage(Message message) {

        if (logger.isInfoEnabled()){
            logger.info("Received kill module message from " + message.getSenderModuleName());
        }

        Thread killModule = new Thread("Kill Module") {
            @Override
            public void run() {
                killModule();
            }
        };
        killModule.start();
    }

    /**
     * Handle an incoming learner action that was done by the learner in the tutor.
     * 
     * @param learnerTutorActionMessage contains information about the learner action taken in the tutor.
     */
    private void handleLearnerTutorActionMessage(Message learnerTutorActionMessage) {
        
        // if the learner action is from a joiner session, the message needs to be re-routed to the host session
        // cause the joiner sessions no longer have a knowledge session actively running
        if (learnerTutorActionMessage instanceof DomainSessionMessage) {
            
            DomainSessionMessage dsMsg = (DomainSessionMessage)learnerTutorActionMessage;
            LearnerTutorAction learnerTutorAction = (LearnerTutorAction)dsMsg.getPayload();
            
            //add team member info (if available) for the learner causing the learner action
            if(learnerTutorAction.getAction().getTeamMember() == null){
                TeamMember<?> teamMember = KnowledgeSessionManager.getInstance().getTeamMemberForDomainSession(dsMsg.getDomainSessionId());
                if(teamMember != null){
                    if(logger.isDebugEnabled()){
                        logger.debug("Setting learner action team member for learner action message to "+teamMember+".\n"+learnerTutorActionMessage);
                    }
                    learnerTutorAction.getAction().setTeamMember(teamMember);
                }
            }
            
            Integer hostDsId = KnowledgeSessionManager.getInstance()
                    .isMemberOfTeamKnowledgeSession(dsMsg.getDomainSessionId());
            if(hostDsId != null && !(learnerTutorAction.getAction() instanceof FinishScenario)){
                // the learner action came from a joiner session and is NOT a FinishScenario action
                // FinishScenario actions should not be passed to the host, as those will allow a joiner to end the course for all learners.
                
                if(logger.isDebugEnabled()){
                    logger.debug("Changing learner tutor action message domain session id from "+dsMsg.getDomainSessionId()+" of the joiner to "+hostDsId+" of the host.");
                }
                
                DomainSessionMessage dsMsgCopy = dsMsg.copyNewDomainSessionId(hostDsId);
                handleTrainingAppGameStateMessage(dsMsgCopy); 

                
                // reply to original version of the message
                sendReply(learnerTutorActionMessage, new ACK(), MessageTypeEnum.PROCESSED_ACK);
                return;
            }
            
        }        
        
        // the host of an individual or team session caused the learner action message to be sent
        handleTrainingAppGameStateMessage(learnerTutorActionMessage);        
        sendReply(learnerTutorActionMessage, new ACK(), MessageTypeEnum.PROCESSED_ACK);
    }

    /**
     * Pass the chat log message from the tutor to the simulation message domain
     * assessment path.
     *
     * @param chatLogMessage the current conversation log
     */
    private void handleChatLogMessage(Message chatLogMessage) {
        handleTrainingAppGameStateMessage(chatLogMessage);
        sendReply(chatLogMessage, new ACK(), MessageTypeEnum.PROCESSED_ACK);
    }

    /**
     * Checks to see if the given domain session id is mapped to an existing domain session.
     *
     * @param domainSessionId the domain session id.
     * @return true if the given domain session id maps to an existing domain session; false
     *         otherwise.
     */
    public boolean domainSessionExists(Integer domainSessionId) {
        return domainSessionIdToDomainSession.containsKey(domainSessionId);
    }

    /**
     * Closes the domain session gracefully and presents the user with an error message page. This
     * method should only be used if the close was caused by logic in the domain module that is
     * outside of the BaseDomainSession manage logic. If another module wishes to close the domain
     * session the DomainModule should handle that in handleCloseDomainSessionRequest.
     *
     * @param domainSessionId the domain session id.
     * @param reason the reason why the domain session is being requested to close.
     * @param details the details behind why the domain session is closing due to an error.
     */
    public void closeDomainSessionWithError(Integer domainSessionId, String reason, String details) {
        final BaseDomainSession domainSessionData = domainSessionIdToDomainSession.get(domainSessionId);
        if (domainSessionData != null) {
            // show the error message
            domainSessionData.terminateSession(true, "The course has been terminated due to the following reason: '" + reason + "'", details);
        } else {
            logger.error("Unable to find a domain session instance for: Domain Session ID " + domainSessionId
                    + " which is needed to gracefully close the domain session. The course attempted to close due to the following reason: '" + reason
                    + "' with details: '" + details + "'");
        }
    }

    /**
     * An externally created close domain session message signals the forceful
     * end of a domain session, therefore handle it by terminating the session
     * with notification.
     *
     * @param msg the request from another module to close the domain session.
     */
    private void handleCloseDomainSessionRequest(final Message msg) {
        final DomainSessionMessage domainSessionMsg = (DomainSessionMessage) msg;
        final BaseDomainSession domainSessionData = domainSessionIdToDomainSession
                .get(domainSessionMsg.getDomainSessionId());
        final CloseDomainSessionRequest closeDomain = (CloseDomainSessionRequest) msg.getPayload();
        final String reason = closeDomain.getReason();
        if (domainSessionData != null) {

            boolean displayMessageToUser = msg.getSenderModuleType() != ModuleTypeEnum.TUTOR_MODULE;

            final String details = "A process outside of the Domain module has requested that the domain session be closed.";

            String displayReason;
            if (reason == null || reason.isEmpty()) {
                displayReason = "The course has been terminated";
            } else {
                // show the reason message
                displayReason = "The course has been terminated due to the following reason: '" + reason + "'";
            }

            final String message = domainSessionData.buildDefaultTerminationShutdownDisplayErrorMessage(displayReason, details);
            /* Terminates the session and sends the Processed ACK/NACK once the
             * other modules have successfully or unsuccessfully closed their
             * domain sessions. This allows the module that sent the
             * CloseDomainSessionRequest message to make sure all modules have
             * closed their domain sessions before it proceeds with its logic.
             * Specifically this is used by the TutorModule so that an embedded
             * application can finish sending and receiving all messages before
             * being unloaded by the logout procedure */
            domainSessionData.terminateSession(false, displayReason, details, LessonCompletedStatusType.CONTROLLER_ENDED_LESSON, new MessageCollectionCallback() {

                @Override
                public void success() {

                    // Note: before #3545 this if/else logic was not in this success method but was in BaseDomainSession
                    //       Before the P'ACK would only be sent after all the termination logic was finished but now
                    //       it is sent before the following code is finished.  If there is a problem in the future
                    //       than a callback will most likely need to be added to the terminate method calls below.
                    try{
                        if (displayMessageToUser) {
                            domainSessionData.terminationShutdownDisplayError(message);
                        } else {
                            domainSessionData.terminationShutdown();
                        }
                    }catch(Exception e){
                        logger.error("Caught exception while trying to finish terminating the domain session " +domainSessionData+" because "+reason, e);
                    }

                    sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);
                }

                @Override
                public void received(Message msg) {
                    // Do nothing
                }

                @Override
                public void failure(Message msg) {

                    // Note: before #3545 this if/else logic was not in this success method but was in BaseDomainSession
                    //       Before the P'ACK would only be sent after all the termination logic was finished but now
                    //       it is sent before the following code is finished.  If there is a problem in the future
                    //       than a callback will most likely need to be added to the terminate method calls below.
                    try{
                        if (displayMessageToUser) {
                            domainSessionData.terminationShutdownDisplayError(message);
                        } else {
                            domainSessionData.terminationShutdown();
                        }
                    }catch(Exception e){
                        logger.error("Caught exception while trying to finish terminating the domain session " +domainSessionData+" because "+reason, e);
                    }

                    sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "The Domain Module encountered a problem while handling a CloseDomainSessionRequest, the following message received a NACK: " + msg), MessageTypeEnum.PROCESSED_NACK);
                }

                @Override
                public void failure(String why) {

                    // Note: before #3545 this if/else logic was not in this success method but was in BaseDomainSession
                    //       Before the P'ACK would only be sent after all the termination logic was finished but now
                    //       it is sent before the following code is finished.  If there is a problem in the future
                    //       than a callback will most likely need to be added to the terminate method calls below.
                    try{
                        if (displayMessageToUser) {
                            domainSessionData.terminationShutdownDisplayError(message);
                        } else {
                            domainSessionData.terminationShutdown();
                        }
                    }catch(Exception e){
                        logger.error("Caught exception while trying to finish terminating the domain session " +domainSessionData+" because "+reason, e);
                    }

                    sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "The Domain Module encountered a problem while handling a CloseDomainSessionRequest: " + why), MessageTypeEnum.PROCESSED_NACK);
                }

            });

        } else {

            logger.error("The "+msg.getSenderModuleType()+" module is requesting to end a domain session but "+
                    "there is no domain session instance for Domain Session ID "
                    + domainSessionMsg.getDomainSessionId()
                    + ". Attempting to send a close domain session request to other modules to allow them to cleanup.");
            
            // simple callback used to notify the module requesting a close domain session that it was processed
            MessageCollectionCallback replyToCloseRequester = new MessageCollectionCallback() {
                
                @Override
                public void success() {
                    sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);
                }
                
                @Override
                public void received(Message msg) {
                    // don't care
                }
                
                @Override
                public void failure(String why) {
                    sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);
                }
                
                @Override
                public void failure(Message msg) {
                    sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);
                }
            };

            DomainSession dSession = new DomainSession(((DomainSessionMessage) msg).getDomainSessionId(),
                    ((DomainSessionMessage) msg).getUserId(), DomainSession.UNKNOWN_DOMAIN_NAME,
                    DomainSession.UNKNOWN_DOMAIN_NAME);
            dSession.copyFromUserSession(domainSessionMsg.getUserSession());
            
            // Note: can't call sendCloseDomainSessionNotToTutor if the domain session isn't known to the domain module
            sendDomainSessionMessage(new ModuleTypeEnum[]{ModuleTypeEnum.LEARNER_MODULE, ModuleTypeEnum.PEDAGOGICAL_MODULE, ModuleTypeEnum.UMS_MODULE, ModuleTypeEnum.SENSOR_MODULE, ModuleTypeEnum.GATEWAY_MODULE}, 
                    new CloseDomainSessionRequest(msg.getSenderModuleType()+" requested to close the domain session."), 
                    dSession, dSession.getDomainSessionId(), dSession.getExperimentId(),
                    MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST, new MessageCollectionCallback() {

                @Override
                public void success() {
                    if (logger.isInfoEnabled()){
                        logger.info(
                            "Continuing cleanup of domain session by requesting the tutor to close the domain session.");
                    }
                    sendToTutor();
                }

                @Override
                public void received(Message msg) {

                }

                @Override
                public void failure(String why) {
                    logger.warn("Although a failure occurred with reason of " + why
                            + ", still continuing cleanup of domain session by requesting the tutor to close the domain session.");
                    sendToTutor();
                }

                @Override
                public void failure(Message msg) {
                    logger.warn("Although a failure occurred with message of " + msg
                            + ", still continuing cleanup of domain session by requesting the tutor to close the domain session.");
                    sendToTutor();
                }

                private void sendToTutor(){

                    if(DomainModuleProperties.getInstance().getLessonLevel() == LessonLevelEnum.COURSE){
                        sendCloseDomainSessionToTutor(domainSessionMsg.getUserSession(),
                                domainSessionMsg.getDomainSessionId(), replyToCloseRequester);
                    }
                }
            });

        }
    }

    /**
     * Respond to a request for active domain sessions by gathering up the list
     * and sending it back.
     *
     * @param msg
     */
    private void handleActiveDomainSessionsRequest(Message msg) {

        // gather all domain sessions in this module
        List<DomainSession> dsList = new ArrayList<>();
        synchronized (domainSessionIdToDomainSession) {
            for (BaseDomainSession baseDS : domainSessionIdToDomainSession.values()) {

                if (baseDS.isActive()) {
                    dsList.add(baseDS.getDomainSessionInfo());
                }
            }
        }

        sendReply(msg, new DomainSessionList(dsList), MessageTypeEnum.ACTIVE_DOMAIN_SESSIONS_REPLY);
    }

    /**
     * Respond to a request for active knowledge sessions by gathering up the list
     * and sending it back.
     *
     * @param msg the request message to reply to
     */
    private void handleActiveKnowledgeSessionsRequest(Message msg) {

        try{
            KnowledgeSessionsRequest request = (KnowledgeSessionsRequest)msg.getPayload();
            if(msg instanceof DomainSessionMessage){

                DomainSessionMessage domainSessionMessage = (DomainSessionMessage)msg;
                BaseDomainSession ds = domainSessionIdToDomainSession.get(domainSessionMessage.getDomainSessionId());
                if (ds != null) {
                    boolean hasGW = ds.getDomainSessionInfo().isGatewayConnected();
                    Map<Integer, AbstractKnowledgeSession> knowledgeSessionMap = KnowledgeSessionManager.getInstance().getKnowledgeSessions(request);
                    KnowledgeSessionsReply reply = new KnowledgeSessionsReply(knowledgeSessionMap);
                    reply.setCanHost(hasGW);
                    sendReply(msg, reply, MessageTypeEnum.ACTIVE_KNOWLEDGE_SESSIONS_REPLY);

                }else{
                    throw new Exception("Unable to find domain session "+domainSessionMessage.getDomainSessionId());
                }
            }else{
                Map<Integer, AbstractKnowledgeSession> knowledgeSessionMap = KnowledgeSessionManager.getInstance().getKnowledgeSessions(request);
                KnowledgeSessionsReply reply = new KnowledgeSessionsReply(knowledgeSessionMap);
                sendReply(msg, reply, MessageTypeEnum.ACTIVE_KNOWLEDGE_SESSIONS_REPLY);
            }
        }catch(Exception e){
            logger.error("Caught exception while trying to send the current knowledge sessions", e);
            NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "Failed to retrieve the current list of knowledge sessions.");
            nack.setErrorHelp("The exception reads: "+e.getMessage());
            sendReply(msg, nack, MessageTypeEnum.NACK);
        }
    }

    /**
     * Handle the knowledge session update request message which comes from the Gateway module when in RTA
     * lesson level mode.  The request contains information to know which session will be the host of a team
     * knowledge session as well as the team member assignments for two or more learners (including the host).
     *
     * @param msg the knowledge session update request message to handle and use to create a new team knowledge
     * session for.
     */
    private void handleKnowledgeSessionUpdatedRequest(Message msg){

        DomainSessionMessage dsMsg = (DomainSessionMessage)msg;
        KnowledgeSessionsReply knowledgeSessionsReply = (KnowledgeSessionsReply)msg.getPayload();
        
        AbstractKnowledgeSession candidateHostKnowledgeSession = KnowledgeSessionManager.getInstance().getKnowledgeSessions().get(dsMsg.getDomainSessionId());
        
        // whether the host of the knowledge session is in active playback mode
        boolean candidateHostKnowledgeSessionActivePlayback = candidateHostKnowledgeSession != null && candidateHostKnowledgeSession.getSessionType() == SessionType.ACTIVE_PLAYBACK;

        Map<Integer, AbstractKnowledgeSession> sessionsRequestMap = knowledgeSessionsReply.getKnowledgeSessionMap();
        if(sessionsRequestMap.size() != 1){
            
            if(candidateHostKnowledgeSessionActivePlayback){
                // an empty knowledge update message is sent at the same time the knowledge session is started in order
                // to clear the TUI lobby.  Can be ignored in playback.
                return;
            }
            // there should only be one session in the map coming from the gateway module
            NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "Unable to set the team assignments with a Knowledge Sessions Update request message.");
            nack.setErrorHelp("There must be only one session being requested in the message and ther are "+sessionsRequestMap.size()+".");
            sendReply(msg, nack, MessageTypeEnum.PROCESSED_NACK);
            return;
        }

        AbstractKnowledgeSession knowledgeSession = sessionsRequestMap.values().iterator().next();
        if(!(knowledgeSession instanceof TeamKnowledgeSession)){
            // the request must be for a team knowledge session not an individual knowledge session, individual
            // knowledge sessions are automatically handled by the domain module and don't need the gateway module to send a request
            NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "Unable to set the team assignments with a Knowledge Sessions Update request message.");
            nack.setErrorHelp("The request must be for a team knowledge session.");
            sendReply(msg, nack, MessageTypeEnum.PROCESSED_NACK);
            return;
        }        

        if((candidateHostKnowledgeSession == null || !candidateHostKnowledgeSessionActivePlayback) &&
                DomainModuleProperties.getInstance().getLessonLevel() != LessonLevelEnum.RTA){
            // normally the domain module sends this message to the tutor module so the TUI can display the
            // current knowledge sessions and team org assignments to learners but in RTA mode
            // the gateway module sends this message to the domain module as an indication that
            // team assignments to learners is completed and contains in this message
            NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "Unable to set the team assignments with a Knowledge Sessions Update request message while in "+DomainModuleProperties.getInstance().getLessonLevel()+" lesson level configuration.");
            nack.setErrorHelp("To use the Knowledge Sessions Update request message change the LessonLevel to "+LessonLevelEnum.RTA.getName()+", restart GIFT and try again.");
            sendReply(msg, nack, MessageTypeEnum.PROCESSED_NACK);
            return;
        }

        final TeamKnowledgeSession rcvdTeamKnowledgeSession = (TeamKnowledgeSession)knowledgeSession;
        final BaseDomainSession requestorBaseDs = domainSessionIdToDomainSession.get(dsMsg.getDomainSessionId());
        if (requestorBaseDs != null) {

            SessionMembership hostSessionMembership = rcvdTeamKnowledgeSession.getHostSessionMember().getSessionMembership();
            if(!candidateHostKnowledgeSessionActivePlayback && !(hostSessionMembership instanceof GroupMembership)){
                // the request message must be a group membership which will contain all the team member assignments
                // EXCEPT when in playback - the host membership will be Individual
                NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "Failed to update the team assignments");
                nack.setErrorHelp("The host session's membership is not a "+GroupMembership.class.getName()+" for domain session with id "+dsMsg.getDomainSessionId());
                sendReply(msg, nack, MessageTypeEnum.PROCESSED_NACK);
                return;
            }
            
            final ManageTeamMembershipRequest createRequest;
            try{
                if(hostSessionMembership instanceof IndividualMembership){
                    // this should only happen in active playback
                    
                    String joiningLearnerName = null;
                    int requestingDsId = 0;
                    
                    // the current members known the the session being created
                    SessionMembers hostSessionMembers = KnowledgeSessionManager.getInstance().getKnowledgeSessionMembers(dsMsg.getDomainSessionId());
                    
                    // determine whether the incoming knowledge session update message is for a learner joining a team knowledge session
                    // or being assigned to a team member having already joined in a previous message.  This is determined by comparing
                    // the state of the session in the message to what is known by the host session membership.
                    for(SessionMember joinedMember : rcvdTeamKnowledgeSession.getJoinedMembers().values()){
                        if(joinedMember.getSessionMembership() != null && joinedMember.getSessionMembership().getTeamMember() == null){
                            
                            boolean alreadyJoined = false;
                            String candidateJoinerLearnerName = joinedMember.getSessionMembership().getUsername();
                            for(SessionMember existingMember : hostSessionMembers.getSessionMemberDSIdMap().values()){
                                if(existingMember.getSessionMembership().getUsername().equals(candidateJoinerLearnerName)){
                                    // this learner has already joined the session
                                    alreadyJoined = true;
                                    break;
                                }
                            }
                            
                            if(!alreadyJoined){
                                // this is a request to join - in this case it is a joiner not the host in the playback
                                joiningLearnerName = candidateJoinerLearnerName;
                                break;
                            }
                        }
                    }
                    
                    if(joiningLearnerName == null && rcvdTeamKnowledgeSession.getJoinedMembers().isEmpty() &&
                            rcvdTeamKnowledgeSession.getHostSessionMember().getSessionMembership().getTeamMember() == null){
                        // this is a request to join - in this case it is the host in the playback - use the playback host as the user joining this new
                        // playback session.
                        // Note: when the original playback session host creates the session they are immediately joined to that session.  In that instance
                        // there are no other learners joined, therefore the joined members size is zero.  This if should only be entered once.
                        joiningLearnerName = rcvdTeamKnowledgeSession.getHostSessionMember().getSessionMembership().getUsername();
                        
                    }
                    
                    if(joiningLearnerName != null){
                        // this is a request to join a team knowledge session
                        
                        // check if the joiner is the host of the playback course, which means the user doesn't need to request
                        // to join.
                        if(candidateHostKnowledgeSession != null &&
                                joiningLearnerName.equals(candidateHostKnowledgeSession.getHostSessionMember().getSessionMembership().getUsername())){
                            if(logger.isDebugEnabled()){
                                logger.debug("Skipping the join knowledge session request in active playback because the user '"+joiningLearnerName+
                                        "' from the playback log is the host of the current playback.");
                            }
                            return;
                        }
                        
                        createRequest =
                                ManageTeamMembershipRequest.createJoinTeamKnowledgeSessionRequest(dsMsg.getDomainSessionId());
                        // creating some unique ds id for this collection of individuals joining this real team session
                        // to fake the joiners session as if it were a real session but it doesn't actually exist
                        requestingDsId = msg.getSequenceNumber() + dsMsg.getDomainSessionId();  
                    }else{
                        // this is a request to assign learner to a team member  
                        
                        // compare the message contents to the current ds knowledge session members to get the following for
                        // the team member being assigned:
                        // 1. the artificially created ds id when that learner joined
                        // 2. the team member name the learner is assigning too
                        String teamMemberNameBeingAssigned = null;
                        for(Integer memberDsId : hostSessionMembers.getSessionMemberDSIdMap().keySet()){
                            SessionMember member = hostSessionMembers.getSessionMemberDSIdMap().get(memberDsId);
                            if(member.getSessionMembership().getTeamMember() == null){
                                // the host session knows about this joined member but the member hasn't been assigned a team member yet,
                                // check to see if the incoming message assigned a team member
                                // - Get by matching username
                                
                                for(SessionMember rcvdMemberCandidate : rcvdTeamKnowledgeSession.getJoinedMembers().values()){
                                    
                                    if(rcvdMemberCandidate.getSessionMembership().getUsername().equals(member.getSessionMembership().getUsername())){
                                        
                                        if(rcvdMemberCandidate.getSessionMembership().getTeamMember() != null){
                                            // the incoming message is defining a team member for a joined member of the playback session (not the host)
                                            teamMemberNameBeingAssigned = rcvdMemberCandidate.getSessionMembership().getTeamMember().getName();
                                            joiningLearnerName = rcvdMemberCandidate.getSessionMembership().getUsername();
                                            requestingDsId = memberDsId;
                                        }
                                        
                                        // no matter if this message contains team member assignment or not the outer for loop is checking
                                        // a specific joined session member and we found it in this if statement so stop checking for the user again
                                        break;
                                    }

                                }

                                if(joiningLearnerName == null &&
                                        member.getSessionMembership().getUsername().equals(
                                            rcvdTeamKnowledgeSession.getHostSessionMember().getSessionMembership().getUsername()) &&
                                        rcvdTeamKnowledgeSession.getHostSessionMember().getSessionMembership().getTeamMember() != null){
                                    // the incoming message is defining a team member for a host member of the playback session
                                    teamMemberNameBeingAssigned = rcvdTeamKnowledgeSession.getHostSessionMember().getSessionMembership().getTeamMember().getName();
                                    joiningLearnerName = rcvdTeamKnowledgeSession.getHostSessionMember().getSessionMembership().getUsername();
                                    requestingDsId = memberDsId;
                                    break;
                                }
                            }
                        }
                        
                        if(teamMemberNameBeingAssigned == null){
                            requestorBaseDs.terminationShutdownDisplayError("Failed to find the team member name being assigned while handling a "+
                                    "knowledge session update request message during playback.");
                            return;
                        }else if(requestingDsId == 0){
                            requestorBaseDs.terminationShutdownDisplayError("Failed to find a domain session id for a learner being assigned a team member.");
                            return;
                        }
                        
                        createRequest =
                                ManageTeamMembershipRequest.createAssignTeamMemberTeamKnowledgeSessionRequest(dsMsg.getDomainSessionId(), teamMemberNameBeingAssigned);
                         
                    }
                    
                    // group membership is only supported when the Gateway module sends knowledge session requests
                    requestorBaseDs.handleTeamKnowledgeSessionMembershipRequest(createRequest, requestingDsId, joiningLearnerName, msg.getSenderModuleType() == ModuleTypeEnum.GATEWAY_MODULE, new AsyncActionCallback() {
                        @Override
                        public void onSuccess() {
    
                            // finally, send the reply that the original message was handled
                            ACK ack = new ACK();
                            sendReply(msg, ack, MessageTypeEnum.PROCESSED_ACK);
                        }
    
                        @Override
                        public void onFailure(Exception e) {
                            logger.error("Caught exception while trying to manage team knowledge session membership request of\n"+createRequest+".", e);
                            requestorBaseDs.terminationShutdownDisplayError("Failed to apply the change in team knowledge session membership.\n"+e.getMessage()+"\n"+requestorBaseDs.getDomainSessionInfo());
                        }
                    });
    
                }else{
                    // make this domain session the host
                    // add all team members found in the message (but with the same user and domain session ids)
                    createRequest =
                            ManageTeamMembershipRequest.createHostedTeamKnowledgeSessionRequest(dsMsg.getDomainSessionId(), knowledgeSession.getNameOfSession(),
                                    (GroupMembership) hostSessionMembership);
                    
                    // group membership is only supported when the Gateway module sends knowledge session requests
                    requestorBaseDs.handleTeamKnowledgeSessionMembershipRequest(createRequest, msg.getSenderModuleType() == ModuleTypeEnum.GATEWAY_MODULE, new AsyncActionCallback() {
                        @Override
                        public void onSuccess() {
    
                            // finally, send the reply that the original message was handled
                            ACK ack = new ACK();
                            sendReply(msg, ack, MessageTypeEnum.PROCESSED_ACK);
                            
                            try {
                                //after replying, begin the team knowledge session so the scenario can start
                                startTeamKnowledgeSessionForAllMembers(requestorBaseDs);
                                
                            } catch (Exception e) {
                                logger.error("Failed to start team session: " + e);
                            }
                        }
    
                        @Override
                        public void onFailure(Exception e) {
                            logger.error("Caught exception while trying to manage team knowledge session membership request of\n"+createRequest+".", e);
                            if(requestorBaseDs.isInPlaybackMode()){
                                requestorBaseDs.terminationShutdownDisplayError("Failed to apply the change in team knowledge session membership.\n"+e.getMessage()+"\n"+requestorBaseDs.getDomainSessionInfo());
                            }else{
                                NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "Failed to apply the change in team knowledge session membership");
                                nack.setErrorHelp("The exception reads: "+e.getMessage());
                                sendReply(msg, nack, MessageTypeEnum.NACK);
                            }
                        }
                    });
                }
                
            } catch (ManageTeamMembershipException e) {
                logger.error("Caught exception while trying to manage team knowledge session membership request of\n"+msg+".", e);
                if(requestorBaseDs.isInPlaybackMode()){
                    requestorBaseDs.terminationShutdownDisplayError("Failed to apply the change in team knowledge session membership.\n"+e.getMessage()+"\n"+requestorBaseDs.getDomainSessionInfo());
                }else{
                    NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, e.getFriendlyMessage());
                    nack.setErrorHelp("The exception reads: "+e.getMessage());
                    sendReply(msg, nack, MessageTypeEnum.NACK);
                }
            } catch (RuntimeException e) {
                logger.error("Caught runtime exception while trying to manage team knowledge session membership request of\n"+msg+".", e);
                if(requestorBaseDs.isInPlaybackMode()){
                    requestorBaseDs.terminationShutdownDisplayError("Failed to apply the change in team knowledge session membership.\n"+e.getMessage()+"\n"+requestorBaseDs.getDomainSessionInfo());
                }else{
                    NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, e.getMessage());
                    nack.setErrorHelp("The exception reads: "+e.getMessage());
                    sendReply(msg, nack, MessageTypeEnum.NACK);
                }
            } catch(Exception e){
                if(requestorBaseDs.isInPlaybackMode()){
                    requestorBaseDs.terminationShutdownDisplayError("Failed to apply the change in team knowledge session membership.\n"+e.getMessage()+"\n"+requestorBaseDs.getDomainSessionInfo());
                }else{
                    logger.error("Caught exception while trying to manage team knowledge session membership request of\n"+msg+".", e);
                    NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "Failed to apply the change in team knowledge session membership");
                    nack.setErrorHelp("The exception reads: "+e.getMessage());
                    sendReply(msg, nack, MessageTypeEnum.NACK);
                }
            }


        }else{
            NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "Failed to set the team assignments");
            nack.setErrorHelp("Unable to find the domain session with id "+dsMsg.getDomainSessionId());
            sendReply(msg, nack, MessageTypeEnum.PROCESSED_NACK);
        }

    }

    /**
     * Handle the request to change team membership for a learner.  This can add or remove the learner from a team knowledge
     * session.  After changing membership the reply message will contain the latest set of active knowledge sessions.
     *
     * @param msg the request message to manage team membership for a learner.
     */
    private void handleManageMembershipTeamKnowledgeSessionRequest(Message msg){

        final DomainSessionMessage domainSessionMessage = (DomainSessionMessage) msg;
        final ManageTeamMembershipRequest membershipRequest = (ManageTeamMembershipRequest) msg.getPayload();
        final BaseDomainSession requestorBaseDS = domainSessionIdToDomainSession.get(domainSessionMessage.getDomainSessionId());
        if (requestorBaseDS != null) {

            try{
                // group membership is only supported when the Gateway module sends knowledge session requests
                requestorBaseDS.handleTeamKnowledgeSessionMembershipRequest(membershipRequest, msg.getSenderModuleType() == ModuleTypeEnum.GATEWAY_MODULE, new AsyncActionCallback() {
                    @Override
                    public void onSuccess() {
                        boolean hasGW = requestorBaseDS.getDomainSessionInfo().isGatewayConnected();
                        Map<Integer, AbstractKnowledgeSession> knowledgeSessionMap = KnowledgeSessionManager.getInstance().getKnowledgeSessions();
                        KnowledgeSessionsReply reply = new KnowledgeSessionsReply(knowledgeSessionMap);
                        reply.setCanHost(hasGW);
                        sendReply(msg, reply , MessageTypeEnum.ACTIVE_KNOWLEDGE_SESSIONS_REPLY);

                        /* Signal the tutor that the sessions have been modified
                         * so the tutor can display any changes to the session.
                         * Use the host domain session id because it is the one
                         * responsible for logging and updating the joined
                         * members if necessary. */
                        notifyTutorKnowledgeSessionsUpdated(membershipRequest.getDomainSessionIdOfHost());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        logger.error("Caught exception while trying to manage team knowledge session membership request of\n"+membershipRequest+".", e);
                        NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "Failed to apply the change in team knowledge session membership");
                        nack.setErrorHelp("The exception reads: "+e.getMessage());
                        sendReply(msg, nack, MessageTypeEnum.NACK);
                    }
                });
            } catch (ManageTeamMembershipException e) {
                logger.error("Caught exception while trying to manage team knowledge session membership request of\n"+membershipRequest+".", e);
                NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, e.getFriendlyMessage());
                nack.setErrorHelp("The exception reads: "+e.getMessage());
                sendReply(msg, nack, MessageTypeEnum.NACK);
            } catch (RuntimeException e) {
                logger.error("Caught runtime exception while trying to manage team knowledge session membership request of\n"+membershipRequest+".", e);
                NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, e.getMessage());
                nack.setErrorHelp("The exception reads: "+e.getMessage());
                sendReply(msg, nack, MessageTypeEnum.NACK);
            } catch(Exception e){
                logger.error("Caught exception while trying to manage team knowledge session membership request of\n"+membershipRequest+".", e);
                NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "Failed to apply the change in team knowledge session membership");
                nack.setErrorHelp("The exception reads: "+e.getMessage());
                sendReply(msg, nack, MessageTypeEnum.NACK);
            }
        }else{
            NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "Failed to apply the change in team knowledge session membership");
            nack.setErrorHelp("Unable to find the domain session with id "+domainSessionMessage.getDomainSessionId());
            sendReply(msg, nack, MessageTypeEnum.NACK);
        }

    }

    @Override
    public void notifyTutorKnowledgeSessionsUpdated(int domainSessionId) {

        KnowledgeSessionsRequest knowledgeSessionsRequest = new KnowledgeSessionsRequest();
        knowledgeSessionsRequest.setFullTeamSessions(false);
        knowledgeSessionsRequest.setIndividualSessions(false);
        knowledgeSessionsRequest.setRunningSessions(false);

        // Push the updated list of knowledge sessions to the tutor.
        // Note: if a callback is added then it will reintroduce #4439 where the domain module disconnects
        //       the tutor client queue connection before receiving the response to this message.
        DomainModule.getInstance().sendKnowledgeSessionsUpdated(domainSessionId, knowledgeSessionsRequest, null);
    }

    /**
     * Handles the start team knowledge session request
     *
     * @param msg The message to be handled
     */
    private void handleStartTeamKnowledgeSessionRequest(Message msg) {

        DomainSessionMessage domainSessionMessage = (DomainSessionMessage) msg;

        BaseDomainSession ds = domainSessionIdToDomainSession.get(domainSessionMessage.getDomainSessionId());
        if (ds != null) {
            try{

                //check that this domain session is the host
                if(KnowledgeSessionManager.getInstance().isHostOfTeamKnowledgeSession(ds.getDomainSessionInfo().getDomainSessionId())){

                    // Validate team knowledge session
                    String invalidSessionReason = KnowledgeSessionManager.getInstance().validateKnowledgeSessionForStart(ds.getDomainSessionInfo().getDomainSessionId());

                    if (invalidSessionReason == null) {
                        
                        if(ds.isInPlaybackMode()){
                            // send a knowledge session update request that has the domain session id in its map of sessions so that it ends
                            // up in the domain session log for playback purposes.
                            KnowledgeSessionsRequest knowledgeSessionsRequest = new KnowledgeSessionsRequest();
                            knowledgeSessionsRequest.setFullTeamSessions(true);
                            knowledgeSessionsRequest.setIndividualSessions(true);
                            knowledgeSessionsRequest.setRunningSessions(false);
                            
                            sendKnowledgeSessionsUpdated(domainSessionMessage.getDomainSessionId(), knowledgeSessionsRequest, null);
                        }
                        
                        startTeamKnowledgeSessionForAllMembers(ds);
                        
                        if(!ds.isInPlaybackMode()){
                            // If we get this far (with no exceptions), we just signal back a simple ack that it succeeded.
                            sendReply(msg, null , MessageTypeEnum.START_TEAM_KNOWLEDGE_SESSION_REPLY);
                            
                            // Signal the tutor that the sessions have been modified so the tutor can display any changes to the session.
                            // Note: from what I can tell this knowledge update will be flushed on this started session (i.e. the sessionMap can be empty)
                            notifyTutorKnowledgeSessionsUpdated(ds.getDomainSessionInfo().getDomainSessionId());
                        }
                        
                    }else if(ds.isInPlaybackMode()){
                        // can't send a reply because the playback messages are sent on the domain topic and nothing is handling
                        // the replies
                        ds.terminateSession(true, 
                                "The playback service encounted a problem on the server.  In order to start this knowledge session, all joined members must be assigned to a team role before the session can be started.", 
                                invalidSessionReason);
                    } else {
                        NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "All joined members must be assigned to a team role before the session can be started.");
                        nack.setErrorHelp(invalidSessionReason);
                        sendReply(msg, nack, MessageTypeEnum.NACK);
                    }

                }else{
                    NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "Failed to start a team knowledge session for domain session " +
                            domainSessionMessage.getDomainSessionId()+" because that domain session is not currently a host of a team knowledge session.");
                    sendReply(msg, nack, MessageTypeEnum.NACK);
                }


            }catch(Exception e){
                logger.error("Caught exception while trying to start the team knowledge session:  ", e);
                NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "Failed to start the host team knowledge session for: " + domainSessionMessage.getDomainSessionId());
                nack.setErrorHelp("The exception reads: "+e.getMessage());
                sendReply(msg, nack, MessageTypeEnum.NACK);
            }
        }else{
            NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "Failed to start the host team knowledge session. " );
            nack.setErrorHelp("Unable to find the domain session with id "+domainSessionMessage.getDomainSessionId());
            sendReply(msg, nack, MessageTypeEnum.NACK);
        }
    }

    /**
     * Attempts to start a team knowledge session for all members.
     *
     * @param hostDs The base domain session information of the host (cannot be null)
     *
     * @throws Exception
     */
    void startTeamKnowledgeSessionForAllMembers(BaseDomainSession hostDs) throws Exception {

        // Get the domain session id of the host.
        int hostDsId = hostDs.getDomainSessionInfo().getDomainSessionId();

        // Find any joined members of the session
        Map<Integer, SessionMember> sessionMembersMap = KnowledgeSessionManager.getInstance()
                .getTeamKnowledgeSessionsMembersForHost(hostDsId);


        // if this domain session is a host of a knowledge session than now is the time to
        // set the session running to true
        AbstractKnowledgeSession aKnowledgeSession = KnowledgeSessionManager.getInstance().getKnowledgeSessions().get(hostDsId);
        if(aKnowledgeSession != null){
            aKnowledgeSession.setSessionRunning(true);
        } else {
            logger.error("Unable to set session to running for domain session id: " + hostDsId);
        }

        // Start the host session first...regardless if there are members.
        hostDs.startTeamKnowledgeSession();

        if (logger.isDebugEnabled()) {
            logger.debug("starting session for host: " + hostDs);
        }

        // Ensure that there are joined members
        if (sessionMembersMap != null) {

            if (!sessionMembersMap.isEmpty() && !hostDs.isInPlaybackMode()) {

                for (SessionMember sessionMember : sessionMembersMap.values()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("starting session for member: " + sessionMember);
                    }

                    BaseDomainSession joinedBds = domainSessionIdToDomainSession
                            .get(sessionMember.getDomainSessionId());
                    if (joinedBds != null) {
                        joinedBds.startTeamKnowledgeSession();
                    } else {
                        // Log an error, but do not prevent the other members from starting.
                        logger.error("Team knowledge session is starting, but the member: " + sessionMember +
                                " could not be started because a base domain session could not be found for the member.");
                    }
                }
            }
        } else {
            logger.error("host is starting a team session, but there are no members.");

        }

    }

    /**
     * Create and send a module status message over the network.
     */
    @Override
    public void sendModuleStatus() {

        moduleStatus.setModuleState(ModuleStateEnum.NORMAL);
        sendMessage(SubjectUtil.DOMAIN_DISCOVERY_TOPIC, domainStatus, MessageTypeEnum.DOMAIN_MODULE_STATUS, null);
    }

    /**
     * Gets a set of survey questions from the UMS.
     *
     * @param domainSessionId
     *            The domain session ID of the requesting session.
     * @param getSurveyRequest
     *            the survey request information (i.e. survey context, gift key,
     *            ...)
     * @param callback
     *            The callback handler.
     * @return List<String> The list of survey questions from the UMS.
     */
    public List<String> sendGetSurveyRequest(int domainSessionId, final GetSurveyRequest getSurveyRequest,
            MessageCollectionCallback callback) {

        final List<String> questions = new ArrayList<>();

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        if (domainSession != null) {

            sendDomainSessionMessage(getSurveyRequest, domainSession.getDomainSessionInfo(), domainSessionId,
                    MessageTypeEnum.GET_SURVEY_REQUEST, callback);

        } else {
            logger.error("Could not get survey questions from the UMS: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }

        return questions;
    }

    /**
     * Gets an experiment from the UMS.
     *
     * @param getExperimentRequest
     *            the experiment request information
     * @param callback
     *            The callback handler.
     */
    public void sendGetExperimentRequest(final GetExperimentRequest getExperimentRequest,
            MessageCollectionCallback callback) {

        // get UMS connection
        MessageCollectionCallback umsCallback = new MessageCollectionCallback() {

            @Override
            public void success() {
                sendMessage(ModuleTypeEnum.UMS_MODULE, getExperimentRequest, MessageTypeEnum.GET_EXPERIMENT_REQUEST,
                        callback);
            }

            @Override
            public void received(final Message aMsg) {

            }

            @Override
            public void failure(Message nackMsg) {
                callback.failure(nackMsg);
            }

            @Override
            public void failure(String why) {
                callback.failure(why);
            }
        };

        selectUMSModule(umsCallback);
    }

    /**
     * Create and send a LoS Query message to the Gateway module.
     *
     * @param domainSession
     *            - information about the domain session sending the LoS Query
     * @param query - the LoS query to send
     */
    public void sendLoSQuery(final DomainSession domainSession, LoSQuery query) {
        sendDomainSessionMessage(query, domainSession, domainSession.getDomainSessionId(),
                MessageTypeEnum.LOS_QUERY, null);
    }
    
    /**
     * Create and send a variables state request message to the Gateway module.
     *
     * @param domainSession
     *            - information about the domain session sending the variables state request.  Can't be null.
     * @param request - the variables state request to send. Can't be null.
     */
    public void sendVariablesStateRequest(final DomainSession domainSession, VariablesStateRequest request) {
        sendDomainSessionMessage(request, domainSession, domainSession.getDomainSessionId(),
                MessageTypeEnum.VARIABLE_STATE_REQUEST, null);
    }

    /**
     * Sends survey results to the UMS to be stored.
     *
     * @param surveyResponse
     *            The response to the survey to send to the UMS.
     * @param key
     *            The key of the survey these questions are associated with.
     * @param domainSession
     *            information about the domain session sending the Survey
     *            results
     * @param callback
     *            Callback for when presenting the survey is complete or a
     *            failure has occurred
     */
    public void sendSurveyResults(final SurveyResponse surveyResponse, final String key, DomainSession domainSession,
            MessageCollectionCallback callback) {

        final BaseDomainSession baseDomainSession = domainSessionIdToDomainSession
                .get(domainSession.getDomainSessionId());

        if (baseDomainSession != null) {

            SubmitSurveyResults submitResults = new SubmitSurveyResults(key,  domainSession.getDomainSourceId(), surveyResponse);
            sendDomainSessionMessage(submitResults, domainSession, domainSession.getDomainSessionId(),
                    MessageTypeEnum.SUBMIT_SURVEY_RESULTS, callback);

        } else {
            logger.error("Could not send survey results to the UMS: Domain Session ID "
                    + domainSession.getDomainSessionId() + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Send the initialize interop connections message to the gateway module
     *
     * @param domainSession
     *            information about the domain session that needs to init
     *            interop connections
     * @param domainContentServerAddr
     *            the address of the domain content server (e.g.
     *            http://10.1.21.123:8885) that might be needed by the gateway
     *            interop plugins implementation classes to retrieve course
     *            content to be handled by a training app instance (e.g.
     *            PowerPoint show).
     * @param interops
     *            The gateway interop plugin implementation class names from the
     *            course.xml (e.g. gateway.interop.ppt.PPTInterface) that need
     *            to be initialized/enabled for the current domain session being
     *            initialized. Can't be null. Currently can't be empty.
     * @param isPlayback whether a playback of a domain session log is currently happening which could have implications
     * on how this interop plugin handles messages going in/out.
     * @param callback
     *            The message callback handler.
     */
    public void sendInitializeInteropConnection(DomainSession domainSession, String domainContentServerAddr,
            Collection<String> interops, boolean isPlayback, MessageCollectionCallback callback) {

        InitializeInteropConnections iConnection = new InitializeInteropConnections(domainContentServerAddr, interops);
        sendDomainSessionMessage(iConnection, domainSession, domainSession.getDomainSessionId(),
                MessageTypeEnum.INIT_INTEROP_CONNECTIONS, callback);
    }

    /**
     * Sends a message that contains the urls that the embedded application is located
     * at, as well as the unique id for the tutor topic to send the embedded training
     * application messages to.
     * @param domainSession
     *          information about the domain session that needs to configure
     *          embedded application connections
     * @param urls
     *          The collection of urls that the embedded training applications
     *          are located at. Currently only one is supported so the urls
     *          collection should only contain a single string
     * @param callback
     *          The message callback handler
     */
    public void sendInitializeEmbeddedAppConnection(DomainSession domainSession,
            Collection<String> urls, MessageCollectionCallback callback) {

        //Sends the message to the TutorModule
        InitializeEmbeddedConnections iConnection = new InitializeEmbeddedConnections(urls);
        sendDomainSessionMessage(iConnection, domainSession, domainSession.getDomainSessionId(),
                MessageTypeEnum.INIT_EMBEDDED_CONNECTIONS, callback);
    }

    /**
     * Send the configure interop connections message to the gateway module
     *
     * @param domainSession
     *            information about the domain session that needs to configure
     *            interop connections
     * @param domainContentServerAddr
     *            the address of the domain content server (e.g.
     *            http://10.1.21.123:8885) that might be needed by the gateway
     *            interop plugins implementation classes to retrieve course
     *            content to be handled by a training app instance (e.g.
     *            PowerPoint show).
     * @param interops
     *            The gateway interop plugin implementation class names from the
     *            course.xml (e.g. gateway.interop.ppt.PPTInterface) that need
     *            to be configured for the current domain session being
     *            initialized. Can't be null. Currently can't be empty.
     * @param callback
     *            The message callback handler.
     */
    public void sendConfigureInteropConnection(DomainSession domainSession, String domainContentServerAddr,
            Collection<String> interops, MessageCollectionCallback callback) {

        InitializeInteropConnections iConnection = new InitializeInteropConnections(domainContentServerAddr, interops);
        sendDomainSessionMessage(iConnection, domainSession, domainSession.getDomainSessionId(),
                MessageTypeEnum.CONFIGURE_INTEROP_CONNECTIONS, callback);
    }

    /**
     * Send the initialize domain session message to the appropriate modules
     *
     * @param domainSession
     *            The domain session information for the current domain session
     *            being initialized
     * @param tutorTopicId
     *            The unique identifier to be used to create the address of the
     *            tutor topic which will receive messages from this domain session's
     *            embedded applications.
     * @param clientInfo 
     *            Contains unique information about the client being used by the learner
     *            to take this course.  Can be null, but not ideal.
     * @param callback
     *            The message callback handler.
     */
    public void sendInitializeDomainSession(DomainSession domainSession, String tutorTopicId, WebClientInformation clientInfo,
            MessageCollectionCallback callback) {

        final BaseDomainSession bDomainSession = domainSessionIdToDomainSession.get(domainSession.getDomainSessionId());
        if (bDomainSession != null) {

            InitializeDomainSessionRequest request = new InitializeDomainSessionRequest(
                    bDomainSession.getCourseFileName(),
                    domainSession.doesRequiresTutorTopic() ? tutorTopicId : null,
                            clientInfo);
            sendDomainSessionMessage(request, domainSession, MessageTypeEnum.INITIALIZE_DOMAIN_SESSION_REQUEST,
                    callback);

        } else {
            logger.error("Could not initialize the domain session: " + domainSession
                    + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Notifies other modules that the Domain Session is starting.
     *
     * @param domainSession
     *            The domain session of the session requesting.
     * @param callback
     *            The message callback handler.
     */
    public void sendStartDomainSession(DomainSession domainSession, MessageCollectionCallback callback) {

        final BaseDomainSession bDomainSession = domainSessionIdToDomainSession.get(domainSession.getDomainSessionId());
        if (bDomainSession != null) {

            sendDomainSessionMessage(null, domainSession, MessageTypeEnum.START_DOMAIN_SESSION, callback);

        } else {
            logger.error("Could not start the domain session: " + domainSession + " does not have a domain session "
                    + "associated with it.");
        }
    }

    /**
     * Send the close domain session message to the appropriate modules
     *
     * @param domainSession
     *            The domain session of the session requesting.
     * @param callback
     *            The message callback handler.
     */
    public void sendCloseDomainSession(DomainSession domainSession, final MessageCollectionCallback callback) {

        final BaseDomainSession bDomainSession = domainSessionIdToDomainSession.get(domainSession.getDomainSessionId());
        if (bDomainSession != null) {

            final MessageCollectionCallback releaseCallback = new MessageCollectionCallback() {

                @Override
                public void success() {

                    if (logger.isInfoEnabled()){
                        logger.info("Successfully closed domain session in other modules, attempting to release those modules");
                    }
                    releaseUserSessionModules(domainSession);
                    callback.success();
                }

                @Override
                public void received(Message msg) {
                    callback.received(msg);
                }

                @Override
                public void failure(Message msg) {

                    logger.error("Failed to successfully close the domain session because received message: " + msg
                            + ", trying anyway to release connections");
                    releaseDomainSessionModules(domainSession);

                    callback.failure(msg);
                }

                @Override
                public void failure(String why) {

                    logger.error("Failed to successfully close the domain session because of reason: " + why
                            + ", trying anyway to release connections");
                    releaseDomainSessionModules(domainSession);

                    callback.failure(why);
                }
            };

            sendDomainSessionMessage(new CloseDomainSessionRequest(), domainSession,
                    MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST, releaseCallback);

        } else {
            logger.error("Could not close the domain session: " + domainSession + " does not have a domain session "
                    + "associated with it.");
        }
    }

    /**
     * Send the close domain session message to the appropriate modules except
     * the tutor module.  If the domain session is not known to the domain module
     * this method does nothing but log an error and call callback.success().
     *
     * @param reason optional user friendly message describing why the session is ending.
     * @param domainSession The domain session of the session requesting.
     * @param callback The message callback handler.
     */
    public void sendCloseDomainSessionNotToTutor(String reason, DomainSession domainSession, MessageCollectionCallback callback) {

        final BaseDomainSession bDomainSession = domainSessionIdToDomainSession.get(domainSession.getDomainSessionId());
        if (bDomainSession != null) {

            List<ModuleTypeEnum> recipients = new ArrayList<>();
            
            /* Only add the ped as a recipient if a message client has been 
             * allocated for it already. If we're closing a session while it is 
             * still initializing, then no message client may be available */
            if(haveSubjectClient(domainSession, ModuleTypeEnum.PEDAGOGICAL_MODULE)) {
                recipients.add(ModuleTypeEnum.PEDAGOGICAL_MODULE);
            }
            
            recipients.add(ModuleTypeEnum.UMS_MODULE);
            
            /* Only add the sensor as a recipient if a message client has been 
             * allocated for it already. If we're closing a session while it is 
             * still initializing, then no message client may be available */
            if(haveSubjectClient(domainSession, ModuleTypeEnum.SENSOR_MODULE)) {
                recipients.add(ModuleTypeEnum.SENSOR_MODULE);
            }
            
            if (domainSession.isGatewayConnected()) {
                recipients.add(ModuleTypeEnum.GATEWAY_MODULE);
            }

            if (domainSession.isLearnerConnected()) {
                recipients.add(ModuleTypeEnum.LEARNER_MODULE);
            }

            sendDomainSessionMessage(recipients.toArray(new ModuleTypeEnum[0]), new CloseDomainSessionRequest(reason),
                    domainSession, domainSession.getDomainSessionId(), domainSession.getExperimentId(),
                    MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST, callback);

        } else {
            logger.error("Could not close the domain session because the domain session can't be found.\n" + domainSession);
            callback.success();
        }
    }

    /**
     * Send the close domain session message to the tutor module.
     *
     * @param domainSessionId
     *            The domain session ID of the session requesting.
     * @param callback
     *            The message callback handler.
     */
    public void sendCloseDomainSessionToTutor(int domainSessionId, MessageCollectionCallback callback) {

        /* If GIFT is in RTA mode, then there is no Tutor Module running and
         * therefore; we should not attempt to send a message to the Tutor
         * Module. */
        if (LessonLevelEnum.RTA.equals(DomainModuleProperties.getInstance().getLessonLevel())) {
            if (callback != null) {
                callback.success();
            }

            return;
        }

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        if (domainSession != null) {

            sendCloseDomainSessionToTutor(domainSession.getDomainSessionInfo(),
                    domainSession.getDomainSessionInfo().getDomainSessionId(), callback);

        } else {
            logger.error("Could not close the domain session because the domain session can't be found by domain session id "+ domainSessionId);
        }
    }

    /**
     * Send the close domain session message to the tutor module.
     *
     * @param userSession
     *            The user information of the learner
     * @param domainSessionId
     *            The domain session ID of the session requesting.
     * @param callback
     *            The message callback handler.
     */
    private void sendCloseDomainSessionToTutor(UserSession userSession, int domainSessionId,
            MessageCollectionCallback callback) {

        if(DomainModuleProperties.getInstance().getLessonLevel() == LessonLevelEnum.RTA){
            // there is no tutor module
            callback.success();
        }

        sendDomainSessionMessage(ModuleTypeEnum.TUTOR_MODULE, new CloseDomainSessionRequest(), userSession,
                domainSessionId, MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST, callback);
    }

    /**
     * Forward the load progress information to the tutor to display to the
     * learner.
     *
     * @param userSession
     *            The user information of the learner
     * @param domainSessionId
     *            The domain session ID of the session requesting.
     * @param loadProgress
     *            contains progress information about loading content into a
     *            training application
     */
    public void sendLoadProgressToTutor(UserSession userSession, int domainSessionId, GenericJSONState loadProgress) {

        if(DomainModuleProperties.getInstance().getLessonLevel() == LessonLevelEnum.RTA){
            // there is no tutor module
            return;
        }

        sendDomainSessionMessage(ModuleTypeEnum.TUTOR_MODULE, loadProgress, userSession, domainSessionId,
                MessageTypeEnum.LOAD_PROGRESS, null);
    }

    /**
     * Send a display feedback request message to the TUI
     *
     * @param domainSessionId
     *            The domain session ID of the session requesting.
     * @param text
     *            The feedback to display
     * @param callback
     *            The message callback handler.   Can be null.  When null the recipient won't need to send a response that the message was processed because
     * the message header will show "NeedsACK":false.
     */
    public void sendTUIFeedbackRequest(int domainSessionId, final TutorUserInterfaceFeedback text,
            MessageCollectionCallback callback) {

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        if (domainSession != null) {

            if(DomainModuleProperties.getInstance().getLessonLevel() == LessonLevelEnum.RTA){
                // the tutor module isn't used in RTA mode, the GW module is connected to an external system
                // that controls things.  Therefore send any TUI feedback strategies to the GW module to
                // deliver to the external systems to present at its discretion
                sendDomainSessionMessage(ModuleTypeEnum.GATEWAY_MODULE, text, domainSession.getDomainSessionInfo(),
                        domainSession.getDomainSessionInfo().getDomainSessionId(),
                        MessageTypeEnum.DISPLAY_FEEDBACK_TUTOR_REQUEST, callback);
            }else{
            sendDomainSessionMessage(text, domainSession.getDomainSessionInfo(),
                    domainSession.getDomainSessionInfo().getDomainSessionId(),
                    MessageTypeEnum.DISPLAY_FEEDBACK_TUTOR_REQUEST, callback);
            }

        } else {
            logger.error("Could not handle display feedback request for the TUI: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Send a display feedback request message to the gateway module.
     *
     * @param domainSessionId
     *            The domain session ID of the session requesting.
     * @param argument
     *            The authored message information including content and delivery settings
     * @param callback
     *            The message callback handler.
     * @param isEmbeddedApp
     *            A flag that indicates whether the scenario is being run within an
     *            embedded training application or an interop training application.
     *            If its an interop application, the message is sent to the Gateway.
     *            Otherwise its an embedded application and the message is sent to the
     *            Tutor Module.
     */
    public void sendTrainingAppFeedbackRequest(int domainSessionId, final generated.dkf.Message argument,
            MessageCollectionCallback callback, boolean isEmbeddedApp) {

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        if (domainSession != null) {

            if(isEmbeddedApp && DomainModuleProperties.getInstance().getLessonLevel() == LessonLevelEnum.RTA){
                // there is no tutor module, embedded applications can't be presented
                callback.failure("Embedded applications aren't supported in "+DomainModuleProperties.getInstance().getLessonLevel()+
                        " lesson level configuration.  Therefore the feedback message '"+argument.getContent()+"' will not be delivered for domain session "+domainSessionId+".");
            }

            ModuleTypeEnum module = isEmbeddedApp ? ModuleTypeEnum.TUTOR_MODULE : ModuleTypeEnum.GATEWAY_MODULE;
            MessageTypeEnum msgType = isEmbeddedApp ? MessageTypeEnum.DISPLAY_FEEDBACK_EMBEDDED_REQUEST : MessageTypeEnum.DISPLAY_FEEDBACK_GATEWAY_REQUEST;
            sendDomainSessionMessage(module, argument.getContent(), domainSession.getDomainSessionInfo(),
                    domainSession.getDomainSessionInfo().getDomainSessionId(),
                    msgType,
                    callback);

        	if(argument.getDelivery() != null &&
        	        argument.getDelivery().getInTrainingApplication() != null &&
        	        argument.getDelivery().getInTrainingApplication().getEnabled() != null &&
        	        argument.getDelivery().getInTrainingApplication().getEnabled().equals(BooleanEnum.TRUE) &&
        	        argument.getDelivery().getInTrainingApplication().getMobileOption() != null &&
        	        argument.getDelivery().getInTrainingApplication().getMobileOption().isVibrate()) {

        	    // send a dummy vibration message to trigger vibration alongside embedded feedback messages
        	    List<Integer> vibratePattern = new ArrayList<>();
        	    vibratePattern.add(2000);
        	    vibratePattern.add(5000);
        	    vibratePattern.add(3000);
        	    vibratePattern.add(1000);
        	    vibratePattern.add(10000);

        	    sendDomainSessionMessage(module, vibratePattern, domainSession.getDomainSessionInfo(),
                        domainSession.getDomainSessionInfo().getDomainSessionId(),
                        MessageTypeEnum.VIBRATE_DEVICE_REQUEST,
                        callback);
        	}

        } else {
            logger.error("Could not handle display feedback request for training application: Domain Session ID "
                    + domainSessionId + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Sends a course state object.
     *
     * @param domainSessionId
     *            - the domain session ID of the session sending the message.
     * @param state
     *            - information about the current state of the course.
     * @param callback
     *            - for responses to sending the course state update
     */
    public void sendCourseState(int domainSessionId, CourseState state, MessageCollectionCallback callback) {

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        if (domainSession != null) {

            sendDomainSessionMessage(state, domainSession.getDomainSessionInfo(),
                    domainSession.getDomainSessionInfo().getDomainSessionId(), MessageTypeEnum.COURSE_STATE, callback);

        } else {
            logger.error("Could not send course state: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Displays a guidance screen to the tutor module that has an HTML page
     * embedded in it
     *
     * @param domainSessionId
     *            The domain session ID of the session requesting.
     * @param displayContentTutorRequest
     *            contains information about a guidance page to present
     * @param callback
     *            The message callback handler.
     */
    public void sendDisplayContentRequest(int domainSessionId,
            AbstractDisplayContentTutorRequest displayContentTutorRequest, MessageCollectionCallback callback) {

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        if (domainSession != null) {            

            /* Skip sending this message if in RTA mode */
            if (LessonLevelEnum.RTA.equals(DomainModuleProperties.getInstance().getLessonLevel())) {
                
                if(logger.isInfoEnabled()){
                    logger.info("Skipping display content request because currently running in "+LessonLevelEnum.RTA+" mode. "+domainSession.getDomainSessionInfo());
                }
                if (callback != null) {
                    callback.success();
                }

                return;
            }

            sendDomainSessionMessage(displayContentTutorRequest, domainSession.getDomainSessionInfo(),
                    domainSession.getDomainSessionInfo().getDomainSessionId(),
                    MessageTypeEnum.DISPLAY_CONTENT_TUTOR_REQUEST, callback);

        } else {
            logger.error("Could not display text in the TUI: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Displays lesson material in the Tutor User Interface.
     *
     * @param domainSessionId
     *            The domain session ID of the session requesting.
     * @param lessonMaterialTransition
     *            The list of media to display.
     * @param contentReference
     *            A reference to the content that helped populate the lesson material course object
     * @param callback
     *            The message callback handler.
     */
    public void sendDisplayMediaCollectionRequest(int domainSessionId, final LessonMaterial lessonMaterialTransition,
            String contentReference, MessageCollectionCallback callback) {

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        if (domainSession != null) {
            
            /* Skip sending this media collection request if in RTA mode and this is NOT a mid lesson media  */
            if (LessonLevelEnum.RTA.equals(DomainModuleProperties.getInstance().getLessonLevel()) &&
                    (domainSession.getLessonState() == null || LessonStateEnum.STOPPED.equals(domainSession.getLessonState()))) {
                
                if(logger.isInfoEnabled()){
                    logger.info("Skipping display media collection request because currently running in "+LessonLevelEnum.RTA+" mode.  Only mid-lesson media collection requests are supported. "+domainSession.getDomainSessionInfo());
                }
                if (callback != null) {
                    callback.success();
                }

                return;
            }

            sendDomainSessionMessage(new DisplayMediaCollectionRequest(lessonMaterialTransition, contentReference),
                    domainSession.getDomainSessionInfo(), domainSession.getDomainSessionInfo().getDomainSessionId(),
                    MessageTypeEnum.DISPLAY_LESSON_MATERIAL_TUTOR_REQUEST, callback);

        } else {
            logger.error("Could not display media in the TUI: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Displays mid-lesson media in the Tutor User Interface.
     *
     * @param domainSessionId
     *            The domain session ID of the session requesting.
     * @param lessonMaterial
     *            The list of media to display.
     * @param callback
     *            The message callback handler.
     */
    public void sendDisplayMidLessonMediaRequest(int domainSessionId, final generated.dkf.LessonMaterialList lessonMaterial,
            MessageCollectionCallback callback) {

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        if (domainSession != null) {

            sendDomainSessionMessage(new DisplayMidLessonMediaRequest(lessonMaterial),
                    domainSession.getDomainSessionInfo(), domainSession.getDomainSessionInfo().getDomainSessionId(),
                    MessageTypeEnum.DISPLAY_MIDLESSON_MEDIA_TUTOR_REQUEST, callback);

        } else {
            logger.error("Could not display media in the TUI: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Displays available learner actions in the Tutor User Interface.
     *
     * @param domainSessionId
     *            The domain session ID of the session requesting.
     * @param learnerActionsList
     *            The list of learner actions to display.
     * @param controls
     *            The scenario controls to make available to the tutor. Can be null.
     * @param callback
     *            The message callback handler.
     */
    public void sendDisplayLearnerActionsRequest(int domainSessionId,
            final List<generated.dkf.LearnerAction> learnerActionsList, ScenarioControls controls, MessageCollectionCallback callback) {

        /* Don't send this message if GIFT is in RTA mode */
        if (LessonLevelEnum.RTA.equals(DomainModuleProperties.getInstance().getLessonLevel())) {
            callback.success();
            return;
        }

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        if (domainSession != null) {

            sendDomainSessionMessage(new DisplayLearnerActionsTutorRequest(learnerActionsList, controls),
                    domainSession.getDomainSessionInfo(), domainSession.getDomainSessionInfo().getDomainSessionId(),
                    MessageTypeEnum.DISPLAY_LEARNER_ACTIONS_TUTOR_REQUEST, callback);

        } else {
            logger.error("Could not display learner actions in the TUI: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Display a chat window in the Tutor User Interface.
     *
     * @param domainSessionId
     *            The domain session ID of the session requesting.
     * @param request
     *            The chat window information.
     * @param callback
     *            The message callback handler.
     */
    public void sendDisplayChatWindowRequest(int domainSessionId, DisplayChatWindowRequest request,
            MessageCollectionCallback callback) {

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        if (domainSession != null) {
            
            /* Skip sending this chat window request if in RTA mode  */
            if (LessonLevelEnum.RTA.equals(DomainModuleProperties.getInstance().getLessonLevel())) {
                
                if(logger.isInfoEnabled()){
                    logger.info("Skipping display chat window request because currently running in "+LessonLevelEnum.RTA+" mode. "+domainSession.getDomainSessionInfo());
                }
                if (callback != null) {
                    callback.success();
                }

                return;
            }

            sendDomainSessionMessage(request, domainSession.getDomainSessionInfo(),
                    domainSession.getDomainSessionInfo().getDomainSessionId(),
                    MessageTypeEnum.DISPLAY_CHAT_WINDOW_REQUEST, callback);

        } else {
            logger.error("Could not display chat window in the TUI: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Display a chat window update in the Tutor User Interface.
     *
     * @param domainSessionId
     *            The domain session ID of the session requesting.
     * @param request
     *            - the chat window update information
     * @param callback
     *            The message callback handler.
     */
    public void sendDisplayChatWindowUpdateRequest(int domainSessionId, final DisplayChatWindowUpdateRequest request,
            MessageCollectionCallback callback) {

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        if (domainSession != null) {

            sendDomainSessionMessage(request, domainSession.getDomainSessionInfo(),
                    domainSession.getDomainSessionInfo().getDomainSessionId(),
                    MessageTypeEnum.DISPLAY_CHAT_WINDOW_UPDATE_REQUEST, callback);

        } else {
            logger.error("Could not display chat window update in the TUI: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Send the survey response that came from a training application instance
     * to the tutor. The tutor should handle the response with some visual
     * indication that the survey being presented was answered.
     *
     * @param domainSessionId
     *            the domain session id associated with the session the survey
     *            response is for, used to identify the appropriate message
     *            recipient as well as deliver the response to the appropriate
     *            tutor user interface.
     * @param surveyResponse
     *            contains the survey answers provided by a learner through a
     *            training application
     */
    public void sendTrainingApplicationSurveyResponse(int domainSessionId, SurveyResponse surveyResponse) {

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        if (domainSession != null) {

            if(DomainModuleProperties.getInstance().getLessonLevel() == LessonLevelEnum.RTA){
                // there is no tutor module, so no need to select a UI component of a survey question choice
                return;
            }

            sendDomainSessionMessage(ModuleTypeEnum.TUTOR_MODULE, surveyResponse, domainSession.getDomainSessionInfo(),
                    domainSession.getDomainSessionInfo().getDomainSessionId(),
                    MessageTypeEnum.TRAINING_APP_SURVEY_RESPONSE, null);
        } else {
            logger.error("Could not send training application survey response: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Send the survey submit that came from a training application instance to
     * the tutor. The tutor should handle the response with some visual
     * indication that the survey being presented was submitted.
     *
     * @param domainSessionId
     *            the domain session id associated with the session the survey
     *            response is for, used to identify the appropriate message
     *            recipient as well as deliver the response to the appropriate
     *            tutor user interface.
     */
    public void sendTrainingApplicationSurveySubmit(int domainSessionId) {

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        if (domainSession != null) {

            if(DomainModuleProperties.getInstance().getLessonLevel() == LessonLevelEnum.RTA){
                // there is no tutor module, so no need to select the submit survey button on the TUI
                return;
            }

            sendDomainSessionMessage(ModuleTypeEnum.TUTOR_MODULE, null, domainSession.getDomainSessionInfo(),
                    domainSession.getDomainSessionInfo().getDomainSessionId(),
                    MessageTypeEnum.TRAINING_APP_SURVEY_SUBMIT, null);
        } else {
            logger.error("Could not send training application survey submit: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Displays a set of survey questions in the Tutor User Interface.  This can be as a survey course object,
     * in the Recall phase of an adaptive course flow course object or during a training application course object 
     * just to name a few examples.
     *
     * @param domainSessionId
     *            The domain session ID of the requesting session.
     * @param survey
     *            The survey to display.
     * @param fullScreen
     *            whether to display the survey in full screen mode
     * @param surveyFinishedTutorCallback
     *            The message callback handler for the tutor when the survey has
     *            been finished. Can't be null.
     * @param surveyAnsweredGatewayHandling
     *            whether the gateway should be notified to allow the learner to
     *            answer the question natively in a training application that is
     *            currently running as part of the ongoing course. If null the
     *            gateway will not receive the survey notification and the
     *            learner can't answer the survey through the gateway.
     */
    public void sendDisplaySurveyRequest(int domainSessionId, final Survey survey, boolean fullScreen,
            MessageCollectionCallback surveyFinishedTutorCallback, boolean surveyAnsweredGatewayHandling) {

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        if (domainSession != null) {
            
            /* Skip sending this survey request if in RTA mode and this is NOT a mid lesson survey */
            if (LessonLevelEnum.RTA.equals(DomainModuleProperties.getInstance().getLessonLevel()) &&
                    (domainSession.getLessonState() == null || LessonStateEnum.STOPPED.equals(domainSession.getLessonState()))) {
                
                if(logger.isInfoEnabled()){
                    logger.info("Skipping display survey request because currently running in "+LessonLevelEnum.RTA+" mode.  Only mid-lesson surveys are supported. "+domainSession.getDomainSessionInfo());
                }
                if (surveyFinishedTutorCallback != null) {
                    surveyFinishedTutorCallback.success();
                }

                return;
            }

            // process survey HTML elements
            SurveyHTMLElementsHandler.processSurveyHTMLElements(survey);

            /* Create and send the survey request */
            DisplaySurveyTutorRequest surveyRequest = new DisplaySurveyTutorRequest(survey);
            surveyRequest.setFullscreen(fullScreen);

            if (surveyAnsweredGatewayHandling) {
                sendDomainSessionMessage(surveyRequest, domainSession.getDomainSessionInfo(),
                        domainSession.getDomainSessionInfo().getDomainSessionId(),
                        MessageTypeEnum.SURVEY_PRESENTED_NOTIFICATION, null);
            }

            sendDomainSessionMessage(surveyRequest, domainSession.getDomainSessionInfo(),
                    domainSession.getDomainSessionInfo().getDomainSessionId(),
                    MessageTypeEnum.DISPLAY_SURVEY_TUTOR_REQUEST, surveyFinishedTutorCallback);

        } else {
            logger.error("Could not display survey in the TUI: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Request the branch path history data based on the ids provided.
     *
     * @param domainSessionId the domain session needing the information
     * @param branchHistoryPaths contains ids used to retrieve the information
     *        from the database
     * @param callback used to provided the database data for this request as a
     *        reply
     * @throws DetailedException if the domain session could not be found. The
     *         callback will not be called in this case.
     */
    public void sendBranchPathHistoryInfoRequest(int domainSessionId, List<BranchPathHistory> branchHistoryPaths,
            MessageCollectionCallback callback) throws DetailedException {

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        if (domainSession != null) {

            sendDomainSessionMessage(branchHistoryPaths, domainSession.getDomainSessionInfo(),
                    domainSession.getDomainSessionInfo().getDomainSessionId(),
                    MessageTypeEnum.BRANCH_PATH_HISTORY_REQUEST, callback);
        } else {
            throw new DetailedException("Unable to request Branch Path History information.",
                    "Can't find the domain session with id '" + domainSessionId
                            + "' therefore unable to request the branch history for " + branchHistoryPaths,
                    null);
        }
    }

    /**
     * Update the branch path history data based in the database.
     *
     * @param domainSessionId
     *            the domain session updating the information
     * @param branchHistoryPath
     *            contains information to place in the database
     * @param callback
     *            used to confirm the database was updated
     * @throws DetailedException
     *             if the domain session could not be found. The callback will
     *             not be called in this case.
     */
    public void sendBranchPathHistoryUpdate(int domainSessionId, BranchPathHistory branchHistoryPath,
            MessageCollectionCallback callback) throws DetailedException {

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        if (domainSession != null) {

            sendDomainSessionMessage(branchHistoryPath, domainSession.getDomainSessionInfo(),
                    domainSession.getDomainSessionInfo().getDomainSessionId(),
                    MessageTypeEnum.BRANCH_PATH_HISTORY_UPDATE, callback);
        } else {
            throw new DetailedException("Unable to update the Branch Path History information.",
                    "Can't find the domain session with id '" + domainSessionId
                            + "' therefore unable to update the branch history for " + branchHistoryPath,
                    null);
        }
    }

    /**
     * Displays the AAR scores in the Tutor User Interface.
     *
     * @param domainSessionId
     *            The domain session ID of the requesting session.
     * @param title the authorable title of this structured review.  Can't be null or empty.
     * @param fullscreen
     *            whether or not to display the AAR in full screen mode on the
     *            tutor
     * @param events
     *            The list of events to display in the AAR
     * @param callback
     *            The message callback handler.
     */
    public void sendDisplayAARRequest(int domainSessionId, String title, boolean fullscreen,
            final List<AbstractAfterActionReviewEvent> events, MessageCollectionCallback callback) {

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        if (domainSession != null) {
            
            /* Skip sending this AAR request if in RTA mode  */
            if (LessonLevelEnum.RTA.equals(DomainModuleProperties.getInstance().getLessonLevel())) {
                
                if(logger.isInfoEnabled()){
                    logger.info("Skipping display structured review (AAR) request because currently running in "+LessonLevelEnum.RTA+" mode. "+domainSession.getDomainSessionInfo());
                }
                if (callback != null) {
                    callback.success();
                }

                return;
            }

            DisplayAfterActionReviewTutorRequest request = new DisplayAfterActionReviewTutorRequest(title, events);
            request.setFullScreen(fullscreen);
            sendDomainSessionMessage(request, domainSession.getDomainSessionInfo(),
                    domainSession.getDomainSessionInfo().getDomainSessionId(),
                    MessageTypeEnum.DISPLAY_AAR_TUTOR_REQUEST, callback);

        } else {
            logger.error("Could not display AAR in the TUI: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Sends a request to the LMS to get the records of course taken
     *
     * @param domainSessionId
     *            The domain session ID of the requesting session.
     * @param lmsUsername
     *            The LMS username of the user to get records of
     * @param publishedScores
     *            collection of identifiable information for published score
     *            located in the LMS
     * @param callback
     *            The message callback handler
     */
    public void sendLmsDataRequest(int domainSessionId, final String lmsUsername,
            Collection<PublishLessonScoreResponse> publishedScores, MessageCollectionCallback callback) {

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);

        if (domainSession != null) {

            LMSDataRequest request = new LMSDataRequest(lmsUsername);
            request.setPublishedScores(publishedScores);

            sendDomainSessionMessage(request, domainSession.getDomainSessionInfo(),
                    domainSession.getDomainSessionInfo().getDomainSessionId(), MessageTypeEnum.LMS_DATA_REQUEST,
                    callback);

        } else {

            logger.error("Could not request LMS Data: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Loads a scenario.
     *
     * @param domainSessionId
     *            The domain session ID of the requesting session.
     * @param simanLoad
     *            - the SIMAN load object to send. Includes arguments used in
     *            loading the scenario (e.g. scenario name)
     * @param callback
     *            The message callback handler.
     * @param isEmbedded
     *            A flag that indicates whether the scenario is being run within an
     *            embedded training application or an interop training application.
     *            If its an interop application, the message is sent to the Gateway.
     *            Otherwise its an embedded application and the message is sent to the
     *            Tutor Module.
     */
    public void sendLoadScenario(int domainSessionId, Siman simanLoad, MessageCollectionCallback callback, boolean isEmbedded) {

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        simanLoad.setRouteType(isEmbedded ? TrainingAppRouteTypeEnum.EMBEDDED : TrainingAppRouteTypeEnum.INTEROP);
        if (domainSession != null) {

            if(isEmbedded && DomainModuleProperties.getInstance().getLessonLevel() == LessonLevelEnum.RTA){
                // there is no tutor module, embedded applications can't be presented
                callback.failure("Embedded applications aren't supported in "+DomainModuleProperties.getInstance().getLessonLevel()+
                        " lesson level configuration.  Therefore the load scenario request will not be delivered.\n"+simanLoad);
            }

            ModuleTypeEnum module = isEmbedded ? ModuleTypeEnum.TUTOR_MODULE : ModuleTypeEnum.GATEWAY_MODULE;
            sendDomainSessionMessage(module, simanLoad, domainSession.getDomainSessionInfo(),
                    domainSession.getDomainSessionInfo().getDomainSessionId(), MessageTypeEnum.SIMAN, callback);

        } else {
            logger.error("Could not load scenario: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Starts a scenario.
     *
     * @param domainSessionId
     *            The domain session ID of the requesting session.
     * @param callback
     *            The message callback handler.
     * @param isEmbedded
     *            A flag that indicates whether the scenario is being run within an
     *            embedded training application or an interop training application.
     *            If its an interop application, the message is sent to the Gateway.
     *            Otherwise its an embedded application and the message is sent to the
     *            Tutor Module.
     */
    public void sendStartScenario(int domainSessionId, MessageCollectionCallback callback, boolean isEmbedded) {

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        Siman simanStart = Siman.CreateStart();
        simanStart.setRouteType(isEmbedded ? TrainingAppRouteTypeEnum.EMBEDDED : TrainingAppRouteTypeEnum.INTEROP);
        if (domainSession != null) {

            if(isEmbedded && DomainModuleProperties.getInstance().getLessonLevel() == LessonLevelEnum.RTA){
                // there is no tutor module, embedded applications can't be presented
                callback.failure("Embedded applications aren't supported in "+DomainModuleProperties.getInstance().getLessonLevel()+
                        " lesson level configuration.  Therefore the start scenario request will not be delivered.");
            }

            ModuleTypeEnum module = isEmbedded ? ModuleTypeEnum.TUTOR_MODULE : ModuleTypeEnum.GATEWAY_MODULE;
            final DomainSession domainInfo = domainSession.getDomainSessionInfo();
            final int infoDomainSessionId = domainInfo.getDomainSessionId();
            sendDomainSessionMessage(module, simanStart, domainInfo, infoDomainSessionId, MessageTypeEnum.SIMAN,
                    callback);
        } else {
            logger.error("Could not start scenario: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }
    }

    @Override
    public void sendKnowledgeSessionCreatedMessage(AbstractKnowledgeSession knowledgeSession) {
        if (knowledgeSession == null) {
            throw new IllegalArgumentException("The parameter 'knowledgeSession' cannot be null.");
        }

        int dsId = knowledgeSession.getHostSessionMember().getDomainSessionId();
        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(dsId);
        if (domainSession != null) {
            final DomainSession domainInfo = domainSession.getDomainSessionInfo();
            
            KnowledgeSessionCreated kcreated = new KnowledgeSessionCreated(knowledgeSession);
            
            // notify any core module that a session was created.  Give mission metadata for the session but not who has joined.
            // This also allows the monitors to see this info
            sendDomainSessionMessage(kcreated,
                    domainInfo, MessageTypeEnum.KNOWLEDGE_SESSION_CREATED, null);
            
            if(haveSubjectClient(domainSession.getDomainSessionInfo(), ModuleTypeEnum.GATEWAY_MODULE)){
                // notify the gateway as well so that GW interop plugins can manage internal logic and external systems
                // can decide what to do with this information (possibly display it)
                // Some courses might not need, therefore not have, a GW module connection.
                sendDomainSessionMessage(ModuleTypeEnum.GATEWAY_MODULE, kcreated,
                        domainInfo, dsId, MessageTypeEnum.KNOWLEDGE_SESSION_CREATED, null);
            }
        }
    }

    /**
     * Sends a message to update the tutor that the state of an active knowledge has been updated.
     *
     * @param domainSessionId
     *            The domain session ID of the requesting session.
     * @param request optional parameter used to filter the knowledge sessions that are sent
     * @param callback
     *            The message callback handler.  Can be null.
     */
    public void sendKnowledgeSessionsUpdated(int domainSessionId, KnowledgeSessionsRequest request, MessageCollectionCallback callback) {

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        if (domainSession != null) {

                boolean hasGW = domainSession.getDomainSessionInfo().isGatewayConnected();
                Map<Integer, AbstractKnowledgeSession> knowledgeSessionMap = KnowledgeSessionManager.getInstance().getKnowledgeSessions(request);
                KnowledgeSessionsReply reply = new KnowledgeSessionsReply(knowledgeSessionMap);
                reply.setCanHost(hasGW);

                // in RTA mode there is no tutor module, the GW module interfaces with some external application.
                // Therefore send this update to the appropriate module that hopefully has some interface to handle/show the update.
                // (e.g. tutor module's client side GIFT lobby UI)
                ModuleTypeEnum uiModuleType = DomainModuleProperties.getInstance().getLessonLevel() == LessonLevelEnum.RTA ?
                        ModuleTypeEnum.GATEWAY_MODULE : ModuleTypeEnum.TUTOR_MODULE;
                
                // send to learner to set the knowledge session information
                // send to LMS to keep track of session members as they join/leave/assigned
                ModuleTypeEnum[] toList = {ModuleTypeEnum.LEARNER_MODULE, ModuleTypeEnum.LMS_MODULE, uiModuleType};

                sendDomainSessionMessage(toList, reply,
                        domainSession.getDomainSessionInfo(), domainSession.getDomainSessionInfo().getDomainSessionId(),
                        domainSession.getDomainSessionInfo().getExperimentId(),
                        MessageTypeEnum.KNOWLEDGE_SESSION_UPDATED_REQUEST, callback);


        } else {
            logger.error("Could not send Knowledge Session Updated message to tutor: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Sends a message to stop the scenario of a domain session
     *
     * @param domainSessionId
     *            The domain session ID of the requesting session.
     * @param callback
     *            The message callback handler.
     * @param isEmbedded
     *            A flag that indicates whether the scenario is being run within an
     *            embedded training application or an interop training application.
     *            If its an interop application, the message is sent to the Gateway.
     *            Otherwise its an embedded application and the message is sent to the
     *            Tutor Module.
     */
    public void sendStopScenario(int domainSessionId, MessageCollectionCallback callback, boolean isEmbedded) {

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        Siman simanStop = Siman.CreateStop();
        simanStop.setRouteType(isEmbedded ? TrainingAppRouteTypeEnum.EMBEDDED : TrainingAppRouteTypeEnum.INTEROP);
        if (domainSession != null) {

            if(isEmbedded && DomainModuleProperties.getInstance().getLessonLevel() == LessonLevelEnum.RTA){
                // there is no tutor module, embedded applications can't be presented
                callback.failure("Embedded applications aren't supported in "+DomainModuleProperties.getInstance().getLessonLevel()+
                        " lesson level configuration.  Therefore unable to send the stop scenario message for domain session "+domainSessionId+".");
            }

            ModuleTypeEnum module = isEmbedded ? ModuleTypeEnum.TUTOR_MODULE : ModuleTypeEnum.GATEWAY_MODULE;
            sendDomainSessionMessage(module, simanStop,
                    domainSession.getDomainSessionInfo(), domainSession.getDomainSessionInfo().getDomainSessionId(),
                    MessageTypeEnum.SIMAN, callback);

        } else {
            logger.error("Could not stop scenario: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Sends a message to the gateway module to restart the scenario.
     *
     * @param domainSessionId
     *            The domain session ID of the requesting session.
     * @param callback
     *            The message callback handler.
     */
    public void sendResetScenario(int domainSessionId, MessageCollectionCallback callback, boolean isEmbedded) {

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        Siman simanReset = Siman.CreateReset();
        simanReset.setRouteType(isEmbedded ? TrainingAppRouteTypeEnum.EMBEDDED : TrainingAppRouteTypeEnum.INTEROP);
        if (domainSession != null) {

            if(isEmbedded && DomainModuleProperties.getInstance().getLessonLevel() == LessonLevelEnum.RTA){
                // there is no tutor module, embedded applications can't be presented
                callback.failure("Embedded applications aren't supported in "+DomainModuleProperties.getInstance().getLessonLevel()+
                        " lesson level configuration.  Therefore unable to send the reset scenario message for domain session "+domainSessionId+".");
            }

            ModuleTypeEnum module = isEmbedded ? ModuleTypeEnum.TUTOR_MODULE : ModuleTypeEnum.GATEWAY_MODULE;
            sendDomainSessionMessage(module, simanReset,
                    domainSession.getDomainSessionInfo(), domainSession.getDomainSessionInfo().getDomainSessionId(),
                    MessageTypeEnum.SIMAN, callback);

        } else {
            logger.error("Could not reset scenario: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Sends a message to the gateway module to pause the scenario.
     *
     * @param domainSessionId
     *            The domain session ID of the requesting session.
     * @param callback
     *            The message callback handler.
     */
    public void sendPauseScenario(int domainSessionId, MessageCollectionCallback callback, boolean isEmbedded) {

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        Siman simanPause = Siman.CreatePause();
        simanPause.setRouteType(isEmbedded ? TrainingAppRouteTypeEnum.EMBEDDED : TrainingAppRouteTypeEnum.INTEROP);
        if (domainSession != null) {

            if(isEmbedded && DomainModuleProperties.getInstance().getLessonLevel() == LessonLevelEnum.RTA){
                // there is no tutor module, embedded applications can't be presented
                callback.failure("Embedded applications aren't supported in "+DomainModuleProperties.getInstance().getLessonLevel()+
                        " lesson level configuration.  Therefore unable to send the pause scenario message for domain session "+domainSessionId+".");
            }

            ModuleTypeEnum module = isEmbedded ? ModuleTypeEnum.TUTOR_MODULE : ModuleTypeEnum.GATEWAY_MODULE;
            sendDomainSessionMessage(module, simanPause,
                    domainSession.getDomainSessionInfo(), domainSession.getDomainSessionInfo().getDomainSessionId(),
                    MessageTypeEnum.SIMAN, callback);

        } else {
            logger.error("Could not pause scenario: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Sends a message to the gateway module to resume the scenario.
     *
     * @param domainSessionId
     *            The domain session ID of the requesting session.
     * @param callback
     *            The message callback handler.
     */
    public void sendResumeScenario(int domainSessionId, MessageCollectionCallback callback, boolean isEmbedded) {

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        Siman simanResume = Siman.CreateResume();
        simanResume.setRouteType(isEmbedded ? TrainingAppRouteTypeEnum.EMBEDDED : TrainingAppRouteTypeEnum.INTEROP);
        if (domainSession != null) {

            if(isEmbedded && DomainModuleProperties.getInstance().getLessonLevel() == LessonLevelEnum.RTA){
                // there is no tutor module, embedded applications can't be presented
                callback.failure("Embedded applications aren't supported in "+DomainModuleProperties.getInstance().getLessonLevel()+
                        " lesson level configuration.  Therefore unable to send the resume scenario message for domain session "+domainSessionId+".");
            }

            ModuleTypeEnum module = isEmbedded ? ModuleTypeEnum.TUTOR_MODULE : ModuleTypeEnum.GATEWAY_MODULE;
            sendDomainSessionMessage(module, simanResume,
                    domainSession.getDomainSessionInfo(), domainSession.getDomainSessionInfo().getDomainSessionId(),
                    MessageTypeEnum.SIMAN, callback);

        } else {
            logger.error("Could not resume scenario: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Sends a message to initialize the lesson
     *
     * @param domainSessionId
     *            The domain session ID of the requesting session.
     * @param contentReference a reference to the content to be displayed that is unique to this course.  Can't be null or empty.
     * @param callback
     *            The message callback handler.
     */
    public void sendInitializeLesson(int domainSessionId, String contentReference, MessageCollectionCallback callback) {

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        if (domainSession != null) {

            sendDomainSessionMessage(new InitializeLessonRequest(contentReference), domainSession.getDomainSessionInfo(),
                    domainSession.getDomainSessionInfo().getDomainSessionId(),
                    MessageTypeEnum.INITIALIZE_LESSON_REQUEST, callback);

        } else {
            logger.error("Could not initialize lesson: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Sends a message to initialize the lesson
     *
     * @param domainSessionId
     *            The domain session ID of the requesting session.
     * @param pedRequest
     *            - the InitializatePedagogicalModelRequest to send.
     * @param callback
     *            The message callback handler.
     */
    public void sendInitializePedagogicalModel(int domainSessionId, InitializePedagogicalModelRequest pedRequest,
            MessageCollectionCallback callback) {

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        if (domainSession != null) {

            sendDomainSessionMessage(pedRequest, domainSession.getDomainSessionInfo(),
                    domainSession.getDomainSessionInfo().getDomainSessionId(),
                    MessageTypeEnum.INITIALIZE_PEDAGOGICAL_MODEL_REQUEST, callback);

        } else {
            logger.error("Could not initialize pedagogical model: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Send a request to instantiate the learner model
     *
     * @param userSession
     *            - information about the user session (including the unique
     *            user id of the learner) the request is associated with
     * @param learnerRequest
     *            - the request to send to the learner
     * @param callback
     *            - used to handle responses to this request
     */
    public void sendInstantiateLearnerRequest(UserSession userSession, InstantiateLearnerRequest learnerRequest,
            MessageCollectionCallback callback) {

        sendUserSessionMessage(learnerRequest, userSession, MessageTypeEnum.INSTANTIATE_LEARNER_REQUEST, callback);
    }

    /**
     * Sends a message to the tutor module to notify that the lesson has started
     *
     * @param domainSessionId
     *            The domain session ID of the requesting session.
     * @param callback
     *            The message callback handler.
     */
    public void sendLessonStarted(int domainSessionId, MessageCollectionCallback callback) {
        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        if (domainSession != null) {

            sendDomainSessionMessage(null, domainSession.getDomainSessionInfo(),
                    MessageTypeEnum.LESSON_STARTED,
                    callback);

        } else {
            logger.error("Could not notify lesson started: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Sends a message to the tutor, pedagogical, learner, and gateway module to notify that the lesson has
     * finished
     *
     * @param domainSessionId
     *            The domain session ID of the requesting session.
     * @param lessonCompleted information about why the lesson is ending.
     * @param callback
     *            The message callback handler.
     */
    public void sendLessonCompleted(int domainSessionId, LessonCompleted lessonCompleted, MessageCollectionCallback callback) {
        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        if (domainSession != null) {
            
            sendDomainSessionMessage(lessonCompleted, domainSession.getDomainSessionInfo(),
                    domainSession.getDomainSessionInfo().getDomainSessionId(), MessageTypeEnum.LESSON_COMPLETED,
                    callback);

        } else {
            logger.error("Could not notify lesson completed: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Sends a message to the tutor, pedagogical, and learner module to notify that the lesson has
     * finished. This method is used when an embedded training application has completed. Used instead
     * of the sendLessonCompleted method because the Gateway is not used with the embedded training
     * application and therefore does not require a message to be sent to it.
     *
     * @param domainSessionId
     *            The domain session ID of the requesting session.
     * @param lessonCompleted information about why the lesson is ending.
     * @param callback
     *            The message callback handler.
     */
    public void sendLessonCompletedNotToGateway(int domainSessionId, LessonCompleted lessonCompleted, MessageCollectionCallback callback) {
        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);

        if (domainSession != null) {

            List<ModuleTypeEnum> modulesList = getRecepients(MessageTypeEnum.LESSON_COMPLETED);
            modulesList.remove(ModuleTypeEnum.GATEWAY_MODULE);  // because this method is meant to not send it to the Gateway
            if(DomainModuleProperties.getInstance().getLessonLevel() == LessonLevelEnum.RTA){
                // there is no tutor module
                modulesList.remove(ModuleTypeEnum.TUTOR_MODULE);
            }
            
            ModuleTypeEnum[] recipients = modulesList.toArray(new ModuleTypeEnum[modulesList.size()]);
            sendDomainSessionMessage(
                    recipients,
                    lessonCompleted,
                    domainSession.getDomainSessionInfo(),
                    domainSession.getDomainSessionInfo().getDomainSessionId(),
                    null,
                    MessageTypeEnum.LESSON_COMPLETED,
                    callback);
        } else {
            logger.error("Could not notify lesson completed: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Sets the environment condition based on the enumeration.
     *
     * @param domainSessionId
     *            The domain session ID of the requesting session.
     * @param val
     *            The environment type to set
     * @param strategyStress an optional value of stress associated with this strategy. Can be null.
     * See ProxyTaskAssessment for ranges on stress value.
     * @param callback
     *            The message callback handler.
     */
    public void sendSetEnvironment(int domainSessionId, final generated.dkf.EnvironmentAdaptation val, final Double strategyStress,
            MessageCollectionCallback callback) {

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        if (domainSession != null) {

            EnvironmentControl envControl = new EnvironmentControl(val);
            envControl.setStress(strategyStress);
            sendDomainSessionMessage(envControl, domainSession.getDomainSessionInfo(),
                    domainSession.getDomainSessionInfo().getDomainSessionId(), MessageTypeEnum.ENVIRONMENT_CONTROL,
                    callback);
        } else {
            logger.error("Could not send Set Environment: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }
    }

    /**
     * Publishes a course's score to the system
     *
     * @param domainSessionId
     *            The domain session's unique ID.
     * @param lmsUsername
     *            The user of the domain session LMS username.
     * @param courseRecord
     *            The course score hierarchy
     * @param callback
     *            The callback for the message's response.
     */
    public void sendPublishScore(int domainSessionId, final String lmsUsername, final LMSCourseRecord courseRecord,
            MessageCollectionCallback callback) {

        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        if (domainSession != null) {

            PublishLessonScore publishLessonScore = new PublishLessonScore(lmsUsername, courseRecord, domainSession.getCourseManager().getConcepts());
            if(logger.isInfoEnabled()){
                logger.info("Sending publish lesson score of " + publishLessonScore + ".");
            }

            sendDomainSessionMessage(publishLessonScore, domainSession.getDomainSessionInfo(),
                    domainSession.getDomainSessionInfo().getDomainSessionId(),
                    MessageTypeEnum.PUBLISH_LESSON_SCORE_REQUEST, callback);

        } else {
            logger.error("Could not publish score: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }
    }
    
    /**
     * Send the performance assessment message to the appropriate module(s).
     *
     * @param assessment
     *            - the performance assessment message to send
     * @param domainSession
     *            information about the domain session the survey was completed
     *            in
     */
    public void sendPerformanceAssessment(PerformanceAssessment assessment, DomainSession domainSession) {
        
        sendDomainSessionMessage(assessment, domainSession, domainSession.getDomainSessionId(),
                MessageTypeEnum.PERFORMANCE_ASSESSMENT, null);

        /* Clear change reasons in performance assessment after it has been
         * sent */
        final BaseDomainSession bDomainSession = domainSessionIdToDomainSession.get(domainSession.getDomainSessionId());
        if(bDomainSession != null) {
            bDomainSession.clearObserverMetadata();
        }
        
        if (assessment != null && assessment.getTasks() != null) {
            for (TaskAssessment taskAssessment : assessment.getTasks()) {
                
                if(taskAssessment != null) {
                    
                     /* This should be performed after the performance assessment has been sent since
                      * we do not want to persist the reason across future performance assessment
                      * messages */
                    taskAssessment.clearObserverMetadata();
                }
            }
        }
    }

    /**
     * Send the lesson graded score request message to the appropriate module(s).
     *
     * @param gradedScoreNode the graded score node data
     * @param courseConcepts the course concepts
     * @param domainSession information about the domain session the survey was completed in
     */
    public void sendGradedScoreNode(GradedScoreNode gradedScoreNode, List<String> courseConcepts, DomainSession domainSession) {

        LtiLessonGradedScoreRequest request = new LtiLessonGradedScoreRequest(gradedScoreNode, courseConcepts);
        sendDomainSessionMessage(request, domainSession, domainSession.getDomainSessionId(), MessageTypeEnum.LESSON_GRADED_SCORE_REQUEST,
                null);
    }

    /**
     * Sends a create user message to the UMS
     *
     * @param isMale
     *            If the user to be created is male
     * @param lmsUsername
     *            The LMS username of the user to be created
     * @param experimentId
     *            An optional experiment ID to use when creating a subject for
     *            an experiment
     * @param callback
     *            Callback for the result of the message
     */
    public void sendCreateUserMessage(final boolean isMale, final String lmsUsername, final String experimentId,
            final MessageCollectionCallback callback) {

        // get UMS connection
        MessageCollectionCallback umsCallback = new MessageCollectionCallback() {

            @Override
            public void success() {

                final UserData userData = new UserData(lmsUsername, isMale ? GenderEnum.MALE : GenderEnum.FEMALE);

                if (experimentId != null) {
                    userData.setExperimentId(experimentId);
                }

                if (logger.isInfoEnabled()){
                    logger.info("Sending create new user request message for " + userData);
                }
                sendMessage(userData, MessageTypeEnum.NEW_USER_REQUEST, callback);
            }

            @Override
            public void received(final Message aMsg) {

            }

            @Override
            public void failure(Message nackMsg) {
                callback.failure(nackMsg);
            }

            @Override
            public void failure(String why) {
                callback.failure(why);
            }
        };

        selectUMSModule(umsCallback);
    }

    /**
     * Sends a {@link AuthorizeStrategiesRequest} for a provided list of
     * {@link Strategy}.
     *
     * @param strategies The {@link List} of {@link Strategy} for which to
     *        request execution.
     * @param domainSession The {@link DomainSession} for which the strategies
     *        are being requested.
     */
    public void sendAuthorizeStrategiesRequest(Map<String, List<StrategyToApply>> strategies, DomainSession domainSession) {

        AuthorizeStrategiesRequest request = new AuthorizeStrategiesRequest(strategies, null);
        BaseDomainSession bds = domainSessionIdToDomainSession.get(domainSession.getDomainSessionId());
        
        modifyRequestToPrependDomainContentServerAddress(request, bds);

        Set<String> attachedWebMonitors = domainSessionIdToWebMonitorInboxes.get(domainSession.getDomainSessionId());
        if(DomainModuleProperties.getInstance().getLessonLevel() == LessonLevelEnum.RTA){
            // send the request to the GW module because an external system is controlling GIFT

            sendDomainSessionMessage(
                    ModuleTypeEnum.GATEWAY_MODULE,
                    request,
                    domainSession,
                    domainSession.getDomainSessionId(),
                    MessageTypeEnum.AUTHORIZE_STRATEGIES_REQUEST,
                    null);

        }

        if(CollectionUtils.isNotEmpty(attachedWebMonitors)) {
            // there are zero or more monitors (e.g. game master)

            for(String destination : attachedWebMonitors) {
                sendDomainSessionMessage(
                        destination,
                        request,
                        domainSession,
                        domainSession.getDomainSessionId(),
                        domainSession.getExperimentId(),
                        MessageTypeEnum.AUTHORIZE_STRATEGIES_REQUEST,
                        null);
            }
        }else if(DomainModuleProperties.getInstance().getLessonLevel() != LessonLevelEnum.RTA){
            // there are NO monitors (e.g. game master) and the lesson level is NOT RTA (which means there is no external system controlling GIFT)
            // Create an apply strategies message and send the message to the domain module inbox so that 
            // the log will record it for tools like game master to show in strategy history 
            try {
                List<StrategyToApply> strategiesToExecute = new ArrayList<>();
                for (List<StrategyToApply> strategiesList : strategies.values()) {
                    strategiesToExecute.addAll(strategiesList);
                }
    
                sendDomainSessionMessage(new ApplyStrategies(strategiesToExecute, Constants.AUTO_APPLIED_BY_GIFT, null), 
                        bds.getDomainSessionInfo(), MessageTypeEnum.APPLY_STRATEGIES, null);

            } catch (Exception e) {
                logger.error("There was a problem applying short circuited strategies.", e);
            }
            return;
        }

    }

    /**
     * Modifies the {@link AuthorizeStrategiesRequest} to prepend the domain content server address to audio files intended for
     * the observer controller
     * 
     * @param request the request to modify
     * @param bds the base domain session
     */
    private void modifyRequestToPrependDomainContentServerAddress(AuthorizeStrategiesRequest request, BaseDomainSession bds) {
        for (List<StrategyToApply> strategiesList : request.getRequests().values()) {
            for (StrategyToApply strat : strategiesList) {
                for (Serializable activity : strat.getStrategy().getStrategyActivities()) {
                    if (activity instanceof InstructionalIntervention) {
                        InstructionalIntervention ii = (InstructionalIntervention) activity;
                        Serializable presentation = ii.getFeedback().getFeedbackPresentation();
                        if (presentation instanceof Audio) {
                            Audio audio = (Audio) presentation;
                            // prepend the domain content address before sending it to the game master
                            // if it is intended for the controller
                            if (audio.getToObserverController() != null) {
                                modifyAudioToPrependDomainContentServerAddress(audio, bds);
                            }
                        }
                    }
                }
            }
        }
    }

    
    /**
     * Modifies an audio feedback object to prepend the domain content server address to audio files
     * 
     * @param audio the audio to modify
     * @param bds the base domain session
     */
    private void modifyAudioToPrependDomainContentServerAddress(Audio audio, BaseDomainSession bds) {
        // prepend logic
        String mp3Path = bds.prependDomainContentServerAddressToFile(audio.getMP3File());
        audio.setMP3File(mp3Path);
        String oggPath = bds.prependDomainContentServerAddressToFile(audio.getOGGFile());
        audio.setOGGFile(oggPath);
    }

    /**
     * Terminate the domain knowledge manager of joined member sessions for the host of a team knowledge
     * session.
     * @param domainSessionIdOfHost the domain session id of a host of a team knowledge session.
     * @param reason The reason for terminating the domain knowledge. Used for logging purposes. Can't be null or empty.
     * @param status information about why the lesson is ending.
     */
    protected void notifyMembersOfKnowledgeSessionEnding(int domainSessionIdOfHost, String reason, LessonCompletedStatusType status){

        Map<Integer, SessionMember> teamMemberSessions = KnowledgeSessionManager.getInstance().getTeamKnowledgeSessionsMembersForHost(domainSessionIdOfHost);
        if(teamMemberSessions != null){
            Iterator<Integer> dsIds = teamMemberSessions.keySet().iterator();
            while(dsIds.hasNext()){
                BaseDomainSession baseDomainSession = domainSessionIdToDomainSession.get(dsIds.next());
                if(baseDomainSession != null){
                    baseDomainSession.terminateDomainKnowledgeSession(reason, status);
                }
            }
        }
    }
    
    /**
     * Notify any joined member sessions that the host domain session is prematurely ending.  This will then
     * terminate those joined member domain sessions as well.  If the domain session id provided is not currently
     * hosting a team knowledge session this method will do nothing.
     * 
     * @param domainSessionIdOfHost the domain session id of the domain session that is ending prematurely.
     * @param reason a human readable reason for the early termination of the domain session.  This is normally displayed
     * to the learner.
     */
    protected void notifyMembersOfDomainSessionTermination(int domainSessionIdOfHost, String reason){
        
        Map<Integer, SessionMember> teamMemberSessions = KnowledgeSessionManager.getInstance().getTeamKnowledgeSessionsMembersForHost(domainSessionIdOfHost);
        if(teamMemberSessions != null){
            Iterator<Integer> dsIds = teamMemberSessions.keySet().iterator();
            while(dsIds.hasNext()){
                BaseDomainSession baseDomainSession = domainSessionIdToDomainSession.get(dsIds.next());
                if(baseDomainSession != null){
                    baseDomainSession.terminationShutdownDisplayError(reason);
                }
            }
        }
    }
    
    /**
     * Notify team knowledge session joiners of a published score event that comes from a team knowledge session.
     * This is necessary so this session can show the scoring results in any future structured review.
     * 
     * @param domainSessionIdOfHost the domain session id of the host of a team knowledge session.  If not
     * a host session id, this method does nothing.
     * @param publishedLessonScoreResponse the response by the LMS to the published lesson score message.  Contains
     * information about the records written to the LMS for future reference. Can't be null.
     * @param rootNode the root node of the score for the team knowledge session.  Can't be null.
     */
    protected void notifyKnowledgeSessionJoinerPublishedScore(int domainSessionIdOfHost, 
            final PublishLessonScoreResponse publishedLessonScoreResponse, 
            final GradedScoreNode rootNode){
        
        Map<Integer, SessionMember> teamMemberSessions = 
                KnowledgeSessionManager.getInstance().getTeamKnowledgeSessionsMembersForHost(domainSessionIdOfHost);
        if(teamMemberSessions != null){
            Iterator<Integer> dsIds = teamMemberSessions.keySet().iterator();
            while(dsIds.hasNext()){
                BaseDomainSession baseDomainSession = domainSessionIdToDomainSession.get(dsIds.next());
                if(baseDomainSession != null){
                    baseDomainSession.addKnowledgeSessionJoinerPublishedScore(publishedLessonScoreResponse, rootNode);
                }
            }
        }
    }

    /**
     * Handle an incoming training app game state message by allowing the
     * appropriate domain session(s) assessment logic to assess its content.
     *
     * @param message
     *            the incoming training application state message to process
     */
    protected void handleTrainingAppGameStateMessage(Message message) {

        synchronized (domainSessionIdToDomainSession) {

            if (message instanceof DomainSessionMessage) {

                DomainSessionMessage domainSessionMessage = (DomainSessionMessage) message;
                BaseDomainSession baseDomainSession = domainSessionIdToDomainSession.get(domainSessionMessage.getDomainSessionId());
                if(baseDomainSession != null){
                    
                    if(domainSessionMessage.getMessageType() == MessageTypeEnum.KNOWLEDGE_SESSION_UPDATED_REQUEST ||
                            domainSessionMessage.getMessageType() == MessageTypeEnum.START_TEAM_KNOWLEDGE_SESSION_REQUEST){
                        // these messages will only come through during an active playback knowledge session and are needed
                        // to build the session (e.g. which learners joined and what team members are they assigned too)
                        try{
                            handleMessage(domainSessionMessage);
                        }catch(Exception e){
                            logger.error("There was a problem handling the domain session message to manage the creation of a knowledge session during playback.\n"+
                                    domainSessionMessage+"\n"+baseDomainSession.getDomainSessionInfo(), e);
                            baseDomainSession.terminateDomainKnowledgeSession("Failed to create the knowledge session for the playback due to an error: "+e.getMessage(), LessonCompletedStatusType.ERROR);
                        }
                    }else{
                        baseDomainSession.handleTrainingAppGameStateMessage(message);
                    }
                }

            } else {

                for (BaseDomainSession ds : domainSessionIdToDomainSession.values()) {
                    ds.handleTrainingAppGameStateMessage(message);
                }
            }
        }
    }

    /**
     * Handle the message containing the metrics data to update the task or concept with.
     *
     * @param message the provider url request message.
     */
    private void handleEvaluatorUpdateRequest(Message message) {
        DomainSessionMessage domainSessionMessage = (DomainSessionMessage) message;
        final int domainSessionId = domainSessionMessage.getDomainSessionId();
        BaseDomainSession ds = domainSessionIdToDomainSession.get(domainSessionId);
        if (ds != null) {
            EvaluatorUpdateRequest request = (EvaluatorUpdateRequest) message.getPayload();

            /* Check if the request has at least one metric populated. If it doesn't, exit method
             * because there is nothing to update. */
            if (!request.hasChanges()) {
                return;
            }

            try {
                // (Note #4941) - don't send to the joiner base domain sessions because it can cause a NPE when
                //                trying to update metrics (e.g. performance, confidence) since those knowledge
                //                sessions aren't fully initialized like the host is.
                ds.handleEvaluatorUpdateRequest(request);
            } catch (Exception e) {
                logger.error("Caught exception while handling notification of an evaluator update request of " + request
                        + ".", e);
                ds.terminateSession(true, "There was a problem handling an evaluator update request notification.",
                        "An exception was thrown that reads:\n" + e.getMessage()
                                + ".\n\nFor additional debug information check the latest Domain module log file at GIFT/output/logger/module/ for more information.");
            }
        } else {
            logger.error("Received evaluator update request message for user " + domainSessionMessage.getUserId()
                    + " in domain session " + domainSessionMessage.getDomainSessionId()
                    + ", but the Domain module doesn't have a domain session with that id.  Checked "
                    + domainSessionIdToDomainSession.size() + " known domain sessions.\n" + message);
        }
    }

    @Override
    protected void cleanup() {
        super.cleanup();

        // Stop sending metrics.
        metrics.stopSending();
    }

    /**
     * Closes a domain session
     *
     * @param domainSession
     *            The ID of the domain session to close
     */
    public void closeDomainSession(DomainSession domainSession) {

        if(logger.isInfoEnabled()){
            logger.info("Request to close domain session: "+domainSession);
        }

        synchronized (domainSessionIdToDomainSession) {
            if (domainSessionIdToDomainSession.containsKey(domainSession.getDomainSessionId())) {

                if(logger.isInfoEnabled()){
                    logger.info("Removing the following domain session because it is closed:\n"+domainSession);
                }
                domainSessionIdToDomainSession.remove(domainSession.getDomainSessionId());

                //
                // update metrics
                //
                metrics.updateMetricCounter(NUM_DOMAIN_SESSIONS_METRIC, domainSessionIdToDomainSession.size());
                if (domainSession.getExperimentId() != null) {
                    metrics.decrementMetricCounter(NUM_EXPERIMENT_DOMAIN_SESSIONS_METRIC);
                }
            }
        }

        try{
            releaseDomainSessionModules(domainSession);
        }catch(Throwable t){
            logger.error("Caught error while trying to release domain session modules for "+domainSession, t);
        }

        boolean removed = allocationStatus.removeUserSession(domainSession);
        if(removed && logger.isDebugEnabled()){
            logger.debug("Successfully removed the following session from the domain module allocated sessions set : "+domainSession);
        }
        
        // delete the runtime course folder that was created for this course execution
        // Note: may have already been cleaned up by the Dashboard depending on how the session ended
        FileServices.getInstance().getFileServices().cleanupCourse(domainSession.getDomainRuntimeId());
    }

    /**
     * Start a file server to host the training material
     *
     * @param fileServerPort
     *            The port to host the file server on
     */
    private void startFileServer(int fileServerPort) {

        String domainDirectory = DomainModuleProperties.getInstance().getDomainDirectory();
        String warDirectory = DomainModuleProperties.getInstance().getCommunicationAppDirectory();
        String trainingAppsMapsDirectory = DomainModuleProperties.getInstance().getTrainingAppsDirectory()
                + File.separator + PackageUtil.getWrapResourcesDir();

        if (fileServerPort > 0 && domainDirectory != null) {
            try {

                Server server = new Server();
                ServerConnector connector = new ServerConnector(server);

                connector.setPort(fileServerPort);
                server.addConnector(connector);

                // define all directories to be hosted by the Domain's file
                // server
                //
                // Nick 2/26/2015: All directories included here will have their
                // contents combined in the file server. So, if
                // directory A and directory B are listed here, then the file
                // server's base directory will contain all of A's
                // files and all of B's files. If A and B both have a file with
                // the same name, then the file server's base
                // directory will contain the copy in the first directory
                // listed.
                ResourceCollection resourceCollection = new ResourceCollection(
                        new String[] { domainDirectory, warDirectory, trainingAppsMapsDirectory });

                WebAppContext context = new WebAppContext();
                context.setContextPath("/");
                context.setBaseResource(resourceCollection);
                context.setDefaultsDescriptor("config/domain/webdefault.xml");
                
                // assign an explicit temporary directory to avoid leaving Jetty's auto-generated ones behind
                context.setTempDirectory(new File("temp/jetty-gift-domain-content"));
                
                /* Ensure static files can be fetched inside a Windows symlink context. 
                 * 
                 * If this is not done, then launching GIFT from a folder whose location
                 * is masked by a symlink (such as how GIFT is normally launched on hosted 
                 * machines) will cause 404 errors when resources from the Domain content 
                 * server are requested because the internal file location in
                 * windows does not match the path that the absolute path that Jetty uses
                 * to reach the file, causing a file validation check in Jetty to fail. */
                context.addAliasCheck(new SameFileAliasChecker());

                // over-ride the default host (this machines IP address) with a
                // host name defined in the properties
                // From
                // http://www.eclipse.org/jetty/documentation/current/configuring-virtual-hosts.html:
                // A virtual host is an alternative name, registered in DNS, for
                // an IP address such that multiple domain
                // names will resolve to the same IP of a shared server
                // instance. If the content to be served to the aliases
                // names is different, then a virtual host needs to be
                // configured for each deployed context to indicate which
                // names a context will respond to.
                String hostname = DomainModuleProperties.getInstance().getDomainContentServerHost();
                if (hostname != null && !hostname.isEmpty()) {
                    context.setVirtualHosts(new String[] { hostname });
                }

                server.setHandler(context);

                server.start();
                if (logger.isInfoEnabled()){
                    logger.info("Training material file server started for directories " + domainDirectory + " and "
                        + warDirectory + " on port " + fileServerPort + ".");
                }

            } catch (Exception e) {
                logger.error("Caught an exception while starting file server", e);
            }
        }
    }

    /**
     * Refresh the available course list by first searching for courses and then
     * determining which are valid.
     *
     * Note: this will only happen in Simple deployment mode (or in Desktop mode if the user
     * is running simple login in desktop mode).
     *
     * @param userId
     *            the unique id of the user to refresh the course list for
     * @param validateLogic
     *            whether or not to call upon the GIFT logic for validation (not
     *            schema validation, not survey validation)
     * @throws DetailedException
     *             if there was an exception thrown by the course searching
     *             logic
     */
    private void refreshCourseList(int userId, boolean validateLogic) throws DetailedException {

        CourseOptionsWrapper courseOptionsWrapper = simpleModeCourseOptions.getCourseOptionsWrapper(userId);

        // Reset since last refresh
        courseOptionsWrapper.courseFileNameToFileMap.clear();
        courseOptionsWrapper.domainOptions.clear();
        courseOptionsWrapper.parseFailedFiles.clear();
        courseOptionsWrapper.upconvertedFiles.clear();

        if (DomainModuleProperties.getInstance().isServerDeploymentMode()) {
            throw new DetailedException(
                    "Failed to refresh the course list because GIFT is currently in " + DeploymentModeEnum.SERVER
                            + " deployment mode.",
                    "Please restart GIFT in " + DeploymentModeEnum.DESKTOP + " or " + DeploymentModeEnum.SIMPLE
                            + " deployment mode to use the logic that searches the local GIFT workspace folder for courses",
                    null);
        }

        try {
            DesktopFolderProxy workspaceDirectoryProxy = new DesktopFolderProxy(WORKSPACE_DIRECTORY);
            DomainCourseFileHandler.getAllCourses(courseOptionsWrapper, workspaceDirectoryProxy, workspaceDirectoryProxy, null, validateLogic, true,
                    null, null);

            if (!courseOptionsWrapper.parseFailedFiles.isEmpty()) {

                // If there was an issue validating a course file, show a dialog
                // with the details.
                // Note: showing a dialog on the main thread isn't the best
                // solution for remote domain module launching or headless
                // machines
                StringBuilder sb = new StringBuilder();
                for (String invalidFile : courseOptionsWrapper.parseFailedFiles) {
                    // gather issues to report all of them at once
                    sb.append("<br>").append(invalidFile);
                }

                final String invalidCourses = sb.toString();

                if (getModuleMode() == ModuleModeEnum.POWER_USER_MODE) {
                    System.out.println(
                            "There was an issue validating one or more courses.  Please read the message dialog created for more information.");

                    // Note: had to thread this and make it top most - had some
                    // course throw URL validation when parsing and this dialog
                    // appeared
                    // preventing the course list from showing on the TUI and
                    // this dialog was behind the browser unknowingly

                    new Thread("InvalidCoursesDialog") {

                        @Override
                        public void run() {

                            JFrame frame = new JFrame();
                            frame.setAlwaysOnTop(true);
                            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                            frame.setVisible(true);
                            frame.setVisible(false);
                            JOptionPane.showMessageDialog(frame,
                                    "<html>There was an issue validating the course(s) listed below, therefore they will not be available in the list of selectable courses.<br>"
                                            + invalidCourses + "<br><br>If you would like to use these courses:<br>"
                                            + "  1) please fix the issue(s) (refer to the Domain log for details)<br>"
                                            + "  2) then restart the domain module<br>"
                                            + "<br>Click the OK button to continue anyway.</html>",
                                    "Course Validation Warning", JOptionPane.WARNING_MESSAGE);
                        }

                    }.start();

                }

                // log the message
                logger.error(
                        "<html>There was an issue validating the course(s) listed below, therefore they will not be available in the list of selectable courses.<br>"
                                + invalidCourses + "<br><br>If you would like to use these courses:<br>"
                                + "  1) please fix the issue(s)<br>" + "  2) then restart the domain module<br>"
                                + "</html>");
            }

            if (courseOptionsWrapper.domainOptions.isEmpty()) {
                // found no course files, warn user
                logger.warn("No course files were found in '"
                        + new File(DomainModuleProperties.getInstance().getDomainDirectory())
                        + "', therefore this domain module will only provide limited functionality.\n"
                        + "You can still continue, however the domain module will only provide limited functionality."
                        + "\n\nHere are some suggestions to resolve the problem:\n"
                        + "  1) Check the latest domain module log for issues in GIFT\\output\\logger\\module\\\n"
                        + "  2) Place a course file in the domain directory (as specified by the domain module's domain directory property)\n"
                        + "  3) If you are using backslashes ('\\') in your domain directory path, please use double backslashes instead ('\\\\')");

                // For now removing this dialog and replacing with logic to show
                // a message in the course selection page of the TUI.
                // JOptionPane.showMessageDialog(null,
                // "<html>No course files were found with a recursive search in
                // '"+directory+"'.<br>You can still continue, however the
                // domain module will only provide limited functionality.<br>" +
                // "<br><br>Here are some suggestions to resolve the
                // problem:<br>" +
                // " 1) Check the latest domain module log for issues in
                // GIFT\\output\\logger\\module\\<br>" +
                // " 2) Place a course file in the domain directory (as
                // specified by the domain module's domain directory
                // property)<br>" +
                // " 3) If you are using backslashes ('\\') in your domain
                // directory path, please use double backslashes instead
                // ('\\\\')<br>" +
                // "<br>Click the OK button to continue starting the Domain
                // module.</html>",
                // "Courses Not Found",
                // JOptionPane.WARNING_MESSAGE);
            }

        } catch (Exception e) {
            throw new DetailedException(
                    "Failed to refresh the course list because an exception was thrown while searching '"
                            + WORKSPACE_DIRECTORY + "'.",
                    "The error reads: " + e.getMessage(), e);
        }
    }

    /**
     * Used to select a specific gateway module instance known to this module
     * already using that gateway module's status information.
     *
     * @param domainSession
     *            information about the domain session that needs the gateway
     *            module
     * @param gwModuleStatus
     *            information about a particular Gateway module to select
     * @param callback
     *            used to notify the caller of responses to the allocation
     *            request
     * @param destroyOnCleanup
     *            - whether the gateway destination connected to should also be
     *            destroyed on the message bus when this module disconnects from
     *            it. Be careful as using this feature could have consequences
     *            on other parts of GIFT that might still need that message bus.
     */
    protected void selectGatewayModule(DomainSession domainSession, ModuleStatus gwModuleStatus,
            MessageCollectionCallback callback, boolean destroyOnCleanup) {

        // Note: filter based on the GW module status information
        selectModule(domainSession, callback, gwModuleStatus, destroyOnCleanup);
    }

    /**
     * Remove the remote gateway module's destination(s) from the message bus.
     * This will cleanup the gateway modules internally facing queue as well as
     * the topic it posts training application states in since the possibly
     * remote JWS gateway module might not have access to the message bus JMX.
     *
     * Note: this should only be called when cleaning up a domain session that
     * uses a java web start gateway module as removing a message bus
     * destination can have consequences on the rest of the GIFT system.
     *
     * @param gwModuleStatus
     *            the gateway module status that contains the name of the jws
     *            gateway module destinations to destroy on the message bus.
     *            (e.g. Gateway_Queue_3b0812da-02b4-4ea2-a597-d78ac6d259ac_Inbox
     *            destination means
     *            Gateway_Queue_3b0812da-02b4-4ea2-a597-d78ac6d259ac needs to be
     *            removed in this logic)
     */
    public void removeRemoteGatewayDestinations(GatewayModuleStatus gwModuleStatus) {

        // strip the Inbox suffix
        String destinationName = gwModuleStatus.getQueueName()
                .replace(AbstractModule.ADDRESS_TOKEN_DELIM + SubjectUtil.INBOX_QUEUE_SUFFIX, Constants.EMPTY);

        if (logger.isInfoEnabled()){
            logger.info("Removing remote Gateway module destination named '" + destinationName + "'.");
        }
        removeClientConnection(destinationName, true);

        if (logger.isInfoEnabled()){
            logger.info("Removing remote Gateway module destination named '" + gwModuleStatus.getTopicName() + "'.");
        }
        removeClientConnection(gwModuleStatus.getTopicName(), true);
    }

    /**
     * Deletes the tutor topic with the specified id. Destroys the topic from
     * the message bus which will impact all modules using this ActiveMQ topic.
     * @param topicId The unique identifier of the tutor topic to be deleted
     */
    public void removeTutorTopicDestinations(String topicId) {
        //The removeClientConnection must be called from the domain
        //module instead of the tutor module due to a conflict with
        //Jetty and ActiveMQ. Similar situation documented here
        //https://activemq.apache.org/jndi-support.html
        String topicName = SubjectUtil.TUTOR_TOPIC_PREFIX + ADDRESS_TOKEN_DELIM + topicId;
        removeClientConnection(topicName, true);
    }
    
    /**
     * Deletes the domain topic with the specified topic name. Destroys the topic from
     * the message bus which will impact all modules using this ActiveMQ topic.
     * @param topicName The unique name of the Domain topic to be deleted
     */
    public void removeDomainTopicDestinations(String topicName) {
        
        domainStatus.getLogPlaybackTopics().remove(topicName);
        
        removeClientConnection(topicName, true);
    }

    /**
     * Used to select any learner module instance known to this module already.
     *
     * @param domainSession
     *            information about the domain session that needs the learner
     *            module
     * @param callback
     *            used to notify the caller of responses to the allocation
     *            request
     */
    protected void selectLearnerModule(DomainSession domainSession, MessageCollectionCallback callback) {

        selectModule(domainSession, ModuleTypeEnum.LEARNER_MODULE, callback);
    }
    
    /**
     * Used to select any sensor module instance known to this module already.
     * 
     * @param domainSession
     *            information about the domain session that needs the sensor
     *            module
     * @param callback
     *            used to notify the caller of responses to the allocation
     *            request
     */
    protected void selectSensorModule(DomainSession domainSession, MessageCollectionCallback callback){
        
        // #4917 - domain and sensor modules need to be on the same machine as the UMS module due to needing to write to the
        //         the same session output folder.  At this point the tutor has selected a domain module that is on the same
        //         computer as the UMS.  Therefore matching the domain with sensor will also mean sensor and ums are matched.
        ConnectionFilter filter = NetworkSession.createConnectionFilter(ipaddr);
        
        if(logger.isDebugEnabled()){
            logger.debug("Adding connection filter of IP address "+ipaddr+" for the domain module selection of the sensor module in "+domainSession);
        }
        selectModule(domainSession, ModuleTypeEnum.SENSOR_MODULE, callback, filter);
    }

    /**
     * Used to run the domain module.
     *
     * @param args
     *            - launch module arguments
     */
    public static void main(String[] args) {
        ModuleModeEnum mode = checkModuleMode(args);
        DomainModuleProperties.getInstance().setCommandLineArgs(args);

        DomainModule dModule = null;

        try {
            dModule = DomainModule.getInstance();
            dModule.setModuleMode(mode);
            dModule.showModuleStartedPrompt();
            dModule.cleanup();
        } catch (Exception e) {
            System.err.println("The Domain Module threw an exception.");
            e.printStackTrace();

            if (dModule != null) {
                dModule.cleanup();
            }

            JOptionPane.showMessageDialog(null,
                    "The Domain Module had a severe error.  Check the log file and the console window for more information.",
                    "Domain Module Error", JOptionPane.ERROR_MESSAGE);

            showModuleUnexpectedExitPrompt(mode);
        }

        if (mode.equals(ModuleModeEnum.POWER_USER_MODE)) {
            System.out.println("Good-bye");
            // kill any threads
            System.exit(0);
        }
    }

    /**
     * This class is responsible for creating all the client connections that
     * this module needs for a domain session.
     *
     * @author mhoffman
     *
     */
    private class DomainSessionModuleAllocation extends Thread {

        /**
         * the messages that started and will ultimately end this sequence of
         * creating client connection
         */
        final UserSessionMessage domainSelectionReply;
        final UserSessionMessage domainSelectionRequest;

        /** the user session info for the domain session */
        final UserSession userSession;

        /** information about the tutor client */
        final WebClientInformation clientInfo;

        /**
         * Class constructor - provide the init domain session related messages
         * that were sent by the Tutor module and the UMS module.
         *
         * @param domainSelectionReply
         *            - the message from the UMS containing the new domain
         *            session id
         * @param domainSelectionRequest
         *            - the message from the tutor containing the domain
         *            selection request
         */
        public DomainSessionModuleAllocation(final UserSessionMessage domainSelectionReply,
                final UserSessionMessage domainSelectionRequest) {
            super("DomainSessionModuleAllocation-" + domainSelectionReply.getUserId());

            this.domainSelectionReply = domainSelectionReply;
            this.domainSelectionRequest = domainSelectionRequest;

            this.userSession = domainSelectionRequest.getUserSession();

            DomainSelectionRequest request = (DomainSelectionRequest) domainSelectionRequest.getPayload();
            this.clientInfo = request.getClientInformation();
        }

        /**
         * All necessary client connections have been made, continue the init
         * domain session sequence.
         */
        private void completed() {
            if (logger.isInfoEnabled()){
                logger.info("Completed domain module allocation sequence for " + userSession + ".");
            }
            forwardDomainSelectionReply(domainSelectionReply, domainSelectionRequest, clientInfo);
        }

        /**
         * A client connection has been successfully established, continue the
         * sequence to the next needed client.
         */
        private void receivedResponse() {
            synchronized (this) {
                this.notifyAll();
            }
        }

        /**
         * There was an issue in creating the clients needed, notify the tutor
         */
        private void failed() {
            sendReply(domainSelectionRequest,
                    new NACK(ErrorEnum.OPERATION_FAILED,
                            "Failed to fully establish connection to all necessary modules"),
                    MessageTypeEnum.PROCESSED_NACK);
        }

        @Override
        public void run() {

            if(logger.isInfoEnabled()){
                logger.info("Starting domain module's module allocation request sequence for " + userSession + ".");
            }

            try {
                //
                // get a LMS module client connection
                //
                MessageCollectionCallback lmsCallback = new MessageCollectionCallback() {

                    @Override
                    public void success() {

                        if(logger.isInfoEnabled()){
                            logger.info("LMS module is now allocated to " + userSession + ".");
                        }

                        receivedResponse();
                    }

                    @Override
                    public void received(final Message aMsg) {

                    }

                    @Override
                    public void failure(Message nackMsg) {

                        // failed to receive a positive response from the module
                        sendReply(domainSelectionRequest,
                                new NACK(ErrorEnum.OPERATION_FAILED,
                                        "Failed to allocate a connection to LMS module because received " + nackMsg),
                                MessageTypeEnum.PROCESSED_NACK);
                    }

                    @Override
                    public void failure(String why) {

                        // failed to receive a positive response from the module
                        sendReply(domainSelectionRequest,
                                new NACK(ErrorEnum.OPERATION_FAILED,
                                        "Failed to allocate a connection to LMS module because " + why),
                                MessageTypeEnum.PROCESSED_NACK);
                    }
                };

                selectModule(userSession, ModuleTypeEnum.LMS_MODULE, lmsCallback);

                try {
                    synchronized (this) {
                        this.wait();
                    }
                } catch (InterruptedException e) {
                    logger.error("Caught exception while waiting for LMS module allocation response", e);
                    failed();
                }

                //
                // get a Ped module client connection
                //
                MessageCollectionCallback pedCallback = new MessageCollectionCallback() {

                    @Override
                    public void success() {

                        if(logger.isInfoEnabled()){
                            logger.info("Pedagogical module is now allocated to " + userSession + ".");
                        }

                        receivedResponse();
                    }

                    @Override
                    public void received(final Message aMsg) {

                    }

                    @Override
                    public void failure(Message nackMsg) {

                        // failed to receive a positive response from the ped
                        // module
                        sendReply(domainSelectionRequest, new NACK(ErrorEnum.OPERATION_FAILED,
                                "Failed to allocate a connection to Pedagogical module because received " + nackMsg),
                                MessageTypeEnum.PROCESSED_NACK);
                    }

                    @Override
                    public void failure(String why) {

                        // failed to receive a positive response from the ped
                        // module
                        sendReply(domainSelectionRequest,
                                new NACK(ErrorEnum.OPERATION_FAILED,
                                        "Failed to allocate a connection to Pedagogical module because " + why),
                                MessageTypeEnum.PROCESSED_NACK);
                    }
                };

                selectModule(userSession, ModuleTypeEnum.PEDAGOGICAL_MODULE, pedCallback);

                try {
                    synchronized (this) {
                        this.wait();
                    }
                } catch (InterruptedException e) {
                    logger.error("Caught exception while waiting for Ped module allocation response", e);
                    failed();
                }

                completed();

            } catch (Exception e) {
                logger.error("Caught exception while allocating the other modules needed by the Domain module", e);
                failed();
            }
        }
    }

    /**
     * This inner class is used to manage the course options of users running
     * the domain module in Simple mode. In this mode this class parses,
     * validates and manages the course options for a user. In other deployment
     * modes this is managed by the GIFT dashboard which is the UI for those
     * modes.
     *
     * @author mhoffman
     *
     */
    private static class SimpleModeCourseOptions {

        /**
         * Mapping of unique user id to the course options information for that
         * user
         */
        private Map<Integer, CourseOptionsWrapper> userToCourseOptions = new HashMap<>();

        public SimpleModeCourseOptions() {

        }

        /**
         * Return the course options information for the specified user. If it
         * doesn't exist the object will be created.
         *
         * @param userId
         *            the user identifier to retrieve course options information
         *            for.
         * @return the course options information for that user
         */
        public CourseOptionsWrapper getCourseOptionsWrapper(int userId) {

            CourseOptionsWrapper userOptions = userToCourseOptions.get(userId);
            if (userOptions == null) {
                userOptions = new CourseOptionsWrapper();
                userToCourseOptions.put(userId, userOptions);
            }

            return userOptions;
        }

        /**
         * Return whether the user specified has course options populated in
         * this class.
         *
         * @param userId
         * @return
         */
        public boolean containsUser(int userId) {
            return userToCourseOptions.containsKey(userId);
        }

    }

    /**
     * This class handles the logic required to start a new subject in an
     * experiment. It will: 1) retrieve the experiment entry from the UMS 2)
     * validate the experiment's course 3) create a new subject in the
     * experiment 4) start the course
     *
     * If there is a failure along the way a negative response message will be
     * sent as a reply to the request message provided.
     *
     * If the logic succeeds, the response to the request message will be the
     * same as the response to the domain selection request message.
     *
     * @author mhoffman
     *
     */
    private class ExperimentCourseRequestHandler extends Thread {

        /**
         * the request message from the tutor to start a subject in the
         * experiment
         */
        private Message experimentCourseRequestMessage;

        /**
         * contains the requested experiment id from the request message
         */
        private ExperimentCourseRequest experimentCourseRequest;

        /**
         * contains the experiment information from the UMS, is assigned after
         * get experiment logic succeeds
         */
        private DataCollectionItem experiment;

        /**
         * contains the course information for the experiment's course, is
         * assigned after validate course logic succeeds
         */
        private DomainOption domainOption;

        /**
         * contains the user id from the UMS, is assigned after create subject
         * logic succeeds
         */
        private UserData userData;

        /**
         * used to indicate if the tutor module was notified that the subject
         * user info was created
         */
        private boolean notifiedTutorOfUserSession;

        /**
         * Set attribute(s)
         *
         * @param experimentCourseRequestMessage
         *            the request message to start a subject in a specific
         *            experiment. This is the message that will be replied too,
         *            either positively (response to domain selection request
         *            message) or negatively (NACK) depending on whether the
         *            course is started or not, respectively.
         * @param experimentCourseRequest
         *            contains the experiment information to use
         */
        public ExperimentCourseRequestHandler(final Message experimentCourseRequestMessage,
                final ExperimentCourseRequest experimentCourseRequest) {
            super("ExperimentCourseRequestHandler-" + experimentCourseRequest.getExperimentId() + ":"
                    + experimentCourseRequest.getClientInformation().getClientAddress());

            if (experimentCourseRequestMessage == null) {
                throw new IllegalArgumentException("The message can't be null.");
            }

            this.experimentCourseRequestMessage = experimentCourseRequestMessage;

            this.experimentCourseRequest = experimentCourseRequest;
        }

        @Override
        public void run() {

            if (logger.isInfoEnabled()){
                logger.info("Starting experiment course request handler for experiment "
                    + experimentCourseRequest.getExperimentId() + " and subject at "
                    + experimentCourseRequest.getClientInformation().getClientAddress());
            }

            //
            // Step 1: Retrieve experiment
            //
            getExperiment();

            if (experiment != null) {

                if (logger.isInfoEnabled()){
                    logger.info("The experiment entry from the database was successfully retrieved for subject at "
                        + experimentCourseRequest.getClientInformation().getClientAddress() + " :\n" + experiment);
                }

                if (experiment.getStatus() != ExperimentStatus.RUNNING) {
                    // ERROR
                    logger.warn("Unable to start the course because the experiment is " + experiment.getStatus()
                            + " [Experiment ID = " + experimentCourseRequest.getExperimentId() + "].");
                    NACK nack = new NACK(ErrorEnum.OPERATION_FAILED,
                            "Unable to start the course because the experiment is " + experiment.getStatus() + ".");
                    nack.setErrorHelp("Please contact the Experiment Administrator for more details.");
                    sendReply(experimentCourseRequestMessage, nack, MessageTypeEnum.NACK);

                    return;
                }

                //
                // Step 2: Validate Experiment course
                //
                validateCourse();

                if (domainOption != null) {

                    if (logger.isInfoEnabled()){
                        logger.info("The experiment's course is valid for subject at "
                            + experimentCourseRequest.getClientInformation().getClientAddress() + "\n" + experiment);
                    }

                    //
                    // Step 3: Create experiment subject in UMS
                    //

                    createSubject();

                    if (userData != null) {

                        if (logger.isInfoEnabled()){
                            logger.info("An new subject entry was created in the database for subject at "
                                + experimentCourseRequest.getClientInformation().getClientAddress() + " in experiment "
                                + experiment.getId());
                        }

                        //
                        // Step 4: link the tutor that is communicating with
                        // this domain to the subject's identifier which
                        // will be used to map other modules this domain module
                        // will be allocating in the next step.
                        //
                        if (linkTutorToSubject()) {

                            //
                            // Step 5: notify tutor module that subject was
                            // created so it can create the user web session
                            //
                            notifyUserSessionCreated();

                            if (notifiedTutorOfUserSession) {
                                //
                                // Step 6: Start course and reply to original
                                // request
                                // (leverages pre-existing code)
                                //
                                startDomain();
                            }
                        }
                    }
                }
            }
        }

        /**
         * Notify the tutor module that the subject has been created so it can
         * create a user web session that will be ready to present the course
         * init instructions page ahead of the first course transition (if
         * needed, i.e. in server mode)
         */
        private void notifyUserSessionCreated() {

            Object waitFor = new Object();

            MessageCollectionCallback subjectCreatedCallback = new MessageCollectionCallback() {

                @Override
                public void success() {
                    notifiedTutorOfUserSession = true;
                    releaseWait();
                }

                @Override
                public void received(Message msg) {
                    // don't care
                }

                @Override
                public void failure(String why) {
                    logger.error("Failed to notify the tutor that the subject was created for the experiment "
                            + experimentCourseRequest.getExperimentId() + " because '" + why + "'.");
                    sendReply(experimentCourseRequestMessage,
                            new NACK(ErrorEnum.OPERATION_FAILED,
                                    "Failed to notify the tutor that the subject was created for the experiment "
                                            + experimentCourseRequest.getExperimentId() + " because '" + why + "'."),
                            MessageTypeEnum.NACK);

                    releaseWait();
                }

                @Override
                public void failure(Message msg) {
                    logger.error("Failed to notify the tutor that the subject was created for the experiment "
                            + experimentCourseRequest.getExperimentId() + " because '" + msg.getPayload() + "'.");
                    sendReply(experimentCourseRequestMessage,
                            new NACK(ErrorEnum.OPERATION_FAILED,
                                    "Failed to notify the tutor that the subject was created for the experiment "
                                            + experimentCourseRequest.getExperimentId() + " because '"
                                            + msg.getPayload() + "'."),
                            MessageTypeEnum.NACK);

                    releaseWait();
                }

                private void releaseWait() {

                    synchronized (waitFor) {
                        waitFor.notifyAll();
                    }
                }
            };

            // notify the tutor that the domain/user session has been created so
            // it can create the user web session
            UserSession userSession = new UserSession(userData.getUserId());
            userSession.setExperimentId(userData.getExperimentId());
            userSession.setSessionType(UserSessionType.EXPERIMENT_USER);
            SubjectCreated subjectCreated = new SubjectCreated(domainOption.getDomainId(),
                    experimentCourseRequest.getPreSessionId());
            DomainModule.getInstance().sendExperimentSubjectCreatedMessage(subjectCreated, userSession,
                    subjectCreatedCallback);

            synchronized (waitFor) {
                try {
                    waitFor.wait();
                } catch (@SuppressWarnings("unused") InterruptedException e) {
                    sendReply(experimentCourseRequestMessage,
                            new NACK(ErrorEnum.OPERATION_FAILED,
                                    "Failed to wait for a subject to be created for for " + experiment + "."),
                            MessageTypeEnum.NACK);
                }
            }
        }

        /**
         * Make sure the tutor module that initiated this experiment course
         * execution for a subject is linked with the user session identifying
         * information to allow the domain module to send messages to that
         * user's tutor instance in the future.
         *
         * @return true if the domain's network session logic has mapped the
         *         tutor to the newly created subjet user info
         */
        private boolean linkTutorToSubject() {

            UserSession userSession = new UserSession(userData.getUserId());
            userSession.setExperimentId(userData.getExperimentId());
            userSession.setSessionType(UserSessionType.EXPERIMENT_USER);

            try {
                ConnectionFilter addressFilter = new ConnectionFilter();
                addressFilter.addRequiredAddress(experimentCourseRequestMessage.getSenderAddress());

                linkModuleClient(userSession, ModuleTypeEnum.TUTOR_MODULE, addressFilter);

            } catch (Exception e) {
                logger.error("Failed to link the tutor module to the new subject " + userData + " for the experiment "
                        + experimentCourseRequest.getExperimentId() + ".", e);
                sendReply(experimentCourseRequestMessage,
                        new NACK(ErrorEnum.OPERATION_FAILED,
                                "Failed to link the tutor module to the new subject " + userData
                                        + " for the experiment " + experimentCourseRequest.getExperimentId() + "."),
                        MessageTypeEnum.NACK);
                return false;
            }

            return true;

        }

        /**
         * Starts the domain session. This will not hold onto the calling
         * thread.
         */
        private void startDomain() {

            DomainSelectionRequest domainSelectionRequest = new DomainSelectionRequest(
                    experimentCourseRequest.getExperimentId(), domainOption.getDomainId(), domainOption.getSourceId(),
                    experimentCourseRequest.getClientInformation(), null);
            UserSession userSession = new UserSession(userData.getUserId());
            userSession.setExperimentId(experimentCourseRequest.getExperimentId());
            userSession.setSessionType(UserSessionType.EXPERIMENT_USER);
            
            /* Pushing the injected UserData username into the user session so
             * that the experiment will have Nuxeo permissions. See #4550 for
             * more details. Hopefully this does not cause problems later on. */
            userSession.setUsername(userData.getUsername());

            Message domainSelectionRequestMessage = new UserSessionMessage(MessageTypeEnum.DOMAIN_SELECTION_REQUEST,
                    experimentCourseRequestMessage.getSequenceNumber(),
                    experimentCourseRequestMessage.getSourceEventId(),
                    experimentCourseRequestMessage.getTimeStamp(),
                    experimentCourseRequestMessage.getSenderModuleName(),
                    experimentCourseRequestMessage.getSenderAddress(),
                    experimentCourseRequestMessage.getSenderModuleType(),
                    experimentCourseRequestMessage.getDestinationQueueName(), domainSelectionRequest, userSession,
                    true);

            if (logger.isInfoEnabled()){
                logger.info(
                    "Copying message header from an experiment course request to a domain selection request in order to begin an experiment domain session.\n"
                            + "Experiment course request message: " + experimentCourseRequestMessage.toString() + "\n"
                            + "Domain selection request message: " + domainSelectionRequestMessage);
            }

            handleDomainSelectionRequest(domainSelectionRequestMessage);
        }

        /**
         * Creates the experiment subject. This will hold onto the calling
         * thread until completed. If the subject is not created successfully,
         * this method will replay with a NACK to the original message provided
         * to this class.
         */
        private void createSubject() {

            Object waitFor = new Object();

            MessageCollectionCallback createSubjectCallback = new MessageCollectionCallback() {

                @Override
                public void success() {

                    if (userData == null) {
                        // ERROR
                        sendReply(experimentCourseRequestMessage,
                                new NACK(ErrorEnum.OPERATION_FAILED,
                                        "Failed to create a new subject for experiment "
                                                + experimentCourseRequest.getExperimentId() + "."),
                                MessageTypeEnum.NACK);
                    }

                    releaseWait();
                }

                @Override
                public void received(Message msg) {
                    userData = (UserData) msg.getPayload();
                }

                @Override
                public void failure(String why) {
                    logger.error("Failed to create a new subject for the experiment "
                            + experimentCourseRequest.getExperimentId() + " because '" + why + "'.");
                    sendReply(experimentCourseRequestMessage,
                            new NACK(ErrorEnum.OPERATION_FAILED,
                                    "Failed to create a new subject for the experiment "
                                            + experimentCourseRequest.getExperimentId() + " because '" + why + "'."),
                            MessageTypeEnum.NACK);

                    releaseWait();
                }

                @Override
                public void failure(Message msg) {
                    logger.error("Failed to create a new subject for the experiment "
                            + experimentCourseRequest.getExperimentId() + " because received the failure message of '"
                            + msg.getPayload() + "'.");
                    sendReply(experimentCourseRequestMessage,
                            new NACK(ErrorEnum.OPERATION_FAILED,
                                    "Failed to create a new subject for the experiment "
                                            + experimentCourseRequest.getExperimentId()
                                            + " because of the failure message '" + msg.getPayload() + "'."),
                            MessageTypeEnum.NACK);

                    releaseWait();
                }

                private void releaseWait() {

                    synchronized (waitFor) {
                        waitFor.notifyAll();
                    }
                }
            };

            sendCreateUserMessage(true, null, experimentCourseRequest.getExperimentId(), createSubjectCallback);

            synchronized (waitFor) {
                try {
                    waitFor.wait();
                } catch (@SuppressWarnings("unused") InterruptedException e) {
                    sendReply(experimentCourseRequestMessage,
                            new NACK(ErrorEnum.OPERATION_FAILED,
                                    "Failed to wait for a subject to be created for for " + experiment + "."),
                            MessageTypeEnum.NACK);
                    return;
                }
            }
        }

        /**
         * Validates the experiment's course. This will hold onto the calling
         * thread until completed. If the course is not valid, this method will
         * replay with a NACK to the original message provided to this class.
         */
        private void validateCourse() {

            Object waitFor = new Object();
            try {

                final long preValidationEpoch = System.currentTimeMillis();
                long buildSurveyValidationTotalTime = 0;

                // where to put the various survey validation requests for each
                // course, mapped by course name
                Map<String, List<SurveyCheckRequest>> surveyValidationRequests = new HashMap<>();

                final boolean isLegacy = experiment.isLegacyExperiment();
                File parentDirectory = isLegacy ? legacyExperimentDirectory.getFolder() : experimentDirectory;
                String experimentPath = isLegacy ? experiment.getCourseFolder()
                        : experimentCourseRequest.getExperimentFolder();
                DesktopFolderProxy courseFolder;
                try {
                    courseFolder = new DesktopFolderProxy(new File(parentDirectory, experimentPath));
                } catch (Exception fileServicesException) {
                    logger.error("Unable to find the " + (isLegacy ? "legacy " : "") + "experiment course folder for '"
                            + experimentPath + "' for experiment '" + experiment.getId() + "'.\nParent directory: '"
                            + parentDirectory + "',\n" + experimentCourseRequest + ",\n" + experiment,
                            fileServicesException);
                    sendReply(experimentCourseRequestMessage, new NACK(ErrorEnum.OPERATION_FAILED,
                            "Failed to find the " + (isLegacy ? "legacy " : "") + "experiment course folder of '"
                                    + experimentPath + "' for experiment " + experiment.getId() + "."),
                            MessageTypeEnum.NACK);
                    return;
                }

                // find the course.xml file in the experiment course folder
                List<FileProxy> courseFiles = new ArrayList<>();
                FileFinderUtil.getFilesByExtension(courseFolder, courseFiles,
                        AbstractSchemaHandler.COURSE_FILE_EXTENSION);

                if (courseFiles.isEmpty()) {
                    // ERROR
                    sendReply(experimentCourseRequestMessage,
                            new NACK(ErrorEnum.OPERATION_FAILED,
                                    "Failed to find the course.xml file for " + experiment.getId() + "."),
                            MessageTypeEnum.NACK);
                    return;
                } else if (courseFiles.size() > 1) {
                    // ERROR
                    sendReply(experimentCourseRequestMessage,
                            new NACK(ErrorEnum.OPERATION_FAILED,
                                    "Found " + courseFiles.size() + " course.xml files for " + experiment.getId()
                                            + ".  Not sure which one to execute."),
                            MessageTypeEnum.NACK);
                    return;
                }

                    //
                    // Check for Internet connection for course validation
                    // purposes
                    //
                    final InternetConnectionStatusEnum connectionStatus = UriUtil.getInternetStatus();

                    // used to handle a response to the survey validation
                    // request message
                    // ...in this case, the results of the validation will be
                    // used to update/filter the course list
                    final MessageCollectionCallback surveyValidationCallback = new MessageCollectionCallback() {

                        SurveyCheckResponse surveyCheckResponse;

                        @Override
                        public void success() {

                            if (surveyCheckResponse != null) {
                                // check response and determine if the
                                // experiment course is still valid

                                for (String courseId : surveyCheckResponse.getResponses().keySet()) {

                                    List<ResponseInterface> responses = surveyCheckResponse.getResponses()
                                            .get(courseId);
                                    for (ResponseInterface response : responses) {

                                        if (response instanceof FailureResponse) {
                                            logger.error("The course with id '" + courseId + "' for experiment "
                                                    + experiment.getId() + " can't be executed because "
                                                    + "because one or more of the survey references failed to pass the validation check.  The reason reported by the survey database is: "
                                                    + ((FailureResponse) response).getMessage());
                                            sendReply(experimentCourseRequestMessage,
                                                    new NACK(ErrorEnum.OPERATION_FAILED,
                                                            "Failed to validate course survey refrences in the experiment "
                                                                    + experiment.getId() + " because "
                                                                    + ((FailureResponse) response).getMessage() + "."),
                                                    MessageTypeEnum.NACK);
                                            releaseWait(false);
                                        }
                                    }
                                }

                            } else {
                                // error, send NACK
                                sendReply(experimentCourseRequestMessage,
                                        new NACK(ErrorEnum.OPERATION_FAILED,
                                                "Failed to validate survey refrences in the experiment course because a Survey Check Response was not received."),
                                        MessageTypeEnum.NACK);
                                releaseWait(false);
                            }

                            releaseWait(true);

                        }

                        @Override
                        public void received(Message msg) {

                            if (msg.getPayload() instanceof SurveyCheckResponse) {
                                surveyCheckResponse = (SurveyCheckResponse) msg.getPayload();
                            }

                        }

                        @Override
                        public void failure(String why) {
                            // error, send NACK

                            logger.error("There was a problem with the survey validation request for "
                                    + experimentCourseRequestMessage + " because '" + why + "'.");
                            sendReply(experimentCourseRequestMessage,
                                    new NACK(ErrorEnum.OPERATION_FAILED,
                                            "Failed to validate survey refrences in the possible domain options because '"
                                                    + why + "'."),
                                    MessageTypeEnum.NACK);

                            releaseWait(false);
                        }

                        @Override
                        public void failure(Message msg) {
                            // error, send NACK

                            logger.error("There was a problem with the survey validation request for "
                                    + experimentCourseRequestMessage + " because the message " + msg
                                    + " was received.");
                            sendReply(experimentCourseRequestMessage,
                                    new NACK(ErrorEnum.OPERATION_FAILED,
                                            "Failed to validate survey refrences in the possible domain options because '"
                                                    + ((NACK) msg.getPayload()).getErrorMessage()
                                                    + "' in response to the Survey Check Request."),
                                    MessageTypeEnum.NACK);

                            releaseWait(false);
                        }

                        /**
                         * Release the calling thread that is waiting for a
                         * response to the survey validation message.
                         *
                         * @param success
                         *            whether the domain option is valid
                         */
                        private void releaseWait(boolean success) {

                            // clear out the domain option since it isn't valid
                            if (!success) {
                                domainOption = null;
                            }

                            synchronized (waitFor) {
                                waitFor.notifyAll();
                            }
                        }
                    };

                    FileProxy courseFile = courseFiles.get(0);
                    try {
                        /* convert all files for the experiment if they are not the latest schema
                         * version */
                        AbstractConversionWizardUtil.updateCourseToLatestVersion(courseFile, courseFolder, true);

                        DomainCourseFileHandler handler = new DomainCourseFileHandler(courseFile, courseFolder, true);

                        DesktopFolderProxy folderProxy = new DesktopFolderProxy(parentDirectory);
                        String courseFileName = folderProxy.getRelativeFileName(courseFile);
                        domainOption = new DomainOption(handler.getCourse().getName(), courseFileName, experiment.getCourseFolder() + Constants.FORWARD_SLASH + courseFile.getName(),
                                handler.getCourse().getDescription(), null);

                        handler.setCurrentInternetStatus(connectionStatus);

                        // validate the course against gift logic
                        if (DomainModuleProperties.getInstance().shouldValidateCourseContentAtCourseListRequest()) {

                            CourseValidationResults validationResults = handler.checkCourse(true, null);
                            DetailedExceptionSerializedWrapper criticalIssue = validationResults.getCriticalIssue();
                            if(criticalIssue != null){
                                //there was a critical validation issue

                                logger.error(
                                        "There was a validation issue with the experiment course of " + courseFile.getFileId()
                                                + " for experiment course request of " + experimentCourseRequestMessage + ".",
                                                criticalIssue);
                                domainOption = null;
                                NACK nack = new NACK(ErrorEnum.OPERATION_FAILED,
                                        "Failed to validate the experiment course.  Please contact the Experiment Administrator which can check the course.");
                                nack.setErrorHelp("The error reads - " + criticalIssue);
                                sendReply(experimentCourseRequestMessage, nack, MessageTypeEnum.NACK);
                                return;
                            }
                        }

                        if (DomainModuleProperties.getInstance().shouldValidateSurveyAtCourseListRequest()) {

                            // build survey validation requests
                            long preTime = System.currentTimeMillis();
                            List<SurveyCheckRequest> handlerRequests = handler.buildSurveyValidationRequest();
                            buildSurveyValidationTotalTime += System.currentTimeMillis() - preTime;

                            if (!handlerRequests.isEmpty()) {
                                // if there are no survey checks to be
                                // performed, don't bother sending an object to
                                // be checked.

                                surveyValidationRequests.put(courseFile.getFileId(), handlerRequests);
                            }
                        }

                    } catch (FileValidationException exception) {
                        logger.error(
                                "Caught specific exception with experiment course of " + courseFile.getFileId()
                                        + " for experiment course request of " + experimentCourseRequestMessage + ".",
                                exception);
                        domainOption = null;
                        NACK nack = new NACK(ErrorEnum.OPERATION_FAILED,
                                "Failed to validate the experiment course.  Please contact the Experiment Administrator which can check the course.");
                        nack.setErrorHelp("The error reads - " + exception);
                        sendReply(experimentCourseRequestMessage, nack, MessageTypeEnum.NACK);
                        return;
                    } catch (Exception e) {
                        logger.error("Caught (general) exception with domain option of " + domainOption
                                + " while trying to check it.", e);
                        domainOption = null;
                        NACK nack = new NACK(ErrorEnum.OPERATION_FAILED,
                                "There was a general problem with checking the experiment course.  Please contact the Experiment Administrator which can check the course.");
                        nack.setErrorHelp("The error reads - " + e.getMessage());
                        sendReply(experimentCourseRequestMessage, nack, MessageTypeEnum.NACK);
                        return;
                    }

                    long postValidationEpoch = System.currentTimeMillis();
                    if (logger.isDebugEnabled()){
                        logger.debug("It took " + (postValidationEpoch - preValidationEpoch)
                            + " ms to validate and organize the courses for " + experimentCourseRequestMessage
                            + ".  Of that it took " + buildSurveyValidationTotalTime
                            + " ms to build the survey validation request.");
                    }

                    if (surveyValidationRequests.isEmpty()) {
                        // there are no surveys to validate, return because we
                        // don't want
                        // to wait while we get a message response since we
                        // aren't sending
                        // a survey validation message.

                        return;

                    } else {
                        if (logger.isDebugEnabled()){
                            logger.debug("Sending survey request at epoch of " + System.currentTimeMillis() + ".");
                        }
                        sendMessage(new SurveyListCheckRequest(surveyValidationRequests),
                                MessageTypeEnum.SURVEY_CHECK_REQUEST, surveyValidationCallback);
                    }
            } catch (Throwable t) {
                // catch all
                logger.error("Caught exception while trying to process domain options request.", t);
                sendReply(experimentCourseRequestMessage,
                        new NACK(ErrorEnum.OPERATION_FAILED,
                                "Failed to retrieve course list because an exception was thrown."),
                        MessageTypeEnum.NACK);
                return;
            }

            synchronized (waitFor) {
                try {
                    waitFor.wait();
                } catch (@SuppressWarnings("unused") InterruptedException e) {
                    sendReply(experimentCourseRequestMessage,
                            new NACK(ErrorEnum.OPERATION_FAILED,
                                    "Failed to wait for the experiment course to be validated for " + experiment + "."),
                            MessageTypeEnum.NACK);
                    return;
                }
            }

        }

        /**
         * Retrieve the experiment details. This will hold onto the calling
         * thread until completed. If the experiment is not received
         * successfully, this method will replay with a NACK to the original
         * message provided to this class.
         */
        private void getExperiment() {

            Object waitFor = new Object();

            GetExperimentRequest getExperimentRequest = new GetExperimentRequest(
                    experimentCourseRequest.getExperimentId());
            MessageCollectionCallback getExperimentRequestCallback = new MessageCollectionCallback() {

                @Override
                public void success() {

                    if (experiment == null) {
                        // ERROR
                        sendReply(experimentCourseRequestMessage,
                                new NACK(ErrorEnum.OPERATION_FAILED,
                                        "Failed to retrieve the experiment for experiment "
                                                + experimentCourseRequest.getExperimentId() + "."),
                                MessageTypeEnum.NACK);
                    }

                    releaseWait();
                }

                @Override
                public void received(Message msg) {
                    experiment = (DataCollectionItem) msg.getPayload();
                }

                @Override
                public void failure(String why) {
                    logger.error("Failed to retrieve the experiment from the UMS for "
                            + experimentCourseRequest.getExperimentId() + " because '" + why + "'.");
                    sendReply(experimentCourseRequestMessage,
                            new NACK(ErrorEnum.OPERATION_FAILED,
                                    "Failed to retrieve the experiment information for experiment "
                                            + experimentCourseRequest.getExperimentId() + " because '" + why + "'."),
                            MessageTypeEnum.NACK);

                    releaseWait();
                }

                @Override
                public void failure(Message msg) {
                    logger.error("Failed to retrieve the experiment from the UMS for "
                            + experimentCourseRequest.getExperimentId() + " because received the failure message of '"
                            + msg + "'.");

                    NACK nack;
                    if (msg.getPayload() instanceof NACK) {
                        nack = new NACK(ErrorEnum.OPERATION_FAILED, "Failed to retrieve the experiment because of '"
                                + ((NACK) msg.getPayload()).getErrorMessage() + "'.");
                        nack.setErrorHelp(
                                "Please contact the Experiment Adminstrator to see if the experiment was deleted.");
                    } else {
                        nack = new NACK(ErrorEnum.OPERATION_FAILED,
                                "Failed to retrieve the experiment because of '" + msg.getPayload() + "'.");
                        nack.setErrorHelp("Please contact the Experiment Adminstrator.");
                    }

                    sendReply(experimentCourseRequestMessage, nack, MessageTypeEnum.NACK);

                    releaseWait();
                }

                private void releaseWait() {

                    synchronized (waitFor) {
                        waitFor.notifyAll();
                    }
                }
            };

            sendGetExperimentRequest(getExperimentRequest, getExperimentRequestCallback);

            synchronized (waitFor) {
                try {
                    waitFor.wait();
                } catch (@SuppressWarnings("unused") InterruptedException e) {
                    sendReply(experimentCourseRequestMessage,
                            new NACK(ErrorEnum.OPERATION_FAILED,
                                    "Failed to wait for the experiment database entry to be retrieved for experiment "
                                            + experimentCourseRequest.getExperimentId() + "."),
                            MessageTypeEnum.NACK);
                    return;
                }
            }
        }

    }

    /**
     * Sends the display team session request to the tutor client.
     *
     * @param domainSessionId The domain session id making the request.
     * @param callback The callback to handle the message request.
     */
    public void sendDisplayTeamSession(int domainSessionId, MessageCollectionCallback callback) {
        final BaseDomainSession domainSession = domainSessionIdToDomainSession.get(domainSessionId);
        if (domainSession != null) {

            final DomainSession domainSessionInfo = domainSession.getDomainSessionInfo();
            if (LessonLevelEnum.RTA.equals(DomainModuleProperties.getInstance().getLessonLevel())) {
                sendDomainSessionMessage(ModuleTypeEnum.GATEWAY_MODULE, null, domainSessionInfo, domainSessionId,
                        MessageTypeEnum.DISPLAY_TEAM_SESSIONS, callback);
            } else {
                sendDomainSessionMessage(null, domainSessionInfo, domainSessionId,
                        MessageTypeEnum.DISPLAY_TEAM_SESSIONS, callback);
            }

        } else {
            logger.error("Could not send display team session request: Domain Session ID " + domainSessionId
                    + " does not have a domain session " + "associated with it.");
        }

    }

    /**
     * Creates the domain topic at which a domain session will receive messages from
     * a its associated log playback service.
     *
     * @param topicId The unique identifier for this domain session's tutor topic
     * @return the name of the created topic, or null, if no topic was created.
     */
    public String createLogPlaybackTopic(String logPlaybackTopicId) {
        
        String topicName = SubjectUtil.DOMAIN_TOPIC_PREFIX + ADDRESS_TOKEN_DELIM + logPlaybackTopicId;
        
        if(!haveSubjectClient(topicName)) {
            
            registerLogPlaybackTrainingAppGameStateMessageHandler(topicName, trainingAppMessageHandler);
            
            domainStatus.getLogPlaybackTopics().add(topicName);
            
            sendMessage(SubjectUtil.DOMAIN_DISCOVERY_TOPIC, domainStatus, MessageTypeEnum.DOMAIN_MODULE_STATUS, null);
            
            return topicName;
        }
        
        return null;
    }
    
    /**
     * Sends the given simulation message from the log playback service with the given topic name 
     * that is associated with the given domain session
     * 
     * @param logPlaybackTopicName the name of the log playback service topic to be used to send the message. Cannot be null. 
     * @param msg the message to send
     * @param domainSession the domain session associated with the log playback service that is sending 
     * this message. Cannot be null.
     */
    public void sendLogPlaybackMessage(String logPlaybackTopicName, Message msg, DomainSession domainSession) {
        sendDomainSessionMessage(
                logPlaybackTopicName, 
                msg.getPayload(), 
                domainSession, 
                domainSession.getDomainSessionId(), 
                domainSession.getExperimentId(), 
                msg.getMessageType(), 
                null);
    }
    
    /**
     * Sends information about a knowledge session's assessment details to the UMS module
     * for logging and event reporting purposes
     * 
     * @param bds the base domain session that this information is being sent for
     * @param variables
     */
    public void sendKnowledgeAssessmentDetails(BaseDomainSession bds, KnowledgeAssessmentDetails variables) {
        
        sendDomainSessionMessage(ModuleTypeEnum.UMS_MODULE, variables, bds.getDomainSessionInfo(), 
                bds.getDomainSessionInfo().getDomainSessionId(),
                MessageTypeEnum.KNOWLEDGE_ASSESSMENT_DETAILS,
                null);
    }
    
    /**
     * Checks to see if the given domain session is part of an active knowledge session and, if so,
     * gets the host domain session for that knowledge session. If the provided domain session is
     * not in a knowledge session or the knowledge session does not support teams, then the domain
     * session itself will be returned as the host.
     * 
     * @param possibleJoinerSession the domain session to get the host for. Can be a joiner or 
     * the host itself.
     * @return the host domain session. Cannot be null.
     */
    private BaseDomainSession getHostSession(BaseDomainSession possibleJoinerSession) {
        
        DomainSession domainSessionInfo = possibleJoinerSession.getDomainSessionInfo();
        
        Integer hostDsId = KnowledgeSessionManager.getInstance().isMemberOfTeamKnowledgeSession(
                domainSessionInfo.getDomainSessionId());
        
        if(hostDsId == null) {
            hostDsId = domainSessionInfo.getDomainSessionId();
        }
        
        return domainSessionIdToDomainSession.get(hostDsId);
    }

    /**
     * Executes the given strategy activity for the given domain session
     * 
     * @param activity the strategy activity to execute. Cannot be null.
     * @param joinedSession the domain session to execute the strategy for. Cannot be null.
     * @throws Exception if an error occurs while applying the strategy.
     */
    @SuppressWarnings("unchecked")
    private void executeStrategyActivity(AbstractStrategy activity, final BaseDomainSession joinedSession) throws Exception {
        
        AbstractStrategy toExecute = activity;
                  
        if(toExecute instanceof MidLessonMediaStrategy) {
            
            /* Check if this media strategy needs to request its content from an external strategy provider */
            MidLessonMediaStrategy strategy = (MidLessonMediaStrategy) activity;
            
            boolean needsProviderRequest = false;
            
            for(generated.dkf.Media mediaItem : strategy.getMediaList().getMedia()) {
                
                if(mediaItem.getDisplaySessionProperties() != null
                        && generated.dkf.BooleanEnum.TRUE.equals(
                                mediaItem.getDisplaySessionProperties().getRequestUsingSessionState())) {
                    
                    /* A request to the strategy provider is needed */
                    needsProviderRequest = true;
                    break;
                }
            }
            
            if(needsProviderRequest) {
                
                /* 
                 * If an external strategy provider is being used to provide content
                 * for mid-lesson media, we can't share the same media objects between
                 * the team member threads, since changing one will affect the others.
                 * To address this, we clone the media items for each joiner.
                 * 
                 * This fixes a problem found in #5519 where multiple team members 
                 * would sometimes see each others' feedback.
                 */
                
                String cloneString = AbstractSchemaHandler.getAsXMLString(
                        strategy.getMediaList(), 
                        generated.dkf.LessonMaterialList.class, 
                        AbstractSchemaHandler.DKF_SCHEMA_FILE);
                
                UnmarshalledFile unmarshalled = AbstractSchemaHandler.getFromXMLString(
                        cloneString, 
                        generated.dkf.LessonMaterialList.class, 
                        AbstractSchemaHandler.DKF_SCHEMA_FILE, 
                        false);
                
                generated.dkf.LessonMaterialList cloneMaterial = (generated.dkf.LessonMaterialList) unmarshalled.getUnmarshalled();
                
                generated.dkf.MidLessonMedia media = new generated.dkf.MidLessonMedia();
                media.setLessonMaterialList(cloneMaterial);
                media.setStrategyHandler(strategy.getHandlerInfo());
                
                toExecute = AbstractStrategy.createActivityFrom(strategy.getName(), media);
                toExecute.setDelayAfterStrategy(strategy.getDelayAfterStrategy());
                toExecute.setStress(strategy.getStress());
                
                /* Check if the content of any media needs to be changed */
                for(generated.dkf.Media mediaItem : cloneMaterial.getMedia()) {
                    
                    if(mediaItem.getDisplaySessionProperties() != null
                            && generated.dkf.BooleanEnum.TRUE.equals(
                                    mediaItem.getDisplaySessionProperties().getRequestUsingSessionState())) {
                        
                        BaseDomainSession hostSession = getHostSession(joinedSession);
                        
                        /* Gather information from the knowledge session to share with the strategy provider, and
                         * also let it know what type of content is being shown */
                        JSONObject strategyProviderData = joinedSession.getDataForExternalStrategyProvider(hostSession);
                        strategyProviderData.put("contentType", "webpage");
                        
                        String contentString = getContentFromExternalStrategyProvider(strategyProviderData);
                        
                        if(contentString != null) {
                            /* This media requires a request from an external provider */
                            mediaItem.setUri(contentString);
                        }
                    }
                }
            }
            
        } else if(toExecute instanceof InstructionalInterventionStrategy) {
            
            /* Check if this media strategy needs to request its content from an external strategy provider */
            InstructionalInterventionStrategy strategy = (InstructionalInterventionStrategy) activity;
            
            boolean needsProviderRequest = false;
            
            if(strategy.getFeedback().getFeedbackPresentation() instanceof generated.dkf.Message) {
                
                generated.dkf.Message message = (generated.dkf.Message) strategy.getFeedback().getFeedbackPresentation();
                if(message.getDisplaySessionProperties() != null 
                        && generated.dkf.BooleanEnum.TRUE.equals(
                                message.getDisplaySessionProperties().getRequestUsingSessionState())) {
                    
                    /* A request to the strategy provider is needed */
                    needsProviderRequest = true;
                }
            }
            
            if(needsProviderRequest) {
                
                /*  
                 * Need to clone instructional intervention for the same reason we do it 
                 * for mid lesson media (see above) 
                 */
                
                String cloneString = AbstractSchemaHandler.getAsXMLString(
                        strategy.getFeedback(), 
                        generated.dkf.Feedback.class, 
                        AbstractSchemaHandler.DKF_SCHEMA_FILE);
                
                UnmarshalledFile unmarshalled = AbstractSchemaHandler.getFromXMLString(
                        cloneString, 
                        generated.dkf.Feedback.class, 
                        AbstractSchemaHandler.DKF_SCHEMA_FILE, 
                        false);
                
                generated.dkf.Feedback cloneFeedback = (generated.dkf.Feedback) unmarshalled.getUnmarshalled();
                
                generated.dkf.InstructionalIntervention media = new generated.dkf.InstructionalIntervention();
                media.setFeedback(cloneFeedback);
                media.setStrategyHandler(strategy.getHandlerInfo());
                
                toExecute = AbstractStrategy.createActivityFrom(strategy.getName(), media);
                toExecute.setDelayAfterStrategy(strategy.getDelayAfterStrategy());
                toExecute.setStress(strategy.getStress());
                        
                BaseDomainSession hostSession = getHostSession(joinedSession);
                
                /* Gather information from the knowledge session to share with the strategy provider, and
                 * also let it know what type of content is being shown */
                JSONObject strategyProviderData = joinedSession.getDataForExternalStrategyProvider(hostSession);
                strategyProviderData.put("contentType", "text");
                
                String contentString = getContentFromExternalStrategyProvider(strategyProviderData);
                
                if(contentString != null) {
                    /* This feedback requires a request from an external provider */
                    ((generated.dkf.Message) cloneFeedback.getFeedbackPresentation()).setContent(contentString);
                }
            }
        }
               
        joinedSession.executeActivity(toExecute);
    }

    /**
     * Sends the given training data to the external strategy provider and returns the response, which specifies what
     * content to provide to an activity.
     * 
     * @param strategyProviderData the data to send to the strategy provider. Cannot be null.
     * @return the content that the strategy provider says to show. Can be null.
     * @throws Exception if an error occurs while making the request.
     */
    private String getContentFromExternalStrategyProvider(JSONObject strategyProviderData) throws Exception{
        
        String strategyProviderUrl = DomainModuleProperties.getInstance().getExternalStrategyProviderUrl();
        if(strategyProviderUrl == null) {
            throw new Exception("A strategy attempted to show content from an external strategy provider, but none has been enabled.");
        }
        
        try {
            java.net.URL url = new java.net.URL(strategyProviderUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            
            // The user agent property is needed to prevent some websites from returning a
            // response of 403 instead of 200
            connection.addRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(30);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            
            /* Writer the JSON to the request as a string*/
            String jsonInputString = strategyProviderData.toJSONString();
            try(OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);           
            }
            
            /* Send the request to the server */
            connection.connect();
            int responseCode = connection.getResponseCode(); // this can throw an exception;
            
            if(responseCode == 200) {
                
                /* Parse the response and return it to the caller */
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String responseBody = br.lines().collect(Collectors.joining());
                
                return responseBody;
                
            } else {
                throw new Exception("Strategy provider returned an invalid response: " + responseCode);
            }
        } catch(Exception e) {
            throw new Exception("Failed to request content from the strategy provier at " + strategyProviderUrl, e);
        }
    }
}