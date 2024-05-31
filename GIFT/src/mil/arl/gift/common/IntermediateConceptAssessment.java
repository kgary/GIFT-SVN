/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.util.List;
import java.util.UUID;

import mil.arl.gift.common.enums.AssessmentLevelEnum;

/**
 *  A intermediate concept assessment is an assessment of a concept that allows for infinite nesting of concept assessments, therefore allowing the performance node
 *  tree to be as complex as the DKF author wishes.  
 *  
 * @author mhoffman
 *
 */
public class IntermediateConceptAssessment extends ConceptAssessment {

    /** list of concept assessments associated (i.e. children) of this intermediate concept */
    private List<ConceptAssessment> conceptAssessments;
    
    /**
     * Class constructor - set this concepts assessment attributes and identify its subconcepts (child concepts)
     * 
     * @param name - name of the concept
     * @param assessment - assessment of the concept
     * @param time - time stamp at which the assessment was last calculated 
     * @param nodeId - unique performance node id that maps to this assessment 
     * @param conceptAssessments - subconcepts of this intermediate concept
     * @param courseId - the course level unique id for this assessment node
     */
    public IntermediateConceptAssessment(String name,
            AssessmentLevelEnum assessment, long time, int nodeId, List<ConceptAssessment> conceptAssessments, UUID courseId) {
        super(name, assessment, time, nodeId, courseId);
        
        setConcepts(conceptAssessments);
    }
    
    /**
     * Set the sub-concepts of this intermediate concept.
     * 
     * @param conceptAssessments - subconcepts of this intermediate concept
     */
    private void setConcepts(List<ConceptAssessment> conceptAssessments){
        
        if(conceptAssessments == null || conceptAssessments.isEmpty()){
            throw new IllegalArgumentException("There must be at least one child concept under an intermediate concept");
        }
        
        this.conceptAssessments = conceptAssessments;
    }
    
    /**
     * Get the sub-concepts of this intermediate concept.
     * 
     * @return the concepts under this intermediate concept
     */
    public List<ConceptAssessment> getConceptAssessments(){
        return conceptAssessments;
    }
    
    @Override
    public boolean equals(Object otherIntermediateConceptAssessment){
        
        if(otherIntermediateConceptAssessment == null){
            return false;
        }else if(!super.equals(otherIntermediateConceptAssessment)){
            return false;
        }else if(!(otherIntermediateConceptAssessment instanceof IntermediateConceptAssessment)){
            return false;
        }
            
        //check concepts
        List<ConceptAssessment> thatConcepts = ((IntermediateConceptAssessment)otherIntermediateConceptAssessment).getConceptAssessments();
        List<ConceptAssessment> thisConcepts = this.getConceptAssessments();        
        
        return thisConcepts.equals(thatConcepts);
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[IntermediateConceptAssessment:");
        sb.append(super.toString());
        sb.append(", subconcepts = {");
        for(ConceptAssessment conceptAssessment : getConceptAssessments()){
            sb.append(" ").append(conceptAssessment).append(", ");
        }
        sb.append("}");
        sb.append("]");
        
        return sb.toString();
    }
}
