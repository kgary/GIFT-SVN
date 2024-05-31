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

import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * The result of a {@link FetchTrainingAppScenarioAdaptations} action containing the
 * scenario adaptations for training applications.
 */
public class FetchTrainingAppScenarioAdaptationsResult extends GatServiceResult {

    /**
     * contains mapping of training applications integrated with GIFT to the scenario adaptation
     * objects that the training application supports. Values are generated classes getCanonicalName() (e.g. generated.dkf.EnvironmentAdaptation.Fog).
     */
    private Map<TrainingApplicationEnum, Set<String>> trainingAppScenarioAdaptationsMap;
	
	/**
	 * Required for GWT serialization.  Can be used for failure responses as well.
	 */
    public FetchTrainingAppScenarioAdaptationsResult(){}
	
	/**
	 * Set the mapping of training applications integrated with GIFT to the scenario adaptation
     * objects that the training application supports.  
	 * 
	 * @param trainingAppScenarioAdaptationsMap mapping of training applications integrated with GIFT to the scenario adaptation
     * objects that the training application supports
	 */
	public FetchTrainingAppScenarioAdaptationsResult(Map<TrainingApplicationEnum, Set<String>> trainingAppScenarioAdaptationsMap){
	    setTrainingAppScenarioAdaptationsMap(trainingAppScenarioAdaptationsMap);
	}

	/**
	 * Gets the mapping of training applications integrated with GIFT to the scenario adaptation
     * objects that the training application supports. 
	 * 
	 * @return mapping of training applications integrated with GIFT to the scenario adaptation
     * objects that the training application supports. Values are generated classes getCanonicalName (e.g. generated.dkf.EnvironmentAdaptation.Fog).
	 */
	public Map<TrainingApplicationEnum, Set<String>> getTrainingAppScenarioAdaptationsMap() {
		return trainingAppScenarioAdaptationsMap;
	}

	/**
	 * Set the mapping of training applications integrated with GIFT to the scenario adaptation
     * objects that the training application supports. 
	 * 
	 * @param trainingAppScenarioAdaptationsMap mapping of training applications integrated with GIFT to the scenario adaptation
     * objects that the training application supports
	 */
	private void setTrainingAppScenarioAdaptationsMap(Map<TrainingApplicationEnum, Set<String>> trainingAppScenarioAdaptationsMap) {
	    
	    if(trainingAppScenarioAdaptationsMap == null){
	        throw new IllegalArgumentException("The training app scenario adaptation map can't be null.");
	    }
		this.trainingAppScenarioAdaptationsMap = trainingAppScenarioAdaptationsMap;
	}


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[FetchConditionsOverallAssessmentTypesResult: trainingAppScenarioAdaptationsMap = {");

        for(TrainingApplicationEnum taType : trainingAppScenarioAdaptationsMap.keySet()){
            builder.append("\n").append(taType.getName()).append(" {").append(trainingAppScenarioAdaptationsMap.get(taType)).append("}");
        }
        builder.append("}");
        builder.append("]");
        return builder.toString();
    }
    
    
}
