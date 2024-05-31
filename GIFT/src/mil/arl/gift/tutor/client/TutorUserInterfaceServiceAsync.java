/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import generated.course.LtiProperties;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.SessionMember;
import mil.arl.gift.common.gwt.client.DetailedRpcResponse;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.shared.AbstractCourseLaunchParameters;
import mil.arl.gift.common.gwt.shared.GetActiveKnowledgeSessionsResponse;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.survey.SurveyGiftData;
import mil.arl.gift.tutor.shared.ClientProperties;
import mil.arl.gift.tutor.shared.data.AbstractKnowledgeSessionResponse;
import mil.arl.gift.tutor.shared.data.CourseListResponse;

/**
 * The async counterpart of @TutorUserInferfaceService
 *
 * @author jleonard
 */
public interface TutorUserInterfaceServiceAsync {
    	
    /**
     * Return the server properties that have been identified as important for the 
     * tutor client.
     * 
     * @param callback 
     */
    void getServerProperties(AsyncCallback<ServerProperties> callback);

    /**
     * For determining if a user session is valid and active
     *
     * @param userSessionKey The user session key
     * @param callback Callback for if the user session is valid
     * @throws IllegalArgumentException
     */
    void isValidUserSessionKey(String userSessionKey, AsyncCallback<Boolean> callback)
            throws IllegalArgumentException;

    /**
     * For determining if a browser session is valid and active
     *
     * @param browserSessionKey The browser session key
     * @param callback Callback for if the browser session is valid
     * @throws IllegalArgumentException
     */
    void isValidBrowserSessionKey(String browserSessionKey, AsyncCallback<Boolean> callback)
            throws IllegalArgumentException;

    /**
     * Creates a new browser session for the given user session
     *
     * @param userSessionKey The user session key
     * @param client Information about this client that will be used to handle the browser session
     * @param callback Callback for the created browser session
     * @throws IllegalArgumentException
     */
    void createBrowserSession(String userSessionKey, ClientProperties client, AsyncCallback<RpcResponse> callback)
            throws IllegalArgumentException;


    /**
     * Create a new user
     *
     * @param isMale Is the new user male
     * @param lmsUsername The new user's LMS username
     * @param client Information about this client that will be used to handle the browser session
     * @param callback Callback for if the action was successful or not
     * @throws IllegalArgumentException
     */
    void createNewUser(boolean isMale, String lmsUsername, ClientProperties client, AsyncCallback<RpcResponse> callback)
            throws IllegalArgumentException;
    
    /**
     * Login a user
     *
     * @param username The username of the user to login
     * @param password the password of the user
     * @param loginAsUserName used to assume the identify of another GIFT user for debugging purposes.  In most cases this is null.
     * @param client Information about this client that will be used to handle the browser session
     * @param callback Callback for if the action was successful or not
     * @throws IllegalArgumentException
     */
    void userLogin(String username, String password, String loginAsUserName, ClientProperties client, AsyncCallback<RpcResponse> callback)
            throws IllegalArgumentException;

    /**
     * Login a user
     *
     * @param userId The user ID of the user to login
     * @param client Information about this client that will be used to handle the browser session
     * @param callback Callback for if the action was successful or not
     * @throws IllegalArgumentException
     */
    void userLogin(int userId, ClientProperties client, AsyncCallback<RpcResponse> callback)
            throws IllegalArgumentException;

    /**
     * Logout a user associated with a browser session
     *
     * @param browserSessionKey The browser session key
     * @param callback Callback for if the action was successful or not
     * @throws IllegalArgumentException
     */
    void userLogout(String browserSessionKey, AsyncCallback<RpcResponse> callback)
            throws IllegalArgumentException;

