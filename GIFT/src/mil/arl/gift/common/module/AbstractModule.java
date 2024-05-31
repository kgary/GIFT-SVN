/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.module;

import java.awt.SplashScreen;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.ModuleConnectionConfigurationException;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.WebClientInformation;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.LessonLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.ModuleModeEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.io.TimeUtil;
import mil.arl.gift.net.api.AllocatedModuleListener;
import mil.arl.gift.net.api.ConnectionFilter;
import mil.arl.gift.net.api.MessageCollectionCallback;
import mil.arl.gift.net.api.NetworkSession;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.MessageHandler;
import mil.arl.gift.net.api.message.RawMessageHandler;
import mil.arl.gift.net.api.message.UserSessionMessage;
import mil.arl.gift.net.util.Util;

/**
 * This class contains the common implementation for all module classes. Modules
 * classes need to extend this class.
 *
 * @author mhoffman
 *
 */
public abstract class AbstractModule {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AbstractModule.class);

    /**
     * The delimiter used to separate components of a queue or topic address.
     */
    public static final String ADDRESS_TOKEN_DELIM = "_";

    /** the module's name */
    private String moduleName;

    /** the module status information */
    protected ModuleStatus moduleStatus;

    /** the module mode information */
    protected ModuleModeEnum moduleMode;

    /** the module's heartbeat daemon */
    protected ModuleHeartbeatDaemon moduleHeartbeat = null;

    /** instance of the module property file parser for this module instance */
    protected AbstractModuleProperties properties;

    /** flag used to indicate that this module is in the process of shutting
     * down */
    protected boolean shuttingDown = false;

    /** handles sending/receiving raw messages and giving those messages to this
     * module as GIFT messages */
    private NetworkSession networkSession;

    /** contains information about the user sessions this module is allocated to */
    protected AllocationStatus allocationStatus = new AllocationStatus();

    /** IP address of the machine executing this module */
    protected static String ipaddr = Util.getLocalHostAddress().getHostAddress();

    static {

        try {

            //display java information
            String javaVersion = System.getProperty("java.version");
            String javaBin = System.getProperty("sun.boot.library.path");
            System.out.println("Using Java version " + javaVersion + " from "+javaBin);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Class constructor
     *
     * @param defaultModuleName - the default module name to use in case the
     * module implementation doesn't provide a unique name
     * @param moduleQueueName - where message for this module are read from
     * @param moduleQueueInboxName - where other modules can send message to
     * this module
     * @param properties - properties for this module
     */
    public AbstractModule(String defaultModuleName, String moduleQueueName, String moduleQueueInboxName, AbstractModuleProperties properties) {
        this(defaultModuleName, moduleQueueName, moduleQueueInboxName, properties, true);

        moduleStatus = new ModuleStatus(getModuleName(), moduleQueueInboxName, getModuleType());
    }

    /**
     * Class constructor
     *
     * @param defaultModuleName The default name of the module if one is not
     * specified in the properties
     * @param moduleQueueName The queue name for the module to listen to
     * messages send to it on
     * @param moduleQueueInboxName The queue name for the queue to receive
     * messages on before having them routed to the module queue
     * @param properties The properties of the module
     * @param clearQueueOnStartup If the module queue should be cleared of old
     * messages before listening begins
     */
    public AbstractModule(String defaultModuleName, String moduleQueueName, String moduleQueueInboxName,
            AbstractModuleProperties properties, boolean clearQueueOnStartup) {
        this.properties = properties;
        moduleMode = ModuleModeEnum.POWER_USER_MODE;	//default to multi process

        if (properties.getModuleName() != null) {
            moduleName = properties.getModuleName();
        } else {
            moduleName = defaultModuleName;
        }

        //consume messages from this module's queue
        MessageHandler messageHandler = new MessageHandler() {
            @Override
            public boolean processMessage(Message message) {

                //ignore incoming messages if currently shutting this module down
                if (shuttingDown) {
                    return false;
                }

                if (message.getMessageType() == MessageTypeEnum.MODULE_ALLOCATION_REQUEST) {
                    //another module is requesting that this module be allocated for it to use, handle that request

                    handleModuleAllocationRequestMessage(message);
                } else {

                    handleMessage(message);
                }

                return true;
            }
        };

        try {
            networkSession = new NetworkSession(getModuleName(), getModuleType(), moduleQueueName, clearQueueOnStartup, moduleQueueInboxName,
                    properties.getBrokerURL(), properties.getMessageEncodingType(), messageHandler, !properties.shouldIgnoreIPAddrAllocation(), properties.getLessonLevel() == LessonLevelEnum.RTA);

            if(logger.isInfoEnabled()){
                logger.info("Created network session for "+getModuleName()+". (queue inbox = "+moduleQueueInboxName+").");
            }
        } catch (ModuleConnectionConfigurationException moduleConnectionException){
            System.out.println("Caught exception while trying to establish connection to ActiveMQ.  Is ActiveMQ running at "+properties.getBrokerURL()+"?");
            throw moduleConnectionException;
        } catch (Throwable e) {
            System.out.println("Caught exception while trying to establish connection to ActiveMQ.  Is ActiveMQ running at "+properties.getBrokerURL()+"?");
            throw new RuntimeException("Module failed to connection to the message bus.", e);
        }
    }

    /**
     * Initialize the module heartbeat
     */
    protected void initializeHeartbeat() {

        moduleHeartbeat = new ModuleHeartbeatDaemon(this);
        moduleHeartbeat.init();
    }

    /**
     * Return whether a client for the subject has already been created.
     *
     * @param subjectName - the subject name to find an existing client of
     * @return boolean - true iff a client has been created for that subject
     */
    protected boolean haveSubjectClient(String subjectName) {
        return networkSession.haveConnection(subjectName);
    }
    
    /**
     * Return a deep copy of the modules that should receive the provided message type according the the network architecture.
     * 
     * @param messageType the message type to get the recipient module types for, i.e. which module types should receive
     * this message type
     * @return a collection of zero or more modules that should receive this message type.  Won't be null.
     */
    protected static List<ModuleTypeEnum> getRecepients(MessageTypeEnum messageType){
        return NetworkSession.getRecepients(messageType);
    }

    /**
     * Return whether a client for the module type has already been created.
     *
     * @param userSession - learner unique id, used to find module connection for, or
     * allocated to, a particular user
     * @param moduleType - the module type to find an existing client of
     * @return boolean - true iff a client has been created for that module type
     */
    protected boolean haveSubjectClient(UserSession userSession, ModuleTypeEnum moduleType) {
        return networkSession.haveConnection(userSession, moduleType);
    }

    /**
     * Remove (i.e. terminate, disconnect) the client connection with the given
     * name. This means there will be no more handling of events from this
     * client connection.
     *
     * @param subjectName - identifies the client connection that will be
     * removed
     * @param destroyDestination - whether the destination should also be destroyed on the message bus.
     * Be careful as using this feature could have consequences on other parts of GIFT that might still need
     * that message bus.
     */
    protected void removeClientConnection(String subjectName, boolean destroyDestination) {
        networkSession.removeConnection(subjectName, true, destroyDestination);
    }

    /**
     * Create a topic client instance for the subject. Note: this will consume
     * messages.
     *
     * @param subjectName - name of the subject to create a topic client
     * instance for
     */
    protected void createSubjectTopicClient(String subjectName) {
        createSubjectTopicClient(subjectName, true);
    }

    /**
     * Create a topic client instance for the subject.
     *
     * @param subjectName - name of the subject to create a topic client
     * instance for
     * @param consumeTopicAlso - whether to setup a message handler for the
     * topic in order to consume messages on that topic
     */
    protected void createSubjectTopicClient(String subjectName, boolean consumeTopicAlso) {

        MessageHandler messageHandler = null;

        if (consumeTopicAlso) {

            //create handler for consuming GIFT messages on this topic
            messageHandler = new MessageHandler() {
                @Override
                public boolean processMessage(Message message) {

                    //ignore incoming messages if currently shutting this module down
                    if (shuttingDown) {
                        return false;
                    }

                    handleMessage(message);

                    return true;
                }
            };
        }

        createSubjectTopicClient(subjectName, messageHandler);
    }

    /**
     * Create a topic client instance for the subject that uses the handler
     * provided to consume messages. NOTE: If the handler is null, messages will
     * not be consumed
     *
     * @param subjectName - name of the subject to create a topic client
     * instance for
     * @param handler - the message handler for the topic used to consume
     * messages on that topic
     */
    protected void createSubjectTopicClient(String subjectName, MessageHandler handler) {
        //let the topic creation logic know this module is a Java Web Start module (currently on the Gateway module)
        networkSession.createSubjectTopicClient(subjectName, handler,
                properties.isRemoteMode());
    }

    /**
     * Create the queue client instance for the subject and add a mapping of
     * module type to that created client instance for later retrieval when a
     * class needs to send a message to the module type's queue.
     *
     * @param subjectName - name of the subject to create a module queue client
     * instance for
     * @param moduleType - the module type the message client is associated with
     * @param destroyOnCleanup whether the client connection should be removed from the message bus
     * when cleaning up / shutting down this module
     */
    protected void createSubjectQueueClient(String subjectName, ModuleTypeEnum moduleType, boolean destroyOnCleanup) {
        networkSession.createSubjectQueueClient(subjectName, moduleType, destroyOnCleanup);
    }

    /**
     * Create a queue client instance for the subject with a custom message
     * handler.
     *
     * @param subjectName - name of the subject to create a queue client
     * instance for
     * @param handler - the message handler to handle messages sent to this
     * queue
     * @param destroyOnCleanup - whether the destination should also be destroyed on the message bus.
     * Be careful as using this feature could have consequences on other parts of GIFT that might still need
     * that message bus.
     */
    protected void createSubjectQueueClient(String subjectName, RawMessageHandler handler, boolean destroyOnCleanup) {
        networkSession.createSubjectQueueClient(subjectName, handler, destroyOnCleanup);
    }

    /**
     * Send a reply message without needing to be notified about a reply from
     * the recipient.
     *
     * @param messageReplyingToo - the message being replied too. More
     * importantly the message with the sequence number to reply too
     * @param payload - the payload of the message to send
     * @param messageType - the type of message being sent
     * @return boolean - whether the message was sent
     */
    public boolean sendReply(Message messageReplyingToo, Object payload, MessageTypeEnum messageType) {
        return sendReply(messageReplyingToo, payload, messageType, null);
    }

    /**
     * Send a reply message with the added benefit of being notified when there
     * is a reply from the recipient.
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
        return networkSession.sendReply(messageReplyingToo, payload, messageType, callback);
    }

    /**
     * Send a non-user/non-domain session message to a specific module type.
     *
     * @param moduleType - the module type to send this message too
     * @param payload - the payload of the message to send
     * @param messageType - the type of message being sent
     * @param callback - used to notify of reply message to this message being
     * sent
     * @return boolean - whether the message was sent successfully
     */
    protected boolean sendMessage(ModuleTypeEnum moduleType, Object payload, MessageTypeEnum messageType, MessageCollectionCallback callback) {
        return networkSession.sendMessage(moduleType, payload, messageType, callback);
    }

    /**
     * Send a non-user/non-domain session message to one or more recipients based on the GIFT communication architecture as defined statically in GIFT (NetworkSession.java).
     *
     * @param payload - the payload of the message to send
     * @param messageType - the type of message being sent
     * @param callback - used to notify of reply message to this message being sent
     * @return boolean - whether the message was sent successfully
     */
    protected boolean sendMessage(Object payload, MessageTypeEnum messageType, MessageCollectionCallback callback){
        return networkSession.sendMessage(payload, messageType, callback);
    }

    /**
     * Send a non-user/non-domain session message to one or more recipients
     * based on the GIFT communication architecture as defined statically in
     * GIFT (NetworkSession.java).
     *
     * @param payload - the payload of the message to send
     * @param messageType - the type of message being sent
     * @return A {@link CompletableFuture} that completes successfully when a
     *         message has been received. Can complete exceptionally if there
     *         was a problem sending the message, or if the recipient responded
     *         with a {@link NACK}.
     */
    protected CompletableFuture<Message> sendMessageAsync(Object payload, MessageTypeEnum messageType) {
        final CompletableFuture<Message> future = new CompletableFuture<>();

        networkSession.sendMessage(payload, messageType, new MessageCollectionCallback() {

            private Message reply;

            @Override
            public void success() {
                future.complete(reply);
            }

            @Override
            public void received(Message msg) {
                if (reply == null) {
                    reply = msg;
                }
            }

            @Override
            public void failure(String why) {
                fail(why);
            }

            @Override
            public void failure(Message msg) {
                fail(msg.toString());
            }

            private void fail(String message) {
                future.completeExceptionally(new Exception(message));
            }
        });

        return future;
    }

    /**
     * Send a user session message to a specific module type.
     *
     * @param moduleType - the module type to send this message too
     * @param payload - the payload of the message to send
     * @param userSession - info about the user associated with this user session
     * message
     * @param messageType - the type of message being sent
     * @param callback - used to notify of reply message to this message being
     * sent
     * @return boolean - whether the message was sent successfully
     */
    protected boolean sendUserSessionMessage(ModuleTypeEnum moduleType, Object payload, UserSession userSession, MessageTypeEnum messageType, MessageCollectionCallback callback) {
        return networkSession.sendMessage(moduleType, payload, userSession, messageType, callback);
    }

    /**
     * Send a user session message to one or more recipients based on the GIFT
     * communication architecture as defined statically in GIFT
     * (NetworkSession.java).
     *
     * @param payload - the payload of the message to send
     * @param userSession - info about the user associated with this user session
     * message
     * @param messageType - the type of message being sent
     * @param callback - used to notify of reply message to this message being
     * sent
     * @return boolean - whether the message was sent successfully
     */
    protected boolean sendUserSessionMessage(Object payload, UserSession userSession, MessageTypeEnum messageType, MessageCollectionCallback callback) {
        return networkSession.sendUserSessionMessage(payload, userSession, messageType, callback);
    }

    /**
     * Send a user session message to one or more recipients based on the GIFT
     * communication architecture as defined statically in GIFT
     * (NetworkSession.java).
     *
     * @param payload - the payload of the message to send
     * @param userSession - info about the user associated with this user
     *        session message
     * @param messageType - the type of message being sent
     * @return A {@link CompletableFuture} that completes successfully when a
     *         response is received from the recipient. Can complete
     *         exceptionally if there was an error while sending the message, or
     *         if the recipient responds with a NACK.
     */
    protected CompletableFuture<Message> sendUserSessionMessageAsync(Object payload, UserSession userSession, MessageTypeEnum messageType) {
        final CompletableFuture<Message> future = new CompletableFuture<>();
        networkSession.sendUserSessionMessage(payload, userSession, messageType,
                new MessageCollectionCallback() {

                    private Message reply;

                    @Override
                    public void success() {
                        future.complete(reply);
                    }

                    @Override
                    public void received(Message msg) {
                        if (reply == null) {
                            reply = msg;
                        }
                    }

                    @Override
                    public void failure(String why) {
                        fail(why);
                    }

                    @Override
                    public void failure(Message msg) {
                        fail(msg.toString());
                    }

                    private void fail(String message) {
                        future.completeExceptionally(new Exception(message));
                    }
                });
                return future;
    }

    /**
     * Send a user session message to one or more recipients based on the GIFT
     * communication architecture as defined statically in GIFT
     * (NetworkSession.java).
     *
     * @param payload The payload of the message to send
     * @param userSession Info about the user associated with this user session
     *        message
     * @param messageType The type of message being sent
     * @return A {@link CompletableFuture} that completes when the
     *         {@link Message} has been successfully sent and the response has
     *         been received. Can complete exceptionally if there was a problem
     *         sending/receiving a message or if multiple messages were
     *         received.
     */
    protected CompletableFuture<Message> sendUserSessionMessage(Object payload, UserSession userSession, MessageTypeEnum messageType) {
        final CompletableFuture<Message> future = new CompletableFuture<>();
        networkSession.sendUserSessionMessage(payload, userSession, messageType,
                new MessageCollectionCallback() {

                    private Message receivedMsg = null;

                    @Override
                    public void success() {
                        future.complete(receivedMsg);
                    }

                    @Override
                    public void received(Message msg) {
                        if (receivedMsg == null) {
                            receivedMsg = msg;
                        } else {
                            fail(new Exception("Received the message " + msg + " when the message " + receivedMsg + " was already received."));
                        }
                    }

                    @Override
                    public void failure(String why) {
                        fail(new Exception("There was a problem sending the message " + payload + ": " + why));
                    }

                    @Override
                    public void failure(Message msg) {
                        fail(new Exception("There was a problem sending the message " + payload + ": " + msg));
                    }

                    private void fail(Exception ex) {
                        future.completeExceptionally(ex);
                    }
                });

        return future;
    }

    /**
     * Send a domain session message to a specific module type.
     *
     * @param moduleType - the module type to send this message too
     * @param payload - the payload of the message to send
     * @param userSession - info about the user associated with this domain session
     * message
     * @param domainSessionId - the unique domain session id associated with
     * this domain session message
     * @param messageType - the type of message being sent
     * @param callback - used to notify of reply message to this message being
     * sent. Can be null.
     * @return boolean - whether the message was sent successfully
     */
    protected boolean sendDomainSessionMessage(ModuleTypeEnum moduleType, Object payload, UserSession userSession, int domainSessionId,
            MessageTypeEnum messageType, MessageCollectionCallback callback) {
        return networkSession.sendMessage(moduleType, payload, userSession, domainSessionId, messageType, callback);
    }

    /**
     * Send a domain session message to a specific module type.
     *
     * @param moduleType the {@link ModuleTypeEnum} to send this message to.
     * @param payload the {@link Object} payload of the message to send.
     * @param userSession The {@link UserSession} about the user associated with
     *        this domain session message
     * @param domainSessionId the unique domain session id associated with this
     *        domain session message
     * @param messageType the {@link MessageTypeEnum} of the message being sent
     * @return A {@link CompletableFuture} that completes when a reply is
     *         received. Can complete exceptionally if an unexpected reply was
     *         received or if there was a problem sending the message.
     */
    protected CompletableFuture<DomainSessionMessage> sendDomainSessionMessageAsync(ModuleTypeEnum moduleType, Object payload, UserSession userSession, int domainSessionId, MessageTypeEnum messageType) {
        final CompletableFuture<DomainSessionMessage> future = new CompletableFuture<>();
        sendDomainSessionMessage(moduleType, payload, userSession, domainSessionId, messageType, new MessageCollectionCallback() {

            private DomainSessionMessage msg = null;

            @Override
            public void success() {
                future.complete(msg);
            }

            @Override
            public void received(Message msg) {
                if (this.msg == null) {
                    if (msg instanceof DomainSessionMessage) {
                        this.msg = (DomainSessionMessage) msg;
                    } else {
                        failure("Expected a domain session message but received " + msg);
                    }
                } else {
                    failure("A second reply was received " + msg);
                }
            }

            @Override
            public void failure(String why) {
                future.completeExceptionally(
                        new Exception("There was a problem sending the domain session message: " + why));
            }

            @Override
            public void failure(Message msg) {
                failure(msg.toString());
            }
        });

        return future;
    }

    /**
     * Send a domain session message to a list of module types.
     *
     * @param moduleTypes - the module types to send this message too
     * @param payload - the payload of the message to send
     * @param userSession - info about the user associated with this domain session
     * message
     * @param domainSessionId - the unique domain session id associated with
     * this domain session message
     * @param experimentId unique experiment id associated with the domain session.  Can be null.
     * @param messageType - the type of message being sent
     * @param callback - used to notify of reply message to this message being
     * sent
     * @return boolean - whether the message was sent successfully
     */
    protected boolean sendDomainSessionMessage(ModuleTypeEnum[] moduleTypes, Object payload, UserSession userSession, int domainSessionId, String experimentId,
            MessageTypeEnum messageType, MessageCollectionCallback callback) {
        return networkSession.sendMessage(moduleTypes, payload, userSession, domainSessionId, messageType, callback);
    }

    /**
     * Send a domain session message to one or more recipients based on the GIFT
     * communication architecture as defined statically in GIFT
     * (NetworkSession.java).
     *
     * @param payload - the payload of the message to send
     * @param userSession - info about the user associated with this domain session
     * message
     * @param domainSessionId - the unique domain session id associated with
     * this domain session message
     * @param messageType - the type of message being sent
     * @param callback - used to notify of reply message to this message being
     * sent.  Can be null.  When null the recipient won't need to send a response that the message was processed because
     * the message header will show "NeedsACK":false.
     * @return boolean - whether the message was sent successfully
     */
    protected boolean sendDomainSessionMessage(Object payload, UserSession userSession, int domainSessionId,
            MessageTypeEnum messageType, MessageCollectionCallback callback) {
        return networkSession.sendMessage(payload, userSession, domainSessionId, messageType, callback);
    }

    /**
     * Send a domain session message to one or more recipients based on the GIFT
     * communication architecture as defined statically in GIFT
     * (NetworkSession.java).
     *
     * @param payload - the payload of the message to send
     * @param domainSession - the domain session associated with this domain session message
     * @param messageType - the type of message being sent
     * @param callback - used to notify of reply message to this message being
     * sent
     * @return boolean - whether the message was sent successfully
     */
    protected boolean sendDomainSessionMessage(Object payload, DomainSession domainSession, MessageTypeEnum messageType, MessageCollectionCallback callback) {
        return networkSession.sendMessage(domainSession, payload, messageType, callback);
    }

    /**
     * Send a domain session message to one or more recipients based on the GIFT
     * communication architecture as defined statically in GIFT
     * (NetworkSession.java).
     *
     * @param payload - the payload of the message to send
     * @param domainSession - the domain session associated with this domain
     *        session message
     * @param messageType - the type of message being sent
     * @return A {@link CompletableFuture} that completes when a reply is
     *         received. Can complete exceptionally if an unexpected reply was
     *         received or if there was a problem sending the message.
     */
    protected CompletableFuture<DomainSessionMessage> sendDomainSessionMessageAsync(Object payload, DomainSession domainSession, MessageTypeEnum messageType) {
        final CompletableFuture<DomainSessionMessage> future = new CompletableFuture<>();
        sendDomainSessionMessage(payload, domainSession, messageType, new MessageCollectionCallback() {

            private DomainSessionMessage msg = null;

            @Override
            public void success() {
                future.complete(msg);
            }

            @Override
            public void received(Message msg) {
                if (this.msg == null) {
                    if (msg instanceof DomainSessionMessage) {
                        this.msg = (DomainSessionMessage) msg;
                    } else {
                        failure("Expected a domain session message but received " + msg);
                    }
                } else {
                    failure("A second reply was received " + msg);
                }
            }

            @Override
            public void failure(String why) {
                future.completeExceptionally(
                        new Exception("There was a problem sending the domain session message: " + why));
            }

            @Override
            public void failure(Message msg) {
                failure(msg.toString());
            }
        });

        return future;
    }

    /**
     * Send a non-user/non-domain session message to a specific address (i.e.
     * subject)
     *
     * @param subjectName - the destination to send this message too
     * @param payload - the payload of the message to send
     * @param messageType - the type of message being sent
     * @param callback - used to notify of reply message to this message being
     * sent
     * @return boolean - whether the message was sent successfully
     */
    protected boolean sendMessage(String subjectName, Object payload, MessageTypeEnum messageType, MessageCollectionCallback callback) {
        return networkSession.sendMessage(subjectName, payload, messageType, callback);
    }

    /**
     * Send a user session message to a specific address (i.e. subject)
     *
     * @param subjectName - the destination to send this message too
     * @param payload - the payload of the message to send
     * @param userSession - info about the user associated with this user session
     * message
     * @param messageType - the type of message being sent
     * @param callback - used to notify of reply message to this message being
     * sent
     * @return boolean - whether the message was sent successfully
     */
    protected boolean sendUserSessionMessage(String subjectName, Object payload, UserSession userSession, MessageTypeEnum messageType, MessageCollectionCallback callback) {
        return networkSession.sendMessage(subjectName, payload, userSession, messageType, callback);
    }

    /**
     * Send a domain session message to a specific address (i.e. subject)
     *
     * @param subjectName - the destination to send this message too
     * @param payload - the payload of the message to send
     * @param userSession - info about the user associated with this domain session
     * message
     * @param domainSessionId - the unique domain session id associated with
     * this domain session message
     * @param experimentId unique experiment id associated with the domain session.  Can be null.
     * @param messageType - the type of message being sent
     * @param callback - used to notify of reply message to this message being
     * sent
     * @return boolean - whether the message was sent successfully
     */
    protected boolean sendDomainSessionMessage(String subjectName, Object payload, UserSession userSession, int domainSessionId, String experimentId,
            MessageTypeEnum messageType, MessageCollectionCallback callback) {
        return networkSession.sendMessage(subjectName, payload, userSession, domainSessionId, messageType, callback);
    }

    /**
     * Register a message handler to receive training app game state messages that are received via a
     * gateway topic client connection (if one exists for this module).
     *
     * @param handler callback used to process training app state messages
     */
    protected void registerTrainingAppGameStateMessageHandler(RawMessageHandler handler){
        networkSession.registerTrainingAppGameStateMessageHandler(handler);
    }

    /**
     * Register a message handler to receive embedded training app game state
     * messages that are received via a tutor topic client connection (if one
     * exists for this module).
     *
     * @param topicName The name of the topic to which to register the
     *        {@link RawMessageHandler}.
     * @param handler callback used to process embedded training app state
     *        messages
     */
    protected void registerEmbeddedTrainingAppGameStateMessageHandler(String topicName, RawMessageHandler handler) {
    	networkSession.registerEmbeddedTrainingAppGameStateMessageHandler(topicName, handler);
    }

    /**
     * Register a message handler to receive training app game state messages
     * that are received via a domain topic client connection (if one exists for
     * this module) as part of a log playback service.
     *
     * @param topicName The name of the topic to which to register the
     *        {@link RawMessageHandler}.
     * @param handler callback used to process embedded training app state
     *        messages
     */
    protected void registerLogPlaybackTrainingAppGameStateMessageHandler(String topicName, RawMessageHandler handler) {
        networkSession.registerLogPlaybackTrainingAppGameStateMessageHandler(topicName, handler);
    }

    /**
     * Register for allocated module events.
     *
     * @param listener used for notification of module allocations
     */
    protected void registerAllocatedModuleHandler(AllocatedModuleListener listener){
        networkSession.addAllocatedModuleListener(listener);
    }

    /**
     * Remove all the client connections for the domain session linked (i.e.
     * grouped, allocated together for the user) modules
     *
     * @param userSession - the key that links all of the allocated modules for a
     * user whose domain session is ending
     */
    protected void releaseDomainSessionModules(UserSession userSession) {
        //let the logic that is cleaning up message bus connections to modules that this module is in server mode
        networkSession.releaseDomainSessionModules(userSession, properties.getDeploymentMode() == DeploymentModeEnum.SERVER);
    }

    /**
     * Remove all the client connections for the user session linked (i.e.
     * grouped, allocated together for the user) modules
     *
     * @param userSession - the key that links all of the allocated modules for a
     * user whose domain session is ending
     */
    protected void releaseUserSessionModules(UserSession userSession){
        //let the logic that is cleaning up message bus connections to modules that this module is in server mode
        networkSession.releaseUserSessionModules(userSession, properties.getDeploymentMode() == DeploymentModeEnum.SERVER);
    }

    /**
     * Handle the module allocation request message be determining if this
     * module can fill the request and then notifying the requesting module.
     * Finally make sure this module is able to use the modules it needs that
     * the requester module is already using (i.e. the modules that have already
     * been allocated to the requesting module)
     *
     * @param message the incoming module allocation request to process
     */
    protected void handleModuleAllocationRequestMessage(Message message) {

        UserSession userSession = null;
        if(message instanceof UserSessionMessage){
            userSession = ((UserSessionMessage)message).getUserSession();
        }

        ModuleAllocationRequest request = (ModuleAllocationRequest) message.getPayload();

        if(logger.isInfoEnabled()){
            logger.info("Received module allocation request of "+request);
        }

        //determine if this module can support the allocation request
        updateAllocationStatus(userSession);
        if (networkSession.isAlreadyAllocated(message) || !allocationStatus.isFullyAllocated()) {

            if(logger.isInfoEnabled()){
                logger.info("Allocated this module to " + request.getRequestorInfo().getModuleName() + " for module request = " + request);
            }
            try {
                allocateToUser(userSession);

                //gather needed modules from requestor's allocated modules
                networkSession.useSameModules(message, properties.getDeploymentMode() == DeploymentModeEnum.SERVER);

                //then, after success, send reply to request
                sendReply(message, new ModuleAllocationReply(), MessageTypeEnum.MODULE_ALLOCATION_REPLY);

            } catch (Exception e) {
                logger.error("Caught exception while trying to handle allocation request for which this module should be able to satisfy the request", e);
                sendReply(message, new ModuleAllocationReply("Module should be able to satisfy request but failed to do so"), MessageTypeEnum.MODULE_ALLOCATION_REPLY);
            }

        } else {

            //send reply to request
            if(logger.isInfoEnabled()){
                logger.info("Not able to allocated this module to " + request.getRequestorInfo().getModuleName() + " for module request = " + request);
            }

            String reason;
            if(properties.getDeploymentMode() != DeploymentModeEnum.SERVER){
                reason = "This module can't be allocated to another user because it is fully allocated already.\n"
                        + "I see that you are running in a mode other than Server.  Please be aware of the limitations of the Sensor and Gateway modules (see GIFT Configuration Settings documentation).";
            }else{
                reason = "This module can't be allocated to another user because it is fully allocated already.";
            }
            ModuleAllocationReply allocationReply = new ModuleAllocationReply(reason);
            Collection<UserSession> allocatedSessions = allocationStatus.getAllocatedSessions();
            if(!allocatedSessions.isEmpty()){
                StringBuffer sb = new StringBuffer();
                sb.append("The following user session are using this module: {\n");
                for(UserSession uSession : allocatedSessions){
                    sb.append(uSession).append("\n");
                }
                sb.append("}");
                allocationReply.setAdditionalInformation(sb.toString());
            }
            sendReply(message, allocationReply, MessageTypeEnum.MODULE_ALLOCATION_REPLY);
        }

    }

    /**
     * Request that a module of the given type is allocated to this module by
     * sending a request message to a module of the given type. If the request fails or
     * succeeds the callback will be notified.
     *
     * @param userSession - info about the learner, used as an additional key for
     * allocation and grouping of modules supporting this user during the
     * session
     * @param moduleType The module type that a connection is needed for
     * @param selectionCallback The message callback for allocation message
     * related events
     */
    protected void selectModule(UserSession userSession, final ModuleTypeEnum moduleType, final MessageCollectionCallback selectionCallback) {
        selectModule(userSession, moduleType, selectionCallback, new ConnectionFilter());
    }

    /**
     * Request that a module of the given type is allocated to this module by
     * sending a request message to a module of the given type. If the request fails or
     * succeeds the callback will be notified.
     *
     * @param userSession - info about the learner, used as an additional key for
     * allocation and grouping of modules supporting this user during the
     * session
     * @param moduleType The module type that a connection is needed for
     * @param selectionCallback The message callback for allocation message related events
     * @param clientInfo - information about the tutor client that could be used for filtering potential connections
     *
     */
    protected void selectModule(UserSession userSession, final ModuleTypeEnum moduleType, final MessageCollectionCallback selectionCallback, WebClientInformation clientInfo) {
        //let the logic creating a connection to the module know that the requesting module is in server mode
        ConnectionFilter filter = NetworkSession.createConnectionFiler(clientInfo);
        selectModule(userSession, moduleType, selectionCallback, filter);
    }
    
    /**
     * Request that a module of the given type is allocated to this module by sending a request
     * message to a module of the given type.  If the request fails or succeeds the callback will be notified.
     * 
     * @param userSession - info about the learner, used as an additional key for
     * allocation and grouping of modules supporting this user during the
     * session
     * @param moduleType The module type that a connection is needed for
     * @param selectionCallback The message callback for allocation message related events
     * @param filter optional information on addresses to ignore or match (required) when selecting a module connection.  Can be null.
     */
    protected void selectModule(UserSession userSession, final ModuleTypeEnum moduleType, final MessageCollectionCallback selectionCallback, ConnectionFilter filter) {
        networkSession.selectModule(userSession, moduleType, selectionCallback, moduleStatus, filter, properties.getDeploymentMode() == DeploymentModeEnum.SERVER);
    }

    /**
     * Request that the module represented by the provided module status is allocated to this module by
     * sending a request message to that module. If the request fails or
     * succeeds the callback will be notified.
     *
     * @param userSession - info about the learner, used as an additional key for
     * allocation and grouping of modules supporting this user during the
     * session
     * @param selectionCallback The message callback for allocation message related events
     * @param moduleToSelect information about the module to select
     * @param destroyOnCleanup - whether the destination connected to should also be destroyed on the message bus when this module disconnects from it.
     * Be careful as using this feature could have consequences on other parts of GIFT that might still need
     * that message bus.
     */
    protected void selectModule(UserSession userSession, final MessageCollectionCallback selectionCallback, ModuleStatus moduleToSelect, boolean destroyOnCleanup) {
        networkSession.selectModule(userSession, moduleToSelect.getModuleType(), selectionCallback, moduleStatus, moduleToSelect, destroyOnCleanup);
    }

    /**
     * Request that a UMS module be allocated to this module by sending a request message to a module of that type.
     * If the request fails or succeeds the callback will be notified. This method should only be used for module allocation
     * when a user id is not known (e.g. create new user request).
     *
     * @param selectionCallback - The message callback for allocation message
     */
    protected void selectUMSModule(final MessageCollectionCallback selectionCallback){
        networkSession.selectUMSModule(selectionCallback, moduleStatus);
    }

    /**
     * Request that a Gateway module be allocated to this module by sending a
     * request message to a module of that type. If the request fails or
     * succeeds the callback will be notified. This method should only be used
     * for module allocation when a user id is not known (e.g. establishing
     * interop connections for the web monitor).
     *
     * @param selectionCallback - The message callback for allocation message
     * @param moduleStatus The {@link ModuleStatus} of the module allocating the
     *        specified module type.
     */
    protected void selectGatewayModule(final MessageCollectionCallback selectionCallback, ModuleStatus moduleStatus){
        networkSession.selectModule(
                new UserSession(UserSessionMessage.PRE_USER_UNKNOWN_ID),
                ModuleTypeEnum.GATEWAY_MODULE, selectionCallback,
                moduleStatus,
                new ConnectionFilter(),
                false);
    }

    /**
     * Request that a Domain module be allocated to this module by sending a request message to a module of that type.
     * If the request fails or succeeds the callback will be notified. This method should only be used for module allocation
     * when a user id is not known (e.g. start experiment request).
     *
     * @param selectionCallback - The message callback for allocation message
     */
    protected void selectDomainModule(final MessageCollectionCallback selectionCallback){
        networkSession.selectDomainModule(selectionCallback, moduleStatus);
    }

    /**
     * Sets up a module client for the given user session to the module of the given type matching the given filter
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
    protected void linkModuleClient(final UserSession userSession, final ModuleTypeEnum moduleType, final ConnectionFilter filter) {
    	networkSession.linkModuleClient(userSession, filter, moduleType);
    }

    /**
     * Cleans up any resources the module is using and terminates the module
     */
    public void killModule() {

        if(logger.isInfoEnabled()){
            logger.info("Killing module named "+getModuleName());
        }

        cleanup();

        if(moduleMode.equals(ModuleModeEnum.POWER_USER_MODE)) {
            if(logger.isInfoEnabled()){
                logger.info("Exiting for "+ModuleModeEnum.POWER_USER_MODE+" mode.");
            }
        	System.exit(0);

        } else {

        	synchronized(this) {
        	    if(logger.isInfoEnabled()){
        	        logger.info("Single process mode - notifying thread for " + moduleName);
        	    }
        		this.notify();
        	}
        }
    }

    /**
     * The module is closing. Cleans up any resources the module is using
     */
    protected void cleanup() {

        if(logger.isInfoEnabled()){
            logger.info("Cleaning up module");
        }

        shuttingDown = true;

        if (moduleHeartbeat != null) {
            moduleHeartbeat.shutdown();
        }

        //destroy any message bus destinations flagged by this module
        //Note: a remote JWS gateway module most likely won't have access to the JMX code executed
        //      in MessageClient.
        networkSession.cleanup(!properties.isRemoteMode());
    }

    /**
     * Show a common module started message and prompt for modules
     */
    protected void showModuleStartedPrompt() {

        if(moduleMode.equals(ModuleModeEnum.POWER_USER_MODE)) {

            System.out.println(moduleName + " started at "+TimeUtil.timeFirstFormat.format(new Date())+".\nCheck the appropriate module log in 'GIFT\\output\\logger\\module' for more details");

            try {
	            BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
	            String input = null;
	            do {
	                System.out.print("\nPress Enter to close this module.\n");
	                input = inputReader.readLine();

	            } while (input != null && input.length() != 0);
	        } catch (Exception e) {
	            System.err.println("Caught exception while reading input: \n");
	            e.printStackTrace();
	        }
        } else {
        	synchronized(this) {
        		try {
        		    if(logger.isInfoEnabled()){
        		        logger.info("Module " + moduleName + " is running.");
        		    }
					this.wait();
				} catch (InterruptedException e) {
					logger.error("Caught exception while trying to wait thread.", e);
				}
        	}
        }
    }

    /**
     * Show a common module unexpected exit prompt and then after the user presses enter, it exits the program.
     *
     * @param mode the running mode of this module instance
     */
    protected static void showModuleUnexpectedExitPrompt(ModuleModeEnum mode) {
        System.out.println("The module has exited, check prompt and log for more details");

        //attempt to close the splash screen (if available)
        try{
            SplashScreen splash = SplashScreen.getSplashScreen();
            if(splash != null){
                splash.close();
            }
        }catch(@SuppressWarnings("unused") Exception e){}

        if(mode.equals(ModuleModeEnum.POWER_USER_MODE)) {
	        try {
	            BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
	            String input = null;
	            do {
	                System.out.print("Press Enter to Exit");
	                input = inputReader.readLine();

	            } while (input.length() != 0);

	        } catch (Exception e) {

	            System.err.println("Caught exception while reading input: \n");
	            e.printStackTrace();
	        }

	        System.out.println("Good-bye");
        	System.exit(101);
        // Single Process Mode
        } else {
        	// No need to print anything to console or read input, since there is only a system tray.
        	System.exit(101);
        }
    }

    /**
     * Checks the module mode from the main args and sets it onto the module.
     * @param args The main arguments
     * @return the running mode of this module instance
     */
    protected static ModuleModeEnum checkModuleMode(String[] args) {
    	if(args.length > 0) {
    		if(args[0].equals(ModuleModeEnum.LEARNER_MODE.getName())) {
    			return ModuleModeEnum.LEARNER_MODE;
    		} else {
    			return ModuleModeEnum.POWER_USER_MODE;
    		}
    	} else {
    		return ModuleModeEnum.POWER_USER_MODE;
    	}
    }

    /**
     * Check and update the allocation status of this module for the user session specified.
     * i.e. determine if the allocation status can supportReturn whether or not this module is currently allocated at its capacity.
     *
     * @param userSession - the user session info for the incoming module allocation request.  Can be null.
     */
    public void updateAllocationStatus(UserSession userSession){
        //nothing to do if the module instance doesn't have specific logic
    }

    /**
     * This module is being allocated to a unique user.  If this module can only support
     * a single user and/or needs to keep track of the users being allocated to this module, the implementation
     * class of this abstract class should override this method.
     *
     * @param userSession - information about the user session (including the unique user id of the learner)
     *                      for which this module is being allocated.
     *             Note: this value can be null when the allocation happens outside of a user session (e.g. Tutor-UMS new user request)
     * @throws Exception - thrown if there was a severe issue in allocating this module to the user.
     */
    public void allocateToUser(UserSession userSession) throws Exception{

        if(userSession == null){
            return;
        }

        allocationStatus.addUserSession(userSession);
    }

    /**
     * Return the maximum number of domain session allocations for this module
     *
     * @return int
     */
    protected int getMaxNumberOfAllocations() {

        return Integer.MAX_VALUE;
    }

    /**
     * Return the unique name of the module
     *
     * @return String
     */
    public String getModuleName() {

        return moduleName;
    }

    /**
     * Return the URI of the message broker
     *
     * @return URI The URI of the message broker
     */
    public URI getMessageBrokerURI() {

        return networkSession.getMessageBrokerURI();
    }

    /**
     * Return the running mode of the module.
     *
     * @return The running mode of the module.
     */
    public ModuleModeEnum getModuleMode() {
    	return moduleMode;
    }

    /**
     * Sets the running mode of the module.
     *
     * @param mode Running mode of the module.
     */
    public void setModuleMode(ModuleModeEnum mode) {
        logger.info("Setting module mode to "+mode+".");
    	moduleMode = mode;
    }

    /**
     * Initialize the module instance
     *
     * @throws IOException if there was a problem initializing this module
     */
    protected abstract void init() throws IOException ;

    /**
     * Return the module type
     *
     * @return ModuleTypeEnum the type of module
     */
    protected abstract ModuleTypeEnum getModuleType();

    /**
     * Handle the message by calling consumer logic for the particular message
     * type.
     *
     * @param message - the received message which needs to be acted upon
     */
    protected abstract void handleMessage(Message message);

    /**
     * Create and send a module status message over the network.
     */
    public abstract void sendModuleStatus();

    /**
     * This inner class is used to wrap information about a module's allocation status.
     * i.e. what user sessions are using this module and whether or not the module
     * is fully allocated.
     *
     * @author mhoffman
     *
     */
    public class AllocationStatus{

        /** mapping of user hash identifier to current user session */
        private Map<Integer, UserSession> allocatedSessions = new HashMap<>();

        /** whether this module is fully allocated and can't support more user sessions */
        private boolean fullyAllocated;

        /**
         * Return whether this module is fully allocated and can't support more user sessions
         *
         * @return true iff this module is fully allocated and can't support more user sessions
         */
        public boolean isFullyAllocated() {
            return fullyAllocated;
        }

        /**
         * Set whether this module is fully allocated and can't support more user sessions
         *
         * @param fullyAllocated can this module support running more user sessions
         */
        public void setFullyAllocated(boolean fullyAllocated) {
            this.fullyAllocated = fullyAllocated;
        }

        /**
         * Return the collection of user session allocated to this module.
         *
         * @return can be empty but not null
         */
        public Collection<UserSession> getAllocatedSessions() {
            return allocatedSessions.values();
        }

        /**
         * Add a user session to this allocation status object.
         * Duplicate sessions will not be added.
         *
         * @param userSession a user session this module is allocate to
         */
        public void addUserSession(UserSession userSession){

            if(!allocatedSessions.containsKey(getUserHash(userSession))){
                allocatedSessions.put(getUserHash(userSession), userSession);
            }
        }

        /**
         * Remove the specified user session from this allocation status object.
         * This is used when the user session is no longer using this module.
         *
         * @param userSession the session to remove
         * @return true if the session was found and removed
         */
        public boolean removeUserSession(UserSession userSession){
            return allocatedSessions.remove(getUserHash(userSession)) != null;
        }

        /**
         * Get the unique hash value for the user session provided.  As of now this is a copy of the
         * hashCode method in UserSession.  The reason it is copied here is that DomainSession also implements
         * hashCode but we need the ability to retrieve a hash code based on user session info no matter
         * if the user session is pre or during a domain session.
         *
         * @param userSession  the user session to get a hash code for
         * @return the user session hash code
         */
        private int getUserHash(UserSession userSession){

            int hashCode = 0;

            hashCode |= userSession.getUserId() << 0;
            hashCode |= userSession.getExperimentId() != null ? userSession.getExperimentId().hashCode() << 4 : 0;
            hashCode |= userSession.getGlobalUserId() != null ? userSession.getGlobalUserId().hashCode() << 6 : 0;

            return hashCode;
        }

        @Override
        public String toString(){

            StringBuilder sb = new StringBuilder();
            sb.append("[AllocationStatus: ");
            sb.append("fullyAllocated = ").append(isFullyAllocated());

            sb.append(", users = {");
            for(UserSession userSession : getAllocatedSessions()){
                sb.append(" ").append(userSession).append(",");
            }
            sb.append("}");

            sb.append("]");
            return sb.toString();
        }
    }
}
