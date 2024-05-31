/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events;

import java.io.Serializable;

import generated.dkf.Actions.StateTransitions.StateTransition;
import mil.arl.gift.common.util.StringUtils;

/**
 * An event that represents a request for renaming of a particular scenario object.
 * 
 * @author sharrison
 */
public class RenameScenarioObjectEvent extends ScenarioEditorEvent {

    /** The scenario object to rename */
    private Serializable scenarioObject = null;

    /** The old name of the scenario object */
    private String oldName;

    /** The new name of the scenario object */
    private String newName;

    /**
     * Constructs an event requesting the renaming of a provided scenario object
     * 
     * @param scenarioObject The scenario object that has been requested for renaming. Can't be
     *        null.
     * @param oldName the old name of the scenario object. Can't be blank.
     * @param newName the new name of the scenario object. Can't be blank.
     */
    public RenameScenarioObjectEvent(Serializable scenarioObject, String oldName, String newName) {
        setScenarioObject(scenarioObject);
        setOldName(oldName);
        setNewName(newName);
    }

    /**
     * Gets the scenario object which has been requested for renaming.
     * 
     * @return The scenario object. Can't be null.
     */
    public Serializable getScenarioObject() {
        return scenarioObject;
    }

    /**
     * Sets the scenario object which has been requested for renaming.
     * 
     * @param scenarioObject The value of the scenario object. Can't be null.
     */
    private void setScenarioObject(Serializable scenarioObject) {
        if (scenarioObject == null) {
            throw new IllegalArgumentException("The parameter 'scenarioObject' cannot be null.");
        }

        this.scenarioObject = scenarioObject;
    }

    /**
     * Gets the old name.
     * 
     * @return The old name of the scenario object before it was renamed. Can't be null or empty.
     */
    public String getOldName() {
        return oldName;
    }

    /**
     * Sets the old name of the scenario object before it was renamed.
     * 
     * @param oldName The name that was replaced in the scenario object. Can't be null or empty except with {@link StateTransition}
     * because in the DKF schema the state transition name is optional.
     */
    private void setOldName(String oldName) {
        if (!(getScenarioObject() instanceof StateTransition) && StringUtils.isBlank(oldName)) {
            throw new IllegalArgumentException("The parameter 'oldName' cannot be blank.");
        }

        this.oldName = oldName;
    }

    /**
     * Gets the new name.
     * 
     * @return The new name to use for renaming the scenario object. Can't be null or empty.
     */
    public String getNewName() {
        return newName;
    }

    /**
     * Sets the name which will be used to rename the scenario object.
     * 
     * @param newName The name to use to replace the existing name of the scenario object. Can't be
     *        null or empty.
     */
    private void setNewName(String newName) {
        if (StringUtils.isBlank(newName)) {
            throw new IllegalArgumentException("The parameter 'newName' cannot be blank.");
        }

        this.newName = newName;
    }

    @Override
    public String toString() {
        return new StringBuilder("[RenameScenarioObjectEvent: ")
                .append("oldName = ").append(getOldName())
                .append(", newName = ").append(getNewName())
                .append(", scenarioObject = ").append(getScenarioObject())
                .append("]").toString();
    }
}
