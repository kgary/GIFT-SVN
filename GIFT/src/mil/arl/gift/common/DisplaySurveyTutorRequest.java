/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import mil.arl.gift.common.survey.Survey;

/**
 * This class contains a request for the tutor to display a survey.  The survey elements
 * are contained within this class.
 * 
 * @author mhoffman
 *
 */
public class DisplaySurveyTutorRequest {

    /** whether to display the survey in full screen mode */
    private boolean fullScreen;
    
    /** the survey elements to display in the tutor */
    private Survey survey;
    
    /**
     * Class constructor - set attribute(s)
     * 
     * @param survey the survey elements to display in the tutor
     */
    public DisplaySurveyTutorRequest(Survey survey){
        
        if(survey == null){
            throw new IllegalArgumentException("The survey can't be null");
        }
        
        this.survey = survey;
    }
    
    /**
     * Set whether to display the survey in full screen mode
     * 
     * @param fullscreen the value to use
     */
    public void setFullscreen(boolean fullscreen){
        this.fullScreen = fullscreen;
    }
    
    /**
     * Return whether to display the survey in full screen mode
     * 
     * @return boolean the fullscreen mode value
     */
    public boolean useFullscreen(){
        return fullScreen;
    }
    
    /**
     * Return the survey elements to display in the tutor
     * 
     * @return Survey the survey to display
     */
    public Survey getSurvey(){
        return survey;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[DisplaySurveyTutorRequest: ");
        sb.append("survey = ").append(getSurvey());
        sb.append(", fullscreen = ").append(useFullscreen());
        sb.append("]");
        
        return sb.toString();
    }
}
