/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey.score;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a score for a question that has one or more scales associated with its answers.
 * 
 * @author mhoffman
 *
 */
public class QuestionScaleScore extends AbstractScaleScore {

    /** collection of scores for the question's replies */
    private List<ReplyScaleScore> replyScores;
    
    /**
     * Class constructor
     */
    public QuestionScaleScore(){
        replyScores = new ArrayList<>();
    }
    
    public List<ReplyScaleScore> getReplyScores(){
        return replyScores;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[QuestionScaleScore: ");
        sb.append("replyScores = {");
        for(ReplyScaleScore score : replyScores){
            sb.append(score.toString()).append(", ");
        }
        sb.append("}");
        sb.append("]");
        
        return sb.toString();
    }
}
