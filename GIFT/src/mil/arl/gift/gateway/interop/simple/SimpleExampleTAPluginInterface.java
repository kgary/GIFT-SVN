/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.interop.simple;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.Siman;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.SimanTypeEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.ta.state.SimpleExampleState;
import mil.arl.gift.common.ta.state.StopFreeze;
import mil.arl.gift.common.ta.state.TrainingAppState;
import mil.arl.gift.gateway.GatewayModule;
import mil.arl.gift.gateway.GatewayModuleUtils;
import mil.arl.gift.gateway.interop.AbstractInteropInterface;
import mil.arl.gift.gateway.interop.ApplicationConnectionFactory;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.xmlrpc.XMLRPCClient;
import mil.arl.gift.net.xmlrpc.XMLRPCServer;

/**
 * This interop plugin is used to communicate with the C# 'Simple Example Training App' located
 * in Training.Apps\ExampleTrainingApplication folder.  
 * 
 * The Simple Example Training Application C# program presents a window to handle various
 * interactions with GIFT.  That application communicates to GIFT via this plugin class using XML-RPC.
 * There are other communication protocols that could have been chosen such as using Sockets (used in the
 * existing DIS interop plugin), however we fealt that XML-RPC is a little easier for beginner Java programmers.
 * 
 * This plugin will register to handle SIMAN and Display Feedback GIFT messages.
 * 
 * Note: All GIFT messages received by interop plugins will be during the 'Training Application' part of 
 * a GIFT course.
 * 
 * @author mhoffman
 *
 */
public class SimpleExampleTAPluginInterface extends AbstractInteropInterface {
    
    /** Instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(SimpleExampleTAPluginInterface.class);
    
    /**
     * contains the types of GIFT messages this interop interface can consume from GIFT modules
     * and handle or send to some external training application
     */
    private static List<MessageTypeEnum> supportedMsgTypes;    
    static{
        supportedMsgTypes = new ArrayList<MessageTypeEnum>();
        supportedMsgTypes.add(MessageTypeEnum.SIMAN);
        supportedMsgTypes.add(MessageTypeEnum.DISPLAY_FEEDBACK_GATEWAY_REQUEST);
    }
    
    private static List<TrainingApplicationEnum> REQ_TRAINING_APPS;
    static{
        REQ_TRAINING_APPS = new ArrayList<TrainingApplicationEnum>();
        REQ_TRAINING_APPS.add(TrainingApplicationEnum.SIMPLE_EXAMPLE_TA); 
    }
    
    /**
     * contains the list of GIFT messages (e.g. Entity State) that this interop plugin interface
     * can create and send to GIFT modules (e.g. Domain module) after consuming some relevant information from 
     * an external training applications (e.g. VBS). 
     */
    private static List<MessageTypeEnum> PRODUCED_MSG_TYPES;
    static{
        PRODUCED_MSG_TYPES = new ArrayList<MessageTypeEnum>();
        PRODUCED_MSG_TYPES.add(MessageTypeEnum.STOP_FREEZE);
        PRODUCED_MSG_TYPES.add(MessageTypeEnum.SIMPLE_EXAMPLE_STATE);
    }
    
    /** This is the AHK class name of the Example Training Application window used to manipulate the window */
    private static final String WINDOW_TITLE = "Simple Example Training Application";
    //MH 4/4/14 - It was finally figured out that the suffix to the c# window is based on the visual studio version,
    //            therefore implemented wild card AHK class logic
    //private static final String AHK_CLASS_RELEASE = "WindowsForms10.Window.8.app.0.378734a";
    //private static final String AHK_CLASS_DEBUG = "WindowsForms10.Window.8.app.0.bf7771";
    //private static final String AHK_CLASS = "WindowsForms10.Window.8.app.0.378734a";
    private static final String AHK_CLASS = GatewayModuleUtils.AHK_CLASS_WILDCARD;
    
    /** this exe will launch the Simple Example Training Application */
    private static final File APP_EXE = 
            new File(".." + File.separator + "Training.Apps" + File.separator + "SimpleExampleTrainingApplication" + File.separator + 
                    "ExampleTrainingApplication" + File.separator + "bin" + File.separator + "Release" + File.separator + "ExampleTrainingApplication.exe");

    /** this is the process that launches the TA and allows this class to monitor whether the TA has closed or not */
    private Process trainingAppProcess = null;
    
