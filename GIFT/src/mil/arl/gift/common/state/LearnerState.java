/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.state;

import java.io.Serializable;

/**
 * The learner state contains the current and next (i.e. trend) learner state
 * 
 * @author mhoffman
 *
 */
@SuppressWarnings("serial")
public class LearnerState implements Serializable{

    /** the performance state */
    private PerformanceState performance;
    
    /** the cognitive state */
    private CognitiveState cognitive;
    
    /** the affective state */
    private AffectiveState affective;
	
    /** 
     * Default constructor needed for GWT RPC serialization
     */
    private LearnerState() {}
    
	/**
	 * Class constructor - set attributes
	 * 
	 * @param performance - the performance state attributes, can't be null
	 * @param cognitive - the cognitive state attributes, can't be null
	 * @param affective - the affective state attributes, can't be null
	 */
	public LearnerState(PerformanceState performance, CognitiveState cognitive, AffectiveState affective){
	    this();
	    
	    if(performance == null){
	        throw new IllegalArgumentException("The performance state is null");
	    }else if(cognitive == null){
            throw new IllegalArgumentException("The cognitive state is null");
        }else if(affective == null){
            throw new IllegalArgumentException("The affective state is null");
        }
	    
        this.performance = performance;
        this.cognitive = cognitive;
        this.affective = affective;
	}
	
	/**
	 * Return the performance state 
	 * @return won't be null
	 */
    public PerformanceState getPerformance() {
        return performance;
    }

    /**
     * Return the cognitive state 
     * @return won't be null
     */
    public CognitiveState getCognitive() {
        return cognitive;
    }

    /**
     * Return the affective state 
     * @return won't be null
     */
    public AffectiveState getAffective() {
        return affective;
    }
    
    /**
     * Return whether this learner state contains no real state information, i.e. it is empty.
     * @return true if this learner state is currently blank.
     */
    public boolean isEmpty(){
        return (performance == null || performance.isEmpty()) && (cognitive == null || cognitive.isEmpty()) && (affective == null || affective.isEmpty());
    }

    @Override
	public String toString(){
		
	    StringBuffer sb = new StringBuffer();
	    sb.append("[LearnerState:");
	    sb.append("\nperformance = ").append(getPerformance());
	    sb.append(",\ncognitive = ").append(getCognitive());
	    sb.append(",\naffective = ").append(getAffective());
	    sb.append("]");
		
		return sb.toString();
	}
}