    /**
     * Start a domain session for a user associated with a browser session
     *
     * @param browserSessionKey The browser session key
     * @param domainRuntimeId the selected domain runtime id which is the path to the course used when taking the course
     * @param domainSourceId the selected domain source id which is the path to the authored course
     * @param callback Callback for if the action was successful or not
     * @throws IllegalArgumentException
     */
    void userStartDomainSession(String browserSessionKey, String domainRuntimeId, String domainSourceId, AsyncCallback<RpcResponse> callback)
            throws IllegalArgumentException;

    /**
     * Resume getting updates for an active domain session
     *
     * @param browserSessionKey The browser session key
     * @param callback Callback for if the action was successful or not
     * @throws IllegalArgumentException
     */
    void userResumeDomainSessionUpdates(String browserSessionKey, AsyncCallback<RpcResponse> callback)
            throws IllegalArgumentException;

    /**
     * Pause getting updates for an active domain session
     *
     * @param browserSessionKey The browser session key
     * @param callback Callback for if the action was successful or not
     * @throws IllegalArgumentException
     */
    void userPauseDomainSessionUpdates(String browserSessionKey, AsyncCallback<RpcResponse> callback)
            throws IllegalArgumentException;

    /**
     * Ends the active domain session
     *
     * @param browserSessionKey The browser session key
     * @param callback Callback for if the action was successful or not
     * @throws IllegalArgumentException
     */
    void userEndDomainSession(String browserSessionKey, AsyncCallback<RpcResponse> callback)
            throws IllegalArgumentException;

    /**
     * Gets the name of the active domain session associated with a browser
     * session
     *
     * @param browserSessionKey The browser session key
     * @param callback Callback for the active domain name for a user
     * @throws IllegalArgumentException
     */
    void getActiveDomainSessionName(String browserSessionKey, AsyncCallback<String> callback)
            throws IllegalArgumentException;

    /**
     * Get the domain options of a user associated with a browser session
     *
     * @param browserSessionKey The browser session key
     * @param callback Callback for the domain options for a user
     * @throws IllegalArgumentException
     */
    void getDomainOptions(String browserSessionKey, AsyncCallback<CourseListResponse> callback)
            throws IllegalArgumentException;
    
    /**
     * Ends the domain session for a user and logs the user out.
     *
     * @param browserSessionKey The browser session key
     @param callback used to notify the caller of the request's response
     * @return RpcResponse If the action was successful or not
     * @throws IllegalArgumentException
     */
    void userEndDomainSessionAndLogout(String browserSessionKey,  AsyncCallback<RpcResponse> callback)
            throws IllegalArgumentException;

    /**
     * Used to login in user and start a course in offline mode.  This is useful for things like the GIFT dashboard
     * that want to bypass the course selection page in the TUI.
     * 
     * @param username the GIFT user name to login
     * @param userId the unique id of the GIFT user to login
     * @param domainRuntimeId the selected domain runtime id which is the path to the course used when taking the course
     * @param domainSourceId the selected domain source id which is the path to the authored course
     * @param client Information about this client that will be used to handle the browser session
     * @param callback used to notify the caller of the request's response
     * @throws IllegalArgumentException
     */
    void offlineUserLoginAndSelectDomain(String username, int userId, String courseRuntimeId, String courseSourceId, 
            ClientProperties client, AsyncCallback<RpcResponse> asyncCallback) throws IllegalArgumentException;
    
    /**
     * Used to login in an lti Tool Consumer user. 
     * 
     * @param consumerKey The consumer key for the lti user launching the course.
     * @param consumerId The consumer id for the lti user launching the course. 
     * @param dataSetId (optional) The data collection data set id that can be specified during the lti launch sequence.
     * @param client Information about this client that will be used to handle the browser session
     * @param callback used to notify the caller of the request's response
     * @throws IllegalArgumentException
     */
    void ltiUserLogin(String consumerKey, String consumerId, String dataSetId, ClientProperties client, AsyncCallback<RpcResponse> asyncCallback) throws IllegalArgumentException;
    


