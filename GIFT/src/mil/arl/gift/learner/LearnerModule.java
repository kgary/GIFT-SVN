/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.CloseDomainSessionRequest;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.InstantiateLearnerRequest;
import mil.arl.gift.common.LMSData;
import mil.arl.gift.common.LMSDataRequest;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.PerformanceAssessment;
import mil.arl.gift.common.PublishLessonScore;
import mil.arl.gift.common.SubmitSurveyResults;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.KnowledgeSessionsReply;
import mil.arl.gift.common.course.dkf.session.SessionMember;
import mil.arl.gift.common.course.dkf.session.TeamKnowledgeSession;
import mil.arl.gift.common.enums.ErrorEnum;
import mil.arl.gift.common.enums.MessageEncodingTypeEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.ModuleModeEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.lti.LtiLessonGradedScoreRequest;
import mil.arl.gift.common.module.AbstractModule;
import mil.arl.gift.common.python.XMLRPCPythonServerManager;
import mil.arl.gift.common.sensor.FilteredSensorData;
import mil.arl.gift.common.state.AbstractPerformanceState;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.IntermediateConceptPerformanceState;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.common.ta.state.TrainingAppState;
import mil.arl.gift.net.api.MessageCollectionCallback;
import mil.arl.gift.net.api.SubjectUtil;
import mil.arl.gift.net.api.message.ACK;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.MessageUtil;
import mil.arl.gift.net.api.message.NACK;
import mil.arl.gift.net.api.message.RawMessageHandler;
import mil.arl.gift.net.api.message.UserSessionMessage;

/**
 * The Learner Module contains the state of the learner.  
 * 
 * @author mhoffman
 *
 */
public class LearnerModule extends AbstractModule {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(LearnerModule.class);
    
    /** the singleton instance of this class */
    private static LearnerModule instance = null;
    
    /** the name of the module */
    private static final String DEFAULT_MODULE_NAME = "Learner_Module";
    
    /** 
     * The static block configuring the learner logger must be placed above the static fields that 
     * use an instance of LearnerModuleProperties to retrieve property values. If not, every time the 
     * Learner Module is initialized, log4j will display WARN statements indicating that no logger has 
     * been appended for the module.
     */
    static {
        //use Learner log4j
        PropertyConfigurator.configureAndWatch(PackageUtil.getConfiguration() + "/learner/learner.log4j.properties");
    }
    
    /** min milliseconds between sending a query for new LMS records, instead using the in-memory learner state if available */
    private static final long minLearnerRecordQueryMs = (long) (LearnerModuleProperties.getInstance().getMinDurationBetweenRecordsQuery() * 60 * 1000);
    
    /** mapping of unique user id to learner instance */
    private Map<UserSession, Learner> userIdToLearner = new HashMap<>();
    
    /** used to delay the deletion of Learner instances for a while */
    private RemovalQueueManager removalQueueManager = new RemovalQueueManager();
    
    /**
     * Return the singleton instance of this class
     *
     * @return the instance of this class
     */
    public static synchronized LearnerModule getInstance() {

        if (instance == null) {
            instance = new LearnerModule();
        }

        return instance;
    }

    /**
     * Class constructor
     */
    private LearnerModule()  {
        super(DEFAULT_MODULE_NAME, SubjectUtil.LEARNER_QUEUE_PREFIX + ADDRESS_TOKEN_DELIM + ipaddr, SubjectUtil.LEARNER_QUEUE_PREFIX + ADDRESS_TOKEN_DELIM + ipaddr + ADDRESS_TOKEN_DELIM + SubjectUtil.INBOX_QUEUE_SUFFIX, LearnerModuleProperties.getInstance());
    }

    /**
     * Perform initialization logic for the module
     * @throws IOException if there was a problem parsing the learner configuration file
     */
    @Override
    protected void init() throws IOException {
        
        //create client to send status to
        createSubjectTopicClient(SubjectUtil.LEARNER_DISCOVERY_TOPIC, false);
        
        //register a handler for notification of simulation messages
        registerTrainingAppGameStateMessageHandler(new RawMessageHandler() {
            
            @Override
            public boolean processMessage(String msg, MessageEncodingTypeEnum encodingType) {

                //received message in queue, decode it
                Message message = MessageUtil.getMessageFromString(msg, encodingType);

                handleTrainingAppStateMessage(message);

                return true;
            }
        });
        
        //start the module heartbeat
        initializeHeartbeat();
                
        // start the xmlrpc python server if it's configured to start.
        if (LearnerModuleProperties.getInstance().getStartXMLRpcPythonServer()) {
            initializeXMLRpcPythonServer();
        }       
    }
    
    /**
     * Initializes the XML RPC Python server based on the settings in the learner.properties file.
     *
     */
    private void initializeXMLRpcPythonServer() {
        // If the xml rpc python server should be started, start it here.
        try {
            
            int pythonPort = LearnerModuleProperties.getInstance().getXMLRpcPythonServerPort();
            String pythonClass = LearnerModuleProperties.getInstance().getXMLRpcPythonServerClassName();
            
            if (pythonPort != LearnerModuleProperties.PYTHON_SERVER_INVALID_PORT &&
                    !pythonClass.isEmpty()) {
                XMLRPCPythonServerManager.launchServer(pythonPort, pythonClass); 
                if(logger.isInfoEnabled()){
                    logger.info("Python server was started on port " + pythonPort + " using class name: " + pythonClass);
                }
            } else {
                logger.error("Unable to start xmlrpc python server.  Invalid settings found in the learner.properties file.  A valid nonzero port must be found, and a valid python class name must be specified.");
            }
            
        }
        catch (Exception e) {
            logger.error("Unable to start the xmlrpc python server.", e);
        }
    }

