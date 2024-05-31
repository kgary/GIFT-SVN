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
 * An event that indicates a scenario object has changed its references.
 * 
 * @author sharrison
 */
public class ReferencesChangedEvent extends ScenarioEditorEvent {
    
    /** The source of the event fired */
    private Serializable refChangedSource = null;
    
    /** The old value that was replaced by the new value */
    private Serializable oldValue = null;
    
    /** The new value that the scenario object changed to */
    private Serializable newValue = null;

    /**
     * Constructs an event indicating a reference change for the provided scenario object that fired
     * the event.
     * 
     * @param refChangedSource the source of the event fired. Can't be null.
     * @param oldValue the old value that was replaced by the new value.
     * @param newValue the new value that the scenario object changed to.
     */
    public ReferencesChangedEvent(Serializable refChangedSource, Serializable oldValue, Serializable newValue) {
        setReferenceChangedSource(refChangedSource);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Gets the source of the reference change fired event.
     * 
     * @return the source of the event fired. Can't be null.
     */
    public Serializable getReferenceChangedSource() {
        return refChangedSource;
    }

    /**
     * Sets the source of the reference change fired event.
     * 
     * @param refChangedSource the source of the event fired. Can't be null.
     */
    private void setReferenceChangedSource(Serializable refChangedSource) {
        if (refChangedSource == null) {
            throw new IllegalArgumentException("The parameter 'refChangedSource' cannot be null.");
        }

        this.refChangedSource = refChangedSource;
    }
    
    /**
     * Gets the old reference value for the scenario object.
     * 
     * @return The old reference value. Can be null.
     */
    public Serializable getOldValue() {
        return oldValue;
    }
    
    /**
     * Gets the new reference value for the scenario object.
     * 
     * @return The new reference value. Can be null.
     */
    public Serializable getNewValue() {
        return newValue;
    }
    
    @Override
    public String toString() {
        return new StringBuilder("[ReferencesChangedEvent: ")
                .append("old value = ").append(getOldValue())
                .append(", new value = ").append(getNewValue())
                .append(", reference changed source = ").append(getReferenceChangedSource())
                .append("]").toString();
    }

}
