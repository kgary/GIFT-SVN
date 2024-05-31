/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.dashboard.shared.rpcs;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A set of information describing the current status of a browser session's web monitor service.
 * This should generally contain static information about the service that isn't expected to change
 * much over the service's lifetime, as frequent updates should be handled using websocket messages.
 * 
 * @author nroberts
 */
public class WebMonitorStatus implements IsSerializable{

    /** The URL of the broker that the web monitor service is listening to */
    private String brokerUrl;
    
    /** A default no-argument constructor required for GWT serialization */
    private WebMonitorStatus() {}

    /**
     * Creates a new set of web monitor status information
     * 
     * @param brokerUrl the URL of the broker that the web monitor service is listening to. Cannot be null.
     */
    public WebMonitorStatus(String brokerUrl) {
        this();
        
        if(brokerUrl == null) {
            throw new IllegalArgumentException("The broker URL that the web monitor is listening on cannot be null");
        }
        
        this.brokerUrl = brokerUrl;
    }

    /**
     * Gets the URL of the broker that the web monitor service is listening to
     * 
     * @return the broker URL. Cannot be null.
     */
    public String getBrokerUrl() {
        return brokerUrl;
    }
    
}