    @Override
    public ModuleTypeEnum getModuleType() {
        return ModuleTypeEnum.LEARNER_MODULE;
    }
    
    @Override
    protected void handleMessage(Message message) {
    
        MessageTypeEnum type = message.getMessageType();
        if (type == MessageTypeEnum.SENSOR_DATA) {
            handleUnfilteredSensorData(message);

        } else if (type == MessageTypeEnum.SENSOR_FILTER_DATA) {
            handleFilteredSensorData(message);

        } else if (type == MessageTypeEnum.INSTANTIATE_LEARNER_REQUEST) {
            handleInstantiateLearnerRequest(message);

        } else if (type == MessageTypeEnum.INITIALIZE_DOMAIN_SESSION_REQUEST) {
            handleInitializeDomainSessionRequest(message);
            
        } else if (type == MessageTypeEnum.INITIALIZE_LESSON_REQUEST) {
            handleInitializeLessonRequest(message);

        } else if (type == MessageTypeEnum.START_DOMAIN_SESSION) {
            handleStartDomainSessionRequest(message);

        } else if (type == MessageTypeEnum.LESSON_STARTED) {
            handleLessonStarted(message);

        } else if (type == MessageTypeEnum.LESSON_COMPLETED) {
            handleLessonCompleted(message);
            
        } else if (type == MessageTypeEnum.COURSE_STATE){
            handleCourseState(message);

        } else if (type == MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST) {
            handleCloseDomainSessionRequest(message);
            
        } else if (type == MessageTypeEnum.SUBMIT_SURVEY_RESULTS){
            handleSubmitSurveyResults(message);
            
        } else if (type == MessageTypeEnum.PUBLISH_LESSON_SCORE_REQUEST){
            handlePublishLessonScore(message);
            
        } else if (type == MessageTypeEnum.PERFORMANCE_ASSESSMENT){
            
            //DEBUG
            //Date rcv = new Date();
            
            handlePerformanceAssessment(message);
            
            //DEBUG
            //System.out.println("time = "+ (new Date().getTime() - rcv.getTime()));
           
//        } else if (type == MessageTypeEnum.LOGOUT_REQUEST){
//            handleLogoutRequest(message);

        } else if (type == MessageTypeEnum.KILL_MODULE) {
                handleKillModuleMessage(message);

        } else if (type == MessageTypeEnum.LESSON_GRADED_SCORE_REQUEST) {
            handleLessonGradedScore(message);
            
        } else if(type == MessageTypeEnum.KNOWLEDGE_SESSION_UPDATED_REQUEST){
            handleKnowledgeSessionUpdatedRequest(message);

        } else {

            logger.error(getModuleName() + " received unhandled message:" + message);
            
            if(message.needsHandlingResponse()) {
                this.sendReply(message, new NACK(ErrorEnum.UNHANDLED_MESSAGE_ERROR, "The module did not know how to handle the message"), MessageTypeEnum.PROCESSED_NACK);
            }
        }
        
    }

    /**
     * Handle an incoming publish lesson score message by adding it to the active
     * learner's course records collection.  In addition the course record
     * will be used to update the learner state.
     * 
     * @param message the publish lesson score message to handle
     */
    private void handlePublishLessonScore(Message message){
        
        DomainSessionMessage domainSessionMessage = (DomainSessionMessage)message;
        
        if(logger.isDebugEnabled()){
            logger.info(getModuleName() + " received " + message.toString() + " message");
        }
                
        //get learner instance
        if(userIdToLearner.get(domainSessionMessage.getUserSession()) != null){
            
            Learner learner = userIdToLearner.get(domainSessionMessage.getUserSession());
            
            PublishLessonScore publishLessonScore = (PublishLessonScore) message.getPayload();
            
            try{
                // apply the record to the host of a knowledge session (DKF)
                learner.addLMSCourseRecord(publishLessonScore.getCourseData(), true);
                
                // apply the record to any joiners of the knowledge session (DKF)
                AbstractKnowledgeSession knowledgeSession = learner.getCurrentKnowledgeSession();
                if(knowledgeSession != null && knowledgeSession instanceof TeamKnowledgeSession){
                    TeamKnowledgeSession tKnowledgeSession = (TeamKnowledgeSession)knowledgeSession;
                    Collection<SessionMember> joinerMembers = tKnowledgeSession.getJoinedMembers().values();
                    for(SessionMember joiner : joinerMembers){
                        
                        Learner joinerLearner = userIdToLearner.get(joiner.getUserSession());
                        if(joinerLearner != null){
                            joinerLearner.addLMSCourseRecord(publishLessonScore.getCourseData(), true);
                        }
                    }
                }
            }catch(Exception e){
                logger.error("Caught exception while trying to apply the publish lesson score:\n"+publishLessonScore, e);
                sendReply(message, new NACK(ErrorEnum.OPERATION_FAILED, "Unable to apply the publish lesson score because an exception was thrown (domain session "+domainSessionMessage.getDomainSessionId()+"."), MessageTypeEnum.PROCESSED_NACK);
            }
            
            sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
            
        }else{
            sendReply(message, new NACK(ErrorEnum.USER_NOT_FOUND_ERROR, "Unable to find the learner for user id of "+domainSessionMessage.getUserId()+"."), MessageTypeEnum.PROCESSED_NACK);
        }
    }
    
