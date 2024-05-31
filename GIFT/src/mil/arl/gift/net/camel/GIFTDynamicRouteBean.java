/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.camel;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.ServiceUnavailableException;

import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.camel.Consume;
import org.apache.camel.Endpoint;
import org.apache.camel.ServiceStatus;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.bean.BeanComponent;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.enums.MessageEncodingTypeEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.module.DomainModuleStatus;
import mil.arl.gift.common.module.GatewayModuleStatus;
import mil.arl.gift.common.module.ModuleStatus;
import mil.arl.gift.common.module.TutorModuleStatus;
import mil.arl.gift.net.api.SubjectUtil;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.MessageUtil;

/**
 * This class listens for module status messages and creates camel routes from each
 * of the modules 'Inbox' named queues to the logger queue, monitor topic and the module's
 * internally read queue.  The logic in this class is executed in the ActiveMQ process.
 *
 * @author mhoffman
 *
 */
public class GIFTDynamicRouteBean extends BeanComponent {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(GIFTDynamicRouteBean.class);

    /** the encoding property name */
    private final static String ENCODING_PROPERTY = "Encoding";

    /**
     * the URL of the message bus JMX, used to remove destinations on the message bus
     * *** Must match the JMX URL in GIFT/config/common.properties and GIFT/external/activemq/bin/win32/wrapper.conf ***
     * Note: ideally the jmx url would be driven by a configuration file (like MessageClient.java)
     * 
     */
    private final static String JMX_URL = "localhost:7020";

    final private Map<String, GIFTRouteBuilder> routeMap = new HashMap<String, GIFTRouteBuilder>();

    final private Set<String> routedTopics = new HashSet<String>();

    /** used to monitor module status and when to cleanup any routes related to timed out modules */
    private ModuleStatusMonitor moduleMonitor = new ModuleStatusMonitor();

    /** The Gateway topic name prefix */
    private static final String GATEWAY_TOPIC = "Gateway_Topic_";
    
    /** The Gateway queue name prefix */
    private static final String GATEWAY_QUEUE = "Gateway_Queue_";

    /** The Tutor topic name prefix */
    private static final String TUTOR_TOPIC = "Tutor_Topic_";
    
    /** The Domain topic name prefix */
    private static final String DOMAIN_TOPIC = "Domain_Topic_";
    
    /** 
     * used to show timestamps in the activemq output on the console (when the console is available) because times are
     * not prefixed in this output logic automatically.
     */
    private static FastDateFormat fastDateFormat = FastDateFormat.getInstance("HH:mm:ss MM/dd/yyyy", null, null);

    /**
     * Object names used to filter threads.
     */
    private static final String CAMEL_OBJECT = "org.apache.camel:";
    private static final String CAMEL_ROUTES_QUERY = CAMEL_OBJECT+ "type=routes,*";
    private static final String ACTIVEMQ_OBJECT = "org.apache.activemq:";
    private static final String ACTIVEMQ_QUEUES_QUERY = ACTIVEMQ_OBJECT + "Type=Queue,*";
    private static final String ACTIVEMQ_TOPICS_QUERY = ACTIVEMQ_OBJECT + "Type=Topic,*";
    private static final String ACTIVEMQ_BROKER_QUERY = ACTIVEMQ_OBJECT + "BrokerName=localhost,Type=Broker";


    //////////////////////////////////////////// using jms Message property /////////////////////////////////////////////////////////////////////////////////

    /*
     * The following four "onXYZ(String msg)" methods
     * get called automatically from within Camel,
     * when a message is received on the uri specified in the @Consume tag.
     */
    @Consume(uri = "activemq:topic:Sensor_Discovery_Topic?mapJmsMessage=false")
    public void onSensorDiscoveryMessage(javax.jms.Message jmsMsg) {
        final Message message = convertMessage(jmsMsg);
        if (message == null || message.getMessageType() != MessageTypeEnum.MODULE_STATUS) {
            System.out.println(fastDateFormat.format(System.currentTimeMillis())
                    + " ERROR: received unhandled message type = " + jmsMsg);
            return;
        }

        handleModuleStatusMessage((ModuleStatus) message.getPayload());
    }

    @Consume(uri = "activemq:topic:Gateway_Discovery_Topic?mapJmsMessage=false")
    public void onGatewayDiscoveryMessage(javax.jms.Message jmsMsg) {
        final Message message = convertMessage(jmsMsg);
        if (message == null || message.getMessageType() != MessageTypeEnum.GATEWAY_MODULE_STATUS) {
            System.out.println(fastDateFormat.format(System.currentTimeMillis())
                    + " ERROR: received unhandled message type = " + jmsMsg);
            return;
        }

        handleModuleStatusMessage((GatewayModuleStatus) message.getPayload());
        handleGatewayModuleStatusMessage((GatewayModuleStatus) message.getPayload());
    }

    @Consume(uri = "activemq:topic:Learner_Discovery_Topic?mapJmsMessage=false")
    public void onLearnerDiscoveryMessage(javax.jms.Message jmsMsg) {
        final Message message = convertMessage(jmsMsg);
        if (message == null || message.getMessageType() != MessageTypeEnum.MODULE_STATUS) {
            System.out.println(fastDateFormat.format(System.currentTimeMillis())
                    + " ERROR: received unhandled message type = " + jmsMsg);
            return;
        }

        handleModuleStatusMessage((ModuleStatus) message.getPayload());
    }

