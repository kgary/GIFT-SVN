/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.LessonLevelEnum;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.util.StringUtils;

/**
 * This class contains properties from the server that are needed by the client.
 * 
 * @author mhoffman
 *
 */
public class ServerProperties implements IsSerializable{
    
    /** common property values*/
    protected static final String TRUE = "true";
    protected static final String YES = "yes"; 
    

    /**  The expected name in the dashboard.properties file for the landing page message for the dashboard */
    public static final String LANDINGPAGE_MESSAGE = "LandingPageMessage";
    
    /** The expected name in the dashboard.properties file to determine if the cloud login page should be shown instead of the normal login page. */
    public static final String USE_CLOUDLOGINPAGE = "UseCloudLoginPage";
    
    /**  The expected name in the dashboard.properties file for the timeout to use for when the tutor is starting a course */
    public static final String TUTOR_COURSE_START_TIMEOUT = "TutorCourseStartTimeout";
    
    public static final String DEPLOYMENT_MODE = "DeploymentMode";
    
    /** the username for an anonymized user **/
    public static final String READ_ONLY_USER = "ReadOnlyUser";
    
    /** The URL for the TUI host */
    public static final String TUI_URL = "TuiUrl";
    /** The URL for the TUI websocket service */
    public static final String TUI_WS_URL = "TuiWsUrl";
    /** The URL for the DashBoard host */
    public static final String DASHBOARD_URL = "DashboardUrl";
    /** The URL for the Dashboard Web Socket servlet */
    public static final String DASHBOARD_WS_URL = "DashboardWebsocketUrl";
    /** The URL for the GAT host */
    public static final String GAT_URL = "GatUrl";
    /** The URL for the ASAT host */
    public static final String ASAT_URL = "AsatUrl";
    /** The URL for the competency framework */
    public static final String AUTHORITATIVE_SYSTEM_URL = "AuthoritativeSystemUrl";
    /** The URL for the TRADEM host */
    public static final String TRADEM_URL = "TrademUrl";
    /** The URL for the CMS host */
    public static final String CMS_URL = "CmsUrl";
    /** The URL for the LTI Tool Provider servlet */
    public static final String LTI_URL = "LtiUrl";
    /** The URL for the LTI Tool Consumer servlet */
    public static final String LTI_CONSUMER_URL = "LtiConsumerUrl";
    
    public static final String USE_HTTPS = "UseHttps";
    
    public static final String WINDOW_TITLE = "WindowTitle";
    
    public static final String DOMAIN_CONTENT_SERVER_ADDRESS = "DomainContentServerAddress";
    
    public static final String BYPASS_SURVEY_PERMISSION_CHECK = "BypassSurveyPermissionCheck";
    
    public static final String VERSION_NAME = "VersionName";
    /** The integer value for the number of allowed attempts for a Mbp's recall attempts when the course.xml doesn't specify*/ 
    public static final String DEFAULT_MBP_RECALL_ALLOWED_ATTEMPTS = "DefaultMbpRecallAllowedAttempts";
    /** The integer value for the number of allowed attempts for a Mbp's practice attempts when the course.xml doesn't specify*/ 
    public static final String DEFAULT_MBP_PRACTICE_ALLOWED_ATTEMPTS = "DefaultMbpPracticeAllowedAttempts";
    
    public static final String VERSION_DATE = "VersionDate";
    
    public static final String BUILD_DATE = "BuildDate";
    
    /** the property key for build location */
    public static final String BUILD_LOCATION = "BuildLocation";
    
    public static final String DOCUMENTATION_TOKEN = "DocumentationToken";
    
    public static final String TRUSTED_LTI_CONSUMERS = "TrustedLtiConsumers";
    
    public static final String TRUSTED_LTI_PROVIDERS = "TrustedLtiProviders";
    
    public static final String LTI_HELP_PAGE_URL = "LtiHelpPageUrl";
    
    public static final String ENABLE_CLIENT_ANALYTICS = "EnableClientAnalytics";
    
    public static final String CLIENT_ANALYTICS_URL = "ClientAnalyticsUrl";
    