    /**
     * The XML RPC server created by this class to allow the training application to remotely call methods
     * in this class, mainly the 'handleTrainingApplicationMessage' method.
     */
    private XMLRPCServer server;
    
    /**
     * The XML RPC client created by this class to call remote methods in the training application C# program which
     * is hosting an XML RPC server.
     */
    private XMLRPCClient client;
    
    /** the Simple Example TA XML-RPC hosted method names */
    private static final String TA_LOAD_METHOD_NAME = "load";
    private static final String TA_BLOB_METHOD_NAME = "blob";
    private static final String TA_CLOSE_APP_METHOD_NAME = "closeApplication";
    
    /** needed for XML-RPC called methods that will need to send messages via the Gateway module */
    private static SimpleExampleTAPluginInterface instance = null;
    
    /**
     * This inner class contains the RPC methods the training application RPC client can call.
     * The only reason I choose not to use the outer class of SimpleExamplePluginTAPluginInterface was to prevent having
     * to create another constructor (as mentioned in the Notes below) and to organize the RPC methods
     * together instead of possible exposing other methods in ExamplePLuginInterface.
     * 
     * Notes: 
     * 1) This class must contain either no class constructor OR an empty constructor.
     * 2) Every RPC method must return a non-null, non-void, non java.lang.Object value (e.g. Integer).
     * 3) if using an inner class, the class must be static
     *  
     * @author mhoffman
     *
     */
    public static class SimpleExampleTAPluginXMLRPC{
        
        /**
         * This is a RPC method hosted by the RPC server in this class and called by the 
         * training application's RPC client to handle 'game state' messages.
         * 
         * XML-RPC method name = 
         *  "mil.arl.gift.gateway.interop.simple.SimpleExampleTAPluginInterface$SimpleExampleTAPluginXMLRPC.handleTrainingApplicationMessage"
         * 
         * @param message the training application message content
         * @return Integer an arbitrary return object to satisfy the XML-RPC library requirements
         */
        public Integer handleTrainingApplicationMessage(String message){
            
            SimpleExampleState exampleState = new SimpleExampleState(message);
            sendMessageToGIFT(exampleState, MessageTypeEnum.SIMPLE_EXAMPLE_STATE);   
            
            return XMLRPCServer.EMPTY_RETURN_OBJECT;
        }
        
        /**
         * This is a RPC method hosted by the RPC server in this class and called by the 
         * training application's RPC client to notify GIFT that the TA is finished.
         * 
         * XML-RPC method name = 
         *  "mil.arl.gift.gateway.interop.simple.SimpleExampleTAPluginInterface$SimpleExampleTAPluginXMLRPC.handleTrainingApplicationFinished"
         * 
         * @return Integer an arbitrary return object to satisfy the XML-RPC library requirements
         */
        public Integer handleTrainingApplicationFinished(){
            
            StopFreeze stopFreeze = new StopFreeze(0, 0, 0, 0);
            sendMessageToGIFT(stopFreeze, MessageTypeEnum.STOP_FREEZE);   
            
            return XMLRPCServer.EMPTY_RETURN_OBJECT;
        }
    }
    
    /**
     * Class constructor
     * 
     * @param name - the display name of the plugin
     */
    public SimpleExampleTAPluginInterface(String name){
        super(name, true); 
        
        instance = this;
    }

    @Override
    public boolean configure(Serializable config) throws ConfigurationException {
        
        if (config instanceof generated.gateway.XMLRPC) {

            generated.gateway.XMLRPC pluginConfig = (generated.gateway.XMLRPC) config;
            try{
                server = ApplicationConnectionFactory.createXMLRPCServer(pluginConfig.getServerNetworkPort(), SimpleExampleTAPluginXMLRPC.class);
            }catch(Exception e){
                throw new ConfigurationException("Failed to create the XML RPC server.",
                        e.getMessage(), 
                        e);
            }
            
            try{
                client = ApplicationConnectionFactory.createXMLRPCClient(pluginConfig.getExternalServerNetworkAddress(), pluginConfig.getExternalServerNetworkPort());
            }catch(Exception e){
                throw new ConfigurationException("Failed to create the XML RPC client.",
                        e.getMessage(), 
                        e);
            }
            
            logger.info("Plugin has been configured");

        } else {
            throw new ConfigurationException(getName()+" Plugin interface can't configure.",
                    getName() + " plugin interface uses "+generated.gateway.XMLRPC.class+" interop input and doesn't support using the interop config instance of " + config,
                    null);
        }
        
        return false;
    }