    @Consume(uri = "activemq:topic:Pedagogical_Discovery_Topic?mapJmsMessage=false")
    public void onPedagogicalDiscoveryMessage(javax.jms.Message jmsMsg) {
        final Message message = convertMessage(jmsMsg);
        if (message == null || message.getMessageType() != MessageTypeEnum.MODULE_STATUS) {
            System.out.println(fastDateFormat.format(System.currentTimeMillis())
                    + " ERROR: received unhandled message type = " + jmsMsg);
            return;
        }

        handleModuleStatusMessage((ModuleStatus) message.getPayload());
    }

    @Consume(uri = "activemq:topic:Tutor_Discovery_Topic?mapJmsMessage=false")
    public void onTutorDiscoveryMessage(javax.jms.Message jmsMsg) {
        final Message message = convertMessage(jmsMsg);
        if (message == null) {
            System.out.println(fastDateFormat.format(System.currentTimeMillis())
                    + " ERROR: received unhandled message type = " + jmsMsg);
            return;
        }

        /* If the message arriving is a ModuleStatus, use the same logic used
         * for all module statuses. This case occurs when the singleton
         * TutorModule is issuing its heartbeat. Else if the message is a
         * TutorModuleStatus, create a new routing. This case occurs when a
         * domain session is advertising its Tutor Topic used for sending
         * messages from an embedded application to the domain session */
        if (message.getMessageType() == MessageTypeEnum.MODULE_STATUS) {
            handleModuleStatusMessage((ModuleStatus) message.getPayload());
        } else if (message.getMessageType() == MessageTypeEnum.TUTOR_MODULE_STATUS) {
            handleTutorModuleStatusMessage((TutorModuleStatus) message.getPayload());
        } else {
            System.out.println(fastDateFormat.format(System.currentTimeMillis())
                    + " ERROR: received unhandled message type = " + jmsMsg);
        }
    }

    @Consume(uri = "activemq:topic:LMS_Discovery_Topic?mapJmsMessage=false")
    public void onLMSDiscoveryMessage(javax.jms.Message jmsMsg) {
        final Message message = convertMessage(jmsMsg);
        if (message == null || message.getMessageType() != MessageTypeEnum.MODULE_STATUS) {
            System.out.println(fastDateFormat.format(System.currentTimeMillis())
                    + " ERROR: received unhandled message type = " + jmsMsg);
            return;
        }

        handleModuleStatusMessage((ModuleStatus) message.getPayload());
    }

    @Consume(uri = "activemq:topic:UMS_Discovery_Topic?mapJmsMessage=false")
    public void onUMSDiscoveryMessage(javax.jms.Message jmsMsg) {
        final Message message = convertMessage(jmsMsg);
        if (message == null || message.getMessageType() != MessageTypeEnum.MODULE_STATUS) {
            System.out.println(fastDateFormat.format(System.currentTimeMillis())
                    + " ERROR: received unhandled message type = " + jmsMsg);
            return;
        }

        handleModuleStatusMessage((ModuleStatus) message.getPayload());
    }

    @Consume(uri = "activemq:topic:Domain_Discovery_Topic?mapJmsMessage=false")
    public void onDomainDiscoveryMessage(javax.jms.Message jmsMsg) {
        final Message message = convertMessage(jmsMsg);
        if (message == null || message.getMessageType() != MessageTypeEnum.DOMAIN_MODULE_STATUS) {
            System.out.println(fastDateFormat.format(System.currentTimeMillis())
                    + " ERROR: received unhandled message type = " + jmsMsg);
            return;
        }

        handleModuleStatusMessage((DomainModuleStatus) message.getPayload());
        handleDomainModuleStatusMessage((DomainModuleStatus) message.getPayload());
    }

    @Consume(uri = "activemq:topic:Monitor_Discovery_Topic?mapJmsMessage=false")
    public void onWebMonitorDiscoveryMessage(javax.jms.Message jmsMsg) {
        final Message message = convertMessage(jmsMsg);
        if (message == null || message.getMessageType() != MessageTypeEnum.WEB_MONITOR_MODULE_STATUS) {
            System.out.println(fastDateFormat.format(System.currentTimeMillis())
                    + " ERROR: received unhandled message type = " + jmsMsg);
            return;
        }

        handleModuleStatusMessage((ModuleStatus) message.getPayload());
    }

    /**
     * Converts the jms Message to the net api Message.
     * 
     * @param jmsMsg the jms message to convert. If null, the return will be
     *        null.
     * @return The converted net api Message. Can be null if the jms message is
     *         null or is an unknown type.
     */
    private Message convertMessage(javax.jms.Message jmsMsg) {
        Message message = null;
        try {
            if (jmsMsg instanceof TextMessage) {
                TextMessage tMsg = (TextMessage) jmsMsg;
                message = MessageUtil.getMessageFromString(tMsg.getText(),
                        MessageEncodingTypeEnum.valueOf(tMsg.getByteProperty(ENCODING_PROPERTY)));
            } else if (jmsMsg instanceof BytesMessage) {
                BytesMessage bMsg = (BytesMessage) jmsMsg;

                final byte[] buffer;
                try {
                    buffer = new byte[(int) bMsg.getBodyLength()];
                } catch (JMSException jmsEx) {
                    logger.error("There was a problem getting the length of the response.", jmsEx);
                    throw jmsEx;
                }

                try {
                    bMsg.readBytes(buffer);
                } catch (JMSException jmsEx) {
                    logger.error("There was a problem reading the data from the response.", jmsEx);
                    throw jmsEx;
                }

                /* Base64 encoding schemes are commonly used when there is a
                 * need to encode binary data that needs be stored and
                 * transferred over media that are designed to deal with textual
                 * data. This is to ensure that the data remains intact without
                 * modification during transport. */
                message = MessageUtil.getMessageFromString(Base64.getEncoder().encodeToString(buffer),
                        MessageEncodingTypeEnum.valueOf(bMsg.getByteProperty(ENCODING_PROPERTY)));
            }
        } catch (EnumerationNotFoundException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }

        return message;
    }

