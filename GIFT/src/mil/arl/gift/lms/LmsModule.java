/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.lms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.KnowledgeSessionCreated;
import mil.arl.gift.common.LMSConnectionInfo;
import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.common.LMSCourseRecords;
import mil.arl.gift.common.LMSData;
import mil.arl.gift.common.LMSDataRequest;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.PedagogicalRequest;
import mil.arl.gift.common.PublishLessonScore;
import mil.arl.gift.common.PublishLessonScoreResponse;
import mil.arl.gift.common.SubmitSurveyResults;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.course.CourseRecordRef;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.SessionType;
import mil.arl.gift.common.course.dkf.session.IndividualKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.KnowledgeSessionsReply;
import mil.arl.gift.common.course.dkf.session.LessonCompleted;
import mil.arl.gift.common.course.dkf.session.TeamKnowledgeSession;
import mil.arl.gift.common.enums.ErrorEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.ModuleModeEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.module.AbstractModule;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.common.survey.score.AbstractScale;
import mil.arl.gift.common.ta.request.EnvironmentControl;
import mil.arl.gift.lms.impl.common.LmsException;
import mil.arl.gift.lms.impl.common.LmsInvalidCourseRecordException;
import mil.arl.gift.lms.impl.common.LmsInvalidStudentIdException;
import mil.arl.gift.lms.impl.common.LmsIoException;
import mil.arl.gift.net.api.SubjectUtil;
import mil.arl.gift.net.api.message.ACK;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.NACK;
import mil.arl.gift.net.api.message.UserSessionMessage;

/**
 * This is the LMS Module which is responsible for maintaining a link to learner (i.e. student) training records.
 *
 * @author mhoffman
 *
 */
public class LmsModule extends AbstractModule {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(LmsModule.class);

    /** The default name to use for this {@link LmsModule}. */
    private static final String DEFAULT_MODULE_NAME = "LMS_Module";

    /** The singleton instance of the {@link LmsModule}. */
    private static LmsModule instance = null;

    static {
        //use LMS log4j
        PropertyConfigurator.configureAndWatch(PackageUtil.getConfiguration() + "/lms/lms.log4j.properties");
    }
    
    /** 
     * mapping of created but not started team knowledge session used to keep track of the latest
     * session membership until the session is started.
     * key: domain session id
     * value: latest knowledge session information.  Can be null if the session has been created but no updates have been received yet.
     */
    private Map<Integer, TeamKnowledgeSession> teamKnowledgeSessionMap = new HashMap<>();

    /**
     * Return the singleton instance of this class
     *
     * @return LmsModule
     */
    public static LmsModule getInstance(){
        if (instance == null) {
            instance = new LmsModule();
            instance.init();
        }
        return instance;
    }

    /**
     * Private constructor used to enforce the singleton pattern.
     */
    private LmsModule() {
        super(DEFAULT_MODULE_NAME, SubjectUtil.LMS_QUEUE_PREFIX + ADDRESS_TOKEN_DELIM + ipaddr, SubjectUtil.LMS_QUEUE_PREFIX + ADDRESS_TOKEN_DELIM + ipaddr + ADDRESS_TOKEN_DELIM + SubjectUtil.INBOX_QUEUE_SUFFIX, LmsModuleProperties.getInstance());

    }

    @Override
    protected void init() {

        //initialize LMS connections
        try{
            LMSConnectionsManager.getInstance();
        }catch(Throwable t){
            logger.error("Failed to initialize the LMS connections.", t);
            throw new RuntimeException("Failed to initialize the LMS connections.");
        }

        //create client to send lms status to
        createSubjectTopicClient(SubjectUtil.LMS_DISCOVERY_TOPIC, false);

        //start the module heartbeat
        initializeHeartbeat();
    }

    @Override
    public ModuleTypeEnum getModuleType(){
        return ModuleTypeEnum.LMS_MODULE;
    }

