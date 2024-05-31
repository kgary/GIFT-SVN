/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events;

import java.io.Serializable;

/**
 * An event that notifies listeners that a place of interest was edited
 * 
 * @author nroberts
 */
public class PlaceOfInterestEditedEvent extends ScenarioEditorEvent {
    
    /** The place of interest that was edited */
    private Serializable place;
    
    /**
     * Creates a new event indicating that the given place of interest was edited
     * 
     * @param place the place of interest that was edited
     */
    public PlaceOfInterestEditedEvent(Serializable place) {
        this.place = place;
    }

    /**
     * Gets the place of interest that was edited
     * 
     * @return the place of interest that was edited
     */
    public Serializable getPlace() {
        return place;
    }

    @Override
    public String toString() {
        return new StringBuilder("[PlaceOfInterestEditedEvent: place ='")
                .append(place)
                .append("']")
                .toString();
    }
}
