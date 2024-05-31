/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.interop;

import java.io.IOException;

import mil.arl.gift.net.socket.ApplicationStateListener;
import mil.arl.gift.net.socket.SocketHandler;
import mil.arl.gift.net.xmlrpc.XMLRPCClient;
import mil.arl.gift.net.xmlrpc.XMLRPCServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to create connections of various types that can be used to help facilitate
 * communication between a GIFT Gateway module interop plugin class (e.g. VBSPluginInterface.java) and
 * an external training application (e.g. VBS).  
 * 
 * In the future there could be more abstraction in this factory however as of now there is no common approach
 * to communicating across all interops that have been created thus far.  For example the VBSPluginInterface uses
 * sockets to communicate with the protocol of send from GIFT then listen for a response to that message.  That protocol
 * doesn't continuously listen for events from the training application VBS.  On the other hand the SimpleExampleTAPluginInterface
 * uses XMLRPC with both a client and a server instance where the server is always listening and the client is used based on
 * GIFT requests at any point during a GIFT course.  Then there is the DISInterface which uses a third party library that
 * uses sockets however in this case that interop only listens for DIS traffic from outside of GIFT as well as sends DIS
 * traffic based on GIFT requests, however neither require a response being sent or received to those messages, respectively.
 * 
 * Also in the future this factory could also leverage the interop's configuration parameters/object to instantiate the appropriate
 * communication protocol.  
 * 
 * @author mhoffman
 *
 */
public class ApplicationConnectionFactory {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationConnectionFactory.class);
    
    /**
     * Create a socket handler that will not listen for messages at all times but can
     * send messages whenever needed.
     * 
     * Make sure to call connect() before attempting to send messages.
     * 
     * @param address the address of the socket (e.g. "http://localhost", "10.1.21.123")   
     * @param port the port to connect to and listen on (e.g. 12345)
     * @return the created socket  
     */
    public static SocketHandler createSocket(String address, int port){
        
        if(logger.isInfoEnabled()){
            logger.info("Creating socket handler on "+address+":"+port);
        }
        return new SocketHandler(address, port);
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
     * @return the created socket
     */
    public static SocketHandler createSocket(String address, int port, ApplicationStateListener appStateListener){
        if(logger.isInfoEnabled()){
            logger.info("Creating socket handler on "+address+":"+port);
        }
        return new SocketHandler(address, port, appStateListener);
    }
    
    /**
     * Creates a new XML RPC Client which should be used when the intent is to launch an XML RPC server 
     * on the address provided and then create a client connection to communicate with that server.
     * 
     * @param address the address of the server (e.g. "http://localhost", "10.1.21.123")   
     * @param port port the server should should listen on (e.g. 12345)  
     * @return the XML RPC client
     * @throws IOException if there was a problem initializing the client
     */
    public static XMLRPCClient createXMLRPCClient(String address, int port) throws IOException{
        if(logger.isInfoEnabled()){
            logger.info("Creating XML RPC client on "+address+":"+port);
        }
        return new XMLRPCClient(address, port);
    }
    
    /**
     * Creates a new XML RPC Server on the specified port.  An XML RPC client, either in GIFT or
     * in an another application can connect to this server and call the methods in the handlers
     * provided. 
     * 
     * Note: call start method to start the server.
     * 
     * @param listenPort the port to listen for RPC requests on
     * @param handlers the list of classes that contain methods which can be called by an RPC client.
     *         Note: the class name is used as the suffix to the method.
     *               Example: the class mil.arl.gift.net.simple.XMLRPCServer$Example has method test, therefore the client
     *               RPC call would be "mil.arl.gift.net.simple.XMLRPCServer$Example.test".
     * @return the XML RPC server
     * @throws Exception if there was a problem initializing the server
     */
    public static XMLRPCServer createXMLRPCServer(int listenPort, Class<?>... handlers) throws Exception{
        if(logger.isInfoEnabled()){
            logger.info("Creating XML RPC server on port "+listenPort+" with the following handlers: "+handlers);
        }
        return new XMLRPCServer(listenPort, handlers);
    }
}
