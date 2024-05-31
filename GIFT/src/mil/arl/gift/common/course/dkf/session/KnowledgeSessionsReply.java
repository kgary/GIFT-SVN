/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.dkf.session;

import java.io.Serializable;
import java.util.Map;

/**
 * Contains knowledge session information based on possible filters in the knowledge session request.
 * 
 * @author mhoffman
 *
 */
public class KnowledgeSessionsReply implements Serializable {

    /**
     * default
     */
    private static final long serialVersionUID = 1L;

    /** whether the domain session can host a knowledge session */
    private boolean canHost = false;
    
    /**  
     * collection of knowledge sessions from a domain module.
     * This list can be a subset of the entire knowledge sessions known to the domain module if
     * the corresponding request message had filter options enabled.  
     */
    private Map<Integer, AbstractKnowledgeSession> knowledgeSessionMap;
    
    /**
     * Required for GWT serialization - don't use
     */
    @SuppressWarnings("unused")
    private KnowledgeSessionsReply(){
        
    }
    
    /**
     * Set attribute(s)
     * @param knowledgeSessionMap a collection of knowledge sessions from a domain module.
     * This list can be a subset of the entire knowledge sessions known to the domain module if
     * the corresponding request message had filter options enabled.  Can't be null but can be empty.
     */
    public KnowledgeSessionsReply(Map<Integer, AbstractKnowledgeSession> knowledgeSessionMap){
        setKnowledgeSessionMap(knowledgeSessionMap);
    }

    /**
     * Return whether the domain session can host a knowledge session
     * 
     * @return default is false
     */
    public boolean canHost() {
        return canHost;
    }

    /**
     * Set whether the domain session can host a knowledge session
     * 
     * @param canHost true if the domain session can host a knowledge session
     */
    public void setCanHost(boolean canHost) {
        this.canHost = canHost;
    }

    /**
     * Return the  collection of knowledge sessions from a domain module.
     * This list can be a subset of the entire knowledge sessions known to the domain module if
     * the corresponding request message had filter options enabled. 
     * @return won't be null but can be empty.
     */
    public Map<Integer, AbstractKnowledgeSession> getKnowledgeSessionMap() {
        return knowledgeSessionMap;
    }

    /**
     * Set a collection of knowledge sessions from a domain module.
     * This list can be a subset of the entire knowledge sessions known to the domain module if
     * the corresponding request message had filter options enabled.  
     * @param knowledgeSessionMap Can't be null but can be empty.
     */
    private void setKnowledgeSessionMap(Map<Integer, AbstractKnowledgeSession> knowledgeSessionMap) {
        
        if(knowledgeSessionMap == null){
            throw new IllegalArgumentException("The map can't be null");
        }
        this.knowledgeSessionMap = knowledgeSessionMap;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[KnowledgeSessionsReply: canHost = ");
        builder.append(canHost);
        builder.append(", knowledgeSessionMap = ");
        builder.append(knowledgeSessionMap);
        builder.append("]");
        return builder.toString();
    }
    
    
}
