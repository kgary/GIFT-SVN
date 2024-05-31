/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.course.LtiProperties;
import mil.arl.gift.common.AbstractLearnerTutorAction;
import mil.arl.gift.common.AbstractRuntimeParameters;
import mil.arl.gift.common.ChatLog;
import mil.arl.gift.common.CloseDomainSessionRequest;
import mil.arl.gift.common.DisplayChatWindowUpdateRequest;
import mil.arl.gift.common.DisplayMessageTutorRequest;
import mil.arl.gift.common.DomainOptionsRequest;
import mil.arl.gift.common.DomainSelectionRequest;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.ExperimentCourseRequest;
import mil.arl.gift.common.GetSurveyRequest;
import mil.arl.gift.common.InitializeDomainSessionRequest;
import mil.arl.gift.common.LearnerTutorAction;
import mil.arl.gift.common.LoginRequest;
import mil.arl.gift.common.UserData;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.UserSessionList;
import mil.arl.gift.common.WebClientInformation;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.KnowledgeSessionsReply;
import mil.arl.gift.common.course.dkf.session.KnowledgeSessionsRequest;
import mil.arl.gift.common.course.dkf.session.KnowledgeSessionsUtil;
import mil.arl.gift.common.course.dkf.session.ManageTeamMembershipRequest;
import mil.arl.gift.common.enums.ErrorEnum;
import mil.arl.gift.common.enums.GenderEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.ModuleModeEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.experiment.SubjectCreated;
import mil.arl.gift.common.gwt.server.AsyncResponseCallback;
import mil.arl.gift.common.gwt.server.SessionStatusListener;
import mil.arl.gift.common.lti.LtiGetProviderUrlRequest;
import mil.arl.gift.common.lti.LtiGetUserRequest;
import mil.arl.gift.common.lti.LtiUserRecord;
import mil.arl.gift.common.module.AbstractModule;
import mil.arl.gift.common.module.TutorModuleStatus;
import mil.arl.gift.common.survey.AbstractQuestionResponse;
import mil.arl.gift.common.usersession.UserSessionCompositeKey;
import mil.arl.gift.net.api.ConnectionFilter;
import mil.arl.gift.net.api.MessageCollectionCallback;
import mil.arl.gift.net.api.SubjectUtil;
import mil.arl.gift.net.api.message.ACK;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.MessageHandlingException;
import mil.arl.gift.net.api.message.NACK;
import mil.arl.gift.net.api.message.UserSessionMessage;
import mil.arl.gift.tutor.shared.AbstractAction;
import mil.arl.gift.tutor.shared.ClientProperties;
import mil.arl.gift.tutor.shared.KnowledgeSessionsUpdated;


/**
 * The tutoring module that the user interacts with to access the system, run
 * training scenarios, and receive tutoring.
 *
 * @author jleonard
 */
public class TutorModule extends AbstractModule {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(TutorModule.class);

    /** The active instance of the Tutor module */
    private static TutorModule instance = null;

    /** The default module name */
    private static final String DEFAULT_MODULE_NAME = "Tutor_WebServer_Module";
    
    

    /** mapping of unique GIFT user id to the user session */
    private final Map<UserSessionCompositeKey, UserWebSession> userIdToUserSessionMap = new HashMap<UserSessionCompositeKey, UserWebSession>();

    private final Map<String, UserWebSession> userSessionIdToUserSessionMap = new HashMap<String, UserWebSession>();

    private final Map<Integer, DomainWebState> domainSessionIdToWebStateMap = new HashMap<Integer, DomainWebState>();

    private final Map<String, TutorBrowserWebSession> sessionIdToBrowserSessionMap = new HashMap<String, TutorBrowserWebSession>();
    
    /** mapping of unique domain session ids to the corresponding tutor module status for each of the tutor topics */
    private final Map<Integer, TutorModuleStatus> domainIdToTutorModuleStatus = new ConcurrentHashMap<Integer, TutorModuleStatus>();
    
    /** 
     * A mapping from each browser session to the name of whatever course its user is waiting to start. An entry is assigned when a user
     * selects a domain in the TUI and is removed when that same user begins running a course.
     */
    private final Map<TutorBrowserWebSession, LoadCourseParameters> browserSessionToPendingCourseName = new ConcurrentHashMap<TutorBrowserWebSession, LoadCourseParameters>();

    /**
     * A mapping from each client session ID to a callback that should be invoked when a subject has been created for that client. This 
     * is used to allow clients for experiment subjects to begin responding to Domain session messages once database entries for the subjects
     * have been created.
     */
	protected Map<String, ExperimentSubjectCreatedCallback> preSessionIdToSubjectCreatedCallback = new HashMap<String, ExperimentSubjectCreatedCallback>();

    /**
     * Return the singleton instance of this class
     *
     * @return TutorModule
     */
    public static TutorModule getInstance() {
        if (instance == null) {
            instance = new TutorModule();
            instance.init();
        }
        return instance;
    }

    /**
     * Constructor
     */
    private TutorModule() {
        super(DEFAULT_MODULE_NAME, SubjectUtil.TUTOR_QUEUE + ADDRESS_TOKEN_DELIM + ipaddr, SubjectUtil.TUTOR_QUEUE + ADDRESS_TOKEN_DELIM + ipaddr + ADDRESS_TOKEN_DELIM + SubjectUtil.INBOX_QUEUE_SUFFIX, TutorModuleProperties.getInstance());
        
        String isSPL = System.getProperty("isSingleProcess");
        
        if(logger.isInfoEnabled()){
            logger.info("isSPL property value = "+isSPL);
        }
        
        if(isSPL != null && Boolean.parseBoolean(isSPL)) {
            
            this.setModuleMode(ModuleModeEnum.LEARNER_MODE);
        }
    }

    @Override
    protected void init() {

        //create client to send status too
        createSubjectTopicClient(SubjectUtil.TUTOR_DISCOVERY_TOPIC, false);

        //start the module heartbeat
        initializeHeartbeat();
        
        if(logger.isInfoEnabled()){
            logger.info("Tutor module has been initialized and is sending heartbeats.");
        }
    }

    @Override
    public ModuleTypeEnum getModuleType() {
        return ModuleTypeEnum.TUTOR_MODULE;
    }

    @Override
    protected void handleMessage(Message message) {

        if (logger.isInfoEnabled()) {
            logger.info(getModuleName() + " received " + message);
        }
        
        //flag used to indicate that if an exception is generated for this message than the tutor module
        //should request that the domain session be closed.
        //Default:  most messages are critical, therefore set to true
        boolean isCriticalMessage = true;
        
        try{

            if (message.getMessageType() == MessageTypeEnum.KILL_MODULE) {
                isCriticalMessage = false;
                handleKillModuleMessage(message);
            } else if (message.getMessageType() == MessageTypeEnum.START_DOMAIN_SESSION) {
                handleStartDomainSessionRequestMessage(message);
            } else if (message.getMessageType() == MessageTypeEnum.INITIALIZE_DOMAIN_SESSION_REQUEST) {
                handleInitializeDomainSessionRequestMessage(message);
            } else if (message.getMessageType() == MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST) {
                handleCloseDomainSessionRequestMessage(message);
            } else if (message.getMessageType() == MessageTypeEnum.DISPLAY_CONTENT_TUTOR_REQUEST) {
                handleDisplayContentRequestMessage(message);
            } else if (message.getMessageType() == MessageTypeEnum.DISPLAY_FEEDBACK_TUTOR_REQUEST) {
                isCriticalMessage = false;
                handleDisplayFeedbackTutorRequestMessage(message);
            } else if (message.getMessageType() == MessageTypeEnum.DISPLAY_SURVEY_TUTOR_REQUEST) {
                handleDisplaySurveyTutorPanelMessage(message);
            } else if (message.getMessageType() == MessageTypeEnum.DISPLAY_TEAM_SESSIONS) {
                handleDisplayTeamSessionRequestMessage(message);
            } else if (message.getMessageType() == MessageTypeEnum.DISPLAY_LESSON_MATERIAL_TUTOR_REQUEST) {
                handleDisplayLessonMaterialTutorPanelMessage(message);
            } else if (message.getMessageType() == MessageTypeEnum.DISPLAY_MIDLESSON_MEDIA_TUTOR_REQUEST) {
                handleDisplayMidLessonMediaTutorPanelMessage(message);
            } else if (message.getMessageType() == MessageTypeEnum.DISPLAY_AAR_TUTOR_REQUEST) {
                handleDisplayAarTutorPanelMessage(message);
            } else if (message.getMessageType() == MessageTypeEnum.LESSON_STARTED) {
                handleLessonStartedMessage(message);
            } else if (message.getMessageType() == MessageTypeEnum.LESSON_COMPLETED) {
                handleLessonCompletedMessage(message);
            } else if (message.getMessageType() == MessageTypeEnum.DISPLAY_LEARNER_ACTIONS_TUTOR_REQUEST) {
                isCriticalMessage = false;
                handleDisplayLearnerActionsMessage(message);
            } else if (message.getMessageType() == MessageTypeEnum.ACTIVE_USER_SESSIONS_REQUEST) {
                isCriticalMessage = false;
                handleActiveUserSessionsRequest(message);
            } else if (message.getMessageType() == MessageTypeEnum.DISPLAY_CHAT_WINDOW_REQUEST) {
                handleDisplayChatWindowRequestMessage(message);
            } else if (message.getMessageType() == MessageTypeEnum.DISPLAY_CHAT_WINDOW_UPDATE_REQUEST) {
                handleDisplayChatWindowUpdateRequestMessage(message);
            } else if (message.getMessageType() == MessageTypeEnum.DISPLAY_COURSE_INIT_INSTRUCTIONS_REQUEST){
            	handleDisplayCourseInitInstructionsRequest(message);
            } else if (message.getMessageType() == MessageTypeEnum.SUBJECT_CREATED){
            	handleSubjectCreatedMessage(message);
            } else if (message.getMessageType() == MessageTypeEnum.TRAINING_APP_SURVEY_RESPONSE){
                handleTrainingApplicationSurveyResponse(message);
            } else if (message.getMessageType() == MessageTypeEnum.TRAINING_APP_SURVEY_SUBMIT){
                handleTrainingApplicationSurveySubmit(message);
            } else if (message.getMessageType() == MessageTypeEnum.LOAD_PROGRESS){
                handleLoadProgress(message);
            } else if (message.getMessageType() == MessageTypeEnum.INIT_EMBEDDED_CONNECTIONS) {
                handleInitEmbeddedConnections(message);
            } else if (message.getMessageType() == MessageTypeEnum.SIMAN) {
                handleSimanMessage(message);
            } else if (message.getMessageType() == MessageTypeEnum.DISPLAY_FEEDBACK_EMBEDDED_REQUEST) {
                handleTrainingAppFeedbackRequest(message);
            } else if (message.getMessageType() == MessageTypeEnum.VIBRATE_DEVICE_REQUEST) {
                handleTrainingAppFeedbackRequest(message);
            } else if (message.getMessageType() == MessageTypeEnum.COURSE_STATE){
                handleCourseStateMessage(message);
            } else if (message.getMessageType() == MessageTypeEnum.KNOWLEDGE_SESSION_UPDATED_REQUEST){
                handleKnowledgeSessionUpdated(message);
            } else if (message.getMessageType() == MessageTypeEnum.KNOWLEDGE_SESSION_CREATED){
                handleKnowledgeSessionCreated(message);
            } else {
    
                isCriticalMessage = false;
                logger.error(getModuleName() + " received unhandled message:" + message);
                
                if(message.needsHandlingResponse()) {
                    this.sendReply(message, new NACK(ErrorEnum.UNHANDLED_MESSAGE_ERROR, "The module did not know how to handle the message"), MessageTypeEnum.PROCESSED_NACK);
                }
            }
        
        }catch(Exception e){
            
            if(isCriticalMessage){
                logger.error("Caught exception while trying to handle message, therefore trying to end the domain session.", e);
                handleCriticalMessageException(message);
            }else{
                throw new MessageHandlingException(e);
            }
        }

    }
    
