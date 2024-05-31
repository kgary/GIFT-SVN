/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.interop.scatt;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;

import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.Siman;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.SimanTypeEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.ta.state.RifleShotMessage;
import mil.arl.gift.gateway.GatewayModule;
import mil.arl.gift.gateway.interop.AbstractInteropInterface;
import mil.arl.gift.net.api.message.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to allow communicate between the SCATT marksmanship
 * training application and GIFT.
 * 
 * @author ohasan
 */
public class SCATTInterface extends AbstractInteropInterface {

    /**
     * Reference to the Logger object.
     */
    private static Logger logger = LoggerFactory
            .getLogger(SCATTInterface.class);

    ServerSocket serverSocket = null;
    boolean listening = true;
    ServerSocketThread serverSocketThread;
    MultiServerThread serverThread;

    private int currentShotIndex = 1;

    Process scattBridgeProcess;
    Process scattProProcess;

    /**
     * contains the types of GIFT messages this interop interface can consume from GIFT modules
     * and handle or send to some external training application
     */
    private static List<MessageTypeEnum> supportedMsgTypes;
    static {
        supportedMsgTypes = new ArrayList<MessageTypeEnum>();
        supportedMsgTypes.add(MessageTypeEnum.SIMAN);
    }
    
    private static List<TrainingApplicationEnum> REQ_TRAINING_APPS;
    static{
        REQ_TRAINING_APPS = new ArrayList<TrainingApplicationEnum>();
    }
    
    /**
     * contains the list of GIFT messages (e.g. Entity State) that this interop plugin interface
     * can create and send to GIFT modules (e.g. Domain module) after consuming some relevant information from 
     * an external training applications (e.g. VBS). 
     */
    private static List<MessageTypeEnum> PRODUCED_MSG_TYPES;
    static{
        PRODUCED_MSG_TYPES = new ArrayList<MessageTypeEnum>();
        PRODUCED_MSG_TYPES.add(MessageTypeEnum.RIFLE_SHOT_MESSAGE);
    }

    public SCATTInterface(String displayName) {
        super(displayName, true);

        logger.info("SCATTInterface created");
    }

    @Override
    public boolean configure(Serializable config) throws ConfigurationException {

        if (config instanceof generated.gateway.SCATT) {

            generated.gateway.SCATT scattConfig = (generated.gateway.SCATT) config;

            // create a socket to listen on the specified port
            try {

                serverSocket = new ServerSocket(scattConfig.getNetworkPort());
            } catch (IOException e) {

                logger.error("Could not listen on port: "
                        + scattConfig.getNetworkPort(), e);
            }

            logger.info("SCATT interface configured on "
                    + scattConfig.getNetworkAddress() + ":"
                    + scattConfig.getNetworkPort());
        }        
        
        return false;
    }

    @Override
    public List<MessageTypeEnum> getSupportedMessageTypes() {
        return supportedMsgTypes;
    }

