/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.remote;

import java.awt.Desktop;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.tools.remote.HostInfo.HostType;


/**
 * Program to run on GIFT learner or server hosts, to allow remote start of GIFT modules from the Instructor/Operator Station (Monitor)
 * 
 * @author cragusa
 *
 */
public class RemoteLaunchService extends Thread implements LaunchConstants {

    private static Logger logger = LoggerFactory.getLogger(RemoteLaunchService.class);
        
    static {
        //use log4j
        PropertyConfigurator.configureAndWatch(PackageUtil.getConfiguration() + "/tools/remote/remote.launch.service.log4j.properties");
    }

    
    /** Swing Timer used to send heartbeats */
    private Timer heartbeatTimer = new Timer("HeartbeatTimer");
            
    /** fully qualified path name to the launch script */
    private String launchScriptPathName;    
    
    /** socket on which to send heart beat packets */
    private DatagramSocket sendSocket;    
    
    /** Socket over which commands are received */
    private DatagramSocket recvSocket;

    /** Datagram packet containing the heart beat information. The same packet gets reused over and over */
    private DatagramPacket heartbeatPacket;
    
    /** time interval in milliseconds between heartbeats */
    private static final int REMOTE_HEARTBEAT_INTERVAL = RemoteLaunchServiceProperties.getInstance().getRemoteLaunchHeartbeatInterval();
    
    /** Time in milliseconds after which (if not heartbeat is received) the Monitor Module is assumed to have dropped off the network. */
    private static final int TIME_OUT = REMOTE_HEARTBEAT_INTERVAL + 500;
    
    /** Map of of monitor hostInfo's to an object containing the HostInfo and a timestamp */
    private final Map<HostInfo, HostInfoWithTimeStamp> hostInfoToTimeStampedHostInfo = new HashMap<>();
        
    /** Simple list used to accumulate stale HostInfos (map key) for subsequent removal.*/
    private final List<HostInfo> staleHostInfoList = new ArrayList<>();
    
    /**
     * Simple container to hold a HostInfo along with a time stamp.
     * Could have also created a subclass of HostInfo that included a time stamp. Chose the 
     * @author cragusa
     *
     */
    class HostInfoWithTimeStamp {
    	
    	/** the HostInfo of interest */
    	private HostInfo hostInfo;
    	
    	/** Time (in milliseconds since Epoch) of last update of the associated HostInfo instance */
    	private long     lastUpdateTime;
    	
    	/**
    	 * Constructor
    	 * 
    	 * @param hostInfo the HostInfo to encapsulate.
    	 */
    	HostInfoWithTimeStamp(HostInfo hostInfo) {
    		
    		this.setHostInfo(hostInfo);
    	}
    	
    	/**
    	 * Sets the hostInfo
    	 * @param hostInfo the new/updated value of hostInfo
    	 */
    	private void setHostInfo(HostInfo hostInfo) {
    		
    		this.hostInfo = hostInfo;
    		this.lastUpdateTime = System.currentTimeMillis();
    	}
    	
    	/**
    	 * Gets hostInfo
    	 * 
    	 * @return reference to this.hostInfo.
    	 */
    	HostInfo getHostInfo() {
    		
    		return this.hostInfo;
    	}
    	
    	/**
    	 * Checks if the contained HostInfo instance is stale.
    	 * 
    	 * @param time (in milliseconds since epoch) to compare.
    	 * 
    	 * @return true if hostInfo is stale. Otherwise, false.
    	 */
    	boolean isStale(long time) {
    		
    		return (time - lastUpdateTime) > TIME_OUT;
    	}
    	
    }
    
