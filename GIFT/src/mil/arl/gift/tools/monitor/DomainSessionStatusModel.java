/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.arl.gift.common.DomainSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores the state of domain sessions in the system and maintains a timeout 
 * for each for determining when a domain session activates/deactivates.
 *
 * @author jleonard
 */
public class DomainSessionStatusModel {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(DomainSessionStatusModel.class);

    /** current active domain sessions */
    private final Map<Integer,DomainSession> globalDomainSessions = new HashMap<Integer,DomainSession>();

    /** listeners of domain session status events */
    private final List<DomainSessionStatusListener> listeners = new ArrayList<DomainSessionStatusListener>();

    /**
     * Return whether the domain session is currently known about and is active.
     * 
     * @param domainSessionId a unique domain session id to check if active
     * @return boolean 
     */
    public synchronized boolean containsDomainSession(final int domainSessionId) {

        return globalDomainSessions.containsKey(domainSessionId);
    }
    
    /**
     * Add an active domain session.
     * 
     * @param domainSession info about a domain session to add
     */
    public synchronized void addDomainSession(DomainSession domainSession){
        
        if(containsDomainSession(domainSession.getDomainSessionId())){
            //already know about this domain session
            return;
        }
        
        globalDomainSessions.put(domainSession.getDomainSessionId(), domainSession);
        
        logger.info("Added domain session of "+domainSession);

        for(DomainSessionStatusListener listener : listeners) {
            
            try{
                listener.domainSessionActive(domainSession);
            }catch(Exception e){
                logger.error("Caught exception from misbehaving listener "+listener, e);
            }
        }
    }
    
    /**
     * Add a list of active domain sessions.
     * 
     * @param domainSessions collection of domain sessions to add
     */
    public synchronized void addDomainSessions(final List<DomainSession> domainSessions){
        
        if(domainSessions == null){
            return;
        }
        
        for(DomainSession ds : domainSessions){
            addDomainSession(ds);
        }
    }
    
    /**
     * Remove a list of inactive domain sessions.
     * 
     * @param domainSessionIds collection of domain session ids to remove
     */
    public void removeDomainSessions(List<Integer> domainSessionIds){
        
        if(domainSessionIds == null){
            return;
        }
        
        for(Integer dsId : domainSessionIds){
            removeDomainSession(dsId);
        }
    }

    /**
     * Domain session is no longer active, remove it from the list of active domain sessions and
     * stop any timers created for that session.
     * 
     * @param domainSessionId domain session id to remove
     */
    public synchronized void removeDomainSession(final int domainSessionId) {

        if(containsDomainSession(domainSessionId)) {
            
            DomainSession ds = globalDomainSessions.get(domainSessionId);  
            globalDomainSessions.remove(domainSessionId);
            
            logger.info("Removing domain session of "+ds);
            
            for(DomainSessionStatusListener listener : listeners) {
                
                try{
                    listener.domainSessionInactive(ds);
                }catch(Exception e){
                    logger.error("Caught exception from misbehaving listener "+listener, e);
                }
            }
        }
    }

    /**
     * Add a listener of domain session status events
     * 
     * @param listener to add that is interested in domain session status events
     */
    public synchronized void addListener(final DomainSessionStatusListener listener) {
        if(listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a listener of domain session status events
     * 
     * @param listener to remove that is no longer interested in domain session status events
     */
    public synchronized void removeListener(final DomainSessionStatusListener listener) {
        if(listeners != null && listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }    
}