    @Override
    public List<MessageTypeEnum> getSupportedMessageTypes() {
        return supportedMsgTypes;
    }
    
    @Override
    public void setEnabled(boolean value) throws ConfigurationException{
        
        boolean isEnabledAlready = isEnabled();
        super.setEnabled(value);
        
        if(!isEnabledAlready && value){
            
            //
            // launch the training application 
            // Note: 
            //      1) this logic is here just so the GIFT user doesn't have to launch it, instead GIFT orchestrates 
            //         the starting and stopping of the TA.
            //      2) this logic could have been placed somewhere else in this class, like when the SIMAN Load msg is received.
            //
            
            if(trainingAppProcess != null){
                //check if the processes has exited
                
                try{
                    //will throw an exception if the process hasn't existed
                    trainingAppProcess.exitValue();
                }catch(IllegalThreadStateException threadStateException){
                    logger.error("The Simple Example TA launch thread has not exited from the previous launch.  Therefore the TA is in an unknown state.  "+
                            "One possible way to remedy this problem before exeucting the course again would be to terminate the TA (possibly through task manager if need be?).", threadStateException);
                    throw new RuntimeException("There was a problem launching the Simple Example TA.");
                }
            }
            
            ProcessBuilder pBuilder = new ProcessBuilder(APP_EXE.getAbsolutePath());
            pBuilder.redirectErrorStream(true);
            try {
                trainingAppProcess = pBuilder.start();
            } catch (IOException ioe) {
                logger.error("Caught exception when trying to start the Simple Example TA.", ioe);
                throw new RuntimeException("There was a problem launching the Simple Example TA.");
            }
            
            //allowing the TA window to appear so the first message(s) can be seen
            try {
                Thread.sleep(1000);
            } catch (@SuppressWarnings("unused") InterruptedException e1) {
                //not important
            }
            
            //listen for RPC requests
            try {
                server.start();
            } catch (IOException e) {
                logger.error("Caught exception when trying to start the RPC server.", e);
                throw new RuntimeException("There was a problem starting the RPC server.");
            }
            
            logger.info("Started listening for incoming training application messages.");
            
        }else if (!value){
            
            //
            // Notify the TA to close (because GIFT is disabling the interop plugin that communicates with it)
            // Note: ignoring failures in closing the TA in order to allow the GIFT course to continue (or Gateway module to shutdown).
            //
            try{
                if(server.isRunning() && trainingAppProcess != null){
                	
                	// Server is running, TA has ran at some point
                	// We want to ensure both are killed
                	try{
                		// This method call will tell us if TA is still running
                		trainingAppProcess.exitValue();
                		
                	}catch (@SuppressWarnings("unused") IllegalThreadStateException e) {
                		// Training app is still running, kill it
                		logger.warn("Training application is still running when it's not expected to be, attempting to close it.");
                		client.callMethod(TA_CLOSE_APP_METHOD_NAME, new Vector<>(0), null);
                	}
                	
                }
            }catch(Exception e){
                logger.warn("Caught exception while trying to call the remote method named "+TA_CLOSE_APP_METHOD_NAME+
                        " in order to have the application close itself.  Therefore the application will most likely remain open.", e);
            }
            
            //stop listening for RPC requests
            server.stop();

            
            logger.info("Stopped listening for incoming training application messages.");
        }
    }

    /**
     * Call upon the Gateway module to construct and then send a new GIFT message using the payload
     * provided.
     * 
     * @param payload the contents of the message to send
     * @param messageTypeEnum the enumerated type of the message
     */
    private static void sendMessageToGIFT(TrainingAppState payload, MessageTypeEnum messageTypeEnum){        
        GatewayModule.getInstance().sendMessageToGIFT(payload, messageTypeEnum, instance); 
    }
    
