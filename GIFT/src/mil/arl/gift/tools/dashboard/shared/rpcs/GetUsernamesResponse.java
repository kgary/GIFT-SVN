/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.rpcs;

import java.util.List;

import mil.arl.gift.common.gwt.client.RpcResponse;

public class GetUsernamesResponse extends RpcResponse {

	private List<String> usernames;
	
    /**
     * Default Constructor
     *
     * Required for GWT
     */
	public GetUsernamesResponse() {
		
	}

    /**
     * An rpc that is used to retrieve the usernames from the ums database
     * 
     * @param success - true if the rpc is a success, false otherwise.
     * @param response - Error or success response.
     * @param usernames - The list of usernames retrieved from the ums manager
     */
	public GetUsernamesResponse(boolean isSuccess, String response, List<String> usernames) {
		this.usernames = usernames;
		this.setResponse(response);
		this.setIsSuccess(isSuccess);
	}
	
	/** 
	 * Accessor to get the usernames list of the response
     * @return the usernames list of the response (can be null).
     */
	public List<String> getUsernamesList() {
		return usernames;
	}
	
}
