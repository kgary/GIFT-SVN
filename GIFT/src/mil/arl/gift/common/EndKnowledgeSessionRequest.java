/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.io.Serializable;

/**
 * The message payload for {@link MessageTypeEnum#LESSON_COMPLETED} or
 * {@link MessageTypeEnum#CLOSE_DOMAIN_SESSION_REQUEST} type messages that are being redirected in
 * order to notify the game master panel that the knowledge session is ending.
 * 
 * @author sharrison
 */
public class EndKnowledgeSessionRequest implements Serializable {

    /**
     * A unique ID for identifying this specific version of the class during deserialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor
     */
    public EndKnowledgeSessionRequest() {
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[EndKnowledgeSessionRequest]");
        return sb.toString();
    }
}
