/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

/**
 * This class represents a pedagogical request for do'ing nothing
 * 
 * @author mhoffman
 *
 */
public class RequestDoNothingTactic extends AbstractPedagogicalRequest {
    
    /**
     * Class constructor
     * 
     * @param strategyName - unique name of the 'do nothing' strategy
     */
    public RequestDoNothingTactic(String strategyName) {
        super(strategyName);
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[RequestDoNothingTactic: ");
        sb.append(super.toString());
        sb.append("]");
        
        return sb.toString();
    }
}
