/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.util.UUID;

import mil.arl.gift.common.enums.AssessmentLevelEnum;

/**
 * This class contains information about an assessment of a domain's concept.
 * 
 * @author mhoffman
 *
 */
public class ConceptAssessment extends AbstractAssessment {
	
    /**
     * Class constructor - set attributes
     * @param name - name of the concept
     * @param assessment  - assessment of the concept
     * @param time - time stamp at which the assessment was last calculated 
     * @param nodeId - dkf level unique performance node id that maps to this assessment 
     * @param courseUUID - course level unique id for this assessment node
     */
    public ConceptAssessment(String name, AssessmentLevelEnum assessment, long time, int nodeId, UUID courseUUID){
        super(name, assessment, time, nodeId, courseUUID);
        
    }
    
    @Override
    public boolean equals(Object otherConceptAssessment){
        
        if(otherConceptAssessment == null){
            return false;
        } else if(!super.equals(otherConceptAssessment)){
            return false;
        }else if(!(otherConceptAssessment instanceof ConceptAssessment)){
            return false;
        }
        
        return true;
    }
        
    @Override
    public String toString(){
    	
        StringBuffer sb = new StringBuffer();
        sb.append("[ConceptAssessment: ");
        sb.append(super.toString());
        sb.append("]");
    	
    	return sb.toString();
    }
}
