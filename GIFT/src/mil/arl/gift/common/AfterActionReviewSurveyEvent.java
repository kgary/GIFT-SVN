/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.util.List;
import mil.arl.gift.common.survey.SurveyResponseMetadata;
import mil.arl.gift.common.survey.score.ScoreInterface;

/**
 * A survey event to display in the AAR
 *
 * @author jleonard
 */
public class AfterActionReviewSurveyEvent extends AbstractAfterActionReviewEvent {
    
    private SurveyResponseMetadata surveyResponseMetadata;
    
    private List<ScoreInterface> surveyScores;
    
    /**
     * Constructor
     * 
     * @param courseObjectName the unique name of the course object that created this event to be reviewed.
     * @param surveyResponse The completed response to the survey.  Can't be null.
     * @param surveyScores The list of scores for the survey.  Can be empty but not null.
     */
    public AfterActionReviewSurveyEvent(String courseObjectName, SurveyResponseMetadata surveyResponse, List<ScoreInterface> surveyScores) {
        super(courseObjectName);
        
        if(surveyResponse == null){
            throw new IllegalArgumentException("The survey response can't be null.");
        }
        
        if(surveyScores == null){
            throw new IllegalArgumentException("The survey scores collection can't be null.");
        }
        
        this.surveyResponseMetadata = surveyResponse;
        this.surveyScores = surveyScores;
    }
    
    /**
     * Constructor for survey events that may or may not be shown in the AAR 
     * 
     * @param courseObjectName the unique name of the course object that created this event to be reviewed.
     * @param surveyResponse The completed response to the survey.  Can't be null.
     * @param surveyScores The list of scores for the survey.  Can be empty but not null.
     * @param showInAAR Whether to display the survey responses in the After Action Review
     */
    public AfterActionReviewSurveyEvent(String courseObjectName, SurveyResponseMetadata surveyResponse, List<ScoreInterface> surveyScores, boolean showInAAR) {      
    	super(courseObjectName, showInAAR);
    	
        if(surveyResponse == null){
            throw new IllegalArgumentException("The survey response can't be null.");
        }
        
        if(surveyScores == null){
            throw new IllegalArgumentException("The survey scores collection can't be null.");
        }
        
        this.surveyResponseMetadata = surveyResponse;
        this.surveyScores = surveyScores;
    }
    
    /**
     * Gets the response to the survey completed
     * 
     * @return SurveyResponse The response to the survey completed
     */
    public SurveyResponseMetadata getSurveyResponseMetadata() {
        
        return surveyResponseMetadata;
    }
    
    /**
     * Gets the list of scores for the survey
     * 
     * @return The list of scores for the survey
     */
    public List<ScoreInterface> getSurveyScores() {
        
        return surveyScores;
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[AfterActionReviewSurveyEvent: ");
        sb.append(super.toString());
        sb.append(", surveyResponseMetadata =\n").append(surveyResponseMetadata);
        sb.append(",\n surveyScores = {\n");
        for(ScoreInterface score : getSurveyScores()){
            sb.append(score).append(",");
        }
        sb.append("}");
        sb.append("]");
        
        return sb.toString();
    }
}
