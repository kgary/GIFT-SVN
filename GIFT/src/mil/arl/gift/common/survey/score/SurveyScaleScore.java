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
 * This is a score for a survey that has one or more scales associated with its
 * questions.
 *
 * @author mhoffman
 */
public class SurveyScaleScore extends AbstractScaleScore {

    /** collection of each question's scoring information */
    private List<QuestionScaleScore> questionScores;

    /**
     * Class constructor
     */
    public SurveyScaleScore() {
        questionScores = new ArrayList<>();
    }

    /**
     * Return the collection of each question's scoring information
     *
     * @return List<QuestionScaleScore> Gets the collection of each question's
     * scoring information
     */
    public List<QuestionScaleScore> getQuestionScores() {
        return questionScores;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[SurveyScaleScore: ");
        sb.append("questionScores = {");
        for (QuestionScaleScore score : questionScores) {
            sb.append(score.toString()).append(", ");
        }
        sb.append("}");
        sb.append(", ").append(super.toString());
        sb.append("]");

        return sb.toString();
    }
}
