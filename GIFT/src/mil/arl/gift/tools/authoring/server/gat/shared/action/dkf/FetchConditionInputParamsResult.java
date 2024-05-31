/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.dkf;

import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * A result that stores the input parameters for a condition implementation.
 * 
 * @author bzahid
 */
public class FetchConditionInputParamsResult extends GatServiceResult {
	
	/** The condition input parameters. */
	private List<String> inputParams = new ArrayList<String>();
	
	/** 
	 * Instantiates a new result. 
	 */
	public FetchConditionInputParamsResult() {
		super();
	}

	/**
	 * Gets the input parameters for a condition implementation.
	 * 
	 * @return the valid input parameters.
	 */
	public List<String> getInputParams() {
		return inputParams;
	}

	/**
	 * Sets the input parameters for a condition implementation.
	 * 
	 * @param inputParams the valid input parameters for a condition implentation.
	 */
	public void setInputParams(List<String> inputParams) {
		this.inputParams = inputParams;
	}
	
}
