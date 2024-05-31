/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.strategy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import generated.dkf.InstructionalIntervention;
import generated.dkf.MidLessonMedia;
import generated.dkf.PerformanceAssessment;
import generated.dkf.ScenarioAdaptation;
import generated.dkf.Strategy;
import mil.arl.gift.common.course.dkf.strategy.PerformanceAssessmentStrategy;
import mil.arl.gift.common.score.TaskScoreNode;
import mil.arl.gift.common.util.StringUtils;

/**
 * This is the base class for strategy classes.  It contains the name of the strategy.
 *
 * @author mhoffman
 *
 */
public abstract class AbstractStrategy {
    
    /** the stress value for this strategy, optional or between {@link #MIN_STRESS} and {@link #MAX_STRESS} */
    private Double stress = null;

    /** the strategy name */
    private String name;

    /** amount of time (seconds) to wait before executing the strategy */
    private float delayAfterStrategy = 0.0f;

    /**
     * Class constructor
     *
     * @param name - the unique name of the strategy
     */
    public AbstractStrategy(String name){
        this.name = name;
    }

    /**
     * Return the strategy name
     *
     * @return String
     */
    public String getName(){
        return name;
    }    
    
    /**
     * Return the stress value for this strategy.  The value is normally set in the DKF.
     * @return null if not set, or a value between {@link #TaskScoreNode.MIN_STRESS} and {@link #TaskScoreNode.MAX_STRESS}.
     */
    public Double getStress() {
        return stress;
    }

    /**
     * Set the stress value for this strategy.  The value is normally set in the DKF.
     * 
     * @param stress can be null to indicate the value is not set, otherwise a value between {@link #TaskScoreNode.MIN_STRESS} 
     * and {@link #MAX_STRESS} is used.  If outside of those bounds the value will be changed to {@link #TaskScoreNode.MIN_STRESS} 
     * or {@link #MAX_STRESS};
     */
    public void setStress(Double stress) {
        
        if(stress != null) {
            if(stress < TaskScoreNode.MIN_STRESS) {
                stress = TaskScoreNode.MIN_STRESS;
            }else if(stress > TaskScoreNode.MAX_STRESS) {
                stress = TaskScoreNode.MAX_STRESS;
            }
        }
        this.stress = stress;
    }

    /**
     * Return the amount of time (seconds) to wait after executing the strategy
     *
     * @return time in seconds, default is 0.0
     */
    public float getDelayAfterStrategy() {
        return delayAfterStrategy;
    }

    /**
     * Set the amount of time (seconds) to wait before executing the next
     * strategy.
     *
     * @param delayAfterStrategy will not be applied if less than zero
     */
    public void setDelayAfterStrategy(float delayAfterStrategy) {

        if (delayAfterStrategy < 0) {
            return;
        }

        this.delayAfterStrategy = delayAfterStrategy;
    }    

    /**
     * Generates a list of activities from a DKF strategy.
     *
     * @param strategy The {@link Strategy} from which to generate the list of
     *        activities.
     * @return The List of activities that were generated from the passed
     *         {@link Strategy}.
     */
    public static List<AbstractStrategy> createActivitiesFrom(Strategy strategy) {
        List<AbstractStrategy> toRet = new ArrayList<>();

        String strategyName = strategy.getName();
        for (Serializable activity : strategy.getStrategyActivities()) {
            AbstractStrategy commonStrategy = createActivityFrom(strategyName, activity);
            
            if(strategy.getStress() != null) {
                commonStrategy.setStress(strategy.getStress().doubleValue());
            }
            toRet.add(commonStrategy);
        }

        return toRet;
    }

    /**
     * Converts a JAXB {@link Strategy} activity into an
     * {@link AbstractStrategy}.
     *
     * @param strategyName The name of the {@link Strategy} to which to the
     *        activity belongs. Can't be null or empty.
     * @param activity The activity to convert. Can't be null.
     * @return The resulting {@link AbstractStrategy}. Can't be null.
     */
    public static AbstractStrategy createActivityFrom(String strategyName, Serializable activity) {
        if (StringUtils.isBlank(strategyName)) {
            throw new IllegalArgumentException("The parameter 'strategyName' cannot be null or empty.");
        } else if (activity == null) {
            throw new IllegalArgumentException("The parameter 'activity' cannot be null.");
        }

        if (activity instanceof generated.dkf.InstructionalIntervention) {
            InstructionalIntervention instructionalIntervention = (InstructionalIntervention) activity;
            return new InstructionalInterventionStrategy(strategyName, instructionalIntervention);
        } else if (activity instanceof generated.dkf.MidLessonMedia) {
            return new MidLessonMediaStrategy(strategyName, (MidLessonMedia) activity);
        } else if (activity instanceof generated.dkf.ScenarioAdaptation) {
            return new ScenarioAdaptationStrategy(strategyName, (ScenarioAdaptation) activity);
        } else if (activity instanceof generated.dkf.PerformanceAssessment) {
            return new PerformanceAssessmentStrategy(strategyName, (PerformanceAssessment) activity);
        } else {
            throw new IllegalArgumentException("Found unhandled strategy activity of '" + activity
                    + "' when building domain action knowledge.  An unhandled strategy means nothing will happen if that strategy is selected and that isn't exceptable.");
        }
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append(", name = ").append(getName());
        sb.append(", delay = ").append(getDelayAfterStrategy());
        sb.append(", stress = ").append(getStress());

        return sb.toString();
    }
}
