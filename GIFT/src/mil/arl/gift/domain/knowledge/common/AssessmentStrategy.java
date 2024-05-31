/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import mil.arl.gift.common.DomainAssessmentContent;

/**
 * Wrapper around a strategy that contains one or more activities (e.g. feedback) triggered
 * by domain logic that need to be handled.
 * 
 * @author mhoffman
 *
 */
public class AssessmentStrategy implements DomainAssessmentContent {

	/** contains one or more activities (e.g. feedback) to apply */
    private generated.dkf.Strategy strategy;
    
    /**
     * Set attribute(s).
     * 
     * @param strategy contains one or more activities (e.g. feedback) to apply. Can't be null.
     */
    public AssessmentStrategy(generated.dkf.Strategy strategy){
        setStrategy(strategy);
    }

    /**
     * Return the strategy.
     * 
     * @return the strategy that contains one or more activities (e.g. feedback) to apply.  Won't be null.
     */
    public generated.dkf.Strategy getStrategy() {
        return strategy;
    }

    /**
     * Set the strategy.
     * 
     * @param strategy contains one or more activities (e.g. feedback) to apply.  Can't be null.
     */
    private void setStrategy(generated.dkf.Strategy strategy) {
        
        if(strategy == null){
            throw new IllegalArgumentException("The strategy can't be null");
        }
        this.strategy = strategy;
    }
    
    @Override
    public String toString() {
    	
    	StringBuilder sb = new StringBuilder();
    	sb.append("[AssessmentStrategy: ");
    	sb.append("strategy = [");
    	sb.append(" name = ").append(strategy.getName());
    	sb.append(", size = ").append(strategy.getStrategyActivities().size());
    	sb.append("]");
    	sb.append("]");
    	return sb.toString();
    }
}
