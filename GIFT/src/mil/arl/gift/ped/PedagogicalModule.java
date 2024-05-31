/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ped;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import mil.arl.gift.common.CloseDomainSessionRequest;
import mil.arl.gift.common.CourseState;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.InitializeDomainSessionRequest;
import mil.arl.gift.common.InitializePedagogicalModelRequest;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.PedagogicalRequest;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.enums.ErrorEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.ModuleModeEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.module.AbstractModule;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.net.api.SubjectUtil;
import mil.arl.gift.net.api.message.ACK;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.NACK;
import mil.arl.gift.net.api.message.UserSessionMessage;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the Pedagogical Module which is responsible for determining selecting pedagogical strategies
 * based on the learner's state.
 * 
 * @author mhoffman
 *
 */
public class PedagogicalModule extends AbstractModule {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(PedagogicalModule.class);
    
    /** the singleton instance of this class */
    private static PedagogicalModule instance = null;

    /** the name of the module */
    private static final String DEFAULT_MODULE_NAME = "Pedagogical_Module";
    
    /** mapping of unique user session to Pedagogical instance */
    private Map<UserSession, Pedagogical> userSessionToPed = new HashMap<>();
    
    /** mapping of unique domain session id to the domain module address */
    private Map<Integer, String> domainSessionIdToDomain = new HashMap<Integer, String>();

    static {
        //use Learner log4j
        PropertyConfigurator.configureAndWatch(PackageUtil.getConfiguration() + "/ped/ped.log4j.properties");
    }

    /**
     * Return the singleton instance of this class
     *
     * @return PedagogicalModule
     */
    public static PedagogicalModule getInstance() {

        if (instance == null) {
            instance = new PedagogicalModule();
            instance.init();
        }

        return instance;
    }

    /**
     * Class constructor
     */
    private PedagogicalModule() {
        super(DEFAULT_MODULE_NAME, SubjectUtil.PED_QUEUE_PREFIX + ADDRESS_TOKEN_DELIM + ipaddr, SubjectUtil.PED_QUEUE_PREFIX + ADDRESS_TOKEN_DELIM + ipaddr + ADDRESS_TOKEN_DELIM + SubjectUtil.INBOX_QUEUE_SUFFIX, PedagogicalModuleProperties.getInstance());
    }

    /**
     * Perform initialization logic for the module
     */
    @Override
    protected void init() {
        
        //create client to send status too
        createSubjectTopicClient(SubjectUtil.PED_DISCOVERY_TOPIC, false);

        //start the module heartbeat
        initializeHeartbeat();
    }
    
    @Override
    public ModuleTypeEnum getModuleType() {
        return ModuleTypeEnum.PEDAGOGICAL_MODULE;
    }
    

    @Override
    protected void handleMessage(Message message) {

        MessageTypeEnum type = message.getMessageType();
        if (type == MessageTypeEnum.LEARNER_STATE){
//            
//            //DEBUG
//            //Date rcv = new Date();
//            
            handleLearnerState(message);
//            
//            //DEBUG
//            //System.out.println("time = "+ (new Date().getTime() - rcv.getTime()));
//            
        } else if (type == MessageTypeEnum.INITIALIZE_DOMAIN_SESSION_REQUEST) {
            handleInitializeDomainSessionRequest(message);
            
        } else if (type == MessageTypeEnum.INITIALIZE_LESSON_REQUEST) {
            handleInitializeLessonRequest(message);

        } else if (type == MessageTypeEnum.INITIALIZE_PEDAGOGICAL_MODEL_REQUEST) {
            handleInitializePedagogicalModelRequest(message);
            
        } else if (type == MessageTypeEnum.START_DOMAIN_SESSION) {
            handleStartDomainSessionRequest(message);

        } else if (type == MessageTypeEnum.LESSON_STARTED) {
            handleLessonStarted(message);
            
        } else if (type == MessageTypeEnum.LESSON_COMPLETED) {
            handleLessonCompleted(message);

        } else if (type == MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST) {
            handleCloseDomainSessionRequest(message);

        } else if (type == MessageTypeEnum.KILL_MODULE) {
            handleKillModuleMessage(message);
            
        } else if (type == MessageTypeEnum.COURSE_STATE) {
            handleCourseStateMessage(message);

        } else {

            logger.error(getModuleName() + " received unhandled message:" + message);
            
            if(message.needsHandlingResponse()) {
                this.sendReply(message, new NACK(ErrorEnum.UNHANDLED_MESSAGE_ERROR, "The module did not know how to handle the message"), MessageTypeEnum.PROCESSED_NACK);
            }
        }
        
    }
    
