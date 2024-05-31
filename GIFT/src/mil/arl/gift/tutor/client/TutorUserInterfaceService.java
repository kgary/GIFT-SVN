/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

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
 * The client side stub for the TWS RPC service.
 */
@RemoteServiceRelativePath("tutoruserinterface")
public interface TutorUserInterfaceService extends RemoteService {
	
    /**
     * Return the server properties that have been identified as important for the 
     * tutor client.
     * 
     * @return the properties needed by the tutor client
     */
    ServerProperties getServerProperties();

    /**
     * For determining if a user session key is valid and active
     *
     * @param userSessionKey The user session key
     * @return Boolean If the user session ID is valid and active
     * @throws IllegalArgumentException
     */
    Boolean isValidUserSessionKey(String userSessionKey)
            throws IllegalArgumentException;

    /**
     * For determining if a browser session ID is valid and active
     *
     * @param browserSessionKey The browser session ID
     * @return Boolean If the browser session ID is valid and active
     * @throws IllegalArgumentException
     */
    Boolean isValidBrowserSessionKey(String browserSessionKey)
            throws IllegalArgumentException;

    /**
     * Creates a new browser session for the given user session
     *
     * @param userSessionKey The ID of the user session
     * @param client Information about this client that will be used to handle the browser session
     * @return RpcResponse If the action was successful or not
     * @throws IllegalArgumentException
     */
    RpcResponse createBrowserSession(String userSessionKey, ClientProperties client)
            throws IllegalArgumentException;

    /**
     * Create a new user in GIFT
     *
     * @param isMale Is the new user male
     * @param lmsUsername The new user's LMS username
     * @param client Information about this client that will be used to handle the browser session
     * @return RpcResponse If the action was successful or not
     * @throws IllegalArgumentException
     */
    RpcResponse createNewUser(boolean isMale, String lmsUsername, ClientProperties client)
            throws IllegalArgumentException;

    /**
     * Login a user
     *
     * @param userId The user ID of the user to login
     * @param client Information about this client that will be used to handle the browser session
     * @return RpcResponse If the action was successful or not
     * @throws IllegalArgumentException
     */
    RpcResponse userLogin(int userId, ClientProperties client)
            throws IllegalArgumentException;
    
    /**
     * Login a user
     *
     * @param username The username of the user to login
     * @param password the password of the user
     * @param loginAsUserName used to assume the identify of another GIFT user for debugging purposes.  In most cases this is null.
     * @param client Information about this client that will be used to handle the browser session
     * @return RpcResponse If the action was successful or not
     * @throws IllegalArgumentException
     */
    RpcResponse userLogin(String username, String password, String loginAsUserName, ClientProperties client)
            throws IllegalArgumentException;

    /**
     * Logout a user associated with a browser session
     *
     * @param browserSessionKey The browser session key
     * @return RpcResponse If the action was successful or not
     * @throws IllegalArgumentException
     */
    RpcResponse userLogout(String browserSessionKey)
            throws IllegalArgumentException;

    /**
     * Start a domain session for a user associated with a browser session
     *
     * @param browserSessionKey The browser session key
     * @param domainRuntimeId the selected domain runtime id which is the path to the course used when taking the course
     * @param domainSourceId the selected domain source id which is the path to the authored course
     * @return RpcResponse If the action was successful or not
     * @throws IllegalArgumentException
     */
    RpcResponse userStartDomainSession(String browserSessionKey, String domainRuntimeId, String domainSourceId)
            throws IllegalArgumentException;

    /**
     * Resume getting updates for an active domain session
     *
     * @param browserSessionKey The browser session key
     * @return RpcResponse If the action was successful or not
     * @throws IllegalArgumentException
     */
    RpcResponse userResumeDomainSessionUpdates(String browserSessionKey)
            throws IllegalArgumentException;

    /**
     * Pauses getting updates for an active domain session
     *
     * @param browserSessionKey The browser session key
     * @return RpcResponse If the action was successful or not
     * @throws IllegalArgumentException
     */
    RpcResponse userPauseDomainSessionUpdates(String browserSessionKey)
            throws IllegalArgumentException;

