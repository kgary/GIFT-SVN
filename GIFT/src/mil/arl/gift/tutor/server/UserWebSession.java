/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.course.LessonMaterialList;
import mil.arl.gift.common.AbstractDisplayContentTutorRequest;
import mil.arl.gift.common.AbstractRuntimeParameters;
import mil.arl.gift.common.AvatarData;
import mil.arl.gift.common.ChatLog;
import mil.arl.gift.common.DisplayAfterActionReviewTutorRequest;
import mil.arl.gift.common.DisplayAvatarAction;
import mil.arl.gift.common.DisplayChatWindowRequest;
import mil.arl.gift.common.DisplayChatWindowUpdateRequest;
import mil.arl.gift.common.DisplayCourseInitInstructionsRequest;
import mil.arl.gift.common.DisplayLearnerActionsTutorRequest;
import mil.arl.gift.common.DisplayMediaCollectionRequest;
import mil.arl.gift.common.DisplayMediaTutorRequest;
import mil.arl.gift.common.DisplayMidLessonMediaRequest;
import mil.arl.gift.common.DisplayScriptedAvatarAction;
import mil.arl.gift.common.DisplaySurveyTutorRequest;
import mil.arl.gift.common.DisplayTextToSpeechAvatarAction;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.InitializeEmbeddedConnections;
import mil.arl.gift.common.InteropConnectionsInfo;
import mil.arl.gift.common.Siman;
import mil.arl.gift.common.TutorUserInterfaceFeedback;
import mil.arl.gift.common.UserData;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.UserSessionType;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.ErrorEnum;
import mil.arl.gift.common.enums.MessageFeedbackDisplayModeEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.SimanTypeEnum;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.server.AbstractWebSession;
import mil.arl.gift.common.gwt.server.AsyncResponseCallback;
import mil.arl.gift.common.gwt.server.AsyncReturnBlocker;
import mil.arl.gift.common.gwt.server.GiftServletUtils;
import mil.arl.gift.common.gwt.shared.MediaHtml;
import mil.arl.gift.common.io.UriUtil;
import mil.arl.gift.common.lti.LtiUserId;
import mil.arl.gift.common.survey.AbstractQuestionResponse;
import mil.arl.gift.common.survey.SurveyResponse;
import mil.arl.gift.common.ta.state.GenericJSONState;
import mil.arl.gift.common.usersession.LtiUserSessionDetails;
import mil.arl.gift.net.api.MessageCollectionCallback;
import mil.arl.gift.net.api.message.ACK;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.NACK;
import mil.arl.gift.net.api.message.UserSessionMessage;
import mil.arl.gift.net.embedded.message.codec.EmbeddedAppMessageEncoder;
import mil.arl.gift.tutor.server.DomainWebState.CharacterIdleListener;
import mil.arl.gift.tutor.server.TutorModule.LoadCourseParameters;
import mil.arl.gift.tutor.server.messagehandler.EmbeddedAppMessageHandler;
import mil.arl.gift.tutor.shared.AbstractAction;
import mil.arl.gift.tutor.shared.ActionTypeEnum;
import mil.arl.gift.tutor.shared.ClientProperties;
import mil.arl.gift.tutor.shared.DialogTypeEnum;
import mil.arl.gift.tutor.shared.InitTrainingAppAction;
import mil.arl.gift.tutor.shared.KnowledgeSessionsUpdated;
import mil.arl.gift.tutor.shared.PreloadAvatarAction;
import mil.arl.gift.tutor.shared.SubmitAction;
import mil.arl.gift.tutor.shared.TrainingAppMessageAction;
import mil.arl.gift.tutor.shared.WidgetTypeEnum;
import mil.arl.gift.tutor.shared.properties.DisplayMediaCollectionWidgetProperties;
import mil.arl.gift.tutor.shared.properties.SurveyWidgetProperties;
import mil.arl.gift.tutor.shared.properties.WidgetProperties;

/**
 * A UserWebSession represents the user's web presence in the tutor.
 * A user web session can have multiple browser sessions (eg, a user
 * can have multiple browsers opened at the same time.
 *
 * Additionally the user web session manages the domain web state (the state
 * of the domain session as it should be presented to the user's browser web clients).
 *
 * So a UserWebSession has a 1 to many relationship to BrowserWebSessions.  It has a 1 to 1 relationship
 * to the DomainWebState.
 *
 * This class is responsible primarily for handling messages from the TutorModule for a specific
 * user and updating the domain web state and updating the browsers associated with the user.
 * Conversely, it is responsible for handling messages received by a browser client and passing
 * those back to the TutorModule (and/or other browser clients that are associated for the user).
 *
 *
 * @author jleonard
 */
public class UserWebSession extends AbstractWebSession {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(UserWebSession.class);

    private String sessionKey = UUID.randomUUID().toString();

    /** information about the user session this web session is associated with */
    private final UserSession userSessionInfo;

    private String lmsUsername;

    /**
     * A user web session can have multiple browser sessions associated with it.
     * This means that the user can have multiple browsers, but each browser session is associated
     * with only 1 user.  This is a 1 to many relationship.
     */
    private final Set<TutorBrowserWebSession> browserSessions = new HashSet<>();

    /** The domain web state represents the state of the domain session as it is presented to the
     * web client(s).  For now, a user can only have one domain web state, but can have multiple browsers.
     */
    private DomainWebState domainWebState = null;

    /**
     * flag indicating whether the user session is currently requesting that a domain session be
     * created by/with the domain module.  Since this process takes some cycles and the dashboard could notify
     * the tutor to end the session and logout while that is happening, this allows for external checking
     * of what the user session is doing thereby preventing events that would normally result in errors.
     */
    private Boolean creatingDomainSession = false;

    /** the rpc response to use when the domain session ends */
    public static final String DOMAIN_SESSION_ENDED = "the domain session has ended";

    /**
     * A scheduler used to schedule a session cleanup task when the socket is terminated or fails to initially connect.
     * This schedule will be shutdown if the scheduled task is executed.
     */
    private ScheduledExecutorService endSessionScheduler = Executors.newScheduledThreadPool(1);

    private ScheduledFuture<?> endSessionTaskHandle = null;

    private static final long DEFAULT_SIMAN_STOP_TIMEOUT = 5000;

    /**
     * The amount of time to wait for the client to establish its initial web socket handshake before
     * ending the session due to connection failure
     */
    private static final int CLIENT_HANDSHAKE_TIMEOUT = 60000;

    /** The queue for messages which the session has currently not yet replied to */
    private Queue<Message> messagesPendingReply = new LinkedList<>();

    /** A flag that indicates whether or not the embedded application has sent a StopFreeze message.
     * If the StopFreeze has been sent, no other messages, other than ACKs, should be sent to the DomainModule. */
    private boolean embeddedApplicationSentStopFreeze = false;

    /** A flag that indicates whether or not the embedded application has received a Siman Stop message.
     * If the SimanStop has been sent, no other messages, other than ACKs, should be sent to the DomainModule
     * and no other SimanStop messages should be sent to the embedded applicaiton. */
    private boolean embeddedApplicationReceivedSimanStop = false;

    /** the path to the default character for the TUI to show */
    private static final String DEFAULT_CHARACTER_PATH = TutorModuleProperties.getInstance().getDefaultCharacterPath();

    /** The handler that processes the inoming embedded application messages. */
    private EmbeddedAppMessageHandler handler = new EmbeddedAppMessageHandler();

    /**
     * Creates a new user web session
     *
     * @param userSession - information about the user session (including the unique user id of the learner)
     *                      this web session is associated with
     */
    public UserWebSession(UserSession userSession) {
        userSessionInfo = userSession;

        if(userSessionInfo.getUsername() != null){
            setLmsUsername(userSessionInfo.getUsername());
        }

        // if the client doesn't perform its initial web socket handshake within a certain time, end the session
        // to prevent it from lingering
        startEndSessionTimer(CLIENT_HANDSHAKE_TIMEOUT);
    }

    /**
     * Gets the key of this user session;
     *
     * @return String The key of the user session
     */
    public String getUserSessionKey() {
        return sessionKey;
    }

    /**
     * Gets the user's ID
     *
     * @return int The user's ID
     */
    public int getUserId() {
        return userSessionInfo.getUserId();
    }

    /**
     * Return the user session information for this web session.
     *
     * @return UserSession
     */
    public UserSession getUserSessionInfo(){
        return userSessionInfo;
    }

    /**
     * Set the username for the user of this web session.
     *
     * @param username - the GIFT username for the user of this web session
     */
    public void setUsername(String username){
        userSessionInfo.setUsername(username);
    }

    /**
     * Gets the user's LMS username
     *
     * @return String The user's LMS username
     */
    public String getLmsUsername() {
        return lmsUsername;
    }

    /**
     * Sets the user's LMS username
     *
     * @param lmsUsername String The user's LMS username
     */
    private void setLmsUsername(String lmsUsername) {
        this.lmsUsername = lmsUsername;
    }

    /**
     * Gets the domain web state associated with this user session
     *
     * @return DomainWebState The domain web state associated with this user
     * session.  Can be null.
     */
    public DomainWebState getDomainWebState() {
        return domainWebState;
    }

    /**
     * Return whether this user session is currently requesting that a domain session be
     * created by/with the domain module.  Since this process takes some cycles and the dashboard could notify
     * the tutor to end the session and logout while that is happening, this allows for external checking
     * of what the user session is doing thereby preventing events that would normally result in errors.
     *
     * @return
     */
    public boolean isCreatingDomainSession(){
        return creatingDomainSession;
    }

    /**
     * Sets the domain web state associated with this user session
     *
     * @param webState A domain web state associated with this user session
     */
    public void setDomainWebState(final DomainWebState webState) {

        this.domainWebState = webState;
    }

    /**
     * Getter for the message that is pending reply. Used
     * to hold a message that the embedded application needs to
     * acknowledge such as a Siman Stop message
     * @return
     */
    public Message dequeueMessagePendingReply() {
        return messagesPendingReply.poll();
    }

    /**
     * Setter for the message that is pending reply. Used
     * to hold a message that the embedded application needs to
     * acknowledge such as a Siman Stop message
     * @param newMessage the new message to store
     */
    public void enqueueMessagePendingReply(Message newMessage) {
        messagesPendingReply.add(newMessage);
    }

