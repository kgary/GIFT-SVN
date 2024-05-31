/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar;

import com.google.gwt.user.client.Command;

/**
 * An interface used to provide a timeline chart with the UI elements that are used to
 * control how it plays back its history data.
 * <br/><br/>
 * This allows the controls to affect the timeline even if they are located in a completely
 * separate part of the user interface nowhere near the chart itself.
 * 
 * @author nroberts
 */
public interface TimelineControls {

    /**
     * Gets the controls used to play, pause, and loop the history playback in the timeline
     * 
     * @return the play controls. Cannot be null.
     */
    public TimelinePlayControls getPlayControls();
    
    /**
     * Gets the controls used adjust the vertical and horizontal scale of the timeline
     * 
     * @return the scale controls. Cannot be null.
     */
    public TimelineScaleControls getScaleControls();
    
    /**
     * Refreshes any labels that are being used to display the current time of 
     * the timeline's history playback
     * 
     * @param formattedTime a formatted string displaying the current time of the history
     * playback. Will not be null.
     */
    public void refreshPlaybackTimeLabel(String formattedTime);
    
    /**
     * Sets whether the timeline controls are visible. Used to hide said controls when no
     * timeline is needed, such as for active sessions
     * 
     * @param visible whether the timeline controls should be visible
     */
    public void setVisible(boolean visible);
    
    /**
     * Sets the timeline's implementation of the control that toggles whether summative 
     * assessments are shown. This will be invoked when the user interacts with the 
     * proper UI component 
     * 
     * @param impl a command implementing the logic for the control. Can be null, if the 
     * control should do nothing
     */
    public void setSummativeButtonImpl(Command impl);
}
