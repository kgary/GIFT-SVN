/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.interop.vbsplugin;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.ActorTypeCategoryEnum;
import generated.dkf.EnvironmentAdaptation;
import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.LoSQuery;
import mil.arl.gift.common.Siman;
import mil.arl.gift.common.enums.ErrorEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.SimanTypeEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.ta.request.EnvironmentControl;
import mil.arl.gift.common.ta.request.VariablesStateRequest;
import mil.arl.gift.common.ta.request.VariablesStateRequest.VARIABLE_TYPE;
import mil.arl.gift.common.ta.request.VariablesStateRequest.VariableInfo;
import mil.arl.gift.common.ta.state.GenericJSONState;
import mil.arl.gift.common.ta.state.LoSResult;
import mil.arl.gift.common.ta.state.LoSResult.VisibilityResult;
import mil.arl.gift.common.ta.state.StartResume;
import mil.arl.gift.common.ta.state.StopFreeze;
import mil.arl.gift.common.ta.state.VariablesState;
import mil.arl.gift.common.ta.state.VariablesState.VariableNumberState;
import mil.arl.gift.common.ta.state.VariablesState.VariableState;
import mil.arl.gift.common.ta.state.VariablesState.WeaponState;
import mil.arl.gift.common.ta.state.VariablesStateResult;
import mil.arl.gift.common.util.JOptionPaneUtil;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.gateway.GatewayModule;
import mil.arl.gift.gateway.GatewayModuleUtils;
import mil.arl.gift.gateway.installer.TrainingApplicationInstallPage;
import mil.arl.gift.gateway.interop.AbstractInteropInterface;
import mil.arl.gift.gateway.interop.ApplicationConnectionFactory;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.NACK;
import mil.arl.gift.net.socket.SocketHandler;

/**
 * This is the VBS interop interface responsible for communicating with VBS using its scripting language and a VBS
 * dll loaded when the VBS application is started.  The dll (i.e. plugin) communicates commands to and from this class.
 * 
 * @author jleonard
 */
public class VBSPluginInterface extends AbstractInteropInterface {

    private static final boolean RETRY_ON_FAIL = true;    
    
    /** Instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(VBSPluginInterface.class);

    /** 
     * This is the AHK class name of the VBS Application used to manipulate the window
     * Refer to {@link GatewayModuleUtils.setAlwaysOnTop} for obtaining AHK class name
     */
    /** VBS3 v 19.1.6.6 - has a unique version name */
    private static final String AHK_CLASS_VBS3_19 = "VBS3 19.1.6.6";
    
    /** VBS3 v 3.9.2 - has a general name */
    private static final String AHK_CLASS_VBS3_3_9_2 = "VBS3"; 
    
    /** VBS2 */
    private static final String AHK_CLASS_VBS2 = "VBS2";
    
    /** 
     * This is the auto hot key class name as provided by AutoHot Key application and must
     * match the AHK class for the VBS version being used. </br> 
     * Default to null so the following is not applied when the author chooses to not have GIFT manage VBS window/menu:</br>
     * 1. AutoHotKey to manipulate window foreground/background</br>
     * 2. sending window/menu management messages to VBS (e.g. Escape until reaching VBS main menu)</br>
     * 3. no loading or unloading scenarios in VBS</br>
     * </br>
     * The various VBS engine queries will still happen (e.g. line of sight).
     */
    private String ahkClass = null;
    
    /** 
     * VBS command key terms 
     */
    //used to execute an AHK script to bring VBS into the foreground
    private static final String FOCUS_AHK_COMMAND = "FocusVBS";
    //used to execute an AHK script to hit the escape keyboard button to pause VBS (this doesn't pause the simulation clock)
    private static final String PAUSE_AHK_COMMAND = "PauseVBS";
    //used to execute a VBS scripting command to pause the VBS simulation clock
    private static final String PAUSE_COMMAND = "PauseSimulation true";
    //used to execute an AHK script to hit the escape keyboard button to un-pause VBS (only executed if VBS is paused)
    private static final String RESUME_AHK_COMMAND = "ResumeVBS";
    //used to execute a VBS scripting command to un-pause the VBS simulation clock (only executed if VBS is paused)
    private static final String RESUME_COMMAND = "PauseSimulation false";
    //used to execute a VBS scripting command to end the VBS scenario
    private static final String END_SCENARIO_COMMAND = "endMission \"end1\""; 
    //used to test if the connection to the GIFT VBS plugin is valid
    private static final String STATUS = "ping"; 
    //used to get the VBS version number
    private static final String VERSION_NUMBER_COMMAND = "VersionNumber";
    // used to get the VBS product version 
    private static final String VERSION_PRODUCT_COMMAND = "productVersion";
    // used to get the scenario name
    private static final String MISSION_NAME_COMMAND = "missionName";
    // used to get the terrain display name
    private static final String TERRAIN_NAME_COMMAND = "mapproperties select 1";
    
    /**
     * contains the types of GIFT messages this interop interface can consume from GIFT modules
     * and handle or send to some external training application
     */
    private static List<MessageTypeEnum> supportedMsgTypes;    
    static{
        supportedMsgTypes = new ArrayList<>();
        supportedMsgTypes.add(MessageTypeEnum.SIMAN);
        supportedMsgTypes.add(MessageTypeEnum.ENVIRONMENT_CONTROL);
        supportedMsgTypes.add(MessageTypeEnum.LOS_QUERY);
        supportedMsgTypes.add(MessageTypeEnum.VARIABLE_STATE_REQUEST);
        supportedMsgTypes.add(MessageTypeEnum.DISPLAY_FEEDBACK_GATEWAY_REQUEST);
    }
    
    /**
     * contains the list of GIFT messages (e.g. Entity State) that this interop plugin interface
     * can create and send to GIFT modules (e.g. Domain module) after consuming some relevant information from 
     * an external training applications (e.g. VBS). 
     */
    private static List<MessageTypeEnum> PRODUCED_MSG_TYPES;
    static{
        PRODUCED_MSG_TYPES = new ArrayList<>();
        PRODUCED_MSG_TYPES.add(MessageTypeEnum.STOP_FREEZE);
        PRODUCED_MSG_TYPES.add(MessageTypeEnum.START_RESUME);
        PRODUCED_MSG_TYPES.add(MessageTypeEnum.LOS_RESULT);
    }
    
    /**
     * contains the training applications that this interop plugin was built for and should connect to
     */
    private static List<TrainingApplicationEnum> REQ_TRAINING_APPS;
    static{
        REQ_TRAINING_APPS = new ArrayList<>();
        REQ_TRAINING_APPS.add(TrainingApplicationEnum.VBS); 
    }
    
    /**
     * default values for required vbs scripting commands
     */
    private static final String DEFAULT_ADAPTATION_DURATION = "5";
    private static final String DEFAULT_FOG_RED_COLOR = "1";
    private static final String DEFAULT_FOG_GREEN_COLOR = "1";
    private static final String DEFAULT_FOG_BLUE_COLOR = "1";
    private static final int DEFAULT_DUSK = 18;
    private static final int DEFAULT_DAWN = 8;
    private static final int DEFAULT_MIDDAY = 12;
    private static final int DEFAULT_MIDNIGHT = 0;

    /** the socket connection */
    private SocketHandler socketHandler = null;
    
    /** flag used to indicate whether VBS is currently the foreground window due to an AHK script */
    private boolean isVBSForeground = false;
    
    /** configuration parameters for the connection to the GIFT VBS plugin */
    private generated.gateway.VBS vbsPluginConfig = null;
    
    /**
     * Class constructor
     * 
     * @param name - the display name of the plugin
     */
    public VBSPluginInterface(String name) {
        super(name, true);
    }
    
