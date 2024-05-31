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
 * An event that represents a request to clear and re-populate the ScenarioOutlineEditor's data trees.
 * 
 * @author mcambata
 */
public class PopulateScenarioTreesEvent extends ScenarioEditorEvent {

	/** The source of the event fired */
    private Serializable sourceScenarioObject = null;
	
    /**
     * Constructs an event requesting to clear and re-populate the ScenarioOutlineEditor's data trees
     * @param scenarioObject The source of the event fired
     */
    public PopulateScenarioTreesEvent(Serializable scenarioObject) {
    	setSourceScenarioObject(scenarioObject);
    }
    
    /**
     * Gets the value of sourceScenarioEvent
     * @return the Serializable containing the source of the event fired
     */
    public Serializable getSourceScenarioObject() {
    	return sourceScenarioObject;
    }
    
    /**
     * Sets the value of sourceScenarioEvent
     * @param newScenarioObject The Serializable containing the source of the event fired
     */
    private void setSourceScenarioObject(Serializable newScenarioObject) {
    	sourceScenarioObject = newScenarioObject;
    }

    @Override
    public String toString() {
        return new StringBuilder("[PopulateScenarioTreesEvent: ")
        .append("source scenario object = ").append(getSourceScenarioObject())
        .append("]").toString();
    }
}
