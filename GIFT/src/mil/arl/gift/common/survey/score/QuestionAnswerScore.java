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
 * This is a score for a question that has a correct answer.
 *
 * @author mhoffman
 */
public class QuestionAnswerScore extends AbstractAnswerScore {

    /** collection of scores for the question's replies */
    private List<ReplyAnswerScore> replyScores;

    /**
     * Class constructor
     */
    public QuestionAnswerScore() {
        replyScores = new ArrayList<>();
    }

    /**
     * Class constructor - set known attributes from already scored question
     *
     * @param totalEarnedPoints The points earned for the question
     * @param highestPossiblePoints The highest number of points that could be
     * earned on this question
     */
    public QuestionAnswerScore(double totalEarnedPoints, double highestPossiblePoints) {
        super(totalEarnedPoints, highestPossiblePoints);
        replyScores = new ArrayList<>();
    }

    /**
     * Return the collection of reply scores.
     *
     * @return List<ReplyAnswerScore> Gets the collection of reply scores
     */
    public List<ReplyAnswerScore> getReplyScores() {
        return replyScores;
    }

    @Override
    public void collate() {

        // calculate total question points from the question's replies
        double highestPossibleScore = 0, totalEarnedScore = 0;
        for (ReplyAnswerScore ras : replyScores) {
            highestPossibleScore += ras.getHighestPossiblePoints();
            totalEarnedScore += ras.getTotalEarnedPoints();
        }        
        
        setHighestPossiblePoints(highestPossibleScore);
        setTotalEarnedPoints(totalEarnedScore);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[QuestionAnswerScore: ");
        sb.append(super.toString());
        sb.append(", replyScores = {");
        for (ReplyAnswerScore score : replyScores) {
            sb.append(score.toString()).append(", ");
        }
        sb.append("}");
        sb.append("]");

        return sb.toString();
    }
}
