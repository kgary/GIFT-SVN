/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.rpcs;

import mil.arl.gift.common.experiment.DataCollectionItem;
import mil.arl.gift.common.gwt.client.DetailedRpcResponse;

/**
 * A response containing an experiment
 * 
 * @author nroberts
 */
public class ExperimentResponse extends DetailedRpcResponse {
	
	/** The experiment */
	private DataCollectionItem experiment;
	
	/**
	 * No-arg public constructor. Required by GWT RPC.
	 */
	public ExperimentResponse(){
		
	}
	
	/**
	 * Creates a response containing an experiment
	 * 
	 * @param isSuccess whether or not the list was successfully retrieved
	 * @param response error or success response
	 * @param experiment the experiment
	 */
	public ExperimentResponse(boolean isSuccess, String response, DataCollectionItem experiment){
		this.setIsSuccess(isSuccess);
		this.setResponse(response);
		this.experiment = experiment;
	}

	/**
	 * Gets the experiment
	 * 
	 * @return the experiment
	 */
	public DataCollectionItem getExperiment() {
		return experiment;
	}
}
