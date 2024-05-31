/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.interop.unity;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
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
public class UnityInterface extends AbstractInteropInterface {

    /** The logger for the class */
    private static final Logger logger = LoggerFactory.getLogger(UnityInterface.class);

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

    /**
     * The object used to manage communication with the Unity application over a
     * socket.
     */
    private AsyncSocketHandler socketHandler;

    /**
     * The configuration specifying how to connect to the external Unity
     * application.
     */
    private Unity unityConfig;

    /**
     * Constructs a new {@link UnityInterface} with the provided display name.
     *
     * @param displayName The display name of this {@link UnityInterface}. Can't
     *        be null.
     */
    public UnityInterface(String displayName) {
        super(displayName, false);
    }

    @Override
    public boolean configure(Serializable config) throws ConfigurationException {
        if (logger.isTraceEnabled()) {
            logger.trace("configure(" + config + ")");
        }

        if (config instanceof generated.gateway.Unity) {

            /* Save a reference to the configuration. */
            unityConfig = (Unity) config;
            createSocketHandler();

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

    @Override
    public List<MessageTypeEnum> getSupportedMessageTypes() {
        return supportedMsgs;
    }

    @Override
    public List<MessageTypeEnum> getProducedMessageTypes() {
        return producedMsgs;
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
            socketHandler.sendMessage(jsonString);
        } catch (IOException e) {
            logger.error("Caught exception when trying to send message to GIFT Unity SDK:\n"+jsonString, e);
            errorMsg.append("There was a problem sending the following message to the Unity application: ")
                    .append(jsonString).append('\n').append(e);
        }

        return false;
    }

    /**
     * The method that is invoked when a new text message is received from the
     * Unity application.
     *
     * @param line The text that was received from the Unity application.
     */
    private void handleRawUnityMessage(String line) {
        if (logger.isTraceEnabled()) {
            logger.trace("handleTrainingAppData('" + line + "')");
        }

        /* A message that starts with a '!' character indicates that the rest of
         * the line is an error message. This is provided functionality in the
         * GIFT Unity SDK. */
        if (line.startsWith("!")) {
            String errMsg = new StringBuilder("The Unity application reported the following error: ")
                    .append(line)
                    .toString();
            logger.error(errMsg);
            return;
        }

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
            } else {
                final String typeName = message != null ? message.getClass().getName() : "null";
                logger.warn("A message of type '" + typeName + "' was received from the unity application. "
                        + "It could not be sent to the DomainModule because it is not of type TrainingAppState");
            }
        } catch (ParseException e) {
            logger.error("There was a problem parsing the follwing message from the Unity Desktop applicaiton:\n" + line, e);
        } catch (Exception ex) {
            logger.error("There was a problem handling the following message from the Unity Desktop application:\n" + line, ex);
        }
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
    public void setEnabled(boolean value) throws ConfigurationException {
        if (logger.isTraceEnabled()) {
            logger.trace("setEnabled(" + value + ")");
        }

        /* If the values are the same, there's no change to be made. */
        if (value == isEnabled()) {
            return;
        }

        if (value) {
            if (logger.isInfoEnabled()) {
                logger.info("Enabling Unity interface");
            }

            /* Ensure a a connection has been established with the Unity
             * application */
            try {
                establishConnection();
            } catch (IOException ioEx) {
                throw new ConfigurationException("Unable to establish connection",
                        "There was a problem while trying to establish a connection to the '" + getName()
                                + "' Unity application.\n"
                                + "1.) Ensure the Unity application is running before starting GIFT"
                                + "2.) Ensure that the Unity application is listening for connections at '"
                                + unityConfig.getNetworkAddress() + ":" + unityConfig.getNetworkPort() + "'",
                        ioEx);
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("Disabling Unity interface");
            }

            try {
                if (socketHandler != null) {
                    if(logger.isInfoEnabled()){
                        logger.info("Disconnecting socket handler");
                    }
                    socketHandler.disconnect();
                    socketHandler = null; // in order to recreate it upon next needed connection
                }
            } catch (@SuppressWarnings("unused") IOException e) {
                // not sure if we care at this point
            }
        }

        super.setEnabled(value);
    }

    @Override
    public void cleanup() {
        if (logger.isTraceEnabled()) {
            logger.trace("cleanup()");
        }

        try {
            if (socketHandler != null) {
                if(logger.isInfoEnabled()){
                    logger.info("Closing socket handler");
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

    /**
     * Ensures that an {@link AsyncSocketHandler} has been created and assigned
     * to the {@link #socketHandler} field. If the {@link #socketHandler} field
     * is already populated, no action is taken.
     */
    private void createSocketHandler() {
        if (socketHandler == null) {
            final String address = this.unityConfig.getNetworkAddress();
            final int port = this.unityConfig.getNetworkPort();
            socketHandler = new AsyncSocketHandler(address, port, this::handleRawUnityMessage);
            
            if(logger.isInfoEnabled()){
                logger.info("Created new socket handler");
            }
        }
    }

    /**
     * Ensures that the {@link #socketHandler} has established a connection with
     * the Unity application. No action is taken if the connection is already
     * established.
     *
     * @throws IOException if there was an error while establishing the
     *         connection.
     */
    private void establishConnection() throws IOException {
        if (logger.isTraceEnabled()) {
            logger.trace("establishConnection()");
        }

        if (socketHandler == null) {
            createSocketHandler();
        }

        if (!socketHandler.isConnected()) {
            socketHandler.connect();
            if(logger.isInfoEnabled()){
                logger.info("Re-connecting existing socket handler");
            }
        }
    }
}
