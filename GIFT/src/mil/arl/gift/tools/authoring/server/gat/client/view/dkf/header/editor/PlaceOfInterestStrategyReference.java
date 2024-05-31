/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor;

import generated.dkf.Strategy;

/**
 * A wrapper for a strategy that references a place of interest
 * @author mhoffman
 *
 */
public class PlaceOfInterestStrategyReference extends PlaceOfInterestReference {

    /** a strategy that references a place of interest */
    private Strategy strategy;
    
    /**
     * Creates a wrapper around the given strategy reference
     * @param strategy a strategy that references a place of interest. Can't be null.
     */
    public PlaceOfInterestStrategyReference(Strategy strategy){
        super();
        
        if(strategy == null){
            throw new IllegalArgumentException("The strategy can't be null");
        }
        this.strategy = strategy;
    }
    
    /**
     * Return the strategy that references a place of interest
     * @return wont be null
     */
    public Strategy getStrategy(){
        return strategy;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[PlaceOfInterestStrategyReference: ");
        builder.append("count = ").append(getReferenceCount());
        builder.append(", strategy = ");
        builder.append(strategy);
        builder.append("]");
        return builder.toString();
    }
    
    
}
