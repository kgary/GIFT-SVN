/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.io.Serializable;

import generated.dkf.Condition;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * An abstract panel responsible for editing a specific type of ConditionInput
 * 
 * @author tflowers
 *
 * @param <T> The type of input to edit (i.e. type returned by {@link Input#getType()})
 */
public abstract class ConditionInputPanel<T extends Serializable> extends ScenarioValidationComposite {

    /** The condition that is currently being edited */
    private Condition condition = null;
    
    /** Dirty flag for the condition editor. Will be set to true if anything is changed. */
    private static boolean dirty = false;

    /** The condition input that is currently being edited */
    private T input = null;

    /**
     * Sets the condition input that is being edited by this {@link ConditionInputPanel}
     * 
     * @param input The new input object to edit, can't be null. Can't be null.
     * @param condition The condition that contains the input. Can't be null. Must contain 'input'.
     */
    public void edit(T input, Condition condition) {
        if (input == null) {
            throw new IllegalArgumentException("The parameter 'input' cannot be null.");
        } else if (condition == null) {
            throw new IllegalArgumentException("The parameter 'condition' cannot be null.");
        } else if (condition.getInput() == null || condition.getInput().getType() != input) {
            throw new IllegalArgumentException("The parameter 'condition' must contain the parameter 'input'.");
        }
       
        this.input = input;
        this.condition = condition;
        
        /* onEdit should not be firing validation requests; that will be handled in the condition
         * panel */
        onEdit();
        setReadonly(ScenarioClientUtility.isReadOnly());

        setClean();
    }

    /**
     * Gets the input that is currently being edited.
     * 
     * @return The input that is currently being edited. Can't be null after {@link #onEdit()} has
     *         been invoked.
     */
    public T getInput() {
        return input;
    }
    
    /**
     * Gets the condition that is currently being edited.
     * 
     * @return The condition that is currently being edited. Can't be null after {@link #onEdit()} has
     *         been invoked.
     */
    public Condition getCondition() {
        return condition;
    }

    /**
     * Method that is called once {@link #edit(Serializable)} has been called and the new value for
     * {@link #input} has been verified. This method should be responsible for populating the UI
     * with the current state of the condition input.
     */
    protected abstract void onEdit();
    
    /**
     * Retrieves the dirty flag for the condition editor.
     *
     * @return the dirty flag. True if anything has changed without saving.
     */
    public static boolean isDirty() {
        return dirty;
    }

    /**
     * Sets the dirty flag for the condition editor to true. Should be called if anything in the
     * editor has changed without being saved.
     */
    public void setDirty() {
        dirty = true;
    }

    /**
     * Sets the dirty flag for the condition editor to false. Should only be called on save.
     */
    public void setClean() {
        dirty = false;
    }
    
    /**
     * Sets the components to read only mode which prevents users from making changes.
     * 
     * @param isReadonly True to prevent editing, false to allow editing.
     */
    protected abstract void setReadonly(boolean isReadonly);
}
