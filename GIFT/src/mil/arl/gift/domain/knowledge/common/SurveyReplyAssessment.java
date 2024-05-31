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
 * Contains the assessment to give if a particular reply is given by a learner for a 
 * survey lesson assessment.
 * 
 * @author mhoffman
 *
 */
public class SurveyReplyAssessment {

    /** the unique id of a reply */
    private int replyId;
    
    /** the assessment to give if this reply is selected for a survey lesson assessment question */
    private AssessmentLevelEnum assessment;
    
    /**
     * Class constructor - set attributes
     * 
     * @param replyId the unique db id of a reply 
     * @param assessment the assessment to give if this reply is selected for a survey lesson assessment question
     */
    public SurveyReplyAssessment(int replyId, AssessmentLevelEnum assessment){
        this.replyId = replyId;
        this.assessment = assessment;
    }
    
    /**
     * Class constructor - set attributes from generated class's object contents
     * 
     * @param reply - dkf content for a survey reply assessment
     */
    public SurveyReplyAssessment(generated.dkf.Reply reply){
        this.replyId = reply.getKey().intValue();
        this.assessment = AssessmentLevelEnum.valueOf(reply.getResult());
    }
    
    public int getReplyId(){
        return replyId;
    }
    
    public AssessmentLevelEnum getAssessment(){
        return assessment;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[SurveyReplyAssessment: ");
        sb.append("replyId = ").append(getReplyId()); 
        sb.append(", assessment = ").append(getAssessment());
        sb.append("]");
        
        return sb.toString();
    }
}
