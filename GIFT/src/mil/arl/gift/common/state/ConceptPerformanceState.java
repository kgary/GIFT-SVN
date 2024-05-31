/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.state;


/**
 * This class contains the learner state performance state measurements of a concept.
 * 
 * @author mhoffman
 *
 */
@SuppressWarnings("serial")
public class ConceptPerformanceState extends AbstractPerformanceState {
    
    /** 
     * Default constructor needed for GWT RPC serialization
     */
    protected ConceptPerformanceState() {}
    
    /**
     * Class constructor - set attributes
     * 
     * @param state - concept performance state 
     */
    public ConceptPerformanceState(PerformanceStateAttribute state){
        super(state);
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[ConceptPerformanceState: ");
        sb.append(super.toString());
        sb.append("]");
        
        return sb.toString();
    }
}
