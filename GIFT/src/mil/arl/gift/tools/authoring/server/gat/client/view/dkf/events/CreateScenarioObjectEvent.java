/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events;

import java.io.Serializable;

import generated.dkf.Concept;
import generated.dkf.Condition;
import generated.dkf.Task;

/**
 * An event that represents a request for creation of a particular scenario object.
 * 
 * @author sharrison
 */
public class CreateScenarioObjectEvent extends ScenarioEditorEvent {
    
    /** Optional parent field. Mandatory for {@link Concept} and {@link Condition} */
    private Serializable parent = null;
    
    /** The scenario object to create */
    private Serializable scenarioObject = null;
    
    /**
     * Constructs an event requesting the creation of a provided scenario object
     * 
     * @param scenarioObject The scenario object that has been requested for creation. Can't be
     *        null.
     * @param parent The parent of the scenario object to create. This is only required for creating
     *        {@link Concept concepts} and {@link Condition conditions}.
     */
    public CreateScenarioObjectEvent(Serializable scenarioObject, Serializable parent) {
        setScenarioObject(scenarioObject);
        setParent(parent);
    }

    /**
     * Gets the scenario object which has been requested for creation.
     * 
     * @return The scenario object. Can't be null.
     */
    public Serializable getScenarioObject() {
        return scenarioObject;
    }

    /**
     * Sets the scenario object which has been requested for creation.
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
     * Gets the parent of the object which has been requested for creation.
     * 
     * @return The parent object. Can be null.
     */
    public Serializable getParent() {
        return parent;
    }

    /**
     * Sets the parent of the scenario object which has been requested for creation.
     * 
     * @param parent The value of the parent. Can't be null if the {@link #scenarioObject} is a {@link Concept} or {@link Condition}.
     */
    private void setParent(Serializable parent) {
        if (getScenarioObject() instanceof Concept) {
            if (parent == null) {
                throw new IllegalArgumentException(
                        "The parameter 'parent' cannot be null since the created object is a Concept.");
            } else if (!(parent instanceof Task || parent instanceof Concept)) {
                throw new IllegalArgumentException(
                        "The parameter 'parent' must be of type Task or Concept since the created object is a Concept.");
            }
        } else if (getScenarioObject() instanceof Condition) {
            if (parent == null) {
                throw new IllegalArgumentException(
                        "The parameter 'parent' cannot be null since the created object is a Condition.");
            } else if (!(parent instanceof Concept)) {
                throw new IllegalArgumentException(
                        "The parameter 'parent' must be of type Concept since the created object is a Condition.");
            }
        }

        this.parent = parent;
    }
    
    @Override
    public String toString() {
        return new StringBuilder("[CreateScenarioObjectEvent: ")
                .append("scenario object = ").append(getScenarioObject())
                .append("parent = ").append(getParent())
                .append("]").toString();
    }
}
