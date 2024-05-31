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
 * The parameters of the feedback to display in the Tutor User Interface
 *
 * @author jleonard
 */
public class TutorUserInterfaceFeedback implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** The action to present text feedback (as well as optional display enhancements like beep and/or flash) */
    private DisplayTextAction displayTextAction;
    
    /**  The action to play an audio file through the TUI (web browser) */
    private PlayAudioAction playAudioAction;
    
    /** The action to display an avatar and, optionally, trigger a pre-scripted avatar action such as move, speak, etc. */
    private DisplayAvatarAction displayAvatarAction;

    /** The action to clear all text feedback messages from the TUI. Can be null, which means the previous text feedback will not be cleared from the TUI. */
    private ClearTextAction clearTextAction;
    
    /** The action to display an HTML page that contains feedback content through the TUI */
    private DisplayHTMLFeedbackAction displayHTMLAction;
    
    /**
     * Default Constructor
     *
     * Required to be exist and be public for GWT compatibility
     */
    public TutorUserInterfaceFeedback() {
    }

    /**
     * Constructor
     * 
     * An illegal argument exception will be thrown if all the actions are null
     * or if two or more of the actions will play audio at the same time
     *
     * @param displayTextAction The action to present text feedback (as well as optional display enhancements like beep and/or flash)
     * @param playAudioAction The action to play an audio file through the TUI (web browser)
     * @param displayAvatarAction The action to display an avatar and, optionally, trigger a pre-scripted avatar action such as move, speak, etc..
     * @param clearTextAction The action to clear all previous text feedback messages from the TUI.  Can be null, which means the previous text feedback will not be cleared from the TUI.
     * @param displayHTMLAction the action to present a hyperlink that when clicked will open an HTML file in the browser
     */
    public TutorUserInterfaceFeedback(DisplayTextAction displayTextAction, 
            PlayAudioAction playAudioAction, 
            DisplayAvatarAction displayAvatarAction, 
            ClearTextAction clearTextAction,
            DisplayHTMLFeedbackAction displayHTMLAction) {
        
        this.displayTextAction = displayTextAction;
        this.playAudioAction = playAudioAction;
        this.displayAvatarAction = displayAvatarAction;
        this.clearTextAction = clearTextAction;
        this.displayHTMLAction = displayHTMLAction;
        
        validate();
    }

    /**
     * Gets the text feedback to display
     *
     * @return DisplayTextAction The action to display text feedback
     */
    public DisplayTextAction getDisplayTextAction() {
        
        return displayTextAction;
    }

    /**
     * Sets the text feedback to display
     *
     * @param displayTextAction The action to display text feedback
     */
    public void setDisplayTextAction(DisplayTextAction displayTextAction) {
        
        DisplayTextAction oldAction = this.displayTextAction;
        
        try {
            
            this.displayTextAction = displayTextAction;
            
            validate();
            
        } catch (RuntimeException e) {
            
            this.displayTextAction = oldAction;
            
            throw e;
        }
    }

    /**
     * Gets the audio to play as feedback
     *
     * @return PlayAudioAction The action to play audio
     */
    public PlayAudioAction getPlayAudioAction() {
        
        return playAudioAction;
    }

    /**
     * Sets the audio to play as feedback
     *
     * @param playAudioAction The action to play audio
     */
    public void setPlayAudioAction(PlayAudioAction playAudioAction) {
        
        PlayAudioAction oldAction = this.playAudioAction;
        
        try {
            
            this.playAudioAction = playAudioAction;
            
            validate();
            
        } catch (RuntimeException e) {
            
            this.playAudioAction = oldAction;
            
            throw e;
        }
    }

    /**
     * Gets the avatar to display as part of the feedback
     *
     * @return DisplayAvatarAction The action to display an avatar.  Can be null.
     */
    public DisplayAvatarAction getDisplayAvatarAction() {
        
        return displayAvatarAction;
    }

    /**
     * Sets the avatar to display as part of the feedback
     *
     * @param displayAvatarAction The action to display an avatar
     */
    public void setDisplayAvatarAction(DisplayAvatarAction displayAvatarAction) {
        
        DisplayAvatarAction oldAction = this.displayAvatarAction;
        
        try {
            
            this.displayAvatarAction = displayAvatarAction;
            
            validate();
            
        } catch (RuntimeException e) {
            
            this.displayAvatarAction = oldAction;
            
            throw e;
        }
    }

    /**
     * Gets the object that signifies that the text currently displayed in the TUI will be cleared.
     * If the object is null this means the text will not be cleared.
     * 
     * @return The object that signifies that the text currently displayed in the TUI will be cleared.     * 
     */
    public ClearTextAction getClearTextAction() {
    	
    	return clearTextAction;
    }
    
    /**
     * Gets the HTML to display as part of the feedback
     *
     * @return DisplayHTMLFeedbackAction The action to display an HTML file
     */
    public DisplayHTMLFeedbackAction getDisplayHTMLAction() {
        
        return displayHTMLAction;
    }
    
    /**
     * Determines whether this feedback only contains an 
     * avatar action
     * @return true if the avatar action is non null and all
     * other actions are null, false otherwise
     */
    public boolean onlyContainsAvatarAction() {
        return getDisplayAvatarAction() != null
                && getClearTextAction() == null
                && getPlayAudioAction() == null
                && getDisplayTextAction() == null;
    }
    
    /**
     * Determines whether this feedback only contains an 
     * clear text action
     * @return true if the clear text action is non null and all
     * other actions are null, false otherwise
     */
    public boolean onlyContainsClearAction() {
        return getClearTextAction() != null
                && getDisplayAvatarAction() == null
                && getPlayAudioAction() == null
                && getDisplayTextAction() == null;
    }
    
    /**
     * Validates that the feedback will work
     * 
     * @throws IllegalArgumentException if the feedback attributes are not valid (e.g. no feedback was specified, text
     * and avatar feedback both have competing audio tracks)
     */
    public final void validate() throws IllegalArgumentException{
        
        if (displayTextAction == null && playAudioAction == null && displayAvatarAction == null && clearTextAction == null && displayHTMLAction == null) {
            
            throw new IllegalArgumentException("No feedback specified");
        }
        
        if (displayTextAction != null && displayTextAction.hasAudio()) {
            
            if (playAudioAction != null && playAudioAction.hasAudio()) {
                
                throw new IllegalArgumentException("Text and Audio feedback both have audio.");
            }
            
            if (displayAvatarAction != null && displayAvatarAction.hasAudio()) {
                
                throw new IllegalArgumentException("Text and Avatar feedback both have audio.");                
            }
        }
        
        if (playAudioAction != null && playAudioAction.hasAudio()) {
            
            if (displayAvatarAction != null && displayAvatarAction.hasAudio()) {
                
                throw new IllegalArgumentException("Audio and Avatar feedback both have audio.");                
            }
        }
        
        if(displayTextAction != null && displayHTMLAction != null){
            
            throw new IllegalArgumentException("Can't display two things at once.");
        }
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[TutorUserInterfaceFeedback: ");
        sb.append("displayAvatarAction = ").append(getDisplayAvatarAction());
        sb.append(", displayTextAction = ").append(getDisplayTextAction());
        sb.append(", playAudioAction = ").append(getPlayAudioAction());
        sb.append(", clearTextAction = ").append(getClearTextAction());
        sb.append(", displayHTMLAction = ").append(getDisplayHTMLAction());
        sb.append("]");
        return sb.toString();
    }
}
