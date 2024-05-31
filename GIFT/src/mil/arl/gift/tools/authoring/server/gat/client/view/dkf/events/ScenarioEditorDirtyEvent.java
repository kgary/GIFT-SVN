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
 * Fires whenever a change is made within the DKF scenario editor
 */
public class ScenarioEditorDirtyEvent extends ScenarioEditorEvent {

    /**
     * The scenario object that was the origin of the dirty editor event.
     */
    private Serializable sourceScenarioObject;
    
    /**
     * Constructor. No validation will occur since the {@link #sourceScenarioObject} was not set.
     * Use the other constructor to trigger validation.
     */
    public ScenarioEditorDirtyEvent() {
        this(null);
    }
    
    /**
     * Constructor.
     * 
     * @param sourceScenarioObject the scenario object that was the origin of the dirty editor
     *        event. Set to null if the event should be untraceable (e.g. no panels need to validate on the change).
     */
    public ScenarioEditorDirtyEvent(Serializable sourceScenarioObject) {
        this.sourceScenarioObject = sourceScenarioObject;
    }

    /**
     * The scenario object that was the origin of the dirty editor event.
     * 
     * @return the scenario object that fired the event. Can be null.
     */
    public Serializable getSourceScenarioObject() {
        return sourceScenarioObject;
    }

    @Override
    public String toString() {
        return new StringBuilder("[ScenarioEditorDirtyEvent: ")
                .append("source scenario object = ").append(getSourceScenarioObject())
                .append("]").toString();
    }
}
