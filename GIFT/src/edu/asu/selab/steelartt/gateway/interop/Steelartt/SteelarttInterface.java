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
    private AsyncSocketHandler dataSocketHandler;

    public SteelarttInterface(String displayName) {
        super(displayName, false);
    }
    

    /**
     * The method that is invoked when a data message is received from the
     * Unity application. This method then should ideally send an ACK.
     *
     * @param line The text that was received from the Unity application.
     */
    @Override
    private void handleRawUnityMessage(String line) {
        logger.info("handleRawUnityMessage()");

        try {
            final Object message = EmbeddedAppMessageEncoder.decodeForGift(line);
            MessageTypeEnum msgType;
            try {
                msgType = EmbeddedAppMessageEncoder.getDecodedMessageType(message);
            } catch (Exception e) {
                logger.error("There was a problem determining the message type of a payload.", e);
                return;
            }
    
            if (message instanceof TrainingAppState) {
                GatewayModule.getInstance().sendMessageToGIFT((TrainingAppState) message, msgType, this);
                // Might introduce changes here based on what we want to send to Gateway module.
            } else {
                final String typeName = message != null ? message.getClass().getName() : "null";
                logger.warn("A message of type '" + typeName + "' was received from the unity application. "
                        + "It could not be sent to the DomainModule because it is not of type TrainingAppState");
            }
        } catch (ParseException e) {
            logger.error("There was a problem parsing the following message from the Unity Desktop application:\n" + line, e);
        } catch (Exception ex) {
            logger.error("There was a problem handling the following message from the Unity Desktop application:\n" + line, ex);
        }
    }

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
        super.setEnabled();

        if(!value){
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
            if (dataSocketHandler != null) {
                if(logger.isInfoEnabled()){
                    logger.info("Closing data socket handler");
                }
                dataSocketHandler.close();
                dataSocketHandler = null; // in order to recreate it upon next needed connection
            }
        } catch (Exception e) {
            final String errMsg = new StringBuilder("There was a problem closing the data socket connection to ")
                    .append(getName()).toString();

            logger.error(errMsg, e);
        }

        
        
    }

    @Override
    private void createSocketHandlers() {
        logger.info("createSocketHandlers()");
        super.createSocketHandlers();
        if (dataSocketHandler == null) {
            final String dataportAddress = this.unityConfig.getNetworkAddress();
            final int dataPort = this.unityConfig.getDataNetworkPort();
            logger.info("dataPort: "+ dataPort);
            dataSocketHandler = new AsyncSocketHandler(dataportAddress, dataPort, this::handleRawUnityMessage);

            if(logger.isInfoEnabled()){
                logger.info("Created new data socket handler");
            }
        }

    }

    @Override
    private void establishConnection() throws IOException {
        logger.info("establishConnection()");
       super.establishConnection();

        if (dataSocketHandler == null) {
            createSocketHandlers();
        }
    
        if (!dataSocketHandler.isConnected()) {
            try {
                dataSocketHandler.connect();
                if (logger.isInfoEnabled()) {
                    logger.info("Established data connection with Unity application");
                }
            } catch (IOException e) {
                logger.error("Failed to establish data connection with Unity application at {}:{}", unityConfig.getNetworkAddress(), unityConfig.getControlNetworkPort(), e);
                throw e;
            }
        }
    }
}