    @Override
    protected void handleMessage(Message message) {
        MessageTypeEnum type = message.getMessageType();
        if (type == MessageTypeEnum.LMS_DATA_REQUEST) {
            handleLMSDataRequest(message);
        } else if (type == MessageTypeEnum.PUBLISH_LESSON_SCORE_REQUEST) {
            handlePublishLessonScoreRequest(message);
        } else if (type == MessageTypeEnum.SUBMIT_SURVEY_RESULTS){
            handleSubmitSurveyResults(message);
        } else if(type == MessageTypeEnum.LEARNER_STATE){
            handleLearnerState(message);
        } else if(type == MessageTypeEnum.PEDAGOGICAL_REQUEST){
            handlePedRequest(message);
        } else if (type == MessageTypeEnum.KILL_MODULE) {
            handleKillModuleMessage(message);
        } else if (type == MessageTypeEnum.INITIALIZE_DOMAIN_SESSION_REQUEST) {
            handleInitDomainSessionMessage(message);
        } else if (type == MessageTypeEnum.START_DOMAIN_SESSION) {
            handleStartDomainSessionMessage(message);
        } else if (type == MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST) {
            handleCloseDomainSessionMessage(message);
        } else if(type == MessageTypeEnum.KNOWLEDGE_SESSION_CREATED){
            handleKnowledgeSessionCreated(message);
        } else if(type == MessageTypeEnum.START_TEAM_KNOWLEDGE_SESSION_REQUEST){
            handleStartTeamKnowledgeSession(message);
        } else if(type == MessageTypeEnum.KNOWLEDGE_SESSION_UPDATED_REQUEST){
            handleKnowledgeSessionUpdatedRequest(message);
        } else if(type == MessageTypeEnum.LESSON_COMPLETED){
            handleLessonCompletedMessage(message);
        } else if(type == MessageTypeEnum.ENVIRONMENT_CONTROL){
            handleEnvironmentControl(message);
        }
        else {

            logger.error(getModuleName() + " received unhandled message:" + message);

            if(message.needsHandlingResponse()) {
                this.sendReply(message, new NACK(ErrorEnum.UNHANDLED_MESSAGE_ERROR, "The module did not know how to handle the message"), MessageTypeEnum.PROCESSED_NACK);
            }
        }
    }
    
    /**
     * Handle the notification of an environment control message.
     * @param message contains an environment control message, used to apply environment scenario adaptation.
     */
    private void handleEnvironmentControl(Message message){
        
        EnvironmentControl eControl = (EnvironmentControl)message.getPayload();
        try{
            LMSConnectionsManager.getInstance().handleEnvironmentAdaptation(((DomainSessionMessage)message).getUserSession(),
                    ((DomainSessionMessage)message).getDomainSessionId(), eControl);
        }finally{
            // for now don't worry about errors
            sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
        }
    }
    
    /**
     * Handle the notification of a lesson (aka DKF, domain knowledge session) being completed.
     * @param message contains information about the lesson completed event.
     */
    private void handleLessonCompletedMessage(Message message){
        
        LessonCompleted lessonCompleted = (LessonCompleted) message.getPayload();
        
        try{
            LMSConnectionsManager.getInstance().handleLessonCompleted(((DomainSessionMessage)message).getUserSession(),
                    ((DomainSessionMessage)message).getDomainSessionId(), lessonCompleted);
        }finally{
            // for now don't worry about errors
            sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
        }
    }

