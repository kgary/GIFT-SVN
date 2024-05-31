/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.interop.detestbedplugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
import mil.arl.gift.common.ta.request.EnvironmentControl;
import mil.arl.gift.gateway.GatewayModuleUtils;
import mil.arl.gift.gateway.installer.TrainingApplicationInstallPage;
import mil.arl.gift.gateway.interop.AbstractInteropInterface;
import mil.arl.gift.gateway.interop.ApplicationConnectionFactory;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.xmlrpc.XMLRPCClient;
import mil.arl.gift.gateway.GatewayModuleProperties;

/**
 * This interop plugin interface is responsible for communicating with the DE Testbed training application via XMLRPC.
 * 
 * @author dscrane
 *
 */
public class DETestbedPluginXMLRPCInterface extends AbstractInteropInterface {
    
    /** Instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(DETestbedPluginXMLRPCInterface.class);
    
    /** this .bat will launch the DE Testbed */
    private static File DE_TESTBED_BAT;
    
    /** this is the process that launches the DE Testbed and allows this class to monitor whether the DE Testbed has closed or not */
    private Process deTestbedProcess; 
    
    /**
     * contains the types of GIFT messages this interop interface can consume from GIFT modules
     * and handle or send to some external training application
     */
    private static List<MessageTypeEnum> supportedMsgTypes;    
    static{
        supportedMsgTypes = new ArrayList<MessageTypeEnum>();
        supportedMsgTypes.add(MessageTypeEnum.SIMAN);
        supportedMsgTypes.add(MessageTypeEnum.DISPLAY_FEEDBACK_GATEWAY_REQUEST);
        supportedMsgTypes.add(MessageTypeEnum.ENVIRONMENT_CONTROL);
    }
    
    /**
     * contains the training applications that this interop plugin was built for and should connect to
     */
    private static List<TrainingApplicationEnum> REQ_TRAINING_APPS;
    static{
        REQ_TRAINING_APPS = new ArrayList<TrainingApplicationEnum>();
        REQ_TRAINING_APPS.add(TrainingApplicationEnum.DE_TESTBED); 
    }
    
    /**
     * contains the list of GIFT messages (e.g. Entity State) that this interop plugin interface
     * can create and send to GIFT modules (e.g. Domain module) after consuming some relevant information from 
     * an external training applications (e.g. VBS). 
     */
    private static List<MessageTypeEnum> PRODUCED_MSG_TYPES;
    static{
        PRODUCED_MSG_TYPES = new ArrayList<MessageTypeEnum>();
    }
    
    /**
     * The XML RPC client created by this class to call remote methods in the training application C# program which
     * is hosting an XML RPC server.
     */
    private XMLRPCClient client;
    
    /** the DE Testbed XML-RPC hosted method names */
    private static final String DE_TESTBED_LOAD_METHOD_NAME = "load";
    private static final String DE_TESTBED_PAUSE_METHOD_NAME = "pause";
    private static final String DE_TESTBED_RESUME_METHOD_NAME = "resume";
    private static final String DE_TESTBED_STOP_METHOD_NAME = "stop";
    private static final String DE_TESTBED_DISPLAY_MESSAGE_METHOD_NAME = "displayMessage";
    private static final String DE_TESTBED_SET_FOG_METHOD_NAME = "setFog";
    
    private static final double FOG_DURATION = 5.0;
    
    /** This is the window name of the DE Testbed application used by AHK to manipulate the window */
    private static final String WINDOW_TITLE = "DE Testbed";
    
    /**
     * Class constructor
     * 
     * @param name - the display name of the plugin
     */
    public DETestbedPluginXMLRPCInterface(String name){
        super(name, true); 
    }

