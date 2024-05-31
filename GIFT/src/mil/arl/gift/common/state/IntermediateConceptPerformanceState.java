/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.state;

import java.util.List;
import java.util.Objects;

/**
 * This class contains the learner state performance state measurements for an intermediate concept.
 * 
 * @author mhoffman
 *
 */
@SuppressWarnings("serial")
public class IntermediateConceptPerformanceState extends
        ConceptPerformanceState {
    
    /** the performance state(s) of concepts associated with this task */
    private List<ConceptPerformanceState> concepts;
    
    /** 
     * Default constructor needed for GWT RPC serialization
     */
    @SuppressWarnings("unused")
    private IntermediateConceptPerformanceState() {}

    /**
     * Class constructor - set attributes
     * 
     * @param state - performance statue for an intermediate concept
     * @param concepts - collection of performance states for the subconcepts of this intermediate concept
     */
    public IntermediateConceptPerformanceState(PerformanceStateAttribute state, List<ConceptPerformanceState> concepts) {
        super(state);
        
        this.concepts = concepts;
    }
    
    /**
     * Return the concept performance states for the task
     * 
     * @return the concepts under this intermediate concept
     */
    public List<ConceptPerformanceState> getConcepts() {
        return concepts;
    } 
    
    @Override    
    public Long getObservationStartedTime(){
        
        Long obsStartTime = super.getObservationStartedTime();
        if(obsStartTime == null){

            // check descendants
            for(ConceptPerformanceState conceptPerfState : concepts){
                
                obsStartTime = conceptPerfState.getObservationStartedTime();
                if(obsStartTime != null){
                    break;
                }
            }
        }
        
        return obsStartTime;
    }   
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(concepts);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        IntermediateConceptPerformanceState other = (IntermediateConceptPerformanceState) obj;
        return Objects.equals(concepts, other.concepts);
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[IntermediateConceptPerformanceState: ");
        sb.append(super.toString());
        
        sb.append(", subconcepts = {");
        for(ConceptPerformanceState state : concepts){
            sb.append(state).append(", ");
        }
        sb.append("}");

        sb.append("]");
        
        return sb.toString();
    }

}