    /**
     * Handler for incoming knowledge session updated messages from the domain module.
     * 
     * @param message The knowledge session updated message from the domain module.
     */
    private void handleKnowledgeSessionUpdated(Message message) {
        
        if (logger.isDebugEnabled()){
            logger.info("handleKnowledgeSessionUpdated() message received: " + message);
        }
        
        
        if ( message.getMessageType() == MessageTypeEnum.KNOWLEDGE_SESSION_UPDATED_REQUEST && message.getPayload() != null) {
            
            KnowledgeSessionsReply knowledgeSessionsReply = (KnowledgeSessionsReply)message.getPayload();
            
            synchronized (userSessionIdToUserSessionMap) {
                for (UserWebSession userSession : userSessionIdToUserSessionMap.values()) {

                    // Only broadcast to user sessions that are actively in a domain session.
                    // TODO - This could possibly be optimized further if needed by detecting
                    // if the user session is in a state (like team session UI) where it needs
                    // to be aware of knowledge session changes.
                    if (userSession.getDomainWebState() != null) {

                        // during tutor runtime, the user only needs to be notified of changes to courses that 
                        // match the current course id of the user.
                        String userCourse = userSession.getDomainWebState().getDomainSourceId();
                        Map<Integer, AbstractKnowledgeSession> filteredMap = 
                                KnowledgeSessionsUtil.filterKnowledgeSessions(userCourse, knowledgeSessionsReply.getKnowledgeSessionMap());
                        
                        // Only send the filtered list to the client.
                        KnowledgeSessionsReply reply = new KnowledgeSessionsReply(filteredMap);
                        
                        // Put the payload map here instead of the internal map because client cannot serialize a concurrent hash
                        // map.
                        KnowledgeSessionsUpdated action = new KnowledgeSessionsUpdated(reply);
                        
                        userSession.broadcastMessageToBrowsers(action);
                    }
                    
                }
            }
        } 

        if(message.needsHandlingResponse()){
            // Send a processed ack back whether this succeeds or fails internally.
            sendReply(message, null , MessageTypeEnum.KNOWLEDGE_SESSION_UPDATED_REPLY);
        }
        
    }
    
    /**
     * Handler for incoming knowledge session created messages from the domain module.
     * 
     * @param message The knowledge session created message from the domain module.
     */
    private void handleKnowledgeSessionCreated(Message message) {
        
        if (logger.isDebugEnabled()){
            logger.info("handleKnowledgeSessionCreated() message received: " + message);
        }

        // Send a processed ack back whether this succeeds or fails internally.
        sendReply(message, null , MessageTypeEnum.PROCESSED_ACK);
        
    }

