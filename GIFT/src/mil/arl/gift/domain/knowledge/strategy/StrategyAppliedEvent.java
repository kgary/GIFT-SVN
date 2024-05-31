/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/              
package mil.arl.gift.domain.knowledge.strategy;

import java.util.Set;

import mil.arl.gift.common.util.StringUtils;

/**
 * Contains information about a strategy or strategy set that was applied.
 * @author mhoffman
 *
 */
public class StrategyAppliedEvent {

    /** the unique name of a strategy. */
    private String strategyName;
    
    /** optional stress value associated with the strategy being applied */
    private Double strategyAppliedStress;
    
    /** optional difficulty value associated with the strategy being applied */
    private Double strategyAppliedDifficulty;
    
    /** optional collection of task course level ids this strategy applies too. 
     * Values should be what is found in {@link mil.arl.gift.common.state.AbstractPerformanceStateAttribute.nodeCourseId}. */
    private Set<String> tasksAppliedToo;
    
    /**
     * Set strategy name
     * @param strategyName The unique name of the strategy. Can't be null or empty.
     */
    public StrategyAppliedEvent(String strategyName) {
        setStrategyName(strategyName);        
    }

    /**
     * Get the strategy name.
     * @return  The unique name of the strategy. Can't be null or empty.
     */
    public String getStrategyName() {
        return strategyName;
    }

    /**
     * Set the strategy name.
     * @param strategyName  The unique name of the strategy. Can't be null or empty.
     */
    private void setStrategyName(String strategyName) {
        if(StringUtils.isBlank(strategyName)) {
            throw new IllegalArgumentException("The strategy name is null or blank");
        }
        this.strategyName = strategyName;
    }

    /**
     * Return the optional value of stress associated with this strategy.
     * @return optional value of stress associated with this strategy.  Can be null.
     * See ProxyTaskAssessment for ranges on stress value.
     */
    public Double getStrategyAppliedStress() {
        return strategyAppliedStress;
    }

    /**
     * Set the optional value of stress associated with this strategy.
     * @param strategyAppliedStress optional value of stress associated with this strategy.  Can be null.
     * See ProxyTaskAssessment for ranges on stress value.
     */
    public void setStrategyAppliedStress(Double strategyAppliedStress) {
        this.strategyAppliedStress = strategyAppliedStress;
    }

    /**
     * Return the optional difficulty value associated with the strategy being applied
     * @return can be null.
     */
    public Double getStrategyAppliedDifficulty() {
        return strategyAppliedDifficulty;
    }

    /**
     * Set the optional difficulty value associated with the strategy being applied
     * @param strategyAppliedDifficulty can be null
     */
    public void setStrategyAppliedDifficulty(Double strategyAppliedDifficulty) {
        this.strategyAppliedDifficulty = strategyAppliedDifficulty;
    }

    /**
     * Return the course level unique id of the tasks the strategy applies too.  This is useful
     * for knowing which tasks the strategy attributes (e.g. stress) should be applied too.
     * If not provided the strategy attributes will be applied to all active tasks.
     * @return can be null or empty.  Values should be what is found in {@link mil.arl.gift.common.state.AbstractPerformanceStateAttribute.nodeCourseId}.
     */
    public Set<String> getTasksAppliedToo() {
        return tasksAppliedToo;
    }

    /**
     * Set the course level unique id of the tasks the strategy applies too.  This is useful
     * for knowing which tasks the strategy attributes (e.g. stress) should be applied too.
     * If not provided the strategy attributes will be applied to all active tasks.
     * 
     * @param tasksAppliedToo can be null or empty.  Values should be what is found in {@link mil.arl.gift.common.state.AbstractPerformanceStateAttribute.nodeCourseId}.
     */
    public void setTasksAppliedToo(Set<String> tasksAppliedToo) {
        this.tasksAppliedToo = tasksAppliedToo;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[StrategyAppliedEvent: strategyName=");
        builder.append(strategyName);
        builder.append(", strategyAppliedStress=");
        builder.append(strategyAppliedStress);
        builder.append(", strategyAppliedDifficulty=");
        builder.append(strategyAppliedDifficulty);
        builder.append(", tasksAppliedToo=");
        builder.append(tasksAppliedToo);
        builder.append("]");
        return builder.toString();
    }
    
    
}
