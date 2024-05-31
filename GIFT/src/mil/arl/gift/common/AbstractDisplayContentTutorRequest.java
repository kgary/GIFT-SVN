/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import generated.course.Guidance;

import java.io.Serializable;

/**
 * This class contains information about text that needs to be displayed by the Tutor
 *
 * @author jleonard
 */
public abstract class AbstractDisplayContentTutorRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** amount of milliseconds to display the guidance on the tutor */
    private int displayDuration = 0;
    
    /** flag used to indicate if this guidance is to be shown only when the training app is loading */
    private boolean whileTrainingAppLoads = false;

    /**
     * Default constructor
     */
    public AbstractDisplayContentTutorRequest() {
    }
    
    /**
     * Default constructor
     */
    public AbstractDisplayContentTutorRequest(int displayDuration, boolean whileTrainingAppLoads) {
    	this.displayDuration = displayDuration;
        this.whileTrainingAppLoads = whileTrainingAppLoads;
    }
    
    public static AbstractDisplayContentTutorRequest getRequest(Serializable transition) {
    	if(transition instanceof Guidance) {
    		Guidance guidance = (Guidance) transition;
    		if(guidance.getGuidanceChoice() instanceof Guidance.Message) {
    			return new DisplayMessageTutorRequest(guidance);
    		}
    	}
    	
    	return new DisplayMediaTutorRequest(transition);
    }
    
    /**
     * Return whether the content should be shown in full screen mode.
     * 
     * @return true if the content should be shown in full screen display
     */
    public abstract boolean isFullscreen();
        
    public abstract String getMessage();
    
    /**
     * Return the title of the content.  This is often used as a title on the webpage/dialog or course object navigation.
     * 
     * @return can be null if not set
     */
    public abstract String getTitle();
    
    /**
     * Returns the amount of time in milliseconds the text should be displayed before being removed
     * 
     * If set to zero, display indefinitely
     * 
     * @return The amount of time the text should be displayed before being
     * removed
     */
    public int getDisplayDuration() {
        return displayDuration;
    }
    
    public void setDisplayDuration(int displayDuration) {
    	this.displayDuration = displayDuration;
    }

    /**
     * Return whether this guidance is for when the training app loads
     * 
     * @return should this guidance be displayed while the training app loads
     */
    public boolean isWhileTrainingAppLoads() {
        return whileTrainingAppLoads;
    }

    /**
     * Set the flag used to indicate if this guidance is to be shown only when the training app is loading 
     * 
     * @param whileTrainingAppLoads whether the guidance should be displayed while the training app loads
     */
    public void setWhileTrainingAppLoads(boolean whileTrainingAppLoads) {
        this.whileTrainingAppLoads = whileTrainingAppLoads;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("display duration = ").append(getDisplayDuration()).append(" ms");
        sb.append(", whileTrainingAppLoads = ").append(isWhileTrainingAppLoads());

        return sb.toString();
    }
}
