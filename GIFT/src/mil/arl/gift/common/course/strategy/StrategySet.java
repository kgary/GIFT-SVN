/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.strategy;

import java.util.List;

/**
 * Wrapper around one or more strategies.
 * 
 * @author mhoffman
 *
 */
public class StrategySet {

    /** zero or more strategies */
    private List<AbstractStrategy> strategies;
    
    /** optional stress value associated with this set of strategies */
    private Double stress;
    
    /** optional difficulty value associated with this set of strategies */
    private Double difficulty;
    
    /**
     * Set strategies
     * @param strategies zero or more strategies, can't be null.
     */
    public StrategySet(List<AbstractStrategy> strategies) {
        setStrategies(strategies);
    }

    /**
     * Return the strategies for this strategy set
     * @return zero or more strategies
     */
    public List<AbstractStrategy> getStrategies() {
        return strategies;
    }

    /**
     * Set the strategies for this strategy set
     * @param strategies zero or more strategies
     */
    private void setStrategies(List<AbstractStrategy> strategies) {
        
        if(strategies == null) {
            throw new IllegalArgumentException("The strategies is null");
        }
        this.strategies = strategies;
    }

    /**
     * Return the stress value for this strategy set
     * @return can be null if not set.  Check TaskAssessment for value range.
     */
    public Double getStress() {
        return stress;
    }

    /**
     * Set the stress value for this strategy set
     * @param stress can be null.  Check TaskAssessment for value range.
     */
    public void setStress(Double stress) {
        this.stress = stress;
    }
    
    /**
     * Return the difficulty value for this strategy set
     * @return can be null if not set.  Check TaskAssessment for value range.
     */
    public Double getDifficulty() {
        return stress;
    }

    /**
     * Set the difficulty value for this strategy set
     * @param difficulty can be null.  Check TaskAssessment for value range.
     */
    public void setDifficulty(Double difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[StrategySet: ");
        builder.append("stress=");
        builder.append(stress);
        builder.append(", difficulty=");
        builder.append(difficulty);
        builder.append(", strategies=\n");
        builder.append(strategies);
        builder.append("]");
        return builder.toString();
    }
    
    
}
