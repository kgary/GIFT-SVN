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
import mil.arl.gift.gateway.interop.AbstractInteropInterface;
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
public class SteelArttSocket extends SteelArttInteropTemplate {

    /** The logger for the class */
    private static final Logger logger = LoggerFactory.getLogger(SteelArttSocket.class);

    /**
     * The socket handler that recieves competency message data from the unity app over socket.
     */
    private AsyncSocketHandler dataSocketHandler;

    public SteelArttSocket(String displayName) {
        super(displayName,false);
    }
    

    @Override
    public void cleanup() {
        if (logger.isTraceEnabled()) {
            logger.trace("cleanup()");
        }
        closeSocketHandler(controlSocketHandler);
        closeSocketHandler(dataSocketHandler);
    }

    private void createSocketsOrConsumers() {
        logger.info("createSocketsOrConsumers()");
        // can't create a template method for both the if blocks below coz the indiviudal methods for getting each port are different.
        // i.e. getNetworkPort() & getDataNetworkPort()
        if(controlSocketHandler == null){
            final String controlAddress = getUnityConfig().getNetworkAddress();
            final int controlPort = getUnityConfig().getNetworkPort();
            controlSocketHandler = new AsyncSocketHandler(controlAddress, controlPort, this::handleControlMessageAck);
            
            if(logger.isInfoEnabled()){
                logger.info("Created new control socket handler");
            }
        }
        if (dataSocketHandler == null) {
            final String dataAddress = getUnityConfig().getNetworkAddress();
            final int dataPort = getUnityConfig().getDataNetworkPort();
            logger.info("dataPort: "+ dataPort);
            dataSocketHandler = new AsyncSocketHandler(dataAddress, dataPort, this::handleRawUnityMessage);

            if(logger.isInfoEnabled()){
                logger.info("Created new data socket handler");
            }
        }

    }

    private void establishConnection() throws IOException{
        logger.info("establishConnection()");
        if (controlSocketHandler == null || dataSocketHandler == null) {
            createSocketsOrConsumers();
        }

        connectSocketHandler(controlSocketHandler);
        connectSocketHandler(dataSocketHandler);
    }

    private void disconnectSocketHandler(AsyncSocketHandler socketHandler){
        // This method will disconnect the socket handler.
        // Created this method coz it is being called twice, once for each socket.
        try{
                 if (socketHandler != null) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Disconnecting data socket handler");
                    }
                    socketHandler.disconnect();
                    socketHandler = null;
                }
            } catch (IOException e) {
                logger.error("Error disconnecting data socket handler: ", e);
            }
    }

    protected void closeSocketHandler(AsyncSocketHandler socketHandler){
        // This method will close the socket handler and make the socketHandler variable = null.
        // Created this method coz it is being called twice, once for each socket.
        try {
            if (socketHandler != null) {
                if(logger.isInfoEnabled()){
                    logger.info("Closing data socket handler");
                }
                socketHandler.close();
                socketHandler = null; // in order to recreate it upon next needed connection
            }
        } catch (Exception e) {
            final String errMsg = new StringBuilder("There was a problem closing the socket connection to ")
                    .append(getName()).toString();

            logger.error(errMsg, e);
        }
    }
    

}
