/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared.websocket;

/**
 * A utility class used to maintain common resources that are shared by the Tutor module's client- and server-side web sockets
 * 
 * @author nroberts
 */
public class TutorWebSocketUtils {

    /** 
     * The amount of time that the Tutor server should wait for clients in the GIFT mobile app 
     * to reconnect before closing their socket connections entirely. 
     * <br/><br/>
     * This value should normally be larger than {@link #DEFAULT_SOCKET_TIMEOUT_MS} because 
     * mobile devices are more prone to sudden network interruptions and may experience said 
     * interruptions for longer periods of time depending on network availability. Larger values 
     * can be used to give clients using the GIFT mobile app more time to reconnect and resume 
     * their sessions, while smaller values can be used to reduce the amount of time that said
     * sessions linger on the server after such a disconnect, freeing up memory sooner.
     */
    public static final int MOBILE_SOCKET_TIMEOUT_MS = 3600000; // 1 hour
    
    /**
     * The amount of time that the Tutor server should wait for regular clients to reconnect 
     * before closing their socket connections entirely.
     * <br/><br/>
     * This value should normally be smaller than {@link #MOBILE_SOCKET_TIMEOUT_MS} because 
     * non-mobile devices typically have more consistent network connections where interruptions 
     * are short and infrequent. Larger values can be used to give regular clients more time to 
     * reconnect and resume their sessions, while smaller values can be used to reduce the amount 
     * of time that said sessions linger on the server after such a disconnect, freeing up 
     * memory sooner.
     */
    public static final int DEFAULT_SOCKET_TIMEOUT_MS = 300000; // 5 minutes
    
    /** The name of the URL parameter in a web socket request that provides the ID to give the web socket */
    public static final String SOCKET_ID_PARAM = "socketId";

}
