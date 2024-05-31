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
import mil.arl.gift.common.gwt.server.AsyncReturnBlocker;
import mil.arl.gift.net.api.MessageCollectionCallback;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.NACK;
import mil.arl.gift.tutor.shared.data.AbstractKnowledgeSessionResponse;

/**
 * Callback to handle a KnowledgeSessionReply payload.  This callback returns
 * a single knowledge session that can be requested to be returned to the client.
 * 
 * @author nblomberg
 *
 */
public class LookupKnowledgeSessionCallback implements MessageCollectionCallback {

    /** Instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(LookupKnowledgeSessionCallback.class);
    
    /** Async return blocker containing the response that will be sent to the client. */
    private AsyncReturnBlocker<AbstractKnowledgeSessionResponse> returnBlocker;
    
    /** The domain id of the session to be returned to the client (if found). */
    private int lookupDomainId;
    
    /** The browser id making the request. */
    private String browserSessionId;
    
    /**
     * Constructor
     */
    public LookupKnowledgeSessionCallback(AsyncReturnBlocker<AbstractKnowledgeSessionResponse> returnBlocker, 
            int lookupDomainId, String browserSessionId) {
        this.returnBlocker = returnBlocker;
        this.lookupDomainId = lookupDomainId;
        this.browserSessionId = browserSessionId;
    }
    
    @Override
    public void success() {
        // Nothing to do here
    }

    @Override
    public void received(Message msg) {
        if (logger.isDebugEnabled()) {
            logger.debug("LookupKnowledgeSessionCallback - Message reply received: " + msg.getMessageType());
        }
        final Object payload = msg.getPayload();
        KnowledgeSessionsReply knowledgeSessionReply = (KnowledgeSessionsReply)payload;
        Map<Integer, AbstractKnowledgeSession> idToSessionMap = knowledgeSessionReply.getKnowledgeSessionMap();
        
        if (logger.isDebugEnabled()) {
            logger.debug("Domain SessionId to lookup =" + lookupDomainId);
            logger.debug("Returned Sessions = " + idToSessionMap.size());
        }

        // Get the session that was created (based on the domain session id)
        AbstractKnowledgeSession foundSession = idToSessionMap.get(lookupDomainId);
        if (logger.isDebugEnabled()) {
            logger.debug("found knowledge session=" + foundSession);
        }
        
        AbstractKnowledgeSessionResponse response = null; 
        if (foundSession != null) {
            response = new AbstractKnowledgeSessionResponse(true,
                    "success", foundSession);
        } else {
            response = new AbstractKnowledgeSessionResponse(false,
                    "Received a list of knowledge sessions, but could not find one with domain id: " + lookupDomainId, null);
        }
        
        returnBlocker.setReturnValue(response);
        
    }

    @Override
    public void failure(Message msg) {
        String errorMsg = "The knowledge session request failed because msg='" + msg + "'.";
        logger.error(errorMsg);
        if(msg.getPayload() instanceof NACK){
            NACK nack = (NACK)msg.getPayload();
            errorMsg = nack.getErrorMessage();
            
        }
        AbstractKnowledgeSessionResponse response = new AbstractKnowledgeSessionResponse(false,
                errorMsg, null);
        response.setBrowserSessionId(browserSessionId);
        returnBlocker.setReturnValue(response);
    }

    @Override
    public void failure(String why) {
        
        String errorMsg = "The knowledge session request failed because why='" + why + "'.";
        logger.error(errorMsg);
        AbstractKnowledgeSessionResponse response = new AbstractKnowledgeSessionResponse(false,
                errorMsg, null);
        response.setBrowserSessionId(browserSessionId);
        returnBlocker.setReturnValue(response);
    }

}