    /**
     * Handle the performance assessment message by providing the assessment to the appropriate learner instance
     * 
     * @param message - the performance assessment message
     */
    private void handlePerformanceAssessment(Message message){
        
        DomainSessionMessage domainSessionMessage = (DomainSessionMessage)message;
        
        if(logger.isDebugEnabled()){
            logger.info(getModuleName() + " received " + message.toString() + " message");
        }
                
        //get learner instance
        if(userIdToLearner.get(domainSessionMessage.getUserSession()) != null){
            
            Learner learner = userIdToLearner.get(domainSessionMessage.getUserSession());
            
            PerformanceAssessment performanceAssessment = (PerformanceAssessment)message.getPayload();
            
            learner.addPerformanceAssessment(performanceAssessment);
            
        }else{
            logger.error("Unable to process performance assessment because unable to find a learner instance for user id = "+ domainSessionMessage.getUserId());
        }

    }
    
    /**
     * Handle an incoming knowledge session updated request message by delivering it to the learner
     * instance that is hosting that knowledge session (DKF).
     * @param message the knowledge session updated request message to process
     */
    private void handleKnowledgeSessionUpdatedRequest(Message message){
        
        DomainSessionMessage domainSessionMessage = (DomainSessionMessage)message;
        
        if(logger.isDebugEnabled()){
            logger.info(getModuleName() + " received " + message.toString() + " message");
        }
                
        //get learner instance
        if(userIdToLearner.get(domainSessionMessage.getUserSession()) != null){
            
            Learner learner = userIdToLearner.get(domainSessionMessage.getUserSession());
            
            // check if the knowledge session map contains the domain session id of this learner instance
            // In the case of course object with DS log playback, only the last knowledge session update request
            // message will contain this learners DS id.  The previous instances of this message are being played back
            // from the log and therefore will contain the DS id of the message in the log.
            KnowledgeSessionsReply knowledgeSessionsReply = (KnowledgeSessionsReply)message.getPayload();            
            Map<Integer, AbstractKnowledgeSession> sessionsRequestMap = knowledgeSessionsReply.getKnowledgeSessionMap();
            AbstractKnowledgeSession knowledgeSession = sessionsRequestMap.get(domainSessionMessage.getDomainSessionId());
            if(knowledgeSession != null){
                learner.setCurrentKnowledgeSession(knowledgeSession);
            }
            
        }
    }
    
    /**
     * Handle an incoming lesson graded score message. The score will be used to update the learner
     * state.
     * 
     * @param message the lesson graded score message to handle
     */
    private void handleLessonGradedScore(Message message) {
        
        DomainSessionMessage domainSessionMessage = (DomainSessionMessage)message;
        
        if(logger.isDebugEnabled()){
            logger.info(getModuleName() + " received " + message.toString() + " message");
        }
                
        //get learner instance
        if(userIdToLearner.get(domainSessionMessage.getUserSession()) != null){
            
            Learner learner = userIdToLearner.get(domainSessionMessage.getUserSession());

            LtiLessonGradedScoreRequest request = (LtiLessonGradedScoreRequest) message.getPayload();

            learner.addSkillScore(request.getGradedScoreNode(), request.getCourseConcepts());
            
        }else{
            logger.error("Unable to process handle lesson graded score because unable to find a learner instance for user id = "+ domainSessionMessage.getUserId());
        }
    }
    
    /**
     * Handle the training app state message by providing the payload to the appropriate
     * learner instance so it can apply it to the learner state.
     * 
     * @param message - a training app state message
     */
    private void handleTrainingAppStateMessage(Message message){
        
        //get learner instance
        if(userIdToLearner.get(((UserSessionMessage)message).getUserSession()) != null){
            Learner learner = userIdToLearner.get(((UserSessionMessage)message).getUserSession());

            try{
                learner.addTrainingAppState(message.getMessageType(), (TrainingAppState) message.getPayload());
            }catch(Exception e){
                logger.error("Caught exception while trying to apply training app state", e);
            }
        }
    }
    