    /** whether this gift instance has restricted access (e.g. white list authentication is implemented) */
    public static final String RESTRICTED_USER_ACCESS = "RestrictedUserAccess";
    
    /** the prohibited user names */
    public static final String PROHIBITED_NAMES = "ProhibitedNames";
    public static final String PROHIBITED_NAMES_DELIM = Constants.COMMA;
    
    /** The server-generated ID to use when connecting to this client's web socket*/
    public static final String WEB_SOCKET_ID = "WEB_SOCKET_ID";
    
    /** The URL location of the default avatar on the tutor server*/
    public static final String DEFAULT_CHARACTER = "DefaultCharacter";
    
    /** The API key used to interact with Google Maps' services */
    public static final String GOOGLE_MAPS_API_KEY = "GoogleMapsApiKey";

    /**
     * The delay (in milliseconds) after an assessment notification is fired
     * before another is allowed to be fired
     */
    public static final String ASSESSMENT_NOTIFICATION_DELAY_MS = "AssessmentNotificationDelayMs";
    
    /**
     * the URL of the service providing military symbols (e.g. 2525C)
     */
    public static final String MILITARY_SYMBOL_SERVICE_URL = "MilitarySymbolServiceURL";
    
    /**
     * The lesson level for GIFT
     */
    public static final String LESSON_LEVEL = "LessonLevel";
    
    /**
     * The image used on the Dashboard login page, Tutor (TUI) pages, GAT course/survey/conversation-tree preview, error pages 
     */
    public static final String BACKGROUND_IMAGE = "Background";
    
    /** the organization image used in places such as the login page */
    public static final String ORGANIZATION_IMAGE = "Organization";
    
    /**
     * icon used for things like the system tray (recommended size: 32x32)
     */
    public static final String SYSTEM_ICON_SMALL             = "System_Icon_Small";
    
    /**
     * image used on Experiment/LTI welcome/resume/completed pages, error pages, Simple login page 
     * Recommended size: width = 200, height = 160
     */
    public static final String LOGO = "Logo";

    /** Property field for checking if the JRE is running as 32-bit or 64-bit */
    public static final String JRE_BIT = "JREBit";
    
    /** property field for the default course list filter for take a course dashboard page, contains
     * a comma delimited list of common.io.CourseListFilter.CourseSourceOption enum values */
    public static final String COURSE_LIST_FILTER = "CourseListFilter";
    
    /** the file servlet path to domain session output folders */
    public static final String SESSION_OUTPUT_SERVER_PATH = "SessionOutputServerPath";
    
    /** 
     * The maximum number of messages to display in a message panel displayed
     * by the web monitor
     */
    public static final String MESSAGE_DISPLAY_BUFFER_SIZE = "MessageDisplayBufferSize";
    
    /**
     * Whether to use the server's built-in authentication logic to redirect to a single sign-on (SSO)
     * login page rather than GIFT's normal login page
     */
    public static final String USE_SSO_LOGIN = "UseSSOLogin";

    private DeploymentModeEnum deploymentMode = null;
    
    /** The lesson level for GIFT */
    private LessonLevelEnum lessonLevel = null;
    
    /** map of properties */
    private Map<String, String> properties = new HashMap<String, String>();
    
    /** the list of default course list filter for the dashboard take a course page,
     * values are of common.io.CourseListFilter.CourseSourceOption enum */
    private List<String> defaultCourseListFilter = null;
    
    /** The URL to any configured external strategy provider */
    public static final String EXTERNAL_STRATEGY_PROVIDER_URL = "ExternalStrategyProviderUrl";

    /**
     * Default constructor
     */
    public ServerProperties(){

    }
    
    /**
     * Clones the properties from the given instance into a new instance
     */
    public ServerProperties(ServerProperties propertiesToCopy){
    	this.properties.putAll(propertiesToCopy.properties);
    }
    
    /**
     * Add a value to the property map that can be referenced by the given key.
     * 
     * @param key the key to be used to obtain the value from the map
     * @param value the value to associate with the key in the map
     * @return String the previous value of the specified key in this map, or null if it did not have one 
     */
    public String addProperty(String key, String value){
        return properties.put(key, value);
    }
    