    /**
     * Used to start a course.  This is an asynchronous request.  The caller must call this first, then
     * call the checkStartCourseStatus request to get the completed result of the request. 
     * This is useful for things like the GIFT dashboard that want to bypass the course selection page in the TUI.
     * 
     * @param browserSessionId The browser session key
     * @param domainRuntimeId the selected domain runtime id which is the path to the course used when taking the course
     * @param domainSourceId the selected domain source id which is the path to the authored course
     * @param params Additional parameters that may be needed to start the course or provide runtime information to the course.
     * @param asyncCallback used to notify the caller of the request's response
     * @throws DetailedException If an unexpected error occurs while starting the course 
     */
    void startCourse(String browserSessionId, String domainRuntimeId, String domainSourceId, AbstractCourseLaunchParameters params,
            AsyncCallback<RpcResponse> asyncCallback) throws DetailedException;
	
	
	/**
	 * Checks the status of a startCourse request.  The result will be 'null' until the server has completed.
	 * Once the server has completed, the response will be non-null and will contain either a success or failure
	 * in the result.
	 * 
	 * @param browserSessionId The browser session key
     * @param domainRuntimeId the selected domain runtime id which is the path to the course used when taking the course
     * @param domainSourceId the selected domain source id which is the path to the authored course
     * @param asyncCallback used to notify the caller of the request's response
	 * @throws DetailedException
	 */
	void checkStartCourseStatus(String browserSessionId, String domainRuntimeId, String domainSourceId,
            AsyncCallback<RpcResponse> asyncCallback) throws DetailedException;
	
	/**
     * Detects whether or not a resource is reachable from the given URL
     * 
     * @param url the resource's URL
     * @param callback a callback invoked once it has been determined whether or not the resource is reachable
     */
	void isUrlResourceReachable(String url, AsyncCallback<RpcResponse> callback);

	/**
	 * Begins the course belonging to the experiment with the specified ID. 
	 * 
	 * @param experimentId the ID of the experiment whose course should be started
	 * @param experimentFolder the folder of the experiment course relative to
     *        the runtime experiment folder.
	 * @param client Information about this client that will be used to handle the browser session
	 * @param asyncCallback Callback for if the action was successful or not
	 */
	void startExperimentCourse(String experimentId, String experimentFolder, ClientProperties client, AsyncCallback<RpcResponse> asyncCallback) throws IllegalArgumentException;
	
	/**
	 * Checks to see if there is an existing session for the given username.
	 * 
	 * @param username The username to get an existing session for
	 */
	void getExistingUserSession(String username, AsyncCallback<RpcResponse> asyncCallback);
	
	/**
	 * Sets the status of the feedback widget on the server. Used to determine whether 
	 * or not feedback updates will be queued or pushed directly to the client.
	 * 
	 * @param browserSessionId The browser session key
	 * @param isActive Whether or not the feedback widget is active.
	 * @param callback The callback to execute after the status update.
	 */
	void setFeedbackWidgetIsActive(String browserSessionId, boolean isActive, AsyncCallback<RpcResponse> asyncCallback);
	
	/**
	 * Sets the id of the active conversation widget on the server. Used to determine whether 
	 * or not chat updates will be queued or pushed directly to the client.
	 * 
	 * @param browserSessionId The browser session key
	 * @param isActive The id of the active conversation widget
	 * @param callback The callback to execute after the status update.
	 */
	void setActiveConversationWidget(String browserSessionId, int chatId, AsyncCallback<RpcResponse> asyncCallback);
	
	/**
	 * Sets the status of the conversation widget on the server to inactive. Used to determine 
	 * whether or not chat updates will be queued or pushed directly to the client.
	 * 
	 * @param inactiveChatId The unique id of the conversation (created by the server) that the client would like to notify
     * the server about being idle.  An inactive conversation means that the server should NOT deliver updates to
     * the chat until it becomes active again.  The client would need to notify the server if the conversation
     * becomes active again.
	 * @param browserSessionId The browser session key
	 * @param callback The callback to execute after the status update.
	 */
	void setInactiveConversationWidget(int inactiveChatId, String browserSessionId, AsyncCallback<RpcResponse> asyncCallback);
	