    /**
     * Handle a course state update message.
     * 
     * @param message - the course state message
     */
    private void handleCourseStateMessage(Message message){
        
        if(logger.isInfoEnabled()){
            logger.info(getModuleName() + " received " + message.toString() + " message");
        }
        DomainSessionMessage domainSessionMessage = (DomainSessionMessage)message;
       
        //get pedagogical instance for learner
        if( userSessionToPed.containsKey(domainSessionMessage.getUserSession()) ){
            
            Pedagogical ped = userSessionToPed.get(domainSessionMessage.getUserSession());
            
            CourseState courseState = (CourseState) message.getPayload();

            // handle course state and retrieve any ped requests as a result of the update
            PedagogicalRequest request = ped.handleCourseStateUpdate(courseState);

            if(logger.isInfoEnabled()){
                logger.info("after handling the incoming course state, the Ped Requests are: " + request);
            }

            if(request == null || request.getRequests().isEmpty()){
                sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
            }else{
                sendPedagogicalRequest(ped.getDomainSession(), request, message);
            }

        }else{
            logger.error("Unable to handle course state message because the pedagogical instance for domain session with id = "+domainSessionMessage.getDomainSessionId() + " could not be found.\n"+message);
            sendReply(message, new NACK(ErrorEnum.OPERATION_FAILED, "Failed to find a pedagogical instance for domain session"), MessageTypeEnum.PROCESSED_NACK);
        }
    }
    
    /**
     * Handle the learner state message sent by the learner module
     * 
     * @param message - the learner state message for a learner
     */
    private void handleLearnerState(Message message){
        
        logger.info(getModuleName() + " received " + message.toString() + " message");
        DomainSessionMessage domainSessionMessage = (DomainSessionMessage)message;
       
        //get pedagogical instance for learner
        if( userSessionToPed.containsKey(domainSessionMessage.getUserSession()) ){
            
            Pedagogical ped = userSessionToPed.get(domainSessionMessage.getUserSession());
            
            //handle learner state
            ped.handleLearnerStateUpdate((LearnerState)message.getPayload());

        }else{
            logger.error("Unable to handle learner state message because the pedagogical instance for domain session with id = "+domainSessionMessage.getDomainSessionId() + " could not be found.\n"+message);
        }
    }

    /**
     * Handle the initialize domain session request message
     * 
     * @param message - the initialized domain session request message
     */
    private void handleInitializeDomainSessionRequest(Message message) {

        DomainSessionMessage domainSessionMessage = (DomainSessionMessage)message;
        if (logger.isInfoEnabled()) {
            logger.info(getModuleName() + " received " + message.toString() + " message");
        }
        
        int domainId = domainSessionMessage.getDomainSessionId();
        
        if( !userSessionToPed.containsKey(domainSessionMessage.getUserSession()) ) {
            
            UserSession uSession = domainSessionMessage.getUserSession();
            DomainSession dSession = new DomainSession(domainId, uSession.getUserId(), DomainSession.UNKNOWN_DOMAIN_NAME, DomainSession.UNKNOWN_DOMAIN_NAME);
            dSession.copyFromUserSession(uSession);
            
            try{
                Pedagogical ped = new Pedagogical(dSession); 

                //pass the initialization request down through the ped to the ped model
                ped.initialize((InitializeDomainSessionRequest)message.getPayload());                
                
                userSessionToPed.put(domainSessionMessage.getUserSession(), ped);
                domainSessionIdToDomain.put(domainId, message.getSenderAddress());
                
            }catch(Exception e){
                logger.error("Caught exception while initializing the ped model", e);
                sendReply(message, new NACK(ErrorEnum.OPERATION_FAILED, "Failed to initalize the Pedagogical module"), MessageTypeEnum.PROCESSED_NACK);
            }

        }
        else {
            
            logger.warn("Request to initialize Ped with domain session id of " + domainSessionMessage.getDomainSessionId() +  " but this domain session already exists.\n"+domainSessionMessage);
        }
        
        sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
    }
    
