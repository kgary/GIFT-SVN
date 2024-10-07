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
public class SteelArttSocket extends AbstractInteropInterface {

    /** The logger for the class */
    private static final Logger logger = LoggerFactory.getLogger(SteelArttSocket.class);

    private Unity unityConfig;

    protected Unity getUnityConfig(){
        return this.unityConfig;
    }

    protected void setUnityConfig(Unity config){
        this.unityConfig = config;
    }

    /**
     * The socket handler that recieves competency message data from the unity app over socket.
     */
    private AsyncSocketHandler dataSocketHandler;

    /**
     * The socket handler that sends control mesgs to the unity app over socket and receives ACKs for the same.
     */
    private AsyncSocketHandler controlSocketHandler;

    public SteelArttSocket(String displayName) {
        super(displayName,false);
    }
    
    /**
     * The singleton collection of the message types which are supported.
     */
    private static final List<MessageTypeEnum> supportedMsgs = Arrays.asList(
            MessageTypeEnum.SIMAN,
            MessageTypeEnum.DISPLAY_FEEDBACK_GATEWAY_REQUEST
    );

    /**
     * The singleton collection of the message types which this interop plugin
     * can produce.
     */
    private static final List<MessageTypeEnum> producedMsgs = Arrays.asList(
            MessageTypeEnum.SIMPLE_EXAMPLE_STATE,
            MessageTypeEnum.STOP_FREEZE
    );

    /**
     * The collection of training applications that must be configured for this
     * interop plugin to run.
     */
    private static final List<TrainingApplicationEnum> REQUIRED_TRAINING_APPLICATIONS = Arrays.asList(
            TrainingApplicationEnum.UNITY_DESKTOP
    );

    @Override
    public List<MessageTypeEnum> getSupportedMessageTypes() {
        return supportedMsgs;
    }

    @Override
    public List<MessageTypeEnum> getProducedMessageTypes() {
        return producedMsgs;
    }
    @Override
    public List<TrainingApplicationEnum> getReqTrainingAppConfigurations() {
        return REQUIRED_TRAINING_APPLICATIONS;
    }

    @Override
    public Serializable getScenarios() throws DetailedException {
        return null;
    }

    @Override
    public Serializable getCurrentScenarioMetadata() throws DetailedException {
        return null;
    }

    @Override
    public Serializable getSelectableObjects() throws DetailedException {
        return null;
    }

    @Override
    public void selectObject(Serializable objectIdentifier) throws DetailedException {

    }

    @Override
    public void loadScenario(String scenarioIdentifier) throws DetailedException {

    }

    @Override
    public File exportScenario(File exportFolder) throws DetailedException {
        return null;
    }

    @Override
    public boolean configure(Serializable config) throws ConfigurationException {
        if (config instanceof generated.gateway.Unity) {
            /* Set the unity config to be used later. */
            setUnityConfig((Unity)config);

            createBothSocketHandlers();

            if (logger.isInfoEnabled()) {
                logger.info("Plugin has been configured");
            }

            return false; 
            // all configure methods in other interop plugins, return false by default
            // Re: InteropConfigFileHandler.java:buildInteropInterfaces(), looks like this is coz
            // the intention of this method is to check if this function "updated" a config value, and only then write it to disk.
        } else {
            throw new ConfigurationException(
                    "Unity Plugin interface can't configure.",
                    "The Unity Plugin interface only uses the interop config type of "
                            + generated.gateway.Unity.class
                            + " and doesn't support using the interop config instance of " + config,
                    null);
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean handleGIFTMessage(Message message, StringBuilder errorMsg) throws ConfigurationException {

        JSONObject jsonMsg;
        try {
            if (message.getMessageType().equals(MessageTypeEnum.SIMAN)) {
                Siman siman = (Siman) message.getPayload();

                JSONObject jsonLoadArgs = null;
                if (SimanTypeEnum.LOAD.equals(siman.getSimanTypeEnum())) {
                    jsonLoadArgs = new JSONObject();
                    final InteropInputs interopInputs = getLoadArgsByInteropImpl(getClass().getName(),
                            siman.getLoadArgs());

                    UnityInteropInputs unityInteropInputs = (UnityInteropInputs) interopInputs.getInteropInput();
                    for (Nvpair nv : unityInteropInputs.getLoadArgs().getNvpair()) {
                        jsonLoadArgs.put(nv.getName(), nv.getValue());
                    }

                }

                jsonMsg = EmbeddedAppMessageEncoder.encodeSimanForEmbeddedApplication(siman, jsonLoadArgs);
            } else {
                jsonMsg = EmbeddedAppMessageEncoder.encodeForEmbeddedApplication(message);
            }
        } catch (Exception e) {
            errorMsg.append("There was a problem encoding the following message for the Unity application: ")
                    .append(message).append('\n').append(e);
            return false;
        }

        final String jsonString = jsonMsg.toJSONString();
        try {
            controlSocketHandler.sendMessage(jsonString);
        } catch (IOException e) {
            logger.error("Caught exception when trying to send message to GIFT Unity SDK:\n"+jsonString, e);
            errorMsg.append("There was a problem sending the following message to the Unity application: ")
                    .append(jsonString).append('\n').append(e);
        }

        return false;
    }

    /**
     * The method that is invoked when a data message is received from the
     * Unity application. This method then should ideally send an ACK.
     *
     * @param line The text that was received from the Unity application.
     */
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
    private void handleControlMessageAck(String line) {
        logger.info("handleControlMessageAck()");
        if (logger.isTraceEnabled()) {
            logger.trace("handleControlMessageAck('" + line + "')");
        }

        // Handle control message ACK here

        // Log the control message
        logger.info("Control message ACK received: {}", line);
    }

  
    // this method "enables" the interop plugin to be able to handle incoming SIMAN msgs or external data msgs.
    @Override
    public void setEnabled(boolean value) throws ConfigurationException {
        if (logger.isTraceEnabled()) {
            logger.trace("setEnabled(" + value + ")");
        }

        /* If the values are the same, there's no change to be made. */
        if (value == isEnabled()) {
            return;
        }

        if(value){
            if (logger.isInfoEnabled()) {
                logger.info("Enabling Unity interface");
            }

            /* Ensure a a connection has been established with the Unity
             * application */
            try {
                establishBothSocketsConnection();
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
            disconnectSocketHandler(dataSocketHandler);
            disconnectSocketHandler(controlSocketHandler);
        }
        
        super.setEnabled(value); // called this coz all setEnabled methods in other interop plugins call the super from AbstractInteropInterface.
    }

    @Override
    public void cleanup() {
        if (logger.isTraceEnabled()) {
            logger.trace("cleanup()");
        }
        closeSocketHandler(controlSocketHandler);
        closeSocketHandler(dataSocketHandler);
    }

    private void createBothSocketHandlers() {
        logger.info("createBothSocketHandlers()");
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

    private void establishBothSocketsConnection() throws IOException{
        logger.info("establishBothSocketsConnection()");
        if (controlSocketHandler == null || dataSocketHandler == null) {
            createBothSocketHandlers();
        }

        connectSocketHandler(controlSocketHandler);
        connectSocketHandler(dataSocketHandler);
    }

    protected void connectSocketHandler(AsyncSocketHandler socketHandler) throws IOException{
        // This method will connect the socket handler if it isn't connected.
        // Created this method coz it is being called twice, once for each socket.
         if (!socketHandler.isConnected()) {
            socketHandler.connect();
            if(logger.isInfoEnabled()){
                logger.info("Re-connecting existing socket handler");
            }
        }
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
