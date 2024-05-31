/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.AssessmentLevelEnum;

/**
 * Contains the information needed to assess a survey lesson assessment question based on the
 * reply given by a learner.
 * 
 * @author mhoffman
 *
 */
public class QuestionAssessment {
    
    /** logger instance */
    private static Logger logger = LoggerFactory.getLogger(QuestionAssessment.class);

    /** unique id of a question in a survey */
    private int questionId;
    
    /** list of question assessments for this survey */
    private List<SurveyReplyAssessment> replyAssessments;
    
    /**
     * Class constructor - set attributes
     * 
     * @param questionId - set the id of the question associated with this assessment
     * @param replyAssessments list of question assessments for this survey.  Can be empty but not null.
     */
    public QuestionAssessment(int questionId, List<SurveyReplyAssessment> replyAssessments){
        
        if(questionId < 0){
            throw new IllegalArgumentException("The question id of "+questionId+" should be non-negative.");
        }
        this.questionId = questionId;
        
        if(replyAssessments == null){
            throw new IllegalArgumentException("The reply assessments collection can't be null.");
        }
        this.replyAssessments = replyAssessments;
    }
    
    /**
     * Class constructor - set attributes from generated class's object content
     * 
     * @param question - dkf content for a question assessment
     */
    public QuestionAssessment(generated.dkf.Question question){
        
        this.questionId = question.getKey().intValue();
        
        replyAssessments = new ArrayList<SurveyReplyAssessment>(question.getReply().size());
        for(generated.dkf.Reply reply : question.getReply()){
            SurveyReplyAssessment replyAssessment = new SurveyReplyAssessment(reply);
            replyAssessments.add(replyAssessment);
        }
    }
    
    /**
     * Return the survey database unique question id for this question assessment
     * 
     * @return question id from the survey database
     */
    public int getQuestionId(){
        return questionId;
    }
    
    /**
     * Return the list of reply assessments for this question assessment.
     * 
     * @return List<SurveyReplyAssessment>
     */
    public List<SurveyReplyAssessment> getReplyAssessments(){
        return replyAssessments;
    }
    
    /**
     * Return the assessment level associated with the learner providing the given
     * reply for a particular survey lesson assessment question.
     * Note: this will probably only be called once per question, therefore iterating over the list is fine
     * 
     * @param replyId - the id of a reply
     * @return AssessmentLevelEnum
     */
    public AssessmentLevelEnum getAssessment(int replyId){
        
        AssessmentLevelEnum assessment = AssessmentLevelEnum.BELOW_EXPECTATION;
        
        for(SurveyReplyAssessment replyAssessment : replyAssessments){
            
            if(replyId == replyAssessment.getReplyId()){
                //found reply in list
                
                assessment = replyAssessment.getAssessment();
                break;
            }
        }
        
        logger.debug("Question assessment for replyId = "+replyId+" resulted in "+assessment+" for "+this);
        
        return assessment;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[QuestionAssessment: ");
        sb.append("questionId = ").append(getQuestionId());
        
        sb.append(", replyAssessments = {");
        for(SurveyReplyAssessment assessment : replyAssessments){
            sb.append(assessment).append(", ");
        }
        sb.append("}");
        
        sb.append("]");
        
        return sb.toString();
    }
}