    ///////////////////////////////////////////////camel converting message payload to string ///////////////////////////////////////////////////////////////////////////

//    @Consume(uri = "activemq:topic:Sensor_Discovery_Topic")
//    public void onSensorDiscoveryMessage(String msg) {
//        Message message = (Message)MessageUtil.getMessageFromString2(msg, MessageEncodingTypeEnum.JSON);
//        if (message.getMessageType() == MessageTypeEnum.MODULE_STATUS) {
//            handleModuleStatusMessage((ModuleStatus) message.getPayload());
//        }
//    }
//
//    @Consume(uri = "activemq:topic:Gateway_Discovery_Topic")
//    public void onGatewayDiscoveryMessage(String msg) {
//        Message message = (Message)MessageUtil.getMessageFromString2(msg, MessageEncodingTypeEnum.JSON);
//        if (message.getMessageType() == MessageTypeEnum.GATEWAY_MODULE_STATUS) {
//            handleModuleStatusMessage((GatewayModuleStatus) message.getPayload());
//            handleGatewayModuleStatusMessage((GatewayModuleStatus) message.getPayload());
//        }
//    }
//
//    @Consume(uri = "activemq:topic:Domain_Discovery_Topic")
//    public void onDomainDiscoveryMessage(String msg) {
//        Message message = (Message)MessageUtil.getMessageFromString2(msg, MessageEncodingTypeEnum.JSON);
//        if (message.getMessageType() == MessageTypeEnum.MODULE_STATUS) {
//            handleModuleStatusMessage((ModuleStatus) message.getPayload());
//        }
//    }
//
//    @Consume(uri = "activemq:topic:UMS_Status_Topic")
//    public void onStatusMessage(String msg) {
//        Message message = (Message)MessageUtil.getMessageFromString2(msg, MessageEncodingTypeEnum.JSON);
//        if (message.getMessageType() == MessageTypeEnum.AGGREGATE_MODULE_STATUS) {
//            handleAggregateModuleStatusMessage((AggregateModuleStatus) message.getPayload());
//        }
//    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Make sure a route exists for this module.
     *
     * @param moduleStatus
     */
    private synchronized void handleModuleStatusMessage(ModuleStatus moduleStatus) {

    	if( !routeMap.containsKey(moduleStatus.getQueueName()) ) {

    		setupRoute(moduleStatus);
    	}

    }

    /**
     * Make sure a route exists for the gateway module topic.
     *
     * @param gatewayModuleStatus
     */
    private synchronized void handleGatewayModuleStatusMessage(GatewayModuleStatus gatewayModuleStatus) {

        final String topicName = gatewayModuleStatus.getTopicName();

        if( !routedTopics.contains(topicName) ) {

            RouteBuilder route = new RouteBuilder() {

                @Override
                public void configure() throws Exception {

                    this.from("activemq:topic:"+topicName).routeId(topicName)
                        .to("activemq:topic:"+SubjectUtil.MONITOR_TOPIC)
                        .to("activemq:queue:Logger_Queue");

                }
            };

            try {

                this.getCamelContext().addRoutes(route);
                
                System.out.println(fastDateFormat.format(System.currentTimeMillis()) + " CREATED Gateway Training App State topic route: "+topicName);

            } catch (Exception e) {

                e.printStackTrace();
            }

            this.routedTopics.add(topicName);
        }

        //update the module status timer logic
        moduleMonitor.receivedModuleStatus(System.currentTimeMillis(), gatewayModuleStatus);
    }

    /**
     * Make sure a route exists for the tutor module topic.
     *
     * @param tutorModuleStatus
     */
    private synchronized void handleTutorModuleStatusMessage(TutorModuleStatus tutorModuleStatus) {
    	final String topicName = tutorModuleStatus.getTopicName();

    	if( !routedTopics.contains(topicName) ) {

            RouteBuilder route = new RouteBuilder() {

                @Override
                public void configure() throws Exception {

                    this.from("activemq:topic:"+topicName).routeId(topicName)
                        .to("activemq:topic:"+SubjectUtil.MONITOR_TOPIC)
                        .to("activemq:queue:Logger_Queue");

                }
            };

            try {

                this.getCamelContext().addRoutes(route);
                
                System.out.println(fastDateFormat.format(System.currentTimeMillis()) + " CREATED Tutor Embedded Training App State topic route: "+topicName);

            } catch (Exception e) {

                e.printStackTrace();
            }

            this.routedTopics.add(topicName);
        }

        //update the module status timer logic
        moduleMonitor.receivedModuleStatus(System.currentTimeMillis(), tutorModuleStatus);
    }
    
