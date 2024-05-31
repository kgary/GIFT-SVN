/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.rpcs;

import java.util.ArrayList;

import mil.arl.gift.common.experiment.DataCollectionItem;
import mil.arl.gift.common.gwt.client.DetailedRpcResponse;

/**
 * A response containing a list of experiments
 * 
 * @author nroberts
 */
public class ExperimentListResponse extends DetailedRpcResponse {
	
	/** The experiment list*/
	private ArrayList<DataCollectionItem> experiments;
	
	/**
	 * No-arg public constructor. Required by GWT RPC.
	 */
	public ExperimentListResponse(){
		
	}
	
	/**
	 * Creates a response containing a list of experiments
	 * 
	 * @param isSuccess whether or not the list was successfully retrieved
	 * @param response error or success response
	 * @param experiments the list of experiments
	 */
	public ExperimentListResponse(boolean isSuccess, String response, ArrayList<DataCollectionItem> experiments){
		this.setIsSuccess(isSuccess);
		this.setResponse(response);
		this.experiments = experiments;
	}

	/**
	 * Gets the list of experiments
	 * 
	 * @return the list of experiments
	 */
	public ArrayList<DataCollectionItem> getExperiments() {
		return experiments;
	}
}
