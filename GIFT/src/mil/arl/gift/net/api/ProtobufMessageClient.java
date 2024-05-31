/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQMessageConsumer;
import org.apache.activemq.command.ActiveMQDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.InvalidProtocolBufferException;

import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.net.api.message.RawProtobufMessageHandler;

/**
 * A message client which is used to send and receive protobuf messages via an
 * ActiveMQ message bus.
 *
 * @author tflowers
 *
 * @param <T> The type of protobuf message that is sent and received from the
 *        message bus.
 */
public abstract class ProtobufMessageClient<T extends AbstractMessage> implements MessageListener {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ProtobufMessageClient.class);

    /** The default URL of the ActiveMQ message broker. */
    private final static String DEFAULT_URL = "tcp://localhost:61618";

    /**
     * the URL of the message bus JMX, used to remove destinations on the
     * message bus
     */
    private final static String JMX_URL = CommonProperties.getInstance().getJmxUrl();

    /** see activemq.xml for matching value */
    private final static String BROKER_NAME = "localhost";

    /**
     * An enumeration that is used to identify a {@link Destination} as either a
     * queue or topic.
     *
     * @author tflowers
     *
     */
    public enum DestinationType {
        /** A queue {@link Destination}. */
        QUEUE,
        /** A topic {@link Destination}. */
        TOPIC
    }

    /** The type of destination this client is connected to. */
    private final DestinationType destinationType;

    /** The URL of the message broker. */
    private final String connectionUrl;

    /** The clientID (name) of this client. */
    private String clientId = null;

    /** The topic/queue this client is interacting with. */
    private String subjectName = null;

    /**
     * The factory class that creates the client connection to the message
     * broker.
     */
    private ActiveMQConnectionFactory connectionFactory = null;

    /** The connection to the message broker, creates the connection session. */
    private Connection brokerConnection = null;

    /** The current session of the connection to the message broker. */
    private Session brokerSession = null;

    /** Instance that sends messages to the message broker. */
    private MessageProducer clientMessageProducer = null;

    /** Instance that receives messages from the message broker. */
    private ActiveMQMessageConsumer clientMessageConsumer = null;

    /** the message bus destination (e.g. topic) connected to by this client */
    protected ActiveMQDestination activeMQDestination = null;

    /**
     * Control to stop the message consumer from listening when it's time for
     * the thread to stop.
     */
    private volatile boolean isActive = true;

    /**
     * The message handler that will take the callback when a message is
     * received.
     */
    private RawProtobufMessageHandler<T> msgHandler = null;

    /**
     * If the resource the client is connecting to should be flushed before
     * handling messages.
     */
    private boolean pruneOldMessages = true;

    /**
     * The {@link AbstractMessage} from which all generated messages are
     * created.
     */
    private final T messageTemplate;

    /**
     * The {@link AbstractMessage} from which uncaught error messages are
     * created..
     */
    private final T errorMessageTemplate;
    
    /**
     * The {@link AbstractMessage} from which unprocessed ACK messages are
     * created..
     */
    private final T ackMessageTemplate;

    /** A counter used to ensure all correlation ids are unique */
    private final AtomicInteger lastCorrelationId = new AtomicInteger();

    /**
     * A map of correlation ids to the corresponding {@link CompletableFuture}.
     */
    private final ConcurrentMap<String, CompletableFuture<T>> outstandingFutures = new ConcurrentHashMap<>();

    /** is there an active connection to the ActiveMQ message bus */
    private boolean isOnline = false;
    
    /** whether to try to reconnect to the message bus when a JMS Exception is received while attempting
     * to connect or when connected */
    private boolean keepTryingOnFail = false;

    /**
     * The thread that periodically listens for messages arriving on the client.
     */
    private Thread listenThread = null;

    /** The execution body of the {@link #listenThread}. */
    private final Runnable listenRunnable = () -> {
        try {
            listen();
        } catch (Throwable e) {
            logger.error("Caught exception while listening on " + getSubjectName() + ".", e);
        }
    };

    /**
     * The consumer which receives the responses to the requests sent by this
     * client.
     */
    private MessageConsumer responseMessageConsumer = null;

    /**
     * The producer which sends responses to received requests.
     */
    private MessageProducer responseMessageProducer = null;

    /**
     * The destination to which responses to this client's requests should be
     * sent.
     */
    private Destination responseDestination = null;

    /**
     * The thread which processes responses to this client.
     */
    private Thread listenResponseThread = null;

    /**
     * The executable body for {@link #listenResponseThread}.
     */
    private final Runnable listenResponseRunnable = () -> {
        try {
            responseDestination = brokerSession.createTemporaryQueue();
            responseMessageConsumer = brokerSession.createConsumer(responseDestination);
            responseMessageConsumer.setMessageListener(this::handleResponse);
        } catch (JMSException jmsEx) {
            logger.error("There was a problem creating the response queue and consumer", jmsEx);
            return;
        }
    };

    /**
     * The thread that is used to periodically attempt to reconnect to the
     * message bus when the client has become disconnected.
     */
    private Thread reconnectThread = null;

    /** The execution body of the {@link #reconnectThread}. */
    private final Runnable reconnectRunnable = () -> {
        try {
            reconnect();
        } catch (

        Throwable e) {
            logger.error("Caught exception while attempting to reconnect on " + getSubjectName() + ".", e);
        }
    };

    /**
     * Listens for exceptions on the connection and reconnects the client upon
     * error
     */
    private final ExceptionListener jmsExceptionListener = e -> {
        isOnline = false;
        
        if(keepTryingOnFail){
    
            synchronized (reconnectRunnable) {
    
                if (reconnectThread == null || !reconnectThread.isAlive()) {
    
                    reconnectThread = new Thread(reconnectRunnable, getSubjectName() + "_Reconnect_Thread");
    
                    reconnectThread.start();
    
                } else {
    
                    logger.warn("Tried to start the reconnect thread while it was already running.");
                }
            }
        }
    };

    /** listeners of events from this client */
    // MH: changed to Collections.newSetFromMap from HashSet in an attempt to
    // prevent sporadic and hard to debug ConcurrentModificationExceptions
    // during client cleanup/termination code even with using synchronized. This
    // issue needs more attention, luckily it doesn't happen often
    // and it only happens when shutting down gift modules.
    private final Set<ProtobufMessageClientConnectionListener<T>> connectionListeners = Collections
            .newSetFromMap(new ConcurrentHashMap<ProtobufMessageClientConnectionListener<T>, Boolean>());

    /** List of unprocessed JMS Messages */
    private final List<Message> incomingMessageList = new LinkedList<>();

    /**
     * Constructor
     *
     * @param connectionUrl The URL of the message broker to connect to. If null
     *        the default will be used.
     * @param subjectName The name of the JMS subject to interact with
     * @param pruneOldMessages If old messages should be removed before
     *        listening starts.
     * @param destinationType The type of {@link Destination} to which this
     *        {@link ProtobufMessageClient} is connecting.
     * @param messageTemplate The {@link AbstractMessage} that is used to create
     *        all incoming messages.
     * @param errorMessageTemplate The {@link AbstractMessage} that is used to
     *        create all uncaught error message responses.
     * @param ackMessageTemplate The {@link AbstractMessage} that is used to
     *        send an acknowledgement indicating that a request was received.
     */
    public ProtobufMessageClient(String connectionUrl, String subjectName,
            boolean pruneOldMessages,
            DestinationType destinationType, T messageTemplate, T errorMessageTemplate, T ackMessageTemplate) {

        if (subjectName == null) {
            throw new IllegalArgumentException("The parameter 'subjectName' cannot be null.");
        } else if (destinationType == null) {
            throw new IllegalArgumentException("The parameter 'destinationType' cannot be null.");
        }

        this.connectionUrl = connectionUrl != null ? connectionUrl : DEFAULT_URL;
        this.subjectName = subjectName;
        this.pruneOldMessages = pruneOldMessages;
        this.destinationType = destinationType;
        this.messageTemplate = messageTemplate;
        this.errorMessageTemplate = errorMessageTemplate;
        this.ackMessageTemplate = ackMessageTemplate;
    }

    /**
     * Sets the clientID for this session.
     *
     * @param clientId The clientID to be set for this session.
     */
    final public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Returns the URL of the connection to the message broker
     *
     * @return String The URL to the message broker
     */
    public String getConnectionUrl() {

        return connectionUrl;
    }

    /**
     * Return the subject that is used to connect to in the ActiveMQ message
     * broker.
     *
     * @return String - the subject
     */
    public String getSubjectName() {

        return subjectName;
    }

    /**
     * Gets the active broker session
     *
     * @return Session The active broker session
     */
    protected Session getBrokerSession() {

        return brokerSession;
    }

    /**
     * Sets the message producer that sends messages to the message broker
     *
     * @param producer The message producer that sends messages
     */
    protected void setMessageProducer(MessageProducer producer) {

        if (producer == null) {
            throw new IllegalArgumentException("The message producer can't be null.");
        }

        if (logger.isInfoEnabled()) {
            logger.info("Setting message producer for " + getSubjectName() + ".");
        }
        clientMessageProducer = producer;
    }

    /**
     * Sets the message consumer that receives message from the message broker
     *
     * @param consumer The message consumer that receives messages
     */
    protected void setMessageConsumer(ActiveMQMessageConsumer consumer) {

        if (consumer == null) {
            throw new IllegalArgumentException("The message consumer can't be null.");
        }

        if (logger.isInfoEnabled()) {
            logger.info("Setting message consumer for " + getSubjectName() + ".");
        }
        clientMessageConsumer = consumer;
    }

    /**
     * Sets the {@link RawProtobufMessageHandler} to accept received message
     * call backs.
     *
     * @param handler The {@link RawProtobufMessageHandler} to handle received
     *        messages.
     */
    final public void setMessageHandler(RawProtobufMessageHandler<T> handler) {
        msgHandler = handler;
    }

    /**
     * Returns if this message client also consumes messages
     *
     * @return boolean If this message client consumes messages
     */
    final public boolean isConsumer() {

        return msgHandler != null;
    }

    /**
     * Return whether this message client is still active and has not been
     * ordered to disconnect.
     *
     * @return boolean If this message client is still active
     */
    public boolean isActive() {

        return isActive;
    }

    /**
     * Return whether this message client is still connected to the message
     * broker
     *
     * @return boolean If this message client is still connected
     */
    public boolean isOnline() {

        return isOnline;
    }

    /**
     * Gets the current client ID for this message client
     *
     * @return String The client ID for this client.
     */
    final public String getClientId() {

        return clientId;
    }

    /**
     * Establishes a connection with the message bus.
     *
     * @throws JMSException if there was an error while establishing the
     *         connection to the message bus.
     */
    private void initializeConnection() throws JMSException {

        connectionFactory = new ActiveMQConnectionFactory(connectionUrl);

        if (clientId != null) {

            connectionFactory.setClientID(clientId);
        }

        connectionFactory.setUseRetroactiveConsumer(!pruneOldMessages);

        // MH: fix for problem of 80-300ms delay in sending messages frequently,
        // caused by async publishing
        // http://activemq.apache.org/version-5-performance-tuning.html

        connectionFactory.setUseAsyncSend(true);

        if (logger.isInfoEnabled()) {
            logger.info("Creating broker connection for " + getSubjectName() + ".");
        }
        brokerConnection = connectionFactory.createConnection();
        brokerConnection.setExceptionListener(jmsExceptionListener);
        brokerConnection.start();

        if (logger.isInfoEnabled()) {
            logger.info("Creating broker session for " + getSubjectName() + ".");
        }
        brokerSession = brokerConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        responseMessageProducer = brokerSession.createProducer(null);
    }

    /**
     * Use a JMX connection to destroy the destination provided on the ActiveMQ
     * message bus. This is needed to help cleanup any ActiveMQ threads that
     * could linger even after the client has been closed. See #2177.
     *
     * @param destination the name of a queue or topic on the message bus. Can't
     *        be null or empty.
     * @param destinationType the type of destination. Needed in order to
     *        determine to know what remove command to use. If null the method
     *        will attempt to remove destinations of all types with the given
     *        name.
     * @throws DetailedException if there was a problem connecting and removing
     *         the destination on the message bus.
     */
    public static void attemptDestroy(String destination, DestinationType destinationType) throws DetailedException {

        if (destination == null || destination.isEmpty()) {
            throw new IllegalArgumentException("The destination can't be null or empty.");
        }

        JMXConnector jmxc = null;
        MBeanServerConnection conn = null;
        try {

            if (!JMX_URL.isEmpty()) {
                JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + JMX_URL + "/jmxrmi");
                jmxc = JMXConnectorFactory.connect(url);
                conn = jmxc.getMBeanServerConnection();
            } else {
                logger.error("Unable to destroy the queue/topic named '" + destination + "'.  "
                        + "Please make sure the JmxURL property is defined in the GIFT\\config\\common.properties file.");
            }

        } catch (Throwable t) {
            throw new DetailedException("Unable to destroy the queue/topic named '" + destination + "'.",
                    "Failed to create a JMX connection to '" + JMX_URL + "'.  Please check the ActiveMQ jmxremote "
                            + "properties in GIFT\\external\\activemq\\bin\\win32\\wrapper.conf if running with "
                            + "GIFT\\external\\activemq\\bin\\win32\\activemq.bat -or- GIFT\\external\\activemq\\conf\\activemq.xml "
                            + "if running in Learner Mode.\n\nThe error reads:\n" + t.getMessage(),
                    t);
        }

        if (conn != null) {

            String operationName = null;
            if (destinationType == null) {
                // try removing the queue first, then try the topic of the same
                // name second
                operationName = "removeQueue";
            } else {

                switch (destinationType) {
                case QUEUE:
                    operationName = "removeQueue";
                    break;
                case TOPIC:
                    operationName = "removeTopic";
                    break;
                }
            }

            try {
                ObjectName activeMQ = new ObjectName("org.apache.activemq:BrokerName=" + BROKER_NAME + ",Type=Broker");
                Object[] params = { destination };
                String[] sig = { "java.lang.String" };
                conn.invoke(activeMQ, operationName, params, sig);

                if (logger.isInfoEnabled()) {
                    logger.info("The message bus destination of '" + destination + "' has been successfully removed.");
                }

                if (destinationType == null) {
                    // this successful attempt deleted a queue, now try deleting
                    // the same named topic
                    try {
                        attemptDestroy(destination, DestinationType.TOPIC);
                    } catch (@SuppressWarnings("unused") DetailedException e) {
                        // don't care because the queue was delete, so at least
                        // one destination was deleted
                    }
                }

            } catch (Exception e) {

                if (destinationType == null) {
                    // this failed attempt tried to delete a queue, try again
                    // this time deleting a topic
                    attemptDestroy(destination, DestinationType.TOPIC);
                } else {
                    throw new DetailedException("Unable to destroy the queue/topic named '" + destination + "'.",
                            "Failed to invoke the '" + operationName + "' operation on '" + destination
                                    + "' through JMX connection.  Does that destination exist?"
                                    + " Check the ActiveMQ Console (e.g. http://localhost:8161/admin/index.jsp). The error reads:\n"
                                    + e.getMessage(),
                            e);
                }
            } finally {

                // close the thread
                if (jmxc != null) {
                    closeJMXConnection(destination, jmxc);
                }
            }
        } else {
            logger.error("Unable to get a connection to the JMX Server at url: " + JMX_URL);
        }
    }

    /**
     * Close the JMX thread used by GIFT to destroy topics/queues.
     *
     * @param destination the name of the destination being destroyed.
     * @param jmxc the connection to close.
     */
    private static void closeJMXConnection(final String destination, final JMXConnector jmxc) {

        if (jmxc == null) {
            return;
        }

        // closing in a thread because the JavaDoc for jmxc.close() states:
        // "Closing a connection is a potentially slow operation. For example,
        // if the server has crashed,
        // the close operation might have to wait for a network protocol
        // timeout. Callers that do not
        // want to block in a close operation should do it in a separate
        // thread."
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    jmxc.close();
                } catch (IOException e) {
                    logger.warn("There was a problem while trying to disconnect the JMX connection to '" + destination
                            + "'.", e);
                }
            }
        }, "MessageClient JMX Close - " + destination).start();
    }

    /**
     * Use a JMX connection to destroy the destination represented by this
     * client on the ActiveMQ message bus. This is needed to help cleanup any
     * ActiveMQ threads that could linger even after the client has been closed.
     * See #2177.
     *
     * @throws DetailedException if there was a problem connecting and removing
     *         the destination on the message bus.
     */
    public void attemptDestroy() throws DetailedException {
        attemptDestroy(getSubjectName(), getDestinationType());
    }

    /**
     * Child classes define this method to finish a connection to a resource.
     *
     * @throws JMSException if there was a problem completing the connection
     */
    protected abstract void completeConnection() throws JMSException;

    /**
     * Getter for the destinationType.
     *
     * @return The value of {@link #destinationType}.
     */
    protected DestinationType getDestinationType() {
        return destinationType;
    }

    /**
     * Initialize the client.
     *
     * @return boolean If the connection was successful.
     * @throws JMSException - thrown if there was a severe error in connecting
     *         the client to the message bus
     */
    final public boolean connect() throws JMSException {

        // Initializes the connection to the message broker which will have to
        // be done for all connections
        initializeConnection();

        // Completes the connection by connecting to the desired resource
        // (topic, queue, etc)
        // Messages can now be sent and received
        completeConnection();

        isOnline = true;

        synchronized (listenRunnable) {

            if (listenThread == null || !listenThread.isAlive()) {

                listenThread = new Thread(listenRunnable, getSubjectName() + "_Message_Listener");

                listenThread.start();

            } else {

                logger.warn("Tried to start the listening thread for " + subjectName + " while it was already running");
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("Connection established to message broker at " + getConnectionUrl() + " for subject "
                    + getSubjectName());
        }

        onConnect();

        return true;
    }

    /**
     * Initialize the client.
     *
     * @param keepTryingOnFail true to keep trying to connect if the connection
     *        fails; false otherwise. This will not prevent the JMSException
     *        from being thrown. Default is false.
     * @return boolean If the connection was successful.
     * @throws JMSException - thrown if there was a severe error in connecting
     *         the client to the message bus
     */
    public final boolean connect(boolean keepTryingOnFail) throws JMSException {
        this.keepTryingOnFail = keepTryingOnFail;
        return connect();
    }

    /**
     * Attempts reconnecting to the message broker until successful or stopped.
     */
    private void reconnect() {

        if (logger.isInfoEnabled()) {
            logger.info("Attempting to reconnect to subject '" + getSubjectName() + "'");
        }

        terminateConnection(false);

        while (!isOnline() && isActive()) {

            synchronized (reconnectThread) {

                try {
                    connect();
                } catch (@SuppressWarnings("unused") JMSException e) {

                    try {

                        // Connect failed, try again in 1 second
                        reconnectThread.wait(1000);

                    } catch (@SuppressWarnings("unused") InterruptedException ex) {
                    }
                }
            }
        }

        if (isOnline() && isActive()) {

            logger.warn("Successfully reconnected to subject '" + getSubjectName() + "'");
        } else {
            logger.error("Failed to reconnect to subject '" + getSubjectName() + "'");
        }
    }

    /**
     * Called when the message client successfully connects/reconnects
     */
    private void onConnect() {

        if (logger.isInfoEnabled()) {
            logger.info("Connect successful");
        }

        synchronized (connectionListeners) {

            for (ProtobufMessageClientConnectionListener<T> listener : connectionListeners) {

                try {
                    listener.connectionOpened(this);
                } catch (Exception e) {
                    logger.error("Caught exception from mis-behaving listener " + listener, e);
                }
            }
        }
    }

    @Override
    public void onMessage(Message msg) {

        if (msg != null && isActive) {
            // only add the message if this client is active

            synchronized (incomingMessageList) {

                incomingMessageList.add(msg);

                incomingMessageList.notifyAll();
            }
        }
    }

    /**
     * Removes all the messages that were received before this method is called
     */
    private void pruneOldMessages() {

        try {

            // give 100 milliseconds to allows ActiveMQ time to get any messages
            // ready
            Message msg = clientMessageConsumer.receive(100);

            long startupTime = System.currentTimeMillis();

            // eat through any messages that are in the queue at startup
            while (msg != null && msg.getJMSTimestamp() < startupTime) {

                msg = clientMessageConsumer.receiveNoWait();
            }

        } catch (JMSException e) {

            logger.error("Caught JMSException while clearing out stale messages", e);
        }
    }

    /**
     * Dispatches messages to be handled
     */
    private void dispatchMessages() {

        Message msg;

        // process all the messages in the queue on this tick
        do {

            synchronized (incomingMessageList) {

                // MH: issue 3318 needed isActive() to be added here because a
                // Gateway topic message was being processed
                // in handleMessage outside of this synchronized block when the
                // incomingMessageList.notifyAll was called
                // in disconnect method.
                if (incomingMessageList.isEmpty() && isActive()) {

                    try {
                        // wait until a message is received or the client is
                        // disconnecting
                        incomingMessageList.wait();

                    } catch (@SuppressWarnings("unused") InterruptedException e) {
                    }
                }

                // pulling all available messages inside a single
                // synchronized block
                // may be more efficient
                msg = incomingMessageList.isEmpty() ? null : incomingMessageList.remove(0);
            }

            if (msg != null) {

                handleMessage(msg);
            }

        } while (msg != null);

        if (logger.isInfoEnabled()) {
            logger.info("Leaving dispatch messages logic (for now) because the collection had no messages for "
                    + getSubjectName() + ".");
        }

    }

    /**
     * Listens and handles messages on the message broker until the client goes
     * offline or is being terminated
     */
    private void listen() {

        if (isOnline && clientMessageConsumer != null && msgHandler != null) {

            // Clears the queue of messages that existed before the start of the
            // message client
            if (pruneOldMessages) {

                pruneOldMessages();
                pruneOldMessages = false;
            }

            try {

                clientMessageConsumer.setMessageListener(this);
                if (logger.isInfoEnabled()) {
                    logger.info("Setting message listener for message consumer of " + getSubjectName());
                }

            } catch (JMSException e) {

                logger.error("Caught JMSException while setting message listener", e);
            }

            while (isActive() && isOnline()) {

                if (logger.isInfoEnabled()) {
                    logger.info(
                            "Starting dispath message logic, i.e. waiting for message to be placed in the queue for "
                                    + getSubjectName() + ".");
                }
                dispatchMessages();
            }

            if (logger.isInfoEnabled()) {
                logger.info("Message client of subject '" + getSubjectName()
                        + "' is no longer listening to the message broker.");
            }
        }
    }

    /**
     * Handles the message by processing the message into a GIFT object and
     * passes it to the property GIFT message handler
     *
     * @param incomingMsg The message to handle
     */
    private void handleMessage(Message incomingMsg) {

        try {
            
            if (incomingMsg instanceof BytesMessage) {
                
                BytesMessage bIncomingMsg = (BytesMessage) incomingMsg;

                /* Extract the reply data from the incoming message */
                final String correlationId = bIncomingMsg.getJMSCorrelationID();
                final Destination replyTo = bIncomingMsg.getJMSReplyTo();

                /* Decode the payload of the message. */
                final T message;
                try {
                    message = decodeMessage(bIncomingMsg);
                } catch (Exception ex) {
                    logger.error("There was a problem decoding the message: " + bIncomingMsg, ex);
                    return;
                }

                /* Determine if a responder should be created. */
                final boolean requiresResponse = correlationId != null && replyTo != null;
                CompletableFuture<T> responder = null;
                
                if(requiresResponse) {
                    
                    //if the message requires a response, send an unprocessed ACK so that the request doesn't time out
                    final BytesMessage ackResponse = brokerSession.createBytesMessage();
                    ackResponse.writeBytes(ackMessageTemplate.toByteArray());
                    ackResponse.setJMSCorrelationID(correlationId);
                    responseMessageProducer.send(replyTo, ackResponse);
                }

                /* Route the message to the handler. */
                try {
                    responder = msgHandler.processMessage(message);

                    final boolean missingRequiredResponse = responder == null && requiresResponse;
                    if (missingRequiredResponse) {
                        logger.error("The message '" + message + "' expected a response but none was generated.");
                    }

                    final boolean unexpectedResponseGenerated = !requiresResponse && responder != null;
                    if (unexpectedResponseGenerated) {
                        logger.error(
                                "A response was generated for the message " + message + " when none was expected.");
                    }
                } catch (Exception ex) {
                    if (responder == null) {
                        logger.error("There was a problem handling the message: " + message, ex);
                    } else {
                        responder.completeExceptionally(ex);
                    }
                }

                /* If a responder was created, handle sending the response when
                 * it arrives. */
                if (responder != null) {
                    responder.thenAccept(response -> {
                        try {
                            final BytesMessage bResponse = brokerSession.createBytesMessage();
                            bResponse.writeBytes(response.toByteArray());
                            bResponse.setJMSCorrelationID(correlationId);
                            responseMessageProducer.send(replyTo, bResponse);
                        } catch (JMSException jmsEx) {
                            logger.error("There was a problem handling the response to the message.", jmsEx);
                        }
                    }).exceptionally(err -> {
                        logger.error("There was a problem handling the message asynchronously.", err);
                        try {
                            final BytesMessage nackResponse = brokerSession.createBytesMessage();
                            nackResponse.writeBytes(errorMessageTemplate.toByteArray());
                            nackResponse.setJMSCorrelationID(correlationId);
                            responseMessageProducer.send(replyTo, nackResponse);
                        } catch (JMSException ex) {
                            logger.error("There was a problem sending an uncaught error response.", ex);
                        }

                        return null;
                    });
                }
            }

        } catch (JMSException e) {

            logger.error("Caught JMSException error", e);
        }
    }

    /**
     * The method that handles {@link Message} responses sent to the
     * {@link #responseDestination}.
     *
     * @param response The {@link Message} that was sent as a response. Can't be
     *        null.
     */
    private void handleResponse(Message response) {

        String correlationId;
        try {
            correlationId = response.getJMSCorrelationID();
        } catch (JMSException e) {
            logger.error("There was a problem getting the correlation id for the message: " + response, e);
            return;
        }

        if (correlationId == null) {
            logger.warn("A message addressed to the response queue did not contain a correlation id.");
            return;
        }

        /* Find the corresponding future. */
        final CompletableFuture<T> future = outstandingFutures.remove(correlationId);
        if (future == null) {
            logger.error("There is no future for the message: " + response);
            return;
        }

        if (response instanceof BytesMessage) {
            final BytesMessage bResponse = (BytesMessage) response;
            T protoResponse;
            try {
                protoResponse = decodeMessage(bResponse);
            } catch (Exception e) {
                logger.error("There was a problem decoding the protobuf response.", e);
                return;
            }

            try {
                future.complete(protoResponse);
            } catch (Throwable t) {
                logger.error("There was a problem handling the message: " + protoResponse, t);
                return;
            }
        } else {
            logger.error("There response that was received for " + correlationId + " was not a bytes messsage.");
        }
    }

    /**
     * Decodes a {@link BytesMessage} as the {@link AbstractMessage} used by
     * this {@link ProtobufMessageClient}.
     *
     * @param message The {@link BytesMessage} to decode. Can't be null.
     * @return The decoded {@link AbstractMessage}. Can't be null.
     * @throws JMSException if there was a problem reading the binary data from
     *         the {@link BytesMessage}.
     * @throws InvalidProtocolBufferException if there was a problem parsing the
     *         binary data as the {@link AbstractMessage}.
     */
    private T decodeMessage(BytesMessage message) throws JMSException, InvalidProtocolBufferException {
        final byte[] buffer;
        try {
            buffer = new byte[(int) message.getBodyLength()];
        } catch (JMSException jmsEx) {
            logger.error("There was a problem getting the length of the response.", jmsEx);
            throw jmsEx;
        }

        try {
            message.readBytes(buffer);
        } catch (JMSException jmsEx) {
            logger.error("There was a problem reading the data from the response.", jmsEx);
            throw jmsEx;
        }

        try {
            @SuppressWarnings("unchecked")
            final T protoResponse = (T) messageTemplate.newBuilderForType().mergeFrom(buffer).build();
            return protoResponse;
        } catch (InvalidProtocolBufferException e) {
            logger.error("There was a problem parsing the data as a protobuf message.", e);
            throw e;
        }
    }

    /**
     * Ends any message listening and disconnects the client from the message
     * broker
     *
     * @param destroyDestination - whether the destination this client is
     *        connected to on the message bus should be removed from the message
     *        bus. Be careful as using this feature could have consequences on
     *        other parts of GIFT that might still need that message bus.
     */
    final public void disconnect(boolean destroyDestination) {

        if (logger.isInfoEnabled()) {
            logger.info("Disconnecting " + this);
        }
        try {

            isActive = false;

            synchronized (incomingMessageList) {
                // since the connection is disconnecting, clear the local
                // message queue and wake the thread up
                incomingMessageList.clear();
                incomingMessageList.notifyAll();
            }

            synchronized (listenRunnable) {

                if (listenThread != null && listenThread.isAlive()) {

                    try {
                        if (logger.isInfoEnabled()) {
                            logger.info("Waiting for the listen thread to end for " + getSubjectName());
                        }
                        listenThread.join();

                        if (logger.isInfoEnabled()) {
                            logger.info("Listen thread ended for " + getSubjectName());
                        }

                    } catch (@SuppressWarnings("unused") InterruptedException e) {
                        // Do nothing
                    }
                }

            }

            synchronized (reconnectRunnable) {

                if (reconnectThread != null && reconnectThread.isAlive()) {

                    try {
                        if (logger.isInfoEnabled()) {
                            logger.info("Waiting for the reconnect thread to end for " + getSubjectName());
                        }
                        reconnectThread.join();

                        if (logger.isInfoEnabled()) {
                            logger.info("Reconnect thread ended for " + getSubjectName());
                        }

                    } catch (@SuppressWarnings("unused") InterruptedException e) {
                        // Do nothing
                    }

                    reconnectThread = null;
                }
            }

            terminateConnection(destroyDestination);
        } catch (Throwable t) {
            logger.error("Failed to disconnect message client for " + getSubjectName(), t);
        }
    }

    /**
     * Completely severs the connection between the message client and message
     * broker
     *
     * @param destroyDestination - whether the destination this client is
     *        connected to on the message bus should be removed from the message
     *        bus. Be careful as using this feature could have consequences on
     *        other parts of GIFT that might still need that message bus.
     */
    private void terminateConnection(boolean destroyDestination) {

        isOnline = false;

        try {

            if (clientMessageProducer != null) {

                logger.info("Closing the client message producer for '" + getSubjectName() + "'.");
                clientMessageProducer.close();
                clientMessageProducer = null;
            }

        } catch (JMSException e) {

            if (!isActive()) {

                logger.error("Caught JMSException while disconnecting the message client message producer", e);

            } else {

                logger.warn(
                        "Caught JMSException for an inactive message client (hence why this is a warning and not an error) while disconnecting the message client message producer");
            }
        }

        try {

            if (clientMessageConsumer != null) {

                if (logger.isInfoEnabled()) {
                    logger.info("Closing the client message consumer for '" + getSubjectName() + "'.");
                }

                try {
                    if (clientMessageConsumer.isDurableSubscriber()) {
                        brokerSession.unsubscribe(clientMessageConsumer.getConsumerId().getConnectionId());
                    }
                } catch (Exception e) {
                    logger.error("Caught exception while unsubscribing the durable subscriber for '" + getSubjectName()
                            + "'.  Continuing to dispose and nullify the consumer and terminate the connection.", e);
                }

                clientMessageConsumer.dispose();
                clientMessageConsumer = null;
                if (logger.isInfoEnabled()) {
                    logger.info("Closed message consumer for '" + getSubjectName() + "'.");
                }
            }

        } catch (JMSException e) {

            if (!isActive()) {

                logger.error("Caught JMSException while disconnecting the message client message consumer", e);

            } else {

                logger.warn(
                        "Caught JMSException for an inactive message client (hence why this is a warning and not an error) while disconnecting the message client message consumer");
            }
        }

        try {

            if (brokerSession != null) {

                if (logger.isInfoEnabled()) {
                    logger.info("Closing the broker session for '" + getSubjectName() + "'.");
                }

                try {
                    brokerSession.close();
                } catch (NullPointerException npe) {
                    // IssueID #2272 - the close method can occasionally cause
                    // an NPE
                    logger.error("Caught exception while trying to close the broker session for '" + getSubjectName()
                            + "'.  This appears to be a bug in the ActiveMQSession class.  Continuing to nullify the session and terminate the connection.",
                            npe);
                }

                brokerSession = null;
            }

        } catch (JMSException e) {

            if (!isActive()) {

                logger.error("Caught JMSException while disconnecting the message client broker session", e);

            } else {

                logger.warn(
                        "Caught JMSException for an inactive message client (hence why this is a warning and not an error) while disconnecting the message client broker session");
            }
        }

        try {

            if (brokerConnection != null) {

                if (logger.isInfoEnabled()) {
                    logger.info("Closing the broker connection for '" + getSubjectName() + "'.");
                }

                brokerConnection.close();
                brokerConnection = null;
            }

        } catch (JMSException e) {

            if (!isActive()) {

                logger.error("Caught JMSException while disconnecting the message client broker connection", e);

            } else {

                logger.warn(
                        "Caught JMSException for an inactive message client (hence why this is a warning and not an error) while disconnecting the message client broker connection");
            }
        }

        try {
            if (destroyDestination) {
                if (logger.isInfoEnabled()) {
                    logger.info("Destroying the destination of '" + getSubjectName() + "'.");
                }
                attemptDestroy();
            }
        } catch (DetailedException e) {
            logger.error("Failed to destroy the destination of '" + getSubjectName() + "'.", e);
        }

        // wake up the message listener thread and let it handle any messages
        // already received from the message bus.
        synchronized (incomingMessageList) {

            while (!incomingMessageList.isEmpty()) {
                incomingMessageList.notifyAll();
            }

            // one more just in case the message list was empty to being with
            // and the thread is in wait
            incomingMessageList.notifyAll();

        }

        // wait for listen thread to finish gracefully after just having allowed
        // it to read all messages in the message list
        // The listen thread needs to be finished before attempting a reconnect
        // which will re-initialize and start the listen thread
        if (listenThread != null && listenThread.isAlive()) {
            try {
                listenThread.join();
            } catch (@SuppressWarnings("unused") InterruptedException e) {
            }
        }

        if (!isActive()) {

            // Notify listeners of disconnect event
            synchronized (connectionListeners) {

                for (ProtobufMessageClientConnectionListener<T> listener : connectionListeners) {

                    try {
                        listener.connectionClosed(this);
                    } catch (Exception e) {
                        logger.error("Caught exception from misbehaving listener " + listener, e);
                    }
                }
            }

        } else {

            // Notify listeners of the connection being lost
            synchronized (connectionListeners) {

                for (ProtobufMessageClientConnectionListener<T> listener : connectionListeners) {

                    try {
                        listener.onConnectionLost(this);
                    } catch (Exception e) {
                        logger.error("Caught exception from misbehaving listener " + listener, e);
                    }
                }
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("Connection disconnected from message broker at " + getConnectionUrl() + " for subject '"
                    + getSubjectName() + "'.");
        }
    }

    /**
     * Add a listener to be notified when there are changes in the client
     * connection
     *
     * @param listener The listener interested in connection change
     *        notifications
     */
    public void addConnectionStatusListener(ProtobufMessageClientConnectionListener<T> listener) {
        synchronized (connectionListeners) {
            connectionListeners.add(listener);
        }
    }

    /**
     * Removes a listener from being notified when there are changes in the
     * client connection
     *
     * @param listener The listener uninterested in connection change
     *        notifications
     */
    public void removeConnectionStatusListener(ProtobufMessageClientConnectionListener<T> listener) {
        synchronized (connectionListeners) {
            connectionListeners.remove(listener);
        }
    }

    /**
     * Sends a message to this client's connected to resource synchronously.
     *
     * @param protoMessage The {@link AbstractMessage} to send. Can't be null.
     * @return True if the message was successfully sent to the message bus,
     *         false otherwise.
     * @throws JMSException if there was a problem while sending the message.
     */
    final public boolean sendMessage(T protoMessage) throws JMSException {
        return doSendMessage(null, protoMessage);
    }

    /**
     * Sends a message to this client's connected to resource and transmits the
     * response via a returned {@link CompletableFuture}.
     *
     * @param protoMessage The {@link AbstractMessage} to send. Can't be null.
     * @return The {@link CompletableFuture} which will resolve to the
     *         {@link AbstractMessage} that was sent in response.
     */
    public CompletableFuture<T> sendMessageAsync(T protoMessage) {
        final String correlationId = Integer.toString(lastCorrelationId.incrementAndGet());
        final CompletableFuture<T> future = new CompletableFuture<>();

        /* Ensure that a destination for responses exists */
        if (listenResponseThread == null) {
            synchronized (listenResponseRunnable) {
                if (listenResponseThread == null) {
                    // listenResponseThread = new Thread(listenResponseRunnable,
                    // getSubjectName() + " Response Listener");
                    // listenResponseThread.start();
                    listenResponseRunnable.run();
                }
            }
        }

        outstandingFutures.put(correlationId, future);
        doSendMessage(correlationId, protoMessage);

        return future;
    }

    /**
     * Constructs a JMS message and sends it through the
     * {@link #clientMessageProducer} synchronously.  Will only send if this
     * interop plugin is online (connected to the message broker) and active (message client not ordered to disconnect).
     *
     * @param correlationId The unique identifier to use for a message which
     *        requires a response. Can be null if the message does not require a
     *        response.
     * @param protoMessage The {@link AbstractMessage} payload to send to the
     *        message bus. Can't be null.
     * @return True if the message was sent successfully, false otherwise.
     */
    private boolean doSendMessage(String correlationId, T protoMessage) {
        if (isOnline() && isActive()) {

            try {

                if (clientMessageProducer != null) {
                    /* Create the message to send */
                    final BytesMessage bytesMessage = brokerSession.createBytesMessage();
                    bytesMessage.writeBytes(protoMessage.toByteArray());
                    if (correlationId != null) {
                        bytesMessage.setJMSReplyTo(responseDestination);
                        bytesMessage.setJMSCorrelationID(correlationId);
                    }

                    /* JMS defines a 10 level priority value with 0 as the
                     * lowest and 9 as the highest. Clients should consider 0-4
                     * as gradients of normal priority and 5-9 as gradients of
                     * expedited priority. Priority is set to 4, by default. */
                    final int MESSAGE_PRIORITY = 4;

                    /* By setting this to 0, this means that messages will never
                     * expire on the broker This is essential because there are
                     * instances where the machine time of the sending client
                     * differs from the machine that the broker is on and the
                     * message may be perceived as already being expired on
                     * receipt. */
                    final int TTL = 0;

                    clientMessageProducer.send(bytesMessage, DeliveryMode.NON_PERSISTENT, MESSAGE_PRIORITY, TTL);
                    return true;
                } else {
                    logger.error("Could not send message, message client does not have a message producer.");
                }

            } catch (JMSException e) {
                logger.error("Caught JMSException while sending message '" + protoMessage.toString() + "'", e);
            }
        } else {
            logger.error("Could not send message, message client is not in a usable state (online = " + isOnline()
                    + ", active = " + isActive() + ") when trying to send message:\n" + protoMessage.toString() + "");
        }

        return false;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[ProtobufMessageClient: ");
        sb.append(" subject = ");
        sb.append(getSubjectName());
        sb.append(", url = ");
        sb.append(connectionUrl);
        sb.append(", id = ");
        sb.append(clientId);
        sb.append("]");

        return sb.toString();
    }
}
