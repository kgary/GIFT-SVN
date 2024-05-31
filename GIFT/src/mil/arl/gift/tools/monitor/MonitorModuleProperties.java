/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor;

import java.util.ArrayList;
import java.util.List;
import mil.arl.gift.common.module.AbstractModuleProperties;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

/**
 * Contains the Monitor module property values.
 *
 * @author mhoffman
 */
public class MonitorModuleProperties extends AbstractModuleProperties {

    /** the properties file name */
    private static final String PROPERTIES_FILE = "tools/monitor/monitor.properties";

    /** 
     * Properties
     */
    private static final String MESSAGE_DISPLAY_BUFFER_SIZE = "MessageDisplayBufferSize";

    private static final int    DEFAULT_MESSAGE_DISPLAY_BUFFER_SIZE = 1000;

    private static final String POST_DOMAIN_SESSION_BOOKMARK_TIMEOUT = "PostDomainSessionBookmarkTimeoutSeconds";

    private static final int    DEFAULT_DOMAIN_SESSION_BOOKMARK_TIMEOUT_SECONDS = 60; //1 minute
    
    private static final String WEB_TUTOR_MODULE_PORT = "WebTutorModulePort";

    private static final String WEB_TUTOR_MODULE_PATH = "WebTutorModulePath";
    
    private static final String ENABLE_WEBCAM_VIEWING = "EnableWebcamViewing"; 
    
    private static final String MAX_WEBCAM_FBS = "MaxiumWebcamFPS";
        
    private static final int DEFAULT_WEBCAM_FPS = 1;
    
    private static final String REMOTE_LAUNCH_DISCOVERY_BROADCAST_ADDRESS = "RemoteLaunchDiscoveryBroadcastAddress";
    
    private static final int    DEFAULT_REMOTE_LAUNCH_WORKSTATION_LISTEN_PORT = 50601;
    
    private static final String REMOTE_LAUNCH_WORKSTATION_LISTEN_PORT_LIST = "RemoteLaunchWorkstationListenPortList";    
    
    private static final String REMOTE_LAUNCH_MONITOR_LISTEN_PORT = "RemoteLaunchMonitorListenPort";
    
    private static final int    DEFAULT_REMOTE_LAUNCH_MONITOR_LISTEN_PORT = 50600;    

    private static final String REMOTE_LAUNCH_HEARTBEAT_INTERVAL = "RemoteLaunchHeartbeatInterval"; 
    
    private static final int    DEFAULT_REMOTE_LAUNCH_HEARTBEAT_INTERVAL_MILLIS = 5000;   
    
    private static final String APPLICATION_TITLE = "ApplicationTitle";
    
    private static final String DEFAULT_APPLICATION_TITLE = "Module Monitor";
    
    /** singleton instance of this class */
    private static MonitorModuleProperties instance = null;

    /**
     * Return the singleton instance of this class
     *
     * @return MonitorModuleProperties
     */
    public static synchronized MonitorModuleProperties getInstance() {

        if (instance == null) {
            instance = new MonitorModuleProperties();
        }

        return instance;
    }

    /**
     * Class constructor
     */
    private MonitorModuleProperties() {
        super(PROPERTIES_FILE);
    }

    /**
     * Size of buffer that holds messages for display in the monitor. Size is
     * count of messages.
     *
     * @return size of buffer used to hold messages
     */
    public int getMessageDisplayBufferSize() {

        return this.getPropertyIntValue(MESSAGE_DISPLAY_BUFFER_SIZE, DEFAULT_MESSAGE_DISPLAY_BUFFER_SIZE);
    }

    /**
     * Time in seconds to keep a domain session entry in the bookmark editing
     * panel, after the domain session has completed.
     *
     * @return number of milliseconds
     */
    public int getPostDomainSessionBookmarkTimeoutSeconds() {

        return this.getPropertyIntValue(POST_DOMAIN_SESSION_BOOKMARK_TIMEOUT, DEFAULT_DOMAIN_SESSION_BOOKMARK_TIMEOUT_SECONDS);
    }
    