    /**
     * Sets up the heartbeat packet for the workstation where this remote launch service is running.
     * 
     * @param listenPort the port on which this remote launch service is listening.
     * 
     * @throws UnknownHostException
     */
    private void setupHeartbeatPacket(int listenPort) throws UnknownHostException, IOException {
                
        HostInfo myHostInfo = new HostInfo();
        
        myHostInfo.setHostType(HostType.LEARNER); //TODO: get this from properties file?  
        
        String hostname = InetAddress.getLocalHost().getHostName();
        myHostInfo.setHostname(hostname);   
        
        String hostAddr = InetAddress.getLocalHost().getHostAddress();     
        myHostInfo.setIpAddress(hostAddr);
                
        myHostInfo.setListenPort(listenPort);

        logger.info("myHostInfo: " + myHostInfo);
        
        byte[] encodedMyHostInfo = RemoteMessageUtil.encode(myHostInfo).getBytes();
        
        heartbeatPacket = new DatagramPacket(encodedMyHostInfo, encodedMyHostInfo.length);
    }    
    
    
    /**
     * Set up the socket on which heart beats will be sent.
     * 
     * @throws SocketException
     * @throws UnknownHostException
     */
    private void setupSendSocket() throws IOException, SocketException, UnknownHostException {
        
        sendSocket = new DatagramSocket();
        sendSocket.setReuseAddress(true);
    }
        
    
    /**
     * Execute a command string
     * 
     * @param cmd the command string
     */
    private void execCommand(String cmd) {

        if (logger.isDebugEnabled()) {

            logger.debug("execCommand: " + cmd);
        }

        if (cmd.startsWith(LAUNCH_WEBPAGE_COMMAND)) {
            String[] command = cmd.split(" ");
            try {
                Desktop.getDesktop().browse(new URI(command[1]));
            } catch (@SuppressWarnings("unused") URISyntaxException | IOException ex) {
            }

        } else {

            try {

                Process p = Runtime.getRuntime().exec(launchScriptPathName + " " + cmd);

                if (p == null) {

                    logger.warn("Failed to execute command: '" + cmd + "'");
                }

            } catch (IOException e) {

                logger.error("Caught IOException while trying to execCommand: " + cmd, e);
            }
        }
    }    
    
    
    /**
     * Sends a single heartbeat to a single Monitor
     */    
    private void sendHeartBeatToMonitor(HostInfo monitorHostInfo) {
    	
        try {
            
            if( monitorHostInfo != null ) {
                
                InetAddress destAddress = InetAddress.getByName(monitorHostInfo.getIpAddress());
                
                int destPort = monitorHostInfo.getListenPort();
                
                heartbeatPacket.setAddress(destAddress);
                
                heartbeatPacket.setPort(destPort);
                
                if(logger.isDebugEnabled()) {
                    
                    logger.debug("sending heartbeat to: " + destAddress.getCanonicalHostName() + ":" + destPort);
                }
                
                sendSocket.send(heartbeatPacket);
            }  
            else {
                
                if(logger.isDebugEnabled()) {
                    
                    logger.debug("Not sending heartbeat: ");
                    logger.debug("\tmonitorHostInfo: " + monitorHostInfo);
                }
            }
            
        } catch (IOException e) {
            
            logger.error("sendHeartBeat failed: ", e);
        }    	
    	
    }
    

    
        
    /**
     * Sends heartbeats to all known monitors (that are not stale).
     *  
     */

    private void sendHeartBeats()  {
    	
    	staleHostInfoList.clear();
    	
    	synchronized (hostInfoToTimeStampedHostInfo) {
    	
	    	for( HostInfoWithTimeStamp hostInfoWithTimeStamp : hostInfoToTimeStampedHostInfo.values() ) {
	    	
	    		long now = System.currentTimeMillis();
	    	
	    		if(hostInfoWithTimeStamp.isStale(now)) {
	    			
	    			staleHostInfoList.add(hostInfoWithTimeStamp.getHostInfo());
	    		}
	    		else {
	    			
	    			sendHeartBeatToMonitor(hostInfoWithTimeStamp.getHostInfo()); 			
	    		}
	    	}
	    	
	    	for(HostInfo hostInfoKey: staleHostInfoList) {
	    		
	    		hostInfoToTimeStampedHostInfo.remove(hostInfoKey);	    		
	    	}
    	}    	
    }    
    
