/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.ModuleConnectionConfigurationException;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.WebClientInformation;
import mil.arl.gift.common.enums.ErrorEnum;
import mil.arl.gift.common.enums.MessageEncodingTypeEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.module.AbstractModule;
import mil.arl.gift.common.module.GatewayModuleStatus;
import mil.arl.gift.common.module.ModuleAllocationReply;
import mil.arl.gift.common.module.ModuleAllocationRequest;
import mil.arl.gift.common.module.ModuleStatus;
import mil.arl.gift.common.module.ModuleStatusListener;
import mil.arl.gift.common.module.ModuleStatusMonitor;
import mil.arl.gift.common.module.ModuleStatusMonitor.StatusReceivedInfo;
import mil.arl.gift.common.util.CompareUtil;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.api.MessageCollection.MessageCollectionFinishedCallback;
import mil.arl.gift.net.api.message.ACK;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageHandler;
import mil.arl.gift.net.api.message.MessageUtil;
import mil.arl.gift.net.api.message.NACK;
import mil.arl.gift.net.api.message.PayloadDecodeException;
import mil.arl.gift.net.api.message.RawMessageHandler;
import mil.arl.gift.net.api.message.UserSessionMessage;
import mil.arl.gift.net.util.Util;

/**
 * This class is responsible for consuming and sending messages for a module
 * using client connections. It will consume the raw messages and provide a GIFT
 * message to a GIFT message handler.
 *
 * @author mhoffman
 */