    /**
     * Handle the initialize lesson request message
     * 
     * @param message - the initialized lesson request message
     */
    private void handleInitializeLessonRequest(Message message) {

        DomainSessionMessage domainSessionMessage = (DomainSessionMessage)message;
        if (logger.isInfoEnabled()) {
            logger.info(getModuleName() + " received " + message.toString() + " message");
        }
                
        if(userSessionToPed.containsKey(domainSessionMessage.getUserSession())) {
            //nothing to do...

        }
        else {
            
            logger.warn("Request to initialize lesson in Ped with domain session id of " + domainSessionMessage.getDomainSessionId() +  " but unable to find a ped mapped to that domain session");
        }
        
        sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
    }
    
    /**
     * Handle the initialize pedagogical model request message
     * 
     * @param message - the initialized pedagogical model request message
     */
    private void handleInitializePedagogicalModelRequest(Message message) {

        DomainSessionMessage domainSessionMessage = (DomainSessionMessage)message;
        if (logger.isInfoEnabled()) {
            logger.info(getModuleName() + " received " + message.toString() + " message");
        }
               
        if(userSessionToPed.containsKey(domainSessionMessage.getUserSession())) {
            
            Pedagogical ped = userSessionToPed.get(domainSessionMessage.getUserSession()); 
            
            try{
                //pass the initialization request down through the ped to the ped model
                ped.initialize((InitializePedagogicalModelRequest)message.getPayload());                
                
            }catch(Throwable e){
                logger.error("Caught exception while initializing the ped model for "+domainSessionMessage.getUserSession(), e);
                sendReply(message, new NACK(ErrorEnum.OPERATION_FAILED, "Failed to initalize ped model for the Pedagogical module"), MessageTypeEnum.PROCESSED_NACK);
                return;
            }

        }else {            
            logger.warn("Request to initialize Ped model in Ped with domain session id of " + domainSessionMessage.getDomainSessionId() +  
                    " but unable to find a ped mapped to that domain session.  Replying with ACK anyway.");
        }
        
        sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
    }

    /**
     * Handle the start domain session request message
     * 
     * @param message - the start domain session request message
     */
    private void handleStartDomainSessionRequest(Message message) {

        if (logger.isInfoEnabled()) {
            logger.info(getModuleName() + " received " + message.toString() + " message");
        }

        sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
    }

    /**
     * Handle the lesson started message
     * 
     * @param message - the lesson started message
     */
    private void handleLessonStarted(Message message) {

        if (logger.isInfoEnabled()) {
            logger.info(getModuleName() + " received " + message.toString() + " message");
        }
        
        
        DomainSessionMessage domainSessionMessage = (DomainSessionMessage)message;
        if(userSessionToPed.containsKey(domainSessionMessage.getUserSession())) {
            
            try {
                Pedagogical ped = userSessionToPed.get(domainSessionMessage.getUserSession()); 
                ped.handleLessonStarted();
            }catch(Throwable t) {
                logger.error("Caught exception while notifying the ped model of lesson started for "+domainSessionMessage.getUserSession(), t);
                sendReply(message, new NACK(ErrorEnum.OPERATION_FAILED, "Failed to notify a ped model of the Pedagogical module that a lesson was starting"), MessageTypeEnum.PROCESSED_NACK);
                return;
            }
        }

        sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
    }

    /**
     * Handle the lesson completed message
     * 
     * @param message - the lesson completed message
     */
    private void handleLessonCompleted(Message message) {

        if (logger.isInfoEnabled()) {
            logger.info("Received " + message.toString() + " message");
        }

        sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
    }

    /**
     * Handle the close domain session request message
     * 
     * @param message - the close domain session request message
     */
    private void handleCloseDomainSessionRequest(Message message) {

        if (logger.isInfoEnabled()) {
            logger.info("Received " + message);
        }
        
        if (this.userSessionToPed.containsKey(((DomainSessionMessage)message).getUserSession())) {
            
            this.userSessionToPed.remove(((DomainSessionMessage)message).getUserSession());
            boolean removed = allocationStatus.removeUserSession(((DomainSessionMessage)message).getUserSession());
            if(logger.isInfoEnabled()){
                logger.info("Closed Pedagogical for user session " + ((DomainSessionMessage)message).getUserSession()+".  Removed from allocated sessions for this module: "+removed);
            }
            
        }       
                
        sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
        
        releaseDomainSessionModules(((UserSessionMessage)message).getUserSession());
    }