    /**
     * Handle an incoming knowledge session updated request message.  This contains the session membership
     * for a team knowledge session as well as session metadata (e.g. name, description, pre-defined team organization structure).
     * When appropriate send the latest knowledge session details to the LMS connections.
     * @param message the knowledge session updated request message to handle.
     */
    private void handleKnowledgeSessionUpdatedRequest(Message message){
        
        KnowledgeSessionsReply knowledgeSessionsReply = (KnowledgeSessionsReply) message.getPayload();
        for(AbstractKnowledgeSession knowledgeSession : knowledgeSessionsReply.getKnowledgeSessionMap().values()){
            
            if(knowledgeSession instanceof TeamKnowledgeSession){
                
                if(knowledgeSession.getSessionType() == SessionType.ACTIVE_PLAYBACK){
                    // in this session type the knowledge session updated request is created and sent by the
                    // domain module after the session has started (i.e. start team knowledge session).  That started session
                    // message won't be received by this class because it is in the playback.
                    try{
                        LMSConnectionsManager.getInstance().handleKnowledgeSessionCreated(((DomainSessionMessage)message).getUserSession(),
                                ((DomainSessionMessage)message).getDomainSessionId(), new KnowledgeSessionCreated(knowledgeSession));

                    }finally{
                        // now that the session has started, remove it from the map that was tracking it from creation to starting
                        teamKnowledgeSessionMap.remove(((DomainSessionMessage)message).getDomainSessionId());
                    }
                }else{
                    // updated the latest information about the knowledge session until it has started
                    teamKnowledgeSessionMap.put(((DomainSessionMessage)message).getDomainSessionId(), (TeamKnowledgeSession)knowledgeSession);
                }
            }
        }
    }
    
    /**
     * Handle an incoming start team knowledge session message.  This is an indication that
     * a live active (non playback) knowledge session with one or more players is starting.  Send the
     * latest knowledge session details to the LMS connections.
     * @param message the start team knowledge session message to handle.
     */
    private void handleStartTeamKnowledgeSession(Message message){
        
        DomainSessionMessage dsMsg = (DomainSessionMessage)message;
        TeamKnowledgeSession teamSession = teamKnowledgeSessionMap.get(dsMsg.getDomainSessionId());
        if(teamSession != null){
            try{
                LMSConnectionsManager.getInstance().handleKnowledgeSessionCreated(((DomainSessionMessage)message).getUserSession(),
                        ((DomainSessionMessage)message).getDomainSessionId(), new KnowledgeSessionCreated(teamSession));
                
            }finally{
                // now that the session has started, remove it from the map that was tracking it from creation to starting
                teamKnowledgeSessionMap.remove(((DomainSessionMessage)message).getDomainSessionId());
            }
            
        }
    }
    
    /**
     * Handle the knowledge session created message by providing it to the connected LMSs to record.
     * For an individual session, send the latest knowledge session details to the LMS connections.
     * 
     * @param message a knowledge session create message to store information about in a connected LMS
     */
    private void handleKnowledgeSessionCreated(Message message){
        
        KnowledgeSessionCreated knowledgeSessionCreated = (KnowledgeSessionCreated)message.getPayload();
        
        if(knowledgeSessionCreated.getKnowledgeSession() instanceof IndividualKnowledgeSession){

            LMSConnectionsManager.getInstance().handleKnowledgeSessionCreated(((DomainSessionMessage)message).getUserSession(),
                    ((DomainSessionMessage)message).getDomainSessionId(), knowledgeSessionCreated);
        }else{
            // start to keep track of session detail updates until the session has started
            teamKnowledgeSessionMap.put(((DomainSessionMessage)message).getDomainSessionId(), null);
        }
    }

    /**
     * Handle the pedagogical request message by providing it to the connected LMSs to record.
     *
     * @param message a pedagogical request to save in a connected LMS
     */
    private void handlePedRequest(Message message){

        PedagogicalRequest request = (PedagogicalRequest)message.getPayload();

        LMSConnectionsManager.getInstance().publishPedagogicalRequest(((DomainSessionMessage)message).getUserSession(),
                ((DomainSessionMessage)message).getDomainSessionId(), request);
    }

    /**
     * Handle the learner state message by providing it to the connected LMSs to record.
     *
     * @param message a learner state message to save in a connected LMS
     */
    private void handleLearnerState(Message message){

        LearnerState state = (LearnerState)message.getPayload();

        LMSConnectionsManager.getInstance().publishLearnerState(((DomainSessionMessage)message).getUserSession(),
                ((DomainSessionMessage)message).getDomainSessionId(), state);
    }
    
