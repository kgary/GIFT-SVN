/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.dkf.strategy;

import mil.arl.gift.common.course.strategy.AbstractStrategy;

/**
 * This is the base class for strategy implementations for DKFs.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractDKFStrategy extends AbstractStrategy {
    
    /** strategy handler information */
    private generated.dkf.StrategyHandler handler;
    
    /**
     * Class constructor 
     * 
     * @param name - the unique name of the strategy
     * @param handler - the strategy handler for this strategy
     */
    public AbstractDKFStrategy(String name, generated.dkf.StrategyHandler handler){
        super(name);
        setHandler(handler);
    }
    
    private void setHandler(generated.dkf.StrategyHandler handler){
        
        if(handler == null){
            throw new IllegalArgumentException("The strategy handler can't be null");
        }

        this.handler = handler;
    }    
    
    /**
     * Return the strategy handler information for this strategy.
     * 
     * @return generated.dkf.StrategyHandler
     */
    public generated.dkf.StrategyHandler getHandlerInfo(){
        return handler;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        sb.append(", handler = ").append(getName());
        
        return sb.toString();
    }
}
