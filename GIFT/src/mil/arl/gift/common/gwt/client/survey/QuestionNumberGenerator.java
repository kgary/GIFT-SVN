/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.survey;

/**
 *
 * @author jleonard
 */
public class QuestionNumberGenerator {
    
    private int currentQuestionNumber = 0;
    
    public QuestionNumberGenerator(){
    }
    
    /**
     * Increment the current question number for this generator.
     * 
     * @return the next question number
     */
    public int getNextQuestionNumber() {
        
        currentQuestionNumber += 1;
        
        return currentQuestionNumber;
    }
    
    /**
     * Return the current question number (doesn't increment or get the next question number)
     * 
     * @return the current question number
     */
    public int getCurrentQuestionNumber(){
        return currentQuestionNumber;
    }
    
}
