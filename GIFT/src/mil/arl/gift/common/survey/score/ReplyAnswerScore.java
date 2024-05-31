/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey.score;


/**
 * This is a score for a reply that has a correct answer.
 * Note: this class represents a notional grouping of one or more responses to a question.  A single question
 * can have multiple groupings, i.e. multiple instances of this class, one for each grouping.  For example, a multiple choice
 * question will have a single instance of this class, while a matrix of choices question may have an instance for each row and/or column.
 * 
 * @author mhoffman
 *
 */
public class ReplyAnswerScore extends AbstractAnswerScore {

    /**
     * Class constructor - set attributes
     * 
     * @param totalEarnedPoints total points from all question scores
     * @param highestPossiblePoints highest number of possible points for the corresponding questions
     */
    public ReplyAnswerScore(double totalEarnedPoints, double highestPossiblePoints){
        super(totalEarnedPoints, highestPossiblePoints);
    }
    
    @Override
    public void collate() {
        //nothing to do here
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[ReplyAnswerScore: ");
        sb.append(super.toString());
        sb.append("]");
        return sb.toString();
    }
}