    /**
     * Make sure a route exists for the domain module topic.
     *
     * @param gatewayModuleStatus
     */
    private synchronized void handleDomainModuleStatusMessage(DomainModuleStatus domainModuleStatus) {

        final Set<String> topicNames = domainModuleStatus.getLogPlaybackTopics();

        for(String topicName : topicNames) {
            if(!routedTopics.contains(topicName) ) {
    
                RouteBuilder route = new RouteBuilder() {
    
                    @Override
                    public void configure() throws Exception {
    
                        this.from("activemq:topic:"+topicName).routeId(topicName)
                            .to("activemq:topic:"+SubjectUtil.MONITOR_TOPIC)
                            .to("activemq:queue:Logger_Queue");
    
                    }
                };
    
                try {
    
                    this.getCamelContext().addRoutes(route);
                    
                    System.out.println(fastDateFormat.format(System.currentTimeMillis()) + " CREATED Domain Log Service topic route: "+topicName);
    
                } catch (Exception e) {
    
                    e.printStackTrace();
                }
    
                this.routedTopics.add(topicName);
            }
        }

        //update the module status timer logic
        moduleMonitor.receivedModuleStatus(System.currentTimeMillis(), domainModuleStatus);
    }

    /**
     * Builds a new route based on the module status provided.
     *
     * @param moduleStatus
     */
    private void setupRoute(ModuleStatus moduleStatus) {

    	String queueName = moduleStatus.getQueueName();

    	GIFTRouteBuilder routeBuilder = new GIFTRouteBuilder(queueName);

    	System.out.println(fastDateFormat.format(System.currentTimeMillis()) + " Created "+routeBuilder);

    	routeMap.put(queueName, routeBuilder);

    	try {

    		this.getCamelContext().addRoutes(routeBuilder);

    		// the 'startRoute' logic was added for the logic of reconnecting GIFT modules
    		// after ActiveMQ had gone down and come back online.  However this code is not needed
    		// when first starting ActiveMQ (e.g. tested through Single process launcher) because
    		// it will create 2 of every route that comes through here, one in the above 'addRoutes'
    		// and the second in the 'startRoute'.  Starting two routes could be seen in the activemq.log
    		// log file as well as when the route was being cleaned up as there would be lingering threads
    		// when viewed in jconsole.  Those threads would only terminate when ActiveMQ was closed.
    		// Therefore the check for the camel context status was added to not start the routes if the context is already started.
    		// #2177
    		if(this.getCamelContext().getStatus() != ServiceStatus.Started){
                RoutesDefinition routes = routeBuilder.getRouteCollection();
                for(RouteDefinition route : routes.getRoutes()) {
                    logger.error(fastDateFormat.format(System.currentTimeMillis()) + " STARTING ROUTE = "+route);
                    this.getCamelContext().startRoute(route);
                }
    		}

    		System.out.println(fastDateFormat.format(System.currentTimeMillis()) + " ROUTE CREATED: "+moduleStatus.getModuleName() +
 		           " of module type " + moduleStatus.getModuleType() +
 		           " to be sent to " + queueName+", ROUTE = "+routeBuilder);

    	}catch(Exception ex) {
    	    logger.error(fastDateFormat.format(System.currentTimeMillis()) + " Caught exception while trying to setup a route for "+moduleStatus, ex);
    		System.out.println(ex);
    		routeMap.remove(queueName);
    	}
    }

    @Override
    protected Endpoint createEndpoint(String string, String string1, Map<String, Object> map) throws Exception {
        return null;
    }

    /**
     * Remove the routes associated with the given module status from ActiveMQ, as well as 
     * any temporary topics or queues that may be specific to said status. This uses JMX 
     * to stop and remove any routes created for the module represented by this module status.
     *
     * @param moduleStatus contains information about a module that will be used to remove the routes
     * created by this class.
     * @throws Exception if there was a problem connecting to JMX or executing the logic to remove the
     * route.
     */
    public void removeRoute(ModuleStatus moduleStatus) throws Exception{
        //If the module in question is a gateway, remove both the queue route and the topic
        //route since the module has been destroyed. Otherwise if the module is a tutor only
        //remove the topic route only since the module needs to persist
        if(moduleStatus instanceof TutorModuleStatus) {
            removeTopicRoutes(moduleStatus);
            
        } else if(moduleStatus instanceof GatewayModuleStatus) {
            removeQueueRoutes(moduleStatus);
            removeTopicRoutes(moduleStatus);
            
        } else if(moduleStatus instanceof DomainModuleStatus) {
            removeTopicRoutes(moduleStatus);
        }
    }