	/**
	 * Dequeues and handles an update from the chat request queue
	 * 
	 * @param browserSessionId The browser session key
	 * @param callback The callback to execute once the update is handled
	 */
	void dequeueChatWidgetUpdate(String browserSessionId, AsyncCallback<RpcResponse> asyncCallback);
	
	/**
	 * Dequeues and handles an update from the feedback queue
	 * 
	 * @param browserSessionId The browser session key
	 * @param callback The callback to execute once the update is handled.  The response success value will be true
	 * when a feedback was sent to the client as a result of this request to dequeue.  False is an indication
	 * that the feedback is most likely queued because the server considers the avatar to be idle.
	 */
	void dequeueFeedbackWidgetUpdate(String browserSessionId, AsyncCallback<RpcResponse> asyncCallback);
	
	/**
	 * Notifies the server that the tutor actions widget is open on the client.
	 * 
	 * @param browserSessionId The browser session key
	 * @param isAvailable True if the tutor actions widget is open on the client, false otherwise.
	 */
	void setTutorActionsAvailable(String browserSessionId, boolean isAvailable, AsyncCallback<RpcResponse> asyncCallback);
	
    /**
     * Determines whether or not the avatar is idle
     *
     * @param userSessionKey The user session key
     * @param asyncCallback The callback to execute after the avatar status is retrieved
     */
    void isAvatarIdle(String userSessionKey, AsyncCallback<Boolean> asyncCallback);
    
    void sendEmbeddedAppState(String message, String browserSessionKey, AsyncCallback<RpcResponse> asyncCallback);
    
    /**
     * Makes the tutor module request the specified survey and return it
     * @param browserSessionKey the identifier for the browser session
     * @param surveyContextId the id for the survey context to fetch the survey from
     * @param surveyKey the identifier for the survey within the specified context to request
     * @param asyncCallback the callback to handle the completion of the survey
     */
    void getSurvey(String browserSessionKey, int surveyContextId, String giftKey, AsyncCallback<SurveyGiftData> asyncCallback);
    
    /**
     * Builds the encrypted OAuth URL that will be used to send the request to the LTI provider.
     * 
     * @param rawUrl the raw media url before it has been protected by OAuth.
     * @param mediaTypeProperties The MediaTypeProperties associated with the content.
     * @param browserSessionKey the browser session key.
     * @param callback the callback used to handle the response or catch any failures.
     */
    void buildOAuthLtiUrl(String rawUrl, LtiProperties properties, String browserSessionKey, AsyncCallback<RpcResponse> callback);

    /**
     * Notifies the Domain that the learner wants to manually stop the current training application scenario
     * 
     * @param browserSessionKey the identifier for the browser session
     * @param the callback that will handle a result indicating whether or not the operation was successful
     */
    void stopTrainingAppScenario(String browserSessionKey, AsyncCallback<GenericRpcResponse<Void>> callback);
    
    /**
     * Retrieves the active {@link AbstractKnowledgeSession knowledge sessions}.
     * 
     * @param browserSessionKey the browser id that is making the request
     * @param callback the callback returning the list of active knowledge sessions.
     */
    void fetchActiveKnowledgeSessions(String browserSessionKey, AsyncCallback<GetActiveKnowledgeSessionsResponse> callback);
    
    /**
     * Requests to host a team session.  If successful, the data for the hosted session is returned.
     * 
     * @param browserSessionKey the browser id that is making the request
     * @param callback the callback returning the containing the knowledge session of the host (if successful).
     */
    void hostTeamSessionRequest(String browserSessionKey, AsyncCallback<AbstractKnowledgeSessionResponse> callback);
    
