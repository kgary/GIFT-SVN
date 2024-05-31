/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.io.DetailedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to manage a Java network socket for GIFT.
 * 
 * @author mhoffman
 *
 */
public class SocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(SocketHandler.class);  
    
    /** max amount of time in seconds to wait before failing to establish a connection */
    private static final int ESTABLISH_CONNECTION_TIMEOUT = 5000;
    
    /** max amount of time to wait for a response on the opened connection */
    private static final int RESPONSE_TIMEOUT = 10000;
        
    /** the network connection for sending and receiving messages */
    protected Socket socket;
    
    /** the address of the connection (e.g. 10.1.21.13:3000 */
    private SocketAddress socketAddress;
    
    private ApplicationStateListener appStateListener = null;
    
    /** listens for incoming byte data over the socket. */
    private ApplicationByteListener appByteListener = null;
    
    /** The amount of bytes for the appByteListener to read in at a time */
    private int byteListenerReadSize = 0;
    
    private SocketReader socketReader = null;
    
    /**
     * Create a socket handler that will not listen for messages at all times but can
     * send messages whenever needed.
     * 
     * Make sure to call connect() before attempting to send messages.
     * 
     * @param address the address of the socket (e.g. "http://localhost", "10.1.21.123")   
     * @param port the port to connect to and listen on (e.g. 12345)  
     */
    public SocketHandler(String address, int port){        
        socketAddress = new InetSocketAddress(address, port);        
    }
    
    /**
     * Create a socket handler that will listen for messages at all times and can
     * send messages whenever needed.
     * 
     * Make sure to call connect() before attempting to send messages.
     * 
     * @param address the address of the socket (e.g. "http://localhost", "10.1.21.123")   
     * @param port the port to connect to and listen on (e.g. 12345)  
     * @param appStateListener the listener of training application state messages used to notify of
     * states received on this socket connection (only after the socket is connected)
     */
    public SocketHandler(String address, int port, ApplicationStateListener appStateListener){
        this(address, port);
        this.appStateListener = appStateListener;
    }
    
    /**
     * Create a socket handler that will listen for messages at all times and can
     * send messages whenever needed.
     * 
     * Make sure to call connect() before attempting to send messages.
     * 
     * @param address the address of the socket (e.g. "http://localhost", "10.1.21.123")   
     * @param port the port to connect to and listen on (e.g. 12345)  
     * @param appByteListener the listener of training application byte messages used to notify of
     * states received on this socket connection (only after the socket is connected)
     * @param byteListenerReadSize the amount of bytes for the appByteListener to read in at a time
     */
    public SocketHandler(String address, int port, ApplicationByteListener appByteListener, int byteListenerReadSize){
        this(address, port);
        this.appByteListener = appByteListener;
        this.byteListenerReadSize = byteListenerReadSize;
    }
    
    /**
     * Connects this socket to the server. 
     * The connection will then block until established or an error occurs.
     * 
     * @throws IOException if an error occurs during the connection
     * @throws SocketException if timeout expires before connecting or if there is an error in the underlying protocol, such as a TCP error.
     * @throws IllegalArgumentException if endpoint is null, if a SocketAddress subclass not supported by this socket, 
     *                                  if appByteListener is null, or if byteListenerReadSize is less than 0.
     */
    public void connect() throws IOException, SocketException, IllegalArgumentException{
        
        if(socket == null){
            socket = new Socket();  
        }
        
        if(!socket.isConnected()){
            
            if(logger.isDebugEnabled()){
                logger.debug("Making connection to "+socketAddress);
            }
            socket.connect(socketAddress, ESTABLISH_CONNECTION_TIMEOUT);
            socket.setSoTimeout(RESPONSE_TIMEOUT);
            
            if(appStateListener != null){
                
                if(logger.isDebugEnabled()){
                    logger.debug("Setting socket listener "+appStateListener);
                }
                socketReader = new SocketReader(socket, appStateListener);
                socketReader.start();
            }
            else if (appByteListener != null) {
                
                if(logger.isDebugEnabled()){
                    logger.debug("Setting socket byte listener "+appByteListener);
                }
                
                socketReader = new SocketReader(socket, appByteListener, byteListenerReadSize);
                socketReader.start();
            }
            
            if(logger.isInfoEnabled()){
                logger.info("Successfully made connection to socket at address "+socketAddress);
            }
        }
    }
    
    /**
     * Return whether the socket connection is connected.
     * 
     * @return is the socket connected
     */
    public boolean isConnected(){
        return socket != null && socket.isConnected();
    }
    
    /**
     * Closes this socket. 
     * Any thread currently blocked in an I/O operation upon this socket will throw a SocketException. 
     * Once a socket has been closed, it is not available for further networking use (i.e. can't be reconnected or rebound). 
     * A new socket needs to be created. 
     * 
     * @throws IOException if an I/O error occurs when closing this socket.
     */
    public void disconnect() throws IOException{
        
        if(socket != null){
            
            if(logger.isInfoEnabled()){
                logger.info("Closing socket connection to "+socketAddress);
            }
            socket.close();
            socket = null;
            
            if(socketReader != null){
                
                if(logger.isInfoEnabled()){
                    logger.info("Stopping socket reader for socket connection to "+socketAddress);
                }
                socketReader.stopListening();
                socketReader = null;
            }
        }
    }
    
    /**
     * Sends a script command to the VBS GIFT Plugin
     * 
     * @param command The command to execute in the plugin
     * @param errorMsg - used to append error content too, if an error occurs
     * @param retryOnFail - if a response is needed (see response parameter) this flag is used to try 
     * to send the command again if it fails to receive a response the first time
     * @param readResponse - if true, the socket will wait for a response to the message (e.g. acknowledgement)
     * and place the response in the string returned by this method.  If false the message will be sent and a response will not
     * be waited for.
     * @return String The response received for the command sent if the response should be read based on the value of {@link readResponse}.  
     * Null if the response isn't read or there was an issue reading the response.
     * @throws IOException if there was a problem sending the command
     * @throws ConfigurationException if the socket has not been initialized or is not connected
     * @throws DetailedException if there was a problem reading the response to the command that was sent
     */    
    public String sendCommand(String command, StringBuilder errorMsg, boolean retryOnFail, boolean readResponse) throws IOException, ConfigurationException, DetailedException {
            
        if(logger.isDebugEnabled()){
            logger.debug("Sending command of \n"+command);
        }
        
        if(socket == null) { 
            throw new ConfigurationException("The socket hasn't been connected yet.", "Please call connect() before using the socket.", null);
        } else if(!socket.isConnected()){
            throw new ConfigurationException("The socket has been instantiated at some point but is no longer connected.", "There is most likely a logic error in the usage of the socket class which caused the socket to disconnect but not be set to null.  Currently there is no reconnect logic to handle this state.", null);
        }
            
        
        PrintWriter socketOutput = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        socketOutput.println(command + '\0');
        if(logger.isDebugEnabled()){
            logger.debug("Command sent successfully of \n"+command);
        }

        if(readResponse){
            //wait for and capture the response to the sent command/message
            
            char responseArray[] = new char[512];
            
            try {
                
                socketInput.read(responseArray);
                String response = new String(responseArray).trim();
                return response;
                
            } catch (IOException e) {
                
                if( retryOnFail ) {
                    //try to send/read the command one more time
                    
                    if(logger.isDebugEnabled()){
                        logger.debug("Setting socket connection to null for connection to "+socketAddress+" because the following command could not be sent but retry on fail flag is set to tru:\n"+command);
                    }
                    
                    //reconnect first - this is an attempt at refreshing the connection to help
                    //                  resolve some socket issues that could be causing an issue here (hasn't been proven to help, yet)
                    disconnect();
                    connect();
                    
                    sendCommand(command, errorMsg, false, readResponse);
                }
                
                logger.error("IOException when waiting for a response from the VBS Plugin", e);               
                throw new DetailedException("Failed to send a command to the external application.", "The following command could not be sent to the address "+socketAddress+":\n"+command, e);
            }
        }
        
        return null;
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[ SocketHandler: ");
        sb.append("address = ").append(socketAddress);
        sb.append(", connected = ").append(socket != null && socket.isConnected());
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * This inner class is responsible for continuously reading from the socket.  This is useful for
     * receiving training application state updates.
     * 
     * @author mhoffman
     *
     */
    private class SocketReader extends Thread{
        
        private final Socket socketToRead;
        private final ApplicationStateListener socketReaderAppStateListener;
        
        /** listens for incoming byte data over the socket. */
        private final ApplicationByteListener socketReaderAppByteListener;
        
        /** 
         * The amount of incoming bytes read at once by the appByteListener. 
         * Set to 0 if the appByteListener is not used.
         */
        private final int byteReadSize;
        
        /** 
         * flag used to indicate whether the reader should continue waiting and reading training application
         * state on the socket 
         */
        private boolean keepListening = true;
        
        /**
         * Set attributes.
         * 
         * @param socket the socket to listen on
         * @param appStateListener used to notify of training application states read from the socket
         */
        public SocketReader(final Socket socket, final ApplicationStateListener appStateListener){
            super("SocketReader.State : "+socket.getInetAddress());
            
            if(appStateListener == null){
                throw new IllegalArgumentException("The application state listener can't be null.");
            }
            
            this.socketToRead = socket;
            this.socketReaderAppStateListener = appStateListener;
            this.socketReaderAppByteListener = null;
            this.byteReadSize = 0;
        }
        
        /**
         * Set attributes when listening for bytes.
         * 
         * @param socket the socket to listen on
         * @param appByteListener used to notify of bytes read from the socket
         * @param byteReadSize the amount of bytes for the appByteListener to read in at a time. Must be greater than 0.
         *                      Large numbers may increase memory usage.
         * @throws IllegalArgumentException if appByteListener is null or if byteReadSize is less than 0.
         */
        public SocketReader(final Socket socket, final ApplicationByteListener appByteListener, int byteReadSize) {
            super("SocketReader.Byte : "+socket.getInetAddress());
            
            if (appByteListener == null) {
                throw new IllegalArgumentException("The parameter 'appByteListener' cannot be null.");
            }
            else if (byteReadSize <= 0) {
                throw new IllegalArgumentException("The parameter 'byteReadSize' cannot be 0 or less.");
            }
            
            this.socketToRead = socket;
            this.socketReaderAppByteListener = appByteListener;
            this.socketReaderAppStateListener = null;
            this.byteReadSize = byteReadSize;
        }
        
        public void stopListening(){
            keepListening = false;
        }

        @Override
        public void run() {

            logger.info("Started socket reader on "+socketToRead.getInetAddress());
            
            try {
                InputStream readDataFromSocket = socketToRead.getInputStream();
                InputStreamReader reader = new InputStreamReader(readDataFromSocket);
                
                while(keepListening && socketToRead != null && socketToRead.isConnected()){
                    if (socketReaderAppStateListener != null) {
                        BufferedReader bufferedReader = new BufferedReader(reader);
                        
                        String textRead = bufferedReader.readLine();
    
                        if(textRead != null){
                            
                            if(logger.isDebugEnabled()){
                                logger.debug("read from socket: " + textRead);
                            }
                            
                            try{
                                socketReaderAppStateListener.handleApplicationState(textRead);
                            }catch(Throwable t){
                                logger.error("Caught error from misbehaving application state listener.  Continuing to read from the socket.  Mishandled state from external application:\n"+textRead, t);
                            }
                        }
                    } else if (socketReaderAppByteListener != null) {
                        
                        byte[] bytesRead = new byte[byteReadSize];
                        
                        readDataFromSocket.read(bytesRead);
                        
                        if(bytesRead != null){
                            
                            if(logger.isDebugEnabled()){
                                logger.debug("read from socket: " + bytesRead);
                            }
                            
                            try{
                                socketReaderAppByteListener.handleApplicationByteInput(bytesRead);
                            }catch(Throwable t){
                                logger.error("Caught error from misbehaving application byte listener.  Continuing to read from the socket.  Mishandled state from external application:\n"+bytesRead, t);
                            }
                        }
                    }
                }
                
                logger.info("Gracefully ending the socket reader.  keepListening = "+keepListening+", isConnected = "+(socketToRead != null && socketToRead.isConnected()));
                
            } catch (IOException e) {
                logger.error("The socket read had a severe error and will no longer read from "+socketToRead.getInetAddress()+".", e);
            }
        }
        
        
    }
}
