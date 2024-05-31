/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.conversation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.ChatLog;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.UriUtil;
import mil.arl.gift.common.io.UriUtil.InternetConnectionStatusEnum;
import mil.arl.gift.domain.DomainKnowledgeActionInterface;
import mil.arl.gift.domain.knowledge.condition.autotutor.AutoTutorWebServiceInterface;
import mil.arl.gift.domain.knowledge.condition.autotutor.AutoTutorWebServiceInterface.ActionsResponse;
import mil.arl.gift.domain.knowledge.condition.autotutor.AutoTutorWebServiceInterface.AssessmentResponse;
import mil.arl.gift.domain.knowledge.condition.autotutor.AutoTutorWebServiceInterface.Script_Reference_Type;
import mil.arl.gift.domain.knowledge.conversation.ConversationManager.ConversationAssessment;

/**
 * This class is responsible for maintaining state about a single AutoTutor conversation
 * in a single domain session.
 * 
 * @author mhoffman
 *
 */
public class AutoTutorModel implements ConversationModelInterface{
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AutoTutorModel.class);
    
    private static final String DEFAULT_CONCEPT_EXPECTATION = "AutoTutor Conversation expectation";
    private static final String DEFAULT_CONCEPT_HINT = "AutoTutor Conversation hint";
    private static final String DEFAULT_CONCEPT_PROMPT = "AutoTutor Conversation prompt";
    
    /** interface to the AT WS */
    transient private AutoTutorWebServiceInterface autoTutorSession = null;
    
    private int chatId;
    
    /** the name of script */
    private String scriptNameOrURL;
    
    /** whether the script is remote to the AT WS or not. */
    private Script_Reference_Type scriptRefType;
    
    /** stores the last chat log to (possibly) use later to retrieve the actions list from the AT WS */
    private ChatLog queuedChatLog = null;
    
