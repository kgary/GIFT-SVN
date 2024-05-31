/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.util;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Element;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.RootPanel;

import generated.course.AuthoredBranch;
import generated.course.AuthoredBranch.Paths;
import generated.course.AuthoredBranch.Paths.Path;
import generated.course.BooleanEnum;
import generated.course.Course;
import generated.course.LtiProvider;
import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.LessonLevelEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.gwt.client.IFrameMessageHandlerChild;
import mil.arl.gift.common.gwt.client.JsniUtility;
import mil.arl.gift.common.gwt.client.iframe.IFrameMessageType;
import mil.arl.gift.common.gwt.client.iframe.messages.IFrameSimpleMessage;
import mil.arl.gift.common.gwt.client.lti.LtiClientUtility;
import mil.arl.gift.common.gwt.client.lti.LtiProviderJSO;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.survey.SurveyContext;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.place.CoursePlace;
import mil.arl.gift.tools.authoring.server.gat.client.place.DkfPlace;
import mil.arl.gift.tools.authoring.server.gat.client.place.GenericParamPlace;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.CourseElementUtil;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchGatServerProperties;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchGatServerPropertiesResult;

/**
 * Contains a bunch of utility methods that are generally helpful to the client
 * side of the GIFT authoring tool.
 *
 * While reading through the GIFT authoring tool code base I've found several
 * bloated classes that contain logic that isn't specifically relevant to the
 * class that contains said logic. My temporary solution is to relocate this
 * logic to this utility class. Perhaps in the future we can move this logic to
 * a more appropriate location but that'll require more thought and planned
 * refactor.
 *
 * @author elafave
 *
 */
public class GatClientUtility {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(GatClientUtility.class.getName());

    /**
     * A callback that is invoked if the server properties are changed
     * @author tflowers
     *
     */
    public interface ServerPropertiesChangeHandler {
        void onServerPropertiesChange(ServerProperties properties);
    }

	/** A URL token used to open the course editor */
    private final static String GAT_COURSE_TOKEN = "/#CoursePlace:";

    /** A URL token used to open the DKFeditor */
    private final static String GAT_DKF_TOKEN = "/#DkfPlace:";

    /** A URL token used to open the conversation editor */
    private final static String GAT_CONVERSATION_TOKEN = "/#ConversationPlace:";

    /** A URL token used to open the sensor configuration editor */
    private final static String GAT_SENSOR_CONFIG_TOKEN = "/#SensorsConfigurationPlace:";

    /** A URL token used to open the learner configuration editor */
    private final static String GAT_LEARNER_CONFIG_TOKEN = "/#LearnerConfigurationPlace:";

    /** A URL token used to open the pedagogy configuration editor */
    private final static String GAT_PEDAGOGY_CONFIG_TOKEN = "/#PedagogyConfigurationPlace:";

    /** A URL token used to open the metadata editor */
    private final static String GAT_METADATA_TOKEN = "/#MetadataPlace:";


    /**
     * The url parameter for redirecting to the Wrap landing page. Must match
     * {@link mil.arl.gift.tools.spl.SingleProcessLauncher#LAUNCH_WRAP_URL_PARAM} and
     * {@link mil.arl.gift.tools.controlpanel.ToolButtons#LAUNCH_WRAP_URL_PARAM}
     */
    public final static String GIFT_WRAP_LAUNCH_TOKEN = "launchwrap";

    /**
     * The url parameter for redirecting to the Wrap page to select an existing object
     */
    public final static String GIFT_WRAP_LAUNCH_SELECT_EXISTING_TOKEN = "selectexistingwrap";

    /** A URL token used to determine the course object to preview */
    public final static String PREVIEW_INDEX = "previewIndex";

    /**
     * must match
     * mil.arl.gift.tools.dashboard.client.bootstrap.BsMyToolsWidget#USERNAME_GAT_URL_PARAM
     */
    public final static String USER_NAME_URL_PARAM = "userName";

    /** must match mil.arl.gift.tools.dashboard.client.bootstrap.BsMyToolsWidget#DEBUG_URL_PARAM */
    private final static String DEBUG_URL_PARAM = "debug";

    /**
     * must match
     * mil.arl.gift.tools.dashboard.client.bootstrap.BsMyToolsWidget#BROWSER_SESSION_ID_URL_PARAM
     */
    private final static String BROWSER_SESSION_ID_PARAM = "browserSessionId";

    /**
     * The default username for a user that accesses GIFT Wrap through the system tray icon in
     * Desktop mode. Must match {@link GatRpcServiceImpl#GIFT_WRAP_DESKTOP_USER}.
     */
    public final static String GIFT_WRAP_DESKTOP_USER = "GIFT_Wrap_local-user";

    /**
     * The url parameter for filtering the objects on the GIFT Wrap page
     */
    public final static String GIFT_WRAP_TYPE_URL_PARAM = "wrapType";

	/**
	 * Used as the prefix to modals (e.g. course preview, dkf editor, etc.)
	 * e.g. http://localhost:8080/gat/?userName=mhoffman&browserSessionId=acc70157-f438-498d-adfa-a2a6f8110518
	 * This is the URL prior to the "#CoursePlace:..." that the GIFT Dashboard builds.
	 */
	private static String gatHostUrl = "";

	/** server properties that the client may need - can be null if not retrieved from server yet */
	public static ServerProperties properties = null;

	/** Keeps track of whether or not the course is open in read-only mode */
	private static boolean readOnly = false;

	/** String used as a tool tip for delete buttons in read only mode */
	public final static String CAN_NOT_DELETE_READ_ONLY = "Can not delete content in Read Only mode";

	/** The collection that contains all the registered ServerPropertiesChangeHandlers */
	private static Collection<ServerPropertiesChangeHandler> serverPropertiesChangeHandlers = new ArrayList<>();

	static{
		preventDefaultDropBehavior();
	}
	
	/**
	 * Sets the username of the current user, as resolved against an external SSO service
	 * 
	 * @param username the resolved username.
	 */
	public static native void setResolvedUsername(String username)/*-{
        $wnd.resolvedUsername = username;
    }-*/;
	
	/**
     * Gets the username of the current user, as resolved against an external SSO service
     * 
     * @return username the resolved username.
     */
	private static native String getResolvedUsername()/*-{
	
	    if($wnd.resolvedUsername == null){
	    
	        //If this window doesn't track the resolved username, check the parent window
	        $wnd.resolvedUsername = $wnd.parent.resolvedUsername;
	    }
	
        return $wnd.resolvedUsername;
    }-*/;

	/**
	 * Gets the user name.
	 * @return User name.
	 */
	public static String getUserName() {
	    
	    /* 
	     * If the user had their username resolved by an external SSO like keycloak, prefer their username in
	     * the SSO system. Otherwise, use whatever username in the URL was used to load the GIFT Authoring Tool.
	     * 
	     *  The URL username is used when opening the GAT from the Dashboard while the resolved username is used
	     *  only for external tools using SSO. 
	     */
	    String resolvedUsername = getResolvedUsername();
		String userName = resolvedUsername != null 
		        ? resolvedUsername
		        : Location.getParameter(USER_NAME_URL_PARAM);

        // return a default GIFT Wrap Desktop User if the following conditions are met:
        // 1. No username was provided
        // 2. GIFT is in Desktop Mode
        // 3. The GAT was accessed through GIFT Wrap
        if (StringUtils.isBlank(userName)) {
            if (properties == null || properties.getDeploymentMode() == DeploymentModeEnum.DESKTOP) {
                if (isGIFTWrapMode()) {
                    return GIFT_WRAP_DESKTOP_USER;
                }
            }
        }

		// NOTE:  There is an IE issue with nested IFrames that forced our URL construction to be:
        // http://<gathost:port>/gat/?userName=user#DkfPlace:<params> to
        // http://<gathost:port>/gat/?userName=user/#DkfPlace:<params>
        //
        // The extra "/" was added before the "#" to make the URLs load properly in a nested IFrame in IE.
        // However in doing that, the userName may now have an extra "/" character appended to it when
        // querying from the Location.getParameter("userName") method.
        // For now we will strip off any trailing "/" from the userName parameter if we find one on the URL.
        //
        // For more information on this issue:
        // Since the GAT Editors were moved into nested IFrames with the same src url, the original urls
        // are similar (same SRC url), so IE is blocking what it sees as a potential malicious recursion
        // and prevents the Iframe from being loaded:
        //   http://stackoverflow.com/questions/22126190/why-iframe-is-not-loaded-for-same-url-as-page?lq=1
        // By adding the "/" prior to the "#" IE treats the src url is now different since the #<place> is different and the iframe
        // loads fine.
		if (userName != null && !userName.isEmpty() &&
		        (userName.substring(userName.length() - 1).compareTo(Constants.FORWARD_SLASH) == 0 ||
		         userName.substring(userName.length() - 1).compareTo(Constants.BACKWARD_SLASH) == 0)) {

		    userName = userName.substring(0, userName.length() - 1);

		}

		return userName;
	}

