/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server.webmonitor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import mil.arl.gift.tools.dashboard.server.DashboardProperties;
import mil.arl.gift.tools.remote.EncodableDecodable;
import mil.arl.gift.tools.remote.HostInfo;
import mil.arl.gift.tools.remote.LaunchCommand;
import mil.arl.gift.tools.remote.RemoteMessageType;
import mil.arl.gift.tools.remote.RemoteMessageUtil;
import mil.arl.gift.tools.remote.ScriptTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages and identifies remote clients
 *
 * @author jleonard
 */
public class RemoteClientManager {
    
    /** Interval at which heartbeats will be sent */
    private static final int HEARTBEAT_INTERVAL_MILLIS = DashboardProperties.getInstance().getRemoteLaunchHeartbeatInterval();

    /** Timeout for workstations: If TIMEOUT_INTERVAL_MILLIS elapses without receipt of
     * a heartbeat from a workstation, that workstation will be assumed to be
     * offline.
     */
    private static final int TIMEOUT_INTERVAL_MILLIS = HEARTBEAT_INTERVAL_MILLIS + 500;
    
    private static RemoteClientManager instance = null;
    
    /**
     * Gets the active instance of the remote client manager
     *
     * @return RemoteClientManager The active instance of the remote client
     * manager
     */
    public static RemoteClientManager getInstance() {

        if (instance == null) {

            instance = new RemoteClientManager();
        }

        return instance;
    }

    /**
     * Class used to send Remote Launch Commands to Remote Launch Service
     *
     * @author cragusa
     */
    private class SendSocket {

        /** Socket for sending remote launch commands */
        private DatagramSocket socket;

        /** Port numbers to which heartbeats should be broadast */
        private int[] heartbeatDestPorts;

        /** Broadcast address for sending heartbeats */
        private InetAddress broadcastAddress;

        /** Buffer in which to store the heartbeat information -- same information is sent
         * every heartbeat */
        private byte[] heartbeatBuffer;

        /** Constant used within heartbeatDestPorts as place holder when parsing a port
         * number fails */
        private static final int BAD_PORT_NUMBER = -1;

        /**
         * Sets up the send socket
         *
         * @throws SocketException
         */
        private void setupSendSocket() throws SocketException {

            socket = new DatagramSocket();
            socket.setBroadcast(true);
            socket.setReuseAddress(true);
        }

        /** Default constructor */
        private SendSocket() throws SocketException {

            setupSendSocket();
        }

        /**
         * Finds all the network interfaces, the addresses for each. Iterates
         * till a broadcast address is found.
         *
         * @return the first IPv4 broadcast address found
         * @throws IOException
         */
        //TODO: move this into common package
        private String discoverBroadcastAddress() throws IOException {

            String broadcastAddress = null;

            System.setProperty("java.net.preferIPv4Stack", "true"); //without this it will return "255.255.255.255" every time.

            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            boolean foundBroadcast = false;

            while (interfaces.hasMoreElements() && !foundBroadcast) {

                NetworkInterface networkInterface = interfaces.nextElement();

                if (networkInterface.getInterfaceAddresses().size() < 1) {
                    //ignore
                } else if (networkInterface.isLoopback()) {
                    //ignore
                } else {

                    logger.info("Network Interface Name: " + networkInterface.getName());

                    for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {

                        logger.info("Interface Address: " + interfaceAddress.getAddress());

                        InetAddress broadcast = interfaceAddress.getBroadcast();

                        if (broadcast == null) {
                            continue;
                        } else {

                            broadcastAddress = broadcast.getCanonicalHostName();
                            logger.info("Broadcast Address: " + broadcastAddress);
                            foundBroadcast = true;
                            break;
                        }
                    }
                }
            }

            return broadcastAddress;
        }

        /**
         * Logs the broadcast ports for this socket
         * 
         * @param ports The ports to log
         */
        private void logBroadcastPorts(int[] ports) {

            StringBuilder parsedPorts = new StringBuilder();

            for (int i = 0; i < ports.length; i++) {

                parsedPorts.append(" ").append(ports[i]);
            }

            logger.info("Broadcast heartbeats to the following ports:" + parsedPorts.toString());
        }

