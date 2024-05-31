/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge;

import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;

/**
 * Used for notifications of knowledge session events.
 * 
 * @author mhoffman
 *
 */
public interface KnowledgeSessionEventListener {

    /**
     * Sends a message to the monitor topic notifying that a new knowledge
     * session was created.
     * 
     * @param knowledgeSession the session that was created.
     */
    public void sendKnowledgeSessionCreatedMessage(AbstractKnowledgeSession knowledgeSession);
    
    /**
     * Sends a notification to the tutor that the knowledge sessions have been updated.
     * This sends the full list of knowledge sessions to the tutor (unfiltered) so that
     * the tutor can update any clients that may be listening.  This triggers a
     * push notification to connected tutor clients.
     *
     * @param domainSessionId The domain session sending the notification.
     */
    public void notifyTutorKnowledgeSessionsUpdated(int domainSessionId);
}
