/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.remote;

import mil.arl.gift.common.module.AbstractModuleProperties;


public class RemoteLaunchServiceProperties extends AbstractModuleProperties {
     
    /** the properties file name */
    private static final String PROPERTIES_FILE = "tools/remote/remote.launch.service.properties";
           
    private static final String REMOTE_LAUNCH_WORKSTATION_LISTEN_PORT = "RemoteLaunchWorkstationListenPort";
    
    private static final int    DEFAULT_REMOTE_LAUNCH_WORKSTATION_LISTEN_PORT = 50601;
    
    private static final String REMOTE_LAUNCH_HEARTBEAT_INTERVAL = "RemoteLaunchHeartbeatInterval"; 
    
    private static final int    DEFAULT_REMOTE_LAUNCH_HEARTBEAT_INTERVAL_MILLIS = 5000;   
        
    
    /** singleton instance of this class */
    private static RemoteLaunchServiceProperties instance = null;

    
    /**
     * Return the singleton instance of this class
     *
     * @return MonitorModuleProperties
     */
    public static synchronized RemoteLaunchServiceProperties getInstance() {

        if (instance == null) {
            instance = new RemoteLaunchServiceProperties();
        }

        return instance;
    }
    
    /**
     * Class constructor
     */
    private RemoteLaunchServiceProperties() {
        super(PROPERTIES_FILE);
    }
        
    /**
     * Gets the port number used by the remote launch service when listening for commands or heartbeats broadcast by the Monitor.
     * 
     * @return the port specified in the properties file if it exists. otherwise returns DEFAULT_REMOTE_LAUNCH_WORKSTATION_LISTEN_PORT
     */
    public int getRemoteLaunchWorkstationListenPort() {        
        return this.getPropertyIntValue(REMOTE_LAUNCH_WORKSTATION_LISTEN_PORT, DEFAULT_REMOTE_LAUNCH_WORKSTATION_LISTEN_PORT);
    }
        
    /**
     * Gets the heartbeat interval used by both the Monitor and the remote launch service for their respective heartbeats.
     * 
     * @return the user specified heartbeat interval (in milliseconds) or DEFAULT_REMOTE_LAUNCH_HEARTBEAT_INTERVAL_MILLIS, if the user has not specified a value. 
     */
    public int getRemoteLaunchHeartbeatInterval() {        
        return this.getPropertyIntValue(REMOTE_LAUNCH_HEARTBEAT_INTERVAL, DEFAULT_REMOTE_LAUNCH_HEARTBEAT_INTERVAL_MILLIS);
    }
    
}
