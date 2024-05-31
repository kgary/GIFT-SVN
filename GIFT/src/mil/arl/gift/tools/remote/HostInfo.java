/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represent host information. Used by both the monitor and the remote launch service for heartbeat.
 * @author cragusa
 *
 */
public class HostInfo implements EncodableDecodable {
    
    /**
     * Type of host, either: Instructor/Experimentor Operator Station, or a workstation.
     * @author cragusa
     *
     */
    public enum HostType {        
        IOS_EOS,
        LEARNER,
        SERVER,
    }
    
    /** The log4j logger instance */
    private static Logger logger = LoggerFactory.getLogger(HostInfo.class);
    
    /** Number of fields in the payload when this message is encoded */
    private static final int PAYLOAD_FIELD_COUNT = 4;
    
    /** Index of the host type within the encoded payload */
    private static final int HOST_TYPE_INDEX = 0;
    
    /** Index of the host name within the encoded payload */
    private static final int HOST_NAME_INDEX = 1;
    
    /** Index of the host IP address within the encoded payload */
    private static final int HOST_ADDR_INDEX = 2;
    
    /** Index of the host port within the encoded payload */
    private static final int HOST_PORT_INDEX = 3;
    
    //self explanatory
    private HostType hostType;
    private String   hostname;    
    private String   ipAddress;
    private int      listenPort;    
    
    /** No-arg constructor, intended only for use by the RemoteMessageUtil */
    HostInfo() {}
    
    /**
     * Full-up constructor designed to ensure that all fields are populated
     * @param type The type of this host.
     * @param hostname host name of this computer
     * @param ipAddress IPv4 address of this computer
     * @param port the port listening on
     */
    public HostInfo(HostType type, String hostname, String ipAddress, int port) {
        
        setHostType(type);
        setHostname(hostname);
        setIpAddress(ipAddress);
        setListenPort(port);
    }
    
    /**
     * Sets the host type.
     * @param hostType
     */
    void setHostType(HostType hostType) {        
        this.hostType = hostType;
    }
    
    /**
     * Gets the host type
     * @return the host type
     */
    public HostType getHostType() {
        return hostType;
    }

    /**
     * Sets the hostname
     * @param hostname
     */
    void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * Gets the hostname
     * @return the hostname
     */
    public String getHostname() {
        return hostname;
    }

    /** 
     * Sets the IP address
     * @param ipAddress
     */
    void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Gets the IP address
     * @return the IP address
     */
    public String getIpAddress() {
        return ipAddress;
    }
    
    /**
     * sets the listen port
     * @param listenPort
     */
    void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }    
    
    /**
     * gets the listen port
     * @return the listen port
     */
    public int getListenPort() {
        return listenPort;
    }    

    @Override
    public int hashCode() {
        
        int retVal = 0;
        
        if( ipAddress != null ) {
            
            retVal = ipAddress.hashCode();
        }
        
        return retVal;
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if(obj != null && obj instanceof HostInfo) {
            
            HostInfo that = (HostInfo)obj;
            
            boolean hostnamesEqual = this.hostname  == null && that.hostname  == null || this.hostname.equals(that.hostname);
            boolean ipAddrsEqual   = this.ipAddress == null && that.ipAddress == null || this.ipAddress.equals(that.ipAddress);
            boolean portsEqual     = this.listenPort == that.listenPort;
            
            return hostnamesEqual && ipAddrsEqual && portsEqual;                        
        }
        else {
            
            return false;
        }
    }
        
    @Override
    public void encode(StringBuffer buffer, char delimeter) {
        
        //HOST_TYPE_INDEX 0
        buffer.append(hostType); 
        
        //HOST_NAME_INDEX 1
        buffer.append(delimeter);
        buffer.append(hostname);
        
        //HOST_ADDR_INDEX 2
        buffer.append(delimeter);
        buffer.append(ipAddress);
        
        //HOST_PORT_INDEX 3
        buffer.append(delimeter);
        buffer.append(listenPort);
        
        if(logger.isDebugEnabled()) {
            
            logger.debug("Encoded this HostInfo as: " + buffer.toString());
        }
    }
       
    
    @Override
    public void decode(String string, char delimeter) {
        
        string = string.trim();
        
        if(logger.isDebugEnabled()) {
            
            logger.debug("decoding: " + string);
        }
        
        String[] tokens = string.split("\\" + delimeter, PAYLOAD_FIELD_COUNT);
        
        if( tokens.length != PAYLOAD_FIELD_COUNT ) {
            
            logger.warn("Decoding: " + string);
            logger.warn("Incorrect number of tokens found: " + tokens.length);
            logger.warn("not decoding!");
        }
        else {
            
            //TODO: see if I can get rid of the calls to trim
            setHostType(HostType.valueOf(tokens[HOST_TYPE_INDEX]));
            setHostname(tokens[HOST_NAME_INDEX]);
            setIpAddress(tokens[HOST_ADDR_INDEX]);
            setListenPort( Integer.parseInt( tokens[HOST_PORT_INDEX]));   
        }
    }
        
    @Override
    public String toString() {
        return hostname + ":" + ipAddress + ":" + listenPort;
    }    
    
    
    //Testing
    public static void main(String[] args) {
    
        HostInfo hostInfo = new HostInfo();
        
        hostInfo.setHostType(HostType.LEARNER);
        hostInfo.setHostname("foo");
        hostInfo.setIpAddress("0.0.0.0");
        //hostInfo.setListenPort(2050);
        
        try {

            System.out.println(hostInfo);        
            String encodedHostInfo = RemoteMessageUtil.encode(hostInfo);        
            System.out.println(encodedHostInfo);        
            EncodableDecodable recoveredHostInfo = RemoteMessageUtil.decode(encodedHostInfo);        
            System.out.println(recoveredHostInfo);

            LaunchCommand launchCmd = new LaunchCommand("start ums -d");
            System.out.println(launchCmd);        
            String encodedLaunchCmd = RemoteMessageUtil.encode(launchCmd);        
            System.out.println(encodedLaunchCmd);
            EncodableDecodable recoveredLaunchCommand = RemoteMessageUtil.decode(encodedLaunchCmd);        
            System.out.println(recoveredLaunchCommand);
        }
        catch(Exception ex) {
            System.err.println(ex);
        }        
    }


    @Override
    public RemoteMessageType getMessageType() {
        
        return RemoteMessageType.HOST_INFO;
    }    
}
