/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events;

/**
 * An event that notifies listeners that a team or team member's name has been changed
 * 
 * @author nroberts
 */
public class TeamRenamedEvent extends ScenarioEditorEvent {
    
    /** The team or team member's old name before the rename */
    private String oldName;
    
    /** The team or team member's new name after the rename */
    private String newName;
    
    /**
     * Creates a new team rename event
     * 
     * @param oldName the team or team member's old name before the rename
     * @param newName the team or team member's new name after the rename
     */
    public TeamRenamedEvent(String oldName, String newName) {
        this.oldName = oldName;
        this.newName = newName;
    }

    /**
     * Gets the team or team member's old name before the rename
     * 
     * @return the team or team member old name
     */
    public String getOldName() {
        return oldName;
    }

    /**
     * Gets the team or team member's new name after the rename
     * 
     * @return the team or team member's new name after the rename
     */
    public String getNewName() {
        return newName;
    }

    @Override
    public String toString() {
        return new StringBuilder("[TeamRenamedEvent: oldName ='")
                .append(oldName)
                .append("', newName = '")
                .append(newName)
                .append("']")
                .toString();
    }
}
