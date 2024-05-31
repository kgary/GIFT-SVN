/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.shared.websocket.messages;

import mil.arl.gift.common.gwt.shared.websocket.responsedata.AbstractMessageResponseData;


/**
 * The AsyncMessageResponse class is used as the response to an async websocket message.  
 * If a simple 'success/fail' is needed, simply set the isSuccess flag to indicate success or failure.
 * An optional message can be used as well (on failure for example) to indicate whey there was a failure.
 * 
 * Additionally if more complex data is needed in the response, the responseData can be set before sending
 * the response back.  The responseData can be any custom data type that may be needed by the implementer.
 * 
 * @author nblomberg
 *
 */
public class AsyncMessageResponse extends AbstractWebSocketMessage {

   
    /** Indicates if there response is successful or failed. */
    private boolean isSuccess = false;
    
    /** An optional result message that can be used to indicate why a failure occurred. */
    private String resultMessage;
    
    /** The origin message id of the message being responded to. */
    private int originId = AbstractWebSocketMessage.INVALID_MESSAGE_ID;
    
    /** (Optional) Users can extend optional data to be encoded in the response that can be handled if needed. */
    private AbstractMessageResponseData responseData;
    
    /** 
     * Constructor - default (required for GWT serialization).
     */
    public AsyncMessageResponse() {
        super();
    }
    
    /**
     * Accessor to get the result message. 
     * @return String the result message.
     */
    public String getResultMessage() {
        return resultMessage;
    }
    
    /**
     * Accessor to set the result message.
     * @param resultMessage The result message which can provide details of why a failure occurred. 
     */
    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }
    
    /**
     * Accessor to set the message success.
     * 
     * @param success True to indicate that the original message request was successful.  False to indicate that the message failed.
     */
    public void setIsSuccess(boolean success) {
        isSuccess = success;
    }
    
    /**
     * Accessor to get if the original message succeeded or failed.
     * @return True if the message was successful.  False indicates the original message failed.
     */
    public boolean isSuccess() {
        return isSuccess;
    }
    
    /**
     * Accessor to set the id of the original message that was sent that needs a response.
     * 
     * @param originId The id of the original message that was sent that needs a response.
     */
    public void setOriginId(int originId) {
        this.originId = originId;
    }
    
    /**
     * Accessor to get the id of the original message that was sent.
     * 
     * @return The id of the original message that was sent.
     */
    public int getOriginId() {
        return originId;
    }
    
    /**
     * Optional - gets any custom data for the response.
     * @return custom response data that can be used to have further details that may be needed by the caller.  This is optional, and can be null.
     */
    public AbstractMessageResponseData getResponseData() {
        return responseData;
    }
    
    /**
     * Sets any optional custom data that may be needed by the response.
     * 
     * @param data Custom data to be set in the response.
     */
    public void setResponseData(AbstractMessageResponseData data) {
        responseData = data;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("[AsyncMessageResponse: ");
        sb.append("originId = ").append(getOriginId());
        sb.append(", isSuccess = ").append(isSuccess());
        sb.append(", resultMessage = ").append(getResultMessage());
        sb.append(", responseData = ").append(getResponseData());
        sb.append(", ").append(super.toString());
        sb.append("]");
        
        return sb.toString();
    }
}