    @Override
    public boolean configure(Serializable config) throws ConfigurationException {
        
        if(isAvailable(false)){
            
            // Get DE_TESTBED_HOME environment variable
            String deTestbedHome = System.getenv(TrainingApplicationInstallPage.DE_TESTBED_HOME);
            if(deTestbedHome != null){
                //check if the start script exists at that location
                DE_TESTBED_BAT =
                        new File(deTestbedHome + File.separator + TrainingApplicationInstallPage.DE_TESTBED_HOME_BATCH);
                if(!DE_TESTBED_BAT.exists()){
                    
                    if (!GatewayModuleProperties.getInstance().isRemoteMode()) {
                        throw new ConfigurationException("Unable to find the 'DE Testbed' executable of\n "+DE_TESTBED_BAT+".",
                        		"If the 'DE Testbed' program is installed, make sure you run the GIFT installer and \nprovide the necessary information for the DE Testbed option.\n\n" +
                        		"Would you like to continue by making the '"+getName()+"' interface unavailable to use?\n"+
                        		"(this automatically updates default.interopConfig.xml and require you to use the GIFT installer to enable it in the future)\n\n" +
                        		"Otherwise install the 'DE Testbed' program and configure GIFT by using the GIFT installer.",
                        		null);
                    }else{
                        logger.info("DE Testbed was not found on this computer after looking at the '"+TrainingApplicationInstallPage.DE_TESTBED_HOME+"' environment variable value of '"+deTestbedHome+"'."+
                                " A message should be displayed about this to the user in the JWS GW installer.");
                    }
                }
            }else{
                
                if(!GatewayModuleProperties.getInstance().isRemoteMode()){
                    throw new ConfigurationException("The environment variable '"+TrainingApplicationInstallPage.DE_TESTBED_HOME+"' was not found.",
                            "If the 'DE Testbed' program is installed, make sure you run the GIFT installer and \nprovide the necessary information for the 'DE Testbed' option.\n"+
                            "Note: if the problem persists, try restarting Windows to make sure the changes were applied.\n\n"+
                            "Would you like to continue by making the '"+getName()+"' interface unavailable to use?\n"+
                            "(this automatically updates default.interopConfig.xml and requires you to use the GIFT installer to enable it in the future)\n\n" +
                            "Otherwise install the 'DE Testbed' program and configure GIFT by using the GIFT installer.",
                            null);
                }else{
                    logger.info("DE Testbed was not found on this computer because the environment variable '"+TrainingApplicationInstallPage.DE_TESTBED_HOME+"' was not found.  A message should be displayed about this to the user in the JWS GW installer.");
                }
            }
        
            // Configure for XMLRPC
            if (config instanceof generated.gateway.XMLRPC) {

                generated.gateway.XMLRPC pluginConfig = (generated.gateway.XMLRPC) config;
                try{
                    client = ApplicationConnectionFactory.createXMLRPCClient(pluginConfig.getExternalServerNetworkAddress(), pluginConfig.getExternalServerNetworkPort());
                }catch(Exception e){
                    throw new ConfigurationException("Failed to create the XML RPC client.", e.getMessage(), e);
                }
                
                logger.info("Plugin has been configured");

            } else {
                throw new ConfigurationException(getName()+" Plugin interface can't configure.",
                        getName()+" plugin interface uses "+generated.gateway.XMLRPC.class+" interop input and doesn't support using the interop config instance of " + config,
                        null);
            }
        }
        
        return false;
    }
    
    /**
     * Retrieve the testbed batch file.
     * 
     * @return will return null if the batch file could not be found by using the testbed home environment variable.
     */
    private File getTestbedBatch(){
        
        File testbedBatchFile = null;
        
        String deTestbedHome = System.getenv(TrainingApplicationInstallPage.DE_TESTBED_HOME);
        if(deTestbedHome != null){
            //check if the start script exists at that location
            testbedBatchFile = new File(deTestbedHome + File.separator + TrainingApplicationInstallPage.DE_TESTBED_HOME_BATCH);
            if(!testbedBatchFile.exists()){
                testbedBatchFile = null;
            }
        }
        
        return testbedBatchFile;
    }

    @Override
    public List<MessageTypeEnum> getSupportedMessageTypes() {
        return supportedMsgTypes;
    }
    
