/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

import mil.arl.gift.common.MobileAppProperties;

/**
 * A set of properties specific to a client that is shared with the server. This class is used to allow the server to detect 
 * specific client attributes that are not tied to user or browser sessions, such as pre-login information that might be needed 
 * to set up those sessions in the first place.
 * 
 * @author nroberts
 */
public class ClientProperties implements IsSerializable{

    /** The properties that are provided by the GIFT mobile app, if this client is embedded inside it */
    private MobileAppProperties mobileAppProperties;
    
    /** The unique ID assigned to this client's web socket */
    private String webSocketId;

    /**
     * No-arg constructor required for GWT RPC serialization
     */
    private ClientProperties() {}
    
    /**
     * Creates a new set of properties for a client with the given web socket ID and mobile app status
     * 
     * @param webSocketId the unique ID assigned to this client's web socket
     * @param mobileAppProperties the properties that are provided by the GIFT mobile app, if this client is embedded inside it.
     * A value of null indicates that this client is not embedded in the mobile app.
     */
    public ClientProperties(String webSocketId, MobileAppProperties mobileAppProperties) {
        this();
        
        this.webSocketId = webSocketId;
        this.mobileAppProperties = mobileAppProperties;
    }

    /**
     * Gets the properties that are provided by the GIFT mobile app, if this client is embedded inside it.
     * A value of null indicates that this client is not embedded in the mobile app.
     * 
     * @return the mobile app properties, or null, if this client is not embedded in the mobile app
     */
    public MobileAppProperties getMobileAppProperties() {
        return mobileAppProperties;
    }

    /**
     * Gets the unique ID assigned to this client's web socket
     * 
     * @return the web socket ID
     */
    public String getWebSocketId() {
        return webSocketId;
    }
    
    @Override
    public String toString() {
        return new StringBuilder()
                .append("[TutorClient webSocketId='")
                .append(webSocketId)
                .append("', mobileAppProperties='")
                .append(mobileAppProperties)
                .append("']")
                .toString();
    }
}
