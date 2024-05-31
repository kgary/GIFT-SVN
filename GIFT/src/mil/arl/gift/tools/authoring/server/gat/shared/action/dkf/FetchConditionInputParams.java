/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.dkf;

import net.customware.gwt.dispatch.shared.Action;

/**
 * An action that gets input parameters for a condition implementation.
 * 
 * @author bzahid
 */
public class FetchConditionInputParams implements Action<FetchConditionInputParamsResult> {
	
	/** The condition implementation to retrieve valid input parameters for. */
	String conditionImpl;
	
	/**
	 * Default constructor
	 */
	public FetchConditionInputParams() {
		
	}
	
	/**
	 * Initializes a new action.
	 * 
	 * @param conditionImpl The condition implementation to retrieve valid input parameters for.
	 */
	public FetchConditionInputParams(String conditionImpl) {
		this.conditionImpl = conditionImpl;
	}

	/**
	 * Gets the condition implementation.
	 * 
	 * @return the condition implementation.
	 */
	public String getConditionImpl() {
		return conditionImpl;
	}

	/**
	 * Sets the condition implementation to retrieve valid input parameters for.
	 * 
	 * @param conditionImpl The condition implementation.
	 */
	public void setConditionImpl(String conditionImpl) {
		this.conditionImpl = conditionImpl;
	}
	
    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[FetchConditionInputParams: ");
        sb.append("conditionImpl = ").append(conditionImpl);
        sb.append("]");

        return sb.toString();
    } 
}
