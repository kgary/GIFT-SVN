/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.log4j.PropertyConfigurator;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.BranchPathHistory;
import mil.arl.gift.common.DomainSelectionRequest;
import mil.arl.gift.common.GetExperimentRequest;
import mil.arl.gift.common.GetKnowledgeAssessmentSurveyRequest;
import mil.arl.gift.common.GetSurveyRequest;
import mil.arl.gift.common.LoginRequest;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.SubmitSurveyResults;
import mil.arl.gift.common.SurveyCheckResponse;
import mil.arl.gift.common.SurveyListCheckRequest;
import mil.arl.gift.common.UserData;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.UserSessionType;
import mil.arl.gift.common.enums.ErrorEnum;
import mil.arl.gift.common.enums.GenderEnum;
import mil.arl.gift.common.enums.MessageEncodingTypeEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.ModuleModeEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.experiment.DataCollectionItem;
import mil.arl.gift.common.experiment.ExperimentUtil.DataSetType;
import mil.arl.gift.common.experiment.ExperimentUtil.ExperimentStatus;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.ProhibitedUserException;
import mil.arl.gift.common.lti.LtiGetUserRequest;
import mil.arl.gift.common.lti.LtiUserId;
import mil.arl.gift.common.lti.LtiUserRecord;
import mil.arl.gift.common.module.AbstractModule;
import mil.arl.gift.common.sensor.SensorFileCreated;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.SurveyGiftData;
import mil.arl.gift.common.usersession.LtiUserSessionDetails;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.SubjectUtil;
import mil.arl.gift.net.api.message.ACK;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.MessageUtil;
import mil.arl.gift.net.api.message.NACK;
import mil.arl.gift.net.api.message.RawMessageHandler;
import mil.arl.gift.net.api.message.UserSessionMessage;
import mil.arl.gift.ums.db.HibernateObjectConverter;
import mil.arl.gift.ums.db.HibernateObjectReverter;
import mil.arl.gift.ums.db.UMSDatabaseException;
import mil.arl.gift.ums.db.UMSDatabaseManager;
import mil.arl.gift.ums.db.UserNotFoundException;
import mil.arl.gift.ums.db.survey.Surveys;
import mil.arl.gift.ums.db.table.DbBranchPathHistory;
import mil.arl.gift.ums.db.table.DbDataCollection;
import mil.arl.gift.ums.db.table.DbDomainSession;
import mil.arl.gift.ums.db.table.DbEventFile;
import mil.arl.gift.ums.db.table.DbExperimentSubject;
import mil.arl.gift.ums.db.table.DbGlobalUser;
import mil.arl.gift.ums.db.table.DbLtiUserRecord;
import mil.arl.gift.ums.db.table.DbSensorFile;
import mil.arl.gift.ums.db.table.DbSurveyResponse;
import mil.arl.gift.ums.db.table.DbUser;
import mil.arl.gift.ums.logger.MessageLogManager;

/**
 * This class is the User Management Session module which is responsible for
 * various components of a user's session including login services, database
 * queries and domain session services.
 *
 * @author mhoffman
 *
 */
public class UMSModule extends AbstractModule {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(UMSModule.class);

    /** the singleton instance of this class */
    private static UMSModule instance = null;

    /** the name of the module */
    private static final String DEFAULT_MODULE_NAME = "UMS_Module";

    /** instance of the db manager */
    private UMSDatabaseManager dbMgr;

    /** instance of the message logger which logs all messages placed in the logger
     * queue */
    private MessageLogManager msgLogger;

    static {
        //use UMS log4j
        PropertyConfigurator.configureAndWatch(PackageUtil.getConfiguration() + "/ums/ums.log4j.properties");
    }

    /**
     * Return the singleton instance of this class
     *
     * @return UMSModule
     */
    public static UMSModule getInstance() {

        if (instance == null) {
            instance = new UMSModule();
        }

        return instance;
    }

    /**
     * Class constructor
     */
    private UMSModule() {
        super(DEFAULT_MODULE_NAME, SubjectUtil.UMS_QUEUE_PREFIX + ADDRESS_TOKEN_DELIM + ipaddr, SubjectUtil.UMS_QUEUE_PREFIX + ADDRESS_TOKEN_DELIM + ipaddr + ADDRESS_TOKEN_DELIM + SubjectUtil.INBOX_QUEUE_SUFFIX, UMSModuleProperties.getInstance());

        if (logger.isInfoEnabled()) {
            logger.info("Initializing UMS module...");
        }
        
        init();
        
        if (logger.isInfoEnabled()) {
            logger.info("UMS module initialized.");
        }
    }

    /**
     * Perform initialization logic for the module
     */
    @Override
    protected void init() {

        initializeDB();

        msgLogger = new MessageLogManager(UMSModuleProperties.getInstance().getSystemMsgLogDuration());

        // Initialize the logger
        createSubjectQueueClient(SubjectUtil.LOGGER_QUEUE, new RawMessageHandler() {

            @Override
            public boolean processMessage(String msg, MessageEncodingTypeEnum encodingType) {

                //received message in queue, decode it
                Message message = MessageUtil.getMessageFromString(msg, encodingType);

                //FYI. this is a message logger, no need to ACK/NACK message

                handleLogMessage(message, msg, encodingType);

                return true;
            }
        }, true);
        
        //create client to send UMS status too
        createSubjectTopicClient(SubjectUtil.UMS_DISCOVERY_TOPIC, false);

        //start the module heartbeat
        initializeHeartbeat();
    }

    @Override
    public ModuleTypeEnum getModuleType() {
        return ModuleTypeEnum.UMS_MODULE;
    }

    /**
     * Initialize the database connection
     */
    private void initializeDB() {

        try{
            dbMgr = UMSDatabaseManager.getInstance();
        }catch(Throwable t){
            throw new RuntimeException("Failed to connection to the database.", t);
        }
    }

    /**
     * Handle the message to be logged
     *
     * @param message - the received message which needs to be acted upon
     * @param rawMsg - the encoding raw message
     * @param encodingType - the type of codec used on this message
     */
    protected void handleLogMessage(Message message, String rawMsg, MessageEncodingTypeEnum encodingType) {
        msgLogger.handleMessage(message, rawMsg, encodingType);
    }

