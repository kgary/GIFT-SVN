/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy;

/**
 * This class represents a pedagogical request for scenario adaptation.
 * 
 * @author mhoffman
 *
 */
public class RequestBranchAdaptation extends AbstractPedagogicalRequest {

    /** contains pedagogy information about a branch adaptation */
    private BranchAdaptationStrategy strategy;
    
    /**
     * Class constructor - set attributes
     * 
     * @param strategy - the branching strategy information
     */
    public RequestBranchAdaptation(BranchAdaptationStrategy strategy){
        super(BranchAdaptationStrategy.DEFAULT_STRATEGY_NAME);
        
        if(strategy == null){
            throw new IllegalArgumentException("The strategy can't be null");
        }

        this.strategy = strategy;
    }    
    
    /**
     * Return the strategy that contains pedagogy information about a branch adaptation
     * 
     * @return BranchAdaptationStrategy
     */
    public BranchAdaptationStrategy getStrategy(){
        return strategy;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[RequestBranchAdaptation: ");
        sb.append(super.toString());
        sb.append(", strategy = ").append(getStrategy());
        
        sb.append("]");
        
        return sb.toString();
    }
}