    /**
     * Handles the message to kill this module
     *
     * @param message - The Kill Module message
     */
    private void handleKillModuleMessage(Message message) {
        Thread killModule = new Thread("Kill Module"){
            @Override
            public void run() {
                killModule();
            }            
        };
        killModule.start();
    }

    
    /**
     * Send a pedagogical request to the appropriate tutor based on the domain session 
     * 
     * @param domainSession information about the domain session (including the unique user id of the learner) 
     *                      the request is associated with
     * @param request - the pedagogical request to send.  If null or the requests within the request is empty this method does nothing.
     * @param messageReplyingToo - Contains a message that this pedagogical request is in reply too.  This can be null.
     */
    public void sendPedagogicalRequest(DomainSession domainSession, PedagogicalRequest request, Message messageReplyingToo){
        
        try{
            if(request == null || request.getRequests().isEmpty()){
                return;
            }
            
            String domainModuleAddress = domainSessionIdToDomain.get(domainSession.getDomainSessionId());
            if(domainModuleAddress != null){
                
                if(messageReplyingToo == null){
                    sendDomainSessionMessage(request, domainSession, domainSession.getDomainSessionId(), MessageTypeEnum.PEDAGOGICAL_REQUEST, null);
                }else{
                    sendReply(messageReplyingToo, request, MessageTypeEnum.PEDAGOGICAL_REQUEST);
                }
            }else{
                logger.error("Unable to send pedagogical request because a domain module was not found using domain session id of "+domainSession.getDomainSessionId()+
                        ". This is a critical error as pedagogical request are essential for tutoring, therefore requesting that this domain session be ended" +
                        " by the domain module.");
                sendCloseDomainSessionRequest(domainSession, 
                        "Unable to send a pedagogical request because the pedagogical module was unable to find information about the request's domain session.");
            }
            
        }catch(Exception e){
            logger.error("Caught exception while trying to send the pedagogical request of "+request+".  This is a critical error as pedagogical request are essential for tutoring, therefore requesting that this domain session "+domainSession+" be ended" +
                        " by the domain module.", e);
            sendCloseDomainSessionRequest(domainSession, 
                    "There was a critical error sending the pedagogical requests to the Domain module, therefore the Pedaogical module is requesting the domain session be ended.");
        }
    }
    
    /**
     * Send the close domain session request to the Domain module because a critical error has happened in 
     * this module.
     * 
     * @param domainSession - information about the domain session (including the unique user id of the learner)
     * @param reason a useful message indicating why the pedagogy is requesting that the domain session end.
     */
    public void sendCloseDomainSessionRequest(DomainSession domainSession, String reason){
        
        logger.info("Sending an unexpected close domain session request for domain session "+domainSession+" to the domain module because '"+reason+"'.");
        
        CloseDomainSessionRequest request = new CloseDomainSessionRequest(reason);
        sendDomainSessionMessage(ModuleTypeEnum.DOMAIN_MODULE, request, domainSession, domainSession.getDomainSessionId(), MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST, null);
    }

    /**
     * Create and send a Learner module status message over the network.
     */
    @Override
    public void sendModuleStatus() {
        sendMessage(SubjectUtil.PED_DISCOVERY_TOPIC, moduleStatus, MessageTypeEnum.MODULE_STATUS, null);
    }

    @Override
    protected void cleanup() {
        super.cleanup();
    }


    //NOTE: Revert to SVN r994 for legacy code containing GUI that allows loading of 
    public static void main(String[] args) {
        ModuleModeEnum mode = checkModuleMode(args);
        PedagogicalModuleProperties.getInstance().setCommandLineArgs(args);

        PedagogicalModule pModule = null;
        try{
            pModule = PedagogicalModule.getInstance();
            pModule.setModuleMode(mode);
            pModule.showModuleStartedPrompt();
            pModule.cleanup();
            pModule.cleanup();
            
        }catch(Exception e){
            System.err.println("The Pedagogical Module threw an exception.");
            e.printStackTrace();
            
            if(pModule != null){
                pModule.cleanup();
            }
            
            JOptionPane.showMessageDialog(null,
                    "The Pedagogical Module had a severe error.  Check the log file and the console window for more information.",
                    "Pedagogical Module Error",
                    JOptionPane.ERROR_MESSAGE);
            showModuleUnexpectedExitPrompt(mode);
        }
        
        if(mode.equals(ModuleModeEnum.POWER_USER_MODE)) {
            System.out.println("Good-bye");
            //kill any threads
            System.exit(0);
        }
    }   
    
}
