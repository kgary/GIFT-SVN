/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.state;

import java.io.Serializable;

import mil.arl.gift.common.enums.AbstractEnum;

/**
 * This interface is implemented by learner state attribute classes that provide temporal related information
 * on a state attribute value.
 * 
 * @author mhoffman
 *
 */
public interface TemporalStateAttribute extends Serializable{

    /**
     * Return the short term value for the state attribute.
     * 
     * @return AbstractEnum
     */
    public AbstractEnum getShortTerm();
    
    /**
     * Return the time in milliseconds that the short term state value was set.
     * 
     * @return long time in milliseconds (since epoch)
     */
    public long getShortTermTimestamp();
    
    /**
     * Return the long term value for the state attribute.
     * 
     * @return AbstractEnum
     */
    public AbstractEnum getLongTerm();
    
    /**
     * Return the time in milliseconds that the long term state value was set.
     * 
     * @return long time in milliseconds (since epoch)
     */
    public long getLongTermTimestamp();
    
    /**
     * Return the predicted term value for the state attribute.
     * 
     * @return AbstractEnum
     */
    public AbstractEnum getPredicted();
    
    /**
     * Return the time in milliseconds that the predicted state value was set.
     * 
     * @return long time in milliseconds (since epoch)
     */
    public long getPredictedTimestamp();
}