    @Override
    public boolean handleGIFTMessage(Message message, StringBuilder errorMsg) {

        boolean replySent = false;

        if (message.getMessageType() == MessageTypeEnum.SIMAN) {

            Siman siman = (Siman) message.getPayload();

            logger.info("SCATTInterface: received SIMAN message: "
                    + siman.getSimanTypeEnum());

            if (siman.getSimanTypeEnum() == SimanTypeEnum.LOAD) {

                startListening();

                // launch the SCATT Bridge application
                try {

                    Runtime runtime = Runtime.getRuntime();

                    scattBridgeProcess = runtime
                            .exec("\"../Training.Apps/SCATT/SCATT Bridge/Release/SCATT Bridge.exe\"");

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // sleep this thread for 5 seconds to allow the SCATT Bridge
                // application to load
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            } else if (siman.getSimanTypeEnum() == SimanTypeEnum.START) {

                // launch the SCATT Professional application - for now we launch
                // our emulator of that application
                try {
                    Runtime runtime = Runtime.getRuntime();

                    scattProProcess = runtime
                            .exec("\"../Training.Apps/SCATT/SCATT Emulator/Release/SCATT Emulator.exe\"");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else if (siman.getSimanTypeEnum() == SimanTypeEnum.STOP) {

                stopListening();

                // kill the currently running SCATT Professional application and
                // the SCATT Bridge application
                scattBridgeProcess.destroy();

                scattProProcess.destroy();
            }
        }

        return replySent;
    }

    private void startListening() {

        listening = true;

        // create the thread to manage the ServerSocket
        new ServerSocketThread().start();

        // reset the current shot index back to 1 since we are starting a new
        // listening session
        currentShotIndex = 1;
    }

    private void stopListening() {

        listening = false;
    }

    @Override
    public void cleanup() {
        
        if(serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.error("There was a problem closing the server socket during interop cleanup", e);
            }
        }
    }

    public class ServerSocketThread extends Thread {

        @Override
        public void run() {

            while (listening) {

                try {
                    new MultiServerThread(serverSocket.accept()).start();
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
        }
    }

    public class MultiServerThread extends Thread {
        private Socket socket = null;

        public MultiServerThread(Socket socket) {
            super("MultiServerThread");
            this.socket = socket;
        }

        @Override
        public void run() {

            SCATTMessageTypeEnum messageType = null;
            byte[] buffer = new byte[512];

            while (listening) {

                try {
                    socket.getInputStream().read(buffer);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // logger.debug(Arrays.toString(buffer));

                // if we are still listening (may have stopped while we were
                // waiting on the input stream), handle the packet
                if (listening) {

                    messageType = getMessageType(buffer);

                    logger.debug("Received SCATT message: type: " + messageType);

                    switch (messageType) {

                    case STATE:

                        handleStateMessage(buffer);
                        break;

                    case SHOT:

                        handleShotMessage(buffer);
                        break;
                    default:

                        logger.error("Received a SCATT message that is not yet handled");
                        break;
                    }
                }
            }
        }
    }

    /**
     * Returns the message type corresponding the specified SCATT message.
     * 
     * @param message the byte array holding the SCATT message
     * @return the message type corresponding the specified SCATT message
     */
    SCATTMessageTypeEnum getMessageType(byte[] message) {

        SCATTMessageTypeEnum returnValue = null;
        byte[] temp = new byte[4];

        ByteBuffer byteBuffer = ByteBuffer.wrap(message);

        // get the message type from the message buffer - it's the first four
        // bytes
        byteBuffer.get(temp, 0, 4);
        byteBuffer = ByteBuffer.wrap(temp);

        returnValue = SCATTMessageTypeEnum.getByValue(Integer
                .reverseBytes(byteBuffer.getInt()));

        return returnValue;
    }

    /**
     * Handles a STATE message received from SCATT. Parses the data in the
     * SCATTT message and creates and sends an internal GIFT message.
     * 
     * @param message the byte array holding the SCATT STATE message
     */
    void handleStateMessage(byte[] message) {

    }

    /**
     * Handles a SHOT message received from SCATT. Parses the data in the SCATTT
     * message and creates and sends an internal GIFT message.
     * 
     * @param message the byte array holding the SCATT SHOT message
     */
    void handleShotMessage(byte[] message) {

        float x, y, result;
        byte[] temp = new byte[4];
        int tempInt;
        ByteBuffer byteBuffer = null;

        // get the X value
        byteBuffer = ByteBuffer.wrap(message);
        byteBuffer.position(4);
        byteBuffer.get(temp, 0, 4);
        byteBuffer = ByteBuffer.wrap(temp);
        tempInt = Integer.reverseBytes(byteBuffer.getInt());
        x = Float.intBitsToFloat(tempInt);

        // get the Y value
        byteBuffer = ByteBuffer.wrap(message);
        byteBuffer.position(8);
        byteBuffer.get(temp, 0, 4);
        byteBuffer = ByteBuffer.wrap(temp);
        tempInt = Integer.reverseBytes(byteBuffer.getInt());
        y = Float.intBitsToFloat(tempInt);

        // get the Result value
        byteBuffer = ByteBuffer.wrap(message);
        byteBuffer.position(12);
        byteBuffer.get(temp, 0, 4);
        byteBuffer = ByteBuffer.wrap(temp);
        tempInt = Integer.reverseBytes(byteBuffer.getInt());
        result = Float.intBitsToFloat(tempInt);

        // create and send a GIFT message for the shot
        RifleShotMessage shotMessage = new RifleShotMessage();
        shotMessage.setLocation(new Point3d(x, y, 0));
        shotMessage.setResult(result);
        shotMessage.setShotNumber(currentShotIndex++);

        logger.debug("Sending RifleShotMNessage: " + shotMessage);

        GatewayModule.getInstance().sendMessageToGIFT(shotMessage,
                MessageTypeEnum.RIFLE_SHOT_MESSAGE, this);
    }
    
    @Override
    public List<TrainingApplicationEnum> getReqTrainingAppConfigurations(){
        return REQ_TRAINING_APPS;
    }
    
    @Override
    public List<MessageTypeEnum> getProducedMessageTypes(){
        return PRODUCED_MSG_TYPES;
    }
    
    @Override
    public Serializable getScenarios() {
        return null;
    }

    @Override
    public Serializable getSelectableObjects() {
        return null;
    }
    
    @Override
    public void loadScenario(String scenarioIdentifier)
            throws DetailedException {
        //not supported
    }
    
    @Override
    public File exportScenario(File exportFolder) throws DetailedException {
        return null;
    }  
    
    @Override
    public void selectObject(Serializable objectIdentifier)
            throws DetailedException {
        
    }
    
    @Override
    public Serializable getCurrentScenarioMetadata() throws DetailedException {
        return null;
    }
}
