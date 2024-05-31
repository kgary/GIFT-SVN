/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import mil.arl.gift.common.enums.QuestionStateEnum;

/**
 * This class contains information about a domain concept's question such as
 * the state of the question.
 * 
 * @author mhoffman
 *
 */
public class QuestionResult {

    /** the state of the question */
    private QuestionStateEnum state;
    
    /**
     * Class constructor 
     * 
     * @param state - the state of the question
     */
    public QuestionResult(QuestionStateEnum state){
    	this.state = state;
    }
    
    /**
     * Return the state of the question
     * 
     * @return QuestionStateEnum
     */
    public QuestionStateEnum getQuestionState(){
    	return state;
    }
    
    @Override
    public String toString() {
    	
        StringBuffer sb = new StringBuffer();
        sb.append("[QuestionResult:");
        sb.append(" question state = ").append(getQuestionState());
        sb.append("]");
    	
    	return sb.toString();
    }
}