    /**
     * Handle the Init Domain Session message by passing to the connected LMSs
     * and responding with Processed ACK
     * 
     * @param message - InitializeDomainSessionRequest
     */
    private void handleInitDomainSessionMessage(Message message) {
        
        LMSConnectionsManager.getInstance().publishInitDomainSession(message);
        
        sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
    }
    
    /**
     * Handle the Start Domain Session message by passing to the connected LMSs
     * and responding with Processed ACK
     * 
     * @param message - no payload, just a DomainSessionMessage
     */
    private void handleStartDomainSessionMessage(Message message) {
        
        LMSConnectionsManager.getInstance().publishStartDomainSession(message);
        
        sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
    }
    
    /**
     * Handle the Close Domain Session message by passing to the connected LMSs
     * and responding with Processed ACK
     * 
     * @param message - CloseDomainSessionRequest
     */
    private void handleCloseDomainSessionMessage(Message message) {
        
        LMSConnectionsManager.getInstance().publishCloseDomainSession(message);
        
        sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
    }

    /**
     * Handle the LMS data request message by sending the LMS data for the specified learner
     * user.
     *
     * @param message - the LMS data request message
     */
    private void handleLMSDataRequest(Message message) {

        if(logger.isInfoEnabled()){
            logger.info(getModuleName() + " received message: " + message);
        }

        UserSession userSession = null;
        try {

            LMSDataRequest dataRequest = (LMSDataRequest)message.getPayload();
            userSession = ((UserSessionMessage)message).getUserSession();

            LMSData lmsData = new LMSData();
            if (dataRequest.isLearnerRequest()) {
                List<AbstractScale> learnerStateAttributes = LMSConnectionsManager.getInstance().getLMSLearnerStateAttributeData(userSession.getUserId(), dataRequest);
                lmsData.setAbstractScales(learnerStateAttributes);
            } else {
                LMSCourseRecords lmsCourseRecords = LMSConnectionsManager.getInstance().getLMSData(userSession.getUserId(), dataRequest);
                lmsData.setCourseRecords(lmsCourseRecords);
            }

            sendReply(message, lmsData, MessageTypeEnum.LMS_DATA_REPLY);
        } catch (LmsIoException ex) {

            logger.error("Caught an LmsIoException when retrieving course history", ex);
            sendReply(message,
                    new NACK(ErrorEnum.LMS_RETRIEVE_ERROR, "Retrieving course history resulted in a LmsIoException.\n"+ex.getMessage()), MessageTypeEnum.PROCESSED_NACK);

        } catch (LmsInvalidStudentIdException ex) {

            logger.error("Caught an LmsInvalidStudentIdException when retrieving course history", ex);
            sendReply(message,
                    new NACK(ErrorEnum.LMS_RETRIEVE_ERROR, "Retrieving course history resulted in a LmsInvalidStudentIdException\n."+ex.getMessage()), MessageTypeEnum.PROCESSED_NACK);

        } catch (DetailedException e){

            logger.error("Caught an Exception when retrieving course history for "+userSession, e);
            NACK nack = new NACK(ErrorEnum.LMS_RETRIEVE_ERROR, "Retrieving course history resulted in the error '"+e.getReason()+"'.");
            nack.setErrorHelp(e.getDetails());
            sendReply(message, nack, MessageTypeEnum.PROCESSED_NACK);

        } catch (Throwable e){

            logger.error("Caught an Exception when retrieving course history", e);
            sendReply(message,
                    new NACK(ErrorEnum.LMS_RETRIEVE_ERROR, "Retrieving course history resulted in a exception."), MessageTypeEnum.PROCESSED_NACK);

        }
    }

