/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.io.Serializable;

/**
 * This class contains information about text that needs to be displayed by the Tutor
 *
 * @author jleonard
 */
public abstract class AbstractDisplayGuidanceTutorRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /** whether the guidance content should be displayed in full screen on the tutor */
    private boolean fullscreen = false;
    
    /** amount of milliseconds to display the guidance on the tutor */
    private int displayDuration = 0;
    
    /** flag used to indicate if this guidance is to be shown only when the training app is loading */
    private boolean whileTrainingAppLoads;

    /**
     * Default constructor
     */
    public AbstractDisplayGuidanceTutorRequest() {
    }

    /**
     * Class constructor 
     * 
     * @param fullscreen whether the guidance content should be displayed in full screen on the tutor
     */
    public AbstractDisplayGuidanceTutorRequest(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }

    /**
     * Class constructor
     *
     * @param fullscreen whether the guidance content should be displayed in full screen on the tutor
     * @param displayDuration The amount of time in milliseconds the text should be displayed.
     * When the value is:
     * i. 0 AND whileTrainingAppLoads = false : allow user to choose when to continue
     * ii. 0 AND whileTrainingAppLoads = true : show guidance until training app is started, then guidance is cleared
     * iii. greater than 0 AND whileTrainingAppLoads = false : show continue button after 'duration' seconds
     * iv. greater than 0 AND whileTrainingAppLoads = true : show guidance until training app is started, then guidance is cleared (i.e. duration is ignored) 
     */
    public AbstractDisplayGuidanceTutorRequest(boolean fullscreen, int displayDuration) {
        this.fullscreen = fullscreen;
        this.displayDuration = displayDuration;
    }
    
    /**
	 * Returns if the guidance should be displayed full screen
     * 
     * @return boolean If the guidance should be displayed full screen
     */
    public boolean isFullscreen() {

        return fullscreen;
    }

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
        sb.append(", fullscreen = ").append(isFullscreen());
        sb.append(", whileTrainingAppLoads = ").append(isWhileTrainingAppLoads());

        return sb.toString();
    }
}