        /**
         * Sets up the list of ports to heatbeat
         *
         * @param hostInfo
         * @return
         * @throws IOException
         */
        private boolean setupHeartbeat(HostInfo hostInfo) throws IOException {

            boolean success = false;

            String broadcastAddressAsString = DashboardProperties.getInstance().getRemoteLaunchDiscoveryBroadcastAddress();

            if (broadcastAddressAsString == null) {

                broadcastAddressAsString = discoverBroadcastAddress();

                logger.info("broadcastAddressAsString: " + broadcastAddressAsString);
            }

            if (broadcastAddressAsString != null) {

                broadcastAddress = InetAddress.getByName(broadcastAddressAsString);

                heartbeatBuffer = RemoteMessageUtil.encode(hostInfo).getBytes();

                String destPortList = DashboardProperties.getInstance().getRemoteLaunchWorkstationListenPortList();

                String[] tokens = destPortList.split(" ");

                heartbeatDestPorts = new int[tokens.length];

                for (int i = 0; i < heartbeatDestPorts.length; i++) {

                    try {

                        heartbeatDestPorts[i] = Integer.parseInt(tokens[i].trim());
                    } catch (@SuppressWarnings("unused") Exception ex) {

                        logger.warn("failed to parse port from string: " + tokens[i]);

                        heartbeatDestPorts[i] = BAD_PORT_NUMBER;
                    }
                }

                logBroadcastPorts(heartbeatDestPorts);

                success = (heartbeatDestPorts != null) && (heartbeatDestPorts.length > 0) && (broadcastAddress != null) && (heartbeatBuffer != null);
            }

            return success;
        }

        /**
         * Sends a heartbeat packet
         * 
         * @throws IOException 
         */
        private synchronized void sendHeartbeatPacket() throws IOException {

            for (int i = 0; i < heartbeatDestPorts.length; i++) {

                if (heartbeatDestPorts[i] != BAD_PORT_NUMBER) {

                    if (logger.isDebugEnabled()) {

                        StringBuilder buffer = new StringBuilder();

                        buffer.append("Sending Heartbeat Packet: ");
                        buffer.append(" heartbeatBuffer=").append(heartbeatBuffer.toString());
                        buffer.append(" heartbeatBuffer.length=").append(heartbeatBuffer.length);
                        buffer.append(" broadcastAddress=").append(broadcastAddress);
                        buffer.append(" heartbeatDestPorts[").append(i).append("]=").append(heartbeatDestPorts[i]);

                        logger.debug(buffer.toString());
                    }

                    DatagramPacket packet = new DatagramPacket(heartbeatBuffer, heartbeatBuffer.length, broadcastAddress, heartbeatDestPorts[i]);

                    socket.send(packet);
                }
            }

        }

        /**
         * Sends a launch command to a remote host.
         *
         * @param command the launch command
         * @param hostInfo about the recipient receiving the command
         * @throws IOException if there was a problem sending the command
         */
        public synchronized void sendLaunchCommand(String command, HostInfo hostInfo) throws IOException {

            if (logger.isInfoEnabled()) {

                logger.info("sending launch command " + command + " to host: " + hostInfo);
            }

            LaunchCommand launchCommand = new LaunchCommand(command);

            byte[] buffer = RemoteMessageUtil.encode(launchCommand).getBytes();

            InetAddress inetAddress = InetAddress.getByName(hostInfo.getIpAddress());

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, inetAddress, hostInfo.getListenPort());

            socket.send(packet);
            