    /**
     * Return whether GIFT should be configured to run in simple mode.
     * 
     * @return whether GIFT should be configured to run in simple mode.
     */
    public boolean isSimpleMode(){        
    	return getDeploymentMode().equals(DeploymentModeEnum.SIMPLE);
    }
    
    /**
     * Returns whether GIFT should be configured to run in server mode.
     * 
     * @return whether GIFT should be configured to run in server mode.
     */
    public boolean isServerMode() {    	
        return getDeploymentMode().equals(DeploymentModeEnum.SERVER);
    }    
    
    /**
     * Return whether GIFT should be configured to run in desktop mode.
     * 
     * @return whether GIFT should be configured to run in desktop mode.
     */
    public boolean isDesktopMode() { 
    	return getDeploymentMode().equals(DeploymentModeEnum.DESKTOP);
    }
    /**
     * Returns the deployment mode for GIFT.
     * 
     * @return String the deployment mode for GIFT.
     */
    public DeploymentModeEnum getDeploymentMode() {
    	
    	if(deploymentMode == null) {
    		deploymentMode = DeploymentModeEnum.valueOf(getPropertyValue(DEPLOYMENT_MODE));
    	}
    	
    	return deploymentMode;      	
    }
    
    public String getReadOnlyUser() {
        return getPropertyValue(READ_ONLY_USER);
    }
        
    /**
     * Return the landing page message to use in the tutor client.
     * 
     * @return String  Can be null or empty string
     */
    public String getLandingPageMessage(){
        return getPropertyValue(LANDINGPAGE_MESSAGE);
    }
    
    /**
     * Return the url to the tutor web socket service. 
     * 
     * @return The url to the tutor web socket service.
     */
    public String getTutorWebSocketUrl() {
        return getPropertyValue(TUI_WS_URL);
    }
    
    /**
     * Return the URL of the dashboard e.g. http://localhost:8080/dashboard
     * @return the URL of the GAT
     */
    public String getDashboardUrl() {
    	return getPropertyValue(DASHBOARD_URL);
    }
    
    /**
     * Return the url to the dashboard web socket service. 
     * 
     * @return The url to the dashboard web socket service.
     */
    public String getDashboardWebSocketUrl() {
        return getPropertyValue(DASHBOARD_WS_URL);
    }
    
    
    /**
     * Get the number of seconds before the dashboard times out when the tutor is starting
     * a course.  
     * 
     * @return number of seconds to use for a tutor timeout
     */
    public String getTutorCourseStartTimeout(){
        return getPropertyValue(ServerProperties.TUTOR_COURSE_START_TIMEOUT);
    }
    
    /**
     * Return the URL of the gift authoring tool (GAT) e.g. http://localhost:8080/gat
     * @return the URL of the GAT
     */
    public String getGatUrl() {
    	return getPropertyValue(GAT_URL);
    }
    
    /**
     * Return the URL of the LTI servlet e.g. http://localhost:8080/dashboard/lti
     * @return the URL of the LTI servlet.
     */
    public String getLtiUrl() {
        return getPropertyValue(LTI_URL);
    }
    
    /**
     * Return the URL of the LTI consumer servlet e.g. http://localhost:8080/dashboard/lticonsumer
     * @return the URL of the LTI consumer servlet.
     */
    public String getLtiConsumerUrl() {
        return getPropertyValue(LTI_CONSUMER_URL);
    }
    
    /**
     * Returns whether or not to by pass permission checks for surveys. This value can
     * only be used if in Desktop Deployment mode, so even if value is set to true in
     * common properties folder, false will be returned if not in Desktop Deployment mode. 
     * 
     * @return true iff desktop mode and flag is true, false otherwise
     */
    public boolean getBypassSurveyPermissionsCheck(){
    	if(getDeploymentMode() != DeploymentModeEnum.DESKTOP){
    		return false;
    	}
    	return getPropertyBooleanValue(BYPASS_SURVEY_PERMISSION_CHECK, false);
    }
    
