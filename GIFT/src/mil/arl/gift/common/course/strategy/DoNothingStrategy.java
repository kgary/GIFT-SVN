/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.strategy;

import generated.dkf.DoNothingInstStrategy;

import mil.arl.gift.common.course.dkf.strategy.AbstractDKFStrategy;

/**
 * This class contains information on a Do Nothing strategy.
 * 
 * @author mhoffman
 *
 */
public class DoNothingStrategy extends AbstractDKFStrategy {
    
    /**
     * Class constructor - set attributes
     * 
     * @param name unique name of a strategy
     * @param doNothingStrategy - dkf.xsd generated class instance
     */
    public DoNothingStrategy(String name, DoNothingInstStrategy doNothingStrategy){
        super(name, doNothingStrategy.getStrategyHandler());
        
        if(doNothingStrategy.getDelayAfterStrategy() != null && doNothingStrategy.getDelayAfterStrategy().getDuration() != null){
            this.setDelayAfterStrategy(doNothingStrategy.getDelayAfterStrategy().getDuration().floatValue());
        }
    }

    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[DoNothingStrategy: ");
        sb.append(super.toString());
        sb.append("]");
        
        return sb.toString();
    }
}