    /**
     * Handles a message indicating that an experiment subject has been created. This method will register the subject's browser session with the 
     * tutor module and notify the Domain once the subject is ready to begin an experiment course.
     * 
	 * @param message The received message
	 */
	private void handleSubjectCreatedMessage(final Message message) {
		
		if(message instanceof UserSessionMessage){
			
			UserSessionMessage userMessage = (UserSessionMessage) message;
			
			final UserSession session = userMessage.getUserSession();
			
			if(session != null){
				
				if(message.getPayload() instanceof SubjectCreated){
					
					SubjectCreated payload =  (SubjectCreated) message.getPayload();
									
					final String sessionId = payload.getPreSessionId();
					
					//get the callback that the presession with the given ID is waiting on
					final ExperimentSubjectCreatedCallback callback = preSessionIdToSubjectCreatedCallback.remove(sessionId);
					
					if(logger.isInfoEnabled()){
					    logger.info("Setting up UMS connection for experiment subject with ID: " + session.getUserId());
					}
					
					if(!haveSubjectClient(session, ModuleTypeEnum.UMS_MODULE)){
						
						//need to set up a UMS connection so that the subject can be logged out later on
				        MessageCollectionCallback umsCallback = new MessageCollectionCallback() {
				            
				            @Override
				            public void success() {
				            	
								if(logger.isInfoEnabled()){
								    logger.info("UMS client successfully established for experiment subject with ID: " + session.getUserId());
								}
				            	
				            	//register the subject's web sessions and create a browser session for them
								final TutorBrowserWebSession browserSession = callback.onSubjectCreated(session);
								
								//prepare the browser session to begin the experiment course
								if(browserSession != null){
	                                browserSessionToPendingCourseName.put(browserSession, new LoadCourseParameters(sessionId, sessionId));
	                                
	                                sendReply(
	                                        message, 
	                                        new ACK(),
	                                        MessageTypeEnum.PROCESSED_ACK);
								}else{
								    
								    NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "Failed to create the appropriate session for experiment user after establishing a connection to the UMS module due to an error");
								    nack.setErrorHelp("The browserwebsession was null.  There is most likely an error in the tutor log file that mentions why.  This is for user session "+session);
                                    sendReply(
                                            message, 
                                            nack,
                                            MessageTypeEnum.PROCESSED_NACK);
								}
				                
				            }
				      
				            @Override
				            public void received(final Message aMsg) {
		
				            }
				    
				            @Override
				            public void failure(Message nackMsg) {
				            	
				            	logger.warn("Failed to create a web session for an experiment subject because a UMS client could not be established for the subject."
										+ "Received message: " + nackMsg.toString());
				            	
				            	sendReply(
										message, 
										new NACK(ErrorEnum.OPERATION_FAILED, 
												"Failed to create a web session for an experiment subject because a UMS client could not be established for the subject."
													+ "Received message: " + nackMsg.toString()),
										MessageTypeEnum.NACK);
				            }
				    
				            @Override
				            public void failure(String why) {
				            	
				            	logger.warn("Failed to create a web session for an experiment subject because a UMS client could not be established for the subject."
										+ "Reason: " + why);
				            	
				            	sendReply(
										message, 
										new NACK(ErrorEnum.OPERATION_FAILED, 
												"Failed to create a web session for an experiment subject because a UMS client could not be established for the subject."
													+ "Reason: " + why),
										MessageTypeEnum.NACK);
				            }
				        };		       
					
						selectModule(session, ModuleTypeEnum.UMS_MODULE, umsCallback);		
						
					} else {
						
						//register the subject's web sessions and create a browser session for them
						final TutorBrowserWebSession browserSession = callback.onSubjectCreated(session);
						
						//prepare the browser session to begin the experiment course
						if(browserSession != null){
    						browserSessionToPendingCourseName.put(browserSession, new LoadCourseParameters(sessionId, sessionId));
    						
    						sendReply(
    								message, 
    								new ACK(),
    								MessageTypeEnum.PROCESSED_ACK);
						}else{
                            NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "Failed to create the appropriate session for experiment user due to an error");
                            nack.setErrorHelp("The browserwebsession was null.  There is most likely an error in the tutor log file that mentions why.  This is for user session "+session);
                            sendReply(
                                    message, 
                                    nack,
                                    MessageTypeEnum.PROCESSED_NACK);
						}
					}
					
				} else {
					
					logger.warn("Failed to create a web session for an experiment subject because the received message payload was not a session ID. "
							+ "Received message: " + message.toString());
					
					sendReply(
							message, 
							new NACK(ErrorEnum.OPERATION_FAILED, 
									"Failed to create a web session for an experiment subject because the received message payload was not a session ID. "
										+ "Received message: " + message.toString()),
							MessageTypeEnum.NACK);
				}
				
			} else {
				
				logger.warn("Failed to create a web session for an experiment subject because the received subject created message does not have a user session."
						+ " message. Received message: " + message.toString());
		
				sendReply(
						message, 
						new NACK(ErrorEnum.OPERATION_FAILED, 
								"Failed to create a web session for an experiment subject because the received subject created message does not have a user session."
								+ " message. Received message: " + message.toString()), 
						MessageTypeEnum.NACK);
			}
			
		} else {
			
			logger.warn("Failed to create a web session for an experiment subject because the received subject created message is not a user"
							+ " message. Received message: " + message.toString());
			
			sendReply(
					message, 
					new NACK(ErrorEnum.OPERATION_FAILED, 
							"Failed to create a web session for an experiment subject because the received subject created message is not a user"
							+ " message. Received message: " + message.toString()), 
					MessageTypeEnum.NACK);
		}
		
		
	}

	/**
     * This method will present some text to the user on the TUI describing that a critical exception was
     * caught and the domain session needs to be closed.  After the user acknowledges the text the tutor
     * will request that the domain module close the domain session.
     * 
     * @param msg the incoming message that caused an exception which can't be overcome, i.e. the domain session
     * is most likely in limbo after this exception leaving the user wondering what to do next.
     */
    private void handleCriticalMessageException(Message msg){
        
        if(msg instanceof DomainSessionMessage){
            
            DomainSessionMessage domainSessionMessage = (DomainSessionMessage)msg;
            final UserSession userSession = domainSessionMessage.getUserSession();
   
            if (userSession != null) {
                final DomainWebState domainWebState = domainSessionIdToWebStateMap.get(domainSessionMessage.getDomainSessionId());
    
                if (domainWebState != null) {
    
                    MessageTypeEnum messageType = domainSessionMessage.getMessageType();
                    DisplayMessageTutorRequest displayTextRequest = DisplayMessageTutorRequest.createTextRequest(null, "A critical exception happened when handling a "+messageType.getDisplayName()+".  Therefore the domain session must terminate.");
    
                    ActionListener listener = new ActionListener() {
                        @Override
                        public void onAction(AbstractAction action) {
                            
                            if(logger.isInfoEnabled()){
                                logger.info("The request from a DisplayMessageTutorRequest has been fulfilled, received: " + action);
                            }
    
                            //close the domain session
                            TutorModule.getInstance().endDomainSession(userSession, new AsyncResponseCallback() {

                                @Override
                                public void notify(boolean success, String response, String additionalInformation) {
                                    if (success) {
                                        domainWebState.onDomainSessionClosed();
                                    }
                                }
                            });
                        }
                    };
    
                    //display the reason for the domain session being closed
                    domainWebState.displayArticleContent(displayTextRequest, listener);
                    
                }else{
                    logger.error("Unable to find a domain session web session for user id of "+domainSessionMessage.getUserId()+" in domain session id of "+domainSessionMessage.getDomainSessionId()+".  Therefore not able to close the domain session.  If the domain session is able to recover from this issue than" +
                            " this error message can probably be ignored.");
                }
                
            }else{
                logger.error("Unable to find a user web session for user id of "+domainSessionMessage.getUserId()+".  Therefore not able to close the domain session.  If the domain session is able to recover from this issue than" +
                		" this error message can probably be ignored.");
            }
        }else{
            logger.error("Currently unable to handle a critical message exception for a non-domain session message like "+msg.getMessageType()+".");
        }
    }

    /**
     * Handles the message to kill this module
     *
     * @param msg The Kill Module message
     */
    private void handleKillModuleMessage(Message msg) {
        Thread killModule = new Thread("Kill Module") {

            @Override
            public void run() {
                killModule();
            }
        };
        killModule.start();
    }

    @Override
    public void sendModuleStatus() {
        /* Send module status */
        sendMessage(SubjectUtil.TUTOR_DISCOVERY_TOPIC, moduleStatus, MessageTypeEnum.MODULE_STATUS, null);

        /* Send topic statuses. The concurrent hashmap .values() is safe and
         * will not throw a ConcurrentModificationException. */
        for (TutorModuleStatus tutorStatus : domainIdToTutorModuleStatus.values()) {
            sendMessage(SubjectUtil.TUTOR_DISCOVERY_TOPIC, tutorStatus, MessageTypeEnum.TUTOR_MODULE_STATUS, null);
        }
    }
    
    /**
     * Creates a browser session for a user session
     *
     * @param userSession The user session to create a browser session
     * @param clientAddress The tutor client address for this create session request
     * @param client Information about this client that will be used to handle the browser session
     * @return BrowserSession The created browser session
     */
    public TutorBrowserWebSession createBrowserSession(UserWebSession userSession, String clientAddress, ClientProperties client) {
    	
        WebClientInformation clientInfo = new WebClientInformation();
        clientInfo.setClientAddress(clientAddress);
        clientInfo.setMobileAppProperties(client.getMobileAppProperties());
        final TutorBrowserWebSession browserSession = new TutorBrowserWebSession(userSession.getUserSessionKey(), clientInfo, client.getWebSocketId());
        userSession.addBrowserSession(browserSession);
        sessionIdToBrowserSessionMap.put(browserSession.getBrowserSessionKey(), browserSession);
        browserSession.addStatusListener(new SessionStatusListener() {

            @Override
            public void onStop() {
            }

            @Override
            public void onEnd() {
                sessionIdToBrowserSessionMap.remove(browserSession.getBrowserSessionKey());
            }
        });        
        
        return browserSession;
    }

    /**
     * Gets the browser session for a given browser session key
     *
     * @param browserSessionKey The browser session key
     * @return BrowserSession The browser session
     */
    public TutorBrowserWebSession getBrowserSession(String browserSessionKey) {
        return sessionIdToBrowserSessionMap.get(browserSessionKey);
    }

    /**
     * Creates a user session
     *
     * @param userSession - information about the user session (including the unique user id of the learner) 
     *                      to associate a web sesison with
     * @return UserSession The created user session
     */
    public UserWebSession createUserSession(final UserSession userSession) {
        
        if(logger.isInfoEnabled()){
            logger.info("Creating user session for user: " + userSession);
        }
        final UserWebSession session = new UserWebSession(userSession);
        UserSessionCompositeKey key = new UserSessionCompositeKey(userSession.getUserId(), userSession.getExperimentId(), 
                userSession.getGlobalUserId(), userSession.getSessionType());
        userIdToUserSessionMap.put(key, session);
        userSessionIdToUserSessionMap.put(session.getUserSessionKey(), session);
        session.addStatusListener(new SessionStatusListener() {

            @Override
            public void onStop() {
                if(logger.isInfoEnabled()){
                    logger.info("Removing user session from collection because the session has stopped.  "+userSession);
                }
                UserSessionCompositeKey key = new UserSessionCompositeKey(userSession.getUserId(), userSession.getExperimentId(), 
                        userSession.getGlobalUserId(), userSession.getSessionType());
                userIdToUserSessionMap.remove(key);
                userSessionIdToUserSessionMap.remove(session.getUserSessionKey());
            }

            @Override
            public void onEnd() {
            }
        });
        return session;
    }

    /**
     * Gets the user session for a given user ID
     *
     * @param userId The user ID
     * @return UserSession The user session
     */
    //public UserWebSession getUserSession(final int userId) {
    //    UserWebSession session = userIdToUserSessionMap.get(userId);
    //    return session;
    //}
    
    /**
     * Gets the user session for a given user ID
     *
     * @param userId The user ID
     * @return UserSession The user session
     */
    public UserWebSession getUserSession(UserSession userSession) {
        
        if (userSession == null) {
            throw new IllegalArgumentException("userSession parameter cannot be null.");
        }
        UserSessionCompositeKey key = new UserSessionCompositeKey(userSession.getUserId(), userSession.getExperimentId(), 
                userSession.getGlobalUserId(), userSession.getSessionType());
        UserWebSession webSession = userIdToUserSessionMap.get(key);
        return webSession;
    }
    
    public UserWebSession getUserSession(UserSessionMessage msg) {
        UserSessionMessage userSessionMessage = msg;
        UserSession userSession = userSessionMessage.getUserSession();
        UserWebSession userWebSession = getUserSession(userSession);
        
        return userWebSession;
    }


    /**
     * Gets the user session for a given user session key
     *
     * @param userSessionKey The user session key
     * @return UserSession The user session
     */
    public UserWebSession getUserSession(final String userSessionKey) {
        UserWebSession userSession = userSessionIdToUserSessionMap.get(userSessionKey);
        return userSession;
    }

    /**
     * Creates a domain web state for a given user and domain session ID
     *
     * @param userId The user ID
     * @param domainSessionId The domain session ID
     * @param domainRuntimeId the selected domain runtime id which is the path to the course used when taking the course
     * @param domainSourceId the selected domain source id which is the path to the authored course
     * @param experimentId the unique experiment id.  Can be null if the domain session is not part of an experiment.
     * @return DomainWebState The created domain web state object.  This holds the state of the domain session as it 
     * should be presented to the web clients.
     */
    public DomainWebState createDomainWebState(final UserSession userSession, final int domainSessionId, final String domainRuntimeId, final String domainSourceId, final String experimentId) {
        final int userId = userSession.getUserId();
        final UserWebSession userWebSession = getUserSession(userSession);
        if (userWebSession != null) {
        	
        	if(userWebSession.getDomainWebState() == null){
        	    if(logger.isInfoEnabled()){
        	        logger.info("Creating new domain web session for userId = "+userId+", domainSessionId = "+domainSessionId+".");
        	    }
        		
	            final DomainWebState domainWebState = new DomainWebState(userWebSession, userSession, domainSessionId, domainRuntimeId, domainSourceId, experimentId);
	            domainSessionIdToWebStateMap.put(domainSessionId, domainWebState);
	            
	            userWebSession.setDomainWebState(domainWebState);  
	            return domainWebState;
        	
        	} else {
        		return userWebSession.getDomainWebState();
        	}
        }
        return null;
    }
    
    public void removePedagogicalInstance(UserSession userSession, int domainSessionId){        
        domainSessionIdToWebStateMap.remove(domainSessionId);
        allocationStatus.removeUserSession(userSession);
    }

    /**
     * Gets the domain web state for a given domain session ID
     *
     * @param domainSessionId The domain session ID
     * @return DomainSessionWebState The web state for the domain session.
     */
    public DomainWebState getDomainWebState(final int domainSessionId) {
        DomainWebState webState = domainSessionIdToWebStateMap.get(domainSessionId);
        return webState;
    }

    /**
     * Ends an active domain session for a user
     *
     * @param userSessionId The user ID of the user the domain session should be
     * ended for
     * @param callback A callback for when the action is complete
     */
    public void endDomainSession(final UserSession userSession, final AsyncResponseCallback callback) {
        final UserWebSession uSession = getUserSession(userSession);
        if (uSession != null && uSession.getDomainWebState() != null) {

            MessageCollectionCallback msgCollectionCallback = new MessageCollectionCallback() {

                @Override
                public void success() {
                    callback.notify(true, "success", null);
                }

                @Override
                public void received(Message msg) {
                    // Do nothing
                }

                @Override
                public void failure(Message msg) {
                    String message = "Failed to end the domain session because '" + ((NACK) msg.getPayload()).getErrorMessage()+"'.";
                    logger.error(message);
                    
                    if(msg.getMessageType() == MessageTypeEnum.PROCESSED_NACK){
                        
                        NACK nack = (NACK) msg.getPayload();
                        if(nack.getErrorEnum() == ErrorEnum.DOMAIN_SESSION_NOT_FOUND_ERROR){
                            logger.warn("The domain module was unable to gracefully close the domain session, however the tutor and domain module are still working to close the domain session.");
                            callback.notify(true, "The domain session was not found, therefore continuing on with ending the domain session.", null);
                            return;
                        }
                    }
                    
                    callback.notify(false, message, null);
                }

                @Override
                public void failure(String why) {
                    String message = "Failed to end the domain session because '" + why +"'.";
                    logger.error(message);
                    
                    if(logger.isInfoEnabled()){
                        logger.info("Sending Close domain session request to all other modules besides the domain module in an attempt allow them to reset themselves in lieu of any issues the domain module might be having.");
                    }
                    
                    //Attempt to send close domain session to all other modules except the domain module
                    //Don't care if it fails since the domain module already failed and is possibly not usable again at this point, 
                    //therefore try to salvage the other modules for re-use in the next domain/user session.
                    sendDomainSessionMessage(new ModuleTypeEnum[]{ModuleTypeEnum.LEARNER_MODULE, ModuleTypeEnum.PEDAGOGICAL_MODULE, ModuleTypeEnum.UMS_MODULE, ModuleTypeEnum.SENSOR_MODULE, ModuleTypeEnum.GATEWAY_MODULE}, 
                            new CloseDomainSessionRequest("Domain Module Failed to handle closing the domain session requested by the Tutor."), 
                            uSession.getUserSessionInfo(), 
                            uSession.getDomainWebState().getDomainSessionId(),
                            uSession.getDomainWebState().getExperimentId(),
                            MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST, null);
                    
                    callback.notify(false, message, null);
                }
            };

            sendDomainSessionMessage(ModuleTypeEnum.DOMAIN_MODULE, new CloseDomainSessionRequest(), uSession.getUserSessionInfo(), 
                    uSession.getDomainWebState().getDomainSessionId(),
                    MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST, msgCollectionCallback);
        }
    }
    
    /**
     * Request the User Id from the UMS using the GIFT username as the key.
     * 
     * @param username the GIFT username to use to lookup the GIFT user id
     * @param callback used when the request is replied too
     */
    public void sendGetUserIdMessage(final String username, final MessageCollectionCallback callback){
        
        //get UMS connection
        MessageCollectionCallback umsCallback = new MessageCollectionCallback() {
            
            @Override
            public void success() {
                
                if(logger.isInfoEnabled()){
                    logger.info("Sending get user id request message for username of " + username);
                }
                sendMessage(username, MessageTypeEnum.USER_ID_REQUEST, callback);
            }
      
            @Override
            public void received(final Message aMsg) {

            }
    
            @Override
            public void failure(Message nackMsg) {
                callback.failure(nackMsg);
            }
    
            @Override
            public void failure(String why) {
                callback.failure(why);
            }
        };
        
        selectUMSModule(umsCallback);
    }

    /**
     * Sends a create user message to the UMS
     *
     * @param isMale If the user to be created is male
     * @param lmsUsername The LMS username of the user to be created
     * @param callback Callback for the result of the message
     */
    public void sendCreateUserMessage(final boolean isMale, final String lmsUsername, final MessageCollectionCallback callback) {
        
        //get UMS connection
        MessageCollectionCallback umsCallback = new MessageCollectionCallback() {
            
            @Override
            public void success() {
                
                final UserData userData = new UserData(lmsUsername, isMale ? GenderEnum.MALE : GenderEnum.FEMALE);
                if(logger.isInfoEnabled()){
                    logger.info("Sending create new user request message for " + userData);
                }
                sendMessage(userData, MessageTypeEnum.NEW_USER_REQUEST, callback);
            }
      
            @Override
            public void received(final Message aMsg) {

            }
    
            @Override
            public void failure(Message nackMsg) {
                callback.failure(nackMsg);
            }
    
            @Override
            public void failure(String why) {
                callback.failure(why);
            }
        };
        
        selectUMSModule(umsCallback);
    }
    
    /**
     * Gets a specific survey for a BrowserSession by making a request to the UMS module.
     * @param browserSessionKey the key for the browser session which is requesting the survey
     * @param surveyContextId the id for the surveyContext in which the survey resides
     * @param giftKey the string that identifies the survey (also used in the course.xml file)
     * @param callback the callback to invoke once the result has been received
     */
    public void sendGetSurveyRequest(String browserSessionKey, int surveyContextId, String giftKey, MessageCollectionCallback callback) {
        TutorBrowserWebSession bws = getBrowserSession(browserSessionKey);
        if(bws != null) {
            UserWebSession uws = getUserSession(bws.getUserSessionKey());
            if(uws != null) {
                DomainWebState dws = uws.getDomainWebState();
                if(dws != null) {
                    GetSurveyRequest getSurveyRequest = new GetSurveyRequest(surveyContextId, giftKey);
                    sendDomainSessionMessage(
                            getSurveyRequest, 
                            dws.getUserSession(), 
                            dws.getDomainSessionId(),
                            MessageTypeEnum.GET_SURVEY_REQUEST, 
                            callback);            
                } else {
                    callback.failure("The UserWebSession " 
                            + uws.toString() + " DomainWebSession was null.");
                }
            } else {
                callback.failure("There was no UserWebSession for the user session key " + bws.getUserSessionKey());
            }
        } else {
            callback.failure("There was no BrowserWebSession for the browser session key " + browserSessionKey);
        }
    }
    
    public void sendTutorSurveyQuestionResponse(AbstractQuestionResponse questionResponse, UserSession userSession, int domainSessionId) {
        //send question response to domain
        sendDomainSessionMessage(ModuleTypeEnum.DOMAIN_MODULE, questionResponse, userSession, 
                domainSessionId, MessageTypeEnum.TUTOR_SURVEY_QUESTION_RESPONSE, null);
    }
    

    
    /**
     * Sends a request to manage the team membership session.  This can include actions such as hosting/unhosting,
     * joining/leaving and selecting roles within the team.
     * 
     * @param userSession The user making the request
     * @param domainSessionId The domain session id of the user making the request.
     * @param request The request to send to the domain module.
     * @param callback The callback to handle the response.  Cannot be null.
     */
    public void manageTeamSessionRequest(UserSession userSession, int domainSessionId, ManageTeamMembershipRequest request,
            MessageCollectionCallback callback) {
        
        sendDomainSessionMessage(ModuleTypeEnum.DOMAIN_MODULE, request, userSession,
                domainSessionId, MessageTypeEnum.MANAGE_MEMBERSHIP_TEAM_KNOWLEDGE_SESSION, callback);
    }
    
    /**
     * Sends a request to get the active knowledge sessions from the domain module.
     * 
     * @param userSession the user making the request
     * @param domainSessionId the domain session id of the user making the request
     * @param request the filter information for the returned knowledge sessions 
     * @param callback the callback to handle the response.  Cannot be null.
     */
    public void sendKnowledgeSessionRequestMessage(UserSession userSession, int domainSessionId, KnowledgeSessionsRequest request,
            MessageCollectionCallback callback){
        
        sendDomainSessionMessage(ModuleTypeEnum.DOMAIN_MODULE, request, userSession,
                domainSessionId, MessageTypeEnum.ACTIVE_KNOWLEDGE_SESSIONS_REQUEST, callback);
    }
    
    /**
     * Send a request to the domain module to start the team session.
     * 
     * @param userSession The user session making the request.
     * @param domainSessionId The domain session id of the user making the request.
     * @param callback The callback to handle the request.  Cannot be null.
     */
    public void startTeamSessionRequest(UserSession userSession, int domainSessionId,
            MessageCollectionCallback callback) {
        sendDomainSessionMessage(null, userSession,
                domainSessionId, MessageTypeEnum.START_TEAM_KNOWLEDGE_SESSION_REQUEST, callback);
    }
    
    /**
     * Sends a login message to the UMS to login a user
     *
     * @param userSession - information about the user session (including the unique user id of the learner) 
     *                      the message is associated with and is required for login purposes
     * @param callback Callback for the result of the message
     */
    public void sendLoginMessage(final UserSession userSession, final MessageCollectionCallback callback) {
        
        //get UMS connection - this is needed to create user session messages
        final MessageCollectionCallback umsCallback = new MessageCollectionCallback() {
            
            @Override
            public void success() {
                
                if(logger.isInfoEnabled()){
                    logger.info("Sending login message for user " + userSession +".");
                }
                LoginRequest request = new LoginRequest(userSession.getUserId());
                request.setUsername(userSession.getUsername());
                request.setUserType(userSession.getSessionType());
                sendUserSessionMessage(request, userSession, MessageTypeEnum.LOGIN_REQUEST, callback);
            }
      
            @Override
            public void received(final Message aMsg) {

            }
    
            @Override
            public void failure(Message nackMsg) {
                callback.failure(nackMsg);
            }
    
            @Override
            public void failure(String why) {
                callback.failure(why);
            }
        };
        
        //get LMS connection - this is needed to send MessageTypeEnum.START_TEAM_KNOWLEDGE_SESSION_REQUEST to the LMS module
        //
        final MessageCollectionCallback lmsCallback = new MessageCollectionCallback() {
            
            @Override
            public void success() {
                selectModule(userSession, ModuleTypeEnum.UMS_MODULE, umsCallback);                
            }
      
            @Override
            public void received(final Message aMsg) {

            }
    
            @Override
            public void failure(Message nackMsg) {
                callback.failure(nackMsg);
            }
    
            @Override
            public void failure(String why) {
                callback.failure(why);
            }
        };
        
        selectModule(userSession, ModuleTypeEnum.LMS_MODULE, lmsCallback);  
    }

    /**
     * Sends a request from the Tutor Module to the UMS Module to get the Lti User Record based on the consumer
     * key and consumer id.  This selects/allocates the UMS Module first.
     * 
     * @param consumerKey The consumer key of the lti user.
     * @param consumerId The consumer id of the lti user.
     * @param callback Callback for the result of the message.
     */
    public void getLtiUserRecord(final String consumerKey, final String consumerId, final LtiGetUserRecordCallback callback) {

        //get UMS connection
        MessageCollectionCallback umsCallback = new MessageCollectionCallback() {
            
            @Override
            public void success() {
                
                if(logger.isInfoEnabled()){
                    logger.info("Getting the lti user record for consumer key: " + consumerKey + ", consumerId: " + consumerId);
                }
                LtiGetUserRequest request = new LtiGetUserRequest(consumerKey, consumerId);

                sendUserSessionMessage(ModuleTypeEnum.UMS_MODULE, request,  new UserSession(UserSessionMessage.PRE_USER_UNKNOWN_ID), MessageTypeEnum.LTI_GETUSER_REQUEST, new MessageCollectionCallback() {

                    private Message reply = null;
                    
                    @Override
                    public void success() {
                        if(logger.isInfoEnabled()){
                            logger.info("Received success for lti get user record with reply: " + reply);
                        }
                        
                        
                        LtiUserRecord payload = (LtiUserRecord) reply.getPayload();
                        callback.onSuccess(payload);
                    }

                    @Override
                    public void received(Message msg) {
                        if(logger.isInfoEnabled()){
                            logger.info("getLtiUserRecord received: " + msg);
                        }
                        if (msg.getMessageType() == MessageTypeEnum.LTI_GETUSER_REPLY) {
                            reply = msg;
                        }
                    }

                    @Override
                    public void failure(Message msg) {
                        logger.error("getLtiUserRecord() failure occurred msg=" + msg);
                        callback.onFailure("getLtiUserRecord() failure occurred.  ", "Message: " + msg);
                        
                    }

                    @Override
                    public void failure(String why) {
                         logger.error("getLtiUserRecord() failure occurred why=" + why);
                         callback.onFailure("getLtiUserRecord() failure occurred.  ", "Why: " + why);
                    }
                    
                });
            }
      
            @Override
            public void received(final Message aMsg) {

            }
    
            @Override
            public void failure(Message nackMsg) {
                logger.error("selectUMSModule() failure occurred message=" + nackMsg);
                callback.onFailure("selectUMSModule() failure occurred.  ", "Message: " + nackMsg);
            }
    
            @Override
            public void failure(String why) {
                logger.error("selectUMSModule() failure occurred why=" + why);
                callback.onFailure("selectUMSModule() failure occurred.  ", "Why: " + why);
            }
        };
        
        selectUMSModule(umsCallback);
    }

    /**
     * Gets the domain options for a user from the Domain module
     *
     * @param userSession - information about the user session (including the unique user id of the learner) 
     *                      the request is associated with
     * @param lmsUsername the LMS User name to use to query for LMS records for a GIFT user
     * @param session - information about the client session
     * @param callback Callback for the result of the message
     */
    public void getDomainOptions(final UserSession userSession, final String lmsUsername, final TutorBrowserWebSession session, final MessageCollectionCallback callback) {
        
        //first make sure domain module is connected since this is the first domain module message sent 
        //by the tutor after a user session starts
        selectModule(userSession, ModuleTypeEnum.DOMAIN_MODULE, new MessageCollectionCallback() {


            @Override
            public void success() {
                
                DomainOptionsRequest request = new DomainOptionsRequest(session.getClientInformation());
                request.setLMSUserName(lmsUsername);

                sendUserSessionMessage(request,
                        userSession,
                        MessageTypeEnum.DOMAIN_OPTIONS_REQUEST,
                        callback);
            }

            @Override
            public void received(Message msg) {

            }

            @Override
            public void failure(Message msg) {
                logger.error("Failed to select a domain module because a failure message was returned: " + msg.getPayload().toString());
                callback.failure(msg);
            }

            @Override
            public void failure(String why) {
                logger.error("Failed to select a domain module because some failure occurred: " + why);
                callback.failure(why);
            }
        });  

    }

    /**
     * Selects a domain for a user from the Domain module
     *
     * @param userSession - information about the user session (including the unique user id of the learner) 
     *                      the request is associated with. Can't be null.
     * @param lmsUsername The LMS username. Can't be null or empty.
     * @param domainRuntimeId the selected domain runtime id which is the path to the course used when taking the course. Can't be null or empty.
     * @param domainSourceId the selected domain source id which is the path to the authored course. Can't be null or empty.
     * @param browserSession The session information needed for the domain selection request. Can't be null.
     * @param runtimeParmas (Optional - can be null) Additional parameters used to configure the domain session at runtime.
     * @param callback Callback for the result of the message. Can't be null.
     */
    public void selectDomain(final UserSession userSession, final String lmsUsername, final String domainRuntimeId, final String domainSourceId,
            final TutorBrowserWebSession browserSession, final AbstractRuntimeParameters runtimeParams, final MessageCollectionCallback callback) {
        
        if(browserSession == null){
            throw new IllegalArgumentException("The browser session can't be null.");
        }else if(userSession == null){
            throw new IllegalArgumentException("The user session can't be null.");
        }else if(callback == null){
            throw new IllegalArgumentException("The callback can't be null.");
        }

        //make sure we have a domain module allocated
        //This is mainly here for when the request domain options logic was bypassed and a course is being loaded directly (i.e. bypassing the user interface) 
        selectModule(userSession, ModuleTypeEnum.DOMAIN_MODULE, new MessageCollectionCallback() {
            
            @Override
            public void success() {
                
                //remember which browser session was used to select the domain so we know it is waiting to start a course
                browserSessionToPendingCourseName.put(browserSession, new LoadCourseParameters(domainRuntimeId, domainSourceId));
                
                DomainSelectionRequest request = new DomainSelectionRequest(lmsUsername, domainRuntimeId, domainSourceId, 
                        browserSession.getClientInformation(), runtimeParams);
                sendUserSessionMessage(request,
                        userSession,
                        MessageTypeEnum.DOMAIN_SELECTION_REQUEST,
                        callback);                
            }
            
            @Override
            public void received(Message msg) {
               
            }
            
            @Override
            public void failure(Message msg) {
                logger.error("Failed to select a domain module because a failure message was returned: " + msg.getPayload().toString());
                callback.failure(msg);
            }

            @Override
            public void failure(String why) {
                logger.error("Failed to select a domain module because some failure occurred: " + why);
                callback.failure(why);
            }

        });
        

    }

    /**
     * The learner has completed a report using the TUI.
     *
     * @param learnerAction information about the action taken.  Can't be null.
     * @param webState - The domain web state that is sending the action.
     * @param callback for handling responses to the user action message.  Can be null.
     */
    public void sendUserActionTaken(final AbstractLearnerTutorAction learnerAction, DomainWebState webState, MessageCollectionCallback callback) {

        LearnerTutorAction action = new LearnerTutorAction(learnerAction);
        sendDomainSessionMessage(action, webState.getUserSession(), webState.getDomainSessionId(), MessageTypeEnum.LEARNER_TUTOR_ACTION, callback);
    }
    
    /**
     * Sends a chat message to the domain module
     * 
     * @param chatLog contains the history of the chat including tutor and user inputs
     * @param webState - The domain web state that is sending the chat message.
     * @param callback for handling responses to the chat log update.  Can be null.
     */
    public void sendChatMessage(final ChatLog chatLog, DomainWebState webState, MessageCollectionCallback callback) {
        
        sendDomainSessionMessage(chatLog, webState.getUserSession(), webState.getDomainSessionId(), MessageTypeEnum.CHAT_LOG, callback);
    }

    /**
     * Sends a logout message to the UMS to logout a user
     *
     * @param userSession - information about the user session (including the unique user id of the learner) 
     *                      the request is associated with
     * @param callback Callback for the result of the message
     */
    public void sendLogoutMessage(final UserSession userSession, MessageCollectionCallback callback) {
        sendUserSessionMessage(null, userSession, MessageTypeEnum.LOGOUT_REQUEST, callback);
    }
    
    /**
     * Starts the course belonging to the experiment with the given ID
     *
     * @param experimentId the experiment whose course to start
     * @param experimentFolder the folder of the experiment course relative to
     *        the runtime experiment folder. Should only be null for legacy
     *        experiments.
     * @param clientAddress the address for the client from which this method was invoked
     * @param client properties describing the client. Used to determine if the client is within the GIFT mobile app.
     * @param callback Callback for the result of the message
     */
    public void sendStartExperimentCourseMessage(final String experimentId, final String experimentFolder, final String clientAddress, final ClientProperties client, final ExperimentSubjectCreatedCallback subjectCallback, final MessageCollectionCallback callback) {
    	        
    	// THIRD: after Domain module connection, let the domain module know to start the experiment course
        final MessageCollectionCallback domainCallback = new MessageCollectionCallback() {
            
            @Override
            public void success() {
            	
            	WebClientInformation clientInfo = new WebClientInformation();
                clientInfo.setClientAddress(clientAddress);
                
                if(client != null) {
                    clientInfo.setMobileAppProperties(client.getMobileAppProperties());
                }
                
                String preSessionId = UUID.randomUUID().toString();
            	ExperimentCourseRequest request = new ExperimentCourseRequest(experimentId, experimentFolder, clientInfo, preSessionId);
            	
            	//set up logic to be executed once an experiment subject has been created
            	preSessionIdToSubjectCreatedCallback.put(preSessionId, subjectCallback);
                
                sendMessage(ModuleTypeEnum.DOMAIN_MODULE, request, MessageTypeEnum.EXPERIMENT_COURSE_REQUEST, callback);
            }
      
            @Override
            public void received(final Message aMsg) {

            }
    
            @Override
            public void failure(Message nackMsg) {
                callback.failure(nackMsg);
            }
    
            @Override
            public void failure(String why) {
                callback.failure(why);
            }
        };
        
        // SECOND:  after UMS module connection, get Domain module connection
        MessageCollectionCallback umsCallback = new MessageCollectionCallback() {
            
            @Override
            public void success() {
                selectDomainModule(domainCallback);
            }
      
            @Override
            public void received(final Message aMsg) {

            }
    
            @Override
            public void failure(Message nackMsg) {
                callback.failure(nackMsg);
            }
    
            @Override
            public void failure(String why) {
                callback.failure(why);
            }
        };
        
        // FIRST: get UMS connection - this was added for #4917 in order for the domain module to be on the same computer as the UMS
        selectUMSModule(umsCallback);
        
    }
    
    /**
     * Sets up a module client for the given user session to the Domain module at the given address
     *
     * @param userSession the user session to establish a module client for
     * @param domainAddress the address of the domain module to establish a module client to
     */
    public void linkDomainModuleToUser(final UserSession userSession, String domainAdress) {
        
        ConnectionFilter addressFilter = new ConnectionFilter();
        addressFilter.addRequiredAddress(domainAdress);
        
        linkModuleClient(userSession, ModuleTypeEnum.DOMAIN_MODULE, addressFilter);
    }

    @Override
    protected void cleanup() {
        super.cleanup();
    }
    
    public Map<TutorBrowserWebSession, LoadCourseParameters> getBrowserSessionToPendingCourseNameMap() {
        return browserSessionToPendingCourseName;
    }

    /**
     * Handles a message that the domain session is being started
     *
     * @param msg The received message
     */
    private void handleStartDomainSessionRequestMessage(Message msg) {
        if(logger.isDebugEnabled()){
            logger.debug("handleStartDomainSessionRequestMessage: " + msg);
        }
        try{
            DomainSessionMessage domainSessionMessage = (DomainSessionMessage) msg;
            UserWebSession userSession = getUserSession(domainSessionMessage.getUserSession());
            if (userSession != null) {
                userSession.handleStartDomainSessionRequestMessage(msg);
            } else {
                sendReply(msg, new NACK(ErrorEnum.USER_NOT_FOUND_ERROR, "Not an active user."), MessageTypeEnum.PROCESSED_NACK);
            }
            
        }catch(Exception e){
            logger.error("Caught exception while handling start domain session request message.  Sending NACK message as a reply.", e);
            sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }

    /**
     * Handles a message that the domain session is being initialized
     *
     * @param msg The received message
     */
    private void handleInitializeDomainSessionRequestMessage(Message msg) {

        try{
            DomainSessionMessage domainSessionMessage = (DomainSessionMessage) msg;
            UserWebSession userSession = getUserSession(domainSessionMessage.getUserSession());
            if (userSession != null) {
                
                
                DomainWebState domainSession = userSession.getDomainWebState();
                if (domainSession != null) {
                    InitializeDomainSessionRequest initDomainRequest = (InitializeDomainSessionRequest) msg.getPayload();
                    if(initDomainRequest.getTutorTopicId() != null) {
                        //Save the domain sessions unique topic token 
                        //and create the topic used for any embedded applications
                        //if the domain session needs an embedded queue
                        String topicName = SubjectUtil.TUTOR_TOPIC_PREFIX + ADDRESS_TOKEN_DELIM + initDomainRequest.getTutorTopicId();
                        createSubjectTopicClient(topicName, false);
                        TutorModuleStatus tutorStatus = new TutorModuleStatus(topicName, moduleStatus);
                        sendMessage(SubjectUtil.TUTOR_DISCOVERY_TOPIC, tutorStatus, MessageTypeEnum.TUTOR_MODULE_STATUS, null);
                        
                        //Add a new module status for this domain sessions topic
                        domainIdToTutorModuleStatus.put(domainSessionMessage.getDomainSessionId(), tutorStatus);
                    }
                    userSession.handleInitializeDomainSessionRequestMessage(msg);
                }
                
               
            } else {
                sendReply(msg, new NACK(ErrorEnum.USER_NOT_FOUND_ERROR, "Not an active user."), MessageTypeEnum.PROCESSED_NACK);
            }
            
        }catch(Exception e){
            logger.error("Caught exception while handling initialize domain session request message.  Sending NACK message as a reply.", e);
            sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }

    /**
     * Handles a message that the domain session is being closed
     *
     * @param msg The received message
     */
    private void handleCloseDomainSessionRequestMessage(Message msg) {
        
        try{

            DomainSessionMessage domainSessionMessage = ((DomainSessionMessage) msg);
            UserSession user = domainSessionMessage.getUserSession();
            UserWebSession userSession = getUserSession(user);
            if (userSession != null) {
                int domainSessionId = ((DomainSessionMessage) msg).getDomainSessionId();
                
                //remove the tutor module status from the dictionary so the 
                //heartbeat no longer advertises it
                if(domainIdToTutorModuleStatus.containsKey(domainSessionId)) {
                    TutorModuleStatus removedStatus = domainIdToTutorModuleStatus.remove(domainSessionId);
                    
                    // even though the domain module destroys the tutor topic for the tutor (see BaseDomainSession.cleanup())
                    // that logic doesn't release the message client thread the tutor module created to push
                    // messages to that topic.  Therefore destroy the tutor's connection to that topic here to release the thread.
                    removeClientConnection(removedStatus.getTopicName(), false);
                }
                
                userSession.handleCloseDomainSessionRequestMessage(msg);       
            } else {
                if(logger.isInfoEnabled()){
                    logger.info("Received a close domain session request for a user session that doesn't exist for user = "+user+".  Sending ACK because the domain session is technically closed. "+msg);
                }
                sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);
            }
        
        }catch(Exception e){
            logger.error("Caught exception while handling close domain session request message.  Sending NACK message as a reply.", e);
            sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }
    
    
    /**
     * Accessor to get the TutorModuleStatus for the domain session id.  Null is returned if not found.
     * @param domainSessionId The domain session id to lookup.
     * @return TutorModuleStatus the TutorModuleStatus if found, null is returned if not found.
     */
    public TutorModuleStatus getTutorModuleStatus(int domainSessionId) {
        return domainIdToTutorModuleStatus.get(domainSessionId);
    }
    
    /**
     * Releases the domain session modules for the domain session which occurs when a domain session is closed.
     * 
     * @param domainSession The domain session that is being closed.
     */
    public void cleanupDomainSession(DomainSession domainSession) {
        releaseDomainSessionModules(domainSession);
    }
    
    /**
     * Handles the display team session request message.  
     *
     * @param msg The received message
     */
    private void handleDisplayTeamSessionRequestMessage(final Message msg) {
        
        try{
            UserWebSession userSession = getUserSession(((UserSessionMessage) msg).getUserSession());
    
            if (userSession != null) {
                
                userSession.handleDisplayTeamSessionMessage(msg);
            } else {
    
                sendReply(msg, new NACK(ErrorEnum.USER_NOT_FOUND_ERROR, "Not an active user."), MessageTypeEnum.PROCESSED_NACK);
            }
        
        }catch(Exception e){
            logger.error("Caught exception while handling display team session message.  Sending NACK message as a reply.", e);
            sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }

    /**
     * Handles a message to display some feedback in the TUI
     *
     * @param msg The received message
     */
    private void handleDisplayFeedbackTutorRequestMessage(final Message msg) {
        
        try{
            UserWebSession userSession = getUserSession(((UserSessionMessage) msg).getUserSession());
    
            if (userSession != null) {
                
                userSession.handleDisplayFeedbackTutorRequestMessage(msg);
            } else {
    
                sendReply(msg, new NACK(ErrorEnum.USER_NOT_FOUND_ERROR, "Not an active user."), MessageTypeEnum.PROCESSED_NACK);
            }
        
        }catch(Exception e){
            logger.error("Caught exception while handling display feedback message.  Sending NACK message as a reply.", e);
            sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }

    /**
     * Handles a message to display some text in the TUI
     *
     * @param msg The received message
     */
    private void handleDisplayContentRequestMessage(final Message msg) {
        
        try{
            UserSessionMessage userSessionMessage = (UserSessionMessage) msg;
            UserSession user = userSessionMessage.getUserSession();
            UserWebSession userSession = getUserSession(user);
            if (userSession != null) {
               userSession.handleDisplayContentRequestMessage(msg);
            } else {
                sendReply(msg, new NACK(ErrorEnum.USER_NOT_FOUND_ERROR, "Not an active user."), MessageTypeEnum.PROCESSED_NACK);
            }
            
        }catch(Exception e){
            logger.error("Caught exception while handling display guidance message.  Sending NACK message as a reply.", e);
            sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }

	  
    /**
     * Handles a message to display a survey in the TUI
     *
     * @param msg The received message
     */
    private void handleDisplaySurveyTutorPanelMessage(final Message msg) {
        
        try{

            UserSessionMessage userSessionMessage = (UserSessionMessage) msg;
            UserSession user = userSessionMessage.getUserSession();
            final UserWebSession userSession = getUserSession(user);
            if (userSession != null) {
                userSession.handleDisplaySurveyTutorPanelMessage(msg);
            } else {
                sendReply(msg, new NACK(ErrorEnum.USER_NOT_FOUND_ERROR, "Not an active user."), MessageTypeEnum.PROCESSED_NACK);
            }
        
        }catch(Exception e){
            logger.error("Caught exception while handling display survey message.  Sending NACK message as a reply.", e);
            sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }
    
    /**
     * Handles a message with a survey response created by the learner answering the survey through
     * the training application.
     * 
     * @param msg contains the survey response to apply
     */
    private void handleTrainingApplicationSurveyResponse(final Message msg){
        
        try{

            UserSessionMessage userSessionMessage = (UserSessionMessage) msg;
            UserSession user = userSessionMessage.getUserSession();
            UserWebSession userSession = getUserSession(user);
            if (userSession != null) {
                userSession.handleTrainingApplicationSurveyResponse(msg);
            } else {
                sendReply(msg, new NACK(ErrorEnum.USER_NOT_FOUND_ERROR, "Not an active user."), MessageTypeEnum.PROCESSED_NACK);
            }
            
        }catch(Exception e){
            logger.error("Caught exception while handling training application survey response message.  Sending NACK message as a reply.", e);
            sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
                
    }

    /**
     * Handles a message with a survey submit request created by the learner answering the survey through
     * the training application.
     * 
     * @param msg contains the survey submit request to apply
     */
    private void handleTrainingApplicationSurveySubmit(final Message msg){
        
        try{

            UserSessionMessage userSessionMessage = (UserSessionMessage) msg;
            UserSession user = userSessionMessage.getUserSession();
            UserWebSession userSession = getUserSession(user);
            if (userSession != null) {
                userSession.handleTrainingApplicationSurveySubmit(msg);
            } else {
                sendReply(msg, new NACK(ErrorEnum.USER_NOT_FOUND_ERROR, "Not an active user."), MessageTypeEnum.PROCESSED_NACK);
            }
            
        }catch(Exception e){
            logger.error("Caught exception while handling training application survey submit message.  Sending NACK message as a reply.", e);
            sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }
    
    /**
     * Handles a message with a load progress created by the gateway module loading content into a training application.
     * 
     * @param msg contains the load progress to show to the learner
     */
    private void handleLoadProgress(final Message msg){
        
        try{

            UserSessionMessage userSessionMessage = (UserSessionMessage) msg;
            UserSession user = userSessionMessage.getUserSession();
            UserWebSession userSession = getUserSession(user);
            if (userSession != null) {
                userSession.handleLoadProgress(msg);
            } else {
                sendReply(msg, new NACK(ErrorEnum.USER_NOT_FOUND_ERROR, "Not an active user."), MessageTypeEnum.PROCESSED_NACK);
            }
            
        }catch(Exception e){
            logger.error("Caught exception while handling load progress message.  Sending NACK message as a reply.", e);
            sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }
    
    /**
     * Handles a message to display initialization instructions for a course
     * 
     * @param msg The received message
     */
    private void handleDisplayCourseInitInstructionsRequest(final Message msg){
    	
    	try{   		  		
    	     UserSessionMessage userSessionMessage = (UserSessionMessage) msg;
             UserSession user = userSessionMessage.getUserSession();
             final UserWebSession userSession = getUserSession(user);
             if (userSession != null) {
            	 
            	 userSession.handleDisplayCourseInitInstructionsRequest(msg);
             } else {
                 sendReply(msg, new NACK(ErrorEnum.USER_NOT_FOUND_ERROR, "Not an active user."), MessageTypeEnum.PROCESSED_NACK);
             }
    		
    	} catch(Exception e){
    		logger.error("Caught exception while handling display course init instructions message.  Sending NACK message as a reply.", e);
            sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
    	}
    }

    /**
     * Handles a message to display lesson material in the TUI
     *
     * @param msg The received message
     */
    private void handleDisplayLessonMaterialTutorPanelMessage(final Message msg) {
        
        try{

            UserSessionMessage userSessionMessage = (UserSessionMessage) msg;
            UserSession user = userSessionMessage.getUserSession();
            UserWebSession userSession = getUserSession(user);
            if (userSession != null) {
                userSession.handleDisplayLessonMaterialTutorPanelMessage(msg);
            } else {
                sendReply(msg, new NACK(ErrorEnum.USER_NOT_FOUND_ERROR, "Not an active user."), MessageTypeEnum.PROCESSED_NACK);
            }
            
        }catch(Exception e){
            logger.error("Caught exception while handling display lesson material message.  Sending NACK message as a reply.", e);
            sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }
    
    /**
     * Handles a message to display mid-lesson media in the TUI
     *
     * @param msg The received message
     */
    private void handleDisplayMidLessonMediaTutorPanelMessage(final Message msg) {
        
        try{

            UserSessionMessage userSessionMessage = (UserSessionMessage) msg;
            UserSession user = userSessionMessage.getUserSession();
            UserWebSession userSession = getUserSession(user);
            if (userSession != null) {
                userSession.handleDisplayMidLessonMediaTutorPanelMessage(msg);
            } else {
                sendReply(msg, new NACK(ErrorEnum.USER_NOT_FOUND_ERROR, "Not an active user."), MessageTypeEnum.PROCESSED_NACK);
            }
            
        }catch(Exception e){
            logger.error("Caught exception while handling display mid-lesson media message.  Sending NACK message as a reply.", e);
            sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }

    /**
     * Handles a message to display the AAR in the TUI.
     *
     * @param msg The received message
     */
    private void handleDisplayAarTutorPanelMessage(final Message msg) {
        
        try{

            UserWebSession userSession = getUserSession((UserSessionMessage) msg);
            if (userSession != null) {
                userSession.handleDisplayAarTutorPanelMessage(msg);
            } else {
                sendReply(msg, new NACK(ErrorEnum.USER_NOT_FOUND_ERROR, "Not an active user."), MessageTypeEnum.PROCESSED_NACK);
            }
            
        }catch(Exception e){
            logger.error("Caught exception while handling display AAR message.  Sending NACK message as a reply.", e);
            sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }

    /**
     * Handles a message to indicate the lesson has started.
     * 
     * @param msg The received message
     */
    private void handleLessonStartedMessage(final Message msg) {
        
        try{
            UserWebSession userSession = getUserSession((UserSessionMessage) msg);
            if (userSession != null) {
                userSession.handleLessonStartedMessage(msg);
            } else {
                sendReply(msg, new NACK(ErrorEnum.USER_NOT_FOUND_ERROR, "Not an active user."), MessageTypeEnum.PROCESSED_NACK);
            }
            
        }catch(Exception e){
            logger.error("Caught exception while handling lesson started message.  Sending NACK message as a reply.", e);
            sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }
    
    /**
     * Handles a message to indicate the lesson has finished.
     * 
     * @param msg The received message
     */
    private void handleLessonCompletedMessage(final Message msg) {
        
        try{
            UserWebSession userSession = getUserSession((UserSessionMessage) msg);
            if (userSession != null) {
               userSession.handleLessonCompletedMessage(msg);
            } else {
                sendReply(msg, new NACK(ErrorEnum.USER_NOT_FOUND_ERROR, "Not an active user."), MessageTypeEnum.PROCESSED_NACK);
            }
            
        }catch(Exception e){
            logger.error("Caught exception while handling lesson completed message.  Sending NACK message as a reply.", e);
            sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }
    
    private void handleDisplayLearnerActionsMessage(final Message msg) {
        
        UserWebSession userSession = getUserSession((UserSessionMessage) msg);
        if (userSession != null) {
            userSession.handleDisplayLearnerActionsMessage(msg);
            
        } else {
            sendReply(msg, new NACK(ErrorEnum.USER_NOT_FOUND_ERROR, "Not an active user."), MessageTypeEnum.PROCESSED_NACK);
        }
    }
    
    private void handleCourseStateMessage(final Message msg){
        
        UserWebSession userSession = getUserSession((UserSessionMessage) msg);
        if (userSession != null) {
            userSession.handleCourseStateMessage(msg);
            sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);  //need to send this in case domain module is waiting for response
                                                                       //before continuing course
        } else {
            sendReply(msg, new NACK(ErrorEnum.USER_NOT_FOUND_ERROR, "Not an active user."), MessageTypeEnum.PROCESSED_NACK);
        }
    }
    
    /**
     * Handle the request for active user sessions by gathering the information and sending a reply.
     * 
     * @param msg
     */
    private void handleActiveUserSessionsRequest(final Message msg){
        
        //gather all user sessions in this module
        List<mil.arl.gift.common.UserSession> usList = new ArrayList<mil.arl.gift.common.UserSession>();
        synchronized(userSessionIdToUserSessionMap){
            for(UserWebSession userSession : userSessionIdToUserSessionMap.values()){
                
                mil.arl.gift.common.UserSession commonUserSession = new mil.arl.gift.common.UserSession(userSession.getUserId());
                commonUserSession.setUsername(userSession.getUserSessionInfo().getUsername());
                usList.add(commonUserSession);
            }
        }
        
        sendReply(msg, new UserSessionList(usList), MessageTypeEnum.ACTIVE_USER_SESSIONS_REPLY);
    }

    /**
     * Handle the display chat window request message by displaying the chat window interface on the TUI.
     * 
     * @param msg the display chat window request message to handle
     */
    private void handleDisplayChatWindowRequestMessage(final Message msg) {
        
        try{

            UserWebSession userSession = getUserSession((UserSessionMessage) msg);
    
            if (userSession != null) {
    
                userSession.handleDisplayChatWindowRequestMessage(msg);
    
            } else {
                sendReply(msg, new NACK(ErrorEnum.USER_NOT_FOUND_ERROR, "Not an active user."), MessageTypeEnum.PROCESSED_NACK);
            }
        
        }catch(Exception e){
            logger.error("Caught exception while handling display chat window request message.  Sending NACK message as a reply.", e);
            sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }
    
    /**
     * Handle an update to the current chat window being displayed on the TUI.  The update most likely involves
     * a new chat entry from the tutor.
     * 
     * @param msg the chat window update message to handle
     */
    private void handleDisplayChatWindowUpdateRequestMessage(final Message msg) {
        
        try{

            UserWebSession userSession = getUserSession((UserSessionMessage) msg);
    
            if (userSession != null) {
    
               userSession.handleDisplayChatWindowUpdateRequestMessage(msg);
    
            } else {    
                sendReply(msg, new NACK(ErrorEnum.USER_NOT_FOUND_ERROR, "Not an active user."), MessageTypeEnum.PROCESSED_NACK);
            }
            
        }catch(Exception e){
            logger.error("Sending NACK message as a reply to a chat window update request message because an exception was caught.\n"+msg, e);
            NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "There was a problem while attempting to apply the chat window update request.  The error message is "+e.getMessage());
            nack.setErrorHelp("If you have access to the Tutor module server log, it will contain additional details on the error.  Otherwise contact the GIFT administrator for additional help.");
            sendReply(msg, nack, MessageTypeEnum.PROCESSED_NACK);
        }
    }
    
    /**
     * Called when the TutorModule receives a message with the MessageType INIT_EMBEDDED_CONNECTIONS.
     * It informs the correct client side BrowserSession that it will need to display an embedded 
     * training application as well as telling it where to navigate its iframe to view the 
     * embedded application.
     * @param msg The msg that the TutorModule received. Payload should be of 
     * type InitializeEmbeddedConnections
     */
    private void handleInitEmbeddedConnections(final Message msg) {
        try {
            
            UserWebSession userSession = getUserSession((UserSessionMessage) msg);
            if(userSession != null) {      
                
                userSession.handleInitEmbeddedConnections(msg);

            } else {
                sendReply(msg, new NACK(ErrorEnum.USER_NOT_FOUND_ERROR, "Not an active user."), MessageTypeEnum.PROCESSED_NACK);
            }
            
        } catch(Exception e) {
            logger.error("There was an exception while the TutorModule was handling the InitializeEmbeddedConnections from the DomainModule", e);
            NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "There was an exception while the TutorModule was handling the InitializeEmbeddedConnections from the DomainModule");
            nack.setErrorHelp("If you have access to the Tutor module server log, it will contain additional details on the error.  Otherwise contact the GIFT administrator for additional help.");
            sendReply(msg, nack, MessageTypeEnum.PROCESSED_NACK);
        }
    }
    
    /**
     * Called when the TutorModule receives a message with the MessageType SIMAN
     * The message is used to signal a change of execution state in the current 
     * embedded training application. The message is relayed to the embedded 
     * application on the client side so that the training app is able to perform 
     * the correct action.
     * @param msg The msg that the TutorModule received. Payload should be of 
     * type Siman
     */
    private void handleSimanMessage(final Message msg) {
        //Saves this message as a message to be replied to
        DomainSessionMessage domainSessionMessage = (DomainSessionMessage) msg;
        UserWebSession userSession = getUserSession((UserSessionMessage) msg);
        if(userSession != null) {

            userSession.handleSimanMessage(msg);
            
        } else {
            logger.error("Unable to find a user with the id " + domainSessionMessage.getUserId());
            sendReply(msg, new NACK(ErrorEnum.USER_NOT_FOUND_ERROR, "Not an active user."), MessageTypeEnum.PROCESSED_NACK);
        }

    }
    
    /**
     * Handles a request, from the domain module, to display feedback within the embedded 
     * training application. Msg is expected to have a payload of type string.
     * @param msg The message from the domain module.
     */
    private void handleTrainingAppFeedbackRequest(final Message msg) {
        //Converts the GIFT Message to JSON to be consumed by the embedded application
        DomainSessionMessage domainSessionMessage = (DomainSessionMessage) msg;
        UserWebSession userSession = getUserSession((UserSessionMessage) msg);
        if(userSession != null) {        
            userSession.handleTrainingAppFeedbackRequest(msg);
            
            
        } else {
            logger.error("Unable to find a user with the id " + domainSessionMessage.getUserId());
            sendReply(msg, new NACK(ErrorEnum.USER_NOT_FOUND_ERROR, "Not an active user."), MessageTypeEnum.PROCESSED_NACK);
        }
        
        sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);
    }

    /**
     * Retrieves and handles a queued chat update request to the active chat window displayed on the client.
     * 
     * @return True if there was an update to handle, false otherwise.
     */
    public boolean dequeueChatUpdate(UserWebSession userSession) {
        
    	if (userSession != null && userSession.getDomainWebState() != null) {
    		DisplayChatWindowUpdateRequest chatRequest = UpdateQueueManager.getInstance().dequeueChatUpdate(userSession.getUserId());
    		if(chatRequest != null) {
    			userSession.getDomainWebState().displayArticleChatWindow(chatRequest);
    			return true;
    		} else {
    			userSession.getDomainWebState().checkForChatUpdates();
    		}
    	}
    	
    	return false;
    }
    
    /**
     * Sends an app state from the specified BrowserSession to the domain module.
     * Messages will be sent through the tutor topic unless the message is an ACK
     * to a previously received message in which case it will be sent via the sendReply
     * method of the AbstractModule class. 
     * @param message The JSON string to decode into a GIFT Message and send to the
     * @param browserSessionKey The identifier for the browser session that is sending 
     * the message.
     */
    public void sendMessageFromEmbeddedApplication(String message, String browserSessionKey) {

        //Get the tutor topic name using the browserSessionKey
        TutorBrowserWebSession browserSession = getBrowserSession(browserSessionKey);
        if(browserSession != null) {
            String userSessionKey = browserSession.getUserSessionKey();
            UserWebSession userSession = getUserSession(userSessionKey);
            if(userSession != null) {
                userSession.sendMessageFromEmbeddedApplication(message);
            } else {
                logger.error(userSessionKey + " is not an active user.");
            }
        } else {
            logger.error("There was no browser session for the specified ID of " + browserSessionKey);
        }

    }
    
    /**
     * Builds the encrypted OAuth URL that will be used to send the request to the LTI provider.
     * 
     * @param rawUrl the raw media url before it has been protected by OAuth.
     * @param mediaTypeProperties The MediaTypeProperties associated with the content.
     * @param browserSessionKey the browser session key.
     * @param callback the callback used to handle the response or catch any failures. This will
     *            return a failure if the browser session or user session aren't found.
     */
    public void buildOAuthLtiUrl(String rawUrl, LtiProperties properties, String browserSessionKey, final MessageCollectionCallback callback) {

        // Get the tutor topic name using the browserSessionKey
        TutorBrowserWebSession browserSession = getBrowserSession(browserSessionKey);
        if (browserSession != null) {
            String userSessionKey = browserSession.getUserSessionKey();
            UserWebSession userSession = getUserSession(userSessionKey);
            if (userSession != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Sending build OAuth message from the tutor module to the domain module. Domain Session id: "+userSession.getDomainWebState().getDomainSessionId());
                }
                try {
                    int domainSessionId = userSession.getDomainWebState().getDomainSessionId();
                    LtiGetProviderUrlRequest request = new LtiGetProviderUrlRequest(properties.getLtiIdentifier(), properties.getCustomParameters(),
                            rawUrl, domainSessionId);
                    sendDomainSessionMessage(request, userSession.getUserSessionInfo(), domainSessionId, MessageTypeEnum.LTI_GET_PROVIDER_URL_REQUEST, callback);
                } catch (Exception e) {
                    String errorMsg = "Error sending the request to retrieve the LTI provider URL.";
                    logger.error(errorMsg, e);
                    callback.failure(errorMsg);
                }
            } else {
                String errorMsg = userSessionKey + " is not an active user.";
                logger.error(errorMsg);
                callback.failure(errorMsg);
            }
        } else {
            String errorMsg = "There was no active browser session for the specified key";
            logger.error(errorMsg);
            callback.failure(errorMsg);
        }
    }
    
    /**
     * Wrapper function to send a domain session message for an embedded application message.
     * 
     * @param tutorTopic The topic to reply to.
     * @param payload The payload to be sent.
     * @param userSession The user session sending the message.
     * @param domainSessionId The domain session sending the message.
     * @param experimentId The experiment id (if applicable) that the message belongs to.
     * @param messageType The type of message.
     */
    public void sendEmbeddedApplicationMessageToDomainSession(String tutorTopic, Object payload, UserSession userSession, 
            int domainSessionId, 
            String experimentId, 
            MessageTypeEnum messageType) {
        
        // Sends the domain session message via ActiveMQ.
        sendDomainSessionMessage(
                tutorTopic,
                payload, 
                userSession, 
                domainSessionId, 
                experimentId, 
                messageType, 
                null);
    }
    
    /**
     * Inner class used to manage course load parameters.
     * 
     * @author mhoffman
     *
     */
    public class LoadCourseParameters{
        
        /** the selected domain runtime id which is the path to the course used when taking the course */
        private String courseRuntimeId;
        
        /** the selected domain source id which is the path to the authored course */
        private String courseSourceId;
        
        /**
         * Needed for GWT serialization
         */
        public LoadCourseParameters(){ }
        
        /**
         * Set attributes
         * 
         * @param courseRuntimeId the selected domain runtime id which is the path to the course used when taking the course.
         * @param courseSourceId the selected domain source id which is the path to the authored course 
         */
        public LoadCourseParameters(String courseRuntimeId, String courseSourceId){
            this.courseRuntimeId = courseRuntimeId;
            this.courseSourceId = courseSourceId;
        }

        public String getCourseRuntimeId() {
            return courseRuntimeId;
        }

        public String getCourseSourceId() {
            return courseSourceId;
        }        
        
        @Override
        public String toString(){
            
            StringBuilder sb = new StringBuilder();
            sb.append("[LoadCourseParameters: ");
            sb.append("courseRuntimeId = ").append(getCourseRuntimeId());
            sb.append(", courseSourceId = ").append(getCourseSourceId());
            sb.append("]");
            return sb.toString();
        }
    }

    
}
