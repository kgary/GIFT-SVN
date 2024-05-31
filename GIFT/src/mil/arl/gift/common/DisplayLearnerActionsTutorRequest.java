/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import generated.dkf.ScenarioControls;

/**
 * This class contains information about the available learner actions that need to be displayed by the Tutor
 *
 * @author jleonard
 */
public class DisplayLearnerActionsTutorRequest {

    /** The list of learner actions to display */
    private List<generated.dkf.LearnerAction> actions = new ArrayList<generated.dkf.LearnerAction>();
    
    /** The scenario controls to make available to the tutor */
    private ScenarioControls controls;

    /**
     * Constructor
     *
     * @param actions The list of learner actions to display. Cannot be null.
     * @param controls The scenario controls to make available to the tutor. Can be null.
     */
    public DisplayLearnerActionsTutorRequest(List<generated.dkf.LearnerAction> actions, ScenarioControls controls) {
        this.actions.addAll(actions);
        this.controls = controls;
    }

    /**
     * Gets the list of learner actions to be displayed
     *
     * @return List<generated.dkf.LearnerAction> The list of learner actions to be displayed
     */
    public List<generated.dkf.LearnerAction> getActions() {
        return actions;
    }
    
    /**
     * Gets the scenario controls to make available to the tutor. Can return null.
     * 
     * @return ScenarioControls the scenario controls to make available to the tutor
     */
    public ScenarioControls getControls() {
        return controls;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[DisplayLearnerActionsTutorRequest: ");
        
        sb.append("actions = {");
        for (generated.dkf.LearnerAction action : actions) {
            sb.append("\n[").append(action.getDisplayName()).append(" type = ").append(action.getType().name());
            if(action.getLearnerActionParams() != null){
                
                if(action.getLearnerActionParams() != null){
                    Serializable actionParams = action.getLearnerActionParams();
                    if(actionParams instanceof generated.dkf.TutorMeParams){
                        generated.dkf.TutorMeParams tutorMeParams = (generated.dkf.TutorMeParams)actionParams;
                        
                        Serializable configuration = tutorMeParams.getConfiguration();
                        if(configuration instanceof generated.dkf.AutoTutorSKO){
                            generated.dkf.AutoTutorSKO autoTutorSKO = (generated.dkf.AutoTutorSKO)configuration;
                            Serializable scriptType = autoTutorSKO.getScript();
                            
                            if(scriptType instanceof generated.dkf.LocalSKO){
                                generated.dkf.LocalSKO localSKO = (generated.dkf.LocalSKO)scriptType;
                                sb.append(", AutoTutor Local SKO = ").append(localSKO.getFile());
                            }else if(scriptType instanceof generated.dkf.ATRemoteSKO){
                                generated.dkf.ATRemoteSKO remoteSKO = (generated.dkf.ATRemoteSKO)scriptType;
                                sb.append(", AutoTutor remote SKO = ").append(remoteSKO.getURL());
                            }
                            
    
                        }else if(configuration instanceof String){
                            sb.append(", conversation file = ").append(configuration);
                        }
                    }else if(actionParams instanceof generated.dkf.LearnerAction.StrategyReference){
                        generated.dkf.LearnerAction.StrategyReference strategyReference = (generated.dkf.LearnerAction.StrategyReference)actionParams;
                        sb.append(", strategy reference = ").append(strategyReference.getName());
                    }
                }
            }
            
            if(action.getDescription() != null){
                sb.append(", description = ").append(action.getDescription());
            }
            
            sb.append("],");
        }
        sb.append("}");
        sb.append(", controls = {");
        
        if(controls != null) {
            sb.append("ScenarioControls: preventManualStop = ");
            sb.append(controls.getPreventManualStop() != null);
            
        } else {
            sb.append("null");
        }
        
        sb.append("}");
        sb.append("]");

        return sb.toString();
    }
}
