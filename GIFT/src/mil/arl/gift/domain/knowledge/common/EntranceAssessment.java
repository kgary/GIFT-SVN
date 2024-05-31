/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import mil.arl.gift.common.enums.AssessmentLevelEnum;

/**
 * Contains the assessment to give if a sequential segment is traversed
 * in order.
 * 
 * @author mhoffman
 *
 */
public class EntranceAssessment {

    /** the sequential segment to traverse */
    private SequentialSegment sSegment;
    
    /** the assessment to give  when the entrance segment is crossed */
    private AssessmentLevelEnum assessment;
    
    /**
     * Class constructor
     * 
     * @param sSegment the sequential segment to traverse
     * @param assessment the assessment to give when the entrance segment is crossed
     */
    public EntranceAssessment(SequentialSegment sSegment, AssessmentLevelEnum assessment){
        
        if(sSegment == null){
            throw new IllegalArgumentException("The segment can't be null.");
        }
        this.sSegment = sSegment;
        
        if(assessment == null){
            throw new IllegalArgumentException("The assessment can't be null.");
        }
        this.assessment = assessment;
    }
    
    /**
     * Class constructor - set attributes from dkf content
     * 
     * @param entrance - dkf content for an entrance
     * @param placesOfInterestManager - used to organize places of interest which can be referenced by 
     * name in various parts of the DKF
     */
    public EntranceAssessment(generated.dkf.Entrance entrance, PlacesOfInterestManager placesOfInterestManager){
        
        this.assessment = AssessmentLevelEnum.valueOf(entrance.getAssessment());
        
        this.sSegment = new SequentialSegment(entrance.getName(), entrance.getInside(), entrance.getOutside(), placesOfInterestManager);
    }
    
    public SequentialSegment getSequentialSegment(){
        return sSegment;
    }
    
    public AssessmentLevelEnum getAssessment(){
        return assessment;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[SurveyReplyAssessment: ");
        sb.append("segment = ").append(getSequentialSegment()); 
        sb.append(", assessment = ").append(getAssessment());
        sb.append("]");
        
        return sb.toString();
    }
}