    /**
     * Handle the submit lesson survey results by providing the results to the
     * learner's learner instance
     * 
     * @param message - the submit lesson survey results
     */
    private void handleSubmitSurveyResults(Message message){
        
        if(logger.isDebugEnabled()){
            logger.info(getModuleName() + " received " + message);
        }

        //get learner instance
        if(userIdToLearner.get(((UserSessionMessage)message).getUserSession()) != null){
            Learner learner = userIdToLearner.get(((UserSessionMessage)message).getUserSession());

            try{
                learner.addSurveyResponse((SubmitSurveyResults) message.getPayload());
            
                sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
            } catch(DetailedException e) {
                 logger.error("Caught exception while trying to apply survey results", e);
                 
                 NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, e.getReason());
                 nack.setErrorHelp(e.getDetails());              
                 
                 sendReply(message, nack, MessageTypeEnum.PROCESSED_NACK);
            } catch(Exception e){
                logger.error("Caught exception while trying to apply survey results", e);
                sendReply(message, new NACK(ErrorEnum.OPERATION_FAILED, "Unable to apply survey results for user id of "+((UserSessionMessage)message).getUserId()), MessageTypeEnum.PROCESSED_NACK);
            }

        }else{
            logger.error("Unable to process lesson survey results because unable to find a learner instance for user id = "+((UserSessionMessage)message).getUserId());
            sendReply(message, new NACK(ErrorEnum.USER_NOT_FOUND_ERROR, "The user id of "+((UserSessionMessage)message).getUserId()+" was not found in the learner module."), MessageTypeEnum.PROCESSED_NACK);
        }
    }
    
    /**
     * Add the user to the queue for removal in the future (i.e. delete the Learner instance).
     * @param userSession information about the user, used when deleting the Learner instance
     * @return true if user session is currently mapped to a Learner instance.
     */
    private boolean queueLearnerRemoval(UserSession userSession){
        
        boolean exists = userIdToLearner.containsKey(userSession);
        if(exists){
            removalQueueManager.queueLearnerRemoval(userSession);
        }
        return exists;
    }
    
    /**
     * Cancel the removal of a Learner instance, if currently queued for removal in the future.
     * 
     * @param userSession information about the user, used to retrieve any pending queued removal task.
     * @return </br>
     * true if:</br>
     * 1. this task is scheduled for one-time execution and has not yet run</br>
     * 2. this task is scheduled for repeated execution.</br>
     * 3. the user session is not schedule for removal</br>
     * 4. the user session was in the middle of removal when this method was called, this thread waited and
     * now the removal has completed.
     * </br>
     * false if:</br>
     * 1. the task was scheduled for one-time execution and has already run</br>
     * 2. if the task was already cancelled. 
     */
    private boolean cancelQueuedRemoval(UserSession userSession){
        return removalQueueManager.cancelQueuedRemoval(userSession);
    }
        
    /**
     * Remove the learner instance for the learner with the given user id.
     * 
     * @param userSession - a learner's user id to find and remove a learner instance for
     * @return boolean - whether the learner instance was removed
     */
    private boolean removeLearner(UserSession userSession){
        
        Learner learner = userIdToLearner.remove(userSession);
        if(learner != null){
            try{
                learner.cleanup();
            }catch(Throwable t){
                logger.error("Failed to cleanup learner of "+learner, t);
            }
        }
        
        boolean removed = allocationStatus.removeUserSession(userSession);
        if(removed && logger.isDebugEnabled()){
            logger.debug("Successfully removed the following session from the learner module allocated sessions set : "+userSession);
        }
    
        return learner != null;
    }
    
