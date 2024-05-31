/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action;

import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.common.gwt.shared.DocumentUtil;
import mil.arl.gift.common.io.DetailedException;
import net.customware.gwt.dispatch.shared.Result;

/**
 * Super type of all Gat Service Results.
 *
 * @author cragusa
 */
public class GatServiceResult implements Result {

	/**
	 * Reference to error message (if any).
	 */
	private String errorMsg;
	
	/**
	 * Boolean indicating if the corresponding action was successful.
	 */
	private boolean success = true;
	
	/** Developer-friendly information about the exception. */
	private String errorDetails;
	
	/** The cause of the exception. */
	private ArrayList<String> errorStackTrace;
	
	/* List of exceptions reached. */
	private List<DetailedException> exceptionsList = null;

	private static final String IMAGE_TAG = "<img";
	
	private static final String LINE_BREAK = "<br/>";
	
	/**
	 * No-arg constructor. Needed for serialization.
	 * Defaults to successful result.
	 */
	public GatServiceResult() {		
	}
	
	/**
	 * Gets the (optional) error message.
	 * 
	 * @return null if isSuccess() returns true, otherwise returns a String with the error message.
	 */
	public String getErrorMsg() {
		return this.errorMsg;
	}
	
	/**
	 * Sets the error message.
	 * 
	 * @param msg The error message.
	 */
	public void setErrorMsg(String msg) {
		this.errorMsg = msg;
	}
	
	/**
	 * Method used to determine if the corresponding action succeeded.
	 * 
	 * @return true if the corresponding action succeeded, otherwise returns false.
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * Sets the success value.
	 * 
	 * @param success A boolean value indicating whether the corresponding action succeeded or not.
	 */
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	/**
	 * Gets the developer-friendly information about the exception, while also escaping the string if there are no html characters for a cleaner message.
	 * 
	 * @return the developer-friendly message about the exception. Can be null.
	 */
	public String getErrorDetails() {
		if(errorDetails != null) {
			return (errorDetails.contains(IMAGE_TAG) || errorDetails.contains(LINE_BREAK)) ?  errorDetails : DocumentUtil.escapeHTML(errorDetails);
		} else {
			return null;
		}
	}

	/**
	 * Sets the developer-friendly information about the exception.
	 * 
	 * @param details - the developer-friendly message about the exception. Can be null.
	 */
	public void setErrorDetails(String details) {
		this.errorDetails = details;
	}

	/**
	 * Gets the stack trace of the exception thrown.
	 * 
	 * @return the stack trace of the reported exception.
	 */
	public ArrayList<String> getErrorStackTrace() {
		return errorStackTrace;
	}

	/**
	 * Sets the stack trace of the exception thrown.
	 * 
	 * @param stackTrace - the stack trace of the reported exception
	 */
	public void setErrorStackTrace(ArrayList<String> stackTrace) {
		this.errorStackTrace = stackTrace;
	}
	
	/**
	 * Gets the list of exceptions. 
	 * 
	 * @param exceptionsList - the list of exceptions.
	 */
	public List<DetailedException> getExceptionsList() {
		return exceptionsList;
	}
	
	/**
	 * Sets the list of exceptions. 
	 * 
	 * @param exceptionsList - the list of exceptions.
	 */
	public void setExceptionsList(List<DetailedException> exceptionsList) {
		this.exceptionsList = exceptionsList;
	}

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[GatServiceResult: success=");
        builder.append(success);
        
        if(!success){
            builder.append(", errorMsg=");
            builder.append(errorMsg);
            builder.append(", errorDetails=");
            builder.append(errorDetails);
            builder.append(", errorStackTrace=");
            builder.append(errorStackTrace);
        }
        
        builder.append("]");
        return builder.toString();
    }

}