    /**
     * Return whether the secure HTTPS connection should be used rather than the HTTP connection.
     * 
     * @return boolean true if "https://" connections should be used instead of "http://".  (DEFAULT: false)
     */
    public boolean shouldUseHttps() {
    	return getPropertyBooleanValue(USE_HTTPS, false);
    }
    
    /**
     * Return the name of this GIFT version (e.g. "2015-1")
     * 
     * @return can be null if not set.
     */
    public String getVersionName(){
        return getPropertyValue(VERSION_NAME);
    }
    
    /**
     * Return the version release date as a string
     * 
     * @return the release date as a string (Note: this could be in any format)
     */
    public String getVersionDate(){
        return getPropertyValue(VERSION_DATE);
    }
    
    /**
     * Return the build date
     * 
     * @return build date in the format of 'yyyy:MM:dd HH:mm z'
     */
    public String getBuildDate(){
        return getPropertyValue(BUILD_DATE);
    }
    
    /**
     * Return the location of the build
     * @return the path to this GIFT instance on this computer (e.g. C:/work/GIFT 2021-1/)
     */
    public String getBuildLocation(){
        return getPropertyValue(BUILD_LOCATION);
    }
    
    /**
     * Get the documentation token to use in the URLs to the GIFT portal
     * wiki pages that contain documentation for this GIFT instance.
     * 
     * @return the documentation token to use in URLs
     */
    public String getDocumentationToken(){
        return getPropertyValue(DOCUMENTATION_TOKEN);
    }
    
    /**
     * Gets the ServerProperties.WINDOW_TITLE property
     * @return the value of the WindowTitle property
     */
    public String getWindowTitle(){
        return getPropertyValue(WINDOW_TITLE);
    }
    
    /**
     * Gets the flag indicating whether client Analytics are enabled
     * 
     * @return ClientAnalyticsFlag flag
     */
    public boolean isClientAnalyticsEnabled(){
    	return this.getPropertyBooleanValue(ENABLE_CLIENT_ANALYTICS, false);
    }
    
    /**
     * Return whether this gift instance has restricted access (e.g. white list authentication is implemented)
     * 
     * @return default is false
     */
    public boolean hasRestrictedAccess(){
        return getPropertyBooleanValue(RESTRICTED_USER_ACCESS, false);
    }
    
    /**
     * Return the list of prohibited GIFT user names.  These usernames are those that should never be allowed
     * to login, create workspaces, etc. as it can cause issues in systems like Nuxeo.
     * 
     * @return the list of prohibited user names. Never null. Can be empty.
     */
    public ArrayList<String> getProhibitedNames() {
        ArrayList<String> prohibitedNames = new ArrayList<String>();

        String delimitedNames = getPropertyValue(PROHIBITED_NAMES);
        if (StringUtils.isNotBlank(delimitedNames)) {
            String[] names = delimitedNames.split(PROHIBITED_NAMES_DELIM);
            prohibitedNames.addAll(Arrays.asList(names));
        }

        return prohibitedNames;
    }
    
    /**
     * Gets the analytics server ip
     * 
     * @return string ClientAnalyticsServerIp
     */
    public String getClientAnalyticsUrl(){
    	return this.getPropertyValue(CLIENT_ANALYTICS_URL);
    }
    
    /**
	 * Sets the ID uniquely identifying this client's web socket on the server end 
	 * 
	 * @return the webSocketId an ID uniquely identifying this client's web socket on the server end 
	 */
	public String getWebSocketId() {
		return this.getPropertyValue(WEB_SOCKET_ID);
	}

    /**
     * Return the assessment notification delay property value.
     * 
     * @return default is 5000 milliseconds
     */
    public int getAssessmentNotificationDelayMs() {
        return getPropertyIntegerValue(ASSESSMENT_NOTIFICATION_DELAY_MS, 5000);
    }
    
    /**
     * Return the URL of the service providing military symbols (e.g. 2525C)
     * @return the URL
     */
    public String getMilitarySymbolServiceURL(){
        return getPropertyValue(MILITARY_SYMBOL_SERVICE_URL);
    }
    
