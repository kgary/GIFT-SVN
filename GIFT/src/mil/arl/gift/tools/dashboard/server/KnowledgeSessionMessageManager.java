/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server;

import static mil.arl.gift.common.util.StringUtils.isBlank;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import javax.jms.IllegalStateException;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import generated.course.Concepts;
import generated.dkf.Actions;
import generated.dkf.Actions.StateTransitions.StateTransition;
import generated.dkf.Actions.StateTransitions.StateTransition.StrategyChoices;
import generated.dkf.Strategy;
import generated.dkf.StrategyRef;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.EvaluatorUpdateRequest;
import mil.arl.gift.common.InitializePedagogicalModelRequest;
import mil.arl.gift.common.KnowledgeSessionCreated;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.PublishLessonScore;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.aar.LogFilePlaybackService;
import mil.arl.gift.common.aar.LogFilePlaybackTarget;
import mil.arl.gift.common.aar.LogIndexService;
import mil.arl.gift.common.aar.LogMetadata;
import mil.arl.gift.common.aar.LogSpan;
import mil.arl.gift.common.aar.ScoreNodeUpdate;
import mil.arl.gift.common.aar.util.AbstractAarAssessmentManager;
import mil.arl.gift.common.aar.util.ApplyPatchResult;
import mil.arl.gift.common.aar.util.MessageManager;
import mil.arl.gift.common.aar.util.PatchedState;
import mil.arl.gift.common.aar.util.PerformancePatchState;
import mil.arl.gift.common.course.AssessmentChainOfCustody;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.SessionMember;
import mil.arl.gift.common.course.dkf.session.SessionScenarioInfo;
import mil.arl.gift.common.course.strategy.ApplyStrategies;
import mil.arl.gift.common.course.strategy.AuthorizeStrategiesRequest;
import mil.arl.gift.common.course.strategy.ExecuteOCStrategy;
import mil.arl.gift.common.course.strategy.StrategyStateUpdate;
import mil.arl.gift.common.course.strategy.StrategyToApply;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.gwt.client.FailureResponse;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.SuccessfulResponse;
import mil.arl.gift.common.gwt.server.BrowserWebSession;
import mil.arl.gift.common.gwt.server.SessionStatusListener;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.logger.DomainSessionMessageEntry;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.common.ta.util.ExternalMonitorConfig;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StrategyUtil;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.api.SubjectUtil;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.DomainSessionMessageInterface;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.SequenceNumberGenerator;
import mil.arl.gift.tools.dashboard.server.gamemaster.DomainInfoCacheData.DomainSessionKey;
import mil.arl.gift.tools.dashboard.server.gamemaster.DomainInformationCache;
import mil.arl.gift.tools.dashboard.server.gamemaster.GameMasterAssessmentManager;
import mil.arl.gift.tools.dashboard.server.messagehandlers.DomainSessionEntityFilter;
import mil.arl.gift.tools.dashboard.server.messagehandlers.KnowledgeSessionMessageHandler;
import mil.arl.gift.tools.dashboard.server.messagehandlers.SessionEntityFilter;
import mil.arl.gift.tools.dashboard.shared.messages.DashboardMessage;
import mil.arl.gift.tools.dashboard.shared.messages.GatewayConnection;
import mil.arl.gift.tools.dashboard.shared.messages.InitializationMessage;
import mil.arl.gift.tools.dashboard.shared.messages.KnowledgeSessionState;
import mil.arl.gift.tools.dashboard.shared.messages.ProcessedBookmarkCache;
import mil.arl.gift.tools.dashboard.shared.messages.ProcessedStrategyCache;
import mil.arl.gift.tools.dashboard.shared.rpcs.SessionTimelineInfo;
import mil.arl.gift.tools.map.shared.SIDC;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.db.DbServicesInterface;

/**
 * Manager to handle messages relating to knowledge sessions.  Essentially this takes incoming
 * ActiveMQ messages and routes any domain session messages to any browsers that have subscribed to
 * receive the messages.
 *
 * @author nblomberg
 *
 */
public class KnowledgeSessionMessageManager implements BrowserSessionListener {


    /** Instance of the logger. */
    private static final Logger logger = LoggerFactory.getLogger(KnowledgeSessionMessageManager.class);

    /** Instance of the singleton class. */
    private static KnowledgeSessionMessageManager instance = null;

    /** Instance of the manager class used to manage user/browser sessions. */
    private UserSessionManager userSessionManager = null;
    
    /** 
     * A lock used to ensure that browser sessions registering and deregistering for session playback 
     * is kept synchronous. This prevents some race conditions that can occur if a playback is 
     * deregistered at the same time as or after another playback is registered
     */
    private Object playbackRegistrationLock = new Object();

    /**
     * Constructor - default
     *
     * @param userSessionManager The manager responsible for handling user
     *        sessions.
     */
    private KnowledgeSessionMessageManager(UserSessionManager userSessionManager) {
        this.userSessionManager = userSessionManager;

        this.userSessionManager.addStatusListener(this);
    }


    /**
     * Create an an instance of the singleton
     *
     * @param userSessionManager The manager responsible for handling user sessions.
     */
    public static void createInstance(UserSessionManager userSessionManager) {
        if (instance == null) {
            instance = new KnowledgeSessionMessageManager(userSessionManager);
        }
    }

    /**
     * Get an instance of the singleton.  The user must call createInstance() during initialization or this will return null.
     *
     * @return Instance of the singleton.  Can return null if createInstance() has not been called.
     */
    public static KnowledgeSessionMessageManager getInstance() {
        if (instance == null) {
            logger.error("Singleton instance is null.  Please make sure to call createInstance() during initialization prior to using the class.");
        }

        return instance;
    }

    /**
     * Handle the incoming message
     *
     * @param domainMsg The {@link DomainSessionMessageInterface} to handle.
     *        Can't be null.
     * @param isPlayback A flag indicating whether or not the message being
     *        received is a playback message, or a live message from the bus.
     *        True indicates the message is a playback message, false indicates
     *        the message is from the bus.
     */
    public void handleMessage(DomainSessionMessageInterface domainMsg, boolean isPlayback) {

        /* Cast and unpack the message */
        final DomainSessionKey key = new DomainSessionKey(domainMsg);
        Object payload = domainMsg.getPayload();

        /* This flag indicates whether or not an AuthorizeStrategiesRequest
         * should automatically be replied to with an ApplyStrategies message
         * for all requested strategies. This 'short circuit' should occur when
         * all attached web monitor clients are in auto mode. */
        final boolean isAuthorizeStrategies = payload instanceof AuthorizeStrategiesRequest;
        boolean shouldStrategiesShortCircuit = isAuthorizeStrategies;

        final boolean isNewKnowledgeSession = payload instanceof KnowledgeSessionCreated;
        final boolean isEnding = domainMsg.getMessageType() == MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST
                || domainMsg.getMessageType() == MessageTypeEnum.LESSON_COMPLETED;

        for (String browserSessionKey : userSessionManager.getBrowserSessionKeys()) {
            final DashboardBrowserWebSession browserSession = getDashboardSession(browserSessionKey, false);
            if (browserSession == null) {
                continue;
            }

            /* Allow message if a session is started/ending or the message is
             * from a monitored session */
            if (isNewKnowledgeSession || isEnding || isBrowserSessionMonitoringLiveDomainSession(browserSession, key)) {

                shouldStrategiesShortCircuit &= browserSession.handleMessage(domainMsg);
            }
        }

        /* Do not need any additional processing for playback messages */
        if (isPlayback) {
            return;
        }

        /* If the AuthorizeStrategiesRequest should short circuit or if the
         * activities are mandatory, send an ApplyStrategies message to the
         * Domain Module. */
        if (isAuthorizeStrategies) {
            AuthorizeStrategiesRequest authStrats = (AuthorizeStrategiesRequest) payload;

            List<StrategyToApply> strategiesToApply = new ArrayList<>();
            for (List<StrategyToApply> strategies : authStrats.getRequests().values()) {
                if (shouldStrategiesShortCircuit) {
                    strategiesToApply.addAll(strategies);
                } else {
                    for (StrategyToApply strategyToApply : strategies) {
                        Strategy strategy = strategyToApply.getStrategy();
                        final List<Serializable> strategyActivities = strategy.getStrategyActivities();
                        if (CollectionUtils.isEmpty(strategyActivities)) {
                            continue;
                        }

                        /* If it is mandatory or only contains activities
                         * directed at the controller, add it to the process
                         * list and move on to the next strategy */
                        if (StrategyUtil.isMandatory(strategyActivities.get(0))
                                || StrategyUtil.isToControllerOnly(strategy)) {
                            strategiesToApply.add(strategyToApply);
                        }
                    }
                }
            }

            if (!strategiesToApply.isEmpty()) {
                /* Send the message applying the strategies */
                WebMonitorModule.getInstance().sendApplyStrategies(
                        new ApplyStrategies(strategiesToApply, Constants.AUTO_APPLIED_BY_GIFT, null), key);
            }
        }

        /* If the scenario is ending, clean up all cached information. */
        if (isEnding) {
            if (logger.isDebugEnabled()) {
                logger.debug("Dropping cached domain session data");
            }

            WebMonitorModule.getInstance().getDomainInformationCache().dropDomainSessionData(key);
        }
    }

