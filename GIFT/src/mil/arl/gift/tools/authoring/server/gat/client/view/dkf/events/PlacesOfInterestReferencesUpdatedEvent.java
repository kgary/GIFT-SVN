/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events;

/**
 * An event that notifies listeners that the global map of places of interest references has been updated.
 * 
 * @author nroberts
 */
public class PlacesOfInterestReferencesUpdatedEvent extends ScenarioEditorEvent {

    /**
     * Constructs an notifying listeners that the global map of places of interest references has been updated
     */
    public PlacesOfInterestReferencesUpdatedEvent() {
    }
    
    @Override
    public String toString() {
        return new StringBuilder("[PlacesOfInterestReferencesUpdatedEvent]").toString();
    }
}
