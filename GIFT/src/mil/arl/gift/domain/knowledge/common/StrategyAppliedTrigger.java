/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import mil.arl.gift.common.util.StringUtils;

/**
 * This strategy will activate anytime a strategy is given that has the matching name of the strategy
 * being tracked by this trigger instance.
 * @author mhoffman
 *
 */
public class StrategyAppliedTrigger extends AbstractTrigger {
    
    /** information about the strategy being tracked by this trigger */
    private generated.dkf.StrategyApplied strategyApplied;

    /**
     * Set attributes for this trigger
     * @param triggerName the name of this trigger, used for display purposes.  Can't be null or empty.
     * @param strategyApplied information about the strategy being tracked by this trigger.  Can't be null.
     */
    public StrategyAppliedTrigger(String triggerName, generated.dkf.StrategyApplied strategyApplied){
        super(triggerName);
        
        if(strategyApplied == null){
            throw new IllegalArgumentException("The strategy applied object can't be null");
        }
        
        this.strategyApplied = strategyApplied;
    }
    
    @Override
    public boolean shouldActivate(String appliedStratergyName){        
        return StringUtils.equalsIgnoreCase(appliedStratergyName, strategyApplied.getStrategyName());
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[StrategyAppliedTrigger: ");
        sb.append(super.toString());
        sb.append(", strategy = ").append(strategyApplied.getStrategyName());
        sb.append("]");
        
        return sb.toString();
    }
}