    /**
     * The port to access the Tutor Webserver
     *
     * If this is undefined then the Tutor Webpage cannot be launched
     *
     * @return String The port to access the Tutor Webserver
     */
    public Integer getWebTutorPort() {
        int portNumber = this.getPropertyIntValue(WEB_TUTOR_MODULE_PORT, 0);
        return portNumber != 0 ? portNumber : null;
    }

    /**
     * The path on the Tutor Webserver to access the Tutor Webpage
     *
     * If this is undefined then the Tutor Webpage cannot be launched
     *
     * @return String The path on the Tutor Webserver to access the Tutor Webpage
     */
    public String getWebTutorPath() {
        return this.getPropertyValue(WEB_TUTOR_MODULE_PATH);
    }
    
    /**
     * Whether the monitor should show webcam feeds
     * 
     * @return boolean
     */
    public boolean getEnableWebcamViewing(){
        return this.getPropertyBooleanValue(ENABLE_WEBCAM_VIEWING);
    }
    
    /**
     * Return the maximum webcam frames per second
     * 
     * @return int - max webcam FPS for the monitor
     */
    public int getMaxWebcamFPS(){
        return this.getPropertyIntValue(MAX_WEBCAM_FBS, DEFAULT_WEBCAM_FPS);
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
     * Gets the title of the monitor application window
     *
     * @return String The title of the monitor application window
     */
    public String getApplicationTitle() {
        String value = getPropertyValue(APPLICATION_TITLE);
        return value != null ? value : DEFAULT_APPLICATION_TITLE;
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
            
            value = "" + DEFAULT_REMOTE_LAUNCH_WORKSTATION_LISTEN_PORT;
        }
        
        return value;
    }

    @Override
    public void setCommandLineArgs(String[] args) {

        List<Option> moduleOptionsList = new ArrayList<>();
        
        OptionBuilder.hasArg();
        
        moduleOptionsList.add(OptionBuilder.create(MESSAGE_DISPLAY_BUFFER_SIZE));
        
        OptionBuilder.hasArg();
        
        moduleOptionsList.add(OptionBuilder.create(POST_DOMAIN_SESSION_BOOKMARK_TIMEOUT));
        
        OptionBuilder.hasArg();
        
        moduleOptionsList.add(OptionBuilder.create(WEB_TUTOR_MODULE_PORT));
        
        OptionBuilder.hasArg();
        
        moduleOptionsList.add(OptionBuilder.create(WEB_TUTOR_MODULE_PATH));
        
        OptionBuilder.hasArg();
        
        moduleOptionsList.add(OptionBuilder.create(ENABLE_WEBCAM_VIEWING));
        
        OptionBuilder.hasArg();
        
        moduleOptionsList.add(OptionBuilder.create(MAX_WEBCAM_FBS));
        
        OptionBuilder.hasArg();
        
        moduleOptionsList.add(OptionBuilder.create(REMOTE_LAUNCH_DISCOVERY_BROADCAST_ADDRESS));
        
        OptionBuilder.hasArg();
        
        moduleOptionsList.add(OptionBuilder.create(REMOTE_LAUNCH_WORKSTATION_LISTEN_PORT_LIST));
        
        OptionBuilder.hasArg();
        
        
        moduleOptionsList.add(OptionBuilder.create(REMOTE_LAUNCH_MONITOR_LISTEN_PORT));
                        
        OptionBuilder.hasArg();
        
        moduleOptionsList.add(OptionBuilder.create(REMOTE_LAUNCH_HEARTBEAT_INTERVAL));
        
        OptionBuilder.hasArg();
        
        moduleOptionsList.add(OptionBuilder.create(APPLICATION_TITLE));
        
        setCommandLineArgs(moduleOptionsList, args);        
    }
}