    /**
     * Ends the active domain session
     *
     * @param browserSessionKey The browser session key
     * @return RpcResponse If the action was successful or not
     * @throws IllegalArgumentException
     */
    RpcResponse userEndDomainSession(String browserSessionKey)
            throws IllegalArgumentException;

    /**
     * Gets the name of the active domain session associated with a browser
     * session
     *
     * @param browserSessionKey The browser session key
     * @return String The name of the active domain session, null if there is
     * not an active domain session
     * @throws IllegalArgumentException
     */
    String getActiveDomainSessionName(String browserSessionKey)
            throws IllegalArgumentException;

    /**
     * Get the domain options of a user associated with a browser session
     *
     * @param browserSessionKey The browser session key
     * @return ArrayList<DomainOptionData> The domain options for a user
     * @throws IllegalArgumentException
     */
    CourseListResponse getDomainOptions(String browserSessionKey)
            throws IllegalArgumentException;

    /**
     * Ends the domain session for a user and logs the user out.
     *
     * @param browserSessionKey The browser session key
     * @return RpcResponse If the action was successful or not
     * @throws IllegalArgumentException
     */
    RpcResponse userEndDomainSessionAndLogout(String browserSessionKey)
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
     * @return RpcResponse contains the request's response
     * @throws IllegalArgumentException
     */
    RpcResponse offlineUserLoginAndSelectDomain(String username, int userId, String domainRuntimeId, 
    		String domainSourceId, ClientProperties client) throws IllegalArgumentException;

    /**
     * Used to login in an lti Tool Consumer user.
     * 
     * @param consumerKey The consumer key for the lti user launching the course.
     * @param consumerId The consumer id for the lti user launching the course. 
     * @param dataSetId (optional) The data collection data set id that can be specified during the lti launch sequence.
     * @param client Information about this client that will be used to handle the browser session
     * @return RpcResponse contains the request's response
     * @throws IllegalArgumentException
     */
    RpcResponse ltiUserLogin(String consumerKey, String consumerId, String dataSetId, ClientProperties client) throws IllegalArgumentException;
    
    /**
     * Used to start a course.  This is an asynchronous request.  The caller must call this first, then
     * call the checkStartCourseStatus request to get the completed result of the request. 
     * This is useful for things like the GIFT dashboard that want to bypass the course selection page in the TUI.
     * 
     * 
     * @param browserSessionId The browser session key
     * @param domainRuntimeId the selected domain runtime id which is the path to the course used when taking the course
     * @param domainSourceId the selected domain source id which is the path to the authored course
     * @param params Additional parameters that may be needed to start the course or provide runtime information to the course.
     * @return RpcResponse contains the request's response
     * @throws DetailedException if an unexpected error occurs while starting the course.
     */
    RpcResponse startCourse(String browserSessionId, String domainRuntimeId, String domainSourceId, AbstractCourseLaunchParameters params) throws DetailedException;
	
	/**
     * Checks the status of a startCourse request.  The result will be 'null' until the server has completed.
     * Once the server has completed, the response will be non-null and will contain either a success or failure
     * in the result.
     * 
     * @param browserSessionId The browser session key
     * @param domainRuntimeId the selected domain runtime id which is the path to the course used when taking the course
     * @param domainSourceId the selected domain source id which is the path to the authored course
     * @return RpcResponse The response will be 'null' while the course is still being started.  Once the server completes
     * it will be non-null and either be a success or failure response type.
     * @throws DetailedException if an unexpected error occurs while starting the course.
     */
	RpcResponse checkStartCourseStatus(String browserSessionId, String domainRuntimeId, String domainSourceId) throws DetailedException;
	
	/**
     * Detects whether or not a resource is reachable from the given URL
     * 
     * @param url the resource's URL
	 * @return RpcResponse contains the request's response
	 * @throws IllegalArgumentException 
     */
	RpcResponse isUrlResourceReachable(String url) throws IllegalArgumentException;
	
	/**
	 * Begins the course belonging to the experiment with the specified ID. 
	 * 
	 * @param experimentId the ID of the experiment whose course should be started
	 * @param experimentFolder the folder of the experiment course relative to
     *        the runtime experiment folder.
	 * @param client Information about this client that will be used to handle the browser session
	 * @return RpcResponse contains the request's response
	 * @throws IllegalArgumentException 
	 */
	RpcResponse startExperimentCourse(String experimentId, String experimentFolder, ClientProperties client);

