/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;

/**
 * This class contains a newly created knowledge session.
 *
 * @author sharrison
 */
public class KnowledgeSessionCreated {

    /** The knowledge session that was created */
    private AbstractKnowledgeSession knowledgeSession;

    /**
     * Constructor.
     *
     * @param knowledgeSession the knowledge session that was created. Can't be
     *        null.
     */
    public KnowledgeSessionCreated(AbstractKnowledgeSession knowledgeSession) {
        if (knowledgeSession == null) {
            throw new IllegalArgumentException("The parameter 'knowledgeSession' cannot be null.");
        }

        this.knowledgeSession = knowledgeSession;
    }

    /**
     * Return the knowledge session that was created.
     * 
     * @return the knowledge session. Can't be null.
     */
    public AbstractKnowledgeSession getKnowledgeSession() {
        return knowledgeSession;
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[KnowledgeSessionCreated: ");
        sb.append(", knowledge session = ").append(getKnowledgeSession());
        sb.append("]");

        return sb.toString();
    }
}