    @Override
    public boolean isAvailable(boolean testInterop){
        
        if(super.isAvailable(testInterop)){
        
            if(testInterop){            
                //test connection to GIFT VBS plugin
                
                StringBuilder errorMsg = new StringBuilder();
                
                if(logger.isInfoEnabled()){
                    logger.info("Checking if the GIFT VBS plugin is active, i.e. is the socket connection valid and available.");
                }
                
                // make sure there is a connection
                boolean madeConnection = false;
                if(socketHandler == null || !socketHandler.isConnected()){
                    try {
                        createSocketHandler();
                        socketHandler.connect();
                        madeConnection = true;
                    } catch (IllegalArgumentException | IOException e) {
                        socketHandler = null;  //because the socket failed to connect, start over by setting the handler to null 
                        throw new ConfigurationException("Failed to connect the VBS Plugin interface socket connection.", 
                                "There was an exception while trying to connect:\n"+e.getMessage(), e);
                    }
                }
                
                boolean checkAgain = false;
                do{
                    sendCommand(STATUS, errorMsg, false);
                                     
                    if(errorMsg.length() > 0){
                        //there was a problem checking the status
                        logger.error("There was a problem checking the status of the GIFT VBS plugin: '"+errorMsg+"'.");  
                        
                        if(logger.isInfoEnabled()){
                            logger.info("Asking the user to start VBS at this time before failing this check.");                       
                        }
                        
                        // Calling the JOptionPaneUil class to force the dialog to the front since we don't have a
                        // parent JFrame to anchor to.
                        // Attempting to allow the user to start VBS
                        int choice = JOptionPaneUtil.showConfirmDialog(
                                "GIFT is trying to connect to the VBS Training Application via the GIFT VBS plugin.\n"
                                + "Please start VBS using the GIFT provided VBS shortcut.  If you already did this \n"
                                + "are you sure that the GIFT installer copied the appropriate files to your VBS directory?\n\n"
                                + "To help verify this, check for '<vbs-install-path>\\plugins\\GIFTVBSPlugin.dll'.\n"
                                + "If that file doesn't exist, you will need to force a re-install\n"
                                + "by first deleting the '"+TrainingApplicationInstallPage.VBS_HOME+"' environment variable (for now).\n\n"
                                + "Press the OK button when VBS reaches the Main Menu to have GIFT check again.\n\n"
                                + "Pressing the Cancel button will most likely end the current course.", 
                                "Unable to connect to VBS", 
                                JOptionPane.OK_CANCEL_OPTION, 
                                JOptionPane.WARNING_MESSAGE);
            
                       
                        
                        if(choice == JOptionPane.CANCEL_OPTION){
                            //user canceled connection test, therefore this interop will not be available
                            if(logger.isInfoEnabled()){
                                logger.info("The user decided to cancel the VBS connection check.");
                            }
                            return false;
                        }
                        
                        
                    }else{
                        //found VBS!
                        checkAgain = false;
                    }
                    
                }while(checkAgain);
                
                //disconnect the connection that was made here
                if(madeConnection){
                    
                    if(logger.isInfoEnabled()){
                        logger.info("Successfully tested the VBS plugin interface because a connection was made to the GIFT VBS plugin");
                    }
                    
                    try {
                        socketHandler.disconnect();
                    } catch (@SuppressWarnings("unused") IOException e) {
                        //not sure if we care at this point
                    }
                }
                
            }
        }
        
        return super.isAvailable(testInterop);
        
    }


    @Override
    public boolean configure(Serializable config) throws ConfigurationException {
        
        if (config instanceof generated.gateway.VBS) {
    
            this.vbsPluginConfig = (generated.gateway.VBS) config;
            createSocketHandler();
            
            if(logger.isInfoEnabled()){
                logger.info("Plugin has been configured");
            }

        } else {
            throw new ConfigurationException("VBS Plugin interface can't configure.",
                    "The VBS Plugin interface only uses the interop config type of "+generated.gateway.VBS.class+" and doesn't support using the interop config instance of " + config,
                    null);
        }
        
        return false;
    }
    
    /**
     * Creates the socket connection if there are configuration parameters and the socket handler in this class is null.
     */
    private void createSocketHandler(){
        
        if(vbsPluginConfig != null){
            
            if(socketHandler == null){
                socketHandler = ApplicationConnectionFactory.createSocket(vbsPluginConfig.getNetworkAddress(), vbsPluginConfig.getNetworkPort());
                
                if(logger.isInfoEnabled()){
                    logger.info("VBS plugin interface socket handler created.");
                }
            }
        }
    }
    
    @Override
    public boolean handleGIFTMessage(Message message, StringBuilder errorMsg) throws ConfigurationException {

        boolean replySent = false;
        
        if (message.getMessageType() == MessageTypeEnum.SIMAN) {

            Siman siman = (Siman) message.getPayload();
            replySent = handleSIMANMessage(message, siman, errorMsg);            

        } else if (message.getMessageType() == MessageTypeEnum.ENVIRONMENT_CONTROL) {

            EnvironmentControl control = (EnvironmentControl)message.getPayload();
            applyEnvironmentControl(control, message, errorMsg);
            
        }else if (message.getMessageType() == MessageTypeEnum.LOS_QUERY) {

            replySent = doLosQuery((DomainSessionMessage)message, errorMsg);
            
        }else if(message.getMessageType() == MessageTypeEnum.VARIABLE_STATE_REQUEST){
            
            replySent = doVariablesStateQuery((DomainSessionMessage)message, errorMsg);
            
        }else if (message.getMessageType() == MessageTypeEnum.DISPLAY_FEEDBACK_GATEWAY_REQUEST){
            
            String text = (String)message.getPayload();
            sendCommand("hint \""+text+ "\";", message, errorMsg);
            
        }else {
            logger.error("Received unhandled GIFT message to send over to the VBS plugin, " + message);            
            errorMsg.append("VBS plugin can't handle message of type ").append(message.getMessageType());
        }
        
        return replySent;
    }
    
