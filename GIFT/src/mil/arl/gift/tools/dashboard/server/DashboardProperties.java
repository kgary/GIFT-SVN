/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.CourseListFilter.CourseSourceOption;
import mil.arl.gift.common.util.StringUtils;


/**
 * Properties for the dashboard application.  These properties are defined in the
 * dashboard.properties file (and any subclass property file such as common.properties).
 *
 * @author nblomberg
 *
 */
public class DashboardProperties extends CommonProperties {
    
    /** instance of the logger */
    private static final Logger logger = LoggerFactory.getLogger(DashboardProperties.class);

    /** the property file name */
    private static final String PROPERTY_FILE = "dashboard.properties";

    private static final String COURSE_VALIDATION_CACHE_EXPIRATION_PROP = "CourseValidationCacheExpiration";
    private static final String INVALID_COURSE_VALIDATION_CACHE_EXPIRATION_PROP = "InvalidCourseValidationCacheExpiration";
    
    /** the property key for the ERT default event types, used to specify which session log events should be selected by default */
    private static final String DEFAULT_EVENT_TYPES = "defaultEventTypes";
    
    /** the property key for the GAT file server for which file types to exclude */
    private static final String FILE_SERVER_EXCLUDES_PROPERTY = "File_Server_Excludes";
    
    /** the property key for the GAT for where survey exports should be placed */
    private static final String SURVEY_EXPORT_PATH_PROPERTY = "Survey_Export_Path";

    /** Default time in seconds before a course validation cache entry expires. */
    private static final int DEFAULT_COURSE_VALIDATION_CACHE_EXPIRATION = 5*60;

    /** Default time in seconds before a course that failed validation should be revalidated. */
    private static final int DEFAULT_INVALID_COURSE_VALIDATION_CACHE_EXPIRATION = 10;

    /** Default time in seconds to allow the tutor to start a course before the dashboard terminates the session  */
    private static final int DEFAULT_TUTOR_COURSE_START_TIMEOUT = 10;

    /** The URL for the websocket service */
    private static final String WS_URL = "WebSocketUrl";

    /** The amount of time (in milliseconds) during a page refresh to allow the websocket to be re-established.
     *  If this value is exceeded, the server ends the browser session and considers the browser to be disconnected. */
    private static final String WEBSOCKET_REFRESH_TIMER_MS = "WebSocketRefreshTimerMs";

    /** Default value (in milliseconds) to allow the websocket to be re-established. */
    private static final int DEFAULT_WEBSOCKET_REFRESH_TIMER_MS = 5000;

    /**
     * The key for the path to the YAML file that details how to map a DIS
     * message to a SIDC.
     */
    private static final String DIS_TO_SIDC_MAPPING_FILE = "SidcMappingFile";
    
    /** The default selected choices under the 'Source' filter on the Dashboard 'Take a Course' page.
    Values taken from common.io.CourseListFilter.java */
    private static final String DEFAULT_COURSE_LIST_FILTER = "defaultCourseListFilter";
    
    private static final String REMOTE_LAUNCH_DISCOVERY_BROADCAST_ADDRESS = "RemoteLaunchDiscoveryBroadcastAddress";
    
    private static final int    DEFAULT_REMOTE_LAUNCH_WORKSTATION_LISTEN_PORT = 50601;
    
    private static final String REMOTE_LAUNCH_WORKSTATION_LISTEN_PORT_LIST = "RemoteLaunchWorkstationListenPortList";    
    
    private static final String REMOTE_LAUNCH_MONITOR_LISTEN_PORT = "RemoteLaunchMonitorListenPort";
    
    private static final int    DEFAULT_REMOTE_LAUNCH_MONITOR_LISTEN_PORT = 50600;    

    private static final String REMOTE_LAUNCH_HEARTBEAT_INTERVAL = "RemoteLaunchHeartbeatInterval"; 
    
    private static final int    DEFAULT_REMOTE_LAUNCH_HEARTBEAT_INTERVAL_MILLIS = 5000;   
    
    /** 
     * The maximum number of messages to display in a message panel displayed
     * by the web monitor
     */
    private static final String MESSAGE_DISPLAY_BUFFER_SIZE = "MessageDisplayBufferSize";
    
    /** The default message display buffer size to use when none is provided */
    private static final int    DEFAULT_MESSAGE_DISPLAY_BUFFER_SIZE = 1000;
    
    /** the list of default event types for the ERT */
    private String[] defaultEventTypes = null;

    /** singleton instance of this class */
    private static DashboardProperties instance = null;