//    /** flag used to indicate if a chat message has been received yet */
//    private boolean isFirstMessage = true;
    
    /** whether the autotutor session is active (e.g. has the seed prompt been given) */
    private boolean activeSession = false;
    
    /** logic for handling chat logs in a different thread */
    transient private Thread handlerThread;
    private volatile boolean keepHandling = true;
    private List<ChatLog> chatLogQueue = new ArrayList<ChatLog>();
    
    /** used for updating performance assessments for performance nodes based on conversation assessments */
    private ConversationAssessmentHandlerInterface conversationAssessmentHandler;
    
    /** used for presenting conversation updates to the learner */
    private DomainKnowledgeActionInterface domainKnowledgeActionInterface;
    
    /**
     * Set attributes for an AutoTutor SKO contained within a network resource (URL).
     * Note: this class should be created by the AutoTutorManager class, hence the lack of constructor modifier
     * 
     * @param chatId the unique conversation id for all users of this domain module instance.  Can't be a negative number.
     * @param scriptReference contains the URL to the AutoTutor SKO to use for this conversation.  Can't be null or empty.
     * @param conversationAssessmentHandler used for updating performance assessments for performance nodes based on conversation assessments.  Can't be null.
     * @param domainKnowledgeActionInterface used for presenting conversation updates to the learner.  Can't be null.
     */
    AutoTutorModel(int chatId, String scriptReference, ConversationAssessmentHandlerInterface conversationAssessmentHandler, DomainKnowledgeActionInterface domainKnowledgeActionInterface){
        
        if(chatId < 1){
            throw new IllegalArgumentException("The chat id must be greater than 0");
        }
        
        this.chatId = chatId;
        
        if(conversationAssessmentHandler == null){
            throw new IllegalArgumentException("The conversation assessment handler can't be null.");
        }
        
        this.conversationAssessmentHandler = conversationAssessmentHandler;
        
        if(domainKnowledgeActionInterface == null){
            throw new IllegalArgumentException("The domain knowledge action interface can't be null.");
        }
        
        this.domainKnowledgeActionInterface = domainKnowledgeActionInterface;
        
        if(scriptReference == null || scriptReference.isEmpty()){
            throw new IllegalArgumentException("The script name/url can't be null or empty.");
        }
        
        this.scriptNameOrURL = scriptReference;
        this.scriptRefType = Script_Reference_Type.URL;
        
    }
    
    /**
     * Set attributes for an AutoTutor SKO contained within a GIFT file.
     * Note: this class should be created by the AutoTutorManager class, hence the lack of constructor modifier
     * 
     * Deprecated as of 8/18 - new AutoTutor ACE server no longer has an API to upload a sko from a GIFT course folder
     * 
     * @param chatId the unique conversation id for all users of this domain module instance
     * @param giftScript a GIFT file that contains the AutoTutor SKO to use for this conversation instance
     * @param conversationAssessmentHandler used for updating performance assessments for performance nodes based on conversation assessments
     * @param domainKnowledgeActionInterface used for presenting conversation updates to the learner
     */
    @Deprecated
    AutoTutorModel(int chatId, FileProxy giftScript, ConversationAssessmentHandlerInterface conversationAssessmentHandler, DomainKnowledgeActionInterface domainKnowledgeActionInterface){
        
        if(chatId < 1){
            throw new IllegalArgumentException("The chat id must be greater than 0");
        }
        
        this.chatId = chatId;
        
        if(giftScript == null){
            throw new IllegalArgumentException("The script can't be null.");
        }else if(!giftScript.exists()){
            throw new IllegalArgumentException("The script must exist.");
        }
        
        this.scriptRefType = Script_Reference_Type.FILE;
        
        if(conversationAssessmentHandler == null){
            throw new IllegalArgumentException("The domain knowledge manager can't be null.");
        }
        
        this.conversationAssessmentHandler = conversationAssessmentHandler;
        
        if(domainKnowledgeActionInterface == null){
            throw new IllegalArgumentException("The domain knowledge action interface can't be null.");
        }
        
        this.domainKnowledgeActionInterface = domainKnowledgeActionInterface;        
    }
    
    /**
     * Initialize the AutoTutor web service based on the type of SKO file reference (e.g. gift file)
     * 
     * @return the first actions of the conversation
     */
    private ActionsResponse initialize(){
        autoTutorSession = new AutoTutorWebServiceInterface();
        
        switch(scriptRefType){
            
        case URL:
            autoTutorSession.initScriptByURL(scriptNameOrURL);
            break;
         // 8/18 - new AutoTutor ACE server no longer has an API to upload a sko from a GIFT course folder
//        case FILE:
//            autoTutorSession.initScriptByXML(giftScript);
//            break;
        default:
            //ERROR
            logger.error("Unhandled script reference type of "+scriptRefType+".");
        }
        
        ActionsResponse actions = autoTutorSession.getInitialActions();
        actions.setChatId(chatId);
        return actions;
    }
    
    @Override
    public void stop(){
        
        if(logger.isInfoEnabled()){
            logger.info("Stopping AutoTutor conversation...");
        }
        
        //stop the handling thread
        keepHandling = false;
        synchronized (chatLogQueue) {
            chatLogQueue.clear();
            chatLogQueue.notifyAll();
        }
        
        if(logger.isInfoEnabled()){
            logger.info("Waiting for AutoTutor conversation handler thread to finish...");
        }
        
        //wait for the thread to die
        if(Thread.currentThread() != handlerThread){
            while(handlerThread != null && handlerThread.isAlive()){
                //nothing to do but wait
            }
        }
        
        if(logger.isInfoEnabled()){
            logger.info("AT WS interface condition is stopped.");
        }
    }
    
    @Override
    public void start(){
        
        //create thread to handle incoming requests - this is to release the handle sim messages calling thread.
        handlerThread = new Thread("AutoTutor Chat "+chatId+" Handler"){
            
            private void terminateSession(String reason, String details){
                
                //have to thread this call because it will cause the stop method to be called in this class
                new Thread("ATWS Fatal Error"){
                    @Override
                    public void run() {
                        domainKnowledgeActionInterface.fatalError(reason, details);
                    }
                }.start();
            }
            
            @Override
            public void run() {
                
                if(logger.isInfoEnabled()){
                    logger.info("Starting AutoTutor conversation handler thread for conversation "+chatId+".");                
                }
                
                try{
                    ActionsResponse action = initialize();
                    domainKnowledgeActionInterface.handleDomainActionWithLearner(action);
                    activeSession = true;
                }catch(Exception e){
                    logger.error("Caught exception while trying to initialize and deliver the first part of the AutoTutor conversation for chat "+chatId, e);
                    terminateSession("Terminating course early because there was a problem with the AutoTutor server.", "There was an exception when trying to initialize the AutoTutor script with the message of '"+e.getMessage()+"'.");
                }
                
                while(keepHandling){
                    
                    synchronized (chatLogQueue) {
                        
                        //wait for a message to be added to the queue
                        try {
                            chatLogQueue.wait();
                        } catch (InterruptedException e1) {
                            logger.error("Caught exception while waiting for chat log entries.", e1);
                        }
                        
                        if(logger.isInfoEnabled()){
                            logger.info("Waking up, there are "+chatLogQueue.size()+" chat logs in the queue.");
                        }
                        
                        //process last chat log
                        if(!chatLogQueue.isEmpty()){
                            ChatLog chatLog = chatLogQueue.remove(chatLogQueue.size()-1);
                            
                            try{
                                
//                                //the first message signals that AT WS connection should be established and
//                                //the first actions retrieved.
//                                if(isFirstMessage && chatLog.getUserEntries().isEmpty()){
//                                    logger.info("Received first chat message from tutor.  Initializing AT session.");
//                                    isFirstMessage = false;
//                                    ActionsResponse response = initAutoTutorSession(scriptNameOrURL, scriptRefType);                                    
//                                    response.setChatId(chatId);
//                                    
//                                    logger.info("Requesting that this response be displayed to the user: "+response+".");
//                                    conditionActionInterface.displayInformationToUser(response);
//                                    continue;
//                                }
                                
                                if(logger.isInfoEnabled()){
                                    logger.info("Process chat log of "+chatLog);
                                }
                                
                                AssessmentResponse response = handleChatLog(chatLog);
                                if(response == null){
                                    //ERROR
                                    throw new Exception("The assessment response can't be null.");
                                }
                                
                                AssessmentLevelEnum expectationAssessment = autoTutorSession.calculateAssessment(response, AutoTutorWebServiceInterface.EXPECTATION);  
                                AssessmentLevelEnum hintAssessment = autoTutorSession.calculateAssessment(response, AutoTutorWebServiceInterface.HINT);  
                                AssessmentLevelEnum promptAssessment = autoTutorSession.calculateAssessment(response, AutoTutorWebServiceInterface.PROMPT);  
                                
                                if(logger.isInfoEnabled()){
                                    logger.info("New assessments of chat result - \n"+response);
                                } 
                                
                                //save the chat log used for this assessment
                                if(queuedChatLog != null){
                                    synchronized (queuedChatLog) {
                                        queuedChatLog = chatLog;
                                    }
                                }else{
                                    queuedChatLog = chatLog;
                                }
                                
//                                //TESTING - bypasses "GIFT tutor loop"
//                                if(response != null){
//                                    handleRequestForActions();
//                                }
                                
                                List<ConversationAssessment> assessments = new ArrayList<>();
                                ConversationAssessment conversationAssessmentExpectation = new ConversationAssessment(DEFAULT_CONCEPT_EXPECTATION, expectationAssessment);
                                ConversationAssessment conversationAssessmentHint = new ConversationAssessment(DEFAULT_CONCEPT_HINT, hintAssessment);
                                ConversationAssessment conversationAssessmentPrompt = new ConversationAssessment(DEFAULT_CONCEPT_PROMPT, promptAssessment);
                                assessments.add(conversationAssessmentExpectation);
                                assessments.add(conversationAssessmentHint);
                                assessments.add(conversationAssessmentPrompt);
                                conversationAssessmentHandler.assessPerformanceFromConversation(assessments);
                                
                            }catch(Exception e){
                                logger.error("Caught exception while trying to handle the chat log of "+chatLog, e);
                                terminateSession("Terminating course early because there was a problem with the AutoTutor server.", "While using the AutoTutor server webservice an exception occurred with the message of '"+e.getMessage()+"'.");
                            }
                        }
                    }//end sync
                }//end while
                
                logger.info("AutoTutor conversation thread has finished for chat "+chatId+".");
            }
        };
        
        handlerThread.start();
    }
    
    @Override
    public void deliverNextActions(){
        
        ActionsResponse action = getNextActions();
        domainKnowledgeActionInterface.handleDomainActionWithLearner(action);
    }
    
    /**
     * Query the AT WS interface for the next actions and then handle any actions.
     * 
     * @return the next actions to present to the learner.  Can be null if the conversation is over.
     */
    private ActionsResponse getNextActions(){
        
        if(activeSession){
            ActionsResponse actionsResponse = autoTutorSession.getActions(queuedChatLog.getLastUserEntry());
            actionsResponse.setChatId(chatId);
            
            if(logger.isInfoEnabled()){
                logger.info("Requesting that this response be displayed to the user: "+actionsResponse+".");
            }
            return actionsResponse;
        }else{
            logger.warn("The autotutor conversation of "+this+" is receiving a request for next conversational action but it has already been completed, therefore no more information will be presented to the user.");
            return null;
        }
    }

    /**
     * Update the conversation model with the learner's latest entries.
     * 
     * @param chatLog contains the learner's last entry in the chat.
     */
    void addUserResponse(ChatLog chatLog){
        
        //all interactions should be on another thread to release the handle sim msg thread.  
        synchronized(chatLogQueue){
            
            queuedChatLog = chatLog;
            
            chatLogQueue.add(chatLog);
            chatLogQueue.notifyAll();
        }
    }
    
    /**
     * Query the AT WS interface for an assessment based on the chat log.
     * 
     * @param chatLog - the current chat log
     * @return AssessmentResponse - response from the AT WS get assessments method call
     * @throws Exception if there was a problem handling the latest chat log.
     */
    private AssessmentResponse handleChatLog(ChatLog chatLog) throws Exception{
        
        if(autoTutorSession == null){
            throw new RuntimeException("The auto tutor session has not been initialized.");
        }else if(chatLog == null){
            throw new IllegalArgumentException("The chat log can't be null.");
        }else if(chatLog.getUserEntries().isEmpty()){
            throw new IllegalArgumentException("The user's entries can't be empty.");
        }
        
        String lastUserEntry = chatLog.getLastUserEntry();
        AssessmentResponse assessmentResponse = autoTutorSession.getAssessments(lastUserEntry);

        return assessmentResponse;
    }
    
    /**
     * Validate the AutoTutor SKO reference provided by checking for its existence.
     * 
     * @param scriptNameOrURL contains either a course folder relative file name or a network resource URL
     * @param courseFolder the course folder that would contains a course folder relative SKO file
     * @throws DetailedException if the AutoTutor SKO could not be found.
     */
    public static void checkAutoTutorReference(String scriptNameOrURL, AbstractFolderProxy courseFolder) throws DetailedException{
        
        //Check if the URL is a file on this computer
        try{
            if(courseFolder.fileExists(scriptNameOrURL)){ 
                return;
            }
        }catch(@SuppressWarnings("unused") IOException e){
            //maybe this isn't a local file to begin with
        }
        
        //its not a GIFT file, check it as a network resource URL
        
        try {
            UriUtil.validateUri(scriptNameOrURL, courseFolder, InternetConnectionStatusEnum.UNKNOWN);
        } catch (Exception e) {
            throw new DetailedException("Failed to validate the AutoTutor SKO reference of '"+scriptNameOrURL+"'.", "Tried checking that path as a GIFT file and as a network reachable resource.", e);
        }
    }
}
