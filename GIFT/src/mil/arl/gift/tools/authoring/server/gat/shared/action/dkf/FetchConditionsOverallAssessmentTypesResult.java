/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.dkf;

import java.util.Map;
import java.util.Set;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * The result of a {@link FetchConditionsOverallAssessmentTypes} action containing information
 * about the condition's ability to populate overall assessment scorer types.
 */
public class FetchConditionsOverallAssessmentTypesResult extends GatServiceResult {

    /**
     * the set of overall assessment types to the condition classes can populate it its life cycle.
     * Key: condition class name without the src package prefix {@link PACKAGE_PATH} (e.g. domain.knowledge.condition.EliminateHostilesCondition)
     * Values: {@link generated.dkf.Count}, {@link generated.dkf.ViolationTime},
     * {@link generated.dkf.CompletionTime}.  Can be empty or null.
     */
    private Map<String, Set<String>> overallAssessmentTypesConditionsMap;
	
	/**
	 * Required for GWT serialization.  Can be used for failure responses as well.
	 */
    public FetchConditionsOverallAssessmentTypesResult(){}
	
	/**
	 * Set the description for the domain condition.  
	 * 
	 * @param overallAssessmentTypesConditionsMap the set of overall assessment types to the condition classes can populate it its life cycle.
     * Key: condition class name without the src package prefix {@link PACKAGE_PATH} (e.g. domain.knowledge.condition.EliminateHostilesCondition)
     * Values: {@link generated.dkf.Count}, {@link generated.dkf.ViolationTime},
     * {@link generated.dkf.CompletionTime}.  Can be empty or null.
	 */
	public FetchConditionsOverallAssessmentTypesResult(Map<String, Set<String>> overallAssessmentTypesConditionsMap){
	    setOverallAssessmentTypesconditionsMap(overallAssessmentTypesConditionsMap);
	}

	/**
	 * Gets the set of overall assessment types to the condition classes can populate it its life cycle.
	 * 
	 * @return the set of overall assessment types to the condition classes can populate it its life cycle.
     * Key: condition class name without the src package prefix {@link PACKAGE_PATH} (e.g. domain.knowledge.condition.EliminateHostilesCondition)
     * Values: {@link generated.dkf.Count}, {@link generated.dkf.ViolationTime},
     * {@link generated.dkf.CompletionTime}.  Can be empty or null.
	 */
	public Map<String, Set<String>> getOverallAssessmentTypesconditionsMap() {
		return overallAssessmentTypesConditionsMap;
	}

	/**
	 * Sets the set of overall assessment types to the condition classes can populate it its life cycle.
	 * 
	 * @param overallAssessmentTypesConditionsMap the set of overall assessment types to the condition classes can populate it its life cycle.
     * Key: condition class name without the src package prefix {@link PACKAGE_PATH} (e.g. domain.knowledge.condition.EliminateHostilesCondition)
     * Values: {@link generated.dkf.Count}, {@link generated.dkf.ViolationTime},
     * {@link generated.dkf.CompletionTime}.  Can be empty or null. 
	 */
	private void setOverallAssessmentTypesconditionsMap(Map<String, Set<String>> overallAssessmentTypesConditionsMap) {
	    
	    if(overallAssessmentTypesConditionsMap == null){
	        throw new IllegalArgumentException("The overall assessment types condition map can't be null.");
	    }
		this.overallAssessmentTypesConditionsMap = overallAssessmentTypesConditionsMap;
	}


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[FetchConditionsOverallAssessmentTypesResult: overallAssessmentTypesConditionsMap = {");

        for(String condClazz : overallAssessmentTypesConditionsMap.keySet()){
            builder.append("\n").append(condClazz).append(" {").append(overallAssessmentTypesConditionsMap.get(condClazz)).append("}");
        }
        builder.append("}");
        builder.append("]");
        return builder.toString();
    }
    
    
}