    /**
     * Getter for the flag indicating whether or not the
     * embedded application has sent a StopFreeze message.
     * Used to prevent more messages from being sent to the DomainModule.
     * Flag should be set to true when the embedded application sends a
     * StopFreeze message.
     * @return
     */
    public boolean getEmbeddedApplicationSentStopFreeze() {
        return embeddedApplicationSentStopFreeze;
    }

    /**
     * Setter for the flag indicating whether or not the
     * embedded application has sent a StopFreeze message.
     * Used to prevent more messages from being sent to the DomainModule.
     * Flag should be set to true when the embedded application sends a
     * StopFreeze message.
     * @param newValue
     */
    public void setEmbeddedApplicationSentStopFreeze(boolean newValue) {
        embeddedApplicationSentStopFreeze = newValue;
    }

    /**
     * Getter for the flag indicating whether or not the
     * embedded application has received a Siman Stop message.
     * Used to prevent more messages from being sent to the DomainModule
     * and to prevent the embedded application from receiving multiple Siman
     * Stop messages. Flag should be set to true when the embedded application
     * receives a Siman Stop message.
     * @return
     */
    public boolean getEmbeddedApplicationReceivedSimanStop() {
        return embeddedApplicationReceivedSimanStop;
    }

    /**
     * Setter for the flag indicating whether or not the
     * embedded application has received a Siman Stop message.
     * Used to prevent more messages from being sent to the DomainModule
     * and to prevent the embedded application from receiving multiple Siman
     * Stop messages. Flag should be set to true when the embedded application
     * receives a Siman Stop message.
     */
    public void setEmbeddedApplicationReceivedSimanStop(boolean newValue) {
        embeddedApplicationReceivedSimanStop = newValue;
    }

    /**
     * Logout the user
     *
     * @param session The browser session that initiated the logout
     */
    public RpcResponse logoutUser(final TutorBrowserWebSession session) {
        logger.debug("logoutUser() start");

        if (session == null) {
            RpcResponse response = new RpcResponse(null, null, false, "Logout user failed because the session is null.");
            return response;
        }

        final AsyncReturnBlocker<RpcResponse> returnBlocker = new AsyncReturnBlocker<>();
        TutorModule.getInstance().sendLogoutMessage(getUserSessionInfo(), new MessageCollectionCallback() {

            @Override
            public void success() {

                synchronized(browserSessions) {
                    browserSessions.remove(session);
                }

                //don't show the logout success dialog if the tutor is embedded (not embedded in simple deployment mode)
                if(TutorModuleProperties.getInstance().getDeploymentMode() == DeploymentModeEnum.SIMPLE){
                    session.displayDialog(DialogTypeEnum.INFO_DIALOG, "Logout Success", "You have successfully logged out");
                }

                session.stopSession();
                stopSession();
                session.endSession();
                endSession();

                logger.debug("logoutUser() logoutMessage returned success");

                RpcResponse response = new RpcResponse(session.getUserSessionKey(), session.getBrowserSessionKey(), true, "User has been logged out.");
                returnBlocker.setReturnValue(response);
            }

            @Override
            public void received(Message msg) {
                // Do nothing
            }

            @Override
            public void failure(Message msg) {
                StringBuilder response = new StringBuilder();
                response.append("NACK received from ");
                response.append(msg.getSenderAddress());
                response.append(": ");
                response.append(((NACK) msg.getPayload()).getErrorMessage());

                session.displayDialog(DialogTypeEnum.ERROR_DIALOG, "Logout Error", response.toString());
                session.stopSession();
                stopSession();
                session.endSession();
                endSession();

                logger.debug("logoutUser() logoutMessage returned failure w/msg");

                RpcResponse rpcResponse = new RpcResponse(session.getUserSessionKey(), session.getBrowserSessionKey(), false, "");
                rpcResponse.setResponse("A failure occurred due to a failed message while logging out the user session.");
                rpcResponse.setAdditionalInformation(response.toString());
                returnBlocker.setReturnValue(rpcResponse);
            }

            @Override
            public void failure(String why) {
                StringBuilder response = new StringBuilder();
                response.append("Failure: ");
                response.append(why);

                session.displayDialog(DialogTypeEnum.ERROR_DIALOG, "Logout Error", response.toString());
                session.stopSession();
                stopSession();
                session.endSession();
                endSession();

                logger.debug("logoutUser() logoutMessage returned failure w/why");
                RpcResponse rpcResponse = new RpcResponse(session.getUserSessionKey(), session.getBrowserSessionKey(), false, "");
                rpcResponse.setResponse("A failure occurred while logging out the user session.");
                rpcResponse.setAdditionalInformation(response.toString());
                returnBlocker.setReturnValue(rpcResponse);
            }
        });

        RpcResponse rpcResponse = returnBlocker.getReturnValueOrTimeout();

        logger.debug("logoutUser() end");

        return rpcResponse;
    }

    /**
     * Selects a domain for the user
     *
     * @param domainRuntimeId the selected domain runtime id which is the path to the course used when taking the course
     * @param domainSourceId the selected domain source id which is the path to the authored course
     * @param browserSession The browser session that initiated the select
     * domain
     * @param runtimeParams (Optional) Additional parameters that can be used to configure the domain session.
     * @param callback used to notify the result of creating a domain session with the selected domain
     */
    public void userSelectDomain(final String domainRuntimeId, final String domainSourceId,
            final TutorBrowserWebSession browserSession, final AbstractRuntimeParameters runtimeParams, final AsyncResponseCallback callback) {

        if(logger.isDebugEnabled()){
            logger.debug("userSelectDomain called: " + domainRuntimeId + " " + browserSession + ", runtimeParams=" + runtimeParams);
        }
        creatingDomainSession = true;

        // The domain selection request requires that an lms username is filled out. Certain login types may not have an
        // actual username that a user picks or signs in from (such as experiments or lti).  In the case of the lti user,
        // a generated username is made that is unique to the user from the tool consumer.
        String lmsUser = lmsUsername;
        if (lmsUser == null && userSessionInfo.isSessionType(UserSessionType.LTI_USER)) {

            LtiUserSessionDetails sessionDetails = (LtiUserSessionDetails)userSessionInfo.getSessionDetails();
            if (sessionDetails != null && sessionDetails.getLtiUserId() != null) {
                lmsUser = LtiUserId.getCompositeConsumerName(sessionDetails.getLtiUserId());
            }
        }

        TutorModule.getInstance().selectDomain(userSessionInfo, lmsUser, domainRuntimeId, domainSourceId, browserSession, runtimeParams, new MessageCollectionCallback() {

            private UserSessionMessage reply;

            @Override
            public void success() {

                if (reply != null) {

                    mil.arl.gift.common.DomainSession domainSession = (mil.arl.gift.common.DomainSession) reply.getPayload();

                    //is the user session still active?
                    if(TutorModule.getInstance().getUserSession(domainSession) != null){

                        final DomainWebState webState =
                                TutorModule.getInstance().createDomainWebState(domainSession, domainSession.getDomainSessionId(), domainRuntimeId, domainSourceId, domainSession.getExperimentId());
                        finishedCreatingDomainSession();
                        if (webState != null) {
                            // Notify the browser sessions that a new domain web state was created.
                            onDomainWebStateCreated();
                            TutorModule.getInstance().sendReply(reply, new ACK(), MessageTypeEnum.PROCESSED_ACK);
                            callback.notify(true, "successfully created domain session.", null);
                        } else {

                            logger.error("Could not create a non-Experiment domain session for "+domainSession+".");

                            TutorModule.getInstance().sendReply(reply, new NACK(ErrorEnum.OPERATION_FAILED, "Could not create a domain session"), MessageTypeEnum.PROCESSED_NACK);
                            browserSession.displayDialog(DialogTypeEnum.ERROR_DIALOG, "Domain Selection Failure", "The server could not select a domain: Could not create a domain session.");
                            callback.notify(false, "could not create domain session.", null);
                        }
                    }else{
                        if(logger.isInfoEnabled()){
                            logger.info("Quietly ending the select domain sequence since the user session is no longer active for "+domainSession);
                        }
                    }
                } else {
                    finishedCreatingDomainSession();
                    logger.error("Domain selection reply was not received.");

                    TutorModule.getInstance().sendReply(reply, new NACK(ErrorEnum.UNHANDLED_MESSAGE_ERROR, "A domain selection reply type message was not received"), MessageTypeEnum.PROCESSED_NACK);
                    browserSession.displayDialog(DialogTypeEnum.ERROR_DIALOG, "Domain Selection Failure", "The server could not select a domain: Domain selection reply message not received.");
                    callback.notify(false, "domain selection reply was not received.", null);
                }
            }

            @Override
            public void received(Message msg) {
                if (msg.getMessageType() == MessageTypeEnum.DOMAIN_SELECTION_REPLY) {
                    reply = (UserSessionMessage) msg;
                }
            }

            @Override
            public void failure(Message msg) {
                finishedCreatingDomainSession();
                if(domainWebState == null){
                    //domain session doesn't exist or has ended while waiting for a response
                    logger.debug("domain session doesn't exist, sending callback response of '"+DOMAIN_SESSION_ENDED+"' to client.");

                    String message = DOMAIN_SESSION_ENDED;
                    String advancedDesc = null;
                    if(msg != null && msg.getPayload() instanceof NACK){
                        NACK nack = (NACK) msg.getPayload();
                        message += " because "+nack.getErrorMessage();

                        if(nack.getErrorHelp() != null && !nack.getErrorHelp().isEmpty()){
                            advancedDesc = nack.getErrorHelp();
                        }
                    }
                    callback.notify(false, message, advancedDesc);

                }else{
                    String errorMsg = ((NACK) msg.getPayload()).getErrorMessage();
                    String help = ((NACK) msg.getPayload()).getErrorHelp();

                    StringBuilder response = new StringBuilder();
                    response.append(errorMsg);

                    if(logger.isDebugEnabled()){
                        logger.debug("domain session did exist, sending callback response of '"+response.toString()+"' to client.");
                    }
                    callback.notify(false, response.toString(), help);
                }
            }

            @Override
            public void failure(String why) {
                finishedCreatingDomainSession();
                if(domainWebState == null){
                    //domain session doesn't exist or has ended while waiting for a response
                    callback.notify(true, DOMAIN_SESSION_ENDED, null);

                }else{
                    StringBuilder response = new StringBuilder();
                    response.append("Failure: ");
                    response.append(why);

                    callback.notify(false, response.toString(), null);
                }
            }

            private void finishedCreatingDomainSession(){
                creatingDomainSession = false;
            }
        });
    }

