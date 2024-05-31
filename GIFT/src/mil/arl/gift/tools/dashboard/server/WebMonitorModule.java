/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.DomainSessionList;
import mil.arl.gift.common.EvaluatorUpdateRequest;
import mil.arl.gift.common.FinishScenario;
import mil.arl.gift.common.InitializeInteropConnections;
import mil.arl.gift.common.LearnerTutorAction;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.UserSessionList;
import mil.arl.gift.common.course.TrainingAppUtil;
import mil.arl.gift.common.course.dkf.session.KnowledgeSessionsReply;
import mil.arl.gift.common.course.dkf.session.KnowledgeSessionsRequest;
import mil.arl.gift.common.course.strategy.ApplyStrategies;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.ModuleStateEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.module.AbstractModule;
import mil.arl.gift.common.module.GatewayModuleStatus;
import mil.arl.gift.common.module.ModuleStatus;
import mil.arl.gift.common.module.ModuleStatusListener;
import mil.arl.gift.common.module.ModuleStatusMonitor;
import mil.arl.gift.common.module.ModuleStatusMonitor.StatusReceivedInfo;
import mil.arl.gift.common.module.WebMonitorModuleStatus;
import mil.arl.gift.common.ta.util.ExternalMonitorConfig;
import mil.arl.gift.net.api.MessageCollectionCallback;
import mil.arl.gift.net.api.SubjectUtil;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.DomainSessionMessageInterface;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.NACK;
import mil.arl.gift.net.api.message.UserSessionMessage;
import mil.arl.gift.tools.dashboard.server.gamemaster.DomainInfoCacheData.DomainSessionKey;
import mil.arl.gift.tools.dashboard.server.gamemaster.DomainInformationCache;
import mil.arl.gift.tools.dashboard.server.webmonitor.message.MonitorMessageListener;
import mil.arl.gift.tools.dashboard.shared.messages.EndSessionRequest;
import mil.arl.gift.tools.dashboard.shared.messages.GatewayConnection;
import mil.arl.gift.tools.monitor.DomainSessionStatusListener;
import mil.arl.gift.tools.monitor.DomainSessionStatusModel;
import mil.arl.gift.tools.monitor.UserStatusListener;
import mil.arl.gift.tools.monitor.UserStatusModel;

/**
 * A web based version of the monitor application.  This currently lives in dashboard, but
 * could later be abstracted in case other areas would want a different version/flavor of a
 * web based monitor.
 *
 * @author nblomberg
 *
 */
public class WebMonitorModule extends AbstractModule {

    /** The logger for the class */
    private static Logger logger = LoggerFactory.getLogger(WebMonitorModule.class);

    /** Default name of the web monitor module */
    private static final String DEFAULT_MODULE_NAME  = "WebMonitorModule";

    /** Name of the web monitor queue. */
    private static final String WEBMONITOR_QUEUE_PREFIX  = "WebMonitor_Queue";

    /** The name of the queue from which this module receives messages. */
    private static final String WEBMONITOR_QUEUE_NAME = WEBMONITOR_QUEUE_PREFIX + ":" + ipaddr;

    /** Singleton instance of the web monitor module. */
    private static WebMonitorModule instance = null;

    /** The {@link WebMonitorModuleStatus} for this {@link WebMonitorModule} */
    private WebMonitorModuleStatus webMonitorStatus;

    /** Instance of the knowledge session message manager. */
    private KnowledgeSessionMessageManager ksmManager = null;

    /** Listens for changes in module status */
    @SuppressWarnings("unused")
    private WebMonitor monitor;

    /** Mapping of queues associated with the module. */
    private Map<String, HashSet<String>> moduleQueues = new HashMap<>();

    /**
     * The list of message types which should be processed from the monitor
     * topic.
     *   Learner state - Used to show assessments
     *   Initialize ped model - Used to build strategy lists in scenario injects panel and timeline
     *   Knowledge session created - 
     *   Lesson started - Used to detect when a knowledge session starts and initialize it's cache data
     *   Lesson completed - Used to detect when a knowledge session ends and to stop interop connections when Game Master is connected to Gateway
     *   Geolocation - Used to detect GPS location of mobile app users and draw on map
     *   Entity state - Used to detect DIS entities and draw on map
     *   Detonation -  Used to detect DIS detonations and draw on map
     *   Weapon fire - Used to detect DIS weapon fire and draw on map
     *   Close domain session request - Used to detect if knowledge session ended prematurely
     *   Publish lesson score request - Used to get existing scores for summative assessment dialog and to update scores 
     */
    protected static final Set<MessageTypeEnum> WHITE_LISTED_TOPIC_MESSAGE_TYPES = Stream.of(
            MessageTypeEnum.LEARNER_STATE,
            MessageTypeEnum.INITIALIZE_PEDAGOGICAL_MODEL_REQUEST,
            MessageTypeEnum.KNOWLEDGE_SESSION_CREATED,
            MessageTypeEnum.LESSON_STARTED,
            MessageTypeEnum.LESSON_COMPLETED,
            MessageTypeEnum.GEOLOCATION,
            MessageTypeEnum.ENTITY_STATE,
            MessageTypeEnum.DETONATION,
            MessageTypeEnum.WEAPON_FIRE,
            MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST,
            MessageTypeEnum.PUBLISH_LESSON_SCORE_REQUEST
    ).collect(Collectors.toSet());

    /** map of module type (domain, gateway, etc) to module status */
    private Map<ModuleTypeEnum, ModuleStateEnum> moduleStatusMap = new HashMap<>();

