/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.rpcs;

import mil.arl.gift.common.gwt.client.DetailedRpcResponse;

/**
 * A response containing a double value
 * 
 * @author nroberts
 */
public class DoubleResponse extends DetailedRpcResponse {

	/** The double value*/
	private double value;
	
	/**
	 * Public no-arg constructor. Required by GWT RPC.
	 */
	public DoubleResponse(){
		
	}
	
	/**
	 * Creates a response containing a double value
	 * 
	 * @param isSuccess whether or not the export was successful
	 * @param response error or success response
	 * @param value the double value
	 */
	public DoubleResponse(boolean isSuccess, String response, double value){
		this.setIsSuccess(isSuccess);
		this.setResponse(response);
		this.value = value;
	}

	/**
	 * Gets the double value
	 * 
	 * @return the double value
	 */
	public double getValue() {
		return value;
	}
}
