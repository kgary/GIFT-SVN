/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.messages;

import java.io.Serializable;

import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;

/**
 * A wrapper around an {@link AbstractKnowledgeSession} to be added to the
 * dashboard.
 * 
 * @author sharrison
 */
@SuppressWarnings("serial")
public class AddKnowledgeSession implements Serializable {

    /** The learner state that this knowledge session state wraps */
    private AbstractKnowledgeSession knowledgeSession;

    /**
     * Default no-arg constructor needed for GWT serialization
     */
    private AddKnowledgeSession() {
    }

    /**
     * Constructor
     * 
     * @param knowledgeSession the knowledge session to wrap. Can't be null.
     */
    public AddKnowledgeSession(AbstractKnowledgeSession knowledgeSession) {
        this();
        if (knowledgeSession == null) {
            throw new IllegalArgumentException("The parameter 'knowledgeSession' cannot be null.");
        }

        this.knowledgeSession = knowledgeSession;
    }

    /**
     * Returns the knowledge session to add.
     * 
     * @return the knowledge session. Can't be null.
     */
    public AbstractKnowledgeSession getKnowledgeSession() {
        return knowledgeSession;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[AddKnowledgeSession: ");
        sb.append("knowledge session = ").append(knowledgeSession);
        sb.append("]");

        return sb.toString();
    }
}