    /** Caches domain information from individual messages */
    private final DomainInformationCache domainInfoCache = new DomainInformationCache();

    /** The gateway connections that should be used to visualize game state */
    private GatewayConnection gatewayConnection = null;

    /** The manager handling the existing network connection to the Gateway module */
    private GatewayManager gateway = null;

    /** The configuration that external monitor applications should use when they are connected and sharing data */
    private ExternalMonitorConfig externalMonitorConfig = new ExternalMonitorConfig();
    
    /** The state of a user connected to the system */
    private UserStatusModel userStatusModel = new UserStatusModel();

    /** The state of an active domain session */
    private DomainSessionStatusModel domainSessionStatusModel = new DomainSessionStatusModel();
    
    /** map of domain module (unique address) to their active domain session ids */
    private final Map<String, List<Integer>> globalDModuleAddrToDSessionId = new HashMap<>();
    
    /** map of tutor module (unique address) to their active user session ids */
    private final Map<String, List<UserSession>> globalTModuleAddrToUSessionId = new HashMap<>();
    
    /** 
     * array used that indicates whether a timer used to delay the removal of a domain session from 
     * the monitor to allow the domain session to be closed first is active 
     */
    private ArrayList<Integer> domainSessionsPendingRemoval = new ArrayList<Integer>();
    
    /** Used to delay removal of the user sessions from the monitor.  This is so that messages that may
     * be incoming can still be received before the monitor closed out the user session.  This is similar
     * to the domain session removal logic.
     */
    private ArrayList<UserSession> userSessionsPendingRemoval = new ArrayList<UserSession>();
    
    /** 
     * Listeners that have registered to reveive message traffic from this module. Used primarily to allow
     * browser sessions to display messages for the web monitor
     */
    private final List<MonitorMessageListener> messageListeners = new ArrayList<MonitorMessageListener>();

    /**
     * Create the instance of the singleton.  This must be done once during
     * initialization of the application or before first needed.  This must
     * be called prior to calling getInstance().  This makes the singleton
     * initialization more explicit and easier to track when the singleton
     * is first created.
     *
     * @param ksmHandler instance of the knowledge session manager
     * @return Instance of the the web monitor module.
     */
    public static WebMonitorModule createInstance(KnowledgeSessionMessageManager ksmHandler) {
        if(logger.isDebugEnabled()){
            logger.debug("Creating WebMonitorModule instance.");
        }
        if (instance == null) {
            instance = new WebMonitorModule();
            instance.init(ksmHandler);
        } else {
            logger.error("Calling create instance, but there's already an existing instance.");
        }

        return instance;
    }

    /**
     * Gets the singleton instance of the RuntimeTool module
     *
     * @return RuntimeToolModule The singleton instance of the RuntimeTool module
     */
    public static WebMonitorModule getInstance() {

        if (instance == null) {

            logger.error("Singleton instance is null.  Please make sure to create the WebMonitorModule instance first.");
        }

        return instance;
    }

    /**
     * Default Constructor
     */
    private WebMonitorModule() {
        super(DEFAULT_MODULE_NAME, WEBMONITOR_QUEUE_NAME, WEBMONITOR_QUEUE_NAME + ":" + SubjectUtil.INBOX_QUEUE_SUFFIX, WebMonitorModuleProperties.getInstance());

    }

    @Override
    protected void init() {

        // Add listener for monitoring for module status changes.
        monitor = new WebMonitor();

        webMonitorStatus = new WebMonitorModuleStatus(moduleStatus);

        //for receiving all messages (on a particular GIFT network of modules)
        createSubjectTopicClient(SubjectUtil.MONITOR_TOPIC, this::processMonitorTopicMessage);

        createSubjectTopicClient(SubjectUtil.MONITOR_DISCOVERY_TOPIC, false);

        initializeHeartbeat();
    }

    /**
     * Processes a {@link Message} that was pulled from the monitor topic.
     *
     * @param message The {@link Message} to process. Can't be null.
     * @return Whether or not the message was processed successfully. Always
     *         true.
     */
    protected boolean processMonitorTopicMessage(Message message) {
        /* Let the domainInfoCache process any messages it needs to */
        boolean isDuplicate = domainInfoCache.processMessage(message);

        /* Determine if the message should be sent to Game Master. Catch any exceptions 
         * that may be thrown by Game Master to ensure Web Monitor continues receiving messages. */
        try {
            if (!isDuplicate) {
                if (WHITE_LISTED_TOPIC_MESSAGE_TYPES.contains(message.getMessageType())) {
                    
                    /* We only want to handle unique messages that adhere to the
                     * whitelist */
                    handleMessage(message);
                }
            }
            
        } catch(Exception e) {
            logger.error("Caught exception handling a Game Master message from the monitor topic", e);
        }
        
        /* Update the active session in Web Monitor */
        if (message instanceof UserSessionMessage) {
            handleAbstractUserSessionMessage((UserSessionMessage) message);
        }
        
        /* If the message is not forwarded to Game master in the above block attempt to handle
         * the message for all Web Monitor listeners */
        synchronized (messageListeners) {

            for (MonitorMessageListener i : messageListeners) {

                try {
                    i.handleMessage(message);
                } catch (Exception e) {
                    logger.error("Caught exception from misbehaving message listener", e);
                }
            }
        }

        return true;
    }