    /**
     * Converts an {@link InitializePedagogicalModelRequest} containing the
     * scenario {@link Strategy strategies} into a mapping of {@link Strategy}
     * name to {@link Strategy}.
     *
     * @param initPedRequest The {@link InitializePedagogicalModelRequest} to
     *        convert. Can't be null.
     * @return The mapping of {@link Strategy} name to {@link Strategy}. The map
     *         will never be null. If The
     *         {@link InitializePedagogicalModelRequest} contains course actions
     *         instead of scenario actions, an empty map will be returned.
     */
    private LinkedHashMap<Strategy, Boolean> convertPedagogicalRequestToStrategyLookup(InitializePedagogicalModelRequest initPedRequest) {
        LinkedHashMap<Strategy, Boolean> toRet = new LinkedHashMap<>();

        /* We only care about DKF actions */
        if (initPedRequest == null || initPedRequest.isCourseActions()) {
            return toRet;
        }

        try {
            /* Deserializes the XML string */
            byte[] actionsBytes = initPedRequest.getActions().getBytes();
            UnmarshalledFile actionsFile = AbstractSchemaHandler.parseAndValidate(
                    Actions.class,
                    new ByteArrayInputStream(actionsBytes),
                    (java.io.File) null,
                    true);

            Actions actions = (Actions) actionsFile.getUnmarshalled();
            List<Strategy> strategies = actions.getInstructionalStrategies().getStrategy();

            LinkedHashMap<String, Strategy> nameToStrategyMap = new LinkedHashMap<>();
            for (Strategy strategy : strategies) {
                nameToStrategyMap.put(strategy.getName(), strategy);
                toRet.put(strategy, false);
            }

            /* Find strategy associations with the state transitions */
            List<StateTransition> stateTransitions = actions.getStateTransitions() == null ? null
                    : actions.getStateTransitions().getStateTransition();
            if (stateTransitions != null) {
                for (StateTransition stateTransition : stateTransitions) {
                    StrategyChoices choices = stateTransition.getStrategyChoices();
                    if (choices == null) {
                        continue;
                    }

                    for (StrategyRef strategyRef : choices.getStrategies()) {
                        Strategy transitionStrategy = nameToStrategyMap.get(strategyRef.getName());
                        if (transitionStrategy != null) {
                            toRet.put(transitionStrategy, true);
                        }
                    }
                }
            }
        } catch (FileNotFoundException | JAXBException | SAXException e) {
            logger.error("There was an error while handling the " + initPedRequest + " message payload.", e);
        }

        return toRet;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[KnowledgeSessionMessageManager: ");
        sb.append("]");
        return sb.toString();
    }

    /**
     * Checks if the provided domain session is being monitored by the browser
     * session.
     *
     * @param dashboardSession the browser session being checked for monitoring the
     *        domain session.
     * @param key The unique key that identifies the domain session that is
     *        being checked.
     * @return true if the domain session is being monitored by the browser
     *         session; false otherwise.
     */
    private boolean isBrowserSessionMonitoringLiveDomainSession(DashboardBrowserWebSession dashboardSession, DomainSessionKey key) {
        AbstractKnowledgeSession knowledgeSession = dashboardSession.getKnowledgeSession();

        /* Ensure that there is a value */
        if (knowledgeSession == null) {
            return false;
        }

        /* Check to make sure this is the same domain session id */
        final DomainSessionKey subscribedKey = new DomainSessionKey(knowledgeSession);
        return subscribedKey.equals(key);
    }

    /**
     * Registers a browser session to listen to events from a knowledge session.
     *
     * @param browserSessionKey - The browser session key
     * @param knowledgeSession - The knowledge session to subscribe to
     */
    public void registerBrowser(String browserSessionKey, AbstractKnowledgeSession knowledgeSession) {
        if(logger.isDebugEnabled()){
            logger.debug("registerBrowser(): " + browserSessionKey);
        }

        DashboardBrowserWebSession dashSession = getDashboardSession(browserSessionKey);

        if (dashSession.getMessageHandler() == null) {
            /* Add a knowledge session message handler to the browser session */
            dashSession.setMessageHandler(new KnowledgeSessionMessageHandler(browserSessionKey));
        }

        /* Registering for global life cycle events (e.g. start and end) */
        if (knowledgeSession == null) {
            return;
        }

        final SessionMember hostSessionMember = knowledgeSession.getHostSessionMember();
        final int domainSessionId = hostSessionMember.getDomainSessionId();

        /* Inform the WebMonitor that a specific domain session has been
         * attached to through this module. */
        final DomainSessionKey key = new DomainSessionKey(knowledgeSession);
        final boolean isPast = knowledgeSession.inPastSessionMode();

        /* Add the domain session to the web monitor list if not in past mode */
        if (!isPast) {
            WebMonitorModule.getInstance().addAttachedDomainSession(domainSessionId);
        }

        /* Create the server-side filter for the knowledge session. */
        DomainSessionEntityFilter filter = SessionEntityFilter.getInstance(browserSessionKey).createDomainSessionFilter(knowledgeSession);

        final AbstractKnowledgeSession previousMonitoredSession = dashSession.getKnowledgeSession();
        if (previousMonitoredSession != null && !previousMonitoredSession.equals(knowledgeSession)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Deregistering existing session '" + previousMonitoredSession.getNameOfSession()
                        + "' with host domain id ["
                        + previousMonitoredSession.getHostSessionMember().getDomainSessionId() + "]");
            }

            /* Determine which deregister to call */
            if (previousMonitoredSession.inPastSessionMode()) {
                deregisterBrowserPlayback(browserSessionKey);
            } else {
                deregisterBrowser(browserSessionKey, previousMonitoredSession);
            }
        }

        dashSession.setKnowledgeSession(knowledgeSession);

        /* Send an initialization message to the registered browser */
        InitializePedagogicalModelRequest initPedRequest = WebMonitorModule.getInstance().getDomainInformationCache()
                .getLastInitializePedagogicalRequest(key);
        if (initPedRequest == null && logger.isDebugEnabled()) {
            logger.debug("Missing a ped request for domain session id: " + domainSessionId);
        }

        LinkedHashMap<Strategy, Boolean> strategyLookup = convertPedagogicalRequestToStrategyLookup(initPedRequest);
        LearnerState learnerState = WebMonitorModule.getInstance().getDomainInformationCache().getLastLearnerState(key);
        KnowledgeSessionState knowledgeSessionState = new KnowledgeSessionState(learnerState);
        knowledgeSessionState.setCachedTaskStates(
                WebMonitorModule.getInstance().getDomainInformationCache().getCachedTaskStates(key));
        knowledgeSessionState.setCachedProcessedStrategy(
                WebMonitorModule.getInstance().getDomainInformationCache().getCachedProcessedStrategies(key));
        knowledgeSessionState.setCachedProcessedBookmark(
                WebMonitorModule.getInstance().getDomainInformationCache().getCachedProcessedBookmarks(key));

        InitializationMessage initPayload = new InitializationMessage(strategyLookup, knowledgeSessionState);

        DashboardMessage dashboardMessage;
        if (isPast) {
            Long timestamp = WebMonitorModule.getInstance().getDomainInformationCache().getLatestMessageTimestamp(key);
            if (timestamp == null) {
                final LogMetadata log = dashSession.getLogMetadata();
                if (log != null) {
                    timestamp = log.getStartTime();
                }
            }

            dashboardMessage = new DashboardMessage(initPayload, knowledgeSession, timestamp);
        } else {
            dashboardMessage = new DashboardMessage(initPayload, knowledgeSession);
        }

        dashSession.getWebSocket().send(dashboardMessage);
        