    @Override
    protected void onSessionStopping() {
        if(logger.isInfoEnabled()){
            logger.info("Stopping user session for user ID " + getUserId());
        }

        // This stops any other browser session that may be attached to the user session.
        // This code can get hit when logging into the same user session from a different browser
        // and trying to start a course when one is already started in the other browser.
        synchronized(browserSessions) {
            for (final TutorBrowserWebSession i : browserSessions) {
             // Only show the dialog in simple mode.
                if(TutorModuleProperties.getInstance().getDeploymentMode() == DeploymentModeEnum.SIMPLE){
                    i.displayDialog(DialogTypeEnum.INFO_DIALOG, "Session Ended", "This user session has been ended, login again to resume.");
                }
                i.stopSession();
            }
        }

    }

    /**
     * Removes all browser sessions from the list.  This should only be called
     * after the browser sessions have been stopped/ended properly.
     */
    private void removeAllBrowserSessions() {
        synchronized(browserSessions) {
            browserSessions.clear();
        }
    }

    @Override
    protected void onSessionEnding() {
        if(logger.isInfoEnabled()){
            logger.info("Ending user session for user ID " + getUserId());
        }

        synchronized(browserSessions) {
            for (final TutorBrowserWebSession i : browserSessions) {
               i.endSession();
            }
        }

        removeAllBrowserSessions();
    }

    @Override
    protected void onSessionEnded() {

        //close the schedule thereby ending any threads it has started
        try{
            endSessionScheduler.shutdown();
        }catch(Exception e){
            logger.error("Failed to shutdown the end session scheduler, therefore the scheduler thread will remain running for the ended session of "+this, e);
        }
    }

    /**
     * Adds a browser session that is part of this user session
     *
     * @param session The browser session that is part of this user session
     */
    public void addBrowserSession(TutorBrowserWebSession session) {
        synchronized (browserSessions) {
            browserSessions.add(session);
        }
    }

    /**
     * Removes a browser session that is part of this user session
     *
     * @param session The browser session that is part of this user session
     */
    public void removeBrowserSession(TutorBrowserWebSession session) {
        synchronized (browserSessions) {
            browserSessions.remove(session);
        }
    }

    /**
     * Retrieves the browser session key for the first browser web session in the set
     * Typically, there is only one browser web session stored in the set
     * @return String - The browser session key or null if there are no browser sessions
     */
    public String getBrowserSessionKey() {

        synchronized(browserSessions) {
            for(TutorBrowserWebSession b : browserSessions) {
                return b.getBrowserSessionKey();
            }
        }

        return null;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[UserSession: ");
        sb.append("ID: ").append(getUserId());
        sb.append(", domainWebState = ").append(getDomainWebState());
        sb.append(", LMS Username = ").append(getLmsUsername());
        sb.append("]");

        return sb.toString();
    }

    /**
     * Creates a new user and a user session for that user.
     *
     * @param isMale Is the user to be created male
     * @param lmsUsername The LMS username of the user to be created
     * @param callback Callback for the result of the create user operation
     */
    public static void createUser(boolean isMale, String lmsUsername, final CreateUserAsyncResponseCallback callback) {

        TutorModule.getInstance().sendCreateUserMessage(isMale, lmsUsername, new MessageCollectionCallback() {

            private Message reply = null;

            @Override
            public void success() {
                if (reply != null) {
                    UserSession userSession = new UserSession(((UserData) reply.getPayload()).getUserId());

                    //the username field will be blank coming from the UMS db, therefore use the LMS username
                    //for this session
                    userSession.setUsername(((UserData) reply.getPayload()).getLMSUserName());

                    UserWebSession session = TutorModule.getInstance().createUserSession(userSession);
                    session.setLmsUsername(((UserData) reply.getPayload()).getLMSUserName());
                    callback.notify(userSession, true, "Create user was successful");
                }
            }

            @Override
            public void received(Message msg) {
                if (msg.getMessageType() == MessageTypeEnum.LOGIN_REPLY) {
                    reply = msg;
                }
            }

            @Override
            public void failure(Message msg) {
                StringBuilder response = new StringBuilder();
                response.append("NACK received from ");
                response.append(msg.getSenderAddress());
                response.append(": ");
                response.append(((NACK) msg.getPayload()).getErrorMessage());
                callback.notify(null, false, response.toString());
            }

            @Override
            public void failure(String why) {
                StringBuilder response = new StringBuilder();
                response.append("Failure: ");
                response.append(why);

                callback.notify(null, false, response.toString());
            }
        });
    }

    /**
     * Request the User Id from the UMS using the GIFT username as the key.
     *
     * @param username the GIFT username to use to lookup the GIFT user id
     * @param callback used when the request is replied too
     */
    public static void getUserIdByUserName(final String username, final GetUserIdAsyncResponseCallback callback) {

        TutorModule.getInstance().sendGetUserIdMessage(username, new MessageCollectionCallback() {

            private Message reply = null;

            @Override
            public void success() {

                if(reply.getPayload() != null){
                    UserData userData = (UserData)reply.getPayload();
                    callback.notify(userData.getUserId(), true, "User id obtained");
                }else{
                    callback.notify(0, false, "Did not receive a user id from the user id request");
                }

            }

            @Override
            public void received(Message msg) {
                reply = msg;
            }

            @Override
            public void failure(String why) {
                callback.notify(0, false, "Failed to get the user id for username of "+username+" because '"+why+"'.");
            }

            @Override
            public void failure(Message msg) {
                StringBuilder response = new StringBuilder();
                response.append("NACK received from ");
                response.append(msg.getSenderAddress());
                response.append(": ");
                response.append(((NACK) msg.getPayload()).getErrorMessage());
                callback.notify(0, false, "Failed to get the user id for username of "+username+" because '"+response.toString()+"'.");
            }
        });
    }

    /**
     * Login a user and create the user session.
     *
     * @param userSession - information about the user session (including the unique user id of the learner)
     *                      that is requesting the login
     * @param callback Callback for the result of the login operation
     */
    public static void loginUser(final UserSession userSession, final AsyncResponseCallback callback) {
        TutorModule.getInstance().sendLoginMessage(userSession, new MessageCollectionCallback() {

            private Message reply = null;

            @Override
            public void success() {

                if (reply != null) {

                    UserData userData = (UserData) reply.getPayload();
                    logger.info("Login was successful for user " + userData);

                      //set the username to the username provided or the LMS username if no username was provided
                      if(userSession.getUsername() == null){
                          userSession.setUsername(((UserData) reply.getPayload()).getLMSUserName());
                      }

                    UserWebSession session = TutorModule.getInstance().createUserSession(userSession);
                    session.setLmsUsername(((UserData) reply.getPayload()).getLMSUserName());

                    callback.notify(true, "Login was successful", null);

                } else {
                    logger.error("Got a null reply for the login message while logging in user " + userSession);
                }
            }

            @Override
            public void received(Message msg) {
                if (msg.getMessageType() == MessageTypeEnum.LOGIN_REPLY) {
                    reply = msg;
                }
            }

            @Override
            public void failure(Message msg) {
                StringBuilder response = new StringBuilder();

                if (((NACK) msg.getPayload()).getErrorEnum() == ErrorEnum.USER_NOT_FOUND_ERROR) {

                    if(userSession.getUsername() != null){
                        response.append("'");
                        response.append(userSession.getUsername());
                    }else{
                        response.append("User '");
                        response.append(userSession.getUserId());
                    }
                    response.append("' does not exist.");

                }else if(((NACK) msg.getPayload()).getErrorEnum() == ErrorEnum.INCORRECT_CREDENTIALS){

                    if(userSession.getUsername() != null){
                        response.append("Incorrect username/password for '");
                        response.append(userSession.getUsername());
                        response.append("'.");
                    }else{
                        response.append("User '");
                        response.append(userSession.getUserId());
                        response.append("' requires a username and password.");
                    }

                } else {
                    response.append("NACK received from ");
                    response.append(msg.getSenderAddress());
                    response.append(": ");
                    response.append(((NACK) msg.getPayload()).getErrorMessage());
                }
                callback.notify(false, response.toString(), null);
                logger.error("Login failed for user " + userSession + ": " + response.toString());
            }

            @Override
            public void failure(String why) {
                StringBuilder response = new StringBuilder();
                response.append("Failure: ");
                response.append(why);

                callback.notify(false, response.toString(), null);
                logger.error("Login failed for user " + userSession + ": " + response.toString());
            }
        });
    }

    /**
     * Starts the course belonging to the experiment with the given ID. This will also initialize a new user session, browser session, and
     * domain session for the client at the given address.
     *
     * @param experimentId the ID of the experiment whose course to start
     * @param experimentFolder the folder of the experiment course relative to
     *        the runtime experiment folder. Should only be null for legacy
     *        experiments.
     * @param clientAddress the addess of the client invoking this method
     * @param callback a callback that should be invoked when a subject has been made for the client or when an error has occurred
     * @param client information about this client that will be used to handle the browser session
     */
    public static void startExperimentCourse(String experimentId, String experimentFolder, final String clientAddress, final ClientProperties client, final ExperimentSubjectCreatedCallback callback) {

        TutorModule.getInstance().sendStartExperimentCourseMessage(experimentId, experimentFolder, clientAddress, client, callback, new MessageCollectionCallback() {

            private Message reply = null;

            @Override
            public void success() {
                logger.debug("startExperimentCourse received success");
                if (reply != null) {

                    //at this point, the Domain module has created a new user as an experiment subject and a new domain session, so we essentially
                    //need to treat the reply as a combination of a create user reply and a select domain reply

                    mil.arl.gift.common.DomainSession domainSession = (mil.arl.gift.common.DomainSession) reply.getPayload();

                    UserWebSession session = null;
                    TutorBrowserWebSession browserSession = null;

                    if(TutorModule.getInstance().getUserSession(domainSession) == null){
                        session = TutorModule.getInstance().createUserSession(domainSession);

                    } else {
                        session = TutorModule.getInstance().getUserSession(domainSession);
                    }

                    if(TutorModule.getInstance().getBrowserSession(session.getBrowserSessionKey()) == null){
                        browserSession = TutorModule.getInstance().createBrowserSession(session, clientAddress, client);

                    } else {
                        browserSession = TutorModule.getInstance().getBrowserSession(session.getBrowserSessionKey());
                    }

                    final DomainWebState domainWebState =
                            TutorModule.getInstance().createDomainWebState(domainSession, domainSession.getDomainSessionId(), domainSession.getDomainRuntimeId(), domainSession.getDomainSourceId(), domainSession.getExperimentId());

                    if (domainWebState != null) {

                        // Notify the browser sessions that a new domain web session was created.
                        session.onDomainWebStateCreated();

                        try{
                            //need to link the Tutor client for the new subject's user session to the Domain module that created it
                            TutorModule.getInstance().linkDomainModuleToUser(domainSession, reply.getSenderAddress());

                        } catch(@SuppressWarnings("unused") Exception e){
                            logger.warn("Successfully created experiment domain session, but could not link the new user session to the existing Domain client.");
                        }

                        TutorModule.getInstance().sendReply(reply, new ACK(), MessageTypeEnum.PROCESSED_ACK);

                    } else {

                        logger.error("Could not create an Experiment domain session for "+domainSession+".");

                        TutorModule.getInstance().sendReply(reply, new NACK(ErrorEnum.OPERATION_FAILED, "Could not create a domain session"), MessageTypeEnum.PROCESSED_NACK);
                        browserSession.displayDialog(DialogTypeEnum.ERROR_DIALOG, "Domain Selection Failure", "The server could not select a domain: Could not create a domain session.");
                        callback.onFailure("could not create domain session.", null);
                    }

                } else {
                    logger.error("Domain selection reply was not received.");

                    TutorModule.getInstance().sendReply(reply, new NACK(ErrorEnum.UNHANDLED_MESSAGE_ERROR, "A domain selection reply type message was not received"), MessageTypeEnum.PROCESSED_NACK);
                    callback.onFailure("domain selection reply was not received.", null);
                }
            }

            @Override
            public void received(Message msg) {
                logger.debug("startExperimentCourse received: " + msg);
                if (msg.getMessageType() == MessageTypeEnum.DOMAIN_SELECTION_REPLY) {
                    reply = msg;
                }
            }

            @Override
            public void failure(Message msg) {

                String errorMsg = ((NACK) msg.getPayload()).getErrorMessage();
                String help = ((NACK) msg.getPayload()).getErrorHelp();

                StringBuilder response = new StringBuilder();
                response.append(errorMsg);

                logger.debug("domain session did exist, sending callback response of '"+response.toString()+"' to client.");
                callback.onFailure(response.toString(), help);
            }

            @Override
            public void failure(String why) {
                logger.debug("startExperimentCourse received why failure: " + why);
                StringBuilder response = new StringBuilder();
                response.append("Failure: ");
                response.append(why);

                callback.onFailure(response.toString(), null);
            }
        });
    }

    /**
     * Handler for the request to display a team session.
     *
     * @param msg The received message
     */
    public void handleDisplayTeamSessionMessage(final Message msg) {
        try{

            DomainWebState webState = getDomainWebState();

            if (webState != null) {

                // Tell the client to display the team session widget.
                WidgetProperties properties = new WidgetProperties();
                properties.setIsFullscreen(true);
                webState.displayWidget(WidgetTypeEnum.TEAM_SESSION_WIDGET, properties);
            }
        } catch (Exception e) {
            logger.error("Exception caught handling the display team session message request.", e);
        }

        // For now just ack back to the server.
        TutorModule.getInstance().sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);
    }

