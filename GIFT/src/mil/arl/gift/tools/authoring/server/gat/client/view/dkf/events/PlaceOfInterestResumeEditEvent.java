/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events;

import java.io.Serializable;

import generated.dkf.Condition;

/**
 * An event that notifies Avoid Location condition editor listeners to resume
 * editing after returning from the map editor in GIFT Wrap
 * 
 * @author cpadilla
 */
public class PlaceOfInterestResumeEditEvent extends ScenarioEditorEvent {
    
    /** The first place of interest that was created or selected on the map */
    private Serializable place;
    
    /** The condition for which the place was edited for and which to resume editing */
    private Condition condition;
    
    /** Whether the condition should remove the default value on resume */
    private boolean cleanupDefault;
    
    /**
     * Creates a new event indicating that the given place of interest was created
     * or selected
     * 
     * @param place the place of interest that was created or selected
     * @param condition the condition for which to resume editing
     * @param cleanupDefault whether the condition should remove the default value on resume
     */
    public PlaceOfInterestResumeEditEvent(Serializable place, Condition condition, boolean cleanupDefault) {
        this.place = place;
        this.condition = condition;
        this.cleanupDefault = cleanupDefault;
    }

    /**
     * Gets the place of interest that was created or selected
     * 
     * @return the place of interest that was created or selected
     */
    public Serializable getPlace() {
        return place;
    }
    
    /**
     * Gets the condition for which the place was edited for and which to resume editing
     * 
     * @return the condition for which the place was edited for
     */
    public Condition getCondition() {
        return condition;
    }

    /**
     * Gets whether the condition should remove the default value on resume
     * 
     * @return whether the condition should remove the default value on resume
     */
    public boolean getCleanupDefault() {
        return cleanupDefault;
    }
    
    @Override
    public String toString() {
        return new StringBuilder("[PlaceOfInterestEditedEvent: place ='")
                .append(place)
                .append("']")
                .toString();
    }
}
