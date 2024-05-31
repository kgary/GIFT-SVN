/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.lti;

import java.util.List;

import mil.arl.gift.common.score.GradedScoreNode;

/**
 * The LtiLessonGradedScoreRequest class encapsulates the data needed send the graded score result
 * from the LTI provider.
 * 
 * @author sharrison
 *
 */
public class LtiLessonGradedScoreRequest {

    /** The graded score node */
    private GradedScoreNode gradedScoreNode;

    /** The course concepts */
    private List<String> courseConcepts;

    /**
     * Class constructor
     * 
     * @param gradedScoreNode the graded score node.
     * @param courseConcepts the course concepts.
     */
    public LtiLessonGradedScoreRequest(GradedScoreNode gradedScoreNode, List<String> courseConcepts) {
        if (gradedScoreNode == null) {
            throw new IllegalArgumentException("The graded score node can't be null");
        } else if (courseConcepts == null) {
            throw new IllegalArgumentException("The course concepts can't be null");
        }

        this.gradedScoreNode = gradedScoreNode;
        this.courseConcepts = courseConcepts;
    }

    /**
     * @return the gradedScoreNode
     */
    public GradedScoreNode getGradedScoreNode() {
        return gradedScoreNode;
    }

    /**
     * @return the courseConcepts
     */
    public List<String> getCourseConcepts() {
        return courseConcepts;
    }

    @Override
    public String toString() {
        StringBuilder toStringBuilder = new StringBuilder();
        toStringBuilder.append("LtiLessonGradedScoreRequest [");
        toStringBuilder.append("gradedScoreNode=").append(gradedScoreNode);
        toStringBuilder.append(", courseConcepts=").append(courseConcepts);
        toStringBuilder.append("]");
        return toStringBuilder.toString();
    }
}
