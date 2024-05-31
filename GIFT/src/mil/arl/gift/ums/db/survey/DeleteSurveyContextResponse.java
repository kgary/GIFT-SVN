/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db.survey;

import java.util.List;

/**
 * This class contains information about the delete survey context operation
 * 
 * @author bzahid
 */
public class DeleteSurveyContextResponse {

	/** Whether or not the delete operation succeeded */
	private boolean success = false;
	
	/** Whether or not the user has permission to delete the survey */
	private boolean isAllowedUser = true;	
	
	/** Whether or not the deletion failed because one or more survey elements had a set of responses associated with it */
	private boolean hadSurveyResponses = false;
	
	/** Information about why the delete operation failed */
	private String response = null;
	
    /** (optional) the stack trace of an exception thrown during the RPC call */
    private List<String> errorStackTrace;

	/**
	 * Gets whether or not the delete operation was successful.
	 * 
	 * @return true if the delete operation succeeded, false otherwise.
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * Sets whether or not the operation was successful.
	 * 
	 * @param success true if the delete operation succeeded, false otherwise.
	 */
	public void setIsSuccess(boolean success) {
		this.success = success;
	}

	/**
	 * Gets whether or not the user has permission to delete the survey context.
	 * 
	 * @return true if the user has permission to delete the survey context, false otherwise.
	 */
	public boolean isAllowedUser() {
		return isAllowedUser;
	}

	/**
	 * Sets whether or not the user has permission to delete the survey context.
	 * 
	 * @param isAllowedUser true if the user has permission to delete the survey context, false otherwise.
	 */
	public void setIsAllowedUser(boolean isAllowedUser) {
		this.isAllowedUser = isAllowedUser;
	}

	/**
	 * Gets whether or not the survey context contains survey responses.
	 *  
	 * @return true if survey context contains survey responses, false otherwise.
	 */
	public boolean hadSurveyResponses() {
		return hadSurveyResponses;
	}

	/**
	 * Sets whether or not the survey context contains survey responses.
	 * 
	 * @param hadSurveyResponses true if the survey context contains survey responses, false otherwise.
	 */
	public void setHadSurveyResponses(boolean hadSurveyResponses) {
		this.hadSurveyResponses = hadSurveyResponses;
	}

	/**
	 * Gets information about why the delete operation failed.
	 * 
	 * @return the response containing information about why the delete operation failed.
	 */
	public String getResponse() {
		return response;
	}

	/**
	 * Sets information about why the delete operation failed.
	 * 
	 * @param response information about why the delete operation failed.
	 */
	public void setResponse(String response) {
		this.response = response;
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
        StringBuilder builder = new StringBuilder();
        builder.append("[DeleteSurveyContextResponse: success=");
        builder.append(success);
        builder.append(", isAllowedUser=");
        builder.append(isAllowedUser);
        builder.append(", hadSurveyResponses=");
        builder.append(hadSurveyResponses);
        builder.append(", response=");
        builder.append(response);
        builder.append("]");
        return builder.toString();
    }
	
}
