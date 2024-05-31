/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client;

import java.util.ArrayList;

/**
 * An extension of {@link mil.arl.gift.common.gwt.client.RpcResponse RpcResponse} that provides detailed information regarding any errors that happen 
 * during an RPC call.
 * 
 * @author nroberts
 */
public class DetailedRpcResponse extends RpcResponse {
	
	/** The message that should be shown back to the user if an error is encountered*/
	private String errorMessage;
	
	/** A more robust error message mentioning details that may be useful for developers */
	private String errorDetails;
	
	/** The stack trace for the error if one is encountered, if applicable*/
	private ArrayList<String> errorStackTrace;

	/**
     * Default Constructor
     *
     * Required for GWT
     */
    public DetailedRpcResponse() {
    }

    /**
     * Constructor
     * 
     * @param userSessionId The user session ID of the requesting user
     * @param browserSessionId The session ID of the requesting browser
     * @param success If the request was successful
     * @param response The message of the response
     * @param errorMessage The error message of the response.  Can't be null or empty.
     * @param errorDetails The details of the error.  Can be null or empty.
     * @param errorStackTrace The stack trace of the error
     */
    public DetailedRpcResponse(String userSessionId, String browserSessionId, boolean success, String response, String errorMessage, String errorDetails, ArrayList<String> errorStackTrace) {
        super(userSessionId, browserSessionId, success, response);
        
        setErrorMessage(errorMessage);
        setErrorDetails(errorDetails);
        this.errorStackTrace = errorStackTrace;
    }

	/**
	 * Gets the message that should be shown back to the user if an error is encountered
	 * 
	 * @return the error message.  Won't be null or empty.
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Sets the message that should be shown back to the user if an error is encountered
	 * 
	 * @param errorMessage the error message.  Can't be null or empty.
	 */
	public void setErrorMessage(String errorMessage) {
	    
	    if(errorMessage == null || errorMessage.isEmpty()){
	        throw new IllegalArgumentException("The error message can't be null or empty.");
	    }
	    
		this.errorMessage = errorMessage;
	}

	/**
	 * Gets a more robust error message mentioning details that may be useful for developers
	 * 
	 * @return the error details. Can be null. 
	 */
	public String getErrorDetails() {
		return errorDetails;
	}

	/**
	 * Sets a more robust error message mentioning details that may be useful for developers
	 * 
	 * @param errorDetails the error details.
	 */
	public void setErrorDetails(String errorDetails) {	       
		this.errorDetails = errorDetails;
	}

	/**
	 * Gets the stack trace for the error if one is encountered, if applicable
	 * 
	 * @return the error stack trace.  Can be null or empty.
	 */
	@Override
    public ArrayList<String> getErrorStackTrace() {
		return errorStackTrace;
	}

	/**
	 * Sets the stack trace for the error if one is encountered, if applicable
	 * 
	 * @param errorStackTrace the error stack trace to set
	 */
	public void setErrorStackTrace(ArrayList<String> errorStackTrace) {
		this.errorStackTrace = errorStackTrace;
	}
	
	@Override
    public String toString(){
	    
	    StringBuilder sb = new StringBuilder();
	    sb.append("[DetailedRpcResponse: ");
	    sb.append("message = ").append(getErrorMessage());
	    sb.append(", details = ").append(getErrorDetails());
	    sb.append(", ").append(super.toString());
	    sb.append("]");
	    return sb.toString();
	}
}
