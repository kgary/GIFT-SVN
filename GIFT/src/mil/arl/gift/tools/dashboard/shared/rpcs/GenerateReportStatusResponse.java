/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.rpcs;

import mil.arl.gift.common.ert.GenerateReportStatus;
import mil.arl.gift.common.gwt.client.DetailedRpcResponse;

/**
 * A response containing the status of an export experiment report process
 * 
 * @author nroberts
 */
public class GenerateReportStatusResponse extends DetailedRpcResponse {
	
	/** The status of an experiment report */
	private GenerateReportStatus status;
	
	/**
	 * No-arg public constructor. Required by GWT RPC.
	 */
	public GenerateReportStatusResponse(){
		
	}
	
	/**
	 * Creates a response containing the result of an export
	 * 
	 * @param isSuccess whether or not the export was successful
	 * @param response error or success response
	 * @param status the satus
	 */
	public GenerateReportStatusResponse(boolean isSuccess, String response, GenerateReportStatus status){
		this.setIsSuccess(isSuccess);
		this.setResponse(response);
		this.status = status;
	}

	/**
	 * Gets the status
	 * 
	 * @return the status
	 */
	public GenerateReportStatus getStatus() {
		return status;
	}
}