	/**
	 * Returns whether the course object name is valid within a course. This method
	 * checks all course objects within the course and nested within authored branches
	 * and compares the names of those course objects with the specified name
	 *
	 * @param name the name of the course object to check is valid
	 * @param course the course containing the course objects to compare the name to
	 * @param thisTransitionToIgnore optional course object that should be ignored when checking names, this could
	 * be because the course object already exists in the course but it being changed to another name.  Can be null.
	 * @return whether the name would be unique among the course objects in the course
	 */
	public static boolean isCourseObjectNameValid(String name, Course course, Serializable thisTransitionToIgnore) {

	    if (StringUtils.isBlank(name)) {
	        return false;
	    }

        // check for duplicates among other course objects in the top level course
        for(Serializable otherTransition : course.getTransitions().getTransitionType()) {

            if(otherTransition.equals(thisTransitionToIgnore)){
                continue;
            }

            String otherTransitionName = getTransitionName(otherTransition);

            if(otherTransitionName != null && name.equals(otherTransitionName.trim())) {
                return false;
            }

            if (otherTransition instanceof AuthoredBranch) {
                if (!isCourseObjectNameValid(name, (AuthoredBranch)otherTransition, thisTransitionToIgnore)) {
                    return false;
                }
            }
        }

	    return true;
	}

	/**
	 * Returns whether the course object name is valid within an authored branch. This
	 * method checks all course objects within the authored branch and nested within
	 * authored branches and compares the names of those course objects with the
	 * specified name
	 *
	 * @param name the name of the course object to check is valid
	 * @param branch the authored branch containing the course objects to compare the name to
	 * @param thisTransitionToIgnore optional course object that should be ignored when checking names, this could
     * be because the course object already exists in the course but it being changed to another name.  Can be null.
	 * @return whether the name would be unique among the course objects in the course
	 */
	private static boolean isCourseObjectNameValid(String name, AuthoredBranch branch, Serializable thisTransitionToIgnore) {

	    if (StringUtils.isBlank(name)) {
	        return false;
	    }

        if (branch != null) {
            for (Path path : branch.getPaths().getPath()) {
                for (Serializable otherTransition : path.getCourseobjects().getAAROrAuthoredBranchOrEnd()) {

                    if(otherTransition.equals(thisTransitionToIgnore)){
                        continue;
                    }

                    String otherTransitionName = getTransitionName(otherTransition);

                    if (otherTransitionName != null && name.equals(otherTransitionName.trim())) {
                        return false;
                    }

                    if (otherTransition instanceof AuthoredBranch) {
                        if (!isCourseObjectNameValid(name, (AuthoredBranch)otherTransition, thisTransitionToIgnore)) {
                            return false;
                        }
                    }
                }
            }
        }

	    return true;

    }
	
    /**
     * Gets whether or not GIFT's current lesson level is set to Real-Time Assessment (RTA)
     *  
     * @return whether RTA is set as GIFT's current lesson level
     */
    public static boolean isRtaLessonLevel() {
        if (LessonLevelEnum.RTA.equals(properties.getLessonLevel())) {
            return true;
        }
        
        return false;
    }

    /**
     * Checks if the JRE is running in 64-bit.
     * https://docs.oracle.com/javame/config/cdc/cdc-opt-impl/ojmeec/1.1/architecture/html/properties.htm#g1001328
     * 
     * @return true if running in 64-bit; false otherwise.
     */
    public static boolean isJRE64Bit() {
        return properties.isJRE64Bit();
    }

    /**
	 * Whether the GAT is in debug mode.
	 *
	 * @return true if the GAT should render in debug mode (e.g. color code scored answers in surveys)
	 */
    public static boolean isDebug(){

        String debugFlag = Location.getParameter(DEBUG_URL_PARAM);

        // NOTE:  There is an IE issue with nested IFrames that forced our URL construction to be:
        // http://<gathost:port>/gat/?userName=user#DkfPlace:<params> to
        // http://<gathost:port>/gat/?userName=user/#DkfPlace:<params>
        //
        // The extra "/" was added before the "#" to make the URLs load properly in a nested IFrame in IE.
        // However in doing that, the parameter may now have an extra "/" character appended to it when
        // querying from the Location.getParameter() method.
        // For now we will strip off any trailing "/" from the parameter if we find one on the URL.
        //
        // For more information on this issue:
        // Since the GAT Editors were moved into nested IFrames with the same src url, the original urls
        // are similar (same SRC url), so IE is blocking what it sees as a potential malicious recursion
        // and prevents the Iframe from being loaded:
        //   http://stackoverflow.com/questions/22126190/why-iframe-is-not-loaded-for-same-url-as-page?lq=1
        // By adding the "/" prior to the "#" IE treats the src url is now different since the #<place> is different and the iframe
        // loads fine.
        if (debugFlag != null && !debugFlag.isEmpty() &&
                (debugFlag.substring(debugFlag.length() - 1).compareTo(Constants.FORWARD_SLASH) == 0 ||
                 debugFlag.substring(debugFlag.length() - 1).compareTo(Constants.BACKWARD_SLASH) == 0)) {

            debugFlag = debugFlag.substring(0, debugFlag.length() - 1);

        }

        return debugFlag != null && Boolean.valueOf(debugFlag);
    }

	/**
	 * Returns the unique identifier for the user's current
	 * browser
	 * @return the browser's unique identifier
	 */
	public static String getBrowserSessionKey() {
	    return Location.getParameter(BROWSER_SESSION_ID_PARAM);
	}

	/**
	 * Returns true if the preview parameter is true
	 *
	 * @return true if the preview parameter is true, false otherwise.
	 */
	public static boolean isPreviewMode() {
		String token = Location.getHash();

		logger.info("Checking if in course preview mode on token = "+token);

		// Get the preview key from the url parameters
		HashMap<String, String> map = PlaceParamParser.getParams(token);
		String preview = map.get("preview");

		return preview != null && preview.equalsIgnoreCase("true");
	}

    /**
     * Retrieves the GIFT Wrap type url parameter from the {@link Location} if it exists.
     *
     * @return the GIFT Wrap {@link TrainingApplicationEnum} type or null if it cannot be found.
     */
    public static TrainingApplicationEnum getGIFTWrapType() {
        String wrapType = Location.getParameter(GIFT_WRAP_TYPE_URL_PARAM);
        if (wrapType == null) {
            return null;
        }

        try {
            return TrainingApplicationEnum.valueOf(wrapType);
        } catch (@SuppressWarnings("unused") EnumerationNotFoundException nfe) {
            return null;
        }
    }

    /**
     * Returns the flag indicating if GIFT is running in GIFT Wrap mode.
     *
     * @return true if the gift wrap parameter is true; false otherwise.
     */
    public static boolean isGIFTWrapMode() {
        String token = Location.getHash();

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("isGIFTWrapMode() - token: " + token);
        }

        // no hash token
        if (StringUtils.isBlank(token) || StringUtils.equals(token, "#")) {
            return false;
        } else if (token.startsWith("#")) {
            /* remove the preceeding hash; Location.getHash() says it returns the string to the
             * right of the hash, but it also includes the hash */
            token = token.substring(1);
        }