    /**
     * Returns the lesson level for GIFT.
     * 
     * @return String the lesson level for GIFT.
     */
    public LessonLevelEnum getLessonLevel() {
        
        if(lessonLevel == null) {
            lessonLevel = LessonLevelEnum.valueOf(getPropertyValue(LESSON_LEVEL));
        }
        
        return lessonLevel;
    }
    
    /**
     * Return the default selected choices under the 'Source' filter on the Dashboard 'Take a Course' page.
     * Values are display names taken from common.io.CourseListFilter.CourseSourceOption
     * @return can be null or empty if not set in the properties.
     */
    public List<String> getCourseListFilter(){
        
        // if not cached then parse the property value
        if(defaultCourseListFilter == null){
            defaultCourseListFilter = new ArrayList<>();
            
            String rawDefaultCourseListFilter = getPropertyValue(COURSE_LIST_FILTER);
            if(StringUtils.isNotBlank(rawDefaultCourseListFilter)){
                
                String[] valueStrs = rawDefaultCourseListFilter.split(",");
                for(String value : valueStrs){
                    defaultCourseListFilter.add(value);
                }                
            }
        }
        
        return defaultCourseListFilter;
        
    }
    
    /**
     * Return the default domain session output file servlet path that can be used
     * to retrieve video/audio files to play in UIs like the Game Master.
     * @return the {@value #SESSION_OUTPUT_SERVER_PATH} property value from the server 
     * (e.g. "/dashboard/recorder/output/domainSessions/")
     */
    public String getSessionOutputServerPath(){
        return getPropertyValue(SESSION_OUTPUT_SERVER_PATH);
    }

    /**
     * Accessor to retrieve the flag indicating if the JRE is 64-bit.
     * 
     * @return true if the JRE is 64-bit; false otherwise.
     */
    public boolean isJRE64Bit() {
        return getPropertyBooleanValue(JRE_BIT, false);
    }
    
    /**
     * Gets the maximum number of messages to display in a message panel displayed
     * by the web monitor. Default value is 1000.
     * @return the maximum message display buffer size
     */
    public int getMessageDisplayBufferSize() {
        return getPropertyIntegerValue(MESSAGE_DISPLAY_BUFFER_SIZE, 1000);
    }

    /**
     * Return the boolean property value for the given property name
     *
     * @param propertyName - the name of the property to get its value
     * @param defaultValue The default value to return when the property is not
     * set
     * @return boolean - the value as a boolean.
     */
    public boolean getPropertyBooleanValue(String propertyName, boolean defaultValue) {

        String value = getPropertyValue(propertyName);

        if (value != null) {

            //remove trailing white space that could have accidently been added while changing property value which will cause 
            //the equals condition to fail
            value = value.trim();

            return value.equalsIgnoreCase(TRUE) || value.equalsIgnoreCase(YES);

        } else {

            return defaultValue;
        }
    }

    /**
     * Return the integer property value for the given property name
     *
     * @param propertyName - the name of the property to get its value
     * @param defaultValue The default value to return when the property is not
     *        set
     * @return integer - the value as an integer.
     */
    public int getPropertyIntegerValue(String propertyName, int defaultValue) {

        String value = getPropertyValue(propertyName);

        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (@SuppressWarnings("unused") NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    /**
     * Return the property value for the given property name
     * 
     * @param propertyName - the name of the property to get its value
     * @return String - the property's value, null if property is not found
     */
    public String getPropertyValue(String propertyName){        
        return properties.get(propertyName);
    }
    
    /**
     * Gets whether to use the server's built-in authentication logic to redirect to a single sign-on (SSO)
     * login page rather than GIFT's normal login page
     * 
     * @return whether to use SSO login
     */
    public boolean shouldUseSSOLogin(){
        return this.getPropertyBooleanValue(USE_SSO_LOGIN, false);
    }
    
    /**
     * Gets all of the property values that are currently stored by this set of
     * server properties. This can be useful when copying properties.
     * 
     * @return the stored server properties. Will not be null.
     */
    Map<String, String> getAllProperties(){
        return properties;
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[ServerProperties: \n");
        
        for(String key : properties.keySet()){
            sb.append("\nkey:").append(key).append(" value:").append(properties.get(key));
        }
        
        sb.append("]");
        
        return sb.toString();
        
    }
}