    /**
     * Return the singleton instance of this class
     *
     * @return DashboardProperties
     */
    public static synchronized DashboardProperties getInstance(){

        if(instance == null){
            instance = new DashboardProperties();
        }

        return instance;
    }

    /**
     * Private constructor
     */
    private DashboardProperties(){
        //providing the 2nd property file path so that the DashboardServiceImpl.java can be used in junit
        //which doesn't use the war path for the config file
        super(PROPERTY_FILE, "tools" + File.separator + "dashboard" + File.separator + PROPERTY_FILE);

    }

    @Override
    public void refresh(){
        super.refresh();
        defaultEventTypes = null;
    }

    /**
     * Accessor to get the landing page message for the dashboard.
     *
     * @return String - The landing page message to be displayed.  Can be an empty string meaning that there is no message to be displayed.
     */
    public String getLandingPageMessage() {
        return getPropertyValue(ServerProperties.LANDINGPAGE_MESSAGE, "");
    }

    /**
     * Accessor to retrieve the flag to determine if the cloud login page should be shown.
     * This should only be set to true for the cloud version of GIFT.
     *
     * @return - true if the cloud version of the login page should be shown, false otherwise.
     */
    public String getUseCloudLoginPage() {
        return getPropertyValue(ServerProperties.USE_CLOUDLOGINPAGE, "");
    }

    /**
     * Get the number of seconds that a course validation result should be cached.
     * Zero results in no caching, while a value that is too high may result in
     * invalid course validation results until the cache expires.
     * @return The number of seconds a course validation result should be cached
     */
    public int getValidationCacheExpiration() {
        return super.getPropertyIntValue(COURSE_VALIDATION_CACHE_EXPIRATION_PROP, DEFAULT_COURSE_VALIDATION_CACHE_EXPIRATION);
    }

    /**
     * Get the number of seconds before a course that failed validation should be revalidated.
     * The idea is that invalid courses may change more frequently.
     * @return The number of seconds before course that failed validation should be cached before revalidation.
     */
    public int getInvalidCourseCacheExpiration() {
        return super.getPropertyIntValue(INVALID_COURSE_VALIDATION_CACHE_EXPIRATION_PROP, DEFAULT_INVALID_COURSE_VALIDATION_CACHE_EXPIRATION);
    }

    /**
     * Get the number of seconds before the dashboard times out when the tutor is starting
     * a course.
     *
     * @return number of seconds to use for a tutor timeout
     */
    public int getTutorCourseStartTimeout(){
        int value = super.getPropertyIntValue(ServerProperties.TUTOR_COURSE_START_TIMEOUT, DEFAULT_TUTOR_COURSE_START_TIMEOUT);

        if(value <= 0){
            value = DEFAULT_TUTOR_COURSE_START_TIMEOUT;
        }

        return value;
    }

    /**
     * Return the url to the web socket service.
     *
     * @return The url to the web socket service.
     */
    public String getWebSocketUrl() {
        return getPropertyValue(WS_URL);
    }

    /**
     * Return the amount of time (in milliseconds) during a page refresh to allow the websocket to be re-established.
     *
     * @return The amount of time (in milliseconds) during a page refresh to allow the websocket to be re-established.
     */
    public int getWebSocketRefreshTimerMs() {
        return getPropertyIntValue(WEBSOCKET_REFRESH_TIMER_MS, DEFAULT_WEBSOCKET_REFRESH_TIMER_MS);
    }

    /**
     * Return the path to the YAML file that is responsible for mapping DIS
     * entity types to SIDC identifiers.
     *
     * @return The file path for the mapping file relative to the GIFT folder
     */
    public String getDisToSidcMappingFilePath() {
        return getPropertyValue(DIS_TO_SIDC_MAPPING_FILE);
    }
    
    /**
     * Return the list of event types that should be selected by default for reports created by the ERT.
     * 
     * @return String[]
     */
    public String[] getDefaultEventTypes(){
        
        if(defaultEventTypes == null){
            
            String rawDefaultEventTypesProp = getPropertyValue(DEFAULT_EVENT_TYPES);
            if(rawDefaultEventTypesProp != null){
                defaultEventTypes = rawDefaultEventTypesProp.split(",");
            } 
        }
        
        return defaultEventTypes;
    }
    
