/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.state;

import java.io.Serializable;
import java.util.Objects;

import mil.arl.gift.common.util.StringUtils;


/**
 * This is the base class for performance state classes.
 * 
 * @author mhoffman
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractPerformanceState implements Serializable{

    /** the state of the performance node */
    protected PerformanceStateAttribute state;

    /**
     * Flag indicating if the performance state contains a child or descendent condition that requires manual
     * observation.
     */
    private boolean containsObservedAssessment = false;
    
    /** 
     * Default constructor needed for GWT RPC serialization
     */
    protected AbstractPerformanceState() {}
    
    /**
     * Class constructor - set attribute(s)
     * 
     * @param state - the state of the performance node
     */
    public AbstractPerformanceState(PerformanceStateAttribute state){
        this();
        
        this.state = state;
    }
    
    /**
     * Return the performance state of the performance node
     * 
     * @return PerformanceStateAttribute
     */ 
    public PerformanceStateAttribute getState() {
        return state;
    }

    /**
     * Update the performance state
     * 
     * @param newState - a new state attribute for this performance state
     */
    public void updateState(PerformanceStateAttribute newState) {
        this.state = newState;
    }

    /**
     * Retrieve the flag indicating if the performance state contains a child or descendent condition that requires manual
     * observation
     * 
     * @return true if the performance state is for a node that contains a child or descendent condition that requires manual
     * observation; false otherwise.
     */
    public boolean isContainsObservedAssessmentCondition() {
        return containsObservedAssessment;
    }

    /**
     * Set the flag indicating if the performance state contains a child or descendent condition that requires manual
     * observation
     * 
     * @param containsObservedAssessment true if the performance state contains a child or descendent 
     * condition that requires manual observation; false otherwise.
     */
    public void setContainsObservedAssessmentCondition(boolean containsObservedAssessment) {
        this.containsObservedAssessment = containsObservedAssessment;
    }    
    
    @Override
    public int hashCode() {
        return Objects.hash(containsObservedAssessment, state);
    }

    @Override
    public boolean equals(Object otherState) {
        if (state == null) {
            return false;
        }else if(!(otherState instanceof AbstractPerformanceState)) {
            return false;
        }
        
        AbstractPerformanceState state = (AbstractPerformanceState)otherState;

        if (this.getState() == null) {
            if (state.getState() != null) {
                return false;
            }
        } else if (!this.getState().equals(state.getState())) {
            return false;
        }

        if (this.isContainsObservedAssessmentCondition() != state.isContainsObservedAssessmentCondition()) {
            return false;
        }

        return true;
    }  
    
    /**
     * Return the time stamp (epoch) at which the observer started to give an observation (e.g. comment, assessment).
     * The evaluator value must be provided in order to know if this assessment was provided by an observer.
     * @param the time at which some observation that is included in this assessment was started
     * by the observer.  Will be null if not set at this state level.
     */
    public Long getObservationStartedTime(){
        
        if(StringUtils.isNotBlank(getState().getEvaluator())){
            return getState().getShortTermTimestamp();
        }
        
        return null;

    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append(" state = ").append(getState());        
        return sb.toString();
    }
}
