/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.server.websocket;

import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.server.websocket.AbstractServerWebSocket;
import mil.arl.gift.common.gwt.shared.websocket.messages.AbstractAsyncWebSocketMessage;
import mil.arl.gift.common.gwt.shared.websocket.messages.AbstractWebSocketMessage;
import mil.arl.gift.common.gwt.shared.websocket.messages.AsyncMessageResponse;
import mil.arl.gift.common.gwt.shared.websocket.responsedata.RpcResponseData;
import mil.arl.gift.tutor.server.TutorBrowserWebSession;
import mil.arl.gift.tutor.server.TutorModule;
import mil.arl.gift.tutor.shared.AbstractAction;
import mil.arl.gift.tutor.shared.websocket.messages.DoActionAsyncMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A web socket used by the tutor server that is used to push tutor actions to the web client.
 * 
 * @author nblomberg
 *
 */
public class TutorServerWebSocket extends AbstractServerWebSocket {

    /** Instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(TutorServerWebSocket.class.getName());

    /** 
     * Constructor - default
     */
    public TutorServerWebSocket() {
        super();
        logger.info("TutorServerWebSocket()");
        
    }


    @Override
    public void onReceiveMessage(AbstractWebSocketMessage message) {
        logger.info("onReceiveMessage message = " + message);
        
        // For now, nothing to do here since the server is only pushing messages to the client.
    }

    @Override
    public void onReceiveMessageAsync(AbstractAsyncWebSocketMessage message, final AsyncMessageResponse response) {
        
        
        if (message instanceof DoActionAsyncMessage) {
            
            // Handle the do action request.
            RpcResponse rpcResponse = handleDoActionAsyncMessage((DoActionAsyncMessage)message);
            
            // Send a response (which is a wrapper containing the old gwt-rpc response data).
            // This is for backwards compatibility so that the original logic can be adapted to websockets
            // with little changes.
            RpcResponseData rpcResponseData = new RpcResponseData();
            rpcResponseData.setRpcResponse(rpcResponse);
            
            // Set the websocket response to success.  The RpcResponseData contains the actual response for backwards compatibility.
            response.setIsSuccess(true);
            response.setResponseData(rpcResponseData);
            
            send(response);
        }
        
        
    }

    
    /**
     * Handler for the websocket DoActionAsyncMessage.  This logic was moved from the GWT-rpc handler in
     * TutorUserInterfaceServiceImpl.java and was converted to use websockets.
     * 
     * @param message The websocket message containing the action data.
     * @return The gwt rpcResponse containing the result (success or failure).
     */
    private RpcResponse handleDoActionAsyncMessage(DoActionAsyncMessage message) {
        String browserSessionKey = message.getBrowserSessionKey();
        AbstractAction action = message.getAction();
        TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionKey);
        RpcResponse response = null;
        try{
            if (session != null) {
                response = session.handleClientAction(action);
            } else {
                logger.warn("doAction session = null, using null for userSessionId and browserSessionId and response = false for message " + message);
                response = new RpcResponse(null, null, false, "Not a valid browser session.");
            }
        }catch(IllegalArgumentException e){
            
            String errorMsg = "Exception caught in doAction with the message: " + e.getMessage()+".  The action is "+action+" for session "+session;
            logger.error("Exception caught in doAction with the message: " + e.getMessage()+".  The action is "+action+" for session "+session, e);
            response = new RpcResponse(null, null, false, errorMsg);
        }
        
        return response;
    }
   
}