    /**
     * Handle a GIFT Simulation management (SIMAN) message by applying the appropriate logic based on the type.
     * 
     * @param message the full GIFT message containing the SIMAN payload
     * @param siman the siman payload with specific type of action to take
     * @param errorMsg used to keep track of error messages caused by this method
     * @return true if this method sent a response GIFT message to the sender of the GIFT SIMAN message being handled.
     */
    private boolean handleSIMANMessage(Message message, Siman siman, StringBuilder errorMsg){
        
        boolean replySent = false;
        
        if (siman.getSimanTypeEnum() == SimanTypeEnum.LOAD) {  

            //
            // get appropriate configuration info for loading
            //
            generated.course.InteropInputs interopInputs = getLoadArgsByInteropImpl(this.getClass().getName(), siman.getLoadArgs());                
            generated.course.VBSInteropInputs inputs = (generated.course.VBSInteropInputs) interopInputs.getInteropInput();

            String scenarioName = inputs.getLoadArgs().getScenarioName();

            if (scenarioName != null) {
                // the scenario name element was created and should be not empty to be considered valid at this point
                // (i.e. doesn't mean the scenario name value is an actual scenario that will load successfully yet)
                
                if(scenarioName.isEmpty()){
                    GatewayModule.getInstance().sendReplyMessageToGIFT(
                            message,
                            new NACK(ErrorEnum.MALFORMED_DATA_ERROR, "The VBS Interop connection could not load the scenario, no scenario name was defined"),
                            MessageTypeEnum.PROCESSED_NACK,
                            this);
                    
                    replySent = true;
                }else{
                    
                    // Make sure the AHK_CLASS references the right version of VBS
                    // The 'VersionNumber' command returns an array of integers : 
                    // - Prior to VBS3 V3.6: [Major, Minor, Build]
                    // - Since V3.6 : [Major, Minor, hotfix, build], e.g. [3,9,2,146851], [19,1,6,6]
                    // The product version command returns an Array - [full product name, short product name, version*100, build number]
                    // e.g. Might return ["VBS2 2.05","VBS2",205,96802], ["VBS3 3.9.2","VBS3",309,146851], ["VBS319.1.6.6","VBS3",1901,6]
                    String versionNumberRawResult = sendCommand(VERSION_NUMBER_COMMAND, errorMsg, false);
                    String productVersionRawResult = sendCommand(VERSION_PRODUCT_COMMAND, errorMsg, false);
                    try{
                        // determine VBS2 or VBS3 - remove quotes around each value in the array string, split into arguments
                        String regex = "[\\[\"\\]]";
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(productVersionRawResult);
                        String[] args = matcher.replaceAll("").split(",");
                        if(args.length > 1){
                            String shortProductName = args[1];
                            if("VBS3".equals(shortProductName)){
                                // determine which version of VBS3
                                
                                String fullProductName = args[0];
                                if("VBS319.1.6.6".equals(fullProductName)){
                                    ahkClass = AHK_CLASS_VBS3_19;
                                }else{
                                    // the default (for Army, what is available on milgaming site)
                                    ahkClass = AHK_CLASS_VBS3_3_9_2;
                                }
                            }else{
                                // assume VBS2
                                ahkClass = AHK_CLASS_VBS2;
                            }
                        }
                        
                        if(logger.isDebugEnabled()){
                            logger.debug("Connected to VBS product "+productVersionRawResult+" [full product name, short product name, version*100, build number], number "+versionNumberRawResult+" [Major, Minor, (hotfix), Build]");
                        }
                    }catch(NullPointerException | StringIndexOutOfBoundsException e){
                        //We noticed a NPE exception in the gateway log file at this point but not sure
                        //how it got here if the dialog in the isAvailable dialog method appeared because
                        //VBS was not configured for GIFT.
                        //Also noticed an index out of bounds exception when a bad response is received
                        JOptionPaneUtil.showConfirmDialog(
                                "The GIFT VBS plugin didn't respond correctly to either the version number request (response = "+versionNumberRawResult+") or the product version request (response = "+productVersionRawResult+").\n\n"+
                                        "Did the GIFT installer copy the appropriate files to your VBS directory automatically?\n\n"+
                                        "To help verify this check for '<vbs-install-path>\\plugins\\GIFTVBSPlugin.dll'.\n"+
                                        "If you re-installed VBS after installing GIFT with VBS, you will need to force a re-install\n"+
                                        "by first deleting the '"+TrainingApplicationInstallPage.VBS_HOME+"' environment variable (for now).", 
                                "Problem Communicating with GIFT VBS plugin", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                        throw new ConfigurationException("Failed to apply the load command",
                                "The GIFT VBS plugin didn't respond correctly to the '"+VERSION_NUMBER_COMMAND+"' or '"+VERSION_PRODUCT_COMMAND+"' request.  VBS is most likely not configured to work with GIFT correctly.",
                                e);
                    }

                    try{
                        loadScenario(scenarioName);
                        if(logger.isInfoEnabled()){
                            logger.info("Loaded VBS scenario  '" + scenarioName +"'.");
                        }
                    }catch(DetailedException e){
                        errorMsg.append(e.getReason()).append(" because ").append(e.getDetails());
                    }

                    if (logger.isInfoEnabled()) {    
                        logger.info("hostmission");
                    }
                }

            } else {
                // this means that GIFT won't be managing the scenario/menu, a configuration that is
                // used in RTA Lesson level and not specified option in the VBS course object.

                // nothing to do
            }
            

        } else if (siman.getSimanTypeEnum() == SimanTypeEnum.START) {
            
            if(ahkClass != null){
                try{
                    sendCommand(FOCUS_AHK_COMMAND, message, errorMsg);  
                }catch(DetailedException e){
                    
                    if(e.getCause() instanceof java.net.SocketTimeoutException){
                        //ignore - this was seen in VBS3 3.9.2 when it was still loading the VBS scenario ordered by GIFT (siman.load logic above)
                        //         the GIFT VBS plugin appears starved of cycles and therefore the response to the focusVBS request is not received
                        //         before the socket read timeout occurs.  This event shouldn't terminate the GIFT course.
                    }else{
                        throw e;
                    }
                }
            }
            
            GatewayModuleUtils.setAlwaysOnTop(ahkClass, true);
            isVBSForeground = true;
            
            GatewayModuleUtils.giveFocus(ahkClass);

            //Because VBS LVC game (v1.4) doesn't send a START_RESUME DIS PDU, create one of our own
            //TODO: this message won't make it to Domain module condition classes if the task must be activated by some entity state
            //      based trigger. 
            StartResume startResume = new StartResume(0, 0, 0);
            
            // start querying for VBS scenario metadata
            if(logger.isInfoEnabled()){
                logger.info("starting service to query for VBS mission metadata");
            }
            ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
            Runnable queryRunnable = new Runnable() {
                
                @Override
                public void run() {

                    boolean received = false;
                    try{
                        received = sendVBSScenarioMetadata();
                    }finally{
                        if(received){
                            if(logger.isInfoEnabled()){
                                logger.info("Shutting down VBS mission metadata query service");
                            }
                            scheduledExecutorService.shutdown();
                        }
                    }
                }
            };
            scheduledExecutorService.scheduleAtFixedRate(queryRunnable, 5, 10, TimeUnit.SECONDS);            
            
            DomainSessionMessage dsMsg = (DomainSessionMessage)message;
            GatewayModule.getInstance().sendMessageToGIFT(dsMsg.getUserSession(), dsMsg.getDomainSessionId(), dsMsg.getExperimentId(), startResume, MessageTypeEnum.START_RESUME, this);

        } else if (siman.getSimanTypeEnum() == SimanTypeEnum.PAUSE) {
            
            sendCommand(PAUSE_COMMAND, message, errorMsg);
            
            if(errorMsg.length() == 0){
                sendCommand(PAUSE_AHK_COMMAND, message, errorMsg);
            }
            
            //Because VBS LVC Game (v1.4) doesn't send a DIS STOP_FREEZE DIS PDU once the VBS scenario is paused, create one of our own
            StopFreeze stopFreeze = new StopFreeze(0, StopFreeze.RECESS, 0, 0);
            
            DomainSessionMessage dsMsg = (DomainSessionMessage)message;
            GatewayModule.getInstance().sendMessageToGIFT(dsMsg.getUserSession(), dsMsg.getDomainSessionId(), dsMsg.getExperimentId(), stopFreeze, MessageTypeEnum.STOP_FREEZE, this);

        } else if (siman.getSimanTypeEnum() == SimanTypeEnum.RESUME) {
            
            sendCommand(RESUME_COMMAND, message, errorMsg);
            
            if(errorMsg.length() == 0){
                sendCommand(RESUME_AHK_COMMAND, message, errorMsg);
            }
            
            GatewayModuleUtils.setAlwaysOnTop(ahkClass, true);
            isVBSForeground = true;
            
            GatewayModuleUtils.giveFocus(ahkClass);

            //Because VBS LVC game (v1.4) doesn't send a START_RESUME DIS PDU, create one of our own
            StartResume startResume = new StartResume(0, 0, 0);
            
            DomainSessionMessage dsMsg = (DomainSessionMessage)message;
            GatewayModule.getInstance().sendMessageToGIFT(dsMsg.getUserSession(), dsMsg.getDomainSessionId(), dsMsg.getExperimentId(), startResume, MessageTypeEnum.START_RESUME, this);

        } else if (siman.getSimanTypeEnum() == SimanTypeEnum.STOP) {
            
            GatewayModuleUtils.setAlwaysOnTop(ahkClass, false);
            isVBSForeground = false;
            
            try{
                loadScenario(null);
                if(logger.isInfoEnabled()){
                    logger.info("Clear VBS scenario");
                }
            }catch(DetailedException e){
                errorMsg.append(e.getReason()).append(" because ").append(e.getDetails());
            }
            
            //Because VBS LVC Game (v1.4) doesn't send a DIS STOP_FREEZE DIS PDU once the VBS scenario is stopped, create one of our own
            //TODO: The question is whether that PDU will reach ApplicationCompletedCondition
            StopFreeze stopFreeze = new StopFreeze(0, 0, 0, 0);
            
            DomainSessionMessage dsMsg = (DomainSessionMessage)message;
            GatewayModule.getInstance().sendMessageToGIFT(dsMsg.getUserSession(), dsMsg.getDomainSessionId(), dsMsg.getExperimentId(), stopFreeze, MessageTypeEnum.STOP_FREEZE, this);

        } else if (siman.getSimanTypeEnum() == SimanTypeEnum.RESTART) {

            //nothing to do
            
        } else{
            errorMsg.append("VBS plugin can't handle siman type of ").append(siman.getSimanTypeEnum());
            logger.error("Found unhandled Siman type of "+siman.getSimanTypeEnum());
        }
        
        return replySent;
    }

    /**
     * Apply the scenario adaptation using VBS scripting command.
     *
     * @param control contains parameters for the scenario adaptation (e.g. set
     *        fog to 0.9)
     * @param message the GIFT message to translate and communicate via this
     *        interop interface
     * @param errorMsg error message produced by the plugin handling this gift
     *        message
     */
    private void applyEnvironmentControl(EnvironmentControl control, Message message, StringBuilder errorMsg){

        EnvironmentAdaptation adaptation = control.getEnvironmentStatusType();
        Serializable type = adaptation.getType();

        if(type instanceof generated.dkf.EnvironmentAdaptation.Overcast) {

            generated.dkf.EnvironmentAdaptation.Overcast overcast = (generated.dkf.EnvironmentAdaptation.Overcast)type;

            double value = overcast.getValue().doubleValue();

            String duration = DEFAULT_ADAPTATION_DURATION;
            if(overcast.getScenarioAdaptationDuration() != null){
                duration = overcast.getScenarioAdaptationDuration().toString();
            }

            String commandStr = duration + " setOvercast " + Double.toString(value);
            if(logger.isDebugEnabled()){
                logger.debug("sending command to VBS plugin:\n"+commandStr);
            }

            sendCommand(commandStr, message, errorMsg);

        } else if(type instanceof generated.dkf.EnvironmentAdaptation.Fog) {

            generated.dkf.EnvironmentAdaptation.Fog fog = (generated.dkf.EnvironmentAdaptation.Fog)type;

            double density = fog.getDensity().doubleValue();

            String duration = DEFAULT_ADAPTATION_DURATION;
            if(fog.getScenarioAdaptationDuration() != null){
                duration = fog.getScenarioAdaptationDuration().toString();
            }

            String rColor = DEFAULT_FOG_RED_COLOR;
            String gColor = DEFAULT_FOG_GREEN_COLOR;
            String bColor = DEFAULT_FOG_BLUE_COLOR;
            if(fog.getColor() != null){

                generated.dkf.EnvironmentAdaptation.Fog.Color customColor = fog.getColor();
                
                //normalize incoming values [0-255] to [0-1] for VBS scripting
                double rColorDouble = customColor.getRed() / 255d;
                double gColorDouble = customColor.getGreen() / 255d;
                double bColorDouble = customColor.getBlue() / 255d;
                rColor = String.valueOf(rColorDouble);
                gColor = String.valueOf(gColorDouble);
                bColor = String.valueOf(bColorDouble);
            }

            String commandStr = duration + " setFog [" + Double.toString(density) + ", ["+rColor+","+gColor+","+bColor+"], 0.5]";
            if(logger.isDebugEnabled()){
                logger.debug("sending command to VBS plugin:\n"+commandStr);
            }

            sendCommand(commandStr, message, errorMsg);

        } else if(type instanceof generated.dkf.EnvironmentAdaptation.Rain) {

            generated.dkf.EnvironmentAdaptation.Rain rain = (generated.dkf.EnvironmentAdaptation.Rain)type;

            double value = rain.getValue().doubleValue();

            String duration = DEFAULT_ADAPTATION_DURATION;
            if(rain.getScenarioAdaptationDuration() != null){
                duration = rain.getScenarioAdaptationDuration().toString();
            }

            String commandStr = duration + "  setRain " + Double.toString(value);
            if(logger.isDebugEnabled()){
                logger.debug("sending command to VBS plugin:\n"+commandStr);
            }

            sendCommand(commandStr, message, errorMsg);

        } else if (type instanceof generated.dkf.EnvironmentAdaptation.TimeOfDay) {

            generated.dkf.EnvironmentAdaptation.TimeOfDay tod = (generated.dkf.EnvironmentAdaptation.TimeOfDay)type;

            int hourOfDayValue = DEFAULT_MIDNIGHT;
            if(tod.getType() instanceof generated.dkf.EnvironmentAdaptation.TimeOfDay.Dawn){
                hourOfDayValue = DEFAULT_DAWN;
            }else if(tod.getType() instanceof generated.dkf.EnvironmentAdaptation.TimeOfDay.Dusk){
                hourOfDayValue = DEFAULT_DUSK;
            }else if(tod.getType() instanceof generated.dkf.EnvironmentAdaptation.TimeOfDay.Midday){
                hourOfDayValue = DEFAULT_MIDDAY;
            }else if(tod.getType() instanceof generated.dkf.EnvironmentAdaptation.TimeOfDay.Midnight){
                hourOfDayValue = DEFAULT_MIDNIGHT;
            }

            try {

                //retrieve the current VBS simulate hour of day
                String currentSimulationDate = sendCommand("date", errorMsg, RETRY_ON_FAIL);
                currentSimulationDate = currentSimulationDate.substring(1, currentSimulationDate.length() - 1);
                String[] dateArray = currentSimulationDate.split(",");
                String currentHourOfDay = dateArray[3].trim();
                int currentHourOfDayValue = Integer.parseInt(currentHourOfDay);

                //calculate the amount of time to skip forward in vbs
                int skipTime = 0;
                int hourDiff = hourOfDayValue - currentHourOfDayValue;

                if(hourDiff > 0){
                    //desired hour is in the future (of the current day)
                    skipTime = hourDiff;

                }else if(hourDiff < 0){
                    //desired time is in the past (of the current day)
                    skipTime = hourDiff + 24;
                }

                String commandStr = "skipTime " + skipTime;
                if(logger.isDebugEnabled()){
                    logger.debug("sending command to VBS plugin:\n"+commandStr);
                }

                sendCommand(commandStr, message, errorMsg);

            } catch (Exception e) {
                logger.error("Caught an exception while changing the time of day", e);
            }

        }else if(type instanceof generated.dkf.EnvironmentAdaptation.CreateActors){

            generated.dkf.EnvironmentAdaptation.CreateActors createActors = (generated.dkf.EnvironmentAdaptation.CreateActors)type;

            generated.dkf.EnvironmentAdaptation.CreateActors.Side side = createActors.getSide();
            String sideStr = "civilian";
            if(side.getType() instanceof generated.dkf.EnvironmentAdaptation.CreateActors.Side.Blufor){
                sideStr = "west";
            }else if(side.getType() instanceof generated.dkf.EnvironmentAdaptation.CreateActors.Side.Opfor){
                sideStr = "east";
            }
            
            String actorType = createActors.getType();
            String actorName = createActors.getActorName();
            ActorTypeCategoryEnum typeCategory = createActors.getTypeCategory();
            boolean isVehicle = typeCategory != null && typeCategory.equals(ActorTypeCategoryEnum.VEHICLE);

            generated.dkf.Coordinate coordinate = createActors.getCoordinate();
            if(coordinate.getType() instanceof generated.dkf.AGL){

                generated.dkf.AGL agl = (generated.dkf.AGL)coordinate.getType();

                StringBuilder command = new StringBuilder();
                command.append("createCenter ").append(sideStr).append(";");
                command.append("_grp = createGroup ").append(sideStr).append(";");
                
                if(isVehicle) {
                    // use different script command for vehicle.
                    // Note: createVehicle also seems to work for lifeform/person
                    
                    command.append("_giftVehicle = createVehicle [\"").append(actorType).append("\", [").append(agl.getX()).append(Constants.COMMA).append(agl.getY()).append(Constants.COMMA).append(agl.getElevation()).append("], [], 0, \"CAN_COLLIDE\"];");
                    
                    if(StringUtils.isBlank(actorName)) {
                        // give a default name to all gift created vehicles in VBS which could be useful
                        // for a command that removes all gift created vehicles
                        // Note: some characters are not valid (e.g. -)
                        actorName = "GIFTVehicle";
                    }                    

                    command.append("_giftVehicle setVehicleVarName \"").append(actorName).append("\";");
                    
                    if(createActors.getHeading() != null) {
                        int heading = createActors.getHeading().getValue();
                        command.append("_giftVehicle setDir ").append(heading).append(";");
                    }
                    
                }else {
                    // for all other types (e.g. person)
                    command.append("_giftUnit = _grp createUnit [\"").append(actorType).append("\", [").append(agl.getX()).append(Constants.COMMA).append(agl.getY()).append(Constants.COMMA).append(agl.getElevation()).append("], [], 0, \"CAN_COLLIDE\", true, true];");
    
                    if(StringUtils.isBlank(actorName)) {
                        // give a default name to all gift created units in VBS which could be useful
                        // for a command that removes all gift created units
                        // Note: some characters are not valid (e.g. -)
                        actorName = "GIFTUnit";
                    } 
                    
                    // although documentation has you believe you should use setUnitName for Units, that doesn't actually work
                    command.append("_giftUnit setVehicleVarName \"").append(actorName).append("\";");
                    
                    if(createActors.getHeading() != null) {
                        int heading = createActors.getHeading().getValue();
                        command.append("_giftUnit setDir ").append(heading).append(";");
                    }
                }

                String commandStr = command.toString();
                if(logger.isDebugEnabled()){
                    logger.debug("sending command to VBS plugin:\n"+commandStr);
                }

                sendCommand(commandStr, message, errorMsg);
            }else{
                errorMsg.append("Received unhandled coordinate type of ").append(coordinate.getType()).append(" for VBS create actors scenario adaptation request.");
                return;
            }
            
        }else if(type instanceof generated.dkf.EnvironmentAdaptation.RemoveActors){
            
            generated.dkf.EnvironmentAdaptation.RemoveActors removeActors = (generated.dkf.EnvironmentAdaptation.RemoveActors)type;
            
            StringBuilder command = new StringBuilder();
            command.append("{ if(");
            
            Serializable actorId = removeActors.getType();
            if(actorId instanceof generated.dkf.EnvironmentAdaptation.RemoveActors.Location){
                //currently not supported
                errorMsg.append("Received unhandled remove actor identification type of Location for VBS remove actors scenario adaptation request.  Please use actor name instead.");
                return;
            }else if(actorId instanceof String){

                String vehicleName = (String) actorId;
                command.append("\"").append(vehicleName).append("\" in vehicleVarName _x");
            }

            command.append(") then { deleteVehicle _x; };}foreach ");
            
            // while 'deleteVehicle' works for both people and vehicles, there is a different command for getting all of a particular type
            ActorTypeCategoryEnum typeCategory = removeActors.getTypeCategory();
            boolean isVehicle = typeCategory != null && typeCategory.equals(ActorTypeCategoryEnum.VEHICLE);
            if(isVehicle) {
                command.append("allVehicles;");
            }else {
                command.append("allUnits;");
            }

            String commandStr = command.toString();
            if(logger.isDebugEnabled()){
                logger.debug("sending command to VBS plugin:\n"+commandStr);
            }

            sendCommand(commandStr, message, errorMsg);

        }else if(type instanceof generated.dkf.EnvironmentAdaptation.Endurance){

            generated.dkf.EnvironmentAdaptation.Endurance endurance = (generated.dkf.EnvironmentAdaptation.Endurance)type;
            
            // if entity marking is not specified default to 'player' a keyword in VBS
            String entityMarking;
            if(endurance.getTeamMemberRef() != null && StringUtils.isNotBlank(endurance.getTeamMemberRef().getEntityMarking())){
                entityMarking = endurance.getTeamMemberRef().getEntityMarking();
            }else{
                entityMarking = "player";
            }

            //FYI.  I have yet to see this change anything useful in VBS
            double value = endurance.getValue().doubleValue();

            String commandStr = entityMarking + " setEndurance "+value;
            if(logger.isDebugEnabled()){
                logger.debug("sending command to VBS plugin:\n"+commandStr);
            }

            sendCommand(commandStr, message, errorMsg);

        }else if(type instanceof generated.dkf.EnvironmentAdaptation.FatigueRecovery){

            generated.dkf.EnvironmentAdaptation.FatigueRecovery fatigue = (generated.dkf.EnvironmentAdaptation.FatigueRecovery)type;
            
            // if entity marking is not specified default to 'player' a keyword in VBS
            String entityMarking;
            if(fatigue.getTeamMemberRef() != null && StringUtils.isNotBlank(fatigue.getTeamMemberRef().getEntityMarking())){
                entityMarking = fatigue.getTeamMemberRef().getEntityMarking();
            }else{
                entityMarking = "player";
            }

            //FYI.  I have yet to see this change anything useful in VBS
            double value = fatigue.getRate().doubleValue();

            String commandStr = entityMarking + " setFatigueRecoveryRate "+value;
            if(logger.isDebugEnabled()){
                logger.debug("sending command to VBS plugin:\n"+commandStr);
            }

            sendCommand(commandStr, message, errorMsg);

        }else if(type instanceof generated.dkf.EnvironmentAdaptation.Teleport){

            generated.dkf.EnvironmentAdaptation.Teleport teleport = (generated.dkf.EnvironmentAdaptation.Teleport)type;

            // if entity marking is not specified default to 'player' a keyword in VBS
            String entityMarking;
            if(teleport.getTeamMemberRef() != null && StringUtils.isNotBlank(teleport.getTeamMemberRef().getEntityMarking())){
                entityMarking = teleport.getTeamMemberRef().getEntityMarking();
            }else{
                entityMarking = "player";
            }
            
            StringBuilder command = new StringBuilder();

            generated.dkf.Coordinate coordinate = teleport.getCoordinate();
            if(coordinate.getType() instanceof generated.dkf.AGL){

                generated.dkf.AGL agl = (generated.dkf.AGL)coordinate.getType();
                command.append(entityMarking).append(" setPos [").append(agl.getX()).append(Constants.COMMA).append(agl.getY()).append("];");

                if(teleport.getHeading() != null){
                    command.append(entityMarking).append(" setDir ").append(teleport.getHeading().getValue());
                }

                String commandStr = command.toString();
                if(logger.isDebugEnabled()){
                    logger.debug("sending command to VBS plugin:\n"+commandStr);
                }

                sendCommand(commandStr, message, errorMsg);
            }else{
                errorMsg.append("Received unhandled coordinate type of ").append(coordinate.getType()).append(" for VBS teleport learner scenario adaptation request.");
                return;
            }
            
        }else if(type instanceof generated.dkf.EnvironmentAdaptation.Script){

            generated.dkf.EnvironmentAdaptation.Script script = (generated.dkf.EnvironmentAdaptation.Script)type;

            if(logger.isDebugEnabled()){
                logger.debug("sending command to VBS plugin:\n"+script.getValue());
            }

            sendCommand(script.getValue(), message, errorMsg);
            
        }else if(type instanceof generated.dkf.EnvironmentAdaptation.RemoveHighlightOnObjects){
            // remove one or more highlighting of object that was previously created
            
            generated.dkf.EnvironmentAdaptation.RemoveHighlightOnObjects removeHighlightObj = 
                    (generated.dkf.EnvironmentAdaptation.RemoveHighlightOnObjects)type;
            
            String objDisplayName = removeHighlightObj.getHighlightName();
            
            StringBuilder command = new StringBuilder();
            command.append("deleteVehicle ").append("_").append(objDisplayName).append(";");
            
            String commandStr = command.toString();
            if(logger.isDebugEnabled()){
                logger.debug("sending REMOVE HighlightObject command to VBS plugin:\n"+commandStr);
            }

            sendCommand(commandStr, message, errorMsg);
            
        }else if(type instanceof generated.dkf.EnvironmentAdaptation.RemoveBreadcrumbs){
            // remove breadcrumbs attached to a team member
            
            generated.dkf.EnvironmentAdaptation.RemoveBreadcrumbs removeBreadcrumbs = 
                    (generated.dkf.EnvironmentAdaptation.RemoveBreadcrumbs)type;
            
            StringBuilder command = new StringBuilder();            
            
            List<generated.dkf.EnvironmentAdaptation.RemoveBreadcrumbs.TeamMemberRef> teamMemberRefs = removeBreadcrumbs.getTeamMemberRef();
            
            for(generated.dkf.EnvironmentAdaptation.RemoveBreadcrumbs.TeamMemberRef teamMemberRef : teamMemberRefs){
                String entityMarking;
                if(StringUtils.isNotBlank(teamMemberRef.getEntityMarking())){
                    entityMarking = teamMemberRef.getEntityMarking();
                }else{
                    entityMarking = "player";
                }
                
                command.append("_removeMark = getReferenceMark ").append(entityMarking).append(";");
                command.append("deleteReferenceMark _removeMark;");
            }
            
            String commandStr = command.toString();
            if(logger.isDebugEnabled()){
                logger.debug("sending REMOVE RemoveBreadcrumbs command to VBS plugin:\n"+commandStr);
            }

            sendCommand(commandStr, message, errorMsg);
            
        }else if(type instanceof generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs){
            // create breadcrumbs
            
            generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs createBreadcrumbs = 
                    (generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs)type;
            
            StringBuilder command = new StringBuilder();
            
            generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locationInfo = createBreadcrumbs.getLocationInfo();
            if(locationInfo == null){
                //ERROR
                errorMsg.append("Received no location information for VBS create breadcrumb(s) scenario adaptation request.");
                return;
            }
                
            int i = 0;
            for(generated.dkf.Coordinate coordinate : locationInfo.getCoordinate()){
                // one or more breadcrumbs to place for this set
                
                if(coordinate.getType() instanceof generated.dkf.AGL){

                    generated.dkf.AGL agl = (generated.dkf.AGL)coordinate.getType();
                    
                    // createReferenceMark requires ASL coordinate, not AGL
                    command.append("_coord").append(i).append(" = convertToASL ").append("[").append(agl.getX()).append(Constants.COMMA).append(agl.getY()).append(Constants.COMMA).append(agl.getElevation()).append("];");
                    command.append("_marker").append(i).append(" = west createReferenceMark ").append("_coord").append(i).append(";");

                    for(generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs.TeamMemberRef teamMemberRef : createBreadcrumbs.getTeamMemberRef()){
                        // assign it to each of the specified team members
                        
                        String entityMarking;
                        if(StringUtils.isNotBlank(teamMemberRef.getEntityMarking())){
                            entityMarking = teamMemberRef.getEntityMarking();
                        }else{
                            entityMarking = "player";
                        }
                        
                        command.append(entityMarking).append(" setReferenceMark ").append("_marker").append(i).append(";");
                    } // end for loop on team members
                    
                    i++;
                }else{
                    errorMsg.append("Received unhandled coordinate type of ").append(coordinate.getType()).append(" for VBS create breadcrumbs object scenario adaptation request.");
                    return;
                }
            } // end for on coordinates

            
            String commandStr = command.toString();
            if(logger.isDebugEnabled()){
                logger.debug("sending CreateBreadcrumbs command to VBS plugin:\n"+commandStr);
            }

            sendCommand(commandStr, message, errorMsg);
            
        }else if(type instanceof generated.dkf.EnvironmentAdaptation.HighlightObjects){
            // add one or more highlighting of object
            
            generated.dkf.EnvironmentAdaptation.HighlightObjects highlightObj = (generated.dkf.EnvironmentAdaptation.HighlightObjects)type;
            
            StringBuilder command = new StringBuilder();
                
            String objDisplayName = highlightObj.getName();
            
            String actorType;
            generated.dkf.EnvironmentAdaptation.HighlightObjects.Color color = highlightObj.getColor();
            if(color == null){
                // default to blue
                actorType = "vbs2_editor_waypoint_blue";
            }else if(color.getType() instanceof generated.dkf.EnvironmentAdaptation.HighlightObjects.Color.Blue){
                actorType = "vbs2_editor_waypoint_blue";
            }else if(color.getType() instanceof generated.dkf.EnvironmentAdaptation.HighlightObjects.Color.Green){
                actorType = "vbs2_editor_waypoint_green";
            }else if(color.getType() instanceof generated.dkf.EnvironmentAdaptation.HighlightObjects.Color.Red){
                actorType = "vbs2_editor_waypoint_red";
            }else{
                // catch all case, default to blue
                actorType = "vbs2_editor_waypoint_blue";
            }
            
            Serializable objType = highlightObj.getType();
            if(objType instanceof generated.dkf.EnvironmentAdaptation.HighlightObjects.TeamMemberRef){
                // attaching the highlight to an actor
                
                generated.dkf.EnvironmentAdaptation.HighlightObjects.TeamMemberRef teamMemberRef = (generated.dkf.EnvironmentAdaptation.HighlightObjects.TeamMemberRef)objType;
                
                String entityMarking;
                if(StringUtils.isNotBlank(teamMemberRef.getEntityMarking())){
                    entityMarking = teamMemberRef.getEntityMarking();
                }else{
                    entityMarking = "player";
                }
                
                if(logger.isDebugEnabled()){
                    logger.debug("Building command to highlight "+entityMarking+" in VBS.");
                }
                
                // create waypoint
                command.append("_").append(objDisplayName).append(" = createVehicle [\"").append(actorType).append("\"");
                command.append(", getPos ").append(entityMarking);
                command.append(", [], 0, \"NONE\"];");
                
                // set the highlight object name
                command.append("_").append(objDisplayName).append(" setUnitName \"").append(objDisplayName).append("\";");
                
                // attach waypoint
                double right = 0, front = 0, up = 0;
                if(highlightObj.getOffset() != null){
                    
                    generated.dkf.EnvironmentAdaptation.HighlightObjects.Offset offset = highlightObj.getOffset();
                    right = offset.getRight() != null ? offset.getRight().doubleValue() : 0;
                    front = offset.getFront() != null ? offset.getFront().doubleValue() : 0;
                    up = offset.getUp() != null ? offset.getUp().doubleValue() : 0;                    
                }
                
                command.append("_").append(objDisplayName).append(" attachTo [").append(entityMarking).append(",[").append(right).append(",").append(front).append(",").append(up).append("]];");
                
                if(highlightObj.getDuration() != null){
                    // TODO: need to implement
                    
//                        command.append("sleep ").append(objToHighlight.getDuration().intValue()).append(";");
//                        command.append("detach ").append("_").append(objName).append(";");
                }
                
                
            }else if(objType instanceof generated.dkf.EnvironmentAdaptation.HighlightObjects.LocationInfo){
                // creating the highlight at a fixed location
                
                generated.dkf.EnvironmentAdaptation.HighlightObjects.LocationInfo locationInfo = 
                        (generated.dkf.EnvironmentAdaptation.HighlightObjects.LocationInfo)objType;
                generated.dkf.Coordinate coordinate = locationInfo.getCoordinate();
                if(coordinate.getType() instanceof generated.dkf.AGL){

                    generated.dkf.AGL agl = (generated.dkf.AGL)coordinate.getType();
                    
                    if(logger.isDebugEnabled()){
                        logger.debug("Building command to highlight ["+agl.getX()+", "+agl.getY()+", "+agl.getElevation()+"] in VBS.");
                    }

                    command.append("_").append(objDisplayName).append(" = createVehicle [\"").append(actorType).append("\"");
                    command.append(", [").append(agl.getX()).append(Constants.COMMA).append(agl.getY()).append(Constants.COMMA).append(agl.getElevation()).append("]");
                    command.append(", [], 0, \"NONE\"];");
                    
                    // set the highlight object name
                    command.append("_").append(objDisplayName).append(" setUnitName \"").append(objDisplayName).append("\";");
                    
                    if(highlightObj.getDuration() != null){
                        // TODO: need to implement
                        
                        // [5] execVM "mycontent\scripts\sleep.sqf";
//                            command.append("_sleeping = [").append(objToHighlight.getDuration().intValue()).append(",  _createdObj, \"deleteVehicle _nearbyBldg;\"] execVM \"mycontent\\scripts\\sleep.sqf\";");
//                            command.append("sleep ").append(objToHighlight.getDuration().intValue()).append(";");
//                            command.append("waitUntil {scriptDone _sleeping};");
//                            command.append("deleteVehicle ").append("_").append(objName).append(";");
                    }

                }else{
                    errorMsg.append("Received unhandled coordinate type of ").append(coordinate.getType()).append(" for VBS highlight object scenario adaptation request.");
                    return;
                }
                
            }

            String commandStr = command.toString();
            if(logger.isDebugEnabled()){
                logger.debug("sending HighlightObject command to VBS plugin:\n"+commandStr);
            }

            sendCommand(commandStr, message, errorMsg);

        }else {

            //ERROR
            logger.error("Received unhandled environment control type " + control.getEnvironmentStatusType());
            errorMsg.append("VBS plugin can't handle environment control type of ").append(control.getEnvironmentStatusType());
        }
    }
    
    /**
     * Query VBS via the GIFT VBS Plugin DLL on behalf of GIFT for variable state info on one or more
     * players in the environment.
     * 
     * @param variablesStateRequestMsg the original request message to respond to with the
     *        variables state results
     * @param errorMsg error message builder.
     * @return boolean - whether this method sent a GIFT message in response to
     *         this query
     */
    private boolean doVariablesStateQuery(DomainSessionMessage variablesStateRequestMsg, StringBuilder errorMsg) {
        
        boolean handled = false;
        
        if(logger.isInfoEnabled()) {            
            logger.info("\nVBS plugin rec'd Variables State Request Query");            
        }
        
        boolean addedSomething = false;
        VariablesState variablesState = new VariablesState();
        VariablesStateRequest request = (VariablesStateRequest) variablesStateRequestMsg.getPayload();
        for(VARIABLE_TYPE varType : request.getTypeToVarInfoMap().keySet()){
        
            VariableInfo varInfo = request.getTypeToVarInfoMap().get(varType);
            Set<String> entityIds = varInfo.getEntityIds();
            switch(varType){
            case WEAPON_STATE:
            
                if(entityIds == null){
                continue;
            }
                Map<String, VariableState> wStateVarMap = variablesState.getVariableMapForType(VARIABLE_TYPE.WEAPON_STATE);
                for(String entityId : entityIds){
                    WeaponState wState = doWeaponStateQuery(entityId, errorMsg);
            
                    if(wState != null){
                        wStateVarMap.put(entityId, wState);
                        addedSomething = true;
                    }
                }
                
                break;
            case ANIMATION_PHASE:  
                
                Map<String, VariableState> aPhaseVarMap = variablesState.getVariableMapForType(VARIABLE_TYPE.ANIMATION_PHASE);
                for(String entityId : entityIds){
                    
                    VariableNumberState aVNumState = null;
                    try{
                        aVNumState = doAnimationPhaseQuery(entityId, varInfo.getVarName(), errorMsg);                        
                        
                    }catch(ParseException parseException){
                        // this could be due to the animation phase not being loaded yet, don't flood the log with errors
                        logger.debug("Received exception while trying to get the animation phase value of '"+varInfo.getVarName()+"' for "+entityId, parseException);
                    }finally{
                        // make sure there is something returned, even if null. This is needed by RequestExternalAttributeCondition
                        // to trigger another request
                        if(aVNumState == null){
                            aVNumState = new VariableNumberState(varInfo.getVarName(), null);
                        }
                        
                        aPhaseVarMap.put(entityId, aVNumState);
                        addedSomething = true;
                    }
                }
                
                break;
                
            case VARIABLE:
                
                Map<String, VariableState> vPhaseVarMap = variablesState.getVariableMapForType(VARIABLE_TYPE.VARIABLE);
                for(String entityId : entityIds){
                    
                    VariableNumberState aVNumState = null;
                    try{
                        aVNumState = doVariableStateQuery(entityId, varInfo.getVarName(), errorMsg);
                    }catch(ParseException parseException){
                        logger.error("Received exception while trying to get the value of the variable '"+varInfo.getVarName()+" for "+entityId, parseException);
                    }finally{
                        // make sure there is something returned, even if null. This is needed by RequestExternalAttributeCondition
                        // to trigger another request
                        if(aVNumState == null){
                            aVNumState = new VariableNumberState(varInfo.getVarName(), null);
                        }
                        
                        vPhaseVarMap.put(entityId, aVNumState);
                        addedSomething = true;
                    }
                }
                
                break;                
            }
        }
        
        if(addedSomething) {

            VariablesStateResult result = new VariablesStateResult(request.getRequestId(), variablesState);

            GatewayModule.getInstance().sendMessageToGIFT(variablesStateRequestMsg.getUserSession(), variablesStateRequestMsg.getDomainSessionId(), 
                    variablesStateRequestMsg.getExperimentId(), result, MessageTypeEnum.VARIABLE_STATE_RESULT, this);
            
            handled = true;
            
        } else {
        
            logger.error("Failed to generates any results for variables State request query");
            errorMsg.append("VBS plugin failed to generate any results for variables State request query");            
        }  
        
        return handled;
    }
    
    /**
     * Query VBS via the GIFT VBS plugin for the variable value of the specific variable name on the actor
     * with the given entity marking.
     * @param entityMarking the unique identifier in VBS for an entity to get the variable value for.  Shouldn't be null or empty.
     * @param animationName the name of a variable on the VBS entity to get the variable value for (e.g. 'status')
     * @param errorMsg error message builder.  If empty after this method than there was no error.
     * @return a new VariableNumberState with the value of the variable value 
     * @throws ParseException if there was a problem converting the variable value into a number
     */
    private VariableNumberState doVariableStateQuery(String entityMarking, String varName, StringBuilder errorMsg) throws ParseException{
        
        if(StringUtils.isBlank(entityMarking)){
            return new VariableNumberState(varName, null);
        }
        
        String command = entityMarking + " getVariable \"" + varName + "\";";

        if(logger.isInfoEnabled()) {                
            logger.info("sending command: " + command);
        }
        
        String result = sendCommand(command, errorMsg, RETRY_ON_FAIL);
        
        Number varValue = null;
        if(result != null) {

            if(logger.isInfoEnabled()) {                    
                logger.info("rec'd result: " + result);
            }
            
            if(result.equalsIgnoreCase("scalar")){
                // VBS3 returns 'scalar' when the entity can't be found
                return new VariableNumberState(varName, null);
            }
            
            varValue = NumberFormat.getInstance().parse(result);
        }
        
        return new VariableNumberState(varName, varValue);
    }
    
    /**
     * Query VBS via the GIFT VBS plugin for the animation phase of the specific animation name on the actor
     * with the given entity marking.
     * @param entityMarking the unique identifier in VBS for an entity to get the animation phase value for.  Shouldn't be null or empty.
     * @param animationName the name of an animation on the VBS entity to get the animation phase for (e.g. 'down')
     * @param errorMsg error message builder.  If empty after this method than there was no error.
     * @return a new VariableNumberState with the value of the animation phase 
     * @throws ParseException if there was a problem converting the animation phase value into a number
     */
    private VariableNumberState doAnimationPhaseQuery(String entityMarking, String animationName, StringBuilder errorMsg) throws ParseException{
        
        if(StringUtils.isBlank(entityMarking)){
            return new VariableNumberState(animationName, null);
        }
        
        String command = entityMarking + " animationPhase '" + animationName + "';";

        if(logger.isInfoEnabled()) {                
            logger.info("sending command: " + command);
        }
        
        String result = sendCommand(command, errorMsg, RETRY_ON_FAIL);
        
        Number varValue = null;
        if(result != null) {

            if(logger.isInfoEnabled()) {                    
                logger.info("rec'd result: " + result);
            }
            
            if(result.equalsIgnoreCase("scalar")){
                // VBS3 returns 'scalar' when the entity can't be found
                return new VariableNumberState(animationName, null);
            }
            
            varValue = NumberFormat.getInstance().parse(result);
        }
        
        return new VariableNumberState(animationName, varValue);
    }
    
    /**
     * Query VBS via the GIFT VBS Plugin DLL on behalf of GIFT for Weapon state info on one
     * players in the environment.
     * 
     * @param marking the unique identifier in VBS for an entity to get the weapon status for
     * @param errorMsg error message builder.
     * @return the new weapon state object containing the results of any queries to VBS.  Will
     * be null if the entity marking was null/blank.  Could contain spotty values if a command
     * failed.
     */
    private WeaponState doWeaponStateQuery(String marking, StringBuilder errorMsg){
            
        if(StringUtils.isBlank(marking)){
            return null;
        }
        
        WeaponState wState = new WeaponState(marking);

        String command = "weaponSafety " + marking + ";";

        if(logger.isInfoEnabled()) {                
            logger.info("sending command: " + command);
        }
        
        String result = sendCommand(command, errorMsg, RETRY_ON_FAIL);
        
        if(result != null) {

            if(logger.isInfoEnabled()) {                    
                logger.info("rec'd result: " + result);
            }
            
            if(!result.equals("<null>")){
                wState.setWeaponSafetyStatus(result.equalsIgnoreCase("TRUE") ? Boolean.TRUE : Boolean.FALSE);
            }
        }else {
         
            logger.error("Received null result from command: " + command);                    
        }
        
        command = "currentWeapon " + marking + ";";

        if(logger.isInfoEnabled()) {                
            logger.info("sending command: " + command);
        }
        
        result = sendCommand(command, errorMsg, RETRY_ON_FAIL);
        
        if(result != null) {

            if(logger.isInfoEnabled()) {                    
                logger.info("rec'd result: " + result);
            }
            
            if(!result.equals("<null>")){
                wState.setHasWeapon(result.length() > 0 ? Boolean.TRUE : Boolean.FALSE);
            }
        }else {
         
            logger.error("Received null result from command: " + command);                    
        }
        
        command = marking + " weaponDirection \"\";";

        if(logger.isInfoEnabled()) {                
            logger.info("sending command: " + command);
        }
        
        result = sendCommand(command, errorMsg, RETRY_ON_FAIL);
        
        if(result != null) {

            if(logger.isInfoEnabled()) {                    
                logger.info("rec'd result: " + result);
            }
            
            if(!result.equals("<null>")){
                String[] tokens = org.apache.commons.lang.StringUtils.split(result, "[],");
                if(tokens.length == 3){
                    Vector3d weaponAim = new Vector3d(Double.valueOf(tokens[0]), Double.valueOf(tokens[1]), Double.valueOf(tokens[2]));
                    wState.setWeaponAim(weaponAim);
                }
            }
        }else {
         
            logger.error("Received null result from command: " + command);                    
        }
        
        return wState;        
    }  
        
        
    /**
     * Query VBS via the GIFT VBS Plugin DLL on behalf of GIFT for LOS between
     * player and a list of points in the environment.
     * 
     * @param losQueryMsg the original request message to respond to with the
     *        LOS results
     * @param errorMsg error message builder.
     * @return boolean - whether this method sent a GIFT message in response to
     *         this query
     */
    private boolean doLosQuery(DomainSessionMessage losQueryMsg, StringBuilder errorMsg) {
                
        boolean handled = false;
        
        if(logger.isInfoEnabled()) {            
            logger.info("\nVBS plugin rec'd LOS_QUERY");            
        }        
                
        LoSQuery losQuery = (LoSQuery)losQueryMsg.getPayload();    
        List<Point3d> points = losQuery.getLocations();

        Map<String, List<VisibilityResult>> entityLoSResults = new HashMap<>();

        for(String entityMarking : losQuery.getEntities()){
            
            List<VisibilityResult> results = new ArrayList<>();
            entityLoSResults.put(entityMarking, results);
            
            for(int index = 0; index < points.size(); index++) {
    
                Point3d point = points.get(index);
                String command = entityMarking + " getVisibilityFromCamPos [" + point.getX() +"," + point.getY() + "," + point.getZ() + "];";
    
                if(logger.isInfoEnabled()) {                    
                    logger.info("sending command: " + command);
                }    
                
                String result = sendCommand(command, errorMsg, RETRY_ON_FAIL);
                
                if(result != null) {
    
                    if(logger.isInfoEnabled()) {                        
                        logger.info("rec'd result: " + result);
                    }
                    
                    double visibilityResult = 0.0;
                    
                    if( !result.contains("<null>") ) {                           
                        visibilityResult = Double.parseDouble(result);
                    }else if (logger.isInfoEnabled()) {
                        
                        if(logger.isInfoEnabled()){
                            logger.info("result string was <null>. reporting 0.0 as visibility result");
                        }
                    }
                    
                    results.add(new VisibilityResult(index, visibilityResult));
                }
                else {
                 
                    logger.error("Received null result from command: " + command);                    
                }
            }
        }
        
        if(!entityLoSResults.isEmpty()) {

            LoSResult losResult = new LoSResult(entityLoSResults, losQuery.getRequestId());

            GatewayModule.getInstance().sendMessageToGIFT(losQueryMsg.getUserSession(), losQueryMsg.getDomainSessionId(), losQueryMsg.getExperimentId(), losResult, MessageTypeEnum.LOS_RESULT, this);
            
            handled = true;
        }
        else {
        
          logger.error("Failed to generates any results for LOS query");
          errorMsg.append("VBS plugin failed to generate any results for LOS query");
            
        }  
        
        return handled;        
    } 
    
    /**
     * Request the VBS scenario metadata and then send a GIFT message with that information.  If the VBS
     * scenario is not running when this is called a GIFT message will not be sent and false will be returned.
     * @return true if a GIFT message was sent with the VBS scenario metadata.
     */
    @SuppressWarnings("unchecked")
    private boolean sendVBSScenarioMetadata(){
        
        boolean receivedInfo = false;
        
        if(logger.isInfoEnabled()) {            
            logger.info("\nVBS plugin performing scenario metadata query");            
        }
        
        StringBuilder errorMsg = new StringBuilder();
        String result = sendCommand(MISSION_NAME_COMMAND, errorMsg, false);
        String missionName = null;
        
        if(result != null) {

            if(logger.isInfoEnabled()) {                        
                logger.info(MISSION_NAME_COMMAND + " command rec'd result: " + result);
            }
            
            missionName = result.replace("\"", "");
        }
        
        result = sendCommand(TERRAIN_NAME_COMMAND, errorMsg, false);
        
        String terrainName = null;
        
        if(result != null) {

            if(logger.isInfoEnabled()) {                        
                logger.info(TERRAIN_NAME_COMMAND + " command rec'd result: " + result);
            }
            
            terrainName = result.replace("\"", "");
        }
        
        if(StringUtils.isNotBlank(missionName) && StringUtils.isNotBlank(terrainName)){
            JSONObject jObj = new JSONObject();
            jObj.put("scenarioName", missionName);
            jObj.put("terrainName", terrainName);
            
            if(logger.isInfoEnabled()){
                logger.info("Sending VBS mission metadata message to GIFT");
            }
            
            GenericJSONState state = new GenericJSONState();
            state.addAll(jObj);
            GatewayModule.getInstance().sendMessageToGIFT(state, MessageTypeEnum.GENERIC_JSON_STATE, this);
            receivedInfo = true;
        }
        
        return receivedInfo;
    }
    
    
    /**
     * Send a script command to VBS GIFT Plugin and then replies to the GIFT
     * message that called the command.
     * 
     * @param command The VBS command to execute in the VBS plugin DLL
     * @param replyTo The request message that caused the command to be sent.  Can be null.
     * @param errorMsg Buffer to write error messages too.  Can't be null.
     */
    public void sendCommand(String command, Message replyTo, StringBuilder errorMsg) {
                
        String result = sendCommand(command, errorMsg, RETRY_ON_FAIL);

        if (result != null) {
            
            if(logger.isDebugEnabled()){
                logger.debug("Received a response of "+result+" from sending the command "+command);
            }
            
//            //TODO: Still possible for a command to fail within VBS, need to handle those cases
            
        } else {
            
            logger.error("The attempt at sending the command "+command+" has failed because it returned a result of null");
            
            errorMsg.append("The VBS plugin failed to respond.  Check the interop configuration and the Gateway module log for more details.");

        }
    }
    
    
    /**
     * Sends a script command to the VBS GIFT Plugin.</br>
     * Note: made this synchronized once the VBS scenario metadata thread was added which can asynchronously send commands
     * to the GIFT VBS plugin.  That plugin hasn't been tested against concurrent requests.
     * 
     * @param command The command to execute in the plugin
     * @param errorMsg - used to append error content too, if an error occurs
     * @param retryOnFail - whether to keep trying to send the command again if it fails
     * @return String The result of the command being executed.  Null if there was an issue.
     */    
    public synchronized String sendCommand(String command, StringBuilder errorMsg, boolean retryOnFail) {
        
        try {
            
            String response = socketHandler.sendCommand(command, errorMsg, retryOnFail, true);
            return response;

        } catch (IOException e) {    
            
            //this is usually caused when VBS is not running when the GW module
            //starts
            socketHandler = null;
            
            logger.error("IOException caught when connecting to the VBS Plugin.  This is not a problem if VBS isn't running yet and you plan to start it later.  Also, if you don't plan on running any GIFT courses"+
                    " that use VBS, this can be ignored.  Otherwise, one solution might be to try a different port. Refer to the GIFT installation instructions on VBS for more information.", e);
            errorMsg.append("Failed to send message to the VBS Plugin.  Is VBS running and does it have the GIFT VBS plugin installed?  Refer to the GIFT installation instructions on VBS for more information.");
        }
        
        return null;
    }
    
    /**
     * Calls the necessary logic to make sure the VBS window is not the foreground window
     * as well as end any running scenario.  This is useful for a premature shutdown of GIFT
     * that occurs during a VBS scenario as part of a GIFT course.
     */
    private void undoForegroundVBS(){
        
        if(isVBSForeground){
            
            try{
                GatewayModuleUtils.setAlwaysOnTop(ahkClass, false);
            }catch(ConfigurationException e){
                logger.error("Caught exception while trying to set VBS window to NOT be the foreground window.", e);
            }
            
            isVBSForeground = false;
            
            if(ahkClass != null){
            StringBuilder errorMsg = new StringBuilder();  //don't care
            sendCommand(END_SCENARIO_COMMAND, null, errorMsg);
        }
    }
    }

    @Override
    public void cleanup() {
        
        if(isVBSForeground){
            undoForegroundVBS();
        }
    }
    
    @Override
    public void setEnabled(boolean value) throws ConfigurationException{
        
        if(isEnabled() && !value){
            //transition from enabled to not enabled
            
            if(logger.isInfoEnabled()){
                logger.info("Disabling VBS plugin interface");
            }
            
            if(isVBSForeground){
                undoForegroundVBS();
            }
            
            try {
                if(socketHandler != null){
                    socketHandler.disconnect();
                }
            } catch (@SuppressWarnings("unused") IOException e) {
                //not sure if we care at this point
            }
        }else if(!isEnabled() && value){
            //transition from not enabled to enabled
            
            if(logger.isInfoEnabled()){
                logger.info("Enabling VBS plugin interface");
            }
            
            try {
                createSocketHandler();
                socketHandler.connect();
            } catch (IllegalArgumentException | IOException e) {
                throw new ConfigurationException("Failed to connect the VBS Plugin interface socket connection.", 
                        "There was an exception while trying to connect:\n"+e.getMessage(), e);
            }
        }
        
        super.setEnabled(value);
    }

    @Override
    public List<MessageTypeEnum> getSupportedMessageTypes() {
        return supportedMsgTypes;
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
        
        if(ahkClass != null){
        StringBuilder errorMsg = new StringBuilder();
        if(scenarioIdentifier == null){            
            sendCommand(END_SCENARIO_COMMAND, null, errorMsg);
        }else{
            sendCommand("hostMission [\"" + scenarioIdentifier + "\"]", null, errorMsg); 
        }
        
        if(errorMsg.length() > 0){
            throw new DetailedException("Failed to load the VBS scenario named '"+scenarioIdentifier+"'.", 
                    "There was an error when trying to load the scenario:\n"+errorMsg.toString(), null);
        }
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
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[VBSPluginInterface: ");
        sb.append(super.toString());
        sb.append(", socket = ").append(socketHandler);
        
        sb.append(", messageTypes = {");
        for(MessageTypeEnum mType : supportedMsgTypes){
            sb.append(mType).append(", ");
        }
        sb.append("}");
        
        sb.append("]");
        
        return sb.toString();
    }

}