//    /**
//     * Handle the logout request message by removing the appropriate learner instance
//     * 
//     * @param message - the logout request message
//     */
//    private void handleLogoutRequest(Message message){
//      
//        String errorMsg = null;
//        ErrorEnum errorEnum = null;
//        
//        logger.info(getModuleName() + " received " + message);
//
//        if(!removeLearner(((UserSessionMessage)message).getUserId())){
//          
//          errorMsg = "Unable to find learner with user id = "+((UserSessionMessage)message).getUserId();
//          errorEnum = ErrorEnum.USER_NOT_FOUND_ERROR;
//        }
//        
//        if (errorMsg != null && errorEnum != null) {
//            //send NACK
//            NACK nack = new NACK(errorEnum, errorMsg);
//
//            logger.error(getModuleName() + " sending " + nack + " to " + ModuleTypeEnum.TUTOR_MODULE);
//
//            sendReply(message, nack, MessageTypeEnum.PROCESSED_NACK);
//            
//        }else{
//            
//            logger.info("Successfully removed learner "+((UserSessionMessage)message).getUserId());
//            
//          //send ACK
//          sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
//
//        }
//    }

    /**
     * Handle the instantiate learner request message by creating a learner
     * instance based on the learner's unique user id.
     *
     * @param message - the instantiate learner request message
     */
    private void handleInstantiateLearnerRequest(final Message requestMessage) {

        String errorMsg = null;
        ErrorEnum errorEnum = null;
        int userId = ((UserSessionMessage)requestMessage).getUserId();
        InstantiateLearnerRequest learnerRequest = (InstantiateLearnerRequest)requestMessage.getPayload();
        UserSession userSession = ((UserSessionMessage)requestMessage).getUserSession();
        
        // cancel the removal of this user if queued up
        cancelQueuedRemoval(userSession);
        Learner learner = userIdToLearner.get(userSession);
                
        if(logger.isInfoEnabled()){
            logger.info(getModuleName() + " received " + requestMessage);
        }        

        try {
            if(learner == null){        
                //create new learner instance
                learner = new Learner(((UserSessionMessage)requestMessage).getUserSession(), learnerRequest.getLearnerConfig());
                if(logger.isInfoEnabled()){
                    logger.info(getModuleName() + " created NEW learner instance for " + learner.getUserSessionInfo() + ". NOTE: learner instance has not been added yet");
                }
            }
            
            getLearnerStateAttributes(requestMessage, learner);
            
        } catch(Exception e) {
            logger.error("Error creating learner instance and updating with learner records for " + userId, e);
            errorMsg = "Error creating learner instance and updating with learner records for " + userId + ": " + e.getMessage();
            errorEnum = ErrorEnum.LEARNER_INSTANTIATED_ERROR;
        }
        
        if(errorMsg != null && errorEnum != null){            
            //there was an error instantiating the learner instance, send NACK
            
            NACK nack = new NACK(errorEnum, errorMsg);
            logger.error(getModuleName() + " sending " + nack + " to " + ModuleTypeEnum.UMS_MODULE);
            sendReply(requestMessage, nack, MessageTypeEnum.PROCESSED_NACK);
        }
    }
    
    /**
     * Retrieve the LMS learner state attribute data for the learner and provide that info to the learner.
     * 
     * @param requestMessage - the original message that spawned the needed for the learner to obtain LMS records
     * @param learner - the learner that needs the LMS data
     */
    private void getLearnerStateAttributes(final Message requestMessage, final Learner learner){
        
        boolean sent = false;
        
        long timeSinceLastQuery = learner.getLastLMSQuery() != null ? System.currentTimeMillis() - learner.getLastLMSQuery().getTime() : Long.MAX_VALUE;
        if(timeSinceLastQuery > minLearnerRecordQueryMs){
            
            InstantiateLearnerRequest learnerRequest = ((InstantiateLearnerRequest)requestMessage.getPayload());
            final generated.course.Concepts courseConcepts = learnerRequest.getCourseConcepts();
            learner.setCourseConcepts(courseConcepts);
            
            MessageCollectionCallback callback = new MessageCollectionCallback() {
                
                LMSData lmsData;
                
                @Override
                public void success() {
                    if(logger.isInfoEnabled()){
                        logger.info("All LMS data was received, sending reply");
                    }
                    sendReply(requestMessage, lmsData, MessageTypeEnum.LMS_DATA_REPLY);
                }
    
                @Override
                public void received(Message msg) {
                    if (msg.getMessageType() == MessageTypeEnum.LMS_DATA_REPLY) {
                        lmsData = (LMSData) msg.getPayload();
                        learner.updateLearnerStatesFromLMS(lmsData.getAbstractScales());
                    }
                }
                
                @Override
                public void failure(String why) {
                    
                    //remove the learner instance
                    queueLearnerRemoval(((UserSessionMessage)requestMessage).getUserSession());
                      
                    NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, why);
                      
                    if(logger.isInfoEnabled()){
                        logger.info("LMS data was not received, therefore sending "+nack+" to "+ModuleTypeEnum.UMS_MODULE);
                    }
                    sendReply(requestMessage, nack, MessageTypeEnum.PROCESSED_NACK);                
                }
                
                @Override
                public void failure(Message msg) {           
                  
                    //remove the learner instance
                    queueLearnerRemoval(((UserSessionMessage)requestMessage).getUserSession());
                      
                    NACK rcvdNACK = (NACK)msg.getPayload();
                    NACK nack = new NACK(rcvdNACK.getErrorEnum(), rcvdNACK.getErrorMessage());
                      
                    if(logger.isInfoEnabled()){
                        logger.info("LMS data was not received, therefore sending "+nack+" to "+ModuleTypeEnum.UMS_MODULE);
                    }
                    sendReply(requestMessage, nack, MessageTypeEnum.PROCESSED_NACK);    
                }
            };
            
            if(logger.isDebugEnabled()){
                logger.debug("Requesting LMS data because it has been "+timeSinceLastQuery/60000+" min since the last query.");
            }
            
            //request LMS data in chunks of 10 entries

            LMSDataRequest request = new LMSDataRequest(learnerRequest.getLMSUserName());
            request.setCourseConcepts(courseConcepts);
            request.setLearnerRequest(true);
            request.setSinceWhen(learner.getLastLMSQuery());
            sent = sendUserSessionMessage(request, ((UserSessionMessage)requestMessage).getUserSession(), MessageTypeEnum.LMS_DATA_REQUEST, callback);
        }else{
            // send empty LMSData as response to satisfy the response
            sent = sendReply(requestMessage, new LMSData(), MessageTypeEnum.LMS_DATA_REPLY);
            
            if(logger.isDebugEnabled()){
                logger.debug("Not requesting LMS data because it has only been "+timeSinceLastQuery/60000+" min since the last query.");
            }
        }
        
        if(!sent){
            // unable to send message to LMS module 
            
            //- send NACK back to UMS
            
            String errorMsg = getModuleName() + " was unable to send a message to the LMS";
            NACK nack = new NACK(ErrorEnum.MODULE_NOT_FOUND, errorMsg);

            logger.error(getModuleName() + " sending " + nack + " to " + ModuleTypeEnum.UMS_MODULE);

            sendReply(requestMessage, nack, MessageTypeEnum.PROCESSED_NACK); 
            
        }else{
            userIdToLearner.put(learner.getUserSessionInfo(), learner); 
            if(logger.isInfoEnabled()){
                logger.info(getModuleName() + " added learner instance for learner of " + learner.getUserSessionInfo()); 
            }
        }

    }

    /**
     * Handle the initialized domain session request message
     * 
     * @param message - the initialized domain session request message
     */
    private void handleInitializeDomainSessionRequest(Message message) {

        if(logger.isInfoEnabled()){
            logger.info(getModuleName() + " received " + message.toString() + " message");
        }
        
        DomainSessionMessage domainSessionMsg = (DomainSessionMessage)message;
        if (userIdToLearner.containsKey(domainSessionMsg.getUserSession())) {
            Learner learner = userIdToLearner.get(domainSessionMsg.getUserSession());
            DomainSession dSession = new DomainSession(domainSessionMsg.getDomainSessionId(), domainSessionMsg.getUserId(), DomainSession.UNKNOWN_DOMAIN_NAME, DomainSession.UNKNOWN_DOMAIN_NAME);
            dSession.copyFromUserSession(domainSessionMsg.getUserSession());
            try{
                learner.setCurrentDomainSession(dSession);
            }catch(Throwable t){
                logger.error("Failed to notify of new domain session - "+learner, t);
                NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "Failed to successfully handle updating the learner model that a new domain session was about to begin");
                nack.setErrorHelp(t.toString());
                sendReply(message, nack, MessageTypeEnum.PROCESSED_NACK);
                return;
            }
            
            sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
            
        }else{
            logger.error("Unable to process intialize domain session request because unable to find a learner instance for user id = "+domainSessionMsg.getUserId());
            sendReply(message, new NACK(ErrorEnum.USER_NOT_FOUND_ERROR, "The user id of "+domainSessionMsg.getUserId()+" was not found in the learner module."), MessageTypeEnum.PROCESSED_NACK);
    }

    }

    /**
     * Handle the start domain session request message
     * 
     * @param message - the start domain session request message
     */
    private void handleStartDomainSessionRequest(Message message) {
        
        if(logger.isInfoEnabled()){
            logger.info(getModuleName() + " received " + message.toString() + " message");      
        }

        sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
    }
    
    /**
     * Handle the initialize lesson request message
     * 
     * @param message - the initialized lesson request message
     */
    private void handleInitializeLessonRequest(Message message) {
        
        if(logger.isInfoEnabled()){
            logger.info(getModuleName() + " received " + message.toString() + " message");
        }

        // send pedagogical model the learner state
        DomainSessionMessage domainSessionMsg = (DomainSessionMessage) message;
        if (userIdToLearner.containsKey(domainSessionMsg.getUserSession())) {
            Learner learner = userIdToLearner.get(domainSessionMsg.getUserSession());
            learner.init();
        }

        sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
    }

    /**
     * Handle the lesson started message
     * 
     * @param message - the lesson started message
     */
    private void handleLessonStarted(Message message) {

        if(logger.isInfoEnabled()){
            logger.info(getModuleName() + " received " + message.toString() + " message");
        }
        
        //clear learner's last performance state so it doesn't conflict with incoming performance assessment nodes
        DomainSessionMessage domainSessionMsg = (DomainSessionMessage)message;
        if (userIdToLearner.containsKey(domainSessionMsg.getUserSession())) {
            Learner learner = userIdToLearner.get(domainSessionMsg.getUserSession());
            try{
                learner.lessonStarted();
            }catch(Throwable t){
                logger.error("Failed to notify of new knowledge session - "+learner, t);
                NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "Failed to successfully handle updating the learner model that a new knowledge lesson was about to begin");
                nack.setErrorHelp(t.toString());
                sendReply(message, nack, MessageTypeEnum.PROCESSED_NACK);
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
        if(logger.isInfoEnabled()){
            logger.info(getModuleName() + " received " + message.toString() + " message");
        }
        
        //clear learner's last performance state so it doesn't conflict with incoming performance assessment nodes
        DomainSessionMessage domainSessionMsg = (DomainSessionMessage)message;
        if (userIdToLearner.containsKey(domainSessionMsg.getUserSession())) {
            Learner learner = userIdToLearner.get(domainSessionMsg.getUserSession());
            try{
                learner.lessonCompleted();
            }catch(Throwable t){
                logger.error("Failed to notify of a completed knowledge session - "+learner, t);
                NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "Failed to successfully handle updating the learner model of a completed knowledge lesson");
                nack.setErrorHelp(t.toString());
                sendReply(message, nack, MessageTypeEnum.PROCESSED_NACK);
                return;
            }
        }
        
        sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
    }
    
    /**
     * Handle an incoming course state by resetting the current knowledge session information.
     * @param message received course state information
     */
    public void handleCourseState(Message message){
        if(logger.isInfoEnabled()){
            logger.info(getModuleName() + " received " + message.toString() + " message");
        }
        
        sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
        
        DomainSessionMessage dsMsg = (DomainSessionMessage)message;
        
        // find learner instance
        Learner learner = userIdToLearner.get(dsMsg.getUserSession());
        
        if(learner != null){
      
            // a course state message happens right before the next course object is started.
            // This means the end of a lesson/dkf/knowledge-session.
            // Therefore this is a good time to reset the current knowledge session 
            // NOTE: lesson completed message happens before publish score message so can't use that message
            //       There are also two publish score messages at the end of a dkf (course completed percent and overall assessment)
            learner.setCurrentKnowledgeSession(null);
        }
    }

    /**
     * Handle the close domain session request message
     * 
     * @param message - the close domain session request message
     */
    private void handleCloseDomainSessionRequest(Message message) {

        if(logger.isInfoEnabled()){
            logger.info(getModuleName() + " received " + message);
        }
        
        try{
            if(!queueLearnerRemoval(((UserSessionMessage)message).getUserSession())){
                //send NACK
                
                String errorMsg = "Unable to find learner with user id = "+((UserSessionMessage)message).getUserId();
                
                //MH: do we really care if the learner instance doesn't exist.  The close could be when a learner instance
                //    isn't needed anyway.
                if(logger.isInfoEnabled()){
                    logger.info("While handling close domain session request - "+errorMsg);
                }
                sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
              
            }else{        
                sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
            } 
        }catch(Throwable t){
            logger.error("Caught error while trying to cleanup learner instance for close domain session request of\n"+message, t);
        }
        
        try{ 
            releaseDomainSessionModules(((UserSessionMessage)message).getUserSession());
        }catch(Throwable t){
            logger.error("Caught error while trying to release domain session modules for close domain session request of\n"+message, t);
        }
        
        if(logger.isInfoEnabled()){
            logger.info("Finished closing domain session.");
        }
    }

    /**
     * Handle the raw sensor data message by ...
     *
     * @param message - the raw sensor data message containing unfiltered sensor data
     */
    private void handleUnfilteredSensorData(Message message) {

        if (logger.isInfoEnabled()) {
            logger.info(getModuleName() + " received " + message);
        }

        logger.error("Sensor Data message handling not implemented yet");
    }

    /**
     * Handle the sensor filter data message by ...
     *
     * @param message - filtered sensor data message containing filtered sensor data
     */
    private void handleFilteredSensorData(Message message) {

        if (logger.isInfoEnabled()) {
            logger.info(getModuleName() + " received " + message);
        }
        
        //find learner instance
        Learner learner = userIdToLearner.get(((UserSessionMessage)message).getUserSession());
        
        if(learner != null){
            
            learner.addFilteredSensorData(((DomainSessionMessage)message).getDomainSessionId(), (FilteredSensorData)message.getPayload());
            
        }else{

            logger.error("Not processing filtered sensor data because learner module was unable to find learner with user id = "+((UserSessionMessage)message).getUserId());
        }
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

    @Override
    public void sendModuleStatus() {
        sendMessage(SubjectUtil.LEARNER_DISCOVERY_TOPIC, moduleStatus, MessageTypeEnum.MODULE_STATUS, null);
    }
    
    /**
     * Send the learner state
     * 
     * @param userSession - information about the user session (including the unique user id of the learner) 
     *                      the state is associated with
     * @param domainSession - current domain session for the user
     * @param learnerState - latest state to send
     */
    public void sendLearnerState(UserSession userSession, DomainSession domainSession, LearnerState learnerState){
        sendDomainSessionMessage(learnerState, userSession, domainSession.getDomainSessionId(), MessageTypeEnum.LEARNER_STATE, null);

        /* Clear change reasons in performance after it has been sent */
        if (learnerState != null && learnerState.getPerformance() != null
                && learnerState.getPerformance().getTasks() != null) {
            for (TaskPerformanceState taskState : learnerState.getPerformance().getTasks().values()) {
                clearStateChangeReason(taskState);
            }
        }
    }

    /**
     * Clears the change reason from the learner state performance state and all
     * descendents. This should be performed after the learner state has been
     * sent since we do not want to persist the reason across future learner
     * state messages.
     * 
     * @param performanceState the performance state that should have its and
     *        its descedents' reason cleared out.
     */
    private void clearStateChangeReason(AbstractPerformanceState performanceState) {
        if (performanceState == null) {
            return;
        }

        /* Clear 'reason' from state */
        if (performanceState.getState() != null) {
            performanceState.getState().setObserverComment(null);
        }
        
        /* Clear observer media from state */
        if (performanceState.getState() != null) {
            performanceState.getState().setObserverMedia(null);
        }

        /* Find children */
        List<ConceptPerformanceState> childConcepts = null;
        if (performanceState instanceof TaskPerformanceState) {
            TaskPerformanceState tState = (TaskPerformanceState) performanceState;
            childConcepts = tState.getConcepts();
        } else if (performanceState instanceof IntermediateConceptPerformanceState) {
            IntermediateConceptPerformanceState icState = (IntermediateConceptPerformanceState) performanceState;
            childConcepts = icState.getConcepts();
        }

        /* Perform clear on children */
        if (childConcepts != null) {
            for (ConceptPerformanceState cState : childConcepts) {
                clearStateChangeReason(cState);
            }
        }
    }

    /**
     * Send the close domain session request to the Domain module because a critical error has happened in 
     * this module.
     * 
     * @param userSession - information about the user session (including the unique user id of the learner)
     * @param domainSession current domain session for the user
     * @param reason a useful message indicating why the learner is requesting that the domain session end.
     */
    public void sendCloseDomainSessionRequest(UserSession userSession, DomainSession domainSession, String reason){
        
        if(logger.isInfoEnabled()){
            logger.info("Sending an unexpected close domain session request for domain session "+domainSession+" to the domain module because '"+reason+"'.");
        }
        
        CloseDomainSessionRequest request = new CloseDomainSessionRequest(reason);
        sendDomainSessionMessage(ModuleTypeEnum.DOMAIN_MODULE, request, userSession, domainSession.getDomainSessionId(), MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST, null);
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        
        // shutdown the python xml rpc server (if it was started).
        XMLRPCPythonServerManager.destroyServiceProcess();
        if(logger.isInfoEnabled()){
            logger.info("Python XML rpc server was shutdown.");
        }
        
    }
    
    /**
     * Used to run the Learner Module
     * 
     * @param args - launch module arguments
     */
    public static void main(String[] args) {
        ModuleModeEnum mode = checkModuleMode(args);
        LearnerModuleProperties.getInstance().setCommandLineArgs(args);

        LearnerModule lModule = null;
        try{
            lModule = LearnerModule.getInstance();
            lModule.init();
            lModule.setModuleMode(mode);
            lModule.showModuleStartedPrompt();
            lModule.cleanup();
            
        }catch(Exception e){
            logger.error("Caught exception while instantiating learner module. ", e);
            e.printStackTrace();        
            
            if(lModule != null){
                lModule.cleanup();
            }
            
            JOptionPane.showMessageDialog(null,
                    "The Learner Module had a severe error.  Check the log file and the console window for more information.",
                    "Learner Module Error",
                    JOptionPane.ERROR_MESSAGE); 
            showModuleUnexpectedExitPrompt(mode);
        }

        if(mode.equals(ModuleModeEnum.POWER_USER_MODE)) {
            System.out.println("Good-bye");
            //kill any threads
            System.exit(0);
        }
    }
    
    /**
     * Manages removal requests for a user.  Removal means that the Learner instance is deleted by calling removeLearner method.
     * 
     * @author mhoffman
     *
     */
    private class RemovalQueueManager{
        
        /** used to manage the scheduling of removal tasks */
        private Timer timer = new Timer("RemovalQueueManagerTimer");        
        
        /** handle to the remove timer tasks, one per user session */
        private Map<UserSession, RemoveTimerTask> userToRemovalTask = new HashMap<>();
        
        /** milliseconds until the learner should be deleted from being in-memory after not having start a course in this time limit */
        private final long timeUntilRemovalMS = (long) (LearnerModuleProperties.getInstance().getStaleLearnerCleanupDuration() * 60 * 60 * 1000);
        
        /** semaphore used to manage synchronization when the removal timer task is running
         * and at the same time a request to cancel is received.  The cancel must wait. */
        private Object REMOVAL_LOCK = new Object();

        /**
         * default
         */
        public RemovalQueueManager(){ }
        
        /**
         * Add the user session to the queue for removal in the future.
         * 
         * @param userSession contains information about the user potentially being removed in the future.  If
         * null this method does nothing.
         */
        public void queueLearnerRemoval(UserSession userSession){
            
            if(userSession == null){
                return;
            }else if(userToRemovalTask.containsKey(userSession)){
                cancelQueuedRemoval(userSession);                
            }
            
            RemoveTimerTask removeTimerTask = new RemoveTimerTask(userSession);
            timer.schedule(removeTimerTask, timeUntilRemovalMS);
            userToRemovalTask.put(userSession, removeTimerTask);
            
            if(logger.isDebugEnabled()){
                logger.debug("Scheduled stale learner removal timer task for "+userSession);
            }
        }
        
        /**
         * Cancels a pending removal task for this user if it exists.  If the removal task is currently running,
         * this cancel request will wait until the removal has completed and will return true.
         * @param userSession information about the user, used to retrieve any pending queued removal task.
         * @return </br>
         * true if:</br>
         * 1. this task is scheduled for one-time execution and has not yet run</br>
         * 2. this task is scheduled for repeated execution.</br>
         * 3. the user session is not schedule for removal</br>
         * 4. the user session was in the middle of removal when this method was called, this thread waited and
         * now the removal has completed.
         * </br>
         * false if:</br>
         * 1. the task was scheduled for one-time execution and has already run</br>
         * 2. if the task was already cancelled. 
         */
        public boolean cancelQueuedRemoval(UserSession userSession){
            
            synchronized(REMOVAL_LOCK){
                RemoveTimerTask removeTimerTask = userToRemovalTask.remove(userSession);
                if(removeTimerTask != null){
                    if(logger.isDebugEnabled()){
                        logger.debug("Canceling stale learner removal timer task for "+userSession);
                    }
                    return removeTimerTask.cancel();
                }
            }
            
            return true;
        }
        
        /**
         * The timer task responsible to removing the learner instance from the learner module.
         * 
         * @author mhoffman
         *
         */
        private class RemoveTimerTask extends TimerTask{
            
            /** information about the user being removed */
            private UserSession userSession;
            
            /**
             * Creates a new timer task that will remove the learner instance from the given user session
             * 
             * @param userSession the user session of the learner instance to remove. Cannot be null.
             */
            public RemoveTimerTask(UserSession userSession) {
                
                if(userSession == null) {
                    throw new IllegalArgumentException("The user session associated with a removal timer cannot be null");
                }
                
                this.userSession = userSession;
            }

            @Override
            public void run() {
                synchronized(REMOVAL_LOCK){
                    if(logger.isDebugEnabled()){
                        logger.debug("The remove timer task has fired, removing stale learner for "+userSession);
                    }
                    removeLearner(userSession);
                    userToRemovalTask.remove(userSession);
                }
            }
            
            
        }
    }
}
