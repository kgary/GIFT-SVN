/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.dashboard.server.webmonitor;

import java.util.HashSet;
import java.util.Set;

import mil.arl.gift.tools.dashboard.server.DashboardBrowserWebSession;
import mil.arl.gift.tools.dashboard.shared.messages.DashboardMessage;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.AbstractMessageUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageWatchedDomainSessionUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.WebMonitorUpdate;

/**
 * A service that can be used by a browser session to perform web monitor oprations and receive
 * monitoring updates from GIFT's modules.
 * 
 * @author nroberts
 */
public class WebMonitorService{
    
    /** The browser session used to send updates from this service to the client */
    private DashboardBrowserWebSession browserSession;
    
    /** The domain sessions that the browser session is watching */
    private Set<Integer> watchedDomainSessions = new HashSet<>();
    
    /**
     * Creates a new web monitor service that uses the given browser session to
     * send updates to the client
     * 
     * @param browserSession the browser session with which to send updates to the client. Cannot be null.
     */
    public WebMonitorService(DashboardBrowserWebSession browserSession) {
        
        if(browserSession == null) {
            throw new IllegalArgumentException("The browser session associated with a web monitor service cannot be null");
        }
        
        this.browserSession = browserSession;
    }
    
    /**
     * Sends an update from the web monitor to the client
     * 
     * @param update the update to send. Cannot be null.
     */
    public void sendWebMonitorUpdate(WebMonitorUpdate update) {
        
        if(browserSession == null) {
            return;
        }
        
        if(update instanceof AbstractMessageUpdate) {
            
            AbstractMessageUpdate messageUpdate = (AbstractMessageUpdate) update;
            
            if(messageUpdate.getDomainSessionId() != null
                    && !watchedDomainSessions.contains(messageUpdate.getDomainSessionId())) {
                
                /* This browser session is not listening for updates to the given update's domain session, 
                 * so don't send the update */
                return;
            }
        }
        
        final DashboardMessage msg = new DashboardMessage(update, -1, System.currentTimeMillis());
        browserSession.getWebSocket().send(msg);
    }
    
    /**
     * Begins watching the domain session with the given ID to receive messages from it
     * 
     * @param domainSessionId the ID of the domain session to watch.
     */
    public void watchDomainSession(int domainSessionId) {
        
        synchronized(watchedDomainSessions) {
            
            boolean added = watchedDomainSessions.add(domainSessionId);
            
            if(added) {
                sendWatchedDomainSessions();
            }
        }
    }
    
    /**
     * Stops watching the domain session with the given ID to stop receiving messages from it
     * 
     * @param domainSessionId the ID of the domain session to stop watching.
     */
    public void unwatchDomainSession(int domainSessionId) {
        synchronized(watchedDomainSessions) {
            
            boolean removed = watchedDomainSessions.remove(domainSessionId);
            
            if(removed) {
                sendWatchedDomainSessions();
            }
        }
    }
    
    /**
     * Gets whether this browser session is watching the domain session with the given ID
     * 
     * @param domainSessionId the ID of the domain session
     * @return whether the browser session is watching that domain session
     */
    public boolean isWatching(int domainSessionId) {
        synchronized(watchedDomainSessions) {
            return watchedDomainSessions.contains(domainSessionId);
        }
    }
    
    /**
     * Gets a copy of the set of domain sessions watched by the browser session
     * 
     * @return the watched domain sessions. Will not be null but can be empty.
     */
    public Set<Integer> getWatchedDomainSessions(){
        synchronized(watchedDomainSessions) {
            return new HashSet<>(watchedDomainSessions);
        }
    }
    
    /**
     * Sends an update to the browser session's client telling it what
     * domain sessions are currently being watched
     */
    public void sendWatchedDomainSessions() {
        sendWebMonitorUpdate(new MessageWatchedDomainSessionUpdate(watchedDomainSessions));
    }
}