    /**
     * Close the JMX thread used by GIFT to destroy topics/queues.
     *
     * @param destination the name of the destination being destroyed.
     * @param jmxc the connection to close.
     */
    private static void closeJMXConnection(final String destination, final JMXConnector jmxc){

        if(jmxc == null){
            return;
        }

        //closing in a thread because the javadoc for jmxc.close() states:
        // "Closing a connection is a potentially slow operation. For example, if the server has crashed,
        //  the close operation might have to wait for a network protocol timeout. Callers that do not
        //  want to block in a close operation should do it in a separate thread."
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    jmxc.close();
                } catch (IOException e) {
                    logger.warn("There was a problem while trying to disconnect the JMX connection to '"+destination+"'.", e);
                }
            }
        }, "GIFTDynamicRouteBean JMX Close - "+destination).start();
    }

    /**
     * Removes all ActiveMQ queues and Camel routes that contain the given queue name. This can be used
     * to remove multiple queues and routes with a similar naming scheme, such as temporary queues
     * and routes associated with a module for a single domain session.
     * 
     * @param topicName the text of the name to search for. Any queues or routes with names that contain
     * this text will be removed. Can be null, in which case, no queues or routes will be removed.
     * @param jmxc a JMX connector to use to get the server connection for the message bus. Cannot be null.
     * @throws Exception if a problem occurs while removing the routes and queues
     */
    private static void removeQueueRoutes(ModuleStatus moduleStatus) throws Exception {
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"+JMX_URL+"/jmxrmi");
        JMXConnector jmxc = null;

        try {
            jmxc = JMXConnectorFactory.connect(url);
        } catch (IOException e) {
            if(e.getCause() instanceof ServiceUnavailableException) {
                throw(new Exception("There was a problem connecting to the JMX at " + url
                        +"\nYou may need to change the JMX port number at the following locations:\n"
                        + "1) 'JmxUrl' property in GIFT/config/common.properties\n"
                        + "2) 'jmxremote.port' property in GIFT/external/activemq/bin/win32/wrapper.conf\n"
                        + "3) 'JMX_URL' String in GIFTDynamicRouteBean.java", e));
            } else {
                throw(e);
            }
        }

        try{
            MBeanServerConnection server = jmxc.getMBeanServerConnection();

            // Remove all of the routes associated with the queue name being removed
            removeRoutes(moduleStatus.getQueueName(), server);
            
            // Remove all of the queues associated with the queue name being removed.
            ObjectName objName = new ObjectName(ACTIVEMQ_QUEUES_QUERY);
            List<ObjectName> cacheList = new LinkedList<ObjectName>(server.queryNames(objName, null));
            for (Iterator<ObjectName> iter = cacheList.iterator(); iter.hasNext();){

                objName = iter.next();
                String keyProps = objName.getCanonicalKeyPropertyListString();

                if(keyProps.contains(moduleStatus.getQueueName())) {

                    // The ActiveMQ broker is needed to remove queues
                    ObjectName objectRouteName = new ObjectName(ACTIVEMQ_BROKER_QUERY);
                    BrokerViewMBean amqBroker = MBeanServerInvocationHandler.newProxyInstance(
                            server, objectRouteName, BrokerViewMBean.class, true);

                    // Get the topic name from the Destination property to remove the queue
                    amqBroker.removeQueue(objName.getKeyProperty("Destination"));

                }
            }
        }finally{

            //close the thread
            if(jmxc != null){
                closeJMXConnection(moduleStatus.getQueueName(), jmxc);
            }

        }
    }

    /**
     * Removes all ActiveMQ topics and Camel routes that match the given module status. This is
     * useful when cleaning up the message bus for modules like the Domain, Tutor, and Gateway 
     * that may launch additional temporary topics and routes during a single domain session.
     * 
     * @param moduleStatus the module status used to determine which topics and routes to remove.
     * Can be null, in which case, no topics or routes will be removed.
     * @throws Exception if a problem occurs while removing the routes and topics
     */
    private void removeTopicRoutes(ModuleStatus moduleStatus) throws Exception {
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"+JMX_URL+"/jmxrmi");
        JMXConnector jmxc = null;

        try {
            jmxc = JMXConnectorFactory.connect(url);
        } catch (IOException e) {
            if(e.getCause() instanceof ServiceUnavailableException) {
                throw(new Exception("There was a problem connecting to the JMX at " + url
                        +"\nYou may need to change the JMX port number at the following locations:\n"
                        + "1) 'JmxUrl' property in GIFT/config/common.properties\n"
                        + "2) 'jmxremote.port' property in GIFT/external/activemq/bin/win32/wrapper.conf\n"
                        + "3) 'JMX_URL' String in GIFTDynamicRouteBean.java", e));
            } else {
                throw(e);
            }
        }

        try{
            String topicName = null;
            if(moduleStatus instanceof GatewayModuleStatus) {
                
                /* Get the name of the topic associated with this Gateway module status. If the topic
                 * is associated with a JWS Gateway instance, then this name will contain the UUID that
                 * uniquely identifies all the routes and topics associated with that instance that
                 * need to be cleaned up*/
                topicName = ((GatewayModuleStatus)moduleStatus).getTopicName();
                
                /* Most of the topics associated with the JWS Gateway app will be cleaned up by using
                 * the topic name above, but there are two topics that break the normal naming scheme
                 * for topics associated with the JWS Gateway app:
                 * - ActiveMQ.Advisory.Consumer.Queue.Gateway_Queue_a0d4c6de-f0e4-4539-9714-2e9555011af7_Inbox
                 * - ActiveMQ.Advisory.Producer.Queue.Gateway_Queue_a0d4c6de-f0e4-4539-9714-2e9555011af7_Inbox 
                 * 
                 * Despite having Gateway_Queue in the name rather than Gateway_Topic, these are temporary topics
                 * associated with the JWS Gateway app that need to be removed, otherwise they will accumulate each
                 * time the JWS Gateway app is run.
                 * 
                 * Unfortunately, since they do not match the normal naming scheme, removeTopicRoutes does not
                 * remove them because they do not contain the topic name above (which, for the example given,
                 * would be Gateway_Topic_a0d4c6de-f0e4-4539-9714-2e9555011af7).
                 * 
                 * In order to properly remove these topics, we need to call removeTopicRoues a second time, replacing
                 * Gateway_Topic in the above topic name with Gateway_Queue. This makes removeTopicRoutes look for 
                 * Gateway_Queue_a0d4c6de-f0e4-4539-9714-2e9555011af7, which DOES match the last two topics we're
                 * trying to remove.
                 * 
                 * Note: We cannot simply remove Gateway_Topic_ from the above topic name because that causes 
                 * closing the Gateway module from the monitor to remove ALL routes and topics with the same IP 
                 * address, even  for other modules (i.e. Gateway_Topic_10.3.82.6 just becomes 10.3.82.6).
                 * 
                 * For more information on this, refer to ticket #4756 (https://gifttutoring.org/issues/4756)
                 */
                removeTopicRoutes(topicName.replace(GATEWAY_TOPIC, GATEWAY_QUEUE), jmxc);
                
                System.out.println(fastDateFormat.format(System.currentTimeMillis()) + " Attempting to REMOVE Gateway Training App State Topic route (status has timed out): "+((GatewayModuleStatus)moduleStatus).getTopicName());
                
            } else if(moduleStatus instanceof TutorModuleStatus) {
                topicName = ((TutorModuleStatus)moduleStatus).getTopicName();
                topicName = topicName.replace(TUTOR_TOPIC, "");
                
                System.out.println(fastDateFormat.format(System.currentTimeMillis()) + " Attempting to REMOVE Tutor Embedded Training App State Topic route (status has timed out): "+((TutorModuleStatus)moduleStatus).getTopicName());
            
            } else if(moduleStatus instanceof DomainModuleStatus) {
                
                Set<String> currentDomainTopics = ((DomainModuleStatus) moduleStatus).getLogPlaybackTopics();
                Iterator<String> topicItr = routedTopics.iterator();
                
                /* 
                 * Iterate through all the topics that have routes and remove the routes for any domain 
                 * topics that have been removed from the Domain module status
                 */
                while(topicItr.hasNext()) {
                    
                    topicName = topicItr.next();
                    if(topicName.startsWith(DOMAIN_TOPIC) && !currentDomainTopics.contains(topicName)) {
                        
                        topicName = topicName.replace(TUTOR_TOPIC, "");
                        System.out.println(fastDateFormat.format(System.currentTimeMillis()) + " Attempting to REMOVE Domain Log Service Topic route (status has timed out): "+DOMAIN_TOPIC+topicName);
                    
                        removeTopicRoutes(topicName, jmxc);
                    }
                }
                
                return;
            }

            removeTopicRoutes(topicName, jmxc);

        }finally{

            //close the thread
            if(jmxc != null){
                closeJMXConnection(moduleStatus.getQueueName(), jmxc);
            }

        }
    }
    
    /**
     * Removes all ActiveMQ topics and Camel routes that contain the given topic name. This can be used
     * to remove multiple topics and routes with a similar naming scheme, such as temporary topics
     * and routes associated with a module for a single domain session.
     * 
     * @param topicName the text of the name to search for. Any topics or routes with names that contain
     * this text will be removed. Can be null, in which case, no topics or routes will be removed.
     * @param jmxc a JMX connector to use to get the server connection for the message bus. Cannot be null.
     * @throws Exception if a problem occurs while removing the routes and topics
     */
    private static void removeTopicRoutes(String topicName, JMXConnector jmxc) throws Exception{
        
       MBeanServerConnection server = jmxc.getMBeanServerConnection();
       
       // Remove all of the routes associated with the topic name being removed
       removeRoutes(topicName, server);

       /* Remove all of the topics associated with the topic name being removed.
        * 
        * Note that there may be more than one such topic. A good example of this are the 
        * topics that are used by the JWS Gateway app during a domain session in Server mode.
        * For these, we need to remove multiple topics whose names contain either 
        * Gateway_Topic_<UUID> or Gateway_Queue_<UUID>, such as  
        * ActiveMQ.Advisory.Producer.Topic.Gateway_Topic_f0e4-4539-9714-2e9555011af7 */
       ObjectName objName = new ObjectName(ACTIVEMQ_TOPICS_QUERY);
       List<ObjectName> cacheList = new LinkedList<ObjectName>(server.queryNames(objName, null));
       for (Iterator<ObjectName> iter = cacheList.iterator(); iter.hasNext();){

           objName = iter.next();
           String keyProps = objName.getCanonicalKeyPropertyListString();

           if(topicName != null && keyProps.contains(topicName)) {

               // The ActiveMQ broker is needed to remove topics
               ObjectName objectRouteName = new ObjectName(ACTIVEMQ_BROKER_QUERY);
               BrokerViewMBean amqBroker = MBeanServerInvocationHandler.newProxyInstance(
                       server, objectRouteName, BrokerViewMBean.class, true);

               // Get the topic name from the Destination property to remove the topic
               amqBroker.removeTopic(objName.getKeyProperty("Destination"));

               System.out.println(fastDateFormat.format(System.currentTimeMillis()) + " REMOVING Topic: "+topicName);
           }
       }
    }

    /**
     * Removes all of the Camel routes with names containing the given text. A queue or topic name
     * can be passed into this method to remove routes associated with that queue or topic.
     * 
     * @param name the text of the name to search for (i.e. a queue or topic name). Any routes with 
     * names that contain this text will be removed. If null, no routes will be removed.
     * @param server the server connection for the message bus. Cannot be null.
     * @throws Exception if a problem occurs while removing the routes
     */
    private static void removeRoutes(String name, MBeanServerConnection server) throws Exception {
        
        /* Remove routes by querying the server for all camel ObjectNames with type=route
        *
        * Note: ObjectNames can be determined using the MBeans tab of the JConsole.
        * The ObjectName also contains properties that can be used as filters.
        */
       ObjectName objName = new ObjectName(CAMEL_ROUTES_QUERY);
       List<ObjectName> cacheList = new LinkedList<ObjectName>(server.queryNames(objName, null));
       for (Iterator<ObjectName> iter = cacheList.iterator(); iter.hasNext();){

           objName = iter.next();
           String keyProps = objName.getCanonicalKeyPropertyListString();

           if(keyProps.contains(name)) {
               //need to remove both a module's Inbox route as well as any gateway module topic route that was created.

               //stop and remove the route for proper cleanup
               ObjectName objectRouteName = new ObjectName(CAMEL_OBJECT + keyProps);
               Object[] params = {};
               String[] sig = {};

               /* Note: Operation names can be found in the MBeans tab of the JConsole
                * by expanding the MBean tree and clicking 'Operations'. */
               server.invoke(objectRouteName, "stop", params, sig);
               server.invoke(objectRouteName, "remove", params, sig);
               
               System.out.println(fastDateFormat.format(System.currentTimeMillis()) + " REMOVING camel route for: "+keyProps);
           }
       }
    }

    /**
     * Used for testing the remove route logic.
     *
     * @param args not used
     * @throws Exception if there was a problem removing a route
     */
    public static void main(String[] args) throws Exception{

        ModuleStatus mStatus = new ModuleStatus(GATEWAY_TOPIC, GATEWAY_TOPIC, ModuleTypeEnum.GATEWAY_MODULE);
        GIFTDynamicRouteBean testInstance = new GIFTDynamicRouteBean();
        testInstance.removeRoute(mStatus);
    }

    /**
     * This inner class is used to monitor module status updates and remove
     * any routes that were created by this class for modules that have timed out.
     *
     * @author mhoffman
     *
     */
    private class ModuleStatusMonitor{

        /**
         * maximum time between module status message before it is consider timed-out
         * Note: ideally this would be driven by a properties file.
         */
        private static final long LAST_STATUS_TIMEOUT = 60000;

        /**
         * Contains information on the last time a module was heard from
         * The reason this is not a multi-key map is because there are two use cases:
         *  1. get information for all modules of a given module type
         *  2. update the module information for a single module of a given type
         */
        private Map<ModuleTypeEnum, Map<String, StatusReceivedInfo>> moduleStatusInfo = new HashMap<ModuleTypeEnum, Map<String, StatusReceivedInfo>>();

        /** this is the thread that checks for timed-out message clients */
        private Timer timer;

        /** time between checks */
        private static final int DELAY_MS = 5000;

        /**
         * Class constructor
         */
        private ModuleStatusMonitor(){
            timer = new Timer("RouteBean - ModuleStatusMonitorTimer");
        }

        /**
         * Create and initialize a new instance of the subclass StatusReceivedInfo for the module
         * status information being provided.
         *
         * @param time the time at which the status was received
         * @param status the module status information to create timer logic for
         * @return StatusReceivedInfo - new instance
         */
        private StatusReceivedInfo createInfo(long time, ModuleStatus status){

            StatusReceivedInfo info = new StatusReceivedInfo(time, status);
            timer.schedule(new StatusTimerTask(info), DELAY_MS, DELAY_MS);
            return info;
        }

        /**
         * Notify the monitor that a module was heard from
         *
         * @param time - the time at which the module status was created
         * @param status - the module status being received for a module
         */
        public void receivedModuleStatus(long time, ModuleStatus status){

            if(status == null){
                return;
            }

            boolean updateOperation = false;
            Map<String, StatusReceivedInfo> statusMap = moduleStatusInfo.get(status.getModuleType());
            if(statusMap == null){
                statusMap = new ConcurrentHashMap<String, StatusReceivedInfo>();
                moduleStatusInfo.put(status.getModuleType(), statusMap);  //(ADD OPERATION)
            }

            StatusReceivedInfo info = null;
            String statusKey =getStatusKeyFromStatus(status);            
            synchronized(statusMap){

                info = statusMap.get(statusKey);
                if(info != null){
                    updateOperation = true;
                }else{
                    info = createInfo(time, status);
                    statusMap.put(statusKey, info);
                    if(logger.isInfoEnabled()){
                        logger.info(fastDateFormat.format(System.currentTimeMillis()) + " Discovered module from status: "+status);
                    }
                }

            }//release statusMap to perform listener notification method calls


            if(updateOperation){
                info.update(time, status);
            }
        }

        /**
         * The module has become stale (i.e. timed-out).  Remove any associated routes
         * from ActiveMQ.
         *
         * @param info
         */
        private void removeModule(StatusReceivedInfo info){

            synchronized(moduleStatusInfo){
                
                Map<String, StatusReceivedInfo> statusMap = moduleStatusInfo.get(info.getModuleStatus().getModuleType());
                if(statusMap != null){
                    statusMap.remove(info.getStatusKey());
                }

                try{
                    removeRoute(info.getModuleStatus());
                }catch(Exception e){
                    logger.error("Caught exception while trying to remove route for "+info, e);
                }
            }
        }        
        
        /**
         * Return the status key from the module status provided.
         * 
         * @param status the module status to determine the appropriate status key for.  Can't be null.
         * @return the status key used to uniquely identify this status among other module status messages. 
         */
        public String getStatusKeyFromStatus(ModuleStatus status){
            
            String key;
            if(status instanceof TutorModuleStatus){
                //use the tutor embedded training app topic name which is unique to this status and not
                //the tutor module queue name which is unique to the tutor module and not a course instance.
                key = ((TutorModuleStatus)status).getTopicName();
                
            }else{
                key = status.getQueueName();
            }
            
            return key;
        }

        /**
         * This class contains a time stamp associated with a module status and is used
         * to determine when the last time the module was heard from.
         *
         * @author mhoffman
         *
         */
        public class StatusReceivedInfo{

            /** last time a module status message was received for a module (milliseconds) */
            private long lastRcvdTime;

            /** the last status for the module associated with this received information */
            private ModuleStatus status;

            /** the time at which the status was created/sent */
            private long sentTime;

            /**
             * the amount of time in milliseconds since the last received timestamp that caused a module timeout
             * because the value was higher than the timeout threshold
             */
            private long timeoutValue = 0L;

            /**
             * whether the monitoring for the module represented by this class was cancelled
             */
            private boolean cancelledMonitoring = false;
            
            /**
             * the key used to identify this status among other module status messages
             */
            private String statusKey;

            /**
             * Class constructor
             *
             * @param sentTime - the time at which the status was created/sent
             * @param status the last status for the module associated with this received information
             */
            public StatusReceivedInfo(long sentTime, ModuleStatus status){
                this.status = status;
                this.sentTime = sentTime;

                // Default the value with the current time.
                this.lastRcvdTime = System.currentTimeMillis();
                
                statusKey = getStatusKeyFromStatus(status);
            }
            
            /**
             * Return the key used to identify this status among other module status messages.
             * 
             * @return won't be null or empty.
             */
            public String getStatusKey(){
                return statusKey;
            }

            /**
             * Another message was received from this client, update the last time received information
             *
             * @param sentTime - the time at which the status was created/sent
             * @param status the last status for the module associated with this received information
             */
             void update(long sentTime, ModuleStatus status){
                this.status = status;
                this.sentTime = sentTime;
                lastRcvdTime = System.currentTimeMillis();
            }

            /**
             * Return the last time a module status message was received for the client.
             *
             * @return long
             */
            public long getTimeReceived(){
                return lastRcvdTime;
            }

            /**
             * Return the time at which the status was created/sent
             *
             * @return long
             */
            public long getSentTime(){
                return sentTime;
            }

            /**
             * Return the last module status associated with this received information
             *
             * @return ModuleStatus
             */
            public ModuleStatus getModuleStatus(){
                return status;
            }

            void setTimeoutValue(long value){
                this.timeoutValue = value;
            }

            /**
             * Return the amount of time in milliseconds since the last received timestamp that caused a module timeout
             * because the value was higher than the timeout threshold.
             *
             * @return can be zero if not set because there wasn't a timeout
             */
            public long getTimeoutValue() {
                return timeoutValue;
            }

            public boolean shouldCancelledMonitoring() {
                return cancelledMonitoring;
            }

            @Override
            public String toString(){

                StringBuffer sb = new StringBuffer();
                sb.append("[ReceivedInfo:");
                sb.append(" time = ").append(getTimeReceived());
                sb.append(", sentTime = ").append(getSentTime());
                sb.append(", timeout = ").append(getTimeoutValue());
                sb.append(", statusKey = ").append(getStatusKey());
                sb.append(", cancelMonitoring = ").append(shouldCancelledMonitoring());
                sb.append(", status = ").append(getModuleStatus());
                sb.append("]");
                return sb.toString();
            }
        }

        /**
         * This class contains the timer task associated with a single module and it's status.
         * It will check for a timed-out module by examining the timestamp associated with the last received status.
         *
         * @author mhoffman
         *
         */
        protected class StatusTimerTask extends TimerTask{

            /** container for the last module status received */
            private StatusReceivedInfo info;

            /** used to hold the "now" time */
            private long now;

            /**
             * Class constructor
             *
             * @param info container for the last module status received
             */
            public StatusTimerTask(StatusReceivedInfo info){
                this.info = info;
            }

            @Override
            public void run() {

                now = System.currentTimeMillis();

                //check if module has timed-out
                if((now - LAST_STATUS_TIMEOUT) > info.getTimeReceived()){

                    info.setTimeoutValue((now - info.getTimeReceived()));
                    if(!info.shouldCancelledMonitoring()){
                        if(logger.isInfoEnabled()){
                            logger.info(fastDateFormat.format(System.currentTimeMillis()) + " Module has timed out having not heard from it in "+(info.getTimeoutValue()/1000.0)+" seconds - "+info);
                        }
                    }else{
                        if(logger.isInfoEnabled()){
                            logger.info(fastDateFormat.format(System.currentTimeMillis()) + " Module status monitoring timer task is gracefully ending for "+info+".");
                        }
                    }

                    removeModule(info);

                    cancel();
                    
                    System.out.println(fastDateFormat.format(System.currentTimeMillis()) + " Status timer task ended for: "+info.getStatusKey());
                }
            }
        }
    }

}
