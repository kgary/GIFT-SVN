/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events;

/**
 * An event that notifies listeners that a place of interest's name has been changed
 * 
 * @author nroberts
 */
public class PlaceOfInterestRenamedEvent extends ScenarioEditorEvent {
    
    /** The place of interest's old name before the rename */
    private String oldName;
    
    /** The place of interest's new name after the rename */
    private String newName;
    
    /**
     * Creates a new place of interest rename event
     * 
     * @param oldName the place of interest's old name before the rename
     * @param newName the place of interest's new name after the rename
     */
    public PlaceOfInterestRenamedEvent(String oldName, String newName) {
        this.oldName = oldName;
        this.newName = newName;
    }

    /**
     * 
     * @return
     */
    public String getOldName() {
        return oldName;
    }

    /**
     * Gets the place of interest's new name after the rename
     * 
     * @return the place of interest's new name after the rename
     */
    public String getNewName() {
        return newName;
    }

    @Override
    public String toString() {
        return new StringBuilder("[UpdatePlaceOfInterestReferencesEvent: oldName ='")
                .append(oldName)
                .append("', newName = '")
                .append(newName)
                .append("']")
                .toString();
    }
}
