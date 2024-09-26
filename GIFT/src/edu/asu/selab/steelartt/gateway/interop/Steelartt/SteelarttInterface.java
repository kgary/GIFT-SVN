package mil.arl.gift.gateway.interop.Steelartt;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.course.InteropInputs;
import generated.course.Nvpair;
import generated.course.UnityInteropInputs;
import generated.gateway.Unity;
import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.Siman;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.SimanTypeEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.ta.state.TrainingAppState;
import mil.arl.gift.gateway.GatewayModule;
import mil.arl.gift.gateway.interop.unity.UnityInterface;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.embedded.message.codec.EmbeddedAppMessageEncoder;
import mil.arl.gift.net.socket.AsyncSocketHandler;

/**
 * The interop plugin that allows for communication with a training application
 * that was built using the Unity game engine and the GIFT Unity SDK.
 *
 * @author tflowers
 *
 */
public class SteelarttInterface extends UnityInterface {

    /** The logger for the class */
    private static final Logger logger = LoggerFactory.getLogger(SteelarttInterface.class);


    /**
     * The socket handler that recieves positional/speech data from the unity app over socket
     */
    // private AsyncSocketHandler dataSocketHandler;

    // Testing Kafka
    private KafkaDataConsumer kafkaDataConsumer;

    public SteelarttInterface(String displayName) {
        super(displayName);
    }
    

    @Override
    public boolean configure(Serializable config) throws ConfigurationException {
        super.configure(config);

        if (config instanceof generated.gateway.Unity) {

            // Testing Kafka
            createKafkaDataConsumer();

            if (logger.isInfoEnabled()) {
                logger.info("Plugin has been configured");
            }

            return false;
        } else {
            throw new ConfigurationException(
                    "Unity Plugin interface can't configure.",
                    "The Unity Plugin interface only uses the interop config type of "
                            + generated.gateway.Unity.class
                            + " and doesn't support using the interop config instance of " + config,
                    null);
        }

    }

    /**
     * The method that is invoked when a data message is received from the
     * Unity application. This method then should ideally send an ACK.
     *
     * @param line The text that was received from the Unity application.
     */

    // private void handleRawUnityMessage(String line) {
    //     logger.info("handleRawUnityMessage()");

    //     try {
    //         final Object message = EmbeddedAppMessageEncoder.decodeForGift(line);
    //         MessageTypeEnum msgType;
    //         try {
    //             msgType = EmbeddedAppMessageEncoder.getDecodedMessageType(message);
    //         } catch (Exception e) {
    //             logger.error("There was a problem determining the message type of a payload.", e);
    //             return;
    //         }
    
    //         if (message instanceof TrainingAppState) {
    //             GatewayModule.getInstance().sendMessageToGIFT((TrainingAppState) message, msgType, this);
    //             // Might introduce changes here based on what we want to send to Gateway module.
    //         } else {
    //             final String typeName = message != null ? message.getClass().getName() : "null";
    //             logger.warn("A message of type '" + typeName + "' was received from the unity application. "
    //                     + "It could not be sent to the DomainModule because it is not of type TrainingAppState");
    //         }
    //     } catch (ParseException e) {
    //         logger.error("There was a problem parsing the following message from the Unity Desktop application:\n" + line, e);
    //     } catch (Exception ex) {
    //         logger.error("There was a problem handling the following message from the Unity Desktop application:\n" + line, ex);
    //     }
    // }

    // This method receives the ACKs for the control messages(sent by gift to unity)  sent by the unity app. 
    private void handleControlMessage(String line) {
        logger.info("handleControlMessage()");
        if (logger.isTraceEnabled()) {
            logger.trace("handleControlMessage('" + line + "')");
        }

        // Handle control message ACK here

        // Log the control message
        logger.info("Control message ACK received: {}", line);
    }

  

    @Override
    public void setEnabled(boolean value) throws ConfigurationException {
        super.setEnabled(value);

        if(value){
            try {
                // establishDataSocketConnection();
                // Testing Kafka
                if (value) {
                    kafkaDataConsumer.start();
                } else {
                    kafkaDataConsumer.stop();
                }
            } catch (IOException ioEx) {
                throw new ConfigurationException("Unable to establish connection",
                        "There was a problem while trying to establish a connection to the '" + getName()
                                + "' Unity application.\n"
                                + "1.) Ensure the Unity application is running before starting GIFT"
                                + "2.) Ensure that the Unity application is listening for connections at '"
                                + getUnityConfig().getNetworkAddress() + ":" + getUnityConfig().getNetworkPort() + "'",
                        ioEx);
            }
        }
        else{
            try{
                 if (dataSocketHandler != null) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Disconnecting data socket handler");
                    }
                    dataSocketHandler.disconnect();
                    dataSocketHandler = null; // in order to recreate it upon next needed connection
                }
            } catch (IOException e) {
                logger.error("Error disconnecting data socket handler: ", e);
            }
        }
        
    }

    @Override
    public void cleanup() {
        if (logger.isTraceEnabled()) {
            logger.trace("cleanup()");
        }
        super.cleanup();
        try {
            // if (dataSocketHandler != null) {
            //     if(logger.isInfoEnabled()){
            //         logger.info("Closing data socket handler");
            //     }
            //     dataSocketHandler.close();
            //     dataSocketHandler = null; // in order to recreate it upon next needed connection
            // }
            if (kafkaDataConsumer != null) {
                kafkaDataConsumer.stop();
                kafkaDataConsumer = null;
            }
        } catch (Exception e) {
            final String errMsg = new StringBuilder("There was a problem closing the data socket connection to ")
                    .append(getName()).toString();

            logger.error(errMsg, e);
        }

        
        
    }

    // private void createDataSocketHandler() {
    //     logger.info("createDataSocketHandler()");
    //     if (dataSocketHandler == null) {
    //         final String dataportAddress = getUnityConfig().getNetworkAddress();
    //         final int dataPort = getUnityConfig().getDataNetworkPort();
    //         logger.info("dataPort: "+ dataPort);
    //         dataSocketHandler = new AsyncSocketHandler(dataportAddress, dataPort, this::handleRawUnityMessage);

    //         if(logger.isInfoEnabled()){
    //             logger.info("Created new data socket handler");
    //         }
    //     }

    // }
    private void createKafkaDataConsumer() {
        if (kafkaDataConsumer == null) {
            String bootstrapServers = "localhost:9092"; // Replace with your Kafka broker address
            String topic = "unity-data-topic"; // Replace with your Kafka topic
            String groupId = "gift-consumer-group"; // Replace with your consumer group ID
            kafkaDataConsumer = new KafkaDataConsumer(bootstrapServers, topic, groupId, this);
        }
    }


    // private void establishDataSocketConnection() throws IOException {
    //     logger.info("establishDataSocketConnection()");
    //     if (dataSocketHandler == null) {
    //         createKafkaDataConsumer();
    //     }
    
    //     if (!dataSocketHandler.isConnected()) {
    //         try {
    //             dataSocketHandler.connect();
    //             if (logger.isInfoEnabled()) {
    //                 logger.info("Established data connection with Unity application");
    //             }
    //         } catch (IOException e) {
    //             logger.error("Failed to establish data connection with Unity application at {}:{}", getUnityConfig().getNetworkAddress(), getUnityConfig().getNetworkPort(), e);
    //             throw e;
    //         }
    //     }
    // }
}
