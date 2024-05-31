/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.survey.SurveyResponseMetadata;

/**
 * The details for an after action review
 * 
 * @author jleonard
 */
public class AfterActionReviewDetailsNode implements IsSerializable  {
    
    private String html;
    
    private SurveyResponseMetadata surveyResponseMetadata;
    
    private GradedScoreNode gradedScoreNode;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public AfterActionReviewDetailsNode() {
        html = null;
        surveyResponseMetadata = null;
        gradedScoreNode = null;
    }

    /**
     * Constructor, for a non-survey related event
     * 
     * @param html The HTML to display for the event.  Can't be null or empty.
     */
    public AfterActionReviewDetailsNode(String html) {
        
        if(html == null || html.isEmpty()){
            throw new IllegalArgumentException("The html string can't be null or empty.");
        }
        
        this.html = html;
        surveyResponseMetadata = null;
        gradedScoreNode = null;
    }
    
    /**
     * Constructor, for a survey related event
     * 
     * @param html The HTML to display for the survey event
     * @param surveyResponseMetadata The metadata of the response to the survey
     */
    public AfterActionReviewDetailsNode(String html, SurveyResponseMetadata surveyResponseMetadata) {
        
        if(html == null || html.isEmpty()){
            throw new IllegalArgumentException("The html string can't be null or empty.");
        }else if(surveyResponseMetadata == null){
            throw new IllegalArgumentException("The survey response can't be null.");
        }
        
        this.html = html;        
        this.surveyResponseMetadata = surveyResponseMetadata; 
        this.gradedScoreNode = null;
    }
    
    /**
     * Constructor, for a graded score node (DKF scoring)
     * 
     * @param node contains the DKF scoring results
     */
    public AfterActionReviewDetailsNode(GradedScoreNode node){
        
        if(node == null){
            throw new IllegalArgumentException("The node can't be null.");
        }
        
        this.gradedScoreNode = node;        
        this.html = null;
        this.surveyResponseMetadata = null;
    }
    
    /**
     * Gets the HTML for the event
     * 
     * @return String The HTMl for the event.  Can be null.
     */
    public String getHtml() {
        
        return html;
    }

    /**
     * Gets the response to the survey for a survey event
     *
     * @return SurveyResponse The response to the survey for a survey event. Can be null.
     */
    public SurveyResponseMetadata getSurveyResponse() {

        return surveyResponseMetadata;
    }
    
    /**
     * Return the graded score node to show in a structured review 
     * 
     * @return contains the scoring rule results
     */
    public GradedScoreNode getScoreNode(){
        return gradedScoreNode;
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[AfterActionReviewDetailsNode: ");
        sb.append("html = ").append(getHtml());
        sb.append("surveyResponse = ").append(getSurveyResponse());
        sb.append("gradedScoreNode = ").append(getScoreNode());
        sb.append("]");
        return sb.toString();

    }
    
}