    /**
     * Launches the DE Testbed process.
     */
    private void launchDeTestbedProcess() {
        
        if(DE_TESTBED_BAT == null){
            throw new DetailedException("Failed to launch DE Testbed application.", "The path to the DE Testbed launch script was not set.", null);
        }
        
        // Start DE Testbed process
        ProcessBuilder pBuilder = new ProcessBuilder(DE_TESTBED_BAT.getAbsolutePath());
        pBuilder.directory(new File(System.getenv(TrainingApplicationInstallPage.DE_TESTBED_HOME)));
        pBuilder.redirectErrorStream(true);
        
        try {
            deTestbedProcess = pBuilder.start();
        } catch (IOException ioe) {

            logger.error("Caught exception when trying to start the DE Testbed.", ioe);
            throw new RuntimeException("There was a problem launching the DE Testbed.");
        }
        
        // Read the DE Testbed output until the "GIFT PLUGIN INITIALIZED" string is found
        BufferedReader reader = new BufferedReader(new InputStreamReader(deTestbedProcess.getInputStream()));
        String line;
        try {
            while ((line = reader.readLine()) != null) {

                if(line.equals("GIFT PLUGIN INITIALIZED")) {
                    
                    break;
                }
            }
        } catch (IOException ioe) {

            logger.error("Caught exception when trying to parse the DE Testbed initialization sequence.", ioe);
            throw new RuntimeException("There was a problem parsing the DE Testbed intitialization sequence.");
        }
    }
    
    @Override
    public void setEnabled(boolean value) throws ConfigurationException{
        
        boolean isEnabledAlready = isEnabled();
        super.setEnabled(value);
        
        if(!isEnabledAlready && value) {

            //make sure the testbed batch was found
            if(DE_TESTBED_BAT == null){
                DE_TESTBED_BAT = getTestbedBatch();
            }
            
            launchDeTestbedProcess();
        }
        else if(isEnabledAlready && !value)
        {
            // Potentially destroy DE Testbed process
            if(deTestbedProcess != null)
            {
                try {
                    deTestbedProcess.exitValue();
                } catch (IllegalThreadStateException itse) {
                    logger.info("Destroying DE Testbed process.", itse);
                    deTestbedProcess.destroy();
                }
                
                deTestbedProcess = null;
            }
        }
    }

