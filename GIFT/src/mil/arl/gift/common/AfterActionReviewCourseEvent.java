/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import mil.arl.gift.common.score.GradedScoreNode;

/**
 * A course event to display in the AAR
 *
 * @author jleonard
 */
public class AfterActionReviewCourseEvent extends AbstractAfterActionReviewEvent {

    private GradedScoreNode gradedScoreNode;

    /**
     * Constructor
     *
     * @param courseObjectName the unique name of the course object that created this event to be reviewed.
     * @param gradedScoreNode The score of the course event
     */
    public AfterActionReviewCourseEvent(String courseObjectName, GradedScoreNode gradedScoreNode) {
        super(courseObjectName);
        
        this.gradedScoreNode = gradedScoreNode;
    }

    /**
     * Gets the score of the course event
     *
     * @return GradedScoreNode The score of the course event
     */
    public GradedScoreNode getScore() {
        return gradedScoreNode;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[AfterActionReviewCourseEvent: ");
        sb.append(super.toString());
        sb.append(", score node = ").append(gradedScoreNode);
        sb.append("]");
        return sb.toString();
    }
}
