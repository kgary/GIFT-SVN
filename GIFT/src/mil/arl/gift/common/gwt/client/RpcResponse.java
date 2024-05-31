/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A response back from the RPC service, indicating success or failure
 *
 * @author jleonard
 */
public class RpcResponse implements IsSerializable {

    private String userSessionId;
    private String browserSessionId;
    
    /** whether or not the RPC call was successfully handled */
    private boolean success;
    
    /** a simple message about the response */
    private String response;
    
    /** (optional) additional information about the response that could be shown to the user */
    private String additionalInformation;
    	
    /** (optional) the stack trace of an exception thrown during the RPC call */
    private List<String> errorStackTrace;
    
    /**
     * Default Constructor
     *
     * Required for GWT
     */
    public RpcResponse() {
    }

    /**
     * Constructor
     * 
     * @param userSessionId The user session ID of the requesting user
     * @param browserSessionId The session ID of the requesting browser
     * @param success If the request was successful
     * @param response The message of the response
     */
    public RpcResponse(String userSessionId, String browserSessionId, boolean success, String response) {
        this.userSessionId = userSessionId;
        this.browserSessionId = browserSessionId;
        this.success = success;
        this.response = response;
    }
    
    /**
     * Gets the user session ID of the requesting browser
     * 
     * @return String The user session ID of the requesting user
     */
    public String getUserSessionId() {
        return userSessionId;
    }
    
    /**
     * Sets the user session ID of the requesting browser
     * 
     * @param userSessionId The user session ID of the requesting user
     */
    public void setUserSessionId(String userSessionId) {
        this.userSessionId = userSessionId;
    }

    /**
     * Gets the browser session ID of the requesting browser
     * 
     * @return String The browser session ID of the requesting browser
     */
    public String getBrowserSessionId() {
        return browserSessionId;
    }
    
    /**
     * Sets the browser session ID of the requesting browser
     * 
     * @param browserSessionId The browser session ID of the requesting browser
     */
    public void setBrowserSessionId(String browserSessionId) {
        this.browserSessionId = browserSessionId;
        
    }

    /**
     * True if the request was successful
     * 
     * @return boolean If the request was successful 
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Sets if the request was successful
     * 
     * @param success If the request was successful
     */
    public void setIsSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Gets the message of the response
     * 
     * @return String The message of the response
     */
    public String getResponse() {
        return response;
    }

    /**
     * Sets the message of the response
     * 
     * @param response The message of the response
     */
    public void setResponse(String response) {
        this.response = response;
    }
    
    public void setAdditionalInformation(String additionalInformation){
        this.additionalInformation = additionalInformation;
    }
    
    /**
     * Return the additional information about the response that could be shown to the user.
     * 
     * @return String can be null. 
     */
    public String getAdditionalInformation(){
        return additionalInformation;
    }
    
    /**
     * Gets the stack trace of an exception caught during the RPC.
     * 
	 * @return the stack trace of an exception caught during the RPC. Can be null.
	 */
	public List<String> getErrorStackTrace() {
		return errorStackTrace;
	}
	
    /**
     * Sets the stack trace of an exception caught during the RPC.
     * 
	 * @param errorStackTrace the stack trace of an exception caught during the RPC. Can be null.
	 */
	public void setErrorStackTrace(List<String> errorStackTrace) {
		this.errorStackTrace = errorStackTrace;
	}

	@Override
    public String toString() {
	    
	    StringBuffer sb = new StringBuffer();
	    sb.append("[RpcResponse: ");
	    sb.append("userSessionId=").append(userSessionId);
	    sb.append(", browserSessionId=").append(browserSessionId);
	    sb.append(", success=").append(success);
	    sb.append(", response=").append(response);
	    sb.append(", additionalInformation=").append(additionalInformation);
	    sb.append("]");
	    
	    return sb.toString();
    }
}
