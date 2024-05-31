/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.xmlrpc;

import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import mil.arl.gift.common.io.Constants;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a generic XML RPC client that can be used by GIFT to call RPC server methods.
 * The client is started using the network information provided.
 * 
 * @author mhoffman
 *
 */
public class XMLRPCClient {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(XMLRPCClient.class);
    
    /**
     * The xml rpc client connection.
     */
    private XmlRpcClient client;
    
    /**
     * flag used to indicate whether the server refused this client connection
     * the last time the client tried to contact the server
     */
    private boolean connectionRefused = false;
    
    /**
     * Constructor used when intent is to launch XML RPC server on the local host and then create a client connection.
     * 
     * @param address the address of the server (e.g. "http://localhost", "10.1.21.123")   
     * @param port port the server should should listen on (e.g. 12345)  
     * @throws IOException if there was a problem initializing the client
     */
    public XMLRPCClient(String address, int port) throws IOException {
        init(address, port); 
    }  
    
    /**
     * Initialize the client.
     * 
     * @param address the address of the server (e.g. "http://localhost", "10.1.21.123")   
     * @param port port the server should should listen on (e.g. 12345)  
     * @throws MalformedURLException if the address and port form an invalid URL
     */
    protected void init(String address, int port) throws MalformedURLException{
        
        // For the python server we always want to use HTTP since this is an internal connection.  Do not
        // use dynamic getTransferProtocol() function to construct the url here.
        String urlString = Constants.HTTP + address + ":" + port;        
        URL url = new URL(urlString);

        XmlRpcClientConfigImpl config  = new XmlRpcClientConfigImpl();
        config.setServerURL(url);
        
        client = new XmlRpcClient();
        client.setConfig(config); 
        
        if(logger.isInfoEnabled()){
            logger.info("XML-RPC client initialized for server URL of "+urlString+".");
        }
    }

    /**
     * Call the XML-RPC server method with the given parameters.
     * 
     * @param methodName the name of the remote method to call 
     *              Note: if using the GIFT XML RPC Server (mil.arl.gift.net.simple.XMLRPCServer), the class name should appear as
     *                    the prefix to the method name (e.g. mil.arl.gift.net.simple.XMLRPCServer$Example.test).
     * @param params contains the parameters to the method in the exact order of the parameters listed in the method signature.
     *              Can be null.
     * @return Object the return value of the remote method.
     *              Note: the current XML-RPC library requires all remote methods to return a non-null value.
     * @throws XmlRpcException if there was a problem requesting the method
     */
    public Object callMethod(String methodName, Vector<?> params) throws XmlRpcException{
        
        if(params == null){
            params = new Vector<>(0);
        }
        
        return client.execute(methodName, params);
    }
    
    /**
     * Call a remote method with the given parameter.
     * 
     * @param methodName the name of the method to call on the RPC server
     * @param param the single parameter to pass to the rpcMethod.  Can be null.
     * @param errorMsg - used to append error content too, if an error occurs
     * @return Object the return value of the remote method.  Can be null.
     */    
    public Object callMethod(String methodName, String param, StringBuilder errorMsg) {
        
        Vector<String> params = new Vector<>(1);
        if(params != null){
            params.addElement(param);
        }
        
        return callMethod(methodName, params, errorMsg);
    }

    /**
     * Call a remote method with the given parameters.
     * 
     * @param rpcMethodName the name of the method to call on the RPC server
     * @param params the parameters to pass to the rpcMethod.  Can be null.
     * @param errorMsg - used to append error content too, if an error occurs.  Can be null if errors in this call aren't important.
     * @return Object the return value of the remote method.  Can be null.
     */    
    public Object callMethod(String rpcMethodName, Vector<?> params, StringBuilder errorMsg) {
        
        try {
            Object returnObj = callMethod(rpcMethodName, params);
            connectionRefused = false;
            return returnObj;
        } catch (XmlRpcException e) {
            if(!connectionRefused){
                //don't keep logging the same issue about a bad connection
                logger.error("There was an exception thrown when calling the RPC method of '"+rpcMethodName+"'.\n"+
                "If this exception is a ConnectionException, this XML RPC Client will not log another error about it until a succesful connection is made first.", e);
            }
            
            if(errorMsg != null){
                errorMsg.append("Failed to call RPC method named '").append(rpcMethodName).append("'.");
            }
            
            if(e.getCause() != null && e.getCause() instanceof ConnectException){
                connectionRefused = true;
                if(errorMsg != null){
                    errorMsg.append("  Disabling this XML RPC client because the failure is due to a connection exception.  The most likely cause is the XML RPC server was never started.");
                }
            }
        }

        return null;
    }

    /**
     * This method is used to test the execution of the XML RPC client with the XML RPC Server (mil.arl.gift.net.xmlrpc.XMLRPCServer).
     * Note: the server should be launched before this client makes calls to it.
     * 
     * @param args - not supported/used
     */
    public static void main(String[] args) {
        
        try{
        
            //Note: having 'http://" in address arg caused "java.net.UnknownHostException: http" when calling 'callMethod' below
            XMLRPCClient client = new XMLRPCClient("localhost", 10564);
            
            Vector<String> params = new Vector<>();
            params.addElement("hello world!");
            
            String response = (String)client.callMethod("mil.arl.gift.net.xmlrpc.XMLRPCServer$Example.test", params);
            System.out.println(response);
            
        }catch(Exception e){
            e.printStackTrace();
        }

    }

}