	/**
	 * Checks to see if there is an existing session for the given username.
	 * 
	 * @param username The username to get an existing session for
	 * @return RpcResponse contains the result of the request
	 */
	RpcResponse getExistingUserSession(String username);
	
	/**
	 * Sets the status of the feedback widget on the server. Used to determine whether 
	 * or not feedback updates will be queued or pushed directly to the client.
	 * 
	 * @param browserSessionId The browser session key
	 * @param isActive Whether or not the feedback widget is active.
	 */
	RpcResponse setFeedbackWidgetIsActive(String browserSessionId, boolean isActive);
	
	/**
	 * Sets the id of the active conversation widget on the server. Used to determine whether 
	 * or not chat updates will be queued or pushed directly to the client.
	 * 
	 * @param browserSessionId The browser session key
	 * @param isActive The id of the active conversation widget
	 */
	RpcResponse setActiveConversationWidget(String browserSessionId, int chatId);
	
	/**
	 * Sets the status of the conversation widget on the server to inactive. Used to determine 
	 * whether or not chat updates will be queued or pushed directly to the client.
	 * 
	 * @param inactiveChatId The unique id of the conversation (created by the server) that the client would like to notify
     * the server about being idle.  An inactive conversation means that the server should NOT deliver updates to
     * the chat until it becomes active again.  The client would need to notify the server if the conversation
     * becomes active again.
	 * @param browserSessionId The browser session key
	 * @return an RpcResponse containing the result of the request
	 */
	RpcResponse setInactiveConversationWidget(int inactiveChatId, String browserSessionId);
	
	/**
	 * Dequeues and handles an update from the chat request queue
	 * 
	 * @param browserSessionId The browser session key
	 * @return an RpcResponse containing the result of the request
	 */
	RpcResponse dequeueChatWidgetUpdate(String browserSessionId);
	
	/**
	 * Dequeues and handles an update from the feedback request queue
	 * 
	 * @param browserSessionId The browser session key
	 * @return an RpcResponse containing the result of the request
	 */
	RpcResponse dequeueFeedbackWidgetUpdate(String browserSessionId);
	
	/**
	 * Notifies the server that the tutor actions widget is open on the client.
	 * 
	 * @param browserSessionId The browser session key
	 * @param isAvailable True if there are tutor actions available, false otherwise.
	 * @return an RpcResponse containing the result of the request
	 */
	RpcResponse setTutorActionsAvailable(String browserSessionId, boolean isAvailable);
	
    /**
     * Determines whether or not the avatar is idle
     *
     * @param userSessionKey The user session key
     * @return Boolean True if the avatar is currently idle, false otherwise
     * @throws IllegalArgumentException If the domain session web session could not be retrieved
     */
    Boolean isAvatarIdle(String userSessionKey) throws IllegalArgumentException;
    
    RpcResponse sendEmbeddedAppState(String message, String browserSessionKey);
    
    /**
     * Makes the tutor module request the specified survey and return it
     * @param browserSessionKey the identifier for the browser session
     * @param surveyContextId the id for the survey context to fetch the survey from
     * @param surveyKey the identifier for the survey within the specified context to request
     * @return the survey and metadata about the survey that was fetched from the UMS database
     * @throws Exception If there was an error fetching the survey data with the specified parameters
     */
    SurveyGiftData getSurvey(String browserSessionKey, int surveyContextId, String giftKey) throws Exception;
    
    /**
     * Builds the encrypted OAuth URL that will be used to send the request to the LTI provider.
     * 
     * @param rawUrl the raw media url before it has been protected by OAuth.
     * @param mediaTypeProperties The MediaTypeProperties associated with the content.
     * @param browserSessionKey the browser session key
     */
    RpcResponse buildOAuthLtiUrl(String rawUrl, LtiProperties properties, String browserSessionKey);

    /**
     * Notifies the Domain that the learner wants to manually stop the current training application scenario
     * 
     * @param browserSessionKey the identifier for the browser session
     * @return a result indicating whether or not the operation was successful
     */
    GenericRpcResponse<Void> stopTrainingAppScenario(String browserSessionKey);
    
