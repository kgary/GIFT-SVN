/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.shared;

import mil.arl.gift.common.course.dkf.session.KnowledgeSessionsReply;
import mil.arl.gift.common.gwt.client.DetailedRpcResponse;

/**
 * Contains the knowledge sessions known to a domain module.
 * 
 * @author sharrison
 */
public class GetActiveKnowledgeSessionsResponse extends DetailedRpcResponse {
    
    /** knowledge sessions known to a domain module */
    private KnowledgeSessionsReply knowledgeSessionsReply;

    /**
     * Required for GWT
     */
    @SuppressWarnings("unused")
    private GetActiveKnowledgeSessionsResponse() {
    }

    /**
     * An rpc that is used to get the existing session information of a user (if it exists).
     * 
     * @param success - true if the rpc is a success, false otherwise.
     * @param response - Error or success response.
     * @param knowledgeSessionsReply - knowledge sessions known to a domain module
     */
    public GetActiveKnowledgeSessionsResponse(boolean success, String response,
            KnowledgeSessionsReply knowledgeSessionsReply) {
        this.setIsSuccess(success);
        this.setResponse(response);
        this.knowledgeSessionsReply = knowledgeSessionsReply;
    }

    /**
     * Return information about knowledge sessions.
     * 
     * @return the knowledge sessions known to a domain module
     */
    public KnowledgeSessionsReply getKnowledgeSessions() {
        return knowledgeSessionsReply;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("[GetActiveKnowledgeSessionsResponse: ");
        sb.append("knowledge sessions = {");
        sb.append(knowledgeSessionsReply);
        sb.append("}");
        sb.append(", ").append(super.toString());
        sb.append("]");

        return sb.toString();
    }
}
