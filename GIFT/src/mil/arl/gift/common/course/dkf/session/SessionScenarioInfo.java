/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.dkf.session;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import generated.course.Concepts;
import generated.dkf.Scenario;
import mil.arl.gift.common.course.InteropsInfo.ConditionInfo;
import mil.arl.gift.common.score.GradedScoreNode;

/**
 * Information about a scenario associated with a knowledge session being played back. This
 * can be used by Game Master in order to access information about the scenario that wouldn't 
 * normally be available client-side, such as the performance node structure and 
 * condition display names.
 * 
 * @author nroberts
 */
public class SessionScenarioInfo implements Serializable{

    private static final long serialVersionUID = 1L;
    
    /** The scenario information that was parsed from the DKF associated with the knowledge session */
    private Scenario scenario;
    
    /** A mapping from each condition imlementation name to its associated condtion info */
    private Map<String, ConditionInfo> conditionImplToConditionInfo = new HashMap<>();
    
    /** The most recent published score for the scenario's nodes */
    private GradedScoreNode currentScore;
    
    /** The course concepts for the course associated with the scenario */
    private Concepts.Hierarchy courseConcepts;
    
    /**
     * A default, no-arg constructor required for GWT serialization
     */
    private SessionScenarioInfo() {}
    
    /**
     * Creates a new set of knowledge session scenario information using the given parsed DKF
     * 
     * @param scenario the scenario parsed from the knowledge session's DKF.
     */
    public SessionScenarioInfo(Scenario scenario) {
        this();
        this.scenario = scenario;
    }

    /**
     * Gets the DKF scenario associated with the knowledge session
     * 
     * @return the DKF scenario. Will not be null.
     */
    public Scenario getScenario() {
        return scenario;
    }
    
    /**
     * Adds information about the given condition implementation class
     * 
     * @param conditionImpl the name of the condition implementation class. Cannot be null.
     * @param info the condition info. Cannot be null.
     */
    public void addConditionInfo(String conditionImpl, ConditionInfo info) {
        conditionImplToConditionInfo.put(conditionImpl, info);
    }
    
    /**
     * Gets information about the given condition implementation class
     * 
     * @param conditionImpl the condition implementation class to look for. Will return
     * null if null is passed in.
     * @return the condition info. Can be null if no condition info was loaded for the
     * given condition implementation class.
     */
    public ConditionInfo getConditonInfo(String conditionImpl) {
        return conditionImplToConditionInfo.get(conditionImpl);
    }

    /**
     * Gets the most recent published score for the scenario's nodes
     * 
     * @return the current score. Can be null.
     */
    public GradedScoreNode getCurrentScore() {
        return currentScore;
    }

    /**
     * Sets the most recent published score for the scenario's nodes
     * 
     * @param currentScore the current score. Can be null.
     */
    public void setCurrentScore(GradedScoreNode currentScore) {
        this.currentScore = currentScore;
    }

    /**
     * Gets the course concepts of the course associated with the scenario
     * 
     * @return the course concepts. Can be null if the course had no course concepts.
     */
    public Concepts.Hierarchy getCourseConcepts() {
        return courseConcepts;
    }

    /**
     * Gets the course concepts of the course associated with the scenario
     * 
     * @return the course concepts. Can be null if the course had no course concepts.
     */
    public void setCourseConcepts(Concepts.Hierarchy courseConcepts) {
        this.courseConcepts = courseConcepts;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[SessionScenarioInfo : scenario=");
        builder.append(scenario);
        builder.append(", conditionImplToConditionInfo=");
        builder.append(conditionImplToConditionInfo);
        builder.append(", currentScore=");
        builder.append(currentScore);
        builder.append(", courseConcepts=");
        builder.append(courseConcepts);
        builder.append("]");
        return builder.toString();
    }
    
    
}
