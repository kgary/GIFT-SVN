/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.dkf;

import mil.arl.gift.common.course.InteropsInfo.ConditionInfo;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * The result of a {@link FetchConditionImplDescription} action containing the condition information requested
 */
public class FetchConditionImplDescriptionResult extends GatServiceResult {

	/** information about a condition including the description as an inline string.  */
	private ConditionInfo conditionInfo;
	
	/**
	 * Required for GWT serialization.  Can be used for failure responses as well.
	 */
    public FetchConditionImplDescriptionResult(){}
	
	/**
	 * Set the description for the domain condition.  
	 * 
	 * @param description the generic description for a domain condition.  Can't be null or empty. Supports HTML syntax. 
	 */
	public FetchConditionImplDescriptionResult(ConditionInfo conditionInfo){
	    setConditionInfo(conditionInfo);
	}

	/**
	 * Gets the information about a condition including the description as an inline string.
	 * 
	 * @return the information about a condition including the description as an inline string.  Won't be null. 
	 */
	public ConditionInfo getConditionInfo() {
		return conditionInfo;
	}

	/**
	 * Sets the information about a condition including the description as an inline string.
	 * 
	 * @param conditionInfo information about a condition including the description as an inline string.  Can't be null. 
	 */
	private void setConditionInfo(ConditionInfo conditionInfo) {
	    
	    if(conditionInfo == null){
	        throw new IllegalArgumentException("The condition info can't be null.");
	    }
		this.conditionInfo = conditionInfo;
	}


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[FetchConditionImplDescriptionResult: conditionInfo = ");
        builder.append(conditionInfo);
        builder.append("]");
        return builder.toString();
    }
    
    
}
