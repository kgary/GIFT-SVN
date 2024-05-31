/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared.data;

import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.gwt.client.DetailedRpcResponse;

/**
 * A response containing a single abstract knowledge session
 * 
 * @author nblomberg
 *
 */
public class AbstractKnowledgeSessionResponse extends DetailedRpcResponse {

    /** The knowledge session.  Can be null if there was an error in the response */
    private AbstractKnowledgeSession session = null;

    /**
     * Required for GWT
     */
    @SuppressWarnings("unused")
    private AbstractKnowledgeSessionResponse() {
    }

    /**
     * An rpc that is used to return the host team session data
     * 
     * @param success - true if the rpc is a success, false otherwise.
     * @param response - Error or success response.
     * @param session - The found session data if successful, null otherwise.
     */
    public AbstractKnowledgeSessionResponse(boolean success, String response,
            AbstractKnowledgeSession session) {
        this.setIsSuccess(success);
        this.setResponse(response);
        this.session = session;
    }

    /**
     * @return the hosted session (if successful).  Null can be returned if the response is a failure.
     */
    public AbstractKnowledgeSession getKnowledgeSession() {
        return session;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("[AbstractKnowledgeSession: ");
        sb.append("session = {");
        sb.append(getKnowledgeSession());
        sb.append("}");
        sb.append("]");

        return sb.toString();
    }
}