    @Override
    public boolean handleGIFTMessage(Message message, StringBuilder errorMsg) {

        // Below is some arbitrary handling of GIFT messages.
        // In most instances your interop plugin will need to handle SIMAN messages in order to 
        // allow GIFT to synchronize with the DE Testbed.
        // Look at the other interop plugins for more examples of handling GIFT messages.
        
        if (message.getMessageType() == MessageTypeEnum.SIMAN) {

            Siman siman = (Siman) message.getPayload();
            
            if (siman.getSimanTypeEnum() == SimanTypeEnum.LOAD) { 
                
                //Parse the specific load arguments authored in the course for this interop plugin
                generated.course.InteropInputs interopInputs = getLoadArgsByInteropImpl(this.getClass().getName(), siman.getLoadArgs());
                generated.course.DETestbedInteropInputs inputs = (generated.course.DETestbedInteropInputs) interopInputs.getInteropInput();
                
                generated.course.DETestbedInteropInputs.LoadArgs args = inputs.getLoadArgs();

                try{
                    loadScenario(args.getScenarioName());
                    logger.info("Loaded DE Testbed scenario  '" + args.getScenarioName() +"'.");
                }catch(DetailedException e){
                    errorMsg.append(e.getReason()).append(" because ").append(e.getDetails());
                }
            
            } else if (siman.getSimanTypeEnum() == SimanTypeEnum.START) {
                //nothing to do
            
            } else if (siman.getSimanTypeEnum() == SimanTypeEnum.PAUSE) {
                
                try{
                    GatewayModuleUtils.minimizeWindow(GatewayModuleUtils.AHK_CLASS_WILDCARD, WINDOW_TITLE);
                }catch(ConfigurationException e){
                    logger.error("Failed to minimize the DE Testbed window.", e);
                }
                
                Object returnObj = client.callMethod(DE_TESTBED_PAUSE_METHOD_NAME, new Vector<>(0), errorMsg);       
                logger.info("Receive return value of "+returnObj+" from XML-RPC call for method named '" + DE_TESTBED_PAUSE_METHOD_NAME +"'.");
            } else if (siman.getSimanTypeEnum() == SimanTypeEnum.RESUME) {
                
                try{
                    GatewayModuleUtils.setAlwaysOnTop(GatewayModuleUtils.AHK_CLASS_WILDCARD, WINDOW_TITLE, true);                
                    GatewayModuleUtils.giveFocus(GatewayModuleUtils.AHK_CLASS_WILDCARD, WINDOW_TITLE);
                }catch(ConfigurationException e){
                    logger.error("Failed to set the DE Testbed window as the foreground window.", e);
                }

                Object returnObj = client.callMethod(DE_TESTBED_RESUME_METHOD_NAME, new Vector<>(0), errorMsg);       
                logger.info("Receive return value of "+returnObj+" from XML-RPC call for method named '" + DE_TESTBED_RESUME_METHOD_NAME +"'.");
            } else if (siman.getSimanTypeEnum() == SimanTypeEnum.STOP) {
                
                Object returnObj = client.callMethod(DE_TESTBED_STOP_METHOD_NAME, new Vector<>(0), errorMsg);       
                logger.info("Receive return value of "+returnObj+" from XML-RPC call for method named '" + DE_TESTBED_STOP_METHOD_NAME +"'.");
            }
            
        }else if(message.getMessageType() == MessageTypeEnum.DISPLAY_FEEDBACK_GATEWAY_REQUEST){            
            //send some feedback text to the DE Testbed training application
            
            String text = (String)message.getPayload();
            client.callMethod(DE_TESTBED_DISPLAY_MESSAGE_METHOD_NAME, text, errorMsg);
            
        }else if(message.getMessageType() == MessageTypeEnum.ENVIRONMENT_CONTROL){
            //send the control information to the training application

            double fogFraction = 0.0;

            EnvironmentControl control = (EnvironmentControl)message.getPayload();
            generated.dkf.EnvironmentAdaptation type = control.getEnvironmentStatusType();
            if(type.getType() instanceof generated.dkf.EnvironmentAdaptation.Fog){
                
                generated.dkf.EnvironmentAdaptation.Fog fog = (generated.dkf.EnvironmentAdaptation.Fog)type.getType();
                fogFraction = fog.getDensity().doubleValue();

            }else{
                return false;
            }
            
            Vector<Double> fogArgs = new Vector<Double>();
            fogArgs.add(fogFraction);
            fogArgs.add(FOG_DURATION);
            client.callMethod(DE_TESTBED_SET_FOG_METHOD_NAME, fogArgs, errorMsg);
            
            logger.info("Setting fog level to "+fogFraction+".");
            
        }else{
            logger.error("Received unhandled GIFT message to send over to the "+getName()+" plugin, " + message);
            
            errorMsg.append(getName()).append(" plugin can't handle message of type ").append(message.getMessageType());
        }

        return false;
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

    /**
     * The scenario identifier is the name of a scenario hosted by the DE Testbed external application.
     */
    @Override
    public void loadScenario(String scenarioIdentifier)
            throws DetailedException {
        
        StringBuilder errorMsg = new StringBuilder();
        client.callMethod(DE_TESTBED_LOAD_METHOD_NAME, scenarioIdentifier, errorMsg);   
        
        if(errorMsg.length() > 0){
            throw new DetailedException("Failed to load the DE Testbed scenario named '"+scenarioIdentifier+"'.", 
                    "There was an error when trying to load the scenario:\n"+errorMsg.toString(), null);
        }
    }

    @Override
    public void cleanup() {
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
