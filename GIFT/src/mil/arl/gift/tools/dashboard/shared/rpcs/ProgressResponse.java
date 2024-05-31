/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.rpcs;

import mil.arl.gift.common.gwt.client.DetailedRpcResponse;
import mil.arl.gift.common.io.ProgressIndicator;

/**
 * A RPC that is used to retrieve information related to a task's current progress
 * 
 * @author nroberts
 */
public class ProgressResponse extends DetailedRpcResponse {

	private ProgressIndicator progress;
	
	/**
	 * No-arg public constructor. Required by GWT RPC.
	 */
	public ProgressResponse(){
		
	}
	
	 /**
     * Creates a RPC that is used to retrieve information related to a task's current progress
     * 
     * @param isSuccess - true if the rpc is a success, false otherwise.
     * @param response - Error or success response.
     * @param progress - Information related to a task's current progress.  Can be null.
     */
	public ProgressResponse(boolean isSuccess, String response, ProgressIndicator progress) {
		this.progress = progress;
		this.setResponse(response);
		this.setIsSuccess(isSuccess);
	}

	/**
	 * Return the current progress.
	 * @return can be null
	 */
	public ProgressIndicator getProgress() {
		return progress;
	}
	
	@Override
    public String toString() {
	    StringBuilder sb = new StringBuilder();
	    sb.append("[ProgressResponse: ");
	    sb.append("progress = ").append(progress);
	    sb.append(super.toString());
	    sb.append("]");
	    return sb.toString();
	}
}