            //this is a possible fix for the issue when the monitor doesn't send a launch command message for Launch All related
            //requests.  It was noticed that the first 1 to 4 requests in a batch (usually UMS, LMS, Ped, then Learner) would not be sent.
            //This wait logic between sending commands seem to fix the issue.
            try {
                this.wait(100);
            } catch (@SuppressWarnings("unused") InterruptedException e) {
                // nothing to do
            }
        }
    }

    /**
     * Listens for packets from the remote GIFT host.
     *
     * @author cragusa
     */
    private class GiftHostListener extends Thread {

        /** socket on which to receive packets from the remote host */
        private DatagramSocket recvSocket;

        /**
         * @param port the port on which to listen for
         * @throws IOException
         */
        private GiftHostListener(int port) throws IOException {

            recvSocket = new DatagramSocket(port);
        }

        /**
         * Schedules the removal a remote host entry.
         *
         * @param entry the entry to remove
         */
        private void scheduleEntryRemoval(final HostInfo hostInfo) {

            Timer timer = new Timer(TIMEOUT_INTERVAL_MILLIS, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {

                    remoteClientOffline(hostInfo);

                    if (logger.isInfoEnabled()) {

                        logger.info("Removed: " + hostInfo + "  (timed out)");
                    }

                    remoteHostToTimer.remove(hostInfo);
                }
            });

            timer.setRepeats(false);
            remoteHostToTimer.put(hostInfo, timer);
            timer.start();
        }

        @Override
        public void run() {

            for (;;) {

                byte[] buffer = new byte[RemoteMessageUtil.RECV_BUFFER_SIZE];

                DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);

                try {

                    Arrays.fill(buffer, (byte) 0);

                    recvSocket.receive(incomingPacket);
                    String incomingString = new String(incomingPacket.getData());
                    incomingString = incomingString.trim();

                    EncodableDecodable obj = RemoteMessageUtil.decode(incomingString);

                    if (obj.getMessageType().equals(RemoteMessageType.HOST_INFO)) {

                        final HostInfo hostInfo = (HostInfo) obj;

                        if (!remoteHostToTimer.containsKey(hostInfo)) {

                            SwingUtilities.invokeLater(new Runnable() {

                                @Override
                                public void run() {

                                    remoteClientOnline(hostInfo);
                                    logger.info("Added: " + hostInfo);
                                    scheduleEntryRemoval(hostInfo);
                                }
                            });
                        } else {

                            if (remoteHostToTimer.containsKey(hostInfo)) {

                                //restart the timer so the entry doesn't time out.
                                remoteHostToTimer.get(hostInfo).restart();
                            } else {

                                logger.warn("Couldn't find key!");
                            }
                        }
                    } else {

                        logger.warn("Received unrecognized packet: " + incomingString);
                    }

                } catch (IOException e) {

                    logger.warn("Caught IOException when trying to decode incoming packet", e);
                }
            }
        }
    }
    private static Logger logger = LoggerFactory.getLogger(RemoteClientManager.class);

    /** Used to send remote launch commands */
    private SendSocket sendSocket;

    /** Timer to manage timeouts of remote host entries */
    private Map<HostInfo, Timer> remoteHostToTimer = new HashMap<>();

    /** host information for this RemotePanel - will be broadcast to the workstations */
    private HostInfo myHostInfo;

    private final Set<RemoteClientStatusListener> remoteClientStatusListeners = new HashSet<>();
    
    private final String launchProcessScriptPath = ScriptTools.generateScriptFilePathName();

    /**
     * Default Constructor
     */
    private RemoteClientManager() {

        try {
            sendSocket = new SendSocket();
        } catch (SocketException e) {
            logger.error("Caught an IOException while creating a SendSocket", e);
        }

        final int listenPort = DashboardProperties.getInstance().getRemoteLaunchMonitorListenPort();

        try {
            GiftHostListener giftHostListener = new GiftHostListener(listenPort);
            giftHostListener.start();
        } catch (IOException e) {
            logger.error("Caught an IOException while creating a GiftHostListener", e);
        }

        //Put this into a thread to speed up launch time of Monitor.
        Runnable startHeartbeatRunnable = new Runnable() {

            @Override
            public void run() {
                startHeartbeat(listenPort);
            }
        };

        new Thread(startHeartbeatRunnable).start();
    }

    private void setupHostInfo(int listenPort) throws UnknownHostException {

        //TODO: If I have multiple NIC's and/or multiple IP addresses how do I know
        // the one I get here is the one I want to use?
    	//MH 11/6/14 - look at the new net.util.Util.java for all local addresses
        String hostname = InetAddress.getLocalHost().getHostName();
        String hostAddr = InetAddress.getLocalHost().getHostAddress();

        myHostInfo = new HostInfo(HostInfo.HostType.IOS_EOS, hostname, hostAddr, listenPort);
    }

    /**
     * Starts heartbeating on the heartbeat port
     * 
     * @param listenPort The port to heartbeat on
     */
    private void startHeartbeat(int listenPort) {

        boolean success = true;

        try {

            setupHostInfo(listenPort);

        } catch (UnknownHostException e) {

            logger.error("Caught UnknownHostException", e);
            success = false;
        }

        if (success) {

            try {
                success = sendSocket.setupHeartbeat(myHostInfo);
                logger.info("sendSocket.setupHeartbeat success=" + success);
            } catch (Exception e2) {

                logger.error("Caught Exception while trying to setup the sendSocket", e2);
            }
        }

        if (success) {

            TimerTask heartbeatTask = new TimerTask() {

                @Override
                public void run() {

                    try {

                        RemoteClientManager.this.sendSocket.sendHeartbeatPacket();

                    } catch (IOException e) {
                        logger.error("Caught an IOException while sending a heartbeat packet", e);
                    }
                }
            };

            java.util.Timer heartbeatTimer = new java.util.Timer("Heartbeat");

            heartbeatTimer.scheduleAtFixedRate(heartbeatTask, 0, HEARTBEAT_INTERVAL_MILLIS);
        }
    }

    /**
     * Notifies listeners that a remote client has been found
     * 
     * @param hostInfo The info about the remote client
     */
    private void remoteClientOnline(HostInfo hostInfo) {
        for (RemoteClientStatusListener listener : remoteClientStatusListeners) {
            
            try{
                listener.onRemoteClientOnline(hostInfo);
            }catch(Exception e){
                logger.error("Caught exception from misbehaving listener "+listener, e);
            }
        }
    }

    /**
     * Notifies listeners that a remote client has went offline
     * 
     * @param hostInfo The info about the offline client
     */
    private void remoteClientOffline(HostInfo hostInfo) {
        for (RemoteClientStatusListener listener : remoteClientStatusListeners) {
            
            try{
                listener.onRemoteClientOffline(hostInfo);
            }catch(Exception e){
                logger.error("Caught exception from misbehaving listener "+listener, e);
            }
        }
    }
    
    /**
     * Sends the command to the remote client or executes it locally
     * 
     * @param command The command to execute
     * @param hostInfo The host to send the command to
     * @return String
     * @throws IOException if there was a problem sending the command
     */
    public String sendCommand(String command, HostInfo hostInfo) throws IOException {
        
        if (hostInfo.getIpAddress().equals("127.0.0.1")) {

            if (command.startsWith("launchWebpage")) {

                final String[] commandArgs = command.split(" ");

                try {                    

                    // NOTE: This only works on Microsoft Windows
                    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler \"" + new URI(commandArgs[1]) + "\"");

                } catch (@SuppressWarnings("unused") URISyntaxException ex) {
                } catch (@SuppressWarnings("unused") IOException ex) {
                }

            } else {

                Process p = Runtime.getRuntime().exec(launchProcessScriptPath + " " + command);
                
                if (p == null) {
                    
                    String warning = "Failed to execute command: '" + command + "'";
                    logger.warn(warning);
                    return warning;
                }
            }
            
        } else {

            sendSocket.sendLaunchCommand(command, hostInfo);
        }

        return hostInfo.getHostname() + ": " + command;
    }

    /**
     * Adds a listener interested in getting updates about remote clients
     *
     * @param listener The interested listener
     */
    public void addRemoteClientStatusListener(RemoteClientStatusListener listener) {
        remoteClientStatusListeners.add(listener);
    }

    /**
     * Removes a listener no longer interested in getting updates about remote
     * clients
     *
     * @param listener The uninterested listener
     */
    public void removeRemoteClientStatusListener(RemoteClientStatusListener listener) {
        remoteClientStatusListeners.remove(listener);
    }
}
