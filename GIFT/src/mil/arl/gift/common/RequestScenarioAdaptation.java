/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

/**
 * This class represents a pedagogical request for scenario adaptation.
 * 
 * @author mhoffman
 *
 */
public class RequestScenarioAdaptation extends AbstractPedagogicalRequest {
    
    /**
     * Class constructor
     * 
     * @param strategyName - the unique scenario adaptation strategy name
     */
    public RequestScenarioAdaptation(String strategyName) {
        super(strategyName);
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[RequestScenarioAdaptation: ");
        sb.append(super.toString());
        sb.append("]");
        
        return sb.toString();
    }
}
