/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.util.Set;

import mil.arl.gift.common.util.StringUtils;

/**
 * This is the base class for pedagogical request classes.
 *
 * @author mhoffman
 *
 */
public abstract class AbstractPedagogicalRequest {

    public static final String PATH_DELIMETER = "#";
    protected static final String UNKNOWN_PERFORMANCE_NODE = "unknown";

    /** the domain action knowledge strategy name */
    private String strategyName;

    /** whether this strategy is a macro strategy or a micro strategy */
    private boolean macroRequest = false;

    /** amount of time (seconds) to wait after executing the strategy */
    private float delayAfterStrategy = 0.0f;
    
    /** the reason why this pedagogical request is being made. */
    private String reasonForRequest = null;
    
    /** optional collection of DKF XML node (Task/concept) ids that caused this strategy to be requested. */
    private Set<Integer> nodeIds;

    /**
     * Class constructor - set attribute(s).
     *
     * @param strategyName - unique name of this strategy
     */
    public AbstractPedagogicalRequest(String strategyName){
        setStrategyName(strategyName);
    }

    /**
     * Setter for the name of the strategy that is being requested for
     * execution.
     *
     * @param strategyName The new strategy name that is being requested. Can't
     *        be null or empty.
     */
    private void setStrategyName(String strategyName){

        if (StringUtils.isBlank(strategyName)) {
            throw new IllegalArgumentException("The strategy name can't be null or empty.");
        }

        this.strategyName = strategyName;
    }

    /**
     * Return the domain action knowledge strategy name
     *
     * @return String
     */
    public String getStrategyName(){
        return strategyName;
    }
    
    /**
     * Return the optional collection of DKF XML node (Task/concept) ids that caused this strategy to be requested.
     * @return can be null or empty.  
     */
    public Set<Integer> getTaskConceptsAppliedToo() {
        return nodeIds;
    }

    /**
     * Set the optional collection of DKF XML node (Task/concept) ids that caused this strategy to be requested.
     * 
     * @param nodeIds can be null or empty.
     */
    public void setTaskConceptsAppliedToo(Set<Integer> nodeIds) {
        this.nodeIds = nodeIds;
    }

    /**
     * Set whether this request is a macro strategy (versus a micro strategy).
     *
     * @param value - is a macro strategy request
     */
    public void setIsMacroRequest(boolean value){
        this.macroRequest = value;
    }

    /**
     * Return whether this request is a macro strategy (versus a micro strategy).
     *
     * @return boolean
     */
    public boolean isMacroRequest(){
        return macroRequest;
    }

    /**
     * Return whether this request is a micro strategy (versus a macro strategy).
     *
     * @return boolean
     */
    public boolean isMicroRequest(){
        return !macroRequest;
    }

    /**
     * Return the amount of time (seconds) to wait after executing the strategy
     *
     * @return time in seconds, default is 0.0
     */
    public float getDelayAfterStrategy() {
        return delayAfterStrategy;
    }

    /**
     * Set the amount of time (seconds) to wait after executing the strategy.
     *
     * @param delayAfterStrategy will not be applied if less than zero
     */
    public void setDelayAfterStrategy(float delayAfterStrategy) {

        if(delayAfterStrategy < 0){
            return;
        }

        this.delayAfterStrategy = delayAfterStrategy;
    }

    /**
     * Return the reason why this pedagogical request is being made.
     * 
     * @return the reason for this request.  Can be null or empty.
     */
    public String getReasonForRequest() {
        return reasonForRequest;
    }

    /**
     * Set the reason why this pedagogical request is being made.
     * 
     * @param reasonForRequest the reason for this request.  Can be null or empty.
     */
    public void setReasonForRequest(String reasonForRequest) {
        this.reasonForRequest = reasonForRequest;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("strategy = ").append(strategyName);
        sb.append(", reason = ").append(reasonForRequest);
        sb.append(", taskConceptsAppliedToo = ");
        sb.append(nodeIds);
        sb.append(", isMacroRequest = ").append(macroRequest);
        sb.append(", delay = ").append(getDelayAfterStrategy());

        return sb.toString();
    }
}
