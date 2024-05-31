/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared;

import mil.arl.gift.common.course.dkf.session.KnowledgeSessionsReply;

/**
 * A websocket message containing the latest state of knowledge sessions from the domain module
 * 
 * @author nblomberg
 *
 */
public class KnowledgeSessionsUpdated extends AbstractAction {

    private KnowledgeSessionsReply knowledgeSessionsReply;
    
    /**
     * Constructor - default for GWT serialization
     */
    public KnowledgeSessionsUpdated() {
        super(ActionTypeEnum.KNOWLEDGE_SESSIONS_UPDATED);
    }
    
    /**
     * Constructor
     * 
     * @param knowledgeSessions the knowledge sessions known to a domain module.  This should not be null.
     */
    public KnowledgeSessionsUpdated(KnowledgeSessionsReply knowledgeSessionsReply){
        super(ActionTypeEnum.KNOWLEDGE_SESSIONS_UPDATED);
        
        if(knowledgeSessionsReply == null){
            throw new IllegalArgumentException("The knowledge sessions can't be null");
        }
        this.knowledgeSessionsReply = knowledgeSessionsReply;
    }
    
    /**
     * Get the knowledge sessions. 
     * 
     * @return contains the knowledge sessions known to the domain module.  Won't be null.
     */
    public KnowledgeSessionsReply getKnowledgeSessions() {
        return knowledgeSessionsReply;
    }
   

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[KnowledgeSessionsUpdated: ");
        builder.append(super.toString());
        builder.append(", knowledgeSessions = ").append(knowledgeSessionsReply);
        builder.append("]");
        return builder.toString();
    }
    
    
}