        // remove training slash
        if (token.endsWith(Constants.FORWARD_SLASH)) {
            token = token.substring(0, token.length() - 1);
        }

        /* if the token is equal to the launch param, then we are in GIFT Wrap mode. */
        if (StringUtils.equals(token, GIFT_WRAP_LAUNCH_TOKEN)
                || StringUtils.equals(token, GIFT_WRAP_LAUNCH_SELECT_EXISTING_TOKEN)) {
            return true;
        }

        /* check if the url parameters contains a flag indicating that we are in GIFT Wrap mode */
        HashMap<String, String> map = PlaceParamParser.getParams(token);
        String launchWrap = map.get(DkfPlace.PARAM_GIFTWRAP);
        return StringUtils.equals(launchWrap, Boolean.TRUE.toString());
    }

    /**
	 * Prevents the default drag-and-drop behavior performed when dragging files onto the browser. This keeps
	 * the browser from trying to navigate away from the GAT whenever the user drags a file with a supported MIME type
	 * (i.e. plain text, XML, HTML) onto it.
	 */
	private static native void preventDefaultDropBehavior()/*-{

		$wnd.addEventListener("dragover", function(e) {
			e = e || event;
			e.preventDefault();
		}, false);

		$wnd.addEventListener("drop", function(e) {
			e = e || event;
			e.preventDefault();
		}, false);

    }-*/;


	/**
     * Gets the server properties.
     *
     * @return the server properties or null if there was an error on the server.
     */
	public static ServerProperties getServerProperties() {
		return properties;
	}

	/**
	 * Registers a handler that will be invoked any time the server properties are changed.
	 * The handler will also be immediately invoked if the value for the server properties
	 * is a non-null value already.
	 * @param handler the handler to register.  Can't be null.
	 */
	public static void addServerPropertiesChangeHandler(ServerPropertiesChangeHandler handler) {

	    if(handler == null) {
	        throw new IllegalArgumentException("The value of 'handler' can't be null");
	    }

	    if(properties != null) {
	        handler.onServerPropertiesChange(properties);
	    }

	    serverPropertiesChangeHandlers.add(handler);
	}

	/**
	 * Initializes server properties by retrieving them from the server.
	 * @param callback the callback that is invoked after the server properties
	 * have been received, can be null.<br/>
	 * <b>NOTE:</b> the callback should not contain
	 * any logic that renders the UI as it could be a significant amount of time before
	 * the callback is invoked<br/>
	 * <b>Note:</b> if the server properties have already been returned by the server, this method
	 * does nothing but callback.onSuccess().
	 */
	public static void initServerProperties(final AsyncCallback<Void> callback) {

	    // set the GAT host URL to what comes before the "#CoursePlace:..." in the URL the GIFT dashboard builds
        // (e.g. http://localhost:8080/dashboard/GiftAuthoringTool.html/?userName=mhoffman&browserSessionId=acc70157-f438-498d-adfa-a2a6f8110518 )
        gatHostUrl = Window.Location.getHref().replace(Window.Location.getHash(), "");
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("initServerProperties() - GAT HOST URL = "+gatHostUrl);
        }
        
	    if(properties != null){
	        callback.onSuccess(null);
	        return;
	    }

        ServerProperties storedProps = JsniUtility.getServerProperties();
		
        if(storedProps != null) {
            
            // If server properties have been saved to local session storage, then re-use them.
            // This saves a server call for GAT clients embedded in the Dashboard or other GAT clients.
            setServerProperties(storedProps);
            
            if (callback != null) {
                callback.onSuccess(null);
            }
            
        } else {
        
            // If no server properties have been saved to local session storage, request them from the server.
            // This is needed for the base window of GIFT Wrap to retrieve its properties.
		SharedResources.getInstance().getDispatchService().execute(new FetchGatServerProperties(), new AsyncCallback<FetchGatServerPropertiesResult>(){

            @Override
            public void onFailure(Throwable t) {
            	logger.warning("Failed to retrieve server properties: " + t);
            	if (callback != null) {
            	    callback.onFailure(t);
            	}
            }

            @Override
            public void onSuccess(FetchGatServerPropertiesResult result) {

                if (result.isSuccess()) {
                        
                        ServerProperties props = result.getServerProperties();
                        setServerProperties(props);

                        //save the server properties to local session storage so that child iframes can access them
                        JsniUtility.setServerProperties(props);
                    	
                	if (callback != null) {
                        callback.onSuccess(null);
                    }
                    	
                } else {
                	logger.warning("Failed to retrieve server properties: " + result.getErrorMsg());
                	if (callback != null) {
                        callback.onFailure(new Exception(result.getErrorMsg()));
                    }
                }
                    
            }
            });    	
	}
	}

	/**
	 * Sets the server properties that this client should share throughout the GAT
	 * 
	 * @param props the properties to save. Can be null.
	 */
	private static void setServerProperties(ServerProperties props) {
        
        logger.info("Setting server properties\n" + props);
	    
	    properties = props;
        
        //Informs all subscribers that the property values have changed
        for(ServerPropertiesChangeHandler handler : serverPropertiesChangeHandlers) {
            try {
                handler.onServerPropertiesChange(properties);
            } catch(Throwable t) {
                logger.log(Level.SEVERE, "There was an exception while executing a ServerPropertiesChangeHandler", t);
            }
        }
	}
	
	/**
	 * Retrieves a course folder name based on a relative path that is passed in.
	 * $TODO$ we may need to tweak this logic based on folders such as templates.
	 *
	 * @param relativePath - Workspace relative path.
	 * @return - The course folder name based on the path if found.   An empty string is returned if it can't be found.
	 */
	public static String getCourseFolderName(String relativePath) {

	    final int COURSE_FOLDER_INDEX = 1;
	    String courseFolderName = "";

	    if (!relativePath.isEmpty()) {
	    	if(relativePath.startsWith("/")) {
	    		relativePath = relativePath.substring(1);
	    	}

	        String[] directories = relativePath.split("/");

	        if (directories.length > COURSE_FOLDER_INDEX) {
	            courseFolderName = directories[COURSE_FOLDER_INDEX];
	        }
	    }

	    return courseFolderName;

	}


	/**
	 * Converts a path for a course item in one path into a course folder that is
	 * relative to the user's workspace.  For example:
	 *    If relativePath is a path to an item in the public location: "Public/CourseA/my.course.xml
	 *    The path returned will be converted to the user's workspace: "username/CourseA"

	 * @param relativePath - Workspace relative path for a file/folder in another location (see example in the description above)
	 * @return - A workspace relative path of the course folder under the user's workspace (see example in the description above)
	 */
	public static String convertPathToUserCourseFolder(String relativePath) {
	    String courseFolderPath = "";

	    String courseFolderName = getCourseFolderName(relativePath);
	    String userName = getUserName();

	    if (!courseFolderName.isEmpty() && !userName.isEmpty()) {
	        courseFolderPath = userName + "/" + courseFolderName;
	    }
	    return courseFolderPath;
	}

	/**
     * Alters the appearance of the cursor to indicate waiting state.
     *
     * @param waiting whether or not we are waiting
     */
	public static void showWaiting(boolean waiting){

		if(waiting) {
			RootPanel.getBodyElement().getStyle().setProperty("cursor", "progress");

		} else {
			RootPanel.getBodyElement().getStyle().setProperty("cursor", "default");
		}
	}

	/**
	 * Returns the user to the My Courses screen on the GIFT dashboard.
	 */
	public static void returnToDashboard() {
	    logger.info("returnToDashboard() called.");
		IFrameMessageHandlerChild.getInstance().sendMessage(new IFrameSimpleMessage(IFrameMessageType.GO_TO_DASHBOARD));
	}

	/**
	 * Removes contexts from list that the current user does not have visibility for
	 *
	 * @param surveyContextList List to be filtered
	 * @return list with context not visible to current user filtered out
	 */
	public static List<SurveyContext> filterContextsWithoutPermission(List<SurveyContext> surveyContextList){
		for(int i = 0; i < surveyContextList.size(); i++){
			if(surveyContextList.get(i) == null || !surveyContextList.get(i).getVisibleToUserNames().contains(GatClientUtility.getUserName()) && !surveyContextList.get(i).getVisibleToUserNames().contains(mil.arl.gift.common.survey.Constants.VISIBILITY_WILDCARD)){
				surveyContextList.remove(i);
				i--;
			}
		}
		return surveyContextList;
	}

	/**
	 * Check to see if the current user has permission to the Selected Survey context
	 * @param context context to check permissions on
	 * @return true if user has permissions or it is a public context, false if not
	 */
	public static boolean hasPermissionToSurveyContext(SurveyContext context){
		return context.getVisibleToUserNames().contains(GatClientUtility.getUserName()) || context.getVisibleToUserNames().contains(mil.arl.gift.common.survey.Constants.VISIBILITY_WILDCARD) ?
				true : false;

	}

	/**
	 * Saves a course object reference that is currently loaded in the window (e.g. modal).  A reference
	 * might be a dkf.xml, conversationTree.xml, etc.  If this method is not called when 'save and close'
	 * then changes to that reference won't be written to disk.
	 */
	public static native void saveEmbeddedCourseObject()/*-{
		try {

			if ($wnd.saveEmbeddedCourseObject != null) {
				$wnd.saveEmbeddedCourseObject();
			} else if ($wnd.parent.saveEmbeddedCourseObject != null) {
				$wnd.parent.saveEmbeddedCourseObject();
			}

		} catch (exception) {
			// if the course presenter accesses wnd.parent, a 'Permission Denied' exception is thrown
			$wnd.saveEmbeddedCourseObject();
		}
    }-*/;

	 /**
     * Saves the course in the background and displays a save notification
     */
    public static native void saveCourseAndNotify()/*-{

		if ($wnd.saveCourseAndNotify != null) {
			$wnd.saveCourseAndNotify();
		}

    }-*/;

	/**
	 * Defines the save method used to save an embedded course object.
	 *
	 * @param saveObj The function that should be called to save an embedded course object
	 */
	public static native void defineSaveEmbeddedCourseObject(JavaScriptObject saveObj)/*-{
		if ($wnd.parent != null) {
			$wnd.parent.saveEmbeddedCourseObject = saveObj;
		}
    }-*/;

	/**
	 * Defines the method called by the additional button on the course object modal
	 *
	 * @param actionObj The function that should be called by the additional button on the course object modal
	 */
	public static native void defineAdditionalButtonAction(JavaScriptObject actionObj)/*-{
		if ($wnd.parent != null) {
			$wnd.parent.additionalButtonAction = actionObj;
		}
    }-*/;

	/**
	 * Calls the method attached to the additional button on the course object modal
	 */
	public static native void additionalButtonAction()/*-{
		if ($wnd.additionalButtonAction != null) {
			$wnd.additionalButtonAction();
		}
    }-*/;

	/**
	 * Returns a url to the newly created file
	 *
	 * @param courseFolderPath The path to the course folder
	 * @param baseName The base name of this file
	 * @param editorExtension The file type to create
	 * @return the url to the new file
	 */
	public static String createModalDialogUrl(String courseFolderPath, String baseName, String editorExtension) {
		Date date = new Date();
		DateTimeFormat format = DateTimeFormat.getFormat("ddMMyyhhmmss");
		return createModalDialogUrl(courseFolderPath +"/"+ baseName + "_" + format.format(date) + editorExtension, true);
	}

	/**
     * Returns a url to the newly created file but allows passing additional
     * parameters in the url via the GWT Places methodology that the 'place'
     * may need (DkfPlace, CoursePlace, etc).
     *
     * @param courseFolderPath The path to the course folder
     * @param baseName The base name of this file
     * @param editorExtension The file type to create
     * @param paramMap Mapping of additional parameters that will be encoded into the url (optional).
     * @return the url to the new file
     */
    public static String createModalDialogUrlWithParams(String courseFolderPath, String baseName, String editorExtension, HashMap<String, String> paramMap) {
        Date date = new Date();
        DateTimeFormat format = DateTimeFormat.getFormat("ddMMyyhhmmss");
        return createModalDialogUrlWithParams(courseFolderPath +"/"+ baseName + "_" + format.format(date) + editorExtension, true, paramMap);
    }

	/**
	 * Returns a url to the specified file
	 *
	 * @param coursePath The path to the course folder
	 * @param fileName The file path relative to the course folder
	 * @return the url to the new file
	 */
	public static String getModalDialogUrl(String coursePath, String fileName) {
		return createModalDialogUrl(coursePath +"/"+ fileName, false);
	}

	/**
	 * Prompts the user to save and preview the course
	 *
	 * @param index The index of the course object to preview
	 */
	public static native void showPreviewDialog(int index) /*-{
		$wnd.previewCourse(index);
    }-*/;

	/**
	 * Opens a preview window that will display the course object at the specified index.
	 *
	 * @param index The index of the course object to preview
	 * @param filePath The file path of the course
	 */
	public static void previewCourseObject(int index, String filePath) {

		HashMap<String, String> paramMap = new HashMap<>();
		paramMap.put(CoursePlace.PARAM_FILEPATH, filePath);
		paramMap.put(PREVIEW_INDEX, Integer.toString(index));
		paramMap.put("preview", "true");

		String editorToken = GAT_COURSE_TOKEN;
		String params = PlaceParamParser.encodeTokenParameters(paramMap);
		StringBuilder urlToOpen = new StringBuilder(gatHostUrl);
		urlToOpen.append(editorToken);
		urlToOpen.append(params);

		openPreviewWindow(urlToOpen.toString());
	}

	private static native void openPreviewWindow(String url) /*-{
		var win = window.open("", "Course Preview", "location=no, resizable=yes");
		if (win.document == null) {
			window.alert("Please allow popups to view the Course Preview.");
		} else {
			win.document.title = 'Course Preview';
			win.document.body.innerHTML = "<iframe style=\"background: gray; border: none; top: 0; bottom: 0; left: 0; right: 0; position: absolute; height: 100%; width: 100%;\" allowfullscreen=\"\" src=\"" + url + "\" allow=\"autoplay\"></iframe>";
			win.gatWindow = $wnd;
			win.isPreviewWindow = true;
			$wnd.previewWindow = win;
		}
    }-*/;

	/**
	 * Closes the preview window.
	 */
	public static native void closePreviewWindow() /*-{
		$wnd.parent.gatWindow.closePreviewWindow();
    }-*/;

	/**
     * Returns a url to the specified file but allows passing additional
     * parameters in the url via the GWT Places methodology that the 'place'
     * may need (DkfPlace, CoursePlace, etc).
     *
     * @param coursePath The path to the course folder
     * @param fileName The file path relative to the course folder
     * @param paramMap Mapping of additional parameters that will be encoded into the url (optional).
     * @return the url to the new file
     */
    public static String getModalDialogUrlWithParams(String coursePath, String fileName, HashMap<String, String> paramMap) {
        return createModalDialogUrlWithParams(coursePath +"/"+ fileName, false, paramMap);
    }

	/**
     * Extracts the path to the file being edited by a modal relative to the
     * course folder.
     *
     * <h1>Example</h1>
     *
     * <code><pre>
     * String fileName = getFilenameFromModalUrl(
     *     "tflowers/4553/"
     *     "http://localhost:8080/gat/?userName=tflowers&amp;browserSessionId=e207b299-915b-4e89-a47a-6299b28a2244&amp;debug=false/#PedagogyConfigurationPlace:filePath=tflowers%2F4553%2FPed_060420113040.pedagogicalconfig.xml|deployMode=Desktop|createNew=false",
     *     ".pedagogicalconfig.xml");
     *
     * // fileName's value is "Ped_060420113040.pedagogicalconfig.xml"
     * </pre></code>
     *
     * @param courseFolderPath The path to the course folder relative to the
     *        workspaces folder. A trailing slash on the path is optional. This
     *        path should not be URL encoded. Can't be null.
     * @param url The URL that is provided to a modal in order to load the
     *        editor. This is the URL from which the file path will be
     *        extracted. Can't be null.
     * @param fileExtension The extension of the file path that is being
     *        extracted, including the dot. Can't be null.
     * @return A decoded path to the file relative to the course folder. Can't
     *         be null.
     */
	public static String getFilenameFromModalUrl(String courseFolderPath, String url, String fileExtension) {
        /* Make sure the course folder path ends with a slash */
        courseFolderPath = courseFolderPath.endsWith("/") ? courseFolderPath : courseFolderPath + "/";

        /* Determine the values of the provided strings within the URL */
        final String encodedCourseFolderPath = URL.encodeQueryString(courseFolderPath);
        final String encodedFileExtension = URL.encodeQueryString(fileExtension);

        /* Calculate the indices at which to slice within the URL */
        final int courseFolderPathStart = url.lastIndexOf(encodedCourseFolderPath);
        final int filePathStart = courseFolderPathStart + encodedCourseFolderPath.length();
        final int filePathEnd = url.lastIndexOf(encodedFileExtension) + encodedFileExtension.length();

        /* Return the decoded string slice */
        return URL.decodeQueryString(url.substring(filePathStart, filePathEnd));
    }

	/**
	 * Gets the url to the file
	 *
	 * @param filename The name of the file
	 * @param createNew Whether or not a new file should be created
	 * @return The url to the file
	 */
	private static String createModalDialogUrl(String filename, boolean createNew) {

		String editorToken = generateEditorTokenFromFileName(filename);

		HashMap<String, String> paramMap = new HashMap<>();
		paramMap.put(DkfPlace.PARAM_FILEPATH, filename);
		paramMap.put(DkfPlace.PARAM_DEPLOYMODE,  properties.getDeploymentMode().toString());
        paramMap.put(GenericParamPlace.PARAM_CREATENEW, Boolean.toString(createNew));

        String params = PlaceParamParser.encodeTokenParameters(paramMap);
        StringBuilder urlToOpen = new StringBuilder(gatHostUrl);
        urlToOpen.append(editorToken);
		urlToOpen.append(params);

		return urlToOpen.toString();
	}

    /**
     * Builds the URL to use for the GIFT Wrap modal.
     *
     * @param trainingApplicationType the optional parameter field to specify the type of training
     *        application to be viewed/edited. If null, all types are allowed.
     * @return the url
     */
    public static String createGIFTWrapModalUrl(TrainingApplicationEnum trainingApplicationType) {
        StringBuilder sb = new StringBuilder();

        // add path to GAT
        sb.append(gatHostUrl);

        // add parameters
        sb.append("?");
        sb.append(GatClientUtility.USER_NAME_URL_PARAM).append("=").append(getUserName());

        // option field
        if (trainingApplicationType != null) {
            sb.append(Constants.AND).append(GatClientUtility.GIFT_WRAP_TYPE_URL_PARAM).append("=")
                    .append(trainingApplicationType);
        }

        // add token
        sb.append("#").append(GatClientUtility.GIFT_WRAP_LAUNCH_SELECT_EXISTING_TOKEN);

        return sb.toString();
    }

	/**
	 * Private function used to generate the editor place token from the file extension that is being
	 * retrieved from the server.
	 *
	 * @param filename The filename to retrieve from the server.
	 *
	 * @return String The editor place token that will need to be appended to the url.
	 */
	private static String generateEditorTokenFromFileName(String filename) {
	    String editorToken;
        if(filename.endsWith(AbstractSchemaHandler.COURSE_FILE_EXTENSION)) {
            editorToken = GAT_COURSE_TOKEN;
        } else if(filename.endsWith(AbstractSchemaHandler.DKF_FILE_EXTENSION)) {
            editorToken = GAT_DKF_TOKEN;
        } else if(filename.endsWith(AbstractSchemaHandler.METADATA_FILE_EXTENSION)) {
            editorToken = GAT_METADATA_TOKEN;
        } else if(filename.endsWith(AbstractSchemaHandler.SENSOR_CONFIG_FILE_EXTENSION)) {
            editorToken = GAT_SENSOR_CONFIG_TOKEN;
        } else if(filename.endsWith(AbstractSchemaHandler.LEARNER_CONFIG_FILE_EXTENSION)) {
            editorToken = GAT_LEARNER_CONFIG_TOKEN;
        } else if(filename.endsWith(AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION)) {
            editorToken = GAT_CONVERSATION_TOKEN;
        } else if(filename.endsWith(AbstractSchemaHandler.EMAP_PEDAGOGICAL_CONFIG_FILE_EXTENSION)){
            editorToken = GAT_PEDAGOGY_CONFIG_TOKEN;
        } else {
            WarningDialog.warning("Unrecognized file type", "This file cannot be opened because it is not a file that can be authored in the GIFT Authoring Tool.<br/>"
                    + "<br/>"
                    + "In the future, GIFT will support previewing the contents of files like these.");
            return null;
        }

        return editorToken;
	}


	/**
     * Gets the url to the file by constructing the url plus the place and place parameters.
     * There are some core parameters that are always put on the url, but this method allows the input
     * of additional parameters (optionally).
     *
     * @param filename The name of the file
     * @param createNew Whether or not a new file should be created
     * @param paramMap Mapping of additional parameters that will be encoded into the url (optional).
     * @return The url to the file
     */
	private static String createModalDialogUrlWithParams(String filename, boolean createNew, HashMap<String, String> paramMap) {
	    if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("createModalDialogUrlWithParams(");
            List<Object> params = Arrays.<Object>asList(filename, createNew, paramMap);
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

	    HashMap<String, String> fullParams = new HashMap<>();

	    fullParams.put(DkfPlace.PARAM_FILEPATH, filename);
	    fullParams.put(DkfPlace.PARAM_DEPLOYMODE,  properties.getDeploymentMode().toString());
	    fullParams.put(GenericParamPlace.PARAM_CREATENEW, Boolean.toString(createNew));

	    if (paramMap != null && !paramMap.isEmpty()) {
	        fullParams.putAll(paramMap);
	    }


        String editorToken = generateEditorTokenFromFileName(filename);
        String params = PlaceParamParser.encodeTokenParameters(fullParams);
        StringBuilder urlToOpen = new StringBuilder(gatHostUrl);
        // prevent duplicating the slash
        if (editorToken.startsWith("/")) {
            editorToken = editorToken.substring(1);
        }
        urlToOpen.append(editorToken);
        urlToOpen.append(params);

        return urlToOpen.toString();
	}

	/**
	 * Gets the window corresponding to the course editor that contains the course currently being modified. This can be handy
	 * for distinguishing whether a window contains a sub-editor or not, since the topmost editor should ALWAYS contain a course.
	 * <br>
	 * In the case of the preview window, this method returns the current window.
	 *
	 * @return the window corresponding to the course editor that contains the course currently being modified, or null
	 * if no such window could be found (this should never happen when invoked from any editor window)
	 */
	public static native JavaScriptObject getBaseEditorWindow()/*-{

		var currentWnd = $wnd;

		try {
			if (currentWnd.parent.isPreviewWindow) {
				return currentWnd;
			}
		} catch (e) {
			// An Access Denied exception is thrown when the current window is the base course editor window.
			// In which case, currentWnd should be returned.
		}

		while (currentWnd != null) {

			if (currentWnd.editorBaseCoursePath) {
				return currentWnd;

			} else {
				if (currentWnd != currentWnd.parent) {
					// In case where a window does not have a parent, window.parent contains a reference to itself
					currentWnd = currentWnd.parent;
				} else {
					return currentWnd;
				}
			}
		}

		return null;

    }-*/;
	
	/**
     * Gets the window surrounding the editor that this method is invoked from.
     *
     * @return the window corresponding to this editor. Cannot be null.
     */
	public static native JavaScriptObject getEditorWindow()/*-{
        return $wnd;
    }-*/;


    /**
     * Gets the path to the folder of the course file currently being edited by the base editor to
     * which a sub-editor's files should be saved.
     *
     * @return the path to the folder of the course file currently being edited by the base editor.
     */
    public static native String getBaseCourseFolderPath()/*-{

		var currentWnd = $wnd;

		while (currentWnd != null) {

			if (currentWnd.editorBaseCoursePath) {
				return currentWnd.editorBaseCoursePath;
			} else if (currentWnd == currentWnd.parent) {
				// In case where a window does not have a parent, window.parent contains a reference to itself
				break;
			} else {
				currentWnd = currentWnd.parent;
			}
		}

		return null;

    }-*/;
    
    /**
     * Gets the URL where media files inside the course folder can be retrieved from. This is needed
     * to allow the survey composer to display media from within the course folder
     *
     * @return the URL used to reach media files in the course folder.
     */
    public static native String getBaseCourseFolderUrl()/*-{

        var currentWnd = $wnd;

        while (currentWnd != null) {

            if (currentWnd.editorBaseCourseUrl) {
                return currentWnd.editorBaseCourseUrl;
            } else if (currentWnd == currentWnd.parent) {
                // In case where a window does not have a parent, window.parent contains a reference to itself
                break;
            } else {
                currentWnd = currentWnd.parent;
            }
        }

        return null;

    }-*/;

	/**
	 * Gets the survey context ID being used by the course file currently being edited by the base editor.
	 *
	 * @return the survey context ID
	 */
	public static BigInteger getBaseCourseSurveyContextId(){

		String idString = getBaseCourseSurveyContextIdString();

		if(idString != null){

			try{
				return new BigInteger(idString);

			} catch(@SuppressWarnings("unused") NumberFormatException e){
				return null;
			}

		} else {

			return null;
		}
	}

	/**
	 * Gets the string representation of the survey context ID being used by the course file currently being edited by the base editor.
	 *
	 * @return the survey context ID string
	 */
	private static native String getBaseCourseSurveyContextIdString()/*-{

		var currentWnd = $wnd;

		while (currentWnd != null) {

			if (currentWnd.getBaseCourseSurveyContextId) {
				return currentWnd.getBaseCourseSurveyContextId();

			} else {
				currentWnd = currentWnd.parent;
			}
		}

		return null;

    }-*/;

	/**
	 * Get the name of the course opened in the GAT.
	 *
	 * @return the name of the opened course.  Null if not set.
	 */
	public static String getCourseName(){

	    String courseName = getBaseCourseName();

	    return courseName;
	}

	/**
     * Gets the course name currently opened in the GAT.
     *
     * @return the course name
     */
    private static native String getBaseCourseName()/*-{

		var currentWnd = $wnd;

		while (currentWnd != null) {

			if (currentWnd.getBaseCourseName) {
				return currentWnd.getBaseCourseName();

			} else if (currentWnd == currentWnd.parent) {

				// In case where a window does not have a parent, window.parent contains a reference to itself
				break;

			} else {
				currentWnd = currentWnd.parent;
			}

		}

		return null;

    }-*/;

	/**
	 * Gets the concepts being used by the course file currently being edited by the base editor.
	 *
	 * @return the course concepts, can be null.
	 */
	public static List<String> getBaseCourseConcepts(){

		JsArrayString conceptsArray = getBaseCourseConceptsArray();

		if(conceptsArray != null){

			try{
				List<String> concepts = new ArrayList<>();

				for(int i = 0; i < conceptsArray.length(); i++){
					concepts.add(conceptsArray.get(i));
				}

				return concepts;

			} catch(@SuppressWarnings("unused") NumberFormatException e){
				return null;
			}

		} else {

			return null;
		}
	}

	/**
     * Gets the LTI providers being used by the course file currently being edited by the base editor.
     *
     * @return the course LTI providers, can be null.
     */
    private static List<LtiProvider> getBaseLtiProviders(){

        JsArray<LtiProviderJSO> ltiProvidersArray = getBaseLtiProvidersArray();

        if(ltiProvidersArray != null){

            try {
                List<LtiProvider> ltiProviders = new ArrayList<>();

                for (int i = 0; i < ltiProvidersArray.length(); i++) {
                    LtiProviderJSO providerJso = ltiProvidersArray.get(i);
                    LtiProvider provider = new LtiProvider();
                    provider.setIdentifier(providerJso.getName());
                    provider.setKey(providerJso.getKey());
                    provider.setSharedSecret(providerJso.getSharedSecret());

                    if (providerJso.getProtectClientData() == null) {
                        provider.setProtectClientData(BooleanEnum.FALSE);
                    } else {
                        try {
                            provider.setProtectClientData(BooleanEnum.fromValue(providerJso.getProtectClientData()));
                        } catch (Exception e) {
                            if (logger.isLoggable(Level.INFO)) {
                                logger.log(Level.INFO,
                                        "Exception caught when trying to parse the provider's 'protectClientData' flag. Defaulting to False. Message: "
                                                + e.getMessage(),
                                        e);
                            }

                            provider.setProtectClientData(BooleanEnum.FALSE);
                        }
                    }

                    ltiProviders.add(provider);
                }

                return ltiProviders;

            } catch (Exception e) {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("Exception caught when trying to get base LTI providers. Message: " + e.getMessage());
                }
                return null;
            }

        } else {

            return null;
        }
    }

    /**
     * Gets the LTI providers in the course common.properties file.
     *
     * @return the course LTI providers from the server properties. Can be null.
     */
    private static List<LtiProvider> getServerPropertiesLtiProviders(){

        if (properties != null) {

            try {
                String providersJson = properties.getPropertyValue(ServerProperties.TRUSTED_LTI_PROVIDERS);
                if (providersJson != null) {
                    return LtiClientUtility.getLtiProviderList(providersJson);
                }
            } catch (Exception e) {

                // improperly formatted providers could throw JavaScriptExceptions, so catch them
                // just in case
                logger.severe(e.toString());
            }
        }

        return null;
    }

    /**
     * Gets the LTI providers being used by the course file currently being edited by the base
     * editor.
     *
     * @return the course LTI providers. Can be null.
     */
    public static List<LtiProvider> getCourseLtiProviders() {

        List<LtiProvider> ltiPropertyProviders = getServerPropertiesLtiProviders();
        List<LtiProvider> ltiCourseProviders = getBaseLtiProviders();

        // Set will only have unique values based on comparator; TreeSet will sort.
        Set<LtiProvider> providerSet = new TreeSet<>(new Comparator<LtiProvider>() {

            @Override
            public int compare(LtiProvider provider1, LtiProvider provider2) {
                return provider1.getIdentifier().compareTo(provider2.getIdentifier());
            }

        });

        if (ltiPropertyProviders != null) {
            providerSet.addAll(ltiPropertyProviders);
        }

        if (ltiCourseProviders != null) {
            providerSet.addAll(ltiCourseProviders);
        }

        return new ArrayList<>(providerSet);
    }

    /**
	 * Sets whether or not the editor is open in read-only mode
	 *
	 * @param readOnlyValue true if the course is read-only, false otherwise
	 */
	public static native void setReadOnly(boolean readOnlyValue)/*-{
		$wnd.readOnly = readOnlyValue;
		@mil.arl.gift.tools.authoring.server.gat.client.provider.CourseReadOnlyProvider::getInstance()().@mil.arl.gift.tools.authoring.server.gat.client.provider.CourseReadOnlyProvider::setReadOnly(Z)(readOnlyValue);
    }-*/;

	/**
	 * Gets whether or not the course is open in read-only mode.
	 *
	 * @return true if the course is read-only, false otherwise.
	 */
    public static native boolean isReadOnly()/*-{
		var currentWnd = $wnd;

		while (currentWnd != null) {

			if (currentWnd.readOnly != null) {
				return currentWnd.readOnly;
			} else if (currentWnd == currentWnd.parent) {
				// In case where a window does not have a parent, window.parent contains a reference to itself
				break;
			} else {
				currentWnd = currentWnd.parent;
			}
		}

		return false;
    }-*/;

	/**
	 * Gets the JavaScript representation of the concepts being used by the course file currently being edited by the base editor.
	 *
	 * @return the concept JavaScript representation
	 */
	private static native JsArrayString getBaseCourseConceptsArray()/*-{

		var currentWnd = $wnd;

		while (currentWnd != null) {

			if (currentWnd.getBaseCourseConcepts) {
				return currentWnd.getBaseCourseConcepts();

			} else {
				currentWnd = currentWnd.parent;
			}
		}

		return null;

    }-*/;

	/**
     * Gets the JavaScript representation of the LTI providers being used by the course file currently being edited by the base editor.
     *
     * @return the LTI providers JavaScript representation
     */
    private static native JsArray<LtiProviderJSO> getBaseLtiProvidersArray()/*-{

		var currentWnd = $wnd;

		while (currentWnd != null) {

			if (currentWnd.getBaseLtiProviders) {
				return currentWnd.getBaseLtiProviders();

			} else {
				currentWnd = currentWnd.parent;
			}
		}

		return null;

    }-*/;

	/**
	 * Convenience method to call the JSNI cancelModal(false) method which will simply cancel the modal dialog
	 * window which can be used in the event an editor has a critical error to return the user out of the modal dialog.
	 *
	 * Refer to the {@link #cancelModal(boolean) cancelModal(boolean)} method.
	 */
	public static void cancelModal() {
	    cancelModal(false);
	}

	/**
	 * JSNI function used to cancel a course object modal editor.  NOTE:  This assumes that this will be called
	 * from an editor that is wrapped into a course object modal where the editor is embedded as an IFame in the url.
	 *
	 */
	public static native void cancelModal(boolean removeSelection)/*-{

		console.log("cancelModal() called: " + removeSelection);
		var modalId = $wnd.modalId;
		if (modalId != null) {

			var modalWidget = $wnd.parent.document.getElementById(modalId);
			if (modalWidget != null) {
				modalWidget.cancelModal(removeSelection);
			} else {
                console.log("SEVERE: Unable to cancel the editor, the modal widget is null.");
			}
		} else {
            console.log("SEVERE: Unable to cancel the editor, the modal id was null.");
		}

    }-*/;

	/**
	 * Sets whether or not the course object modal this editor is in should allow this editor's content to be saved.
	 * NOTE:  This method assumes that it will be called from within an editor that is wrapped into a course object
	 * modal where the editor is embedded as an IFame in the url.
	 *
	 * @param enabled whether or not saving should be enabled
	 */
	public static native void setModalSaveEnabled(boolean enabled)/*-{

		console.log("setModalSaveEnabled() called: " + enabled);
		var modalId = $wnd.modalId;
		if (modalId != null) {

			var modalWidget = $wnd.parent.document.getElementById(modalId);
			if (modalWidget != null) {
				modalWidget.setModalSaveEnabled(enabled);
			} else {
                console.log("SEVERE: Unable to enable/disable saving the editor, the modal widget is null.");
			}
		} else {
            console.log("SEVERE: Unable to  enable/disable saving the editor, the modal id was null.");
		}

    }-*/;

	/**
     * Sets whether or not the course object modal this editor is in should show the save and cancel buttons.
     * NOTE:  This method assumes that it will be called from within an editor that is wrapped into a course object
     * modal where the editor is embedded as an IFame in the url.
     *
     * @param saveVisible whether the save button should be visible
     * @param cancelVisible whether the cancel button should be visible
     */
    public static native void setModalButtonsVisible(boolean saveVisible, boolean cancelVisible)/*-{

        console.log("setModalButtonsVisible() called: " + saveVisible + ", " + cancelVisible);
		var modalId = $wnd.modalId;
		if (modalId != null) {

			var modalWidget = $wnd.parent.document.getElementById(modalId);
			if (modalWidget != null) {
				modalWidget.setModalButtonsVisible(saveVisible, cancelVisible);
			} else {
                console.log("SEVERE: Unable to show/hide the editor's save and cancel buttons, the modal widget is null.");
			}
		} else {
            console.log("SEVERE: Unable to show/hide the editor's save and cancel buttons, the modal id was null.");
		}

    }-*/;

    /**
     * Retrieves the name of a transition.
     *
     * @param transition The transition to get the name for.
     * @return The transition name. Can be null.
     */
    public static String getTransitionName(Serializable transition){

        if (CourseElementUtil.isTransitionGuidance(transition)) {
            return ((generated.course.Guidance) transition).getTransitionName();

        } else if (CourseElementUtil.isTransitionPresentSurvey(transition)) {
            return ((generated.course.PresentSurvey) transition).getTransitionName();

        } else if (CourseElementUtil.isTransitionLessonMaterial(transition)) {
            return ((generated.course.LessonMaterial) transition).getTransitionName();

        } else if (CourseElementUtil.isTransitionAAR(transition)) {
            return ((generated.course.AAR) transition).getTransitionName();

        } else if (CourseElementUtil.isTransitionTrainingApplication(transition)) {
            return ((generated.course.TrainingApplication) transition).getTransitionName();

        } else if (CourseElementUtil.isTransitionMerrillsBranchPoint(transition)) {
            return ((generated.course.MerrillsBranchPoint) transition).getTransitionName();

        } else if (CourseElementUtil.isTransitionAuthoredBranch(transition)) {
            return ((generated.course.AuthoredBranch) transition).getTransitionName();

        } else{
            return null;
        }
    }

	/**
	 * Converts a YouTube video URL into its equivalent embedded version to be used in iframes.
	 *
	 * @param originalVideoUrl the original YouTube video URL
	 * @return the embedded URL
	 */
	public static String getEmbeddedYouTubeUrl(String originalVideoUrl){

		String url = originalVideoUrl;

		if(url != null){

			if(!url.contains("/embed/")) {
				// make sure the url will open in an iframe

				if(url.contains("youtu.be")){
					url = url.replace("youtu.be/", "www.youtube.com/embed/");

				} else {

	            	// define a pattern describing known YouTube URL conventions
	        		RegExp pattern = RegExp.compile("^[^v]+v=(.{11}).*");
	        		MatchResult match = pattern.exec(url);

	        		String youTubeVideoID = null;
	        		if(pattern.test(url)){
	        			// if the non-embedded URL matches any known YouTube URL conventions, get the video ID of the YouTube video

	        			youTubeVideoID = match.getGroup(1);
	        			url = url.substring(url.indexOf(youTubeVideoID));
	        			url = "https://www.youtube.com/embed/" + url;
	        		}
				}

	        }

			if(!url.contains("http")) {
				 url = "http://" + url;
			}
		}

		return url;
	}

	/**
	 * Requests the length of the YouTube video at the given URL and returns it through the given callback. The video length is
	 * retrieved by embedding the video in a hidden iframe and requesting its properties via
	 * <a href="https://developers.google.com/youtube/iframe_api_reference">Youtube's IFrame Player API</a>. Note that since this logic
	 * relies on an API provided by YouTube, the given callback will not be invoked if the given URL does not point to a valid
	 * YouTube video, so a timeout of 20 seconds has been added to ensure that a result is always given to the callback.
	 *
	 * @param videoUrl the URL of the YouTube video to retrieve the length from
	 * @param initCallback a callback handling the returned length or an error, if no such length is available
	 */
	public static void getYouTubeVideoLength(String videoUrl, final Callback<Integer, String> initCallback){

		if(videoUrl == null || videoUrl.trim().isEmpty()){
			initCallback.onFailure("Cannot get video length for a YouTube video with no URL.");
			return;
		}

		String embeddedUrl = getEmbeddedYouTubeUrl(videoUrl) + "?enablejsapi=1";

		final Frame testFrame = new Frame();
		testFrame.setUrl(embeddedUrl);
		testFrame.setVisible(false);

		RootPanel.get().add(testFrame);

		//if it takes more than 10 seconds to get the length, remove the iframe and return a failure result,
		//since the given video URL likely doesn't point to a valid YouTube video
		final Timer iframeRemovalTimer = new Timer() {

			@Override
			public void run() {

				RootPanel.get().remove(testFrame);

				initCallback.onFailure("The request to get the video length timed out.");
			}
		};

		iframeRemovalTimer.schedule(10000);

		//initialize a YouTube player for the video so that we can get its YouTube properties, particularly its length
		initYouTubePlayer(testFrame.getElement(), new Callback<Integer, String>() {

			@Override
			public void onSuccess(Integer properties) {

				RootPanel.get().remove(testFrame);

				initCallback.onSuccess(properties);

				iframeRemovalTimer.cancel();
			}

			@Override
			public void onFailure(String errorMsg) {

				RootPanel.get().remove(testFrame);

				initCallback.onFailure(errorMsg);

				iframeRemovalTimer.cancel();
			}
		});
	}

	/**
	 * Initializes a JavaScript YouTube Player object using the given element and passes its properties (currently, just the length)
	 * to the given callback. This method uses
	 * <a href="https://developers.google.com/youtube/iframe_api_reference">Youtube's IFrame Player API</a> in order to create the
	 * player object through which the properties are then retrieved. The given element is assumed to be an iframe; though, the
	 * YouTube API is capable of using other elements (such as divs) as well.
	 *
	 * @param element the element (assumed to be an iframe) used to initialize the player
	 */
	private static native void initYouTubePlayer(Element element, Callback<Integer, String> initCallback) /*-{
		
		var player;
		
		//construct a YouTube player element 
		player = new $wnd.YT.Player(element, {
			events: {
				'onReady' : onPlayerReady
			}
		});
		
		//define a function to get the video's duration once it is ready
		function onPlayerReady(event) {
			
			var duration = player.getDuration();
		    	
		    //invoke the callback to pass the initialization information
		    initCallback.@com.google.gwt.core.client.Callback::onSuccess(Ljava/lang/Object;)(@java.lang.Integer::valueOf(Ljava/lang/String;)(duration));
		}
		
		if(!player){
			
			var errorMsg = "Failed to initialize YouTube player.";
			
			//invoke the callback to pass the failure information
		    initCallback.@com.google.gwt.core.client.Callback::onFailure(Ljava/lang/Object;)(errorMsg);
		}
		
	}-*/;

	/**
	 * Gets the name of the course object that is currently being edited in the base course editor. This can be useful for
	 * sub-editors that don't have direct access to the main course object. Null will be returned if the author has not selected
	 * a course object for editing.
	 *
	 * @return the name of the course object that is currently being edited
	 */
	public static String getCurrentCourseObjectName() {
	    return getCurrentCourseObjectNameNative(getBaseEditorWindow());
	}

	/**
	 * Gets the name of the course object currently being edited in the given window
	 *
	 * @param targetWindow the window editing the course object
	 * @return the name of the course object being edited
	 */
	private static native String getCurrentCourseObjectNameNative(JavaScriptObject targetWindow) /*-{
		return targetWindow.getCurrentCourseObjectName();
    }-*/;


    /**
     * Generates a {@link AuthoredBranch} with a unique branch id
     *
	 * @param course the course containing the authored branch
     * @return the generated {@link AuthoredBranch}.
     */
    public static AuthoredBranch generateNewAuthoredBranch(Course course) {
        AuthoredBranch authoredBranch = new AuthoredBranch();
        authoredBranch.setBranchId(generateAuthoredBranchId(course));
        Paths newPaths = new Paths();
        authoredBranch.setPaths(newPaths);

        Path path = new Path();
        path.setPathId(BigInteger.ONE);
        path.setName("Initial Path");
        path.setCondition(new AuthoredBranch.Paths.Path.Condition());
        newPaths.getPath().add(path);

        return authoredBranch;
    }

    /**
     * Generates a unique ID that can be applied to an authored branch. The algorithm for doing so is
     * to identify the largest branch ID in the given course and increment by one
     *
     * @param course the course containing the authored branch
     * @return A unique ID that can be applied to an authored branch.
     */
    public static BigInteger generateAuthoredBranchId(Course course) {

        BigInteger largestIdFound = BigInteger.valueOf(0);

        if (course != null && course.getTransitions() != null) {
            for (Serializable transition: course.getTransitions().getTransitionType()) {
                if (transition instanceof AuthoredBranch) {
                    AuthoredBranch authoredBranch = (AuthoredBranch)transition;

                    BigInteger largestAuthoredBranchId = getLargestId(authoredBranch);

                    // if authored branch id is bigger, set largest id to authored branch id
                    if (largestAuthoredBranchId.compareTo(largestIdFound) == 1) {
                        largestIdFound = largestAuthoredBranchId;
                    }
                }
            }
        }

        // add one to the largest node id found to make it unique
        return largestIdFound.add(BigInteger.valueOf(1));
    }

    /**
     * Recursively gets the largest id of all authored branches in this branch,
     * including the root branch
     *
     * @param authoredBranch the root authored branch to search through
     * @return the largest branch id of all authored branches in this branch
     */
    private static BigInteger getLargestId(AuthoredBranch authoredBranch) {
        BigInteger largestIdFound = BigInteger.valueOf(0);

        if (authoredBranch != null) {
            // if the branch id is bigger, set largest node id to branch id
            if (authoredBranch.getBranchId().compareTo(largestIdFound) == 1) {
                largestIdFound = authoredBranch.getBranchId();
            }

            if (authoredBranch.getPaths() != null) {
                for (Path path : authoredBranch.getPaths().getPath()) {
                    if (path.getCourseobjects() != null) {
                        for (Serializable transition : path.getCourseobjects().getAAROrAuthoredBranchOrEnd()) {
                            if (transition instanceof AuthoredBranch) {
                                AuthoredBranch nestedBranch = (AuthoredBranch)transition;
                                BigInteger largestNestedAuthoredBranchID = getLargestId(nestedBranch);

                                // if the branch id is bigger, set largest node id to branch id
                                if (largestNestedAuthoredBranchID.compareTo(largestIdFound) == 1) {
                                    largestIdFound = largestNestedAuthoredBranchID;
                                }
                            }
                        }
                    }
                }
            }

            return largestIdFound;
        } else {
            return null;
        }
    }

    /**
     * Notifies any handlers on this editor's window or its parent windows that the given scenario file
     * has been saved.
     *
     * @param the workspace-relative path to the scenario file. If null, the operation will be ignored.
     */
    public static native void onScenarioSaved(String filePath)/*-{

		if (filePath == null) {
			return;
		}

		var currentWnd = $wnd;

		while (currentWnd != null) {

			if (currentWnd.onScenarioSaved) {
				return currentWnd.onScenarioSaved(filePath);

			} else if (currentWnd == currentWnd.parent) {
				// In case where a window does not have a parent, window.parent contains a reference to itself
				break;

			} else {
				currentWnd = currentWnd.parent;
			}
		}

    }-*/;

    /**
     * Opens the AutoTutor Script Authoring Tool (ASAT) into a separate window and passes in any parameters that
     * it needs to author AutoTutor scripts for the current user.
     */
    public static void openASATWindow() {
        
        /* Get the base ASAT URL and username */
        String asatUrl = GatClientUtility.getServerProperties().getPropertyValue(ServerProperties.ASAT_URL);
        String username = GatClientUtility.getUserName(); 
        
        /* Build a query containing the user identification parameters needed by the ASAT to identify the current user*/
        StringBuilder queryBuilder = new StringBuilder();
        
        ArrayList<String> urlParams = new ArrayList<>();
        
        urlParams.add("TheEmail=" + URL.encodeQueryString(username) + "@gifttutoring.org");
        urlParams.add("TheFullName=" + URL.encodeQueryString(username));
        
        if(!urlParams.isEmpty()) {
            
            queryBuilder.append("?");
            
            boolean isFirstParam = true;
            for(String param : urlParams) {
                
                if(isFirstParam) {
                    isFirstParam = false;
                    
                } else {
                    queryBuilder.append("&");
                }
                
                queryBuilder.append(param);
            }
        }
        
       /* open ASAT in a separate browser tab/window */
        Window.open(asatUrl + queryBuilder.toString(),"_blank","");
    }
    
    /**
     * Gets the ID of a scenario to load from an external tool. This is only intended to
     * be used when the GAT needs to be reachable from an external tool on the same network
     * that uses the same SSO service.
     * 
     * @return the external sceneario ID. Can be null.
     */
    public static String getExternalScenarioId() {
        return Location.getParameter("scenarioId");
    }
}
