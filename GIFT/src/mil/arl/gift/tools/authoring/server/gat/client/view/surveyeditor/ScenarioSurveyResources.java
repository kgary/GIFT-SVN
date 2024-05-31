/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import generated.dkf.Scenario;

/**
 * Survey resources for a {@link Scenario} whose survey context can be modified by the survey composer
 * 
 * @author nroberts
 */
public class ScenarioSurveyResources extends AbstractSurveyResources {
    
    /** The {@link Course} defining the survey resources */
    private Scenario scenario;
    
    /**
     * Creates a set of survey resources defined by the given scenario
     * 
     * @param scenario the scenario defining the survey resources
     */
    public ScenarioSurveyResources(Scenario scenario) {
        
        if(scenario == null) {
            throw new IllegalArgumentException("The scenario cannot be null");
        }
        
        this.scenario = scenario;
    }

    @Override
    public int getSurveyContextId() {
        
        if(scenario != null 
                && scenario.getResources() != null
                && scenario.getResources().getSurveyContext() != null) {
            
            return scenario.getResources().getSurveyContext().intValue();
        }
        
        return -1;
    }

    @Override
    public boolean hasSharedQuestionBank() {
        return false;
    }

}