    /**
     * Begin sending heartbeats. Should be called only once.
     */
    private void startHeartbeats() {

        final TimerTask heartbeatTask = new TimerTask() {

            @Override
            public void run() {
                
                sendHeartBeats(); 
            }            
        };
        
        logger.info("scheduling heartbeats with interval of: " + REMOTE_HEARTBEAT_INTERVAL);
        
        heartbeatTimer.schedule(heartbeatTask, 0, REMOTE_HEARTBEAT_INTERVAL);
    }
        
    
    /**
     * No-arg constructor
     * 
     * @throws IOException if any of the socket operations fail
     */
    RemoteLaunchService() throws IOException {
        
        setName("RemoteLaunchService");
        
        launchScriptPathName = ScriptTools.generateScriptFilePathName();
        
        logger.info("launchScriptPathName: " + launchScriptPathName);
        
        int listenPort = RemoteLaunchServiceProperties.getInstance().getRemoteLaunchWorkstationListenPort();

        recvSocket = new DatagramSocket(listenPort, InetAddress.getByName(WILDCARD_ADDRESS));
        
        setupSendSocket();
        
        setupHeartbeatPacket(listenPort);
        
        startHeartbeats();
    }
        
    
    static {
        
        PropertyConfigurator.configure(PackageUtil.getConfiguration() + "/tools/remote/remote.launch.service.log4j.properties");
    }
    
    
    private static void printDebugLevel() {
    	
    	if (logger.isTraceEnabled()) {
    		
    		System.out.println("log4j: trace");
    	}    	
    	else if(logger.isDebugEnabled()) {
    		
    		System.out.println("log4j: debug");
    	}
    	else if (logger.isInfoEnabled()) {
    		
    		System.out.println("log4j: info");
    		
    	}
    	else if (logger.isWarnEnabled()) {
    		
    		System.out.println("log4j: warn");
    		
    	}
    	else if (logger.isErrorEnabled()) {
    		
    		System.out.println("log4j: debug");
    		
    	}
    	else {
    		
    		System.out.println("unknown");
    	}
    	
    }
    
    
    
    /**
     * Main program entry point
     * 
     * @param args not used
     */
    public static void main(String[] args) {
        
        try {

        	printDebugLevel();
        	
            new RemoteLaunchService().start();
        }
        catch(IOException ex) {
            
            logger.error("Caught IOException while starting RemoteLaunchService: " + ex);
        }
    }     
  
    
    /**
     * Adds a hostInfo to the hostInfoToTimeStampedHostInfo map if not already present.
     * Otherwise updates the hostInfo (including updating the update time).
     * 
     * @param hostInfo the hostInfo to be added or updated
     */
    private void addUpdateHostInfo(HostInfo hostInfo) {
    
    	synchronized(hostInfoToTimeStampedHostInfo) {
    		
	    	if( !hostInfoToTimeStampedHostInfo.containsKey(hostInfo) ) {
	    		
	    		hostInfoToTimeStampedHostInfo.put(hostInfo, new HostInfoWithTimeStamp(hostInfo));    		
	    	}
	    	else {
	    		
	    		hostInfoToTimeStampedHostInfo.get(hostInfo).setHostInfo(hostInfo);
	    	}
    	}
    }
        
    
    /**
     * run method for the main thread
     * 
     */
    @Override
    public void run() {
            
        logger.info("Starting listening thread");
        
        byte[] buffer = new byte[RemoteMessageUtil.RECV_BUFFER_SIZE];
        
        DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);
        
        for(;;) {
            
            Arrays.fill(buffer, (byte)0);
            
            try {
                
                recvSocket.receive(incomingPacket);
                String string = new String(incomingPacket.getData());
                
                EncodableDecodable msg = RemoteMessageUtil.decode(string);
                
                if(msg.getMessageType().equals(RemoteMessageType.LAUNCH_COMMAND)) {

                    if(logger.isInfoEnabled()) {                        
                        logger.info("Received msg: " + msg);
                    }    
                    
                    LaunchCommand launchCommand = (LaunchCommand)msg;                    
                    String commandString = launchCommand.getLaunchString();
                    
                    execCommand(commandString);
                }
                else if (msg.getMessageType().equals(RemoteMessageType.HOST_INFO)) {
                    
                    if(logger.isDebugEnabled()) {
                        
                        logger.debug("Received msg: " + msg);
                    }
                    
                    addUpdateHostInfo( (HostInfo)msg );
                }
                else {
                    
                    logger.error("Unrecognized message");
                }
                
            } catch (IOException e) {
                
                logger.error("Caught IOException in RemoteLaunchService::run()", e);
            }
        }
    }
    
}