    /**
     * Return the comma delimited string containing the default selected choices under the 'Source' filter on the Dashboard 'Take a Course' page.
     * Values are display names taken from common.io.CourseListFilter.CourseSourceOption
     * @return can be null if not set in the properties. E.g. "My Courses,Showcase Courses,Shared With Me"
     */
    public String getDefaultCourseListFilter(){
        
            StringBuilder checkedStrBuilder = new StringBuilder();
            
            String rawDefaultCourseListFilter = getPropertyValue(DEFAULT_COURSE_LIST_FILTER);
            if(StringUtils.isNotBlank(rawDefaultCourseListFilter)){
                
                String[] valueStrs = rawDefaultCourseListFilter.split(",");
                for(String value : valueStrs){
                    
                    try{
                        // check its a valid enum
                        for(CourseSourceOption optionEnum : CourseSourceOption.values()){
                            if(optionEnum.getDisplayName().equals(value)){
                                checkedStrBuilder.append(value).append(",");
                                break;
                            }
                        }
                    }catch(@SuppressWarnings("unused") Exception e){
                        logger.warn("Found an unhandled course list filter value of '"+value+"' in the property value for "+DEFAULT_COURSE_LIST_FILTER+" in "+PROPERTY_FILE+".");                        
                    }
                }
                
            }
        
        return checkedStrBuilder.toString();
    }
    
    /**
     * Gets the list of patterns to apply to find what files to exclude from the
     * survey images list
     *
     * @return String[] The list of exclude patterns
     */
    public String[] getSurveyImagesDirectoryExcludes() {
        String excludes = getPropertyValue(FILE_SERVER_EXCLUDES_PROPERTY);
        if (excludes != null) {
            return excludes.split(",");
        }
        return null;
    }
    
    /**
     * Gets the path to the directory where surveys are exported.
     * 
     * @return Path to survey export directory.
     */
    public String getSurveyExportPath() {
        return getPropertyValue(SURVEY_EXPORT_PATH_PROPERTY);
    }
    
    /**
     * Gets the user specified broadcast address.  If not set, the system will attempt to ascertain the broadcast address on its own.
     *     
     * @return the IPv4 broadcast address as a string, to be used by the Monitor when broadcasting its host information
     */
    public String getRemoteLaunchDiscoveryBroadcastAddress() {        
        return this.getPropertyValue(REMOTE_LAUNCH_DISCOVERY_BROADCAST_ADDRESS);
    }
    
    /**
     * Gets the port number used by the remote launch capability to listen for heartbeats sent by the learner workstations.
     * 
     * @return the port specified in the properties file if it exists. otherwise returns DEFAULT_REMOTE_LAUNCH_MONITOR_LISTEN_PORT
     */
    public int getRemoteLaunchMonitorListenPort() {        
        return this.getPropertyIntValue(REMOTE_LAUNCH_MONITOR_LISTEN_PORT, DEFAULT_REMOTE_LAUNCH_MONITOR_LISTEN_PORT);
    }
    
    /**
     * Gets the heartbeat interval by the Monitor remote launch panel.
     * 
     * @return the user specified heartbeat interval (in milliseconds) or DEFAULT_REMOTE_LAUNCH_HEARTBEAT_INTERVAL_MILLIS, if the user has not specified a value. 
     */
    public int getRemoteLaunchHeartbeatInterval() {        
        return this.getPropertyIntValue(REMOTE_LAUNCH_HEARTBEAT_INTERVAL, DEFAULT_REMOTE_LAUNCH_HEARTBEAT_INTERVAL_MILLIS);
    }
    
    /**
     * Gets a string containing a space-delimited list of port numbers to which the monitor will send broadcast heartbeats.
     * 
     * Multiple port numbers are supported to more easily accommodate port conflicts on the learner workstations.
     * 
     * Example: Workstation remote launch service listens by default on port 50601. If a single workstation has a port conflict on this particular port,
     * only the single workstation property file needs to be modified (i.e. changed to a different port number). Then the new port number is added
     * to the list. Rather than changing properties files for all the workstations (and potentially encountering another conflict).
     * 
     * @return a string containing a space-delimited list of port numbers to which the monitor will send broadcast heartbeats.
     */
    public String getRemoteLaunchWorkstationListenPortList() {

        String value = this.getPropertyValue(REMOTE_LAUNCH_WORKSTATION_LISTEN_PORT_LIST);
        
        if(value == null) {
            
            value = Integer.toString(DEFAULT_REMOTE_LAUNCH_WORKSTATION_LISTEN_PORT);
        }
        
        return value;
    }
    
    /**
     * Size of buffer that holds messages for display in the monitor. Size is
     * count of messages. Default value is 1000. 
     *
     * @return size of buffer used to hold messages
     */
    public int getMessageDisplayBufferSize() {
        return this.getPropertyIntValue(MESSAGE_DISPLAY_BUFFER_SIZE, DEFAULT_MESSAGE_DISPLAY_BUFFER_SIZE);
    }
}
