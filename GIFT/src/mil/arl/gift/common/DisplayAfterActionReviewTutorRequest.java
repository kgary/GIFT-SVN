/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * This class contains information about AAR that needs to be displayed by the
 * Tutor.
 *
 * @author cragusa
 */
public class DisplayAfterActionReviewTutorRequest {

    private final List<AbstractAfterActionReviewEvent> events = new ArrayList<>();
    
    /** flag used to indicate whether the AAR should be presented in full screen mode on the Tutor */
    private boolean fullscreen;
    
    /** the authorable title of this structured review */
    private String title;

    /**
     * Constructor
     *
     * @param title the authorable title of this structured review. Can't be null or empty.
     * @param events The events to display in the AAR
     */
    public DisplayAfterActionReviewTutorRequest(String title, List<AbstractAfterActionReviewEvent> events) {

        if(title == null || title.isEmpty()){
            throw new IllegalArgumentException("The title can't be null or empty.");
        }
        
        this.title = title;
        this.events.addAll(events);
    }
    
    public void setFullScreen(boolean value){
        this.fullscreen = value;
    }
    
    /**
     * Return whether the AAR should be presented in full screen mode on the Tutor
     * 
     * @return boolean
     */
    public boolean getFullScreen(){
        return fullscreen;
    }

    /**
     * Return the authorable title of this structured review
     * 
     * @return won't be null or empty.
     */
    public String getTitle(){
        return title;
    }
    /**
     * Gets the list of events to display in the AAR
     *
     * @return List<AbstractAfterActionReviewEvent> The list of event to display
     * in the AAR
     */
    public List<AbstractAfterActionReviewEvent> getEvents() {

        return events;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[DisplayAfterActionReviewTutorRequest: ");
        sb.append("title = ").append(getTitle());
        sb.append(", fullscreen = ").append(getFullScreen());
        
        sb.append(", Events = {");
        for(AbstractAfterActionReviewEvent reviewEvent : getEvents()){
            sb.append("\n").append(reviewEvent).append(",");
        }
        sb.append("}");
        
        sb.append("]");
        return sb.toString();
    }
}
