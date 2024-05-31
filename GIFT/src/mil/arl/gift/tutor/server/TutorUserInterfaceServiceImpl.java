/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.server;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import generated.course.LtiProperties;
import mil.arl.gift.common.AbstractRuntimeParameters;
import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.DomainOptionsList;
import mil.arl.gift.common.FinishScenario;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.UserSessionType;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.KnowledgeSessionsRequest;
import mil.arl.gift.common.course.dkf.session.ManageTeamMembershipRequest;
import mil.arl.gift.common.course.dkf.session.SessionMember;
import mil.arl.gift.common.enums.DomainOptionRecommendationEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.gwt.client.DetailedRpcResponse;
import mil.arl.gift.common.gwt.client.FailureResponse;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.client.SuccessfulResponse;
import mil.arl.gift.common.gwt.server.AsyncResponseCallback;
import mil.arl.gift.common.gwt.server.AsyncReturnBlocker;
import mil.arl.gift.common.gwt.server.GiftServletUtils;
import mil.arl.gift.common.gwt.server.SessionStatusEnum;
import mil.arl.gift.common.gwt.server.authentication.UserAuthenticationException;
import mil.arl.gift.common.gwt.server.authentication.UserAuthenticationMgr;
import mil.arl.gift.common.gwt.server.authentication.WhiteListUserAuth;
import mil.arl.gift.common.gwt.shared.AbstractCourseLaunchParameters;
import mil.arl.gift.common.gwt.shared.GetActiveKnowledgeSessionsResponse;
import mil.arl.gift.common.gwt.shared.LtiParameters;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.ImageProperties;
import mil.arl.gift.common.lti.LtiRuntimeParameters;
import mil.arl.gift.common.lti.LtiUserRecord;
import mil.arl.gift.common.metrics.MetricsSender;
import mil.arl.gift.common.survey.SurveyGiftData;
import mil.arl.gift.common.usersession.LtiUserSessionDetails;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.api.MessageCollectionCallback;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.NACK;
import mil.arl.gift.tutor.client.TutorUserInterfaceService;
import mil.arl.gift.tutor.shared.ClientProperties;
import mil.arl.gift.tutor.shared.DialogTypeEnum;
import mil.arl.gift.tutor.shared.data.AbstractKnowledgeSessionResponse;
import mil.arl.gift.tutor.shared.data.CourseListResponse;
import mil.arl.gift.tutor.shared.data.GwtDomainOption;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class TutorUserInterfaceServiceImpl extends RemoteServiceServlet implements
        TutorUserInterfaceService {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(TutorUserInterfaceServiceImpl.class);
    
    private static final String MSC_DIRECTORY_KEY = "LocalMediaSemanticsCharacterServerDirectory";
    private static final String MSC_URL_KEY = "RemoteMediaSemanticsCharacterServerUrl";
    private static final String VHCS_ONLINE_KEY = "isVirtualHumanCharacterServerOnline";
    
    /** capture important properties that the tutor client may need */
    private static ServerProperties SERVER_PROPERTIES = new ServerProperties();
    static{
        SERVER_PROPERTIES.addProperty(ServerProperties.DEPLOYMENT_MODE, 
                TutorModuleProperties.getInstance().getDeploymentMode().getName());
        SERVER_PROPERTIES.addProperty(ServerProperties.WINDOW_TITLE, 
                TutorModuleProperties.getInstance().getWindowTitle());
        SERVER_PROPERTIES.addProperty(ServerProperties.TUI_WS_URL, 
                TutorModuleProperties.getInstance().getTutorWebSocketURL());
        SERVER_PROPERTIES.addProperty(ServerProperties.USE_HTTPS, 
        		Boolean.toString(TutorModuleProperties.getInstance().shouldUseHttps()));
        
        // IMAGES
        SERVER_PROPERTIES.addProperty(ServerProperties.BACKGROUND_IMAGE, 
                ImageProperties.getInstance().getPropertyValue(ImageProperties.BACKGROUND));
        SERVER_PROPERTIES.addProperty(ServerProperties.ORGANIZATION_IMAGE, 
                ImageProperties.getInstance().getPropertyValue(ImageProperties.ORGANIZATION_IMAGE));
        SERVER_PROPERTIES.addProperty(ServerProperties.SYSTEM_ICON_SMALL, 
                ImageProperties.getInstance().getPropertyValue(ImageProperties.SYSTEM_ICON_SMALL));
        SERVER_PROPERTIES.addProperty(ServerProperties.LOGO, 
                ImageProperties.getInstance().getPropertyValue(ImageProperties.LOGO));
        
        //logger.info("setting experiment mode to "+SERVER_PROPERTIES.getPropertyValue(TutorModuleProperties.EXPERIMENT_MODE));
    }

    /** The metrics sender is responsible for sending metrics of the tutor rpcs to the metrics server */
    private MetricsSender metrics = new MetricsSender("tutor");

    /**
     * Constructor
     */
    public TutorUserInterfaceServiceImpl() {
        
        logger.info("Creating TutorModule class instance.");
        TutorModule.getInstance();
    }
    
   
    /**
     * Helper function to retrieve an existing domain web state based on a browser session id.
     * 
     * @param browserSessionKey - Browser session id which is used to lookup if there is an existing domain session.
     * @return - DomainWebState - returns the domain web state associated with the browser session id if found.  
     *           Null is returned if it can't be found or if there was an error retrieving it.
     */
    private DomainWebState getExistingDomainWebState(String browserSessionKey) {
        
        logger.debug("getExistingDomainWebState for: " + browserSessionKey);
        
            
        TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionKey);
        if (session != null) {
            UserWebSession userSession = TutorModule.getInstance().getUserSession(session.getUserSessionKey());
            if (userSession != null) {
                DomainWebState domainSession = userSession.getDomainWebState();
                if (domainSession != null) {
                    
                    return domainSession;
                } else {
                    logger.error("getExistingDomainWebState - domainSessionWebSession is null");
                }
            } else {
                logger.error("getExistingDomainWebState - UserWebSession is null");
            }
        } else {
            logger.error("getExistingDomainWebState - BrowserWebSession is null");
        }
                      
        return null;
    }

    /**
     * Checks whether a media semantics character server is available by checking for a locally running
     * and configure correctly instance first followed by a remote server by URL.  This assumes
     * the GIFT/config/tutor/context/tutor.xml is configured correctly with media semantics information.
     * If a media semantics server URL was specified in the tutor.xml and there was an issue connecting to that URL
     * a swing dialog will be shown with a warning message about the problem.
     * 
     * @return true if a media semantics character server is found
     */
    private boolean isMediaSemanticsCharacterServerAvailable(){
     
        boolean isOnline = false;
        String characterServerDirArg = this.getServletContext().getInitParameter(MSC_DIRECTORY_KEY);
        if(characterServerDirArg != null && !characterServerDirArg.isEmpty()){                
            isOnline = true; 
        }

        if (!isOnline) {

            String serverUrl = this.getServletContext().getInitParameter(MSC_URL_KEY);
            
            if(serverUrl != null){
                //
                // Verify the URL is valid and active
                //
    
                try {
                    //looking for 'cs.exe' in the character server url
                    URL url = new URL(serverUrl + "/" + "cs.exe");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);

                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        // Got a response back from the server, it is online
                        //reference: http://en.wikipedia.org/wiki/List_of_HTTP_status_codes
                        
                        logger.info("The external character server url has been defined, avatars will be enabled.");
                        isOnline = true;

                    }else{
                        throw new Exception("Received a non-success response of "+connection.getResponseCode()+" ("+HttpURLConnection.HTTP_OK+" is a success response code) from the address of "+serverUrl);
                    }
                    
                }catch (MalformedURLException me){
                    logger.error("Caught Malformed URL Exception while validating Character Server URL of "+serverUrl+".  The most common fix is to add a URL scheme (e.g. http://) to the beginning of the address.", me);
                    
                    JOptionPane.showMessageDialog(null, "The character server URL of "+serverUrl+" is not valid (check the tutor log for more details).\n" +
                            "The most common fix is to add a URL scheme (e.g. http://) to the beginning of the address.\n" +
                            "You can either continue on and the avatar will be disabled for the execution of this Tutor module\n" +
                            "                                              OR\n" +
                            "Fix the issue and then restart the Tutor module.", "Problem configuring Tutor Avatar", JOptionPane.ERROR_MESSAGE);
                    
                } catch (Exception ex) {
                    logger.error("Caught exception while trying to check the character server URL of "+serverUrl+".", ex);
                    
                    JOptionPane.showMessageDialog(null, "The character server URL of "+serverUrl+" is causing an exception.\n" +
                            "Check the tutor log for more information.\n" +
                            "You can either continue on and the avatar will be disabled for the execution of this Tutor module\n" +
                            "                                              OR\n" +
                            "Fix the issue and then restart the Tutor module.", "Problem configuring Tutor Avatar", JOptionPane.ERROR_MESSAGE);
                }
               
            }
        }//end if
        
        return isOnline;
    }
    
    /**
     * Checks whether the virtual human character server is running and reachable.
     * This assumes the GIFT/config/tutor/context/tutor.xml is configured correctly with virtual human
     * character server information.
     * 
     * @return true if the virtual human character server is running and reachable.
     */
    private boolean isVirtualHumanCharacterServerAvailable(){
     
        boolean isOnline = false;
        
        String vhcsOnline = this.getServletContext().getInitParameter(VHCS_ONLINE_KEY);
        if(vhcsOnline != null && !vhcsOnline.isEmpty()){
            isOnline = Boolean.valueOf(vhcsOnline);
        }
        
        if(logger.isDebugEnabled()){
            logger.debug("Tutor module virtual human character server check: online result = "+vhcsOnline+", returning = "+isOnline);
        }
        return isOnline;
    }
    
    @Override
    public void init() throws ServletException {
        super.init();
        
        // Start sending metrics.
        metrics.startSending();

        //
        // Check if a character server is available
        //
        boolean isCharacterServerOnline = isVirtualHumanCharacterServerAvailable() || isMediaSemanticsCharacterServerAvailable();        
        if(logger.isDebugEnabled()){
            logger.debug("Character server online = "+isCharacterServerOnline);
        }
        CharacterServerService.getInstance().setIsOnline(isCharacterServerOnline);
        
        // Create the instance of the course launch manager.
        CourseLaunchManager.getInstance();

    }
    
    @Override
    public void destroy() {
        
        // Stop sending metrics.
        metrics.stopSending();
        super.destroy();
        
        
    }

    @Override
    public Boolean isValidUserSessionKey(String userSessionKey) throws IllegalArgumentException {
        long start = System.currentTimeMillis();
        Boolean isValid = false;
        UserWebSession session = TutorModule.getInstance().getUserSession(userSessionKey);
        if (session != null && session.getSessionStatus() == SessionStatusEnum.RUNNING) {
            
            isValid = true;
        }else{
        	logger.warn("Error in isValidUserSessionKey: " + (session == null ? "Session is null" : "session status is " + session.getSessionStatus()
        			+ " instead of " + SessionStatusEnum.RUNNING + " as expected for user session key " + userSessionKey + "."));
        }
        
        metrics.endTrackingRpc("isValidUserSessionKey", start);
        return isValid;
    }

    @Override
    public Boolean isValidBrowserSessionKey(String browserSessionKey) throws IllegalArgumentException {
        long start = System.currentTimeMillis();
        Boolean isValid = false;
        TutorBrowserWebSession browserSession = TutorModule.getInstance().getBrowserSession(browserSessionKey);
        if (browserSession != null && browserSession.getSessionStatus() == SessionStatusEnum.RUNNING) {
            isValid = true;
        }else{
        	logger.warn("Error in isValidBrowserSessionKey: " + (browserSession == null ? "Session is null" : "session status is " + browserSession.getSessionStatus()
        			+ " instead of " + SessionStatusEnum.RUNNING + " as expected for browser session key " + browserSessionKey + "."));
        }
        metrics.endTrackingRpc("isValidBrowserSessionKey", start);
        return isValid;
    }
    
    @Override
    public ServerProperties getServerProperties(){
        long start = System.currentTimeMillis();
        
        ServletRequest request = getThreadLocalRequest();
        
        //update tutor module properties, then update maintenance message
        TutorModuleProperties.getInstance().refresh();
        SERVER_PROPERTIES.addProperty(TutorModuleProperties.DEPLOYMENT_MODE, TutorModuleProperties.getInstance().getDeploymentMode().toString());
        SERVER_PROPERTIES.addProperty(TutorModuleProperties.LANDINGPAGE_MESSAGE, TutorModuleProperties.getInstance().getLandingPageMessage());
        SERVER_PROPERTIES.addProperty(ServerProperties.TUI_WS_URL, TutorModuleProperties.getInstance().getTutorWebSocketURL());
        SERVER_PROPERTIES.addProperty(ServerProperties.RESTRICTED_USER_ACCESS, String.valueOf(WhiteListUserAuth.getInstance().isEnabled(request)));
        SERVER_PROPERTIES.addProperty(ServerProperties.USE_HTTPS, Boolean.toString(TutorModuleProperties.getInstance().shouldUseHttps()));
        metrics.endTrackingRpc("getServerProperties", start);
        
        if(UserAuthenticationMgr.getInstance().isSSOSupported(request)) {
            SERVER_PROPERTIES.addProperty(ServerProperties.USE_SSO_LOGIN, String.valueOf(true));
        }
        
        ServerProperties properties = new ServerProperties(SERVER_PROPERTIES);
        properties.addProperty(ServerProperties.WEB_SOCKET_ID, UUID.randomUUID().toString());
        
        return properties;
    }

    @Override
    public RpcResponse createBrowserSession(String userSessionId, ClientProperties client) throws IllegalArgumentException {
    	long start = System.currentTimeMillis();
    	RpcResponse response = null;
    	try{
	        UserWebSession userSession = TutorModule.getInstance().getUserSession(userSessionId);
	        if (userSession != null && userSession.getSessionStatus() == SessionStatusEnum.RUNNING) {
	            
	            final String clientAddress = GiftServletUtils.getWebClientAddress(getThreadLocalRequest());
	            TutorBrowserWebSession browserSession = TutorModule.getInstance().createBrowserSession(userSession, clientAddress, client);
	            response = new RpcResponse(userSession.getUserSessionKey(), browserSession.getBrowserSessionKey(), true, null);
	            
	        } 
    	} catch(IllegalArgumentException e){
        	logger.error("Exception caught in createBrowserSession with the message: " + e);
            response = new RpcResponse(null, null, false, 
            		"Failed to create browser session after logging in due to the following error:\n" + e);
        }
        metrics.endTrackingRpc("createBrowserSession", start);
        
        return response;
    }

    @Override
    public RpcResponse createNewUser(boolean isMale, final String lmsUsername, final ClientProperties client) throws IllegalArgumentException {

        long start = System.currentTimeMillis();
        final RpcResponse createUserResponse = new RpcResponse(new String(), new String(), true, null);

        try{
	        
	        final String clientAddress = GiftServletUtils.getWebClientAddress(getThreadLocalRequest());
	        
	        UserWebSession.createUser(isMale, lmsUsername, new CreateUserAsyncResponseCallback() {
	
	            @Override
	            public void notify(UserSession user, boolean success, String response) {
	                if (success) {
	                    
	                    if (user != null) {
	                        UserWebSession userSession = TutorModule.getInstance().getUserSession(user);
    	                    if (userSession != null) {                        
    	                    	try{
	    	                        TutorBrowserWebSession browserSession = TutorModule.getInstance().createBrowserSession(userSession, clientAddress, client);
	    	                        createUserResponse.setUserSessionId(userSession.getUserSessionKey());
	    	                        createUserResponse.setBrowserSessionId(browserSession.getBrowserSessionKey());
	    	                        browserSession.displayDialog(DialogTypeEnum.INFO_DIALOG, "Create User Success", "User '" + user.getUserId() + "' has been successfully created");
    	                    	
    	                    	} catch(IllegalArgumentException e){
    	                    		logger.warn("Failed to create browser session for user " + user + ". Details: " + e);
    	                    		success = false;
    	                    		response = "Failed to create browser session after logging in due to the following error:\n" + e;
    	                    	}
    	                    } else{
    	                    	logger.warn("tried to create a new user for " + lmsUsername + ", but userSession is null");
    	                    }
	                    } else {
	                        logger.error("tried to create a new user for " + lmsUsername + ", but the user is null.");
                        }
	                }
	                createUserResponse.setIsSuccess(success);
	                createUserResponse.setResponse(response);
	                synchronized (TutorUserInterfaceServiceImpl.this) {
	                    TutorUserInterfaceServiceImpl.this.notifyAll();
	                }
	            }
	        });
	
	        synchronized (this) {
	            while (createUserResponse.getResponse() == null) {
	                try {
	                    this.wait();
	                } catch (@SuppressWarnings("unused") InterruptedException e) {
	                }
	            }
	        }
        }catch(IllegalArgumentException e){
        	logger.error("IllegalArgumentException caught in createNewUser with the message: " + e.getMessage());
        	throw e;
        }
        metrics.endTrackingRpc("createNewUser", start);

        return createUserResponse;
    }

    @Override
    public RpcResponse userLogin(final int userId, final ClientProperties client) throws IllegalArgumentException {
        long start = System.currentTimeMillis();
        final AsyncReturnBlocker<RpcResponse> returnBlocker = new AsyncReturnBlocker<RpcResponse>();
        final UserSession user = new UserSession(userId);
        try{
	        UserWebSession userSession = TutorModule.getInstance().getUserSession(user);
	        
	        final String clientAddress = GiftServletUtils.getWebClientAddress(getThreadLocalRequest());
	        
	        if (userSession != null && userSession.getSessionStatus() == SessionStatusEnum.RUNNING) {
	            
	        	// Clear saved session data since the Dashboard allows users to save
	        	userEndDomainSessionAndLogout(userSession.getBrowserSessionKey());
	        	
	        	/*
	            logger.info("Existing user session for user ID " + userId + ", creating new Browser Session");
	            final BrowserWebSession browserSession = TutorModule.getInstance().createBrowserSession(userSession, clientAddress);
	            final RpcResponse loginResponse = new RpcResponse(userSession.getUserSessionKey(), browserSession.getBrowserSessionKey(), true, "success");
	            returnBlocker.setReturnValue(loginResponse);
	            */
	        } //else {
	            
	            logger.info("No user session for user ID " + userId + ", attempting to login");
	            UserWebSession.loginUser(user, new AsyncResponseCallback() {
	
	                @Override
	                public void notify(boolean success, String response, String additionalInformation) {
	                    
	                    if (success) {
	                    	try{
		                        UserWebSession userSession = TutorModule.getInstance().getUserSession(user);
		                        final TutorBrowserWebSession browserSession = TutorModule.getInstance().createBrowserSession(userSession, clientAddress, client);
		                        RpcResponse loginResponse = new RpcResponse(userSession.getUserSessionKey(), browserSession.getBrowserSessionKey(), true, "success");
		                        returnBlocker.setReturnValue(loginResponse);
	                        
	                    	} catch(IllegalArgumentException e){
	                    		logger.warn("Failed to create browser session for user " + user + ". Details: " + e);
		                        RpcResponse loginResponse = new RpcResponse(null, null, false, 
		                        		"Failed to create browser session after logging in due to the following error:\n" + e);
		                        returnBlocker.setReturnValue(loginResponse);
	                    	}
	                    } else {
	                    	logger.warn("loginUser notify success = false, using null for userSessionId and browserSessionId and response = " + response);
	                        RpcResponse loginResponse = new RpcResponse(null, null, success, response);
	                        returnBlocker.setReturnValue(loginResponse);
	                    }
	                }
	            });
	        //}
        }catch(IllegalArgumentException e){
        	logger.error("Exception caught in userLogin with the message: " + e.getMessage());
        	throw e;
        }
        metrics.endTrackingRpc("userLogin", start);
        
        return returnBlocker.getReturnValueOrTimeout();
    }
    
    @Override
    public RpcResponse startCourse(String browserSessionId, String domainRuntimeId, String domainSourceId, AbstractCourseLaunchParameters params) throws DetailedException {
    	
        
        long start = System.currentTimeMillis();
        
        // Convert any course launch parameters into runtime parameters.  If the parameters are null, then the runtimeParams will
        // be null (which is okay since they are optional).
        AbstractRuntimeParameters runtimeParams = convertCourseLaunchParamsToRuntimeParams(params);

        // The start course 
        RpcResponse startCourseResponse = CourseLaunchManager.getInstance().startCourseAsync(browserSessionId, domainRuntimeId, domainSourceId, runtimeParams);
        
        if(startCourseResponse == null) {
            logger.error("Failed to start the course because userStartDomainSessionAsync returned a null response. "
                    + "[browserSessionId = " + browserSessionId 
                    + ", domainRuntimeId = " + domainRuntimeId 
                    + ", domainSourceId = " + domainSourceId + "]");
            
            throw new DetailedException("An error occurred while starting the course.", 
                    "Received a null response while attempting to start the domain session.", null);
        }

        metrics.endTrackingRpc("startCourse", start);
        return startCourseResponse;
    }
    
    @Override
    public RpcResponse checkStartCourseStatus(String browserSessionId, String domainRuntimeId, String domainSourceId) throws DetailedException {
        long start = System.currentTimeMillis();
        RpcResponse courseStatus = CourseLaunchManager.getInstance().getCourseLaunchStatus(browserSessionId, domainRuntimeId, domainSourceId);
        
        if (courseStatus != null) {
            if(courseStatus.isSuccess()){
                
                // We need to get the domain session and notify that the course is ready to be started.
                DomainWebState webState = getExistingDomainWebState(browserSessionId);
                if (webState != null) {
           
                    logger.debug("checkStartCourseStatus - found the domain session, signaling that course is ready to be started.");
                    
                    // If we get this far we need to tell the tui that the course is ready to be started.
                    webState.signalCourseReady();
                                
                    // This should be a success here.
                    metrics.endTrackingRpc("checkStartCourseStatus", start);
                    return courseStatus;
                } else {
                            
                    logger.warn("checkStartCourseStatus - getExistingDomainSessionWebSession returned null");
                    
                    /* When the user cancels the gateway module, the domain session is null. Unless the rpc response
                     * is successful, the user will see a server exception dialog after canceling the course and clicking
                     * continue to end the course in the TUI. In order to hide the exception, check the rpc response. 
                     * This response will describe the error if an error occurred, or will indicate that the domain session ended,
                     * which is the case when the user clicks continue to end the course.
                     */                
                    if(courseStatus.getResponse().equals(UserWebSession.DOMAIN_SESSION_ENDED)) {
                        return courseStatus;
                    } else {
                        return new RpcResponse(null, null, false, "getExistingDomainSessionWebSession returned null");
                    }
                }
                
            }else{
                //return ERROR info
                logger.warn("checkStartCourseStatus - userStartDomainSession returned success=false.  Response is: " + courseStatus);
                return courseStatus;
            }
        } else {
            return null;
        }
        
    }
    
    /**
     * Converts course launch parameters into runtime parameters.  The runtime parameters can be used to 
     * further configure the domain session when it is started.
     * 
     * @param params The course launch parameters (can be null).
     * @return AbstractRuntimeParameters the runtime parameters that were converted (can return null).
     */
    private AbstractRuntimeParameters convertCourseLaunchParamsToRuntimeParams(
            AbstractCourseLaunchParameters params) {
        
        AbstractRuntimeParameters runtimeParameters = null;
        if (params != null) {
            if (params instanceof LtiParameters) {
                LtiParameters ltiLaunchParams = (LtiParameters)params;
                
                String lisServiceUrl = ltiLaunchParams.getLisServiceUrl();
                String lisSourcedid = ltiLaunchParams.getLisSourcedid();
                String consumerKey = ltiLaunchParams.getConsumerKey();
                
                // Only if all the parameters are specified, will they be converted to runtime parameters.
                if (lisServiceUrl != null && !lisServiceUrl.isEmpty() &&
                        lisSourcedid != null && !lisSourcedid.isEmpty() && 
                        consumerKey != null && !consumerKey.isEmpty()) {
                    runtimeParameters = new LtiRuntimeParameters(consumerKey, lisServiceUrl, lisSourcedid);
                } else {
                    logger.error("The runtime parameter will be ignored. Found an lti parameter, but one or more required parameters are null: " + params);
                }
            } else {
                logger.error("The runtime parameter will be ignored.  Unsupported runtime parameter: " + params);
            }
        }

        return runtimeParameters;
    }
    
    @Override
    public RpcResponse offlineUserLoginAndSelectDomain(String username, final int userId, String domainRuntimeId, 
    		String domainSourceId, final ClientProperties client) throws IllegalArgumentException {
        
        logger.debug("offlineUserLoginAndSelectDomain - logging in user: " + userId);
        //login
        //Note: allocates UMS module
        final String clientAddress = GiftServletUtils.getWebClientAddress(getThreadLocalRequest());
        //Creates a user session with the userId and username provided by the caller
        //Username and user id are both included in order to verify that the same user specified
        //during offline login exists in the UMS database
        UserSession uSession = new UserSession(userId);
        uSession.setUsername(username);
        
        //Logs into the UMS with the credentials passed by the client-side RPC caller
        RpcResponse loginResponse = loginWithUserSession(uSession, clientAddress, client);
        if(loginResponse != null && loginResponse.isSuccess()){

            //select domain
            //Note: allocates Domain module
            RpcResponse selectDomainResponse = userStartDomainSession(loginResponse.getBrowserSessionId(), domainRuntimeId, domainSourceId);
            if(selectDomainResponse.isSuccess()){
                
                // We need to get the domain session and notify that the course is ready to be started.
                
                DomainWebState webState = getExistingDomainWebState(loginResponse.getBrowserSessionId());
                if (webState != null) {
           
                    logger.debug("found the domain session, signaling that course is ready to be started.");
                    
                    // If we get this far we need to tell the tui that the course is ready to be started.
                    webState.signalCourseReady();
                                
                    // This should be a success here.
                    return selectDomainResponse;
                } else {
                            
                    logger.warn("offlineUserLoginAndSelectDomain - getExistingDomainSessionWebSession returned null");
                    return new RpcResponse(null, null, false, "getExistingDomainSessionWebSession returned null");
                }
                
               
            }else{
                //return ERROR info
                logger.error("offlineUserLoginAndSelectDomain - userStartDomainSession returned success=false.  Response is: " + selectDomainResponse);
                return selectDomainResponse;
            }
        }else{
            //return ERROR info
            logger.error("offlineUserLoginAndSelectDomain - userLogin returned success=false.  Response is: " + loginResponse);
            return loginResponse;
        }
    }
    
    @Override
    public RpcResponse ltiUserLogin(String consumerKey, String consumerId, String dataSetId, ClientProperties client) throws IllegalArgumentException {

        long start = System.currentTimeMillis();
        if(logger.isDebugEnabled()){
            logger.debug("ltiUserLogin - logging in lti user with key: " + consumerKey + ", consumerId: " + consumerId);
        }
       

        // This is a synchronous call.
        LtiUserRecord ltiRecord = getLtiUserRecord(consumerKey, consumerId);

        // If the user is not found, the record is null.
        if (ltiRecord == null) {
            
            logger.error("Unable to login user with consumerKey (" + consumerKey + ") and id (" + consumerId + ") because no existing lti user record was found.");
            RpcResponse errorResponse = new RpcResponse();
            errorResponse.setIsSuccess(false);
            errorResponse.setResponse("Error getting the lti user record.");
            return errorResponse;
        }
        
        if(logger.isInfoEnabled()){
            logger.info("Lti record received: " + ltiRecord);
        }
        
        Date curTime = new Date();
        // If a request comes in that is older than the allowed time, then it is considered stale.
        // This is an lti security measure to prevent old / unauthorized requests to the gift courses.
        if (curTime.getTime() - ltiRecord.getLtiTimestamp().getTime() > TutorModuleProperties.getInstance().getLtiTimeoutTutorMs()) {
            logger.error("Unable to login user with consumerKey (" + consumerKey + ") and id (" + consumerId + "), because the timestamp has expired.");
            RpcResponse errorResponse = new RpcResponse();
            errorResponse.setIsSuccess(false);
            errorResponse.setResponse("The LTI launch request timestamp is stale and the request is no longer valid.  Please try starting the course again from the LTI Tool Consumer.");
            return errorResponse;
        }
        
        // If the lti user is found, then log the user in based on the lti gift user id and the gift user name.
        RpcResponse loginResponse = ltiUserLogin(ltiRecord, dataSetId, client);
        if(loginResponse.isSuccess()){

            metrics.endTrackingRpc("ltiUserLogin", start);            
            return loginResponse;
            
            
        }else{
            //return ERROR info
            logger.error("ltiUserLogin - userLogin returned success=false.  Response is: " + loginResponse);
            return loginResponse;
        }
    }
    

    
    @Override
    public RpcResponse userLogin(final String username, final String password, String loginAsUserName, final ClientProperties client) throws IllegalArgumentException {
        
        long start = System.currentTimeMillis();
        final AsyncReturnBlocker<RpcResponse> returnBlocker = new AsyncReturnBlocker<RpcResponse>();

        String decryptedPassword;
        
        if(SERVER_PROPERTIES.shouldUseSSOLogin()) {
            
            /* Don't use the login as username if using SSO, since SSO doesn't currently support it */
            loginAsUserName = null;
            decryptedPassword = null;
            
        } else {
            decryptedPassword = decryptPassword(password);
        }
        
        if(logger.isDebugEnabled()){
            logger.debug("Attempting to login a user (ONLINE MODE authentication) with username "+username+".  [client = "+client+"]");
        }
        
        try {
	        //the address of the TUI client were a use is trying to login to GIFT
	        final String clientAddress = GiftServletUtils.getWebClientAddress(getThreadLocalRequest());
	        
	        //
	        // Authenticate credentials before continuing
	        //
	        String failedReason = null;
	        try{
	            failedReason = UserWebSession.authenticate(username, decryptedPassword, loginAsUserName, getThreadLocalRequest());
	        }catch(UserAuthenticationException userAuthenticationException){
	            //the authentication logic has provided additional information as to why they authentication failed
	            
	            logger.warn("Someone at "+clientAddress+" is trying to access "+username+"'s account but there was a problem: "+userAuthenticationException+".");
	            RpcResponse loginResponse = new RpcResponse(null, null, false, userAuthenticationException.getAuthenticationProblem());
	            returnBlocker.setReturnValue(loginResponse);
	            return returnBlocker.getReturnValueOrTimeout();
	        }
	        
	        if(failedReason != null){
	            //an unknown/un-handled authentication problem occurred.  Use the incorrect credentials excuse instead of giving direct
	            //insight into the authentication protocol.
	            logger.warn("Someone at "+clientAddress+" is trying to access "+username+"'s account but the login attempt failed because "+failedReason);
	            RpcResponse loginResponse = new RpcResponse(null, null, false, failedReason);
	            returnBlocker.setReturnValue(loginResponse);
	        }else{ 
	            
	            //determine the user to login as based on whether the login as username was provided
	            final String userToLoginAs;
                if(loginAsUserName != null){
                    logger.warn("User '"+username+"' is logging in as user '"+loginAsUserName+"'.");
                    userToLoginAs = loginAsUserName;
                }else{
                    userToLoginAs = username;
                }
	            
	            //
	            // first - get user id from username
	            //
	            UserWebSession.getUserIdByUserName(userToLoginAs, new GetUserIdAsyncResponseCallback() {
	                
	                @Override
	                public void notify(final int userId, boolean success, String response) {
	                    
	                    if(success){

                            logger.info("No user session for user ID " + userToLoginAs + ", attempting to login");               
                            
                            //
                            // then login using the user id obtained following the original (now 'simple') login path
                            //
                            final UserSession userSession = new UserSession(userId);
                            userSession.setUsername(userToLoginAs);
                            RpcResponse loginResponse = loginWithUserSession(userSession, clientAddress, client);
                            returnBlocker.setReturnValue(loginResponse);
	                            
	                    }else{
	                    	logger.warn("userLogin notify success = false, using null for userSessionId and browserSessionId and response = " + response);
	                        RpcResponse loginResponse = new RpcResponse(null, null, success, response);
	                        returnBlocker.setReturnValue(loginResponse);
	                    }
	                        
	                }
	            });
	        
	        }//end else on Authentication
        }catch(IllegalArgumentException e){
        	logger.error("Exception caught in userLogin with the message: " + e.getMessage(), e);
        	throw e;
        }
        metrics.endTrackingRpc("userLogin", start);
        return returnBlocker.getReturnValueOrTimeout();
    }
    
    /**
     * Logs in a user, specified by a user session, through the UMS Module
     * @param userSession The user session to used as credentials for the login operation
     * @param clientAddress The client ip as a string
     * @param client Information about this client that will be used to handle the browser session
     * @return The result (success or failure) of the login operation. Returns null in the case of a timeout
     */
    private RpcResponse loginWithUserSession(final UserSession userSession, final String clientAddress, final ClientProperties client) {
        final AsyncReturnBlocker<RpcResponse> returnBlocker = new AsyncReturnBlocker<RpcResponse>();
        
        UserWebSession userWebSession = TutorModule.getInstance().getUserSession(userSession);
        if (userWebSession != null && userWebSession.getSessionStatus() == SessionStatusEnum.RUNNING) {
            
            // Clear saved session data since the Dashboard allows users to save
            userEndDomainSessionAndLogout(userWebSession.getBrowserSessionKey());
            
        }
        
        UserWebSession.loginUser(userSession, new AsyncResponseCallback() {
            
            @Override
            public void notify(boolean success, String response, String additionalInformation) {
                
                if (success) {
                	try{
	                    UserWebSession webSession = TutorModule.getInstance().getUserSession(userSession);
	                    final TutorBrowserWebSession browserSession = TutorModule.getInstance().createBrowserSession(webSession, clientAddress, client);
	                    RpcResponse loginResponse = new RpcResponse(webSession.getUserSessionKey(), browserSession.getBrowserSessionKey(), true, "success");
	                    returnBlocker.setReturnValue(loginResponse);
	                    
                	} catch(IllegalArgumentException e){
                		logger.warn("Failed to create browser session for user " + userSession + ". Details: " + e);
                        RpcResponse loginResponse = new RpcResponse(null, null, false, 
                        		"Failed to create browser session after logging in due to the following error:\n" + e);
                        returnBlocker.setReturnValue(loginResponse);
                	}
                } else {
                    logger.warn("loginUser notify success = false, using null for userSessionId and browserSessionId and response = " + response);
                    RpcResponse loginResponse = new RpcResponse(null, null, success, response);
                    returnBlocker.setReturnValue(loginResponse);
                }
            }
        });
        
        return returnBlocker.getReturnValueOrTimeout();
    }
    
    @Override
    public RpcResponse getExistingUserSession(String username) {
    	
    	final AsyncReturnBlocker<RpcResponse> returnBlocker = new AsyncReturnBlocker<RpcResponse>();
    	
    	//
        // first - get user id from username
        //
        UserWebSession.getUserIdByUserName(username, new GetUserIdAsyncResponseCallback() {
            
            @Override
            public void notify(final int userId, boolean success, String response) {
                
                if(success){

                    UserSession userSession = new UserSession(userId);
                    UserWebSession userWebSession = TutorModule.getInstance().getUserSession(userSession);        
        
                    if (userWebSession != null) {
                    	logger.info("Found user web session. Status = " + userWebSession.getSessionStatus());
                    	RpcResponse result = new RpcResponse(userWebSession.getUserSessionKey(), userWebSession.getBrowserSessionKey(), true, null);
                    	returnBlocker.setReturnValue(result);
                    } else {
                    	RpcResponse result = new RpcResponse(null, null, false, "No existing user session.");
                    	returnBlocker.setReturnValue(result);
                    }
                } else {
                	RpcResponse result = new RpcResponse(null, null, false, "No existing user session.");
                	returnBlocker.setReturnValue(result);
                }
            }
        });
        
        return returnBlocker.getReturnValueOrTimeout();
    }

    @Override
    public RpcResponse userLogout(final String browserSessionKey) throws IllegalArgumentException {
        
        
        long start = System.currentTimeMillis();
        
        if (logger.isDebugEnabled()) {
            logger.debug("userLogout rpc called for browser: " + browserSessionKey);
        }
        
        RpcResponse response = null;
        try{
	        TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionKey);
	        if (session != null) {
	            UserWebSession userSession = TutorModule.getInstance().getUserSession(session.getUserSessionKey());
	            if (userSession != null) {
	                
	                userSession.logoutUser(session);
	                response = new RpcResponse(session.getUserSessionKey(), session.getBrowserSessionKey(), true, null);
	            } else {
	            	logger.warn("Tried to log out session with id: " + browserSessionKey + " but userSession is null");
	                response =  new RpcResponse(null, session.getBrowserSessionKey(), false, "Browser session does not have a valid user session.");
	            }
	        } else {
	        	
	        	if(browserSessionKey == null){
	        		logger.warn("userLogout session = null, using null for userSessionId and browserSessionId and response = false");
	        		
	        	} else {
	        		logger.warn("userLogout session not found for id: " + browserSessionKey + ", using null for userSessionId and browserSessionId and response = false");
	        	}
	        	
	            response = new RpcResponse(null, null, false, "Not a valid browser session.");
	        }
        }catch(IllegalArgumentException e){
        	logger.error("Exception caught in userLogout with the message: " + e.getMessage());
        	throw e;
        }
        metrics.endTrackingRpc("userLogout", start);
        return response;
    }

    @Override
    public RpcResponse userStartDomainSession(final String browserSessionKey, final String domainRuntimeId, final String domainSourceId) throws IllegalArgumentException {
        RpcResponse response = userStartDomainSession(browserSessionKey, domainRuntimeId, domainSourceId, null);
        return response;
    }
    
    
    /**
     * Private method to start a domain session.  This should only be called internally to the class.
     * 
     * @param browserSessionKey The browser session key making the request.
     * @param domainRuntimeId The runtime id of the course that is being started.
     * @param domainSourceId The source id of the course that is being started.
     * @param runtimeParams (Optional) Additional parameters that can be used to configure the domain session.
     * @return RpcResponse the response of the start domain session request.
     * @throws IllegalArgumentException
     */
    private RpcResponse userStartDomainSession(final String browserSessionKey, final String domainRuntimeId, final String domainSourceId, final AbstractRuntimeParameters runtimeParams) throws IllegalArgumentException {
        long start = System.currentTimeMillis();
        
        RpcResponse response = null;
        try{
	        final TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionKey);
	        if (session != null) {
	            final UserWebSession userSession = TutorModule.getInstance().getUserSession(session.getUserSessionKey());
	            if (userSession != null) {
	                if(logger.isInfoEnabled()){
	                    logger.info("User selected "+domainRuntimeId+" as the course for "+userSession);
	                }
	                
	                final AsyncReturnBlocker<RpcResponse> returnBlocker = new AsyncReturnBlocker<RpcResponse>();
	                session.setDomainSessionReturnBlocker(returnBlocker);
	                
	                userSession.userSelectDomain(domainRuntimeId, domainSourceId, session, runtimeParams, new AsyncResponseCallback() {
	                    
	                    @Override
	                    public void notify(boolean success, String response, String additionalInformation) {	

	                        if(success){
	                            returnBlocker.setReturnValue(new RpcResponse(session.getUserSessionKey(), session.getBrowserSessionKey(), true, response));
	                        }else{
	                            RpcResponse rpcResponse = new RpcResponse(session.getUserSessionKey(), session.getBrowserSessionKey(), false, response);
	                            rpcResponse.setAdditionalInformation(additionalInformation);
	                            returnBlocker.setReturnValue(rpcResponse);
	                        }
	                    }
	                });
	                
	                response = returnBlocker.getReturnValue();
	                
	            } else {
		        	logger.warn("userStartDomainSession userSession = null, using null for userSessionId and "+  session.getBrowserSessionKey() + " for broserSessionId and response = false");
	                response = new RpcResponse(null, session.getBrowserSessionKey(), false, "Browser session does not have a valid user session.");
	            }
	        } else {
	        	logger.warn("userStartDomainSession session = null, using null for userSessionId and browserSessionId and response = false");
	            response = new RpcResponse(null, null, false, "Not a valid browser session.");
	        }
        }catch(IllegalArgumentException e){
        	logger.error("Exception caught in userStartDomainSession with the message: " + e.getMessage());
        	throw e;
        }
        metrics.endTrackingRpc("userStartDomainSession", start);
        return response;
    }

    

    @Override
    public String getActiveDomainSessionName(String browserSessionKey) throws IllegalArgumentException {
        long start = System.currentTimeMillis();
        
        if (logger.isDebugEnabled()) {
            logger.debug("getActiveDomainSessionName() called for browser: " + browserSessionKey);
        }
        String response = null;
        try{
            
	        TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionKey);
	        if (session != null) {
	            
	            UserWebSession userSession = TutorModule.getInstance().getUserSession(session.getUserSessionKey());
	            if (userSession != null) {
	                
	                DomainWebState webState = userSession.getDomainWebState();
	                if (webState != null) {
	                    response = webState.getDomainRuntimeId();
	                }else{
	    	        	logger.info("There is no active domain session for "+userSession);
	                }
	                
	            }else{
		        	logger.info("There is no active domain session because there is no user session for "+session);
	            }
	            
	        }else{
	        	logger.info("There is no active domain session because the browser session could not be found for session key of "+browserSessionKey);
	        }
	        
        }catch(IllegalArgumentException e){
        	logger.error("Exception caught in while trying to get the active domain session name for session key of "+browserSessionKey+".", e);
        	throw e;
        }
        
        metrics.endTrackingRpc("getActiveDomainSessionName", start);
        return response;
    }

    @Override
    public CourseListResponse getDomainOptions(final String browserSessionKey) throws IllegalArgumentException {

        long start = System.currentTimeMillis();
        
        CourseListResponse response = null;
        final TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionKey);
        if (session != null) {

            final UserWebSession userSession = TutorModule.getInstance().getUserSession(session.getUserSessionKey());
            if (userSession != null) {

                final AsyncReturnBlocker<CourseListResponse> returnBlocker = new AsyncReturnBlocker<CourseListResponse>();
                TutorModule.getInstance().getDomainOptions(userSession.getUserSessionInfo(), userSession.getLmsUsername(), session, new MessageCollectionCallback() {

                    private Message reply = null;

                    @Override
                    public void success() {

                        ArrayList<GwtDomainOption> options = null;
                        if (reply != null) {
                            options = new ArrayList<GwtDomainOption>();
                            DomainOptionsList optionsList = ((DomainOptionsList) reply.getPayload());
                            for (DomainOption domainOption : optionsList.getOptions()) {							

                                String recommendationTypeString = null;
                                String recommendationMessage = null;

                                // Set the message to display when the course
                                // cannot be chosen                              
                                DomainOption.DomainOptionRecommendation recommendation = domainOption.getDomainOptionRecommendation();
                                if (recommendation != null) {
                                	
                                	logger.trace("Choosing course: " + domainOption.getDomainOptionRecommendation().toString());
                                    
                                    DomainOptionRecommendationEnum recommendationType = recommendation.getDomainOptionRecommendationEnum();
                                    if(recommendationType == DomainOptionRecommendationEnum.RECOMMENDED){
                                        recommendationMessage = "This course is recommended because: " + recommendation.getReason();
                                    }else if(recommendationType == DomainOptionRecommendationEnum.NOT_RECOMMENDED){
                                        recommendationMessage = "This course is not recommended because: " + recommendation.getReason();
                                    }else{
                                        recommendationMessage = "Cannot select this course, Reason: "
                                                + recommendationType.getDisplayName()
                                                + ", " + recommendation.getReason();
                                    }
                                    
                                    if(recommendationType != null){
                                        recommendationTypeString = recommendationType.getName();
                                    }
                                }

                                options.add(new GwtDomainOption(domainOption.getDomainId(), domainOption.getDomainName(), domainOption.getDescription(), recommendationTypeString, recommendationMessage));
                            }
                        } else {
                            logger.warn("Could not get the domain options for browser session key ("
                                    + browserSessionKey
                                    + ") because the message returned was malformed.");
                        }
                        CourseListResponse courseListResponse = new CourseListResponse(options);
                        returnBlocker.setReturnValue(courseListResponse);
                    }

                    @Override
                    public void received(Message msg) {
                        if (msg.getMessageType() == MessageTypeEnum.DOMAIN_OPTIONS_REPLY) {
                            reply = msg;
                        }
                    }

                    @Override
                    public void failure(Message msg) {
                        logger.error("Could not get the domain options for browser session key ("
                                + browserSessionKey
                                + ") because a failure message was returned: " + msg.getPayload().toString());
                        
                        CourseListResponse response;
                        if(msg.getPayload() instanceof NACK){
                            NACK nack = (NACK)msg.getPayload();
                            response = new CourseListResponse(nack.getErrorMessage(), nack.getErrorHelp());
                        }else{
                            response = new CourseListResponse("Failed to retrieve the course list.", 
                                    "The Domain module had a problem retrieving the course list.");
                        }
                        returnBlocker.setReturnValue(response);
                    }

                    @Override
                    public void failure(String why) {
                        logger.error("Could not get the domain options for browser session key ("
                                + browserSessionKey
                                + ") because some failure occurred: " + why);
                        
                        CourseListResponse response = new CourseListResponse("Failed to retrieve the course list.", 
                                why);
                        
                        returnBlocker.setReturnValue(response);                  
                    }
                });
                response = returnBlocker.getReturnValueOrTimeout();
            } else {
                logger.warn("Could not get the domain options for browser session key ("
                        + browserSessionKey
                        + ") because its user session key ("
                        + session.getUserSessionKey()
                        + ") is invalid.");
            }
        } else {
            logger.warn("Could not get the domain options for browser session key ("
                    + browserSessionKey
                    + ") because it is invalid.");
        }
        
        metrics.endTrackingRpc("getDomainOptions", start);
        return response;
    }

    @Override
    public RpcResponse userResumeDomainSessionUpdates(String browserSessionKey) throws IllegalArgumentException {
        long start = System.currentTimeMillis();
        RpcResponse response = null;
        TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionKey);
        if (session != null) {
            
            UserWebSession userSession = TutorModule.getInstance().getUserSession(session.getUserSessionKey());
            
            if(userSession != null && userSession.getDomainWebState() != null 
                    && !userSession.getDomainWebState().isLessonInactive()) {
                
                /*
                 * If the domain session is in the middle of a training app, avoid resuming the session, since
                 * its state is likely out of sync with the client
                 */
                response = new RpcResponse(session.getUserSessionKey(), session.getBrowserSessionKey(), false, 
                        "The Tutor web page was unable to synchronize with the training application that is currently in progress.");
                
            } else {
            
                session.resumeBrowserSession();
                response = new RpcResponse(session.getUserSessionKey(), session.getBrowserSessionKey(), true, null);
                
            } 
        } else {
        	logger.warn("Could not get the domain options for browser session key ("
                    + browserSessionKey
                    + ") because it is invalid.");
            response = new RpcResponse(null, null, false, "Not a valid browser session.");
        }
        
        metrics.endTrackingRpc("getLoadCourseProgress", start);
        return response;
    }

    @Override
    public RpcResponse userPauseDomainSessionUpdates(String browserSessionKey) throws IllegalArgumentException {
        long start = System.currentTimeMillis();
        RpcResponse response = null;
        TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionKey);
        if (session != null) {
            session.suspendDomainSessionListening();
            
        	logger.info("Domain session: " + browserSessionKey + " has been successfully suspended");
            response = new RpcResponse(session.getUserSessionKey(), session.getBrowserSessionKey(), true, null);
           
        } else {
        	logger.warn("Could not get the domain options for browser session key ("
                    + browserSessionKey
                    + ") because it is invalid.");
            response = new RpcResponse(null, null, false, "Not a valid browser session.");
        }
        
        metrics.endTrackingRpc("userPauseDomainSessionUpdates", start);
        return response;
    }

    @Override
    public RpcResponse userEndDomainSession(String browserSessionKey) throws IllegalArgumentException {
        long start = System.currentTimeMillis();
        logger.debug("userEndDomainSession() start");
        RpcResponse response = null;
        final TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionKey);
        if (session != null) {
            UserWebSession userSession = TutorModule.getInstance().getUserSession(session.getUserSessionKey());
            if (userSession != null) {
                final AsyncReturnBlocker<RpcResponse> returnBlocker = new AsyncReturnBlocker<RpcResponse>();
                TutorModule.getInstance().endDomainSession(userSession.getUserSessionInfo(), new AsyncResponseCallback() {

                    @Override
                    public void notify(boolean success, String response, String additionalInformation) {
                        if (success) {
                            session.resumeDomainSessionListening();
                        }
                        logger.debug("userEndDomainSession: success=" + success + ", response=" + response);
                        returnBlocker.setReturnValue(new RpcResponse(session.getUserSessionKey(), session.getBrowserSessionKey(), success, response));
                    }
                });
                response = returnBlocker.getReturnValueOrTimeout();
            } else {
            	logger.warn("Could not get the domain options for browser session key ("
                        + browserSessionKey
                        + ") because its user session key ("
                        + session.getUserSessionKey()
                        + ") is invalid.");
                response =  new RpcResponse(null, session.getBrowserSessionKey(), false, "Browser session does not have a user session while failing to end domain session.");
            }
        } else {
        	logger.warn("Could not get the domain options for browser session key ("
                    + browserSessionKey
                    + ") because it is invalid.");
            response = new RpcResponse(null, null, false, "Not a valid browser session.");
        }
        
        metrics.endTrackingRpc("userEndDomainSession", start);
        logger.debug("userEndDomainSession() end");
        return response;
    }

    @Override
    public RpcResponse userEndDomainSessionAndLogout(String browserSessionKey) {

        long start = System.currentTimeMillis();
        RpcResponse response = null;
        logger.debug("userEndDomainSessionAndLogout - called for session: " + browserSessionKey);
        if (getActiveDomainSessionName(browserSessionKey) != null) {
            logger.debug("userEndDomainSessionAndLogout - active domainsession found, ending session now: " + browserSessionKey);
            userEndDomainSession(browserSessionKey);
        }

        final TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionKey);
        if (session != null) {
            UserWebSession userSession = TutorModule.getInstance().getUserSession(session.getUserSessionKey());
            if (userSession != null) {
                logger.debug("userEndDomainSessionAndLogout - logging user out: " + session);
                response = userSession.logoutUser(session);
                
                if (response != null && !response.isSuccess()) {
                    logger.error("Error occurred logging out user with response: " + response);
                }
            } else {
                logger.warn("userEndDomainSessionAndLogout - Browser session does not have a valid user session: " + session);
                response = new RpcResponse(null, session.getBrowserSessionKey(), false, "Browser session does not have a valid user session.");
            }
        } else {
            logger.warn("userEndDomainSessionAndLogout - Not a valid browser session: " + browserSessionKey);
            response = new RpcResponse(null, null, false, "Not a valid browser session.");
        }
        
        logger.debug("userEndDomainSessionAndLogout() ended");
        metrics.endTrackingRpc("userEndDomainSessionAndLogout", start);
        
        return response;
    }

	@Override
	public RpcResponse isUrlResourceReachable(String urlString)
			throws IllegalArgumentException {
		
		RpcResponse response = null;
		
		try{
		
			URL url = new URL(urlString);
		    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		    connection.setRequestMethod("GET");
		    connection.setConnectTimeout(5000);
		
		    if (connection.getResponseCode() < 400) {
		    	response = new RpcResponse(null, null, true, null);
		    
		    } else {
		    	response = new RpcResponse(null, null, false, null);
		    }
		    
		} catch(Exception e){
			
			logger.error("An exception occurred while verifying that the URL '"+urlString+"' was reachable.", e);
			
			response = new RpcResponse(null, null, false, e.toString());
		}
		
		return response;
	}

	@Override
	public RpcResponse startExperimentCourse(String experimentId, String experimentFolder, final ClientProperties client) {
		
		long start = System.currentTimeMillis();
		
		final RpcResponse startExperimentResponse = new RpcResponse(new String(), new String(), true, null);
		
		if(StringUtils.isNotBlank(experimentId)){
	        
	        final String clientAddress = GiftServletUtils.getWebClientAddress(getThreadLocalRequest());
	        
	        final Object lock = new Object();
	        
	        UserWebSession.startExperimentCourse(experimentId, experimentFolder, clientAddress, client, new ExperimentSubjectCreatedCallback() {
	        	
	        	TutorBrowserWebSession browserSession= null;
	        	
	        	@Override
	        	public TutorBrowserWebSession onSubjectCreated(UserSession userSession){
	        		
	        		try{
	        		
		        		//need to create a user session and browser session for the new subject
		        		UserWebSession session = TutorModule.getInstance().createUserSession(userSession);	        		
		        		browserSession = TutorModule.getInstance().createBrowserSession(session, clientAddress, client);
		        		
		        		startExperimentResponse.setUserSessionId(session.getUserSessionKey());
		        		startExperimentResponse.setBrowserSessionId(session.getBrowserSessionKey());
		        		
		        		startExperimentResponse.setIsSuccess(true);
		        		startExperimentResponse.setResponse("successfully created browser session.");
	        		
	        		} catch(IllegalArgumentException e){
                		logger.warn("Failed to create browser session for user " + userSession + ". Details: " + e);
                		
                        String response = "Failed to create browser session after logging in due to the following error:\n" + e;
                        
                        startExperimentResponse.setIsSuccess(false);
		                startExperimentResponse.setResponse(response);
                	}
	        		
	        		//update the client that invoked this method with the new user session and browser session created
	        		synchronized (lock) {
	                    lock.notifyAll();
	                }
	        		
	        		return browserSession;
	        	}
	
	            @Override
	            public void onFailure(String response, String additionalInformation) {
	            	
	            	if(browserSession == null){
	
	            		//if a browser session hasn't been made yet, just reply to the inital RPC call
		                startExperimentResponse.setIsSuccess(false);
		                startExperimentResponse.setResponse(response);
		                startExperimentResponse.setAdditionalInformation(additionalInformation);		               
		                
	            	} else {
	            		
	            		//if a browser session has been made, the RPC call has already been completed, so we need to use the browser session
	            		//to report a failure
	            		browserSession.displayDialog(DialogTypeEnum.ERROR_DIALOG, 
	            				"Well, this is a problem...", 
	            				response + "\n\n" + additionalInformation);
	            	}
	            	
	            	synchronized (lock) {
	            		lock.notifyAll();
		            }
	            }
	        });
	        
	
	        synchronized (lock) {
	            while (startExperimentResponse.getResponse() == null) {
	                try {
	                    lock.wait();
	                } catch (InterruptedException e) {
	                	logger.error("InteruruptedException caught with message: " + e.getMessage());
	                }
	            }
	        }
	        
		} else {
			logger.warn("The experiment URL used to reach this page does not contain an experiment ID; therefore, no experiment corresponding to this URL could be found. ");
			startExperimentResponse.setIsSuccess(false);
			startExperimentResponse.setResponse("The experiment URL used to reach this page does not contain an experiment ID; "
					+ "therefore, no experiment corresponding to this URL could be found. ");
		}
        
        metrics.endTrackingRpc("startExperimentCourse", start);

        return startExperimentResponse;
	}
	
	/**
	 * This will decrypt the user's password based upon agreed upon methods between this and 
	 * the DashboardServiceImpl and return the actual user's password. The software uses a 
	 * common password to encrypt/decrypt the user's password. 
	 * @param password - The encrypted password
	 * @return String - the unencrypted password
	 */
    private String decryptPassword(String password) {
        String decryptedPassword = "Didn't decrypt properly";

        // use a password property to use for encrypting the user's password.
        //
        char[] encryptKey = TutorModuleProperties.getInstance().getCiphorPassword().toCharArray();
        
        // this is an agreed upon value found also used in DashboardServiceImpl.
        //
        byte[] salt = "21125150OU812".getBytes();
        
        try {
            
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            SecretKey tmp = keyFactory.generateSecret(new PBEKeySpec(encryptKey, salt, 20, 128));
            SecretKeySpec key = new SecretKeySpec(tmp.getEncoded(), "AES");
            byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            IvParameterSpec ivspec = new IvParameterSpec(iv);
            
            Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            
            // Initialize the cipher with the PBKDF2WithHmacSHA256 key.
            //
            pbeCipher.init(Cipher.DECRYPT_MODE, key, ivspec);
            

            // Decode the Internet friendy format first.
            //
            byte[] decodedString = Base64.getDecoder().decode(password);
            
            // Perform the decryption.
            //
            decryptedPassword = new String (pbeCipher.doFinal(decodedString), "UTF-8");
            
        } catch (Exception e) {
            logger.error("Something blew up while trying to decrypt the password.", e);
        }
        
        return decryptedPassword;
    }
    
    
	@Override
	public RpcResponse setFeedbackWidgetIsActive(String browserSessionId, boolean isActive) {
		
		RpcResponse response = new RpcResponse();
		TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionId);		
				
		if (session != null) {
			UserWebSession userSession = TutorModule.getInstance().getUserSession(session.getUserSessionKey());
			UpdateQueueManager.getInstance().setFeedbackIsActive(userSession.getUserId(), isActive); 
			response.setIsSuccess(true);
			logger.info("Set the status of the Feedback widget to " + (isActive ? "active" : "inactive"));
			
		} else {
			response.setIsSuccess(false);
			logger.warn("Failed to set the status of the Feedback widget because the BrowserWebSession was null.");
		}
		
		return response;
	}
	
	
	@Override
	public RpcResponse dequeueFeedbackWidgetUpdate(String browserSessionId) {

		RpcResponse response = new RpcResponse();
		TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionId);		
		
		if (session != null) {
			UserWebSession userSession = TutorModule.getInstance().getUserSession(session.getUserSessionKey());
			
			if (userSession != null) {
			    response.setIsSuccess(userSession.dequeueFeedbackUpdate());
			} else {
			    logger.error("Could not find a user session to dequeue the feedback widget update for browser session: " + browserSessionId);
			    response.setIsSuccess(false);
			}
		} else {
			response.setIsSuccess(false);
			logger.warn("Failed to retrieve any queued updates for the active feedback widget.");
		}
		
		return response;
	}
	
	
	@Override
	public RpcResponse setActiveConversationWidget(String browserSessionId, int chatId) {
		
		RpcResponse response = new RpcResponse();
		TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionId);		
		
		if (session != null) {
		    if(logger.isInfoEnabled()){
		        logger.info("Received a request from the client to set the active chat id to " + chatId);
		    }
			UserWebSession userSession = TutorModule.getInstance().getUserSession(session.getUserSessionKey());
			UpdateQueueManager.getInstance().setActiveChatId(userSession.getUserId(), chatId);
			response.setIsSuccess(true);
			
		} else {
			logger.warn("Failed to set the active chat id because the BrowserWebSession was null.");
			response.setIsSuccess(false);
		}
		
		return response;
	}
	
	
	@Override
	public RpcResponse dequeueChatWidgetUpdate(String browserSessionId) {

		RpcResponse response = new RpcResponse();
		TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionId);		
		
		if (session != null) {
			UserWebSession userSession = TutorModule.getInstance().getUserSession(session.getUserSessionKey());
			try{
			    boolean dequeued = TutorModule.getInstance().dequeueChatUpdate(userSession);
			    response.setIsSuccess(dequeued);
			}catch(Exception e){
			    logger.error("Caught exception while trying to dequeue the next chat widget update for "+userSession, e);
			    response.setIsSuccess(false);
			    response.setResponse("Failed to dequeue the next chat widget update.");
			    response.setAdditionalInformation("An exception was thrown while attempting to retrieve the next conversation element.");
			    response.setErrorStackTrace(DetailedException.getFullStackTrace(e));
			}
		} else {
			logger.warn("Failed to retrieve any queued updates for the active chat widget.");
			response.setIsSuccess(false);
		}

		return response;
	}
	
	@Override
	public RpcResponse setTutorActionsAvailable(String browserSessionId, boolean isAvailable) {

		RpcResponse response = new RpcResponse();
		TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionId);		
		
		if (session != null) {
			UserWebSession userSession = TutorModule.getInstance().getUserSession(session.getUserSessionKey());
			if(userSession != null && userSession.getDomainWebState() != null) {
				userSession.getDomainWebState().setTutorActionsAvailable(isAvailable);
			}
			// If the user session or domain session is null, the course has ended so no need to set the tutor actions.
			response.setIsSuccess(true);
		} else {
			logger.warn("Failed to set the tutor actions widget status because the BrowserWebSession was null.");
			response.setIsSuccess(false);
		}

		return response;
	}
		
	@Override
	public RpcResponse setInactiveConversationWidget(int inactiveChatId, String browserSessionId) {
		
		RpcResponse response = new RpcResponse();
		TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionId);		
		
		if (session != null) {
			UserWebSession userSession = TutorModule.getInstance().getUserSession(session.getUserSessionKey());
			UpdateQueueManager.getInstance().setInactiveChatId(userSession.getUserId(), inactiveChatId);
			logger.info("Received a request to set the conversation widget to inactive.  Setting the active chat id to -1");
			response.setIsSuccess(true);
		} else {
			logger.warn("Failed to set the chat id because the BrowserWebSession was null.");
			response.setIsSuccess(false);
		}
		
		return response;
	}
	
	@Override
	public Boolean isAvatarIdle(String browserSessionKey) throws IllegalArgumentException {
        long start = System.currentTimeMillis();
        
        Boolean response = false;
        try{
            
	        TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionKey);
	        if (session != null) {
	            
	            UserWebSession userSession = TutorModule.getInstance().getUserSession(session.getUserSessionKey());
	            if (userSession != null) {
	                
	                DomainWebState webState = userSession.getDomainWebState();
	                if (webState != null) {
	                    response = webState.isCharacterIdle();
	                }else{
	    	        	logger.info("There is no active domain session for "+userSession);
	                }
	                
	            }else{
		        	logger.info("There is no active domain session because there is no user session for "+session);
	            }
	            
	        }else{
	        	logger.info("There is no active domain session because the browser session could not be found for session key of "+browserSessionKey);
	        }
	        
        }catch(IllegalArgumentException e){
        	logger.error("Exception caught in while trying to get the active domain session name for session key of "+browserSessionKey+".", e);
        	throw e;
        }
        
        metrics.endTrackingRpc("isAvatarIdle", start);
        return response;
	}
	
	
	/**
	 * Private utility method (should not be public) to get the lti record based on the consumer key 
	 * and the consumer id.  The lti record contains information such as the gift user id which should
	 * not be exposed to the client.
	 * 
	 * @param consumerKey The consumer key of the lti user.
	 * @param consumerId The consumer id of the lti user.
	 * @return the LtiUserRecord containing the user information if found, null if the record was not retrieved.
	 */
	private LtiUserRecord getLtiUserRecord(String consumerKey, String consumerId) {
	    
	    final AsyncReturnBlocker<LtiUserRecord> returnBlocker = new AsyncReturnBlocker<LtiUserRecord>();
	    
	    
	    TutorModule.getInstance().getLtiUserRecord(consumerKey, consumerId, new LtiGetUserRecordCallback() {

            @Override
            public void onSuccess(LtiUserRecord record) {
                
                returnBlocker.setReturnValue(record);
            }

            @Override
            public void onFailure(String errorMessage,
                    String additionalInformation) {
                
                returnBlocker.setReturnValue(null);
            }
	        
	    });
        
        return returnBlocker.getReturnValueOrTimeout();

	}
	
	
	/**
     * Private utility method (should not be public) to log the lti user once the gift user id
     * and the gift username have been retreived.
     * 
     * @param userId  The gift user id of the lti user.
     * @param username The gift username of the lti user.
     * @param dataSetId (optional) The data collection data set id that can be specified during the lti launch sequence.
     * @param client Information about this client that will be used to handle the browser session
     * @return the rpc response of the login.
     * @throws IllegalArgumentException
     */
    private RpcResponse ltiUserLogin(final LtiUserRecord ltiRecord, final String dataSetId, final ClientProperties client) throws IllegalArgumentException {
        final AsyncReturnBlocker<RpcResponse> returnBlocker = new AsyncReturnBlocker<RpcResponse>();
        try{
            final int userId = ltiRecord.getGlobalUserId();
            // For now both the user id and the global user id are set to the same value.  Ideally
            // this should only be the global user id being set, but there are currently legacy restrictions
            // which require the user id to be filled out (similar to experiments).
            final UserSession lookupUser = new UserSession(userId);
            lookupUser.setSessionType(UserSessionType.LTI_USER);

            /* Injecting a read-only user because LTI needs a username for Nuxeo
             * permissions. See #4550 for more details. */
            lookupUser.setUsername(TutorModuleProperties.getInstance().getReadOnlyUser());

            LtiUserSessionDetails sessionDetails = new LtiUserSessionDetails(ltiRecord.getLtiUserId(), dataSetId, userId);
            lookupUser.setSessionDetails(sessionDetails);
            
            UserWebSession userSession = TutorModule.getInstance().getUserSession(lookupUser);
            
            final String clientAddress = GiftServletUtils.getWebClientAddress(getThreadLocalRequest());
            
            if (userSession != null) {
                
                if(logger.isInfoEnabled()){
                    logger.info("ltiUserLogin - found existing user session, ending the session first.");
                }
                
                // Clear out any existing domain session (if it exists) and also logout the browser session.
                RpcResponse logoutResponse = userEndDomainSessionAndLogout(userSession.getBrowserSessionKey());
                
                if (logoutResponse != null && logoutResponse.isSuccess()) {
                    if(logger.isInfoEnabled()){
                        logger.info("ltiUserLogin - session logged out first, now attempting to login.");
                    }
                } else {
                    
                    returnBlocker.setReturnValue(logoutResponse);
                }
            
            }  else {
                if(logger.isInfoEnabled()){
                    logger.info("No user session for user " + lookupUser + ", attempting to login");
                }
            }

       
            UserWebSession.loginUser(lookupUser, new AsyncResponseCallback() {

                @Override
                public void notify(boolean success, String response, String additionalInformation) {
                    
                    if (success) {
                    	try{
	                        UserWebSession userSession = TutorModule.getInstance().getUserSession(lookupUser);
	                        final TutorBrowserWebSession browserSession = TutorModule.getInstance().createBrowserSession(userSession, clientAddress, client);
	                        RpcResponse loginResponse = new RpcResponse(userSession.getUserSessionKey(), browserSession.getBrowserSessionKey(), true, "success");
	                        returnBlocker.setReturnValue(loginResponse);
	                        
                    	} catch(IllegalArgumentException e){
                    		logger.warn("Failed to create browser session for user " + lookupUser + ". Details: " + e);
	                        RpcResponse loginResponse = new RpcResponse(null, null, false, 
	                        		"Failed to create browser session after logging in due to the following error:\n" + e);
	                        returnBlocker.setReturnValue(loginResponse);
                    	}
                    } else {
                        logger.warn("loginUser notify success = false, using null for userSessionId and browserSessionId and response = " + response);
                        RpcResponse loginResponse = new RpcResponse(null, null, success, response);
                        returnBlocker.setReturnValue(loginResponse);
                    }
                }
            });
        }catch(IllegalArgumentException e){
            logger.error("Exception caught in userLogin with the message: " + e.getMessage());
            throw e;
        }
        
        return returnBlocker.getReturnValueOrTimeout();
    }
    
    @Override
    public RpcResponse sendEmbeddedAppState(String message, String browserSessionKey) {
        RpcResponse response = new RpcResponse();
        TutorModule.getInstance().sendMessageFromEmbeddedApplication(message, browserSessionKey);
        response.setIsSuccess(true);
        return response;
    }
	
	@Override
	public SurveyGiftData getSurvey(final String browserSessionKey, final int surveyContextId, final String giftKey) throws Exception {
	    final AsyncReturnBlocker<SurveyGiftData> blocker = new AsyncReturnBlocker<>();
	    
	    TutorModule.getInstance().sendGetSurveyRequest(browserSessionKey, surveyContextId, giftKey, new MessageCollectionCallback() {
            
	        private SurveyGiftData survey;
	        
            @Override
            public void success() {
                blocker.setReturnValue(survey);
            }
            
            @Override
            public void received(Message msg) {
                if(msg.getMessageType() == MessageTypeEnum.GET_SURVEY_REPLY) {
                    survey = (SurveyGiftData) msg.getPayload();
                } else {
                    String logMessage = new StringBuilder()
                            .append("Was expecting to receive a message of type '")
                            .append(MessageTypeEnum.GET_SURVEY_REPLY.getName())
                            .append("' but instead, the message type of '")
                            .append(msg.getMessageType().getName())
                            .append("' was received.")
                            .toString();
                    logger.error(logMessage);
                    blocker.setReturnValue(null);
                }
            }
            
            @Override
            public void failure(String why) {
                String logMessage = new StringBuilder()
                        .append("There was an error retrieving the survey where ")
                        .append("surveyContextId = ")
                        .append(surveyContextId)
                        .append(" and giftKey = ")
                        .append(giftKey)
                        .append(" because ")
                        .append(why)
                        .toString();
                logger.error(logMessage);
                blocker.setReturnValue(null);
            }
            
            @Override
            public void failure(Message msg) {
                String logMessage = new StringBuilder()
                        .append("There was an error retrieving the survey where ")
                        .append("surveyContextId = ")
                        .append(surveyContextId)
                        .append(" and giftKey = ")
                        .append(giftKey)
                        .append(" but the message ")
                        .append(msg)
                        .append(" was received.")
                        .toString();
                logger.error(logMessage);
                blocker.setReturnValue(null);
            }
        });
	    
	    SurveyGiftData surveyGiftData = blocker.getReturnValueOrTimeout();
	    if(surveyGiftData != null) {
	        return surveyGiftData;
	    } else {
	        throw new Exception("There was an issue retrieving the survey with the key " 
	                + giftKey + " in the context " + surveyContextId);
	    }
	}

    @Override
    public RpcResponse buildOAuthLtiUrl(String rawUrl, LtiProperties properties, String browserSessionKey) {
        final AsyncReturnBlocker<RpcResponse> returnBlocker = new AsyncReturnBlocker<RpcResponse>();

        if (logger.isDebugEnabled()) {
            logger.debug("Entered buildOAuthLtiUrl in the TutorUserInterfaceServiceImpl with url: " + rawUrl + " and properties: " + properties);
        }
        
        final RpcResponse response = new RpcResponse();
        TutorModule.getInstance().buildOAuthLtiUrl(rawUrl, properties, browserSessionKey, new MessageCollectionCallback() {

            @Override
            public void success() {
                // nothing to do
            }

            @Override
            public void received(Message msg) {
                response.setResponse((String)msg.getPayload());
                response.setIsSuccess(true);
                returnBlocker.setReturnValue(response);
            }

            @Override
            public void failure(String why) {
                response.setResponse(why);
                response.setIsSuccess(false);
                returnBlocker.setReturnValue(response);
            }

            @Override
            public void failure(Message msg) {
                response.setResponse((String)msg.getPayload());
                response.setIsSuccess(false);
                returnBlocker.setReturnValue(response);
            }
        });

        return returnBlocker.getReturnValueOrTimeout();
    }
    
    @Override
    public GenericRpcResponse<Void> stopTrainingAppScenario(String browserSessionKey) {
        
        TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionKey);
        if (session != null) {
            UserWebSession userSession = TutorModule.getInstance().getUserSession(session.getUserSessionKey());
            
            if(userSession != null) {
                
                DomainWebState domainState = userSession.getDomainWebState();
                
                if(domainState != null) {
                    
                    final AsyncReturnBlocker<GenericRpcResponse<Void>> returnBlocker = new AsyncReturnBlocker<>();
                    
                    TutorModule.getInstance().sendUserActionTaken(new FinishScenario(), domainState, new MessageCollectionCallback() {
                        
                        @Override
                        public void success() {
                            returnBlocker.setReturnValue(new SuccessfulResponse<Void>());
                        }
                        
                        @Override
                        public void received(Message msg) {
                            //Nothing to do here
                        }
                        
                        @Override
                        public void failure(String why) {
                            returnBlocker.setReturnValue(new FailureResponse<Void>(new DetailedException(
                                    "The training application could not stopped because a problem occurred while telling the application to stop.", 
                                    "An unexpected failure occurred while stopping the application: " + why, 
                                    null
                            )));
                        }
                        
                        @Override
                        public void failure(Message msg) {
                            returnBlocker.setReturnValue(new FailureResponse<Void>(new DetailedException(
                                    "The training application could not stopped because a problem occurred while telling the application to stop.", 
                                    "An unexpected failure occurred while processing the message: " + msg, 
                                    null
                            )));
                        }
                    });
                    
                    return returnBlocker.getReturnValueOrTimeout();
                    
                } else {
                    return new FailureResponse<>(new DetailedException(
                            "The training application could not stopped because a problem occurred while gathering this session's domain state.", 
                            "The user session with the key '" + session.getUserSessionKey() + "' does not have an an associated domain state.", 
                            null
                    ));
                }
                
            } else {
                return new FailureResponse<>(new DetailedException(
                        "The training application could not stopped because a problem occurred while gathering this session's data.", 
                        "No user session with the key '" + session.getUserSessionKey() + "' was found on the server.", 
                        null
                ));
            }
            
        } else {
            return new FailureResponse<>(new DetailedException(
                    "The training application could not stopped because a problem occurred while gathering this session's data.", 
                    "No browser session with the key '" + browserSessionKey + "' was found on the server.", 
                    null
            ));
        }
    }
    
    
    
    @Override
    public GetActiveKnowledgeSessionsResponse fetchActiveKnowledgeSessions(String browserSessionKey) {
        
        final AsyncReturnBlocker<GetActiveKnowledgeSessionsResponse> returnBlocker = new AsyncReturnBlocker<GetActiveKnowledgeSessionsResponse>();

        final TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionKey);
        if (session != null) {
            UserWebSession userWebSession = TutorModule.getInstance().getUserSession(session.getUserSessionKey());
            if (userWebSession != null) {
                UserSession userSession  = userWebSession.getUserSessionInfo();
                DomainWebState webState = userWebSession.getDomainWebState();
                
                KnowledgeSessionsRequest request = new KnowledgeSessionsRequest();
                request.setFullTeamSessions(false);
                request.setIndividualSessions(false);
                request.setRunningSessions(false);
                request.addCourseId(webState.getDomainSourceId());
                TutorModule.getInstance().sendKnowledgeSessionRequestMessage(userSession, webState.getDomainSessionId(), request, 
                        new ActiveKnowledgeSessionsCallback(returnBlocker, Constants.EMPTY));
            }
                        
        }
        return returnBlocker.getReturnValueOrTimeout();
    }
    
    
    
    
    @Override
    public AbstractKnowledgeSessionResponse hostTeamSessionRequest(String browserSessionKey) {
        final AsyncReturnBlocker<AbstractKnowledgeSessionResponse> returnBlocker = new AsyncReturnBlocker<AbstractKnowledgeSessionResponse>();

        final TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionKey);
        if (session != null) {
            UserWebSession userWebSession = TutorModule.getInstance().getUserSession(session.getUserSessionKey());
            if (userWebSession != null) {
                UserSession userSession  = userWebSession.getUserSessionInfo();
                DomainWebState webState = userWebSession.getDomainWebState();
                final Integer domainSessionId = webState.getDomainSessionId();
                
                // This should be passed in from the client if we want to allow users to create their own names, for
                // now just append a common suffix.
                final String SESSION_NAME_SUFFIX;
                if(webState.getDomainSourceId().contains(Constants.FORWARD_SLASH)){
                    int courseFolderEnd = webState.getDomainSourceId().lastIndexOf(Constants.FORWARD_SLASH);
                    String noCourseXML = webState.getDomainSourceId().substring(0, courseFolderEnd);
                    int courseFolderStart = noCourseXML.lastIndexOf(Constants.FORWARD_SLASH);
                    if(courseFolderStart != -1){
                        SESSION_NAME_SUFFIX = noCourseXML.substring(courseFolderStart+1);
                    }else{
                        SESSION_NAME_SUFFIX = noCourseXML;
                    }
                }else{
                    SESSION_NAME_SUFFIX = "Hosted Session";
                }
                
                String sessionName = SESSION_NAME_SUFFIX;
                ManageTeamMembershipRequest request = 
                        ManageTeamMembershipRequest.createHostedTeamKnowledgeSessionRequest(domainSessionId, sessionName);
                
                TutorModule.getInstance().manageTeamSessionRequest(userSession, domainSessionId, request,
                        new LookupKnowledgeSessionCallback(returnBlocker, domainSessionId, browserSessionKey));
            }
        }
        return returnBlocker.getReturnValueOrTimeout();
    }
    
    @Override
    public GetActiveKnowledgeSessionsResponse leaveTeamSessionRequest(String browserSessionKey, String sessionName, int hostDomainId, String hostUserName) {
        final AsyncReturnBlocker<GetActiveKnowledgeSessionsResponse> returnBlocker = new AsyncReturnBlocker<GetActiveKnowledgeSessionsResponse>();

        final TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionKey);
        if (session != null) {
            UserWebSession userWebSession = TutorModule.getInstance().getUserSession(session.getUserSessionKey());
            if (userWebSession != null) {
                UserSession userSession  = userWebSession.getUserSessionInfo();
                DomainWebState webState = userWebSession.getDomainWebState();
                
                ManageTeamMembershipRequest request = null;
                int senderDomainId = webState.getDomainSessionId();
                
                if (senderDomainId == hostDomainId) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("leaveTeamSessionRequest - requesting host to destroy the session: " + hostUserName);
                    }
                    // Request is for the host to destroy the session.
                    request = ManageTeamMembershipRequest.createDestroyTeamKnowledgeSessionRequest(hostDomainId);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("leaveTeamSessionRequest - requesting member to leave the session: " + userSession.getUsername());
                    }
                    // Request is for a non-host to leave the session.
                    request = ManageTeamMembershipRequest.createLeaveTeamKnowledgeSessionRequest(hostDomainId);
                }
                
                TutorModule.getInstance().manageTeamSessionRequest(userSession, webState.getDomainSessionId(), request,
                        new ActiveKnowledgeSessionsCallback(returnBlocker, webState.getDomainSourceId()));
            }
        }
        return returnBlocker.getReturnValueOrTimeout();
    }

    @Override
    public GetActiveKnowledgeSessionsResponse kickFromTeamSessionRequest(String browserSessionKey, int hostDomainId, SessionMember userToKick) {
        final AsyncReturnBlocker<GetActiveKnowledgeSessionsResponse> returnBlocker = new AsyncReturnBlocker<GetActiveKnowledgeSessionsResponse>();

        final TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionKey);
        if (session != null) {
            UserWebSession userWebSession = TutorModule.getInstance().getUserSession(session.getUserSessionKey());
            if (userWebSession != null) {
                UserSession userSession  = userWebSession.getUserSessionInfo();
                DomainWebState webState = userWebSession.getDomainWebState();
                
                
                int senderDomainId = webState.getDomainSessionId();
                
                if (senderDomainId == hostDomainId && senderDomainId != userToKick.getDomainSessionId()) {
                    
                    //only allow hoster to kick other joiners in the team session
                    if (logger.isDebugEnabled()) {
                        logger.debug("kickFromTeamSessionRequest - requesting member to leave the session: " + userSession.getUsername());
                    }
                    
                    //get the web state of the joiner being kicked
                    DomainWebState joinerWebState = TutorModule.getInstance().getDomainWebState(userToKick.getDomainSessionId());
                    
                    if(joinerWebState != null) {
                        
                        //have the joiner's domain session request to leave the team session
                        ManageTeamMembershipRequest request = ManageTeamMembershipRequest.createLeaveTeamKnowledgeSessionRequest(hostDomainId);
                        
                        TutorModule.getInstance().manageTeamSessionRequest(joinerWebState.getUserSession(), joinerWebState.getDomainSessionId(), request,
                                new ActiveKnowledgeSessionsCallback(returnBlocker, joinerWebState.getDomainSourceId()));
                    }
                }
            }
        }
        return returnBlocker.getReturnValueOrTimeout();
    }
    
    @Override
    public GetActiveKnowledgeSessionsResponse changeTeamSessionNameRequest(String browserSessionKey, int hostDomainId, String newSessionName) {
        final AsyncReturnBlocker<GetActiveKnowledgeSessionsResponse> returnBlocker = new AsyncReturnBlocker<GetActiveKnowledgeSessionsResponse>();

        final TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionKey);
        if (session != null) {
            UserWebSession userWebSession = TutorModule.getInstance().getUserSession(session.getUserSessionKey());
            if (userWebSession != null) {
                UserSession userSession  = userWebSession.getUserSessionInfo();
                DomainWebState webState = userWebSession.getDomainWebState();
                
                
                int senderDomainId = webState.getDomainSessionId();
                
                if (senderDomainId == hostDomainId) {
                    
                    //only allow host to change the team session name
                    if (logger.isDebugEnabled()) {
                        logger.debug("changeTeamSessionNameRequest - requesting to change the team session name: " + userSession.getUsername());
                    }
                    
                    ManageTeamMembershipRequest request = ManageTeamMembershipRequest.createChangeTeamKnowledgeSessionNameRequest(hostDomainId, newSessionName);
                    
                    TutorModule.getInstance().manageTeamSessionRequest(userSession, webState.getDomainSessionId(), request,
                            new ActiveKnowledgeSessionsCallback(returnBlocker, webState.getDomainSourceId()));
                }
            }
        }
        return returnBlocker.getReturnValueOrTimeout();
    }
    
    @Override
    public DetailedRpcResponse startTeamSessionRequest(String browserSessionKey) {
        
        final String browserSessionId = browserSessionKey;
        final AsyncReturnBlocker<DetailedRpcResponse> returnBlocker = new AsyncReturnBlocker<DetailedRpcResponse>();

        final TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionKey);
        if (session != null) {
            UserWebSession userWebSession = TutorModule.getInstance().getUserSession(session.getUserSessionKey());
            if (userWebSession != null) {
                UserSession userSession  = userWebSession.getUserSessionInfo();
                DomainWebState webState = userWebSession.getDomainWebState();
                TutorModule.getInstance().startTeamSessionRequest(userSession, webState.getDomainSessionId(), 
                        
                        new MessageCollectionCallback() {
                            @Override
                            public void success() {
                                // do nothing
                            }
        
                            @Override
                            public void received(Message msg) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("startTeamSessionRequest - Message reply received: " + msg.getMessageType());
                                }

                                DetailedRpcResponse response = new DetailedRpcResponse();
                                response.setIsSuccess(true);
                                response.setBrowserSessionId(browserSessionId);
                                returnBlocker.setReturnValue(response);
                            }
        
                            @Override
                            public void failure(String why) {
                                String errorMsg = "The request to start the host session failed because why='" + why + "'.";
                                logger.error(errorMsg);
                                DetailedRpcResponse response = new DetailedRpcResponse();
                                response.setIsSuccess(false);
                                response.setBrowserSessionId(browserSessionId);
                                response.setResponse(errorMsg);
                                returnBlocker.setReturnValue(response);
                            }
        
                            @Override
                            public void failure(Message msg) {
                                String errorMsg = "The request to start the host session failed because msg='" + msg + "'.";
                                logger.error(errorMsg);
                                
                                if(msg.getPayload() instanceof NACK){
                                    NACK nack = (NACK)msg.getPayload();
                                    errorMsg = nack.getErrorMessage();
                                    
                                }
                                DetailedRpcResponse response = new DetailedRpcResponse();
                                response.setIsSuccess(false);
                                response.setBrowserSessionId(browserSessionId);
                                response.setResponse(errorMsg);
                                returnBlocker.setReturnValue(response);
                            }
                        });
            }
        }
        return returnBlocker.getReturnValueOrTimeout();
    }


    @Override
    public AbstractKnowledgeSessionResponse joinSessionRequest(String browserSessionKey, AbstractKnowledgeSession selectedSession) {

        final AsyncReturnBlocker<AbstractKnowledgeSessionResponse> returnBlocker = new AsyncReturnBlocker<AbstractKnowledgeSessionResponse>();

        final TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionKey);
        if (session != null) {
            UserWebSession userWebSession = TutorModule.getInstance().getUserSession(session.getUserSessionKey());
            if (userWebSession != null) {
                UserSession userSession  = userWebSession.getUserSessionInfo();
                DomainWebState webState = userWebSession.getDomainWebState();
                
                final Integer hostDomainSessionId = selectedSession.getHostSessionMember().getDomainSessionId();
                ManageTeamMembershipRequest request = ManageTeamMembershipRequest.createJoinTeamKnowledgeSessionRequest(hostDomainSessionId);
                TutorModule.getInstance().manageTeamSessionRequest(userSession, webState.getDomainSessionId(), request, 
                        new LookupKnowledgeSessionCallback(returnBlocker, hostDomainSessionId, browserSessionKey));
                        
            }
        }
        return returnBlocker.getReturnValueOrTimeout();

    }


    @Override
    public GetActiveKnowledgeSessionsResponse selectTeamMemberRole(String browserSessionKey, String roleName, 
            int hostDomainId) {

        final AsyncReturnBlocker<GetActiveKnowledgeSessionsResponse> returnBlocker = new AsyncReturnBlocker<GetActiveKnowledgeSessionsResponse>();

        final TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionKey);
        if (session != null) {
            UserWebSession userWebSession = TutorModule.getInstance().getUserSession(session.getUserSessionKey());
            if (userWebSession != null) {
                UserSession userSession  = userWebSession.getUserSessionInfo();
                DomainWebState webState = userWebSession.getDomainWebState();

                    ManageTeamMembershipRequest request = ManageTeamMembershipRequest.createAssignTeamMemberTeamKnowledgeSessionRequest(
                            hostDomainId, roleName);
                    TutorModule.getInstance().manageTeamSessionRequest(userSession, webState.getDomainSessionId(), request,
                            new ActiveKnowledgeSessionsCallback(returnBlocker, webState.getDomainSourceId()));
            }   
        }
        return returnBlocker.getReturnValueOrTimeout();
    }
    
    @Override
    public GetActiveKnowledgeSessionsResponse unassignTeamMemberRole(String browserSessionKey, String roleName, 
            int hostDomainId) {

        final AsyncReturnBlocker<GetActiveKnowledgeSessionsResponse> returnBlocker = new AsyncReturnBlocker<GetActiveKnowledgeSessionsResponse>();

        final TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionKey);
        if (session != null) {
            UserWebSession userWebSession = TutorModule.getInstance().getUserSession(session.getUserSessionKey());
            if (userWebSession != null) {
                UserSession userSession  = userWebSession.getUserSessionInfo();
                DomainWebState webState = userWebSession.getDomainWebState();

                    ManageTeamMembershipRequest request = ManageTeamMembershipRequest.createUnassignTeamMemberTeamKnowledgeSessionRequest(
                            hostDomainId, roleName);
                    TutorModule.getInstance().manageTeamSessionRequest(userSession, webState.getDomainSessionId(), request,
                            new ActiveKnowledgeSessionsCallback(returnBlocker, webState.getDomainSourceId()));
            }   
        }
        return returnBlocker.getReturnValueOrTimeout();
    }
    
    @Override
    public GenericRpcResponse<Integer> getCurrentDomainId(String browserSessionKey) {
        GenericRpcResponse<Integer> response = new GenericRpcResponse<Integer>();

        final TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionKey);
        if (session != null) {
            UserWebSession userWebSession = TutorModule.getInstance().getUserSession(session.getUserSessionKey());
            if (userWebSession != null) {

                DomainWebState webState = userWebSession.getDomainWebState();
                if (webState != null) {
                    response.setWasSuccessful(true);
                    response.setContent(webState.getDomainSessionId());
                } else {
                    response.setWasSuccessful(false);
                }   
            } else {
                response.setWasSuccessful(false);
            }
        } else{
            response.setWasSuccessful(false);
        }
        return response;
    }
}