public class NetworkSession
        implements ModuleStatusListener, MessageCollectionFinishedCallback, MessageClientConnectionListener {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(NetworkSession.class);

    /** flag used to indicate that this session is in the process of shutting down */
    private boolean shuttingDown = false;

    /** The message broker location */
    private URI messageBrokerURI;

    /**
     * The sourceEventId that will be generated for the message collection
     */
    private AtomicInteger atomicSourceEventId = new AtomicInteger(0);

    /** the message encoding type to use for all messages being sent by this module */
    private MessageEncodingTypeEnum messageCodecType;

    /** the name of the module's external message inbox for which other modules can send messages too */
    private String connectionAddress;  // old: getModuleInboxQueueName()

    /** The name of the sender module */
    private String senderName;      //old : getModuleName()

    /** the module type that is instantiating this network session */
    private ModuleTypeEnum senderModuleType; 

    /** the module's queue client instance */
    private QueueMessageClient moduleQueueClient;

    /**
     * list of clients that should be destroyed on the message bus by this network session instance.
     * This is needed in order to cleanup the message bus of destinations (e.g. topics) that are no
     * longer being used (e.g. java web start gateway module topic for a gateway module that has been closed).
     * Be careful using this as other topics/queues may still be using that destination.
     */
    private List<MessageClient> otherClientsToDestroy = new ArrayList<>();

    /** handler for simulation messages received from a GW module topic */
    private RawMessageHandler simulationMessageHandler;

    /** handler for embededded simulation messages received from a Tutor module topic */
    private RawMessageHandler embeddedSimulationMessageHandler;

    /**
     * map of user session information to a connected gateway topic name that is for that user only
     * key: unique identifier of a gift user
     * value: the gateway topic name that user has been associated with
     */
    private Map<CompositeUserKey, String> userToGatewayTopic = new HashMap<>();

    /** instance of the module monitor used to check for timed-out modules */
    private ModuleStatusMonitor moduleMonitor = ModuleStatusMonitor.getInstance();

    /** whether or not to filter certain module's based on IP address */
    protected boolean ipaddrFilterOn = true;

    /** temporary key used for lookup purposes */
    private CompositeUserModuleKey lookupKey = new CompositeUserModuleKey();

    /**
     * mapping of subject (i.e. queue name, topic name) to this modules client connection to that subject.
     */
    private final Map<String, MessageClient> subjectToClient = new HashMap<>();

    /**
     * mapping of module type to the message client subject names for modules of that type
     */
    private final Map<ModuleTypeEnum, List<String>> moduleTypeToSubjects = new HashMap<>();

    /**
     * mapping of composite key (module type, user id) to the message client subject name
     * Note: only module connections that are currently being used in a user session will have entries
     *       in this map.  The exception is the UMS module which is needed by the Tutor module prior to a user session.
     */
    private final Map<CompositeUserModuleKey, String> compositeToSubject = new HashMap<>();

    /** outstanding message collections waiting to be satisfied with response(s) */
    private final Map<Integer, MessageCollection> messageCollectionMap = new HashMap<>();

    /** listeners that are interested in being notified of allocated module events */
    private final List<AllocatedModuleListener> allocatedModuleListeners = new ArrayList<>();

    /** responsible for monitoring status of all modules that this module is receiving discovery information on */
    private ModuleStatusMonitor moduleStatusMonitor = ModuleStatusMonitor.getInstance();

    /** list of unprocessed but decoded Messages (not ACKs or NACKs) */
    private final List<Message> decodedMessageList = new LinkedList<>();

    /**
     * list of unprocessed but decoded ACK/NACK messages
     * The reason these are in a separate thread is to give these messages types precedence over
     * any other waiting message types due to Message Collection timeout logic that is waiting
     * for a response to sending a message.
     */
    private final List<Message> decodedACKs = new LinkedList<>();

    /**
     * Used to wake-up the thread waiting for incoming decoded messages to arrive
     */
    private final Object decodedMessageMutex = new Object();

    /**
     * contains pre-determined message destinations defined by the GIFT
     * architecture
     */
    protected static final Map<MessageTypeEnum, ModuleTypeEnum[]> messageTypeToRecipients = new HashMap<>();

    /**
     * contains pre-determined module connections needed on a per module type basis based on GIFT architecture.
     * Note: Only the modules responsible for initiating module allocation requests are listed.
     */
    protected static final Map<ModuleTypeEnum, String[]> moduleClientsNeeded = new HashMap<>();

    static {
        //for creating new user, logging in, viewing LMS history, starting/stopping a session
        moduleClientsNeeded.put(ModuleTypeEnum.TUTOR_MODULE,
                new String[]{SubjectUtil.UMS_DISCOVERY_TOPIC, SubjectUtil.LMS_DISCOVERY_TOPIC, SubjectUtil.DOMAIN_DISCOVERY_TOPIC});

        //to allocate connections for a domain session
        moduleClientsNeeded.put(ModuleTypeEnum.DOMAIN_MODULE,
                new String[]{SubjectUtil.UMS_DISCOVERY_TOPIC, SubjectUtil.LMS_DISCOVERY_TOPIC, SubjectUtil.SENSOR_DISCOVERY_TOPIC,
                SubjectUtil.LEARNER_DISCOVERY_TOPIC, SubjectUtil.PED_DISCOVERY_TOPIC, SubjectUtil.GATEWAY_DISCOVERY_TOPIC,
                SubjectUtil.TUTOR_DISCOVERY_TOPIC, SubjectUtil.MONITOR_DISCOVERY_TOPIC});

        //for message monitoring purposes
        moduleClientsNeeded.put(ModuleTypeEnum.MONITOR_MODULE,
                new String[]{SubjectUtil.UMS_DISCOVERY_TOPIC, SubjectUtil.LMS_DISCOVERY_TOPIC, SubjectUtil.SENSOR_DISCOVERY_TOPIC,
                SubjectUtil.LEARNER_DISCOVERY_TOPIC, SubjectUtil.PED_DISCOVERY_TOPIC, SubjectUtil.GATEWAY_DISCOVERY_TOPIC,
                SubjectUtil.TUTOR_DISCOVERY_TOPIC, SubjectUtil.DOMAIN_DISCOVERY_TOPIC});

        //for message logging purposes
        //TODO: remove these activation calls once the logger is removed from the UMS module
        moduleClientsNeeded.put(ModuleTypeEnum.UMS_MODULE,
                new String[]{SubjectUtil.LMS_DISCOVERY_TOPIC, SubjectUtil.SENSOR_DISCOVERY_TOPIC,
                SubjectUtil.LEARNER_DISCOVERY_TOPIC, SubjectUtil.PED_DISCOVERY_TOPIC, SubjectUtil.GATEWAY_DISCOVERY_TOPIC,
                SubjectUtil.TUTOR_DISCOVERY_TOPIC, SubjectUtil.DOMAIN_DISCOVERY_TOPIC});

        messageTypeToRecipients.put(MessageTypeEnum.START_DOMAIN_SESSION,
                new ModuleTypeEnum[]{ModuleTypeEnum.LEARNER_MODULE,
                    ModuleTypeEnum.UMS_MODULE,
                    ModuleTypeEnum.SENSOR_MODULE,
                    ModuleTypeEnum.GATEWAY_MODULE,
                    ModuleTypeEnum.PEDAGOGICAL_MODULE,
                    ModuleTypeEnum.TUTOR_MODULE,
                    ModuleTypeEnum.LMS_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.INITIALIZE_DOMAIN_SESSION_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.LEARNER_MODULE,
                    ModuleTypeEnum.UMS_MODULE,
                    ModuleTypeEnum.SENSOR_MODULE,
                    ModuleTypeEnum.GATEWAY_MODULE,
                    ModuleTypeEnum.PEDAGOGICAL_MODULE,
                    ModuleTypeEnum.TUTOR_MODULE,
                    ModuleTypeEnum.LMS_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.LEARNER_MODULE,
                    ModuleTypeEnum.PEDAGOGICAL_MODULE,
                    ModuleTypeEnum.UMS_MODULE,
                    ModuleTypeEnum.SENSOR_MODULE,
                    ModuleTypeEnum.GATEWAY_MODULE,
                    ModuleTypeEnum.TUTOR_MODULE,
                    ModuleTypeEnum.LMS_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.LOGOUT_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.UMS_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.LOGIN_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.UMS_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.LTI_GETUSER_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.UMS_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.LTI_GET_PROVIDER_URL_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.DOMAIN_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.NEW_USER_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.UMS_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.DOMAIN_OPTIONS_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.DOMAIN_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.DOMAIN_SELECTION_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.DOMAIN_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.LEARNER_TUTOR_ACTION,
                new ModuleTypeEnum[]{ModuleTypeEnum.DOMAIN_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.EXPERIMENT_COURSE_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.DOMAIN_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.GET_SURVEY_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.UMS_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.GET_EXPERIMENT_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.UMS_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.LOS_QUERY,
                new ModuleTypeEnum[]{ModuleTypeEnum.GATEWAY_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.VARIABLE_STATE_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.GATEWAY_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.SURVEY_PRESENTED_NOTIFICATION,
                new ModuleTypeEnum[]{ModuleTypeEnum.GATEWAY_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.PERFORMANCE_ASSESSMENT,
                new ModuleTypeEnum[]{ModuleTypeEnum.LEARNER_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.PUBLISH_LESSON_SCORE_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.LMS_MODULE,
                    ModuleTypeEnum.LEARNER_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.LESSON_GRADED_SCORE_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.LEARNER_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.ENVIRONMENT_CONTROL,
                new ModuleTypeEnum[]{ModuleTypeEnum.GATEWAY_MODULE,
                        ModuleTypeEnum.LMS_MODULE}); // for xapi statements

        messageTypeToRecipients.put(MessageTypeEnum.SUBMIT_SURVEY_RESULTS,
                new ModuleTypeEnum[]{ModuleTypeEnum.UMS_MODULE,
                        ModuleTypeEnum.LEARNER_MODULE,
                        ModuleTypeEnum.LMS_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.SENSOR_FILTER_DATA,
                new ModuleTypeEnum[]{ModuleTypeEnum.LEARNER_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.SENSOR_DATA,
                new ModuleTypeEnum[]{ModuleTypeEnum.LEARNER_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.SENSOR_FILE_CREATED,
                new ModuleTypeEnum[]{ModuleTypeEnum.UMS_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.INIT_INTEROP_CONNECTIONS,
                new ModuleTypeEnum[]{ModuleTypeEnum.GATEWAY_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.INIT_EMBEDDED_CONNECTIONS,
                new ModuleTypeEnum[]{ModuleTypeEnum.TUTOR_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.CONFIGURE_INTEROP_CONNECTIONS,
                new ModuleTypeEnum[]{ModuleTypeEnum.GATEWAY_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.TUTOR_SURVEY_QUESTION_RESPONSE,
                new ModuleTypeEnum[]{ModuleTypeEnum.GATEWAY_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.LEARNER_STATE,
                new ModuleTypeEnum[]{ModuleTypeEnum.PEDAGOGICAL_MODULE,
                        ModuleTypeEnum.LMS_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.INITIALIZE_LESSON_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.PEDAGOGICAL_MODULE,
                        ModuleTypeEnum.LEARNER_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.COURSE_STATE,
                new ModuleTypeEnum[]{ModuleTypeEnum.PEDAGOGICAL_MODULE,
                        ModuleTypeEnum.TUTOR_MODULE,
                        ModuleTypeEnum.LEARNER_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.DISPLAY_TEAM_SESSIONS,
                new ModuleTypeEnum[]{ModuleTypeEnum.TUTOR_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.INITIALIZE_PEDAGOGICAL_MODEL_REQUEST,
                new ModuleTypeEnum[] {ModuleTypeEnum.PEDAGOGICAL_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.LESSON_STARTED,
                new ModuleTypeEnum[]{ModuleTypeEnum.TUTOR_MODULE,
                    ModuleTypeEnum.LEARNER_MODULE,
                    ModuleTypeEnum.PEDAGOGICAL_MODULE,
                    ModuleTypeEnum.GATEWAY_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.LESSON_COMPLETED,
                new ModuleTypeEnum[]{ModuleTypeEnum.TUTOR_MODULE,
                    ModuleTypeEnum.LEARNER_MODULE,
                    ModuleTypeEnum.PEDAGOGICAL_MODULE,
                    ModuleTypeEnum.GATEWAY_MODULE,
                    ModuleTypeEnum.LMS_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.LMS_DATA_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.LMS_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.INSTANTIATE_LEARNER_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.LEARNER_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.DISPLAY_FEEDBACK_GATEWAY_REQUEST,
                new ModuleTypeEnum[]{ ModuleTypeEnum.GATEWAY_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.DISPLAY_FEEDBACK_EMBEDDED_REQUEST,
                new ModuleTypeEnum[]{ ModuleTypeEnum.TUTOR_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.VIBRATE_DEVICE_REQUEST,
                new ModuleTypeEnum[]{
                        ModuleTypeEnum.TUTOR_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.DISPLAY_COURSE_INIT_INSTRUCTIONS_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.TUTOR_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.DISPLAY_FEEDBACK_TUTOR_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.TUTOR_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.DISPLAY_GUIDANCE_TUTOR_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.TUTOR_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.DISPLAY_CONTENT_TUTOR_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.TUTOR_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.DISPLAY_AAR_TUTOR_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.TUTOR_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.DISPLAY_SURVEY_TUTOR_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.TUTOR_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.DISPLAY_LEARNER_ACTIONS_TUTOR_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.TUTOR_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.DISPLAY_LESSON_MATERIAL_TUTOR_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.TUTOR_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.DISPLAY_MIDLESSON_MEDIA_TUTOR_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.TUTOR_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.DISPLAY_CHAT_WINDOW_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.TUTOR_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.DISPLAY_CHAT_WINDOW_UPDATE_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.TUTOR_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.CHAT_LOG,
                new ModuleTypeEnum[]{ModuleTypeEnum.DOMAIN_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.PEDAGOGICAL_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.DOMAIN_MODULE,
                        ModuleTypeEnum.LMS_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.ACTIVE_DOMAIN_SESSIONS_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.DOMAIN_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.ACTIVE_USER_SESSIONS_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.TUTOR_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.SURVEY_CHECK_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.UMS_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.DOMAIN_SESSION_START_TIME_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.UMS_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.USER_ID_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.UMS_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.SUBJECT_CREATED,
                new ModuleTypeEnum[]{ModuleTypeEnum.TUTOR_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.BRANCH_PATH_HISTORY_REQUEST,
                new ModuleTypeEnum[]{ModuleTypeEnum.UMS_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.BRANCH_PATH_HISTORY_UPDATE,
                new ModuleTypeEnum[]{ModuleTypeEnum.UMS_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.UNDER_DWELL_VIOLATION,
                new ModuleTypeEnum[]{ModuleTypeEnum.DOMAIN_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.OVER_DWELL_VIOLATION,
                new ModuleTypeEnum[]{ModuleTypeEnum.DOMAIN_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.EVALUATOR_UPDATE_REQUEST,
                new ModuleTypeEnum[] { ModuleTypeEnum.DOMAIN_MODULE });

        messageTypeToRecipients.put(MessageTypeEnum.ACTIVE_KNOWLEDGE_SESSIONS_REQUEST,
                new ModuleTypeEnum[] { ModuleTypeEnum.DOMAIN_MODULE });

        messageTypeToRecipients.put(MessageTypeEnum.MANAGE_MEMBERSHIP_TEAM_KNOWLEDGE_SESSION,
                new ModuleTypeEnum[] { ModuleTypeEnum.DOMAIN_MODULE });

        messageTypeToRecipients.put(MessageTypeEnum.START_TEAM_KNOWLEDGE_SESSION_REQUEST,
                new ModuleTypeEnum[] { ModuleTypeEnum.DOMAIN_MODULE,
                        ModuleTypeEnum.LMS_MODULE});

        messageTypeToRecipients.put(MessageTypeEnum.START_TEAM_KNOWLEDGE_SESSION_REPLY,
                new ModuleTypeEnum[] { ModuleTypeEnum.TUTOR_MODULE });

        messageTypeToRecipients.put(MessageTypeEnum.KNOWLEDGE_SESSION_UPDATED_REPLY,
                new ModuleTypeEnum[] { ModuleTypeEnum.DOMAIN_MODULE });

        messageTypeToRecipients.put(MessageTypeEnum.AUTHORIZE_STRATEGIES_REQUEST,
                new ModuleTypeEnum[] { ModuleTypeEnum.MONITOR_MODULE });

        messageTypeToRecipients.put(MessageTypeEnum.EXECUTE_OC_STRATEGY,
                new ModuleTypeEnum[] { ModuleTypeEnum.MONITOR_MODULE });

        messageTypeToRecipients.put(MessageTypeEnum.APPLY_STRATEGIES,
                new ModuleTypeEnum[] { ModuleTypeEnum.DOMAIN_MODULE });

        messageTypeToRecipients.put(MessageTypeEnum.EXTERNAL_MONITOR_CONFIG,
                new ModuleTypeEnum[] { ModuleTypeEnum.GATEWAY_MODULE });
        
        messageTypeToRecipients.put(MessageTypeEnum.KNOWLEDGE_SESSION_CREATED, 
                new ModuleTypeEnum[] { ModuleTypeEnum.LMS_MODULE });
    }

    //TODO: change individual connection properties to connection info object
    /**
     * Class constructor - set connection attributes and connect
     *
     * @param senderName - display name of the module
     * @param senderModuleType - the module type that is instantiating this network session
     * @param connectionName - the name of the module's (internal) connection, i.e. the module's address, from which the module receives incoming messages
     * @param clearOnStartUp - whether or not to clear the connection of any cached messages upon establishing a connection
     * @param connectionAddress - the name of the module's external message inbox for which other modules can send messages too
     * @param messageBrokerURL - the URL of the message broker that maintains message passing
     * @param messageEncodingType - the encoding scheme to use for encoding outgoing messages
     * @param messageHandler - handler of incoming messages
     * @param ipAddressFilter - whether or not to apply an IP address filtering when considering modules to allocate (based on this module's IP address)
     * @param redirectTutorMsgsToGateway - whether or not message normally sent to the tutor module as part of the architecture
     * should be sent to the gateway module instead because the tutor module is not going to be used. True to redirect.
     * Certain additional message types will also be sent to the Gateway module (e.g. Learner State, Authorize Strategies request).
     * @throws ModuleConnectionConfigurationException if there was a problem making a connection to the message bus
     */
    public NetworkSession(String senderName, ModuleTypeEnum senderModuleType, String connectionName,
            boolean clearOnStartUp, String connectionAddress, String messageBrokerURL,
            MessageEncodingTypeEnum messageEncodingType, MessageHandler messageHandler, boolean ipAddressFilter,
            boolean redirectTutorMsgsToGateway) throws ModuleConnectionConfigurationException {

        if (messageBrokerURL == null) {
            throw new IllegalArgumentException("The message broker URL can't be null");
        }

        if (messageEncodingType == null) {
            throw new IllegalArgumentException("The message encoding type can't be null");
        }

        if (connectionAddress == null) {
            throw new IllegalArgumentException("The connection address can't be null");
        }

        if (senderName == null) {
            throw new IllegalArgumentException("The sender name can't be null");
        }

        if (senderModuleType == null) {
            throw new IllegalArgumentException("The sender module type can't be null");
        }
        try {

            this.messageBrokerURI = new URI(messageBrokerURL);

        } catch (URISyntaxException ex) {

            throw new IllegalArgumentException("The message broker URL is not a valid URI", ex);
        }

        this.messageCodecType = messageEncodingType;
        this.connectionAddress = connectionAddress;
        this.senderName = senderName;
        this.senderModuleType = senderModuleType;
        this.ipaddrFilterOn = ipAddressFilter;

        if(redirectTutorMsgsToGateway){
            // the tutor module is not going to be running so the Gateway module is now
            // responsible for exposing an API to start/stop sessions.  Therefore any
            // important course related messages that the tutor once managed need to
            // be sent to the Gateway module.

            // for creating new user, logging in, starting/stopping a session
            moduleClientsNeeded.put(ModuleTypeEnum.GATEWAY_MODULE,
                    new String[] { SubjectUtil.UMS_DISCOVERY_TOPIC, SubjectUtil.LMS_DISCOVERY_TOPIC,
                            SubjectUtil.DOMAIN_DISCOVERY_TOPIC });

            Set<Entry<MessageTypeEnum, ModuleTypeEnum[]>> entrySet = messageTypeToRecipients.entrySet();
            Iterator<Entry<MessageTypeEnum, ModuleTypeEnum[]>> itr = entrySet.iterator();
            while(itr.hasNext()){
                Entry<MessageTypeEnum, ModuleTypeEnum[]> entry = itr.next();
                MessageTypeEnum  messageType = entry.getKey();
                ModuleTypeEnum[] recipients = entry.getValue();

                // add the gateway module as recipients to these key messages that the external
                // system wants to know about
                if(messageType == MessageTypeEnum.SENSOR_FILTER_DATA ||
                        messageType == MessageTypeEnum.LEARNER_STATE ||
                        messageType == MessageTypeEnum.PEDAGOGICAL_REQUEST ||
                        messageType == MessageTypeEnum.AUTHORIZE_STRATEGIES_REQUEST){

                    ModuleTypeEnum[] newRecipients = new ModuleTypeEnum[recipients.length + 1];
                    for(int i = 0; i < recipients.length; i++){
                        newRecipients[i] = recipients[i];
                    }

                    // add to the end
                    newRecipients[newRecipients.length-1] = ModuleTypeEnum.GATEWAY_MODULE;
                    recipients = newRecipients;
                    entry.setValue(newRecipients);
                }

                // replace tutor module as recipient with gateway module
                for(int i=0; i < recipients.length; i++){
                    if(recipients[i] == ModuleTypeEnum.TUTOR_MODULE){
                        recipients[i] = ModuleTypeEnum.GATEWAY_MODULE;
                    }
                }
            }

        }

        initializeModuleConnection(connectionName, clearOnStartUp, messageHandler);

        moduleStatusMonitor.addListener(this);

        //activate listeners for modules needed by this module for module allocation requests
        if(moduleClientsNeeded.containsKey(senderModuleType)){
            for(String moduleDiscoveryTopic : moduleClientsNeeded.get(senderModuleType)){
                activateModuleHelper(moduleDiscoveryTopic);
            }
        }
    }
    
    /**
     * Return a deep copy of the modules that should receive the provided message type according the the network architecture.
     * 
     * @param messageType the message type to get the recipient module types for, i.e. which module types should receive
     * this message type
     * @return a collection of zero or more modules that should receive this message type.  Won't be null.
     */
    public static List<ModuleTypeEnum> getRecepients(MessageTypeEnum messageType){
        if(messageType == null){
            return new ArrayList<>(0);
        }
        ModuleTypeEnum[] modulesArray = messageTypeToRecipients.get(messageType);
        if(modulesArray == null){
            return new ArrayList<>(0);
        }else{
            return new ArrayList<ModuleTypeEnum>(Arrays.asList(modulesArray));
        }
    }

    /**
     * Listen for module discovery messages on the given topic. This is useful for communicating with a particular module type.
     *
     * @param moduleDiscoveryTopic the name of the discovery topic to listen too
     */
    protected void activateModuleHelper(String moduleDiscoveryTopic) {

        createSubjectTopicClient(moduleDiscoveryTopic, new RawMessageHandler() {
            @Override
            public boolean processMessage(String msg, MessageEncodingTypeEnum encodingType) {

                //ignore incoming messages if currently shutting this module down
                if (shuttingDown) {
                    return false;
                }

                Message message = MessageUtil.getMessageFromString(msg, encodingType);
                handleModuleStatusMessage(message);

                return true;
            }
        }, false);
    }

    /**
     * Add a new allocated module listener to the list of listeners.
     *
     * @param listener - new listener to add
     */
    public void addAllocatedModuleListener(AllocatedModuleListener listener){

        synchronized(allocatedModuleListeners){

            if(!allocatedModuleListeners.contains(listener)){
                allocatedModuleListeners.add(listener);
            }
        }
    }

    /**
     * Handle the module status message from another module
     *
     * @param message - a module status message which needs to be acted upon
     */
    private void handleModuleStatusMessage(Message message) {
        ModuleStatus mStatus = (ModuleStatus) message.getPayload();
        moduleMonitor.receivedModuleStatus(message.getTimeStamp(), mStatus);
    }

    /**
     * Register a message handler to receive training app game state messages that are received via a
     * gateway topic client connection (if one exists for this module).
     *
     * @param handler - handler for training app game state messages
     */
    public void registerTrainingAppGameStateMessageHandler(RawMessageHandler handler){

        if(simulationMessageHandler != null){
            logger.warn("Overriding simulation message handler");
        }

        simulationMessageHandler = handler;
    }

    /**
     * Register a message handler to receive embedded training app game state messages that are received via a
     * tutor topic client connection (if one exists for this module).
     *
     * @param handler - handler for training app game state messages
     */
    public void registerEmbeddedTrainingAppGameStateMessageHandler(String topicName, RawMessageHandler handler) {

        if(embeddedSimulationMessageHandler != null) {
            logger.warn("Overriding embedded simulation message handler");
        }

        embeddedSimulationMessageHandler = handler;

        createSubjectTopicClient(topicName, embeddedSimulationMessageHandler, true);
    }

    /**
     * Register a message handler to receive training app game state messages that are received via a
     * Domain topic client connection (if one exists for this module) as part of a log playback service.
     *
     * @param handler - handler for training app game state messages
     */
    public void registerLogPlaybackTrainingAppGameStateMessageHandler(String topicName, RawMessageHandler handler) {
        createSubjectTopicClient(topicName, handler, true);
    }

    /**
     * Return the module's queue name (i.e. Subject value)
     *
     * @return String
     */
    public String getModuleQueueName() {
        return moduleQueueClient.getSubjectName();
    }

    /**
     * Create the module's client connection instance. Also listen for messages
     * added to the connection.
     *
     * @param moduleQueueName - the name of the module's queue
     * @param clearQueueOnStartup - whether or not to delete any messages already in the module's queue
     * @param messageHandler - the message handler for messages in the module's queue
     * @throws ModuleConnectionConfigurationException thrown if there is a problem connecting to the module
     */
    private void initializeModuleConnection(String moduleQueueName, boolean clearQueueOnStartup,
            final MessageHandler messageHandler)
            throws ModuleConnectionConfigurationException {

        try {

            if (logger.isInfoEnabled()) {
                logger.info("Initializing module connection for " + senderName);
            }

            //
            // create decoded message queue handler
            //
            Runnable listenRunnable = new Runnable() {
                @Override
                public void run() {
                    handleDecodedMessage(messageHandler);
                }
            };

            // The thread is used to pull decoded messages from the decoded
            // queue.
            new Thread(listenRunnable, moduleQueueName + " Decoded Message Listener").start();

            //
            // create queue client
            //
            moduleQueueClient = new QueueMessageClient(messageBrokerURI.toString(), moduleQueueName,
                    clearQueueOnStartup);
            moduleQueueClient.addConnectionStatusListener(this);
            moduleQueueClient.setMessageHandler(new RawMessageHandler() {
                private long TOO_LONG = 500;

                private long beforeDecodeTime;

                private long afterDecodeTime;

                @Override
                public boolean processMessage(String msg, MessageEncodingTypeEnum encodingType) {

                    if(shuttingDown){
                        //don't process a message if the module is shutting down
                        return true;
                    }

                    Message message = null;
                    try {

                        beforeDecodeTime = System.currentTimeMillis();
                        message = MessageUtil.getMessageFromString(msg, encodingType);
                        afterDecodeTime = System.currentTimeMillis();

                        if (afterDecodeTime - beforeDecodeTime > TOO_LONG) {
                            logger.warn("It took " + (afterDecodeTime - beforeDecodeTime) + " ms to decode the " + message.getMessageType() + " message of:\n"+message);
                        }
                    } catch (MessageDecodeException e) {
                        //send NACK
                        NACK nack = new NACK(ErrorEnum.MALFORMED_DATA_ERROR, "Unable to decode message string into a GIFT message");
                        sendReply(message, nack, MessageTypeEnum.NACK, null);

                        if (e instanceof PayloadDecodeException) {
                            //notify message collections that can be found by using

                            handleExceptionWithMessageCollection((PayloadDecodeException) e);
                        }

                        //push exception up the stack
                        throw e;
                    }

                    handleIncomingMessage(message);
                    
                    return true;
                }
            });

            if (!moduleQueueClient.connect()) {
                logger.error("Failed to connect to queue = " + moduleQueueName + ".");
            }
        } catch (Exception e) {
            throw new ModuleConnectionConfigurationException("Unable to establish a network connection to the GIFT message bus. Is ActiveMQ running?",
                    e.getMessage(),
                    e);
        }
    }

    /**
     * Handle the incoming message
     * 
     * @param message the incoming message to handle
     */
    private void handleIncomingMessage(Message message) {
        /* Only ACK those messages expecting an ACK reply */
        if (message.needsHandlingResponse()) {

            /* Send ACK */
            boolean sent = sendReply(message, new ACK(), MessageTypeEnum.ACK, null);

            if (logger.isInfoEnabled()) {
                logger.info(String.format("Replying with ACK to message #%d of type '%s' %s successful",
                        message.getSequenceNumber(), message.getMessageType().toString(), sent ? "was" : "wasn't"));
            }
        }

        // Handle ACK/NACK messages on their own queue to give them higher
        // priority
        if (message.getMessageType() == MessageTypeEnum.ACK || message.getMessageType() == MessageTypeEnum.NACK) {
            // place decoded message in queue and notify listener
            synchronized (decodedMessageMutex) {

                decodedACKs.add(message);

                decodedMessageMutex.notifyAll();
            }

        } else {
            // place decoded message in queue and notify listener
            synchronized (decodedMessageMutex) {

                decodedMessageList.add(message);

                decodedMessageMutex.notifyAll();
            }
        }
    }

    /**
     * This method is responsible for continually pulling messages from the decoded message queue.
     *
     * @param messageHandler - the message handler to provided messages too
     */
    private void handleDecodedMessage(final MessageHandler messageHandler){

        Message message = null;
        do {

            synchronized (decodedMessageMutex) {

                if (decodedMessageList.isEmpty() && decodedACKs.isEmpty()) {

                    try {
                        //wait until notified of a new message or when shutting down
                        decodedMessageMutex.wait();

                    } catch (@SuppressWarnings("unused") InterruptedException e) {
                    }
                }

                //
                // Select the next decoded message to handle
                // - Give ACK/NACK messages precedence to help prevent message collection ACK timeouts
                //
                //TODO: pulling all available messages inside a single synchronized block
                //      may be more efficient
                message = decodedACKs.isEmpty() ? decodedMessageList.isEmpty() ? null : decodedMessageList.remove(0) : decodedACKs.remove(0);
//                message = decodedMessageList.isEmpty() ? null : decodedMessageList.remove(0);
            }

            if(message != null){

                try{

//                    long before = System.currentTimeMillis();
                    boolean handled = handleMessageWithMessageCollection(message);

                    if (!handled) {
                        messageHandler.processMessage(message);
                    }

//                    long after = System.currentTimeMillis();
//                    if((after - before) > 1000){
//                        logger.error("It took too long! "+(after - before)+", "+message);
//                    }

                }catch(Exception e){
                    logger.error("Caught exception while handling decoded message, therefore the message will be dropped.  The message is "+message, e);

                    sendReply(message, new NACK(ErrorEnum.OPERATION_FAILED, "Caught exception while trying to process decoded message."), MessageTypeEnum.PROCESSED_NACK, null);
                }
            }

        } while (!shuttingDown);

        if(logger.isInfoEnabled()){
            logger.info("Ending the handling of decoded messages for sender named "+senderModuleType);
        }

    }

//    /**
//     * Add the connection listener to the message client of the specified module type.
//     *
//     * @param moduleType
//     * @param listener
//     */
//    public void addModuleConnectionListener(ModuleTypeEnum moduleType, MessageClientConnectionListener listener){
//
//        MessageClient client = getMessageClient(moduleType);
//        if(client != null){
//            client.addConnectionStatusListener(listener);
//        }else{
//            logger.error("Unable to add module connection listener for module type = "+moduleType+" because there is no corresponding client connection");
//        }
//    }
    /**
     * Return whether or not a message of the given type should be delivered to
     * the module after being provided to the message collections which didn't
     * handle it.
     *
     * @param messageType the message type to check.
     * @return boolean true if the message type should be ignored; false otherwise.
     */
    private static boolean shouldIgnore(MessageTypeEnum messageType) {
        return messageType == MessageTypeEnum.PROCESSED_ACK || messageType == MessageTypeEnum.PROCESSED_NACK
                || messageType == MessageTypeEnum.ACK || messageType == MessageTypeEnum.NACK;
    }

    /**
     * Return whether a client for the subject has already been created.
     *
     * @param subjectName - the subject name to find an existing client of
     * @return boolean - true iff a client has been created for that subject
     */
    public boolean haveConnection(String subjectName) {
        return subjectToClient.containsKey(subjectName);
    }

    /**
     * Return whether a client for the module type has already been created.
     *
     * @param userSession - learner unique id, used to find module connection for, or
     * allocated to, a particular user
     * @param moduleType - the module type to find an existing client of
     * @return boolean - true iff a client has been created for that module type
     */
    public boolean haveConnection(UserSession userSession, ModuleTypeEnum moduleType) {

        String subject = getConnectionName(userSession, moduleType);

        if (subject == null) {

            //I don't like doing this but it works for now...
            //The UMS and LMS are the only modules used before a User session is created, therefore they won't
            //have mappings to user ids
            //The Domain module will also be used before a User session is created when initializing an experiment, therefore
            //it won't have mappings to user ids in such cases either
            if (moduleType == ModuleTypeEnum.UMS_MODULE || moduleType == ModuleTypeEnum.LMS_MODULE || senderModuleType == ModuleTypeEnum.TUTOR_MODULE && moduleType == ModuleTypeEnum.DOMAIN_MODULE) {
                List<String> subjects = moduleTypeToSubjects.get(moduleType);

                //just choose the first one...
                if (subjects != null && !subjects.isEmpty()) {
                    subject = subjects.get(0);
                }
            }

        }
        return haveConnection(subject);
    }

    /**
     * Return the collection of module types for which there are client
     * connections.
     *
     * @return Set<ModuleTypeEnum>
     */
    public Set<ModuleTypeEnum> getConnectedModuleTypes() {
        return moduleTypeToSubjects.keySet();
    }

    /**
     * Return the collection of module addresses for which there are client
     * connections.
     *
     * @return Set<String>
     */
    public Set<String> getConnectedModuleAddresses() {
        return subjectToClient.keySet();
    }

    /**
     * Return the module connection addresses by the given module type. Some
     * modules can support multiple connections, therefore the list will contain
     * more than one entry.
     *
     * @param moduleType - the module type to check for connections too
     * @return List<String> - list of connections for that module type.  Can be null and can be empty
     */
    public List<String> getModuleAddresses(ModuleTypeEnum moduleType) {
        return moduleTypeToSubjects.get(moduleType);
    }

    /**
     * Remove a client for the subject.
     *
     * @param subjectName - the subject name to find an existing client of
     * @param disconnect - whether the client needs to be disconnected, i.e. no
     * longer listen for events from that client connection
     * @param destroyDestination - whether the destination should also be destroyed on the message bus.
     * Be careful as using this feature could have consequences on other parts of GIFT that might still need
     * that message bus.
     * @return boolean - true iff a client has been removed for that subject
     */
    public boolean removeConnection(String subjectName, boolean disconnect, boolean destroyDestination) {

        if(subjectName == null){
            throw new IllegalArgumentException("Can't remove a client connection with a null subject name");
        }

        MessageClient client;

        synchronized (subjectToClient) {

            client = subjectToClient.remove(subjectName);

            if(client != null){
                if(logger.isInfoEnabled()){
                    logger.info("Removed client connection named " + subjectName);
                }

                //cleanup module-subject mapping for this connection being removed
                synchronized (moduleTypeToSubjects) {
                    for(ModuleTypeEnum mType : moduleTypeToSubjects.keySet()){
                        List<String> subjects = moduleTypeToSubjects.get(mType);

                        if(subjects != null){
                            if(subjects.remove(subjectName)){
                                if(logger.isInfoEnabled()){
                                    logger.info("Removed mapping of module type "+mType+" to subject "+subjectName);
                                }
                                break;
                            }
                        }
                    }
                }

                //cleanup user mapped modules that mapped to this subject
                synchronized(compositeToSubject){

                    //gather keys that reference the subject being removed
                    List<CompositeUserModuleKey> keysToRemove = new ArrayList<>();
                    for(CompositeUserModuleKey key : compositeToSubject.keySet()){
                        String subject = compositeToSubject.get(key);

                        if(subjectName.equals(subject)){
                            keysToRemove.add(key);
                        }
                    }

                    //remove those keys from the underlying map
                    for(CompositeUserModuleKey key : keysToRemove){
                        compositeToSubject.remove(key);
                    }
                }
            }
        }

        if (disconnect) {

            if (client != null) {

                if(destroyDestination){
                    if(logger.isInfoEnabled()){
                        logger.info("Disconnecting and destroying connection to "+client);
                    }
                    client.disconnect(true);
                    synchronized(otherClientsToDestroy) {
                        otherClientsToDestroy.remove(client);
                    }
                }else{
                    if(logger.isInfoEnabled()){
                        logger.info("Disconnecting (but not destroying) connection to "+client);
                    }
                    client.disconnect(false);
                }

                return true;

            } else if(destroyDestination){
                //there isn't a client connection but the caller wishes
                //to destroy the destination (if exists) on the message bus

                if(logger.isInfoEnabled()){
                    logger.info("Attempting to destroy the message bus destination of '"+subjectName+"' without a client connection to that destination.");
                }
                MessageClient.attemptDestroy(subjectName, null);

            }


        } else {

            return true;
        }

        return false;
    }

    /**
     * Remove a user connection for a client for the module type. If no user connections remain for the client after a user connection has
     * been removed, then the client connection will be removed as well.
     *
     * @param userSession - session identifier for a user, used to find module connection for, or
     * allocated to, a particular user
     * @param moduleType - the module type to find an existing client of
     * @param disconnect - whether the user needs to be disconnected, i.e. no
     * longer listen for events from that client connection
     * @param destroy - whether the destination should also be destroyed on the message bus.
     * Be careful as using this feature could have consequences on other parts of GIFT that might still need
     * that message bus.
     * @return boolean - true iff a client has been removed for that module type
     */
    public boolean removeUserConnection(UserSession userSession, ModuleTypeEnum moduleType, boolean disconnect, boolean destroy) {

        boolean removed = false;

        //remove the user connection and get it's associated client
        String subject = getConnectionName(userSession, moduleType, true);

        if(!compositeToSubject.containsValue(subject)){
            //only remove the client connection itself if all of its users have been disconnected

            if (subject != null && removeConnection(subject, disconnect, destroy)) {

                synchronized (moduleTypeToSubjects) {

                    List<String> subjects = moduleTypeToSubjects.get(moduleType);

                    if (subjects != null) {
                        removed = subjects.remove(subject);
                    }
                }
            }
        }

        return removed;
    }

    /**
     * Return the connection name associated with the user id and module type
     * provided.
     *
     * @param userSession - the learner's user id, used as an additional key for
     * allocation and grouping of modules supporting a user during the session
     * @param moduleType - module type to find an existing connection for
     * @param removeEntry - whether or not to also remove the entry in the map
     * @return String - a client connection name, null if none was found
     */
    private String getConnectionName(UserSession userSession, ModuleTypeEnum moduleType, boolean removeEntry) {

        if (userSession == null) {
            logger.warn("getConnectionName() called with a null userSession.");
            return null;
        }

        synchronized (compositeToSubject) {


            lookupKey.setUserId(userSession.getUserId());
            lookupKey.setExperimentId(userSession.getExperimentId());
            lookupKey.setGlobalUserId(userSession.getGlobalUserId());
            lookupKey.setModuleType(moduleType);

            if (removeEntry) {

                if(logger.isInfoEnabled()){
                    logger.info("Removing mapping of message client connection with user = " + userSession + " and moduleType = " + moduleType);
                }
                String subject = compositeToSubject.remove(lookupKey);
                if(logger.isInfoEnabled()){
                    logger.info("Found subject of " + subject + " in map of size " + compositeToSubject.size() + " using composite key of " + lookupKey);
                }
                return subject;

            } else {

                if (!compositeToSubject.containsKey(lookupKey)) {
                    //I don't like doing this but it works for now...
                    //The UMS and LMS are the only modules used before a User session is created, therefore they won't
                    //have mappings to user ids
                    //The Domain module will also be used before a User session is created when initializing an experiment, therefore
                    //it won't have mappings to user ids in such cases either
                    //The Gateway module will also be used without a user session when the web monitor sends entity states
                    //to training applications while playing back a session
                    if (moduleType == ModuleTypeEnum.UMS_MODULE || moduleType == ModuleTypeEnum.LMS_MODULE
                            || senderModuleType == ModuleTypeEnum.TUTOR_MODULE && moduleType == ModuleTypeEnum.DOMAIN_MODULE
                            || senderModuleType == ModuleTypeEnum.MONITOR_MODULE && moduleType == ModuleTypeEnum.GATEWAY_MODULE) {

                        synchronized (moduleTypeToSubjects) {

                            List<String> subjects = moduleTypeToSubjects.get(moduleType);

                            //just choose the first one...
                            if (subjects != null && !subjects.isEmpty()) {

                                return subjects.get(0);
                            }
                        }
                    }
                }

                return compositeToSubject.get(lookupKey);
            }
        }
    }

    /**
     * Return the connection name associated with the user id and module type
     * provided.
     *
     * @param userSession - the learner's user id, used as an additional key for
     * allocation and grouping of modules supporting a user during the session
     * @param moduleType - module type to find an existing connection for
     * @return String - a client connection name, null if none was found
     */
    private String getConnectionName(UserSession userSession, ModuleTypeEnum moduleType) {
        return getConnectionName(userSession, moduleType, false);
    }

    /**
     * Map a module of particular type with the given queue name to a user.
     *
     * @param key - a composite key associating a user and module type to a module queue
     * @param moduleQueueName - the module queue name
     */
    private void mapModuleToUser(CompositeUserModuleKey key, String moduleQueueName) {

        if(logger.isInfoEnabled()){
            logger.info("Mapping module w/ queue name " + moduleQueueName + " to user based on key: " + key);
        }

        synchronized (compositeToSubject) {

            compositeToSubject.put(key, moduleQueueName);
        }
    }

    /**
     * Return a new message client instance for a known module of the given type
     * based on search criteria (IP Address, FIFO).
     *
     * @param userSession information about the user session the module client is being created for
     * @param connectionFilter - contains filter logic when looking for a module client
     * @param moduleType - the type of module creating a client connection too,
     * used to find the list of known modules of a given type
     * @param destroyClientOnCleanup - whether the destination should be added to the destinations to destroy by this module's network
     * session instance.
     * Be careful as using this feature could have consequences on other parts of GIFT that might still need
     * that message bus.
     * @return MessageClient - new instance, or null if an error occurred while creating the module client.
     */
    private MessageClient createModuleClient(UserSession userSession, ConnectionFilter connectionFilter, ModuleTypeEnum moduleType, boolean destroyClientOnCleanup) {

        List<ModuleStatus> mStatusList = moduleStatusMonitor.getLastStatus(moduleType);

        if (mStatusList == null || mStatusList.isEmpty()) {
            logger.error("This module has no up-to-date module status for a module of type = " + moduleType + ", therefore unable to send a module allocation request");
            return null;
        }

        //
        // Select module instance to connect too based on
        //      1: connection filter (if ipaddrFilter is on)
        //      2: IP addr
        //      3: FIFO
        //

        MessageClient client = null;

        if(ipaddrFilterOn && connectionFilter != null){
            //by connection filter...
            //Note: if the ip address filter is off then the connection filter shouldn't be used since it filters by ip addresses

            if(logger.isInfoEnabled()){
                logger.info("Searching for module of type "+moduleType+" by connection filter of "+connectionFilter);
            }

            for (int i = 0; i < mStatusList.size(); i++) {

                ModuleStatus mStatus = mStatusList.get(i);
                if(!connectionFilter.accept(mStatus)){
                    if(logger.isInfoEnabled()){
                        logger.info("Removing "+mStatus+" from potential connection matches for "+connectionFilter);
                    }
                    mStatusList.remove(i);
                    i--;
                }
            }
        }

        if(mStatusList.size() == 1 && !connectionFilter.isIgnoreAddress(mStatusList.get(0).getQueueName())){
            //found one and only client to choose from and its not on the ignore list

            if(logger.isInfoEnabled()){
                logger.info("Creating client connection based on connection filter for module with status = "+mStatusList.get(0));
            }

            client = createSubjectQueueClient(mStatusList.get(0), destroyClientOnCleanup);

            if (client != null && userSession.getUserId() != UserSessionMessage.PRE_USER_UNKNOWN_ID) {
                //add mapping

                CompositeUserModuleKey compositeKey = new CompositeUserModuleKey(userSession.getUserId(), mStatusList.get(0).getModuleType());
                compositeKey.setExperimentId(userSession.getExperimentId());
                compositeKey.setGlobalUserId(userSession.getGlobalUserId());
                mapModuleToUser(compositeKey, mStatusList.get(0).getQueueName());
            }

        }else{

            if(logger.isInfoEnabled()){
                logger.info("Searching for module of type "+moduleType+" in remaining list of "+mStatusList.size()+" known modules by using IP address filtering based on {"+Util.getLocalAddresses()+"}.");
            }

            //by IP addr...
            for (ModuleStatus mStatus : mStatusList) {

                String queueName = mStatus.getQueueName();
                if (connectionFilter != null && !connectionFilter.accept(mStatus)) {
                    //didn't pass filter test
                    continue;

                } else if (/*ipaddrFilterOn &&*/ isLocalQueue(queueName)) {
                    //if the module is on the same host as this module add it to the list of available modules

                    if(logger.isInfoEnabled()){
                        logger.info("Creating client connection for localhost module with status = "+mStatus);
                    }

                    client = createSubjectQueueClient(mStatus, destroyClientOnCleanup);

                    if (client != null && userSession.getUserId() != UserSessionMessage.PRE_USER_UNKNOWN_ID) {
                        //add mapping
                        CompositeUserModuleKey compositeKey = new CompositeUserModuleKey(userSession.getUserId(), mStatus.getModuleType());
                        compositeKey.setExperimentId(userSession.getExperimentId());
                        compositeKey.setGlobalUserId(userSession.getGlobalUserId());
                        mapModuleToUser(compositeKey, mStatus.getQueueName());
                        break;
                    }
                }

            }//end for

            if (client == null) {

                if(logger.isInfoEnabled()){
                    logger.info("Searching for module of type "+moduleType+" in remaining list of "+mStatusList.size()+" known modules by using FIFO logic.");
                }

                //by FIFO...
                for (ModuleStatus mStatus : mStatusList) {

                    if (connectionFilter != null && !connectionFilter.accept(mStatus)) {
                        //didn't pass filter test
                        continue;

                    } else {

                        if(logger.isInfoEnabled()){
                            logger.info("Creating client connection for module with status = "+mStatus);
                        }

                        //for now, just use first module in list (that succeeds of course)
                        client = createSubjectQueueClient(mStatus, destroyClientOnCleanup);

                        if (client != null && userSession.getUserId() != UserSessionMessage.PRE_USER_UNKNOWN_ID) {
                            //add mapping
                            CompositeUserModuleKey compositeKey = new CompositeUserModuleKey(userSession.getUserId(), mStatus.getModuleType());
                            compositeKey.setExperimentId(userSession.getExperimentId());
                            compositeKey.setGlobalUserId(userSession.getGlobalUserId());

                            mapModuleToUser(compositeKey, mStatus.getQueueName());
                            break;
                        }
                    }

                }//end for
            }
        }

        if(client == null){
            StringBuffer sb = new StringBuffer();
            sb.append("Failed to create a message client for a module of type ").append(moduleType).append(" from known modules of that type and ignore list: {");

            if(connectionFilter != null && connectionFilter.getIgnoreAddresses() != null){

                for(String address : connectionFilter.getIgnoreAddresses()){
                    sb.append(address).append(", ");
                }
            }
            sb.append("}");
            logger.warn(sb.toString());
        }

        return client;

    }

    /**
     * Sets up and returns a module client for a known module of the given type
     * based on search criteria (IP Address, FIFO).
     *
     * Note: This method assumes that the given module type has already been allocated for this network session's module and should
     * only be used to create module clients for user sessions that are redirected to a different module address (for instance,
     * when a Domain module creates an experiment subject for the Tutor and then needs to redirect its messages to
     * the tutor client for that subject).
     *
     * @param userSession the user session to establish a module client for
     * @param moduleType the type of module to establish a client to
     * @param filter a filter used to determine what module address to establish a client for
     */
    public void linkModuleClient(UserSession userSession, ConnectionFilter filter, ModuleTypeEnum moduleType){
        createModuleClient(userSession, filter, moduleType, false);
    }

    /**
     * Whether the GIFT module queue name provided contains one of the IP addresses of this machine that
     * is running the JVM with this Network Session instance.
     * For example if this network session is for the Domain module (IP addresses of 127.0.0.1, 10.1.21.160, 192.168.1.31)
     * and the queue name is "Gateway_10.1.21.160:INBOX", the method would return true.
     *
     * @param queueName the GIFT module queue name to check
     * @return true iff the queue name is deemed a local machine queue
     */
    private boolean isLocalQueue(String queueName){

        for(InetAddress address : Util.getLocalAddresses()){

            if(queueName.contains(address.getHostAddress())){
                return true;
            }
        }

        return false;
    }

    /**
     * Return whether this module already has a connection to the requesting
     * module, and that connection is mapped to the specified user.
     *
     * @param moduleAllocationRequestMessage - the request to check
     * @return boolean - whether this module is already allocated to the user
     */
    public boolean isAlreadyAllocated(Message moduleAllocationRequestMessage) {

        ModuleAllocationRequest request = (ModuleAllocationRequest) moduleAllocationRequestMessage.getPayload();

        if (moduleAllocationRequestMessage instanceof UserSessionMessage) {
            UserSession userSession = ((UserSessionMessage) moduleAllocationRequestMessage).getUserSession();
            String subject = getConnectionName(userSession, request.getRequestorInfo().getModuleType());

            if (subject != null) {
                //found a connection for the requesting module and it is mapped to the specified user

                return true;
            }
        }

        return false;
    }

    /**
     * Create message clients for the modules referenced in the collection that
     * are needed by this module.
     *
     * @param moduleAllocationRequestMessage - an allocation request that will be satisfied by this module and which may contain
     *                       other module connections that this module should use as well.
     * @param isServerMode whether this network session's module is in sever deployment mode.  This is currently used to add
     * the gateway module to the list of message bus destinations to be destroyed by this network session.
     */
    public void useSameModules(Message moduleAllocationRequestMessage, boolean isServerMode) {

        ModuleAllocationRequest request = (ModuleAllocationRequest) moduleAllocationRequestMessage.getPayload();

        int userId = UserSessionMessage.PRE_USER_UNKNOWN_ID;
        String experimentId = null;
        Integer globalUserId = null;
        if (moduleAllocationRequestMessage instanceof UserSessionMessage) {
            userId = ((UserSessionMessage) moduleAllocationRequestMessage).getUserId();
            experimentId = ((UserSessionMessage) moduleAllocationRequestMessage).getExperimentId();
            globalUserId = ((UserSessionMessage) moduleAllocationRequestMessage).getUserSession().getGlobalUserId();
        }

        if(logger.isInfoEnabled()){
            logger.info("Creating needed message clients so that this module is using the same modules as the requesting module.");
        }

        //for now use all allocated modules
        for (ModuleStatus mStatus : request.getAllocatedModules().values()) {

             if (mStatus.getModuleType() != senderModuleType) {

                 if(logger.isInfoEnabled()){
                     logger.info("Adding module connection for " + mStatus + " from module request list of allocated modules");
                 }

                MessageClient client = null;

                if(!haveConnection(mStatus.getQueueName())){
                    if(logger.isInfoEnabled()){
                        logger.info("Creating connection to "+mStatus.getQueueName());
                    }
                    client = createSubjectQueueClient(mStatus.getQueueName(), mStatus.getModuleType(), false);
                } else {
                    client = getMessageClient(mStatus.getQueueName());
                }

                if (client != null
                        && userId != UserSessionMessage.PRE_USER_UNKNOWN_ID) {
                    CompositeUserModuleKey compositeKey = new CompositeUserModuleKey(userId, mStatus.getModuleType());
                    compositeKey.setExperimentId(experimentId);
                    compositeKey.setGlobalUserId(globalUserId);
                    mapModuleToUser(compositeKey, mStatus.getQueueName());
                }

                //setup GW topic connection if the module registered a simulation message handler,
                //otherwise receiving GW topic messages (i.e. training app game state) isn't needed
                if(mStatus.getModuleType() == ModuleTypeEnum.GATEWAY_MODULE && simulationMessageHandler != null){

                    GatewayModuleStatus gwStatus = (GatewayModuleStatus)mStatus;
                    if(!haveConnection(gwStatus.getTopicName())){
                        if(logger.isInfoEnabled()){
                            logger.info("Creating "+gwStatus.getTopicName()+" connection in order to receive simulation messages.");
                        }

                        createSubjectTopicClient(gwStatus.getTopicName(), simulationMessageHandler, isServerMode);
                        if(logger.isInfoEnabled()){
                            logger.info("Successfully created connection to receive simulation messages from "+gwStatus.getTopicName());
                        }

                        //save the user's gateway topic in order to retrieve the gateway topic
                        //when cleaning up for a domain session
                        CompositeUserKey compositeKey = new CompositeUserKey(userId);
                        compositeKey.setExperimentId(experimentId);
                        compositeKey.setGlobalUserId(globalUserId);
                        synchronized (userToGatewayTopic) {
                            String oldTopicName = userToGatewayTopic.put(compositeKey, gwStatus.getTopicName());

                            if(oldTopicName != null && !oldTopicName.equals(gwStatus.getTopicName())){
                                //there is a gateway topic still associated with that user
                                //clean that connection up
                                if(logger.isInfoEnabled()){
                                    logger.info("Removing old connection to Gateway Topic named '"+oldTopicName+"' for "+compositeKey+" because that user is now associated with the Gateway Topic '"+gwStatus.getTopicName()+"'.");
                                }
                                removeConnection(oldTopicName, true, isServerMode);
                            }
                        }

                    }
                }
            }
        }

        //don't forget to map connection to requesting module
        //Note: the client has already been created at this point because the module allocation request was ACK'ed
        if(logger.isInfoEnabled()){
            logger.info("Adding module connection for " + request.getRequestorInfo() + " from REQUESTING module's request");
        }
        MessageClient client = getMessageClient(request.getRequestorInfo().getQueueName());

        if (client != null) {

            if(userId != UserSessionMessage.PRE_USER_UNKNOWN_ID){
                // (should be) a valid user id that is starting a course
                CompositeUserModuleKey compositeKey = new CompositeUserModuleKey(userId, request.getRequestorInfo().getModuleType());
                compositeKey.setExperimentId(experimentId);
                compositeKey.setGlobalUserId(globalUserId);
                mapModuleToUser(compositeKey, request.getRequestorInfo().getQueueName());
            }else if(request.getRequestorInfo().getModuleType() != ModuleTypeEnum.TUTOR_MODULE &&
                    request.getRequestorInfo().getModuleType() != ModuleTypeEnum.DOMAIN_MODULE &&
                    request.getRequestorInfo().getModuleType() != ModuleTypeEnum.GATEWAY_MODULE){
                // these modules are the only modules that can request module allocation in the architecture
                // The tutor module normally allocates UMS, LMS and Domain when in Course Lesson level mode
                // The domain module allocates Sensor, Learner, Ped and Gateway modules (always)
                // The Gateway module allocates UMS, LMS and Domain when in RTA lesson level mode.
                logger.error("Failed to create map of {userId:moduleType} for "+request.getRequestorInfo()+" because a user id was not provided.");
            }
        }else{
            // this is most likely caused because the module that owns this NetworkSession instance needs to
            // listen to the heartbeat topic for the module type of the queue name.
            logger.error("Failed to create map of {userId:moduleType} for "+request.getRequestorInfo()+" because a client could not be found/created.");
        }

    }

    /**
     * Remove all the client connections for the domain session linked (i.e.
     * grouped, allocated together for the user) modules
     *
     * @param userSession - the key that links all of the allocated modules for a
     * user whose domain session is ending
     * @param isServerMode whether this network session's module is in sever deployment mode.  This is currently used to add
     * the gateway module to the list of message bus destinations to be destroyed by this network session.
     */
    public void releaseDomainSessionModules(UserSession userSession, boolean isServerMode) {

        if(logger.isInfoEnabled()){
            logger.info("Releasing domain session allocated modules for user " + userSession);
        }

        //Note: just because a particular module type is listed here for connection removal, doesn't mean this module (i.e. the module with this network
        //session instance) has that connection.  For now this is just a quick solution for all modules to use.
        removeUserConnection(userSession, ModuleTypeEnum.PEDAGOGICAL_MODULE, true, false);
        removeUserConnection(userSession, ModuleTypeEnum.SENSOR_MODULE, true, false);
        removeUserConnection(userSession, ModuleTypeEnum.LEARNER_MODULE, true, false);
        removeUserConnection(userSession, ModuleTypeEnum.GATEWAY_MODULE, true, isServerMode);
        removeUserConnection(userSession, ModuleTypeEnum.TUTOR_MODULE, true, false);

        //make sure to cleanup any gateway topic connection as well, otherwise the message client listener thread will cause a leak
        removeGatewayTopicConnection(userSession, isServerMode);
    }

    /**
     * Remove all the client connections for the user session linked (i.e.
     * grouped, allocated together for the user) modules
     *
     * @param userSession - the key that links all of the allocated modules for a
     * user whose domain session is ending
     * @param isServerMode whether this network session's module is in sever deployment mode.  This is currently used to add
     * the gateway module to the list of message bus destinations to be destroyed by this network session.
     */
    public void releaseUserSessionModules(UserSession userSession, boolean isServerMode){

        releaseDomainSessionModules(userSession, isServerMode);
        removeUserConnection(userSession, ModuleTypeEnum.TUTOR_MODULE, true, false);
    }

    /**
     * Remove the client connection to a Gateway module Topic for the user specified.
     *
     * @param userSession contains information about a user session used to lookup the associated Gateway module topic, if any.
     * @param isServerMode whether this network session's module is in server deployment mode.  This is currently used to
     * destroy the message client connected to the Gateway module topic.
     */
    private void removeGatewayTopicConnection(UserSession userSession, boolean isServerMode){

        synchronized(userToGatewayTopic){

            CompositeUserKey topicLookupKey = new CompositeUserKey(userSession.getUserId());
            topicLookupKey.setExperimentId(userSession.getExperimentId());
            topicLookupKey.setGlobalUserId(userSession.getGlobalUserId());

            String topicName = userToGatewayTopic.remove(topicLookupKey);
            if(topicName != null){
                removeConnection(topicName, true, isServerMode);
            }
        }
    }

    /**
     * Request that a UMS module be allocated to this module by sending a request message to a module of that type.
     * If the request fails or succeeds the callback will be notified. This method should only be used for module allocation
     * when a user id is not known (e.g. create new user request).
     *
     * @param selectionCallback - The message callback for allocation message
     * @param requestingModulesStatus The module status of the module allocating
     * the specified module type
     */
    public void selectUMSModule(final MessageCollectionCallback selectionCallback, ModuleStatus requestingModulesStatus){
        selectModule(new UserSession(UserSessionMessage.PRE_USER_UNKNOWN_ID),
                ModuleTypeEnum.UMS_MODULE, selectionCallback, requestingModulesStatus, new ConnectionFilter(), false);
    }

    /**
     * Request that a Domain module be allocated to this module by sending a request message to a module of that type.
     * If the request fails or succeeds the callback will be notified. This method should only be used for module allocation
     * when a user id is not known (e.g. start experiment course request).
     *
     * NOTE: This method will throw an UnsupportedOperationException if the sender module invoking it is not the Tutor module
     *
     * @param selectionCallback - The message callback for allocation message
     * @param requestingModulesStatus The module status of the module allocating
     * the specified module type
     * @throws UnsupportedOperationException if the sender module invoking this method is not the Tutor module
     */
    public void selectDomainModule(final MessageCollectionCallback selectionCallback, ModuleStatus requestingModulesStatus) throws UnsupportedOperationException{

        if (senderModuleType == ModuleTypeEnum.TUTOR_MODULE || senderModuleType == ModuleTypeEnum.GATEWAY_MODULE) {
            // user starts course from tutor (sometimes via Dashboard) when in course lesson level setting
            // user starts course from gateway when in RTA lesson level setting
            selectModule(new UserSession(UserSessionMessage.PRE_USER_UNKNOWN_ID),
                    ModuleTypeEnum.DOMAIN_MODULE, selectionCallback, requestingModulesStatus, new ConnectionFilter(), false);

        } else {

            UnsupportedOperationException e = new UnsupportedOperationException("Unable to select a Domain module for module of type");

            logger.error("An exception occurred while selecting a Domain module.", e);

            throw e;
        }
    }
    
    /**
     * Create a new connection filter instance using the web client information provided.
     * @param clientInfo if null this method returns a new, default connection filter instance.  If this
     * contains a client address that IP address will be used as a required address in the returned filter.
     * @return a new connection filter.
     */
    public static ConnectionFilter createConnectionFiler(WebClientInformation clientInfo){
        
        ConnectionFilter filter = new ConnectionFilter();
        if(clientInfo != null && clientInfo.getClientAddress() != null){
            filter.addRequiredAddress(clientInfo.getClientAddress());

            // If the client address is a "local address", then we want the filter to contain all possible
            // values of what a local ip address can be since the tutor could pass in an address such as 127.0.0.1, but
            // the gateway module have an actual physical ip of the host such as 10.1.1.1.
            if (Util.isLocalAddress(clientInfo.getClientAddress())) {

                filter.addAllRequiredAddresses(Util.getLocalAddresses());
            }
        }
        
        return filter;
    }
    
    /**
     * Create a new connection filter instance using the required IP address provided.
     * @param requiredIpAddress if null or empty a new, default connection filter instance is returned.
     * If populated, that address will be set as a required address in the filter.  If the address
     * is a local address any other local addresses will also be added to the required address list.
     * @return a new connection filter
     */
    public static ConnectionFilter createConnectionFilter(String requiredIpAddress){
        
        ConnectionFilter filter = new ConnectionFilter();
        if(StringUtils.isNotBlank(requiredIpAddress)){
            filter.addRequiredAddress(requiredIpAddress);
            
            // If the client address is a "local address", then we want the filter to contain all possible
            // values of what a local ip address can be since the tutor could pass in an address such as 127.0.0.1, but
            // the gateway module have an actual physical ip of the host such as 10.1.1.1.
            if (Util.isLocalAddress(requiredIpAddress)) {
                filter.addAllRequiredAddresses(Util.getLocalAddresses());
            }
        }
        
        return filter;
    }

    /**
     * Request that the module represented by the provided module status is allocated to this module by
     * sending a request message to that module. If the request fails or
     * succeeds the callback will be notified.
     *
     * @param userSession - information about the user session (including the unique user id of the learner)
     *                      used for allocation and grouping of modules supporting this user during the session
     * @param moduleType - the module type that a connection is needed for
     * @param selectionCallback - the message callback for allocation message
     * related events
     * @param requestingModulesStatus The module status of the module allocating
     * the specified module type
     * @param requiredModuleStatus information about the module to select
     * @param isServerMode whether this network session's module is in sever deployment mode.  This is currently used to add
     * the gateway module to the list of message bus destinations to be destroyed by this network session.
     */
    public void selectModule(final UserSession userSession, final ModuleTypeEnum moduleType,
            final MessageCollectionCallback selectionCallback, final ModuleStatus requestingModulesStatus,
            final ModuleStatus requiredModuleStatus, boolean isServerMode) {

        ConnectionFilter filter = new ConnectionFilter();
        if(requiredModuleStatus != null){
            filter.setRequiredModule(requiredModuleStatus);
        }

        selectModule(userSession, moduleType, selectionCallback, requestingModulesStatus, filter, isServerMode);
    }

    /**
     * Request that a module of the given type is allocated to this module by
     * sending a message to a module of the given type. If the request fails or
     * succeeds the callback will be notified.
     *
     * @param userSession - information about the user session (including the unique user id of the learner)
     *                      used for allocation and grouping of modules supporting this user during the session
     * @param moduleType - the module type that a connection is needed for
     * @param selectionCallback - the message callback for allocation message
     * related events
     * @param requestingModulesStatus The module status of the module allocating
     * the specified module type
     * @param filter - connection filter information to use to help select the appropriate connection to setup, can be null
     * @param destroyConnectionOnCleanup whether the connection being established by this 'select' request should have its corresponding
     * message bus destination (e.g. topic) destroyed by this network session in the future.
     */
    public void selectModule(final UserSession userSession, final ModuleTypeEnum moduleType,
            final MessageCollectionCallback selectionCallback, final ModuleStatus requestingModulesStatus,
            final ConnectionFilter filter, boolean destroyConnectionOnCleanup) {

        if (!haveConnection(userSession, moduleType)) {
            //don't already have a connection to a module of this type mapped to this user, create new connection (if needed, or use existing client)

            if(logger.isInfoEnabled()){
                logger.info("Don't already have a "+moduleType+" connection mapped to user "+userSession+", therefore attempting to create one...");
            }            
            
            ConnectionFilter extendedFilter = new ConnectionFilter(filter);
            if(moduleType == ModuleTypeEnum.DOMAIN_MODULE){
                // create filter so the Domain module is on the same computer as the UMS
                
                // #4917 - the domain and ums must be on the same computer. At this point which ever module (tutor or GW depending on GIFT config)
                //         is making this domain module selection should already have a UMS connection.

                ModuleStatus umsStatus = moduleMonitor.getLastStatus(ModuleTypeEnum.UMS_MODULE, getConnectionName(userSession, ModuleTypeEnum.UMS_MODULE));
                if(umsStatus != null){
                    
                    try{
                        // Currently the GIFT architecture places the ip address in the queue name so we can extract it.
                        // Ideally the ip addr would be its own variable in all module status messages.
                        String umsQueueName = umsStatus.getQueueName();
                        int beginIndex = umsQueueName.indexOf(AbstractModule.ADDRESS_TOKEN_DELIM)+1;
                        int endIndex = umsQueueName.lastIndexOf(AbstractModule.ADDRESS_TOKEN_DELIM);
                        String umsIpAddr = umsQueueName.substring(beginIndex, endIndex);
                        extendedFilter.addRequiredAddress(umsIpAddr);
                        
                        if(logger.isDebugEnabled()){
                            logger.debug("Adding IP address filter of "+umsIpAddr+" to Domain module selection so that it is on the same computer as the UMS");
                        }
                    }catch(Exception e){
                        logger.warn("Unable to extract the UMS module IP address from the UMS queue name of "+umsStatus.getQueueName()+
                                ".  Therefore the domain module can't be gauranteed to be on the same computer as the UMS module", e);
                    }

                }
                
            }


            MessageClient client = createModuleClient(userSession, extendedFilter, moduleType, destroyConnectionOnCleanup);
            if (client == null) {
                selectionCallback.failure("Failed to create a message client for a module of type " + moduleType + " from known modules of that type");
                return;
            }

        }else if(filter != null && filter.getIgnoreAddresses() != null && !filter.getIgnoreAddresses().isEmpty()){
            //make sure the current connection isn't on the ignore list

            String connectionName = getConnectionName(userSession, moduleType);
            if(filter.getIgnoreAddresses().contains(connectionName)){

                if(logger.isInfoEnabled()){
                    logger.info("Currently have a "+moduleType+" connection mapped to user "+userSession+", but that address of "+connectionName+
                        " is on the ignore list, therefore trying to select and connect to another module of that type");
                }

                MessageClient client = createModuleClient(userSession, filter, moduleType, destroyConnectionOnCleanup);
                if (client == null) {
                    selectionCallback.failure("There are no available " + moduleType.getDisplayName() + "s known to the "+this.senderModuleType.getDisplayName()+".");
                    return;
                }
            }
        }

        if(moduleType == ModuleTypeEnum.GATEWAY_MODULE){
            //setup GW module connection

            if(logger.isInfoEnabled()){
                logger.info("Before selecting a "+moduleType+", a connection to the simulation message topic must be established...");
            }

            ModuleStatus mStatus = moduleMonitor.getLastStatus(moduleType, getConnectionName(userSession, moduleType));
            if(mStatus != null){
                GatewayModuleStatus gwStatus = (GatewayModuleStatus)mStatus;

                List<String> clientAddress = filter != null ? filter.getRequiredAddress() : null;
                if(!ipaddrFilterOn || filter != null && (filter.isAddressInRequiredList(gwStatus.getTopicName()) || mStatus.equals(filter.getRequiredModule()))){
                    //the gateway module should be on the same machine as the tutor client because it usually controls
                    //the training application using things like auto-hot key.

                    if (!haveConnection(gwStatus.getTopicName())) {
                        //don't have connection currently, need to create connection

                        if(simulationMessageHandler != null){
                            createSubjectTopicClient(gwStatus.getTopicName(), simulationMessageHandler,
                                    destroyConnectionOnCleanup);
                            if(logger.isInfoEnabled()){
                                logger.info("Successfully created connection to receive simulation messages from "+gwStatus.getTopicName());
                            }
                        }else{
                            //ERROR - simulation message handler was not provided
                            selectionCallback.failure("A simulation message handler was not provided which is a requirement for "+moduleType+" connections");
                            return;
                        }
                    }
                }else{
                    //ERROR - if IP address filtering is on, then we don't support the GW module on workstation other than tutor client due to
                    //        GW ability to control training application

                    if(clientAddress != null && !clientAddress.isEmpty()){
                        selectionCallback.failure("This "+senderModuleType+" is configured for IP address filtering, meaning the "+moduleType+" must reside on the same workstation as the tutor client with address of "+clientAddress+".");
                    }else{
                        selectionCallback.failure("This "+senderModuleType+" is configured for IP address filtering, meaning the "+moduleType+" must reside on the same workstation as the tutor client.  However the tutor client address was not provided.");
                    }

                    return;
                }

            }else{
                //ERROR - need GW status to retrieve topic name simulation messages will be sent too
                selectionCallback.failure("Unable to retrieve a "+moduleType+" status, therefore unable to establish connection to retrieve simulation messages from it");
                return;
            }
            
        }

        //
        //build the allocated modules map to provide to the module being selected
        //Note: it might eventually be better to store this info somewhere, but this should only be called during init
        //

        //get all the modules types this module already has connections for.  Based on current connection containers
        //we need to start with module types, then filter by user id.
        Set<ModuleTypeEnum> connectedModules = getConnectedModuleTypes();
        Map<ModuleTypeEnum, ModuleStatus> allocatedModules = new HashMap<>();
        for (ModuleTypeEnum mType : connectedModules) {

            if (mType == requestingModulesStatus.getModuleType()) {
                //skip this module (i.e. itself) because it's info will be part of the message header
                continue;
            }else if(mType == ModuleTypeEnum.MONITOR_MODULE){
                //don't care about the monitor module (for now)
                continue;
            }

            //check if have module mapped to this user id
            String connectionName = getConnectionName(userSession, mType);
            if (connectionName == null) {
                //there is no module of this type allocated for this user id, therefore don't include it
                continue;
            }


            ModuleStatus mStatus = moduleStatusMonitor.getLastStatus(mType, connectionName);
            if (mStatus == null) {
                selectionCallback.failure("Unable to get the module status for module type = " + mType + " based on connection name of "+connectionName+", therefore unable to fully populate the allocated modules for the module allocation request");
                return;

            } else if (haveConnection(userSession, mType)) {
                //found module for which a connection already exists

                allocatedModules.put(mType, mStatus);
            }

        }

        //TODO: current solution is to send the request no matter if this module has the connection already or not, this is
        //      because there is currently no logic in place that handles cleanup/renew/release on allocated modules downstream.
        //      This means, for example, if the domain module needs the list of allocated modules from the Tutor and the tutor already
        //      has a domain client, simply not sending the request to the domain module won't allow the domain module to discover the
        //      tutor allocated modules to use.

        ModuleAllocationRequest request = new ModuleAllocationRequest(requestingModulesStatus);
        request.setAllocatedModule(allocatedModules);

        if(logger.isInfoEnabled()){
            logger.info("Requesting "+moduleType+" allocation = " + request);
        }

        //TODO: creating a connection and successfully allocating a module are two different things, need to address that
        MessageCollectionCallback requestCallback = new MessageCollectionCallback() {
            boolean failed;

            Message message;

            /** new filter instance to populate with allocation failed information */
            ConnectionFilter updatedFilter = new ConnectionFilter();

            @Override
            public void success() {

                if (failed) {
                    //try to find another module of the type

                    //add previously ignored addresses
                    if(filter != null){
                        updatedFilter.getIgnoreAddresses().addAll(filter.getIgnoreAddresses());
                        updatedFilter.addAllRequiredAddresses(filter.getRequiredAddress());
                    }

                    //add the sender of the rejected module allocated request to ignore list
                    updatedFilter.addIgnoreAddress(message.getSenderAddress());

                    //request another module be selected (if available)
                    selectModule(userSession, moduleType, this, requestingModulesStatus, updatedFilter, destroyConnectionOnCleanup);

                } else {
                    //notify calling method's callback
                    selectionCallback.success();
                }
            }

            @Override
            public void received(Message msg) {

                if (msg.getMessageType() == MessageTypeEnum.MODULE_ALLOCATION_REPLY) {
                    // received module allocation reply

                    message = msg;

                    //if unable to handle allocate, select different module
                    boolean isDenied = ((ModuleAllocationReply) msg.getPayload()).isDenied();
                    if (isDenied) {
                        logger.warn("Module allocation request was not satisfied because " + msg.getPayload());
                        failed = isDenied;
                    }
                }

            }

            @Override
            public void failure(String why) {

                //notify calling method's callback
                selectionCallback.failure(why);
            }

            @Override
            public void failure(Message msg) {

                //notify calling method's callback
                selectionCallback.failure(msg);
            }
        };


        sendMessage(moduleType, request, userSession, MessageTypeEnum.MODULE_ALLOCATION_REQUEST, requestCallback);
    }

    /**
     * Create a queue client instance for the subject with a custom message
     * handler.
     *
     * @param subjectName - name of the subject to create a queue client
     * instance for
     * @param handler - the message handler to handle messages sent to this
     * queue
     * @param destroyOnCleanup whether the connection being created should have its corresponding
     * message bus destination (e.g. topic) destroyed by this network session in the future.
     * @return MessageClient - the created message client, can be null if error
     * happened during creation
     */
    public MessageClient createSubjectQueueClient(String subjectName, RawMessageHandler handler, boolean destroyOnCleanup) {

        QueueMessageClient qClient = null;

        if (shuttingDown) {
            return null;
        }

        if (subjectName == null) {
            throw new IllegalArgumentException("Can't create a connection using a null subject name");
        } else if (subjectToClient.containsKey(subjectName)) {

            if(logger.isInfoEnabled()){
                logger.info("There is already a queue client instance for subject " + subjectName + ", therefore returning that client");
            }
            qClient = (QueueMessageClient) subjectToClient.get(subjectName);

        } else {
            //create connection

            try {

                if(logger.isInfoEnabled()){
                    logger.info("Creating subject queue client for "+subjectName+".");
                }

                //
                // create queue client
                //
                qClient = new QueueMessageClient(messageBrokerURI.toString(), subjectName, true);
                qClient.addConnectionStatusListener(this);

                if (handler != null) {
                    qClient.setMessageHandler(handler);
                }

                if (!qClient.connect()) {
                    qClient = null;
                    logger.error("Failed to connect to queue = " + subjectName + ".");
                } else {

                    if (logger.isInfoEnabled()) {
                        logger.info("successfully created queue client for subject = " + subjectName);
                    }

                    synchronized (subjectToClient) {
                        subjectToClient.put(subjectName, qClient);
                    }
                }

                if(destroyOnCleanup){
                    if(logger.isInfoEnabled()){
                        logger.info("Adding '"+subjectName+"' destination to be destroyed by this module in the future.");
                    }
                    synchronized(otherClientsToDestroy) {
                        otherClientsToDestroy.add(qClient);
                    }
                }

            } catch (Exception e) {
                logger.error("Caught exception while trying to create and connect to subject named " + subjectName, e);
            }
        }

        return qClient;
    }

    /**
     * Create the queue client instance for the subject and add a mapping of
     * module type to that created client instance for later retrieval when a
     * class needs to send a message to the module type's queue.
     *
     * @param subjectName - name of the subject to create a module queue client
     * instance for
     * @param moduleType - the module type the message client is associated with
     * @param destroyOnCleanup whether the connection being created should have its corresponding
     * message bus destination (e.g. topic) destroyed by this network session in the future.
     * @return MessageClient - a new message client or null if one could not be
     * created or if the client already exists.
     */
    public MessageClient createSubjectQueueClient(String subjectName, ModuleTypeEnum moduleType, boolean destroyOnCleanup) {

        MessageClient client = createSubjectQueueClient(subjectName, destroyOnCleanup);

        if (client != null && moduleType != null) {

            synchronized (moduleTypeToSubjects) {

                List<String> subjects = moduleTypeToSubjects.get(moduleType);
                if (subjects == null) {
                    subjects = new ArrayList<>();
                    moduleTypeToSubjects.put(moduleType, subjects);
                }

                if (logger.isInfoEnabled()) {
                    logger.info("successfully created mapping of module type = " + moduleType + " to message client with subject = " + subjectName);
                }

                subjects.add(subjectName);
            }

        } else if (moduleType != null) {
            logger.error("unable to add mapping of module type = " + moduleType + " to client with subject = " + subjectName
                    + " because the client could not be created.  See previous log statements for details.");
        }

        return client;
    }

    /**
     * Create a topic client instance for the subject.
     *
     * @param subjectName - name of the subject to create a topic client
     * instance for
     * @param messageHandler - the message handler for the topic used to consume
     * messages on that topic
     * @param destroyOnCleanup whether the connection being created should have its corresponding
     * message bus destination (e.g. topic) destroyed by this network session in the future.
     * @return MessageClient - the created message client, can be null if error happened during creation or if the client
     * already exists.
     */
    public MessageClient createSubjectTopicClient(String subjectName,
            final MessageHandler messageHandler, boolean destroyOnCleanup) {

        RawMessageHandler rawMessageHandler = null;
        if (messageHandler != null) {
            rawMessageHandler = new RawMessageHandler() {
                @Override
                public boolean processMessage(String msg, MessageEncodingTypeEnum encodingType) {

                    Message message = MessageUtil.getMessageFromString(msg, encodingType);

                    // FYI. There is no need for an ACK/NACK on a topic

                    messageHandler.processMessage(message);

                    return true;
                }
            };
        }

        return createSubjectTopicClient(subjectName, rawMessageHandler, destroyOnCleanup);
    }

    /**
     * Create a queue client instance for the subject.
     *
     * @param subjectName - name of the subject to create a queue client
     * instance for
     * @param destroyOnCleanup whether the connection being established by this 'select' request should have its corresponding
     * message bus destination (e.g. topic) destroyed by this network session in the future.
     * @return MessageClient - the created message client, can be null if error
     * happened during creation
     */
    private MessageClient createSubjectQueueClient(String subjectName, boolean destroyOnCleanup) {
        return createSubjectQueueClient(subjectName, (RawMessageHandler) null, destroyOnCleanup);
    }

    /**
     * Create a message client instance for the module whose status object is
     * given.
     *
     * @param moduleStatus - the module information to create a message client
     * for
     * @param destroyOnCleanup whether the connection being created should have its corresponding
     * message bus destination (e.g. topic) destroyed by this network session in the future.
     * @return MessageClient - the created message client, can be null if error
     * happened during creation
     */
    public MessageClient createSubjectQueueClient(ModuleStatus moduleStatus, boolean destroyOnCleanup) {
        return createSubjectQueueClient(moduleStatus.getQueueName(), moduleStatus.getModuleType(), destroyOnCleanup);
    }

    /**
     * Return the client for the module of the given type
     *
     * @param moduleType - the module type that a message client is needed for
     * @return MessageClient - the message client associated with the module type
     */
    private MessageClient getMessageClient(ModuleTypeEnum moduleType) {

        String subject = null;
        synchronized (moduleTypeToSubjects) {
            List<String> subjects = moduleTypeToSubjects.get(moduleType);

            if(subjects != null && !subjects.isEmpty()){

                //MH (11/1): pretty sure this commented code is legacy logic
//            if (subjects.size() > 1) {
//                throw new Exception("There should only be at most one module of type " + moduleType + " on a single GIFT network (for now at least)");
//            } else if (subjects.size() == 1) {
                subject = subjects.get(0);
            }


        }

        return subjectToClient.get(subject);
    }

    /**
     * Return the existing client for the module of the given type associated with the user
     *
     * @param userSession contains information about a user
     * @param moduleType the type of module connection that is needed.  The type can be the same module
     * that instantiated this network session.
     * @return MessageClient will be null if a message client for that module type is not associated
     * with the user specified.
     */
    private MessageClient getMessageClient(UserSession userSession, ModuleTypeEnum moduleType) {

        String subject = getConnectionName(userSession, moduleType);
        MessageClient client = subjectToClient.get(subject);
        
        if(client == null && moduleType == senderModuleType){
            // get the connection to this module's inbox queue for sending a message that will loop back
            // This is normally needed to make sure a message appears in the domain session log (e.g. ApplyStrategies)
            client = createSubjectQueueClient(connectionAddress, moduleType, false);
        }

        return client;
    }

    /**
     * Returns the URI to the message broker
     *
     * @return URI The URI to the message broker
     */
    public URI getMessageBrokerURI() {
        return messageBrokerURI;
    }

    /**
     * Return the client for the module with the given subject name
     *
     * @param subjectName - the key to finding the message client
     * @return MessageClient - the client for that connection name.  Can be null.
     */
    public MessageClient getMessageClient(String subjectName) {
        return subjectToClient.get(subjectName);
    }

    /**
     * Create a topic client instance for the subject that uses the handler
     * provided to consume messages. NOTE: If the handler is null, messages will
     * not be consumed
     *
     * @param subjectName - the name of the topic to receive messages from
     * @param handler - handles incoming messages
     * @param destroyOnCleanup whether the connection being created should have its corresponding
     * message bus destination (e.g. topic) destroyed by this network session in the future.
     * @return MessageClient - the created message client, can be null if an error happened during creation or
     * the client already exists.
     */
    public MessageClient createSubjectTopicClient(String subjectName, RawMessageHandler handler, boolean destroyOnCleanup) {

        if (shuttingDown) {
            return null;
        }

        if (subjectToClient.containsKey(subjectName)) {
            logger.error("There is already a topic client instance for subject " + subjectName);
        } else {
            //create connection

            try {

                //
                // create topic client
                //
                TopicMessageClient tClient = new TopicMessageClient(
                        messageBrokerURI.toString(), subjectName);
                tClient.addConnectionStatusListener(this);

                //if needing to consume from this topic, setup message handler
                if (handler != null) {
                    tClient.setMessageHandler(handler);
                }

                if (!tClient.connect()) {
                    logger.error("Failed to connect to topic = " + subjectName + ".");
                } else {

                    if (logger.isInfoEnabled()) {
                        logger.info("successfully created topic client for subject = " + subjectName);
                    }

                    synchronized (subjectToClient) {
                        subjectToClient.put(subjectName, tClient);
                    }
                }

                if(destroyOnCleanup){
                    if(logger.isInfoEnabled()){
                        logger.info("Adding '"+subjectName+"' to destinations to be destroyed by this module in the future.");
                    }
                    synchronized(otherClientsToDestroy) {
                        otherClientsToDestroy.add(tClient);
                    }
                }

                return tClient;

            } catch (Exception e) {
                logger.error("Caught exception while trying to create and connect", e);
            }
        }

        return null;
    }


    /**
     * Provide the incoming message to the outstanding message collections
     *
     * @param msg the message to handle
     * @return boolean - if the message was handled by a message collection
     */
    private boolean handleMessageWithMessageCollection(Message msg) {

        boolean handled = false;
        synchronized (messageCollectionMap) {

            MessageCollection messageCollection = messageCollectionMap.get(msg.getSourceEventId());
            if(messageCollection != null){
                handled = messageCollection.receive(msg);
            }else{
                //the old brute force way, give the receive message to all collections

                for (MessageCollection i : messageCollectionMap.values()) {
                    boolean result = i.receive(msg);
                    if (result) {
                        if (logger.isInfoEnabled()) {
                            logger.info("A message collection was given the received message of " + msg);
                        }

                        handled = true;
                        break;
                    }
                }
            }
        }//end synch

        if (!handled) {
            handled = shouldIgnore(msg.getMessageType());

            if(handled && logger.isDebugEnabled()){
                logger.debug("The following message was not handled by any message collection but is considered as an ignore message type so no additional processing will happen:\n"+msg);
            }
        }

        return handled;
    }

    /**
     * Provide an error that occurred while decoding the payload to the
     * pertinent message collection
     *
     * @param decodeException
     */
    private void handleExceptionWithMessageCollection(PayloadDecodeException decodeException) {

        synchronized (messageCollectionMap) {

            MessageCollection messageCollection = messageCollectionMap.get(decodeException.getIncompleteMessage().getSourceEventId());
            if(messageCollection != null){

                messageCollection.receiveFailure(decodeException);
                if (logger.isInfoEnabled()) {
                    logger.info("A message collection was given the received exception of " + decodeException);
                }
            }

        }//end synch
    }

    /**
     * Retrieve the list of client connections that need to be sent a message of the given type.
     *
     * @param messageType the message type to use to retrieve the necessary clients.
     * @return the message clients known to this network session instance (usually for a single module instance)
     * that are for the module types that need to receive this particular message type based on the architecture.
     * The list can be empty but not null.
     */
    private Set<MessageClient> getMessageClients(MessageTypeEnum messageType){

        //TODO: design a way so don't have to create new set every time
        Set<MessageClient> clients = new HashSet<>();

        ModuleTypeEnum[] moduleTypes = messageTypeToRecipients.get(messageType);
        if(moduleTypes != null){

            for(ModuleTypeEnum moduleType : moduleTypes){

                MessageClient client = getMessageClient(moduleType);

                if(client == null){
                    logger.error("Unable to find a message client for "+moduleType);
                    clients.clear();
                    break;
                }

                clients.add(client);

            }//end for
        }

        return clients;
    }

    /**
     * Retrieve the list of client connections that need to be sent a message of
     * the given type.
     *
     * @param userSession the id of the user to use to find modules allocated to that user
     * @param messageType the type of message to find module types to send to based on definitions/paths in this class
     * @param ignoreModules array of module types that would normally want to receive this message type but can be ignored
     * in the eyes of the sender.  For example the domain module might not need the gateway module in a course therefore
     * it doesn't need to get the gateway message client in this call for messages normally sent from domain to gateway modules.
     * Can be null or empty.
     * @return the module clients that are allocated to this user that need to receive this message type.
     */
    private Set<MessageClient> getMessageClients(UserSession userSession, MessageTypeEnum messageType, ModuleTypeEnum...ignoreModules) {

        //TODO: design a way so don't have to create new set every time
        Set<MessageClient> clients = new HashSet<>();

        ModuleTypeEnum[] moduleTypes = messageTypeToRecipients.get(messageType);

        if (moduleTypes != null) {

            boolean ignore;
            for (ModuleTypeEnum moduleType : moduleTypes) {

                //check the ignore list - if available
                if(ignoreModules != null && ignoreModules.length != 0){
                    ignore = false;

                    for(ModuleTypeEnum ignoreModuleType : ignoreModules){

                        if(moduleType == ignoreModuleType){
                            //ignore this recipient module type because the sender said to do so
                            ignore = true;
                            break;
                        }
                    }

                    if(ignore){
                        continue;
                    }
                }

                MessageClient client = getMessageClient(userSession, moduleType);

                if (client == null) {
                    String subject = getConnectionName(userSession, moduleType);
                    logger.error("Unable to find a message client for " + moduleType + " and user " + userSession + 
                            " that is needed to send the "+messageType.getName()+" message. Subject being searched for is '"+subject+
                            "' in current subject to client map:\n"+subjectToClient+"\nModule Type to Subjects:\n"+moduleTypeToSubjects);
                    clients.clear();
                    break;
                }

                clients.add(client);

            }//end for

        } else {
            logger.error("There are no GIFT static paths for message type " + messageType + ", maybe it needs to be added?");
        }

        return clients;
    }

    /**
     * Send a message (pre user session and pre domain session) (e.g. New User Request message)
     *
     * @param payload - the payload of the message to send
     * @param messageType - the type of message being sent
     * @param callback - used to notify of reply message to this message being sent
     * @return boolean - whether the message was sent successfully
     */
    public boolean sendMessage(Object payload, MessageTypeEnum messageType, MessageCollectionCallback callback){

        int sourceEventId = atomicSourceEventId.incrementAndGet();

        boolean sent = false;

        try{
            Set<MessageClient> recipients = getMessageClients(messageType);

            if(!recipients.isEmpty()){

                Message message = createMessage(payload, sourceEventId, messageType, callback != null);
                sent = sendMessage(recipients, message, callback);
            }else{
                logger.error("There are no message clients to send message of type "+messageType+" too, therefore unable to send the payload = "+payload);
                throw new Exception("There are no message client to send the message to.");
            }

        } catch (Exception e) {
            logger.error("Caught exception while trying to send message with payload of " + payload, e);

            if (callback != null) {
                callback.failure("Failed to send " + messageType + " message. Exception: " + e);
            }
        }

        return sent;
    }

    /**
     * Send a message to a specific module type.
     *
     * @param moduleType - the module type to send this message too
     * @param payload - the payload of the message to send
     * @param messageType - the type of message being sent
     * @param callback - used to notify of reply message to this message being sent
     * @return boolean - whether the message was sent successfully
     */
    public boolean sendMessage(ModuleTypeEnum moduleType, Object payload, MessageTypeEnum messageType, MessageCollectionCallback callback) {

        int sourceEventId = atomicSourceEventId.incrementAndGet();

        boolean sent = false;

        try {
            MessageClient client = getMessageClient(moduleType);
            if (client != null) {
                Message message = createMessage(payload, sourceEventId, messageType, callback != null);
                sent = sendMessage(Collections.singleton(client), message, callback);
            } else {
                logger.error("Unable to find a message client for module type " + moduleType + ", therefore unable to send the payload = " + payload + " for message type " + messageType);
                throw new Exception("Unable to find a message client for module type " + moduleType);
            }

        } catch (Exception e) {
            logger.error("Caught exception while trying to send message to any module of type " + moduleType, e);

            if (callback != null) {
                callback.failure("Failed to send the "+messageType+" to a message client for module type " + moduleType+". Exception: " + e);
            }
        }

        return sent;
    }

    /**
     * Send a user session message
     *
     * @param moduleType - the module type to send this message too
     * @param payload - the payload of the message to send
     * @param userSession - information about the user session (including the unique user id of the learner)
     *                      the message is associated with
     * @param messageType - the type of message being sent
     * @param callback - used to notify of reply message to this message being
     * sent
     * @return boolean - whether the message was sent successfully
     */
    public boolean sendMessage(ModuleTypeEnum moduleType, Object payload, UserSession userSession, MessageTypeEnum messageType, MessageCollectionCallback callback) {

        int sourceEventId = atomicSourceEventId.incrementAndGet();

        boolean sent = false;

        try{

            MessageClient client = getMessageClient(userSession, moduleType);
            if (client != null) {
                Message message = createUserSessionMessage(userSession, sourceEventId, payload, messageType, callback != null);
                sent = sendMessage(Collections.singleton(client), message, callback);
            } else {
                logger.error("Unable to find a message client for module type " + moduleType + " and user session "+userSession+", therefore unable to send the payload = " + payload + " for message type " + messageType);
                throw new Exception("Unable to find a message client for module type " + moduleType + " and user session "+userSession);
            }

        } catch (Exception e) {
            logger.error("Caught exception while trying to send message to any module of type " + moduleType, e);

            if (callback != null) {
                callback.failure("Failed to send the "+messageType+" to a message client for module type " + moduleType+". Exception: " + e);
            }
        }

        return sent;
    }

    /**
     * Send a user session message
     *
     * @param payload - the payload of the message to send
     * @param userSession - information about the user session (including the unique user id of the learner)
     *                      the message is associated with
     * @param messageType - the type of message being sent
     * @param callback - used to notify of reply message to this message being
     * sent
     * @return boolean - whether the message was sent successfully
     */
    public boolean sendUserSessionMessage(Object payload, UserSession userSession, MessageTypeEnum messageType, MessageCollectionCallback callback) {

        int sourceEventId = atomicSourceEventId.incrementAndGet();

        boolean sent = false;

        try{

            Set<MessageClient> recipients = getMessageClients(userSession, messageType);
            if (!recipients.isEmpty()) {
                Message message = createUserSessionMessage(userSession, sourceEventId, payload, messageType, callback != null);
                sent = sendMessage(recipients, message, callback);
            } else {
                logger.error("There are no message clients to send message of type " + messageType + " too, therefore unable to send the payload = " + payload + " for message type " + messageType);
                throw new Exception("There are no message clients to send message of type " + messageType);
            }

        } catch (Exception e) {
            logger.error("Caught exception while trying to send "+messageType+" message with payload of " + payload+".", e);

            if (callback != null) {
                callback.failure("Failed to send the "+messageType+" to a message client. Exception: " + e);
            }
        }

        return sent;
    }

    /**
     * Send a domain session message
     *
     * @param moduleType - the module type to send this message too
     * @param payload - the payload of the message to send
     * @param userSession - information about the user session (including the unique user id of the learner)
     *                      the message is associated with
     * @param domainSessionId - the unique domain session id associated with
     * this domain session message
     * @param messageType - the type of message being sent
     * @param callback - used to notify of reply message to this message being
     * sent.  Can be null.
     * @return boolean - whether the message was sent successfully
     */
    public boolean sendMessage(ModuleTypeEnum moduleType, Object payload, UserSession userSession, int domainSessionId,
            MessageTypeEnum messageType, MessageCollectionCallback callback) {

        int sourceEventId = atomicSourceEventId.incrementAndGet();

        boolean sent = false;

        try{

            MessageClient client = getMessageClient(userSession, moduleType);
            if (client != null) {
                Message message = createDomainSessionMessage(userSession, sourceEventId, domainSessionId, payload, messageType, callback != null);
                sent = sendMessage(Collections.singleton(client), message, callback);
            } else {
                logger.error("Unable to find a message client for module type " + moduleType + ", therefore unable to send the payload = " + payload + " for message type " + messageType);
                throw new Exception("Unable to find a message client for module type " + moduleType);
            }

        } catch (Exception e) {
            logger.error("Caught exception while trying to send "+messageType+" message to any module of type " + moduleType, e);

            if (callback != null) {
                callback.failure("Failed to send the "+messageType+" message to a message client for module type " + moduleType+". Exception: " + e);
            }
        }

        return sent;
    }

    /**
     * Send a domain session message
     *
     * @param moduleTypes - the module types to send this message too
     * @param payload - the payload of the message to send
     * @param userSession - information about the user session (including the unique user id of the learner)
     *                      the message is associated with
     * @param domainSessionId - the unique domain session id associated with
     * this domain session message
     * @param messageType - the type of message being sent
     * @param callback - used to notify of reply message to this message being
     * sent
     * @return boolean - whether the message was sent successfully
     */
    public boolean sendMessage(ModuleTypeEnum[] moduleTypes, Object payload, UserSession userSession, int domainSessionId,
            MessageTypeEnum messageType, MessageCollectionCallback callback) {

        int sourceEventId = atomicSourceEventId.incrementAndGet();

        boolean sent = false;

        try{

            Set<MessageClient> recipients = new HashSet<>(moduleTypes.length);
            for (ModuleTypeEnum mType : moduleTypes) {
                MessageClient client = getMessageClient(userSession, mType);

                if (client == null) {
                    logger.error("Unable to find a message client for module type " + mType + ", therefore unable to send the payload = " + payload + " for message type " + messageType);

                    if (callback != null) {

                        callback.failure("Unable to find a message client for module type " + mType);
                    }

                    return sent;
                }

                recipients.add(client);
            }

            if (!recipients.isEmpty()) {
                Message message = createDomainSessionMessage(userSession, sourceEventId, domainSessionId, payload, messageType, callback != null);
                sent = sendMessage(recipients, message, callback);
            } else {
                logger.error("There are no message clients to send message of type " + messageType + " too, therefore unable to send the payload = " + payload);
                throw new Exception("There are no message clients to send message of type " + messageType +".");
            }

        } catch (Exception e) {
            logger.error("Caught exception while trying to send "+messageType+" message to any modules", e);

            if (callback != null) {
                callback.failure("Failed to send the "+messageType+" message to a message client. Exception: " + e);
            }
        }

        return sent;
    }

    /**
     * Send a domain session message
     *
     * @param payload - the payload of the message to send
     * @param domainSession - the domain session associated with this domain session message
     * @param messageType - the type of message being sent
     * @param callback - used to notify of reply message to this message being
     * sent
     * @return boolean - whether the message was sent successfully
     */
    public boolean sendMessage(DomainSession domainSession, Object payload, MessageTypeEnum messageType, MessageCollectionCallback callback){

        if(domainSession.isGatewayConnected()){
            return sendMessage(payload, domainSession, domainSession.getDomainSessionId(), messageType, callback);
        }else{
            return sendMessage(payload, domainSession, domainSession.getDomainSessionId(), messageType, callback, ModuleTypeEnum.GATEWAY_MODULE);
        }

    }

    /**
     * Send a domain session message
     *
     * @param payload - the payload of the message to send
     * @param userSession - information about the user session (including the unique user id of the learner)
     *                      the message is associated with
     * @param domainSessionId - the unique domain session id associated with
     * this domain session message
     * @param messageType - the type of message being sent
     * @param callback - used to notify of reply message to this message being
     * sent.  Can be null.  When null the recipient won't need to send a response that the message was processed because
     * the message header will show "NeedsACK":false.
     * @param ignoreModules array of module types that would normally want to receive this message type but can be ignored
     * in the eyes of the sender.  For example the domain module might not need the gateway module in a course therefore
     * it doesn't need to get the gateway message client in this call for messages normally sent from domain to gateway modules.
     * Can be null or empty.
     * @return boolean - whether the message was sent successfully
     */
    public boolean sendMessage(Object payload, UserSession userSession, int domainSessionId,
            MessageTypeEnum messageType, MessageCollectionCallback callback, ModuleTypeEnum...ignoreModules){

        int sourceEventId = atomicSourceEventId.incrementAndGet();

        boolean sent = false;

        try{
            Set<MessageClient> recipients = getMessageClients(userSession, messageType, ignoreModules);
            if (!recipients.isEmpty()) {
                Message message = createDomainSessionMessage(userSession, sourceEventId, domainSessionId, payload, messageType, callback != null);
                sent = sendMessage(recipients, message, callback);
            } else if(ignoreModules == null || ignoreModules.length == 0){
                //there are no recipients and the ignore list is empty
                logger.error("There are no message clients to send message of type " + messageType + " too, therefore unable to send the payload = " + payload);
                throw new Exception("There are no message clients to send message of type " + messageType);
            } else if(callback != null){
                //there are no recipients and the ignore list has modules, therefore the ignore list must have removed
                //the recipients
                callback.success();
            }
        }catch(Exception e){
            logger.error("Caught exception while trying to send message type "+messageType+" with payload = "+payload+".", e);

            if(callback != null){
                callback.failure(e.getMessage());
            }
        }

        return sent;
    }

    /**
     * Send a message to a specific address (i.e. subject)
     *
     * @param subjectName - the destination to send this message too
     * @param payload - the payload of the message to send
     * @param messageType - the type of message being sent
     * @param callback - used to notify of reply message to this message being
     * sent
     * @return boolean - whether the message was sent successfully
     */
    public boolean sendMessage(String subjectName, Object payload, MessageTypeEnum messageType, MessageCollectionCallback callback) {

        int sourceEventId = atomicSourceEventId.incrementAndGet();

        boolean sent = false;

        try{
            MessageClient client = getMessageClient(subjectName);
            if (client != null) {
                Message message = createMessage(payload, sourceEventId, messageType, callback != null);
                sent = sendMessage(Collections.singleton(client), message, callback);
            } else {
                logger.error("Unable to find a message client for subject name " + subjectName + ", therefore unable to send the payload = " + payload + " for message type " + messageType);
                throw new Exception("Unable to find a message client for subject name " + subjectName);
            }

        }catch(Exception e){
            logger.error("Caught exception while trying to send message type "+messageType+" with payload = "+payload+".", e);

            if(callback != null){
                callback.failure(e.getMessage());
            }
        }

        return sent;
    }

    /**
     * Send a user session message to a specific address (i.e. subject)
     *
     * @param subjectName - the destination to send this message too
     * @param payload - the payload of the message to send
     * @param userSession - information about the user session (including the unique user id of the learner)
     *                      the message is associated with
     * @param messageType - the type of message being sent
     * @param callback - used to notify of reply message to this message being
     * sent
     *
     * @return boolean - whether the message was sent successfully
     */
    public boolean sendMessage(String subjectName, Object payload, UserSession userSession, MessageTypeEnum messageType, MessageCollectionCallback callback) {

        int sourceEventId = atomicSourceEventId.incrementAndGet();

        boolean sent = false;

        try{
            MessageClient client = getMessageClient(subjectName);
            if (client != null) {
                Message message = createUserSessionMessage(userSession, sourceEventId, payload, messageType, callback != null);
                sent = sendMessage(Collections.singleton(client), message, callback);
            } else {
                logger.error("Unable to find a message client for subject name " + subjectName + ", therefore unable to send the payload = " + payload + " for message type " + messageType);
                throw new Exception("Unable to find a message client for subject name " + subjectName);
            }

        } catch (Exception e) {
            logger.error("Caught exception while trying to send message to any module with address of " + subjectName, e);

            if (callback != null) {
                callback.failure("Failed to send the "+messageType+" message to a message client with address of " + subjectName+". Exception: " + e);
            }
        }

        return sent;
    }

    /**
     * Send a domain session message to a specific address (i.e. subject)
     *
     * @param subjectName - the destination to send this message too
     * @param payload - the payload of the message to send
     * @param userSession - information about the user session (including the unique user id of the learner)
     *                      the message is associated with
     * @param domainSessionId - the unique domain session id associated with
     * this domain session message
     * @param messageType - the type of message being sent
     * @param callback - used to notify of reply message to this message being
     * sent
     * @return boolean - whether the message was sent successfully
     */
    public boolean sendMessage(String subjectName, Object payload, UserSession userSession, int domainSessionId,
            MessageTypeEnum messageType, MessageCollectionCallback callback) {

        int sourceEventId = atomicSourceEventId.incrementAndGet();

        boolean sent = false;

        try{
            MessageClient client = getMessageClient(subjectName);
            if (client != null) {
                Message message = createDomainSessionMessage(userSession, sourceEventId, domainSessionId, payload, messageType, callback != null);
                sent = sendMessage(Collections.singleton(client), message, callback);
            } else {
                logger.error("Unable to find a message client for subject name " + subjectName + ", therefore unable to send the payload = " + payload + " for message type " + messageType);

                if (callback != null) {

                    callback.failure("Unable to find a message client for subject name " + subjectName);
                }
            }

        } catch (Exception e) {
            logger.error("Caught exception while trying to send "+messageType+" message to any module with address of " + subjectName, e);

            if (callback != null) {
                callback.failure("Failed to send the "+messageType+" message to any module with address of "+subjectName+". Exception: " + e);
            }
        }

        return sent;
    }

    /**
     * Send a reply message with the given payload.
     *
     * @param messageReplyingToo - the message being replied too. More
     * importantly the message with the sequence number to reply too
     * @param payload - the payload of the message to send
     * @param messageType - the type of message being sent
     * @param callback - used to notify of reply message to this message being
     * sent
     * @return boolean - whether the message was sent
     */
    public boolean sendReply(Message messageReplyingToo, Object payload, MessageTypeEnum messageType, MessageCollectionCallback callback) {

        int sourceEventId = atomicSourceEventId.incrementAndGet();

        boolean sent = false;

        if (messageReplyingToo != null) {

            MessageClient client = getMessageClient(messageReplyingToo.getSenderAddress());
            if (client == null) {
                //try creating the client to just send replies too
                client = createSubjectQueueClient(messageReplyingToo.getSenderAddress(), messageReplyingToo.getSenderModuleType(), false);
            }

            if (client != null) {
                Message message = createReplyMessage(messageReplyingToo, sourceEventId, payload, messageType, callback != null);
                sent = sendMessage(Collections.singleton(client), message, callback);
            } else {
                logger.error("Unable to find a message client for module type " + messageReplyingToo.getSenderAddress() + ", therefore unable to send a reply to message = " + messageReplyingToo);

                if (callback != null) {

                    callback.failure("Unable to find a message client for module type " + messageReplyingToo.getSenderAddress());
                }
            }

        } else {
            logger.error("unable to reply to the received message of " + messageReplyingToo);

            if (callback != null) {

                callback.failure("unable to reply to the received message of " + messageReplyingToo);
            }
        }

        return sent;
    }

    /**
     * Send a message to a specific message client(s)
     *
     * @param clients - the destinations to send the message
     * @param message - the message to send
     * @param callback - used to notify the sender of a response.  Can be null.
     * @return boolean - whether the message was sent successfully
     */
    private boolean sendMessage(Set<MessageClient> clients, Message message, MessageCollectionCallback callback) {

        boolean sent;

        int sourceEventId = atomicSourceEventId.incrementAndGet();

        //TODO: stream line this more - if only 1 client and no callback, don't create message collection class, just send it
        MessageCollection mCollection = new MessageCollection(sourceEventId, message, clients, callback, this);

        if (callback != null) {
            synchronized (messageCollectionMap) {
                messageCollectionMap.put(sourceEventId, mCollection);
            }
        }

        sent = mCollection.send(messageCodecType);

        if (!sent) {
            logger.error("Unable to send message to all recipients: " + mCollection);

            if (callback != null) {

                callback.failure("Unable to send message to all recipients.");
            }
        }

        return sent;
    }

    /**
     * Create a new Domain Session Message with the given content.
     *
     * @param userSession information about the user running the domain session
     * @param sourceEventId the unique id from the message collection
     * @param domainSessionId the unique session id
     * @param payload the content of the message to send
     * @param messageType the type of message being sent
     * @param needsACK whether this message needs to be replied to by the receiver
     * @return the new domain session message
     */
    private Message createDomainSessionMessage(UserSession userSession, int sourceEventId, int domainSessionId, Object payload, MessageTypeEnum messageType, boolean needsACK) {
        DomainSessionMessage message = new DomainSessionMessage(messageType, sourceEventId, senderName, connectionAddress, senderModuleType, null, payload, userSession, domainSessionId, needsACK);
        return message;
    }

    /**
     * Create a new User Session message with the given content
     *
     * @param userSession the unique user id of the user running the domain session
     * @param sourceEventId the unique id from the message collection
     * @param payload the content of the message to send
     * @param messageType the type of message being sent
     * @param needsACK whether this message needs to be replied to by the receiver
     * @return Message - the new user session message
     */
    private Message createUserSessionMessage(UserSession userSession, int sourceEventId, Object payload, MessageTypeEnum messageType, boolean needsACK) {
        return new UserSessionMessage(messageType, sourceEventId, senderName, connectionAddress, senderModuleType, null, payload, userSession, needsACK);
    }

    /**
     * Create a new Message with the given content Note: this is not a domain or
     * user session message
     *
     * @param payload the content of the message to send
     * @param sourceEventId the unique id from the message collection
     * @param messageType the type of message being sent
     * @param needsACK whether this message needs to be replied to by the receiver
     * @return Message - the new message
     */
    private Message createMessage(Object payload, int sourceEventId, MessageTypeEnum messageType, boolean needsACK) {
        return new Message(messageType, sourceEventId, senderName, connectionAddress, senderModuleType, null, payload, needsACK);
    }

    /**
     * Create a new reply message in response to the given message. The reply
     * message will be of the same type the message being replied too is. In
     * addition the sequence number of that message will be used as the
     * reply-to-sequence-number for the new reply message.
     *
     * @param messageReplyingToo the message being replied too, used to extract the message sequence id so the recipient
     * of the reply message knows which message the reply is for.
     * @param sourceEventId the unique id from the message collection
     * @param payload the content of the message to send
     * @param messageType the type of message being sent
     * @param needsACK whether this message needs to be replied to by the receiver
     * @return Message - the new reply message
     */
    private Message createReplyMessage(Message messageReplyingToo, int sourceEventId, Object payload, MessageTypeEnum messageType, boolean needsACK) {

        Message message;
        if (messageReplyingToo instanceof DomainSessionMessage) {
            message = createReplyDomainSessionMessage(((DomainSessionMessage) messageReplyingToo).getUserSession(),
                    sourceEventId,
                    ((DomainSessionMessage) messageReplyingToo).getDomainSessionId(),
                    messageReplyingToo.getSequenceNumber(), payload, messageType, needsACK);
        } else if (messageReplyingToo instanceof UserSessionMessage) {
            message = createReplyUserSessionMessage(((UserSessionMessage) messageReplyingToo).getUserSession(), sourceEventId, messageReplyingToo.getSequenceNumber(), payload, messageType, needsACK);
        } else {
            message = createMessage(payload, sourceEventId, messageType, needsACK);
            message.setReplyToSequenceNumber(messageReplyingToo.getSequenceNumber());
        }

        return message;
    }

    /**
     * Create a new user session message that is in response to a message with the given user session and sequence number
     * information.
     *
     * @param userSession contains information about the user session the message being replied to was associated with
     * @param sourceEventId the unique id from the message collection
     * @param messageReplyToSeqNum the sequence number of the message being replied to
     * @param payload the contents of the reply message being created
     * @param messageType the type of message being created
     * @param needsACK whether this message requires that the receiver send an acknowledge message to let this sender know the message was received.
     * @return the created reply message
     */
    private Message createReplyUserSessionMessage(UserSession userSession, int sourceEventId, int messageReplyToSeqNum, Object payload, MessageTypeEnum messageType, boolean needsACK){

        Message message = createUserSessionMessage(userSession, sourceEventId, payload, messageType, needsACK);
        message.setReplyToSequenceNumber(messageReplyToSeqNum);

        return message;
    }

    /**
     * Create a new domain session message that is in response to a message with the given user session and sequence number
     * information.
     *
     * @param userSession contains information about the user session the message being replied to was associated with
     * @param sourceEventId the unique id from the message collection
     * @param domainSessionId the unique identifier the message being replied to was associated with
     * @param messageReplyToSeqNum the sequence number of the message being replied to
     * @param payload the contents of the reply message being created
     * @param messageType the type of message being created
     * @param needsACK whether this message requires that the receiver send an acknowledge message to let this sender know the message was received.
     * @return the created reply message
     */
    private Message createReplyDomainSessionMessage(UserSession userSession, int sourceEventId, int domainSessionId, int messageReplyToSeqNum, Object payload, MessageTypeEnum messageType, boolean needsACK){

        Message message = createDomainSessionMessage(userSession,
                sourceEventId,
                domainSessionId,
                payload, messageType, needsACK);
        message.setReplyToSequenceNumber(messageReplyToSeqNum);

        return message;
    }

    /**
     * The network session is closing. Cleans up any resources the session is
     * using
     *
     * @param destroyDestinations whether to destroy any destinations (e.g. topics) on the message bus that
     * this module has flagged as needing to be destroyed as part of being cleaned up.
     */
    public synchronized void cleanup(boolean destroyDestinations) {

        if(!shuttingDown){

            if(logger.isInfoEnabled()){
                logger.info("Cleaning up "+senderName+" module's connection to message bus");
            }

            shuttingDown = true;

            moduleStatusMonitor.close();

            moduleQueueClient.disconnect(destroyDestinations);

            //disconnect connections to other clients
            synchronized (subjectToClient) {
                for (MessageClient i : subjectToClient.values()) {
                    i.disconnect(false);
                }
            }

            //cleanup the decoded message queue and thread
            synchronized(decodedMessageMutex){
                decodedMessageList.clear();
                decodedACKs.clear();
                decodedMessageMutex.notifyAll();
            }

            if(destroyDestinations){
                
                synchronized(otherClientsToDestroy) {
                    //destroy any other clients the module has registered for being destroyed
                    for(MessageClient client : otherClientsToDestroy){
                        if(logger.isInfoEnabled()){
                            logger.info("Processing other clients to destroy: destroying the client "+client);
                        }
                        client.disconnect(true);
                    }
    
                    otherClientsToDestroy.clear();
                }
            }

        }
    }

    @Override
    public void moduleStatusAdded(long sentTime, ModuleStatus status) {
    }

    @Override
    public void moduleStatusChanged(long sentTime, ModuleStatus status) {
    }

    @Override
    public void moduleStatusRemoved(StatusReceivedInfo status) {

        //disconnect client for stale module
        ModuleStatus moduleStatus = status.getModuleStatus();
        MessageClient client = getMessageClient(moduleStatus.getQueueName());
        if (client != null) {

            //notify this module of which user sessions this affects, if any
            //Note: the client disconnect event will cause the subject to be removed from compositeToSubject, therefore
            //      this notification needs to happen here and now (especially in case the disconnect logic fails for some reason).
            if(!allocatedModuleListeners.isEmpty()){
                synchronized(compositeToSubject){

                    for(CompositeUserModuleKey key : compositeToSubject.keySet()){

                        String subject = compositeToSubject.get(key);
                        if(subject != null && subject.equals(moduleStatus.getQueueName())){
                            //found user session referencing this disconnected connection

                            //Note: most likely case, for now, is that a module goes down and it is used by 1 user, therefore this will only be called once
                            notifyAllocatedModuleRemovedEvent(key.getUserId(), key.getModuleType(), status);
                        }
                    }
                }
            }

            //this will cause the connection to be removed from compositeToSubject (in this class)
            if(otherClientsToDestroy.contains(client)){
                if(logger.isInfoEnabled()){
                    logger.info("Disconnecting and destroying connection to "+client);
                }
                client.disconnect(true);
                otherClientsToDestroy.remove(client);
            }
        }
    }

    /**
     * Notify allocated module listeners that currently allocated module has been removed.
     *
     * @param userId - the user id associated with the allocated module connection being removed
     * @param moduleType - the type of module associated with the connection being removed
     * @param lastStatus - the last status from that module
     */
    private void notifyAllocatedModuleRemovedEvent(final int userId, final ModuleTypeEnum moduleType, final StatusReceivedInfo lastStatus){

        new Thread("AllocatedModuleNotification-"+userId+":"+moduleType){

            @Override
            public void run() {

                synchronized(allocatedModuleListeners){

                    for(AllocatedModuleListener listener : allocatedModuleListeners){

                        try{
                            listener.allocatedModuleRemoved(userId, moduleType, lastStatus);
                        }catch(Exception e){
                            logger.error("Caught exception from mis-behaving listener while notifying this module of an allocated module (type: "+moduleType+") remove event for user "+userId, e);
                        }
                    }
                }
            }

        }.start();
    }

    @Override
    public void messageCollectionFinished(int sourceEventId) {
        synchronized (messageCollectionMap) {
            messageCollectionMap.remove(sourceEventId);
        }//end synch
    }

    /**
     * This class contains the unique identifier of a user which is useful as
     * a composite key in a map.
     *
     * @author mhoffman
     *
     */
    private class CompositeUserKey{

        /* These fields are components to the composite key.  Typically only one of these fields should be set, while the
         * others will be null.
         */
        private Integer userId;
        private String experimentId;
        private Integer globalUserId;

        /**
         * the keys will be null
         */
        public CompositeUserKey(){

        }

        /**
         * Class constructor - set key values
         *
         * @param userId - the user id associated with the module type
         */
        public CompositeUserKey(Integer userId) {
            setUserId(userId);
        }

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        /**
         * Return the experiment id if this user is a subject in an experiment.
         *
         * @return can be null
         */
        public String getExperimentId() {
            return experimentId;
        }

        public void setExperimentId(String experimentId) {
            this.experimentId = experimentId;
        }

        /**
         * @return the globalUserId
         */
        public Integer getGlobalUserId() {
            return globalUserId;
        }

        /**
         * @param globalUserId the globalUserId to set
         */
        public void setGlobalUserId(Integer globalUserId) {
            this.globalUserId = globalUserId;
        }

        @Override
        public boolean equals(Object o) {

            if (o == this) {
                return true;
            } else if (o != null && o instanceof CompositeUserKey) {

                CompositeUserKey otherKey = (CompositeUserKey) o;

                //check user id

                if (CompareUtil.equalsNullSafe(this.getUserId(), otherKey.getUserId())) {
                    if (CompareUtil.equalsNullSafe(this.getExperimentId(), otherKey.getExperimentId())) {
                        if (CompareUtil.equalsNullSafe(this.getGlobalUserId(), otherKey.getGlobalUserId())) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }


        @Override
        public int hashCode() {

            int hashCode = 7;

            hashCode = 37 * hashCode + (userId != null ? userId : 0);
            hashCode = 37 * hashCode + (experimentId != null ? experimentId.hashCode() : 0);
            hashCode = 37 * hashCode + (getGlobalUserId() != null ? getGlobalUserId().hashCode() : 0);

            return hashCode;
        }

        @Override
        public String toString() {

            StringBuilder sb = new StringBuilder();
            sb.append("[CompositeUserKey: ");
            sb.append("hashCode = ").append(this.hashCode());
            sb.append(", userId = ").append(this.getUserId());
            sb.append(", experimentId = ").append(this.getExperimentId());
            sb.append(", globalUserId = ").append(this.getGlobalUserId());
            sb.append("]");
            return sb.toString();
        }
    }

    /**
     * This class contains the unique identifier of a user and a module type reference
     * which is useful in a map where users are associated with 1 instance of a particular module type
     *
     * @author mhoffman
     *
     */
    private class CompositeUserModuleKey extends CompositeUserKey {

        private ModuleTypeEnum moduleType;
        /**
         * Class constructor - keys will be null
         */
        public CompositeUserModuleKey() {
        }

        /**
         * Class constructor - set key values
         *
         * @param userId - the user id associated with the module type
         * @param moduleType - the type of module for this key
         */
        public CompositeUserModuleKey(Integer userId, ModuleTypeEnum moduleType) {
            super(userId);

            setModuleType(moduleType);
        }


        public ModuleTypeEnum getModuleType() {
            return moduleType;
        }

        public void setModuleType(ModuleTypeEnum moduleType) {
            this.moduleType = moduleType;
        }

        @Override
        public boolean equals(Object o) {

            if (o == this) {
                return true;
            } else if (o != null && o instanceof CompositeUserModuleKey) {

                CompositeUserModuleKey otherKey = (CompositeUserModuleKey) o;

                //check user id
                if (CompareUtil.equalsNullSafe(this.getModuleType(), otherKey.getModuleType())) {
                    if (CompareUtil.equalsNullSafe(this.getUserId(), otherKey.getUserId())) {
                        if (CompareUtil.equalsNullSafe(this.getExperimentId(), otherKey.getExperimentId())) {
                            if (CompareUtil.equalsNullSafe(this.getGlobalUserId(), otherKey.getGlobalUserId())) {
                                return true;
                            }
                        }
                    }
                }
            }

            return false;
        }

        @Override
        public int hashCode() {

            int hashCode = 7;

            hashCode = 37 * hashCode + (getUserId() != null ? getUserId() : 0);
            hashCode = 37 * hashCode + (moduleType != null ? moduleType.getValue() : 0);
            hashCode = 37 * hashCode + (getExperimentId() != null ? getExperimentId().hashCode() : 0);
            hashCode = 37 * hashCode + (getGlobalUserId() != null ? getGlobalUserId().hashCode() : 0);

            return hashCode;
        }

        @Override
        public String toString() {

            StringBuilder sb = new StringBuilder();
            sb.append("[CompositeUserModuleKey: ");
            sb.append("hashCode = ").append(this.hashCode());
            sb.append(", userId = ").append(this.getUserId());
            sb.append(", experimentId = ").append(this.getExperimentId());
            sb.append(", moduleType = ").append(this.getModuleType());
            sb.append(", globalUserId = ").append(this.getGlobalUserId());
            sb.append("]");
            return sb.toString();
        }
    }

    @Override
    public void connectionOpened(MessageClient client) {
        synchronized (subjectToClient) {
            if (!subjectToClient.containsKey(client.getSubjectName())) {
                subjectToClient.put(client.getSubjectName(), client);
            } else {
                logger.error("There is already a topic client instance for subject " + client.getSubjectName());
            }
        }
    }

    @Override
    public void onConnectionLost(MessageClient client) {
        if (!shuttingDown) {
            removeConnection(client.getSubjectName(), false, false);
        }
    }

    @Override
    public void connectionClosed(MessageClient client) {
        if (!shuttingDown) {
            removeConnection(client.getSubjectName(), true, false);
        }
    }
}