    @Override
    protected void handleMessage(Message message) {

        MessageTypeEnum type = message.getMessageType();

        if (type == MessageTypeEnum.LOGIN_REQUEST) {
            handleLoginRequestMessage(message);

        } else if (type == MessageTypeEnum.NEW_USER_REQUEST) {
            handleNewUserRequestMessage(message);

        } else if (type == MessageTypeEnum.LOGOUT_REQUEST) {
            handleLogoutRequestMessage(message);

        } else if (type == MessageTypeEnum.DOMAIN_SELECTION_REQUEST) {
            handleDomainSelectionRequestMessage(message);

        } else if (type == MessageTypeEnum.GET_SURVEY_REQUEST) {
            handleGetSurveyRequest(message);

        } else if (type == MessageTypeEnum.SUBMIT_SURVEY_RESULTS) {
            handleSubmitSurveyResults(message);

        } else if (type == MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST) {
            handleCloseDomainSessionRequest(message);

        } else if (type == MessageTypeEnum.START_DOMAIN_SESSION) {
            handleStartDomainSessionRequest(message);

        } else if (type == MessageTypeEnum.INITIALIZE_DOMAIN_SESSION_REQUEST) {
            handleInitializeDomainSessionRequest(message);

        } else if (type == MessageTypeEnum.KILL_MODULE) {
            handleKillModuleMessage(message);

        } else if (type == MessageTypeEnum.SENSOR_FILE_CREATED) {
            handleSensorFileCreated(message);
            
        } else if (type == MessageTypeEnum.SURVEY_CHECK_REQUEST) {
            handleSurveyCheckRequest(message);
            
        } else if (type == MessageTypeEnum.DOMAIN_SESSION_START_TIME_REQUEST) {
        	 handleDomainSessionStartTimeRequest(message);
        	 
        } else if (type == MessageTypeEnum.USER_ID_REQUEST) {            
            handleUserIdRequestMessage(message);
            
        } else if (type == MessageTypeEnum.GET_EXPERIMENT_REQUEST) {
            handleGetExperimentRequest(message);
            
        } else if (type == MessageTypeEnum.BRANCH_PATH_HISTORY_REQUEST){
            handleBranchPathHistoryRequest(message);
            
        } else if (type == MessageTypeEnum.BRANCH_PATH_HISTORY_UPDATE){
            handleBranchPathHistoryUpdate(message);
        } else if (type == MessageTypeEnum.LTI_GETUSER_REQUEST) {
            handleLtiGetUserRequest(message);
            
        } else if (type == MessageTypeEnum.KNOWLEDGE_ASSESSMENT_DETAILS) {
            /* Nothing to do here. This message is only sent for logging purposes.*/
            
        } else {

            logger.error(getModuleName() + " received unhandled message:" + message);
            
            if(message.needsHandlingResponse()) {
                this.sendReply(message, new NACK(ErrorEnum.UNHANDLED_MESSAGE_ERROR, "The module did not know how to handle the message"), MessageTypeEnum.PROCESSED_NACK);
            }
        }
    }

    /**
     * Handle the sensor file created message by creating the appropriate
     * entries in the database
     *
     * @param message - the sensor file created message
     */
    private void handleSensorFileCreated(Message message) {

        if (logger.isInfoEnabled()) {
            logger.info(getModuleName() + " received " + message.toString() + " message");
        }

        //find domain session entry
        DbDomainSession dSession = null;
        try {
            dSession = dbMgr.selectRowById(((DomainSessionMessage) message).getDomainSessionId(), DbDomainSession.class);
        } catch (Exception e) {
            displayDBProblemMessage();
            logger.error("Caught exception while selected DB row", e);
        }

        if (dSession != null) {

            //create Sensor File entry
            SensorFileCreated s = (SensorFileCreated) message.getPayload();
            DbSensorFile sFile = new DbSensorFile(dSession, s.getSensorType().getName(), s.getFileName());

            try{
                dbMgr.insertRow(sFile);
                //update domain session entry

                Set<DbSensorFile> sFiles = dSession.getSensorFiles();
                if (sFiles == null) {
                    sFiles = new HashSet<DbSensorFile>();
                }

                sFiles.add(sFile);

                if (dbMgr.updateRow(dSession)) {

                    if (logger.isInfoEnabled()) {
                        logger.info("Sensor File successfully added to the database and the domain session was updated");
                    }

                } else {
                    logger.error("Updating the domain session with id = " + dSession.getSessionId() + " failed for " + message.toString());
                }
                
            }catch(Exception e){
                logger.error("The database insert failed for the new sensor file entry = " + sFile, e);
            }
        } else {
            logger.error(getModuleName() + " unable to create a Sensor File entry in the database because unable to find a domain session entry for the domain session referenced by " + message.toString());
        }
    }

    /**
     * Handle the Start Domain session request message by setting the start time
     * for the domain session entry in the db.
     *
     * @param message - the start domain session request message
     */
    public void handleStartDomainSessionRequest(Message message) {

        String errorMsg = null;
        ErrorEnum errorEnum = null;
        DomainSessionMessage domainSessionMessage = (DomainSessionMessage) message;

        if (logger.isInfoEnabled()) {
            logger.info(getModuleName() + " received " + message.toString() + " message");
        }

        DbDomainSession dSession = null;
        try {
            dSession = dbMgr.selectRowById(domainSessionMessage.getDomainSessionId(), DbDomainSession.class);
        } catch (Exception e) {
            displayDBProblemMessage();
            logger.error("Caught exception while selected DB row", e);
        }

        if (dSession != null) {
            //set the domain session start timestamp, then update the db entry

            dSession.setStartTime(new Date());

            if (!dbMgr.updateRow(dSession)) {
                //create error
                errorMsg = "Unable to update the domain session with id = " + dSession.getSessionId();
                errorEnum = ErrorEnum.DB_UPDATE_ERROR;
            }
        } else {
            //create error
            errorMsg = "Unable to find a domain session with session id = " + domainSessionMessage.getDomainSessionId();
            errorEnum = ErrorEnum.DOMAIN_SESSION_NOT_FOUND_ERROR;
        }

        if (errorMsg != null && errorEnum != null) {
            //send NACK
            NACK nack = new NACK(errorEnum, errorMsg);

            logger.error(getModuleName() + " sending " + nack.toString() + " to " + ModuleTypeEnum.TUTOR_MODULE);

            sendReply(message, nack, MessageTypeEnum.PROCESSED_NACK);
        } else {
            //send ack
            sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
        }
    }

    /**
     * Handle the close domain session request message by setting the end
     * timestamp for the domain session specified by the message.
     *
     * @param message - the close domain session request message
     */
    public void handleCloseDomainSessionRequest(Message message) {

        String errorMsg = null;
        ErrorEnum errorEnum = null;

        if (logger.isInfoEnabled()) {
            logger.info(getModuleName() + " received " + message);
        }

        DbDomainSession dSession = null;
        try {
            dSession = dbMgr.selectRowById(((DomainSessionMessage) message).getDomainSessionId(), DbDomainSession.class);
        } catch (Exception e) {
            displayDBProblemMessage();
            logger.error("Caught exception while selected DB row", e);
        }

        if (dSession != null) {
            //set the domain session end timestamp, then update the db entry

            dSession.setEndTime(new Date());

            if (!dbMgr.updateRow(dSession)) {
                //create error
                errorMsg = "Unable to update the domain session with id = " + dSession.getSessionId();
                errorEnum = ErrorEnum.DB_UPDATE_ERROR;
            }
        } else {
            //create error
            errorMsg = "Unable to find a domain session with session id = " + ((DomainSessionMessage) message).getDomainSessionId();
            errorEnum = ErrorEnum.DOMAIN_SESSION_NOT_FOUND_ERROR;
        }

        if (errorMsg != null && errorEnum != null) {
            //send NACK		

            NACK nack = new NACK(errorEnum, errorMsg);
            logger.error(getModuleName() + " sending " + nack + " to " + ModuleTypeEnum.TUTOR_MODULE);

            sendReply(message, nack, MessageTypeEnum.PROCESSED_NACK);
        } else {
            //send ack
            sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
        }
    }

