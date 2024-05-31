/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.xmlrpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfig;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a generic XML RPC server that can be used by GIFT to allow RPC client requests.
 * The server is started using 'localhost' network information and the provided port.
 *        
 * Note: all RPC methods must return a non-null, non-void value (also can't use java.lang.Object).  If it doesn't you
 *       may received the following error on the RPC client side:
 *       
 *       "Fault: <Fault 0: 'java.lang.IllegalArgumentException: void return types for handler methods not supported'>"
 * 
 * @author mhoffman
 *
 */
public class XMLRPCServer {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(XMLRPCServer.class);
    
    /**
     * The current XML-RPC library requires every method to return a non-null object (can't use java.lang.Object).
     * This object is used as the default object to return if the XML-RPC method doesn't need
     * to return something normally.  
     */
    public static final Integer EMPTY_RETURN_OBJECT = Integer.valueOf(0);
    
    /**
     * The main interface to the XML-RPC server
     */
    private WebServer webServer;
    
    /** flag used to indicate whether the server is currently running */
    private boolean running = false;
    
    /**
     * Class constructor - initialize the server.
     * Note: call start method to start the server.
     * 
     * @param listenPort the port to listen for RPC requests on
     * @param handlers the list of classes that contain methods which can be called by an RPC client.
     *         Note: the class name is used as the suffix to the method.
     *               Example: the class mil.arl.gift.net.simple.XMLRPCServer$Example has method test, therefore the client
     *               RPC call would be "mil.arl.gift.net.simple.XMLRPCServer$Example.test".
     * @throws Exception if there was a problem initializing the server
     */
    public XMLRPCServer(int listenPort, Class<?>... handlers) throws Exception{
        
        init(listenPort, handlers);
    }
    
    /**
     * Initialize the server with the specified parameters.
     * 
     * @param listenPort the port to listen for RPC requests on
     * @param handlers the list of classes that contain methods which can be called by an RPC client.
     *         Note: the class name is used as the suffix to the method.
     *               Example: the class mil.arl.gift.net.simple.XMLRPCServer$Example has method test, therefore the client
     *               RPC call would be "mil.arl.gift.net.simple.XMLRPCServer$Example.test".
     * @throws UnknownHostException if there is a problem getting the network address information using "localhost"
     * @throws InstantiationException if there is a problem with testing the instantiation of one of the handler classes
     * @throws IllegalAccessException if there is a problem with testing the instantiation of one of the handler classes
     * @throws XmlRpcException if there is a problem adding of the handlers
     * @throws ReflectiveOperationException if there is a problem with testing the instantiation of one of the handler classes
     */
    protected void init(int listenPort, Class<?>[] handlers) 
            throws UnknownHostException, InstantiationException, IllegalAccessException, XmlRpcException, ReflectiveOperationException{
        
        if(handlers == null || handlers.length == 0){
            throw new IllegalArgumentException("The list of handlers must contain at least one value.");
        }
        
        webServer = new WebServer(listenPort);

        XmlRpcServerConfig config = new XmlRpcServerConfigImpl();

        XmlRpcServer server = webServer.getXmlRpcServer();
        server.setConfig(config);
        
        PropertyHandlerMapping pMapping = new PropertyHandlerMapping();
        for(Class<?> clazz : handlers){
            
            //test the class
            try{
                clazz.getDeclaredConstructor().newInstance();
            }catch(InstantiationException ie){
                logger.error("Caught exception while testing the XML-RPC Server handler class of "+clazz.getName()+".\n" +
                		"Are you sure the class contains an empty argument constructor OR no constructor at all which is required by the XML-RPC server library?", ie);
                throw ie;
            }catch(ReflectiveOperationException roe) {
                logger.error("Caught exception while testing the XML-RPC Server handler class of "+clazz.getName()+".\n" +
                        "Are you sure the class contains an empty argument constructor OR no constructor at all which is required by the XML-RPC server library?", roe);
                throw roe;
            }
            
            logger.info("adding handler of "+clazz.getName()+".");
            pMapping.addHandler(clazz.getName(), clazz);
        }
        
        server.setHandlerMapping(pMapping);
    }
    
    /**
     * Return whether or not the server is currently running (i.e. has been started successfully).
     * 
     * @return boolean
     */
    public boolean isRunning(){
        return running;
    }
    
    /**
     * Start the XML-RPC server.
     * 
     * @throws IOException if there was a severe problem starting the XML-RPC server
     */
    public void start() throws IOException{   
        webServer.start(); 
        running = true;
    }
    
    /**
     * Stop the XML-RPC server.
     */
    public void stop(){
        
        try{
            webServer.shutdown();
        }catch(Exception e){
            logger.error("Caught exception while trying to stop the XML-RPC Server.", e);
            throw e;
        }finally{
            running = false;
        }        
        
    }

    /**
     * This method is used to test the execution of the XML RPC Server.
     * Note: the server should be launched before the client makes call to it.
     * 
     * @param args - not supported/used
     */
    public static void main(String[] args) {
        
        try {            

            System.out.println("Attempting to start XML-RPC Server...");
            
            XMLRPCServer server = new XMLRPCServer(10564, Example.class);
            
            System.out.println("Starting...");
            server.start();
            
            try {
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
                String input = null;
                do {
                    System.out.print("Press Enter to Exit\n");
                    input = inputReader.readLine();
    
                } while (input != null && input.length() != 0);
            } catch (Exception e) {
                System.err.println("Caught exception while reading input: \n");
                e.printStackTrace();
            }
            
            server.stop();
            
        } catch (Exception exception) {
            System.err.println(exception);
        }

        System.out.println("Good-bye");
    }
    
    /**
     * Used in the main method to test this class.
     * 
     * @author mhoffman
     *
     */
    public static class Example{
        
        /**
         * This is an example RPC method.
         * 
         * @param content some input data to show
         * @return String some response text to return to the caller/client
         */
        public String test(String content){
            System.out.println(content);
            return "nice to meet you";
        }
    }

}