    /**
     * Handle the publish lesson score by writing the necessary entries to the connected LMS(s).
     *
     * @param message the publish lesson score request to handle
     */
    private void handlePublishLessonScoreRequest(Message message) {

        if(logger.isInfoEnabled()){
            logger.info(getModuleName() + " received message: " + message);
        }

        LMSCourseRecord record = ((PublishLessonScore)message.getPayload()).getCourseData();

        try {

            Map<LMSConnectionInfo, CourseRecordRef> publishedRecordsByLMS = LMSConnectionsManager.getInstance().publishLessonScore(((DomainSessionMessage)message).getUserSession(),
                    ((DomainSessionMessage)message).getDomainSessionId(), ((PublishLessonScore)message.getPayload()).getLmsUsername(), record, ((PublishLessonScore)message.getPayload()).getConcepts());

            if(message.needsHandlingResponse()){
                sendReply(message, new PublishLessonScoreResponse(publishedRecordsByLMS), MessageTypeEnum.PUBLISH_LESSON_SCORE_REPLY);
            }

        } catch (LmsInvalidStudentIdException ex) {
            sendReply(message,
                    new NACK(ErrorEnum.LMS_RETRIEVE_ERROR, "Publishing a lesson score resulted in a LmsInvalidStudentIdException."), MessageTypeEnum.PROCESSED_NACK);
            logger.error("Caught an LmsInvalidStudentIdException when publishing lesson score", ex);
        } catch (LmsInvalidCourseRecordException ex) {
            sendReply(message,
                    new NACK(ErrorEnum.LMS_RETRIEVE_ERROR, "Publishing a lesson score resulted in a LmsInvalidCourseRecordException."), MessageTypeEnum.PROCESSED_NACK);
            logger.error("Caught an LmsInvalidCourseRecordException when publishing lesson score", ex);
        } catch (LmsException ex) {
            sendReply(message,
                    new NACK(ErrorEnum.LMS_RETRIEVE_ERROR, "Publishing a lesson score resulted in a LmsException."), MessageTypeEnum.PROCESSED_NACK);
            logger.error("Caught an LmsException when publishing lesson score", ex);
        } catch (Throwable e){
            sendReply(message,
                    new NACK(ErrorEnum.LMS_RETRIEVE_ERROR, "Publishing a lesson score resulted in an exception."), MessageTypeEnum.PROCESSED_NACK);
            logger.error("Caught an Exception when publishing lesson score", e);
        }
    }

    /**
     * Handles submitting survey results by writing the necessary entries to the connected LMS(s).
     *
     * @param message the submit survey results message
     */
    private void handleSubmitSurveyResults(Message message){

        try {
            SubmitSurveyResults results = (SubmitSurveyResults) message.getPayload();
            LMSConnectionsManager.getInstance().publishSurveyResults(((DomainSessionMessage) message).getUserSession(),
                    ((DomainSessionMessage) message).getDomainSessionId(), ((DomainSessionMessage) message).getUsername(), results);
            sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);
        } catch (Exception e) {
            sendReply(message, new NACK(ErrorEnum.UNHANDLED_MESSAGE_ERROR, "One or more of the survey results were not submitted. " + e.getMessage()),
                    MessageTypeEnum.PROCESSED_NACK);
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
        sendMessage(SubjectUtil.LMS_DISCOVERY_TOPIC, moduleStatus, MessageTypeEnum.MODULE_STATUS, null);
    }

    @Override
    protected void cleanup() {

        super.cleanup();

        try{
            LMSConnectionsManager.getInstance().cleanup();
        }catch(Throwable t){
            logger.error("Failed to cleanup the LMS connections.", t);
        }
    }

    /**
     * Use to run the LMS module
     *
     * @param args - launch module arguments
     */
    public static void main(String[] args) {
        ModuleModeEnum mode = checkModuleMode(args);
        LmsModuleProperties.getInstance().setCommandLineArgs(args);

        LmsModule lModule = null;
        try{
            lModule = LmsModule.getInstance();
            lModule.setModuleMode(mode);
            lModule.showModuleStartedPrompt();
            lModule.cleanup();
        }catch(Exception e){
            logger.error("Caught an exception while launching LMS", e);

            if(lModule != null){
                lModule.cleanup();
            }

            JOptionPane.showMessageDialog(null,
                    "The LMS Module had a severe error.  Check the log file and the console window for more information.",
                    "LMS Module Error",
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
