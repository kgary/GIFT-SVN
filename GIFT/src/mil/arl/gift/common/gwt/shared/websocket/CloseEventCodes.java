/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.shared.websocket;

/**
 * A utility class that identifies common status codes expected from Web Socket close events and provides helpful
 * methods for interpreting them.
 * <br/><br/>
 * The close status codes supported by this class are based off the list found at 
 * <a>https://developer.mozilla.org/en-US/docs/Web/API/CloseEvent</a>.
 * 
 * @author nroberts
 */
public class CloseEventCodes {
    
    /** 
     * Normal closure; the connection successfully completed whatever purpose for which it was created. 
     */
    public final static int NORMAL_CLOSURE = 1000;
    
    /**
     * The endpoint is going away, either because of a server failure or because the browser is navigating away from 
     * the page that opened the connection.
     */
    public final static int GOING_AWAY = 1001;
    
    /**
     * The endpoint is terminating the connection due to a protocol error.
     */
    public final static int PROTOCOL_ERROR = 1002;
    
    /**
     * The connection is being terminated because the endpoint received data of a type 
     * it cannot accept (for example, a text-only endpoint received binary data).
     */
    public final static int UNSUPPORTED_DATA = 1003;
    
    /**
     * Reserved.  Indicates that no status code was provided even though one was expected.
     */
    public final static int NO_STATUS_RECEIVED = 1005;
    
    /**
     * Reserved. Used to indicate that a connection was closed abnormally (that is, with 
     * no close frame being sent) when a status code is expected.
     */
    public final static int ABNORMAL_CLOSURE = 1006;
    
    /**
     * The endpoint is terminating the connection because a message was received that contained 
     * inconsistent data (e.g., non-UTF-8 data within a text message).
     */
    public final static int INVALID_FRAME_PAYLOAD_DATA = 1007;
    
    /**
     * The endpoint is terminating the connection because it received a message that violates its policy. 
     * This is a generic status code, used when codes 1003 and 1009 are not suitable.
     */
    public final static int POLICY_VIOLATION = 1008;
    
    /**
     * The endpoint is terminating the connection because a data frame was received that is too large.
     */
    public final static int MESSAGE_TOO_BIG = 1009;
    
    /**
     * The client is terminating the connection because it expected the server to 
     * negotiate one or more extension, but the server didn't.
     */
    public final static int MISSING_EXTENSION = 1010;
    
    /**
     * The server is terminating the connection because it encountered an unexpected 
     * condition that prevented it from fulfilling the request.
     */
    public final static int INTERNAL_ERROR = 1011;
    
    /** 
     * The server is terminating the connection because it is restarting.
     */
    public final static int SERVICE_RESTART = 1012;
    
    /**
     * The server is terminating the connection due to a temporary condition, e.g. it is 
     * overloaded and is casting off some of its clients.
     */
    public final static int TRY_AGAIN_LATER = 1013;
    
    /**
     *  The server was acting as a gateway or proxy and received an invalid response 
     *  from the upstream server. This is similar to 502 HTTP Status Code.
     */
    public final static int BAD_GATEWAY = 1014;
    
    /**
     * Reserved. Indicates that the connection was closed due to a failure to perform a 
     * TLS handshake (e.g., the server certificate can't be verified).
     */
    public final static int TLS_HANDSHAKE = 1015;
    
    /**
     * Gets whether the given close event code signifies an <i>intentional</i> closure of the web socket connection by the client 
     * or the server (e.g. client left page, server ended session, etc.). Any code that returns false for this method should be 
     * considered an erroneous closure (see {@link #isErroneousClosure(int)}.
     * 
     * @param closeCode the close event code to check
     * @return whether the given close event signifies a graceful closure of the connection
     */
    public static boolean isGracefulClosure(int closeCode) {
        return closeCode == NORMAL_CLOSURE || closeCode == GOING_AWAY;
    }
    
    /**
     * Gets whether the given close event code signifies an <i><u>un</u>intentional</i> closure of the web socket connection 
     * (e.g. client lost connection to server, server is overloaded, abnormal closure, etc.). Any code that returns false 
     * for this method should be considered a graceful closure (see {@link #isGracefulClosure(int)}.
     * 
     * @param closeCode the close event code to check
     * @return whether the given close event signifies an erroneous closure of the connection
     */
    public static boolean isErroneousClosure(int closeCode) {
        return closeCode != NORMAL_CLOSURE && closeCode != GOING_AWAY;
    }
    
    /**
     * Gets a description of the reason the web socket connection was closed according to the given close event code. 
     * 
     * @param code the close event code to get a description for
     * @return the description of the given close event code. Cannot be null.
     */
    public static String getClosureDescription(int closeCode) {
        
        if(closeCode == NORMAL_CLOSURE){
            return "Unable to communicate with the server. The connection was closed normally.";
            
        } else if(closeCode == GOING_AWAY){
            return "Unable to communicate with the server. The connection was closed because the client is leaving the connection.";
            
        } else if(closeCode == PROTOCOL_ERROR){
            return "Unable to communicate with the server. The connection was terminated due to a protocol error.";
            
        } else if(closeCode == UNSUPPORTED_DATA){
            return "Unable to communicate with the server. "
                    + "The connection was terminated because an unexpected data type was received.";
                
        } else if(closeCode == NO_STATUS_RECEIVED){
            return "Unable to communicate with the server. "
                    + "The connection was terminated but no status was given. Please check this error's details "
                    + "for more information about why the connection was terminated.";
            
        } else if(closeCode == ABNORMAL_CLOSURE){
            return "Unable to communicate with the server. "
                    + "The connection was terminated because it attempted to close abnormally.";
            
        } else if(closeCode == INVALID_FRAME_PAYLOAD_DATA){
            return "Unable to communicate with the server. "
                    + "The connection was terminated because it received a message that contained inconsistent data.";
            
        } else if(closeCode == POLICY_VIOLATION){
            return "Unable to communicate with the server. "
                    + "The connection was terminated because it received a message that violated its communication policy.";
            
        } else if(closeCode == MESSAGE_TOO_BIG){
            return "Unable to communicate with the server. "
                    + "The connection was terminated because it received a message that was too large.";
            
        } else if(closeCode == MISSING_EXTENSION){
            return "Unable to communicate with the server. "
                    + "The connection was terminated because the server failed to negotiate an extension for a message.";
            
        } else if(closeCode == INTERNAL_ERROR){
            return "Unable to communicate with the server. "
                    + "The server terminated the connection because it was unable to fulfill a request.";
            
        } else if(closeCode == SERVICE_RESTART){
            return "Unable to communicate with the server. "
                    + "The server terminated the connection because it is restarting.";
            
        } else if(closeCode == TRY_AGAIN_LATER){
            return "Unable to communicate with the server. "
                    + "The server terminated the connection because it is currently overloaded. Please try again later.";
            
        } else if(closeCode == BAD_GATEWAY){
            return "Unable to communicate with the server. "
                    + "A bad response was received due to a proxy error.";
            
        } else if(closeCode == TLS_HANDSHAKE){
            return "Unable to communicate with the server. "
                    + "The connection was closed because no handshake could be established between the client and the server. "
                    + "This is likely the result of a problem with the server's certificate.";
            
        } else {
            return "Unable to communicate with the server for an unexpected reason (Error code: " 
                    + closeCode + "). Please check your network connection.";
        }
    }
}
