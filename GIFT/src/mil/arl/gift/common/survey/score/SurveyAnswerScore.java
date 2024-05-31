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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a score for a survey that has question(s) with correct answer(s).
 *
 * @author mhoffman
 */
public class SurveyAnswerScore extends AbstractAnswerScore {
    
    private static final Logger logger = LoggerFactory.getLogger(SurveyAnswerScore.class);

    /** collection of each question's scoring information */
    private List<QuestionAnswerScore> questionScores;

    /**
     * Class constructor
     */
    public SurveyAnswerScore() {
        questionScores = new ArrayList<>();
    }

    /**
     * Class constructor - set known attributes from already scored survey
     *
     * @param totalEarnedPoints The points earned for the question
     * @param highestPossiblePoints The highest number of points that could be
     * earned on this question
     */
    public SurveyAnswerScore(double totalEarnedPoints, double highestPossiblePoints) {
        super(totalEarnedPoints, highestPossiblePoints);
        questionScores = new ArrayList<>();
    }

    /**
     * Return the collection of each question's scoring information
     *
     * @return List<QuestionAnswerScore> Gets the collection of each question's
     * scoring information
     */
    public List<QuestionAnswerScore> getQuestionScores() {
        return questionScores;
    }

    @Override
    public void collate() {
        
        if(logger.isInfoEnabled()) {
            logger.info("Collating "+questionScores.size()+" question scores to determine total earned and highest possible points");
        }

        // calculate total points from all the questions
        double highestPossibleScore = 0, totalEarnedScore = 0;
        for (QuestionAnswerScore qas : questionScores) {
            qas.collate();
            highestPossibleScore += qas.getHighestPossiblePoints();
            totalEarnedScore += qas.getTotalEarnedPoints();
        }        

        setHighestPossiblePoints(highestPossibleScore);
        setTotalEarnedPoints(totalEarnedScore);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[SurveyAnswerScore: ");
        sb.append(super.toString());
        sb.append(", questionScores = {");
        for (QuestionAnswerScore score : questionScores) {
            sb.append(score.toString()).append(", ");
        }
        sb.append("}");
        sb.append("]");

        return sb.toString();
    }
}