    /**
     * Retrieves the active {@link AbstractKnowledgeSession knowledge sessions}.
     * 
     * @param browserSessionKey the browser id that is making the request
     * @return the result containing the list of active knowledge sessions.
     */
    GetActiveKnowledgeSessionsResponse fetchActiveKnowledgeSessions(String browserSessionKey);
    
    /**
     * Requests to host a team session.  If successful, the data for the hosted session is returned.
     * 
     * @param browserSessionKey the browser id that is making the request
     * @return the result containing the knowledge session of the host (if successful).
     */
    AbstractKnowledgeSessionResponse hostTeamSessionRequest(String browserSessionKey);
    
    /**
     * Requests to leave a team session.  If successful, the complete list of active knowledge sessions is returned.
     * The list can be empty if there are no active knowledge sessions.
     * 
     * @param browserSessionKey the browser id that is making the request
     * @param sessionName The name of the session to leave
     * @param hostDomainId The domain session id of the host
     * @param hostUserName The username of the host
     * @return The list of active knowledge sessions.  Can be an empty list if there are no active knowledge sessions.
     */
    GetActiveKnowledgeSessionsResponse leaveTeamSessionRequest(String browserSessionKey, String sessionName, int hostDomainId, String hostUserName);
    
    /**
     * Requests to kick a user from a team session.  If successful, the complete list of active knowledge sessions is returned.
     * The list can be empty if there are no active knowledge sessions.
     * 
     * @param browserSessionKey the browser id that is making the request
     * @param hostDomainId The domain session id of the host
     * @param userToKick The user to kick
     */
    GetActiveKnowledgeSessionsResponse kickFromTeamSessionRequest(String browserSessionKey, int hostDomainId, SessionMember userToKick);
    
    /**
     * Requests to change a team session name.  If successful, the complete list of active knowledge sessions is returned.
     * The list can be empty if there are no active knowledge sessions.
     * 
     * @param browserSessionKey the browser id that is making the request
     * @param hostDomainId The domain session id of the host
     * @param newSessionName the new name that the session should use
     */
    GetActiveKnowledgeSessionsResponse changeTeamSessionNameRequest(String browserSessionKey, int hostDomainId, String newSessionName);
    
    /**
     * Requests to start a team session.  If successful, all users in the session will start transitioning into the training
     * application.
     * 
     * @param browserSessionKey the browser id that is making the request
     * @return the result of the request.
     */
    DetailedRpcResponse startTeamSessionRequest(String browserSessionKey);
    
    /**
     * Requests to join a team session.  If successful, the latest information of the joined session is returned.
     * 
     * @param browserSessionKey the browser id that is making the request
     * @param selectedSession the session to try to join
     * @param asyncCallback the callback returning the list of active knowledge sessions.
     */
    AbstractKnowledgeSessionResponse joinSessionRequest(String browserSessionKey, AbstractKnowledgeSession selectedSession);
    
    /**
     * Requests to select a team member role.  If successful, the latest list of active knowledge sessions is returned.  
     * 
     * @param browserSessionKey the browser id that is making the request
     * @param roleName The team member role to join
     * @param hostDomainId The domain session id of the host
     * @return The list of active knowledge sessions.  Can be an empty list if there are no active knowledge sessions.
     */
    GetActiveKnowledgeSessionsResponse selectTeamMemberRole(String browserSessionKey, String roleName, int hostDomainId);
    
    /**
     * Unassigns the user from a team member role.  If successful, the latest list of active knowledge sessions is returned.
     * 
     * @param browserSessionKey the browser id that is making the request
     * @param roleName The role name to be unassigned from
     * @param hostDomainId The domain session id of the host
     * @return The list of active knowledge sessions.  Can be an empty list if there are no active knowledge sessions.
     */
    GetActiveKnowledgeSessionsResponse unassignTeamMemberRole(String browserSessionKey, String roleName, int hostDomainId);
    
    /**
     * Fetches the current domain id of the user (if in an active domain session.)
     * 
     * @param browserSessionKey the browser id that is making the request.
     * @return The domain id of the user if in an active domain session.  Otherwise an error will be returned.
     */
    GenericRpcResponse<Integer> getCurrentDomainId(String browserSessionKey);
}