    /**
     * Handles a message to display some feedback in the TUI
     *
     * @param msg The received message
     */
    public void handleDisplayFeedbackTutorRequestMessage(final Message msg) {

        try{

            DomainWebState webState = getDomainWebState();

            if (webState != null) {

                TutorUserInterfaceFeedback feedback = (TutorUserInterfaceFeedback) msg.getPayload();

                // Check to see if this is "message" feedback. The assumption
                // right now is that if a text and avatar action is present,
                // then it is a "message" feedback that can be displayed either
                // as text, or spoken by the avatar, or both
                // Eventually, this assumption will no longer be correct
                if (feedback.getDisplayTextAction() != null && feedback.getDisplayAvatarAction() != null) {

                    generated.dkf.InTutor deliverySettings = feedback.getDisplayTextAction().getDeliverySettings();
                    if(deliverySettings != null){

                        // Mutate the message feedback depending on how the display mode is set
                        if (MessageFeedbackDisplayModeEnum.TEXT_ONLY.getName().equals(deliverySettings.getMessagePresentation())) {

                            feedback = new TutorUserInterfaceFeedback(feedback.getDisplayTextAction(), feedback.getPlayAudioAction(), null, feedback.getClearTextAction(), null);

                        } else if (MessageFeedbackDisplayModeEnum.AVATAR_ONLY.getName().equals(deliverySettings.getMessagePresentation())) {

                            // Only mutate the feedback if the CS is online and a default avatar is set to be used
                            if (feedback.getDisplayAvatarAction().getAvatar() == null && CharacterServerService.getInstance().isOnline()) {

                                feedback = new TutorUserInterfaceFeedback(null, feedback.getPlayAudioAction(), feedback.getDisplayAvatarAction(), feedback.getClearTextAction(), null);
                            }
                        }
                    }
                }

                if (feedback.getDisplayAvatarAction() != null) {

                    if (feedback.getDisplayAvatarAction().getAvatar() == null) {

                        //TODO: The character server being down can also affect avatars with
                        //      scripted events
                        //      If TTS is requested, that should be denied also
                        if (CharacterServerService.getInstance().isOnline()) {

                            feedback.getDisplayAvatarAction().setAvatar(new AvatarData(DEFAULT_CHARACTER_PATH, 200, 250));

                        } else {

                            logger.warn("Feedback needs default avatar, but Character Server is not enabled. Not rendering avatar.");

                            try {

                                if(feedback.getDisplayAvatarAction().isPreloadOnly()
                                        || feedback.getDisplayTextAction() == null && feedback.getPlayAudioAction() == null && feedback.getClearTextAction() == null) {

                                    //skip message if preloading an avatar or showing a message that ONLY uses the avatar
                                    if(msg.needsHandlingResponse()){
                                        TutorModule.getInstance().sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);
                                    }

                                    return;

                                } else {
                                    feedback = new TutorUserInterfaceFeedback(feedback.getDisplayTextAction(), feedback.getPlayAudioAction(), null, feedback.getClearTextAction(), null);
                                }


                            } catch (@SuppressWarnings("unused") RuntimeException e) {

                                logger.warn("Without avatar, feedback is no longer valid.");

                                TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "Can't render default avatar, feedback is no longer valid."), MessageTypeEnum.PROCESSED_NACK);

                                return;
                            }
                        }
                    }

                    if(feedback.getDisplayAvatarAction() != null && feedback.getDisplayAvatarAction().isPreloadOnly()) {

                        logger.info("Preloading avatar: " + feedback.getDisplayAvatarAction().getAvatar());

                        //if the domain is trying to preload this avatar before it is shown, have the client load it without updating the article widget
                        webState.doServerAction(new PreloadAvatarAction(feedback.getDisplayAvatarAction().getAvatar()));

                        if(msg.needsHandlingResponse()){

                            //when the domain module message sender wants a response to the feedback request
                            TutorModule.getInstance().sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);
                        }

                        return;
                    }
                }

                //Calculate characteristics of the feedback message
                boolean isScriptedAvatarAction = feedback.getDisplayAvatarAction() instanceof DisplayScriptedAvatarAction;
                boolean isTextToSpeechAvatarAction = feedback.getDisplayAvatarAction() instanceof DisplayTextToSpeechAvatarAction;
                boolean originalMessageIsDummyMessage = feedback.onlyContainsAvatarAction()
                        && !isScriptedAvatarAction
                        && !isTextToSpeechAvatarAction;

                boolean shouldCreateDummyMessage = !originalMessageIsDummyMessage && !feedback.onlyContainsClearAction();

                // Create the dummy message to be sent across the websocket
                //if the message contains more than avatar data.
                // If the message only contains avatar data, then it can be
                //sent directly across the websocket instead of creating an
                //additional dummy message.
                // The dummy message serves the purpose of displaying the
                //appropriate avatar and forcing the client to send an
                //avatar idle notification so that queued feedback can
                //be displayed.
                TutorUserInterfaceFeedback showAvatarFeedback = null;
                if(shouldCreateDummyMessage) {
                    showAvatarFeedback = new TutorUserInterfaceFeedback();
                    showAvatarFeedback.setDisplayAvatarAction(new DisplayAvatarAction());

                    if(feedback.getDisplayAvatarAction() != null){
                        AvatarData showAvatarData = feedback.getDisplayAvatarAction().getAvatar();
                        showAvatarFeedback.getDisplayAvatarAction().setAvatar(showAvatarData);
                    }
                }

                // If showAvatarFeedback is null, then a dummy message was not created, and
                //therefore only one message needs to be sent.
                if(originalMessageIsDummyMessage) {
                    webState.displayChatWidget(true, Arrays.asList(feedback));
                } else {
                    if(showAvatarFeedback != null) {
                        webState.displayChatWidget(true, Arrays.asList(showAvatarFeedback));
                    }

                    if(UpdateQueueManager.getInstance().shouldEnqueueNextFeedbackUpdate(getUserId())) {

                        if(feedback.getClearTextAction() != null) {
                            // Don't enqueue a clear text action since the queue is going to be cleared for the next transition.
                            webState.displayArticleFeedback(feedback);
                            webState.notifyFeedbackWidget(0);

                        } else {
                            UpdateQueueManager.getInstance().enqueueFeedbackUpdate(getUserId(), feedback);
                            webState.notifyFeedbackWidget(UpdateQueueManager.getInstance().getFeedbackUpdateQueueLength(getUserId()));
                        }

                    } else {
                        webState.displayArticleFeedback(feedback);
                    }
                }

                if(msg.needsHandlingResponse()){
                    //when the domain module message sender wants a response to the feedback request

                    if(webState.isCharacterIdle() || !originalMessageIsDummyMessage){
                        // don't delay the feedback request ACK if the character is already loaded and not acting right now (idle)
                        // don't delay the feedback request ACK if the request contains feedback to speak and the character is already acting (not idle)
                        //    - this case allows the domain module to send multiple feedback actions w/o having to wait for the character to finish and have the
                        //      tutor module queue the actions appropriately.
                        //

                        TutorModule.getInstance().sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);
                    }else{
                        webState.addCharacterIdleListener(new CharacterIdleListener() {

                            @Override
                            public boolean shouldRemoveListener() {
                                return true;
                            }

                            @Override
                            public void idleNotification() {
                                //needed by at least the case where a clear feedback history message is sent by the domain module
                                //that sender is waiting for a P'ACK message response before continuing.
                                TutorModule.getInstance().sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);
                            }
                        });
                    }


                }

            } else {

                TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.DOMAIN_SESSION_NOT_FOUND_ERROR, "Not an active domain session."), MessageTypeEnum.PROCESSED_NACK);
            }


        }catch(Exception e){
            logger.error("Caught exception while handling display feedback message.  Sending NACK message as a reply.", e);
            TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }

    /**
     * Handle an update to the current chat window being displayed on the TUI.  The update most likely involves
     * a new chat entry from the tutor.
     *
     * @param msg the chat window update message to handle
     */
    public void handleDisplayChatWindowUpdateRequestMessage(final Message msg) {

        try{
                DomainWebState webState = getDomainWebState();

                if (webState != null) {

                    DisplayChatWindowUpdateRequest chatRequest = (DisplayChatWindowUpdateRequest) msg.getPayload();

                    if (chatRequest.getAvatarAction() != null) {

                        if (chatRequest.getAvatarAction().getAvatar() == null) {

                            //TODO: The CS being down can also effect provided avatars
                            //      If TTS is requested, that should be denied also
                            if (CharacterServerService.getInstance().isOnline()) {

                                chatRequest.getAvatarAction().setAvatar(new AvatarData(DEFAULT_CHARACTER_PATH, 200, 250));

                            } else {

                                logger.warn("Feedback needs default avatar, but Character Server is not enabled. Not rendering avatar for chat update.");
                            }
                        }
                    }

                    if(UpdateQueueManager.getInstance().shouldEnqueueNextChatUpdate(getUserId(), chatRequest.getChatId())) {

                        UpdateQueueManager.getInstance().enqueueChatUpdate(getUserId(), chatRequest);

                        webState.notifyArticleChatWindow(chatRequest.getChatId(),
                                UpdateQueueManager.getInstance().getChatUpdateQueueLength(getUserId(), chatRequest.getChatId()));

                    } else {
                        webState.displayArticleChatWindow(chatRequest);
                    }

                    TutorModule.getInstance().sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);

                } else {
                    TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.DOMAIN_SESSION_NOT_FOUND_ERROR, "Not an active domain session."), MessageTypeEnum.PROCESSED_NACK);
                }



        }catch(Exception e){
            logger.error("Sending NACK message as a reply to a chat window update request message because an exception was caught.\n"+msg, e);
            NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "There was a problem while attempting to apply the chat window update request.  The error message is "+e.getMessage());
            nack.setErrorHelp("If you have access to the Tutor module server log, it will contain additional details on the error.  Otherwise contact the GIFT administrator for additional help.");
            TutorModule.getInstance().sendReply(msg, nack, MessageTypeEnum.PROCESSED_NACK);
        }
    }


    /**
     * Handle the display chat window request message by displaying the chat window interface on the TUI.
     *
     * @param msg the display chat window request message to handle
     */
    public void handleDisplayChatWindowRequestMessage(final Message msg) {

        try{


            DomainWebState webState = getDomainWebState();

            if (webState != null) {

                DisplayChatWindowRequest chatRequest = (DisplayChatWindowRequest) msg.getPayload();

                logger.info("Handling chat window request of "+chatRequest);

                if (chatRequest.getAvatar() != null) {

                    if (chatRequest.getAvatar().getAvatar() == null) {

                        //TODO: The CS being down can also effect provided avatars
                        //      If TTS is requested, that should be denied also
                        if (CharacterServerService.getInstance().isOnline()) {
                            chatRequest.getAvatar().setAvatar(new AvatarData(DEFAULT_CHARACTER_PATH, 200, 250));

                        } else if(!getDomainWebState().isCharacterIdle()) {

                            //treat the avatar as idle if it is offline so that the queue of messages can proceed normally, otherwise
                            //the conversation won't proceed past the first message
                            getDomainWebState().characterIdleNotification();
                        }
                    }
                }

                webState.displayArticleChatWindow(chatRequest, new ActionListener() {
                    @Override
                    public void onAction(AbstractAction action) {

                        if(action.getActionType() == ActionTypeEnum.CLOSE) {

                            logger.info("Received "+action.getActionType()+" action on the chat window.  Sending ACK message to original display chat window request.");

                            TutorModule.getInstance().sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);
                        }
                    }
                });

                //not sure if this should be done here but the domain module needs an initial empty chat to kick things off
                ChatLog chat = new ChatLog(chatRequest.getChatId(), new ArrayList<String>(0), new ArrayList<String>(0));
                TutorModule.getInstance().sendChatMessage(chat, webState, null);

            } else {
                TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.DOMAIN_SESSION_NOT_FOUND_ERROR, "Not an active domain session."), MessageTypeEnum.PROCESSED_NACK);
            }


        }catch(Exception e){
            logger.error("Caught exception while handling display chat window request message.  Sending NACK message as a reply.", e);
            TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }


    public void handleDisplayLearnerActionsMessage(final Message msg) {

        DomainWebState webState = getDomainWebState();

        if (webState != null) {
            DisplayLearnerActionsTutorRequest request = (DisplayLearnerActionsTutorRequest)msg.getPayload();

            //need to notify client that a training application lesson is initializing so that a loading message can be shown
            webState.lessonInitializing();

            webState.displayLearnerActions(false, request.getActions(), request.getControls());
            TutorModule.getInstance().sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);

        } else {
            TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.DOMAIN_SESSION_NOT_FOUND_ERROR, "Not an active domain session."), MessageTypeEnum.PROCESSED_NACK);
        }
    }

    /**
     * Handles a course state message (normally sent by the domain module) which is an indication
     * that a new course object could be loaded next.  The tutor client should be notified
     * that the current course object is completed and any widget clean up should be performed now.
     *
     * @param msg the course state domain session message
     */
    public void handleCourseStateMessage(final Message msg){

        UpdateQueueManager.getInstance().discardUserQueues(getUserId());
        DomainWebState webState = getDomainWebState();

        if (webState != null) {
            webState.courseObjectCompleted();
        } else {
            TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.DOMAIN_SESSION_NOT_FOUND_ERROR, "Not an active domain session."), MessageTypeEnum.PROCESSED_NACK);
        }
    }

    /**
     * Handles a message to indicate the lesson has finished.
     *
     * @param msg The received message
     */
    public void handleLessonCompletedMessage(final Message msg) {

        try{

            DomainWebState webState = getDomainWebState();
            if (webState != null) {
                webState.lessonCompleted();
                TutorModule.getInstance().sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);
            } else {
                TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.DOMAIN_SESSION_NOT_FOUND_ERROR, "Not an active domain session."), MessageTypeEnum.PROCESSED_NACK);
            }


        }catch(Exception e){
            logger.error("Caught exception while handling lesson completed message.  Sending NACK message as a reply.", e);
            TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }

    /**
     * Handles a message to indicate the lesson has started.
     *
     * @param msg The received message
     */
    public void handleLessonStartedMessage(final Message msg) {

        try{

            DomainWebState webState = getDomainWebState();
            if (webState != null) {
                webState.lessonStarted();
                TutorModule.getInstance().sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);
            } else {
                TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.DOMAIN_SESSION_NOT_FOUND_ERROR, "Not an active domain session."), MessageTypeEnum.PROCESSED_NACK);
            }


        }catch(Exception e){
            logger.error("Caught exception while handling lesson started message.  Sending NACK message as a reply.", e);
            TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }

    /**
     * Handles a message to display the AAR in the TUI.
     *
     * @param msg The received message
     */
    public void handleDisplayAarTutorPanelMessage(final Message msg) {

        try{

            UpdateQueueManager.getInstance().discardUserQueues(getUserId());
            DomainWebState webState = getDomainWebState();
            if (webState != null) {
                ActionListener listener = new ActionListener() {

                    @Override
                    public void onAction(AbstractAction action) {
                        TutorModule.getInstance().sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);
                    }
                };

                DisplayAfterActionReviewTutorRequest request = (DisplayAfterActionReviewTutorRequest) msg.getPayload();
                webState.displayAfterActionReview(request.getTitle(), request.getFullScreen(), request.getEvents(), listener);
            } else {
                TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.DOMAIN_SESSION_NOT_FOUND_ERROR, "Not an active domain session."), MessageTypeEnum.PROCESSED_NACK);
            }


        }catch(Exception e){
            logger.error("Caught exception while handling display AAR message.  Sending NACK message as a reply.", e);
            TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }

    /**
     * Handles a message to display lesson material in the TUI
     *
     * @param msg The received message
     */
    public void handleDisplayLessonMaterialTutorPanelMessage(final Message msg) {

        try{

            UpdateQueueManager.getInstance().discardUserQueues(getUserId());
            DomainWebState webState = getDomainWebState();
            if (webState != null) {
                ActionListener listener = new ActionListener() {

                    @Override
                    public void onAction(AbstractAction action) {

                        if(action instanceof SubmitAction){

                            SubmitAction submitAction = (SubmitAction) action;

                            if(submitAction.getProperties() != null){

                                if(DisplayMediaCollectionWidgetProperties.getOverDwelled(submitAction.getProperties())){

                                    //let the Domain know that the learner spent too much time on this lesson material
                                    TutorModule.getInstance().sendReply(msg, "Overdwell", MessageTypeEnum.OVER_DWELL_VIOLATION);

                                    return;

                                } else if(DisplayMediaCollectionWidgetProperties.getUnderDwelled(submitAction.getProperties())){

                                    //let the Domain know that the learner spent too little time on this lesson material
                                    TutorModule.getInstance().sendReply(msg, "Underdwell", MessageTypeEnum.UNDER_DWELL_VIOLATION);

                                    return;
                                }
                            }
                        }

                        TutorModule.getInstance().sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);
                    }
                };

                DisplayMediaCollectionRequest request = (DisplayMediaCollectionRequest) msg.getPayload();

                //copy media to a list that is agnostic of schema type for conversion to HTML
                List<Serializable> mediaList = new ArrayList<>();
                mediaList.addAll(request.getMediaList());

                List<MediaHtml> mediaHtmlList = GiftServletUtils.convertMediaListToHtml(mediaList);
                LessonMaterialList.Assessment assessment = null;

                if(request.getLessonMaterial() != null
                        && request.getLessonMaterial().getLessonMaterialList() != null){

                    //get the assessment for this lesson material, if one is available
                    assessment = request.getLessonMaterial().getLessonMaterialList().getAssessment();
                }

                webState.displayMediaCollection(mediaHtmlList, listener, assessment);
            } else {
                TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.DOMAIN_SESSION_NOT_FOUND_ERROR, "Not an active domain session."), MessageTypeEnum.PROCESSED_NACK);
            }

        }catch(Exception e){
            logger.error("Caught exception while handling display lesson material message.  Sending NACK message as a reply.", e);
            TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }

    /**
     * Handles a message to display mid-lesson media in the TUI
     *
     * @param msg The received message
     */
    public void handleDisplayMidLessonMediaTutorPanelMessage(final Message msg) {

        try{

            UpdateQueueManager.getInstance().discardUserQueues(getUserId());
            DomainWebState webState = getDomainWebState();
            if (webState != null) {

                ActionListener listener = new ActionListener() {

                    @Override
                    public void onAction(AbstractAction action) {
                        TutorModule.getInstance().sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);
                    }
                };

                DisplayMidLessonMediaRequest request = (DisplayMidLessonMediaRequest) msg.getPayload();

                List<Serializable> mediaList = new ArrayList<>();
                mediaList.addAll(request.getMediaList());

                List<MediaHtml> mediaHtmlList = GiftServletUtils.convertMediaListToHtml(mediaList);

                webState.displayMediaCollection(mediaHtmlList, listener, null);
            } else {
                TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.DOMAIN_SESSION_NOT_FOUND_ERROR, "Not an active domain session."), MessageTypeEnum.PROCESSED_NACK);
            }

        }catch(Exception e){
            logger.error("Caught exception while handling display mid-lesson media message.  Sending NACK message as a reply.", e);
            TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }

    /**
     * Handles a message to display initialization instructions for a course
     *
     * @param msg The received message
     */
    public void handleDisplayCourseInitInstructionsRequest(final Message msg){

        try{
             //list of browser sessions assigned to a user that need to be overriden to display the course initialization instructions
            UserSessionMessage userSessionMessage = (UserSessionMessage) msg;
            UserSession user = userSessionMessage.getUserSession();
             Map<TutorBrowserWebSession, LoadCourseParameters> browserSessionToPendingCourseName = TutorModule.getInstance().getBrowserSessionToPendingCourseNameMap();
             for(TutorBrowserWebSession browserSession : browserSessionToPendingCourseName.keySet()){

                 if(browserSession.getUserSessionKey() != null && browserSession.getUserSessionKey().equals(getUserSessionKey())){

                     if(getDomainWebState() == null){

                         // Using a browser session's remembered domain selection, create a domain session web session.
                         LoadCourseParameters loadCourseParameters = browserSessionToPendingCourseName.get(browserSession);
                         TutorModule.getInstance().createDomainWebState(user, ((DomainSessionMessage) msg).getDomainSessionId(),
                                 loadCourseParameters.getCourseRuntimeId(), loadCourseParameters.getCourseSourceId(), ((DomainSessionMessage) msg).getExperimentId());
                     }

                 }
             }

             DomainWebState webState = getDomainWebState();

             if(webState != null) {

                 // Notify the browser sessions that a new domain web session was created.
                 onDomainWebStateCreated();

                 ActionListener listener = new ActionListener() {

                     @Override
                     public void onAction(AbstractAction action) {
                         TutorModule.getInstance().sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);
                     }
                 };

                 //display the course initialization instructions
                 DisplayCourseInitInstructionsRequest request = (DisplayCourseInitInstructionsRequest) msg.getPayload();
                 webState.displayCourseInitInstructions(request, listener);
             } else {
                 TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.DOMAIN_SESSION_NOT_FOUND_ERROR, "The DomainWebState is null."), MessageTypeEnum.PROCESSED_NACK);
             }
        } catch(Exception e){
            logger.error("Caught exception while handling display course init instructions message.  Sending NACK message as a reply.", e);
            TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }


    /**
     * Handles a message with a load progress created by the gateway module loading content into a training application.
     *
     * @param msg contains the load progress to show to the learner
     */
    public void handleLoadProgress(final Message msg){

        try{


            UpdateQueueManager.getInstance().discardUserQueues(getUserId());
            DomainWebState webState = getDomainWebState();
            if (webState != null) {

                webState.applyLoadProgress((GenericJSONState)msg.getPayload());

            }else {
                TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.DOMAIN_SESSION_NOT_FOUND_ERROR, "Not an active domain session."), MessageTypeEnum.PROCESSED_NACK);
            }


        }catch(Exception e){
            logger.error("Caught exception while handling load progress message.  Sending NACK message as a reply.", e);
            TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }


    /**
     * Handles a message with a survey submit request created by the learner answering the survey through
     * the training application.
     *
     * @param msg contains the survey submit request to apply
     */
    public void handleTrainingApplicationSurveySubmit(final Message msg){

        try{
            UpdateQueueManager.getInstance().discardUserQueues(getUserId());
            DomainWebState webState = getDomainWebState();
            if (webState != null) {

                webState.applyExternalSurveySubmit();

            }else {
                TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.DOMAIN_SESSION_NOT_FOUND_ERROR, "Not an active domain session."), MessageTypeEnum.PROCESSED_NACK);
            }

        }catch(Exception e){
            logger.error("Caught exception while handling training application survey submit message.  Sending NACK message as a reply.", e);
            TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }


    /**
     * Handles a message with a survey response created by the learner answering the survey through
     * the training application.
     *
     * @param msg contains the survey response to apply
     */
    public void handleTrainingApplicationSurveyResponse(final Message msg){

        try{
            UpdateQueueManager.getInstance().discardUserQueues(getUserId());
            DomainWebState webState = getDomainWebState();
            if (webState != null) {

                SurveyResponse surveyResponse = (SurveyResponse)msg.getPayload();
                webState.applyExternalSurveyResponse(surveyResponse);

            }else {
                TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.DOMAIN_SESSION_NOT_FOUND_ERROR, "Not an active domain session."), MessageTypeEnum.PROCESSED_NACK);
            }
        }catch(Exception e){
            logger.error("Caught exception while handling training application survey response message.  Sending NACK message as a reply.", e);
            TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }

    }


    /**
     * Handles a message to display a survey in the TUI
     *
     * @param msg The received message
     */
    public void handleDisplaySurveyTutorPanelMessage(final Message msg) {

        try{
            final DomainWebState webState = getDomainWebState();

            if (webState != null) {

                if(!webState.isLessonActive()) {

                    //if this is not a mid-lesson survey, we need to clean up the learner's queued feedback messages
                    UpdateQueueManager.getInstance().discardUserQueues(getUserId());
                }

                ActionListener listener = new ActionListener() {

                    private boolean handled = false;

                    @Override
                    public void onAction(AbstractAction action) {
                        if (!handled) {
                            if (action.getActionType() == ActionTypeEnum.SUBMIT) {
                                SubmitAction submit = (SubmitAction) action;

                                //first check if the submit is a survey response
                                SurveyResponse surveyResponse = SurveyWidgetProperties.getAnswers(submit.getProperties());

                                if (surveyResponse != null) {

                                    // Copy the survey response before it goes out on the wire.
                                    SurveyResponse newSurveyResponse = SurveyResponse.createShallowCopy(surveyResponse);

                                    handled = true;
                                    TutorModule.getInstance().sendReply(msg, newSurveyResponse, MessageTypeEnum.DISPLAY_SURVEY_TUTOR_REPLY);
                                }else{
                                    //second check if the submit is an intermediate question response
                                    AbstractQuestionResponse questionResponse = SurveyWidgetProperties.getCurrentQuestionAnswers(submit.getProperties());

                                    if(questionResponse != null){
                                        //send question response to domain
                                        TutorModule.getInstance().sendTutorSurveyQuestionResponse(questionResponse, getUserSessionInfo(), webState.getDomainSessionId());
                                    }
                                }

                            } else if (action.getActionType() == ActionTypeEnum.CLOSE) {
                                handled = true;
                                TutorModule.getInstance().sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);
                            }
                        }
                    }
                };

                DisplaySurveyTutorRequest request = (DisplaySurveyTutorRequest) msg.getPayload();
                webState.displaySurvey(request, listener);

            } else {
                TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.DOMAIN_SESSION_NOT_FOUND_ERROR, "Not an active domain session."), MessageTypeEnum.PROCESSED_NACK);
            }


        }catch(Exception e){
            logger.error("Caught exception while handling display survey message.  Sending NACK message as a reply.", e);
            TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }


    /**
     * Handles a message to display some text in the TUI
     *
     * @param msg The received message
     */
    public void handleDisplayContentRequestMessage(final Message msg) {

        try{

            DomainWebState webState = getDomainWebState();
            
            if(webState == null) {
                // USE CASE:
                // the current use case for this is when the domain module request to show the GIFT 'did you know' guidance page
                // while the domain session is being started.  The logic is a copy from the {@link handleDisplayCourseInitInstructionsRequest} method
                // which is also executed before the tutor's domain web state is fully initialized
                
                //list of browser sessions assigned to a user that need to be overridden to display the course initialization instructions
               UserSessionMessage userSessionMessage = (UserSessionMessage) msg;
               UserSession user = userSessionMessage.getUserSession();
                Map<TutorBrowserWebSession, LoadCourseParameters> browserSessionToPendingCourseName = TutorModule.getInstance().getBrowserSessionToPendingCourseNameMap();
                for(TutorBrowserWebSession browserSession : browserSessionToPendingCourseName.keySet()){
    
                    if(browserSession.getUserSessionKey() != null && browserSession.getUserSessionKey().equals(getUserSessionKey())){
    
                        if(getDomainWebState() == null){
    
                            // Using a browser session's remembered domain selection, create a domain session web session.
                            LoadCourseParameters loadCourseParameters = browserSessionToPendingCourseName.get(browserSession);
                            TutorModule.getInstance().createDomainWebState(user, ((DomainSessionMessage) msg).getDomainSessionId(),
                                    loadCourseParameters.getCourseRuntimeId(), loadCourseParameters.getCourseSourceId(), ((DomainSessionMessage) msg).getExperimentId());
                        }
    
                    }
                }

                webState = getDomainWebState();
            }

            if (webState != null) {

                AbstractDisplayContentTutorRequest displayContentRequest = (AbstractDisplayContentTutorRequest) msg.getPayload();

                ActionListener listener = new ActionListener() {
                    @Override
                    public void onAction(AbstractAction action) {

                        logger.info("The request from a DisplayGuidanceTutorRequest has been fulfilled, received: " + action);

                        TutorModule.getInstance().sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);
                    }
                };

                /*
                * Make adjustments to URI data.
                */
                if(displayContentRequest instanceof DisplayMediaTutorRequest) {
                    adjustDisplayGuidanceTutorRequest((DisplayMediaTutorRequest) displayContentRequest);
                }

                webState.displayArticleContent(displayContentRequest, listener);

            } else {
                TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.DOMAIN_SESSION_NOT_FOUND_ERROR, "Not an active domain session."), MessageTypeEnum.PROCESSED_NACK);
            }

        }catch(Exception e){
            logger.error("Caught exception while handling display guidance message.  Sending NACK message as a reply.", e);
            TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }

    /**
     * This checks for Same Origin Policy violation and Mixed Content errors.
     * @param mediaUri The URI to validate against.
     * @return Returns true if the URI is in violation of same origin policy violation or mixed content errors.
     */
    private static synchronized boolean validateUriForViolation(StringBuilder mediaUri){
        return UriUtil.validateUriForSOPViolationOrBlockedContent(mediaUri);
    }

    /**
     * Handles adjustments to the incoming DisplayHtmlPageGuidanceTutorRequest. This allows for dynamic updates
     * with respect to data that only the TutorModule has interest in.  This method currently supports URI validation against
     * Same Origin Policy violations and blocked content caused by requesting insecure content from a secure page.
     *
     * @param request The DisplayHtmlPageGuidanceTutorRequest to make adjustments on
     */
    private void adjustDisplayGuidanceTutorRequest(DisplayMediaTutorRequest request) {

        String requestUrl;

        if (request != null) {

            requestUrl = request.getUrl();

            if(requestUrl != null && requestUrl.length() > 0 ) {

                StringBuilder urlStr = new StringBuilder(requestUrl);
                if(validateUriForViolation(urlStr)) {
                    request.setShouldOpenInNewWindow(true);
                }
            }
        }
    }

    /**
     * Retrieves and handles a queued feedback update.
     *
     * @return true if there was an update to handle, false otherwise.  False can be returned if the server considers the
     * avatar to not be idle.
     */
    public boolean dequeueFeedbackUpdate() {

        if (getDomainWebState() != null) {
            TutorUserInterfaceFeedback feedback = UpdateQueueManager.getInstance().dequeueFeedbackUpdate(getUserId());
            if(feedback != null) {
                return getDomainWebState().displayArticleFeedback(feedback);
            }
        }

        return false;
    }


    /**
     * Handles a message that the domain session is being closed
     *
     * @param msg The received message
     */
    public void handleCloseDomainSessionRequestMessage(Message msg) {

        try{

            DomainWebState webState = getDomainWebState();
            if (webState != null) {

                logger.info("Removing domain session entry because of close domain session request for domain session id of "+webState.getDomainSessionId());
                TutorModule.getInstance().removePedagogicalInstance(webState.getUserSession(), webState.getDomainSessionId());
                onDomainSessionClosed();

                TutorModule.getInstance().sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);

                //release domain session connections
                DomainSession domainSession = new DomainSession(webState.getDomainSessionId(), getUserId(), webState.getDomainRuntimeId(), webState.getDomainSourceId());
                domainSession.setExperimentId(getUserSessionInfo().getExperimentId());
                TutorModule.getInstance().cleanupDomainSession(domainSession);

            } else if (isCreatingDomainSession()) {
                logger.info("Received a close domain session request for a user session that is in the process of creating a domain session. "+msg);
                TutorModule.getInstance().sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);
            } else{
                logger.info("Received a close domain session request for a user session that doesn't have a domain session for user = "+getUserId()+
                        ", domainWebState = "+getDomainWebState()+".  Sending ACK because the domain session is technically closed.  "+msg);
                TutorModule.getInstance().sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);
            }

            UpdateQueueManager.getInstance().discardUserQueues(getUserId());


        }catch(Exception e){
            logger.error("Caught exception while handling close domain session request message.  Sending NACK message as a reply.", e);
            TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }

    /**
     * Handles a message that the domain session is being initialized
     *
     * @param msg The received message
     */
    public void handleInitializeDomainSessionRequestMessage(Message msg) {

        try{
            DomainWebState webState = getDomainWebState();
            if (webState != null) {
                TutorModule.getInstance().sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);
            } else {
                TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.DOMAIN_SESSION_NOT_FOUND_ERROR, "Not an active domain session."), MessageTypeEnum.PROCESSED_NACK);
            }

        }catch(Exception e){
            logger.error("Caught exception while handling initialize domain session request message.  Sending NACK message as a reply.", e);
            TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }

    /**
     * Handles a message that the domain session is being started
     *
     * @param msg The received message
     */
    public void handleStartDomainSessionRequestMessage(Message msg) {
        logger.debug("handleStartDomainSessionRequestMessage: " + msg);
        try{

            DomainWebState webState = getDomainWebState();
            if (webState != null) {
                TutorModule.getInstance().sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);
            } else {
                TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.DOMAIN_SESSION_NOT_FOUND_ERROR, "Not an active domain session."), MessageTypeEnum.PROCESSED_NACK);
            }

            //if a user has browser sessions that were waiting for a domain session to start, we need to remove their pending status

            Map<TutorBrowserWebSession, LoadCourseParameters> browserSessionToPendingCourseName = TutorModule.getInstance().getBrowserSessionToPendingCourseNameMap();
            Iterator<TutorBrowserWebSession> itr = browserSessionToPendingCourseName.keySet().iterator();
            while(itr.hasNext()){

                TutorBrowserWebSession browserSession = itr.next();
                if(browserSession.getUserSessionKey() != null && browserSession.getUserSessionKey().equals(getUserSessionKey())){
                    itr.remove();
                }
            }


        }catch(Exception e){
            logger.error("Caught exception while handling start domain session request message.  Sending NACK message as a reply.", e);
            TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.OPERATION_FAILED, "An exception was thrown."), MessageTypeEnum.PROCESSED_NACK);
        }
    }

    /**
     * Processes an incoming action from the web client.
     *
     * @param action The action received
     */
    public void handleClientAction(AbstractAction action) {
        if (logger.isDebugEnabled()) {
            logger.debug("handleClientAction() called with action: " + action);
        }
        if (getDomainWebState() != null) {

            // For now the user web session passes the action into the domain web session state to be processed.
            DomainWebState webState = getDomainWebState();
            webState.processAction(action);
        }
    }

    /**
     * Processes an incoming action from the server.
     *
     * @param action The action to be processed.
     */
    public void handleServerAction(AbstractAction action) {
        if (logger.isDebugEnabled()) {
            logger.debug("handleServerAction() called with action: " + action);
        }

        // Apply the action to the domain web state.
        // This typically updates the widget state.
        if (getDomainWebState() != null) {

            // For now the user web session passes the action into the domain web session state to be processed.
            DomainWebState webState = getDomainWebState();
            webState.processAction(action);
        }


        // Send the update to any browser session that is connected.
        synchronized(browserSessions) {
            for (TutorBrowserWebSession browserSession : browserSessions) {
                if (!browserSession.isSuspended()) {
                    browserSession.processAction(action);
                }
            }
        }

    }

    /**
     * Handler for when the domain web state is created.
     *
     */
    public void onDomainWebStateCreated() {

        // Notify the browser web sessions that the domain web session state was created.
        // This initializes the browser sessions with the same domain web state.
        synchronized(browserSessions) {
            for (TutorBrowserWebSession browserSession : browserSessions) {

                DomainWebState webState = getDomainWebState();

                if (webState != null) {
                    webState.notifyBrowserWebSessionOfNewDomainWebState(browserSession);
                } else {
                    logger.error("Unable to notify the browser sessions that a new domain web state was created because the domainWebState is null.");
                }
            }
        }
    }

    /**
     * Handler for when the domain session is closed.  This should notify the browser
     * web sessions along with cleaning up the domain web state.
     *
     */
    public void onDomainSessionClosed() {
        if (logger.isDebugEnabled()) {
            logger.debug("onDomainSessionClosed");
        }

        // Update the domain session web state.
        DomainWebState webState = getDomainWebState();

        if (webState != null) {
            webState.onDomainSessionClosed();

            logger.info("Removing domain session entry because domain session ended for domain session id of "+getDomainWebState().getDomainSessionId());
            TutorModule.getInstance().removePedagogicalInstance(domainWebState.getUserSession(), getDomainWebState().getDomainSessionId());

            logger.info("Domain session has ended therefore setting domain web state to null for User Session object");
            setDomainWebState(null);
        }

        // Notify the browser web sessions.
        synchronized(browserSessions) {
            for (TutorBrowserWebSession browserSession : browserSessions) {
                if (!browserSession.isSuspended) {
                    browserSession.onDomainSessionClosed();
                }
            }
        }


    }

    /**
     * Resumes a browser web session to allow it to receive & listen for domain session
     * state updates.
     *
     * @param browserSession The browser web session to be resumed.
     */
    public void resumeBrowserWebSession(TutorBrowserWebSession browserSession) {

        browserSession.resumeDomainSessionListening();

        DomainWebState webState = getDomainWebState();

        if (webState != null) {
            webState.resumeBrowserSession(browserSession);
        }
    }

    @Override
    protected void onSessionStopped() {
        // do nothing.

    }

    /**
     * A timer task that will end all user, browser, and domain sessions belonging to the experiment subject that owns this browser session. If this
     * browser session is not owned by an experiment subject, then no logic will be executed
     *
     * @author nroberts
     */
    private class EndSessionTask implements Runnable{

        @Override
        public void run() {

            final String userInfo;
            if(getUserSessionInfo() != null && getUserSessionInfo().getExperimentId() != null){
                userInfo = "subject " + getUserSessionInfo().getUserId()
                        + " in experiment " + getUserSessionInfo().getExperimentId();
            } else if (getUserSessionInfo() != null && getUserSessionInfo().getUsername() != null) {
                userInfo = "user " + getLmsUsername();
            } else {
                userInfo = "user with id " + getUserId();
            }

            /*
             * If a browser session running for a subject has not responded within a certain amount of time,
             * we want to end that subject's domain session and log out the subject, since its sessions will never again be used for anything
             * if the subject has closed their browser.
             */
            if(logger.isInfoEnabled()){
                logger.info("Ending user, browser, and domain sessions for " + userInfo);
            }

            if(getDomainWebState() != null){

                final Object lock = this;

                final DomainWebState webState = getDomainWebState();

                TutorModule.getInstance().endDomainSession(getUserSessionInfo(), new AsyncResponseCallback() {

                    @Override
                    public void notify(boolean success, String response, String additionalInformation) {

                        try{
                            if (success) {

                                //once the domain session is closed, resolve any listeners waiting for widgets to be displayed
                                webState.closeWidgetListeners();

                                synchronized(browserSessions) {
                                    for (TutorBrowserWebSession browserSession : browserSessions) {
                                        browserSession.resumeDomainSessionListening();
                                    }
                                }

                            }

                        } catch(Exception e){
                            logger.warn("An exception occurred while attempting to clean up the domain session for " + userInfo, e);

                        } finally{
                            synchronized (lock) {
                                lock.notify();
                            }
                        }
                    }
                });

                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (@SuppressWarnings("unused") InterruptedException e) {
                    }
                }
            }

            //iterate over the browser sessions using a copy of the list to avoid issues with synchronization locks as sessions are closed
            Set<TutorBrowserWebSession> browserSessionsToEnd = new HashSet<>(browserSessions);

            for (TutorBrowserWebSession browserSession : browserSessionsToEnd) {
                logoutUser(browserSession);
                browserSession.timeoutDomainSessionReturnBlocker();
            }

            if(logger.isInfoEnabled()){
                logger.info(userInfo + " browser web session socket was closed, therefore the domain session was shut down.");
            }
        }
    }

    /**
     * Cancel the end session task even but not if it is currently running.
     */
    public void cancelEndSessionTimeoutTask() {
        if (logger.isDebugEnabled()) {
            logger.debug("cleanupEndSessionTimer()");
        }

        if (endSessionTaskHandle != null) {
            endSessionTaskHandle.cancel(false);
            endSessionTaskHandle = null;
        }
    }

    /**
     * Schedule a task that is responsible for ending the domain session after the specified amount
     * of time has elapsed.
     *
     * @param delayMs amount of time in milliseconds before the end session task will execute.
     */
    public void startEndSessionTimer(int delayMs) {
        endSessionTaskHandle = endSessionScheduler.schedule(new EndSessionTask(), delayMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Called when the TutorModule receives a message with the MessageType INIT_EMBEDDED_CONNECTIONS.
     * It informs the correct client side BrowserSession that it will need to display an embedded
     * training application as well as telling it where to navigate its iframe to view the
     * embedded application.
     * @param msg The msg that the TutorModule received. Payload should be of
     * type InitializeEmbeddedConnections
     */
    public void handleInitEmbeddedConnections(Message msg) {

        // TODO - Do we need to check to see if the domain session exists here?
        InitializeEmbeddedConnections initEmbeddedConnections = (InitializeEmbeddedConnections) msg.getPayload();
        AbstractAction action = new InitTrainingAppAction(initEmbeddedConnections.getUrls());
        // TODO - fix me later
        handleServerAction(action);
        //Replies to the domain with a response informing it that the
        //tutor module can receive the embedded applications feedback
        //requests
        InteropConnectionsInfo iConnInfo = new InteropConnectionsInfo();
        iConnInfo.addSupportedMessageType(MessageTypeEnum.DISPLAY_FEEDBACK_GATEWAY_REQUEST);
        TutorModule.getInstance().sendReply(msg, iConnInfo, MessageTypeEnum.INTEROP_CONNECTIONS_INFO);
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
    public void handleSimanMessage(Message msg) {

        DomainWebState webState = getDomainWebState();

        if (webState != null) {

            // Convert the message into an action.
            String appMessage;
            try {

                if(msg.getPayload() instanceof Siman && SimanTypeEnum.LOAD.equals(((Siman) msg.getPayload()).getSimanTypeEnum())){

                    //clean up leftover states previous lessons involving embedded applications
                    setEmbeddedApplicationReceivedSimanStop(false);
                    setEmbeddedApplicationSentStopFreeze(false);
                }

                appMessage = EmbeddedAppMessageEncoder.encodeForEmbeddedApplication(msg.getPayload()).toJSONString();

                AbstractAction action = new TrainingAppMessageAction(appMessage);


                enqueueMessagePendingReply(msg);

                /* If the embedded application has already received a SimanStop,
                 * it should not be receiving any additional Siman messages
                 */
                if(getEmbeddedApplicationReceivedSimanStop()) {
                    return;
                }

                handleServerAction(action);

                if(((Siman) msg.getPayload()).getSimanTypeEnum() == SimanTypeEnum.STOP) {
                    //If the message is a Siman stop indicate that the shutdown sequence has begun
                    setEmbeddedApplicationReceivedSimanStop(true);

                    /* Give the embedded application a chance to handle the SimanStop,
                     * otherwise move on. */
                    Timer timer = new Timer("StopEmbedded-"+userSessionInfo.getUserId());
                    timer.schedule(new TimerTask() {

                        @Override
                        public void run() {
                            for(Message pending = dequeueMessagePendingReply(); pending != null; pending = dequeueMessagePendingReply()) {
                                TutorModule.getInstance().sendReply(pending, new ACK(), MessageTypeEnum.PROCESSED_ACK);
                            }
                        }

                    }, DEFAULT_SIMAN_STOP_TIMEOUT);
                }
            } catch(ParseException parseEx) {
                logger.error("There was a problem parsing the message from the domain module. msg = " + msg, parseEx);
                NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "The TutorModule had a problem parsing the Siman message from the domain module");
                nack.setErrorHelp("If you have access to the Tutor module server log, it will contain additional details on the error.  Otherwise contact the GIFT administrator for additional help.");
                TutorModule.getInstance().sendReply(msg, nack, MessageTypeEnum.PROCESSED_NACK);
            } catch(Exception e) {
                logger.error("The TutorModule had a problem handling the Siman message from the domain module", e);
                NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "The TutorModule had a problem handling the Siman message from the domain module");
                nack.setErrorHelp("If you have access to the Tutor module server log, it will contain additional details on the error.  Otherwise contact the GIFT administrator for additional help.");
                TutorModule.getInstance().sendReply(msg, nack, MessageTypeEnum.PROCESSED_NACK);
            }

        } else {
            logger.error("Unable to find a domainWebState for user: " + this);
            TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.DOMAIN_SESSION_NOT_FOUND_ERROR, "Not an active domain session for the specified domain session."), MessageTypeEnum.PROCESSED_NACK);
        }

    }

    /**
     * Handles a request, from the domain module, to display feedback within the embedded
     * training application. Msg is expected to have a payload of type string.
     * @param msg The message from the domain module.
     */
    public void handleTrainingAppFeedbackRequest(Message msg) {

        try {
            String appMessage = EmbeddedAppMessageEncoder.encodeForEmbeddedApplication(msg).toJSONString();


            DomainWebState webState = getDomainWebState();

            if (webState != null) {

                //If the shutdown process has begun then feedback should not be presented
                if(getEmbeddedApplicationSentStopFreeze() || getEmbeddedApplicationReceivedSimanStop()) {
                    return;
                }

                //Transmits the JSON string to the client side BrowserSession
                AbstractAction action = new TrainingAppMessageAction(appMessage);
                handleServerAction(action);

            } else {
                logger.error("Unable to find a domainWebState for user: " + this);
                TutorModule.getInstance().sendReply(msg, new NACK(ErrorEnum.DOMAIN_SESSION_NOT_FOUND_ERROR, "Not an active domain session for the specified domain session."), MessageTypeEnum.PROCESSED_NACK);
            }

        } catch(ParseException parseEx) {
            logger.error("There was a problem parsing the message from the domain module. msg = " + msg, parseEx);
            NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "The TutorModule had a problem parsing the request for feedback in the training application from the domain module");
            nack.setErrorHelp("If you have access to the Tutor module server log, it will contain additional details on the error.  Otherwise contact the GIFT administrator for additional help.");
            TutorModule.getInstance().sendReply(msg, nack, MessageTypeEnum.PROCESSED_NACK);
        } catch(Exception e) {
            logger.error("The TutorModule had a problem handling the request to display feedback in the training application from the domain module", e);
            NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "The TutorModule had a problem handling the request to display feedback in the training application from the domain module");
            nack.setErrorHelp("If you have access to the Tutor module server log, it will contain additional details on the error.  Otherwise contact the GIFT administrator for additional help.");
            TutorModule.getInstance().sendReply(msg, nack, MessageTypeEnum.PROCESSED_NACK);
        }


    }

    /**
     * Sends an app state from the specified BrowserSession to the domain module.
     * Messages will be sent through the tutor topic unless the message is an ACK
     * to a previously received message in which case it will be sent via the sendReply
     * method of the AbstractModule class.
     * @param message The JSON string to decode into a GIFT Message and send to the
     */
    public void sendMessageFromEmbeddedApplication(String message) {

        // Process the incoming message from the embedded application.
        // This typically will send the message from the Tutor server into the Domain module
        // via ActiveMQ.
        handler.processIncomingEmbeddedAppMessage(this, message);

    }

    /**
     * Broadcasts a message to all web browsers connected to the session.  This does not go through
     * the normal action flow of the Tutor where the domain state is updated.  This simply broadcasts
     * a message to each browser.  Only supported actions are allowed to be broadcast to the webbrowsers
     * and explicit support should be added when needed.
     *
     * @param action The message to send to the browsers.
     */
    public void broadcastMessageToBrowsers(AbstractAction action) {

        // Only allow explicit messages for now.  Most other callers likely should use the normal handlers
        // where the domain web state is updated for each action.
        if (!(action instanceof KnowledgeSessionsUpdated)) {
            logger.error("Trying to broadcast an unsupported message to the web browsers.  Use the normal handlers which update domain state "
                    + "instead, or add support for the message in this method.");

            return;
        }

        synchronized(this.browserSessions) {
            for (TutorBrowserWebSession session : browserSessions) {
                session.sendWebSocketMessage(action);
            }
        }

    }


}