    public boolean processPlaybackMessage(Message message) {
        /* Let the domainInfoCache process any messages it needs to */
        boolean isDuplicate = domainInfoCache.processMessage(message);

        /* We only want to handle unique messages that adhere to the
         * whitelist */
        if (!isDuplicate) {
            final MessageTypeEnum messageType = message.getMessageType();
            final boolean isWhiteListed = WHITE_LISTED_TOPIC_MESSAGE_TYPES.contains(messageType);
            if (isWhiteListed && message instanceof DomainSessionMessageInterface) {
                ksmManager.handleMessage((DomainSessionMessageInterface) message, true);
            }
        }

        return true;
    }

    /**
     * Initialize the WebMonitorModule
     *
     * @param ksmManager - Instance of the knowledge session manager
     */
    public void init(KnowledgeSessionMessageManager ksmManager) {
        this.init();

        this.ksmManager = ksmManager;
    }

    @Override
    protected ModuleTypeEnum getModuleType() {
        return ModuleTypeEnum.MONITOR_MODULE;
    }

    @Override
    protected void handleMessage(Message message) {
        if (message instanceof DomainSessionMessageInterface) {
            ksmManager.handleMessage((DomainSessionMessageInterface) message, false);
        }
    }

    @Override
    public void sendModuleStatus() {
        sendMessage(SubjectUtil.MONITOR_DISCOVERY_TOPIC, webMonitorStatus, MessageTypeEnum.WEB_MONITOR_MODULE_STATUS, null);
    }

    /**
     * Send a kill module message to the module with the given address.
     *
     * @param queueName name of the module queue to connect too and send the message too
     * @param moduleType the type of module associated with the queue
     */
    public void killModule(final String queueName, ModuleTypeEnum moduleType) {

        if(logger.isInfoEnabled()){
            logger.info("Sending kill message to " + queueName + " of module type " + moduleType);
        }

        //since this is the only message the monitor sends to modules,
        // 1) create client
        // 2) send message
        // 3) remove client

        createSubjectQueueClient(queueName, moduleType, false);
        sendMessage(queueName, null, MessageTypeEnum.KILL_MODULE, null);
        removeClientConnection(queueName, false);
    }

    /**
     * Declares that this {@link WebMonitorModule} is attached to a domain
     * session with the given id.
     *
     * @param domainSessionId The id of the domain session to which this
     *        {@link WebMonitor} has been attached.
     */
    public void addAttachedDomainSession(int domainSessionId) {
        webMonitorStatus.getAttachedDomainSessions().add(domainSessionId);
    }

    /**
     * Declares that this {@link WebMonitorModule} is no longer attached to a
     * domain session with the given id.
     *
     * @param domainSessionKey The unique key that identifies the domain session
     *        from which this {@link WebMonitor} has detached.
     */
    public void removeAttachedDomainSession(DomainSessionKey domainSessionKey) {
        webMonitorStatus.getAttachedDomainSessions().remove(domainSessionKey.getDomainSessionId());
        removeClient(domainSessionKey);
    }

    /**
     * Ensures that a connection has been established with the given ActiveMQ
     * destination. If a connection to the destination already exists, no action
     * is taken.
     *
     * @param domainSessionKey The unique key identifying an id of the domain to
     *        for which the connection is being established.
     */
    public void establishClient(DomainSessionKey domainSessionKey) {
        String destination = domainInfoCache.getDomainModuleAddress(domainSessionKey);
        if (destination == null) {
            throw new IllegalArgumentException(
                    "The address of domain session '" + domainSessionKey + "' is not yet known.");
        }

        if (!domainInfoCache.isSessionConnected(domainSessionKey.getDomainSessionId())) {
            createSubjectQueueClient(destination, ModuleTypeEnum.DOMAIN_MODULE, false);
            domainInfoCache.setSessionConnected(domainSessionKey.getDomainSessionId(), true);
        }
    }

    /**
     * Removes that association between the domain session id and its
     * destination. Also closes the connection to the destination if there are
     * no longer any domain sessions being referenced there.
     *
     * @param domainSessionKey The unique key identifying the domain session
     *        that is no longer attached to.
     */
    private void removeClient(DomainSessionKey domainSessionKey) {
        /* Get the destination that this domain session is associated with */
        String destination = domainInfoCache.getDomainModuleAddress(domainSessionKey);
        if (destination == null) {
            throw new IllegalArgumentException("The address of domain session ID is not yet known.");
        }

        domainInfoCache.setSessionConnected(domainSessionKey.getDomainSessionId(), false);
        if (!domainInfoCache.isAddressedReferenced(destination)) {
            removeClientConnection(destination, false);
        }
    }