    /**
     * Requests to leave a team session.  If successful, the complete list of active knowledge sessions is returned.
     * The list can be empty if there are no active knowledge sessions.
     * 
     * @param browserSessionKey the browser id that is making the request
     * @param sessionName The name of the session to leave
     * @param hostDomainId The domain session id of the host
     * @param hostUserName The username of the host
     * @param callback the callback returning the list of active knowledge sessions.
     */
    void leaveTeamSessionRequest(String browserSessionKey,  String sessionName, int hostDomainId, String hostUserName, AsyncCallback<GetActiveKnowledgeSessionsResponse> callback);
    
    /**
     * Requests to kick a user from a team session.  If successful, the complete list of active knowledge sessions is returned.
     * The list can be empty if there are no active knowledge sessions.
     * 
     * @param browserSessionKey the browser id that is making the request
     * @param hostDomainId The domain session id of the host
     * @param userToKick The user to kick
     * @param callback the callback returning the list of active knowledge sessions.
     */
    void kickFromTeamSessionRequest(String browserSessionKey, int hostDomainId, SessionMember userToKick, AsyncCallback<GetActiveKnowledgeSessionsResponse> callback);
    
    /**
     * Requests to change a team session name.  If successful, the complete list of active knowledge sessions is returned.
     * The list can be empty if there are no active knowledge sessions.
     * 
     * @param browserSessionKey the browser id that is making the request
     * @param hostDomainId The domain session id of the host
     * @param newSessionName the new name that the session should use
     * @param callback the callback returning the list of active knowledge sessions.
     */
    void changeTeamSessionNameRequest(String browserSessionKey, int hostDomainId, String newSessionName, AsyncCallback<GetActiveKnowledgeSessionsResponse> callback);
    
    /**
     * Requests to start a team session.  If successful, all users in the session will start transitioning into the training
     * application.
     * 
     * @param browserSessionKey the browser id that is making the request
     * @param callback the callback returning the list of active knowledge sessions.
     */
    void startTeamSessionRequest(String browserSessionKey, AsyncCallback<DetailedRpcResponse> callback);

    /**
     * Requests to join a team session.  If successful, the latest information of the joined session is returned.
     * 
     * @param browserSessionKey the browser id that is making the request
     * @param selectedSession the session to try to join
     * @param asyncCallback the callback returning the list of active knowledge sessions.
     */
    void joinSessionRequest(String browserSessionKey, AbstractKnowledgeSession selectedSession,
            AsyncCallback<AbstractKnowledgeSessionResponse> asyncCallback);

    /**
     * Requests to select a team member role.  If successful, the latest list of active knowledge sessions is returned.  
     * 
     * @param browserSessionKey the browser id that is making the request
     * @param roleName The team member role to join
     * @param hostDomainId The domain session id of the host
     * @param callback the callback returning the list of active knowledge sessions.
     */
    void selectTeamMemberRole(String browserSessionKey, String roleName, int hostDomainId,
            AsyncCallback<GetActiveKnowledgeSessionsResponse> asyncCallback);
    
    /**
     * Unassigns the user from a team member role.  If successful, the latest list of active knowledge sessions is returned.
     * 
     * @param browserSessionKey the browser id that is making the request
     * @param roleName The role name to be unassigned from
     * @param hostDomainId The domain session id of the host
     * @param callback the callback returning the list of active knowledge sessions.
     */
    void unassignTeamMemberRole(String browserSessionKey, String roleName, int hostDomainId,
            AsyncCallback<GetActiveKnowledgeSessionsResponse> asyncCallback);
    
    /**
     * Fetches the current domain id of the user (if in an active domain session.)
     * 
     * @param browserSessionKey the browser id that is making the request.
     * @param callback the callback returning the domain session id
     */
    void getCurrentDomainId(String browserSessionKey, AsyncCallback<GenericRpcResponse<Integer>> asyncCallback);
}
