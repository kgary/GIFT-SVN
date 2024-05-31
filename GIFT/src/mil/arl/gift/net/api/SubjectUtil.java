/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api;

/**
 * This class contains the names of various topics and queues.
 * A topic is used to broadcast information to more than one consumer/recipient,
 * while a queue is used for point-to-point communication where a producer
 * sends a message to the consumers queue - i.e. a module sends a message
 * to another module's queue.
 *
 * @author mhoffman
 *
 */
public class SubjectUtil {

    /**
     * TOPICS
     * Note: these topic names are also in external\activemq\conf\camel.xml
     */

    public static final String SENSOR_DISCOVERY_TOPIC = "Sensor_Discovery_Topic";

    public static final String DOMAIN_DISCOVERY_TOPIC = "Domain_Discovery_Topic";

    public static final String GATEWAY_DISCOVERY_TOPIC = "Gateway_Discovery_Topic";

    /** the topic used to send out simulation messages as part of interaction with a training application (e.g. VBS), 
     * the Domain module usually pulls from this */
    public static final String GATEWAY_TOPIC_PREFIX    = "Gateway_Topic";

    public static final String LEARNER_DISCOVERY_TOPIC  = "Learner_Discovery_Topic";

    public static final String PED_DISCOVERY_TOPIC  = "Pedagogical_Discovery_Topic";

    public static final String LMS_DISCOVERY_TOPIC  = "LMS_Discovery_Topic";

    public static final String UMS_DISCOVERY_TOPIC  = "UMS_Discovery_Topic";

    public static final String TUTOR_DISCOVERY_TOPIC  = "Tutor_Discovery_Topic";

    public static final String MONITOR_DISCOVERY_TOPIC = "Monitor_Discovery_Topic";

    public static final String TUTOR_TOPIC_PREFIX     = "Tutor_Topic";
    
    /** The topic used to send out simulation messages as part of a log playback service within the Domain module, 
     * the Domain module pulls from this */
    public static final String DOMAIN_TOPIC_PREFIX     = "Domain_Topic";

    public static final String SENSOR_STATUS_TOPIC = "Sensor_Status_Topic";

    /** Note: this should only be used by camel to route all messages to all monitors using the same message bus */
    public static final String MONITOR_TOPIC = "Monitor_Topic";

    /**
     * Queues
     */

    public static final String TUTOR_QUEUE 			= "Tutor_Queue";

    public static final String MONITOR_QUEUE        = "Monitor_Queue";

    public static final String SENSOR_QUEUE_PREFIX	= "Sensor_Queue";

    public static final String LEARNER_QUEUE_PREFIX  = "Learner_Queue";

    public static final String LMS_QUEUE_PREFIX  = "LMS_Queue";

    public static final String UMS_QUEUE_PREFIX  = "UMS_Queue";

    public static final String PED_QUEUE_PREFIX  = "Pedagogical_Queue";

    public static final String GATEWAY_QUEUE_PREFIX = "Gateway_Queue";

    public static final String DOMAIN_QUEUE_PREFIX  = "Domain_Queue";

    public static final String TUTOR_QUEUE_PREFIX  = "Tutor_Queue";

    public static final String INBOX_QUEUE_SUFFIX 	= "Inbox";

    public static final String LOGGER_QUEUE         = "Logger_Queue";
}