    @Override
    public boolean handleGIFTMessage(Message message, StringBuilder errorMsg) {
        
        // Below is some arbitrary handling of GIFT messages.
        // In most instances your interop plugin will need to handle SIMAN messages in order to 
        // allow GIFT to synchronize with the TA.
        // Look at the other interop plugins for more examples of handling GIFT messages.
        
        if (message.getMessageType() == MessageTypeEnum.SIMAN) {

            Siman siman = (Siman) message.getPayload();
            
            if (siman.getSimanTypeEnum() == SimanTypeEnum.LOAD) { 
                
                //Parse the specific load arguments authored in the course for this interop plugin
                generated.course.InteropInputs interopInputs = getLoadArgsByInteropImpl(this.getClass().getName(), siman.getLoadArgs());
                generated.course.SimpleExampleTAInteropInputs inputs = (generated.course.SimpleExampleTAInteropInputs) interopInputs.getInteropInput();
                
                generated.course.SimpleExampleTAInteropInputs.LoadArgs args = inputs.getLoadArgs();
                
                //actually calling a different RPC method here, called "load" to provide another example of an RPC method.
                try{
                    loadScenario(args.getScenarioName());
                    logger.info("Loaded Simple Example TA scenario  '" + args.getScenarioName() +"'.");
                }catch(DetailedException e){
                    errorMsg.append(e.getReason()).append(" because ").append(e.getDetails());
                }
                
            }else if(siman.getSimanTypeEnum() == SimanTypeEnum.PAUSE){
                
                client.callMethod(TA_BLOB_METHOD_NAME, "Pause message received", errorMsg);
                
                //setting the "Example Training Application" program's window to NOT be always on top,
                //thereby allowing the user to select or view other windows in the same desktop real estate.
                //This is optional but adds an automated look and feel
                try{
                    GatewayModuleUtils.setAlwaysOnTop(AHK_CLASS, WINDOW_TITLE, false);  
                }catch(ConfigurationException e){
                    logger.error("Failed to set the training app windows as NOT the foreground window", e);
                }
                
                
            }else if(siman.getSimanTypeEnum() == SimanTypeEnum.RESTART){
                
                client.callMethod(TA_BLOB_METHOD_NAME, "Restart message received", errorMsg);
                
            }else if(siman.getSimanTypeEnum() == SimanTypeEnum.RESUME){
                
                client.callMethod(TA_BLOB_METHOD_NAME, "Resume message received", errorMsg);
                
                //setting the "Example Training Application" program's window to be in the foreground
                //This is optional but adds an automated look and feel
                try{
                    GatewayModuleUtils.setAlwaysOnTop(AHK_CLASS, WINDOW_TITLE, true);  
                    GatewayModuleUtils.giveFocus(AHK_CLASS, WINDOW_TITLE);
                }catch(ConfigurationException e){
                    logger.error("Failed to set the training app windows as the foreground window", e);
                }
                
            }else if(siman.getSimanTypeEnum() == SimanTypeEnum.START){
                
                client.callMethod(TA_BLOB_METHOD_NAME, "Start message received", errorMsg);
                
                //setting the "Example Training Application" program's window to be in the foreground
                //This is optional but adds an automated look and feel
                try{
                    GatewayModuleUtils.setAlwaysOnTop(AHK_CLASS, WINDOW_TITLE, true);
                    GatewayModuleUtils.giveFocus(AHK_CLASS, WINDOW_TITLE);
                }catch(ConfigurationException e){
                    logger.error("Failed to set the training app windows as the foreground window", e);
                }
                
            }else if(siman.getSimanTypeEnum() == SimanTypeEnum.STOP){
                
                client.callMethod(TA_CLOSE_APP_METHOD_NAME, new Vector<>(0), null);
                
            }else{
                errorMsg.append(getName()).append(" plugin can't handle siman type of ").append(siman.getSimanTypeEnum());
                logger.error("Found unhandled Siman type of "+siman.getSimanTypeEnum());
            }
            
        }else if(message.getMessageType() == MessageTypeEnum.DISPLAY_FEEDBACK_GATEWAY_REQUEST){            
            //send some feedback text to the example training application
            
            String text = (String)message.getPayload();
            client.callMethod(TA_BLOB_METHOD_NAME, "Display Feedback received with feedback of \""+text+ "\"", errorMsg);
            
        }else{
            logger.error("Received unhandled GIFT message to send over to the "+getName()+" plugin, " + message);
            
            errorMsg.append(getName()).append(" plugin can't handle message of type ").append(message.getMessageType());
        }
            
        return false;
    }

    @Override
    public void cleanup() {
        server.stop();
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
        
        StringBuilder errorMsg = new StringBuilder();
        client.callMethod(TA_LOAD_METHOD_NAME, scenarioIdentifier, errorMsg);   
        
        if(errorMsg.length() > 0){
            throw new DetailedException("Failed to load the Simple Example TA scenario named '"+scenarioIdentifier+"'.", 
                    "There was an error when trying to load the scenario:"+errorMsg.toString(), null);
        }    
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
