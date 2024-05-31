/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge;

import java.io.IOException;

import generated.dkf.ObserverControls;
import generated.dkf.ScenarioControls;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.domain.learneraction.LearnerActionsManager;


/**
 * This class contains the various types of resources available for a scenario.
 * 
 * @author mhoffman
 *
 */
public class Resources {
    
    /** unique db id for domain survey context which has survey(s) associated with it */
    private Integer surveyContextId;
    
    private LearnerActionsManager actionsManager;
    
    /** The scenario controls to make available to the tutor */
    private ScenarioControls scenarioControls;
    
    /** The data controlling how a scenario's knowledge session is presented to an observer controller */
    private ObserverControls observerControls;
    
    /**
     * Class constructor
     * 
     * @param resources - the domain configuration container for all resources
     * @param courseFolder the course folder that contains all course assets
     * @param skipExternalFileLoading whether to skip loading/checking files that this DKF references (e.g. learner actions xml, conversation tree xml) 
     *  This is useful when the DKF is used for Game Master playback and not during course execution.
     * @throws IOException if there is a problem with resources, i.e. learner action file can't be found or accessed
     * @throws FileValidationException  if there was a problem parsing the learner actions file
     */
    public Resources(generated.dkf.Resources resources, AbstractFolderProxy courseFolder, boolean skipExternalFileLoading) throws IOException, FileValidationException{
        
        actionsManager = new LearnerActionsManager(courseFolder, skipExternalFileLoading);
        
        generated.dkf.AvailableLearnerActions aLearnerActions = resources.getAvailableLearnerActions();
        if(!skipExternalFileLoading){
            actionsManager.addLearnerActionsFiles(aLearnerActions.getLearnerActionsFiles());
        }
        actionsManager.addLearnerActionsList(aLearnerActions.getLearnerActionsList());
        
        if(resources.getSurveyContext() != null){
            this.surveyContextId = resources.getSurveyContext().intValue();
        }
        
        this.scenarioControls = resources.getScenarioControls();
        this.observerControls = resources.getObserverControls();
        
    }
    
    /**
     * Return the list of available learner actions for the current domain
     * 
     * @return LearnerActionsList
     */
    public generated.dkf.LearnerActionsList getLearnerActions(){
        return actionsManager.getLearnerActions();
    }
    
    /**
     * Return the learner actions manager responsible for managing learner actions that are presented in the feedback widget
     * on the TUI.
     * 
     * @return the manager responsible for learner actions
     */
    public LearnerActionsManager getLearnerActionsManager(){
        return actionsManager;
    }
    
    /**
     * Return the unique db id for domain survey context which has survey(s) associated with it
     * 
     * @return Integer - can be null if no survey context was provided
     */
    public Integer getSurveyContextId(){
        return surveyContextId;
    }
    
    /**
     * Return the scenario controls to make available to the tutor. Can return null.
     * 
     * @return the scenario controls to make available to the tutor
     */
    public ScenarioControls getScenarioControls() {
        return scenarioControls;
    }
    
    /**
     * Return the data controlling how a scenario's knowledge session is presented to an observer controller. Can return null.
     * 
     * @return the observer controls. Can be null.
     */
    public ObserverControls getObserverControls() {
        return observerControls;
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[Resources: ");
        sb.append("surveyContextId = ").append(getSurveyContextId());
        sb.append(", actions = ").append(actionsManager);
        
        sb.append(", controls = {");
        
        if(scenarioControls != null) {
            sb.append("ScenarioControls: preventManualStop = ");
            sb.append(scenarioControls.getPreventManualStop() != null);
            
        } else {
            sb.append("null");
        }
        
        sb.append("} , observerControls = {");
        
        if(observerControls != null) {
            
            sb.append("ObserverControls: goodAudio = ");
            sb.append(observerControls.getAudio() != null ? observerControls.getAudio().getGoodPerformance() : null);
            sb.append(", badAudio = ");
            sb.append(observerControls.getAudio() != null ? observerControls.getAudio().getPoorPerformance() : null);
            
        } else {
            sb.append("null");
        }
        
        sb.append("}");
        sb.append("]");
        
        return sb.toString();
    }
}
