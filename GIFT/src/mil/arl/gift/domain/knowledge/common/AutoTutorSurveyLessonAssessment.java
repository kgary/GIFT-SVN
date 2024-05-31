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
 * Contains the information needed to assess an AutoTutor survey lesson assessment based on the
 * replies given by a learner.
 * 
 * @author mhoffman
 *
 */
public class AutoTutorSurveyLessonAssessment extends
        AbstractSurveyLessonAssessment {
    
    /** unique AutoTutor chat session id used to match and sync chat updates and logs */
    private int chatId;
    
    /**
     * Class constructor - set attribute
     * 
     * @param chatId unique AutoTutor chat session id used to match and sync chat updates and logs
     */
    public AutoTutorSurveyLessonAssessment(int chatId){
        setChatId(chatId);
    }
    
    private void setChatId(int chatId){
        
        if(chatId <= 0){
            throw new IllegalArgumentException("The chat id of "+chatId+" must be greater than zero to be considered a valid id.");
        }
        
        this.chatId = chatId;
    }
    
    /**
     * Return the unique AutoTutor chat session id used to match and sync chat updates and logs
     * 
     * @return int
     */
    public int getChatId(){
        return chatId;
    }

    @Override
    public AssessmentLevelEnum getAssessment(Object surveyResult) {
        throw new UnsupportedOperationException("The logic to assess a mid-lesson AutoTutor session is currently implemented in AutoTutorWebServiceInterfaceCondition.java");
    }

}