    /**
     * Request from the Domain the collection of active knowledge sessions.
     *
     * @param callback - used to notify of reply message to this message being
     *        sent
     */
    public void requestActiveKnowledgeSessionsFromDomain(final MessageCollectionCallback callback) {
        ModuleTypeEnum moduleType = ModuleTypeEnum.DOMAIN_MODULE;
        KnowledgeSessionsRequest payload = new KnowledgeSessionsRequest();
        MessageTypeEnum messageType = MessageTypeEnum.ACTIVE_KNOWLEDGE_SESSIONS_REQUEST;

        HashSet<String> queues = WebMonitorModule.getInstance().getModuleQueues().get(moduleType.getName());

        for (final String queue : queues) {
            createSubjectQueueClient(queue, moduleType, false);
            MessageCollectionCallback intermediateCb = callback == null ? null : new MessageCollectionCallback() {

            	private void removeClientIfNeeded(final String queue) {
					if (!domainInfoCache.isAddressedReferenced(queue)) {
					    
					    /* 
				         * Note: Removing a client MUST be handled in a separate thread to fix a bug found in #5228. 
				         * 
				         * If we try to remove this client while still inside a message collection that's connected
				         * to said client, this can cause a deadlock where one thread has a lock on the connectionListeners 
				         * in the Message client while another has a lock on the message collection map in NetworkSession. 
				         * This prevents the message decoding thread in the web monitor from processing further messages
				         * from the domain module, e
				         * 
				         * To avoid this, we handle removing the client in a separate thread and give it a slight delay to
				         * allow the thread handling the message collection to finish handling this callback first and remove
				         * any client listeners associated with it.
				         */
					    
					    Thread removeThread = new Thread("Remove Client " + queue) {
					      
					        @Override
                            public void run() {
					            
					            try {
                                    sleep(500);
                                } catch (InterruptedException e) {
                                    logger.warn("Interrupted while waiting to remove client " + queue, e);
                                    
                                } finally {
                                    removeClientConnection(queue, false);
                                }
					        }
					    };
						removeThread.start();
                    }
				}
            	
                @Override
                public void success() {
                    callback.success();
                    removeClientIfNeeded(queue);
                }

                @Override
                public void received(Message msg) {
                    final Object payload = msg.getPayload();
                    if (payload instanceof KnowledgeSessionsReply) {
                        /* Cache the reply */
                        KnowledgeSessionsReply reply = (KnowledgeSessionsReply) payload;
                        domainInfoCache.cacheKnowledgeSessions(reply.getKnowledgeSessionMap());
                    }

                    callback.received(msg);
                }

                @Override
                public void failure(Message msg) {
                    callback.failure(msg);
                    removeClientIfNeeded(queue);
                }

                @Override
                public void failure(String why) {
                    callback.failure(why);
                    removeClientIfNeeded(queue);
                }
            };

            sendMessage(moduleType, payload, messageType, intermediateCb);
        }
    }

    /**
     * Sends an {@link ApplyStrategies} payload to a domain session.
     *
     * @param applyStrategies The payload to send. Can't be null.
     * @param domainSessionKey The unique key identifying the domain session to
     *        which the payload should be sent.
     */
    public void sendApplyStrategies(ApplyStrategies applyStrategies, DomainSessionKey domainSessionKey) {
        establishClient(domainSessionKey);
        sendDomainSessionMessage(
                domainInfoCache.getDomainModuleAddress(domainSessionKey),
                applyStrategies,
                domainInfoCache.getUserSession(domainSessionKey),
                domainSessionKey.getDomainSessionId(),
                null,
                MessageTypeEnum.APPLY_STRATEGIES,
                null);
    }

    /**
     * Sends a request to the Domain Module to end the current domain knowledge
     * session.
     *
     * @param endSession Details/options related to ending the domain knowledge
     *        session.
     * @param domainSessionKey The unique key identifying the domain session for
     *        which to request the end of the domain knowledge session.
     */
    public void sendEndSessionRequest(EndSessionRequest endSession, DomainSessionKey domainSessionKey) {
        establishClient(domainSessionKey);
        sendDomainSessionMessage(
                domainInfoCache.getDomainModuleAddress(domainSessionKey),
                new LearnerTutorAction(new FinishScenario()),
                domainInfoCache.getUserSession(domainSessionKey),
                domainSessionKey.getDomainSessionId(),
                null,
                MessageTypeEnum.LEARNER_TUTOR_ACTION,
                null);
    }

    /**
     * Getter for this {@link WebMonitorModule}'s
     * {@link DomainInformationCache}.
     *
     * @return The {@link DomainInformationCache}, can't be null.
     */
    public DomainInformationCache getDomainInformationCache() {
        return domainInfoCache;
    }

    /**
     * Get the queues associated with this module.
     *
     * @return The map of queues associated with the module.
     */
    public Map<String, HashSet<String>> getModuleQueues() {
        return moduleQueues;
    }

    /**
     * Returns the module status map.  This map contains a mapping of each module
     * along with the status of the module.  This mapping ignores queues that are
     * used by each module.
     *
     * @return The mapping containing the module type to module state.
     */
    public Map<ModuleTypeEnum, ModuleStateEnum> getModuleStatusMap() {
        return moduleStatusMap;
    }

    /**
     * Sends an {@link EvaluatorUpdateRequest} payload to a domain session.
     *
     * @param request The payload to send. Can't be null.
     * @param domainSessionKey The unique key identifying the domain session to
     *        which the payload should be sent.
     */
    public void sendEvaluatorUpdateRequest(EvaluatorUpdateRequest request, DomainSessionKey domainSessionKey) {
        establishClient(domainSessionKey);
        sendDomainSessionMessage(
                domainInfoCache.getDomainModuleAddress(domainSessionKey),
                request,
                domainInfoCache.getUserSession(domainSessionKey), domainSessionKey.getDomainSessionId(),
                null,
                MessageTypeEnum.EVALUATOR_UPDATE_REQUEST,
                null);
    }

