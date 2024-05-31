/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client;

import mil.arl.gift.common.course.dkf.session.KnowledgeSessionsReply;

/**
 * Class used to listen for knowledge session changes from the server.
 * 
 * @author nblomberg
 *
 */
public interface KnowledgeSessionListener  {

    /**
     * Handler for when a knowledge session list has been updated.
     * 
     * @param sessions The knowledge session information which contains a map where the key value is the host domain session id, and mapped
     * to the abstract knowledge session data.
     */
    void onKnowledgeSessionUpdated(KnowledgeSessionsReply knowledgeSessionsReply);
   
}
