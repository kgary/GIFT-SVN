/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import generated.dkf.InTutor;
import java.io.Serializable;

/**
 * Displays text as feedback
 * 
 * @author jleonard
 */
public class DisplayTextAction implements FeedbackAction, Serializable {

    private static final long serialVersionUID = 1L;

    private String displayedText;
    
    /** information on how this feedback text should be delivered to the user */
    private InTutor deliverySettings;

    /**
     * Default Constructor
     *
     * Required to be exist and be public for GWT compatibility
     */
    public DisplayTextAction() {
        
    }

    /**
     * Constructor
     *
     * @param displayedText The text to display for feedback
     */
    public DisplayTextAction(String displayedText) {

        setDisplayedText(displayedText);
    }
    
    /**
     * Set information on how this feedback text should be delivered to the user.
     *  
     * @param deliverySettings the various settings that can be used for feedback text.  Can be null.
     */
    public void setDeliverySettings(InTutor deliverySettings){
        this.deliverySettings = deliverySettings;
    }
    
    /**
     * Return information on how this feedback text should be delivered to the user.
     * 
     * @return InTutor the various settings that can be used for feedback text.  Can be null.
     */
    public InTutor getDeliverySettings(){
        return deliverySettings;
    }

    /**
     * Gets the text to display for feedback
     *
     * @return String The text to display for feedback
     */
    public String getDisplayedText() {
        return displayedText;
    }
    
    /**
     * Sets the text to display for feedback
     * 
     * @param text the text to display for feedback. Cannot be null.
     */
    public void setDisplayedText(String text) {
        
        if (text == null) {

            throw new IllegalArgumentException("The text to display cannot be null");
        }
        
        displayedText = text;
    }
    

    @Override
    public boolean hasAudio() {
        
        // Text feedback will never produce audio on its own
        return false;
    }
    
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[DisplayTextAction: ");
        sb.append("text = ").append(getDisplayedText());
        sb.append(", deliverySettings = ").append(getDeliverySettings());
        sb.append("]");
        return sb.toString();
    }
}