        if (knowledgeSession.inPastSessionMode()) {
            
            /* Start the heartbeat service when the sesssion has been registered. If a past session playback 
             * is being started, then this will ensure that entities on the map will remain on the map if
             * the user seeks to a different time before playing.*/
            filter.startHeartbeatService();
        }
    }

    /**
     * Deregister a browser session which causes the browser session to no longer listen to the
     * events for a knowledge session.
     *
     * @param browserSessionKey - The browser session key
     * @param knowledgeSession - The knowledge session to unsubscribe from
     */
    public void deregisterBrowser(String browserSessionKey, final AbstractKnowledgeSession knowledgeSession) {
        if (logger.isDebugEnabled()) {
            logger.debug("deregisterBrowser(): " + browserSessionKey + " for session: " + knowledgeSession);
        }

        DashboardBrowserWebSession dashSession = getDashboardSession(browserSessionKey);

        /* Can't deregister something that doesn't exist */
        if (knowledgeSession == null) {
            return;
        }

        /* Determine whether or not there are still clients monitoring the
         * session of the client who just detached. */
        AbstractKnowledgeSession sessionProperty = dashSession.getKnowledgeSession();

        /* Can't deregister something that doesn't exist */
        if (sessionProperty == null) {
            return;
        }

        /* No session is being monitored now */
        dashSession.setKnowledgeSession(null);

        final SessionMember hostSessionMember = knowledgeSession.getHostSessionMember();
        final int hostDomainSessionId = hostSessionMember.getDomainSessionId();
        final DomainSessionKey key = new DomainSessionKey(knowledgeSession);
        SessionEntityFilter.getInstance(browserSessionKey).destroyDomainSessionFilter(key);

        /* See if the domain session has anymore attached sessions */
        boolean domainHasAttachedClients = false;

        for (String bsk : userSessionManager.getBrowserSessionKeys()) {

            final BrowserWebSession browserSession = userSessionManager.getBrowserSession(bsk);
            if (browserSession instanceof DashboardBrowserWebSession) {
                DashboardBrowserWebSession dSession = (DashboardBrowserWebSession) browserSession;

                AbstractKnowledgeSession ks = dSession.getKnowledgeSession();
                if (ks == null) {
                    continue;
                }

                if (hostDomainSessionId == ks.getHostSessionMember().getDomainSessionId()) {
                    domainHasAttachedClients = true;
                    break;
                }
            }
        }

        /* If there are no more clients attached to the domain session, alert
         * the Web Monitor. */
        if (!domainHasAttachedClients) {
            WebMonitorModule.getInstance().removeAttachedDomainSession(key);
        }
    }

    /**
     * Registers a {@link LogFilePlaybackService} with a specified
     * {@link BrowserWebSession}.
     * <br/><br/>
     * <b>Note:</b> Registering and deregistering browser playbacks are 
     * synchronous operations, so if another session is being registered or
     * deregistered, then this method will complete after the previous 
     * session is handled.
     *
     * @param browserSessionKey The unique identifier of the
     *        {@link BrowserWebSession} for which the
     *        {@link LogFilePlaybackService} is created. Can't be null.
     * @param log The {@link LogMetadata} describing the log that the created
     *        {@link LogFilePlaybackService} should play. Can't be null.
     * @throws IOException if there was a problem creating the
     *         {@link LogFilePlaybackService}.
     * @throws Exception if no messages where found in the log file in the log span
     */
    public void registerBrowserPlayback(String browserSessionKey, LogMetadata log) throws IOException, Exception {
        if (StringUtils.isBlank(browserSessionKey)) {
            throw new IllegalArgumentException("The parameter 'browserSessionKey' cannot be blank.");
        } else if (log == null) {
            throw new IllegalArgumentException("The parameter 'log' cannot be null.");
        }
        
        synchronized (playbackRegistrationLock) {

        final DashboardBrowserWebSession browserSession = getDashboardSession(browserSessionKey);
        if (browserSession == null) {
            throw new IllegalArgumentException("No browser session with the key " + browserSessionKey + " exists.");
        }

        final AbstractKnowledgeSession knowledgeSession = log.getSession();
        final SessionMember hostSessionMember = knowledgeSession.getHostSessionMember();
        final int domainSessionId = hostSessionMember.getDomainSessionId();
        final DomainSessionKey key = new DomainSessionKey(knowledgeSession);

        // need to remove the workspace course folder from the path to the observer controls audio file
        // so that the Game Master can play it in past session
        LogFilePlaybackService.prepareSessionOutputAudioFileName(knowledgeSession.getObserverControls());

        final AbstractKnowledgeSession previouslyMonitoredSession = browserSession.getKnowledgeSession();
        if (previouslyMonitoredSession != null && !previouslyMonitoredSession.equals(knowledgeSession)) {
            /* Determine which deregister to call */
            if (previouslyMonitoredSession.inPastSessionMode()) {
                
                //need to deregister asynchronously here, since we are already in a synchronous block
                deregisterBrowserPlaybackAsync(browserSessionKey);
                
            } else {
                deregisterBrowser(browserSessionKey, previouslyMonitoredSession);
            }
        }

        browserSession.setKnowledgeSession(knowledgeSession);

        File logFile = Paths.get(PackageUtil.getDomainSessions(), log.getLogFile()).toFile();
        LogSpan logSpan = log.getLogSpan();
        LogFilePlaybackTarget target = msg -> {
            /* If a domain session message is being played back from a log,
             * associate it with the appropriate browser session to avoid
             * potential collisions if other browser sessions attempt to play
             * back the same knowledge session */
                if(msg instanceof DomainSessionMessageEntry) {
                    ((DomainSessionMessageEntry) msg).setPlaybackId(browserSessionKey);
                }
                
            /* Let the domainInfoCache process any messages it needs to. Must
             * occur after we set the playback id if msg is a
             * DomainSessionMessageEntry. */
            WebMonitorModule.getInstance().getDomainInformationCache().processMessage(msg);

            if (msg instanceof DomainSessionMessageInterface) {
                /* Ensure that playback messages are only handled by the browser
                 * session that started the playback */
                handleMessage((DomainSessionMessageInterface) msg, true);
            }
        };
        Predicate<Message> msgFilter = msg -> {
            return msg.getDestinationQueueName().startsWith(SubjectUtil.GATEWAY_TOPIC_PREFIX)
                    || MessageTypeEnum.AUTHORIZE_STRATEGIES_REQUEST.equals(msg.getMessageType())
                    || MessageTypeEnum.APPLY_STRATEGIES.equals(msg.getMessageType())
                    || WebMonitorModule.WHITE_LISTED_TOPIC_MESSAGE_TYPES.contains(msg.getMessageType());
        };

        Long timestamp = WebMonitorModule.getInstance().getDomainInformationCache().getLatestMessageTimestamp(key);
        if (timestamp == null) {
            timestamp = log.getStartTime();
        }

        final UserSession userSession = new UserSession(hostSessionMember.getUserSession().getUserId());
        userSession.setUsername(hostSessionMember.getSessionMembership().getUsername());
        userSession.setExperimentId(hostSessionMember.getUserSession().getExperimentId());
        
        //create a dummy domain session message to allow clients to detect that a new playback session has been created
        DomainSessionMessage domainMsg = new DomainSessionMessage(MessageTypeEnum.KNOWLEDGE_SESSION_CREATED,
                SequenceNumberGenerator.nextSeqNumber(), 0, timestamp, null, null, ModuleTypeEnum.DOMAIN_MODULE, "",
                new KnowledgeSessionCreated(knowledgeSession), userSession, domainSessionId, false);
        
        //wrap the dummy domain session message in a playback message so that it is associated with the browser session's playback ID
        DomainSessionMessageEntry playbackMsg = new DomainSessionMessageEntry(domainMsg.getSourceEventId(),
                    domainMsg.getDomainSessionId(), domainMsg.getUserSession(), 0, timestamp, domainMsg);
        playbackMsg.setPlaybackId(browserSessionKey);

        WebMonitorModule.getInstance().processMonitorTopicMessage(playbackMsg);
        WebMonitorModule.getInstance().getDomainInformationCache().cacheKnowledgeSession(playbackMsg.getDomainSessionId(), knowledgeSession);

        browserSession.setLogMetadata(log);
        registerBrowser(browserSessionKey, knowledgeSession);

        AbstractAarAssessmentManager assessmentMgr = null;
        if(log.getDkf() != null) {
            
            /* Attempt to load the found DKF reference into an assessment manager */
            assessmentMgr = new GameMasterAssessmentManager(log.getDkf(), knowledgeSession);
        }

        final LogFilePlaybackService service = new LogFilePlaybackService(logFile, logSpan, hostSessionMember.getSessionMembership().getUsername(), target,
                msgFilter, assessmentMgr);

        /* Save the service to the map, and stop playback of any previous
         * services. */
        LogFilePlaybackService oldService = browserSession.setActivePlaybackService(service);
        if (oldService != null) {
            oldService.terminatePlayback();
        }

        /* Register a handler to terminate the playback service if the session
         * ends (the websocket disconnects) */
        browserSession.addStatusListener(new SessionStatusListener() {

            @Override
            public void onStop() {

            }

            @Override
            public void onEnd() {
                final LogFilePlaybackService service = browserSession.getActivePlaybackService();
                if (service != null) {
                    service.terminatePlayback();
                }
            }
        });

        service.seek(log.getStartTime());
        }
    }
    
    /**
     * Permanently stops playback of the current scenario for a specified
     * browser session. This should be called as the final cleanup and not
     * simply as a mechanism for pausing playback which may be later resumed.
     * For pausing playback use the {@link #stopPlayback(String)} method
     * instead.
     * <br/><br/>
     * <b>Note:</b> Registering and deregistering browser playbacks are 
     * synchronous operations, so if another session is being registered or
     * deregistered, then this method will complete after the previous 
     * session is handled.
     *
     * @param browserSessionKey The unique identifier of the browser session for
     *        which playback should be stopped.
     */
    public void deregisterBrowserPlayback(String browserSessionKey) {
        
        synchronized (playbackRegistrationLock) {
            deregisterBrowserPlaybackAsync(browserSessionKey);
        }
    }

    /**
     * Permanently stops playback of the current scenario for a specified
     * browser session. This should be called as the final cleanup and not
     * simply as a mechanism for pausing playback which may be later resumed.
     * For pausing playback use the {@link #stopPlayback(String)} method
     * instead.
     * <br/><br/>
     * <b>Note</b>: Unlike {@link #deregisterBrowserPlayback(String)}, this method
     * will <u>NOT</u> be performed synchronously with other register/deregister calls.
     * This method should only be used inside of another register/deregister call that
     * is already being performed synchronously.
     *
     * @param browserSessionKey The unique identifier of the browser session for
     *        which playback should be stopped.
     */
    private void deregisterBrowserPlaybackAsync(String browserSessionKey) {
        
        DashboardBrowserWebSession dashSession = getDashboardSession(browserSessionKey);

        LogFilePlaybackService service = dashSession.setActivePlaybackService(null);
        if (service != null) {
            service.terminatePlayback();
        }
        
        LogMetadata logMetadata = dashSession.setLogMetadata(null);
        if (logMetadata != null) {
            final DomainSessionKey key = new DomainSessionKey(logMetadata.getSession());

            /* Destroy the server-side filter for this session. */
            SessionEntityFilter.getInstance(browserSessionKey).destroyDomainSessionFilter(key);

            /* Remove this knowledge session from the cache */
            WebMonitorModule.getInstance().getDomainInformationCache().dropDomainSessionData(key);

            /* No session is being monitored now */
            dashSession.setKnowledgeSession(null);
        }
    }

    /**
     * Gets a mapping of simulation time to the {@link LearnerState} at that
     * given time for the currently playing past session of a specified browser
     * session.
     *
     * @param browserSessionKey The unique identifier of the browser session
     *        whose played-back log should be queried for {@link LearnerState}.
     * @return A mapping of a Unix epoch time from the past simulation (measured
     *         in milliseconds) to the {@link LearnerState} that was produced at
     *         that time. Can be empty if:
     *         <ul>
     *         <li>there are no {@link LearnerState} objects for the played back
     *         log</li>
     *         <li>the specified browser session doesn't exist</li>
     *         <li>the browser session is not currently playing back a log.</li>
     *         </ul>
     */
    public SessionTimelineInfo fetchLearnerStates(String browserSessionKey) {
        final SessionTimelineInfo toRet = new SessionTimelineInfo();

        final DashboardBrowserWebSession dashSession = getDashboardSession(browserSessionKey);
        if(dashSession == null) {
            return toRet;
        }

        LogMetadata metadata = dashSession.getLogMetadata();
        if (metadata == null) {
            return toRet;
        }

        final Iterator<MessageManager> iterator =  dashSession.getActivePlaybackService().getMessages().stream().iterator();

            // the last time stamp an observer update happened
            Long lastObservedMsgTimeStamp = null;
            
            while (iterator.hasNext()) {
                MessageManager msgManager = iterator.next();
                Message msg = msgManager.getMessage();
                if (msg.getMessageType() == MessageTypeEnum.LEARNER_STATE) {

                    Long timeStamp = msg.getTimeStamp();

                    /* Gather learner state messages from the log to populate
                     * the timeline. Check if there is patch for this learner
                     * state before modifying the timestamp. */
                    final LearnerState learnerState = (LearnerState)msg.getPayload();

                    /* Check if the un-patched state has an observer update that
                     * is new compared to the last observer update */
                    Long observationStartedTime = ((LearnerState) msgManager.getOriginalMessage().getPayload()).getPerformance()
                            .getObservationStartedTime();
                    if(observationStartedTime != null && 
                            !observationStartedTime.equals(lastObservedMsgTimeStamp)){
                        // found a new observer update
                        timeStamp = observationStartedTime;
                        lastObservedMsgTimeStamp = observationStartedTime;
                    }
    
                    if(toRet.getLearnerStates().containsKey(timeStamp)){
                        // this learner state collides with another, need a new timestamp value, try adding a millisecond
                        
                        Long candidateTime;
                        int msAdded = 1;
                        while(true){
                            candidateTime = Long.valueOf(timeStamp + msAdded);
                            if(!toRet.getLearnerStates().containsKey(candidateTime)){
                                break;
                            }
                            
                            msAdded++;
                        }
                        
                        logger.warn("Found a duplicate learner state at time "+timeStamp+".  Adding "+msAdded+" milliseconds to the following:\n"+learnerState);
                        timeStamp = candidateTime;
                    }
                    
                    toRet.getLearnerStates().put(timeStamp, learnerState);
                    /* If the timestamp was adjusted, update the patch file
                     * to match */
                    if (Long.compare(msg.getTimeStamp(), timeStamp) != 0) {
                        msgManager.setAdjustedTimestamp(timeStamp);
                    }

                    if (msgManager.isPatched()) {
                        Set<String> patchedNames = new HashSet<>();
                        for (PatchedState patch : msgManager.getPatchedStates()) {
                            
                            if(patch instanceof PerformancePatchState) {
                                PerformancePatchState perfPatch = (PerformancePatchState) patch;
                                patchedNames.add(perfPatch.getName());
                        }
                        }
                        toRet.getPatchedLearnerStatePerformances().put(timeStamp, patchedNames);
                    }

                } else if(msg.getMessageType() == MessageTypeEnum.APPLY_STRATEGIES) {

                    // Gather apply strategies messages from the log to populate the timeline
                    Long timeStamp = msg.getTimeStamp();
                    ApplyStrategies applyStrategies = (ApplyStrategies) msg.getPayload();
    
                    if(toRet.getStrategies().containsKey(timeStamp)){
                        // this applied strategy collides with another, need a new timestamp value, try adding a millisecond
                        
                        Long candidateTime;
                        int msAdded = 1;
                        while(true){
                            candidateTime = Long.valueOf(timeStamp + msAdded);
                            if(!toRet.getStrategies().containsKey(candidateTime)){
                                break;
                            }
                            
                            msAdded++;
                        }
                        
                        logger.warn("Found a duplicate applied strategy at time "+timeStamp+".  Adding "+msAdded+" milliseconds to the following:\n"+applyStrategies);
                        timeStamp = candidateTime;
                    }
                    
                    // Collect all the strategies applied by this message in a single update
                    StrategyStateUpdate updateMsg = new StrategyStateUpdate(applyStrategies.getEvaluator());
                    
                    for (StrategyToApply entry : applyStrategies.getStrategies()) {
                        final String reason = entry.getTrigger();

                        List<Strategy> applied = updateMsg.getAppliedStrategies().get(reason);
                        if(applied == null){
                            applied = new ArrayList<>();
                            updateMsg.getAppliedStrategies().put(reason, applied);
                        }

                        Strategy strategy = entry.getStrategy();
                        final List<Serializable> strategyActivities = strategy.getStrategyActivities();
                        if (CollectionUtils.isEmpty(strategyActivities)) {
                            continue;
                        }

                        applied.add(strategy);
                    }
                    
                    toRet.getStrategies().put(timeStamp, updateMsg);
                    
                }else if(msg.getMessageType() == MessageTypeEnum.EXECUTE_OC_STRATEGY) {

                    /* Gather execute OC strategy messages from the log to populate the timeline with strategies (e.g. feedback)
                     sent to the OCT */
                    Long timeStamp = msg.getTimeStamp();
                    ExecuteOCStrategy exeOCStrategy = (ExecuteOCStrategy) msg.getPayload();
                    
                    if(!exeOCStrategy.isScenarioControl()){
                        /* strategies for the OCT that are part of a strategy requested by the pedagogical module
                         are already collected via the Apply Strategies logic above.  The logic below is more
                         for strategies that come from scenario elements such as task triggers */
                        continue;
                    }
    
                    if(toRet.getStrategies().containsKey(timeStamp)){
                        // this execute strategy collides with another, need a new timestamp value, try adding a millisecond
                        
                        Long candidateTime;
                        int msAdded = 1;
                        while(true){
                            candidateTime = Long.valueOf(timeStamp + msAdded);
                            if(!toRet.getStrategies().containsKey(candidateTime)){
                                break;
                            }
                            
                            msAdded++;
                        }
                        
                        logger.warn("Found a duplicate execute OC strategy at time "+timeStamp+".  Adding "+msAdded+" milliseconds to the following:\n"+exeOCStrategy);
                        timeStamp = candidateTime;
                    }
                    
                    // Collect all the strategies applied by this message in a single update
                    StrategyStateUpdate updateMsg = new StrategyStateUpdate(exeOCStrategy.getEvaluator());
                    
                    Strategy strategy = exeOCStrategy.getStrategy();
                    final String reason = exeOCStrategy.getReason();

                    List<Strategy> applied = updateMsg.getAppliedStrategies().get(reason);
                    if(applied == null){
                        applied = new ArrayList<>();
                        updateMsg.getAppliedStrategies().put(reason, applied);
                    }

                    final List<Serializable> strategyActivities = strategy.getStrategyActivities();
                    if (CollectionUtils.isEmpty(strategyActivities)) {
                        continue;
                    }

                    applied.add(strategy);
                    
                    toRet.getStrategies().put(timeStamp, updateMsg);
                }
            } // end while

        /* Gather any summative assessments that need to be shown in the timeline */
        toRet.setScenarioInfo(dashSession.getActivePlaybackService().getKnowledgeSessionScenario());

        return toRet;
    }

    @Override
    public void onBrowserSessionEnding(BrowserWebSession webSession) {

        if (webSession instanceof DashboardBrowserWebSession) {
            cleanupSession(webSession.getBrowserSessionKey());
            }
        }

    /**
     * Updates the game master session state as being in 'auto' or 'manual' mode. In 'auto' mode,
     * all incoming strategy requests will be automatically approved with the suggested actions.
     *
     * @param browserSessionKey the key of the browser session being used to monitor the knowledge
     *        session.
     * @param isAutoMode true if the game master session is in 'auto' mode; false otherwise.
     */
    public void updateGameMasterAutoState(String browserSessionKey, boolean isAutoMode) {
        BrowserWebSession browserSession = userSessionManager.getBrowserSession(browserSessionKey);

        if (browserSession != null && browserSession instanceof DashboardBrowserWebSession) {
            DashboardBrowserWebSession dashSession = (DashboardBrowserWebSession) browserSession;
            dashSession.setAutoModeEnabled(isAutoMode);
        }
    }

    /**
     * Caches the processed strategies referenced by the
     * {@link ProcessedStrategyCache} items.
     *
     * @param browserSessionKey the key of the browser session being used to
     *        monitor the knowledge session.
     * @param strategyCache the collection of processed strategies to cache.
     */
    public void cacheProcessedStrategy(String browserSessionKey, Collection<ProcessedStrategyCache> strategyCache) {
        final DashboardBrowserWebSession dashSession = getDashboardSession(browserSessionKey);

        AbstractKnowledgeSession knowledgeSession = dashSession.getKnowledgeSession();
        if (knowledgeSession == null || CollectionUtils.isEmpty(strategyCache)) {
            return;
        }

        /* Organize processed cache items by knowledge session */
        Map<AbstractKnowledgeSession, List<ProcessedStrategyCache>> cacheMap = new HashMap<>();
        for (ProcessedStrategyCache cacheItem : strategyCache) {
            AbstractKnowledgeSession session = cacheItem.getKnowledgeSession();
            /* Check if knowledge session is supported by this browser
             * session */
            if (!knowledgeSession.equals(session)) {
                continue;
            }

            List<ProcessedStrategyCache> cache = cacheMap.get(session);
            if (cache == null) {
                cache = new ArrayList<>();
                cacheMap.put(session, cache);
            }

            cache.add(cacheItem);
        }

        /* Add processed cache items to the cache */
        final DomainInformationCache domainInformationCache = WebMonitorModule.getInstance()
                .getDomainInformationCache();
        for (Entry<AbstractKnowledgeSession, List<ProcessedStrategyCache>> entry : cacheMap.entrySet()) {
            final DomainSessionKey key = new DomainSessionKey(entry.getKey());
            domainInformationCache.cacheProcessedStrategy(key, entry.getValue());
        }
    }
    
    /**
     * Caches the processed bookmarks referenced by the
     * {@link ProcessedBookmarkCache} items.
     *
     * @param browserSessionKey the key of the browser session being used to
     *        monitor the knowledge session.
     * @param bookmarkCache the collection of processed bookmarks to cache.
     */
    public void cacheProcessedBookmark(String browserSessionKey, ProcessedBookmarkCache bookmarkCache) {
        final DashboardBrowserWebSession dashSession = getDashboardSession(browserSessionKey);

        AbstractKnowledgeSession knowledgeSession = dashSession.getKnowledgeSession();
        if (knowledgeSession == null || bookmarkCache == null) {
            return;
        }

        /* Organize processed cache items by knowledge session */
        Map<AbstractKnowledgeSession, List<ProcessedBookmarkCache>> cacheMap = new HashMap<>();
      
        AbstractKnowledgeSession session = bookmarkCache.getKnowledgeSession();
        /* Check if knowledge session is supported by this browser
         * session */
        if (!knowledgeSession.equals(session)) {
            return;
        }

        List<ProcessedBookmarkCache> cache = cacheMap.get(session);
        if (cache == null) {
            cache = new ArrayList<>();
            cacheMap.put(session, cache);
        }

        cache.add(bookmarkCache);

        /* Add processed cache items to the cache */
        final DomainInformationCache domainInformationCache = WebMonitorModule.getInstance()
                .getDomainInformationCache();
        for (Entry<AbstractKnowledgeSession, List<ProcessedBookmarkCache>> entry : cacheMap.entrySet()) {
            final DomainSessionKey key = new DomainSessionKey(entry.getKey());
            domainInformationCache.cacheProcessedBookmark(key, entry.getValue());
        }
    }

    /**
     * Sets the time of the knowledge session playback being run by the browser
     * session with the given key
     *
     * @param browserSessionKey the key of the browser session being used to
     *        play back a knowledge session. Cannot be null.
     * @param time the time to set the session playback to
     * @throws IllegalArgumentException one of the parameters is invalid.
     * @throws IllegalStateException the playback service or log metadata for
     *         this browser session does not exist.
     */
    public void setPlaybackTime(String browserSessionKey, long time)
            throws IllegalArgumentException, IllegalStateException {

        final DashboardBrowserWebSession dashSession = getDashboardSession(browserSessionKey);

        LogFilePlaybackService playbackService = dashSession.getActivePlaybackService();
        LogMetadata log = dashSession.getLogMetadata();
        if (playbackService == null || log == null) {
            throw new IllegalStateException(
                    "The playback service or log metadata does not exist for this browser session key '"
                            + browserSessionKey + "'");
        } else if (time < log.getStartTime() || time > log.getEndTime()) {
            /* Seek time is out of bounds */
            final String errorMsg = time
                    + (time < log.getStartTime() ? " < " + log.getStartTime() : " > " + log.getEndTime());
            throw new IllegalArgumentException("The seek time is out of bounds. " + errorMsg);
        }

        playbackService.seek(time);
        if (!playbackService.isPlaying()) {
            /* If seeking while paused, re-call stopPlayback to trigger the
             * entity state heartbeats. This can happen when a playback reaches
             * the end of a session, and then is seeked back while paused. */
            stopPlayback(browserSessionKey);
        }
    }

    /**
     * Starts the knowledge session playback being run by the browser session with the given key
     *
     * @param browserSessionKey the key of the browser session being used to play back a knowledge session. Cannot be null.
     */
    public void startPlayback(String browserSessionKey) {
        final DashboardBrowserWebSession dashSession = getDashboardSession(browserSessionKey);

        LogFilePlaybackService playbackService = dashSession.getActivePlaybackService();
        if (playbackService != null) {
            /* Start playback and respect any previous message delay from
             * pausing */
            playbackService.startPlayback(false);
        }

        final LogMetadata logMetadata = dashSession.getLogMetadata();
        if (logMetadata != null) {
            final SessionMember hostSessionMember = logMetadata.getSession().getHostSessionMember();
            final int dsId = hostSessionMember.getDomainSessionId();
            final DomainSessionKey dsk = new DomainSessionKey(logMetadata.getSession());
            final DomainSessionEntityFilter dsef = SessionEntityFilter.getInstance(browserSessionKey).getDomainSessionFilter(dsk);

            if (dsef != null) {
                dsef.stopHeartbeatService();
            } else if (logger.isWarnEnabled()) {
                logger.warn(
                        "Unable to stop the heart beat service for domain session {}'s filter because it doesn't exist.",
                        dsId);
            }
        }
    }

    /**
     * Stops the knowledge session playback being run by the browser session with the given key
     *
     * @param browserSessionKey the key of the browser session being used to play back a knowledge session. Cannot be null.
     */
    public void stopPlayback(String browserSessionKey) {
        final DashboardBrowserWebSession dashSession = getDashboardSession(browserSessionKey);

        LogFilePlaybackService playbackService = dashSession.getActivePlaybackService();
        if (playbackService != null) {
            playbackService.pausePlayback();
        }

        final LogMetadata logMetadata = dashSession.getLogMetadata();
        if (logMetadata != null) {
            AbstractKnowledgeSession ks = logMetadata.getSession();
            final SessionMember hostSessionMember = ks.getHostSessionMember();
            final int dsId = hostSessionMember.getDomainSessionId();

            DomainSessionEntityFilter dsef = SessionEntityFilter.getInstance(browserSessionKey)
                    .getDomainSessionFilter(new DomainSessionKey(ks));
            if (dsef != null) {
                dsef.startHeartbeatService();
            } else {
                logger.warn("Unable to start the filter heartbeat service for domain session for domain session " + dsId
                        + " because it doesn't exist.");
            }
        }
    }

    /**
     * Sets the time of the knowledge session playback being run by the browser
     * session with the given key to when the provided task or concept is
     * activated.
     *
     * @param browserSessionKey the key of the browser session being used to
     *        play back a knowledge session. Cannot be null.
     * @param taskConceptName the name of the task or concept to use when
     *        finding the activation start time. Can't be null or blank.
     * @return the activation start time.
     */
    public Long jumpToActivationStart(String browserSessionKey, String taskConceptName) {
        final DashboardBrowserWebSession dashSession = getDashboardSession(browserSessionKey);
        if (dashSession == null) {
            return null;
        }

        LogMetadata metadata = dashSession.getLogMetadata();
        if (metadata == null) {
            return null;
        }

        return dashSession.getActivePlaybackService().jumpToActivationStart(taskConceptName);
    }

    /**
     * Update the team org filter state (selected or not selected for each team
     * and team member).
     *
     * @param browserSessionKey the key of the browser session being used to
     *        monitor the knowledge session that these entities belong to.
     * @param domainSessionId the id of the domain session being updated.
     * @param teamRolesSelected the collection of team roles (team and team
     *        members) and whether or not they have been selected to be shown.
     */
    public void updateSessionTeamOrgFilterState(String browserSessionKey, int domainSessionId, Map<String, Boolean> teamRolesSelected) {
        DashboardBrowserWebSession dashSession = getDashboardSession(browserSessionKey);

        final SessionEntityFilter sef = SessionEntityFilter.getInstance(browserSessionKey);
        DomainSessionKey key = new DomainSessionKey(dashSession.getKnowledgeSession());
        final DomainSessionEntityFilter dsef = sef.getDomainSessionFilter(key);

        if (dsef != null) {
            dsef.updateDomainSessionFilter(teamRolesSelected);
        } else if (logger.isWarnEnabled()) {
            logger.warn(
                    "Unable to update the entity selection for domain session {}'s filter because it doesn't exist.",
                    domainSessionId);
        }
    }

    /**
     * Sets the application that monitored game state information should be shared with with when playing
     * back a session
     *
     * @param browserSessionKey the key of the browser session making the request. Cannot be null.
     * @param gatewayConnection the gateway connection to share entity state information with. If not null, the connections
     * will be established as needed. If different than the last gateway connection, appropriate existing connections will be closed.
     * @throws Exception if a problem occurs while connecting to or disconnecting from the application
     */
    public void setGatewayConnections(String browserSessionKey, GatewayConnection gatewayConnection)
            throws Exception {

        final DashboardBrowserWebSession dashSession = getDashboardSession(browserSessionKey);

        //determine which user is making this request
        BrowserWebSession browserSession = userSessionManager.getBrowserSession(browserSessionKey);
        if(browserSession == null) {
            return;
        }

        UserWebSession userSession = userSessionManager.getUserSession(browserSession.getUserSessionKey());
        if(userSession == null) {
            return;
        }

        //connect to the application using the user's credentials
        WebMonitorModule.getInstance().setGatewayConnections(gatewayConnection, userSession.getUserSessionInfo().getUserName());

        /* If this browser session is already monitoring a knowledge session,
         * inform the external application about the session. */
        AbstractKnowledgeSession knowledgeSession = dashSession.getKnowledgeSession();
        if (knowledgeSession != null) {
            final KnowledgeSessionCreated ksCreated = new KnowledgeSessionCreated(knowledgeSession);
            WebMonitorModule.getInstance().sendGameStateMessageToGateway(ksCreated,
                    MessageTypeEnum.KNOWLEDGE_SESSION_CREATED);
        }
    }

    /**
     * Gets the gateway connections that GIFT is currently sharing knowledge session data with
     * to monitor said data outside of GIFT
     *
     * @return the gateway connections that data is being shared with. Can be null, if no gateway connection is connected.
     */
    public GatewayConnection getGatewayConnection() {
        return WebMonitorModule.getInstance().getGatewayConnection();
    }

    /**
     * Sets the configuration that external monitor applications should use when
     * they are connected and sharing data
     *
     * @param config the configuration to use. Cannot be null.
     */
    public void setExternalMonitorConfig(ExternalMonitorConfig config) {
        WebMonitorModule.getInstance().setExternalMonitorConfig(config);
    }

    /**
     * Gets the SIDC for the military symbol that should be used to represent training application entities with the given
     * role within the knowledge session with the given domain session ID. This can be used to determine what symbol should
     * be used to represent team roles that haven't been drawn yet due to being filtered out by the session's entity filter.
     *
     * @param browserSessionKey the key of the browser session requesting the SIDC
     * @param roleName the name of the team role that a SIDC is being requested for
     * @return  the SIDC corresponding to the given team role. Can be null if no entity data has been processed
     * for an entity with the given team role or one of its child roles.
     */
    public SIDC getSidcForRole(String browserSessionKey, String roleName){
        final DashboardBrowserWebSession dashSession = getDashboardSession(browserSessionKey);

        /* Build the key for the domain knowledge session */
        final DomainSessionKey key = new DomainSessionKey(dashSession.getKnowledgeSession());

        /* Get the SIDC from the entity filter */
        final SessionEntityFilter sef = SessionEntityFilter.getInstance(browserSessionKey);
        DomainSessionEntityFilter dsef = sef.getDomainSessionFilter(key);
        return dsef.getLatestRoleSidc(roleName);
    }

    /**
     * Deregisters the specified browser session from the knowledge session it
     * is listening to (if any).
     *
     * @param browserSessionKey the browser session key to remove from the
     *        session map.
     */
    public void cleanupSession(String browserSessionKey) {
        final DashboardBrowserWebSession dashSession = getDashboardSession(browserSessionKey);

        final AbstractKnowledgeSession ks = dashSession.setKnowledgeSession(null);
        if (ks != null) {
            /* Determine which deregister to call */
            if (ks.inPastSessionMode()) {
                deregisterBrowserPlayback(browserSessionKey);
            } else {
                deregisterBrowser(browserSessionKey, ks);
            }
        }
    }

    /**
     * Gets the {@link DashboardBrowserWebSession} that is identified by the
     * provided browser session key.
     *
     * @param browserSessionKey The {@link String} value of the browser session
     *        key for the browser to fetch. Can't be blank.
     * @return The {@link DashboardBrowserWebSession} associated with the
     *         provided browser session key. Can't be null.
     * @throws IllegalArgumentException if the browser session key is blank or
     *         if there is no {@link DashboardBrowserWebSession} associated with
     *         the provided key value.
     */
    private DashboardBrowserWebSession getDashboardSession(String browserSessionKey) {
        return getDashboardSession(browserSessionKey, true);
    }

    /**
     * Gets the {@link DashboardBrowserWebSession} that is identified by the
     * provided browser session key.
     *
     * @param browserSessionKey The {@link String} value of the browser session
     *        key for the browser to fetch. Can't be blank.
     * @param shouldThrow A flag indicating whether the browser session not
     *        being found should result in a thrown exception or a null return
     *        value. True causes an exception to be thrown, false causes null to
     *        be returned.
     * @return The {@link DashboardBrowserWebSession} associated with the
     *         provided browser session key. Can't be null.
     * @throws IllegalArgumentException if shouldThrow is true and if the
     *         browser session key is blank or if there is no
     *         {@link DashboardBrowserWebSession} associated with the provided
     *         key value.
     */
    private DashboardBrowserWebSession getDashboardSession(String browserSessionKey, boolean shouldThrow) {
        if (isBlank(browserSessionKey)) {
            throw new IllegalArgumentException("The parameter 'browserSessionKey' cannot be blank.");
        }

        final BrowserWebSession browserSession = userSessionManager.getBrowserSession(browserSessionKey);
        if (browserSession instanceof DashboardBrowserWebSession) {
            return (DashboardBrowserWebSession) browserSession;
        } else if (shouldThrow) {
            throw new IllegalArgumentException("There is no browser session with the key " + browserSessionKey);
        } else {
            return null;
        }
    }

    /**
     * Creates a patch for the changes made in the given
     * {@link PerformanceStateAttribute}.
     * 
     * @param browserSessionKey The {@link String} value of the browser session
     *        key for the browser to fetch. Can't be blank.
     * @param username The username of the person making the edits.
     * @param timestamp the timestamp of the edit.
     * @param performanceState the performance state that contains the edits
     *        made.
     * @return the patch file name if created; null otherwise.
     */
    public GenericRpcResponse<String> editLogPatchForPerformanceStateAttribute(String browserSessionKey, String username, long timestamp,
            PerformanceStateAttribute performanceState) {
        if (isBlank(browserSessionKey)) {
            throw new IllegalArgumentException("The parameter 'browserSessionKey' cannot be blank.");
        }

        final DashboardBrowserWebSession dashSession = getDashboardSession(browserSessionKey);
        if (dashSession == null || dashSession.getLogMetadata() == null) {
            return null;
        }

        final LogFilePlaybackService playbackService = dashSession.getActivePlaybackService();
        if (playbackService != null) {
            ApplyPatchResult results = playbackService.editLogPatchForPerformanceStateAttribute(timestamp,
                    performanceState);
            
            /* Save the patches to a log patch file */
            String patchFileName = writePatchesToLog(username, dashSession);
            
            final DbServicesInterface dbServices = ServicesManager.getInstance().getDbServices();
            final SessionMember hostSessionMember = dashSession.getKnowledgeSession().getHostSessionMember();

            if (!results.getAffectedMessages().isEmpty()) {
                for (MessageManager affectedMsg : results.getAffectedMessages()) {
                    if (affectedMsg.getMessage().getMessageType() != MessageTypeEnum.LEARNER_STATE) {
                        continue;
                    }

                    if (!affectedMsg.isPatched()) {
                        /* TODO #4759: How do we want to handle when a message
                         * patch was removed or reverted? */
                        continue;
                    }

                    try {
                        DomainSession domainSession = new DomainSession(
                                hostSessionMember.getDomainSessionId(), 
                                hostSessionMember.getUserSession().getUserId(), 
                                dashSession.getKnowledgeSession().getCourseRuntimeId(), 
                                dashSession.getKnowledgeSession().getCourseSourceId());
                        
                        dbServices.pastSessionLearnerStateUpdated(dashSession.getKnowledgeSession(),
                                domainSession,
                                (LearnerState) affectedMsg.getMessage().getPayload(),
                                (LearnerState) affectedMsg.getPreviousPatchedMessage().getPayload());
                    } catch (Throwable e) {
                        
                        logger.error("Unable to send the edited evaluator update because: ", e);
                        
                        /* Attempt to include the LRS name in the reported error if possible */
                        String firstLrsName = "the connected LRS";
                        List<String> lrsNames = ServicesManager.getInstance().getDbServices().getLrsNames();
                        if(!lrsNames.isEmpty()) {
                            firstLrsName = "<b/>" + lrsNames.get(0) + "</b>";
                        }
                        
                        /* Report a helpful error to the user informing them that the score could not
                         * be published to a LRS */
                        return  new FailureResponse<>(patchFileName, new DetailedException(
                                "The assessment has been saved to the session but could not be published to a Learner Record Store (LRS)", 
                                "A problem occurred while saving the new assessment to " + firstLrsName +". The new assessment has been "
                                + "saved to this session's local patch and will display in Game Master, but it will not "
                                + "be viewable in the connected LRS.<br/><br/>"
                                + "Please contact your LRS administrator and provide the below error.", 
                                e));
                    }
                }
            }
            
            return new SuccessfulResponse<String>(patchFileName);
            
        } else {
            return new SuccessfulResponse<String>(null);
        }
    }

    /**
     * Creates a patch for the changes made in the given
     * {@link EvaluatorUpdateRequest}.
     * 
     * @param browserSessionKey The {@link String} value of the browser session
     *        key for the browser to fetch. Can't be blank.
     * @param username The username of the person making the edits.
     * @param timestamp the timestamp of the edit.
     * @param updateEntireSpan true to apply the patch to the entire message
     *        span; false to apply it starting at the provided timestamp (if no
     *        message exists, one will be created).
     * @param evaluatorUpdateRequest the evaluator update that contains the
     *        edits made.
     * @param applyToFutureStates whether the update request should be applied to
     *        future learner states as well.
     * @return the patch file name if created; null otherwise.
     */
    public GenericRpcResponse<String> createLogPatchForEvaluatorUpdate(String browserSessionKey, String username, long timestamp,
            boolean updateEntireSpan, EvaluatorUpdateRequest evaluatorUpdateRequest, boolean applyToFutureStates) {
        if (isBlank(browserSessionKey)) {
            throw new IllegalArgumentException("The parameter 'browserSessionKey' cannot be blank.");
        }

        final DashboardBrowserWebSession dashSession = getDashboardSession(browserSessionKey);
        if (dashSession == null || dashSession.getLogMetadata() == null) {
            return null;
        }

        
        final LogFilePlaybackService playbackService = dashSession.getActivePlaybackService();
        if(playbackService != null){
            final List<ApplyPatchResult> resultList = playbackService.updateMessagesForEvaluatorUpdate(timestamp, updateEntireSpan,
                    evaluatorUpdateRequest, applyToFutureStates);
            
            /* Save the patches to a log patch file */
            String patchFileName = writePatchesToLog(username, dashSession);
            
            final DbServicesInterface dbServices = ServicesManager.getInstance().getDbServices();
            final SessionMember hostSessionMember = dashSession.getKnowledgeSession().getHostSessionMember();
            
            for(final ApplyPatchResult results : resultList) {
    
                if (!results.getAffectedMessages().isEmpty()) {
                    
                    // retrieve the username of the host of the knowledge session
                    // #4759 - the user session username was not being set on the legacy JSON decode logic and was added late to the Proto decode logic
                    //         so have to try and get the username from the session membership which was more reliable.
                    String hostUsername = hostSessionMember.getUserSession().getUsername();
                    if(hostUsername == null){
                        hostUsername = hostSessionMember.getSessionMembership().getUsername();                        
                    }
                    
                    for (MessageManager affectedMsg : results.getAffectedMessages()) {
                        
                        if (affectedMsg.getMessage().getMessageType() != MessageTypeEnum.LEARNER_STATE) {
                            continue;
                        }
    
                        if (!affectedMsg.isPatched()) {
                            /* TODO #4759: How do we want to handle when a message
                             * patch was removed or reverted? */
                            continue;
                        }
    
                        try {
                            DomainSession domainSession = new DomainSession(
                                    hostSessionMember.getDomainSessionId(), 
                                    hostSessionMember.getUserSession().getUserId(), 
                                    dashSession.getKnowledgeSession().getCourseRuntimeId(), 
                                    dashSession.getKnowledgeSession().getCourseSourceId());
                            
                            dbServices.pastSessionLearnerStateUpdated(dashSession.getKnowledgeSession(),
                                    domainSession,
                                    (LearnerState) affectedMsg.getMessage().getPayload(),
                                    (LearnerState) affectedMsg.getPreviousPatchedMessage().getPayload());
                            
                        } catch (Throwable e) {
                            
                            logger.error("Unable to send the published assessment because: ", e);
                            
                            /* Attempt to include the LRS name in the reported error if possible */
                            String firstLrsName = "the connected LRS";
                            List<String> lrsNames = ServicesManager.getInstance().getDbServices().getLrsNames();
                            if(!lrsNames.isEmpty()) {
                                firstLrsName = "<b/>" + lrsNames.get(0) + "</b>";
                            }
                            
                            /* Report a helpful error to the user informing them that the score could not
                             * be published to a LRS */
                            return  new FailureResponse<>(patchFileName, new DetailedException(
                                    "The assessment has been saved to the session but could not be published to a Learner Record Store (LRS)", 
                                    "A problem occurred while saving the new assessment to " + firstLrsName +". The new assessment has been "
                                    + "saved to this session's local patch and will display in Game Master, but it will not "
                                    + "be viewable in the connected LRS.<br/><br/>"
                                    + "Please contact your LRS administrator and provide the below error.", 
                                    e));
                        }
                    }
                }
            }

            return new SuccessfulResponse<String>(patchFileName);
                
        } else {
            return new SuccessfulResponse<String>(null);
        }
    }

    /**
     * Creates a patch for the deletion of a {@link PerformanceStateAttribute}.
     * 
     * @param browserSessionKey The {@link String} value of the browser session
     *        key for the browser to fetch. Can't be blank.
     * @param username The username of the person making the edits.
     * @param timestamp the timestamp of the deletion.
     * @param performanceState the performance state that is being deleted.
     * @return the patch file name if created; null otherwise.
     */
    public String removeLogPatchForAttribute(String browserSessionKey, String username, long timestamp,
            PerformanceStateAttribute performanceState) {
        if (isBlank(browserSessionKey)) {
            throw new IllegalArgumentException("The parameter 'browserSessionKey' cannot be blank.");
        }

        final DashboardBrowserWebSession dashSession = getDashboardSession(browserSessionKey);
        if (dashSession == null || dashSession.getLogMetadata() == null) {
            return null;
        }

        final LogFilePlaybackService playbackService = dashSession.getActivePlaybackService();
        if(playbackService != null){
            playbackService.removeLogPatchForAttribute(timestamp, performanceState);
            
            /* TODO #4759: How do we want to handle when a message patch was
             * removed or reverted? */

            return writePatchesToLog(username, dashSession);
        }else{
            return null;
        }
    }

    /**
     * Delete the session log patch.
     *
     * @param browserSessionKey The unique identifier of the browser making the
     *        request. Can't be blank.
     */
    void deleteSessionLogPatch(String browserSessionKey) {
        if (isBlank(browserSessionKey)) {
            throw new IllegalArgumentException("The parameter 'browserSessionKey' cannot be blank.");
        }

        final DashboardBrowserWebSession dashSession = getDashboardSession(browserSessionKey);
        if (dashSession == null || dashSession.getLogMetadata() == null) {
            return;
        }

        final LogFilePlaybackService playbackService = dashSession.getActivePlaybackService();
        if(playbackService != null){
            playbackService.deleteLogPatchFile(dashSession.getLogMetadata());
            /* TODO #4759: How do we want to handle when a message patch was
             * removed or reverted? */
        }
    }
    
    /**
     * Gets scenario information about the knowledge session being played back
     * 
     * @param browserSessionKey The unique identifier of the browser making the
     *        request. Can't be blank.
     * @param username the username of the user associated with the browser session. Cannot be null.
     * @return scenario information about the knowledge session. Can be null.
     */
    public SessionScenarioInfo getKnowledgeSessionScenario(String browserSessionKey, String username) {
        if (isBlank(browserSessionKey)) {
            throw new IllegalArgumentException("The parameter 'browserSessionKey' cannot be blank.");
}

        final DashboardBrowserWebSession dashSession = getDashboardSession(browserSessionKey);
        if (dashSession == null || dashSession.getLogMetadata() == null) {
            return null;
        }
        
        final LogFilePlaybackService playbackService = dashSession.getActivePlaybackService();
        if(playbackService != null){
            return playbackService.getKnowledgeSessionScenario();
        }
        
        return null;
    }
    /**
     * Applies the given condition assessments as a patch to the knowledge session playback for the given
     * browser session and publishes the calculated score
     * 
     * @param browserSessionKey the client session requesting this information. Cannot be null.
     * @param username the user name of the user making the request. Cannot be null.
     * @param timestamp the timestamp during the knowledge session at which this assessment was provided.
     * @param conceptToConditionAssessments a mapping from each concept ID to the assessments that were
     * provided to that concept's conditions by the observer controller. Cannot be null.
     * @param courseConcepts the course concepts. Cannot be null.
     * @return the patch file name if created; null otherwise.
     */
    public GenericRpcResponse<String> publishKnowledgeSessionOverallAssessments(String browserSessionKey, String username, long timestamp, 
            Map<Integer, List<ScoreNodeUpdate>> conceptToConditionAssessments, List<String> courseConcepts) {
        
        if (isBlank(browserSessionKey)) {
            throw new IllegalArgumentException("The parameter 'browserSessionKey' cannot be blank.");
        }

        final DashboardBrowserWebSession dashSession = getDashboardSession(browserSessionKey);
        if (dashSession == null || dashSession.getLogMetadata() == null) {
            return null;
        }

        final LogFilePlaybackService playbackService = dashSession.getActivePlaybackService();
        if(playbackService != null){
            
            /* Apply the given OC condition assessments to generate a patch */
            ApplyPatchResult result = playbackService.updateMessagesForOverallAssessments(timestamp, conceptToConditionAssessments, courseConcepts);
            
            /* Save the patches to a log patch file */
            String patchFileName = writePatchesToLog(username, dashSession);
            
            final DbServicesInterface dbServices = ServicesManager.getInstance().getDbServices();
            final SessionMember hostSessionMember = dashSession.getKnowledgeSession().getHostSessionMember();
            
            AssessmentChainOfCustody chainOfCustody = new AssessmentChainOfCustody(
                    hostSessionMember.getUserSession().getUserId(), 
                    hostSessionMember.getDomainSessionId(), 
                    playbackService.getLogFileFolder(), 
                    playbackService.getDkfFileName(), 
                    playbackService.getLogFileName());
            
            for (MessageManager affectedMsg : result.getAffectedMessages()) {
                if (affectedMsg.getMessage().getMessageType() != MessageTypeEnum.PUBLISH_LESSON_SCORE_REQUEST) {
                    continue;
                }

                if (!affectedMsg.isPatched()) {
                    /* TODO #4759: How do we want to handle when a message
                     * patch was removed or reverted? */
                    continue;
                }

                try {
                    /* For each patched message, publish a new course record with the calculated score */
                    PublishLessonScore newScore = (PublishLessonScore) affectedMsg.getMessage().getPayload();
                    PublishLessonScore oldScore = (PublishLessonScore) affectedMsg.getPreviousPatchedMessage().getPayload();
                    Concepts.Hierarchy concepts = ((PublishLessonScore)affectedMsg.getOriginalMessage().getPayload()).getConcepts();
                    
                    dbServices.pastSessionCourseRecordUpdated(
                            dashSession.getKnowledgeSession(),
                            chainOfCustody,
                            newScore.getCourseData(),
                            oldScore.getCourseData(),
                            concepts);
                } catch (Throwable e) {
                    
                    logger.error("Unable to send the published assessments because: ", e);
                    
                    /* Attempt to include the LRS name in the reported error if possible */
                    String firstLrsName = "the connected LRS";
                    List<String> lrsNames = ServicesManager.getInstance().getDbServices().getLrsNames();
                    if(!lrsNames.isEmpty()) {
                        firstLrsName = "<b/>" + lrsNames.get(0) + "</b>";
                    }
                    
                    /* Report a helpful error to the user informing them that the score could not
                     * be published to a LRS */
                    return  new FailureResponse<>(patchFileName, new DetailedException(
                            "The new score has been saved to the session but could not be published to a Learner Record Store (LRS)", 
                            "A problem occurred while saving the new score to " + firstLrsName +". The new score has been "
                            + "saved to this session's local patch and will display in Game Master, but it will not "
                            + "be viewable in the connected LRS.<br/><br/>"
                            + "Please contact your LRS administrator and provide the below error.", 
                            e));
                }
            }
            
            return new SuccessfulResponse<String>(patchFileName);
            
        } else {
            return new SuccessfulResponse<String>(null);
        }
    }
    

    /**
     * Writes the message patches that are stored in-memory for the current knowledge session playback
     * to the patch file associated with the session. If the patch file name was changed (such as if it was upgraded
     * from a legacy JSON patch to a newer protobuf patch), then its record in the log index will also 
     * be updated so that it is properly loaded going forward.
     * 
     * @param username the username of the user performing the write. Cannot be null.
     * @param dashSession the dashboard session that has registered the log playback that the patches
     * should be written to
     * @return the name of the patch file that the patches were saved to. This value will only change if
     * the log had a legacy JSON patch file that was updated to protobuf. Will not be null.
     */
    private String writePatchesToLog(String username, DashboardBrowserWebSession dashSession) {
        
        LogFilePlaybackService playbackService = dashSession.getActivePlaybackService();
        LogMetadata metadata = dashSession.getLogMetadata();
        
        String patchFileName = playbackService.writePatchMessages(username);
        String oldPatchFileName = metadata.getLogPatchFile();
        
        metadata.setLogPatchFile(patchFileName);
        
        if(oldPatchFileName != null && !oldPatchFileName.equals(patchFileName)) {
            try {
                /* The existing log file was replaced with a newer one (such as when converting a legacy
                 * JSON patch to Protobuf), so we need to update the name of the patch file in the log index */
                LogIndexService.getInstance().updateLogMetadata(metadata, null);
                
            } catch (IOException e) {
                logger.error("Unable to update log index for an updated patch file. This may cause the UI to "
                        + "reflect the old patch file until the log index is updated.", e);
}
        }
        
        return patchFileName; 
    }
    

    /**
     * Calculates the assessment levels for the parent performance nodes of the given condition assessments
     * 
     * @param browserSessionKey the client session requesting this information. Cannot be null.
     * @param username the user name of the user making the request. Cannot be null.
     * @param conceptToConditionAssessments a mapping from each leaf concept to the assessment levels that were assigned
     * to each of its conditions by the observer controller. Cannot be null.
     * @return the calculated assessment levels of all the parent nodes. Cannot be null.
     */
    public GenericRpcResponse<Map<Integer, AssessmentLevelEnum>> calculateRollUp(String browserSessionKey,
            String username, Map<Integer, List<ScoreNodeUpdate>> conceptToConditionAssessments) {
        
        if (isBlank(browserSessionKey)) {
            throw new IllegalArgumentException("The parameter 'browserSessionKey' cannot be blank.");
}

        final DashboardBrowserWebSession dashSession = getDashboardSession(browserSessionKey);
        if (dashSession == null || dashSession.getLogMetadata() == null) {
            return null;
        }

        final LogFilePlaybackService playbackService = dashSession.getActivePlaybackService();
        if(playbackService != null){
            
            /* Apply the given OC condition assessments to generate a patch */
            Map<Integer, AssessmentLevelEnum> calculatedAssessments = playbackService.calculateRollUp(conceptToConditionAssessments);
            
            return new SuccessfulResponse<>(calculatedAssessments);
            
        } else {
            return new SuccessfulResponse<>(null);
        }
    }
    
}
