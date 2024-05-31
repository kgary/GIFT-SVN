/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import java.util.List;
import java.util.UUID;

import mil.arl.gift.common.ConceptAssessment;
import mil.arl.gift.common.enums.AssessmentLevelEnum;

/**
 *  A intermediate concept assessment is an assessment of a concept that allows for infinite 
 *  nesting of concept assessments, therefore allowing the performance node tree to be as complex as the DKF author wishes. 
 *  This particular instance contains only the unique ids of the child subconcepts associated with this
 *  intermediate concept node.   
 *  
 * @author mhoffman
 *
 */
public class ProxyIntermediateConceptAssessment extends ConceptAssessment {

    /** list of concept assessments associated (i.e. children) of this intermediate concept */
    private List<UUID> concepts;
    
    /**
     * Class constructor - set this concepts assessment attributes and identify its subconcepts (child concepts)
     * 
     * @param name - name of the concept
     * @param assessment - assessment of the concept
     * @param time - time stamp at which the assessment was last calculated 
     * @param nodeId - unique performance node id that maps to this assessment 
     * @param concepts - subconcepts of this intermediate concept
     * @param courseId - course level unique id for this assessment node
     */
    public ProxyIntermediateConceptAssessment(String name,
            AssessmentLevelEnum assessment, long time, int nodeId, List<UUID> concepts, UUID courseId) {
        super(name, assessment, time, nodeId, courseId);
        
        setConcepts(concepts);
    }
    
    /**
     * Set the sub-concepts of this intermediate concept.
     * 
     * @param concepts
     */
    private void setConcepts(List<UUID> concepts){
        
        if(concepts == null || concepts.isEmpty()){
            throw new IllegalArgumentException("There must be at least one child concept under an intermediate concept");
        }
        
        this.concepts = concepts;
    }
    
    /**
     * Get the sub-concepts of this intermediate concept.
     * 
     * @return List<UUID>
     */
    public List<UUID> getConceptIds(){
        return concepts;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[IntermediateConceptAssessment:");
        sb.append(super.toString());
        sb.append(", subconcepts = {");
        for(UUID conceptId : getConceptIds()){
            sb.append(" ").append(conceptId).append(", ");
        }
        sb.append("}");
        sb.append("]");
        
        return sb.toString();
    }
}