    /**
     * Sets the gateway connections that should be enabled to visualize game state information when playing
     * back a session
     *
     * @param newGatewayConn the gateway connections to share entity state information with. If not null, the connections
     * will be established as needed. If different than the last connection information, appropriate existing connection will be closed.
     * @param requestingObserver the name of the observer controller making the request
     * @throws Exception if a problem occurs while connecting to or disconnecting from one or more connections
     */
    public void setGatewayConnections(final GatewayConnection newGatewayConn, String requestingObserver) throws Exception {

        if(DeploymentModeEnum.SERVER.equals(DashboardProperties.getInstance().getDeploymentMode())) {
            return; //do not interact with the Gateway module in server mode
        }

        if(Objects.equals(gatewayConnection, newGatewayConn)) {
            return; //don't modify the connections until necessary
        }

        if(gateway == null || gateway.getAllocatedGateway() == null) {

            //establish a new network connection with the Gateway module if none exists yet
            GatewayManager newGateway = new GatewayManager();
            newGateway.start();

            gateway = newGateway;
        }

        if(newGatewayConn == null || !newGatewayConn.hasConnection()) {

            //set the external monitor app to null so that we stop sending messages to the gateway during the disconnect
            gatewayConnection = null;

            //disconnect interop connections if no training application is selected
            CompletableFuture<Void> asyncResult = new CompletableFuture<>();
            sendMessage(ModuleTypeEnum.GATEWAY_MODULE, null, MessageTypeEnum.LESSON_COMPLETED, new MessageCollectionCallback() {

                @Override
                public void success() {
                    asyncResult.complete(null);
                }

                @Override
                public void received(Message msg) {
                    //nothing to do
                }

                @Override
                public void failure(String why) {
                    asyncResult.completeExceptionally(new Exception(why));
                }

                @Override
                public void failure(Message msg) {

                    if(msg.getPayload() instanceof NACK) {
                        asyncResult.completeExceptionally(new Exception(((NACK) msg.getPayload()).getErrorMessage()));

                    } else {
                        asyncResult.completeExceptionally(new Exception("An unknown error was thrown"));
                    }
                }
            });

            asyncResult.get(30000, TimeUnit.MILLISECONDS);

        } else {
            
            //figure out what interop connections are needed for the selected training application
            List<String> interops = new ArrayList<>();
            for(TrainingApplicationEnum taType : newGatewayConn.getTaTypes()){                
                interops.addAll(TrainingAppUtil.trainingAppToInteropClassNames.get(taType));
            }

            if(newGatewayConn.shouldUseDIS() && !interops.contains(TrainingAppUtil.DIS_INTERFACE)) {
                interops.add(TrainingAppUtil.DIS_INTERFACE);
            }

            String domainContentServerAddress = DashboardProperties.getInstance().getDomainContentServerAddress();
            InitializeInteropConnections initInteropConnections = new InitializeInteropConnections(
                    domainContentServerAddress, interops, requestingObserver);
            initInteropConnections.setPlayback(true);

            //establish interop connections for the selected training application
            CompletableFuture<Void> asyncResult = new CompletableFuture<>();
            sendMessage(ModuleTypeEnum.GATEWAY_MODULE, initInteropConnections, MessageTypeEnum.INIT_INTEROP_CONNECTIONS, new MessageCollectionCallback() {

                @Override
                public void success() {
                    gatewayConnection = newGatewayConn;
                    asyncResult.complete(null);

                    //if the user has modified the monitor settings, make sure application knows about them
                    sendExternalMonitorConfigToGateway();
                }

                @Override
                public void received(Message msg) {
                    //nothing to do
                }

                @Override
                public void failure(String why) {
                    asyncResult.completeExceptionally(new Exception(why));
                }

                @Override
                public void failure(Message msg) {

                    if(msg.getPayload() instanceof NACK) {
                        asyncResult.completeExceptionally(new Exception(((NACK) msg.getPayload()).getErrorMessage()));

                    } else {
                        asyncResult.completeExceptionally(new Exception("An unknown error was thrown"));
                    }
                }
            });

            asyncResult.get(30000, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Sends the provided payload to the Gateway module so that it can be passed
     * to any connected training application. Note that this will only have an
     * effect if a connection to a training application was previously
     * established using
     * {@link #setGatewayConnections(GatewayConnection, String)}.
     *
     * @param payload the payload to send.
     * @param messageType the type of message that the payload refers to.
     */
    public void sendGameStateMessageToGateway(Object payload, MessageTypeEnum messageType) {
        if (payload == null) {
            throw new IllegalArgumentException("The parameter 'payload' cannot be null.");
        } else if (messageType == null) {
            throw new IllegalArgumentException("The parameter 'messageType' cannot be null.");
        }

        if(DeploymentModeEnum.SERVER.equals(DashboardProperties.getInstance().getDeploymentMode())) {
            return; //do not interact with the Gateway module in server mode
        }

        if (gatewayConnection != null) {
            sendMessage(ModuleTypeEnum.GATEWAY_MODULE, payload, messageType, null);
        }
    }

    /**
     * Gets the gateway connections that this web monitor is currently sharing knowledge session data with
     * to monitor said data outside of GIFT
     *
     * @return the gateway connection that data is being shared with. Can be null, if no connections are connected.
     */
    public GatewayConnection getGatewayConnection() {
        return gatewayConnection;
    }

    /**
     * Sets the configuration that external monitor applications should use when
     * they are connected and sharing data
     *
     * @param config the configuration to use. If null, no action will be
     *        performed.
     */
    public void setExternalMonitorConfig(ExternalMonitorConfig config) {

        if (config != null) {
            externalMonitorConfig = config;

            sendExternalMonitorConfigToGateway();
        }
    }

    /**
     * Sends a message to the Gateway module notifying any connected training
     * applications being used as external monitors that a user has modified the
     * configuration that controls how said applications should monitor the data
     * that is shared with them
     */
    private void sendExternalMonitorConfigToGateway() {
        sendGameStateMessageToGateway(externalMonitorConfig, MessageTypeEnum.EXTERNAL_MONITOR_CONFIG);
    }
    
    /**
     * Registers a listener that is interested in being notified when users come
     * online and go offline
     *
     * @param listener The interested listener. If null, no listener will be added.
     */
    public void registerUserStatusListener(final UserStatusListener listener) {
        userStatusModel.addListener(listener);
    }
    
    /**
     * Deregisters a listener to stop being notified when users come
     * online and go offline
     *
     * @param listener The listener to deregister. If null, no listener will be removed.
     */
    public void deregisterUserStatusListener(final UserStatusListener listener) {
        userStatusModel.removeListener(listener);
    }
    
    /**
     * Registers a listener that is interested in being notified when domain
     * sessions start and end
     *
     * @param listener The interested listener. If null, no listener will be added.
     */
    public void registerDomainSessionStatusListener(final DomainSessionStatusListener listener) {
        domainSessionStatusModel.addListener(listener);
    }
    
    /**
     * Deregisters a listener to stop being notified when domain
     * sessions start and end
     *
     * @param listener The listener to deregister. If null, no listener will be removed.
     */
    public void deregisterDomainSessionStatusListener(final DomainSessionStatusListener listener) {
        domainSessionStatusModel.removeListener(listener);
    }
    
    /**
     * Registers a listener that is interested in getting all messages in the
     * system
     *
     * @param listener The interested listener
     */
    public void registerMessageListener(final MonitorMessageListener listener) {
        synchronized (messageListeners) {
            messageListeners.add(listener);
        }
    }
    
    public void deregisterMessageListener(final MonitorMessageListener listener) {
        synchronized (messageListeners) {
           messageListeners.remove(listener);
        }
    }
    
    /**
     * Handle any monitoring of a user session message.
     * 
     * @param message the user session message to handle. Cannot be null.
     */
    private void handleAbstractUserSessionMessage(final UserSessionMessage message) {

        final UserSession user = message.getUserSession();
        if(message.getMessageType() == MessageTypeEnum.PROCESSED_ACK || message.getMessageType() == MessageTypeEnum.PROCESSED_NACK
                || message.getMessageType() == MessageTypeEnum.ACK || message.getMessageType() == MessageTypeEnum.NACK){
                 //Note: have to ignore these ACK/NACK messages which will happen after a logout message is sent.
                 //      Also, these are some of the most commonly sent messages, therefore checking for these first is a good idea.
                 
                 return;
                 
        }else if(message.getMessageType() == MessageTypeEnum.LOGOUT_REQUEST){
            //signals that its time to remove the user session
            
            if(message.getSenderModuleType() == ModuleTypeEnum.TUTOR_MODULE){
               
                // Similar to the domain session logic, the user sessions are not immediately removed.
                // there are cases (such as when a domain session is also being closed for the user session) where incoming
                // messages need to be processed.  In this case, a slight delay (5s) is used to handle the incoming messages
                // before the user session is actually removed from the monitor.
                if (!userSessionsPendingRemoval.contains(user)) {

                    // Delay the user session removal for 5 seconds.
                    Timer delayedDSRemovalTimer = new Timer("Delayed User Session Remove");
                    delayedDSRemovalTimer.schedule(new TimerTask() {
                        
                        @Override
                        public void run() {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Removing user session: " + user);
                            }                           

                            userStatusModel.removeUserSession(user);
                            
                            List<UserSession> usIds = globalTModuleAddrToUSessionId.get(message.getSenderAddress());
                            if(usIds != null && !usIds.isEmpty() && usIds.contains(message.getUserSession())){
                                if (logger.isInfoEnabled()) {
                                    logger.info("Removing user session id of "+message.getUserId()+" from being mapped to tutor module named "+message.getSenderModuleName());
                                }
                                
                                usIds.remove(user); 
                            }
                            
                            synchronized (userSessionsPendingRemoval) {
                                userSessionsPendingRemoval.remove(user);
                            }
                            
                        }
                    }, 5000);
                    
                    synchronized (userSessionsPendingRemoval) {
                        userSessionsPendingRemoval.add(user);
                    }
                } 
            }
            
        }else if(message.getMessageType() == MessageTypeEnum.LOGIN_REQUEST){
            //signals that its time to add a user session
            
            if(message.getSenderModuleType() == ModuleTypeEnum.TUTOR_MODULE){
                //add mapping of tutor module to user session
                addTutorModuleUserSession(message.getSenderAddress(), user);
            }
            
        }else{
            //otherwise make sure there is an entry for this user session
            //if login failed user message will be module allocation request or reply at this point, so don't add to list
            if(message.getMessageType() != MessageTypeEnum.MODULE_ALLOCATION_REQUEST && message.getMessageType() != MessageTypeEnum.MODULE_ALLOCATION_REPLY){
                userStatusModel.addUserSession(message.getUserSession());
            }
            
            if(message.getSenderModuleType() == ModuleTypeEnum.TUTOR_MODULE){
                //add mapping of tutor module to user session
                addTutorModuleUserSession(message.getSenderAddress(), user);
            }
        }
                
        if (message instanceof DomainSessionMessage) {
            handleDomainSessionMessage((DomainSessionMessage) message);
        }
    }
    
    /**
     * Add a new mapping of a domain module's domain session.
     * 
     * @param dmIdentifier - unique identifier of a domain module. Cannot be null.
     * @param dsId - unique identifier of a domain session
     */
    private void addDomainModuleDomainSession(String dmIdentifier, int dsId){
        
        List<Integer> dsIds = globalDModuleAddrToDSessionId.get(dmIdentifier);
        if(dsIds == null){
            dsIds = new ArrayList<>();
            globalDModuleAddrToDSessionId.put(dmIdentifier, dsIds);
        }
        
        if(!dsIds.contains(dsId)){
            if (logger.isInfoEnabled()) {
                logger.info("Adding new domain session id of "+dsId+" to current mapping of domain session ids of "+dsId+" to domain module identifier of "+dmIdentifier);
            }
            
            dsIds.add(dsId);
        }
    }
    
    /**
     * Add a new mapping of a tutor module's user session.
     * 
     * @param tmIdentifier - unique identifier of a tutor module. Cannot be null.
     * @param user - the user session to add. Cannot be null.
     */
    private void addTutorModuleUserSession(String tmIdentifier, UserSession user){

        List<UserSession> usIds = globalTModuleAddrToUSessionId.get(tmIdentifier);
        if(usIds == null){
            usIds = new ArrayList<UserSession>();
            globalTModuleAddrToUSessionId.put(tmIdentifier, usIds);
        }
        
        if(!usIds.contains(user)){
            if (logger.isInfoEnabled()) {
                logger.info("Adding new user "+user+" to current mapping of user session ids of "+usIds+" to tutor module identifier of "+tmIdentifier);
            }
            
            usIds.add(user);
        } 
    }

    /**
     * Handle monitoring a domain session message.
     * 
     * @param msg the message to handle. Cannot be null.
     */
    private void handleDomainSessionMessage(final DomainSessionMessage msg) {        
        if(msg.getMessageType() == MessageTypeEnum.PROCESSED_ACK || msg.getMessageType() == MessageTypeEnum.PROCESSED_NACK
           || msg.getMessageType() == MessageTypeEnum.ACK || msg.getMessageType() == MessageTypeEnum.NACK){
            //Note: have to ignore these ACK/NACK messages which will happen after a close domain session message is sent.
            //      Also, these are some of the most commonly sent messages, therefore checking for these first is a good idea.
            
            return;
                    
        }else if(msg.getMessageType() == MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST){
            //signals that its time to remove the domain session
            if(msg.getSenderModuleType() == ModuleTypeEnum.DOMAIN_MODULE){
                //remove domain module domain session id mapping                
                final Integer domainSessionId = msg.getDomainSessionId();
                if(domainSessionStatusModel.containsDomainSession(domainSessionId)){
                    //the domain session needs to be removed from the list
                    if (!domainSessionsPendingRemoval.contains(domainSessionId)) {
                        //there isn't a timer already handling the removal call
                        //delay the removal of the domain session to allow for all domain session messages associated with
                        //closing a domain session to be monitored
                        Timer delayedDSRemovalTimer = new Timer("Delayed Domain Session Remove");
                        delayedDSRemovalTimer.schedule(new TimerTask() {
                            
                            @Override
                            public void run() {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Removing domain session: " + domainSessionId);
                                }
                                
                                domainSessionStatusModel.removeDomainSession(domainSessionId);  
                                
                                synchronized (domainSessionsPendingRemoval) {
                                    domainSessionsPendingRemoval.remove(domainSessionId);
                                }
                                
                            }
                        }, 5000);
                        
                        synchronized (domainSessionsPendingRemoval) {
                            domainSessionsPendingRemoval.add(domainSessionId);
                        }
                        
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("domainSessionsPendingRemoval already contains the domain session to be removed, NOT scheduling a removal timer.");
                        }                        
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("domainSessionStatusModel cannot find domain session: " + msg.getDomainSessionId());
                    }                    
                }
                
                List<Integer> dsIds = globalDModuleAddrToDSessionId.get(msg.getSenderAddress());
                if(dsIds != null && !dsIds.isEmpty() && dsIds.contains(msg.getDomainSessionId())){
                    if (logger.isInfoEnabled()) {
                        logger.info("Removing domain session id of "+msg.getDomainSessionId()+" from being mapped to domain module named "+msg.getSenderModuleName());
                    }
                    
                    dsIds.remove(Integer.valueOf(msg.getDomainSessionId()));
                }
            }
                        
        }else{
            //otherwise make sure there is an entry for this domain session
            
            if(!domainSessionStatusModel.containsDomainSession(msg.getDomainSessionId())){
                DomainSession dSession = new DomainSession(msg.getDomainSessionId(), msg.getUserId(), DomainSession.UNKNOWN_DOMAIN_NAME, DomainSession.UNKNOWN_DOMAIN_NAME);
                dSession.copyFromUserSession(msg.getUserSession());
                domainSessionStatusModel.addDomainSession(dSession);
            }
            
            if(msg.getSenderModuleType() == ModuleTypeEnum.DOMAIN_MODULE){
                //add mapping of domain module to domain session
                
                addDomainModuleDomainSession(msg.getSenderAddress(), msg.getDomainSessionId());
            }
        }        

    }

    /**
     * A helper class used to allow the web monitor module to manage a connection to the Gateway module. This
     * class is primarily used to set up a connection between the web monitor and the Gateway when needed in
     * order to allow the web monitor to connect to an external application and send monitored entity states
     * to it.
     *
     * @author nroberts
     */
    //NOTE: Most of the logic in this class has been adapted from BaseDomainSession.InitializeGateway
    private class GatewayManager{

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

            if (DashboardProperties.getInstance().getDeploymentMode() != DeploymentModeEnum.SERVER) {

                // get a gateway module client connection
                MessageCollectionCallback gatewayCallback = new MessageCollectionCallback() {

                    @Override
                    public void success() {

                        if(logger.isInfoEnabled()){
                            logger.info("Gateway module is now allocated to web monitor.");
                        }

                        releaseHold();
                    }

                    @Override
                    public void received(final Message aMsg) {

                    }

                    @Override
                    public void failure(Message nackMsg) {

                        NACK nack = (NACK)nackMsg.getPayload();
                        handleFailure(nack.getErrorMessage());
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

                        ModuleStatus moduleStatus = status.getModuleStatus();
                        if(moduleStatus.getModuleType() == ModuleTypeEnum.DOMAIN_MODULE){
                            
                            //remove domain sessions mapped to this domain module
                            if (logger.isInfoEnabled()) {
                                logger.info("Removing the domain sessions mapped to domain module named "+moduleStatus.getModuleName()+" because that module is no longer online after not having heard from it for "+(status.getTimeoutValue()/1000.0)+" seconds.");
                            }
                            
                            List<Integer> dsIds = globalDModuleAddrToDSessionId.remove(moduleStatus.getQueueName());
                            domainSessionStatusModel.removeDomainSessions(dsIds);                  
                            
                        }else if(moduleStatus.getModuleType() == ModuleTypeEnum.TUTOR_MODULE){
                            
                            //remove user sessions mapped to this tutor module
                            if (logger.isInfoEnabled()) {
                                logger.info("Removing the user sessions mapped to tutor module named "+moduleStatus.getModuleName()+" because that module is no longer online after not having heard from it for "+(status.getTimeoutValue()/1000.0)+" seconds.");
                            }
                            
                            List<UserSession> usIds = globalTModuleAddrToUSessionId.remove(moduleStatus.getQueueName());
                            userStatusModel.removeUserSessions(usIds);
                        }
                    }

                    @Override
                    public void moduleStatusChanged(long sentTime, ModuleStatus status) {

                        //check if GW module
                        if(gwModuleStatus == null && status.getModuleType() == ModuleTypeEnum.GATEWAY_MODULE){

                            if(logger.isInfoEnabled()){
                                logger.info("Gateway Module Status Added for web monitor: " + status + ".");
                            }

                            selectGatewayModule(gatewayCallback, status);

                            gwModuleStatus = (GatewayModuleStatus) status;
                            timeoutTimer.cancel();
                        }
                    }

                    @Override
                    public void moduleStatusAdded(long sentTime, final ModuleStatus status) {

                        if(status.getModuleType() == ModuleTypeEnum.DOMAIN_MODULE){
                            
                            //request active domain sessions
                            createSubjectQueueClient(status.getQueueName(), status.getModuleType(), false);
                            sendMessage(null, MessageTypeEnum.ACTIVE_DOMAIN_SESSIONS_REQUEST, new MessageCollectionCallback() {
                                
                                @Override
                                public void success() {
                                    removeClientConnection(status.getQueueName(), false);                            
                                }
                                
                                @Override
                                public void received(Message msg) {
                                    
                                    if(msg.getPayload() instanceof DomainSessionList){
                                        
                                        DomainSessionList dsList = (DomainSessionList)msg.getPayload();
                                        if (logger.isInfoEnabled()) {
                                            logger.info("Adding mapping domain module: "+status+" active domain sessions of "+dsList);     
                                        }
                                                               
                                        domainSessionStatusModel.addDomainSessions(dsList.getDomainSessions());                              
                                        
                                    }else{
                                        logger.error("Received unhandled message payload when requesting active domain sessions from domain module: "+status+" - "+msg);
                                    }               
                                }
                                
                                @Override
                                public void failure(String why) {
                                    logger.error("Unable to retrieve active domain sessions from domain module: "+status+" because "+why);
                                    removeClientConnection(status.getQueueName(), false);
                                }
                                
                                @Override
                                public void failure(Message msg) {
                                    logger.error("Unable to retrieve active domain sessions from domain module: "+status+" because received response message of "+msg);
                                    removeClientConnection(status.getQueueName(), false);
                                }
                            });
                            
                        }else if(status.getModuleType() == ModuleTypeEnum.TUTOR_MODULE){
                            
                            //request active user sessions
                            createSubjectQueueClient(status.getQueueName(), status.getModuleType(), false);
                            sendMessage(null, MessageTypeEnum.ACTIVE_USER_SESSIONS_REQUEST, new MessageCollectionCallback() {
                                
                                @Override
                                public void success() {
                                    removeClientConnection(status.getQueueName(), false);                            
                                }
                                
                                @Override
                                public void received(Message msg) {
                                    
                                    if(msg.getPayload() instanceof UserSessionList){
                                        
                                        UserSessionList usList = (UserSessionList)msg.getPayload();
                                        
                                        if (logger.isInfoEnabled()) {
                                            logger.info("Adding mapping tutor module: "+status+" active user sessions of "+usList);
                                        }
                                        
                                        userStatusModel.addUserSessions(usList.getUserSessions());
                                        
                                    }else{
                                        logger.error("Received unhandled message payload when requesting active user sessions from tutor module: "+status+" - "+msg);
                                    }
                                }
                                
                                @Override
                                public void failure(String why) {
                                    logger.error("Unable to retrieve active user sessions from tutor module: "+status+" because "+why);
                                    removeClientConnection(status.getQueueName(), false);
                                }
                                
                                @Override
                                public void failure(Message msg) {
                                    logger.error("Unable to retrieve active user sessions from tutor module: "+status+" because received response message of "+msg);
                                    removeClientConnection(status.getQueueName(), false);
                                }
                            });
                            
                        }
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
}