    /**
     * Handle the during lesson survey answers by storing them in the database.
     *
     * @param message - the lesson survey results message
     */
    public void handleSubmitSurveyResults(Message message) {

        if (logger.isInfoEnabled()) {
            logger.info(getModuleName() + " received " + message);
        }

        Session session = dbMgr.createNewSession();

        session.beginTransaction();

        try {
            DomainSessionMessage dmMessage = (DomainSessionMessage) message;
            SubmitSurveyResults surveyResults = (SubmitSurveyResults) dmMessage.getPayload();
            
            HibernateObjectConverter giftToHibernateSurvey = new HibernateObjectConverter(UMSDatabaseManager.getInstance());
            DbSurveyResponse dbSurveyResponse = giftToHibernateSurvey.convertSurveyResponse(surveyResults.getSurveyResponse(), dmMessage.getUserSession(), session, dmMessage.getDomainSessionId());

            try{
                dbMgr.insertRow(dbSurveyResponse, session);

                session.getTransaction().commit();

                session.close();

                //send ack			
                sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);

            } catch(Exception e) {

                session.getTransaction().rollback();

                logger.error("Could not insert survey response, the survey response could not be inserted: " + dbSurveyResponse, e);

                session.close();

                NACK nack = new NACK(ErrorEnum.DB_INSERT_ERROR, "Could not insert survey response, the survey response could not be inserted");

                logger.error(getModuleName() + " sending " + nack + " to " + ModuleTypeEnum.TUTOR_MODULE);

                sendReply(message, nack, MessageTypeEnum.PROCESSED_NACK);
            }

        } catch (Throwable e) {

            session.getTransaction().rollback();

            logger.error("Caught an exception while inserting a survey response", e);

            session.close();

            NACK nack = new NACK(ErrorEnum.DB_INSERT_ERROR, "Caught an exception while inserting a survey response");

            logger.error(getModuleName() + " sending " + nack + " to " + ModuleTypeEnum.TUTOR_MODULE);

            sendReply(message, nack, MessageTypeEnum.PROCESSED_NACK);
        }
    }
    
    /**
     * Async handler to check the survey references.  The Surveys.checkSurveyReferences() method can take some time, 
     * so we're making it asynchronous so the UMS module is not tied up by a single request.
     * 
     * @author nblomberg
     *
     */
    private class SurveyCheckRequestAsyncHandler extends Thread {
        
        /** The original message to be processed and replied to.  The payload should be a type of SurveyListCheckRequest object. */
        private Message baseMessage;
        
        /**
         * Constructor
         * 
         * @param baseMessage Required parameter.   The original message to be processed and replied to.  The payload should be a type of SurveyListCheckRequest object.
         */
        public SurveyCheckRequestAsyncHandler(final Message baseMessage) {
            super("SurveyCheckRequestAsyncHandler-" + System.currentTimeMillis());

            if (baseMessage == null) {
                throw new IllegalArgumentException("The message can't be null.");
            }
            
            this.baseMessage = baseMessage;
        }
        
        @Override
        public void run() {
            
            // The logic below was originally in the handleSurveyCheckRequest() method, but was moved into a separate thread
            // so that the UMS module is not stalled by long running operation.
            try{        
                
                
                SurveyListCheckRequest request = (SurveyListCheckRequest) baseMessage.getPayload();
                if (logger.isInfoEnabled()) {
                    logger.info("Handling survey check request of " + request);
                }
                
                SurveyCheckResponse response = Surveys.checkSurveyReferences(request);
                sendReply(
                        baseMessage,
                        response,
                        MessageTypeEnum.SURVEY_CHECK_RESPONSE); 
                
            }catch(Exception e){
                logger.error("Caught exception while handling a survey check request.", e);
                
                sendReply(
                        baseMessage,
                        new NACK(ErrorEnum.OPERATION_FAILED,
                        "An exception was caught while trying to handle the survey check request."),
                        MessageTypeEnum.PROCESSED_NACK);
            }
        }
        
    }
    
    
    /**
     * Handle a survey check request message by checking the various survey elements against
     * the database values.  A single reply message will be sent for this request, even if the request message
     * contains multiple requests. 
     * 
     * @param message - the survey check request message
     */
    private void handleSurveyCheckRequest(Message message){
        
        SurveyCheckRequestAsyncHandler handler = new SurveyCheckRequestAsyncHandler(message);
        handler.start(); // releases the calling thread
        
    }
    
    
    /**
     * Async handler to get the survey request.  The get survey opration could take a bit, 
     * so for now we're making it asynchronous so the UMS module is not tied up by a single request.
     * 
     * @author nblomberg
     *
     */
    private class GetSurveyRequestAsyncHandler extends Thread {
        
        /** The base message to be processed and responded to. The payload should be a type of GetSurveyRequest object. */
        private Message baseMessage;


        /**
         * Constructor
         * 
         * @param baseMessage - Required.  The base message to be processed and responded to.  The payload should be a type of GetSurveyRequest object. 
         */
        public GetSurveyRequestAsyncHandler(final Message baseMessage) {
            super("GetSurveyRequestAsyncHandler-" + System.currentTimeMillis());

            if (baseMessage == null) {
                throw new IllegalArgumentException("The message can't be null.");
            }

            this.baseMessage = baseMessage;

        }
        
        @Override
        public void run() {

            try{
                // The following logic was originally in the handleGetSurveyRequest() method.  It was moved into a thread so that
                // the UMS module can be freed up to perform other work.
                if (logger.isInfoEnabled()) {
                    logger.info(getModuleName() + " received " + baseMessage);
                }
    
                mil.arl.gift.common.survey.SurveyGiftData surveyGiftData = null;
                GetSurveyRequest request = (GetSurveyRequest) baseMessage.getPayload();
                NACK nack = null;
                if(request instanceof GetKnowledgeAssessmentSurveyRequest){  
                    try{
                        surveyGiftData = Surveys.getConceptsSurvey(request.getSurveyContextId(), ((GetKnowledgeAssessmentSurveyRequest) request).getConcepts(), true);
                    }catch(DetailedException e){
                        //show string containing the concept names (for debugging purposes)
                        nack = new NACK(ErrorEnum.GET_SURVEY_ERROR,
                                e.getReason());
                        nack.setErrorHelp(e.getDetails());
                    }
                }else{
                     Survey survey = Surveys.getSurveyContextSurvey(request.getSurveyContextId(), request.getGiftKey());
                     if(survey == null){
                         throw new Exception("Failed to find the survey in the database (survey context id = "+request.getSurveyContextId()+", key = "+request.getGiftKey()+")");
                     }
                     surveyGiftData = new SurveyGiftData(request.getGiftKey(), survey);
                }
    
                if (surveyGiftData != null) {
    
                    sendReply(
                            baseMessage,
                            surveyGiftData,
                            MessageTypeEnum.GET_SURVEY_REPLY);   
                    
                } else {            
    
                    if(nack == null){ 
                        if(request instanceof GetKnowledgeAssessmentSurveyRequest){
                            
                            //show string containing the concept names (for debugging purposes)
                            nack = new NACK(ErrorEnum.GET_SURVEY_ERROR,
                                    "Survey with key '" + request.getGiftKey() + "' does not exist for survey context with ID '" + 
                                    request.getSurveyContextId() + "' and concept list of " + ((GetKnowledgeAssessmentSurveyRequest)request).getConceptNames());
                        }else{
                            nack = new NACK(ErrorEnum.GET_SURVEY_ERROR,
                                    "Survey with key '" + request.getGiftKey() + "' does not exist for survey context with ID '" + request.getSurveyContextId() + "'");
                        }
                    }
                    
                    sendReply(baseMessage, nack, MessageTypeEnum.PROCESSED_NACK);
                }
            }catch(Throwable t){
                logger.error("Failed to retrieve the survey for the request:\n"+baseMessage, t);
                NACK nack = new NACK(ErrorEnum.GET_SURVEY_ERROR, "The UMS module failed to retrieve the survey.");
                nack.setErrorHelp("The error was logged in the UMS module log file.  The message from the error is: "+t.getMessage());                
                
                sendReply(baseMessage, nack, MessageTypeEnum.PROCESSED_NACK);
            }
        }
        
    }

    /**
     * handle the incoming Request for a survey.
     *
     * @param request - the request message
     */
    private void handleGetSurveyRequest(Message message) {

        GetSurveyRequestAsyncHandler handler = new GetSurveyRequestAsyncHandler(message);
        
        // frees up the UMS module to do other work.
        handler.start(); 
        
    }
    
    /**
     * handle the incoming Request to update a branch path history.
     *
     * @param request - the request message
     */
    private void handleBranchPathHistoryUpdate(Message message) {

        if (logger.isInfoEnabled()) {
            logger.info(getModuleName() + " received " + message);
        }

        BranchPathHistory branchPathHistory = (BranchPathHistory) message.getPayload();        

        try{
            dbMgr.updateBranchPathHistoryProperties(branchPathHistory);
        }catch(Exception e){
            logger.error("Caught exception while trying to update a Branch Path History db entry for " + branchPathHistory, e);
            
            NACK nack= new NACK(ErrorEnum.OPERATION_FAILED,
                    "There was a problem when searching the database for " + branchPathHistory + ".");

            sendReply(message, nack, MessageTypeEnum.PROCESSED_NACK);
            return;
        }
        
        sendReply(
                message,
                new ACK(),
                MessageTypeEnum.PROCESSED_ACK);   
    }
    
    /**
     * handle the incoming Request for a branch path history.
     *
     * @param request - the request message
     */
    private void handleBranchPathHistoryRequest(Message message) {

        if (logger.isInfoEnabled()) {
            logger.info(getModuleName() + " received " + message);
        }

        @SuppressWarnings("unchecked")
        List<BranchPathHistory> branchPathsHistory = (List<BranchPathHistory>) message.getPayload();        

        for(BranchPathHistory branchPathHistory : branchPathsHistory){
            DbBranchPathHistory dbBranchPathHistory;
            try{
                dbBranchPathHistory =  dbMgr.getBranchPathHistory(branchPathHistory);
            }catch(Exception e){
                logger.error("Caught exception while trying to find a Branch Path History for " + branchPathHistory, e);
                
                NACK nack= new NACK(ErrorEnum.OPERATION_FAILED,
                        "There was a problem when searching the database for " + branchPathHistory + ".");
    
                sendReply(message, nack, MessageTypeEnum.PROCESSED_NACK);
                return;
            }
    
            if (dbBranchPathHistory == null) {
                //create it
                
                try{
                    dbMgr.updateBranchPathHistoryProperties(branchPathHistory);
                }catch(Exception e){
                    logger.error("Caught exception while trying to create a Branch Path History db entry for " + branchPathHistory, e);
                    
                    NACK nack= new NACK(ErrorEnum.OPERATION_FAILED,
                            "There was a problem when searching the database for " + branchPathHistory + ".");
    
                    sendReply(message, nack, MessageTypeEnum.PROCESSED_NACK);
                    return;
                }
            
            }  else {
                
                branchPathHistory.setActualCnt(dbBranchPathHistory.getActualCnt());
                branchPathHistory.setCnt(dbBranchPathHistory.getCnt());
            }
        }
        
        sendReply(
                message,
                branchPathsHistory,
                MessageTypeEnum.BRANCH_PATH_HISTORY_REPLY);   
    }
    
    /**
     * handle the incoming Request for an experiment.
     *
     * @param request - the request message
     */
    private void handleGetExperimentRequest(Message message) {

        if (logger.isInfoEnabled()) {
            logger.info(getModuleName() + " received " + message);
        }

        DataCollectionItem experiment = null;
        GetExperimentRequest request = (GetExperimentRequest) message.getPayload();
        
        String experimentId = request.getExperimentId();

        // we want to lazy load subjects and LTI results because we aren't using either collection in this method.
        DbDataCollection experimentEntry =  dbMgr.getExperiment(experimentId, false, false, null);

        if (experimentEntry == null) {
        	
            logger.error("Unable to find an experiment entry for experiment id = " + experimentId);
            
            NACK nack= new NACK(ErrorEnum.GET_SURVEY_ERROR,
                    "Experiment with ID '" + request.getExperimentId() + "' does not exist.");

            sendReply(message, nack, MessageTypeEnum.PROCESSED_NACK);
            
            return;
        
        }  else {
        	
        	HibernateObjectReverter hibernateToGift = new HibernateObjectReverter(UMSDatabaseManager.getInstance());
        	
        	experiment = hibernateToGift.convertExperiment(experimentEntry);

            sendReply(
                    message,
                    experiment,
                    MessageTypeEnum.GET_EXPERIMENT_REPLY);   
        }   
    }

    /**
     * Handle the domain selection request message by creating a domain session
     * for the user.
     *
     * @param message - the domain selection request message
     */
    private void handleDomainSelectionRequestMessage(Message message) {


        UserSessionMessage userSessionMessage = (UserSessionMessage) message;

        if (logger.isInfoEnabled()) {
            logger.info(getModuleName() + " received " + message.toString() + " message");
        }
        
        UserSession userSession = userSessionMessage.getUserSession();

        if (userSession != null && userSession.isSessionType(UserSessionType.LTI_USER)) {
            handleDomainSelectionLtiUser(message);
        } else if(userSessionMessage.getExperimentId() != null){
            handleDomainSelectionExperimentUser(message);
        }else{
            handleDomainSelectionNormalGiftUser(message);
        }

       
    }
    
    /**
     * Handler for the DomainSelectionRequest message specific for an LTI user.
     * 
     * @param message The message to be handled.
     */
    private void handleDomainSelectionLtiUser(Message message) {
        String errorMsg = null;
        ErrorEnum errorEnum = null;
        UserSessionMessage userSessionMessage = (UserSessionMessage) message;
        
        
        UserSession userSession = userSessionMessage.getUserSession();
        LtiUserSessionDetails ltiDetails = (LtiUserSessionDetails)userSession.getSessionDetails();
        
        DbGlobalUser globalUser = null;

        try {
            globalUser = dbMgr.selectRowById(ltiDetails.getGlobalUserId(), DbGlobalUser.class);
        } catch (Exception e) {
            displayDBProblemMessage();
            logger.error("Caught exception while trying to look up the global user "+ltiDetails.getGlobalUserId()+" in the db when handling a domain selection request of "+message+".", e);
        }
        
        if (globalUser != null) {
            DomainSelectionRequest request = (DomainSelectionRequest) message.getPayload();
            DbDomainSession dSession = new DbDomainSession(globalUser);
            dSession.setDomain(request.getDomainRuntimeId());
            dSession.setDomainSourceId(request.getDomainSourceId());

            try{
                dbMgr.insertRow(dSession);
                //send session id back

                if (logger.isInfoEnabled()) {
                    logger.info(getModuleName() + " created domain session = " + dSession + " for user " + globalUser);
                }

                mil.arl.gift.common.DomainSession domainSession = new mil.arl.gift.common.DomainSession(dSession.getSessionId(), globalUser.getGlobalId(), request.getDomainRuntimeId(), request.getDomainSourceId());
                domainSession.copyFromUserSession(userSessionMessage.getUserSession());
                sendReply(message,
                        domainSession,
                        MessageTypeEnum.DOMAIN_SELECTION_REPLY);

            } catch(Exception e) {
                logger.error("Caught exception while trying to insert a new domain session entry of "+dSession+" in the database.", e);
                
                //create error message
                errorMsg = "Unable to add new domain session to the database because the insert row failed";
                errorEnum = ErrorEnum.DB_INSERT_ERROR;
            }


        } else {
            errorMsg = "Unable to find user in the database with user id of " + userSessionMessage.getUserId();
            errorEnum = ErrorEnum.USER_NOT_FOUND_ERROR;
        }
        
        if (errorMsg != null && errorEnum != null) {
            //send NACK
            NACK nack = new NACK(errorEnum, errorMsg);

            logger.error(getModuleName() + " sending " + nack.toString() + " to " + ModuleTypeEnum.TUTOR_MODULE);

            sendReply(message, nack, MessageTypeEnum.PROCESSED_NACK);
        }
    }
    
    /**
     * Handler for the DomainSelectionRequest message specific for an experiment user.
     * 
     * @param message The message to be handled.
     */
    private void handleDomainSelectionExperimentUser(Message message) {
        
        String errorMsg = null;
        ErrorEnum errorEnum = null;
        UserSessionMessage userSessionMessage = (UserSessionMessage) message;
        //this is for an experiment domain session
        DbExperimentSubject subject = null;
        try {
            //MH: cant use two tuple composite id because we couldn't figure out the property names for the inner class of DbExperimentSubject.ExperimentSubjectId
            //    cant use example because all rows for an experiment are selected even if the user id is specified
            subject = dbMgr.getExperimentSubject(userSessionMessage.getExperimentId(), userSessionMessage.getUserId());
            
        } catch (Exception e) {
            displayDBProblemMessage();
            logger.error("Caught exception while trying to look up the subject "+userSessionMessage.getUserId()+" in experiment "+userSessionMessage.getExperimentId()+" in the db when handling a domain selection request of "+message+".", e);
        }
        
        if (subject != null) {
            DomainSelectionRequest request = (DomainSelectionRequest) message.getPayload();
            DbDomainSession dSession = new DbDomainSession(subject);
            dSession.setDomain(request.getDomainRuntimeId());
            dSession.setDomainSourceId(request.getDomainSourceId());

            try{
                dbMgr.insertRow(dSession);
                //send session id back

                if (logger.isInfoEnabled()) {
                    logger.info(getModuleName() + " created domain session = " + dSession + " for subject " + subject);
                }

                mil.arl.gift.common.DomainSession domainSession = new mil.arl.gift.common.DomainSession(dSession.getSessionId(), subject.getExperimentSubjectId().getSubjectId(), request.getDomainRuntimeId(), request.getDomainSourceId());
                domainSession.copyFromUserSession(userSessionMessage.getUserSession());
                sendReply(message,
                        domainSession,
                        MessageTypeEnum.DOMAIN_SELECTION_REPLY);

            } catch(Exception e) {
                
                logger.error("Caught exception while trying to insert a new domain session entry of "+dSession+" in the database.", e);
                
                //create error message
                errorMsg = "Unable to add new domain session to the database because the insert row failed";
                errorEnum = ErrorEnum.DB_INSERT_ERROR;
            }


        } else {
            errorMsg = "Unable to find subject in the database with subject id of " + userSessionMessage.getUserId() + " for experiment " + userSessionMessage.getExperimentId();
            errorEnum = ErrorEnum.USER_NOT_FOUND_ERROR;
        }
        
        if (errorMsg != null && errorEnum != null) {
            //send NACK
            NACK nack = new NACK(errorEnum, errorMsg);

            logger.error(getModuleName() + " sending " + nack.toString() + " to " + ModuleTypeEnum.TUTOR_MODULE);

            sendReply(message, nack, MessageTypeEnum.PROCESSED_NACK);
        }
    }
    
    /**
     * Handler for the DomainSelectionRequest message specific for a normal gift user.
     * 
     * @param message The message to be handled.
     */
    private void handleDomainSelectionNormalGiftUser(Message message) {
        String errorMsg = null;
        ErrorEnum errorEnum = null;
        UserSessionMessage userSessionMessage = (UserSessionMessage) message;
        
        //this is not an experiment domain session
        DbUser user = null;
        try {
            user = dbMgr.selectRowById(userSessionMessage.getUserId(), DbUser.class);
        } catch (Exception e) {
            displayDBProblemMessage();
            logger.error("Caught exception while trying to look up the user "+userSessionMessage.getUserId()+" in the db when handling a domain selection request of "+message+".", e);
        }
        
        if (user != null) {
            DomainSelectionRequest request = (DomainSelectionRequest) message.getPayload();
            
            // create published course user (if this course is a 'course data' published course)
            // - Do this first so the created domain session can get the experiment id
            
            // get path to course folder from course id
            String courseFilePath = request.getDomainSourceId();
            int fileDividerIndex = courseFilePath.lastIndexOf("/");
            
            List<DbDataCollection> dataCollections;
            if(fileDividerIndex != -1) {
                String courseFolder = courseFilePath.substring(0, fileDividerIndex);
                dataCollections = dbMgr.getPublishedCoursesOfType(courseFolder, DataSetType.COURSE_DATA, true, null);
                
            } else {
                
                /* This is a special edge case course that doesn't have a course folder, so don't 
                 * check for data collections. A good example of this is the course used by the Data 
                 * Collector tool, which is just used to collect domain session messages */
                dataCollections = new ArrayList<>();
            }
            DbExperimentSubject subject = null;
            if(!CollectionUtils.isEmpty(dataCollections)){
                DbDataCollection dataCollection = dataCollections.get(0);  // there should be only 1
                
                if(!ExperimentStatus.RUNNING.equals(dataCollection.getStatus())){
                    // don't allow this course instance to be started by this user
                    logger.error("Unable to support starting a "+DataSetType.COURSE_DATA+" published course because it is currently paused.\nPublished Course:"+dataCollection+"\nRequest:"+userSessionMessage);
                    
                    //create error message
                    errorMsg = "Unable to start this course because the course is currently paused.  Please contact the owner of the published course, "+dataCollection.getAuthorUsername()+", if you believe this is a mistake.";
                    errorEnum = ErrorEnum.OPERATION_FAILED;
                }
                
                if(errorMsg == null && errorEnum == null){
                    // only proceed if no error so far
                    subject = new DbExperimentSubject();
                    subject.setStartTime(new Date(message.getTimeStamp()));
                    
                    try{
                        UMSDatabaseManager.getInstance().addSubjectToExperiment(subject, dataCollection);
                    }catch(Exception e){
                        logger.error("Caught exception while trying to insert a new subject for the published course "+dataCollection+" in the database.", e);
                        
                        //create error message
                        errorMsg = "Unable to add new subject for this course in the database because the insert row failed";
                        errorEnum = ErrorEnum.DB_INSERT_ERROR;
                    }
                }

            }
            
            if(errorMsg == null && errorEnum == null){
                // only proceed if no error so far
            
                DbDomainSession dSession = new DbDomainSession(user);
                dSession.setDomain(request.getDomainRuntimeId());
                dSession.setDomainSourceId(request.getDomainSourceId());
                dSession.setSubject(subject);
    
                try{
                    dbMgr.insertRow(dSession);
                    //send session id back
    
                    if (logger.isInfoEnabled()) {
                        logger.info(getModuleName() + " created domain session = " + dSession + " for user " + user);
                    }

                    mil.arl.gift.common.DomainSession domainSession = new mil.arl.gift.common.DomainSession(dSession.getSessionId(), user.getUserId(), request.getDomainRuntimeId(), request.getDomainSourceId());
                    domainSession.copyFromUserSession(userSessionMessage.getUserSession());
                    if(subject != null && subject.getExperimentSubjectId() != null){
                        domainSession.setSubjectId(subject.getExperimentSubjectId().getSubjectId());
                    }
                    sendReply(message,
                            domainSession,
                            MessageTypeEnum.DOMAIN_SELECTION_REPLY);
    
    
                } catch(Exception e) {
                    logger.error("Caught exception while trying to insert a new domain session entry of "+dSession+" in the database.", e);
                    
                    //create error message
                    errorMsg = "Unable to add new domain session to the database because the insert row failed";
                    errorEnum = ErrorEnum.DB_INSERT_ERROR;
                }
            }


        } else {
            errorMsg = "Unable to find user in the database with user id of " + userSessionMessage.getUserId();
            errorEnum = ErrorEnum.USER_NOT_FOUND_ERROR;
        }
        
        if (errorMsg != null && errorEnum != null) {
            //send NACK
            NACK nack = new NACK(errorEnum, errorMsg);

            logger.error(getModuleName() + " sending " + nack.toString() + " to " + ModuleTypeEnum.TUTOR_MODULE);

            sendReply(message, nack, MessageTypeEnum.PROCESSED_NACK);
        }
    }

    /**
     * Handle the logout request message from the tutor module.
     *
     * @param message - the logout request message
     */
    private void handleLogoutRequestMessage(Message message) {

        if (logger.isInfoEnabled()) {
            logger.info(getModuleName() + " received " + message);
        }
        
        if(message instanceof UserSessionMessage){
        	
        	UserSessionMessage userMessage = (UserSessionMessage) message;
        	
        	if(userMessage.getExperimentId() != null){
        		
        		//check to see if any experiment subject data needs to be cleaned up
        		DbExperimentSubject subject = UMSDatabaseManager.getInstance().getExperimentSubject(
        				userMessage.getExperimentId(), 
        				userMessage.getUserId()
        		);
        		
        		if(subject != null && subject.getMessageLogFilename() == null){
        			
	        		try {
	        			//if the experiment subject does not have an associated message log file, then it never actually started a domain
	        			//session and doesn't have any data to report, so it needs to be cleaned up to avoid leaving a useless record
						UMSDatabaseManager.getInstance().deleteRow(subject);
						
					} catch (Exception e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Detected an experiment subject with no message log file but failed to clean up its record due to an error.\n"
                                    + subject, e);
                        }
					}
        		}
        	}
        }

        //for now, just send a logout reply
        sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
    }

    /**
     * Handle the new user request message by adding the user to the database
     * and sending that users information back to the tutor.
     *
     * @param message - a new user request message which needs to be acted upon
     */
    private void handleNewUserRequestMessage(Message message) {

        String errorMsg = null;
        ErrorEnum errorEnum = null;

        if (logger.isInfoEnabled()) {
            logger.info(getModuleName() + " received " + message);
        }


        UserData uData = ((UserData) message.getPayload());

        if (uData != null) {

            //determine if this is a subject in an experiment
            if(uData.getExperimentId() != null){
                
                if (logger.isInfoEnabled()) {
                    logger.info("Adding new subject to experiment represented by experiment id of '" + uData.getExperimentId() + "'.");
                }
                
                try{
                    // we want to lazy load subjects and LTI results because we aren't using either collection in this method.
                    DbDataCollection dbExperiment = UMSDatabaseManager.getInstance().getExperiment(uData.getExperimentId(), false, false, null);                    
                    DbExperimentSubject subject = new DbExperimentSubject();
                    subject.setStartTime(new Date(message.getTimeStamp()));
                    
                    UMSDatabaseManager.getInstance().addSubjectToExperiment(subject, dbExperiment);
                    
                    if (logger.isInfoEnabled()) {
                        logger.info("Added new experiment subject of " + subject);
                    }
                    
                    uData.setUserId(subject.getExperimentSubjectId().getSubjectId());
                    
                    /* Injecting a read-only user because experiments need a
                     * username for Nuxeo permissions. See #4550 for more
                     * details. Hopefully this does not create an issue later
                     * on. */
                    uData.setUsername(UMSModuleProperties.getInstance().getReadOnlyUser());

                }catch(DetailedException e){
                    logger.error("Caught exception while trying to create experiment subject for experiment (id "+uData.getExperimentId()+").", e);
                    
                   //create error message
                    errorMsg = "Unable to add new subject to experiment (id "+uData.getExperimentId()+") because failed to insert a new row in the database.\n"
                            + "Reason : "+e.getReason()+"\n"
                                    + "Details : "+e.getDetails()+".";
                    errorEnum = ErrorEnum.DB_INSERT_ERROR;
                }
                
            }else{
                //
                // insert user into db
                //
                
                if(uData.getUsername() != null && CommonProperties.getProhibitedNames().contains(uData.getUsername().toLowerCase())){
                    //username is prohibited
                    errorMsg = "Unable to create a new user in the database for username of '"+uData.getUsername()+"' because that username is prohibited.";
                    errorEnum = ErrorEnum.DB_INSERT_ERROR;
                }else if(uData.getLMSUserName() != null && CommonProperties.getProhibitedNames().contains(uData.getLMSUserName().toLowerCase())){
                    //username is prohibited (even know its just the LMS username, just be consistent about what's allowed through gift)
                    errorMsg = "Unable to create a new user in the database for username of '"+uData.getLMSUserName()+"' because that username is prohibited.";
                    errorEnum = ErrorEnum.DB_INSERT_ERROR;
                }else{
                
                    GenderEnum gender = uData.getGender();
                    DbUser user = new DbUser(gender.getName());
                    user.setLMSUserName(uData.getLMSUserName());
        
                    try{
                        dbMgr.insertRow(user);
                        //send user data back
        
                        if (logger.isInfoEnabled()) {
                            logger.info(getModuleName() + " created user = " + user);
                        }
                        uData.setUserId(user.getUserId());
        
                    } catch(Exception e) {
                        
                        logger.error("Caught exception while trying to insert a new user entry of "+user+" in the database.", e);
    
                        //create error message
                        errorMsg = "Unable to add new user to the database because the insert row failed";
                        errorEnum = ErrorEnum.DB_INSERT_ERROR;
                    }
                }
            }
            
        } else {
            errorMsg = "Unable to add new user to the database because the user data was null";
            errorEnum = ErrorEnum.MALFORMED_DATA_ERROR;
        }


        if (errorMsg != null && errorEnum != null) {
            //send NACK
            NACK nack = new NACK(errorEnum, errorMsg);

            logger.error(getModuleName() + " sending " + nack + " to " + ModuleTypeEnum.TUTOR_MODULE);

            sendReply(message, nack, MessageTypeEnum.PROCESSED_NACK);
            
        }else{
            
            if (logger.isInfoEnabled()) {
                logger.info("User created, therefore sending " + uData + " upstream to " + ModuleTypeEnum.TUTOR_MODULE);
            }
            sendReply(message, uData, MessageTypeEnum.LOGIN_REPLY);
        }
    }
    
    /**
     * Handle a Get user id request message by looking up the user in the UMS db.
     * 
     * @param message - a user id request message
     */
    public void handleUserIdRequestMessage(Message message){
        
        if (logger.isInfoEnabled()) {
            logger.info(getModuleName() + " received " + message);
        }
        
        //find user in db
        String username = (String) message.getPayload();
        try{
            DbUser user = dbMgr.getUserByUsername(username, true);
            UserData userData = new UserData(user.getUserId(), user.getLMSUserName(), GenderEnum.valueOf(user.getGender()));
            userData.setUsername(username);
            if (logger.isInfoEnabled()) {
                logger.info("User already existed in UMS, sending user data of " + userData + ".");
            }
            sendReply(message, userData, MessageTypeEnum.USER_ID_REPLY);
            
        }catch(UMSDatabaseException exception){
            logger.error("Caught exception while trying to retrieve user by username of '"+username+"'.", exception);
            
            NACK nack = new NACK(ErrorEnum.DB_INSERT_ERROR, "Unable to add new user to the database because the insert failed");

            logger.error(getModuleName() + " sending " + nack + " to " + ModuleTypeEnum.TUTOR_MODULE);

            sendReply(message, nack, MessageTypeEnum.PROCESSED_NACK);            
        }catch(ProhibitedUserException exception){
            logger.error("Caught exception while trying to retrieve user by username of '"+username+"'.", exception);
            
            NACK nack = new NACK(ErrorEnum.DB_INSERT_ERROR, "Unable to add new user to the database because the username is on the prohibitied list.");

            logger.error(getModuleName() + " sending " + nack + " to " + ModuleTypeEnum.TUTOR_MODULE);

            sendReply(message, nack, MessageTypeEnum.PROCESSED_NACK);            
        }

    }

    /**
     * Handle the login request message by finding the appropriate user
     * information and sending that information back to the tutor.
     *
     * @param message - a login request message which needs to be acted upon
     */
    private void handleLoginRequestMessage(Message message) {

        if (logger.isInfoEnabled()) {
            logger.info(getModuleName() + " received " + message);
        }
        
        LoginRequest request = (LoginRequest) message.getPayload();
        
        try {
            
            
            UserData userData = dbMgr.loginUser(request, properties.getDeploymentMode());
            sendReply(message, userData, MessageTypeEnum.LOGIN_REPLY);
        }catch (UserNotFoundException exception){
            logger.error("Caught exception while logging in user", exception);
            
            //the incorrect username was provided in the request
            NACK nack = new NACK(ErrorEnum.INCORRECT_CREDENTIALS, "The incorrect username of "+request.getUsername()+" was provided for user with id = " + ((UserSessionMessage) message).getUserId());

            logger.error(getModuleName() + " sending " + nack + " to " + ModuleTypeEnum.TUTOR_MODULE);

            sendReply(message, nack, MessageTypeEnum.PROCESSED_NACK);
        } catch (Exception e) {
            displayDBProblemMessage();
            logger.error("Caught exception while trying to login user for request of " + request, e);
            
            NACK nack = new NACK(ErrorEnum.USER_NOT_FOUND_ERROR, "unable to find user with id = " + ((UserSessionMessage) message).getUserId());

            logger.error(getModuleName() + " sending " + nack + " to " + ModuleTypeEnum.TUTOR_MODULE);

            sendReply(message, nack, MessageTypeEnum.PROCESSED_NACK);
        }

    }
    
    
    /**
     * Handles the incoming lti get user request.  This method retrieves a record from the 
     * ltiuserrecord table in the UMS database.
     * 
     * @param message The incoming message which should be of type LtiGetUserRequest.
     */
    private void handleLtiGetUserRequest(Message message) {

        if (logger.isInfoEnabled()) {
            logger.info(getModuleName() + " received " + message);
        }

        try {
            
            LtiGetUserRequest request = (LtiGetUserRequest) message.getPayload();
            DbLtiUserRecord dbRecord = dbMgr.getLtiUserRecord(request.getConsumerKey(),  request.getConsumerId(), false);
            
            if (dbRecord != null) {
                
                
                if (dbRecord.getGlobalUser() != null) {
                    LtiUserRecord ltiUserRecord = new LtiUserRecord(new LtiUserId(dbRecord.getConsumerKey(), dbRecord.getConsumerId()), 
                            dbRecord.getGlobalUser().getGlobalId(), dbRecord.getLaunchRequestTimestamp().getTime());
                    
                    sendReply(message, ltiUserRecord, MessageTypeEnum.LTI_GETUSER_REPLY); 
                } else {
                    NACK nack = new NACK(ErrorEnum.USER_NOT_FOUND_ERROR, "handleLtiGetUserRequest() could not find a global id for the request: " + request);

                    logger.error(getModuleName() + " sending " + nack + " to " + ModuleTypeEnum.TUTOR_MODULE);

                    sendReply(message, nack, MessageTypeEnum.PROCESSED_NACK);
                }
                
                
            } else {
                NACK nack = new NACK(ErrorEnum.USER_NOT_FOUND_ERROR, "handleLtiGetUserRequest() could not find an lti user from request: " + request);

                logger.error(getModuleName() + " sending " + nack + " to " + ModuleTypeEnum.TUTOR_MODULE);

                sendReply(message, nack, MessageTypeEnum.PROCESSED_NACK);
            }
            
        } catch (Exception e) {
           
            logger.error("handleLtiGetUserRequest() caught exception:  ", e);
            
            NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "handleLtiGetUserRequest() caught exception " + message);

            logger.error(getModuleName() + " sending " + nack + " to " + ModuleTypeEnum.TUTOR_MODULE);

            sendReply(message, nack, MessageTypeEnum.PROCESSED_NACK);
        }

    }

    /**
     * Handle the initialize domain session request message
     *
     * @param message - the domain session request message
     */
    private void handleInitializeDomainSessionRequest(Message message) {

        if (logger.isInfoEnabled()) {
            logger.info(getModuleName() + " received " + message.toString() + " message");
        }

        sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
    }
    
    /**
     * Handle the domain session start time message
     * 
     * @param message - the domain session start time request message
     */
    private void handleDomainSessionStartTimeRequest(Message message){
    	
    	if (logger.isInfoEnabled()) {
	            logger.info(getModuleName() + " received " + message.toString() + " message");
	    }
	   	
	   	Object payload = message.getPayload();
	   	
	   	if(payload instanceof Integer){   		    				
	   		
	   		DbDomainSession dSession = null;
	
	        try {
	            dSession = dbMgr.selectRowById(payload, DbDomainSession.class);
	        } catch (Exception e) {
	            displayDBProblemMessage();
	            logger.error("Caught exception while selected DB row", e);
	        }

	        if (dSession != null) {
	        	
	        	//get the start time of the first domain session found and send it back along with its associated domain session id   
	        	sendReply(message, dSession.getStartTime(), MessageTypeEnum.DOMAIN_SESSION_START_TIME_REPLY);
	        
	        } else { 
	        	sendReply(message, new NACK(ErrorEnum.OPERATION_FAILED, "unable to find domain session with id ="+ payload), MessageTypeEnum.PROCESSED_NACK);
	        }
	   		
	   	} else {   	
	   		sendReply(message, new NACK(ErrorEnum.OPERATION_FAILED, "detected invalid payload of" + payload + " while trying to find domain session start time. Payload should be of type: Integer."), MessageTypeEnum.PROCESSED_NACK);
	   	}
   }

    /**
     * Handles the message to kill this module
     *
     * @param message - The Kill Module message
     */
    private void handleKillModuleMessage(Message message) {
        Thread killModule = new Thread("Kill Module") {

            @Override
            public void run() {
                killModule();
            }
        };
        killModule.start();
    }

    /**
     * Create and send a UMS status message over the network.
     */
    @Override
    public void sendModuleStatus() {
        sendMessage(SubjectUtil.UMS_DISCOVERY_TOPIC, moduleStatus, MessageTypeEnum.MODULE_STATUS, null);
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        
        if (logger.isInfoEnabled()) {
            logger.info("Finished cleaning up base module class.");
        }
        
        //shutdown UMS db
        try{
            dbMgr.cleanup();
            
            if (logger.isInfoEnabled()) {
                logger.info("Finished cleaning up database manager.");
            }
        }catch(Exception e){
            logger.error("Caught exception while trying to cleanup UMS database. Still shutting down UMS module.", e);
        }
    }

    /**
     * Create a new Event File entry in the database and update the appropriate
     * domain session entry
     *
     * @param fileName - the event file name (i.e. domain session message log
     * file)
     * @param domainSessionId - the domain session id associated with the event
     * file
     */
    public void eventFileCreatedNotification(String fileName, int domainSessionId) {

        if (logger.isInfoEnabled()) {
            logger.info("An event file was created named = " + fileName + " for domain session id = " + domainSessionId);
        }

        //find domain session entry
        DbDomainSession dSession = null;
        try {
            dSession = dbMgr.selectRowById(domainSessionId, DbDomainSession.class);
        } catch (Exception e) {
            displayDBProblemMessage();
            logger.error("Caught exception while selected DB row", e);
        }

        if (dSession != null) {

            //create Sensor File entry
            DbEventFile eFile = new DbEventFile(fileName);

            try{
                dbMgr.insertRow(eFile);
                
                //update domain session entry

                dSession.setEventFile(eFile);

                if (dbMgr.updateRow(dSession)) {

                    if (logger.isInfoEnabled()) {
                        logger.info("Event File successfully added to the database and the domain session was updated");
                    }

                } else {
                    logger.error("Updating the domain session with id = " + dSession.getSessionId() + " failed for new event file named " + fileName);
                }

            } catch(Exception e) {
                logger.error("The database insert failed for the new event file entry = " + eFile, e);
            }
        } else {
            logger.error(getModuleName() + " failed to create an Event File in the database because unable to find a domain session entry for the domain session id " + domainSessionId);
        }
    }

    /**
     * Displays a generic message that there is a database issue
     */
    private void displayDBProblemMessage() {
        System.out.println("\n\n *** A problem with the database was detected, check log for more details\n");
    }

    /**
     * Used to run the UMS module
     *
     * @param args - launch module arguments
     */
    public static void main(String[] args) {
        
        if (logger.isInfoEnabled()) {
            logger.info("Starting UMS...");
        }
        
        Runtime.getRuntime().addShutdownHook(new Thread("UMS Shutdown Hook"){
        	
        	@Override
        	public void run(){
        		
        		//Clean up and shut down the Derby database
        		try{
	        		UMSDatabaseManager manager = UMSDatabaseManager.getInstance();
	        		
	        		try {
						manager.cleanup();
						
					} catch (Exception e1) {
						e1.printStackTrace();
					}

        		}catch(Throwable managerException){
        			managerException.printStackTrace();
        		}		
        	}
        });
        
    	ModuleModeEnum mode = checkModuleMode(args);
        UMSModuleProperties.getInstance().setCommandLineArgs(args);

        UMSModule umsModule = null;
        try{
            umsModule = UMSModule.getInstance();
            umsModule.setModuleMode(mode);
            umsModule.showModuleStartedPrompt(); 
            
            //don't call cleanup again if already cleaned due to cleanup being called
            //elsewhere (e.g. killModule)
            if(umsModule.shuttingDown){
                umsModule.cleanup(); 
            }
        }catch(Exception e){
            System.err.println("The UMS Module threw an exception.");
            e.printStackTrace();
            
            if(umsModule != null){
                umsModule.cleanup();
            }
            
            JOptionPane.showMessageDialog(null,
                    "The UMS Module had a severe error.  Check the log file and the console window for more information.",
                    "UMS Module Error",
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
