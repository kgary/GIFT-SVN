/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.server;


import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.KnowledgeSessionsReply;
import mil.arl.gift.common.course.dkf.session.KnowledgeSessionsUtil;
import mil.arl.gift.common.gwt.server.AsyncReturnBlocker;
import mil.arl.gift.common.gwt.shared.GetActiveKnowledgeSessionsResponse;
import mil.arl.gift.net.api.MessageCollectionCallback;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.NACK;


/**
 * Callback to handle a KnowledgeSessionReply payload.  The response returns the full set
 * of knowledge sessions that is returned in the payload.
 * 
 * @author nblomberg
 *
 */
public class ActiveKnowledgeSessionsCallback implements MessageCollectionCallback {

    /** Instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(ActiveKnowledgeSessionsCallback.class);
    
    // The async return blocker that contains the response.
    AsyncReturnBlocker<GetActiveKnowledgeSessionsResponse> returnBlocker;
    
    /** Optional parameter to allow filtering the list of knowledge sessions by the course id. */
    private String filteredCourseSourceId = "";
    
    /**
     * Constructor
     * 
     * @param returnBlocker The async return blocker that contains the response that will be sent to the client.
     * @param courseSourceId Optional.  Can be null or empty string to indicate no filter.  If specified, the returned list
     * of knowledge sessions will be filtered based on the course id.
     */
    public ActiveKnowledgeSessionsCallback(AsyncReturnBlocker<GetActiveKnowledgeSessionsResponse> returnBlocker, String courseSourceId) {
        this.returnBlocker = returnBlocker;
        
        if (courseSourceId != null) {
            this.filteredCourseSourceId = courseSourceId;
        }
        
    }
    
    @Override
    public void success() {
        // Nothing to do here
    }

    @Override
    public void received(Message msg) {
        final Object payload = msg.getPayload();
        KnowledgeSessionsReply knowledgeSessionReply = (KnowledgeSessionsReply)payload;
        
        GetActiveKnowledgeSessionsResponse response = null;
        if (!filteredCourseSourceId.isEmpty()) {
            // Filter by the course id if specified.
            Map<Integer, AbstractKnowledgeSession> filteredMap = 
                    KnowledgeSessionsUtil.filterKnowledgeSessions(filteredCourseSourceId, knowledgeSessionReply.getKnowledgeSessionMap());
            
            // Only send the filtered list to the client.
            KnowledgeSessionsReply filteredReply = new KnowledgeSessionsReply(filteredMap);
            filteredReply.setCanHost(knowledgeSessionReply.canHost());
            
            response = new GetActiveKnowledgeSessionsResponse(true,
                    "success", filteredReply);

        } else {
            // Otherwise, simply return the full unfiltered list.
            response = new GetActiveKnowledgeSessionsResponse(true,
                    "success", knowledgeSessionReply);
        }
        
        returnBlocker.setReturnValue(response);
        
    }

    @Override
    public void failure(Message msg) {
        logger.error("Failed to retrieve the active knowledge sessions: " + msg);
        
        String errorMsg = "Failed to retrieve the active knowledge sessions because '" + msg + "'.";
        if (msg.getPayload() instanceof NACK) {
            errorMsg =((NACK) msg.getPayload()).getErrorMessage();
        }
        GetActiveKnowledgeSessionsResponse response = new GetActiveKnowledgeSessionsResponse(false,
                errorMsg, null);
        returnBlocker.setReturnValue(response);
    }

    @Override
    public void failure(String why) {
        
        logger.error("Failed to retrieve the active knowledge sessions because '" + why + "'.");
        GetActiveKnowledgeSessionsResponse response = new GetActiveKnowledgeSessionsResponse(false,
                "Failed to retrieve the active knowledge sessions because '" + why + "'.", null);
        returnBlocker.setReturnValue(response);
        
    }
    

}
